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
package com.clustercontrol.hub.util;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * 収集(文字列)をDBに格納する際に使用する ID を生成
 *
 */
public class StringDataIdGenerator {
	
	private static Log m_log = LogFactory.getLog( StringDataIdGenerator.class );
	
	private static AtomicLong generator; 
	
	public static synchronized void init() {
		if (generator != null) {
			return ;
		}
		
		try {
			Long maxId = QueryUtil.getMaxCollectStringDataId();
			
			if (maxId == null) {
				m_log.info("init() : Not found id, so start from 0.");
				generator = new AtomicLong();
			} else {
				m_log.info(String.format("init() : Max of id is %d, so start from %d.", maxId, maxId));
				generator = new AtomicLong(maxId);
			}
		} catch(Exception e) {
			m_log.warn("init() : Fail to get max of id, so start from 0.", e);
			generator = new AtomicLong();
		}
	}
	
	public static Long getNext(){
		if (generator == null) {
			init();
		}
		return generator.incrementAndGet();
	}
	
	public static Long getCurrent(){
		if (generator == null) {
			init();
		}
		return generator.get();
	}
	
	public static Long getMax(){
		return Long.MAX_VALUE;
	}
}