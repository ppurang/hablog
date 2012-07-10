package org.purang.blog.domain

import java.util.UUID
import annotation.switch
import java.net.URLEncoder

object `package` {
  type CreateUnique[T] = UUID => Unique[T]
  val CreateUnique = Unique

  implicit def string2uuid[B](uuidString: String): UUID = UUID.fromString(uuidString)
  implicit def uuid2string[B](uuid: UUID): String = uuid.toString

}

trait Unique[+A] {
  def uuid: UUID
}

object Unique {
  private case class AnyUnique[A](uuid: UUID) extends Unique[A]

  def apply[B](uuid: UUID): Unique[B] = AnyUnique[B](uuid)
  def apply[B](uuidString: String): Unique[B] = AnyUnique[B](uuidString)

  def unapply[B](unique: Unique[B]) = unique.uuid
}


sealed trait BlogState {
  val next: Option[BlogState]
  val prev: Option[BlogState]
}

case object Nascent extends BlogState {
  val next = Some(Draft)
  val prev = None
}

case object Draft extends BlogState {
  val next = Some(Published)
  val prev = Some(Nascent)
}

case object Published extends BlogState {
  val next = Some(Retired)
  val prev = Some(Draft)
}

case object Retired extends BlogState {
  val next = None
  val prev = Some(Published)
}

case class Rating(likes: Int, dislikes: Int) {
  def like() = Rating(this.likes + 1, this.dislikes)
  def dislike() = Rating(this.likes, this.dislikes + 1)
}

object InitialLike extends Rating(1,0)
object InitialDisLike extends Rating(0,1)

case class User(twitterÍd: String)
case class Comment(user: User, text: String, created: Created, replies: List[Comment])

case class BlogEntry(uuid: UUID = UUID.randomUUID(),
                     state: BlogState = Nascent,
                     created: Created,
                     modified: Option[Modified] = None,
                     content: Content,
                     tags: List[Tag] = List(),
                     rating: Option[Rating] = None,
                     comments: List[Comment] = List()
                     )
      extends Unique[BlogEntry]

case class Created(time: Long = System.currentTimeMillis())

case class Modified(time: Long = System.currentTimeMillis())

case class Content(headline: Headline, sections: List[Section] = List())

case class Headline(headline: String)

object HeadlineWrapper extends Function1[String, Headline] {
  implicit def apply(str: String): Headline = Headline(str)

  implicit def unapply(headline: Headline): String = headline.headline
}

case class Section(headline: Option[Headline] = None, paragraphs: List[Paragraph] = List())

case class Paragraph(content: String)

object ParagraphWrapper extends Function1[String, Paragraph] {
  implicit def apply(str: String): Paragraph = Paragraph(str)

  implicit def unapply(paragraph: Paragraph): String = paragraph.content
}

case class Tag(tag: String)

object TagWrapper extends Function1[String, Tag] {
  implicit def apply(str: String): Tag = Tag(tag=str)

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