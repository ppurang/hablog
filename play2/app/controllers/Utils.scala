package controllers

object Utils {

  trait Fold[A] {
    def fold[B](f: A => B,  g: => B): B
  }

  case class OptionFold[A](o: Option[A]) extends Fold[A] {
    def fold[B](f: A => B, g: => B) : B = o match {
      case Some(a) => f(a)
      case _ => g
    }
  }

  implicit def optionToOptionFold(o: Option[_]): Fold[_] = OptionFold(o)
}
