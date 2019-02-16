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

    if (n < 2) {
      MethodIdentity.mkString(new StringBuilder("<invalid class name>") ++= name, arguments)
    } else {
      MethodIdentity.mkString(new StringBuilder(s.substring(1, n - 1)) += '.' ++= name, arguments)
    }
  }
}
