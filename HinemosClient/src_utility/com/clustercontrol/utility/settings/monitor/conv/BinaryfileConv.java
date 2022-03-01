/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.conv;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.BinaryCheckInfoResponse;
import org.openapitools.client.model.BinaryCheckInfoResponse.CollectTypeEnum;
import org.openapitools.client.model.BinaryCheckInfoResponse.CutTypeEnum;
import org.openapitools.client.model.BinaryCheckInfoResponse.LengthTypeEnum;
import org.openapitools.client.model.BinaryCheckInfoResponse.TsTypeEnum;
import org.openapitools.client.model.BinaryPatternInfoResponse;
import org.openapitools.client.model.MonitorInfoResponse;

import com.clustercontrol.binary.bean.BinaryConstant;
import com.clustercontrol.binary.util.BinaryBeanUtil;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.monitor.xml.BinaryValue;
import com.clustercontrol.utility.settings.monitor.xml.BinaryfileInfo;
import com.clustercontrol.utility.settings.monitor.xml.BinaryfileMonitor;
import com.clustercontrol.utility.settings.monitor.xml.BinaryfileMonitorList;
import com.clustercontrol.utility.settings.monitor.xml.BinaryfileMonitors;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.util.OpenApiEnumConverter;
import com.clustercontrol.utility.util.UtilityManagerUtil;

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
	private final static String SCHEMA_REVISION = "2";
	
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
	 * @throws ParseException 
	 * @throws InvalidSetting 
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static List<MonitorInfoResponse> createMonitorInfoList(BinaryfileMonitorList binaryfileMonitors) throws ConvertorException, HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, InvalidSetting, ParseException {
		List<MonitorInfoResponse> monitorInfoList = new LinkedList<MonitorInfoResponse>();

		for (BinaryfileMonitor binaryfileMonitor : binaryfileMonitors.getBinaryfileMonitor()) {
			logger.debug("Monitor Id : " + binaryfileMonitor.getMonitor().getMonitorId());
			MonitorInfoResponse monitorInfo = MonitorConv.createMonitorInfo(binaryfileMonitor.getMonitor());

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
	 * @throws RestConnectFailed 
	 * @throws ParseException 
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static BinaryfileMonitors createBinaryMonitors(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		BinaryfileMonitors binaryfileMonitors = new BinaryfileMonitors();

		for (MonitorInfoResponse monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorsettingRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			BinaryfileMonitor binaryfileMonitor = new BinaryfileMonitor();
			binaryfileMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));
			
			int orderNo = 0;
			for (BinaryPatternInfoResponse binaryPatternInfo : monitorInfo.getBinaryPatternInfo()) {
				binaryfileMonitor.addBinaryValue(MonitorConv.createBinaryValue(monitorInfo.getMonitorId(),binaryPatternInfo, ++orderNo));
			}

			binaryfileMonitor.setBinaryfileInfo(createBinaryInfo(monitorInfo));
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
	private static BinaryfileInfo createBinaryInfo(MonitorInfoResponse monitorInfo) {
		BinaryfileInfo binaryfileInfo = new BinaryfileInfo();
		binaryfileInfo.setMonitorTypeId("");
		binaryfileInfo.setMonitorId(monitorInfo.getMonitorId());
		
		binaryfileInfo.setDirectory(monitorInfo.getBinaryCheckInfo().getDirectory());
		binaryfileInfo.setFileName(monitorInfo.getBinaryCheckInfo().getFileName());
		
		String collectType = OpenApiEnumConverter.enumToString(monitorInfo.getBinaryCheckInfo().getCollectType());
		binaryfileInfo.setCollectType(collectType);

		String cutType = OpenApiEnumConverter.enumToString(monitorInfo.getBinaryCheckInfo().getCutType());
		// データ構造と詳細情報の設定.
		BinaryConstant.DataArchType dataArchType = BinaryBeanUtil.getDataArchType(
				collectType,
				cutType,
				monitorInfo.getBinaryCheckInfo().getTagType());
		switch (dataArchType) {

		case INTERVAL:
			// 時間区切り.
			binaryfileInfo.setCutType(BinaryConstant.CUT_TYPE_INTERVAL);
			break;

		case PRESET:
		case CUSTOMIZE:
			// プリセット指定あり・もしくは手入力の場合.
			binaryfileInfo.setCutType(monitorInfo.getBinaryCheckInfo().getTagType());
			binaryfileInfo.setFileHeadSize(monitorInfo.getBinaryCheckInfo().getFileHeadSize());
			String lengthType = OpenApiEnumConverter.enumToString(monitorInfo.getBinaryCheckInfo().getLengthType());
			binaryfileInfo.setLengthType(lengthType);
			//レコードは固定長を選択される場合
			if(LengthTypeEnum.FIXED == monitorInfo.getBinaryCheckInfo().getLengthType()){
				binaryfileInfo.setRecordSize(binaryfileInfo.getRecordSize());
			} else {
				binaryfileInfo.setRecordSize(0);
			}
			binaryfileInfo.setRecordSize(monitorInfo.getBinaryCheckInfo().getRecordSize());
			binaryfileInfo.setRecordHeadSize(monitorInfo.getBinaryCheckInfo().getRecordHeadSize());
			binaryfileInfo.setSizeLength(monitorInfo.getBinaryCheckInfo().getSizeLength());
			binaryfileInfo.setSizePosition(monitorInfo.getBinaryCheckInfo().getSizePosition());
			binaryfileInfo.setHaveTs(monitorInfo.getBinaryCheckInfo().getHaveTs());
			binaryfileInfo.setTsPosition(monitorInfo.getBinaryCheckInfo().getTsPosition());
			String tsType = OpenApiEnumConverter.enumToString(monitorInfo.getBinaryCheckInfo().getTsType());
			binaryfileInfo.setTsType(tsType);
			binaryfileInfo.setLittleEndian(monitorInfo.getBinaryCheckInfo().getLittleEndian());
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
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	private static BinaryCheckInfoResponse  createBinaryCheckInfo(BinaryfileInfo binaryfileInfo) throws InvalidSetting, HinemosUnknown {
		BinaryCheckInfoResponse binaryfileCheckInfo = new BinaryCheckInfoResponse();

		binaryfileCheckInfo.setDirectory(binaryfileInfo.getDirectory());
		binaryfileCheckInfo.setFileName(binaryfileInfo.getFileName());
		
		CollectTypeEnum collectTypeEnum = OpenApiEnumConverter.stringToEnum(binaryfileInfo.getCollectType(), CollectTypeEnum.class);
		binaryfileCheckInfo.setCollectType(collectTypeEnum);

		// データ構造と詳細情報の設定.
		BinaryConstant.DataArchType dataArchType = BinaryBeanUtil.getDataArchType(
				binaryfileInfo.getCollectType(),
				binaryfileInfo.getCutType(),
				binaryfileInfo.getCutType());
		switch (dataArchType) {

		case INTERVAL:
			// 時間区切り.
			binaryfileCheckInfo.setCutType(CutTypeEnum.INTERVAL);
			break;

		case PRESET:
		case CUSTOMIZE:
			// プリセット指定あり・もしくは手入力の場合.
			binaryfileCheckInfo.setCutType(CutTypeEnum.LENGTH);
			binaryfileCheckInfo.setTagType(binaryfileInfo.getCutType());
			binaryfileCheckInfo.setFileHeadSize(binaryfileInfo.getFileHeadSize());

			LengthTypeEnum lengthTypeEnum = OpenApiEnumConverter.stringToEnum(binaryfileInfo.getLengthType(), LengthTypeEnum.class);
			binaryfileCheckInfo.setLengthType(lengthTypeEnum);
			
			//レコードは固定長を選択される場合
			if(LengthTypeEnum.FIXED == lengthTypeEnum){
				binaryfileCheckInfo.setRecordSize(binaryfileInfo.getRecordSize());
			} else {
				binaryfileCheckInfo.setRecordSize(0);
			}
			binaryfileCheckInfo.setRecordHeadSize(binaryfileInfo.getRecordHeadSize());
			binaryfileCheckInfo.setSizeLength(binaryfileInfo.getSizeLength());
			binaryfileCheckInfo.setSizePosition(binaryfileInfo.getSizePosition());
			binaryfileCheckInfo.setHaveTs(binaryfileInfo.getHaveTs());
			binaryfileCheckInfo.setTsPosition(binaryfileInfo.getTsPosition());
			TsTypeEnum tstypeEnum = OpenApiEnumConverter.stringToEnum(binaryfileInfo.getTsType(), TsTypeEnum.class);
			binaryfileCheckInfo.setTsType(tstypeEnum);
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
