/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui.views.commands;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.client.ui.util.FileDownloader;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.constant.HinemosModuleConstant;
import com.clustercontrol.utility.settings.ui.action.CommandAction;
import com.clustercontrol.utility.settings.ui.action.ReadXMLAction;
import com.clustercontrol.utility.settings.ui.bean.FuncInfo;
import com.clustercontrol.utility.settings.ui.dialog.JobunitListDialog;
import com.clustercontrol.utility.settings.ui.preference.SettingToolsXMLPreferencePage;
import com.clustercontrol.utility.settings.ui.util.BackupUtil;
import com.clustercontrol.utility.settings.ui.views.ImportExportExecView;
import com.clustercontrol.utility.ui.dialog.ErrorDialogWithScroll;
import com.clustercontrol.utility.util.ClientPathUtil;
import com.clustercontrol.utility.util.FileUtil;
import com.clustercontrol.utility.util.MultiManagerPathUtil;
import com.clustercontrol.utility.util.UtilityEndpointWrapper;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.utility.util.ZipUtil;
import com.clustercontrol.ws.utility.HinemosUnknown_Exception;
import com.clustercontrol.ws.utility.InvalidRole_Exception;
import com.clustercontrol.ws.utility.InvalidUserPass_Exception;

/**
 * 設定情報を取得するクライアント側アクションクラス<BR>
 *
 * @version 6.1.0
 * @since 2.0.0
 */
public class ExportSettingCommand extends AbstractHandler implements IElementUpdater {
	/*ロガー*/
	private Log log = LogFactory.getLog(getClass());
	
	/** アクションID */
	public static final String ID = ExportSettingCommand.class.getName();
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

	/** 処理対象のリスト */
    private List<FuncInfo> m_funcList = null;
    
	private String m_successList = "";
	private String m_failureList = "";
	private CommandAction m_action = null;
	private String filePath = null;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// keyチェック
		try {
			UtilityEndpointWrapper wrapper = UtilityEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
			String version = wrapper.getVersion();
			if (version.length() > 7) {
				boolean result = Boolean.valueOf(version.substring(7, version.length()));
				if (!result) {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.expiration.term.invalid"));
				}
			}
		} catch (HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					e.getMessage());
			return null;
		} catch (Exception e) {
			// キーファイルを確認できませんでした。処理を終了します。
			// Key file not found. This process will be terminated.
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.expiration.term"));
			return null;
		}
		
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == this.window || !isEnabled() ){
			return null;
		}

		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		ImportExportExecView listView = (ImportExportExecView) viewPart.getSite().getPage().findView(ImportExportExecView.ID);

		if (null == listView){
			return null;
		}
		List<FuncInfo> checkList = listView.getCheckedFunc();
		
		m_funcList = new ArrayList<FuncInfo>();

		String parentPath = MultiManagerPathUtil.getDirectoryPathTemporary(SettingToolsXMLPreferencePage.KEY_XML);
		
		//空の場合エラーメッセージを表示する。
		if (checkList.size() == 0) {
			 MessageDialog.openError(null,
					 Messages.getString("message.error"),
					 Messages.getString("message.export.error1"));
		} else {
			StringBuffer funcs = new StringBuffer();
			Iterator<FuncInfo> it = checkList.iterator();
			while (it.hasNext()) {
				
				// エクスポート対象を選択し、OKボタンを押したかどうかのフラグ
				boolean isTargetSelected = true;
				
				FuncInfo funcInfo = it.next();
				
				//ジョブユニットを選択させる
				if (funcInfo.getId().equals(HinemosModuleConstant.JOB_MST)) {
					
					JobunitListDialog jobunitListDialog = new JobunitListDialog(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							"export", funcInfo.getDefaultXML().get(0));
					
					// OKボタンが押下された、ジョブユニットが選択されていた場合は、対象のジョブユニットのジョブIDをセット
					if (jobunitListDialog.open() == IDialogConstants.OK_ID) {
						if (jobunitListDialog.getSelectionData() != null && jobunitListDialog.getSelectionData().size() != 0) {
							funcInfo.setTargetIdList(jobunitListDialog.getSelectionData());
						} else {
							isTargetSelected = false;
						}
						
					} else {
						funcInfo.setTargetIdList(null);
						isTargetSelected = false;
						
					}
				}
				
				// 対象が選択されている場合は、エクスポートリストに追加する
				if (isTargetSelected) {
					if(funcs.length() > 0){
						funcs.append(", ");
					}
					funcs.append(funcInfo.getName());
					m_funcList.add(funcInfo);
				}
				
			}
			log.debug("Export : " + m_funcList.toString());

			
			// エクスポート確認ダイアログを表示
			if(m_funcList.size() != 0 &&
					MessageDialog.openQuestion(
							null,
							Messages.getString("message.confirm"),
							Messages.getString("string.manager") + " : "
									+ UtilityManagerUtil.getCurrentManagerName() + " ( "
									+ EndpointManager.get(UtilityManagerUtil.getCurrentManagerName()).getUrlListStr()
									+ " )\n\n"
									+ Messages.getString("message.export.confirm1") + " ( "
									+ Messages.getString("records.mib", new String[] { String.valueOf(m_funcList.size()) })
									+ " )\n\n" + funcs.toString()))
			{
				// プログレスバーの表示
				try {
					ClientPathUtil pathUtil = ClientPathUtil.getInstance();
					if(pathUtil.lock(parentPath)){
						filePath = MultiManagerPathUtil.getDirectoryPath(SettingToolsXMLPreferencePage.KEY_XML);
					} else {
						//#3660
						//現在のロック機構ではOOME発生などでunlockが呼ばれない場合、ロック取得に失敗し続けるので、
						//ロックが取得できない際は解放して取り直す
						pathUtil.unlock(parentPath);
						if(pathUtil.lock(parentPath)) {
							filePath = MultiManagerPathUtil.getDirectoryPath(SettingToolsXMLPreferencePage.KEY_XML);
						}
					}
					
					IRunnableWithProgress op = new IRunnableWithProgress() {
						@Override
						public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
				
							List<String> defaultFileList = null;
							List<String> fileList = null;
							
							// プログレス・モニタの開始の開始
							monitor.beginTask(Messages.getString("string.export"), 100);
							// メッセージリストの初期化
							m_successList = "";
							m_failureList = "";
							
							m_action = new CommandAction();
							for (int i = 0; i < m_funcList.size(); i++) {
								if (monitor.isCanceled())
									throw new InterruptedException();

								FuncInfo info = m_funcList.get(i);
								
								int result = 0;
								
								// Rich版かつバックアップが有効なときのみ
								if ((BackupUtil.isBackup(BackupUtil.WHEN_EXPORT)) && (filePath == null)){
									if (!BackupUtil.moveXml(info)){
										String[] args = { info.getName() };
										log.warn(Messages.getString("message.backup.fail", args));
										result = -1;
									}
								}
								
								if (result == 0){
									
									// エクスポートメソッドの実行
									defaultFileList = info.getDefaultXML();
									
									// XMLファイルのリスト作成
									fileList = ReadXMLAction.getXMLFile(defaultFileList);
									
									String[] args = { info.getName() };
									
									monitor.subTask(Messages.getString("message.export.running", args));
									
									result = m_action.exportCommand(info, fileList, info.getTargetIdList());
								}
								
								if(result == 0) {
									m_successList = m_successList + " " + info.getName() + "\n";
								}
								else {
									m_failureList = m_failureList + " " + info.getName() + "\n";
								}
								
								monitor.worked(100/m_funcList.size());
							}
							monitor.done();
						}
					};
					// ダイアログの表示
					new ProgressMonitorDialog(PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getShell()).run(false, true, op);
				} catch (InvocationTargetException e) {
					log.error(e);
				} catch (InterruptedException e) {
					// キャンセル後の処理
					MessageDialog.openInformation(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							Messages.getString("string.cancel"),
							Messages.getString("message.cancel"));
				}
				
				// 結果出力ダイアログ
				String message = "";
				// 成功した場合
				if(!m_successList.equals("")){
					message = Messages.getString("message.export.result1") + "\n\n" + m_successList;
					
					MultiStatus mStatus = new MultiStatus(this.toString(), IStatus.OK, message, null);
					
					String resultMessage = m_action.getStdOut().replaceAll("\r\n", "\n");
					String[] resultMessages = resultMessage.split("\n");
					for(int i = 0; i < resultMessages.length; i++){
						mStatus.add(new Status(IStatus.INFO, this.toString(), IStatus.OK, resultMessages[i], null));
					}
					ErrorDialogWithScroll.openError(null, Messages.getString("message.confirm"), null, mStatus);
				}
				
				// 失敗した場合
				if(!m_failureList.equals("")){
					message = Messages.getString("message.export.result2") + "\n\n" + m_failureList;
					
					MultiStatus mStatus = new MultiStatus(this.toString(), IStatus.OK, message, null);
					
					String resultMessage = m_action.getStdOut().replaceAll("\r\n", "\n") + m_action.getErrOut().replaceAll("\r\n", "\n");
					String[] resultMessages = resultMessage.split("\n");
					for(int i = 0; i < resultMessages.length; i++){
						mStatus.add(new Status(IStatus.WARNING, this.toString(), IStatus.OK, resultMessages[i], null));
					}
					// 結果表示（失敗）
					ErrorDialogWithScroll.openError(null, Messages.getString("message.confirm"), null, mStatus);
				}
				
				// Viewのアップデート
				listView.update();
			}
				
		}

		if(filePath != null){
			//Web版ダウンロード
			List<String> inputFileNameList = new ArrayList<>();
			
			File dir = new File(filePath);
			
			FileUtil.addFiles2List(dir, inputFileNameList);
			
			File exportArchive = null;
			try {
				exportArchive = File.createTempFile("export_", ".zip", new File(ClientPathUtil.getDefaultXMLPath()));
				log.info("create export file : " + exportArchive.getAbsolutePath());
			} catch (IOException e) {
				log.error("fail to createTempFile : " + e.getMessage());
				MessageDialog.openError(null,
						 Messages.getString("message.error"),
						 Messages.getString("message.export.error2"));
			}
			
			if(inputFileNameList.size() > 0 && exportArchive != null){
				try {
					ZipUtil.archive(inputFileNameList, exportArchive.getAbsolutePath(), new File(filePath).getAbsolutePath());
					FileDownloader.openBrowser(window.getShell(), exportArchive.getAbsolutePath(), "export.zip");
				} catch (Exception e) {
					MessageDialog.openError(null,
							 Messages.getString("message.error"),
							 Messages.getString("message.export.error2"));
				}
			}
				
			ClientPathUtil.getInstance().unlock(parentPath);
			
			if(exportArchive != null) {
				if(exportArchive.delete()) {
					log.info("delete  exportArchive : " + exportArchive.getAbsolutePath());
				} else {
					log.error("fail to delete  exportArchive : " + exportArchive.getAbsolutePath());
				}
			}
			
		}
		
		if(filePath != null || ClientPathUtil.getInstance().isBussy(parentPath)){
			ClientPathUtil.getInstance().unlock(parentPath);
		}
		
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
				if(part instanceof ImportExportExecView){
					// Enable button when 1 item is selected
				}
				this.setBaseEnabled(editEnable);
				this.setBaseEnabled(true);
			}
		}
	}
}
