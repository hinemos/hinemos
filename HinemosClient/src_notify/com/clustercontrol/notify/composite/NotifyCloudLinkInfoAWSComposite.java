/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.util.Messages;

/**
 * 連携情報のAWS向けコンポジット<BR>
 */
public class NotifyCloudLinkInfoAWSComposite extends AbstractNotifyCloudLinkInfoComposite {

	public NotifyCloudLinkInfoAWSComposite(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	public void reflectInputData() {

		m_eventBus.setText(eventBus);
		m_detailType.setText(detailType);
		m_source.setText(source);

		if (linkInfoDataMap != null) {
			notifyCloudLinkDataInfoComposite.setInfoLinkDataList(linkInfoDataMap);
			notifyCloudLinkDataInfoComposite.update();
		}
	}

	@Override
	public void setInputData() {
		eventBus = m_eventBus.getText();
		detailType = m_detailType.getText();
		source = m_source.getText();
	}

	@Override
	public void update() {
		// source
		if (m_source.getText().isEmpty()) {
			m_source.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			m_source.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// detail type
		if (m_detailType.getText().isEmpty()) {
			m_detailType.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			m_detailType.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

	}

	@Override
	public Composite createComposite(Composite parent) {
		// ツールチップテキスト
		String tooltipText = Messages.getString("notify.parameter.tooltip")
				+ Messages.getString("replace.parameter.notify") + Messages.getString("replace.parameter.node");

		// クラウドグループ
		Group sendGroup = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 10;
		sendGroup.setLayout(layout);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		sendGroup.setLayoutData(gridData);
		sendGroup.setText(Messages.getString("notifies.cloud.sendto") + " : ");

		Label label = new Label(sendGroup, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("notifies.cloud.eventbus"));

		m_eventBus = new Text(sendGroup, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_eventBus.setLayoutData(gridData);
		m_eventBus.setText("");
		m_eventBus.setToolTipText(tooltipText);

		// 空行
		label = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// イベント情報
		Group eventGroup = new Group(parent, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 10;
		eventGroup.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		eventGroup.setLayoutData(gridData);
		eventGroup.setText(Messages.getString("notifies.cloud.event.info") + " : ");

		label = new Label(eventGroup, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("notifies.cloud.detail.type"));

		m_detailType = new Text(eventGroup, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_detailType.setLayoutData(gridData);
		m_detailType.setText("");
		m_detailType.setToolTipText(tooltipText);
		m_detailType.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		label = new Label(eventGroup, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("notifies.cloud.source"));

		m_source = new Text(eventGroup, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_source.setLayoutData(gridData);
		m_source.setText("");
		m_source.setToolTipText(tooltipText);
		m_source.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		return eventGroup;
	}


	@Override
	public String getTableMessage() {
		return 	Messages.getString("notifies.cloud.detail");
	}
}
