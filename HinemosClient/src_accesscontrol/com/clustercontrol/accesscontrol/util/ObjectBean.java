/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.util;

/**
 * オブジェクト権限情報(オブジェクト種別、オブジェクトID)をまとめたクラス
 *
 */


public class ObjectBean{
	private String m_managerName = "";
	private String m_objectType = "";
	private String m_objectId = "";
	private String m_objectIdForDisplay = ""; // objectId をそのまま表示しない場合に使用する
	private String m_objectIdLabelForDisplay = ""; // オブジェクト種別とID名が異なる場合に表示用に設定するラベル

	public ObjectBean() {
	}

	public ObjectBean(String managerName, String objectType, String objectId) {
		this(managerName, objectType, objectId, objectId);
	}

	public ObjectBean(String managerName, String objectType, String objectId, String objectIdForDisplay) {
		this(managerName, objectType, objectId, objectIdForDisplay, null);
	}

	public ObjectBean(String managerName, String objectType, String objectId, String objectIdForDisplay,
			String objectIdLabelForDisplay) {
		this.m_managerName = managerName;
		this.m_objectType = objectType;
		this.m_objectId = objectId;
		this.m_objectIdForDisplay = objectIdForDisplay;
		this.m_objectIdLabelForDisplay = objectIdLabelForDisplay;
	}

	// オブジェクト種別
	public String getObjectType() {
		return m_objectType;
	}

	public void setObjectType(String objectType) {
		this.m_objectType = objectType;
	}

	// オブジェクトID
	public String getObjectId() {
		return m_objectId;
	}

	public void setObjectId(String objectId) {
		this.m_objectId = objectId;
	}

	/**
	 * オブジェクトIDをユーザへ向けて表示する際に使用すべき文字列を返します。
	 * <p>
	 * 通常はオブジェクトIDそのものですが、オブジェクトIDが特殊フォーマットである一部の機能(フィルタ設定)では
	 * よりユーザフレンドリーな文字列が設定されます。
	 */
	public String getObjectIdForDisplay() {
		return m_objectIdForDisplay;
	}

	public void setObjectIdForDisplay(String objectIdForDisplay) {
		this.m_objectIdForDisplay = objectIdForDisplay;
	}

	/**
	 * @return the m_managerName
	 */
	public String getManagerName() {
		return m_managerName;
	}

	/**
	 * @param m_managerName the m_managerName to set
	 */
	public void setManagerName(String m_managerName) {
		this.m_managerName = m_managerName;
	}

	/**
	 * オブジェクトIDのラベル表示でオブジェクト種別の名称とID名が異なる場合に使用します。<BR>
	 * 
	 * 画面側の表示では「{オブジェクト種別}＋"ID"」のようにラベルを生成しており、<BR>
	 * 多くの設定の場合問題になりませんが、（例. 通知 - 通知ID）<BR>
	 * まれに名称が異なるケースがありそのために使用します。（例. SDML制御 - アプリケーションID）<BR>
	 * 
	 * @return
	 */
	public String getObjectIdLabelForDisplay() {
		return m_objectIdLabelForDisplay;
	}

	public void setObjectIdLabelForDisplay(String objectIdLabelForDisplay) {
		this.m_objectIdLabelForDisplay = objectIdLabelForDisplay;
	}
}
