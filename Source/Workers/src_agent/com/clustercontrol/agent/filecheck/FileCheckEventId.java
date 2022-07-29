/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.filecheck;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ファイルチェック契機のイベント(マネージャへの通知)ごとに割り当てるユニークなIDを表します。
 */
public class FileCheckEventId {
	private static final int PROCESS_BYTES_LENGTH = 6;
	private static final int TIME_BYTES_LENGTH = 6;

	private static final Log log = LogFactory.getLog(FileCheckEventId.class);

	/**
	 * 乱数生成器。
	 */
	private static SecureRandom random;

	/**
	 * IDの固定バイト列。
	 * 初期化処理に対してFindBugsがdouble-checked-lockingを検知してしまうので、
	 * volatile宣言している。
	 * (このケースではDCLイディオムの問題の肝である、「命令の並び替えや最適化によって
	 * "nullではないが未初期化状態"のオブジェクトを参照してしまう」ことにはならないと思うので、
	 * 宣言は必要ないとは思うが…。)
	 */
	private static volatile byte[] staticBytes;

	/**
	 * 最後に(直近で)IDとして採用した日時。
	 */
	private static AtomicLong lastTimeValue;

	static {
		initialize();
	}

	// staticフィールドのリセットを可能にする
	static void initialize() {
		random = new SecureRandom();
		staticBytes = null;
		lastTimeValue = new AtomicLong();
	}

	private byte[] dynamicBytes;
	private byte[] bytes;
	private String base64;

	// 外部依存動作のモック置換を可能にする
	private External external;
	static class External {
		Enumeration<NetworkInterface> getNetworkInterfaces() throws SocketException {
			return NetworkInterface.getNetworkInterfaces();
		}
	}

	/**
	 * IDを生成します。
	 */
	public FileCheckEventId() {
		this(new External());
	}

	FileCheckEventId(External external) {
		this.external = external;
		
		// 固定部分が未生成なら生成する
		if (staticBytes == null) {
			synchronized (FileCheckEventId.class) {
				if (staticBytes == null) {
					staticBytes = generateStaticBytes();
					log.info("Generated static identifier: " + Hex.encodeHexString(staticBytes));
				}
			}
		}
		// 変動部分を生成する
		dynamicBytes = generateDynamicBytes();

		// 結合
		bytes = joinBytes(dynamicBytes, staticBytes);
		
		// Base64で保存
		base64 = Base64.encodeBase64String(bytes);

		log.info("Generated: " + Hex.encodeHexString(bytes) + "(" + base64 + ")");
	}

	/**
	 * IDを返します。
	 * @return IDのバイト列。
	 */
	public byte[] getBytes() {
		return bytes;
	}

	/**
	 * IDの動的部分(静的でない部分)を返します。
	 * @return IDの動的部分のバイト列。 
	 */
	public byte[] getDynamicBytes() {
		return dynamicBytes;
	}

	/**
	 * IDの静的部分(エージェントのプロセス内で固定の部分)を返します。
	 * @return IDの静的部分のバイト列。
	 */
	public byte[] getStaticBytes() {
		return staticBytes;
	}

	/**
	 * IDを返します。
	 * @return IDをBase64で表現した文字列。
	 */
	public String getBase64() {
		return base64;
	}

	private byte[] generateStaticBytes() {
		// MACアドレスを取得する
		byte[] macAddress = null;
		try {
			for (NetworkInterface nif : Collections.list(external.getNetworkInterfaces())) {
				if (nif.isLoopback()) continue;
				byte[] mac = nif.getHardwareAddress();
				if (mac == null) continue;
				// WindowsではISATAPのトンネリングアダプタが8バイトを返してくるので、これを除外する。
				if (mac.length != 6) continue;
				macAddress = mac;
				log.info("MAC address: " + Hex.encodeHexString(macAddress));
				break;
			}
		} catch (Exception e) {
			log.warn("Exception has occurred.", e);
		}

		// MACを取得できなかった場合、乱数で代用する
		if (macAddress == null) {
			log.warn("Failed to generate an unique identifier from MAC address, Uses random bytes instead.");
			macAddress = new byte[6];
			random.nextBytes(macAddress);
			log.info("(Pseudo) MAC address: " + Hex.encodeHexString(macAddress));
		}

		// プロセス識別IDを、乱数で生成する
		byte[] procId = new byte[PROCESS_BYTES_LENGTH];
		random.nextBytes(procId);
		log.info("Process identifier: " + Hex.encodeHexString(procId));

		// バイト列を結合
		return joinBytes(macAddress, procId);
	}

	private byte[] generateDynamicBytes() {
		// 重複しない現在時刻を採番
		long last;
		long now;
		do {
			last = lastTimeValue.get();
			now = System.currentTimeMillis();
			if (last >= now) {
				now = last + 1;
			}
		} while (!lastTimeValue.compareAndSet(last, now));

		// 時刻をバイト列化
		ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE / 8);
		buffer.putLong(now);
		byte[] bytes = buffer.array();
		return Arrays.copyOfRange(bytes, bytes.length - TIME_BYTES_LENGTH, bytes.length);
	}

	// 2つのbyte[]を結合 (HinemosCommonのBinaryUtilへ移動させるべきか？)
	private byte[] joinBytes(byte[] a1, byte[] a2) {
		byte[] ret = new byte[a1.length + a2.length];
		System.arraycopy(a1, 0, ret, 0, a1.length);
		System.arraycopy(a2, 0, ret, a1.length, a2.length);
		return ret;
	}
}
