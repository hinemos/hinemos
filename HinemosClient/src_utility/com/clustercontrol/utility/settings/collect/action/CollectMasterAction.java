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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.log4j.Logger;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.ClearMethod;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.ExportMethod;
import com.clustercontrol.utility.settings.ImportMethod;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.settings.collect.conv.CollectConv;
import com.clustercontrol.utility.settings.collect.conv.CollectMasterConv;
import com.clustercontrol.utility.settings.collect.util.PerformanceCollectMasterEndpointWrapper;
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
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.ws.collectmaster.CollectMasterInfo;
import com.clustercontrol.ws.collectmaster.CollectorCalcMethodMstData;
import com.clustercontrol.ws.collectmaster.CollectorCategoryCollectMstData;
import com.clustercontrol.ws.collectmaster.CollectorCategoryMstData;
import com.clustercontrol.ws.collectmaster.CollectorItemCalcMethodMstData;
import com.clustercontrol.ws.collectmaster.CollectorItemCodeMstData;
import com.clustercontrol.ws.collectmaster.CollectorPollingMstData;
import com.clustercontrol.ws.collectmaster.HinemosUnknown_Exception;
import com.clustercontrol.ws.collectmaster.InvalidRole_Exception;
import com.clustercontrol.ws.collectmaster.InvalidUserPass_Exception;

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
	 * 収集項目定義情報を全て削除します。<BR>
	 * 
	 * @since 1.0
	 * @return 終了コード
	 */
	@ClearMethod
	public int clearCollectMaster(){
		
		log.debug("Start Clear CollectCalcMaster ");

		int ret = 0;
		
		try {
			PerformanceCollectMasterEndpointWrapper.deleteCollectMasterAll();
		} catch (Exception e) {
			log.error(Messages.getString("CollectMaster.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import CollectMaster : (Error)");
			return ret;
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
			list = (CollectMasters)CollectMasters.unmarshal(new InputStreamReader(
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
		ArrayList<CollectorCalcMethodMstData> calcMethodList = new ArrayList<CollectorCalcMethodMstData>();
		CollectorCalcMethodMstData calcMethodData = null;
		
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
		ArrayList<CollectorCategoryMstData> categoryList = new ArrayList<CollectorCategoryMstData>();
		ArrayList<CollectorItemCodeMstData> itemCodeList = new ArrayList<CollectorItemCodeMstData>();
		CollectorCategoryMstData categoryData = null;
		CollectorItemCodeMstData itemCodeData = null;
		CollectorItemCodeMstData childItemCodeData = null;
		
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
		ArrayList<CollectorItemCalcMethodMstData> itemCalcMethodList = new ArrayList<CollectorItemCalcMethodMstData>();
		ArrayList<CollectorPollingMstData> pollingList = new ArrayList<CollectorPollingMstData>();
		CollectorItemCalcMethodMstData itemCalcMethodData = null;
		CollectorPollingMstData pollingData = null;
		
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
		ArrayList<CollectorCategoryCollectMstData> categoryCollectList = new ArrayList<CollectorCategoryCollectMstData>();
		CollectorCategoryCollectMstData categoryCollectData = null;
		
		CollectorCategoryCollectFrame categoryCollectFrame = list.getCollectorCategoryCollectFrame();
		for (CollectorCategoryCollects categoryCollects : categoryCollectFrame.getCollectorCategoryCollects()) {
			
			categoryCollectData = CollectMasterConv.xml2dto(categoryCollects);
			
			if(	categoryCollectData.getPlatformId() != null &&
					categoryCollectData.getSubPlatformId() != null &&
					categoryCollectData.getCategoryCode() != null) {
				categoryCollectList.add(categoryCollectData);
			}
			
		}

		CollectMasterInfo collectMasterInfo = new CollectMasterInfo();

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
			PerformanceCollectMasterEndpointWrapper.addCollectMaster(collectMasterInfo);
			log.info(Messages.getString("CollectMaster.ImportSucceeded") + " : " + fileName);

		} catch (HinemosUnknown_Exception e) {
			log.error(Messages.getString("CollectMaster.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (InvalidRole_Exception e) {
			log.error(Messages.getString("CollectMaster.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (InvalidUserPass_Exception e) {
			log.error(Messages.getString("CollectMaster.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (WebServiceException e) {
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
		
		CollectMasterInfo collectMasterInfo = null;
		List<CollectorCalcMethodMstData> calcList = null;
		List<CollectorCategoryMstData> categoryList = null;
		List<CollectorItemCodeMstData> itemCodeList = null;
		List<CollectorItemCalcMethodMstData> itemCalcList = null;
		List<CollectorPollingMstData> pollingList = null;
		List<CollectorCategoryCollectMstData> categoryCollectList = null;
		
		try {
			// 収集項目定義のエクスポート
			collectMasterInfo = PerformanceCollectMasterEndpointWrapper.getCollectMasterInfo();

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
		
		Iterator<CollectorCalcMethodMstData> itrCalc = calcList.iterator();
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
		
		Iterator<CollectorCategoryMstData> itrCategory = categoryList.iterator();
		while(itrCategory.hasNext()) {
			
			// カテゴリコードの取得
			items = CollectMasterConv.dto2Xml(itrCategory.next());
			
			Iterator<CollectorItemCodeMstData> itrItemCode = itemCodeList.iterator();
			while(itrItemCode.hasNext()) {
				
				// 親アイテムコードの取得
				CollectorItemCodeMstData parentItemCode = itrItemCode.next();
				
				// CollectCategoryMstData と CollectItemCodeData のcategoryCodeが同じ場合
				// かつ parentItemCodeが入っていないもの
				if( items.getCategoryCode().equals(parentItemCode.getCategoryCode())
						&& (parentItemCode.getParentItemCode() == null || parentItemCode.getParentItemCode().equals("")) ) {
					
					parentItemCodeData = CollectMasterConv.dto2Xml(parentItemCode);
					
					// 子アイテムコードの取得
					Iterator<CollectorItemCodeMstData> itrChildItemCode = itemCodeList.iterator();
					while(itrChildItemCode.hasNext()){
						
						CollectorItemCodeMstData childItemCode = itrChildItemCode.next();
						
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
		
		Iterator<CollectorItemCalcMethodMstData> itrItemCalc = itemCalcList.iterator();
		while(itrItemCalc.hasNext()) {
			
			// プラットフォーム別収集対象情報の取得
			itemCalcMethods = CollectMasterConv.dto2Xml(itrItemCalc.next());
			
			Iterator<CollectorPollingMstData> itrPolling = pollingList.iterator();
			while(itrPolling.hasNext()) {
				
				CollectorPollingMstData pollingData = itrPolling.next();
				
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
		
		Iterator<CollectorCategoryCollectMstData> itrCategoryCollects = categoryCollectList.iterator();
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
