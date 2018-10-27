/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 *
 * 性能[グラフ]のスライダーを描画するJavaScriptです。
 */
var BrushLine = [];
var ControlBrushLine = [];
var msec_day = 24 * 60 * 60 * 1000;
var msec_week = msec_day * 7;
var msec_month = msec_day * 31;
var msec_year = msec_day * 365;
var msec_10year = msec_year * 10;
var msec_none = -1;

// 同じものを利用したいためグローバル変数化
BrushLine = function(start_date, end_date, select_start_date, select_end_date, timezoneoffset, graphsize) {
var self = this;
var margin = {top: 0, right: 20, bottom: 20, left: 20},
	width = 650 - margin.left - margin.right,
	height = 40 - margin.top - margin.bottom;
var x = d3.time.scale()
	.domain([new Date(start_date), new Date(end_date)])
	.range([0, width]);

var tooltip = d3.select("body").select("#slider_tooltip");

this.timezoneoffset = timezoneoffset;
this.startdate = start_date;
this.enddate = end_date;
this.selectstartdate = select_start_date;
this.selectenddate = select_end_date;
this.graphsize = graphsize;

this.brush = d3.svg.brush()
	.x(x)
	.extent([new Date(select_start_date), new Date(select_end_date)])
	.on("brushend", function() {// イベント登録
		var startdate = (self.brush.extent()[0]).getTime();// 選択範囲
		var enddate = (self.brush.extent()[1]).getTime();
		if (startdate == enddate) { // スクロールバーのように動かす
			var time_diff = self.selectenddate - self.selectstartdate;// 選択範囲の差分
			var newstartdate = self.startdate;// 全体の範囲start
			var newenddate = self.enddate;// 全体の範囲end
			var newselectstart;
			var newselectend;
			if (startdate < self.selectstartdate && startdate < self.selectenddate) {
				// 前側にずれる
				newselectstart = self.selectstartdate - time_diff;
				newselectend = self.selectstartdate;
				startdate = newselectstart;// 未来の選択範囲start
				enddate = newselectend;
				if (startdate < newstartdate) {
					// 前側にずれる行き止まりの場合は、範囲内で一番最古をstartにする
					startdate = newstartdate;
					enddate = startdate + time_diff;
				}
				
			} else {
				// 後ろ側にずれる
				newselectstart = self.selectenddate;
				newselectend = self.selectenddate + time_diff;
				startdate = newselectstart;// 未来の選択範囲start
				enddate = newselectend;
				if (newselectend > newenddate) {
					// 後ろ側にずれる行き止まりの場合は、範囲内で一番最新をendにする
					enddate = newenddate;
					startdate = enddate - time_diff;
				}
			}
			// 全体の範囲start、end、選択の範囲start、選択の範囲end
			ControlBrushLine.delCreateBrush(newstartdate, newenddate, startdate, enddate, self.graphsize);
		}
		
		var param = {};
		param.method_name = "brushend";
		param.xaxis_min = startdate;
		param.xaxis_max = enddate;
		// スライダー連続操作抑止のため、スライダーの上にdivを乗せて操作不可にする
		ControlBrushLine.setLoadingVisibility("visible");
		setTimeout(function() {
			// 指定ms後にスライダーの上のdivを消す
			ControlBrushLine.setLoadingVisibility("hidden");
			}, ControlBrushLine.getTimeoutValue());

		callJavaMethod(param);
		tooltip.style("visibility", "hidden");
		
		// 選択期間をメンバに抑える
		self.selectstartdate = startdate;
		self.selectenddate = enddate;
		// 現在選択中の期間を文字列で表示する
		ControlBrushLine.applySelectInfo(new Date(startdate), new Date(enddate));
	}) // end of スライド終了イベント
	.on("brush", function() {
		var pagey = d3.event.sourceEvent.pageY;
		var pagex = d3.event.sourceEvent.pageX;
		// chromeとfirefoxはd3.event.pageXで取れる、IE10、11も取れるがIE10だと座標が若干ずれている
		// そのため、IE10の場合はif文で値を取得しなおす
		// firefoxでは[event is not defined.]になるため、ブラウザチェックする
		if (!checkBrowserKind("firefox") && (event.pageY == undefined || event.pageX == undefined)) { // IE9、10でのevent.pageX、event.pageYが取れない対策
			pagex = event.clientX + (document.body.scrollLeft || document.documentElement.scrollLeft);
			pagey = event.clientY + (document.body.scrollTop || document.documentElement.scrollTop);
		}
		var parseDate = d3.time.format('%Y/%m/%d %H:%M:%S');
		tooltip.style("visibility", "visible").text(parseDate(self.brush.extent()[0]) + " - " + parseDate(self.brush.extent()[1]));
		tooltip.style("top", (pagey-15)+"px").style("left",(pagex+10)+"px");
		return;
	}); // end of スライド操作イベント

var arc = d3.svg.arc()// arc:弧
	.outerRadius(height / 2)//外側の半径
	.startAngle(0)// アークが始まる角度
	.endAngle(function(d, i) { return i ? -Math.PI : Math.PI; });// アークが終わる角度

var widthh = width + margin.left + margin.right;
var heigtt = height + margin.top + margin.bottom;
var svg = d3.select("#brush_child")
	.append("svg")
	.attr("width", widthh)
	.attr("height", heigtt)
	.append("g")
	.attr("transform", "translate(" + margin.left + "," + margin.top + ")");

// ticksの算出
var date_diff = end_date - start_date;
var ticks = 10;
switch (date_diff) {
case msec_day :
	ticks = 8;
	break;
case msec_week :
	ticks = 7;
	break;
case msec_month:
	ticks = 6;
	break;
case msec_year:
	ticks = 6;
	break;
default:
	ticks = 10;
	break;
}

svg.append("g")
	.attr("class", "x axis")
	.attr("transform", "translate(0," + height + ")")
	.call(d3.svg.axis().scale(x).orient("bottom").ticks(ticks).tickFormat(d3_time_scaleLocalFormat));

self.brushg = svg.append("g")
	.attr("class", "brush")
	.call(this.brush);

self.brushg.selectAll(".resize").append("path")
	.attr("transform", "translate(0," + height / 2 + ")")
	.attr("d", arc);

self.brushg.selectAll("rect")
	.attr("height", height);

// 現在選択中の期間を文字列で表示する
ControlBrushLine.applySelectInfo(new Date(select_start_date), new Date(select_end_date));

};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// Brushの作成
ControlBrushLine.delCreateBrush = function(start_date, end_date, select_start_date, select_end_date, totalGraphSize) {
	document.getElementById('all_brush').style.visibility = "visible";
	var dom_obj=document.getElementById('brush_child');
	var dom_obj_parent=dom_obj.parentNode;
	dom_obj_parent.removeChild(dom_obj);

	var div_child = document.createElement('div');
	div_child.id = "brush_child";
	
	var div_block = document.getElementById('brush');
	// 最後尾にadd
	div_block.appendChild(div_child);
	brushobj = new BrushLine(Number(start_date), Number(end_date), Number(select_start_date), Number(select_end_date), 0, totalGraphSize);
	
	// スライダー連続操作抑止用のdivを消す(保険)
	ControlBrushLine.setLoadingVisibility("hidden");

	// ラジオボタンにチェックする
	ControlBrushLine.initRadio(Number(end_date) - Number(start_date));
	
	var beforebutton = document.getElementById("beforesvg");
	if (beforebutton == null) {
		// ボタンを生成する
		d3.select("#before_button")
		.append("svg")
		.attr("id", "beforesvg")
		.attr("width", "30px")
		.attr("height", "30px")
		.style("margin", "5px")
		.append("polygon")
			.attr("points", "0,7 8,0 8,14")
			.attr("fill", "#000066");
		d3.select("#beforesvg")
		.append("polygon")
			.attr("points", "6,7 14,0 14,14 ")
			.attr("fill", "#000066")
			.attr("fill", "#000066");
		d3.select("#before_button")
		.on("mouseover", function() {
			var parseDate = d3.time.format('%Y/%m/%d %H:%M:%S');
			var tooltip = d3.select("body").select("#slider_tooltip");
			var ret = ControlBrushLine.getPeriod(true);
			var pagey = d3.event.pageY;
			var pagex = d3.event.pageX;
			// chromeとfirefoxはd3.event.pageXで取れる、IE10、11も取れるがIE10だと座標が若干ずれている
			// そのため、IE10の場合はif文で値を取得しなおす
			// firefoxでは[event is not defined.]になるため、ブラウザチェックする
			if (!checkBrowserKind("firefox") && (event.pageY == undefined || event.pageX == undefined)) { // IE9、10でのevent.pageX、event.pageYが取れない対策
				pagex = event.clientX + (document.body.scrollLeft || document.documentElement.scrollLeft);
				pagey = event.clientY + (document.body.scrollTop || document.documentElement.scrollTop);
			}
			tooltip.style("visibility", "visible").text("change to :" + parseDate(new Date(ret.new_period_start)) + " - " + parseDate(new Date(ret.new_period_end)));
			tooltip.style("top", (pagey-15)+"px").style("left",(pagex+10)+"px");
			return;
		})
		.on("mouseout", function() { 
			var tooltip = d3.select("body").select("#slider_tooltip");
				tooltip.style("visibility", "hidden");
		})
		.on("mousedown", function() { d3.event.stopPropagation();})
		.on("mouseup", function() { d3.event.stopPropagation();})
		.on("click", function(d) {
			ControlBrushLine.changePeriod(true);
		});
	}

	var afterbutton = document.getElementById("aftersvg");
	if (afterbutton == null) {
		// ボタンを生成する
		d3.select("#after_button")
		.append("svg")
		.attr("id", "aftersvg")
		.attr("width", "30px")
		.attr("height", "30px")
		.style("margin", "6px")
		.append("polygon")
			.attr("points", "0,0 8,7 0,14 ")
			.attr("fill", "#000066");
		d3.select("#aftersvg")
		.append("polygon")
			.attr("points", "6,0 14,7 6,14 ")
			.attr("fill", "#000066");
		d3.select("#after_button")
		.on("mouseover", function() {
			var parseDate = d3.time.format('%Y/%m/%d %H:%M:%S');
			var tooltip = d3.select("body").select("#slider_tooltip");
			var ret = ControlBrushLine.getPeriod(false);
			var pagey = d3.event.pageY;
			var pagex = d3.event.pageX;
			// chromeとfirefoxはd3.event.pageXで取れる、IE10、11も取れるがIE10だと座標が若干ずれている
			// そのため、IE10の場合はif文で値を取得しなおす
			// firefoxでは[event is not defined.]になるため、ブラウザチェックする
			if (!checkBrowserKind("firefox") && (event.pageY == undefined || event.pageX == undefined)) { // IE9、10でのevent.pageX、event.pageYが取れない対策
				pagex = event.clientX + (document.body.scrollLeft || document.documentElement.scrollLeft);
				pagey = event.clientY + (document.body.scrollTop || document.documentElement.scrollTop);
			}
			tooltip.style("visibility", "visible").text("change to :" + parseDate(new Date(ret.new_period_start)) + " - " + parseDate(new Date(ret.new_period_end)));
			tooltip.style("top", (pagey)+"px").style("left",(pagex-350)+"px");
			return;
		})
		.on("mouseout", function() { 
			var tooltip = d3.select("body").select("#slider_tooltip");
				tooltip.style("visibility", "hidden");
		})
		.on("mousedown", function() { d3.event.stopPropagation();})
		.on("mouseup", function() { d3.event.stopPropagation();})
		.on("click", function(d) {
			ControlBrushLine.changePeriod(false);
		});
	}

}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// Brushの非表示
ControlBrushLine.delBrush = function() {
	document.getElementById('all_brush').style.visibility = "hidden";
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// 選択範囲の変更
BrushLine.prototype.moveTarget = function(select_start_date, select_end_date) {
	var self = brushobj;
	var startdate = self.brush.x().domain()[0].getTime();//全体の範囲
	var enddate = self.brush.x().domain()[1].getTime();
	// 表示中のスライダー外になる場合は、表示を変更しない
	if (startdate < select_start_date && select_end_date < enddate) {
		self.brushg.transition()
		.duration(HINEMOS_COLLECT_CONST.CONST_ANIMATION_INTERVAL)
		.tween("brush", function() {
			var i = d3.interpolate(self.brush.extent(), [new Date(select_start_date), new Date(select_end_date)]);
			// 選択期間をメンバに抑える
			self.selectstartdate = select_start_date;
			self.selectenddate = select_end_date;
			// 現在選択中の期間を文字列で表示する
			ControlBrushLine.applySelectInfo(new Date(select_start_date), new Date(select_end_date));
			return function(t) { self.brushg.call(self.brush.extent(i(t)));};
		});
	} else {
		// スライダーの期間は必ず1日or1週間or1ヶ月or1年or10年のどれかに当てはまらせる
		// 両サイドいっぺんに超えることはない、縮小時に制限しているため
		var diff = select_end_date - select_start_date;
		var centerdate = select_end_date - Math.floor(diff/2);
		var term = msec_10year;
		if (diff >= msec_10year) term = msec_10year;
		if (msec_year <= diff && diff < msec_10year) term = msec_10year;
		if (msec_month <= diff && diff < msec_year) term = msec_year;
		if (msec_week <= diff && diff < msec_month) term = msec_month;
		if (msec_day <= diff && diff < msec_week) term = msec_week;
		if (diff < msec_day) term = msec_day;
		var newstartdate = centerdate - Math.floor(term / 2);// 新たな範囲start
		var newenddate = centerdate + Math.floor(term / 2);// 新たな範囲end
		// java側に、スライダーの表示期間タイプを通知
		var param1 = {};
		param1.method_name = "noticeSliderType";
		param1.start_date = newstartdate;
		param1.end_date = newenddate;
		callJavaMethod(param1);
		ControlBrushLine.delCreateBrush(newstartdate, newenddate, select_start_date, select_end_date, self.graphsize);
		
		ControlBrushLine.initRadio(term);// 選択の変更
		// 選択の通知
	}
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// スライダーの期間ボタン(10years、yearなど)を押したときに呼ばれます。
//スライダー全体の期間を変更します。
ControlBrushLine.changeSlider = function() {
	var self = brushobj;
	var msec_none = -1;

	var radiolist = document.getElementsByName("radio_term");
	var term_type = msec_none;
	for (var i = 0; i < radiolist.length; i++) {
		if (radiolist[i].checked) {
			var term_value = radiolist[i].value;
			if (term_value == '10year') {
				term_type = msec_10year;
			} else if (term_value == 'year') {
				term_type = msec_year;
			} else if (term_value == 'month') {
				term_type = msec_month;
			} else if (term_value == 'week') {
				term_type = msec_week;
			} else if (term_value == 'day') {
				term_type = msec_day;
			} else {
				term_type = msec_none;
			}
			break;
		}
	}
	var select_startdate = self.brush.extent()[0];
	if (select_startdate instanceof Date) {
		select_startdate = select_startdate.getTime();
	}
	var select_enddate = self.brush.extent()[1];
	if (select_enddate instanceof Date) {
		select_enddate = select_enddate.getTime();
	}
	var startdate = self.brush.x().domain()[0].getTime();
	var enddate = self.brush.x().domain()[1].getTime();

	// 選択期間を中央にして表示範囲を変更する
	var center_date = select_startdate + (select_enddate - select_startdate) / 2;
	var new_startdate = center_date - (term_type / 2);
	var new_enddate = center_date + (term_type / 2);
	var timeout = ControlBrushLine.getTimeoutValue() / 2;
	var param1 = {};
	if (new_startdate > select_startdate || new_enddate < select_enddate) {
		// 期間選択ボタンを押下したときに、表示中の範囲よりも期間選択ボタンが狭い場合は、右端を基準に押下した期間に狭める
		new_enddate = select_enddate;
		startdate = select_enddate - term_type;
		new_startdate = startdate;
		select_startdate = startdate;
		// java側に、スライダーの表示期間タイプを通知
		param1.method_name = "noticeSlider_brushend";
		param1.xaxis_min = select_startdate;
		param1.xaxis_max = select_enddate;
		param1.test = "sikibetsu";
	} else {
		// java側に、スライダーの表示期間タイプを通知
		param1.method_name = "noticeSliderType";
		param1.test = "sikibetsu2";
	}
	param1.start_date = new_startdate.toFixed(0);
	param1.end_date = new_enddate.toFixed(0);

	// スライダー連続操作抑止のため、スライダーの上にdivを乗せて操作不可にする
	ControlBrushLine.setLoadingVisibility("visible");
	setTimeout(function() {
		// 指定ms後にスライダーの上のdivを消す
		ControlBrushLine.setLoadingVisibility("hidden");
		ControlBrushLine.delCreateBrush(new_startdate, new_enddate, select_startdate, select_enddate, self.graphsize);
		}, timeout);

	callJavaMethod(param1);
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// 引数で指定された期間に一致するラジオボタンを選択状態にします。
// 初期表示時や、選択範囲が広すぎる場合に使用
ControlBrushLine.initRadio = function(term) {
	var date_new = "";

	switch (term) {
	case msec_day :
		date_new = "day";
		break;
	case msec_week :
		date_new = "week";
		break;
	case msec_month:
		date_new = "month";
		break;
	case msec_year:
		date_new = "year";
		break;
	case msec_10year:
		date_new = "10year";
		break;
	default:
		date_new = "";
		break;
	}

	var radiolist = document.getElementsByName("radio_term");
	var term_type = msec_none;
	var i;
	for (i = 0; i < radiolist.length; i++) {
		if (radiolist[i].value == date_new) {
			break;
		}
	}
	document.getElementsByName("radio_term")[i].checked = true;
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// 選択中期間の文字列表示を更新します
// startdateとenddateはDate型でくるべきです
ControlBrushLine.applySelectInfo = function(startdate, enddate) {
	
	var parseDate = d3.time.format('%Y/%m/%d %H:%M:%S.%L');
	var selectinfo_start = d3.select("body").select("#selectinfo_start");
	var selectinfo_end = d3.select("body").select("#selectinfo_end");
	document.getElementById("selectinfo_start").value = parseDate(startdate);
	document.getElementById("selectinfo_end").value = parseDate(enddate);
	// テキストインプットの背景色を白にも戻す
	document.getElementById("selectinfo_start").style.backgroundColor = "white";
	document.getElementById("selectinfo_end").style.backgroundColor = "white";
	
	// 開始時刻のテキストインプットenter押下で表示範囲を変更する
	selectinfo_start.on("keyup", function(d) {
		call();
	});
	// 終了時刻のテキストインプットenter押下で表示範囲を変更する
	selectinfo_end.on("keyup", function(d) {
		call();
	});
	function call() {
		if (d3.event.keyCode == 13) { // enter_key(keycode=13) pressed
			var datelong_start = brushobj.selectstartdate;
			var datelong_end = brushobj.selectenddate;
			var startstr = document.getElementById("selectinfo_start").value;
			var endstr = document.getElementById("selectinfo_end").value;
			// テキストインプットの背景色を白にも戻す
			document.getElementById("selectinfo_start").style.backgroundColor = "white";
			document.getElementById("selectinfo_end").style.backgroundColor = "white";

			var datecheck_start = parseDate.parse(startstr);
			// 指定日時のフォーマットチェック
			if (isReallyNaN(datecheck_start) || datecheck_start == null) {
				alert("Please specify Time(Start) in the correct format.\n[" + startstr + "]");
				document.getElementById("selectinfo_start").focus();
				document.getElementById("selectinfo_start").style.backgroundColor = "pink";
				return;
			}
			datelong_start = datecheck_start.getTime();
			var datecheck_end = parseDate.parse(endstr);
			if (isReallyNaN(datecheck_end) || datecheck_end == null) {
				alert("Please specify Time(End) in the correct format.\n[" + endstr + "]");
				document.getElementById("selectinfo_end").focus();
				document.getElementById("selectinfo_end").style.backgroundColor = "pink";
				return;
			}
			datelong_end = datecheck_end.getTime();
			
			// 開始時刻と終了時刻のエラーチェック
			if (datelong_start >= datelong_end) {
				document.getElementById("selectinfo_start").style.backgroundColor = "pink";
				document.getElementById("selectinfo_end").style.backgroundColor = "pink";
				// 指定日時の整合性
				alert("Please input the date and time after Time(End) into Time(Start).\n[" + startstr + " - " + endstr + "]");
				return;
			} else if (datelong_end - datelong_start < 60 * 1000) {
				document.getElementById("selectinfo_start").style.backgroundColor = "pink";
				document.getElementById("selectinfo_end").style.backgroundColor = "pink";
				// 1min以下の指定は無効にするPlease specify at least one minute.
				alert("Please specify at least one minute.\n[" + startstr + " - " + endstr + "]");
				return;
			} else if (datelong_end - datelong_start > msec_10year) {
				document.getElementById("selectinfo_start").style.backgroundColor = "pink";
				document.getElementById("selectinfo_end").style.backgroundColor = "pink";
				// 10年以上の指定は無効にするPlease specify at least one minute.
				alert("Please specify at under 10 years.\n[" + startstr + " - " + endstr + "]");
				return;
			}
			// enterkey連打防止のためにフォーカスをはずす
			document.getElementById("selectinfo_start").blur();
			document.getElementById("selectinfo_end").blur();
			
			// テキストインプットの背景色を白にも戻す
			document.getElementById("selectinfo_start").style.backgroundColor = "white";
			document.getElementById("selectinfo_end").style.backgroundColor = "white";

			// スライダーの表示範囲を変更する
			brushobj.moveTarget(datelong_start, datelong_end);

			// スライダー連続操作抑止のため、スライダーの上にdivを乗せて操作不可にする
			ControlBrushLine.setLoadingVisibility("visible");
			setTimeout(function() {
				// 指定ms後にスライダーの上のdivを消す
				ControlBrushLine.setLoadingVisibility("hidden");
				}, ControlBrushLine.getTimeoutValue());

			var param = {};
			param.method_name = "inputdate";
			param.xaxis_min = datelong_start;
			param.xaxis_max = datelong_end;
			param.test = "fromslider";
			callJavaMethod(param);
		}
	};
};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// スライダーの"<<"または">>"が押されたときに呼ばれます。(引数=trueが"<<"、falseが">>")
// スライダーの期間と表示期間を全体的に変更します。
ControlBrushLine.changePeriod = function(directionflg) {
	var self = brushobj;
	// 選択範囲
	var startdate = self.brush.extent()[0];
	if (startdate instanceof Date) {
		startdate = startdate.getTime();
	}
	var enddate = self.brush.extent()[1];
	if (enddate instanceof Date) {
		enddate = enddate.getTime();
	}
	var parseDate = d3.time.format('%Y/%m/%d %H:%M:%S.%L');
	console.log(parseDate(new Date(startdate)) + ", " + parseDate(new Date(enddate)) + ", " + parseDate(new Date(self.selectstartdate)) + ", " + parseDate(new Date(self.selectenddate)));
	if (startdate != self.selectstartdate || enddate != enddate) {
		// スライダーの時刻と、グラフの時刻が異なる場合は処理を行わない
		// スライダーを操作しながら"<<"または">>"を押下した場合は処理を行わない
		return;
	}
	
	var ret = ControlBrushLine.getPeriod(directionflg);
	ControlBrushLine.delCreateBrush(ret.new_period_start, ret.new_period_end, ret.new_select_start, ret.new_select_end, self.graphsize);
	// スライダー連続操作抑止のため、スライダーの上にdivを乗せて操作不可にする
	ControlBrushLine.setLoadingVisibility("visible");
	setTimeout(function() {
		// 指定ms後にスライダーの上のdivを消す
		ControlBrushLine.setLoadingVisibility("hidden");
		}, ControlBrushLine.getTimeoutValue());

	var param1 = {};
	param1.method_name = "inputdate";
	param1.xaxis_min = ret.new_select_start;
	param1.xaxis_max = ret.new_select_end;
	param1.test = "changePeriod";
	callJavaMethod(param1);

	return;
};
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// 期間変更ボタン押下時に、新たなスライダーの期間と表示期間を返します。
// directionflg : trueは前戻し、falseは後ろ戻し
// ret.new_select_start : 新しい選択期間開始
// ret.new_select_end   : 新しい選択期間終了
// ret.new_period_start : 新しいスライダー開始
// ret.new_period_end   : 新しいスライダー終了
ControlBrushLine.getPeriod = function(directionflg) {
	var self = brushobj;

	var select_startdate = self.brush.extent()[0];
	if (select_startdate instanceof Date) {
		select_startdate = select_startdate.getTime();
	}
	var select_enddate = self.brush.extent()[1];
	if (select_enddate instanceof Date) {
		select_enddate = select_enddate.getTime();
	}
	var startdate = self.brush.x().domain()[0].getTime();
	var enddate = self.brush.x().domain()[1].getTime();

	// スライダーの差分
	var diff_period = enddate - startdate;
	var new_period_start;
	if (directionflg) {
		new_period_start = startdate - diff_period;
	} else {
		new_period_start = startdate + diff_period;
	}
	var new_period_end;
	if (directionflg) {
		new_period_end = enddate - diff_period;
	} else {
		new_period_end = enddate + diff_period;
	}
	// 選択部分の差分
	var new_select_start;
	if (directionflg) {
		new_select_start = select_startdate - diff_period;
	} else {
		new_select_start = select_startdate + diff_period;
	}
	var new_select_end;
	if (directionflg) {
		new_select_end = select_enddate - diff_period;
	} else {
		new_select_end = select_enddate + diff_period;
	}
/*
	console.log("select_startdate" + new Date(select_startdate));
	console.log("select_enddate" + new Date(select_enddate));
	console.log("startdate" + new Date(startdate));
	console.log("enddate" + new Date(enddate));
	console.log("new_period_start" + new Date(new_period_start));
	console.log("new_period_end" + new Date(new_period_end));
	console.log("new_select_start" + new Date(new_select_start));
	console.log("new_select_end" + new Date(new_select_end));
*/
	var ret = {};
	ret.new_select_start = new_select_start;
	ret.new_select_end = new_select_end;
	ret.new_period_start = new_period_start;
	ret.new_period_end = new_period_end;
	
	return ret;
}
// スライダー連続操作抑止をする時間を返します
// 表示しているグラフの数によって時間を変えます
// 抑止する操作は以下の場合
// ・スライダーの期間ボタン(10years、yearなど)を押したとき
// ・テキスト入力で表示期間を変更したとき
// ・スライダーの"<<"または">>"が押されたとき
// ・スライダー操作で期間を変更したとき(始点or終点の変更)
ControlBrushLine.getTimeoutValue = function() {
	if (brushobj.graphsize < 10) {
		return 1500;
	} else if (brushobj.graphsize >= 10 && brushobj.graphsize < 20) {
		return 2500;
	} else if (brushobj.graphsize >= 20 && brushobj.graphsize < 30) {
		return 3500;
	} else if (brushobj.graphsize >= 30 && brushobj.graphsize < 40) {
		return 4500;
	} else if (brushobj.graphsize >= 40 && brushobj.graphsize < 50) {
		return 5500;
	} else if (brushobj.graphsize >= 50 && brushobj.graphsize < 60) {
		return 6500;
	} else if (brushobj.graphsize >= 60 && brushobj.graphsize < 70) {
		return 7500;
	} else if (brushobj.graphsize >= 70 && brushobj.graphsize < 80) {
		return 8500;
	} else if (brushobj.graphsize >= 80 && brushobj.graphsize < 90) {
		return 9500;
	} else if (brushobj.graphsize >= 90 && brushobj.graphsize < 100) {
		return 10500;
	} else if (brushobj.graphsize >= 100) {
		return 11500;
	}
}
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// スライダーの操作不可・可を制御します。
// 引数で指定されてたスタイルで#loadingのvisibilityを変更します。
ControlBrushLine.setLoadingVisibility = function(style_type) {
	d3.select("body").select("#loading").style("visibility", style_type);
	
	// スライダーの操作抑止と同時に期間ボタンの抑止も行う
	var checktype = true;
	if (style_type == "hidden") {
		checktype = false;
	}
	document.getElementById("on_10year").disabled = checktype;
	document.getElementById("on_year").disabled = checktype;
	document.getElementById("on_month").disabled = checktype;
	document.getElementById("on_week").disabled = checktype;
	document.getElementById("on_day").disabled = checktype;
};
