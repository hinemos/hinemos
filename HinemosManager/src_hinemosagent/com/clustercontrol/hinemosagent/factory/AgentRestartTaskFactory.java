/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hinemosagent.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.hinemosagent.bean.AgentRestartTaskParameter;
import com.clustercontrol.hinemosagent.bean.TopicInfo;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.plugin.api.AsyncTaskFactory;
import com.clustercontrol.repository.bean.AgentCommandConstant;
import com.clustercontrol.repository.util.QueryUtil;

/**
 * Hinemosエージェントへの再起動指示を行う非同期タスクです。
 * 
 * @since 6.2.0
 */
public class AgentRestartTaskFactory implements AsyncTaskFactory {

	public static final Log log = LogFactory.getLog(AgentRestartTaskFactory.class);

	@Override
	public Runnable createTask(Object param) {
		// パラメータの型が特定のものでない(通常はプログラムエラー)場合は、nullをタスクに渡す。
		// createTask の呼び出し元で無効なパラメータのハンドリングを行っていないので、いったんタスクは作っておき、タスク実行時に無視する。
		if (param instanceof AgentRestartTaskParameter) {
			return new AgentRestartTask((AgentRestartTaskParameter) param);
		} else {
			log.warn("createTask: Invalid type = " + (param == null ? "null" : param.getClass().getName()));
			return new AgentRestartTask(null);
		}
	}

	public static class AgentRestartTask implements Runnable {
		private final AgentRestartTaskParameter param;

		// 外部依存動作をモックへ置換できるように分離
		private External external;
		static class External {
			boolean isValidAgent(String facilityId) {
				return AgentConnectUtil.isValidAgent(facilityId);
			}
			
			void checkObjectPrivilege(String facilityId) throws FacilityNotFound, InvalidRole {
				QueryUtil.getFacilityPK(facilityId, ObjectPrivilegeMode.EXEC);
			}
			
			void setTopic(String facilityId, TopicInfo topicInfo) {
				AgentConnectUtil.setTopic(facilityId, topicInfo);
			}
			
			int getSleepTime() {
				return HinemosPropertyCommon.repository_restart_sleep.getIntegerValue();
			}
		}
		
		public AgentRestartTask(AgentRestartTaskParameter param) {
			this(new External(), param);
		}

		AgentRestartTask(External external, AgentRestartTaskParameter param) {
			this.external = external;
			this.param = param;
		}

		public AgentRestartTaskParameter getParameter() {
			return param;
		}

		@Override
		public void run() {
			// 無効なパラメータを渡された場合は何もしない。
			if (param == null) {
				log.warn("run: Parameter is invalid.");
				return;
			}
			
			String facilityId = param.getFacilityId();

			// エージェントが無効になっていたら何もしない。
			if (!external.isValidAgent(facilityId)) {
				log.info("run: Agent is invalid. facilityId=" + facilityId);
				return;
			}
				
			// オブジェクト権限がない場合、当該ノード情報が存在しない場合は、何もしない。
			try {
				external.checkObjectPrivilege(facilityId);
			} catch (InvalidRole e) {
				log.info("run: Object privilege EXEC not assigned. facilityId=" + facilityId);
				return;
			} catch (FacilityNotFound e) {
				log.info("run: Facility not found. facilityId=" + facilityId);
				return;
			}

			// 再起動topicを積む
			log.info("run: Restart. facilityId=" + facilityId);
			TopicInfo topicInfo = new TopicInfo();
			topicInfo.setAgentCommand(AgentCommandConstant.RESTART);
			external.setTopic(facilityId, topicInfo);

			// 次のタスク実行までsleep
			try {
				Thread.sleep(external.getSleepTime());
			} catch (InterruptedException e) {
				log.info("run: Sleep was interrupted. cause=" + e.getMessage());
			}
		}
	}

}
