/*

Copyright (C) 2011 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.custom.session;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.CustomInvalid;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.jobmanagement.bean.MonitorJobEndNode;
import com.clustercontrol.jobmanagement.util.MonitorJobWorker;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.commons.bean.SettingUpdateInfo;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.custom.bean.CommandExecuteDTO;
import com.clustercontrol.custom.bean.CommandResultDTO;
import com.clustercontrol.custom.bean.Type;
import com.clustercontrol.custom.factory.RunCustom;
import com.clustercontrol.custom.factory.RunCustomString;
import com.clustercontrol.custom.factory.SelectCustom;
import com.clustercontrol.custom.util.CustomManagerUtil;

/**
 * カスタム監視を制御するSession Bean <BR>
 * 
 * @version 6.0.0
 * @since 4.0.0
 */
public class MonitorCustomControllerBean {

	private static Log m_log = LogFactory.getLog( MonitorCustomControllerBean.class );

	/**
	 * <注意！> このメソッドはAgentユーザ以外で呼び出さないこと！
	 * <注意！> キャッシュの都合上、Agentユーザ以外から呼び出すと、正常に動作しません。
	 * 
	 * 要求してきたエージェントに対して、コマンド監視として実行すべきコマンド実行情報を返す
	 * @param requestedFacilityId エージェントが対応するノードのfacilityId
	 * @return コマンド実行情報の一覧
	 * @throws CustomInvalid コマンド実行情報に不整合が見つかった場合
	 * @throws InvalidRole
	 * @throws HinemosUnknown 予期せぬエラーが発生した場合
	 * 
	 */
	public ArrayList<CommandExecuteDTO> getCommandExecuteDTO(String requestedFacilityId) throws CustomInvalid, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		ArrayList<CommandExecuteDTO> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectCustom selector = new SelectCustom();
			list = selector.getCommandExecuteDTO(requestedFacilityId);
			jtm.commit();
		} catch (CustomInvalid | InvalidRole | HinemosUnknown e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.warn("getCommandExecuteDTO() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}


	/**
	 * エージェントから送信されてきたコマンド実行結果に対して、閾値判定および結果生成を行う
	 * @param CommandResultDTO エージェントから送信されたコマンド実行結果
	 * @throws MonitorNotFound 送信されたコマンド監視設定がすでに存在しない場合
	 * @throws HinemosUnknown 予期せぬエラーが発生した場合
	 * @throws CustomInvalid 監視設定に不整合が存在する場合
	 * 
	 */
	public void evalCommandResult(List<CommandResultDTO> dtoList) throws HinemosUnknown, MonitorNotFound, CustomInvalid {
		JpaTransactionManager jtm = null;
		List<MonitorJobEndNode> monitorJobEndNodeList = new ArrayList<>();
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			for (CommandResultDTO dto : dtoList) {
				
				if (dto.getType() == Type.NUMBER){
					RunCustom runner = new RunCustom(dto);
					runner.monitor();
					if (runner.getMonitorJobEndNodeList() != null 
							&& runner.getMonitorJobEndNodeList().size() > 0) {
						monitorJobEndNodeList.addAll(runner.getMonitorJobEndNodeList());
					}
				}else if (dto.getType() == Type.STRING){
					RunCustomString runner = new RunCustomString(dto);
					runner.monitor();
					if (runner.getMonitorJobEndNodeList() != null 
							&& runner.getMonitorJobEndNodeList().size() > 0) {
						monitorJobEndNodeList.addAll(runner.getMonitorJobEndNodeList());
					}
				}else{
					m_log.warn("CustomResultDTO() type error " );
				}
				
			}
			jtm.commit();
		} catch (HinemosUnknown | MonitorNotFound | CustomInvalid e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.warn("CustomResultDTO() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		// 監視ジョブEndNode処理
		try {
			if (monitorJobEndNodeList != null && monitorJobEndNodeList.size() > 0) {
				for (MonitorJobEndNode monitorJobEndNode : monitorJobEndNodeList) {
					MonitorJobWorker.endMonitorJob(
							monitorJobEndNode.getRunInstructionInfo(),
							monitorJobEndNode.getMonitorTypeId(),
							monitorJobEndNode.getMessage(),
							monitorJobEndNode.getErrorMessage(),
							monitorJobEndNode.getStatus(),
							monitorJobEndNode.getEndValue());
				}
				// 接続中のHinemosAgentに対する更新通知
				SettingUpdateInfo.getInstance().setCustomMonitorUpdateTime(HinemosTime.currentTimeMillis());
				CustomManagerUtil.broadcastConfigured();
			}
		} catch (Exception e) {
			m_log.warn("CustomResultDTO() MonitorJobWorker.endMonitorJob() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}


	}

}
