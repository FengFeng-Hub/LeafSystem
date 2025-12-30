package leaf.system.interceptor;

import leaf.common.Log;
import leaf.system.exception.MyBatisHandledException;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * MyBatis异常拦截器
 * 用于拦截MyBatis操作中的异常，避免被全局异常处理器重复处理
 */
@Component
@Intercepts({
        // 执行 SQL（你已有）
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        // 参数绑定阶段
        @Signature(type = ParameterHandler.class, method = "setParameters", args = {PreparedStatement.class}),
        // 创建 statement + prepare 阶段
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
        // 结果映射阶段
        @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})
})
public class MyBatisExceptionInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) {
        try {
            return invocation.proceed();
        } catch (Throwable e) {
            String sql = "Unknown SQL";
            int errorCode = 0;
            String errorMsg = e.getMessage();

            try {
                Object arg0 = invocation.getArgs()[0];
                Object arg1 = invocation.getArgs().length > 1 ? invocation.getArgs()[1] : null;

                if (arg0 instanceof MappedStatement) {
                    MappedStatement mappedStatement = (MappedStatement) arg0;
                    BoundSql boundSql = mappedStatement.getBoundSql(arg1);
                    if (boundSql != null) {
                        sql = boundSql.getSql();
                    }
                }

                if (e.getCause() instanceof SQLException) {
                    SQLException sqlEx = (SQLException) e.getCause();
                    errorCode = sqlEx.getErrorCode();
                    errorMsg = sqlEx.getMessage();
                }
            } catch (Exception ex) {
                // 获取 SQL 或错误码失败时使用默认值
            }

            StringBuilder log = new StringBuilder();
            log.append("[ ERROR - ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(new Date())).append(" ================================================== ]\n");
            log.append("ErrorMsg:").append(errorMsg).append("\n");
            log.append("------\n");
            log.append("SQL:").append(errorCode).append("\n");
            log.append(sql).append("\n");
            log.append("------\n");
            log.append(getStackTrace(e));
            Log.write("Error_MyBatis", String.valueOf(log));
            throw new MyBatisHandledException("MyBatis操作异常", e);
        }
    }

    private String getStackTrace(Throwable e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}
