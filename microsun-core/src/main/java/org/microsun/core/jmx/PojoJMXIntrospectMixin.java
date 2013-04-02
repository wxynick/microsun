/*
 * @(#)PojoJMXIntrospectMixin.java	 Nov 27, 2009
 *
 * Copyright 2004-2009 WXXR Network Technology Co. Ltd. 
 * All rights reserved.
 * 
 * WXXR PROPRIETARY/CONFIDENTIAL.
 */

package org.microsun.core.jmx;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;

import org.microsun.core.jmx.annotation.ManagedAttribute;
import org.microsun.core.jmx.annotation.ManagedOperation;
import org.microsun.core.jmx.annotation.ManagedOperationParameter;
import org.microsun.core.jmx.annotation.ManagedOperationParameters;

public class PojoJMXIntrospectMixin implements DynamicMBean {
   private static final String[] EMPTY_S_ARRAY = new String[0];

   public static class OpsKey
   {
      
      private final String methodName;
      private final String[] signature;
      private final int hash;

      public OpsKey(String methodName, String[] sig)
      {
         this.methodName = methodName;
         this.signature = (sig == null) ? EMPTY_S_ARRAY : sig;
         int tmp = methodName.hashCode();
         for (int i = 0; i < signature.length; i++)
         {
            tmp += signature[i].hashCode();
         }
         hash = tmp;
      }

      public int hashCode()
      {
         return hash;
      };
      public String getMethodName()
      {
         return methodName;
      }

      public String[] getSignature()
      {
         return signature;
      }

      public boolean equals(Object obj)
      {
         if (obj == this) return true;
         OpsKey key = (OpsKey) obj;
         if (key.hash != hash) return false;
         if (!key.methodName.equals(methodName)) return false;
         if (key.signature.length != signature.length) return false;
         for (int i = 0; i < signature.length; i++)
         {
            if (!signature[i].equals(key.signature[i])) return false;
         }
         return true;
      }
   }

   private final Object target;
   private final Class<?> clazz;
   private final Map<OpsKey, Method> ops = new HashMap<OpsKey, Method>();
   private final Map<String, Method> gets = new HashMap<String, Method>();
   private final Map<String, Method> sets = new HashMap<String, Method>();
   private MBeanInfo mbeanInfo;


   public PojoJMXIntrospectMixin(Object target)
   {
      this(target,null);
   }
   

   public PojoJMXIntrospectMixin(Object target,Class<?> clazz)
   {
      this.target = target;
      if(clazz == null){
    	  this.clazz = target.getClass();
      }else{
    	  this.clazz = clazz;
      }
   }

   public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException
   {
      Method get = (Method) gets.get(attribute);
      if (get == null) throw new AttributeNotFoundException(attribute);
      try
      {
         return get.invoke(target);
      }
      catch (IllegalAccessException e)
      {
         throw new ReflectionException(e);
      }
      catch (InvocationTargetException e)
      {
         if (e.getTargetException() instanceof Exception) throw new MBeanException((Exception) e.getTargetException());
         throw new MBeanException(new Exception(e.getTargetException().getMessage()));
      }
   }

   public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
   {
      Method set = (Method) sets.get(attribute.getName());
      if (set == null) throw new AttributeNotFoundException(attribute.getName());
      try
      {
         Object[] args = {attribute.getValue()};
         set.invoke(target, args);
      }
      catch (IllegalAccessException e)
      {
         throw new ReflectionException(e);
      }
      catch (InvocationTargetException e)
      {
         if (e.getTargetException() instanceof Exception) throw new MBeanException((Exception) e.getTargetException());
         throw new MBeanException(new Exception(e.getTargetException().getMessage()));
      }
   }

   public AttributeList getAttributes(String[] attributes)
   {
      AttributeList list = new AttributeList();
      for (int i = 0; i < attributes.length; i++)
      {
         try
         {
            list.add(0, new Attribute(attributes[i], getAttribute(attributes[i])));
         }
         catch (AttributeNotFoundException e)
         {
            throw new RuntimeException(e);
         }
         catch (MBeanException e)
         {
            throw new RuntimeException(e);
         }
         catch (ReflectionException e)
         {
            throw new RuntimeException(e);
         }
      }
      return list;
   }

   public AttributeList setAttributes(AttributeList attributes)
   {
      for (int i = 0; i < attributes.size(); i++)
      {
         try
         {
            Attribute attr = (Attribute) attributes.get(i);
            setAttribute(attr);
         }
         catch (InvalidAttributeValueException inv)
         {
            throw new RuntimeException(inv);
         }
         catch (AttributeNotFoundException e)
         {
            throw new RuntimeException(e);
         }
         catch (MBeanException e)
         {
            throw new RuntimeException(e);
         }
         catch (ReflectionException e)
         {
            throw new RuntimeException(e);
         }
      }
      return attributes;
   }

   public Object invoke(String actionName, Object params[], String signature[]) throws MBeanException, ReflectionException
   {
      OpsKey key = new OpsKey(actionName, signature);
      Method m = (Method) ops.get(key);
      if (m == null) throw new NoSuchMethodError(actionName);
      try
      {
         return m.invoke(target, params);
      }
      catch (IllegalAccessException e)
      {
         throw new ReflectionException(e);
      }
      catch (InvocationTargetException e)
      {
         Throwable cause = e.getTargetException();
         if (cause instanceof Exception) throw new MBeanException((Exception) cause);
         throw new MBeanException(new Exception(e.getMessage()));
      }
   }

   public MBeanInfo getMBeanInfo()
   {
      //System.out.println("******************* MBEAN INFO **********************");
      if (mbeanInfo != null)
      {
         //System.out.println("****mbeanInfo already exists***");
         return mbeanInfo;
      }


      try
      {
         introspectInterfaceMethods();
         introspectAnnotatedMethods();

         Map<String,MBeanAttributeInfo> attributes = new HashMap<String,MBeanAttributeInfo>();
         Iterator<String> it = gets.keySet().iterator();
         while (it.hasNext())
         {
            String attribute = it.next();
            Method m = gets.get(attribute);
            boolean isWritable = sets.containsKey(attribute);
            boolean isIs = m.getName().startsWith("is");
            String desc = getAttributeDescription(attribute, m);
            MBeanAttributeInfo info = new MBeanAttributeInfo(attribute, m.getReturnType().getName(), desc, true, isWritable, isIs);
            attributes.put(attribute, info);
         }
         it = sets.keySet().iterator();
         while (it.hasNext())
         {
            String attribute = (String) it.next();
            if (gets.containsKey(attribute)) continue;
            Method m = (Method) sets.get(attribute);
            String desc = getAttributeDescription(attribute, m);
            MBeanAttributeInfo info = new MBeanAttributeInfo(attribute, m.getReturnType().getName(), desc, false, true, false);
            attributes.put(attribute, info);
         }

         MBeanOperationInfo[] operations = new MBeanOperationInfo[ops.size()];
         Iterator<Method> mit = ops.values().iterator();
         int i = 0;
         while (mit.hasNext())
         {
            Method m = mit.next();
            operations[i++] = createMBeanOperationInfo(m, m.getName(), getOperationDescription(m));
         }
         MBeanAttributeInfo[] attrs = (MBeanAttributeInfo[]) attributes.values().toArray(new MBeanAttributeInfo[attributes.size()]);

         mbeanInfo = new MBeanInfo(this.clazz.getName(), this.clazz.getName(),
                 attrs, null, operations, null);
         //System.out.println("***returning MBeanInfo****");
         return mbeanInfo;
      }

      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   private void introspectAnnotatedMethods() {
      Method[] methods = this.clazz.getMethods();

      //System.out.println("**** introspect attributes ****");
      for (int i = 0; i < methods.length; i++)
      {
         ManagedAttribute attrAnn = methods[i].getAnnotation(ManagedAttribute.class);
         ManagedOperation opAnn = methods[i].getAnnotation(ManagedOperation.class);
         if((attrAnn == null)&&(opAnn == null)){
            continue;
         }
         if (methods[i].getName().startsWith("get") && methods[i].getParameterTypes().length == 0)
         {
            //System.out.println("adding get attribute: " + methods[i].getName().substring(3));
            gets.put(methods[i].getName().substring(3), methods[i]);
         }
         else if (methods[i].getName().startsWith("is") && methods[i].getParameterTypes().length == 0 &&
                 (methods[i].getReturnType().equals(Boolean.class) || methods[i].getReturnType().equals(boolean.class)))
         {
            //System.out.println("adding is attribute: " + methods[i].getName().substring(2));
            gets.put(methods[i].getName().substring(2), methods[i]);
         }
         else if (methods[i].getName().startsWith("set") && methods[i].getParameterTypes().length == 1)
         {
            sets.put(methods[i].getName().substring(3), methods[i]);
         }
         else
         {
            addManagedOp(methods[i]);
         }
      }
   }

   private void addManagedOp(Method method) {
      String[] signature = new String[method.getParameterTypes().length];
      for (int j = 0; j < method.getParameterTypes().length; j++)
      {
         signature[j] = method.getParameterTypes()[j].getName();
      }
      ops.put(new OpsKey(method.getName(), signature), method);
   }

   private String getAttributeDescription(String attribute, Method m) {
        ManagedAttribute attrAnn = m.getAnnotation(ManagedAttribute.class);
        return (attrAnn != null) ? attrAnn.description() : "MBean Attribute";
   }
 
   private String getOperationDescription(Method m) {
      ManagedOperation attrAnn = m.getAnnotation(ManagedOperation.class);
      return (attrAnn != null) ? attrAnn.description() : "MBean Operation";
   }

   protected MBeanParameterInfo[] getOperationParameters(Method method) {
      ManagedOperationParameter[] params = getManagedOperationParameters(method);
      if (params == null || params.length == 0) {
          return new MBeanParameterInfo[0];
      }

      MBeanParameterInfo[] parameterInfo = new MBeanParameterInfo[params.length];
      Class<?>[] methodParameters = method.getParameterTypes();

      for (int i = 0; i < params.length; i++) {
          ManagedOperationParameter param = params[i];
          parameterInfo[i] =
                  new MBeanParameterInfo(param.name(), methodParameters[i].getName(), param.description());
      }
      return parameterInfo;
  }

   protected ManagedOperationParameter[] getManagedOperationParameters(Method method) {
      ManagedOperationParameters params = method.getAnnotation(ManagedOperationParameters.class);
      ManagedOperationParameter[] result = null;
      if (params == null) {
         result = new ManagedOperationParameter[0];
      }
      else {
         result = params.value();
      }
      return result;
   }

   
   protected MBeanOperationInfo createMBeanOperationInfo(Method method, String name,String description) {
      MBeanParameterInfo[] params = getOperationParameters(method);
      MBeanOperationInfo info = null;
      if (params.length == 0) {
          info = new MBeanOperationInfo(description, method);
      }else {
          info = new MBeanOperationInfo(name,
              description,
              params,
              method.getReturnType().getName(),
              MBeanOperationInfo.UNKNOWN);
      }
      return info;
  }
   
   protected void introspectInterfaceMethods() throws IntrospectionException {
      Class<?> mbeanInterface = findStandardInterface(this.clazz);
      if(mbeanInterface == null){
          return;
      }
      Method[] methods = mbeanInterface.getMethods();

      for (int i = 0; i < methods.length; ++i)
      {
          String methodName = methods[i].getName();
          Class<?>[] signature = methods[i].getParameterTypes();
          Class<?> returnType  = methods[i].getReturnType();

          if (methodName.startsWith("set") && methodName.length() > 3 
                  && signature.length == 1 && returnType == Void.TYPE)
          {
              String key = methodName.substring(3, methodName.length());
              Method setter = (Method) sets.get(key);
              if (setter != null && setter.getParameterTypes()[0].equals(signature[0]) == false)
              {
                  continue;
              }
              sets.put(key, methods[i]);
          }
          else if (methodName.startsWith("get") && methodName.length() > 3 
                  && signature.length == 0 && returnType != Void.TYPE)
          {
              String key = methodName.substring(3, methodName.length());
              Method getter = (Method) gets.get(key);
              if (getter != null && getter.getName().startsWith("is"))
              {
                 continue;
              }
              gets.put(key, methods[i]);
          }
          else if (methodName.startsWith("is") && methodName.length() > 2 
                  && signature.length == 0 && (returnType == Boolean.TYPE))
          {
              String key = methodName.substring(2, methodName.length());
              Method getter = (Method) gets.get(key);
              if (getter != null && getter.getName().startsWith("get"))
              {
                 continue;
              }
              gets.put(key, methods[i]);
          }
          else
          {
             addManagedOp(methods[i]);
          }
      }
  }


   public static Class<?> findStandardInterface(Class<?> mbeanClass)
   {
       Class<?> concrete = mbeanClass;
       Class<?> stdInterface = null;
       while (null != concrete)
       {
           stdInterface = findStandardInterface(concrete, concrete.getInterfaces());
           if (null != stdInterface)
           {
               return stdInterface;
           }
           concrete = concrete.getSuperclass();
       }
       return null;
   }

   private static Class<?> findStandardInterface(Class<?> concrete, Class<?>[] interfaces)
   {
       String stdName = concrete.getName() + "MBean";
       Class<?> retval = null;

       // look to see if this class implements MBean std interface
       for (int i = 0; i < interfaces.length; ++i)
       {
           if (interfaces[i].getName().equals(stdName))
           {
               retval = interfaces[i];
               break;
           }
       }

       return retval;
   }


}
