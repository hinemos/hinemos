/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.binary.factory;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.binary.BinaryMonitorConfig;
import com.clustercontrol.agent.binary.result.BinaryRecord;
import com.clustercontrol.binary.bean.BinarySearchBean;
import com.clustercontrol.binary.util.BinaryBeanUtil;
import com.clustercontrol.util.BinaryUtil;
import com.clustercontrol.ws.monitor.BinaryPatternInfo;

/**
 * バイナリフィルタリングクラス.
 * 
 * @since 6.1.0
 * @version 6.1.0
 */
public class BinaryFiltering {

	// ログ出力関連
	/** ロガー */
	private static Log log = LogFactory.getLog(BinaryFiltering.class);
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	// フィルタ条件フィールド.
	/** バイナリ検索条件リスト */
	private List<BinaryPatternInfo> matchInfoList;
	/** バイナリ検索条件マップ(キー優先順) */
	private Map<Integer, BinaryPatternInfo> matchInfoMap;
	/** 検索パターンバイナリマップ(キー優先順) */
	private Map<Integer, List<Byte>> patternBinaryMap;

	// 任意バイナリファイル向けフィールド.
	/** 検索パターンバイナリ最大バイト長 */
	private int maxPatternLength;
	/** マッチパターン優先順位 */
	private int priority;

	// エラーハンドリング用.
	/** 引数エラー(true:エラー、false:正常) */
	private boolean errorParams;

	// マッチ結果保持用.
	/** マッチしたパターンのキー(優先順位)(マッチなしは-1) */
	private Integer matchKey;

	/**
	 * コンストラクタ.
	 * 
	 * @param matchInfoList
	 *            バイナリ検索条件リスト
	 * @param patternList
	 *            検索文字列リスト
	 */
	public BinaryFiltering(List<BinaryPatternInfo> matchInfoList) {
		// 引数不正チェック.
		if (matchInfoList == null || matchInfoList.isEmpty()) {
			log.warn("failed to match pattern by parameter");
			this.errorParams = true;
			return;
		} else {
			this.errorParams = false;
		}
		this.matchInfoList = matchInfoList;
		this.maxPatternLength = this.setMaps();
	}

	/**
	 * バイナリレコード毎にパターンマッチするかチェック.
	 * 
	 * @param binary
	 *            検索対象バイナリが格納されたBinaryRecord
	 * 
	 * @return 処理対象の場合はtrue,引数不正もfalse;
	 * 
	 */
	public boolean matchPatternRecord(BinaryRecord binary) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		log.debug(methodName + DELIMITER + "start.");

		// 変数初期化.
		List<Byte> searchBinary = new ArrayList<Byte>(binary.getAlldata());

		// 引数存在チェック.
		if (errorParams || searchBinary == null || searchBinary.isEmpty()) {
			log.warn("failed to match pattern by record binary is empty");
			return false;
		}

		// パターンマッチするかチェック.
		return this.matchPattern(searchBinary);

	}

	/**
	 * サイズの大きなバイナリがパターンマッチするかチェック.
	 * 
	 * @param bigData
	 *            検索対象バイナリが格納されたList<BinaryRecord>
	 * 
	 * @return 処理対象の場合はtrue,引数不正もfalse;
	 * 
	 */
	public boolean matchPatternBigData(List<BinaryRecord> bigData) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		log.debug(methodName + DELIMITER + "start.");

		// 引数存在チェック.
		if (errorParams || bigData == null || bigData.isEmpty()) {
			log.warn("failed to match pattern");
			return false;
		}

		// ループ用変数初期化.
		List<BinaryRecord> searchData = new ArrayList<BinaryRecord>();
		BinaryRecord tmpRecord = null;

		// 検索パターンバイナリより検索対象のレコードが長くなるように整形する
		for (BinaryRecord record : bigData) {
			if (tmpRecord == null && record.getSize() > maxPatternLength) {
				// 元々の1レコードが長いならそのまま足す.
				searchData.add(new BinaryRecord(record));
				log.debug(methodName + DELIMITER + String.format(
						"add search data by a record. recordSize=%d, length=%d", record.getSize(), maxPatternLength));
			} else {
				// 元々の1レコードが短い場合はレコード統合してく.
				if (tmpRecord == null) {
					tmpRecord = new BinaryRecord(record);
				} else {
					tmpRecord.add(record);
				}
				if (tmpRecord.getSize() > maxPatternLength) {
					searchData.add(tmpRecord);
					log.debug(methodName + DELIMITER
							+ String.format("add search data by coodinated records. recordSize=%d, length=%d",
									tmpRecord.getSize(), maxPatternLength));
					tmpRecord = null;
				}
			}
		}
		// 末尾の半端分を検索対象として足す.
		if (tmpRecord != null) {
			searchData.add(tmpRecord);
		}

		// ループ用変数初期化.
		int topPriority = 0;
		List<Byte> joinBinary = new ArrayList<Byte>();
		boolean isTopProcess = false;
		boolean isProcess = false;
		boolean joined = false;
		int fromIndex = 0;

		// 整形した検索対象レコードとレコード間の継目についてパターンマッチチェック.
		Iterator<BinaryRecord> searchDataItr = searchData.iterator();
		while (searchDataItr.hasNext()) {
			BinaryRecord record = searchDataItr.next();

			// 前のレコードとの継目として先頭の最大パターンバイト長分を追加.
			if (record.getSize() > maxPatternLength) {
				joinBinary.addAll(record.getAlldata().subList(0, maxPatternLength));
				joined = true;
				log.debug(methodName + DELIMITER + String.format("joined prefix binary. length=%d", maxPatternLength));
			} else {
				// 整形しているのでここの分岐入るのは最終レコードのみ.
				joinBinary.addAll(record.getAlldata());
				joined = false;
				log.debug(methodName + DELIMITER + String.format("joined all binary. length=%d", record.getSize()));
			}

			// 前のレコードとの継目についてパターンマッチチェック.
			if (joinBinary.size() >= maxPatternLength) {
				log.debug(methodName + DELIMITER + "check of the joint to forward record");
				isProcess = matchPattern(joinBinary);
				if (topPriority < this.priority) {
					topPriority = this.priority;
					isTopProcess = isProcess;
				}
				if (topPriority == 1) {
					// 優先順位第一位の条件合致した場合は終了.
					break;
				}
				// OutOfMemoryError防止用にチェック済レコード削除.
				joinBinary = null;
			} else if (joined) {
				// 中途半端な開始の継目初期化.
				joinBinary = null;
			}

			// レコードのパターンマッチチェック.
			log.debug(methodName + DELIMITER + "check of the record");
			isProcess = matchPattern(record.getAlldata());
			if (topPriority < this.priority) {
				topPriority = this.priority;
				isTopProcess = isProcess;
			}
			if (topPriority == 1) {
				// 優先順位第一位の条件に合致した場合は終了.
				break;
			}

			// 中途半端な継目もしくはパターンマッチチェック済の場合、次のレコードとの継目を初期化.
			if (joinBinary == null) {
				// 末尾の最大パターンバイト長分を継目バイナリとして設定.
				if (record.getSize() > maxPatternLength) {
					fromIndex = record.getSize() - maxPatternLength;
					joinBinary = record.getAlldata().subList(fromIndex, record.getSize());
					log.debug(methodName + DELIMITER
							+ String.format("joined suffix binary. length=%d", maxPatternLength));
				} else {
					joinBinary = record.getAlldata();
					log.debug(methodName + DELIMITER + String.format("joined all binary. length=%d", record.getSize()));
				}
			}

			// OutOfMemoryError防止用に不要なレコードから削除.
			searchDataItr.remove();
			record = null;

		}

		// 監視結果送信用にどのキーでマッチしたかセット.
		if (topPriority != 0) {
			this.matchKey = Integer.valueOf(topPriority);
		} else {
			this.matchKey = Integer.valueOf(-1);
		}
		log.debug(
				methodName + DELIMITER + "matchKey = " + this.matchKey.toString() + ", isTopProcess = " + isTopProcess);
		return isTopProcess;
	}

	/**
	 * 検索情報を優先順をキーにマップ化する.
	 * 
	 * @return 最大検索パターンバイト長
	 */
	private int setMaps() {

		// 検索条件とパターンマップ化.
		this.matchInfoMap = new TreeMap<Integer, BinaryPatternInfo>();
		for (int order = 1; order <= this.matchInfoList.size(); order++) {
			this.matchInfoMap.put(order, this.matchInfoList.get(order - 1));
		}

		// 検索文字列バイナリマップの設定.
		this.patternBinaryMap = new TreeMap<Integer, List<Byte>>();

		// ループ用変数初期化.
		List<Byte> patternBinary = null;
		BinaryPatternInfo matchInfo = null;

		int maxPatternLength = 0;

		// 優先順に取り出して検索パターンをバイナリに変換.
		for (Map.Entry<Integer, BinaryPatternInfo> matchInfoEntry : this.matchInfoMap.entrySet()) {

			// パターンとセットのバイナリ検索条件を取得.
			matchInfo = matchInfoEntry.getValue();

			// 検索文字列と検索パターン取得.
			BinarySearchBean binarySearch = BinaryBeanUtil.getSearchBean(matchInfo.getGrepString());

			switch (binarySearch.getSearchType()) {

			case STRING:
				// 検索文字列をユーザー指定の方式でデコード.
				try {
					patternBinary = BinaryUtil.arrayToList(matchInfo.getGrepString().getBytes(matchInfo.getEncoding()));
				} catch (UnsupportedEncodingException e) {
					patternBinary = null;
					log.warn("input encoding [" + matchInfo.getEncoding() + "] is not supported :" + e.getMessage());
					break;
				}
				if (patternBinary == null) {
					log.warn("grep string [" + matchInfo.getGrepString() + "] failed to change binary");
				}
				break;

			case HEX:
				// 16進数文字列をバイトリストに変換する.
				patternBinary = BinaryUtil.stirngToList(binarySearch.getOnlyHexString());
				if (patternBinary == null) {
					log.warn("grep string(hex) [" + matchInfo.getGrepString() + "] failed to change binary");
				}
				break;

			case EMPTY:
			case ERROR:
			default:
				// 他は不正データ(想定外).
				patternBinary = null;
				log.warn("grep string [" + matchInfo.getGrepString() + "] failed to get search type");
				break;
			}

			// 検索対象バイナリ追加.
			patternBinaryMap.put(matchInfoEntry.getKey(), patternBinary);

			if (patternBinary == null) {
				continue;
			}

			// 最大検索バイト長をセット(ファイル全体監視の処理向け).
			if (maxPatternLength < patternBinary.size()) {
				maxPatternLength = patternBinary.size();
			}

		}

		return maxPatternLength;

	}

	/**
	 * パターンマッチするかチェック.
	 * 
	 * @param searchBinary
	 *            検索対象バイナリ
	 * 
	 * @return 処理対象の場合はtrue,引数不正もfalse;
	 * 
	 */
	private boolean matchPattern(List<Byte> searchBinary) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		log.debug(methodName + DELIMITER + "start.");

		// ループ用変数初期化.
		BinaryPatternInfo binaryProvision = null;
		List<Byte> patternBinary = null;
		this.priority = 0;

		// 優先順に取り出してマッチするか確認.
		for (Map.Entry<Integer, BinaryPatternInfo> matchInfoEntry : this.matchInfoMap.entrySet()) {

			// マッチキーをセット.
			this.matchKey = matchInfoEntry.getKey();
			log.debug(methodName + DELIMITER + "checked match with matchKey = " + this.matchKey.toString());

			// パターンとセットのバイナリ検索条件を取得.
			binaryProvision = matchInfoEntry.getValue();
			patternBinary = patternBinaryMap.get(matchInfoEntry.getKey());
			log.debug(methodName + DELIMITER + "binary provision to match : searchText = "
					+ binaryProvision.getGrepString() + ", processType = " + binaryProvision.isProcessType());

			// 引数不正等で検索対象文字列がバイナリ変換できていない場合(変換時にログ出力済).
			if (patternBinary == null) {
				continue;
			}

			// ログ出力(バイナリ長いとログ見づらいので最大長でカット).
			if (log.isDebugEnabled()) {
				int maxLength = BinaryMonitorConfig.getHexstrMaxLength() / 2;
				List<Byte> showPatBinary = new ArrayList<Byte>(patternBinary);
				List<Byte> showSeaBinary = new ArrayList<Byte>(searchBinary);
				if (showPatBinary.size() > maxLength) {
					showPatBinary = showPatBinary.subList(0, maxLength);
				}
				if (searchBinary.size() > maxLength) {
					showSeaBinary = showSeaBinary.subList(0, maxLength);
				}
				log.debug(methodName + DELIMITER + String.format("patternBinary : size=%dbyte, value=[%s]",
						patternBinary.size(), BinaryUtil.listToString(showPatBinary, 1)));
				log.debug(methodName + DELIMITER + String.format("searchBinary : size=%dbyte, value=[%s]",
						searchBinary.size(), BinaryUtil.listToString(showSeaBinary, 1)));
			}

			// 検索対象のバイナリ値が短い場合はチェックスキップ.
			if (searchBinary.size() < patternBinary.size()) {
				log.info(methodName + DELIMITER
						+ String.format(
								"skip to search because search binary is longer. size(search)=%dbyte, size(pattern)=%d, searchText=%s",
								searchBinary.size(), patternBinary.size(), binaryProvision.getGrepString()));
				continue;
			}

			// 検索対象のバイナリが含まれてるかチェック.
			if (searchBinary.containsAll(patternBinary)) {
				// 優先順位を返却値として格納.
				this.priority = matchInfoEntry.getKey().intValue();
				if (binaryProvision.isProcessType()) {
					// 条件に一致したら処理するの場合は含まれてるので処理対象としてtrue返却.
					log.debug(methodName + DELIMITER + "matched and process(binary)");
					return true;
				} else {
					// 条件に一致したら処理しないの場合は処理対象外としてfalse返却.
					log.debug(methodName + DELIMITER + "matched and unprocess(binary)");
					return false;
				}
			}
			log.debug(methodName + DELIMITER + "unmatched. ");

		}

		// マッチなし.
		this.matchKey = Integer.valueOf(-1);
		return false;
	}

	/**
	 * マッチしたパターンの優先順位取得
	 */
	public int getmatchKey() {
		return this.matchKey.intValue();
	}

	/**
	 * マッチしたバイナリ検索条件取得<br>
	 * <br>
	 * 監視結果送信用ロジック.
	 */
	public BinaryPatternInfo getMatchBinaryProvision() {
		if (this.matchKey <= 0) {
			return null;
		}
		return this.matchInfoMap.get(this.matchKey);
	}
}
