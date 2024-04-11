/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.collect.action;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.openapitools.client.model.AddCollectMasterRequest;
import org.openapitools.client.model.CollectMasterInfoResponse;
import org.openapitools.client.model.CollectorCalcMethodMstDataRequest;
import org.openapitools.client.model.CollectorCalcMethodMstDataResponse;
import org.openapitools.client.model.CollectorCategoryCollectMstDataRequest;
import org.openapitools.client.model.CollectorCategoryCollectMstDataResponse;
import org.openapitools.client.model.CollectorCategoryMstDataRequest;
import org.openapitools.client.model.CollectorCategoryMstDataResponse;
import org.openapitools.client.model.CollectorItemCalcMethodMstDataRequest;
import org.openapitools.client.model.CollectorItemCalcMethodMstDataResponse;
import org.openapitools.client.model.CollectorItemCodeMstDataRequest;
import org.openapitools.client.model.CollectorItemCodeMstDataResponse;
import org.openapitools.client.model.CollectorPollingMstDataRequest;
import org.openapitools.client.model.CollectorPollingMstDataResponse;

import org.openapitools.client.model.PerformanceMonitorInfoResponse;

import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.collect.util.CollectRestClientWrapper;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.ClearMethod;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.ExportMethod;
import com.clustercontrol.utility.settings.ImportMethod;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.settings.collect.conv.CollectConv;
import com.clustercontrol.utility.settings.collect.conv.CollectMasterConv;
import com.clustercontrol.utility.settings.master.xml.ChildItems;
import com.clustercontrol.utility.settings.master.xml.CollectMasters;
import com.clustercontrol.utility.settings.master.xml.CollectorCalcMethodFrame;
import com.clustercontrol.utility.settings.master.xml.CollectorCalcMethods;
import com.clustercontrol.utility.settings.master.xml.CollectorCategoryCollectFrame;
import com.clustercontrol.utility.settings.master.xml.CollectorCategoryCollects;
import com.clustercontrol.utility.settings.master.xml.CollectorItemCalcFrame;
import com.clustercontrol.utility.settings.master.xml.CollectorItemCalcMethods;
import com.clustercontrol.utility.settings.master.xml.CollectorItemCodeFrame;
import com.clustercontrol.utility.settings.master.xml.CollectorItemCodes;
import com.clustercontrol.utility.settings.master.xml.CollectorItems;
import com.clustercontrol.utility.settings.master.xml.PollingCollector;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.ui.dialog.MessageDialogWithScroll;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.utility.util.XmlMarshallUtil;

/**
 * 収集項目定義情報のマスター情報を取得、設定します。<br>
 * XMLファイルに定義された収集項目定義情報をPostgreSQLに反映させるクラス<br>
 * ただし、すでに登録されている収集項目定義情報と重複する場合はスキップされる。
 * 
 * @param action 動作
 * @param XMLファイルパス（ユーザ情報定義の入力元）
 * 
 * @version 6.0.0
 * @since 1.0.0
 * 
 * DIFF機能はありません。
 * 
 * 
 */
public class CollectMasterAction {

	/* ロガー */
	protected static Logger log = Logger.getLogger(CollectMasterAction.class);

	public CollectMasterAction() throws ConvertorException {
		super();
	}
	
	/**
	 * リソース監視が存在するかチェックし、存在する場合ユーザに<BR>
	 * 削除可否を問うダイアログを出力します。
	 * 
	 * @return 削除可否(true:削除可、false:削除不可)
	 */
	
	private boolean checkDelete(){
		
		boolean delete = false;
		
		List<PerformanceMonitorInfoResponse> resMonList = null;
		
		//マネージャからリソース監視の一覧を取得
		try {
			resMonList = MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getPerformanceList(null);
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass e) {
			log.warn("checkDelete(): " + HinemosMessage.replace(e.getMessage()), e);
			return false;
		}catch (MonitorNotFound e) {
			//リソース監視が存在しない場合もここを通るので、何もしない
			log.debug("checkDelete(): No Resource Monitoring found");
		}
		
		//リソース監視設定が存在しない場合は、削除
		if(resMonList == null || resMonList.isEmpty()){
			log.debug("checkDelete(): Since there is no resource monitoring, do delete");
			return true;
		}
		
		StringBuilder monIds= new StringBuilder() ;
		int count=0;
		
		//リソース監視一覧を取得
		for (PerformanceMonitorInfoResponse resMon : resMonList){
			if (count > 0) {
				monIds.append(", ");
			}

			//初回と以降10監視項目IDごとに改行を挿入
			if(count%10 == 0){
				monIds.append("\n");
			}
			monIds.append(resMon.getMonitorId());
			
			count++;
		}
		
		//ダイアログ表示
		MessageDialogWithScroll messageDialogWithScroll = new MessageDialogWithScroll(Messages.getString("CollectMaster.UsedMaster",new String[] { monIds.toString() }));
		delete = messageDialogWithScroll.openQuestion();
		
		//消さない場合はログ出力
		if(!delete){
			log.info(Messages.getString("CollectMaster.NotDeleteUsedMaster"));
		}
		
		return delete;
	}

	/**
	 * 収集項目定義情報を全て削除します。<BR>
	 * 
	 * @since 1.0
	 * @return 終了コード
	 */
	@ClearMethod
	public int clearCollectMaster(){
		
		log.debug("Start Clear CollectCalcMaster ");

		int ret = 0;
		
		//削除前にリソース監視設定の有無を確認
		if(checkDelete()){
			try {
				CollectRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteCollectMasterAll();
				log.info(Messages.getString("CollectMaster.ClearAllSettings") );
			} catch (Exception e) {
				log.error(Messages.getString("CollectMaster.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				log.debug("End Import CollectMaster : (Error)");
				return ret;
			}
		}else{
			ret = SettingConstants.ERROR_INPROCESS;
		}

		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("CollectMaster.ClearCompleted"));
		}else{
			log.error(Messages.getString("CollectMaster.EndWithErrorCode") );
		}
		
		log.debug("End Clear CollectMaster");
		return ret;
	}

	/**
	 * 収集項目定義情報をマネージャに投入します。
	 * 
	 * @return
	 */
	@ImportMethod
	public int importCollectMaster(String fileName){
		
		log.debug("Start Import CollectMaster :" + fileName);
		
		int ret=0;
		
		//XMからBeanに取り込みます。
		CollectMasters list = null;
		try {
			list = XmlMarshallUtil.unmarshall(CollectMasters.class,new InputStreamReader(
					new FileInputStream(fileName), "UTF-8"));
			
		} catch (MarshalException e1) {
			log.error(Messages.getString("CollectMaster.UnmarshalXmlFailed") + " " + e1.getMessage(),e1);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import CollectMaster : " + fileName +"(Error)");
			return ret;
		} catch (ValidationException e1) {
			log.error(Messages.getString("CollectMaster.UnmarshalXmlFailed") + " " + e1.getMessage(),e1);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import CollectMaster : " + fileName +"(Error)");
			return ret;
		} catch (UnsupportedEncodingException e1) {
			log.error(Messages.getString("CollectMaster.UnmarshalXmlFailed") + " " + e1.getMessage(),e1);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import CollectMaster : " + fileName +"(Error)");
			return ret;
		} catch (FileNotFoundException e1) {
			log.error(Messages.getString("CollectMaster.UnmarshalXmlFailed") + " " + e1.getMessage(),e1);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import CollectMaster : " + fileName +"(Error)");
			return ret;
		} catch (IOException e1) {
			log.error(Messages.getString("CollectMaster.UnmarshalXmlFailed") + " " + e1.getMessage(),e1);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import CollectMaster : " + fileName +"(Error)");
			return ret;
		}
		
		/*
		 * スキーマのバージョンチェック
		 */
		if(!checkSchemaVersion(list.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		
		// 収集項目マスタ編集でマネージャに渡すクラスと渡す順番は以下のとおり
		// CollectorCalcMethodMstData
		// CollectorCategoryMstData
		// CollectorItemCodeMstData
		// CollectorItemCalcMethodMstData
		// CollectorPollingMstData
		// CollectorCategoryCollectMstData
		
		/*
		 * インポート用計算定義情報の作成
		 */
		ArrayList<CollectorCalcMethodMstDataRequest> calcMethodList = new ArrayList<CollectorCalcMethodMstDataRequest>();
		CollectorCalcMethodMstDataRequest calcMethodData = null;
		
		CollectorCalcMethodFrame calcFrame = list.getCollectorCalcMethodFrame();
		for (CollectorCalcMethods methods : calcFrame.getCollectorCalcMethods()) {
			
			calcMethodData = CollectMasterConv.xml2dto(methods);
			if(calcMethodData.getCalcMethod() != null){
				calcMethodList.add(calcMethodData);
			}
			
		}
		
		/*
		 * インポート用収集項目コードの作成
		 */
		ArrayList<CollectorCategoryMstDataRequest> categoryList = new ArrayList<CollectorCategoryMstDataRequest>();
		ArrayList<CollectorItemCodeMstDataRequest> itemCodeList = new ArrayList<CollectorItemCodeMstDataRequest>();
		CollectorCategoryMstDataRequest categoryData = null;
		CollectorItemCodeMstDataRequest itemCodeData = null;
		CollectorItemCodeMstDataRequest childItemCodeData = null;
		
		CollectorItemCodeFrame itemFrame = list.getCollectorItemCodeFrame();
		for (CollectorItems items : itemFrame.getCollectorItems()) {
			
			categoryData = CollectMasterConv.xml2dto(items);
			if(categoryData.getCategoryCode() != null) {
				categoryList.add(categoryData);
			
				for (CollectorItemCodes itemCode : items.getCollectorItemCodes()) {
					
					itemCodeData = CollectMasterConv.xml2dto(itemCode);
					if(itemCodeData.getItemCode() != null){
						itemCodeList.add(itemCodeData);
						
						// 子持ちのItemCodeが存在する場合
						if(itemCode.getChildItemsCount() != 0){
							for(ChildItems childItemCode : itemCode.getChildItems()){
								
								childItemCodeData = CollectMasterConv.xml2dto(childItemCode);
								if(childItemCode.getItemCode() != null){
									itemCodeList.add(childItemCodeData);
								}
							}
						}
					}
				}
			}
		}
		
		/*
		 * インポート用プラットフォーム毎の収集対象定義の作成
		 */
		ArrayList<CollectorItemCalcMethodMstDataRequest> itemCalcMethodList = new ArrayList<CollectorItemCalcMethodMstDataRequest>();
		ArrayList<CollectorPollingMstDataRequest> pollingList = new ArrayList<CollectorPollingMstDataRequest>();
		CollectorItemCalcMethodMstDataRequest itemCalcMethodData = null;
		CollectorPollingMstDataRequest pollingData = null;
		
		CollectorItemCalcFrame itemCalcFrame = list.getCollectorItemCalcFrame();
		
		for (CollectorItemCalcMethods itemCalcMethods : itemCalcFrame.getCollectorItemCalcMethods()) {
			
			itemCalcMethodData = CollectMasterConv.xml2dto(itemCalcMethods);
			
			if(itemCalcMethodData.getCollectMethod() != null &&
					itemCalcMethodData.getPlatformId() != null &&
					itemCalcMethodData.getSubPlatformId() != null &&
					itemCalcMethodData.getItemCode() != null) {
				
				itemCalcMethodList.add(itemCalcMethodData);
			
				for(PollingCollector pollings : itemCalcMethods.getPollingCollector()){
					
					pollingData = CollectMasterConv.xml2dto(pollings);
					
					if(pollingData.getCollectMethod() != null &&
							pollingData.getPlatformId() != null &&
							pollingData.getSubPlatformId() != null &&
							pollingData.getItemCode() != null &&
							pollingData.getVariableId() != null) {
						
						pollingList.add(pollingData);
						
					}
				}
			}
		}
		
		/*
		 * インポート用カテゴリ毎の収集方法の作成
		 */
		ArrayList<CollectorCategoryCollectMstDataRequest> categoryCollectList = new ArrayList<CollectorCategoryCollectMstDataRequest>();
		CollectorCategoryCollectMstDataRequest categoryCollectData = null;
		
		CollectorCategoryCollectFrame categoryCollectFrame = list.getCollectorCategoryCollectFrame();
		for (CollectorCategoryCollects categoryCollects : categoryCollectFrame.getCollectorCategoryCollects()) {
			
			categoryCollectData = CollectMasterConv.xml2dto(categoryCollects);
			
			if(	categoryCollectData.getPlatformId() != null &&
					categoryCollectData.getSubPlatformId() != null &&
					categoryCollectData.getCategoryCode() != null) {
				categoryCollectList.add(categoryCollectData);
			}
			
		}

		AddCollectMasterRequest  collectMasterInfo = new AddCollectMasterRequest ();

		// 計算定義情報の設定
		if (calcMethodList != null && calcMethodList.size() > 0) {
			collectMasterInfo.getCollectorCalcMethodMstDataList().addAll(calcMethodList);
		}

		// カテゴリ定義情報の設定
		if (categoryList != null && categoryList.size() > 0) {
			collectMasterInfo.getCollectorCategoryMstDataList().addAll(categoryList);
		}

		// 収集項目コードの設定
		if (itemCodeList != null && itemCodeList.size() > 0) {
			collectMasterInfo.getCollectorItemCodeMstDataList().addAll(itemCodeList);
		}

		// 収集項目毎の計算方法定義の設定
		if (itemCalcMethodList != null && itemCalcMethodList.size() > 0) {
			collectMasterInfo.getCollectorItemCalcMethodMstDataList().addAll(itemCalcMethodList);
		}

		// ポーリング情報定義の設定
		if (pollingList != null && pollingList.size() > 0) {
			collectMasterInfo.getCollectorPollingMstDataList().addAll(pollingList);
		}

		// カテゴリ毎の収集方法の設定
		if (categoryCollectList != null && categoryCollectList.size() > 0) {
			collectMasterInfo.getCollectorCategoryCollectMstDataList().addAll(categoryCollectList);
		}

		// 収集項目定義のインポート
		log.debug("Start Import CollectMasterInfo :" + fileName);
		try{
			CollectRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).addCollectMaster(collectMasterInfo);
			log.info(Messages.getString("CollectMaster.ImportSucceeded") + " : " + fileName);

		} catch (HinemosUnknown e) {
			log.error(Messages.getString("CollectMaster.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (InvalidRole e) {
			log.error(Messages.getString("CollectMaster.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (InvalidUserPass e) {
			log.error(Messages.getString("CollectMaster.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (Exception e) {
			log.warn(Messages.getString("CollectMaster.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
		}
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("CollectMaster.ImportCompleted"));
		}else{
			log.error(Messages.getString("CollectMaster.EndWithErrorCode") );
		}

		log.debug("End Import CollectCalcMaster");
		return ret;
		
	}

	/**
	 * スキーマのバージョンチェックを行い、メッセージを出力する。<BR>
	 * ※メッセージ詳細に出力するためは本クラスのloggerにエラー内容を出力する必要がある
	 * 
	 * @param XMLファイルのスキーマ
	 * @return チェック結果
	 */
	private boolean checkSchemaVersion(com.clustercontrol.utility.settings.master.xml.SchemaInfo schmaversion) {
		/*スキーマのバージョンチェック*/
		int res = CollectMasterConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.master.xml.SchemaInfo sci = CollectMasterConv.getSchemaVersion();
		
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	/**
	 * 収集項目定義情報をマネージャから読み出します。
	 * @return
	 */
	@ExportMethod
	public int exportCollectMaster(String fileName){
		
		log.debug("Start Export CollectMaster");
		
		int ret = 0;
		
		CollectMasterInfoResponse               collectMasterInfo = null;
		List<CollectorCalcMethodMstDataResponse>         calcList = null;
		List<CollectorCategoryMstDataResponse>       categoryList = null;
		List<CollectorItemCodeMstDataResponse>       itemCodeList = null;
		List<CollectorItemCalcMethodMstDataResponse>  itemCalcList = null;
		List<CollectorPollingMstDataResponse>          pollingList = null;
		List<CollectorCategoryCollectMstDataResponse> categoryCollectList = null;

		try {
			// 収集項目定義のエクスポート
			collectMasterInfo = CollectRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getCollectMasterInfo();

			// 各項目定義の取得
			calcList = collectMasterInfo.getCollectorCalcMethodMstDataList();
			categoryList = collectMasterInfo.getCollectorCategoryMstDataList();
			itemCodeList = collectMasterInfo.getCollectorItemCodeMstDataList();
			itemCalcList = collectMasterInfo.getCollectorItemCalcMethodMstDataList();
			pollingList = collectMasterInfo.getCollectorPollingMstDataList();
			categoryCollectList = collectMasterInfo.getCollectorCategoryCollectMstDataList();
		} catch (Exception e) {
			log.error(Messages.getString("CollectMaster.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Export PerfCollect (Error)");
			return ret;
		}
		
		// ファイル書き出し用変数
		CollectMasters exportData = new CollectMasters();
		CollectorCalcMethodFrame calcFrame = new CollectorCalcMethodFrame();
		CollectorItemCodeFrame itemCodeFrame = new CollectorItemCodeFrame();
		CollectorItemCalcFrame itemCalcFrame = new CollectorItemCalcFrame();
		CollectorCategoryCollectFrame categoryCollectFrame = new CollectorCategoryCollectFrame();
		
		/*
		 * エクスポート用計算定義情報の作成
		 */
		log.debug("Export CollectorCalcMethods start");
		CollectorCalcMethods methods = null;
		
		Iterator<CollectorCalcMethodMstDataResponse> itrCalc = calcList.iterator();
		while(itrCalc.hasNext()) {
			
			methods = CollectMasterConv.dto2Xml(itrCalc.next());
			calcFrame.addCollectorCalcMethods(methods);
		}
		
		/*
		 * エクスポート用収集項目コードの作成
		 */
		log.debug("Export CollectorItem start");
		CollectorItems items = null;
		CollectorItemCodes parentItemCodeData = null;
		ChildItems childItemCodeData = null;
		
		Iterator<CollectorCategoryMstDataResponse> itrCategory = categoryList.iterator();
		while(itrCategory.hasNext()) {
			
			// カテゴリコードの取得
			items = CollectMasterConv.dto2Xml(itrCategory.next());
			
			Iterator<CollectorItemCodeMstDataResponse> itrItemCode = itemCodeList.iterator();
			while(itrItemCode.hasNext()) {
				
				// 親アイテムコードの取得
				CollectorItemCodeMstDataResponse parentItemCode = itrItemCode.next();
				
				// CollectCategoryMstData と CollectItemCodeData のcategoryCodeが同じ場合
				// かつ parentItemCodeが入っていないもの
				if( items.getCategoryCode().equals(parentItemCode.getCategoryCode())
						&& (parentItemCode.getParentItemCode() == null || parentItemCode.getParentItemCode().equals("")) ) {
					
					parentItemCodeData = CollectMasterConv.dto2Xml(parentItemCode);
					
					// 子アイテムコードの取得
					Iterator<CollectorItemCodeMstDataResponse> itrChildItemCode = itemCodeList.iterator();
					while(itrChildItemCode.hasNext()){
						
						CollectorItemCodeMstDataResponse childItemCode = itrChildItemCode.next();
						
						// 親アイテムコードのitemCodeと子アイテムコードのparentItemCodeが等しい場合
						if(parentItemCode.getItemCode().equals(childItemCode.getParentItemCode())){
							
							CollectorItemCodes tmpData = CollectMasterConv.dto2Xml(childItemCode);
							
							childItemCodeData = new ChildItems();
							childItemCodeData.setItemCode(tmpData.getItemCode());
							childItemCodeData.setCategoryCode(tmpData.getCategoryCode());
							childItemCodeData.setParentItemCode(tmpData.getParentItemCode());
							childItemCodeData.setItemName(tmpData.getItemName());
							childItemCodeData.setMeasure(tmpData.getMeasure());
							childItemCodeData.setDeviceSupport(tmpData.getDeviceSupport());
							childItemCodeData.setDeviceType(tmpData.getDeviceType());
							childItemCodeData.setGraphRange(tmpData.getGraphRange());
							
							// 親アイテムコード情報に子アイテムコード情報を追加
							parentItemCodeData.addChildItems(childItemCodeData);
						}
					}
					// カテゴリ情報に親アイテムコード情報を追加
					items.addCollectorItemCodes(parentItemCodeData);
				}
			}
			// 収集項目コード情報にカテゴリ情報を追加
			itemCodeFrame.addCollectorItems(items);
		}
		
		/*
		 * エクスポート用プラットフォーム毎の収集対象定義の作成
		 */
		log.debug("Export CollectorItemCalcMethods start");
		CollectorItemCalcMethods itemCalcMethods = null;
		PollingCollector pollingCollector = null;
		
		Iterator<CollectorItemCalcMethodMstDataResponse> itrItemCalc = itemCalcList.iterator();
		while(itrItemCalc.hasNext()) {
			
			// プラットフォーム別収集対象情報の取得
			itemCalcMethods = CollectMasterConv.dto2Xml(itrItemCalc.next());
			
			Iterator<CollectorPollingMstDataResponse> itrPolling = pollingList.iterator();
			while(itrPolling.hasNext()) {
				
				CollectorPollingMstDataResponse pollingData = itrPolling.next();
				
				if(itemCalcMethods.getCollectMethod().equals(pollingData.getCollectMethod())
						&& itemCalcMethods.getPlatformId().equals(pollingData.getPlatformId())
						&& itemCalcMethods.getSubPlatformId().equals(pollingData.getSubPlatformId())
						&& itemCalcMethods.getItemCode().equals(pollingData.getItemCode())) {
				
					pollingCollector = CollectMasterConv.dto2Xml(pollingData);
					
					// プラットフォーム別収集対象情報に収集方法を追加
					itemCalcMethods.addPollingCollector(pollingCollector);
				}
			}
			itemCalcFrame.addCollectorItemCalcMethods(itemCalcMethods);
		}
		
		/*
		 * エクスポート用カテゴリ毎の収集方法の作成
		 */
		log.debug("Export CollectorCategoryCollects start");
		CollectorCategoryCollects categoryCollects = null;
		
		Iterator<CollectorCategoryCollectMstDataResponse> itrCategoryCollects = categoryCollectList.iterator();
		while(itrCategoryCollects.hasNext()) {
			
			categoryCollects = CollectMasterConv.dto2Xml(itrCategoryCollects.next());
			categoryCollectFrame.addCollectorCategoryCollects(categoryCollects);
		}
		
		// exportDataにエクスポートした内容を入れる
		exportData.setCollectorCalcMethodFrame(calcFrame);
		exportData.setCollectorItemCodeFrame(itemCodeFrame);
		exportData.setCollectorItemCalcFrame(itemCalcFrame);
		exportData.setCollectorCategoryCollectFrame(categoryCollectFrame);
		
		// XMLファイルに出力
		try {
			exportData.setCommon(CollectConv.versionCollectDto2Xml(Config.getVersion()));

			// スキーマ情報のセット
			exportData.setSchemaInfo(CollectMasterConv.getSchemaVersion());
			try(FileOutputStream fos = new FileOutputStream(fileName);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
					exportData.marshal(osw);
			}
		} catch (Exception e) {
			log.warn(Messages.getString("CollectMaster.ExportFailed") + " : " + e.getMessage());
			log.debug(e,e);
			ret = SettingConstants.ERROR_INPROCESS;
		}
			
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("CollectMaster.ExportCompleted"));
		}else{
			log.error(Messages.getString("CollectMaster.EndWithErrorCode") );
		}
			
		log.debug("End Export CollectMaster");
		return ret;
	}

	public Logger getLogger() {
		return log;
	}

}
