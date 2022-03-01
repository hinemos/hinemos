/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.session;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.util.RoleValidator;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.CommandTemplateDuplicate;
import com.clustercontrol.fault.CommandTemplateNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.fault.UsedCommandTemplate;
import com.clustercontrol.notify.factory.ModifyCommandTemplate;
import com.clustercontrol.notify.factory.SelectCommandTemplate;
import com.clustercontrol.notify.model.CommandTemplateInfo;
import com.clustercontrol.notify.util.NotifyCacheRefreshCallback;
import com.clustercontrol.notify.util.NotifyValidator;

/**
 * コマンド通知テンプレート機能の管理を行う Session Bean です。<BR>
 * Entity Bean へのアクセスは、Session Bean を介して行います。
 */
public class CommandTemplateControllerBean {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( CommandTemplateControllerBean.class );

	public static CommandTemplateControllerBean bean() {
		return new CommandTemplateControllerBean();
	}
	/**
	 * コマンド通知テンプレート情報を作成します。
	 *
	 * @param info 作成対象のコマンド通知テンプレート情報
	 * @return CommandTemplateInfo 作成に成功したコマンド通知テンプレート情報
	 * @throws CommandTemplateDuplicate
	 * @throws CommandTemplateNotFound
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public CommandTemplateInfo addCommandTemplate(CommandTemplateInfo info) throws CommandTemplateDuplicate, InvalidSetting, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		CommandTemplateInfo ret = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			//入力チェック(オーナーロールID)
			NotifyValidator.validateCommandTemplateInfo(info);

			//ユーザがオーナーロールIDに所属しているかチェック
			RoleValidator.validateUserBelongRole(info.getOwnerRoleId(),
					(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID),
					(Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR));

			ModifyCommandTemplate modify = new ModifyCommandTemplate();
			modify.add(info,(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));

			jtm.commit();

			SelectCommandTemplate select = new SelectCommandTemplate();
			ret = select.getCommandTemplate(info.getCommandTemplateId());
		} catch (CommandTemplateDuplicate | InvalidSetting | HinemosUnknown | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (Exception e){
			m_log.warn("addCommandTemplate() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return ret;
	}

	/**
	 * コマンド通知テンプレート情報を変更します。
	 *
	 * @param info 変更対象のコマンド通知テンプレート情報
	 * @return CommandTemplateInfo 変更に成功したコマンド通知テンプレート情報
	 * @throws CommandTemplateNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 */
	public CommandTemplateInfo modifyCommandTemplate(CommandTemplateInfo info) throws CommandTemplateNotFound, InvalidRole, HinemosUnknown, InvalidSetting {
		JpaTransactionManager jtm = null;
		CommandTemplateInfo ret = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			ModifyCommandTemplate modify = new ModifyCommandTemplate();
			modify.modify(info,(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));

			jtm.addCallback(new NotifyCacheRefreshCallback());
			jtm.commit();

			SelectCommandTemplate select = new SelectCommandTemplate();
			ret = select.getCommandTemplate(info.getCommandTemplateId());
		} catch (CommandTemplateNotFound | HinemosUnknown | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("modifyCommandTemplate() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return ret;
	}

	/**
	 * コマンド通知テンプレート情報を削除します。
	 *
	 * @param templateIdList 削除対象のコマンド通知テンプレートIDリスト
	 * @return List<CommandTemplateInfo> 削除に成功したコマンド通知テンプレート情報
	 * @throws CommandTemplateNotFound
	 * @throws UsedCommandTemplate
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public List<CommandTemplateInfo> deleteCommandTemplate(List<String> templateIdList) throws CommandTemplateNotFound, UsedCommandTemplate, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		List<CommandTemplateInfo> retList = new ArrayList<>(); 
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			ModifyCommandTemplate modify = new ModifyCommandTemplate();
			for (String templateId : templateIdList) {
				//引用チェック
				NotifyValidator.validateDeleteCommandTemplateInfo(templateId);
				retList.add(modify.delete(templateId));
			}

			jtm.commit();
		} catch(CommandTemplateNotFound | UsedCommandTemplate | HinemosUnknown | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("deleteCommandTemplate() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return retList;
	}

	/**
	 * 引数で指定されたコマンド通知テンプレート情報を返します。
	 *
	 * @param templateId 取得対象のコマンド通知テンプレートID
	 * @return コマンド通知テンプレート情報
	 * @throws CommandTemplateNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public CommandTemplateInfo getCommandTemplate(String templateId) throws CommandTemplateNotFound, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		CommandTemplateInfo info = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			SelectCommandTemplate select = new SelectCommandTemplate();
			info = select.getCommandTemplate(templateId);
		} catch (CommandTemplateNotFound | HinemosUnknown | InvalidRole e){
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.warn("getCommandTemplate() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return info;
	}

	/**
	 * オーナーロールIDを指定してコマンド通知テンプレート情報一覧を返します。
	 * 指定しない場合はユーザの読み取り可能なすべてのコマンド通知テンプレートを返します。
	 * 
	 * @param ownerRoleId オーナーロールID
	 * @return List<CommandTemplateInfo> コマンド通知テンプレート情報一覧（Objectの2次元配列）
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 */
	public List<CommandTemplateInfo> getCommandTemplateList(String ownerRoleId) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		List<CommandTemplateInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			SelectCommandTemplate select = new SelectCommandTemplate();
			if (ownerRoleId == null || ownerRoleId.isEmpty()) {
				list = select.getCommandTemplateList();
			} else {
				list = select.getCommandTemplateListByOwnerRole(ownerRoleId);
			}
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getCommandTemplateList() : "
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
}
