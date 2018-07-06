/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.traputil.dialog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.traputil.action.GetImportMibDetailTableDefine;
import com.clustercontrol.utility.traputil.bean.ImportMibDetailTableDefine;
import com.clustercontrol.utility.traputil.bean.ImportStatus;
import com.clustercontrol.utility.traputil.bean.SnmpTrapMasterInfo;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.ws.monitor.TrapValueInfo;

/**
 * MIBインポート押下時に起動するダイアログ
 * 
 * @version 6.1.0
 * @sincce 2.4.0
 * 
 */
public class MibImportDialog extends CommonDialog {
	
	/*ロガー*/
	private Log log = LogFactory.getLog(getClass());
	
	/** インポートテーブル */
	private CommonTableViewer importTableViewer = null;
	
	/** インポートビューで選択されたMIB詳細リスト */
	private List<SnmpTrapMasterInfo> detailList = null;
	
	private ArrayList<SnmpTrapMasterInfo> mibList = new ArrayList<>();
	
	private String monitorId;
	
	public MibImportDialog(Shell parent, String monitorId) {
		super(parent);
		this.monitorId = monitorId;
	}

	public void setDetailList(List<SnmpTrapMasterInfo> detailList){
		this.detailList = detailList;
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(700, 600);
	}

	@Override
	protected void customizeDialog(Composite parent) {
		
		Shell shell = this.getShell();
		
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
		layout.numColumns = 1;
		parent.setLayout(layout);
		
		/*
		 * 実行予定・結果テーブル
		 */
		// ラベル
		label = new Label(parent, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("dialog.traputil.import.label.1"));
		
		//テーブル
		importTableViewer = new ImportDialogTableViewer(parent,
				SWT.FILL | SWT.V_SCROLL |SWT.H_SCROLL |SWT.BORDER);
		// テーブルビューアの作成
        importTableViewer.createTableColumn(GetImportMibDetailTableDefine.get(),
        		ImportMibDetailTableDefine.SORT_COLUMN_INDEX,
        		ImportMibDetailTableDefine.SORT_ORDER);
		importTableViewer.getTable().setHeaderVisible(true);
		importTableViewer.getTable().setLinesVisible(true);
		
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		importTableViewer.getTable().setLayoutData(gridData);

		
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
		
		//データ表示
		fillTable(collectionToArray(this.detailList,null));

	}
	
	/**
	 * 選択されたMIB情報を実行ステータスと共に表示する
	 *
	 */
	private void fillTable(ArrayList<Object> inputList) {
		
		if(validate() != null) {
			//MIB情報が空ならなにもしない
		}
		else {
			this.importTableViewer.setInput(inputList);
		}
	}
	
    @Override
	protected boolean action() {
		
		//MIB情報が空の場合は何もせず終了
		if(validate() != null) {
			return false;
		}
		
		//マスター編集ビュー更新
		MessageDialog.openInformation(null,
				Messages.getString("message"),
				Messages.getString("message.traputil.16"));
		
		//closeボタンのみ有効化
    	this.getButton(IDialogConstants.CANCEL_ID).setEnabled(true);
		
		return true;
	}
	
    public List<SnmpTrapMasterInfo> getMibList(){
    	return mibList;
    }
	
	/**
	 * インポート実行状態とマスタ情報をObjectの2次元配列に格納
	 * 
	 * @param detailList
	 * @return
	 */
	private ArrayList<Object> collectionToArray(List<SnmpTrapMasterInfo> detailList,ImportStatus status) {
		
		ArrayList<Object> list = new ArrayList<>();
		ImportStatus importStatus = status;
		if(importStatus == null) {
			importStatus = ImportStatus.TYPE_WAIT;
		}
		
		if(detailList != null){
		
			Set<String> registeredSet = new HashSet<>();
			try {
				MonitorInfo info = MonitorSettingEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMonitor(monitorId);
				for(TrapValueInfo trap: info.getTrapCheckInfo().getTrapValueInfos()){
					registeredSet.add(trap.getTrapOid() + trap.getGenericId() + trap.getSpecificId() + trap.getMib());
				}
			} catch (HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception | MonitorNotFound_Exception e) {
				log.error(e);
				return list;
			}
			
			Iterator<SnmpTrapMasterInfo> itr = detailList.iterator();
			while(itr.hasNext())
			{
				SnmpTrapMasterInfo detail = (SnmpTrapMasterInfo)itr.next();
				if(registeredSet.contains(detail.getTrapOid() + detail.getGenericId() + detail.getSpecificId() + detail.getMib())){
					importStatus = ImportStatus.TYPE_REGISTERED;
				} else {
					mibList.add(detail);
					importStatus = ImportStatus.TYPE_WAIT;
				}
				list.add(collectionToArrayElement(detail, importStatus));
			}
		}
		return list;
	}

	/**
	 * インポート実行状態とマスタ情報の1セットをObjectの
	 * 2次元配列の1エレメントに格納
	 * 
	 * @param detail, status
	 * @return
	 */
	private ArrayList<Object> collectionToArrayElement(SnmpTrapMasterInfo detail, ImportStatus status) {
		
		ArrayList<Object> info = new ArrayList<>();
		
		if(detail != null){
			
			info.add(status);
			info.add(detail.getMib());
			info.add(detail.getUei());
			info.add(detail.getTrapOid());
			info.add(Integer.valueOf(detail.getGenericId()));
			info.add(Integer.valueOf(detail.getSpecificId()));
			info.add(Integer.valueOf(detail.getPriority()));
			info.add(detail.getLogmsg());
			
		}
		return info;
	}

	@Override
	protected ValidateResult validate() {
		ValidateResult validateResult = null;
		
		//コンストラクタで取得したMIB情報が空かどうか
		if(	(this.detailList == null || this.detailList.size() == 0) ) {
			
			validateResult = new ValidateResult();
			validateResult.setValid(false);
			validateResult.setID(Messages.getString("message.hinemos.1"));
			validateResult.setMessage(Messages.getString("message.traputil.13"));
		}
		
		return validateResult;
	}
	
	
	@Override
	protected void okPressed() {
    	ValidateResult result = this.validate();
        
        if (result == null || result.isValid()) {
        	
        	this.getButton(IDialogConstants.OK_ID).setEnabled(false);
        	this.getButton(IDialogConstants.CANCEL_ID).setEnabled(false);
        	
        	this.action();
        	
        } else {
            this.displayError(result);
        }
        
        close();
	}

	@Override
	protected void cancelPressed() {
		mibList = null;
		super.cancelPressed();
	}
	
	/**
	 * ＯＫボタンのテキストを返します。
	 * 
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("run");
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
