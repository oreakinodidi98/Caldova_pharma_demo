package com.caldova.production.model;

import java.util.Locale;

public enum LineStatus {
    RUNNING("Running"), ATTENTION("Attention"), STOPPED("Stopped");

    private final String label;

    LineStatus(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }
    public String getCssClass() { return name().toLowerCase(Locale.ROOT); }
}
