package moudle;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */

class SubscriberMethodFinder {

    /**
     * 这是一个存放  <注册类的类类型，该类所有带注解方法> 的Map
     */
    private static final Map<Class<?>, List<SubscriberMethod>> methodsBySubscriberClass_Map = new ConcurrentHashMap<>();

    /**
     * 根据注册了eventbox的类的类类型，查找到所有该类带有@Subscribe注解的方法实体
     * @param subscriberClass
     * @return
     */
    List<SubscriberMethod> findSubscriberMethods(Class<?> subscriberClass) {
        List<SubscriberMethod> subscriberMethods = methodsBySubscriberClass_Map.get(subscriberClass);

        //如果有内容，则直接返回内容
        if (subscriberMethods != null) {
            return subscriberMethods;
        }else{
            subscriberMethods = new ArrayList<>();
        }

        //没有内容，则通过java反射进行查找
        Subscribe subscribeAnno ;
        Method[] methods = subscriberClass.getMethods();
        for(Method method : methods){
            subscribeAnno = method.getAnnotation(Subscribe.class);
            if(subscribeAnno != null && method.getParameterTypes().length == 1){
                subscriberMethods.add(new SubscriberMethod(method,method.getParameterTypes()[0],subscribeAnno.threadMode())) ;
            }
        }
        return subscriberMethods ;
    }
}

