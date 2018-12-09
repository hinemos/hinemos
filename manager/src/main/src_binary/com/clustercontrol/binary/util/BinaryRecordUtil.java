/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.binary.bean.BinaryRecordDTO;
import com.clustercontrol.binary.bean.BinaryResultDTO;
import com.clustercontrol.binary.bean.BinarySample;
import com.clustercontrol.binary.bean.BinaryTagConstant;
import com.clustercontrol.hub.bean.CollectStringTag;
import com.clustercontrol.hub.bean.StringSampleTag;
import com.clustercontrol.hub.bean.ValueType;
import com.clustercontrol.util.BinaryUtil;

/**
 * バイナリデータUtil<br>
 * <br>
 * 基本的にArrayList<Byte>でバイナリデータを扱う想定のUtil.
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
public class BinaryRecordUtil {

	/** ログ出力用インスタンス */
	public static final Log m_log = LogFactory.getLog(BinaryRecordUtil.class);
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	/**
	 * 任意バイナリファイルの監視結果を収集値として加工.<br>
	 * <br>
	 * 個々のレコードを複数まとめて初めてファイルとして成立.
	 * 
	 * @param facilityId
	 *            監視実施したFacilityID
	 * @param input
	 *            監視結果<br>
	 *            <br>
	 * @return 加工済の収集データ.
	 */
	public static List<BinarySample> resultToSample(String facilityId, BinaryResultDTO input) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start.");

		// ループで使う変数初期化.
		List<BinarySample> output = new ArrayList<BinarySample>();
		BinarySample record = null;
		List<Byte> tmpByteList = null;

		// xml受信データを収集ビューレコードとして加工.
		for (BinaryRecordDTO recordDto : input.binaryRecords) {
			// ログ出力.
			m_log.debug(methodName + DELIMITER + "start converting xml data : [" + recordDto.getOxStr() + "]");

			// 共通フィールド設定.
			record = mapRecDtoToSample(facilityId, input, recordDto);

			// バイナリ値の設定.
			tmpByteList = new ArrayList<Byte>();
			record.setValue(new ArrayList<Byte>());
			// 本体レコードデータの付与(ファイル全体監視はファイルヘッダの設定なし).
			if (!recordDto.getBase64Str().isEmpty()) {
				tmpByteList = BinaryUtil.base64ToList(recordDto.getBase64Str());
				if (tmpByteList == null) {
					// レコードデータの文字列不正.
					m_log.warn(methodName + DELIMITER + "record binary are failed to decode.");
					return null;
				}
				record.getValue().addAll(tmpByteList);
				m_log.debug(methodName + DELIMITER
						+ String.format("record binary to the byte list. size=%d", tmpByteList.size()));
			}

			output.add(record);
		}
		if (output != null && output.isEmpty()) {
			m_log.debug(methodName + DELIMITER + "return output count=" + output.size());
		} else {
			m_log.debug(methodName + DELIMITER + "return output count=0.");
		}
		return output;
	}

	/**
	 * ログ/連続バイナリデータの監視結果を収集値として加工.<br>
	 * <br>
	 * 基本的には個々のレコードがファイルとして成立するよう整形.<br>
	 * ※レコードサイズによっては複数レコードに分割.
	 * 
	 * @param facilityId
	 *            監視実施したFacilityID
	 * @param input
	 *            監視結果<br>
	 *            <br>
	 * @return 加工済の収集データ.
	 */
	public static List<BinarySample> sqntlResultToSample(String facilityId, BinaryResultDTO input) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		// ループで使う変数初期化.
		List<BinarySample> output = new ArrayList<BinarySample>();
		BinarySample record = null;
		List<Byte> tmpByteList = null;
		List<Byte> fileHeader = new ArrayList<Byte>();

		// ファイルヘッダの変換と長さチェック.
		if (!input.binaryFile.getFileHeader().isEmpty()) {
			fileHeader = BinaryUtil.base64ToList(input.binaryFile.getFileHeader());
			if (fileHeader == null) {
				// ファイルヘッダの文字列不正.
				m_log.warn(methodName + DELIMITER + "file header binary are failed to decode.");
				return null;
			}
			long settingSize = input.monitorInfo.getBinaryCheckInfo().getFileHeadSize();
			if (fileHeader.size() < settingSize) {
				m_log.warn(methodName + DELIMITER
						+ String.format(
								"failed to get file header. fileHeaderSize(setting)=%d, fileHeaderSize(data)=%d",
								settingSize, fileHeader.size()));
				return null;
			}
		}

		// xml受信データを収集ビューレコードとして加工.
		for (BinaryRecordDTO recordDto : input.binaryRecords) {
			// ログ出力.
			m_log.debug(methodName + DELIMITER + "start converting xml data : [" + recordDto.getOxStr() + "]");

			// 共通フィールド設定.
			record = mapRecDtoToSample(facilityId, input, recordDto);
			record.setValue(new ArrayList<Byte>());
			// バイナリ値の設定.
			tmpByteList = new ArrayList<Byte>();
			// ファイルヘッダの付与(空文字もありえる).
			if (!fileHeader.isEmpty()) {
				record.getValue().addAll(fileHeader);
				m_log.debug(methodName + DELIMITER
						+ String.format("add file header to the byte list. size=%d", fileHeader.size()));
			}
			// レコードデータ(ヘッダ＋本体)の付与(空文字もありえる).
			if (!recordDto.getBase64Str().isEmpty()) {
				tmpByteList = BinaryUtil.base64ToList(recordDto.getBase64Str());
				if (tmpByteList == null) {
					// レコードデータの文字列不正.
					m_log.warn(methodName + DELIMITER + "record binary are failed to decode.");
					return null;
				}
				record.getValue().addAll(tmpByteList);
				m_log.debug(methodName + DELIMITER
						+ String.format("add record binary to the byte list. size=%d", tmpByteList.size()));
			}

			output.add(record);
		}

		if (output != null && output.isEmpty()) {
			m_log.debug(methodName + DELIMITER + "return output count=" + output.size());
		} else {
			m_log.debug(methodName + DELIMITER + "return output count=0.");
		}
		return output;
	}

	/**
	 * xml受信値とSamplクラスのマッピング. <br>
	 * <br>
	 * ログ・連続バイナリと任意バイナリファイルの共通フィールドのみ設定.<br>
	 * ※バイナリ値はレコード単位が変わるので対象外.<br>
	 * 
	 * @param facilityId
	 *            監視実施したFacilityID
	 * @param allResult
	 *            全ての監視結果<br>
	 * @param recordResult
	 *            レコードに紐づく監視結果<br>
	 *            <br>
	 * @return マッピング済のBinarySample.
	 */
	private static BinarySample mapRecDtoToSample(String facilityId, BinaryResultDTO allResult,
			BinaryRecordDTO recordResult) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start.");

		BinarySample record = new BinarySample();

		// 各フィールド設定.
		record.setDateTime(allResult.msgInfo.getGenerationDate());
		record.setMonitorId(allResult.monitorInfo.getMonitorId());
		record.setFacilityId(facilityId);
		record.setTargetName(allResult.monitorInfo.getBinaryCheckInfo().getBinaryfile());
		record.setCollectType(allResult.monitorInfo.getBinaryCheckInfo().getCollectType());
		record.setFilePosition(recordResult.getFilePosition());
		record.setRecKey(recordResult.getKey());
		record.setFileHeaderSize(allResult.monitorInfo.getBinaryCheckInfo().getFileHeadSize());
		record.setRecordTime(recordResult.getRecTime());

		// タグデータの設定.
		List<StringSampleTag> tmpTagList = new ArrayList<StringSampleTag>();
		StringSampleTag tmpTag = new StringSampleTag();
		// ファイル名タグの設定.
		tmpTag.setKey(BinaryTagConstant.CommonTagName.FILE_NAME);
		tmpTag.setType(ValueType.string);
		tmpTag.setValue(allResult.monitorInfo.getBinaryCheckInfo().getBinaryfile());
		tmpTagList.add(tmpTag);
		// レコードバイナリタイムスタンプのタグ設定.
		if (recordResult.getRecTime().longValue() > 0) {
			tmpTag = new StringSampleTag();
			tmpTag.setTag(CollectStringTag.TIMESTAMP_IN_LOG);
			tmpTag.setValue(recordResult.getRecTime().toString());
			tmpTagList.add(tmpTag);
		}
		// ファイルタグの設定.
		tmpTagList.addAll(convertTags(allResult.binaryFile.getTags()));
		// レコードタグの設定.
		tmpTagList.addAll(convertTags(recordResult.getTags()));
		// タグデータセット.
		record.setTagList(tmpTagList);

		m_log.debug(methodName + DELIMITER + "return BinarySample. monitorID=" + record.getMonitorId() + ", recordKey="
				+ record.getRecKey());
		return record;
	}

	/**
	 * xml受信tag文字列をDB格納用のリストに変換.<br>
	 * 
	 * @param tags
	 *            xml受信tag文字列
	 * @return DB格納用のリスト、引数不正の場合は空のリスト返却.
	 */
	private static List<StringSampleTag> convertTags(String tags) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start.");
		List<StringSampleTag> tagList = new ArrayList<StringSampleTag>();
		Map<String, String> tagMap = BinaryBeanUtil.tagStrToMap(tags);

		// tagMap取得不可の場合0件リストオブジェクトとして返却.
		if (tagMap == null || tagMap.isEmpty()) {
			m_log.debug(methodName + DELIMITER + "tagMap is empty.");
			return tagList;
		}

		// マップのDBタグ変換(全てstring型とする).
		StringSampleTag tmpTag = null;
		for (Entry<String, String> tagEntry : tagMap.entrySet()) {
			tmpTag = new StringSampleTag();
			tmpTag.setKey(tagEntry.getKey());
			tmpTag.setType(ValueType.string);
			tmpTag.setValue(tagEntry.getValue());
			tagList.add(tmpTag);
		}

		m_log.debug(methodName + DELIMITER + "return tagList. size=" + tagList.size());
		return tagList;
	}

}
