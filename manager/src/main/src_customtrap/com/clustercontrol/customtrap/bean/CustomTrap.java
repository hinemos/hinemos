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

package com.clustercontrol.customtrap.bean;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 受信した カスタムトラップ1件分の情報を内部形式にて保持する<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class CustomTrap implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * データ種別(数値／文字列)
	 *
	 */
	public static enum Type {
		NUM,STRING;

		@JsonCreator
		public static Type fromString(String jsonValue) {
			switch (jsonValue.toLowerCase()) {
			case "num":
				return Type.NUM;
			case "string":
				return Type.STRING;
			}
			return null;
		}
	}

	/** 送信データ上の日付*/
	@JsonProperty("DATE")
	private String date;
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	
	/** データ種別 */
	@JsonProperty("TYPE")
	private Type type;

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	/** キー */
	@JsonProperty("KEY")
	private String key;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	/** メッセージ */
	@JsonProperty("MSG")
	private String msg;

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	/** 受信時間 */
	@JsonIgnore
	private long receivedTime;

	public long getReceivedTime() {
		return receivedTime;
	}

	public void setReceivedTime(long receivedTime) {
		this.receivedTime = receivedTime;
	}

	/** サンプリング時間 */
	@JsonIgnore
	private long sampledTime;

	public long getSampledTime() {
		return sampledTime;
	}

	public void setSampledTime(long sampledTime) {
		this.sampledTime = sampledTime;
	}

	/** オリジナルメッセージ */
	@JsonIgnore
	private String orgMsg;

	public String getOrgMsg() {
		return orgMsg;
	}

	public void setOrgMsg(String orgMsg) {
		this.orgMsg = orgMsg;
	}

	@JsonIgnore
	public boolean isStringType() {
		return (this.type == Type.STRING);
	}
	
	@JsonIgnore
	public boolean isNumberType() {
		return (this.type == Type.NUM);
	}

	@Override
	public String toString() {
		return "CustomTrap [DATE=" + date + ", TYPE=" + type + ", KEY=" + key + ", MSG=" + msg + ",orgMsg=" + orgMsg + "]";
	}

}
