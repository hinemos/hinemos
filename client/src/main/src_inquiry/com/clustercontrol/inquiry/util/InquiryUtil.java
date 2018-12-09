/*
* Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
*
* Hinemos (http://www.hinemos.info/)
*
* See the LICENSE file for licensing information.
*/

package com.clustercontrol.inquiry.util;

import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.inquiry.TargetStatus;

public class InquiryUtil {
	
	public static String convertStatus(TargetStatus status) {
		if (status == null)
			return null;
		
		switch (status) {
		case CREATING:
			return Messages.getString("inquiry.target.status.creating");
		case DOWNLOADABLE:
			return Messages.getString("inquiry.target.status.downloadable");
		case EMPTY:
			return Messages.getString("inquiry.target.status.empty");
		default:
			return null;
		}
	}
	
	public static Boolean isDownloadable(String statusString) {
		if (Messages.getString("inquiry.target.status.downloadable").equals(statusString))
			return true;
		
		return false;
	}
}
