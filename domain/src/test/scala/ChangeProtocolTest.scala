package org.purang.blog.domain

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import java.util.UUID.{randomUUID => uuid}
import org.purang.blog.domain.ChangeProtocol.{BlogEntryStateChangedPersistenceFailed, BlogEntryStateChanged}
import org.purang.blog.domain.status._
import org.purang.blog.domain.status.asProblemStatuses

class ChangeProtocolTest extends FunSuite with ShouldMatchers {
  import Fixtures._

  test("should allow deserializing some event json") {
    SomeEvent.event.fromJson(SomeEvent.json) should be(SomeEvent.event)
  }

  test("should allow serializing some event") {
    SomeEvent.event.toJson should be (SomeEvent.json)
  }

  test("should allow deserializing some failed event json") {
    FailedEvent.event.fromJson(FailedEvent.json) should be(FailedEvent.event)
  }

  test("should allow serializing some failed event") {
    println(FailedEvent.event.stringIdentifier)
    FailedEvent.event.toJson should be (FailedEvent.json)
  }


 object Fixtures {
   object SomeEvent {
     def json = """{"uid":"unique-maybe","state":"Published"}"""

     val event = BlogEntryStateChanged("unique-maybe", Published)
   }
   object FailedEvent {
     def json = """{"uid":"unique-maybe","state":"Published","ps":{"BadRequest":{"message":"==None=="}}}"""

     val event = BlogEntryStateChangedPersistenceFailed("unique-maybe", Published, asProblemStatuses(BadRequest(None)) )
   }
 }

}

