# 自定义加密库



加密类需要实现方法如下：

1. Java版本

```java
// randomPassword=0 或 fixedPassword='',需实现
byte[] e(byte[] bytes);
String d(byte[] bytes);

// randomPassword>0&&randomPassword<=32 或 fixedPassword!='',需实现
byte[] e(byte[] bytes, String key);
String d(byte[] bytes, String key);

```

2. Kotlin版本


```kotlin
// randomPassword=0 或 fixedPassword='',需实现
fun e(bytes: ByteArray): ByteArray;
fun d(bytes: ByteArray): String;

// randomPassword>0&&randomPassword<=32 或 fixedPassword!='',需实现
fun e(bytes: ByteArray, key: String): ByteArray;
fun d(bytes: ByteArray, key: String): String;
```



在项目根目录创建`buildSrc`项目，确保`settings.gradle`里面**不会**存在`include ':buildSrc'`。如果有，删除该行。



将你的加密库需要拷贝到`buildSrc`项目中进行依赖，这样插件才能依赖找到加密库进行字节处理。

将你的加密库拷贝一份到你自己需要加密的module中，这样你的项目才能在打包时将加密库一起打包。