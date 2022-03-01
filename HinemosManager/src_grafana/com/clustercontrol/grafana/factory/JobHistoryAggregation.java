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

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.util.UserRoleCache;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.grafana.util.JobHistoryQueryBuilder;
import com.clustercontrol.rest.endpoint.grafana.dto.GetJobHistoryAggregationCountResponse;
import com.clustercontrol.rest.endpoint.grafana.dto.GetJobHistoryAggregationRequest;
import com.clustercontrol.rest.endpoint.grafana.dto.GetJobHistoryAggregationResponse;
import com.clustercontrol.rest.endpoint.grafana.dto.enumtype.JobHistoryGroupByEnum;

public class JobHistoryAggregation {

	private static Logger m_log = Logger.getLogger(JobHistoryAggregation.class);

	public GetJobHistoryAggregationResponse getJobHistoryAggregation(GetJobHistoryAggregationRequest dtoReq) throws HinemosUnknown, InvalidSetting {
		
		GetJobHistoryAggregationResponse dtoRes = new GetJobHistoryAggregationResponse();
		
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

			// クエリの生成
			String sql = new JobHistoryQueryBuilder().build(dtoReq, roleIds);

			// クエリの発行
			HinemosEntityManager em = jtm.getEntityManager();
			@SuppressWarnings("unchecked")
			List<Object[]> resultList = (List<Object[]>) em.createNativeQuery(sql).getResultList();

			// レスポンスデータの生成
			List<String> groupByList = new ArrayList<String>();
			for(JobHistoryGroupByEnum g : dtoReq.getGroupBy()) {
				groupByList.add(g.getCode());
			}
			List<GetJobHistoryAggregationCountResponse> jcrList = new ArrayList<GetJobHistoryAggregationCountResponse>();
			
			dtoRes.setGroupBy(groupByList.get(0));
			// groupBy 指定が 1 つの場合
			if (groupByList.size() == 1) {
				for (Object[] o : resultList) {
					GetJobHistoryAggregationCountResponse jcr = new GetJobHistoryAggregationCountResponse();
					jcr.setValue(o[0] == null ? "" : o[0].toString());
					jcr.setCount((Long) o[1]);
					jcr.setNestGroupBy(null);
					jcrList.add(jcr);
				}
				dtoRes.setCountLists(jcrList);
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
						keyValue = o[0] == null ? "" : o[0].toString();
					}
					tmpList.add(o);
				}
				listGroupedByFirstValue.add(tmpList);

				for (List<Object[]> list : listGroupedByFirstValue) {
					GetJobHistoryAggregationCountResponse jcr = new GetJobHistoryAggregationCountResponse();
					GetJobHistoryAggregationResponse nestGroupBy = new GetJobHistoryAggregationResponse();
					List<GetJobHistoryAggregationCountResponse> innerJcrList = new ArrayList<GetJobHistoryAggregationCountResponse>();
					Long count = 0L;
					for (Object[] innerList : list) {
						GetJobHistoryAggregationCountResponse innerJcr = new GetJobHistoryAggregationCountResponse();
						innerJcr.setValue(innerList[1] == null ? "" : innerList[1].toString());
						innerJcr.setCount((Long) innerList[2]);
						innerJcr.setNestGroupBy(null);
						count = Long.sum(count, (Long) innerList[2]);
						innerJcrList.add(innerJcr);
					}
					nestGroupBy.setGroupBy(groupByList.get(1));
					nestGroupBy.setCountLists(innerJcrList);
					jcr.setNestGroupBy(nestGroupBy);
					jcr.setValue(!list.isEmpty() ? list.get(0)[0].toString() : null);
					jcr.setCount(count);
					jcrList.add(jcr);
				}
				dtoRes.setCountLists(jcrList);
			}
		}
		
		return dtoRes;
	}
}
