package io.github.portfoligno.dex.permission

import java.io.File

import cats.Parallel
import cats.effect.ConcurrentEffect
import cats.syntax.functor._
import io.github.portfoligno.dex.permission.data.{ClassMethod, ClassName, MethodIdentity}
import io.github.portfoligno.dex.permission.settings.MappingSource
import io.github.portfoligno.dex.permission.utility.->
import org.jf.dexlib2.DexFileFactory
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.MethodReference

import scala.collection.convert.ImplicitConversionsToScala._
import scala.language.reflectiveCalls

object PermissionAnalyzer {
  def analyzeDexContainer[M[_] : ConcurrentEffect, F[_]](
    file: File,
    mappingSource: MappingSource = MappingSource()
  )(
    implicit F: Parallel[M, F]
  ): M[List[AnalysisResult]] =
    mappingSource.fetch().map { table =>
      val container = DexFileFactory.loadDexContainer(file, null)

      val methods = for {
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

      methods
        .groupBy(_._1)
        .view
        .flatMap {
          case id -> seq =>
            table.get(id).map(mapping =>
              AnalysisResult(
                id,
                seq
                  .map {
                    case _ -> m =>
                      ClassMethod(
                        ClassName.fromByteCodeClassName(m.getDefiningClass),
                        m.getName,
                        m.getParameterTypes.view.map(ClassName.fromByteCodeClassName).toList
                      )
                  }
                  .toSet,
                mapping
                  .view
                  .flatMap {
                    case className -> permissions =>
                      permissions.map(_ -> className)
                  }
                  .groupBy(_._1)
                  .mapValues(_.map(_._2).toSet)
              )
            )
        }
        .toList
    }
}
