/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.bean;

import java.util.ArrayList;

import com.clustercontrol.util.Messages;

import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Panel;
import org.eclipse.swt.graphics.Color;


public class ColorfulAssociation extends Association {
	private static final long serialVersionUID = -1271305038780048007L;
	private final transient Color defaultColor = new Color(null, 120, 120, 120);
	private transient Color lineColor = defaultColor;
	private transient Color labelColor = defaultColor;
	private String label = "";
	private String targetAdjacentLabel = null;
	private String orderToolTip = null;
	private ArrayList<String> tooltipList = new ArrayList<String>();

	public Color getLineColor() {
		return lineColor;
	}

	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}

	public Color getLabelColor() {
		return labelColor;
	}

	public void setLabelColor(Color labelColor) {
		this.labelColor = labelColor;
	}

	public ColorfulAssociation(String source, String target) {
		super(source, target);
	}

	public void setDefaultColor() {
		lineColor = defaultColor;
	}

	public void addTooltip(String str) {
		tooltipList.add(str);
	}

	public Panel getToolTip() {
		Panel tooltip = new Panel();
		tooltip.setLayoutManager(new FlowLayout(false));

		tooltip.add(new Label(Messages.getString("wait.rule") + " : "));

		tooltip.add(new Label(getSource() + " → " + getTarget()));

		// ツールチップ
		for (String str : tooltipList) {
			Panel subPanel = new Panel();
			subPanel.setLayoutManager(new FlowLayout(true));
			subPanel.add(new Label(str));
			tooltip.add(subPanel);
		}
		if(orderToolTip != null){
			Panel subPanel = new Panel();
			subPanel.setLayoutManager(new FlowLayout(true));
			subPanel.add(new Label(orderToolTip));
			tooltip.add(subPanel);
		}

		return tooltip;
	}

	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((tooltipList == null) ? 0 : tooltipList.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ColorfulAssociation other = (ColorfulAssociation) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (tooltipList == null) {
			if (other.tooltipList != null)
				return false;
		} else if (!tooltipList.equals(other.tooltipList))
			return false;
		return true;
	}

	public String getTargetAdjacentLabel() {
		return this.targetAdjacentLabel;
	}

	public void setTargetAdjacentLabel(String targetAdjacentLabel) {
		this.targetAdjacentLabel = targetAdjacentLabel;
	}

	public void setOrderToolTip(String str) {
		orderToolTip = str;
	}
	
}
