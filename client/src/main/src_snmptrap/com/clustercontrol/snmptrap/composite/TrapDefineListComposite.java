/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.snmptrap.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.monitor.preference.MonitorPreferencePage;
import com.clustercontrol.monitor.run.composite.ITableItemCompositeDefine;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.composite.action.StringVerifyListener;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.snmptrap.dialog.SnmpTrapCreateDialog;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.monitor.TrapValueInfo;

/**
 * SNMPTRAP監視OID一覧コンポジットクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class TrapDefineListComposite extends Composite{

	public static final int MAX_COLUMN = SnmpTrapCreateDialog.MAX_COLUMN;
	public static final int WIDTH_TITLE = SnmpTrapCreateDialog.WIDTH_TITLE;
	public static final int WIDTH_VALUE = SnmpTrapCreateDialog.WIDTH_VALUE;
	public static final int WIDTH_TITLE_WIDE = SnmpTrapCreateDialog.WIDTH_TITLE_WIDE;
	public static final int WIDTH_TITLE_SMALL = SnmpTrapCreateDialog.WIDTH_TITLE_SMALL;

	// ----- instance フィールド ----- //

	/** 判定情報一覧 コンポジット。 */
	protected TrapDefineTableComposite infoList = null;

	protected ITableItemCompositeDefine<TrapValueInfo> define = null;

	private Map<TrapValueInfo, String> mibMap = new ConcurrentHashMap<TrapValueInfo, String>();
	private Map<String, List<TrapValueInfo>> trapMap = new ConcurrentHashMap<String, List<TrapValueInfo>>();

	/** 追加 ボタン。 */
	protected Button btnAdd = null;

	/** 変更 ボタン。 */
	protected Button btnModify = null;

	/** 削除 ボタン。 */
	protected Button btnDelete = null;

	/** コピー ボタン。 */
	protected Button btnCopy = null;

	/** MIB */
	protected Combo cmbMib = null;

	/** フィルタ */
	protected Text txtFilter = null;

	/** フィルタクリアボタン */
	protected Button btnClearFilter = null;

	/** 表示ボタン */
	protected Button btnShowOidTable = null;
	private Label labelRecordNumber;

	// ----- コンストラクタ ----- //

	/**
	 * インスタンスを返します。
	 *
	 * @param parent
	 *            親のコンポジット
	 * @param style
	 *            スタイル
	 */
	public TrapDefineListComposite(Composite parent, int style, ITableItemCompositeDefine<TrapValueInfo> define) {
		this(parent, style, define, null);
	}

	public TrapDefineListComposite(Composite parent, int style, ITableItemCompositeDefine<TrapValueInfo> define, List<TrapValueInfo> items) {
		super(parent, style);

		this.define = define;
		if(items != null){
			this.define.initTableItemInfoManager(items);
		} else {
			this.define.initTableItemInfoManager();
		}
		this.initialize();
	}

	// ----- instance メソッド ----- //

	/**
	 * 引数で指定された監視情報の値を、各項目に設定します。
	 *
	 * @param info 設定値として用いる監視情報
	 */
	public void setInputData(List<TrapValueInfo> list) {
		this.infoList.setInputData(list);
		mibMap = new ConcurrentHashMap<TrapValueInfo, String>();
		for(TrapValueInfo info: list){
			mibMap.put(info, info.getMib());
		}
		trapMap = new ConcurrentHashMap<String, List<TrapValueInfo>>();
		for(String mib: new TreeSet<String>(mibMap.values())){
			trapMap.put(mib, new ArrayList<TrapValueInfo>());
		}
		for(TrapValueInfo info: list){
			trapMap.get(info.getMib()).add(info);
		}

		updateMibList();

		// 必須項目を明示
		this.update();
	}

	/**
	 * コンポジットを生成・構築します。
	 */
	protected void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = MAX_COLUMN;

		//変数として利用されるラベル
		Label label = null;

		// グループ
		Group groupFilter = new Group(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "filter", groupFilter);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = MAX_COLUMN;
		groupFilter.setLayout(layout);
		GridData gridData = new GridData();
		gridData.horizontalSpan = MAX_COLUMN;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupFilter.setLayoutData(gridData);
		groupFilter.setText(Messages.getString("filter") + " : ");

		/*
		 * MIB
		 */
		// ラベル
		label = new Label(groupFilter, SWT.NONE);
		WidgetTestUtil.setTestId(this, "mib", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("mib") + " : ");
		// コンボボックス
		this.cmbMib = new Combo(groupFilter, SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL);
		WidgetTestUtil.setTestId(this, "mib", cmbMib);
		gridData = new GridData();
		gridData.horizontalSpan = MAX_COLUMN - WIDTH_TITLE - 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint=30;
		this.cmbMib.setLayoutData(gridData);
		this.cmbMib.setVisibleItemCount(10);
		// MIBリスト取得
		this.cmbMib.add("");
		this.cmbMib.select(0);
		this.cmbMib.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				//表示処理
				update();
			}
		});

		// Utilityオプションがある場合は「インポートボタンへ変更」
		if(true){
			label = new Label(groupFilter, SWT.NONE);
			WidgetTestUtil.setTestId(this, null, label);
			gridData = new GridData();
			gridData.horizontalSpan = 3;
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			label.setLayoutData(gridData);
		}

		/*
		 * フィルタ
		 */
		// ラベル
		label = new Label(groupFilter, SWT.NONE);
		WidgetTestUtil.setTestId(this, "trapname", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("trap.name") + " : ");
		// テキスト
		this.txtFilter = new Text(groupFilter, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "filter", txtFilter);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.txtFilter.setLayoutData(gridData);
		this.txtFilter.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_256));
		// クリアボタン
		this.btnClearFilter = new Button(groupFilter, SWT.NONE);
		WidgetTestUtil.setTestId(this, "clearfilter", btnClearFilter);
		this.btnClearFilter.setText(Messages.getString("clear"));
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.btnClearFilter.setLayoutData(gridData);
		this.btnClearFilter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				txtFilter.setText("");
				update();
			}
		});
		// 表示ボタン
		this.btnShowOidTable = new Button(groupFilter, SWT.NONE);
		WidgetTestUtil.setTestId(this, "oidtable", btnShowOidTable);
		this.btnShowOidTable.setText(Messages.getString("show"));
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.btnShowOidTable.setLayoutData(gridData);
		this.btnShowOidTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//表示処理
				update();
			}
		});

		/*
		 * 文字列監視判定情報一覧
		 */
		this.infoList = new TrapDefineTableComposite(this, SWT.BORDER, this.define);
		WidgetTestUtil.setTestId(this, "pagelist", infoList);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = MAX_COLUMN;
		gridData.heightHint = 150;
		this.infoList.setLayoutData(gridData);
		this.infoList.getTableViewer().addSelectionChangedListener(new ISelectionChangedListener(){
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				checkButtonEnabled();
			}
		});

		/*
		 * 操作ボタン
		 */
		Composite composite = new Composite(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, composite);
		layout = new GridLayout(6, true);
		composite.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = MAX_COLUMN;
		composite.setLayoutData(gridData);

		/*
		 * 件数
		 */
		labelRecordNumber = new Label(composite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "recordNumber", label);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
		labelRecordNumber.setLayoutData(gridData);
		updateRecordNumber(0, 0);
		
		// ボタン右寄せのための処理
		int validButtonCount = 0;

		validButtonCount += (this.define.getButtonOptions() & ITableItemCompositeDefine.ADD) != 0? 1: 0;
		validButtonCount += (this.define.getButtonOptions() & ITableItemCompositeDefine.MODIFY) != 0? 1: 0;
		validButtonCount += (this.define.getButtonOptions() & ITableItemCompositeDefine.DELETE) != 0? 1: 0;
		validButtonCount += (this.define.getButtonOptions() & ITableItemCompositeDefine.COPY) != 0? 1: 0;

		if(4 - validButtonCount > 0){
			label = new Label(composite, SWT.NONE);
			WidgetTestUtil.setTestId(this, "dummy", label);
			gridData = new GridData();
			gridData.horizontalSpan = 4 - validButtonCount;
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			label.setLayoutData(gridData);
		}

		// 追加ボタン
		if((this.define.getButtonOptions() & ITableItemCompositeDefine.ADD) != 0){
			this.btnAdd = this.createButton(composite, Messages.getString("add"));
			WidgetTestUtil.setTestId(this, "add", btnAdd);
			this.btnAdd.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {

					// シェルを取得
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

					CommonDialog dialog = define.createDialog(shell);
					if (dialog.open() == IDialogConstants.OK_ID) {
						define.getTableItemInfoManager().add(define.getCurrentCreatedItem());

						addItem(define.getCurrentCreatedItem());
						updateMibList();
						update();
					}
				}
			});
		}
		// 変更ボタン
		if((this.define.getButtonOptions() & ITableItemCompositeDefine.MODIFY) != 0){
			this.btnModify = this.createButton(composite, Messages.getString("modify"));
			WidgetTestUtil.setTestId(this, "modify", btnModify);
			this.btnModify.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					TrapValueInfo item = getSelectedItem();
					if (item != null) {

						// シェルを取得
						Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

						CommonDialog dialog = define.createDialog(shell, item);
						if (dialog.open() == IDialogConstants.OK_ID) {
							Table table = infoList.getTableViewer().getTable();
							WidgetTestUtil.setTestId(this, "modify", table);
							int selectIndex = table.getSelectionIndex();
							define.getTableItemInfoManager().modify(item, define.getCurrentCreatedItem());
							table.setSelection(selectIndex);
							removeItem(item);
							addItem(define.getCurrentCreatedItem());
							updateMibList();
							update();
						}
					}
					else{
						MessageDialog.openWarning(
								null,
								Messages.getString("warning"),
								Messages.getString("message.monitor.30"));
					}
				}
			});
		}

		// 削除ボタン
		if((this.define.getButtonOptions() & ITableItemCompositeDefine.DELETE) != 0){
			this.btnDelete = this.createButton(composite, Messages.getString("delete"));
			WidgetTestUtil.setTestId(this, "delete", btnDelete);
			this.btnDelete.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					List<TrapValueInfo> infos = getSelectedItems();

					if (infos.size() > 0) {
						if (MessageDialog.openConfirm(
								null,
								Messages.getString("confirmed"),
								Messages.getString("message.monitor.snmptrap.confirm.to.delete.selected.items"))) {

							define.getTableItemInfoManager().delete(infos);
							removeItem(infos.toArray(new TrapValueInfo[0]));
							updateMibList();
							update();
						}
					}
					else{
						MessageDialog.openWarning(
								null,
								Messages.getString("warning"),
								Messages.getString("message.monitor.30"));
					}
				}
			});
		}

		// コピーボタン
		if((this.define.getButtonOptions() & ITableItemCompositeDefine.COPY) != 0){
			this.btnCopy = this.createButton(composite, Messages.getString("copy"));
			WidgetTestUtil.setTestId(this, "copy", btnCopy);
			this.btnCopy.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					TrapValueInfo item = getSelectedItem();
					if (item != null) {

						// シェルを取得
						Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

						CommonDialog dialog = define.createDialog(shell, item);
						if (dialog.open() == IDialogConstants.OK_ID) {
							Table table = infoList.getTableViewer().getTable();
							WidgetTestUtil.setTestId(this, "modify", table);
							int selectIndex = table.getSelectionIndex();
							define.getTableItemInfoManager().add(define.getCurrentCreatedItem());
							table.setSelection(selectIndex);
							addItem(define.getCurrentCreatedItem());
							updateMibList();
							update();
						}
					}
					else{
						MessageDialog.openWarning(
								null,
								Messages.getString("warning"),
								Messages.getString("message.monitor.30"));
					}
				}
			});
		}

		this.update();
	}

	private void updateRecordNumber(int displayNumber, int totalNumber) {
		labelRecordNumber.setText(Messages.getString("records.total", new Object[] {displayNumber, totalNumber}));
	}

	/**
	 * コンポジットを有効/無効化します。
	 *
	 */
	@Override
	public void setEnabled(boolean enabled){
		super.setEnabled(enabled);
		cmbMib.setEnabled(enabled);
		txtFilter.setEnabled(enabled);
		btnClearFilter.setEnabled(enabled);
		btnShowOidTable.setEnabled(enabled);
		infoList.setEnabled(enabled);
		btnAdd.setEnabled(enabled);
		btnDelete.setEnabled(enabled);
		btnModify.setEnabled(enabled);
		btnCopy.setEnabled(enabled);

		checkButtonEnabled();
	}

	/**
	 * ボタンを返します。
	 *
	 * @param parent 親のコンポジット
	 * @param label ボタンに表示するテキスト
	 * @return ボタン
	 */
	protected Button createButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, button);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		button.setLayoutData(gridData);

		button.setText(label);

		return button;
	}

	/**
	 * 引数で指定された判定情報の行を選択状態にします。
	 *
	 * @param identifier 識別キー
	 */
	protected void selectItem(TrapValueInfo item) {
		Table table = this.infoList.getTableViewer().getTable();
		WidgetTestUtil.setTestId(this, null, table);
		TableItem[] items = table.getItems();

		if (items == null || item == null) {
			return;
		}

		for (int i = 0; i < items.length; i++) {

			TrapValueInfo tmpItem = (TrapValueInfo) items[i].getData();
			WidgetTestUtil.setTestId(this, "items" + i, items[i]);
			if (item.equals(tmpItem)) {
				table.select(i);
				return;
			}
		}
	}

	/**
	 * 選択されている判定情報の識別キーを返します。
	 *
	 * @return 識別キー。選択されていない場合は、<code>null</code>。
	 */
	protected TrapValueInfo getSelectedItem() {
		List<TrapValueInfo> infos = getSelectedItems();
		return infos.size() > 0? infos.get(0): null;
	}

	protected List<TrapValueInfo> getSelectedItems() {
		StructuredSelection selection = (StructuredSelection) this.infoList.getTableViewer().getSelection();

		List<TrapValueInfo> tableItemInfos = new ArrayList<TrapValueInfo>();
		for(Object o: selection.toList()){
			if(o instanceof TrapValueInfo){
				tableItemInfos.add((TrapValueInfo)o);
			}
		}

		return tableItemInfos;
	}

	/**
	 * テーブルに登録されているアイテムを返します。
	 *
	 * @return List<T> テーブルアイテム。
	 */
	public List<TrapValueInfo> getItems(){
		return this.infoList.getTableItemData();
	}

	/**
	 * コンポジットを更新します。
	 * <p>
	 *
	 */
	@Override
	public void update() {
		infoList.getTableViewer().setInput(getFilteredList());

		checkButtonEnabled();
	}

	protected void checkButtonEnabled(){
		boolean enabled = this.infoList.getTableViewer().getTable().getEnabled() && this.infoList.getTableViewer().getTable().getSelectionCount() <= 1;
		btnModify.setEnabled(enabled);
		btnCopy.setEnabled(enabled);
	}


	private void updateMibList(){
		String tmp = cmbMib.getText();
		cmbMib.removeAll();
		cmbMib.add("");

		if(!getItems().isEmpty()){
			Set<String> set = new TreeSet<String>(mibMap.values());
			for(String mib: set){
				cmbMib.add(mib);
			}
		}

		if(cmbMib.indexOf(tmp) != -1){
			cmbMib.select(cmbMib.indexOf(tmp));
		}
	}

	private void addItem(TrapValueInfo... infos){
		if(infos != null){
			for(TrapValueInfo info: infos){
				mibMap.put(info, info.getMib());
				if(trapMap.get(info.getMib()) == null){
					trapMap.put(info.getMib(), new ArrayList<TrapValueInfo>());
				}
				trapMap.get(info.getMib()).add(info);
			}
		}
	}

	private void removeItem(TrapValueInfo... infos){
		if(infos != null){
			for(TrapValueInfo info: infos){
				mibMap.remove(info);
				trapMap.get(info.getMib()).remove(info);
				if(trapMap.get(info.getMib()).isEmpty()){
					trapMap.remove(info.getMib());
				}
			}
		}
	}

	private List<TrapValueInfo> getFilteredList(){
		List<TrapValueInfo> tmp1 = new ArrayList<>();
		List<TrapValueInfo> tmp2 = new ArrayList<>();

		if(!"".equals(cmbMib.getText().trim()) && trapMap.containsKey(cmbMib.getText().trim())){
			tmp1.addAll(trapMap.get(cmbMib.getText().trim()));
		} else {
			tmp1.addAll(getItems());
		}

		if(!"".equals(txtFilter.getText().trim())){
			for(TrapValueInfo info: tmp1){
				if(Pattern.matches(".*" + txtFilter.getText().trim() + ".*", info.getUei())){
					tmp2.add(info);
				}
			}
		} else {
			tmp2 = tmp1;
		}
		return limitDisplayTrapValueInfoList(tmp2);
	}
	
	private List<TrapValueInfo> limitDisplayTrapValueInfoList(List<TrapValueInfo> infoList) {
		int maxTrapOids = ClusterControlPlugin.getDefault().getPreferenceStore().getInt(MonitorPreferencePage.P_MAX_TRAP_OID);
		
		int totalNumber = infoList.size();
		int displayNumber = Math.min(totalNumber, maxTrapOids);
		updateRecordNumber(displayNumber, totalNumber);
		return infoList.subList(0, displayNumber);
	}
}
