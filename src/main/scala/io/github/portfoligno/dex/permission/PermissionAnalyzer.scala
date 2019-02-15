package io.github.portfoligno.dex.permission

import java.io.File

import io.github.portfoligno.dex.permission.settings.MappingSource
import org.jf.dexlib2.DexFileFactory
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.MethodReference

import scala.collection.convert.ImplicitConversionsToScala._
import scala.reflect.ClassTag

object PermissionAnalyzer {
  private
  val ReferenceInstruction = implicitly[ClassTag[ReferenceInstruction]]
  private
  val MethodReference = implicitly[ClassTag[MethodReference]]

  def analyzeDexContainer[F[_]](file: File, mappingSource: MappingSource): Unit = {
    val container = DexFileFactory.loadDexContainer(file, null)

    container
      .getDexEntryNames
      .view
      .flatMap(container.getEntry(_).getClasses.toSeq)
      .flatMap(_.getMethods)
      .flatMap(m => Option(m.getImplementation))
      .flatMap(_.getInstructions)
      .flatMap(ReferenceInstruction.unapply)
      .flatMap(i => MethodReference.unapply(i.getReference))
  }
}
