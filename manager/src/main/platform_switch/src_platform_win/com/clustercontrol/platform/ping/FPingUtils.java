/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.platform.ping;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.clustercontrol.platform.ping.FPingUtils.ICMPRes.Status;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;

/**
 * FPing Utility.
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
public class FPingUtils {
	
	private Logger logger = Logger.getLogger(FPingUtils.class);
	
	//IPHeleper
	private static ICMPHelper iphelper = null;
	//SourceAddress for Ipv6
	private Inet6Address anyAddress = null;
	//結果メッセージ
	private List<String> resultMessages = null;
	//ホスト別実行結果
	private Map<String,List<ICMPRes>> resultMap = null;
	
	static {
		//ライブラリを読み込む
		//多重読み込みNGのため、staticブロックで実行
		iphelper = (ICMPHelper)Native.loadLibrary ("Iphlpapi", ICMPHelper.class);
	}
	
	/**
	 * Fping実行オプション
	 *
	 */
	public static class FPingOption {
		//繰り返し回数
		public int count = 1;
		//実行間隔(ms)
		public long interval = 1000;
		//パケットサイズ(byte)
		public int packetSize = 32;
		//タイムアウト時間(ms)
		public int timeout = 500;
		//Time To Live
		public int ttl = 128;//windowsデフォルト値 128
		//Type of Service
		public int tos = 0;
	}
	
	/**
	 * ICMP 実行結果
	 *
	 */
	public static class ICMPRes {
		//ホスト
		public String host = null;
		//IP
		public String ip = null;
		//ステータスコード
		public int statusCd = 0;
		//ステータス
		public Status status = null;
		//Message
		public String message = null;
		//count
		public int count = 0;
		//RoundTripTime(ms)
		public int rtt = -1;
		//PacketSize(byte)
		public int size = -1;
		//Time to live
		public int ttl = -1;
		
		//statuscdの意味を表すenum
		public enum Status {
			IP_SUCCESS(0,"The status was success."),
			IP_BUF_TOO_SMALL(11001,"The reply buffer was too small."),
			IP_DEST_NET_UNREACHABLE(11002,"The destination network was unreachable."),
			IP_DEST_HOST_UNREACHABLE(11003,"The destination host was unreachable."),
			IP_DEST_PROT_UNREACHABLE(11004,"The destination protocol was unreachable."),
			IP_DEST_PORT_UNREACHABLE(11005,"The destination port was unreachable."),
			IP_NO_RESOURCES(11006,"Insufficient IP resources were available."),
			IP_BAD_OPTION(11007,"A hardware error occurred."),
			IP_HW_ERROR(11008,"A hardware error occurred."),
			IP_PACKET_TOO_BIG(11009,"The packet was too big."),
			IP_REQ_TIMED_OUT(11010,"The request timed out."),
			IP_BAD_REQ(11011,"A bad request."),
			IP_BAD_ROUTE(11012,"A bad route."),
			IP_TTL_EXPIRED_TRANSIT(11013,"The time to live (TTL) expired in transit."),
			IP_TTL_EXPIRED_REASSEM(11014,"The time to live expired during fragment reassembly."),
			IP_PARAM_PROBLEM(11015,"A parameter problem."),
			IP_SOURCE_QUENCH(11016,"Datagrams are arriving too fast to be processed and datagrams may have been discarded."),
			IP_OPTION_TOO_BIG(11007,"An IP option was too big."),
			IP_BAD_DESTINATION(11018,"A bad destination."),
			IP_GENERAL_FAILURE(11050,"A general failure. This error can be returned for some malformed ICMP packets."),
			UNKNOWN(99999,"Unknown Error."),
			;
			
			private final int id;
			private final String msg;
			
			private Status(final int id, String msg) {
				this.id = id;
				this.msg = msg;
			}
			
			public int getId() {
				return this.id;
			}
			
			public String getMsg() {
				return this.msg;
			}
			
			//id to enum
			public static Status getStatus(final int id) {
				Status[] stautses = Status.values();
				for (Status stauts : stautses) {
					if (stauts.getId() == id) {
						return stauts;
					}
				}
				return null;
			}
		}
	}
	
	/**
	 * ICMPリクエスト内容
	 *
	 */
	private static class ICMPReq{
		//対象ホスト
		public String host;
		//IPアドレス IPv4/IPv6の判断に使用
		public InetAddress ip;
		//実行回数
		public int count;
		//ICMPHandler クローズ用に保持
		public Pointer imcpHandler;
		//終了判断用のhandleオブジェクト
		public WinNT.HANDLE event;
		//レスポンス格納先のポインタ
		public Pointer response;
		//packet size
		public int size;
	}
	
	/**
	 * 編集結果のメッセージを返却
	 * @return
	 */
	public List<String> getResultMessages() {
		return resultMessages;
	}

	/**
	 * Fping実行
	 * 
	 * @param hosts 送信先IPアドレス
	 * @param version ICMPバージョン(4 or 6)
	 * @param sentCount 送信回数
	 * @param sentInterval　送信間隔
	 * @param timeout タイムアウト時間
	 * @param bytes 送信バイト数
	 * @param result 結果メッセージ ※使用しない　結果はエラーメッセージにセット
	 * @param error エラーメッセージ　※成功失敗を含めた結果メッセージ
	 * @return システムエラーの場合のみfalse
	 */
	public boolean fping(
			HashSet<String> hosts,
			int version, 
			int sentCount,
			int sentInterval,
			int timeout,
			int bytes,
			final ArrayList<String> result, 
			final ArrayList<String> error) {
		
		FPingUtils.FPingOption option = new FPingUtils.FPingOption();
		option.count = sentCount;
		option.interval = sentInterval;
		option.timeout = timeout;
		option.packetSize = bytes;
		Iterator<String> itr = hosts.iterator();
		List<String> hostList = new ArrayList<String>();
		
		while(itr.hasNext()) {
			hostList.add(itr.next());
		}
		//実行
		try {
			fpingImpl(hostList, option);
		
			error.addAll(this.resultMessages);
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
		
		return true;
	}
	
	/**
	 * ping実行
	 * 
	 * @param hostList 宛先ホスト
	 * @param options 実行パラメータ
	 * @return
	 */
	private void fpingImpl(List<String> hostList, FPingOption options) {
		
		//結果メッセージ
		resultMessages = new ArrayList<>();
		
		//実行結果格納
		resultMap = new LinkedHashMap<>();
		
		for (String host : hostList) {
			resultMap.put(host, new ArrayList<ICMPRes>());
		}
		
		//実行中のICMPリクエスト内容を保持する配列
		List<ICMPReq> icmpReqs = new ArrayList<>();
		
		//現在のリピート回数
		int repeat = -1;
		//前回pingの実行時間　intervalの判断に使用
		long befICMPReqTime = 0;
		
		while (true) {
			//ICMPリクエスト実行結果チェック
			setICMPResult(icmpReqs);
			
			if (repeat >= (options.count -1) && icmpReqs.size() == 0) {
				//countまでリピート済　かつ　ICMPリクエストすべて処理済の時
				//終了
				break;
			}
			
			if (repeat < (options.count -1) 
					&& (System.currentTimeMillis() - befICMPReqTime) >= options.interval ) {
				//前回実行時間よりinterval経過している場合に実施
				
				repeat++;
				
				for (int i = 0; i < hostList.size(); i++) {
					String host = hostList.get(i);
					String id = host + "_" + repeat + "_" + Thread.currentThread().getId();
					
					//終了検知用Event
					WinNT.HANDLE event = Kernel32.INSTANCE.CreateEvent(null, true, false, id);
					
					InetAddress ip = null;
					Object[] pingRef = null;
					try {
						//IPv4・IPv6の判断（機能としては名前解決できるが、HinemosからはIPアドレスのみ渡される）
						//Hinemosから渡されるIpv6はフル桁のみ（省略形::は来ない）
						ip = InetAddress.getByName(host);
						
						if (ip instanceof Inet4Address) {
							//ICMPリクエストを実行し、実行時のPointerを受け取る
							pingRef = pingRequest2V4((Inet4Address)ip, options, event);
						} else {
							pingRef = pingRequest2V6((Inet6Address)ip, options, event);
						}
					} catch (UnknownHostException e) {
						logger.warn(e);
						Kernel32.INSTANCE.CloseHandle(event);
						continue;
					}
					
					//ICMPリクエストオブジェクトを生成
					ICMPReq icmpReq = new ICMPReq();
					icmpReq.host = host;
					icmpReq.ip = ip;
					icmpReq.count = repeat;
					icmpReq.event = event;
					icmpReq.response = (Pointer) pingRef[0];
					icmpReq.imcpHandler = (Pointer) pingRef[1];
					icmpReqs.add(icmpReq);
					
				}
				
				//interval実行用に現在時刻を保持
				befICMPReqTime = System.currentTimeMillis();
			}
			
			try {
				//レスポンス終了／次のICMPリクエスト実行時刻を待つため、sleep
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// ignore
			}
		}
		
		
		//ping実行集計メッセージ編集
		for (Entry<String, List<ICMPRes>> entry: this.resultMap.entrySet()) {
			//形式 は「IPアドレス : レスポンス(1回目) レスポンス(2回目) レスポンス(3回目) ....」
			//疎通可の時　RTT(x.xx形式)
			//疎通NGの時　-
			//上記以外の時 空白  
			
			StringBuffer sb = new StringBuffer();
			sb.append(entry.getKey()).append(" : ");
			for (ICMPRes res : entry.getValue()) {
				if (res.status == Status.IP_SUCCESS) {
					sb.append(String.format(" %d.00 ", res.rtt));
				} else if (res.status == Status.UNKNOWN) {
					
				} else {
					sb.append(" - ");
				}
			}
			this.resultMessages.add(sb.toString());
		}
		
	}
	
	/**
	 * ICMPリクエストの実行結果をチェックし、レスポンス結果をセット
	 * 
	 * @param resultMap レスポンス結果格納用
	 * @param icmpReqs ICMPリクエスト配列
	 */
	private void setICMPResult(List<ICMPReq> icmpReqs) {
		for (Iterator<ICMPReq> ite = icmpReqs.iterator(); ite.hasNext();) {
			ICMPReq icmpReq = ite.next();
			
			//ICMPリスエストの実行結果をチェック
			int result = Kernel32.INSTANCE.WaitForSingleObject(icmpReq.event, 0);
			
			switch(result){
			case 0x00000102://WAIT_TIMEOUT
				//実行中の場合
				//次回チェックするため、何もしない
				
				break;
				
			case 0x00000000://WAIT_OBJECT_0 Signal状態
				//実行済の場合
				
				ICMPRes respose = null;
				if (icmpReq.ip instanceof Inet4Address) {
					//Pointerを実行結果のオブジェクトに変換
					respose = this.getResponseV4(icmpReq.response, icmpReq);
				} else {
					respose = this.getResponseV6(icmpReq.response, icmpReq);
				}
				
				//ICMPリクエストのHandlerをClose
				this.closeICMP(icmpReq.imcpHandler);
				//イベントのHandleをクローズ
				Kernel32.INSTANCE.CloseHandle(icmpReq.event);
				//レスポンス結果のセットが終わったため、ICMPリクエスト配列から削除
				ite.remove();
				
				//返却結果をセット
				resultMap.get(icmpReq.host).add(respose);
				
				break;
				
			case 0xFFFFFFFF://WAIT_FAILED
			case 0x00000080://WAIT_ABANDONED
			default:
				//エラーの場合
				logger.warn("wait failid" + result);
				
				//レスポンス結果編集（エラー）
				respose = getErroRes(icmpReq, result);
				
				//ICMPリクエストのHandlerをClose
				this.closeICMP(icmpReq.imcpHandler);
				//イベントのHandleをクローズ
				Kernel32.INSTANCE.CloseHandle(icmpReq.event);
				//レスポンス結果のセットが終わったため、ICMPリクエスト配列から削除
				ite.remove();
				
				//返却結果をセット
				resultMap.get(icmpReq.host).add(respose);
				
				break;
			}
		}
	}
	
	/**
	 * ICMPリクエスト送信(v4)
	 * 
	 * @param inetAddress 送信先ホスト
	 * @param params 実行オプション
	 * @param event 終了判断用eventHandler
	 * @return [0]：replyDataのPointer　[1]:ICPMリクエストのHandler
	 */
	private Object[] pingRequest2V4(Inet4Address inetAddress, FPingOption params, WinNT.HANDLE event) {
		
		//API実行用の引数を編集
		
		//IP(v4)
		ICMPHelper.IpAddr ipaddr = new ICMPHelper.IpAddr();
		ipaddr.bytes = inetAddress.getAddress();
		
		//領域確保
		int replyDataSize = params.packetSize + (new ICMPHelper.IcmpEchoReply().size () + 8);;
		Pointer sendData  = new Memory (params.packetSize);
		Pointer replyData = new Memory (replyDataSize);
		
		//実行オプションを編集
		ICMPHelper.OptionInformation option = new ICMPHelper.OptionInformation();
		option.ttl = (byte) params.ttl;
		option.tos = (byte) params.tos;
		option.flags = (byte) 0;
		option.optionsSize = (byte) 0;
		option.optionsData = null;
		
		//ICMP Handle(v4)作成
		Pointer icmpHandle = iphelper.IcmpCreateFile();
		
		//API実行(v4)
		iphelper.IcmpSendEcho2(
			icmpHandle,
			event,
			null,
			null,
			ipaddr,
			sendData,
			(short) params.packetSize,
			option,
			replyData,
			replyDataSize,
			params.timeout
		);
		
		//結果確認、close用のPointerを返却
		return new Object[]{replyData, icmpHandle};
	}
	
	/**
	 * ICMPリクエスト送信(v6)
	 * 
	 * @param inetAddress 送信先ホスト
	 * @param fpoption 実行オプション
	 * @param event 終了判断用eventHandler
	 * @return [0]：replyDataのPointer　[1]:ICPMリクエストのHandler
	 */
	private Object[] pingRequest2V6(Inet6Address inetAddress, FPingOption fpoption, WinNT.HANDLE event) {
		
		//API実行用の引数を編集
		
		if (this.anyAddress == null) {
			try {
				this.anyAddress = (Inet6Address)InetAddress.getByName("::0");
			} catch (UnknownHostException e) {
				//発生しない
			}
		}
		
		final int AF_INET6 = 23;
		
		//src IP(v6)
		ICMPHelper.SockAddrIn6 srcAddr = new ICMPHelper.SockAddrIn6();
		srcAddr.sin6Family = AF_INET6;
		srcAddr.sin6Port = 0;
		srcAddr.sin6FlowInfo = 0;
		srcAddr.sin6Addr.bytes = this.anyAddress.getAddress();
		srcAddr.sin6_scope_id = this.anyAddress.getScopeId();;
		
		//dest IP(v6)
		ICMPHelper.SockAddrIn6 destAddr = new ICMPHelper.SockAddrIn6();
		destAddr.sin6Family = AF_INET6;
		destAddr.sin6Port = 0;
		destAddr.sin6FlowInfo = 0;
		destAddr.sin6Addr.bytes = inetAddress.getAddress();
		destAddr.sin6_scope_id = inetAddress.getScopeId();;
		
		//領域確保
		int replyDataSize = fpoption.packetSize + new ICMPHelper.Icmpv6EchoReplyLh().size();
		Pointer sendData  = new Memory (fpoption.packetSize);
		Pointer replyData = new Memory (replyDataSize);
		
		//実行オプションを編集
		ICMPHelper.OptionInformation option = new ICMPHelper.OptionInformation();
		option.ttl = (byte) fpoption.ttl;
		option.tos = (byte) fpoption.tos;
		option.flags = (byte) 0;
		option.optionsSize = (byte) 0;
		option.optionsData = null;
		
		//ICMP Handle(v6)作成
		Pointer icmpHandle = iphelper.Icmp6CreateFile();
		
		//API実行(v6)
		iphelper.Icmp6SendEcho2(
			icmpHandle,
			event,
			null,
			null,
			srcAddr,
			destAddr,
			sendData,
			(short) fpoption.packetSize,
			option,
			replyData,
			replyDataSize,
			fpoption.timeout
		);
		
		//結果確認、close用のPointerを返却
		return new Object[]{replyData, icmpHandle};
	}
	
	/**
	 * ICMPクローズ(v4/v6共通)
	 * 
	 * @param icmpHandle createしたICMP　Handle
	 */
	private void closeICMP(Pointer icmpHandle) {
		iphelper.IcmpCloseHandle(icmpHandle);
	}
	
	/**
	 * ICMP実行結果のポインタをJavaオブジェクトへ変換(v4)
	 * 
	 * @param replyData ICMP実行結果
	 * @param icmpReq ICMPリクエスト内容
	 * @return
	 */
	private ICMPRes getResponseV4(Pointer replyData, ICMPReq icmpReq) {
		
		ICMPHelper.IcmpEchoReply icmpEchoReply = new ICMPHelper.IcmpEchoReply(replyData);
		
		ICMPRes response = new ICMPRes();
		
		response.host = icmpReq.host;
		byte[] add = icmpEchoReply.address.bytes;
		response.ip = String.format ("%d.%d.%d.%d",
				add[0] & 0xff,add[1] & 0xff,add[2] & 0xff, add[3] & 0xff
		);
		response.count = icmpReq.count;
		response.statusCd = icmpEchoReply.status;
		response.status = Status.getStatus(response.statusCd);
		response.rtt = icmpEchoReply.roundTripTime;
		response.size = icmpEchoReply.dataSize;
		response.ttl = icmpEchoReply.options.ttl & 0xff;
		
		return response;
	}
	
	/**
	 * ICMP実行結果のポインタをJavaオブジェクトへ変換(v6)
	 * 
	 * @param replyData ICMP実行結果
	 * @param icmpReq ICMPリクエスト内容
	 * @return
	 */
	private ICMPRes getResponseV6(Pointer replyData, ICMPReq icmpReq) {
		
		ICMPHelper.Icmpv6EchoReplyLh icmpEchoReply = new ICMPHelper.Icmpv6EchoReplyLh(replyData);
		
		ICMPRes response = new ICMPRes();
		
		response.host = icmpReq.host;
		short[] add = icmpEchoReply.address.sin6Addr;
		response.ip = String.format ("%04X:%04X:%04X:%04X:%04X:%04X:%04X:%04X",
				add[0],add[1],add[2],add[3],add[4],add[5],add[6],add[7]
		);
		response.count = icmpReq.count;
		response.statusCd = icmpEchoReply.status;
		response.status = Status.getStatus(response.statusCd);
		response.rtt = icmpEchoReply.roundTripTime;
		response.size = icmpReq.size;
		response.ttl = 0;
		
		return response;
	}
	
	/**
	 * エラー時のリクエスト結果を生成
	 * 
	 * @param icmpReq ICMPリクエスト内容
	 * @param errcd エラーコード
	 * @return
	 */
	private ICMPRes getErroRes(ICMPReq icmpReq, int errcd) {
		
		ICMPRes response = new ICMPRes();
		
		response.host       = icmpReq.host;
		response.ip         = icmpReq.host;
		response.count      = icmpReq.count;
		response.statusCd   = errcd;
		response.status     = Status.getStatus(99999);
		response.rtt        = 0;
		response.size       = icmpReq.size;
		response.ttl        = 0;
		
		return response;
	}
	
	//for test
	public static void main(String[] args) {
		
		PropertyConfigurator.configure("resource\\log4j.properties");
		
		while (true) {
			for (int i = 0; i < 20; i++) {
				
				new FpingThread().start();
			}
			new FpingThread().run();
			try {
				Thread.sleep(1000 * 30);
			} catch (InterruptedException e) {
				//ignore
			}
		}
	}
	
	//for test
	private static class FpingThread extends Thread {
		@Override
		public void run() {
			Logger logger = Logger.getLogger(FPingUtils.class); 
			logger.info(Thread.currentThread().getId() + "run" );
			
			try {
				FPingUtils util = new FPingUtils();
				
				//送信先ホスト
				List<String> hostList = new ArrayList<>();
				hostList.add("google.com");
				hostList.add("127.0.0.1");
				hostList.add("10.0.2.203");
				hostList.add("127.0.0.1");
				hostList.add("fe80::1d14:64ba:609d:d24e%17");
				hostList.add("fe80:0:0:0:3811:91f1:78f2:edec");
				hostList.add("0:0:0:0:0:0:0:1");
				hostList.add("::1");
				
				//Fping実行オプション
				FPingOption options = new FPingOption();
				options.interval = 1000 * 1;
				options.count = 5;
				options.packetSize = 1024;
				options.timeout = 1000 * 1;
				options.ttl = 255;
				
				//fping実行
				util.fpingImpl(hostList, options);
				
				StringBuilder sb = new StringBuilder();
				
				//実行結果を出力
				sb.append(" result \n");
				for (String msg: util.getResultMessages()) {
					sb.append(msg).append("\n");
				}
				
				logger.info(Thread.currentThread().getId() + " " + sb.toString());
			} catch (Throwable t) {
				logger.fatal(t);
			}
		}
	}; 
}