package com.eventbox.cxd.moudle.eventbox;



final class Subscription {
    final Object subscriber;
    final SubscriberMethod subscriberMethod;

    Subscription(Object subscriber, SubscriberMethod subscriberMethod) {
        this.subscriber = subscriber;
        this.subscriberMethod = subscriberMethod;
    }

}