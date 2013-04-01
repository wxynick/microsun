/*
 * @(#)IMBeanObject.java	 Sep 15, 2010
 *
 * Copyright 2004-2010 WXXR Network Technology Co. Ltd. 
 * All rights reserved.
 * 
 * WXXR PROPRIETARY/CONFIDENTIAL.
 */

package org.microsun.core.jmx.api;

import javax.management.MBeanServer;

/**
 * @class desc A IMBeanObject.
 * 
 * @author Neil
 * @version v1.0 
 * @created time Sep 15, 2010  12:00:33 PM
 */
public interface IMBeanObject {
   void registerMBean(MBeanServer server, String objNamePrefix) throws Exception;  
   void unregisterMBean(MBeanServer server);
}
