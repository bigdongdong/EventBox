package moudle;


import java.lang.reflect.Method;

public class SubscriberMethod {
    final Method method;  //@Subscribe 注解对应的方法信息
    final Class<?> eventType;  //方法的参数类型
    final ThreadMode threadMode ; //处理线程

    public SubscriberMethod(Method method, Class<?> eventType, ThreadMode threadMode) {
        this.method = method;
        this.eventType = eventType;
        this.threadMode = threadMode;
    }
}
