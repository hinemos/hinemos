/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.ws.WebServiceException;

//import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.difference.CSVUtil;
import com.clustercontrol.utility.difference.DiffUtil;
import com.clustercontrol.utility.difference.ResultA;
import com.clustercontrol.utility.settings.ClearMethod;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.DiffMethod;
import com.clustercontrol.utility.settings.ExportMethod;
import com.clustercontrol.utility.settings.ImportMethod;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.settings.platform.action.ObjectPrivilegeAction;
import com.clustercontrol.utility.settings.ui.dialog.DeleteProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityDialogInjector;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.UtilityDialogConstant;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.access.HinemosUnknown_Exception;
import com.clustercontrol.ws.access.InvalidRole_Exception;
import com.clustercontrol.ws.access.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorDuplicate_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;

/**
 * 監視クラスの抽象クラスとなります。
 *
 * @version 6.1.0
 * @since 2.0.0
 *
 *
 */
public abstract class AbstractMonitorAction<T> {

	public AbstractMonitorAction() throws ConvertorException {
		super();
	}

	@SuppressWarnings("unchecked")
	@ImportMethod
	public int importXml(String filePath) throws ConvertorException {
		getLogger().debug("Start Import "  + getDataClass().getSimpleName());

		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
	    	getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
	    	getLogger().debug("End Import "  + getDataClass().getSimpleName() + " (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
	    }

		int returnValue = SettingConstants.SUCCESS;

		// XMLファイルからの読み込み
		T object = null;
		try {
			// ジェネリックの限界。テンプレート パラメータに指定した型に所属する static な関数を直接よべない。
			// そのため、Class 型を返す関数を用意したり、 Unmarshaller を直接呼ぶ必要が発生する。
			object = (T)Unmarshaller.unmarshal(getDataClass(), new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
		} catch (Exception e) {
			returnValue = handleCastorException(e);
			getLogger().debug("End Import "  + getDataClass().getSimpleName() + " (Error)");
			return returnValue;
		}

		// スキーマのバージョンチェック
		if (!checkSchemaVersionScope(object)) {
			return SettingConstants.ERROR_SCHEMA_VERSION;
		}

		// castor の 情報を DTO に変換。
		List<MonitorInfo> monitorInfoList = new LinkedList<MonitorInfo>();
		try {
			monitorInfoList = createMonitorInfoList(object);
		} catch (Exception e) {
			getLogger().warn(Messages.getString("SettingTools.ExportFailed"), e);
			returnValue = SettingConstants.ERROR_INPROCESS;
		}

		// MonitorInfo をマネージャに登録。
		List<String> objectIdList = new ArrayList<String>();
		for (com.clustercontrol.ws.monitor.MonitorInfo monitorInfo : monitorInfoList) {
			try {
				if (monitorInfo.getMonitorType() == null){
					// クライアントチェックエラーの場合
					getLogger().warn(Messages.getString("SettingTools.ImportFailed") + " : " + monitorInfo.getMonitorId() + " " + monitorInfo.getDescription());
					returnValue = SettingConstants.ERROR_INPROCESS;
				}
				else{
					if (MonitorSettingEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).addMonitor(monitorInfo)) {
						objectIdList.add(monitorInfo.getMonitorId());
						getLogger().info(Messages.getString("SettingTools.ImportSucceeded") + " : " + monitorInfo.getMonitorId());
					} else {
						getLogger().warn(Messages.getString("SettingTools.ImportFailed") + " : " + monitorInfo.getMonitorId());
						returnValue = SettingConstants.ERROR_INPROCESS;
					}
				}
			} catch (MonitorDuplicate_Exception e) {
				//重複時、インポート処理方法を確認する
				if(!ImportProcessMode.isSameprocess()){
					String[] args = {monitorInfo.getMonitorId()};
					UtilityProcessDialog dialog = UtilityDialogInjector.createImportProcessDialog(
							null, Messages.getString("message.import.confirm2", args));
				    ImportProcessMode.setProcesstype(dialog.open());
				    ImportProcessMode.setSameprocess(dialog.getToggleState());
				}

			    if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.UPDATE){
			    	try {
			    		if (MonitorSettingEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).modifyMonitor(monitorInfo)) {
			    			objectIdList.add(monitorInfo.getMonitorId());
							getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Update") + " : " + monitorInfo.getMonitorId());
						} else {
							getLogger().warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
							returnValue = SettingConstants.ERROR_INPROCESS;
						}
					} catch (Exception e1) {
						getLogger().warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
						returnValue = SettingConstants.ERROR_INPROCESS;
					}
			    } else if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
			    	getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Skip") + " : " + monitorInfo.getMonitorId());
			    } else if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			    	getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			    	returnValue = SettingConstants.ERROR_INPROCESS;
			    	break;
			    }
			} catch (Exception e) {
				returnValue = SettingConstants.ERROR_INPROCESS;
				if (!handleDTOException(e, Messages.getString("SettingTools.ImportFailed"), monitorInfo)) {
					break;
				}
			}
		}
		
		//オブジェクト権限同時インポート
		importObjectPrivilege(com.clustercontrol.bean.HinemosModuleConstant.MONITOR, objectIdList);
		
		//差分削除
		checkDelete(monitorInfoList);
		
		// 処理の終了
		if (returnValue == 0) {
			getLogger().info(Messages.getString("SettingTools.ImportCompleted"));
		}else{
			getLogger().error(Messages.getString("SettingTools.EndWithErrorCode") );
		}
		getLogger().debug("End Import " + getDataClass().getSimpleName());

		return returnValue;
	}

	@SuppressWarnings("unchecked")
	@DiffMethod
	public int diffXml(String filePath1, String filePath2) throws ConvertorException {
		getLogger().debug("Search differrence: "  + getDataClass().getSimpleName());

		int returnValue = SettingConstants.SUCCESS;

		// XMLファイルからの読み込み
		T object1 = null;
		T object2 = null;
		try {
			// ジェネリックの限界。テンプレート パラメータに指定した型に所属する static な関数を直接よべない。
			// そのため、Class 型を返す関数を用意したり、 Unmarshaller を直接呼ぶ必要が発生する。
			object1 = (T)Unmarshaller.unmarshal(getDataClass(), new InputStreamReader(new FileInputStream(filePath1), "UTF-8"));
			object2 = (T)Unmarshaller.unmarshal(getDataClass(), new InputStreamReader(new FileInputStream(filePath2), "UTF-8"));
			sort(object1);
			sort(object2);
		} catch (Exception e) {
			returnValue = handleCastorException(e);
			getLogger().debug("End Import "  + getDataClass().getSimpleName() + " (Error)");
			return returnValue;
		}

		// スキーマのバージョンチェック
		if (!checkSchemaVersionScope(object1)) {
			return SettingConstants.ERROR_SCHEMA_VERSION;
		}
		if (!checkSchemaVersionScope(object2)) {
			return SettingConstants.ERROR_SCHEMA_VERSION;
		}

		FileOutputStream fos = null;
		try {
			ResultA resultA = new ResultA();
			boolean diff = DiffUtil.diffCheck2(object1, object2, getDataClass(), resultA);
			assert resultA.getResultBs().size() == 1;
			
			if (diff){
				returnValue += SettingConstants.SUCCESS_DIFF_1;
			}

			if (diff || DiffUtil.isAll()) {
				CSVUtil.CSVSerializer csvSerializer = CSVUtil.createCSVSerializer();
				fos = new FileOutputStream(filePath2 + ".csv");
				csvSerializer.write(fos, resultA.getResultBs().values().iterator().next());
			}
			else {
				File f = new File(filePath2 + ".csv");
				if (f.exists()) {
					if (!f.delete())
						getLogger().warn(String.format("Fail to delete file. %s", f.getAbsolutePath()));
				}
			}
		}
		catch (Exception e) {
			getLogger().error("unexpected: ", e);
			returnValue = SettingConstants.ERROR_INPROCESS;
		}
		catch (Error e) {
			getLogger().error("unexpected: ", e);
			returnValue = SettingConstants.ERROR_INPROCESS;
		}
		finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
		}

		// 処理の終了
		if ((returnValue >= SettingConstants.SUCCESS) && (returnValue<=SettingConstants.SUCCESS_MAX)){
			getLogger().info(Messages.getString("SettingTools.DiffCompleted"));
		}else{
			getLogger().error(Messages.getString("SettingTools.EndWithErrorCode") );
		}

		getLogger().debug("End differrence: " + getDataClass().getSimpleName());

		return returnValue;
	}

	/**
	 * スキーマのバージョンチェックを行う。
	 *
	 * @param Castor で生成されたクラスのインスタンス
	 * @return
	 */
	protected abstract boolean checkSchemaVersionScope(T object);

	/**
	 * Castor で生成されたクラスの Class 型を返す。
	 *
	 * @return
	 */
	protected abstract Class<T> getDataClass();

	/**
	 * Castor で生成されたクラスの インスタンスから、MonitorInfo のリストを作成する。
	 *
	 * @param Castor で生成されたクラスのインスタンス
	 * @return
	 * @throws MonitorNotFound_Exception
	 * @throws com.clustercontrol.ws.monitor.InvalidUserPass_Exception
	 * @throws com.clustercontrol.ws.monitor.InvalidRole_Exception
	 * @throws com.clustercontrol.ws.monitor.HinemosUnknown_Exception
	 */
	protected abstract List<MonitorInfo> createMonitorInfoList(T object) throws ConvertorException, com.clustercontrol.ws.monitor.HinemosUnknown_Exception, com.clustercontrol.ws.monitor.InvalidRole_Exception, com.clustercontrol.ws.monitor.InvalidUserPass_Exception, MonitorNotFound_Exception;

	@ExportMethod
	public int exportDTO(String filePath) throws ConvertorException {
		getLogger().debug("Start Export " + getDataClass().getSimpleName());

		// エージェント監視情報を取得。
		List<com.clustercontrol.ws.monitor.MonitorInfo> monitorInfoList_dto = null;
		try {
			monitorInfoList_dto = getFilterdMonitorList();
			Collections.sort(
				monitorInfoList_dto,
				new Comparator<com.clustercontrol.ws.monitor.MonitorInfo>() {
					/**
					 * 監視項目IDを比較する。
					 */
					@Override
					public int compare(MonitorInfo monitorInfo1, MonitorInfo monitorInfo2) {
						return monitorInfo1.getMonitorId().compareTo(monitorInfo2.getMonitorId());
					}
				});
		}
		catch (Exception e) {
			getLogger().error(Messages.getString("SettingTools.FailToGetList") + " " + e);
			getLogger().debug(e.getMessage(), e);
			getLogger().debug("End Export " + getDataClass().getSimpleName() + " (Error)");
			return SettingConstants.ERROR_INPROCESS;
		}


		// XMLファイルに出力
		int ret = SettingConstants.SUCCESS;
		try {
			// Caster のデータ構造に変換。
			Object monitorInfoList_cas = createCastorData(monitorInfoList_dto);
			try(FileOutputStream fos = new FileOutputStream(filePath);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
					Marshaller.marshal(monitorInfoList_cas, osw);

			}

			for (com.clustercontrol.ws.monitor.MonitorInfo monitorInfo: monitorInfoList_dto){
				getLogger().info(Messages.getString("SettingTools.ExportSucceeded") + " : " + monitorInfo.getMonitorId());
			}

		}
		catch (Exception e) {
			getLogger().error(Messages.getString("SettingTools.MarshalXmlFailed") + " " + e);
			getLogger().debug(e.getMessage(), e);
			ret = SettingConstants.ERROR_INPROCESS;
		}catch(Error e){
			// Error時もメッセージ出力
			getLogger().error(Messages.getString("SettingTools.MarshalXmlFailed") + " " + e);
			getLogger().debug(e.getMessage(), e);
			ret = SettingConstants.ERROR_INPROCESS;
		}

		// 処理の終了
		if (ret == 0) {
			getLogger().info(Messages.getString("SettingTools.ExportCompleted"));
		}else{
			getLogger().error(Messages.getString("SettingTools.EndWithErrorCode") );
		}

		getLogger().debug("End Export " + getDataClass().getSimpleName());
		return ret;
	}

	/**
	 * 特定の種別でフィルターされた MonitorInfo のリストを返す。
	 *
	 * @return
	 * @throws MonitorNotFound_Exception
	 * @throws com.clustercontrol.ws.monitor.InvalidUserPass_Exception
	 * @throws com.clustercontrol.ws.monitor.InvalidRole_Exception
	 * @throws com.clustercontrol.ws.monitor.HinemosUnknown_Exception
	 */
	protected abstract List<com.clustercontrol.ws.monitor.MonitorInfo> getFilterdMonitorList() throws com.clustercontrol.ws.monitor.HinemosUnknown_Exception, com.clustercontrol.ws.monitor.InvalidRole_Exception, com.clustercontrol.ws.monitor.InvalidUserPass_Exception, MonitorNotFound_Exception;

	/**
	 * 指定した MonitorInfo から、Castor のデータを作成する。
	 *
	 * @param
	 * @return
	 * @throws MonitorNotFound_Exception
	 * @throws com.clustercontrol.ws.monitor.InvalidUserPass_Exception
	 * @throws com.clustercontrol.ws.monitor.InvalidRole_Exception
	 * @throws com.clustercontrol.ws.monitor.HinemosUnknown_Exception
	 */
	protected abstract T createCastorData(List<com.clustercontrol.ws.monitor.MonitorInfo> monitorInfoList) throws com.clustercontrol.ws.monitor.HinemosUnknown_Exception, com.clustercontrol.ws.monitor.InvalidRole_Exception, com.clustercontrol.ws.monitor.InvalidUserPass_Exception, MonitorNotFound_Exception;

	@ClearMethod
	public int clear() throws ConvertorException {
		getLogger().debug("Start Clear " + getDataClass().getSimpleName());

		// 対象種別の監視ID一覧の取得
		List<com.clustercontrol.ws.monitor.MonitorInfo> monitorList = null;
		try {
			monitorList = getFilterdMonitorList();
		} catch (Exception e) {
			getLogger().error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			getLogger().debug(HinemosMessage.replace(e.getMessage()), e);
			getLogger().debug("End Clear " + getDataClass().getSimpleName() + " (Error)");
			return SettingConstants.ERROR_INPROCESS;
		}
		// 監視設定の削除
		int returnValue = SettingConstants.SUCCESS;

		Map<String, List<String>> monitorMap = new HashMap<>();
		
		for (MonitorInfo monitorInfo : monitorList) {
			if(!monitorMap.containsKey(monitorInfo.getMonitorTypeId())){
				monitorMap.put(monitorInfo.getMonitorTypeId(), new ArrayList<String>());
			}
			monitorMap.get(monitorInfo.getMonitorTypeId()).add(monitorInfo.getMonitorId());
		}

		for(Entry<String, List<String>> ent : monitorMap.entrySet()){
			try {
				MonitorSettingEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteMonitor(ent.getValue());
				getLogger().info(Messages.getString("SettingTools.ClearSucceeded") + " : " + ent.getValue().toString());
			} catch (WebServiceException e) {
				getLogger().error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				returnValue = SettingConstants.ERROR_INPROCESS;
				break;
			} catch (Exception e) {
				getLogger().warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				returnValue = SettingConstants.ERROR_INPROCESS;
			}
		}

		// 処理の終了
		if (returnValue == 0) {
			getLogger().info(Messages.getString("SettingTools.ClearCompleted"));
		}else{
			getLogger().error(Messages.getString("SettingTools.EndWithErrorCode") );
		}

		getLogger().debug("End Clear " + getDataClass().getSimpleName());

		return returnValue;
	}

	/**
	 * マネージャにアクセスした際に発生する例外を処理する。
	 *
	 * @param
	 */
	protected boolean handleDTOException(Exception e, String message, MonitorInfo monitorInfo) {
		boolean isContinue = true;

		if (
			e instanceof InvalidUserPass_Exception ||
			e instanceof InvalidRole_Exception ||
			e instanceof HinemosUnknown_Exception
			) {
			//　パスワードが不正なので処理を中断。
			
			getLogger().error(message + " : " + HinemosMessage.replace(e.getMessage()));
			getLogger().debug(HinemosMessage.replace(e.getMessage()), e);
			isContinue = false;
		}
		else if (e instanceof MonitorDuplicate_Exception) {
			//　登録済みなので処理を継続
			getLogger().info(Messages.getString("SettingTools.Duplicated") + " : " + HinemosMessage.replace(e.getMessage()));
		}
		else if (e instanceof com.clustercontrol.ws.monitor.InvalidSetting_Exception) {
			//　マネージャーへ渡した情報が不正だっただけなので処理を継続
			getLogger().error(Messages.getString("SettingTools.InvalidSetting") + " : " + HinemosMessage.replace(e.getMessage()));
			getLogger().debug(HinemosMessage.replace(e.getMessage()), e);
		}
		else {
			// 未知のエラーなので処理を中断。
			getLogger().error(message + " : " + HinemosMessage.replace(e.getMessage()));
			getLogger().debug(HinemosMessage.replace(e.getMessage()), e);
		}

		return isContinue;
	}

	/**
	 * Castor のマーシャリング中に発生した例外の処理をする。
	 *
	 * @param
	 */
	protected int handleCastorException(Exception e) {

		getLogger().error(Messages.getString("SettingTools.UnmarshalXmlFailed") + " " + e);
		getLogger().debug(e.getMessage(), e);

		return SettingConstants.ERROR_INPROCESS;
	}

	/**
	 * XML 側の情報をソートする。
	 *
	 * @param
	 */
	protected abstract void sort(T object);

	private Logger logger = Logger.getLogger(this.getClass());
	public Logger getLogger(){return logger;}

	protected void checkDelete(List<MonitorInfo> xmlElements){

		List<MonitorInfo> subList = null;
		try {
			subList = getFilterdMonitorList();
		}
		catch (Exception e) {
			getLogger().error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			getLogger().debug(e.getMessage(), e);
		}

		if(subList == null || subList.size() <= 0){
			return;
		}

		for(MonitorInfo mgrInfo: new ArrayList<>(subList)){
			for(MonitorInfo xmlElement: new ArrayList<>(xmlElements)){
				if(mgrInfo.getMonitorId().equals(xmlElement.getMonitorId())){
					subList.remove(mgrInfo);
					xmlElements.remove(xmlElement);
					break;
				}
			}
		}

		if(subList.size() > 0){
			for(MonitorInfo info: subList){
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getMonitorId()};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}

			    if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
			    	try {
			    		List<String> args = new ArrayList<>();
			    		args.add(info.getMonitorId());
			    		MonitorSettingEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteMonitor(args);
			    		getLogger().info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getMonitorId());
					} catch (Exception e1) {
						getLogger().warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
			    } else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
			    	getLogger().info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getMonitorId());
			    } else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			    	getLogger().info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
			    	return;
			    }
			}
		}
	}
	
	/**
	 * オブジェクト権限同時インポート
	 * 
	 * @param objectType
	 * @param objectIdList
	 */
	protected void importObjectPrivilege(String objectType, List<String> objectIdList){
		if(ImportProcessMode.isSameObjectPrivilege()){
			ObjectPrivilegeAction.importAccessExtraction(
					ImportProcessMode.getXmlObjectPrivilege(),
					objectType,
					objectIdList,
					getLogger());
		}
	}
}
