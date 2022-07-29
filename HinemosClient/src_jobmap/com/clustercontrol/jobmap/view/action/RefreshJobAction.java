/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.view.action;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.jobmap.util.JobMapActionUtil;
import com.clustercontrol.jobmap.view.JobTreeView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * ジョブ[一覧]ビューの「キャンセル」を行うクライアント側アクションクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class RefreshJobAction  extends BaseAction {
	// ログ
	private static Log m_log = LogFactory.getLog( RefreshJobAction.class );

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		super.execute(event);
		
		JobTreeView view = JobMapActionUtil.getJobTreeView();
		if (view == null) {
			return null;
		}

		// 確認ダイアログを生成
		String message;
		if(JobEditStateUtil.existEditing()){
			message = Messages.getString("message.job.43") + "\n" +
					Messages.getString("message.job.30");
		}else{
			message = Messages.getString("message.job.30");
		}
		if (MessageDialog.openQuestion(
				null,
				Messages.getString("confirmed"),
				message)) {
			m_log.debug("RefreshJob start " + new Date());
			
			try {
				for (String managerName : JobEditStateUtil.getManagerList()) {
					JobEditState jobEditState = JobEditStateUtil.getJobEditState(managerName);
					JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
					for (JobInfoWrapper info : jobEditState.getLockedJobunitList()) {
						wrapper.releaseEditLock(info.getJobunitId(), jobEditState.getEditSession(info));
					}
				}
			} catch (InvalidRole e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} catch (Exception e) {
				m_log.warn("updateJobunitUpdateTime() : " + HinemosMessage.replace(e.getMessage()), e);
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}
			
			view.update();
			view.getJobMapTreeComposite().refresh();
			
			JobTreeView moduleRegistView = JobMapActionUtil.getJobTreeModuleRegistView();
			if (moduleRegistView != null) {
				moduleRegistView.update();
				moduleRegistView.getJobMapTreeComposite().redraw();
			}
			m_log.debug("RefreshJob end " + new Date());
		}
				
		return null;
	}
}