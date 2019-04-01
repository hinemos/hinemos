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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.clustercontrol.binary.bean.BinaryConstant;
import com.clustercontrol.binary.bean.BinaryQueryInfo;
import com.clustercontrol.binary.bean.BinarySearchBean;
import com.clustercontrol.binary.model.CollectBinaryData;
import com.clustercontrol.binary.model.CollectBinaryDataTag;
import com.clustercontrol.binary.util.BinaryBeanUtil;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.hub.bean.StringData;
import com.clustercontrol.hub.bean.StringQueryInfo;
import com.clustercontrol.hub.bean.StringQueryResult;
import com.clustercontrol.hub.bean.Tag;
import com.clustercontrol.hub.model.CollectStringDataPK;
import com.clustercontrol.hub.model.CollectStringKeyInfo;
import com.clustercontrol.hub.session.HubControllerBean;
import com.clustercontrol.hub.session.HubControllerBean.Token;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;
import com.clustercontrol.platform.QueryExecutor;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.BinaryUtil;
import com.clustercontrol.util.MessageConstant;

/**
 * バイナリ収集結果の操作クラス.
 *
 * @version 6.1.0
 * @since 6.1.0
 */
public class BinaryHubController {

	// ログ出力関連
	/** ロガー */
	private static Logger logger = Logger.getLogger(BinaryHubController.class);
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	// ファイル増分監視の検索処理.
	/** キーワード初回判定フラグ */
	private boolean isFirstCheck = true;

	/**
	 * バイナリ収集情報を検索.<br>
	 * <br>
	 * ※初期構築時、HubControllerBeanのqueryCollectStringData()を参照に実装
	 * 
	 * @return バイナリ収集情報(データ本体はバイナリデータを16進数文字列に変換して送信)
	 * @see com.clustercontrol.hub.session.HubControllerBean
	 */
	public StringQueryResult queryCollectBinaryData(BinaryQueryInfo queryInfo)
			throws InvalidSetting, HinemosDbTimeout, HinemosUnknown, InvalidRole {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		// 性能測定用に開始時間を出力.
		long start = System.currentTimeMillis();
		logger.debug(String.format(methodName + DELIMITER + "start query. query=%s", queryInfo));

		// 検索タイムアウト値を取得.
		int searchTimeout = HinemosPropertyCommon.hub_binary_search_timeout.getIntegerValue();
		logger.info(methodName + DELIMITER + "query timeout=" + searchTimeout);

		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// ファシリティID・監視IDに紐づく収集IDをマップに格納.
			Map<Long, CollectStringKeyInfo> keys = this.getCollectStringKeyInfo(queryInfo, searchTimeout);

			// アクセス可能な収集IDがない場合は終了.
			if (keys.isEmpty()) {
				StringQueryResult dummy = new StringQueryResult();
				dummy.setOffset(queryInfo.getOffset());
				dummy.setSize(0);
				dummy.setCount((queryInfo.isNeedCount() != null && queryInfo.isNeedCount()) ? 0 : null);
				dummy.setTime(System.currentTimeMillis() - start);
				logger.debug(
						String.format(methodName + DELIMITER + "end query because keys are empty. result=%s, query=%s",
								dummy.toResultString(), queryInfo));
				return dummy;
			}

			// 収集バイナリデータ取得クエリを生成.
			StringBuilder dataQueryStr = this.createDataQueryStr(queryInfo);
			StringQueryResult result = new StringQueryResult();
			result.setOffset(queryInfo.getOffset());

			// データの取得件数を確認するためのクエリ生成.
			String countQueryStr = String.format(
					"SELECT COUNT(DISTINCT d) " + dataQueryStr.toString() + " AND d.filePosition='%s'",
					BinaryConstant.FILE_POSISION_END);
			logger.debug(String.format(methodName + DELIMITER + "query count. queryStr=%s, query=%s", countQueryStr,
					queryInfo));

			// DBから件数取得.
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("collectIds", keys.keySet());
			Long count = QueryExecutor.getDataByJpqlWithTimeout(countQueryStr, Long.class, parameters, searchTimeout);

			// 0件チェック(取得件数取得時).
			if (count == null || count == 0) {
				result.setSize(0);
				result.setCount(0);
				result.setTime(System.currentTimeMillis() - start);
				logger.debug(String.format(
						methodName + DELIMITER + "end query because the count is 0 on counting. result=%s, query=%s",
						result.toResultString(), queryInfo));
				return result;
			}
			logger.debug(
					String.format(methodName + DELIMITER + "count by the query. count=%d, query=%s", count, queryInfo));

			// 取得件数が上限超える場合はエラーメッセージ表示.
			if (count > HinemosPropertyCommon.hub_binary_search_max_count.getNumericValue()) {
				throw new InvalidSetting(MessageConstant.MESSAGE_HUB_BINARY_SEARCH_MAX_COUNT.getMessage());
			}

			// 収集蓄積バイナリデータ取得用クエリ生成.
			String queryStr = String.format("SELECT DISTINCT d " + dataQueryStr.toString() + " AND d.filePosition='%s'"
					+ " ORDER BY d.time DESC", BinaryConstant.FILE_POSISION_END);
			logger.debug(
					String.format(methodName + DELIMITER + "query data. queryStr=%s, query=%s", queryStr, queryInfo));

			// SQLパラメータセット.
			parameters = new HashMap<String, Object>();
			parameters.put("collectIds", keys.keySet());

			// DBアクセスして取得.
			List<CollectBinaryData> dataResults = QueryExecutor.getListByJpqlWithTimeout(queryStr,
					CollectBinaryData.class, parameters, searchTimeout);

			// 0件チェック(データ取得時).
			if (dataResults == null || dataResults.isEmpty()) {
				result.setSize(0);
				if(queryInfo.isNeedCount() != null && queryInfo.isNeedCount()){
					result.setCount(0);
				} 
				result.setTime(System.currentTimeMillis() - start);
				logger.debug(String.format(
						methodName + DELIMITER + "end query because the count is 0 on searching. result=%s, query=%s",
						result.toResultString(), queryInfo));
				return result;
			}

			// 取得結果をキーワードで絞り込む.
			List<CollectBinaryData> refineResults = this.refineDataResults(searchTimeout, queryInfo, dataResults);

			// 0件チェック(データ絞込み時).
			if (refineResults == null || refineResults.isEmpty()) {
				result.setSize(0);
				if(queryInfo.isNeedCount() != null && queryInfo.isNeedCount()){
					result.setCount(0);
				} 
				result.setTime(System.currentTimeMillis() - start);
				logger.debug(String.format(
						methodName + DELIMITER + "end query because the count is 0 on refining. result=%s, query=%s",
						result.toResultString(), queryInfo));
				return result;
			}

			// 検索結果の合計全件をセット.
			result.setCount(refineResults.size());
			logger.debug(
					String.format(methodName + DELIMITER + "set count of all searched result. result=%s, keywords=[%s]",
							refineResults.size(), queryInfo.getKeywords()));

			// 検索結果を表示分のみに絞り込む.
			refineResults = this.refineOnlyView(queryInfo.getOffset(), queryInfo.getSize(), refineResults);

			// 検索結果の表示レコードを先頭レコードに置き換える.
			refineResults = this.replaceTopRecord(searchTimeout, refineResults);

			// 絞り込み成功したので返却値をセット.
			result.setOffset(queryInfo.getOffset());
			result.setSize(refineResults.size());

			int messageMaxLen = HinemosPropertyCommon.monitor_log_line_max_length.getIntegerValue();

			// 不要なデータ破棄(OutOfMemoryError防止).
			dataResults = null;

			// 取得結果を返却用にセット.
			List<StringData> binaryDataList = new ArrayList<StringData>();
			for (CollectBinaryData record : refineResults) {
				CollectStringKeyInfo key = keys.get(record.getCollectId());
				StringData data = new StringData();
				data.setFacilityId(key.getFacilityId());
				data.setMonitorId(key.getMonitorId());
				data.setTime(record.getTime());
				List<Byte> recordBinary = BinaryUtil.arrayToList(record.getValue());
				if (recordBinary != null && recordBinary.size() > (messageMaxLen / 2)) {
					recordBinary = recordBinary.subList(0, messageMaxLen / 2);
				}
				String recordBinaryStr = BinaryUtil.listToString(recordBinary, 1);
				data.setData(recordBinaryStr);
				List<Tag> tagList = new ArrayList<Tag>();
				Tag tag = null;
				for (CollectBinaryDataTag recordTag : record.getTagList()) {
					tag = new Tag();
					tag.setKey(recordTag.getKey());
					tag.setValue(recordTag.getValue());
					tagList.add(tag);
				}
				data.setTagList(tagList);
				data.setPrimaryKey(record.getId());
				data.setRecordKey(record.getRecordKey());
				binaryDataList.add(data);
			}

			// 検索結果を 時刻降順 > FacilityID昇順 > 監視ID昇順 > レコードキー降順(最新順)に整列させる
			Collections.sort(binaryDataList, new Comparator<StringData>() {
				@Override
				public int compare(StringData info1, StringData info2) {

					// 時刻で比較(降順なので比較オブジェクト反転).
					int result = info2.getTime().compareTo(info1.getTime());
					if (result != 0) {
						return result;
					}

					// FacilityIDで比較
					result = info1.getFacilityId().compareTo(info2.getFacilityId());
					if (result != 0) {
						return result;
					}

					// 監視IDで比較
					result = info1.getMonitorId().compareTo(info2.getMonitorId());
					if (result != 0) {
						return result;
					}

					// レコードキーで比較(降順なので比較オブジェクト反転).
					return info2.getRecordKey().compareTo(info1.getRecordKey());
				}
			});

			result.setDataList(binaryDataList);

			// 処理終了.
			result.setTime(System.currentTimeMillis() - start);
			logger.debug(String.format(methodName + DELIMITER + "end query. result=%s, query=%s",
					result.toResultString(), queryInfo));
			return result;

		} catch (HinemosDbTimeout e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosDbTimeout(MessageConstant.MESSAGE_HUB_SEARCH_TIMEOUT.getMessage());
		} catch (InvalidSetting | HinemosUnknown | InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (RuntimeException e) {
			logger.warn(methodName + DELIMITER + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}

	}

	/**
	 * キー情報取得.
	 */
	private Map<Long, CollectStringKeyInfo> getCollectStringKeyInfo(BinaryQueryInfo queryInfo, Integer searchTimeout)
			throws InvalidRole, HinemosUnknown, InvalidSetting, HinemosDbTimeout {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		logger.debug(methodName + DELIMITER + "start.");

		// キー重複削除クエリを設定.
		StringBuilder keyQueryStr = new StringBuilder("SELECT DISTINCT k FROM CollectStringKeyInfo k");

		// ファシリティIDと監視IDを検索条件から取得して設定する.
		List<String> facilityIds = new ArrayList<>();
		List<String> monitorIds = new ArrayList<>();
		if (queryInfo.getFacilityId() != null || queryInfo.getMonitorId() != null) {
			StringBuilder whereStr = new StringBuilder();

			// ファシリティIDの設定.
			if (queryInfo.getFacilityId() != null) {
				if (!FacilityTreeAttributeConstant.UNREGISTERED_SCOPE.equals(queryInfo.getFacilityId())) {
					RepositoryControllerBean repositoryCtrl = new RepositoryControllerBean();
					try {
						if (repositoryCtrl.isNode(queryInfo.getFacilityId())) {
							// ファシリティIDがノードに該当する場合は追加.
							facilityIds.add(queryInfo.getFacilityId());
						} else {
							// 対象リポジトリのノードを全量取得して追加.
							List<NodeInfo> nodeinfoList = repositoryCtrl.getNodeList(queryInfo.getFacilityId(), 0);
							if (!nodeinfoList.isEmpty()) {
								for (NodeInfo node : nodeinfoList) {
									facilityIds.add(node.getFacilityId());
								}
							}
						}
					} catch (FacilityNotFound e) {
						logger.warn(methodName + DELIMITER + e.getMessage());
						throw new IllegalStateException(methodName + DELIMITER + "can't get NodeInfo. facilityId = "
								+ queryInfo.getFacilityId());
					}
				} else {
					facilityIds.add(FacilityTreeAttributeConstant.UNREGISTERED_SCOPE);
				}
				whereStr.append("k.id.facilityId IN :nodeIds");
			}

			// 監視IDの設定.
			if (queryInfo.getMonitorId() != null) {
				// 先にアクセスできるノードの範囲を絞っているので
				// クエリを実施しているユーザーが、指定した監視設定にアクセスできるかどうかは、考慮なし.
				if (whereStr.length() != 0) {
					whereStr.append(" AND ");
				}
				whereStr.append(String.format("k.id.monitorId = '%s'", queryInfo.getMonitorId()));
			} else {
				// 監視項目IDの指定がない場合は、ユーザがアクセス可能な全監視項目IDをクエリに設定.
				if (whereStr.length() != 0) {
					whereStr.append(" AND ");
				}
				whereStr.append("k.id.monitorId IN :monitorIds");

				monitorIds = new MonitorSettingControllerBean().getMonitorIdList(null);
			}

			if (whereStr.length() != 0) {
				keyQueryStr.append(" WHERE ").append(whereStr);
			}
		}

		// 収集ID取得クエリのパラメータ(ファシリティID)設定.
		Map<String, Object> parameters = new HashMap<String, Object>();
		if (!facilityIds.isEmpty()) {
			parameters.put("nodeIds", facilityIds);
			logger.debug(
					String.format(methodName + DELIMITER + "target nodes. nodes=%s, query=%s", facilityIds, queryInfo));
		} else {
			// 検索対象となるノードがないためエラー.
			logger.warn(methodName + DELIMITER + MessageConstant.MESSAGE_HUB_SEARCH_NO_NODE.getMessage());
			throw new InvalidSetting(MessageConstant.MESSAGE_HUB_SEARCH_NO_NODE.getMessage());
		}

		// 収集ID取得クエリのパラメータ(監視ID)設定.
		if (!monitorIds.isEmpty()) {
			parameters.put("monitorIds", monitorIds);
			logger.debug(String.format(methodName + DELIMITER + "target monitorIds. monitorIds=%s, query=%s",
					monitorIds, queryInfo));
		}

		// DBアクセスして取得.
		List<CollectStringKeyInfo> ketResults = QueryExecutor.getListByJpqlWithTimeout(keyQueryStr.toString(),
				CollectStringKeyInfo.class, parameters, searchTimeout);
		logger.debug(String.format(methodName + DELIMITER + "get ketResults by the query=%s", keyQueryStr.toString()));

		// <収集ID,主キーオブジェクト>のマップとして格納.
		Map<Long, CollectStringKeyInfo> keys = new HashMap<>();
		for (CollectStringKeyInfo r : ketResults) {
			keys.put(r.getCollectId(), r);
		}
		logger.debug(
				String.format(methodName + DELIMITER + "target data key. keys=%s, query=%s", keys.values(), queryInfo));

		return keys;
	}

	/**
	 * 収集バイナリデータ取得クエリ文字列生成.
	 */
	private StringBuilder createDataQueryStr(BinaryQueryInfo queryInfo) throws InvalidSetting {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		logger.debug(methodName + DELIMITER + "start.");

		// バイナリ収集DB登録データ取得クエリを設定.
		StringBuilder dataQueryStr = null;
		if (HinemosPropertyCommon.hub_search_switch_join.getBooleanValue()) {
			// タグテーブルとジョインして取得.
			dataQueryStr = new StringBuilder("FROM CollectBinaryData d JOIN d.tagList t ");
		} else {
			dataQueryStr = new StringBuilder("FROM CollectBinaryData d ");
		}

		String whereId = "WHERE d.id.collectId IN :collectIds";

		// 検索条件が何も設定されていない場合は終了.
		if (queryInfo.getFrom() == null && queryInfo.getTo() == null
				&& (queryInfo.getKeywords() == null || queryInfo.getKeywords().isEmpty())) {
			logger.debug(methodName + DELIMITER + "the parameters in queryInfo (from,to and keywords) are empty.");
			dataQueryStr.append(whereId);
			return dataQueryStr;
		}

		// 条件となる時刻のFromTo大小チェック.
		if (queryInfo.getFrom() != null && queryInfo.getTo() != null && queryInfo.getFrom() > queryInfo.getTo()) {
			throw new InvalidSetting(MessageConstant.MESSAGE_HUB_SEARCH_DATE_INVALID.getMessage());
		}

		// From時刻をクエリに設定.
		StringBuilder whereStr = new StringBuilder();
		if (queryInfo.getFrom() != null) {
			whereStr.append(" AND ");
			whereStr.append(String.format("d.time >= '%d'", queryInfo.getFrom()));
		}

		// To時刻をクエリに設定.
		if (queryInfo.getTo() != null) {
			whereStr.append(" AND ");
			whereStr.append(String.format("d.time < '%d'", queryInfo.getTo()));
		}

		// 検索ワードを元にタグ検索のクエリ追加(通常の文字列検索はバイナリデータでは曖昧検索不可のため取得後実施).
		if (queryInfo.getKeywords() != null && !queryInfo.getKeywords().isEmpty()) {

			// 接続条件文字列セット.
			boolean and = true;
			if (StringQueryInfo.Operator.AND == queryInfo.getOperator()) {
				and = true;
			} else {
				and = false;
			}

			// and条件の場合のみクエリ追加(or条件の場合は文字列検索の絞込みと同じタイミングで絞込み実施する).
			if (and) {
				// 検索ワードを"タグキー"・"検索ワード"・"否定モード"のセットに分解する.
				String keywords = queryInfo.getKeywords();
				List<Token> tokens = HubControllerBean.parseKeywords(keywords);
				if (tokens == null || tokens.isEmpty()) {
					// キーワード入力あるのに解析結果が存在しない場合はクライアントにエラーメッセージ出力.
					logger.warn(methodName + DELIMITER
							+ String.format("failed to separates words. keywords=[%s]", keywords));
					throw new InvalidSetting(MessageConstant.MESSAGE_HUB_SEARCH_KEYWORD_INVALID.getMessage());
				}

				List<Token> tagTokens = new ArrayList<Token>();
				this.setTokenList(tokens, queryInfo, tagTokens, null, null);

				// タグ指定の場合のみクエリとして検索条件追加.
				if (!tagTokens.isEmpty() && and) {
					// 先頭文字列.
					StringBuffer conditionValueBuffer = new StringBuffer();
					conditionValueBuffer.append("(");

					// 分解した検索ワードをクエリとして組み立てる.
					boolean isTop = true;
					for (Token token : tagTokens) {
						// 接続条件を追加.
						if (!isTop) {
							conditionValueBuffer.append(" AND ");
						}
						if (token.negate) {
							conditionValueBuffer.append(String.format(
									"EXISTS(SELECT t FROM IN(d.tagList) t WHERE (t.key LIKE '%s' AND t.value NOT LIKE '%s'))",
									token.key, token.word));
						} else {
							conditionValueBuffer.append(String.format(
									"EXISTS(SELECT t FROM IN(d.tagList) t WHERE (t.key LIKE '%s' AND t.value LIKE '%s'))",
									token.key, token.word));
						}
						isTop = false;
					}
					whereStr.append(" AND " + conditionValueBuffer.toString()).append(")");
				}

			}

		}

		dataQueryStr.append(whereId);

		if (whereStr.length() != 0) {
			dataQueryStr.append(whereStr);
		}

		logger.debug(String.format(methodName + DELIMITER + "create dataQueryStr. dataQueryStr=%s", dataQueryStr));
		return dataQueryStr;
	}

	/**
	 * データ絞込み処理.<br>
	 * <br>
	 * DB取得データを入力されたキーワードを元に絞り込む.
	 * 
	 * @param queryInfo
	 *            検索条件.
	 * @param dataResults
	 *            DB取得データ.
	 * @return キーワード絞込みされた検索結果、キーワード入力なしの場合は全件返却.
	 * 
	 * @throws HinemosDbTimeout
	 * @throws InvalidSetting
	 */
	private List<CollectBinaryData> refineDataResults(Integer searchTimeout, BinaryQueryInfo queryInfo,
			List<CollectBinaryData> dataResults) throws HinemosDbTimeout, InvalidSetting {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		logger.debug(methodName + DELIMITER + String.format("start to refine data. all size=%d", dataResults.size()));

		// キーワード入力チェック.
		String keywords = queryInfo.getKeywords();
		List<Token> allTokenList = null;
		if (keywords == null || keywords.isEmpty()) {
			logger.info(methodName + DELIMITER + "get the empty keywords.");
			return dataResults;
		}
		// 検索ワードを"タグキー"・"検索ワード"・"否定モード"のセットに分解する.
		allTokenList = HubControllerBean.parseKeywords(keywords);
		// 分解した検索ワードをタグ検索と文字列と16進数で分類.
		List<Token> tagTokenList = new ArrayList<Token>();
		List<Token> hexTokenList = new ArrayList<Token>();
		List<Token> wordTokenList = new ArrayList<Token>();
		this.setTokenList(allTokenList, queryInfo, tagTokenList, wordTokenList, hexTokenList);

		// 結合条件の取得.
		boolean and = (StringQueryInfo.Operator.AND == queryInfo.getOperator());
		if (and && hexTokenList.isEmpty() && wordTokenList.isEmpty()) {
			// and条件でtag検索のみの場合は、DBから取得時に絞込みされているので返却.
			logger.debug(methodName + DELIMITER
					+ "skip to refine because the keywords are only for searching tags and a coupling condition is \"and\".");
			return dataResults;
		}

		// 条件ワード分のマッチ結果を取得するための検索ワードをリスト設定(ファイル全体監視向け).
		List<String> tokenStringList = new ArrayList<String>();
		if (wordTokenList != null && !wordTokenList.isEmpty()) {
			for (Token wordToken : wordTokenList) {
				tokenStringList.add(wordToken.toString());
			}
		}
		if (hexTokenList != null && !hexTokenList.isEmpty()) {
			for (Token hexToken : hexTokenList) {
				tokenStringList.add(hexToken.toString());
			}
		}
		if (!and) {
			// or条件の場合はタグ検索も判定用に追加.
			if (tagTokenList != null && !tagTokenList.isEmpty()) {
				for (Token tagToken : tagTokenList) {
					tokenStringList.add(tagToken.toString());
				}
			}
		} else {
			// and条件の場合は使わないのでクリアしておく.
			tagTokenList.clear();
		}

		// ループ用変数初期化.
		List<Byte> recordBinary = null;
		Map<String, CollectBinaryData> refineResultsMap = new TreeMap<String, CollectBinaryData>();
		String key = null;

		// --ループ処理ここから--
		// 取得レコード毎に検索実施してマッチする場合は返却対象として追加.
		for (CollectBinaryData data : dataResults) {

			// 収集方式取得.
			String collectType = data.getCollectType();
			// ファイルキーを取得できない場合はキー生成できないので飛ばす(不正データ).
			if (data.getFileKey() == null) {
				logger.warn(methodName + DELIMITER + "failed to get key of a file. FileKey=" + data.getFileKey());
				continue;
			}
			// 絞込み結果マップ用のキー(ファイル全体：監視時刻_収集ID_ファイルパス / 増分データ：監視時刻_収集ID_レコードキー).
			key = data.getFileKey();

			// 初回チェック検知フラグ初期化.
			boolean isMatch = false;

			// 収集方式に応じて絞込み条件マッチするかチェック.
			if (BinaryConstant.COLLECT_TYPE_WHOLE_FILE.equals(collectType)) {
				logger.debug(methodName + DELIMITER
						+ String.format("prepared to check match. collectType=%s, key=%s", collectType, key));

				// ファイル全体監視の場合はファイル単位でマッチするかチェックするため、とりあえず紐づくレコードの主キーを全件取得.
				String queryStr = String.format("SELECT DISTINCT d.id " + "FROM CollectBinaryData d "
						+ "WHERE d.fileKey='%s' " + "ORDER BY d.recordKey DESC", key);
				logger.debug(methodName + DELIMITER
						+ String.format("query data to get primary keys of a file. queryStr=%s.", queryStr));

				// DBアクセスして取得.
				Map<String, Object> parameters = new HashMap<String, Object>();
				List<CollectStringDataPK> pkList = QueryExecutor.getListByJpqlWithTimeout(queryStr,
						CollectStringDataPK.class, parameters, searchTimeout);
				logger.debug(
						methodName + DELIMITER + String.format("get primary keys of a file. count=%d", pkList.size()));
				Map<String, Boolean> matchMap = new HashMap<String, Boolean>();

				// 主キーを元に実データ取得してキーワードにマッチするかチェック.
				CollectBinaryData topData = null;
				Iterator<CollectStringDataPK> pkItr = pkList.iterator();
				while (pkItr.hasNext()) {
					CollectStringDataPK pk = pkItr.next();

					queryStr = String.format(
							"SELECT DISTINCT d " + "FROM CollectBinaryData d " + "WHERE d.id.collectId='%d'"
									+ " AND d.id.dataId='%d' " + "ORDER BY d.recordKey DESC",
							pk.getCollectId(), pk.getDataId());
					logger.debug(methodName + DELIMITER + String.format("query data. queryStr=%s.", queryStr));

					// DBアクセスして実データ取得.
					List<CollectBinaryData> fileData = QueryExecutor.getListByJpqlWithTimeout(queryStr,
							CollectBinaryData.class, parameters, searchTimeout);
					logger.debug(
							methodName + DELIMITER + String.format("get the file data. count=%d", fileData.size()));
					Iterator<CollectBinaryData> fileDataItr = fileData.iterator();

					// 主キーで取得してるので1件のみ取得想定だが念のためループ.
					while (fileDataItr.hasNext()) {
						CollectBinaryData fileRecord = fileDataItr.next();
						// ファイル単位でマッチするかmatchMapを利用してチェック.(チェック結果はマッチマップに格納して共有).
						this.checkMatchByFile(fileRecord, searchTimeout, queryInfo.getTextEncoding(), tagTokenList,
								wordTokenList, hexTokenList, and, isMatch, key, matchMap, refineResultsMap);
						if (!pkItr.hasNext() && !fileDataItr.hasNext()) {
							// 先頭レコードをセット(レコードキー降順なので先頭レコードが最後尾).
							topData = new CollectBinaryData();
							topData.setId(fileRecord.getId());
							topData.setFileKey(fileRecord.getFileKey());
							topData.setFilePosition(fileRecord.getFilePosition());
							topData.setRecordKey(fileRecord.getRecordKey());
							topData.setTagList(fileRecord.getTagList());
							topData.setTime(fileRecord.getTime());
							topData.setValue(fileRecord.getValue());
						}
						// 不要なデータ破棄(OutOfMemoryError防止).
						fileDataItr.remove();
					}
					pkItr.remove();
				}
				// マップを元に任意バイナリファイルのマッチ結果を判定.
				isMatch = and;
				for (String keyWord : tokenStringList) {
					Boolean match = matchMap.get(keyWord);
					if (and) {
						// and条件の場合は1ワードでも合致していない場合は除外.
						if (match == null || !match) {
							isMatch = false;
							break;
						}
					} else {
						// or条件の場合は1ワードでも合致してれば絞込み結果として追加.
						if (match != null && match) {
							isMatch = true;
							break;
						}
					}
				}
				if (isMatch) {
					// 任意バイナリファイルの場合は先頭1レコードのみを返却する.
					if (topData != null) {
						refineResultsMap.put(key, topData);
						logger.debug(methodName + DELIMITER + String
								.format("add refineResultsMap. key=%s, recordKey=%s", key, data.getRecordKey()));
					} else {
						logger.debug(methodName + DELIMITER + String
								.format("not add refineResultsMap. key=%s, recordKey=%s", key, data.getRecordKey()));
					}
				}
				// 不要なデータ破棄(OutOfMemoryError防止).
				matchMap = null;

			} else {

				// 増分監視の場合はレコード単位で条件マッチするかチェックして絞込み.
				this.isFirstCheck = true;

				// タグ検索.
				isMatch = this.searchOrTag(data.getDataId(), searchTimeout, tagTokenList, isMatch, null);

				// 取得レコードのバイナリデータチェック(時間区切りは空データもある).
				recordBinary = BinaryUtil.arrayToList(data.getValue());
				if (recordBinary == null || recordBinary.isEmpty()) {
					// 空の場合はタグ以外のキーワード検索スキップ.
					logger.debug(methodName + DELIMITER + "get the empty binary data. dataID=" + data.getDataId());
					if ((!wordTokenList.isEmpty() || !hexTokenList.isEmpty()) && and) {
						// and条件の場合は文字列・16進数検索には引っかからないのでfalse.
						isMatch = false;
						logger.debug(methodName + DELIMITER
								+ String.format(
										"empty data is't match. wordTokenList size=%d, hexTokenList size=%d, and=%b",
										wordTokenList.size(), hexTokenList.size(), and));
					}
				} else {
					// 実データに対して文字列・16進数検索.
					isMatch = this.searchKeyword(recordBinary, queryInfo.getTextEncoding(), wordTokenList, and, isMatch,
							null);
					isMatch = this.searchHex(recordBinary, hexTokenList, and, isMatch, null);
				}

				// 条件に合致する場合は絞りこみ結果として追加.
				if (isMatch) {
					// 任意バイナリファイル以外は全レコード返却するので単純に追加.
					refineResultsMap.put(key, data);
					logger.debug(methodName + DELIMITER
							+ String.format("add refineResultsMap. key=%s, recordKey=%s", key, data.getRecordKey()));
				} else {
					logger.debug(methodName + DELIMITER + String
							.format("not add refineResultsMap. key=%s, recordKey=%s", key, data.getRecordKey()));
				}

			}

		}
		// --ループ処理ここまで--

		// マップバリューをリストとして追加、キー=時刻降順(最新順)になるよう整形.
		List<CollectBinaryData> refineResults = new ArrayList<CollectBinaryData>();
		refineResults.addAll(refineResultsMap.values());
		Collections.reverse(refineResults);
		logger.debug(
				String.format(methodName + DELIMITER + "refine binary data results. size=%d", refineResults.size()));
		return refineResults;
	}

	/**
	 * キーワード解析結果を文字列と16進数のセットに分解.<br>
	 * 
	 * @param parentTokenList
	 *            元となるキーワード解析結果.
	 * @param queryInfo
	 *            検索条件(文字列検索のエンコーディング指定チェック用).
	 * @param tagTokenList
	 *            分解後のタグセット格納先リスト、nullの場合はセットskip.
	 * @param wordTokenList
	 *            分解後の文字列セット格納先リスト、nullの場合はセットskip.
	 * @param hexTokenList
	 *            分解後の16進数セット格納先リスト、nullの場合はセットskip.
	 * 
	 * @throws InvalidSetting
	 */
	private void setTokenList(List<Token> parentTokenList, BinaryQueryInfo queryInfo, List<Token> tagTokenList,
			List<Token> wordTokenList, List<Token> hexTokenList) throws InvalidSetting {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		if (parentTokenList == null || parentTokenList.isEmpty()) {
			logger.debug(methodName + DELIMITER + "parentTokenList is empty.");
			return;
		}

		// 解析されたキーワードの先頭が"0x"の場合は16進数文字列とみなす.
		for (Token token : parentTokenList) {
			// タグ検索(or検索用に取得).
			if (token.key != null) {
				if (tagTokenList == null) {
					continue;
				}
				tagTokenList.add(token);
				continue;
			}
			// 検索タイプ取得.
			BinarySearchBean searchBean = BinaryBeanUtil.getSearchBean(token.word);

			switch (searchBean.getSearchType()) {

			case EMPTY:
				break;

			case STRING:
				if (wordTokenList == null) {
					break;
				}
				wordTokenList.add(token);
				break;

			case HEX:
				if (hexTokenList == null) {
					break;
				}
				// 16進数文字列を追加.
				Token hexToken = new Token(token.key, searchBean.getOnlyHexString(), token.negate);
				hexTokenList.add(hexToken);
				break;

			case ERROR:
				logger.info(methodName + DELIMITER
						+ "failed to get search hex string because input is empty or not hex strings ([a-f] or [A-F] or [0-9]).");
				throw new InvalidSetting(MessageConstant.MESSAGE_HUB_BINARY_SEARCH_HEX_INVALID.getMessage());

			default:
				// 想定外.
				logger.warn(methodName + DELIMITER
						+ String.format("invalid type to search binary. inputWord=%s, getType=%s", token.word,
								searchBean.getSearchType().toString()));
				break;
			}
		}

		if (wordTokenList == null || wordTokenList.isEmpty()) {
			// 文字列検索が入力されてるのにエンコーディング指定なしのためエラー.
			if (queryInfo.getTextEncoding() == null || queryInfo.getTextEncoding().isEmpty()) {
				logger.info(methodName + DELIMITER + "failed to decode Strings to binary because [encoding] is empty.");
				throw new InvalidSetting(MessageConstant.MESSAGE_HUB_BINARY_SEARCH_NO_ENCODING.getMessage());
			}
		}

		// 取得したリストの各サイズをログ出力.
		if (logger.isDebugEnabled()) {
			int parentTokenListSize = 0;
			int tagTokenListSize = 0;
			int wordTokenListSize = 0;
			int hexTokenListSize = 0;
			parentTokenListSize = parentTokenList.size();
			if (tagTokenList != null) {
				tagTokenListSize = tagTokenList.size();
			}
			if (wordTokenList != null) {
				wordTokenListSize = wordTokenList.size();
			}
			if (hexTokenList != null) {
				hexTokenListSize = hexTokenList.size();
			}
			logger.debug(methodName + DELIMITER
					+ String.format(
							"end. parentTokenList size=%d, tagTokenList size=%d, wordTokenList size=%d, hexTokenList size=%d",
							parentTokenListSize, tagTokenListSize, wordTokenListSize, hexTokenListSize));
		}
	}

	/**
	 * ファイル単位で検索条件にマッチするかチェック.
	 * 
	 * @param data
	 *            チェック対象のレコード.
	 * @param encoding
	 *            文字列チェック時のエンコーディング.
	 * @param tagTokenList
	 *            タグ検索条件.
	 * @param wordTokenList
	 *            文字列検索条件.
	 * @param hexTokenList
	 *            16進数検索条件
	 * @param and
	 *            結合条件(true:and,false:or)
	 * @param isMatch
	 * @param key
	 *            最終的にマッチした表示レコードを格納するマップのキー(表示順考慮).
	 * @param matchMap
	 *            検索ワード毎に前レコードまでの検索結果を格納しているマップ.
	 * @param refineResultsMap
	 *            最終的な絞込みデータマップ.
	 * 
	 */
	private void checkMatchByFile(CollectBinaryData data, Integer searchTimeout, String encoding,
			List<Token> tagTokenList, List<Token> wordTokenList, List<Token> hexTokenList, boolean and, boolean isMatch,
			String key, Map<String, Boolean> matchMap, Map<String, CollectBinaryData> refineResultsMap)
			throws HinemosDbTimeout, InvalidSetting {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		logger.debug(methodName + DELIMITER
				+ String.format("start to check match. collectID=%d, dataID=%d, key=%s, recordKey=%s",
						data.getCollectId(), data.getDataId(), data.getFileKey(), data.getRecordKey()));

		// バイナリ取得.
		List<Byte> recordBinary = BinaryUtil.arrayToList(data.getValue());
		if (recordBinary == null || recordBinary.isEmpty()) {
			this.searchOrTag(data.getDataId(), searchTimeout, tagTokenList, isMatch, matchMap);
			this.searchEmptyRecord(wordTokenList, hexTokenList, matchMap);
		} else {
			// 実データ検索.
			this.searchOrTag(data.getDataId(), searchTimeout, tagTokenList, isMatch, matchMap);
			this.searchKeyword(recordBinary, encoding, wordTokenList, and, isMatch, matchMap);
			this.searchHex(recordBinary, hexTokenList, and, isMatch, matchMap);
		}

	}

	/**
	 * ファイル全体監視の空レコードの検索結果セット.
	 * 
	 * @param wordTokenList
	 *            文字列検索条件.
	 * @param hexTokenList
	 *            16進数検索条件
	 * @param matchMap
	 *            検索ワード毎に前レコードまでの検索結果を格納しているマップ.
	 * 
	 */
	private void searchEmptyRecord(List<Token> wordTokenList, List<Token> hexTokenList, Map<String, Boolean> matchMap) {
		// 空データの場合の検索結果セット.
		boolean tmpMatch = false;

		if (wordTokenList != null && !wordTokenList.isEmpty()) {
			for (Token token : wordTokenList) {
				tmpMatch = false;
				this.setMatchMap(token, tmpMatch, matchMap);
			}
		}

		if (hexTokenList != null && !hexTokenList.isEmpty()) {
			for (Token token : wordTokenList) {
				tmpMatch = false;
				this.setMatchMap(token, tmpMatch, matchMap);
			}
		}
	}

	/**
	 * Or条件タグ検索.
	 * 
	 * @param dataId
	 *            検索対象レコードのデータID.
	 * @param longSearchTimeout
	 *            検索上限時間.
	 * @param tagTokenList
	 *            検索対象タグ.
	 * @param isMatch
	 *            表示レコードマッチ結果.
	 * @param matchMap
	 *            マッチ結果格納マップ.
	 */
	private boolean searchOrTag(Long dataId, Integer longSearchTimeout, List<Token> tagTokenList, boolean isMatch,
			Map<String, Boolean> matchMap) throws HinemosDbTimeout {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		boolean tmpMatch = false;

		// タグ検索.
		if (tagTokenList != null && !tagTokenList.isEmpty()) {
			Map<String, Object> parameters = new HashMap<String, Object>();
			StringBuilder querySb = null;
			Long count = null;
			for (Token tagToken : tagTokenList) {
				// 対象のタグでDB検索して取得できるか確認する.
				querySb = new StringBuilder();
				querySb.append(String.format("SELECT COUNT(DISTINCT d) " + "FROM CollectBinaryData d JOIN d.tagList t "
						+ "WHERE d.id.dataId='%d'", dataId));
				if (tagToken.negate) {
					querySb.append(String.format(
							"AND (EXISTS (SELECT t FROM IN(d.tagList) t WHERE (t.key LIKE '%s' AND t.value NOT LIKE '%s')))",
							tagToken.key, tagToken.word));
				} else {
					querySb.append(String.format(
							"AND (EXISTS (SELECT t FROM IN(d.tagList) t WHERE (t.key LIKE '%s' AND t.value LIKE '%s')))",
							tagToken.key, tagToken.word));
				}
				logger.debug(methodName + DELIMITER + String.format("query data. queryStr=%s.", querySb.toString()));

				// DBアクセスして件数取得.
				count = QueryExecutor.getDataByJpqlWithTimeout(querySb.toString(), Long.class, parameters,
						longSearchTimeout);
				if (count >= 1) {
					// 存在した場合.
					tmpMatch = true;
				} else {
					tmpMatch = false;
				}
				logger.debug(methodName + DELIMITER
						+ String.format(
								"match binary record by tag. tagKey=%s, tagValue=%s, matchResult=%b, count=%d, dataId=%d",
								tagToken.key, tagToken.word, tmpMatch, count, dataId));

				if (matchMap == null) {
					// 1レコードで完結する場合は他ワードのチェック結果と条件結合.
					if (this.isFirstCheck) {
						isMatch = tmpMatch;
					} else {
						// このロジック呼ぶのはor条件のみ.
						isMatch = isMatch || tmpMatch;
					}
					this.isFirstCheck = false;
				} else {
					// 他レコードも見た上で判定必要な場合は前の確認結果元にマップに格納.
					this.setMatchMap(tagToken, tmpMatch, matchMap);
				}
			}
		}
		return isMatch;
	}

	/**
	 * キーワード検索.
	 * 
	 * @param recordBinary
	 *            検索対象レコードバイナリ.
	 * @param encoding
	 *            エンコーディング方式.
	 * @param wordTokenList
	 *            検索対象ワード.
	 * @param and
	 *            結合条件(true:and,false:or).
	 * @param isMatch
	 *            表示レコードマッチ結果.
	 * @param matchMap
	 *            マッチ結果格納マップ.
	 * @throws InvalidSetting
	 */
	private boolean searchKeyword(List<Byte> recordBinary, String encoding, List<Token> wordTokenList, boolean and,
			boolean isMatch, Map<String, Boolean> matchMap) throws InvalidSetting {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		boolean tmpMatch = false;
		List<Byte> searchBinary = null;

		// キーワード検索.
		if (wordTokenList != null && !wordTokenList.isEmpty()) {
			for (Token wordToken : wordTokenList) {
				// 検索ワードをバイナリ変換.
				try {
					searchBinary = BinaryUtil.arrayToList(wordToken.word.getBytes(encoding));
				} catch (UnsupportedEncodingException e) {
					// ログ出力は共通部品側でしてるのでしない.
					throw new InvalidSetting(MessageConstant.MESSAGE_HUB_BINARY_SEARCH_ENCODING_INVALID.getMessage());
				}
				// 検索ワードがレコードバイナリに含まれるかどうか判定.
				if (wordToken.negate) {
					tmpMatch = !(BinaryUtil.byteListIndexOf(recordBinary,searchBinary) >= 0 );
				} else {
					tmpMatch = (BinaryUtil.byteListIndexOf(recordBinary,searchBinary) >= 0 );
				}
				logger.debug(String.format(
						methodName + DELIMITER
								+ "match binary record by text. text=%s, negate=%b, matchResult=%b, searchBinary=[%s]",
						wordToken.word, wordToken.negate, tmpMatch, BinaryUtil.listToString(searchBinary, 1)));

				if (matchMap == null) {
					// 1レコードで完結する場合は他ワードのチェック結果と条件結合.
					if (this.isFirstCheck) {
						isMatch = tmpMatch;
					} else if (and) {
						isMatch = isMatch && tmpMatch;
					} else {
						isMatch = isMatch || tmpMatch;
					}
					this.isFirstCheck = false;
				} else {
					// 他レコードも見た上で判定必要な場合は前の確認結果元にマップに格納.
					this.setMatchMap(wordToken, tmpMatch, matchMap);
				}
			}
		}
		return isMatch;
	}

	/**
	 * 16進数検索.
	 * 
	 * @param recordBinary
	 *            検索対象レコードバイナリ.
	 * @param hexTokenList
	 *            検索対象ワード.
	 * @param and
	 *            結合条件(true:and,false:or).
	 * @param isMatch
	 *            表示レコードマッチ結果.
	 * @param matchMap
	 *            マッチ結果格納マップ.
	 */
	private boolean searchHex(List<Byte> recordBinary, List<Token> hexTokenList, boolean and, boolean isMatch,
			Map<String, Boolean> matchMap) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		boolean tmpMatch = false;
		List<Byte> searchBinary = null;

		// 16進数検索.
		if (hexTokenList != null && !hexTokenList.isEmpty()) {
			for (Token hexToken : hexTokenList) {
				// 16進数文字列をバイトリストに変換する.
				searchBinary = BinaryUtil.stringToList(hexToken.word);

				// 検索ワードがレコードバイナリに含まれるかどうか判定.
				if (hexToken.negate) {
					tmpMatch = !(BinaryUtil.byteListIndexOf(recordBinary,searchBinary) >= 0 );
				} else {
					tmpMatch = (BinaryUtil.byteListIndexOf(recordBinary,searchBinary) >= 0 );
				}

				logger.debug(String.format(
						methodName + DELIMITER
								+ "match binary record by hex string. hex=%s negate=%b matchResult=%b, searchBinary=[%s]",
						hexToken.word, hexToken.negate, tmpMatch, BinaryUtil.listToString(searchBinary, 1)));

				// 条件結合.
				if (matchMap == null) {
					// 1レコードで完結する場合は他ワードのチェック結果と条件結合.
					if (this.isFirstCheck) {
						isMatch = tmpMatch;
					} else if (and) {
						isMatch = isMatch && tmpMatch;
					} else {
						isMatch = isMatch || tmpMatch;
					}
					this.isFirstCheck = false;
				} else {
					// 他レコードも見た上で判定必要な場合は条件結合してマップに格納.
					this.setMatchMap(hexToken, tmpMatch, matchMap);
				}
			}
		}

		return isMatch;
	}

	/**
	 * ファイル全体監視の検索結果セット.
	 * 
	 * @param token
	 *            検索ワード.
	 * @param tmpMatch
	 *            レコードの検索結果.
	 * @param matchMap
	 *            検索ワード毎に前レコードまでの検索結果を格納しているマップ.
	 * 
	 */
	private void setMatchMap(Token token, boolean tmpMatch, Map<String, Boolean> matchMap) {
		Boolean beforeMatch = matchMap.get(token.toString());
		if (beforeMatch == null) {
			// 前の確認結果ないのでそのまま格納.
			matchMap.put(token.toString(), tmpMatch);
		} else if (token.negate) {
			// 他レコード含めてtrue(含んでない)じゃないとだめ.
			matchMap.put(token.toString(), beforeMatch && tmpMatch);
		} else {
			// 他レコード含めてどれかしらがtrue(含んでる)ならtrueとしてみなす.
			matchMap.put(token.toString(), beforeMatch || tmpMatch);
		}
	}

	/**
	 * 検索結果を表示分のみに絞り込む.
	 * 
	 * @param from
	 *            表示先頭No(0はじまり).
	 * @param size
	 *            表示件数.
	 * @param allResults
	 *            検索結果の全件.
	 * @return onlyViewResults 表示分のみに絞り込んだ検索結果
	 */
	private List<CollectBinaryData> refineOnlyView(Integer from, Integer size, List<CollectBinaryData> allResults)
			throws HinemosDbTimeout, HinemosUnknown, InvalidSetting {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		// 不正な引数がクライアントから渡されていないかチェック.
		if (from < 0) {
			// マイナス値はクライアントから渡されない想定なのでwarnかつunknown.
			String message = String.format(
					"failed to refine results of binary search because the number of top record is negative. from=%d",
					from);
			logger.warn(methodName + DELIMITER + message);
			throw new HinemosUnknown(message);
		}
		if (size <= 0) {
			// 0以下はクライアントから渡されない想定なのでwarnかつunknown.
			String message = String.format(
					"failed to refine results of binary search because the number of last record is under top. from=%d",
					size);
			logger.warn(methodName + DELIMITER + message);
			throw new HinemosUnknown(message);
		}

		// 初回の検索時からデータ削除されて件数が不整合になっていないかチェック.
		if (from >= allResults.size()) {
			// 初回の検索時からデータを削除された可能性があるので、検索からやり直しさせる.
			logger.info(methodName + DELIMITER
					+ String.format(
							"failed to refine results of binary search because number of top record is over size. from=%d, size=%d",
							from, allResults.size()));
			throw new InvalidSetting(MessageConstant.MESSAGE_HUB_BINARY_SEARCH_AGAIN.getMessage());
		}

		// 表示先頭レコード～全件取得の最終レコードに絞り込む.
		List<CollectBinaryData> onlyViewResults;
		if (from > 0) {
			onlyViewResults = allResults.subList(from, allResults.size());
			logger.debug(methodName + DELIMITER + String.format(
					"success to refine forward results of binary search. from=%d, to=%d", from, allResults.size()));
		} else {
			onlyViewResults = allResults;
			logger.debug(methodName + DELIMITER
					+ String.format("skip to refine forward results of binary search. from=%d", from));
		}

		// 表示先頭レコード～表示最終レコードに絞り込む.
		if (onlyViewResults.size() > size) {
			onlyViewResults = onlyViewResults.subList(0, size);
			logger.debug(methodName + DELIMITER
					+ String.format("success to refine rearward results of binary search. to=%d, preSize=%d", size,
							onlyViewResults.size()));
		} else {
			logger.debug(methodName + DELIMITER
					+ String.format("skip to refine rearward results of binary search. to=%d, preSize=%d", size,
							onlyViewResults.size()));
		}

		return onlyViewResults;

	}

	/**
	 * ファイル全体監視のレコードを先頭レコードに置き換える.<br>
	 * 
	 * @param dataResults
	 *            表示データ.
	 * @return キーワード絞込みされた検索結果、キーワード入力なしの場合は全件返却.
	 * 
	 * @throws HinemosDbTimeout
	 */
	private List<CollectBinaryData> replaceTopRecord(Integer searchTimeout, List<CollectBinaryData> dataResults)
			throws HinemosDbTimeout {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		logger.debug(methodName + DELIMITER
				+ String.format("start to replace top record. result size=%d", dataResults.size()));

		List<CollectBinaryData> replacedResults = new ArrayList<CollectBinaryData>();

		for (CollectBinaryData data : dataResults) {

			// タグからファイル種類取得.
			String collectType = data.getCollectType();

			if (BinaryConstant.COLLECT_TYPE_WHOLE_FILE.equals(collectType)) {
				// ファイル全体監視は先頭レコードを取得しなおす.
				if (BinaryConstant.FILE_POSISION_TOP.equals(data.getFilePosition())) {
					// 既に先頭レコードならskip.
					replacedResults.add(data);
					logger.debug(methodName + DELIMITER
							+ String.format(
									"skip to replace top record. collectId=%d, dataId=%d, collectType=%s, filePosition=%s",
									data.getCollectId(), data.getDataId(), collectType, data.getFilePosition()));
					continue;
				}

				// 先頭レコードをDBから取得.
				String queryStr = String.format("SELECT DISTINCT d " + "FROM CollectBinaryData d "
						+ "WHERE d.fileKey='%s'" + " AND d.filePosition='%s' ", data.getFileKey(),
						BinaryConstant.FILE_POSISION_TOP);
				logger.debug(
						methodName + DELIMITER + String.format("query data to get top record. queryStr=%s.", queryStr));

				// DBアクセスして実データ取得.
				Map<String, Object> parameters = new HashMap<String, Object>();
				List<CollectBinaryData> topRecord = QueryExecutor.getListByJpqlWithTimeout(queryStr,
						CollectBinaryData.class, parameters, searchTimeout);
				if (topRecord == null || topRecord.isEmpty()) {
					// 取得できない場合は1件のみのレコードなので足しておく.
					replacedResults.add(data);
					logger.debug(methodName + DELIMITER
							+ String.format(
									"failed to replace top record. collectId=%d, dataId=%d, collectType=%s, filePosition=%s, fileKey=%s",
									data.getCollectId(), data.getDataId(), collectType, data.getFilePosition(),
									data.getFileKey()));
					continue;
				}

				// 先頭レコードを表示用レコードとして追加.
				replacedResults.add(topRecord.get(0));
				logger.debug(methodName + DELIMITER
						+ String.format(
								"replace top record. collectId=%d, dataId=%d, collectType=%s, filePosition=%s, fileKey=%s",
								topRecord.get(0).getCollectId(), topRecord.get(0).getDataId(), collectType,
								data.getFilePosition(), data.getFileKey()));
				continue;

			} else {
				// ファイル全体監視以外はそのまま返却.
				replacedResults.add(data);
				logger.debug(methodName + DELIMITER
						+ String.format("skip to replace top record. collectId=%d, dataId=%d, collectType=%s",
								data.getCollectId(), data.getDataId(), collectType));
				continue;
			}
		}

		return replacedResults;
	}

}
