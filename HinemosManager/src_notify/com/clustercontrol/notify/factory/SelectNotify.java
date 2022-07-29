/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.notify.model.NotifyInfo;
import com.clustercontrol.notify.util.QueryUtil;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * 通知情報を検索するクラスです。
 *
 * @version 3.0.0
 * @since 1.0.0
 */
public class SelectNotify {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( SelectNotify.class );

	/**
	 * 通知情報を返します。
	 *
	 * @param notifyId 取得対象の通知ID
	 * @return 通知情報
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.notify.ejb.entity.NotifyInfoBean
	 * @see com.clustercontrol.notify.ejb.entity.NotifyEventInfoBean
	 */
	public NotifyInfo getNotify(String notifyId) throws NotifyNotFound, InvalidRole, HinemosUnknown {
		// 通知情報を取得
		NotifyInfo info = null;

		try {
			info = QueryUtil.getNotifyInfoPK(notifyId);
		} catch (NotifyNotFound e) {
			String[] args = { notifyId };
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_004, args);
			throw e;
		} catch (InvalidRole e) {
			String[] args = { notifyId };
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_004, args);
			throw e;
		}

		return info;
	}

	/**
	 * 指定された種別の通知情報を返します。
	 *
	 * @param notifyId 取得対象の通知ID
	 * @param notifyType 取得対象の通知種別
	 * @return 通知情報
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.notify.ejb.entity.NotifyInfoBean
	 * @see com.clustercontrol.notify.ejb.entity.NotifyEventInfoBean
	 */
	public NotifyInfo getNotify(String notifyId, Integer notifyType) throws NotifyNotFound, InvalidRole, HinemosUnknown {
		// 通知情報を取得
		NotifyInfo info = null;

		try {
			info = QueryUtil.getNotifyInfoFindByNotifyIdAndNotifyType(notifyId, notifyType);
		} catch (NotifyNotFound e) {
			String[] args = { notifyId };
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_004, args);
			throw e;
		} catch (InvalidRole e) {
			String[] args = { notifyId };
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_004, args);
			throw e;
		}

		return info;
	}

	/**
	 * 通知情報を返します。
	 *
	 * @param notifyId 取得対象の通知ID
	 * @param ownerRoleId オーナーロールID
	 * @return 通知情報
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.notify.ejb.entity.NotifyInfoBean
	 * @see com.clustercontrol.notify.ejb.entity.NotifyEventInfoBean
	 */
	public NotifyInfo getNotifyByOwnerRole(String notifyId, String ownerRoleId) throws NotifyNotFound, InvalidRole, HinemosUnknown {
		// 通知情報を取得
		NotifyInfo info = null;

		try {
			info = QueryUtil.getNotifyInfoPK_OR(notifyId, ownerRoleId);
		} catch (NotifyNotFound e) {
			String[] args = { notifyId };
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_004, args);
			throw e;
		} catch (InvalidRole e) {
			String[] args = { notifyId };
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_004, args);
			throw e;
		}

		return info;
	}

	/**
	 * 通知情報一覧を返します(障害検知用通知を除く)。
	 * <p>
	 * <ol>
	 * <li>通知IDの昇順に並んだ全ての通知情報を取得します。</li>
	 * <li>１通知情報をテーブルのカラム順（{@link com.clustercontrol.notify.bean.NotifyTableDefine}）に、リスト（{@link ArrayList}）にセットします。</li>
	 * <li>この１通知情報を保持するリストを、通知情報一覧を保持するリスト（{@link ArrayList}）に格納し返します。<BR>
	 *  <dl>
	 *  <dt>通知情報一覧（Objectの2次元配列）</dt>
	 *  <dd>{ 通知情報1 {カラム1の値, カラム2の値, … }, 通知情報2{カラム1の値, カラム2の値, …}, … }</dd>
	 *  </dl>
	 * </li>
	 * </ol>
	 *
	 * @return 通知情報一覧（Objectの2次元配列）
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.notify.bean.NotifyTableDefine
	 * @see #collectionToArray(Collection)
	 */
	public ArrayList<NotifyInfo> getNotifyList() throws NotifyNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("getNotifyList() : start");
		ArrayList<NotifyInfo> list = new ArrayList<NotifyInfo>();

		try {
			// 通知情報一覧を取得
			List<NotifyInfo> ct = QueryUtil.getAllNotifyInfoOrderByNotifyId();
			for(NotifyInfo notify : ct){
				list.add(getNotify(notify.getNotifyId()));
			}
		} catch (NotifyNotFound e) {
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_006, null);
			throw e;
		} catch (InvalidRole e) {
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_006, null);
			throw e;
		} catch (HinemosUnknown e) {
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_006, null);
			throw e;
		}

		return list;
	}

	/**
	 * 指定種別の通知情報一覧を返します。
	 * <p>
	 * <ol>
	 * <li>通知IDの昇順に並んだ指定種別の通知情報を取得します。</li>
	 * <li>１通知情報をテーブルのカラム順（{@link com.clustercontrol.notify.bean.NotifyTableDefine}）に、リスト（{@link ArrayList}）にセットします。</li>
	 * <li>この１通知情報を保持するリストを、通知情報一覧を保持するリスト（{@link ArrayList}）に格納し返します。<BR>
	 *  <dl>
	 *  <dt>通知情報一覧（Objectの2次元配列）</dt>
	 *  <dd>{ 通知情報1 {カラム1の値, カラム2の値, … }, 通知情報2{カラム1の値, カラム2の値, …}, … }</dd>
	 *  </dl>
	 * </li>
	 * </ol>
	 *
	 * @param notifyType 通知種別
	 * @return 通知情報一覧（Objectの2次元配列）
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.notify.bean.NotifyTableDefine
	 * @see #collectionToArray(Collection)
	 */
	public ArrayList<NotifyInfo> getNotifyList(Integer notifyType) throws NotifyNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("getNotifyList(notifyType) : start");
		ArrayList<NotifyInfo> list = new ArrayList<NotifyInfo>();

		try {
			// 通知情報一覧を取得
			List<NotifyInfo> ct = QueryUtil.getNotifyInfoByNotifyTypeOrderByNotifyId(notifyType);
			for(NotifyInfo notify : ct){
				list.add(getNotify(notify.getNotifyId()));
			}
		} catch (NotifyNotFound e) {
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_006, null);
			throw e;
		} catch (InvalidRole e) {
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_006, null);
			throw e;
		} catch (HinemosUnknown e) {
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_006, null);
			throw e;
		}

		return list;
	}
	
	/**
	 * オーナーロールIDを条件として通知情報一覧を返します(障害検知用通知を除く)。
	 *
	 * @param ownerRoleId
	 * @return 通知情報一覧（Objectの2次元配列）
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.notify.bean.NotifyTableDefine
	 * @see #collectionToArray(Collection)
	 */
	public ArrayList<NotifyInfo> getNotifyListByOwnerRole(String ownerRoleId) throws HinemosUnknown {
		m_log.debug("getNotifyListByOwnerRole() : start");
		ArrayList<NotifyInfo> list = new ArrayList<NotifyInfo>();

		// 通知情報一覧を取得
		List<NotifyInfo> ct = QueryUtil.getAllNotifyInfoOrderByNotifyId_OR(ownerRoleId);
		for(NotifyInfo notify : ct){
			list.add(notify);
		}

		return list;
	}

	/**
	 * オーナーロールID、通知種別を条件として通知情報一覧を返します。
	 *
	 * @param ownerRoleId オーナーロールID
	 * @param notityType 通知種別
	 * @return 通知情報一覧（Objectの2次元配列）
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.notify.bean.NotifyTableDefine
	 * @see #collectionToArray(Collection)
	 */
	public ArrayList<NotifyInfo> getNotifyListByOwnerRole(String ownerRoleId, Integer notifyType) throws HinemosUnknown {
		m_log.debug("getNotifyListByOwnerRole(ownerRoleId, notifyType) : start");
		ArrayList<NotifyInfo> list = new ArrayList<NotifyInfo>();

		// 通知情報一覧を取得
		List<NotifyInfo> ct = QueryUtil.getNotifyInfoByNotifyTypeOrderByNotifyId_OR(ownerRoleId, notifyType);
		for(NotifyInfo notify : ct){
			list.add(notify);
		}

		return list;
	}
	/**
	 * 指定された通知種別の通知情報一覧を返します
	 *
	 * @param notifyType 指定必須
	 * @param ownerRoleId 絞り込み条件として不要な場合はnullでよい
	 * @return 通知情報一覧
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.notify.bean.NotifyTableDefine
	 * @see #collectionToArray(Collection)
	 */
	public ArrayList<NotifyInfo> getNotifyListByNotifyType(Integer notifyType ,String ownerRoleId) throws NotifyNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("getNotifyListByNotifyType() : start");
		ArrayList<NotifyInfo> list = new ArrayList<NotifyInfo>();

		try {
			// 通知情報一覧を取得
			if(ownerRoleId == null ){
				List<NotifyInfo> ct = QueryUtil.getNotifyInfoFindByNotifyType(notifyType);
				for(NotifyInfo notify : ct){
					list.add(getNotify(notify.getNotifyId()));
				}
			}else{
				List<NotifyInfo> ct = QueryUtil.getNotifyInfoFindByNotifyType_OR(notifyType,ownerRoleId);
				for(NotifyInfo notify : ct){
					list.add(getNotify(notify.getNotifyId()));
				}
			}
		} catch (HinemosUnknown e) {
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_006, null);
			throw e;
		}

		return list;
	}
	

}
