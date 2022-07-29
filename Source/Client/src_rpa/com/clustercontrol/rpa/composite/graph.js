/*
 * Copyright (c) 2021 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

/*
 * 集計グラフを描画するJavaScriptです。
 */

//////////////////////////////////////////////////////////////
// Method
//////////////////////////////////////////////////////////////

ControlGraph = {};

// グラフをすべて削除する
ControlGraph.delDiv = function () {
	try {
		var aNode = document.getElementById("RightGraphs");
		for (var i =aNode.childNodes.length-1; i>=0; i--) {
			aNode.removeChild(aNode.childNodes[i]);
		}
		
		aNode = document.getElementById("LeftGraphs");
		for (var i =aNode.childNodes.length-1; i>=0; i--) {
			aNode.removeChild(aNode.childNodes[i]);
		}
		
		aNode = document.getElementById("CenterGraphs");
		for (var i =aNode.childNodes.length-1; i>=0; i--) {
			aNode.removeChild(aNode.childNodes[i]);
		}
		
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

//積み上げ棒グラフ（日別シナリオ実施件数）を追加
ControlGraph.addExecScenarioGraph = function (data) {
	
	try {
		// グラフ全体のサイズ
		var svgWidth = HINEMOS_RPA_CONST.CONST_GRAPH_WIDTH_EXEC_SCENARIO 
						- HINEMOS_RPA_CONST.CONST_GRAPH_MARGIN_LEFT 
						- HINEMOS_RPA_CONST.CONST_GRAPH_MARGIN_RIGHT;
		var svgHeight = HINEMOS_RPA_CONST.CONST_GRAPH_HEIGHT_EXEC_SCENARIO 
						- HINEMOS_RPA_CONST.CONST_GRAPH_MARGIN_TOP 
						- HINEMOS_RPA_CONST.CONST_GRAPH_MARGIN_BOTTOM;
		
		// グラフの枠作成
		var svg = d3.select('#LeftGraphs').append('svg').attr({
			width: svgWidth + HINEMOS_RPA_CONST.CONST_GRAPH_MARGIN_LEFT + HINEMOS_RPA_CONST.CONST_GRAPH_MARGIN_RIGHT,
			height: svgHeight + HINEMOS_RPA_CONST.CONST_GRAPH_MARGIN_TOP + HINEMOS_RPA_CONST.CONST_GRAPH_MARGIN_BOTTOM
		}).append("g").attr("transform", "translate(" + HINEMOS_RPA_CONST.CONST_GRAPH_MARGIN_LEFT*1.5 + "," + HINEMOS_RPA_CONST.CONST_GRAPH_MARGIN_TOP + ")");
		
		if (data[0].values.length != 0){
			//スタック関数の設定
			var stack = d3.layout.stack().offset("zero").values(function(d) { return d.values; });
			
			//データを積み上げた状態の配列
			data = stack(data);
			
			// X軸の作成
			var xScale = d3.scale.ordinal().rangeRoundBands([0, svgWidth], 0.1);
			
			// X軸のデータ作成
			var xAxis = d3.svg.axis().scale(xScale).orient("bottom");
			
			// X軸に各項目名をセット
			xScale.domain(data[0].values.map(function(d){return d.x;}));
			
			// Y軸の作成
			var yScale = d3.scale.linear().domain([0, d3.max(data, function(d) {return d3.max(d.values, function(d) {return d.y0 + d.y;});})]).range([svgHeight, 0]);
			
			// Y軸のデータ作成
			var yAxis = d3.svg.axis().scale(yScale).orient('left');
			
			// カラーパレットの設定
			var color = [HINEMOS_RPA_CONST.CONST_COLOR_GREEN, HINEMOS_RPA_CONST.CONST_COLOR_RED]; 
			
			// データのグループ作成
			var groups = svg.selectAll("g")
				.data(data)
				.enter()
				.append("g")
				.style("fill", function(d, i) {return color[i];});
			
			// それぞれのデータのグラフを作成
			var layers = groups.selectAll("rect")
				.data(function(d) { return d.values; })
				.enter()
				.append("rect")
				.attr("x", function(d) {return xScale(d.x);})
				.attr("y", function(d) {return yScale(d.y0) - (svgHeight - yScale(d.y));})
				.attr("height", function(d){return svgHeight - yScale(d.y);})
				.attr("width", function(d) {return xScale.rangeBand();});
			
			// X軸をグラフに追加
			svg.append('g').attr({
				class: 'x axis',
				transform: 'translate(0,' + svgHeight + ')'
			}).call(xAxis)
			.selectAll("text")
			.attr("x", 5)
			.attr("y", 5)
			.attr("transform", "rotate(45)")
			.style("text-anchor", "start");
			
			// Y軸をグラフに追加
			svg.append("g").attr({
				class: 'y axis'
			}).call(yAxis);
			
			// タイトルを設定
			svg.append("g")
			.attr("class", "x axis axis_title")
			.attr("id", "title_top")
			.append("text")
			.style("text-anchor","middle")
			.style("font-size","9pt")
			.style("font-weight","bold")
			.attr("y", HINEMOS_RPA_CONST.CONST_GRAPH_TITLE_Y_POSITION)
			.attr("x", svgWidth/2)
			.text(truncateText(data[0].name, 60));
			
		} else {
			// タイトルを設定
			svg.append("g")
			.attr("class", "x axis axis_title")
			.attr("id", "title_top")
			.append("text")
			.style("text-anchor","start")
			.style("font-size","9pt")
			.style("font-weight","bold")
			.attr("y", HINEMOS_RPA_CONST.CONST_GRAPH_TITLE_Y_POSITION)
			.attr("x", 0)
			.text(truncateText(data[0].name, 60));
		}
		
		console.log("addExecScenarioGraph Complete");
	} catch(e) {
		var param = {};
		param.method_name = "javascript_error";
		param.exception = e;
		callJavaMethod(param);
	}
};

//積み上げ棒グラフ（時間帯別削減工数）を追加
ControlGraph.addReductionByTimeZoneGraph = function (data) {

	try {
		// グラフ全体のサイズ
		var svgWidth = HINEMOS_RPA_CONST.CONST_GRAPH_WIDTH_REDUCTION_BY_TIME_ZONE 
						- HINEMOS_RPA_CONST.CONST_GRAPH_MARGIN_LEFT 
						- HINEMOS_RPA_CONST.CONST_GRAPH_MARGIN_RIGHT;
		var svgHeight = HINEMOS_RPA_CONST.CONST_GRAPH_HEIGHT_REDUCTION_BY_TIME_ZONE 
						- HINEMOS_RPA_CONST.CONST_GRAPH_MARGIN_TOP 
						- HINEMOS_RPA_CONST.CONST_GRAPH_MARGIN_BOTTOM;
		
		// グラフの枠作成
		var svg = d3.select('#RightGraphs').append('svg').attr({
			width: svgWidth + HINEMOS_RPA_CONST.CONST_GRAPH_MARGIN_LEFT + HINEMOS_RPA_CONST.CONST_GRAPH_MARGIN_RIGHT,
			height: svgHeight + HINEMOS_RPA_CONST.CONST_GRAPH_MARGIN_TOP + HINEMOS_RPA_CONST.CONST_GRAPH_MARGIN_BOTTOM
		}).append("g").attr("transform", "translate(" + HINEMOS_RPA_CONST.CONST_GRAPH_MARGIN_LEFT + "," + HINEMOS_RPA_CONST.CONST_GRAPH_MARGIN_TOP + ")");
		
		//スタック関数の設定
		var stack = d3.layout.stack().offset("zero").values(function(d) { return d.values; });
		
		//データを積み上げた状態の配列
		data = stack(data);
		
		// X軸の作成
		var xScale = d3.scale.linear().domain([0, d3.max(data, function(d) {return d3.max(d.values, function(d) {return d.y0 + d.y;});})]).range([0, svgWidth]);
		
		// X軸のデータ作成
		var xAxis = d3.svg.axis().scale(xScale).orient('bottom');
		
		// Y軸の作成
		var yScale = d3.scale.ordinal().rangeRoundBands([svgHeight, 0], 0.1);
		
		// Y軸のデータ作成
		var yAxis = d3.svg.axis().scale(yScale).orient("left");
		
		// Y軸に各項目名をセット
		yScale.domain(data[0].values.map(function(d){return d.x;}));
		
		// カラーパレットの設定
		var color = [HINEMOS_RPA_CONST.CONST_COLOR_BLUE, HINEMOS_RPA_CONST.CONST_COLOR_ORANGE];
		
		// データのグループ作成
		var groups = svg.selectAll("g")
			.data(data)
			.enter()
			.append("g")
			.style("fill", function(d, i) {return color[i];});
		
		// それぞれのデータのグラフを作成
		var layers = groups.selectAll("rect")
			.data(function(d) { return d.values; })
			.enter()
			.append("rect")
			.attr("y", function(d) {return yScale(d.x);})
			.attr("x", function(d) {return xScale(d.y0);})
			.attr("width", function(d){return xScale(d.y);})
			.attr("height", function(d) {return yScale.rangeBand();});
		
		// X軸をグラフに追加
		svg.append('g').attr({
			class: 'x axis',
			transform: 'translate(0,' + svgHeight + ')'
		}).call(xAxis)
		.selectAll("text")
		.attr("x", 5)
		.attr("y", 5)
		.attr("transform", "rotate(45)")
		.style("text-anchor", "start");
		
		// Y軸をグラフに追加
		svg.append("g").attr({
			class: 'y axis'
		}).call(yAxis);
		
		// タイトルを設定
		svg.append("g")
		.attr("class", "x axis axis_title")
		.attr("id", "title_top")
		.append("text")
		.style("text-anchor","middle")
		.style("font-size","9pt")
		.style("font-weight","bold")
		.attr("y", HINEMOS_RPA_CONST.CONST_GRAPH_TITLE_Y_POSITION)
		.attr("x", svgWidth/2)
		.text(truncateText(data[0].name, 60));
		
		console.log("addReductionByTimeZoneGraph Complete");
	} catch(e) {
		var param = {};
		param.method_name = "javascript_error";
		param.exception = e;
		callJavaMethod(param);
	}
};

//積み上げ棒グラフ（シナリオ別orノード別のエラー数）を追加
ControlGraph.addErrorsGraph = function (data) {
	ControlGraph.addCenterGraph(data, [HINEMOS_RPA_CONST.CONST_COLOR_GREEN, HINEMOS_RPA_CONST.CONST_COLOR_RED]);
	console.log("addErrorsGraph Complete");
};

//積み上げ棒グラフ（シナリオ別orノード別の削減工数）を追加
ControlGraph.addReductionGraph = function (data) {
	ControlGraph.addCenterGraph(data, [HINEMOS_RPA_CONST.CONST_COLOR_BLUE, HINEMOS_RPA_CONST.CONST_COLOR_ORANGE]);
	console.log("addReductionGraph Complete");
};

//積み上げ棒グラフ（画面下部）を追加
ControlGraph.addCenterGraph = function (data, color) {

	try {
		var valueLength = data[0].values.length / 15;
		if (valueLength < 1){
			valueLength = 1;
		}
		var heightBySize = HINEMOS_RPA_CONST.CONST_BAR_GRAPH_HEIGHT * valueLength;
		
		// グラフ全体のサイズ
		var svgWidth = HINEMOS_RPA_CONST.CONST_BAR_GRAPH_WIDTH 
				- HINEMOS_RPA_CONST.CONST_BAR_GRAPH_MARGIN_LEFT 
				- HINEMOS_RPA_CONST.CONST_GRAPH_MARGIN_RIGHT;
		var svgHeight = heightBySize 
				- HINEMOS_RPA_CONST.CONST_GRAPH_MARGIN_TOP 
				- HINEMOS_RPA_CONST.CONST_GRAPH_MARGIN_BOTTOM;
		
		// グラフの枠作成
		var svg = d3.select('#CenterGraphs').append('svg').attr({
			width: svgWidth + HINEMOS_RPA_CONST.CONST_BAR_GRAPH_MARGIN_LEFT + HINEMOS_RPA_CONST.CONST_GRAPH_MARGIN_RIGHT,
			height: svgHeight + HINEMOS_RPA_CONST.CONST_GRAPH_MARGIN_TOP + HINEMOS_RPA_CONST.CONST_GRAPH_MARGIN_BOTTOM
		}).append("g").attr("transform", "translate(" + HINEMOS_RPA_CONST.CONST_BAR_GRAPH_MARGIN_LEFT + "," + HINEMOS_RPA_CONST.CONST_GRAPH_MARGIN_TOP + ")");
		
		//スタック関数の設定
		var stack = d3.layout.stack().offset("zero").values(function(d) { return d.values; });
		
		//データを積み上げた状態の配列
		data = stack(data);
		
		// X軸の作成
		var xScale = d3.scale.linear().domain([0, d3.max(data, function(d) {return d3.max(d.values, function(d) {return d.y0 + d.y;});})]).range([0, svgWidth]);
		
		// X軸のデータ作成
		var xAxis = d3.svg.axis().scale(xScale).orient('bottom');
		
		// Y軸の作成
		var yScale = d3.scale.ordinal().rangeRoundBands([svgHeight, 0], 0.1);
		
		// Y軸のデータ作成
		var yAxis = d3.svg.axis().scale(yScale).orient("left");
		
		// Y軸に各項目名をセット
		yScale.domain(data[0].values.map(function(d){return d.x;})); 
		
		// データのグループ作成
		var groups = svg.selectAll("g")
			.data(data)
			.enter()
			.append("g")
			.style("fill", function(d, i) {return color[i];});
		
		// それぞれのデータのグラフを作成
		var layers = groups.selectAll("rect")
			.data(function(d) { return d.values; })
			.enter()
			.append("rect")
			.attr("y", function(d) {return yScale(d.x);})
			.attr("x", function(d) {return xScale(d.y0);})
			.attr("width", function(d){return xScale(d.y);})
			.attr("height", function(d) {return yScale.rangeBand();});
		
		// X軸をグラフに追加
		svg.append('g').attr({
			class: 'x axis',
			transform: 'translate(0,' + svgHeight + ')'
		}).call(xAxis)
		.selectAll("text")
		.attr("x", 5)
		.attr("y", 5)
		.attr("transform", "rotate(45)")
		.style("text-anchor", "start");
		
		// Y軸をグラフに追加
		svg.append("g").attr({
			class: 'y axis'
		}).call(yAxis);
		
		// タイトルを設定
		svg.append("g")
		.attr("class", "x axis axis_title")
		.attr("id", "title_top")
		.append("text")
		.style("text-anchor","middle")
		.style("font-size","9pt")
		.style("font-weight","bold")
		.attr("y", HINEMOS_RPA_CONST.CONST_GRAPH_TITLE_Y_POSITION)
		.attr("x", svgWidth/2)
		.text(truncateText(data[0].name, 60));
	} catch(e) {
		var param = {};
		param.method_name = "javascript_error";
		param.exception = e;
		callJavaMethod(param);
	}
};

// 円グラフ（エラー割合）を追加
ControlGraph.addErrorsPieGraph = function (data) {
	ControlGraph.addPieGraph(data, [HINEMOS_RPA_CONST.CONST_COLOR_GREEN, HINEMOS_RPA_CONST.CONST_COLOR_RED]);
	console.log("addErrorsPieGraph Complete");
}

// 円グラフ（削減工数）を追加
ControlGraph.addReductionPieGraph = function (data) {
	ControlGraph.addPieGraph(data, [HINEMOS_RPA_CONST.CONST_COLOR_BLUE, HINEMOS_RPA_CONST.CONST_COLOR_ORANGE]);
	console.log("addReductionPieGraph Complete");
};

//円グラフを追加
ControlGraph.addPieGraph = function (data, color) {
	
	try {
		// 描画域のサイズ
		var svgWidth = HINEMOS_RPA_CONST.CONST_PIE_GRAPH_WIDTH;
		var svgHeight = HINEMOS_RPA_CONST.CONST_PIE_GRAPH_HEIGHT;
		
		// グラフ全体のサイズからマージンを引いたサイズ
		var drawWidth = svgWidth;
		var drawHeight = svgHeight - HINEMOS_RPA_CONST.CONST_GRAPH_MARGIN_BOTTOM;
		
		// ドーナツグラフ全体のサイズ
		var width = svgWidth - HINEMOS_RPA_CONST.CONST_PIE_GRAPH_MARGIN_LEFT - HINEMOS_RPA_CONST.CONST_PIE_GRAPH_MARGIN_RIGHT;
		var height = svgHeight - HINEMOS_RPA_CONST.CONST_PIE_GRAPH_MARGIN_BOTTOM - HINEMOS_RPA_CONST.CONST_PIE_GRAPH_MARGIN_BOTTOM;
		var radius = Math.min(width, height) / 2;
		
		var donutWidth = HINEMOS_RPA_CONST.CONST_PIE_GRAPH_WIDTH_CENTER;
		
		// 円弧の外径と内径を定義（ドーナッツグラフの作成）
		var arc = d3.svg.arc()
			.outerRadius(radius - donutWidth)
			.innerRadius(radius);
		
		// パイを定義
		var pie = d3.layout.pie()
			.sort(null)
			.value(function(d) { return d; });
		
		// svgの定義
		var svg = d3.select("#LeftGraphs").append("svg")
			.attr("width", drawWidth)
			.attr("height", drawHeight)
			.append("g")
			.attr("transform", "translate(" + svgWidth / 2 + "," + svgHeight / 2 + ")");
	
		// パイにデータを割り当て、パイを作成
		var g = svg.selectAll(".arc")
			.data(pie(data.value))
			.enter().append("g")
			.attr("class", "arc");
		
		// 円弧を指定
		g.append("path")
			.attr("d", arc)
			.style("fill", function(d, i) { return color[i]; });
		
		// 値をパイに表示
		g.append("text")
			.attr("transform", function(d) { return "translate(" + arc.centroid(d) + ")"; })
			.attr("dy", ".35em")
			.style("text-anchor", "middle")
			.style("fill", HINEMOS_RPA_CONST.CONST_COLOR_WHITE)
			.style("font-size","10pt")
			.text(function(d) { return d.value; });
		
		// 中央に値を表示する為の数値、文字列を設定
		var dataText = Math.floor((data.value[0] / (data.value[0] + data.value[1])) *100);
		if (!dataText){
			dataText = 0;
		}
		
		var legendSize = 18;
		var horz = 0;
		if (dataText < 10){
			horz = -12;
		} else if (dataText < 100 && 9 < dataText){
			horz = -16;
		} else {
			horz = -20;
		}
		var vert = legendSize - (legendSize / 2);
		var translateText = 'translate(' + horz + ',' + vert + ')';
		
		dataText = dataText + "％";
		if (data.value[0] < 1 && data.value[1] < 1){
			dataText = "－"
		}
		
		// 中央の値を設定
		var legend = svg
					.append('g')
					.attr('class', 'legend')
					.attr('transform', translateText)
					.append('text')
					.text(dataText);
		
		// タイトルを設定
		svg.append("g")
			.attr("class", "x axis axis_title")
			.attr("id", "title_top")
			.append("text")
			.style("text-anchor","middle")
			.style("font-size","9pt")
			.style("font-weight","bold")
			.attr("y", HINEMOS_RPA_CONST.CONST_PIE_GRAPH_TITLE_Y_POSITION)
			.attr("x", 0)
			.text(truncateText(data.name, 60));
	} catch(e) {
		var param = {};
		param.method_name = "javascript_error";
		param.exception = e;
		callJavaMethod(param);
	}
};
