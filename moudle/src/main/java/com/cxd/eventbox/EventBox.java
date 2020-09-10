package com.cxd.eventbox;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/*
* create by cxd on 2019/5/1
* 在上海，和女友住在宾馆里，特别纪念一下
* 当时我临近毕业，她在上财读暑校
* */
public class EventBox {

    public final  static String TAG = "EventBox";

    private static volatile EventBox defaultInstanse ;  //默认单例

    //<subscriberClass , List<Subscription> >
    private Map<Class<?>,List<Subscription>> subscriptionsBySubscriberClass = new HashMap<>();

    //<subscriberClass , List<event> >  未被及时处理的event缓存map，且已经指明订阅者
    private Map<Class<?>, List<Object>> cacheEventsBySubscriberClass = new HashMap<>();


    public static EventBox getDefault(){
        if (defaultInstanse == null) {
            synchronized (EventBox.class) {
                if (defaultInstanse == null) {
                    defaultInstanse = new EventBox();
                }
            }
        }
        return defaultInstanse;
    }

    public synchronized void register(Object subscriber) {
        Class<?> subscriberClass = subscriber.getClass();

        //避免重复注册
        //没有重复注册这个概念   ----2020/9/10
//        if (subscriptionsBySubscriberClass.containsKey(subscriberClass)){
//            return;
//        }

        //根据这个类的类类型，查找到所有带有注解 @Subscribe 的方法
        List<Subscription> subscriptions = SubscriptionFinder.findSubscriberMethods(subscriber);
        if(subscriptions!=null && subscriptions.size()!=0){
            subscriptionsBySubscriberClass.put(subscriberClass,subscriptions);
        }

        //检查并发送指向性event
        List<Object> cacheEvents = cacheEventsBySubscriberClass.remove(subscriberClass);
        if(cacheEvents != null){
            for (Object cacheEvent : cacheEvents){
                send(cacheEvent , subscriberClass);
            }
        }

        Log.i(TAG, "register: "+subscriptionsBySubscriberClass.toString()+"\n");
    }


    public synchronized void unregister(Object subscriber){
        subscriptionsBySubscriberClass.remove(subscriber.getClass());
        cacheEventsBySubscriberClass.remove(subscriber.getClass());

        Log.i(TAG, "unregister: "+subscriptionsBySubscriberClass.toString()+"\n");

    }


    /**
     * 发送指向性event
     *
     * 为保证性能，一般subscriber需要在onStop或onDestory方法中unregister
     * 有目的地传输，发送的都是粘性event，保证之后register的subscriber可以接受到
     *
     * @param event
     * @param subscriberClass
     *
     */
    public synchronized void send(Object event , Class<?> subscriberClass) {
        List<Subscription> subscriptions = subscriptionsBySubscriberClass.get(subscriberClass);
        if(subscriptions == null || subscriptions.size()==0){
            //走到这一步，说明有对应类型的event被注册，但是对象subscriber中却没有
            // 所以进行粘性处理
            List<Object> cacheEvents = cacheEventsBySubscriberClass.get(subscriberClass);
            if (cacheEvents == null) {
                cacheEvents = new ArrayList<>();
            }
            cacheEvents.add(event); //将cacheEvent加入到list中
            cacheEventsBySubscriberClass.put(subscriberClass, cacheEvents);
            return;
        }

        //注册过的，检查event的类型，判断目的subscriber中是否有此类型
        Class<?> eventType = event.getClass();
        for(Subscription subscription : subscriptions){
            if(eventType == subscription.eventType){
                //找到同类型，发射
                sendEvent(subscription,event);
                return;
            }
        }
    }


    /**
     * 发送指向性event，并指定某些个类接受
     * @param event
     * @param subscriberClasses
     */
    public void send(Object event ,Class<?>... subscriberClasses){
        for(Class<?> subscriberClass : subscriberClasses){
            send(event,subscriberClass);
        }
    }

    /**
     * 进行event的发送
     * @param subscription
     * @param event
     * @throws InvocationTargetException
     */
    private void sendEvent(final Subscription subscription, final Object event) {

        try {

            subscription.method.invoke(subscription.subscriber,event);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
