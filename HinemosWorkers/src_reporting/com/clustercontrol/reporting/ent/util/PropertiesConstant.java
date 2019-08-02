/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.ent.util;

/**
 * PropertiesのConstantクラス
 * 
 * @version 5.0.b
 * @since 5.0.b
 */
public final class PropertiesConstant{

	// Property keys and default values for per item/category data source
	public static final String CHART_TITLE_KEY = "chart.title";
	public static final String LABEL_KEY = "label";
	public static final String FIXVAL_KEY = "fixval";
	public static final String MINVAL_KEY = "minval";
	public static final String GRAPH_OUTPUT_ID_KEY = "graph.output.id";
	public static final String DIVIDER_KEY = "divider";
	public static final String ITEM_CODE_KEY = "item.codes";
	public static final String ADJUST_MIN_VALUE_KEY = "adjust.min.value";
	public static final String ADJUST_MAX_VALUE_KEY = "adjust.max.value";

	public static final String LEGEND_TRIM_PREFIX_KEY = "legend.trim.prefix";
	public static final String LEGEND_TRIM_SUFFIX_KEY = "legend.trim.suffix";
	public static final String TRIM_STR_KEY = "trim.str";
	public static final String TRIM_STR_DEFAULT = " ... ";

	public static final String MODE_AUTO = "auto";
	public static final String OUTPUT_MODE_KEY = "output.mode";
	public static final String OUTPUT_MODE_DEFAULT = MODE_AUTO;

	public static final String ADD_POINT_TIME_KEY = "add.point.time";
	public static final String LEGEND_TYPE_KEY = "legend.type";
	public static final String TYPE_FACILITY_ID = "facility_id";
	public static final String TYPE_FACILITY_NAME = "facility_name";
	public static final String LEGEND_TYPE_DEFAULT = TYPE_FACILITY_ID;

	// Property keys and default values for resource group template
	public static final String REPORT_GRAPH_LINE_MAX_KEY = "report.graph.line.max";
	public static final String GRAPH_LINE_MAX_DEFAULT = "10";
	public static final String CHART_TYPE_NUM_KEY = "chart.type.num";
	public static final String CATEGORY_TITLE_KEY = "category.title";

	// Property keys and default values for job run time data source
	public static final String JOB_ORDER_NUM_KEY = "job.order.num";
	public static final String JOB_ORDER_NUM_DEFAULT = "30";

	public static final String JOB_UNIT_REGEX_KEY_ = "job.unit.id";
	public static final String REGEX_DEFAULT = "%%";
	public static final String JOB_ID_REGEX_KEY_ = "job.id";
	public static final String JOB_ID_REGEX_EXC_KEY_ = "job.id.exc";
	public static final String JOB_ORDER_KEY_KEY_ = "job.order.key";

	public static final String JOB_GRAPH_LINE_MAX_KEY = "job.graph.line.max";

	public static final String JOB_GRAPH_DIVIDER_KEY = "job.graph.divider";
	public static final String JOB_GRAPH_DIVIDER_DEFAULT = "60000";
	public static final String JOB_GRAPH_LABEL_KEY = "job.graph.label";
	public static final String JOB_GRAPH_LABEL_DEFAULT = "分"; // TODO i18n

	public static final String JOB_GRAPH_CHART_TITLE_KEY = "job.graph.chart.title";
	
	public static final String ORDER_KEY_MAX = "max";
	public static final String ORDER_KEY_AVG = "avg";
	public static final String ORDER_KEY_DIFF = "diff";
}
