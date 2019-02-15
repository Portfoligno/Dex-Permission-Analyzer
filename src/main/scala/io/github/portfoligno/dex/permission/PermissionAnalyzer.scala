package io.github.portfoligno.dex.permission

import java.io.File

import io.github.portfoligno.dex.permission.settings.MappingSource
import org.jf.dexlib2.DexFileFactory

import scala.collection.convert.ImplicitConversionsToScala._

object PermissionAnalyzer {
  def analyzeDexContainer[F[_]](file: File, mappingSource: MappingSource): Unit = {
    val container = DexFileFactory.loadDexContainer(file, null)

    container
      .getDexEntryNames
      .map(container.getEntry)
  }
}
