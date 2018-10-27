/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlType;

/**
 * 監視情報のチェック条件情報を保持する抽象クラス<BR>
 * <p>
 * 各監視管理クラスで継承してください。
 * jaxbで利用するため、引数なしのコンストラクタが必要。
 * そのため、abstractにしない。
 * 
 * @version 2.0.0
 * @since 2.0.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
@MappedSuperclass
public class MonitorCheckInfo implements Serializable {

	private static final long serialVersionUID = 2945451890219694984L;

	/** 監視項目ID */
	private String m_monitorId;

	/** 監視対象ID */
	private String m_monitorTypeId;

	/**
	 * コンストラクタ。
	 */
	public MonitorCheckInfo(){
	}

	@Id
	@Column(name="monitor_id")
	public String getMonitorId(){
		return this.m_monitorId;
	}
	public void setMonitorId(String monitorId){
		this.m_monitorId = monitorId;
	}
	
	@Transient
	public String getMonitorTypeId(){
		return this.m_monitorTypeId;
	}
	public void setMonitorTypeId(String monitorTypeId){
		this.m_monitorTypeId = monitorTypeId;
	}
}
