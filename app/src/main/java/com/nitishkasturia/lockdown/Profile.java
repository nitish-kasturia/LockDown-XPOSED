package com.nitishkasturia.lockdown;

import java.io.Serializable;
import java.util.List;


public class Profile implements Serializable{

    final public static String TYPE_PIN = "_PIN_", TYPE_PATTERN = "_PATTERN_", TYPE_PASSWORD = "_PASSWORD_";

    private String name = null, type = null;
    private List<String> appList = null;

    private String PIN = null;

    private boolean enabled = true;
    private boolean hideApps = false;
    private boolean notifyUser = false;

    public Profile(String name, String type){
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getAppList() {
        return appList;
    }

    public void setAppList(List<String> appList) {
        this.appList = appList;
    }

    public String getPIN() {
        return PIN;
    }

    public void setPIN(String PIN) {
        this.PIN = PIN;
    }

    public boolean isEnabled(){
        return enabled;
    }

    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }

    public boolean isHideApps() {
        return hideApps;
    }

    public void setHideApps(boolean hideApps) {
        this.hideApps = hideApps;
    }

    public boolean isNotifyUser() {
        return notifyUser;
    }

    public void setNotifyUser(boolean notifyUser) {
        this.notifyUser = notifyUser;
    }
}