/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.chartcustomizer;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickMarkPosition;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.ui.RectangleInsets;

import net.sf.jasperreports.engine.JRChart;
import net.sf.jasperreports.engine.JRChartCustomizer;

public class JobDetailGanttChartCustomizer implements JRChartCustomizer {

	@Override
	public void customize(JFreeChart chart, JRChart jasperChart) {
		CategoryPlot plot = chart.getCategoryPlot();
		CategoryAxis axis = plot.getDomainAxis();
		axis.setVisible(false);
		ValueAxis rangeAxis = plot.getRangeAxis();
		rangeAxis.setAxisLineVisible(false);
		//plot.setRangeGridlinesVisible(false);
		//rangeAxis.setMinorTickCount(2);
		rangeAxis.setRange(rangeAxis.getLowerBound() - 1.0d, rangeAxis.getUpperBound() + 1.0d);
		rangeAxis.setAutoTickUnitSelection(false);
		((DateAxis)rangeAxis).setTickUnit(new DateTickUnit(DateTickUnit.HOUR, 2));
		((DateAxis)rangeAxis).setTickMarkPosition(DateTickMarkPosition.START);
		plot.setInsets(new RectangleInsets(2.0d, 1.0d, 2.0d, 1.0d));
	}
	
}
