/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.factory;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import com.clustercontrol.HinemosManagerMain;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.util.HinemosTime;

/**
 * セッションIDを作成するクラスです。
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class CreateSessionId {

	private static String prevDate = "";
	private static int prevNumber = HinemosManagerMain._instanceId;

	/**
	 * 現在時刻からセッションIDを作成します。
	 * 2台のクラスタ構成にて、1台目は除数0のミリ秒、2台目は除数1のミリ秒にて
	 * 重複しないように払いだされる。
	 * 
	 * @return セッションID
	 */
	synchronized public static String create() {
		ILockManager lm = LockManagerFactory.instance().create();
		ILock lock = lm.create(CreateSessionId.class.getName());
		
		try {
			lock.writeLock();
			
			String sessionId = null;
	
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			dateFormat.setTimeZone(HinemosTime.getTimeZone());
			String dateString = dateFormat.format(HinemosTime.getDateInstance());
			
			DecimalFormat format = new DecimalFormat("-000");
			if(prevDate.equals(dateString)){
				prevNumber += HinemosManagerMain._instanceCount;
				sessionId = dateString + format.format(prevNumber);
			}
			else{
				sessionId = dateString + format.format(0);
				prevDate = dateString;
				prevNumber = HinemosManagerMain._instanceId;
			}
	
			return sessionId;
		} finally {
			lock.writeUnlock();
		}
	}
}
