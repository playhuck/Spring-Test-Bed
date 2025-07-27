package com.side.springtestbed.utils.event;

import java.util.List;

public interface EventListenerResolver {
    List<EventListener<? extends Event>> resolveListeners();
}

