package com.caldova.production.service;

public class LineNotFoundException extends RuntimeException {
    public LineNotFoundException() { super("The requested production line was not found."); }
}
