/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.selfcheck.monitor;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.selfcheck.TableSizeConfig;
import com.clustercontrol.selfcheck.util.TableSizeQueryExecuter;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * テーブルの蓄積量を確認する処理の実装クラス
 */
public class TableSizeMonitor extends SelfCheckMonitorBase {

	private static Log m_log = LogFactory.getLog( TableSizeMonitor.class );

	public final String monitorId = "SYS_TBLSIZE";
	public final String application = "SELFCHECK (Table Size)";

	public enum ThresholdType { MBYTE, COUNT };

	/**
	 * コンストラクタ
	 * @param dataSourceName データソース名（"HinemosDS"など）
	 */
	public TableSizeMonitor() {
	}

	/**
	 * セルフチェック処理名
	 */
	@Override
	public String toString() {
		return MessageConstant.SELFCHECK_TYPE_TABLE_SIZE.getMessage();
	}

	/**
	 * 監視項目ID
	 */
	@Override
	public String getMonitorId() {
		return monitorId;
	}

	/**
	 * テーブルのサイズチェック処理
	 * @throws Exception 
	 */
	@Override
	public void execute() throws InvalidSetting {
		if (!HinemosPropertyCommon.selfcheck_monitoring_table_size.getBooleanValue()) {
			m_log.debug("skip");
			return;
		}

		String tableSizeRaw = HinemosPropertyCommon.selfcheck_monitoring_table_size_list.getStringValue();
		List<TableSizeConfig> tableSizes = new ArrayList<TableSizeConfig>();
		for (String tableSize : tableSizeRaw.split(",")) {
			String[] pair = tableSize.split(":");
			if (pair.length == 3) {
				ThresholdType type;
				try {
					type = ThresholdType.valueOf(pair[2]);
				} catch (IllegalArgumentException e) {
					m_log.warn("table size monitoring - invalid type, set [MBYTE|COUNT]. : " + tableSize);
					continue;
				}
				tableSizes.add(new TableSizeConfig(pair[0], type, Long.parseLong(pair[1])));
			}
		}
		List<TableSizeConfig> tableList = Collections.unmodifiableList(tableSizes);

		for (TableSizeConfig config : tableList) {
			String tableName = config.tableName;
			long threshold = config.threshold;
			ThresholdType thresholdType = config.thresdholdType;

			String subKey = tableName;
			
			/** ローカル変数 */
			JpaTransactionManager tm = null;
	
			long size = -1;
			long thresholdOrig = threshold;
			long physicalSize = -1;
			double physicalSizeMByte = -1.0;
			long count = -1;
	
			long sizeThresdhold = threshold;
	
			boolean warn = true;
	
			/** メイン処理 */
			m_log.debug("monitoring table size. (tableName=" + tableName + ", threshold=" + threshold + " [" + getThresholdUnit(thresholdType) + "])");
	
			try {
				// データソースのオブジェクトをJNDI経由で取得し、取得したコネクションが正しく動作するかを確認する
				tm = new JpaTransactionManager();
				tm.begin();
	
				// 判定対象値を取得する
				switch (thresholdType) {
				case MBYTE :
					// convert MByte to byte
					sizeThresdhold = threshold * 1024 * 1024;
					size = getTableSize(tableName);
					break;
				case COUNT :
					sizeThresdhold = threshold;
					size = getTableCount(tableName);
					break;
				default :
					String message = "monitoring type is invalid. (type = " + thresholdType + ")";
					m_log.info(message);
					throw new InvalidSetting(message);
				}
	
				if (size <= sizeThresdhold) {
					if (m_log.isDebugEnabled()) {
						m_log.debug("table's size is low. (tableName=" + tableName + ", size=" + size + ", threshold=" + thresholdOrig + " [" + getThresholdUnit(thresholdType) + "])");
					}
	
					warn = false;
				}
	
				if (warn) {
					m_log.info("log table's size is too high. (tableName=" + tableName + ", size=" + size
							+ ", threshold=" + thresholdOrig + " " + getThresholdUnit(thresholdType) + ")");
				}
				if (!isNotify(subKey, warn)) {
					continue;
				}
				switch (thresholdType) {
				case MBYTE:
					physicalSize = size;
					count = getTableCount(tableName);
					break;
				case COUNT:
					physicalSize = getTableSize(tableName);
					count = size;
					break;
				default:
					// 1回目のチェックで引っ掛かるので通常ここに到達することはない
					m_log.info("monitoring type is invalid. (type=" + thresholdType + ")");
				}
			} catch (Exception e) {
				if (tm != null)
					tm.rollback();
				m_log.warn("monitoring log table failure. (tableName=" + tableName + ", threshold=" + threshold + " [" + getThresholdUnit(thresholdType) + "])", e);
				throw e;
			} finally {
				if (tm != null) {
					tm.close();
				}
			}
			physicalSizeMByte = physicalSize / 1024.0 / 1024.0;
	
			String[] msgAttr1 = { tableName, String.format("%.2f", physicalSizeMByte), Long.toString(count), Long.toString(thresholdOrig), getThresholdUnit(thresholdType) };
			AplLogger.put(InternalIdCommon.SYS_SFC_SYS_006, msgAttr1,
					"stored data (" +
							tableName +
							") is too large (" +
							String.format("%.2f", physicalSizeMByte) +
							" [mbyte], " +
							count +
							" [rows(statistics)] > threshold " +
							thresholdOrig +
							" " +
							getThresholdUnit(thresholdType)  +
					").");
		}

		return;
	}

	/**
	 * 特定のテーブルの物理サイズを返すメソッド
	 * @param tableName 対象とするテーブル名
	 * @return 物理サイズ
	 */
	public static long getTableSize(String tableName) {
		return TableSizeQueryExecuter.getTableSize(tableName);
	}

	/**
	 * 特定のテーブルのレコード数（統計情報から取得した概算値）を返すメソッド
	 * @param tableName 対象とするテーブル名（スキーマ.テーブルの形式でなくてはならない）
	 * @return レコード数
	 * @throws InvalidSetting 
	 */
	public static long getTableCount(String tableName) throws InvalidSetting {
		return TableSizeQueryExecuter.getTableCount(tableName);
	}

	private static String getThresholdUnit(ThresholdType type) {
		// ローカル変数
		String unit = "";

		// メイン処理
		switch (type) {
		case MBYTE :
			unit = "[mbyte]";
			break;
		case COUNT :
			unit = "[rows]";
			break;
		default :
		}

		return unit;
	}

}
