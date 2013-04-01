package org.microsun.core.microkernel;


public interface IKernelContext {
   <T> void registerService(Class<T> interfaceClazz,T handler);
   <T> void unregisterService(Class<T> interfaceClazz,T handler);
   <T> T getService(Class<T> interfaceClazz);
   <T> T getService(Class<T> interfaceClazz,long timeout);
   void setAttribute(String key, Object value);
   Object removeAttribute(String key);
   Object getAttribute(String key);
}
