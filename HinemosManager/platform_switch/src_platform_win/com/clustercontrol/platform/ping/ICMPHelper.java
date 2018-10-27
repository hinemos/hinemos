/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.platform.ping;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.ByValue;
import com.sun.jna.Union;
import com.sun.jna.platform.win32.WinNT;

/**
 * IP Helper(Iphlpapi.dll)のAPI定義
 * 
 * @see https://msdn.microsoft.com/ja-jp/library/windows/desktop/aa366071(v=vs.85).aspx
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
public interface ICMPHelper extends Library {

	/**
	 * icmp生成(v4)
	 * 
	 * @return
	 */
	//HANDLE IcmpCreateFile(void);
	public Pointer IcmpCreateFile();
	
	/**
	 * ICMPクローズ(v4/v6)
	 * 
	 * @param icmpHandle
	 * @return
	 */
	//BOOL IcmpCloseHandle(
	//	  _In_ HANDLE IcmpHandle
	//);
	public boolean IcmpCloseHandle(Pointer icmpHandle);
	
	/**
	 * icmp送信(v4)
	 * 
	 * @return
	 */
	//	DWORD WINAPI IcmpSendEcho2(
	//		  _In_     HANDLE                 IcmpHandle,
	//		  _In_opt_ HANDLE                 Event,
	//		  _In_opt_ PIO_APC_ROUTINE        ApcRoutine,
	//		  _In_opt_ PVOID                  ApcContext,
	//		  _In_     IPAddr                 DestinationAddress,
	//		  _In_     LPVOID                 RequestData,
	//		  _In_     WORD                   RequestSize,
	//		  _In_opt_ PIP_OPTION_INFORMATION RequestOptions,
	//		  _Out_    LPVOID                 ReplyBuffer,
	//		  _In_     DWORD                  ReplySize,
	//		  _In_     DWORD                  Timeout
	//	);
	public int IcmpSendEcho2(
			Pointer icmpHandle,
			WinNT.HANDLE event,
			Pointer apcRoutine,
			Pointer apcContext,
			IpAddr destinationAddress,
			Pointer requestData,
			short requestSize,
			OptionInformation requestOptions,
			Pointer      replyBuffer,
			long         replySize,
			long         timeout
	);
	
	/**
	 * IPアドレス構造体
	 * 
	 * @return
	 */
	//  typedef struct {
	//  union {
	//    struct {
	//      u_char s_b1,s_b2,s_b3,s_b4;
	//    } S_un_b;
	//    struct {
	//      u_short s_w1,s_w2;
	//    } S_un_w;
	//    u_long S_addr;
	//  } S_un;
	//	} IPAddr;
	public static class IpAddr extends Structure implements ByValue {
		
		public byte[] bytes = new byte[4];
	
		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList (new String[] {
					"bytes"
			});
		}
	}
	
	/**
	 * IcmpSendEcho2実行オプション構造体
	 * 
	 * @return
	 */
//	typedef struct _IP_OPTION_INFORMATION32 {
//		  UCHAR              Ttl;
//		  UCHAR              Tos;
//		  UCHAR              Flags;
//		  UCHAR              OptionsSize;
//		  UCHAR * POINTER_32 OptionsData;
//		} IP_OPTION_INFORMATION32, *PIP_OPTION_INFORMATION32;
	public static class OptionInformation extends Structure implements ByValue {
		public byte ttl;
		public byte tos;
		public byte flags;
		public byte optionsSize;
		public Pointer optionsData;
		
		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList (new String[] {
				"ttl", "tos", "flags", "optionsSize", "optionsData"
			});
		}
	}
	
	/**
	 * IcmpSendEcho2 replay構造体
	 * 
	 * @return
	 */
	//	typedef struct icmp_echo_reply {
	//		  IPAddr                       Address;
	//		  ULONG                        Status;
	//		  ULONG                        RoundTripTime;
	//		  USHORT                       DataSize;
	//		  USHORT                       Reserved;
	//		  PVOID                        Data;
	//		  struct ip_option_information  Options;
	//		} ICMP_ECHO_REPLY, *PICMP_ECHO_REPLY;
	public class IcmpEchoReply extends Structure {
		public IpAddr address;
		public int status;
		public int roundTripTime;
		public short dataSize;
		public short reserved;
		public Pointer data;
		public OptionInformation options;
		
		public IcmpEchoReply(){
		}
		
		public IcmpEchoReply(Pointer p){
			useMemory(p);
			read();
		}
		
		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList (new String[] {
				  "address", "status", "roundTripTime", "dataSize", "reserved", "data", "options"
			});
		}
	}
	
	//For ICMPv6
	//HANDLE Icmp6CreateFile(void);
	public Pointer Icmp6CreateFile();
	
//	DWORD Icmp6SendEcho2(
//			  _In_     HANDLE                 IcmpHandle,
//			  _In_opt_ HANDLE                 Event,
//			  _In_opt_ PIO_APC_ROUTINE        ApcRoutine,
//			  _In_opt_ PVOID                  ApcContext,
//			  _In_     struct sockaddr_in6    *SourceAddress,
//			  _In_     struct sockaddr_in6    *DestinationAddress,
//			  _In_     LPVOID                 RequestData,
//			  _In_     WORD                   RequestSize,
//			  _In_opt_ PIP_OPTION_INFORMATION RequestOptions,
//			  _Out_    LPVOID                 ReplyBuffer,
//			  _In_     DWORD                  ReplySize,
//			  _In_     DWORD                  Timeout
//			);
	public int Icmp6SendEcho2(
			Pointer icmpHandle,
			WinNT.HANDLE event,
			Pointer apcRoutine,
			Pointer apcContext,
			SockAddrIn6 sourceAddress,
			SockAddrIn6 destinationAddress,
			Pointer requestData,
			short requestSize,
			OptionInformation requestOptions,
			Pointer      replyBuffer,
			long         replySize,
			long         timeout
	);
	
//	typedef struct sockaddr_in {
//		  ADDRESS_FAMILY sin6_family;
//		  USHORT         sin6_port;
//		  ULONG          sin6_flowinfo;
//		  IN6_ADDR       sin6_addr;
//		  union {
//		    ULONG    sin6_scope_id;
//		    SCOPE_ID sin6_scope_struct;
//		  };
//		} SOCKADDR_IN6, *PSOCKADDR_IN6;
	public static class SockAddrIn6 extends Structure {
		public byte sin6Family;
		public short sin6Port;
		public int sin6FlowInfo;
		public In6Addr sin6Addr;
		public int sin6_scope_id;

		
		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList (new String[] {
					"sin6Family", "sin6Port", "sin6FlowInfo", "sin6Addr","sin6_scope_id"
			});
		}
		
		public static class UNION extends Union {
			public static class ByReference extends UNION
				implements Structure.ByReference {
			}
			public int sin6_scope_id;
		}
	}
	
	//	typedef struct in6_addr {
	//		  union {
	//		    UCHAR  Byte[16];
	//		    USHORT Word[8];
	//		  } u;
	//		} IN6_ADDR, *PIN6_ADDR, *LPIN6_ADDR;
	public static class In6Addr extends Structure implements ByValue{
		
		public byte[] bytes = new byte[16];
		
		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList (new String[] {
					"bytes"
			});
		}
	}
	
	/**
	 * Icmp6SendEcho2 replay構造体
	 * 
	 * @return
	 */
	//	typedef struct icmpv6_echo_reply_lh {
	//		  IPV6_ADDRESS_EX Address;
	//		  ULONG           Status;
	//		  unsigned int    RoundTripTime;
	//		} ICMPV6_ECHO_REPLY, *PICMPV6_ECHO_REPLY;
	public class Icmpv6EchoReplyLh extends Structure {
		public IPV6AddressExLh address;
		public int status;
		public int roundTripTime;
		
		public Icmpv6EchoReplyLh(){
		}
		
		public Icmpv6EchoReplyLh(Pointer p){
			useMemory(p);
			read();
		}
		
		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList (new String[] {
				  "address", "status", "roundTripTime"
			});
		}
	}
	
	//	typedef struct _IPV6_ADDRESS_EX_LH {
	//		  USHORT sin6_port;
	//		  ULONG  sin6_flowinfo;
	//		  USHORT sin6_addr[8];
	//		  ULONG  sin6_scope_id;
	//		} IPV6_ADDRESS_EX, *PIPV6_ADDRESS_EX;
	public static class IPV6AddressExLh extends Structure{
		public short sin6Port;
		public int sin6FlowInfo;
		public short[] sin6Addr = new short[8];
		public int sin6ScopeId;
		
		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList (new String[] {
					"sin6Port", "sin6FlowInfo", "sin6Addr", "sin6ScopeId"
			});
		}
	}
}
