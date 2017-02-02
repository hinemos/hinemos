/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

性能[グラフ]の散布図を描画するJavaScriptです。
 */
Scattergraph = function(elementid, options, graphsize) {
var self = this;
this.chart = document.getElementById(elementid);
this.innerid = elementid;
this.graphtype = HINEMOS_COLLECT_CONST.CONST_SCATTERGRAPH;// グラフの種類別に分けること、capで使用


//this.options = options || {};
this.startdate = options.startdate;
this.enddate = options.enddate;
this.colors = d3.scale.category10();

this.baseheight = Number((self.chart.style.height).replace(/px/g, "")) - HINEMOS_COLLECT_CONST.CONST_GRAPH_RIGHT_LEGEND_HEIGHT;

this.lineids = [];
this.points2 = [];// 点の情報
this.lines = [];// 近似線の情報
this.points = null;// プロットの情報(g-tag)
this.node = null;// 積み上げ情報(g-tag)
this.colorlist = [];

// サブ情報
this.itemname = options.itemname;
this.monitorid = options.monitorid;
this.displayname = options.displayname;

this.graphrange_x = null; // master
this.graphrange_y = null; // sub

this.ylabel = "";//cap用
this.xlabel = "";
this.title = "";
this.facilityid = null;// 単品の場合、titleにファシリティ名が入っているためファシリティIDを保持(重複防止)
this.managername = null;

// マスター情報
this.masteritemname = options.masteritemname;
this.mastermonitorid = options.mastermonitorid;
this.displayname = options.masterdisplayname;

// 回帰用
this.slope = [];
this.intercept = [];
this.R2 = [];
this.approxarr = [];// プロット情報

//////////////////////////////////////////////////////////////
// Create Margins and Axis and hook our zoom function
//////////////////////////////////////////////////////////////
this.margin = {top: HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_TOP, right: HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_RIGHT, bottom: HINEMOS_COLLECT_CONST.CONST_SCATTERGRAPH_MARGIN_BOTTOM, left: HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_LEFT};
this.width = HINEMOS_COLLECT_CONST.CONST_SCATTERGRAPH_WIDTH - HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_LEFT - HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_RIGHT;
this.height = HINEMOS_COLLECT_CONST.CONST_SCATTERGRAPH_HEIGHT - HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_TOP - HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_BOTTOM;

this.x = d3.scale.linear()
		.domain([0, 100])
		.range([0, this.width]);

this.y = d3.scale.linear()
		.domain([0, 100])
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
	.tickFormat(function(d, i) {
		if ((d + "").length > 7) {
			// 指数表記、小数点2桁
			d = d.toExponential(2);
		}
		return d;
	})
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
var zoommethod = this.redraw();
if (getGraphConfig("data-stack-flg")) {
	zoommethod = this.redrawStack();
}
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


// グラフ1つ1つを囲むグレーの枠線
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
	.attr("height", this.height)
	.attr("pointer-events", "all");

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
	.style("fill",HINEMOS_COLLECT_CONST.CONST_COLOR_WHITE)
	.attr("y", y_posi)
	.attr("x", -30)
	.text(truncateText(title, 60));


// グラフのitemName(左)表示
this.vis.append("g")
	.attr("class", "y axis axis_ylabel")
	.attr("id", "title_left")
	.append("text")
	.attr("transform","rotate(-90)")
	.attr("y", (-this.margin.left) + 14)
	.attr("x", -graphheight/2)
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


// グラフのitemName(下)表示
this.vis.append("g")
	.attr("class", "y axis axis_xlabel")
	.attr("id", "title_bottom")
	.append("text")
	.style("text-anchor","middle")
	.attr("y", this.height + 28)
	.attr("x", this.width/2)
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
		var tt = self.xlabel;
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

this.line = d3.svg.line()
	.interpolate("linear")
	.x(function(d, i) {
		return this.x(this.approxarr[d.key][i].x);
	})
	.y(function(d, i) {
		return this.y(this.approxarr[d.key][i].y);
	});

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

};// end of Scattergraph

//////////////////////////////////////////////////////////////
// Method
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
Scattergraph.prototype.update = function() {
	var self = this;

	// 近似線の描画
	if (getGraphConfig("data-approx-flg")) {
		for (i = 0; i < this.lineids.length; i++) {
			var id = this.lineids[i];
			if (self.slope[id] == null) continue;
			var target = this.vis.select('#' + id);
			target.attr('d', this.line(this.approxarr[id]));
		}
	}

	this.points.selectAll('circle').attr("transform", function(d) {
		return "translate(" + self.x(d.point.valuex) + "," + self.y(d.point.valuey) + ")"; }
	);
	
	// 表示の更新後にスタイルを指定する
	self.vis.selectAll(".axis line").style("stroke", HINEMOS_COLLECT_CONST.CONST_COLOR_GRAY);
	self.vis.selectAll(".axis text")
	.style("font-size", "8pt");

}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
Scattergraph.prototype.redraw = function() {
	var self = this;
	return function() {
		self.vis.select(".x.axis").call(self.xAxis);
		self.vis.select(".y.axis").call(self.yAxis);

		self.plot.call(d3.behavior.zoom().x(self.x).y(self.y).on("zoom", self.redraw()))
		
		self.update();
		
	}
}

//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
//指定されたidのグラフにプロットを追加します
//プロット情報は[x軸, y軸]の配列である必要があります
// x軸が第1選択、y軸がそれ以降に選択したもの
Scattergraph.prototype.createPoints = function(data_arr, itemorder) {
	try {
		var nodatacount = 0;// 集約時は初期化は最初だけ、単体時はfacilityごとに初期化する
		var item_left = "";
		var item_bottom = "";
		var itemmasterobj = itemorder.splice(0, 1)[0];
		var itemmaster = itemmasterobj.itemname + "(" + itemmasterobj.monitorid + ")";
		var mastermonitorid = itemmasterobj.itemname + itemmasterobj.monitorid;
		
		var itemdataarr = [];
		for (var k = 0; k < itemorder.length; k++) {
			nodatacount = 0;// totalの場合用初期化
			var facidataarr = [];
			var submonitorobj = itemorder[k];
			var submonitorid = submonitorobj.itemname + submonitorobj.monitorid;
			for (var facimane in data_arr) {
				// 単体の場合は、この中でselfを生成する
				// 統合の場合は、このループを抜けてからselfを生成する

				if (!getGraphConfig("data-total-flg")) {
					nodatacount = 0;
				}
				var alldata = [];
				var masteritemdata = data_arr[facimane][mastermonitorid];
				var collectid = masteritemdata.collectid;
				var startdate = masteritemdata.startdate;
				var enddate = masteritemdata.enddate;
				// グラフのx軸タイトル(master)の作成、あとで反映する
				item_bottom = itemmaster;
				if (masteritemdata.measure != "") {
					item_bottom+="[" + masteritemdata.measure + "]";
				}
				
				// マスター情報の作成
				for (var i = 0; i < masteritemdata.data.length; i++) {
					if (isReallyNaN(Number(masteritemdata.data[i][1]))) {
						// NaNだった場合はデータを使用しないためループの先頭に戻る
						continue;
					}
					var data = {};
					data.key = facimane;
					data.managername = masteritemdata.managername;
					data.realmanagername = masteritemdata.realmanagername;
					data.monitoridx = mastermonitorid;
					data.collectidx = collectid;
					data.facilityid = masteritemdata.facilityid;
					data.facilityname = masteritemdata.facilityname;
					data.realfacilityid = masteritemdata.realfacilityid;
					data.itemmaster = itemmaster;
					data.date = masteritemdata.data[i][0];
					data.valuex = masteritemdata.data[i][1];
					alldata.push(data);
				}// マスター情報の作成終了
				
				// サブデータの情報作成開始(マスターにサブの情報を追加していく)
				var facilityitemdata = data_arr[facimane][submonitorid];
				if (facilityitemdata == null) {
					// マスターデータはあるがサブデータが無い場合(グラフ表示数制限でマスター情報しか取れていない場合など)
					continue;
				}
				// グラフy軸のタイトル作成、あとで反映する
				item_left = itemorder[k].itemname + "(" + itemorder[k].monitorid + ")";
				if (facilityitemdata.measure != "") {
					item_left += "[" + facilityitemdata.measure + "]";
				}
				var itemdata = JSON.parse(JSON.stringify(alldata));// マスターデータのコピー
				var collectidsub = facilityitemdata.collectid;
				if (collectidsub != "none" && collectid != "none") {
					nodatacount++;
				}
				for (var i = 0; i < facilityitemdata.data.length; i++) {
					for (var j = 0; j < itemdata.length; j++) {
						// 1つ目の同じ日付のところに入れる
						if (facilityitemdata.data[i][0] == itemdata[j].date) {
							if (isReallyNaN(Number(facilityitemdata.data[i][1]))) {
								// NaNだった場合はデータを使用しないためループの先頭に戻る
								continue;
							}
							var data = itemdata[j];
							data.monitoridy = submonitorid;
							data.collectidy = facilityitemdata.collectid;
							data.itemsub = itemorder[k].itemname + "(" + itemorder[k].monitorid + ")";
							data.date = facilityitemdata.data[i][0];
							data.valuey = facilityitemdata.data[i][1];
							break;
						}
					}
				}
				var monitorsort = [mastermonitorid, submonitorid];
				monitorsort.sort(sortStr);
				if (!getGraphConfig("data-total-flg")) {
					id = monitorsort[0] + monitorsort[1] + masteritemdata.managername + masteritemdata.facilityid;
				} else {
					id = monitorsort[0] + monitorsort[1];
				}
				facidataarr[facimane] = itemdata;
				
				// selfに情報を入れていく
				var self = scattergraph[id];
				self.startdate = startdate;
				self.enddate = enddate;
				
				if (collectidsub != "none") {
					self.graphrange_y = facilityitemdata.graphrange;
				}
				if (collectid != "none") {
					self.graphrange_x = masteritemdata.graphrange;
				}
				
				if (self.lineids.indexOf(facimane) < 0) {
					// 初回の場合はnewする
					self.lineids[self.lineids.length] = facimane;
					self.points2[facimane] = [];
					self.colorlist[facimane] = self.colors(self.lineids.length);
				}
				self.points2[facimane] = self.points2[facimane].concat(itemdata);
				
				// ソート
				self.points2[facimane].sort(
				function(a, b) {
					if (a.date < b.date) { return -1 };
					if (a.date > b.date) { return 1 };
					return 0
				});
				// 重複データの除去と対象外データの除去
				// valueyにデータがあってvaluexにデータが無いものも除去
				// valueyにデータが無くってvaluexにあるものは、objectは作られていないので考慮不要
				var newlinepoints = self.points2[facimane].filter(function (x, i, self) {
					var count = 0;
					for (count = 0; count < self.length; count++) {
						if (self[count].date == x.date // 日付重複チェック
								&& self[count].date >= startdate && self[count].date <= enddate // 日付対象内チェック
								&& typeof(self[count].valuey) != "undefined") { // データの整合性チェック
							break;
						}
					}
					return count == i;
				});
				
				// 重複データ除去後のデータを正規データとする
				self.points2[facimane] = newlinepoints;
					
				if (getGraphConfig("data-approx-flg")) {
					// 近似フラグがtrueの場合は、近似直線情報を分析する
					// 分析結果はそれぞれメンバに抑えるため戻り値無し
					self.getRegressionInfo();
					self.drawApprox();
				}
				// itemName[単位]の更新
				self.addTitle(self, "title_left", item_left);
				self.addTitle(self, "title_bottom", item_bottom);

				if (!getGraphConfig("data-total-flg")) {
					if (getGraphConfig("data-legend-flg")) {
						// 凡例の描画
						self.createScatterLegend();
					}
					self.createGtagDot(nodatacount);
				}
			}
			if (getGraphConfig("data-total-flg")) {
				if (getGraphConfig("data-legend-flg")) {
					// 凡例の描画
					self.createScatterLegend();
				}
				self.createGtagDot(nodatacount);
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
// 凡例を作成します。
Scattergraph.prototype.createScatterLegend = function() {
	var self = this;
	if (getGraphConfig("data-legend-flg")) {
		// 凡例を作成する
		var legendlist = [];
		// 凡例を全消しする
		self.vis.selectAll(".legend").remove();

		for (facimanekey in self.points2) {
			if (self.points2[facimanekey] != null && self.points2[facimanekey].length != 0) legendlist.push(facimanekey);
				if (legendlist.length == HINEMOS_COLLECT_CONST.CONST_STACKED_TOOLTIP_COUNT) {
					legendlist.push("omitted below");// facilityidに半角空白は入らない
					break;
				}
		}
		
		var legend = self.vis.selectAll(".legend")
		.data(legendlist)
		.enter().append("g")
		.attr("class", "legend")
			.attr("transform", function(d, i) {
				var x_posi = 0;
				var j = 0;
				if (i % 2 == 0) {
					x_posi = (-self.width) + 20;
				} else {
					x_posi = (-self.width/2) + 20;
				}
				j = Math.floor(i/2);// 何段目か
				return "translate(" + x_posi + "," + (j * 20 + self.height + 38) + ")";// itemName表示があるため、graph.jsと位置が異なる
			});

		legend.append("rect")
		.attr("rx", 5)
		.attr("ry", 5)
		.attr("x", self.width - 18)
		.attr("width", 10)
		.attr("height", 10)
		.style("fill", function(d) { return self.colorlist[d]; });

		legend.append("text")
		.attr("x", self.width-6)
		.attr("y", 7)
		.attr("dy", ".35em")
		.attr("font-size", "7pt")
		.attr("fill", function(d) { 
			var target = self.points2[d];
			if (target == null) return HINEMOS_COLLECT_CONST.CONST_COLOR_RED;
		})
		.style("text-anchor", "start")
		.text(function(d) { 
			var target = self.points2[d];
			if (target == null) return d;
			var facilityname = target[0].facilityname;
			var managername = target[0].realmanagername;
			return truncateText(facilityname + "(" + managername + ")", 30); 
		})
		.append("title") // 凡例マウスオーバーでフル出力
			.text(function(d) {
				var target = self.points2[d];
				if (target == null) return d;
				var facilityname = target[0].facilityname;
				var managername = target[0].realmanagername;
				return facilityname + "(" + managername + ")"; 
			});

		// 凡例件数によって縦幅を広げる
		var times = Math.round(legendlist.length/2);
		if (legendlist.length == 0) {
			times = 0;
		}
		// div
		var divheight = self.baseheight;
		var newheight = (Number(divheight) + HINEMOS_COLLECT_CONST.CONST_GRAPH_RIGHT_LEGEND_HEIGHT * times) + 20;// 20は上下のマージン
		self.chart.style.height = newheight + "px";
		
		// svg
		d3.select(self.chart).select(".svg_all").attr("height", newheight);
		self.vis.selectAll(".graph_background_block2").attr("height", newheight);
		
		// 左のitemNameの位置を調整、ここに来るときには単位は追加済み
		self.vis.select("#title_left text").attr("x", -(newheight - this.margin.top - this.margin.bottom)/2);
	}
	
};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// 左に表示しているitemNameがグラフ描画領域を超えて表示されないように調整します
// すべてのグラフの凡例描画処理が終わってから処理を実施してください
Scattergraph.prototype.cleanItemName = function() {
	for (var keyValue in scattergraph) {
		var self = scattergraph[keyValue];
		var svgheight = d3.select(self.chart).select(".graph_background_block2").attr("height");
		var lefttitle = self.vis.select("#title_left").text();
		if (self.ylabel == "") {
			self.ylabel = lefttitle;
		}
		var i = 0;
		for (i = 0; i < lefttitle.length; i++) {
			if (svgheight < i*14) {
				break;
			}
		}
		lefttitle = truncateText(lefttitle, i*2);
		self.vis.select("#title_left text").text(lefttitle);
		
		var svgwidth = d3.select(self.chart).select(".graph_background_block2").attr("width");
		var bottomtitle = self.vis.select("#title_bottom").text();
		if (self.xlabel == "") {
			self.xlabel = bottomtitle;
		}
		var i = 0;
		for (i = 0; i < bottomtitle.length; i++) {
			if (svgwidth < i*14) {
				break;
			}
		}
		bottomtitle = truncateText(bottomtitle, i*2);
		self.vis.select("#title_bottom text").text(bottomtitle);
	}
};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// gタグを作成し、その中にdotを作成します。
// 作成したdotにツールチップを設定します。
// すべてのデータがselfに設定できてから呼ぶこと
Scattergraph.prototype.createGtagDot = function(nodatacount) {
	var self = this;
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


	self.points.selectAll('.dot')
	.data(function(d, index){
		var dd = self.points2[d];
		var a = [];
		dd.forEach(function(point, i){
			a.push({'index': index, 'point': point});
		});
		return a;
	})
	.enter()
	.append('circle')
	.attr('class', function(d) {
		return 'dot ' + d.point.facilityid;
	})
	.attr("r", function(d) {
		return 2;
	})
	.attr('fill', function(d, i){
		return self.colorlist[d.point.key];
	})
	.attr("transform", function(d) { 
		return "translate(" + self.x(d.point.valuex) + "," + self.y(d.point.valuey) + ")"; }
	)
	// tooltipの設定
	.on("mouseover", function(d) { // 点のmouseoverイベント
		var parseDate = d3.time.format('%Y/%m/%d %H:%M:%S');
		var str = "<dl>";
		str += "<dt style='border-bottom-color :" + self.colorlist[d.point.key] + ";border-left-color :" + self.colorlist[d.point.key] + ";word-wrap: break-word;'><p style='margin:0px'>" 
			+ d.point.realmanagername + "  :  <" + parseDate(new Date(d.point.date)) + ">" + "</p><p style='margin:0px'>" + d.point.facilityname + "(" + d.point.realfacilityid + ")</p></dt>";
		str += "<dd>" 
			+ d.point.itemsub + " : " + d.point.valuey + "</dd>";
		str += "<dd>" 
			+ d.point.itemmaster + " : " + d.point.valuex + "</dd>";
		str += "</dl>";

		d3.select("body").select(".tooltip_mouse").style("visibility", "visible").style("width", "300px").html(str);
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
	self.getCollectIdNone(self, nodatacount);
	self.update();
};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// プロット情報から近似直線用の情報を作成します
Scattergraph.prototype.getRegressionInfo = function() {
	var self = this;
	// 初回のみ作成
	if (self.points2 == null) {
		return;
	}

	for (key in self.points2) {// key:facilityid+managername
		var valuexarr = [];
		var valueyarr = [];
		var target = self.points2[key];
		if (target.length == 0) {
			continue;
		}
		var managername;
		var realamanagername;
		var facilityid;
		var facilityname;
		var key;
		for (var i = 0; i < target.length; i++) {
			var valuex = target[i].valuex;
			var valuey = target[i].valuey;
			valuexarr.push(valuex);
			valueyarr.push(valuey);
			managername = target[i].managername;
			realmanagername = target[i].realmanagername;
			facilityid = target[i].facilityid;
			facilityname = target[i].facilityname;
			key = target[i].key;
		}
		
		// 傾き・切片の算出
		self.getSlopeInterceptR2(valuexarr, valueyarr, key);
		
		// 傾き・切片を算出できたので描画する線の情報を算出する
		// x値(valuex)のソート
		valuexarr.sort(sortStr);

		self.approxarr[key] = [];
		for (var i = 0; i < valuexarr.length; i++) {
			var obj = {};
			obj.x = valuexarr[i];
			obj.y = self.slope[key] * obj.x + self.intercept[key];
			obj.realmanagername = realmanagername;
			obj.managername = managername;
			obj.facilityid = facilityid;
			obj.facilityname = facilityname;
			obj.key = key;
			self.approxarr[key].push(obj);
		}
	}
};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// プロット情報から近似直線用の情報を分析します
// 分析結果は各メンバに設定します
Scattergraph.prototype.getSlopeInterceptR2 = function(valuexarr, valueyarr, key) {
	var self = this;
	if (valuexarr.length != valueyarr.length) {
		throw new IllegalArgumentException("array lengths are not equal");
	}
	var valuexlength = valuexarr.length;

	// first pass
	var sum1 = 0.0, sum2 = 0.0;
	for (var i = 0; i < valuexlength; i++) {
		sum1 += valuexarr[i];
		sum2 += valueyarr[i];
	}
	var xbar = sum1 / valuexlength;
	var ybar = sum2 / valuexlength;

	// second pass: compute summary statistics
	var xxbar = 0.0, yybar = 0.0, xybar = 0.0;
	for (var i = 0; i < valuexlength; i++) {
		xxbar += (valuexarr[i] - xbar) * (valuexarr[i] - xbar);
		yybar += (valueyarr[i] - ybar) * (valueyarr[i] - ybar);
		xybar += (valuexarr[i] - xbar) * (valueyarr[i] - ybar);
	}
	if (xybar != 0 && xxbar != 0) {
		self.slope[key]  = xybar / xxbar; // 傾き
	} else {
		// 0/0が発生してしまう
		self.slope[key]  = 0;
	}
	self.intercept[key] = ybar - self.slope[key] * xbar;// 切片

	// more statistical analysis
	var rss = 0.0;	  // residual sum of squares
	var ssr = 0.0;	  // regression sum of squares
	for (var i = 0; i < valuexlength; i++) {
		var fit = self.slope[key]*valuexarr[i] + self.intercept[key];
		rss += (fit - valueyarr[i]) * (fit - valueyarr[i]);
		ssr += (fit - ybar) * (fit - ybar);
	}

	var degreesOfFreedom = valuexlength-2;
	self.R2[key] = ssr / yybar;
	svar = rss / degreesOfFreedom;
	svar1 = svar / xxbar;
	svar0 = svar/valuexlength + xbar*xbar*svar1;
};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// プロット情報から近似直線を描画します
Scattergraph.prototype.drawApprox = function() {
	var self = this;
	
	// グラフの描画(pathタグの追加、データは別途追加)
	self.vis.selectAll('.line')
	.data(self.lineids)
	.enter()
	.append("path")
		.attr("class", "line")
	.attr("id", function(d, i) {return d;})
	.style("stroke-width", HINEMOS_COLLECT_CONST.CONST_LINE_WIDTH_DEFAULT)
	.style("fill", "none")
	.attr("clip-path", "url(#clip)")
	.on("mouseover", function(d) { // 線のmouseoverイベント
		var facilityname = self.points2[d][0].facilityname;
		var managername = self.points2[d][0].managername;
		var realmanagername = self.points2[d][0].realmanagername;
		var color = self.colorlist[d];
		var sign = "+";
		if (self.intercept[d] < 0) {
			sign = "";
		}
		var str = "<dl>";
		str += "<dt style='border-bottom-style:dashed;border-bottom-color :" + color + ";border-left-color :" + color + ";word-wrap: break-word;'>" + facilityname + "(" + realmanagername + ")</dt>";
		str += "<dd style='border-bottom-style:dashed'>y = " + (Number(self.slope[d])).toPrecision(4) + "x " + sign + (Number(self.intercept[d])).toPrecision(4) + "</dd>";
		str += "<dd style='border-bottom-style:dashed'>R2 = " + (Number(self.R2[d])).toPrecision(4) + "</dd>";
		d3.select("body").select(".tooltip_mouse").style("visibility", "visible").style("width", "300px").html(str);
		var paths = document.getElementsByTagName("path");
		for (var i = 0; i < paths.length; i++) {
			var pathsingle = paths[i];
			if (pathsingle.id == d) {
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
		disableTooltip();
		var paths = document.getElementsByTagName("path");
		for (var i = 0; i < paths.length; i++) {
			var pathsingle = paths[i];
			if (pathsingle.id == d) {
				d3.select(pathsingle).style("stroke-width", HINEMOS_COLLECT_CONST.CONST_LINE_WIDTH_DEFAULT);
			}
		}
	})
	.attr('stroke', function(d,i){
		return self.colorlist[d];
	});

	// 線の追加(データ)
	for (j = 0; j < self.lineids.length; j++) {
		var id = self.lineids[j];
		// 初回以外はプロットも消す
		if (self.points != null) {
			self.points.selectAll("g#" + id + " circle")
			.data(10)
			.exit()
			.remove();
		}
	}
};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// グラフの左と下に表示されている監視項目を追加します
Scattergraph.prototype.addTitle = function(self, idtitle, title) {
	if (title == "") {
		return;
	}
	var monitortitle = self.vis.select("#" + idtitle).text();
	if (monitortitle.slice(-1) != "]") {
		// タイトルは後から追加する、未追加の場合は追加
		self.vis.select("#" + idtitle + " text")
		.text(title);
	}
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// collectIdが取れなかったノードがある場合に、グラフの上に表示されているタイトルを変更します
// 線グラフ、積み上げグラフ共有関数です
Scattergraph.prototype.getCollectIdNone = function(self, nodatacount) {
	var nodestr = getGraphMessages("mess-nodes");
	var totalstr = "(" + getGraphMessages("mess-total");
	if (getGraphConfig("data-total-flg")) {
		var graphsize = self.vis.select("#title_top").text();
		if (graphsize.indexOf(nodestr) == -1) {
			if (nodatacount != Number(graphsize)) {
				// まとめて表示、かつ、collectidが取れなかったものがある場合はタイトルを変更する
				// 未書き換えの場合のみ
				var newtitle = nodatacount + nodestr + totalstr + graphsize + nodestr + ")";
				self.vis.select("#title_top text")
				.text(newtitle);
				// グラフタイトルをメンバに抑える
				self.title = newtitle;
				if (nodatacount == 0) {
					// 全ノードがnone(collectidが存在しない)ので、背景をグレー、X軸のメモリ文字色を白にする
					dispNoData(self);
				}
			} else {
				// 背景はなし(線あり)
				self.vis.selectAll(".graph_background_block").style("fill", "none");
				// まとめて表示、かつ、collectidが取れなかったものがある場合はタイトルを変更する
				var graphsize = self.vis.select("#title_top").text();
				// 未書き換えの場合のみ
				var newtitle = graphsize + nodestr;
				self.vis.select("#title_top text")
				.text(newtitle);
				// グラフタイトルをメンバに抑える
				self.title = newtitle;
			}
		}
	} else {
		//	単体表示の場合
		if (nodatacount == 0) {
			// 単体表示でcollectidが取れなかった場合は、背景をグレーにする(tickと同じ色)
			dispNoData(self);
			// 閾値表示している場合は、閾値バーの透過度などを見えなくする
			ThresholdGraph.prototype.setThresholdOpacityZero(self.innerid);
		} else {
			// 背景はなし(線あり)
			self.vis.selectAll(".graph_background_block").style("fill", "none");
		}
	
	}
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
//すべてのグラフベースの線を消します
// start_date:表示範囲の最古日時、end_date:表示範囲の最新時刻
Scattergraph.prototype.removePoints = function(startdate, enddate, alldel) {
	try {
		for (var keyValue in scattergraph) {
			var self = scattergraph[keyValue];
			// グラフ画像保存と自動更新で使用するため、期間をメンバに保持
			self.startdate = startdate;
			self.enddate = enddate;
			
			for (var keyline in self.points2) {
				// 重複データの除去と期間外の除去
				self.points2[keyline] = self.points2[keyline].filter(function (x, i, self) {
					var count = 0;
					for (count = 0; count < self.length; count++) {
						if (self[count].date == x.date && self[count].date >= startdate && self[count].date <= enddate) {
							break;
						}
					}
					return count == i;
				});
				// プロットも消す
				self.points.selectAll("g#" + keyline + " circle")
				.data(function(d, index) {
					var dd = self.points2[d];
					var a = [];
					dd.forEach(function(point, i){
						a.push({'index': index, 'point': point});
					});
					return a;
				})
				.exit()
				.remove();
			}
			if (getGraphConfig("data-approx-flg")) {
				self.getRegressionInfo();
			}
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
// Method
//////////////////////////////////////////////////////////////

ControlScattergraph = {};

// chart_blockの子要素をすべて削除する
// また、保持しているグラフ情報も削除する
ControlScattergraph.delDiv = function () {
	try {
		var aNode = document.getElementById("chart_block");
		for (var i =aNode.childNodes.length-1; i>=0; i--) {
			aNode.removeChild(aNode.childNodes[i]);
		}
		scattergraph = null;
		scattergraph = [];
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
ControlScattergraph.addDiv = function (parentid, name) {
	try {
		var div_child = document.createElement('div');
		div_child.id = name;//total:monitorId, single:facilityId_managerName_monitorId
		div_child.classList.add("chart");
		var height = HINEMOS_COLLECT_CONST.CONST_SCATTERGRAPH_HEIGHT;
		height += HINEMOS_COLLECT_CONST.CONST_GRAPH_RIGHT_LEGEND_HEIGHT;
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
ControlScattergraph.addGraphAtOnce = function (plotjson, graphSize, preferenceSize, settinginfo) {
	try {
		// 設定情報を解析して保持する
		setGraphConfig(settinginfo);
		// グラフ数カウント
		var graphcount = preferenceSize;

		// 情報の解析
		var itemorder = plotjson.orderitem;
		var itemmasterobj = itemorder[0];
		itemorder.shift();
		var itemmaster = itemmasterobj.itemname + "(" + itemmasterobj.monitorid + ")";//.splice(0, 1)[0];
		var baseall = [];
		var id = ""; // total:monitorid+monitorid, single:monitorid+monitorid+facilityid
		var parentid = "p_";
		var item = null;
		for (var i = 0; i < plotjson.all.length; i++) {
			var item1 = plotjson.all[i];
			var monitorid = item1.monitorid;
			var displayname = item1.displayname;
			var itemname = item1.itemname;
			var facilityid = item1.facilityid;
			var managername = item1.managername;

			if (getGraphConfig("data-total-flg")) {
				// まとめて表示の場合
				baseall[itemname + "(" + monitorid + ")"] = item1;
			} else {
				// 単品表示の場合
				if (baseall[managername + facilityid] == null) {
					baseall[managername + facilityid] = [];
				}
				var targetmoni = baseall[managername + facilityid];
				if (targetmoni == null) {
					targetmoni = [];
				}
				targetmoni[itemname + "(" + monitorid + ")"] = item1;
			}
		} // 情報の解析が終了
		
		if (getGraphConfig("data-total-flg")) {
			// マスター情報
			var mastermonitorinfo = baseall[itemmaster];
			var mastermonitorid = mastermonitorinfo.monitorid;
			var masterdisplayname = mastermonitorinfo.displayname;
			var mastermanagername = mastermonitorinfo.managername;
			var masteritemname = mastermonitorinfo.itemname;
			// 解析結果を元にnewする
			for (var i = 0; i < itemorder.length; i++) {
				// サブ情報
				var monikey = itemorder[i].itemname + "(" + itemorder[i].monitorid + ")";
				var monitorinfo = baseall[monikey];
				if (monitorinfo == null) {
					// マスターデータはあるがサブデータが無い場合(グラフ表示数制限でマスター情報しか取れていない場合など)
					continue;
				}
				var slavemonitorid = monitorinfo.monitorid;
				var slavedisplayname = monitorinfo.displayname;
				var slaveitemname = monitorinfo.itemname;
				var facilityid = monitorinfo.facilityid;
				// サブ情報にマスター情報を入れる
				monitorinfo.mastermonitorid = mastermonitorid;
				monitorinfo.masterdisplayname = masterdisplayname;
				monitorinfo.masteritemname = masteritemname;
			
				var monitorsort = [monitorinfo.masteritemname + mastermonitorid, slaveitemname + slavemonitorid];
				monitorsort.sort(sortStr);
				id = monitorsort[0] + monitorsort[1];
				var parentid = monitorinfo.itemname + monitorinfo.monitorid;
				ControlScattergraph.addDiv(parentid, id);
				graphcount = graphcount - graphSize;
				if (graphcount < 0) {
					graphSize = graphSize + graphcount;
				}
				scattergraph[id] = new Scattergraph(id, monitorinfo, graphSize);
			}
		} else { // シングル表示の場合
			for (var i = 0; i < itemorder.length; i++) {
				for (var key in baseall) {// single:key=facilitymanager
					var targetmoni = baseall[key];//key:managername+facilityid

					// マスター情報
					var mastermonitorinfo = targetmoni[itemmaster];
					var mastermonitorid = mastermonitorinfo.monitorid;
					var masterdisplayname = mastermonitorinfo.displayname;
					var mastermanagername = mastermonitorinfo.managername;
					var masteritemname = mastermonitorinfo.itemname;
					// サブ情報
					var monikey = itemorder[i].itemname + "(" + itemorder[i].monitorid + ")";
					var monitorinfo = targetmoni[monikey];
					if (monitorinfo == null) {
						// マスターデータはあるがサブデータが無い場合(グラフ表示数制限でマスター情報しか取れていない場合など)
						continue;
					}
					var slavemonitorid = monitorinfo.monitorid;
					var slavedisplayname = monitorinfo.displayname;
					var slaveitemname = monitorinfo.itemname;
					var facilityid = monitorinfo.facilityid;
					// サブ情報にマスター情報を入れる
					monitorinfo.mastermonitorid = mastermonitorid;
					monitorinfo.masterdisplayname = masterdisplayname;
					monitorinfo.masteritemname = mastermonitorinfo.itemname;
				
					var monitorsort = [masteritemname + mastermonitorid, slaveitemname + slavemonitorid];
					monitorsort.sort(sortStr);
					id = monitorsort[0] + monitorsort[1] + mastermanagername + facilityid;
					var parentid = monitorinfo.itemname + monitorinfo.monitorid;
					ControlScattergraph.addDiv(parentid, id);
					graphcount = graphcount - graphSize;
					if (graphcount < 0) {
						graphSize = graphSize + graphcount;
					}
					scattergraph[id] = new Scattergraph(id, monitorinfo, graphSize);
				}
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
	// プロットを一気に追加します
ControlScattergraph.addPlotAtOnce = function (plotjson) {
	var stacks = getGraphConfig("data-stack-flg");
	try {
		// tooltipを消す
		disableTooltip();
		
		var length = plotjson.all.length;
		var data_arr = [];
		// データの整理
		for (var i = 0; i < length; i++) {
			var item = plotjson.all[i];
			var groupid = item.groupid;
			groupid = item.itemname + item.monitorid;
			var id = item.facilityid + "_" + item.managername;// + item.collectid;
			if (data_arr[id] == null) {
				data_arr[id] = [];
			}
			data_arr[id][groupid] = item;
		}
		Scattergraph.prototype.createPoints(data_arr, plotjson.orderitem);
		Scattergraph.prototype.cleanItemName();
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
// 散布図は分岐しないのでメソッドを呼ぶだけ
ControlScattergraph.trimBranch = function() {
	ControlScattergraph.trimYAxis();
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// y軸はmasterMonitorIDなので、全グラフ同じになるようにします(すべてのグラフの最小・最大をチェックして反映します)
// x時はsubMasterIDごとに同じになるようにします
ControlScattergraph.trimYAxis = function () {
try {
	// itemNameとmonitoridの取得
	var itemTrimList = [];
	for (var keyValue in scattergraph) {
		var self = scattergraph[keyValue];
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
		if (itemflg) {
			// すでにリストに入っているなら先頭に戻る
			continue;
		}
		itemTrimList.push(itemmoni);
	}
	// itemname+monitoridの配列が完成↑

	for (var j = 0; j < itemTrimList.length; j++) {
		var max_x = Number.MIN_VALUE/2;
		var min_x = Number.MAX_VALUE;
		var max_y = Number.MIN_VALUE/2;
		var min_y = Number.MAX_VALUE;
		var itemtrim = itemTrimList[j];
		var graphrange_x = null;
		var graphrange_y = null;
		for (var keyValue in scattergraph) {
			var self = scattergraph[keyValue];
			var itemname = self.itemname;
			var monitorid = self.monitorid;
			var itemmoni = itemname + monitorid;
			if (itemmoni != itemtrim) {
				continue;
			}
			if (self.graphrange_x != null) {
				graphrange_x = self.graphrange_x;
			}
			if (self.graphrange_y != null) {
				graphrange_y = self.graphrange_y;
			}
			if (graphrange_x && graphrange_y) {
				break;
			}
			for (var linekey in self.points2) {
				for (var k = 0; k < self.points2[linekey].length; k++) {
					if (graphrange_x == false && max_x < self.points2[linekey][k].valuex) {
						max_x = self.points2[linekey][k].valuex;
					}
					if (graphrange_x == false && min_x > self.points2[linekey][k].valuex) {
						min_x = self.points2[linekey][k].valuex;
					}
					if (graphrange_y == false && max_y < self.points2[linekey][k].valuey) {
						max_y = self.points2[linekey][k].valuey;
					}
					if (graphrange_y == false && min_y > self.points2[linekey][k].valuey) {
						min_y = self.points2[linekey][k].valuey;
					}
				}
			}
		}
		// 軸のサイズが確定↑
		
		// x軸のサイズを反映↓
		// 余白の設定
		if (graphrange_x) {
			max_x = 110;
			min_x = -10;
		} else {
			if (max_x == Number.MIN_VALUE || min_x == Number.MAX_VALUE) {
				max_x = 10;
				min_x = 0;
			} else if (max_x == min_x) {
				min_x-=1;
				max_x+=1;
			} else {
				var term = (max_x -min_x) / 10;
				min_x = min_x - term;
				max_x = max_x + term;
			}
		}
		// y軸のサイズを反映↓
		// 余白の設定
		if (graphrange_y) {
			max_y = 110;
			min_y = -10;
		} else {
			if (max_y == Number.MIN_VALUE || min_y == Number.MAX_VALUE) {
				max_y = 10;
				min_y = 0;
			} else if (max_y == min_y) {
				min_y-=1;
				max_y+=1;
			} else {
				var term = (max_y -min_y) / 10;
				min_y = min_y - term;
				max_y = max_y + term;
			}
		}
		var x_domain = [min_x, max_x];
		var y_domain = [min_y, max_y];
		for (var keyValue in scattergraph) {
			var self = scattergraph[keyValue];
			var itemname = self.itemname;
			var monitorid = self.monitorid;
			var itemmoni = itemname + monitorid;
			if (itemmoni != itemtrim) {
				continue;
			}
			self.x.domain(x_domain)
			.range([0, self.width]);
			self.y.domain(y_domain)
			.range([self.height, 0]);
			self.redraw()();
			self.animationScatter();
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
ControlScattergraph.trimXAxis = function(xmin, xmax, ignoreid) {
	// nop
	return;
};

//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
Scattergraph.prototype.animationScatter = function() {
	var self = this;
	self.vis.select("#clip rect").attr("width", 0);

	self.vis.select("#clip rect")
	.transition().duration(HINEMOS_COLLECT_CONST.CONST_ANIMATION_INTERVAL)
	.attr("width", self.width);
} // end of animationScatter
