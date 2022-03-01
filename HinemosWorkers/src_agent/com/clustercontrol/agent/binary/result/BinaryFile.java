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
import org.openapitools.client.model.AgtBinaryFileDTORequest;

import com.clustercontrol.agent.binary.factory.BinaryCollector;
import com.clustercontrol.binary.util.BinaryBeanUtil;
import com.clustercontrol.util.BinaryUtil;
import com.clustercontrol.util.XMLUtil;

public class BinaryFile {

	// ログ出力関連
	/** ロガー */
	private static Log m_log = LogFactory.getLog(BinaryFile.class);
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	// 取得元のファイルに関する情報.
	/** 最終更新日時 */
	private Timestamp lastModTime = new Timestamp(0);
	/** 各種タグ(key=tagName,value=tagValue) */
	private Map<String, String> tags = new HashMap<String, String>();

	/** レコード数 */
	private int recordCount;

	/**
	 * ファイルヘッダ<br>
	 * <br>
	 * 増分監視のみ設定される、ファイル全体は特にファイルヘッダとして区別しない
	 */
	private List<Byte> fileHeader = new ArrayList<Byte>();
	/** 監視対象ファイルとの接続オブジェクト */
	private FileChannel fileChannel;
	/** ファイルヘッダ開始位置 */
	private long fileHeaderStart;
	/** ファイルヘッダサイズ */
	private int fileHeaderSize;

	/**
	 * xml送信用DTOの取得.<br>
	 * <br>
	 * フィールドの値を元にxml送信用DTOを生成して返却する.<br>
	 * ※String型のフィールドはxml無効文字列を除外すること.
	 * 
	 * @return xml送信用DTO.
	 * 
	 */
	public AgtBinaryFileDTORequest getDTO() {
		AgtBinaryFileDTORequest dto = new AgtBinaryFileDTORequest();

		dto.setLastModTime(Long.valueOf(lastModTime.getTime()));
		dto.setFileHeader(XMLUtil.ignoreInvalidString(BinaryUtil.listToBase64(this.getFileHeader())));
		dto.setTags(XMLUtil.ignoreInvalidString(BinaryBeanUtil.tagMapToStr(tags)));
		dto.setRecordCount(this.recordCount);

		return dto;
	}

	// 各フィールドのsetterとgetter.
	/** 最終更新日時 */
	public Timestamp getLastModTime() {
		return lastModTime;
	}

	/** 最終更新日時 */
	public void setLastModTime(Timestamp lastModTime) {
		this.lastModTime = lastModTime;
	}

	/** レコード数 */
	public int getRecordCount() {
		return recordCount;
	}

	/** レコード数 */
	public void setRecordCount(int recordCount) {
		this.recordCount = recordCount;
	}

	/** ファイルヘッダ */
	public List<Byte> getFileHeader() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start.");
		if (this.fileHeader != null) {
			return this.fileHeader;
		}

		// 設定値不正.
		if (this.fileChannel == null || this.fileHeaderStart < 0 || this.fileHeaderSize < 0) {
			m_log.warn(methodName + DELIMITER + "failed to get list of Byte because of invalid setting.");
			return null;
		}

		// 読込んでレコードセット.
		List<Byte> hedear = new ArrayList<Byte>();
		boolean success = false;
		try {
			this.fileChannel.position(this.fileHeaderStart);
			BinaryCollector collector = new BinaryCollector();
			success = collector.addBinaryList(hedear, this.fileChannel, this.fileHeaderSize);
			if (!success) {
				m_log.warn(methodName + DELIMITER + "failed to get list of Byte because of reading");
				return null;
			}
		} catch (IOException e) {
			m_log.warn(methodName + DELIMITER + "failed to get list of Byte because of reading Exception."
					+ e.getMessage(), e);
			return null;
		}

		return hedear;
	}

	/** ファイルヘッダ */
	public void setFileHeader(List<Byte> fileHeader) {
		this.fileHeader = fileHeader;
	}

	/** 各種タグ(key=tagName,value=tagValue) */
	public Map<String, String> getTags() {
		return tags;
	}

	/** 監視対象ファイルとの接続オブジェクト */
	public FileChannel getFileChannel() {
		return fileChannel;
	}

	/** 監視対象ファイルとの接続オブジェクト */
	public void setFileChannel(FileChannel fileChannel) {
		this.fileChannel = fileChannel;
	}

	/** ファイルヘッダ開始位置 */
	public long getFileHeaderStart() {
		return fileHeaderStart;
	}

	/** ファイルヘッダ開始位置 */
	public void setFileHeaderStart(long fileHeaderStart) {
		this.fileHeaderStart = fileHeaderStart;
	}

	/** ファイルヘッダサイズ */
	public int getFileHeaderSize() {
		if (this.fileHeader != null) {
			return this.fileHeader.size();
		}
		return fileHeaderSize;
	}

	/** ファイルヘッダサイズ */
	public void setFileHeaderSize(int fileHeaderSize) {
		this.fileHeaderSize = fileHeaderSize;
	}

	@Override
	public String toString() {

		StringBuilder tagString = new StringBuilder();
		if (tags != null && !tags.isEmpty()) {
			for (Map.Entry<String, String> tag : tags.entrySet()) {
				tagString.append("[key=");
				tagString.append(tag.getKey());
				tagString.append(", value=");
				tagString.append(tag.getValue());
				tagString.append("]");
			}
		}

		String fileHeaderStr = BinaryUtil.listToString(this.getFileHeader(), 1);
		String returnStr = String.format("BinaryFile[lastModTime=%s, fileHeader=%s, tags=[%s]]", lastModTime.toString(),
				fileHeaderStr, tagString.toString());
		return returnStr;
	}

	public String toShortString() {
		String returnStr = String.format("BinaryFile[lastModTime=%s, fileHeaderSize=%d]", lastModTime.toString(),
				this.getFileHeaderSize());
		return returnStr;
	}
}
