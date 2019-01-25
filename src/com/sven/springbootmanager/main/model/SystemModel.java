package com.sven.springbootmanager.main.model;

import java.util.List;

public class SystemModel{
    private List<LogInfo> logInfos;

    public LogInfo findLogInfoByOwner(String owner){
        if(logInfos == null){
            return null;
        }
        for (LogInfo logInfo : logInfos) {
            if(owner.equals(logInfo.getOwner())){
                return logInfo;
            }
        }
        return null;
    }

    public List<LogInfo> getLogInfos() {
        return logInfos;
    }

    public void setLogInfos(List<LogInfo> logInfos) {
        this.logInfos = logInfos;
    }

    @Override
    public String toString() {
        return "SystemModel{" +
                "logInfos=" + logInfos +
                '}';
    }
}
