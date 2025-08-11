package com.github.kmbulebu.dsc.api;

import com.github.kmbulebu.dsc.api.listeners.ReadCommandListener;
import java.time.Duration;

/**
 * STUB INTERFACE
 * This is a stub interface to allow the project to compile without the real dependency.
 * Replace this with the actual dependency from the dsc-it100-java library.
 */
public interface DSCPanel {
    void addReadCommandListener(ReadCommandListener listener);
    void connect(String host, int port, String password, Duration timeout) throws Exception;
    void disconnect();
    boolean isConnected();
}
