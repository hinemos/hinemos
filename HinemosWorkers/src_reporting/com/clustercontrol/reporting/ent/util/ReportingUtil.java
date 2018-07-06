/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.ent.util;

import com.clustercontrol.reporting.ReportUtil;

/**
 * Reporting Utility Class
 * 
 * @version 5.0.b
 * @since 5.0.b
 */
public final class ReportingUtil{

	/**
	 * Calculate average interval for SQL query
	 * 
	 * @param run_interval
	 * @param output_period_for
	 * @param output_period_for
	 * @return int average interval
	 */
	public static int run2AvgInterval( int run_interval, int output_period_type, int output_period_for ){
		if( ReportUtil.OUTPUT_PERIOD_TYPE_MONTH == output_period_type ){
			if( 1 == output_period_for ){
				return 2 * 60 * 60;
			} else {
				return 6 * 60 * 60;
			}
		}

		int avg = 1;

		// TODO Make it proportional to output duration
		int day_period = output_period_for;
		if (day_period <= 1){
			if (run_interval < 5 * 60){
				avg = 5 * 60;
			}else{
				avg = run_interval;
			}
		} else if (day_period <= 7){
			if (run_interval < 30 * 60){
				avg = 30 * 60;
			}else{
				avg = run_interval;
			}
		} else if (day_period <= 14){
			if (run_interval <= 30 * 60){
				avg = 60 * 60;
			}else{
				avg = run_interval;
			}
		} else {
			avg = 2 * 60 * 60;
		}

		return avg;
	}

	/**
	 * Trim legend label in format "prefix ... suffix"
	 * 
	 * @param label
	 * @param headLength
	 * @param tailLength
	 * @param conjunction
	 * @return trimmed legend name
	 */
	public static String trimLegend(String label, int headLength, int tailLength, String conjunction){
		StringBuffer trimmed = new StringBuffer(label);
		if (label.length()-1 > headLength + tailLength) {
			trimmed.append(label.substring(0, headLength));
			trimmed.append(conjunction);
			trimmed.append(label.substring(label.length()-tailLength, label.length()));
		}
		return trimmed.toString();
	}
}
