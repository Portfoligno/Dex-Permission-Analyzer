package io.github.portfoligno.dex.permission

package object utility {
  type ->[A, B] = (A, B)
  type Table[A, B, C] = Map[A, Map[B, C]]

  object -> {
    def unapply[A, B](arg: A -> B): Option[A -> B] =
      Some(arg)
  }

  object Table {
    def apply[A, B, C](elems: A -> B -> C*): Table[A, B, C] =
      elems
        .groupBy(_._1._1)
        .mapValues(_.view.map(t => t._1._2 -> t._2).toMap)
  }

  @inline
  private[permission]
  def flip[A, B, C](f: A => B => C)(x: B)(y: A): C =
    f(y)(x)
}
