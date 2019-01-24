package com.sven.springbootmanager.common.bean;

public class JavaProcess {
    String pid;
    String pakageName;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPakageName() {
        return pakageName;
    }

    public void setPakageName(String pakageName) {
        this.pakageName = pakageName;
    }

    @Override
    public String toString() {
        return "JavaProcess{" +
                "pid='" + pid + '\'' +
                ", pakageName='" + pakageName + '\'' +
                '}';
    }
}
