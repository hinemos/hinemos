/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.traputil.action;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.utility.constant.SnmpTrapMibMstConstant;
import com.clustercontrol.utility.traputil.bean.SnmpTrapMasterInfo;
import com.clustercontrol.utility.traputil.bean.SnmpTrapMibMasterData;

import com.clustercontrol.utility.mib.Mib;
import com.clustercontrol.utility.mib.MibLoader;
import com.clustercontrol.utility.mib.MibLoaderException;
import com.clustercontrol.utility.mib.MibSymbol;
import com.clustercontrol.utility.mib.MibValue;
import com.clustercontrol.utility.mib.MibValueSymbol;
import com.clustercontrol.utility.mib.snmp.SnmpNotificationType;
import com.clustercontrol.utility.mib.snmp.SnmpTrapType;
import com.clustercontrol.utility.mib.value.NumberValue;
import com.clustercontrol.utility.mib.value.ObjectIdentifierValue;

/**
 * MIBファイルパースクラス
 * 
 * @version 6.1.0
 * @since 2.4.0
 */
public class ParseMib {

	private static Log log = LogFactory.getLog(ParseMib.class);

	/*
	 * v1、v2トラップの抽出処理はcom.clustercontrol.snmptrap.factory.TrapSnmpのまま
	 */

	private ArrayList<String> searchPaths = null;
	private MibLoader loader = null;
	private Mib mib = null;
	
	/**
	 * コンストラクタ
	 *
	 */
	public ParseMib() {
		this.loader = new MibLoader();
		loader.reset();
	}
	
	/**
	 * ロードされたMIBオブジェクトからトラップタイプ
	 * (TRAP-TYPE/NOTIFICATION-TYPE)を取り出し、
	 * SnmpTrapMasterInfoのリストを返す
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws MibLoaderException
	 */
	public boolean parseTrapMib(String file, boolean searchCurrentDir)
	throws IOException, MibLoaderException {
		
		//指定されたディレクトリを検索パスに設定
		if(this.searchPaths != null){
			
			for(int i = 0; i < this.searchPaths.size(); i++) {
				loader.addAllDirs(new File(this.searchPaths.get(i)));
			}
		}
		
		//「入力MIBファイルのカレントディレクトリをMIB検索ディレクトリに含める」
		//がチェックされている場合は、MIBファイルの存在するディレクトリを検索パスに追加
		if(searchCurrentDir) {
			loader.addDir(new File(file).getParentFile());
		}
		
		//ロード
		mib = loader.load(new File(file));
		
		//トラップがあるかチェック
		boolean trapExist = false;
		
		//すべてのMIBシンボルを取得し、MibTypeをチェックする
		Collection<MibSymbol> symbols = mib.getAllSymbols();
		Iterator<MibSymbol> itr_mibSymbol = symbols.iterator();
		while(itr_mibSymbol.hasNext()) {
			MibSymbol symbol = itr_mibSymbol.next();
			if (symbol instanceof MibValueSymbol) {
				MibValueSymbol mibValueSymbol = (MibValueSymbol) symbol;
				String mibTypeName = mibValueSymbol.getType().getName();
				
				//MibTypeがトラップのものだったらtrueとする
				if (mibTypeName.equals("NOTIFICATION-TYPE")) {
					trapExist = true;
					break;
				} else if (mibTypeName.equals("TRAP-TYPE")) {
					trapExist = true;
					break;
				}
			}
		}
		
		//trapが存在するかどうかのフラグを返す
		return trapExist;
	}

	/**
	 * MIBマスター情報の取得
	 */
	public SnmpTrapMibMasterData getMibMaster() {
		SnmpTrapMibMasterData master = new SnmpTrapMibMasterData();
		String desc = null;
		
		//MIB名、説明、並び順のみ
		//更新・変更の各日付、ユーザはマネージャ更新時に付加すること
		master.setMib(mib.getName());
		master.setOrderNo(SnmpTrapMibMstConstant.ENTERPRISE_MIB_ORDER_NO);
		
		if(mib.getHeaderComment() != null) {
			desc = SnmpTrapMibMstConstant.HEADER + mib.getHeaderComment();
		}
		
		if(mib.getFooterComment() != null) {
			desc = desc + " ; " + SnmpTrapMibMstConstant.FOOTER + mib.getFooterComment();
		}
		
		if(desc != null){
			if(desc.length() > DataRangeConstant.VARCHAR_256){
				master.setDescription(desc.substring(0,DataRangeConstant.VARCHAR_256));
			}
		}
	
		return master;
	}
	
	/**
	 * TRAPTYPEまたはNOTIFICATION-TYPEのトラップ情報の抽出
	 * 
	 * @return
	 */
	public ArrayList<SnmpTrapMasterInfo> getDetails(int priority, boolean strictlyAnalyze) {

		log.info("import mibfile name : " + mib.getName() + ", version : " + mib.getSmiVersion() + ", Stricty Analyze : " + strictlyAnalyze);
		
		ArrayList<SnmpTrapMasterInfo> detailList = new ArrayList<SnmpTrapMasterInfo>();
		SnmpTrapMasterInfo detail = null;

		Iterator<MibSymbol> it = mib.getAllSymbols().iterator();
		while (it.hasNext()) {
			
			detail = getDetail(it.next(), priority, strictlyAnalyze);
			if(detail != null){
				
				detailList.add(trimMibContents(detail));

			}
		}
		
		return detailList;
	}
			
	
	private SnmpTrapMasterInfo getDetail(MibSymbol symbol, int priority, boolean strictlyAnalyze) {
		SnmpTrapMasterInfo trap = null;
		
		if (symbol instanceof MibValueSymbol) {
			MibValue value = ((MibValueSymbol) symbol).getValue();
			
			if (value != null) {

				// 厳密な解析を行う場合（default）
				if(strictlyAnalyze) {
					// ﾄラップ名 symbol.getName()
					// OID value.toString());
					if(mib.getSmiVersion() == 1) {
						
						//V1形式
						if (((MibValueSymbol)symbol).getType() instanceof SnmpTrapType ) {
							
							trap = parseV1Trap((MibValueSymbol) symbol, (NumberValue) value, priority);
							
						}
						
					}
					else if(mib.getSmiVersion() == 2) {
						if (value instanceof ObjectIdentifierValue) {						
							//V2形式
							if (((MibValueSymbol)symbol).getType() instanceof SnmpNotificationType) {

								trap = parseV2Trap((MibValueSymbol) symbol, (ObjectIdentifierValue) value, priority);

							}
						}
					}
				}
				// 厳密な解析を行わない場合
				else {
					//V1形式
					if (((MibValueSymbol)symbol).getType() instanceof SnmpTrapType ) {
						
						trap = parseV1Trap((MibValueSymbol) symbol, (NumberValue) value, priority);
						
					}
					//V2形式
					else if (((MibValueSymbol)symbol).getType() instanceof SnmpNotificationType) {

						trap = parseV2Trap((MibValueSymbol) symbol, (ObjectIdentifierValue) value, priority);

					}
				}
				
			}
		}

		return trap;
	}
	
	/**
	 * 文字数制限チェック
	 * ueiとlogmsgを対象とし、制限を越える場合は
	 * エラーではなく、制限内にトリムして返す
	 * 
	 * @param trap
	 */
	private SnmpTrapMasterInfo trimMibContents(SnmpTrapMasterInfo trap) {
		String uei = trap.getUei();
		String logmsg = trap.getLogmsg();
		
		if(uei.length() > DataRangeConstant.VARCHAR_256) {
			trap.setUei(uei.substring(0,DataRangeConstant.VARCHAR_256));
		}
		
		if(logmsg.length() > DataRangeConstant.VARCHAR_256) {
			trap.setLogmsg(logmsg.substring(0,DataRangeConstant.VARCHAR_256));
		}
		
		return trap;
	}

	/**
	 * V1形式のMibValueSymbolからトラップ詳細情報を取得する
	 * 
	 * @return
	 */
	private SnmpTrapMasterInfo parseV1Trap(MibValueSymbol symbol, NumberValue value, int priority) {
		
		SnmpTrapMasterInfo trap = new SnmpTrapMasterInfo();
		
		MibValueSymbol valueSymbol = symbol;
		SnmpTrapType trapType = (SnmpTrapType)valueSymbol.getType();
		
		//mib
		trap.setMib(this.mib.getName());
		
		//trap_oid
		trap.setTrapOid(SnmpTrapMibMstConstant.DOT_CHAR + trapType.getEnterprise().toString());

		//TODO GENERICトラップ判定
		//generic_id
		trap.setGenericId(SnmpTrapMibMstConstant.ENTERPRISE_GENERIC_ID);
		
		//specific_id
		trap.setSpecificId(Integer.parseInt( value.toString()) );
		
		//uei
		/* ドット区切りのオブジェクト名のツリー形式で出力される
		   ex) iso(1).org(3).dod(6).internet(1).private(4).enterprises(1).企業名.製品名・・・
		   企業名以降を切り出してueiにセットする
		 */
		String uei = null;
		uei = ((ObjectIdentifierValue)(trapType.getEnterprise())).toDetailString() + "/" + symbol.getName();
		//indexの切り出し
		uei = uei.replaceAll("\\([0-9]*\\)", "");
		//ドット区切りかからスラッシュ区切りに
		uei = uei.replaceAll("\\.", "/");
		//企業名以降の切り出し
		uei = uei.replaceAll(".*(mib-2|enterprises)/", "");
		trap.setUei( uei);

		//logmsg
		//desc
		/*
		 * logmsgフォーマット(改行無し)
		 * 
		 *   "トラップ名(MIB名) " received. "パラメータ名:"%parm[#num]% ・・・
		 *   
		 * descフォーマット(改行有り)
		 * 
		 *   "MIBから取得するdescription"
		 *   "パラメータ名:"%parm[#num]%, ・・・
		 */
		String logmsg = "";
		String desc = "";
		StringBuffer parms = new StringBuffer();
		logmsg = valueSymbol.getName() + "(" + mib.getName() + ")" + SnmpTrapMibMstConstant.LOGMSG;
		desc = trapType.getDescription();
		
		//パラメータリストの取り出し
		ArrayList<ObjectIdentifierValue> valueList = trapType.getVariables();
		for(int i = 0; i < valueList.size(); i++) {
			parms.append(((ObjectIdentifierValue) valueList.get(i)).getName());
			parms.append("=%parm[#");
			parms.append(i+1);
			parms.append("]% ");
		}
		logmsg += " " + parms;
		desc += "\n" + parms;
				
		trap.setLogmsg(logmsg);
		trap.setDescr(desc);

		//priority
		trap.setPriority(priority);
		
		return trap;
	}

	/**
	 * V2形式のMibValueSymbolからトラップ詳細情報を取得する
	 * 
	 * @return
	 */
	private SnmpTrapMasterInfo parseV2Trap(MibValueSymbol symbol, ObjectIdentifierValue value, int priority) {
		SnmpTrapMasterInfo trap = new SnmpTrapMasterInfo();
		
		MibValueSymbol valueSymbol = symbol;
		SnmpNotificationType notificatiionType = (SnmpNotificationType)valueSymbol.getType();
		String trapOid = null;
		
		//mib
		trap.setMib(this.mib.getName());
		
		//trap_oid, generic_id, specific_id
		trapOid = SnmpTrapMibMstConstant.DOT_CHAR + value.toString();
		
		int length = trapOid.length();
		int lastIndex = trapOid.lastIndexOf(SnmpTrapMibMstConstant.DOT_CHAR);
		
		String lastSubIdStr = trapOid.substring(lastIndex + 1);
		int lastSubId = -1;
		
		try {
			lastSubId = Integer.parseInt(lastSubIdStr);
		} catch (NumberFormatException nfe) {
			lastSubId = -1;
		}
		
		// スタンダードトラップかチェック
		if (SnmpTrapMibMstConstant.GENERIC_TRAPS.contains(trapOid)) {
			/*
			 * generic trap
			 */
			
			//oid
			trap.setTrapOid(SnmpTrapMibMstConstant.SNMP_TRAPS + SnmpTrapMibMstConstant.DOT_CHAR + trapOid.charAt(length - 1));
			
			//generic_id
			trap.setGenericId(lastSubId - 1);
			
			//specific_id
			trap.setSpecificId(SnmpTrapMibMstConstant.GENERIC_SPECIFIC_ID);
		}
		else {
			/*
			 * enterprise trap
			 */
			
			//oid
			int nextToLastIndex = trapOid.lastIndexOf(SnmpTrapMibMstConstant.DOT_CHAR, lastIndex - 1);

			String nextToLastSubIdStr = trapOid.substring(nextToLastIndex + 1, lastIndex);
			if (nextToLastSubIdStr.equals("0")) {
				// エンタープライズ値をセット
				trap.setTrapOid(trapOid.substring(0, nextToLastIndex));
			} else {
				trap.setTrapOid(trapOid.substring(0, lastIndex));
			}
			
			//generic_id
			trap.setGenericId(SnmpTrapMibMstConstant.ENTERPRISE_GENERIC_ID);
			
			//specific_id
			trap.setSpecificId(value.getValue());
		}
		
		//uei
		/* ドット区切りのオブジェクト名のツリー形式で出力される
		   ex) iso(1).org(3).dod(6).internet(1).private(4).enterprises(1).企業名.製品名・・・
		   企業名以降を切り出してueiにセットする
		 */
		String uei = null;
		uei = value.toDetailString();
		//indexの切り出し
		uei = uei.replaceAll("\\([0-9]*\\)", "");
		//ドット区切りかからスラッシュ区切りに
		uei = uei.replaceAll("\\.", "/");
		//企業名以降の切り出し
		uei = uei.replaceAll(".*(mib-2|enterprises)/", "");
		trap.setUei( uei);
		
		//logmsg
		//desc
		/*
		 * logmsgフォーマット(改行無し)
		 * 
		 *   "トラップ名(MIB名) " received. "パラメータ名:"%parm[#num]% ・・・
		 *   
		 * descフォーマット(改行有り)
		 * 
		 *   "MIBから取得するdescription"
		 *   "パラメータ名:"%parm[#num]%, ・・・
		 */
		String logmsg = "";
		String desc = "";
		StringBuffer parms = new StringBuffer();
		logmsg = value.getName() + "(" + mib.getName() + ")" + SnmpTrapMibMstConstant.LOGMSG;
		desc = notificatiionType.getDescription();
		
		//パラメータリストの取り出し
		List<ObjectIdentifierValue> valueList = notificatiionType.getObjects();
		for(int i = 0; i < valueList.size(); i++) {
			parms.append(((ObjectIdentifierValue) valueList.get(i)).getName());
			parms.append("=%parm[#");
			parms.append(i+1);
			parms.append("]% ");
		}
		logmsg += " " + parms;
		desc += "\n" + parms;
				
		trap.setLogmsg(logmsg);
		trap.setDescr(desc);
		
		//priority
		trap.setPriority(priority);
	
		return trap;
	}

	
	/** MIBファイル検索パスを追加します */
	public void addSearchPath(String dir) {
		if(this.searchPaths == null) {
			this.searchPaths = new ArrayList<String>();
		}
		this.searchPaths.add(dir);
	}
	
	/** MIBローダーと設定情報を初期化します */
	public void reset(){
		this.loader.removeAllDirs();
		this.searchPaths = null;
		this.mib = null;
		this.loader.reset();
	}
	
}
