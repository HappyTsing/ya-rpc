package com.wang.extension;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author happytsing
 * @Description Java 原生的 SPI 机制在查找实现类的时候，需要遍历配置文件中定义的所有实现类，而这个过程会把所有实现类都实例化。
 *              <p>一个接口如果有很多实现类，而我们只需要其中一个的时候，就会产生其他不必要的实现类。
 *              <p>因此此处对原生 SPI 进行增强，实现懒惰加载。
 * @Usage {@code ExtensionLoader<Serializer> extensionLoader = ExtensionLoader.getExtensionLoader(Serializer.class);}
 *        <p>{@code Serializer serializer = extensionLoader.getExtension("protostuff");}
 *
 * @concept
 *      扩展接口：为了实现功能的可扩展，例如
 *                                    <p>1. 序列化方式，可以选择 protostuff、kryo 或 hessian
 *                                    <p>2. 注册中心，可以选择 zookeeper 或 eureka
 *               <p>使用 SPI 机制即可让开发者快速提供新的实现方式，无需修改框架源码。
 *               <p>于是需要定义一个扩展接口，例如序列化接口 Serializer，当想提供新的序列化方式时，只需要提供该接口的实现类即可。
 *       <p>扩展类：扩展的接口此处称为扩展类，例如： {@code public class ProtostuffSerializer implements Serializer{...}}
 * @reference
 *      <a href="https://dubbo.apache.org/zh/docsv2.7/dev/source/dubbo-spi/">dubbo-spi</a>
 *      <a href="https://github.com/apache/dubbo/blob/3.1/dubbo-common/src/main/java/org/apache/dubbo/common/extension/ExtensionLoader.java">dubbo-source-code</a>
 *      <a href="https://www.cnblogs.com/chenchuxin/p/15143771.html">ccx-rpc</a>
 */
@Slf4j
public class ExtensionLoader<T> {
    /**
     * 首先通过 getExtensionLoader 方法获取一个 ExtensionLoader 实例
     * 然后通过 getExtension获取扩展类对象。
     */


    private static final String SERVICE_DIRECTORY = "META-INF/extensions/";

    /**
     * ExtensionLoader 实例
     */
    private static final Map<Class<?>, ExtensionLoader> EXTENSION_LOADERS = new ConcurrentHashMap<>();

    /**
     * Extension 扩展类实例
     */
    private static final Map<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();


    /**
     * 扩展接口，例如扩展序列化时，则传入 Serializer.class
     * Class 类是泛型的，因此 Serializer.class 实际上是一个 Class<Serializer> 的对象。
     */
    private final Class<?> type;

    /**
     * 持有者
     */
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    /**
     * 扩展类示例的缓存
     */
    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();



    private ExtensionLoader(Class<?> type){
        this.type = type;
    }

    public static <S> ExtensionLoader<S> getExtensionLoader(Class<S> type){
        if (type == null) {
            throw new IllegalArgumentException("Extension type should not be null.");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type must be an interface.");
        }
        if (type.getAnnotation(SPI.class) == null) {
            throw new IllegalArgumentException("Extension type must be annotated by @SPI");
        }
        /**
         * firstly get from cache, if not hit, create one
         * ExtensionLoader<T> 是一个泛型类，实例化时指定类型参数，此处传入 new ExtensionLoader<S>
         * 当然方法 getExtensionLoader(Class<S> type) 是一个泛型方法，其
         */
        ExtensionLoader<S> extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        if (extensionLoader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<S>(type));
            extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        }
        return extensionLoader;
    }

    /**
     * 获取扩展类
     * @param name SERVICE_DIRECTORY 文件夹下配置文件的 key，其对应的 value 是扩展类的文件位置。
     *             例如配置文件内容: protostuff=com.wang.serialize.protostuff.ProtostuffSerializer
     *             此时 name = protostuff
     * @return 扩展类实例
     *
     */
    public T getExtension(String name) {
        if(name == null || name.length() == 0){
            throw new IllegalArgumentException("Extension name should not be null or empty.");
        }

//        if ("true".equals(name)) {
//            // 获取默认的拓展实现类
//            return getDefaultExtension();
//        }

        // 首先检查扩展类实例缓存
        // Holder，顾名思义，用于持有目标对象
        Holder<Object> holder = cachedInstances.get(name);

        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }
        Object instance = holder.get();
        // 缓存未命中则创建扩展类对象
        // double-check，懒汉单例实现，当程序需要某个实现类的时候，才会去真正初始化它
        if (instance == null) {
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                    // 创建扩展类对象实例
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }
        return (T) instance;
    }

    /**
     * 通过传入的扩展类实例 name，构建扩展类
     * @param name SERVICE_DIRECTORY 文件夹下配置文件的 key，其对应的 value 是扩展类的文件位置
     * @return 扩展类实例
     */
    private T createExtension(String name)  {
        // load all extension classes of type T from file and get specific one by name
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw new RuntimeException("No such extension of name " + name);
        }
        T instance = (T) EXTENSION_INSTANCES.get(clazz);
        try{
            if (instance == null) {
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.getDeclaredConstructor().newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return instance;
    }

    /**
     * 获取当前扩展接口的所有类实例
     */
    private Map<String, Class<?>> getExtensionClasses() {
        // get the loaded extension class from the cache
        Map<String, Class<?>> classes = cachedClasses.get();
        // double check
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = new HashMap<>();
                    // load all extensions from our extensions directory
                    loadDirectory(classes);
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    private void loadDirectory(Map<String, Class<?>> extensionClasses) {
        // 当前扩展接口的配置文件的位置，例如 Serializer 扩展接口的配置文件位置为 META-INF/extensions/com.wang.serialize.Serializer
        String fileName = ExtensionLoader.SERVICE_DIRECTORY + type.getName();
        try {
            Enumeration<URL> urls;
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            urls = classLoader.getResources(fileName);
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL resourceUrl = urls.nextElement();
                    loadResource(extensionClasses, classLoader, resourceUrl);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 将配置文件中定义的扩展类都实例化，存储到 extensionClasses 中，其 key 为 name，value 为类实例。{protostuff:clazz}
     */
    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, URL resourceUrl) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), UTF_8))) {
            String line;
            // read every line
            while ((line = reader.readLine()) != null) {
                // get index of comment
                final int ci = line.indexOf('#');
                if (ci >= 0) {
                    // string after # is comment so we ignore it
                    line = line.substring(0, ci);
                }
                line = line.trim();
                if (line.length() > 0) {
                    try {
                        final int ei = line.indexOf('=');
                        String name = line.substring(0, ei).trim();
                        String clazzName = line.substring(ei + 1).trim();
                        // our SPI use key-value pair so both of them must not be empty
                        if (name.length() > 0 && clazzName.length() > 0) {
                            Class<?> clazz = classLoader.loadClass(clazzName);
                            log.info(name);
                            extensionClasses.put(name, clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        log.error(e.getMessage());
                    }
                }

            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}