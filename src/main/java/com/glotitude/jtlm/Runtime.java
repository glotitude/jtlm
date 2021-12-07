package com.glotitude.jtlm;

import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;

public class Runtime {
    private final LinkedBlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>();
    private final Interpreter interpreter;

    public Runtime(Interpreter interpreter) {
        this.interpreter = interpreter;

        eventQueue.add(new Event("launch", null));
    }

    public void addEvent(Event event) {
        eventQueue.add(event);
    }

    public void run() {
        while (true) {
            try {
                trigger(eventQueue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void trigger(Event event) {
        if (interpreter.eventBinding.get(event.name) == null) return;

        for (TlmCallable callable: interpreter.eventBinding.get(event.name)) {
            callable.call(interpreter, Collections.singletonList(event.payload));
        }
    }
}