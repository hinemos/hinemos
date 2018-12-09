/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.jobmanagement.HinemosUnknown_Exception;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;
import com.clustercontrol.ws.jobmanagement.InvalidUserPass_Exception;
import com.clustercontrol.ws.jobmanagement.JobInfo;
import com.clustercontrol.ws.jobmanagement.JobMasterNotFound_Exception;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;
import com.clustercontrol.ws.jobmanagement.NotifyNotFound_Exception;
import com.clustercontrol.ws.jobmanagement.UserNotFound_Exception;

/**
 * JobEditStateクラス
 *
 * 以下を提供します。<BR>
 * <li>編集ロックに関するユーティリティ
 * <li>ジョブ登録時に更新する必要があるジョブユニットを管理するユーティリティ
 *
 * @version 5.0.0
 * @since 4.1.0
 */
public class JobEditState{
	/** Logger */
	private static Log m_log = LogFactory.getLog( JobEditState.class );

	/** 新規作成または編集したマネージャに登録する必要があるジョブユニットのリスト */
	private ArrayList<JobTreeItem> editedJobunitList = new ArrayList<JobTreeItem>();

	/** 削除されたマネージャから削除する必要があるジョブユニットのリスト */
	private ArrayList<JobTreeItem> deletedJobunitList = new ArrayList<JobTreeItem>();

	/** 編集ロックを取得したジョブユニットのバックアップ */
	private ConcurrentHashMap<JobInfo, JobTreeItem> lockedJobunitMap = new ConcurrentHashMap<JobInfo, JobTreeItem>();

	/** 編集ロックを取得したジョブユニットのリスト */
	private ConcurrentHashMap<JobInfo, Integer> editSessionMap = new ConcurrentHashMap<JobInfo, Integer>();

	/** ジョブユニットと最終更新日時の組 */
	private ConcurrentHashMap<String, Long> jobunitUpdateTimeMap = new ConcurrentHashMap<String, Long>();

	private String managerName;
	private JobTreeItem m_jobTreeItem; // マネージャごとのジョブツリーのキャッシュ

	public JobEditState( String managerName ){
		this.managerName = managerName;
	}

	/**
	 * マネージャに登録する必要があるジョブユニットを追加します
	 * @param jobunit 編集したジョブユニット
	 */
	public void addEditedJobunit(JobTreeItem jobunit ){
		if( jobunit.getData().getType() == JobConstant.TYPE_COMPOSITE ){
			// ツリーのトップが呼ばれることはないはず
			m_log.warn("addEditJobunit() : jobunit is TOP");
			return;
		} else if( jobunit.getData().getType() == JobConstant.TYPE_JOBUNIT ){
			// ジョブユニットの場合は編集済みジョブユニットに登録する

			if( !editedJobunitList.contains(jobunit) ){
				m_log.debug("addEditJobunit() : add " + jobunit.getData().getJobunitId());
				editedJobunitList.add(jobunit);
			}
		} else {
			// ジョブユニットでない場合は親を再帰的に呼び出す
			addEditedJobunit(jobunit.getParent());
		}
	}

	/**
	 * マネージャに登録する必要があるジョブユニットのリストを返します。
	 *
	 * @return ArrayList<JobTreeItem>
	 */
	public ArrayList<JobTreeItem> getEditedJobunitList( ){
		return editedJobunitList;
	}

	/**
	 *マネージャに登録する必要があるジョブユニットのリストからジョブユニットを削除します。
	 *
	 */
	public void removeEditedJobunit( JobTreeItem jobunit ){
		editedJobunitList.remove(jobunit);
	}

	/**
	 * マネージャから削除する必要があるジョブユニットを追加します
	 * @param jobunit 削除操作したジョブユニット
	 */
	public void addDeletedJobunit( JobTreeItem jobunit ){
		deletedJobunitList.add(jobunit);
	}

	/**
	 * マネージャから削除する必要があるジョブユニットのリストを返します。<BR>
	 *
	 * @return ArrayList<String>
	 */
	public ArrayList<JobTreeItem> getDeletedJobunitList( ){
		return deletedJobunitList;
	}

	/**
	 * ジョブツリーが編集された時にtrueを返します。
	 * @return ジョブツリーが編集されている場合true
	 */
	public boolean isEditing(){
		m_log.debug("isEditing() : lockedJobunitList.size()=" + lockedJobunitMap.size());
		boolean edit = true;
		if( lockedJobunitMap.size() == 0 ){
			// 編集モードのジョブが存在しない場合
			edit = false;
		}
		return edit;
	}

	/**
	 * 編集ロックを取得したjobunitのリストを返します。
	 * @return ArrayList<JobInfo> 編集ロックを取得したジョブユニットのリスト
	 */
	public List<JobInfo> getLockedJobunitList( ){
		List<JobInfo> list = new ArrayList<JobInfo>();
		for( JobInfo info : editSessionMap.keySet() ){
			m_log.debug("list add " + info.getJobunitId());
			list.add(info);
		}
		return list;
	}

	/**
	 * ジョブユニットのバックアップ(編集ロック取得前)を返す
	 * @param info
	 * @return ジョブユニットのバックアップ
	 */
	public JobTreeItem getLockedJobunitBackup(JobInfo info ){
		return lockedJobunitMap.get(info);
	}

	/**
	 * ジョブユニットの編集ロック状態をクリアする
	 *
	 * @param info
	 */
	public void removeLockedJobunit(JobInfo info ){
		lockedJobunitMap.remove(info);
		editSessionMap.remove(info);
	}

	/**
	 * 編集ロックを取得したジョブユニットを追加する
	 * @param info ジョブユニット
	 * @param item ジョブツリーのバックアップ
	 * @param editSession セッション
	 */
	public void addLockedJobunit(JobInfo info, JobTreeItem item, Integer editSession ){
		if (info == null) {
			m_log.info("JobInfo is null, editSession=" + editSession);
			return;
		}
		if (editSession == null) {
			m_log.info("info=" + info + "info.getId()=" + info.getId());
			return;
		}
		editSessionMap.put(info, editSession);
		if( item != null ){
			lockedJobunitMap.put(info, item);
		}
	}

	/**
	 * 編集ロックを取得したジョブユニット配下のジョブかどうかをチェックする。
	 * @param jobunitId 選択されたジョブのジョブユニットID
	 * @return 編集ロックを取得したジョブユニット配下のジョブである場合true
	 */
	public boolean isLockedJobunitId(String jobunitId ){
		for (JobInfo info : editSessionMap.keySet()) {
			if (jobunitId.equals(info.getJobunitId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * ジョブユニットのセッションを取得する
	 *
	 * @param info ジョブユニット
	 * @return セッション
	 */
	public Integer getEditSession(JobInfo info ){
		return editSessionMap.get(info);
	}

	/**
	 * 編集ロックの状態をすべてクリアする
	 */
	public void clearEditStateAll( ){
		m_log.debug("clearEditStateAll()");

		editedJobunitList.clear();
		deletedJobunitList.clear();
		lockedJobunitMap.clear();
		editSessionMap.clear();
		jobunitUpdateTimeMap.clear();
	}

	/**
	 * 編集したジョブユニットの編集状態をクリアする
	 * 編集していない場合は、removeLockedJobunit()の呼び出しだけでよい
	 *
	 * @param item ジョブツリーアイテム
	 */
	public void exitEditMode(JobTreeItem item ){
		editedJobunitList.remove(item);
		deletedJobunitList.remove(item);
		removeLockedJobunit(item.getData());
	}

	/**
	 * 指定したジョブユニットの最終更新日時を取得します
	 *
	 * @param jobunitId ジョブユニットID
	 * @return 最終更新日時
	 */
	public Long getJobunitUpdateTime(String jobunitId ){
		return jobunitUpdateTimeMap.get(jobunitId);
	}

	/**
	 * 指定したジョブユニットの最終更新日時を設定します
	 *
	 * @param jobunitId ジョブユニットID
	 * @param updateTime 最終更新日時
	 */
	public void putJobunitUpdateTime(String jobunitId, Long updateTime ){
		jobunitUpdateTimeMap.put(jobunitId, updateTime);
	}

	/**
	 * 最終更新日時を保存するマップから指定したジョブユニットの情報を削除します
	 *
	 * @param jobunitId 情報を削除するジョブユニットID
	 */
	public void removeJobunitUpdateTime( String jobunitId ){
		jobunitUpdateTimeMap.remove(jobunitId);
	}

	private void updateJobunitUpdateTime( JobTreeItem tree ){
		try {
			List<String> jobunitIdList = new ArrayList<String>();
			for( JobTreeItem jobunit : tree.getChildren().get(0).getChildren() ){
				String jobunitId = jobunit.getData().getJobunitId();
				jobunitIdList.add(jobunitId);
			}
			
			JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(managerName);
			List<Long> updateTimeList = wrapper.getUpdateTimeList(jobunitIdList);
			if (updateTimeList.size() != jobunitIdList.size()) {
				m_log.warn("size differ " + updateTimeList.size() + ", " + jobunitIdList.size());
			}
			for (int i = 0; i < jobunitIdList.size(); i++) {
				String jobunitId = jobunitIdList.get(i);
				Long updateTime = updateTimeList.get(i);
				if( updateTime != null ){
					m_log.info("jobunitId="  + jobunitId + ", updateTime=" + updateTime); 
					putJobunitUpdateTime(jobunitId, updateTime);
				}
			}
		} catch (InvalidRole_Exception e ){
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e ){
			m_log.warn("updateJobunitUpdateTime() : " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
	}

	/**
	 * ジョブツリーをマネージャから取得して、キャッシュを更新して返します
	 * 
	 * @param ownerRoleId
	 * @param m_treeOnly
	 * @return
	 * @throws HinemosUnknown_Exception
	 * @throws InvalidRole_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws JobMasterNotFound_Exception
	 * @throws NotifyNotFound_Exception
	 * @throws UserNotFound_Exception
	 */
	public JobTreeItem updateJobTree( String ownerRoleId, boolean m_treeOnly ) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, JobMasterNotFound_Exception, NotifyNotFound_Exception, UserNotFound_Exception{
		// managerがルートとなる木を作成する
		//     manager
		//     |
		//     -----jobunits

		//ジョブツリーのルート(Manager)を生成
		m_jobTreeItem = new JobTreeItem();
		JobInfo managerInfo = new JobInfo();
		managerInfo.setId(managerName);
		managerInfo.setName(managerName);
		managerInfo.setType(JobConstant.TYPE_MANAGER);
		managerInfo.setJobunitId(managerName);
		m_jobTreeItem.setData(managerInfo);
		m_jobTreeItem.setParent(null);
		
		JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(managerName);

		JobTreeItem orgTree = wrapper.getJobTree(ownerRoleId, m_treeOnly);

		updateJobunitUpdateTime(orgTree);

		// Children
		for( JobTreeItem childItem : orgTree.getChildren().get(0).getChildren() ){
			childItem.setParent(m_jobTreeItem);
			m_jobTreeItem.getChildren().add(childItem);
		}
		
		return m_jobTreeItem;
	}
	
	/**
	 * ジョブツリーのキャッシュをそのまま返します
	 * @return
	 */
	public JobTreeItem getJobTree() {
		return m_jobTreeItem;
	}
	

	/**
	 * Clear job lock states
	 */
	public void releaseEditLock(){
		List<JobInfo> lockedJobunitList = getLockedJobunitList();
		if( lockedJobunitList.size() > 0 ){
			JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper( managerName );
			for( JobInfo jobunit : lockedJobunitList ){
				try{
					wrapper.releaseEditLock(getEditSession(jobunit));
					removeLockedJobunit(jobunit);
					m_log.info("releaseEditLock jobunit=" + jobunit + ", managerName=" + managerName);
				}catch(Exception e){
					m_log.warn("releaseEditLock() : " + e.getMessage());
				}
			}
		}
		clearEditStateAll();
	}
}
