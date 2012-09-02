package org.purang.blog.domain

package object status {
  type or[+A, +B] = Either[A, B]

  type StatusCode = Int

  class Status(code: StatusCode, message: Option[String])

  case class Ok(message: Option[String]) extends Status(200, message)

  case class Created(link: String, message: Option[String]) extends Status(201, message)

  case class Accepted(message: Option[String]) extends Status(200, message)

  case class BadRequest(message: Option[String]) extends Status(400, message)

  case class NotFound(message: Option[String]) extends Status(404, message)

  case class Error(message: Option[String]) extends Status(500, message)

  case class Retry(message: Option[String], time: Long) extends Status(503, message)

  type ProblemStatuses = BadRequest or NotFound or Retry or Error
  type CommonStatuses = Ok or Created or Accepted or ProblemStatuses

  def asProblemStatuses: Status => ProblemStatuses = s => s match {
    case b: BadRequest => Left(Left(Left(b)))
    case n: NotFound => Left(Left(Right(n)))
    case r: Retry => Left(Right(r))
    case e: Error => Right(e)
  }


/*  def asCommonStatuses : Status => CommonStatuses = s => s match {
    case o:Ok => Left(Left(Left(Left(Left(Left(o)))))
    case c: Created => Left(Left(Left(Left(Left(Right(c)))))
    case a: Accepted => Left(Left(Left(Left(Right(a)))))
    case b: BadRequest =>  Left(Left(Left(Right(b))))//Left(Left(Right(b)))
    case n: NotFound =>  Left(Left(Right(n)))
    case r: Retry => Left(Right(r))
    case e: Error => Right(e)
  }*/

}

object ChangeProtocol {
  import status._
  type BlogEntryId = UniqueId
  type CommentEntryId = UniqueId

  //todo make the following asynch (need lift-json for scala 2.10.0 )

  sealed trait Event



  case class IndexEvent(e: BlogEntry) extends Event
  case class IndexEventFailed(e: BlogEntry, ps: ProblemStatuses) extends Event
  case class ReIndexEvent(e: BlogEntry) extends Event
  case class ReIndexEventFailed(e: BlogEntry, ps: ProblemStatuses) extends Event



  //new blog entry
  case class NewBlogEntryCreated(be: BlogEntry) extends Event
  case class NewBlogEntryPersistenceFailed(be: BlogEntry, ps: ProblemStatuses) extends Event
  case class NewBlogEntryIndexingFailed(be: BlogEntry) extends Event

  //state changes
  case class BlogEntryStateChanged(uid: BlogEntryId, state: BlogEntryState) extends Event
  case class BlogEntryStateChangedPersistenceFailed(uid: BlogEntryId, state: BlogEntryState, ps: ProblemStatuses) extends Event

  //comments
  //type AddComment = BlogEntryId => Seq[CommentEntryId] => Comment => CommonStatuses
  case class CommentAdded(uid: BlogEntryId, comment: Comment, path: String)  extends Event
  case class CommentAddedFailed(uid: BlogEntryId, comment: Comment, path: String, ps: ProblemStatuses)  extends Event

  type RemoveComment = BlogEntryId => Seq[CommentEntryId] => CommonStatuses
  case class CommentRemoved(uid: BlogEntryId, path: Seq[CommentEntryId])  extends Event

  //tags
  type AddTag = BlogEntryId => Tag => CommonStatuses
  type RemoveTag = BlogEntryId => Tag => CommonStatuses

  //content
  type ReplaceContent = BlogEntryId => List[Section] => CommonStatuses
  type ReplaceHeadline = BlogEntryId => Headline => CommonStatuses
  type ReplaceTitle = BlogEntryId => Headline => CommonStatuses
  type ReplaceSummary = BlogEntryId => Text => CommonStatuses

  //rating
  type AddRating = BlogEntryId => Rating => CommonStatuses
  type AddRatingComment = BlogEntryId => Seq[CommentEntryId]  => Rating => CommonStatuses

  //views in progress
  //type BlogView = View => BlogEntryId => CommonStatuses
  //type CommentView = View => CommentEntryId => CommonStatuses
}
