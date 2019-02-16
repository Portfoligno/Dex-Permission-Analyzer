package io.github.portfoligno.dex.permission

import java.io.File

import cats.Parallel
import cats.effect.ConcurrentEffect
import cats.syntax.functor._
import io.github.portfoligno.dex.permission.data.{ClassName, MethodIdentity}
import io.github.portfoligno.dex.permission.settings.MappingSource
import org.jf.dexlib2.DexFileFactory
import org.jf.dexlib2.dexbacked.DexBackedMethod
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
  ): Unit = {
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

      val o: Seq[(MethodIdentity, Seq[DexBackedMethod])] = methods
        .groupBy(_._1)
        .view
        .map(t => t._1 -> t._2.map(_._2).toSeq)
        .toSeq
    }
  }
}
