package com.clustercontrol.collect.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.collect.bean.SummaryTypeConstant;
import com.clustercontrol.collect.model.CollectData;
import com.clustercontrol.collect.model.CollectKeyInfoPK;
import com.clustercontrol.collect.model.SummaryDay;
import com.clustercontrol.collect.model.SummaryHour;
import com.clustercontrol.collect.model.SummaryMonth;
import com.clustercontrol.collect.session.CollectControllerBean;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.Messages;

/**
 * 性能情報のCSVデータを出力、削除を行うクラス<BR>
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class ExportCollectDataFile {
	private static Log m_log = LogFactory.getLog(ExportCollectDataFile.class);
	
	/** csvファイル名の最大bytes数 */
	private static final int MAX_FILE_NAME_BYTES = 200;

	/**
	 *
	 * ①1 File / 1 Node でCSV性能データファイルを作成する<BR>
	 * facilityId = Node の場合：1ファイル facilityId = Scope
	 * の場合：配下の(有効な)ノード毎に1ファイルを作成する
	 *
	 * ②header が有効な場合 各CSVファイルに収集項目の項目名をヘッダとして出力する
	 *
	 * ③archiveが有効な場合 facilityId = Node の場合：対象1ファイルをzip圧縮する facilityId = Scope
	 * の場合：対象1ファイル群を1ファイルにzip圧縮する
	 *
	 * @param map
	 * @param header
	 * @param archive
	 * @return
	 */
	public static synchronized ArrayList<String> create(TreeMap<String, String> facilityIdNameMap,
			List<String> targetFacilityList,
			List<CollectKeyInfoPK> collectKeyInfoList, 
			Integer summaryType, String localeStr, boolean header, String userId, String defaultDateStr) throws HinemosUnknown {
		m_log.debug("create() facilityIdNameMap = " + facilityIdNameMap.toString() 
				+ ", targetFacilityList =" + targetFacilityList.toString()
				+ ", collectKeyInfoList =" + collectKeyInfoList.toString()
				+ ", summaryType = " + summaryType + ", localeStr = " + localeStr + ", header = " + header 
				+ ", defaultDateStr = " + defaultDateStr);

		// null check
		if (facilityIdNameMap.size() == 0) {
			m_log.debug("target file is empty");
			return new ArrayList<String>();
		}

		ArrayList<String> targetFileNameList = new ArrayList<String>();
		try {
			// zip名はクライアント側でリネームされるため、ここでは厳密に命名しない
			String zipName = Messages.getString(SummaryTypeConstant.typeToMessageCode(summaryType), Locale.ENGLISH) + "_" + defaultDateStr + ".zip";
			targetFileNameList.add(zipName);

			// 出力ファイル作成スレッドを実行して抜ける
			CreatePerfFileTask task = new CreatePerfFileTask(targetFacilityList, collectKeyInfoList, 
					summaryType, localeStr, header, defaultDateStr, userId, zipName);
			Thread thread = new Thread(task);
			thread.start();

		} catch (Exception e) {
			m_log.warn("create() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}
		// for debug
		if (m_log.isDebugEnabled()) {
			for (String fileName : targetFileNameList) {
				m_log.debug("target file name = " + fileName);
			}
		}
		return targetFileNameList;
	}

	/**
	 * ファイル名を作成して返します。(itemName+moitorId+facilityid+defaultDateStr+summarytype+"."+extension)
	 * 
	 * @param summaryType
	 * @param facilityid
	 * @param monitorId
	 * @param fileId
	 * @param extension
	 * @return
	 */
	private static String createFileName(String itemNameEng, String displayName, String monitorId, String summaryType, 
			String facilityid, String defaultDateStr, String extension) {
		if (!displayName.equals("") && !itemNameEng.endsWith("[" + displayName + "]")) {
			itemNameEng+= "[" + displayName + "]";
		}
		String retName = removeFileNameProhibitionWord(itemNameEng + "_" + monitorId + "_" + facilityid 
				+ "_" + defaultDateStr + "_" + summaryType);
		return retName + "." + extension;
	}

	/**
	 * 指定された文字列からファイル禁則文字を除去します。(\ / : * ? " < > |)<br>
	 * また、ファイル名の文字数をチェックし、200Bytesより長い場合は短縮します。
	 * 
	 * @param fileName
	 * @return
	 */
	private static String removeFileNameProhibitionWord(String fileName) {
		String result = fileName;
		// \ / : * ? " < > |
		result = result.replace("\\", "");
		result =  result.replace("/", "");
		result =  result.replace(":", "");
		result =  result.replace("*", "");
		result =  result.replace("?", "");
		result =  result.replace("\"", "");
		result =  result.replace("<", "");
		result =  result.replace(">", "");
		result =  result.replace("|", "");
		
		// ファイル名の文字数チェック
		if (result.getBytes().length > MAX_FILE_NAME_BYTES) {
			byte[] buf = result.getBytes();
			result = new String(buf, 0, MAX_FILE_NAME_BYTES);
		}
		return result;
	}
	/**
	 * 性能データがない場合の空ファイルを作成する
	 *
	 * @param filepath
	 * @throws HinemosUnknown
	 */
	private static void writeEmptyDataFile(String filepath) throws HinemosUnknown {
		m_log.debug("writeEmptyDataFile() filepath = " + filepath);

		try {
			File file = new File(filepath);

			if (checkBeforeWritefile(file)) {
				PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));

				pw.println("Performance Data has not been collected yet.");

				pw.close();
			} else {
				m_log.info("writeEmptyDataFile() filepath = " + filepath + " write error");
				throw new HinemosUnknown("filepath = " + filepath + " write error");
			}
		} catch (IOException e) {
			m_log.warn("writeEmptyDataFile() filepath = " + filepath + " write error : " + e.getClass().getSimpleName()
					+ ", " + e.getMessage(), e);
			throw new HinemosUnknown("filepath = " + filepath + " write error", e);
		}
	}

	/**
	 *
	 * @param monitorId
	 * @param facilityId
	 * @param filepath
	 * @param dataSettings
	 * @return
	 * @throws HinemosUnknown
	 */
	private static void writeFile(String facilityId, String filepath, Map<String, Map<Long, Float>> itemCodeTimeDataMap, String itemMonitor) {

		PrintWriter pw = null;

		String exportEncode = HinemosPropertyUtil.getHinemosPropertyStr("performance.export.encode", "MS932");

		String exportLineSeparator = "\r\n";
		String exportLineSeparatorStr = HinemosPropertyUtil.getHinemosPropertyStr("performance.export.line.separator",
				"CRLF");

		if ("CRLF".equals(exportLineSeparatorStr)) {
			exportLineSeparator = "\r\n";
		} else if ("LF".equals(exportLineSeparatorStr)) {
			exportLineSeparator = "\n";
		} else if ("CR".equals(exportLineSeparatorStr)) {
			exportLineSeparator = "\r";
		}

		try {
			pw = new PrintWriter(
					new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filepath, true), exportEncode)));

			JpaTransactionManager jtm = null;

			try {
				jtm = new JpaTransactionManager();
				jtm.begin();

				for (Long time : itemCodeTimeDataMap.get(itemMonitor).keySet()) {
					// write
					SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
					sdf1.setTimeZone(HinemosTime.getTimeZone());

					String timeformat = sdf1.format(new Date(time));
					StringBuffer bs = new StringBuffer(timeformat);
					Float value = itemCodeTimeDataMap.get(itemMonitor).get(time);
					bs.append(",");
					bs.append(value);
					pw.print(bs.toString());
					pw.print(exportLineSeparator);
					pw.flush();

				}
				jtm.commit();
			} catch (Exception e) {
				m_log.warn("writeFile() write csv file by SQLException facilityId = "
						+ facilityId + ", filepath = " + filepath, e);
				if (jtm != null){
					jtm.rollback();
				}
			} finally {
				if (jtm != null) {
					jtm.close();
				}
			}
		} catch (UnsupportedEncodingException e) {
			m_log.warn("writeFile() Unsupported Encoding : exportEncode = " + exportEncode
					+ ". Please modify performance.export.encode : " + e.getClass().getSimpleName() + ", "
					+ e.getMessage(), e);
		} catch (FileNotFoundException e) {
			m_log.warn("File does not exists! filepath = " + filepath + " : " + e.getClass().getSimpleName() + ", "
					+ e.getMessage(), e);
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}

	/**
	 * 指定したファイルにItemCodeのリスト(itemInfoList)のヘッダ情報を出力する。
	 *
	 * @param filepath
	 * @param itemInfoList
	 */
	private static void writeHeader(Integer summaryType, String localeStr, String filepath, String facilityId, 
			String itemMonitor, String oldDate, String latestDate)
			throws HinemosUnknown {
		PrintWriter pw = null;
		Locale locale = new Locale(localeStr);
		try {
			File file = new File(filepath);

			if (checkBeforeWritefile(file)) {
				String exportEncode = HinemosPropertyUtil.getHinemosPropertyStr("performance.export.encode", "MS932");

				String exportLineSeparator = "\r\n";
				String exportLineSeparatorStr = HinemosPropertyUtil
						.getHinemosPropertyStr("performance.export.line.separator", "CRLF");

				if ("CRLF".equals(exportLineSeparatorStr)) {
					exportLineSeparator = "\r\n";
				} else if ("LF".equals(exportLineSeparatorStr)) {
					exportLineSeparator = "\n";
				} else if ("CR".equals(exportLineSeparatorStr)) {
					exportLineSeparator = "\r";
				}

				pw = new PrintWriter(
						new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), exportEncode)));

				// File Header
				pw.print(Messages.getString("FACILITY_ID", locale) + " : " + facilityId + exportLineSeparator);
				pw.print(Messages.getString("SUMMARYTYPE", locale) + " : "
				+ Messages.getString(SummaryTypeConstant.typeToMessageCode(summaryType), locale) + exportLineSeparator);
				pw.print(Messages.getString("COLLECTION_OLDEST_DATE", locale) + " : " + oldDate + exportLineSeparator); // 最古
				pw.print(Messages.getString("COLLECTION_LATEST_DATE", locale) + " : " + latestDate + exportLineSeparator); // 最新

				pw.print(exportLineSeparator);

				// Column Header
				// date
				pw.print(Messages.getString("TIMESTAMP", locale));

				pw.print("," + HinemosMessage.replace(itemMonitor, locale));
				pw.print(exportLineSeparator); // 改行
			} else {
				m_log.info("writeHeader() filepath = " + filepath + " write error");
				throw new HinemosUnknown("filepath = " + filepath + " write error");
			}
		} catch (IOException e) {
			m_log.warn("writeHeader() filepath = " + filepath + " is io error", e);
			throw new HinemosUnknown("filepath = " + filepath + " is io error", e);

		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}

	/**
	 * 指定したファイル名のリストを削除する。ディレクトリはperformance.export.dirで指定。
	 *
	 * @param filePathList
	 */
	public static void deleteFile(ArrayList<String> fileNameList) throws HinemosUnknown {
		m_log.debug("deleteFile()");

		String homeDir = System.getProperty("hinemos.manager.home.dir");
		String exportDirectory = HinemosPropertyUtil.getHinemosPropertyStr("performance.export.dir",
				homeDir + "/var/export/");

		for (String fileName : fileNameList) {
			String filePath = exportDirectory + fileName;
			m_log.debug("deleteFile() targetFileName = " + fileName);
			m_log.debug("deleteFile() targetFilePath = " + filePath);

			File file = new File(filePath);

			if (file.exists()) {
				boolean ret = file.delete();
				m_log.debug("deleteFile() targetFilePath = " + filePath + " : delete succeed. + " + ret);
			} else {
				m_log.debug("deleteFile() targetFilePath = " + filePath + " : does not exist.");
			}
		}
	}

	/**
	 * 指定したファイルパスのリストを削除する。ディレクトリチェックは行わない。
	 *
	 * @param filePathList
	 */
	private static void deleteFilePath(ArrayList<String> filePathList) {
		m_log.debug("deleteFilePath()");

		for (String filePath : filePathList) {
			m_log.debug("deleteFilePath() targetFilePath = " + filePath);

			File file = new File(filePath);

			if (file.exists()) {
				m_log.info("Delete Performance Export File. file = " + file.getPath());

				boolean ret = file.delete();
				m_log.debug("deleteFilePath() targetFilePath = " + filePath + " : delete succeed. " + ret);
			} else {
				m_log.debug("deleteFilePath() targetFilePath = " + filePath + " : does not exist.");
			}
		}
	}

	/**
	 * ファイルパスをfromからtoにリネームする
	 *
	 * @param from
	 * @param to
	 */
	private static void renameFile(String from, String to) {
		m_log.debug("from = " + from + ", to = " + to);
		File file = new File(from);
		if (file.exists() && file.canWrite()) {
			boolean ret = file.renameTo(new File(to));
			if (!ret) {
				m_log.info("renameFile error");
			}
		}
	}

	/**
	 * 対象ファイルが存在するかをチェックする
	 *
	 * @param fileName
	 * @return
	 */
	public static boolean isCreatedFile(String fileName) {
		String homeDir = System.getProperty("hinemos.manager.home.dir");
		String exportDirectory = HinemosPropertyUtil.getHinemosPropertyStr("performance.export.dir",
				homeDir + "/var/export/");

		String createFilePath = exportDirectory + fileName;
		File file = new File(createFilePath);

		return file.exists();
	}

	/**
	 * 指定したファイルに書き込み可能かをチェックする。ファイルが存在しない場合は、新規作成を試み、その可否を返す。
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private static boolean checkBeforeWritefile(File file) throws IOException {
		if (file.exists()) {
			if (file.isFile() && file.canWrite()) {
				return true;
			}
		} else {
			m_log.info("checkBeforeWritefile() Crate Performance Export File. file = " + file.getPath());
			return file.createNewFile();
		}
		return false;
	}

	/**
	 * ダウンロードするファイルを作成する処理クラス
	 *
	 *
	 */
	private static class CreatePerfFileTask implements Runnable {

		private List<String> m_targetFacilityList;
		private List<CollectKeyInfoPK> m_targetCollectKeyInfoList;
		private Integer m_summaryType;
		private String m_localeStr;
		private boolean m_header = false;
		private String m_fileId = "";
		private String m_userId = "";
		private String m_zipName = "";

		/**
		 * デフォルトコンストラクタ
		 *
		 * @param facilityIdNameMap
		 * @param targetItemCodeCollectMap
		 * @param facilityCollectMap
		 * @param summaryType
		 * @param header
		 * @param archive
		 * @param fileId
		 * @param userId
		 * @param zipName
		 */
		private CreatePerfFileTask(List<String> targetFacilityList, 
				List<CollectKeyInfoPK> collectKeyInfoList, 
				Integer summaryType, String localeStr, boolean header, String fileId, String userId, String zipName) {

			this.m_targetFacilityList = targetFacilityList;
			this.m_targetCollectKeyInfoList = collectKeyInfoList;
			this.m_summaryType = summaryType;
			this.m_localeStr = localeStr;
			this.m_header = header;
			this.m_fileId = fileId;
			this.m_userId = userId;
			this.m_zipName = zipName;

			m_log.debug("CreatePerfFileTask Create : " 
					+ ", m_targetFacilityList = " + m_targetFacilityList.toString() 
					+ ", m_targetCollectKeyInfoList = " + m_targetCollectKeyInfoList.toString() 
					+ ", summaryType = " + summaryType + ", summaryTypeStr = " + m_localeStr + ", header = " + header
					+ ", fileId, zipName = " + zipName);
		}

		/**
		 * ダウンロードデータの作成
		 */
		@Override
		public void run() {
			m_log.info("CreatePerfFileTask start!");

			boolean success = true;
			ArrayList<String> createFilePathList = new ArrayList<String>();
			try {
				// 新たなスレッドとして開始される場合は、ユーザ情報が格納されていないため、ユーザ情報を改めて格納
				HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, m_userId);
				HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR,
						new AccessControllerBean().isAdministrator());

				CollectControllerBean controller = new CollectControllerBean();

				String homeDir = System.getProperty("hinemos.manager.home.dir");
				String exportDirectory = HinemosPropertyUtil.getHinemosPropertyStr("performance.export.dir",
						homeDir + "/var/export/");

				Map<String, Map<Long, Float>> itemCodeTimeDataMap = new HashMap<String, Map<Long, Float>>();

				for (String facilityId : m_targetFacilityList) {
					for (CollectKeyInfoPK collectInfo : m_targetCollectKeyInfoList) {
						itemCodeTimeDataMap.clear();
						String itemName = collectInfo.getItemName();
						String monitorId = collectInfo.getMonitorId();
						String displayName = collectInfo.getDisplayName();
						String itemMonitor = itemName;
						if (!displayName.equals("") && !itemName.endsWith("[" + displayName + "]")) {
							itemMonitor += "[" + displayName + "]";
						}
						itemMonitor+= "(" + monitorId + ")";
						Integer collectorId = null;
						try {
							collectorId = controller.getCollectId(itemName, displayName, monitorId, facilityId);
						} catch (Exception e) {
							m_log.debug("collectId not found. " + e.getClass().getName() + ", itemName:" + itemName + ", displayName:"+displayName + 
									", monitorId:" + monitorId + ", facilityId:" + facilityId);
						}
						if (collectorId == null) {
							m_log.warn("not found");
						} else {
							
							Map<Long, Float> timeDataMap = new TreeMap<Long, Float>();
							// サマリデータ、または収集データ(raw)のタイプでスイッチ
							switch (m_summaryType) {
							case SummaryTypeConstant.TYPE_AVG_HOUR:
								List<SummaryHour> list_havg = controller.getSummaryHourList(collectorId);
								if (list_havg.isEmpty())
									break;
								for (SummaryHour summaryhour : list_havg) {
									timeDataMap.put(summaryhour.getTime(), summaryhour.getAvg());
								}
								itemCodeTimeDataMap.put(itemMonitor, timeDataMap);
								break;
							case SummaryTypeConstant.TYPE_MIN_HOUR:
								List<SummaryHour> list_hmin = controller.getSummaryHourList(collectorId);
								if (list_hmin.isEmpty())
									break;
								for (SummaryHour summaryhour : list_hmin) {
									timeDataMap.put(summaryhour.getTime(), summaryhour.getMin());
								}
								itemCodeTimeDataMap.put(itemMonitor, timeDataMap);
								break;
							case SummaryTypeConstant.TYPE_MAX_HOUR:
								List<SummaryHour> list_hmax = controller.getSummaryHourList(collectorId);
								if (list_hmax.isEmpty())
									break;
								for (SummaryHour summaryhour : list_hmax) {
									timeDataMap.put(summaryhour.getTime(), summaryhour.getMax());
								}
								itemCodeTimeDataMap.put(itemMonitor, timeDataMap);
								break;
							case SummaryTypeConstant.TYPE_AVG_DAY:
								List<SummaryDay> list_davg = controller.getSummaryDayList(collectorId);
								if (list_davg.isEmpty())
									break;
								for (SummaryDay summaryday : list_davg) {
									timeDataMap.put(summaryday.getTime(), summaryday.getAvg());
								}
								itemCodeTimeDataMap.put(itemMonitor, timeDataMap);
								break;
							case SummaryTypeConstant.TYPE_MIN_DAY:
								List<SummaryDay> list_dmin = controller.getSummaryDayList(collectorId);
								if (list_dmin.isEmpty())
									break;
								for (SummaryDay summaryday : list_dmin) {
									timeDataMap.put(summaryday.getTime(), summaryday.getMin());
								}
								itemCodeTimeDataMap.put(itemMonitor, timeDataMap);
								break;
							case SummaryTypeConstant.TYPE_MAX_DAY:
								List<SummaryDay> list_dmax = controller.getSummaryDayList(collectorId);
								if (list_dmax.isEmpty())
									break;
								for (SummaryDay summaryday : list_dmax) {
									timeDataMap.put(summaryday.getTime(), summaryday.getMax());
								}
								itemCodeTimeDataMap.put(itemMonitor, timeDataMap);
								break;
							case SummaryTypeConstant.TYPE_AVG_MONTH:
								List<SummaryMonth> list_mavg = controller.getSummaryMonthList(collectorId);
								if (list_mavg.isEmpty())
									break;
								for (SummaryMonth summarymonth : list_mavg) {
									timeDataMap.put(summarymonth.getTime(), summarymonth.getAvg());
								}
								itemCodeTimeDataMap.put(itemMonitor, timeDataMap);
								break;
							case SummaryTypeConstant.TYPE_MIN_MONTH:
								List<SummaryMonth> list_mmin = controller.getSummaryMonthList(collectorId);
								if (list_mmin.isEmpty())
									break;
								for (SummaryMonth summarymonth : list_mmin) {
									timeDataMap.put(summarymonth.getTime(), summarymonth.getMin());
								}
								itemCodeTimeDataMap.put(itemMonitor, timeDataMap);
								break;
							case SummaryTypeConstant.TYPE_MAX_MONTH:
								List<SummaryMonth> list_mmax = controller.getSummaryMonthList(collectorId);
								if (list_mmax.isEmpty())
									break;
								for (SummaryMonth summarymonth : list_mmax) {
									timeDataMap.put(summarymonth.getTime(), summarymonth.getMax());
								}
								itemCodeTimeDataMap.put(itemMonitor, timeDataMap);
								break;
							default: // defaultはRAWとする
								List<CollectData> list_data = controller.getCollectDataList(collectorId);
								if (list_data.isEmpty())
									break;
								for (CollectData data : list_data) {
									timeDataMap.put(data.getTime(), data.getValue());
								}
								itemCodeTimeDataMap.put(itemMonitor, timeDataMap);
								break;
							}
						}
						////
						// CSVファイルの作成
						////

						if (itemCodeTimeDataMap.isEmpty()) {
							m_log.debug("run() Create Empty CSV File at targetFacilityId = " + facilityId);
							String createFilePath = exportDirectory
									+ createFileName(HinemosMessage.replace(itemName, new Locale(m_localeStr)), displayName, monitorId, 
											Messages.getString(SummaryTypeConstant.typeToMessageCode(m_summaryType), Locale.ENGLISH), 
											facilityId, m_fileId, "csv");
							m_log.debug("itemCodeTimeDataMap is Empty. addFilePathList add = " + createFilePath);
							createFilePathList.add(createFilePath);
							writeEmptyDataFile(createFilePath);
						} else {
							m_log.debug("run() Create CSV File at targetFacilityId = " + facilityId);
							String createTmpFilePath = exportDirectory
									+ createFileName(HinemosMessage.replace(itemName, new Locale(m_localeStr)), displayName, monitorId, 
											String.valueOf(m_summaryType), facilityId, m_fileId, "tmp");
							String createFilePath = exportDirectory
									+ createFileName(HinemosMessage.replace(itemName, new Locale(m_localeStr)), displayName, monitorId,
											Messages.getString(SummaryTypeConstant.typeToMessageCode(m_summaryType), Locale.ENGLISH),
											facilityId, m_fileId, "csv");

							// ヘッダの追加
							if (m_header) {
								// 最新、最古時間を取得する
								SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
								sdf1.setTimeZone(HinemosTime.getTimeZone());
								List<Long> dateList = new ArrayList<Long>(itemCodeTimeDataMap.get(itemMonitor).keySet());
								String oldDate = sdf1.format(new Date(dateList.get(0)));
								String latestDate = sdf1.format(new Date(dateList.get(dateList.size() - 1)));
								
								writeHeader(m_summaryType, m_localeStr, createTmpFilePath, facilityId, itemMonitor, oldDate, latestDate);
							}

							// データの追加
							writeFile(facilityId, createTmpFilePath, itemCodeTimeDataMap, itemMonitor);

							// 完成したらファイル名を変更
							m_log.debug("itemCodeTimeDataMap is not Empty. addFilePathList add = " + createFilePath);
							
							// ファイル名の重複をなくす(ファイル名短縮により発生する可能性)
							if (!createFilePathList.contains(createFilePath)) {
								createFilePathList.add(createFilePath);
							}
							renameFile(createTmpFilePath, createFilePath);
						}
					}

				}

				////
				// 圧縮
				////
				// 戻り値として渡すパスの設定
				String createTmpArchiveFilePath = exportDirectory
						+ createFileName("", "", "", String.valueOf(m_summaryType), "", m_fileId, "tmp");
				String craeteZipArchiveFilePath = exportDirectory + m_zipName;
				m_log.debug("run() create archive csv file : " + craeteZipArchiveFilePath);

				// 圧縮
				ZipCompresser.archive(createFilePathList, createTmpArchiveFilePath);

				// 元ファイルの削除
				deleteFilePath(createFilePathList);
				createFilePathList.clear();

				// 完成したらファイル名を変更
				createFilePathList.add(craeteZipArchiveFilePath);
				renameFile(createTmpArchiveFilePath, craeteZipArchiveFilePath);

			} catch (HinemosUnknown e) {
				m_log.info("run() " + e.getMessage(), e);
				success = false;
			} catch (Exception e) {
				m_log.info("run() " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				success = false;
			} finally {
				if (!success) {
					m_log.debug("ExportCollectedDataFile.run() : Since processing went wrong, a file is deleted.");
					deleteFilePath(createFilePathList);
				}
			}
			m_log.info("run() CreatePerfFileTask finish!");
		}
	}

}
