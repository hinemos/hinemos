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
 * 連携情報のAzure向けコンポジット<BR>
 */
public class NotifyCloudLinkInfoAzureComposite extends AbstractNotifyCloudLinkInfoComposite {

	public NotifyCloudLinkInfoAzureComposite(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	public void reflectInputData() {
		m_endpoint.setText(eventBus);
		m_accessKey.setText(accessKey);
		m_subject.setText(detailType);
		m_eventType.setText(source);
		m_dataVersion.setText(dataVersion);

		if (linkInfoDataMap != null) {
			notifyCloudLinkDataInfoComposite.setInfoLinkDataList(linkInfoDataMap);
			notifyCloudLinkDataInfoComposite.update();
		}
	}

	@Override
	public void setInputData() {

		eventBus = m_endpoint.getText();
		accessKey = m_accessKey.getText();
		detailType = m_subject.getText();
		source = m_eventType.getText();
		dataVersion = m_dataVersion.getText();

	}

	@Override
	public void update() {
		// endpoit
		if (m_endpoint.getText().isEmpty()) {
			m_endpoint.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			m_endpoint.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// accesskey
		if (m_accessKey.getText().isEmpty()) {
			m_accessKey.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			m_accessKey.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// subject
		if (m_subject.getText().isEmpty()) {
			m_subject.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			m_subject.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// eventtype
		if (m_eventType.getText().isEmpty()) {
			m_eventType.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			m_eventType.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
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
		label.setText(Messages.getString("notifies.cloud.endpoint"));

		m_endpoint = new Text(sendGroup, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_endpoint.setLayoutData(gridData);
		m_endpoint.setText("");
		m_endpoint.setToolTipText(tooltipText);
		m_endpoint.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		label = new Label(sendGroup, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("notifies.cloud.accesskey"));

		m_accessKey = new Text(sendGroup, SWT.BORDER | SWT.PASSWORD);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_accessKey.setLayoutData(gridData);
		m_accessKey.setText("");
		m_accessKey.setToolTipText(tooltipText);
		m_accessKey.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

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
		label.setText(Messages.getString("notifies.cloud.subject"));

		m_subject = new Text(eventGroup, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_subject.setLayoutData(gridData);
		m_subject.setText("");
		m_subject.setToolTipText(tooltipText);
		m_subject.addModifyListener(new ModifyListener() {
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
		label.setText(Messages.getString("notifies.cloud.event.type"));

		m_eventType = new Text(eventGroup, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_eventType.setLayoutData(gridData);
		m_eventType.setText("");
		m_eventType.setToolTipText(tooltipText);
		m_eventType.addModifyListener(new ModifyListener() {
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
		label.setText(Messages.getString("notifies.cloud.data.version"));

		m_dataVersion = new Text(eventGroup, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_dataVersion.setLayoutData(gridData);
		m_dataVersion.setText("");
		m_dataVersion.setToolTipText(tooltipText);

		return eventGroup;
	}

	@Override
	public String getTableMessage() {
		return 	Messages.getString("notifies.cloud.data");
	}
}
