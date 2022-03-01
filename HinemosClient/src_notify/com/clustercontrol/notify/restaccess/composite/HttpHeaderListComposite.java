/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.restaccess.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.notify.restaccess.dialog.HttpHeaderCreateDialog;
import com.clustercontrol.notify.restaccess.viewer.HttpHeaderTableRecord;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * 
 * RESTアクセス情報 httpヘッダー一覧部分
 *
 */
public class HttpHeaderListComposite extends Composite {
	
	/**　順番号　*/
	public static final int ORDER_NO = 0;
	
	/** key */
	public static final int KEY = 1;
	
	/** value */
	public static final int VALUE = 2;

	/** ダミー**/
	public static final int DUMMY = 3;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = ORDER_NO;

	/** 初期表示時ソートオーダー */
	public static final int SORT_ORDER = 1;
	
	/** valueでのtooltip表示要否 */
	private boolean m_isTooltipDispray = false;

	/**
	 * テーブル定義を取得します。<BR>
	 *
	 * @return
	 */
	public static ArrayList<TableColumnInfo> getTableColumnInfoList() {
		//テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();
		tableDefine.add(ORDER_NO,
				new TableColumnInfo(Messages.getString("no", locale), TableColumnInfo.NONE, 40, SWT.LEFT));
		tableDefine.add(KEY,
				new TableColumnInfo(Messages.getString("key", locale), TableColumnInfo.NONE, 120, SWT.LEFT));
		tableDefine.add(VALUE,
				new TableColumnInfo(Messages.getString("value", locale), TableColumnInfo.NONE, 120, SWT.LEFT));

		return tableDefine;
	}
	
	
	/** テーブルビューアー。 */
	private CommonTableViewer m_tableViewer = null;

	/** 情報一覧 */
	private List<HttpHeaderTableRecord> m_headerList = null;

	/**
	 *
	 * @return
	 */
	public List<HttpHeaderTableRecord> getHeaderList(){
		return this.m_headerList;
	}
	/**
	 * 引数で情報をコンポジット内リストに反映させる
	 * @param keyList
	 */
	public void setHeaderList(List<HttpHeaderTableRecord> list){
		if (list != null) {
			this.m_headerList = list;
			this.update();
		}
	}
	public void addHeaderList(HttpHeaderTableRecord rec){
		this.m_headerList.add(rec);
	}

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param isTooltipDispray httpヘッダダイアログでのツールチップ表示
	 * @param managerName マネージャ名
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public HttpHeaderListComposite(Composite parent, int style ,boolean isTooltipDispray) {
		super(parent, style);
		this.m_headerList = new ArrayList<HttpHeaderTableRecord>();
		this.initialize();
		m_isTooltipDispray = isTooltipDispray;
	}
	
	/**
	 * 引数で指定された判定情報の行を選択状態にします。
	 *
	 * @param identifier 識別キー
	 */
	@SuppressWarnings("unused")
	private void selectItem(Integer order) {
		Table tblLogFormatKeyList = m_tableViewer.getTable();
		TableItem[] items = tblLogFormatKeyList.getItems();

		if (items == null || order == null) {
			return;
		}
		tblLogFormatKeyList.select(order);
		return;
	}
	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {
	
		/*
		 * 情報初期化
		 */
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		Table tblLogFormatKeyList = new Table(this, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		tblLogFormatKeyList.setHeaderVisible(true);
		tblLogFormatKeyList.setLinesVisible(true);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		tblLogFormatKeyList.setLayoutData(gridData);

		// テーブルビューアの作成
		m_tableViewer = new CommonTableViewer(tblLogFormatKeyList);
		m_tableViewer.createTableColumn(getTableColumnInfoList(),
				SORT_COLUMN_INDEX,
				SORT_ORDER);
		m_tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				// シェルを取得
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				//キーをもとに、変更対象の情報を引き渡し、必要なら変更結果を反映
				int tagIndex = getHeaderListIndexAsSelection();
				HttpHeaderTableRecord tagRec = getHeaderList().get(tagIndex);
				HttpHeaderCreateDialog dialog = new HttpHeaderCreateDialog(shell, tagRec,m_isTooltipDispray);
				if (dialog.open() == IDialogConstants.OK_ID) {
					HttpHeaderTableRecord newRec = dialog.getInputData();
					newRec.setHeaderOrderNo(tagRec.getHeaderOrderNo());
					getHeaderList().set(tagIndex, newRec);
					update();
				}
			}
		});
	}
	
	/**
	 * コンポジットを更新します。<BR>
	 * 情報一覧を取得し、テーブルビューアーにセットします。
	 */
	@Override
	public void update() {
		// テーブル更新
		ArrayList<Object> listAll = new ArrayList<Object>();
		for (HttpHeaderTableRecord key : getHeaderList()) {
			ArrayList<Object> list = new ArrayList<Object>();
			list.add(key.getHeaderOrderNo());
			list.add(key.getKey());
			list.add(key.getValue());
			//ダミー列
			list.add(null);
			listAll.add(list);
		}
		m_tableViewer.setInput(listAll);
	}
	
	/**
	 * 選択したテーブル行番号を返す。
	 *
	 */
	public Integer getSelection() {
		StructuredSelection selection = (StructuredSelection) m_tableViewer.getSelection();
		if (selection.getFirstElement() instanceof List) {
			List<?> list = (List<?>)selection.getFirstElement();
			if (list.get(0) instanceof Integer) {
				return (Integer) list.get(0);
			}
		}
		return null;
	}
	/**
	 * 選択したテーブルのキー（順番号）を返す。
	 *
	 */
	public Long getSelectionOrderNo() {
		Integer index =  getHeaderListIndexAsSelection();
		if(index != null ){
			return m_headerList.get(index).getHeaderOrderNo();
		}
		return null;
	}
	/**
	 * 選択したテーブル行に対応するHeaderListのindexを返す
	 *
	 */
	public Integer getHeaderListIndexAsSelection() {
		StructuredSelection selection = (StructuredSelection) m_tableViewer.getSelection();
		if (selection.getFirstElement() instanceof List) {
			List<?> list = (List<?>)selection.getFirstElement();
			if (list.get(0) instanceof Long) {
				Long findOrderNo = (Long) list.get(0);
				
				for (int counter = 0, maxCount = m_headerList.size(); counter < maxCount; counter++) {
					Long recOrderNo = m_headerList.get(counter).getHeaderOrderNo();
					if( recOrderNo != null && recOrderNo.equals(findOrderNo) ){
						return counter;
					}
				}
			}
		}
		return null;
	}

	/**
	 * 選択したテーブル行に対応するInfoオブジェクトを削除する。
	 *
	 */
	public void deleteSelectionInfo() {
		Integer index =  getHeaderListIndexAsSelection();
		if(index != null ){
			m_headerList.remove(index.intValue());
			//ORDER_NOを振り直し
			for (int counter = 0, maxCount = m_headerList.size(); counter < maxCount; counter++) {
				Long recOrderNo = Long.valueOf(counter + 1);
				m_headerList.get(counter).setHeaderOrderNo(recOrderNo); 
			}
		}
		return ;
	}
	
	/**
	 * 
	 */
	public void setSelection() {
		Table setSelectionTable = m_tableViewer.getTable();
		int selectIndex = setSelectionTable.getSelectionIndex();
		update();
		setSelectionTable.setSelection(selectIndex);
	}


	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.m_tableViewer.getTable().setEnabled(enabled);
	}
	
}
