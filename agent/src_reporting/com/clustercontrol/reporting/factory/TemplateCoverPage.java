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
public class TemplateCoverPage extends TemplateBase {

	private static Log m_log = LogFactory.getLog(TemplateCoverPage.class);
	
	@Override
	public List<JasperPrint> getReport(Integer pageOffset) {
		
		m_log.info("TemplateCoverPage( " + m_templateId + ") : getReport() start.");
		
		List<JasperPrint> jpList = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();
		JasperPrint jp = null;
		
		// プロパティデータすべてjrxmlに渡すパラメータとして定義
		params.putAll(m_propertiesMap);
		// タイトルだけ、設定に登録されている内容で上書き
		params.put(ReportingConstant.TITLE_MAIN_KEY_VALUE, ReportUtil.getReportTitle());
		
		try {
			String fmtStr = isDefine(ReportingConstant.DATE_FORMAT_KEY_VALUE, Messages.getString("COMMON_DATE_FORMAT"));
			SimpleDateFormat fmt = new SimpleDateFormat(fmtStr);
			JREmptyDataSource ds = new JREmptyDataSource();
			JasperReport report = JasperCompileManager.compileReport(m_jrxmlFilePath);
			m_log.debug("check main.title = " + params.get(ReportingConstant.TITLE_MAIN_KEY_VALUE));
			params.put("COVER_DATE", fmt.format(new Date()));
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
		
		m_log.info("TemplateCoverPage( " + m_templateId + ") : getReport() complated.");
		
		return jpList;
	}
}
