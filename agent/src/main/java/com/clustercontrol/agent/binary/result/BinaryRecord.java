/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.binary.result;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.binary.BinaryMonitorConfig;
import com.clustercontrol.agent.binary.factory.BinaryCollector;
import com.clustercontrol.binary.util.BinaryBeanUtil;
import com.clustercontrol.util.BinaryUtil;
import com.clustercontrol.util.XMLUtil;
import com.clustercontrol.ws.agentbinary.BinaryRecordDTO;
import com.clustercontrol.ws.monitor.BinaryPatternInfo;

/**
 * バイナリレコードクラス.<br>
 * <br>
 * バイナリログ/連続バイナリデータをレコード毎に分割するにあたって<br>
 * レコード毎の情報を格納するクラス.<br>
 * <br>
 * フィールドの追加/削除行う場合は下記xml送信用DTOと平仄をとること.<br>
 * /HinemosManager/src_binary/com/clustercontrol/binary/bean/BinaryRecordDTO
 * 
 */
public class BinaryRecord {

	// ログ出力関連
	/** ロガー */
	private static Log m_log = LogFactory.getLog(BinaryRecord.class);
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	// レコードに関する情報.
	/** ファイル内レコード位置 */
	private String filePosition;
	/** レコード連番(キー生成用) */
	private String sequential;
	/** レコードキー(取り出し順) */
	private String key;
	/** タイムスタンプ(レコード毎) */
	private Timestamp ts;
	/** レコードサイズ(可変長の場合はレコードヘッダ含まない) */
	private int size;
	/** 各種タグ(key=tagName,value=tagValue) */
	private Map<String, String> tags;
	/** マッチしたバイナリ検索条件(フィルタなしはnull) */
	private BinaryPatternInfo matchBinaryProvision;

	/** レコードバイナリ全体(ヘッダ含む) */
	private List<Byte> alldata;
	/** 監視対象ファイルとの接続オブジェクト */
	private FileChannel fileChannel;
	/** レコードバイナリ開始位置 */
	private long recordStart;

	// コンストラクタ.
	/**
	 * 空データ作成.
	 */
	public BinaryRecord() {
		this.sequential = "";
		this.key = "";
		this.alldata = new ArrayList<Byte>();
		this.ts = new Timestamp(0);
		this.size = 0;
		this.tags = new HashMap<String, String>();

		this.fileChannel = null;
		this.recordStart = 0;
	}

	/**
	 * レコードデータのみ指定.
	 */
	public BinaryRecord(List<Byte> alldata) {
		this.sequential = "";
		this.key = "";
		this.alldata = alldata;
		this.ts = new Timestamp(0);
		this.size = alldata.size();
		this.tags = new HashMap<String, String>();

		this.fileChannel = null;
		this.recordStart = 0;
	}

	/**
	 * キーとレコードデータ指定.
	 */
	public BinaryRecord(String key, List<Byte> alldata) {
		this.key = key;
		this.alldata = alldata;
		this.ts = new Timestamp(0);
		this.size = alldata.size();
		this.tags = new HashMap<String, String>();

		this.fileChannel = null;
		this.recordStart = 0;
	}

	/**
	 * キーとファイルチャンネル指定(リスト保持なしタイプ).<br>
	 * <br>
	 * OutOfMemoryError対策.
	 */
	public BinaryRecord(String key, FileChannel fileChannel, long recordStart, int size) {
		this.key = key;
		this.tags = new HashMap<String, String>();
		this.ts = new Timestamp(0);

		this.alldata = null;
		this.fileChannel = fileChannel;
		this.recordStart = recordStart;
		this.size = size;
	}

	/**
	 * コピーオブジェクト作成.
	 */
	public BinaryRecord(BinaryRecord record) {
		this.sequential = record.sequential;
		this.key = record.key;
		this.alldata = record.alldata;
		this.ts = record.ts;
		this.size = record.size;
		this.tags = record.tags;

		this.fileChannel = record.fileChannel;
		this.recordStart = record.recordStart;
	}

	/**
	 * xml送信用DTOの取得.<br>
	 * <br>
	 * フィールドの値を元にxml送信用DTOを生成して返却する.<br>
	 * ※String型のフィールドはxml無効文字列を除外すること.
	 * 
	 * @return xml送信用DTO.
	 * 
	 */
	public BinaryRecordDTO getDTO() {
		BinaryRecordDTO dto = new BinaryRecordDTO();

		dto.setFilePosition(filePosition);
		dto.setSequential(sequential);
		dto.setKey(XMLUtil.ignoreInvalidString(key));
		dto.setBase64Str(XMLUtil.ignoreInvalidString(BinaryUtil.listToBase64(this.getAlldata())));

		// 16進数表記はログ出力やメッセージ出力に使われるので長すぎる場合はカット.
		List<Byte> messByte = null;
		int maxLength = BinaryMonitorConfig.getHexstrMaxLength() / 2;
		if (this.getAlldata().size() > maxLength) {
			messByte = new ArrayList<Byte>(this.getAlldata());
			messByte = messByte.subList(0, maxLength);
		} else {
			messByte = this.getAlldata();
		}
		dto.setOxStr(XMLUtil.ignoreInvalidString(BinaryUtil.listToString(messByte, 1)));

		dto.setRecTime(Long.valueOf(ts.getTime()));
		dto.setTags(XMLUtil.ignoreInvalidString(BinaryBeanUtil.tagMapToStr(tags)));
		dto.setMatchBinaryProvision(matchBinaryProvision);

		return dto;
	}

	/**
	 * レコード統合.
	 */
	public void add(BinaryRecord addRecord) {

		if (this.alldata != null) {
			this.alldata.addAll(addRecord.getAlldata());
		}

		this.size = this.size + addRecord.getSize();

	}

	// 各フィールドのsetterとgetter.
	/** レコード先頭識別フラグ */
	public String getFilePosition() {
		return filePosition;
	}

	/** レコード先頭識別フラグ */
	public void setFilePosition(String filePosition) {
		this.filePosition = filePosition;
	}

	/**
	 * レコードバイナリ全体(ヘッダ含む)取得.<br>
	 * <br>
	 * 保持しているリストがない場合は読込み結果を返却.
	 */
	public List<Byte> getAlldata() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start.");
		if (this.alldata != null) {
			return alldata;
		}

		// 設定値不正.
		if (this.fileChannel == null || this.recordStart < 0 || this.size < 0) {
			m_log.warn(methodName + DELIMITER + "failed to get list of Byte because of invalid setting.");
			return null;
		}

		// 読込んでレコードセット.
		List<Byte> record = new ArrayList<Byte>();
		boolean success = false;
		try {
			this.fileChannel.position(this.recordStart);
			BinaryCollector collector = new BinaryCollector();
			success = collector.addBinaryList(record, this.fileChannel, this.size);
			if (!success) {
				m_log.warn(methodName + DELIMITER + "failed to get list of Byte because of reading");
				return null;
			}
		} catch (IOException e) {
			m_log.warn(methodName + DELIMITER + "failed to get list of Byte because of reading Exception."
					+ e.getMessage(), e);
			return null;
		}

		return record;
	}

	/** レコードバイナリ全体(ヘッダ含む) */
	public void setAlldata(List<Byte> alldata) {
		this.alldata = alldata;
	}

	/** レコードキー情報 */
	public String getKey() {
		return key;
	}

	/** レコードキー情報 */
	public void setKey(String key) {
		this.key = key;
	}

	/** タイムスタンプ */
	public Timestamp getTs() {
		return ts;
	}

	/** タイムスタンプ */
	public void setTs(Timestamp ts) {
		this.ts = ts;
	}

	/** レコードサイズ(可変長の場合はレコードヘッダ含まない) */
	public int getSize() {
		return size;
	}

	/** レコードサイズ(可変長の場合はレコードヘッダ含まない) */
	public void setSize(int size) {
		this.size = size;
	}

	/** レコード連番(キー生成用) */
	public String getSequential() {
		return sequential;
	}

	/** レコード連番(キー生成用) */
	public void setSequential(String sequential) {
		this.sequential = sequential;
	}

	/** 各種タグ(key=tagName,value=tagValue) */
	public Map<String, String> getTags() {
		return tags;
	}

	/** マッチしたバイナリ検索条件(フィルタなしはnull) */
	public BinaryPatternInfo getMatchBinaryProvision() {
		return matchBinaryProvision;
	}

	/** マッチしたバイナリ検索条件(フィルタなしはnull) */
	public void setMatchBinaryProvision(BinaryPatternInfo matchBinaryProvision) {
		this.matchBinaryProvision = matchBinaryProvision;
	}

}
