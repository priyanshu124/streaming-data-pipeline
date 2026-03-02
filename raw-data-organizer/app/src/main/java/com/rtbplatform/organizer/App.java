package com.rtbplatform.organizer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;

import com.rtbplatform.organizer.config.OrganizerProperties;

@SpringBootApplication
@EnableConfigurationProperties(OrganizerProperties.class)
public class App {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(App.class, args);
        // OrganizerProperties props = context.getBean(OrganizerProperties.class);
        FileOrganizer organizer = context.getBean(FileOrganizer.class);

        try {
            long startTime = System.currentTimeMillis();
            int filesOrganized = organizer.organize();
            long duration = System.currentTimeMillis() - startTime;

            System.out.println("\n=== Organization Complete ===");
            System.out.println("Files organized: " + filesOrganized);
            System.out.println("Duration: " + duration + "ms");
        } catch (Exception e) {
            System.err.println("Organization failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
