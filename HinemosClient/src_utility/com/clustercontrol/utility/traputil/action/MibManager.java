/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.traputil.action;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.constant.SnmpTrapMibMstConstant;
import com.clustercontrol.utility.traputil.bean.SnmpTrapMasterInfo;
import com.clustercontrol.utility.traputil.bean.SnmpTrapMibMasterData;


/**
 * MIB情報を管理するアクションクラス<BR>
 * シングルトン。
 * 
 * @version 6.1.0
 * @since 1.2.0
 * 
 * 
 */
public class MibManager {
	
	/** シングルトン用インスタンス。 */
	private static MibManager INSTANCE = new MibManager();

	/** MIB名一覧のクライアント内データ */
	private ArrayList<SnmpTrapMibMasterData> mibMasterList = null;

	/** MIB詳細のクライアント内データ */
	private ArrayList<SnmpTrapMasterInfo> mibDetailList = null;

	/** マネージャとの同期モード */
	private boolean isSync = false;

	/**
	 * 本クラスのインスタンスを返します。<BR>
	 * シングルトン用インスタンスが<code> null </code>ならば、インスタンスを生成します。<BR>
	 * シングルトン用インスタンスが存在すれば、シングルトン用インスタンスを返します。
	 * 
	 * @return 唯一のインスタンス
	 */
	public static MibManager getInstance(boolean isSync) {
		INSTANCE.isSync = isSync;

		return INSTANCE;
	}

	/**
	 * コンストラクタ。<BR>
	 * アクセスを禁止します。
	 */
	private MibManager() {
		this.mibMasterList = new ArrayList<SnmpTrapMibMasterData>();
		this.mibDetailList = new ArrayList<SnmpTrapMasterInfo>();

	}

	/**
	 * 初期処理を行います。
	 * 
	 */
	public void initialize() {
		if (this.isSync) {
		} else {
			clearMibData();
		}
	}

	/**
	 * 全てのMIB一覧情報を返します。
	 * <p>
	 * 
	 * @return MIB一覧情報一覧
	 */
	public ArrayList<SnmpTrapMibMasterData> getMibMasters() throws InvocationTargetException{

		ArrayList<SnmpTrapMibMasterData> result = null;

		if (this.isSync) {
//			try {
//				MibManagerLib lib = new MibManagerLib();
//				result =  (ArrayList<SnmpTrapMibMasterData>)lib.getMibMasters(uid,pw,url);
//			} catch (IllegalArgumentException e) {
//				log.error(e.getMessage(), e);
//			} catch (SecurityException e) {
//				log.error(e.getMessage(), e);
//			} catch (HinemosUnknown_Exception e) {
//				log.error(e.getMessage(), e);
//			} catch (InvalidRole_Exception e) {
//				log.error(e.getMessage(), e);
//			} catch (InvalidUserPass_Exception e) {
//				log.error(e.getMessage(), e);
//			} catch (MonitorNotFound_Exception e) {
//				log.error(e.getMessage(), e);
//			}
		} else {
			result = this.mibMasterList;
		}
		
		// マネージャと接続できず、nullのままであった場合
		if(result == null) {
			result = new ArrayList<SnmpTrapMibMasterData>();
		}
		
		return result;

	}

	/**
	 * 引数で指定したMIB名のMIB一覧情報を返します。
	 * <p>
	 * MIB一覧情報キャッシュよりMIB一覧情報を取得します。
	 * 
	 * @param mib
	 *            MIB名
	 * @return MIB一覧情報
	 * @throws InvocationTargetException 
	 */
	public SnmpTrapMibMasterData getMibMaster(String mib) throws InvocationTargetException {

		SnmpTrapMibMasterData result = null;

//		if (this.isSync) {
//			try {
//				MibManagerLib lib = new MibManagerLib();
//				result = (SnmpTrapMibMasterData)lib.getMibMaster(mib, uid,pw,url);
//			} catch (IllegalArgumentException e) {
//				log.error(e.getMessage(), e);
//			} catch (SecurityException e) {
//				log.error(e.getMessage(), e);
//				log.error(e.getMessage(), e);
//			} catch (HinemosUnknown_Exception e) {
//				log.error(e.getMessage(), e);
//			} catch (InvalidRole_Exception e) {
//				log.error(e.getMessage(), e);
//			} catch (InvalidUserPass_Exception e) {
//				log.error(e.getMessage(), e);
//			} catch (MonitorNotFound_Exception e) {
//				log.error(e.getMessage(), e);
//			}
//		} else {
//			result = getMibMasterLocal(mib);
//		}

		return result;
	}

	/**
	 * 引数で指定したMIB一覧情報を追加します。
	 * 
	 * @param mibMaster
	 *            MIBマスター情報
	 * @return 成功した場合、<code> true </code>
	 * @throws InvocationTargetException 
	 */
	public boolean addMibMaster(SnmpTrapMibMasterData mibMaster) throws InvocationTargetException {

		if (mibMaster == null) {
			return false;
		}

		if (this.isSync) {
			boolean result = false;

//			try {
//				MibManagerLib lib = new MibManagerLib();
//				result = (Boolean)lib.addMibMaster(mibMaster, uid,pw,url);
//			} catch (IllegalArgumentException e) {
//				log.error(e.getMessage(), e);
//			} catch (SecurityException e) {
//				log.error(e.getMessage(), e);
//			} catch (HinemosUnknown_Exception e) {
//				log.error(e.getMessage(), e);
//			} catch (InvalidRole_Exception e) {
//				log.error(e.getMessage(), e);
//			} catch (InvalidUserPass_Exception e) {
//				log.error(e.getMessage(), e);
//			} catch (MonitorNotFound_Exception e) {
//				log.error(e.getMessage(), e);
//			}
			return result;
		} else {

			// 重複チェック
			/*
			 * マネージャ情報とではなくファイルから既にロードしたMIB情報 (インポートビューに表示されているMIB)と比較する
			 * 比較内容はMIB名のみ
			 * 
			 * 入力ダイアログの時点で同一ファイル名に対して行っているが、 異なるダイアログインスタンスでの入力は防げないのと、
			 * ファイル名が異なる場合もあるため、ここでも実施する
			 */
			for (int i = 0; i < mibMasterList.size(); i++) {
				if (((SnmpTrapMibMasterData) mibMasterList.get(i)).getMib()
						.equals(mibMaster.getMib())) {

					MessageDialog.openError(null,
							Messages.getString("message"), Messages
									.getString("message.traputil.3")
									+ mibMaster.getMib());

					return false;
				}
			}

			// ArrayListの最後尾へ追加
			this.mibMasterList.add(mibMaster);
			return true;
		}
	}

	/**
	 * MIB一覧情報の更新日付・ユーザのみを更新します
	 * 
	 * @param mibList
	 * @return
	 * @throws InvocationTargetException 
	 */
	public boolean modifyOnlyDateUser(ArrayList<String> mibList) throws InvocationTargetException {

		// マネージャ非同期モードでは動作しない
		if (!this.isSync) {
			return false;
		}

		SnmpTrapMibMasterData master = null;
		for (int i = 0; i < mibList.size(); i++) {
			master = getMibMaster(mibList.get(i));

			// 日付とユーザのみを更新するために、masterをそのままセット
			if (master != null) {
				modifyMibMaster(master);
			}
		}

		return true;
	}

	/**
	 * 引数で指定したMIB情報を変更します。
	 * 
	 * @param MIBマスター情報
	 * @return 成功した場合、<code> true </code>
	 * @throws InvocationTargetException 
	 */
	public boolean modifyMibMaster(SnmpTrapMibMasterData mibMaster) throws InvocationTargetException {

		boolean result = false;

		if (this.isSync) {
//			try {
//				MibManagerLib lib = new MibManagerLib();
//				result = (Boolean)lib.modifyMibMaster(mibMaster, uid,pw,url);
//			} catch (IllegalArgumentException e) {
//				log.error(e.getMessage(), e);
//			} catch (SecurityException e) {
//				log.error(e.getMessage(), e);
//			} catch (HinemosUnknown_Exception e) {
//				log.error(e.getMessage(), e);
//			} catch (InvalidRole_Exception e) {
//				log.error(e.getMessage(), e);
//			} catch (InvalidUserPass_Exception e) {
//				log.error(e.getMessage(), e);
//			} catch (MonitorNotFound_Exception e) {
//				log.error(e.getMessage(), e);
//			}
			
		} else {
			// Ver2.4 インポート時のキャッシュ情報の変更は
			// 行わせないため呼ばれないはずだが念のため実装

			// PKで検索(クライアント実装)
			for (int i = 0; i < mibMasterList.size(); i++) {
				if (((SnmpTrapMibMasterData) mibMasterList.get(i)).getMib()
						.equals(mibMaster.getMib())) {
					mibMasterList.set(i, mibMaster);
				}
			}
			result = true;
		}

		return result;
	}

	/**
	 * 引数で指定したMIBマスター情報を削除します。
	 * 
	 * @param mib
	 *            MIB名
	 * @return 成功した場合、<code> true </code>
	 * @throws InvocationTargetException 
	 */
	public boolean deleteMibMaster(String mib) throws InvocationTargetException {

		if (this.isSync) {

			boolean result = false;
//			try {
//				MibManagerLib lib = new MibManagerLib();
//				result = (Boolean)lib.deleteMibMaster(mib, uid, pw, url);
//			} catch (IllegalArgumentException e) {
//				log.error(e.getMessage(), e);
//			} catch (SecurityException e) {
//				log.error(e.getMessage(), e);
//			} catch (HinemosUnknown_Exception e) {
//				log.error(e.getMessage(), e);
//			} catch (InvalidRole_Exception e) {
//				log.error(e.getMessage(), e);
//			} catch (InvalidUserPass_Exception e) {
//				log.error(e.getMessage(), e);
//			} catch (MonitorNotFound_Exception e) {
//				log.error(e.getMessage(), e);
//			}
			
			return result;

		} else {
			// PKで検索(クライアント実装)
			for (int i = 0; i < mibMasterList.size(); i++) {
				if (((SnmpTrapMibMasterData) mibMasterList.get(i)).getMib()
						.equals(mib)) {
					mibMasterList.remove(i);
				}
			}
			return true;
		}
	}

	/** ************************************************************* */
	/* MIB詳細情報操作メソッド */
	/** ************************************************************* */

	/**
	 * PK(mib,trap_oid,specific_id,generic_id)リストを含むMIB詳細情報を マネージャから取得します。
	 * 
	 * @param info
	 * @return
	 * @throws InvocationTargetException 
	 */
	public SnmpTrapMasterInfo getMibDetail(SnmpTrapMasterInfo info) throws InvocationTargetException {

		SnmpTrapMasterInfo result = null;
//		try {
//			MibManagerLib lib = new MibManagerLib();
//			result = (SnmpTrapMasterInfo)lib.getMibDetail(info, uid, pw, url);
//		} catch (IllegalArgumentException e) {
//			log.error(e.getMessage(), e);
//		} catch (SecurityException e) {
//			log.error(e.getMessage(), e);
//		}
		
		return result;
	}

	/**
	 * 
	 * @param master
	 * @return
	 * @throws InvocationTargetException 
	 */
	public ArrayList<SnmpTrapMasterInfo> getMibDetails(
			SnmpTrapMibMasterData master) throws InvocationTargetException {

		return getMibDetails(master.getMib());

	}

	/**
	 * 
	 * @param mib
	 * @return
	 * @throws InvocationTargetException 
	 */
	public ArrayList<SnmpTrapMasterInfo> getMibDetails(String mib) throws InvocationTargetException {

		ArrayList<SnmpTrapMasterInfo> result = null;

		if (this.isSync) {
//			try {
//				MibManagerLib lib = new MibManagerLib();
//				result = (ArrayList<SnmpTrapMasterInfo>)lib.getMibDetails(mib, uid, pw, url);
//			} catch (IllegalArgumentException e) {
//				log.error(e.getMessage(), e);
//			} catch (SecurityException e) {
//				log.error(e.getMessage(), e);
//			} catch (HinemosUnknown_Exception e) {
//				log.error(e.getMessage(), e);
//			} catch (InvalidRole_Exception e) {
//				log.error(e.getMessage(), e);
//			} catch (InvalidUserPass_Exception e) {
//				log.error(e.getMessage(), e);
//			} catch (MonitorNotFound_Exception e) {
//				log.error(e.getMessage(), e);
//			}
			
			return result;

		} else {
			result = getMibDetailsLocal(mib);
		}

		return result;

	}

	/**
	 * 与えられたMIB名でMIB詳細情報を検索し、 マッチする要素のリストを返します。
	 * 
	 * @param mib
	 * @return
	 */
	private ArrayList<SnmpTrapMasterInfo> getMibDetailsLocal(String mib) {

		ArrayList<SnmpTrapMasterInfo> foundList = new ArrayList<SnmpTrapMasterInfo>();

		for (int i = 0; i < mibDetailList.size(); i++) {
			if (mibDetailList.get(i).getMib().equals(mib)) {
				foundList.add(mibDetailList.get(i));
			}
		}

		return foundList;
	}

	/**
	 * 
	 * @param detailList
	 * @return
	 * @throws InvocationTargetException 
	 */
	public boolean addMibDetails(ArrayList<SnmpTrapMasterInfo> detailList) throws InvocationTargetException {

		if (this.isSync) {
			// このルートは利用されないはず
			int success = 0;

			for (int i = 0; i < detailList.size(); i++) {
				if (addMibDetail(detailList.get(i))) {
					success++;
				}
			}

			// 全部成功したらtrue、1個でも失敗したらfalse
			if (success == detailList.size()) {
				return true;
			} else {
				return false;
			}
		} else {
			// 重複チェックは行わずリストの最後尾に追加する
			this.mibDetailList.addAll(detailList);
			return true;
		}
	}

	/**
	 * 
	 * @param detail
	 * @return
	 * @throws InvocationTargetException 
	 */
	public boolean addMibDetail(SnmpTrapMasterInfo detail) throws InvocationTargetException {

		if (this.isSync) {
			boolean result = false;

			/*
			 * 最初に一覧情報更新
			 */

			// マネージャからMIB一覧情報検索
			// →無ければローカルのMIB一覧情報を登録
			// →それでも無ければMIB詳細に含まれるMIB名から生成して登録
			SnmpTrapMibMasterData master = null;
			try{
				master = getMibMaster(detail.getMib());
			} catch (InvocationTargetException e) {
				// do nothing
			} 

			//mibが無い場合は本来はmasterはnullとなる。
			//Ver4.0.0のエンドポイント実装不備でデフォルトコンストラクタで初期化された
			//SnmpTrapMibMasterDataがmasterに設定される。
			//デフォルトコンストラクタではmib=nullとなる。空文字チェックは念のため。
			if (master == null || master.getMib() == null || master.getMib().equals("")) {

				master = getMibMasterLocal(detail.getMib());

				if (master == null) {

					master = new SnmpTrapMibMasterData();
					master.setMib(detail.getMib());
					master.setOrderNo(SnmpTrapMibMstConstant.ENTERPRISE_MIB_ORDER_NO);
					master
							.setDescription(Messages
									.getString("traputil.import.mib.description.default"));

				}

				if (!addMibMaster(master)) {
					return false;
				}

			}

//			try {
//				MibManagerLib lib = new MibManagerLib();
//				result = (Boolean)lib.addMibDetail(detail, uid, pw, url);
//			} catch (IllegalArgumentException e) {
//				log.error(e.getMessage(), e);
//			} catch (SecurityException e) {
//				log.error(e.getMessage(), e);
//			} catch (HinemosUnknown_Exception e) {
//				log.error(e.getMessage(), e);
//			} catch (InvalidRole_Exception e) {
//				log.error(e.getMessage(), e);
//			} catch (InvalidUserPass_Exception e) {
//				log.error(e.getMessage(), e);
//			} catch (MonitorNotFound_Exception e) {
//				log.error(e.getMessage(), e);
//			}
			
			return result;

		} else {
			// 重複チェックは行わずリストの最後尾に追加する
			this.mibDetailList.add(detail);
			return true;
		}
	}

	/**
	 * 
	 * @param list
	 * @throws InvocationTargetException 
	 */
	public void modifyMibDetails(ArrayList<SnmpTrapMasterInfo> list) throws InvocationTargetException {
		if (list != null && list.size() != 0) {
			for (int i = 0; i < list.size(); i++) {
				modifyMibDetail(list.get(i));
			}
		}
	}

	/**
	 * 
	 * @param detail
	 * @throws InvocationTargetException 
	 */
	public boolean modifyMibDetail(SnmpTrapMasterInfo detail) throws InvocationTargetException {

		boolean result = false;

		if (this.isSync) {

//			try {
//				MibManagerLib lib = new MibManagerLib();
//				result = (Boolean)lib.modifyMibDetail(detail, uid, pw, url);
//			} catch (IllegalArgumentException e) {
//				log.error(e.getMessage(), e);
//			} catch (SecurityException e) {
//				log.error(e.getMessage(), e);
//			} catch (HinemosUnknown_Exception e) {
//				log.error(e.getMessage(), e);
//			} catch (InvalidRole_Exception e) {
//				log.error(e.getMessage(), e);
//			} catch (InvalidUserPass_Exception e) {
//				log.error(e.getMessage(), e);
//			} catch (MonitorNotFound_Exception e) {
//				log.error(e.getMessage(), e);
//			}

			return result;

		} else {
			// ローカルデータの変更処理は呼ばれないはず

			// detailで検索
			int idx = findMibDetailLocal(detail);

			// 見つからなかった
			if (idx < 0) {
				// 何もしない
				result = false;
			} else {
				this.mibDetailList.set(idx, detail);
				result = true;
			}

		}

		return result;
	}

	/**
	 * 
	 * @param detailList
	 * @return
	 * @throws InvocationTargetException 
	 */
	public ArrayList<String> deleteMibDetails(
			ArrayList<SnmpTrapMasterInfo> detailList) throws InvocationTargetException {
		ArrayList<String> mibs = new ArrayList<String>();
		String mib = null;

		if (detailList != null && detailList.size() != 0) {
			for (int i = 0; i < detailList.size(); i++) {
				mib = deleteMibDetail(detailList.get(i));
				if (!mibs.contains(mib)){
					// 削除対象MIB名リストに追加
					mibs.add(mib);
				}

			}
			
			for(String deleteMib : mibs){
				// MIB詳細情報が全てなくなった場合、MIB一覧情報も削除
				if (getMibDetails(deleteMib).size() == 0) {
					deleteMibMaster(deleteMib);
				}
			}
		}

		if(mibs.size() == 0){
			mibs = null;
		}
		
		// MIB名一覧
		// 削除されなければnull
		// 削除されればMIB名のリスト
		return mibs;
	}

	/**
	 * 
	 * @param detail
	 * @return
	 * @throws InvocationTargetException 
	 */
	public String deleteMibDetail(SnmpTrapMasterInfo detail) throws InvocationTargetException {

		String mib = detail.getMib();

		if (this.isSync) {

			String result = "";
//			try {
//				MibManagerLib lib = new MibManagerLib();
//				result = (String)lib.deleteMibDetail(detail, uid, pw, url);
//			} catch (IllegalArgumentException e) {
//				log.error(e.getMessage(), e);
//			} catch (SecurityException e) {
//				log.error(e.getMessage(), e);
//			} catch (HinemosUnknown_Exception e) {
//				log.error(e.getMessage(), e);
//			} catch (InvalidRole_Exception e) {
//				log.error(e.getMessage(), e);
//			} catch (InvalidUserPass_Exception e) {
//				log.error(e.getMessage(), e);
//			} catch (MonitorNotFound_Exception e) {
//				log.error(e.getMessage(), e);
//			}

			return result;

		} else {

			// detailで検索
			int idx = findMibDetailLocal(detail);

			// 見つからなかった
			if (idx < 0) {
				// 何もしない
			} else {
				this.mibDetailList.remove(idx);
			}
			return mib;
		}
	}

	private SnmpTrapMibMasterData getMibMasterLocal(String mib) {
		SnmpTrapMibMasterData master = null;
		Iterator<SnmpTrapMibMasterData> it = this.mibMasterList.iterator();

		// 引数のMIB名でマッチング
		while (it.hasNext()) {
			master = (SnmpTrapMibMasterData) it.next();
			if (master.getMib().equals(mib)) {
				// 最初の1個でリターン(PKなのでこれでOK)
				return master;
			}
		}

		// 見つからなかった
		return null;
	}

	/**
	 * 与えられたMIB詳細情報のPKとクライアント内のMIB詳細情報リストの マッチする要素のインデックスを返します
	 * 
	 * @param detail
	 * @return idx
	 */
	private int findMibDetailLocal(SnmpTrapMasterInfo detail) {
		int idx = 0;
		SnmpTrapMasterInfo found = null;

		if (this.mibDetailList != null && this.mibDetailList.size() != 0) {
			for (idx = 0; idx < this.mibDetailList.size(); idx++) {
				found = this.mibDetailList.get(idx);

				// PK: MIB, trap_oid, specific_id, generic_id
				if (detail.getMib().equals(found.getMib())
						&& detail.getTrapOid().equals(found.getTrapOid())
						&& detail.getSpecificId() == found.getSpecificId()
						&& detail.getGenericId() == found.getGenericId()) {

					break;
				}
			}
			
			if (found == null || idx == this.mibDetailList.size()) {
				return -1;
			} else {
				return idx;
			}
		}
		return -1;
	}

	/**
	 * このオブジェクトに保持される全ての情報を初期化します。
	 * 
	 */
	public void clearMibData() {
		this.mibDetailList.clear();
		this.mibMasterList.clear();

	}

	/**
	 * このオブジェクトのマネージャとの同期モードを返します。
	 * 
	 * @return
	 */
	public boolean isSync() {
		return isSync;
	}

	/**
	 * このオブジェクトのマネージャとの同期モードを設定します。
	 * 
	 * @param isSync
	 */
	public void setSync(boolean isSync) {
		this.isSync = isSync;
	}
}