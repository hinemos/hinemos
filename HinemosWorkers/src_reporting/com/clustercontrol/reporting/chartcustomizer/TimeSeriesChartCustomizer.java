/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.chartcustomizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;

import net.sf.jasperreports.engine.JRChart;
import net.sf.jasperreports.engine.JRChartCustomizer;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeSeriesChartCustomizer implements JRChartCustomizer {
	
	private static Log log = LogFactory.getLog(TimeSeriesChartCustomizer.class);

	@Override
	public void customize(JFreeChart chart, JRChart jasperChart) {
		XYPlot plot = chart.getXYPlot();
		log.debug("Chart Title: " + chart.getTitle().getText());
		
		DateAxis srcAxis = (DateAxis)plot.getDomainAxis();
		Date minDate = srcAxis.getMinimumDate();
		Date maxDate = srcAxis.getMaximumDate();
		log.debug("date range: " + minDate + " - " + maxDate);
		if ((maxDate.getTime() - minDate.getTime()) / (1000.0 * 60 * 60 * 24) > 365.5) {
			srcAxis.setDateFormatOverride(new SimpleDateFormat(" YYYY/M "));
		} else if ((maxDate.getTime() - minDate.getTime()) / (1000.0 * 60 * 60 * 24) > 6.5) {
			srcAxis.setDateFormatOverride(new SimpleDateFormat("  M/d  "));
		} else if ((maxDate.getTime() - minDate.getTime()) / (1000.0 * 60 * 60 * 24) > 1.5) {
			srcAxis.setDateFormatOverride(new SimpleDateFormat("M/d H'H'"));
		} else {
			srcAxis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
		}
		//srcAxis.setLowerBound(srcAxis.getLowerBound() - 1.0d);
		srcAxis.setUpperMargin(srcAxis.getUpperMargin() + 0.01);
		
		ValueAxis rangeAxis = plot.getRangeAxis();
		//NumberAxis axis = (NumberAxis)rangeAxis;
		//axis.setNumberFormatOverride(new DecimalFormat("#0.0"));
		if (!"%".equals(rangeAxis.getLabel())) {
			if (rangeAxis.getRange().getUpperBound() <= 5.0E-9) {
				NumberAxis axis = (NumberAxis)rangeAxis;
				axis.setNumberFormatOverride(new DecimalFormat("#0"));
			//} else if (rangeAxis.getRange().getUpperBound() < 0.1) {
			//	rangeAxis.setUpperBound(0.1);
			}
		}
	}
	
}
