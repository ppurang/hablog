package controllers

import play.api._
import play.api.mvc._
import org.purang.blog.domain._

object Application extends Controller {

  private val database = new collection.mutable.HashMap[String, BlogEntry]()

  def all() = Action {
    Ok(ListBlogEntryJsonSerializer(database.values.toList)).as("application/json")
    //Ok("tada")
  }

  def blog(id: String) = Action {
    Ok(BlogEntryJsonDeserializer.unapply(database(id))).as("application/json")
  }

  def createBlog = Action(parse.tolerantText) {
    request => {
      import org.purang.blog.domain.NascentBlogEntryJsonDeserializer
      val entry = convert(NascentBlogEntryJsonDeserializer(request.body))
      database(entry.uid) = entry
      Results.Created(entry.uid).withHeaders(
        LOCATION -> entry.uid
      )
    }
  }

/*
  def addComment(id: String, ids: String)  = Action(parse.tolerantText) {
      request => {
        database.get(id) match {
          case Some(be) => {
            //find the comment and update it
            //not found return bad request

          }
          case None => NotFound(id)
        }

        Results.Created(entry.uid).withHeaders(
          LOCATION -> entry.uid
        )
      }
    }
*/


}