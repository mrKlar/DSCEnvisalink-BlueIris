package com.envisalink.blueiris;

import com.github.kmbulebu.dsc.api.DSCPanel;
import com.github.kmbulebu.dsc.api.commands.read.PartitionArmAwayCommand;
import com.github.kmbulebu.dsc.api.commands.read.PartitionDisarmedCommand;
import com.github.kmbulebu.dsc.api.commands.read.ReadCommand;
import com.github.kmbulebu.dsc.api.listeners.ReadCommandListener;
import com.github.kmbulebu.dsc.api.netty.NettyDSCPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;

public class EnvisalinkListener implements ReadCommandListener {

    private static final Logger logger = LogManager.getLogger(EnvisalinkListener.class);

    private final String host;
    private final int port;
    private final String password;
    private DSCPanel dscPanel;
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
        // Using the Netty-based implementation
        dscPanel = new NettyDSCPanel();

        // Add this class as a listener for command events
        dscPanel.addReadCommandListener(this);

        // Connect to the panel.
        dscPanel.connect(host, port, password, Duration.ofSeconds(10));
        logger.info("Successfully connected to Envisalink.");
    }

    @Override
    public void onReadCommand(ReadCommand command) {
        logger.debug("Received command from Envisalink: {}", command);

        if (eventHandler == null) {
            logger.warn("Received a command, but no event handler is registered.");
            return;
        }

        // Check the type of command and call the appropriate handler method
        if (command instanceof PartitionArmAwayCommand) {
            PartitionArmAwayCommand armCmd = (PartitionArmAwayCommand) command;
            logger.info("Partition {} has been armed (Away).", armCmd.getPartition());
            eventHandler.onPartitionArmAway(armCmd.getPartition());
        } else if (command instanceof PartitionDisarmedCommand) {
            PartitionDisarmedCommand disarmCmd = (PartitionDisarmedCommand) command;
            logger.info("Partition {} has been disarmed.", disarmCmd.getPartition());
            eventHandler.onPartitionDisarmed(disarmCmd.getPartition());
        }
        // PartitionArmStayCommand is intentionally ignored as per requirements.
    }

    public void stop() {
        if (dscPanel != null && dscPanel.isConnected()) {
            logger.info("Disconnecting from Envisalink.");
            dscPanel.disconnect();
        }
    }
}
