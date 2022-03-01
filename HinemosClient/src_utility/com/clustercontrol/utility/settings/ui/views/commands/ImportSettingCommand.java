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
import java.util.Arrays;
import java.util.Comparator;
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
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.openapitools.client.model.CheckPublishResponse;

import com.clustercontrol.client.ui.util.FileDownloader;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.UrlNotFound;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.TargetPlatformUtil;
import com.clustercontrol.utility.constant.HinemosModuleConstant;
import com.clustercontrol.utility.settings.ui.action.CommandAction;
import com.clustercontrol.utility.settings.ui.action.ReadXMLAction;
import com.clustercontrol.utility.settings.ui.bean.FuncInfo;
import com.clustercontrol.utility.settings.ui.dialog.FilterSettingDialog;
import com.clustercontrol.utility.settings.ui.dialog.JobunitListDialog;
import com.clustercontrol.utility.settings.ui.preference.SettingToolsXMLPreferencePage;
import com.clustercontrol.utility.settings.ui.util.BackupUtil;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.FilterSettingProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.settings.ui.views.ImportExportExecView;
import com.clustercontrol.utility.ui.dialog.MessageDialogWithCheckbox;
import com.clustercontrol.utility.ui.settings.dialog.UtilityImportCommandDialog;
import com.clustercontrol.utility.util.ClientPathUtil;
import com.clustercontrol.utility.util.FileUtil;
import com.clustercontrol.utility.util.MultiManagerPathUtil;
import com.clustercontrol.utility.util.SettingUtil;
import com.clustercontrol.utility.util.StringUtil;
import com.clustercontrol.utility.util.UtilityDialogConstant;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.utility.util.UtilityRestClientWrapper;
import com.clustercontrol.utility.util.ZipUtil;

/**
 * 情報を登録するクライアント側アクションクラス<BR>
 *
 * @version 6.1.0
 * @since 2.0.0
 */
public class ImportSettingCommand extends AbstractHandler implements IElementUpdater {
	
	private Log log = LogFactory.getLog(getClass());
	/** アクションID */
	public static final String ID = ImportSettingCommand.class.getName();
	private IWorkbenchWindow window;
	/** ビュー */
	private IWorkbenchPart viewPart;

	private final int CANCEL = -1;
	private final int OK_WITHOUT_OBJECT_PRIVILEGE = 1;
	private final int OK_WITH_OBJECT_PRIVILEGE = 2;

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
	
	/** 一括インポート単位数のMAP */
	private Map<String ,Integer> m_unitNumMap = null;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// keyチェック
		try {
			UtilityRestClientWrapper wrapper = UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
			CheckPublishResponse response = wrapper.checkPublish();
			boolean isPublish = response.getPublish();
			if (!isPublish) {
				MessageDialog.openWarning(
						null,
						Messages.getString("warning"),
						Messages.getString("message.expiration.term.invalid"));
			}
		} catch (InvalidRole | InvalidUserPass e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					e.getMessage());
			return null;
		} catch (HinemosUnknown e) {
			if(UrlNotFound.class.equals(e.getCause().getClass())) {
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.expiration.term"));
				return null;
			} else {
				MessageDialog.openInformation(null, Messages.getString("message"),
						e.getMessage());
				return null;
			}
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
		
		// ビューで選択されたインポート対象のリスト
		List<FuncInfo> checkedList = listView.getCheckedFunc();
		
		//インポート単位数の取得
		List<String > funcIdList = new ArrayList<String>();
		for( FuncInfo rec : checkedList){
			funcIdList.add(rec.getId());
		}
		try {
			m_unitNumMap =SettingUtil.getImportUnitNumList(funcIdList);
		} catch (InvalidUserPass | InvalidRole | RestConnectFailed | HinemosUnknown e1) {
			 MessageDialog.openError(null,
					 Messages.getString("message.error"),
					 Messages.getString("SettingTools.EndWithErrorCode") +" getImportUnitNumList message=" + e1.getMessage());
			return null;
		}
		
		// インポート対象リスト
		// ジョブユニットを選択しなかった場合、このリストに追加しない
		m_funcList = new ArrayList<FuncInfo>();

		// リッチクライアントの場合ディレクトリの存在チェック
		if (TargetPlatformUtil.isRCP() &&
				!MultiManagerPathUtil.existsDirectory(SettingToolsXMLPreferencePage.KEY_XML)) {
			MessageDialog.openWarning(
					null,
					Messages.getString("warning"),
					Messages.getString("message.utility.preferences.common.error"));
			return null;
		}
		String parentPath = MultiManagerPathUtil.getDirectoryPathTemporary(SettingToolsXMLPreferencePage.KEY_XML);
		String tmpPath = null;

		ClientPathUtil pathUtil = ClientPathUtil.getInstance();
		String filePath = null;
		
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
		
		if (checkedList.size() == 0) {
			 MessageDialog.openError(null,
					 Messages.getString("message.error"),
					 Messages.getString("message.import.error1"));
		} else {
			
			UtilityImportCommandDialog dialog = new UtilityImportCommandDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
			if(dialog.open() != Window.OK){
				return null;
			} else {
				tmpPath = dialog.getFilePath();
			}
			
			// 同期チェック（設定とマスタは同時にインポートできない仕様であるため。）
			Iterator<FuncInfo> it = checkedList.iterator();
			boolean masterFlg = false;
			boolean otherFlg = false;
			boolean withObjectprivilege = false;
			final String objectPrivilegeXml = ReadXMLAction.getXMLFile(listView.getObjectPrivilegeFunc().getDefaultXML()).get(0);
			// boolean includedObjectPrivilage = false;
			while(it.hasNext()) {
				FuncInfo info = it.next();
				if(info.getId().matches("^"+HinemosModuleConstant.MASTER+".*")){
					masterFlg = true;
				}
				else {
					otherFlg = true;
				}
				
				// 実行機能の中にマスタと設定の両方が含まれている場合
				if(masterFlg && otherFlg) {
					
					MessageDialog.openWarning(
							null,
							Messages.getString("message.warning"),
							Messages.getString("message.import.error3"));
					if(tmpPath != null || ClientPathUtil.getInstance().isBussy(parentPath)){
						ClientPathUtil.getInstance().unlock(parentPath);
					}
					return null;
				}
				
				// オブジェクト権限を同時にインポートした方がよいものがあるか
				if(!info.getObjectType().isEmpty()){
					withObjectprivilege = true;
				}
			}
			
			
			// ファイルの有無チェックと実行機能リストの生成
			List<String> xmlFiles = null;
			it = checkedList.iterator();
			StringBuffer funcNameList = new StringBuffer();
			StringBuffer addMessage = new StringBuffer();
			FilterSettingProcessMode.init();
			while(it.hasNext()) {
				
				// インポート対象を選択し、OKボタンを押したかどうかのフラグ
				// ver.3.2, ver4.0時点ではジョブユニットでのみ使用する
				boolean isTargetSelected = true;
				
				FuncInfo info = it.next();
				
				//ジョブユニットを選択させる
				if (info.getId().equals(HinemosModuleConstant.JOB_MST)) {
					String fileName = ReadXMLAction.getXMLFile(info.getDefaultXML()).get(0);
					File xmlFile = new File(fileName);
					if(!xmlFile.exists()) {
						// 存在しないファイルがある場合
						String[] args = { xmlFile.getName(), info.getName()};
						MessageDialog.openError(
								null,
								Messages.getString("message.confirm"),
								Messages.getString("message.import.error2",args));
						if(tmpPath != null || ClientPathUtil.getInstance().isBussy(parentPath)){
							ClientPathUtil.getInstance().unlock(parentPath);
						}
						return null;
					}
					// ジョブユニット選択ダイアログを表示
					JobunitListDialog jobunitListDialog = new JobunitListDialog(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							"import", fileName);
					
					// OKボタンが押下された、ジョブユニットが選択されていた場合は、対象のジョブユニットのジョブIDをセット
					if (jobunitListDialog.open() == IDialogConstants.OK_ID) {
						if (jobunitListDialog.getSelectionData() != null && jobunitListDialog.getSelectionData().size() != 0) {
							info.setTargetIdList(jobunitListDialog.getSelectionData());
						} else {
							isTargetSelected = false;
						}
						
					// キャンセルボタンが押下された場合は、対象のジョブユニットのジョブIDをセットしない
					} else {
						info.setTargetIdList(null);
						isTargetSelected = false;
					}
				}
				
				//フィルタ設定の場合、出力内容を選択させる
				if (info.isFilterSettingFunc()) {
					String fileName = ReadXMLAction.getXMLFile(info.getDefaultXML()).get(0);
					File xmlFile = new File(fileName);
					if(!xmlFile.exists()) {
						// 存在しないファイルがある場合
						String[] args = { xmlFile.getName(), info.getName()};
						MessageDialog.openError(
								null,
								Messages.getString("message.confirm"),
								Messages.getString("message.import.error2",args));
						if(tmpPath != null || ClientPathUtil.getInstance().isBussy(parentPath)){
							ClientPathUtil.getInstance().unlock(parentPath);
						}
						return null;
					}
					
					if(FilterSettingProcessMode.isSameNextChoice()){
						// "同じ選択を次の設定にも適用" が 以前に選択されていたら それに従う。
						FilterSettingProcessMode.setLastSelect(info.getActionClassName());
					}else{
						// 種別選択ダイアログを表示
						FilterSettingDialog filtersettingDialog = new FilterSettingDialog(
								PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "import",info.getName());
						// OKボタンが押下された際に選択されていた種別をセット
						if (filtersettingDialog.open() == IDialogConstants.OK_ID) {
							if (filtersettingDialog.getSelectionData() != null && filtersettingDialog.getSelectionData().size() != 0) {
								filtersettingDialog.setFilterSettingProcessMode(info.getActionClassName());
							} else {
								isTargetSelected = false;
							}
						// キャンセルボタンが押下された場合は、対象のジョブユニットのジョブIDをセットしない
						} else {
							isTargetSelected = false;
						}
					}
				}
				
				// 対象が選択されていた場合は、XMLを読み込む
				if (isTargetSelected) {
					funcNameList.append(" ");
					funcNameList.append(info.getName());
					funcNameList.append("\n");
					
					if ( info.getId().equals(HinemosModuleConstant.JOB_MST ) ) {
						addMessage.append("\n"+Messages.getString("message.import.confirm9", new String[] { info.getName() }));
					} else if (info.getId().equals(HinemosModuleConstant.JOB_MAP_IMAGE)
							|| info.getId().equals(HinemosModuleConstant.NODE_MAP_IMAGE)
							|| info.getId().equals(HinemosModuleConstant.INFRA_FILE)) {
						// FIXME v.7.0時点では、isTargetSelectedがtrueになるのはジョブ設定のみでありこちらに行くことはない。
						addMessage.append("\n"+Messages.getString("message.import.confirm10", new String[] { info.getName() }));
					}
					
					// 処理対象としてリストに追加
					m_funcList.add(info);
					
					xmlFiles = ReadXMLAction.getXMLFile(info.getDefaultXML());
					
					Iterator<String> itrFilePath = xmlFiles.iterator();
					while(itrFilePath.hasNext()) {
						
						File xmlFile = new File(itrFilePath.next());
						if(!xmlFile.exists()) {
							// 存在しないファイルがある場合、インポート対象か確認する
							if (!ReadXMLAction.isRequiredImportFile(xmlFile.getName())) {
								log.debug(xmlFile.getName() + " is not required when import.");
								continue;
							}
							String[] args = { xmlFile.getName(), info.getName()};
							MessageDialog.openError(
									null,
									Messages.getString("message.confirm"),
									Messages.getString("message.import.error2",args));
							if(tmpPath != null || ClientPathUtil.getInstance().isBussy(parentPath)){
								ClientPathUtil.getInstance().unlock(parentPath);
							}
							return null;
						}
					}
				}
			}
			
			ImportProcessMode.setProcesstype(0);
			ImportProcessMode.setSameprocess(false);
			ImportProcessMode.setObjectPrivilege(false);
			ImportProcessMode.setXmlObjectPrivilege(objectPrivilegeXml);
			ImportProcessMode.setRollbackIfAbnormal(false);
			ImportProcessMode.setSameRollbackIfAbnormal(false);
			ImportProcessMode.setCancelForAbend(false);
			ImportProcessMode.setSameCancelForAbend(false);
			DeleteProcessMode.setProcesstype(0);
			DeleteProcessMode.setSameprocess(false);
			IMPORT:if(m_funcList.size() != 0){
				if (withObjectprivilege ){
					int status = showComfirmWithObjectPrivilege(funcNameList.toString(),addMessage.toString());
					switch(status){
					case CANCEL:
						break IMPORT;
					case OK_WITH_OBJECT_PRIVILEGE:
						ImportProcessMode.setObjectPrivilege(true);
						File xmlFile = new File(objectPrivilegeXml);
						if(!xmlFile.exists()) {
							String[] args = { xmlFile.getName(), listView.getObjectPrivilegeFunc().getName()};
							MessageDialog.openError(
									null,
									Messages.getString("message.confirm"),
									Messages.getString("message.import.error2",args));
							if(tmpPath != null || ClientPathUtil.getInstance().isBussy(parentPath)){
								ClientPathUtil.getInstance().unlock(parentPath);
							}
							return null;
						}
						break;
					}
				}else if (!showComfirmDialog(funcNameList.toString(),addMessage.toString())){
					break IMPORT;
				}
				
				// プログレスバーの表示
				try {
					IRunnableWithProgress op = new IRunnableWithProgress() {
						@Override
						public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
				
							List<String> defaultFileList = null;
							List<String> fileList = null;

							// インポート順のソート
							FuncInfo[] obj = m_funcList.toArray(new FuncInfo[0]);
							Arrays.sort(obj, new Comparator<FuncInfo>() {
										/**
										 * インポートする順番をソートするために必要なComparatorクラス
										 * 
										 */
										@Override
										public int compare(FuncInfo o1, FuncInfo o2){
											return o1.getWeight() - o2.getWeight();
										}
									});

							List<FuncInfo> importOrderList = new ArrayList<FuncInfo>();
							for(int i=0; i<obj.length; i++){
								importOrderList.add((FuncInfo)obj[i]);
							}
				
							// プログレス・モニタの開始の開始
							monitor.beginTask(Messages.getString("string.import"), 100);
							// メッセージリストの初期化
							m_successList = "";
							m_failureList = "";
							m_action = new CommandAction();
							
							boolean doNotify = false;
							boolean doJob = false;
							FuncInfo notifyFuncInfo = null;
							List<String> notifyFileList = null;
							for (int i = 0; i < importOrderList.size(); i++) {
								if (monitor.isCanceled())
									throw new InterruptedException();
								
								FuncInfo info = importOrderList.get(i);
								defaultFileList = info.getDefaultXML();
								
								// 異常時の処理方法を確認(Job、アップロード系、プラットフォームマスタ以外)
								if ( !(info.getId().equals(HinemosModuleConstant.JOB_MST))
									&& !(info.getId().equals(HinemosModuleConstant.JOB_MAP_IMAGE))
									&& !(info.getId().equals(HinemosModuleConstant.NODE_MAP_IMAGE))
									&& !(info.getId().equals(HinemosModuleConstant.INFRA_FILE))
									&& !(info.getId().equals(HinemosModuleConstant.MASTER_PLATFORM))
									&& !(info.getId().equals(HinemosModuleConstant.MASTER_COLLECT))
										) {
									if (ImportProcessMode.isSameRollbackIfAbnormal() == false
											&& ImportProcessMode.getProcesstype() != UtilityDialogConstant.CANCEL) {
										String[] dialogTitleArgs = new String[] { info.getName() };
										MessageDialogWithToggle errorProcessDialog = createImportErrorProcessDialog(
														Messages.getString("message.import.confirm5", dialogTitleArgs));
										int retErrorProcess = errorProcessDialog.open();
										ImportProcessMode.setSameRollbackIfAbnormal(errorProcessDialog.getToggleState());
										switch (retErrorProcess) {
										case UtilityDialogConstant.ROLLBACK:
											ImportProcessMode.setRollbackIfAbnormal(true);
											break;
										case UtilityDialogConstant.SKIP:
										default:
											ImportProcessMode.setRollbackIfAbnormal(false);
											break;
										}
									}
								}
								// XMLファイルのリスト作成
								fileList = ReadXMLAction.getXMLFile(defaultFileList);
								
								String[] args = { info.getName() };
								
								monitor.subTask(Messages.getString("message.import.running", args));

								if(info.getId().equals(HinemosModuleConstant.PLATFORM_NOTIFY)){
									doNotify = true;
									notifyFuncInfo = info;
									notifyFileList = fileList;
								}

								if(info.getId().equals(HinemosModuleConstant.JOB_MST)){
									doJob = true;
								}
								int result = 0;
								
								if (BackupUtil.isBackup(BackupUtil.WHEN_IMPORT)){
									result = m_action.backupCommand(info, fileList, info.getTargetIdList());
									if (result != 0){
										log.error(Messages.getString("message.backup.fail", args));
										result = -1;
										boolean continueFlg = MessageDialog.openQuestion(
												null,
												Messages.getString("message.error"),
												Messages.getString("ContinueQuestion.WhenExportError") + "\n\n" + info.getName()
												);
										if (continueFlg){
											result = 0;
										}
									}
								}
								
								ImportProcessMode.setImportUnitNum(m_unitNumMap.get(info.getId()));
								result = m_action.importCommand(info, fileList, info.getTargetIdList());
								
								if(result == 0) {
									m_successList = m_successList + " " + info.getName() + "\n";
								}
								else {
									m_failureList = m_failureList + " " + info.getName() + "\n";
								}
								
								monitor.worked(100/importOrderList.size());
							}
							if(doJob && doNotify){
								ImportProcessMode.setProcesstype(UtilityDialogConstant.SKIP);
								ImportProcessMode.setSameprocess(true);
								ImportProcessMode.setImportUnitNum(m_unitNumMap.get(notifyFuncInfo.getId()));
								m_action.importCommand(notifyFuncInfo, notifyFileList, notifyFuncInfo.getTargetIdList());
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
					message = Messages.getString("message.import.result1") + "\n\n" + m_successList;
					MultiStatus mStatus = new MultiStatus(this.toString(), IStatus.OK, message, null);
					
					String resultMessage = m_action.getStdOut().replaceAll("\r\n", "\n");
					String[] resultMessages = resultMessage.split("\n");
					for(int i = 0; i < resultMessages.length; i++){
						mStatus.add(new Status(IStatus.INFO, this.toString(), IStatus.OK,
								StringUtil.cutTailForStatus(resultMessages[i]), null));
					}
					ErrorDialog.openError(null, Messages.getString("message.confirm"), null, mStatus);
				}
				
				// 失敗した場合
				if(!m_failureList.equals("")){
					message = Messages.getString("message.import.result2") + "\n\n" + m_failureList;
					
					MultiStatus mStatus = new MultiStatus(this.toString(), IStatus.OK, message, null);
					
					String resultMessage = m_action.getStdOut().replaceAll("\r\n", "\n") + m_action.getErrOut().replaceAll("\r\n", "\n");
					String[] resultMessages = resultMessage.split("\n");
					for(int i = 0; i < resultMessages.length; i++){
						mStatus.add(new Status(IStatus.WARNING, this.toString(), IStatus.OK,
								StringUtil.cutTailForStatus(resultMessages[i]), null));
					}
					
					// 結果表示（失敗）
					ErrorDialog.openError(
							null, 
							Messages.getString("message.confirm"), 
							null, 
							mStatus);
				}
				
			}
		}
		// Web版バックアップファイルのzip保存
		if((filePath != null) && (BackupUtil.isBackup(BackupUtil.WHEN_IMPORT))){
			String backupTmpPath = BackupUtil.getBackupPath();
			backupTmpPath=backupTmpPath.replaceAll("\\\\", "/");
			List<String> inputFileNameList = new ArrayList<>();
			File dir = new File(backupTmpPath);
			FileUtil.addFiles2List(dir, inputFileNameList);
			File exportArchive = null;
			try {
				exportArchive = File.createTempFile("backup_", ".zip", new File(ClientPathUtil.getDefaultXMLPath()));
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
					FileDownloader.openBrowser(window.getShell(), exportArchive.getAbsolutePath(), "backup.zip");
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
//					ImportExportExecView view = (ImportExportExecView)part;
				}
				this.setBaseEnabled(editEnable);
				this.setBaseEnabled(true);
			}
		}
	}

	/**
	 * @param funcNameList
	 * @return 状態（キャンセル(-1)、OKチェックボックスOFF（1）、OKチェックボックスOFF（2））
	 */
	private int showComfirmWithObjectPrivilege(String funcNameList, String addMessage){
		String msg = Messages.getString("string.manager") + " :  " +
				UtilityManagerUtil.getCurrentManagerName() +
				" ( " +
				RestConnectManager.get(UtilityManagerUtil.getCurrentManagerName()).getUrlListStr() +
				" )\n\n" +
				Messages.getString("message.import.confirm1") +
				"\n\n" +
				funcNameList;
		if(addMessage != null && !(addMessage.isEmpty()) ){
			msg  = msg + addMessage;
		}
		MessageDialogWithCheckbox messageDialogWithCheckbox = new MessageDialogWithCheckbox(
				msg,
				Messages.getString("message.import.confirm.object.privilege")
				);
		boolean status = messageDialogWithCheckbox.openQuestion();
		if (status){
			if (messageDialogWithCheckbox.isChecked()){
				return OK_WITH_OBJECT_PRIVILEGE;
			}else{
				return OK_WITHOUT_OBJECT_PRIVILEGE;
			}
		}else{
			return CANCEL;
		}
	}
	
	private boolean showComfirmDialog(String funcNameList , String addMessage){
		String msg = Messages.getString("string.manager") + " :  " + UtilityManagerUtil.getCurrentManagerName() + " ( " + RestConnectManager.get(UtilityManagerUtil.getCurrentManagerName()).getUrlListStr() + " )\n\n" +
				Messages.getString("message.import.confirm1") + "\n\n" + funcNameList;
		if(addMessage != null && !(addMessage.isEmpty()) ){
			msg  = msg + "\n" + addMessage;
		}
		
		return MessageDialog.openQuestion(
				null,
				Messages.getString("message.confirm"),
				msg);
	}
	
	private MessageDialogWithToggle createImportErrorProcessDialog(String message){
		return new MessageDialogWithToggle(null, 
				  Messages.getString("message.confirm"), 
				  null, 
				  message, 
				  MessageDialogWithToggle.QUESTION, 
//				  new String[]{Messages.getString("message.import.error.rollback"),
//							   Messages.getString("message.import.error.skip")},
				  // TODO 6612対応 7.0.1リリース時には一時変更を戻す
				  new String[]{Messages.getString("message.import.error.skip")}, 
				  0,
				  Messages.getString("message.import.confirm6"), 
				  false);
	}
}
