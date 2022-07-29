/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.utility.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.settings.ui.dialog.UtilityDialogInjector;
import com.clustercontrol.utility.settings.ui.dialog.UtilityProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityResultDialog;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;


/**
 * インポートにおける 複数一括登録の制御向け処理のテンプレートクラス<Br>
 * 
 * インスタンス生成後、importExecuteを実行することで各種処理が行われる。<Br>
 * 
 * コンストラクタにて引き渡された requestRecordDtoのimportRecDtoListを、
 * まとめ処理単位数毎に分割して、callImportWrapperへと流し込む。<Br>
 * 
 * インポート時に内容が反映されなかった データ については一覧を作成し ダイアログにて表示する。(非表示可能)<Br>
 * 
 * 「異常時は操作取り消し」を選択されていた際に、インポートでなんらかの異常が発生した場合
 * 続行についてユーザに確認を行う。<Br>
 *
 * @param <T> requestRecordDto インポート向けのRESTAPIが利用するJsonデータのDtoクラス（個別のレコード向け）
 * @param <S> responseDto インポート向けのRESTAPIが返却するJsonデータのDtoクラス（全体向け）
 * @param <V> responseRecordDto インポート向けのRESTAPIが返却するJsonデータのDtoクラス（個別のレコード向け）
 */
public abstract class ImportClientController<T,S,V> {
	protected Logger log;
	protected List<T> importRecList;
	protected boolean occurException =false;
	protected Set<T> requestCompleteSet = new HashSet<T>();
	protected int ret;
	protected String importInfoName = "";
	protected List<V> importFailedList = new ArrayList<V>();
	protected List<V> importSuccessList = new ArrayList<V>();
	protected List<V> importSkipList = new ArrayList<V>();
	protected List<T> importNoRequestList = new ArrayList<T>();
	protected List<T> requestTimeoutList = new ArrayList<T>();
	protected boolean displayFailed =true;

	public ImportClientController(Logger logger, String importInfoName,List<T> importRecList ,boolean displayFailed) {
		this.importInfoName = importInfoName;
		this.log = logger;
		this.importRecList=importRecList;
		this.displayFailed=displayFailed;
	}

	public int importExecute(){

		//更新単位の件数毎にListを分割
		int importUnitNum = Integer.MAX_VALUE;
		if(ImportProcessMode.getImportUnitNum() != null){
			importUnitNum = ImportProcessMode.getImportUnitNum();
		}
		if( log.isDebugEnabled() ){
			log.debug("importExecute() :Number of units= " +importUnitNum);
		}
		List<List<T>> importUnitList = new ArrayList<List<T>>();			
		List<T> importRecDtoUnit = new ArrayList<T>();
		int allRecNum = importRecList.size();
		int blockNum = 0;
		for(int recIndex =0;  recIndex < allRecNum ;recIndex++ ){
			importRecDtoUnit.add(importRecList.get(recIndex));
			blockNum++;
			if ((blockNum == importUnitNum) || (recIndex + 1) == allRecNum) {
				importUnitList.add(importRecDtoUnit);
				importRecDtoUnit=new ArrayList<T>();
				blockNum = 0;
			}
		}

		// 更新単位にインポートメソッドを呼び出し、結果をログ出力
		// API異常発生時はそこで中断、レコード個別の異常発生時はユーザ選択次第で続行
		for (List<T> importTarget : importUnitList) {
			try {
				S response  = callImportWrapper(importTarget);
				List<V> resultList = getResRecList(response);
				occurException = getOccurException(response);
				requestCompleteSet.addAll(importTarget);
				int resNum = resultList.size();
				for (int resIndex = 0; resIndex < resNum; resIndex++) {
					V resRec =resultList.get(resIndex);
					if ( isResNormal(resRec) ){
						importSuccessList.add(resRec);
					} else if(isResSkip(resRec)){
						importSkipList.add(resRec);
					} else {
						if( log.isDebugEnabled() ){
							log.debug("importExecute() : error result. " + resRec.toString());
						}
						importFailedList.add(resRec);
						this.ret = SettingConstants.ERROR_INPROCESS;
					}
					setResultLog(resRec);
				}

				// 「異常時は操作取り消し」選択時に異常発生なら、続行の是非を確認
				if (ImportProcessMode.isRollbackIfAbnormal() && this.occurException) {
					if (!ImportProcessMode.isSameCancelForAbend()) {
						String[] dialogTitleArgs = new String[] { importInfoName };
						UtilityProcessDialog dialog = UtilityDialogInjector.createImportContinueDialog(null,
								Messages.getString("message.import.confirm7",dialogTitleArgs));
						int retContinue = dialog.open();
						ImportProcessMode.setSameCancelForAbend(dialog.getToggleState());
						switch (retContinue) {
						case UtilityDialogConstant.YES:
							ImportProcessMode.setCancelForAbend(false);
							break;
						case UtilityDialogConstant.NO:
						default:
							ImportProcessMode.setCancelForAbend(true);
							break;
						}
					}
					if (ImportProcessMode.isCancelForAbend()) {
						break;
					}
				}
			} catch (HinemosUnknown e) {
				log.info(Messages.getString("SettingTools.ImportFailed") + " : "
						+ HinemosMessage.replace(e.getMessage()));
				this.ret = SettingConstants.ERROR_INPROCESS;
				break;
			} catch (InvalidUserPass e) {
				log.info(Messages.getString("SettingTools.InvalidUserPass") + " : "
						+ HinemosMessage.replace(e.getMessage()));
				this.ret = SettingConstants.ERROR_INPROCESS;
				break;
			} catch (InvalidRole e) {
				log.info(Messages.getString("SettingTools.InvalidRole") + " : "
						+ HinemosMessage.replace(e.getMessage()));
				this.ret = SettingConstants.ERROR_INPROCESS;
				break;
			} catch (RestConnectFailed e) {
				log.warn(Messages.getString("SettingTools.ImportFailed") + " : "
						+ HinemosMessage.replace(e.getMessage()));
				this.ret = SettingConstants.ERROR_INPROCESS;
				// マネージャへのリクエストが応答タイムアウトの場合 マネージャ側の処理成否が不明なので別途エラー表示
				Throwable rootCause = ExceptionUtils.getRootCause(e);
				if (rootCause instanceof java.net.SocketTimeoutException && (rootCause.getMessage().equals("Read timed out") )) {
					requestCompleteSet.addAll(importTarget);
					requestTimeoutList.addAll(importTarget);
					for(T noReq : requestTimeoutList){
						String resMessage =  this.importInfoName + " : " + getReqKeyValue(noReq) ;
						log.warn(Messages.getString("SettingTools.ImportRequestTimeout") + " : " + resMessage);
					}
				}
				break;
			}
		}
		
		// 未依頼のレコードがあれば ImportNoRequestList に追加
		for(T rec : this.importRecList){
			if( !( requestCompleteSet.contains(rec) ) ){
				importNoRequestList.add(rec);
			}
		}
		
		// インポート出来なかったレコードがあれば 一覧をダイアログで表示(表示の場合のみ)
		if( !(importFailedList.isEmpty() && importNoRequestList.isEmpty() ) && displayFailed ){
			// ダイアログメッセージ編集
			StringBuilder messageBulider  = new StringBuilder();
			messageBulider.append( importInfoName +":"+Messages.getString("message.import.result3"));
			for (V errorRes : importFailedList) {
				messageBulider.append("\n" + getResKeyValue(errorRes));
			}
			for (T noReq : requestTimeoutList) {
				messageBulider.append("\n" + getReqKeyValue(noReq));
			}
			for (T noReq : importNoRequestList) {
				messageBulider.append("\n" + getReqKeyValue(noReq));
			}

			// ダイアログdetail編集
			List<String> detailList = new ArrayList<String>();
			for(V errorRes : importFailedList){
				String resMessage =  getResKeyValue(errorRes) ;
				String resExcpotionMessage = getRestExceptionMessage(errorRes); 
				if(resExcpotionMessage != null){
					//例外が発生していれば 詳細に表示
					resMessage = resMessage + ":" + resExcpotionMessage;
					detailList.add(resMessage);
				}
			}
			for(T noReq : requestTimeoutList){
				String resMessage = getReqKeyValue(noReq) + ":"+ Messages.getString("SettingTools.ImportRequestTimeout");
				detailList.add(resMessage);
			}
			// detail が 0 件だとダイアログが表示されないので 空レコードを挿入
			if (detailList.size() == 0) {
				detailList.add("");
			}

			UtilityResultDialog dialog = UtilityDialogInjector.createImportResultDialog(null, this.toString(),
					Messages.getString("message.confirm"), messageBulider.toString(), detailList);
			dialog.open();
		}
		
		return this.ret;
	}
	
	/**
	 * 正常に処理された処理結果の一覧を返却します。
	 * 
	 */
	public List<V> getImportSuccessList(){
		return this.importSuccessList;
	} 

	/**
	 * responseRecordDto を元に結果ログを出力します。
	 * 
	 * 変更が必要な場合のみ、オーバーライドすること。
	 * 
	 */
	protected void setResultLog( V responseRec ){
		String keyValue = getResKeyValue(responseRec);
		if ( isResNormal(responseRec) ) {
			log.info(Messages.getString("SettingTools.ImportSucceeded") + " : " + this.importInfoName + ":" + keyValue);
		} else if(isResSkip(responseRec)){
			log.info(Messages.getString("SettingTools.ImportSucceeded.Skip") + " : " + this.importInfoName + ":" + keyValue);
		} else {
			log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + this.importInfoName + ":" + keyValue + " : "
					+ HinemosMessage.replace(getRestExceptionMessage(responseRec)));
		}
	}
	
	/**
	 * まとめ処理数毎に分割されたrequestRecordDtoのリストをインポート用RESTAPIへ流し込む
	 */
	protected abstract S callImportWrapper(List<T> importRecList)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed;

	/**
	 * responseDto から responseRecordDto のリストを取得する。
	 */
	protected abstract List<V> getResRecList ( S importResponse );

	/**
	 * responseDto から 異常の有無を取得する。（インポートの続行制御向け）
	 */
	protected abstract Boolean getOccurException ( S importResponse );

	/**
	 * requestRecordDtoからKeyValueを表す文字列を取得する（インポート失敗一覧表示向け）
	 */
	protected abstract String getReqKeyValue( T importRec );

	/**
	 * responseRecordDtoからKeyValueを表す文字列を取得する（インポート失敗一覧表示向け）
	 */
	protected abstract String getResKeyValue( V responseRec );

	/**
	 * responseRecordDto から 正常終了か否かを取得する（インポート失敗一覧表示向け）
	 */
	protected abstract boolean isResNormal( V responseRec );

	/**
	 * responseRecordDto から 処理がスキップされたか否かを取得する（インポート失敗一覧表示向け）
	 * マネージャ側で スキップを返すような実装があった場合のみ、オーバーライドすること。(Skipが必要なケースはレア)
	 */
	protected boolean isResSkip( V responseRec ){
		return false;
	}

	/**
	 * responseRecordDtoから ExceptionMessageを表す文字列を取得する（ログ向け）
	 */
	protected abstract String getRestExceptionMessage( V responseRec );

}

