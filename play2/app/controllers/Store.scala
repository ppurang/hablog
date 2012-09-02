package controllers

import org.purang.blog.backend.Store
import org.purang.net.http.{POST, ApplicationJson, ContentType, GET}
import org.purang.net.http.ning._
import org.purang.blog.domain.???
import org.purang.blog.domain.status._
import org.purang.blog.domain.{BlogEntryJsonSerializer, BlogEntryJsonDeserializer}

class Riak(url: String) extends Store {

  def fetch = u => {
    (GET > url + u >> ContentType(ApplicationJson)) ~> {
      _.fold(
        t => Left(asProblemStatuses(Error(Option(t._1.toString)))),
        _ match {
          case (200, _, Some(body), _) => Right(BlogEntryJsonDeserializer(body))
          case (200, _, None, _) => Left(asProblemStatuses(Error(Option("Store fetch returned 200 without a body for %s".format(u)))))
          case (s, headers, body, _) => Left(asProblemStatuses(Error(Option("Store fetch returned %s for %s".format(s, u))))) //todo not enough : not everything is an error
        }
      )
    }
  }

  def create = be => {
    (POST > url + be.uid >> ContentType(ApplicationJson) >>> BlogEntryJsonSerializer(be)) ~> {
      _.fold(
        t => Left(asProblemStatuses(Error(Option(t._1.toString)))),
        _ match {
          case (204, _, _, _) => Right(Created(url + be.uid, Some(url + be.uid)))
          case (s, headers, body, _) => Left(asProblemStatuses(Error(Option("Store create returned %s for %s".format(s, be)))))//todo not enough : not everything is an error
        }
      )
    }
  }

  def update = be => {
      (POST > url + be.uid >> ContentType(ApplicationJson) >>> BlogEntryJsonSerializer(be)) ~> {
        _.fold(
          t => Left(asProblemStatuses(Error(Option(t._1.toString)))),
          _ match {
            case (204, _, _, _) => Right(Ok(Option("updated")))
            case (s, headers, body, _) => Left(asProblemStatuses(Error(Option("Store update returned %s for %s".format(s, be)))))//todo not enough : not everything is an error
          }
        )
      }
    }
}
