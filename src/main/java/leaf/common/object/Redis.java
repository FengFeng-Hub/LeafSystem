package leaf.common.object;

import leaf.common.Config;
import leaf.common.Log;
import leaf.common.util.DateTime;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;

/**
 * Redis操作类
 * 依赖 jedis-x.x.x.jar commons-pool2-x.x.x.jar
 */
public class Redis {
    public static JedisPool JEDIS_POOL;
    /**
     * 全局 Jedis 对象
     */
    private Jedis jedis;

    /**
     * 无参构造器
     */
    public Redis() {
        jedis = JEDIS_POOL.getResource();
    }

    /** unshift
     * 有参构造器
     * @param index 使用第几号库
     */
    public Redis(int index) {
        jedis = JEDIS_POOL.getResource();
        jedis.select(index);
    }

//    /**
//     * 配置
//     */
//    public static void config() {
//        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
//        jedisPoolConfig.setMaxTotal(Config.parseInteger(Config.Properties.getProperty("common.redis.maxTotal","8"),"common.redis.maxTotal"));
//        jedisPoolConfig.setMaxIdle(Config.parseInteger(Config.Properties.getProperty("common.redis.maxIdle","8"),"common.redis.maxIdle"));
//        jedisPoolConfig.setMinIdle(Config.parseInteger(Config.Properties.getProperty("common.redis.minIdle","0"),"common.redis.minIdle"));
//        try {
//            jedisPoolConfig.setMaxWaitMillis(Long.parseLong(Config.Properties.getProperty("common.redis.MaxWaitMillis","-1")));
//        } catch(Exception e) {
//            Log.write("Error_config","=================== Error ===================\n" +
//                    "Time:"+ DateTime.now("yyyy-MM-dd HH:mm:ss")+"\nMessage:string转long失败！ - common.redis.MaxWaitMillis");
//        }
//        Redis.JEDIS_POOL = new JedisPool(jedisPoolConfig,Config.Properties.getProperty("common.redis.host","127.0.0.1"),
//                Config.parseInteger(Config.Properties.getProperty("common.redis.port",""),"common.redis.port"));
//    }

    /**
     * 配置
     * @param host 主机
     * @param port 端口号
     * @param password 密码
     * @param database Redis数据库索引
     * @param maxActive 连接池最大连接数
     * @param maxIdle 连接池中的最大空闲连接
     * @param minIdle 连接池中的最小空闲连接
     * @param maxWait 连接池最大阻塞等待时间
     */
    public static void config(String host, String port, String password, String database,
                              String maxActive, String maxIdle, String minIdle, String maxWait) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(Config.parseInteger(maxActive, "spring.redis.jedis.pool.max-active"));
        jedisPoolConfig.setMaxIdle(Config.parseInteger(maxIdle, "spring.redis.jedis.pool.max-idle"));
        jedisPoolConfig.setMinIdle(Config.parseInteger(minIdle, "spring.redis.jedis.pool.min-idle"));

        try {
            jedisPoolConfig.setMaxWaitMillis(Long.parseLong(maxWait));
        } catch(Exception e) {
            Log.write("Error_config","=================== Error ===================\n" +
                    "Time:"+ DateTime.now("yyyy-MM-dd HH:mm:ss")+"\nMessage:string转long失败！ - spring.redis.jedis.pool.max-wait");
        }

        Redis.JEDIS_POOL = new JedisPool(host, Config.parseInteger(port, "spring.redis.port"));
    }

    /**
     * 获取Jedis对象
     * @return 全局Jedis对象
     */
    public Jedis getJedis() {
        return jedis;
    }

    /**
     * 清空当前数据库
     * @return 执行信息，成功则返回OK
     */
    public String clearDB() {
        return jedis.flushDB();
    }

    /**
     * 清空全部数据库
     * @return 执行信息，成功则返回OK
     */
    public String clearAll() {
        return jedis.flushAll();
    }

    /**
     * 设置键的过期时间
     * @param key 键
     * @param time 过期时间(s)
     * @return 影响条数
     */
    public long expire(String key,int time) {
        return jedis.expire(key,time);
    }

    /**
     * 设置string键值对
     * @param key 键
     * @param value 值
     * @return 执行信息，成功则返回OK
     */
    public String set(String key,String value) {
        return jedis.set(key,value);
    }

    /**
     * 设置string键值对以及过期时间
     * @param key 键
     * @param time 过期时间(s)
     * @param value 值
     * @return 执行信息，成功则返回OK
     */
    public String set(String key,int time,String value) {
        return jedis.setex(key,time,value);
    }

    /**
     * 设置string键值同时获取旧值
     * @param key 键
     * @param value 值
     * @return 旧值
     */
    public String setAndGetOld(String key,String value) {
        return jedis.getSet(key,value);
    }

    /**
     * 获取对应键的值
     * @param key 键
     * @return 插入信息，成功则返回OK
     */
    public String get(String key) {
        return jedis.get(key);
    }

    /**
     * 判断某个键是否存在
     * @param key 键
     * @return true存在
     */
    public boolean exists(String key) {
        return jedis.exists(key);
    }

    /**
     * 从右边插入一个或多个值
     * @param key 键
     * @param values 值
     * @return 插入条数
     */
    public long rpushList(String key,String ... values) {
        return jedis.rpush(key,values);
    }

    /**
     * 从左边插入一个或多个值
     * @param key 键
     * @param values 值
     * @return 插入条数
     */
    public long lpushList(String key,String ... values) {
        return jedis.lpush(key,values);
    }

    /**
     * 获取列表长度
     * @param key 键
     * @return 插入条数
     */
    public long length(String key) {
        return jedis.llen(key);
    }

    /**
     * 设置map键值对
     * @param key 键
     * @param map map
     * @return 执行信息，成功则返回OK
     */
    public String setMap(String key,Map<String,String> map) {
        return jedis.hmset(key,map);
    }

    /**
     * 删除map的字段
     * @param key 键
     * @param fields 字段
     * @return 删除条数
     */
    public long delMap(String key,String ... fields) {
        return jedis.hdel(key,fields);
    }

    /**
     * 释放jedis
     */
    public void close() {
        if(jedis != null) jedis.close();
    }
}
