package io.github.portfoligno.dex.permission

import io.github.portfoligno.dex.permission.data.{ClassName, MethodIdentity}
import io.github.portfoligno.dex.permission.utility.->

import scala.collection.immutable.Traversable

package object settings {
  private[settings] val CONTENT_PROVIDER = "CONTENT_PROVIDER"

  private[settings] val ContentProvider = "android.content.ContentProvider"
  private[settings] val Uri = "android.net.Uri"
  private[settings] val String = "java.lang.String"
  private[settings] val CancellationSignal = "android.os.CancellationSignal"
  private[settings] val Bundle = "android.os.Bundle"

  type MappingParser = String => Traversable[MethodIdentity -> ClassName -> String]

  object MappingParser {
    val axplorer: MappingParser = ???
    val pScout: MappingParser = ???
  }
}
