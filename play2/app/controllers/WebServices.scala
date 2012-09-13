package controllers

import play.api._
import mvc._
import org.purang.blog.domain._
import java.util.UUID.{randomUUID => uuid}
import org.purang.blog.domain.{Created => c}
import org.purang.net.http.{GET, ApplicationJson, ContentType, POST}
import Utils._
import net.liftweb.json.NoTypeHints
import org.purang.blog.backend._
import akka.actor.Props
import org.purang.blog.domain.ChangeProtocol.{CommentAdded, BlogEntryStateChanged, NewBlogEntryCreated}
import scala.Some
import org.purang.blog.domain.User
import org.purang.blog.domain.Comment
import org.purang.blog.domain.BlogEntry

object WebServices extends Controller {

  import play.api.Play.current
  import org.purang.net.http.ning._

  val esUrl: Option[String] = Play.configuration.getString("es.url")
  val riakUrl = Play.configuration.getString("riak.url")


  private val database = new collection.mutable.HashMap[String, BlogEntry]()

  val withExamples: Option[Boolean] = Play.configuration.getBoolean("with.examples")
  if(withExamples.getOrElse(true)) {
    import  ZExamples._
    database(e01.uid) = e01
    database(e02.uid) = e02
    database(e03.uid) = e03
    database(e04.uid) = e04
    database(e05.uid) = e05
  }


  val riak = new infrastructure.Riak(riakUrl.get)
  val es = new infrastructure.ES(esUrl.get)
  val kafka = new infrastructure.Kafka()

  val mp = org.purang.blog.backend.system.actorOf(Props[BackendMultiplexer], "mp")
  val riakActor = org.purang.blog.backend.system.actorOf(Props(new StoreActor(riak)), "store")
  val esActor = org.purang.blog.backend.system.actorOf(Props(new IndexActor(es)), "index")
  val ebusActor = org.purang.blog.backend.system.actorOf(Props(new EventBusActor(kafka)), "ebus")

  val storeSuccesses = org.purang.blog.backend.system.actorOf(Props(new Successes("STORAGE")), "store-successes")
  val storeFailures = org.purang.blog.backend.system.actorOf(Props(new Failures(("STORAGE"))), "store-failures")

  val indexSuccesses = org.purang.blog.backend.system.actorOf(Props(new Successes("INDEX")), "index-successes")
  val indexFailures = org.purang.blog.backend.system.actorOf(Props(new Failures(("INDEX"))), "index-failures")


  //todo in the end the js client should go directly to the index
  def all() = Action {
    implicit val formats = net.liftweb.json.Serialization.formats(NoTypeHints)
    esUrl.fold(
      u => (GET > u + "_search?q=title.content:*&sort=created.time:desc&pretty" >> ContentType(ApplicationJson)) ~> {
        _.fold(
          t => Results.InternalServerError.apply(t._1.toString),
          _ match {
            case (200, _, Some(body), _) => {
              Results.Ok(net.liftweb.json.compact(net.liftweb.json.render(net.liftweb.json.Extraction.decompose {
                for (
                  map <- (net.liftweb.json.parse(body) \ "hits" \ "hits").values.asInstanceOf[List[Map[String, Any]]];
                  mvc <- map
                  if mvc._1 == "_source"
                ) yield mvc._2
              })))
            }
            case (200, _, None, _) => Results.InternalServerError.apply("200 without a body...")
            case (status, _, body, _) => Results.Status(status).apply(body.getOrElse("No entity body found"))
          }
        )
      },
      Ok(ListBlogEntryJsonSerializer(database.values.toList)).as("application/json")
    )
  }

  def blog(id: String) = Action {
    val fetch = riak.fetch(id)
    fetch.fold(
      ps => Results.InternalServerError(ps.toString)
      ,
      x => Ok(BlogEntryJsonSerializer(x)).as("application/json")
    )
    //Ok(BlogEntryJsonDeserializer.unapply(database(id))).as("application/json")
  }

  def createBlog = Action(parse.tolerantText) {
    request => {
      import org.purang.blog.domain.NascentBlogEntryJsonDeserializer
      val entry = convert(NascentBlogEntryJsonDeserializer(request.body))
      database(entry.uid) = entry

      org.purang.blog.backend.system.actorFor("akka://HaBlogSystem/user/mp") ! NewBlogEntryCreated(entry)
      Results.Accepted(entry.uid).withHeaders().withHeaders(LOCATION -> entry.uid)
    }
  }

  def addOrReplaceComment(id: String, user: String, ids: String) = Action(parse.tolerantText) {
    request => {
      val nc = Comment(uuid.toString, User(user), request.body, Option(c()), None, Nil)
      mp ! CommentAdded(id, nc, ids)
      Results.Accepted(id).withHeaders().withHeaders(LOCATION -> id)
    }
  }


  def changeState(uid: String, state: String) = Action {
    org.purang.blog.backend.system.actorFor("akka://HaBlogSystem/user/mp") ! BlogEntryStateChanged(uid, state match { //todo this is a method in itself as json serialization/de-serialization uses this too!
      case "Nascent" => Nascent
      case "Draft" => Draft
      case "Published" => Published
      case "Retired" => Retired
      case _ => throw new AssertionError("%s is not a valid state".format(state)) //todo use either instead
   })
    Results.Accepted(uid).withHeaders(LOCATION -> uid)
  }

  //https://gist.github.com/2328236
  def Secured[A](username: String, password: String)(action: Action[A]) = Action(action.parser) { request =>
    request.headers.get("Authorization").flatMap { authorization =>
      authorization.split(" ").drop(1).headOption.filter { encoded =>
        new String(org.apache.commons.codec.binary.Base64.decodeBase64(encoded.getBytes)).split(":").toList match {
          case u :: p :: Nil if u == username && password == p => true
          case _ => false
        }
      }.map(_ => action(request))
    }.getOrElse {
      Unauthorized.withHeaders("WWW-Authenticate" -> """Basic realm="Secured"""")
    }
  }
}
E