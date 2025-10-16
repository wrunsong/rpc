package lilac.rpcframework.extension;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ExtensionLoader<T> {

    // 该目录下的文件存放的是各个支持SPI机制的类的配置文件，路径是相对resources目录的路径
    private static final String EXTENSION_DIRECTORY = "extensions";
    // interface -> extension_loader
    private static final Map<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();
    // class which implemented interface -> object of class
    private static final Map<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();

    // 该ExtensionLoader管理的接口类型
    private final Class<?> type;

    // class name -> class object
    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();
    // class name -> class
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    private ExtensionLoader(Class<?> type) {
        this.type = type;
    }

    public static <S> ExtensionLoader<S> getExtensionLoader(Class<S> type) {
        // initialize extensionLoader
        if (type == null) {
            log.error("Extension type can not be null.");
            return null;
        }
        if (!type.isInterface()) {
            log.error("Extension type must be interface.");
            return null;
        }
        if (type.getAnnotation(SPI.class) == null) {
            log.error("Extension type must have SPI annotation");
            return null;
        }
        // firstly get from cache, if not hit, create one
        ExtensionLoader<S> extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        if (extensionLoader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<S>(type));
            extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        }
        return extensionLoader;
    }

    /**
     * 获取相应的对象
     * @param name 在extensions的配置文件中，定义的键值对的key e.g. name = zk
     * @return
     */
    public T getExtension(String name) {
        if (name == null || name.trim().isEmpty()) {
            log.error("Extension name can not be empty.");
            return null;
        }
        // firstly get from cache, if not hit, create one
        Holder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }
        // create a singleton if no instance exists
        Object instance = holder.getValue();
        if (instance == null) {
            synchronized (holder) {
                if (instance == null) {
                    instance = createInstance(name);
                    holder.setValue(instance);
                }
            }
        }

        return (T) instance;
    }

    /**
     * 创建对象实例，能进到这个方法的只会有一个线程
     * @param name
     * @return
     */
    private T createInstance(String name) {
        Class<?> clazz = getClassesMap().get(name);

        if (clazz == null) {
            log.error("Class of {} is not found.", name);
            return null;
        }

        T instance = (T) EXTENSION_INSTANCES.get(clazz);
        if (instance == null) {
            // 不用再加锁了，方法外面加过了
            try {
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.getConstructor().newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            } catch (Exception e) {
                log.error("Failed to instantiate {}. Error message: {}", name, e.getMessage());
            }
        }
        return instance;
    }

    /**
     *
     * @return className -> class
     */
    private Map<String, Class<?>> getClassesMap(){
        Map<String, Class<?>> classNameToClazzMap = cachedClasses.getValue();
        if (classNameToClazzMap == null) {
            synchronized (cachedClasses) {
                classNameToClazzMap = cachedClasses.getValue();
                if (classNameToClazzMap == null) {
                    classNameToClazzMap = new ConcurrentHashMap<>();
                    loadDirectory(classNameToClazzMap);
                    cachedClasses.setValue(classNameToClazzMap);
                }
            }
        }
        return classNameToClazzMap;
    }

    /**
     * 利用类加载器，找到所有该接口（type）的SPI配置文件
     * @param classNameToClazzMap className不是类的限定名，而是在extensions配置文件中自己取的名字，也就是键值对的key
     */
    private void loadDirectory(Map<String, Class<?>> classNameToClazzMap){
        String filePath = EXTENSION_DIRECTORY + File.separator + type.getName();
        try {
            // 使用ClassLoader来加载资源，因为这样可以跨jar包，只要都在同一个resources路径下被同一个类加载器加载，就都能找到
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            Enumeration<URL> urls = classLoader.getResources(filePath);
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    loadResource(classNameToClazzMap, classLoader, url);
                }
            }
        } catch (Exception e) {
            log.error("Failed to load extension directory {}. Error message: {}", filePath, e.getMessage());
        }

    }

    /**
     * 将该接口对应的SPI配置文件中配置的所有类都缓存进来
     * @param classNameToClazzMap
     * @param classLoader
     * @param resourceUrl
     */
    private void loadResource(Map<String, Class<?>> classNameToClazzMap, ClassLoader classLoader, URL resourceUrl){
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), StandardCharsets.UTF_8))) {
            String line;
            // read every line
            while ((line = reader.readLine()) != null) {
                // delete comment
                final int commentIndex = line.indexOf('#');
                if (commentIndex >= 0) {
                    // string after # is comment so we ignore it
                    line = line.substring(0, commentIndex);
                }
                line = line.trim();
                if (!line.isEmpty()) {
                    try {
                        final int equalIndex = line.indexOf('=');
                        String key = line.substring(0, equalIndex).trim();
                        String value = line.substring(equalIndex + 1).trim();
                        if (!key.isEmpty() && !value.isEmpty()) {
                            Class<?> clazz = classLoader.loadClass(value);
                            classNameToClazzMap.put(key, clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        log.error("Failed to find class. Error message: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to load resource {}. Error message: {}", resourceUrl, e.getMessage());
        }
    }

}
