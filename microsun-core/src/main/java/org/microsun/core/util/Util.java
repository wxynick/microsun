/*
 * @(#)Util.java	 2013-4-1
 *
 * Copyright 2004-2013 WXXR Network Technology Co. Ltd. 
 * All rights reserved.
 * 
 * WXXR PROPRIETARY/CONFIDENTIAL.
 */
package org.microsun.core.util;

import java.io.Closeable;
import java.net.Socket;

/**
 * 
 * @class desc Util
 * 
 * @author wangxuyang
 * @version $Revision: 1.0 $
 * @Create 2013-4-1
 */
public class Util {
	  public static void close(Closeable cl) {
	      if (cl == null) return;
	      try {
	         cl.close();
	      } catch (Exception e) {
	      }
	   }

	   public static void close(Socket s) {
	      if (s == null) return;
	      try {
	         s.close();
	      } catch (Exception e) {
	      }
	   }

	   public static void close(Closeable... cls) {
	      for (Closeable cl : cls) {
	         close(cl);
	      }
	   }


}
