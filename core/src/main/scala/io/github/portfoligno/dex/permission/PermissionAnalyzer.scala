package io.github.portfoligno.dex.permission

import java.io.File

import cats.effect.ConcurrentEffect
import cats.syntax.functor._
import io.github.portfoligno.dex.permission.data.{ClassMethod, ClassName, MethodIdentity}
import io.github.portfoligno.dex.permission.settings.MappingSource
import org.jf.dexlib2.DexFileFactory
import org.jf.dexlib2.Opcode._
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.MethodReference

import scala.collection.convert.ImplicitConversionsToScala._

object PermissionAnalyzer {
  import utility._
  import utility.implicits._

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
          op = instruction.getOpcode

          methodReference <- classTagOf[MethodReference].unapply(instruction.getReference)
          callee = ClassName.fromByteCodeClassName(methodReference.getDefiningClass)
          c = op match {
            case INVOKE_VIRTUAL | INVOKE_VIRTUAL_RANGE | INVOKE_VIRTUAL_QUICK | INVOKE_VIRTUAL_QUICK_RANGE
                 | INVOKE_INTERFACE | INVOKE_INTERFACE_RANGE
                 | INVOKE_POLYMORPHIC | INVOKE_POLYMORPHIC_RANGE
                 | INVOKE_CUSTOM | INVOKE_CUSTOM_RANGE =>
              None
            case INVOKE_SUPER | INVOKE_SUPER_RANGE | INVOKE_SUPER_QUICK | INVOKE_SUPER_QUICK_RANGE
                 | INVOKE_DIRECT | INVOKE_DIRECT_RANGE
                 | INVOKE_STATIC | INVOKE_STATIC_RANGE =>
              Some(callee)
            case r @ _ =>
              throw new IllegalArgumentException(r.name)
          }
          id = MethodIdentity(methodReference.getName, methodReference
            .getParameterTypes
            .view
            .map(ClassName.fromByteCodeClassName)
            .toList)
        }
          yield id -> c -> (op -> callee -> method)

        callers
          .groupBy(_._1)
          .view
          .flatMap {
            case id -> Some(callee) -> methods =>
              permissionLookup
                .get(id, callee)
                .map(id -> methods -> _
                  .view
                  .map(_ -> Set(callee))
                  .toMap)

            case id -> None -> methods =>
              permissionLookup
                .get(id)
                .map(id -> methods -> _
                  .view
                  .flatMap {
                    case y -> xs =>
                      xs.map(_ -> y)
                  }
                  .groupBy(_._1)
                  .mapValues(_.map(_._2).toSet))
          }
          .map {
            case id -> methods -> permissions =>
              AnalysisResult(
                id,
                methods
                  .map {
                    case _ -> (invocation -> m) =>
                      invocation -> ClassMethod(
                        ClassName.fromByteCodeClassName(m.getDefiningClass),
                        m.getName,
                        ClassName.fromByteCodeClassNameK(m.getParameterTypes).toList
                      )
                  }
                  .toSet,
                permissions
              )
          }
          .toList
      }
}
