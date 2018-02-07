/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.conv;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.binary.bean.BinaryConstant;
import com.clustercontrol.binary.util.BinaryBeanUtil;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.monitor.xml.BinaryValue;
import com.clustercontrol.utility.settings.monitor.xml.BinaryfileInfo;
import com.clustercontrol.utility.settings.monitor.xml.BinaryfileMonitor;
import com.clustercontrol.utility.settings.monitor.xml.BinaryfileMonitorList;
import com.clustercontrol.utility.settings.monitor.xml.BinaryfileMonitors;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.ws.monitor.BinaryCheckInfo;
import com.clustercontrol.ws.monitor.BinaryPatternInfo;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;

/**
 * バイナリファイル 監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 *
 * @version 6.1.0
 * @since 6.1.0
 *
 */
public class BinaryfileConv {
	private final static Log logger = LogFactory.getLog(BinaryfileConv.class);

	private final static String SCHEMA_TYPE = "I";
	private final static String SCHEMA_VERSION = "1";
	private final static String SCHEMA_REVISION = "1";
	
	/**
	 * <BR>
	 *
	 * @return
	 */
	public static SchemaInfo getSchemaVersion(){
		SchemaInfo schema = new SchemaInfo();

		schema.setSchemaType(SCHEMA_TYPE);
		schema.setSchemaVersion(SCHEMA_VERSION);
		schema.setSchemaRevision(SCHEMA_REVISION);

		return schema;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	public static int checkSchemaVersion(SchemaInfo schemaInfo) {
		return BaseConv.checkSchemaVersion(
				SCHEMA_TYPE,
				SCHEMA_VERSION,
				SCHEMA_REVISION,
				schemaInfo.getSchemaType(),
				schemaInfo.getSchemaVersion(),
				schemaInfo.getSchemaRevision()
				);
	}

	/**
	 * Castor で作成した形式の バイナリファイル 監視設定情報を DTO へ変換する<BR>
	 *
	 * @param
	 * @return
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static List<MonitorInfo> createMonitorInfoList(BinaryfileMonitorList binaryfileMonitors) throws ConvertorException, HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		List<MonitorInfo> monitorInfoList = new LinkedList<MonitorInfo>();

		for (BinaryfileMonitor binaryfileMonitor : binaryfileMonitors.getBinaryfileMonitor()) {
			logger.debug("Monitor Id : " + binaryfileMonitor.getMonitor().getMonitorId());
			MonitorInfo monitorInfo = MonitorConv.createMonitorInfo(binaryfileMonitor.getMonitor());

			BinaryValue[] binaryVals = binaryfileMonitor.getBinaryValue();
			MonitorConv.sort(binaryVals);
			for (BinaryValue binaryValue : binaryVals) {
				monitorInfo.getBinaryPatternInfo().add(MonitorConv.createMonitorBinaryValueInfo(binaryValue));
			}

			monitorInfo.setBinaryCheckInfo(createBinaryCheckInfo(binaryfileMonitor.getBinaryfileInfo()));
			monitorInfoList.add(monitorInfo);
		}

		return monitorInfoList;
	}

	/**
	 * DTO から、Castor で作成した形式の バイナリファイル 監視設定情報へ変換する<BR>
	 *
	 * @param
	 * @return
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static BinaryfileMonitors createBinaryMonitors(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		BinaryfileMonitors binaryfileMonitors = new BinaryfileMonitors();

		for (MonitorInfo monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorSettingEndpointWrapper
					.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			BinaryfileMonitor binaryfileMonitor = new BinaryfileMonitor();
			binaryfileMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));
			
			int orderNo = 0;
			for (BinaryPatternInfo binaryPatternInfo : monitorInfo.getBinaryPatternInfo()) {
				binaryfileMonitor.addBinaryValue(MonitorConv.createBinaryValue(binaryPatternInfo, ++orderNo));
			}

			binaryfileMonitor.setBinaryfileInfo(createBinaryInfo(monitorInfo.getBinaryCheckInfo()));
			binaryfileMonitors.addBinaryfileMonitor(binaryfileMonitor);
		}

		binaryfileMonitors.setCommon(MonitorConv.versionDto2Xml());
		binaryfileMonitors.setSchemaInfo(getSchemaVersion());

		return binaryfileMonitors;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static BinaryfileInfo createBinaryInfo(BinaryCheckInfo binaryfileCheckInfo) {
		BinaryfileInfo binaryfileInfo = new BinaryfileInfo();
		binaryfileInfo.setMonitorTypeId("");
		binaryfileInfo.setMonitorId(binaryfileCheckInfo.getMonitorId());
		
		binaryfileInfo.setDirectory(binaryfileCheckInfo.getDirectory());
		binaryfileInfo.setFileName(binaryfileCheckInfo.getFileName());
		
		binaryfileInfo.setCollectType(binaryfileCheckInfo.getCollectType());

		// データ構造と詳細情報の設定.
		BinaryConstant.DataArchType dataArchType = BinaryBeanUtil.getDataArchType(
						binaryfileCheckInfo.getCollectType(),
						binaryfileCheckInfo.getCutType(),
						binaryfileCheckInfo.getTagType());
		switch (dataArchType) {

		case INTERVAL:
			// 時間区切り.
			binaryfileInfo.setCutType(BinaryConstant.CUT_TYPE_INTERVAL);
			break;

		case PRESET:
		case CUSTOMIZE:
			// プリセット指定あり・もしくは手入力の場合.
			binaryfileInfo.setCutType(binaryfileCheckInfo.getTagType());
			binaryfileInfo.setFileHeadSize(binaryfileCheckInfo.getFileHeadSize());
			binaryfileInfo.setLengthType(binaryfileCheckInfo.getLengthType());
			binaryfileInfo.setRecordSize(binaryfileCheckInfo.getRecordSize());
			binaryfileInfo.setRecordHeadSize(binaryfileCheckInfo.getRecordHeadSize());
			binaryfileInfo.setSizeLength(binaryfileCheckInfo.getSizeLength());
			binaryfileInfo.setSizePosition(binaryfileCheckInfo.getSizePosition());
			binaryfileInfo.setHaveTs(binaryfileCheckInfo.isHaveTs());
			binaryfileInfo.setTsPosition(binaryfileCheckInfo.getTsPosition());
			binaryfileInfo.setTsType(binaryfileCheckInfo.getTsType());
			binaryfileInfo.setLittleEndian(binaryfileCheckInfo.isLittleEndian());
			break;

		case NONE:
		default:
			// ファイル全体監視等は設定しない.
			break;

		}

		return binaryfileInfo;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static BinaryCheckInfo createBinaryCheckInfo(BinaryfileInfo binaryfileInfo) {
		BinaryCheckInfo binaryfileCheckInfo = new BinaryCheckInfo();
		binaryfileCheckInfo.setMonitorTypeId("");
		binaryfileCheckInfo.setMonitorId(binaryfileInfo.getMonitorId());
		
		binaryfileCheckInfo.setDirectory(binaryfileInfo.getDirectory());
		binaryfileCheckInfo.setFileName(binaryfileInfo.getFileName());
		
		binaryfileCheckInfo.setCollectType(binaryfileInfo.getCollectType());

		// データ構造と詳細情報の設定.
		BinaryConstant.DataArchType dataArchType = BinaryBeanUtil.getDataArchType(
				binaryfileInfo.getCollectType(),
				binaryfileInfo.getCutType(),
				binaryfileInfo.getCutType());
		switch (dataArchType) {

		case INTERVAL:
			// 時間区切り.
			binaryfileCheckInfo.setCutType(BinaryConstant.CUT_TYPE_INTERVAL);
			break;

		case PRESET:
		case CUSTOMIZE:
			// プリセット指定あり・もしくは手入力の場合.
			binaryfileCheckInfo.setCutType(BinaryConstant.CUT_TYPE_LENGTH);
			binaryfileCheckInfo.setTagType(binaryfileInfo.getCutType());
			binaryfileCheckInfo.setFileHeadSize(binaryfileInfo.getFileHeadSize());
			binaryfileCheckInfo.setLengthType(binaryfileInfo.getLengthType());
			binaryfileCheckInfo.setRecordSize(binaryfileInfo.getRecordSize());
			binaryfileCheckInfo.setRecordHeadSize(binaryfileInfo.getRecordHeadSize());
			binaryfileCheckInfo.setSizeLength(binaryfileInfo.getSizeLength());
			binaryfileCheckInfo.setSizePosition(binaryfileInfo.getSizePosition());
			binaryfileCheckInfo.setHaveTs(binaryfileInfo.getHaveTs());
			binaryfileCheckInfo.setTsPosition(binaryfileInfo.getTsPosition());
			binaryfileCheckInfo.setTsType(binaryfileInfo.getTsType());
			binaryfileCheckInfo.setLittleEndian(binaryfileInfo.getLittleEndian());
			break;

		case NONE:
			break;

		default:
			String msg = "Unknown CollectType : " + binaryfileInfo.getCollectType() + ", CutType" + binaryfileInfo.getCutType();
			logger.error(msg);
			throw new IllegalArgumentException(msg);
		}

		return binaryfileCheckInfo;
	}
}
