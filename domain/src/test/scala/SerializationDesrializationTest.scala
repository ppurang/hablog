package org.purang.blog.domain

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class SerializationDesrializationTest extends FunSuite with ShouldMatchers{
    import Fixtures._
    test("should allow deserializing some blog-entry json"){
      val blogEntry: BlogEntry = BlogEntryJsonDeserializer(json)
      val section: Section = blogEntry.content.sections(2)
      section.headline  should be(Some(Headline("The need")))
      section.paragraphs.size should be(2)
    }

    test("should allow serializing some blog entry"){
      //use a lot of magic :) implicits and type
      import BlogEntryJsonSerializer._
      (entry: String) should include(""""created":{"time":35255716153154}""")
    }
}

object Fixtures {
  def json = {
    """
      |{
      |    "uid":"8aac796b-e3f4-4e89-8b23-735110f8c3ba",
      |    "state":"Nascent",
      |    "created":{
      |        "time":35255716153154
      |    },
      |    "content":{
      |        "headline":{
      |            "headline":"First *blog post* ever"
      |        },
      |        "sections":[
      |            {
      |                "headline":{
      |                    "headline":"Summary"
      |                },
      |                "paragraphs":[
      |                    {
      |                        "content":"This journey took a long time. It almost never started and then it had many close calls. This post narrates this story."
      |                    }
      |                ]
      |            },
      |            {
      |                "paragraphs":[
      |                    {
      |                        "content":"It would have been so easy -- just pick a blogging platform and\n voila one has a blog with a lot of functionality. In case you really want to own it just roll out one of the many blogging suites on your server. But **none** can boast of the pain and joy creating your own software\n        brings with it. The creativity, the decision making, the struggles to implement the vision at the micro and macro level,\n        and other nitty gritties spice up the adventure."
      |                    }
      |                ]
      |            },
      |            {
      |                "headline":{
      |                    "headline":"The need"
      |                },
      |                "paragraphs":[
      |                    {
      |                        "content":"[Twitter](http://twitter.com) is too terse;\n most journals have more ads then real content;\n a private diary isn't social enough."
      |                    },
      |                    {
      |                        "content":"A good blog is about content, content and content.\n A great or unique writing style makes the difference when content is not a problem.\n        All other things equal a great design and technological schnickschnack give the blog that aura of invincibility."
      |                    }
      |            ]
      |        }
      |    ]
      |},"tags":[
      |    {
      |        "tag":"blog"
      |    },
      |    {
      |        "tag":"writing"
      |    }
      |]}
      |
    """.stripMargin
  }
  val entry = BlogEntry(
    "8aac796b-e3f4-4e89-8b23-735110f8c3ba",
    Nascent,
    Created(35255716153154l),
    None,
    Content(
      Headline("First *blog post* ever"),
      List(
        Section(
          Some(Headline("Summary")),
          List(
            Paragraph("This journey took a long time. It almost never started and then it had many close calls. This post narrates this story.")
          )
        ),
        Section(
          None,
          List(
            Paragraph("""It would have been so easy -- just pick a blogging platform and \n voila one has a blog with a lot of functionality. In case you really want to own it just roll out one of the many blogging suites on your server. But **none** can boast of the pain and joy creating your own software \n brings with it. The creativity, the decision making, the struggles to implement the vision at the micro and macro level, \n and other nitty gritties spice up the adventure.""")
          )
        ),
        Section(
          Some(Headline("The need")),
          List(
            Paragraph("""[Twitter](http://twitter.com) is too terse; \n most journals have more ads then real content; \n a private diary isn't social enough."""),
            Paragraph("""A good blog is about content, content and content. \n A great or unique writing style makes the difference when content is not a problem. \n All other things equal a great design and technological schnickschnack give the blog that aura of invincibility.""")
          )
        )
      )
    ),
    List(Tag("blog"), Tag("writing")),
    None,
    List()
  )
}
