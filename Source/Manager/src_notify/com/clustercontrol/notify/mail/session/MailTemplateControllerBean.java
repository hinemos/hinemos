/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.mail.session;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.util.RoleValidator;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.MailTemplateDuplicate;
import com.clustercontrol.fault.MailTemplateNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.notify.mail.factory.ModifyMailTemplate;
import com.clustercontrol.notify.mail.factory.SelectMailTemplate;
import com.clustercontrol.notify.mail.model.MailTemplateInfo;
import com.clustercontrol.notify.mail.util.QueryUtil;
import com.clustercontrol.notify.util.NotifyValidator;

/**
 * メールテンプレート機能の管理を行う Session Bean <BR>
 * クライアントからの Entity Bean へのアクセスは、Session Bean を介して行います。
 */
public class MailTemplateControllerBean {

	private static Log m_log = LogFactory.getLog( MailTemplateControllerBean.class );

	/**
	 * メールテンプレート情報をマネージャに登録します。<BR>
	 * 
	 * @param MailTemplateInfo
	 * @return MailTemplateInfo
	 * @throws HinemosUnknown
	 * @throws MailTemplateDuplicate
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * 
	 * @see com.clustercontrol.notify.factory.AddMailTemplate#add(MailTemplateInfoData)
	 */
	public MailTemplateInfo addMailTemplate(MailTemplateInfo data) throws HinemosUnknown, MailTemplateDuplicate, InvalidSetting, InvalidRole {
		MailTemplateInfo ret = null;
		JpaTransactionManager jtm = null;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			//入力チェック
			NotifyValidator.validateMailTemplateInfo(data, true);
			
			//ユーザがオーナーロールIDに所属しているかチェック
			RoleValidator.validateUserBelongRole(data.getOwnerRoleId(),
					(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID),
					(Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR));

			ModifyMailTemplate mailTemplate = new ModifyMailTemplate();
			mailTemplate.add(data, (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));

			jtm.commit();
			ret = new SelectMailTemplate().getMailTemplateInfo(data.getMailTemplateId());
		} catch (MailTemplateDuplicate | InvalidSetting e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("addMailTemplate() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
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
	 * マネージャ上のメールテンプレート情報を変更します。<BR>
	 * 
	 * @param MailTemplateInfo
	 * @return MailTemplateInfo
	 * @throws HinemosUnknown
	 * @throws MailTemplateNotFound
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * 
	 * @see com.clustercontrol.notify.factory.ModifyMailTemplate#modify(MailTemplateInfoData)
	 * @see com.clustercontrol.notify.bean.MailTemplateInfoData
	 */
	public MailTemplateInfo modifyMailTemplate(MailTemplateInfo data) throws HinemosUnknown, MailTemplateNotFound,InvalidSetting, InvalidRole {
		MailTemplateInfo ret = null;
		JpaTransactionManager jtm = null;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			//入力チェック
			NotifyValidator.validateMailTemplateInfo(data, false);

			ModifyMailTemplate mailTemplate = new ModifyMailTemplate();
			mailTemplate.modify(data, (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));

			jtm.commit();
			ret = new SelectMailTemplate().getMailTemplateInfo(data.getMailTemplateId());
		} catch (MailTemplateNotFound | InvalidSetting | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("modifyMailTemplate() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
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
	 * メールテンプレート情報をマネージャから削除します。<BR>
	 * 
	 * @param mailTemplateId 削除対象のメールテンプレートID
	 * @return List<MailTemplateInfo>
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws MailTemplateNotFound 
	 * 
	 * @see com.clustercontrol.notify.factory.DeleteMailTemplate#delete(String)
	 */
	public List<MailTemplateInfo> deleteMailTemplate(List<String> mailTemplateIdList) throws InvalidRole, HinemosUnknown, MailTemplateNotFound {
		m_log.debug("deleteMailTemplate");

		JpaTransactionManager jtm = null;
		List<MailTemplateInfo> retList =  new ArrayList<>();

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			// メールテンプレート情報を削除
			ModifyMailTemplate mailTemplate = new ModifyMailTemplate();
			for(String mailTemplateId : mailTemplateIdList) {
				retList.add(new SelectMailTemplate().getMailTemplateInfo(mailTemplateId));
				mailTemplate.delete(mailTemplateId);
			}

			jtm.commit();
		} catch (InvalidRole | HinemosUnknown e){
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (MailTemplateNotFound e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.warn("deleteMailTemplate() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
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
	 * 引数で指定されたメールテンプレート情報を返します。
	 * 
	 * @param mailTemplateId 取得対象のメールテンプレートID
	 * @return MailTemplateInfo
	 * @throws HinemosUnknown
	 * @throws MailTemplateNotFound
	 * @throws InvalidRole
	 * 
	 * @see com.clustercontrol.notify.factory.SelectMailTemplate#getMailTemplateInfo(String)
	 */
	public MailTemplateInfo getMailTemplateInfo(String mailTemplateId) throws MailTemplateNotFound, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		MailTemplateInfo info = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// メールテンプレート情報を取得
			SelectMailTemplate mailTemplate = new SelectMailTemplate();
			info = mailTemplate.getMailTemplateInfo(mailTemplateId);

			jtm.commit();
		} catch (InvalidRole | MailTemplateNotFound e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getMailTemplateInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
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
	 * メールテンプレートID一覧を取得します。<BR>
	 * 
	 * 戻り値のArrayListにはMailTemplateId(String)が順に
	 * 格納されています。
	 * 
	 * @return メールテンプレートID一覧
	 * @throws MailTemplateNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * 
	 * @see com.clustercontrol.notify.factory.SelectMailTemplate#getMailTemplateIdList()
	 */
	public ArrayList<String> getMailTemplateIdList() throws MailTemplateNotFound, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		ArrayList<String> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// メールテンプレートID一覧を取得
			SelectMailTemplate mailTemplate = new SelectMailTemplate();
			list = mailTemplate.getMailTemplateIdList();

			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getMailTemplateIdList() : "
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
	 * メールテンプレート情報一覧を取得します。<BR>
	 * 
	 * 戻り値はArrayListのArrayListであり以下のように格納される。
	 * 
	 * <Pre>
	 * ArrayList info = new ArrayList();
	 *  info.add(mailTemplate.getMailTemplateId());
	 *  info.add(mailTemplate.getDescription());
	 *	info.add(mailTemplate.getRegUser());
	 *	info.add(mailTemplate.getRegDate() == null ? null:new Date(mailTemplate.getRegDate().getTime()));
	 *	info.add(mailTemplate.getUpdateUser());
	 *	info.add(mailTemplate.getUpdateDate() == null ? null:new Date(mailTemplate.getUpdateDate().getTime()));
	 * 
	 *  list.add(info);
	 * </Pre>
	 * 
	 * 
	 * @return メールテンプレート情報一覧（Objectの2次元配列）
	 * @throws MailTemplateNotFound
	 * @thorws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * @see com.clustercontrol.notify.factory.SelectMailTemplate#getMailTemplateList()
	 */
	public ArrayList<MailTemplateInfo> getMailTemplateList() throws MailTemplateNotFound, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		ArrayList<MailTemplateInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// メールテンプレート一覧を取得
			SelectMailTemplate mailTemplate = new SelectMailTemplate();
			list = mailTemplate.getMailTemplateList();

			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getMailTemplateList() : "
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
	 * オーナーロールIDを指定してメールテンプレート情報一覧を取得します。<BR>
	 * 
	 * @param ownerRoleId オーナーロールID
	 * @return メールテンプレート情報一覧
	 * @thorws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * @see com.clustercontrol.notify.factory.SelectMailTemplate#getMailTemplateList()
	 */
	public ArrayList<MailTemplateInfo> getMailTemplateListByOwnerRole(String ownerRoleId) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		ArrayList<MailTemplateInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// メールテンプレート一覧を取得
			SelectMailTemplate mailTemplate = new SelectMailTemplate();
			if (ownerRoleId != null) {
				list = mailTemplate.getMailTemplateListByOwnerRole(ownerRoleId);
			} else {
				list = mailTemplate.getMailTemplateList();
			}

			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getMailTemplateListByOwnerRole() : "
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
