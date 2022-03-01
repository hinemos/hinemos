/*
 * Copyright (c) 2021 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 *
 */

/*
 * 集計グラフの定数を定義しているJavaScriptです。
 * 定数を定義しているが、定数の値は書き換え可能なので書き換えないよう注意。
 */

var HINEMOS_RPA_CONST = {
		
////////////////////////////////////////
// COMMON
////////////////////////////////////////
'CONST_COLOR_BLACK' : "#000000",
'CONST_COLOR_WHITE' : "#ffffff",
'CONST_COLOR_GRAY' : "#e7e7e7",

'CONST_COLOR_BLUE' : "#1f77b4",
'CONST_COLOR_ORANGE' : "#ff7f0e",

'CONST_COLOR_GREEN' : "#009d5b",
'CONST_COLOR_RED' : "#ed1a3d",

'CONST_GRAPH_MARGIN_LEFT' : 40,
'CONST_GRAPH_MARGIN_TOP' : 40,
'CONST_GRAPH_MARGIN_RIGHT' : 40,
'CONST_GRAPH_MARGIN_BOTTOM' : 40,

'CONST_GRAPH_TITLE_Y_POSITION' : -20,

////////////////////////////////////////
// ExecScenarioGraph
////////////////////////////////////////
'CONST_GRAPH_WIDTH_EXEC_SCENARIO' : 600,
'CONST_GRAPH_HEIGHT_EXEC_SCENARIO' : 240,

////////////////////////////////////////
// ReductionByTimeZoneGraph
////////////////////////////////////////
'CONST_GRAPH_WIDTH_REDUCTION_BY_TIME_ZONE' : 300,
'CONST_GRAPH_HEIGHT_REDUCTION_BY_TIME_ZONE' : 360,

////////////////////////////////////////
// BarGraph
////////////////////////////////////////
'CONST_BAR_GRAPH_WIDTH' : 450,
'CONST_BAR_GRAPH_HEIGHT' : 300,

'CONST_BAR_GRAPH_MARGIN_LEFT' : 120,
'CONST_BAR_GRAPH_MARGIN_RIGHT' : 60,

////////////////////////////////////////
// PieGraph
////////////////////////////////////////
'CONST_PIE_GRAPH_WIDTH' : 300,
'CONST_PIE_GRAPH_HEIGHT' : 300,
'CONST_PIE_GRAPH_WIDTH_CENTER' : 50,

'CONST_PIE_GRAPH_MARGIN_LEFT' : 60,
'CONST_PIE_GRAPH_MARGIN_TOP' : 60,
'CONST_PIE_GRAPH_MARGIN_RIGHT' : 60,
'CONST_PIE_GRAPH_MARGIN_BOTTOM' : 60,

'CONST_PIE_GRAPH_TITLE_Y_POSITION' : -120,

};

Object.freeze(HINEMOS_RPA_CONST);