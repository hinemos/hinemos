/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 *
 * 性能[グラフ]の円グラフを描画するJavaScriptです。
 */
Piegraph = function(elementid, options, graphsize) {
var self = this;


var pie_width = 200,
	pie_height = 200,
	pie_radius = Math.min(pie_width, pie_height) / 2;
	
var width = HINEMOS_COLLECT_CONST.CONST_PIEGRAPH_WIDTH - HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_LEFT - HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_RIGHT;
var height = HINEMOS_COLLECT_CONST.CONST_PIEGRAPH_HEIGHT - HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_TOP - HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_BOTTOM;

var margin = {top: HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_TOP, right: HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_RIGHT, bottom: HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_BOTTOM, left: HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN_LEFT};

this.startdate = options.startdate;
this.enddate = options.enddate;
this.innerid = elementid;

// 表示しているすべての情報源
this.piedata = [];

this.chart = document.getElementById(elementid);
this.graphtype = HINEMOS_COLLECT_CONST.CONST_PIEGRAPH;

this.color = d3.scale.category10();

this.arc = d3.svg.arc()
	.outerRadius(pie_radius - 10)
	.innerRadius(0);

this.labelArc = d3.svg.arc()
	.outerRadius(pie_radius - 40)
	.innerRadius(pie_radius - 40);

// pieはデータを取って、data / value / startAngle / endAngleのオブジェクトを作る。
// スタートとエンドが被らないように、さらに全体が360になるようにマッピングしてくれる。
this.pie = d3.layout.pie()
	.sort(function(a, b) { return b.total - a.total; })
	.value(function(d) { return d.total; });

this.vis = d3.select(this.chart)
	.append("svg")
	.attr("class", "svg_all")
	.attr("width", width + margin.left + margin.right)
	.attr("height", height + margin.top + margin.bottom)
	.append("g")
		.attr("transform", "translate(" + margin.left + "," + margin.top + ")")

this.plot2 = this.vis.append("rect")
	.attr("class", "graph_background_block2")
	.attr("fill", HINEMOS_COLLECT_CONST.CONST_COLOR_WHITE)
	.attr("stroke", "#c0c0c0")
	.attr("stroke-width", HINEMOS_COLLECT_CONST.CONST_GRAPH_BORDER_WIDTH + "px")
	.attr("width", width + margin.left + margin.right)
	.attr("height", height + margin.top + margin.bottom)
	.attr("x", -margin.left)
	.attr("y", -margin.top)
	.attr("pointer-events", "all");
	
// グラフタイトル部分の色
this.plot2 = this.vis.append("rect")
	.attr("class", "graph_title_rect")
	.attr("fill", HINEMOS_COLLECT_CONST.CONST_COLOR_HINEMOS_BASE)
	.attr("width", width + margin.left + margin.right)
	.attr("height", HINEMOS_COLLECT_CONST.CONST_GRAPH_TITLE_HEIGHT)
	.attr("x", -margin.left)
	.attr("y", -margin.top)
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
	.attr("x", width-20)
	.attr("y", (-margin.top)+5)
	.attr("pointer-events", "all")
	.attr("cursor", "pointer");
this.vis.selectAll(".arrow") // title
	.append("title")
	.text("download...")
	.attr("cursor", "pointer");
this.vis.selectAll(".arrow") // text
	.append("text")
	.text("PNG")
	.attr("font-size", "7pt")
	.attr("fill", HINEMOS_COLLECT_CONST.CONST_COLOR_HINEMOS_BASE)
	.attr("y", (-margin.top)+15)
	.attr("x", width-14)
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
var y_posi = (-margin.top) + 15;
var x_posi = width/2;
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
	.text(truncateByWidth(title, width, 'Meiryo, メイリオ',"9pt", "bold"))
	.on("mouseout", function(){
		disableTooltip();
	})
	.on("mouseover", function(){
		var pagey = d3.event.pageY;
		var pagex = d3.event.pageX;
		if (!checkBrowserKind("firefox") && (event.pageY == undefined || event.pageX == undefined)) { // IE9、10でのevent.pageX、event.pageYが取れない対策
			pagex = event.clientX + (document.body.scrollLeft || document.documentElement.scrollLeft);
			pagey = event.clientY + (document.body.scrollTop || document.documentElement.scrollTop);
		}
		d3.select("body").select(".tooltip_mouse")
		.style("visibility", "visible")
		.style("width", "300px")
		.style("font-size", "8pt")
		.style("word-wrap","break-word")
		.html(title)
		.style("top", (pagey-20)+"px")
		.style("left",(pagex+20)+"px");
	});

// グラフのitemName(左)表示
this.ylabel = options.ylabel;
var lefttitle = options.ylabel;
var i = 0;
for (i = 0; i < lefttitle.length; i++) {
	if (height < i*11) {
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
	.attr("y", (-margin.left) + 14)
	.attr("x", -height/2)
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
	.attr("width", width)
	.attr("height", height);

this.vis.append("g")
	.attr("id", "piepie")
	.attr("transform", "translate(" + pie_width/2 + "," + ((pie_height-HINEMOS_COLLECT_CONST.CONST_GRAPH_TITLE_HEIGHT)/2) + ")");
	
d3.select("body")
	.style("font-size", "10pt")
	.style("font-family", "Meiryo, メイリオ");

}// end of Piegraph
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// 円グラフを作成します。
Piegraph.prototype.createPie = function(data) {
var self = this;

// 円グラフを消す
self.vis.select("#piepie").selectAll(".arc").remove();

var g = self.vis.select("#piepie").selectAll(".arc")
		.data(this.pie(data))
	.enter().append("g")
		.attr("class", "arc")
		.style("text-anchor", "middle");

g.append("path")
		.attr("d", self.arc)
		.style("fill", function(d) { 
			return d.data.color; })
		.on('mouseover', function(d) { 
			d3.select("body").select(".tooltip_mouse").style("visibility", "visible").style("width", "300px").html(d.data.tooltip);
		 })
		.on("mousemove", function(d) {
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
		.on("mouseout", function(d) {
			disableTooltip();
		})
		.style("opacity", "0.7")
		.style("stroke", HINEMOS_COLLECT_CONST.CONST_COLOR_GRAY)
		.transition()
		.duration(HINEMOS_COLLECT_CONST.CONST_ANIMATION_INTERVAL_PIE)
		.attrTween("d", function(d){
			var interpolate = d3.interpolate(
				{ startAngle : 0, endAngle : 0 },
				{ startAngle : d.startAngle, endAngle : d.endAngle }
			);
			return function(t){
				return self.arc(interpolate(t));
			}
		});

g.append("text")
		.attr("transform", function(d) { return "translate(" + self.labelArc.centroid(d) + ")"; })
		.attr("dy", ".35em")
		.text(function(d) {
			if (d.data.percent >= 10) { // 10%以下の場合は、円グラフに文字を描画しない
				return d.data.label;
			}
		 })
		.style("font-size", "8pt")
		.attr("fill", function(d) { // 背景色が濃い場合は文字を白色にする
			return HINEMOS_COLLECT_CONST.CONST_COLOR_BLACK;
		});
g.append("text")
		.attr("transform", function(d) { 
			var position = self.labelArc.centroid(d);
			return "translate(" + position[0] + ", " + (position[1]+12) + ")"; })
		.attr("dy", ".35em")
		.text(function(d) {
			if (d.data.priority == undefined) { // データが無い場合はpriorityが無い
				return "";
			}
			if (d.data.percent >= 10) { // 10%以下の場合は、円グラフに文字を描画しない
				return "(" + d.data.total + ")";
			}
		 })
		.style("font-size", "8pt")
		.attr("fill", function(d) { // 背景色が濃い場合は文字を白色にする
			return HINEMOS_COLLECT_CONST.CONST_COLOR_BLACK;
		});


};// end of createPie
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
Piegraph.prototype.removePoints = function(startdate, enddate, alldel) {
	// グラフと同様に処理しているので、DBからデータを前後多めに取得している
	// 表示範囲の期間を縮めただけの場合はremovePointsしか呼ばれないため、消す
	// 期間を増やした場合はremovePointsで要らない部分を削除しても
	// addで不要な情報が追加されるため、addでも要らない部分を消す
	for (var keyValue in piegraph) {
		var self = piegraph[keyValue];
		// グラフ画像保存のため期間をメンバに保持
		self.startdate = startdate;
		self.enddate = enddate;
		
		for (var idValue in self.piedata) {
			var dataid = self.piedata[idValue];
			var def_data = dataid.data;
//			self.piedata[idValue] = dataid;
			var thresholdinfomax = dataid.thresholdinfomax;
			var thresholdinfomin = dataid.thresholdinfomin;
			var thresholdwarnmax = dataid.thresholdwarnmax;
			var thresholdwarnmin = dataid.thresholdwarnmin;
			
			// 重複データの除去と期間外の除去
			self.piedata[idValue].data = def_data.filter(function (x, i, self) {
				var count = 0;
				for (count = 0; count < self.length; count++) {
					if (self[count][0] == x[0] && self[count][0] >= startdate && self[count][0] <= enddate && !alldel) {
						break;
					}
				}
				return count == i;
			});
		}
		var count_info = 0;
		var count_warn = 0;
		var count_critical = 0;
		// データの整理
		var mousedatas = [];
		for (var idValue in self.piedata) {
			var singlepie = self.piedata[idValue];
			var mousesingle = {};
			mousesingle.managername = singlepie.managername;
			mousesingle.facilityname = singlepie.facilityname;
			mousesingle.countinfo = 0;
			mousesingle.countwarn = 0;
			mousesingle.countcritical = 0;
			mousesingle.countunknown = 0;
			if (mousedatas[mousesingle.managername] == null) {
				mousedatas[mousesingle.managername] = [];
			}
				
			for (j = 0; j < self.piedata[idValue].data.length; j++) {
				var singledata = singlepie.data[j];
				// 値の取得と比較
				var value_y = Number(singledata[1]);
				if (isReallyNaN(value_y)) {
					mousesingle.countunknown++;
				} else if (thresholdinfomin <= value_y && value_y <= thresholdinfomax) {
					mousesingle.countinfo++;
				} else if (thresholdwarnmin <= value_y && value_y <= thresholdwarnmax) {
					mousesingle.countwarn++;
				} else {
					mousesingle.countcritical++;
				}
			}
			mousedatas[mousesingle.managername].push(mousesingle);
		}
		var data1 = ControlPiegraph.createPieData(keyValue, mousedatas);
		self.createPie(data1);
	}



};
//////////////////////////////////////////////////////////////
ControlPiegraph = {};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// 円グラフの要素情報の解析を行います。
ControlPiegraph.addPlotAtOnce = function (plotjson) {
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
			if (item.ishttpsce) {
			// http監視(シナリオ)の場合、重要度別の表示はしないためデータを空にする
				data_arr[groupid][id].data = [];
			}
		}
		for (var groupkey in data_arr) {
			var data_none = 0;
			var data = data_arr[groupkey];
			for (var id in data) {
				var def_data = null;
				if (piegraph[groupkey].piedata[id] == null) {
					piegraph[groupkey].piedata[id] = [];
					def_data = [];
				} else {
					def_data = piegraph[groupkey].piedata[id].data;
				}
				var dataid = data[id];
				if (dataid.collectid != "none") {
					data_none++;
				}
				piegraph[groupkey].piedata[id] = dataid;
				var startdate = dataid.startdate;
				var enddate = dataid.enddate;
				piegraph[groupkey].startdate = startdate;
				piegraph[groupkey].enddate = enddate;
				def_data = def_data.concat(dataid.data);
				
				// 重複データの除去と期間外の除去
				piegraph[groupkey].piedata[id].data = def_data.filter(function (x, i, self) {
					var count = 0;
					for (count = 0; count < self.length; count++) {
						if (self[count][0] == x[0] && self[count][0] >= startdate && self[count][0] <= enddate) {
							break;
						}
					}
					return count == i;
				});
			}// データの整理終わり
				
			// 表示データの整理
			var mousedatas = [];
			for (var piekey in piegraph[groupkey].piedata) {
				var singlepie = piegraph[groupkey].piedata[piekey];
				var mousesingle = {};
				mousesingle.managername = singlepie.managername;
				mousesingle.realmanagername = singlepie.realmanagername;//偽りの無いマネージャ名
				mousesingle.facilityid = singlepie.facilityid;
				mousesingle.realfacilityid = singlepie.realfacilityid;
				mousesingle.facilityname = singlepie.facilityname;
				mousesingle.countinfo = 0;
				mousesingle.countwarn = 0;
				mousesingle.countcritical = 0;
				mousesingle.countunknown = 0;
				if (mousedatas[mousesingle.managername] == null) {
					mousedatas[mousesingle.managername] = [];
				}
				var datelen = piegraph[groupkey].piedata[piekey].data.length;
				var thresholdinfomax = piegraph[groupkey].piedata[piekey].thresholdinfomax;
				var thresholdinfomin = piegraph[groupkey].piedata[piekey].thresholdinfomin;
				var thresholdwarnmax = piegraph[groupkey].piedata[piekey].thresholdwarnmax;
				var thresholdwarnmin = piegraph[groupkey].piedata[piekey].thresholdwarnmin;
				for (j = 0; j < datelen; j++) {
					var singledata = singlepie.data[j];
					// 値の取得と比較
					var value_y = Number(singledata[1]);
					if (isReallyNaN(value_y)) {
						mousesingle.countunknown++;
					} else if (thresholdinfomin <= value_y && value_y < thresholdinfomax) {
						mousesingle.countinfo++;
					} else if (thresholdwarnmin <= value_y && value_y < thresholdwarnmax) {
						mousesingle.countwarn++;
					} else {
						mousesingle.countcritical++;
					}
				}
				mousedatas[mousesingle.managername].push(mousesingle);
			}
			// データなしのものがあったらタイトルを更新する
			var nodestr = getGraphMessages("mess-nodes");
			var totalstr = "(" + getGraphMessages("mess-total");
			var graphsize = piegraph[groupkey].vis.select("#title_top").text();
			if (getGraphConfig("data-total-flg")) {
				if (graphsize.indexOf(nodestr) == -1) {
					if (data_none != Number(graphsize)) {
						// collectidが無いものが存在する
						// まとめて表示、かつ、collectidが取れなかったものがある場合はタイトルを変更する
							// 未書き換えの場合のみ
							var newtitle = data_none + nodestr + totalstr + graphsize + nodestr + ")";
							piegraph[groupkey].vis.select("#title_top text")
							.text(newtitle);
							// グラフタイトルをメンバに抑える
							piegraph[groupkey].title = newtitle;
					} else {
						if (graphsize.indexOf(nodestr) == -1) {
							// 未書き換えの場合のみ
							var newtitle = graphsize + nodestr;
							piegraph[groupkey].vis.select("#title_top text")
							.text(newtitle);
							// グラフタイトルをメンバに抑える
							piegraph[groupkey].title = newtitle;
						}
					}
				}
			}
			var data1 = ControlPiegraph.createPieData(groupkey, mousedatas);
			piegraph[groupkey].createPie(data1);
		}
	} catch (e) {
		var param = {};
		param.method_name = "javascript_error";
		param.exception = e;
		callJavaMethod(param);
	}
};// addPlotAtOnce
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// 円グラフの要素情報の解析を行います。
ControlPiegraph.createPieData = function (groupkey, mousedatas) {

	var self = piegraph[groupkey];
	var count_info = 0;
	var count_warn = 0;
	var count_critical = 0;
	var count_unknown = 0;
	var str = "<dl>";
	var str_info2 = "";
	var str_warn2 = "";
	var str_crit2 = "";
	var str_unknown2 = "";
	var str_none2 = "";
	var tooltipcomplete = false;
	for (var key in mousedatas) { // key:managername(dummy)
		var data = mousedatas[key];
		var str_info = "";
		var str_warn = "";
		var str_crit = "";
		var str_unknown = "";
		var str_none = "";
		var managername = "";
		for (var i = 0; i < data.length; i++) {
			count_info+=data[i].countinfo;
			count_warn+=data[i].countwarn;
			count_critical+=data[i].countcritical;
			count_unknown+=data[i].countunknown;
			if (!tooltipcomplete) {
				managername = data[i].realmanagername;
				var color = self.color(data[i].facilityid);
				str_info = str_info + "<dd style='border-bottom-color :" + color + ";border-left-color :" + color + ";word-wrap: break-word;'>" + data[i].facilityname + "(" + data[i].realfacilityid + ") : " + data[i].countinfo + "</dd>";
				str_warn = str_warn + "<dd style='border-bottom-color :" + color + ";border-left-color :" + color + ";word-wrap: break-word;'>" + data[i].facilityname + "(" + data[i].realfacilityid + ") : " + data[i].countwarn + "</dd>";
				str_crit = str_crit + "<dd style='border-bottom-color :" + color + ";border-left-color :" + color + ";word-wrap: break-word;'>" + data[i].facilityname + "(" + data[i].realfacilityid + ") : " + data[i].countcritical + "</dd>";
				str_unknown = str_unknown + "<dd style='border-bottom-color :" + color + ";border-left-color :" + color + ";word-wrap: break-word;'>" + data[i].facilityname + "(" + data[i].realfacilityid + ") : " + data[i].countunknown + "</dd>";
				str_none = str_none + "<dd style='border-bottom-color :" + color + ";border-left-color :" + color + ";word-wrap: break-word;'>" + data[i].facilityname + "(" + data[i].realfacilityid + ") : -</dd>";
				if (i >= HINEMOS_COLLECT_CONST.CONST_STACKED_TOOLTIP_COUNT - 1 && data.length > HINEMOS_COLLECT_CONST.CONST_STACKED_TOOLTIP_COUNT) {
					// 10件以上はツールチップは表示しない
					var omitted = "<dd style='border-bottom-style:dashed;border-bottom-color :#ffffff;border-left-color :#ffffff;'>omitted below</dd>";
					str_info = str_info + omitted;
					str_warn = str_warn + omitted;
					str_crit = str_crit + omitted;
					str_unknown = str_unknown + omitted;
					str_none = str_none + omitted;
					tooltipcomplete = true;
				}
			}
		}
		if (str_info != "" && str_warn != "" && str_crit != "" && str_unknown != "" && str_none != "") {
			str_info2 += "<dt style='word-wrap: break-word;'>" + managername + "</dt>" + str_info;
			str_warn2 += "<dt style='word-wrap: break-word;'>" + managername + "</dt>" + str_warn;
			str_crit2 += "<dt style='word-wrap: break-word;'>" + managername + "</dt>" + str_crit;
			str_unknown2 += "<dt style='word-wrap: break-word;'>" + managername + "</dt>" + str_unknown;
			str_none2 +=  "<dt style='word-wrap: break-word;'>" + managername + "</dt>" + str_none;
		}
	}
	str_info2 = "<dl>" + str_info2 + "</dl>";
	str_warn2 = "<dl>" + str_warn2 + "</dl>";
	str_crit2 = "<dl>" + str_crit2 + "</dl>";
	str_unknown2 = "<dl>" + str_unknown2 + "</dl>";
	str_none2 = "<dl>" + str_none2 + "</dl>";

	var h3tag1 = "<h3 class='piechart' style='border-bottom-color :PERT_COLOR;border-left-color :PERT_COLOR'>";
	var h3tag2 = "</h3><div style='padding-left:20px;'>";
	var counttotal = count_info + count_warn + count_critical + count_unknown;
	var data1 = [];
	if (count_info != 0) {
		var single_info = {};
		single_info.total = count_info;
		single_info.priority = getGraphMessages("mess-information");
		single_info.percent = Math.round(count_info/counttotal*100);
		single_info.label = single_info.priority + "[" + single_info.percent + "%]";
		single_info.color = HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_INFO;
		var toolstr = h3tag1.replace( /PERT_COLOR/g , HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_INFO);
		single_info.tooltip = toolstr + single_info.priority + " : " + single_info.percent + "%" + h3tag2 + str_info2 + "</div>";
		data1.push(single_info);
	}
	if (count_warn != 0) {
		var single_warn = {};
		single_warn.total = count_warn;
		single_warn.priority = getGraphMessages("mess-warning");
		single_warn.percent = Math.round(count_warn/counttotal*100);
		single_warn.label = single_warn.priority + "[" + single_warn.percent + "%]";
		single_warn.color = HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_WARN;
		var toolstr = h3tag1.replace( /PERT_COLOR/g , HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_WARN);
		single_warn.tooltip = toolstr + single_warn.priority + " : " + single_warn.percent + "%" + h3tag2 + str_warn2 + "</div>";
		data1.push(single_warn);
	}
	if (count_critical != 0) {
		var single_cri = {};
		single_cri.total = count_critical;
		single_cri.priority = getGraphMessages("mess-critical");
		single_cri.percent = Math.round(count_critical/counttotal*100);
		single_cri.label = single_cri.priority + "[" + single_cri.percent + "%]";
		single_cri.color = HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_CRITICAL;
		var toolstr = h3tag1.replace( /PERT_COLOR/g , HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_CRITICAL);
		single_cri.tooltip = toolstr + single_cri.priority + " : " + single_cri.percent + "%" + h3tag2 + str_crit2 + "</div>";
		data1.push(single_cri);
	}
	if (count_unknown != 0) {
		var single_unknown = {};
		single_unknown.total = count_unknown;
		single_unknown.priority = getGraphMessages("mess-unknown");
		single_unknown.percent = Math.round(count_unknown/counttotal*100);
		single_unknown.label = single_unknown.priority + "[" + single_unknown.percent + "%]";
		single_unknown.color = HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_UNKNOWN;
		var toolstr = h3tag1.replace( /PERT_COLOR/g , HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_UNKNOWN);
		single_unknown.tooltip = toolstr + single_unknown.priority + " : " + single_unknown.percent + "%" + h3tag2 + str_unknown2 + "</div>";
		data1.push(single_unknown);
	}
	if (counttotal == 0) {
		var single_none = {};
		single_none.total = 1;
		single_none.percent = 100;// percentが10以下だと文字が表示されないので100を入れておく
		single_none.label = "NO DATA";
		single_none.color = HINEMOS_COLLECT_CONST.CONST_COLOR_GRAY;
		single_none.tooltip = str_none2;
		data1.push(single_none);
	}
	
	// 文字が重なるのでソートする
	data1.sort(
	function(a, b) {
		if (a.total < b.total) { return 1 };
		if (a.total > b.total) { return -1 };
		return 0
	});

	
	
	return data1;
};// createPieData
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// chart_blockの子要素をすべて削除する
// また、保持しているグラフ情報も削除する
ControlPiegraph.delDiv = function () {
	try {
		var aNode = document.getElementById("chart_block");
		for (var i =aNode.childNodes.length-1; i>=0; i--) {
			aNode.removeChild(aNode.childNodes[i]);
		}
		piegraph = null;
		piegraph = [];
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
}; // end of delDiv
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// グラフ1つ専用のdivを作成する
// 親のdiv(itemName別)のサイズも変更する
ControlPiegraph.addDiv = function (parentid, name) {
	try {
		var div_child = document.createElement('div');
		div_child.id = name;//total:monitorId, single:facilityId_managerName_monitorId
		div_child.classList.add("chart");
		div_child.style.width = HINEMOS_COLLECT_CONST.CONST_PIEGRAPH_WIDTH + "px";
		div_child.style.height = HINEMOS_COLLECT_CONST.CONST_PIEGRAPH_HEIGHT + "px";
		div_child.style.margin = HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN + "px";
		div_child.style.backgroundColor = "yellow";
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
//			div_parent.style.background="#ffffff";
		}

		// 一番前にadd
		div_parent.appendChild(div_child);
		if (!getGraphConfig("data-return-flg")) {
			// 折り返しフラグがfalseの場合は、横幅を広げていく
			var childnum = div_parent.children.length * (HINEMOS_COLLECT_CONST.CONST_PIEGRAPH_WIDTH + HINEMOS_COLLECT_CONST.CONST_GRAPH_MARGIN*2 +HINEMOS_COLLECT_CONST.CONST_GRAPH_BORDER_WIDTH*2);
			// 横幅の調整、グラフの幅と左右のmarginと左右のborderの合計になる
			div_parent.style.width = childnum + 'px';
		}
	} catch (e) {
		var param = {};
		param.method_name = "javascript_error";
		param.exception = e;
		callJavaMethod(param);
	}
}; // end of addDiv
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// グラフのベースを一気に追加します
ControlPiegraph.addGraphAtOnce = function (plotjson, graphSize, preferenceSize, settinginfo) {
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
			ControlPiegraph.addDiv(parentid, id);
			graphcount = graphcount - graphSize;
			if (graphcount < 0) {
				graphSize = graphSize + graphcount;
			}
			piegraph[id] = new Piegraph(id, item, graphSize);
		}
	} catch (e) {
		var param = {};
		param.method_name = "javascript_error";
		param.exception = e;
		callJavaMethod(param);
	}
}; // end of addGraphAtOnce
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
ControlPiegraph.trimBranch = function() {
	// nop
}; // end of trimBranch
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
ControlPiegraph.trimXAxis = function(xaxis_min, xaxis_max, targetid) {
	// nop
}; // end of trimXAxis


