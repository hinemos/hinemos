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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
import org.eclipse.jface.dialogs.MessageDialog;
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

import com.clustercontrol.client.ui.util.FileDownloader;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.constant.HinemosModuleConstant;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.settings.ui.action.CommandAction;
import com.clustercontrol.utility.settings.ui.action.ReadXMLAction;
import com.clustercontrol.utility.settings.ui.bean.FuncInfo;
import com.clustercontrol.utility.settings.ui.preference.SettingToolsXMLPreferencePage;
import com.clustercontrol.utility.settings.ui.views.ImportExportExecView;
import com.clustercontrol.utility.ui.settings.dialog.UtilityDiffCommandDialog;
import com.clustercontrol.utility.util.ClientPathUtil;
import com.clustercontrol.utility.util.MultiManagerPathUtil;
import com.clustercontrol.utility.util.UtilityEndpointWrapper;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.utility.util.ZipUtil;
import com.clustercontrol.ws.utility.HinemosUnknown_Exception;
import com.clustercontrol.ws.utility.InvalidRole_Exception;
import com.clustercontrol.ws.utility.InvalidUserPass_Exception;

/**
 * 比較処理を実行するクライアント側アクションクラス<BR>
 *
 * @version 6.1.0
 * @since 2.0.0
 */
public class DiffSettingCommand extends AbstractHandler implements IElementUpdater {
	/*ロガー*/
	private Log log = LogFactory.getLog(getClass());
	
	/** アクションID */
	public static final String ID = DiffSettingCommand.class.getName();
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

	private static class MessageStrings {
		private String message_error1;
		private String message_confirm1;
		private String message_result0;
		private String message_result1;
		private String message_result2;
		private String string_name;
	}

	private MessageStrings messages = createMessages();
	
	private List<FuncInfo> m_funcList = null;
	private String m_successList = "";
	private String m_diffList = "";
	private String m_failureList = "";
	private CommandAction m_action = null;

	private MessageStrings createMessages() {
		MessageStrings ms = new MessageStrings();
		
		ms.message_error1 = "message.diff.error1";
		ms.message_confirm1 = "message.diff.confirm1";
		ms.message_result0 = "message.diff.result0";
		ms.message_result1 = "message.diff.result1";
		ms.message_result2 = "message.diff.result2";
		ms.string_name = "string.diff";
		
		return ms;
	}
	
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
		if (checkList.size() == 0) {
			MessageDialog.openError(
					null,
					Messages.getString("message.error"),
					Messages.getString(messages.message_error1));
			return null;
		}
		
		// 比較非対応の機能をチェックする
		for (FuncInfo funcInfo: checkList) {
			if (funcInfo.getId().equals(HinemosModuleConstant.MASTER_COLLECT)){
				MessageDialog.openError(
						null,
						Messages.getString("message.confirm"),
						Messages.getString("message.diff.not.support.collect.master"));
						return null;
			}
			if (funcInfo.getId().equals(HinemosModuleConstant.MASTER_PLATFORM)){
				MessageDialog.openError(
						null,
						Messages.getString("message.confirm"),
						Messages.getString("message.diff.not.support.platform.master"));
						return null;
			}
			if (funcInfo.getId().equals(HinemosModuleConstant.MASTER_JMX)){
				MessageDialog.openError(
						null,
						Messages.getString("message.confirm"),
						Messages.getString("message.diff.not.support.jmx.master"));
						return null;
			}
		}
		
		String parentPath = MultiManagerPathUtil.getDirectoryPath(SettingToolsXMLPreferencePage.KEY_XML);
		String tmpPath = null;
		String diffParentPath = MultiManagerPathUtil.getDirectoryPath(SettingToolsXMLPreferencePage.KEY_DIFF_XML);
		String diffTmpPath = null;

		UtilityDiffCommandDialog dialog = new UtilityDiffCommandDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		if(dialog.open() != Window.OK){
			return null;
		} else {
			tmpPath = dialog.getFilePath();
			diffTmpPath = dialog.getDiffFilePath();
		}
		
		// 選択された内容が有効な状態になっているかチェック。
		List<FuncInfo> funcList = check(checkList);
		if (funcList == null) {
			// 有効でないので終了。
			//UtilityDiffCommandDialogで取得したロックを解放する
			if(tmpPath != null || ClientPathUtil.getInstance().isBussy(parentPath)){
				ClientPathUtil.getInstance().unlock(parentPath);
			}
			ClientPathUtil.getInstance().unlock(diffParentPath);
			return null;
		}

		// 実際に比較する機能をリストとして保持
		m_funcList = funcList;

		StringBuilder funcNameList = new StringBuilder();
		for (FuncInfo funcInfo: m_funcList) {
			funcNameList.append(" ");
			funcNameList.append(funcInfo.getName());
			funcNameList.append("\n");
		}
		
		if (m_funcList.size() != 0 &&
				MessageDialog.openQuestion(
						null,
						Messages.getString("message.confirm"),
						Messages.getString(messages.message_confirm1) + "\n\n" + funcNameList.toString())){
			// プログレスバーの表示
			try {
				IRunnableWithProgress op = new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						// 削除順のソート
						List<FuncInfo> deleteOrderList = new ArrayList<FuncInfo>(m_funcList);
						Collections.sort(deleteOrderList, new Comparator<FuncInfo>() {
							@Override
							public int compare(FuncInfo o1, FuncInfo o2) {
								return o2.getWeight() - o1.getWeight();
							}
						});

						// プログレス・モニタの開始の開始
						monitor.beginTask(Messages.getString(messages.string_name), 100);

						m_successList = "";
						m_diffList = "";
						m_failureList = "";

						// 処理実行。
						m_action = new CommandAction();
						for (FuncInfo info: deleteOrderList) {
							if (monitor.isCanceled()) throw new InterruptedException();

							String[] args = { info.getName() };
							monitor.subTask(Messages.getString("message.diff.running", args));
							int result = m_action.diffCommand(info);
							prepareMag(result,info,diffParentPath);
							monitor.worked(100/deleteOrderList.size());
						}

						monitor.done();

					}
				};
				// ダイアログの表示
				new ProgressMonitorDialog(PlatformUI.getWorkbench().
						getActiveWorkbenchWindow().getShell()).run(false,true, op);
			}
			catch (InvocationTargetException e) {
				log.error(e);
			}
			catch (InterruptedException e) {
				// キャンセル後の処理
				MessageDialog.openInformation(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						Messages.getString("string.cancel"),
						Messages.getString("message.cancel"));
			}

			// 結果出力ダイアログ
			String message = "";
			// 成功した場合
			if (!m_successList.equals("") || !m_diffList.equals("")) {
				if (!m_successList.equals("")){
					message = Messages.getString(messages.message_result0) + "\n\n" + m_successList.toString() +  "\n\n";
				}
				if (!m_diffList.equals("")){
					message = message + Messages.getString(messages.message_result1) + "\n\n" + m_diffList.toString();
				}
				MultiStatus mStatus = new MultiStatus(this.toString(), IStatus.OK, message, null);
				String resultMessage = m_action.getStdOut().replaceAll("\r\n", "\n");
				String[] resultMessages = resultMessage.split("\n");
				for (int i = 0; i < resultMessages.length; i++) {
					mStatus.add(new Status(IStatus.INFO, this.toString(), IStatus.OK, resultMessages[i], null));
				}

				ErrorDialog.openError(null, Messages.getString("message.confirm"), null, mStatus);
			}

			// 失敗した場合
			if (!m_failureList.equals("")) {
				message = Messages.getString(messages.message_result2) + "\n\n" + m_failureList.toString();

				MultiStatus mStatus = new MultiStatus(this.toString(),IStatus.OK, message, null);
				String resultMessage = m_action.getStdOut().replaceAll("\r\n", "\n") + m_action.getErrOut().replaceAll("\r\n", "\n");
				String[] resultMessages = resultMessage.split("\n");
				for (int i = 0; i < resultMessages.length; i++) {
					mStatus.add(new Status(IStatus.WARNING, this.toString(), IStatus.OK, resultMessages[i], null));
				}

				// 結果表示（失敗）
				ErrorDialog.openError(null, Messages.getString("message.confirm"), null, mStatus);
			}
		}

		if(tmpPath != null || ClientPathUtil.getInstance().isBussy(parentPath)){
			ClientPathUtil.getInstance().unlock(parentPath);
		}

		if(diffTmpPath != null){
			List<String> inputFileNameList = new ArrayList<>();
			
			File dir = new File(diffTmpPath);
			
			File[] files = dir.listFiles();
			if (null == files){
				log.debug("dir.listFiles() == null");
				return null;
			}
			
			for(File file: files){
				if(Pattern.compile(".*\\.csv$").matcher(file.getAbsolutePath()).find()){
					inputFileNameList.add(file.getAbsolutePath());
				}
			}
			
			File diffArchive = null;
			try {
				diffArchive = File.createTempFile("diff_", ".zip", new File(ClientPathUtil.getDefaultXMLDiffPath()));
				log.info("create export file : " + diffArchive.getAbsolutePath());
			} catch (IOException e) {
				log.error("fail to createTempFile : " + e.getMessage());
				MessageDialog.openError(null,
						 Messages.getString("message.error"),
						 Messages.getString("message.diff.error3"));
			}
			
			if(inputFileNameList.size() > 0 && diffArchive != null){
				try {
					ZipUtil.archive(inputFileNameList, diffArchive.getAbsolutePath(), new File(diffTmpPath).getAbsolutePath());
					FileDownloader.openBrowser(window.getShell(), diffArchive.getAbsolutePath(), "diff.zip");
				} catch (Exception e) {
					MessageDialog.openError(null,
							 Messages.getString("message.error"),
							 Messages.getString("message.diff.error3"));
				}
			}
			
			ClientPathUtil.getInstance().unlock(diffParentPath);
			
			if(diffArchive != null) {
				if(diffArchive.delete()) {
					log.info("delete  diffArchive : " + diffArchive.getAbsolutePath());
				} else {
					log.error("fail to delete  diffArchive : " + diffArchive.getAbsolutePath());
				}
			}
		}
		
		if(ClientPathUtil.getInstance().isBussy(diffParentPath)){
			ClientPathUtil.getInstance().unlock(diffParentPath);
		}
		
		return null;
	}
	
	private void prepareMag(int result, FuncInfo info, String diffParentPath){
		if (result<SettingConstants.SUCCESS){
			m_failureList = m_failureList + " " + info.getName() + "\n";
			return;
		}
		List<String> xmlList = info.getDefaultXML();
		if (result == SettingConstants.SUCCESS) {
			m_successList = m_successList + " " + info.getName() + "\n";
			return;
		}
		int cnt = 0;
		for (String xml : xmlList){
			if ((result & (int)Math.pow(2, cnt))==Math.pow(2, cnt)){
				String[] args = { diffParentPath + "\\" + xml + ".csv"};
				m_diffList= m_diffList + " " + info.getName() + "\t"
						+ Messages.getString("message.diff.detail",args) + "\n";
			}
			cnt++;
		}
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

	private List<FuncInfo> check(List<FuncInfo> checkList) {
		for (FuncInfo info: checkList) {
			List<ReadXMLAction.DiffFilePaths> xmlFiles = ReadXMLAction.getDiffXMLFiles(info.getDefaultXML());

			for (ReadXMLAction.DiffFilePaths paths: xmlFiles) {
				File xmlFile = new File(paths.filePath1);
				if(!xmlFile.exists()) {
					// 存在しないファイルがある場合、Excelで出力されないファイル(インポートされないファイル)かどうか確認
					if (!ReadXMLAction.isRequiredImportFile(xmlFile.getName())) {
						log.debug(xmlFile.getName() + " does not exist, it may have been exported by Excel.");
						continue;
					}
					String[] args = { xmlFile.getAbsolutePath(), info.getName()};
					MessageDialog.openError(
							null,
							Messages.getString("message.confirm"),
							Messages.getString("message.diff.error2",args));
					return null;
				}

				xmlFile = new File(paths.filePath2);
				if(!xmlFile.exists()) {
					// 存在しないファイルがある場合、Excelで出力されないファイル(インポートされないファイル)かどうか確認
					if (!ReadXMLAction.isRequiredImportFile(xmlFile.getName())) {
						log.debug(xmlFile.getName() + " does not exist, it may have been exported by Excel.");
						continue;
					}
					String[] args = { xmlFile.getAbsolutePath(), info.getName()};
					MessageDialog.openError(
							null,
							Messages.getString("message.confirm"),
							Messages.getString("message.diff.error2",args));
					return null;
				}
			}
		}

		return checkList;
	}
}
