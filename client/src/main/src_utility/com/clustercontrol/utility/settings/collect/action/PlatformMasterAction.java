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
import java.util.Iterator;
import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.log4j.Logger;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.ExportMethod;
import com.clustercontrol.utility.settings.ImportMethod;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.settings.collect.conv.CollectConv;
import com.clustercontrol.utility.settings.collect.conv.PlatformMasterConv;
import com.clustercontrol.utility.settings.collect.util.PerformanceCollectMasterEndpointWrapper;
import com.clustercontrol.utility.settings.master.xml.CollectorMstPlatforms;
import com.clustercontrol.utility.settings.master.xml.CollectorPlatforms;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.ws.collectmaster.CollectorPlatformMstData;
import com.clustercontrol.ws.collectmaster.HinemosUnknown_Exception;
import com.clustercontrol.ws.collectmaster.InvalidRole_Exception;
import com.clustercontrol.ws.collectmaster.InvalidUserPass_Exception;

/**
 * プラットフォームのマスター情報を取得、設定します。<br>
 * XMLファイルに定義されたプラットフォーム情報を反映させるクラス<br>
 * ただし、すでに登録されているプラットフォーム情報と重複する場合はスキップされる。
 * 
 * @param action 動作
 * @param XMLファイルパス（ユーザ情報定義の入力元）
 * 
 * @version 6.0.0
 * @since 1.2.0
 * 
 */
public class PlatformMasterAction {

	/* ロガー */
	protected static Logger log = Logger.getLogger(PlatformMasterAction.class);

	public PlatformMasterAction() throws ConvertorException {
		super();
	}
	
	/**
	 * プラットフォーム情報をマネージャに投入します。
	 * 
	 * @return
	 */
	@ImportMethod
	public int importPlatformMaster(String fileName){
		
		log.debug("Start Import PlatformMaster :" + fileName);
		
		int ret=0;
		
		//XMLからBeanに取り込みます。
		CollectorMstPlatforms list = null;
		try {
			list = (CollectorMstPlatforms)CollectorMstPlatforms.unmarshal(new InputStreamReader(
					new FileInputStream(fileName), "UTF-8"));
			
		} catch (MarshalException e1) {
			log.error(Messages.getString("CollectMaster.UnmarshalXmlFailed") + " " + e1.getMessage());
			log.debug(e1,e1);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import PlatformMaster : " + fileName +"(Error)");
			return ret;
		} catch (ValidationException e1) {
			log.error(Messages.getString("CollectMaster.UnmarshalXmlFailed") + " " + e1.getMessage());
			log.debug(e1,e1);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import PlatformMaster : " + fileName +"(Error)");
			return ret;
		} catch (UnsupportedEncodingException e1) {
			log.error(Messages.getString("CollectMaster.UnmarshalXmlFailed") + " " + e1.getMessage());
			log.debug(e1,e1);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import PlatformMaster : " + fileName +"(Error)");
			return ret;
		} catch (FileNotFoundException e1) {
			log.error(Messages.getString("CollectMaster.UnmarshalXmlFailed") + " " + e1.getMessage());
			log.debug(e1,e1);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import PlatformMaster : " + fileName +"(Error)");
			return ret;
		}
		
		/*
		 * スキーマのバージョンチェック
		 */
		if(!checkSchemaVersion(list.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		for (CollectorPlatforms platforms : list.getCollectorPlatforms() ) {
			CollectorPlatformMstData data = PlatformMasterConv.xml2dto(platforms);
			
			try {
				PerformanceCollectMasterEndpointWrapper.addCollectPlatformMaster(data);
				log.info(Messages.getString("CollectMaster.ImportSucceeded") + " : " + data.getPlatformId());
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
		int res = PlatformMasterConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.master.xml.SchemaInfo sci = PlatformMasterConv.getSchemaVersion();
		
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}
	
	/**
	 * プラットフォーム情報をマネージャから読み出します。
	 * @return
	 */
	@ExportMethod
	public int exportPlatformMaster(String fileName){
		
		log.debug("Start Export PlatformMaster");
		
		int ret = 0;
		
		List<CollectorPlatformMstData> list = null;

		try {
			list = PerformanceCollectMasterEndpointWrapper.getCollectPlatformMaster();
			log.info(Messages.getString("CollectMaster.ExportSucceeded") + " : " + fileName);
		} catch (Exception e) {
			log.error(Messages.getString("CollectMaster.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Export PlatformMaster (Error)");
			return ret;
		}
		
		CollectorMstPlatforms exportData = new CollectorMstPlatforms();
		CollectorPlatforms platforms = null;
		
		Iterator<CollectorPlatformMstData> itr = list.iterator();
		while(itr.hasNext()) {
			platforms = PlatformMasterConv.dto2Xml(itr.next());
			
			exportData.addCollectorPlatforms(platforms);
		}
		
		if (exportData == null || exportData.getCollectorPlatformsCount() == 0){
			ret = SettingConstants.ERROR_INPROCESS;
		}
		else {
			// XMLファイルに出力
			try{
				exportData.setCommon(CollectConv.versionCollectDto2Xml(Config.getVersion()));
				
				// スキーマ情報のセット
				exportData.setSchemaInfo(PlatformMasterConv.getSchemaVersion());
				try(FileOutputStream fos = new FileOutputStream(fileName);
					OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
					exportData.marshal(osw);
				}
			} catch (Exception e) {
				log.warn(Messages.getString("CollectMaster.ExportFailed") + " : " + e.getMessage());
				log.debug(e,e);
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("CollectMaster.ExportCompleted"));
		}else{
			log.error(Messages.getString("CollectMaster.EndWithErrorCode") );
		}
			
		log.debug("End Export PlatformMaster");
		return ret;
	}

	public Logger getLogger() {
		return log;
	}
}
