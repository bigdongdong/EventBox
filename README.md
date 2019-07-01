# EventBox
Android  事件盒子，指向性传递event ，一款比EventBus 更好用的Android事件总线框架！

# 截图预览
<img  width = "350" src = "https://github.com/bigdongdong/EventBox/blob/master/preview/screen.jpg"></img>
<img  width = "350" src = "https://github.com/bigdongdong/EventBox/blob/master/preview/1.gif"></img></br>
<img  width = "350" src = "https://github.com/bigdongdong/EventBox/blob/master/preview/2.gif"></img>
<img  width = "350" src = "https://github.com/bigdongdong/EventBox/blob/master/preview/both.gif"></img></br>

# 项目配置

```
  allprojects {
      repositories {
          ...
          maven { url 'https://jitpack.io' }  //添加jitpack仓库
      }
  }
  
  dependencies {
	  implementation 'com.github.bigdongdong:EventBox:1.0' //添加依赖
  }
```

# 使用说明

**1.在Activity或Fragment中注册和注销EventBox**
```java
    @Override
    public void onStart() {
        super.onStart();
        EventBox.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBox.getDefault().unregister(this);
    }
```

**2.添加event的订阅方法**  
通过@Subscribe来注解方法，使该方法能被EventBox检测到，添加进subscription池

```java
    @Subscribe
    public void getData(String s){
        //do something ...
        
    }
```



**3.在需要的地方发送event，并指明接受者**
```java
  EventBox.getDefault().send("爱你一万年", MainActivity.class);
```
</br></br></br>
# 特性说明：
**1.黏性事件：  
如果你发送了event给予一个尚未启动的sbscriber（activity or fragment)，则该event将被自动添加到EventBox的event缓存池中，直到目的activity启动时消费该event  
2.多发模式：  
鉴于EventBus的广播模式，EventBox可以采取指向性的一对多模式，使用时只需在send()方法中添加目的类.class，例如：**
```java
  EventBox.getDefault().send("爱你们一万年", FirstActivity.class ,SecondActivity.class,ThirdActivity.class...);
```
**3.非指向性事件：  
不推荐的方法，send只有一个参数：event时，将默认发送给所有符合条件的sbscriber,并且不做粘性事件处理，后注册的suscriber将无法收到它注册之前发送的event**



</br></br></br>
# 注意事项：  
**同一类中可以有多个带@Subscribe的方法  
方法的名称可以自定义  
方法仅可有一个参数，且同一类中所有带@Subscribe的方法参数类型唯一  
当event类型为int,float,double时，在接受方法中需指定参数类型为包装类：Integer,Float,Double  
用户需要自己处理线程问题，推荐使用 rxjava2**


