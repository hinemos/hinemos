/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 *
 *性能[グラフ]の[上限・下限表示]を描画するJavaScriptです。
 */
ThresholdGraph = function(target, thresholdinfo) {
var self = this;

this.elementid = target.innerid;
this.hh = target.height;//svgの高さ

this.width = target.width,//すべてのオブジェクトの幅
this.dragbarw = HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_OPERATION_WIDTH;// 透過されている握る部分

// 擬似インプット
this.info_min = thresholdinfo.info_min;// green
this.info_max = thresholdinfo.info_max;
this.warn_min = thresholdinfo.warn_min;// yellow
this.warn_max = thresholdinfo.warn_max;

this.managername = thresholdinfo.managername;
this.itemname = thresholdinfo.itemname;
this.monitorid = thresholdinfo.monitorid;
this.pluginid = thresholdinfo.pluginid;

this.infoheight = target.y(this.info_min) - target.y(this.info_max),// 緑部分の幅
this.warnheight = target.y(this.warn_min) - target.y(this.warn_max);// 黄色部分の幅

// 緑部分上部のイベント登録
this.infotop = d3.behavior.drag()
	.origin(Object)
	.on("dragstart", function() { d3.event.sourceEvent.stopPropagation(); })
	.on("dragend", self.dragendOperationBar())
	.on("drag", this.tdragresize());

// 緑部分下部のイベント登録
this.infobottom = d3.behavior.drag()
	.on("dragstart", function() { d3.event.sourceEvent.stopPropagation(); })
	.origin(Object)
	.on("dragend", self.dragendOperationBar())
	.on("drag", this.bdragresize());

// 黄色部分上部のイベント登録
this.warntop = d3.behavior.drag()
	.origin(Object)
	.on("dragstart", function() { d3.event.sourceEvent.stopPropagation(); })
	.on("dragend", self.dragendOperationBar())
	.on("drag", self.tdragresize2());

//黄色部分上部のイベント登録
this.warnbottom = d3.behavior.drag()
	.origin(Object)
	.on("dragstart", function() { d3.event.sourceEvent.stopPropagation(); })
	.on("dragend", self.dragendOperationBar())
	.on("drag", self.bdragresize2());

//イベント登録なし
this.dragevent_none = d3.behavior.drag()
	.origin(Object)
	.on("dragstart", function() { d3.event.sourceEvent.stopPropagation(); })
	.on("dragend", function() { d3.event.sourceEvent.stopPropagation(); })
	.on("drag", function() { d3.event.sourceEvent.stopPropagation(); });

// 描画する元グラフ(NewGraph)のvisを取得
this.svg = target.vis.select("#upperlower");

// 背景の赤
this.back = this.svg.append("rect")
	.data([{x: 0, y: 0}])// groupの座標
	.attr("width", target.width)
	.attr("height", target.height)
	.attr("pointer-events", "none")
	.style("fill", HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_CRITICAL)
	.style("opacity", HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_CRITICAL_OPACITY)
	.attr("class", "threshold_back");

// 緑部分のグループ
this.newg = this.svg.append("g")
	.data([{x: 0, y: target.y(this.info_max)}])// groupの座標
	.attr("clip-path", "url(#clip)")
	.attr("class", "newg");
// 黄色部分のグループ
this.newg2 = this.svg.append("g")
	.data([{x: 0, y: target.y(this.warn_max)}])// groupの座標
	.attr("clip-path", "url(#clip)")
	.attr("class", "newg2");


// 緑部分の四角(大きさが変わるところ)
this.dragrectinfo = this.newg.append("rect")
	.attr("id", "info_body")
	.attr("x", function(d) { return d.x; })
	.attr("y", function(d) { return d.y; })
	.attr("height", this.infoheight)
	.attr("width", this.width)
	.attr("pointer-events", "none")
	.attr("fill", HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_INFO)
	.attr("fill-opacity", HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_INFO_OPACITY);

// 緑部分の握るところ(上側)
this.dragbartopinfo = this.newg.append("rect")
	.attr("x", function(d) { return d.x; })
	.attr("y", function(d) { return d.y + (self.dragbarw); })
	.attr("height", this.dragbarw)
	.attr("id", "infotop")
	.attr("width", this.width)
	.attr("fill", HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_INFO)
	.attr("fill-opacity", HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_INFO_OPERATION_OPACITY)
	.attr("cursor", "ns-resize")
	.call(this.infotop);

// 緑部分の握るところ(下側)
this.dragbarbottominfo = this.newg.append("rect")
	.attr("x", function(d) { return d.x; })
	.attr("y", function(d) { return d.y + self.infoheight - (self.dragbarw); })
	.attr("id", "infobottom")
	.attr("height", this.dragbarw)
	.attr("width", this.width)
	.attr("fill", HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_INFO)
	.attr("fill-opacity", HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_INFO_OPERATION_OPACITY)
	.attr("cursor", "ns-resize")
	.call(this.infobottom);

// 黄色部分の四角(大きさが変わるところ)
this.dragrectwarn = this.newg2.append("rect")
	.attr("id", "warn_body")
	.attr("x", function(d) { return d.x; })
	.attr("y", function(d) { return d.y; })
	.attr("height", this.warnheight)
	.attr("width", this.width)
	.attr("pointer-events", "none")
	.attr("fill", HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_WARN)
	.attr("fill-opacity", HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_WARN_OPACITY);

// 黄色部分の握るところ(上側)
this.dragbartopwarn = this.newg2.append("rect")
	.attr("x", function(d) { return d.x; })
	.attr("y", function(d) { return d.y - (self.dragbarw); })
	.attr("height", this.dragbarw)
	.attr("id", "warntop")
	.attr("width", this.width)
	.attr("fill", HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_WARN)
	.attr("fill-opacity", HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_WARN_OPERATION_OPACITY)
	.attr("cursor", "ns-resize")
	.call(this.warntop);

// 黄色部分の握るところ(下側)
this.dragbarbottomwarn = this.newg2.append("rect")
	.attr("x", function(d) { return d.x; })
	.attr("y", function(d) { return d.y + self.warnheight - (self.dragbarw); })
	.attr("id", "warnbottom")
	.attr("height", this.dragbarw)
	.attr("width", this.width)
	.attr("fill", HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_WARN)
	.attr("fill-opacity", HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_WARN_OPERATION_OPACITY)
	.attr("cursor", "ns-resize")
	.call(this.warnbottom);

} // end of ThresholdGraph


/////////////////////////////////////
// 緑上のバー動かすと呼ばれる
ThresholdGraph.prototype.tdragresize = function(d) {
	var self = this;
	return function(d) {
		var oldy = d.y; 

		d.y = Math.max(0, Math.min(d.y + self.infoheight, d3.event.y)); 
		self.infoheight = self.infoheight + (oldy - d.y);

		// 透過部分上部のy軸位置の変更
		self.dragbartopinfo
			.attr("y", function(d) { 
				self.info_max = graph[self.elementid].y.invert(d.y);
				if (self.infoheight == 0) {
					self.info_max = self.info_min;
				}
				return d.y - (self.dragbarw); 
			});

		// 緑部分のy軸位置と高さの変更
		self.dragrectinfo
			.attr("y", function(d) { return d.y; })
			.attr("height", self.infoheight);
		
		// ツールチップの表示
		self.appendTooltip();
	}
}

/////////////////////////////////////////
// 緑下のバーを動かすと呼ばれる
ThresholdGraph.prototype.bdragresize = function(d) {
	var self = this;
	return function(d) {
		var dragy = Math.max(d.y, Math.min(self.hh, d.y + self.infoheight + d3.event.dy));

		self.infoheight = dragy - d.y;

		// 透過部分下部のy軸位置の変更
		self.dragbarbottominfo
			.attr("y", function(d) { 
				self.info_min = graph[self.elementid].y.invert(dragy);
				if (self.infoheight == 0) {
					self.info_min = self.info_max;
				}
				return dragy;
			 });

		// 緑部分の高さの変更
		self.dragrectinfo
			.attr("height", self.infoheight);

		// ツールチップの表示
		self.appendTooltip();
	}
}
/////////////////////////////////////
// 黄色上のバー動かすと呼ばれる
ThresholdGraph.prototype.tdragresize2 = function(d) {
	var self = this;
	return function(d) {
		var oldy = d.y; 
		
		d.y = Math.max(0, Math.min(d.y + self.warnheight, d3.event.y)); 
		self.warnheight = self.warnheight + (oldy - d.y);

		// 透過部分上部のy軸位置の変更
		self.dragbartopwarn
			.attr("y", function(d) {
			self.warn_max = graph[self.elementid].y.invert(d.y);
			if (self.warnheight == 0) {
				self.warn_max = self.warn_min;
			}
			return d.y - (self.dragbarw); });

		// 黄色部分のy軸位置と高さの変更
		self.dragrectwarn.attr("y", function(d) { 
			return d.y; })
			.attr("height", self.warnheight);
		// ツールチップの表示
		self.appendTooltip();
	}
}

/////////////////////////////////////////
//黄色下のバーを動かすと呼ばれる
ThresholdGraph.prototype.bdragresize2 = function(d) {
	var self = this;
	return function(d) {
		var dragy2 = Math.max(d.y, Math.min(self.hh, d.y + self.warnheight + d3.event.dy));
		self.warnheight = dragy2 - d.y;

		// 透過部分下部のy軸位置の変更
		self.dragbarbottomwarn.attr("y", function(d) {
			self.warn_min = graph[self.elementid].y.invert(dragy2);
			if (self.warnheight == 0) {
				self.warn_min = self.warn_max;
			}
			return dragy2;
		 });

		// 黄色部分の高さの変更
		self.dragrectwarn.attr("height", self.warnheight);
		
		// ツールチップの表示
		self.appendTooltip();
	}
}

/////////////////////////////////////////
// 閾値のツールチップを表示します
ThresholdGraph.prototype.appendTooltip = function() {
	var self = this;
	var num_warn_min = Number(self.warn_min);
	var num_warn_max = Number(self.warn_max);
	var num_info_min = Number(self.info_min);
	var num_info_max = Number(self.info_max);
	var str = "<dl>" 
		+ "<dt style='border-bottom-color :" + HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_WARN + ";border-left-color :" + HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_WARN + ";'>" + getGraphMessages("mess-warning") + " : " 
		+ num_warn_min.toPrecision(self.getPrecisionThresholdNum(num_warn_min)) + " ～ " + num_warn_max.toPrecision(self.getPrecisionThresholdNum(num_warn_max)) +"</dt>"
		+ "<dt style='border-bottom-color :" + HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_INFO + ";border-left-color :" + HINEMOS_COLLECT_CONST.CONST_THRESHOLD_COLOR_INFO + ";'>" + getGraphMessages("mess-information") + " : " 
		+ num_info_min.toPrecision(self.getPrecisionThresholdNum(num_info_min)) + " ～ " + num_info_max.toPrecision(self.getPrecisionThresholdNum(num_info_max)) + "</dt>"
		+ "</dl>";
	var tooltip = d3.select("body").select(".tooltip_mouse").style("visibility", "visible").style("width", "250px").html(str);
	var pagey = d3.event.sourceEvent.pageY;
	var pagex = d3.event.sourceEvent.pageX;
	// chromeとfirefoxはd3.event.pageXで取れる、IE10、11も取れるがIE10だと座標が若干ずれている
	// そのため、IE10の場合はif文で値を取得しなおす
	// firefoxでは[event is not defined.]になるため、ブラウザチェックする
	if (!checkBrowserKind("firefox") && (event.pageY == undefined || event.pageX == undefined)) { // IE9、10でのevent.pageX、event.pageYが取れない対策
		pagex = event.clientX + (document.body.scrollLeft || document.documentElement.scrollLeft);
		pagey = event.clientY + (document.body.scrollTop || document.documentElement.scrollTop);
	}
	tooltip.style("top", (pagey-15)+"px").style("left",(pagex+10)+"px");
}

/////////////////////////////////////////
// グラフを動かすと呼ばれる
ThresholdGraph.prototype.redraw = function() {
	var self = this;
	var info_ychange = 0;
	var info_heightchange = 0;
	var warn_ychange =0;
	var warn_heightchange = 0;
	// 緑部分のy軸位置と高さの変更
	self.dragrectinfo
		.attr("y", function(d) {
			var yvalue = graph[self.elementid].y(self.info_max);
			if (yvalue < HINEMOS_COLLECT_CONST.CONST_THRESHOLD_LIMIT_DISP_MINUS) {
				info_ychange = yvalue - HINEMOS_COLLECT_CONST.CONST_THRESHOLD_LIMIT_DISP_MINUS;
				yvalue = HINEMOS_COLLECT_CONST.CONST_THRESHOLD_LIMIT_DISP_MINUS;
			}
			if (HINEMOS_COLLECT_CONST.CONST_THRESHOLD_LIMIT_DISP_PLUS < yvalue) {
				info_ychange = yvalue = HINEMOS_COLLECT_CONST.CONST_THRESHOLD_LIMIT_DISP_PLUS;;
				yvalue = HINEMOS_COLLECT_CONST.CONST_THRESHOLD_LIMIT_DISP_PLUS;
			}
			d.y = yvalue;
			return yvalue;
		})
		.attr("height", function(d) {
			self.infoheight = graph[self.elementid].y(self.info_min) - graph[self.elementid].y(self.info_max);
			if (info_ychange != 0) {
				if (info_ychange < 0) {
					self.infoheight = self.infoheight - info_ychange*-1;
				} else {
					self.infoheight = self.infoheight - info_ychange;
				}
				if (self.infoheight < 0) {
					self.infoheight = 10;
				}
				info_heightchange = self.infoheight;
			}
			if (HINEMOS_COLLECT_CONST.CONST_THRESHOLD_LIMIT_DISP_PLUS + HINEMOS_COLLECT_CONST.CONST_GRAPH_HEIGHT < self.infoheight) {
				info_heightchange = HINEMOS_COLLECT_CONST.CONST_THRESHOLD_LIMIT_DISP_PLUS*2;
				return HINEMOS_COLLECT_CONST.CONST_THRESHOLD_LIMIT_DISP_PLUS*2;
			}
			return self.infoheight;
			});
		
	// 透過部分上部のy軸位置の変更
	self.dragbartopinfo
		.attr("y", function(d) {
			var yvalue = graph[self.elementid].y(self.info_max);
			if (info_ychange < 0) {
				return HINEMOS_COLLECT_CONST.CONST_THRESHOLD_LIMIT_DISP_MINUS - (self.dragbarw);
			} else if (info_ychange > 0) {
				return HINEMOS_COLLECT_CONST.CONST_THRESHOLD_LIMIT_DISP_PLUS - (self.dragbarw);
			}
			return yvalue - (self.dragbarw); 
		});

	// 透過部分下部のy軸位置の変更
	self.dragbarbottominfo
		.attr("y", function(d) { 
			var yvalue = graph[self.elementid].y(self.info_max);
			if (info_ychange < 0) {
				yvalue = HINEMOS_COLLECT_CONST.CONST_THRESHOLD_LIMIT_DISP_MINUS;
			} else if (info_ychange > 0) {
				yvalue = HINEMOS_COLLECT_CONST.CONST_THRESHOLD_LIMIT_DISP_PLUS;
			}
			var infoheight = self.infoheight;
			if (info_heightchange != 0) {
				infoheight = info_heightchange;
			}
			return yvalue + infoheight; 
		});
		
	// warn start
	
	// 黄色部分のy軸位置と高さの変更
	self.dragrectwarn
		.attr("y", function(d) {
			// Yの座標が遠すぎる場合、値を擬似値に変更する
			var yvalue = graph[self.elementid].y(self.warn_max);
			if (yvalue < HINEMOS_COLLECT_CONST.CONST_THRESHOLD_LIMIT_DISP_MINUS) {
				warn_ychange = yvalue - HINEMOS_COLLECT_CONST.CONST_THRESHOLD_LIMIT_DISP_MINUS; // どれだけ短くしたか
				yvalue = HINEMOS_COLLECT_CONST.CONST_THRESHOLD_LIMIT_DISP_MINUS;
			}
			if (HINEMOS_COLLECT_CONST.CONST_THRESHOLD_LIMIT_DISP_PLUS < yvalue) {
				warn_ychange = yvalue - HINEMOS_COLLECT_CONST.CONST_THRESHOLD_LIMIT_DISP_PLUS; // どれだけ短くしたか
				yvalue = HINEMOS_COLLECT_CONST.CONST_THRESHOLD_LIMIT_DISP_PLUS;
			}
			d.y = yvalue;
			return yvalue;
		})
		.attr("height", function(d) {
			self.warnheight = graph[self.elementid].y(self.warn_min) - graph[self.elementid].y(self.warn_max);
			if (warn_ychange != 0) {
				if (warn_ychange < 0) {
					self.warnheight = self.warnheight - warn_ychange*-1;
				} else {
					self.warnheight = self.warnheight - warn_ychange;
				}
				if (self.warnheight < 0) {
					// yの座標を変更したら高さ分よりも下にyの座標が来た場合
					self.warnheight = 30;
				}
				warn_heightchange = self.warnheight;
			}
				
			if (HINEMOS_COLLECT_CONST.CONST_THRESHOLD_LIMIT_DISP_PLUS + HINEMOS_COLLECT_CONST.CONST_GRAPH_HEIGHT < self.warnheight) {
				warn_heightchange = HINEMOS_COLLECT_CONST.CONST_THRESHOLD_LIMIT_DISP_PLUS*2;
				return HINEMOS_COLLECT_CONST.CONST_THRESHOLD_LIMIT_DISP_PLUS*2;
			}
			return self.warnheight;
			});
		
	// 透過部分上部のy軸位置の変更
	self.dragbartopwarn
		.attr("y", function(d) {
			var yvalue = graph[self.elementid].y(self.warn_max);
			if (warn_ychange < 0) {
				return HINEMOS_COLLECT_CONST.CONST_THRESHOLD_LIMIT_DISP_MINUS - (self.dragbarw);
			} else if (warn_ychange > 0) {
				return HINEMOS_COLLECT_CONST.CONST_THRESHOLD_LIMIT_DISP_PLUS - (self.dragbarw);
			}
			return yvalue - (self.dragbarw);
		});

	// 透過部分下部のy軸位置の変更
	self.dragbarbottomwarn
		.attr("y", function(d) { 
			var yvalue = graph[self.elementid].y(self.warn_max);
			if (warn_ychange < 0) {
				yvalue = HINEMOS_COLLECT_CONST.CONST_THRESHOLD_LIMIT_DISP_MINUS;
			} else if (warn_ychange > 0) {
				yvalue = HINEMOS_COLLECT_CONST.CONST_THRESHOLD_LIMIT_DISP_PLUS;
			}
			var warnheight = self.warnheight;
			if (warn_heightchange != 0) {
				warnheight = warn_heightchange;
			}
			return yvalue + warnheight; 
		});
}
/////////////////////////////////////////
// 閾値の操作が終わったときに呼ばれる
ThresholdGraph.prototype.dragendOperationBar = function() {
	var self = this;
	return function(d) {
		var infomin = Number(self.info_min);
		var infomax = Number(self.info_max);
		var warnmin = Number(self.warn_min);
		var warnmax = Number(self.warn_max);
		var param = {};
		param.method_name = "changeThreshold";
		param.info_min = infomin.toPrecision(self.getPrecisionThresholdNum(infomin));
		param.info_max = infomax.toPrecision(self.getPrecisionThresholdNum(infomax));
		param.warn_min = warnmin.toPrecision(self.getPrecisionThresholdNum(warnmin));
		param.warn_max = warnmax.toPrecision(self.getPrecisionThresholdNum(warnmax));
		param.itemname = self.itemname;
		param.monitorid = self.monitorid;
		param.managername = self.managername;
		param.pluginid = self.pluginid;
	
		callJavaMethod(param);
		// 閾値のツールチップを消します
		disableTooltip();
	}
}
/////////////////////////////////////////
// 閾値描画を削除する
ThresholdGraph.prototype.removeElements = function(elementid) {
	var deleteThre = document.getElementById(elementid);
	var deletetarget = deleteThre.getElementsByClassName("threshold_back");
	ThresholdGraph.prototype.removeTarget(deletetarget);

	deletetarget = deleteThre.getElementsByClassName("newg");
	ThresholdGraph.prototype.removeTarget(deletetarget);
	
	deletetarget = deleteThre.getElementsByClassName("newg2");
	ThresholdGraph.prototype.removeTarget(deletetarget);
}
/////////////////////////////////////////
// 閾値描画を削除する
ThresholdGraph.prototype.removeTarget = function(targetObj) {
	for (var i = 0; i < targetObj.length; i++) {
		var parent = targetObj[i].parentNode;
		parent.removeChild(targetObj[i]);
	}
}
/////////////////////////////////////////
//指定されたelementidの閾値の範囲を変更する
ThresholdGraph.prototype.setThresholdParam = function(elementid, targetObj) {
	var self = thresholdgraph[elementid];
	if (self.info_min == targetObj.info_min
		&& self.info_max == targetObj.info_max
		&& self.warn_min == targetObj.warn_min
		&& self.warn_max == targetObj.warn_max) {
		return;
	}

	self.info_min = targetObj.info_min;// green
	self.info_max = targetObj.info_max;
	self.warn_min = targetObj.warn_min;// yellow
	self.warn_max = targetObj.warn_max;
	self.redraw();
}
/////////////////////////////////////////
//すべてのグラフの閾値の範囲を変更する
ThresholdGraph.prototype.setThresholdParamMonitorId = function(targetObj) {
	for (keyValue in thresholdgraph) {
		if (thresholdgraph[keyValue].monitorid == targetObj.monitorid) {
			ThresholdGraph.prototype.setThresholdParam(keyValue, targetObj);
		}
	}
}
/////////////////////////////////////////
// collectIdがnoneの場合、閾値表示をしないように表示を変更する
// ・閾値表示のバーなどの透過度を0にする
// ・バーのイベントをなくす
// ・バーのカーソル種類をautoにする
ThresholdGraph.prototype.setThresholdOpacityZero = function(targetId) {
	for (keyValue in thresholdgraph) {
		if (keyValue == targetId) {
			var thre_self = thresholdgraph[keyValue];
			thre_self.back
			.attr("cursor", "auto")
			.style("opacity", 0);
			
			thre_self.dragrectinfo
			.attr("cursor", "auto")
			.style("opacity", 0);
			
			thre_self.dragbartopinfo
			.call(thre_self.dragevent_none)
			.attr("cursor", "auto")
			.style("opacity", 0);

			thre_self.dragbarbottominfo
			.call(thre_self.dragevent_none)
			.attr("cursor", "auto")
			.style("opacity", 0);

			thre_self.dragrectwarn
			.attr("cursor", "auto")
			.style("opacity", 0);
			
			thre_self.dragbartopwarn
			.call(thre_self.dragevent_none)
			.attr("cursor", "auto")
			.style("opacity", 0);
			
			thre_self.dragbarbottomwarn
			.call(thre_self.dragevent_none)
			.attr("cursor", "auto")
			.style("opacity", 0);
			
			break;
		}
	}
}
/////////////////////////////////////////
// 閾値を操作した際に表示されるツールチップのそれぞれの値の有効桁数を返します。
// 小数点以上の値の長さ＋小数点は第3位まで
ThresholdGraph.prototype.getPrecisionThresholdNum = function(param) {
	var params = (param + "").split("\.");
	if (params.length == 1) {
		// 小数点なしの場合
		return params[0].length;
	} else {
		var leftlen = params[0].length;
		var rightlen = params[1].length;
		if (rightlen > 3) {
			rightlen = 3;
		}
		return leftlen + rightlen;
	}
}
