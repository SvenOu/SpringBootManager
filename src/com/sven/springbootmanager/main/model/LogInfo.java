package com.sven.springbootmanager.main.model;

public class LogInfo {
    private String owner;
    private String logInfo;

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getLogInfo() {
        return logInfo;
    }

    public void setLogInfo(String logInfo) {
        this.logInfo = logInfo;
    }

    @Override
    public String toString() {
        return "LogInfo{" +
                "owner='" + owner + '\'' +
                ", logInfo='" + logInfo + '\'' +
                '}';
    }
}
