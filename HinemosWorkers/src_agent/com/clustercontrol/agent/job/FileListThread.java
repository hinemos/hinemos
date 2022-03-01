/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.job;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Date;
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

			//Fileを指定パスで作成
			File fi = new File(dir);

			final String filterFileName = fileName;
			// ファイルのフィルタ条件
			FileFilter fileFilter = new FileFilter() {
				@Override
				public boolean accept(File f) {
					// ファイル、隠しファイル以外、ファイル名でフィルタリングする
					return f.isFile() && !f.isHidden() && f.getName().matches(filterFileName);
				}
			};

			File[] files = fi.listFiles(fileFilter);
			if (files != null) {
				for(int i = 0; i < files.length; i++){
					fileList.add(files[i].getCanonicalPath());
				}
			} else {
				m_log.warn(dir +" is not directory or does not have a reference permission");
			}
		}
		catch(Exception e){
			m_log.warn("getFileList error. " + e.getMessage(), e);
		}

		m_log.debug("get file list end");
		return fileList;
	}
}
