/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.binary.factory;

import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.binary.readingstatus.FileReadingStatus;
import com.clustercontrol.agent.binary.result.BinaryRecord;
import com.clustercontrol.binary.bean.BinaryConstant;
import com.clustercontrol.util.BinaryUtil;
import com.clustercontrol.util.FileUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.ws.monitor.BinaryCheckInfo;

public class BinarySeparator {

	// クラス共通フィールド.
	/** ロガー */
	private static Log log = LogFactory.getLog(BinarySeparator.class);

	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	// レコード区切り位置調整用フィールド
	/** 読込開始位置より後ろの半端レコード */
	private boolean firstRecord = true;
	/** 読込開始位置より後ろのレコード先頭位置 */
	private long firstReadPosition = 0;

	/** レコード分割の際スキップしたレコードのサイズ */
	private int skippedSize = 0;

	/**
	 * バイナリデータ固定長レコード分割.<br>
	 * <br>
	 * レコード固定長のバイナリデータをタイムスタンプ毎もしくは指定サイズで分割する.
	 * 
	 * @param readedBinary
	 *            分割対象のバイナリデータ
	 * @param binaryInfo
	 *            監視設定の内バイナリ監視に関する情報<br>
	 * @param byTimeStmp
	 *            タイムスタンプ毎で区切る場合はtrue<br>
	 *            <br>
	 * @return レコード毎のタイムスタンプをキーにして格納したマップ.<br>
	 *         設定不正の場合は引数のリストをそのまま格納して返却.
	 * 
	 */
	public List<BinaryRecord> separateFixed(String monitorId, List<Byte> readedBinary, BinaryCheckInfo binaryInfo,
			FileReadingStatus fileRs) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		List<BinaryRecord> returnList = new ArrayList<BinaryRecord>();
		BinaryRecord tmpBinRecord = new BinaryRecord();
		this.skippedSize = 0;

		// 分割前チェック.
		if (binaryInfo.isHaveTs()) {
			// タイムスタンプ毎の場合に必要な設定値.
			if (binaryInfo.getTsPosition() <= 0 || binaryInfo.getTsType() == null || binaryInfo.getTsType().isEmpty()) {
				String tsType = binaryInfo.getTsType();
				if (tsType == null) {
					tsType = "null";
				}
				log.warn(methodName + DELIMITER
						+ String.format(
								"skipped to separate because of invalid arguments."
										+ "monitorID=%s, lengthType=%s, haveTs=%b TsPosition=%d, TsType=%s",
								monitorId, binaryInfo.getLengthType(), binaryInfo.isHaveTs(),
								binaryInfo.getTsPosition(), tsType));
				tmpBinRecord.setAlldata(readedBinary);
				returnList.add(tmpBinRecord);
				return returnList;
			}
		}

		// 固定長で区切る場合に必要な設定値.
		if (binaryInfo.getRecordSize() <= 0) {
			log.warn(methodName + DELIMITER
					+ String.format(
							"skipped to separate because of invalid arguments."
									+ "monitorID=%s, lengthType=%s, recordSize=%d",
							monitorId, binaryInfo.getLengthType(), binaryInfo.getRecordSize()));
			tmpBinRecord.setAlldata(readedBinary);
			returnList.add(tmpBinRecord);
			return returnList;
		}

		// 変数初期化.
		List<Byte> tmpList = new ArrayList<Byte>();
		List<Byte> timeStampByte = new ArrayList<Byte>();
		int tsSize = 0;
		if (binaryInfo.isHaveTs()) {
			tsSize = BinaryConstant.TIMESTAMP_BYTE_MAP.get(binaryInfo.getTsType()).intValue();
		}
		int inRecordByte = 0;
		Timestamp ts = null;
		String sequential = "";
		int count = 1;
		int keta = String.valueOf(Integer.MAX_VALUE).length();
		boolean setTs = false;

		// 分割処理.
		for (int i = 0; i < readedBinary.size(); i++) {
			inRecordByte++;

			if (binaryInfo.isHaveTs()) {
				// タイムスタンプ毎の場合はタイムスタンプ取得.
				if (inRecordByte >= binaryInfo.getTsPosition()
						&& inRecordByte < (binaryInfo.getTsPosition() + tsSize)) {
					// タイムスタンプとして追加.
					timeStampByte.add(readedBinary.get(i));
				}
				if (inRecordByte == (binaryInfo.getTsPosition() + tsSize - 1)) {
					// タイムスタンプをバイナリ⇒Timestamp型に変換.
					ts = this.binaryToTs(timeStampByte, binaryInfo.getTsType(), binaryInfo.isLittleEndian());
					tmpBinRecord.setTs(ts);
					// タイムスタンプ毎の場合はキーにファイル名＋タイムスタンプ＋連番を付与.
					sequential = FileUtil.paddingZero(count, keta);
					tmpBinRecord.setKey(fileRs.getMonFileName() + ts.toString() + sequential);
					setTs = true;
				}
			}

			tmpList.add(readedBinary.get(i));

			if (inRecordByte == binaryInfo.getRecordSize()) {
				if (!binaryInfo.isHaveTs()) {
					// ただの固定長の場合はレコードのキーにファイル名＋連番を付与.
					sequential = FileUtil.paddingZero(count, keta);
					tmpBinRecord.setKey(fileRs.getMonFileName() + sequential);
				}
				if (binaryInfo.isHaveTs() && !setTs) {
					// 設定値不正等でタイムスタンプが設定できてない場合は現在時刻をセット.
					ts = new Timestamp(HinemosTime.currentTimeMillis());
					tmpBinRecord.setTs(ts);
					// タイムスタンプ毎の場合はキーにファイル名＋タイムスタンプ＋連番を付与.
					sequential = FileUtil.paddingZero(count, keta);
					tmpBinRecord.setKey(fileRs.getMonFileName() + ts.toString() + sequential);
					// ログ出力しとく.
					log.info(methodName + DELIMITER
							+ String.format(
									"failed to get time stamp from binary by a record."
											+ " monitorId=%s, file=[%s], setTime(current)=%s",
									monitorId, fileRs.getMonFileName(), ts.toString()));
					setTs = false;
				}
				// レコードサイズとレコード内容をセット.
				tmpBinRecord.setFilePosition(BinaryConstant.FILE_POSISION_END);
				tmpBinRecord.setSequential(sequential);
				tmpBinRecord.setSize(binaryInfo.getRecordSize());
				tmpBinRecord.setAlldata(tmpList);

				// レコード末尾なのでレコードスキップするか判定の上追加.
				if (this.checkAddRecord(i, monitorId, fileRs, tmpBinRecord.getSize() )) {
					returnList.add(tmpBinRecord);
				}

				// 次のレコードを取得するため変数を初期化.
				tmpList = new ArrayList<Byte>();
				tmpBinRecord = new BinaryRecord();
				inRecordByte = 0;
				timeStampByte = new ArrayList<Byte>();
				sequential = "";
				count++;
				continue;
			}
		}

		log.debug(methodName + String.format(" separated by a fixed length. record count=%d", returnList.size()));
		return returnList;
	}

	/**
	 * バイナリデータ可変長レコード分割(タイムスタンプ毎).<br>
	 * <br>
	 * レコード可変長のバイナリデータをタイムスタンプ毎で分割する.
	 * 
	 * @param readedBinary
	 *            分割対象のバイナリデータ
	 * @param binaryInfo
	 *            監視設定の内バイナリ監視に関する情報<br>
	 * @param byTimeStmp
	 *            タイムスタンプ毎で切る場合はtrue<br>
	 *            <br>
	 * @return レコード毎のタイムスタンプをキーにして格納したマップ
	 */
	public List<BinaryRecord> separateVariable(String monitorId, List<Byte> readedBinary, BinaryCheckInfo binaryInfo,
			FileReadingStatus fileRs) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		List<BinaryRecord> returnList = new ArrayList<BinaryRecord>();
		BinaryRecord tmpBinRecord = new BinaryRecord();
		this.skippedSize = 0;

		// 分割前チェック.
		if (binaryInfo.isHaveTs()) {
			// タイムスタンプ毎の場合に必要な設定値.
			if (binaryInfo.getTsPosition() <= 0 || binaryInfo.getTsType() == null || binaryInfo.getTsType().isEmpty()) {
				String tsType = binaryInfo.getTsType();
				if (tsType == null) {
					tsType = "null";
				}
				log.warn(methodName + DELIMITER
						+ String.format(
								"skipped to separate because of invalid arguments."
										+ " lengthType=%s, haveTs=%b TsPosition=%d, TsType=%s",
								binaryInfo.getLengthType(), binaryInfo.isHaveTs(), binaryInfo.getTsPosition(), tsType));
				tmpBinRecord.setAlldata(readedBinary);
				returnList.add(tmpBinRecord);
				return returnList;
			}
		}

		// 可変長区切りの場合に必要な設定値.
		if (binaryInfo.getRecordHeadSize() <= 0 || binaryInfo.getSizePosition() <= 0
				|| binaryInfo.getSizeLength() <= 0) {
			// バイナリ監視の設定値不正のため分割しない.
			log.warn(methodName + DELIMITER
					+ String.format(
							"skipped to separate because of invalid arguments."
									+ " lengthType=%s, recordHeadSize=%d, sizePosition=%d, sizeLength=%d",
							binaryInfo.getLengthType(), binaryInfo.getRecordHeadSize(), binaryInfo.getSizePosition(),
							binaryInfo.getSizeLength()));
			tmpBinRecord.setAlldata(readedBinary);
			returnList.add(tmpBinRecord);
			return returnList;
		}

		// 分割処理の変数初期化.
		List<Byte> tmpList = new ArrayList<Byte>();
		List<Byte> timeStampByte = new ArrayList<Byte>();
		List<Byte> recordSizeByte = new ArrayList<Byte>();
		int tsSize = 0;
		if (binaryInfo.isHaveTs()) {
			tsSize = BinaryConstant.TIMESTAMP_BYTE_MAP.get(binaryInfo.getTsType()).intValue();
		}
		int inRecordByte = 0;
		int recordSize = 0;
		Timestamp ts = null;

		String sequential = "";
		int count = 1;
		int keta = String.valueOf(Integer.MAX_VALUE).length();
		boolean setTs = false;

		// 分割処理.
		for (int i = 0; i < readedBinary.size(); i++) {
			inRecordByte++;
			if (inRecordByte >= binaryInfo.getSizePosition()
					&& inRecordByte < (binaryInfo.getSizePosition() + binaryInfo.getSizeLength())) {
				// レコードサイズをあらわすバイトを取得.
				recordSizeByte.add(readedBinary.get(i));
			}
			if (inRecordByte == (binaryInfo.getSizePosition() + binaryInfo.getSizeLength() - 1)) {
				// レコードサイズをあらわすバイトの末尾なのでバイトサイズ取得.
				if (binaryInfo.isLittleEndian()) {
					// 逆順で格納されているバイナリの場合は逆順に修正.
					Collections.reverse(recordSizeByte);
				}
				// ByteBufferでint値変換するために4byte必要.
				recordSizeByte = BinaryUtil.paddingByteList(recordSizeByte, Byte.valueOf((byte) 0), 4, false);
				recordSize = ByteBuffer.wrap(BinaryUtil.listToArray(recordSizeByte)).getInt();
				if( log.isTraceEnabled() ){
					log.trace(methodName + DELIMITER
							+ String.format(
									"  getRecordSize ."
											+ " recordSize=%s  recordSizeByte=%s ",
											recordSize,recordSizeByte));
				}
				tmpBinRecord.setSize(recordSize);
			}

			if (binaryInfo.isHaveTs()) {
				// タイムスタンプ区切りの場合はバイトからタイムスタンプ取得.
				if (inRecordByte >= binaryInfo.getTsPosition()
						&& inRecordByte < (binaryInfo.getTsPosition() + tsSize)) {
					// タイムスタンプとして追加.
					timeStampByte.add(readedBinary.get(i));
				}
				if (inRecordByte == (binaryInfo.getTsPosition() + tsSize - 1)) {
					// タイムスタンプをバイナリ⇒Timestamp型に変換.
					ts = binaryToTs(timeStampByte, binaryInfo.getTsType(), binaryInfo.isLittleEndian());
					tmpBinRecord.setTs(ts);
					// タイムスタンプ毎の場合はキーにファイル名＋タイムスタンプ＋連番を付与.
					sequential = FileUtil.paddingZero(count, keta);
					tmpBinRecord.setKey(fileRs.getMonFileName() + ts.toString() + sequential);
					setTs = true;
					if( log.isTraceEnabled() ){
						log.trace(methodName + DELIMITER
								+ String.format(
										"  getTimeStamp ."
												+ "ts=%s  timeStampByte=%s  setTs=%b ",
												ts,timeStampByte,setTs));
					}
				}
			}

			tmpList.add(readedBinary.get(i));

			if (inRecordByte == (binaryInfo.getRecordHeadSize() + recordSize)) {
				if (!binaryInfo.isHaveTs()) {
					// ただの固定長の場合はレコードのキーにファイル名と連番を付与.
					sequential = FileUtil.paddingZero(count, keta);
					tmpBinRecord.setKey(fileRs.getMonFileName() + sequential);
				}
				if (binaryInfo.isHaveTs() && !setTs) {
					// 設定値不正等でタイムスタンプが設定できてない場合は現在時刻をセット.
					ts = new Timestamp(HinemosTime.currentTimeMillis());
					tmpBinRecord.setTs(ts);
					// タイムスタンプ毎の場合はキーにファイル名＋タイムスタンプ＋連番を付与.
					sequential = FileUtil.paddingZero(count, keta);
					tmpBinRecord.setKey(fileRs.getMonFileName() + ts.toString() + sequential);
					// ログ出力しとく.
					log.info(methodName + DELIMITER
							+ String.format(
									"failed to get time stamp from binary by a record."
											+ " monitorId=%s, file=[%s], setTime(current)=%s",
									monitorId, fileRs.getMonFileName(), ts.toString()));
					setTs = false;
				}

				// 現在のバイト = ヘッダサイズ＋レコードサイズ ⇒ レコード末尾のバイトなのでリストに追加.
				tmpBinRecord.setFilePosition(BinaryConstant.FILE_POSISION_END);
				tmpBinRecord.setSequential(sequential);
				tmpBinRecord.setAlldata(tmpList);

				// レコード末尾なのでレコードスキップするか判定の上追加.
				if (this.checkAddRecord(i, monitorId, fileRs, (tmpBinRecord.getSize()+ binaryInfo.getRecordHeadSize()))){
					returnList.add(tmpBinRecord);
				}

				// 変数初期化.
				tmpList = new ArrayList<Byte>();
				tmpBinRecord = new BinaryRecord();
				inRecordByte = 0;
				timeStampByte = new ArrayList<Byte>();
				recordSizeByte = new ArrayList<Byte>();
				recordSize = 0;
				count++;
				continue;
			}
		}

		return returnList;
	}

	/**
	 * バイナリをタイムスタンプ変換.<br>
	 * <br>
	 * バイナリ⇒long値⇒Timestampクラスに変換.<br>
	 * 
	 * @param timeStamp
	 *            読込済バイナリデータ
	 * @param tsType
	 *            タイムスタンプ種類
	 * @param revertFlg
	 *            バイナリ格納逆順フラグ <br>
	 *            例)16進数表記00 05 08に対して、実際に格納されているバイト配列が08 05 00の場合、true <br>
	 *            <br>
	 * @return 読込バイナリデータのサイズ
	 */
	private Timestamp binaryToTs(List<Byte> timeStamp, String tsType, boolean revertFlg) {

		long tsLong = binaryToTsLong(timeStamp, tsType, revertFlg);
		Timestamp ts = new Timestamp(tsLong);

		return ts;
	}

	/**
	 * バイナリをタイムスタンプlong値変換.<br>
	 * <br>
	 * バイナリをタイムスタンプ種別にあわせてTimestamp型に変換可能なlong値に変換.<br>
	 * ※Timestamp型のコンストラクタで指定するlong値は<br>
	 * グリニッジ標準時(1970/1/1 0:0:0)を起点としたミリ秒.<br>
	 * 
	 * @param timeStamp
	 *            読込済バイナリデータ
	 * @param tsType
	 *            タイムスタンプ種類
	 * @param revertFlg
	 *            バイナリ格納逆順フラグ <br>
	 *            例)16進数表記00 05 08に対して、実際に格納されているバイト配列が08 05 00の場合、true <br>
	 *            <br>
	 * @return 読込バイナリデータのサイズ
	 */
	private long binaryToTsLong(List<Byte> timeStamp, String tsType, boolean revertFlg) {
		long tsLong = 0L;

		List<Byte> secBinary = new ArrayList<Byte>(timeStamp);
		if (BinaryConstant.TS_TYPE_SEC_AND_USEC.equals(tsType)) {
			// 協定世界時からの経過秒＋マイクロ秒の場合.

			// 経過秒を格納.
			secBinary = secBinary.subList(0, 4);
			if (revertFlg) {
				Collections.reverse(secBinary);
			}
			// ByteBufferでlong値変換するために8byte必要.
			secBinary = BinaryUtil.paddingByteList(secBinary, Byte.valueOf((byte) 0), 8, false);
			byte[] secBinaryArray = BinaryUtil.listToArray(secBinary);
			tsLong = ByteBuffer.wrap(secBinaryArray).getLong() * 1000;

			// マイクロ秒を格納.
			List<Byte> micBinary = new ArrayList<Byte>(timeStamp);
			micBinary = micBinary.subList(4, 8);
			if (revertFlg) {
				Collections.reverse(micBinary);
			}
			// ByteBufferでlong値変換するために8byte必要.
			micBinary = BinaryUtil.paddingByteList(micBinary, Byte.valueOf((byte) 0), 8, false);
			byte[] micBinaryArray = BinaryUtil.listToArray(micBinary);
			// TimeStampクラスで扱うのはミリ秒までなので小数点以下切捨て.
			tsLong = tsLong + (ByteBuffer.wrap(micBinaryArray).getLong() / 1000);

		} else if (BinaryConstant.TS_TYPE_ONLY_SEC.equals(tsType)) {
			// 経過秒を格納.
			secBinary = new ArrayList<Byte>(timeStamp);
			if (revertFlg) {
				Collections.reverse(secBinary);
			}
			// ByteBufferでlong値変換するために8byte必要.
			secBinary = BinaryUtil.paddingByteList(secBinary, Byte.valueOf((byte) 0), 8, false);
			byte[] secBinaryArray = BinaryUtil.listToArray(secBinary);
			tsLong = ByteBuffer.wrap(secBinaryArray).getLong() * 1000;
		} else {
			// TimeStampの種別が不明なのでとりあえず渡された引数をEpochからのミリ秒とみなしてlong変換.
			List<Byte> msBinary = BinaryUtil.paddingByteList(timeStamp, Byte.valueOf((byte) 0), 8, false);
			byte[] msBinaryArray = BinaryUtil.listToArray(msBinary);
			tsLong = ByteBuffer.wrap(msBinaryArray).getLong();
		}

		return tsLong;
	}

	/**
	 * レコード追加判定.
	 * 
	 * @param index
	 *            判定対象レコードの取得完了時のバイナリ配列内インデックス
	 * @param monitorId
	 *            監視ID
	 * @param fileRs
	 *            ファイルの読込みステータス
	 * @param curRecSize
	 *            判定対象レコードのデータサイズ（レコードヘッダ含む）※可変長の場合は注意
	 * @return true:追加する、false:追加スキップ
	 */
	private boolean checkAddRecord(int index, String monitorId, FileReadingStatus fileRs,int curRecSize) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		// スキップ判定.
		// skipSizeと比較
		if (fileRs.isToSkipRecord()) {
			if ((index + 1) <= fileRs.getSkipSize()) {
				// スキップサイズより前のインデックスなので飛ばす.
				if( log.isTraceEnabled() ){
					log.trace(methodName + DELIMITER
							+ String.format(
									"  isToSkipRecord ."
											+ " index=%d  fileRs.getSkipSize()=%d ",
											index,fileRs.getSkipSize()));
				}
				// スキップ済のレコードサイズを記録
				this.skippedSize = index + 1;
				return false;
			}
			if (this.firstRecord) {
				// Manager送信後に、次回監視用にファイルRS更新(updateFileRSで実施)する際の計算用にセット.
				//（飛ばしたサイズを今回の判定を加味して再セット。
				// スキップサイズより大きいはじめてのレコードは半端サイズチェックを行い、該当( レコード開始位置が読み飛ばしの範囲 )なら飛ばす.
				this.firstRecord = false;
				//ファイルの読込み開始位置（fileRs.getPosition() ）＋ 読み進めた長さ － このレコードの長さ＝ このレコードの開始位置
				long nowRecStartPos = fileRs.getPosition() + index + 1 - curRecSize;
				if ( nowRecStartPos < ( fileRs.getSkipSize() + fileRs.getPosition()) ){
					if( log.isTraceEnabled() ){
						log.trace(methodName + DELIMITER
								+ String.format(
										"skipped records. first record is  fragment data "
												+ " monitorId=%s, file=[%s], fileRs.getPosition()=%d, nowRecStartPos=%d, getSkipSize=%d",
												monitorId, fileRs.getMonFileName(),  fileRs.getPosition() ,nowRecStartPos ,fileRs.getSkipSize()));
					}
					// 半端なサイズの場合、このデータを含むレコードまでをスキップ済とする(skipSizeより大きくなる)
					this.skippedSize = index + 1;
					this.firstReadPosition = index + 2; // 次レコードの先頭位置(ログ出力用).
					return false;
				}else{
					this.firstReadPosition = fileRs.getPosition() + index + 1; // 次レコードの先頭位置(ログ出力用).
				}
			}
			// １件目で半端サイズでない場合 と ２件目以降読込みの対象とする。
			log.info(methodName + DELIMITER
					+ String.format(
							"skipped records."
									+ " monitorId=%s, file=[%s], skipSize(*)=%d, readPosition(*)=%d, *exclude fileHeader",
							monitorId, fileRs.getMonFileName(), fileRs.getSkipSize(), this.firstReadPosition));
			// Manager送信後に、次回監視用にファイルRS更新する際の計算用にセット.
			fileRs.setToSkipRecord(false);
		}
		// 送信用レコード追加.
		return true;
	}

	/**
	 * 読込バイナリデータ長取得.<br>
	 * 
	 * @param readedMap
	 *            読込済バイナリデータ
	 * @return 読込バイナリデータのサイズ
	 */
	public long getReadedSize(List<BinaryRecord> readedList) {
		long readedSize = 0;

		if (readedList == null || readedList.isEmpty()) {
			return readedSize;
		}

		for (BinaryRecord record : readedList) {
			readedSize = readedSize + record.getAlldata().size();
		}

		return readedSize;
	}

	/** レコード分割の際スキップしたレコードのサイズ*/
	public int getSkippedSize(){
		return this.skippedSize;
	}

	/**
	 * wtmpファイルのレコード統合.<br>
	 * <br>
	 * 固定長ブロックでレコード分割したwtmpファイルを同一ttyでレコード統合.<br>
	 * ※lastコマンド打ち込んだ時にファイルとして機能する単位に統合する.<br>
	 * ※厳密には複数レコード含むことになるため、タイムスタンプは先頭レコードの情報持たせる<br>
	 * 
	 * @param sendData
	 *            同一ttyで統合したレコード.
	 * @return
	 */
	public List<BinaryRecord> coordinateWtmp(List<BinaryRecord> sendData) {
		// 変数初期化.
		Map<String, BinaryRecord> returnMap = new TreeMap<String, BinaryRecord>();
		List<BinaryRecord> ttyList = new ArrayList<BinaryRecord>();
		List<Byte> ttyBinary = null;
		BinaryRecord ttyTmp = null;
		boolean exist = false;
		BinaryRecord addRecord = null;

		// ttyのバイナリ取得とttyリスト作成.
		for (BinaryRecord record : sendData) {
			// ttyのバイナリ取得.
			ttyBinary = new ArrayList<Byte>(record.getAlldata());
			ttyBinary = ttyBinary.subList(8, 40);

			if (ttyList.isEmpty()) {
				// とりあえず初回はセット.
				ttyTmp = new BinaryRecord();
				ttyTmp.setAlldata(ttyBinary);
				ttyTmp.setKey(record.getKey());
				ttyList.add(ttyTmp);
				returnMap.put(record.getKey(), record);
				continue;
			} else {

				// tty存在チェックフラグ初期化.
				exist = false;

				// 先に取り出したレコードの中に同一ttyが存在するかチェック.
				for (BinaryRecord ttyBinaryRecord : ttyList) {
					if (BinaryUtil.equals(ttyBinaryRecord.getAlldata(), ttyBinary)) {
						// 存在した場合は一致したttyのkey元に既存レコードにバイナリ追加.
						addRecord = returnMap.get(ttyBinaryRecord.getKey());
						addRecord.getAlldata().addAll(record.getAlldata());
						returnMap.put(addRecord.getKey(), addRecord);
						// 存在した場合はフラグオンにしてループ終了.
						exist = true;
						break;
					}
				}

				if (!exist) {
					// 存在しなかった場合は新たなレコードとして追加.
					ttyTmp = new BinaryRecord();
					ttyTmp.setAlldata(ttyBinary);
					ttyTmp.setKey(record.getKey());
					ttyList.add(ttyTmp);
					returnMap.put(record.getKey(), record);
				}

			}
		}

		// マップをリスト変換.
		List<BinaryRecord> returnList = new ArrayList<BinaryRecord>();
		for (BinaryRecord record : returnMap.values()) {
			returnList.add(record);
		}

		return returnList;
	}

}
