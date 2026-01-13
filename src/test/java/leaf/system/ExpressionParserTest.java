package leaf.system;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.regex.*;

/**
 * 完整表达式解析器
 * 支持：&&、||、()、==、!=、>、>=、<、<=、contains
 * 示例：packageName == 'com.demo' && className == 'User'
 */
public class ExpressionParserTest {

    /**
     * 解析表达式并返回布尔结果
     */
    public static boolean evaluateExpression(String expr, Map<String, Object> context) {
        // 去除空格（保留括号内的内容）
        expr = expr.trim();

        // 1. 处理括号
        if (expr.contains("(")) {
            return evaluateParentheses(expr, context);
        }

        // 2. 处理逻辑运算符（按优先级：&& 先于 ||）
        if (expr.contains("&&")) {
            return evaluateLogicalAnd(expr, context);
        }

        if (expr.contains("||")) {
            return evaluateLogicalOr(expr, context);
        }

        // 3. 处理比较表达式
        return evaluateComparison(expr, context);
    }

    /**
     * 处理括号
     */
    private static boolean evaluateParentheses(String expr, Map<String, Object> context) {
        Stack<Integer> stack = new Stack<>();
        List<Range> parentheses = new ArrayList<>();

        // 找出所有括号对
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (c == '(') {
                stack.push(i);
            } else if (c == ')') {
                if (!stack.isEmpty()) {
                    int start = stack.pop();
                    parentheses.add(new Range(start, i));
                }
            }
        }

        // 从最内层括号开始处理
        Collections.sort(parentheses, (a, b) -> Integer.compare(b.start, a.start));

        String result = expr;
        for (Range range : parentheses) {
            String subExpr = expr.substring(range.start + 1, range.end);
            boolean subResult = evaluateExpression(subExpr, context);

            // 替换括号表达式为结果
            String before = result.substring(0, range.start);
            String after = result.substring(range.end + 1);
            result = before + (subResult ? "true" : "false") + after;
        }

        return evaluateExpression(result, context);
    }

    /**
     * 处理 && 运算
     */
    private static boolean evaluateLogicalAnd(String expr, Map<String, Object> context) {
        // 拆分 &&，但要避免拆分括号内的内容
        List<String> parts = splitByOperator(expr, "&&");

        boolean result = true;
        for (String part : parts) {
            result = result && evaluateExpression(part.trim(), context);
            if (!result) {
                break; // 短路计算
            }
        }
        return result;
    }

    /**
     * 处理 || 运算
     */
    private static boolean evaluateLogicalOr(String expr, Map<String, Object> context) {
        List<String> parts = splitByOperator(expr, "||");

        boolean result = false;
        for (String part : parts) {
            result = result || evaluateExpression(part.trim(), context);
            if (result) {
                break; // 短路计算
            }
        }
        return result;
    }

    /**
     * 智能拆分表达式，避免拆分括号内的内容
     */
    private static List<String> splitByOperator(String expr, String operator) {
        List<String> parts = new ArrayList<>();
        int start = 0;
        int bracketCount = 0;

        for (int i = 0; i < expr.length() - operator.length() + 1; i++) {
            // 检查括号层级
            if (expr.charAt(i) == '(') bracketCount++;
            else if (expr.charAt(i) == ')') bracketCount--;

            // 检查是否匹配操作符且不在括号内
            if (bracketCount == 0 && expr.startsWith(operator, i)) {
                parts.add(expr.substring(start, i).trim());
                i += operator.length() - 1;
                start = i + 1;
            }
        }

        // 添加最后一部分
        if (start < expr.length()) {
            parts.add(expr.substring(start).trim());
        }

        return parts;
    }

    /**
     * 处理比较表达式
     */
    private static boolean evaluateComparison(String expr, Map<String, Object> context) {
        // 使用正则匹配比较表达式
        Pattern pattern = Pattern.compile(
                "^(.*?)\\s*(==|!=|>|>=|<|<=|contains)\\s*(.*)$"
        );

        Matcher matcher = pattern.matcher(expr.trim());

        if (matcher.matches()) {
            String leftExpr = matcher.group(1).trim();
            String operator = matcher.group(2).trim();
            String rightExpr = matcher.group(3).trim();

            Object leftValue = resolveValue(leftExpr, context);
            Object rightValue = parseRightValue(rightExpr);

            return compareValues(leftValue, rightValue, operator);
        }

        // 如果不是比较表达式，可能是布尔常量
        if ("true".equalsIgnoreCase(expr)) return true;
        if ("false".equalsIgnoreCase(expr)) return false;

        // 或者是一个返回布尔值的变量
        Object value = resolveValue(expr, context);
        return value != null && Boolean.parseBoolean(value.toString());
    }

    /**
     * 解析变量（支持 a.b.c）
     */
    private static Object resolveValue(String expr, Map<String, Object> context) {
        // 处理布尔常量
        if ("true".equalsIgnoreCase(expr)) return true;
        if ("false".equalsIgnoreCase(expr)) return false;
        if ("null".equalsIgnoreCase(expr)) return null;

        String[] parts = expr.split("\\.");
        Object value = context.get(parts[0]);

        for (int i = 1; i < parts.length && value != null; i++) {
            value = getProperty(value, parts[i]);
        }
        return value;
    }

    /**
     * 获取对象属性（简化版）
     */
    private static Object getProperty(Object obj, String property) {
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).get(property);
        }
        // 可以扩展支持Java Bean等其他对象类型
        return null;
    }

    /**
     * 解析右侧值
     */
    private static Object parseRightValue(String expr) {
        expr = expr.trim();

        // 字符串
        if (expr.startsWith("'") && expr.endsWith("'")) {
            return expr.substring(1, expr.length() - 1);
        }

        // 布尔值
        if ("true".equalsIgnoreCase(expr)) return true;
        if ("false".equalsIgnoreCase(expr)) return false;

        // null
        if ("null".equalsIgnoreCase(expr)) return null;

        // 数字
        try {
            if (expr.contains(".")) {
                return Double.parseDouble(expr);
            } else {
                return Long.parseLong(expr);
            }
        } catch (NumberFormatException e) {
            return expr; // 保持为字符串
        }
    }

    /**
     * 比较值
     */
    private static boolean compareValues(Object left, Object right, String operator) {
        switch (operator) {
            case "==":
                return Objects.equals(left, right);
            case "!=":
                return !Objects.equals(left, right);
            case "contains":
                return left != null && right != null &&
                        left.toString().contains(right.toString());
            default:
                // 数字比较
                if (left instanceof Number && right instanceof Number) {
                    double leftNum = ((Number) left).doubleValue();
                    double rightNum = ((Number) right).doubleValue();
                    switch (operator) {
                        case ">": return leftNum > rightNum;
                        case ">=": return leftNum >= rightNum;
                        case "<": return leftNum < rightNum;
                        case "<=": return leftNum <= rightNum;
                    }
                }
                throw new IllegalArgumentException("不支持的操作符: " + operator);
        }
    }

    /**
     * 测试方法
     */
    @Test
    public void test() {
        Map<String, Object> context = new HashMap<>();
        Map<String, Object> packageInfo = new HashMap<>();
        packageInfo.put("name", "com.demo");
        context.put("packageName>", "com.demo");
        context.put("packageName", "com.demo");
        context.put("className", "User");
        context.put("version", 2.0);
        context.put("enabled", true);

        // 测试各种表达式
        String[] tests = {
                "packageName> == 'com.demo' && className == 'User'",
                "packageName == 'com.demo' && className == 'Admin'",
                "version > 1.0 && version < 3.0",
                "packageName contains 'demo' && className != 'Admin'",
                "(packageName == 'com.demo' || packageName == 'com.test') && className == 'User'",
                "enabled == true && version >= 2.0",
                "packageName != null && className != null"
        };

        for (String test : tests) {
            boolean result = evaluateExpression(test, context);
            System.out.println(test + " => " + result);
        }
    }

    /**
     * 辅助类：表示范围
     */
    private static class Range {
        int start;
        int end;

        Range(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}