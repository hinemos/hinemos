/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.factory;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.notify.model.NotifyInfo;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.notify.util.QueryUtil;

/**
 * システム通知情報を更新するクラスです。
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class ModifyNotifyRelation {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( ModifyNotifyRelation.class );
	
	/**
	 * システム通知情報を作成します。
	 * <p>
	 * <ol>
	 *  <li>通知情報を作成します。</li>
	 *  <li>通知イベント情報を作成し、通知情報に設定します。</li>
	 * </ol>
	 * 
	 * @param info 作成対象の通知情報
	 * @return 作成に成功した場合、<code> true </code>
	 * @throws HinemosUnknown
	 * 
	 * @see com.clustercontrol.notify.ejb.entity.SystemNotifyInfoBean
	 * @see com.clustercontrol.notify.ejb.entity.SystemNotifyEventInfoBean
	 */
	public boolean add(Collection<NotifyRelationInfo> info) throws HinemosUnknown {
		NotifyRelationInfo relation = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			if(info != null){
				// システム通知イベント情報を挿入
				Iterator<NotifyRelationInfo> it= info.iterator();

				while(it.hasNext()){
					relation = it.next();

					if(relation != null){
						NotifyInfo notifyInfo = new SelectNotify().getNotify(relation.getNotifyId());
						relation.setNotifyType(notifyInfo.getNotifyType());
						em.persist(relation);
					}
				}
			}

		} catch (Exception e) {
			m_log.warn("add() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		return true;
	}

	/**
	 * システム通知情報を作成します。
	 * <p>
	 * <ol>
	 *  <li>通知情報を作成します。</li>
	 *  <li>通知イベント情報を作成し、通知情報に設定します。</li>
	 * </ol>
	 * 
	 * @param info 作成対象の通知情報
	 * @param ownerRoleId オーナーロールID
	 * @return 作成に成功した場合、<code> true </code>
	 * @throws HinemosUnknown
	 * 
	 * @see com.clustercontrol.notify.ejb.entity.SystemNotifyInfoBean
	 * @see com.clustercontrol.notify.ejb.entity.SystemNotifyEventInfoBean
	 */
	public boolean add(Collection<NotifyRelationInfo> info, String owenrRoleId) throws HinemosUnknown {
		NotifyRelationInfo relation = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			if(info != null){
				// システム通知イベント情報を挿入
				Iterator<NotifyRelationInfo> it= info.iterator();

				while(it.hasNext()){
					relation = it.next();

					if(relation != null){
						NotifyInfo notifyInfo = new SelectNotify().getNotifyByOwnerRole(relation.getNotifyId(), owenrRoleId);
						relation.setNotifyType(notifyInfo.getNotifyType());
						em.persist(relation);
					}
				}
			}

		} catch (Exception e) {
			m_log.warn("add() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		return true;
	}
	
	/**
	 * システム通知情報を変更します。
	 * <p>
	 * <ol>
	 *  <li>通知IDより、システム通知情報を取得します。</li>
	 *  <li>システム通知情報を変更します。</li>
	 *  <li>システム通知情報に設定されているシステム通知イベント情報を削除します。</li>
	 *  <li>システム通知イベント情報を作成し、通知情報に設定します。</li>
	 * </ol>
	 * 
	 * @param info 変更対象のシステム通知情報
	 * @return 変更に成功した場合、<code> true </code>
	 * @throws HinemosUnknown
	 * @throws NotifyNotFound
	 * 
	 * @see com.clustercontrol.notify.ejb.entity.SystemNotifyInfoBean
	 * @see com.clustercontrol.notify.ejb.entity.SystemNotifyEventInfoBean
	 * @see com.clustercontrol.notify.factory.DeleteSystemNotify#deleteEvents(Collection)
	 */
	public boolean modify(Collection<NotifyRelationInfo> info, String notifyGroupId) throws HinemosUnknown, NotifyNotFound {
		NotifyRelationInfo relation = null;
		m_log.debug("ModifyNotifyRelation.modify() notifyGroupId = " + notifyGroupId);
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			/**
			 * 通知グループと通知IDは更新のたびに内容が変わる可能性があるので、
			 * findByPKでデータを読み出し更新するのでは、消え残る可能性がある。
			 * そこで、通知グループに紐つくものをすべて削除し、再度投入する。
			 **/
			// システム通知イベント情報を削除
			if(notifyGroupId != null && !notifyGroupId.equals("")){
				delete(notifyGroupId);
			}
			if(info != null){
				Iterator<NotifyRelationInfo> it= info.iterator();

				while(it.hasNext()){

					relation = it.next();

					if(relation != null){
						// 通知情報を検索
						m_log.debug("NotifyRelation ADD before : notifyGroupId = " + relation.getNotifyGroupId() + ", notifyId = " + relation.getNotifyId());
						NotifyRelationInfo entity = new NotifyRelationInfo(relation.getNotifyGroupId(), relation.getNotifyId());
						NotifyInfo notifyInfo = new SelectNotify().getNotify(relation.getNotifyId());
						entity.setNotifyType(notifyInfo.getNotifyType());
						entity.setFunctionPrefix(relation.getFunctionPrefix());
						em.persist(entity);
						m_log.debug("NotifyRelation ADD : notifyGroupId = " + entity.getId().getNotifyGroupId() + ", notifyId = " + entity.getId().getNotifyId());
					}
				}
			}
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("modify() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		return true;
	}
	
	/**
	 * システム通知情報を変更します。
	 * <p>
	 * <ol>
	 *  <li>通知IDより、システム通知情報を取得します。</li>
	 *  <li>システム通知情報を変更します。</li>
	 *  <li>システム通知情報に設定されているシステム通知イベント情報を削除します。</li>
	 *  <li>システム通知イベント情報を作成し、通知情報に設定します。</li>
	 * </ol>
	 * 
	 * @param info 変更対象のシステム通知情報
	 * @param ownerRoleId オーナーロールID
	 * @return 変更に成功した場合、<code> true </code>
	 * @throws HinemosUnknown
	 * @throws NotifyNotFound
	 * 
	 * @see com.clustercontrol.notify.ejb.entity.SystemNotifyInfoBean
	 * @see com.clustercontrol.notify.ejb.entity.SystemNotifyEventInfoBean
	 * @see com.clustercontrol.notify.factory.DeleteSystemNotify#deleteEvents(Collection)
	 */
	public boolean modify(Collection<NotifyRelationInfo> info, String notifyGroupId, String ownerRoleId) throws HinemosUnknown, NotifyNotFound {
		NotifyRelationInfo relation = null;
		m_log.debug("ModifyNotifyRelation.modify() notifyGroupId = " + notifyGroupId);
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			/**
			 * 通知グループと通知IDは更新のたびに内容が変わる可能性があるので、
			 * findByPKでデータを読み出し更新するのでは、消え残る可能性がある。
			 * そこで、通知グループに紐つくものをすべて削除し、再度投入する。
			 **/
			// システム通知イベント情報を削除
			if(notifyGroupId != null && !notifyGroupId.equals("")){
				delete(notifyGroupId);
			}
			if(info != null){
				Iterator<NotifyRelationInfo> it= info.iterator();

				while(it.hasNext()){

					relation = it.next();

					if(relation != null){
						// 通知情報を検索
						m_log.debug("NotifyRelation ADD before : notifyGroupId = " + relation.getNotifyGroupId() + ", notifyId = " + relation.getNotifyId());
						NotifyRelationInfo entity = new NotifyRelationInfo(relation.getNotifyGroupId(), relation.getNotifyId());
						NotifyInfo notifyInfo = new SelectNotify().getNotifyByOwnerRole(relation.getNotifyId(), ownerRoleId);
						entity.setNotifyType(notifyInfo.getNotifyType());
						entity.setFunctionPrefix(relation.getFunctionPrefix());
						em.persist(entity);
						m_log.debug("NotifyRelation ADD : notifyGroupId = " + entity.getId().getNotifyGroupId() + ", notifyId = " + entity.getId().getNotifyId());
					}
				}
			}
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("modify() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		return true;
	}
	
	/**
	 * 通知グループIDを基に関連情報を削除します。
	 * <p>
	 * <ol>
	 *  <li>通知グループIDを基に関連情報を削除します。</li>
	 * </ol>
	 * 
	 * @param notifyGroupId 削除対象の通知グループID
	 * @return 削除に成功した場合、<code> true </code>
	 * @throws HinemosUnknown
	 */
	public boolean delete(String notifyGroupId) throws HinemosUnknown {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			QueryUtil.deleteNotifyRelationInfoByNotifyGroupId(notifyGroupId);

 			// JPAではDML処理順序が保障されないため、フラッシュ実行
			jtm.flush();
		} catch (Exception e) {
			m_log.warn("delete() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		return true;
	}
}