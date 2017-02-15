/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * リソース取得クラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class Messages {
	private static Log m_log = LogFactory.getLog( Messages.class );
	
	private static final String RESOURCE_BUNDLE_COMMON = "messages_common";
	private static final String RESOURCE_BUNDLE_CLIENT = "messages_client";
	private static boolean clientFlag = true;
	private static boolean commonFlag = true;

	private Messages() {}

	public static String getString(String key) {
		return getString(key, key, null);
	}

	public static String getString(String key, String def) {
		return getString(key, def, null);
	}

	public static String getString(String key, Locale locale) {
		return getString(key, key, locale);
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
	public static String getString(String key, String def, Locale locale) {
		String ret = def;
		ResourceBundle.Control control = ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT);
		if (clientFlag) {
			ResourceBundle bundle = null;
			try {
				if (locale != null) {
					bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_CLIENT, locale, control);
				} else {
					bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_CLIENT, control);
				}
			} catch (MissingResourceException e) {
				m_log.info(RESOURCE_BUNDLE_CLIENT + " missing"); // Client以外はここを1回だけ通る
				clientFlag = false;
			}
			try {
				if (clientFlag) {
					ret = bundle.getString(key);
				}
			} catch (MissingResourceException e) {}
		}
		if (commonFlag) {
			ResourceBundle bundle = null;
			try {
				if (locale != null) {
					bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_COMMON, locale, control);
				} else {
					bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_COMMON, control);
				}
			} catch (MissingResourceException e) {
				m_log.warn(RESOURCE_BUNDLE_COMMON + " missing"); // ここは通らないはず
				commonFlag = false;
			}
			try {
				if (commonFlag) {
					ret = bundle.getString(key);
				}
			} catch (MissingResourceException e) {}
		}
		
		return ret;
	}

	public static String getString(String key, Object[] args) {
		return getString(key, args, null);
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
	public static String getString(String key, Object[] args, Locale locale) {
		MessageFormat messageFormat = new MessageFormat(getString(key, key, locale));
		return messageFormat.format(args);
	}
	
}