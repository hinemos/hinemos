/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.factory;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.ReportingNotFound;
import com.clustercontrol.reporting.bean.ReportingInfo;
import com.clustercontrol.rest.endpoint.reporting.dto.CreateReportingFileRequest;
import com.clustercontrol.platform.util.reporting.ExecReportingProcess;

/**
 * 
 * レポーティング機能が提供する操作を実行するクラスです。
 * 
 * @version 4.1.2
 *
 */
public class OperationReporting {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(OperationReporting.class);

	/**
	 * @param reportId
	 * @return 作成されるレポートファイル名のリスト
	 */
	public List<String> runReporting(String reportId) {
		return runReporting(reportId, null);
	}

	/**
	 * @param reportId
	 * @param dtoReq
	 * @return 作成されるレポートファイル名のリスト
	 */
	public List<String> runReporting(String reportId, CreateReportingFileRequest dtoReq) {

		List<String> ret = null;
		m_log.debug("OperationReporting() : reportId = " + reportId);

		try {
			ReportingInfo info = new SelectReportingInfo().getReportingInfo(reportId);
			ret = ExecReportingProcess.execute(info, dtoReq);
		} catch (ReportingNotFound e) {
			// 何もしない
		} catch (InvalidRole e) {
			// 何もしない
		} catch (HinemosUnknown e) {
			// 何もしない
		}

		return ret;
	}
}
