package com.envisalink.blueiris;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main implements EnvisalinkEventHandler {

    private static final Logger logger = LogManager.getLogger(Main.class);

    private final BlueIrisService blueIrisService;
    private final Config config;

    public Main() {
        this.config = new Config("config.properties");
        this.blueIrisService = new BlueIrisService(config);
        EnvisalinkListener envisalinkListener = new EnvisalinkListener(config);
        envisalinkListener.setEventHandler(this);

        // Add a shutdown hook to gracefully disconnect
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown signal received. Disconnecting...");
            blueIrisService.close();
            envisalinkListener.stop();
            logger.info("Shutdown complete.");
        }));

        try {
            // Initial connections
            blueIrisService.connect();
            envisalinkListener.start();
        } catch (Exception e) {
            logger.fatal("Failed to start application services. Please check configuration and connectivity.", e);
            System.exit(1); // Exit if we can't establish initial connections
        }
    }

    @Override
    public void onPartitionArmAway(int partition) {
        logger.info("Handling Partition Arm Away event for partition {}", partition);
        int profileId = config.getIntProperty("profile.armed.away", -1);
        if (profileId != -1) {
            try {
                blueIrisService.setProfile(profileId);
            } catch (Exception e) {
                logger.error("Failed to set Blue Iris profile after arm event.", e);
            }
        } else {
            logger.warn("No profile configured for 'profile.armed.away'.");
        }
    }

    @Override
    public void onPartitionDisarmed(int partition) {
        logger.info("Handling Partition Disarmed event for partition {}", partition);
        int profileId = config.getIntProperty("profile.disarmed", -1);
        if (profileId != -1) {
            try {
                blueIrisService.setProfile(profileId);
            } catch (Exception e) {
                logger.error("Failed to set Blue Iris profile after disarm event.", e);
            }
        } else {
            logger.warn("No profile configured for 'profile.disarmed'.");
        }
    }

    public static void main(String[] args) {
        logger.info("Starting Envisalink to Blue Iris Sync Service...");
        new Main();
        // The application will now run and listen for events.
        // The main thread will exit, but the Netty and Shutdown Hook threads will keep the JVM alive.
    }
}
