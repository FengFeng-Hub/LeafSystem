package leaf.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * 反射操作类
 */
public class Clazz {
    private Object obj;

    /**
     * 调用构造器
     * @param classPath 类路径 例如：lyf.common.Clazz
     * @param params 参数
     */
    public Clazz(String classPath, Object ... params) {
        try {
            Class clazz = Class.forName(classPath);//通过类名获得Class
            if(params.length > 0) {
                Class<?>[] classes = new Class[params.length];

                for(int i = 0;i < params.length;i++) {
                    classes[i] = params[i].getClass();
                }

                Constructor<Object> constructor = clazz.getDeclaredConstructor(classes);//调用有参数构造需要使用Constructor类对象
//                constructor.setAccessible(true);//设置私有构造可以访问
                this.obj = constructor.newInstance(params);
            } else {
                this.obj = clazz.newInstance();//实例化类 调用无参数构造
            }
        } catch (ClassNotFoundException e1) {
            Log.write("Error",Log.getException(e1,"调用失败：没有找到类'"+classPath+"'"));
        } catch(NoSuchMethodException e2) {
            Log.write("Error",Log.getException(e2,"调用失败：没有找到类'"+classPath+"'的有参构造方法"));
        } catch(InstantiationException | IllegalAccessException | InvocationTargetException e3) {
            Log.write("Error",Log.getException(e3));
        }
    }

    /**
     * 调用方法
     * @param methodName 方法名
     * @param params 方法一个或多个参数
     * @return 调用方法后的返回值
     */
    public Object invoke(String methodName,Object ... params) {
        try {
            if(params.length > 0) {
                Class<?>[] classes = new Class[params.length];

                for (int i = 0; i < params.length; i++) {
                    classes[i] = params[i].getClass();
                }

                return obj.getClass().getMethod(methodName,classes).invoke(obj,params);//调用此方法
            }

            return obj.getClass().getMethod(methodName).invoke(obj);//调用此方法
        } catch (NoSuchMethodException e1) {
            Log.write("Error",Log.getException(e1,"调用失败：没有找到方法'"+methodName+"'"));
        } catch(IllegalAccessException | InvocationTargetException e2) {
            Log.write("Error",Log.getException(e2));
        }
        return null;
    }

    /**
     * 调用静态方法
     * @param classPath 类路径 例如：lyf.common.Clazz
     * @param methodName 方法名
     * @param params 方法一个或多个参数
     * @return 调用方法后的返回值
     */
    public static Object invokeStatic(String classPath,String methodName,Object ... params) {
        try {
            if(params.length > 0) {
                Class<?>[] classes = new Class[params.length];

                for(int i = 0;i < params.length;i++) {
                    classes[i] = params[i].getClass();
                }
                return Class.forName(classPath).getMethod(methodName,classes).invoke(null,params);
            }

            return Class.forName(classPath).getMethod(methodName).invoke(null);
        } catch(ClassNotFoundException e1) {
            Log.write("Error",Log.getException(e1,"'" + classPath + "." + methodName + "'调用失败：没有找到类"));
        } catch(NoSuchMethodException | NullPointerException e2) {
            Log.write("Error",Log.getException(e2,"'" + classPath + "." + methodName + "'调用失败：没有找到静态方法"));
        } catch(IllegalAccessException e3) {
            Log.write("Error",Log.getException(e3,"'" + classPath + "." + methodName + "'调用失败：IllegalAccessException"));
        } catch(InvocationTargetException e4) {
            Log.write("Error",Log.getException(e4,"'" + classPath + "." + methodName + "'调用失败：InvocationTargetException"));
        }
        return null;
    }

    /**
     * 通过方法路径调用静态方法
     * @param methodPath 方法路径
     * @param params 方法一个或多个参数
     * @return 调用方法后的返回值
     */
    public static Object invokeStaticByMethodPath(String methodPath,Object ... params) {
        String[] split = methodPath.split("\\.");
        String classPath = "";

        for(int i = 0;i < split.length - 1;i ++) {
            if(i != split.length - 2) {
                classPath += split[i] + ".";
            } else {
                classPath += split[i];
            }
        }

        return invokeStatic(classPath,split[split.length - 1],params);
    }

    /**
     * 线程方法（静态）
     * @param classPath 类路径 例如：lyf.common.Clazz
     * @param methodName 方法名
     * @param params 方法一个或多个参数
     */
    public static void threadMethod(String classPath,String methodName,Object ... params) {
        new Thread(()->{
            Clazz.invokeStatic(classPath,methodName,params);
        }).start();
    }
}
