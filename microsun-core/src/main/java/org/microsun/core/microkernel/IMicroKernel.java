package org.microsun.core.microkernel;
public interface IMicroKernel<C extends IKernelContext, T extends IKernelModule<C>> {

   public void registerKernelModule(T module);

   public void unregisterKernelModule(T module);

}