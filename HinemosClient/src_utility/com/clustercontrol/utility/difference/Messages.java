/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Messages {
	private static Log log = LogFactory.getLog(Messages.class);

	private static final String BUNDLE_NAME = "com.clustercontrol.utility.difference.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private static final ResourceBundleSelector _selector;

	static {
		_selector = new ResourceBundleSelector(RESOURCE_BUNDLE);
	}

	private Messages() {
	}

	public static String getString(String key) {
		return _selector.getString(key);
	}

	/**
	 * 外部からこのクラスで参照するリソースバンドルを追加する
	 * 
	 * @param bundle
	 */
	public static void addBundle(ResourceBundle bundle) {
		_selector.addBundle(bundle);
	}

	/**
	 * 複数のリソースバンドルを参照可能とするクラス
	 *
	 */
	public static class ResourceBundleSelector {
		List<ResourceBundle> bundles;

		public ResourceBundleSelector(ResourceBundle... defaults) {
			this.bundles = new ArrayList<>();
			for (ResourceBundle bundle : defaults) {
				this.bundles.add(bundle);
			}
		}

		public void addBundle(ResourceBundle bundle) {
			bundles.add(bundle);
			log.debug("addBundle() : " + bundle.getBaseBundleName());
		}

		public String getString(String key) {
			for (ResourceBundle bundle : bundles) {
				try {
					return bundle.getString(key);
				} catch (MissingResourceException e) {
					if (log.isDebugEnabled()) {
						log.debug(
								"getString() : Missing. key=" + key + ", ResourceBundle=" + bundle.getBaseBundleName());
					}
					// NOP (continue)
				}
			}
			return key;
		}
	}
}
