package io.github.portfoligno.dex.permission.data

case class MethodIdentity(
  name: String,
  arguments: List[ClassName]
) {
  override
  def toString: String =
    MethodIdentity.mkString(new StringBuilder(name), arguments)
}

object MethodIdentity {
  private[data]
  def mkString(b: StringBuilder, arguments: List[ClassName]): String =
    arguments
      .addString(b, "(", "", ")")
      .mkString
}
