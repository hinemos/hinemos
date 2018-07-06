/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.ws.WebServiceException;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;


import org.apache.log4j.Logger;

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
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.UtilityDialogConstant;

/**
 * インポート・エクスポート・削除するアクションの基底クラス<br>
 * 
 * @version 6.0.0
 * @since 5.0.a
 * 
 * 
 */
public abstract class BaseAction<D, E, T> {

	protected Logger log = Logger.getLogger(this.getClass());
	
	public BaseAction() throws ConvertorException {
		super();
	}
	
	/**
	 * 情報をマネージャから削除します。<BR>
	 * 
	 * @return 終了コード
	 */
	@ClearMethod
	public int clear() {

		log.debug("Start Clear " + getActionName());

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		// 定義一覧の取得
		List<D> infoList = null;
		try {
			infoList = getList();
			Collections.sort(infoList, new InfoComparator());
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Clear " + getActionName() + " (Error)");
			return ret;
		}

		// 定義の削除
		for (D info: infoList) {
			try {
				deleteInfo(info);
				log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + getKeyInfoD(info));
			} catch (WebServiceException e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				break;
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ClearCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Clear " + getActionName());
		return ret;
	}


	/**
	 * 情報をマネージャから取得し、XMLに出力します。<BR>
	 * 
	 * @param 出力するXMLファイル
	 * @return 終了コード
	 */
	@ExportMethod
	public int exportXml(String xmlFile) {

		log.debug("Start Export " + getActionName());

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		// 定義一覧の取得
		List<D> infoList = null;
		try {
			infoList = getList();
			Collections.sort(infoList, new InfoComparator());
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Export " + getActionName() + " (Error)");
			return ret;
		}

		// 定義の格納
		T xmlInfo = newInstance();
		for (D info : infoList) {
			try {
				addInfo(xmlInfo, info);
				log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + getKeyInfoD(info));
			} catch (Exception e) {
				log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		
		// XMLファイルに出力
		try {
			exportXml(xmlInfo, xmlFile);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.MarshalXmlFailed"),e);
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ExportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Export " + getActionName());
		return ret;
	}

	/**
	 * XMLの情報をマネージャに投入します。<BR>
	 * 
	 * @param 入力するXMLファイル
	 * @return 終了コード
	 * @throws Exception 
	 */
	@ImportMethod
	public int importXml(String xmlFile) throws Exception {

		log.debug("Start Import " + getActionName());
		
		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
	    	getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
	    	getLogger().debug("End Import " + getActionName() + " (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
	    }
		
		// 返り値変数(条件付き正常終了用）
		int ret = 0;
		T xmlInfo = null;
		// XMLファイルからの読み込み
		try {
			xmlInfo = getXmlInfo(xmlFile);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"),e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Inport " + getActionName() + " (Error)");
			return ret;
		}

		ret = checkSchemaVersion(xmlInfo);
		
		if(ret == SettingConstants.ERROR_SCHEMA_VERSION){
			return ret;
		}
		
		preCheckDuplicate();
		
		// 定義の登録
		List<E> objectIdList = new ArrayList<E>();
		for (E info : getElements(xmlInfo)) {
			try {
				int tmpRet = 0;
				tmpRet = registElement(info);
				if(tmpRet == -1){
					ret = SettingConstants.ERROR_INPROCESS;
					break;
				} else if(tmpRet == 9){
					ret = SettingConstants.ERROR_INPROCESS;
					continue;
				}
				objectIdList.add(info);
				log.info(Messages.getString("SettingTools.ImportSucceeded") + " : " + getKeyInfoE(info));
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				continue;
			}
		}
		
		//オブジェクト権限同時インポート
		try {
			importObjectPrivilege(objectIdList);
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		
		//差分削除
		try {
			checkDelete(xmlInfo);
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}

		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Inport " + getActionName());
		return ret;
	}

	
	/**
	 * 差分比較処理を行います。
	 * XMLファイル２つ（filePath1,filePath2）を比較する。
	 * 差分がある場合：差分をＣＳＶファイルで出力する。
	 * 差分がない場合：出力しない。
	 * 				   または、すでに存在している同一名のＣＳＶファイルを削除する。
	 *
	 * @param filePath1 XMLファイル名
	 * @param filePath2 XMLファイル名
	 * @return 終了コード
	 * @throws Exception 
	 */
	@DiffMethod
	public int diffXml(String filePath1, String filePath2) throws Exception {

		log.debug("Start Differrence " + getActionName());

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		T xmlInfo1 = null;
		T xmlInfo2 = null;
		
		// XMLファイルからの読み込み
		try {
			xmlInfo1 = getXmlInfo(filePath1);
			xmlInfo2 = getXmlInfo(filePath2);
			sort(xmlInfo1);
			sort(xmlInfo2);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence " + getActionName() + " (Error)");
			return ret;
		}
		
		ret = checkSchemaVersion(xmlInfo1);
		ret = checkSchemaVersion(xmlInfo2);
		
		if(ret == SettingConstants.ERROR_SCHEMA_VERSION){
			return ret;
		}
		

		FileOutputStream fos = null;
		
		try {
			ResultA resultA = new ResultA();
			//比較処理に渡す
			boolean diff = DiffUtil.diffCheck2(xmlInfo1, xmlInfo2, xmlInfo1.getClass(), resultA);
			assert resultA.getResultBs().size() == 1;
			
			if(diff){
				ret += SettingConstants.SUCCESS_DIFF_1;
			}
			//差分がある場合、ＣＳＶファイル作成
			if (diff || DiffUtil.isAll()) {
				CSVUtil.CSVSerializer csvSerializer = CSVUtil.createCSVSerializer();
				fos = new FileOutputStream(filePath2 + ".csv");
				csvSerializer.write(fos, resultA.getResultBs().values().iterator().next());
			}
			//差分がない場合、すでに作成済みのＣＳＶファイルがあれば、削除
			else {
				File f = new File(filePath2 + ".csv");
				if (f.exists()) {
					if (!f.delete())
						log.warn(String.format("Fail to delete file. %s", f.getAbsolutePath()));
				}
			}
		}
		catch (Exception e) {
			getLogger().error("unexpected: ", e);
			ret = SettingConstants.ERROR_INPROCESS;
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
		if ((ret >= SettingConstants.SUCCESS) && (ret<=SettingConstants.SUCCESS_MAX)){
			log.info(Messages.getString("SettingTools.DiffCompleted"));
		}else{
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
		}
		getLogger().debug("End Differrence " + getActionName());

		return ret;
	}
	

	private class InfoComparator implements Comparator<D> {
		@Override
		public int compare(
				D info1,
				D info2) {
			return BaseAction.this.compare(info1, info2);
		}
	}
		
	private void sort(T info) {
		E[] infoList = getArray(info);
		Arrays.sort(
			infoList,
			new Comparator<E>() {
				@Override
				public int compare(E o1, E o2) {
					return sortCompare(o1, o2);
				}
			});
		 setArray(info, infoList);
	}

	// 実行するアクションの名前です
	protected abstract String getActionName();
	// 対象となるレコードのリストをマネージャから取得します
	protected abstract List<D> getList() throws Exception;
	// レコードをマネージャから削除します
	protected abstract void deleteInfo(D info) throws WebServiceException, Exception;
	// DTOのキー情報をStringで取得します
	protected abstract String getKeyInfoD(D info);
	// XmlRootオブジェクトを作成します
	protected abstract T newInstance();
	// マネージャのレコードから、出力情報をXmlRootオブジェクトに加えます
	protected abstract void addInfo(T xmlInfo, D info) throws Exception;
	// XmlRootオブジェクトからXMLを出力します
	protected abstract void exportXml(T xmlInfo, String xmlFile) throws Exception;
	// XmlRootオブジェクトからエレメントのリストを取得します
	protected abstract List<E> getElements(T xmlInfo);
	// エレメントをマネージャに登録します
	protected abstract int registElement(E element) throws Exception;
	// エレメントのキー情報をStringで取得します
	protected abstract String getKeyInfoE(E info);
	// XmlRootオブジェクトをXMLから取得します
	protected abstract T getXmlInfo(String filePath) throws Exception;
	// XmlRootオブジェクトのスキーマバージョンをチェックします
	protected abstract int checkSchemaVersion(T xmlInfo) throws Exception;
	// XmlRootオブジェクトからエレメントの配列を取得します
	protected abstract E[] getArray(T info);
	// DTOのコンパレータ用のcompareメソッドです
	protected abstract int compare(D info1, D info2);
	// エレメントのコンパレータ用のcompareメソッドです
	protected abstract int sortCompare(E info1, E info2);
	// XmlRootオブジェクトにソート済みのエレメントをセットします
	protected abstract void setArray(T xmlInfo, E[] infoList);
	// 差分削除確認処理
	protected abstract void checkDelete(T xmlInfo) throws Exception;
	//オブジェクト権限同時インポート
	protected abstract void importObjectPrivilege(List<E> objectList) throws Exception;
	
	//差分削除
	// 重複確認用の事前メソッド、オーバライド前提
	protected void preCheckDuplicate(){}
	
	public Logger getLogger() {
		return log;
	}
	
	/**
	 * バージョン出力結果を判定し、ログ出力する
	 */
	public static boolean checkSchemaVersionResult(Logger log, int resCode, String type_tool, String version_tool, String revision_tool) {
		
		switch (resCode) {
		case 0:
			//スキーマタイプ・バージョン一致
			break;
		case -1:
			//スキーマタイプ不一致 （エラー）
			log.error( Messages.getString("SettingTools.SchemaVerionDifferent")  +" : tool version " + 
					type_tool + "-" + version_tool + "-" + revision_tool );
			return false;
		case -2:
			//XMLのバージョン > APのバージョン
			log.info( Messages.getString("SettingTools.SchemaVerionNew")  + " : tool version " +
					type_tool + "-" + version_tool + "-" + revision_tool );
			break;
		case -3:
			//XMLのバージョン < APのバージョン
			log.info(Messages.getString("SettingTools.SchemaVerionOld")  + " : tool version " +
					type_tool + "-" + version_tool + "-" + revision_tool );
			break;
		default:
			//この分岐は到達しない
			assert false;
			break;
		}
		return true;
	}
}
