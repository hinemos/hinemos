/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.openapitools.client.model.AddJobmapIconImageRequest;
import org.openapitools.client.model.JobmapIconIdDefaultListResponse;
import org.openapitools.client.model.JobmapIconImageInfoResponse;
import org.openapitools.client.model.ModifyJobmapIconImageRequest;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.IconFileDuplicate;
import com.clustercontrol.fault.IconFileNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.UrlNotFound;
import com.clustercontrol.jobmanagement.util.JobmapIconImageUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.RestConnectManager;

/**
 * ジョブマップ用のアイコンの画像キャッシュ管理クラス。
 * 
 * インスタンス管理については RCP/RAP間の差分実装が必要なため
 * 本クラスを継承したJobmapImageCacheUtil側に委ねています。
 **/
public class JobmapIconImageCache {
	
	private static final String TEMP_FILE_PREFIX = "JobMap_";
	
	// ログ
	private static Log m_log = LogFactory.getLog( JobmapIconImageCache.class );

	private Map<String, Map<String, JobmapIconImageCacheEntry>> m_jobmapIconImageCache 
		= new ConcurrentHashMap<>();
	//グラフィックイメージキャッシュ（画像バイトコード単位）
	private Map<String, Image> m_graphicImageCacheHexIndex 
	= new ConcurrentHashMap<>();
	//グラフィックイメージキャッシュ（アイコンID単位）
	private Map<JobmapIconImageInfoResponse, Image> m_graphicImageCacheIconIndex 
		= new ConcurrentHashMap<>();
	private Map<String, String> m_defaultJobnetIconIdCache = new ConcurrentHashMap<>();
	private Map<String, String> m_defaultJobIconIdCache = new ConcurrentHashMap<>();
	private Map<String, String> m_defaultApprovalIconIdCache = new ConcurrentHashMap<>();
	private Map<String, String> m_defaultMonitorIconIdCache = new ConcurrentHashMap<>();
	private Map<String, String> m_defaultFileIconIdCache = new ConcurrentHashMap<>();
	private Map<String, String> m_defaultFileCheckIconIdCache = new ConcurrentHashMap<>();
	private Map<String, String> m_defaultResourceIconIdCache = new ConcurrentHashMap<>();
	private Map<String, String> m_defaultJobLinkSendIconIdCache = new ConcurrentHashMap<>();
	private Map<String, String> m_defaultJobLinkRcvIconIdCache = new ConcurrentHashMap<>();
	private Map<String, String> m_defaultRpaIconIdCache = new ConcurrentHashMap<>();
	
	public JobmapIconImageCache() {
		refresh();
	}
	/**
	 * ジョブマップ用デフォルトアイコンID（ジョブネット用）取得
	 * 
	 * @param managerName マネージャ名
	 * @return ジョブマップ用デフォルトアイコンID（ジョブネット用）
	 */
	public String getJobmapIconIdDefaultJobnet(String managerName) {
		return m_defaultJobnetIconIdCache.get(managerName);
	}

	/**
	 * ジョブマップ用デフォルトアイコンID（ジョブ用）取得
	 * 
	 * @param managerName マネージャ名
	 * @return ジョブマップ用デフォルトアイコンID（ジョブ用）
	 */
	public String getJobmapIconIdDefaultJob(String managerName) {
		return m_defaultJobIconIdCache.get(managerName);
	}

	/**
	 * ジョブマップ用デフォルトアイコンID（承認ジョブ用）取得
	 * 
	 * @param managerName マネージャ名
	 * @return ジョブマップ用デフォルトアイコンID（承認ジョブ用）
	 */
	public String getJobmapIconIdDefaultApproval(String managerName) {
		return m_defaultApprovalIconIdCache.get(managerName);
	}

	/**
	 * ジョブマップ用デフォルトアイコンID（監視ジョブ用）取得
	 * 
	 * @param managerName マネージャ名
	 * @return ジョブマップ用デフォルトアイコンID（監視ジョブ用）
	 */
	public String getJobmapIconIdDefaultMonitor(String managerName) {
		return m_defaultMonitorIconIdCache.get(managerName);
	}

	/**
	 * ジョブマップ用デフォルトアイコンID（ファイル転送ジョブ用）取得
	 * 
	 * @param managerName マネージャ名
	 * @return ジョブマップ用デフォルトアイコンID（ファイル転送ジョブ用）
	 */
	public String getJobmapIconIdDefaultFile(String managerName) {
		return m_defaultFileIconIdCache.get(managerName);
	}

	/**
	 * ジョブマップ用デフォルトアイコンID（ファイルチェックジョブ用）取得
	 * 
	 * @param managerName マネージャ名
	 * @return ジョブマップ用デフォルトアイコンID（ファイルチェックジョブ用）
	 */
	public String getJobmapIconIdDefaultFileCheck(String managerName) {
		return m_defaultFileCheckIconIdCache.get(managerName);
	}

	/**
	 * ジョブマップ用デフォルトアイコンID（リソース制御ジョブ用）取得
	 * 
	 * @param managerName マネージャ名
	 * @return ジョブマップ用デフォルトアイコンID（リソース制御ジョブ用）
	 */
	public String getJobmapIconIdDefaultResource(String managerName) {
		return m_defaultResourceIconIdCache.get(managerName);
	}

	/**
	 * ジョブマップ用デフォルトアイコンID（ジョブ連携送信ジョブ用）取得
	 * 
	 * @param managerName マネージャ名
	 * @return ジョブマップ用デフォルトアイコンID（ジョブ連携送信ジョブ用）
	 */
	public String getJobmapIconIdDefaultJobLinkSend(String managerName) {
		return m_defaultJobLinkSendIconIdCache.get(managerName);
	}

	/**
	 * ジョブマップ用デフォルトアイコンID（ジョブ連携待機ジョブ用）取得
	 * 
	 * @param managerName マネージャ名
	 * @return ジョブマップ用デフォルトアイコンID（ジョブ連携待機ジョブ用）
	 */
	public String getJobmapIconIdDefaultJobLinkRcv(String managerName) {
		return m_defaultJobLinkRcvIconIdCache.get(managerName);
	}

	/**
	 * ジョブマップ用デフォルトアイコンID（RPAシナリオジョブ用）取得
	 * 
	 * @param managerName マネージャ名
	 * @return ジョブマップ用デフォルトアイコンID（リソース制御ジョブ用）
	 */
	public String getJobmapIconIdDefaultRpa(String managerName) {
		return m_defaultRpaIconIdCache.get(managerName);
	}

	/**
	 * ジョブマップ用デフォルトアイコンイメージ（ジョブネット用）取得
	 * 
	 * @param managerName マネージャ名
	 * @return ジョブマップ用デフォルトアイコンイメージ（ジョブネット用）
	 */
	public JobmapIconImageCacheEntry getJobmapIconImageDefaultJobnet(String managerName) {
		if (m_jobmapIconImageCache.containsKey(managerName)
				&& m_jobmapIconImageCache.get(managerName).containsKey(m_defaultJobnetIconIdCache.get(managerName))) {
			return m_jobmapIconImageCache.get(managerName).get(m_defaultJobnetIconIdCache.get(managerName));
		} else {
			return null;
		}
	}

	/**
	 * ジョブマップ用デフォルトアイコンイメージ（ジョブ用）取得
	 * 
	 * @param managerName マネージャ名
	 * @return ジョブマップ用デフォルトアイコンイメージ（ジョブ用）
	 */
	public JobmapIconImageCacheEntry getJobmapIconImageDefaultJob(String managerName) {
		if (m_jobmapIconImageCache.containsKey(managerName)
				&& m_jobmapIconImageCache.get(managerName).containsKey(m_defaultJobIconIdCache.get(managerName))) {
			return m_jobmapIconImageCache.get(managerName).get(m_defaultJobIconIdCache.get(managerName));
		} else {
			return null;
		}
	}

	/**
	 * ジョブマップ用デフォルトアイコンイメージ（承認ジョブ用）取得
	 * 
	 * @param managerName マネージャ名
	 * @return ジョブマップ用デフォルトアイコンイメージ（承認ジョブ用）
	 */
	public JobmapIconImageCacheEntry getJobmapIconImageDefaultApproval(String managerName) {
		if (m_jobmapIconImageCache.containsKey(managerName)
				&& m_jobmapIconImageCache.get(managerName).containsKey(m_defaultApprovalIconIdCache.get(managerName))) {
			return m_jobmapIconImageCache.get(managerName).get(m_defaultApprovalIconIdCache.get(managerName));
		} else {
			return null;
		}
	}

	/**
	 * ジョブマップ用デフォルトアイコンイメージ（監視ジョブ用）取得
	 * 
	 * @param managerName マネージャ名
	 * @return ジョブマップ用デフォルトアイコンイメージ（監視ジョブ用）
	 */
	public JobmapIconImageCacheEntry getJobmapIconImageDefaultMonitor(String managerName) {
		if (m_jobmapIconImageCache.containsKey(managerName)
				&& m_jobmapIconImageCache.get(managerName).containsKey(m_defaultMonitorIconIdCache.get(managerName))) {
			return m_jobmapIconImageCache.get(managerName).get(m_defaultMonitorIconIdCache.get(managerName));
		} else {
			return null;
		}
	}

	/**
	 * ジョブマップ用デフォルトアイコンイメージ（ファイル転送ジョブ用）取得
	 * 
	 * @param managerName マネージャ名
	 * @return ジョブマップ用デフォルトアイコンイメージ（ファイル転送ジョブ用）
	 */
	public JobmapIconImageCacheEntry getJobmapIconImageDefaultFile(String managerName) {
		if (m_jobmapIconImageCache.containsKey(managerName)
				&& m_jobmapIconImageCache.get(managerName).containsKey(m_defaultFileIconIdCache.get(managerName))) {
			return m_jobmapIconImageCache.get(managerName).get(m_defaultFileIconIdCache.get(managerName));
		} else {
			return null;
		}
	}

	/**
	 * ジョブマップ用デフォルトアイコンイメージ（ファイルチェックジョブ用）取得
	 * 
	 * @param managerName マネージャ名
	 * @return ジョブマップ用デフォルトアイコンイメージ（ファイルチェックジョブ用）
	 */
	public JobmapIconImageCacheEntry getJobmapIconImageDefaultFileCheck(String managerName) {
		if (m_jobmapIconImageCache.containsKey(managerName)
				&& m_jobmapIconImageCache.get(managerName).containsKey(m_defaultFileCheckIconIdCache.get(managerName))) {
			return m_jobmapIconImageCache.get(managerName).get(m_defaultFileCheckIconIdCache.get(managerName));
		} else {
			return null;
		}
	}

	/**
	 * ジョブマップ用デフォルトアイコンイメージ（リソース制御ジョブ用）取得
	 * 
	 * @param managerName マネージャ名
	 * @return ジョブマップ用デフォルトアイコンイメージ（リソース制御ジョブ用）
	 */
	public JobmapIconImageCacheEntry getJobmapIconImageDefaultResource(String managerName) {
		if (m_jobmapIconImageCache.containsKey(managerName)
				&& m_jobmapIconImageCache.get(managerName).containsKey(m_defaultResourceIconIdCache.get(managerName))) {
			return m_jobmapIconImageCache.get(managerName).get(m_defaultResourceIconIdCache.get(managerName));
		} else {
			return null;
		}
	}

	/**
	 * ジョブマップ用デフォルトアイコンイメージ（ジョブ連携送信ジョブ用）取得
	 * 
	 * @param managerName マネージャ名
	 * @return ジョブマップ用デフォルトアイコンイメージ（ジョブ連携送信ジョブ用）
	 */
	public JobmapIconImageCacheEntry getJobmapIconImageDefaultJobLinkSend(String managerName) {
		if (m_jobmapIconImageCache.containsKey(managerName)
				&& m_jobmapIconImageCache.get(managerName).containsKey(m_defaultJobLinkSendIconIdCache.get(managerName))) {
			return m_jobmapIconImageCache.get(managerName).get(m_defaultJobLinkSendIconIdCache.get(managerName));
		} else {
			return null;
		}
	}

	/**
	 * ジョブマップ用デフォルトアイコンイメージ（ジョブ連携待機ジョブ用）取得
	 * 
	 * @param managerName マネージャ名
	 * @return ジョブマップ用デフォルトアイコンイメージ（ジョブ連携待機ジョブ用）
	 */
	public JobmapIconImageCacheEntry getJobmapIconImageDefaultJobLinkRcv(String managerName) {
		if (m_jobmapIconImageCache.containsKey(managerName)
				&& m_jobmapIconImageCache.get(managerName).containsKey(m_defaultJobLinkRcvIconIdCache.get(managerName))) {
			return m_jobmapIconImageCache.get(managerName).get(m_defaultJobLinkRcvIconIdCache.get(managerName));
		} else {
			return null;
		}
	}

	/**
	 * ジョブマップ用デフォルトアイコンイメージ（RPAシナリオジョブ用）取得
	 * 
	 * @param managerName マネージャ名
	 * @return ジョブマップ用デフォルトアイコンイメージ（リソース制御ジョブ用）
	 */
	public JobmapIconImageCacheEntry getJobmapIconImageDefaultRpa(String managerName) {
		if (m_jobmapIconImageCache.containsKey(managerName)
				&& m_jobmapIconImageCache.get(managerName).containsKey(m_defaultRpaIconIdCache.get(managerName))) {
			return m_jobmapIconImageCache.get(managerName).get(m_defaultRpaIconIdCache.get(managerName));
		} else {
			return null;
		}
	}

	/**
	 * ジョブマップ用アイコンイメージ取得
	 * 
	 * @param managerName マネージャ名
	 * @param iconId アイコンID
	 * @return ジョブマップ用アイコンイメージ
	 */
	public JobmapIconImageCacheEntry getJobmapIconImageCacheEntry(String managerName, String iconId)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, 
			InvalidSetting, IconFileNotFound {
		if (m_jobmapIconImageCache.containsKey(managerName)
				&& m_jobmapIconImageCache.get(managerName).containsKey(iconId)) {
			return m_jobmapIconImageCache.get(managerName).get(iconId);
		} else {
			JobMapRestClientWrapper wrapper = JobMapRestClientWrapper.getWrapper(managerName);
			try {
				JobmapIconImageInfoResponse dto = wrapper.getJobmapIconImage(iconId);
				File file = wrapper.downloadJobmapIconImageFile(iconId);

				JobmapIconImageInfoResponse jobmapIconImage = new JobmapIconImageInfoResponse();
				RestClientBeanUtil.convertBean(dto, jobmapIconImage);

				if (!m_jobmapIconImageCache.containsKey(managerName)) {
					m_jobmapIconImageCache.put(managerName, new ConcurrentHashMap<String, JobmapIconImageCacheEntry>());
				}
				JobmapIconImageCacheEntry cacheEntry = createCacheEntry(jobmapIconImage, file);
				m_jobmapIconImageCache.get(managerName).put(jobmapIconImage.getIconId(), cacheEntry);
				return cacheEntry;
			} catch (Exception e) {
				throw e;
			}
		}
	}

	/**
	 * ジョブマップ用アイコンイメージ一覧取得（WS->キャッシュの同期あり）
	 * 
	 * @param managerName マネージャ名
	 * @return ジョブマップ用アイコンイメージ一覧
	 */
	public static List<JobmapIconImageCacheEntry> getJobmapIconImageList(String managerName)
			throws HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, IconFileNotFound, RestConnectFailed {
		
		List<JobmapIconImageCacheEntry> cacheEntryList =  new ArrayList<JobmapIconImageCacheEntry>();
		boolean isPublish = false;
		JobMapRestClientWrapper mapWrapper = JobMapRestClientWrapper.getWrapper(managerName);
		try{
			//ジョブマップ向けエンドポイント有効チェック
			isPublish = mapWrapper.checkPublish().getPublish();
		} catch (Exception e) {
			// マルチマネージャ接続時にジョブマップが有効になってないマネージャの混在によりendpoint通信で異常が出る場合あり
			// この場合は、該当マネージャのイメージ一覧は空とする
			if( m_log.isDebugEnabled() ){
				m_log.debug("Exception . getJobmapIconImageList(String managerName = " +managerName + " )");
			}
		}
		if(isPublish){
			// ジョブマップ向けエンドポイントが有効ならジョブマップ用アイコンファイル一覧情報取得
			List<JobmapIconImageInfoResponse> list = mapWrapper.getJobmapIconImageList();
			for (JobmapIconImageInfoResponse iconImage : list) {
				File file = mapWrapper.downloadJobmapIconImageFile(iconImage.getIconId());
				JobmapIconImageCacheEntry cacheEntry = createCacheEntry(iconImage, file);
				cacheEntryList.add(cacheEntry);
			}
		}
		//取得した情報をキャッシュにセット
		JobmapImageCacheUtil iconCache = JobmapImageCacheUtil.getInstance();
		iconCache.putCacheJobmapIconImageList(managerName, cacheEntryList);
		
		return cacheEntryList;
	}


	/**
	 * ジョブマップ用アイコンイメージ キャッシュセット
	 * 
	 * @param managerName マネージャ名
	 * @param ジョブマップ用アイコンイメージ一覧
	 */
	public void putCacheJobmapIconImageList(String managerName,List<JobmapIconImageCacheEntry> list ) {

		if (!m_jobmapIconImageCache.containsKey(managerName)) {
			m_jobmapIconImageCache.put(managerName, new ConcurrentHashMap<String, JobmapIconImageCacheEntry>());
		}
		if(list != null){
			for (JobmapIconImageCacheEntry cacheEntry : list) {
				m_jobmapIconImageCache.get(managerName).put(cacheEntry.getJobmapIconImage().getIconId(), cacheEntry);
			}
		}
	}

	/**
	 * ジョブマップ用アイコンイメージ新規登録・更新
	 * 
	 * @param managerName マネージャ名
	 * @param jobmapIconImage ジョブマップ用アイコンイメージ
	 * @param isNew true：新規登録、false：更新
	 */
	public void modifyJobmapIconImage(String managerName, JobmapIconImageInfoResponse jobmapIconImage, File iconImageFile, boolean isNew, byte[] iconFileData)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, 
			InvalidSetting, IconFileNotFound, IconFileDuplicate {

		JobMapRestClientWrapper wrapper = JobMapRestClientWrapper.getWrapper(managerName);

		File uploadFile = null;
		
		// キャッシュ上の値を確認する
		if (isNew) {
			// 新規登録
			uploadFile = iconImageFile;
			AddJobmapIconImageRequest dtoReq = new AddJobmapIconImageRequest();
			RestClientBeanUtil.convertBean(jobmapIconImage, dtoReq);
			wrapper.addJobmapIconImage(iconImageFile, dtoReq);
		} else {
			// 更新
			ModifyJobmapIconImageRequest dtoReq = new ModifyJobmapIconImageRequest();
			RestClientBeanUtil.convertBean(jobmapIconImage, dtoReq);
			
			if (iconImageFile != null) {
				uploadFile = iconImageFile;
			} else {
				FileOutputStream fos = null;
				try {
					uploadFile = File.createTempFile( TEMP_FILE_PREFIX, "" );
				
					fos = new FileOutputStream(uploadFile);
					fos.write(iconFileData);
					fos.close();
					
				} catch (Exception e) {
					throw new HinemosUnknown("write icondata tempfile failed", e);
				} finally {
					if (fos != null) {
						try {
							fos.close();
						} catch (IOException e) {
						}
					}
				}
			}
			
			wrapper.modifyJobmapIconImage(jobmapIconImage.getIconId(), uploadFile, dtoReq);
			
		}
		if (!m_jobmapIconImageCache.containsKey(managerName)) {
			m_jobmapIconImageCache.put(managerName, new ConcurrentHashMap<String, JobmapIconImageCacheEntry>());
		}
		JobmapIconImageCacheEntry cacheEntry = createCacheEntry(jobmapIconImage, uploadFile);
		m_jobmapIconImageCache.get(managerName).put(jobmapIconImage.getIconId(), cacheEntry);

		//画像イメージインスタンスのキャッシュの同期
		syncGraphicImageCache();
		
		if (iconImageFile == null && uploadFile.exists()) {
			if (!uploadFile.delete()) {
				m_log.info("tempiconfile delete fail" + uploadFile.getAbsolutePath());
			}
		}
	}

	/**
	 * ジョブマップ用アイコンイメージ削除
	 * 
	 * @param managerName マネージャ名
	 * @param iconId アイコンID
	 */
	public void deleteJobmapIconImage(String managerName, List<String> iconIds)
		throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, 
		InvalidSetting, IconFileNotFound {

		JobMapRestClientWrapper wrapper = JobMapRestClientWrapper.getWrapper(managerName);

		// キャッシュ上の値を確認する
		// 削除
		wrapper.deleteJobmapIconImage(String.join(",", iconIds));

		for (String iconId : iconIds) {
			if (m_jobmapIconImageCache.containsKey(managerName)) {
				m_jobmapIconImageCache.get(managerName).remove(iconId);
			}
		}
		//画像イメージインスタンスのキャッシュの同期
		syncGraphicImageCache();
	}

	/**
	 * イメージインスタンス取得（ファイルデータで）
	 *  JobmapIconImageが不明な場合はこちらで対応（アイコン管理ビュー向けの処理）
	 * @param target ファイルデータ
	 * @return 対応するイメージインスタンス
	 */
	public Image loadByteGraphicImage(byte[] target ){
		//違うインスタンスでも同じ内容のバイト配列ならキャッシュヒットするようにHexStringをキーとする
		String hexcode = DatatypeConverter.printHexBinary(target);
		Image ret = m_graphicImageCacheHexIndex.get(hexcode);
		if( ret == null ){//キャッシュにない場合のみインスタンスを生成
			if(m_log.isDebugEnabled()){
				m_log.debug("loadGraphicImage ( byte[] ) newImage target="+hexcode);
			}
			ret =JobmapIconImageUtil.getIconImage(target);
			m_graphicImageCacheHexIndex.put(hexcode,ret);
		}
		return ret;
	}
	/**
	 * イメージインスタンス取得（IconImageで）
	 *  通常はこちらでロード
	 * @param target アイコンイメージクラス
	 * @return 対応するイメージインスタンス
	 */
	public Image loadGraphicImage( JobmapIconImageInfoResponse target, byte[] filedata){
		Image ret = m_graphicImageCacheIconIndex.get(target);
		if( ret == null ){//キャッシュにない場合のみインスタンス取得
			if(m_log.isDebugEnabled()){
				m_log.debug("loadGraphicImage ( JobmapIconImage ) set target="+target.getIconId());
			}
			ret = loadByteGraphicImage(filedata);//せっかくなのでbyte側と共通化
			m_graphicImageCacheIconIndex.put(target,ret);
		}
		return ret;
	}
	/**
	 * イメージ変換結果キャッシュの同期
	 *  アイコンインデックスは完全再設定
	 *  Hexインデックスはつき合わせして払い落とし
	 */
	private void syncGraphicImageCache(){
		//アイコンインデックスは一旦全削除
		m_graphicImageCacheIconIndex.clear();
		//Hex一覧作成と アイコンインデックスを再設定
		HashSet<String> curHexList = new HashSet<String>();
		for(Map.Entry<String, Map<String, JobmapIconImageCacheEntry>> entry : m_jobmapIconImageCache.entrySet()){
			String managerName = entry.getKey();
			Map<String, JobmapIconImageCacheEntry> managerCache = m_jobmapIconImageCache.get(managerName);
			for(Map.Entry<String, JobmapIconImageCacheEntry> childEntry : managerCache.entrySet()){
				JobmapIconImageInfoResponse icon = managerCache.get(childEntry.getKey()).getJobmapIconImage();
				byte[] filedata = managerCache.get(childEntry.getKey()).getFiledata();
				String hexcode = DatatypeConverter.printHexBinary(filedata);
				curHexList.add(hexcode);
				loadGraphicImage(icon, filedata);
			}
		}
		//Hex一覧に存在しないHexキャッシュは削除
		Set<String> keySet = m_graphicImageCacheHexIndex.keySet();
		for( String disposeKey : keySet ){
			if( !  curHexList.contains(disposeKey) ){
				if(m_log.isDebugEnabled()){
					m_log.debug("loadGraphicImage ( JobmapIconImage ) delete hex="+disposeKey);
				}
				//XXX
				//Imageインスタンスはできれば廃棄（そのままだとリッチクライアントはGDIハンドルがリークする）したいが、
				//いずれかのビューで表示中のインスタンスをdisposeすると処理全体が落ちる。
				//ハンドルの上限10000までアイコン変更でリークすることは考えづらい。
				//現状は廃棄せずキャッシュからの削除にとどめる。
				//Image target = m_graphicImageCacheHexIndex.get(disposeKey);
				//target.dispose();
				m_graphicImageCacheHexIndex.remove(disposeKey);
			}
		}
	}
	
	/**
	 * キャッシュ情報更新（全体）
	 */
	public void refresh() {
		m_log.debug("refresh ()");
		// 初期化
		m_jobmapIconImageCache.clear();

		// マネージャ毎での更新
		for(String managerName : RestConnectManager.getActiveManagerSet()) {
			refresh(managerName,false);
		}

		//画像イメージインスタンスのキャッシュの同期
		syncGraphicImageCache();
	}
	
	/**
	 * キャッシュ情報更新（マネージャー毎）
	 */
	public void refresh(String managerName , boolean isSyncGraphicImage ) {
		m_log.debug("start refresh(String managerName = " +managerName + " )");
		// 該当マネージャーのキャッシュを削除
		m_jobmapIconImageCache.remove(managerName);

		// ジョブマップ用アイコンID（ジョブ）
		String defaultJobIconId = "";
		// ジョブマップ用アイコンID（ジョブネット）
		String defaultJobnetIconId = "";
		// ジョブマップ用アイコンID（承認ジョブ）
		String defaultApprovalIconId = "";
		// ジョブマップ用アイコンID（監視ジョブ）
		String defaultMonitorIconId = "";
		// ジョブマップ用アイコンID（ファイル転送ジョブ）
		String defaultFileIconId = "";
		// ジョブマップ用アイコンID（ファイルチェックジョブ）
		String defaultFileCheckIconId = "";
		// ジョブマップ用アイコンID（リソース制御ジョブ）
		String defaultResourceIconId = "";
		// ジョブマップ用アイコンID（ジョブ連携送信ジョブ）
		String defaultJobLinkSendIconId = "";
		// ジョブマップ用アイコンID（ジョブ連携待機ジョブ）
		String defaultJobLinkRcvIconId = "";
		// ジョブマップ用アイコンID（RPAシナリオジョブ）
		String defaultJobRpaIconId = "";

		JobMapRestClientWrapper mapWrapper = JobMapRestClientWrapper.getWrapper(managerName);
		try {
			// ジョブマップ用アイコンファイル一覧取得
			List<JobmapIconImageInfoResponse> list = mapWrapper.getJobmapIconImageList();
			//取得した情報をキャッシュにセット
			List<JobmapIconImageCacheEntry> entryList = new ArrayList<>();
			for (JobmapIconImageInfoResponse iconImage : list) {
				File file = mapWrapper.downloadJobmapIconImageFile(iconImage.getIconId());
				entryList.add(createCacheEntry(iconImage, file));
			}
			putCacheJobmapIconImageList(managerName, entryList);

			// ジョブマップ用アイコンID取得
			List<JobmapIconIdDefaultListResponse> dtoResList = mapWrapper.getJobmapIconIdDefaultList();
			for (JobmapIconIdDefaultListResponse dtoRes : dtoResList) {
				switch(dtoRes.getType()) {
					case JOB:
						defaultJobIconId = dtoRes.getDefaultId();
						break;
					case JOBNET:
						defaultJobnetIconId = dtoRes.getDefaultId();
						break;
					case APPROVALJOB:
						defaultApprovalIconId = dtoRes.getDefaultId();
						break;
					case MONITORJOB:
						defaultMonitorIconId = dtoRes.getDefaultId();
						break;
					case FILEJOB:
						defaultFileIconId = dtoRes.getDefaultId();
						break;
					case FILECHECKJOB:
						defaultFileCheckIconId = dtoRes.getDefaultId();
						break;
					case RESOURCEJOB:
						defaultResourceIconId = dtoRes.getDefaultId();
						break;
					case JOBLINKSENDJOB:
						defaultJobLinkSendIconId = dtoRes.getDefaultId();
						break;
					case JOBLINKRCVJOB:
						defaultJobLinkRcvIconId = dtoRes.getDefaultId();
						break;
					case RPAJOB:
						defaultJobRpaIconId = dtoRes.getDefaultId();
						break;
					default:
						break;
				}
			}
		} catch (InvalidRole e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (HinemosUnknown e) {
			// マルチマネージャ接続時にジョブマップが有効になってないマネージャの混在によりendpoint通信で異常が出る場合あり
			// 有効になっていないマネージャからは原因例外UrlNotFoundが返る
			// この場合は、エラーダイアログなどは表示しない。
			if(UrlNotFound.class.equals(e.getCause().getClass())) {
				if( m_log.isDebugEnabled() ){
					m_log.debug("WebServiceException . refresh(String managerName = " +managerName + " )");
				}
			} else {
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}
		} catch (Exception e) {
			// 上記以外の例外
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
		m_defaultJobIconIdCache.put(managerName, defaultJobIconId);
		m_defaultJobnetIconIdCache.put(managerName, defaultJobnetIconId);
		m_defaultApprovalIconIdCache.put(managerName, defaultApprovalIconId);
		m_defaultMonitorIconIdCache.put(managerName, defaultMonitorIconId);
		m_defaultFileIconIdCache.put(managerName, defaultFileIconId);
		m_defaultFileCheckIconIdCache.put(managerName, defaultFileCheckIconId);
		m_defaultResourceIconIdCache.put(managerName, defaultResourceIconId);
		m_defaultJobLinkSendIconIdCache.put(managerName, defaultJobLinkSendIconId);
		m_defaultJobLinkRcvIconIdCache.put(managerName, defaultJobLinkRcvIconId);
		m_defaultRpaIconIdCache.put(managerName, defaultJobRpaIconId);

		if( isSyncGraphicImage ){
			//必要なら画像イメージインスタンスのキャッシュの同期
			syncGraphicImageCache();
		}
		m_log.debug("end refresh(String managerName = " +managerName + " )");
	}

	private static JobmapIconImageCacheEntry createCacheEntry(JobmapIconImageInfoResponse jobmapIconImage,
			File iconImageFile) throws HinemosUnknown {
		JobmapIconImageCacheEntry cacheEntry = new JobmapIconImageCacheEntry();
		cacheEntry.setJobmapIconImage(jobmapIconImage);
		try {
			cacheEntry.setFiledata(Files.readAllBytes(iconImageFile.toPath()));
		} catch (IOException e) {
			m_log.warn("createCacheEntry : " + e.getMessage());
			throw new HinemosUnknown(e);
		} finally {
			if (iconImageFile.exists()) {
				if (!iconImageFile.delete()) {
					m_log.warn("Failed delete. file=" + iconImageFile.getAbsolutePath());
				}
			}
		}
		return cacheEntry;
	}
}
