package controllers

import play.api._
import libs.ws.WS
import mvc._
import org.purang.blog.domain._
import java.util.UUID.{randomUUID => uuid}
import org.purang.blog.domain.{Created => c}
import org.purang.blog.domain.addCommentToList
import org.purang.net.http.{GET, ApplicationJson, ContentType, POST}
import scala.Left
import org.purang.blog.domain.Headline
import scala.Some
import scala.Right
import org.purang.blog.domain.Created
import org.purang.blog.domain.Section
import org.purang.blog.domain.User
import org.purang.blog.domain.Comment
import org.purang.blog.domain.Text
import org.purang.blog.domain.Tag
import org.purang.blog.domain.BlogEntry
import Utils._
import net.liftweb.json.NoTypeHints
import org.purang.blog.backend._
import akka.actor.Props
import org.purang.blog.domain.ChangeProtocol.{CommentAdded, BlogEntryStateChanged, NewBlogEntryCreated}
import scala.Left
import scala.Some
import org.purang.blog.domain.Created
import org.purang.blog.domain.Section
import org.purang.blog.domain.User
import org.purang.blog.domain.Comment
import org.purang.blog.domain.Text
import org.purang.blog.domain.BlogEntry
import org.purang.blog.domain.Headline
import scala.Right
import org.purang.blog.domain.NascentBlogEntry
import org.purang.blog.domain.Tag
import net.liftweb.json.JsonAST.JObject

object Application extends Controller {
  import org.purang.net.http.ning._

  import play.api.Play.current

  private val database = new collection.mutable.HashMap[String, BlogEntry]()

  val withExamples: Option[Boolean] = Play.configuration.getBoolean("with.examples")
  if(withExamples.getOrElse(true)) {
    import  Examples._
    database(e01.uid) = e01
    database(e02.uid) = e02
    database(e03.uid) = e03
    database(e04.uid) = e04
    database(e05.uid) = e05
  }

  val esUrl: Option[String] = Play.configuration.getString("es.url")
  val adminSecret = Play.configuration.getString("admin.secret")
  val riakUrl = Play.configuration.getString("riak.url")

  val riak = new Riak(riakUrl.get)
  val es = new ES(esUrl.get)

  val mp = org.purang.blog.backend.system.actorOf(Props[BackendMultiplexer], "mp")
  val riakActor = org.purang.blog.backend.system.actorOf(Props(new StoreActor(riak)), "store")
  val esActor = org.purang.blog.backend.system.actorOf(Props(new IndexActor(es)), "index")

  val storeSuccesses = org.purang.blog.backend.system.actorOf(Props(new Successes("STORAGE")), "store-successes")
  val storeFailures = org.purang.blog.backend.system.actorOf(Props(new Failures(("STORAGE"))), "store-failures")

  val indexSuccesses = org.purang.blog.backend.system.actorOf(Props(new Successes("INDEX")), "index-successes")
  val indexFailures = org.purang.blog.backend.system.actorOf(Props(new Failures(("INDEX"))), "index-failures")


  import play.api.data._
  import play.api.data.Forms._
  import play.api.data.validation.Constraints._

  case class SubmittedBE(title: Option[String], headline: String, summary: Option[String], text: String) {
    def asNBE = NascentBlogEntry(title.map(Headline(_)), Headline(headline), summary.map(Text(_)), List(Section(None, Text(text))))
  }

  val editForm : Form[SubmittedBE]= Form(
    mapping(
      "title" -> optional(text),
      "headline" -> nonEmptyText,
      "summary" -> optional(text),
      "section" -> nonEmptyText
    )(SubmittedBE.apply)(SubmittedBE.unapply)
  )

  def editView() = Secured("admin", adminSecret.getOrElse("my very secure password")) {
    Action {
      Ok(views.html.edit(editForm))
    }
  }

  def handleEdit() = Secured("admin", adminSecret.getOrElse("my very secure password")) {
    Action {
      implicit request =>
        Async {
          editForm.bindFromRequest.fold(
          errors => play.api.libs.concurrent.Akka.future(BadRequest), {
            case (e: SubmittedBE) =>
              val entry: NascentBlogEntry = e.asNBE
              import org.purang.blog.domain.NascentBlogEntryJsonSerializer
              WS.url("http://localhost:9000/blog").post(NascentBlogEntryJsonSerializer(entry)).map(
                response => response.status match {
                  case 201 => Redirect(routes.Application.index())/*Ok(response.body).withHeaders(
                      ("Location", response.header("Location").getOrElse("<NO LOCATION HEADER FOUND?>"))
                    )*/
                  case x => Results.Status(x).apply(response.body)
                }
              )
          }
          )
        }
    }
  }


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

  def index = Action{
    Ok(views.html.index())
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
      //Logger.info(request.body)
      val entry = convert(NascentBlogEntryJsonDeserializer(request.body))
      database(entry.uid) = entry

      //mp ! NewBlogEntryCreated(entry)

      //org.purang.blog.backend.system.logConfiguration()

      org.purang.blog.backend.system.actorFor("akka://HaBlogSystem/user/mp") ! NewBlogEntryCreated(entry)
      Results.Accepted(entry.uid).withHeaders().withHeaders(LOCATION -> entry.uid)
/*
      val ixResult = esUrl.map {
        u => (POST > u + entry.uid >> ContentType(ApplicationJson) >>> BlogEntryJsonSerializer(entry)) ~> {
          _.fold(
           t => Results.InternalServerError.apply(t._1.toString),
          _  match {
            case (200, _, _, _) => Results.Ok
            case (status, _, body, _) => Results.Status(status).apply(body.getOrElse("No entity body found"))
          }
          )
        }
      }

      ixResult.getOrElse(Results.Ok) match {
        case Results.Ok => Results.Created(entry.uid).withHeaders(LOCATION -> entry.uid)
        case y => y
      }
*/
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

object Examples {
  val e01 = BlogEntry(
      HintedUUIDUniqueIdGenerator("First *blog post* ever"),
      Nascent,
      Option(Created(1343224588265l)),
      None,
      Some(Headline("Finally, First *blog post* ever")),
      Headline("First *blog post* ever"),
      Some(Text("This journey took a long time. It almost never started and then it had many close calls. This post narrates this story.")),
      List(
        Section(
          None,
          Text( """It would have been so easy -- just pick a blogging platform and  voila one has a blog with a lot of functionality. In case you really want to own it just roll out one of the many blogging suites on your server. But **none** can boast of the pain and joy creating your own software  brings with it. The creativity, the decision making, the struggles to implement the vision at the micro and macro level,  and other nitty gritties spice up the adventure.""")
        ),
        Section(
          Some(Headline("The need")),
          Text( """<p class="bla">[Twitter](http://twitter.com) is too terse;  most journals have more ads then real content;  a private diary isn't social enough.</p><p>A good blog is about content, content and content.  A great or unique writing style makes the difference when content is not a problem.  All other things equal a great design and technological schnickschnack give the blog that aura of invincibility.</p>""")
        )
      ),
      List(Tag("blog"), Tag("writing")),
      Some(InitialLike),
      List(
        Comment(uuid.toString, User("@agreeable"), "totally agree", Option(Created()), Option(InitialLike), Nil),
        Comment(uuid.toString,User("@disagreeable"), "totally disagree", Option(Created())  , Option(InitialDisLike), Nil)
      )
    )
  val e02 = BlogEntry(
      HintedUUIDUniqueIdGenerator("Second *blog post* ever"),
      Nascent,
      Option(Created(1343224588265l)),
      None,
      Some(Headline("Finally, Second *blog post* ever")),
      Headline("Second *blog post* ever"),
      Some(Text("This journey took a long time. It almost never started and then it had many close calls. This post narrates this story.")),
      List(
        Section(
          None,
          Text( """It would have been so easy -- just pick a blogging platform and  voila one has a blog with a lot of functionality. In case you really want to own it just roll out one of the many blogging suites on your server. But **none** can boast of the pain and joy creating your own software  brings with it. The creativity, the decision making, the struggles to implement the vision at the micro and macro level,  and other nitty gritties spice up the adventure.""")
        ),
        Section(
          Some(Headline("The need")),
          Text( """<p class="bla">[Twitter](http://twitter.com) is too terse;  most journals have more ads then real content;  a private diary isn't social enough.</p><p>A good blog is about content, content and content.  A great or unique writing style makes the difference when content is not a problem.  All other things equal a great design and technological schnickschnack give the blog that aura of invincibility.</p>""")
        )
      ),
      List(Tag("blog"), Tag("writing")),
      Some(InitialLike),
      List(
        Comment(uuid.toString,User("@agreeable"), "totally agree", Option(Created()), Option(InitialLike), Nil),
        Comment(uuid.toString,User("@disagreeable"), "totally disagree", Option(Created())  , Option(InitialDisLike), Nil)
      )
    )
  val e03 = BlogEntry(
      HintedUUIDUniqueIdGenerator("Third *blog post* ever"),
      Nascent,
      Option(Created(1343224588265l)),
      None,
      Some(Headline("Finally, Third *blog post* ever")),
      Headline("Third *blog post* ever"),
      Some(Text("This journey took a long time. It almost never started and then it had many close calls. This post narrates this story.")),
      List(
        Section(
          None,
          Text( """It would have been so easy -- just pick a blogging platform and  voila one has a blog with a lot of functionality. In case you really want to own it just roll out one of the many blogging suites on your server. But **none** can boast of the pain and joy creating your own software  brings with it. The creativity, the decision making, the struggles to implement the vision at the micro and macro level,  and other nitty gritties spice up the adventure.""")
        ),
        Section(
          Some(Headline("The need")),
          Text( """<p class="bla">[Twitter](http://twitter.com) is too terse;  most journals have more ads then real content;  a private diary isn't social enough.</p><p>A good blog is about content, content and content.  A great or unique writing style makes the difference when content is not a problem.  All other things equal a great design and technological schnickschnack give the blog that aura of invincibility.</p>""")
        )
      ),
      List(Tag("blog"), Tag("writing")),
      Some(InitialLike),
      List(
        Comment(uuid.toString,User("@agreeable"), "totally agree", Option(Created()), Option(InitialLike), Nil),
        Comment(uuid.toString,User("@disagreeable"), "totally disagree", Option(Created())  , Option(InitialDisLike), Nil)
      )
    )
  val e04 = BlogEntry(
      HintedUUIDUniqueIdGenerator("Fourth *blog post* ever"),
      Nascent,
      Option(Created(1343224588265l)),
      None,
      Some(Headline("Finally, Fourth *blog post* ever")),
      Headline("Fourth *blog post* ever"),
      Some(Text("This journey took a long time. It almost never started and then it had many close calls. This post narrates this story.")),
      List(
        Section(
          None,
          Text( """It would have been so easy -- just pick a blogging platform and  voila one has a blog with a lot of functionality. In case you really want to own it just roll out one of the many blogging suites on your server. But **none** can boast of the pain and joy creating your own software  brings with it. The creativity, the decision making, the struggles to implement the vision at the micro and macro level,  and other nitty gritties spice up the adventure.""")
        ),
        Section(
          Some(Headline("The need")),
          Text( """<p class="bla">[Twitter](http://twitter.com) is too terse;  most journals have more ads then real content;  a private diary isn't social enough.</p><p>A good blog is about content, content and content.  A great or unique writing style makes the difference when content is not a problem.  All other things equal a great design and technological schnickschnack give the blog that aura of invincibility.</p>""")
        )
      ),
      List(Tag("blog"), Tag("writing")),
      Some(InitialLike),
      List(
        Comment(uuid.toString,User("@agreeable"), "totally agree", Option(Created()), Option(InitialLike), Nil),
        Comment(uuid.toString,User("@disagreeable"), "totally disagree", Option(Created())  , Option(InitialDisLike), Nil)
      )
    )
  val e05 = BlogEntry(
      HintedUUIDUniqueIdGenerator("Fifth *blog post* ever"),
      Nascent,
      Option(Created(1343224588265l)),
      None,
      Some(Headline("Finally, Fifth *blog post* ever")),
      Headline("Fifth *blog post* ever"),
      Some(Text("This journey took a long time. It almost never started and then it had many close calls. This post narrates this story.")),
      List(
        Section(
          None,
          Text( """It would have been so easy -- just pick a blogging platform and  voila one has a blog with a lot of functionality. In case you really want to own it just roll out one of the many blogging suites on your server. But **none** can boast of the pain and joy creating your own software  brings with it. The creativity, the decision making, the struggles to implement the vision at the micro and macro level,  and other nitty gritties spice up the adventure.""")
        ),
        Section(
          Some(Headline("The need")),
          Text( """<p class="bla">[Twitter](http://twitter.com) is too terse;  most journals have more ads then real content;  a private diary isn't social enough.</p><p>A good blog is about content, content and content.  A great or unique writing style makes the difference when content is not a problem.  All other things equal a great design and technological schnickschnack give the blog that aura of invincibility.</p>""")
        )
      ),
      List(Tag("blog"), Tag("writing")),
      Some(InitialLike),
      List(
        Comment(uuid.toString,User("@agreeable"), "totally agree", Option(Created()), Option(InitialLike), Nil),
        Comment(uuid.toString,User("@disagreeable"), "totally disagree", Option(Created())  , Option(InitialDisLike), Nil)
      )
    )
}