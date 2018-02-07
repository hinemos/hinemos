/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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