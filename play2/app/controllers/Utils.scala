package controllers

/**
 * Created with IntelliJ IDEA.
 * User: ppurang
 * Date: 8/27/12
 * Time: 10:40 PM
 * To change this template use File | Settings | File Templates.
 */
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
