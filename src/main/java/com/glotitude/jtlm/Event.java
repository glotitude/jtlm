package com.glotitude.jtlm;

public class Event {
    public final String name;
    public final Object payload;

    public Event(String name, Object payload) {
        this.name = name;
        this.payload = payload;
    }
}
