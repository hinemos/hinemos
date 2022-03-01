/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.GetRpaScenarioListRequest;
import org.openapitools.client.model.RpaToolResponse;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.bean.MonitorFilterConstant;
import com.clustercontrol.rpa.bean.RpaScenarioFilterConstant;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.util.RestConnectManager;

/**
 * RPAシナリオ実績[シナリオ一覧]のフィルタダイアログに関するutilityクラス
 */
public class RpaScenarioFilterPropertyUtil {

	// ログ
	private static Log log = LogFactory.getLog( RpaScenarioFilterPropertyUtil.class );

	/**
	 * プロパティをRPAシナリオフィルタDTOに変換するメソッドです。
	 */
	public static GetRpaScenarioListRequest property2dto(Property property){
		GetRpaScenarioListRequest info = new GetRpaScenarioListRequest();

		String rpaToolId = null;								// RPAツールID
		String scenarioId = null;								// シナリオID
		String scenarioName = null; 							// シナリオ名
		String scenarioIdentifyString = null;					// シナリオ識別子
		String scenarioOperationResultCreateSettingId = null;	// シナリオ実績作成設定ID
		String ownerRoleId = null;								// オーナーロールID

		ArrayList<?> values = null;

		//RPAツールID
		values = PropertyUtil.getPropertyValue(property,
				RpaScenarioFilterConstant.RPA_TOOL_ID);
		if (!"".equals(values.get(0))) {
			rpaToolId = (String) values.get(0);
			info.setRpaToolId(rpaToolId);
		}

		//シナリオID
		values = PropertyUtil.getPropertyValue(property,
				RpaScenarioFilterConstant.SCENARIO_ID);
		if (!"".equals(values.get(0))) {
			scenarioId = (String) values.get(0);
			info.setScenarioId(scenarioId);
		}

		//シナリオ名
		values = PropertyUtil.getPropertyValue(property,
				RpaScenarioFilterConstant.SCENARIO_NAME);
		if (!"".equals(values.get(0))) {
			scenarioName = (String) values.get(0);
			info.setScenarioName(scenarioName);
		}

		//シナリオ識別子
		values = PropertyUtil.getPropertyValue(property,
				RpaScenarioFilterConstant.SCENARIO_IDENTIFY_STRING);
		if (!"".equals(values.get(0))) {
			scenarioIdentifyString = (String) values.get(0);
			info.setScenarioIdentifyString(scenarioIdentifyString);
		}

		//シナリオ実績作成設定ID
		values = PropertyUtil.getPropertyValue(property,
				RpaScenarioFilterConstant.SCENARIO_OPERATION_RESULT_CREATE_SETTING_ID);
		if (!"".equals(values.get(0))) {
			scenarioOperationResultCreateSettingId = (String) values.get(0);
			info.setScenarioOperationResultCreateSettingId(scenarioOperationResultCreateSettingId);
		}

		//オーナーロールID
		values = PropertyUtil.getPropertyValue(property,
				MonitorFilterConstant.OWNER_ROLE_ID);
		if (!"".equals(values.get(0))) {
			ownerRoleId = (String) values.get(0);
			info.setOwnerRoleId(ownerRoleId);
		}

		return info;
	}

	/**
	 * RPAシナリオ用フィルタプロパティを取得します。<BR>
	 *
	 * @param locale
	 * @return
	 */
	public static Property getProperty(Locale locale) {

		//マネージャ
		Property manager =
				new Property(RpaScenarioFilterConstant.MANAGER, Messages.getString("facility.manager", locale), PropertyDefineConstant.EDITOR_SELECT);

		//RPAツールID
		Property rpaToolId =
				new Property(RpaScenarioFilterConstant.RPA_TOOL_ID, Messages.getString("rpa.tool", locale), PropertyDefineConstant.EDITOR_SELECT);

		//シナリオID
		Property scenarioId =
				new Property(RpaScenarioFilterConstant.SCENARIO_ID, Messages.getString("rpa.scenario.id", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);

		//シナリオ名
		Property scenarioName =
				new Property(RpaScenarioFilterConstant.SCENARIO_NAME, Messages.getString("rpa.scenario.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);

		//シナリオ識別子
		Property scenarioIdentifyString =
				new Property(RpaScenarioFilterConstant.SCENARIO_IDENTIFY_STRING, Messages.getString("rpa.scenario.identify.string", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);

		//シナリオ実績作成設定ID
		Property scenarioOperationResultCreateSettingId =
				new Property(RpaScenarioFilterConstant.SCENARIO_OPERATION_RESULT_CREATE_SETTING_ID, Messages.getString("rpa.scenario.operation.result.create.setting.id", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);

		//オーナーロールID
		Property ownerRoleId =
				new Property(RpaScenarioFilterConstant.OWNER_ROLE_ID, Messages.getString("owner.role.id", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);

		Object[] obj = RestConnectManager.getActiveManagerSet().toArray();
		Object[] val = new Object[obj.length + 1];
		val[0] = "";
		for(int i = 0; i<obj.length; i++) {
			val[i + 1] = obj[i];
		}

		Object[][] managerValues = {val, val};
		manager.setSelectValues(managerValues);
		manager.setValue("");

		rpaToolId.setSelectValues(getRpaToolNameList());
		rpaToolId.setValue("");
		scenarioId.setValue("");
		scenarioName.setValue("");
		scenarioIdentifyString.setValue("");
		scenarioOperationResultCreateSettingId.setValue("");
		ownerRoleId.setValue("");

		//変更の可/不可を設定
		manager.setModify(PropertyDefineConstant.MODIFY_OK);
		rpaToolId.setModify(PropertyDefineConstant.MODIFY_OK);
		scenarioId.setModify(PropertyDefineConstant.MODIFY_OK);
		scenarioName.setModify(PropertyDefineConstant.MODIFY_OK);
		scenarioIdentifyString.setModify(PropertyDefineConstant.MODIFY_OK);
		scenarioOperationResultCreateSettingId.setModify(PropertyDefineConstant.MODIFY_OK);
		ownerRoleId.setModify(PropertyDefineConstant.MODIFY_OK);

		Property property = new Property(null, null, "");

		// 初期表示ツリーを構成。
		property.removeChildren();
		property.addChildren(manager);
		property.addChildren(rpaToolId);
		property.addChildren(scenarioId);
		property.addChildren(scenarioName);
		property.addChildren(scenarioIdentifyString);
		property.addChildren(scenarioOperationResultCreateSettingId);
		property.addChildren(ownerRoleId);

		return property;
	}

	/**
	 * RPAツール名のリストを配列で返却する
	 * @return
	 */
	private static Object[][] getRpaToolNameList() {

		List<RpaToolResponse> rpaToolList = new ArrayList<>();
		Object retArray[][] = null;
		try{
			for(String managerName : RestConnectManager.getActiveManagerSet()) {
				RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(managerName);
				rpaToolList = wrapper.getRpaTool();
			}
		} catch (InvalidUserPass e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (InvalidRole e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (HinemosUnknown | RestConnectFailed e) {
			log.warn("getCalendarIdList(), " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		if(rpaToolList != null && rpaToolList.isEmpty() == false)
		{
			retArray = new Object[2][rpaToolList.size()+1];
			retArray[0][0] = "";
			retArray[1][0] = "";
			for (int i = 0; i < rpaToolList.size(); i++){
				retArray[0][i+1] = rpaToolList.get(i).getRpaToolId();
				retArray[1][i+1] = rpaToolList.get(i).getRpaToolName();
			}
		}
		else{
			Object nullArray[][] = {
					{ "" },
					{ "" }
			};
			retArray = nullArray;
		}
		return retArray;
	}
}
