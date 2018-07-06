/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.platform.util.reporting;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.util.NotifyCallback;
import com.clustercontrol.platform.HinemosPropertyDefault;
import com.clustercontrol.reporting.bean.ReportingTypeConstant;
import com.clustercontrol.reporting.bean.ReportingInfo;
import com.clustercontrol.reporting.factory.Notice;
import com.clustercontrol.util.CommandCreator;
import com.clustercontrol.util.CommandExecutor;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.CommandExecutor.CommandResult;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * 
 * レポーティング機能JVMを別スレッドで実行するクラスです。(for RHEL)
 * 
 * @version 4.1.2
 *
 */
public class ExecReportingProcess {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( ExecReportingProcess.class );

	private static final int COMMAND_SUCCESS_EXIT = 0;
	private static final CommandCreator.PlatformType _modeType = CommandCreator.convertPlatform("");
	private static String basePath = null;
	private static long timeout = 1800;

	/**
	 * @return レポートファイルを出力するディレクトリのパス
	 */
	public static String getBasePath() {
		return basePath;
	}

	/**
	 * @param info
	 * @param tmpReportingInfo
	 * @return 作成されるレポートファイル名のリスト
	 */
	public static synchronized List<String> execute(ReportingInfo info) {
		return execute(info, null);
	}

	/**
	 * @param info
	 * @param tmpReportingInfo
	 * @return 作成されるレポートファイル名のリスト
	 */
	public static synchronized List<String> execute(ReportingInfo info, ReportingInfo tmpReportingInfo) {

		List<String> fileList = null;
		String reportId = null;
		String outFileName = null;
		String outFilePath = null;
		
		basePath = System.getProperty("hinemos.manager.home.dir", "/opt/hinemos") + "/var/report";
		timeout = 1800;
		
		try {
			basePath = HinemosPropertyDefault.reporting_output_path.getStringValue();
			timeout = HinemosPropertyDefault.reporting_create_timeout.getIntegerValue();
			m_log.info("reporting.output.path = " + basePath);
			m_log.info("reporting.create.timeout = " + timeout);
			
			// ファイル名作成
			Date date = new Date();
			SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
			String fileBaseDir = fmt.format(date);
			String baseName = HinemosPropertyDefault.reporting_filename.getStringValue();
			fmt.applyPattern("yyyyMMddHHmmss");
			reportId = info.getReportScheduleId();
			// YYYYMMDD/hinemos_report_(reportID)_YYYYMMDDhhmmss.pdf
			outFileName = fileBaseDir + File.separator + baseName + "_" + reportId + "_" + fmt.format(date)+ "." + ReportingTypeConstant.outputTypeToString(info.getOutputType());
			outFilePath = getBasePath() + File.separator + outFileName;
			m_log.debug("execute() basePath = " + getBasePath() + ", outFileName = " + outFileName + ", reportId = " + reportId);

			fileList = new ArrayList<>();
			fileList.add(outFileName);

			// コマンドライン文字列作成
			String javaOpts = "-server" +
					" -Dprogram.name=hinemos_reporting" +
					" -Dhinemos.manager.home.dir=" + System.getProperty("hinemos.manager.home.dir", "/opt/hinemos") +
					" -Dhinemos.manager.etc.dir=" + System.getProperty("hinemos.manager.etc.dir", "/opt/hinemos/etc") +
					" -Dhinemos.manager.log.dir=" + System.getProperty("hinemos.manager.log.dir", "/opt/hinemos/var/log") +
					" " + HinemosPropertyDefault.reporting_heap_size.getStringValue() +
					" -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=" + System.getProperty("hinemos.manager.log.dir", "/opt/hinemos/var/log") + 
					" -javaagent:" + System.getProperty("hinemos.manager.home.dir", "/opt/hinemos") + "/lib/eclipselink.jar";
					
			// OSがWindows以外の場合、OOMの情報出力する
			String osName = System.getProperty("os.name");
			m_log.info("os name : " + osName);
			if ( osName != null && !osName.startsWith("Windows") ) {
				javaOpts = javaOpts + 
						" -XX:OnOutOfMemoryError='jmap -histo %p >> " + System.getProperty("hinemos.manager.log.dir", "/opt/hinemos/var/log") 
						+ File.separator + "reporting_outofmemory_histgram.log'";
			}
			
			String addOpts = "";
			if (tmpReportingInfo != null &&
					tmpReportingInfo.getOutputPeriodType() != null &&
					tmpReportingInfo.getOutputPeriodBefore() != null &&
					tmpReportingInfo.getOutputPeriodFor() != null) {
				// 即時実行では期間のみ変更可能
				StringBuilder builder = new StringBuilder("");
				builder.append(" -Dhinemos.reporting.output.period.type=" + tmpReportingInfo.getOutputPeriodType());
				builder.append(" -Dhinemos.reporting.output.period.before=" + tmpReportingInfo.getOutputPeriodBefore());
				builder.append(" -Dhinemos.reporting.output.period.for=" + tmpReportingInfo.getOutputPeriodFor());
				addOpts = builder.toString();
			}

			// Generate classpath
			Path libDir = Paths.get(System.getProperty("hinemos.manager.home.dir", "/opt/hinemos"), "lib", "reporting");
			StringJoiner pathJoiner = new StringJoiner(File.pathSeparator);
			// First, add etc folder
			pathJoiner.add(Paths.get(System.getProperty("hinemos.manager.home.dir", "/opt/hinemos"), "etc").toString());
			// Second, add lib folder
			pathJoiner.add(Paths.get(System.getProperty("hinemos.manager.home.dir", "/opt/hinemos"), "lib").toString());

			// レポーティング用のライブラリーファイルのパスを動的に作成
			// Managerと共通のものは全部linkとして、libDirにおいておくことが前提(installerで対応)
			String[] libFileList = libDir.toFile().list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name){
					return name.endsWith(".jar");
				}
			});

			if (libFileList != null) {
				for(String libFile: libFileList) {
					pathJoiner.add(libDir.resolve(libFile).toString());
				}
			}
			
			//レポート作成に必要なjarファイルが配置されているか確認
			String[] jarFileList = {"commons-beanutils-", "commons-collections-", "commons-digester-", 
					"ecj-", "iText-", "iTextAsian", "iTextAsianCmaps", "jasperreports-functions-",
					"jasperreports-fonts-", "jasperreports-", "jcommon-", "jfreechart-", "poi-"};
			int countJars = 0;
			if (libFileList != null) {
				for(String libFile: libFileList) {
					for(String jarFile: jarFileList){
						if(libFile.startsWith(jarFile)){
							countJars++;
							break;
						}
					}
				}
				if(countJars != jarFileList.length){
					AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.REPORTING,
							MessageConstant.MESSAGE_REPORTING_JARFILES_NOT_FOUND, 
							new String[] {info.getReportScheduleId()});
					return fileList;
				}
			}

			// After all, quote the whole argument to prevent whitespace in path
			String classPath = " -cp \"" + pathJoiner.toString() + "\"";

			String outputStdoutLog = "> " + System.getProperty("hinemos.manager.log.dir", System.getProperty("hinemos.manager.home.dir", "/opt/hinemos") + "/var/log") 
					+ File.separator + "reporting_stdout.log" + " 2>&1";
			
			String command = "java " + javaOpts + addOpts + classPath +
					" com.clustercontrol.reporting.ReportMain" +
					" " + outFilePath +
					" " + reportId +
					" " + outputStdoutLog;
			
			m_log.info("execute() command = " + command);

			// 即時実行時は通知の選択可
			boolean notify = true;
			if (tmpReportingInfo != null &&
					(tmpReportingInfo.getNotifyGroupId() == null || tmpReportingInfo.getNotifyGroupId().isEmpty())) {
				notify = false;
			}

			Thread thread = new Thread(new CreateReportFileTask(info, command, fileList, notify));
			// Manager終了時にこのスレッドの終了を待たない
			thread.setDaemon(true);
			thread.start();
		} catch (Exception e) {
			m_log.warn(e.getMessage(), e);
		} finally {
		}

		return fileList;
	}

	/**
	 * ダウンロードするファイルを作成するJVMを別スレッドから実行する処理クラス
	 * 
	 */
	private static class CreateReportFileTask implements Runnable {
		private String m_command = null;
		private List<String> m_fileList = null;
		private String m_reportId = "";
		private boolean m_notify = true;

		private CreateReportFileTask(ReportingInfo info, String command, List<String> fileList, boolean notify) {
			this.m_command = command;
			this.m_fileList = fileList;
			this.m_reportId = info.getReportScheduleId();
			this.m_notify = notify;

			m_log.debug("CreateReportFileTask Create : reportId = " + info.getReportScheduleId());
		}

		@Override
		public void run() {
			m_log.info("CreateReportFileTask start!");

			JpaTransactionManager jtm = null;
			OutputBasicInfo notifyInfo = null;

			try {
				jtm = new JpaTransactionManager();
				jtm.begin();

				int returnValue = -1;
				String[] cmd = CommandCreator.createCommand(null, m_command, _modeType, false);
				CommandExecutor executor = new CommandExecutor(cmd, timeout * 1000);
				executor.execute();
				CommandResult result = executor.getResult();

				if (result == null) {
					m_log.warn("run() failed. (command = " + m_command + ")");
					if (m_notify) {
						notifyInfo = new Notice().createOutputBasicInfo(m_reportId, null, PriorityConstant.TYPE_CRITICAL, returnValue);
					}
				} else {
					m_log.info("run() executed command. (exitCode = " + result.exitCode + ", command = " + m_command + ")");
					returnValue = result.exitCode;
					if (returnValue != COMMAND_SUCCESS_EXIT) {
						m_log.warn("command execution failure. (command = " + m_command + ", exit code = " + returnValue + ")");
						if (m_notify) {
							notifyInfo = new Notice().createOutputBasicInfo(m_reportId, null, PriorityConstant.TYPE_CRITICAL, returnValue);
						}
					} else {
						String outFile = getBasePath() + File.separator + m_fileList.get(0);
						m_log.debug("outFile = " + outFile);
						File file = new File(outFile);
						if (file.exists()) {
							if (m_notify) {
								notifyInfo = new Notice().createOutputBasicInfo(m_reportId, outFile, PriorityConstant.TYPE_INFO, returnValue);
							}
						} else {
							m_log.warn("file not created. (command = " + m_command + ", exit code = " + returnValue + ")");
							if (m_notify) {
								notifyInfo = new Notice().createOutputBasicInfo(m_reportId, null, PriorityConstant.TYPE_CRITICAL, returnValue);
							}
						}
					}
				}

				// 通知設定
				jtm.addCallback(new NotifyCallback(notifyInfo));

				// 終了処理
				jtm.commit();
			} catch (Exception e) {
				m_log.warn(e.getMessage(), e);
				if (jtm != null) {
					jtm.rollback();
				}
			} finally {
				if (jtm != null)
					jtm.close();
			}
		}
	}
}
