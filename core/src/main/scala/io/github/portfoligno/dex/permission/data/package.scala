package io.github.portfoligno.dex.permission

import org.jf.dexlib2.analysis.reflection.util.ReflectionUtils

package object data {
  type ClassName <: ClassName.Base with ClassName.Tag

  object ClassName {
    private[data] type Base
    private[data] trait Tag extends Any

    def fromReflectionClassName(name: String): ClassName =
      if (name.isEmpty) {
        "".asInstanceOf[ClassName]
      } else {
        ReflectionUtils.javaToDexName(name).asInstanceOf[ClassName]
      }

    def fromByteCodeClassName(name: String): ClassName =
      name.asInstanceOf[ClassName]

    def fromByteCodeClassNameK[F[_]](names: F[String]): F[ClassName] =
      names.asInstanceOf[F[ClassName]]

    def fromByteCodeClassName(name: CharSequence): ClassName =
      name.toString.asInstanceOf[ClassName]

    private[data]
    object Tag {
      // `ReflectionUtils.dexToJavaName` does not work properly
      // See `ReflectionUtils.primitiveMap`
      private
      val primitiveMap = Map(
        "Z" -> "boolean",
        "I" -> "int",
        "J" -> "long",
        "D" -> "double",
        "V" -> "void",
        "F" -> "float",
        "C" -> "char",
        "S" -> "short",
        "B" -> "byte")

      implicit class ClassNameOps(private val name: ClassName) extends AnyVal {
        // Re-implement `ReflectionUtils.dexToJavaName` with adjustments
        def toReflectionClassName: String = {
          val dexName = name.asInstanceOf[String]

          if (dexName.isEmpty) {
            ""
          } else if (dexName.charAt(0) == '[') {
            dexName.replace('/', '.')
          } else {
            primitiveMap.getOrElse(dexName, dexName
              .substring(1, dexName.length - 1)
              .replace('/', '.'))
          }
        }

        def toByteCodeClassName: String =
          name.asInstanceOf[String]
      }
    }
  }
}
