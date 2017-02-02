/*

Copyright (C) 2011 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.agent.job;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.SendQueue;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.ws.jobmanagement.RunInstructionInfo;
import com.clustercontrol.ws.jobmanagement.RunResultInfo;

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
			RunInstructionInfo info,
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
		RunResultInfo info = new RunResultInfo();
		info.setSessionId(m_info.getSessionId());
		info.setJobunitId(m_info.getJobunitId());
		info.setJobId(m_info.getJobId());
		info.setFacilityId(m_info.getFacilityId());
		info.setCommand(m_info.getCommand());
		info.setCommandType(m_info.getCommandType());
		info.setStopType(m_info.getStopType());
		info.setStatus(RunStatusConstant.START);
		info.setTime(startDate.getTime());

		m_log.info("run SessionID=" + m_info.getSessionId() + ", JobID=" + m_info.getJobId());

		//送信
		m_sendQueue.put(info);

		//ファイルリスト取得
		List<String> fileList = getFileList(m_info.getFilePath());
		if (fileList.size() > 0){
			info.setStatus(RunStatusConstant.END);
			info.getFileList().addAll(fileList);
			info.setTime(HinemosTime.getDateInstance().getTime());
			info.setErrorMessage("");
			info.setMessage("");
			info.setEndValue(0);
		} else {
			m_log.info("filelist.size()=0");
			info.setStatus(RunStatusConstant.END);
			info.getFileList().addAll(fileList);
			info.setTime(HinemosTime.getDateInstance().getTime());
			info.setErrorMessage("");
			info.setMessage("file not found");
			info.setEndValue(1);
		}

		//送信
		m_sendQueue.put(info);

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
			if(fileName.length() == 0)
				fileName = ".*";

			//Fileを指定パスで作成
			File fi = new File(dir);

			File[] files = fi.listFiles();
			if (files != null) {
				for(int i = 0; i < files.length; i++){
					if(files[i].isFile() && !files[i].isHidden()){
						if(files[i].getName().matches(fileName))
							fileList.add(files[i].getCanonicalPath());
					}
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
