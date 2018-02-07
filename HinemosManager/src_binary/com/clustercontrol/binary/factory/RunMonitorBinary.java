/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.factory;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.binary.bean.BinaryConstant;
import com.clustercontrol.binary.bean.BinaryRecordDTO;
import com.clustercontrol.binary.bean.BinaryResultDTO;
import com.clustercontrol.binary.bean.BinarySample;
import com.clustercontrol.binary.model.BinaryCheckInfo;
import com.clustercontrol.binary.model.CollectBinaryData;
import com.clustercontrol.binary.model.CollectBinaryDataTag;
import com.clustercontrol.binary.util.BinaryBeanUtil;
import com.clustercontrol.binary.util.BinaryRecordUtil;
import com.clustercontrol.binary.util.CollectBinaryDataJdbcBatchInsert;
import com.clustercontrol.binary.util.CollectBinaryTagJdbcBatchInsert;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JdbcBatchExecutor;
import com.clustercontrol.commons.util.JdbcBatchQuery;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.hub.bean.CollectStringTag;
import com.clustercontrol.hub.bean.StringSampleTag;
import com.clustercontrol.hub.model.CollectDataTagPK;
import com.clustercontrol.hub.model.CollectStringDataPK;
import com.clustercontrol.hub.util.CollectStringDataUtil;
import com.clustercontrol.hub.util.DataId.GeneratorFor;
import com.clustercontrol.hub.util.DataIdGenerator;
import com.clustercontrol.jobmanagement.bean.MonitorJobEndNode;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.jobmanagement.util.MonitorJobWorker;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.BinaryUtil;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

public class RunMonitorBinary {

	/** ログ出力用インスタンス */
	public static final Log m_log = LogFactory.getLog(RunMonitorBinary.class);
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	/** 監視ジョブの実行結果 */
	private List<MonitorJobEndNode> monitorJobEndNodeList = new ArrayList<>();

	/**
	 * バイナリ 監視結果を収集用に整形して登録する.<br>
	 * 
	 * @param facilityId
	 *            監視実施したFacilityID
	 * @param result
	 *            監視結果
	 */
	public void runCollect(String facilityId, BinaryResultDTO result) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		// 収集フラグオフもしくは監視ジョブの場合は実施対象外.
		if (result.monitorInfo.getCollectorFlg() == null || !result.monitorInfo.getCollectorFlg()
				|| result.runInstructionInfo != null) {
			m_log.debug(methodName + DELIMITER + "aside from " + methodName + " monitorId="
					+ result.monitorInfo.getMonitorId());
			return;
		}

		// ファイルヘッダチェック(まだデータがbase64形式なので長さチェックはここではやらない).
		if (result.monitorInfo.getBinaryCheckInfo().getFileHeadSize() > 0
				&& (result.binaryFile.getFileHeader() == null || result.binaryFile.getFileHeader().isEmpty())) {
			m_log.warn(methodName + DELIMITER
					+ String.format(
							"skip to store because failed to get file header. fileHeaderSize(monitorSetting)=%d",
							result.monitorInfo.getBinaryCheckInfo().getFileHeadSize()));
			return;
		}

		// 収集用にxml受信データを加工.
		String collectType = result.monitorInfo.getBinaryCheckInfo().getCollectType();
		List<BinarySample> records = null;

		if (BinaryConstant.COLLECT_TYPE_WHOLE_FILE.equals(collectType)) {
			// 収集方式ファイル全体の場合.
			records = BinaryRecordUtil.resultToSample(facilityId, result);
		} else {
			// 収集方式増分のみの場合.
			records = BinaryRecordUtil.sqntlResultToSample(facilityId, result);
		}

		// xml受信データ不正等で加工できなかった場合.
		if (records == null) {
			m_log.warn(methodName + DELIMITER + "records are null. monitorId=" + result.monitorInfo.getMonitorId());
			return;
		}

		// DBへの登録.
		this.store(records);
	}

	/**
	 * バイナリファイルの監視結果を収集値としてDB登録.
	 * 
	 * @param records
	 *            加工済の収集データ(Agentで分割したレコード単位).
	 */
	private void store(List<BinarySample> records) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		// トランザクション境界スタート.
		JpaTransactionManager jtm = new JpaTransactionManager();
		jtm.begin();

		// ループ用変数初期化.
		List<CollectBinaryData> collectEntityList = new ArrayList<CollectBinaryData>();
		CollectBinaryData collectEntitiy = null;
		List<CollectBinaryDataTag> collectTagList = null;
		CollectDataTagPK collectTagPk = null;
		Long collectId = null;
		Long dataId = null;
		CollectBinaryDataTag collectTag = null;

		// 収集値をDB登録用に整形.
		for (BinarySample agentRec : records) {
			// debug用のログ.
			if (m_log.isDebugEnabled()) {
				m_log.debug(methodName + DELIMITER + "facilityId = " + agentRec.getFacilityId() + ", dateTime = "
						+ agentRec.getDateTime());
				m_log.debug(methodName + DELIMITER + "recordKey = " + agentRec.getRecKey());
			}

			// レコード各フィールドの設定.
			collectEntitiy = new CollectBinaryData();
			collectEntitiy.setId(this.getPrimaryKey(agentRec, jtm));

			collectEntitiy.setCollectType(agentRec.getCollectType());
			collectEntitiy.setFilePosition(agentRec.getFilePosition());
			if (BinaryConstant.COLLECT_TYPE_WHOLE_FILE.equals(agentRec.getCollectType())) {
				// ファイル全体監視の場合は"監視時刻_収集ID_ファイルパス"で収集結果の表示レコードを一意に識別.
				collectEntitiy.setFileKey(agentRec.getDateTime().toString() + "_"
						+ collectEntitiy.getId().getCollectId() + "_" + agentRec.getTargetName());
			} else {
				// 増分監視の場合は"監視時刻_収集ID_レコードキー"で収集結果の表示レコードを一意に識別.
				collectEntitiy.setFileKey(agentRec.getDateTime().toString() + "_"
						+ collectEntitiy.getId().getCollectId() + "_" + agentRec.getRecKey());
			}
			collectEntitiy.setRecordKey(agentRec.getRecKey());
			collectEntitiy.setFileHeadSize(agentRec.getFileHeaderSize());
			collectEntitiy.setValue(BinaryUtil.listToArray(agentRec.getValue()));

			// タグテーブルjoin用のキー取得.
			collectId = collectEntitiy.getCollectId();
			dataId = collectEntitiy.getDataId();

			// 時刻の設定(Agent収集日時).
			collectEntitiy.setTime(agentRec.getDateTime());
			// バイナリログの場合は時刻をAgent収集日時からレコード内タイムスタンプに切替.
			if (agentRec.getRecordTime().longValue() > 0) {
				Long time = collectEntitiy.getTime();
				collectEntitiy.setTime(agentRec.getRecordTime());
				// Agent収集日時は受信日時としてタグ保存.
				collectTagPk = new CollectDataTagPK(collectId, dataId, CollectStringTag.TIMESTAMP_RECIEVED.name());
				collectTag = new CollectBinaryDataTag(collectTagPk, CollectStringTag.TIMESTAMP_RECIEVED.valueType(),
						time.toString());
				collectEntitiy.getTagList().add(collectTag);
			}

			// タグの設定.
			collectTagList = new ArrayList<CollectBinaryDataTag>();
			if (!agentRec.getTagList().isEmpty()) {
				Map<String, CollectBinaryDataTag> tagMap = new HashMap<>();
				for (StringSampleTag tag : agentRec.getTagList()) {
					collectTagPk = new CollectDataTagPK(collectId, dataId, tag.getKey());
					tagMap.put(tag.getKey(), new CollectBinaryDataTag(collectTagPk, tag.getType(), tag.getValue()));
				}
				collectTagList.addAll(tagMap.values());
			}
			collectEntitiy.setTagList(collectTagList);

			// DB登録用のリストに追加.
			collectEntityList.add(collectEntitiy);
		}

		// for文でキーデータの生成等行ったので一度commit.
		jtm.commit();

		// データ挿入クエリ発行.
		List<JdbcBatchQuery> query = new ArrayList<JdbcBatchQuery>();
		if (!collectEntityList.isEmpty()) {
			// タグ以外のデータ挿入.
			query.add(new CollectBinaryDataJdbcBatchInsert(collectEntityList));
			// ログ出力用.
			StringBuilder logArg = new StringBuilder();
			logArg.append("[");

			for (CollectBinaryData data : collectEntityList) {
				if (!data.getTagList().isEmpty()) {
					// タグデータ挿入.
					query.add(new CollectBinaryTagJdbcBatchInsert(data.getTagList()));

					if (m_log.isDebugEnabled()) {
						// ログ出力用の文字列生成.
						StringBuilder logTagArg = new StringBuilder();
						logTagArg.append("[");
						for (int i = 0; i < data.getTagList().size(); i++) {
							logTagArg.append("[");
							logTagArg.append("collectId=");
							logTagArg.append(data.getTagList().get(i).getCollectId());
							logTagArg.append(", dataId=");
							logTagArg.append(data.getTagList().get(i).getDataId());
							logTagArg.append(", key=");
							logTagArg.append(data.getTagList().get(i).getKey());
							logTagArg.append(", value=");
							logTagArg.append(data.getTagList().get(i).getValue());
							logTagArg.append("]");
						}
						logTagArg.append("]");
						m_log.debug(
								methodName + DELIMITER
										+ String.format(
												"ready batch insert for the tagList. tagListsize=%d, tagList=%s",
												data.getTagList().size(), logTagArg));
					}
				}
				if (m_log.isDebugEnabled()) {
					logArg.append("[");
					logArg.append("collectId=");
					logArg.append(data.getCollectId());
					logArg.append(", dataId=");
					logArg.append(data.getDataId());
					logArg.append(", recordKey=");
					logArg.append(data.getRecordKey());
					logArg.append("]");

				}
			}
			if (m_log.isDebugEnabled()) {
				// ログ出力.
				logArg.append("]");
				m_log.debug(methodName + DELIMITER
						+ String.format(
								"ready batch insert for the collectEntityList. collectEntityList size=%d, collectEntityList=%s",
								collectEntityList.size(), logArg));
			}
		}
		// クエリ実行(コミット処理含む).
		JdbcBatchExecutor.execute(query);

		// トランザクション境界終了.
		jtm.close();
		m_log.debug(methodName + DELIMITER + "end");
	}

	/**
	 * 主キーの取得.
	 * 
	 * @param agentRec
	 *            加工済の収集データ(Agentで分割したレコード単位).
	 * @param jtm
	 *            トランザクション制御オブジェクト(収集ID生成用).
	 */
	private CollectStringDataPK getPrimaryKey(BinarySample agentRec, JpaTransactionManager jtm) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		// 収集IDの取得/生成.
		m_log.debug("persist targetName = " + agentRec.getTargetName());
		// 収集ID用にファイル名整形(スキーマの制限により上限512 文字).
		String targetName = "";
		if (agentRec.getTargetName().length() > 512) {
			targetName = agentRec.getTargetName().substring(0, 512);
		} else {
			targetName = agentRec.getTargetName();
		}
		Long collectId = CollectStringDataUtil.getCollectStringKeyInfoPK(agentRec.getMonitorId(),
				agentRec.getFacilityId(), targetName, jtm);

		// データIDの設定(テーブル単位で自動生成).
		Long dataId = DataIdGenerator.getNext(GeneratorFor.BINARY);
		if (dataId == DataIdGenerator.getMax() / 2) {
			AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.HUB_TRANSFER,
					MessageConstant.MESSAGE_HUB_COLLECT_NUMBERING_OVER_INTERMEDIATE, new String[] {},
					String.format("current=%d, max=%d", dataId, DataIdGenerator.getMax()));
		}

		// 収集IDとデータIDを主キーとして設定.
		CollectStringDataPK pk = new CollectStringDataPK(collectId, dataId);

		m_log.debug(methodName + DELIMITER + "return CollectStringDataPK. collectId=" + collectId.toString()
				+ ", dataId=" + dataId.toString());
		return pk;

	}

	/**
	 * 監視結果の通知出力.<br>
	 * 
	 * @param facilityId
	 *            監視実施したFacilityID
	 * @param result
	 *            監視結果
	 * @return 通知情報リスト
	 * @throws HinemosUnknown
	 */
	public List<OutputBasicInfo> runAlert(String facilityId, BinaryResultDTO result) throws HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + String.format("start. facilityId=%s, monitorId=%s, monitorValid=%b",
				facilityId, result.monitorInfo.getMonitorId(), result.monitorInfo.getMonitorFlg()));

		List<OutputBasicInfo> rtn = new ArrayList<>();

		// 監視無効の場合は通知しない(監視ジョブは除く).
		if ((result.monitorInfo.getMonitorFlg() == null || !result.monitorInfo.getMonitorFlg())
				&& result.runInstructionInfo == null) {
			m_log.debug(methodName + DELIMITER + String.format("skip alert because monitorFlg is off. monitorID=%s",
					result.monitorInfo.getMonitorId()));
			return rtn;
		}

		// 監視ファイル分類に応じて通知情報を設定する.
		BinaryCheckInfo binCheckInfo = result.monitorInfo.getBinaryCheckInfo();

		if (BinaryConstant.COLLECT_TYPE_WHOLE_FILE.equals(binCheckInfo.getCollectType())) {
			m_log.debug(methodName + DELIMITER + String
					.format("prepared to add messages for whole file. collectType=%s", binCheckInfo.getCollectType()));
			// 任意バイナリファイル監視の場合はファイル単位で通知情報を設定.
			m_log.debug(methodName + DELIMITER
					+ String.format("whole file result records count=%d", result.binaryRecords.size()));
			for (BinaryRecordDTO record : result.binaryRecords) {
				String matchPattern = "";
				if (m_log.isDebugEnabled()) {
					if (record.getMatchBinaryProvision() == null) {
						matchPattern = "null";
					} else {
						matchPattern = record.getMatchBinaryProvision().getGrepString();
					}
				}

				if (BinaryConstant.FILE_POSISION_END.equals(record.getFilePosition())
						&& record.getMatchBinaryProvision() != null) {
					// ファイル末尾レコードのみ通知情報を設定(レコード分割した全データを受信してから通知する).
					rtn.add(this.createOutputBasicInfo(facilityId, result, record));
					m_log.debug(methodName + DELIMITER
							+ String.format("added message list. filePosition=%s, matchPattern=%s",
									record.getFilePosition(), matchPattern));
				} else {
					// 調査用ログ.
					m_log.debug(methodName + DELIMITER
							+ String.format("skip to add message list. filePosition=%s, matchPattern=%s",
									record.getFilePosition(), matchPattern));
				}
			}
		} else {
			// ログ・連続データ監視の場合はレコード単位で通知情報を設定.
			m_log.debug(methodName + DELIMITER + String.format(
					"prepared to add messages for only incremental. collectType=%s", binCheckInfo.getCollectType()));
			for (BinaryRecordDTO record : result.binaryRecords) {
				if (record.getMatchBinaryProvision() != null) {
					rtn.add(this.createOutputBasicInfo(facilityId, result, record));
				}
			}
		}

		// 通知出力処理.
		m_log.debug(methodName + DELIMITER + String.format("message list count=%d", rtn.size()));
		if (result.runInstructionInfo == null) {
			// 監視ジョブ以外の場合.
			return rtn;
		} else {
			// 監視ジョブの場合
			if (!rtn.isEmpty()) {
				for (OutputBasicInfo output : rtn) {
					this.monitorJobEndNodeList.add(new MonitorJobEndNode(output.getRunInstructionInfo(),
							result.monitorInfo.getMonitorTypeId(),
							makeJobOrgMessage(result.monitorInfo, output.getMessageOrg()), "", RunStatusConstant.END,
							MonitorJobWorker.getReturnValue(output.getRunInstructionInfo(), output.getPriority())));
					m_log.debug(methodName + DELIMITER
							+ String.format("set job end. original message=[%s]", output.getMessageOrg()));
				}
			}
			return new ArrayList<OutputBasicInfo>();
		}
	}

	/**
	 * 監視結果を通知基本情報にマッピング.
	 * 
	 * @param facilityId
	 *            FacilityId
	 * @param result
	 *            監視結果情報.
	 * @param record
	 *            マッピング対象の監視結果レコード.
	 * 
	 * @return 通知基本情報.
	 */
	private OutputBasicInfo createOutputBasicInfo(String facilityId, BinaryResultDTO result, BinaryRecordDTO record)
			throws HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		OutputBasicInfo output = new OutputBasicInfo();

		// 通知グループID
		output.setNotifyGroupId(NotifyGroupIdGenerator.generate(result.monitorInfo));
		// 通知出力メッセージの設定.
		String filePath = MessageConstant.LOGFILE_FILENAME.getMessage() + "="
				+ result.monitorInfo.getBinaryCheckInfo().getBinaryfile() + "\n";
		String searchWord = record.getMatchBinaryProvision().getGrepString();
		String searchString = MessageConstant.SEARCH_STRING.getMessage() + "=" + searchWord + "\n";
		String searchBinary = "";
		String encoding = "";
		if (!BinaryBeanUtil.isSearchHex(searchWord)) {
			String searchBinaryData = "";
			String encodingCondition = record.getMatchBinaryProvision().getEncoding();
			try {
				byte[] byteArray;
				byteArray = searchWord.getBytes(encodingCondition);
				searchBinaryData = BinaryUtil.listToString(BinaryUtil.arrayToList(byteArray));
				searchBinaryData = BinaryConstant.HEX_PREFIX + searchBinaryData;
			} catch (UnsupportedEncodingException e) {
				searchBinaryData = "failed to decoding.";
				m_log.warn(methodName + DELIMITER + String.format("failed to decode string. string=%s, charset=%s",
						searchWord, encodingCondition));
			}
			searchBinary = MessageConstant.SEARCH_BINARY.getMessage() + "=" + searchBinaryData + "\n";
			encoding = MessageConstant.JOB_SCRIPT_ENCODING.getMessage() + "=" + encodingCondition + "\n";
		}
		String hexString = MessageConstant.HEX_EXPRESSION.getMessage() + "=" + record.getOxStr();
		output.setMessageOrg(filePath + searchString + searchBinary + encoding + hexString);

		// 監視ID・ファシリティID・監視種別・監視詳細の設定.
		output.setMonitorId(record.getMatchBinaryProvision().getMonitorId());
		output.setFacilityId(facilityId);
		output.setPluginId(result.monitorInfo.getMonitorTypeId());
		output.setSubKey(record.getMatchBinaryProvision().getGrepString());

		// スコープの設定.
		if (FacilityTreeAttributeConstant.UNREGISTERED_SCOPE.equals(facilityId)) {
			output.setScopeText(result.msgInfo.getHostName());
		} else {
			String facilityPath = new RepositoryControllerBean().getFacilityPath(facilityId, null);
			output.setScopeText(facilityPath);
		}
		output.setApplication(result.monitorInfo.getApplication());

		// メッセージの設定.
		if (record.getMatchBinaryProvision().getMessage() != null) {
			String str = record.getMatchBinaryProvision().getMessage().replace(BinaryConstant.BINARY_LINE,
					record.getOxStr());
			int maxLen = HinemosPropertyCommon.monitor_log_line_max_length.getIntegerValue();
			if (str.length() > maxLen) {
				str = str.substring(0, maxLen);
			}
			output.setMessage(str);
		}

		// 重要度・生成日時・ジョブ設定情報の設定.
		output.setPriority(record.getMatchBinaryProvision().getPriority());
		output.setGenerationDate(result.msgInfo.getGenerationDate());
		output.setRunInstructionInfo(result.runInstructionInfo);

		// 多重化IDの設定.
		output.setMultiId(HinemosPropertyCommon.monitor_systemlog_receiverid.getStringValue());

		return output;
	}

	/**
	 * ジョブ用オリジナルメッセージ.
	 */
	private String makeJobOrgMessage(MonitorInfo monitorInfo, String orgMsg) {
		if (monitorInfo == null || monitorInfo.getBinaryCheckInfo() == null) {
			return "";
		}
		String[] args = { monitorInfo.getBinaryCheckInfo().getDirectory(),
				monitorInfo.getBinaryCheckInfo().getFileName(), monitorInfo.getBinaryCheckInfo().getCollectType() };
		return MessageConstant.MESSAGE_JOB_MONITOR_ORGMSG_BINARY.getMessage(args) + "\n" + orgMsg;
	}

	// 以下getter.
	/**
	 * 監視ジョブの実行結果を返す
	 * 
	 * @return 監視ジョブの実行結果
	 */
	public Collection<? extends MonitorJobEndNode> getMonitorJobEndNodeList() {
		return this.monitorJobEndNodeList;
	}

}
