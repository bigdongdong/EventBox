package com.eventbox.cxd.moudle.eventbox;

import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;


/*
* 目前存在的问题：
* 1.基础类型中 int 无法识别，但是Integer可以
* 其他常用类型：String 、String[] 都可以
*
* 2.目前的无目的传输，不是粘性event，所以不建议使用
* */
public class EventBox {

    private static String TAG = "EventBox";

    static volatile EventBox defaultInstance;  //默认单例


    //subscriber类中 注解方法查找类
    private SubscriberMethodFinder subscriberMethodFinder = new SubscriberMethodFinder();

    //<事件类型，该事件对应的所有subscription> 的map
    private  Map<Class<?>, CopyOnWriteArrayList<Subscription>> subscriptionsByEventType = new HashMap<>();

    //注册的subscriberClass
    private Map<Class<?>,CopyOnWriteArrayList<Subscription>> subscriptionsBySubscriberClass = new HashMap<>();

    //未被及时处理的event缓存map，且已经指明订阅者  <subscriberClass , List<event> >
    private Map<Class<?>, List<Object>> cacheEventsBySubscriberClass = new HashMap<>();

    public static EventBox getDefault(){
        if (defaultInstance == null) {
            synchronized (EventBox.class) {
                if (defaultInstance == null) {
                    defaultInstance = new EventBox();
                }
            }
        }
        return defaultInstance;
    }

    /**
     * 注册
     * @param subscriber 需要注册eventbox的类
     */
    public void register(Object subscriber) {
        Class<?> subscriberClass = subscriber.getClass();  //获取注册类的类类型

        //避免重复注册
        if (subscriptionsBySubscriberClass.containsKey(subscriberClass)){
            return;
        }

        //根据这个类的类类型，查找到所有带有注解 @Subscribe 的方法
        List<SubscriberMethod> subscriberMethods = subscriberMethodFinder.findSubscriberMethods(subscriberClass);
        synchronized (this) {
            for (SubscriberMethod subscriberMethod : subscriberMethods) {
                subscribe(subscriber, subscriberMethod);
            }
        }

        //检查cacheEvents中是否存在指定给该subscriber的cacheEvent
        List<Object> cacheEvents = cacheEventsBySubscriberClass.remove(subscriberClass);

        if(cacheEvents == null){
            return;
        }
        //遍历，将该类的events发送掉
        for (Object cacheEvent : cacheEvents){
            send(subscriberClass,cacheEvent);
        }
    }

    /**
     * 注销subsciber
     * @param subscriber
     */
    public synchronized void unregister(Object subscriber){
        //得到的subscriptions都需要在subscriptionsByEventType中移除
        CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsBySubscriberClass.remove(subscriber.getClass());

        //从map中剔除subscription
        Set<Map.Entry<Class<?>,CopyOnWriteArrayList<Subscription>>> entries = subscriptionsByEventType.entrySet();
        for (Map.Entry<Class<?>, CopyOnWriteArrayList<Subscription>> entry : entries) {
            CopyOnWriteArrayList<Subscription> subscriptions2 = entry.getValue();

            for(Subscription subscription : subscriptions2){
                if(subscriptions.contains(subscription)){

                    //这里只移除了subscriptionsByEventType中某一个eventType的某一个subscription
                    //即使subscriptions2中全部被移除，map的长度也不会降低
                    subscriptions2.remove(subscription);
                }
            }
        }

//        TODO 从map中剔除subscription后，还需要剔除map中value已经为空的键对值
//        TODO 但是对subscriptionsByEventType的操作会报错，日后再修改  ----2019_6_5
//        Iterator<Map.Entry<Class<?>, CopyOnWriteArrayList<Subscription>>>  it
//                                                  = subscriptionsByEventType.entrySet().iterator();
//
//        synchronized (subscriptionsByEventType){
//            while(it.hasNext()){
//                Map.Entry<Class<?>, CopyOnWriteArrayList<Subscription>> entry = it.next();
//                CopyOnWriteArrayList<Subscription> subscriptions3 = entry.getValue();
//                if(subscriptions3 == null || subscriptions3.size() == 0){
//                    //这里整理map，去除空value
//                    subscriptionsByEventType.remove(entry.getKey());
//                }
//            }
//        }

//        Log.i(TAG, "unRegister: subscriptionsBySubscriberClass："+subscriptionsBySubscriberClass.size());
//        Log.i(TAG, "unRegister: subscriptionsByEventType："+subscriptionsByEventType.size());
    }

    /**
     * 进行订阅
     * @param subscriber
     * @param subscriberMethod
     */
    private void subscribe(Object subscriber, SubscriberMethod subscriberMethod) {
        Class<?> eventType = subscriberMethod.eventType;
        Subscription thisSuscription = new Subscription(subscriber, subscriberMethod);


        CopyOnWriteArrayList<Subscription> subscriptions ;

        //1.先进行注册记录list添加
        subscriptions  = subscriptionsBySubscriberClass.get(subscriber.getClass());
        if(subscriptions == null){
            subscriptions = new CopyOnWriteArrayList<>();
        }
        subscriptions.add(thisSuscription);
        subscriptionsBySubscriberClass.put(subscriber.getClass(),subscriptions);


        //2.再进行查找记录list添加
        subscriptions = subscriptionsByEventType.get(eventType);
        if(subscriptions == null){
            subscriptions = new CopyOnWriteArrayList<>();
        }
        subscriptions.add(thisSuscription);
        subscriptionsByEventType.put(eventType, subscriptions);

    }

    /**
     * 发送需要的内容，该方法不筛选订阅者
     *
     * !!!!!!!!!!!!!
     * 不建议使用该方法，无目的传输所发送的event都不是粘性event
     * 如果unregister了subscriber，则无法接收到无目的消息
     * 无目的的传输类似EventBus中的post
     * 但是EventBus中对所有event进行了缓存
     *
     * 该方法对比EventBus中的post方法有如下缺点：
     * 1.没有建设缓存，在unregister的subscriber中无法接受该方法发出的event
     *
     * !!!!!!!!!!!!!
     *
     * @param event
     */
    @Deprecated
    public synchronized void send(Object event) {
        Class<?> eventType = event.getClass();

        //在subscriptionsByEventType中获取eventType对应的subscriptions
        CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(eventType);

        //进行延迟处理
        if(subscriptions == null) {
            Log.e(TAG, "未查找到已注册的subscriber" );
            return;
        }

        for(Subscription subscription : subscriptions){
            try {
                //利用反射调用
                subscription.subscriberMethod.method.invoke(subscription.subscriber,event);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送需要的内容，可以定向发送至某个订阅者
     *
     * 为保证性能，一般subscriber需要在onStop或onDestory方法中unregister
     * 有目的地传输，发送的都是粘性event，保证之后register的subscriber可以接受到
     *
     * 该方法对比EventBus中的post方法有如下有点：
     * 1.可以定向发送event，并且event都是粘性事件
     *
     * @param subscriberClass
     * @param event
     */
    public synchronized void send(Class<?> subscriberClass ,Object event) {
        Class<?> eventType = event.getClass();
        boolean hasSubscriberRegistered = false ; //被指定的subscriber是否已经注册

        //在subscriptionsByEventType中获取eventType对应的subscriptions
        CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(eventType);

        if(subscriptions!=null){
            for(Subscription subscription : subscriptions){
               if(subscription.subscriber.getClass().equals(subscriberClass)){
                   hasSubscriberRegistered = true ;
                   break;
               }else{
                   continue;
               }
            }
        }

       //进行延迟处理
        if(hasSubscriberRegistered == false) {
//            Log.e(TAG, "未查找到已注册的subscriber，已将event存入缓存" );
            List<Object> cacheEvents = cacheEventsBySubscriberClass.get(subscriberClass);
            if (cacheEvents == null) {
                cacheEvents = new ArrayList<>();
            }
            cacheEvents.add(event);
            cacheEventsBySubscriberClass.put(subscriberClass, cacheEvents);
            return;
        }

        for(Subscription subscription : subscriptions){
            //当subscriberClass不为空，并且subscriberClass不相符时，直接跳跃到下一次循环
            if(subscriberClass!=null && !subscription.subscriber.getClass().equals(subscriberClass)){
                continue;
            }else{
                try {
                    //利用反射调用
                    subscription.subscriberMethod.method.invoke(subscription.subscriber,event);
                    return ;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
