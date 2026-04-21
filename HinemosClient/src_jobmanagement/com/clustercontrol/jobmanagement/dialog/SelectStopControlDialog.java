/*
 * Copyright (c) 2026 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.dialog;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.JobOperationPropResponse.AvailableOperationListEnum;
import org.openapitools.client.model.JobOperationRequest.EndStatusEnum;

import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.jobmanagement.composite.DetailComposite.JobElement;
import com.clustercontrol.jobmanagement.view.action.StartSelectedJobDetailAction;
import com.clustercontrol.util.Messages;
import com.clustercontrol.xcloud.util.ControlUtil;
import com.clustercontrol.xcloud.util.validation.ControlValidator;
import com.clustercontrol.xcloud.util.validation.ValidateException;
import com.clustercontrol.xcloud.util.validation.annotation.RequiredInput;

/*
 * ジョブ履歴[ジョブ詳細]ビューで選択されたジョブの停止方法を指定するダイアログ
 */
public class SelectStopControlDialog extends AbstractSelectControlDialog {
	private static Log log = LogFactory.getLog(StartSelectedJobDetailAction.class);

	protected static class Viewer extends AbstractViewer {
		private static Log log = LogFactory.getLog(Viewer.class);
		
		public Viewer(Composite parent, Root root) {
			super(parent, root);
		}
		
		@Override
		protected String getImageKey(boolean enable) {
			return enable ? "IMG_CANCEL_KEY_ENABLE": "IMG_CANCEL_KEY_DISABLE";
		}

		@Override
		protected String getImagePath(boolean enable) {
			return enable ? "icons/enable/cancel.png": "icons/disable/cancel.png";
		}
		
		@Override
		protected Log getLogger() {
			return log;
		}
	}
	
	public SelectStopControlDialog(Shell parent, Set<JobElement> selected, Map<AvailableOperationListEnum, Set<JobElement>> jobMapByOpe) {
		super(parent, selected, jobMapByOpe);
	}

	@Override
	protected String getDialogTitle() {
		return Messages.getString("dialog.job.stop.selected.job");
	}

	@Override
	protected Log getLogger() {
		return log;
	}

	@Override
	protected Viewer createViewer(Composite parent, Root root) {
		return new Viewer(parent, root);
	}
	
	/*
	 * 終了値を更新する際に、終了値を指定するダイアログ
	 */
	protected static class Dialog extends CommonDialog {
		private Optional<EndStatusEnum> endStatus = Optional.empty();
		private Optional<Integer> endValue = Optional.empty();
		
		private Combo cmbControl;
		
		@RequiredInput
		private Text txtEndValue;
		
		public Dialog(Shell parent) {
			super(parent);
			setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		}
		
		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText("ジョブ[変更値入力]");
		}

		
		@Override
		protected void customizeDialog(Composite parent) {
			Locale locale = Locale.getDefault();
			
			GridData gd_parent = new GridData(SWT.FILL, SWT.FILL, true, false);
			parent.setLayoutData(gd_parent);
			
			GridLayout layout = new GridLayout(2, false);
			layout.marginHeight = 10;
			layout.marginWidth = 10;
			layout.marginRight = 10;
			layout.marginLeft = 10;
			parent.setLayout(layout);
			
			Label lblControl = new Label(parent, SWT.RIGHT);
			lblControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblControl.setText(Messages.getString("end.status", locale) + Messages.getString("caption.title_separator", locale));
			
			cmbControl = new Combo(parent, SWT.READ_ONLY);
			cmbControl.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
			cmbControl.setData(ControlValidator.labelKey, Messages.getString("end.status", locale));
			
			cmbControl.add("");
			
			cmbControl.add(EndStatusMessage.STRING_NORMAL);
			cmbControl.setData(EndStatusMessage.STRING_NORMAL, EndStatusEnum.NORMAL);

			cmbControl.add(EndStatusMessage.STRING_WARNING);
			cmbControl.setData(EndStatusMessage.STRING_WARNING, EndStatusEnum.WARNING);

			cmbControl.add(EndStatusMessage.STRING_ABNORMAL);
			cmbControl.setData(EndStatusMessage.STRING_ABNORMAL, EndStatusEnum.ABNORMAL);
			
			cmbControl.select(0);
			
			Label lblTargets = new Label(parent, SWT.RIGHT);
			lblTargets.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblTargets.setText(Messages.getString("end.value", locale) + Messages.getString("caption.title_separator", locale));
			
			txtEndValue = new Text(parent, SWT.BORDER);
			txtEndValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,  true,  false, 1, 1));
			txtEndValue.setData(ControlValidator.labelKey, Messages.getString("end.value", locale));
			
			txtEndValue.addVerifyListener(e->{
					Text text = (Text)e.getSource();
					String newText = text.getText().substring(0, e.start) + e.text + text.getText().substring(e.end);
					if (newText.isEmpty() || newText.equals("-")) {
						e.doit = true;
					} else  {
						e.doit = newText.matches("-?\\d*?");
					}
				});
			
			ControlUtil.setRequired(new Control[]{
					txtEndValue
				});
		}
		
		@Override
		protected void okPressed() {
			try{
				ControlUtil.validate(this);
			} catch(ValidateException e){
				MessageDialog.openError(null, Messages.getString("failed"), e.getMessage());
				return;
			} catch(Exception e){
				MessageDialog.openError(null, Messages.getString("failed"), e.getMessage());
				throw new IllegalStateException("Unexpected error while validating SelectStopControlDialog input.", e);
			}
			
			endStatus = Optional.ofNullable((EndStatusEnum)cmbControl.getData(cmbControl.getItem(cmbControl.getSelectionIndex())));
			endValue = Optional.of(Integer.valueOf(txtEndValue.getText()));
			
			super.okPressed();
		}
		
		public Optional<EndStatusEnum> getEndStatus() {
			return endStatus;
		}
		
		public Optional<Integer> getEndValue() {
			return endValue;
		}
	}
	
	@Override
	protected void okPressed() {
		if (root.getOperation().isPresent()) {
			if (AvailableOperationListEnum.STOP_MAINTENANCE.equals(root.getOperation().get()) ||
				AvailableOperationListEnum.STOP_FORCE.equals(root.getOperation().get())
				) {
				Dialog dialog = new Dialog(getShell());
				if (dialog.open() == OK) {
					root.setNewEndStatus(dialog.getEndStatus().orElse(null));
					root.setNewEndValue(dialog.getEndValue().orElse(null));
					super.okPressed();
				}
			} else {
				super.okPressed();
			}
		} else {
			throw new IllegalStateException();
		}
	}
}
