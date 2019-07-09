package com.alibaba.csb.utils;

import java.net.*;
import java.util.Enumeration;

/**
 * Get the local host IP
 */
public class IPUtils {
    private static String DEFAULT_IP = "127000000001";

    public static String getLocalHostIP() {
        Enumeration allNetInterfaces;
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            try {
                InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
                return jdkSuppliedAddress.getHostAddress();
            } catch (UnknownHostException ex) {
                return DEFAULT_IP;
            }
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
                    return address.getHostAddress();
                }
            }
        }
        return DEFAULT_IP;
    }
}
