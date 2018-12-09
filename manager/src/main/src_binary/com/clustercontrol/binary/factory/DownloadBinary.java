/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.factory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.clustercontrol.binary.bean.BinaryConstant;
import com.clustercontrol.binary.bean.BinaryQueryInfo;
import com.clustercontrol.binary.bean.BinaryTagConstant;
import com.clustercontrol.binary.model.BinaryCheckInfo;
import com.clustercontrol.binary.model.CollectBinaryData;
import com.clustercontrol.binary.model.CollectBinaryDataTag;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.BinaryRecordNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.hub.model.CollectStringDataPK;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.platform.HinemosPropertyDefault;
import com.clustercontrol.platform.QueryExecutor;
import com.clustercontrol.util.BinaryUtil;
import com.clustercontrol.util.FileUtil;

/**
 * バイナリファイルをDBから取得してclientにダウンロード.<br>
 * <br>
 * ※トランザクション制御は呼出元で実施.
 *
 * @version 6.1.0
 * @since 6.1.0
 */
public class DownloadBinary {

	/** ログ出力用インスタンス */
	private static Logger m_log = Logger.getLogger(DownloadBinary.class);
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	// 共通フィールド_コンストラクタ設定.
	BinaryQueryInfo queryInfo;

	// 共通フィールド_DBアクセス条件.
	/** 監視対象ファイル種別 */
	private Integer searchTimeout;

	// 共通フィールド_DB取得結果.
	/** 取得レコード */
	private CollectBinaryData outEntity;
	/** 監視対象ファイルパス */
	private String filePath;

	// 共通フィールド_一時ファイル出力関連.
	/** レコード時刻 */
	private String recordTimeStr;
	/** 一時出力先フォルダ(絶対パス) */
	private String dirName;
	/** 一時出力ファイル名(notパス) */
	private String fileName;
	/** 一時出力ファイルオブジェクト */
	private File tmpFile;

	// ファイル全体監視用フィールド.;
	/** ファイルデータIDリスト(1ファイルに紐づくレコード全量) */
	private List<CollectStringDataPK> pkList;

	// ファイル増分監視用フィールド.
	/** ファイル統合フラグ */
	private boolean addWrite;

	/**
	 * コンストラクタ.
	 * 
	 * @param queryInfo
	 *            バイナリ検索条件
	 */
	public DownloadBinary(BinaryQueryInfo queryInfo) {
		this.queryInfo = queryInfo;
	}

	/**
	 * クライアントから送られたキーに紐づくレコード取得.
	 * 
	 * @throws BinaryRecordNotFound
	 * @throws HinemosDbTimeout
	 * @throws HinemosUnknown
	 */
	public void getOneRecord(CollectStringDataPK primaryKey) throws BinaryRecordNotFound, HinemosDbTimeout, HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + String.format("start."));

		// 変数初期化.
		List<CollectBinaryData> outEntities = null;

		// 検索タイムアウト値を取得.
		this.searchTimeout = HinemosPropertyCommon.hub_binary_search_timeout.getIntegerValue();

		// 収集テーブルから引数をキーにタグ含めた該当レコード取得.
		try {
			// 収集蓄積バイナリデータ取得用クエリ生成.
			String queryStr = String.format("SELECT DISTINCT d " + "FROM CollectBinaryData d "
					+ "WHERE d.id.collectId = '%d' " + "AND d.id.dataId = '%d'", primaryKey.getCollectId(),
					primaryKey.getDataId());
			m_log.debug(methodName + DELIMITER + String.format("query data. queryStr=%s.", queryStr));

			// SQLパラメータセット.
			Map<String, Object> parameters = new HashMap<String, Object>();

			// DBアクセスして取得.
			outEntities = QueryExecutor.getListByJpqlWithTimeout(queryStr, CollectBinaryData.class, parameters,
					this.searchTimeout);

			// 取得なしエラー.
			if (outEntities == null || outEntities.isEmpty()) {
				BinaryRecordNotFound e = new BinaryRecordNotFound(
						String.format("the binary record not found. primaryKey=%s", primaryKey.toString()));
				m_log.info(methodName + DELIMITER + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			// 取得したレコードの内容をログ出力.
			StringBuilder sb = new StringBuilder();
			for (CollectBinaryData entity : outEntities) {
				sb.append(entity.toShortString());
			}
			m_log.debug(methodName + DELIMITER + String.format(
					"get the outEntity for binary record. count=%d, value=[%s]", outEntities.size(), sb.toString()));

			// 1件のみ取得想定なので先頭のみをセット.
			this.outEntity = outEntities.get(0);

			// タグの値取得.
			for (CollectBinaryDataTag tag : outEntity.getTagList()) {
				if (BinaryTagConstant.CommonTagName.FILE_NAME.equals(tag.getKey())) {
					this.filePath = tag.getValue();
					break;
				}
			}

			// ファイルパスが取得できなかった場合はログ出力してデフォルトファイル名をセット.
			if (this.filePath == null || this.filePath.isEmpty()) {
				this.filePath = "file";
				m_log.warn(methodName + DELIMITER
						+ String.format("get path is empty. primaryKey=%s", primaryKey.toString()));
			}

		} catch (BinaryRecordNotFound | HinemosDbTimeout e) {
			throw e;
		} catch (RuntimeException e) {
			m_log.warn(methodName + DELIMITER + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

	}

	/**
	 * 特定ファイルに紐づくレコード全量のデータIDリストを取得.
	 */
	public void getDataIds() throws HinemosUnknown, HinemosDbTimeout {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + String.format("start."));

		try {
			// 任意バイナリファイルの場合は同一ファイルのレコードのDataIDを全量取得.
			String key = this.outEntity.getFileKey();
			String fileIdQueryStr = String.format("SELECT DISTINCT d.id " + "FROM CollectBinaryData d "
					+ "WHERE d.fileKey='%s' " + "ORDER BY d.recordKey ASC", key);
			m_log.debug(methodName + DELIMITER
					+ String.format("query data to get data IDs of file. queryStr=%s.", fileIdQueryStr));

			// SQLパラメータセット.
			Map<String, Object> parameters = new HashMap<String, Object>();
			// DBアクセスして取得.
			this.pkList = QueryExecutor.getListByJpqlWithTimeout(fileIdQueryStr, CollectStringDataPK.class, parameters,
					this.searchTimeout);

			// 取得なしエラー.
			if (this.pkList == null || this.pkList.isEmpty()) {
				BinaryRecordNotFound e = new BinaryRecordNotFound(String.format(
						"the binary record not found for binary file. filePath=%s, fileKey=[%s]", this.filePath, key));
				m_log.info(methodName + DELIMITER + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}

			m_log.debug(methodName + DELIMITER
					+ String.format("get the primary key of a file. count=%d, fileKey=[%s]", this.pkList.size(), key));

		} catch (BinaryRecordNotFound e) {
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (RuntimeException e) {
			m_log.warn(methodName + DELIMITER + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

	}

	/**
	 * 一時出力先のファイル生成.
	 * 
	 * @param fileName
	 *            クライアント設定のファイル名(ユーザー設定).
	 * @param clientName
	 *            クライアント識別名(マネージャ一時ファイル出力先フォルダ作成用)
	 * 
	 * @throws HinemosUnknown
	 */
	public void createTemporaryFile(String clientFileName, String clientName) throws HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + String.format("start."));

		// 一時出力先のファイル名設定.
		if (clientFileName == null || clientFileName.isEmpty()) {
			String onlyFileName = FileUtil.getFileName(this.filePath);
			if (BinaryConstant.COLLECT_TYPE_WHOLE_FILE.equals(this.outEntity.getCollectType())) {
				// 任意バイナリファイルの場合：ファシリティID_監視ID_監視時刻_監視対象ファイル名
				String fileNameTime = FileUtil.fittingFileName(this.recordTimeStr, "-");
				this.fileName = this.queryInfo.getFacilityId() + "_" + this.queryInfo.getMonitorId() + "_"
						+ fileNameTime + "_" + onlyFileName;
			} else {
				// 増分データの場合(ファイル統合)：ファシリティID_監視ID_監視対象ファイル名.
				this.fileName = this.queryInfo.getFacilityId() + "_" + this.queryInfo.getMonitorId() + "_"
						+ onlyFileName;
			}
		} else {
			this.fileName = clientFileName;
		}

		// 一時出力先のディレクトリ生成.
		String binaryExportDir = HinemosPropertyDefault.binary_export_dir.getStringValue();
		File tmpDirectory = new File(binaryExportDir, clientName);
		this.dirName = tmpDirectory.getAbsolutePath();
		if (!tmpDirectory.exists()) {
			if (!tmpDirectory.mkdir()) {
				String errorMessage = String.format("failed to make temporary directory on manager. directory=[%s]",
						this.dirName);
				m_log.warn(methodName + DELIMITER + errorMessage);
				throw new HinemosUnknown(errorMessage);
			} else {
				m_log.debug(methodName + DELIMITER + String
						.format("successed to make temporary directory on manager. directory=[%s]", this.dirName));
			}
		}

		// 一時出力先のファイルオブジェクト生成.
		this.tmpFile = new File(this.dirName, this.fileName);
		this.addWrite = tmpFile.exists();
		m_log.debug(methodName + DELIMITER
				+ String.format("prepared to write binary. dirName=[%s], fileName=[%s], addWrite=%b", this.dirName,
						this.fileName, this.addWrite));

	}

	/**
	 * ファイル全体監視の一時ファイル出力.
	 * 
	 * @param isTop
	 *            先頭レコード判定フラグ(true:先頭レコード,false:2番目以降のレコード)
	 * @param dataId
	 *            データID
	 * @throws HinemosUnknown
	 * @throws HinemosDbTimeout
	 */
	public void outputTmpFileAll(boolean isTop, CollectStringDataPK pk) throws HinemosUnknown, HinemosDbTimeout {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + String.format("start."));

		try {
			// ファイル全体監視の場合は紐づくレコードを順次DBから取得して対象ファイルにひたすら追記.
			String queryStr = String.format("SELECT DISTINCT d " + "FROM CollectBinaryData d "
					+ "WHERE d.id.collectId='%d'" + " AND d.id.dataId='%d' " + "ORDER BY d.recordKey ASC",
					pk.getCollectId(), pk.getDataId());
			m_log.debug(methodName + DELIMITER + String.format("query data. queryStr=%s.", queryStr));

			// SQLパラメータセット.
			Map<String, Object> parameters = new HashMap<String, Object>();

			// DBアクセスして取得.
			List<CollectBinaryData> fileData = QueryExecutor.getListByJpqlWithTimeout(queryStr, CollectBinaryData.class,
					parameters, this.searchTimeout);
			m_log.debug(methodName + DELIMITER + String.format("get the file data. count=%d", fileData.size()));

			// IDキーに取得してるので1件のみ取得想定だが念のためループ.
			for (CollectBinaryData fileRecord : fileData) {
				// 取得したレコードを一時的にManagerのフォルダに出力(先頭レコード以外は追記モード出力).
				BinaryUtil.outputBinary(fileRecord.getValue(), this.tmpFile, !isTop);
				m_log.debug(methodName + DELIMITER
						+ String.format(
								"success write binary from a record to file. dirName=[%s], fileName=[%s], collectId=%d, dataId=%d, isTop=%b",
								this.dirName, this.fileName, pk.getCollectId(), pk.getDataId(), isTop));
			}
		} catch (HinemosDbTimeout e) {
			throw e;
		} catch (RuntimeException e) {
			m_log.warn(methodName + DELIMITER + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * ファイル増分監視の一時ファイル出力.<br>
	 * <br>
	 * 複数レコードを出力する場合は監視ファイルの単位で1ファイルに統合する.
	 * 
	 * @throws HinemosUnknown
	 */
	public void outputTmpFileRecord() throws HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + String.format("start."));

		// ファイル増分監視の場合は最初に取得したレコードを出力
		List<Byte> recordBinary = BinaryUtil.arrayToList(this.outEntity.getValue());
		if (this.addWrite) {
			// 追記の場合はファイルヘッダを除いたバイナリを連結.
			BinaryCheckInfo info;
			try {
				// ファイルヘッダサイズを取得できなかった場合は監視設定から取得.
				int fileheadSize = 0;
				if (this.outEntity.getFileHeadSize() == null) {
					info = QueryUtil.getBinaryCheckInfoPK(this.queryInfo.getMonitorId());
					if (info != null) {
						fileheadSize = BinaryUtil.longParseInt(info.getFileHeadSize());
					}
				} else {
					fileheadSize = BinaryUtil.longParseInt(this.outEntity.getFileHeadSize());
				}
				if (fileheadSize > 0 && fileheadSize < recordBinary.size()) {
					// ファイルヘッダを取り除く.
					recordBinary = recordBinary.subList(fileheadSize, recordBinary.size());
					m_log.debug(methodName + DELIMITER
							+ String.format("remove binary of file head to write binary. entity=[%s].",
									this.outEntity.toShortString()));
				} else if (fileheadSize > 0) {
					// ファイルヘッダしかないレコードは不正データとしてエラーthrow.
					String errorMessage = String.format(
							"failed to connect as file binary because record have only file header.  entity=[%s].",
							this.outEntity.toShortString());
					m_log.warn(methodName + DELIMITER + errorMessage);
					throw new HinemosUnknown(errorMessage);
				} else {
					// ファイルヘッダないのでそのままファイル出力.
					m_log.debug(methodName + DELIMITER
							+ String.format(
									"prepared to write record binary without file header as file binary. entity=[%s].",
									this.outEntity.toShortString()));
				}
			} catch (MonitorNotFound e) {
				m_log.warn(e.getMessage(), e);
				throw new HinemosUnknown(e.getMessage(), e);
			}
		} else {
			m_log.debug(methodName + DELIMITER
					+ String.format("prepared to write binary from a record as file binary. entity=[%s].",
							this.outEntity.toShortString()));
		}
		// 取得したレコードを一時的にManagerのフォルダに出力.
		BinaryUtil.outputBinary(recordBinary, this.tmpFile, this.addWrite);
		m_log.debug(methodName + DELIMITER
				+ String.format("success write binary from a record. dirName=[%s], fileName=[%s], addWrite=%b",
						this.dirName, this.fileName, this.addWrite));
	}

	// 以下getter.
	/** 監視対象ファイル種別 */
	public String getCollectType() {
		return this.outEntity.getCollectType();
	}

	/** ファイル統合フラグ */
	public boolean isAddWrite() {
		return this.addWrite;
	}

	/** ファイルデータIDリスト(1ファイルに紐づくレコード全量) */
	public List<CollectStringDataPK> getPkList() {
		return this.pkList;
	}

	/** 一時出力先フォルダ */
	public String getDirName() {
		return this.dirName;
	}

	/** 一時出力ファイル名 */
	public String getFileName() {
		return this.fileName;
	}

	/** 一時出力ファイルオブジェクト */
	public File getTmpFile() {
		return this.tmpFile;
	}

}
