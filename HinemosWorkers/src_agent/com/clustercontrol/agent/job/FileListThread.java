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
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtRunInstructionInfoResponse;
import org.openapitools.client.model.SetJobResultRequest;

import com.clustercontrol.agent.SendQueue;
import com.clustercontrol.agent.SendQueue.JobResultSendableObject;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.util.HinemosTime;

/**
 * ファイル転送ジョブ用ファイルリスト取得スレッドクラス<BR>
 * 
 * Hinemosのファイル転送ジョブでは、ファイル指定にワールドカード指定を
 * 許します。<BR>
 * そこで、このスレッドにより、そのワイルドカードの展開を
 * システムに問い合わせる形で行います。<BR>
 * <BR>
 * また、ファイルが存在しているかの存在確認も行います。
 *
 */
public class FileListThread extends AgentThread {

	// 転送元ファイルを取得するビジター
	private static class TargetFileVisitor extends SimpleFileVisitor<Path> {
		private List<String> matchFiles = new ArrayList<>();
		private String filterFileName;

		public TargetFileVisitor(String filterFileName) {
			this.filterFileName = filterFileName;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			Path fileNamePath = file.getFileName();
			if (fileNamePath == null) {
				// 想定外の状況 スキップして次へいく
				m_log.warn("visitFile() : Path.getFileName() is null");
				return FileVisitResult.CONTINUE;
			}
			String fileName = fileNamePath.toString();
			m_log.debug("visitFile() : file = " + fileName);

			File f = file.toFile();
			// ファイル、隠しファイル以外、ファイル名でフィルタリングする
			if(f.isFile() &&
				!f.isHidden() &&
				fileName.matches(filterFileName)) {
				// 対象に追加する
				matchFiles.add(f.getCanonicalPath());
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
			// 想定外の状況 スキップして次へいく
			m_log.warn(String.format("visitFileFailed() : occurred error file=%s, %s",
					file.toAbsolutePath(),
					e.getMessage()));
			return FileVisitResult.CONTINUE;
		}

		public List<String> getMatchFiles() {
			return matchFiles;
		}
	}

	//ロガー
	private static Log m_log = LogFactory.getLog(FileListThread.class);

	/**
	 * コンストラクタ
	 * 
	 * @param props
	 */
	public FileListThread(
			AgtRunInstructionInfoResponse info,
			SendQueue sendQueue) {
		super(info, sendQueue);
	}

	/**
	 * ファイルリストを取得します。<BR>
	 * 
	 * ReceiveTopicで受け取ったジョブの指示がファイルリストの場合に
	 * このメソッドが実行されます。
	 */
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		m_log.debug("run start");

		Date startDate = HinemosTime.getDateInstance();

		//実行履歴に追加
		RunHistoryUtil.addRunHistory(m_info, RunHistoryUtil.dummyProcess());

		//---------------------------
		//-- 開始メッセージ送信
		//---------------------------

		//メッセージ作成
		JobResultSendableObject sendme = new JobResultSendableObject();
		sendme.sessionId = m_info.getSessionId();
		sendme.jobunitId = m_info.getJobunitId();
		sendme.jobId = m_info.getJobId();
		sendme.facilityId = m_info.getFacilityId();
		sendme.body = new SetJobResultRequest();
		sendme.body.setCommand(m_info.getCommand());
		sendme.body.setCommandType(m_info.getCommandType());
		sendme.body.setStopType(m_info.getStopType());
		sendme.body.setStatus(RunStatusConstant.START);
		sendme.body.setTime(startDate.getTime());

		m_log.info("run SessionID=" + m_info.getSessionId() + ", JobID=" + m_info.getJobId());

		//送信
		m_sendQueue.put(sendme);

		//ファイルリスト取得
		List<String> fileList = getFileList(m_info.getFilePath());
		if (fileList.size() > 0){
			sendme.body.setStatus(RunStatusConstant.END);
			sendme.body.getFileList().addAll(fileList);
			sendme.body.setTime(HinemosTime.getDateInstance().getTime());
			sendme.body.setErrorMessage("");
			sendme.body.setMessage("");
			sendme.body.setEndValue(0);
		} else {
			m_log.info("filelist.size()=0");
			sendme.body.setStatus(RunStatusConstant.END);
			sendme.body.getFileList().addAll(fileList);
			sendme.body.setTime(HinemosTime.getDateInstance().getTime());
			sendme.body.setErrorMessage("");
			sendme.body.setMessage("file not found");
			sendme.body.setEndValue(1);
		}

		//送信
		m_sendQueue.put(sendme);

		//実行履歴から削除
		RunHistoryUtil.delRunHistory(m_info);

		m_log.debug("run end");
	}

	/**
	 * run()から呼び出されるファイルリスト取得部分のメソッド<BR>
	 * 
	 * @param path
	 * @return
	 */
	private synchronized List<String> getFileList(String path) {
		m_log.debug("get file list start");

		List<String> fileList = new ArrayList<String>();

		try{
			String dir = path.substring(0, path.lastIndexOf("/") + 1);
			String fileName = path.substring(path.lastIndexOf("/") + 1);
			fileName = fileName.replaceAll("[.]", "[.]");
			fileName = fileName.replaceAll("[*]", ".*");
			if(fileName.length() == 0) {
				fileName = ".*";
			}

			// ビジターでシーク対象ファイル一覧を取得する
			TargetFileVisitor fv = new TargetFileVisitor(fileName);
			// 辿る階層は設定ファイルの指定ディレクトリ直下のみ
			Files.walkFileTree(Paths.get(dir), EnumSet.of(FileVisitOption.FOLLOW_LINKS), 1, fv);
			fileList = fv.getMatchFiles();
		}
		catch(Exception e){
			m_log.warn("getFileList error. " + e.getMessage(), e);
		}

		m_log.debug("get file list end");
		return fileList;
	}
}
