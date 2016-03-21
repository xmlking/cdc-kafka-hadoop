import groovy.util.logging.Slf4j
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.KStreamBuilder
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.processor.internals.WallclockTimestampExtractor;

@Slf4j
class MaterializedView {

    boolean testTrue() {

        true
    }

    public static void main(String[] args) throws Exception {
        log.info 'Hello World'
        Properties props = new Properties();

        props.put(StreamsConfig.JOB_ID_CONFIG, "kafka-materializer");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(StreamsConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(StreamsConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(StreamsConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(StreamsConfig.TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        props.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, "localhost:2181")


        StreamsConfig config = new StreamsConfig(props);

        KStreamBuilder builder = new KStreamBuilder();

        KTable<String, String> names = builder.table("maxwell");

        names.mapValues({record -> println  record });

        KafkaStreams kstreams = new KafkaStreams(builder, config);
        kstreams.start();
    }
}
