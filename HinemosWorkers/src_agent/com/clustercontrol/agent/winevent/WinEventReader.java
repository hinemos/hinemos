package com.clustercontrol.agent.winevent;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.clustercontrol.agent.Win32Error;
import com.clustercontrol.agent.util.AgentProperties;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.LONGLONG;
import com.sun.jna.platform.win32.WinDef.PVOID;
import com.sun.jna.platform.win32.WinError;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;

public class WinEventReader {

	private static Log m_log = LogFactory.getLog(WinEventReader.class);

	public interface kernel32 extends Library {
		kernel32 INSTANCE = (kernel32) Native.loadLibrary("kernel32", kernel32.class);

		int GetLastError();
	}

	public interface wevtapi extends Library {
		wevtapi INSTANCE = (wevtapi) Native.loadLibrary("wevtapi", wevtapi.class);

		// EVT_QUERY_FLAGS
		public static final int EvtQueryChannelPath = 0x1;
		public static final int EvtQueryFilePath = 0x2;
		public static final int EvtQueryForwardDirection = 0x100;
		public static final int EvtQueryReverseDirection = 0x200;
		public static final int EvtQueryTolerateQueryErrors = 0x1000;

		// EVT_SEEK_FLAGS
		public static final int EvtSeekRelativeToFirst = 1;
		public static final int EvtSeekRelativeToLast = 2;
		public static final int EvtSeekRelativeToCurrent = 3;
		public static final int EvtSeekRelativeToBookmark = 4;
		public static final int EvtSeekOriginMask = 7;
		public static final int EvtSeekStrict = 0x10000;

		// EVT_RENDER_FLAGS
		public static final int EvtRenderEventValues = 0;
		public static final int EvtRenderEventXml = 1;
		public static final int EvtRenderBookmark = 2;

		// EVT_FORMAT_MESSAGE_FLAGS
		public static final int EvtFormatMessageEvent = 1;
		public static final int EvtFormatMessageLevel = 2;
		public static final int EvtFormatMessageTask = 3;
		public static final int EvtFormatMessageOpcode = 4;
		public static final int EvtFormatMessageKeyword = 5;
		public static final int EvtFormatMessageChannel = 6;
		public static final int EvtFormatMessageProvider = 7;
		public static final int EvtFormatMessageId = 8;
		public static final int EvtFormatMessageXml = 9;

		// EVT_RENDER_CONTEXT_FLAGS
		public static final int EvtRenderContextValues = 0;
		public static final int EvtRenderContextSystem = 1;
		public static final int EvtRenderContextUser = 2;

		//EVT_CHANNEL_CONFIG_PROPERTY_ID
		//public static final int EvtChannelConfigEnabled                = 0;
		//public static final int EvtChannelConfigIsolation              = 1;
		//public static final int EvtChannelConfigType                   = 2;
		//public static final int EvtChannelConfigOwningPublisher        = 3;
		//public static final int EvtChannelConfigClassicEventlog        = 4,
		//public static final int EvtChannelConfigAccess                 = 5,
		//public static final int EvtChannelLoggingConfigRetention       = 6,
		public static final int EvtChannelLoggingConfigAutoBackup = 7;
		//public static final int EvtChannelLoggingConfigMaxSize         = 8;
		public static final int EvtChannelLoggingConfigLogFilePath = 9;
		//public static final int EvtChannelPublishingConfigLevel        = 10;
		//public static final int EvtChannelPublishingConfigKeywords     = 11;
		//public static final int EvtChannelPublishingConfigControlGuid  = 12;
		//public static final int EvtChannelPublishingConfigBufferSize   = 13;
		//public static final int EvtChannelPublishingConfigMinBuffers   = 14;
		//public static final int EvtChannelPublishingConfigMaxBuffers   = 15;
		//public static final int EvtChannelPublishingConfigLatency      = 16;
		//public static final int EvtChannelPublishingConfigClockType    = 17;
		//public static final int EvtChannelPublishingConfigSidType      = 18;
		//public static final int EvtChannelPublisherList                = 19;
		//public static final int EvtChannelPublishingConfigFileMax      = 20;
		//public static final int EvtChannelConfigPropertyIdEND          = 21;

		public static final int INFINITE = -1;
		
		// EVT_HANDLE WINAPI EvtQuery(
		// _In_ EVT_HANDLE Session,
		// _In_ LPCWSTR Path,
		// _In_ LPCWSTR Query,
		// _In_ DWORD Flags
		// );

		HANDLE EvtQuery(HANDLE Session, WString Path, WString Query, int Flags);

		// BOOL WINAPI EvtSeek(
		// _In_ EVT_HANDLE ResultSet,
		// _In_ LONGLONG Position,
		// _In_ EVT_HANDLE Bookmark,
		// _In_ DWORD Timeout,
		// _In_ DWORD Flags
		// );

		boolean EvtSeek(HANDLE ResultSet, LONGLONG Position, HANDLE Bookmark, int Timeout, int Flags);

		// BOOL WINAPI EvtNext(
		// _In_ EVT_HANDLE ResultSet,
		// _In_ DWORD EventArraySize,
		// _In_ EVT_HANDLE* EventArray,
		// _In_ DWORD Timeout,
		// _In_ DWORD Flags,
		// _Out_ PDWORD Returned
		// );

		boolean EvtNext(HANDLE ResultSet, int EventArraySize, HANDLE[] EventArray, DWORD Timeout, int Flags,
				IntByReference Returned);

		// BOOL WINAPI EvtRender(
		// _In_ EVT_HANDLE Context,
		// _In_ EVT_HANDLE Fragment,
		// _In_ DWORD Flags,
		// _In_ DWORD BufferSize,
		// _In_ PVOID Buffer,
		// _Out_ PDWORD BufferUsed,
		// _Out_ PDWORD PropertyCount
		// );

		boolean EvtRender(HANDLE Context, HANDLE Fragment, int Flags, int BufferSize, PVOID Buffer,
				IntByReference BufferUsed, IntByReference PropertyCount);

		// BOOL WINAPI EvtFormatMessage(
		// _In_ EVT_HANDLE PublisherMetadata,
		// _In_ EVT_HANDLE Event,
		// _In_ DWORD MessageId,
		// _In_ DWORD ValueCount,
		// _In_ PEVT_VARIANT Values,
		// _In_ DWORD Flags,
		// _In_ DWORD BufferSize,
		// _In_ LPWSTR Buffer,
		// _Out_ PDWORD BufferUsed
		// );

		boolean EvtFormatMessage(HANDLE PublisherMetadata, HANDLE Event, int MessageId, int ValueCount, Pointer values,
				int Flags, int BufferSize, Pointer Buffer, IntByReference BufferUsed);

		// EVT_HANDLE WINAPI EvtCreateBookmark(
		// _In_opt_ LPCWSTR BookmarkXml
		// );

		HANDLE EvtCreateBookmark(WString BookmarkXml);

		// EVT_HANDLE WINAPI EvtCreateRenderContext(
		// _In_ DWORD ValuePathsCount,
		// _In_ LPCWSTR *ValuePaths,
		// _In_ DWORD Flags
		// );

		HANDLE EvtCreateRenderContext(int ValuePathsCount, Pointer ValuePaths, int Flags);

		// BOOL WINAPI EvtUpdateBookmark(
		// _In_ EVT_HANDLE Bookmark,
		// _In_ EVT_HANDLE Event
		// );
		boolean EvtUpdateBookmark(HANDLE Bookmark, HANDLE Event);

		// EVT_HANDLE WINAPI EvtOpenPublisherMetadata(
		// _In_opt_ EVT_HANDLE Session,
		// _In_ LPCWSTR PublisherIdentity,
		// _In_opt_ LPCWSTR LogFilePath,
		// _In_ LCID Locale,
		// _In_ DWORD Flags
		// );

		HANDLE EvtOpenPublisherMetadata(HANDLE Session, WString PublisherIdentity, WString LogFilePath, int Lolcae,
				int Flags);

		// BOOL WINAPI EvtClose(
		// _In_ EVT_HANDLE Object
		// );

		boolean EvtClose(HANDLE Object);

		//EVT_HANDLE WINAPI EvtOpenChannelConfig(
		//  _In_ EVT_HANDLE Session,
		//  _In_ LPCWSTR    ChannelPath,
		//  _In_ DWORD      Flags
		//);
		
		HANDLE EvtOpenChannelConfig(HANDLE Session, WString ChannelPath, int Flags);
		
		//BOOL WINAPI EvtGetChannelConfigProperty(
		//  _In_  EVT_HANDLE                     ChannelConfig,
		//  _In_  EVT_CHANNEL_CONFIG_PROPERTY_ID PropertyId,
		//  _In_  DWORD                          Flags,
		//  _In_  DWORD                          PropertyValueBufferSize,
		//  _In_  PEVT_VARIANT                   PropertyValueBuffer,
		//  _Out_ PDWORD                         PropertyValueBufferUsed
		//);
		
		boolean EvtGetChannelConfigProperty(HANDLE ChannelConfig, int PropertyId, int Flags,
			int PropertyValueBufferSize, Pointer PropertyValueBuffer, IntByReference PropertyValueBufferUsed);
	}

	private static final String SECOND_CHALLENENGE = "monitor.winevent.second.challenge.render";
	private static boolean secondChallenge = false;
	
	public WinEventReader() {
		String secondChallengeStr = AgentProperties.getProperty(SECOND_CHALLENENGE, "false");
		try {
			secondChallenge = Boolean.parseBoolean(secondChallengeStr);
		} catch (Exception e) {
			m_log.info("monitor.winevent.second.challenge.render uses " + secondChallenge + ". (" + secondChallengeStr + " is not collect)");
		}
	}
	
	/**
	 * 引数のチャネルの最後のEventRecordIDでブックマークファイルを作成する
	 * 
	 * @param bookmarkFileName
	 *            ブックマークファイル名
	 * @param logName
	 *            チャネル名
	 */
	public void updateBookmark(String bookmarkFileName, String logName) throws Win32Exception, IOException {
		String query = "<QueryList><Query><Select Path='" + WinEventMonitor.pergeEventLogNameEnclosure(logName) + "'>*</Select></Query></QueryList>";
		
		HANDLE hResult = null;
		HANDLE hBookmark = null;
		try {
			WString wQuery = new WString(query);
			hResult = wevtapi.INSTANCE.EvtQuery(null, null, wQuery, wevtapi.EvtQueryChannelPath | wevtapi.EvtQueryTolerateQueryErrors);
			if (hResult == null) {
				m_log.warn("updateBookmark() EvtQuery error=" + Win32Error.getMessage(getLastError()));
				throw new Win32Exception(getLastError());
			}

			if (!wevtapi.INSTANCE.EvtSeek(hResult, new LONGLONG(0), null, 0, wevtapi.EvtSeekRelativeToLast)) {
				m_log.warn("updateBookmark() EvtSeek error=" + Win32Error.getMessage(getLastError()));
				throw new Win32Exception(getLastError());
			}

			HANDLE[] saveHandles = new HANDLE[1];
			IntByReference returnedHandleSize = new IntByReference(0);
			try {
				if (!wevtapi.INSTANCE.EvtNext(hResult, 1, saveHandles, new DWORD(wevtapi.INFINITE), 0, returnedHandleSize)) {
					if (WinError.ERROR_NO_MORE_ITEMS == getLastError()) {
						// ERROR_NO_MORE_ITEMS の場合、引数チャネルのイベント数が0件である。
						// ブックマークファイルが存在しなければ、RecordId を 0 としてブックマークファイルを作成する。
						if(!new File(bookmarkFileName).exists()) {
							String bookmarkString = "<BookmarkList><Bookmark Channel='" + WinEventMonitor.pergeEventLogNameEnclosure(logName) +
									"' RecordId='0' IsCurrent='true'/></BookmarkList>";
							saveBookmarkFile(bookmarkFileName, bookmarkString);
							m_log.info("updateBookmark() create " + bookmarkFileName + " with RecordId 0");
						}
						return;
					}
					m_log.warn("updateBookmark() EvtNext error=" + Win32Error.getMessage(getLastError()));
					throw new Win32Exception(getLastError());
				}
				hBookmark = wevtapi.INSTANCE.EvtCreateBookmark(null);
				if (hBookmark == null) {
					m_log.warn("updateBookmark() EvtCreateBookmark error=" + Win32Error.getMessage(getLastError()));
					throw new Win32Exception(getLastError());
				}
				if (!wevtapi.INSTANCE.EvtUpdateBookmark(hBookmark, saveHandles[0])) {
					m_log.warn("updateBookmark() EvtUpdateBookmark error=" + Win32Error.getMessage(getLastError()));
					throw new Win32Exception(getLastError());
				}
			} finally {
				// EvtNext から取得したイベントハンドルは、削除。
				// https://docs.microsoft.com/ja-jp/windows/desktop/api/winevt/nf-winevt-evtnext
				for (int i = 0; i < returnedHandleSize.getValue(); ++i) {
					wevtapi.INSTANCE.EvtClose(saveHandles[i]);
				}
			}
			String saveBookmarkString = renderEvent(hBookmark, wevtapi.EvtRenderBookmark);
			saveBookmarkFile(bookmarkFileName, saveBookmarkString);

		} finally {
			if (hResult != null) {
				wevtapi.INSTANCE.EvtClose(hResult);
			}
			if (hBookmark != null) {
				wevtapi.INSTANCE.EvtClose(hBookmark);
			}
		}
	}

	/**
	 * イベントログの取得、および、取得した最後のEventRecordIDをブックマークファイルへ保存する。
	 * 
	 * @param bookmarkFileName
	 *            ブックマークファイル名
	 * @param query
	 *            クエリ
	 * @param maxEvents
	 *            イベントログの読み込み件数
	 * @param timeout
	 *            EvtNext関数のタイムアウト
	 * @param logName
	 *            チャネル名
	 * @param lastMonitorDate
	 *            前回監視終了時刻
	 * 
	 * @return 取得したイベントログを連結した文字列とレンダリングに失敗したイベントログがあるかどうかを格納した配列
	 *          (インデックス 0：イベントログ、インデックス 1：レンダリングに失敗したかどうか)
	 */
	public String[] readEventLog(String bookmarkFileName, String query, int maxEvents, long timeout, String logName, Date lastMonitorDate) throws Win32Exception, IOException {
		String bookmarkString = getBookmartString(bookmarkFileName);
		if (bookmarkString.isEmpty()) {
			// ブックマークが取得できない場合はブックマークファイルを作成する
			updateBookmark(bookmarkFileName, logName);
			return null;
		}

		HANDLE hBookmark = null;
		try {
			m_log.debug("bookmarkString is " + bookmarkString);
			hBookmark = wevtapi.INSTANCE.EvtCreateBookmark(new WString(bookmarkString));
			if (hBookmark == null) {
				m_log.warn("readEventLog() EvtCreateBookmark error=" + Win32Error.getMessage(getLastError()) + ", bookmarkString=" + bookmarkString);
				throw new Win32Exception(getLastError());
			}			
			//bookmarkに記載されているrecordIDを取得
			Long id = getRecordId(bookmarkString);
			if (id != null) {
				//bookmarkに記載されているレコードIDから現行のイベントログ対象に対して存在するか確認
				if (!existRecordId(hBookmark, WinEventMonitor.pergeEventLogNameEnclosure(logName), id, timeout)) {
					// bookmark記載のレコードIDがない場合はローテートされている可能性があるのでアーカイブファイルを取得する
					File latestArchiveFiles[] = getLatestArchive(WinEventMonitor.pergeEventLogNameEnclosure(logName), lastMonitorDate);
					//ローテート設定があり、ローテートされたファイルが存在する場合
					if (latestArchiveFiles != null) {
						for(File f : latestArchiveFiles) {
							String latestArchiveFileName = f.getAbsolutePath();
							//ローテートされたファイルに対してbookmarkに記載されているレコードID+1が最新のアーカイブされたログファイルに存在するか確認
							String archiveFilePath = "file://" + latestArchiveFileName;
							if (existRecordId(hBookmark, archiveFilePath, id + 1, timeout)) {
								//queryのパスをアーカイブファイルに変更する
								query = query.replace("Path='" +WinEventMonitor.pergeEventLogNameEnclosure(logName), "Path='" + archiveFilePath);
								//bookmarkのチャンネル文字列をアーカイブファイルに変更する
								bookmarkString = bookmarkString.replace("Channel='"+WinEventMonitor.pergeEventLogNameEnclosure(logName), "Channel='"+latestArchiveFileName);
								break;
							} else if (existRecordId(hBookmark, archiveFilePath, id, timeout)) {
								//ローテートされたファイルに対してbookmarkに記載されているレコードIDが最新のアーカイブされたログファイルに存在するか確認
								//アーカイブ後、更新がない場合
								m_log.debug("no new write since latest check record(recordId=" + id + ")");
							} else {
								//最新のローテートされたファイルにブックマークファイルに記載されているレコードID+1がない場合はログを出力する
								m_log.info(String.format("not found out record(RecordID=%s) in logs current and archive(FileName=%s). may be bookmark file is broken, or too many record archived.",
										id+1, latestArchiveFileName));
							}
						}
					}
				}
			}

		} finally {
			if (hBookmark != null) {
				wevtapi.INSTANCE.EvtClose(hBookmark);
			}
		}

		HANDLE hResults = null;
		try {

			WString wQuery = new WString(query);
			hResults = wevtapi.INSTANCE.EvtQuery(null, null, wQuery, wevtapi.EvtQueryChannelPath | wevtapi.EvtQueryTolerateQueryErrors);
			if (hResults == null) {
				m_log.warn("readEventLog() EvtQuery error=" + Win32Error.getMessage(getLastError()) + ", query=" + query);
				throw new Win32Exception(getLastError());
			}

			hBookmark = wevtapi.INSTANCE.EvtCreateBookmark(new WString(bookmarkString));
			if (hBookmark == null) {
				m_log.warn("readEventLog() EvtCreateBookmark error=" + Win32Error.getMessage(getLastError()) + ", bookmarkString=" + bookmarkString);
				throw new Win32Exception(getLastError());
			}

			if (!wevtapi.INSTANCE.EvtSeek(hResults, new LONGLONG(1), hBookmark, 0, wevtapi.EvtSeekRelativeToBookmark)) {
				m_log.warn("readEventLog() EvtSeek error=" + Win32Error.getMessage(getLastError()));
				throw new Win32Exception(getLastError());
			}
			
			HANDLE[] handles = new HANDLE[maxEvents];
			IntByReference returnedHandleSize = new IntByReference(0);
			if (!wevtapi.INSTANCE.EvtNext(hResults, maxEvents, handles, new DWORD(timeout), 0, returnedHandleSize)) {
				if (WinError.ERROR_NO_MORE_ITEMS == getLastError()) {
					// イベントログが取得できない場合はブックマークを更新してnullを返却する
					updateBookmark(bookmarkFileName, logName);
					return null;
				} else {
					m_log.warn("readEventLog() EvtNext error=" + Win32Error.getMessage(getLastError()));
					throw new Win32Exception(getLastError());
				}
			}

			StringBuffer eventLog = new StringBuffer();
			String[] ret = new String[2];

			//最後のインデックスのハンドルはブックマークの更新に必要なためループ内では処理しない
			int lastIndex = returnedHandleSize.getValue() - 1;
			try {
				//レンダリングに失敗したイベントログがあるかどうか
				boolean renderFailed = false;
				for (int i = 0; i < lastIndex; i++) {
					try {
						eventLog.append(formatEvent(handles[i]));
						wevtapi.INSTANCE.EvtClose(handles[i]);
						handles[i] = null;
					} catch (Win32Exception e) {
						renderFailed = true;
					}
				}
				try {
					eventLog.append(formatEvent(handles[lastIndex]));
				} catch (Win32Exception e) {
					renderFailed = true;
				}

				if(renderFailed) {
					ret[1] = "renderFailed";
				}

				if (!wevtapi.INSTANCE.EvtUpdateBookmark(hBookmark, handles[lastIndex])) {
					m_log.warn("readEventLog() EvtUpdateBookmark error=" + Win32Error.getMessage(getLastError()));
					throw new Win32Exception(getLastError());
				}
			} finally {
				// EvtNext から取得したイベントハンドルは、削除。
				// https://docs.microsoft.com/ja-jp/windows/desktop/api/winevt/nf-winevt-evtnext
				wevtapi.INSTANCE.EvtClose(handles[lastIndex]);
			}

			String saveBookmarkString = renderEvent(hBookmark, wevtapi.EvtRenderBookmark);
			saveBookmarkFile(bookmarkFileName, saveBookmarkString);

			ret[0] = eventLog.toString();
			return ret;
		} finally {
			if (hResults != null) {
				wevtapi.INSTANCE.EvtClose(hResults);
			}
			if (hBookmark != null) {
				wevtapi.INSTANCE.EvtClose(hBookmark);
			}
		}
	}

	private String renderEvent(HANDLE h, int renderFlg) throws Win32Exception {
		HANDLE hContext = null;
		try {
			// EvtRenderEventValuesの場合はコンテキストハンドルを作成
			if (renderFlg == wevtapi.EvtRenderEventValues) {
				hContext = wevtapi.INSTANCE.EvtCreateRenderContext(0, null, wevtapi.EvtRenderContextSystem);
				if (hContext == null) {
					m_log.warn("renderEvent() EvtCreateRenderContext error=" + Win32Error.getMessage(getLastError()));
					throw new Win32Exception(getLastError());
				}
			}

			int bufferSize = 0;
			PVOID buffer = null;
			IntByReference bufferUsed = new IntByReference(0);
			IntByReference propertyCount = new IntByReference(0);
			if (!wevtapi.INSTANCE.EvtRender(hContext, h, renderFlg, bufferSize, buffer, bufferUsed, propertyCount)) {
				if (WinError.ERROR_INSUFFICIENT_BUFFER == getLastError()) {
					bufferSize = bufferUsed.getValue();
					buffer = new PVOID(new Memory(bufferSize));
					wevtapi.INSTANCE.EvtRender(hContext, h, renderFlg, bufferSize, buffer, bufferUsed, propertyCount);
				}
				if (WinError.ERROR_SUCCESS != getLastError()) {
					m_log.warn("renderEvent() EvtRender(second time) error=" + Win32Error.getMessage(getLastError()));
					throw new Win32Exception(getLastError());
				}
			} else {
				m_log.warn("renderEvent() EvtRender(first time) error=" + Win32Error.getMessage(getLastError()));
				throw new Win32Exception(getLastError());
			}

			if (hContext != null) {
				// プロバイダ名のみを返却する
				return buffer.getPointer().getPointerArray(0, 1)[0].getWideString(0);
			} else {
				return buffer.getPointer().getWideString(0);
			}
		} finally {
			if (hContext != null) {
				wevtapi.INSTANCE.EvtClose(hContext);
			}
		}
	}

	private String formatEvent(HANDLE h) throws Win32Exception {
		HANDLE hProviderMetadata = null;
		try {
			String provider = renderEvent(h, wevtapi.EvtRenderEventValues);
			hProviderMetadata = wevtapi.INSTANCE.EvtOpenPublisherMetadata(null, new WString(provider), null, 0, 0);
			if (hProviderMetadata == null) {
				// プロバイダ情報が取得できない場合は renderEvent でイベントログをレンダリングする
				// 以下の EvtFormatMessage と比較する <RenderingInfo>要素がレンダリングされない
				m_log.debug("formatEvent() EvtOpenPublisherMetadata error=" + Win32Error.getMessage(getLastError()) + ", provider is " + provider);
				return renderEvent(h, wevtapi.EvtRenderEventXml);
			}

			// プロバイダ情報が取得できた場合は EvtFormatMessage でイベントログをレンダリングする
			Pointer buffer = null;
			int bufferSize = 0;
			IntByReference bufferUsed = new IntByReference(0);

			if (!wevtapi.INSTANCE.EvtFormatMessage(hProviderMetadata, h, 0, 0, null, wevtapi.EvtFormatMessageXml,
					bufferSize, buffer, bufferUsed)) {
				if (WinError.ERROR_INSUFFICIENT_BUFFER == getLastError()) {
					bufferSize = bufferUsed.getValue();
					buffer = new Memory(bufferSize);
					wevtapi.INSTANCE.EvtFormatMessage(hProviderMetadata, h, 0, 0, null, wevtapi.EvtFormatMessageXml,
							bufferSize, buffer, bufferUsed);
				}
				if (WinError.ERROR_SUCCESS != getLastError()) {
					m_log.warn("formatEvent() EvtFormatMessage(second time) error=" + Win32Error.getMessage(getLastError()));
					if(secondChallenge) {
						m_log.info("Second challenge render event.");
						return renderEvent(h, wevtapi.EvtRenderEventXml);
					} else {
						throw new Win32Exception(getLastError());
					}
				}
			} else {
				m_log.warn("formatEvent() EvtFormatMessage(first time) error=" + Win32Error.getMessage(getLastError()));
				throw new Win32Exception(getLastError());
			}
			return buffer.getWideString(0);
		} finally {
			if (hProviderMetadata != null) {
				wevtapi.INSTANCE.EvtClose(hProviderMetadata);
			}
		}
	}

	private void saveBookmarkFile(String bookmarkFileName, String bookmarkString) throws IOException {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(bookmarkFileName);
			pw.print(bookmarkString);
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}

	private String getBookmartString(String bookmarkFileName) {
		StringBuffer sb = new StringBuffer();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(bookmarkFileName));
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			m_log.info("failed getBookmartString " + bookmarkFileName + ", " + e.getMessage());
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				m_log.info("failed getBookmartString close " + bookmarkFileName + ", " + e.getMessage());
			}
		}
		return sb.toString();
	}

	private int getLastError() {
		return kernel32.INSTANCE.GetLastError();
	}

	
	/**
	 * アーカイブされたファイルがあるか確認する
	 * 
	 * @param channelName 対象のチャンネル名
	 * @param lastMonitorDate 前回監視時刻
	 * @param bookmark アーカイブファイル読み込む時のブックマーク
	 * @return アーカイブされたログファイルが存在していたらアーカイブされたタイムスタンプをもとに最新のアーカイブログファイルのフルパスを返す、それ以外の場合はNull
	 */
	private File[] getLatestArchive(String channelName, final Date lastMonitorDate) {
		HANDLE hConfig = wevtapi.INSTANCE.EvtOpenChannelConfig(null, new WString(channelName), 0);
		if (hConfig == null) {
			m_log.warn("when opening channel config, occurred error. " + new Win32Exception(getLastError()).getLocalizedMessage());
			return null;
		}

		String pFile = null;
		try {
			IntByReference dwBufferUsed = new IntByReference(0);
			wevtapi.INSTANCE.EvtGetChannelConfigProperty(hConfig, wevtapi.EvtChannelLoggingConfigAutoBackup, 0,
					0, null, dwBufferUsed);

			int dwBufferSize = dwBufferUsed.getValue();
			Memory propertyValueBuffer = new Memory(dwBufferSize);
			if (!wevtapi.INSTANCE.EvtGetChannelConfigProperty(hConfig, wevtapi.EvtChannelLoggingConfigAutoBackup, 0,
					dwBufferSize, propertyValueBuffer, dwBufferUsed)) {
				m_log.warn("when getting AutoBackup config , occurred error. read buffer size ="+dwBufferSize+", " + new Win32Exception(getLastError()).getLocalizedMessage());
				return null;
			}

			int pAutoBackup = propertyValueBuffer.getInt(0);
			//1だと有効、0だと無効
			if (pAutoBackup == 1) {
				dwBufferUsed = new IntByReference(0);
				wevtapi.INSTANCE.EvtGetChannelConfigProperty(hConfig, wevtapi.EvtChannelLoggingConfigLogFilePath, 0,
						0, null, dwBufferUsed);

				dwBufferSize = dwBufferUsed.getValue();
				propertyValueBuffer = new Memory(dwBufferSize);
				if (!wevtapi.INSTANCE.EvtGetChannelConfigProperty(hConfig, wevtapi.EvtChannelLoggingConfigLogFilePath, 0,
						dwBufferSize, propertyValueBuffer, dwBufferUsed)) {
					m_log.warn("when getting FilePath config , occurred error. read buffer size ="+dwBufferSize+", " + new Win32Exception(getLastError()).getLocalizedMessage());
					return null;
				}
				pFile = propertyValueBuffer.getPointer(0).getWideString(0);
			} else {
				m_log.info("skip read event log config. "+ channelName+ " AutoBackup property=" + pAutoBackup);
				return null;
			}
		} finally {
			wevtapi.INSTANCE.EvtClose(hConfig);
		}

		//File 分解パターン
		Matcher mFilePath = Pattern.compile("(.*)\\\\(.*)\\.(evtx|evt|etl)").matcher(pFile);
		if (mFilePath.matches()) {
			String dirPath = mFilePath.group(1);
			//File名探索パターン
			final Pattern pArchiveFileName = Pattern.compile("Archive-" + mFilePath.group(2) + "-(.*)\\.(evtx|evt|etl)");

			//ディレクトリのパスに環境変数が含まれているか確認
			Matcher m3 = Pattern.compile("(.*)%(.*)%(.*)").matcher(dirPath);
			if (m3.matches()) {
				//環境変数で解決できる場合置き換える
				if (System.getenv(m3.group(2)) != null) {
					dirPath = m3.group(1) + System.getenv(m3.group(2)) + m3.group(3);
					m_log.debug("File Path (changed) = "+ dirPath + " ['" + m3.group(2) + "' -> '" +System.getenv(m3.group(2)) +"']");
				}
			}

			File[] files = new File(dirPath).listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					// マッチ、かつ、前回監視より更新時刻が新しいファイルのみフィルタする
					return (pArchiveFileName.matcher(pathname.getName()).matches()) && (pathname.lastModified() > lastMonitorDate.getTime());
				}});

			if (files == null || files.length == 0) {
				m_log.debug(String.format("no match file in directory(%s). search pattern : %s", dirPath, pArchiveFileName.toString()));
				return null;
			}

			Arrays.sort(files, new Comparator<File>() {
				public int compare(File file1, File file2){
					return file1.getName().compareTo(file2.getName());
				}
			});

			return files;
		} else {
			m_log.warn(String.format("AutoBackup is valid, but File property(%s) unable to analysis.", pFile));
			return null;
		}
	}

	private Long getRecordId(String bookmarkString) {
		// DOMパーサ用ファクトリの生成
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// DOM Documentインスタンス用ファクトリの生成
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();

			// 解析とDocumentインスタンスの取得
			Document doc = builder.parse(new ByteArrayInputStream(bookmarkString.getBytes()));

			XPathFactory xfactory = XPathFactory.newInstance();
			XPath xpath = xfactory.newXPath();

			String location = "/BookmarkList/Bookmark/@RecordId";
			String recordId = xpath.evaluate(location, doc);

			if (recordId.isEmpty()) {
				return null;
			} else {
				return Long.valueOf(recordId);
			}
		} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
			m_log.warn(e.getLocalizedMessage());
			return null;
		}
	}

	private boolean existRecordId(HANDLE hBookmark, String channel, long recordId, long timeout) {
		HANDLE hResults = null;
		//recordIdがイベントログ対象に対して存在するか確認
		String bmRIdQ = "<QueryList><Query><Select Path='"
				+ channel
				+ "'>*[System[EventRecordID="
				+ recordId
				+ "]]</Select></Query></QueryList>";

		WString wQuery = new WString(bmRIdQ);
		try {
			hResults = wevtapi.INSTANCE.EvtQuery(null, null, wQuery, wevtapi.EvtQueryChannelPath | wevtapi.EvtQueryTolerateQueryErrors);
			if (hResults == null) {
				m_log.warn("existRecordId() EvtQuery error=" + Win32Error.getMessage(getLastError()) + ", query=" + bmRIdQ);
				throw new Win32Exception(getLastError());
			}

			if (!wevtapi.INSTANCE.EvtSeek(hResults, new LONGLONG(0), hBookmark, 0, wevtapi.EvtSeekRelativeToBookmark)) {
				m_log.warn("existRecordId() EvtSeek error=" + Win32Error.getMessage(getLastError()));
				throw new Win32Exception(getLastError());
			}
			
			HANDLE[] handles = new HANDLE[1];
			IntByReference returnedHandleSize = new IntByReference(0);
			if (!wevtapi.INSTANCE.EvtNext(hResults, 1, handles, new DWORD(timeout), 0, returnedHandleSize)) {
				if (WinError.ERROR_NO_MORE_ITEMS == getLastError()) {
					return false;
				} else {
					m_log.warn("existRecordId() EvtNext error=" + Win32Error.getMessage(getLastError()));
					throw new Win32Exception(getLastError());
				}
			} else {
				// EvtNext から取得したイベントハンドルは、削除。
				// https://docs.microsoft.com/ja-jp/windows/desktop/api/winevt/nf-winevt-evtnext
				for (int i = 0; i < returnedHandleSize.getValue(); ++i) {
					wevtapi.INSTANCE.EvtClose(handles[i]);
				}
				m_log.debug("existRecordId : recordId " + recordId + " existed " + channel);
				return true;
			}
		} finally {
			if (hResults != null)
				wevtapi.INSTANCE.EvtClose(hResults);
		}
	}
}
