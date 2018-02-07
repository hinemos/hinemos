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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.eclipse.jface.preference.IPreferenceStore;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.EndpointUnit;
import com.clustercontrol.util.LoginManager;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.settings.WSActionLauncher;
import com.clustercontrol.utility.settings.job.action.JobMasterAction;
import com.clustercontrol.utility.settings.ui.bean.FuncInfo;
import com.clustercontrol.utility.settings.ui.constant.CommandConstant;
import com.clustercontrol.utility.settings.ui.util.BackupUtil;
import com.clustercontrol.utility.util.Config;

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
	
    /* ロガー */
	private static Log log = LogFactory.getLog(CommandAction.class);

	private String m_stdout = "";
	private String m_errout = "";

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
	private int doCommand(CommandType commandType, FuncInfo info, List<String> fileList, List<String> idList,boolean backup) {
		int result = -1;
		
		switch (commandType) {
		case Import:
		case Export:
		case Clear:
			if (!LoginManager.isLogin()) {
				return -1;
			}
			break;
		case Diff:
			break;
		}
	
		if (commandType != CommandType.Diff && info.getActionClassName().equals(CommandConstant.ACTION_JOB_MST)) {
			Logger logger = null;
			CharArrayWriter byteWriter = new CharArrayWriter(8192);
			WriterAppender appender = new WriterAppender(new PatternLayout("%d %-5p [%t] [%c] %m%n"), byteWriter);
			PrintStream oldError = System.err;
			String out = "";
			String error = "";
			
			AccountInfo accountInfo = getCurrentAccountInfo();
			assert accountInfo != null : "unexpected";
			log.debug(accountInfo.userid + ", " + accountInfo.url);

			try {
				JobMasterAction actionClass = new JobMasterAction();
				logger = actionClass.getLogger();
				
				appender.setEncoding("UTF-8");
				logger.addAppender(appender);

				ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
				System.setErr(new PrintStream(byteStream));
				
				Config.putConfig("Login.URL", accountInfo.url);
				Config.putConfig("Login.USER", accountInfo.userid);
				IPreferenceStore clientStore = ClusterControlPlugin.getDefault().getPreferenceStore();
				Config.putConfig("HTTP.CONNECT.TIMEOUT", Integer.toString(clientStore.getInt(LoginManager.KEY_HTTP_REQUEST_TIMEOUT)));
				Config.putConfig("HTTP.REQUEST.TIMEOUT", Integer.toString(clientStore.getInt(LoginManager.KEY_HTTP_REQUEST_TIMEOUT)));
				
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
					logger.removeAppender(appender);
				}
				
				System.setErr(oldError);
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

				IPreferenceStore clientStore = ClusterControlPlugin.getDefault().getPreferenceStore();
				args[4] = Integer.toString(clientStore.getInt(LoginManager.KEY_HTTP_REQUEST_TIMEOUT));
				args[5] = Integer.toString(clientStore.getInt(LoginManager.KEY_HTTP_REQUEST_TIMEOUT));

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
		return m_stdout;
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
	public static AccountInfo getCurrentAccountInfo() {
		AccountInfo accountInfo = null;

		if (LoginManager.isLogin()) {
			EndpointUnit unit = EndpointManager.get(ClusterControlPlugin.getDefault().getCurrentManagerName());
			accountInfo = new AccountInfo(
					unit.getUrlListStr(),
					unit.getUserId()
				);
		}
		
		return accountInfo;
	}
}