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

	private String collectorId;   //収集ID
	private String itemCode;      //収集項目コード
	private String displayName;  //リポジトリ表示名 (Ver3.1.0)より追加

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
		this.collectorId = collectorId;
		this.itemCode = itemCode;
		this.displayName = displayName;
	}

	/**
	 * 収集IDを取得します。
	 * @return 収集ID
	 */
	public String getCollectorId() {
		return collectorId;
	}
	/**
	 * 収集IDを設定します。
	 * @param collectorId 収集ID
	 */
	public void setCollectorId(String collectorId) {
		this.collectorId = collectorId;
	}
	/**
	 * 収集項目コードを取得します。
	 * @return 収集項目コード
	 */
	public String getItemCode() {
		return itemCode;
	}
	/**
	 * 収集項目コードを設定します。
	 * @param itemCode 収集項目コード
	 */
	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}
	/**
	 * リポジトリ表示名を取得します。
	 * @return リポジトリ表示名
	 */
	public String getDisplayName() {
		return displayName;
	}
	/**
	 * リポジトリ表示名を設定します。
	 * @param name リポジトリ表示名
	 */
	public void setDisplayName(String name) {
		this.displayName = name;
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

			if( this.collectorId == null )
			{
				lEquals = lEquals && ( lTest.collectorId == null );
			}
			else
			{
				lEquals = lEquals && this.collectorId.equals( lTest.collectorId );
			}
			if( this.itemCode == null )
			{
				lEquals = lEquals && ( lTest.itemCode == null );
			}
			else
			{
				lEquals = lEquals && this.itemCode.equals( lTest.itemCode );
			}
			if( this.displayName == null )
			{
				lEquals = lEquals && ( lTest.displayName == null );
			}
			else
			{
				lEquals = lEquals && this.displayName.equals( lTest.displayName );
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

		result = 37*result + ((this.collectorId != null) ? this.collectorId.hashCode() : 0);

		result = 37*result + ((this.itemCode != null) ? this.itemCode.hashCode() : 0);

		result = 37*result + ((this.displayName != null) ? this.displayName.hashCode() : 0);

		return result;
	}
}