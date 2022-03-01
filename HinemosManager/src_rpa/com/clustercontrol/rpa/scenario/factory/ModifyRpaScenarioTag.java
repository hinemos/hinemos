/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.scenario.factory;

import jakarta.persistence.EntityExistsException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.RpaScenarioTagDuplicate;
import com.clustercontrol.fault.RpaScenarioTagNotFound;
import com.clustercontrol.rpa.scenario.model.RpaScenarioTag;
import com.clustercontrol.rpa.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;

/**
 * RPAシナリオタグ情報を更新するクラス
 *
 * @version 7.0.0
 * @since 7.0.0
 */
public class ModifyRpaScenarioTag {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( ModifyRpaScenarioTag.class );

	/**
	 * RPAシナリオタグ情報を作成します。
	 */
	public void add(RpaScenarioTag data, String name) throws RpaScenarioTagDuplicate {
		long now = HinemosTime.currentTimeMillis();

		//エンティティBeanを作る
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// 重複チェック
			jtm.checkEntityExists(RpaScenarioTag.class, data.getTagId());
			data.setRegDate(now);
			data.setRegUser(name);
			data.setUpdateDate(now);
			data.setUpdateUser(name);
			
			em.persist(data);
			
			// JPAではDML処理順序が保障されないため、フラッシュ実行
			jtm.flush();
		} catch (EntityExistsException e) {
			m_log.info("add() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new RpaScenarioTagDuplicate(e.getMessage(),e);
		}
	}
	
	/**
	 * RPAシナリオタグ情報を変更します。
	 */
	public void modify(RpaScenarioTag data, String name) throws RpaScenarioTagNotFound, InvalidRole {

		//RPAシナリオタグ情報を取得
		RpaScenarioTag rpaScenarioTag = QueryUtil.getRpaScenarioTagPK(data.getTagId(), ObjectPrivilegeMode.MODIFY);

		//RPAシナリオタグ情報を更新
		rpaScenarioTag.setTagName(data.getTagName());
		rpaScenarioTag.setDescription(data.getDescription());
		rpaScenarioTag.setUpdateDate(HinemosTime.currentTimeMillis());
		rpaScenarioTag.setUpdateUser(name);
	}
	
	/**
	 * RPAシナリオタグ情報を削除します。
	 */
	public void delete(String tagId) throws InvalidRole, HinemosUnknown {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// RPAシナリオタグ情報を検索し取得
			RpaScenarioTag entity = null;
			try {
				entity = QueryUtil.getRpaScenarioTagPK(tagId, ObjectPrivilegeMode.MODIFY);
			} catch (RpaScenarioTagNotFound e) {
				throw new HinemosUnknown(e.getMessage(), e);
			}

			//RPAシナリオタグ情報を削除
			em.remove(entity);

			// JPAではDML処理順序が保障されないため、フラッシュ実行
			jtm.flush();
		}
	}
	
	/**
	 * RPAシナリオタグ情報の子タグのタグ階層を一斉更新します。
	 * 設定インポート時に使用
	 * 
	 */
	public List<String> modifyChildTagsPath(RpaScenarioTag data, String name) throws RpaScenarioTagNotFound, InvalidRole {
		List<String> updateTagIdList = new ArrayList<>();
		List<RpaScenarioTag> list = QueryUtil.getAllRpaScenarioTag();
		for(RpaScenarioTag tag : list){
			String[] array = tag.getTagPath().split("\\\\");
			List<String> pathList = new ArrayList<String>(Arrays.asList(array));
			
			for(String path : array){
				if(path.equals(data.getTagId())){
					String updatePath = data.getTagPath() + "\\" + String.join("\\", pathList);
					updateTagIdList.add(tag.getTagId());
					
					//RPAシナリオタグ情報を取得
					RpaScenarioTag rpaScenarioTag = QueryUtil.getRpaScenarioTagPK(tag.getTagId(), ObjectPrivilegeMode.MODIFY);

					//RPAシナリオタグ情報を更新
					rpaScenarioTag.setTagPath(updatePath);
					rpaScenarioTag.setUpdateDate(HinemosTime.currentTimeMillis());
					rpaScenarioTag.setUpdateUser(name);
					
					break;
				} else {
					pathList.remove(path);
				}
			}
		}
		return updateTagIdList;
	}
}