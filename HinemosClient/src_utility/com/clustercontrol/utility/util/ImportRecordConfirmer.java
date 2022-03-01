/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.utility.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.settings.ui.dialog.UtilityDialogInjector;
import com.clustercontrol.utility.settings.ui.dialog.UtilityProcessDialog;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;

/**
 * インポートにおけるレコードの存在確認およびユーザへの上書き確認を行うためのテンプレートクラス<Br>
 * 
 * インスタンス生成後、executeConfirmを実行することで各種処理が行われる。<Br>
 * 
 * コンストラクタにて引き渡された XMLDtoのimportRecDtoListを、
 * getExistIdSet にて取得したSetを用いて 存在確認する。<Br>
 * 
 * 既設レコードならダイアログにて上書き/スキップをユーザに選択させ、
 * インポート（新規/上書き）対象となったレコードはconvertDtoXmlToRestReq を 通じて RESTDtoへ変換し、
 * getImportRecDtoListにて変換結果を取得できる。<Br>
 * 
 * 存在確認の結果については、setNewRrecordFlg メソッドを通じてRESTDtoへ反映可能。<Br>
 *
 * @param <T> XmlDto インポートを試行したいXMLデータのDtoクラス（個別のレコード向け）
 * @param <V> restDto インポート向けのRESTAPIが利用するJsonデータのDtoクラス（個別のレコード向け）
 * @param <S> レコードのkeyValueを表現するためのクラス   (通常はString)
 */
public abstract class ImportRecordConfirmer<T,V,S> {
	protected Logger log;
	protected T[] importXmlDtoList;
	protected int ret = 0;
	protected Set<S> existIdSet ;
	protected List<V> importRecDtoList = new ArrayList<V>();
	protected String confirmMessageId =  "message.import.confirm2" ;
	
	public ImportRecordConfirmer(Logger logger, T[] importRecDtoList){
		this.log = logger;
		this.importXmlDtoList = importRecDtoList;
	}
	
	public int  executeConfirm(){
		//存在チェック向けに現状のID一覧を取得
		try {
			existIdSet = getExistIdSet();
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import PlatformRepositoryNode (Error)");
			return ret;
		}
		
		// import用データ生成（既存レコードか確認しつつ、XMLオブジェクトをRESTAPI向けオブジェクト変換）
		for (int i = 0; i < importXmlDtoList.length; i++) {
			T xmlDto = importXmlDtoList[i];
			V restDto  = null;
			//持ち回り用データに変換して型チェック
			try{
				restDto = convertDtoXmlToRestReq(xmlDto);
				// 登録不要なデータなら NULLで戻ってくるので以後の処理はスキップ
				if(restDto == null){
					continue;
				} 
				//必要な項目が入っていない場合エラーとする
				if ( isLackRestReq(restDto)) {
					log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + getKeyValueXmlDto(xmlDto));
					ret = SettingConstants.ERROR_INPROCESS;
					continue;
				}
			} catch (HinemosUnknown |InvalidSetting e) {
				log.info(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				continue;
			}

			//既存レコードの場合、処理方法を選択してもらう。(上書き、スキップ、処理中断のいずれか)
			//ただし前回選択で「後続も同様に処理」が選択されていた場合は、それを引き継ぐ
			try{
				boolean isNewRecord =true; 
				if ( isExistRecord(xmlDto) ){
					String targetId = getKeyValueXmlDto(xmlDto);
					if(!ImportProcessMode.isSameprocess()){
						String[] args = {targetId};
						UtilityProcessDialog dialog = UtilityDialogInjector.createImportProcessDialog(
								null, Messages.getString(confirmMessageId, args));
						ImportProcessMode.setProcesstype(dialog.open());
						ImportProcessMode.setSameprocess(dialog.getToggleState());
					}
					if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.UPDATE){
						isNewRecord=false;
					} else if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
						//スキップが選択された該当のレコードがインポート対象外
						log.info(Messages.getString("SettingTools.ImportSucceeded.Skip") + " : " + targetId);
						continue;
					} else if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
						//キャンセルが選択されたら、インポート中断
						log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
						ret = SettingConstants.ERROR_CANCEL;
						break;
					}
				}
				setNewRecordFlg(restDto,isNewRecord);
				if (!(additionalCheck(restDto))) {
					continue;
				}
				
				importRecDtoList.add(restDto);
					
			} catch (Exception e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()),e);
				ret = SettingConstants.ERROR_INPROCESS;
				break;
			}
			
		}
		return ret;
	}

	public List<V> getImportRecDtoList(){
		return this.importRecDtoList;
	}
	public void setConfirmMessageId( String confirmMessageId ){
		this.confirmMessageId = confirmMessageId;
	}
	
	/**
	 * XMLDtoを元に存在チェックを行う(Override向けにメソッド化している)
	 */
	protected boolean isExistRecord(T xmlDto){
		return existIdSet.contains(getId(xmlDto));
	};

	/**
	 * XMLDtoをRestRequest向けDtoに変換する
	 * @param XMLDto
	 * @return RESTDto
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 */
	protected abstract V convertDtoXmlToRestReq(T xmlDto) throws HinemosUnknown ,InvalidSetting ;

	/**
	 * 変換されたRESTDtoを対象に内容の不足がないかをチェック
	 */
	protected abstract boolean isLackRestReq(V restDto);

	/**
	 * XMLDtoからKeyValueを表す文字列を取得する（ダイアログ表示向け）
	 */
	protected abstract String getKeyValueXmlDto(T xmlDto);

	/**
	 * 存在確認用のSetを取得する。
	 * 
	 * isExistRecordメソッドでのみ利用しているので左記をOverrideするなら空実装でもよい。
	 */
	protected abstract Set<S> getExistIdSet() throws Exception;

	/**
	 * XMLDtoからIDを取得する（存在チェック向け、存在確認用のSetとの突合に利用）
	 * 
	 * isExistRecordメソッドでのみ利用しているので左記をOverrideするなら空実装でもよい。
	 */
	protected abstract S getId(T xmlDto);

	/**
	 * 変換されたRESTDtoを対象に新規レコードフラグを設定（上書きならfalseとなる）
	 */
	protected abstract void setNewRecordFlg(V restDto, boolean flag);
	
	/**
	 * 存在チェック以後に追加のチェックを行いたい場合、ここに上書き実装する。
	 */
	protected boolean additionalCheck(V restDto){
		return true;
	}

}
