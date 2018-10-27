/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui.action;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.clustercontrol.ui.util.OptionUtil;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.utility.constant.HinemosModuleConstant;
import com.clustercontrol.utility.settings.ui.constant.XMLConstant;
import com.clustercontrol.utility.settings.ui.preference.SettingToolsXMLPreferencePage;
import com.clustercontrol.utility.util.MultiManagerPathUtil;

public class ReadXMLAction {
	public static class DiffFilePaths {
		public String filePath1;
		public String filePath2;
	}
	
	/**
	 * リソースストアからファイル名を取得
	 * 
	 */
	enum XMLFileName {
		PLATFORM_ACCESS_USER(XMLConstant.DEFAULT_XML_PLATFORM_ACCESS_USER, HinemosModuleConstant.STRING_PLATFORM_ACCESS_USER),
		PLATFORM_ACCESS_ROLE(XMLConstant.DEFAULT_XML_PLATFORM_ACCESS_ROLE, HinemosModuleConstant.STRING_PLATFORM_ACCESS_ROLE),
		PLATFORM_ACCESS_ROLE_USER(XMLConstant.DEFAULT_XML_PLATFORM_ACCESS_ROLE_USER, HinemosModuleConstant.STRING_PLATFORM_ACCESS_ROLE_USER),
		PLATFORM_ACCESS_SYSTEM_PRIVILEGE(XMLConstant.DEFAULT_XML_PLATFORM_ACCESS_SYSTEM_PRIVILEGE, HinemosModuleConstant.STRING_PLATFORM_ACCESS_SYSTEM_PRIVILEGE),
		PLATFORM_ACCESS_OBJECT_PRIVILEGE(XMLConstant.DEFAULT_XML_PLATFORM_ACCESS_OBJECT_PRIVILEGE, HinemosModuleConstant.STRING_PLATFORM_ACCESS_OBJECT_PRIVILEGE),
		PLATFORM_REPOSITORY_NODE(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE, HinemosModuleConstant.STRING_PLATFORM_REPOSITORY_NODE),
		PLATFORM_REPOSITORY_NODE_HOSTNAME(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE_HOSTNAME, HinemosModuleConstant.STRING_PLATFORM_REPOSITORY_NODE_HOSTNAME),
		PLATFORM_REPOSITORY_NODE_CPU(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE_CPU, HinemosModuleConstant.STRING_PLATFORM_REPOSITORY_NODE_CPU),
		PLATFORM_REPOSITORY_NODE_MEMORY(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE_MEMORY, HinemosModuleConstant.STRING_PLATFORM_REPOSITORY_NODE_MEMORY),
		PLATFORM_REPOSITORY_NODE_NETWORKINTERFACE(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE_NETWORKINTERFACE, HinemosModuleConstant.STRING_PLATFORM_REPOSITORY_NODE_NETWORKINTERFACE),
		PLATFORM_REPOSITORY_NODE_DISK(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE_DISK, HinemosModuleConstant.STRING_PLATFORM_REPOSITORY_NODE_DISK),
		PLATFORM_REPOSITORY_NODE_FS(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE_FS, HinemosModuleConstant.STRING_PLATFORM_REPOSITORY_NODE_FS),
		PLATFORM_REPOSITORY_NODE_DEVICE(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE_DEVICE, HinemosModuleConstant.STRING_PLATFORM_REPOSITORY_NODE_DEVICE),
		PLATFORM_REPOSITORY_NODE_VARIABLE(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE_VARIABLE, HinemosModuleConstant.STRING_PLATFORM_REPOSITORY_NODE_VARIABLE),
		PLATFORM_REPOSITORY_NODE_NOTE(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_NODE_NOTE, HinemosModuleConstant.STRING_PLATFORM_REPOSITORY_NODE_NOTE),
		PLATFORM_REPOSITORY_SCOPE(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_SCOPE, HinemosModuleConstant.STRING_PLATFORM_REPOSITORY_SCOPE),
		PLATFORM_REPOSITORY_SCOPE_NODE(XMLConstant.DEFAULT_XML_PLATFORM_REPOSITORY_SCOPE_NODE, HinemosModuleConstant.STRING_PLATFORM_REPOSITORY_SCOPE_NODE),
		PLATFORM_CALENDAR(XMLConstant.DEFAULT_XML_PLATFORM_CALENDAR, HinemosModuleConstant.STRING_PLATFORM_CALENDAR),
		PLATFORM_CALENDAR_PATTERN(XMLConstant.DEFAULT_XML_PLATFORM_CALENDAR_PATTERN, HinemosModuleConstant.STRING_PLATFORM_CALENDAR_PATTERN),
		PLATFORM_NOTIFY(XMLConstant.DEFAULT_XML_PLATFORM_NOTIFY, HinemosModuleConstant.STRING_PLATFORM_NOTIFY),
		PLATFORM_MAIL_TEMPLATE(XMLConstant.DEFAULT_XML_PLATFORM_MAIL_TEMPLATE, HinemosModuleConstant.STRING_PLATFORM_MAIL_TEMPLATE),
		PLATFORM_LOG_FORMAT(XMLConstant.DEFAULT_XML_PLATFORM_LOG_FORMAT, HinemosModuleConstant.STRING_PLATFORM_LOG_FORMAT),
		PLATFORM_HINEMOS_PROPERTY(XMLConstant.DEFAULT_XML_PLATFORM_HINEMOS_PROPERTY, HinemosModuleConstant.STRING_PLATFORM_HINEMOS_PROPERTY),
		MONITOR_AGENT(XMLConstant.DEFAULT_XML_MONITOR_AGENT, HinemosModuleConstant.STRING_MONITOR_AGENT),
		MONITOR_HTTP(XMLConstant.DEFAULT_XML_MONITOR_HTTP, HinemosModuleConstant.STRING_MONITOR_HTTP),
		MONITOR_HTTP_SCENARIO(XMLConstant.DEFAULT_XML_MONITOR_HTTP_SCENARIO, HinemosModuleConstant.STRING_MONITOR_HTTP_SCENARIO),
		MONITOR_PERFORMANCE(XMLConstant.DEFAULT_XML_MONITOR_PERFORMANCE, HinemosModuleConstant.STRING_MONITOR_PERFORMANCE),
		MONITOR_PING(XMLConstant.DEFAULT_XML_MONITOR_PING, HinemosModuleConstant.STRING_MONITOR_PING),
		MONITOR_PORT(XMLConstant.DEFAULT_XML_MONITOR_PORT, HinemosModuleConstant.STRING_MONITOR_PORT),
		MONITOR_PROCESS(XMLConstant.DEFAULT_XML_MONITOR_PROCESS, HinemosModuleConstant.STRING_MONITOR_PROCESS),
		MONITOR_SNMP(XMLConstant.DEFAULT_XML_MONITOR_SNMP, HinemosModuleConstant.STRING_MONITOR_SNMP),
		MONITOR_SNMPTRAP(XMLConstant.DEFAULT_XML_MONITOR_SNMPTRAP, HinemosModuleConstant.STRING_MONITOR_SNMPTRAP),
		MONITOR_SQL(XMLConstant.DEFAULT_XML_MONITOR_SQL, HinemosModuleConstant.STRING_MONITOR_SQL),
		MONITOR_CUSTOM(XMLConstant.DEFAULT_XML_MONITOR_CUSTOM, HinemosModuleConstant.STRING_MONITOR_CUSTOM),
		MONITOR_SYSTEMLOG(XMLConstant.DEFAULT_XML_MONITOR_SYSTEMLOG, HinemosModuleConstant.STRING_MONITOR_SYSTEMLOG),
		MONITOR_LOGFILE(XMLConstant.DEFAULT_XML_MONITOR_LOGFILE, HinemosModuleConstant.STRING_MONITOR_LOGFILE),
		MONITOR_WINSERVICE(XMLConstant.DEFAULT_XML_MONITOR_WINSERVICE, HinemosModuleConstant.STRING_MONITOR_WINSERVICE),
		MONITOR_WINEVENT(XMLConstant.DEFAULT_XML_MONITOR_WINEVENT, HinemosModuleConstant.STRING_MONITOR_WINEVENT),
		MONITOR_CUSTOMTRAP(XMLConstant.DEFAULT_XML_MONITOR_CUSTOMTRAP, HinemosModuleConstant.STRING_MONITOR_CUSTOMTRAP),
		MONITOR_JMX(XMLConstant.DEFAULT_XML_MONITOR_JMX, HinemosModuleConstant.STRING_MONITOR_JMX),
		MONITOR_LOGCOUNT(XMLConstant.DEFAULT_XML_MONITOR_LOGCOUNT, HinemosModuleConstant.STRING_MONITOR_LOGCOUNT),
		MONITOR_BINARYFILE(XMLConstant.DEFAULT_XML_MONITOR_BINARYFILE, HinemosModuleConstant.STRING_MONITOR_BINARYFILE),
		MONITOR_PCAP(XMLConstant.DEFAULT_XML_MONITOR_PCAP, HinemosModuleConstant.STRING_MONITOR_PCAP),
		MONITOR_INTEGRATION(XMLConstant.DEFAULT_XML_MONITOR_INTEGRATION, HinemosModuleConstant.STRING_MONITOR_INTEGRATION),
		MONITOR_CORRELATION(XMLConstant.DEFAULT_XML_MONITOR_CORRELATION, HinemosModuleConstant.STRING_MONITOR_CORRELATION),
		JOB_MST(XMLConstant.DEFAULT_XML_JOB_MST, HinemosModuleConstant.STRING_JOB_MST),
		JOB_SCHEDULE(XMLConstant.DEFAULT_XML_JOB_SCHEDULE, HinemosModuleConstant.STRING_JOB_SCHEDULE),
		JOB_FILECHECK(XMLConstant.DEFAULT_XML_JOB_FILECHECK, HinemosModuleConstant.STRING_JOB_FILECHECK),
		JOB_MANUAL(XMLConstant.DEFAULT_XML_JOB_MANUAL, HinemosModuleConstant.STRING_JOB_MANUAL),
		SYSYTEM_MAINTENANCE(XMLConstant.DEFAULT_XML_SYSYTEM_MAINTENANCE, HinemosModuleConstant.STRING_SYSYTEM_MAINTENANCE),
		// 以下マスター関連情報
		MASTER_PLATFORM(XMLConstant.DEFAULT_XML_MASTER_PLATFORM, HinemosModuleConstant.STRING_MASTER_PLATFORM),
		MASTER_COLLECT(XMLConstant.DEFAULT_XML_MASTER_COLLECT, HinemosModuleConstant.STRING_MASTER_COLLECT),
		MASTER_JMX(XMLConstant.DEFAULT_XML_MASTER_JMX, HinemosModuleConstant.STRING_MASTER_JMX),
		INFRA_SETTING(XMLConstant.DEFAULT_XML_INFRA_SETTING, HinemosModuleConstant.STRING_INFRA_SETTING),
		INFRA_FILE(XMLConstant.DEFAULT_XML_INFRA_FILE, HinemosModuleConstant.STRING_INFRA_FILE),
		HUB_TRANSFER(XMLConstant.DEFAULT_XML_HUB_TRANSFER, HinemosModuleConstant.STRING_HUB_TRANSFER),

		NODE_MAP_SETTING(XMLConstant.DEFAULT_XML_NODE_MAP_SETTING, HinemosModuleConstant.STRING_NODE_MAP_SETTING, OptionUtil.TYPE_ENTERPRISE),
		NODE_MAP_IMAGE_BG(XMLConstant.DEFAULT_XML_NODE_MAP_IMAGE, HinemosModuleConstant.STRING_NODE_MAP_IMAGE_BG, OptionUtil.TYPE_ENTERPRISE),
		NODE_MAP_IMAGE_ICON(XMLConstant.DEFAULT_XML_NODE_MAP_ICON, HinemosModuleConstant.STRING_NODE_MAP_IMAGE_ICON, OptionUtil.TYPE_ENTERPRISE),

		REPORT_SCHEDULE(XMLConstant.DEFAULT_XML_REPORT_SCHEDULE, HinemosModuleConstant.STRING_REPORT_SCHEDULE, OptionUtil.TYPE_ENTERPRISE),
		REPORT_TEMPLATE(XMLConstant.DEFAULT_XML_REPORT_TEMPLATE, HinemosModuleConstant.STRING_REPORT_TEMPLATE, OptionUtil.TYPE_ENTERPRISE),
		JOB_MAP_IMAGE(XMLConstant.DEFAULT_XML_JOBMAP_IMAGE, HinemosModuleConstant.STRING_JOB_MAP_IMAGE, OptionUtil.TYPE_ENTERPRISE),

		// Cloud
		CLOUD_USER(XMLConstant.DEFAULT_XML_CLOUD_USER, HinemosModuleConstant.STRING_CLOUD_USER, OptionUtil.TYPE_XCLOUD),
		CLOUD_MONITOR_SERVICE(XMLConstant.DEFAULT_XML_CLOUD_MON_SERVICE, HinemosModuleConstant.STRING_CLOUD_MONITOR_SERVICE, OptionUtil.TYPE_XCLOUD),
		CLOUD_MONITOR_BILLING(XMLConstant.DEFAULT_XML_CLOUD_MON_BILLING, HinemosModuleConstant.STRING_CLOUD_MONITOR_BILLING, OptionUtil.TYPE_XCLOUD);
		
		private String xmlDefaultName;
		private String funcName;
		private String[] optionKey;
		
		XMLFileName(String xmlDefaultName, String funcName, String... optionKey) {
			this.xmlDefaultName = xmlDefaultName;
			this.funcName = funcName;
			this.optionKey = optionKey;
		}
		public String getFilePath() {
			String fileName = MultiManagerPathUtil.getXMLFileName(xmlDefaultName);
			return MultiManagerPathUtil.getDirectoryPath(SettingToolsXMLPreferencePage.KEY_XML) + File.separator + fileName;
		}
		public String getFileName() {
			return MultiManagerPathUtil.getXMLFileName(xmlDefaultName);
		}
		public String getFuncName() {
			return funcName;
		}
		
		public boolean checkOption(Set<String> options) {
			if (optionKey.length != 0) {
				boolean ret = false;
				for (String key : optionKey) {
					if (options.contains(key)) {
						ret = true;
					}
				}
				return ret;
			} else {
				return true;
			}
		}
	}
	
	/*ロガー*/
	private static Log log = LogFactory.getLog(ReadXMLAction.class);

	/**
	 * ファイルの情報一覧を取得します。
	 * XMLCompositeにつめる値を生成します。
	 * 
	 * @return
	 */
	public static List<List<String>> readHeader() {
		File baseDir = new File(MultiManagerPathUtil.getDirectoryPath(SettingToolsXMLPreferencePage.KEY_XML));
		if(!baseDir.exists()){
			if (!baseDir.mkdir())
				log.warn("directory cant be created." + baseDir.getName());
		}

		// 所持のOptionを取得
		Set<String> options = EndpointManager.getAllOptions();
		
		List<List<String>> table_value = new ArrayList<>();
		for (XMLFileName fileProps : XMLFileName.values()) {
			log.debug("File Read : " + fileProps.getFilePath() + " " + fileProps.getFuncName() + ", option" + fileProps.checkOption(options));
			if (fileProps.checkOption(options)) {
				List<String> xmlProps = readHeaderSub(fileProps.getFilePath() , fileProps.getFuncName(), fileProps.getFileName());
				if(xmlProps != null){
					table_value.add(xmlProps);
				}
			}
		}
		return table_value;
	}

	/**
	 * 1つのファイルの情報を生成します。
	 * 
	 * @param url
	 * @param funcName
	 * @param fileName
	 * @return
	 */
	public static List<String> readHeaderSub(String url,String funcName,String fileName) {
		Map<String, String> header = new HashMap<>();
		// common tag
		String generator = "generator";
		header.put(generator, null);
		String author = "author";
		header.put(author, null);
		String generateDate = "generateDate";
		header.put(generateDate, null);
		String runtimeHost = "runtimeHost";
		header.put(runtimeHost, null);
		String connectedManager = "connectedManager";
		header.put(connectedManager, null);
		// shcemaInfo tag
		String schemaType = "schemaType";
		header.put(schemaType, null);
		String schemaVersion = "schemaVersion";
		header.put(schemaVersion, null);
		String schemaRevision = "schemaRevision";
		header.put(schemaRevision, null);
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser saxParser = factory.newSAXParser();
			
			saxParser.parse(url, new DefaultHandler() {
				public boolean isEndSchemaInfo = false;
				public boolean isEndCommon = false;
				public String tagName = "";
				
				@Override
				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
					if (header.containsKey(qName))
						tagName = qName;
				}
				@Override
				public void endElement(String uri, String localName, String qName) throws SAXException {
					if (qName.equals("schemaInfo"))
						isEndSchemaInfo = true;
					else if (qName.equals("common"))
						isEndCommon = true;
					
					if (isEndSchemaInfo && isEndCommon)
						throw new SAXException("end header read.");
					
					tagName = "";
					}
				@Override
				public void characters(char ch[], int start, int length) throws SAXException {
					if (tagName.isEmpty())
						return;
					
					String n = new String(ch, start, length);
					header.put(tagName, n);
					//log.debug("tag=" + tagName + ", val=" + n);
				}
			});
		} catch (IOException | SAXParseException | ParserConfigurationException e) {
			log.debug("file not found : " + fileName);
			header.clear();
		} catch (SAXException e) {
			// read end
		} catch (Exception e) {
			//その他のExceptionでは、非表示
			log.error("error occuer", e);
			return null;
		}
		
		List<String> xmlAtr = new ArrayList<String>();
		
		log.debug("Function Name : " + funcName);
		xmlAtr.add(funcName); //1番目　機能名
		
		log.debug("File Name : " + fileName);
		xmlAtr.add(fileName); //2番目　ファイル名
		
		//ファイルがない場合には、機能名、ファイル名、ファイルなしと表示
		if (header.isEmpty()) {
			xmlAtr.add("file not found."); //3番目　スキーマタイプ
		} else {
			String schema = header.get(schemaType) +
					"-" + header.get(schemaVersion) +
					"-" + header.get(schemaRevision);
			
			log.debug("Schema Type : " + schema);
			
			xmlAtr.add(schema); //3番目　スキーマタイプ
			
			log.debug("Generate Date : " + (header.get(generateDate) != null ? header.get(generateDate) : "not found."));
			log.debug("Generate Tool : " + (header.get(generator) != null ? header.get(generator) : "not found."));
			log.debug("Generate User : " + (header.get(author) != null ? header.get(author) : "not found."));
			log.debug("Genetate Client : " + (header.get(runtimeHost) != null ? header.get(runtimeHost) : "not found."));
		}
		
		xmlAtr.add(header.get(generateDate) != null ? header.get(generateDate) : ""); //4番目　生成日時
		xmlAtr.add(header.get(generator) != null ? header.get(generator) : ""); //5番目　生成ツール
		xmlAtr.add(header.get(author) != null ? header.get(author) : ""); //6番目　生成ユーザ
		xmlAtr.add(header.get(runtimeHost) != null ? header.get(runtimeHost) : ""); //7番目　クライアントマシン
		xmlAtr.add(header.get(connectedManager) != null ? header.get(connectedManager) : ""); //8番目　マネージャ
		
		return xmlAtr;
	}
	
	/**
	 * キーに対応するXMLのパスを返します。
	 * 
	 * @param key
	 * @return
	 */
	public static List<String> getXMLFile(List<String> xmlDefaultNameList){
		List<String> retList = new ArrayList<String>();
		String XMLDir = MultiManagerPathUtil.getDirectoryPath(SettingToolsXMLPreferencePage.KEY_XML);
		
		for (String xmlDefaultName: xmlDefaultNameList) {
			String xmlFileName = MultiManagerPathUtil.getXMLFileName(xmlDefaultName);
			retList.add(XMLDir + File.separator + xmlFileName);
		}

		return retList;
	}
	
	/**
	 * 全ての比較用 XML ファイルをフルパスで取得。
	 * 
	 * @param xmlDefaultNameList
	 * @return
	 */
	public static List<DiffFilePaths> getDiffXMLFiles(List<String> xmlDefaultNameList){
		List<DiffFilePaths> retList = new ArrayList<DiffFilePaths>();

		String XMLDir = MultiManagerPathUtil.getDirectoryPath(SettingToolsXMLPreferencePage.KEY_XML);
		String XMLDiffDir = MultiManagerPathUtil.getDirectoryPath(SettingToolsXMLPreferencePage.KEY_DIFF_XML);
		
		for (String xmlDefaultName: xmlDefaultNameList) {
			DiffFilePaths paths = new DiffFilePaths();
			
			String xmlFileName = MultiManagerPathUtil.getXMLFileName(xmlDefaultName);

			paths.filePath1 = XMLDir + File.separator + xmlFileName;
			paths.filePath2 = XMLDiffDir + File.separator + xmlFileName;
			
			retList.add(paths);
		}

		return retList;
	}
}
