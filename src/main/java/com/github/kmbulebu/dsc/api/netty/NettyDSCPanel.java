package com.github.kmbulebu.dsc.api.netty;

import com.github.kmbulebu.dsc.api.DSCPanel;
import com.github.kmbulebu.dsc.api.listeners.ReadCommandListener;
import java.time.Duration;

/**
 * STUB CLASS
 * This is a stub class to allow the project to compile without the real dependency.
 * Replace this with the actual dependency from the dsc-it100-java library.
 */
public class NettyDSCPanel implements DSCPanel {

    @Override
    public void addReadCommandListener(ReadCommandListener listener) {
        // Do nothing
    }

    @Override
    public void connect(String host, int port, String password, Duration timeout) throws Exception {
        // Do nothing
    }

    @Override
    public void disconnect() {
        // Do nothing
    }

    @Override
    public boolean isConnected() {
        return false;
    }
}
