package controllers

import play.api._
import libs.ws.WS
import mvc._
import org.purang.blog.domain.Section
import org.purang.blog.domain.Text
import org.purang.blog.domain.Headline
import org.purang.blog.domain.NascentBlogEntry

object UI extends Controller {
  import play.api.Play.current
  val adminSecret = Play.configuration.getString("admin.secret")

  def index = Action{
    Ok(views.html.index())
  }

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
                  case 201 => Redirect(routes.UI.index())/*Ok(response.body).withHeaders(
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