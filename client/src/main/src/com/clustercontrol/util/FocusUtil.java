/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.util;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;

public class FocusUtil {
	private static Log m_log = LogFactory.getLog( FocusUtil.class );
	
	private String lastClazz = null;
	private IContextActivation activation = null;
	private ArrayList<String> dialogList = new ArrayList<>();
	
	/** Get singleton */
	private static FocusUtil getInstance(){
		return SingletonUtil.getSessionInstance( FocusUtil.class );
	}
	
	public static void setView(String clazz) {
		m_log.trace("setView1 : " + clazz);
		if (clazz == null || clazz.equals(getInstance().lastClazz)) {
			return;
		}
		m_log.trace("setView2 : " + clazz);
		getInstance().lastClazz = clazz;
		deactivateContext();
		activateContext();
	}
	
	public static void setDialog(String dialog) {
		deactivateContext();
		getInstance().dialogList.add(dialog);
	}
	public static void unsetDialog(String dialog) {
		getInstance().dialogList.remove(dialog);
		if (getInstance().dialogList.isEmpty()){
			activateContext();
		}
	}
	
	private static void activateContext() {
		IContextService contextService = (IContextService)PlatformUI.getWorkbench().getService(IContextService.class);
		if (contextService.getDefinedContextIds().contains(getInstance().lastClazz)) {
			getInstance().activation = contextService.activateContext(getInstance().lastClazz);
			m_log.debug("  activateContext " + getInstance().lastClazz);
		} else {
			// ここは通らないはず。
			// 通ってしまった場合は、plugin.xmlの <extension point="org.eclipse.ui.contexts"> を修正すること
			m_log.warn("notDefined!! : " + getInstance().lastClazz);
		}
	}
	private static void deactivateContext() {
		if (getInstance().activation == null) {
			return;
		}
		IContextService contextService = (IContextService)PlatformUI.getWorkbench().getService(IContextService.class);
		m_log.debug("deactivateContext " + getInstance().activation.getContextId());
		contextService.deactivateContext(getInstance().activation);
		getInstance().activation = null;
	}
}
