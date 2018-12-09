/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ws.mailtemplate;

import java.util.ArrayList;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.FunctionConstant;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MailTemplateDuplicate;
import com.clustercontrol.fault.MailTemplateNotFound;
import com.clustercontrol.notify.mail.model.MailTemplateInfo;
import com.clustercontrol.notify.mail.session.MailTemplateControllerBean;
import com.clustercontrol.ws.util.HttpAuthenticator;

/**
 * メールテンプレート用のWebAPIエンドポイント
 */
@javax.jws.WebService(targetNamespace = "http://mailtemplate.ws.clustercontrol.com")
public class MailTemplateEndpoint {
	@Resource
	WebServiceContext wsctx;

	private static Log m_log = LogFactory.getLog( MailTemplateEndpoint.class );
	private static Log m_opelog = LogFactory.getLog("HinemosOperation");

	/**
	 * echo(WebサービスAPI疎通用)
	 * 
	 * 権限必要なし（ユーザ名チェックのみ実施）
	 * 
	 * @param str
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public String echo(String str) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		return str + ", " + str;
	}

	/**
	 * メールテンプレート情報をマネージャに登録します。<BR>
	 * 
	 * NotifyAdd権限が必要
	 * 
	 * @param info 作成対象のメールテンプレート情報
	 * @return 作成に成功した場合、<code> true </code>
	 * @throws MailTemplateDuplicate
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * 
	 * @see com.clustercontrol.notify.factory.AddMailTemplate#add(MailTemplateInfo)
	 */
	public boolean addMailTemplate(MailTemplateInfo data) throws HinemosUnknown, MailTemplateDuplicate, InvalidUserPass, InvalidRole,InvalidSetting {
		m_log.debug("addMailTemplate");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.NOTIFY, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		boolean ret = false;

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(data != null){
			msg.append(", MailTemplateID=");
			msg.append(data.getMailTemplateId());
		}

		try {
			ret = new MailTemplateControllerBean().addMailTemplate(data);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_MAIL_TEMPLATE + " Add Failed, Method=addMailTemplate, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_MAIL_TEMPLATE + " Add, Method=addMailTemplate, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return ret;
	}

	/**
	 * マネージャ上のメールテンプレート情報を変更します。<BR>
	 * 
	 * NotifyWrite権限が必要
	 * 
	 * @param info 変更対象のメールテンプレート情報
	 * @return 変更に成功した場合、<code> true </code>
	 * @throws MailTemplateNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * 
	 * @see com.clustercontrol.notify.factory.ModifyMailTemplate#modify(MailTemplateInfo)
	 */
	public boolean modifyMailTemplate(MailTemplateInfo data) throws HinemosUnknown, MailTemplateNotFound, InvalidUserPass, InvalidRole,InvalidSetting {
		m_log.debug("modifyMailTemplate");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.NOTIFY, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		boolean ret = false;

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(data != null){
			msg.append(", MailTemplateID=");
			msg.append(data.getMailTemplateId());
		}

		try {
			ret = new MailTemplateControllerBean().modifyMailTemplate(data);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_MAIL_TEMPLATE + " Change Failed, Method=modifyMailTemplate, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_MAIL_TEMPLATE + " Change, Method=modifyMailTemplate, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return ret;
	}

	/**
	 * メールテンプレート情報をマネージャから削除します。<BR>
	 * 
	 * NotifyWrite権限が必要
	 * 
	 * @param mailTemplateId 削除対象のメールテンプレートID
	 * @return 削除に成功した場合、<code> true </code>
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * 
	 * @see com.clustercontrol.notify.factory.DeleteMailTemplate#delete(String)
	 */
	public boolean deleteMailTemplate(String mailTemplateId) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("deleteMailTemplate");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.NOTIFY, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		boolean ret = false;

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", MailTemplateID=");
		msg.append(mailTemplateId);

		try {
			ret =  new MailTemplateControllerBean().deleteMailTemplate(mailTemplateId);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_MAIL_TEMPLATE + " Delete Failed, Method=deleteMailTemplate, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_MAIL_TEMPLATE + " Delete, Method=deleteMailTemplate, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return ret;
	}

	/**
	 * 引数で指定されたメールテンプレート情報を返します。
	 * 
	 * NotifyRead権限が必要
	 * 
	 * @param mailTemplateId 取得対象のメールテンプレートID
	 * @return メールテンプレート情報
	 * @throws HinemosUnknown
	 * @throws MailTemplateNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * 
	 * @see com.clustercontrol.notify.factory.SelectMailTemplate#getMailTemplateInfo(String)
	 */
	public MailTemplateInfo getMailTemplateInfo(String mailTemplateId) throws MailTemplateNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getMailTemplateInfo");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.NOTIFY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", MailTemplateID=");
		msg.append(mailTemplateId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MAIL_TEMPLATE + " Get, Method=getMailTemplateInfo, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new MailTemplateControllerBean().getMailTemplateInfo(mailTemplateId);
	}

	/**
	 * メールテンプレート情報一覧を取得します。<BR>
	 * 
	 * NotifyRead権限が必要
	 * 
	 * @return メールテンプレート情報一覧（Objectの2次元配列）
	 * @throws MailTemplateNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * 
	 * @see com.clustercontrol.notify.factory.SelectMailTemplate#getMailTemplateList()
	 */
	public ArrayList<MailTemplateInfo> getMailTemplateList() throws HinemosUnknown, MailTemplateNotFound, InvalidUserPass, InvalidRole {
		m_log.debug("getMailTemplateList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.NOTIFY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MAIL_TEMPLATE + " Get, Method=getMailTemplateList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new MailTemplateControllerBean().getMailTemplateList();
	}

	/**
	 * オーナーロールIDを条件としてメールテンプレート情報一覧を取得します。<BR>
	 * 
	 * NotifyRead権限が必要
	 * 
	 * @param ownerRoleId
	 * @return メールテンプレート情報一覧（Objectの2次元配列）
	 * @throws MailTemplateNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * 
	 */
	public ArrayList<MailTemplateInfo> getMailTemplateListByOwnerRole(String ownerRoleId) throws HinemosUnknown, MailTemplateNotFound, InvalidUserPass, InvalidRole {
		m_log.debug("getMailTemplateList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.NOTIFY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MAIL_TEMPLATE + " Get, Method=getMailTemplateList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new MailTemplateControllerBean().getMailTemplateListByOwnerRole(ownerRoleId);
	}
}