package controllers

import org.purang.blog.domain._
import java.util.UUID._
import org.purang.blog.domain.Created
import org.purang.blog.domain.Headline
import org.purang.blog.domain.Section
import org.purang.blog.domain.Tag
import org.purang.blog.domain.BlogEntry
import org.purang.blog.domain.User
import org.purang.blog.domain.Comment
import org.purang.blog.domain.Text

import java.util.UUID.{randomUUID => uuid}

object ZExamples {
  val e01 = BlogEntry(
      "First-blog-post-ever_6c8b79f7-6d54-40a5-bcf0-235966e11111",
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
        Comment(uuid.toString, User("@agreeable"), "totally agree", Option(Created()), Option(InitialLike), List(Comment(uuid.toString, User("@disagreeable"), "totally disagree with agreeable", Option(Created()), Option(InitialDisLike), Nil))),
        Comment(uuid.toString,User("@disagreeable"), "totally disagree", Option(Created())  , Option(InitialDisLike), List(Comment(uuid.toString, User("@agreeable"), "totally disagree with disagreeable", Option(Created()), Option(InitialDisLike), Nil)))
      )
    )
  val e02 = BlogEntry(
      "Second-blog-post-ever_6c8b79f7-6d54-40a5-bcf0-235966e22222",
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
    "Third-blog-post-ever_6c8b79f7-6d54-40a5-bcf0-235966e33333",
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
      "Fourth-blog-post-ever_6c8b79f7-6d54-40a5-bcf0-235966e44444",
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
      "Fifth-blog-post-ever_6c8b79f7-6d54-40a5-bcf0-235966e55555",
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