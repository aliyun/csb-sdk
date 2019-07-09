package com.alibaba.csb.sdk;

import java.io.IOException;

public class Version {

    public static void main(String [] args) {
        version();
    }

    public static void version() {
            try {
                System.out.println(CommUtil.geCurrenttVersionFile());
            } catch (IOException e) {
                //
            }
        }
}
