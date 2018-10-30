package com.alibaba.csb.sdk.security;

public class ParamNode implements Comparable<ParamNode> {
    private String name;
    private String value;

    public ParamNode(String name, String value) {
        this.setName(name);
        this.setValue(value);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int compareTo(ParamNode o) {
        int res = name.compareTo(o.name);
        if (res == 0) {
            res = value.compareTo(o.value);
        }
        return res;
    }

    public String toString() {
        return name + "=" + value;
    }

    public String toRawString() {
        return name + value;
    }
}
