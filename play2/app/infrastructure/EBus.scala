package infrastructure

import org.purang.blog.backend.EventBus
import org.purang.blog.domain.status.Ok

class Kafka extends EventBus {
  lazy val kafkaInterOp = new KafkaInterOp()

  def handle = e => {
    println("[ ----  KAFKA ---- ] sending message now!")
    kafkaInterOp.sendMessage(e.stringIdentifier + "," + e.toJson())
    Right(Ok(None))
  }
}
