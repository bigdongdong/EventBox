package com.eventbox.cxd.moudle.eventbox;

import android.os.Looper;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;


/*
* 目前存在的问题：
* 1.基础类型中 int 无法识别，但是Integer可以
*
* 2.不建议使用非指向性传输，因为非指向性传输的event是抢占性质的，
* 即有一个后注册的subscriber消费了缓存中的event，
* 则其他后注册的subscriber无法接收到事件
* 强烈建议采用指向性传输，如果要一对多传输，
* 可采用send(Object event ,Class<?>... subscriberClasses)方法
* */
public class EventBox {

    private static String TAG = "EventBox";

    static volatile EventBox instance ;  //默认单例

    //subscriber类中 注解方法查找类
    private SubscriberMethodFinder subscriberMethodFinder = new SubscriberMethodFinder();

    //<eventType，List<Subscription> >
    private  Map<Class<?>, CopyOnWriteArrayList<Subscription>> subscriptionsByEventType = new HashMap<>();

    //<subscriberClass , List<Subscription> >
    private Map<Class<?>,CopyOnWriteArrayList<Subscription>> subscriptionsBySubscriberClass = new HashMap<>();

    //<subscriberClass , List<event> >  未被及时处理的event缓存map，且已经指明订阅者
    private Map<Class<?>, List<Object>> cacheEventsBySubscriberClass = new HashMap<>();

    //<eventType , List<event> >
    // 强制缓存所有非指定性event，但其中的event是抢占性质，
    // 即先到先得，第一个补注册的subscriber可以获得，之后则没有
    private Map<Class<?> , List<Object>> cacheEventsByEventType = new HashMap<>();


    //单例构造器
    public static EventBox getDefault(){
        if (instance == null) {
            synchronized (EventBox.class) {
                if (instance == null) {
                    instance = new EventBox();
                }
            }
        }
        return instance;
    }

    /**
     * 注册
     * @param subscriber 需要注册eventbox的类
     */
    public synchronized void register(Object subscriber) {
        Class<?> subscriberClass = subscriber.getClass();

        //避免重复注册
        if (subscriptionsBySubscriberClass.containsKey(subscriberClass)){
            return;
        }

        //根据这个类的类类型，查找到所有带有注解 @Subscribe 的方法
        List<SubscriberMethod> subscriberMethods = subscriberMethodFinder.findSubscriberMethods(subscriberClass);
        for (SubscriberMethod subscriberMethod : subscriberMethods) {
            subscribe(subscriber, subscriberMethod);
        }

        //检查并发送指向性event
        List<Object> cacheEvents = cacheEventsBySubscriberClass.remove(subscriberClass);
        if(cacheEvents != null){
            for (Object cacheEvent : cacheEvents){
                send(cacheEvent , subscriberClass);
            }
        }

        //检查并发送非指向性event(抢占式)
        for (SubscriberMethod subscriberMethod : subscriberMethods) {
            cacheEvents = cacheEventsByEventType.remove(subscriberMethod.eventType);
            if(cacheEvents != null){
                for (Object cacheEvent : cacheEvents){
                    send(cacheEvent , subscriber.getClass());
                }
            }
        }

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

        //1.subscriptionsBySubscriberClass中添加
        subscriptions  = subscriptionsBySubscriberClass.get(subscriber.getClass());
        if(subscriptions == null){
            subscriptions = new CopyOnWriteArrayList<>();
        }
        subscriptions.add(thisSuscription);
        subscriptionsBySubscriberClass.put(subscriber.getClass(),subscriptions);


        //2.subscriptionsByEventType中添加
        subscriptions = subscriptionsByEventType.get(eventType);
        if(subscriptions == null){
            subscriptions = new CopyOnWriteArrayList<>();
        }
        subscriptions.add(thisSuscription);
        subscriptionsByEventType.put(eventType, subscriptions);

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
     * 发送非指向性event
     *
     * !!!!!!!!!!!!!!!!!!!
     * 不建议使用非指向性传输，因为非指向性传输的event是抢占性质的，
     * 即有一个后注册的subscriber消费了缓存中的event，
     * 则其他后注册的subscriber无法接收到事件
     * 强烈建议采用指向性传输，如果要一对多传输，
     * 可采用send(Object event ,Class<?>... subscriberClasses)方法
     * !!!!!!!!!!!!!!!!!!!
     *
     * @param event
     */
    @Deprecated
    public synchronized void send(Object event) {
        Class<?> eventType = event.getClass();

        //在subscriptionsByEventType中获取eventType对应的subscriptions
        CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(eventType);

        //强制缓存
        List<Object> cacheEvents = cacheEventsByEventType.get(eventType);
        if (cacheEvents == null) {
            cacheEvents = new ArrayList<>();
        }
        cacheEvents.add(event);
        cacheEventsByEventType.put(eventType, cacheEvents);

        //进行延迟处理
        if(subscriptions == null) {
            Log.e(TAG, "未查找到已注册的subscriber" );

        }

        for(Subscription subscription : subscriptions){
            //利用反射调用
            try {
                sendByThread(subscription,event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送指向性event
     *
     * 为保证性能，一般subscriber需要在onStop或onDestory方法中unregister
     * 有目的地传输，发送的都是粘性event，保证之后register的subscriber可以接受到
     *
     * @param event
     * @param subscriberClass
     */
    public synchronized void send(Object event ,Class<?> subscriberClass) {
        Class<?> eventType = event.getClass();
        boolean hasRegisteredSubscriber = false ; //被指定的subscriber是否已经注册

        //在subscriptionsByEventType中获取eventType对应的subscriptions
        CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(eventType);

        if(subscriptions!=null){
            for(Subscription subscription : subscriptions){
               if(subscription.subscriber.getClass().equals(subscriberClass)){
                   hasRegisteredSubscriber = true ;
                   break;
               }else{
                   continue;
               }
            }
        }

       //进行延迟处理
        if(hasRegisteredSubscriber == false) {
            List<Object> cacheEvents = cacheEventsBySubscriberClass.get(subscriberClass);
            if (cacheEvents == null) {
                cacheEvents = new ArrayList<>();
            }
            cacheEvents.add(event);
            cacheEventsBySubscriberClass.put(subscriberClass, cacheEvents);
            return;
        }

        for(Subscription subscription : subscriptions){
            //当subscriberClass不相符时，直接跳跃到下一次循环
            if(!subscription.subscriber.getClass().equals(subscriberClass)){
                continue;
            }else{
                //利用反射调用
                try {
                    sendByThread(subscription,event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return ;
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
     * 根据线程，将event转至对应subscriber的方法里
     * @param subscription
     * @param event
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private void sendByThread(final Subscription subscription, final Object event) throws InvocationTargetException, IllegalAccessException {
        switch (subscription.subscriberMethod.threadMode){
            case DEFAULT:
                subscription.subscriberMethod.method.invoke(subscription.subscriber,event);
                break;
            case MAIN:
                //TODO 主线程问题

                break;
            case NEW_THREAD:
                new Runnable(){
                    @Override
                    public void run() {
                        try {
                            subscription.subscriberMethod.method.invoke(subscription.subscriber,event);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.run();
                break;
        }
    }

    /**
     * 判断当前是否是主线程
     * @return
     */
    public boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }


    /**
     * 手动清空未指定event缓存
     */
    public void clean(){
        cacheEventsByEventType.clear();
    }
}
