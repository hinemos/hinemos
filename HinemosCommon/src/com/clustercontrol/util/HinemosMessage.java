package com.clustercontrol.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.Messages;

public class HinemosMessage {
	// ログ
	private static Log m_log = LogFactory.getLog( HinemosMessage.class );

	// 引数なし変数
	private static Pattern patternNoArgs = Pattern.compile("\\$\\[([0-9a-zA-Z_]*?)\\]", Pattern.DOTALL);
	// 引数あり変数
	private static Pattern patternArgs = Pattern.compile("\\$\\[([0-9a-zA-Z_]*?):\"", Pattern.DOTALL);

	public static String replace(String message) {
		return replace (message, null);
	}

	/**
	 * メッセージの置換処理
	 * 
	 * @param message 置換対象メッセージ
	 * @param locale ロケール
	 * @return 置換後メッセージ
	 */
	public static String replace(String message, Locale locale) {

		if (message == null || message.equals("")) {
			return "";
		}

		m_log.trace("message = " + message);
		
		MessageSetting messageSetting = new MessageSetting(message);
		do {
			setMessage(messageSetting, messageSetting.curIdx, locale);
		} while(messageSetting.curIdx < messageSetting.message.length());

		return messageSetting.message;
	}

	/**
	 * 開始位置からメッセージ作成処理を行う
	 * 
	 * @param messageSetting 編集用情報
	 * @param locale ロケール
	 */
	private static void setMessage(MessageSetting messageSetting, int startIdx, Locale locale) {
		m_log.trace("message = " + messageSetting.message);

		// 変数名
		String variableName = "";
		List<String> args = new ArrayList<>();

		// 引数あり変数を検索する
		Matcher matcherArgs = patternArgs.matcher(messageSetting.message.substring(startIdx));
		boolean isArgsFind = matcherArgs.find();
		// 引数なし変数を検索する
		Matcher matcherNoArgs = patternNoArgs.matcher(messageSetting.message.substring(startIdx));
		boolean isNoArgsFind = matcherNoArgs.find();

		// 引数あり変数処理:true、引数なし変数処理:false
		boolean isMatchedArgs = false;
		if (isArgsFind && isNoArgsFind) {
			// 両方検索された場合
			isMatchedArgs = matcherArgs.start() < matcherNoArgs.start();
		} else if (isArgsFind) {
			// 引数あり変数のみ検索された場合
			isMatchedArgs = true;
		} else if (isNoArgsFind) {
			// 引数なし変数のみ検索された場合
			isMatchedArgs = false;
		} else {
			// 変数が検索されない場合
			messageSetting.curIdx = messageSetting.message.length();
			return;
		}

		int endIdx = 0;
		if (isMatchedArgs) {
			variableName = matcherArgs.group(1);
			int idx = messageSetting.curIdx + matcherArgs.end();
			boolean isClosed = false;
			for (int i = messageSetting.curIdx + matcherArgs.end(); i < messageSetting.message.length(); i++) {
				if (messageSetting.message.charAt(i) == '$'
						&& messageSetting.message.charAt(i + 1) == '[') {
					// 変数置換
					messageSetting.curIdx = i;
					setMessage(messageSetting, messageSetting.curIdx, locale);
					i = messageSetting.curIdx - 1;
				} else if (messageSetting.message.charAt(i) == '"') {
					if (messageSetting.message.charAt(i - 1) == '\\') {
						continue;
					} else if ( messageSetting.message.length() >= (i + 3) 
							&& messageSetting.message.charAt(i + 1) == ':'
							&& messageSetting.message.charAt(i + 2) == '"') {
						// 引数処理
						args.add(messageSetting.message.substring(idx, i));
						idx = i + 3;
					} else if ( messageSetting.message.length() >= (i + 2)
							&& messageSetting.message.charAt(i + 1) == ']') {
						// 最後の引数
						args.add(messageSetting.message.substring(idx, i));
						endIdx = i + 2;
						isClosed = true;
						break;
					}
				}
			}
			if (!isClosed) {
				// 正しく閉じられていない場合は処理を終了する。
				messageSetting.curIdx = messageSetting.message.length();
				return;
			}
			String ret_str = null;
			if (locale != null) {
				ret_str = Messages.getString(variableName, args.toArray(), locale);
			} else {
				ret_str = Messages.getString(variableName, args.toArray());
			}
			messageSetting.message = messageSetting.message.substring(0, startIdx + matcherArgs.start()) 
					+ ret_str
					+ messageSetting.message.substring(endIdx);
			messageSetting.curIdx = startIdx + matcherArgs.start() + ret_str.length();
		} else {
			variableName = matcherNoArgs.group(1);
			// 変数置換
			String ret_str = null;
			if (locale != null) {
				ret_str = Messages.getString(variableName, new String[0], locale);
			} else {
				ret_str = Messages.getString(variableName, new String[0]);
			}
			messageSetting.message = messageSetting.message.substring(0, startIdx + matcherNoArgs.start()) 
					+ ret_str
					+ messageSetting.message.substring(startIdx + matcherNoArgs.end());
			messageSetting.curIdx = startIdx + matcherNoArgs.start() + ret_str.length();
		}
	}

	/**
	 * messages.propertiesの存在する、HinemosClient/libにクラスパスを通すこと。
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		String test = "[2016/11/18 10:12:33] stdout=$[MESSAGE_JOB_MONITOR_ORGMSG_JMX:\"$[JMX_DB_CONNECTION_COUNT]\":\"19000\":\"null\":\"$[CONVERT_NO]\"]";
		Pattern pattern = Pattern.compile("\\$\\[([0-9a-zA-Z_]*?)\\]", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(test);
		System.out.println(matcher.find());
//		int okCounter = 0;
//		int ngCounter = 0;
//		for (MessageConstant messageConstant : MessageConstant.values()) {
//			String id = messageConstant.getMessage("aaa", "bbb", "ccc");
//			String message = replace(id);
//			if (id.equals(message)) {
//				System.out.println("error. message doesn't change. " + message);
//				ngCounter ++;
//			} else {
//				System.out.println("id=" + id + ", message=" + message);
//				okCounter ++;
//			}
//		}
//
//		// OKの数がHinemosManager/resource/manager_messages.propertiesのメッセージ数と等しいことを確認する。
//		System.out.println("OK=" + okCounter + ", NG=" + ngCounter);
//		System.out.println("------------");
//
//		// message.repository.1=ノード 「{0}」(ID\={1}) を削除します。よろしいですか？
//		String str = "hoge$[message.repository.1:\"$[edit]\":\"$[message.repository.1:aaa:bbb]\"]\nhoge";
//		System.out.println("str1=" + str);
//		System.out.println("str2=" + replace(str));
//		
//		// str2=hogeノード 「編集」(ID=ノード 「aaa」(ID=bbb) を削除します。よろしいですか？) を削除します。よろしいですか？\nhoge
	}

	/**
	 * メッセージの編集用クラス
	 *
	 */
	private static class MessageSetting {
		// 処理対象メッセージ
		private String message = "";
		// 処理位置
		private int curIdx = 0;

		private MessageSetting(String message) {
			this.message = message;
		}
	}
}
