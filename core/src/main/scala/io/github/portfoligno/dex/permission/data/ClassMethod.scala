package io.github.portfoligno.dex.permission.data

case class ClassMethod(
  declaringClass: ClassName,
  name: String,
  arguments: List[ClassName]
) {
  override
  def toString: String = {
    val s = declaringClass.toByteCodeClassName
    val n = s.length
    val prefix =
      if (n < 2) {
        new StringBuilder("<invalid class name>") ++= s
      } else {
        new StringBuilder(s.substring(1, n - 1))
      }

    MethodIdentity.mkString(prefix += '.' ++= name, arguments)
  }
}
