package org.purang.blog.backend

import akka.actor.{Actor, ActorSystem}
import org.purang.blog.domain._
import org.purang.blog.domain.ChangeProtocol._
import org.purang.blog.domain.NascentBlogEntry
import org.purang.blog.domain.status._
import org.purang.blog.domain.ChangeProtocol.NewBlogEntryCreated
import org.purang.blog.domain.ChangeProtocol.NewBlogEntryPersistenceFailed
import org.purang.blog.domain.BlogEntry
import org.purang.blog.domain.ChangeProtocol.BlogEntryStateChanged
import com.typesafe.config.ConfigFactory
import scala.Left
import org.purang.blog.domain.ChangeProtocol.CommentAdded
import org.purang.blog.domain.ChangeProtocol.BlogEntryStateChangedPersistenceFailed
import org.purang.blog.domain.ChangeProtocol.ReIndexEventFailed
import org.purang.blog.domain.ChangeProtocol.NewBlogEntryPersistenceFailed
import org.purang.blog.domain.ChangeProtocol.ReIndexEvent
import org.purang.blog.domain.status.BadRequest
import scala.Right
import org.purang.blog.domain.NascentBlogEntry
import org.purang.blog.domain.Created
import org.purang.blog.domain.status.Created
import org.purang.blog.domain.ChangeProtocol.NewBlogEntryCreated
import org.purang.blog.domain.ChangeProtocol.CommentAddedFailed
import org.purang.blog.domain.status.Ok
import org.purang.blog.domain.ChangeProtocol.IndexEventFailed
import org.purang.blog.domain.ChangeProtocol.BlogEntryStateChanged
import org.purang.blog.domain.BlogEntry
import org.purang.blog.domain.ChangeProtocol.IndexEvent

object `package` {

  val config = ConfigFactory.load("hablogsystem.conf")

  val system = ActorSystem("HaBlogSystem", config)

  class Successes(val hint: String) extends Actor {
    protected def receive = {
      case e => {
        println("+[%s SUCCESS] %s".format(hint, e))
      }
    }
  }

  class Failures(val hint: String) extends Actor {
    protected def receive = {
      case e => {
        println("+[%s FAILURE] %s".format(hint, e))
      }
    }
  }


  class BackendMultiplexer extends Actor {
    protected def receive = {
      case e => {
        //println("new blog entry being created " + e)
        //val selection = system.actorSelection("store")
        //val selection = system.actorSelection("store")
        ///println(selection)
        system.actorFor("akka://HaBlogSystem/user/store") ! e
        system.actorFor("akka://HaBlogSystem/user/ebus") ! e
      }
    }
  }

  class EventBusActor(ebus: EventBus) extends Actor {

    protected def receive = {
      case e: Event => ebus handle e
    }

  }

  class StoreActor(store: Store) extends Actor {
    protected def receive = {
      case NewBlogEntryCreated(e) => {
        println("store received for processing " + e.uid)
        store.create(e).fold(
          ps => system.actorFor("akka://HaBlogSystem/user/store-failures") ! NewBlogEntryPersistenceFailed(e, ps)
          ,
          c => {
            system.actorFor("akka://HaBlogSystem/user/store-successes") ! NewBlogEntryCreated(e)
            system.actorFor("akka://HaBlogSystem/user/index") ! IndexEvent(e)
          }
        )
      }

      case BlogEntryStateChanged(uid, newState) => {
        val fetch = store.fetch(uid)
        fetch.fold(
          ps => system.actorFor("akka://HaBlogSystem/user/store-failures") ! BlogEntryStateChangedPersistenceFailed(uid, newState, ps)
          ,
          c => {
            val newC = c.copy(state = newState)
            store.update(newC).fold(
              ps => system.actorFor("akka://HaBlogSystem/user/store-failures") ! BlogEntryStateChangedPersistenceFailed(uid, newState, ps)
              ,
              ok => {
                system.actorFor("akka://HaBlogSystem/user/store-successes") ! BlogEntryStateChanged(uid, newState)
                system.actorFor("akka://HaBlogSystem/user/index") ! ReIndexEvent(newC)
              }
            )
          }
        )
      }

      case CommentAdded(uid, comment, path) => {
        val fetch = store.fetch(uid)
        fetch.fold(
          ps => system.actorFor("akka://HaBlogSystem/user/store-failures") ! CommentAddedFailed(uid, comment, path, ps)
          ,
          c => {
            val list = addCommentToList(c.comments, comment, path)
            list match {
              case Right(ncc) => {
                val newBE = c.copy(comments = ncc)
                store.update(newBE).fold(
                  ps => system.actorFor("akka://HaBlogSystem/user/store-failures") ! CommentAddedFailed(uid, comment, path, ps)
                  ,
                  ok => {
                    system.actorFor("akka://HaBlogSystem/user/store-successes") ! CommentAdded(uid, comment, path)
                    system.actorFor("akka://HaBlogSystem/user/index") ! ReIndexEvent(newBE)
                  }
                )
              }

              case Left(Right(tooMany)) =>  system.actorFor("akka://HaBlogSystem/user/store-failures") ! CommentAddedFailed(uid, comment, path, asProblemStatuses(BadRequest(Some(tooMany.toString))))

              case Left(Left(noneFound)) => system.actorFor("akka://HaBlogSystem/user/store-failures") ! CommentAddedFailed(uid, comment, path, asProblemStatuses(BadRequest(Some(noneFound.toString))))
            }
          }
        )
      }


    }
  }

  class IndexActor(index: Index) extends Actor {
    protected def receive = {
      case IndexEvent(e) => {
        println("received indexing event: " + e.uid)
        if (e.state == Published) {
          index.index(e).fold(
            ps => system.actorFor("akka://HaBlogSystem/user/index-failures") ! IndexEventFailed(e, ps)
            ,
            ok => system.actorFor("akka://HaBlogSystem/user/index-successes") ! IndexEvent(e)
          )
        }
      }

      case ReIndexEvent(e) => {
        println("received (re)indexing event: " + e.uid)
        if (e.state == Published) {
          index.reindex(e).fold(
            ps => system.actorFor("akka://HaBlogSystem/user/index-failures") ! ReIndexEventFailed(e, ps)
            ,
            ok => system.actorFor("akka://HaBlogSystem/user/index-successes") ! ReIndexEvent(e)
          )
        }
      }
    }
  }


}

/*
package object http {

  import status._
  import ChangeProtocol._

  case class HttpNewBlogEntry(ebus: EventBus, store: Store, index: Index) extends NewBlogEntry {
    override def apply(nbe: NascentBlogEntry) = {
      //fire event
      val ebusCreated = ebus.created(nbe)
      //store
      val storeCreated = store.create(nbe)
      //index
      val indexCreated = index.index(nbe)
      asCommonStatuses(Ok(None))
    }
  }

  case class HttpChangeState(ebus: EventBus, store: Store with Lookup, index: Index) extends ChangeState {
    override def apply(id: BlogEntryId) = state => {
      val findBE: CommonStatuses or BlogEntry = store.get(id).fold(
        t => Left(asCommonStatuses(Error(Some(t.getMessage)))),
        obe => OptionFold.fold(obe)({
          asCommonStatuses(NotFound(Option(id)))
        }, {
          be => Right(be.copy(state = state))
        })
      )
      //fire event
      findBE.fold(
      {x => x},
      {
        be =>
        val ebusCreated = ebus.updated(be)
        //store
        val storeCreated = store.update(be)
        //index
        val indexCreated = index.reindex(be)

        asCommonStatuses(Ok(None))
      })

    }
  }





}
*/

import status._

trait Store {
  def fetch: UniqueId => ProblemStatuses or BlogEntry

  def create: BlogEntry => ProblemStatuses or status.Created

  def update: BlogEntry => ProblemStatuses or Ok
}

trait Index {
  def index: BlogEntry => ProblemStatuses or Ok

  def reindex: BlogEntry => ProblemStatuses or Ok

  def delete: UniqueId => ProblemStatuses or Ok
}

trait EventBus {
  def handle: Event => ProblemStatuses or Ok

//  def happened: Event => ProblemStatuses or Ok
}

trait Lookup {
  def get: UniqueId => Throwable or Option[BlogEntry]
}


