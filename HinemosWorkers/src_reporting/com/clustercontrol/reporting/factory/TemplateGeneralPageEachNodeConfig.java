package com.clustercontrol.reporting.factory;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.reporting.fault.ReportingPropertyNotFound;

import net.sf.jasperreports.engine.JasperPrint;

/**
 * 構成情報毎のページの作成を行うクラス
 * 
 * @version 6.2.b
 * @since 6.2.b
 */
public class TemplateGeneralPageEachNodeConfig extends TemplateGeneralPageOverall {

	private static Log m_log = LogFactory.getLog(TemplateGeneralPageEachNodeConfig.class);
	private List<String> m_nodeConfigList = new ArrayList<>();

	public TemplateGeneralPageEachNodeConfig() {
		m_nodeConfigList.add("OS");
		m_nodeConfigList.add("CPU");
		m_nodeConfigList.add("MEMORY");
		m_nodeConfigList.add("NIC");
		m_nodeConfigList.add("DISK");
		m_nodeConfigList.add("FILESYSTEM");
		m_nodeConfigList.add("PACKAGE");
		m_nodeConfigList.add("CUSTOM");
	}

	@Override
	public List<JasperPrint> getReport(Integer pageOffset) throws ReportingPropertyNotFound {

		List<JasperPrint> jpList = new ArrayList<>();
		List<JasperPrint> tmpList;

		for (String nodeConfig : m_nodeConfigList) {
			m_params.put("NODECONFIG_ID", nodeConfig);
			m_params.put("NODECONFIG_DISPLAY_ID", isDefine("node.config." + nodeConfig, nodeConfig));
			m_log.debug("getReport: nodeConfig: " + nodeConfig);
			tmpList = super.getReport(pageOffset);
			if (tmpList != null) {
				jpList.addAll(tmpList);
				for (JasperPrint jp : tmpList) {
					pageOffset += jp.getPages().size();
				}
			}
		}
		return jpList;
	}
}
