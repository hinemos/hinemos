/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui.action;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.sdml.bean.SdmlUtilityActionResult;
import com.clustercontrol.sdml.util.SdmlUtilityInterfaceNoEclipse;
import com.clustercontrol.util.LoginConstant;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.RestConnectUnit;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.settings.WSActionLauncher;
import com.clustercontrol.utility.settings.job.action.JobMasterAction;
import com.clustercontrol.utility.settings.ui.bean.FuncInfo;
import com.clustercontrol.utility.settings.ui.constant.CommandConstant;
import com.clustercontrol.utility.settings.ui.util.BackupUtil;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.IUtilityPreferenceStore;
import com.clustercontrol.utility.util.Log4J2Util;
import com.clustercontrol.utility.util.SettingUtil;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.utility.util.UtilityPreferenceStore;

/**
 * 
 * Hinemosマネージャに対して、実際に操作を行うクラス
 * 
 * @version 6.1.0
 * @since 1.2.0
 * 
 */
public class CommandAction {
	private enum CommandType {
		Import("import"),
		Export("export"),
		Clear("clear"),
		Diff("diff");
		
		
		private CommandType (String commandName) {
			this.commandName = commandName;
		}
		
		private String commandName;
		
		public String getCommandName() {
			return commandName;
		}
	}
	
	/**
	 * 現在接続中のアカウント情報を取得するためのクラス。<BR>
	 * Hinemosマネージャとメッセージフィルタで現在接続中のアカウント情報取得方法が
	 * 異なるため、このクラスを用いて外からアカウント情報の取得方法を変更できるようにする。
	 */
	public static class AccountInfoProvider {
		public AccountInfo getCurrentAccountInfo() {
			AccountInfo accountInfo = null;

			RestConnectUnit unit = RestConnectManager.get(UtilityManagerUtil.getCurrentManagerName());
			// Only if there is an active connection
			if(null != unit && unit.isActive()) {
				accountInfo = new AccountInfo(unit.getUrlListStr(),unit.getUserId());
			}
			return accountInfo;
		}
	}

    /* ロガー */
	private static Log log = LogFactory.getLog(CommandAction.class);

	private String m_stdout = "";
	private String m_errout = "";

	private AccountInfoProvider accountInfoProvider = null;

	public void setAccountInfoProvider(AccountInfoProvider provider) {
		accountInfoProvider = provider;
	}

	/* std_out内のfilter対象文字列の定義 */
	private static final String[] STDOUT_FILITER_ARRAYS = {
		//Marshaller の利用方法についてのcasterからの警告だが 本件では意図的（generatorによるコードで発生してる）なので不要
		"Marshaller called using one of the *static*  marshal(Object, *) methods."  
	};

	/**
	 * xmlファイルからマネージャへのインポートを実行するメソッド
	 * 
	 * @return 成否
	 */
	/*
	public int importCommand(FuncInfo info, List<String> fileList) {
		return this.importCommand(info, fileList, null);
	}
	*/
	
	/**
	 * xml ファイル間の差分情報を作成するするメソッド
	 * 
	 * @return 成否
	 */
	public int diffCommand(FuncInfo info) {
		log.debug("begin diffCommand()");
		int result = doCommand(CommandType.Diff, info, null, null);
		log.debug("end diffCommand()");
		
		return result;
	}

	/**
	 * 
	 * xmlファイルからマネージャへのインポートを実行するメソッド。 対象を選択してインポートする際に本メソッドを使用する。
	 * 
	 * @param info
	 * @param fileList
	 * @param idList
	 *            エクスポート対象を特定するIDのリスト
	 * @return
	 */
	public int importCommand(FuncInfo info, List<String> fileList, List<String> idList) {
		log.debug("begin importCommand()");
		try {
			//一括インポート単位数が設定されていなければ取得する（portingからの呼び出し時を想定）
			refrectImportUnitNum(info);
		} catch (InvalidRole | InvalidUserPass | RestConnectFailed | HinemosUnknown e) {
			//異常発生時は異常終了
			String errorMessage =  "End Import . refrectImportUnitNum error ."  + e.getClass().getSimpleName() + " (Error) Message="+  e.getMessage();
			m_errout += errorMessage;
			log.error(errorMessage);
			return SettingConstants.ERROR_INPROCESS;
		}
		//import呼び出し
		int result = doCommand(CommandType.Import, info, fileList,idList);
		log.debug("end importCommand()");
		
		return result;
	}

	/**
	 * マネージャからxmlファイルへのエクスポートを実行するメソッド
	 * 
	 * @return 成否
	 */
	/*
	public int exportCommand(FuncInfo info, ArrayList<String> fileList) {
		return this.exportCommand(info, fileList, null);
	}
	*/
	
	/**
	 * 
	 * マネージャからxmlファイルへのエクスポートを実行するメソッド。 対象を選択してエクスポートする際に本メソッドを使用する。
	 * 
	 * @param info
	 * @param fileList
	 * @param idList
	 *            エクスポート対象を特定するIDのリスト
	 * @return
	 */
	public int exportCommand(FuncInfo info, List<String> fileList, List<String> idList) {
		log.debug("begin exportCommand()");
		int result = doCommand(CommandType.Export, info, fileList, idList);
		log.debug("end exportCommand()");
		
		return result;
	}
	/**
	 * 
	 * マネージャからxmlファイルへのバックアップエクスポートを実行するメソッド。 対象を選択してバックアップエクスポートする際に本メソッドを使用する。
	 * 
	 * @param info
	 * @param fileList
	 * @param idList
	 *            バックアップエクスポート対象を特定するIDのリスト
	 * @return
	 */
	public int backupCommand(FuncInfo info, List<String> fileList, List<String> idList) {
		log.debug("begin backupCommand()");
		int result = doCommand(CommandType.Export, info, fileList, idList,true);
		log.debug("end backupCommand()");
		
		return result;
	}
	private int doCommand(CommandType commandType, FuncInfo info, List<String> fileList, List<String> idList) {
		return doCommand(commandType,info,fileList,idList,false);
	}
	
	/**
	 * @return 成否
	 */
	private int doCommand(CommandType commandType, FuncInfo info, List<String> fileList, List<String> idList, 
			 boolean backup) {
		int result = -1;

		if (commandType != CommandType.Diff && info.getActionClassName().equals(CommandConstant.ACTION_JOB_MST)) {
			// Diff以外(Import, Export, Clear)の場合、ログインが必要
			AccountInfo accountInfo = getCurrentAccountInfo();
			assert accountInfo != null : "unexpected";
			log.debug(accountInfo.userid + ", " + accountInfo.url);

			Logger logger = null;
			CharArrayWriter byteWriter = new CharArrayWriter(8192);
			PrintStream oldError = System.err;
			String out = "";
			String error = "";

			try {
				JobMasterAction actionClass = new JobMasterAction();
				logger = actionClass.getLogger();
				
				// log4j出力の一部を画面表示向けに取得するため、アクションクラスのLoggerにappenderを追加
				Log4J2Util.addWriteAppenderToLogger(byteWriter, Thread.currentThread().getName(), logger.getName(),
						org.apache.logging.log4j.Level.INFO);

				ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
				System.setErr(new PrintStream(byteStream));
				
				Config.putConfig("Login.URL", accountInfo.url);
				Config.putConfig("Login.USER", accountInfo.userid);
				IUtilityPreferenceStore clientStore = UtilityPreferenceStore.get();
				Config.putConfig("HTTP.CONNECT.TIMEOUT", Integer.toString(clientStore.getInt(LoginConstant.KEY_HTTP_REQUEST_TIMEOUT)));
				Config.putConfig("HTTP.REQUEST.TIMEOUT", Integer.toString(clientStore.getInt(LoginConstant.KEY_HTTP_REQUEST_TIMEOUT)));
				
				if (backup)
					fileList = BackupUtil.getBackupList(info.getDefaultXML());
				else
					fileList = ReadXMLAction.getXMLFile(info.getDefaultXML());
				
				switch (commandType) {
				case Import:
					{
						result = actionClass.importMaster(
								(ArrayList<String>)fileList,
								(ArrayList<String>)idList);
					}
					break;
				case Export:
					{
						result = actionClass.exportMaster(
								(ArrayList<String>)fileList,
								(ArrayList<String>)idList);
					}
					break;
				case Clear:
					{
						result = actionClass.clearMaster(
								(ArrayList<String>)idList);
					}
					break;
				case Diff:
					{
						result = SettingConstants.ERROR_INPROCESS;
					}
				}
				// FIXME 並列動作時に別スレッドの動作ログが混在することがあるなら スレッド名でログをフィルタすること。
				out = byteWriter.toString();
				error = byteStream.toString("UTF-8");
			}
			catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			finally {
				if (out != null) {
					m_stdout = m_stdout + out + "\n";
				}
				
				if (error != null) {
					m_errout = m_errout + error + "\n";
				}

				if (logger != null) {
					// 不要になったappenderを削除
					Log4J2Util.removeAppenderFromLogger(Thread.currentThread().getName(),logger.getName());
				}
				
				System.setErr(oldError);
			}
		}
		else if (SdmlUtilityInterfaceNoEclipse.isSdmlFunction(info)) {
			String[] args = createArgs(info, commandType, backup);
			// SDMLの場合
			// 本体側からSDMLClientOptionのクラスが見えないため、ClientOption側でLauncherを起動する
			SdmlUtilityActionResult resBean = SdmlUtilityInterfaceNoEclipse.launchActionLauncher(args);

			result = resBean.getResult();
			if (resBean.getStdOut() != null) {
				m_stdout = m_stdout + resBean.getStdOut() + "\n";
			}
			if (resBean.getErrOut() != null) {
				m_errout = m_errout + resBean.getErrOut() + "\n";
			}
		}
		else {
			String[] args = createArgs(info, commandType, backup);
			
			WSActionLauncher helper = new WSActionLauncher(args);
			try {
				result = helper.action();
			}
			catch(ConvertorException e) {
				log.error(e.getMessage(), e);
			}
			finally {
				if (helper.getStdOut() != null) {
					m_stdout = m_stdout + helper.getStdOut() + "\n";
				}
				
				if (helper.getErrOut() != null) {
					m_errout = m_errout + helper.getErrOut() + "\n";
				}
			}
		}

		return result;
	}
	
	/**
	 * 引数を生成する。
	 * 
	 * @param funcInfo
	 * @param commandType
	 * @return
	 */
	private String[] createArgs(FuncInfo funcInfo, CommandType commandType, boolean backup) {
		String[] args = null;

		switch (commandType) {
		case Diff:
			{
				List<String> defaultFileList = funcInfo.getDefaultXML();
				List<ReadXMLAction.DiffFilePaths> fileList = ReadXMLAction.getDiffXMLFiles(defaultFileList);

				args = new String[2 + fileList.size() * 2];
			   	args[0] = funcInfo.getActionClassName();
				args[1] = commandType.getCommandName();
				
				for (int i = 2; i < fileList.size() + 2; ++i) {
					args[i] = fileList.get(i - 2).filePath1;
				}

				for (int i = 2 + fileList.size(); i < fileList.size() * 2 + 2; ++i) {
					args[i] = fileList.get(i - (2 + fileList.size())).filePath2;
				}
			}
			break;
		default:
			AccountInfo accountInfo = getCurrentAccountInfo();
		   	if (accountInfo != null) {
				List<String> fileList = null;
				if (backup)
					fileList = BackupUtil.getBackupList(funcInfo.getDefaultXML());
				else
					fileList = ReadXMLAction.getXMLFile(funcInfo.getDefaultXML());

				args = new String[6 + (commandType != CommandType.Clear ? funcInfo.getDefaultXML().size(): 0)];
			   	args[0] = funcInfo.getActionClassName();
				args[1] = commandType.getCommandName();
				args[2] = accountInfo.url;
				args[3] = accountInfo.userid;

				IUtilityPreferenceStore clientStore = UtilityPreferenceStore.get();
				args[4] = Integer.toString(clientStore.getInt(LoginConstant.KEY_HTTP_REQUEST_TIMEOUT));
				args[5] = Integer.toString(clientStore.getInt(LoginConstant.KEY_HTTP_REQUEST_TIMEOUT));

				if (commandType != CommandType.Clear) {
					for (int i = 0; i < fileList.size(); ++i) {
						args[6 + i] = fileList.get(i);
					}
				}
		   	}
		}

		return args;
	}
	
	public int deleteCommand(FuncInfo info, List<String> idList) {
		log.debug("begin deleteCommand()");
		int result = doCommand(CommandType.Clear, info, null, idList);
		log.debug("end exportCommand()");
		
		return result;
	}

	public String getStdOut() {
		return filteringStdOut(m_stdout);
	}

	public String getErrOut() {
		return m_errout;
	}

	public List<List<String>> getJobunitList() {
		List<List<String>> result = new ArrayList<>();
		
		try {
			JobMasterAction action = new JobMasterAction();
			result = action.getJobunitList();
		} catch (SecurityException e) {
			log.error(e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			log.error(e.getMessage(), e);
		}

		return result;

	}

	public List<List<String>> getJobunitListFromXML(String fileName) {
		List<List<String>> result = new ArrayList<>();

    	AccountInfo accountInfo = getCurrentAccountInfo();

		if (accountInfo != null) {
			String uid = accountInfo.userid;

			try {
				JobMasterAction action = new JobMasterAction();
				result = action.getJobunitListFromXML(fileName, uid);
			} catch (SecurityException e) {
				log.error(e.getMessage(), e);
			} catch (IllegalArgumentException e) {
				log.error(e.getMessage(), e);
			}
		}

		return result;

	}
	
	/**
	 * 接続先およびアカウント情報が格納されています。
	 * 
	 * @version 2.0.0
	 * @since 2.0.0
	 * 
	 */
	public static class AccountInfo {
		public AccountInfo(String url, String userid) {
			this.url = url;
			this.userid = userid;
		}
		
		public final String url;
		public final String userid;
	}
	
	/**
	 * Hinemos クライアントから、現在ログオンしているアカウントを取得します。<BR>
	 * 
	 * @return 未接続のときなど null が返る場合あり。
	 */
	public AccountInfo getCurrentAccountInfo() {
		if (accountInfoProvider == null) {
			accountInfoProvider = new AccountInfoProvider();
		}
		return accountInfoProvider.getCurrentAccountInfo();
	}
	
	/**
	 * インポート対象機能毎の一括インポート単位数を取得し反映する<BR>
	 * 
	 */
	private static void refrectImportUnitNum(FuncInfo info) throws RestConnectFailed ,HinemosUnknown , InvalidRole,InvalidUserPass {
		//設定状況を確認し、設定済みならそちらを優先（portingからの呼び出しでなければ設定済み）
		if (ImportProcessMode.getImportUnitNum() != null) {
			return;
		}
		List<String> param = new ArrayList<String>();
		param.add(info.getId());
		Map<String,Integer> retMap = SettingUtil.getImportUnitNumList( param);
		// 取得できたら動作設定に反映
		if(retMap!=null){
			ImportProcessMode.setImportUnitNum(retMap.get(info.getId()));
		}
	}	
	/**
	 * StdOut内の不要なログをfilterする<BR>
	 * 
	 */
	private static String filteringStdOut(String out) {
		String[] messages = out.split("\n");
		StringBuilder ret = new StringBuilder();
		for (String rec : messages) {
			boolean isFilterTarget = false;
			for (String filter : STDOUT_FILITER_ARRAYS) {
				if (rec.indexOf(filter) >= 0) {
					isFilterTarget = true;
					break;
				}
			}
			if (isFilterTarget) {
				continue;
			}
			ret.append(rec + "\n");
		}
		return ret.toString();
	}
}