package com.example.callblocker.model;

public class BlockedPrefix {
    private String prefix;
    private boolean enabled;

    public BlockedPrefix(String prefix, boolean enabled) {
        this.prefix = prefix;
        this.enabled = enabled;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
