/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.view.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.jobmanagement.action.GetJobKickTableDefine;
import com.clustercontrol.jobmanagement.bean.JobKickConstant;
import com.clustercontrol.jobmanagement.bean.JobKickTypeMessage;
import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.jobmanagement.view.JobKickListView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;

/**
 * ジョブ[実行契機]ビューの「削除」のクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class DeleteJobKickAction extends AbstractHandler implements IElementUpdater {

	// ログ
	private static Log m_log = LogFactory.getLog( DeleteJobKickAction.class );

	/** アクションID */
	public static final String ID = DeleteJobKickAction.class.getName();
	private IWorkbenchWindow window;
	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
		this.window = null;
	}

	/**
	 * ジョブ[実行契機]ビューの「削除」が押された場合に、
	 * 実行契機を削除します。
	 * <p>
	 * <ol>
	 * <li>ジョブ[実行契機]ビューから選択された実行契機を取得します。</li>
	 * <li>削除の確認ダイアログを表示します。</li>
	 * <li>実行契機を削除します。</li>
	 * <li>ジョブ[実行契機]ビューを更新します。</li>
	 * </ol>
	 *
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == this.window || !isEnabled() ){
			return null;
		}

		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		if (!(viewPart instanceof JobKickListView)) {
			return null;
		}

		JobKickListView jobKickListView = null;
		try {
			jobKickListView = (JobKickListView) viewPart.getAdapter(JobKickListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (jobKickListView == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		StructuredSelection selection = (StructuredSelection) jobKickListView
				.getComposite().getTableViewer().getSelection();

		List<?> list = (List<?>)selection.toList();

		Map<String, List<String>[]> jobKickIdMap = new ConcurrentHashMap<>();

		String scheId = null;
		String fileId = null;
		String manId = null;
		int scheSize = 0;
		int fileCheckSize = 0;
		int manualSize = 0;

		for(Object obj : list) {
			List<?> objList = (List<?>)obj;
			String managerName = (String) objList.get(GetJobKickTableDefine.MANAGER_NAME);
			if(jobKickIdMap.get(managerName) == null) {
				jobKickIdMap.put(managerName, new ArrayList[3]);
				jobKickIdMap.get(managerName)[0] = new ArrayList<>();
				jobKickIdMap.get(managerName)[1] = new ArrayList<>();
				jobKickIdMap.get(managerName)[2] = new ArrayList<>();
			}
			String id = (String) objList.get(GetJobKickTableDefine.JOBKICK_ID);
			Integer type = JobKickTypeMessage.stringToType((String) objList.get(GetJobKickTableDefine.TYPE));
			if (type == JobKickConstant.TYPE_SCHEDULE) {
				jobKickIdMap.get(managerName)[0].add(id);
				if (scheId == null || scheId.equals("")) {
					scheId = id;
				}
				scheSize++;
			} else if (type == JobKickConstant.TYPE_FILECHECK) {
				jobKickIdMap.get(managerName)[1].add(id);
				if (fileId == null || fileId.equals("")) {
					fileId = id;
				}
				fileCheckSize++;
			} else if (type == JobKickConstant.TYPE_MANUAL) {
				jobKickIdMap.get(managerName)[2].add(id);
				if (manId == null || manId.equals("")) {
					manId = id;
				}
				manualSize++;
			}
		}

		//実行契機[スケジュール]を選択した場合
		String message = null;
		if(jobKickIdMap.isEmpty()){
			return null;
		} else if(scheSize == 1) {
			message = Messages.getString("schedule") + "["
					+ scheId + "]"
					+ Messages.getString("message.job.2");
		} else if(scheSize > 1){
			Object[] args = {scheSize, Messages.getString("schedule")};
			message = Messages.getString("message.job.123", args);
		}

		if(scheSize > 0) {
			if (MessageDialog.openQuestion(
					null,
					Messages.getString("confirmed"),
					message) == false) {
				return null;
			}

			Map<String, String> errorMsgs = new ConcurrentHashMap<>();
			boolean error = false;

			StringBuffer messageArg = new StringBuffer();
			int i = 0;
			for(Map.Entry<String, List<String>[]> entry : jobKickIdMap.entrySet()) {
				String managerName = entry.getKey();

				if(i > 0) {
					messageArg.append(", ");
				}
				messageArg.append(managerName);

				JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(managerName);
				try {
					wrapper.deleteSchedule(entry.getValue()[0]);
				} catch (InvalidRole_Exception e) {
					// 権限がない場合にはエラーメッセージを表示する
					MessageDialog.openInformation(
							null, 
							Messages.getString("message"),
							Messages.getString("message.accesscontrol.16"));
					error = true;
				} catch (Exception e) {
					m_log.warn("run(), " + e.getMessage(), e);
					errorMsgs.put(managerName, Messages.getString("message.hinemos.failure.unexpected") + HinemosMessage.replace(e.getMessage()));
				}
				i++;
			}

			//メッセージ表示
			if( 0 < errorMsgs.size() ){
				UIManager.showMessageBox(errorMsgs, true);
			} else if (!error) {
				Object[] arg = {messageArg.toString()};
				// 完了メッセージ
				MessageDialog.openInformation(null, Messages.getString("successful"),
						Messages.getString("message.job.75", arg));
			}
		}

		//実行契機[ファイルチェック]を選択した場合
		if(fileCheckSize == 1) {
			message = Messages.getString("file.check") + "["
					+ fileId + "]"
					+ Messages.getString("message.job.2");
		} else if(fileCheckSize > 1){
			Object[] args = {fileCheckSize, Messages.getString("file.check")};
			message = Messages.getString("message.job.123", args);
		}

		if(fileCheckSize > 0) {
			if (MessageDialog.openQuestion(
					null,
					Messages.getString("confirmed"),
					message) == false) {
				return null;
			}

			Map<String, String> errorMsgs = new ConcurrentHashMap<>();
			StringBuffer messageArg = new StringBuffer();
			int i = 0;
			for(Map.Entry<String, List<String>[]> entry : jobKickIdMap.entrySet()) {
				String managerName = entry.getKey();

				if(i > 0) {
					messageArg.append(", ");
				}
				messageArg.append(managerName);

				JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(managerName);
				//実行契機[ファイルチェック]を選択した場合
				try {
					wrapper.deleteFileCheck(entry.getValue()[1]);
				} catch (InvalidRole_Exception e) {
					errorMsgs.put(managerName, Messages.getString("message.accesscontrol.16"));
				} catch (Exception e) {
					m_log.warn("run(), " + e.getMessage(), e);
					errorMsgs.put(managerName, Messages.getString("message.hinemos.failure.unexpected") + HinemosMessage.replace(e.getMessage()));
				}
				i++;
			}

			//メッセージ表示
			if( 0 < errorMsgs.size() ){
				UIManager.showMessageBox(errorMsgs, true);
			} else {
				Object[] arg = {messageArg.toString()};
				MessageDialog.openInformation(null, Messages.getString("successful"),
						Messages.getString("message.job.75", arg));
			}
		}


		//実行契機[マニュアル実行契機]を選択した場合
		if(manualSize == 1) {
			message = Messages.getString("job.manual") + "["
					+ manId + "]"
					+ Messages.getString("message.job.2");
		} else if(manualSize > 1){
			Object[] args = {manualSize, Messages.getString("job.manual")};
			message = Messages.getString("message.job.123", args);
		}

		if(manualSize > 0) {
			if (MessageDialog.openQuestion(
					null,
					Messages.getString("confirmed"),
					message) == false) {
				return null;
			}

			Map<String, String> errorMsgs = new ConcurrentHashMap<>();
			StringBuffer messageArg = new StringBuffer();
			int i = 0;
			for(Map.Entry<String, List<String>[]> entry : jobKickIdMap.entrySet()) {
				String managerName = entry.getKey();

				if(i > 0) {
					messageArg.append(", ");
				}
				messageArg.append(managerName);

				JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(managerName);
				//実行契機[マニュアル実行契機]を選択した場合
				try {
					wrapper.deleteJobManual(entry.getValue()[2]);
				} catch (InvalidRole_Exception e) {
					errorMsgs.put(managerName, Messages.getString("message.accesscontrol.16"));
				} catch (Exception e) {
					m_log.warn("run(), " + e.getMessage(), e);
					errorMsgs.put(managerName, Messages.getString("message.hinemos.failure.unexpected") + HinemosMessage.replace(e.getMessage()));
				}
				i++;
			}

			//メッセージ表示
			if( 0 < errorMsgs.size() ){
				UIManager.showMessageBox(errorMsgs, true);
			} else {
				Object[] arg = {messageArg.toString()};
				MessageDialog.openInformation(null, Messages.getString("successful"),
						Messages.getString("message.job.75", arg));
			}
		}

		jobKickListView.update();
		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if( null != window ){
			IWorkbenchPage page = window.getActivePage();
			if( null != page ){
				IWorkbenchPart part = page.getActivePart();

				boolean editEnable = false;
				if( part instanceof JobKickListView  ){
					// Enable button when 1 item is selected
					JobKickListView view = (JobKickListView)part;
					if(view.getSelectedNum() > 0) {
						editEnable = true;
					}
				}
				this.setBaseEnabled( editEnable );
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
