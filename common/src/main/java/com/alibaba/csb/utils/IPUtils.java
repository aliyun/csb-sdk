package com.alibaba.csb.utils;

import java.net.*;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Get the local host IP
 */
public class IPUtils {
    private static String DEFAULT_IP = "127000000001";

    private static final AtomicReference<String> IP = new AtomicReference<String>();

    public static String getLocalHostIP() {
        String ip = IP.get();
        if (ip != null) {
            return ip;
        }
        Enumeration allNetInterfaces;
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            try {
                InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
                IP.compareAndSet(null, jdkSuppliedAddress.getHostAddress());
            } catch (UnknownHostException ex) {
                IP.compareAndSet(null, DEFAULT_IP);
            }
            return IP.get();
        }

        while (allNetInterfaces.hasMoreElements()) {
            NetworkInterface ni;
            String displayName;
            do {
                ni = (NetworkInterface) allNetInterfaces.nextElement();
                displayName = ni.getDisplayName();
            } while (displayName != null && displayName.startsWith("virbr"));

            Enumeration addresses = ni.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = (InetAddress) addresses.nextElement();
                if (!address.isLoopbackAddress() && address.getHostAddress().indexOf(":") == -1) {
                    IP.compareAndSet(null, address.getHostAddress());
                    return IP.get();
                }
            }
        }
        IP.compareAndSet(null, DEFAULT_IP);
        return IP.get();
    }
}
