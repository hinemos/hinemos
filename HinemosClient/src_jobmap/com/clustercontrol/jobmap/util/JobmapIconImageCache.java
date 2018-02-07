/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
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


public class JobmapIconImageCache {

	private static Map<String, Map<String, JobmapIconImage>> m_jobmapIconImageCache 
		= new ConcurrentHashMap<>();
	private static Map<String, String> m_defaultJobnetIconIdCache = new ConcurrentHashMap<>();
	private static Map<String, String> m_defaultJobIconIdCache = new ConcurrentHashMap<>();
	private static Map<String, String> m_defaultApprovalIconIdCache = new ConcurrentHashMap<>();
	private static Map<String, String> m_defaultMonitorIconIdCache = new ConcurrentHashMap<>();
	private static Map<String, String> m_defaultFileIconIdCache = new ConcurrentHashMap<>();
	
	static {
		refresh();
	}

	/**
	 * ジョブマップ用デフォルトアイコンID（ジョブネット用）取得
	 * 
	 * @param managerName マネージャ名
	 * @return ジョブマップ用デフォルトアイコンID（ジョブネット用）
	 */
	public static String getJobmapIconIdDefaultJobnet(String managerName) {
		return m_defaultJobnetIconIdCache.get(managerName);
	}

	/**
	 * ジョブマップ用デフォルトアイコンID（ジョブ用）取得
	 * 
	 * @param managerName マネージャ名
	 * @return ジョブマップ用デフォルトアイコンID（ジョブ用）
	 */
	public static String getJobmapIconIdDefaultJob(String managerName) {
		return m_defaultJobIconIdCache.get(managerName);
	}

	/**
	 * ジョブマップ用デフォルトアイコンID（承認ジョブ用）取得
	 * 
	 * @param managerName マネージャ名
	 * @return ジョブマップ用デフォルトアイコンID（承認ジョブ用）
	 */
	public static String getJobmapIconIdDefaultApproval(String managerName) {
		return m_defaultApprovalIconIdCache.get(managerName);
	}

	/**
	 * ジョブマップ用デフォルトアイコンID（監視ジョブ用）取得
	 * 
	 * @param managerName マネージャ名
	 * @return ジョブマップ用デフォルトアイコンID（監視ジョブ用）
	 */
	public static String getJobmapIconIdDefaultMonitor(String managerName) {
		return m_defaultMonitorIconIdCache.get(managerName);
	}

	/**
	 * ジョブマップ用デフォルトアイコンID（ファイル転送ジョブ用）取得
	 * 
	 * @param managerName マネージャ名
	 * @return ジョブマップ用デフォルトアイコンID（ファイル転送ジョブ用）
	 */
	public static String getJobmapIconIdDefaultFile(String managerName) {
		return m_defaultFileIconIdCache.get(managerName);
	}

	/**
	 * ジョブマップ用デフォルトアイコンイメージ（ジョブネット用）取得
	 * 
	 * @param managerName マネージャ名
	 * @return ジョブマップ用デフォルトアイコンイメージ（ジョブネット用）
	 */
	public static JobmapIconImage getJobmapIconImageDefaultJobnet(String managerName) {
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
	public static JobmapIconImage getJobmapIconImageDefaultJob(String managerName) {
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
	public static JobmapIconImage getJobmapIconImageDefaultApproval(String managerName) {
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
	public static JobmapIconImage getJobmapIconImageDefaultMonitor(String managerName) {
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
	public static JobmapIconImage getJobmapIconImageDefaultFile(String managerName) {
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
	public static JobmapIconImage getJobmapIconImage(String managerName, String iconId)
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
	 * ジョブマップ用アイコンイメージ一覧取得
	 * 
	 * @param managerName マネージャ名
	 * @return ジョブマップ用アイコンイメージ一覧
	 */
	public static List<JobmapIconImage> getJobmapIconImageList(String managerName)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, 
			InvalidSetting_Exception, IconFileNotFound_Exception {


		JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(managerName);

		// ジョブマップ用アイコンファイル一覧情報取得
		List<JobmapIconImage> list = wrapper.getJobmapIconImageList();
		if (!m_jobmapIconImageCache.containsKey(managerName)) {
			m_jobmapIconImageCache.put(managerName, new ConcurrentHashMap<String, JobmapIconImage>());
		}
		if(list != null){
			for (JobmapIconImage jobmapIconImage : list) {
				m_jobmapIconImageCache.get(managerName).put(jobmapIconImage.getIconId(), jobmapIconImage);
			}
		}
		return list;
	}

	/**
	 * ジョブマップ用アイコンイメージ新規登録・更新
	 * 
	 * @param managerName マネージャ名
	 * @param jobmapIconImage ジョブマップ用アイコンイメージ
	 * @param isNew true：新規登録、false：更新
	 */
	public static void modifyJobmapIconImage(String managerName, JobmapIconImage jobmapIconImage, boolean isNew)
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
	}

	/**
	 * ジョブマップ用アイコンイメージ削除
	 * 
	 * @param managerName マネージャ名
	 * @param iconId アイコンID
	 */
	public static void deleteJobmapIconImage(String managerName, List<String> iconIds)
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
	}

	/**
	 * キャッシュ情報更新
	 */
	public static void refresh() {
		// 初期化
		m_jobmapIconImageCache.clear();

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

		// マネージャより取得
		for(String managerName : EndpointManager.getActiveManagerSet()) {
			JobMapEndpointWrapper wrapper = JobMapEndpointWrapper.getWrapper(managerName);

			try {
				// ジョブマップ用アイコンファイル一覧取得
				getJobmapIconImageList(managerName);

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
		}
	}
}
