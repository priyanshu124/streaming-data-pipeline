package com.ad.bidding.analytics;

import org.apache.kafka.clients.producer.ProducerConfig;
import java.util.Properties;

public class App {
    public static void main(String[] args) {
        System.out.println("--- Environment Check ---");
        
        // Test if Java is working
        System.out.println("Java Version: " + System.getProperty("java.version"));

        // Test if Gradle pulled the Kafka dependency correctly
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        
        System.out.println("Kafka Dependency Check: " + props.getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        System.out.println("-------------------------");
        System.out.println("RESULT: Project is set up correctly!");
    }
}
