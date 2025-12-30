package leaf.system.exception;

/**
 * MyBatis包装异常
 * 用于标记这是已经被MyBatis拦截器处理过的异常
 */
public class MyBatisHandledException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    // 标记这个异常已经被MyBatis拦截器处理过
    private boolean myBatisHandled = true;

    public MyBatisHandledException(String message) {
        super(message);
    }

    public MyBatisHandledException(String message, Throwable cause) {
        super(message, cause);
        if (cause instanceof MyBatisHandledException) {
            this.myBatisHandled = ((MyBatisHandledException) cause).isMyBatisHandled();
        }
    }

    public boolean isMyBatisHandled() {
        return myBatisHandled;
    }

    public void setMyBatisHandled(boolean myBatisHandled) {
        this.myBatisHandled = myBatisHandled;
    }
}
