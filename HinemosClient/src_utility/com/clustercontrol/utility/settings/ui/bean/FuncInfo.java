/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * ジョブの基本情報を保持するクラス
 * 
 * @version 6.1.0
 * @since 1.0.0
 */
public class FuncInfo implements Serializable, Cloneable {
	
	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = -1710299199569960870L;
	
	/** ID */
	private String m_id;
	
	/** 機能名 */
	private String m_name;
	
	/** 重み付け */
	private int m_weight;

	/**アクションクラス名*/
	private String m_actionClassName;
	
	/**XMLファイル名(複数)*/
	private List<String> m_defaultXml;
	
	/** 操作対象のIDリスト */
	private List<String> m_targetIdList;
	
	/** 種別
	 * 
	 *  カテゴリ　　　 ： false
	 *  実際の機能 : true
	 */
	private boolean m_type;

	/** オブジェクト種別 */
	private List<String> m_objectType;
	

	/**
	 * コンストラクタ
	 * 
	 * @param id ID
	 * @param name 機能名
	 * @param type 機能種別
	 */
	public FuncInfo(String id,
					String name,
					String defaultXml ,
					int weight ,
					String actionClassName,
					boolean type,
					String objectType) {
		setId(id);
		setName(name);
		m_defaultXml = new ArrayList<String>();
		setDefaultXML(defaultXml);
		setWeight(weight);
		setActionClassName(actionClassName);
		setType(type);
		setObjectType(objectType);
	}
	
	/**
	 * コンストラクタ (複数XML対応)
	 * 
	 * @param id ID
	 * @param name 機能名
	 * @param type 機能種別
	 */
	public FuncInfo(String id,
					String name,
					List<String> defaultXml,
					int weight ,
					String actionClassName,
					boolean type,
					List<String> objectType) {
		setId(id);
		setName(name);
		setDefaultXML(defaultXml);
		setWeight(weight);
		setActionClassName(actionClassName);
		setType(type);
		setObjectType(objectType);
	}
	
	
	
	/**
	 * IDを返す。<BR>
	 * 
	 * @return ID
	 */
	public String getId() {
		return m_id;
	}
	
	/**
	 * IDを設定する。<BR>
	 * 
	 * @param id ID
	 */
	public void setId(String id) {
		this.m_id = id;
	}
	
	/**
	 * 機能名を返す。<BR>
	 * 
	 * @return　機能名
	 */
	public String getName() {
		return m_name;
	}
	
	/**
	 *　機能名を設定する。<BR>
	 * 
	 * @param name 機能名
	 */
	public void setName(String name) {
		this.m_name = name;
	}
	
	/**
	 * 種別を返す。<BR>
	 * 
	 * @return 種別
	 * @see com.clustercontrol.bean.JobConstant
	 */
	public boolean getType() {
		return m_type;
	}
	
	/**
	 * 種別を設定する。<BR>
	 * 
	 * @param type 種別
	 * @see com.clustercontrol.bean.JobConstant
	 */
	public void setType(boolean type) {
		this.m_type = type;
	}
	

	/**
	 * 情報のクローンを作成する。<BR>
	 * @return ジョブ情報のクローン
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() {
		try {
			FuncInfo clone = (FuncInfo)super.clone();
					
			clone.setId(this.getId());
			clone.setName(this.getName());
			clone.setType(this.getType());
			
			return clone;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public List<String> getDefaultXML() {
		return m_defaultXml;
	}

	public void setDefaultXML(String defaultxml) {
		m_defaultXml = new ArrayList<String>();
		m_defaultXml.add(defaultxml);
	}
	
	public void setDefaultXML(List<String> defaultxml) {
		m_defaultXml = defaultxml;
	}

	public int getWeight() {
		return m_weight;
	}

	public void setWeight(int weight) {
		this.m_weight = weight;
	}
	
	public String getActionClassName() {
		return m_actionClassName;
	}
	
	public void setActionClassName(String name) {
		this.m_actionClassName = name;
	}

	public List<String> getTargetIdList() {
		return m_targetIdList;
	}
	
	public void setTargetIdList(List<String> targetIdList) {
		m_targetIdList = targetIdList;
	}

	public List<String> getObjectType() {
		return m_objectType;
	}

	public void setObjectType(String objectType) {
		m_objectType = new ArrayList<String>();
		if(objectType != null && !objectType.isEmpty()){
			m_objectType.add(objectType);
		}
	}
	
	public void setObjectType(List<String> objectType) {
		m_objectType = objectType;
	}
}