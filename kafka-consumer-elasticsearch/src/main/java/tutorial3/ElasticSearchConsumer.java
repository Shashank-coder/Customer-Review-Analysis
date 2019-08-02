package tutorial3;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ElasticSearchConsumer {



    public static RestHighLevelClient createClient(){

           String hostname = "kafka-4354220244.us-west-2.bonsaisearch.net";
           String username = "fr423rkc9u";
           String password = "v7wurgpd2q";

           final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
           credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        RestClientBuilder builder = RestClient.builder(new HttpHost(hostname, 443, "https"))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {

                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
                        return httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
        });

        RestHighLevelClient client = new RestHighLevelClient(builder);
        return client;
    }

    public static KafkaConsumer<String, String> createConsumer(String topic){

        String bootstrapServers = "127.0.0.1:9092";
        String groupID = "kafka-elasticsearch4";

        // create consumer configs
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupID);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");


        // create consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);

        consumer.subscribe(Arrays.asList(topic));

        return consumer;
    }

    public static void main(String[] args) throws IOException {

        String serverIP = "127.0.0.1";
        String keyspace = "bigData";

        Logger logger = LoggerFactory.getLogger(ElasticSearchConsumer.class.getName());
        RestHighLevelClient client = createClient();


        KafkaConsumer<String, String> consumer = createConsumer("customer_reviews");

        Cluster cluster = Cluster.builder()
                .addContactPoints(serverIP)
                .build();

        Session session = cluster.connect(keyspace);

        logger.info("database connection established!");

        String createTable = "CREATE TABLE IF NOT EXISTS reviews (review_id varchar Primary Key,marketplace varchar, verified_purchase varchar, helpful_votes bigint," +
                "product_parent varchar, review_date date, total_votes bigint, product_id varchar, star_rating bigint, customer_id varchar," +
                "vine varchar, product_category varchar);";

        session.execute(createTable);
        // poll for new data
        while(true) {
            ConsumerRecords<String, String> records =
                    consumer.poll(Duration.ofMillis(100));

            for (ConsumerRecord<String, String> record: records) {
                // where we insert data into Elastic Search
                IndexRequest indexRequest = new IndexRequest(
                        "customer",
                        "reviews"
                ).source(record.value(), XContentType.JSON);

                IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
                String id = indexResponse.getId();
                logger.info(id);

                String x = record.value().toString();
                System.out.println(x);
                String[] y = x.split(",");
                String[] z = new String[12];
                for(int i=0; i < 12; i++){

                    z[i] = y[i].split(":")[1].split("}")[0].replaceAll("\"", "");
                }


                JSONObject obj = new JSONObject();
                obj.put("review_id", z[0]);
                obj.put("product_parent", z[1]);
                obj.put("review_date", z[2]);
                obj.put("total_votes", Integer.parseInt(z[3]));
                obj.put("marketplace", z[4]);
                obj.put("product_id", z[5]);
                obj.put("star_rating", Integer.parseInt(z[6]));
                obj.put("verified_purchase", z[7]);
                obj.put("customer_id", z[8]);
                obj.put("helpful_votes", Integer.parseInt(z[9]));
                obj.put("vine", z[10]);
                obj.put("product_category", z[11]);

                String dataIntsert = "INSERT INTO bigData.reviews JSON" + "'" + obj.toString() + "'";

                session.execute(dataIntsert);
                logger.info("data inserted!");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

//        client.close();
    }
}
