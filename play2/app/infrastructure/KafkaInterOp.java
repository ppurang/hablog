package infrastructure;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.javaapi.producer.ProducerData;
import kafka.producer.ProducerConfig;
import kafka.message.Message;

public class KafkaInterOp {
    public static final String TOPIC = "ppurangblog";

    private final Producer<Message, Message> producer;

    public KafkaInterOp() {
        final Properties props = new Properties();
        props.put("zk.connect", "localhost:2181");
        final ProducerConfig config = new ProducerConfig(props);
        this.producer = new Producer<Message, Message>(config);
    }


    public void sendMessage(String s) throws UnsupportedEncodingException {
        try {
            producer.send(new ProducerData<Message, Message>(TOPIC, new Message(s.getBytes("utf-8"))));
            System.out.println("sent!");
        } catch (final UnsupportedEncodingException e) {
            System.out.println("ohoh! exception");
        }
    }
}
