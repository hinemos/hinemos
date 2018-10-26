/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.factory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.reporting.ReportUtil;
import com.clustercontrol.reporting.bean.ReportingConstant;
import com.clustercontrol.util.Messages;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

/**
 * 表紙の作成を行うクラス
 */
public class TemplateIndexPage extends TemplateBase {

	private static Log m_log = LogFactory.getLog(TemplateIndexPage.class);

	private static final String CONTENTS_KEY_VALUE = "contents.item";
	private static final String PERIOD_KEY_VALUE = "period.item";
	private static final String TARGET_NODES_KEY_VALUE = "target.nodes.item";
	
	
	@Override
	public List<JasperPrint> getReport(Integer pageOffset) {
		
		m_log.debug("TemplateIndexPage( " + m_templateId + ") : getReport() start.");
		
		List<JasperPrint> jpList = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();
		JasperPrint jp = null;
		
		// プロパティデータすべてjrxmlに渡すパラメータとして定義
		params.putAll(m_propertiesMap);
		
		String contents = ReportUtil.getContentsString();
		String fmtStr = isDefine(ReportingConstant.DATE_FORMAT_KEY_VALUE, Messages.getString("COMMON_DATE_FORMAT"));
		SimpleDateFormat fmt = new SimpleDateFormat(fmtStr);
		String period = fmt.format(m_startDate) + " - " + fmt.format(new Date(m_endDate.getTime() - 1));
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < m_nodes.size(); i++) {
			sb.append("- ");
			sb.append(m_nodes.get(i)[1]);
			sb.append(" (");
			sb.append(m_nodes.get(i)[0]);
			sb.append(")\n");
		}
		sb.deleteCharAt(sb.length() - 1);
		String nodesStr = sb.toString();
		
		try {
			JREmptyDataSource ds = new JREmptyDataSource();
			JasperReport report = JasperCompileManager.compileReport(m_jrxmlFilePath);
			
			// 定数
			String contentsItem = isDefine(CONTENTS_KEY_VALUE, Messages.getString("COMMON_REPORT_CONTENTS_ITEM"));
			String periodItem = isDefine(PERIOD_KEY_VALUE, Messages.getString("COMMON_REPORT_PERIOD_ITEM"));
			String targetNodesItem = isDefine(TARGET_NODES_KEY_VALUE, Messages.getString("COMMON_REPORT_TARGET_NODES_ITEM"));
			
			params.put(CONTENTS_KEY_VALUE, contentsItem);
			params.put(PERIOD_KEY_VALUE, periodItem);
			params.put(TARGET_NODES_KEY_VALUE, targetNodesItem);
			
			// 変数
			params.put("REPORT_CONTENTS", contents);
			params.put("REPORT_PERIOD", period);
			params.put("REPORT_TARGET_NODES", nodesStr);
			params.put("PAGE_OFFSET", pageOffset);
			params.put("LOGO_FILENAME", ReportUtil.getLogoFilePath());
			params.put("SHOW_PAGE", ReportUtil.isPageValid());
			jp = JasperFillManager.fillReport(report, params, ds);
		} catch (Exception e) {
			m_log.error(e, e);
		}
		
		if(jp != null) {
			jpList.add(jp);
			setCurPage(pageOffset + jp.getPages().size());
		}
		
		m_log.info("TemplateIndexPage( " + m_templateId + ") : getReport() completed.");

		return jpList;
	}
	
}
