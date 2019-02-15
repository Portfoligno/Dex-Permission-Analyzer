package io.github.portfoligno.dex.permission

import java.io.File

import io.github.portfoligno.dex.permission.settings.MappingSource
import org.jf.dexlib2.DexFileFactory
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.MethodReference

import scala.collection.convert.ImplicitConversionsToScala._

object PermissionAnalyzer {
  def analyzeDexContainer[F[_]](file: File, mappingSource: MappingSource): Unit = {
    val container = DexFileFactory.loadDexContainer(file, null)

    container
      .getDexEntryNames
      .view
      .flatMap(container.getEntry(_).getClasses.toSeq)
      .flatMap(_.getMethods)
      .flatMap(m => Option(m.getImplementation))
      .flatMap(_.getInstructions)
      .flatMap(classTagOf[ReferenceInstruction].unapply)
      .flatMap(i => classTagOf[MethodReference].unapply(i.getReference))
  }
}
