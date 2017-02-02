/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

性能[グラフ]の定数を定義しているJavaScriptです。
定数を定義しているが、定数の値は書き換え可能なので書き換えないよう注意。
 */
////////////////////////////////////////
// NEWGRAPH MODE
////////////////////////////////////////
var HINEMOS_COLLECT_CONST = {
// 自動更新間隔(msec)
'CONST_UPDATE_INTERVAL' : 60000,
// アニメーション描画にかかる時間(msec)
'CONST_ANIMATION_INTERVAL' : 2000,
// アニメーション描画にかかる時間(msec)(円グラフ用)
'CONST_ANIMATION_INTERVAL_PIE' : 1000,

// グラフ線の太さ(デフォルト)
'CONST_LINE_WIDTH_DEFAULT' : "0.5px",

// グラフ線の太さ(マウスオーバー)
'CONST_LINE_WIDTH_MOUSEOVER' : "1.5px",

// イベントフラグ線の太さ
'CONST_EVENTFLG_WIDTH_MOUSEOVER' : "2.0px",

'CONST_GRAPH_MARGIN_TOP' : 35,
'CONST_GRAPH_MARGIN_RIGHT' : 15,
'CONST_GRAPH_MARGIN_BOTTOM' : 20,
'CONST_GRAPH_MARGIN_LEFT' : 60,

'CONST_GRAPH_RIGHT_LEGEND_HEIGHT' : 20,

'CONST_GRAPH_WIDTH' : 420,
'CONST_GRAPH_HEIGHT' : 225,
'CONST_GRAPH_MARGIN' : 4,
// 各グラフの枠線(グレー)の太さ
'CONST_GRAPH_BORDER_WIDTH' : 1,
// タイトル部分の太さ(CONST_GRAPH_MARGIN_TOP - 10)
'CONST_GRAPH_TITLE_HEIGHT' : 35 - 10,


'CONST_GRAPH_SCREEN_ZOOM_INTERVAL' : 20,
'CONST_GRAPH_SCREEN_ZOOM_MIN' : 40,
'CONST_GRAPH_SCREEN_ZOOM_MAX' : 400,

////////////////////////////////////////
// THRESHOLD MODE
////////////////////////////////////////
'CONST_THRESHOLD_COLOR_CRITICAL' : "#ff6666",
'CONST_THRESHOLD_COLOR_INFO' : "#66ff33",
'CONST_THRESHOLD_COLOR_WARN' : "#ffff33",
'CONST_THRESHOLD_COLOR_UNKNOWN' : "#386fae",

'CONST_THRESHOLD_COLOR_CRITICAL_OPACITY' : "0.2",
'CONST_THRESHOLD_COLOR_INFO_OPACITY' : "0.3",
'CONST_THRESHOLD_COLOR_WARN_OPACITY' : "0.3",

'CONST_THRESHOLD_COLOR_INFO_OPERATION_OPACITY' : "0.0",
'CONST_THRESHOLD_COLOR_WARN_OPERATION_OPACITY' : "0.0",

'CONST_THRESHOLD_COLOR_OPERATION_WIDTH' : 10,

'CONST_THRESHOLD_LIMIT_DISP_PLUS' : 300,
'CONST_THRESHOLD_LIMIT_DISP_MINUS' : -300,

////////////////////////////////////////
// STACKED AREA MODE
////////////////////////////////////////
'CONST_STACKED_COLOR_OPACITY' : "0.7",
'CONST_STACKED_TOOLTIP_COUNT' : 10,



////////////////////////////////////////
// PIE MODE
////////////////////////////////////////
'CONST_PIEGRAPH_WIDTH' : 280,
//CONST_PIEGRAPH_HEIGHTはCONST_GRAPH_HEIGHTと同じにすること
'CONST_PIEGRAPH_HEIGHT' : 225,


////////////////////////////////////////
// SCATTER MODE
////////////////////////////////////////
//CONST_SCATTERGRAPH_WIDTHはCONST_GRAPH_WIDTHと同じにすること
'CONST_SCATTERGRAPH_WIDTH' : 420,
//CONST_SCATTERGRAPH_HEIGHTはCONST_GRAPH_HEIGHTと同じにすること
'CONST_SCATTERGRAPH_HEIGHT' : 225,
'CONST_SCATTERGRAPH_MARGIN_BOTTOM' : 35,


////////////////////////////////////////
// COMMON
////////////////////////////////////////
'CONST_LINESTACKGRAPH' : "linestackgraph",
'CONST_PIEGRAPH' : "piegraph",
'CONST_SCATTERGRAPH' : "scattergraph",
'CONST_BARSTACKGRAPH' : "barstackgraph",

'CONST_COLOR_HINEMOS_BASE' : "#386fae",
'CONST_COLOR_BLACK' : "#000000",
'CONST_COLOR_RED' : "#ff0000",
'CONST_COLOR_WHITE' : "#ffffff",
'CONST_COLOR_GRAY' : "#e7e7e7",

////////////////////////////////////////
// SUMMARY TYPE (same as -> com.clustercontrol.collect.bean.SummaryTypeConstant.java)
////////////////////////////////////////
'CONST_SUMMARY_TYPE_RAW' : 0,
'CONST_SUMMARY_TYPE_AVG_HOUR' : 1,
'CONST_SUMMARY_TYPE_AVG_DAY' : 2,
'CONST_SUMMARY_TYPE_AVG_MONTH' : 3,
'CONST_SUMMARY_TYPE_MIN_HOUR' : 4,
'CONST_SUMMARY_TYPE_MIN_DAY' : 5,
'CONST_SUMMARY_TYPE_MIN_MONTH' : 6,
'CONST_SUMMARY_TYPE_MAX_HOUR' : 7,
'CONST_SUMMARY_TYPE_MAX_DAY' : 8,
'CONST_SUMMARY_TYPE_MAX_MONTH' : 9,

////////////////////////////////////////
// CRUSH JAVASCRIPT ERROR MESSAGE
////////////////////////////////////////
'CONST_CRUSH_ERR_MSG' : "Another browser function is still pending."

};

Object.freeze(HINEMOS_COLLECT_CONST);



