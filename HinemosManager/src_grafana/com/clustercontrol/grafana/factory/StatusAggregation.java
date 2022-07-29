/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.grafana.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.util.UserRoleCache;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.filtersetting.bean.FacilityTarget;
import com.clustercontrol.grafana.util.QueryUtil;
import com.clustercontrol.grafana.util.StatusQueryBuilder;
import com.clustercontrol.rest.endpoint.grafana.dto.GetStatusAggregationCountResponse;
import com.clustercontrol.rest.endpoint.grafana.dto.GetStatusAggregationRequest;
import com.clustercontrol.rest.endpoint.grafana.dto.GetStatusAggregationResponse;
import com.clustercontrol.rest.endpoint.grafana.dto.enumtype.StatusFacilityTargetEnum;
import com.clustercontrol.rest.endpoint.grafana.dto.enumtype.StatusGroupByEnum;

public class StatusAggregation {

	private static Logger m_log = Logger.getLogger(StatusAggregation.class);

	public GetStatusAggregationResponse getStatusAggregation(GetStatusAggregationRequest dtoReq) throws HinemosUnknown, InvalidSetting {
		
		GetStatusAggregationResponse dtoRes = new GetStatusAggregationResponse();
		
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			// ADMINISTRATORSロールに所属していないユーザーの場合は、オブジェクト権限チェック用に所属するロールのリストを取得
			List<String> roleIds = Collections.emptyList();
			Boolean isAdministrator = (Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR);
			if (isAdministrator == null || !isAdministrator) {
				String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
				// ユーザ情報が取得できない場合はそのままの実装を返す
				if (loginUser == null || "".equals(loginUser.trim())) {
					dtoRes.setCountLists(Collections.emptyList());
					return dtoRes;
				}
				roleIds = UserRoleCache.getRoleIdList(loginUser);
			}

			// Facility ID のリストを取得
			List<String> facilityIdList = new ArrayList<String>();
			String facilityId = dtoReq.getFilter().getFacilityId();
			boolean useTmpTable = false;
			if (!StringUtils.isEmpty(facilityId)) {
				StatusFacilityTargetEnum facilityTargetOpt = dtoReq.getFilter().getFacilityTarget();
				FacilityTarget target = FacilityTarget.fromCode(facilityTargetOpt.getCode());

				facilityIdList = target.expandFacilityIds(facilityId);
				useTmpTable = true;
			}

			jtm.begin();

			if (useTmpTable) {
				// 一時テーブル作成
				QueryUtil.createTargetFacilityIdsTable();

				// 一時テーブルに Facility ID を格納
				QueryUtil.insertTargetFacilityIds(facilityIdList);
			}

			// クエリの生成
			String sql = new StatusQueryBuilder().build(dtoReq, roleIds, useTmpTable);

			// クエリの発行
			HinemosEntityManager em = jtm.getEntityManager();
			@SuppressWarnings("unchecked")
			List<Object[]> resultList = (List<Object[]>) em.createNativeQuery(sql).getResultList();

			// レスポンスデータの生成
			List<String> groupByList = new ArrayList<String>();
			for(StatusGroupByEnum g : dtoReq.getGroupBy()) {
				groupByList.add(g.getCode());
			}
			List<GetStatusAggregationCountResponse> scrList = new ArrayList<GetStatusAggregationCountResponse>();
			

			if (useTmpTable) {
				// 一時テーブル削除
				QueryUtil.dropTargetCollectorIdsTable();
			}

			dtoRes.setGroupBy(groupByList.get(0));
			// groupBy 指定が 1 つの場合
			if (groupByList.size() == 1) {
				for (Object[] o : resultList) {
					GetStatusAggregationCountResponse scr = new GetStatusAggregationCountResponse();
					scr.setValue(o[0].toString());
					scr.setCount((Long) o[1]);
					scr.setNestGroupBy(null);
					scrList.add(scr);
				}
				dtoRes.setCountLists(scrList);
			}

			jtm.commit();
		}
		
		return dtoRes;
	}
}
