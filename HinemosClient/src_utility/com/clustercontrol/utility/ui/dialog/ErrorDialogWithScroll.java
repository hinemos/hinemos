/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.ui.dialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ErrorDialogWithScroll extends ErrorDialog {

	public ErrorDialogWithScroll(Shell parentShell, String dialogTitle, String message, IStatus status,
			int displayMask) {
		super(parentShell, dialogTitle, message, status, displayMask);
	}

	protected Text messageText = null;
	
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
			//messageText  = new Text(composite, getMessageLabelStyle());
			messageText  = new Text(composite, SWT.V_SCROLL);
			messageText.setText(message);
			messageText.setEditable(false);
			
			//データ量で高さを調整する
			int height = message.split("\n").length > 17 ? 300 : SWT.DEFAULT;
			
			GridDataFactory
					.fillDefaults()
					.align(SWT.FILL, SWT.BEGINNING)
					.grab(true, false)
					.hint(
							convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH),
							height).applyTo(messageText);
					
			
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
	
    
	//Copy from org.eclipse.jface.dialogs.ErrorDialog
	
	/**
	 * Opens an error dialog to display the given error. Use this method if the
	 * error object being displayed does not contain child items, or if you wish
	 * to display all such items without filtering.
	 * 
	 * @param parent
	 *            the parent shell of the dialog, or <code>null</code> if none
	 * @param dialogTitle
	 *            the title to use for this dialog, or <code>null</code> to
	 *            indicate that the default title should be used
	 * @param message
	 *            the message to show in this dialog, or <code>null</code> to
	 *            indicate that the error's message should be shown as the
	 *            primary message
	 * @param status
	 *            the error to show to the user
	 * @return the code of the button that was pressed that resulted in this
	 *         dialog closing. This will be <code>Dialog.OK</code> if the OK
	 *         button was pressed, or <code>Dialog.CANCEL</code> if this
	 *         dialog's close window decoration or the ESC key was used.
	 */
	public static int openError(Shell parent, String dialogTitle,
			String message, IStatus status) {
		return openError(parent, dialogTitle, message, status, IStatus.OK
				| IStatus.INFO | IStatus.WARNING | IStatus.ERROR);
	}

	/**
	 * Opens an error dialog to display the given error. Use this method if the
	 * error object being displayed contains child items <it>and </it> you wish
	 * to specify a mask which will be used to filter the displaying of these
	 * children. The error dialog will only be displayed if there is at least
	 * one child status matching the mask.
	 * 
	 * @param parentShell
	 *            the parent shell of the dialog, or <code>null</code> if none
	 * @param title
	 *            the title to use for this dialog, or <code>null</code> to
	 *            indicate that the default title should be used
	 * @param message
	 *            the message to show in this dialog, or <code>null</code> to
	 *            indicate that the error's message should be shown as the
	 *            primary message
	 * @param status
	 *            the error to show to the user
	 * @param displayMask
	 *            the mask to use to filter the displaying of child items, as
	 *            per <code>IStatus.matches</code>
	 * @return the code of the button that was pressed that resulted in this
	 *         dialog closing. This will be <code>Dialog.OK</code> if the OK
	 *         button was pressed, or <code>Dialog.CANCEL</code> if this
	 *         dialog's close window decoration or the ESC key was used.
	 * @see org.eclipse.core.runtime.IStatus#matches(int)
	 */
	public static int openError(Shell parentShell, String title,
			String message, IStatus status, int displayMask) {
		
		ErrorDialogWithScroll dialog = new ErrorDialogWithScroll(parentShell, title, message,
				status, displayMask);
		return dialog.open();
	}

}
