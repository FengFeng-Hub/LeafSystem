package leaf.common.object;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 缓存类
 */
public class Cache {
    /**
     * 缓存全局对象
     */
    private static final Map<String, Object> cache = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();//延时任务

    /**
     * 获取指定键所映射的值
     * @param key 键
     * @return 值
     */
    public static Object get(String key) {
        return cache.get(key);
    }

    /**
     * 获取指定键所映射的值并转成string类型
     * @param key 键
     * @return 值
     */
    public static String getString(String key) {
        Object obj = cache.get(key);
        if(obj == null) return null;
        return String.valueOf(obj);
    }

    /**
     * 获取指定键所映射的值并转成map类型
     * @param key 键
     * @return 值
     */
    public static JSONMap getMap(String key) {
        return (JSONMap)cache.get(key);
    }

    /**
     * 将指定的值与此映射中的指定键相关联。 如果映射先前包含键的映射，则旧值将替换为指定的值
     * @param key 键
     * @param value 值
     * @return 与key关联的先前值，如果null没有映射， key
     */
    public static Object put(String key, Object value) {
        return cache.put(key, value);
    }

    /**
     * 将指定的值与此映射中的指定键相关联并设置有效时间。 如果映射先前包含键的映射，则旧值将替换为指定的值
     * @param key 键
     * @param value 值
     * @param duration 有效时间（秒）
     */
    public static void put(String key, Object value, long duration) {
        cache.put(key, value);
        executor.schedule(() -> cache.remove(key), duration, TimeUnit.SECONDS); //在指定时间后删除缓存项
    }

    /**
     * 将指定的值与此映射中的指定键相关联并设置有效时间以及时间单位。 如果映射先前包含键的映射，则旧值将替换为指定的值
     * @param key 键
     * @param value 值
     * @param duration 有效时间
     * @param timeUnit 过期时间单位
     *                 TimeUnit.NANOSECONDS 纳秒
     *                 TimeUnit.MICROSECONDS 微秒
     *                 TimeUnit.MILLISECONDS 毫秒
     *                 TimeUnit.SECONDS 秒
     *                 TimeUnit.MINUTES 分钟
     *                 TimeUnit.HOURS 小时
     *                 TimeUnit.DAYS 天
     */
    public static void put(String key, Object value, long duration, TimeUnit timeUnit) {
        cache.put(key, value);
        executor.schedule(() -> cache.remove(key), duration, timeUnit); //在指定时间后删除缓存项
    }

    /**
     * 如果存在（从可选的操作），从该map中删除一个键的映射
     * @param key 键
     * @return 与 key关联的先前值，如果 null没有映射， key
     */
    public static Object remove(String key) {
        return cache.remove(key);
    }

    /**
     * 从该map中删除所有的映射（可选操作）
     */
    public static void clear() {
        cache.clear();
    }

    /**
     * 返回此map中键值映射的数量
     * @return 此映射中键值映射的数量
     */
    public static int size() {
        return cache.size();
    }

    /**
     * 如果此map不包含键值映射，则返回 true
     * @return true如果此映射不包含键值映射
     */
    public static boolean isEmpty() {
        return cache.isEmpty();
    }

    /**
     * 获取所有的key
     * @return 此映射中包含的键的设置视图
     */
    public static Set keySet() {
        return cache.keySet();
    }

    /**
     * 获取所有的value
     * @return 此映射中包含的值的集合视图
     */
    public static Collection values() {
        return cache.values();
    }

    /**
     * 获取所有的映射关系
     * @return 此映射中包含的映射的set视图
     */
    public static Set<Map.Entry<String, Object>> entrySet() {
        return cache.entrySet();
    }
}
