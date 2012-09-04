package org.purang.blog.domain

import net.liftweb.json._

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

  implicit val formats = org.purang.blog.domain.Serialization.formats  + ProblemStatusesSerializer

  object ProblemStatusesSerializer extends Serializer[ProblemStatuses] {
    private val ProblemStatusesClass = classOf[ProblemStatuses]

    def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), ProblemStatuses] = {
      case (TypeInfo(ProblemStatusesClass, _), json) => json match {
        case JObject(JField("BadRequest", JObject(JField("message", JString("==None==")) :: Nil)) :: Nil) => asProblemStatuses(BadRequest(None))
        case JObject(JField("BadRequest", JObject(List(JField("message", JString(s))))) :: Nil) => asProblemStatuses(BadRequest(Option(s)))

        case JObject(JField("NotFound", JObject(JField("message", JString("==None==")) :: Nil)) :: Nil) => asProblemStatuses(NotFound(None))
        case JObject(JField("NotFound", JObject(JField("message", JString(s)) :: Nil)) :: Nil) => asProblemStatuses(NotFound(Option(s)))

        case JObject(JField("Retry", JObject(JField("message", JString("==None==")) :: JField("time", JInt(t)) :: Nil)) :: Nil) => asProblemStatuses(Retry(None, t.longValue()))
        case JObject(JField("Retry", JObject(JField("message", JString(s)) :: JField("time", JInt(t)) :: Nil))  :: Nil)=> asProblemStatuses(Retry(Option(s), t.longValue()))

        case JObject(JField("Error", JObject(JField("message", JString("==None==")) :: Nil))  :: Nil) => asProblemStatuses(BadRequest(None))
        case JObject(JField("Error", JObject(JField("message", JString(s)) :: Nil)) :: Nil) => asProblemStatuses(BadRequest(Option(s)))

        case x => throw new MappingException("Can't convert " + x + " to ProblemStatuses")
      }
    }

    def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
      case x: ProblemStatuses => x match {
        case Left(Left(Left(b))) => JObject(JField("BadRequest", JObject(JField("message", JString(b.message.getOrElse("==None=="))) :: Nil)) :: Nil) //todo ugly
        case Left(Left(Right(n))) =>  JObject(JField("NotFound", JObject(JField("message", JString(n.message.getOrElse("==None=="))) :: Nil)) :: Nil)
        case Left(Right(r)) => JObject(JField("Retry", JObject(JField("message", JString(r.message.getOrElse("==None=="))) :: JField("time", JInt(r.time)) :: Nil)) :: Nil)
        case Right(e) => JObject(JField("Error", JObject(JField("message", JString(e.message.getOrElse("==None=="))) :: Nil)) :: Nil)
      }
    }
  }

  // + FullTypeHints(classOf[Either[_, _]] :: Nil)

  type BlogEntryId = UniqueId
  type CommentEntryId = UniqueId

  //todo make the following asynch (need lift-json for scala 2.10.0 )

  sealed trait Event {
    def stringIdentifier = this.getClass.getName

    def toJson() = net.liftweb.json.Serialization.write(this)

    def fromJson(str: String) = net.liftweb.json.parse(str).extract[this.type]
  }

  sealed trait FailedEvent extends Event {
    def ps: ProblemStatuses
  }


  case class IndexEvent(e: BlogEntry) extends Event {
  }

  case class IndexEventFailed(e: BlogEntry, ps: ProblemStatuses) extends FailedEvent

  case class ReIndexEvent(e: BlogEntry) extends Event

  case class ReIndexEventFailed(e: BlogEntry, ps: ProblemStatuses) extends FailedEvent


  //new blog entry
  case class NewBlogEntryCreated(be: BlogEntry) extends Event

  case class NewBlogEntryPersistenceFailed(be: BlogEntry, ps: ProblemStatuses) extends FailedEvent

  case class NewBlogEntryIndexingFailed(be: BlogEntry) extends Event

  //state changes
  case class BlogEntryStateChanged(uid: BlogEntryId, state: BlogEntryState) extends Event

  case class BlogEntryStateChangedPersistenceFailed(uid: BlogEntryId, state: BlogEntryState, ps: ProblemStatuses) extends FailedEvent

  //comments
  //type AddComment = BlogEntryId => Seq[CommentEntryId] => Comment => CommonStatuses
  case class CommentAdded(uid: BlogEntryId, comment: Comment, path: String) extends Event

  case class CommentAddedFailed(uid: BlogEntryId, comment: Comment, path: String, ps: ProblemStatuses) extends FailedEvent

  type RemoveComment = BlogEntryId => Seq[CommentEntryId] => CommonStatuses

  case class CommentRemoved(uid: BlogEntryId, path: Seq[CommentEntryId]) extends Event

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
  type AddRatingComment = BlogEntryId => Seq[CommentEntryId] => Rating => CommonStatuses

  //views in progress
  //type BlogView = View => BlogEntryId => CommonStatuses
  //type CommentView = View => CommentEntryId => CommonStatuses
}
