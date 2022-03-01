/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.CommandTemplateDuplicate;
import com.clustercontrol.fault.CommandTemplateNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.notify.model.CommandTemplateInfo;
import com.clustercontrol.notify.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;

import jakarta.persistence.EntityExistsException;

/**
 * コマンド通知テンプレート情報を変更するクラスです。
 *
 */
public class ModifyCommandTemplate {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( ModifyCommandTemplate.class );

	/**
	 * コマンド通知テンプレート情報を作成します。
	 * 
	 * @param info 作成対象のコマンド通知テンプレート情報
	 * @return 作成に成功した場合、<code> true </code>
	 * @throws CommandTemplateDuplicate
	 * @throws HinemosUnknown
	 */
	public boolean add(CommandTemplateInfo info, String user) throws HinemosUnknown, CommandTemplateDuplicate {
		m_log.debug("add() CommandTemplateID=" + info.getCommandTemplateId());

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			long now = HinemosTime.currentTimeMillis();

			// 重複チェック
			jtm.checkEntityExists(CommandTemplateInfo.class, info.getCommandTemplateId());

			info.setCreateDate(now);
			info.setCreateUser(user);
			info.setModifyDate(now);
			info.setModifyUser(user);

			em.persist(info);
		} catch (EntityExistsException e) {
			m_log.info("add() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new CommandTemplateDuplicate(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("add() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
		return true;
	}

	/**
	 * コマンド通知テンプレート情報を変更します。
	 *
	 * @param info 変更対象のコマンド通知テンプレート情報
	 * @return 変更に成功した場合、<code> true </code>
	 * @throws CommandTemplateNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public boolean modify(CommandTemplateInfo info , String user) throws CommandTemplateNotFound, InvalidRole, HinemosUnknown {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			long now = HinemosTime.currentTimeMillis();

			// コマンド通知テンプレート情報を取得
			CommandTemplateInfo template = QueryUtil.getCommandTemplateInfoPK(info.getCommandTemplateId(), ObjectPrivilegeMode.MODIFY);

			// コマンド通知テンプレート情報を更新
			template.setCommand(info.getCommand());
			template.setDescription(info.getDescription());
			template.setModifyDate(now);
			template.setModifyUser(user);
		} catch (CommandTemplateNotFound | InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("modify() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
		return true;
	}

	/**
	 * コマンド通知テンプレート情報を削除します。
	 * 
	 * @param commandTemplateId 削除対象のテンプレートID
	 * @return 削除に成功した場合、削除前のコマンド通知テンプレート情報
	 * @throws CommandTemplateNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public CommandTemplateInfo delete(String commandTemplateId) throws CommandTemplateNotFound, InvalidRole, HinemosUnknown {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			CommandTemplateInfo template = QueryUtil.getCommandTemplateInfoPK(commandTemplateId, ObjectPrivilegeMode.MODIFY);
			em.remove(template);
			return template;
		} catch (InvalidRole e) {
			throw e;
		} catch (CommandTemplateNotFound e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("delete() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}
}
