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
package com.clustercontrol.hub.bean;

import com.clustercontrol.maintenance.util.HinemosPropertyUtil;

/**
 * 収集蓄積で使用するプロパティをまとめるクラス。
 *
 */
public enum PropertyConstants {
	hub_transfer_batch_basetime("hub.transfer.batch.basetime", "00:00"),
	hub_transfer_delay_interval("hub.transfer.delay.interval", "0 0 0 * * ? *"),
	hub_search_switch_join("hub.search.switch.join", Boolean.FALSE),
	hub_transfer_max_try_count("hub.transfer.max.try.count", 5),
	hub_transfer_fetch_size("hub.transfer.fetch.size", 10000),
	hub_search_timeout("hub.search.timeout", 15000);
	
	public final String key;
	public final Object defaultValue;
	
	private PropertyConstants(String key, Object defaultValue) {
		this.key = key;
		this.defaultValue = defaultValue;
	}
	
	public String string() {
		return HinemosPropertyUtil.getHinemosPropertyStr(key, defaultValue.toString());
	}
	
	public int number() {
		return HinemosPropertyUtil.getHinemosPropertyNum(key, ((Integer)defaultValue).longValue()).intValue();
	}
	
	public boolean bool() {
		return HinemosPropertyUtil.getHinemosPropertyBool(key, (Boolean)defaultValue);
	}
	
	@Override
	public String toString() {
		return String.format("key=%s, value=%s", key, defaultValue.toString());
	}
	
	public String message_invalid(String value) {
		return String.format("key=%s, value=%s, invalid value then using %s", key, value, defaultValue.toString());
	}
}
