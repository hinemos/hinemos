/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.collect.composite;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.openapitools.client.model.CollectKeyInfoResponseP1;
import org.openapitools.client.model.FacilityInfoResponse;
import org.openapitools.client.model.MonitorNumericValueInfoResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.collect.bean.SummaryTypeConstant;
import com.clustercontrol.collect.bean.SummaryTypeMessage;
import com.clustercontrol.collect.preference.PerformancePreferencePage;
import com.clustercontrol.collect.util.CollectGraphUtil;
import com.clustercontrol.collect.view.CollectGraphView;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.action.GetEventListTableDefine;
import com.clustercontrol.monitor.dialog.EventInfoDialog;
import com.clustercontrol.monitor.run.bean.MultiManagerEventDisplaySettingInfo;
import com.clustercontrol.monitor.util.EventDisplaySettingGetUtil;
import com.clustercontrol.monitor.view.action.MonitorModifyAction;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.HtmlLoaderUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.RestConnectUnit;
import com.clustercontrol.util.TimezoneUtil;

/**
 * 性能グラフを表示するコンポジットクラス<BR>
 *
 * @version 5.0.0
 * @since 4.0.0
 */
public class CollectGraphComposite extends Composite {
	private static final long serialVersionUID = 8060282120185063132L;

	private static Log m_log = LogFactory.getLog(CollectGraphComposite.class);

	private Label settingLabel = null;
	
	/**
	 * 
	 */
	private Browser m_browserGraph;
	
	/**
	 * 
	 */
	private Browser m_browserSlider;
	
	/**
	 * 
	 */
	private CollectGraphView m_collectGraphView;

	/**
	 * Browserの画面起動完了フラグ(trueなるまでグラフの描画はできない。trueになってから処理を始める)
	 */
	private boolean completed = false;
	private boolean slider_completed = false;
	
	/**
	 * 円グラフ用javascript制御名
	 */
	private static final String CONTROL_CLASS_NAME_PIE = "ControlPiegraph";
	/**
	 * 散布図用javascript制御名
	 */
	private static final String CONTROL_CLASS_NAME_SCATTER = "ControlScattergraph";
	/**
	 * 折れ線・積み上げ面グラフ用javascript制御名
	 */
	private static final String CONTROL_CLASS_NAME_LINESTACK = "ControlGraph";
	/**
	 * 棒グラフ用javascript制御名
	 */
	private static final String CONTROL_CLASS_NAME_BARSTACK = "ControlBarGraph";
	/**
	 * 円グラフ用javascript名
	 */
	private static final String GRAPH_CLASS_NAME_PIE = "Piegraph";
	/**
	 * 散布図用javascript名
	 */
	private static final String GRAPH_CLASS_NAME_SCATTER = "Scattergraph";
	/**
	 * 折れ線・積み上げ面用javascript名
	 */
	private static final String GRAPH_CLASS_NAME_LINESTACK = "NewGraph";
	/**
	 * 棒グラフグラフ用javascript名
	 */
	private static final String GRAPH_CLASS_NAME_BARSTACK = "StackBarGraph";
	
	// javascript -> java呼び出し時の識別子
	private static final String CALL_METHOD_NAME_NOTICEGRAPHZOOM = "noticeGraphZoom";
	private static final String CALL_METHOD_NAME_MOUSEUP = "mouseup";
	private static final String CALL_METHOD_NAME_BRUSHEND = "brushend";
	private static final String CALL_METHOD_NAME_NOTICESLIDERTYPE = "noticeSliderType";
	private static final String CALL_METHOD_NAME_NOTICESLIDER_BRUSHEND = "noticeSlider_brushend";
	private static final String CALL_METHOD_NAME_INPUTDATE = "inputdate";
	private static final String CALL_METHOD_NAME_AUTOZOOMGRAPH = "autoZoomGraph";
	private static final String CALL_METHOD_NAME_AUTODRAW = "autodraw";
	private static final String CALL_METHOD_NAME_CHANGETHRESHOLD = "changeThreshold";
	private static final String CALL_METHOD_NAME_OPEN_EVENT_DETAIL = "open_event_detail";
	private static final String CALL_METHOD_NAME_JAVASCRIPT_ERROR = "javascript_error";
	
	/** プラグインIDの末尾文字（数値） */
	private static final String SUFFIX_PLUGIN_ID_NUM = "_N";

	private String lastScript = "";


	/**
	 * コンストラクタ
	 *
	 * @param settings グラフのヘッダ情報
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 */
	public CollectGraphComposite(Composite parent, int style, CollectGraphView collectGraphView) {
		super(parent, style);
		// 初期化
		initialize();
		m_collectGraphView = collectGraphView;
		
		// settingLabel
		settingLabel = new Label(this, SWT.LEFT | SWT.WRAP);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		settingLabel.setLayoutData(gridData);
		setSettingLabel(-1, new ArrayList<CollectKeyInfoResponseP1>());
		
		try {
			m_browserSlider = new Browser(this, SWT.NONE);
			m_browserGraph = new Browser(this, SWT.NONE);
			
			gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.verticalAlignment = GridData.FILL;
			gridData.minimumHeight = 100;
			gridData.heightHint = 100;
			m_browserSlider.setLayoutData(gridData);
			
			GridData gridData1;
			gridData1 = new GridData();
			gridData1.horizontalAlignment = GridData.FILL;
			gridData1.verticalAlignment = GridData.FILL;
			gridData1.grabExcessHorizontalSpace = true;
			gridData1.grabExcessVerticalSpace = true;
			m_browserGraph.setLayoutData(gridData1);
			
			loadBrowser();
			m_log.info("set URL");

		} catch (Exception e) {
			m_log.warn("CollectGraphComposite : " + e.getMessage(), e);
			throw new RuntimeException(e);
		}
		
		this.addControlListener(new ControlListener() {
			private static final long serialVersionUID = 5161768188146531389L;

			@Override
			public void controlResized(ControlEvent e) {
				// nop
			}
			
			@Override
			public void controlMoved(ControlEvent e) {
				// 画面のサイズ変更されるたびにメンバにサイズを保持
				Point point = CollectGraphComposite.this.getSize();
				m_log.debug("resize x:" + point.x + ", y:" + point.y);
				CollectGraphUtil.setScreenWidth(point.x);
			}
		});
	}
	private void loadBrowser() {
		HtmlLoaderUtil.load(m_browserGraph, "graph.html", "com/clustercontrol/collect/composite/");
		m_browserGraph.addProgressListener(new ProgressListener() {
			private static final long serialVersionUID = 9027758974641362334L;

			public void completed(ProgressEvent event) {
				// ページのロードが完了してからjavascriptを実行する
				completed = true;
				new CustomFunction(m_browserGraph, "theJavaFunction");
				m_log.debug("graph completed!!!!!");
			}
			public void changed(ProgressEvent event) {
			}
		});
		
		HtmlLoaderUtil.load(m_browserSlider, "slider.html", "com/clustercontrol/collect/composite/");
		m_browserSlider.addProgressListener(new ProgressListener() {
			private static final long serialVersionUID = -1482560819326936962L;

			public void completed(ProgressEvent event) {
				// ページのロードが完了してからjavascriptを実行する
				slider_completed = true;
				new CustomFunction(m_browserSlider, "theJavaFunction");
				m_log.debug("slider completed!!!!!");
			}
			public void changed(ProgressEvent event) {
			}
		});
	}

	class CustomFunction extends BrowserFunction {
		
		/**
		 * コンストラクタ
		 * @param browser
		 * @param name
		 */
		public CustomFunction(Browser browser, String name) {
			super(browser, name);
		}
		
		/**
		 * javascriptから呼ばれる関数<br>
		 * <br>
		 * 呼ばれるのは以下の場合：<br>
		 * <br>
		 *   グラフ画面の拡大・縮小ボタンを押下した場合(noticeGraphZoom)<br>
		 *     -> 適用ボタンを押された場合に、ズームレベルを前回と同様で表示するため<br>
		 * <br>
		 *   グラフ操作で表示期間が変更された場合(メモリ操作を含む)(mouseup)<br>
		 *     -> 移動分のグラフ情報をマネージャから取得し、画面に描画するため<br>
		 * <br>
		 *   スライダー操作で表示対象期間が変更された場合(brushend)<br>
		 *     -> 移動分のグラフ情報をマネージャから取得し、画面に描画するため<br>
		 * <br>
		 *   スライダー上のボタン(10years, year, month, week, day)でスライダー全体期間が変更された場合(noticeSliderType)<br>
		 *     -> 適用ボタンを押された場合に、スライダーを前回と同様の全体期間で表示するため<br>
		 * <br>
		 *   スライダー上のボタン(10years, year, month, week, day)でスライダー全体期間が変更され、かつ表示対象期間が変更された場合(noticeSlider_brushend)<br>
		 *     -> 適用ボタンを押された場合に、スライダーを前回と同様の全体期間で表示するため、移動分のグラフ情報をマネージャから取得し画面に描画するため<br>
		 * <br>
		 *   スライダー右の選択範囲テキストボックスで表示範囲が変更された場合(inputdate)<br>
		 *     -> 表示していた期間と指定された期間の差分情報をマネージャから取得し、画面に描画するため<br>
		 * <br>
		 *   画面の「自動調整」が押下された場合(autoZoomGraph)<br>
		 *     -> 画面の自動調整を行うため(javascriptではサイズが正しく取得できないため、Java側で取得する)<br>
		 * <br>
		 *   画面の「自動更新」が押下された場合(autodraw)<br>
		 *     -> グラフの自動更新を行うため<br>
		 * <br>
		 *   閾値の範囲を変更した場合(changeThreshold)<br>
		 *     -> 閾値の設定範囲を変更した場合に、変更内容を登録するため<br>
		 * <br>
		 *   イベントフラグの線が押下された場合(open_event_detail)<br>
		 *     -> 対象のイベント詳細画面を表示するため<br>
		 * <br>
		 *   javascriptでエラーが発生した場合(javascript_error)<br>
		 *     -> エラーログを出力するため<br>
		 */
		@Override
		public Object function (Object[] arguments) {
			Object retObj = null;
			try {
				retObj = executeFromJavascript(arguments);
			} catch (InvalidRole e) {
				m_log.error("function InvalidRole_Exception");
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} catch (InvalidUserPass e) {
				m_log.error("function InvalidUserPass_Exception");
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.45"));
			} catch (HinemosDbTimeout e) {
				String message = HinemosMessage.replace(e.getMessage());
				m_log.error("グラフ描画時にエラーが発生 message=" + message, e);
				MessageDialog.openError(
						null,
						Messages.getString("error"),
						Messages.getString("message.collection.graph.unexpected.error") + " : " + message);
			} catch (IllegalStateException e) {
				// Webクライアントのみ、既に別スクリプトが実行中の場合に発生する。
				// メッセージが分かりにくいため変更する。
				m_log.warn("Another script is already being executed. message=" + HinemosMessage.replace(e.getMessage()));
				MessageDialog.openWarning(null, Messages.getString("word.warn"),
						Messages.getString("message.performance.4"));
			} catch (Exception e) {
				m_log.error("グラフ描画時にエラーが発生 message=" + e.getMessage(), e);
				MessageDialog.openError(
						null,
						Messages.getString("error"),
						Messages.getString("message.collection.graph.unexpected.error") + " : " + e.getMessage());
			}
			return retObj;
		}

		/**
		 * Javascriptからの実行
		 * 
		 * @param arguments
		 * @return
		 * @throws HinemosDbTimeout
		 * @throws InvalidUserPass
		 * @throws HinemosUnknown
		 * @throws InvalidRole
		 * @throws RestConnectFailed
		 * @throws InvalidSetting
		 */
		private Object executeFromJavascript(Object[] arguments) 
				throws HinemosDbTimeout, InvalidUserPass, HinemosUnknown, InvalidRole, RestConnectFailed, InvalidSetting {

			String ret = (String)arguments[0];
			m_log.info("executeFromJavascript() start. arguments=" + ret);

			// jsonの解析、<key, value>にする
			ret = ret.replaceAll("\\{", "").replaceAll("\\}", "").replaceAll("\"", "");
			String rets[] = ret.split(",");
			HashMap<String, String> scriptMap = new HashMap<String, String>();
			for(String ret1: rets) {
				String ret1s[] = ret1.split(":");
				if (ret1s.length == 2 && ret1s[0] != null && !ret1s[0].equals("") && ret1s[1] != null && !ret1s[1].equals("")){
					scriptMap.put(ret1s[0], ret1s[1]);
				}
				else if (ret1s.length > 2){
					// findbugs対応 文字列の連結方式をStringBuilderを利用する方法に変更
					StringBuilder value = new StringBuilder(ret1s[1]);
					for (int i = 2; i < ret1s.length; i++) {
						value.append(":" + ret1s[i]);
					}
					scriptMap.put(ret1s[0], value.toString());
				}
			}
			
			String methodName = scriptMap.get("method_name");

			// javascriptの呼び出し種別がエラーの場合は、エラーログに出して終了
			if (methodName.equals(CALL_METHOD_NAME_JAVASCRIPT_ERROR)) {
				m_log.error("executeFromJavascript() ERROR:" + ret);
				m_log.error("executeFromJavascript() execScript:" + lastScript);
				if (lastScript.length() > 2000) {
					lastScript = lastScript.substring(0, 2000) + "...";
				}
				MessageDialog.openInformation(
						null, 
						Messages.getString("error"),
						Messages.getString("message.collection.graph.unexpected.error") + "\n" + ret + "\n\n" + lastScript);

				return null;
			}

			// javascriptの呼び出し種別により処理を分ける
			if (methodName.equals(CALL_METHOD_NAME_AUTOZOOMGRAPH)) {
				// 「自動調整」ボタンが押下されたときに呼ばれる
				// javascriptでは画面サイズが正しく取得できないことがあるため、Java側で取得して返す
				return CollectGraphUtil.getScreenWidth();

			} else if (methodName.equals(CALL_METHOD_NAME_NOTICESLIDERTYPE)
					|| methodName.equals(CALL_METHOD_NAME_NOTICESLIDER_BRUSHEND)) {
				// スライダーの種別(10year,year,month,week,day)を操作したときに呼ばれる
				// メンバに現在表示中の期間(種別)を保持するのみで終わり
				CollectGraphUtil.setSliderStart(parseString2Long(scriptMap.get("start_date")));
				CollectGraphUtil.setSliderEnd(parseString2Long(scriptMap.get("end_date")));
				m_log.debug("executeFromJavascript() noticeSliderType call term:" + (CollectGraphUtil.getSliderEnd() - CollectGraphUtil.getSliderStart()));
				if (methodName.equals(CALL_METHOD_NAME_NOTICESLIDERTYPE)) {
					return null;
				}
				// CALL_METHOD_NAME_NOTICESLIDER_BRUSHEND は処理続行

			} else if (methodName.equals(CALL_METHOD_NAME_NOTICEGRAPHZOOM)) {
				// グラフのズームボタンを操作したときに呼ばれる
				// グラフのズームレベルをメンバに保持して終わり
				CollectGraphUtil.setGraphZoomSize(scriptMap.get("graphZoomSize"));
				m_log.debug("executeFromJavascript() noticeGraphZoom call zoomSize:" + CollectGraphUtil.getGraphZoomSize());
				return null;

			} else if (methodName.equals(CALL_METHOD_NAME_CHANGETHRESHOLD)) {
				// 閾値の範囲を変更した場合に呼ばれる
				// 閾値範囲の変更
				// EventModifyMonitorSettingAction参照
				String infoMaxValue = scriptMap.get("info_max");
				String infoMinValue = scriptMap.get("info_min");
				String warnMaxValue = scriptMap.get("warn_max");
				String warnMinValue = scriptMap.get("warn_min");
				String itemName = scriptMap.get("itemname");
				String monitorId = scriptMap.get("monitorid");
				String managerName = scriptMap.get("managername");
				String pluginId = scriptMap.get("pluginid");

				// 閾値情報クラスのリストを作成する
				List<MonitorNumericValueInfoResponse> numericValueInfo = CollectGraphUtil.createMonitorNumericValueInfoList(
						infoMinValue, infoMaxValue, warnMinValue, warnMaxValue);
				MonitorModifyAction mmAction = new MonitorModifyAction();
				if (pluginId.endsWith(SUFFIX_PLUGIN_ID_NUM)) {
					// 数値の場合のみ、閾値情報を設定する
					mmAction.setGraphMonitorNumericValueInfo(numericValueInfo);
				}
				// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
				if (mmAction.dialogOpen(m_collectGraphView.getSite().getShell(), managerName, pluginId, monitorId) == IDialogConstants.OK_ID) {
					// 閾値の変更をした場合は、変更した範囲で全グラフの閾値表示を更新する
					String threshold = CollectGraphUtil.getThresholdData(managerName, monitorId);
					if (threshold != null) {
						infoMinValue = threshold.split(",")[0];
						infoMaxValue = threshold.split(",")[1];
						warnMinValue = threshold.split(",")[2];
						warnMaxValue = threshold.split(",")[3];
					}

					String param =
							"ThresholdGraph.prototype.setThresholdParamMonitorId({"
							+ "\'itemname\':\"" + CollectGraphUtil.escapeParam(itemName) + "\", "
							+ "\'monitorid\':\"" + CollectGraphUtil.escapeParam(monitorId) + "\", "
							+ "\'info_max\':" + CollectGraphUtil.escapeParam(infoMaxValue) + ", "
							+ "\'info_min\':" + CollectGraphUtil.escapeParam(infoMinValue) + ", "
							+ "\'warn_max\':" + CollectGraphUtil.escapeParam(warnMaxValue) + ", " 
							+ "\'warn_min\':" + CollectGraphUtil.escapeParam(warnMinValue) 
							+ "});";
					executeScript(param, m_browserGraph);
				}
				return true;

			} else if (methodName.equals(CALL_METHOD_NAME_OPEN_EVENT_DETAIL)) {
				// イベントフラグ線を押下したときに呼ばれる
				// イベント詳細画面を表示する
				// EventDetailAction参照
				String managerName = scriptMap.get("managername");
				String facilityId = scriptMap.get("facilityid");
				Long date = Long.valueOf(scriptMap.get("datelong"));
				String pluginId = scriptMap.get("pluginid");
				String monitorDetailId = scriptMap.get("monitordetailid");
				if (monitorDetailId == null) {
					// monitordetailid = displaynameのこと、入っていない場合もある
					monitorDetailId = "";
				}
				String monitorId = scriptMap.get("monitorid");
				m_log.debug("executeFromJavascript() open_event_detail managerName:" + managerName + ", facilityid:" + facilityId + ", date:" + date 
						+ ", pluginid:" + pluginId + ", monitordetailid:" + monitorDetailId + ", monitorid:" + monitorId);
				List<Object> list = new ArrayList<>();
				list.add(GetEventListTableDefine.MANAGER_NAME, managerName);
				list.add(1, "");
				list.add(GetEventListTableDefine.RECEIVE_TIME, new Date(date));
				list.add(3, "");
				list.add(GetEventListTableDefine.PLUGIN_ID, pluginId);
				list.add(GetEventListTableDefine.MONITOR_ID, monitorId);
				list.add(GetEventListTableDefine.MONITOR_DETAIL_ID, monitorDetailId);
				list.add(GetEventListTableDefine.FACILITY_ID, facilityId);
				MultiManagerEventDisplaySettingInfo eventDspSetting = new EventDisplaySettingGetUtil().getEventDisplaySettingInfo(RestConnectManager.getActiveManagerNameList());
				EventInfoDialog dialog = new EventInfoDialog(m_collectGraphView.getSite().getShell(), list, eventDspSetting);
				if (dialog.open() == IDialogConstants.OK_ID){
					dialog.okButtonPress(managerName);
				}
				return null;
			}
			// autoZoomGraph noticeSliderType noticeGraphZoom changeThreshold openEventDetailはこの時点でreturn済

			// グラフ表示の更新処理
			return updateGraphs(scriptMap);
		}

		/**
		 * グラフ表示を更新する
		 * JSメソッド brushend、mouseup、autodraw、inputdate について処理を行う。
		 * 
		 * @param scriptMap
		 * @return
		 * @throws HinemosDbTimeout
		 * @throws InvalidUserPass
		 * @throws HinemosUnknown
		 * @throws InvalidRole
		 * @throws RestConnectFailed
		 * @throws InvalidSetting
		 */
		private Object updateGraphs(HashMap<String, String> scriptMap)
						throws HinemosDbTimeout, InvalidUserPass, HinemosUnknown, InvalidRole, RestConnectFailed, InvalidSetting {
			m_log.debug("updateGraphs() start.");

			boolean isAutoGragh = false;

			// 現在時刻は、マネージャのHinemosTimeから取得する。
			// これは、マネージャとクライアントに時間差（offsetも含む）がある場合にグラフ右端が表示されない場合があるため。
			long nowDate = CollectGraphUtil.getManagerTime();
			m_log.debug("updateGraphs() nowDate=" + CollectGraphUtil.toDatetimeString(nowDate));

			if (!scriptMap.containsKey("xaxis_min") || !scriptMap.containsKey("xaxis_max")) {
				m_log.info("updateGraphs() xaxis_min or xaxis_max is not define, so finished.");
				return null;
			}
			Long xaxis_min = parseString2Long(scriptMap.get("xaxis_min"));
			Long xaxis_max = parseString2Long(scriptMap.get("xaxis_max"));
			m_log.debug("updateGraphs() xaxis_min=" + CollectGraphUtil.toDatetimeString(xaxis_min) + ", xaxis_max=" + CollectGraphUtil.toDatetimeString(xaxis_max));
			m_log.debug("updateGraphs() TargetConditionStartDate=" + CollectGraphUtil.toDatetimeString(CollectGraphUtil.getTargetConditionStartDate()) 
					+ ", TargetConditionEndDate=" + CollectGraphUtil.toDatetimeString(CollectGraphUtil.getTargetConditionEndDate()));

			// X軸のスケール変更
			// 描画範囲が1日より長い場合にrawが指定されていた場合、ave_hourに変える
			long date_diff = xaxis_max - xaxis_min;
			if (date_diff > CollectGraphUtil.MILLISECOND_DAY && CollectGraphUtil.getSummaryType() == SummaryTypeConstant.TYPE_RAW) {
				m_log.info("updateGraphs() summary type is raw and x axis range is greater than 24 hours, so change scale.");
				CollectGraphUtil.setSummaryType(SummaryTypeConstant.TYPE_AVG_HOUR);
				// ComboBoxを変更する
				m_collectGraphView.getCollectSettingComposite().setSummaryTypeComboBox(SummaryTypeConstant.TYPE_AVG_HOUR);
				// labelの更新
				setSettingLabel(SummaryTypeConstant.TYPE_AVG_HOUR, CollectGraphUtil.getCollectKeyInfoList());

				// 表示中のRAWとこれから表示するavg_hourを混合させないためにすべてのplotを消す
				deletePlots(CALL_METHOD_NAME_BRUSHEND, xaxis_min, xaxis_max, true);
				CollectGraphUtil.setTargetConditionStartDate(Long.MAX_VALUE);
				CollectGraphUtil.setTargetConditionEndDate(0L);
			}

			// メソッド別の処理
			String methodName = scriptMap.get("method_name");
			if (methodName.equals(CALL_METHOD_NAME_MOUSEUP)) {
				m_log.debug("updateGraphs() CALL_METHOD_NAME_MOUSEUP");

				// スライダーの選択範囲を変更する
				// inputdateの場合はjavascript内で実施しているのでここではしない
				String script = "BrushLine.prototype.moveTarget(" + xaxis_min + ", " + xaxis_max + ");";
				executeScript(script, m_browserSlider);

			} else if (methodName.equals(CALL_METHOD_NAME_BRUSHEND)
					|| methodName.equals(CALL_METHOD_NAME_INPUTDATE) 
					|| methodName.equals(CALL_METHOD_NAME_NOTICESLIDER_BRUSHEND)) {
				m_log.debug("updateGraphs() CALL_METHOD_NAME_BRUSHEND or CALL_METHOD_NAME_INPUTDATE or CALL_METHOD_NAME_NOTICESLIDER_BRUSHEND. methodName=" + methodName);

				if (CollectGraphUtil.getBarFlg()
						&& (CollectGraphUtil.getTargetConditionStartDate() < xaxis_min
								|| CollectGraphUtil.getTargetConditionEndDate() > xaxis_max)) {
					String removePointsScript = getHeadGraphClassName() + ".prototype.removePoints(" + xaxis_min + ", " + xaxis_max + ", " + false + ");";
					executeScript(removePointsScript, m_browserGraph);
				}
				// xaxis_max日時まで描画されているため、この値で描画済みグラフ上の最新時間を更新する（設定しないと、自動更新ボタンクリックでプロットされない範囲が発生する）
				CollectGraphUtil.setTargetDrawnDate(xaxis_max);
				// すべてのグラフの表示期間(x軸のみ)を変更する
				String script = getHeadControlClassName() + ".trimXAxis(" + xaxis_min + ", " + xaxis_max + ", null);";
				executeScript(script, m_browserGraph);

			} else if (methodName.equals(CALL_METHOD_NAME_AUTODRAW)) {
				m_log.debug("updateGraphs() CALL_METHOD_NAME_AUTODRAW");

				isAutoGragh = true;
				// 現在表示している期間の間隔で、現在時刻で最新情報を取得する
				long term = xaxis_max - xaxis_min;
				if (xaxis_max < nowDate) {
					xaxis_max = nowDate;
				}
				xaxis_min = xaxis_max - term;
				m_log.debug("updateGraphs() xaxis_min=" + CollectGraphUtil.toDatetimeString(xaxis_min) + ", xaxis_max=" + CollectGraphUtil.toDatetimeString(xaxis_max) + ", term=" + term);

				// スライダーの選択範囲を変更する
				String script = "BrushLine.prototype.moveTarget(" + xaxis_min + ", " + xaxis_max + ");";
				executeScript(script, m_browserSlider);
				// すべてのグラフの表示期間(x軸のみ)を変更する
				script = getHeadControlClassName() + ".trimXAxis(" + (xaxis_min) + ", " + (xaxis_max) + ", null);";
				executeScript(script, m_browserGraph);
			}

			// 表示範囲外プロット消去
			deletePlots(methodName, xaxis_min, xaxis_max, false);

			// グラフを描画する
			// 以下、グラフ模式図
			//		　　　 ↓getTargetConditionStartDate()
			//		　　　　　　 ↓getTargetConditionEndDate()
			// 現在	────□□□───	現在描画中のグラフ		□グラフ箇所
			// 新	─■■■□─────	新たに描画するグラフ	□データ取得しない箇所
			//		　　　　 ↑xaxis_max							■データ取得箇所
			//		 ↑xaxis_min
			m_log.debug("updateGraphs() Plotting graphs."
					+ " TargetConditionStartDate=" + CollectGraphUtil.toDatetimeString(CollectGraphUtil.getTargetConditionStartDate())
					+ ", TargetConditionEndDate=" + CollectGraphUtil.toDatetimeString(CollectGraphUtil.getTargetConditionEndDate()));
			m_log.debug("  xaxis_min=" + CollectGraphUtil.toDatetimeString(xaxis_min) + ", xaxis_max=" + CollectGraphUtil.toDatetimeString(xaxis_max));
			if (xaxis_max <= CollectGraphUtil.getTargetConditionStartDate()
					|| CollectGraphUtil.getTargetConditionEndDate() <= xaxis_min) {
				// 新たなグラフ描画範囲が、現在の範囲から外れている場合
				// 現在	────□□□───
				// 新	─■■■──────	パターン1
				// 新	───────■■■─	パターン2
				m_log.debug("updateGraphs() X axis range is out side, adding graphs plot all.");
				addGraphPlot(xaxis_min, xaxis_max, xaxis_min, xaxis_max, nowDate);
			} else {
				// 新たなグラフ描画範囲が、現在の範囲と重なる場合は、左側、右側それぞれについてデータ取得する
				// 左側の部分についてデータ取得
				// 現在──□□□─
				// 新　─■□□──
				if (xaxis_min < CollectGraphUtil.getTargetConditionStartDate()
						&& CollectGraphUtil.getTargetConditionStartDate() <=  xaxis_max) {
					m_log.debug("updateGraphs() Adding graph plot at left side.");
					addGraphPlot(xaxis_min, CollectGraphUtil.getTargetConditionStartDate(), xaxis_min, xaxis_max, nowDate);
				}
				// 右側の部分についてデータ取得
				// 現在─□□□──
				// 新　──□□■─
				if (xaxis_min < CollectGraphUtil.getTargetConditionEndDate()
						&& CollectGraphUtil.getTargetConditionEndDate() <= xaxis_max) {
					m_log.debug("updateGraphs() Adding graph plot at right side.");
					addGraphPlot(CollectGraphUtil.getTargetConditionEndDate(), xaxis_max, xaxis_min, xaxis_max, nowDate);
				}
			}

			// y軸をきれいにする
			String yaxisParam = getHeadControlClassName() + ".trimBranch();";
			executeScript(yaxisParam, m_browserGraph);

			m_log.debug("updateGraphs() end. Target date is changed. TargetConditionStartDate=" + CollectGraphUtil.toDatetimeString(CollectGraphUtil.getTargetConditionStartDate())
					+ ", TargetConditionEndDate=" + CollectGraphUtil.toDatetimeString(CollectGraphUtil.getTargetConditionEndDate()));
			return true;
		}

		/**
		 * 画面から消えた部分の線を消します<br>
		 * javascriptの関数が[brushend][autodraw][inputdate][noticeSlider_brushend]のときのみ、この関数を実行する必要があります。<br>
		 * (javascriptの関数が[mouseup]の場合は、javascript内で線を消しています。)<br>
		 * startdateとenddateはグラフ表示範囲としてjavascriptのメンバ変数に抑えられます。<br>
		 * 
		 * @param methodName 画面から呼ばれたときの引数のmethod_name
		 * @param startdate 前半削除部の時間(～startdateを消す)
		 * @param enddate 後半削除部の時間(enddate～を消す)
		 * @param allDel 全消しするかどうか(rawからavg(hour)に強制的に変わる場合は画面に表示しているものをすべて消す)
		 */
		private void deletePlots(String methodName, Long startdate, Long enddate, boolean allDel) {
			if (methodName.equals(CALL_METHOD_NAME_BRUSHEND) || methodName.equals(CALL_METHOD_NAME_AUTODRAW) 
					|| methodName.equals(CALL_METHOD_NAME_INPUTDATE) || methodName.equals(CALL_METHOD_NAME_NOTICESLIDER_BRUSHEND)) {
				String script = getHeadGraphClassName() + ".prototype.removePoints("+ startdate + ", " + enddate + ", " + allDel + ");";
				if (CollectGraphUtil.getStackFlg()) {
					script = getHeadGraphClassName() + ".prototype.removeStack("+ startdate + ", " + enddate + ");";
				}
				executeScript(script, m_browserGraph);
				if(CollectGraphUtil.getTargetConditionStartDate() < startdate){
					CollectGraphUtil.setTargetConditionStartDate(startdate);
				}
				if(CollectGraphUtil.getTargetConditionEndDate() > enddate){
					CollectGraphUtil.setTargetConditionEndDate(enddate);
				}
			}
		}
		
		/**
		 * 引数で指定された値をlongで返します。<br>
		 * スライダー操作時に時刻(String)が小数点でくる場合がある。<br>
		 * NumberFormatExceptionが発生しないようにこちらを使用する。
		 * @param value
		 * @return
		 */
		private long parseString2Long(String value) {
			Double d_value = Double.valueOf(value);
			Long l_value = d_value.longValue();
			return l_value;
		}
	}
	/**
	 * 現在表示中のグラフに線やプロットを追加します。
	 * 
	 * @param startDate DBから取得するデータの開始時刻
	 * @param endDate DBから取得するデータの終了時刻
	 * @param selectStartDate グラフの表示開始時刻
	 * @param selectEndDate グラフの表示終了時刻
	 * @param nowDate 現在日時
	 * @throws RestConnectFailed 
	 * @throws HinemosDbTimeout
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidSetting 
	 */
	private void addGraphPlot(Long startDate, Long endDate, Long selectStartDate, Long selectEndDate, Long nowDate) 
			throws HinemosDbTimeout, InvalidUserPass, HinemosUnknown, InvalidRole, RestConnectFailed, InvalidSetting {
		m_log.debug("addGraphPlot() start. "
				+ "startDate=" + CollectGraphUtil.toDatetimeString(startDate)
				+ ", endDate=" + CollectGraphUtil.toDatetimeString(endDate)
				+ ", selectStartDate=" + CollectGraphUtil.toDatetimeString(selectStartDate)
				+ ", selectEndDate=" + CollectGraphUtil.toDatetimeString(selectEndDate)
				+ ", nowDate=" + CollectGraphUtil.toDatetimeString(nowDate));

		StringBuffer sb = CollectGraphUtil.getGraphJsonData(startDate, endDate, selectStartDate, selectEndDate, nowDate);
		if (sb == null) {
			m_log.debug("addGraphPlot() add plot data 0.");
			return;
		}
		String addparam = getHeadControlClassName() + ".addPlotAtOnce(" + sb.toString() + ");";
		long start =System.currentTimeMillis();
		m_log.debug("addGraphPlot() ControlGraph.addPlotAtOnce start");
		executeScript(addparam, m_browserGraph);
		m_log.debug("addGraphPlot() ControlGraph.addPlotAtOnce end time:" + (System.currentTimeMillis() - start) + "ms");
	}

	/**
	 * グラフコンポジットのウィジェットの構成初期化
	 *
	 * @param parent
	 * @param style
	 */
	private void initialize() {
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		this.setLayout( layout );
		
		// メンバに抑えている情報をクリアする
		CollectGraphUtil.init(false, false, new ArrayList<CollectKeyInfoResponseP1>(), false, false, false, "");
	}

	/**
	 * 画面上部に選択したサマリータイプ収集値表示名を表示します。
	 * 
	 * @param summaryType
	 * @param itemCodeList
	 */
	private void setSettingLabel (int summaryType, List<CollectKeyInfoResponseP1> itemCodeList) {
		ArrayList<String> itemCodeNameList = new ArrayList<>();
		for(RestConnectUnit unit : RestConnectManager.getActiveManagerList()) {
			for (CollectKeyInfoResponseP1 itemCode : itemCodeList) {
				String displayName = itemCode.getDisplayName();
				String itemName = itemCode.getItemName();
				if (!displayName.equals("") && !itemName.endsWith("[" + displayName + "]")) {
					itemName += "[" + displayName + "]";
				}

				String str = itemName + CollectSettingComposite.SEPARATOR_AT + itemCode.getMonitorId() +
						"(" + unit.getManagerName() + ")";
				if (!itemCodeNameList.contains(str)) {
					itemCodeNameList.add(str);
				}
			}
		}
		StringBuffer itemCodeStr = new StringBuffer();
		for (String itemCodeName : itemCodeNameList) {
			if (0 < itemCodeStr.length()) {
				itemCodeStr.append(", ");
			}
			itemCodeStr.append(HinemosMessage.replace(itemCodeName));
		}
		if (itemCodeStr.length() > 256) {
			itemCodeStr = new StringBuffer(itemCodeStr.substring(0, 256));
			itemCodeStr.append("...");
		}
		settingLabel.setText(Messages.getString("collection.summary.type")  + " : " + 
				SummaryTypeMessage.typeToString(summaryType) + ",   " +
				Messages.getString("collection.display.name") + " : " + itemCodeStr.toString());
	}
	
	/**
	 * 現在表示中のグラフをすべて削除します。
	 */
	public void deleteGraphs() {
		String param = CONTROL_CLASS_NAME_LINESTACK + ".delDiv();";
		executeScript(param, m_browserGraph);
		param = CONTROL_CLASS_NAME_PIE + ".delDiv();";
		executeScript(param, m_browserGraph);
		param = CONTROL_CLASS_NAME_SCATTER + ".delDiv();";
		executeScript(param, m_browserGraph);
		param = CONTROL_CLASS_NAME_BARSTACK + ".delDiv();";
		executeScript(param, m_browserGraph);
		// メンバで保持している情報はdrawGraphsでクリアしている
	}

	/**
	 * 適用ボタン押下後に呼ばれます。
	 * 
	 * 
	 * @param collectKeyInfoList
	 * @param summaryCode
	 * @param facilityInfoMap マネージャ名、FacilityInfoのマップ
	 * @param returnFlg 折り返し有無
	 * @throws RestConnectFailed 
	 * @throws HinemosDbTimeout
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 * @throws InvalidUserPass
	 * @throws InvalidSetting 
	 */
	public void drawGraphs(List<CollectKeyInfoResponseP1> collectKeyInfoList, String selectInfoStr, int summaryType, Map<String, List<FacilityInfoResponse>> facilityInfoMap, 
			boolean returnFlg, boolean returnKindFlg, boolean totalFlg, boolean stackflg, boolean appflg, 
			boolean threflg, boolean pieflg, boolean scatterflg, boolean legendFlg, boolean sigmaFlg, boolean barFlg) 
					throws HinemosDbTimeout, InvalidUserPass, HinemosUnknown, InvalidRole, RestConnectFailed, InvalidSetting {

		m_log.info("drawGraphs() START. selectInfoStr:" + selectInfoStr + ", summaryType:" + summaryType);

		if (!completed || !slider_completed) {
			m_log.info("uncompleted!!");
			return;
		}
		
		setSettingLabel(summaryType, collectKeyInfoList);
		
		CollectGraphUtil.init(totalFlg, stackflg, collectKeyInfoList, pieflg, scatterflg, barFlg, selectInfoStr);
		
		// 画面に表示する文言の設定
		setGraphMessages();
		
		// グラフ種別などを文字列に変換する
		boolean booleanArr[] = {returnFlg, returnKindFlg, totalFlg, stackflg, appflg, threflg, pieflg, scatterflg, legendFlg, sigmaFlg, barFlg, ClusterControlPlugin.isRAP()};
		String setting = getBooleanString(booleanArr);
		m_log.info("setting boolean[return, returnkind, total, stack, applox, thre, pie, scatter, legend, sigma, bar, rap]:" + setting);
		
		// 画面上のすべてのdivを削除する
		deleteGraphs();
		
		int facilitySize = 0;
		int graphSize = 0;
		for (Map.Entry<String, List<FacilityInfoResponse>> entry : facilityInfoMap.entrySet()) {
			if (entry.getValue() != null) {
				for (FacilityInfoResponse info : entry.getValue()) {
					// マネージャ名とファシリティ名のマップを作成する
					facilitySize = CollectGraphUtil.sortManagerNameFacilityIdMap(entry.getKey(), info, facilitySize);
				}
			}
		}
		graphSize = facilitySize * collectKeyInfoList.size();
		
		int managercount = CollectGraphUtil.getSelectManagerCount();
		m_log.info("drawGraphs() graphSize:" + graphSize + ", itemCodeList.size:" + collectKeyInfoList.size() 
		+ ", facilityLength:" + facilitySize + ", managerCount:" + managercount);
		if (threflg && managercount != 1 && (!stackflg && !pieflg && !scatterflg && !barFlg)) {
			MessageDialog.openError(
					null, 
					Messages.getString("error"), // 複数マネージャで上限下限表示は実行できません
					Messages.getString("message.collection.graph.do.not.display.upperandlowerlimits.in.multiplemanager"));
			return;
		}
		int preferenceCount = ClusterControlPlugin.getDefault().getPreferenceStore().getInt(PerformancePreferencePage.P_GRAPH_MAX);
		if (graphSize > preferenceCount) {
			// Preferenceで指定したグラフ数以上は表示できないメッセージ
			String args[] = {String.valueOf(graphSize), String.valueOf(preferenceCount)};
			if (!MessageDialog.openConfirm(null, Messages.getString("confirmed"),
					Messages.getString("message.collection.graph.limitsize", args))) {
				return;
			}
			m_log.info("取得するグラフデータ数超過のため、表示数制限 " + preferenceCount + "/" + graphSize);
		}
		
		// マネージャ名をダミーマネージャ名に変換する
		CollectGraphUtil.addManagerDummyName();
		
		// グラフのベース情報を作成
		String createGraphJson = CollectGraphUtil.drawGraphSheets(summaryType, appflg);
		// グラフのベースの描画
		String params = getHeadControlClassName() + ".addGraphAtOnce(" + createGraphJson + ", " + facilitySize + ", " + preferenceCount + ", '" + setting + "');";
		Long start = System.currentTimeMillis();
		m_log.info("start base_graph Draw");
		executeScript(params, m_browserGraph);
		m_log.info("end base_graph Draw time:" + (System.currentTimeMillis() - start) + "ms");
		
		// グラフのズームアイテムの描画、初期表示時は100%とする
		// 自動調整を実行する可能性があるので、グラフをすべて描画し終わってからinitZoomする
		String preferenceZoom = ClusterControlPlugin.getDefault().getPreferenceStore().getString(CollectSettingComposite.P_COLLECT_GRAPH_ZOOM_LEVEL);
		CollectGraphUtil.setGraphZoomSize(preferenceZoom);
		params = "initZoom(\'" + CollectGraphUtil.getGraphZoomSize() + "\', " + returnFlg + ", " + CollectGraphUtil.getScreenWidth() +");";
		executeScript(params, m_browserGraph);

		// hinemos_web.cfgから取得したインターバルを設定する(空白、null、0以下の値は修正前のデフォルトの7000で置き換え)
		String zipfileCreateInterval = System.getProperty("graph.zipfile.create.interval");
		String interval = "7000";
		if("".equals(zipfileCreateInterval) || zipfileCreateInterval == null){
			zipfileCreateInterval = interval;
		}else{
			boolean isNum = zipfileCreateInterval.matches("[0-9]+[\\.]?[0-9]*");
			if(!isNum){
				zipfileCreateInterval = interval;
			}else{
				if(Long.parseLong(zipfileCreateInterval) < 1){
					zipfileCreateInterval = interval;
				}
			}
		}
		m_log.info("start set zipfile interval=" + zipfileCreateInterval);
		params = "initZipfileCreateInterval(" + zipfileCreateInterval + ");";
		executeScript(params, m_browserGraph);
		
		// スライダーの削除 -> 作成
		int totalGraphSize = graphSize;
		if (graphSize > preferenceCount) {
			totalGraphSize = preferenceCount;
		}
		params = "ControlBrushLine.delCreateBrush(" + CollectGraphUtil.getSliderStart() 
				+ "," + CollectGraphUtil.getSliderEnd()
				+ "," + CollectGraphUtil.getTargetConditionStartDate()
				+ "," + CollectGraphUtil.getTargetConditionEndDate()
				+ "," + totalGraphSize + ");";
		executeScript(params, m_browserSlider);
		
		// collectIdの収集
		CollectGraphUtil.collectCollectIdInfo(summaryType);
		
		// プロット情報の取得と描画
		addGraphPlot(CollectGraphUtil.getTargetConditionStartDate(), CollectGraphUtil.getTargetConditionEndDate(), 
				CollectGraphUtil.getTargetConditionStartDate(), CollectGraphUtil.getTargetConditionEndDate(),
				CollectGraphUtil.getManagerTime());
		
		// y軸をきれいにする
		String yaxisParam = getHeadControlClassName() + ".trimBranch();";
		executeScript(yaxisParam, m_browserGraph);

		m_log.info("drawGraphs() END.");
	}
	
	/** 
	 * 多言語対応のため、画面に表示する文言・メッセージ・タイムゾーンを渡します。
	 * グラフ画面・スライダー画面ともにエラーが発生する可能性があるので、両方のbrowserで実施。
	 */
	private void setGraphMessages() {
		String param = "setGraphMessages({"
				+ "\'autoadjust\':\'" + CollectGraphUtil.escapeParam(Messages.getString("collect.autoadjust")) + "\', " // 自動調整
				+ "\'autoupdate\':\'" + CollectGraphUtil.escapeParam(Messages.getString("collect.autoupdate")) + "\', " // 自動更新
				+ "\'bulkpng\':\'" + CollectGraphUtil.escapeParam(Messages.getString("collect.bulkpng")) + "\', " // 一括PNG
				+ "\'nodes\':\'" + CollectGraphUtil.escapeParam(Messages.getString("collect.nodes")) + "\', " // ノード
				+ "\'total\':\'" + CollectGraphUtil.escapeParam(Messages.getString("collect.total")) + "\', " // 全
				+ "\'time\':\'" + CollectGraphUtil.escapeParam(Messages.getString("time")) + "\', " // 日時
				+ "\'priority\':\'" + CollectGraphUtil.escapeParam(Messages.getString("priority")) + "\', " // 重要度
				+ "\'message\':\'" + CollectGraphUtil.escapeParam(Messages.getString("message")) + "\', " // メッセージ
				+ "\'total\':\'" + CollectGraphUtil.escapeParam(Messages.getString("collect.total")) + "\', " // 全
				+ "\'information\':\'" + CollectGraphUtil.escapeParam(Messages.getString("info")) + "\', "
				+ "\'warning\':\'" + CollectGraphUtil.escapeParam(Messages.getString("warning")) + "\', "
				+ "\'critical\':\'" + CollectGraphUtil.escapeParam(Messages.getString("critical")) + "\', "
				+ "\'unknown\':\'" + CollectGraphUtil.escapeParam(Messages.getString("unknown")) + "\', "
				+ "\'detail\':\'" + CollectGraphUtil.escapeParam(Messages.getString("detail")) + "\', "// 詳細
				+ "\'prediction\':\'" + CollectGraphUtil.escapeParam(Messages.getString("collect.prediction", new String[]{"@@"})) + "\', "// 予測先({@@}分後)
				+ "\'timezoneoffset\':" + TimezoneUtil.getTimeZoneOffset()/(1000*60) + ", "// タイムゾーンオフセット(数値)
				+ "\'captureerror\':\'" + CollectGraphUtil.escapeParam(Messages.getString("message.collection.graph.capture.morethan.displayed")) + "\', "// キャプチャファイルが100以上あるため、実行できません
				+ "\'unexpectederror\':\'" + CollectGraphUtil.escapeParam(Messages.getString("message.collection.graph.unexpected.error")) + "\', "// 予期しないエラーが発生しました。
				+ "\'datainsufficient\':\'" + CollectGraphUtil.escapeParam(Messages.getString("message.collection.6")) + "\'" // 収集データが不足しています
				+ "});"; 
		executeScript(param, m_browserGraph);
		executeScript(param, m_browserSlider);
	}
	
	/**
	 * ビューの更新時に呼ばれるアクション(最新時刻のデータを表示)
	 */
	@Override
	public void update() {
		super.update();
	}
	
	public String getZoomLevel() {
		return CollectGraphUtil.getGraphZoomSize();
	}
	
	/**
	 * グラフ描画時にエラーが発生した場合に、グラフとスライダーを非表示にします。
	 */
	public void removeGraphSliderDisp() {
		m_log.debug("removeGraphSliderDisp()");
		String delSlider = "ControlBrushLine.delBrush()";
		executeScript(delSlider, m_browserSlider);
		deleteGraphs();
		String delGraphZoom = "removeZoomArea()";
		executeScript(delGraphZoom, m_browserGraph);
	}
	
	/**
	 * 指定されたscript(Javascript)をexecBrowserに対して実行します。
	 * 
	 * @param script
	 * @param execBrowser
	 * @return
	 */
	private Object executeScript(String script, Browser execBrowser) {
		this.lastScript = script;
		m_log.debug("executeScript() script=" + this.lastScript);
		Object retObj = execBrowser.evaluate(this.lastScript);
		return retObj;
	}
	
	/**
	 * booleanの配列を数字の配列に変換します。<br>
	 * true  -> 1<br>
	 * false -> 0<br>
	 * @param booleanArr
	 * @return
	 */
	private static String getBooleanString(boolean[] booleanArr) {
		StringBuffer booleanBuffer = new StringBuffer();
		for (boolean bool : booleanArr) {
			int str = BooleanUtils.toInteger(bool);
			booleanBuffer.append(str);
		}
		String retStr = booleanBuffer.toString();
		return retStr;
	}
	
	/**
	 * 選択したグラフ種別で実行する制御クラスの名前を返します。
	 * @return
	 */
	private static String getHeadControlClassName() {
		String headClassName = CONTROL_CLASS_NAME_LINESTACK;
		if (CollectGraphUtil.getPieFlg()) {
			headClassName = CONTROL_CLASS_NAME_PIE;
		} else if (CollectGraphUtil.getScatterFlg()) {
			headClassName = CONTROL_CLASS_NAME_SCATTER;
		} else if (CollectGraphUtil.getBarFlg()) {
			headClassName = CONTROL_CLASS_NAME_BARSTACK;
		}
		return headClassName;
	}
	
	/**
	 * 選択したグラフ種別で実行するクラスの名前を返します。
	 * @return
	 */
	private static String getHeadGraphClassName() {
		String headClassName = GRAPH_CLASS_NAME_LINESTACK;
		if (CollectGraphUtil.getPieFlg()) {
			headClassName = GRAPH_CLASS_NAME_PIE;
		} else if (CollectGraphUtil.getScatterFlg()) {
			headClassName = GRAPH_CLASS_NAME_SCATTER;
		} else if (CollectGraphUtil.getBarFlg()) {
			headClassName = GRAPH_CLASS_NAME_BARSTACK;
		}
		return headClassName;
	}
}
