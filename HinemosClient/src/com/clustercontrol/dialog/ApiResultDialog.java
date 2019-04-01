/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.dialog;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.access.InvalidRole_Exception;

/**
 * API呼び出しの結果(成功・失敗)を表示するための汎用メッセージダイアログです。
 * 
 * @since 6.2.0
 */
public class ApiResultDialog {
	
	private List<Item> successList;
	private List<Item> failureList;

	private static class Item {
		String managerName;
		String message;
	}
	
	public ApiResultDialog() {
		successList = new ArrayList<>();
		failureList = new ArrayList<>();
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
			sb.append(it.managerName);
			sb.append(" : ");
			sb.append(it.message);
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
	 * @param idMessage APIの操作対象を明示するためにメッセージ末尾に付与する文字列。
	 *                  「(SomeID=xxxx)」という書式を想定しています。
	 *                  汎用のメッセージリソースしか持っていないアクセス権限エラーの場合のみ使用します。
	 * @return このインスタンス自身を返します。
	 */
	public ApiResultDialog addFailure(String managerName, Throwable throwable, String idMessage) {
		Item item = new Item();
		item.managerName = managerName;

		// 権限エラーのみ特定メッセージ固定、他のエラーは例外が保持しているメッセージを元にする。
		// WSの名前空間ごとにクラスが作成されるので、simple名で比較する。
		if (throwable.getClass().getSimpleName().equals(InvalidRole_Exception.class.getSimpleName())) {
			if (StringUtils.isEmpty(idMessage)) {
				item.message = Messages.get("message.accesscontrol.16");
			} else {
				item.message = Messages.get("message.accesscontrol.16") + " " + idMessage;
			}
		} else {
			item.message = HinemosMessage.replace(throwable.getMessage());
		}

		failureList.add(item);
		return this;
	}
}
