/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ping.bean;

import javax.xml.bind.annotation.XmlType;



/**
 * fpingの結果を格納するクラス<BR>
 * 
 *
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class PingResult {

	private String ipAddress;
	private String messeage;
	private String messeageOrg;

	private int lost;
	private float average = 0;
	private float reachRatio;


	/**
	 * コンストラクタ
	 * @param ipAddress
	 * @param messeage
	 * @param messeageOrg
	 * @param lost
	 * @param average
	 * @param reachRatio
	 */
	public PingResult(String ipAddress, String messeage, String messeageOrg, int lost, float average, float reachRatio) {
		super();
		this.ipAddress=ipAddress;
		this.messeage = messeage;
		this.messeageOrg = messeageOrg;
		this.lost = lost;
		this.average = average;
		this.reachRatio = reachRatio;
	}


	/**
	 * コンストラクタ
	 *
	 */
	public PingResult() {
		super();

	}


	/**
	 * 平均応答時間を取得します。
	 * @return
	 */
	public float getAverage() {
		return average;
	}
	/**
	 * 平均応答時間を設定します。
	 * @return
	 */
	public void setAverage(float average) {
		this.average = average;
	}
	/**
	 * 損失率を取得します。
	 * 
	 * @return
	 */
	public int getLost() {
		return lost;
	}
	/**
	 * 損失率を設定します。
	 * @param lost
	 */
	public void setLost(int lost) {
		this.lost = lost;
	}
	/**
	 * メッセージを取得します。
	 * 
	 * @return
	 */
	public String getMesseage() {
		return messeage;
	}
	/**
	 * メッセージを設定します。
	 * @param messeage
	 */
	public void setMesseage(String messeage) {
		this.messeage = messeage;
	}
	/**
	 * オリジナルメッセージを取得します。
	 * @return
	 */
	public String getMesseageOrg() {
		return messeageOrg;
	}
	/**
	 * オリジナルメッセージを設定します。
	 * @param messeageOrg
	 */
	public void setMesseageOrg(String messeageOrg) {
		this.messeageOrg = messeageOrg;
	}
	/**
	 * 到達率を取得します。
	 * @return
	 */
	public float getReachRatio() {
		return reachRatio;
	}
	/**
	 * 到達率を設定します。
	 * 
	 */
	public void setReachRatio(float reachRatio) {
		this.reachRatio = reachRatio;
	}
	/**
	 * IPアドレスを取得します。
	 * @return
	 */
	public String getIpAddress() {
		return ipAddress;
	}
	/**
	 * IPアドレスを設定します。
	 * @param ipAddress
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

}
