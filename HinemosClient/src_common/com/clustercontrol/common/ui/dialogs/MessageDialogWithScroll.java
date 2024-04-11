/*
 * Copyright (c) 2023 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.common.ui.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.util.Messages;

/**
 * メッセージダイアログ (メッセージ部分をスクロール表示)<br>
 * 
 * MessageDialogWithCheckboxを流用
 */
public class MessageDialogWithScroll extends MessageDialog {

	protected Text messageText = null;
	
	public MessageDialogWithScroll(String title) {
		super(
				null,
				Messages.getString("message.confirm"),
				null,
				title,
				MessageDialog.CONFIRM,
				new String[]{"Ok","Cancel"},
				0
			);
	}

	//Constructor for Information Dialog
	public MessageDialogWithScroll(String dialogTitle, String dialogMessage){
		super(
				null,
				dialogTitle,
				null,
				dialogMessage,
				MessageDialog.INFORMATION,
				new String[]{"Ok"},
				0
				);
	}
	@Override
	protected Control createMessageArea(Composite composite) {
		//LabelをTextに差し替えるため、IconAndMessageDialogより移植
		
		// create composite
		// create image
		Image image = getImage();
		if (image != null) {
			imageLabel = new Label(composite, SWT.NULL);
			image.setBackground(imageLabel.getBackground());
			imageLabel.setImage(image);
			addAccessibleListeners(imageLabel, image);
			GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.BEGINNING)
					.applyTo(imageLabel);
		}
		// create message
		if (message != null) {
			
			//データ量が多いとき、スクロールバー表示とするため、Textを使用する
			messageText  = new Text(composite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.WRAP);
			messageText.setText(message);
			messageText.setEditable(false);
			
			//データ量で高さを調整する
			int height = SWT.DEFAULT;
			if(message.length() > 120) {
				height = 85;
			}

			GridDataFactory
					.fillDefaults()
					.align(SWT.FILL, SWT.BEGINNING)
					.grab(true, false)
					.hint( convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH)
							,height)
					.applyTo(messageText);
					
			
			return composite;
		}
		return composite;
	}
	
	//LabelをTextに差し替えるため、IconAndMessageDialogより移植
	/**
	 * Add an accessible listener to the label if it can be inferred from the
	 * image.
	 * 
	 * @param label
	 * @param image
	 */
	private void addAccessibleListeners(Label label, final Image image) {
		label.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			public void getName(AccessibleEvent event) {
				final String accessibleMessage = getAccessibleMessageFor(image);
				if (accessibleMessage == null) {
					return;
				}
				event.result = accessibleMessage;
			}
		});
	}
	
	//LabelをTextに差し替えるため、IconAndMessageDialogより移植
	private String getAccessibleMessageFor(Image image) {
		if (image.equals(getErrorImage())) {
			return JFaceResources.getString("error");//$NON-NLS-1$
		}

		if (image.equals(getWarningImage())) {
			return JFaceResources.getString("warning");//$NON-NLS-1$
		}

		if (image.equals(getInfoImage())) {
			return JFaceResources.getString("info");//$NON-NLS-1$
		}

		if (image.equals(getQuestionImage())) {
			return JFaceResources.getString("question"); //$NON-NLS-1$
		}

		return null;
	}
	
	@Override
	protected Control createCustomArea(Composite parent) {
		// このエリアのコンポジット生成。
		Composite thisArea = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginLeft = 40;
		thisArea.setLayout(gridLayout);
		return thisArea;
	}

	public boolean openConfirm() {
		boolean ret=false;
		switch(this.open()) {
		case 0:
			//Ok
			ret=true;
			break;
		case 1:
			//Cancel
			break;
		}
		return ret;
	}
}
