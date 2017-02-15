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