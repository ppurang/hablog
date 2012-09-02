package controllers

import org.purang.blog.backend.Index
import org.purang.net.http.{POST, ApplicationJson, ContentType, GET}
import org.purang.net.http.ning._
import org.purang.blog.domain.???
import org.purang.blog.domain.status._
import org.purang.blog.domain.BlogEntryJsonSerializer

class ES(url: String) extends Index {
  def index = be => {
    (POST > url + be.uid >> ContentType(ApplicationJson) >>> BlogEntryJsonSerializer(be)) ~> {
              _.fold(
               t => Left(asProblemStatuses(Error(Option(t._1.toString)))),
              _  match {
                case (200, _, _, _) => Right(Ok(None))
                case (201, _, _, _) => Right(Ok(None))
                case (s, _, _, _) => Left(asProblemStatuses(Error(Option("Indexing returned %s for %s".format(s, be)))))//todo not enough : not everything is an error
              }
              )
            }
          }

  def reindex = be => {
      (POST > url + be.uid >> ContentType(ApplicationJson) >>> BlogEntryJsonSerializer(be)) ~> {
                _.fold(
                 t => Left(asProblemStatuses(Error(Option(t._1.toString)))),
                _  match {
                  case (200, _, _, _) => Right(Ok(None))
                  case (201, _, _, _) => Right(Ok(None))
                  case (s, _, _, _) => Left(asProblemStatuses(Error(Option("Re-indexing returned %s for %s".format(s, be)))))//todo not enough : not everything is an error
                }
                )
              }
            }
  def delete = ???
}
