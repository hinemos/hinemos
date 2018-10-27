/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

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
	
	private final static String[] COMMON_BUNDLE_NAMES = {
		"messages_common",
		"messages_xcloud_common_aws",
		"messages_xcloud_common_hyperv",
		"messages_xcloud_common_azure",
		"messages_xcloud_common_kvm",
		"messages_xcloud_common_vmware"
	};
	
	private final static String[] CLIENT_BUNDLE_NAMES = {
		"messages_client",
		"messages_xcloud_client_aws",
		"messages_xcloud_client_hyperv",
		"messages_xcloud_client_azure",
		"messages_xcloud_client_kvm",
		"messages_xcloud_client_vmware" 
	};
	
	private static Map<Locale, Set<String>> missing = new HashMap<Locale, Set<String>>();
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
		Set<String> bundleNames = missing.get(locale);
		if (bundleNames == null) {
			bundleNames = new HashSet<>();
			missing.put(locale, bundleNames);
		}
		
		String ret = def;
		ResourceBundle.Control control = ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT);
		if (clientFlag) {
			boolean foundBundle = false;
			for (String name: CLIENT_BUNDLE_NAMES) {
				ResourceBundle bundle = null;
				try {
					if (locale != null) {
						bundle = ResourceBundle.getBundle(name, locale, control);
					} else {
						bundle = ResourceBundle.getBundle(name, control);
					}
					foundBundle = true;
				} catch (MissingResourceException e) {
					if (!bundleNames.contains(name)) {
						bundleNames.add(name);
						m_log.debug(name + " missing");
					}
				}
				
				if (bundle != null) {
					try {
						bundleNames.remove(name);
						ret = bundle.getString(key);
						break;
					} catch (MissingResourceException e) {
					}
				}
			}
			
			if (!foundBundle) {
				 // Client以外はここを1回だけ通る
				m_log.info(String.format("Client bundles are missing. bundles=%s", Arrays.toString(CLIENT_BUNDLE_NAMES)));
				clientFlag = false;
			}
		}
		
		if (commonFlag) {
			boolean foundBundle = false;
			for (String name: COMMON_BUNDLE_NAMES) {
				ResourceBundle bundle = null;
				try {
					if (locale != null) {
						bundle = ResourceBundle.getBundle(name, locale, control);
					} else {
						bundle = ResourceBundle.getBundle(name, control);
					}
					foundBundle = true;
				} catch (MissingResourceException e) {
					if (!bundleNames.contains(name)) {
						bundleNames.add(name);
						m_log.debug(name + " missing");
					}
				}
				
				if (bundle != null) {
					try {
						bundleNames.remove(name);
						ret = bundle.getString(key);
						break;
					} catch (MissingResourceException e) {
					}
				}
			}
			
			if (!foundBundle) {
				m_log.warn(String.format("Common bundles are missing. bundles=%s", Arrays.toString(COMMON_BUNDLE_NAMES)));
				commonFlag = false;
			}
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