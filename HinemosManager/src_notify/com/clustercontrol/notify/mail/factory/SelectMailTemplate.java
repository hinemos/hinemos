/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.mail.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MailTemplateNotFound;
import com.clustercontrol.notify.mail.model.MailTemplateInfo;
import com.clustercontrol.notify.mail.util.QueryUtil;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * メールテンプレート情報を検索するクラス<BR>
 *
 * @version 2.4.0
 * @since 2.4.0
 */
public class SelectMailTemplate {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( SelectMailTemplate.class );

	/**
	 * メールテンプレート情報を返します。
	 * 
	 * @param mailTemplateId 取得対象のメールテンプレートID
	 * @return メールテンプレート情報
	 * @throws MailTemplateNotFound
	 * @throws InvalidRole
	 * 
	 * @see com.clustercontrol.notify.ejb.entity.MailTemplateInfoBean
	 */
	public MailTemplateInfo getMailTemplateInfo(String mailTemplateId) throws MailTemplateNotFound, InvalidRole {
		// メールテンプレート情報を取得
		MailTemplateInfo entity = null;
		try {
			entity = QueryUtil.getMailTemplateInfoPK(mailTemplateId);
		} catch (MailTemplateNotFound e) {
			String[] args = { mailTemplateId };
			AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.PLATFORM_MAIL_TEMPLATE, MessageConstant.MESSAGE_SYS_004_MAILTEMP, args);
			throw e;
		} catch (InvalidRole e) {
			String[] args = { mailTemplateId };
			AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.PLATFORM_MAIL_TEMPLATE, MessageConstant.MESSAGE_SYS_004_MAILTEMP, args);
			throw e;
		}
		return entity;
	}

	/**
	 * テンプレートID一覧を返します。
	 * <p>
	 * テンプレートIDの昇順に並んだメールテンプレートID一覧を返します。
	 * 
	 * @return テンプレートID一覧
	 * @throws MailTemplateNotFound
	 * 
	 * @see com.clustercontrol.notify.ejb.entity.MailTemplateInfoBean
	 */
	public ArrayList<String> getMailTemplateIdList() {

		ArrayList<String> list = null;

		// メールテンプレートID一覧を取得
		List<MailTemplateInfo> ct = QueryUtil.getAllMailTemplateInfoOrderByMailTemplateId();

		Iterator<MailTemplateInfo> itr = ct.iterator();
		while(itr.hasNext())
		{
			if(list == null){
				list = new ArrayList<String>();
			}

			MailTemplateInfo mailTemplate = itr.next();
			list.add(mailTemplate.getMailTemplateId());
		}

		return list;
	}

	/**
	 * メールテンプレート情報一覧を返します。
	 * <p>
	 * <ol>
	 * <li>メールテンプレートIDの昇順に並んだ全てのメールテンプレート情報を取得します。</li>
	 * <li>１メールテンプレート情報をテーブルのカラム順（{@link com.clustercontrol.notify.bean.MailTemplateTableDefine}）に、リスト（{@link ArrayList}）にセットします。</li>
	 * <li>この１メールテンプレート情報を保持するリストを、メールテンプレート情報一覧を保持するリスト（{@link ArrayList}）に格納し返します。<BR>
	 *  <dl>
	 *  <dt>メールテンプレート情報一覧（Objectの2次元配列）</dt>
	 *  <dd>{ メールテンプレート情報1 {カラム1の値, カラム2の値, … }, メールテンプレート情報2{カラム1の値, カラム2の値, …}, … }</dd>
	 *  </dl>
	 * </li>
	 * </ol>
	 * 
	 * @return メールテンプレート情報一覧（Objectの2次元配列）
	 * @throws MailTemplateNotFound
	 * 
	 * @see com.clustercontrol.notify.bean.MailTemplateTableDefine
	 * @see #collectionToArray(Collection)
	 */
	public ArrayList<MailTemplateInfo> getMailTemplateList() {
		m_log.debug("getMailTemplateList() : start");
		ArrayList<MailTemplateInfo> list = new ArrayList<MailTemplateInfo>();

		// メールテンプレート情報一覧を取得
		List<MailTemplateInfo> ct = QueryUtil.getAllMailTemplateInfoOrderByMailTemplateId();

		for(MailTemplateInfo entity : ct){
			list.add(entity);
			// for debug
			if(m_log.isDebugEnabled()){
				m_log.debug("getMailTemplateList() : " +
						"mailTemplateId = " + entity.getMailTemplateId() +
						", description = " + entity.getDescription() +
						", subject = " + entity.getSubject() +
						", body = " + entity.getBody() +
						", regUser = " + entity.getRegUser() +
						", updateUser = " + entity.getUpdateUser() +
						", regDate = " + entity.getRegDate() +
						", updateDate = " + entity.getUpdateDate());
			}
		}
		return list;
	}

	/**
	 * オーナーロールIDを条件としてメールテンプレート情報一覧を返します。
	 * 
	 * @param ownerRoleId
	 * @return メールテンプレート情報一覧（Objectの2次元配列）
	 * @throws MailTemplateNotFound
	 * 
	 */
	public ArrayList<MailTemplateInfo> getMailTemplateListByOwnerRole(String ownerRoleId) {
		m_log.debug("getMailTemplateList() : start");
		ArrayList<MailTemplateInfo> list = new ArrayList<MailTemplateInfo>();

		// メールテンプレート情報一覧を取得
		List<MailTemplateInfo> ct = QueryUtil.getAllMailTemplateInfoOrderByMailTemplateId_OR(ownerRoleId);

		for(MailTemplateInfo entity : ct){
			list.add(entity);
			// for debug
			if(m_log.isDebugEnabled()){
				m_log.debug("getMailTemplateList() : " +
						"mailTemplateId = " + entity.getMailTemplateId() +
						", description = " + entity.getDescription() +
						", subject = " + entity.getSubject() +
						", body = " + entity.getBody() +
						", regUser = " + entity.getRegUser() +
						", updateUser = " + entity.getUpdateUser() +
						", regDate = " + entity.getRegDate() +
						", updateDate = " + entity.getUpdateDate());
			}
		}
		return list;
	}
}
