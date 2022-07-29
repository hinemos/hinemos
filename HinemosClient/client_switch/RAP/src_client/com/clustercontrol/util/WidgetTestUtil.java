/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.JavaScriptExecutor;
import org.eclipse.rap.rwt.widgets.WidgetUtil;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

/**
 * Widget Test Utility
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class WidgetTestUtil{

	/** Enable UI tests */
	static boolean enableUITests;

	static {
		/** Enable UI tests while org.eclipse.rap.rwt.enableUITests is set to true under development mode */
		String env_devMode = System.getProperty("org.eclipse.rap.rwt.developmentMode");
		String env_enableUITests = System.getProperty("org.eclipse.rap.rwt.enableUITests");
		enableUITests = Boolean.valueOf(env_devMode).booleanValue() && Boolean.valueOf(env_enableUITests).booleanValue();
	}

	// prevent instantiation
	private WidgetTestUtil() {
	}

	/**
	 * Set TestId attribute by JavaScript
	 * 
	 * @param widget
	 * @param value
	 * @see http://eclipse.org/rap/developers-guide/devguide.php?topic=scripting.html&version=2.3
	 */
	public static void setTestId(Object parent, String prefix, Widget widget) {
		if( enableUITests && !widget.isDisposed() ){
			String testid = parent.getClass().getSimpleName() + "-";
			if( null == prefix ){
				testid += widget.getClass().getSimpleName();
			}else{
				testid += prefix + widget.getClass().getSimpleName();
			}
			String $el = widget instanceof Text ? "$input" : "$el";
			String id = WidgetUtil.getId(widget);
			exec("rap.getObject( '", id, "' ).", $el, ".attr( 'testid', '", testid + "' );");
		}
	}

	private static void exec(String... strings) {
		StringBuilder builder = new StringBuilder();
		builder.append("try{");
		for (String str : strings) {
			builder.append(str);
		}
		builder.append("}catch(e){}");
		JavaScriptExecutor executor = RWT.getClient().getService(JavaScriptExecutor.class);
		executor.execute(builder.toString());
	}
}
