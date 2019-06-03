package com.alibaba.csb.ws.sdk.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.axis.client.Stub;
import com.alibaba.csb.ws.sdk.WSParams;

/**
 * Dynamic Proxy Invocation Handler, set security related info to WS Client Proxy
 *
 * @author fuzhao.fz 2019年5月26日
 */
public class AxisStubDynamicProxyHandler implements InvocationHandler {
    private AxisStubInterceptor interceptor = new AxisStubInterceptor();
    private Object proxy;
    private AtomicBoolean isBound = new AtomicBoolean(false);

    public void setParams(WSParams params) {
        interceptor.setWSParams(params);
    }

    public <T extends Stub> Object bind(T business) {
        if (isBound.get()) {
            return business;
        }
        this.proxy = business;
        isBound.set(true);
        Class<?>[] ics = business.getClass().getInterfaces();
        return Proxy.newProxyInstance(business.getClass().getClassLoader(), ics, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        interceptor.before(this.proxy, method.getName());
        Object result;
        try {
            result = method.invoke(this.proxy, args);
        } finally {
            interceptor.after(this.proxy);
        }
        return result;
    }
}
