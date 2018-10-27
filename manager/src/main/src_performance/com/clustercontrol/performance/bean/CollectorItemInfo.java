/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.performance.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * 収集項目の設定情報を保持するDTOクラス<p>
 * 
 * 収集設定には、全般の情報、収集項目に関する情報、閾値監視に関する情報があり、
 * そのうち、収集項目に関する情報を保持また、クライアントマネージャ間でやり取りをする
 * ためのクラスです。
 * 
 * @version 4.0.0
 * @since 1.0.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class CollectorItemInfo implements Serializable {
	private static final long serialVersionUID = 4107793107236713239L;

	private String m_collectorId;   //収集ID
	private String m_itemCode;      //収集項目コード
	private String m_displayName;  //リポジトリ表示名 (Ver3.1.0)より追加

	public CollectorItemInfo(){
		super();
	}

	/**
	 * 指定された値を保持した CollectorItemInfo オブジェクトを生成します。
	 * 
	 * @param collectMethod 収集方法
	 * @param platformId プラットフォーム
	 * @param itemCode 収集項目コード
	 * @param displayName リポジトリ表示名
	 */
	public CollectorItemInfo(
			final String collectorId,
			final String itemCode,
			final String displayName){
		m_collectorId = collectorId;
		m_itemCode = itemCode;
		m_displayName = displayName;
	}

	/**
	 * 収集IDを取得します。
	 * @return 収集ID
	 */
	public String getCollectorId() {
		return m_collectorId;
	}
	/**
	 * 収集IDを設定します。
	 * @param collectorId 収集ID
	 */
	public void setCollectorId(String collectorId) {
		m_collectorId = collectorId;
	}
	/**
	 * 収集項目コードを取得します。
	 * @return 収集項目コード
	 */
	public String getItemCode() {
		return m_itemCode;
	}
	/**
	 * 収集項目コードを設定します。
	 * @param itemCode 収集項目コード
	 */
	public void setItemCode(String itemCode) {
		m_itemCode = itemCode;
	}
	/**
	 * リポジトリ表示名を取得します。
	 * @return リポジトリ表示名
	 */
	public String getDisplayName() {
		return m_displayName;
	}
	/**
	 * リポジトリ表示名を設定します。
	 * @param name リポジトリ表示名
	 */
	public void setDisplayName(String name) {
		m_displayName = name;
	}

	/**
	 * このオブジェクトと他のオブジェクトが等しいかどうかを示します。
	 */
	@Override
	public boolean equals( Object pOther )
	{
		if( pOther instanceof CollectorItemInfo )
		{
			CollectorItemInfo lTest = (CollectorItemInfo) pOther;
			boolean lEquals = true;

			if( this.m_collectorId == null )
			{
				lEquals = lEquals && ( lTest.m_collectorId == null );
			}
			else
			{
				lEquals = lEquals && this.m_collectorId.equals( lTest.m_collectorId );
			}
			if( this.m_itemCode == null )
			{
				lEquals = lEquals && ( lTest.m_itemCode == null );
			}
			else
			{
				lEquals = lEquals && this.m_itemCode.equals( lTest.m_itemCode );
			}
			if( this.m_displayName == null )
			{
				lEquals = lEquals && ( lTest.m_displayName == null );
			}
			else
			{
				lEquals = lEquals && this.m_displayName.equals( lTest.m_displayName );
			}

			return lEquals;
		}
		else
		{
			return false;
		}
	}

	/**
	 * オブジェクトのハッシュコード値を返します。
	 */
	@Override
	public int hashCode()
	{
		int result = 17;

		result = 37*result + ((this.m_collectorId != null) ? this.m_collectorId.hashCode() : 0);

		result = 37*result + ((this.m_itemCode != null) ? this.m_itemCode.hashCode() : 0);

		result = 37*result + ((this.m_displayName != null) ? this.m_displayName.hashCode() : 0);

		return result;
	}
}