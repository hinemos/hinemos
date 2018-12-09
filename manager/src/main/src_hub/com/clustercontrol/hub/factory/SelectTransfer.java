/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.factory;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.clustercontrol.hub.bean.TransferDestTypePropMst;
import com.clustercontrol.hub.bean.TransferInfoDestTypeMst;
import com.clustercontrol.hub.model.TransferInfo;
import com.clustercontrol.hub.session.HubControllerBean;
import com.clustercontrol.hub.session.TransferFactory;
import com.clustercontrol.hub.session.TransferFactory.Property;
import com.clustercontrol.hub.util.QueryUtil;

/**
 * 収集蓄積[転送]設定情報を検索するクラス<BR>
 * <p>
 * @version 6.0.0
 * @since 6.0.0
 */
public class SelectTransfer {

	/** ログ出力のインスタンス。 */
	private static Logger logger = Logger.getLogger( SelectTransfer.class );

	/**
	 * 受け渡し設定ID一覧を取得します。<BR>
	 * 
	 * @return 受け渡し設定ID一覧
	 */
	public List<String> getTransferIdList(String ownerRoleId) {
		logger.debug("getLogTransferIdList");
		List<String> list = new ArrayList<String>();
		List<TransferInfo> ct = getTransferListByOwnerRole(ownerRoleId);
		for (TransferInfo transfer : ct) {
			list.add(transfer.getTransferId());
		}
		return list;
	}
	
	/**
	 * 受け渡し設定情報を取得します。
	 * @param transferId
	 * @return
	 * @throws Exception
	 */
	public TransferInfo getTransferInfo(String transferId) throws Exception{
		logger.debug("getLogTransfer");
		if (transferId == null || transferId.isEmpty())
			return null;
		return QueryUtil.getTransferInfo(transferId);
	}

	/**
	 * オーナーロールIDを条件として
	 * 受け渡し設定情報一覧を取得します。
	 * 
	 * @return 受け渡し設定情報のリスト
	 */
	public List<TransferInfo> getTransferListByOwnerRole(String ownerRoleId) {
		logger.debug("getLogTransferListByOwnerRole");
		return QueryUtil.getTransferInfoList_OR(ownerRoleId);
	}

	/**
	 * 受け渡し設定情報一覧を取得します。
	 * 
	 * @return 受け渡し設定情報のリスト
	 */
	public List<TransferInfo> getTransferInfoList() {
		logger.debug("getLogTransferList");
		return QueryUtil.getTransferInfoList();
	}

	/**
	 * 
	 * @return
	 */
	public List<TransferInfoDestTypeMst> getTransferInfoDestTypeMstList() {
		logger.debug("getLogTransferDestTypeMstList");
		
		List<TransferInfoDestTypeMst> msts = new ArrayList<>();
		for (TransferFactory factory: HubControllerBean.getTransferFactoryList().values()) {
			TransferInfoDestTypeMst mst = new TransferInfoDestTypeMst();
			mst.setDestTypeId(factory.getDestTypeId());
			mst.setName(factory.getName());
			mst.setDescription(factory.getDescription());
			
			List<TransferDestTypePropMst> props = new ArrayList<>();
			for (Property p: factory.properties()) {
				TransferDestTypePropMst prop = new TransferDestTypePropMst();
				prop.setName(p.name);
				prop.setDescription(p.description);
				prop.setValue(p.defaultValue.toString());
				
				props.add(prop);
			}
			mst.setDestTypePropMsts(props);
			
			msts.add(mst);
		}
		return msts;
	}
}