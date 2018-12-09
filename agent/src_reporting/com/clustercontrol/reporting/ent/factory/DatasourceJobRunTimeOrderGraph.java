/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.ent.factory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.reporting.ReportUtil;
import com.clustercontrol.reporting.bean.ReportingConstant;
import com.clustercontrol.reporting.ent.session.ReportingJobEntControllerBean;
import com.clustercontrol.reporting.ent.util.PropertiesConstant;
import com.clustercontrol.reporting.factory.DatasourceBase;
import com.clustercontrol.reporting.fault.ReportingPropertyNotFound;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRCsvDataSource;


/**
 * コマンドジョブごとの実行時間グラフ（実行時間上位一覧の順）レポート作成元となるデータソースを作成するクラス
 * 
 * @version 5.0.b
 * @since 5.0.b
 */
public class DatasourceJobRunTimeOrderGraph extends DatasourceBase {
	private static Log m_log = LogFactory.getLog(DatasourceJobRunTimeOrderGraph.class);

	private List<JobOrderInfo> m_jobOrderList = null;
	
	private static class JobOrderInfo {
		JobOrderInfo(String jobUnitId, String jobId) {
			this.jobUnitId = jobUnitId;
			this.jobId = jobId;
		}

		String jobUnitId;
		String jobId;
	}
	
	/**
	 * データソース（CSVファイル）をHinemos DBから生成する
	 */
	@Override
	public HashMap<String, Object> createDataSource(int num) throws ReportingPropertyNotFound {
		if(m_propertiesMap.get(SUFFIX_KEY_VALUE+"."+num).isEmpty()) {
			throw new ReportingPropertyNotFound(SUFFIX_KEY_VALUE+"."+num + " is not defined.");
		}
		String suffix = m_propertiesMap.get(SUFFIX_KEY_VALUE+"."+num);
		String dayString = new SimpleDateFormat("MMdd").format(m_startDate);
		
		String csvFileName = "";
		HashMap<String, Object> retMap = new HashMap<String, Object>();
		
		int orderNum = Integer.parseInt(isDefine(PropertiesConstant.JOB_ORDER_NUM_KEY, PropertiesConstant.JOB_ORDER_NUM_DEFAULT));
		int jobGraphLineMax = Integer.parseInt(isDefine(PropertiesConstant.JOB_GRAPH_LINE_MAX_KEY, PropertiesConstant.GRAPH_LINE_MAX_DEFAULT));
		
		String jobUnitRegex = isDefine(PropertiesConstant.JOB_UNIT_REGEX_KEY_+"."+num, "%%");		
		String jobIdRegex = isDefine(PropertiesConstant.JOB_ID_REGEX_KEY_+"."+num, "%%");
		String jobIdRegexExc = isDefine(PropertiesConstant.JOB_ID_REGEX_EXC_KEY_+"."+num, "");
		
		String jobOrderKey = isDefine(PropertiesConstant.JOB_ORDER_KEY_KEY_ + "." + num, PropertiesConstant.ORDER_KEY_MAX);
		// 設定値がどの値にも当てはまらない場合は、maxをデフォルトとする
		if( !(jobOrderKey.equals(PropertiesConstant.ORDER_KEY_MAX) || jobOrderKey.equals(PropertiesConstant.ORDER_KEY_AVG) || jobOrderKey.equals(PropertiesConstant.ORDER_KEY_DIFF)) ) {
			jobOrderKey = PropertiesConstant.ORDER_KEY_MAX;
		}
		
		// グラフタイトルの取得
		String jobChartTitle = isDefine(PropertiesConstant.JOB_GRAPH_CHART_TITLE_KEY, "job runtime");
		
		int graphDiv = Integer.parseInt(isDefine(PropertiesConstant.JOB_GRAPH_DIVIDER_KEY, PropertiesConstant.JOB_GRAPH_DIVIDER_DEFAULT));
		String graphLabel = isDefine(PropertiesConstant.JOB_GRAPH_LABEL_KEY, PropertiesConstant.JOB_GRAPH_LABEL_DEFAULT);
		
		// get data from Hinemos DB
		BufferedWriter bw = null;
		try {
			
			// グラフ作成対象となるジョブを抽出する
			String ownerRoleId = null;
			if (!"ADMINISTRATORS".equals(ReportUtil.getOwnerRoleId())) {
				ownerRoleId = ReportUtil.getOwnerRoleId();
			}
			m_jobOrderList = new ArrayList<JobOrderInfo>();
			
			ReportingJobEntControllerBean controller = new ReportingJobEntControllerBean();
			List<Object[]> summaryJobSessionJobList = controller.getSummaryJobSessionJob(m_startDate.getTime(), m_endDate.getTime(), jobUnitRegex, jobIdRegex, jobIdRegexExc, jobOrderKey, ownerRoleId, orderNum);
			
			if (summaryJobSessionJobList != null) {
				for (Object[] objects : summaryJobSessionJobList) {
					String jobunitId = objects[0].toString();
					String jobId = objects[1].toString();
					
					m_jobOrderList.add(new JobOrderInfo(jobunitId, jobId));
				}
			}
			
			// 抽出したジョブ情報を基に実行時間を抽出する
			String[] columns = { "graph_number", "jobunit_id", "job_id", "date_time", "elapsed_time" };
			String columnsStr = ReportUtil.joinStrings(columns, ",");
			
			int count = 0;
			int graphNum = 0;
			File csv = null;
			
			for(JobOrderInfo info : m_jobOrderList) {
				
				// グラフごとのCSVファイルの作成（カウントアップ前）
				if (count % jobGraphLineMax == 0) {
					csvFileName = ReportUtil.getCsvFileNameForTemplateType(m_templateId, suffix + "_" + dayString + "_" + String.format("%03d", ++graphNum));
					
					// CSVファイルが既に存在している場合は、データ作成処理をスキップ
					if(new File(csvFileName).exists()){
						m_log.info("File : " + csvFileName + " is exists.");
					} else {
						m_log.info("output csv: " + csvFileName);
						
						csv = new File(csvFileName);
						bw = new BufferedWriter(new FileWriter(csv, false));
						bw.write(columnsStr);
						bw.newLine();
					}
				}
				
				List<JobSessionJobEntity> jobSessionJobList = controller.getJobSessionJobByJobunitIdAndJobId(m_startDate.getTime(), m_endDate.getTime(), info.jobUnitId, info.jobId, ownerRoleId);
				
				Calendar timeCal = Calendar.getInstance();
				Calendar baseCal = Calendar.getInstance();
				baseCal.setTimeInMillis(0L);
				Long maxTime = null; // 初回判定のため、初期値には　null を格納する
				
				// 1970-01-01 00:00:00 の作成
				
				if (jobSessionJobList != null) {
					// write to csv file
					for (JobSessionJobEntity jobSessionJobEntity : jobSessionJobList) {
						
						String jobunitId = jobSessionJobEntity.getId().getJobunitId();
						String jobId = jobSessionJobEntity.getId().getJobId();
						Timestamp startTime = new Timestamp(jobSessionJobEntity.getStartDate());
						Long elapsedTime = jobSessionJobEntity.getEndDate() - jobSessionJobEntity.getStartDate();
						
						timeCal.setTimeInMillis(startTime.getTime());
						
						// 日付を比較し、1日(86400000ミリ秒)以上はなれた場合、baseCalを1日ずらして、値を格納する(日が進んだ場合)	
						if(timeCal.getTimeInMillis() - baseCal.getTimeInMillis() > 86400000) {
							
							// ベースカレンダーが初期設定の場合は、最初の時刻情報で初期化する
							if(baseCal.getTimeInMillis() == 0L) {
								m_log.info("baseCalendar initialise.");	
								
								baseCal.setTimeInMillis(timeCal.getTimeInMillis());
								baseCal.set(Calendar.HOUR_OF_DAY, 0);
								baseCal.set(Calendar.MINUTE, 0);
								baseCal.set(Calendar.SECOND, 0);
								baseCal.set(Calendar.MILLISECOND, 0);
							}
							else {
								// 日付を進める際に、値が格納されている場合は書き出す
								if(maxTime != null) {
									if (maxTime > 0) {
										maxTime = maxTime / graphDiv;
									}
									
									bw.write(graphNum + "," + jobunitId + "," + jobId + "," + 
											new Timestamp(baseCal.getTimeInMillis()) + "," + 
											maxTime);
									bw.newLine();
								}
									
								// 日付を1日足す
								baseCal.add(Calendar.DAY_OF_MONTH, 1);
								
								// 日付が1日以上離れている場合は、間の日時に0を埋める
								while(timeCal.getTimeInMillis() - baseCal.getTimeInMillis() > 86400000) {
									
									// 1日足してもまだ1日以上を開いている場合は、間の値を""(空欄)で埋める
									bw.write(graphNum + "," + jobunitId + "," + jobId + "," + 
											new Timestamp(baseCal.getTimeInMillis()) + "," + 
											"");
									bw.newLine();
									
									// 日付を1日足す
									baseCal.add(Calendar.DAY_OF_MONTH, 1);
									
								}
								
							}
							
							// ベース時刻の初期化
							maxTime = -1L;
						}
						
						// 日別で最大実行時間を抽出する
						if (elapsedTime > maxTime) {
							maxTime = elapsedTime;
						}
					}
					
					// 最終日の時刻を抽出する
					if(maxTime > -1) {
						if (maxTime > 0) {
							maxTime = maxTime / graphDiv;
						}
						
						bw.write(graphNum + "," + info.jobUnitId + "," + info.jobId + "," + 
								new Timestamp(baseCal.getTimeInMillis()) + "," + 
								maxTime);
						bw.newLine();
					}
				}
				
				// 処理グラフ数のカウントアップ
				count++;
				
				// グラフごとのCSVファイルにて、上限に達した場合にクローズする（カウントアップ後）
				if (count % jobGraphLineMax == 0) {
				
					bw.close();
					
					// 生成されたcsvファイルをもとにDatasourceを生成
					JRCsvDataSource ds = new JRCsvDataSource(csvFileName);
					ds.setUseFirstRowAsHeader(true);
				
					retMap.put("chart.title."+num, jobChartTitle+" "+num);
					retMap.put("label."+num, graphLabel);
					retMap.put(ReportingConstant.STR_DS+"_"+num++, ds);
				}
			}
			
			// 最後の確認
			if (count % jobGraphLineMax != 0) {
			
				bw.close();
				
				// 生成されたcsvファイルをもとにDatasourceを生成
				JRCsvDataSource ds = new JRCsvDataSource(csvFileName);
				ds.setUseFirstRowAsHeader(true);
			
				retMap.put("chart.title."+num, jobChartTitle+" "+num);
				retMap.put("label."+num, graphLabel);
				retMap.put(ReportingConstant.STR_DS+"_"+num++, ds);
			}
		} catch (IOException | JRException e) {
			m_log.error(e, e);
		} catch (Exception e) {
			m_log.error(e, e);
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					m_log.warn("bw close failure : ", e);
				}
			}
		}
		
		return retMap;
	}
}
