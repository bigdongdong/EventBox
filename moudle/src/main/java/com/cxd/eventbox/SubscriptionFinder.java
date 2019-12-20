package com.cxd.eventbox;

import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.cxd.eventbox.EventBox.TAG;


/**
 * 带有EventBoxSubscribe注解的类的查找工具类
 */
class SubscriptionFinder {

    public static List<Subscription> findSubscriberMethods(Object subscriber) {
        Class<?> subscriberClass = subscriber.getClass();
        List<Subscription> subscriberMethods = new ArrayList<>();
        
        EventBoxSubscribe subscribeAnno ;
        Method[] methods = subscriberClass.getMethods();

        for(Method method : methods){
            subscribeAnno = method.getAnnotation(EventBoxSubscribe.class);
            if(subscribeAnno == null){
                continue;
            }

            if(method.getParameterTypes().length == 1){
                subscriberMethods.add(new Subscription(subscriber,method.getParameterTypes()[0],method)) ;
            }else{
                Log.e(TAG, method.getName()+"()方法必须且仅拥有一个参数！");
            }
        }
        return subscriberMethods ;
    }
}

