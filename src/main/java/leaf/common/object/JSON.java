package leaf.common.object;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.List;
import java.util.Map;

public class JSON {
    /**
     * json对象
     */
    private static ObjectMapper objectMapper;

    static {
        synchronized(JSON.class) {
            if(objectMapper == null) {
                objectMapper = new ObjectMapper();
            }
        }
    }

    /**
     * json字符串转map
     * @param str json字符串
     * @return 转换后的map
     */
    public static Map toMap(String str) {
        try {
            return objectMapper.readValue(str,Map.class);
        } catch (JsonProcessingException e) {
             return null;
        }
    }

    /**
     * json字符串转list
     * @param str json字符串
     * @return 转换后的list
     */
    public static List toList(String str) {
        try {
            return objectMapper.readValue(str, List.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * json字符串转object
     * @param str json字符串
     * @return 转换后的list
     */
    public static <T> T toObject(String str, Class<T> type) {
        try {
            return objectMapper.readValue(str, type);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * object转json字符串
     * @param obj 需要转换的对象
     * @return 转换后的json字符串
     */
    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 格式化JSON字符串
     * @param str json字符串
     * @return 格式化后的 String 对象
     */
    public static String formatJSON(String str) {
        ObjectMapper mapper = new ObjectMapper();
        // 启用美化输出
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // 先将字符串解析为对象，再序列化为格式化后的字符串
        try {
            return mapper.writeValueAsString(mapper.readValue(str, Object.class));
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
