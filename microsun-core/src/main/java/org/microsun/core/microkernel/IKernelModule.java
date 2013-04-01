package org.microsun.core.microkernel;

 public interface IKernelModule<T extends IKernelContext> {
	void start(T ctx);

	void stop();
}
