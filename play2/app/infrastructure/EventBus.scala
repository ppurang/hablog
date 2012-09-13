package infrastructure

import org.purang.blog.backend.EventBus
import org.purang.blog.domain.status.Ok
import java.util.Properties
import kafka.producer.{Producer, ProducerConfig, ProducerData}
import kafka.message.Message

class Kafka extends EventBus {

  def handle = e => {
    println("[ ----  KAFKA ---- ] sending message now!")
    val topic = "ppurangblog"
    val props = new Properties()
    //props.put("zk.connect", "localhost:2181")
    //props.put("zk.connect", "0.0.0.0:2181")
    props.put("serializer.class", "kafka.serializer.StringEncoder")
    props.put("producer.type", "async")
    props.put("compression.codec", "1")
    props.put("broker.list", "localhost:9092")
    val config = new ProducerConfig(props)
    val producer = new Producer[String, String](config)
    producer.send(new ProducerData(topic, e.stringIdentifier + "," + e.toJson()))
    Right(Ok(None))
  }
}
