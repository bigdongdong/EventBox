package moudle;


/**
 * Subscription类，包含一个subscriber和一个subscriberMethod，是单个event发送的最小目标
 */
final class Subscription {
    final Object subscriber;
    final SubscriberMethod subscriberMethod;

    Subscription(Object subscriber, SubscriberMethod subscriberMethod) {
        this.subscriber = subscriber;
        this.subscriberMethod = subscriberMethod;
    }

}