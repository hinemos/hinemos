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
import com.clustercontrol.grafana.util.JobLastRunQueryBuilder;
import com.clustercontrol.rest.endpoint.grafana.dto.GetJobLastRunTimeListRequest;
import com.clustercontrol.rest.endpoint.grafana.dto.GetJobLastRunTimeListResponse;
import com.clustercontrol.rest.endpoint.grafana.dto.GetJobLastRunTimeResponse;

public class JobLastRunTime {

	private static Logger m_log = Logger.getLogger(JobLastRunTime.class);

	public GetJobLastRunTimeListResponse getJobLastRunTimeList(GetJobLastRunTimeListRequest dtoReq) throws HinemosUnknown, InvalidSetting {
		
		GetJobLastRunTimeListResponse dtoRes = new GetJobLastRunTimeListResponse();
		
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			// ADMINISTRATORSロールに所属していないユーザーの場合は、オブジェクト権限チェック用に所属するロールのリストを取得
			List<String> roleIds = Collections.emptyList();
			Boolean isAdministrator = (Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR);
			if (isAdministrator == null || !isAdministrator) {
				String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
				// ユーザ情報が取得できない場合はそのままの実装を返す
				if (loginUser == null || "".equals(loginUser.trim())) {
					dtoRes.setJobLastRunTimeList(Collections.emptyList());
					return dtoRes;
				}
				roleIds = UserRoleCache.getRoleIdList(loginUser);
			}

			// クエリの生成
			String sql = new JobLastRunQueryBuilder().build(dtoReq, roleIds);

			// クエリの発行
			HinemosEntityManager em = jtm.getEntityManager();
			@SuppressWarnings("unchecked")
			List<Object[]> resultList = (List<Object[]>) em.createNativeQuery(sql).getResultList();

			// レスポンスデータの生成

			List<GetJobLastRunTimeResponse> jlrList = new ArrayList<GetJobLastRunTimeResponse>();
			for (Object[] o : resultList) {
				GetJobLastRunTimeResponse jlr = new GetJobLastRunTimeResponse();
				jlr.setTriggerInfo(o[0].toString());
				jlr.setJobId(o[1].toString());
				jlr.setLatestStartDate(o[2].toString());
				jlr.setLatestEndDate(o[3] == null ? "" : o[3].toString());
				jlrList.add(jlr);
			}
			dtoRes.setJobLastRunTimeList(jlrList);
		}
		
		return dtoRes;
	}
}
