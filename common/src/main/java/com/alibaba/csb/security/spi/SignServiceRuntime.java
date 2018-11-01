package com.alibaba.csb.security.spi;

import com.alibaba.csb.sdk.security.DefaultSignServiceImpl;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * SPI current provider selector
 * <p>
 * 选取当前使用的实现类
 * <p>
 * Created on 18/6/15.
 */
public class SignServiceRuntime {
    private static ServiceLoader<SignService> serviceLoader = ServiceLoader.load(SignService.class);
    private static final Map<String, SignService> signServiceMap = new HashMap<String, SignService>();

    /**
     * 选取SingService实现类，选取顺序：
     * 1. 根据请求参数指定的实现类名来选取，如果没有找到则抛出Exception
     * 2. 如果请求参数为空，则试图从系统环境变量-Dcom.alibaba.csb.security.spi.SignService指定的实现类，如果指定的实现类没找到则抛出Exception
     * 3. 如果没有指定系统环境变量，则从当前CLASSPATH中的/META-INF/services/com.alibaba.csb.security.spi.SignService文件中指定的实现类返回
     *
     * @param pickImpl
     * @return
     */
    public static SignService pickSignService(String pickImpl) {
        if (pickImpl == null) {
            Iterator<SignService> it = serviceLoader.iterator();
            if (!it.hasNext()) {
                return DefaultSignServiceImpl.getInstance();
            }
            return it.next(); //return the first one
        } else {
            SignService signService = signServiceMap.get(pickImpl);//从缓存中查找
            if (signService == null) {
                synchronized (signServiceMap) {
                    signService = getNewSignService(pickImpl);
                    signServiceMap.put(pickImpl, signService);
                }
            }
            return signService;
        }
    }

    private static SignService getNewSignService(String pickImpl) {
        //find from spi definition
        for (Iterator<SignService> it = serviceLoader.iterator(); it.hasNext(); ) {
            SignService ss = it.next();
            if (pickImpl.equals(ss.getClass().getName())) { //serviceLoader中包含用户指定的签名类
                return ss;
            }
        }

        //load by myself
        final AccessControlContext acc = (System.getSecurityManager() != null) ? AccessController.getContext() : null;
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            ClassLoader loader = (cl == null) ? ClassLoader.getSystemClassLoader() : cl;
            final Class<?> cc = Class.forName(pickImpl, false, loader);

            if (!SignService.class.isAssignableFrom(cc)) {
                throw new IllegalArgumentException(String.format("The class %s is not implement interface: com.alibaba.csb.security.spi.SignService", pickImpl));
            }
            PrivilegedAction<SignService> action = new PrivilegedAction<SignService>() {
                public SignService run() {
                    try {
                        return SignService.class.cast(cc.newInstance());
                    } catch (Throwable e) {
                        throw new Error(e);
                    }
                }
            };
            return AccessController.doPrivileged(action, acc);
        } catch (ClassNotFoundException x) {
            throw new IllegalArgumentException(String.format("Can not class-found SPI provider for name:%s", pickImpl));
        }
    }

}
