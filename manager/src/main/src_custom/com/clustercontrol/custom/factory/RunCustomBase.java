/*


This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.custom.factory;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.custom.bean.CommandResultDTO;
import com.clustercontrol.fault.CustomInvalid;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.jobmanagement.bean.MonitorJobEndNode;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;

/**
 * コマンド監視の監視処理基底クラス<br/>
 * 
 * @version 6.0.0
 * @since 6.0.0
 */
public abstract class RunCustomBase {

	private static Log m_log = LogFactory.getLog(RunCustom.class);;
	protected CommandResultDTO result = null;
	protected List<MonitorJobEndNode> monitorJobEndNodeList = new ArrayList<>(); 
	
	/**
	 * 閾値判定を行い、監視結果を通知する。<br/>
	 * 
	 * @throws HinemosUnknown
	 *             予期せぬ内部エラーが発生した場合
	 * @throws MonitorNotFound
	 *             該当する監視設定が存在しない場合
	 * @throws CustomInvalid
	 *             監視設定に不整合が存在する場合
	 */
	public abstract void monitor() throws HinemosUnknown, MonitorNotFound, CustomInvalid;

	/**
	 * 通知機能に対して、コマンド監視の結果を通知する。<br/>
	 * 
	 * @param priority
	 *            監視結果の重要度(PriorityConstant.INFOなど)
	 * @param monitor
	 *            コマンド監視に対応するMonitorInfo
	 * @param facilityId
	 *            監視結果に対応するファシリティID
	 * @param facilityPath
	 *            ファシリティIDに対応するパス文字列
	 * @param msg
	 *            監視結果に埋め込むメッセージ
	 * @param msgOrig
	 *            監視結果に埋め込むオリジナルメッセージ
	 * @param pluginID
	 *            PluginID
	 * @throws HinemosUnknown
	 *             予期せぬ内部エラーが発生した場合
	 * @throws CustomInvalid
	 *             監視設定に不整合が存在する場合
	 */
	protected void notify(int priority, MonitorInfo monitor, String facilityId, String facilityPath, String subKey,
			String msg, String msgOrig, String pluginID) throws HinemosUnknown, CustomInvalid {
		// Local Variable
		OutputBasicInfo notifyInfo = null;

		notifyInfo = new OutputBasicInfo();
		notifyInfo.setMonitorId(monitor.getMonitorId());
		notifyInfo.setPluginId(pluginID);
		// デバイス名単位に通知抑制されるよう、抑制用サブキーを設定する。
		notifyInfo.setSubKey(subKey == null ? "" : subKey);
		notifyInfo.setPriority(priority);
		notifyInfo.setApplication(monitor.getApplication());
		notifyInfo.setFacilityId(facilityId);
		notifyInfo.setScopeText(facilityPath);
		notifyInfo.setGenerationDate(result.getCollectDate());
		notifyInfo.setMessage(msg);
		notifyInfo.setMessageOrg(msgOrig);

		try {
			// 通知処理
			new NotifyControllerBean().notify(notifyInfo, NotifyGroupIdGenerator.generate(monitor));
		} catch (Exception e) {
			m_log.warn("notify() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown("unexpected internal failure occurred. [" + result + "]", e);
		}
	}

	/**
	 * 監視ジョブの実行結果を返す
	 * 
	 * @return 監視ジョブの実行結果
	 */
	public List<MonitorJobEndNode> getMonitorJobEndNodeList() {
		return this.monitorJobEndNodeList;
	}

}
