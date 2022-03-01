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
import com.clustercontrol.grafana.util.EventQueryBuilder;
import com.clustercontrol.grafana.util.QueryUtil;
import com.clustercontrol.rest.endpoint.grafana.dto.GetEventAggregationCountResponse;
import com.clustercontrol.rest.endpoint.grafana.dto.GetEventAggregationRequest;
import com.clustercontrol.rest.endpoint.grafana.dto.GetEventAggregationResponse;
import com.clustercontrol.rest.endpoint.grafana.dto.enumtype.EventFacilityTargetEnum;
import com.clustercontrol.rest.endpoint.grafana.dto.enumtype.EventGroupByEnum;

public class EventAggregation {

	private static Logger m_log = Logger.getLogger(EventAggregation.class);

	public GetEventAggregationResponse getEventAggregation(GetEventAggregationRequest dtoReq) throws HinemosUnknown, InvalidSetting {
		
		GetEventAggregationResponse dtoRes = new GetEventAggregationResponse();
		
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
				EventFacilityTargetEnum facilityTargetOpt = dtoReq.getFilter().getFacilityTarget();
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
			String sql = new EventQueryBuilder().build(dtoReq, roleIds, useTmpTable);

			// クエリの発行
			HinemosEntityManager em = jtm.getEntityManager();
			@SuppressWarnings("unchecked")
			List<Object[]> resultList = (List<Object[]>) em.createNativeQuery(sql).getResultList();

			// レスポンスデータの生成
			List<String> groupByList = new ArrayList<String>();
			for(EventGroupByEnum g : dtoReq.getGroupBy()) {
				groupByList.add(g.getCode());
			}
			List<GetEventAggregationCountResponse> ecrList = new ArrayList<GetEventAggregationCountResponse>();

			if (useTmpTable) {
				// 一時テーブル削除
				QueryUtil.dropTargetCollectorIdsTable();
			}

			dtoRes.setGroupBy(groupByList.get(0));
			// groupBy 指定が 1 つの場合
			if (groupByList.size() == 1) {
				for (Object[] o : resultList) {
					GetEventAggregationCountResponse ecr = new GetEventAggregationCountResponse();
					ecr.setValue(o[0].toString());
					ecr.setCount((Long) o[1]);
					ecr.setNestGroupBy(null);
					ecrList.add(ecr);
				}
				dtoRes.setCountLists(ecrList);
			} else if (groupByList.size() == 2) {
				// groupBy指定が2つの時
				String keyValue = "";
				List<Object[]> tmpList = new ArrayList<Object[]>();
				List<List<Object[]>> listGroupedByFirstValue = new ArrayList<List<Object[]>>();
				for (Object[] o : resultList) {
					if (!keyValue.equals(o[0].toString())) {
						List<Object[]> innerList = new ArrayList<Object[]>(tmpList);
						if(!innerList.isEmpty()) {
							listGroupedByFirstValue.add(innerList);
						}
						tmpList.clear();
						keyValue = o[0].toString();
					}
					tmpList.add(o);
				}
				listGroupedByFirstValue.add(tmpList);

				for (List<Object[]> list : listGroupedByFirstValue) {
					GetEventAggregationCountResponse ecr = new GetEventAggregationCountResponse();
					GetEventAggregationResponse nestGroupBy = new GetEventAggregationResponse();
					List<GetEventAggregationCountResponse> innerEcrList = new ArrayList<GetEventAggregationCountResponse>();
					Long count = 0L;
					for (Object[] innerList : list) {
						GetEventAggregationCountResponse innerEcr = new GetEventAggregationCountResponse();
						innerEcr.setValue(innerList[1].toString());
						innerEcr.setCount((Long) innerList[2]);
						innerEcr.setNestGroupBy(null);
						count = Long.sum(count, (Long) innerList[2]);
						innerEcrList.add(innerEcr);
					}
					nestGroupBy.setGroupBy(groupByList.get(1));
					nestGroupBy.setCountLists(innerEcrList);
					ecr.setNestGroupBy(nestGroupBy);
					ecr.setValue(!list.isEmpty() ? list.get(0)[0].toString() : null);
					ecr.setCount(count);
					ecrList.add(ecr);
				}
				dtoRes.setCountLists(ecrList);
			}

			jtm.commit();

		}

		return dtoRes;
	}
}
