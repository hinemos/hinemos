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
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.utility.util.UtilityManagerUtil;

import org.openapitools.client.model.JobTreeItemResponseP1;
import org.openapitools.client.model.JobTreeItemResponseP2;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.UserNotFound;

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
	private ArrayList<JobTreeItemWrapper> editedJobunitList = new ArrayList<JobTreeItemWrapper>();

	/** 削除されたマネージャから削除する必要があるジョブユニットのリスト */
	private ArrayList<JobTreeItemWrapper> deletedJobunitList = new ArrayList<JobTreeItemWrapper>();

	/** 編集ロックを取得したジョブユニットのバックアップ */
	private ConcurrentHashMap<JobInfoWrapper, JobTreeItemWrapper> lockedJobunitMap = new ConcurrentHashMap<JobInfoWrapper, JobTreeItemWrapper>();

	/** 編集ロックを取得したジョブユニットのリスト */
	private ConcurrentHashMap<JobInfoWrapper, Integer> editSessionMap = new ConcurrentHashMap<JobInfoWrapper, Integer>();

	/** ジョブユニットと最終更新日時の組 */
	private ConcurrentHashMap<String, Long> jobunitUpdateTimeMap = new ConcurrentHashMap<String, Long>();

	private String managerName;
	private JobTreeItemWrapper m_jobTreeItem; // マネージャごとのジョブツリーのキャッシュ

	public JobEditState( String managerName ){
		this.managerName = managerName;
	}

	/**
	 * マネージャに登録する必要があるジョブユニットを追加します
	 * @param jobunit 編集したジョブユニット
	 */
	public void addEditedJobunit(JobTreeItemWrapper jobunit ){
		if( jobunit.getData().getType() == JobInfoWrapper.TypeEnum.COMPOSITE ){
			// ツリーのトップが呼ばれることはないはず
			m_log.warn("addEditJobunit() : jobunit is TOP");
			return;
		} else if( jobunit.getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT ){
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
	public ArrayList<JobTreeItemWrapper> getEditedJobunitList( ){
		return editedJobunitList;
	}

	/**
	 *マネージャに登録する必要があるジョブユニットのリストからジョブユニットを削除します。
	 *
	 */
	public void removeEditedJobunit( JobTreeItemWrapper jobunit ){
		editedJobunitList.remove(jobunit);
	}

	/**
	 * マネージャから削除する必要があるジョブユニットを追加します
	 * @param jobunit 削除操作したジョブユニット
	 */
	public void addDeletedJobunit( JobTreeItemWrapper jobunit ){
		deletedJobunitList.add(jobunit);
	}

	/**
	 * マネージャから削除する必要があるジョブユニットのリストを返します。<BR>
	 *
	 * @return ArrayList<String>
	 */
	public ArrayList<JobTreeItemWrapper> getDeletedJobunitList( ){
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
	public List<JobInfoWrapper> getLockedJobunitList( ){
		List<JobInfoWrapper> list = new ArrayList<JobInfoWrapper>();
		for( JobInfoWrapper info : editSessionMap.keySet() ){
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
	public JobTreeItemWrapper getLockedJobunitBackup(JobInfoWrapper info ){
		return lockedJobunitMap.get(info);
	}

	/**
	 * ジョブユニットの編集ロック状態をクリアする
	 *
	 * @param info
	 */
	public void removeLockedJobunit(JobInfoWrapper info ){
		lockedJobunitMap.remove(info);
		editSessionMap.remove(info);
	}

	/**
	 * 編集ロックを取得したジョブユニットを追加する
	 * @param info ジョブユニット
	 * @param item ジョブツリーのバックアップ
	 * @param editSession セッション
	 */
	public void addLockedJobunit(JobInfoWrapper info, JobTreeItemWrapper item, Integer editSession ){
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
		for (JobInfoWrapper info : editSessionMap.keySet()) {
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
	public Integer getEditSession(JobInfoWrapper info ){
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
	public void exitEditMode(JobTreeItemWrapper item ){
		m_log.debug("exitEditMode(), editedJobunitList start. jobunitId=" + item.getData().getJobunitId());
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
		m_log.debug("putJobunitUpdateTime() :jobunitId="+ jobunitId + " updateTime =" +updateTime );
		jobunitUpdateTimeMap.put(jobunitId, updateTime);
	}

	/**
	 * 最終更新日時を保存するマップから指定したジョブユニットの情報を削除します
	 *
	 * @param jobunitId 情報を削除するジョブユニットID
	 */
	public void removeJobunitUpdateTime( String jobunitId ){
		m_log.debug("removeJobunitUpdateTime() :jobunitId="+ jobunitId );
		jobunitUpdateTimeMap.remove(jobunitId);
	}

	private void updateJobunitUpdateTime( JobTreeItemWrapper tree ){
		
		for( JobTreeItemWrapper jobunit : tree.getChildren().get(0).getChildren() ){
			if(jobunit.getData().getUpdateTime() != null){ 
				putJobunitUpdateTime(jobunit.getData().getJobunitId(),
						JobTreeItemUtil.convertDtStringtoLong(jobunit.getData().getUpdateTime()));
			}
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
	public JobTreeItemWrapper updateJobTree( String ownerRoleId, boolean m_treeOnly ) throws HinemosUnknown, InvalidRole, InvalidUserPass, JobMasterNotFound, NotifyNotFound, UserNotFound,RestConnectFailed{
		// managerがルートとなる木を作成する
		//     manager
		//     |
		//     -----jobunits

		//ジョブツリーのルート(Manager)を生成
		m_jobTreeItem = new JobTreeItemWrapper();
		JobInfoWrapper managerInfo =JobTreeItemUtil.createJobInfoWrapper();
		managerInfo.setId(managerName);
		managerInfo.setName(managerName);
		managerInfo.setType(JobInfoWrapper.TypeEnum.MANAGER);
		managerInfo.setJobunitId(managerName);
		m_jobTreeItem.setData(managerInfo);
		m_jobTreeItem.setParent(null);
		
		JobTreeItemWrapper orgTree =null;
		JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
		JobTreeItemResponseP1 orgTreeRes = wrapper.getJobTree(ownerRoleId);
		if( m_treeOnly ){
			//変換
			orgTree = JobTreeItemUtil.getItemFromP1(orgTreeRes);
		}else{
			//不要な情報を削り落として変換
			if(orgTreeRes.getData().getDescription() == null) {
				JobTreeItemResponseP2 dtoP2 = JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getJobTreeJobInfoFull(ownerRoleId);
				orgTree = JobTreeItemUtil.getItemFromP2ForTreeView(dtoP2);
			} else {
				orgTree = JobTreeItemUtil.getItemFromP1ForTreeView(orgTreeRes);
			}
		}

		updateJobunitUpdateTime(orgTree);
		// Children TOP->JOB->Jobunitの一覧を取得してセット
		for( JobTreeItemWrapper childItem : orgTree.getChildren().get(0).getChildren() ){
			childItem.setParent(m_jobTreeItem);
			m_jobTreeItem.getChildren().add(childItem);
		}
		
		return m_jobTreeItem;
	}
	
	/**
	 * ジョブツリーのキャッシュをそのまま返します
	 * @return
	 */
	public JobTreeItemWrapper getJobTree() {
		return m_jobTreeItem;
	}
	

	/**
	 * Clear job lock states
	 */
	public void releaseEditLock(){
		List<JobInfoWrapper> lockedJobunitList = getLockedJobunitList();
		if( lockedJobunitList.size() > 0 ){
			JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper( managerName );
			for( JobInfoWrapper jobunit : lockedJobunitList ){
				try{
					wrapper.releaseEditLock(jobunit.getJobunitId(),getEditSession(jobunit));
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
