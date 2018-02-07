/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.traputil.dialog;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.traputil.action.MibManager;
import com.clustercontrol.utility.traputil.bean.SnmpTrapMasterInfo;
import com.clustercontrol.utility.traputil.composite.MibListComposite;

/**
 * MIBインポート押下時に起動するベースとなるダイアログ
 * 
 * @version 6.1.0
 * @since 5.0.a
 * 
 */
public class ImportDialog extends CommonDialog {
		private MibListComposite mibListComposite = null;

	
	public ImportDialog(Shell parent) {
		super(parent);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(980, 800);
	}

	protected Shell shell;
	
	protected String managerName = "";
	protected String monitorId = "";
	
	public String getManagerName() {
		return managerName;
	}

	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}

	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	@Override
	protected void customizeDialog(Composite parent) {
		shell = this.getShell();
		
		// 変数として利用されるラベル
		Label label = null;
		
		// 変数として利用されるグリッドデータ
		GridData gridData = null;
		
		// タイトル
		shell.setText(Messages.getString("dialog.traputil.import.title"));
		
		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);

		// ヘッダの情報
		Composite cmpHead = new Composite(parent, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 5;
		cmpHead.setLayout(layout);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		cmpHead.setLayoutData(gridData);

		// ラベルコンポジット
		Composite cmpLable = new Composite(cmpHead, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		cmpLable.setLayout(layout);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		cmpLable.setLayoutData(gridData);
		
		// マネージャタイトルラベル
		label = new Label(cmpLable, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		gridData.horizontalSpan = 1;
		label.setLayoutData(gridData);
		label.setText(Messages.getString(com.clustercontrol.util.Messages.getString("facility.managername") + ":"));

		// マネージャラベル
		label = new Label(cmpLable, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		label.setLayoutData(gridData);
		label.setText(getManagerName());

		// 監視IDタイトルラベル
		label = new Label(cmpLable, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		gridData.horizontalSpan = 1;
		label.setLayoutData(gridData);
		label.setText(com.clustercontrol.util.Messages.getString("monitor.id") + ":");

		// 監視IDラベル
		label = new Label(cmpLable, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		label.setLayoutData(gridData);
		label.setText(getMonitorId());
		
		
		mibListComposite  = new MibListComposite(parent, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		mibListComposite.setLayoutData(gridData);
		
		//インポートビューの画面の更新時はマネージャと同期せず
		//クライアント内のみのデータを再描画する
		mibListComposite.setSyncMode(false);
		
		
		mibListComposite.getMibNameComposite().getMibList().addSelectionChangedListener(new ISelectionChangedListener(){
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
			}
		});
		
		mibListComposite.getMibDetailTable().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
			}
		});

		// ボトムの情報
		Composite cmpBottom = new Composite(parent, SWT.NONE);
		layout = new GridLayout(2, true);
		layout.marginWidth = 0;
		layout.marginHeight = 5;
		cmpBottom.setLayout(layout);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		cmpBottom.setLayoutData(gridData);

		new Label(cmpBottom, SWT.NONE);
		
		// ボタンコンポジット
		Composite cmpButton = new Composite(cmpBottom, SWT.NONE);
		layout = new GridLayout(3, true);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		cmpButton.setLayout(layout);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		cmpButton.setLayoutData(gridData);
		
		Button btnAddMibFile = new Button(cmpButton, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		btnAddMibFile.setLayoutData(gridData);
		btnAddMibFile.setText(Messages.getString("button.traputil.read"));
		btnAddMibFile.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				// ダイアログ表示、取得したコンポジットをセット
				MibFileInputDialog dialog = new MibFileInputDialog(shell);
				dialog.setMibListComposite(mibListComposite);
				dialog.open();
				mibListComposite.update();
			}
		});
		
		Button btnClearMib = new Button(cmpButton, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		btnClearMib.setLayoutData(gridData);
		btnClearMib.setText(Messages.getString("button.traputil.clear"));
		btnClearMib.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				//ファイルから取得したMIB情報のクリア
				MibManager.getInstance(false).clearMibData();
				mibListComposite.update();
			}
		});
		
		Button btnDeleteOid = new Button(cmpButton, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		btnDeleteOid.setLayoutData(gridData);
		btnDeleteOid.setText(Messages.getString("button.traputil.delete"));
		btnDeleteOid.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				ArrayList<SnmpTrapMasterInfo> list = mibListComposite.getSelectDetails();
				
				// 選択しているMIB詳細情報を削除して再描画
				if (list != null && list.size() != 0) {
					//削除確認
					if(MessageDialog.openQuestion(null,
							Messages.getString("message"),
							Messages.getString("message.traputil.14"))){
						ArrayList<String> mibs = null;
						try {
							mibs = MibManager.getInstance(false).deleteMibDetails(list);
						} catch (InvocationTargetException e1) {
							MessageDialog.openError(
									null,
									com.clustercontrol.util.Messages.getString("failed"),
									com.clustercontrol.util.Messages.getString("message.hinemos.failure.unexpected") + ", " + e1.getCause().getMessage());
						}
						
						//MIB一覧情報まで削除したかで再描画範囲を変更する
						if(mibs == null){
							//MIB詳細のみ再描画
							mibListComposite.getMibDetailComposite().update();
						}
						else {
							MessageDialog.openInformation(null,
									Messages.getString("message"),
									Messages.getString("message.traputil.15")
									+ mibs);

							//MIBインポートビュー全体を再描画
							mibListComposite.update();
						}
					}
				}
			}
		});
		
		// ラインを引く
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		line.setLayoutData(gridData);
		
		//ダイアログのサイズ調整（pack:resize to be its preferred size）
//		shell.pack();
//		shell.setSize(new Point(shell.getSize().x, shell.getSize().y));

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);
	}
	
	
	
	
	@Override
	protected boolean action() {
		
		//MIB情報が空の場合は何もせず終了
		if(validate() != null) {
			return false;
		}
		
		/*
		 * インポート処理
		 */
		// ダイアログ表示
		MibImportDialog dialog = new MibImportDialog(shell, monitorId);
		dialog.setDetailList(mibListComposite.getMibDetailComposite().getSelectDetails());
		
		if(dialog.open() == Window.OK){
			mibList = dialog.getMibList();
			
			return true;
		} else {
			return false;
		}
	}
	
	List<SnmpTrapMasterInfo> mibList;
	
	public List<SnmpTrapMasterInfo> getMibList(){
		return mibList;
	}
	
	protected void update(){
		
	}
	
	/**
	 * ＯＫボタンのテキストを返します。
	 * 
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("button.traputil.import");
	}
	
	/**
	 * キャンセルボタンのテキストを返します。
	 * 
	 * @return キャンセルボタンのテキスト
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("close");
	}
	
}
