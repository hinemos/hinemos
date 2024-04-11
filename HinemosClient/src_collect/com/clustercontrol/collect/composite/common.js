/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 *
 * 性能[グラフ]の共通処理を定義しているJavaScriptです。
 */
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// javaに通知する関数です
function callJavaMethod(parama) {
	if (parama.method_name == "javascript_error") {
		alert(getGraphMessages("mess-unexpectederror") + "\n" + getGraphMessages("detail") + ": " + parama.exception.stack);
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
		alert(getGraphMessages("mess-unexpectederror") + "\n" + e.stack + " message:" + param_str);
	}
};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// 
function d3_time_scaleFormat(formats) {
	return function(date) {
		var i = formats.length - 1, f = formats[i];
		while (!f[1](date)) f = formats[--i];
		return f[0](date);
	};
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// https://github.com/mbostock/d3/wiki/Time-Formatting
var d3_time_scaleLocalFormats = [
	[ d3.time.format("%Y"), function(d) { return true;} ],
	[ d3.time.format("%Y/%m"), function(d) { return d.getMonth();} ],
	[ d3.time.format("%m/%d"), function(d) { return d.getDate() != 1;} ],
	[ d3.time.format("%m/%d"), function(d) { return d.getDay() && d.getDate() != 1;} ],
	[ d3.time.format("%H:%M"), function(d) { return d.getHours();} ],
	[ d3.time.format("%H:%M"), function(d) { return d.getMinutes();} ],
	[ d3.time.format(":%S"), function(d) { return d.getSeconds();} ],
	[ d3.time.format(".%L"), function(d) { return d.getMilliseconds();} ]
	];

var d3_time_scaleLocalFormat = d3_time_scaleFormat(d3_time_scaleLocalFormats);
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// 与えられたものが"NaN"かどうかを厳密にチェックします
isReallyNaN = function(x) {
	return x !== x; // xがNaNであればtrue, それ以外ではfalse
};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// 適用押下時の情報を保持します(グラフ種別など)
function setGraphConfig(settinginfo) {
	// {return, returnKind, total, stack, applox, thre, pie, scatter, legend, sigma, bar}
	var settingarr = [];
	for (var i = 0;settinginfo != null && i < settinginfo.length; i++) {
		var info = Number(settinginfo.substring(i, i+1));
		// 0,1でもいいけどわかりやすく変換する
		settingarr.push(Boolean(info));
	}
	d3.selectAll("#graph_config")
		.attr("data-return-flg", settingarr[0])
		.attr("data-returnkind-flg", settingarr[1])
		.attr("data-total-flg", settingarr[2])
		.attr("data-stack-flg", settingarr[3])
		.attr("data-approx-flg", settingarr[4])
		.attr("data-threshold-flg", settingarr[5])
		.attr("data-pie-flg", settingarr[6])
		.attr("data-scatter-flg", settingarr[7])
		.attr("data-legend-flg", settingarr[8])
		.attr("data-sigma-flg", settingarr[9])
		.attr("data-stackbar-flg", settingarr[10])
		.attr("data-rap-flg", settingarr[11]);
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// 指定された情報をtrue/falseで返します
function getGraphConfig(value) {
	var settinginfo = d3.selectAll("#graph_config").attr(value);
	if (settinginfo == "true") {
		return true;
	}
	return false;
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// 保持している情報を初期化します
function clearGraphConfig() {
	// nullで初期化すると属性が消える、falseで初期化しておく
	d3.selectAll("#graph_config")
		.attr("data-return-flg", "false")
		.attr("data-returnkind-flg", "false")
		.attr("data-total-flg", "false")
		.attr("data-stack-flg", "false")
		.attr("data-approx-flg", "false")
		.attr("data-threshold-flg", "false")
		.attr("data-pie-flg", "false")
		.attr("data-scatter-flg", "false")
		.attr("data-legend-flg", "false")
		.attr("data-sigma-flg", "false")
		.attr("data-stackbar-flg", "false");
}
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
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// 文字列のソート
function sortStr(value1, value2) {
	if (value1 < value2) { return -1 };
	if (value1 > value2) { return 1 };
	return 0
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// 画面に表示する文言・メッセージを解析して設定します(多言語対応)
function setGraphMessages(json) {
	d3.selectAll("#graph_messages")
		.attr("mess-nodes", json.nodes) // ノード
		.attr("mess-time", json.time) // 日時
		.attr("mess-priority", json.priority) // 重要度
		.attr("mess-message", json.message) // メッセージ
		.attr("mess-total", json.total) // 全
		.attr("mess-information", json.information) // 情報
		.attr("mess-warning", json.warning) // 警告
		.attr("mess-critical", json.critical) // 危険
		.attr("mess-unknown", json.unknown) // 不明
		.attr("mess-detail", json.detail) // 詳細
		.attr("mess-prediction", json.prediction) // 予測先(@@分後)
		.attr("mess-timezoneoffset", json.timezoneoffset) // タイムゾーンオフセット(数値)
		.attr("mess-captureerror", json.captureerror) // キャプチャファイルが100以上あるため、実行できません
		.attr("mess-unexpectederror", json.unexpectederror) // 予期しないエラーが発生しました。
		.attr("mess-datainsufficient", json.datainsufficient); // 収集データが不足しています
		
		d3.select("#autoadjust").text(json.autoadjust); // 自動調整
		d3.select("#autoupdate").text(json.autoupdate); // 自動更新
		d3.select("#bulkpng").attr("value", json.bulkpng); // 一括PNG
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// 引数に対応する文言を返します(多言語対応)
function getGraphMessages(inparam) {
	var retvalue = d3.selectAll("#graph_messages").attr(inparam);
	return retvalue;
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
//指定されたグラフを「NO DATA」表示します
function dispNoData(self) {
// collectidが取れなかった場合は、背景をグレーにする(tickと同じ色)
self.vis.selectAll(".graph_background_block").style("fill", HINEMOS_COLLECT_CONST.CONST_COLOR_GRAY);
var target = self.vis.select("#nodata");
if (target != null && target[0][0] != null) {
	return;
}
self.vis.append("g")
.attr("id", "nodata")
.append("text")
.style("text-anchor","middle")
.style("font-size","14pt")
.style("font-weight","bold")
.style("fill",HINEMOS_COLLECT_CONST.CONST_COLOR_HINEMOS_BASE)
.attr("y", self.height/2)
.attr("x", self.width/2)
.text("NO DATA");
};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
//ツールチップ表示を見えなくします
function disableTooltip() {
	d3.select("body")
	.select(".tooltip_mouse")
	.style("visibility", "hidden");

};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// 引数で指定されたブラウザで閲覧している場合はtrue、違う場合はfalseを返します
// IEの場合はチェックできません、chrome or firefox だけしかチェックできません
function checkBrowserKind(browsername) {
	var ua = window.navigator.userAgent.toLowerCase();
	if (ua.indexOf(browsername) != -1) {
		return true;
	}
	return false;
};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
var msec_hour = 60 * 60 * 1000;
var msec_day = msec_hour * 24;
var msec_week = msec_day * 7;
var msec_month = msec_day * 31;
var msec_year = msec_day * 365;
var msec_10year = msec_year * 10;
var msec_none = -1;
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// 指定した文字列を指定した幅でカットします
function truncateByWidth(text, width, fontFamily, fontSize, fontWeight) {
	var currentWidth;
	var len = text.length;
	ellipsis = "...";
	while ((currentWidth = getTextWidth(text, fontFamily, fontSize, fontWeight)) > width) {
		--len;
		text = text.substring(0, len) + ellipsis;
	}
	return text;
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
//引数で指定したテキスト及びフォントで、画面上の幅を取得します
function getTextWidth(text, fontFamily, fontSize, fontWeight) {
	// 計算するためのspanを生成
	var span = document.createElement('span');
	// 現在の表示要素に影響しないように設定
	span.style.position = 'absolute';
	span.style.top = '-9999px';
	//折り返しさせない
	span.style.whiteSpace = 'nowrap';
	
	span.style.fontSize = fontSize;
	span.style.fontFamily = fontFamily;
	span.style.fontWeight = fontWeight;
	span.textContent = text;
	// 一旦 DOM Tree に append しする
	document.body.appendChild(span);
	textWidth = span.clientWidth;
	// DOM Treeから削除
	document.body.removeChild(span);
	return textWidth;
}
