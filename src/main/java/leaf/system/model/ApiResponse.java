package leaf.system.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ApiResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 数据
     */
    private Object data;
    /**
     * 是否成功
     */
    @JsonProperty("IsSuccess")
    private String success;
    /**
     * 信息
     */
    @JsonProperty("Msg")
    private String message;
    /**
     * 数据总数
     */
    @JsonProperty("Count")
    private String count;

    @JsonProperty("Code")
    private String code;

    /**
     * 动态属性
     */
    @JsonIgnore
    private Map<String, Object> properties = new HashMap<>();
    // 将所有动态属性作为顶级属性返回
    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return properties;
    }

    public ApiResponse() {}

    public ApiResponse(Object data, String success, String message, String count, String code) {
        this.data = data;
        this.success = success;
        this.message = message;
        this.count = count;
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Object get(String key) {
        return properties.get(key);
    }

    // ========== 链式调用 ==========
    public ApiResponse data(Object data) {
        this.data = data;
        return this;
    }

    public ApiResponse isSuccess(String success) {
        this.success = success;
        return this;
    }

    public ApiResponse doSuccess() {
        this.success = IsSuccess.SUCCESS.getCode();
        return this;
    }

    public ApiResponse doError() {
        this.success = IsSuccess.ERROR.getCode();
        return this;
    }

    public ApiResponse message(String message) {
        this.message = message;
        return this;
    }

    public ApiResponse count(String count) {
        this.count = count;
        return this;
    }

    public ApiResponse code(String code) {
        this.code = code;
        return this;
    }

    public ApiResponse put(String key, Object value) {
        this.properties.put(key, value);
        return this;
    }

    // ========== 快速构建 ==========
    public static ApiResponse build() {
        return new ApiResponse();
    }

    public static ApiResponse success() {
        return build().doSuccess();
    }

    public static ApiResponse success(Object data) {
        return build().doSuccess().data(data);
    }

    public static ApiResponse error() {
        return build().doError();
    }

    public static ApiResponse error(String message) {
        return build().doError().message(message);
    }

    public enum IsSuccess {
        SUCCESS("1", "成功"),ERROR("0", "失败");
        /**
         * 状态码
         */
        private final String code;
        /**
         * 描述
         */
        private final String name;

        IsSuccess(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        /**
         * 根据编码获取枚举
         * @param code 状态码
         * @return 枚举对象
         */
        public static IsSuccess getByCode(String code) {
            for (IsSuccess value : values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }
}
