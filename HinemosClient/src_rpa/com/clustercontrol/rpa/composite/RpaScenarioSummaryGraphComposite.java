/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.composite;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.openapitools.client.model.GetRpaScenarioOperationResultSummaryDataResponse;
import org.openapitools.client.model.GetRpaScenarioOperationResultSummaryForBarResponse;
import org.openapitools.client.model.GetRpaScenarioOperationResultSummaryForPieResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.UrlNotFound;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.rest.endpoint.rpa.dto.SummaryTypeEnum;
import com.clustercontrol.rpa.preference.RpaPreferencePage;
import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.rpa.view.RpaScenarioSummaryGraphView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.HtmlLoaderUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.util.UIManager;

/**
 * 集計グラフを表示するコンポジットクラス
 */
public class RpaScenarioSummaryGraphComposite extends Composite {
	private static Log log = LogFactory.getLog(RpaScenarioSummaryGraphComposite.class);
	
	/** グラフを表示する領域 */
	private Browser browserGraph;
	
	/** 年月設定コンポジット */
	private RpaScenarioYearMonthComposite yearMonthComposite;
	
	/** [適用]ボタン */
	private Button apply = null;
	
	/** このコンポジットを呼び出しているビュー */
	private RpaScenarioSummaryGraphView summaryGraphView;

	/**
	 * Browserの画面起動完了フラグ(trueなるまでグラフの描画はできない。trueになってから処理を始める)
	 */
	private boolean completed = false;

	/**
	 * グラフ用javascript制御名
	 */
	private static final String CONTROL_CLASS_NAME = "ControlGraph";
	/**
	 * javascriptでエラーが発生した場合のメソッド名
	 */
	private static final String CALL_METHOD_NAME_JAVASCRIPT_ERROR = "javascript_error";
	
	private final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss.SSS";

	/** 区切り文字(#!#) */
	protected static final String SEPARATOR_HASH_EX_HASH = "#!#";
	
	private String lastScript = "";
	/**
	 * コンストラクタ
	 *
	 * @param settings グラフのヘッダ情報
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 */
	public RpaScenarioSummaryGraphComposite(Composite parent, int style, RpaScenarioSummaryGraphView graphView) {
		super(parent, style);
		// 初期化
		initialize();
		this.summaryGraphView = graphView;
		
		GridLayout gridLayout = new GridLayout(10, false);
		this.setLayout(gridLayout);
		
		GridData gridData = new GridData();
		Label labelStartDate = new Label(this, SWT.LEFT);
		labelStartDate.setLayoutData(new GridData(60, SizeConstant.SIZE_LABEL_HEIGHT));
		labelStartDate.setText(Messages.getString("rpa.scenario.summary.graph.target.month") + " : ");
		
		yearMonthComposite = new RpaScenarioYearMonthComposite(this, SWT.NONE);
		yearMonthComposite.setLayoutData(gridData);
		
		// 表示ボタン
		apply = new Button(this, SWT.NONE);
		apply.setText(Messages.getString("show"));
		apply.setToolTipText(Messages.getString("show"));
		apply.setLayoutData(new GridData(100, SizeConstant.SIZE_BUTTON_HEIGHT));
		apply.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				List<String> scopeList = summaryGraphView.getSelectScopeList();
				if (scopeList == null || scopeList.size() == 0){
					MessageDialog.openError(
							null,
							Messages.getString("error"),
							Messages.getString("message.rpa.scenario.operation.result.summary.graph.scope"));
					return;
				}
				
				// 現在のファシリティツリーの選択状態の文字列を取得
				List<String> facilityList = new ArrayList<String>();
				for (String selectStr : scopeList) {
					String[] nodeDetail = selectStr.split(SEPARATOR_HASH_EX_HASH);
					if (nodeDetail.length != 0 && nodeDetail[nodeDetail.length - 1].equals(String.valueOf(FacilityConstant.TYPE_NODE))) {
						String facilityId = nodeDetail[nodeDetail.length - 2];
						facilityList.add(facilityId);
					}
				}
				
				try {
					// 適用ボタンを押下不可にする
					apply.setEnabled(false);
					
					String scopes = String.join(",", facilityList);
					drawGraphs(scopes, yearMonthComposite.getValue().getTime());
				} catch (InvalidRole e) {
					log.error("drawGraphs InvalidRole_Exception");
					MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
					deleteGraphs();
				} catch (InvalidUserPass e) {
					log.error("drawGraphs InvalidUserPass_Exception");
					MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.45"));
					deleteGraphs();
				} catch (HinemosDbTimeout e) {
					String message = HinemosMessage.replace(e.getMessage());
					log.error("drawGraphs グラフ描画時にエラーが発生 message=" + message, e);
					MessageDialog.openError(
							null,
							Messages.getString("error"),
							Messages.getString("message.rpa.scenario.operation.result.summary.graph.unexpected.error") + " : " + message);
					deleteGraphs();
				} catch (Exception e) {
					log.error("drawGraphs グラフ描画時にエラーが発生 message=" + e.getMessage(), e);
					MessageDialog.openError(
							null,
							Messages.getString("error"),
							Messages.getString("message.rpa.scenario.operation.result.summary.graph.unexpected.error") + " : " + e.getMessage());
					deleteGraphs();
				} finally {
					// 適用ボタンを押下可能にする
					apply.setEnabled(true);
				}
			}
		});
		
		try {
			browserGraph = new Browser(this, SWT.NONE);
			
			GridData gridData1;
			gridData1 = new GridData();
			gridData1.horizontalAlignment = GridData.FILL;
			gridData1.verticalAlignment = GridData.FILL;
			gridData1.grabExcessHorizontalSpace = true;
			gridData1.grabExcessVerticalSpace = true;
			gridData1.horizontalSpan = 10;
			browserGraph.setLayoutData(gridData1);
			
			loadGraphBrowser();
			log.warn("set URL");

		} catch (Exception e) {
			log.warn("CollectGraphComposite : " + e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
	
	private void loadGraphBrowser(){
		HtmlLoaderUtil.load(browserGraph, "graph.html", "com/clustercontrol/rpa/composite/");
		browserGraph.addProgressListener(new ProgressListener() {
			public void completed(ProgressEvent event) {
				// ページのロードが完了してからjavascriptを実行する
				completed = true;
				new CustomFunction(browserGraph, "theJavaFunction");
				log.debug("add graph completed!!!!!");
				summaryGraphView.setScopeListCheckedTreeItems();
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
		 *   javascriptでエラーが発生した場合(javascript_error)<br>
		 *     -> エラーログを出力するため<br>
		 */
		@Override
		public Object function (Object[] arguments) {
			Object retObj = null;
			try {
				retObj = executeFromJavascript(arguments);
			} catch (InvalidRole e) {
				log.error("function InvalidRole_Exception");
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} catch (InvalidUserPass e) {
				log.error("function InvalidUserPass_Exception");
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.45"));
			} catch (HinemosDbTimeout e) {
				String message = HinemosMessage.replace(e.getMessage());
				log.error("グラフ描画時にエラーが発生 message=" + message, e);
				MessageDialog.openError(
						null,
						Messages.getString("error"),
						Messages.getString("message.rpa.scenario.operation.result.summary.graph.unexpected.error") + " : " + message);
			} catch (Exception e) {
				log.error("グラフ描画時にエラーが発生 message=" + e.getMessage(), e);
				MessageDialog.openError(
						null,
						Messages.getString("error"),
						Messages.getString("message.rpa.scenario.operation.result.summary.graph.unexpected.error") + " : " + e.getMessage());
			}
			return retObj;
		}
		
		private Object executeFromJavascript(Object[] arguments) 
				throws HinemosDbTimeout, InvalidUserPass, HinemosUnknown, InvalidRole, RestConnectFailed {
			SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
			dateFormat.setTimeZone(TimezoneUtil.getTimeZone());
			
			String ret = (String)arguments[0];
			log.debug("wow!called from javascript!" + ret);
			log.info("javascript call:" + ret);

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
				log.error("executeFromJavascript() ERROR:" + ret);
				log.error("execScript:" + lastScript);
				if (lastScript.length() > 2000) {
					lastScript = lastScript.substring(0, 2000) + "...";
				}
				MessageDialog.openInformation(
						null, 
						Messages.getString("error"),
						Messages.getString("message.rpa.scenario.operation.result.summary.graph.unexpected.error") + "\n" + ret + "\n\n" + lastScript);

				return null;
			}

			return true;
		}
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
	}
	
	/**
	 * 現在表示中のグラフをすべて削除します。
	 */
	public void deleteGraphs() {
		String param = CONTROL_CLASS_NAME + ".delDiv();";
		executeScript(param, browserGraph);
	}

	/**
	 * 適用ボタン押下後に呼ばれるグラフ描画
	 */
	public void drawGraphs(String facilityIds, Long targetMonth) throws HinemosDbTimeout, InvalidUserPass, HinemosUnknown, InvalidRole, RestConnectFailed {
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
		dateFormat.setTimeZone(TimezoneUtil.getTimeZone());

		if (!completed) {
			log.info("uncompleted!!");
			return;
		}
		
		// 画面上のすべてのグラフを削除する
		deleteGraphs();
		
		// シナリオ表示数とノード表示数をプレファレンスページから取得する
		int maxDisplayScenarios = ClusterControlPlugin.getDefault().getPreferenceStore().getInt(
				RpaPreferencePage.P_MAX_DISPLAY_SCENARIOS);
		int maxDisplayNodes = ClusterControlPlugin.getDefault().getPreferenceStore().getInt(
				RpaPreferencePage.P_MAX_DISPLAY_NODES);
		
		// 先頭のグラフが描画された場合、他のグラフも描画する
		if (drawExecRpaScenarioByDaysBarChart(facilityIds, targetMonth)){
			drawReductionByTimeZoneBarChart(facilityIds, targetMonth);
			drawErrorRatePieChart(facilityIds, targetMonth);
			drawReductionPieChart(facilityIds, targetMonth);
			drawErrorsByScenariosBarChart(facilityIds, targetMonth, maxDisplayScenarios);
			drawReductionByScenariosBarChart(facilityIds, targetMonth, maxDisplayScenarios);
			drawErrorsByNodesBarChart(facilityIds, targetMonth, maxDisplayNodes);
			drawReductionByNodesBarChart(facilityIds, targetMonth, maxDisplayNodes);
		}

		log.info("drawGraphs() END time:" + dateFormat.format(new Date()));
	}
	
	private Boolean drawExecRpaScenarioByDaysBarChart(String facilityIds, Long targetMonth){
		// 1か月(31日)分が最大
		Integer limit = 31;

		String graphString = setValueForBarChart(facilityIds, targetMonth, SummaryTypeEnum.TYPE_DAILY_COUNT, limit);
		String createGraphJson = "";
		Boolean isWriteble = true;
		if (graphString != null && !graphString.equals(Messages.getString("message.rpa.scenario.operation.result.summary.graph.nodata"))){
			createGraphJson = "[" + graphString + "]";
		} else {
			createGraphJson = "[" + "{'name' : '" + graphString + "', 'values' : []}" + "]";
			isWriteble = false;
		}
		
		String params = getHeadControlClassName() + ".addExecScenarioGraph(" + createGraphJson + ");";
		Long start = System.currentTimeMillis();
		log.info("start base_graph Draw");
		executeScript(params, browserGraph);
		log.info("end base_graph Draw time:" + (System.currentTimeMillis() - start) + "ms");
		return isWriteble;
	}
	
	private void drawReductionByTimeZoneBarChart(String facilityIds, Long targetMonth){
		// 時間帯（24時間）分が最大
		Integer limit = 24;

		String graphJson = setValueForBarChart(facilityIds, targetMonth, SummaryTypeEnum.TYPE_HOURLY_REDUCTION, limit);
		String createGraphJson = "[" + graphJson + "]";
		
		String params = getHeadControlClassName() + ".addReductionByTimeZoneGraph(" + createGraphJson + ");";
		Long start = System.currentTimeMillis();
		log.info("start base_graph Draw");
		executeScript(params, browserGraph);
		log.info("end base_graph Draw time:" + (System.currentTimeMillis() - start) + "ms");
	}
	
	private String setValueForBarChart(String facilityIds, Long targetMonth, SummaryTypeEnum dataType, Integer limit){
		GetRpaScenarioOperationResultSummaryForBarResponse response = new GetRpaScenarioOperationResultSummaryForBarResponse();
		
		//グラフ描画用の情報を取得
		Map<String, String> errorMsgs = new ConcurrentHashMap<String, String>();
		for(String managerName : RestConnectManager.getActiveManagerSet()) {
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(managerName);
			try {
				response = wrapper.getRpaScenarioOperationResultSummaryForBar(facilityIds, targetMonth, dataType, limit);
			} catch (InvalidRole e) {
				// 権限なし
				errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
			} catch (Exception e) {
				// エンタープライズ機能が無効の場合は無視する
				if(UrlNotFound.class.equals(e.getCause().getClass())) {
					continue;
				}
				// 上記以外の例外
				String errMessage = HinemosMessage.replace(e.getMessage());
				log.warn("update(), " + errMessage, e);
				errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + errMessage);
			}
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}
		
		if (dataType == SummaryTypeEnum.TYPE_DAILY_COUNT){
			int count = 0;
			for( GetRpaScenarioOperationResultSummaryDataResponse data : response.getDatas()){
				if (data.getValues().get(0) == 0 && data.getValues().get(1) == 0){
					count++;
				};
			}
			
			if (count == response.getDatas().size()){
				return Messages.getString("message.rpa.scenario.operation.result.summary.graph.nodata");
			}
		}
		
		List<String> successValues = new ArrayList<>();
		List<String> errorValues = new ArrayList<>();
		List<String> titles = new ArrayList<>();

		// 成功件数(削減工数)、失敗件数(実行時間)、グラフタイトルを取得
		int max = response.getDatas().size();
		for (int i = 0; i < max; i++){
			Long successValue = Math.round(response.getDatas().get(i).getValues().get(0));
			successValues.add(successValue.toString());
			Long errorValue = Math.round(response.getDatas().get(i).getValues().get(1));
			errorValues.add(errorValue.toString());
			titles.add(response.getDatas().get(i).getItem());
		}
		
		String name = response.getName();
		
		// 成功件数
		List<String> joinValues = new ArrayList<>();
		List<String> joinList = new ArrayList<>();
		for (int i = 0; i < max; i++){
			String json = "{'y':" + successValues.get(i) + ", 'x':'" + titles.get(i) + "'}";
			joinList.add(json);
		}
		String string = String.join(",", joinList);
		String json1 = "{'name' : '" + name + "', 'values' : [" + string + "]}";
		joinValues.add(json1);
		
		// 失敗件数
		joinList = new ArrayList<>();
		for (int i = 0; i < max; i++){
			String json = "{'y':" + errorValues.get(i) + ", 'x':'" + titles.get(i) + "'}";
			joinList.add(json);
		}
		string = String.join(",", joinList);
		String json2 = "{'name' : '" + name + "', 'values' : [" + string + "]}";
		joinValues.add(json2);
		
		return String.join(",", joinValues);
	}
	
	private void drawErrorsByScenariosBarChart(String facilityIds, Long targetMonth, int maxDisplayScenario){
		String graphJson = setValueForBarChart(facilityIds, targetMonth, SummaryTypeEnum.TYPE_SCENARIO_ERRORS, maxDisplayScenario);
		String createGraphJson = "[" + graphJson + "]";
		
		String params = getHeadControlClassName() + ".addErrorsGraph(" + createGraphJson + ");";
		Long start = System.currentTimeMillis();
		log.info("start base_graph Draw");
		executeScript(params, browserGraph);
		log.info("end base_graph Draw time:" + (System.currentTimeMillis() - start) + "ms");
	}
	
	private void drawErrorsByNodesBarChart(String facilityIds, Long targetMonth, int maxDisplayNodes){
		String graphJson = setValueForBarChart(facilityIds, targetMonth, SummaryTypeEnum.TYPE_NODE_ERRORS, maxDisplayNodes);
		String createGraphJson = "[" + graphJson + "]";
		
		String params = getHeadControlClassName() + ".addErrorsGraph(" + createGraphJson + ");";
		Long start = System.currentTimeMillis();
		log.info("start base_graph Draw");
		executeScript(params, browserGraph);
		log.info("end base_graph Draw time:" + (System.currentTimeMillis() - start) + "ms");
	}
	
	private void drawReductionByScenariosBarChart(String facilityIds, Long targetMonth, int maxDisplayScenarios){
		String graphJson = setValueForBarChart(facilityIds, targetMonth, SummaryTypeEnum.TYPE_SCENARIO_REDUCTION, maxDisplayScenarios);
		String createGraphJson = "[" + graphJson + "]";
		
		String params = getHeadControlClassName() + ".addReductionGraph(" + createGraphJson + ");";
		Long start = System.currentTimeMillis();
		log.info("start base_graph Draw");
		executeScript(params, browserGraph);
		log.info("end base_graph Draw time:" + (System.currentTimeMillis() - start) + "ms");
	}
	
	private void drawReductionByNodesBarChart(String facilityIds, Long targetMonth, int maxDisplayNodes){
		String graphJson = setValueForBarChart(facilityIds, targetMonth, SummaryTypeEnum.TYPE_NODE_REDUCTION, maxDisplayNodes);
		String createGraphJson = "[" + graphJson + "]";
		
		String params = getHeadControlClassName() + ".addReductionGraph(" + createGraphJson + ");";
		log.info("start base_graph Draw = " + params);
		Long start = System.currentTimeMillis();
		log.info("start base_graph Draw");
		executeScript(params, browserGraph);
		log.info("end base_graph Draw time:" + (System.currentTimeMillis() - start) + "ms");
	}
	
	private void drawErrorRatePieChart(String facilityIds, Long targetMonth){
		Integer limit = 10;

		// グラフのベース情報を作成
		String createGraphJson = setValueForPieChart(facilityIds, targetMonth, SummaryTypeEnum.TYPE_ERRORS, limit);
		// グラフのベースの描画
		String params = getHeadControlClassName() + ".addErrorsPieGraph(" + createGraphJson + ");";
		Long start = System.currentTimeMillis();
		log.info("start base_graph Draw");
		executeScript(params, browserGraph);
		log.info("end base_graph Draw time:" + (System.currentTimeMillis() - start) + "ms");
	}
	
	private void drawReductionPieChart(String facilityIds, Long targetMonth){
		Integer limit = 10;
		
		// グラフのベース情報を作成
		String createGraphJson = setValueForPieChart(facilityIds, targetMonth, SummaryTypeEnum.TYPE_REDUCTION, limit);
		// グラフのベースの描画
		String params = getHeadControlClassName() + ".addReductionPieGraph(" + createGraphJson + ");";
		Long start = System.currentTimeMillis();
		log.info("start base_graph Draw");
		executeScript(params, browserGraph);
		log.info("end base_graph Draw time:" + (System.currentTimeMillis() - start) + "ms");
	}
	
	private String setValueForPieChart(String facilityIds, Long targetMonth, SummaryTypeEnum dataType, Integer limit){
		GetRpaScenarioOperationResultSummaryForPieResponse response = new GetRpaScenarioOperationResultSummaryForPieResponse();
		
		//グラフ描画用の情報を取得
		Map<String, String> errorMsgs = new ConcurrentHashMap<String, String>();
		for(String managerName : RestConnectManager.getActiveManagerSet()) {
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(managerName);
			try {
				response = wrapper.getRpaScenarioOperationResultSummaryForPie(facilityIds, targetMonth, dataType);
			} catch (InvalidRole e) {
				// 権限なし
				errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
			} catch (Exception e) {
				// 上記以外の例外
				String errMessage = HinemosMessage.replace(e.getMessage());
				log.warn("update(), " + errMessage, e);
				errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + errMessage);
			}
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}
		
		List<String> joinValues = new ArrayList<>();
		joinValues.add(String.valueOf(Math.round(response.getDatas().get(0))));
		joinValues.add(String.valueOf(Math.round(response.getDatas().get(1))));
		String string = String.join(",", joinValues);
		
		String name = response.getName();
		
		// グラフのベース情報を作成
		return "{'value':[" + string + "], 'name':'" + name + "'}";
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
		log.debug("script:" + this.lastScript);
		Object retObj = execBrowser.evaluate(this.lastScript);
		return retObj;
	}
	
	/**
	 * 実行する制御クラスの名前を返します。
	 * @return
	 */
	private static String getHeadControlClassName() {
		String headClassName = CONTROL_CLASS_NAME;
		return headClassName;
	}
}
