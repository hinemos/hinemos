/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.job;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtJobEnvVariableInfoResponse;
import org.openapitools.client.model.AgtRunInstructionInfoResponse;
import org.openapitools.client.model.GetScriptResponse;
import org.openapitools.client.model.SetJobOutputResultRequest;
import org.openapitools.client.model.SetJobResultRequest;
import org.openapitools.client.model.SetJobStartRequest;

import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.SendQueue;
import com.clustercontrol.agent.SendQueue.JobResultSendableObject;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.agent.util.AgentRequestId;
import com.clustercontrol.agent.util.AgentRestClientEx;
import com.clustercontrol.agent.util.AgentRestClientEx.RetryTimeoutException;
import com.clustercontrol.agent.util.JobCommandExecutor;
import com.clustercontrol.agent.util.JobCommandExecutor.CommandResult;
import com.clustercontrol.agent.util.OutputString;
import com.clustercontrol.agent.util.RestAgentBeanUtil;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.util.CommandCreator;
import com.clustercontrol.util.CommandExecutor;
import com.clustercontrol.util.HinemosTime;

/**
 * コマンドを実行するスレッドクラス<BR>
 * 
 * ジョブ実行の際にプロセスを生成して、 終了まで、状態を監視するクラスです。
 * 
 */
public class CommandThread extends AgentThread {

	// ロガー
	static private Log m_log = LogFactory.getLog(CommandThread.class);

	// ジョブ実行結果を受け取る際のエンコーディング
	private String m_inputEncoding = null;
	// ジョブ実行後のスクリプト削除有無
	private boolean m_scriptDelete = false;

	private Process process = null;
	private JobCommandExecutor cmdExec = null;
	private JobResultSendableObject jobResult;
	private String scriptDir = Agent.getAgentHome() + "script/";;
	private String scriptFile;
	
	private final String REPLACE_STARTCOMMAND = "#[SCRIPT]";


	/**
	 * デバッグ用メイン処理
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			CommandExecutor executor1 = new CommandExecutor(new String[] { "hostname" });
			//System.out.println(executor1.getResult(executor1.execute()).stdout);
			executor1.addEnvironment("HINEMOS_AGENT_HOME", Agent.getAgentHome());
			executor1.execute();
			System.out.println(executor1.getResult().stdout);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * デバッグ用コンストラクタ
	 */
	public CommandThread() {
		super();
	}

	/**
	 * コンストラクタ
	 * 
	 * @param info
	 *			  実行指示
	 * @param sendQueue
	 *			  実行応答用メッセージ送信クラス
	 * @param runHistory
	 *			  実行履歴
	 */
	public CommandThread(AgtRunInstructionInfoResponse info, SendQueue sendQueue) {
		super(info, sendQueue);

		// ログファイルのエンコーディングを設定
		m_inputEncoding =  AgentProperties.getProperty("job.stream.charset");
		if(m_inputEncoding == null){
			m_inputEncoding = System.getProperty("file.encoding");
		}
		// スクリプト削除有無フラグを設定
		m_scriptDelete = Boolean.parseBoolean(AgentProperties.getProperty("job.script.delete", "true"));
		m_log.info("job.stream.charset = " + m_inputEncoding + " job.script.delete = " + m_scriptDelete);

		// 開始メッセージ作成
		jobResult = new JobResultSendableObject();
		jobResult.sessionId = m_info.getSessionId();
		jobResult.jobunitId = m_info.getJobunitId();
		jobResult.jobId = m_info.getJobId();
		jobResult.facilityId = m_info.getFacilityId();
		jobResult.body = new SetJobResultRequest();
		jobResult.body.setCommand(m_info.getCommand());
		jobResult.body.setCommandType(m_info.getCommandType());
		jobResult.body.setStopType(m_info.getStopType());
		jobResult.body.setStatus(RunStatusConstant.START);
		jobResult.body.setTime(HinemosTime.getDateInstance().getTime());
	}
	
	/**
	 * スレッドを開始する前に実行すること。
	 * ジョブの開始メッセージ送信を行い、コマンドジョブに#[SCRIPT]が含まれていた場合、スクリプトの取得を実行する。
	 * @param timeout マネージャアクセスの再実行
	 * @throws RetryTimeoutException
	 */
	public void init(long timeout) throws RetryTimeoutException {
		// ---------------------------
		// -- 開始メッセージ送信
		// ---------------------------
		SetJobStartRequest setJobStartRequest = new SetJobStartRequest();
		try {
			RestAgentBeanUtil.convertBeanSimple(jobResult.body, setJobStartRequest);
		} catch (HinemosUnknown e) {
			m_log.error("CommandThread() : " + e.getMessage(), e);
			return;
		}

		m_log.info("run SessionID=" + jobResult.sessionId + ", JobID=" + jobResult.jobId);

		// Hinemosマネージャに開始メッセージ送信
		// マネージャに開始メッセージが届く前にジョブのコマンドが実行されることと
		// VIPの切り替えが起こった場合に、ジョブが複数のエージェントで起動することを防ぐために
		// ジョブの開始報告は同期した動作とする
		try {
			AgentRequestId agentRequestId = new AgentRequestId();
			if (!AgentRestClientEx.setJobStart(
					jobResult.sessionId, jobResult.jobunitId, jobResult.jobId, jobResult.facilityId,
					setJobStartRequest, agentRequestId.toRequestHeaderValue(),
					timeout)) {
				// ジョブがすでに別エージェントで起動しているか、終了している場合
				return;
			}
		} catch (RetryTimeoutException e) {
			throw e;
		} catch (Exception e) {
			m_log.warn(e.getMessage(), e);
			return;
		}

		// mode取得
		String mode = AgentProperties.getProperty("job.command.mode");
		CommandCreator.PlatformType platform = CommandCreator.convertPlatform(mode);

		// スクリプトの取得
		if (m_info.getCommand().contains(REPLACE_STARTCOMMAND)) {
			
			GetScriptResponse res = null;
			String errorMessage = "script cannot be retrieved in time, so the job is terminated.";
			try {
				res = AgentRestClientEx.getScript(jobResult.sessionId, jobResult.jobunitId, jobResult.jobId, timeout);
			} catch (AgentRestClientEx.RetryTimeoutException e) {
				errorMessage = errorMessage + " timeout occured. elapsedtime=" + timeout + "ms";
				throw e;
			} catch (Exception e) {
				errorMessage = errorMessage + e.getMessage();
				return;
			} finally {
				if (res == null) {
					// マネージャでコマンド終了待ちになっているため、終了通知を送る
					sendJobResultError(errorMessage);
				}
			}
			try {
				if (!res.getEmpty().booleanValue()) {
					scriptFile = scriptDir + m_info.getSessionId() + "_" + m_info.getJobId() + "_" + res.getScriptName();
					createScriptFile(res.getScriptContent(), res.getScriptEncoding());

					if ((platform == CommandCreator.PlatformType.AUTO
						&& CommandCreator.sysPlatform == CommandCreator.PlatformType.WINDOWS_CMD)
						|| platform == CommandCreator.PlatformType.WINDOWS_CMD) {
						m_info.setCommand(m_info.getCommand().replace(REPLACE_STARTCOMMAND, '\"' + scriptFile + '\"'));
					} else {
						m_info.setCommand(m_info.getCommand().replace(REPLACE_STARTCOMMAND, scriptFile));
					}
				}
			} catch (Exception e) {
				//マネージャでコマンド終了待ちになっているため、終了通知を送る
				sendJobResultError("Failed to create script file. "+ e.getMessage());
				m_log.error("init() : " + e.getMessage(), e);
				// ファイル削除
				deleteScript();
				return;
			}
		}
		
		// ---------------------------
		// -- コマンド作成(OSへ渡す形式)
		// ---------------------------
		String[] cmd = null;
		String loginFlagKey = "job.command.login";
		boolean loginFlag = false;
		try {
			String loginFlagStr = AgentProperties.getProperty(loginFlagKey, "false");
			loginFlag = Boolean.parseBoolean(loginFlagStr);
		} catch(Exception e) {
			m_log.warn(e.getMessage());
		}
		String envConvertExportKey = "job.command.env.convert.export";
		boolean envConvertExportFlag = false;
		try {
			String convertExportFlagStr = AgentProperties.getProperty(envConvertExportKey, "false");
			envConvertExportFlag = Boolean.parseBoolean(convertExportFlagStr);
		} catch (Exception e) {
			m_log.warn(e.getMessage());
		}
		String limitFileoutputLengthKey = "job.limit.message.fileoutput.length";
		boolean limitFileoutputLengthFlag = true;
		try {
			String convertExportFlagStr = AgentProperties.getProperty(limitFileoutputLengthKey, "true");
			limitFileoutputLengthFlag = Boolean.parseBoolean(convertExportFlagStr);
		} catch (Exception e) {
			m_log.warn(e.getMessage());
		}

		/** 指定されたモードでコマンド生成の処理を切り替える */
		try {
			Map<String, String> envMap = getCmdEnvExportMap(m_info.getJobEnvVariableInfoList());
			cmd = CommandCreator.createCommand(m_info.getUser(), m_info.getCommand(), platform, m_info.getSpecifyUser().booleanValue(),
					loginFlag, envConvertExportFlag, envMap);
			// ---------------------------
			// -- コマンド実行
			// ---------------------------
			for (int i = 0; i < cmd.length; i++) {
				m_log.info("Command Execute [" + i + "] : " + cmd[i]);
			}
			if (cmd.length == 0) {
				m_log.warn("Command Execute : cmd.length=0");
			}

			cmdExec = new JobCommandExecutor(cmd, Charset.forName(m_inputEncoding), JobCommandExecutor._disableTimeout, m_limit_jobmsg);
			// クラウド管理のテンプレート機能で使用する環境変数を追加
			cmdExec.addEnvironment("HINEMOS_AGENT_HOME", Agent.getAgentHome());
			for (AgtJobEnvVariableInfoResponse env : m_info.getJobEnvVariableInfoList()) {
				cmdExec.addEnvironment(env.getEnvVariableId(), env.getValue());
			}

			cmdExec.setLimitOutput(limitFileoutputLengthFlag);
			OutputString outputSetting = new OutputString(m_info.getNormalJobOutputInfo(), m_info.getErrorJobOutputInfo());
			cmdExec.setOutputSetting(outputSetting);

			process = cmdExec.execute();

			// 実行履歴に追加
			RunHistoryUtil.addRunHistory(m_info, process);
		} catch (Exception e) {
			// プロセス起動に失敗

			// 実行履歴削除メッセージ送信
			m_log.error("CommandThread() : " + e.getMessage());
			
			// Windows環境において指定したコマンドが存在しない場合のエラーメッセージに文字化けが発生するため、英語表記に置き換える
			String errorMessage = e.getMessage();
			String targetStr = "CreateProcess error=2, "; //Windows環境でコマンドが存在しない場合に出現する文字列
			int ptr = errorMessage.lastIndexOf(targetStr);
			if (ptr != -1) {
				// 指定したコマンドが存在しない場合に、英語表記に置き換える
				errorMessage = errorMessage.substring(0, ptr + targetStr.length()) + "No such file or directory";
			}

			// メッセージ作成
			JobResultSendableObject snd = new JobResultSendableObject();
			snd.sessionId = m_info.getSessionId();
			snd.jobunitId = m_info.getJobunitId();
			snd.jobId = m_info.getJobId();
			snd.facilityId = m_info.getFacilityId();
			snd.body = new SetJobResultRequest();
			snd.body.setCommand(m_info.getCommand());
			snd.body.setCommandType(m_info.getCommandType());
			snd.body.setStopType(m_info.getStopType());
			snd.body.setStatus(RunStatusConstant.ERROR);
			snd.body.setTime(HinemosTime.getDateInstance().getTime());
			snd.body.setErrorMessage(errorMessage);
			snd.body.setMessage("");
			m_sendQueue.put(snd);

			//履歴削除
			RunHistoryUtil.delRunHistory(m_info);

			//スクリプト削除
			deleteScript();
			
			m_log.info("run end");
			return;
		}

	}

	/**
	 * ジョブ（コマンド・スクリプト）を実行するクラス<BR>
	 * 
	 * ReceiveTopicで受け取ったジョブの指示が実行の場合に このメソッドが実行されます。
	 * 
	 */
	@Override
	public void run() {
		m_log.debug("run start");

		if (RunHistoryUtil.findProcessRunHistory(m_info) == null) {
			// コンストラクタで失敗 or プロセス終了
			m_log.info("run() : process is null");
			return;
		}

		// コマンド実行
		CommandResult cmdResult = cmdExec.getResult();

		if (RunHistoryUtil.findProcessRunHistory(m_info) == null || !RunHistoryUtil.findProcessRunHistory(m_info).equals(process)) {
			// プロセス終了
			m_log.info("run() : process does already stopped.");
			return;
		}

		if (cmdResult.exitCode != null) {

			jobResult.body.setStatus(RunStatusConstant.END);
			jobResult.body.setEndValue(cmdResult.exitCode);

		} else {

			jobResult.body.setStatus(RunStatusConstant.ERROR);

		}

		// 終了を送信
		jobResult.body.setTime(HinemosTime.getDateInstance().getTime());
		jobResult.body.setErrorMessage(cmdResult.stderr);
		jobResult.body.setMessage(cmdResult.stdout);
		if (cmdExec.getOutputResult() != null) {
			SetJobOutputResultRequest output = new SetJobOutputResultRequest();
			output.setErorrTargetTypeList(cmdExec.getOutputResult().getResultStatus());
			output.setStdoutErrorMessage(cmdExec.getOutputResult().stdout.errorMessage);
			output.setStderrErrorMessage(cmdExec.getOutputResult().stderr.errorMessage);
			jobResult.body.setJobOutput(output);
		}
		
		m_sendQueue.put(jobResult);

		////実行履歴から削除
		RunHistoryUtil.delRunHistory(m_info);

		//スクリプト削除
		if(m_scriptDelete) {
			deleteScript();
		}
		
		m_log.debug("run end");
	}
	
	private void createScriptFile(String scriptContent, String encoding) throws IOException {
		PrintWriter pw = null;
		try {
			File f = new File(scriptFile);
			pw = new PrintWriter(f, encoding);
			pw.print(scriptContent.replaceAll("\n", System.getProperty("line.separator")));
			f.setExecutable(true, false);
			m_log.debug("createScriptFile() : " + scriptFile);
		} finally {
			if(pw != null) {
				pw.close();
			}
		}
	}
	
	private void deleteScript() {
		if(scriptFile == null) {
			return;
		}
		
		File f = new File(scriptFile);
		if(f.exists()) {
			boolean ret = f.delete();
			if (!ret) {
				m_log.warn("deleteScript error");
			}
		}
		m_log.debug("deleteScript() : " + scriptFile);
	}
	
	/**
	 * 指定された環境変数リストをLinkedHashMapへ変換します。
	 */
	private Map<String, String> getCmdEnvExportMap(List<AgtJobEnvVariableInfoResponse> list) {
		Map<String, String> ret = new LinkedHashMap<>();

		if (list == null || list.isEmpty()) {
			ret = null;
		} else {
			for (AgtJobEnvVariableInfoResponse env : list) {
				ret.put(env.getEnvVariableId(), env.getValue());
			}
		}

		return ret;
	}

	private void sendJobResultError(String errorMsg) {
		// メッセージ作成
		JobResultSendableObject runErrorInfo = new JobResultSendableObject();
		runErrorInfo.sessionId = m_info.getSessionId();
		runErrorInfo.jobunitId = m_info.getJobunitId();
		runErrorInfo.jobId = m_info.getJobId();
		runErrorInfo.facilityId = m_info.getFacilityId();
		runErrorInfo.body = new SetJobResultRequest();
		runErrorInfo.body.setCommand(m_info.getCommand());
		runErrorInfo.body.setCommandType(m_info.getCommandType());
		runErrorInfo.body.setStopType(m_info.getStopType());
		runErrorInfo.body.setStatus(RunStatusConstant.ERROR);
		runErrorInfo.body.setTime(HinemosTime.getDateInstance().getTime());
		runErrorInfo.body.setErrorMessage(errorMsg);
		runErrorInfo.body.setMessage("");
		m_sendQueue.put(runErrorInfo);
	}
}
