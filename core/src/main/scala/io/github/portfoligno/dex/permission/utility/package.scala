package io.github.portfoligno.dex.permission

import scala.reflect.ClassTag

package object utility {
  type ->[A, B] = (A, B)
  type Table[A, B, C] = Map[A, Map[B, C]]

  object -> {
    def unapply[A, B](arg: A -> B): Option[A -> B] =
      Some(arg)
  }

  @inline
  private[permission]
  def flip[A, B, C](f: A => B => C)(x: B)(y: A): C =
    f(y)(x)

  @inline
  private[permission]
  def classTagOf[A](implicit A: ClassTag[A]): ClassTag[A] = A


  private[permission]
  object implicits {
    implicit class ToTableOps[A](private val elems: Traversable[A]) extends AnyVal {
      def toTable[T, U, V](implicit ev: A <:< (T -> U -> V)): Table[T, U, V] =
        elems
          .groupBy(_._1._1)
          .mapValues(_
            .view.map(t => t._1._2 -> t._2)
            .toMap)
    }
  }
}
