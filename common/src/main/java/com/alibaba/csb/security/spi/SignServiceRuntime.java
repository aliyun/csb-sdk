package com.alibaba.csb.security.spi;

import com.alibaba.csb.sdk.security.DefaultSignServiceImpl;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * SPI current provider selector
 *
 * 选取当前使用的实现类
 *
 * Created on 18/6/15.
 */
public class SignServiceRuntime {
  private static ServiceLoader<SignService> serviceLoader = ServiceLoader.load(SignService.class);

  /**
   * 选取SingService实现类，选取顺序：
   * 1. 根据请求参数指定的实现类名来选取，如果没有找到则抛出Exception
   * 2. 如果请求参数为空，则试图从系统环境变量-Dcom.alibaba.csb.security.spi.SignService指定的实现类，如果指定的实现类没找到则抛出Exception
   * 3. 如果没有指定系统环境变量，则从当前CLASSPATH中的/META-INF/services/com.alibaba.csb.security.spi.SignService文件中指定的实现类返回
   *
   * @param currImplName
   * @return
   */
  public static SignService pickSignService(String currImplName) {
    Iterator<SignService> it = serviceLoader.iterator();

    String pickImpl = null;
    if (currImplName != null) {
      pickImpl = currImplName;
    } else {
      pickImpl = System.getProperty(SignService.class.getName());
    }

    if (pickImpl == null) {
      if (!it.hasNext()) {
        return DefaultSignServiceImpl.getInstance();
      }

      //return the first one
      return it.next();
    } else {
      SignService ss = null;
      while(it.hasNext()) {
        ss = it.next();
        if (pickImpl.equals(ss.getClass().getName())) {
          return  ss;
        }
      }

      throw new IllegalArgumentException(String.format("Can not found SPI provider for name:%s", pickImpl));
    }

  }

}
