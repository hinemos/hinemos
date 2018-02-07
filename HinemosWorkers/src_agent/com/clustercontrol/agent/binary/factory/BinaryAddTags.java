/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.binary.factory;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryFlag;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.binary.readingstatus.FileReadingStatus;
import com.clustercontrol.agent.binary.result.BinaryFile;
import com.clustercontrol.agent.binary.result.BinaryRecord;
import com.clustercontrol.binary.bean.BinaryConstant;
import com.clustercontrol.binary.bean.BinaryTagConstant;
import com.clustercontrol.util.BinaryUtil;
import com.clustercontrol.ws.monitor.BinaryCheckInfo;
import com.clustercontrol.ws.monitor.BinaryPatternInfo;

/**
 * タグ追加用のクラス.<br>
 * <br>
 * 各Tagのkeyとvalueについては下記参照.<br>
 * /HinemosCommon/src/com/clustercontrol/binary/bean/BinaryTagConstant.java
 */
public class BinaryAddTags {

	// ログ出力関連
	/** ロガー */
	private static Log m_log = LogFactory.getLog(BinaryAddTags.class);

	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	/**
	 * 各ファイルにタグ追加.
	 * 
	 * @param readingStatus
	 * 
	 * @param readingStatus
	 *            監視ファイル読込状態.
	 * @param fileResult
	 *            タグ追加対象の監視結果ファイル情報.
	 * @param binaryInfo
	 *            監視設定の内バイナリ監視に関する情報<br>
	 */
	public static void addFileTags(FileReadingStatus readingStatus, BinaryFile fileResult, BinaryCheckInfo binaryInfo) {

		// ファイル分類の取得.
		String tagType = binaryInfo.getTagType();

		// ファイルメタデータのタグ追加.
		addFileMetadataTags(readingStatus, fileResult);

		// ファイル分類毎にタグを追加.
		if (BinaryConstant.TAG_TYPE_PCAP.equals(tagType)) {
			// パケットキャプチャの場合.
			addPcapFileTags(fileResult, binaryInfo);
			return;
		}

	}

	/**
	 * ファイルメタデータのタグ追加.<br>
	 * <br>
	 * タグ取得できなかった場合はログ出力.
	 * 
	 * @param readingStatus
	 *            監視ファイル読込状態.
	 * @param fileResult
	 *            タグ追加対象の監視結果ファイル情報.
	 */
	private static void addFileMetadataTags(FileReadingStatus readingStatus, BinaryFile fileResult) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		// 共通変数の初期化.
		FileSystem fs = FileSystems.getDefault();
		Path path = fs.getPath(readingStatus.getMonFileName());
		String tagValue = "";

		// 基本ファイルメタデータ.
		try {
			BasicFileAttributes baseAttr;
			baseAttr = Files.readAttributes(path, BasicFileAttributes.class);
			// 作成日時.
			tagValue = baseAttr.creationTime().toString();
			fileResult.getTags().put(BinaryTagConstant.CommonFileAttributes.BasicFileAttributes.CREATION_TIME_TAGNAME,
					tagValue);
			// サイズ.
			tagValue = Long.toString(baseAttr.size());
			fileResult.getTags().put(BinaryTagConstant.CommonFileAttributes.BasicFileAttributes.SIZE_TAGNAME, tagValue);
		} catch (IOException e) {
			// 基本ファイルメタデータは全システムで取得可能な想定なのでwarnレベルで出力.
			m_log.warn(e.getMessage(), e);
		}

		// DOS属性ファイルメタデータ(Windows系のみ).
		try {
			DosFileAttributes dosAttr;
			dosAttr = Files.readAttributes(path, DosFileAttributes.class);
			// 読み取り専用.
			tagValue = BinaryTagConstant.CommonFileAttributes.CommonValue.flgToString(dosAttr.isReadOnly());
			fileResult.getTags().put(BinaryTagConstant.CommonFileAttributes.DosFileAttributes.READONLY_TAGNAME,
					tagValue);
			// 隠しファイル.
			tagValue = BinaryTagConstant.CommonFileAttributes.CommonValue.flgToString(dosAttr.isHidden());
			fileResult.getTags().put(BinaryTagConstant.CommonFileAttributes.DosFileAttributes.HIDDEN_TAGNAME, tagValue);
			// アーカイブファイル.
			tagValue = BinaryTagConstant.CommonFileAttributes.CommonValue.flgToString(dosAttr.isArchive());
			fileResult.getTags().put(BinaryTagConstant.CommonFileAttributes.DosFileAttributes.ARCHIVE_TAGNAME,
					tagValue);
			// システムファイル.
			tagValue = BinaryTagConstant.CommonFileAttributes.CommonValue.flgToString(dosAttr.isSystem());
			fileResult.getTags().put(BinaryTagConstant.CommonFileAttributes.DosFileAttributes.SYSTEM_TAGNAME, tagValue);
		} catch (IOException e) {
			// Windows系以外の場合は取れない想定なのでinfo.
			m_log.info(methodName + DELIMITER + "failed to get file attributes for DOS");
		} catch (Exception e) {
			// Windows系以外の場合は取れない想定なのでinfo.
			m_log.info(methodName + DELIMITER + "failed to get file attributes for DOS");
		}

		// ファイルアクセスメタデータ(ACL)(Windows系のみ).
		try {
			AclFileAttributeView aclAttrView;
			aclAttrView = Files.getFileAttributeView(path, AclFileAttributeView.class);
			// ファイル所有者.
			tagValue = aclAttrView.getOwner().toString();
			fileResult.getTags().put(BinaryTagConstant.CommonFileAttributes.AclFileAttributes.OWNER_TAGNAME, tagValue);
			// ACLループ用変数.
			int tagSeaquence = 1;
			StringBuilder sb = null;
			String tagname = "";
			Iterator<AclEntryPermission> aclPermItr = null;
			AclEntryPermission aclEntPerm = null;
			Iterator<AclEntryFlag> aclFlgsItr = null;
			AclEntryFlag aclEntFlg = null;
			// ACLのtag設定(リスト保持なので複数tag)。
			for (AclEntry aclEntry : aclAttrView.getAcl()) {
				sb = new StringBuilder();
				sb.append("princibal:" + aclEntry.principal() + "\n");
				sb.append("permissions:");
				aclPermItr = aclEntry.permissions().iterator();
				while (aclPermItr.hasNext()) {
					aclEntPerm = aclPermItr.next();
					sb.append(aclEntPerm.toString());
					if (aclPermItr.hasNext()) {
						sb.append(",");
					} else {
						sb.append("\n");
					}
				}
				sb.append("type:" + aclEntry.type().toString() + "\n");
				sb.append("flags:");
				aclFlgsItr = aclEntry.flags().iterator();
				while (aclFlgsItr.hasNext()) {
					aclEntFlg = aclFlgsItr.next();
					sb.append(aclEntFlg.toString());
					if (aclFlgsItr.hasNext()) {
						sb.append(",");
					} else {
						sb.append("\n");
					}
				}
				// タグ追加.
				tagValue = sb.toString();
				tagname = BinaryTagConstant.CommonFileAttributes.AclFileAttributes.ACL_TAGNAME + tagSeaquence;
				fileResult.getTags().put(tagname, tagValue);
				tagSeaquence++;
			}
		} catch (IOException e) {
			// Windows系以外の場合は取れない想定なのでinfo.
			m_log.info(methodName + DELIMITER + "failed to get file attributes of ACL");
		} catch (Exception e) {
			// Windows系以外の場合は取れない想定なのでinfo.
			m_log.info(methodName + DELIMITER + "failed to get file attributes of ACL");
		}

		// Posix属性ファイルメタデータ(Linux系のみ).
		try {
			PosixFileAttributes posixAttr;
			posixAttr = Files.readAttributes(path, PosixFileAttributes.class);
			// ファイルアクセス権.
			tagValue = PosixFilePermissions.toString(posixAttr.permissions());
			fileResult.getTags().put(BinaryTagConstant.CommonFileAttributes.PosixFileAttributes.PERMISSIONS_TAGNAME,
					tagValue);
			// ファイル所有者.
			tagValue = posixAttr.owner().toString();
			fileResult.getTags().put(BinaryTagConstant.CommonFileAttributes.PosixFileAttributes.OWNER_TAGNAME,
					tagValue);
			// ファイル所有グループ.
			tagValue = posixAttr.group().toString();
			fileResult.getTags().put(BinaryTagConstant.CommonFileAttributes.PosixFileAttributes.GROUP_TAGNAME,
					tagValue);
		} catch (IOException e) {
			// Linux系以外の場合は取れない想定なのでwarn.
			m_log.info(methodName + DELIMITER + "failed to get file attributes for Posix");
		} catch (Exception e) {
			// Linux系以外の場合は取れない想定なのでinfo.
			m_log.info(methodName + DELIMITER + "failed to get file attributes for Posix");
		}

	}

	/**
	 * PCAPファイル用のレコードタグ追加.
	 * 
	 * @param fileResult
	 *            タグ追加対象の監視結果ファイル情報.
	 * @param binaryInfo
	 *            バイナリ監視情報.
	 */
	private static void addPcapFileTags(BinaryFile fileResult, BinaryCheckInfo binaryInfo) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		List<Byte> fileHeaderBinary = fileResult.getFileHeader();
		if (fileHeaderBinary == null || fileHeaderBinary.isEmpty()) {
			// ファイルヘッダ存在しないのでログ出力して終了(ありえない想定).
			m_log.warn(methodName + DELIMITER + "failed getting file header.");
			return;
		}

		// リンクヘッダタイプのバイナリ取得.
		List<Byte> linkTypeBinary = new ArrayList<Byte>(fileHeaderBinary);
		linkTypeBinary = linkTypeBinary.subList(20, 24);
		Collections.reverse(linkTypeBinary);
		// リンクヘッダタイプのバイナリをtagValueへ変換.
		String hexStr = Hex.encodeHexString(BinaryUtil.listToArray(linkTypeBinary));
		int linkTypeValue = Integer.parseInt(hexStr, 16);
		String linkType = BinaryTagConstant.Pcap.LinkType.BINARY_VALUE_MAP.get(linkTypeValue);
		if (linkType == null) {
			// 変換できなかった場合.
			linkType = BinaryTagConstant.Pcap.LinkType.UNDEFINED;
		}
		// リンクヘッダタイプのタグ追加.
		fileResult.getTags().put(BinaryTagConstant.Pcap.LinkType.TAGNAME, linkType);
	}

	/**
	 * 各レコードにタグ追加.
	 * 
	 * @param fileInfo
	 *            ファイル単位の監視結果情報.
	 * @param sendData
	 *            タグ追加対象のレコードリスト.
	 * @param binaryInfo
	 *            監視設定の内バイナリ監視に関する情報
	 * @param patternInfoList
	 *            フィルタ条件<br>
	 */
	public static void addRecordTags(BinaryFile fileInfo, List<BinaryRecord> sendData, BinaryCheckInfo binaryInfo,
			List<BinaryPatternInfo> patternInfoList) {

		addComRecTags(sendData);

		String tagType = binaryInfo.getTagType();

		// タイプ別にタグを追加.
		if (BinaryConstant.TAG_TYPE_PACCT.equals(tagType)) {
			addPacctRecTags(sendData, binaryInfo, patternInfoList);
		} else if (BinaryConstant.TAG_TYPE_WTMP.equals(tagType)) {
			// 汎用性低いので開発対象外とする.
			return;
		} else if (BinaryConstant.TAG_TYPE_PCAP.equals(tagType)) {
			addPcapRecTags(fileInfo, sendData, binaryInfo);
		} else {
			// 汎用は特に何もしないので返却
			return;
		}

	}

	/**
	 * 共通レコードタグ追加.
	 * 
	 * @param sendData
	 *            タグ追加対象のレコードリスト.
	 */
	private static void addComRecTags(List<BinaryRecord> sendData) {

		String tagValue = null;

		for (BinaryRecord record : sendData) {
			// レコードサイズ.
			tagValue = Integer.toString(record.getAlldata().size());
			record.getTags().put(BinaryTagConstant.CommonRecordTag.RECORD_SIZE, tagValue);
		}

	}

	/**
	 * PACCTファイル用のレコードタグ追加.
	 * 
	 * @param sendData
	 *            タグ追加対象のレコードリスト.
	 */
	private static void addPacctRecTags(List<BinaryRecord> sendData, BinaryCheckInfo binaryInfo,
			List<BinaryPatternInfo> patternInfoList) {

		// ループ内で利用する変数初期化.
		List<Byte> recBinary = null;
		List<Byte> tmpBinary = new ArrayList<Byte>();
		String hexStr = null;
		String tagValue = null;
		String textEncoding = null;

		// エンコード取得.
		if (patternInfoList != null && !patternInfoList.isEmpty()) {
			for (BinaryPatternInfo pattern : patternInfoList) {
				if (pattern.getEncoding() != null && !pattern.getEncoding().isEmpty()) {
					textEncoding = pattern.getEncoding();
					break;
				}
			}
		}

		// レコード毎にtag付加処理.
		for (BinaryRecord record : sendData) {
			recBinary = record.getAlldata();

			// コマンド名.
			if (textEncoding != null) {
				tmpBinary = new ArrayList<Byte>(recBinary);
				tmpBinary = tmpBinary.subList(48, tmpBinary.size());
				tagValue = BinaryUtil.listToEncodeString(tmpBinary, textEncoding);
				if (tagValue == null) {
					tagValue = "";
				}
				record.getTags().put(BinaryTagConstant.Pacct.CommandName.TAGNAME, tagValue);
			}

			// UserID.
			tmpBinary = new ArrayList<Byte>(recBinary);
			tmpBinary = tmpBinary.subList(8, 10);
			Collections.reverse(tmpBinary);
			hexStr = BinaryUtil.listToString(tmpBinary);
			tagValue = Integer.toString(Integer.parseInt(hexStr, 16));
			record.getTags().put(BinaryTagConstant.Pacct.UserID.TAGNAME, tagValue);

			// GroupID
			tmpBinary = new ArrayList<Byte>(recBinary);
			tmpBinary = tmpBinary.subList(12, 14);
			Collections.reverse(tmpBinary);
			hexStr = BinaryUtil.listToString(tmpBinary);
			tagValue = Integer.toString(Integer.parseInt(hexStr, 16));
			record.getTags().put(BinaryTagConstant.Pacct.GroupID.TAGNAME, tagValue);

		}
	}

	/**
	 * PCAPファイル用のレコードタグ追加.
	 * 
	 * @param fileInfo
	 *            ファイル単位の監視結果情報.
	 * @param sendData
	 *            タグ追加対象のレコードリスト.
	 * @param binaryInfo
	 *            監視設定の内バイナリ監視に関する情報<br>
	 */
	private static void addPcapRecTags(BinaryFile fileInfo, List<BinaryRecord> sendData, BinaryCheckInfo binaryInfo) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start.");

		// リンクヘッダタイプを取得
		String linkType = fileInfo.getTags().get(BinaryTagConstant.Pcap.LinkType.TAGNAME);

		// pcapファイルはリンクヘッダタイプ毎でバイナリ解析してタグ追加.
		if (BinaryTagConstant.Pcap.LinkType.ETHERNET.equals(linkType)
				|| BinaryTagConstant.Pcap.LinkType.PPP_ETHER.equals(linkType)) {
			// Ethernetヘッダを含むリンクヘッダタイプの場合.

			// ループ内で利用する変数初期化.
			List<Byte> ethernetHeader = null;
			List<Byte> sourceMac = null;
			String sourceMacStr = "";
			List<Byte> destMac = null;
			String destMacStr = "";
			List<Byte> protocol = null;
			String protcolValue = "";
			String protocolName = "";

			// レコード毎にタグを追加.
			for (BinaryRecord record : sendData) {
				m_log.debug(methodName + DELIMITER + "add pcap tags for ETHERNET. record size="
						+ record.getAlldata().size());
				if (record.getAlldata().size() <= 30) {
					continue;
				}
				// Ethernetヘッダの取得(先頭16byteはpcapパケットヘッダで物理ヘッダは存在しない).
				ethernetHeader = new ArrayList<Byte>(record.getAlldata());
				ethernetHeader = ethernetHeader.subList(binaryInfo.getRecordHeadSize(), 30);

				// 送信元MACアドレス.
				sourceMac = new ArrayList<Byte>(ethernetHeader);
				sourceMac = sourceMac.subList(6, 12);
				sourceMacStr = BinaryUtil.listToString(sourceMac, 1, ":");
				record.getTags().put(BinaryTagConstant.Pcap.SourceMAC.TAGNAME, sourceMacStr);

				// 宛先MACアドレス.
				destMac = new ArrayList<Byte>(ethernetHeader);
				destMac = destMac.subList(0, 6);
				destMacStr = BinaryUtil.listToString(destMac, 1, ":");
				record.getTags().put(BinaryTagConstant.Pcap.DestinationMAC.TAGNAME, destMacStr);

				// プロトコルタイプ.
				protocol = new ArrayList<Byte>(ethernetHeader);
				protocol = protocol.subList(12, 14);
				protcolValue = BinaryUtil.listToString(protocol).toUpperCase();
				protocolName = BinaryTagConstant.Pcap.Protocol.ETHERNET_VALUE_MAP.get(protcolValue);
				if (protocolName == null) {
					protocolName = BinaryTagConstant.Pcap.Protocol.UNDEFINED;
				}
				record.getTags().put(BinaryTagConstant.Pcap.Protocol.TAGNAME, protocolName);
			}

		}

	}
}
