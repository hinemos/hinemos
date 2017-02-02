/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.factory;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.IconFileDuplicate;
import com.clustercontrol.fault.IconFileNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.jobmanagement.bean.JobmapIconImage;
import com.clustercontrol.jobmanagement.model.JobmapIconImageEntity;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;

/**
 * ジョブマップ関連の情報を操作するクラスです。
 *
 * @version 5.1.0
 */
public class ModifyJobmap {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( ModifyJobmap.class );

	/**
	 * ジョブマップ用アイコン情報を新規登録・更新します。
	 *
	 * @param JobmapIconImage ジョブマップ用アイコン情報
	 * @param modifyUserId 作業ユーザID
	 * @param isNew true:新規登録、false：更新
	 * @throws HinemosUnknown
	 * @throws IconFileNotFound
	 * @throws IconFileDuplicate
	 * @throws InvalidRole
	 */
	public void modifyJobmapIconImage(final JobmapIconImage info, String modifyUserId, boolean isNew)
			throws HinemosUnknown, IconFileNotFound, IconFileDuplicate, InvalidRole {
		m_log.debug("modifyJobmapIconImage() : iconId=" + info.getIconId() + ", isNew=" + isNew);

		JobmapIconImageEntity jobmapIconImageEntity = null;
		try {
			//最終更新日時を設定
			long currentTimeMillis = HinemosTime.currentTimeMillis();
			JpaTransactionManager jtm = new JpaTransactionManager();
			if (isNew) {
				// 新規登録
				// 重複チェック
				jtm.checkEntityExists(JobmapIconImageEntity.class, info.getIconId());
				// 情報設定
				jobmapIconImageEntity = new JobmapIconImageEntity(info.getIconId());
				jobmapIconImageEntity.setOwnerRoleId(info.getOwnerRoleId());
				jobmapIconImageEntity.setRegUser(modifyUserId);
				jobmapIconImageEntity.setRegDate(currentTimeMillis);
			} else {
				// 更新
				// インスタンスの取得
				jobmapIconImageEntity = QueryUtil.getJobmapIconImagePK(info.getIconId(), ObjectPrivilegeMode.MODIFY);
			}
			// 情報設定
			jobmapIconImageEntity.setFiledata(info.getFiledata());
			jobmapIconImageEntity.setDescription(info.getDescription());
			jobmapIconImageEntity.setUpdateUser(modifyUserId);
			jobmapIconImageEntity.setUpdateDate(currentTimeMillis);

		} catch (IconFileNotFound | InvalidRole e) {
			throw e;
		} catch (EntityExistsException e) {
			m_log.info("modifyJobmapIconImage() failure to add a icon image. "
					+ "iconId is duplicated. (iconId = " + info.getIconId() + ")");
			throw new IconFileDuplicate(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("modifyJobmapIconImage() failure to add a icon image. (iconId = " + info.getIconId() + ")"
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * ジョブマップ用アイコン情報を削除します。
	 *
	 * @param iconId アイコンID
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws ObjectPrivilege_InvalidRole
	 */
	public void deleteJobmapIconImage(final String iconId) throws HinemosUnknown, IconFileNotFound, ObjectPrivilege_InvalidRole {
		// ジョブマップ用アイコンを削除
		m_log.debug("deleteJobmapIconImage() : iconId = " + iconId);

		JpaTransactionManager jtm = new JpaTransactionManager();
		HinemosEntityManager em = jtm.getEntityManager();
		// DBのジョブマップ用アイコン情報を削除
		try {
			//削除対象を検索
			JobmapIconImageEntity jobmapIconImageEntity = em.find(JobmapIconImageEntity.class, iconId,
					ObjectPrivilegeMode.MODIFY);
			if (jobmapIconImageEntity == null) {
				IconFileNotFound e = new IconFileNotFound("JobmapIconImageEntity.findByPrimaryKey");
				m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			//削除
			em.remove(jobmapIconImageEntity);
		} catch (IconFileNotFound | ObjectPrivilege_InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("deleteJobmapIconImage() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}
}
