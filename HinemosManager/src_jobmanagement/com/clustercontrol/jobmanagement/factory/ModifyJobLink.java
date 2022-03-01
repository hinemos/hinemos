/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobMasterDuplicate;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.jobmanagement.model.JobLinkSendSettingEntity;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;

import jakarta.persistence.EntityExistsException;

/**
 * ジョブ連携関連情報を登録するクラスです。
 *
 */
public class ModifyJobLink {

	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( ModifyJobLink.class );

	/**
	 * ジョブ連携送信設定情報を登録します
	 * 
	 * @param info ジョブ連携送信設定情報
	 * @param userName ユーザ名
	 * @return
	 * @throws JobMasterDuplicate
	 * @throws HinemosUnknown
	 */
	public void addJobLinkSendSetting(JobLinkSendSettingEntity info, String userName)
			throws JobMasterDuplicate, HinemosUnknown {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			//現在日時を取得
			Long now = HinemosTime.currentTimeMillis();
			
			// 重複チェック
			jtm.checkEntityExists(JobLinkSendSettingEntity.class, info.getJoblinkSendSettingId());
			
			info.setRegDate(now);
			info.setRegUser(userName);
			info.setUpdateDate(now);
			info.setUpdateUser(userName);
			em.persist(info);

		} catch (EntityExistsException e) {
			m_log.info("addJobLinkSendSetting() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new JobMasterDuplicate(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("addJobLinkSendSetting() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * ジョブ連携送信設定情報を更新します
	 * 
	 * @param info ジョブ連携送信設定情報
	 * @param userName ユーザ名
	 * @throws JobMasterNotFound
	 * @throws HinemosUnknown
	 */
	public void modifyJobLinkSendSetting(JobLinkSendSettingEntity info, String userName)
			throws JobMasterNotFound,InvalidRole, HinemosUnknown {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {

			//現在日時を取得
			Long now = HinemosTime.currentTimeMillis();

			JobLinkSendSettingEntity entity 
				= QueryUtil.getJobLinkSendSettingPK(info.getJoblinkSendSettingId(), ObjectPrivilegeMode.MODIFY);
			entity.setDescription(info.getDescription());
			entity.setFacilityId(info.getFacilityId());
			entity.setProcessMode(info.getProcessMode());
			entity.setProtocol(info.getProtocol());
			entity.setPort(info.getPort());
			entity.setHinemosUserId(info.getHinemosUserId());
			entity.setHinemosPassword(info.getHinemosPassword());
			entity.setProxyFlg(info.getProxyFlg());
			entity.setProxyHost(info.getProxyHost());
			entity.setProxyPort(info.getProxyPort()); 
			entity.setProxyUser(info.getProxyUser());
			entity.setProxyPassword(info.getProxyPassword());
			entity.setUpdateDate(now);
			entity.setUpdateUser(userName);

		} catch (JobMasterNotFound | InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("modifyJobLinkSendSetting() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * ジョブ連携送信設定情報を削除します。
	 * 
	 * @param joblinkSendSettingId ジョブ連携送信設定ID
	 * @throws HinemosUnknown
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 */
	public void deleteJobLinkSendSetting(String joblinkSendSettingId)
			throws HinemosUnknown, JobMasterNotFound, InvalidRole {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			JobLinkSendSettingEntity entity 
				= QueryUtil.getJobLinkSendSettingPK(joblinkSendSettingId, ObjectPrivilegeMode.MODIFY);

			// 削除
			em.remove(entity);

		} catch (JobMasterNotFound | InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("deleteJobLinkSendSetting() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}
}
