package com.example.progettogio.interfaces;

public interface ReplicationCallback {

    void replicatorConnecting();

    void replicatorBusy();

    void replicatorOffline();

    void replicatorStopped();
}
