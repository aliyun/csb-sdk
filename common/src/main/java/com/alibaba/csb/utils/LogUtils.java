package com.alibaba.csb.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * log4j util
 */
public class LogUtils {
    private static Object _LOGGER;
    private static Method _ERROR;

    private static Object LOGGER;
    private static Method INFO;
    private static Method WARN;
    private static Method ERROR;

    static {
        try {
            Class<?> logFactoryClass = Class.forName("org.slf4j.LoggerFactory");
            LOGGER = logFactoryClass.getMethod("getLogger", String.class).invoke(logFactoryClass, "CSBSDK");
            INFO = LOGGER.getClass().getMethod("info", String.class, Object[].class);
            WARN = LOGGER.getClass().getMethod("warn", String.class, Object[].class);
            ERROR = LOGGER.getClass().getMethod("error", String.class, Object[].class);

            _LOGGER = logFactoryClass.getMethod("getLogger", Class.class).invoke(logFactoryClass, LogUtils.class);
            _ERROR = LOGGER.getClass().getMethod("error", String.class, Throwable.class);
        } catch (Exception e) {
            System.err.println("未引入Log4j");
        }
    }

    public static void info(String format, Object... arguments) {
        if (LOGGER == null) {
            print(format, arguments);
            return;
        }
        try {
            INFO.invoke(LOGGER, format, arguments);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void warn(String format, Object... arguments) {
        if (LOGGER == null) {
            print(format, arguments);
            return;
        }
        try {
            WARN.invoke(LOGGER, format, arguments);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void error(String format, Object... arguments) {
        if (LOGGER == null) {
            print(format, arguments);
            return;
        }
        try {
            ERROR.invoke(LOGGER, format, arguments);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void exception(String msg, Throwable ex) {
        if (_LOGGER == null) {
            return;
        }
        try {
            _ERROR.invoke(_LOGGER, msg, ex);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static void print(String format, Object... args) {
        String fmt = format.replace("{}","%s");
        System.out.println(String.format(fmt, args));
    }
}
