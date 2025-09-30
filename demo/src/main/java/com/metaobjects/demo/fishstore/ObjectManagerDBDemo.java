package com.metaobjects.demo.fishstore;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.metaobjects.demo.fishstore.service.ObjectManagerDBTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple demo class to test ObjectManagerDB functionality without web server
 */
public class ObjectManagerDBDemo {

    private static final Logger log = LoggerFactory.getLogger(ObjectManagerDBDemo.class);

    public static void main(String[] args) {
        log.info("=== STARTING OBJECTMANAGERDB DEMO ===");

        try {
            // Load Spring application context
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                "file:src/main/webapp/WEB-INF/fishstore-spring.xml"
            );

            // Get the test service and run the demo
            ObjectManagerDBTestService testService = context.getBean(ObjectManagerDBTestService.class);
            testService.testObjectManagerDB();

            log.info("=== OBJECTMANAGERDB DEMO COMPLETED SUCCESSFULLY ===");

            // Close context
            context.close();

        } catch (Exception e) {
            log.error("ObjectManagerDB demo failed", e);
            e.printStackTrace();
        }
    }
}