/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.binary.packet;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapDumper;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.packet.Packet;

import com.clustercontrol.agent.binary.BinaryMonitorConfig;
import com.clustercontrol.agent.log.MonitorInfoWrapper;
import com.clustercontrol.util.FileUtil;
import com.clustercontrol.util.HinemosTime;

/**
 * パケットキャプチャリスナー実装クラス.<br>
 * 
 * @since 6.1.0
 * @version 6.1.0
 */
public class PacketListenerImpl implements PacketListener {

	// ログ出力関連
	/** ロガー */
	private static Log m_log = LogFactory.getLog(PacketListenerImpl.class);
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	// 他static.
	private static final String EXTENSION = ".pcap";

	// パケットキャプチャ用オブジェクト.
	/** パケットキャプチャ制御用オブジェクト */
	private PcapHandle handle;
	/** pcapダンプ用オブジェクト */
	private PcapDumper dumper;

	// 出力先ファイル関連
	/** 出力先ディレクトリ */
	private String dirPath;
	/** 出力先ファイル最大サイズ */
	private long dumpSize;

	/** 出力先ファイル名称の先頭文字列 */
	private String fileNamePrefix;
	/** 出力先ファイル名称用のタイムスタンプ(サイズ区切り) */
	private String fileNameTime;

	// スレッド処理で利用.
	/** 監視設定. */
	private MonitorInfoWrapper monInfo;
	/** 紐づきアドレス */
	private String address;

	/**
	 * コンストラクタ
	 */
	public PacketListenerImpl(MonitorInfoWrapper monInfo, PcapHandle handle, String dirPath, long dumpSize,
			String address) {
		this.monInfo = monInfo;
		this.handle = handle;
		this.dirPath = dirPath;
		this.dumpSize = dumpSize;
		this.address = address;

		// 出力先ファイルの名称で利用する文字列初期化.
		String fileNameIp = FileUtil.fittingFileName(address, "-");
		this.fileNamePrefix = monInfo.getId() + "_" + fileNameIp + "_";
		m_log.debug(
				"constructor PacketListenerImpl" + DELIMITER + "set fileNamePrefix = [" + this.fileNamePrefix + "]");
		this.setFileNameTime();
		m_log.debug("constructor PacketListenerImpl" + DELIMITER + "set fileNameTime = [" + this.fileNameTime + "]");
	}

	/**
	 * クローズ
	 */
	public void close() {
		this.dumper.close();
		this.handle.close();
		this.deleteDumps();
	}

	/**
	 * パケットキャプチャ処理.
	 */
	@Override
	public void gotPacket(Packet packet) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		// 出力先ファイルのオブジェクト生成.
		File file = this.getNewFile();

		// 出力ファイルの生成とpacketのdump出力.
		try {
			boolean created = true;
			if (!file.exists()) {
				// ファイルが存在しない場合は生成.
				created = this.createNewDumpFile(file);
				m_log.debug(methodName + DELIMITER + "create pcap file because it isn't exist. file = ["
						+ file.getAbsolutePath() + "]");
			} else {
				// 存在するが取得したパケット長を追加すると最大サイズを超える場合も生成.
				long nextSize = file.length() + packet.length();
				if (nextSize > dumpSize) {
					this.setFileNameTime();
					file = this.getNewFile();
					created = this.createNewDumpFile(file);
					m_log.debug(methodName + DELIMITER + "create pcap file because it is over max size. file = ["
							+ file.getAbsolutePath() + "]");
				}
			}
			if (!created) {
				// 生成処理失敗した場合は終了.
				return;
			}

			// ダンプ用オブジェクト生成.
			if (this.dumper == null) {
				this.dumper = handle.dumpOpen(file.getAbsolutePath());
				m_log.debug(methodName + DELIMITER + "dumper open=" + dumper.isOpen());
			}
			this.dumper.dump(packet, handle.getTimestamp());

		} catch (NotOpenException e) {
			// PcapHandleがクローズされている場合.
			m_log.warn(methodName + DELIMITER + "closed the handle error.", e);
		} catch (PcapNativeException e) {
			// WinPcap/LibPcapの未インストール等.
			m_log.warn(methodName + DELIMITER + "native exception of pcap.", e);
		}
	}

	/**
	 * 現在時刻を取得してファイル名にあわせて変換.<br>
	 * <br>
	 * yyyy-mm-dd-hh-mm-ss-fffffffff
	 * 
	 */
	private void setFileNameTime() {
		String currentTime = new Timestamp(HinemosTime.currentTimeMillis()).toString();
		this.fileNameTime = FileUtil.fittingFileName(currentTime, "-");
	}

	/**
	 * 新規ファイルオブジェクト取得.<br>
	 * <br>
	 * ファイル名のフォーマット変更する場合はdeleteDumps()メソッドもあわせて修正すること.
	 */
	private File getNewFile() {
		File file = new File(this.dirPath, this.fileNamePrefix + this.fileNameTime + EXTENSION);
		return file;
	}

	/**
	 * ダンプ用のファイル新規生成.
	 */
	private boolean createNewDumpFile(File file) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		// dumper初期化.
		this.clearDumper();

		// 保存期間過ぎたdumpファイルを削除する.
		this.deleteDumps();

		// ファイル生成.
		try {
			if (!file.createNewFile()) {
				m_log.warn(methodName + DELIMITER + "failed to create pcap file = [" + file.getAbsolutePath() + "]");
				return false;
			}
		} catch (IOException e) {
			m_log.warn(methodName + DELIMITER + "failure of making" + file.getPath(), e);
			return false;
		}
		return true;
	}

	/**
	 * dumperを初期化.
	 */
	private void clearDumper() {
		if (this.dumper != null) {
			this.dumper.close();
			this.dumper = null;
		}
	}

	/**
	 * 保存期間過ぎたdumpファイルを削除する.
	 */
	private void deleteDumps() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		File directory = new File(this.dirPath);
		m_log.debug(methodName + DELIMITER + String.format("start delete dumps. directory=[%s]", this.dirPath));

		// dump出力先ディレクトリからファイル全量取得する.
		File[] files = directory.listFiles();

		// 取得なしの場合は処理終了.
		if (files == null || files.length <= 0) {
			m_log.debug(methodName + DELIMITER + "skip to delete dumps because dump file isn't exist.");
			return;
		}
		m_log.debug(methodName + DELIMITER + String.format(
				"prepared to delete dumps. fileNamePrefix=[%s], size(check dumps)=%d", fileNamePrefix, files.length));

		// リスナーに紐づくダンプファイルが保存期間を過ぎていないかチェック.
		long currentTime = HinemosTime.currentTimeMillis();
		for (File dump : files) {
			// 存在しない場合はスキップ.
			String name = dump.getName();
			if (!dump.exists()) {
				m_log.debug(methodName + DELIMITER + String.format("skip to delete dump file. file=[%s]", name));
				continue;
			}

			// ディレクトリは対象外.
			if (dump.isDirectory()) {
				m_log.debug(methodName + DELIMITER
						+ String.format("skip to delete dump file because it's directory. name=[%s]", name));
				continue;
			}

			// 指定の拡張子でない場合は対象外.
			if (!name.endsWith(EXTENSION)) {
				m_log.debug(methodName + DELIMITER
						+ String.format(
								"skip to delete dump file through filter of extension."
										+ " name=[%s], fileNamePrefix=[%s], EXTENSION=[%s]",
								name, fileNamePrefix, EXTENSION));
				continue;
			}

			// ファイル変更なしのミリ秒取得.
			long lastModified = dump.lastModified();
			long unchangedPeriod = BinaryMonitorConfig.getUnchangedStatsPeriod();
			long unchangedTimeMillis = currentTime - lastModified;
			String lastmodifiedTime = null;
			String currentTimeStr = null;
			// 変更なし許容期間を超えてないので削除しない.
			if (unchangedTimeMillis <= unchangedPeriod) {
				if (m_log.isDebugEnabled()) {
					lastmodifiedTime = new Timestamp(lastModified).toString();
					currentTimeStr = new Timestamp(currentTime).toString();
					m_log.debug(methodName + DELIMITER
							+ String.format(
									"skip to delete dump file because file may be changing."
											+ " file=[%s], lastModified=%s, currentTime=%s, unchangedTimeMillis=%d, unchangedPeriod=%d",
									name, lastmodifiedTime, currentTimeStr, unchangedTimeMillis, unchangedPeriod));
				}
				continue;
			}

			// 保存期間をプロパティから取得.
			long storagePeriod = BinaryMonitorConfig.getDumpStoragePeriod();
			long storagePeriodMillis = storagePeriod * 60 * 60 * 1000;
			long maxTimeMillis = currentTime - storagePeriodMillis;
			// 保存期間内なので削除しない.
			if (lastModified >= maxTimeMillis) {
				if (m_log.isDebugEnabled()) {
					lastmodifiedTime = new Timestamp(lastModified).toString();
					currentTimeStr = new Timestamp(currentTime).toString();
					String maxTime = new Timestamp(maxTimeMillis).toString();
					m_log.debug(methodName + DELIMITER + String.format(
							"skip to delete dump file because file is in storage period."
									+ " file=[%s], lastModified=%s, currentTime=%s, maxTime=%s, storagePeriod=%dhours, storagePeriodMillis=%d",
							name, lastmodifiedTime, currentTimeStr, maxTime, storagePeriod, storagePeriodMillis));
				}
				continue;
			}

			// 保存期間を過ぎてるので削除.
			if (!dump.delete()) {
				// 削除失敗.
				m_log.warn(
						methodName + DELIMITER + String.format("faild to delete dump file. file=[%s], lastModified=%s",
								name, new Timestamp(lastModified).toString()));
				continue;
			}
			// 削除完了.
			m_log.info(methodName + DELIMITER + String.format("success to delete dump file. file=[%s], lastModified=%s",
					name, new Timestamp(lastModified).toString()));
		}

	}

	// getter
	/** 監視設定. */
	public MonitorInfoWrapper getMonInfo() {
		return this.monInfo;
	}

	/** パケットキャプチャ制御用オブジェクト */
	public PcapHandle getHandle() {
		return this.handle;
	}

	/** 紐づきアドレス */
	public String getAddress() {
		return this.address;
	}
}
