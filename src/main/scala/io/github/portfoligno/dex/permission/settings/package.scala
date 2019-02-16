package io.github.portfoligno.dex.permission

import java.lang.String.valueOf

import cats.syntax.option._
import com.google.common.base.Splitter
import io.github.portfoligno.dex.permission.data.{ClassName, MethodIdentity}
import io.github.portfoligno.dex.permission.utility.->

import scala.collection.JavaConverters._
import scala.collection.TraversableOnce
import scala.collection.convert.ImplicitConversionsToScala._

package object settings {
  private[settings] val CONTENT_PROVIDER = "CONTENT_PROVIDER"

  private[settings] val ContentProvider = "android.content.ContentProvider"
  private[settings] val Uri = "android.net.Uri"
  private[settings] val String = "java.lang.String"
  private[settings] val CancellationSignal = "android.os.CancellationSignal"
  private[settings] val Bundle = "android.os.Bundle"


  type MappingParser = String => TraversableOnce[Either[Throwable, MethodIdentity -> ClassName -> String]]

  object MappingParser {
    private
    val doubleColons = Splitter.on("::").limit(2).trimResults()
    private
    val commas = Splitter.on(',')
    private
    val spaces = Splitter.on(' ').trimResults().omitEmptyStrings().limit(3)

    private
    val `Permission:` = "Permission:"

    private
    def parseArgumentTypes(s: String) =
      commas.split(s).view.map(ClassName.fromReflectionClassName).toList

    val axplorer: MappingParser =
      _
        .linesIterator
        .map(doubleColons.splitToList(_).asScala match {
          case Seq(m, permission) =>
            val r = m.lastIndexOf(')')
            val l = m.lastIndexOf('(', r - 1)
            val d = m.lastIndexOf('.', l - 1)
            val id = MethodIdentity(
              m.substring(1 + d, l),
              parseArgumentTypes(m.substring(1 + l, r)))

            Right(id -> ClassName.fromReflectionClassName(m.substring(0, d)) -> permission)

          case x @ _ =>
            Left(new IllegalArgumentException(valueOf(x)))
        })

    val pScout: MappingParser =
      _
        .linesIterator
        .scanLeft(
          none[String -> Option[String]]
        )((previous, line) =>
          if (line.startsWith(`Permission:`)) {
            Some(line.substring(`Permission:`.length) -> None)
          } else if (line.charAt(0) == '<') {
            val end = line.lastIndexOf('>')
            previous.map(_.copy(_2 = Some(line.substring(1, end))))
          } else {
            previous
          }
        )
        .flatMap {
          case Some(permission -> Some(method)) =>
            Some(spaces.splitToList(method).asScala match {
              case Seq(classWithColon, _, m) =>
                val r = m.lastIndexOf(')')
                val l = m.lastIndexOf('(', r - 1)
                val id = MethodIdentity(
                  m.substring(0, l),
                  parseArgumentTypes(m.substring(1 + l, r)))

                Right(id -> ClassName.fromReflectionClassName(classWithColon.dropRight(1)) -> permission)

              case x @ _ =>
                Left(new IllegalArgumentException(valueOf(x)))
            })

          case _ =>
            None
        }
  }
}
