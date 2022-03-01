/*
 * Copyright (c) 2021 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

/*
 * 集計グラフの共通処理を定義しているJavaScriptです。
 */
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// javaに通知する関数です
function callJavaMethod(parama) {
	if (parama.method_name == "javascript_error") {
		if (parama.exception.stack) {
			parama.exceptionstack = parama.exception.stack;
		}
	}
	var param_str = JSON.stringify(parama);
	try {
		var result = theJavaFunction(param_str);
		return result;
	} catch(e) {
		if (e.message.indexOf(HINEMOS_COLLECT_CONST.CONST_CRUSH_ERR_MSG) >= 0) {
			// another browser function is still pending
			return;
		}
	}
};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// 指定された文字列の長さが、指定された長さよりも長い場合に省略文字に置換して返します
// valueが数字の場合にlengthを使用すると、undefinedが返ってくる。
function truncateText(value, length) {
	value = String(value);
	var byte = 0, trimStr = "";
	for (var j = 0, len = value.length; j <len ; j++) {
		value.charCodeAt(j) < 0x100 ? byte++ : byte += 2;
		trimStr += value.charAt(j)
		if (byte >= length) {
			trimStr = trimStr.substr(0, j-2) + "...";
			break;
		}
	}
	return trimStr;
};