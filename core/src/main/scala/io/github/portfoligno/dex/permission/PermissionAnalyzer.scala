package io.github.portfoligno.dex.permission

import java.io.File

import cats.Parallel
import cats.effect.ConcurrentEffect
import cats.syntax.functor._
import io.github.portfoligno.dex.permission.data.{ClassMethod, ClassName, MethodIdentity}
import io.github.portfoligno.dex.permission.settings.MappingSource
import org.jf.dexlib2.DexFileFactory
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.MethodReference

import scala.collection.convert.ImplicitConversionsToScala._

object PermissionAnalyzer {
  import io.github.portfoligno.dex.permission.utility._

  def analyzeDexContainer[M[_] : ConcurrentEffect, F[_]](
    file: File,
    mapping: MappingSource = MappingSource()
  )(
    implicit F: Parallel[M, F]
  ): M[List[AnalysisResult]] =
    mapping.fetch().map { permissionLookup =>
      val container = DexFileFactory.loadDexContainer(file, null)

      val callers = for {
        method <- container
          .getDexEntryNames
          .view
          .flatMap(container.getEntry(_).getClasses.toSeq)
          .flatMap(_.getMethods)

        methodReference <- Option(method.getImplementation)
          .toSeq
          .flatMap(_.getInstructions)
          .flatMap(classTagOf[ReferenceInstruction].unapply)
          .flatMap(i => classTagOf[MethodReference].unapply(i.getReference))

        id = MethodIdentity(methodReference.getName, methodReference
          .getParameterTypes
          .view
          .map(ClassName.fromByteCodeClassName)
          .toList)
      }
        yield id -> method

      callers
        .groupBy(_._1)
        .view
        .flatMap {
          case id -> methods =>
            permissionLookup.get(id).map(permissions =>
              AnalysisResult(
                id,
                methods
                  .map {
                    case _ -> m =>
                      ClassMethod(
                        ClassName.fromByteCodeClassName(m.getDefiningClass),
                        m.getName,
                        ClassName.fromByteCodeClassNameK(m.getParameterTypes).toList
                      )
                  }
                  .toSet,
                permissions
                  .view
                  .flatMap {
                    case y -> xs =>
                      xs.map(_ -> y)
                  }
                  .groupBy(_._1)
                  .mapValues(_.map(_._2).toSet)
              )
            )
        }
        .toList
    }
}
