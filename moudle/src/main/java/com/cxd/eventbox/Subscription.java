package com.cxd.eventbox;

import java.lang.reflect.Method;

/**
 * Subscription类
 */
public final class Subscription {
    final Object subscriber; //在invoke时需要用到
    final Class<?> eventType;
    final Method method;

    public Subscription(Object subscriber, Class<?> eventType, Method method) {
        this.subscriber = subscriber;
        this.eventType = eventType;
        this.method = method;
    }
}