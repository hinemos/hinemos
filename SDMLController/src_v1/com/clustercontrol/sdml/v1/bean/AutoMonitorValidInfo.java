/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.v1.bean;

import com.clustercontrol.sdml.model.SdmlControlMonitorRelation;
import com.clustercontrol.sdml.v1.constant.SdmlMonitorTypeEnum;

/**
 * SDML監視種別に対する監視・収集の有効無効を保持するクラス
 *
 */
public class AutoMonitorValidInfo {
	private String monitorId;
	private SdmlMonitorTypeEnum sdmlMonitorType;
	private boolean monitorFlg;
	private boolean collectorFlg;
	private int number;
	private String subType;

	public AutoMonitorValidInfo(SdmlMonitorTypeEnum sdmlMonitorType, boolean monitorFlg, boolean collectorFlg) {
		this.sdmlMonitorType = sdmlMonitorType;
		this.monitorFlg = monitorFlg;
		this.collectorFlg = collectorFlg;
		this.number = 0;
		this.subType = null;
	}

	public AutoMonitorValidInfo(SdmlMonitorTypeEnum sdmlMonitorType, boolean monitorFlg, boolean collectorFlg,
			int number, String subType) {
		this.sdmlMonitorType = sdmlMonitorType;
		this.monitorFlg = monitorFlg;
		this.collectorFlg = collectorFlg;
		this.number = number;
		this.subType = subType;
	}

	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public SdmlMonitorTypeEnum getSdmlMonitorType() {
		return sdmlMonitorType;
	}

	public String getSdmlMonitorTypeId() {
		return sdmlMonitorType.getId();
	}

	public boolean isMonitorFlg() {
		return monitorFlg;
	}

	public boolean isCollectorFlg() {
		return collectorFlg;
	}

	public int getNumber() {
		return number;
	}

	public String getSubType() {
		return subType;
	}

	/**
	 * DBに登録されている監視設定との関連情報から同一の監視設定に対する情報か検証する<BR>
	 * 
	 * @param relation
	 * @return
	 */
	public boolean equalTo(SdmlControlMonitorRelation relation) {
		// 監視項目IDは作成日時によって変わるためSDML監視種別とサブ種別で検証する
		if (!relation.getSdmlMonitorTypeId().equals(sdmlMonitorType.getId())) {
			return false;
		}
		// サブ種別を持たない種別はnullになる
		if (relation.getSubType() == null && subType == null) {
			return true;
		}
		if ((relation.getSubType() == null && subType != null) || (relation.getSubType() != null && subType == null)) {
			return false;
		}
		return relation.getSubType().equals(subType);
	}
}
