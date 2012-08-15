package org.purang.blog.domain

import java.util

object `package` {

  trait Unique[+A] {
    def uid: String
  }

  type UniqueId = String
  type HintedUniqueIdGenerator = String => UniqueId

  object HintedUUIDUniqueIdGenerator extends HintedUniqueIdGenerator {
    def apply(hint: String) = encode(hint).take(30) + "_" + util.UUID.randomUUID()
  }

  private def encode = replaceWith(Map(" " -> "-", "*" -> "", "!" -> "", "&" -> "_and_"))

  private def replaceWith: Map[String, String] => String => String = m => s => s.map(c => m.getOrElse(c.toString, c)).mkString("")

  lazy val InitialLike = Rating(1, 0)
  lazy val InitialDisLike = Rating(0, 1)

  def convert(nbe: NascentBlogEntry) : BlogEntry = BlogEntry(uid = HintedUUIDUniqueIdGenerator(nbe.headline.content), created = Option(Created()), title = nbe.title, headline = nbe.headline, summary = nbe.summary, content = nbe.content, tags = nbe.tags)

  trait NoStackTrace extends Throwable {
    override def fillInStackTrace(): Throwable = this
  }
  case class TooManyCommentsFound(m: String) extends AssertionError with NoStackTrace  {
    override def toString = "TooManyCommentsFound(%s)".format(m)
  }
  case class NoCommentsFound(m: String) extends AssertionError with NoStackTrace {
    override def toString = "NoCommentsFound(%s)".format(m)
  }

  def tail(parts: List[Comment]): List[Comment] = {
    (if (parts.isEmpty) Nil else parts.tail)
  }

  final def addComment(t: Comment, c: Comment, ids: String): Either[Either[TooManyCommentsFound, NoCommentsFound], Comment] = {
    ids.split(",").map(_.trim).filter(_ != "").toList match {
      case x :: Nil => t.replies.filter(_.uid == x) match {
        case y :: Nil => {
          val parts = t.replies span (_.uid != y.uid)
          Right(t.copy(replies = parts._1 ++ (addComment(y, c, "").right.get :: tail(parts._2))))
        }
        case y :: ys => Left(Left(TooManyCommentsFound("%s in %s".format(ids, t))))
        case _ => Left(Right(NoCommentsFound("%s in %s".format(ids, t))))
      }
      case x :: xs => {
        val parts = t.replies span (_.uid != x)

        Right(t.copy(replies = parts._1 ++ (addComment(parts._2.head, c, xs.mkString(",")).right.get :: tail(parts._2))))

      }
      case _ => Right(t.copy(replies = t.replies :+ c))
    }
  }

  final def addCommentToList(t: List[Comment], c: Comment, ids: String): Either[Either[TooManyCommentsFound, NoCommentsFound], List[Comment]] = {
    ids.split(",").map(_.trim).filter(_ != "").toList match {
      case x :: Nil => t.filter(_.uid == x) match {
        case y :: Nil => {
          val parts = y.replies span (_.uid != y.uid)
          Right(parts._1 ++ (addComment(y, c, "").right.get :: tail(parts._2)))
        }
        case y :: ys => Left(Left(TooManyCommentsFound("%s in %s".format(ids, t))))
        case _ => Left(Right(NoCommentsFound("%s in %s".format(ids, t))))
      }
      case x :: xs => t.filter(_.uid == x) match {
        case y :: Nil => {
          val parts = y.replies span (_.uid != y.uid)
          Right(parts._1 ++ (addComment(y, c, xs.mkString(",")).right.get :: tail(parts._2)))
        }
        case y :: ys => Left(Left(TooManyCommentsFound("%s in %s".format(ids, t))))
        case _ => Left(Right(NoCommentsFound("%s in %s".format(ids, t))))
      }
      case _ => Right(t :+ c)
    }
  }

}

sealed trait BlogEntryState {
  val next: Option[BlogEntryState]
  val prev: Option[BlogEntryState]
}

case object Nascent extends BlogEntryState {
  val next = Some(Draft)
  val prev = None
}

case object Draft extends BlogEntryState {
  val next = Some(Published)
  val prev = Some(Nascent)
}

case object Published extends BlogEntryState {
  val next = Some(Retired)
  val prev = Some(Draft)
}

case object Retired extends BlogEntryState {
  val next = None
  val prev = Some(Published)
}

case class Rating(likes: Int, dislikes: Int) {
  def like() = Rating(this.likes + 1, this.dislikes)

  def dislike() = Rating(this.likes, this.dislikes + 1)
}

case class User(twitterÍd: String)

case class NascentComment(someUser: User, comment: String)  {
  def toComment() : Comment = Comment(user = someUser, text = comment, created = Some(Created()), rating = None, replies
    = Nil)
}


case class Comment(uid: String = util.UUID.randomUUID().toString, user: User, text: String, created: Option[Created], rating: Option[Rating], replies: List[Comment]) extends Unique[Comment]


case class NascentBlogEntry(title: Option[Headline] = None,
                            headline: Headline,
                            summary: Option[Text] = None,
                            content: List[Section] = List(),
                            tags: List[Tag] = List())


case class BlogEntry(uid: String,
                     state: BlogEntryState = Nascent,
                     created: Option[Created],
                     modified: Option[Modified] = None,
                     title: Option[Headline] = None,
                     headline: Headline,
                     summary: Option[Text] = None,
                     content: List[Section] = List(),
                     tags: List[Tag] = List(),
                     rating: Option[Rating] = None,
                     comments: List[Comment] = List()
                      )
  extends Unique[BlogEntry]

case class Created(time: Long = System.currentTimeMillis())

case class Modified(time: Long = System.currentTimeMillis())

case class Headline(content: String)

object HeadlineWrapper extends Function1[String, Headline] {
  implicit def apply(str: String): Headline = Headline(str)

  implicit def unapply(headline: Headline): String = headline.content
}

case class Section(headline: Option[Headline] = None, text: Text)

case class Text(content: String)

object TextWrapper extends Function1[String, Text] {
  implicit def apply(str: String): Text = Text(str)

  implicit def unapply(paragraph: Text): String = paragraph.content
}

case class Tag(tag: String)

object TagWrapper extends Function1[String, Tag] {
  implicit def apply(str: String): Tag = Tag(tag = str)

  implicit def unapply(tag: Tag): String = tag.tag
}

//The following is under design

trait Versioned[A, B] {
  val condition: A
  val value: B

  def newer(value: B): Versioned[A, B]
}

trait History[A] {
  val history: List[A]
}

trait HistoryLinks[+A] {
  val previous: Some[Unique[A]]
  val next: Some[Unique[A]]
}


case class ETag(value: String)

case class ETaggedBlogEntry(condition: ETag, value: BlogEntry) extends Versioned[ETag, BlogEntry] {
  def newer(newer: BlogEntry) = ETaggedBlogEntry(condition, newer)
}