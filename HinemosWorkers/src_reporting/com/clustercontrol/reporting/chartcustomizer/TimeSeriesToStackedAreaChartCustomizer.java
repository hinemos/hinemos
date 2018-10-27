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
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer2;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeTableXYDataset;

import net.sf.jasperreports.engine.JRChart;
import net.sf.jasperreports.engine.JRChartCustomizer;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


public class TimeSeriesToStackedAreaChartCustomizer implements JRChartCustomizer {

	private static Log log = LogFactory.getLog(TimeSeriesToStackedAreaChartCustomizer.class);
	
	@Override
	public void customize(JFreeChart chart, JRChart jasperChart) {
		XYPlot plot = chart.getXYPlot();
		log.debug("Chart Title: " + chart.getTitle().getText());
		
		TimeSeriesCollection inputDataSet = (TimeSeriesCollection)plot.getDataset();
		TimeTableXYDataset xyDataSet = new TimeTableXYDataset();
		
		// copy original dataset to new TimeTableXYDataset
		@SuppressWarnings("unchecked")
		List<TimeSeries> seriesList = inputDataSet.getSeries();
		Iterator<TimeSeries> it = seriesList.iterator();
		while (it.hasNext()) {
			TimeSeries series = it.next();
			int itemCount = series.getItemCount();		        
			for (int i = 0; i < itemCount; i++)
			{		
				xyDataSet.add(series.getTimePeriod(i), series.getValue(i), (String)series.getKey(), false);
			} 
		}

		// replace with new dataset and renderer
		StackedXYAreaRenderer2 renderer = new StackedXYAreaRenderer2();
		plot.setDataset(xyDataSet);
		plot.setRenderer(renderer);
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		plot.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);

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
		if (rangeAxis.isAutoRange() == false) {
			if (!"%".equals(rangeAxis.getLabel())) {
				if (rangeAxis.getRange().getUpperBound() > 5.0E-9) {
					rangeAxis.setAutoRange(true);
					log.debug("force AutoRange to true");
				} else {
					NumberAxis axis = (NumberAxis)rangeAxis;
					axis.setNumberFormatOverride(new DecimalFormat("#0"));
				}
			}
		}
	}
	
}
