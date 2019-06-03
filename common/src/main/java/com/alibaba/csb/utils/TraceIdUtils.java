package com.alibaba.csb.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * trace id generator
 */
public class TraceIdUtils {
    private static String IP_16 = "ffffffff";
    private static String IP_int = "255255255255";
    private static final String regex = "\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b";
    private static final Pattern pattern = Pattern.compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");
    private static final String PID = getHexPid(doGetCurrrentPid());
    private static char PID_FLAG = 'd';
    private static AtomicInteger count = new AtomicInteger(1000);

    private static int doGetCurrrentPid() {
        try {
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            String name = runtime.getName();
            return Integer.parseInt(name.substring(0, name.indexOf(64)));
        } catch (Throwable var3) {
            return 0;
        }
    }

    static String getHexPid(int pid) {
        if (pid < 0) {
            pid = 0;
        } else if (pid > 65535) {
            pid %= 60000;
        }

        String str;
        for (str = Integer.toHexString(pid); str.length() < 4; str = '0' + str) {
        }
        return str;
    }

    private static boolean validate(String ip) {
        try {
            return pattern.matcher(ip).matches();
        } catch (Throwable var2) {
            return false;
        }
    }

    private static String getIP_int(String ip) {
        return ip.replace(".", "");
    }

    private static String getIP_16(String ip) {
        String[] ips = ip.split("\\.");
        StringBuilder sb = new StringBuilder();
        String[] var3 = ips;
        int var4 = ips.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            String column = var3[var5];
            String hex = Integer.toHexString(Integer.parseInt(column));
            if (hex.length() == 1) {
                sb.append('0').append(hex);
            } else {
                sb.append(hex);
            }
        }

        return sb.toString();
    }

    private static String getTraceId(String ip, long timestamp, int nextId) {
        StringBuilder appender = new StringBuilder(32);
        appender.append(ip).append(timestamp).append(nextId).append(PID_FLAG).append(PID);
        return appender.toString();
    }

    public static String generate() {
        return getTraceId(IP_16, System.currentTimeMillis(), getNextId());
    }

    public static String generate(String ip) {
        return ip != null && !ip.isEmpty() && validate(ip) ? getTraceId(getIP_16(ip), System.currentTimeMillis(), getNextId()) : generate();
    }

    private static int getNextId() {
        int current;
        int next;
        do {
            current = count.get();
            next = current > 9000 ? 1000 : current + 1;
        } while (!count.compareAndSet(current, next));

        return next;
    }

    static {
        try {
            String ipAddress = IPUtils.getLocalHostIP();
            if (ipAddress != null) {
                IP_16 = getIP_16(ipAddress);
                IP_int = getIP_int(ipAddress);
            }
        } catch (Throwable e) {
        }
    }
}
