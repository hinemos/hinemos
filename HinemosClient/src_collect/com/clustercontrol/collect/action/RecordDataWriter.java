/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.collect.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceContext;
import org.openapitools.client.model.CollectKeyInfoRequest;
import org.openapitools.client.model.CollectKeyInfoResponseP1;
import org.openapitools.client.model.CreatePerfFileRequest;
import org.openapitools.client.model.CreatePerfFileRequest.SummaryTypeEnum;
import org.openapitools.client.model.CreatePerfFileResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.collect.bean.SummaryTypeConstant;
import com.clustercontrol.collect.preference.PerformancePreferencePage;
import com.clustercontrol.collect.util.CollectGraphUtil.CollectFacilityDataInfo;
import com.clustercontrol.collect.util.CollectRestClientWrapper;
import com.clustercontrol.fault.HinemosException;
import com.clustercontrol.fault.PerfFileNotFound;
import com.clustercontrol.util.Messages;


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
	private TreeMap<String, CollectFacilityDataInfo> m_managerFacilityDataInfoMap = null;
	private Integer m_summaryType = null;
	private List<CollectKeyInfoResponseP1> m_targetCollectKeyInfoList = null;	
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
	public RecordDataWriter(TreeMap<String, CollectFacilityDataInfo> managerFacilityDataInfoMap,
			Integer summaryType,
			List<CollectKeyInfoResponseP1> targetCollectKeyInfoList,
			TreeMap<String,  List<String>> targetManagerFacilityMap,
			boolean headerFlag, String filePath, String defaultDateStr) {
		super();
		
		this.m_managerFacilityDataInfoMap = managerFacilityDataInfoMap;
		this.m_summaryType = summaryType;
		this.m_targetCollectKeyInfoList = targetCollectKeyInfoList;
		this.m_targetManagerFacilityMap =targetManagerFacilityMap;
		
		this.headerFlag = headerFlag;
		File f = new File(filePath);
		this.fileName = f.getName();
		this.fileDir = f.getParent();
		this.defaultDateStr = defaultDateStr;

		m_log.debug("RecordDataWriter() " +
				"managerFacilityIdNameMap = " + managerFacilityDataInfoMap.toString() +
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
				
				for (Map.Entry<String, CollectFacilityDataInfo> managerFacilityDataInfo : m_managerFacilityDataInfoMap.entrySet()) {
					if(managerFacilityDataInfo.getKey().startsWith(managerName + SQUARE_SEPARATOR)){
						String split_plot[] = managerFacilityDataInfo.getKey().split(SQUARE_SEPARATOR);
						String facilityName = split_plot[1];
						facilityIdNameMap.put(facilityName, managerFacilityDataInfo.getValue().getName());
					}
				}
				// ラッパー取得
				CollectRestClientWrapper wrapper = CollectRestClientWrapper.getWrapper(managerName);
				
				// Request生成
				CreatePerfFileRequest createPerfFileRequest = new CreatePerfFileRequest();
	
				List<CollectKeyInfoRequest> collectKeyInfoReqList = new ArrayList<>();
				
				for(CollectKeyInfoResponseP1 collectKeyInfoList:m_targetCollectKeyInfoList){
					// findbugs対応 collectKeyInfoRequestの変数スコープを変更
					CollectKeyInfoRequest collectKeyInfoRequest = new CollectKeyInfoRequest();
					collectKeyInfoRequest.setDisplayName(collectKeyInfoList.getDisplayName());
					collectKeyInfoRequest.facilityId(collectKeyInfoList.getFacilityId());
					collectKeyInfoRequest.setMonitorId(collectKeyInfoList.getMonitorId());
					collectKeyInfoRequest.setItemName(collectKeyInfoList.getItemName());
					collectKeyInfoReqList.add(collectKeyInfoRequest);
				}
				createPerfFileRequest.setCollectKeyInfoList(collectKeyInfoReqList);
				
				createPerfFileRequest.setFacilityNameMap(facilityIdNameMap);
				createPerfFileRequest.setFacilityList(entry.getValue());
				createPerfFileRequest.setSummaryType(SummaryTypeEnum.fromValue(SummaryTypeConstant.typeToString(m_summaryType)));
				createPerfFileRequest.setLocaleStr(Locale.getDefault().toString());
				createPerfFileRequest.setHeader(headerFlag);
				createPerfFileRequest.setDefaultDateStr(defaultDateStr);
	
				CreatePerfFileResponse createPerfFileResponse = wrapper.createPerfFile(createPerfFileRequest);
				downloadFileList = createPerfFileResponse.getFileList();
			}
		} catch (HinemosException e) {
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

		File file = new File( this.fileDir, name );
		FileOutputStream fileOutputStream = null;
		DataHandler handler = null;
		try{
			fileOutputStream = new FileOutputStream(file);
			
			// 指定回数だけファイル存在確認をする
			m_log.info("download perf file = " + name + ", waitCount = " + waitCount);
			
			for (Map.Entry<String, List<String>> entry : m_targetManagerFacilityMap.entrySet()) {
				String managerName = entry.getKey();
				for (int i = 0; i < waitCount; i++) {
					if(!this.canceled){
						Thread.sleep(waitSleep);
						m_log.debug("download perf file = " + name + ", create check. count = " + i);
						// クライアントのヒープが小さい場合は下記の行で落ちる。(out of memory)
						CollectRestClientWrapper wrapper = CollectRestClientWrapper.getWrapper(managerName);
						try {
							file = wrapper.downloadPerfFile(prefName);
						} catch (PerfFileNotFound e) {
							// ファイルの作成が完了していない場合はリトライ
							file = null;
						}
						if(file != null){
							m_log.info("download perf file = " + name + ", created !");
							break;
						}
					}
				}
				if(file == null){
					m_log.info("file is null");
					setCancelMessage(Messages.getString("performance.get.collecteddata.error.message") +
							": cannot create collected data for client-timeout");
					setCanceled(true);
					return;
				}
			}

			boolean ret = file.createNewFile();
			if (!ret) {
				m_log.warn("file is already exist.");
			}
			FileDataSource source = new FileDataSource(file);
			handler = new DataHandler(source);

			handler.writeTo(fileOutputStream);

			m_log.info("download perf file  = " + name + ", succeed !");
			m_log.debug("download() succeed!");
		} catch (HinemosException e) {
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
				// findbugs対応 サーバ側にデータファイルの削除を要請するメソッドが廃止されたので 併せて不要メソッド呼び出し廃止
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
					// findbugs対応 サーバ側にデータファイルの削除を要請するメソッドが廃止されたので 併せて不要メソッド呼び出し廃止
					this.progress = 100;
					return;
				}

				this.progress += (50 / downloadFileList.size());
			}
		}

		////
		// delete(10%)
		////
		// findbugs対応 サーバ側にデータファイルの削除を要請するメソッドが廃止されたので 併せて不要メソッド呼び出し廃止

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
