/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.binary.factory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.binary.BinaryMonitor;
import com.clustercontrol.agent.binary.BinaryMonitorConfig;
import com.clustercontrol.agent.binary.readingstatus.FileReadingStatus;
import com.clustercontrol.agent.binary.result.BinaryFile;
import com.clustercontrol.agent.binary.result.BinaryRecord;
import com.clustercontrol.binary.bean.BinaryConstant;
import com.clustercontrol.util.BinaryUtil;
import com.clustercontrol.util.FileUtil;

public class BinaryCollector {

	// ログ出力関連
	/** ロガー */
	private static Log m_log = LogFactory.getLog(BinaryCollector.class);
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	/**
	 * 監視対象バイナリ取得.<br>
	 * <br>
	 * 現在開いているチャネルよりバイナリを取得する.<br>
	 * 前回読込位置からファイル最大サイズまで読込む.<br>
	 * ※ただしファイルヘッダは除外する.<br>
	 * 
	 * @param fileResult
	 *            ファイルヘッダ等、ファイル単位の監視結果を格納するオブジェクト.
	 * @param setList
	 *            取得したバイナリをセットするリスト.
	 * @param bm
	 *            監視処理を実行しているオブジェクト.<br>
	 *            <br>
	 * @return ローテーション実施したファイルのみを読込んだ場合true.
	 * 
	 * @throws IOException
	 * 
	 */
	public boolean setMonitorBinary(BinaryFile fileResult, List<Byte> setList, BinaryMonitor bm) throws IOException {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + String.format("start. monitorId=%s, file=[%s]", bm.getM_wrapper().getId(),
				bm.getReadingStatus().getMonFileName()));

		// 最大読込サイズを設定.
		int maxBytes = 0;
		maxBytes = BinaryMonitorConfig.getMaxGetByte();

		boolean readSuccess = false;
		int rotatedSize = 0;

		// ローテーション判定.
		if (bm.isRotateFlag()) {
			m_log.debug(
					methodName + DELIMITER + String.format("prepared to read rotated files. monitorId=%s, file=[%s]",
							bm.getM_wrapper().getId(), bm.getReadingStatus().getMonFileName()));
			// ローテーションで作成されたファイルを先に読む.
			this.addRotatedBinary(setList, bm, maxBytes);
			if (setList.size() > maxBytes) {
				// 読込長を超えている場合はカットする.
				setList = setList.subList(0, maxBytes);
			}
			if (setList.size() == maxBytes) {
				// ローテーションで作成されたファイルを先に読込んだだけで最大サイズに達したので処理完了.
				return true;
			}
			// 別ファイル読込分のサイズを引いた分を監視対象ファイル読込むように引数セット.
			rotatedSize = setList.size();
			int remainMaxByte = maxBytes - rotatedSize;
			long monitorFileReadSize = bm.getFileChannel().size() - bm.getReadingStatus().getPosition();
			if (monitorFileReadSize > remainMaxByte) {
				// 最大サイズ－ローテーションファイル読込サイズ.
				bm.setReadedSize(remainMaxByte);
			} else {
				// 前回読込位置からファイル最大サイズまで.
				bm.setReadedSize(monitorFileReadSize);
			}
		} else {
			m_log.debug(methodName + DELIMITER
					+ String.format("prepared to read only monitor file. monitorId=%s, file=[%s]",
							bm.getM_wrapper().getId(), bm.getReadingStatus().getMonFileName()));
			// ローテーションファイルを読込まない場合は前回読込位置からファイル最大サイズまで読込む.
			bm.setReadedSize(bm.getFileChannel().size() - bm.getReadingStatus().getPosition());

			// 一度に取得する最大長を超える場合は調整.
			if (bm.getReadedSize() > maxBytes) {
				bm.setReadedSize(maxBytes);
			}
		}

		// ファイルヘッダの取得.
		long fileHeadSize = bm.getM_wrapper().monitorInfo.getBinaryCheckInfo().getFileHeadSize();
		int readHeadSize = 0;
		if (fileHeadSize > 0) {
			// ファイルヘッダを読込む.
			readHeadSize = BinaryUtil.longParseInt(fileHeadSize);
			m_log.debug(methodName + DELIMITER + String.format("set the readHeadSize=%d", readHeadSize));
			bm.getFileChannel().position(0);
			fileResult.setFileHeader(new ArrayList<Byte>());
			addBinaryList(fileResult.getFileHeader(), bm.getFileChannel(), readHeadSize);
			m_log.debug(methodName + DELIMITER
					+ String.format("read the file header. size=%d", fileResult.getFileHeader().size()));
			// ファイルヘッダを避けるようにpositionずらす.
			if (bm.getReadingStatus().getPosition() < readHeadSize) {
				bm.getReadingStatus().setPosition(readHeadSize);
			}
		}

		// 監視対象のバイナリファイルをreadedSize分読込む.
		if (bm.getReadingStatus().isToSkipRecord() && bm.getReadingStatus().getSkipSize() == 0) {
			// レコード区切る時の位置調整用のサイズをセット(設定されていない場合のみ).
			bm.getReadingStatus().setSkipSize(bm.getReadingStatus().getPrevSize() - fileHeadSize);
		}
		bm.getFileChannel().position(bm.getReadingStatus().getPosition());
		m_log.debug(methodName + DELIMITER
				+ String.format(
						"prepared to read file from the position."
								+ " monitorId=%s, file=[%s], position=%d, toSkipRecord=%b, skipSize=%d",
						bm.getM_wrapper().getId(), bm.getReadingStatus().getMonFileName(),
						bm.getFileChannel().position(), bm.getReadingStatus().isToSkipRecord(),
						bm.getReadingStatus().getSkipSize()));
		int readSize = BinaryUtil.longParseInt(bm.getReadedSize());
		readSuccess = addBinaryList(setList, bm.getFileChannel(), readSize);
		// 実際に読込んだファイルサイズを格納.
		bm.setReadedSize(setList.size() - rotatedSize);
		bm.setReadSuccessFlg(readSuccess);

		return false;
	}

	/**
	 * ローテーション済ファイルから取得したバイナリを追加.<br>
	 * <br>
	 * ローテーション時に新規作成されたファイルからバイナリを取得する.<br>
	 * ファイル末尾まで取得したファイルは監視対象外として読込状態をクローズする.<br>
	 * ※最大サイズを超えた場合は次回監視時に取得.
	 * 
	 * @param setList
	 *            追加対象のリスト.
	 * @param bm
	 *            監視処理を実行中のオブジェクト. <br>
	 *            <br>
	 * @return 読込成功したかどうか.
	 * 
	 * @throws IOException
	 * 
	 */
	private boolean addRotatedBinary(List<Byte> setList, BinaryMonitor bm, int maxBytes) throws IOException {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		if (m_log.isDebugEnabled()) {
			if (bm.getRotatedRSMap() == null || bm.getRotatedRSMap().isEmpty()) {
				m_log.debug(methodName + DELIMITER
						+ String.format("start. rotatedFileCounts=0, monitorId=%s, monitorFile=[%s]",
								bm.getM_wrapper().getId(), bm.getReadingStatus().getMonFileName()));
			} else {
				m_log.debug(methodName + DELIMITER
						+ String.format("start. rotatedFileCounts=%d, monitorId=%s, monitorFile=[%s]",
								bm.getRotatedRSMap().size(), bm.getM_wrapper().getId(),
								bm.getReadingStatus().getMonFileName()));
			}
		}

		boolean readSuccess = false;
		long remainder = maxBytes;
		long readSize = maxBytes;
		int preSize = 0;

		long currentFilesize = 0L;
		long currentFileTimeStamp = 0L;
		List<Byte> currentPrefix = null;
		long position = 0L;

		for (FileReadingStatus rotateRs : bm.getRotatedRSMap().values()) {
			m_log.debug(methodName + DELIMITER + String.format(
					"prepared to read rotated file from file reading status. rotatedFile=[%s], monitorId=%s, monitorFile=[%s]",
					rotateRs.getMonFileName(), bm.getM_wrapper().getId(), bm.getReadingStatus().getMonFileName()));
			// ローテーションで作成されたファイル毎に処理.
			try (FileChannel fc = FileChannel.open(Paths.get(rotateRs.getMonFile().toURI()), StandardOpenOption.READ)) {

				// 読込位置をセット.
				fc.position(rotateRs.getPosition());
				// 読込バイト数を余ってる読込可能なサイズにセット.
				readSize = fc.size() - rotateRs.getPosition();
				if (readSize > remainder) {
					readSize = remainder;
				}
				int readSizeInt = BinaryUtil.longParseInt(readSize);

				// 読込前のサイズを取得.
				preSize = setList.size();

				// バイナリファイルの情報更新.
				currentFilesize = fc.size();
				currentFileTimeStamp = rotateRs.getMonFile().lastModified();
				currentPrefix = rotateRs.getCurrenPrefix();

				// 読込んでリストに追加.
				readSuccess = addBinaryList(setList, fc, readSizeInt);
				// 実際に読込んだサイズを取得.
				readSize = setList.size() - preSize;
				position = rotateRs.getPosition() + readSize;
				// ファイルRSの更新.
				rotateRs.storeRS(currentFilesize, position, currentFileTimeStamp, currentPrefix);

				if (position >= currentFilesize) {
					// ローテーションで作成されたファイルを全部読込んだのでRSクローズ.
					rotateRs.closeFileRS();
				}

				// 残サイズを計算.
				remainder = remainder - readSize;
				if (remainder <= 0) {
					// 読込可能なサイズがなくなったら完了.
					m_log.debug(methodName + DELIMITER
							+ String.format(
									"end to read rotated file from file reading status. remainder=%d, rotatedFile=[%s], monitorId=%s, monitorFile=[%s]",
									remainder, rotateRs.getMonFileName(), bm.getM_wrapper().getId(),
									bm.getReadingStatus().getMonFileName()));
					break;
				} else {
					m_log.debug(methodName + DELIMITER
							+ String.format(
									"continue to read rotated file from file reading status. remainder=%d, monitorId=%s, rotatedFile=[%s], monitorFile=[%s]",
									remainder, rotateRs.getMonFileName(), bm.getM_wrapper().getId(),
									bm.getReadingStatus().getMonFileName()));
				}

			}
		}
		m_log.debug(methodName + DELIMITER
				+ String.format(
						"complete to read rotated file from file reading status. monitorId=%s, monitorFile=[%s]",
						bm.getM_wrapper().getId(), bm.getReadingStatus().getMonFileName()));

		return readSuccess;
	}

	/**
	 * 指定リストにファイル読込バイナリを追加.<br>
	 * 
	 * @param setList
	 *            バイナリ追加対象のリスト.
	 * @param fc
	 *            読込対象のファイルチャネル(メソッド呼び出し前にpositionを設定すること).
	 * @param readSize
	 *            読込サイズ. <br>
	 *            <br>
	 * @return 読込成功の場合はtrue.
	 * @throws IOException
	 */
	public boolean addBinaryList(List<Byte> setList, FileChannel fc, int readSize) throws IOException {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		if (m_log.isDebugEnabled()) {
			if (setList == null) {
				m_log.debug(methodName + DELIMITER + String.format("start. setList=null, readSize=%dbyte", readSize));
			} else {
				m_log.debug(methodName + DELIMITER
						+ String.format("start. setList size=%d, readSize=%dbyte", setList.size(), readSize));
			}
		}

		if (readSize <= 0) {
			m_log.debug(methodName + DELIMITER + String.format("skip read because of readSize=%d.", readSize));
			return true;
		}
		// 読込バッファサイズをセット.
		int bufferSize = BinaryMonitorConfig.getGettingBinaryLength();
		int propertySize = bufferSize;
		// 不要なバイトを読込まないようサイズ調整.
		if (bufferSize > readSize) {
			bufferSize = readSize;
		}
		m_log.debug(methodName + DELIMITER
				+ String.format("set size of buffer to read binary. bufferSize=%d, readSize=%d, property=%d",
						bufferSize, readSize, propertySize));
		ByteBuffer monFileBuf = ByteBuffer.allocate(bufferSize);
		byte[] bytearray = monFileBuf.array();
		List<Byte> tmplist = null;
		int readed = 0;
		int reading = 0;
		boolean readSuccess = false;
		long prePosition = fc.position();
		m_log.debug(methodName + DELIMITER
				+ String.format("prepared to read binary. FileChannel prePosition=%d", prePosition));

		while (true) {

			monFileBuf.clear();
			readSuccess = false;
			reading = fc.read(monFileBuf);

			// file末まで到達.
			if (reading == -1 || reading < bufferSize) {
				reading = Integer.parseInt(Long.toString(fc.position() - readed - prePosition));

				// バッファサイズより読込サイズが小さい場合、余サイズ分の配列に前回読込分が残ってるため、サイズ調整の上再読込.
				ByteBuffer monFileEndBuf = ByteBuffer.allocate(reading);
				fc.position(readed + prePosition);

				reading = fc.read(monFileEndBuf);
				byte[] endBytearray = monFileEndBuf.array();

				if (endBytearray == null || endBytearray.length == 0) {
					m_log.info(methodName + DELIMITER + String.format("readed binary file is empty."));
					return false;
				}
				if (setList != null) {
					tmplist = new ArrayList<Byte>(BinaryUtil.arrayToList(endBytearray));
					setList.addAll(tmplist);
					tmplist = null;
				}
				readSuccess = true;

				readed = readed + reading;
				if (readed > readSize) {
					// 最大読み取り値にポジションを修正.
					fc.position(prePosition + readSize);
					if (setList != null) {
						// 取得したリストも最大読み取り値分に修正する.
						setList = setList.subList(0, readSize);
						m_log.debug(methodName + DELIMITER + String.format("cut the setList. size=%d", setList.size()));
					}
				}
				m_log.debug(methodName + DELIMITER + String.format(
						"end to read binary because of coming at end. FileChannel endPosition=%d", fc.position()));

				break;
			}

			bytearray = monFileBuf.array();

			if (setList != null) {
				tmplist = new ArrayList<Byte>(BinaryUtil.arrayToList(bytearray));
				setList.addAll(tmplist);
				tmplist = null;
			}
			bytearray = null;
			readSuccess = true;

			// 累計読み取りサイズを格納.
			readed = readed + reading;

			// ファイル末ではないけど累計読み取りサイズが最大読み取り値以上の場合.
			if (readed >= readSize) {
				// 最大読み取り値にポジションを修正.
				fc.position(prePosition + readSize);
				if (readed > readSize) {
					if (setList != null) {
						// 取得したリストも最大読み取り値分に修正する.
						setList = setList.subList(0, readSize);
						m_log.debug(methodName + DELIMITER + String.format("cut the setList. size=%d", setList.size()));
					}
					m_log.debug(methodName + DELIMITER + String.format(
							"end to read binary because of over readSize. FileChannel endPosition=%d", fc.position()));
				} else {
					m_log.debug(methodName + DELIMITER
							+ String.format(
									"end to read binary because of equaling readSize. FileChannel endPosition=%d",
									fc.position()));
				}
				break;
			}
		}

		return readSuccess;
	}

	/**
	 * 監視対象バイナリ取得.<br>
	 * <br>
	 * 任意バイナリファイル(2MB以上のデータ含む)を読込む.<br>
	 * 
	 * @param setList
	 *            取得したバイナリデータを送信用に分割して格納するリスト.
	 * @param bm
	 *            監視処理を実行しているオブジェクト.<br>
	 * @return 読込成功したらtrue
	 * 
	 * @throws IOException
	 * 
	 */
	public boolean setMonBigData(BinaryFile fileResult, List<BinaryRecord> setList, BinaryMonitor bm)
			throws IOException {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start.");

		// 最大読込ファイルサイズを設定.
		long maxFileSize = 0L;
		maxFileSize = BinaryMonitorConfig.getFileMaxSize();

		if (bm.getFileChannel().size() > maxFileSize) {
			// ファイルサイズが最大読込サイズを上回る場合.
			m_log.info(
					methodName + DELIMITER
							+ String.format(
									"over max size of monitor file. maxFileSize=%d, readingFileSize=%d, file=[%s]",
									maxFileSize, bm.getFileChannel().size(), bm.getReadingStatus().getMonFileName()));
			return false;
		}

		// 1レコードあたりの最大読込サイズを設定.
		int maxBytes = 0;
		maxBytes = BinaryMonitorConfig.getMaxGetByte();

		// 1レコードあたりの最大読込サイズがファイルサイズを超える場合は余計なバイトを読込まないよう調整.
		if (maxBytes > bm.getFileChannel().size()) {
			maxBytes = Integer.parseInt(Long.toString(bm.getFileChannel().size()));
		}

		// チャネルポジションの位置を初期化.
		bm.getFileChannel().position(0);
		boolean success = false;

		// 変数初期化.
		BinaryRecord record = null;
		String key = "";
		int keyKeta = String.valueOf(Long.MAX_VALUE).length();
		String filePosition = BinaryConstant.FILE_POSISION_TOP;
		int seqNum = 0;
		long readedSize = 0L;
		int readedRecordSize = 0;
		long prePosition = 0L;

		// ファイル読込.
		while (true) {
			// 1レコード分読込.
			prePosition = bm.getFileChannel().position();
			success = addBinaryList(null, bm.getFileChannel(), maxBytes);

			if (!success) {
				// 読込失敗したら処理終了.
				m_log.warn(methodName + DELIMITER
						+ String.format("failed to read a big file. readedSize=%d, monitorId=%s, file=[%s]", readedSize,
								bm.getM_wrapper().getId(), bm.getReadingStatus().getMonFileName()));
				return success;
			}

			// 累計読込サイズを算出.
			readedRecordSize = Integer.parseInt(Long.toString(bm.getFileChannel().position() - prePosition));
			readedSize = readedSize + readedRecordSize;

			seqNum++;
			if (m_log.isDebugEnabled()) {
				m_log.debug(methodName + DELIMITER
						+ String.format(
								"readed a record. recordSequeanceNum=%d, readedRecordSize=%d, readedSize=%d, monitorId=%s, file=[%s]",
								seqNum, readedRecordSize, bm.getFileChannel().position(), bm.getM_wrapper().getId(),
								bm.getReadingStatus().getMonFileName()));
			}

			// キー：ファイル絶対パス＋累計読込サイズ(桁そろえ)
			key = bm.getReadingStatus().getMonFileName() + FileUtil.paddingZero(readedSize, keyKeta);

			// 読込成功分をリストとして追加.
			record = new BinaryRecord(key, bm.getFileChannel(), prePosition, readedRecordSize);
			record.setSequential(FileUtil.paddingZero(readedSize, keyKeta));

			// 読込完了判定.
			if (readedSize >= bm.getFileChannel().size()) {
				record.setFilePosition(BinaryConstant.FILE_POSISION_END);
				fileResult.setRecordCount(seqNum);
				setList.add(record);
				// 累計読込サイズがファイルサイズを上回るので、ファイル読込完了.
				m_log.debug(methodName + DELIMITER
						+ String.format(
								"end readed. readedSize=%d, currentFileSize=%d, recordPosition=%s, monitorId=%s, file=[%s]",
								readedSize, bm.getFileChannel().size(), record.getFilePosition(),
								bm.getM_wrapper().getId(), bm.getReadingStatus().getMonFileName()));
				// ファイル末尾のバイナリ出力(末尾データ不正登録調査用).
				m_log.debug(methodName + DELIMITER
						+ String.format("end record binary=[%s]", BinaryUtil.listToString(record.getAlldata(), 1)));
				return true;
			} else {
				// 読込成功分をリストとして追加.
				record.setFilePosition(filePosition);
				setList.add(record);
				// 初回で先頭レコードセットしたので中間レコード扱い.
				filePosition = BinaryConstant.FILE_POSISION_MIDDLE;
				m_log.debug(methodName + DELIMITER
						+ String.format("continue reading. readedSize=%d, recordPosition=%s, monitorId=%s, file=[%s]",
								readedSize, record.getFilePosition(), bm.getM_wrapper().getId(),
								bm.getReadingStatus().getMonFileName()));
				continue;
			}
		}

	}
}
