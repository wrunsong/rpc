package lilac.rpcframework.factory;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 获取单例对象的工厂类
 */
@Slf4j
public class SingletonFactory {
    // hungry mode

    // class name -> class instance
    private static final Map<String, Object> SINGLETON_MAP = new ConcurrentHashMap<>();
    private static final Object LOCK = new Object();

    private SingletonFactory() {}


    public static <T> T getInstance(Class<T> clazz) {
        if (clazz == null) {
            log.error("clazz is null");
            return null;
        }

        String className = clazz.toString();
        if (SINGLETON_MAP.containsKey(className)) {
            return clazz.cast(SINGLETON_MAP.get(className));
        }

        synchronized (LOCK) {
            if (SINGLETON_MAP.containsKey(className)) {
                return clazz.cast(SINGLETON_MAP.get(className));
            }
            try {
                T instance = clazz.getConstructor().newInstance();
                SINGLETON_MAP.put(className, instance);
                return instance;
            } catch (Exception e) {
                log.error("Construct {} error: {}", clazz, e.getMessage());
                return null;
            }
        }
    }
}
