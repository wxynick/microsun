package org.microsun.core.microkernel;


import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.microsun.core.jmx.api.IMBeanObject;
import org.microsun.core.log.Log;
import org.microsun.core.log.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public abstract class AbstractMicroKernel<C extends IKernelContext, M extends IKernelModule<C>> implements MBeanRegistration, IMicroKernel<C,M>{
  private static Log log = LogFactory.getLog(AbstractMicroKernel.class);
   
   private Element moduleConfigure;
   
   private LinkedList<M> modules = new LinkedList<M>();
   
   private LinkedList<M> createdModules = new LinkedList<M>();
   
   private boolean started = false;
   
   private Map<String, Object> attributes = new ConcurrentHashMap<String, Object>();
   
   private HashMap<Class<?>, LinkedList<Object>> serviceHandlers = new HashMap<Class<?>, LinkedList<Object>>();
   

   private MBeanServer mbeanServer;
   
   private String moduleNamePrefix;
   
   @SuppressWarnings("unchecked")
   public void start() throws Exception{
      initModules();
      for (Object mod : getAllModules()) {
         ((M)mod).start(getContext());
      }
      this.started = true;
   }
   
   
   @SuppressWarnings("unchecked")
   public void stop(){
      destroyModules();
      for (Object mod : getAllModules()) {
         ((M)mod).stop();
      }
      this.started = false;
   }

   protected Object getAttribute(String key) {
      return attributes.get(key);
   }

   @SuppressWarnings("unchecked")
   protected <T> T getService(Class<T> interfaceClazz) {
      LinkedList<Object> handlers = null;
      synchronized(this.serviceHandlers){
         handlers = this.serviceHandlers.get(interfaceClazz);
         if(handlers == null){
            return null;
         }
      }
      synchronized(handlers){
         if(handlers.isEmpty()){
            return null;
         }
         return (T)handlers.getFirst();
      }
   }
   

   @SuppressWarnings("unchecked")
   protected <T> T getService(Class<T> interfaceClazz,long timeout) {
      LinkedList<Object> handlers = null;
      synchronized(this.serviceHandlers){
         handlers = this.serviceHandlers.get(interfaceClazz);
         if(handlers == null){
            handlers = new LinkedList<Object>();
            this.serviceHandlers.put(interfaceClazz, handlers);
         }
      }
      synchronized(handlers){
         if(handlers.isEmpty()){
            try {
               handlers.wait(timeout);
            }catch (InterruptedException e) {
            }
         }
         return handlers.isEmpty() ? null : (T)handlers.getFirst();
      }
   }

   protected <T> void registerService(Class<T> interfaceClazz, T handler) {
      LinkedList<Object> handlers = null;
      synchronized(this.serviceHandlers){
         handlers = this.serviceHandlers.get(interfaceClazz);
         if(handlers == null){
            handlers = new LinkedList<Object>();
            this.serviceHandlers.put(interfaceClazz, handlers);
         }
      }
      if(log.isInfoEnabled()){
         log.info("Register service :["+interfaceClazz.getCanonicalName()+"] handler :["+handler+"]");
      }
      synchronized(handlers){
         if(!handlers.contains(handler)){
            handlers.addLast(handler);
            handlers.notifyAll();
            //fireServiceRegistered(interfaceClazz, handler);
         }
      }
   }

   
   protected Object removeAttribute(String key) {
      return attributes.remove(key);
   }

   protected void setAttribute(String key, Object value) {
      attributes.put(key, value);
   }

   protected <T> void unregisterService(Class<T> interfaceClazz, T handler) {
      LinkedList<Object> handlers = null;
      synchronized(this.serviceHandlers){
         handlers = this.serviceHandlers.get(interfaceClazz);
         if(handlers == null){
            return;
         }
      }
      if(log.isInfoEnabled()){
         log.info("Unregister service :["+interfaceClazz.getCanonicalName()+"] handler :["+handler+"]");
      }
      /*synchronized(handlers){
         if(handlers.remove(handler)){
        	 fireServiceUnregistered(interfaceClazz, handler);
         }
      }*/
   }

 
   public void setModuleConfigure(Element serviceConfigure) {
	   if(null != serviceConfigure){
		   this.moduleConfigure = serviceConfigure;
	   }
   }

   public void registerKernelModule(M module) {
      boolean added = false;
      synchronized(modules){
         if(!this.modules.contains(module)){
            this.modules.add(module);
            added = true;
         }
      }
      if(added){
         registerModuleMBean(module);
         if(this.started){
            if(log.isInfoEnabled()){
               log.info("Starting module :["+module+"] ...");
            }
            module.start(getContext());
            if(log.isInfoEnabled()){
               log.info("Module :["+module+"] started !");
            }
         }
      }
   }

   public void unregisterKernelModule(M module) {
      boolean removed = false;
      synchronized(modules){
         removed = this.modules.remove(module);
      }
      if(removed){
         if(this.started){
            if(log.isInfoEnabled()){
               log.info("Stopping module :["+module+"] ...");
            }
            module.stop();
            if(log.isInfoEnabled()){
               log.info("Module :["+module+"] stopped !");
            }
         }      
         unregisterModuleMBean(module);
      }
   }

   public ObjectName preRegister(MBeanServer server, ObjectName name)
         throws Exception {
      this.mbeanServer = server;
      this.moduleNamePrefix = name.getCanonicalName()+",module=";
      registerModuleMBeans();
      return name;
   }

   public void postRegister(Boolean registrationDone) {
      
   }

   public void preDeregister() throws Exception {
      unregisterModuleMBeans();
   }

   public void postDeregister() {
      
   }
   
   @SuppressWarnings("unchecked")
   protected void registerModuleMBeans() {
      for (Object mod : getAllModules()) {
         registerModuleMBean((M)mod);
      }
   }

   protected void registerModuleMBean(M mod) {
      if((mod instanceof IMBeanObject)&&(mbeanServer != null)&&(moduleNamePrefix != null)){
         try {
           /* if(log.isInfoEnabled()){
               log.info("Register module :["+mod+"] as MBean !");
            }*/
            ((IMBeanObject)mod).registerMBean(mbeanServer, moduleNamePrefix);
         }catch (Exception e) {
            log.warn("Failed to register module :"+mod+" as MBean", e);
         }
      }
   }

   protected void unregisterModuleMBean(M mod) {
      if((mod instanceof IMBeanObject)&&(mbeanServer != null)){
         try {
           /* if(log.isInfoEnabled()){
               log.info("UnRegister module :["+mod+"]  MBean !");
            }*/
            ((IMBeanObject)mod).unregisterMBean(mbeanServer);
         }catch (Exception e) {
           // log.warn("Failed to unregister module :"+mod, e);
         }
      }
   }

   abstract protected C getContext();
   
   @SuppressWarnings("unchecked")
   protected void unregisterModuleMBeans() {
      for (Object mod : getAllModules()) {
         unregisterModuleMBean((M)mod);
      }
   }
   
   protected void destroyModules() {
      for (M mod : this.createdModules) {
         try {
            unregisterKernelModule(mod);
         }catch(Exception e){
            log.warn("Failed to unregister module :"+mod, e);
         }
      }
      this.createdModules.clear();
   }
   
   protected void initModules() throws Exception {
      if(moduleConfigure == null){
         return;
      }
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      NodeList nodes = moduleConfigure.getElementsByTagName("module");
      int len = nodes.getLength();
      for (int i = 0; i < len; i++) {
         Node node = nodes.item(i);
         if(node instanceof Element){
            Element elem = (Element)node;
            String clsName = elem.getAttribute("class");
            clsName = clsName != null ? clsName.trim() : null;
            clsName = clsName != null && clsName.length() > 0 ? clsName : null;
            if(clsName != null){
               @SuppressWarnings("unchecked")
			   Class<M> clazz = (Class<M>)cl.loadClass(clsName);
               M mod = clazz.newInstance();
               Method initMethod = null;
               try {
                  initMethod = clazz.getMethod("init", new Class[]{Element.class});
               }catch(Exception e){
                  log.warn("Cannot find init(Element) method to init module :"+mod, e);
               }
               if(initMethod != null){
                  initMethod.invoke(mod, new Object[]{elem});
               }
               registerKernelModule(mod);
               this.createdModules.add(mod);
            }
         }
      }
   }

   private Object[] getAllModules() {
      synchronized(modules){
         if(modules.isEmpty()){
            return new IKernelModule[0];
         }else{
            return modules.toArray(new IKernelModule[modules.size()]);
         }
      }
   }
}
