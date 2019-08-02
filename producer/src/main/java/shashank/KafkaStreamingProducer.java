package shashank;

import org.apache.kafka.clients.producer.*;
//import org.apache.kafka.clients.producer.Callback;
//import org.apache.kafka.clients.producer.ProducerConfig;
//import org.apache.kafka.clients.producer.ProducerRecord;
//import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import java.io.*;
import java.util.zip.GZIPInputStream;



public class KafkaStreamingProducer {

    Logger logger = LoggerFactory.getLogger(KafkaStreamingProducer.class.getName());

    public KafkaStreamingProducer(){}

    public static void main(String[] args) {

        new KafkaStreamingProducer().run();

    }

    public void run(){

        logger.info("Setup");

        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(10000);

        Streaming(msgQueue);

//        create a kafka producer
        KafkaProducer<String, String> producer = createKafkaProducer();

//        add a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("stopping application...");
            logger.info("closing producer...");
            producer.close();
            logger.info("done!");
        }));

//        loop to send data to kafka
        // on a different thread, or multiple different threads....
        while (true) {
            String msg = null;
            try {
                msg = msgQueue.poll(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
//                client.stop();
            }
            if(msg != null){
                logger.info(msg);
                producer.send(new ProducerRecord<>("customer_reviews", null, msg), new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                        if(e != null){
                            logger.error("Something bad happened!", e);
                        }
                    }
                });
            }
        }
//        logger.info("End of application");

    }

public void Streaming(BlockingQueue<String> msgQueue){

    File folder = new File("/home/ubuntu/Desktop/Big Data Dataset");
    File[] listOfFiles = folder.listFiles();
    String[] filesArray = new String[listOfFiles.length];

    for (int i = 0; i < listOfFiles.length; i++) {
        if (listOfFiles[i].isFile()) {
            filesArray[i] = listOfFiles[i].getName();
        }
    }

    GZIPInputStream gzip = null;
    String path1 = "/home/ubuntu/Desktop/Big Data Dataset/" + filesArray[3];
    String path2 = "/home/ubuntu/Desktop/Big Data Dataset/" + filesArray[4];
    try {
        SequenceInputStream stream = new SequenceInputStream(new FileInputStream(path1), new FileInputStream(path2));

        for (int f = 3; f < 4; f++) {
            String path = "/home/ubuntu/Desktop/Big Data Dataset/" + filesArray[f];

            InputStream temp = new FileInputStream(path);
            stream = new SequenceInputStream(stream, temp);

        }

        gzip = new GZIPInputStream(stream);
        BufferedReader br = new BufferedReader(new InputStreamReader(gzip));
        String head = br.readLine();
        String[] features = head.split("\t");

        for (int i = 0; i < 100; i++) {
            String line = br.readLine();
            String[] data = line.split("\t");

            JSONObject jsonString = new JSONObject();

            for (int k = 0; k < data.length; k++) {
                if (k == 12 || k == 13 || k == 5) {
                    continue;
                }
                jsonString.put(features[k], data[k]);
            }
            msgQueue.add(String.valueOf(jsonString));
            System.out.println(jsonString);
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}


    public KafkaProducer<String, String> createKafkaProducer(){
        String bootstrapServers = "127.0.0.1:9092";

//        create Producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

//        create a safe Producer
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        properties.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
        properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");

//        high throughput Producer
        properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20");
        properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32*1024));

//        create the Producer
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);
        return producer;

    }
}
