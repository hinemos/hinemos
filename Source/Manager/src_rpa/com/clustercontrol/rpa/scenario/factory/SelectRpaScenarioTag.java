/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.scenario.factory;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.RpaScenarioTagNotFound;
import com.clustercontrol.rpa.scenario.model.RpaScenarioTag;
import com.clustercontrol.rpa.scenario.model.RpaScenarioTagRelation;
import com.clustercontrol.rpa.util.QueryUtil;

/**
 * RPAシナリオタグ情報を検索するクラス
 */
public class SelectRpaScenarioTag {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( SelectRpaScenarioTag.class );

	/**
	 * RPAシナリオタグ情報を返します。
	 */
	public RpaScenarioTag getRpaScenarioTag(String tagId) throws RpaScenarioTagNotFound, InvalidRole {
		
		// RPAシナリオタグ情報を取得
		RpaScenarioTag entity = null;
		entity = QueryUtil.getRpaScenarioTagPK(tagId);
		
		return entity;
	}
	
	/**
	 * RPAシナリオタグ情報の一覧を返します。
	 */
	public ArrayList<RpaScenarioTag> getRpaScenarioTagList(String ownerRoleId) {
		m_log.debug("getRpaScenarioTagList()");
		
		List<RpaScenarioTag> entityList = null;
		if(ownerRoleId == null || ownerRoleId.isEmpty()) {
			// ログインユーザが参照権限がある全シナリオタグを取得
			entityList = QueryUtil.getAllRpaScenarioTag();
		} else {
			// オーナーロールIDを条件として全シナリオタグを取得
			entityList = QueryUtil.getAllRpaScenarioTag_OR(ownerRoleId);
		}
		ArrayList<RpaScenarioTag> infoList = new ArrayList<>();
		for (RpaScenarioTag entity: entityList) {
			infoList.add(entity);
		}
		
		return infoList;
	}
	
	/**
	 * RPAシナリオタグのリレーション情報の一覧を返します。
	 */
	public ArrayList<RpaScenarioTagRelation> getRpaScenarioTagRelationList() {
		m_log.debug("getRpaScenarioTagList()");
		
		List<RpaScenarioTagRelation> entityList = null;
		entityList = QueryUtil.getAllRpaScenarioTagRelation();
		
		ArrayList<RpaScenarioTagRelation> infoList = new ArrayList<>();
		for (RpaScenarioTagRelation entity: entityList) {
			infoList.add(entity);
		}
		
		return infoList;
	}
	
	/**
	 * 対象のシナリオタグに紐づくRPAシナリオが存在する場合、紐づいているシナリオIDを返します。
	 */
	public List<String> checkRpaScenarioTagRelation(List<String> tagIds) {
		return QueryUtil.getRpaScenarioTagIdFindByScenarioTagRelation(tagIds);
	}
}
