/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.platform.util.reporting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Field;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.util.NotifyCallback;
import com.clustercontrol.platform.HinemosPropertyDefault;
import com.clustercontrol.reporting.bean.ReportingInfo;
import com.clustercontrol.reporting.bean.ReportingTypeConstant;
import com.clustercontrol.reporting.factory.Notice;
import com.clustercontrol.reporting.util.ReportFileFailureListManager;
import com.clustercontrol.rest.endpoint.reporting.dto.CreateReportingFileRequest;
import com.clustercontrol.util.CommandCreator;
import com.clustercontrol.util.CommandExecutor;
import com.clustercontrol.util.CommandExecutor.CommandResult;
import com.clustercontrol.util.Messages;
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
		// ベースディレクトリの確認
		if (!Files.exists(Paths.get(basePath))) {
			try {
				Files.createDirectory(Paths.get(basePath));
				m_log.info("create directory=" + basePath);
			} catch (FileAlreadyExistsException e) {
				m_log.info("already created directory=" + basePath);
			} catch (IOException e) {
				//ここには入らないはず
				m_log.warn("create failed=" + basePath+", "+e.getMessage());
			}
		}
		return basePath;
	}

	/**
	 * @param info
	 * @param tmpReportingInfo
	 * @return 作成されるレポートファイル名のリスト
	 * @throws InvalidSetting
	 */
	public static synchronized List<String> execute(ReportingInfo info) throws InvalidSetting {
		return execute(info, null);
	}

	/**
	 * @param info
	 * @param dtoReq
	 * @return 作成されるレポートファイル名のリスト
	 * @throws InvalidSetting
	 */
	public static synchronized List<String> execute(ReportingInfo info, CreateReportingFileRequest dtoReq) throws InvalidSetting {

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
					" -javaagent:" + System.getProperty("hinemos.manager.home.dir", "/opt/hinemos") + "/lib/eclipselink-3.0.2.jar";
					
			// OSがWindows以外の場合、OOMの情報出力する
			String osName = System.getProperty("os.name");
			m_log.info("os name : " + osName);
			if ( osName != null && !osName.startsWith("Windows") ) {
				javaOpts = javaOpts + 
						" -XX:OnOutOfMemoryError='jmap -histo %p >> " + System.getProperty("hinemos.manager.log.dir", "/opt/hinemos/var/log") 
						+ File.separator + "reporting_outofmemory_histgram.log'";
			}
			
			String addOpts = "";
			if (dtoReq != null) {
				if (dtoReq.getOutputPeriodType() != null &&
						dtoReq.getOutputPeriodBefore() != null &&
						dtoReq.getOutputPeriodFor() != null) {
					// 即時実行では期間のみ変更可能
					StringBuilder builder = new StringBuilder("");
					builder.append(" -Dhinemos.reporting.output.period.type=" + dtoReq.getOutputPeriodType().getCode());
					builder.append(" -Dhinemos.reporting.output.period.before=" + dtoReq.getOutputPeriodBefore());
					builder.append(" -Dhinemos.reporting.output.period.for=" + dtoReq.getOutputPeriodFor());
					addOpts = builder.toString();
				} else {
					throw new InvalidSetting(Messages.getString("MESSAGE_REPORTING_17"));
				}
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
			String[] jarFileList = {"castor-core-", "castor-xml-", "commons-beanutils-", "commons-collections-", "commons-collections4-", "commons-digester-", "commons-lang3-", 
					"ecj-", "itext-", "iTextAsian", "iTextAsianCmaps", "jakarta.inject-api-", "jasperreports-functions-", "jasperreports-fonts-", "jasperreports-", 
					"jcommon-", "jfreechart-", "poi-", "SparseBitSet-"};
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
					AplLogger.put(InternalIdCommon.REPORTING_SYS_001, 
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
			if (dtoReq != null && !dtoReq.getNotifyFlg()) {
				notify = false;
			}

			String threadName = "createReportFileTaskThread";
			int numMultiplicityLimit = HinemosPropertyDefault.reporting_create_process_multiplicity_limit.getIntegerValue();

			m_log.info("Property reporting.createReportFileTask.multipicity.limit : " + numMultiplicityLimit + " .");
			if (numMultiplicityLimit == 0) {
				m_log.info(threadName + " multiplicity control is disable.");
			} else if (numMultiplicityLimit < 0 || numMultiplicityLimit > 1024) {
				m_log.warn("Invalid value of property.");
				m_log.info(threadName + " multiplicity control is disable.");
			} else {
				int numFileTaskRunnning = 0;
				m_log.info(threadName + " multiplicity control is enable.");

				/* 実行中のCreateReportFileTaskの数をカウントする */
				m_log.debug("count " + threadName + " start.");

				ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
				long[] threadIds = threadBean.getAllThreadIds();
				m_log.debug("There are " + threadIds.length + " threads in Manager.");
				ThreadInfo[] threadInfoList = threadBean.getThreadInfo(threadIds, 0);

				for (ThreadInfo threadInfo : threadInfoList) {
					if (threadInfo != null && threadName.equals(threadInfo.getThreadName())) {
						m_log.debug(threadName + " found, " + String.format("%#016x", threadInfo.getThreadId()));
						numFileTaskRunnning++;
					}
				}
				m_log.debug("count " + threadName + " end.");
				m_log.info("running " + threadName + " : " + numFileTaskRunnning);

				/* プロパティでの多重度制限を超えたら実行せずに終了 */
				if (numFileTaskRunnning < numMultiplicityLimit) {
					m_log.debug(threadName + " is going to run.");
				} else {
					m_log.warn(threadName + " multiplicity exceeded.");
					AplLogger.put(InternalIdCommon.REPORTING_SYS_002,
							new String[] { info.getReportScheduleId() });
					return fileList;
				}
			}

			Thread thread = new Thread(new CreateReportFileTask(info, command, fileList, notify));
			thread.setName(threadName);
			// Manager終了時にこのスレッドの終了を待たない
			thread.setDaemon(true);
			thread.start();
		} catch (InvalidSetting e) {
			throw e;
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
				Process process = executor.execute();

				// タイムアウトした場合に終了させるため、子プロセスのpidを取得しておく
				List<Integer> childrenPid = getChildrenPid(getPid(process));

				CommandResult result = executor.getResult();
				if (result == null) {
					// タイムアウト等
					m_log.warn("run() failed. (command = " + m_command + ")");
					if (m_notify) {
						notifyInfo = new Notice().createOutputBasicInfo(m_reportId, null, PriorityConstant.TYPE_CRITICAL, returnValue);
					}
					// 親プロセスだけが終了し、子プロセスが残るため、子プロセスを終了させる
					killProcesses(childrenPid);
					// 一応、親プロセス終了
					process.destroy();
					
					// コマンド失敗は一覧に登録
					ReportFileFailureListManager.regist(m_fileList.get(0));
				} else {
					m_log.info("run() executed command. (exitCode = " + result.exitCode + ", command = " + m_command + ")");
					returnValue = result.exitCode;
					if (returnValue != COMMAND_SUCCESS_EXIT) {
						m_log.warn("command execution failure. (command = " + m_command + ", exit code = " + returnValue + ")");
						if (m_notify) {
							notifyInfo = new Notice().createOutputBasicInfo(m_reportId, null, PriorityConstant.TYPE_CRITICAL, returnValue);
						}
						// コマンド失敗は一覧に登録
						ReportFileFailureListManager.regist(m_fileList.get(0));
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
				if (m_notify) {
					jtm.addCallback(new NotifyCallback(notifyInfo));
				}

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

		/**
		 * プロセスIDを取得します。
		 * 
		 * @param process
		 *            プロセス
		 * @return プロセスID、取得に失敗した場合は-1
		 */
		private int getPid(Process process) {
			m_log.debug("getPid(" + process + ")");

			int pid = -1;
			try {
				Field f = process.getClass().getDeclaredField("pid");
				f.setAccessible(true);
				pid = (int) f.get(process);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				m_log.warn("fail to get pid. " + e);
			}

			m_log.trace("pid=" + pid);
			return pid;
		}

		/**
		 * 子プロセスのプロセスIDを取得します。
		 * 
		 * @param pid
		 *            親プロセスID
		 * @return 子プロセスIDリスト、取得できない場合はnull
		 */
		private List<Integer> getChildrenPid(int pid) {
			m_log.debug("getChildrenPid(" + pid + ")");

			if (pid < 0) {
				return null;
			}

			// 子プロセスID取得
			Process psCommand = null;
			int exitValue = 1;
			try {
				String command = "ps --ppid " + pid + " -o pid --no-heading";
				m_log.debug("get children pid, command=" + command);
				psCommand = Runtime.getRuntime().exec(command);
				exitValue = psCommand.waitFor();
			} catch (IOException | InterruptedException e) {
				m_log.warn("fail to exec command. " + e);
				return null;
			}
			// 正常終了（戻り値が0）をチェック
			m_log.trace("exitValue=" + exitValue);
			if (exitValue != 0) {
				m_log.warn("ps command abnormal exit. exit code=" + exitValue);
				return null;
			}

			List<Integer> childrenPidList = new ArrayList<>();
			try (InputStream is = psCommand.getInputStream();
					BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
				String childPid = "";
				while ((childPid = br.readLine()) != null) {
					m_log.trace("childPid=" + childPid);
					if (childPid.equals("")) {
						continue;
					}
					childrenPidList.add(Integer.parseInt(childPid.trim()));
				}
			} catch (NumberFormatException | IOException e) {
				m_log.warn("fail to read pid. " + e);
				return null;
			}

			m_log.trace("childrenPidList=" + Arrays.toString(childrenPidList.toArray()));
			return childrenPidList;
		}

		/**
		 * プロセスを終了させます。
		 * 
		 * @param pidList
		 *            プロセスIDのリスト
		 * @return 1つでも正しく終了できなかった場合false、それ以外はtrue、ただし引数がnullや空の場合はtrue
		 *         現状、戻り値を使用していないが、将来の拡張性のために設定している。
		 */
		private boolean killProcesses(List<Integer> pidList) {
			if (pidList == null || pidList.size() <= 0) {
				return true;
			}
			m_log.debug("killProcesses(" + Arrays.toString(pidList.toArray()) + ")");

			boolean normalExit = true;
			try {
				for (Integer pid : pidList) {
					// プロセスにシグナル送信（15、SIGTERM、終了）
					String command = "kill -15 " + pid;
					m_log.info("kill process, command=" + command);
					Process killCommand = Runtime.getRuntime().exec(command);
					int exitValue = killCommand.waitFor();
					m_log.trace("exitValue=" + exitValue);
					// 正常終了（戻り値が0）をチェック
					if (exitValue != 0) {
						normalExit = false;
					}
				}
			} catch (IOException | InterruptedException e) {
				m_log.warn("fail to exec command. " + e);
				return false;
			}

			m_log.trace("normalExit=" + normalExit);
			return normalExit;
		}
	}
}
