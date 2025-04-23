/*
 * Copyright (c) 2024 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

public class NotifyCloudLinkInfoGCPComposite extends AbstractNotifyCloudLinkInfoComposite {

	public NotifyCloudLinkInfoGCPComposite(Composite parent, int style) {
		super(parent, style);

	}

	@Override
	public void reflectInputData() {

		m_projectId.setText(eventBus);
		m_topicId.setText(source);
		// Checking the condition to enable the checkbox depending on
		// orderingKey(dataVersion)
		if (!dataVersion.isEmpty()) {
			m_useOrdering.setSelection(true);
		} else {
			m_useOrdering.setSelection(false);
		}
		m_message.setText(detailType);
		m_orderingKey.setText(dataVersion);
		if (linkInfoDataMap != null) {
			notifyCloudLinkDataInfoComposite.setInfoLinkDataList(linkInfoDataMap);
			notifyCloudLinkDataInfoComposite.update();
		}

	}

	@Override
	public void setInputData() {
		eventBus = m_projectId.getText();
		source = m_topicId.getText();
		detailType = m_message.getText();
		dataVersion = m_orderingKey.getText();
		
	}
	
	@Override
	public boolean isValidate() {
		if (m_useOrdering.getSelection() && (dataVersion == null || dataVersion.isEmpty())) {
			return true;
		}
		return false;
	}

    
	@Override
	public void update() {
		// projectid
		if (m_projectId.getText().isEmpty()) {
			m_projectId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			m_projectId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// topicid
		if (m_topicId.getText().isEmpty()) {
			m_topicId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			m_topicId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// useordering
		if (m_useOrdering.getSelection()) {
			m_orderingKey.setEnabled(true);
			m_orderingKey.setEditable(true);
			if (m_orderingKey.getText().equals("")) {
				m_orderingKey.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			} else {
				m_orderingKey.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
	
		} else {
			m_orderingKey.setEnabled(false);
			m_orderingKey.setEditable(false);
			m_orderingKey.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// orderingkey
		if (m_orderingKey.getText().isEmpty() && m_useOrdering.getSelection()) {
			m_orderingKey.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);

		} else {
			m_orderingKey.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
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
		label.setText(Messages.getString("notifies.cloud.projectid"));

		m_projectId = new Text(sendGroup, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_projectId.setLayoutData(gridData);
		m_projectId.setText("");
		m_projectId.setToolTipText(tooltipText);
		m_projectId.addModifyListener(new ModifyListener() {
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
		label.setText(Messages.getString("notifies.cloud.topicid"));

		m_topicId = new Text(sendGroup, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_topicId.setLayoutData(gridData);
		m_topicId.setText("");
		m_topicId.setToolTipText(tooltipText);
		m_topicId.addModifyListener(new ModifyListener() {
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
		label.setText(Messages.getString("notifies.cloud.message"));

		m_message = new Text(eventGroup, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_message.setLayoutData(gridData);
		m_message.setText("");
		m_message.setToolTipText(tooltipText);
		m_message.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// for use ordering do we need to add check box or something else we
		// need to use,need to confirm that.
		label = new Label(eventGroup, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 7;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("notifies.cloud.use.ordering"));

		m_useOrdering = new Button(eventGroup, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_useOrdering.setLayoutData(gridData);
		m_useOrdering.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				Button check = (Button) event.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_orderingKey.setEnabled(true);
					update();
				}
				if (!check.getSelection()) {
					m_orderingKey.setEnabled(false);
					update();
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		label = new Label(eventGroup, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("notifies.cloud.ordering.key"));

		m_orderingKey = new Text(eventGroup, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_orderingKey.setLayoutData(gridData);
		m_orderingKey.setText("");
		m_orderingKey.setToolTipText(tooltipText);
		m_orderingKey.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		return eventGroup;
	}

	@Override
	public String getTableMessage() {

		return Messages.getString("notifies.cloud.attribute");
	}


}
