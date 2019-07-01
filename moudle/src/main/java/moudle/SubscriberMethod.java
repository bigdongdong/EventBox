package moudle;


import java.lang.reflect.Method;

public class SubscriberMethod {
    final Method method;  //@Subscribe 注解对应的方法信息
    final Class<?> eventType;  //方法的参数类型

    public SubscriberMethod(Method method, Class<?> eventType) {
        this.method = method;
        this.eventType = eventType;
    }
}
