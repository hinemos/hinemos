/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.util;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * サブネットのアドレス(CIDR記法で表現可能なIPアドレスのグループ)を表します。
 */
public class SubnetAddress {
	static enum AddressType {
		IPV4, IPV6
	}

	private AddressType addrType;
	private int prefixLength;
	private byte[] address;
	private byte[] prefix;
	private byte[] mask;
	private int hashCode;
	private String str;
	// フィールドを追加する場合は、hashCode と equals() の更新を忘れずに。

	/**
	 * コンストラクタ。
	 * 
	 * @param cidr CIDR記法の文字列。
	 * @throws UnknownHostException CIDR記法に文法的誤りがあります。
	 */
	public SubnetAddress(String cidr) throws UnknownHostException {
		// サブネットのIPアドレス とプレフィックス長へ分離
		String[] tokens = cidr.split("/");

		// InetAddress が不正なIPアドレスをホスト名とみなして名前解決を試みることで生じる
		// タイムロスを抑制するため、基本的な書式チェックを前もって行う。
		checkIpAddressFormat(tokens[0]);

		// InetAddress を 文字列 -> バイト列 コンバータとして利用する。
		// InetAddress のインスタンスをフィールドに持っておけば便利かもしれないが、
		// 想定外のリソースを保持している状態になっても困るので、必要な情報だけを取り出す。
		InetAddress ia = InetAddress.getByName(tokens[0]);
		address = ia.getAddress();

		// プレフィックス長の解析
		if (tokens.length == 1) {
			// 指定されていない場合はIPアドレス全体とみなす
			prefixLength = address.length * 8;
		} else if (tokens.length == 2){
			try {
				prefixLength = Integer.parseInt(tokens[1]);
			} catch (NumberFormatException e) {
				throw new UnknownHostException("Malformed prefix length: " + cidr);
			}
		} else {
			throw new UnknownHostException("Too many slashes: " + cidr);
		}

		// アドレスの種類
		if (ia instanceof Inet4Address) {
			addrType = AddressType.IPV4;
			if (prefixLength < 0 || prefixLength > 32) {
				throw new UnknownHostException("Illegal IPv4 prefix length: " + prefixLength + ", CIDR="  + cidr);
			}
			
		} else if (ia instanceof Inet6Address) {
			addrType = AddressType.IPV6;
			if (prefixLength < 0 || prefixLength > 128) {
				throw new UnknownHostException("Illegal IPv6 prefix length: " + prefixLength + ", CIDR="  + cidr);
			}
		} else {
			// IPアドレスの規格に変化がない限りここへはこないはず
			throw new UnknownHostException("Unknown address type: " + ia.getClass().getName() + ", CIDR=" + cidr);
		}

		// サブネット マスク
		mask = makeSubnetMask(address.length, prefixLength);

		// サブネット プレフィクス
		prefix = mask(address, mask);
		
		// ハッシュコード
		hashCode = makeHashCode();
		
		// 文字列表現
		str = ia.getHostAddress() + "/" + prefixLength;
	}
	
	/**
	 * 指定されたIPアドレスの書式をチェックします。
	 * <p>
	 * 単体テストのため static method へロジックを抽出したものです。
	 * 
	 * @throws UnknownHostException 書式エラー。
	 */
	protected static void checkIpAddressFormat(String addr) throws UnknownHostException {
		if (addr.indexOf('.') != -1) {
			// IPv4
			String[] octets = addr.split("\\.");
			if (octets.length != 4) {
				throw new UnknownHostException("Number of octets is not 4: " + addr);
			}
			for (String octet : octets) {
				try {
					int o = Integer.parseInt(octet);
					if (o < 0 || o > 255) {
						throw new UnknownHostException("Out of octet value range: " + addr);
					}
				} catch (NumberFormatException e) {
					throw new UnknownHostException("Not decimal numeric: " + addr);
				}
			}
		} else {
			// IPv6
			String[] hextets = addr.split(":");
			if (hextets.length > 8) {
				throw new UnknownHostException("Number of hextets is larger than 8: " + addr);
			}
			for (String hextet : hextets) {
				if (hextet.length() == 0) continue;
				try {
					int h = Integer.parseInt(hextet, 16);
					if (h < 0 || h > 65535) {
						throw new UnknownHostException("Out of hextet value range: " + addr);
					}
				} catch (NumberFormatException e) {
					throw new UnknownHostException("Not hex numeric: " + addr);
				}
			}
		}
	}
	
	/**
	 * サブネットマスクのバイト列を生成します。
	 * <p>
	 * 単体テストのため static method へロジックを抽出したものです。
	 * 
	 * @param bytes アドレスのバイト数。
	 * @param length プレフィックスの長さ(マスクのビット数)。
	 */
	protected static byte[] makeSubnetMask(int bytes, int length) {
		byte[] rtn = new byte[bytes];
		int q = length / 8;
		int r = length % 8;
		for (int i = 0; i < bytes; ++i) {
			if (i < q) {
				rtn[i] = (byte) 0xff;
			} else if (r > 0 && i == q) {
				rtn[i] = (byte) (0xff << (8 - r));
			} else {
				rtn[i] = 0;
			}
		}
		return rtn;
	}

	/**
	 * バイト列へマスクを適用して返します。
	 * <p>
	 * 単体テストのため static method へロジックを抽出したものです。
	 * 
	 * @param addr マスクしたいバイト列。
	 * @param mask マスクのバイト列。
	 * @throws IllegalArgumentException アドレスとマスクの長さが異なる。
	 */
	protected static byte[] mask(byte[] addr, byte[] mask) {
		if (addr.length != mask.length) {
			throw new IllegalArgumentException("Length unequals: addr=" + addr.length + ", mask=" + mask.length);
		}
		byte[] rtn = new byte[addr.length];
		for (int i = 0; i < addr.length; ++i) {
			rtn[i] = (byte) (addr[i] & mask[i]);
		}
		return rtn;
	}

	/**
	 * オブジェクトのハッシュコードを計算します。
	 */
	protected int makeHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((addrType == null) ? 0 : addrType.hashCode());
		result = prime * result + prefixLength;
		result = prime * result + Arrays.hashCode(prefix);
		return result;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	/**
	 * 等しいかどうかはプレフィックス部分で判断します。
	 * すなわち以下の式は true となります。
	 * <pre>
	 * new SubnetAddress("192.168.0.0/16").equals(new SubnetAddress("192.168.1.1/16"))
	 * </pre>
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		SubnetAddress other = (SubnetAddress) obj;
		if (addrType != other.addrType) return false;
		if (prefixLength != other.prefixLength) return false;
		if (!Arrays.equals(prefix, other.prefix)) return false;
		return true;
	}

	@Override
	public String toString() {
		return str;
	}

	/**
	 * IPアドレス部を返します。
	 * 
	 * @return IPアドレスを表すバイト列。
	 */
	public byte[] getAddress() {
		return address;
	}

	/**
	 * IPV4である場合は true を返します。
	 */
	public boolean isIpv4() {
		return addrType == AddressType.IPV4;
	}

	/**
	 * IPV6である場合は true を返します。
	 */
	public boolean isIpv6() {
		return addrType == AddressType.IPV6;
	}

	/**
	 * 指定されたIPアドレスへサブネットマスクを適用して返します。
	 * 
	 * @throws IllegalArgumentException アドレスとマスクの長さが異なる。
	 */
	public byte[] mask(byte[] addr) {
		return mask(addr, mask);
	}

	/**
	 * 指定されたIPアドレスが、このサブネットアドレスの範囲内である場合は true を返します。
	 * <p>
	 * 原則として判定エラー時に例外を投げることはありません。
	 * 例えば、IPv4のサブネットアドレスに対してIPv6のアドレスを渡した場合、例外ではなく false を返します。 
	 */
	public boolean contains(byte addr[]) {
		if (addr.length != mask.length) return false;
		return Arrays.equals(mask(addr), prefix);
	}
	
	/**
	 * 指定されたIPアドレスが、このサブネットアドレスの範囲内である場合は true を返します。
	 * <p>
	 * 原則として判定エラー時に例外を投げることはありません。
	 * 例えば、IPv4のサブネットアドレスに対してIPv6のアドレスを渡した場合、例外ではなく false を返します。 
	 */
	public boolean contains(InetAddress addr) {
		return contains(addr.getAddress());
	}

	/**
	 * 指定されたIPアドレスが、このサブネットアドレスの範囲内である場合は true を返します。
	 * <p>
	 * 原則として判定エラー時に例外を投げることはありません。
	 * 例えば、IPv4のサブネットアドレスに対してIPv6のアドレスを渡した場合や、指定された文字列をIPアドレスとして解釈できない場合、
	 * 例外ではなく false を返します。
	 */
	public boolean contains(String addr) {
		try {
			checkIpAddressFormat(addr);
			return contains(InetAddress.getByName(addr));
		} catch (UnknownHostException e) {
			return false;
		}
	}

	/**
	 * 指定された複数のIPアドレスのうち、このサブネットアドレスの範囲内に該当するものをリスト化して返します。
	 */
	public List<String> filter(Iterable<String> addrs) {
		List<String> matched = new ArrayList<>();
		for (String addr: addrs) {
			if (contains(addr)) {
				matched.add(addr);
			}
		}
		return matched;
	}

}
