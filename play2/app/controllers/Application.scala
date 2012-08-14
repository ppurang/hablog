package controllers

import play.api._
import play.api.mvc._
import org.purang.blog.domain._
import java.util.UUID.{randomUUID => uuid}
import org.purang.blog.domain.{Created => c}
import org.purang.blog.domain.addCommentToList

object Application extends Controller {

  private val database = new collection.mutable.HashMap[String, BlogEntry]()
  import  Examples._
  database(e01.uid) = e01
  database(e02.uid) = e02
  database(e03.uid) = e03
  database(e04.uid) = e04
  database(e05.uid) = e05
  def all() = Action {
    Ok(ListBlogEntryJsonSerializer(database.values.toList)).as("application/json")
  }

  def blog(id: String) = Action {
    Ok(BlogEntryJsonDeserializer.unapply(database(id))).as("application/json")
  }

  def createBlog = Action(parse.tolerantText) {
    request => {
      import org.purang.blog.domain.NascentBlogEntryJsonDeserializer
      Logger.info(request.body)
      val entry = convert(NascentBlogEntryJsonDeserializer(request.body))
      database(entry.uid) = entry
      Results.Created(entry.uid).withHeaders(
        LOCATION -> entry.uid
      )
    }
  }

  def addOrReplaceComment(id: String, user: String, ids: String) = Action(parse.tolerantText) {
    request => {
      val nc = Comment(uuid.toString, User(user), request.body, Option(c()), None, Nil)
      database.get(id) match {
        case Some(be) =>  {
          val list = addCommentToList(be.comments, nc, ids)
          list match {
            case Right(nc) => {
              database(be.uid) = be.copy(comments = nc)
              Ok(BlogEntryJsonDeserializer.unapply(database(id))).as("application/json")
            }
            case Left(Right(tooMany)) =>  BadRequest(tooMany.toString)
            case Left(Left(noneFound)) => BadRequest(noneFound.toString)
          }
        }
        case None => NotFound("blog entry: " + id)
      }
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
          Text( """It would have been so easy -- just pick a blogging platform and \n voila one has a blog with a lot of functionality. In case you really want to own it just roll out one of the many blogging suites on your server. But **none** can boast of the pain and joy creating your own software \n brings with it. The creativity, the decision making, the struggles to implement the vision at the micro and macro level, \n and other nitty gritties spice up the adventure.""")
        ),
        Section(
          Some(Headline("The need")),
          Text( """<p class="bla">[Twitter](http://twitter.com) is too terse; \n most journals have more ads then real content; \n a private diary isn't social enough.</p><p>A good blog is about content, content and content. \n A great or unique writing style makes the difference when content is not a problem. \n All other things equal a great design and technological schnickschnack give the blog that aura of invincibility.</p>""")
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
          Text( """It would have been so easy -- just pick a blogging platform and \n voila one has a blog with a lot of functionality. In case you really want to own it just roll out one of the many blogging suites on your server. But **none** can boast of the pain and joy creating your own software \n brings with it. The creativity, the decision making, the struggles to implement the vision at the micro and macro level, \n and other nitty gritties spice up the adventure.""")
        ),
        Section(
          Some(Headline("The need")),
          Text( """<p class="bla">[Twitter](http://twitter.com) is too terse; \n most journals have more ads then real content; \n a private diary isn't social enough.</p><p>A good blog is about content, content and content. \n A great or unique writing style makes the difference when content is not a problem. \n All other things equal a great design and technological schnickschnack give the blog that aura of invincibility.</p>""")
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
          Text( """It would have been so easy -- just pick a blogging platform and \n voila one has a blog with a lot of functionality. In case you really want to own it just roll out one of the many blogging suites on your server. But **none** can boast of the pain and joy creating your own software \n brings with it. The creativity, the decision making, the struggles to implement the vision at the micro and macro level, \n and other nitty gritties spice up the adventure.""")
        ),
        Section(
          Some(Headline("The need")),
          Text( """<p class="bla">[Twitter](http://twitter.com) is too terse; \n most journals have more ads then real content; \n a private diary isn't social enough.</p><p>A good blog is about content, content and content. \n A great or unique writing style makes the difference when content is not a problem. \n All other things equal a great design and technological schnickschnack give the blog that aura of invincibility.</p>""")
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
          Text( """It would have been so easy -- just pick a blogging platform and \n voila one has a blog with a lot of functionality. In case you really want to own it just roll out one of the many blogging suites on your server. But **none** can boast of the pain and joy creating your own software \n brings with it. The creativity, the decision making, the struggles to implement the vision at the micro and macro level, \n and other nitty gritties spice up the adventure.""")
        ),
        Section(
          Some(Headline("The need")),
          Text( """<p class="bla">[Twitter](http://twitter.com) is too terse; \n most journals have more ads then real content; \n a private diary isn't social enough.</p><p>A good blog is about content, content and content. \n A great or unique writing style makes the difference when content is not a problem. \n All other things equal a great design and technological schnickschnack give the blog that aura of invincibility.</p>""")
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
          Text( """It would have been so easy -- just pick a blogging platform and \n voila one has a blog with a lot of functionality. In case you really want to own it just roll out one of the many blogging suites on your server. But **none** can boast of the pain and joy creating your own software \n brings with it. The creativity, the decision making, the struggles to implement the vision at the micro and macro level, \n and other nitty gritties spice up the adventure.""")
        ),
        Section(
          Some(Headline("The need")),
          Text( """<p class="bla">[Twitter](http://twitter.com) is too terse; \n most journals have more ads then real content; \n a private diary isn't social enough.</p><p>A good blog is about content, content and content. \n A great or unique writing style makes the difference when content is not a problem. \n All other things equal a great design and technological schnickschnack give the blog that aura of invincibility.</p>""")
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