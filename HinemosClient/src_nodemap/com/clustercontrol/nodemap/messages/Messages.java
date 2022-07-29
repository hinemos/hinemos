/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.messages;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * リソース取得クラス<BR>
 * @since 1.0.0
 */
public class Messages {
	private static final String RESOURCE_BUNDLE = "com.clustercontrol.nodemap.messages.messages";

	private static ResourceBundle m_bundle = ResourceBundle
	.getBundle(RESOURCE_BUNDLE);

	private Messages() {

	}

	/**
	 * リソースバンドルを返します。
	 * 
	 * @return リソースバンドル
	 */
	public static ResourceBundle getBundle() {
		return m_bundle;
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
	public static String getString(String key, Object[] args) {
		return MessageFormat.format(getString(key), args);
	}

	/**
	 * Returns the resource object with the given key in the resource bundle. If
	 * there isn't any value under the given key, the key is returned.
	 * 
	 * @param key
	 *            the resource name
	 * @return the string
	 */
	public static String getString(String key) {
		try {
			return m_bundle.getString(key);
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
	public static String getString(String key, String def) {
		try {
			return m_bundle.getString(key);
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
	public static String getString(String key, Object[] args, Locale locale) {
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
	public static String getString(String key, Locale locale) {
		ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE,
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
	public static String getString(String key, String def, Locale locale) {
		ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE,
				locale);
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return def;
		}
	}

}