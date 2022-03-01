/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.factory;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.ReportingNotFound;
import com.clustercontrol.jobmanagement.bean.JobLinkMessageId;
import com.clustercontrol.notify.bean.NotifyTriggerType;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.reporting.model.ReportingInfoEntity;
import com.clustercontrol.reporting.util.QueryUtil;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.Messages;


/**
 * 監視管理に通知するクラスです。
 *
 * @version 4.1.2
 */
public class Notice {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( Notice.class );

	/**
	 * レポートIDからレポーティング通知情報を取得し、<BR>
	 * レポーティング通知情報と終了状態を基に、ログ出力情報作成し、監視管理に通知します。
	 * 
	 * @param reportId レポートID
	 * @param outFile 出力レポートファイル名(フルパス)
	 * @param type 終了状態
	 * @param result レポート実行結果
	 * @return 通知情報
	 * @throws HinemosUnknown 
	 * 
	 * @see com.clustercontrol.bean.EndStatusConstant
	 * @see com.clustercontrol.monitor.message.LogOutputNotifyInfo
	 */
	public OutputBasicInfo createOutputBasicInfo(String reportId, String outFile, Integer type, int result) throws HinemosUnknown {
		m_log.info("createOutputBasicInfo() : reportId=" + reportId + ", type=" + type + ", result=" + result);

		OutputBasicInfo rtn = null;

		//ReportingInfoを取得する
		ReportingInfoEntity info = null;
		try {
			info = QueryUtil.getReportingInfoPK(reportId);

			if(info.getNotifyGroupId() != null && info.getNotifyGroupId().length() > 0){
				//通知する

				//通知情報作成
				rtn = new OutputBasicInfo();

				// 通知グループID
				rtn.setNotifyGroupId(info.getNotifyGroupId());
				// ジョブ連携メッセージID
				rtn.setJoblinkMessageId(JobLinkMessageId.getId(NotifyTriggerType.REPORTING,
						HinemosModuleConstant.REPORTING, reportId));
				//プラグインID
				rtn.setPluginId(HinemosModuleConstant.REPORTING);
				//アプリケーション
				//rtn.setApplication(info.getApplication());
				rtn.setApplication("REPORTING");
				//監視項目ID
				rtn.setMonitorId(reportId);

				//ファシリティID
				rtn.setFacilityId(info.getFacilityId());
				try {
					String facilityPath = new RepositoryControllerBean().getFacilityPath(info.getFacilityId(), null);
					rtn.setScopeText(facilityPath);
				} catch (HinemosUnknown e) {
					m_log.warn(e.getMessage(), e);
					rtn.setScopeText(info.getFacilityId());
				}

				//メッセージID、メッセージ、オリジナルメッセージ
				if(type.intValue() == PriorityConstant.TYPE_INFO){
					String[] args1 = {reportId};
					rtn.setMessage(Messages.getString("MESSAGE_REPORTING_12", args1));
					//サブキーとしてファイル名のフルパスを指定(メール通知)
					rtn.setSubKey(outFile);
					//オリジナルメッセージとしてファイル名のフルパスを指定(コマンド通知)
					rtn.setMessageOrg(outFile);
				}
				else if(type.intValue() == PriorityConstant.TYPE_CRITICAL){
					String[] args1 = {reportId};
					rtn.setMessage(Messages.getString("MESSAGE_REPORTING_13", args1));
				}
				//notice.setMessageOrg("result: " + result);

				//重要度
				rtn.setPriority(type.intValue());
				//発生日時
				rtn.setGenerationDate(new Date().getTime());
			}
		} catch (ReportingNotFound e) {
			m_log.error(e,e);
		} catch (InvalidRole e) {
			m_log.error(e,e);
		} catch (Exception e) {
			m_log.error(e,e);
		}
		return rtn;
	}
}
