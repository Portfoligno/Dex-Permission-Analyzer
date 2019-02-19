package io.github.portfoligno.dex.permission

import java.io.File

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

    def analyzeDexContainer[F[_] : ConcurrentEffect](
      file: File,
      mapping: MappingSource = MappingSource()
    ): F[List[AnalysisResult]] =
      mapping.fetch().map { permissionLookup =>
        val container = DexFileFactory.loadDexContainer(file, null)

        val callers = for {
          method <- container
            .getDexEntryNames
            .view
            .flatMap(container.getEntry(_).getClasses.toSeq)
            .flatMap(_.getMethods)

          instruction <- Option(method.getImplementation)
            .toSeq
            .flatMap(_.getInstructions)
            .flatMap(classTagOf[ReferenceInstruction].unapply)

          methodReference <- classTagOf[MethodReference].unapply(instruction.getReference)

          id = MethodIdentity(methodReference.getName, methodReference
            .getParameterTypes
            .view
            .map(ClassName.fromByteCodeClassName)
            .toList)
        }
          yield id -> (method -> instruction.getOpcode)

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
                      case _ -> (m -> op) =>
                        val cm = ClassMethod(
                          ClassName.fromByteCodeClassName(m.getDefiningClass),
                          m.getName,
                          ClassName.fromByteCodeClassNameK(m.getParameterTypes).toList
                        )
                        cm -> op
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
