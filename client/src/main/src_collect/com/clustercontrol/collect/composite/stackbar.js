/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

性能[グラフ]の積み上げ棒グラフを描画するJavaScriptです。
 */
StackBarGraph = function(elementid, options, graphsize) {
var self = this;
this.chart = document.getElementById(elementid);
this.innerid = elementid;
this.graphtype = HINEMOS_COLLECT_CONST.CONST_BARSTACKGRAPH;

this.colors = d3.scale.category10();
this.startdate = options.startdate;
this.enddate = options.enddate;

this.bardata = [];
this.stackdata = [];

this.lineids = [];
this.colorlist = [];

this.causes = ["countinfo", "countwarn", "countcritical", "countunknown"];
this.prioritycolorlist = {"countinfo":HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_INFO, "countwarn":HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_WARN, "countcritical":HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_CRITICAL, "countunknown":HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_UNKNOWN};

this.itemname = options.itemname;
this.monitorid = options.monitorid;

this.summarytype;

// イベントフラグ情報
this.event = {};
this.event.object;
this.event.eventidlist;

//////////////////////////////////////////////////////////////
// Create Margins and Axis and hook our zoom function
//////////////////////////////////////////////////////////////
this.margin = {top: HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_TOP, right: HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_RIGHT, bottom: HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_BOTTOM, left: HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_LEFT};
this.width = HINEMOS_COLLECT_CONST.CONST_GRAPH_WIDTH - HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_LEFT - HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_RIGHT;
this.height = HINEMOS_COLLECT_CONST.CONST_GRAPH_HEIGHT - HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_TOP - HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_BOTTOM;

this.x = d3.scale.ordinal()
	.rangeRoundBands([0, this.width]);

this.y = y = d3.scale.linear()
	.rangeRound([this.height, 0]);

this.xAxis = d3.svg.axis()
	.scale(this.x)
	.orient("bottom")
	.tickFormat(function(d, i) {
		var length = self.stackdata[0].length;
		if (length > 10 && i % 4 != 1) {
			return "";
		}
		var format = self.getTermType(true);
		var parseDate = d3.time.format(format);
		return parseDate(d);
	});

this.yAxis = d3.svg.axis()
	.scale(this.y)
	.orient("left");

//////////////////////////////////////////////////////////////
// Generate our SVG object
//////////////////////////////////////////////////////////////
var graphheight = this.height;

this.vis = d3.select(this.chart)
	.append("svg")
	.attr("class", "svg_all")
		.attr("width", this.width + this.margin.left + this.margin.right)
		.attr("height", graphheight + this.margin.top + this.margin.bottom)
	.append("g")
		.attr("transform", "translate(" + this.margin.left + "," + this.margin.top + ")");

this.plot2 = this.vis.append("rect")
	.attr("class", "graph_background_block2")
	.attr("fill", HINEMOS_COLLECT_CONST.CONST_COLOR_WHITE)
	.attr("stroke", "#c0c0c0")
	.attr("stroke-width", HINEMOS_COLLECT_CONST.CONST_GRAPH_BORDER_WIDTH + "px")
	.attr("width", this.width + this.margin.left + this.margin.right)
	.attr("height", graphheight + this.margin.top + this.margin.bottom)
	.attr("x", -this.margin.left)
	.attr("y", -this.margin.top)
	.attr("pointer-events", "all");
	
// グラフタイトル部分の色
this.plot3 = this.vis.append("rect")
	.attr("class", "graph_title_rect")
	.attr("fill", HINEMOS_COLLECT_CONST.CONST_COLOR_HINEMOS_BASE)
	.attr("width", this.width + this.margin.left + this.margin.right)
	.attr("height", HINEMOS_COLLECT_CONST.CONST_GRAPH_TITLE_HEIGHT)
	.attr("x", -this.margin.left)
	.attr("y", -this.margin.top)
	.attr("pointer-events", "all");

// グラフの枠線x軸
this.vis.append("g")
	.attr("class", "x axis axis_xy")
	.attr("transform", "translate(0," + this.height + ")")
	.style("font-size", "8pt")
	.call(this.xAxis);

// グラフの枠線y軸
this.vis.append("g")
	.attr("class", "y axis axis_xy")
	.style("font-size", "8pt")
	.call(this.yAxis);
	
this.plot = this.vis.append("rect")
	.attr("class", "graph_background_block")
	.attr("fill", "none")
	.attr("width", this.width)
	.attr("height", graphheight)
	.attr("pointer-events", "all")
	this.plot.call(d3.behavior.zoom().x(this.x).y(this.y).on("zoom", self.redraw()));

// downloadボタンの作成
this.vis.append("g")
	.attr("class", "arrow")
	.append("rect")
	.attr("class", "download_rect")
	.attr("fill", HINEMOS_COLLECT_CONST.CONST_COLOR_WHITE)
	.attr("rx", "8px")
	.attr("ry", "8px")
	.attr("width", "30px")
	.attr("height", "14px")
	.attr("x", this.width-20)
	.attr("y", (-this.margin.top)+5)
	.attr("pointer-events", "all")
	.attr("cursor", "pointer");
this.vis.selectAll(".arrow") // title
	.append("title")
	.text("download...")
	.attr("cursor", "pointer");
this.vis.selectAll(".arrow") // text
	.append("text")
	.attr("class", "textpng")
	.text("PNG")
	.attr("font-size", "7pt")
	.attr("fill", HINEMOS_COLLECT_CONST.CONST_COLOR_HINEMOS_BASE)
	.attr("y", (-this.margin.top)+15)
	.attr("x", this.width-14)
	.attr("cursor", "pointer");
this.vis.selectAll(".arrow") // event
	.on("mousedown", function() { d3.event.stopPropagation();})
	.on("mouseup", function() { d3.event.stopPropagation();})
	.on("click", function(d) {
		capture(self);
	});

// グラフのタイトル表示(上部分)
var title = options.facilityname;
// マネージャ名、複数表示の場合は無し
var managername = "(" + options.realmanagername + ")";
if (getGraphConfig("data-total-flg")) {
	managername = "";
} else {
	// 単品表示の場合、pngファイル名にファシリティIDを含むため保持
	this.facilityid = options.realfacilityid;
	this.managername = options.realmanagername;
}
title+=managername;
var y_posi = (-this.margin.top) + 15;
var x_posi = this.width/2;
if (getGraphConfig("data-total-flg")) {
	title = graphsize;
}
this.title = title;
this.vis.append("g")
	.attr("class", "x axis axis_title")
	.attr("id", "title_top")
	.append("text")
	.style("text-anchor","left")
	.style("font-size","9pt")
	.style("font-weight","bold")
	.style("fill", HINEMOS_COLLECT_CONST.CONST_COLOR_WHITE)
	.attr("y", y_posi)
	.attr("x", -30)
	.text(truncateText(title, 60));


// グラフのitemName(左)表示
this.ylabel = options.ylabel;
var lefttitle = options.ylabel;
var i = 0;
for (i = 0; i < lefttitle.length; i++) {
	if (graphheight < i*11) {
		break;
	}
}
lefttitle = truncateText(lefttitle, i*2);
this.vis.append("g")
	.attr("class", "y axis axis_ylabel")
	.attr("id", "title_left")
	.append("text")
	.style("text-anchor","middle")
	.attr("transform","rotate(-90)")
	.attr("y", (-this.margin.left) + 14)
	.attr("x", -graphheight/2)
	.text(lefttitle)
	.style("font-size", "8pt")
	.style("fill", HINEMOS_COLLECT_CONST.CONST_COLOR_HINEMOS_BASE)
	.on("mouseout", function(){
		disableTooltip();
	})
	.on("mouseover", function(){
		var pagey = d3.event.pageY;
		var pagex = d3.event.pageX;
		// chromeとfirefoxはd3.event.pageXで取れる、IE10、11も取れるがIE10だと座標が若干ずれている
		// そのため、IE10の場合はif文で値を取得しなおす
		// firefoxでは[event is not defined.]になるため、ブラウザチェックする
		if (!checkBrowserKind("firefox") && (event.pageY == undefined || event.pageX == undefined)) { // IE9、10でのevent.pageX、event.pageYが取れない対策
			pagex = event.clientX + (document.body.scrollLeft || document.documentElement.scrollLeft);
			pagey = event.clientY + (document.body.scrollTop || document.documentElement.scrollTop);
		}
		var tt = self.ylabel;
		d3.select("body").select(".tooltip_mouse")
		.style("visibility", "visible")
		.style("font-size", "8pt")
		.style("padding", "10px")
		.html(tt)
		.style("top", (pagey-20)+"px")
		.style("left",(pagex+20)+"px");
	});



this.vis.append("clipPath")
	.attr("id", "clip")
	.append("rect")
	.attr("width", this.width)
	.attr("height", this.height);

//////////////////////////////////////////////////////////////
// Create D3 line object and draw data on our SVG object
//////////////////////////////////////////////////////////////

this.stack = d3.layout.stack();

// スタイルの設定
this.vis.selectAll(".axis path")
	.style("fill","none")
	.style("stroke",HINEMOS_COLLECT_CONST.CONST_COLOR_GRAY)
	.style("shape-rendering","crispEdges");

d3.select("body")
	.style("font-size", "10pt")
	.style("font-family", "Meiryo, メイリオ");
	
this.vis.selectAll(".axis text")
	.style("font-size", "8pt");

};// end of StackBarGraph

//////////////////////////////////////////////////////////////
// Method
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// 
StackBarGraph.prototype.update = function() {
	var self = this;
	
	self.createRects();

	// 表示の更新後にスタイルを指定する
	self.vis.selectAll(".axis line").style("stroke", HINEMOS_COLLECT_CONST.CONST_COLOR_GRAY);
	self.vis.selectAll(".axis text")
	.style("font-size", "8pt");
	
	self.animationStackbar();
}

//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// 線をアニメーション表示します。左から右に線を引きます。
// trimYからしか呼ばれません。updateから呼ぶと、ドラッグ中に頻繁にアニメーションが発生します。
StackBarGraph.prototype.animationLine = function() {
	var self = this;
	for (i = 0; i < self.lineids.length; i++) {
		var id = self.lineids[i];
		var line_len = self.points2[id].length;
		if (line_len == 0) {
			continue;
		}
		var target = self.vis.select('#' + id);
		var totalLength = target.node().getTotalLength();
		if (totalLength == 0) {
			// 線が無いfacility、処理続行するとエラーになるためcontinue
			continue;
		}
		target
			.attr("stroke-dasharray", totalLength + " " + totalLength)
			.attr("stroke-dashoffset", totalLength)
			.transition()
			.duration(HINEMOS_COLLECT_CONST.CONST_ANIMATION_INTERVAL)
			.ease("linear")
			.attr("stroke-dashoffset", 0);

	}
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
StackBarGraph.prototype.redraw = function() {
	var self = this;
	return function() {
		self.vis.select(".x.axis").call(self.xAxis);
		self.vis.select(".y.axis").call(self.yAxis);
		self.update();
	}
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
//指定されたidのグラフにプロットを追加します
//プロット情報は[x軸, y軸]の配列である必要があります
StackBarGraph.prototype.createPoints = function(groupkey, data_arr) {
	try {
		var self = bargraph[groupkey];
		var k = 0;
		var nodatacount = 0;
		var data_none = 0;

		for (line_key in data_arr) {
			self.lineids.push(line_key);
			self.colorlist[line_key] = self.colors(self.lineids.length);
			var item = data_arr[line_key];
			if (item.ishttpsce) {
				// http監視(シナリオ)の場合、重要度別の表示はしないためデータを空にする
				item.data = [];
			}
			var startdate = item.startdate;
			var enddate = item.enddate;
			self.startdate = startdate;
			self.enddate = enddate;
			self.summarytype = item.summarytype;
	
			var datearr = [];
			var collectid = item.collectid;
			if (collectid != "none") {
				// collectidが取れなかった場合は、"none"が入ってくる
				nodatacount++;
			}

			var thresholdinfomax = item.thresholdinfomax;
			var thresholdinfomin = item.thresholdinfomin;
			var thresholdwarnmax = item.thresholdwarnmax;
			var thresholdwarnmin = item.thresholdwarnmin;
			var facilityname = item.facilityname;
			var facilityid = item.facilityid;
			var managername = item.managername;
			if (self.bardata[managername] == null) {
				self.bardata[managername] = [];
			}
			if (self.bardata[managername][facilityid] == null) {
				self.bardata[managername][facilityid] = [];
			}
			
			for (var index in item.data) {
				var datedata = item.data[index];
				if (datedata[0] < startdate || enddate < datedata[0]) {
					// 指定範囲外
					continue;
				}
				// 値の取得と比較
				var value_y = Number(datedata[1]);
				var value_x = "date_" + Number(datedata[0]);
				var itemdata;
				itemdata = {};
				itemdata.countunknown = 0;
				itemdata.countinfo = 0;
				itemdata.countwarn = 0;
				itemdata.countcritical = 0;
				itemdata.basedate = Number(datedata[0]);
				itemdata.facilityname = item.facilityname;
				itemdata.realfacilityid = item.realfacilityid;
				itemdata.realmanagername = item.realmanagername;
				if (isReallyNaN(value_y)) {
					itemdata.countunknown++;
				} else if (thresholdinfomin <= value_y && value_y < thresholdinfomax) {
					itemdata.countinfo++;
				} else if (thresholdwarnmin <= value_y && value_y < thresholdwarnmax) {
					itemdata.countwarn++;
				} else {
					itemdata.countcritical++;
				}
				self.bardata[managername][facilityid][value_x] = itemdata;
			}
		}// データの整理終わり(日付ごとの集約終わり)
		
		self.createRects();

		// collectIdが取れなかったものがある場合の対処
		self.getCollectIdNone(self, nodatacount);
		self.redraw()();
	} catch(e) {
		var param = {};
		param.method_name = "javascript_error";
		param.exception = e;
		callJavaMethod(param);
	}	
};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// 最適なフォーマットを返します
// flgがtrueの場合は、「/」付で返します。
StackBarGraph.prototype.getTermType = function(dispflg) {
	var self = this;
	var startdate = self.startdate;
	var enddate = self.enddate;
	var datediff = enddate - startdate;//選択範囲の長さ
	var termtype = "";
	var summarytype = self.summarytype;
	if (dispflg) {
		switch (summarytype) {
		
			case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_AVG_HOUR:
			case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_MIN_HOUR:
			case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_MAX_HOUR:
				if (datediff < msec_day * 2) termtype = '%Hh';// 時単位、1~48
				if (msec_day * 2 <= datediff && datediff < msec_month * 2) termtype = '%m/%d';// 日単位、3~62
				if (msec_month * 2 <= datediff && datediff < msec_year * 3) termtype = '%y/%m';// 月単位、3~12
				if (msec_year * 3 <= datediff) termtype = '%Y'; // 年単位、3~10
			break;
			case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_AVG_DAY:
			case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_MIN_DAY:
			case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_MAX_DAY:
				if (datediff < msec_day * 2) termtype = '%m/%d';// 日単位、0~2
				if (msec_day * 2 <= datediff && datediff < msec_month * 2) termtype = '%m/%d';// 日単位、3~62
				if (msec_month * 2 <= datediff && datediff < msec_year * 3) termtype = '%y/%m';// 月単位、3~12
				if (msec_year * 3 <= datediff) termtype = '%Y'; // 年単位、3~10
			break;
			case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_AVG_MONTH:
			case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_MIN_MONTH:
			case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_MAX_MONTH:
				if (datediff < msec_day * 2) termtype = '%y/%m';// 月単位、0~1
				if (msec_day * 2 <= datediff && datediff < msec_month * 2) termtype = '%y/%m';// 月単位、0~2
				if (msec_month * 2 <= datediff && datediff < msec_year * 3) termtype = '%y/%m';// 月単位、3~24
				if (msec_year * 3 <= datediff) termtype = '%Y'; // 年単位、3~10
			break;
			default:
			
			break;
		}
	} else {
		switch (summarytype) {
			case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_AVG_HOUR:
			case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_MIN_HOUR:
			case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_MAX_HOUR:
				if (datediff < msec_day * 2) termtype = '%Y%m%d%H';// 時単位、1~48
				if (msec_day * 2 <= datediff && datediff < msec_month * 2) termtype = '%Y%m%d';// 日単位、3~62
				if (msec_month * 2 <= datediff && datediff < msec_year * 3) termtype = '%Y%m';// 月単位、3~12
				if (msec_year * 3 <= datediff) termtype = '%Y'; // 年単位、3~10
			break;
			case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_AVG_DAY:
			case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_MIN_DAY:
			case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_MAX_DAY:
				if (datediff < msec_day * 2) termtype = '%Y%m%d';// 日単位、0~2
				if (msec_day * 2 <= datediff && datediff < msec_month * 2) termtype = '%Y%m%d';// 日単位、3~62
				if (msec_month * 2 <= datediff && datediff < msec_year * 3) termtype = '%Y%m';// 月単位、3~12
				if (msec_year * 3 <= datediff) termtype = '%Y'; // 年単位、3~10
			break;
			case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_AVG_MONTH:
			case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_MIN_MONTH:
			case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_MAX_MONTH:
				if (datediff < msec_day * 2) termtype = '%Y%m';// 月単位、0~1
				if (msec_day * 2 <= datediff && datediff < msec_month * 2) termtype = '%Y%m';// 月単位、0~2
				if (msec_month * 2 <= datediff && datediff < msec_year * 3) termtype = '%Y%m';// 月単位、3~24
				if (msec_year * 3 <= datediff) termtype = '%Y'; // 年単位、3~10
			
			break;
			default:
			
			break;
		}
	}
	return termtype;
};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
//
StackBarGraph.prototype.createRects = function() {

	var self = this;
	var parseDate = d3.time.format(self.getTermType(false));

	var thresholddata = {};
	for (var managername in self.bardata) {
		var facilityinfolist = self.bardata[managername];
		for (var facilityid in facilityinfolist) {
			var dateinfolist = facilityinfolist[facilityid];
			for (var datekey in dateinfolist) {
				var dateinfo = dateinfolist[datekey];
				dateinfo.date = parseDate(new Date(dateinfo.basedate));//グラフの分割単位にdateを設定する
				var obj;
				if (thresholddata["date_" + dateinfo.date] == null) {
					obj = {};
					obj.countinfo = 0;
					obj.countwarn = 0;
					obj.countcritical = 0;
					obj.countunknown = 0;
				} else { 
					obj = thresholddata["date_" + dateinfo.date];
				}
				obj.basedate = dateinfo.basedate;
				obj.countinfo += dateinfo.countinfo;
				obj.countwarn += dateinfo.countwarn;
				obj.countcritical += dateinfo.countcritical;
				obj.countunknown += dateinfo.countunknown;
				obj.date = dateinfo.date;
				thresholddata["date_" + dateinfo.date] = obj;
			}
		}
	}
	
	if (thresholddata == null) return;
	var alllist = [];
	// 日付ごとの集約が終わったので、ただの配列(キー無し)にする
	var count = 0;
	var list = [];
	for (linekey in thresholddata) {
		var data = thresholddata[linekey];
		list.push(data);
	}
	
	// ソート
	list.sort(function(a, b) {
		if (a.basedate < b.basedate) { return -1 };
		if (a.basedate > b.basedate) { return 1 };
		return 0
	});

	self.stackdata = self.causes.map(function(c) {
		return list.map(function(d) {
			return {x: d.date, y: d[c], basedate:d.basedate, priority:c};
		});
	});
	
	var stackdata = self.stack(self.stackdata);

	self.x.domain(stackdata[0].map(function(d) { 
		var date = new Date(d.basedate);
	return date; }));
	self.y.domain([0, d3.max(stackdata[stackdata.length - 1], function(d) { 
	return d.y0 + d.y; })]).nice();
	
	self.vis.selectAll(".layer").remove();
	var layer = self.vis.selectAll(".layer")
	.data(stackdata)
	.enter().append("g")
	.attr("class", function(d) {
		if (d.length == 0) return "layer";
		return "layer " + d[0].priority;})
	.style("fill", function(d) {
		if (d == null || d[0] == null) return;
		return self.prioritycolorlist[d[0].priority];
	});

	var layer = self.vis.selectAll(".layer")
	.selectAll("rect")
	.data(function(d, i) {
		return d; })
	.enter()
	.append("rect")
	.attr("id", function(d) {
		return "stackrect " + d.basedate;})
	.attr("x", function(d) {
		var dateee = new Date(d.basedate);
		return self.x(new Date(d.basedate)); })
	.attr("y", function(d) { 
		return self.y(d.y + d.y0); })
	.attr("height", function(d) { 
		return self.y(d.y0) - self.y(d.y + d.y0); })
	.attr("width", self.x.rangeBand() - 1)
	.attr("clip-path", "url(#clip)")
	.on("mouseover", function(d) { // 線のmouseoverイベント
		var str = "";
		var omittednum = 0;
		var breakflg = false;
		breakall : for (var i = self.causes.length; i > 0; i--) {
			var cause = self.causes[i-1];
			for (var managername in self.bardata) {// managername = dummy
				var realmanagername = "";
				var str_sub = "";
				var facilitydatalist = self.bardata[managername];
				breakcauses: for (facilityid in facilitydatalist) {
					var dateinfolist = facilitydatalist[facilityid];
					var dateinfo = 0;
					var targetpriority = 0;
					var facilityname = "";
					var realfacilityid = "";
					for (datekey in dateinfolist) {
						if (d.x == dateinfolist[datekey].date) {
							dateinfo = dateinfolist[datekey];
							targetpriority += dateinfo[cause];
							facilityname = dateinfo.facilityname;
							realfacilityid = dateinfo.realfacilityid;
							realmanagername = dateinfo.realmanagername;// managername = realmanagername
						}
					}
					if (targetpriority == 0) continue;
					omittednum++;
					str_sub = str_sub + "<dd style='border-bottom-color :" + self.prioritycolorlist[cause] + ";border-left-color :" + self.prioritycolorlist[cause] + ";word-wrap: break-word;'>" + facilityname + "(" + realfacilityid + ") : " + targetpriority + "</dd>";
					if (omittednum == HINEMOS_COLLECT_CONST.CONST_STACKED_TOOLTIP_COUNT) {
						str_sub = str_sub + "<dd style='border-bottom-style:dashed;border-bottom-color :#ffffff;border-left-color :#ffffff;'>omitted below</dd>";
						breakflg = true;
						break breakcauses;
					
					}
				}
				if (str_sub == "") continue;
				str += "<dt style='word-wrap: break-word;'>" + realmanagername + "</dt>" + str_sub;
				if (breakflg) break breakall;// 指定件数以上のツールチップを出さないようにbreakする
			}
		}
		str = "<dl>" + str + "</dl>";
		d3.select("body")
		.select(".tooltip_mouse")
		.style("visibility", "visible")
		.style("width", "300px")
		.html(function() {
			return str;
		});
	})
	.on("mousemove", function(d) { // 線のmousemoveイベント
		var pagey = d3.event.pageY;
		var pagex = d3.event.pageX;
		// chromeとfirefoxはd3.event.pageXで取れる、IE10、11も取れるがIE10だと座標が若干ずれている
		// そのため、IE10の場合はif文で値を取得しなおす
		// firefoxでは[event is not defined.]になるため、ブラウザチェックする
		if (!checkBrowserKind("firefox") && (event.pageY == undefined || event.pageX == undefined)) { // IE9、10でのevent.pageX、event.pageYが取れない対策
			pagex = event.clientX + (document.body.scrollLeft || document.documentElement.scrollLeft);
			pagey = event.clientY + (document.body.scrollTop || document.documentElement.scrollTop);
		}
		d3.select("body").select(".tooltip_mouse").style("top", (pagey-20)+"px").style("left",(pagex+10)+"px");
	})
	.on("mouseout", function(d) { // 線のmouseoutイベント
		disableTooltip();
	});

};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// collectIdが取れなかったノードがある場合に、グラフの上に表示されているタイトルを変更します
// 線グラフ、積み上げグラフ共有関数です
StackBarGraph.prototype.getCollectIdNone = function(self, datacount) {
	var nodestr = getGraphMessages("mess-nodes");
	var totalstr = "(" + getGraphMessages("mess-total");
	if (getGraphConfig("data-total-flg")) {
		var graphsize = self.vis.select("#title_top").text();
		if (graphsize.indexOf(nodestr) == -1) {
			if (datacount != Number(graphsize)) {
				// まとめて表示、かつ、collectidが取れなかったものがある場合はタイトルを変更する
					// 未書き換えの場合のみ
					var newtitle = datacount + nodestr + totalstr + Number(graphsize) + nodestr + ")";
					self.vis.select("#title_top text")
					.text(newtitle);
					// グラフタイトルをメンバに抑える
					self.title = newtitle;
				if (datacount == 0) {
					// 全グラフ数と監視対象外ファシリティ数が同じ場合は背景をグレーにする
					dispNoData(self);
				}
			} else {
				// 未書き換えの場合のみ
				var newtitle = Number(graphsize) + nodestr;
				self.vis.select("#title_top text")
				.text(newtitle);
				// グラフタイトルをメンバに抑える
				self.title = newtitle;
			}
		}
	} else {
		if (datacount != 0) {
			// 背景はなし(線あり)
			self.vis.selectAll(".graph_background_block").style("fill", "none");
		} else {
			// 単体表示でcollectidが取れなかった場合は、背景をグレーにする(tickと同じ色)
			dispNoData(self);
		}
	}
};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
//すべてのグラフベースの線を消します
// start_date:表示範囲の最古日時、end_date:表示範囲の最新時刻
StackBarGraph.prototype.removePoints = function(start_date, end_date, alldel) {
	try {
		for (var keyValue in bargraph) {
			var self = bargraph[keyValue];
			self.startdate = start_date;
			self.enddate = end_date;
			for (var managername in self.bardata) {
				var managerinfolist = self.bardata[managername];
				for (var facilityid in managerinfolist) {
					var facilityinfolist = managerinfolist[facilityid];
					for (var datekey in facilityinfolist) {
						var dateinfo = facilityinfolist[datekey];
						if (dateinfo.basedate < start_date || dateinfo.basedate > end_date) {
							delete facilityinfolist[datekey];
						}
					}
				}
			}
			self.redraw()();
		}
	} catch (e) {
		var param = {};
		param.method_name = "javascript_error";
		param.exception = e;
		callJavaMethod(param);
	}
};// end of "removePoints"
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
StackBarGraph.prototype.animationStackbar = function() {
	var self = this;
	self.vis.select("#clip rect").attr("width", 0);

	self.vis.select("#clip rect")
	.transition().duration(HINEMOS_COLLECT_CONST.CONST_ANIMATION_INTERVAL)
	.attr("width", self.width);
} // end of animationStackbar
//////////////////////////////////////////////////////////////
// Method
//////////////////////////////////////////////////////////////

ControlBarGraph = {};

// chart_blockの子要素をすべて削除する
// また、保持しているグラフ情報も削除する
ControlBarGraph.delDiv = function () {
	try {
		var aNode = document.getElementById("chart_block");
		for (var i =aNode.childNodes.length-1; i>=0; i--) {
			aNode.removeChild(aNode.childNodes[i]);
		}
		bargraph = null;
		bargraph = [];
		// 設定情報を初期化します
		clearGraphConfig();
		// 画面の左上に移動
		window.scrollTo(0, 0);
	} catch(e) {
		var param = {};
		param.method_name = "javascript_error";
		param.exception = e;
		callJavaMethod(param);
	}
};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// グラフ1つ専用のdivを作成する
// 親のdiv(itemName別)のサイズも変更する
ControlBarGraph.addDiv = function (parentid, name) {
	try {
		var div_child = document.createElement('div');
		div_child.id = name;//total:monitorId, single:facilityId_managerName_monitorId
		div_child.classList.add("chart");
		var height = HINEMOS_COLLECT_CONST.CONST_GRAPH_HEIGHT;
		div_child.style.width = HINEMOS_COLLECT_CONST.CONST_GRAPH_WIDTH + "px";
		div_child.style.height = height + "px";
		div_child.style.margin = HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN + "px";
		var div_block = document.getElementById('chart_block');
		var div_parent_id = "ALL";
		if (getGraphConfig("data-returnkind-flg")) {
			// 種別ごとに改行する場合は、種別divを作成する
			div_parent_id = parentid;
		}
		var div_parent = document.getElementById(div_parent_id);
		if (div_parent == undefined || div_parent == null) {
			// 対象の種別divが無い場合は作成する(初回のみ)
			var div_midchild = document.createElement('div');
			div_midchild.id = div_parent_id;
			div_block.appendChild(div_midchild)
			div_parent = document.getElementById(div_parent_id);
//			div_parent.style.background="white";
		}

		// 一番前にadd
		div_parent.appendChild(div_child);
		if (!getGraphConfig("data-return-flg")) {
			// 折り返しフラグがfalseの場合は、横幅を広げていく
			var childnum = div_parent.children.length * (HINEMOS_COLLECT_CONST.CONST_GRAPH_WIDTH + HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN*2 +HINEMOS_COLLECT_CONST.CONST_GRAPH_BORDER_WIDTH*2);
			// 横幅の調整、グラフの幅と左右のmarginと左右のborderの合計になる
			div_parent.style.width = childnum + 'px';
		}
	} catch (e) {
		var param = {};
		param.method_name = "javascript_error";
		param.exception = e;
		callJavaMethod(param);
	}
};

//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// グラフのベースを一気に追加します
ControlBarGraph.addGraphAtOnce = function (plotjson, graphSize, preferenceSize, settinginfo) {
	try {
		// 設定情報を解析して保持する
		setGraphConfig(settinginfo);
		// グラフ数カウント
		var graphcount = preferenceSize;

		var length = plotjson.all.length;
		for (var i = 0; i < length; i++) {
			var item = plotjson.all[i];
			var itemname = item.itemname;
			var monitorid = item.monitorid;
			var parentid = monitorid + itemname;
			var id = item.id;//total:monitorId, single:facilityId_managerName_monitorId
			ControlBarGraph.addDiv(parentid, id);
			graphcount = graphcount - graphSize;
			if (graphcount < 0) {
				graphSize = graphSize + graphcount;
			}
			bargraph[id] = new StackBarGraph(id, item, graphSize);
		}
	} catch (e) {
		var param = {};
		param.method_name = "javascript_error";
		param.exception = e;
		callJavaMethod(param);
	}
};

//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
	// プロットを一気に追加します
ControlBarGraph.addPlotAtOnce = function (plotjson) {
	try {
		// tooltipを消す
		disableTooltip();
		
		var length = plotjson.all.length;
		var data_arr = [];
		// データの整理
		for (var i = 0; i < length; i++) {
			var item = plotjson.all[i];
			var groupid = item.groupid;
			var id = item.facilityid + "_" + item.managername;// + item.collectid; // TODO
			if (data_arr[groupid] == null) {
				data_arr[groupid] = [];
			}
			data_arr[groupid][id] = item;
		}
		for (var groupkey in data_arr) {
			StackBarGraph.prototype.createPoints(groupkey, data_arr[groupkey]);
		}
	} catch (e) {
		var param = {};
		param.method_name = "javascript_error";
		param.exception = e;
		callJavaMethod(param);
	}
};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// 折れ線グラフか積み上げ面グラフ化で分岐させます
ControlBarGraph.trimBranch = function() {
	// nop
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// すべてのグラフの表示範囲(x軸範囲)を変更します
// ignoreidを指定した場合、そのignoreidのグラフの表示範囲は変更しません
// 積み上げ面グラフの場合もこのメソッドを使用する
ControlBarGraph.trimXAxis = function(xmin, xmax, ignoreid) {
	// nop
};
