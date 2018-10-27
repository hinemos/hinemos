package com.clustercontrol.agent.winevent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.Win32Error;
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
			wevtapi.INSTANCE.EvtClose(saveHandles[0]);
			saveHandles[0] = null;

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
	 * 
	 * @return 取得したイベントログを連結した文字列とレンダリングに失敗したイベントログがあるかどうかを格納した配列
	 *          (インデックス 0：イベントログ、インデックス 1：レンダリングに失敗したかどうか)
	 */
	public String[] readEventLog(String bookmarkFileName, String query, int maxEvents, long timeout, String logName) throws Win32Exception, IOException {
		HANDLE hResults = null;
		HANDLE hBookmark = null;
		String[] ret = new String[2];
		try {
			StringBuffer eventLog = new StringBuffer();
			
			WString wQuery = new WString(query);
			hResults = wevtapi.INSTANCE.EvtQuery(null, null, wQuery, wevtapi.EvtQueryChannelPath | wevtapi.EvtQueryTolerateQueryErrors);
			if (hResults == null) {
				m_log.warn("readEventLog() EvtQuery error=" + Win32Error.getMessage(getLastError()) + ", query=" + query);
				throw new Win32Exception(getLastError());
			}

			String bookmarkString = getBookmartString(bookmarkFileName);
			if (bookmarkString.isEmpty()) {
				// ブックマークが取得できない場合はブックマークファイルを作成する
				updateBookmark(bookmarkFileName, logName);
				return null;
			} else {
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
				} else {
					//レンダリングに失敗したイベントログがあるかどうか
					boolean renderFailed = false;
					//最後のインデックスのハンドルはブックマークの更新に必要なためループ内では処理しない
					int lastIndex = returnedHandleSize.getValue() - 1;
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
					wevtapi.INSTANCE.EvtClose(handles[lastIndex]);
					handles[returnedHandleSize.getValue() - 1] = null;
					String saveBookmarkString = renderEvent(hBookmark, wevtapi.EvtRenderBookmark);
					saveBookmarkFile(bookmarkFileName, saveBookmarkString);
				}
			}
			
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
					throw new Win32Exception(getLastError());
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
}
