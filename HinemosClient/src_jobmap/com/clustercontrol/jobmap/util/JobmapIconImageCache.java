/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.DatatypeConverter;
import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;

import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.jobmanagement.util.JobmapIconImageUtil;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.jobmanagement.HinemosUnknown_Exception;
import com.clustercontrol.ws.jobmanagement.IconFileDuplicate_Exception;
import com.clustercontrol.ws.jobmanagement.IconFileNotFound_Exception;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;
import com.clustercontrol.ws.jobmanagement.InvalidSetting_Exception;
import com.clustercontrol.ws.jobmanagement.InvalidUserPass_Exception;
import com.clustercontrol.ws.jobmanagement.JobmapIconImage;

/**
 * ジョブマップ用のアイコンの画像キャッシュ管理クラス。
 * 
 * インスタンス管理については RCP/RAP間の差分実装が必要なため
 * 本クラスを継承したJobmapImageCacheUtil側に委ねています。
 **/
public class JobmapIconImageCache {

	// ログ
	private static Log m_log = LogFactory.getLog( JobmapIconImageCache.class );

	private Map<String, Map<String, JobmapIconImage>> m_jobmapIconImageCache 
		= new ConcurrentHashMap<>();
	//グラフィックイメージキャッシュ（画像バイトコード単位）
	private Map<String, Image> m_graphicImageCacheHexIndex 
	= new ConcurrentHashMap<>();
	//グラフィックイメージキャッシュ（アイコンID単位）
	private Map<JobmapIconImage, Image> m_graphicImageCacheIconIndex 
		= new ConcurrentHashMap<>();
	private Map<String, String> m_defaultJobnetIconIdCache = new ConcurrentHashMap<>();
	private Map<String, String> m_defaultJobIconIdCache = new ConcurrentHashMap<>();
	private Map<String, String> m_defaultApprovalIconIdCache = new ConcurrentHashMap<>();
	private Map<String, String> m_defaultMonitorIconIdCache = new ConcurrentHashMap<>();
	private Map<String, String> m_defaultFileIconIdCache = new ConcurrentHashMap<>();
	
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
	 * ジョブマップ用デフォルトアイコンイメージ（ジョブネット用）取得
	 * 
	 * @param managerName マネージャ名
	 * @return ジョブマップ用デフォルトアイコンイメージ（ジョブネット用）
	 */
	public JobmapIconImage getJobmapIconImageDefaultJobnet(String managerName) {
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
	public JobmapIconImage getJobmapIconImageDefaultJob(String managerName) {
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
	public JobmapIconImage getJobmapIconImageDefaultApproval(String managerName) {
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
	public JobmapIconImage getJobmapIconImageDefaultMonitor(String managerName) {
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
	public JobmapIconImage getJobmapIconImageDefaultFile(String managerName) {
		if (m_jobmapIconImageCache.containsKey(managerName)
				&& m_jobmapIconImageCache.get(managerName).containsKey(m_defaultFileIconIdCache.get(managerName))) {
			return m_jobmapIconImageCache.get(managerName).get(m_defaultFileIconIdCache.get(managerName));
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
	public JobmapIconImage getJobmapIconImage(String managerName, String iconId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, 
			InvalidSetting_Exception, IconFileNotFound_Exception {
		if (m_jobmapIconImageCache.containsKey(managerName)
				&& m_jobmapIconImageCache.get(managerName).containsKey(iconId)) {
			return m_jobmapIconImageCache.get(managerName).get(iconId);
		} else {
			JobMapEndpointWrapper wrapper = JobMapEndpointWrapper.getWrapper(managerName);
			try {
				JobmapIconImage jobmapIconImage = wrapper.getJobmapIconImage(iconId);
				if (!m_jobmapIconImageCache.containsKey(managerName)) {
					m_jobmapIconImageCache.put(managerName, new ConcurrentHashMap<String, JobmapIconImage>());
				}
				m_jobmapIconImageCache.get(managerName).put(jobmapIconImage.getIconId(), jobmapIconImage);
				return jobmapIconImage;
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
	public static List<JobmapIconImage> getJobmapIconImageList(String managerName)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, 
			InvalidSetting_Exception, IconFileNotFound_Exception {
		
		List<JobmapIconImage> list =  new ArrayList<JobmapIconImage>();
		String versionInfo = null; 
		try{
			//ジョブマップ向けエンドポイント有効チェック
			JobMapEndpointWrapper mapWrapper = JobMapEndpointWrapper.getWrapper(managerName);
			versionInfo = mapWrapper.getVersion();
		} catch (Exception e) {
			// マルチマネージャ接続時にジョブマップが有効になってないマネージャの混在によりendpoint通信で異常が出る場合あり
			// この場合は、該当マネージャのイメージ一覧は空とする
			if( m_log.isDebugEnabled() ){
				m_log.debug("Exception . getJobmapIconImageList(String managerName = " +managerName + " )");
			}
		}
		if( versionInfo != null ){
			// ジョブマップ向けエンドポイントが有効ならジョブマップ用アイコンファイル一覧情報取得
			JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(managerName);
			list = wrapper.getJobmapIconImageList();
		}
		//取得した情報をキャッシュにセット
		JobmapImageCacheUtil iconCache = JobmapImageCacheUtil.getInstance();
		iconCache.putCacheJobmapIconImageList(managerName, list);
		
		return list;
	}


	/**
	 * ジョブマップ用アイコンイメージ キャッシュセット
	 * 
	 * @param managerName マネージャ名
	 * @param ジョブマップ用アイコンイメージ一覧
	 */
	public void putCacheJobmapIconImageList(String managerName,List<JobmapIconImage> list ) {

		if (!m_jobmapIconImageCache.containsKey(managerName)) {
			m_jobmapIconImageCache.put(managerName, new ConcurrentHashMap<String, JobmapIconImage>());
		}
		if(list != null){
			for (JobmapIconImage jobmapIconImage : list) {
				m_jobmapIconImageCache.get(managerName).put(jobmapIconImage.getIconId(), jobmapIconImage);
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
	public void modifyJobmapIconImage(String managerName, JobmapIconImage jobmapIconImage, boolean isNew)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, 
			InvalidSetting_Exception, IconFileNotFound_Exception, IconFileDuplicate_Exception {

		JobMapEndpointWrapper wrapper = JobMapEndpointWrapper.getWrapper(managerName);

		// キャッシュ上の値を確認する
		if (isNew) {
			// 新規登録
			wrapper.addJobmapIconImage(jobmapIconImage);
		} else {
			// 更新
			wrapper.modifyJobmapIconImage(jobmapIconImage);
		}
		if (!m_jobmapIconImageCache.containsKey(managerName)) {
			m_jobmapIconImageCache.put(managerName, new ConcurrentHashMap<String, JobmapIconImage>());
		}
		m_jobmapIconImageCache.get(managerName).put(jobmapIconImage.getIconId(), jobmapIconImage);

		//画像イメージインスタンスのキャッシュの同期
		syncGraphicImageCache();
	}

	/**
	 * ジョブマップ用アイコンイメージ削除
	 * 
	 * @param managerName マネージャ名
	 * @param iconId アイコンID
	 */
	public void deleteJobmapIconImage(String managerName, List<String> iconIds)
		throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, 
		InvalidSetting_Exception, IconFileNotFound_Exception {

		JobMapEndpointWrapper wrapper = JobMapEndpointWrapper.getWrapper(managerName);

		// キャッシュ上の値を確認する
		// 削除
		wrapper.deleteJobmapIconImage(iconIds);

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
	public Image loadGraphicImage( JobmapIconImage target ){
		Image ret = m_graphicImageCacheIconIndex.get(target);
		if( ret == null ){//キャッシュにない場合のみインスタンス取得
			if(m_log.isDebugEnabled()){
				m_log.debug("loadGraphicImage ( JobmapIconImage ) set target="+target.getIconId());
			}
			ret = loadByteGraphicImage(target.getFiledata());//せっかくなのでbyte側と共通化
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
		for(Map.Entry<String, Map<String, JobmapIconImage>> entry : m_jobmapIconImageCache.entrySet()){
			String managerName = entry.getKey();
			Map<String, JobmapIconImage> managerCache = m_jobmapIconImageCache.get(managerName);
			for(Map.Entry<String, JobmapIconImage> childEntry : managerCache.entrySet()){
				JobmapIconImage icon = managerCache.get(childEntry.getKey());
				String hexcode = DatatypeConverter.printHexBinary(icon.getFiledata());
				curHexList.add(hexcode);
				loadGraphicImage(icon);
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
		for(String managerName : EndpointManager.getActiveManagerSet()) {
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

		JobMapEndpointWrapper wrapper = JobMapEndpointWrapper.getWrapper(managerName);

		JobEndpointWrapper mapWrapper = JobEndpointWrapper.getWrapper(managerName);
		try {
			// ジョブマップ用アイコンファイル一覧取得
			List<JobmapIconImage> list = mapWrapper.getJobmapIconImageList();
			//取得した情報をキャッシュにセット
			putCacheJobmapIconImageList(managerName, list);

			// ジョブマップ用アイコンID（ジョブ）取得
			defaultJobIconId = wrapper.getJobmapIconIdJobDefault();
			// ジョブマップ用アイコンID（ジョブネット）取得
			defaultJobnetIconId = wrapper.getJobmapIconIdJobnetDefault();
			// ジョブマップ用アイコンID（承認ジョブ）取得
			defaultApprovalIconId = wrapper.getJobmapIconIdApprovalDefault();
			// ジョブマップ用アイコンID（監視ジョブ）取得
			defaultMonitorIconId = wrapper.getJobmapIconIdMonitorDefault();
			// ジョブマップ用アイコンID（ファイル転送ジョブ）取得
			defaultFileIconId = wrapper.getJobmapIconIdFileDefault();
		} catch (WebServiceException e) {
			// マルチマネージャ接続時にジョブマップが有効になってないマネージャの混在によりendpoint通信で異常が出る場合あり
			// この場合は、エラーダイアログなどは表示しない。
			if( m_log.isDebugEnabled() ){
				m_log.debug("WebServiceException . refresh(String managerName = " +managerName + " )");
			}
		} catch (InvalidRole_Exception e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
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

		if( isSyncGraphicImage ){
			//必要なら画像イメージインスタンスのキャッシュの同期
			syncGraphicImageCache();
		}
		m_log.debug("end refresh(String managerName = " +managerName + " )");
	}
}
