/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.jobmanagement.bean.JobSessionRequestMessage;
import com.clustercontrol.jobmanagement.bean.JobTriggerInfo;
import com.clustercontrol.jobmanagement.bean.JobTriggerTypeConstant;
import com.clustercontrol.jobmanagement.session.JobRunManagementBean;
import com.clustercontrol.jobmanagement.util.JobFileCheckDuplicationGuard;
import com.clustercontrol.plugin.api.AsyncTaskFactory;

public class CreateJobSessionTaskFactory implements AsyncTaskFactory {

	public static final Log log = LogFactory.getLog(CreateJobSessionTaskFactory.class);

	@Override
	public Runnable createTask(Object param) {
		// ファイルチェック契機なら、ジョブ重複実行ガード機構へ一時情報を追加する。
		if (param instanceof JobSessionRequestMessage) {
			JobSessionRequestMessage msg = (JobSessionRequestMessage) param;
			JobTriggerInfo trigger = msg.getTriggerInfo();
			if (trigger.getTrigger_type() == JobTriggerTypeConstant.TYPE_FILECHECK) {
				JobFileCheckDuplicationGuard.putTransitTriggerInfo(trigger.getTrigger_info(), msg.getSessionId());
			}
			// 上記処理は、"起動時の永続化されたタスクの復元時"と、"オンライン中でのタスク追加時"の双方で実行される可能性があるが、
			// オンライン中においては以下の条件下で実行する必要がある。
			// - AgentEndpoint.jobFileCheckResult を処理しているスレッドである。
			// - JobFileCheckDuplicationGuard のロック中である。
			// 非同期の仕組みを新しく追加するなどで、実行スレッドが変更になる場合は、注意する。
			// なお、対応する情報の除去処理は CreateJobSessionTask.run にある。
		}

		return new CreateJobSessionTask(param);
	}

	public static class CreateJobSessionTask implements Runnable {

		private final JobSessionRequestMessage msg;

		public CreateJobSessionTask(Object param) {
			if (param instanceof JobSessionRequestMessage) {
				msg = (JobSessionRequestMessage)param;
			} else {
				msg = null;
			}
		}

		@Override
		public void run() {

			if (msg == null) {
				log.warn("message is not assigned.");
				return;
			}
			log.debug("run() message : " + msg);

			JobRunManagementBean.makeSession(msg);

			// ファイルチェック契機なら、ジョブ重複実行ガード機構が持つ一時情報を除去する。
			// なお、対応する"一時情報の追加処理"は CreateJobSessionTaskFactory.createTask にある。
			JobTriggerInfo trigger = msg.getTriggerInfo();
			if (trigger.getTrigger_type() == JobTriggerTypeConstant.TYPE_FILECHECK) {
				JobFileCheckDuplicationGuard.removeTransitTriggerInfo(trigger.getTrigger_info());
			}
		}

		@Override
		public String toString() {
			if (msg == null) {
				return this.getClass().getSimpleName() + "[CreateJobSessionRequestMessage=null]";
			} else {
				return this.getClass().getSimpleName() + "[CreateJobSessionRequestMessage=" + msg
						+ "[" + msg + "]]";
			}
		}
	}

}
