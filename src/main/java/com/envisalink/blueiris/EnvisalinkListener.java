package com.envisalink.blueiris;

import com.github.kmbulebu.dsc.it100.ConfigurationBuilder;
import com.github.kmbulebu.dsc.it100.IT100;
import com.github.kmbulebu.dsc.it100.commands.read.PartitionArmedCommand;
import com.github.kmbulebu.dsc.it100.commands.read.PartitionDisarmedCommand;
import com.github.kmbulebu.dsc.it100.commands.read.ReadCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.functions.Action1;

public class EnvisalinkListener {

    private static final Logger logger = LogManager.getLogger(EnvisalinkListener.class);

    private final String host;
    private final int port;
    private final String password;
    private IT100 it100;
    private EnvisalinkEventHandler eventHandler;

    public EnvisalinkListener(Config config) {
        this.host = config.getProperty("envisalink.host");
        this.port = config.getIntProperty("envisalink.port", 4025);
        this.password = config.getProperty("envisalink.password");
    }

    public void setEventHandler(EnvisalinkEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    public void start() throws Exception {
        logger.info("Connecting to Envisalink at {}:{}", host, port);

        // Configure for Envisalink
        // Hostname/IP: envisalink, Port: 4025, Password: user
        it100 = new IT100(new ConfigurationBuilder().withRemoteSocket(host, port)
                //.withEnvisalinkPassword(password) // TODO: Figure out why this does not compile
                .build());

        // Begin listening to IT-100 commands through an rxjava Observable
        it100.connect();
        it100.getReadObservable().subscribe(new Action1<ReadCommand>() {
            @Override
            public void call(ReadCommand command) {
                onReadCommand(command);
            }
        });
        logger.info("Successfully connected to Envisalink.");
    }

    public void onReadCommand(ReadCommand command) {
        logger.debug("Received command from Envisalink: {}", command);

        if (eventHandler == null) {
            logger.warn("Received a command, but no event handler is registered.");
            return;
        }

        // Check the type of command and call the appropriate handler method
        if (command instanceof PartitionArmedCommand) {
            PartitionArmedCommand armCmd = (PartitionArmedCommand) command;
            if (armCmd.getMode() == PartitionArmedCommand.ArmedMode.AWAY) {
                logger.info("Partition {} has been armed (Away).", armCmd.getPartition());
                eventHandler.onPartitionArmAway(armCmd.getPartition());
            }
        } else if (command instanceof PartitionDisarmedCommand) {
            PartitionDisarmedCommand disarmCmd = (PartitionDisarmedCommand) command;
            logger.info("Partition {} has been disarmed.", disarmCmd.getPartition());
            eventHandler.onPartitionDisarmed(disarmCmd.getPartition());
        }
        // PartitionArmStayCommand is intentionally ignored as per requirements.
    }

    public void stop() {
        if (it100 != null) {
            logger.info("Disconnecting from Envisalink.");
            try {
                it100.disconnect();
            } catch (Exception e) {
                logger.error("Error disconnecting from Envisalink", e);
            }
        }
    }
}
