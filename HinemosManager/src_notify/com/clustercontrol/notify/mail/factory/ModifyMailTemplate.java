/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.mail.factory;

import java.util.List;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MailTemplateDuplicate;
import com.clustercontrol.fault.MailTemplateNotFound;
import com.clustercontrol.notify.mail.model.MailTemplateInfo;
import com.clustercontrol.notify.mail.util.QueryUtil;
import com.clustercontrol.notify.model.NotifyInfo;
import com.clustercontrol.notify.model.NotifyMailInfo;
import com.clustercontrol.util.HinemosTime;

/**
 * メールテンプレート情報を更新するクラス<BR>
 *
 * @version 2.4.0
 * @since 2.4.0
 */
public class ModifyMailTemplate {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( ModifyMailTemplate.class );

	/**
	 * メールテンプレート情報を作成します。
	 * <p>
	 * <ol>
	 *  <li>メールテンプレート情報を作成します。</li>
	 * </ol>
	 * 
	 * @param info 作成対象のメールテンプレート情報
	 * @param name メールテンプレート情報を作成したユーザ名
	 * @return 作成に成功した場合、<code> true </code>
	 * @throws MailTemplateDuplicate
	 * 
	 * @see com.clustercontrol.notify.ejb.entity.MailTemplateInfoBean
	 */
	public boolean add(MailTemplateInfo data, String name) throws MailTemplateDuplicate {

		long now = HinemosTime.currentTimeMillis();

		//エンティティBeanを作る
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// 重複チェック
			jtm.checkEntityExists(MailTemplateInfo.class, data.getMailTemplateId());
			data.setRegDate(now);
			data.setRegUser(name);
			data.setUpdateDate(now);
			data.setUpdateUser(name);
			
			em.persist(data);
		} catch (EntityExistsException e) {
			m_log.info("add() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new MailTemplateDuplicate(e.getMessage(),e);
		}

		return true;
	}
	
	/**
	 * メールテンプレート情報を変更します。
	 * <p>
	 * <ol>
	 *  <li>メールテンプレートIDより、メールテンプレート情報を取得し、
	 *      メールテンプレート情報を変更します。</li>
	 * </ol>
	 * 
	 * @param info 変更対象のメールテンプレート情報
	 * @param name 変更したユーザ名
	 * @return 変更に成功した場合、<code> true </code>
	 * @throws MailTemplateNotFound
	 * @throws InvalidRole
	 * 
	 * @see com.clustercontrol.notify.ejb.entity.MailTemplateInfoBean
	 */
	public boolean modify(MailTemplateInfo data, String name) throws MailTemplateNotFound, InvalidRole {

		//メールテンプレート情報を取得
		MailTemplateInfo mailTemplateInfo = QueryUtil.getMailTemplateInfoPK(data.getMailTemplateId(), ObjectPrivilegeMode.MODIFY);

		//メールテンプレート情報を更新
		mailTemplateInfo.setDescription(data.getDescription());
		mailTemplateInfo.setSubject(data.getSubject());
		mailTemplateInfo.setBody(data.getBody());
		mailTemplateInfo.setOwnerRoleId(data.getOwnerRoleId());
		mailTemplateInfo.setUpdateDate(HinemosTime.currentTimeMillis());
		mailTemplateInfo.setUpdateUser(name);

		return true;
	}
	
	/**
	 * メールテンプレート情報を削除します。<BR>
	 * <p>
	 * <ol>
	 *  <li>メールテンプレートIDより、メールテンプレート情報を取得し、
	 *      メールテンプレート情報を削除します。</li>
	 * </ol>
	 * 
	 * @param mailTemplateId 削除対象のメールテンプレートID
	 * @return 削除に成功した場合、<code> true </code>
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public boolean delete(String mailTemplateId) throws InvalidRole, HinemosUnknown {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// メールテンプレート情報を検索し取得
			MailTemplateInfo entity = null;
			try {
				entity = QueryUtil.getMailTemplateInfoPK(mailTemplateId, ObjectPrivilegeMode.MODIFY);
			} catch (MailTemplateNotFound e) {
				throw new HinemosUnknown(e.getMessage(), e);
			}

			// 利用されているメールテンプレートか否かチェックする
			List<NotifyInfo> notifyList = com.clustercontrol.notify.util.QueryUtil.getAllNotifyInfo_NONE();
			for (NotifyInfo notify : notifyList) {
				NotifyMailInfo mail = notify.getNotifyMailInfo();
				if (mail == null || mail.getMailTemplateInfoEntity() == null) {
					continue;
				}
				if (entity.getMailTemplateId().equals(
						mail.getMailTemplateInfoEntity().getMailTemplateId())) {
					String message = "used by " + notify.getNotifyId();
					m_log.info(message);
					throw new HinemosUnknown(message);
				}
			}

			//メールテンプレート情報を削除
			em.remove(entity);

			return true;
		}
	}
}