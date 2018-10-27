/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.traputil.bean;

import java.io.Serializable;

/**
 * SNMPTRAP監視のTRAPマスター情報Bean(DTO)クラス<BR>
 * 
 * @version 6.1.0
 * @since 2.1.0
 */
public class SnmpTrapMasterInfo implements Serializable{
	private static final long serialVersionUID = 5737352544693128254L;
	
	private String mib;
	private String trapOid;
	private int genericId;
	private int specificId;
	private String uei;
	private int priority;
	private String logmsg;
	private String descr;
	
	public SnmpTrapMasterInfo(){
		
	}
    /**
     * 説明を取得します。<BR>
     * @return 説明
     */
	public String getDescr() {
		return descr;
	}
    /**
     * 説明を設定します。<BR>
     * @param descr
     */
	public void setDescr(String descr) {
		this.descr = descr;
	}
    /**
     * GenericIdを取得します。<BR>
     * @return geniricId
     */
	public int getGenericId() {
		return genericId;
	}
    /**
     * GenericIdを設定します。<BR>
     * @param genericId
     */
	public void setGenericId(int genericId) {
		this.genericId = genericId;
	}
    /**
     * ログメッセージを取得します。<BR>
     * @return ログメッセージ
     */
	public String getLogmsg() {
		return logmsg;
	}
    /**
     * ログメッセージを設定します。<BR>
     * @param logmsg
     */
	public void setLogmsg(String logmsg) {
		this.logmsg = logmsg;
	}
    /**
     * MIBを取得します。<BR>
     * @return
     */
	public String getMib() {
		return mib;
	}
    /**
     * MIBを設定します。<BR>
     * @param mib
     */
	public void setMib(String mib) {
		this.mib = mib;
	}
    /**
     * SpecificIdを取得します。<BR>
     * @return
     */
	public int getSpecificId() {
		return specificId;
	}
    /**
     * SepecificIdを設定します。<BR>
     * @param specificId
     */
	public void setSpecificId(int specificId) {
		this.specificId = specificId;
	}
    /**
     * トラップOID名を取得します。<BR>
     * @return トラップOID名
     */
	public String getTrapOid() {
		return trapOid;
	}
    /**
     * トラップOID名を設定します。<BR>
     * @param trapOid
     */
	public void setTrapOid(String trapOid) {
		this.trapOid = trapOid;
	}
    /**
     * UEIを取得します。
     * @return　uei
     */
	public String getUei() {
		return uei;
	}
    /**
     * UEIを設定します。<BR>
     * @param uei
     */
	public void setUei(String uei) {
		this.uei = uei;
	}
	/**
	 * OIDの重要度を取得します。<BR>
	 * @return OIDの重要度
	 */
	public int getPriority() {
		return priority;
	}
	/**
	 * OIDの重要度を設定します。<BR>
	 * @param priority
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}
}
