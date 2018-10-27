/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.util;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.rap.rwt.SingletonUtil;

import com.clustercontrol.util.EndpointManager;


/**
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
public class UtilityManagerUtil {

	private static class ManagerManager {
		private ManagerManager() {
		}

		public static ManagerManager getInstance() {
			return SingletonUtil.getSessionInstance(ManagerManager.class);
		}

		private String currentManagerName;

		public String getCurrentManagerName() {
			if (currentManagerName == null && EndpointManager.getActiveManagerNameList() != null) {
				for (String name : EndpointManager.getActiveManagerNameList()) {
					currentManagerName = name;
					break;
				}
			}
			return currentManagerName;
		}

		public void setCurrentManagerName(String managerName) {
			String oldManagerName = currentManagerName;

			this.currentManagerName = managerName;

			if (oldManagerName != null && !oldManagerName.equals(currentManagerName)) {
				for (ManagerChangeListener listener : notifyList) {
					listener.notifyManagerChanged();
				}
			}
		}

		List<ManagerChangeListener> notifyList = new ArrayList<>();

		public void addManagerChangeListener(ManagerChangeListener listener) {
			notifyList.add(listener);
		}
	}

	public interface ManagerChangeListener{
		void notifyManagerChanged();
	}

	public static String getCurrentManagerName() {
		return ManagerManager.getInstance().getCurrentManagerName();
	}

	public static void setCurrentManagerName(String managerName) {
		ManagerManager.getInstance().setCurrentManagerName(managerName);
	}

	public static void addManagerChangeListener(ManagerChangeListener listener){
		ManagerManager.getInstance().addManagerChangeListener(listener);
	}
}
