/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.bean;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * バイナリレコードにつけるタグ定数.<br>
 * <br>
 * Java上ではHashMap{@code <tagName,tagValue>}として扱うため、<br>
 * 各タグのキーとバリューをここで管理.<br>
 * 
 **/
public final class BinaryTagConstant {

	// インスタンス化防止コンストラクタ.
	private BinaryTagConstant() {
		throw new IllegalStateException("ConstClass");
	}

	/** ファイル共通タグ **/
	public static final class CommonTagName {
		/** ファイル名(絶対パス) **/
		public static final String FILE_NAME = "FileName";
	}

	/** ファイル共通_メタデータ **/
	public static final class CommonFileAttributes {
		private CommonFileAttributes() {
			throw new IllegalStateException("ConstClass");
		}

		/** 基本ファイルメタデータ **/
		public static final class BasicFileAttributes {
			/** 作成日時() **/
			public static final String CREATION_TIME_TAGNAME = "CreationTime";
			/** サイズ(byte) **/
			public static final String SIZE_TAGNAME = "FileSize(byte)";

			private BasicFileAttributes() {
				throw new IllegalStateException("ConstClass");
			}
		}

		/** DOS属性ファイルメタデータ **/
		public static final class DosFileAttributes {
			/** 読み取り専用 **/
			public static final String READONLY_TAGNAME = "ReadOnlyFile";
			/** 隠しファイル **/
			public static final String HIDDEN_TAGNAME = "HiddenFile";
			/** アーカイブファイル **/
			public static final String ARCHIVE_TAGNAME = "ArchiveFile";
			/** システムファイル **/
			public static final String SYSTEM_TAGNAME = "SystemFile";

			private DosFileAttributes() {
				throw new IllegalStateException("ConstClass");
			}
		}

		/** Posix属性ファイルメタデータ **/
		public static final class PosixFileAttributes {
			/** ファイルアクセス権 **/
			public static final String PERMISSIONS_TAGNAME = "FilePermissions";
			/** ファイル所有者 **/
			public static final String OWNER_TAGNAME = "FileOwner";
			/** 所有グループ **/
			public static final String GROUP_TAGNAME = "GroupOwner";

			private PosixFileAttributes() {
				throw new IllegalStateException("ConstClass");
			}
		}

		/** ファイル所有者メタデータ **/
		public static final class AclFileAttributes {
			/** ファイルアクセス制御リスト(末尾連番) **/
			public static final String ACL_TAGNAME = "AccessControlList";
			/** ファイル所有者 **/
			public static final String OWNER_TAGNAME = "FileOwner";

			private AclFileAttributes() {
				throw new IllegalStateException("ConstClass");
			}
		}

		/** 共通value **/
		public static final class CommonValue {
			/** boolean値用TagValue **/
			public static final String TRUE_TAGVALUE = "yes";
			/** boolean値用TagValue **/
			public static final String FALSETAGVALUE = "no";

			/** boolean値のtagValue変換 **/
			public static String flgToString(boolean flg) {
				if (flg) {
					return TRUE_TAGVALUE;
				} else {
					return FALSETAGVALUE;
				}
			}

			private CommonValue() {
				throw new IllegalStateException("ConstClass");
			}
		}

	}
	
	/** レコード共通タグ **/
	public static final class CommonRecordTag {
		/** レコードサイズ **/
		public static final String RECORD_SIZE = "RecordSize(byte)";

		private CommonRecordTag() {
			throw new IllegalStateException("ConstClass");
		}
	}

	/** pacctファイル向けタグ **/
	public static final class Pacct {
		private Pacct() {
			throw new IllegalStateException("ConstClass");
		}

		/** コマンド名 **/
		public static final class CommandName {
			/** tag名称 **/
			public static final String TAGNAME = "CommandName";

			private CommandName() {
				throw new IllegalStateException("ConstClass");
			}
		}

		/** 実行ユーザーID **/
		public static final class UserID {
			/** tag名称 **/
			public static final String TAGNAME = "AccountingUserID";

			private UserID() {
				throw new IllegalStateException("ConstClass");
			}
		}

		/** 実行GroupID **/
		public static final class GroupID {
			/** tag名称 **/
			public static final String TAGNAME = "AccountingGroupID";

			private GroupID() {
				throw new IllegalStateException("ConstClass");
			}
		}

	}

	/** pcapファイル向けタグ **/
	public static final class Pcap {
		private Pcap() {
			throw new IllegalStateException("ConstClass");
		}

		/**
		 * リンクヘッダタイプ<br>
		 * <br>
		 * ネットワークインターフェースの種類によるパケットのヘッダタイプ.<br>
		 * <br>
		 * 各タイプの詳細については下記参照.<br>
		 * http://www.tcpdump.org/linktypes.html<br>
		 * 
		 */
		public static final class LinkType {
			private LinkType() {
				throw new IllegalStateException("ConstClass");
			}

			// tag名称
			/** tag名称 **/
			public static final String TAGNAME = "Link-LayerHeaderType";

			// tagValue
			/** null **/
			public static final String NULL = "null";
			/** IEEE 802.3 Ethernet **/
			public static final String ETHERNET = "IEEE 802.3 Ethernet ";
			/** AX.25 **/
			public static final String AX25 = "AX.25";
			/** IEEE 802.5 Token Ring **/
			public static final String IEEE802_5 = "IEEE 802.5 Token Ring";
			/** ARCNET Data Packets **/
			public static final String ARCNET = "ARCNET Data Packets";
			/** SLIP **/
			public static final String SLIP = "SLIP";
			/** PPP **/
			public static final String PPP = "PPP";
			/** FDDI **/
			public static final String FDDI = "FDDI";
			/** PPP in HDLC-like framing **/
			public static final String PPP_HDLC = "PPP in HDLC-like framing";
			/** PPPoE **/
			public static final String PPP_ETHER = "PPPoE";
			/** LLC/SNAP-encapsulated ATM **/
			public static final String ATM_RFC1483 = "LLC/SNAP-encapsulated ATM";
			/** Raw IP **/
			public static final String RAW = "Raw IP";
			/** Cisco PPP with HDLC framing **/
			public static final String C_HDLC = "Cisco PPP with HDLC framing";
			/** IEEE 802.11 wireless LAN **/
			public static final String IEEE802_11 = "IEEE 802.11 wireless LAN";
			/** Frame Relay **/
			public static final String FRELAY = "Frame Relay";
			/** OpenBSD loopback encapsulation **/
			public static final String LOOP = "OpenBSD loopback encapsulation";
			/** Linux "cooked" capture encapsulation **/
			public static final String LINUX_SLL = "Linux \"cooked\" capture encapsulation";
			/** Apple LocalTalk **/
			public static final String LTALK = "Apple LocalTalk";
			/** OpenBSD pflog **/
			public static final String PFLOG = "OpenBSD pflog";
			/** Prism monitor mode information **/
			public static final String IEEE802_11_PRISM = "Prism monitor mode information";
			/** IP-over-Fibre Channel **/
			public static final String IP_OVER_FC = "IP-over-Fibre Channel";
			/** SunATM devices **/
			public static final String SUNATM = "used by SunATM devices";
			/** Radiotap link-layer information **/
			public static final String IEEE802_11_RADIOTAP = "Radiotap link-layer information";
			/** ARCNET Data Packets **/
			public static final String ARCNET_LINUX = "ARCNET Data Packets";
			/** Apple IP-over-IEEE 1394 **/
			public static final String APPLE_IP_OVER_IEEE1394 = "Apple IP-over-IEEE 1394";
			/** Message Transfer Part Level 2, preceded by a pseudo-header **/
			public static final String MTP2_WITH_PHDR = "Message Transfer Part Level 2, preceded by a pseudo-header";
			/** Message Transfer Part Level 2 **/
			public static final String MTP2 = "Message Transfer Part Level 2";
			/** Message Transfer Part Level 3 **/
			public static final String MTP3 = "Message Transfer Part Level 3";
			/** Signalling Connection Control Part **/
			public static final String SCCP = "Signalling Connection Control Part";
			/** DOCSIS MAC frames **/
			public static final String DOCSIS = "DOCSIS MAC frames";
			/** Linux-IrDA packets **/
			public static final String LINUX_IRDA = "Linux-IrDA packets";
			/** Reserved for private use **/
			public static final String USER0_LINKTYPE_USER15 = "Reserved for private use";
			/** AVS monitor mode information **/
			public static final String IEEE802_11_AVS = "AVS monitor mode information";
			/** BACnet MS/TP frames **/
			public static final String BACNET_MS_TP = "BACnet MS/TP frames";
			/** PPP in HDLC-like encapsulation **/
			public static final String PPP_PPPD = "PPP in HDLC-like encapsulation";
			/** Transparent-mapped generic framing procedure **/
			public static final String GPF_T = "Transparent-mapped generic framing procedure";
			/** Frame-mapped generic framing procedure **/
			public static final String GPF_F = "Frame-mapped generic framing procedure";
			/** Link Access Procedures on the D Channel (LAPD) frames **/
			public static final String LINUX_LAPD = "Link Access Procedures on the D Channel (LAPD) frames";
			/** Bluetooth HCI UART transport layer **/
			public static final String BLUETOOTH_HCI_H4 = "Bluetooth HCI UART transport layer";
			/** Linux USB header **/
			public static final String USB_LINUX = "Linux USB header";
			/** Per-Packet Information **/
			public static final String PPI = "Per-Packet Information";
			/** IEEE 802.15.4 wireless Personal Area Network **/
			public static final String IEEE802_15_4 = "IEEE 802.15.4 wireless Personal Area Network";
			/** a pseudo-header, for SITA. **/
			public static final String SITA = "a pseudo-header, for SITA.";
			/** pseudo-header, for Endace DAG cards **/
			public static final String ERF = " pseudo-header, for Endace DAG cards";
			/** Bluetooth HCI UART transport layer **/
			public static final String BLUETOOTH_HCI_H4_WITH_PHDR = "Bluetooth HCI UART transport layer";
			/** AX.25 packet with KISS header **/
			public static final String AX25_KISS = "AX.25 packet with KISS header";
			/** Link Access Procedures on the D Channel (LAPD) frames **/
			public static final String LAPD = "Link Access Procedures on the D Channel (LAPD) frames";
			/** PPP, as per RFC 1661 and RFC 1662 **/
			public static final String PPP_WITH_DIR = "PPP, as per RFC 1661 and RFC 1662";
			/** Cisco PPP with HDLC framing,with a one-byte pseudo-header **/
			public static final String C_HDLC_WITH_DIR = "Cisco PPP with HDLC framing,with a one-byte pseudo-header";
			/** Frame Relay,with a one-byte pseudo-header **/
			public static final String FRELAY_WITH_DIR = "Frame Relay,with a one-byte pseudo-header";
			/** IPMB over an I2C circuit **/
			public static final String IPMB_LINUX = "IPMB over an I2C circuit";
			/** IEEE 802.15.4 wireless Personal Area Network **/
			public static final String IEEE802_15_4_NONASK_PHY = "IEEE 802.15.4 wireless Personal Area Network";
			/** USB packets, beginning with a Linux USB headert **/
			public static final String USB_LINUX_MMAPPED = "USB packets, beginning with a Linux USB headert";
			/** Fibre Channel FC-2 frames **/
			public static final String FC_2 = "Fibre Channel FC-2 frames";
			/** Fibre Channel FC-2 frames, beginning an encoding of the SOF **/
			public static final String FC_2_WITH_FRAME_DELIMS = "Fibre Channel FC-2 frames, beginning an encoding of the SOF";
			/** Solaris ipnet pseudo-header **/
			public static final String IPNET = "Solaris ipnet pseudo-header";
			/** CAN (Controller Area Network) frames **/
			public static final String CAN_SOCKETCAN = "CAN (Controller Area Network) frames";
			/** Raw IPv4 **/
			public static final String IPV4 = "Raw IPv4";
			/** Raw IPv6; **/
			public static final String IPV6 = "Raw IPv6;";
			/** IEEE 802.15.4 wireless Personal Area Network **/
			public static final String IEEE802_15_4_NOFCS = "IEEE 802.15.4 wireless Personal Area Network";
			/** Raw D-Bus messages **/
			public static final String DBUS = "Raw D-Bus messages";
			/** DVB Common Interface **/
			public static final String DVB_CI = "DVB Common Interface for communication between a PC Card module and a DVB receiver";
			/** Variant of 3GPP TS 27.010 multiplexing protocol **/
			public static final String MUX27010 = "Variant of 3GPP TS 27.010 multiplexing protocol ";
			/** D_PDUs as described by NATO standard STANAG 5066 **/
			public static final String STANAG_5066_D_PDU = "D_PDUs as described by NATO standard STANAG 5066";
			/** Linux netlink NETLINK NFLOG socket log messages **/
			public static final String NFLOG = "Linux netlink NETLINK NFLOG socket log messages";
			/**
			 * Pseudo-header for Hilscher Gesellschaft für Systemautomation mbH
			 * netANALYZER devices
			 **/
			public static final String NETANALYZER = "Pseudo-header for Hilscher Gesellschaft für Systemautomation mbH netANALYZER devices";
			/**
			 * Pseudo-header for Hilscher Gesellschaft für Systemautomation mbH
			 * netANALYZER devices, beginning with the preamble
			 **/
			public static final String NETANALYZER_TRANSPARENT = "Pseudo-header for Hilscher Gesellschaft für Systemautomation mbH netANALYZER devices, beginning with the preamble";
			/** IP-over-InfiniBand **/
			public static final String IPOIB = "IP-over-InfiniBand";
			/** MPEG_2_TS **/
			public static final String MPEG_2_TS = "MPEG-2 Transport Stream";
			/**
			 * Pseudo-header for ng4T GmbH's UMTS Iub/Iur-over-ATM and
			 * Iub/Iur-over-IP format as used by their ng40 protocol tester
			 **/
			public static final String NG40 = "Pseudo-header for ng4T GmbH's UMTS Iub/Iur-over-ATM and Iub/Iur-over-IP format as used by their ng40 protocol tester";
			/** Pseudo-header for NFC LLCP packet captures **/
			public static final String NFC_LLCP = "Pseudo-header for NFC LLCP packet captures";
			/** Raw InfiniBand frames **/
			public static final String INFINIBAND = "Raw InfiniBand frames";
			/** SCTP packets **/
			public static final String SCTP = "SCTP packets";
			/** USBPcap header **/
			public static final String USBPCAP = "USBPcap header";
			/**
			 * Serial-line packet header for the Schweitzer Engineering
			 * Laboratories "RTAC" product
			 **/
			public static final String RTAC_SERIAL = "Serial-line packet header for the Schweitzer Engineering Laboratories \"RTAC\" product";
			/** Bluetooth Low Energy air interface Link Layer packets **/
			public static final String BLUETOOTH_LE_LL = "Bluetooth Low Energy air interface Link Layer packets";
			/** Linux Netlink capture encapsulation **/
			public static final String NETLINK = "Linux Netlink capture encapsulation";
			/** Bluetooth Linux Monitor **/
			public static final String BLUETOOTH_LINUX_MONITOR = "Bluetooth Linux Monitor";
			/** Frame **/
			public static final String BLUETOOTH_BREDR_BB = "Bluetooth Basic Rate and Enhanced Data Rate baseband packets";
			/** Bluetooth Low Energy link-layer packets **/
			public static final String BLUETOOTH_LE_LL_WITH_PHDR = "Bluetooth Low Energy link-layer packets";
			/** PROFIBUS data link layer packets **/
			public static final String PROFIBUS_DL = "PROFIBUS data link layer packets";
			/** Apple PKTAP capture encapsulation **/
			public static final String PKTAP = "Apple PKTAP capture encapsulation";
			/** Ethernet-over-passive-optical-network packets **/
			public static final String EPON = "Ethernet-over-passive-optical-network packets";
			/** IPMI trace packets **/
			public static final String IPMI_HPM_2 = "IPMI trace packets";
			/** Z-Wave RF profile R1 and R2 packets **/
			public static final String ZWAVE_R1_R2 = "Z-Wave RF profile R1 and R2 packets";
			/** Z-Wave RF profile R3 packets **/
			public static final String ZWAVE_R3 = "Z-Wave RF profile R3 packets";
			/**
			 * Formats for WattStopper Digital Lighting Management (DLM) and
			 * Legrand Nitoo Open protocol
			 **/
			public static final String WATTSTOPPER_DLM = "Formats for WattStopper Digital Lighting Management (DLM) and Legrand Nitoo Open protocol";
			/** Messages between ISO 14443 contactless smartcards **/
			public static final String ISO_14443 = "Messages between ISO 14443 contactless smartcards";
			/** Radio data system (RDS) groups **/
			public static final String RDS = "Radio data system (RDS) groups";
			/** Darwin (macOS, etc.) USB header **/
			public static final String USB_DARWIN = "Darwin (macOS, etc.) USB header";
			/** SDLC packets **/
			public static final String SDLC = "SDLC packets";

			/** 定義なし(pcapファイルのバージョンがHinemos対応バージョンより後の場合等.) **/
			public static final String UNDEFINED = "undefined";

			// 他、tag生成時に利用する情報.
			/** バイナリvalueマップ(key:バイナリ上のvalue value:タグバリュー) **/
			public static final Map<Integer, String> BINARY_VALUE_MAP;

			// マップ等の定数初期化.
			static {
				/** バイナリvalueマップ(key:バイナリ上のvalue value:タグバリュー) **/
				HashMap<Integer, String> map = new HashMap<Integer, String>();
				map.put(Integer.valueOf(0), NULL);
				map.put(Integer.valueOf(1), ETHERNET);
				map.put(Integer.valueOf(3), AX25);
				map.put(Integer.valueOf(6), IEEE802_5);
				map.put(Integer.valueOf(7), ARCNET);
				map.put(Integer.valueOf(8), SLIP);
				map.put(Integer.valueOf(9), PPP);
				map.put(Integer.valueOf(10), FDDI);
				map.put(Integer.valueOf(50), PPP_HDLC);
				map.put(Integer.valueOf(51), PPP_ETHER);
				map.put(Integer.valueOf(100), ATM_RFC1483);
				map.put(Integer.valueOf(101), RAW);
				map.put(Integer.valueOf(104), C_HDLC);
				map.put(Integer.valueOf(105), IEEE802_11);
				map.put(Integer.valueOf(107), FRELAY);
				map.put(Integer.valueOf(108), LOOP);
				map.put(Integer.valueOf(113), LINUX_SLL);
				map.put(Integer.valueOf(114), LTALK);
				map.put(Integer.valueOf(117), PFLOG);
				map.put(Integer.valueOf(119), IEEE802_11_PRISM);
				map.put(Integer.valueOf(122), IP_OVER_FC);
				map.put(Integer.valueOf(123), SUNATM);
				map.put(Integer.valueOf(127), IEEE802_11_RADIOTAP);
				map.put(Integer.valueOf(129), ARCNET_LINUX);
				map.put(Integer.valueOf(138), APPLE_IP_OVER_IEEE1394);
				map.put(Integer.valueOf(139), MTP2_WITH_PHDR);
				map.put(Integer.valueOf(140), MTP2);
				map.put(Integer.valueOf(141), MTP3);
				map.put(Integer.valueOf(142), SCCP);
				map.put(Integer.valueOf(143), DOCSIS);
				map.put(Integer.valueOf(144), LINUX_IRDA);
				for (int i = 147; i <= 162; i++) {
					map.put(Integer.valueOf(i), USER0_LINKTYPE_USER15);
				}
				map.put(Integer.valueOf(163), IEEE802_11_AVS);
				map.put(Integer.valueOf(165), BACNET_MS_TP);
				map.put(Integer.valueOf(166), PPP_PPPD);
				map.put(Integer.valueOf(170), GPF_T);
				map.put(Integer.valueOf(171), GPF_F);
				map.put(Integer.valueOf(177), LINUX_LAPD);
				map.put(Integer.valueOf(187), BLUETOOTH_HCI_H4);
				map.put(Integer.valueOf(189), USB_LINUX);
				map.put(Integer.valueOf(192), PPI);
				map.put(Integer.valueOf(195), IEEE802_15_4);
				map.put(Integer.valueOf(196), SITA);
				map.put(Integer.valueOf(197), ERF);
				map.put(Integer.valueOf(201), BLUETOOTH_HCI_H4_WITH_PHDR);
				map.put(Integer.valueOf(202), AX25_KISS);
				map.put(Integer.valueOf(203), LAPD);
				map.put(Integer.valueOf(204), PPP_WITH_DIR);
				map.put(Integer.valueOf(205), C_HDLC_WITH_DIR);
				map.put(Integer.valueOf(209), IPMB_LINUX);
				map.put(Integer.valueOf(215), IEEE802_15_4_NONASK_PHY);
				map.put(Integer.valueOf(220), USB_LINUX_MMAPPED);
				map.put(Integer.valueOf(224), FC_2);
				map.put(Integer.valueOf(225), FC_2_WITH_FRAME_DELIMS);
				map.put(Integer.valueOf(226), IPNET);
				map.put(Integer.valueOf(227), CAN_SOCKETCAN);
				map.put(Integer.valueOf(228), IPV4);
				map.put(Integer.valueOf(229), IPV6);
				map.put(Integer.valueOf(230), IEEE802_15_4_NOFCS);
				map.put(Integer.valueOf(231), DBUS);
				map.put(Integer.valueOf(235), DVB_CI);
				map.put(Integer.valueOf(236), MUX27010);
				map.put(Integer.valueOf(237), STANAG_5066_D_PDU);
				map.put(Integer.valueOf(239), NFLOG);
				map.put(Integer.valueOf(240), NETANALYZER);
				map.put(Integer.valueOf(241), NETANALYZER_TRANSPARENT);
				map.put(Integer.valueOf(242), IPOIB);
				map.put(Integer.valueOf(243), MPEG_2_TS);
				map.put(Integer.valueOf(244), NG40);
				map.put(Integer.valueOf(245), NFC_LLCP);
				map.put(Integer.valueOf(247), INFINIBAND);
				map.put(Integer.valueOf(248), SCTP);
				map.put(Integer.valueOf(249), USBPCAP);
				map.put(Integer.valueOf(250), RTAC_SERIAL);
				map.put(Integer.valueOf(251), BLUETOOTH_LE_LL);
				map.put(Integer.valueOf(253), NETLINK);
				map.put(Integer.valueOf(254), BLUETOOTH_LINUX_MONITOR);
				map.put(Integer.valueOf(255), BLUETOOTH_BREDR_BB);
				map.put(Integer.valueOf(256), BLUETOOTH_LE_LL_WITH_PHDR);
				map.put(Integer.valueOf(257), PROFIBUS_DL);
				map.put(Integer.valueOf(258), PKTAP);
				map.put(Integer.valueOf(259), EPON);
				map.put(Integer.valueOf(260), IPMI_HPM_2);
				map.put(Integer.valueOf(261), ZWAVE_R1_R2);
				map.put(Integer.valueOf(262), ZWAVE_R3);
				map.put(Integer.valueOf(263), WATTSTOPPER_DLM);
				map.put(Integer.valueOf(264), ISO_14443);
				map.put(Integer.valueOf(265), RDS);
				map.put(Integer.valueOf(266), USB_DARWIN);
				map.put(Integer.valueOf(268), SDLC);
				BINARY_VALUE_MAP = Collections.unmodifiableMap(map);
			}
		}

		/** 宛先MACアドレス **/
		public static final class DestinationMAC {
			/** tag名称 **/
			public static final String TAGNAME = "DestinationMacAddress";

			private DestinationMAC() {
				throw new IllegalStateException("ConstClass");
			}
		}

		/** 送信元MACアドレス **/
		public static final class SourceMAC {
			/** tag名称 **/
			public static final String TAGNAME = "SourceMacAddress";

			private SourceMAC() {
				throw new IllegalStateException("ConstClass");
			}
		}

		/** プロトコル */
		public static final class Protocol {
			private Protocol() {
				throw new IllegalStateException("ConstClass");
			}

			// tag名称.
			/** tag名称 **/
			public static final String TAGNAME = "Protocol";

			// tagValue.
			/** IEEE802.3 Length Field **/
			public static final String IEEE802_3_LENGTH = "IEEE802.3 Length Field";
			/** Experimental **/
			public static final String EXPERIMENTAL = "Experimental";
			/** XEROX PUP **/
			public static final String XEROX_PUP = "XEROX PUP";
			/** PUP Addr Trans **/
			public static final String PUP_ADDR_TRANS = "PUP Addr Trans";
			/** Nixdorf **/
			public static final String NIXDORF = "Nixdorf";
			/** XEROX NS IDP **/
			public static final String XEROX_NS_IDP = "XEROX NS IDP";
			/** DLOG **/
			public static final String DLOG = "DLOG";
			/** IPv4 **/
			public static final String IPV4 = "Internet Protocol version 4 (IPv4)";
			/** X.75 Internet **/
			public static final String X75_INTERNET = "X.75 Internet";
			/** NBS Internet **/
			public static final String NBS_INTERNET = "NBS Internet";
			/** ECMA Internet **/
			public static final String ECMA_INTERNET = "ECMA Internet";
			/** Chaosnet **/
			public static final String CHAOSNET = "Chaosnet";
			/** X.25 Level 3 **/
			public static final String X25_LEVEL3 = "X.25 Level 3";
			/** ARP **/
			public static final String ARP = "Address Resolution Protocol (ARP)";
			/** XNS Compatability **/
			public static final String XNS_COMPATABILITY = "XNS Compatability";
			/** Frame Relay ARP **/
			public static final String FR_ARP = "Frame Relay ARP";
			/** Symbolics Private **/
			public static final String SYMBOLICS_PRIVATE = "Symbolics Private";
			/** Xyplex **/
			public static final String XYPLEX = "Xyplex";
			/** Ungermann-Bass net debugr **/
			public static final String UNG_BASS_NET_DBG = "Ungermann-Bass net debugr";
			/** Xerox IEEE802.3 PUP **/
			public static final String XEROX_IEEE802_3_PUP = "Xerox IEEE802.3 PUP";
			/** Banyan VINES **/
			public static final String BANYAN_VINES = "Banyan VINES";
			/** VINES Loopback **/
			public static final String VINES_LOOPBK = "VINES Loopback";
			/** VINES Echo **/
			public static final String VINES_ECHO = "VINES Echo";
			/** Berkeley Trailer nego **/
			public static final String BERKELEY_TRAILER_NEGO = "Berkeley Trailer nego";
			/** Berkeley Trailer encap/IP **/
			public static final String BERKELEY_TRAILER_ENCAP = "Berkeley Trailer encap/IP";
			/** Valid Systems **/
			public static final String VALID_SYSTEMS = "Valid Systems";
			/** TRILL **/
			public static final String TRILL = "TRILL";
			/** L2-IS-IS **/
			public static final String L2_IS_IS = "L2-IS-IS";
			/** PCS Basic Block Protocol **/
			public static final String PCS_BASIC = "PCS Basic Block Protocol";
			/** BBN Simnet **/
			public static final String BBN_SIMNET = "BBN Simnet";
			/** DEC Unassigned (Exp.) **/
			public static final String DEC_UNASSIGNED_EXP = "DEC Unassigned (Exp.)";
			/** DEC MOP Dump/Load **/
			public static final String DEC_MOP_DUMP = "DEC MOP Dump/Load";
			/** DEC MOP Remote Console **/
			public static final String DEC_MOP_REMOTE_CONSOLE = "DEC MOP Remote Console	";
			/** DEC DECNET Phase IV Route **/
			public static final String DEC_DECNET_PHASE = "DEC DECNET Phase IV Route";
			/** DEC LAT **/
			public static final String DEC_LAT = "DEC LAT";
			/** DEC Diagnostic Protocol **/
			public static final String DEC_DIAGNOSTIC = "DEC Diagnostic Protocol";
			/** DEC Customer Protocol **/
			public static final String DEC_CUSTOMER = "DEC Customer Protocol";
			/** DEC LAVC, SCA **/
			public static final String DEC_LAVC = "DEC LAVC, SCA";
			/** DEC Unassigned **/
			public static final String DEC_UNASSIGNED = "DEC Unassigned";
			/** 3Com Corporation **/
			public static final String THREE_COM_CORORATION = "3Com Corporation";
			/** Trans Ether Bridging **/
			public static final String TRANS_ETHER_BRIDGING = "Trans Ether Bridging";
			/** Raw Frame Relay **/
			public static final String RF_RELAY = "Raw Frame Relay";
			/** Ungermann-Bass download **/
			public static final String UNG_BASS_DOWNLOAD = "Ungermann-Bass download";
			/** Ungermann-Bass dia/loop **/
			public static final String UNG_BASS_DIA = "Ungermann-Bass dia/loop";
			/** LRT **/
			public static final String LRT = "LRT";
			/** Proteon **/
			public static final String PROTEON = "Proteon";
			/** Cabletron **/
			public static final String CABLETRON = "Cabletron";
			/** Cronus VLN **/
			public static final String CRONUS_VLN = "Cronus VLN";
			/** Cronus Direct **/
			public static final String CRONUS_DIRECT = "Cronus Direct";
			/** HP Probe **/
			public static final String HP_PROBE = "HP Probe";
			/** IPv4 **/
			public static final String NESTER = "Nestar";
			/** AT&T **/
			public static final String AT_AND_T = "AT&T";
			/** Excelan **/
			public static final String EXCELAN = "Excelan";
			/** SGI diagnostics **/
			public static final String SGI_DIAGNOSTICS = "SGI diagnostics";
			/** SGI network games **/
			public static final String SGI_NETWORK_GAMES = "SGI network games";
			/** SGI reserved **/
			public static final String SGI_RESERVED = "SGI reserved";
			/** SGI bounce server **/
			public static final String SGI_BOUNCE_SERVER = "SGI bounce server";
			/** Apollo Domain **/
			public static final String APOLLO_DOMAIN = "Apollo Domain";
			/** Tymshare **/
			public static final String TYMSHARE = "Tymshare";
			/** Tigan, Inc. **/
			public static final String TIGAN_INC = "Tigan, Inc.";
			/** Reverse Address Resolution Protocol (RARP) **/
			public static final String RARP = "Reverse Address Resolution Protocol (RARP)";
			/** Aeonic Systems **/
			public static final String AEONIC_SYSTEMS = "Aeonic Systems";
			/** DEC LANBridge **/
			public static final String DEC_LANBRIDGE = "DEC LANBridge";
			/** DEC Ethernet Encryption **/
			public static final String DEC_ETHERNET_ENCRYPTION = "DEC Ethernet Encryption";
			/** DEC LAN Traffic Monitor **/
			public static final String DEC_LAN_TRAFFIC_MONITOR = "DEC LAN Traffic Monitor";
			/** Planning Research Corp. **/
			public static final String PLANNING_RESEARCH_CORP = "Planning Research Corp.";
			/** ExperData **/
			public static final String EXPER_DATA = "ExperData";
			/** Stanford V Kernel exp. **/
			public static final String STANFORD_V_KERNEL_EXP = "Stanford V Kernel exp.";
			/** Stanford V Kernel prod. **/
			public static final String STANFORD_V_KERNEL_PROD = "Stanford V Kernel prod.";
			/** Evans & Sutherland **/
			public static final String EVANS_AND_SUTHERLAND = "Evans & Sutherland";
			/** Little Machines **/
			public static final String LITTLE_MACHINES = "Little Machines";
			/** Counterpoint Computers **/
			public static final String COUNTERPOINT_COMPUTERS = "Counterpoint Computers";
			/** Univ. of Mass. @ Amherst **/
			public static final String UNIV_MASS_AMHERST = "Univ. of Mass. @ Amherst";
			/** Veeco Integrated Auto. **/
			public static final String VEECO_INTEGRATED_AUTO = "Veeco Integrated Auto.";
			/** General Dynamics **/
			public static final String GENERAL_DYNAMICS = "General Dynamics";
			/** Autophon **/
			public static final String AUTOPHON = "Autophon";
			/** ComDesign **/
			public static final String COM_DESIGN = "ComDesign";
			/** Computgraphic Corp. **/
			public static final String COMPUTERGRAPHIC_CORP = "Computgraphic Corp.";
			/** Landmark Graphics Corp. **/
			public static final String LANDMARK_GRAPHICS_CORP = "Landmark Graphics Corp.";
			/** Matra **/
			public static final String MATRA = "Matra";
			/** Dansk Data Elektronik **/
			public static final String DNSK_DATA_ELECTRONIK = "Dansk Data Elektronik";
			/** Merit Internodal **/
			public static final String MERIT_INTERNODAL = "Merit Internodal";
			/** Vitalink Communications **/
			public static final String VITALINK_COMMUNICATIONS = "Vitalink Communications";
			/** Vitalink TransLAN III **/
			public static final String VITALINK_TRANSLAN_THREE = "Vitalink TransLAN III";
			/** Appletalk **/
			public static final String APPLETALK = "Appletalk";
			/** Datability **/
			public static final String DATABILITY = "Datability";
			/** Spider Systems Ltd. **/
			public static final String SPIDER_SYSTEMS_LTD = "Spider Systems Ltd.";
			/** Nixdorf Computers **/
			public static final String NIXDORF_COMPUTERS = "Nixdorf Computers";
			/** Siemens Gammasonics Inc. **/
			public static final String SIEMENS_GAMMASONICS_INC = "Siemens Gammasonics Inc.";
			/** DCA Data Exchange Cluster **/
			public static final String DCA_DATA_EXCHANGE_CLUSTER = "DCA Data Exchange Cluster";
			/** Banyan Systems **/
			public static final String BANYAN_SYSTEMS = "Banyan Systems";
			/** Pacer Software **/
			public static final String PACER_SOFTWARE = "Pacer Software";
			/** Applitek Corporation **/
			public static final String APPLITEK_CORPORATION = "Applitek Corporation";
			/** Intergraph Corporation **/
			public static final String INTERGRAPH_CORPORATION = "Intergraph Corporation";
			/** Harris Corporation **/
			public static final String HARRIS_CORPORATION = "Harris Corporation";
			/** Taylor Instrument **/
			public static final String TAYLOR_INSTRUMENT = "Taylor Instrument";
			/** Rosemount Corporation **/
			public static final String ROSEMOUNT_CORPORATION = "Rosemount Corporation";
			/** IBM SNA Service on Ether **/
			public static final String IBM_SNA_SERVICE_ON_ETHER = "IBM SNA Service on Ether";
			/** Varian Associates **/
			public static final String VARIAN_ASSOCIATES = "Varian Associates";
			/** Integrated Solutions TRFS **/
			public static final String INTEGRATED_SOLUTIONS_TRFS = "Integrated Solutions TRFS";
			/** Allen-Bradley **/
			public static final String ALLEN_BRADLEY = "Allen-Bradley";
			/** Retix **/
			public static final String RETIX = "Retix";
			/** AppleTalk AARP (Kinetics) **/
			public static final String APPLETALK_AARP_KINETICS = "AppleTalk AARP (Kinetics)";
			/** Kinetics **/
			public static final String KINETICS = "Kinetics";
			/** Apollo Computer **/
			public static final String APOLLO_COMPUTER = "Apollo Computer";
			/** Wellfleet Communications **/
			public static final String WELLFLEET_COMMUNICATIONS = "Wellfleet Communications";
			/**
			 * Customer VLAN Tag Type (C-Tag, formerly called the Q-Tag)
			 * (initially Wellfleet)
			 **/
			public static final String CUSTOMER_VLAN_TAG_TYPE = "Customer VLAN Tag Type (C-Tag, formerly called the Q-Tag) (initially Wellfleet)";
			/** Hayes Microcomputers **/
			public static final String HAYES_MICROCOMPUTERS = "Hayes Microcomputers";
			/** VG Laboratory Systems **/
			public static final String VG_LABORATORY_SYSTEMS = "VG Laboratory Systems";
			/** Bridge Communications **/
			public static final String BRIDGE_COMMUNICATIONS = "Bridge Communications";
			/** Novell, Inc. **/
			public static final String NOVELL_INC = "Novell, Inc.";
			/** KTI **/
			public static final String KTI = "KTI";
			/** Logicraft **/
			public static final String LOGICRAFT = "Logicraft";
			/** Network Computing Devices **/
			public static final String NETWORK_COMPUTING_DEVICES = "Network Computing Devices";
			/** Alpha Micro **/
			public static final String ALPHA_MICRO = "Alpha Micro";
			/** SNMP **/
			public static final String SNMP = "SNMP";
			/** BIIN **/
			public static final String BIIN = "BIIN";
			/** Technically Elite Concept **/
			public static final String TECHNICALLY_ELITE_CONCEPT = "Technically Elite Concept";
			/** Rational Corp **/
			public static final String RATIONAL_CORP = "Rational Corp";
			/** Qualcomm **/
			public static final String QUALCOMM = "Qualcomm";
			/** Computer Protocol Pty Ltd **/
			public static final String COMPUTER_PROTOCOL_PTY_LTD = "Computer Protocol Pty Ltd";
			/** Charles River Data System **/
			public static final String CHARLES_RIVER_DATA_SYSTEM = "Charles River Data System";
			/** XTP **/
			public static final String XTP = "XTP";
			/** SGI/Time Warner prop. **/
			public static final String SGI_TIME_WARNER_PROP = "SGI/Time Warner prop.";
			/** HIPPI-FP encapsulation **/
			public static final String HIPPI_FP_ENCAPSULATION = "HIPPI-FP encapsulation";
			/** STP, HIPPI-ST **/
			public static final String STP_HIPPI_ST = "STP, HIPPI-ST";
			/** Reserved for HIPPI-6400 **/
			public static final String RESERVED_FOR_HIPPI6400 = "Reserved for HIPPI-6400";
			/** Silicon Graphics prop. **/
			public static final String SILICON_GRAPHICS_PROP = "Silicon Graphics prop.";
			/** Motorola Computer **/
			public static final String MOTOROLA_COMPUTER = "Motorola Computer";
			/** ARAI Bunkichi **/
			public static final String ARAI_BUNKICHI = "ARAI Bunkichi";
			/** RAD Network Devices **/
			public static final String RAD_NETWORK_DEVICES = "RAD Network Devices";
			/** Apricot Computers **/
			public static final String APRICOT_COMPUTERS = "Apricot Computers";
			/** Artisoft **/
			public static final String ARTISOFT = "Artisoft";
			/** Polygon **/
			public static final String POLYGON = "Polygon";
			/** Comsat Labs **/
			public static final String COMSAT_LABS = "Comsat Labs";
			/** SAIC **/
			public static final String SAIC = "SAIC";
			/** VG Analytical **/
			public static final String VG_ANALYTICAL = "VG Analytical";
			/** Quantum Software **/
			public static final String QUANTUM_SOFTWARE = "Quantum Software";
			/** Ascom Banking Systems **/
			public static final String ASCOM_BANKING_SYSTEMS = "Ascom Banking Systems";
			/** Advanced Encryption Syste **/
			public static final String ADVANCED_ENCRYPTION_SYSTE = "Advanced Encryption Syste";
			/** Athena Programming **/
			public static final String ATHENA_PROGRAMMING = "Athena Programming";
			/** Inst Ind Info Tech **/
			public static final String INST_IND_INFO_TECH = "Inst Ind Info Tech";
			/** Taurus Controls **/
			public static final String TAURUS_CONTROLS = "Taurus Controls";
			/** Walker Richer & Quinn **/
			public static final String WALKER_RICHER_AND_QUINN = "Walker Richer & Quinn";
			/** Idea Courier **/
			public static final String IDEA_COURIER = "Idea Courier";
			/** Computer Network Tech **/
			public static final String COMPUTER_NETWORK_TECH = "Computer Network Tech";
			/** Gateway Communications **/
			public static final String GATEWAY_COMMUNICATIONS = "Gateway Communications";
			/** SECTRA **/
			public static final String SECTRA = "SECTRA";
			/** Delta Controls **/
			public static final String DELTA_CONTROLS = "Delta Controls";
			/** Internet Protocol version 6 (IPv6) **/
			public static final String IPV6 = "Internet Protocol version 6 (IPv6)";
			/** ATOMIC **/
			public static final String ATOMIC = "ATOMIC";
			/** Landis & Gyr Powers **/
			public static final String LANDIS_AND_GYR_POWERS = "Landis & Gyr Powers";
			/** Motorola **/
			public static final String MOTOROLA = "Motorola";
			/** TCP/IP Compression **/
			public static final String TCP_IP_COMPRESSION = "TCP/IP Compression";
			/** IP Autonomous Systems **/
			public static final String IP_AUTONOMOUS_SYSTEMS = "IP Autonomous Systems";
			/** Secure Data **/
			public static final String SECURE_DATA = "Secure Data";
			/** IEEE Std 802.3 - Ethernet Passive Optical Network (EPON) **/
			public static final String IEEESTD8023_ETHERNET_PASSIVE = "IEEE Std 802.3 - Ethernet Passive Optical Network (EPON)";
			/** Point-to-Point Protocol (PPP) **/
			public static final String PPP = "Point-to-Point Protocol (PPP)";
			/** General Switch Management Protocol (GSMP) **/
			public static final String GSMP = "General Switch Management Protocol (GSMP)";
			/** MPLS **/
			public static final String MPLS = "MPLS";
			/** MPLS with upstream-assigned label **/
			public static final String MPLS_UPSTREAM_ASSIGNED_LABEL = "MPLS with upstream-assigned label";
			/** Multicast Channel Allocation Protocol (MCAP) **/
			public static final String MCAP = "Multicast Channel Allocation Protocol (MCAP)";
			/** PPP over Ethernet (PPPoE) Discovery Stage **/
			public static final String PPPOE_DISCOVERY = "PPP over Ethernet (PPPoE) Discovery Stage";
			/** PPP over Ethernet (PPPoE) Session Stage **/
			public static final String PPPOE_SESSION = "PPP over Ethernet (PPPoE) Session Stage";
			/** IEEE Std 802.1X - Port-based network access control **/
			public static final String IEEESTD8021X_PORT_NAC = "IEEE Std 802.1X - Port-based network access control";
			/** IEEE Std 802.1Q - Service VLAN tag identifier (S-Tag) **/
			public static final String IEEESTD8021Q_STAG = "IEEE Std 802.1Q - Service VLAN tag identifier (S-Tag)";
			/** Invisible Software **/
			public static final String INVISIBLE_SOFTWARE = "Invisible Software";
			/** IEEE Std 802 - Local Experimental Ethertype **/
			public static final String IEEESTD802_LOCAL_EXP_ETHERTYPE = "IEEE Std 802 - Local Experimental Ethertype";
			/** IEEE Std 802 - OUI Extended Ethertype **/
			public static final String IEEESTD802_OUI_EXT_ETHERTYPE = "IEEE Std 802 - OUI Extended Ethertype";
			/** IEEE Std 802.11 - Pre-Authentication (802.11i) **/
			public static final String IEEESTD80211_80211I = "IEEE Std 802.11 - Pre-Authentication (802.11i)";
			/** IEEE Std 802.1AB - Link Layer Discovery Protocol (LLDP) **/
			public static final String IEEESTD8021AB_LLDP = "IEEE Std 802.1AB - Link Layer Discovery Protocol (LLDP)";
			/** IEEE Std 802.1AE - Media Access Control Security **/
			public static final String IEEESTD8021AE_MACS = "IEEE Std 802.1AE - Media Access Control Security";
			/** Provider Backbone Bridging Instance tag **/
			public static final String BRIDGING_INSTANCE_TAG = "Provider Backbone Bridging Instance tag";
			/** IEEE Std 802.1Q - Multiple VLAN Registration Protocol (MVRP) **/
			public static final String IEEESTD8021Q_MVRP = "IEEE Std 802.1Q - Multiple VLAN Registration Protocol (MVRP)";
			/**
			 * IEEE Std 802.1Q - Multiple Multicast Registration Protocol (MMRP)
			 **/
			public static final String IEEESTD8021Q_MMRP = "IEEE Std 802.1Q - Multiple Multicast Registration Protocol (MMRP)";
			/** IEEE Std 802.11 - Fast Roaming Remote Request (802.11r) **/
			public static final String IEEESTD80211_80211R = "IEEE Std 802.11 - Fast Roaming Remote Request (802.11r)";
			/** IEEE Std 802.21 - Media Independent Handover Protocol **/
			public static final String IEEESTD80221_MIH_PROTOCOL = "IEEE Std 802.21 - Media Independent Handover Protocol";
			/** IEEE Std 802.1Qbe - Multiple I-SID Registration Protocol **/
			public static final String IEEESTD8021QBE_MISR_PROTOCOL = "IEEE Std 802.1Qbe - Multiple I-SID Registration Protocol";
			/** TRILL Fine Grained Labeling (FGL) **/
			public static final String FGL = "TRILL Fine Grained Labeling (FGL)";
			/** IEEE Std 802.1Qbg - ECP Protocol (also used in 802.1BR) **/
			public static final String IEEESTD8021QBG_ECP_PROTOCOL = "IEEE Std 802.1Qbg - ECP Protocol (also used in 802.1BR)";
			/** TRILL RBridge Channel **/
			public static final String TRILL_RBRIDGE_CHANNEL = "TRILL RBridge Channel";
			/** GeoNetworking as defined in ETSI EN 302 636-4-1 **/
			public static final String GEONETWORKING_ETSI_EN_302_636_4_1 = "GeoNetworking as defined in ETSI EN 302 636-4-1";
			/** Loopback **/
			public static final String LOOPBACK = "Loopback";
			/** 3Com(Bridge) XNS Sys Mgmt **/
			public static final String THREE_COM_XNS_SYS_MGMT = "3Com(Bridge) XNS Sys Mgmt";
			/** 3Com(Bridge) TCP-IP Sys **/
			public static final String THREE_COM_TCP_IP_SYS = "3Com(Bridge) TCP-IP Sys";
			/** 3Com(Bridge) loop detect **/
			public static final String THREE_COM_LOOP_DETECT = "3Com(Bridge) loop detect";
			/** LoWPAN encapsulation **/
			public static final String LOWPAN_ENCAPSULATION = "LoWPAN encapsulation";
			/**
			 * The Ethertype will be used to identify a "Channel" in which
			 * control messages are encapsulated as payload of GRE packets. When
			 * a GRE packet tagged with the Ethertype is received, the payload
			 * will be handed to the network processor for processing.
			 **/
			public static final String ETHERTYPE_GRE_PACKET = "The Ethertype will be used to identify a \"Channel\" in which control messages are encapsulated as payload of GRE packets. When a GRE packet tagged with the Ethertype is received, the payload will be handed to the network processor for processing.";
			/** BBN VITAL-LanBridge cache **/
			public static final String BBN_VITAL_LANBRIDGE_CACHE = "BBN VITAL-LanBridge cache";
			/** ISC Bunker Ramo **/
			public static final String ISC_BUNKER_RAMO = "ISC Bunker Ramo";
			/** Reserved **/
			public static final String RESERVED = "Reserved";
			/** 定義なし(EtherNetのバージョンがHinemos対応バージョンより後の場合等.) **/
			public static final String UNDEFINED = "undefined";

			// 他、tag生成時に利用する情報.
			/**
			 * Ethernet valueマップ(key:バイナリ上16進数表記 value:タグバリュー)<br>
			 * <br>
			 * Ethernetヘッダ内でのバイナリ上のvalue<br>
			 * <br>
			 * 詳細は下記参照<br>
			 * https://www.iana.org/assignments/ieee-802-numbers/ieee-802-
			 * numbers.xhtml#ieee-802-numbers-1<br>
			 **/
			public static final Map<String, String> ETHERNET_VALUE_MAP;

			// マップ等の定数初期化.
			static {
				/** Ethernet valueマップ(key:バイナリ上16進数表記 value:タグバリュー) **/
				HashMap<String, String> map = new HashMap<String, String>();
				String hexStr = "";
				int startHex = Integer.parseInt("0000", 16);
				int endHex = Integer.parseInt("05DC", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, IEEE802_3_LENGTH);
				}
				startHex = Integer.parseInt("0101", 16);
				endHex = Integer.parseInt("01FF", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, EXPERIMENTAL);
				}
				map.put("0200", XEROX_PUP);
				map.put("0201", PUP_ADDR_TRANS);
				map.put("0400", NIXDORF);
				map.put("0600", XEROX_NS_IDP);
				startHex = Integer.parseInt("0660", 16);
				endHex = Integer.parseInt("0661", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, DLOG);
				}
				map.put("0800", IPV4);
				map.put("0801", X75_INTERNET);
				map.put("0802", NBS_INTERNET);
				map.put("0803", ECMA_INTERNET);
				map.put("0804", CHAOSNET);
				map.put("0805", X25_LEVEL3);
				map.put("0806", ARP);
				map.put("0807", XNS_COMPATABILITY);
				map.put("0808", FR_ARP);
				map.put("081C", SYMBOLICS_PRIVATE);
				startHex = Integer.parseInt("0888", 16);
				endHex = Integer.parseInt("088A", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, XYPLEX);
				}
				map.put("0900", UNG_BASS_NET_DBG);
				map.put("0A00", XEROX_IEEE802_3_PUP);
				map.put("0A01", PUP_ADDR_TRANS);
				map.put("0BAD", BANYAN_VINES);
				map.put("0BAE", VINES_LOOPBK);
				map.put("0BAF", VINES_ECHO);
				map.put("1000", BERKELEY_TRAILER_NEGO);
				startHex = Integer.parseInt("1001", 16);
				endHex = Integer.parseInt("100F", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, BERKELEY_TRAILER_ENCAP);
				}
				map.put("1600", VALID_SYSTEMS);
				map.put("22F3", TRILL);
				map.put("22F4", L2_IS_IS);
				map.put("1600", PCS_BASIC);
				map.put("5208", BBN_SIMNET);
				map.put("6000", DEC_UNASSIGNED_EXP);
				map.put("22F4", L2_IS_IS);
				map.put("6001", DEC_MOP_DUMP);
				map.put("6002", DEC_MOP_REMOTE_CONSOLE);
				map.put("6003", DEC_DECNET_PHASE);
				map.put("6004", DEC_LAT);
				map.put("6005", DEC_DIAGNOSTIC);
				map.put("6006", DEC_CUSTOMER);
				map.put("6007", DEC_LAVC);
				startHex = Integer.parseInt("6008", 16);
				endHex = Integer.parseInt("6009", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, DEC_UNASSIGNED);
				}
				startHex = Integer.parseInt("6010", 16);
				endHex = Integer.parseInt("6014", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, THREE_COM_CORORATION);
				}
				map.put("6558", TRANS_ETHER_BRIDGING);
				map.put("6559", RF_RELAY);
				map.put("7000", UNG_BASS_DOWNLOAD);
				map.put("7002", UNG_BASS_DIA);
				startHex = Integer.parseInt("7020", 16);
				endHex = Integer.parseInt("7029", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, LRT);
				}
				map.put("7030", PROTEON);
				map.put("7034", CABLETRON);
				map.put("8003", CRONUS_VLN);
				map.put("8004", CRONUS_DIRECT);
				map.put("8005", HP_PROBE);
				map.put("8006", NESTER);
				map.put("8008", AT_AND_T);
				map.put("8010", EXCELAN);
				map.put("8013", SGI_DIAGNOSTICS);
				map.put("8014", SGI_NETWORK_GAMES);
				map.put("8015", SGI_RESERVED);
				map.put("8016", SGI_BOUNCE_SERVER);
				map.put("8019", APOLLO_DOMAIN);
				map.put("802E", TYMSHARE);
				map.put("802F", TIGAN_INC);
				map.put("8035", RARP);
				map.put("8036", AEONIC_SYSTEMS);
				map.put("8038", DEC_LANBRIDGE);
				startHex = Integer.parseInt("8039", 16);
				endHex = Integer.parseInt("803C", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, DEC_UNASSIGNED);
				}
				map.put("803D", DEC_ETHERNET_ENCRYPTION);
				map.put("803E", DEC_UNASSIGNED);
				map.put("803F", DEC_LAN_TRAFFIC_MONITOR);
				startHex = Integer.parseInt("8040", 16);
				endHex = Integer.parseInt("8042", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, DEC_UNASSIGNED);
				}
				map.put("8044", PLANNING_RESEARCH_CORP);
				map.put("8046", AT_AND_T);
				map.put("8047", AT_AND_T);
				map.put("8049", EXPER_DATA);
				map.put("805B", STANFORD_V_KERNEL_EXP);
				map.put("805C", STANFORD_V_KERNEL_PROD);
				map.put("805D", EVANS_AND_SUTHERLAND);
				map.put("8060", LITTLE_MACHINES);
				map.put("8062", COUNTERPOINT_COMPUTERS);
				map.put("8065", UNIV_MASS_AMHERST);
				map.put("8066", UNIV_MASS_AMHERST);
				map.put("8067", VEECO_INTEGRATED_AUTO);
				map.put("8068", GENERAL_DYNAMICS);
				map.put("8069", AT_AND_T);
				map.put("806A", AUTOPHON);
				map.put("806C", COM_DESIGN);
				map.put("806D", COMPUTERGRAPHIC_CORP);
				startHex = Integer.parseInt("806E", 16);
				endHex = Integer.parseInt("8077", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, LANDMARK_GRAPHICS_CORP);
				}
				map.put("807A", MATRA);
				map.put("807B", DNSK_DATA_ELECTRONIK);
				map.put("807C", MERIT_INTERNODAL);
				startHex = Integer.parseInt("807D", 16);
				endHex = Integer.parseInt("807F", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, VITALINK_COMMUNICATIONS);
				}
				map.put("8080", VITALINK_TRANSLAN_THREE);
				startHex = Integer.parseInt("8081", 16);
				endHex = Integer.parseInt("8083", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, COUNTERPOINT_COMPUTERS);
				}
				map.put("809B", APPLETALK);
				startHex = Integer.parseInt("809C", 16);
				endHex = Integer.parseInt("809E", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, DATABILITY);
				}

				startHex = Integer.parseInt("80C0", 16);
				endHex = Integer.parseInt("80C3", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, SPIDER_SYSTEMS_LTD);
				}
				map.put("80A3", NIXDORF_COMPUTERS);
				startHex = Integer.parseInt("80A4", 16);
				endHex = Integer.parseInt("80B3", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, SIEMENS_GAMMASONICS_INC);
				}
				startHex = Integer.parseInt("80C0", 16);
				endHex = Integer.parseInt("80C3", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, DCA_DATA_EXCHANGE_CLUSTER);
				}
				map.put("80C4", BANYAN_SYSTEMS);
				map.put("80C5", BANYAN_SYSTEMS);
				map.put("80C6", PACER_SOFTWARE);
				map.put("80C7", APPLITEK_CORPORATION);
				startHex = Integer.parseInt("80C8", 16);
				endHex = Integer.parseInt("80CC", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, INTERGRAPH_CORPORATION);
				}
				startHex = Integer.parseInt("80CD", 16);
				endHex = Integer.parseInt("80CE", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, HARRIS_CORPORATION);
				}
				startHex = Integer.parseInt("80CF", 16);
				endHex = Integer.parseInt("80D2", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, TAYLOR_INSTRUMENT);
				}
				startHex = Integer.parseInt("80D3", 16);
				endHex = Integer.parseInt("80D4", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, ROSEMOUNT_CORPORATION);
				}
				map.put("80D5", IBM_SNA_SERVICE_ON_ETHER);
				map.put("80DD", VARIAN_ASSOCIATES);
				startHex = Integer.parseInt("80DE", 16);
				endHex = Integer.parseInt("80DF", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, INTEGRATED_SOLUTIONS_TRFS);
				}
				startHex = Integer.parseInt("80E0", 16);
				endHex = Integer.parseInt("80E3", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, ALLEN_BRADLEY);
				}
				startHex = Integer.parseInt("80E4", 16);
				endHex = Integer.parseInt("80F0", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, DATABILITY);
				}
				map.put("80F2", RETIX);
				map.put("80F3", APPLETALK_AARP_KINETICS);
				startHex = Integer.parseInt("80F4", 16);
				endHex = Integer.parseInt("80F5", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, KINETICS);
				}
				map.put("80F7", APOLLO_COMPUTER);
				map.put("80FF", WELLFLEET_COMMUNICATIONS);
				map.put("8100", CUSTOMER_VLAN_TAG_TYPE);
				startHex = Integer.parseInt("8101", 16);
				endHex = Integer.parseInt("8103", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, WELLFLEET_COMMUNICATIONS);
				}
				startHex = Integer.parseInt("8107", 16);
				endHex = Integer.parseInt("8109", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, SYMBOLICS_PRIVATE);
				}
				map.put("8130", HAYES_MICROCOMPUTERS);
				map.put("8131", VG_LABORATORY_SYSTEMS);
				startHex = Integer.parseInt("8132", 16);
				endHex = Integer.parseInt("8136", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, BRIDGE_COMMUNICATIONS);
				}
				startHex = Integer.parseInt("8137", 16);
				endHex = Integer.parseInt("8138", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, NOVELL_INC);
				}
				startHex = Integer.parseInt("8139", 16);
				endHex = Integer.parseInt("813D", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, KTI);
				}
				map.put("8148", LOGICRAFT);
				map.put("8149", NETWORK_COMPUTING_DEVICES);
				map.put("814A", ALPHA_MICRO);
				map.put("814C", SNMP);
				map.put("814D", BIIN);
				map.put("814E", BIIN);
				map.put("814F", TECHNICALLY_ELITE_CONCEPT);
				map.put("8150", RATIONAL_CORP);
				startHex = Integer.parseInt("8151", 16);
				endHex = Integer.parseInt("8153", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, QUALCOMM);
				}
				startHex = Integer.parseInt("815C", 16);
				endHex = Integer.parseInt("815E", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, COMPUTER_PROTOCOL_PTY_LTD);
				}
				startHex = Integer.parseInt("8164", 16);
				endHex = Integer.parseInt("8166", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, CHARLES_RIVER_DATA_SYSTEM);
				}
				map.put("817D", XTP);
				map.put("817E", SGI_TIME_WARNER_PROP);
				map.put("8180", HIPPI_FP_ENCAPSULATION);
				map.put("8181", STP_HIPPI_ST);
				map.put("8182", RESERVED_FOR_HIPPI6400);
				map.put("8183", RESERVED_FOR_HIPPI6400);
				startHex = Integer.parseInt("8184", 16);
				endHex = Integer.parseInt("818C", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, SILICON_GRAPHICS_PROP);
				}
				map.put("818D", MOTOROLA_COMPUTER);
				startHex = Integer.parseInt("819A", 16);
				endHex = Integer.parseInt("81A3", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, QUALCOMM);
				}
				map.put("81A4", ARAI_BUNKICHI);
				startHex = Integer.parseInt("81A5", 16);
				endHex = Integer.parseInt("81AE", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, RAD_NETWORK_DEVICES);
				}
				startHex = Integer.parseInt("81B7", 16);
				endHex = Integer.parseInt("81B9", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, XYPLEX);
				}
				startHex = Integer.parseInt("81CC", 16);
				endHex = Integer.parseInt("81D5", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, APRICOT_COMPUTERS);
				}
				startHex = Integer.parseInt("81D6", 16);
				endHex = Integer.parseInt("81DD", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, ARTISOFT);
				}
				startHex = Integer.parseInt("81E6", 16);
				endHex = Integer.parseInt("81EF", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, POLYGON);
				}
				startHex = Integer.parseInt("81F0", 16);
				endHex = Integer.parseInt("81F2", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, COMSAT_LABS);
				}
				startHex = Integer.parseInt("81F3", 16);
				endHex = Integer.parseInt("81F5", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, SAIC);
				}
				startHex = Integer.parseInt("81F6", 16);
				endHex = Integer.parseInt("81F8", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, VG_ANALYTICAL);
				}
				startHex = Integer.parseInt("8203", 16);
				endHex = Integer.parseInt("8205", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, QUANTUM_SOFTWARE);
				}
				startHex = Integer.parseInt("8221", 16);
				endHex = Integer.parseInt("8222", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, ASCOM_BANKING_SYSTEMS);
				}
				startHex = Integer.parseInt("823E", 16);
				endHex = Integer.parseInt("8240", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, ADVANCED_ENCRYPTION_SYSTE);
				}
				startHex = Integer.parseInt("827F", 16);
				endHex = Integer.parseInt("8282", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, ATHENA_PROGRAMMING);
				}
				startHex = Integer.parseInt("8263", 16);
				endHex = Integer.parseInt("826A", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, CHARLES_RIVER_DATA_SYSTEM);
				}
				startHex = Integer.parseInt("829A", 16);
				endHex = Integer.parseInt("829B", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, INST_IND_INFO_TECH);
				}
				startHex = Integer.parseInt("829C", 16);
				endHex = Integer.parseInt("82AB", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, TAURUS_CONTROLS);
				}
				startHex = Integer.parseInt("82AC", 16);
				endHex = Integer.parseInt("8693", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, WALKER_RICHER_AND_QUINN);
				}
				startHex = Integer.parseInt("8694", 16);
				endHex = Integer.parseInt("869D", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, IDEA_COURIER);
				}
				startHex = Integer.parseInt("869E", 16);
				endHex = Integer.parseInt("86A1", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, COMPUTER_NETWORK_TECH);
				}
				startHex = Integer.parseInt("86A3", 16);
				endHex = Integer.parseInt("86AC", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, GATEWAY_COMMUNICATIONS);
				}
				map.put("86DB", SECTRA);
				map.put("86DE", DELTA_CONTROLS);
				map.put("86DD", IPV6);
				map.put("86DF", ATOMIC);
				startHex = Integer.parseInt("86E0", 16);
				endHex = Integer.parseInt("86EF", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, LANDIS_AND_GYR_POWERS);
				}
				startHex = Integer.parseInt("8700", 16);
				endHex = Integer.parseInt("8710", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, MOTOROLA);
				}
				map.put("876B", TCP_IP_COMPRESSION);
				map.put("876C", IP_AUTONOMOUS_SYSTEMS);
				map.put("876D", SECURE_DATA);
				map.put("8808", IEEESTD8023_ETHERNET_PASSIVE);
				map.put("880B", PPP);
				map.put("880C", GSMP);
				map.put("8847", MPLS);
				map.put("8848", MPLS_UPSTREAM_ASSIGNED_LABEL);
				map.put("8861", MCAP);
				map.put("8863", PPPOE_DISCOVERY);
				map.put("8864", PPPOE_SESSION);
				map.put("888E", IEEESTD8021X_PORT_NAC);
				map.put("88A8", IEEESTD8021Q_STAG);
				startHex = Integer.parseInt("8A96", 16);
				endHex = Integer.parseInt("8A97", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, INVISIBLE_SOFTWARE);
				}
				map.put("88B5", IEEESTD802_LOCAL_EXP_ETHERTYPE);
				map.put("88B6", IEEESTD802_LOCAL_EXP_ETHERTYPE);
				map.put("88B7", IEEESTD802_OUI_EXT_ETHERTYPE);
				map.put("88C7", IEEESTD80211_80211I);
				map.put("88CC", IEEESTD8021AB_LLDP);
				map.put("8800000", IEEESTD8021AE_MACS);
				map.put("880000000", BRIDGING_INSTANCE_TAG);
				map.put("88F5", IEEESTD8021Q_MVRP);
				map.put("88F6", IEEESTD8021Q_MMRP);
				map.put("890D", IEEESTD80211_80211R);
				map.put("8917", IEEESTD80221_MIH_PROTOCOL);
				map.put("8929", IEEESTD8021QBE_MISR_PROTOCOL);
				map.put("893B", FGL);
				map.put("8940", IEEESTD8021QBG_ECP_PROTOCOL);
				map.put("8946", TRILL_RBRIDGE_CHANNEL);
				map.put("8947", GEONETWORKING_ETSI_EN_302_636_4_1);
				map.put("9000", LOOPBACK);
				map.put("9001", THREE_COM_XNS_SYS_MGMT);
				map.put("9002", THREE_COM_TCP_IP_SYS);
				map.put("9003", THREE_COM_LOOP_DETECT);
				map.put("A0ED", LOWPAN_ENCAPSULATION);
				map.put("B7EA", ETHERTYPE_GRE_PACKET);
				map.put("FF00", BBN_VITAL_LANBRIDGE_CACHE);
				startHex = Integer.parseInt("FF00", 16);
				endHex = Integer.parseInt("FF0F", 16);
				for (int i = startHex; i <= endHex; i++) {
					hexStr = Integer.toHexString(i);
					map.put(hexStr, ISC_BUNKER_RAMO);
				}
				map.put("FFFF", RESERVED);

				ETHERNET_VALUE_MAP = Collections.unmodifiableMap(map);
			}

		}

	}
}
