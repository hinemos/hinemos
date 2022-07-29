/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.factory;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MailTemplateNotFound;
import com.clustercontrol.fault.NotifyDuplicate;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.notify.bean.NotifyTypeConstant;
import com.clustercontrol.notify.mail.model.MailTemplateInfo;
import com.clustercontrol.notify.model.NotifyCloudInfo;
import com.clustercontrol.notify.model.NotifyCommandInfo;
import com.clustercontrol.notify.model.NotifyEventInfo;
import com.clustercontrol.notify.model.NotifyInfo;
import com.clustercontrol.notify.model.NotifyInfraInfo;
import com.clustercontrol.notify.model.NotifyJobInfo;
import com.clustercontrol.notify.model.NotifyLogEscalateInfo;
import com.clustercontrol.notify.model.NotifyMailInfo;
import com.clustercontrol.notify.model.NotifyMessageInfo;
import com.clustercontrol.notify.model.NotifyRestInfo;
import com.clustercontrol.notify.model.NotifyStatusInfo;
import com.clustercontrol.notify.util.NotifyUtil;
import com.clustercontrol.notify.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;

import jakarta.persistence.EntityExistsException;

/**
 * 通知情報を変更するクラスです。
 *
 * @version 3.0.0
 * @since 1.0.0
 */
public class ModifyNotify {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( ModifyNotify.class );

	/**
	 * 通知情報を作成します。
	 * <p>
	 * <ol>
	 *  <li>通知情報を作成します。</li>
	 *  <li>通知イベント情報を作成し、通知情報に設定します。</li>
	 *  <li>キャッシュ更新用の通知情報を生成し、ログ出力キューへ送信します。
	 *      監視管理機能で、監視管理機能で保持している通知情報キャッシュに追加されます。</li>
	 * </ol>
	 * 
	 * @param info 作成対象の通知情報
	 * @return 作成に成功した場合、<code> true </code>
	 * @throws HinemosUnknown
	 * @throws NotifyDuplicate
	 * 
	 * @see com.clustercontrol.notify.ejb.entity.NotifyInfoBean
	 * @see com.clustercontrol.notify.ejb.entity.NotifyEventInfoBean
	 */
	public boolean add(NotifyInfo info, String user) throws HinemosUnknown, NotifyDuplicate {
		m_log.debug("add " + "NotifyID = " + info.getNotifyId());

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			long now = HinemosTime.currentTimeMillis();

			// 重複チェック
			jtm.checkEntityExists(NotifyInfo.class, info.getNotifyId());
			info.setRegDate(now);
			info.setRegUser(user);
			info.setUpdateDate(now);
			info.setUpdateUser(user);
			
			info.persistSelf();
			em.persist(info);
		} catch (EntityExistsException e) {
			m_log.info("add() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new NotifyDuplicate(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("add() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		return true;
	}
	
	/**
	 * 通知情報を変更します。
	 * <p>
	 * <ol>
	 *  <li>通知IDより、通知情報を取得します。</li>
	 *  <li>通知情報を変更します。</li>
	 *  <li>通知情報に設定されている通知イベント情報を削除します。</li>
	 *  <li>通知イベント情報を作成し、通知情報に設定します。</li>
	 *  <li>キャッシュ更新用の通知情報を生成し、ログ出力キューへ送信します。
	 *      監視管理機能で、監視管理機能で保持している通知情報キャッシュが更新されます。</li>
	 * </ol>
	 *
	 * @param info 変更対象の通知情報
	 * @return 変更に成功した場合、<code> true </code>
	 * @throws NotifyDuplicate
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.notify.ejb.entity.NotifyInfoBean
	 * @see com.clustercontrol.notify.ejb.entity.NotifyEventInfoBean
	 * @see com.clustercontrol.notify.factory.DeleteNotify#deleteEvents(Collection)
	 */
	public boolean modify(NotifyInfo info , String user) throws NotifyDuplicate, InvalidRole, HinemosUnknown {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			long now = HinemosTime.currentTimeMillis();

			// 通知情報を取得
			NotifyInfo notify = QueryUtil.getNotifyInfoPK(info.getNotifyId(), ObjectPrivilegeMode.MODIFY);

			// 通知情報を更新
			notify.setDescription(info.getDescription());
			notify.setValidFlg(info.getValidFlg());
			notify.setInitialCount(info.getInitialCount());
			notify.setRenotifyType(info.getRenotifyType());
			notify.setNotFirstNotify(info.getNotFirstNotify());
			notify.setRenotifyPeriod(info.getRenotifyPeriod());
			notify.setUpdateDate(now);
			notify.setUpdateUser(user);
			notify.setValidFlg(info.getValidFlg());
			notify.setCalendarId(info.getCalendarId());

			// 通知設定を無効に設定した場合は、関連する通知履歴を削除
			if(!notify.getValidFlg().booleanValue()){
				long start = HinemosTime.currentTimeMillis();
				m_log.debug("remove NotifyHistory");

				QueryUtil.deleteNotifyHistoryByNotifyId(notify.getNotifyId());
				long currentTime = HinemosTime.currentTimeMillis() - start;
				m_log.info("_delete() : count, TIME = " + currentTime + "ms, notifyId = " + notify.getNotifyId());
			}

			// 通知詳細情報を変更
			switch(info.getNotifyType()){
			case NotifyTypeConstant.TYPE_COMMAND:
				modifyNotifyCommand(info, notify);
				break;
			case NotifyTypeConstant.TYPE_EVENT:
				modifyNotifyEvent(info, notify);
				break;
			case NotifyTypeConstant.TYPE_JOB:
				modifyNotifyJob(info, notify);
				break;
			case NotifyTypeConstant.TYPE_LOG_ESCALATE:
				modifyNotifyLogEscalate(info, notify);
				break;
			case NotifyTypeConstant.TYPE_MAIL:
				modifyNotifyMail(info, notify);
				break;
			case NotifyTypeConstant.TYPE_STATUS:
				modifyNotifyStatus(info, notify);
				break;
			case NotifyTypeConstant.TYPE_INFRA:
				modifyNotifyInfra(info, notify);
				break;
			case NotifyTypeConstant.TYPE_REST:
				modifyNotifyRest(info, notify);
				break;
			case NotifyTypeConstant.TYPE_CLOUD:
				modifyNotifyCloud(info, notify);
				break;
			case NotifyTypeConstant.TYPE_MESSAGE:
				modifyNotifyMessage(info, notify);
				break;
			default:
				//NotifyTypeConstantが追加されない限り、ここには来ない想定
				String message = "NotifyType is unknown. type = " + info.getNotifyType();
				m_log.warn("modify() : " +  message);
				throw new HinemosUnknown(message);
			}

		} catch (NotifyNotFound e) {
			NotifyDuplicate e2 = new NotifyDuplicate(e.getMessage(), e);
			e2.setNotifyId(info.getNotifyId());
			throw e2;
		} catch (InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("modify() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		return true;
	}

	private boolean modifyNotifyCommand(NotifyInfo info, NotifyInfo notify) throws NotifyNotFound {
		NotifyCommandInfo command = info.getNotifyCommandInfo();
		if (command != null) {
			NotifyCommandInfo entity = QueryUtil.getNotifyCommandInfoPK(info.getNotifyId());
			NotifyUtil.copyProperties(command, entity);
		}
		return true;
	}

	private boolean modifyNotifyEvent(NotifyInfo info, NotifyInfo notify) throws NotifyNotFound {
		NotifyEventInfo event = info.getNotifyEventInfo();
		if (event != null) {
			NotifyEventInfo entity = QueryUtil.getNotifyEventInfoPK(info.getNotifyId());
			NotifyUtil.copyProperties(event, entity);
		}
		return true;
	}

	private boolean modifyNotifyJob(NotifyInfo info, NotifyInfo notify) throws NotifyNotFound {
		NotifyJobInfo job = info.getNotifyJobInfo();
		if (job != null) {
			NotifyJobInfo entity = QueryUtil.getNotifyJobInfoPK(info.getNotifyId());
			NotifyUtil.copyProperties(job, entity);
		}
		return true;
	}

	private boolean modifyNotifyLogEscalate(NotifyInfo info, NotifyInfo notify) throws NotifyNotFound {
		NotifyLogEscalateInfo log = info.getNotifyLogEscalateInfo();
		if (log != null) {
			NotifyLogEscalateInfo entity = QueryUtil.getNotifyLogEscalateInfoPK(info.getNotifyId());
			NotifyUtil.copyProperties(log, entity);
		}
		return true;
	}

	private boolean modifyNotifyMail(NotifyInfo info, NotifyInfo notify) throws NotifyNotFound {
		NotifyMailInfo mail = info.getNotifyMailInfo();
		if (mail != null) {
			MailTemplateInfo mailTemplateInfoEntity = null;
			if (mail.getMailTemplateId() != null
					&& !"".equals(mail.getMailTemplateId())) {
				try {
					mailTemplateInfoEntity
					= com.clustercontrol.notify.mail.util.QueryUtil.getMailTemplateInfoPK(mail.getMailTemplateId());
				} catch (MailTemplateNotFound e) {
					m_log.debug(e.getMessage(), e);
				} catch (InvalidRole e) {
					m_log.debug(e.getMessage(), e);
				}
			}
			NotifyMailInfo entity = QueryUtil.getNotifyMailInfoPK(info.getNotifyId());
			entity.relateToMailTemplateInfoEntity(mailTemplateInfoEntity);
			NotifyUtil.copyProperties(mail, entity);
		}
		return true;
	}

	private boolean modifyNotifyStatus(NotifyInfo info, NotifyInfo notify) throws NotifyNotFound {
		NotifyStatusInfo status = info.getNotifyStatusInfo();
		if (status != null) {
			NotifyStatusInfo entity = QueryUtil.getNotifyStatusInfoPK(info.getNotifyId());
			NotifyUtil.copyProperties(status, entity);
		}
		return true;
	}
	
	private boolean modifyNotifyInfra(NotifyInfo info, NotifyInfo notify) throws NotifyNotFound {
		NotifyInfraInfo infra = info.getNotifyInfraInfo();
		if (infra != null) {
			NotifyInfraInfo entity = QueryUtil.getNotifyInfraInfoPK(info.getNotifyId());
			NotifyUtil.copyProperties(infra, entity);
		}
		return true;
	}
	
	private boolean modifyNotifyRest(NotifyInfo info, NotifyInfo notify) throws NotifyNotFound {
		NotifyRestInfo rest = info.getNotifyRestInfo();
		if (rest != null) {
			NotifyRestInfo entity = QueryUtil.getNotifyRestInfoPK(info.getNotifyId());
			NotifyUtil.copyProperties(rest, entity);
		}
		return true;
	}
	
	private boolean modifyNotifyCloud(NotifyInfo info, NotifyInfo notify) throws NotifyNotFound {
		NotifyCloudInfo cloud = info.getNotifyCloudInfo();
		if (cloud != null) {
			NotifyCloudInfo entity = QueryUtil.getNotifyCloudInfoPK(info.getNotifyId());
			NotifyUtil.copyProperties(cloud, entity);
		}
		return true;
	}

	private boolean modifyNotifyMessage(NotifyInfo info, NotifyInfo notify) throws NotifyNotFound {
		NotifyMessageInfo message = info.getNotifyMessageInfo();
		if (message != null) {
			NotifyMessageInfo entity = QueryUtil.getNotifyMessageInfoPK(info.getNotifyId());
			NotifyUtil.copyProperties(message, entity);
		}
		return true;
	}

	/**
	 * 通知情報を削除します。
	 * <p>
	 * <ol>
	 *  <li>通知IDより、通知情報を取得します。</li>
	 *  <li>通知情報に設定されている通知イベント情報を削除します。</li>
	 *  <li>通知情報を削除します。</li>
	 *  <li>キャッシュ更新用の通知情報を生成し、ログ出力キューへ送信します。
	 *      監視管理機能で、監視管理機能で保持している通知情報キャッシュから削除されます。</li>
	 * </ol>
	 * 
	 * @param notifyId 削除対象の通知ID
	 * @return 削除に成功した場合、<code> true </code>
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * @see com.clustercontrol.notify.ejb.entity.NotifyInfoBean
	 * @see com.clustercontrol.notify.ejb.entity.NotifyEventInfoBean
	 * @see #deleteEvents(Collection)
	 */
	public boolean delete(String notifyId) throws NotifyNotFound, InvalidRole, HinemosUnknown {

		NotifyInfo notify = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// 通知設定を取得
			notify = QueryUtil.getNotifyInfoPK(notifyId, ObjectPrivilegeMode.MODIFY);

			// この通知設定の結果として通知された通知履歴を削除する
			long historyStart = HinemosTime.currentTimeMillis();
			m_log.debug("remove NotifyHistory");

			QueryUtil.deleteNotifyHistoryByNotifyId(notifyId);
			m_log.info("NotifyRelationInfo_delete() : count, TIME = " + (HinemosTime.currentTimeMillis() - historyStart) + "ms, notifyId = " + notifyId);

			// システム通知情報を削除
			long infoStart = HinemosTime.currentTimeMillis();
			m_log.debug("remove NotifyRelationInfo");

			QueryUtil.deleteNotifyRelationInfoByNotifyId(notifyId);

			m_log.info("NotifyRelationInfo_delete() : count, TIME = " + (HinemosTime.currentTimeMillis() - infoStart) + "ms, notifyId = " + notifyId);

			// 通知情報を削除
			em.remove(notify);

		} catch (InvalidRole e) {
			throw e;
		} catch (NotifyNotFound e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("delete() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
		return true;
	}
}
