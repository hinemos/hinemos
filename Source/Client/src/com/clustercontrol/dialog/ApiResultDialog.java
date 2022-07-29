/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * API呼び出しの結果(成功・失敗)を表示するための汎用メッセージダイアログです。
 * 
 * @since 6.2.0
 */
public class ApiResultDialog {

	private List<Item> successList;
	private List<Item> failureList;

	private Map<Pattern, String> errorMessageMap;

	private static class Item {
		String managerName;
		String message;
	}

	/**
	 * このダイアログが表示する形式でマネージャ名とメッセージをフォーマットして返します。
	 */
	public static String formatMessage(String managerName, String message) {
		return managerName + " : " + message;
	}

	public ApiResultDialog() {
		successList = new ArrayList<>();
		failureList = new ArrayList<>();
		errorMessageMap = new HashMap<>();

		errorMessageMap.put(
				Pattern.compile(".*\\." + InvalidRole.class.getSimpleName()),
				Messages.get("message.accesscontrol.16"));
	}

	public void overrideErrorMessage(String exceptionClassNameRegex, String message) {
		errorMessageMap.put(Pattern.compile(exceptionClassNameRegex), message);
	}

	/**
	 * 成功、及び失敗ダイアログを表示します。
	 * <p>
	 * {@link #addSuccess(String, String)}が呼ばれていない場合は、
	 * 成功ダイアログは表示しません。
	 * 同様に、{@link #addFailure(String, Throwable, String)}が呼ばれていない場合は、
	 * 失敗ダイアログは表示しません。
	 */
	public void show() {
		if (successList.size() > 0) {
			MessageDialog.openInformation(null, Messages.get("successful"), buildMessage(successList));
		}
		if (failureList.size() > 0) {
			MessageDialog.openError(null, Messages.get("failed"), buildMessage(failureList));
		}
	}

	private String buildMessage(List<Item> messages) {
		StringBuilder sb = new StringBuilder();
		for (Item it : messages) {
			if (sb.length() > 0) {
				sb.append("\n");
			}
			sb.append(formatMessage(it.managerName, it.message));
		}
		return sb.toString();
	}

	/**
	 * 成功メッセージを追加します。
	 * 
	 * @param managerName マネージャ名。 
	 * @param message メッセージ内容。
	 * @return このインスタンス自身を返します。
	 */
	public ApiResultDialog addSuccess(String managerName, String message) {
		Item item = new Item();
		item.managerName = managerName;
		item.message = message;
		successList.add(item);
		return this;
	}

	/**
	 * 失敗メッセージを追加します。
	 * 
	 * @param managerName マネージャ名。
	 * @param throwable APIから返された例外。
	 * @peram 
	 * @return このインスタンス自身を返します。
	 */
	public ApiResultDialog addFailure(String managerName, Throwable throwable, String preMessage, String postMessage) {
		Item item = new Item();
		item.managerName = managerName;

		StringBuilder buff = new StringBuilder();
		if (preMessage != null) {
			buff.append(preMessage);
		}

		// オーバーライド指定があるならそちらのメッセージを、そうでなければ例外が保持しているメッセージを取得
		String msg = null;
		for (Entry<Pattern, String> entry : errorMessageMap.entrySet()) {
			if (entry.getKey().matcher(throwable.getClass().getName()).matches()) {
				msg = entry.getValue();
				break;
			}
		}
		if (msg == null) {
			msg = HinemosMessage.replace(throwable.getMessage());
		}
		buff.append(msg);

		if (postMessage != null) {
			buff.append(postMessage);
		}
		item.message = buff.toString();

		failureList.add(item);
		return this;
	}

	/**
	 * 失敗メッセージを追加します。
	 * 
	 * @param managerName マネージャ名。
	 * @param throwable APIから返された例外。
	 * @param idMessage APIの操作対象を明示するためにメッセージ末尾に付与する文字列。
	 *                  「(SomeID=xxxx)」という書式を想定しています。
	 *                  汎用のメッセージリソースしか持っていないアクセス権限エラーの場合のみ使用します。
	 * @return このインスタンス自身を返します。
	 */
	public ApiResultDialog addFailure(String managerName, Throwable throwable, String idMessage) {
		return addFailure(managerName, throwable, null, " " + idMessage);
	}

	/**
	 * 失敗メッセージを追加します。
	 * 
	 * @param managerName マネージャ名。
	 * @param throwable APIから返された例外。
	 * @return このインスタンス自身を返します。
	 */
	public ApiResultDialog addFailure(String managerName, Throwable throwable) {
		return addFailure(managerName, throwable, null, null);
	}

	/**
	 * 保持している成功メッセージ数を返します。
	 */
	public int getSuccessCount() {
		return successList.size();
	}

	/**
	 * 保持している失敗メッセージ数を返します。
	 */
	public int getFailureCount() {
		return failureList.size();
	}

}
