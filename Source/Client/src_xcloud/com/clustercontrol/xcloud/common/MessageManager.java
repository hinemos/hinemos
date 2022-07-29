/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.common;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * リソース取得クラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class MessageManager {
	private static final Map<String, MessageManager> managers = new HashMap<String, MessageManager>();
	
	private String fileName;
	private ResourceBundle bundle;

	private MessageManager(String fileName) {
		this.fileName = fileName;
		bundle = ResourceBundle.getBundle(fileName, Locale.getDefault(), Thread.currentThread().getContextClassLoader());
	}

	private MessageManager(String fileName, ClassLoader loader) {
		this.fileName = fileName;
		bundle = ResourceBundle.getBundle(fileName, Locale.getDefault(), loader);
	}
	public static MessageManager getInstance(String fileName, ClassLoader loader){
		MessageManager manager = null;
		synchronized(managers){
			if(managers.containsKey(fileName)){
				manager = managers.get(fileName);
			} else { 
				manager = new MessageManager(fileName, loader);
				managers.put(fileName, manager);
			}
		}
		return manager;
	}

	public static MessageManager getInstance(String fileName){
		MessageManager manager = null;
		synchronized(managers){
			if(managers.containsKey(fileName)){
				manager = managers.get(fileName);
			} else { 
				manager = new MessageManager(fileName);
				managers.put(fileName, manager);
			}
		}
		return manager;
	}

	/**
	 * Returns the formatted message for the given key in the resource bundle.
	 * 
	 * @param key
	 *            the resource name
	 * @param args
	 *            the message arguments
	 * @return the string
	 */
	public String getString(String key, Object[] args) {
		List<Object> objs = new LinkedList<Object>(); 
		for(Object obj: args){
			objs.add(getString((String)obj));
		}
		MessageFormat messageFormat = new MessageFormat(getString(key));
		return messageFormat.format(objs.toArray());
	}

	/**
	 * Returns the resource object with the given key in the resource bundle. If
	 * there isn't any value under the given key, the key is returned.
	 * 
	 * @param key
	 *            the resource name
	 * @return the string
	 */
	public String getString(String key) {
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the resource object with the given key in the resource bundle. If
	 * there isn't any value under the given key, the default value is returned.
	 * 
	 * @param key
	 *            the resource name
	 * @param def
	 *            the default value
	 * @return the string
	 */
	public String getString(String key, String def) {
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return def;
		}
	}

	/**
	 * Returns the formatted message for the given key in the resource bundle.
	 * 
	 * @param key
	 *            the resource name
	 * @param args
	 *            the message arguments
	 * @param locale
	 * @return the string
	 */
	public String getString(String key, Object[] args, Locale locale) {
		MessageFormat messageFormat = new MessageFormat(getString(key, locale));
		return messageFormat.format(args);
	}

	/**
	 * Returns the resource object with the given key in the resource bundle. If
	 * there isn't any value under the given key, the key is returned.
	 * 
	 * @param key
	 *            the resource name
	 * @param locale
	 * @return the string
	 */
	public String getString(String key, Locale locale) {
		ResourceBundle bundle = ResourceBundle.getBundle(fileName,
				locale);
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the resource object with the given key in the resource bundle. If
	 * there isn't any value under the given key, the default value is returned.
	 * 
	 * @param key
	 *            the resource name
	 * @param def
	 *            the default value
	 * @param locale
	 * @return the string
	 */
	public String getString(String key, String def, Locale locale) {
		ResourceBundle bundle = ResourceBundle.getBundle(fileName,
				locale);
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return def;
		}
	}

}
