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
import com.clustercontrol.hinemosagent.bean.AgentUpdateTaskParameter;
import com.clustercontrol.hinemosagent.bean.TopicInfo;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.hinemosagent.util.AgentUpdateList;
import com.clustercontrol.plugin.api.AsyncTaskFactory;
import com.clustercontrol.repository.bean.AgentCommandConstant;
import com.clustercontrol.repository.util.QueryUtil;
import com.clustercontrol.util.Singletons;

/**
 * Hinemosエージェントへのアップデート指示を行う非同期タスクです。
 * 
 * @since 6.2.0
 */
public class AgentUpdateTaskFactory implements AsyncTaskFactory {

	public static final Log log = LogFactory.getLog(AgentUpdateTaskFactory.class);

	@Override
	public Runnable createTask(Object param) {
		// パラメータの型が特定のものでない(通常はプログラムエラー)場合は、nullをタスクに渡す。
		// createTask の呼び出し元で無効なパラメータのハンドリングを行っていないので、いったんタスクは作っておき、タスク実行時に無視する。
		if (param instanceof AgentUpdateTaskParameter) {
			return new AgentUpdateTask((AgentUpdateTaskParameter) param);
		} else {
			log.warn("createTask: Invalid type = " + (param == null ? "null" : param.getClass().getName()));
			return new AgentUpdateTask(null);
		}
	}

	public static class AgentUpdateTask implements Runnable {
		private final AgentUpdateTaskParameter param;

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
			
			int getLoopInterval() {
				return HinemosPropertyCommon.repository_agentupdate_interval.getIntegerValue();
			}
		}
		
		public AgentUpdateTask(AgentUpdateTaskParameter param) {
			this(new External(), param);
		}

		AgentUpdateTask(External external, AgentUpdateTaskParameter param) {
			this.external = external;
			this.param = param;
		}

		public AgentUpdateTaskParameter getParameter() {
			return param;
		}
		
		@Override
		public void run() {
			// 無効なパラメータを渡された場合は何もしない。
			if (param == null) {
				log.warn("run: Parameter is invalid.");
				return;
			}
			log.debug("run: " + param.getFacilityId());
			
			String facilityId = param.getFacilityId();

			// 空きが出るまで待機する
			AgentUpdateList updateList = Singletons.get(AgentUpdateList.class);
			loop: while (true) {
				// 空きを獲得 (switchにすることで、enumが増えてcaseを加え忘れたとしても、コンパイラ警告が出る)
				switch (updateList.tryAcquire(facilityId)) {
				case ACQUIRED:
					break loop;
				case REJECTED:
					break;
				case ALREADY_EXISTS:
					log.info("run: Already updating. facilityId=" + facilityId);
					return;
				}

				// 待機sleep
				try {
					Thread.sleep(external.getLoopInterval());
				} catch (InterruptedException e) {
					// ignore
				}
			}

			// エージェントが無効になっていたら何もしない。
			if (!external.isValidAgent(facilityId)) {
				log.info("run: Agent is invalid. facilityId=" + facilityId);
				updateList.release(facilityId);
				return;
			}
				
			// オブジェクト権限がない場合、当該ノード情報が存在しない場合は、何もしない。
			try {
				external.checkObjectPrivilege(facilityId);
			} catch (InvalidRole e) {
				log.info("run: Object privilege EXEC not assigned. facilityId=" + facilityId);
				updateList.release(facilityId);
				return;
			} catch (FacilityNotFound e) {
				log.info("run: Facility not found. facilityId=" + facilityId);
				updateList.release(facilityId);
				return;
			}

			// アップデートtopicを積む
			log.info("run: Update. facilityId=" + facilityId);
			TopicInfo topicInfo = new TopicInfo();
			topicInfo.setAgentCommand(AgentCommandConstant.UPDATE);
			external.setTopic(facilityId, topicInfo);
		}
	}

}
