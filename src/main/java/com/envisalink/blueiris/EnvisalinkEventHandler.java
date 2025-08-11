package com.envisalink.blueiris;

public interface EnvisalinkEventHandler {
    void onPartitionArmAway(int partition);
    void onPartitionDisarmed(int partition);
}
