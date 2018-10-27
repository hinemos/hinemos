/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 *
 * 性能[グラフ]の折れ線グラフと積み上げ面グラフを描画するJavaScriptです。
 */
NewGraph = function(elementid, options, graphsize) {
var self = this;
this.chart = document.getElementById(elementid);
this.innerid = elementid;
this.graphtype = HINEMOS_COLLECT_CONST.CONST_LINESTACKGRAPH;

//this.options = options || {};
this.startdate = options.startdate;
this.enddate = options.enddate;
this.sliderstartdate = options.sliderstartdate;
this.sliderenddate = options.sliderenddate;
//console.log("start:" + new Date(Number(this.sliderstartdate)));
//console.log("end:" + new Date(Number(this.sliderenddate)));

this.colors = d3.scale.category10();

this.baseheight = Number((self.chart.style.height).replace(/px/g, "")) - HINEMOS_COLLECT_CONST.CONST_GRAPH_RIGHT_LEGEND_HEIGHT;

this.lineids = [];
this.points2 = [];// 線の情報
this.stackdata = [];// 線の情報
this.points = null;// プロットの情報(g-tag)
this.node = null;// 積み上げ情報(g-tag)
this.colorlist = [];
this.forecastslist = []; // 未来予測の係数リスト(key:lineid, value:)
this.summarytype = HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_AVG_HOUR;
this.itemname = options.itemname;
this.monitorid = options.monitorid;
this.displayname = options.displayname;
this.prediction = [];// 将来予測の情報
this.starlist = [];

this.graphrange;

// ylabelが領域を超えると「…」に置き換えられてしまうので置き換え前の情報を保持(cap用)
this.title = null;
this.ylabel = null;
this.facilityid = null;// 単品の場合、titleにファシリティ名が入っているためファシリティIDを保持(重複防止)
this.managername = null;

// イベントフラグ情報
this.event = {};
this.event.object;
this.event.eventidlist;
this.displineid = "";

//////////////////////////////////////////////////////////////
// Create Margins and Axis and hook our zoom function
//////////////////////////////////////////////////////////////
this.margin = {top: HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_TOP, right: HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_RIGHT, bottom: HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_BOTTOM, left: HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_LEFT};
this.width = HINEMOS_COLLECT_CONST.CONST_GRAPH_WIDTH - HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_LEFT - HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_RIGHT;
this.height = HINEMOS_COLLECT_CONST.CONST_GRAPH_HEIGHT - HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_TOP - HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_BOTTOM;

this.x = d3.time.scale()
		.domain([new Date(options.startdate), new Date(options.enddate)])
		.range([0, this.width]);

this.y = d3.scale.linear()
		.domain([-1, 100])
		.range([this.height, 0]);

//ticksの算出
var date_diff = options.startdate - options.enddate;
var ticks = 10;
switch (date_diff) {
case msec_day :
	ticks = 5;
	break;
case msec_week :
	ticks = 4;
	break;
case msec_month:
	ticks = 3;
	break;
case msec_year:
	ticks = 3;
	break;
default:
	ticks = 6;
	break;
}
this.xAxis = d3.svg.axis()
	.scale(this.x)
	.tickSize(-this.height)
	.tickPadding(4)
	.ticks(ticks)
	.tickFormat(d3_time_scaleLocalFormat)
	.orient("bottom");

this.yAxis = d3.svg.axis()
	.scale(this.y)
	.tickPadding(4)
	.ticks(8)
	.tickSize(-this.width)
	.tickFormat(function(d, i) {
		if ((d + "").length > 7) {
			// 指数表記、小数点2桁
			d = d.toExponential(2);
		}
		return d;
	})
	.orient("left");

//////////////////////////////////////////////////////////////
// Generate our SVG object
//////////////////////////////////////////////////////////////
var legendflg = getGraphConfig("data-legend-flg");
var graphheight = this.height;
if (legendflg) graphheight += HINEMOS_COLLECT_CONST.CONST_GRAPH_RIGHT_LEGEND_HEIGHT;

this.vis = d3.select(this.chart)
	.append("svg")
	.attr("class", "svg_all")
		.attr("width", this.width + this.margin.left + this.margin.right)
		.attr("height", graphheight + this.margin.top + this.margin.bottom)
	.append("g")
		.attr("transform", "translate(" + this.margin.left + "," + this.margin.top + ")");
//		this.vis.call(d3.behavior.zoom().x(this.x).y(this.y).on("zoom", zoommethod))
//		this.vis.call(d3.behavior.drag().on("dragend", function() {self.upup();}));

this.plot2 = this.vis.append("rect")
	.attr("class", "graph_background_block2")
	.attr("fill", HINEMOS_COLLECT_CONST.CONST_COLOR_WHITE)
	.attr("stroke", "#c0c0c0")
	.attr("stroke-width", HINEMOS_COLLECT_CONST.CONST_GRAPH_BORDER_WIDTH + "px")
	.attr("width", this.width + this.margin.left + this.margin.right)
	.attr("height", (graphheight + this.margin.top + this.margin.bottom))
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
	.attr("height", this.height)
	.attr("pointer-events", "all");
//	this.plot.call(d3.behavior.zoom().x(this.x).y(this.y).on("zoom", zoommethod));

this.vis.append("g")
	.attr("id", "upperlower")
	.attr("clip-path", "url(#clip)");

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
this.title = title; // cap用
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
this.vis.append("g")
	.attr("class", "y axis axis_ylabel")
	.attr("id", "title_left")
	.append("text")
	.style("text-anchor","middle")
	.attr("transform","rotate(-90)")
	.attr("y", (-this.margin.left) + 14)
	.attr("x", -graphheight/2)
	.text(options.ylabel)
	.style("font-size", "8pt")
	.style("text-anchor", "middle")
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

if (getGraphConfig("data-stack-flg")) {

// 積み上げ面描画できているのか
this.is_disp_success = true;


	this.vis.append("g").attr("id", "node_group");

	this.area = d3.svg.area()
			.x(function(d) {
			 return self.x(new Date(d.date)); })
			.y0(function(d) {
			 return self.y(d.y0); })
			.y1(function(d) {
			 return self.y(d.y0 + d.y); });

	this.stack = d3.layout.stack()
			.values(
			function(d) {
			// values:array[x],values[0].date,values[0].y
			return d.values; });
	
	// グラフにmouseoverで線が出てくる処理
	var focus = this.vis.append("g").style('display', 'none');
	focus.append('line')
		.attr('id', 'focusLineX')
		.attr('class', 'focusLine')
		.style("fill", "none")
		.style("stroke", "steelblue")
		.style("stroke-width", "0.5px")
		.attr("clip-path", "url(#clip)");
//	focus.append('line')
//			.attr('id', 'focusLineY')
//			.attr('class', 'focusLine');

	var bisectDate = d3.bisector(function(d) { 
		return d.date;
		 })
		 .left;
		
	this.vis.append('rect')
		.attr('class', 'overlay')
		.style("fill", "none")
		.style("stroke", "none")
		.attr("pointer-events", "all")
		.attr('width', this.width)
		.attr('height', graphheight)
		.on('mouseover', function() { 
			focus.style('display', null);
		 })
		.on('mouseout', function() { 
			focus.style('display', 'none');
			disableTooltip();
		})
		.on('mousemove', function() { 
			// 歯抜けしている場合はツールチップを出さない
			if (!self.is_disp_success) {
				return;
			}
			var mouse = d3.mouse(this);
			var mouseDate = self.x.invert(mouse[0]);
			if (self.stackdata.length == 0) return;
			var i = bisectDate(self.stackdata[0].values, mouseDate); // returns the index to the current data item

			var d0 = self.stackdata[0].values[i - 1]
			var d1 = self.stackdata[0].values[i];
			if (d0 == undefined || d1 == undefined) return;
			// work out which date value is closest to the mouse
			var d = mouseDate - d0[0] > d1[0] - mouseDate ? d1 : d0;

			var x = self.x(new Date(d.date));

			var ydomain = self.y.domain();
			focus.select('#focusLineX')
					.attr('x1', x).attr('y1', self.y(ydomain[0]))
					.attr('x2', x).attr('y2', self.y(ydomain[1]));

			// 表示する文言の整理
			var parseDate = d3.time.format('%Y/%m/%d %H:%M:%S');
			var buffer = "";
			var date = "-";
			var strarr = [];
			for (var j = 0; j < self.stackdata.length; j++) {
				var lineinfo = self.stackdata[j];
				var managername = lineinfo.managername;
				if (strarr[managername] == null) {
					strarr[managername] =[];
				}
				var facilityid = lineinfo.facilityid;
				var realfacilityid = lineinfo.realfacilityid;
				var facilityname = lineinfo.facilityname;
				var realmanagername = lineinfo.realmanagername;
				var targetdate = lineinfo.values[i-1];
				var y_value = "";
				if (targetdate != undefined) {
					date = parseDate(new Date(lineinfo.values[i-1].date));
					y_value = lineinfo.values[i-1].y;
				}
				var strobj = {};
				strobj.realmanagername = realmanagername;
				strobj.facilityid = realfacilityid;
				strobj.facilityname = facilityname;
				strobj.y_value = y_value;
				strobj.color = self.colorlist[managername + "_" + facilityid];
				strarr[managername].push(strobj);// 入れる場所を指定するTODO
			}
			
			buffer = "<dl>";
			var count = 0;
			limit:
			for (managername in strarr) {
				var managerlist =strarr[managername];
				var realmanagername = managerlist[0].realmanagername;
				buffer += "<dt style='word-wrap: break-word;'>" + realmanagername + "  :  <" + date + "></dt>";
				for (var k = managerlist.length; k > 0; k--) {
					var strobj = managerlist[k-1];
					buffer += "<dd style='border-bottom-color :" + strobj.color + ";border-left-color :" + strobj.color + ";word-wrap: break-word;'>" 
					+ strobj.facilityname + "(" + strobj.facilityid + ") : " + strobj.y_value + "</dd>";
					count++;
					if (count >= HINEMOS_COLLECT_CONST.CONST_STACKED_TOOLTIP_COUNT) {
						buffer += "<dd style='border-bottom-style:dashed;border-bottom-color :#ffffff;border-left-color :#ffffff;'>omitted below</dd>";
						// 表示件数制限によりループを抜ける
						break limit;
					}
				}
			}
			buffer += "</dl>";
			var pagey = d3.event.pageY;
			var pagex = d3.event.pageX;
			// chromeとfirefoxはd3.event.pageXで取れる、IE10、11も取れるがIE10だと座標が若干ずれている
			// そのため、IE10の場合はif文で値を取得しなおす
			// firefoxでは[event is not defined.]になるため、ブラウザチェックする
			if (!checkBrowserKind("firefox") && (event.pageY == undefined || event.pageX == undefined)) { // IE9、10でのevent.pageX、event.pageYが取れない対策
				pagex = event.clientX + (document.body.scrollLeft || document.documentElement.scrollLeft);
				pagey = event.clientY + (document.body.scrollTop || document.documentElement.scrollTop);
			}
			d3.select("body").select(".tooltip_mouse")
			.style("visibility", "visible")
			.style("width", "300px")
			.html(buffer)
			.style("top", (pagey-20)+"px")
			.style("left",(pagex+100)+"px");
		});

} else {
	// 通常線向け(x、yともにx関数、y関数を実施)
	this.line = d3.svg.line()
		.interpolate("linear")
		.x(function(d, i) {
			return this.x(d.x);
		})
		.y(function(d, i) {
			if (ControlGraph.isAvg(this.displineid)) {
				return this.y(d.avg);
			}
			if (ControlGraph.isSig(this.displineid)) {
				return this.y(d.sig);
			}
			if (ControlGraph.isSim(this.displineid)) {
				return this.y(d.sim);
			}
			if (ControlGraph.isApprox(this.displineid)) {
				return this.y(d.app);
			}
			if (ControlGraph.isApproxTarget(this.displineid)) {
				return this.y(d.tar);
			}
			return this.y(d.y);
		});
}

// イベントフラグ向け(x(日時)だけをx関数実施)
this.eventline = d3.svg.line()
	.interpolate("linear")
	.x(function(d, i) {
		return this.x(d.x);
	})
	.y(function(d, i) {
		return d.y;
	});

// スタイルの設定
this.vis.selectAll(".axis path")
	.style("fill","none")
	.style("stroke", HINEMOS_COLLECT_CONST.CONST_COLOR_GRAY)
	.style("shape-rendering","crispEdges");

d3.select("body")
	.style("font-size", "10pt")
	.style("font-family", "Meiryo, メイリオ");
	
this.vis.selectAll(".axis text")
	.style("font-size", "8pt");

};// end of NewGraph

//////////////////////////////////////////////////////////////
// Method
//////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////
// dragが終了したとき(dragend)に呼ばれる
//////////////////////////////////////////////////////////////
NewGraph.prototype.upup = function() {
	var self = this;
	
	var xaxis_min = self.x.domain()[0],
	xaxis_max = self.x.domain()[1];
	var yaxis_min = self.y.domain()[0],
	yaxis_max = self.y.domain()[1];
	var param = {};
	param.method_name = "mouseup";
	param.xaxis_min = xaxis_min.getTime();
	param.xaxis_max = xaxis_max.getTime();
	param.yaxis_min = yaxis_min;
	param.yaxis_max = yaxis_max;
	param.id = this.innerid;// インスタンスの名前
	
	// 見えなくなる部分は消す
	if (getGraphConfig("data-stack-flg")) {
		self.removeStack(param.xaxis_min, param.xaxis_max);
	} else {
		self.removePoints(param.xaxis_min, param.xaxis_max, false);
	}
	callJavaMethod(param);
	
	// createPointsしてからmoveしないとplot位置がづれる
	// 積み上げ面グラフも同じメソッドを使用する
	ControlGraph.trimXAxis(param.xaxis_min, param.xaxis_max, param.id);
};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// ドラッグ・ズームイン・ズームアウトのたびに画面のアイテムを再描画します
NewGraph.prototype.update = function() {
	var self = this;

	// 折れ線の再描画
	for (i = 0; i < this.lineids.length; i++) {
		var id = this.lineids[i];
		var target = this.vis.select('#' + id);
		self.displineid = id;
		
		var newlinepoints = null;
		if (ControlGraph.isApprox(id)) {
			// 重複データの除去
			newlinepoints = this.points2[ControlGraph.getNormalLineId(id)].filter(function (x, i, self) {
				if (typeof(x.app) != "undefined" && x.app != null) {
					return true;
				}
				return false;
			});
		} else if (ControlGraph.isApproxTarget(id)) {
				// 重複データの除去
				newlinepoints = this.points2[ControlGraph.getNormalLineId(id)].filter(function (x, i, self) {
					if (typeof(x.tar) != "undefined" && x.tar != null) {
						return true;
					}
					return false;
				});
		} else if (ControlGraph.isAvg(id)) {
			// 重複データの除去
			newlinepoints = this.points2[ControlGraph.getNormalLineId(id)].filter(function (x, i, self) {
				if (typeof(x.avg) != "undefined" && x.avg != null) {
					return true;
				}
				return false;
			});
		} else if (ControlGraph.isSig(id)) {
			// 重複データの除去
			newlinepoints = this.points2[ControlGraph.getNormalLineId(id)].filter(function (x, i, self) {
				if (typeof(x.sig) != "undefined" && x.sig != null) {
					return true;
				}
				return false;
			});
		} else if (ControlGraph.isSim(id)) {
			// 重複データの除去
			newlinepoints = this.points2[ControlGraph.getNormalLineId(id)].filter(function (x, i, self) {
				if (typeof(x.sim) != "undefined" && x.sim != null) {
					return true;
				}
				return false;
			});
		} else {
			// 重複データの除去
			newlinepoints = this.points2[id].filter(function (x, i, self) {
				if (typeof(x.y) != "undefined" && x.y != null) {
					return true;
				}
				return false;
			});
		}
		target.attr('d', this.line(newlinepoints));
	}

	// イベントフラグの線の再描画
	if (!this.vis.select(".eventflagline").empty()) {
		for (i = 0; this.event.eventidlist != null && i < this.event.eventidlist.length; i++) {
			var id2 = this.event.eventidlist[i];
			this.vis.select("#eventflagline" + id2).attr('d', this.eventline(self.event.object[id2]));
		}
	}

	// イベントフラグの線の再描画
	if (!this.vis.select(".predictiontarget").empty()) {
		for (i = 0; this.starlist != null && i < this.starlist.length; i++) {
			var star = this.starlist[i];
			this.vis.select("#predictiontarget" + star.lineid).attr('transform', function(d, i) {
				return "translate(" + self.x(d.x) + "," + self.y(d.y) + ")"; 
			});
		}
	}

	// プロットの再描画
	this.points.selectAll('circle').attr("transform", function(d, i) {
		if (ControlGraph.isAvg(d.lineid)) {
			return "translate(" + self.x(d.point.x) + "," + self.y(d.point.avg) + ")"; 
		}
		if (ControlGraph.isSig(d.lineid)) {
			return "translate(" + self.x(d.point.x) + "," + self.y(d.point.sig) + ")"; 
		}
		if (ControlGraph.isSim(d.lineid)) {
			return "translate(" + self.x(d.point.x) + "," + self.y(d.point.sim) + ")"; 
		}
		if (ControlGraph.isApprox(d.lineid)) {
			return "translate(" + self.x(d.point.x) + "," + self.y(d.point.app) + ")"; 
		}
		if (ControlGraph.isApproxTarget(d.lineid)) {
			return "translate(" + self.x(d.point.x) + "," + self.y(d.point.tar) + ")"; 
		}
		return "translate(" + self.x(d.point.x) + "," + self.y(d.point.y) + ")"; 
	});
	
	// 表示の更新後にスタイルを指定する
	self.vis.selectAll(".axis line").style("stroke", HINEMOS_COLLECT_CONST.CONST_COLOR_GRAY);
	self.vis.selectAll(".axis text")
	.style("font-size", "8pt");
};// end of update
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
NewGraph.prototype.redraw = function() {
	var self = this;
	return function() {
		self.vis.select(".x.axis").call(self.xAxis);
		self.vis.select(".y.axis").call(self.yAxis);
		for (var i = 0; i < self.lineids.length; i++) {
			var linekey = self.lineids[i];
			if (ControlGraph.isApprox(linekey)) {
				var normal = ControlGraph.getNormalLineId(linekey);
				if (self.points2[normal].length > 0) {
					var facilityId = self.points2[normal][0].facilityid;
					self.approxPoints(facilityId, normal, self.startdate, self.enddate);
				}
			}
		}
		for (keyvalue in thresholdgraph) {
			var threshold = thresholdgraph[keyvalue];
			threshold.redraw();
		}
		self.redrawPlotData()(true);
		self.plot.call(d3.behavior.zoom().x(self.x).y(self.y).on("zoom", function() { self.redraw()();throttle(self.upup, self);}));
		self.update();
	}
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// ズームイン・アウト、グラフのドラッグ時にイベントが多発するのを抑止する
// 通知時にタイムアウトを設定し、interval[ms]間は処理を行わない
// interval[ms]経過後に処理を実施する、また、タイマー識別子を初期化して再度タイムアウト処理を受け付ける
// 折れ線・積み上げ共通関数
NewGraph.prototype.timer = null;
function throttle (callback, self) {
	var interval = 1500;
	if (NewGraph.prototype.timer != 0 && NewGraph.prototype.timer != null) return; // タイマーが設定されている場合は処理しない
	NewGraph.prototype.timer = setTimeout(function() {
		clearTimeout(NewGraph.prototype.timer);
		NewGraph.prototype.timer = 0;
		self.upup();
	}, interval);
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
//指定されたidのグラフにプロットを追加します
//プロット情報は[x軸, y軸]の配列である必要があります
NewGraph.prototype.createPoints = function(groupkey, data_arr, eventflaginfo) {
	try {
		var self = graph[groupkey];
		var k = 0;
		var nodatacount = 0;
		for (line_key in data_arr) {
			var item = data_arr[line_key];
			var facilityid = item.facilityid;
			var facilityname = item.facilityname;
			var managerName = item.managername;

			self.sliderstartdate = item.sliderstartdate;
			self.sliderenddate = item.sliderenddate;
			self.startdate = item.startdate;
			self.enddate = item.enddate;
			self.summarytype = item.summarytype;

			var collectid = item.collectid;
			if (collectid != "none") {
				// collectidが取れなかった場合は、"none"が入ってくる
				nodatacount++;
				self.graphrange = item.graphrange;
			}
			var measure = item.measure;
			self.addMeasure(self, measure);

			if (getGraphConfig("data-threshold-flg") && !item.ishttpsce) {
				// 閾値モードがtrueの場合、かつ、http監視(シナリオ)以外の場合
				var thresholdinfo = {};
				thresholdinfo.warn_min = item.thresholdwarnmin;
				thresholdinfo.warn_max = item.thresholdwarnmax;
				thresholdinfo.info_min = item.thresholdinfomin;
				thresholdinfo.info_max = item.thresholdinfomax;
				thresholdinfo.elementid = groupkey;
				thresholdinfo.itemname = item.itemname;
				thresholdinfo.monitorid = item.monitorid;
				thresholdinfo.managername = item.realmanagername;
				thresholdinfo.pluginid = item.pluginid;
				if (thresholdgraph[groupkey] != null) {
					ThresholdGraph.prototype.setThresholdParam(groupkey, thresholdinfo);
				} else {
					thresholdgraph[groupkey] = new ThresholdGraph(graph[groupkey], thresholdinfo);
				}
			}
			if (self.lineids.indexOf(line_key) < 0) {
				// 初回の場合はnewする
				self.lineids[k] = line_key;
				self.points2[line_key] = [];
				self.colorlist[line_key] = self.colors(self.lineids.length);
				if (getGraphConfig("data-approx-flg")) {
					// 近似フラグがONの場合は、近似グラフIDを作成する
					k++;
					self.lineids[k] = ControlGraph.getApproxLineId(line_key);
					self.colorlist[self.lineids[k]] = self.colorlist[line_key];
					k++;
					self.lineids[k] = ControlGraph.getApproxTargetLineId(line_key);
					self.colorlist[self.lineids[k]] = self.colorlist[line_key];
				}
				if (getGraphConfig("data-sigma-flg")) {
					k++;
					var sigmalineid = line_key + "avg";
					self.lineids[k] = sigmalineid;
					self.colorlist[sigmalineid] = self.colorlist[line_key];
					
					k++;
					sigmalineid = line_key + "sig";
					self.lineids[k] = sigmalineid;
					self.colorlist[sigmalineid] = self.colorlist[line_key];
					
					k++;
					sigmalineid = line_key + "sim";
					self.lineids[k] = sigmalineid;
					self.colorlist[sigmalineid] = self.colorlist[line_key];
				}
			}
			// 通常グラフ描画用のソート
			for (j = 0; j < item.data.length; j++) {
				var singledata = item.data[j];
				var newpoint = {};
				newpoint.x = Number(singledata[0]);
				if (singledata[1] != null && !isReallyNaN(singledata[1])) {
					newpoint.y = Number(singledata[1]);
				}
				if (getGraphConfig("data-approx-flg")) {
					if (singledata[1] != null && !isReallyNaN(singledata[1])
							&& Number(singledata[0]) >= item.now - item.predictionrange * 60 * 1000
							&& Number(singledata[0]) <= item.now) {
						newpoint.tar = Number(singledata[1]);
					}
				}
				if (getGraphConfig("data-sigma-flg")) {
					if (singledata[2] != null && !isReallyNaN(singledata[2])) {
						newpoint.avg = Number(singledata[2]); // 平均
						if (singledata[3] != null && !isReallyNaN(singledata[3])) {
							newpoint.sig = (Number(singledata[3]) * 2) + newpoint.avg; // 2p
							newpoint.sim = (Number(singledata[3]) * -2) + newpoint.avg; // -2p
						}
					}
				}
				newpoint.facilityid = facilityid;
				newpoint.realfacilityid = item.realfacilityid;
				newpoint.facilityname = facilityname;
				newpoint.managername = managerName;
				newpoint.realmanagername = item.realmanagername;
				self.points2[line_key].push(newpoint);
			}
			
			// 未来予測の係数
			var forecastsvalue = {};
			forecastsvalue.dateoffset = item.dateoffset;
			forecastsvalue.a0 = item.a0value;
			forecastsvalue.a1 = item.a1value;
			forecastsvalue.a2 = item.a2value;
			forecastsvalue.a3 = item.a3value;
			self.forecastslist[line_key] = forecastsvalue;
			
			// 
			var predictionTargetInfo = {};
			predictionTargetInfo.now = item.now;
			predictionTargetInfo.min = item.predictiontarget;
			predictionTargetInfo.str = item.predictiontargetstr;
			self.prediction[line_key] = predictionTargetInfo;
			
			// ソート
			self.points2[line_key].sort(function(a, b) {
				if (a.x < b.x) { return -1 };
				if (a.x > b.x) { return 1 };
				return 0
			});
			// 重複データの除去
			var newlinepoints = self.points2[line_key].filter(function (x, i, self) {
				var count = 0;
				for (count = 0; count < self.length; count++) {
					if (self[count].x == x.x) { // && typeof(x.y) != "undefined") {
						break;
					}
				}
				return count == i;
			});
			
			// 重複データ除去後のデータを正規データとする
			self.points2[line_key] = newlinepoints;
			
//			// 近似線の表示
//			if (getGraphConfig("data-approx-flg") && self.points2[line_key].length > 0) {
//				// 近似用のデータを計算する
//				self.approxPoints(facilityid, line_key, self.startdate, self.enddate);
//			}
			k++;
		}// データの整理が終了
		
		// グラフの描画(pathタグの追加、データは別途追加)
		self.vis.selectAll('.line')
		.data(self.lineids)
		.enter()
		.append("path")
			.attr("class", function(d, i) {
				if (!ControlGraph.isNormal(d) && !ControlGraph.isApproxTarget(d)) {
					return "line line_dash";
				}
				return "line";
			})
		.attr("id", function(d, i) {return d;})
		.style("stroke-width", HINEMOS_COLLECT_CONST.CONST_LINE_WIDTH_DEFAULT)
		.style("fill", "none")
		.attr("clip-path", "url(#clip)")
		.on("mouseover", function(d) { // 線のmouseoverイベント
			// functionの引数dとmouseoverされた線のidが異なるため、thisからidを取得する
			var targetlineid = this.id;
			var baseid = targetlineid;
			var linestyle = "solid";
			var linetype = "";
			if (!ControlGraph.isNormal(baseid)) {
				baseid = ControlGraph.getNormalLineId(baseid);
				linestyle = "dashed"; // 近似線のツールチップ表示は破線にする
			}
			if (ControlGraph.isApprox(targetlineid)) {
				linetype = "[APPROX]";
			} else if (ControlGraph.isAvg(targetlineid)) {
				linetype = "[AVERAGE]";
			} else if (ControlGraph.isSig(targetlineid)) {
				linetype = "[SIGMA]";
			} else if (ControlGraph.isSim(targetlineid)) {
				linetype = "[-SIGMA]";
			}
			var facilityname = self.points2[baseid][0].facilityname;
			var managername = self.points2[baseid][0].realmanagername;
			var realfacilityid = self.points2[baseid][0].realfacilityid;
			var str = "<dl>";
			str += "<dt style='border-bottom-style:" + linestyle + ";word-wrap: break-word;'>" + managername + "</dt>";
			str += "<dd style='border-bottom-color :" + self.colorlist[targetlineid] + ";border-left-color :" + self.colorlist[targetlineid] 
			+ ";border-bottom-style:" + linestyle + ";word-wrap: break-word;'>" + facilityname + "(" + realfacilityid + ")" + linetype + "</dd>";
			str += "</dl>";

			d3.select("body")
			.select(".tooltip_mouse")
			.style("visibility", "visible")
			.style("width", "300px")
			.html(str);
			
			// 別グラフに表示している同じマネージャ・facilityIdのものを太線にする
			var paths = document.getElementsByTagName("path");
			for (var i = 0; i < paths.length; i++) {
				var pathsingle = paths[i];
				if (pathsingle.id == targetlineid) {
					d3.select(pathsingle).style("stroke-width", HINEMOS_COLLECT_CONST.CONST_LINE_WIDTH_MOUSEOVER);
				}
			}
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
			var targetlineid = this.id;
			disableTooltip();
			var paths = document.getElementsByTagName("path");
			for (var i = 0; i < paths.length; i++) {
				var pathsingle = paths[i];
				if (pathsingle.id == targetlineid) {
					d3.select(pathsingle).style("stroke-width", HINEMOS_COLLECT_CONST.CONST_LINE_WIDTH_DEFAULT);
				}
			}
		})
		.attr('stroke', function(d,i){
			return self.colorlist[d];
		});
		
		// 線別スタイルの設定☆
		self.vis.selectAll("path.line")
		.style("stroke-dasharray", "5, 0");
		self.vis.selectAll("path.line_dash")
		.style("stroke-dasharray", "5, 3");
		
		for (j = 0; self.points != null && j < self.lineids.length; j++) {
			var id = self.lineids[j];
			// 初回以外はプロットも消す
			self.points.selectAll("g#" + id + " circle")
			.data(10)
			.exit()
			.remove();
		}
		// 初回のみ作成
		if (self.points == null) {
			// plotの追加(初回だけplotを囲うgを作成)
			self.points = self.vis.selectAll('.dots')
			.data(self.lineids)
			.enter()
			.append("g")
				.attr("class", "dots")
				.attr("id", function(d, i) {
					return d;
					})
			.attr("clip-path", "url(#clip)");
		}
		
		// 最小・最大・平均・最新の算出
		ControlGraph.calcMinMaxAvg(self);

		if (getGraphConfig("data-legend-flg")) {
			// 凡例を作成する、グラフを動かすたびに更新する
			self.createLegend();
		}
		// collectIdが取れなかったものがある場合の対処
		NewGraph.prototype.getCollectIdNone(self, nodatacount);
		
		// イベントフラグの線を描画
		self.createEventFlag(eventflaginfo);
		
		// 
		self.createPredictionTarget();
		
		// プロットの作成
		self.redrawPlotData()();
		self.update();
	} catch(e) {
		var param = {};
		param.method_name = "javascript_error";
		param.exception = e;
		callJavaMethod(param);
	}	
};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// 凡例を作成します。
// 折れ線・積み上げ共通です。(if文で分岐。)
// 凡例はグラフ表示を更新するたびに呼ぶ必要があります。
// (期間によって表示されるfacilityがことなるため)
NewGraph.prototype.createLegend = function() {
	var self = this;
	
	// 凡例を全消しする
	self.vis.selectAll("#legendgroup").remove();
	self.vis.selectAll(".legend").remove();

	var legendlist = [];
	if (getGraphConfig("data-stack-flg")) {
		for (var i = 0; i < self.stackdata.length; i++) {
			if (self.stackdata[i].values != null && self.stackdata[i].values.length != 0 && self.valueinfo[self.stackdata[i].name].size != 0) {
				legendlist.push(self.stackdata[i].name);
			}
			if (legendlist.length == HINEMOS_COLLECT_CONST.CONST_STACKED_TOOLTIP_COUNT) {
				legendlist.push("omitted below");
				break;
			}
		}
	} else {
		for (facimanekey in self.points2) {
			if (ControlGraph.isNormal(facimanekey)) {
				// 近似線は凡例表示しない
				if (self.points2[facimanekey] != null && self.points2[facimanekey].length != 0 && self.valueinfo[facimanekey].size != 0) {
					legendlist.push(facimanekey);
				}
				if (legendlist.length == HINEMOS_COLLECT_CONST.CONST_STACKED_TOOLTIP_COUNT) {
					legendlist.push("omitted below");
					break;
				}
			}
		}
	}
	
	
	var legendgroup = self.vis.append("g").attr("id", "legendgroup")
	.attr("transform", function(d, i) {
		return "translate(0,200)"; 
	});
	if (legendlist.length != 0) {
		var valuelabel = legendgroup
		.append("g")
		.attr("id", "valuelabel");
		
		valuelabel
		.append("text")
		.attr("transform", "translate(120,0)")
		.attr("font-size", "7pt")
		.text("min");
		
		valuelabel
		.append("text")
		.attr("transform", "translate(180,0)")
		.attr("font-size", "7pt")
		.text("avg");
		
		valuelabel
		.append("text")
		.attr("transform", "translate(240,0)")
		.attr("font-size", "7pt")
		.text("max");
		
		valuelabel
		.append("text")
		.attr("font-size", "7pt")
		.attr("transform", "translate(300,0)")
		.text("last");
	}
	
	var legend = legendgroup.selectAll(".legend")
	.data(legendlist)
	.enter().append("g")
	.attr("class", "legend")
	.attr("transform", function(d, i) {
		var x_posi = 0;
		var j = 0;
		x_posi = (-self.width) - 5;
		return "translate(" + x_posi + "," + (i * 20 + 8) + ")"; 
	});

	// rect(background)
	legend.append("rect")
	.attr("transform", "translate(320,-3.5)")
	.attr("width", 375)
	.attr("height", 20)
	.style("fill", function(d, i) {
		var color = "#e6e6fa";
		if (i % 2 == 1) {
			color = "#ffffff";
		}
		return color;
	});
	
	// pie
	legend.append("rect")
	.attr("rx", 5)
	.attr("ry", 5)
	.attr("transform", "translate(327,2)")
	.attr("width", 10)
	.attr("height", 10)
	.style("fill", function(d) { return self.colorlist[d]; });
	
	// facilityname
	legend.append("text")
	.attr("transform", "translate(340, 6)")
	.attr("dy", ".35em")
	.attr("font-size", "7pt")
	.on('mouseover', function(d) {
		// ファシリティ名をマウスオーバーで線を太く
		if (getGraphConfig("data-stack-flg")) {
			return;
		}
		var paths = document.getElementsByTagName("path");
		for (var i = 0; i < paths.length; i++) {
			var pathsingle = paths[i];
			if (pathsingle.id == d) {
				d3.select(pathsingle).style("stroke-width", HINEMOS_COLLECT_CONST.CONST_LINE_WIDTH_MOUSEOVER);
			}
		}
	 })
	.on('mouseout', function(d) { 
		// マウスオーバーが外れたら通常の線の太さに戻す
		if (getGraphConfig("data-stack-flg")) {
			return;
		}
		var paths = document.getElementsByTagName("path");
		for (var i = 0; i < paths.length; i++) {
			var pathsingle = paths[i];
			if (pathsingle.id == d) {
				d3.select(pathsingle).style("stroke-width", HINEMOS_COLLECT_CONST.CONST_LINE_WIDTH_DEFAULT);
			}
		}
	})
	.attr("fill", function(d) { // 文字色(省略文字は赤、それ以外は黒)
		if (getGraphConfig("data-stack-flg")) {
			for (var i = 0; i < self.stackdata.length; i++) {
				if (self.stackdata[i].name == d) {
					return HINEMOS_COLLECT_CONST.CONST_COLOR_BLACK; 
				}
			}
			return HINEMOS_COLLECT_CONST.CONST_COLOR_RED;
		} else {
			var target = self.points2[d];
			if (target == null) return HINEMOS_COLLECT_CONST.CONST_COLOR_RED;
		}
	})
	.style("text-anchor", "start")
	.text(function(d) {
		var facilityname = "";
		var managername = "";
		if (getGraphConfig("data-stack-flg")) {
			for (var i = 0; i < self.stackdata.length; i++) {
				if (self.stackdata[i].name == d) {
					var target = self.stackdata[i];
					facilityname = target.facilityname;
					managername = target.realmanagername;
				}
			}
			
			if (facilityname == "" && managername == "") {
				// 指定件数以上の場合は省略を表示
				return d;
			}
		} else {
			var target = self.points2[d];
			if (target == null) return d;
			facilityname = target[0].facilityname;
			managername = target[0].realmanagername;
		}
		return truncateText(facilityname, 20); 
	})
	.append("title") // 凡例マウスオーバーでフル出力
		.text(function(d) {
			if (getGraphConfig("data-stack-flg")) {
				for (var i = 0; i < self.stackdata.length; i++) {
					if (self.stackdata[i].name == d) {
						var target = self.stackdata[i];
						var facilityname = target.facilityname;
						var managername = target.realmanagername;
						return facilityname + "(" + managername + ")"; 
					}
				}
				// 指定件数以上の場合は省略を表示
				return d;
			} else {
				var target = self.points2[d];
				if (target == null) return d;
				var facilityname = target[0].facilityname;
				var managername = target[0].realmanagername;
				return facilityname + "(" + managername + ")"; 
			}
		});
		
	// min 
	legend.append("text")
	.attr("transform", "translate(477, 6)")
	.attr("dy", ".35em")
	.attr("font-size", "7pt")
	.attr("fill", function(d, i) { // 文字色(省略文字は赤、それ以外は黒)
		if (getGraphConfig("data-stack-flg")) {
			for (var i = 0; i < self.stackdata.length; i++) {
				if (self.stackdata[i].name == d) {
					return HINEMOS_COLLECT_CONST.CONST_COLOR_BLACK; 
				}
			}
			return HINEMOS_COLLECT_CONST.CONST_COLOR_RED;
		} else {
			var target = self.points2[d];
			if (target == null) return HINEMOS_COLLECT_CONST.CONST_COLOR_RED;
		}
	})
	.style("text-anchor", "middle")
	.text(function(d, i) {
		if (i == 10) return ""; // omitted below
		var targetvalue = "";
		var target = self.valueinfo[d];
		if (target == null || target.size == 0 || target.min == undefined) return "-";
		targetvalue = target.min;
		return targetvalue.toPrecision(3); 
	})
	.append("title") // 凡例マウスオーバーでフル出力
		.text(function(d) {
			var targetvalue = "";
			var target = self.valueinfo[d];
			if (target == null || target.size == 0 || target.min == undefined) return "-";
			targetvalue = target.min;
			return targetvalue; 
		});
		
	// avg
	legend.append("text")
	.attr("transform", "translate(537, 6)")
	.attr("dy", ".35em")
	.attr("font-size", "7pt")
	.attr("fill", function(d) { // 文字色(省略文字は赤、それ以外は黒)
		if (getGraphConfig("data-stack-flg")) {
			for (var i = 0; i < self.stackdata.length; i++) {
				if (self.stackdata[i].name == d) {
					return HINEMOS_COLLECT_CONST.CONST_COLOR_BLACK; 
				}
			}
			return HINEMOS_COLLECT_CONST.CONST_COLOR_RED;
		} else {
			var target = self.points2[d];
			if (target == null) return HINEMOS_COLLECT_CONST.CONST_COLOR_RED;
		}
	})
	.style("text-anchor", "middle")
	.text(function(d, i) {
		if (i == 10) return ""; // omitted below
		var targetvalue = "";
		var target = self.valueinfo[d];
		if (target == null || target.size == 0 || target.avg == undefined) return "-";
		targetvalue = target.avg;
		return targetvalue.toPrecision(3); 
	})
	.append("title") // 凡例マウスオーバーでフル出力
		.text(function(d) {
			var targetvalue = "";
			var target = self.valueinfo[d];
		if (target == null || target.size == 0 || target.avg == undefined) return "-";
			targetvalue = target.avg;
			return targetvalue; 
		});
		
	// max
	legend.append("text")
	.attr("transform", "translate(600, 6)")
	.attr("dy", ".35em")
	.attr("font-size", "7pt")
	.attr("fill", function(d) { // 文字色(省略文字は赤、それ以外は黒)
		if (getGraphConfig("data-stack-flg")) {
			for (var i = 0; i < self.stackdata.length; i++) {
				if (self.stackdata[i].name == d) {
					return HINEMOS_COLLECT_CONST.CONST_COLOR_BLACK; 
				}
			}
			return HINEMOS_COLLECT_CONST.CONST_COLOR_RED;
		} else {
			var target = self.points2[d];
			if (target == null) return HINEMOS_COLLECT_CONST.CONST_COLOR_RED;
		}
	})
	.style("text-anchor", "middle")
	.text(function(d, i) {
		if (i == 10) return ""; // omitted below
		var targetvalue = "";
		var target = self.valueinfo[d];
		if (target == null || target.size == 0 || target.max == undefined) return "-";
		targetvalue = target.max;
		return targetvalue.toPrecision(3); 
	})
	.append("title") // 凡例マウスオーバーでフル出力
		.text(function(d) {
			var targetvalue = "";
			var target = self.valueinfo[d];
			if (target == null || target.size == 0 || target.max == undefined) return "-";
			targetvalue = target.max;
			return targetvalue; 
		});
		
	// last
	legend.append("text")
	.attr("transform", "translate(657, 6)")
	.attr("dy", ".35em")
	.attr("font-size", "7pt")
	.attr("fill", function(d) { // 文字色(省略文字は赤、それ以外は黒)
		if (getGraphConfig("data-stack-flg")) {
			for (var i = 0; i < self.stackdata.length; i++) {
				if (self.stackdata[i].name == d) {
					return HINEMOS_COLLECT_CONST.CONST_COLOR_BLACK; 
				}
			}
			return HINEMOS_COLLECT_CONST.CONST_COLOR_RED;
		} else {
			var target = self.points2[d];
			if (target == null) return HINEMOS_COLLECT_CONST.CONST_COLOR_RED;
		}
	})
	.style("text-anchor", "middle")
	.text(function(d, i) {
		if (i == 10) return ""; // omitted below
		var min_value = "";
		var target = self.valueinfo[d];
		if (target == null || target.size == 0 || target.last == undefined) return "-";
		targetvalue = target.last;
		return targetvalue.toPrecision(3); 
	})
	.append("title") // 凡例マウスオーバーでフル出力
		.text(function(d) {
			var targetvalue = "";
			var target = self.valueinfo[d];
			if (target == null || target.size == 0 || target.last == undefined) return "-";
			targetvalue = target.last;
			return targetvalue; 
		});

	// 凡例件数によって縦幅を広げる
	var times = Math.round(legendlist.length);
	if (legendlist.length == 0) {
		times = 0;
	}
	// div
	var divheight = self.baseheight;
	var newheight = (Number(divheight) + HINEMOS_COLLECT_CONST.CONST_GRAPH_RIGHT_LEGEND_HEIGHT * times) + 20;
	self.chart.style.height = newheight + "px";
	
	// svg
	var svgheight = d3.select(self.chart).select(".svg_all").attr("height");
	var targetheight = newheight;
	// 大きいheightにあわすためd3.selectにする(画面のすべてのsvg_allに対して実施)
	d3.select(self.chart).select(".svg_all").attr("height", targetheight);
	// graph_background_block2
	var svgheight = self.vis.selectAll(".graph_background_block2").attr("height");
	self.vis.selectAll(".graph_background_block2").attr("height", targetheight);
	// 左のitemNameの位置を調整、ここに来るときには単位は追加済み
	var titleleftheight = (-(targetheight - this.margin.top - this.margin.bottom)/2);
	self.vis.select("#title_left text").attr("x", titleleftheight);
	
};// createLegend
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// 左に表示しているitemNameがグラフ描画領域を超えて表示されないように調整します
// すべてのグラフの凡例描画処理が終わってから処理を実施してください
NewGraph.prototype.cleanItemName = function() {
	for (var keyValue in graph) {
		var self = graph[keyValue];
		var svgheight = d3.select(self.chart).select(".graph_background_block2").attr("height");
		var lefttitle = self.vis.select("#title_left").text();
		if (self.ylabel == null) {
			self.ylabel = lefttitle;//cap用
		}
		var i = 0;
		for (i = 0; i < lefttitle.length; i++) {
			if (svgheight < i*14) {
				break;
			}
		}
		lefttitle = truncateText(lefttitle, i*2);
		self.vis.select("#title_left text").text(lefttitle);
	}
};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// プロット情報を描画します。
// プロットにツールチップを設定します。
NewGraph.prototype.redrawPlotData = function() {
	var self = this;
	return function(isOnlyApprox) {
		self.points.selectAll('.dot')
		.data(function(d, index){
			var dd = self.points2[d];
			if (!ControlGraph.isNormal(d)) {
				dd = self.points2[ControlGraph.getNormalLineId(d)];
			}
			var a = [];
			if (isOnlyApprox && getGraphConfig("data-approx-flg") && !ControlGraph.isApprox(d)) {
				// 近似線のプロットのみを表示させたい場合は、通常線は何もしない
				return a;
			}
			dd.forEach(function(point, i){
				if (ControlGraph.isNormal(d) && typeof(point.y) != "undefined") {
					a.push({'index': index, 'point': point, 'lineid':d});
				} else if (ControlGraph.isApprox(d) && typeof(point.app) != "undefined") {
					a.push({'index': index, 'point': point, 'lineid':d});
				} else if (ControlGraph.isApproxTarget(d) && typeof(point.tar) != "undefined") {
					a.push({'index': index, 'point': point, 'lineid':d});
				} else if (ControlGraph.isAvg(d) && typeof(point.avg) != "undefined") {
					a.push({'index': index, 'point': point, 'lineid':d});
				} else if (ControlGraph.isSig(d) && typeof(point.sig) != "undefined") {
					a.push({'index': index, 'point': point, 'lineid':d});
				} else if (ControlGraph.isSim(d) && typeof(point.sim) != "undefined") {
					a.push({'index': index, 'point': point, 'lineid':d});
				}
			});
			return a;
		})
		.enter()
		.append('circle')
		.attr('class','dot')
		.attr("r", function(d) {
			if (!ControlGraph.isNormal(d.lineid)) {
				return 0.1;
			}
			return 1;
		})
		.attr('fill', function(d,i){
			return self.colorlist[d.lineid];
		})
		.attr("transform", function(d) { 
			var lineid = d.lineid;
			
			if (ControlGraph.isAvg(lineid)) {
				return "translate(" + self.x(d.point.x) + "," + self.y(d.point.avg) + ")"; 
			}
			if (ControlGraph.isSig(lineid)) {
				return "translate(" + self.x(d.point.x) + "," + self.y(d.point.sig) + ")"; 
			}
			if (ControlGraph.isSim(lineid)) {
				return "translate(" + self.x(d.point.x) + "," + self.y(d.point.sim) + ")"; 
			}
			if (ControlGraph.isApprox(lineid)) {
				return "translate(" + self.x(d.point.x) + "," + self.y(d.point.app) + ")"; 
			}
			if (ControlGraph.isApproxTarget(lineid)) {
				return "translate(" + self.x(d.point.x) + "," + self.y(d.point.tar) + ")"; 
			}
			return "translate(" + self.x(d.point.x) + "," + self.y(d.point.y) + ")"; 
		})
		// tooltipの設定
		.on("mouseover", function(d) { // 点のmouseoverイベント
			var parseDate = d3.time.format('%Y/%m/%d %H:%M:%S');
			var linestyle ="solid";
			if (!ControlGraph.isNormal(d.lineid)) {
				linestyle = "dashed"; // 近似線のツールチップ表示は破線にする
			}
			var value = d.point.y;
			var linetype = "";
			if (ControlGraph.isApprox(d.lineid)) {
				value = d.point.app;
				linetype = "[APPROX]";
			} else if (ControlGraph.isAvg(d.lineid)) {
				value = d.point.avg;
				linetype = "[AVERAGE]";
			} else if (ControlGraph.isSig(d.lineid)) {
				value = d.point.sig;
				linetype = "[SIGMA]";
			} else if (ControlGraph.isSim(d.lineid)) {
				value = d.point.sim;
				linetype = "[-SIGMA]";
			}
			var str = "<dl>";
			str += "<dt style='border-bottom-style:" + linestyle + ";word-wrap: break-word;'>" + d.point.realmanagername + "  :  <" + parseDate(new Date(d.point.x)) + "></dt>";
			str += "<dd style='border-bottom-color :" + self.colorlist[d.lineid] + ";border-left-color :" + self.colorlist[d.lineid] 
			+ ";border-bottom-style:" + linestyle + ";word-wrap: break-word;'>" + d.point.facilityname + "(" + d.point.realfacilityid + ") : " + value + linetype + "</dd>";
			str += "</dl>";

			d3.select("body").select(".tooltip_mouse").style("visibility", "visible").style("width", "300px").html(str);
			var paths = document.getElementsByTagName("path");
			if (ControlGraph.isApprox(d.lineid)) {
				// 将来予測対象データ
				for (var i = 0; i < paths.length; i++) {
					var pathsingle = paths[i];
					if (pathsingle.id == d.lineid.slice(0, -3) + "tar") {
						d3.select(pathsingle).style("stroke-width", HINEMOS_COLLECT_CONST.CONST_LINE_WIDTH_MOUSEOVER);
					}
				}
			}
			for (var i = 0; i < paths.length; i++) {
				var pathsingle = paths[i];
				if (pathsingle.id == d.lineid) {
					d3.select(pathsingle).style("stroke-width", HINEMOS_COLLECT_CONST.CONST_LINE_WIDTH_MOUSEOVER);
				}
			}
		})
		.on("mousemove", function(d) { // 点のmousemoveイベント
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
		.on("mouseout", function(d) { // 点のmouseoutイベント
			disableTooltip();
			var paths = document.getElementsByTagName("path");
			if (ControlGraph.isApprox(d.lineid)) {
				// 将来予測対象データ
				for (var i = 0; i < paths.length; i++) {
					var pathsingle = paths[i];
					if (pathsingle.id == d.lineid.slice(0, -3) + "tar") {
						d3.select(pathsingle).style("stroke-width", HINEMOS_COLLECT_CONST.CONST_LINE_WIDTH_DEFAULT);
					}
				}
			}
			for (var i = 0; i < paths.length; i++) {
				var pathsingle = paths[i];
				if (pathsingle.id == d.lineid) {
					d3.select(pathsingle).style("stroke-width", HINEMOS_COLLECT_CONST.CONST_LINE_WIDTH_DEFAULT);
				}
			}
		});
	}
};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// フラグ情報を描画します。
// 折れ線・積み上げ共通関数です。
NewGraph.prototype.createEventFlag = function(eventflaginfo) {
	var self = this;
	// 線情報
	if (self.event == undefined) return;// フラグなしの場合は処理なしで戻る

	// 既存のイベント線の削除処理
	if (self.event.object != null) {
		for (eventkey in self.event.object) {
			var eventobj = self.event.object[eventkey];
			self.vis.select("#eventflagline" + eventkey).remove(); // pathタグの削除
			delete self.event.object[eventkey]; // メンバで保持している詳細情報の削除
			var place = 0;
			for (place = 0; place < self.event.eventidlist.length; place++) {
				if (self.event.eventidlist[place] == eventkey) {
					self.event.eventidlist.splice(place, 1);
					break;
				}
			}
		}
	}

	var totalarr = {};
	if (self.event.eventidlist == undefined) self.event.eventidlist = [];
	for (var i = 0; i < eventflaginfo.length; i++) {
		if (eventflaginfo[i].monitorid != self.monitorid || eventflaginfo[i].parentdisplayname != self.displayname) {
			// monitoridとdisplaynameが異なる場合はループの先頭に戻る
			continue;
		}
		var lineid = eventflaginfo[i].facilityid + "_" + eventflaginfo[i].managername;
		if (getGraphConfig("data-stack-flg")) {
			lineid = eventflaginfo[i].managername + "_" + eventflaginfo[i].facilityid;
		}
		var eventdetailid = eventflaginfo[i].eventdetailid;
		if ((self.lineids.indexOf(lineid) < 0) 
				|| (Number(self.startdate) > Number(eventflaginfo[i].date) || Number(self.enddate) < Number(eventflaginfo[i].date)) 
				|| (self.event.eventidlist.indexOf(eventdetailid) >= 0)) {
			// 描画しているファシリティIDと異なる場合
			// イベントの日時が範囲外の場合
			// すでにリストに含まれている場合
			// ループの先頭に戻る
			continue;
		}

		self.event.eventidlist.push(eventdetailid);
		var startline = {};
		startline.x = Number(eventflaginfo[i].predictgenerationdate);
		startline.y = Number(0);
		startline.outputdate = Number(eventflaginfo[i].outputdate);
		startline.lineid = lineid;
		startline.facilityid = eventflaginfo[i].facilityid;
		startline.realfacilityid = eventflaginfo[i].realfacilityid;
		startline.facilityname = eventflaginfo[i].facilityname;
		startline.realmanagername = eventflaginfo[i].realmanagername;
		startline.managername = eventflaginfo[i].managername;
		startline.priority = eventflaginfo[i].priority;
		startline.message = eventflaginfo[i].message;
		startline.monitorid = eventflaginfo[i].monitorid;
		startline.monitordetailid = eventflaginfo[i].displayname;// monitordetailidのこと
		startline.eventdetailid = eventflaginfo[i].eventdetailid;
		startline.pluginid = eventflaginfo[i].pluginid;
		var endline = {};
		endline.x = Number(eventflaginfo[i].predictgenerationdate);
		endline.y = Number(self.height);
		endline.lineid = lineid;

		var arr = [];
		arr.push(startline);
		arr.push(endline);
		
		totalarr[eventdetailid] = arr;
	}
	
	if (Object.keys(totalarr).length === 0) {
		// totalarrが空なら処理終わり
		return;
	}
	
	if (self.event.object == null) {
		self.event.object = [];
	}
	for (key in totalarr) {
		self.event.object[key] = totalarr[key];
	}

	// グラフの描画(pathタグの追加、データは別途追加)
	self.vis.selectAll('.eventflagline')
	.data(self.event.eventidlist)
	.enter()
	.append("path")
	.attr("class", "eventflagline")
	.attr("id", function(d) { return "eventflagline" + d;})
	.style("stroke-width", HINEMOS_COLLECT_CONST.CONST_EVENTFLG_WIDTH_MOUSEOVER)
	.attr("clip-path", "url(#clip)")
	.attr('stroke', function(d){
		return self.colorlist[self.event.object[d][0].lineid];
	})
		// tooltipの設定
	.on("mouseover", function(d) { // イベント線のmouseoverイベント
		var parseDate = d3.time.format('%Y/%m/%d %H:%M:%S');
		var selfcolor =  self.colorlist[self.event.object[d][0].lineid];
		var str = "<dl>";
		str += "<dt style='border-bottom-color :" + selfcolor + ";border-left-color :" + selfcolor + ";'>" + self.event.object[d][0].facilityname + "(" + self.event.object[d][0].realmanagername + ")</dt>";
		str += "<dd>" + getGraphMessages("mess-time") + "：" + parseDate(new Date(Number(self.event.object[d][0].x))) + "</dd>";
		str += "<dd>" + getGraphMessages("mess-priority") + "：" + self.event.object[d][0].priority + "</dd>";
		str += "<dd>" + getGraphMessages("mess-message") + "：" + self.event.object[d][0].message + "</dd>";
		str += "</dl>";

		d3.select("body").select(".tooltip_mouse").style("visibility", "visible").style("width", "400px").html(str);
	})
	.on("mousemove", function(d) { // イベント線のmousemoveイベント
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
	.on("mouseout", function(d) { // イベント線のmouseoutイベント
		disableTooltip();
	})
	.on("mousedown", function() { d3.event.stopPropagation();})
	.on("mouseup", function() { d3.event.stopPropagation();})
	.on("click", function(d) {
		// イベント詳細画面を開く
		var param = {};
		var targetobj = self.event.object[d][0];// 0番目だけを見る、1番目は日付とidのみしか入っていない
		param.method_name = "open_event_detail";
		param.managername = targetobj.realmanagername;
		param.monitorid = targetobj.monitorid;
		param.monitordetailid = targetobj.monitordetailid;
		param.datelong = targetobj.outputdate;
		param.pluginid = targetobj.pluginid;
		param.facilityid = targetobj.realfacilityid;
		callJavaMethod(param);
	});

	// 線別スタイルの設定
	self.vis.selectAll("path#eventflagline")
	.style("stroke-dasharray", "5, 0");
};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// 近似直線の予測先を描画します。
NewGraph.prototype.createPredictionTarget = function() {

	if (!getGraphConfig("data-approx-flg")) {
		// 近似表示なしの場合は何もしない
		return;
	}
	var self = this;

	// 線情報
	if (self.prediction == undefined) return;// フラグなしの場合は処理なしで戻る
	
	var starlist = [];
	for (var lineid in self.prediction) {
		var obj = self.prediction[lineid];
		var point = self.points2[lineid];
		if (point.length <= 0) {
			continue;
		}
		var forinfo = self.forecastslist[lineid];
		var facilityname = point[0].facilityname;
		var managername = point[0].realmanagername;
		var date = obj.now + obj.min*60*1000;
		var star = {};
		star.facilityname = facilityname;
		star.managername = managername;
		star.x = date;
		var xx = date - (forinfo.dateoffset);
		star.y = forinfo.a0 + forinfo.a1 * Math.pow(xx, 1) + forinfo.a2 * Math.pow(xx, 2) + forinfo.a3 * Math.pow(xx, 3);
		star.lineid = lineid;
		starlist.push(star);
	
	}
	if (starlist.length <= 0) {
		return;
	}
	self.starlist = null;
	self.starlist = starlist;
	
	if (Number(self.startdate) > starlist[0].x 
		|| Number(self.enddate) < starlist[0].x) {
		// 目印が描画範囲外の場合は何もしない
		return;
	}

	self.vis.selectAll(".predictiontarget")
	.data(self.starlist)
	.enter()
	.append("g")
	.attr("class", "dddots")
	.attr("id", function(d, i) {
		return "predictiontarget_" + d.lineid;
	})
	.attr("clip-path", "url(#clip)")
	.append('circle')
	.attr('id', function(d) {
		return 'predictiontarget' + d.lineid;
	})
	.attr('class', function(d) {
		return 'predictiontarget';
	})
	.attr("r", function(d) {
		return 3;
	})
	.attr('fill', function(d, i){
		return self.colorlist[d.lineid];
	})
	.attr("transform", function(d) { 
		return "translate(" + self.x(d.x) + "," + self.y(d.y) + ")"; 
	})
	// tooltipの設定
	.on("mouseover", function(d) { // 点のmouseoverイベント
		var parseDate = d3.time.format('%Y/%m/%d %H:%M:%S');
		var predictionstr = getGraphMessages("mess-prediction");
		var minstr = predictionstr.replace("@@", self.prediction[d.lineid].str);
		var str = "<dl>";
		str += "<dt style='border-bottom-style:dashed;word-wrap: break-word;'>" + d.managername + " : <" + parseDate(new Date(d.x)) + "> *"  + minstr  + "*</dt>";
		str += "<dd style='border-bottom-color :" + self.colorlist[d.lineid] + ";border-left-color :" + self.colorlist[d.lineid] 
		+ ";border-bottom-style:dashed;word-wrap: break-word;'>" + d.facilityname + " : " + d.y + "</dd>";
		str += "</dl>";
		d3.select("body").select(".tooltip_mouse").style("visibility", "visible").style("width", "350px").html(str);
	})
	.on("mousemove", function(d) { // 点のmousemoveイベント
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
	.on("mouseout", function(d) { // 点のmouseoutイベント
		disableTooltip();
	});
	return;
};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// グラフの左に表示されている監視項目名に単位を追加します
// 線グラフ、積み上げグラフ共有関数です
NewGraph.prototype.addMeasure = function(self, measure) {
	if (measure == "") {
		// 単位が存在しない場合は追加しない(マネージャ複数で対象マネージャに表示するmonitorIdの情報が存在しない場合など)
		return;
	}
	var monitortitle = self.vis.select("#title_left").text();
	if (monitortitle.slice(-1) != "]") {
		// 単位は後から追加する、未追加の場合は追加
		var newtitle = monitortitle + "[" + measure + "]";
		self.vis.select("#title_left text")
		.text(newtitle);
	}
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// collectIdが取れなかったノードがある場合に、グラフの上に表示されているタイトルを変更します
// 線グラフ、積み上げグラフ共有関数です
NewGraph.prototype.getCollectIdNone = function(self, datacount) {
	var nodestr = getGraphMessages("mess-nodes");
	var totalstr = "(" + getGraphMessages("mess-total");
	if (!getGraphConfig("data-total-flg")) {
		if (datacount == 0) {
			// 単体表示でcollectidが取れなかった場合は、背景をグレーにする(tickと同じ色)
			dispNoData(self);
			// 閾値表示している場合は、閾値バーの透過度などを見えなくする
			ThresholdGraph.prototype.setThresholdOpacityZero(self.innerid);
		} else {
			// 背景はなし(線あり)
			self.vis.selectAll(".graph_background_block").style("fill", "none");
		}
	} else {
		var graphsize = self.vis.select("#title_top text").text();
		// タイトルが書き換わっているかどうか、書き換わっているなら初回表示ではないのでタイトルを変更しない
		if (graphsize.indexOf(nodestr) == -1) {
			if (datacount != Number(graphsize)) {
				// 背景はなし(線あり)
				self.vis.selectAll(".graph_background_block").style("fill", "none");
				// まとめて表示、かつ、collectidが取れなかったものがある場合はタイトルを変更する
				if (graphsize.indexOf(nodestr) == -1) {
					// 未書き換えの場合のみ
					var newtitle = datacount + nodestr + totalstr + Number(graphsize) + nodestr + ")";
					self.vis.select("#title_top text")
					.text(newtitle);
					// グラフタイトルをメンバに抑える
					self.title = newtitle;
				}
				if (datacount == 0) {
					// データ取得数が0の場合場合は背景をグレーにする
					dispNoData(self);
					// 閾値表示している場合は、閾値バーの透過度などを見えなくする
					ThresholdGraph.prototype.setThresholdOpacityZero(self.innerid);
				}
			} else {
				// まとめて表示、かつ、collectidが取れなかったものが無い場合
				var graphsize = self.vis.select("#title_top").text();
				var newtitle = Number(graphsize) + nodestr;
				self.vis.select("#title_top text")
				.text(newtitle);
				// グラフタイトルをメンバに抑える
				self.title = newtitle;
			}
		}
	}
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
//n近似線を作成します
//normallineid : おおもとのlineid
NewGraph.prototype.approxPoints = function(facilityid, normallineid, startdate, enddate) {
	var managername = "";
	var realmanagername = "";
	var facilityname = "";
	var realfacilityid = "";
	var self = this;
	
	if (self.points2[normallineid].length <= 0) {
		return;
	}
	
	var newapp = self.points2[normallineid].filter(function(v, i) {
		return (v.apponly != true);
	});
	
	self.points2[normallineid] = newapp;
	
	var forinfo = self.forecastslist[normallineid];
	
	managername = self.points2[normallineid][0].managername;
	realmanagername = self.points2[normallineid][0].realmanagername;
	facilityname = self.points2[normallineid][0].facilityname;
	realfacilityid = self.points2[normallineid][0].realfacilityid;

	// プロット情報の最新時間よりもグラフの表示領域最新時間のほうが未来の場合は、未来時刻に近似線を延ばす
	// 表示領域最新時間の座標を求める
	// サマリタイプ別にプロットの間隔を決める(rawは1m、hourは1h、dayは1day、monthは1month)
	var add_term = 60*1000; // 1minute
	switch (self.summarytype) {
		case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_AVG_HOUR:
		case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_MIN_HOUR:
		case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_MAX_HOUR:
			add_term = msec_hour;
		break;
		case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_AVG_DAY:
		case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_MIN_DAY:
		case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_MAX_DAY:
			add_term = msec_day;
		break;
		case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_AVG_MONTH:
		case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_MIN_MONTH:
		case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_MAX_MONTH:
			add_term = msec_month;
		break;
		default:
		break;
	}
	var disp_min = self.y.domain()[0];
	var disp_max = self.y.domain()[1];
	var disp_diff = disp_max - disp_min;
	disp_max = disp_max + disp_diff * 100;
	disp_min = disp_min - disp_diff * 100;

	var apparr = [];
	for (var term = Number(startdate); term <= (Number(enddate) + add_term); term+=add_term) {
		var newpoint = {};
		newpoint.x = term;
		var xx = newpoint.x - (forinfo.dateoffset);
		var app = forinfo.a0 + forinfo.a1 * Math.pow(xx, 1) + forinfo.a2 * Math.pow(xx, 2) + forinfo.a3 * Math.pow(xx, 3);
		
		if (app < disp_min || disp_max < app) {
			continue;
		}
		newpoint.app = app;
		newpoint.facilityid = facilityid;
		newpoint.realfacilityid = realfacilityid;
		newpoint.facilityname = facilityname;
		newpoint.managername = managername;
		newpoint.realmanagername = realmanagername;
		newpoint.apponly = true;
		apparr.push(newpoint);
	}
	
	
	// ソート
	apparr.sort(function(a, b) {
		if (a.x < b.x) { return -1 };
		if (a.x > b.x) { return 1 };
		return 0
	});
	// 重複データの除去
	var newlinepoints = apparr.filter(function (x, i, self) {
		var count = 0;
		for (count = 0; count < self.length; count++) {
			if (self[count].x == x.x) {
				break;
			}
		}
		return count == i;
	});
	
	for (var k = 0; k < apparr.length; k++) {
		self.points2[normallineid].push(apparr[k]);
	}
	
	return;
};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
//すべてのグラフベースの線を消します
// start_date:表示範囲の最古日時、end_date:表示範囲の最新時刻
NewGraph.prototype.removePoints = function(start_date, end_date, alldel) {
	try {
		for (var keyValue in graph) {
			var self = graph[keyValue];
			self.startdate = start_date;
			self.enddate = end_date;
			for (var keyline in self.points2) {
				var howmany_back = 0;
				var howmany_front = 0;
				// 後半の削除
				if (alldel) {
					// 全消しフラグがtrueの場合は、全部消す
					howmany_back = self.points2[keyline].length;
				} else {
				
				
					var newapp = self.points2[keyline].filter(function(v, i) {
						return (v.apponly != true);
					});

					self.points2[keyline] = newapp;
/*
					self.points2[keyline].some(function(v, i){
						if (typeof(v.apponly) != "undefined") {
							self.points2[keyline].splice(i,1);
						}
					});
*/				
/*				
					// 近似用のオブジェクトを削除
					var appcount = 0;
					for (var k = 0; k < self.points2[keyline].length; k++) {
						if (typeof(self.points2[keyline][k].apponly) != "undefined") {
							appcount++;
						}
						delete self.points2[keyline][k].app;// 近似直線の情報を削除する
					}
					self.points2[keyline].splice(self.points2[keyline].length-appcount, appcount);
*/

					// 指定日時外のオブジェクトを削除
					for (j = self.points2[keyline].length-1; j >= 0; j--) {
						delete self.points2[keyline][j].app;// 近似直線の情報を削除する
						if (self.points2[keyline][j].x >= Number(end_date)) {
							howmany_back++;
						}
					}
					// グラフに余白を作らないように、消す数の調整
					if (howmany_back != self.points2[keyline].length && howmany_back > 0) {
						howmany_back--;
					}
				}
				// plotの数が変わる、再度ループしなおしが良さげ
				// どこから・どのぐらい
				var sum = self.points2[keyline].splice(self.points2[keyline].length-howmany_back, howmany_back);

				// 前半の削除
				for (j = 0; j < self.points2[keyline].length; j++) {
					if (self.points2[keyline][j].x <= Number(start_date)) {
						howmany_front++;
					}
				}
				// グラフに余白を作らないように、消す数の調整
				if (howmany_front != self.points2[keyline].length && howmany_front > 0) {
					howmany_front--;
				}
				// 前半の削除
				sum = self.points2[keyline].splice(0, howmany_front);
			}
			
			for (var i = 0; i < self.lineids.length; i++) {
				// プロットも消す
				self.points.selectAll("g#" + self.lineids[i] + " circle")
				.data(function(d, index) {
					var a = [];
					var normalid = d;
					if (!ControlGraph.isNormal(d)) {
						normalid = ControlGraph.getNormalLineId(normalid);
					}
					var dd = self.points2[normalid];
					dd.forEach(function(point, i){
						if (ControlGraph.isNormal(d) && typeof(point.y) != "undefined") {
							a.push({'index': index, 'point': point, 'lineid':d});
						} else if (ControlGraph.isApprox(d) && typeof(point.app) != "undefined") {
							a.push({'index': index, 'point': point, 'lineid':d});
						} else if (ControlGraph.isApproxTarget(d) && typeof(point.tar) != "undefined") {
							a.push({'index': index, 'point': point, 'lineid':d});
						} else if (ControlGraph.isAvg(d) && typeof(point.avg) != "undefined") {
							a.push({'index': index, 'point': point, 'lineid':d});
						} else if (ControlGraph.isSig(d) && typeof(point.sig) != "undefined") {
							a.push({'index': index, 'point': point, 'lineid':d});
						} else if (ControlGraph.isSim(d) && typeof(point.sim) != "undefined") {
							a.push({'index': index, 'point': point, 'lineid':d});
						}
					});
					// 残すプロットの情報を返す
					return a;
				})
				.exit()
				.remove();
			}
			
			// イベント線の削除処理(範囲外のものを削除する)
			self.removeEventFlag(self, start_date, end_date);
			self.removePredictionTarget(self, start_date, end_date);
			self.redraw();
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
NewGraph.prototype.removeEventFlag = function(self, start_date, end_date) {
	// イベント線の削除処理(範囲外のものを削除する)
	for (eventkey in self.event.object) {
		var eventobj = self.event.object[eventkey];
		if (eventobj[0].x < Number(start_date) || eventobj[0].x > Number(end_date)) {
			// イベント線が範囲外になったので削除する
			self.vis.select("#eventflagline" + eventkey).remove(); // pathタグの削除
			delete self.event.object[eventkey]; // メンバで保持している詳細情報の削除
			var place = 0;
			for (place = 0; place < self.event.eventidlist.length; place++) {
				if (self.event.eventidlist[place] == eventkey) {
					self.event.eventidlist.splice(place, 1);
					break;
				}
			}
		}
	}
};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
NewGraph.prototype.removePredictionTarget = function(self, start_date, end_date) {
	// イベント線の削除処理(範囲外のものを削除する)
	for (var i = 0; i < self.starlist.length; i++) {
		var preobj = self.starlist[i];
		// イベント線が範囲外になったので削除する
		self.vis.select("#predictiontarget" + preobj.lineid).remove(); // pathタグの削除
		delete self.prediction[i]; // メンバで保持している詳細情報の削除
	}
};
//////////////////////////////////////////////////////////////
// Method
//////////////////////////////////////////////////////////////

ControlGraph = {};

// chart_blockの子要素をすべて削除する
// また、保持しているグラフ情報も削除する
ControlGraph.delDiv = function () {
	try {
		var aNode = document.getElementById("chart_block");
		for (var i =aNode.childNodes.length-1; i>=0; i--) {
			aNode.removeChild(aNode.childNodes[i]);
		}
		graph = null;
		graph = [];
		thresholdgraph = null;
		thresholdgraph = [];
		
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
ControlGraph.addDiv = function (parentid, name) {
	try {
		var div_child = document.createElement('div');
		div_child.id = name;//total:monitorId, single:facilityId_managerName_monitorId
		div_child.classList.add("chart");
		var height = HINEMOS_COLLECT_CONST.CONST_GRAPH_HEIGHT;
		var legendflg = getGraphConfig("data-legend-flg");
		if (legendflg) height += HINEMOS_COLLECT_CONST.CONST_GRAPH_RIGHT_LEGEND_HEIGHT;
		div_child.style.width = HINEMOS_COLLECT_CONST.CONST_GRAPH_WIDTH + "px";
		div_child.style.height = height + "px";
		div_child.style.margin = HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN + "px";
		div_child.style.verticalAlign = "top";
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
ControlGraph.addGraphAtOnce = function (plotjson, graphSize, preferenceSize, settinginfo) {
	try {
		// 設定情報を解析して保持する
		setGraphConfig(settinginfo);
		// グラフ数カウント
		var graphcount = preferenceSize;
		// 各グラフの作成
		var length = plotjson.all.length;
		for (var i = 0; i < length; i++) {
			var item = plotjson.all[i];
			var itemname = item.itemname;
			var displayname = item.displayname;
			var monitorid = item.monitorid;
			var parentid = monitorid + itemname;
			var id = item.id;//total:monitorId, single:facilityId_managerName_monitorId_displayName
			ControlGraph.addDiv(parentid, id);
			graphcount = graphcount - graphSize;
			if (graphcount < 0) {
				graphSize = graphSize + graphcount;
			}
			graph[id] = new NewGraph(id, item, graphSize);
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
ControlGraph.addPlotAtOnce = function (plotjson) {
	try {
		// tooltipを消す
		disableTooltip();
		
		// 折れ線表示の場合、線の太さを通常線にします
		if (!getGraphConfig("data-stack-flg")) {
			var paths = document.getElementsByTagName("path");
			for (var i = 0; i < paths.length; i++) {
				var pathsingle = paths[i];
				if (pathsingle.id != "" && pathsingle.id.indexOf("eventflagline") == -1) {
					d3.select(pathsingle).style("stroke-width", HINEMOS_COLLECT_CONST.CONST_LINE_WIDTH_DEFAULT);
				}
			}
		}
		var length = plotjson.all.length;
		var data_arr = [];
		// データの整理
		for (var i = 0; i < length; i++) {
			var item = plotjson.all[i];
			var groupid = item.groupid;// total:monitorid, total:facilityid+managername+monitorid
			var id = item.facilityid + "_" + item.managername;// + item.collectid;
			if (data_arr[groupid] == null) {
				data_arr[groupid] = [];
			}
			data_arr[groupid][id] = item;
		}
		for (var groupkey in data_arr) {
			if (getGraphConfig("data-stack-flg")) {
				var ret = NewGraph.prototype.createStack(groupkey, data_arr[groupkey], plotjson.eventflaginfo);
				if (ret != undefined) {
					// エラーが発生したためループを抜けて処理終了
					// 積み上げ面のプロット数が全部一致していない場合
					var err_facility = (ret.facilitylist).slice(0, -2); // [, ]の除去
					if (err_facility.length > 500) { // 対象ファシリティの文字列が長い場合、短くして[...]を付与
						err_facility = err_facility.slice(0, 500) + "...";
					}
					// monitoridとfacilityidを通知する
					alert(getGraphMessages("mess-datainsufficient") + "\nmonitorId : [" + ret.monitorid + "]\nfacilityId : [" + err_facility + "]");
				}
			} else {
				NewGraph.prototype.createPoints(groupkey, data_arr[groupkey], plotjson.eventflaginfo);
			}
		}
		NewGraph.prototype.cleanItemName();
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
ControlGraph.trimBranch = function() {
	for (var key_value in graph) {
		// 最小・最大・平均・最新の算出
		ControlGraph.calcMinMaxAvg(graph[key_value]);
		if (getGraphConfig("data-legend-flg")) {
			// 凡例を作成する、グラフを動かすたびに更新する
			graph[key_value].createLegend();
		}
	}

	if (getGraphConfig("data-stack-flg")) {
		ControlGraph.trimYAxisStack();
	
	} else {
		ControlGraph.trimYAxis();
	}
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// itemName別にy軸を同じにします
ControlGraph.trimYAxis = function () {
try {
	var itemTrimList = [];
	// itemNameとmonitoridの取得
	for (var keyValue in graph) {
		var self = graph[keyValue];
		var itemname = self.itemname;
		var monitorid = self.monitorid;
		var itemflg = false;
		var itemmoni = itemname + monitorid;
		for (var i = 0; i < itemTrimList.length; i++) {
			if (itemmoni == itemTrimList[i]) {
				itemflg = true;
				break;
			}
		}
		if (!itemflg) {
			// リストに入っていないなら、入れる
			itemTrimList.push(itemmoni);
		}
	}
	// itemname+monitoridの配列が完成↑
	
	for (var j = 0; j < itemTrimList.length; j++) {
		var max_y = Number.MIN_VALUE/2;
		var min_y = Number.MAX_VALUE;
		var itemtrim = itemTrimList[j];
		var isgraphrange = false;
		for (var keyValue in graph) {
			var self = graph[keyValue];
			var itemname = self.itemname;
			var monitorid = self.monitorid;
			var itemmoni = itemname + monitorid;
			if (itemmoni == itemtrim) {
				if (self.graphrange) { // 100%表示対象の場合はmax_yは100固定
					isgraphrange = true;
				} else {
					if (self.graphrange == null) {
						continue; // 監視対象外(collectidが無い)の場合はnullになる
					}
					isgraphrange = false;
					for (var i in self.valueinfo) {
						var value = self.valueinfo[i];
						if (value.size == 0) {
							continue;
						}
						if (max_y < value.disp_max) {
							max_y = value.disp_max;
						}
						if (min_y > value.disp_min) {
							min_y = value.disp_min;
						}
					}
				}
			}
		}
		// サイズが確定↑
		
		// サイズを反映↓
		// 余白の設定
		if (isgraphrange) {
			// 100%固定表示の場合
			max_y = 110;
			min_y = -10;

		} else {
			if (max_y == Number.MIN_VALUE || min_y == Number.MAX_VALUE) {
				max_y = 10;
				min_y = 0;
			} else if (max_y == min_y) {
				min_y-=1;
				max_y+=1;
			}else {
				var term = (max_y -min_y) / 10;
				min_y = min_y - term;
				max_y = max_y + term;
			}
		}
		for (var keyValue in graph) {
			var self = graph[keyValue];
			var itemname = self.itemname;
			var monitorid = self.monitorid;
			var itemmoni = itemname + monitorid;
			if (itemmoni == itemtrim) {
				var y_domain = [min_y, max_y];
				self.y.domain(y_domain)
				.range([self.height, 0]);
			}
			self.redraw()();
			self.animationStack();
		}
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
// すべてのグラフの表示範囲(x軸範囲)を変更します
// ignoreidを指定した場合、そのignoreidのグラフの表示範囲は変更しません
// 積み上げ面グラフの場合もこのメソッドを使用する
ControlGraph.trimXAxis = function(xmin, xmax, ignoreid) {
	// idはgraphのkeyと同じ
	try {
		for (var keyValue in graph) {
			if (ignoreid != null && keyValue == ignoreid) {
				continue;
			}
			var self = graph[keyValue];
			
			var x_domain = [new Date(Number(xmin)), new Date(Number(xmax))];
			self.x.domain(x_domain);
			
			if (getGraphConfig("data-stack-flg")) {
				self.redrawStack()();
			} else {
				self.redraw()();
			}
		}
	} catch(e) {
		var param = {};
		param.method_name = "javascript_error";
		param.exception = e;
		callJavaMethod(param);
	}
};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
ControlGraph.calcMinMaxAvg = function(self) {

	var facilitylist;
	if (getGraphConfig("data-stack-flg")) {
		facilitylist = self.stackdata;
	} else {
		facilitylist = self.points2;
	}
	
	/////////
	// 内部関数
	/////////
	function getY(value, startdate, enddate, type) {
		if (getGraphConfig("data-stack-flg")) {
			if (startdate <= value.date && value.date <= enddate) {
				return value.y;
			}
		} else {
			if (startdate <= value.x && value.x <= enddate && value.apponly != true) {
				switch (type) {
					case "avg":
						return value.avg;
						break;
					case "sig":
						return value.sig;
						break;
					case "sim":
						return value.sim;
						break;
					default:
						return value.y;
						break;
				}
			}
		}
	}
		
	for (var linekey in facilitylist) {
		var size = 0;
		var last = null;
		var startdate = Number(self.startdate);
		var enddate = Number(self.enddate);
		var pointlist = facilitylist[linekey];

		if (getGraphConfig("data-stack-flg")) {
			pointlist = facilitylist[linekey].values;
		}
		
		// 収集値のmin、max、meanを求める
		var y_min = d3.min(pointlist, function (e) { return getY(e, startdate, enddate, "y");});
		var y_max = d3.max(pointlist, function (e) { return getY(e, startdate, enddate, "y");});
		var y_mean = d3.mean(pointlist, function (e) { return getY(e, startdate, enddate, "y");});
		
		// 平均のmin、maxを求める
		var avg_min = d3.min(pointlist, function (e) { return getY(e, startdate, enddate, "avg");});
		var avg_max = d3.max(pointlist, function (e) { return getY(e, startdate, enddate, "avg");});
		
		// 2pのmin、maxを求める
		var sig_min = d3.min(pointlist, function (e) { return getY(e, startdate, enddate, "sig");});
		var sig_max = d3.max(pointlist, function (e) { return getY(e, startdate, enddate, "sig");});
		
		// -2pのmin、maxを求める
		var sim_min = d3.min(pointlist, function (e) { return getY(e, startdate, enddate, "sim");});
		var sim_max = d3.max(pointlist, function (e) { return getY(e, startdate, enddate, "sim");});
		
		// TODO ここで将来予測の予測点を取得する
		var predict = undefined;
		var pred = self.prediction[linekey];
		if (typeof(pred) != "undefined") {
			var forinfo = self.forecastslist[linekey];
			var preddate = pred.now + pred.min*60*1000;
			
			if (startdate <= preddate && preddate <= enddate) {
				var xx = preddate - (forinfo.dateoffset);
				predict = forinfo.a0 + forinfo.a1 * Math.pow(xx, 1) + forinfo.a2 * Math.pow(xx, 2) + forinfo.a3 * Math.pow(xx, 3);
			}
		}
		
		// y軸のmin、maxを求める
		var disp_min = d3.min([y_min, avg_min, sig_min, sim_min, predict]);
		var disp_max = d3.max([y_max, avg_max, sig_max, sim_max, predict]);
		
		// 凡例表示用のlastの日時を取得
		var x_max = d3.max(pointlist, function (e) {
			if (startdate <= e.x && e.x <= enddate && typeof(e.y) != "undefined") {
				if (getGraphConfig("data-stack-flg")) {
					return e.date;
				}
				return e.x;
			}
		});
		for (var i = 0; i < pointlist.length; i++) {
			var obj = pointlist[i];
			var checkdate = obj.x;
			if (getGraphConfig("data-stack-flg")) {
				checkdate = obj.date;
			}
			if (startdate <= checkdate && checkdate <= enddate) {
				size++;
				if (checkdate == x_max) {
					last = obj.y;
				}
			}
		}
		if (self.valueinfo == null) {
			self.valueinfo = [];
		}
		if (getGraphConfig("data-stack-flg")) {
			linekey = facilitylist[linekey].name;
		}
		var ret = [];
		ret.min = y_min; // 凡例表示用の最小値
		ret.max = y_max; // 凡例表示用の最大値
		ret.disp_min = disp_min; // y軸調整用の最小値
		ret.disp_max = disp_max; // y軸調整用の最大値
		ret.avg = y_mean;
		ret.size = size;
		if (last != null) {
			ret.last = last;
		}
		ret.linekey = linekey;
		self.valueinfo[linekey] = ret;
//		console.log("min:" + ret.min + ", max:" + ret.max + ", last:" + last + ", avg:" + ret.avg + ", size:" + ret.size + ", linekey:" + linekey + ", dispmin:" + ret.disp_min + ", dispmax:" + ret.disp_max);
	} // end of loop
};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// xxxYYY => xxx
ControlGraph.getNormalLineId = function(lineId) {
	return lineId.slice(0, lineId.length-3);
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// xxxx => xxxxapp
ControlGraph.getApproxLineId = function(lineId) {
	return lineId + "app";
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
//xxxx => xxxxtar
ControlGraph.getApproxTargetLineId = function(lineId) {
return lineId + "tar";
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// xxxxapp => true
ControlGraph.isApprox = function(lineId) {
	if (lineId.slice(-3) == "app") {
		return true;
	}
	return false;
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
//xxxxapp => true
ControlGraph.isApproxTarget = function(lineId) {
if (lineId.slice(-3) == "tar") {
return true;
}
return false;
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// xxxxsig => true
ControlGraph.isSig = function(lineId) {
	if (lineId.slice(-3) == "sig") {
		return true;
	}
	return false;
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// xxxxsigm => true
ControlGraph.isSim = function(lineId) {
	if (lineId.slice(-3) == "sim") {
		return true;
	}
	return false;
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// xxxxavg => true
ControlGraph.isAvg = function(lineId) {
	if (lineId.slice(-3) == "avg") {
		return true;
	}
	return false;
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// xxxx => true
ControlGraph.isNormal = function(lineId) {
	if (ControlGraph.isApprox(lineId) || ControlGraph.isApproxTarget(lineId) || ControlGraph.isAvg(lineId) || ControlGraph.isSig(lineId) || ControlGraph.isSim(lineId)) {
		return false;
	}
	return true;
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
//指定されたidのグラフにプロットを追加します
//プロット情報は[x軸, y軸]の配列である必要があります
NewGraph.prototype.createStack = function(groupkey, data_arr, eventflaginfo) {
	try {
		var self = graph[groupkey];
		
		var datacount = 0;
		for (line_key in data_arr) {
			var item = data_arr[line_key];
			self.startdate = item.startdate;
			self.enddate = item.enddate;
			self.summarytype = item.summarytype;
			var facilityid = item.facilityid;
			var facilityname = item.facilityname;
			var managerName = item.managername;
			var itemname = item.itemname;
			var collectid = item.collectid;
			if (collectid != "none") {
				// collectidが取れなかった場合は、"none"が入ってくる
				datacount++;
			}
			// 単位を追加する
			var measure = item.measure;
			self.addMeasure(self, measure);

			var targetobj;
			if (self.stackdata == null) {
				self.stackdata = [];
			}
			
			targetobj = new Object();
			targetobj.facilityid = facilityid;
			targetobj.realfacilityid = item.realfacilityid;
			targetobj.facilityname = facilityname;
			targetobj.managername = managerName;
			targetobj.realmanagername = item.realmanagername;
			targetobj.name = managerName + "_" + facilityid;
			targetobj.itemname = itemname;
			targetobj.values = [];
			for (var i = 0; i < self.stackdata.length; i++) {
				var stackdata = self.stackdata[i];
				if (stackdata.facilityid == facilityid && stackdata.managername == managerName && stackdata.itemname == itemname) {
					// いったん外に出して後で入れなおす
					targetobj = self.stackdata.splice(i, 1)[0];
					break;
				}
			}
			
			// 通常グラフ描画用のソート
			for (j = 0; j < item.data.length; j++) {
				var singledata = item.data[j];
				if (isReallyNaN(Number(singledata[1]))) {
					// NaNだった場合はデータを使用しないためループの先頭に戻る
					continue;
				}
				var newpoint = {};
				newpoint.date = Number(singledata[0]);
				newpoint.y = Number(singledata[1]);
				targetobj.values.push(newpoint);
			}
			// ソート
			targetobj.values.sort(function(a, b) {
				if (a.date < b.date) { return -1 };
				if (a.date > b.date) { return 1 };
				return 0
			});
			// 重複データの除去
			var newlinepoints = targetobj.values.filter(function (x, i, self) {
				var count = 0;
				for (count = 0; count < self.length; count++) {
					if (self[count].date == x.date) {
						break;
					}
				}
				return count == i;
			});

			// 重複データ除去後のデータを正規データとする
			targetobj.values = newlinepoints;

			// プロット情報が無い場合はpushしない
			if (targetobj.values != null && targetobj.values.length != 0) {
				self.stackdata.push(targetobj);
			}
		}// データの整理が終了
		
		// 収集値の数がそろっているかのチェック
		var datechecklist = [];
		// エラーチェック用に日付リストを作成する
		for (var cou = 0; cou < self.stackdata.length; cou++) {
			for (var cou_va = 0; cou_va < self.stackdata[cou].values.length; cou_va++) {
				var targetobj = self.stackdata[cou].values[cou_va];
				if (datechecklist.indexOf(targetobj.date) < 0) {
					datechecklist.push(targetobj.date);
				}
			}
		}
		
		var err_monitorid = self.monitorid;
		var err_facilitylist = "";
		for (var key in Object.keys(self.stackdata)) {
			var obj = self.stackdata[key];
			for (var c = 0; c < datechecklist.length; c++) {
				var targetdate = datechecklist[c];
				var check = false;
				var targetfaci = "";
				for (var cc = 0; cc < obj.values.length; cc++) {
					var targetobj = obj.values[cc];
					targetfaci = obj.realfacilityid;
					if (targetdate == targetobj.date) {
						check = true;
						break;
					}
				}
				if (!check) {
					err_facilitylist += targetfaci + ", ";
					break;
				}
			}
		}

		// 収集値の数が合わないため、エラーダイアログを出して終了にする
		if (err_facilitylist != "") {
			// タイトルを更新する
			NewGraph.prototype.getCollectIdNone(self, datacount);
			
			// 積み上げ面グラフが描画できていないフラグを立てる
			self.is_disp_success = false;
			
			var err_obj = {};
			err_obj.monitorid = err_monitorid;
			err_obj.facilitylist = err_facilitylist;
			return err_obj;
		} else {
			self.is_disp_success = true;
		}
		
		// スタックの計算
		var data = self.stack(self.stackdata);

		// mouseoverの線よりも下に描画したいため、線よりも表示を下になるように用意したnode_groupに追加する
		this.node = self.vis.select("#node_group").selectAll(".node")
				.data(data)
				.enter().append("g")
				.attr("class", function(d, i) {
					return "node";
				 })
				.attr("id", function(d, i) {
					var classname = d.managername + "_" + d.facilityid;
					self.lineids[i] = classname;
					return classname;
				 });

		// エリアの描画
		this.node
			.append("path")
			.attr("class",function(d, i) {
				return "area";
			})
			.style("fill", function(d) { 
				self.colorlist[d.name] = self.colors(d.name);
				return self.colorlist[d.name]; })
			.style("opacity", HINEMOS_COLLECT_CONST.CONST_STACKED_COLOR_OPACITY)
			.attr("clip-path", "url(#clip)");

		// 最小・最大・平均・最新の算出
		ControlGraph.calcMinMaxAvg(self);

		// 初回のみ、かつ、凡例表示する場合
		if (self.vis.selectAll(".legend").empty() && getGraphConfig("data-legend-flg")) {
			// 凡例を作成する
			self.createLegend();
		}

		// collectIdが存在しない件数をタイトルに入れる
		NewGraph.prototype.getCollectIdNone(self, datacount);

		// イベントフラグの線を描画
		self.createEventFlag(eventflaginfo);

		self.updateStack();
	} catch(e) {
		var param = {};
		param.method_name = "javascript_error";
		param.exception = e;
		callJavaMethod(param);
	}
};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
NewGraph.prototype.updateStack = function() {
	var self = this;

	for (i = 0; i < this.lineids.length; i++) {
		var pathdata = this.vis.selectAll("#"+this.lineids[i]).select("path");
		pathdata.attr("d", function(d) {
			var areadata = self.area(d.values);
			return self.area(d.values);
		});
	}

	// イベントフラグの線の再描画
	if (!this.vis.select(".eventflagline").empty()) {
		for (i = 0; this.event.eventidlist != null && i < this.event.eventidlist.length; i++) {
		var id = this.event.eventidlist[i];
			this.vis.select("#eventflagline" + id).attr('d', this.eventline(self.event.object[id]));
		}
	}

	// 表示の更新後にスタイルを指定する
	self.vis.selectAll(".axis line").style("stroke", HINEMOS_COLLECT_CONST.CONST_COLOR_GRAY);
	self.vis.selectAll(".axis text")
	.style("font-size", "8pt");
	
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
NewGraph.prototype.animationStack = function() {
	var self = this;
	self.vis.select("#clip rect").attr("width", 0);

	self.vis.select("#clip rect")
	.transition().duration(HINEMOS_COLLECT_CONST.CONST_ANIMATION_INTERVAL)
	.attr("width", self.width);


}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
NewGraph.prototype.redrawStack = function() {
	var self = this;
	return function() {
		self.vis.select(".x.axis").call(self.xAxis);
		self.vis.select(".y.axis").call(self.yAxis);

		self.plot.call(d3.behavior.zoom().x(self.x).y(self.y).on("zoom", function() { self.redrawStack()();throttle(self.upup, self);}));
		self.vis.call(d3.behavior.zoom().x(self.x).y(self.y).on("zoom", function() { self.redrawStack()();throttle(self.upup, self);}));

		self.updateStack();
	}
}

//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// 積み上げ面グラフのすべてのグラフベースの線を消します
NewGraph.prototype.removeStack = function(start_date, end_date) {
	try {
		for (var keyValue in graph) {
			var self = graph[keyValue];
			var target = self.stackdata;
			self.startdate = start_date;
			self.enddate = end_date;
			
			// 消す範囲の調整
			// 実際指定された表示範囲よりも多めに残す
			var del_term = msec_hour;
			switch (self.summarytype) {
				case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_AVG_HOUR:
				case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_MIN_HOUR:
				case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_MAX_HOUR:
					del_term = msec_hour;
				break;
				case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_AVG_DAY:
				case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_MIN_DAY:
				case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_MAX_DAY:
					del_term = msec_day;
				break;
				case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_AVG_MONTH:
				case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_MIN_MONTH:
				case HINEMOS_COLLECT_CONST.CONST_SUMMARY_TYPE_MAX_MONTH:
					del_term = msec_month;
				break;
				default:
				break;
			}
			
			var remove_startdate = start_date - del_term;
			var remove_enddate = end_date + del_term;
			for (var i = 0; i < target.length; i++) {
				var howmany_back = 0;
				var howmany_front = 0;
				var length = target[i].values.length;
				// 後半の削除
				for (j = length-1; j >= 0; j--) {
					if (target[i].values[j].date >= Number(remove_enddate)) {
						howmany_back++;
					}
				}
				// plotの数が変わる、再度ループしなおしが良さげ
				// どこから・どのぐらい
				var sum = target[i].values.splice(length-howmany_back, howmany_back);
				// 前半の削除
				length = target[i].values.length;
				for (j = 0; j < length; j++) {
					if (target[i].values[j].date <= Number(remove_startdate)) {
						howmany_front++;
					}
				}
				sum = target[i].values.splice(0, howmany_front);
			}
			
			// イベント線の削除処理(範囲外のものを削除する)
			self.removeEventFlag(self, start_date, end_date);
			self.redrawStack();
		}
	} catch (e) {
		var param = {};
		param.method_name = "javascript_error";
		param.exception = e;
		callJavaMethod(param);
	}
};// end of "removeStack"
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
//itemName別にy軸を同じにします
ControlGraph.trimYAxisStack = function () {
	try {
		var itemTrimList = [];
		// itemNameの取得
		for (var keyValue in graph) {
			var self = graph[keyValue];
			var itemname = self.itemname;
			var monitorid = self.monitorid;
			var itemmoni = itemname + monitorid;
			var itemflg = false;
			for (var i = 0; i < itemTrimList.length; i++) {
				if (itemmoni == itemTrimList[i]) {
					itemflg = true;
					break;
				}
			}
			if (itemflg) {
				continue;
			}
			itemTrimList.push(itemmoni);
		}
		// itemnameの配列が完成↑
		for (var j = 0; j < itemTrimList.length; j++) {
			var maxarr = [];
			var max_y = Number.MIN_VALUE;
			var min_y = Number.MAX_VALUE;
			var itemtrim = itemTrimList[j];
			for (var keyValue in graph) {
				var self = graph[keyValue];
				var itemname = self.itemname;
				var monitorid = self.monitorid;
				var itemmoni = itemname + monitorid;
				if (itemmoni == itemtrim) {
					for (var k = 0; k < self.stackdata.length; k++) {
						var obj = self.stackdata[k];
						for (var l = 0; l < obj.values.length; l++) {
							var valuesingle = obj.values[l];
							if (maxarr[valuesingle.date] == null) {
								maxarr[valuesingle.date] = 0;
							}
							maxarr[valuesingle.date] += valuesingle.y;
						}
					}
				}
			}
			for (key in maxarr) {
				var targetvalue = maxarr[key];
				if (min_y > targetvalue) {
					min_y = targetvalue;
				}
				if (max_y < targetvalue) {
					max_y = targetvalue;
				}
			}
			// サイズが確定↑
			
			// サイズを反映↓
			// 余白の設定
			if (max_y == Number.MIN_VALUE || min_y == Number.MAX_VALUE) {
				max_y = 10;
				min_y = 0;
			} else {
				min_y = min_y * 0.95;
				max_y = max_y * 1.05;
			}
			for (var keyValue in graph) {
				var self = graph[keyValue];
				var itemname = self.itemname;
				var monitorid = self.monitorid;
				var itemmoni = itemname + monitorid;
				if (itemmoni == itemtrim) {
					var y_domain = [0, max_y];
					self.y.domain(y_domain)
					.range([self.height, 0]);
				}
				self.redrawStack()();
				self.animationStack();
			}
		}
	} catch (e) {
		var param = {};
		param.method_name = "javascript_error";
		param.exception = e;
		callJavaMethod(param);
	}
};

