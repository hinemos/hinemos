/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.session;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.util.RoleValidator;
import com.clustercontrol.bean.SnmpSecurityLevelConstant;
import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.commons.bean.SettingUpdateInfo;
import com.clustercontrol.commons.util.EmptyJpaTransactionCallback;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityDuplicate;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.fault.SnmpResponseError;
import com.clustercontrol.fault.UsedFacility;
import com.clustercontrol.hinemosagent.bean.TopicInfo;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.infra.session.InfraControllerBean;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.monitor.run.util.NodeMonitorPollerController;
import com.clustercontrol.monitor.run.util.NodeToMonitorCacheChangeCallback;
import com.clustercontrol.monitor.session.MonitorControllerBean;
import com.clustercontrol.nodemap.session.NodeMapControllerBean;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.repository.IRepositoryListener;
import com.clustercontrol.repository.bean.AgentStatusInfo;
import com.clustercontrol.repository.bean.FacilitySortOrderConstant;
import com.clustercontrol.repository.bean.FacilityTreeItem;
import com.clustercontrol.repository.bean.NodeInfoDeviceSearch;
import com.clustercontrol.repository.bean.RepositoryTableInfo;
import com.clustercontrol.repository.entity.CollectorPlatformMstData;
import com.clustercontrol.repository.entity.CollectorSubPlatformMstData;
import com.clustercontrol.repository.factory.AgentLibDownloader;
import com.clustercontrol.repository.factory.FacilityModifier;
import com.clustercontrol.repository.factory.FacilitySelector;
import com.clustercontrol.repository.factory.NodeProperty;
import com.clustercontrol.repository.factory.ScopeProperty;
import com.clustercontrol.repository.factory.SearchNodeBySNMP;
import com.clustercontrol.repository.model.CollectorPlatformMstEntity;
import com.clustercontrol.repository.model.CollectorSubPlatformMstEntity;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.ScopeInfo;
import com.clustercontrol.repository.util.FacilityIdCacheInitCallback;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.repository.util.FacilityTreeCacheRefreshCallback;
import com.clustercontrol.repository.util.JobCacheUpdateCallback;
import com.clustercontrol.repository.util.JobMultiplicityCacheKickCallback;
import com.clustercontrol.repository.util.NodeCacheRemoveCallback;
import com.clustercontrol.repository.util.NodeCacheUpdateCallback;
import com.clustercontrol.repository.util.QueryUtil;
import com.clustercontrol.repository.util.RepositoryChangedNotificationCallback;
import com.clustercontrol.repository.util.RepositoryListenerCallback;
import com.clustercontrol.repository.util.RepositoryListenerCallback.Type;
import com.clustercontrol.repository.util.RepositoryValidator;
import com.clustercontrol.util.MessageConstant;

/**
 *
 * リポジトリ情報（ノード、スコープ）の生成、変更、削除、
 * 参照を行うSessionBean<BR>
 * クライアントからの Entity Bean へのアクセスは、Session Bean を介して行います。
 *
 */
public class RepositoryControllerBean {

	private static Log m_log = LogFactory.getLog( RepositoryControllerBean.class );

	public static final int ALL = 0;
	public static final int ONE_LEVEL = 1;

	private static final List<IRepositoryListener> _listenerList = new ArrayList<IRepositoryListener>();

	public static final Long RESTART_AGENT_SLEEP_TIME = 500L;

	/**
	 * ファシリティIDを条件としてFacilityEntity を取得します。
	 *
	 * @param facilityId ファシリティID
	 * @return FacilityEntity
	 * @return HinemosUnknown
	 */
	public FacilityInfo getFacilityEntityByPK(String facilityId) throws FacilityNotFound, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		FacilityInfo facilityEntity = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			/** メイン処理 */
			facilityEntity = QueryUtil.getFacilityPK(facilityId);
			jtm.commit();
		} catch (FacilityNotFound | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.warn("getFacilityEntityByPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return facilityEntity;
	}

	/**
	 * ファシリティIDを条件としてNodeEntity を取得します。
	 *
	 * @param facilityId ファシリティID
	 * @return facilityEntity
	 * @return InvalidRole
	 * @return HinemosUnknown
	 */
	public NodeInfo getNodeEntityByPK(String facilityId) throws FacilityNotFound, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		NodeInfo nodeEntity = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			/** メイン処理 */
			nodeEntity = QueryUtil.getNodePK(facilityId);
			
			jtm.commit();
		} catch (FacilityNotFound e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("getNodeEntityByPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return nodeEntity;
	}

	/**
	 * プラットフォーム定義情報を新規に追加します。<BR>
	 *
	 * @param CollectorPlatformMstData 追加するプラットフォーム定義情報
	 * @throws EntityExistsException
	 * @throws HinemosUnknown
	 */
	public void addCollectorPratformMst(CollectorPlatformMstData data) throws EntityExistsException, HinemosUnknown {
		JpaTransactionManager jtm = null;
		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			FacilityModifier.addCollectorPratformMst(data);

			jtm.commit();
		} catch (EntityExistsException e) {
			if (jtm != null)
				jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("addCollectorPratformMst() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 * サブプラットフォーム定義情報を新規に追加します。<BR>
	 *
	 * @param CollectorSubPlatformMstData 追加するサブプラットフォーム定義情報
	 * @throws EntityExistsException
	 * @throws HinemosUnknown
	 */
	public void addCollectorSubPlatformMst(CollectorSubPlatformMstData data) throws EntityExistsException, HinemosUnknown {
		JpaTransactionManager jtm = null;
		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			FacilityModifier.addCollectorSubPratformMst(data);

			jtm.commit();
		} catch (EntityExistsException e) {
			if (jtm != null)
				jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("addCollectorSubPlatformMst() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 * 指定したプラットフォームIDに該当するプラットフォーム定義情報を削除します。<BR>
	 *
	 * @param platformId プラットフォームID
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 */
	public void deleteCollectorPratformMst(String platformId) throws FacilityNotFound, HinemosUnknown {
		JpaTransactionManager jtm = null;
		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			FacilityModifier.deleteCollectorPratformMst(platformId);

			jtm.commit();
		} catch (FacilityNotFound e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("deleteCollectorPratformMst() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 * 指定したサブプラットフォームIDに該当するサブプラットフォーム定義情報を削除します。<BR>
	 *
	 * @param subPlatformId サブプラットフォームID
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 */
	public void deleteCollectorSubPratformMst(String subPlatformId) throws FacilityNotFound, HinemosUnknown {
		JpaTransactionManager jtm = null;
		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			FacilityModifier.deleteCollectorSubPratformMst(subPlatformId);

			jtm.commit();
		} catch (FacilityNotFound e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("deleteCollectorSubPratformMst() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 * プラットフォームマスタの一覧を取得する。
	 *
	 * @return List<CollectorPlatformMstEntity>
	 */
	public List<CollectorPlatformMstEntity> getCollectorPlatformMstList() throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		List<CollectorPlatformMstEntity> ct = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			/** メイン処理 */
			ct = QueryUtil.getAllCollectorPlatformMst();
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getCollectorPlatformMstList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return ct;
	}

	/**
	 * サブプラットフォームマスタの一覧を取得する。
	 *
	 * @return List<CollectorSubPlatformMstEntity>
	 */
	public List<CollectorSubPlatformMstEntity> getCollectorSubPlatformMstList() throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		List<CollectorSubPlatformMstEntity> ct = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			/** メイン処理 */
			ct = QueryUtil.getAllCollectorSubPlatformMstEntity();
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("CollectorSubPlatformMstEntity() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return ct;
	}

	/**********************
	 * ファシリティツリーのメソッド群
	 **********************/

	/**
	 * ファシリティツリー（スコープツリー）取得を取得します。
	 * <BR>
	 * 取得したファシリティツリーには割り当てられたノードを含みます。<BR>
	 * このメソッドはクライアントの画面情報を作成するために
	 * 呼び出されます。クライアントのロケールを引数をして必要とします。<BR>
	 * （最上位のスコープという表記をscopeをいう表記を切り替えるため。）
	 *
	 * @version 1.0.0
	 * @since 1.0.0
	 *
	 * @param ownerRoleId オーナーロールID
	 * @param locale クライアントのロケール
	 * @return FacilityTreeItemの階層オブジェクト
	 * @throws HinemosUnknown
	 */
	public FacilityTreeItem getFacilityTree(String ownerRoleId, Locale locale) throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		FacilityTreeItem treeItem = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			/** メイン処理 */
			treeItem = FacilitySelector.getFacilityTree(locale, false, null, ownerRoleId);
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getFacilityTree() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return treeItem;
	}

	/**
	 * ファシリティツリー（スコープツリー）取得を取得します。(有効なノードのみ)
	 * <BR>
	 * 取得したファシリティツリーには割り当てられたノードを含みます。<BR>
	 * このメソッドはクライアントの画面情報を作成するために
	 * 呼び出されます。クライアントのロケールを引数をして必要とします。<BR>
	 * （最上位のスコープという表記をscopeをいう表記を切り替えるため。）
	 *
	 * @version 4.0.0
	 * @since 4.0.0
	 *
	 * @param facilityId ファシリティID
	 * @param ownerRoleId オーナーロールID
	 * @param locale クライアントのロケール
	 * @return FacilityTreeItemの階層オブジェクト
	 * @throws HinemosUnknown
	 */
	public FacilityTreeItem getExecTargetFacilityTree(String facilityId, String ownerRoleId, Locale locale) throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		FacilityTreeItem treeItem = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			/** メイン処理 */
			treeItem = FacilitySelector.getFacilityTree(facilityId, locale, false, Boolean.TRUE, ownerRoleId);
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getExecTargetFacilityTree() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return treeItem;
	}

	/**
	 * ファシリティツリー（ノードツリー）取得を取得します。
	 * <BR>
	 * 取得したファシリティツリーには参照可能なノードが割り当てられています。<BR>
	 * このメソッドはクライアントの画面情報を作成するために
	 * 呼び出されます。クライアントのロケールを引数をして必要とします。<BR>
	 * （最上位のスコープという表記をscopeをいう表記を切り替えるため。）
	 *
	 * @param locale クライアントのロケール
	 * @param ownerRoleId オーナーロールID
	 * @return FacilityTreeItemの階層オブジェクト
	 * @throws HinemosUnknown
	 */
	public FacilityTreeItem getNodeFacilityTree(Locale locale, String ownerRoleId) throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		FacilityTreeItem treeItem = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			/** メイン処理 */
			treeItem = FacilitySelector.getNodeFacilityTree(locale, ownerRoleId);
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getNodeFacilityTree() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return treeItem;
	}

	/**********************
	 * ノードのメソッド群(getter)
	 **********************/
	/**
	 * ノード一覧を取得します。<BR>
	 * リポジトリに登録されているすべてのノードを取得します。<BR>
	 * 戻り値はNodeInfoのArrayListで、NodeInfoには
	 * ノードの下記情報のみ格納されています。
	 * ・ファシリティID
	 * ・ファシリティ名
	 * ・IPアドレスバージョン、IPv4, Ipv6
	 * ・説明
	 * getNodeFacilityIdListを利用すること。（getNodeと組み合わせて利用する。）
	 *
	 * @version 1.0.0
	 * @since 1.0.0
	 *
	 * @return NodeInfoの配列
	 * @throws HinemosUnknown
	 */
	public ArrayList<NodeInfo> getNodeList() throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		ArrayList<NodeInfo> list = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			/** メイン処理 */
			list = FacilitySelector.getNodeList();
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getExecTargetFacilityTree() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}

	/**
	 * 詳細版ノード一覧を取得します。<BR>
	 *
	 * @version 4.0.0
	 * @since 4.0.0
	 *
	 * @return NodeInfoの配列
	 * @throws HinemosUnknown
	 */
	public List<NodeInfo> getNodeDetailList() throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		List<NodeInfo> list = new ArrayList<NodeInfo>();
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			for (String facilityId : FacilitySelector.getFacilityIdList("REGISTERED", null, 0, false, false)) {
				NodeInfo nodeInfo = NodeProperty.getProperty(facilityId);
				list.add(nodeInfo);
			}

			jtm.commit();
		} catch (FacilityNotFound e) {
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getNodeDetailList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}

	/**
	 * ノード一覧を取得します。<BR>
	 *
	 * クライアントなどで検索した場合に呼ばれ、該当するノード一覧を取得します。<BR>
	 * 引数はNodeInfoであり、"ファシリティID"、"ファシリティ名"、"説明"、
	 * "IPアドレス"、"OS名"、"OSリリース"、"管理者"、"連絡先"が１つ以上含まれており、
	 * その条件を元に該当するノードを戻り値とします。<BR>
	 * 戻り値はNodeInfoのArrayListで、NodeInfoには
	 * ノードの"ファシリティID"、"ファシリティ名"、"説明"のみ格納されています。<BR>
	 *
	 * @version 1.0.0
	 * @since 1.0.0
	 *
	 * @param property　検索条件のプロパティ
	 * @return NodeInfoの配列
	 * @throws HinemosUnknown
	 */
	public ArrayList<NodeInfo> getFilterNodeList(NodeInfo property) throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		ArrayList<NodeInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			/** メイン処理 */
			list = FacilitySelector.getFilterNodeList(property);

			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getFilterNodeList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}

	/**
	 *
	 * 監視・ジョブ等の処理を実行する対象となる、ファシリティIDのリストを取得します。
	 * 引数で指定されたファシリティIDが、ノードかスコープによって、以下のようなリストを取得します。
	 *
	 * ノードの場合
	 *   引数で指定されたfacilityIdが格納されたArrayList
	 *   ただし、管理対象（有効/無効フラグが真）の場合のみ
	 *
	 * スコープの場合
	 *   配下に含まれるノードのファシリティIDが格納されたArrayList
	 *   ただし、管理対象（有効/無効フラグが真）のみ
	 *
	 *
	 * @version 3.0.0
	 * @since 3.0.0
	 *
	 *
	 * @param facilityId 処理を実行する対象となるファシリティID
	 * @param ownerRoleId 処理対象のオーナーロールID
	 * @return 有効なノードのリスト（有効なノードがひとつも含まれない場合は空のリスト）
	 * @throws HinemosUnknown
	 */
	public ArrayList<String> getExecTargetFacilityIdList(String facilityId, String ownerRoleId) throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		ArrayList<String> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			/** メイン処理 */
			list = FacilitySelector.getNodeFacilityIdList(facilityId, ownerRoleId, RepositoryControllerBean.ALL, false, true);
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getExecTargetFacilityIdList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}

	/**
	 * ノードのファシリティIDリストを取得します。<BR>
	 *
	 * リポジトリにあるすべてのノードのリストを取得します。<BR>
	 * 戻り値は ファシリティID(String)のArrayList<BR>
	 *
	 * getNodeList() との違いはこちらの戻り値はArrayListの２次元ではなく、
	 * 単純にファシリティID（String）のみのArrayList
	 *
	 * @version 1.0.0
	 * @since 1.0.0
	 *
	 * @return ファシリティIDの配列
	 * @throws HinemosUnknown
	 */
	public ArrayList<String> getNodeFacilityIdList() throws HinemosUnknown {
		return getNodeFacilityIdList(false);
	}

	/**
	 * ノードのファシリティIDリストを取得します。<BR>
	 *
	 * リポジトリにあるすべてのノードのリストを取得します。<BR>
	 * 戻り値は ファシリティID(String)のArrayList
	 * 引数のsortにtrueをセットした場合には、listがCollator.compare()にしたがってソートされる。<BR>
	 *
	 * getNodeList() との違いはこちらの戻り値はNodeInfoのArrayListではなく、
	 * 単純にファシリティID（String）のみのArrayList
	 *
	 * @version 2.1.0
	 * @since 2.1.0
	 *
	 * @param sort sort ソートするか？(する:true しない:false)
	 * @return ファシリティIDの配列
	 * @throws HinemosUnknown
	 */
	public ArrayList<String> getNodeFacilityIdList(boolean sort) throws HinemosUnknown{
		JpaTransactionManager jtm = null;
		ArrayList<String> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			/** メイン処理 */
			list = FacilitySelector.getNodeFacilityIdList(sort);
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getNodeFacilityIdList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}

	/**
	 * ノードの詳細プロパティを取得します。<BR>
	 *
	 * faciliyIDで指定されるノードの詳細プロパティを取得します。<BR>
	 *
	 * @version 1.0.0
	 * @since 1.0.0
	 *
	 * @param facilityId ファシリティID
	 * @param locale クライアントのロケール
	 * @return ノード情報プロパティ
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 */
	public NodeInfo getNode(String facilityId) throws FacilityNotFound, HinemosUnknown {
		JpaTransactionManager jtm = null;
		NodeInfo nodeInfo = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			/** メイン処理 */
			nodeInfo = NodeProperty.getProperty(facilityId);
			jtm.commit();
		} catch (FacilityNotFound e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("getNode() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return nodeInfo;
	}


	/**
	 * ファシリティパスを取得します。<BR>
	 *
	 * 第一引数がノードの場合は、パスではなく、ファシリティ名。<BR>
	 * 第一引数がスコープの場合は、第二引数との相対的なファシリティパスを取得します。<BR>
	 * (例　○○スコープ>××システム>DBサーバ)<BR>
	 *
	 * @version 1.0.0
	 * @since 1.0.0
	 *
	 * @param facilityId ファシリティID
	 * @param parentFacilityId 上位のファシリティID
	 * @return String ファシリティパス
	 * @throws HinemosUnknown
	 */
	public String getFacilityPath(String facilityId, String parentFacilityId) throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		String facilityPath = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			/** メイン処理 */
			facilityPath = FacilitySelector.getNodeScopePath(parentFacilityId, facilityId);
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getFacilityPath() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return facilityPath;
	}


	/**
	 * SNMPを利用してノードの情報を取得します。<BR>
	 *
	 *
	 * クライアントからSNMPで検出を行った際に呼び出されるメソッドです。<BR>
	 * SNMPポーリングにより、ノード詳細プロパティをセットし、クライアントに返す。
	 * 戻り値はNodeInfo
	 *
	 * @version 2.1.2
	 * @since 2.1.2
	 *
	 * @param ポーリング対象のIPアドレス、コミュニティ名、バージョン、ポート、ファシリティID、セキュリティレベル、ユーザー名、認証パスワード、暗号化パスワード、認証プロトコル、暗号化プロトコル
	 * @param locale クライアントのロケール
	 * @return ノード情報（更新情報）
	 * @throws HinemosUnknown
	 */
	public NodeInfoDeviceSearch getNodePropertyBySNMP(String ipAddress,
			int port, String community, int version, String facilityID,
			String securityLevel, String user, String authPass,
			String privPass, String authProtocol, String privProtocol)
			throws HinemosUnknown, SnmpResponseError {
		JpaTransactionManager jtm = null;

		/** メイン処理 */
		NodeInfo property = null;
		NodeInfo lastNode = null;
		NodeInfoDeviceSearch snmpInfo = new NodeInfoDeviceSearch();

		if (version == SnmpVersionConstant.TYPE_V3) {
			snmpv3Check(securityLevel, user, authPass, privPass);
		}
		
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			property = SearchNodeBySNMP.searchNode(ipAddress, port, community,
					version, facilityID, securityLevel, user, authPass,
					privPass, authProtocol, privProtocol);
			snmpInfo.setNodeInfo(property);

			boolean isUpdated = false;
			if (facilityID != null) {
				//前回取得時のSNMP情報を取得する
				lastNode = new RepositoryControllerBean().getNode(facilityID);

				//前回情報と比較
				isUpdated = !snmpInfo.equalsNodeInfo(lastNode);
			}
			m_log.debug("isUpdated:" + isUpdated);

			jtm.commit();
		} catch (SnmpResponseError e) {
			jtm.rollback();
			throw e;
		} catch (UnknownHostException e) {
			m_log.info("getNodePropertyBySNMP() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getNodePropertyBySNMP() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return snmpInfo;
	}

	/**
	 * 条件のHashMapに該当するノードのファシリティIDのリストを返却する。<BR>
	 * このメソッドは性能が低いため、要注意。
	 *
	 * @version 3.1.0
	 * @since 3.1.0
	 *
	 * @return ArrayList<String>
	 * @throws HinemosUnknown
	 */
	@Deprecated
	public ArrayList<String> findByCondition(HashMap<String, String> condition) throws  HinemosUnknown {
		JpaTransactionManager jtm = null;
		ArrayList<String> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			/** メイン処理 */
			list = FacilitySelector.getNodeFacilityIdListByCondition(condition);
			jtm.commit();
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("findByCondition() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}

	/**
	 * IPアドレスから該当するノードのファシリティID一覧を取得する。
	 *
	 * @version 4.0.0
	 * @since 4.0.0
	 *
	 * @param ipaddr IPアドレス(Inet4Address or Inet6Address)
	 * @return ファシリティIDのリスト
	 * @throws HinemosUnknown 予期せぬ内部エラーが発生した場合
	 */
	public List<String> getFacilityIdByIpAddress(InetAddress ipaddr) throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		List<String> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			list = FacilitySelector.getFacilityIdByIpAddress(ipaddr);
			jtm.commit();
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("getFacilityIdByIpAddress() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}

	/**
	 * ファシリティIDリストを取得します。<BR>
	 *
	 *  引数のホスト名（ノード名）またはIPアドレスに対応するノードのファシリティIDのリストを
	 *  取得します。<BR>
	 *  戻り値はファシリティID(String)のArrayList
	 * getNodeList(NodeInfo)を利用すること。
	 *
	 * @version 1.0.0
	 * @since 1.0.0
	 *
	 * @param hostName ホスト名（ノード名）
	 * @param ipAddress　IPアドレス(v4)
	 * @return ファシリティIDの配列
	 * @throws HinemosUnknown
	 */
	public ArrayList<String> getFacilityIdList(String hostName, String ipAddress) throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		ArrayList<String> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			/** メイン処理 */
			list = FacilitySelector.getFacilityIdList(hostName, ipAddress);
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getFacilityIdList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}

	/**********************
	 * ノードのメソッド群(getter以外)
	 **********************/

	/**
	 * ノードを新規に追加します。<BR>
	 * またこのメソッドで組み込みスコープである"登録済みノード"スコープにも
	 * 割り当てが行われます。
	 *
	 * @version 3.1.0
	 * @since 1.0.0
	 *
	 * @param nodeinfo 追加するノード情報のプロパティ
	 * @throws FacilityDuplicate
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	public void addNode(NodeInfo nodeInfo) throws FacilityDuplicate, InvalidSetting, HinemosUnknown {
		addNode(nodeInfo, true);
	}

	/**
	 * ノードを新規に追加します。（リポジトリ更新TOPIC未送信選択可能）<BR>
	 * またこのメソッドで組み込みスコープである"登録済みノード"スコープにも
	 * 割り当てが行われます。
	 *
	 * @version 3.1.0
	 * @since 1.0.0
	 *
	 * @param nodeInfo 追加するノード情報のプロパティ
	 * @throws FacilityDuplicate
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	public void addNode(final NodeInfo nodeInfo, boolean topicSendFlg) throws FacilityDuplicate, InvalidSetting, HinemosUnknown {
		 JpaTransactionManager jtm = new JpaTransactionManager();

		try {
			jtm.begin();

			// メンバ変数にnullが含まれていることがあるので、nullをデフォルト値に変更する。
			nodeInfo.setDefaultInfo();

			// 入力チェック
			RepositoryValidator.validateNodeInfo(nodeInfo);
			
			//ユーザがオーナーロールIDに所属しているかチェック
			RoleValidator.validateUserBelongRole(nodeInfo.getOwnerRoleId(),
					(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID),
					(Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR));

			FacilityModifier.addNode(
					nodeInfo,
					(String) HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID),
					FacilitySortOrderConstant.DEFAULT_SORT_ORDER_NODE);

			jtm.addCallback(new NodeCacheUpdateCallback(nodeInfo.getFacilityId()));
			jtm.addCallback(new FacilityIdCacheInitCallback());
			jtm.addCallback(new FacilityTreeCacheRefreshCallback());
			jtm.addCallback(new RepositoryChangedNotificationCallback());
			jtm.addCallback(new NodeToMonitorCacheChangeCallback());
			jtm.addCallback(new EmptyJpaTransactionCallback() {
				@Override
				public void postCommit() {
					NodeMonitorPollerController.registNodeMonitorPoller(nodeInfo);
				}
			});

			try {
				ListenerReadWriteLock.readLock();
				for (IRepositoryListener listener : _listenerList) {
					jtm.addCallback(new RepositoryListenerCallback(listener, Type.ADD_NODE, null, nodeInfo.getFacilityId()));
				}
			} finally {
				ListenerReadWriteLock.readUnlock();
			}

			jtm.commit();
		} catch (EntityExistsException e) {
			jtm.rollback();
			throw new FacilityDuplicate(e.getMessage(), e);
		} catch (InvalidSetting e) {
			jtm.rollback();
			throw e;
		} catch (FacilityNotFound e) {
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("addNode() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
	}

	/**
	 * ノードを変更します。<BR>
	 * 引数のpropertyには変更する属性のみを設定してください。<BR>
	 *
	 * @version 2.0.0
	 * @since 1.0.0
	 *
	 * @param info　変更するノード情報のプロパティ
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void modifyNode(NodeInfo info) throws InvalidSetting, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			// メンバ変数にnullが含まれていることがあるので、nullをデフォルト値に変更する。
			info.setDefaultInfo();

			// 入力チェック
			RepositoryValidator.validateNodeInfo(info);

			/** メイン処理 */
			FacilityModifier.modifyNode(info, (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID), true);

			jtm.addCallback(new NodeCacheUpdateCallback(info.getFacilityId()));
			jtm.addCallback(new JobMultiplicityCacheKickCallback(info.getFacilityId()));
			jtm.addCallback(new FacilityIdCacheInitCallback());
			jtm.addCallback(new FacilityTreeCacheRefreshCallback());
			jtm.addCallback(new RepositoryChangedNotificationCallback());
			jtm.addCallback(new JobCacheUpdateCallback());  // FacilityTreeCacheの更新より後に呼び出す必要がある
			
			// 変更前後で管理対象フラグの有無が異なる場合、ノードに対して実行すべき監視の情報を持つキャッシュを更新する
			if (info.getValid().booleanValue() != this.getNode(info.getFacilityId()).getValid().booleanValue()) {
				jtm.addCallback(new NodeToMonitorCacheChangeCallback());
			}

			try {
				ListenerReadWriteLock.readLock();
				for (IRepositoryListener listener : _listenerList) {
					jtm.addCallback(new RepositoryListenerCallback(listener, Type.CHANGE_NODE, null, info.getFacilityId()));
				}
			} finally {
				ListenerReadWriteLock.readUnlock();
			}

			jtm.commit();
		} catch (InvalidSetting | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (FacilityNotFound e) {
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("modifyNode() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}


	/**
	 * ノード情報を削除します。<BR>
	 *
	 * faciityIDで指定されたノードをリポジトリから削除します。
	 *
	 * @version 1.0.0
	 * @since 1.0.0
	 *
	 * @param facilityIds ファシリティIDの配列
	 * @throws UsedFacility
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void deleteNode(String[] facilityIds) throws UsedFacility, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		/** メイン処理 */
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			for (String facilityId : facilityIds) {
				checkIsUseFacility(facilityId);
				FacilityModifier.deleteNode(facilityId, (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID), true);
			}

			for (final String facilityId : facilityIds) {
				jtm.addCallback(new NodeCacheRemoveCallback(facilityId));
				jtm.addCallback(new EmptyJpaTransactionCallback() {
					@Override
					public void postCommit() {
						NodeMonitorPollerController.unregistNodeMonitorPoller(facilityId);
					}
				});
			}
			jtm.addCallback(new FacilityIdCacheInitCallback());
			jtm.addCallback(new FacilityTreeCacheRefreshCallback());
			jtm.addCallback(new RepositoryChangedNotificationCallback());
			jtm.addCallback(new NodeToMonitorCacheChangeCallback());

			try {
				ListenerReadWriteLock.readLock();
				for (IRepositoryListener listener : _listenerList) {
					for (String facilityId : facilityIds) {
						jtm.addCallback(new RepositoryListenerCallback(listener, Type.REMOVE_NODE, null, facilityId));
					}
				}
			} finally {
				ListenerReadWriteLock.readUnlock();
			}

			// ノードマップ
			// ノードマップで対象スコープの対象ファシリティにつながっているパスを消す
			new NodeMapControllerBean().deleteMapInfo(Arrays.asList(facilityIds), null);
			
			jtm.commit();
		} catch (UsedFacility | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (FacilityNotFound e) {
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("deleteNode() : "
					+ e.getClass().getSimpleName() +", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}




	/**********************
	 * スコープのメソッド群
	 **********************/
	/**
	 * ファシリティID一覧を取得します。<BR>
	 * あるスコープを指定してその直下にあるスコープとノードを取得します。<BR>
	 * このメソッドは引数としてそのスコープのファシリティIDを要求します。<BR>
	 * 戻り値はArrayListで中のFacilityInfoには子の
	 * "ファシリティID"、"ファシリティ名"、"説明"のみ格納されています。<BR>
	 *
	 * @version 1.0.0
	 * @since 1.0.0
	 *
	 * @param parentFacilityId ファシリティID
	 * @return ScopeInfoの配列
	 * @throws HinemosUnknown
	 */
	public ArrayList<FacilityInfo> getFacilityList(String parentFacilityId) throws HinemosUnknown {

		JpaTransactionManager jtm = null;
		ArrayList<FacilityInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			/** メイン処理 */
			list = FacilitySelector.getFacilityListAssignedScope(parentFacilityId);
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getFacilityList() : "
					+ e.getClass().getSimpleName() +", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}

	/**
	 * スコープ用プロパティ情報を取得します。<BR>
	 *
	 * @version 1.0.0
	 * @since 1.0.0
	 *
	 * @param facilityId ファシリティID
	 * @param locale クライアントのロケール
	 * @return スコープのプロパティ情報（ファシリティID、ファシリティ名、説明）
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public ScopeInfo getScope(String facilityId) throws FacilityNotFound, HinemosUnknown, InvalidRole {

		JpaTransactionManager jtm = null;

		/** メイン処理 */
		ScopeInfo property = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 作成時
			if(facilityId == null) {
				property = new ScopeInfo();
			}
			// 変更時
			else {
				property = ScopeProperty.getProperty_NONE(facilityId);
				//ファシリティIDが参照可能かチェックする
				FacilityTreeCache.getFacilityInfo(facilityId);
			}
			jtm.commit();
		} catch (FacilityNotFound | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(),e);
		} catch (Exception e) {
			m_log.warn("getScope() : "
					+ e.getClass().getSimpleName() +", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return property;
	}

	/**
	 * スコープを新規に追加します。<BR>
	 *
	 * parentFacilityIdで指定されるスコープの下にpropertyで指定されるスコープを
	 * 追加します。<BR>
	 * 引数propertyには、"ファシリティID"、"ファシリティ名"、"説明"（任意）を含める必要があります。
	 *
	 * @version 3.1.0
	 * @since 1.0.0
	 *
	 * @param parentFacilityId
	 * @param property
	 * @throws FacilityDuplicate
	 * @throws InvalidSetting
	 * @thorws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void addScope(String parentFacilityId, ScopeInfo property)
			throws FacilityDuplicate, InvalidSetting, InvalidRole, HinemosUnknown {
		addScope(parentFacilityId, property, FacilitySortOrderConstant.DEFAULT_SORT_ORDER_SCOPE);
	}

	/**
	 * スコープを新規に追加します(表示順指定)。<BR>
	 *
	 * parentFacilityIdで指定されるスコープの下にpropertyで指定されるスコープを
	 * 追加します。<BR>
	 * 引数propertyには、"ファシリティID"、"ファシリティ名"、"説明"（任意）を含める必要があります。
	 *
	 * @version 3.1.0
	 * @since 1.0.0
	 *
	 * @param parentFacilityId
	 * @param property
	 * @param sortOrder
	 * @throws FacilityDuplicate
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void addScope(String parentFacilityId, ScopeInfo property, int displaySortOrder)
			throws FacilityDuplicate, InvalidSetting, InvalidRole, HinemosUnknown {
		addScope(parentFacilityId, property, displaySortOrder, true);
	}

	/**
	 * スコープを新規に追加します(表示順指定、リポジトリ更新TOPIC未送信選択可能)。<BR>
	 *
	 * parentFacilityIdで指定されるスコープの下にpropertyで指定されるスコープを
	 * 追加します。<BR>
	 * 引数propertyには、"ファシリティID"、"ファシリティ名"、"説明"（任意）を含める必要があります。
	 *
	 * @version 3.1.0
	 * @since 1.0.0
	 *
	 * @param parentFacilityId
	 * @param info
	 * @param sortOrder
	 * @throws FacilityDuplicate
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	public void addScope(String parentFacilityId, ScopeInfo info, int displaySortOrder, boolean topicSendFlg)
			throws FacilityDuplicate, InvalidSetting, InvalidRole, HinemosUnknown {

		JpaTransactionManager jtm = null;

		/** メイン処理 */
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			//入力チェック
			RepositoryValidator.validateScopeInfo(parentFacilityId, info, true);
			
			//ユーザがオーナーロールIDに所属しているかチェック
			RoleValidator.validateUserBelongRole(info.getOwnerRoleId(),
					(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID),
					(Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR));

			FacilityModifier.addScope(
					parentFacilityId,
					info,
					(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID),
					displaySortOrder,
					topicSendFlg);

			jtm.addCallback(new FacilityIdCacheInitCallback());
			jtm.addCallback(new FacilityTreeCacheRefreshCallback());
			jtm.addCallback(new RepositoryChangedNotificationCallback());

			try {
				ListenerReadWriteLock.readLock();
				for (IRepositoryListener listener : _listenerList) {
					jtm.addCallback(new RepositoryListenerCallback(listener, Type.ADD_SCOPE, info.getFacilityId(), null));
				}
			} finally {
				ListenerReadWriteLock.readUnlock();
			}

			jtm.commit();
		} catch (InvalidSetting | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (EntityExistsException e) {
			if (jtm != null)
				jtm.rollback();
			throw new FacilityDuplicate(e.getMessage(), e);
		} catch (FacilityNotFound e) {
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("addScope() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 * スコープの情報を変更します。<BR>
	 *
	 * 引数propertyで指定した内容でスコープ情報を更新します。<BR>
	 * 引数propertyには、"ファシリティID"、"ファシリティ名"、"説明"（任意）を含める必要があります。
	 * propertyに含まれるファシリティIDに対応するスコープの情報が変更されます。<BR>
	 *
	 * @version 1.0.0
	 * @since 1.0.0
	 *
	 * @param info
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void modifyScope(ScopeInfo info) throws InvalidSetting, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			//入力チェック
			RepositoryValidator.validateScopeInfo(null, info, false);

			//組み込みスコープであるかチェック
			checkIsBuildInScope(info.getFacilityId());

			/** メイン処理 */
			FacilityModifier.modifyScope(info, (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID), true);

			jtm.addCallback(new FacilityIdCacheInitCallback());
			jtm.addCallback(new FacilityTreeCacheRefreshCallback());
			jtm.addCallback(new RepositoryChangedNotificationCallback());
			jtm.addCallback(new JobCacheUpdateCallback());  // FacilityTreeCacheの更新より後に呼び出す必要がある
			
			try {
				ListenerReadWriteLock.readLock();
				for (IRepositoryListener listener : _listenerList) {
					jtm.addCallback(new RepositoryListenerCallback(listener, Type.CHANGE_SCOPE, info.getFacilityId(), null));
				}
			} finally {
				ListenerReadWriteLock.readUnlock();
			}

			jtm.commit();
		} catch (InvalidSetting | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (FacilityNotFound e) {
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("modifyScope() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 * スコープ情報を削除します。<BR>
	 *
	 * faciityIDで指定されたスコープをリポジトリから削除します。
	 *
	 * @version 1.0.0
	 * @since 1.0.0
	 *
	 * @param facilityIds ファシリティID
	 * @throws UsedFacility
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void deleteScope(String[] facilityIds) throws UsedFacility, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			/** メイン処理 */
			for (String facilityId : facilityIds) {
				checkIsBuildInScope(facilityId);
				checkIsUseFacility(facilityId);
				FacilityModifier.deleteScope(facilityId, (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID), true);

				// ノードマップ
				// ノードマップで対象スコープの対象ファシリティにつながっているパスを消す
				new NodeMapControllerBean().deleteMapInfo(null, facilityId);
			}

			jtm.addCallback(new FacilityIdCacheInitCallback());
			jtm.addCallback(new FacilityTreeCacheRefreshCallback());
			jtm.addCallback(new RepositoryChangedNotificationCallback());
			jtm.addCallback(new NodeToMonitorCacheChangeCallback());

			try {
				ListenerReadWriteLock.readLock();
				for (IRepositoryListener listener : _listenerList) {
					for (String facilityId : facilityIds) {
						jtm.addCallback(new RepositoryListenerCallback(listener, Type.REMOVE_SCOPE, facilityId, null));
					}
				}
			} finally {
				ListenerReadWriteLock.readUnlock();
			}
			
			jtm.commit();
		} catch (UsedFacility | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (FacilityNotFound e) {
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("deleteScope() : "
					+ e.getClass().getSimpleName() +", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}



	/**********************
	 * ノード割り当てのメソッド群
	 **********************/
	/**
	 * 割当ノード一覧を取得します。<BR>
	 *
	 * あるファシリティIDの配下または直下のノード一覧を取得します。<BR>
	 * このメソッドでは、引数levelで直下または配下を制御します。<BR>
	 * 戻り値はNodeInfoのArrayListで、NodeInfoには
	 * ノードの"ファシリティID"、"ファシリティ名"、"説明"のみ格納されています。<BR>
	 *
	 *
	 * @version 1.0.0
	 * @since 1.0.0
	 *
	 * @param parentFacilityId
	 * @param level　取得レベル 0:ALL(配下) 1:ONE_LEVEL（直下）
	 * @return NodeInfoの配列
	 * @throws HinemosUnknown
	 */
	public ArrayList<NodeInfo> getNodeList(String parentFacilityId, int level) throws HinemosUnknown {

		JpaTransactionManager jtm = null;
		ArrayList<NodeInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			/** メイン処理 */
			list = FacilitySelector.getNodeList(parentFacilityId, null, level);
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getNodeList() : "
					+ e.getClass().getSimpleName() +", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}


	/**
	 * 割当スコープ一覧を取得します。<BR>
	 * 割り当てスコープ一覧とは、あるノードが属しているスコープすべてを
	 * 一覧表示したものです。
	 * クライアントの割り当てスコープビューの表示データとなります。
	 * 戻り値はArrayListのArrayListで中のArrayListには"スコープ"が最上位からの
	 * スコープパス表記で（Stringで）格納されています。
	 * 外のArrayListには、そのレコードが順に格納されています。
	 *
	 *
	 * @version 1.0.0
	 * @since 1.0.0
	 *
	 * @param facilityId ノードのファシリティID
	 * @return Stringの配列
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public ArrayList<String> getNodeScopeList(String facilityId) throws InvalidRole, HinemosUnknown {

		JpaTransactionManager jtm = null;
		ArrayList<String> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			/** メイン処理 */
			list = FacilitySelector.getNodeScopeList(facilityId);
			jtm.commit();
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (FacilityNotFound e) {
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getNodeScopeList() : "
					+ e.getClass().getSimpleName() +", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}

	/**
	 * ファシリティIDリストを取得します。<BR>
	 *
	 * 引数に指定した親ファシリティIDの配下または直下のファシリティ（スコープ、ノード）の
	 * リストを取得します。<BR>
	 * 結果のリストに親ファシリティのID自身も含まれます<BR>
	 * 戻り値は ファシリティID（String）のArrayList
	 *
	 * @version 2.1.0
	 * @since 1.0.0
	 *
	 * @param parentFacilityId ファシリティID
	 * @param level　取得レベル 0:ALL(配下) 1:ONE_LEVEL（直下）
	 * @return ファシリティIDの配列
	 * @throws HinemosUnknown
	 */
	public ArrayList<String> getFacilityIdList(String parentFacilityId, int level) throws HinemosUnknown {
		/** メイン処理 */
		return getFacilityIdList(parentFacilityId, level, true);
	}

	/**
	 * ファシリティIDリストを取得します。<BR>
	 *
	 * 引数に指定した親ファシリティIDの配下または直下のファシリティ（スコープ、ノード）の
	 * リストを取得します。<BR>
	 * 戻り値は ファシリティID（String）のArrayList
	 *
	 * @version 2.1.0
	 * @since 1.0.0
	 *
	 * @param parentFacilityId ファシリティID
	 * @param level　取得レベル 0:ALL(配下) 1:ONE_LEVEL（直下）
	 * @param scopeFlag スコープ自身を含めるか（含める:true 含めない:false)
	 * @return ファシリティIDの配列
	 * @throws HinemosUnknown
	 */
	public ArrayList<String> getFacilityIdList(String parentFacilityId, int level, boolean scopeFlag) throws HinemosUnknown {
		/** メイン処理 */
		return getFacilityIdList(parentFacilityId, level, false, scopeFlag);
	}

	/**
	 * ファシリティIDリストを取得します。<BR>
	 *
	 * 引数に指定した親ファシリティIDの配下または直下のファシリティ（スコープ、ノード）の
	 * リストを取得します。<BR>
	 * 戻り値は ファシリティID（String）のArrayList
	 * 引数のsortにtrueをセットした場合には、listがCollator.compare()にしたがってソートされる。
	 *
	 *
	 * @version 2.1.0
	 * @since 2.1.0
	 *
	 * @param parentFacilityId ファシリティID
	 * @param level 取得レベル 0:ALL(配下) 1:ONE_LEVEL（直下）
	 * @param sort ソートするか？(する:true しない:false)
	 * @param scopeFlag スコープ自身を含めるか（含める:true 含めない:false)
	 * @return ファシリティIDの配列
	 * @throws HinemosUnknown
	 */
	public ArrayList<String> getFacilityIdList(String parentFacilityId, int level, boolean sort, boolean scopeFlag) throws HinemosUnknown {

		JpaTransactionManager jtm = null;
		ArrayList<String> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			/** メイン処理 */
			list = FacilitySelector.getFacilityIdList(parentFacilityId, null, level, sort, scopeFlag);
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getFacilityIdList() : "
					+ e.getClass().getSimpleName() +", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}

	/**
	 * ノードのファシリティIDリストを取得します。<BR>
	 *
	 * 引数に指定した親ファシリティIDの配下または直下のファシリティ（ノード）の
	 * リストを取得します。<BR>
	 * 戻り値は ファシリティID（String）のArrayList
	 *
	 * @version 2.1.0
	 * @since 1.0.0
	 *
	 * @param parentFacilityId ファシリティID
	 * @param ownerRoleId オーナーロールID
	 * @param level  取得レベル 0:ALL(配下) 1:ONE_LEVEL（直下）
	 * @return ファシリティIDの配列
	 * @throws HinemosUnknown
	 */
	public ArrayList<String> getNodeFacilityIdList(String parentFacilityId, String ownerRoleId, int level) throws HinemosUnknown {
		/** メイン処理 */
		return getNodeFacilityIdList(parentFacilityId, ownerRoleId, level, false, true);
	}

	/**
	 * ノードのファシリティIDリスト取得<BR>
	 * 引数に指定した親ファシリティIDの配下または直下のファシリティ（ノード）の
	 * リストを取得します。<BR>
	 * 戻り値は ファシリティID（String）のArrayList
	 * 引数のsortにtrueをセットした場合には、listがCollator.compare()にしたがってソートされる。
	 *
	 * @version 2.1.0
	 * @since 2.1.0
	 *
	 * @param parentFacilityId
	 * @param ownerRoleId
	 * @param level   取得レベル 0:ALL(配下) 1:ONE_LEVEL（直下）
	 * @param sort sort ソートするか？(する:true しない:false)
	 * @return ファシリティIDの配列
	 * @throws HinemosUnknown
	 */
	public ArrayList<String> getNodeFacilityIdList(String parentFacilityId, String ownerRoleId, int level, boolean sort, Boolean valid) throws HinemosUnknown {

		JpaTransactionManager jtm = null;
		ArrayList<String> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			/** メイン処理 */
			list = FacilitySelector.getNodeFacilityIdList(parentFacilityId, ownerRoleId, level, sort, valid);
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getNodeFacilityIdList() : "
					+ e.getClass().getSimpleName() +", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}

	/**
	 * スコープへのノードの割り当てを行います。（リポジトリ更新TOPIC未送信選択可能）<BR>
	 *
	 * parentFacilityIdで指定されるスコープにfacilityIdsで指定されるノード群を
	 * 割り当てます。
	 *
	 * @version 3.1.0
	 * @since 1.0.0
	 *
	 * @param parentFacilityId　ノードを割り当てるスコープ
	 * @param facilityIds 割り当てさせるノード(群)
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void assignNodeScope(String parentFacilityId, String[] facilityIds, boolean topicSendFlg)
			throws InvalidSetting, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			//入力チェック
			RepositoryValidator.validateaAssignNodeScope(parentFacilityId, facilityIds);

			//組み込みスコープであるかチェック
			checkIsBuildInScope(parentFacilityId);
			
			/** メイン処理 */
			FacilityModifier.assignFacilitiesToScope(parentFacilityId, facilityIds);

			jtm.addCallback(new FacilityIdCacheInitCallback());
			jtm.addCallback(new FacilityTreeCacheRefreshCallback());
			jtm.addCallback(new RepositoryChangedNotificationCallback());
			jtm.addCallback(new NodeToMonitorCacheChangeCallback());


			try {
				ListenerReadWriteLock.readLock();
				for (IRepositoryListener listener : _listenerList) {
					for (String facilityId : facilityIds) {
						jtm.addCallback(new RepositoryListenerCallback(listener, Type.ASSIGN_NODE_TO_SCOPE, parentFacilityId, facilityId));
					}
				}
			} finally {
				ListenerReadWriteLock.readUnlock();
			}

			jtm.commit();
		} catch (InvalidSetting | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (FacilityNotFound e) {
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("assignNodeScope() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 * スコープへのノードの割り当てを行います。<BR>
	 *
	 * parentFacilityIdで指定されるスコープにfacilityIdsで指定されるノード群を
	 * 割り当てます。
	 *
	 * @version 3.1.0
	 * @since 1.0.0
	 *
	 * @param parentFacilityId　ノードを割り当てるスコープ
	 * @param facilityIds 割り当てさせるノード(群)
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void assignNodeScope(String parentFacilityId, String[] facilityIds)
			throws InvalidSetting, InvalidRole, HinemosUnknown {
		assignNodeScope(parentFacilityId, facilityIds, true);
	}

	/**
	 * ノードをスコープから削除します。（割り当てを解除します。）<BR>
	 * parentFacilityIdで指定されるスコープからfacilityIdsで指定されるノード群を
	 * 削除（割り当て解除）します。
	 *
	 * @version 3.1.0
	 * @since 1.0.0
	 *
	 * @param parentFacilityId ノードを取り除くスコープ
	 * @param facilityIds 取り除かれるノード（群）
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void releaseNodeScope(String parentFacilityId, String[] facilityIds)
			throws InvalidSetting, InvalidRole, HinemosUnknown{
		releaseNodeScope(parentFacilityId, facilityIds, true);
	}


	/**
	 * ノードをスコープから削除します。（割り当てを解除します。リポジトリ更新TOPIC未送信選択可能）<BR>
	 * parentFacilityIdで指定されるスコープからfacilityIdsで指定されるノード群を
	 * 削除（割り当て解除）します。
	 *
	 * @version 3.1.0
	 * @since 1.0.0
	 *
	 * @param parentFacilityId ノードを取り除くスコープ
	 * @param facilityIds 取り除かれるノード（群）
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void releaseNodeScope(String parentFacilityId, String[] facilityIds, boolean topicSendFlg)
			throws InvalidSetting, InvalidRole, HinemosUnknown{
		JpaTransactionManager jtm = null;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			//入力チェック
			RepositoryValidator.validateaAssignNodeScope(parentFacilityId, facilityIds);

			//組み込みスコープであるかチェック
			checkIsBuildInScope(parentFacilityId);
			
			/** メイン処理 */
			FacilityModifier.releaseNodeFromScope(
					parentFacilityId,
					facilityIds,
					(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID),
					topicSendFlg);

			jtm.addCallback(new FacilityIdCacheInitCallback());
			jtm.addCallback(new FacilityTreeCacheRefreshCallback());
			jtm.addCallback(new RepositoryChangedNotificationCallback());
			jtm.addCallback(new NodeToMonitorCacheChangeCallback());

			try {
				ListenerReadWriteLock.readLock();
				for (IRepositoryListener listener : _listenerList) {
					for (String facilityId : facilityIds) {
						jtm.addCallback(new RepositoryListenerCallback(listener, Type.RELEASE_NODE_FROM_SCOPE, parentFacilityId, facilityId));
					}
				}
			} finally {
				ListenerReadWriteLock.readUnlock();
			}

			// ノードマップ
			// ノードマップで対象スコープの対象ファシリティにつながっているパスを消す
			new NodeMapControllerBean().deleteMapInfo(Arrays.asList(facilityIds), parentFacilityId);
			
			jtm.commit();
		} catch (InvalidSetting | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (FacilityNotFound e) {
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("releaseNodeScope() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}


	/**********************
	 * その他のメソッド群
	 **********************/
	/**
	 * ファシリティがノードかどうかをチェックします。<BR>
	 *
	 * ファシリティIDに対応するものがノードかチェックし、結果をbooleanで返します。
	 *
	 * @version 1.0.0
	 * @since 1.0.0
	 *
	 * @param facilityId　ファシリティID
	 * @return true：ノード　false:ノードではない（スコープ）
	 * @throws FacilityNotFound 指定されたIDに該当するファシリティが存在しない場合
	 * @throws HinemosUnknown
	 */
	public boolean isNode(String facilityId) throws FacilityNotFound, HinemosUnknown {
		JpaTransactionManager jtm = null;
		boolean rtn = false;

		/** メイン処理 */
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			rtn = FacilitySelector.isNode(facilityId);
			jtm.commit();
		} catch (FacilityNotFound e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (RuntimeException e) {
			m_log.warn("isNode() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return rtn;
	}

	/**
	 * セパレータ文字列を取得します。<BR>
	 *
	 * セパレータ文字列はスコープパス表示の際のスコープを区切る文字列
	 *
	 *
	 * @version 1.0.0
	 * @since 1.0.0
	 *
	 * @return セパレータ文字列
	 */
	public String getSeparator() {
		/** メイン処理 */
		return FacilitySelector.SEPARATOR;
	}

	/**
	 * ノード作成変更時に、利用可能プラットフォームを表示するためのメソッド。
	 *
	 * @version 3.2.0
	 * @since 3.2.0
	 * @return ArrayList<RepositoryTableInfo>
	 * @throws HinemosUnknown
	 */
	public ArrayList<RepositoryTableInfo> getPlatformList() throws HinemosUnknown {
		ArrayList<RepositoryTableInfo> list = new ArrayList<RepositoryTableInfo>();
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			List<CollectorPlatformMstEntity> ct = QueryUtil.getAllCollectorPlatformMst();
			for (CollectorPlatformMstEntity bean : ct) {
				list.add(new RepositoryTableInfo(bean.getPlatformId(), bean.getPlatformName()));
			}
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getPlatformList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}

	/**
	 * ノード作成変更時に、利用可能な仮想化ソリューションを表示するためのメソッド。
	 *
	 * @version 3.2.0
	 * @since 3.2.0
	 * @return ArrayList
	 * @throws HinemosUnknown
	 */
	public ArrayList<RepositoryTableInfo> getCollectorSubPlatformTableInfoList() throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		ArrayList<RepositoryTableInfo> list = new ArrayList<RepositoryTableInfo>();
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			List<CollectorSubPlatformMstEntity> ct = com.clustercontrol.repository.util.QueryUtil.getAllCollectorSubPlatformMstEntity();
			for (CollectorSubPlatformMstEntity bean : ct) {
				list.add(new RepositoryTableInfo(bean.getSubPlatformId(), bean.getSubPlatformName()));
			}
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getVmSolutionMstList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}

	/**
	 * ノード作成変更時に、利用可能な仮想化プロトコルを表示するためのメソッド。
	 *
	 * @version 3.2.0
	 * @since 3.2.0
	 * @return ArrayList
	 * @throws HinemosUnknown
	 */
	public ArrayList<String> getVmProtocolMstList() throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		ArrayList<String> list = new ArrayList<String>();
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			List<String> ct = com.clustercontrol.vm.util.QueryUtil.getVmProtocolMstDistinctProtocol();
			for (String protocol : ct) {
				list.add(protocol);
			}
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getVmProtocolMstList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}
	/**
	 * リポジトリの最終更新時刻を取得
	 *
	 * @return
	 */
	public Date getLastUpdate(){
		Date updateTime = new Date(SettingUpdateInfo.getInstance().getRepositoryUpdateTime());
		m_log.debug("getLastUpdate() lastUpdate = " + updateTime.toString());
		return updateTime;
	}

	/**
	 * エージェントの状態を返します。<BR>
	 *
	 * @return
	 * @throws HinemosUnknown
	 */
	public ArrayList<AgentStatusInfo> getAgentStatusList() throws HinemosUnknown{
		JpaTransactionManager jtm = null;
		ArrayList<AgentStatusInfo> list = null;

		m_log.debug("getAgentStatusList() ");
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			list = AgentLibDownloader.getAgentStatusList();
			jtm.commit();
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("getAgentStatusList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}

	/**
	 * エージェントを再起動、アップデートします。<BR>
	 *
	 *
	 * @param facilityId　ファシリティID
	 * @param agentCommand エージェントに実行するコマンド。
	 * @see com.clustercontrol.repository.bean.AgentCommandConstant
	 */
	public void restartAgent(ArrayList<String> facilityIdList, int agentCommand) {
		// Local Variables
		TopicInfo topicInfo = null;

		// MAIN
		topicInfo = new TopicInfo();

		/*
		 * com.clustercontrol.repository.bean.AgentCommandConstant
		 * public static int RESTART = 1;
		 * public static int UPDATE = 2;
		 */
		topicInfo.setAgentCommand(agentCommand);

		// 同時にアップデートされると困るので、ずらす。
		int restartSleep = RepositoryControllerBean.RESTART_AGENT_SLEEP_TIME.intValue();
		try {
			restartSleep = HinemosPropertyCommon.repository_restart_sleep.getIntegerValue();
			m_log.info("restartAgent() restart sleep = " + restartSleep);
		} catch (Exception e) {
			m_log.warn("restartAgent() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}
		for (String facilityId : facilityIdList) {
			if (AgentConnectUtil.isValidAgent(facilityId)) {

				try {
					// オブジェクト権限チェック
					QueryUtil.getFacilityPK(facilityId, ObjectPrivilegeMode.EXEC);
				} catch (InvalidRole e) {
					// 権限がない場合は、該当のファシリティIDに対する処理は行わない
					continue;
				} catch (FacilityNotFound e) {
					continue;
				}

				m_log.info("restart() : setTopic(" + facilityId + ")");
				AgentConnectUtil.setTopic(facilityId, topicInfo);
				try {
					Thread.sleep(restartSleep);
				} catch (InterruptedException e) {
					m_log.info("restartAgent : " + e.getMessage());
				}
			} else {
				m_log.info("restartAgent() agent does not connect. " +
						"(facilityId=" + facilityId + ")");
			}
		}
	}

	/**
	 * @param facilityId　ファシリティID
	 * @param agentCommand エージェントに実行するコマンド。
	 * @throws HinemosUnknown
	 * @see com.clustercontrol.repository.bean.AgentCommandConstant
	 */
	public HashMap<String, String> getAgentLibMap (ArrayList<String> facilityIdList) throws HinemosUnknown {
		HashMap<String, String> map = null;
		try {
			map = AgentLibDownloader.getAgentLibMap(facilityIdList, false);
		} catch (Exception e) {
			m_log.warn("getAgentLibMap() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
		return map;
	}

	public void checkIsUseFacility (String facilityId) throws HinemosUnknown, UsedFacility {
		String message = "";
		try {
			new JobControllerBean().isUseFacilityId(facilityId);
		} catch(UsedFacility e) {
			message += (e.getMessage() + "\n");
		}
		try {
			new MonitorControllerBean().isUseFacilityId(facilityId);
		} catch(UsedFacility e) {
			message += (e.getMessage() + "\n");
		}
		try {
			new NotifyControllerBean().isUseFacilityId(facilityId);
		} catch(UsedFacility e) {
			message += (e.getMessage() + "\n");
		}
		try {
			new InfraControllerBean().isUseFacilityId(facilityId);
		} catch(UsedFacility e) {
			message += e.getMessage();
		}
		if (message.trim().length() > 0) {
			UsedFacility ex = new UsedFacility(message);
			throw ex;
		}
	}


	/**
	 * ホスト名から逆引きされたIPアドレスに該当するノード一覧を返す。
	 *
	 * @param hostname ホスト名
	 * @return ファシリティIDの配列
	 */
	public Set<String> getNodeListByNodename(String hostname) {
		Set<String> ret = new HashSet<String>();

		JpaTransactionManager jtm = null;

		/** メイン処理 */
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			ret = FacilitySelector.getNodeListByNodename(hostname);

			// コミット処理
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("modifyUserInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			// ロールバック処理
			if (jtm != null)
				jtm.rollback();
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;
	}

	/**
	 * IPアドレスに該当するノード一覧を返す。
	 * @param ipAddress IPアドレス
	 * @return ファシリティIDの配列
	 */
	public Set<String> getNodeListByIpAddress(InetAddress ipAddress) {
		Set<String> ret = new HashSet<String>();

		JpaTransactionManager jtm = null;

		/** メイン処理 */
		try {
			// EntityManager生成
			jtm = new JpaTransactionManager();
			jtm.begin();

			ret = FacilitySelector.getNodeListByIpAddress(ipAddress);

			// コミット処理
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("modifyUserInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			// ロールバック処理
			if (jtm != null)
				jtm.rollback();
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;
	}

	/**
	 * ホスト名に該当するノード一覧を取得する。
	 * @param hostname ホスト名
	 * @return ファシリティIDの配列
	 */
	public Set<String> getNodeListByHostname(String hostname) {
		Set<String> ret = new HashSet<String>();

		JpaTransactionManager jtm = null;

		/** メイン処理 */
		try {
			// EntityManager生成
			jtm = new JpaTransactionManager();
			jtm.begin();

			ret = FacilitySelector.getNodeListByHostname(hostname);

			// コミット処理
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("modifyUserInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			// ロールバック処理
			if (jtm != null)
				jtm.rollback();
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;
	}

	/**
	 * ファシリティIDが配下にあるかどうかを返す。
	 * @param scopeFacilityId スコープのファシリティID
	 * @param nodeFacilityId ノードのファシリティID
	 * @param ownerRoleId オーナーロールID
	 * @return
	 */
	public boolean containsFaciliyId(String scopeFacilityId, String nodeFacilityId, String ownerRoleId) {
		boolean ret = false;

		JpaTransactionManager jtm = null;

		/** メイン処理 */
		try {
			// EntityManager生成
			jtm = new JpaTransactionManager();
			jtm.begin();

			ret = FacilitySelector.containsFaciliyId(scopeFacilityId, nodeFacilityId, ownerRoleId);

			// コミット処理
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("modifyUserInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			// ロールバック処理
			if (jtm != null)
				jtm.rollback();
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;
	}
	
	private void snmpv3Check(String securityLevel, String user, String authPass, String privPass) throws HinemosUnknown {
		if (securityLevel.equals(SnmpSecurityLevelConstant.AUTH_NOPRIV) ||
				securityLevel.equals(SnmpSecurityLevelConstant.AUTH_PRIV)) {
			if(user == null || user.length() < 1) {
				throw new HinemosUnknown(MessageConstant.MESSAGE_PLEASE_SET_USER_NAME.getMessage());
			}
			if(authPass == null || authPass.length() < 8) {
				throw new HinemosUnknown(MessageConstant.MESSAGE_PLEASE_SET_AUTHPASS_8CHARA_MINIMUM.getMessage());
			}
		}
		if (securityLevel.equals(SnmpSecurityLevelConstant.AUTH_PRIV)) {
			if(privPass == null || privPass.length() < 8) {
				throw new HinemosUnknown(MessageConstant.MESSAGE_PLEASE_SET_PRIVPASS_8CHARA_MINIMUM.getMessage());
			}
		}
	}

	/**
	 * リフレッシュを行わずにノードを新規に追加します。<BR>
	 * 性能面を考慮し連続で複数登録する場合などは更新後にリフレッシュを行う
	 *
	 * @version 5.0.0
	 * @since 5.0.0
	 *
	 * @param nodeInfo 追加するノード情報のプロパティ
	 * @throws FacilityDuplicate
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	public void addNodeWithoutRefresh(NodeInfo nodeInfo) throws FacilityDuplicate, InvalidSetting, HinemosUnknown {
		 JpaTransactionManager jtm = new JpaTransactionManager();

		try {
			jtm.begin();

			// メンバ変数にnullが含まれていることがあるので、nullをデフォルト値に変更する。
			nodeInfo.setDefaultInfo();

			// 入力チェック
			RepositoryValidator.validateNodeInfo(nodeInfo);

			FacilityModifier.addNode(
					nodeInfo,
					(String) HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID),
					FacilitySortOrderConstant.DEFAULT_SORT_ORDER_NODE);

			final String facilityId = nodeInfo.getFacilityId();
			
			jtm.addCallback(new NodeCacheRemoveCallback(facilityId));

			try {
				ListenerReadWriteLock.readLock();
				for (IRepositoryListener listener : _listenerList) {
					jtm.addCallback(new RepositoryListenerCallback(listener, Type.ADD_NODE, null, facilityId));
				}
			} finally {
				ListenerReadWriteLock.readUnlock();
			}
			
			jtm.addCallback(new NodeToMonitorCacheChangeCallback());
			final NodeInfo _nodeInfo = nodeInfo;
			jtm.addCallback(new EmptyJpaTransactionCallback() {
				@Override
				public void postCommit() {
					NodeMonitorPollerController.registNodeMonitorPoller(_nodeInfo);
				}
			});

			jtm.commit();
		} catch (EntityExistsException e) {
			String errMsg = " ipAddress=" + nodeInfo.getIpAddressV4() + " "
					+ nodeInfo.getIpAddressV6() + " facilityID="
					+ nodeInfo.getFacilityId() + ",";
			m_log.warn("addNodeWithoutRefresh() : " + errMsg + e.getClass().getSimpleName() + ", " + e.getMessage());

			jtm.rollback();
			throw new FacilityDuplicate(e.getMessage(), e);
		} catch (InvalidSetting | HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (FacilityNotFound e) {
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("addNodeWithoutRefresh() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			jtm.close();
		}
	}

	/**
	 * IRepositoryListenerを追加する.
	 * @param listener 追加するIRepositoryListener
	 */
	public static void addListener(IRepositoryListener listener) {
		if (listener == null) {
			throw new NullPointerException("argument (listerer) is null.");
		}
		try {
			ListenerReadWriteLock.writeLock();

			for (IRepositoryListener obj : _listenerList) {
				if (listener.equals(obj)) {
					m_log.info("skipped, listener already registered : listener = " + listener.getListenerId());
					return;
				}
			}

			m_log.debug("adding new listener : listenerId = " + listener.getListenerId());
			_listenerList.add(listener);
		} finally {
			ListenerReadWriteLock.writeUnlock();
		}
	}

	public static void removeListener(String listenerId) {
		if (listenerId == null) {
			throw new NullPointerException("argument (listererId) is null.");
		}

		List<IRepositoryListener> listenerList = new ArrayList<IRepositoryListener>();
		try {
			ListenerReadWriteLock.readLock();

			for (IRepositoryListener listener : _listenerList) {
				if (listenerId.equals(listener.getListenerId())) {
					m_log.debug("removing listener : listenerId = " + listener.getListenerId());
					listenerList.add(listener);
				}
			}
		} finally {
			ListenerReadWriteLock.readUnlock();

			try {
				ListenerReadWriteLock.writeLock();
				_listenerList.removeAll(listenerList);
			} finally {
				ListenerReadWriteLock.writeUnlock();
			}
		}
	}

	private static class ListenerReadWriteLock {
		private static final ReentrantReadWriteLock _lock = new ReentrantReadWriteLock();

		public static void readLock() {
			_lock.readLock().lock();
		}

		public static void readUnlock() {
			_lock.readLock().unlock();
		}

		public static void writeLock() {
			_lock.writeLock().lock();
		}

		public static void writeUnlock() {
			_lock.writeLock().unlock();
		}
	}
	
	/**
	 * 引数で与えられたファシリティIDのノードが組み込みスコープである場合には
	 * HinemosUnknownを送出します。
	 *
	 * @version 5.0.0
	 * @since 5.0.0
	 *
	 * @param facilityId チェックを行う対象のファシリティID
	 * @throws FacilityDuplicate
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	private void checkIsBuildInScope(String facilityId) throws FacilityNotFound, InvalidRole, HinemosUnknown{
		ScopeInfo facility = QueryUtil.getScopePK(facilityId);

		if(FacilitySelector.isBuildinScope(facility)){
			HinemosUnknown e = new HinemosUnknown("this facility is built in scope. (facilityId = " + facilityId + ")");
			m_log.info("deleteScopeRecursive() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}
}