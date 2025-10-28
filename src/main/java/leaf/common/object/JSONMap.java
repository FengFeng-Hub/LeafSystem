package leaf.common.object;

import java.util.*;

/**
 * JSONMap类
 * 实现 Map 类
 */
public class JSONMap implements Map<String, Object> {
    /**
     * Map
     */
    private Map<String, Object> map;

    /**
     * 构造器，用来初始化私有map，默认HashMap
     */
    public JSONMap() { this.map = new HashMap<>(); }

    /**
     * 有参构造器，用来初始化私有map
     */
    public JSONMap(Map<String, Object> map) { this.map = map; }

    /**
     * Map转JSONMap（递归）
     * @param map 需要转换的Map
     * @return 转换后的JSONMap
     */
    public static JSONMap toJSONMap(Map<String,Object> map) {
        JSONMap jsonMap = new JSONMap();
        JSONMap jsonMapSon;
        JSONList jsonListSon;
        for (Entry<String,Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if(value.getClass().getName().contains("Map")) {
                jsonMapSon = JSONMap.toJSONMap((Map<String, Object>) value);
                jsonMap.put(entry.getKey(),jsonMapSon);
                continue;
            }
            if(value.getClass().getName().contains("List")) {
                jsonListSon = JSONList.toJSONList((List) value);
                jsonMap.put(entry.getKey(),jsonListSon);
                continue;
            }
            jsonMap.put(entry.getKey(),value);
        }
        return jsonMap;
    }

    /**
     * JSONMap转Map
     * @return 转换后的Map类型的值
     */
    public Map<String,Object> toMap() { return this.map; }

    /**
     * 成功响应
     * @return JSONMap类型
     * {
     *      "IsSuccess": "1"//是否调用成功 0:否;1:是
     * }
     */
    public static JSONMap success() {
        JSONMap jsonMap = new JSONMap();
        jsonMap.put("IsSuccess","1");
        return jsonMap;
    }

    /**
     * 成功响应
     * @param data 响应数据
     * @return JSONMap类型
     * {
     *      "IsSuccess": "1",//是否调用成功 0:否;1:是
     *      "data": [响应数据]
     * }
     */
    public static JSONMap success(Object data) {
        JSONMap jsonMap = new JSONMap();
        jsonMap.put("IsSuccess","1");
        jsonMap.put("data",data);
        return jsonMap;
    }

    /**
     * 成功响应
     * @param data 响应数据
     * @param msg 信息
     * @return JSONMap类型
     * {
     *      "IsSuccess": "1",//是否调用成功 0:否;1:是
     *      "data": [响应数据]
     * }
     */
    public static JSONMap success(Object data,String msg) {
        JSONMap jsonMap = new JSONMap();
        jsonMap.put("IsSuccess","1");
        if(data != null)
            jsonMap.put("data",data);
        if(msg != null)
            jsonMap.put("Msg",msg);
        return jsonMap;
    }

    /**
     * 成功响应
     * @param data 响应数据
     * @param dataCount 数据数量
     * @param msg 信息
     * @return JSONMap类型
     * {
     *      "IsSuccess": "1",//是否调用成功 0:否;1:是
     *      "data": [响应数据],
     *      "dataCount": "数据数量",
     *      "Msg": "响应信息"
     * }
     */
    public static JSONMap success(Object data,String dataCount,String msg) {
        JSONMap jsonMap = new JSONMap();
        jsonMap.put("IsSuccess","1");
        if(data != null)
            jsonMap.put("data",data);
        if(dataCount != null)
            jsonMap.put("dataCount",dataCount);
        if(msg != null)
            jsonMap.put("Msg",msg);
        return jsonMap;
    }

    /**
     * 失败响应
     * @return JSONMap类型
     * {
     *     "IsSuccess": "0"//是否调用成功 0:否;1:是
     * }
     */
    public static JSONMap error() {
        JSONMap jsonMap = new JSONMap();
        jsonMap.put("IsSuccess","0");
        return jsonMap;
    }

    /**
     * 失败响应
     * @param msg 信息
     * @return JSONMap类型
     * {
     *     "IsSuccess": "0",//是否调用成功 0:否;1:是
     *     "Msg": "响应信息"
     * }
     */
    public static JSONMap error(String msg) {
        JSONMap jsonMap = new JSONMap();
        jsonMap.put("IsSuccess","0");
        jsonMap.put("Msg",msg);
        return jsonMap;
    }

    /**
     * 失败响应
     * @param msg 信息
     * @param msgCode 信息代码
     * @return JSONMap类型
     * {
     *     "IsSuccess": "0",//是否调用成功 0:否;1:是
     *     "Msg": "响应信息",
     *     "MsgCode": "信息代码"
     * }
     */
    public static JSONMap error(String msg,String msgCode) {
        JSONMap jsonMap = new JSONMap();
        jsonMap.put("IsSuccess","0");
        jsonMap.put("Msg",msg);
        jsonMap.put("MsgCode",msgCode);
        return jsonMap;
    }

    /**
     * 返回到指定键所映射的值并转成string类型
     * @param key 键
     * @return 值
     */
    public String getString(String key) {
        Object obj = this.map.get(key);
        if(obj == null) return null;
        return String.valueOf(obj);
    }

    /**
     * 返回到指定键所映射的值并转成map类型
     * @param key 键
     * @return 值
     */
    public JSONMap getMap(String key) { return (JSONMap)this.map.get(key); }

    /**
     * 返回到指定键所映射的值并转成list类型
     * @param key 键
     * @return 值
     */
    public JSONList getList(String key) { return (JSONList)this.map.get(key); }
    /**
     * 返回到指定键所映射的值
     * @param key 键
     * @return 值
     */
    @Override
    public Object get(Object key) {
        return this.map.get(key);
    }

    /**
     * 将指定的值与此映射中的指定键相关联
     * @param key 键
     * @param value 值
     * @return 与key关联的先前值，如果null没有映射， key
     */
    @Override
    public Object put(String key, Object value) {
        return this.map.put(key, value);
    }

    /**
     * 如果存在（从可选的操作），从该map中删除一个键的映射
     * @param key 键
     * @return key关联的先前值，如果 null没有映射， key
     */
    @Override
    public Object remove(Object key) {
        return this.map.remove(key);
    }

    /**
     * 将指定map的所有映射复制到此映射（可选操作）
     * @param map 指定的Map类型的数据
     */
    @Override
    public void putAll(Map map) {
        this.map.remove(map);
    }

    /**
     * 从该map中删除所有的映射（可选操作）
     */
    @Override
    public void clear() {
        this.map.clear();
    }

    /**
     * 返回此map中键值映射的数量
     * @return 此映射中键值映射的数量
     */
    @Override
    public int size() {
        return this.map.size();
    }

    /**
     * 如果此map不包含键值映射，则返回 true
     * @return true如果此映射不包含键值映射
     */
    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    /**
     * 是否存在指定key的映射关系
     * @param key 键
     * @return true存在
     */
    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    /**
     * 是否存在指定value的映射关系
     * @param value 值
     * @return true存在
     */
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    /**
     * 获取所有的key
     * @return 此映射中包含的键的设置视图
     */
    @Override
    public Set keySet() {
        return this.map.keySet();
    }

    /**
     * 获取所有的value
     * @return 此映射中包含的值的集合视图
     */
    @Override
    public Collection values() {
        return this.map.values();
    }

    /**
     * 获取所有的映射关系
     * @return 此映射中包含的映射的set视图
     */
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return this.map.entrySet();
    }

    /**
     * 转成字符串
     * @return 转换后的字符串
     */
    @Override
    public String toString() {
        Object value;
        String str = "{";
        int i = 0,size = this.map.size();
        for(Entry<String,Object> entry : this.map.entrySet()) {
            i++;
            if(i != size) {
                value = entry.getValue();
                if(value == null) {
                    str += "\"" + entry.getKey().replace("\"","\\\"") + "\":null,";
                } else if(value.getClass().getName().contains("String") || value.getClass().getName().contains("Char")) {
                    str += "\"" + entry.getKey().replace("\"","\\\"") + "\":\"" + value.toString().replace("\"","\\\"") + "\",";
                } else {
                    str += "\""+entry.getKey().replace("\"","\\\"")+"\":"+value+",";
                }
            } else {
                value = entry.getValue();
                if(value == null) {
                    str += "\"" + entry.getKey().replace("\"","\\\"") + "\":null";
                } else if(value.getClass().getName().contains("String") || value.getClass().getName().contains("Char")) {
                    str += "\"" + entry.getKey().replace("\"","\\\"") + "\":\"" + value.toString().replace("\"","\\\"") + "\"";
                } else {
                    str += "\""+entry.getKey().replace("\"","\\\"")+"\":"+value;
                }
            }
        }
        return str+"}";
    }
}
