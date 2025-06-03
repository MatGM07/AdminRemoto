package com.admin.remoto.services.logger;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogEntry {
    private String timestamp;
    private String type;
    private String message;

    public LogEntry(String type, String message) {
        this.timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());
        this.type = type;
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}