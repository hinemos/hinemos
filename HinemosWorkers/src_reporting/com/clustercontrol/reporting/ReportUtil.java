/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.eclipse.persistence.config.PersistenceUnitProperties;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.collect.bean.SummaryTypeConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.platform.util.reporting.PropertyUtil;
import com.clustercontrol.reporting.bean.ReportingConstant;
import com.clustercontrol.reporting.util.ReportingProperties;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.FacilityRelationEntity;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

import jakarta.persistence.Query;

/**
 *
 */
public class ReportUtil {

	public static final int OUTPUT_PERIOD_TYPE_DAY = 0;
	public static final int OUTPUT_PERIOD_TYPE_MONTH = 1;
	public static final int OUTPUT_PERIOD_TYPE_YEAR = 2;
	
	/** CSVデリミター */
	public static final String CSV_DELIMITER = ",";

	/** 正常(文字列) */
	public static final String STRING_NORMAL = Messages.getString("COMMON_PRIORITY_NORMAL");

	/** 警告 */
	public static final String STRING_WARNING = Messages.getString("COMMON_PRIORITY_WARN");

	/** 異常(文字列) */
	// クライアント画面上、ABNORMALではなくERRORと表記されるため、レポートを合わせる
	public static final String STRING_ERROR = Messages.getString("COMMON_STATUS_ERROR");

	/** 危険（文字列）。 */
	public static final String STRING_CRITICAL = Messages.getString("COMMON_PRIORITY_CRIT");

	/** 通知（文字列）。 */
	public static final String STRING_INFO = Messages.getString("COMMON_PRIORITY_INFO");

	/** 不明（文字列）。 */
	public static final String STRING_UNKNOWN = Messages.getString("COMMON_PRIORITY_UNKNOWN");

	private static Log m_log = LogFactory.getLog(ReportUtil.class);

	private static final Properties prop = new Properties();
	private static String m_outPath = null;
	private static String m_outFileSuffix = null;
	private static String m_outFileName = null;
	private static String m_logoFilePath = null;

	private static String m_reportScheduleId = "";
	private static int m_outputPeriodType = OUTPUT_PERIOD_TYPE_DAY;
	private static int m_outputPeriodBefore = 0;
	private static int m_outputPeriodFor = 0;
	private static Date m_startDate = null;
	private static Date m_endDate = null;
	private static String m_facilityId = null;
	private static String m_reportTitle = "";
	private static boolean m_logoValidFlg = true;
	private static String m_logoFilename = null;
	private static boolean m_pageValidFlg = true;
	private static int m_outputType = 0;
	private static String m_ownerRoleId = null;
	private static String m_templateSetId = null;

	// key : templateId, orderNoのリスト, value : テンプレートのプロパティ情報
	private static LinkedHashMap<String[], HashMap<String, String>> m_templateMap= null;

	public static void init() {
		// read properties
		FileInputStream stream = null;
		try {
			String etcdir = System.getProperty("hinemos.manager.etc.dir", "/opt/hinemos/etc");
			stream = new FileInputStream(etcdir + File.separator + "hinemos_reporting.properties");
			prop.load(stream);
		} catch (Exception e) {
			m_log.error(e, e);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}
		
		// read log4j_reporting.properties
		try {
			String configFilePathDefault = System.getProperty("hinemos.manager.etc.dir", "/opt/hinemos/etc") 
					+ File.separator + "log4j2_reporting.properties";
			String configFilePath = prop.getProperty("reporting.log4j.file", configFilePathDefault);
			LoggerContext context = (LoggerContext) LogManager.getContext(false);
			context.setConfigLocation(Paths.get(configFilePath).toUri());
		} catch (Exception e1) {
			m_log.error(e1, e1);
		}
		

		try {
			Class.forName(prop.getProperty("reporting.jdbc.driver"));
		} catch (ClassNotFoundException e) {
			m_log.error(e, e);
		}
	}

	public static String getProperty(String key) {
		return prop.getProperty(key);
	}

	public static String getProperty(String key, String defaultValue) {
		return prop.getProperty(key, defaultValue);
	}
	
	public static Map<String, String> getProperiesMap() {
		Map<String, String> persistenceMap = new HashMap<>();
		persistenceMap.put(PersistenceUnitProperties.JDBC_DRIVER, prop.getProperty("reporting.jdbc.driver"));
		persistenceMap.put(PersistenceUnitProperties.JDBC_URL, prop.getProperty("reporting.jdbc.url"));
		persistenceMap.put(PersistenceUnitProperties.JDBC_USER, prop.getProperty("reporting.jdbc.user"));
		persistenceMap.put(PersistenceUnitProperties.JDBC_PASSWORD, prop.getProperty("reporting.jdbc.password"));
		return persistenceMap;
	}

	@SuppressWarnings("unchecked")
	public static boolean loadReportingInfo(String reportScheduleId) {

		m_log.info("loadReportingInfo");

		List<Object[]> resultList = null;
		Query nativeQuery = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			String execQuery = "SELECT facility_id,output_period_type,output_period_before,output_period_for,template_set_id,"
					+ "       report_title,logo_valid_flg,page_valid_flg,owner_role_id,logo_filename,output_type"
					+ "  FROM setting.cc_reporting_info"
					+ " WHERE report_schedule_id = ?1";
			nativeQuery = em.createNativeQuery(execQuery.toString());
			nativeQuery.setParameter(1, reportScheduleId);
			
			resultList = (List<Object[]>)nativeQuery.getResultList();
			if (resultList != null && !resultList.isEmpty()) {
				for (Object[] rs : resultList) {
					m_facilityId = rs[0].toString();
					if (rs[1] instanceof Integer) {
						m_outputPeriodType = ((Integer)rs[1]).intValue();
					} else if (rs[1] instanceof Short) {
						m_outputPeriodType = ((Short)rs[1]).intValue();
					}
					if (rs[2] instanceof Integer) {
						m_outputPeriodBefore = ((Integer)rs[2]).intValue();
					} else if (rs[2] instanceof Short) {
						m_outputPeriodBefore = ((Short)rs[2]).intValue();
					}
					if (rs[3] instanceof Integer) {
						m_outputPeriodFor = ((Integer)rs[3]).intValue();
					} else if (rs[3] instanceof Short) {
						m_outputPeriodFor = ((Short)rs[3]).intValue();
					}
					m_templateSetId = rs[4].toString();
					m_reportTitle = rs[5].toString();
					m_logoValidFlg = (Boolean)rs[6];
					m_pageValidFlg = (Boolean)rs[7];
					m_ownerRoleId = rs[8].toString();
					m_logoFilename = rs[9].toString();
					if (rs[10] instanceof Integer) {
						m_outputType = ((Integer)rs[10]).intValue();
					} else if (rs[3] instanceof Short) {
						m_outputType = ((Short)rs[10]).intValue();
					}

					// overwrite with additional parameters (immediate run)
					try {
						if (System.getProperty("hinemos.reporting.output.period.type") != null) {
							m_outputPeriodType = Integer.parseInt(System.getProperty("hinemos.reporting.output.period.type"));
						}
						if (System.getProperty("hinemos.reporting.output.period.before") != null) {
							m_outputPeriodBefore = Integer.parseInt(System.getProperty("hinemos.reporting.output.period.before"));
						}
						if (System.getProperty("hinemos.reporting.output.period.for") != null) {
							m_outputPeriodFor = Integer.parseInt(System.getProperty("hinemos.reporting.output.period.for"));
						}
						if (System.getProperty("hinemos.reporting.output.template.set.id") != null) {
							m_templateSetId = System.getProperty("hinemos.reporting.output.template.set.id");
						}
						if (System.getProperty("hinemos.reporting.report.title") != null) {
							m_reportTitle = System.getProperty("hinemos.reporting.report.title");
						}
						if (System.getProperty("hinemos.reporting.logo.valid.flg") != null) {
							m_logoValidFlg = Integer.parseInt(System.getProperty("hinemos.reporting.logo.valid.flg")) != 0 ? true : false;
						}
						if (System.getProperty("hinemos.reporting.logo.filename") != null) {
							m_logoFilename = System.getProperty("hinemos.reporting.logo.filename");
						}
						if (System.getProperty("hinemos.reporting.page.valid.flg") != null) {
							m_pageValidFlg = Integer.parseInt(System.getProperty("hinemos.reporting.page.valid.flg")) != 0 ? true : false;
						}
						if (System.getProperty("hinemos.reporting.output.type") != null) {
							m_outputType = Integer.parseInt(System.getProperty("hinemos.reporting.output.type"));
						}
					} catch (Exception e) {
						m_log.warn("invalid immediate-run reporting parameter: " + e.getMessage());
						return false;
					}

					if (m_outputPeriodType == OUTPUT_PERIOD_TYPE_MONTH) {
						Calendar cal = Calendar.getInstance();
						cal.set(Calendar.DATE, 1);
						cal.set(Calendar.HOUR_OF_DAY, 0);
						cal.set(Calendar.MINUTE, 0);
						cal.set(Calendar.SECOND, 0);
						cal.set(Calendar.MILLISECOND, 0);
						cal.add(Calendar.MONTH, -m_outputPeriodBefore);
						m_startDate = cal.getTime();
						cal.add(Calendar.MONTH, m_outputPeriodFor);
						m_endDate = cal.getTime();
					} else if (m_outputPeriodType == OUTPUT_PERIOD_TYPE_YEAR) {
						Calendar cal = Calendar.getInstance();
						cal.set(Calendar.DATE, 1);
						cal.set(Calendar.HOUR_OF_DAY, 0);
						cal.set(Calendar.MINUTE, 0);
						cal.set(Calendar.SECOND, 0);
						cal.set(Calendar.MILLISECOND, 0);
						cal.add(Calendar.YEAR, -m_outputPeriodBefore);
						m_startDate = cal.getTime();
						cal.add(Calendar.YEAR, m_outputPeriodFor);
						m_endDate = cal.getTime();
					} else {
						Calendar cal = Calendar.getInstance();
						cal.set(Calendar.HOUR_OF_DAY, 0);
						cal.set(Calendar.MINUTE, 0);
						cal.set(Calendar.SECOND, 0);
						cal.set(Calendar.MILLISECOND, 0);
						cal.add(Calendar.DATE, -m_outputPeriodBefore);
						m_startDate = cal.getTime();
						cal.add(Calendar.DATE, m_outputPeriodFor);
						m_endDate = cal.getTime();
					}

					ReportUtil.m_reportScheduleId = reportScheduleId;

					m_log.info(reportScheduleId + ": facilityId = " + m_facilityId
							+ ", outputPeriodType = " + m_outputPeriodType
							+ ", outputPeriodBefore = " + m_outputPeriodBefore
							+ ", outputPeriodFor = " + m_outputPeriodFor
							+ ", templateSetId = " + m_templateSetId
							+ ", reportTitle = " + m_reportTitle
							+ ", logoValidFlg = " + m_logoValidFlg
							+ ", logoFilename = " + m_logoFilename
							+ ", pageValidFlg = " + m_pageValidFlg
							+ ", outputType = " + m_outputType
							+ ", Period = " + m_startDate + " - " + m_endDate);
				}
			} else {
				m_log.warn("reportScheduleId not found: " + reportScheduleId);
				ReportUtil.m_log.info("reportScheduleId not found: " + reportScheduleId);
				return false;
			}
		} catch (Exception e) {
			m_log.error(e, e);
			return false;
		}
		
		return true;
	}
	
	
	@SuppressWarnings("unchecked")
	public static boolean loadTemplateMap(String templateSetId) {

		m_log.info("loadTemplateMap : templateSetId = " + templateSetId);
		List<Object[]> resultList = null;
		Query nativeQuery = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			String execQuery = "SELECT template_id, order_no, title_name"
					+ "  FROM setting.cc_reporting_template_set_detail_info"
					+ " WHERE template_set_id = ?1"
					+ " ORDER BY order_no";
			nativeQuery = em.createNativeQuery(execQuery.toString());
			nativeQuery.setParameter(1, templateSetId);
			
			resultList = (List<Object[]>)nativeQuery.getResultList();
			
			String templateId = "";
			String orderNo = "";
			String titleName = "";
			
			HashMap<String, String> tmpMap = null;
			ReportingProperties repProperties = null;
			
			m_templateMap = new LinkedHashMap<String[], HashMap<String,String>>();
			
			if (resultList != null) {
				for (Object[] rs : resultList) {
					templateId = rs[0].toString();
					orderNo = rs[1].toString();
					titleName = rs[2] != null ? rs[2].toString() : null;
					String[] mapKey = {templateId,orderNo};
					
					// テンプレートごとのプロパティをすべて取得する
					repProperties = new ReportingProperties(templateId);
					tmpMap = repProperties.getAllProperties();
					
					// DBに格納されている値が存在すれば、上書く
					if(titleName != null && !titleName.isEmpty()) {
						titleName = HinemosMessage.replace(titleName);
						tmpMap.put(ReportingConstant.TITLE_MAIN_KEY_VALUE, titleName);
					}
					
					m_log.info("templateMap put : templateId = " + templateId);
					m_templateMap.put(mapKey, tmpMap);
					
					if (m_log.isDebugEnabled()) {
						boolean isFirst = true;
						StringBuilder sb = new StringBuilder();
						for(Map.Entry<String, String> entry : tmpMap.entrySet()) {
							if(isFirst) {
								sb.append(templateId);
								sb.append(": ");
								sb.append(entry.getKey());
								sb.append(" = ");
								sb.append(entry.getValue());
								isFirst = false;
							} 
							else {
								sb.append(", ");
								sb.append(entry.getKey());
								sb.append(" = ");
								sb.append(entry.getValue());
							}
						}
						
						m_log.debug(sb.toString());
					}
					
				}
			}
		} catch (Exception e) {
			m_log.error(e, e);
			return false;
		}
		
		return true;
	}

	private static void getChildNodes(List<String[]> list, String scopeId) {
		m_log.debug("getting nodes in scope: " + scopeId);

		try {
			// TODO:クエリーの呼び出し方について、これでいいのかを、ソースクリーニングのときに検討。
			List<FacilityRelationEntity> facilityRelationList = com.clustercontrol.reporting.util.ReportingQueryUtil.getChildFacilityRelationEntity(scopeId);
			if (facilityRelationList != null) {
				for (FacilityRelationEntity facilityRelationEntity : facilityRelationList) {
					FacilityInfo facilityInfo = com.clustercontrol.reporting.util.ReportingQueryUtil.getFacilityPK(facilityRelationEntity.getChildFacilityId(), ObjectPrivilegeMode.NONE);
					String facilityId = facilityInfo.getFacilityId();
					String facilityName = facilityInfo.getFacilityName();
					Integer type = facilityInfo.getFacilityType();
					if (type == 1) {
						// leaf node
						m_log.debug("  found node: " + facilityId);
						String[] nodeInfo = new String[2];
						nodeInfo[0] = facilityId;
						nodeInfo[1] = facilityName;
						list.add(nodeInfo);
					} else {
						// sub scope
						getChildNodes(list, facilityId);
					}
				}
			}
		} catch (Exception e) {
			m_log.error(e, e);
		}
	}

	public static List<String[]> getNodesInScope(String scopeId) {
		List<String[]> list = new ArrayList<>();

		getChildNodes(list, scopeId);
		if (list.isEmpty()) {
			try {
				FacilityInfo facilityInfo = com.clustercontrol.reporting.util.ReportingQueryUtil.getFacilityPKAndFacilityType(scopeId, 1);
				if (facilityInfo != null) {
						String[] nodeInfo = new String[2];
						nodeInfo[0] = facilityInfo.getFacilityId();
						nodeInfo[1] = facilityInfo.getFacilityName();
						list.add(nodeInfo);
				}
			} catch (Exception e) {
				m_log.error(e, e);
			}
		}

		class NodeInfoComparator implements Comparator<String[]> {
			public int compare(String[] s, String[] t) {
				return s[0].compareTo(t[0]);
			}
		}

		Collections.sort(list, new NodeInfoComparator());

		return list;
	}

	public static String getReportId() {
		return m_reportScheduleId;
	}
	
	public static String getTemplateSetId() {
		return m_templateSetId;
	}
	
	public static LinkedHashMap<String[], HashMap<String, String>> getTemplateMap() {
		return m_templateMap;
	}

	public static int getOutputPeriodType() {
		return m_outputPeriodType;
	}

	public static int getOutputPeriodBefore() {
		return m_outputPeriodBefore;
	}

	public static int getOutputPeriodFor() {
		return m_outputPeriodFor;
	}

	public static Date getStartDate() {
		return m_startDate;
	}

	public static Date getEndDate() {
		return m_endDate;
	}

	public static String getFacilityId() {
		return m_facilityId;
	}

	public static String getOwnerRoleId() {
		return m_ownerRoleId;
	}

	public static String getReportTitle() {
		return m_reportTitle;
	}
	
	public static int getOutputType() {
		return m_outputType;
	}

	public static String getContentsString() {
		StringBuilder builder = new StringBuilder();
		
		for (Map.Entry<String[], HashMap<String, String>> entry : m_templateMap.entrySet()) {
			
			boolean indexFlg = Boolean.parseBoolean(entry.getValue().get(ReportingConstant.INDEX_FLG_KEY_VALUE));
			
			if(indexFlg) {
				String templateName = entry.getValue().get(ReportingConstant.TEMPLATE_NAME_KEY_VALUE);
				
				if(builder.length() > 0) {
					builder.append("\n");
				}
				builder.append("- " + templateName);
			}
		}
		return builder.toString();
	}
	
	public static String getOutPath() {
		m_log.debug("getOutPath() : called");
		if (m_outPath != null) {
			m_log.debug("getOutPath() : m_outPath is not null, m_outPath=" + m_outPath);
			return m_outPath;
		}

		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		String basePath = PropertyUtil.getLogReportPath();
		m_outPath = basePath + "/" + fmt.format(new Date());
		m_log.debug("getOutPath() : m_outPath=" + m_outPath);
		return m_outPath;
	}

	public static boolean createOutPathDir() {
		m_log.debug("createOutPathDir() : called");
		if (m_outPath == null) {
			getOutPath();
		}
		boolean rtn = false;
		try {
			File newDir = new File(m_outPath);
			if (newDir.exists()) {
				return true;
			}
			rtn = newDir.mkdirs();
			if (!rtn) {
				throw new HinemosUnknown("mkdirs failed. path=" + m_outPath);
			}
			File csvDir = new File(m_outPath + "/csv");
			rtn = csvDir.mkdirs();
			if (!rtn) {
				throw new HinemosUnknown("mkdir is failure. path=" + m_outPath + "/csv");
			}
		} catch (Exception e) {
			m_log.error(e, e);
			return false;
		}
		return true;
	}
	
	public static String getCsvOutPath() {
		m_log.debug("getCsvOutPath() : called");
		return getOutPath() + "/csv";
	}

	public static void setOutFileSuffix(String suffix) {
		m_outFileSuffix = suffix;
	}

	public static String getOutFileSuffix() {
		if (m_outFileSuffix != null) {
			return m_outFileSuffix;
		}

		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
		if (m_reportScheduleId != null && !m_reportScheduleId.isEmpty()) {
			m_outFileSuffix = m_reportScheduleId + "_" + fmt.format(new Date());
		} else {
			m_outFileSuffix = fmt.format(new Date());
		}

		return m_outFileSuffix;
	}

	public static void setReportFileName(String fileName) {
		m_outFileName = fileName;

		File reportFile = new File(fileName);
		String reportBaseName = reportFile.getName();

		// (prefix)_(reportID)_YYYYMMDDhhmmss.pdf
		int extpos = reportBaseName.lastIndexOf(".pdf");
		if (extpos > 0) {
			String nameBody = reportBaseName.substring(0, extpos);
			String prefix = ReportUtil.getProperty("reporting.filename", "hinemos_report");
			if (nameBody.startsWith(prefix + "_")) {
				String suffix = nameBody.substring(prefix.length() + 1);
				m_log.info("File name suffix: " + suffix);
				setOutFileSuffix(suffix);
			}
		}
	}

	public static String getReportFileName() {
			m_log.debug("getReportFileName() : called");
		if (m_outFileName != null) {
			m_log.debug("getReportFileName() : m_outFileName is not null, m_outFileName=" + m_outFileName);
			return m_outFileName;
		}

		try {
			String baseName = ReportUtil.getProperty("reporting.filename", "hinemos_report");
			String fileExtension = "";
			
			// 拡張子の設定
			if (getOutputType() == ReportingConstant.TYPE_PDF) {
				fileExtension = ".pdf";
			} else if (getOutputType() == ReportingConstant.TYPE_XLSX) {
				fileExtension = ".xlsx";
			}
			
			setReportFileName(getOutPath() + "/" + baseName + "_" + getOutFileSuffix() + fileExtension);
		} catch (Exception e) {
			m_log.error(e, e);
		}

		m_log.debug("getReportFileName() : m_outFileName=" + m_outFileName);
		return m_outFileName;
	}

	public static String getCsvFileNameForReportFile(String reportFileName) {
		return getCsvFileNameForReportFile(reportFileName, "");
	}

	public static String getCsvFileNameForReportFile(String reportFileName, String suffix) {
		try {
			File reportFile = new File(reportFileName);
			String reportBaseName = reportFile.getName();
			String csvFileName = reportBaseName.substring(0, reportBaseName.lastIndexOf(".jrxml")) + suffix + "_" + getOutFileSuffix() + ".csv";
			return getCsvOutPath() + "/" + csvFileName;
		} catch (Exception e) {
			m_log.error(e, e);
			m_log.error("Invalid file name: " + reportFileName);
			return null;
		}
	}
	
	public static String getCsvFileNameForTemplateType(String type) {
		return getCsvFileNameForTemplateType(type, "", true);
	}

	public static String getCsvFileNameForTemplateType(String type, String suffix) {
		m_log.debug("getCsvFileNameForTemplateType() : called");
		return getCsvFileNameForTemplateType(type, suffix, true);
	}

	public static String getCsvFileNameForTemplateType(String type, String suffix, boolean extension) {
		String csvFileName = type + "_" + suffix + "_" + getOutFileSuffix() + (extension ? ".csv" : "");
		return getCsvOutPath() + "/" + csvFileName;
	}
	
	
	public static boolean removeExpiredDirectories() {
		String periodStr = ReportUtil.getProperty("reporting.retention.period", "7");
		Integer period = Integer.parseInt(periodStr);
		m_log.info("reporting.retention.period: " + period);
		if (period == 0) {
			return false;
		}
		boolean deleted = false;
		
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.DATE, -period);
		
		String basePath = PropertyUtil.getLogReportPath();
		
		Path dir = new File(basePath).toPath();
		// /output_path/YYYYDDMM/
		try(DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]")) {
			for (Path path : ds) {
				m_log.info("path: " + path);
				// exclude today's directory
				if (Files.isSameFile(path, new File(getOutPath()).toPath())) {
					continue;
				}
				Path yyyymmdd = path.getFileName();
				if(yyyymmdd == null){
					m_log.error(String.format("removeExpiredDirectories() : failed to get file name from path. path=[%s]", path.toString()));
					return deleted;
				}
				Date dirDate = fmt.parse(yyyymmdd.toString());
				if (dirDate.getTime() >= cal.getTimeInMillis()) {
					continue;
				} else {
					m_log.info("delete expired directory: " + path.getFileName());
				}

				try {
					if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
						DirectoryStream<Path> dsSub = Files.newDirectoryStream(path);
						for (Path pathSub : dsSub) {
							m_log.info("pathSub: " + pathSub);
							// /output_path/YYYYDDMM/csv/
							Path csv = pathSub.getFileName();
							if(csv == null){
								m_log.error(String.format("removeExpiredDirectories() : failed to get file name from pathSub. pathSub=[%s]", pathSub.toString()));
								return deleted;
							}
							if ("csv".equals(csv.toString()) &&
								Files.isDirectory(pathSub, LinkOption.NOFOLLOW_LINKS)) {
									DirectoryStream<Path> dsCsv = Files.newDirectoryStream(pathSub);
									for (Path pathCsv : dsCsv) {
										m_log.info("CSV path: " + pathCsv);
										if (pathCsv.toString().endsWith(".csv") && pathCsv.toFile().isFile()) {
											boolean deleteFlag = pathCsv.toFile().delete();
											if (!deleteFlag) {
												m_log.info("delete failed : " + pathCsv.toString());
											}
											deleted = true;
										}
									}
									dsCsv.close();
									Files.delete(pathSub);
									deleted = true;
							} else if (pathSub.toString().endsWith(".pdf") && pathSub.toFile().isFile()) {
								Files.delete(pathSub);
								deleted = true;
							} else if (pathSub.toString().endsWith(".xlsx") && pathSub.toFile().isFile()) {
								Files.delete(pathSub);
								deleted = true;
							}
						}
						dsSub.close();
						Files.delete(path);
					}
				} catch (Exception e) {
					m_log.info("removing of directory failed with " + e);
				}
			}
		} catch (IOException | ParseException e) {
			m_log.error(e, e);
		} catch (Exception e) {
			m_log.error(e, e);
		}
		
		return deleted;
	}

	public static boolean isLogoValid() {
		return m_logoValidFlg;
	}

	public static String getLogoFilePath() {
		if (m_logoValidFlg == false) {
			return "";
		}
		if (m_logoFilePath != null) {
			return m_logoFilePath;
		}
		
		setLogoFilePath(m_logoFilename);
		
		return m_logoFilePath;
	}

	public static void setLogoFilePath(String filename) {
		if (filename == null) {
			m_logoFilePath = null;
			return;
		}

		File file = new File(filename);
		if (file.isAbsolute()) {
			m_logoFilePath = filename;
		} else {
			String etcdir = System.getProperty("hinemos.manager.etc.dir", "/opt/hinemos/etc");
			m_logoFilePath = etcdir + File.separator + ReportingConstant.STR_REP + File.separator + filename;
		}
	}

	public static boolean isPageValid() {
		return m_pageValidFlg;
	}

	public static String joinStringsToCsv(String[] strings) {
		StringBuilder sb = new StringBuilder();
		int strsCnt = strings.length;
		
		for (int i = 0; i < strsCnt; i++) {
			if (strings[i] == null) {
				sb.append("\"\"");
			} else {
				sb.append('"');
				sb.append(strings[i].replace("\"", "\"\""));
				sb.append('"');
			}
			sb.append(CSV_DELIMITER);
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	public static String joinStrings(String[] strings, String delimiter) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < strings.length; i++) {
			sb.append(strings[i]);
			if (i < strings.length - 1) {
				sb.append(delimiter);
			}
		}
		return sb.toString();
	}

	public static String getEndStatusString(int endStatus) {
		String endStatusStr;

		switch (endStatus) {
		case EndStatusConstant.TYPE_NORMAL:	// 正常
			//endStatusStr = "<style backcolor='green'>" + EndStatusConstant.STRING_NORMAL + "</style>";
			endStatusStr = STRING_NORMAL;
			break;
		case EndStatusConstant.TYPE_WARNING: // 警告
			//endStatusStr = "<style backcolor='yellow'>" + EndStatusConstant.STRING_WARNING + "</style>";
			endStatusStr = STRING_WARNING;
			break;
		default: // 異常、その他
			//endStatusStr = "<style backcolor='red'>" + EndStatusConstant.STRING_ABNORMAL + "</style>";
			endStatusStr = STRING_ERROR;
			break;
		}

		return endStatusStr;
	}

	public static String getStatusString(int status) {
		return StatusConstant.typeToMessageCode(status);
	}
	
	@SuppressWarnings("unchecked")
	public static  ArrayList<String> getValueFromDB(String columnName, String tableName, String whereKey, String whereValue){
		
		ArrayList<String> retValList = new ArrayList<String>();
		String retVal = null;
		
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			
			m_log.debug("SQL : SELECT "+ columnName +" FROM "+ tableName +" WHERE " + whereKey + " like "+ whereValue +" limit 1");
			
			// Hinemos DB内の情報を基に出力値を算出する
			List<Object[]> resultList = null;
			String query = "SELECT "+ columnName +" FROM "+ tableName +" WHERE " + whereKey + " like ?1 limit 1";
			Query nativeQuery = em.createNativeQuery(query.toString());
			nativeQuery.setParameter(1, whereValue);
			
			// 出力値を格納する
			resultList = (List<Object[]>)nativeQuery.getResultList();
			if(resultList != null) {
				for (Object[] rs : resultList) {
					retVal = rs[0].toString();
					m_log.debug(columnName +":" + whereValue + ":" + retVal);
					
					retValList.add(retVal);
				}
			}
		} catch (Exception e) {
			m_log.error(e, e);
		}
		
		return retValList;
	}
	
	/**
	 * 出力期間からサマリーのタイプを取得する。<BR>
	 * 
	 * @param fromTime
	 * @param toTime
	 * @return
	 */
	public static int getSummaryType(long fromTime, long toTime) {
		long periodTime = toTime - fromTime; // 出力期間の差分
		
		long daySec = 1000 * 24 * 60 * 60; // 1日
		long monthSec = daySec * 30; // 1か月
		long yearSec = daySec * 365; // 1年
		
		// 出力期間の日数からサマリータイプを取得する。
		if (periodTime <= daySec) {
			return SummaryTypeConstant.TYPE_RAW;
		} else if (periodTime <= monthSec) {
			return SummaryTypeConstant.TYPE_AVG_HOUR;
		} else if (periodTime <= yearSec) {
			return SummaryTypeConstant.TYPE_AVG_DAY;
		} else {
			return SummaryTypeConstant.TYPE_AVG_MONTH;
		}
	}
}
