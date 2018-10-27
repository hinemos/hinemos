/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.monitor.bean.MonitorTypeMstConstant;
import com.clustercontrol.monitor.composite.MonitorTypeListComposite;
import com.clustercontrol.monitor.plugin.IMonitorPlugin;
import com.clustercontrol.monitor.plugin.LoadMonitorPlugin;
import com.clustercontrol.monitor.run.bean.MonitorTypeMessage;
import com.clustercontrol.monitor.view.MonitorListView;
import com.clustercontrol.monitor.view.action.MonitorModifyAction;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
/**
 * 監視種別一覧を表示するダイアログクラス<BR>
 *
 * @version 6.1.0
 * @since 4.0.0
 */
public class MonitorTypeDialog extends CommonDialog {

	// ----- instance フィールド ----- //
	// 後でpackするためsizeXはダミーの値。
	private static final int sizeX = 300;
	private static final int sizeY = 300;

	// 監視種別一覧用コンポジット
	private MonitorTypeListComposite listComposite = null;

	// 監視種別リスト用ビュー(listComposite内のオブジェクト)
	private ListViewer monitorTypeList = null;

	// 呼び出し元ビュー
	private MonitorListView view = null;

	// 監視種別マップ（値、表示文字列）
	private Map<ArrayList<Object>, String> monitorTypeMstMap = null;

	// ----- コンストラクタ ----- //

	/**
	 * ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親とするシェル
	 */
	public MonitorTypeDialog(Shell parent, MonitorListView view) {
		super(parent);
		this.view = view;
	}

	// ----- instance メソッド ----- //

	@Override
	protected Point getInitialSize() {
		return new Point(sizeX, sizeY);
	}

	/**
	 * ダイアログ作成のメイン処理
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		parent.getShell().setText(Messages.getString("monitor.type"));

		GridLayout layout = new GridLayout(5, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		listComposite = new MonitorTypeListComposite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, listComposite);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 5;
		listComposite.setLayoutData(gridData);

		monitorTypeList = listComposite.getMonitorTypeList();

		monitorTypeList.setLabelProvider(new LabelProvider(){
			@Override
			public String getText(Object element) {
				// ラベルを取得する
				return monitorTypeMstMap.get(element);
			}
		});

		// 監視設定項目を作成する。
		monitorTypeMstMap = getMonitorTypeMstMap(MonitorTypeMstConstant.getListAll());
		List<Map.Entry<ArrayList<Object>, String>> monitorTypeMstList
			= new ArrayList<>(monitorTypeMstMap.entrySet());
		Collections.sort(monitorTypeMstList, new Comparator<Map.Entry<ArrayList<Object>, String>>(){
			@Override
			public int compare(Map.Entry<ArrayList<Object>, String> o1, Map.Entry<ArrayList<Object>, String> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});
		for (Map.Entry<ArrayList<Object>, String> entry : monitorTypeMstList) {
			monitorTypeList.add(entry.getKey());
		}

		// アイテムをダブルクリックした場合、それを選択したこととする。
		monitorTypeList.addDoubleClickListener(
				new IDoubleClickListener() {
					@Override
					public void doubleClick(DoubleClickEvent event) {
						okPressed();
					}
				});

		//ダイアログのサイズ調整（pack:resize to be its preferred size）
		shell.pack();
		shell.setSize(new Point(shell.getSize().x, shell.getSize().y));
	}

	/**
	 * 選択されたアイテム(監視種別マスタの定義)の取得
	 * @return
	 */
	public ArrayList<?> getSelectItem() {
		return this.listComposite.getSelectItem();
	}

	/**
	 * OK ボタンの表示テキスト設定
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("next");
	}

	/**
	 * OK ボタン押下<BR>
	 * 本処理の中で、指定された監視機能の作成ダイアログを表示させる。
	 */
	@Override
	protected void okPressed() {
		// 選択項目のnullチェック
		if(this.getSelectItem() == null){
			ValidateResult result = null;
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.monitor.57"));
			displayError(result);
			return;
		}

		// DBに登録されているダイアログのクラス名を取得する
		String pluginId = (String)((this.getSelectItem())).get(0);
		// ダイアログの生成
		MonitorModifyAction action = new MonitorModifyAction();
		action.dialogOpen(getParentShell(), null, pluginId, null);

		// 監視設定後に、監視種別一覧は閉じない。
		// super.okPressed();

		// 監視設定後に監視設定ビューを更新
		this.view.update();
	}

	@Override
	protected void cancelPressed() {
		super.cancelPressed();
	}

	/**
	 * キャンセルボタンの表示テキスト設定
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}

	private Map<ArrayList<Object>, String> getMonitorTypeMstMap(ArrayList<ArrayList<Object>> monitorTypeMstList) {
		Map<ArrayList<Object>, String> monitorTypeMstMap = new HashMap<>();
		if (monitorTypeMstList != null) {

			Set<String> activeOptions = EndpointManager.getAllOptions();

			for (ArrayList<Object> monitorTypeMst : monitorTypeMstList) {
				String label = "";
				String pluginName = null;
				String pluginId = (String)monitorTypeMst.get(0);

				if (pluginId.equals(HinemosModuleConstant.MONITOR_AGENT)) {
					pluginName = Messages.getString("agent.monitor");
				} else if (pluginId.equals(HinemosModuleConstant.MONITOR_HTTP_N)
						|| pluginId.equals(HinemosModuleConstant.MONITOR_HTTP_S)
						|| pluginId.equals(HinemosModuleConstant.MONITOR_HTTP_SCENARIO)) {
					pluginName = Messages.getString("http.monitor");
				} else if (pluginId.equals(HinemosModuleConstant.MONITOR_PERFORMANCE)) {
					pluginName = Messages.getString("performance.monitor");
				} else if (pluginId.equals(HinemosModuleConstant.MONITOR_PING)) {
					pluginName = Messages.getString("ping.monitor");
				} else if (pluginId.equals(HinemosModuleConstant.MONITOR_PORT)) {
					pluginName = Messages.getString("port.monitor");
				} else if (pluginId.equals(HinemosModuleConstant.MONITOR_PROCESS)) {
					pluginName = Messages.getString("process.monitor");
				} else if (pluginId.equals(HinemosModuleConstant.MONITOR_SNMP_N)
						|| pluginId.equals(HinemosModuleConstant.MONITOR_SNMP_S)) {
					pluginName = Messages.getString("snmp.monitor");
				} else if (pluginId.equals(HinemosModuleConstant.MONITOR_SQL_N)
						|| pluginId.equals(HinemosModuleConstant.MONITOR_SQL_S)) {
					pluginName = Messages.getString("sql.monitor");
				} else if (pluginId.equals(HinemosModuleConstant.MONITOR_SYSTEMLOG)) {
					pluginName = Messages.getString("systemlog.monitor");
				} else if (pluginId.equals(HinemosModuleConstant.MONITOR_LOGFILE)) {
					pluginName = Messages.getString("logfile.monitor");
				} else if (pluginId.equals(HinemosModuleConstant.MONITOR_BINARYFILE_BIN)) {
					pluginName = Messages.getString("binary.file.monitor");
				} else if (pluginId.equals(HinemosModuleConstant.MONITOR_PCAP_BIN)) {
					pluginName = Messages.getString("packet.capture.monitor");
				} else if (pluginId.equals(HinemosModuleConstant.MONITOR_CUSTOM_N)
						|| pluginId.equals(HinemosModuleConstant.MONITOR_CUSTOM_S)) {
					pluginName = Messages.getString("custom.monitor");
				} else if (pluginId.equals(HinemosModuleConstant.MONITOR_SNMPTRAP)) {
					pluginName = Messages.getString("snmptrap.monitor");
				} else if (pluginId.equals(HinemosModuleConstant.MONITOR_WINSERVICE)) {
					pluginName = Messages.getString("winservice.monitor");
				} else if (pluginId.equals(HinemosModuleConstant.MONITOR_WINEVENT)) {
					pluginName = Messages.getString("winevent.monitor");
				} else if (pluginId.equals(HinemosModuleConstant.MONITOR_JMX)) {
					pluginName = Messages.getString("jmx.monitor");
				} else if (pluginId.equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N)
						|| pluginId.equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S)) {
					pluginName = Messages.getString("customtrap.monitor");
				} else if (pluginId.equals(HinemosModuleConstant.MONITOR_LOGCOUNT)) {
					pluginName = Messages.getString("logcount.monitor");
				} else if (pluginId.equals(HinemosModuleConstant.MONITOR_CORRELATION)) {
					pluginName = Messages.getString("correlation.monitor");
				} else if (pluginId.equals(HinemosModuleConstant.MONITOR_INTEGRATION)) {
					pluginName = Messages.getString("integration.monitor");
				} else {
					// ExtensionMonitorはオプションによって追加される
					String option = null;
					for(IMonitorPlugin extensionMonitor: LoadMonitorPlugin.getExtensionMonitorList()){
						if(pluginId.equals(extensionMonitor.getMonitorPluginId())){
							pluginName = extensionMonitor.getMonitorName();
							option = extensionMonitor.getOption();
							break;
						}
					}
					// TODO extension point(monitorPlugin)に直接activitiesを適用できるようにしたい
					// 当該オプションがない場合、スキップ
					if(!activeOptions.contains(option)){
						continue;
					}

					if(pluginName == null){
						pluginName = pluginId;
					}
				}
				label = pluginName + " (" +
						MonitorTypeMessage.typeToString((Integer)monitorTypeMst.get(1)) + ")";
				monitorTypeMstMap.put(monitorTypeMst, label);
			}
		}
		return monitorTypeMstMap;
	}
}
