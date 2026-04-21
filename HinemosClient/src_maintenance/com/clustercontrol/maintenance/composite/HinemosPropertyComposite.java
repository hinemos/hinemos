/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.composite;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.HinemosPropertyResponse;
import org.openapitools.client.model.HinemosPropertyResponse.TypeEnum;

import com.clustercontrol.common.util.CommonRestClientWrapper;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.maintenance.HinemosPropertyTypeMessage;
import com.clustercontrol.maintenance.action.GetHinemosPropertyTableDefine;
import com.clustercontrol.maintenance.composite.action.HinemosPropertyDoubleClickListener;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * 共通設定コンポジットクラスです。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class HinemosPropertyComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog(HinemosPropertyComposite.class);

	// ----- instance フィールド ----- //

	/** プロパティID検索 */
	private Text propertyKeyFilterText = null;

	/** テーブルビューア */
	private CommonTableViewer tableViewer = null;

	// ----- コンストラクタ ----- //

	/**
	 * インスタンスを返します。
	 *
	 * @param parent
	 *            親のコンポジット
	 * @param style
	 *            スタイル
	 */
	public HinemosPropertyComposite(Composite parent, int style) {
		super(parent, style);

		this.initialize();
	}

	// ----- instance メソッド ----- //

	/**
	 * コンポジットを生成・構築します。
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(3, false);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		
		Label propertyKey = new Label(this, SWT.RIGHT);
		propertyKey.setText(Messages.getString("キー", Locale.getDefault()) + " : ");
		GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		propertyKey.setLayoutData(gridData);
		
		// Hinemosプロパティのキー検索窓
		propertyKeyFilterText = new Text(this, SWT.BORDER);
		gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gridData.widthHint = 300;
		propertyKeyFilterText.setLayoutData(gridData);
		
		propertyKeyFilterText.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.keyCode == SWT.CR) {
						HinemosPropertyComposite.this.update();
						tableViewer.refresh();
					}
				}
			});

		Table table = new Table(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		WidgetTestUtil.setTestId(this, null, table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		gridData = new GridData(GridData.FILL, GridData.FILL, true, true, 3, 1);
		table.setLayoutData(gridData);

		// テーブルビューアの作成
		this.tableViewer = new CommonTableViewer(table);
		this.tableViewer.createTableColumn(GetHinemosPropertyTableDefine.get(),
				GetHinemosPropertyTableDefine.SORT_COLUMN_INDEX1, GetHinemosPropertyTableDefine.SORT_COLUMN_INDEX2,
				GetHinemosPropertyTableDefine.SORT_ORDER);

		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumn(i).setMoveable(true);
		}

		// ダブルクリックリスナの追加
		this.tableViewer.addDoubleClickListener(new HinemosPropertyDoubleClickListener(this));
	}

	/**
	 * tableViewerを返します。
	 *
	 * @return tableViewer
	 */
	public CommonTableViewer getTableViewer() {
		return this.tableViewer;
	}

	/**
	 * このコンポジットが利用するテーブルを返します。
	 *
	 * @return テーブル
	 */
	public Table getTable() {
		return this.tableViewer.getTable();
	}

	/**
	 * コンポジットを更新します。
	 * <p>
	 *
	 */
	@Override
	public void update() {
		/*
		 * 共通設定情報とスケジュール情報が別テーブルにあるため、それぞれ取得する。
		 */
		// 共通設定情報取得
		List<HinemosPropertyResponse> HinemosPropertyList = null;
		Map<String, List<HinemosPropertyResponse>> dispDataMap = new ConcurrentHashMap<String, List<HinemosPropertyResponse>>();
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();

		for (String managerName : RestConnectManager.getActiveManagerSet()) {
			CommonRestClientWrapper wrapper = CommonRestClientWrapper.getWrapper(managerName);
			try {
				HinemosPropertyList = wrapper.getHinemosPropertyList();
			} catch (InvalidRole e) {
				errorMsgs.put(managerName, Messages.getString("message.accesscontrol.16"));
			} catch (Exception e) {
				m_log.warn("update() getHinemosPropertyList, " + e.getMessage(), e);
				errorMsgs.put(managerName, Messages.getString("message.hinemos.failure.unexpected") + ", "
						+ HinemosMessage.replace(e.getMessage()));
			}

			if (HinemosPropertyList == null) {
				HinemosPropertyList = new ArrayList<HinemosPropertyResponse>();
			}

			dispDataMap.put(managerName, HinemosPropertyList);
		}

		// メッセージ表示
		if (0 < errorMsgs.size()) {
			UIManager.showMessageBox(errorMsgs, true);
		}

		// tableViewer にセットするための詰め替え
		List<List<Object>> listInput = new ArrayList<>();
		Calendar createCal = Calendar.getInstance(TimezoneUtil.getTimeZone());
		Calendar modifyCal = Calendar.getInstance(TimezoneUtil.getTimeZone());

		for (Map.Entry<String, List<HinemosPropertyResponse>> map : dispDataMap.entrySet()) {
			for (HinemosPropertyResponse info : map.getValue()) {
				List<Object> list = new ArrayList<>();

				list.add(map.getKey());
				list.add(info.getKey());
				list.add(info.getValue());
				if (TypeEnum.STRING.equals(info.getType())) {
					list.add(HinemosPropertyTypeMessage.STRING_STRING);
				} else if (TypeEnum.NUMERIC.equals(info.getType())) {
					list.add(HinemosPropertyTypeMessage.STRING_NUMERIC);
				} else {
					list.add(HinemosPropertyTypeMessage.STRING_TRUTH);
				}

				list.add(info.getDescription());
				list.add(info.getOwnerRoleId());
				list.add(info.getCreateUserId());
				if (info.getCreateDatetime() != null) {
					try {
						createCal.setTime(TimezoneUtil.getSimpleDateFormat().parse(info.getCreateDatetime()));
					} catch (ParseException e) {
						// ここには入らない想定
						m_log.warn("invalid ModifyDatetime.", e);
					}
					list.add(createCal.getTime());
				} else {
					list.add(null);
				}
				list.add(info.getModifyUserId());
				if (info.getModifyDatetime() != null) {
					try {
						modifyCal.setTime(TimezoneUtil.getSimpleDateFormat().parse(info.getModifyDatetime()));
					} catch (ParseException e) {
						// ここには入らない想定
						m_log.warn("invalid ModifyDatetime.", e);
					}
					list.add(modifyCal.getTime());
				} else {
					list.add(null);
				}
				list.add(null);

				listInput.add(list);
			}
		}
		
		// 指定された部分文字列でフィルター
		String filterText = Optional.ofNullable(propertyKeyFilterText.getText()).orElse("");
		if (!filterText.isEmpty()) {
			listInput = listInput.stream().filter(l->l.get(1).toString().contains(filterText)).collect(Collectors.toList());
		}
		
		// テーブル更新
		this.tableViewer.setInput(listInput);
	}
}
