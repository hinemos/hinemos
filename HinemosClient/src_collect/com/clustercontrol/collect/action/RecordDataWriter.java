/*

 Copyright (C) 2006 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 */

package com.clustercontrol.collect.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.activation.DataHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceContext;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.collect.preference.PerformancePreferencePage;
import com.clustercontrol.collect.util.CollectEndpointWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.collect.CollectKeyInfoPK;
import com.clustercontrol.ws.collect.HinemosUnknown_Exception;
import com.clustercontrol.ws.collect.InvalidRole_Exception;
import com.clustercontrol.ws.collect.InvalidUserPass_Exception;

/**
 * 実績データをエクスポートするクラス
 *
 * @version 4.0.0
 * @since 1.0.0
 *
 */
public class RecordDataWriter implements Runnable {
	private static Log m_log = LogFactory.getLog(RecordDataWriter.class);
	
	// input
	private TreeMap<String, String> m_managerFacilityIdNameMap = null;
	private Integer m_summaryType = null;
	private List<CollectKeyInfoPK> m_targetCollectKeyInfoList = null;	
	private Map<String, List<String>> m_targetManagerFacilityMap = null;
	
	private boolean headerFlag;
	private String fileName = null;
	private String fileDir = null;
	private String defaultDateStr = null;

	private int progress;

	private int dlMaxWait = 1;
	private int waitSleep = 1000;
	private int waitCount = 60;

	private boolean canceled;
	private String cancelMessage = null;

	private ServiceContext context = null;
	
	/**
	 * セパレータ
	 */
	private static final String SQUARE_SEPARATOR = "\u2029";

	/**
	 * デフォルトコンストラクタ
	 *
	 * @param targetFacilityId
	 * @param headerFlag
	 * @param archiveFlag
	 * @param folderName
	 */
	public RecordDataWriter(TreeMap<String, String> managerFacilityIdNameMap,
			Integer summaryType,
			List<CollectKeyInfoPK> targetCollectKeyInfoList,
			TreeMap<String,  List<String>> targetManagerFacilityMap,
			boolean headerFlag, String filePath, String defaultDateStr) {
		super();
		
		this.m_managerFacilityIdNameMap = managerFacilityIdNameMap;
		this.m_summaryType = summaryType;
		this.m_targetCollectKeyInfoList = targetCollectKeyInfoList;
		this.m_targetManagerFacilityMap =targetManagerFacilityMap;
		
		this.headerFlag = headerFlag;
		File f = new File(filePath);
		this.fileName = f.getName();
		this.fileDir = f.getParent();
		this.defaultDateStr = defaultDateStr;

		m_log.debug("RecordDataWriter() " +
				"managerFacilityIdNameMap = " + managerFacilityIdNameMap.toString() +
				", summaryType = " + summaryType +
				", targetManagerFacilityMap = " + targetManagerFacilityMap.toString() +
				", headerFlag = " + headerFlag +
				", fileOath = " + filePath +
				", fileName = " + fileName +
				", fileDir = " + fileDir + 
				", defaultDateStr = " + defaultDateStr);

		// 性能データダウンロード待ち時間(分)
		dlMaxWait = ClusterControlPlugin.getDefault().getPreferenceStore().getInt(
				PerformancePreferencePage.P_DL_MAX_WAIT);
		waitCount = dlMaxWait * 60 * 1000 / waitSleep ;

		m_log.debug("RecordDataWriter() " + "dlMaxWait = " + dlMaxWait +  ", waitCount = " + waitCount);
	}

	public void setContext(ServiceContext context) {
		this.context = context;
	}
	
	/**
	 * コンストラクタで指定された条件で、マネージャサーバに性能実績データをファイルとして作成する
	 *
	 * @return
	 */
	public List<String> export() {
		List<String> downloadFileList = null;
		try{
			for (Map.Entry<String, List<String>> entry : m_targetManagerFacilityMap.entrySet()) {
				String managerName = entry.getKey();
				Map<String, String> facilityIdNameMap = new HashMap<String, String>();
				
				for (Map.Entry<String, String> managerFacilityIdName : m_managerFacilityIdNameMap.entrySet()) {
					if(managerFacilityIdName.getKey().startsWith(managerName + SQUARE_SEPARATOR)){
						String split_plot[] = managerFacilityIdName.getKey().split(SQUARE_SEPARATOR);
						String facilityName = split_plot[1];
						facilityIdNameMap.put(facilityName, managerFacilityIdName.getValue());
					}
				}
			CollectEndpointWrapper wrapper = CollectEndpointWrapper.getWrapper(managerName);
			downloadFileList = wrapper.createPerfFile(facilityIdNameMap, m_targetCollectKeyInfoList, 
					entry.getValue(), m_summaryType, headerFlag, defaultDateStr);
			}
		} catch (HinemosUnknown_Exception | InvalidUserPass_Exception | InvalidRole_Exception e) {
			setCancelMessage(Messages.getString("performance.get.collecteddata.error.message") + ":" + e.getMessage());
			setCanceled(true);
			m_log.warn("export()", e);
		} catch (Exception e) {
			setCancelMessage(Messages.getString("performance.get.collecteddata.error.message") + ":" + e.getMessage() +
					"(" + e.getClass().getName() + ")");
			setCanceled(true);
			m_log.warn("export()", e);
		}
		// for debug
		if(m_log.isDebugEnabled()){
			if(downloadFileList == null || downloadFileList.size() == 0){
				m_log.debug("export() downloadFileList is null");
			}
			else{
				for(String fileName : downloadFileList){
					m_log.debug("export() downloadFileName = " + fileName);
				}
			}
		}
		return downloadFileList;
	}

	/**
	 * マネージャサーバで作成した性能実績データのファイルをダウンロードして指定のフォルダに配置します
	 *
	 * @param fileName
	 */
	public void download(String prefName, String name ){
		if( null == name ){
			name = prefName;
		}
		m_log.debug("download() downloadFileName = " + prefName + ", name = " + name);
		m_log.info("download perf file  = " + name);

		FileOutputStream fileOutputStream = null;
		DataHandler handler = null;
		try{
			// 指定回数だけファイル存在確認をする
			m_log.info("download perf file = " + name + ", waitCount = " + waitCount);
			
			for (Map.Entry<String, List<String>> entry : m_targetManagerFacilityMap.entrySet()) {
				String managerName = entry.getKey();
				for (int i = 0; i < waitCount; i++) {
					if(!this.canceled){
						Thread.sleep(waitSleep);
						m_log.debug("download perf file = " + name + ", create check. count = " + i);
						// クライアントのヒープが小さい場合は下記の行で落ちる。(out of memory)
						CollectEndpointWrapper wrapper = CollectEndpointWrapper.getWrapper(managerName);
						handler = wrapper.downloadPerfFile(prefName);
						if(handler != null){
							m_log.info("download perf file = " + name + ", created !");
							break;
						}
					}
				}
				if(handler == null){
					m_log.info("download handler is null");
					setCancelMessage(Messages.getString("performance.get.collecteddata.error.message") +
							": cannot create collected data for client-timeout");
					setCanceled(true);
					return;
				}
			}
			if(handler == null){
				m_log.info("download handler is null");
				setCancelMessage(Messages.getString("performance.get.collecteddata.error.message") +
						": cannot create collected data for client-timeout");
				setCanceled(true);
				return;
			}

			File file = new File( this.fileDir, name );
			boolean ret = file.createNewFile();
			if (!ret) {
				m_log.warn("file is already exist.");
			}
			fileOutputStream = new FileOutputStream(file);
			handler.writeTo(fileOutputStream);

			m_log.info("download perf file  = " + name + ", succeed !");
			m_log.debug("download() succeed!");
		} catch (HinemosUnknown_Exception e) {
			setCancelMessage(Messages.getString("performance.get.collecteddata.error.message") + ":" + e.getMessage());
			setCanceled(true);
			m_log.warn("download()", e);
		} catch (InvalidUserPass_Exception e) {
			setCancelMessage(Messages.getString("performance.get.collecteddata.error.message") + ":" + e.getMessage());
			setCanceled(true);
			m_log.warn("download()", e);
		} catch (InvalidRole_Exception e) {
			setCancelMessage(Messages.getString("performance.get.collecteddata.error.message") + ":" + e.getMessage());
			setCanceled(true);
			m_log.warn("download()", e);
		} catch (InterruptedException e) {
			setCancelMessage(Messages.getString("performance.get.collecteddata.error.message") + ":" + e.getMessage());
			setCanceled(true);
			m_log.warn("download()", e);
		} catch (IOException e) {
			setCancelMessage(Messages.getString("performance.get.collecteddata.error.write") + ":" + e.getMessage());
			setCanceled(true);
			m_log.warn("download()", e);
		} finally {
			try{
				if(fileOutputStream != null){
					fileOutputStream.close();
				}
			}catch (IOException e) {
				setCancelMessage(Messages.getString("performance.get.collecteddata.error.write") + ":" + e.getMessage());
				setCanceled(true);
				m_log.warn("download()", e);
			}
		}
	}

	/**
	 * マネージャサーバで作成した性能実績データのファイルを削除する
	 *
	 * @param fileNameList
	 */
	public void delete(List<String> fileNameList){
		m_log.debug("delete()");

		try{
			if(fileNameList != null && fileNameList.size() > 0){
				m_log.debug("delete() run delete!");
				for (Map.Entry<String, List<String>> entry : m_targetManagerFacilityMap.entrySet()) {
					String managerName = entry.getKey();
					CollectEndpointWrapper wrapper = CollectEndpointWrapper.getWrapper(managerName);
					wrapper.deletePerfFile(new ArrayList<String>(fileNameList));
				}
			}
			m_log.debug("delete() succeed!");
		} catch (HinemosUnknown_Exception e) {
			setCancelMessage(Messages.getString("performance.get.collecteddata.error.delete") + ":" + e.getMessage());
			setCanceled(true);
			m_log.warn("delete()", e);
		} catch (InvalidUserPass_Exception e) {
			setCancelMessage(Messages.getString("performance.get.collecteddata.error.delete") + ":" + e.getMessage());
			setCanceled(true);
			m_log.warn("delete()", e);
		} catch (InvalidRole_Exception e) {
			setCancelMessage(Messages.getString("performance.get.collecteddata.error.delete") + ":" + e.getMessage());
			setCanceled(true);
			m_log.warn("delete()", e);
		}
	}


	/**
	 * ファイルへのエクスポートを実行します。
	 */
	@Override
	public void run() {

		// 開始
		this.progress = 0;
		
		ContextProvider.releaseContextHolder();
		ContextProvider.setContext(context);
		
		////
		// export(マネージャサイドの動作)(40%)
		////
		List<String> downloadFileList = null;
		if(!this.canceled){
			downloadFileList = export();
		}else{
			this.progress = 100;
			return;
		}

		if(downloadFileList == null || downloadFileList.size() == 0){
			if(!isCanceled()){
				setCancelMessage(Messages.getString("performance.insufficient.data"));
				setCanceled(true);
			}
			return;
		}

		////
		// download(50%)
		////
		m_log.debug("downloadFileList.size() : " + downloadFileList.size());
		if( 1 == downloadFileList.size() ){
			// Archived as 1 zip pack
			if(!this.canceled){
				download(downloadFileList.get(0), this.fileName);
			}else{
				delete(downloadFileList);
				this.progress = 100;
				return;
			}
			this.progress += 50;
		}else{
			// serveral files
			for(String downloadFile : downloadFileList){
				if(!this.canceled){
					download(downloadFile, null);
				}else{
					delete(downloadFileList);
					this.progress = 100;
					return;
				}

				this.progress += (50 / downloadFileList.size());
			}
		}

		////
		// delete(10%)
		////
		delete(downloadFileList);

		// 終了
		this.progress = 100;
	}

	/**
	 * 現在までに性能値データの何％のエクスポートが完了したのかを取得します。
	 *
	 * @return 進捗（%表記 0～100）
	 */
	public int getProgress() {
		return progress;
	}

	/**
	 * エクスポート中に処理を中止します。
	 *
	 * @param 処理を中止したい場合にはtrueを設定します。
	 */
	public void setCanceled(boolean b) {
		this.canceled = b;
	}


	/**
	 * キャンセルの有無を取得
	 * @return
	 */
	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * キャンセル時のメッセージの取得
	 * @return
	 */
	public String getCancelMessage() {
		return cancelMessage;
	}

	/**
	 * キャンセル時のメッセージの設定
	 * @param cancelMessage
	 */
	public void setCancelMessage(String cancelMessage) {
		this.cancelMessage = cancelMessage;
	}
}
