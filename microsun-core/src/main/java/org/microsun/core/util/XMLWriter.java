/*
 * @(#)XMLWriter.java	 2013-3-19
 *
 * Copyright 2004-2013 WXXR Network Technology Co. Ltd. 
 * All rights reserved.
 * 
 * WXXR PROPRIETARY/CONFIDENTIAL.
 */
package org.microsun.core.util;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @class desc XMLWriter
 * 
 * @author wangxuyang
 * @version $Revision: 1.4 $
 * @Create 2013-3-19
 */
public class XMLWriter {
	private final Writer w;
	private final List<String> elements = new ArrayList<String>();
	private boolean empty;
	private boolean endAttr = true;
	private boolean indent;

	public XMLWriter(Writer w) {
		this(w, true);		
	}

	public XMLWriter(Writer w, boolean indent) {
		this.w = w;
		this.indent = indent;
	}
	public XMLWriter header() throws IOException {
		w.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		if (indent) {
			newLine();
		}
		return this;
	}
	public XMLWriter indent(int nb) throws IOException {
		if (indent) {
			while (nb-- > 0) {
				w.append("  ");
			}
		}
		return this;
	}

	public XMLWriter newLine() throws IOException {
		if (indent) {
			w.append("\n");
		}
		return this;
	}

	public XMLWriter element(String name) throws IOException {
		if (!endAttr) {
			endAttr = true;
			w.append(">");
		}
		if (!elements.isEmpty()) {
			newLine();
			indent(elements.size());
		}
		w.append("<").append(name);
		elements.add(name);
		empty = true;
		endAttr = false;
		return this;
	}

	public XMLWriter attribute(String name, Object value) throws IOException {
		if (value != null) {
			w.append(" ").append(name).append("='")
					.append(encode(value.toString())).append("'");
		}
		return this;
	}

	public XMLWriter end() throws IOException {
		return end(true);
	}

	public XMLWriter end(boolean indent) throws IOException {
		String name = (String) elements.remove(elements.size() - 1);
		if (!endAttr) {
			endAttr = true;
			w.append("/>");
		} else {
			if (indent && !empty) {
				newLine();
				indent(elements.size());
			}
			w.append("</").append(name).append(">");
		}
		empty = false;
		return this;
	}

	public XMLWriter text(Object value) throws IOException {
		if (!endAttr) {
			endAttr = true;
			w.append(">");
		}
		w.append(encode(value.toString()));
		return this;
	}
	public XMLWriter textCDDATA(Object value) throws IOException {
		if (!endAttr) {
			endAttr = true;
			w.append(">");
		}
		w.append("<![CDATA["+encode(value.toString())+"]]>");
		return this;
	}
	public XMLWriter textElement(String name, Object value) throws IOException {
		if (value != null) {
			element(name).text(value).end(false);
		}
		return this;
	}

	private static String encode(Object o) {
		String s = o != null ? o.toString() : "";
		return s.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;").replaceAll("'", "&apos;");
	}
}
