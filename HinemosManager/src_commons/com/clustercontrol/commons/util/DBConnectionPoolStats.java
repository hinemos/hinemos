/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * DBコネクションのプール使用数の統計データを保持するためのクラス
 * 1時間単位でプール使用数の最大値をキューで保持する
 *
 * @see com.clustercontrol.commons.util.JpaSessionEventListener
 */
public class DBConnectionPoolStats {
	
		private volatile long updateTime;
		private final AtomicInteger maxUseCount;
		
		public DBConnectionPoolStats(long time, int count){
			updateTime = time;
			maxUseCount = new AtomicInteger(count);
		}
		
		public void setMaxUseInfo(long time, int count){
			updateTime = time;
			maxUseCount.set(count);
		}
		
		public long getUpdateTime(){
			return updateTime;
		}
		
		public int getMaxUseCount(){
			return maxUseCount.get();
		}
}