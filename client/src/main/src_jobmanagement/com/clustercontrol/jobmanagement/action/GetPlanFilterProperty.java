/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.action;

import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.jobmanagement.bean.HistoryFilterPropertyConstant;
import com.clustercontrol.jobmanagement.bean.PlanFilterPropertyConstant;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.Messages;

/**
 * ジョブ[スケジュール予定]ビューのフィルタ用プロパティを取得するクライアント側アクションクラス<BR>
 *
 * マネージャにSessionBean経由でアクセスし、フィルタ用プロパティを取得する
 *
 * @version 4.1.0
 * @since 4.1.0
 */
public class GetPlanFilterProperty {
	/**
	 * マネージャにSessionBean経由でアクセスし、スケジュール予定フィルタ用プロパティを取得する
	 *
	 * @return スケジュール予定フィルタ用プロパティ
	 *
	 */
	public Property getProperty() {

		//マネージャ
		Property m_manager = new Property(HistoryFilterPropertyConstant.MANAGER,
				Messages.getString("facility.manager"), PropertyDefineConstant.EDITOR_SELECT);
		//開始
		Property m_fromDate = new Property(PlanFilterPropertyConstant.FROM_DATE,
				Messages.getString("start"), PropertyDefineConstant.EDITOR_DATETIME);
		//終了
		Property m_toDate = new Property(PlanFilterPropertyConstant.TO_DATE,
				Messages.getString("end"), PropertyDefineConstant.EDITOR_DATETIME);
		//含む - 実行契機ID
		Property m_jobkickId = new Property(PlanFilterPropertyConstant.JOBKICK_ID,
				Messages.getString("jobkick.id"), PropertyDefineConstant.EDITOR_TEXT);

		Object[] obj = EndpointManager.getActiveManagerSet().toArray();
		Object[] val = new Object[obj.length + 1];
		val[0] = "";
		for(int i = 0; i<obj.length; i++) {
			val[i + 1] = obj[i];
		}

		Object[][] managerValues = {val, val};
		m_manager.setSelectValues(managerValues);
		m_manager.setValue("");

		m_fromDate.setValue("");
		m_toDate.setValue("");
		m_jobkickId.setValue("");

		//変更の可/不可を設定
		m_manager.setModify(PropertyDefineConstant.MODIFY_OK);
		m_fromDate.setModify(PropertyDefineConstant.MODIFY_OK);
		m_toDate.setModify(PropertyDefineConstant.MODIFY_OK);
		m_jobkickId.setModify(PropertyDefineConstant.MODIFY_OK);

		//文字数上限制限
		m_jobkickId.setStringUpperValue(1000);

		Property property = new Property(null, null, "");

		// 初期表示ツリーを構成。
		property.removeChildren();
		property.addChildren(m_manager);
		property.addChildren(m_fromDate);
		property.addChildren(m_toDate);
		property.addChildren(m_jobkickId);

		return property;
	}
}
