package com.side.springtestbed.utils.event;

import java.util.*;

public class EventPublisher {
    private Map<Class<? extends Event>, EventListener<? extends Event>> eventListenerMap;

    public static EventPublisher newInstance(EventListenerResolver eventListenerResolver) {
        List<? extends EventListener<? extends Event>> eventListeners = eventListenerResolver != null ? eventListenerResolver.resolveListeners() : null;
        return eventListeners != null && !eventListeners.isEmpty() ? new EventPublisher(eventListeners) : new EventPublisher();
    }

    public EventPublisher(List<? extends EventListener<? extends Event>> eventListeners) {
        this.eventListenerMap = new HashMap();
        Iterator var2 = eventListeners.iterator();

        while(var2.hasNext()) {
            EventListener<? extends Event> eventListener = (EventListener)var2.next();
            Class<? extends Event> eventClass = eventListener.listensTo();
            this.eventListenerMap.put(eventClass, eventListener);
        }

    }

    protected EventPublisher() {
        this(new ArrayList(0));
    }

    public <E extends Event> void publish(E event) {
        if (!this.eventListenerMap.isEmpty()) {
            EventListener<E> eventListener = this.getEventListener(event);
            if (eventListener != null) {
                eventListener.on(event);
            }
        }

    }

    private <E extends Event> EventListener<E> getEventListener(E event) {
        EventListener<E> eventListener = (EventListener)this.eventListenerMap.get(event.getClass());
        return eventListener;
    }
}
