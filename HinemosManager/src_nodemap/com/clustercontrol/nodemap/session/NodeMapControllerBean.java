/*
Copyright (C) 2010 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.
 */

package com.clustercontrol.nodemap.session;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.util.ObjectPrivilegeUtil;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.BgFileNotFound;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.IconFileNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.NodeMapNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.nodemap.NodeMapException;
import com.clustercontrol.nodemap.bean.Association;
import com.clustercontrol.nodemap.bean.FacilityElement;
import com.clustercontrol.nodemap.bean.NodeElement;
import com.clustercontrol.nodemap.bean.NodeMapModel;
import com.clustercontrol.nodemap.bean.ReservedFacilityIdConstant;
import com.clustercontrol.nodemap.bean.ScopeElement;
import com.clustercontrol.nodemap.model.MapAssociationEntity;
import com.clustercontrol.nodemap.model.MapAssociationEntityPK;
import com.clustercontrol.nodemap.model.MapBgImageEntity;
import com.clustercontrol.nodemap.model.MapIconImageEntity;
import com.clustercontrol.nodemap.model.MapInfoEntity;
import com.clustercontrol.nodemap.model.MapPositionEntity;
import com.clustercontrol.nodemap.model.MapPositionEntityPK;
import com.clustercontrol.nodemap.util.QueryUtil;
import com.clustercontrol.nodemap.util.SearchConnectionExecutor;
import com.clustercontrol.nodemap.util.SearchConnectionProperties;
import com.clustercontrol.ping.bean.PingResult;
import com.clustercontrol.ping.bean.PingRunCountConstant;
import com.clustercontrol.ping.bean.PingRunIntervalConstant;
import com.clustercontrol.ping.factory.RunMonitorPing;
import com.clustercontrol.ping.util.ReachAddressFping;
import com.clustercontrol.poller.impl.Snmp4jPollerImpl;
import com.clustercontrol.poller.util.DataTable;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.bean.FacilityTreeItem;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Messages;

/**
 *
 * <!-- begin-user-doc --> マップ情報の制御を行うsession bean <!-- end-user-doc --> *
 *
 */
public class NodeMapControllerBean {
	private static Log m_log = LogFactory.getLog( NodeMapControllerBean.class );
	private final int MAX_FILENAME = 64;

	/**
	 * リポジトリ情報からマップのデータを生成します。<BR>
	 * 
	 * @param parentFacilityId 描画対象スコープの親のファシリティID
	 * @param facilityId 描画対象スコープのファシリティID
	 * @return マップ描画用データ
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * @see com.clustercontrol.nodemap.model.NodeMapModel
	 */
	public NodeMapModel createNodeMapModel(String facilityId) throws InvalidRole, HinemosUnknown {
		m_log.debug("createNodeMap() start : " + facilityId);

		JpaTransactionManager jtm = null;

		NodeMapModel map = null;

		// リポジトリからスコープの情報を取得する
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// リポジトリからスコープのパスを取得する
			String facilityPath = new RepositoryControllerBean().getFacilityPath(facilityId, null);
			m_log.debug("facilityPath = " + facilityPath);

			// リポジトリからスコープのファシリティ名を取得する
			String facilityName = "";
			String ownerRoleId = null;
			if(ReservedFacilityIdConstant.ROOT_SCOPE.equals(facilityId)){
				facilityName = "";
			} else {
				FacilityInfo info = FacilityTreeCache.getFacilityInfo(facilityId);
				if (info == null) {
					m_log.warn("cannot find " + facilityId);
					throw new FacilityNotFound("cannot find " + facilityId);
				}
				m_log.debug("FacilityInfo = " + info);
				facilityName = info.getFacilityName();
				ownerRoleId = info.getOwnerRoleId();
			}
			m_log.debug("facilityName = " + facilityName);

			String parentFacilityId = null;
			// 親のファシリティのIDを取得する
			if(ReservedFacilityIdConstant.ROOT_SCOPE.equals(facilityId)){ // トップスコープの場合は、親はなし
				parentFacilityId = null;
			} else {
				m_log.debug("getParentId = " + facilityId + "," + ReservedFacilityIdConstant.ROOT_SCOPE);
				parentFacilityId = getParentId(facilityId);
				m_log.debug("parentFacilityId = " + parentFacilityId);
			}

			boolean builtin = false;
			// 組み込みスコープか否かを取得する
			if(ReservedFacilityIdConstant.ROOT_SCOPE.equals(facilityId)){
				// トップスコープは組み込みスコープ扱い
				builtin = true;
			} else {
				builtin = FacilityTreeCache.getFacilityInfo(facilityId).getBuiltInFlg();
				m_log.debug("builtin = " + builtin);
			}

			// 新規にマップを生成する
			map = new NodeMapModel(parentFacilityId, facilityId, facilityName, facilityPath, ownerRoleId, builtin);
			jtm.commit();
		} catch (FacilityNotFound e) {
			jtm.rollback();
			return new NodeMapModel("", "", "Could not create a map.", "-", "", false);
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("createNodeMapModel() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		m_log.debug("createNodeMapModel() end");
		return map;
	}

	/**
	 * マップのデータをDBに登録します。<BR>
	 * 
	 * @param facilityId 描画対象スコープのファシリティID
	 * @return マップ描画用データ
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * @see com.clustercontrol.nodemap.model.NodeMapModel
	 */
	public void registerNodeMapModel(NodeMapModel map) throws InvalidRole, HinemosUnknown {

		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			HinemosEntityManager em = jtm.getEntityManager();

			// オブジェクト権限チェック
			ObjectPrivilegeUtil.getObjectPrivilegeObject(HinemosModuleConstant.PLATFORM_REPOSITORY, map.getMapId(), ObjectPrivilegeMode.MODIFY);

			// 登録時は、該当のマップの関係（コネクション）と要素を全て削除した後、登録する。
			try {
				// 関係を削除
				List<MapAssociationEntity> assoList = QueryUtil.getMapAssociationByMapId(map.getMapId());
				for(MapAssociationEntity bean : assoList){
					try {
						// 削除処理
						em.remove(bean);
					} catch (Exception e) {
						m_log.warn("registerNodeMapModel() MapAssociationEntity.remove : "
								+ "[mapId, source, target] = "
								+ "[" + bean.getId().getMapId()
								+ ", " + bean.getId().getSource()
								+ ", " + bean.getId().getTarget() + "]"
								+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					}
				}
			} catch (Exception e) {
				m_log.warn("registerNodeMapModel() MapAssociationEntity.remove : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				throw new HinemosUnknown(e.getMessage(), e);
			}

			try {
				// 要素を削除
				MapInfoEntity mapInfoEntity = null;
				try {
					mapInfoEntity = QueryUtil.getMapInfoPK(map.getMapId());
				} catch (NodeMapNotFound e) {
				}
				if (mapInfoEntity != null && mapInfoEntity.getMapPositionEntities() != null) {
					Collection<MapPositionEntity> conList = mapInfoEntity.getMapPositionEntities();
					for(MapPositionEntity bean : conList){
						try {
							// 削除処理
							em.remove(bean);
						} catch (Exception e) {
							m_log.warn("registerNodeMapModel() MapPositionEntity.remove : "
									+ "[mapId, elementId] = "
									+ "[" + bean.getId().getMapId() + ", " + bean.getId().getElementId() + "]"
									+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
						}
					}
					mapInfoEntity.setMapPositionEntities(null);
				}
			} catch (Exception e) {
				m_log.warn("registerNodeMapModel() MapInfoEntity.remove : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				throw new HinemosUnknown(e.getMessage(), e);
			}

			// JPAではDML処理順序が保障されないため、フラッシュ実行
			jtm.flush();

			try {
				// ファシリティIDをマップIDとしてマップ情報を取得する
				MapInfoEntity bean = null;
				MapBgImageEntity mapBgImageEntity = null;
				try {
					mapBgImageEntity = QueryUtil.getMapBgImagePK(map.getBgName());
				} catch (BgFileNotFound e) {
				}
				try {
					bean = QueryUtil.getMapInfoPK(map.getMapId());
					bean.relateToMapBgImageEntity(mapBgImageEntity);
				} catch (NodeMapNotFound e) {
					// 無い場合は生成
					bean = new MapInfoEntity(map.getMapId(), mapBgImageEntity);
				}
			} catch (EntityExistsException e) {
				throw new HinemosUnknown(e.getMessage(), e);
			} catch (Exception e) {
				m_log.warn("registerNodeMapModel() MapInfoEntity.create : "
						+ "mapId = " + map.getMapId() + " "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				throw new HinemosUnknown(e.getMessage(), e);
			}

			// エレメントを更新
			FacilityElement[] elemet = map.getContentArray();
			for(int i=0; i<elemet.length; i++){
				MapPositionEntity posi = null;
				MapPositionEntityPK posiPk = null;
				try {
					posiPk = new MapPositionEntityPK(map.getMapId(), elemet[i].getFacilityId());
					try {
						posi = QueryUtil.getMapPositionPK(posiPk);
					} catch (NodeMapNotFound e) {
						// 無い場合は新たに生成
						posi = new MapPositionEntity(posiPk, null);
					}
					posi.setX(elemet[i].getX());
					posi.setY(elemet[i].getY());
				} catch (EntityExistsException e) {
					throw new HinemosUnknown(e.getMessage(), e);
				} catch (Exception e) {
					m_log.warn("registerNodeMapModel() MapPositionEntity.create() : "
							+ (posiPk != null ? posiPk.toString() : map.getMapId()) + " "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw new HinemosUnknown(e.getMessage(), e);
				}
			}
			jtm.flush();

			// 関連を更新
			if (map.getAssociations() != null) {
				for (Association asso : map.getAssociations()) {
					MapAssociationEntityPK beanPk = null;
					try {
						beanPk = new MapAssociationEntityPK(map.getMapId(), asso.getSource(), asso.getTarget());
						try {
							QueryUtil.getMapAssociationPK(beanPk);
						} catch (NodeMapNotFound e) {
							// MapAssociationEntity永続化
							new MapAssociationEntity(beanPk);
						}
					} catch (EntityExistsException e) {
						throw new HinemosUnknown(e.getMessage(), e);
					} catch (Exception e) {
						m_log.warn("registerNodeMapModel() MapPositionEntity.create : "
								+ (beanPk != null ? beanPk.toString() : map.getMapId()) + " "
								+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
						throw new HinemosUnknown(e.getMessage(), e);
					}
				}
			}

			// アイコンを更新
			for (FacilityElement element : map.getContentArray()) {
				String facilityId = element.getFacilityId();
				String iconImage = element.getIconImage();
				if (iconImage == null) {
					continue;
				}
				FacilityInfo bean = null;
				try {
					bean = new RepositoryControllerBean().getFacilityEntityByPK(facilityId);
				} catch (FacilityNotFound e) {
					// スコープの場合はこのルートを通る。
					continue;
				} catch (InvalidRole e) {
					m_log.info("registerNodeMapModel() facilityId = " + facilityId + " " + e.getMessage());
					if (FacilityTreeCache.getFacilityInfo(facilityId).getIconImage().equals(iconImage)) {
						// アイコンに変更がない場合は何もしない
						continue;
					} else {
						// アイコンに変更がある場合は例外を投げる
						throw e;
					}
				} catch (HinemosUnknown e) {
					throw e;
				} catch (Exception e) {
					m_log.warn("registerNodeMapModel() NodeEntity.findByPrimaryKey : "
							+ "facilityId = " + facilityId + " "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw new HinemosUnknown(e.getMessage(), e);
				}
				if (!iconImage.equals(bean.getIconImage())) {
					bean.setIconImage(iconImage);
				}
			}
			jtm.commit();

			// キャッシュをリフレッシュする
			FacilityTreeCache.refresh();
		} catch (InvalidRole | HinemosUnknown e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getFacilityPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("registerNodeMapModel() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 * マップのデータを取得します。<BR>
	 * 
	 * @param facilityId 描画対象スコープのファシリティID
	 * @return マップ描画用データ
	 * @throws NodeMapException
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * @see com.clustercontrol.nodemap.model.NodeMapModel
	 */
	public NodeMapModel getNodeMapModel(String facilityId) throws HinemosUnknown, InvalidRole, NodeMapException {
		m_log.debug("getNodeMap() start");

		JpaTransactionManager jtm = null;
		NodeMapModel map = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// facilityIdが履歴には存在するが、リポジトリに存在しない場合をチェック。
			try {
				if (!ReservedFacilityIdConstant.ROOT_SCOPE.equals(facilityId)) {
					if (FacilityTreeCache.getFacilityInfo(facilityId) == null) {
						throw new FacilityNotFound("cannot find " + facilityId);
					}
				}
			} catch (FacilityNotFound e) {
				throw new NodeMapException (e.getMessage(), e);
			} catch (Exception e) {
				m_log.warn("getNodeMapModel() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				throw new HinemosUnknown(e.getMessage(), e);
			}

			// リポジトリ情報をもとにマップを生成
			map = createNodeMapModel(facilityId);
			String mapId = facilityId;

			m_log.debug("MapId = " + mapId);

			MapInfoEntity bean = null;
			try {
				// ファシリティIDをマップIDとしてマップ情報を取得する
				try {
					bean = QueryUtil.getMapInfoPK(mapId);
				} catch (NodeMapNotFound e) {
				}
				if (bean == null) {
					try {
						// マップ情報がまだ生成されていない場合
						// 指定のスコープ配下のスコープおよびノードの一覧を取得する
						ArrayList<String> facilityIdList
						= new RepositoryControllerBean().getFacilityIdList(facilityId, RepositoryControllerBean.ONE_LEVEL, true);
						for (String fid : facilityIdList) {
							if (fid.equals(facilityId)) {
								// スコープ配下のファシリティIDに、そのスコープ自身も含まれるので、それをスキップする
								continue;
							}
							try {
								// エレメントを生成
								FacilityElement element = createElementForRepository(facilityId, fid);
								// 座標未登録ノードを追加
								map.addContent(element);
							} catch (InvalidRole e) {
								// 何もしない
							}
						}
					}catch (HinemosUnknown e) {
						throw e;
					}catch (Exception e) {
						m_log.warn("getNodeMapModel() : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
						throw new HinemosUnknown(e.getMessage(), e);
					}
					return map;
				}
			} catch (HinemosUnknown e) {
				throw e;
			} catch (Exception e) {
				m_log.warn("getNodeMapModel() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				throw new HinemosUnknown(e.getMessage(), e);
			}

			// 背景イメージ名を設定する
			map.setBgName(
					bean.getMapBgImageEntity() == null ? null : bean.getMapBgImageEntity().getFilename());

			ArrayList<String> childList = null;
			try {
				childList = new RepositoryControllerBean().getFacilityIdList(facilityId, RepositoryControllerBean.ONE_LEVEL, true);
			} catch (HinemosUnknown e) {
				throw e;
			} catch (Exception e) {
				m_log.warn("getNodeMapModel() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				throw new HinemosUnknown(e.getMessage(), e);
			}

			for (String childId : childList) {
				if (childId.equals(facilityId)) {
					// スコープ配下のファシリティIDに、そのスコープ自身も含まれるので、それをスキップする
					continue;
				}
				String parentId = facilityId;

				// 対応するファシリティIDの座標情報がDBにあるか否かを調べる
				MapPositionEntity elementPosition = null;
				try {
					try {
						elementPosition = QueryUtil.getMapPositionPK(mapId, childId);
					} catch (NodeMapNotFound e) {
					}
					if (elementPosition == null) {
						try {
							// DBに対象のファシリティの図の情報が無い場合の処理
							// エレメントを生成
							FacilityElement element = createElementForRepository(facilityId, childId);

							// 座標未登録ノードを追加
							map.addContent(element);
						} catch (FacilityNotFound | InvalidRole e) {
							// 何もしない。
						} catch (HinemosUnknown e) {
							throw e;
						} catch (Exception e) {
							m_log.warn("getNodeMapModel() : "
									+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
							throw new HinemosUnknown(e.getMessage(), e);
						}

					} else {
						// リポジトリの情報を元にエレメントを生成
						FacilityElement element = createElementForRepository(parentId, childId);
						element.setPosition(elementPosition.getX(), elementPosition.getY());
						// 座標保持ノードに追加
						map.addContent(element);
					}
				} catch (InvalidRole e) {
					// 何もしない。
				} catch (HinemosUnknown e) {
					throw e;
				} catch (FacilityNotFound e) {
					throw new HinemosUnknown(e.getMessage(), e);
				} catch (Exception e) {
					m_log.warn("getNodeMapModel() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw new HinemosUnknown(e.getMessage(), e);
				}
			}

			// 関連情報を取得して設定
			Collection<MapAssociationEntity> associations = null;
			m_log.debug("set association");
			try {
				associations = QueryUtil.getMapAssociationByMapId(mapId);
				for (MapAssociationEntity assoBean : associations) {
					String src = assoBean.getId().getSource();
					String trg = assoBean.getId().getTarget();

					// src も trg も両方存在する場合のみマップのモデルに追加する
					if(map.getElement(src) != null && map.getElement(trg) != null){
						Association asso = new Association(src, trg);
						map.addAssociation(asso);
					}
				}
			} catch (Exception e) {
				m_log.warn("getNodeMapModel() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}
			jtm.commit();
		} catch (HinemosUnknown | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.warn("getNodeMapModel() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return map;
	}

	/**
	 * 背景データを取得します。<BR>
	 * 
	 * @param filename
	 * @return filedata
	 * @throws BgFileNotFound
	 * @throws HinemosUnknown
	 */
	public byte[] getBgImage(String filename) throws HinemosUnknown, BgFileNotFound {

		JpaTransactionManager jtm = null;

		// 背景のバイナリデータを取得
		byte[] filedata = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			MapBgImageEntity bean = QueryUtil.getMapBgImagePK(filename);
			filedata = bean.getFiledata();
			jtm.commit();
		} catch (BgFileNotFound e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("getBgImage() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return filedata;
	}


	/**
	 * 背景データをDBに登録します。<BR>
	 * 
	 * @param filename
	 * @param filedata
	 * @throws HinemosUnknown
	 * @throws NodeMapException
	 */
	public void setBgImage(String filename, byte[] filedata) throws HinemosUnknown, NodeMapException {

		JpaTransactionManager jtm = null;
		MapBgImageEntity entity = null;

		if (filename != null && filename.length() > MAX_FILENAME) {
			String[] args = { Integer.toString(MAX_FILENAME) };
			NodeMapException e =  new NodeMapException(
					Messages.getString("NODEMAP_FILE_NAME_TOO_LONG", args));
			m_log.info("setBgImage() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 上書き
			try {
				entity = QueryUtil.getMapBgImagePK(filename);
			} catch (BgFileNotFound e) {
				// MapBgImageEntity永続化
				entity = new MapBgImageEntity(filename);
			}
			entity.setFiledata(filedata);

			jtm.commit();
		} catch (EntityExistsException e) {
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} catch (Exception e) {
			m_log.warn("setBgImage() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 * 背景画像のファイル名一覧を取得します。<BR>
	 * 
	 * @ejb.interface-method
	 * 
	 * @return Collection<String>
	 * @throws HinemosUnknown
	 */
	public Collection<String> getBgImagePK() throws HinemosUnknown {

		JpaTransactionManager jtm = null;

		// 背景のPKを取得
		ArrayList<String> filenameArray = new ArrayList<String>();
		Collection<MapBgImageEntity> beanCollection = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			beanCollection = QueryUtil.getAllMapBgImage();
			for (MapBgImageEntity bean : beanCollection) {
				filenameArray.add(bean.getFilename());
			}
			Collections.sort(filenameArray);
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getBgImagePK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return filenameArray;
	}

	/**
	 * 背景画像のファイル名の存在有無を取得します。<BR>
	 * 
	 * @return boolean
	 * @throws HinemosUnknown
	 */
	public boolean isBgImage(String filename) throws HinemosUnknown {

		JpaTransactionManager jtm = null;
		boolean rtnflg = true;

		// 背景のバイナリデータを取得
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			try {
				QueryUtil.getMapBgImagePK(filename);
			} catch (BgFileNotFound e) {
				rtnflg = false;
			}
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("isBgImage() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return rtnflg;
	}

	/**
	 * アイコン画像を取得します。<BR>
	 * 
	 * @param filename
	 * @return filedata
	 * @throws HinemosUnknown
	 * @throws IconFileNotFound
	 */
	public byte[] getIconImage(String filename) throws HinemosUnknown, IconFileNotFound {

		JpaTransactionManager jtm = null;

		// 背景のバイナリデータを取得
		MapIconImageEntity bean = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			bean = QueryUtil.getMapIconImagePK(filename);
			jtm.commit();
		} catch (IconFileNotFound e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("getIconImage() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return bean.getFiledata();
	}

	/**
	 * アイコン画像をDBに登録します。<BR>
	 * 
	 * @param filename
	 * @param filedata
	 * @throws NodeMapException
	 * @throws HinemosUnknown
	 * 
	 */
	public void setIconImage(String filename, byte[] filedata) throws NodeMapException, HinemosUnknown {

		JpaTransactionManager jtm = null;

		if (filename != null && filename.length() > MAX_FILENAME) {
			String[] args = { Integer.toString(MAX_FILENAME) };
			NodeMapException e = new NodeMapException(
					Messages.getString("NODEMAP_FILE_NAME_TOO_LONG", args));
			m_log.info("setIconImage() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 上書き
			MapIconImageEntity bean = null;
			try {
				bean = QueryUtil.getMapIconImagePK(filename);
			} catch (IconFileNotFound e) {
				bean = new MapIconImageEntity(filename);
			}
			bean.setFiledata(filedata);

			jtm.commit();
		} catch (EntityExistsException e) {
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} catch (Exception e) {
			m_log.warn("setIconImage() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 * アイコンのファイル名一覧を取得します。<BR>
	 * 
	 * @return Collection<String>
	 * @throws HinemosUnknown
	 */
	public Collection<String> getIconImagePK() throws HinemosUnknown {

		JpaTransactionManager jtm = null;

		// アイコンのPKを取得
		ArrayList<String> filenameArray = new ArrayList<String>();
		Collection<MapIconImageEntity> beanCollection = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			//MapIconImageEntity.findAll
			beanCollection = QueryUtil.getAllMapIconImage();
			for (MapIconImageEntity bean : beanCollection) {
				filenameArray.add(bean.getFilename());
			}
			Collections.sort(filenameArray);

			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getIconImagePK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return filenameArray;
	}

	/**
	 * アイコンのファイル名の存在有無を取得します。<BR>
	 * 
	 * @return boolean
	 * @throws HinemosUnknown
	 */
	public boolean isIconImage(String filename) throws HinemosUnknown {

		JpaTransactionManager jtm = null;
		boolean rtnflg = true;

		// アイコンの存在有無を確認
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			try {
				QueryUtil.getMapIconImagePK(filename);
			} catch (IconFileNotFound e) {
				rtnflg = false;
			}
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("isIconImage() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return rtnflg;
	}


	/**
	 * 親を探す。
	 * 
	 * @param targetId 対象のスコープID
	 * @param serachId 探索しているスコープID
	 */
	private String getParentId(String targetId) {

		JpaTransactionManager jtm = null;

		if (ReservedFacilityIdConstant.ROOT_SCOPE.equals(targetId)) {
			return null;
		} else if (FacilityTreeAttributeConstant.REGISTERED_SCOPE.equals(targetId) ||
				FacilityTreeAttributeConstant.INTERNAL_SCOPE.equals(targetId)) {
			return ReservedFacilityIdConstant.ROOT_SCOPE;
		}

		String facilityId = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			Collection<FacilityInfo> parentBeanCol
			= com.clustercontrol.repository.util.QueryUtil.getParentFacilityEntity(targetId);

			/*
			 * スコープの親は必ず一個。
			 * 最上位スコープの一段下のみ、親はFacilityRelationLocalから取得できない。
			 */
			if (parentBeanCol.size() == 0) {
				return ReservedFacilityIdConstant.ROOT_SCOPE;
			}
			if (parentBeanCol.size() != 1) {
				m_log.info("getParentId (" + targetId + ") logic error " +
						parentBeanCol.size());
				return null;
			}
			FacilityInfo parentBean = (parentBeanCol.toArray(new FacilityInfo[0]))[0];
			facilityId = parentBean.getFacilityId();

			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getParentId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			return null;
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return facilityId;
	}

	private FacilityElement createElementForRepository(String parentId, String fid) throws HinemosUnknown, FacilityNotFound, InvalidRole {
		FacilityElement element;
		FacilityInfo facilityInfo = null;

		try {
			String userId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

			FacilityTreeItem item = FacilityTreeCache.getFacilityTreeByUserId(userId);
			if (!isExistTreeItem(item, fid)) {
				m_log.info("cannot refer facilityInfo, userId="+userId+", facilityId=" + fid);
				throw new InvalidRole("cannot refer facilityInfo, userId="+userId+", facilityId=" + fid);
			}

			facilityInfo = FacilityTreeCache.getFacilityInfo(fid);
			// 見つからなかったらFacilityNotFoundを投げる
			if (facilityInfo == null) {
				m_log.warn("createElementForRepository() : facility is not found. facilityId="+fid);
				throw new FacilityNotFound("createElementForRepository() : facility is not found. facilityId="+fid);
			}
		} catch (InvalidRole | FacilityNotFound e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("createElementForRepository() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(),e);
		}

		// アイコン名を取得
		String facilityName = facilityInfo.getFacilityName();
		String iconImage = facilityInfo.getIconImage();
		String ownerRoleId = facilityInfo.getOwnerRoleId();
		boolean builtin =facilityInfo.getBuiltInFlg();

		// スコープの場合
		if (facilityInfo.getFacilityType() == FacilityConstant.TYPE_SCOPE) {
			m_log.debug("Scope : " + facilityName + "(" + fid + ")");

			// Hinemos3.1ではスコープのiconImageはnullとなる。
			// Hinemos3.1では有効/無効フラグはスコープでは常に有効となる。
			element = new ScopeElement(parentId, fid, facilityName, iconImage, ownerRoleId, builtin, true);
		} else {  // ノードの場合
			m_log.debug("Node : " + facilityName + "(" + fid + ")");

			Integer ipAddressVersion = null;
			String ipAddressV4 = "-";
			String ipAddressV6 = "-";
			String nodeName = "-";

			NodeInfo nodeEntity = null;
			try {
				nodeEntity = new RepositoryControllerBean().getNodeEntityByPK(fid);
				ipAddressVersion = nodeEntity.getIpAddressVersion();
				ipAddressV4 = nodeEntity.getIpAddressV4();
				ipAddressV6 = nodeEntity.getIpAddressV6();
				nodeName = nodeEntity.getNodeName();
			} catch (InvalidRole e) {
				// 何もしない
			} catch (FacilityNotFound | HinemosUnknown e) {
				throw e;
			} catch (Exception e) {
				m_log.warn("createElementForRepository() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				throw new HinemosUnknown(e.getMessage(), e);
			}
			boolean valid = facilityInfo.getValid();
			iconImage = facilityInfo.getIconImage();
			element = new NodeElement(parentId, fid, facilityName, iconImage, ownerRoleId, builtin, valid);

			// 表示用の属性値を追加設定
			element.setAttributes("IpProtocolNumber", ipAddressVersion);
			element.setAttributes("IpNetworkNumber", ipAddressV4);
			element.setAttributes("IpNetworkNumberV6", ipAddressV6);
			element.setAttributes("NodeName", nodeName);
		}
		// 表示用の属性値を設定
		element.setAttributes("FacilityId", facilityInfo.getFacilityId());
		element.setAttributes("Description", facilityInfo.getDescription());

		return element;
	}

	public List<Association>getL2ConnectionMap(String scopeId) {
		List<Association> result = null;

		try {
			result = new SearchConnectionExecutor(scopeId, false).execute();
		} catch (Exception e) {
			m_log.error(e.getMessage());
		}

		return result;
	}

	public List<Association>getL3ConnectionMap(String scopeId) {
		List<Association> result = null;

		try {
			result = new SearchConnectionExecutor(scopeId, true).execute();
		} catch (Exception e) {
			m_log.error(e.getMessage());
		}

		return result;
	}

	// ツリーに指定したfacilityIdのfacilityInfoが存在するか再帰的に調べる
	private boolean isExistTreeItem(FacilityTreeItem item, String facilityId) {
		// 自身が一致するか
		if (item.getData().getFacilityId().equals(facilityId)) {
			return true;
		}

		// 一致する子が存在するか
		for (FacilityTreeItem child : item.getChildrenArray()) {
			if (isExistTreeItem(child, facilityId)) {
				return true;
			}
		}

		return false;
	}

	public static void main(String[] args) {
		Set<String> oidList = new HashSet<String>();
		oidList.add(SearchConnectionProperties.DEFAULT_OID_FDB);
		oidList.add(SearchConnectionProperties.DEFAULT_OID_ARP);
		DataTable tmpTable = Snmp4jPollerImpl.getInstance().polling(
				"172.26.98.5",
				161,
				1,
				"public",
				3,
				5000,
				oidList,
				null,
				null,
				null,
				null,
				null,
				null);

		for (String key : tmpTable.keySet()) {
			System.out.println("key:"+key+",value:"+tmpTable.getValue(key).getValue());
		}
	}
	/**
	 * 　fpingによるping数の取得を行います。<BR>
	 * @param facilityId Ping対象のファシリティID(スコープ)collectのfacilityIDとは異なる
	 * @return ping結果文字列のリスト
	 * @throws HinemosUnknown
	 * @throws NodeMapException 
	 */
	public List<String> pingToFacilityList(List<String> facilityList) throws HinemosUnknown, NodeMapException {

		String message = "";
		String messageOrg = "";
		//まずは、データを作ります。
		// hosts[] →　IPアドレスリスト(String の配列)
		// hostsv6[] → IPv6アドレスリスト(Stringの配列)
		// node    → IPアドレスとノード名のリスト
		// target  → nodoのリスト
		HashSet<String> hosts     = new HashSet<String>();
		HashSet<String> hostsv6   = new HashSet<String>();
		// ipとname
		Hashtable<String, List<String>> facilityNameTable = new Hashtable<>();

		String facilityId = null;
		int version = 4;
		String[] node;
		for(int index=0; index<facilityList.size(); index++){
			facilityId = facilityList.get(index);
			if(facilityId != null && !"".equals(facilityId)){
				node = new String[2];
				try{

					// ノードの属性取得
					NodeInfo info = new RepositoryControllerBean().getNode(facilityId);

					if(info.getIpAddressVersion() != null){
						version = info.getIpAddressVersion();
					}else{
						version = 4;
					}

					if(version == 6){

						InetAddress[] ip = InetAddress.getAllByName(info.getIpAddressV6());

						if(ip.length != 1){
							//IPアドレスをInetAddressクラスでデコードしているのに1つじゃないときは
							//UnnownHostExcption
							UnknownHostException e = new UnknownHostException();
							m_log.info("pingToFacilityList() : "
									+ e.getClass().getSimpleName() + ", " + e.getMessage());
							throw e;
						}

						node[0] = ip[0].getHostAddress();
						if(node[0] != null && !node[0].equals("")) {
							//IPアドレスをHashSetに入れていく。
							hostsv6.add(node[0]);
						}
					}else{
						node[0] = info.getIpAddressV4();
						if(node[0] != null && !node[0].equals("")){

							//IPアドレスをHashSetに入れていく。
							hosts.add(node[0]);
						}
					}
					if(node[0] != null && !node[0].equals("")){
						node[1] = info.getNodeName();
						//targetをつめていく。
						List<String> facilitys = facilityNameTable.get(node[0]);
						if (facilitys == null) {
							facilitys = new ArrayList<>();
						}
						facilitys.add(facilityId);
						facilityNameTable.put(node[0], facilitys);
					}
				}catch(FacilityNotFound e){
					message = MessageConstant.MESSAGE_COULD_NOT_GET_NODE_ATTRIBUTES_PING.getMessage()
							+ "," + facilityId;
					messageOrg = e.getMessage();
					throw new NodeMapException (message + ", " + messageOrg, e);
				} catch (UnknownHostException e) {
					// 何もしない
				}
			}
		}

		int runCount = 0;
		int runInterval = 0;
		int pingTimeout = 0;
		try {
			// 回数[回](default:1、1～9)
			String runCountKey = "nodemap.ping.runcount";
			runCount = HinemosPropertyUtil.getHinemosPropertyNum(
					runCountKey, Long.valueOf(PingRunCountConstant.TYPE_COUNT_01)).intValue();
			CommonValidator.validateInt(runCountKey, runCount, 1, 9);

			// 間隔[ms](default:1000、0～5000)
			String runIntervalKey = "nodemap.ping.runinterval";
			runInterval = HinemosPropertyUtil.getHinemosPropertyNum(
					runIntervalKey, Long.valueOf(PingRunIntervalConstant.TYPE_SEC_02)).intValue();
			CommonValidator.validateInt(runIntervalKey, runInterval, 0, 5  * 1000);
			
			// タイムアウト[ms](default:5000、1～3600000)
			String pintTimeoutKey = "nodemap.ping.timeout";
			pingTimeout = HinemosPropertyUtil.getHinemosPropertyNum(
					pintTimeoutKey, Long.valueOf(PingRunIntervalConstant.TYPE_SEC_05)).intValue();
			CommonValidator.validateInt(pintTimeoutKey, pingTimeout, 1, 60 * 60 * 1000);
		} catch (Exception e) {
			m_log.warn("pingToFacilityList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		ReachAddressFping reachabilityFping = new ReachAddressFping(runCount, runInterval, pingTimeout);

		boolean result = true;
		boolean resultTmp = true;
		ArrayList<String> msgErr = new ArrayList<>();
		ArrayList<String> msgErrV6 = new ArrayList<>();
		Hashtable<String, PingResult> fpingResultSet = new Hashtable<String, PingResult>();
		Hashtable<String, PingResult> fpingResultSetV6 = new Hashtable<String, PingResult>();
		
		RunMonitorPing monitorPing = new RunMonitorPing();
		//IPv4のホストが在ればfpingを利用して監視
		if(hosts.size() !=0 ) {
			result = reachabilityFping.isReachable(hosts , 4);
			msgErr = reachabilityFping.getM_errMsg();
		}
		//IPv6のホストが在ればfping6を利用して監視
		if(hostsv6.size() !=0 ) {
			resultTmp = reachabilityFping.isReachable(hostsv6 , 6);
			msgErrV6 = reachabilityFping.getM_errMsg();
		}

		if (!result || !resultTmp) {
			return null;
		}
		List<String> retList = new ArrayList<>();
		fpingResultSet = monitorPing.wrapUpFping(msgErr, runCount, 4);
		fpingResultSetV6 = monitorPing.wrapUpFping(msgErrV6, runCount, 6);
		//IPv4の情報が存在しない場合は、IPv6の情報で上書きする
		m_log.debug("pingToFacilityList(): before fpingResultSet check");
		if( fpingResultSet.size() == 0){
			m_log.debug("pingToFacilityList(): after fpingResultSet check");
			fpingResultSet = fpingResultSetV6;
		}
		//IPv4の情報が存在する場合は、IPv6の情報を追加する
		else if(fpingResultSetV6 .size()!= 0){
			fpingResultSet.putAll(fpingResultSetV6);
		}
		for (Map.Entry<String, List<String>> ipAdd : facilityNameTable.entrySet()) {
			PingResult pingResult = fpingResultSet.get(ipAdd.getKey());
			for (String facility : ipAdd.getValue()) {
				retList.add(facility + " : " + pingResult.getMesseageOrg());
			}
		}
		return retList;
	}
	
	/**
	 * 指定されたnodeListとmapIdにマッチするパスを削除します。<br>
	 * ノード削除時：mapListは指定されない(null)<br>
	 * スコープ削除時：nodeListは指定されない(null)、スコープ削除時はmapListがtargetまたはsourceになっている場合もあるので同様に削除する<br>
	 * スコープ割り当て解除時：nodeList、mapListが指定される<br>
	 * 
	 * @param nodeList
	 * @param mapList
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	private void deleteMapAssociation(List<String> nodeList, String mapId) throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		try {
			m_log.debug("mapId:" + mapId);
			if (nodeList != null) {
				m_log.debug("nodelist:" + nodeList.toString());
			}
			jtm = new JpaTransactionManager();
			jtm.begin();
			HinemosEntityManager em = jtm.getEntityManager();

			List<MapAssociationEntity> deleteList = new ArrayList<>();
			if (mapId == null || mapId.equals("")) {
				// ノード削除時、削除対象のレコードを取得
				deleteList = QueryUtil.getMapAssociationBySourceOrTarget(nodeList);
			} else {
				// スコープ削除 or スコープ割り当て解除時
				// mapIdに対応するレコードを取得
				deleteList.addAll(QueryUtil.getMapAssociationByMapId(mapId));
				if (nodeList == null) {
					// スコープ削除時はsourceまたはtargetにスコープが指定されているかもしれないので検索して消す
					List<String> mapList = new ArrayList<>();
					mapList.add(mapId);
					deleteList.addAll(QueryUtil.getMapAssociationBySourceOrTarget(mapList));
				}
			}
		
			for(MapAssociationEntity bean : deleteList){
				// 削除処理
				if (nodeList != null && !nodeList.isEmpty()) {
					for (String facilityId : nodeList) {
						if (bean.getId().getSource().equals(facilityId) 
								|| bean.getId().getTarget().equals(facilityId)) {
							em.remove(bean);
						}
					}
				} else {
					// スコープ削除時はファシリティに関係なく削除
					em.remove(bean);
				}
			}
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("deleteMapAssociation() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}	

	/**
	 * 指定されたnodeListとmapIdにマッチするポジションを削除します。<br>
	 * ノード削除時：mapIdは指定されない(null)<br>
	 * スコープ削除時：nodeListは指定されない(null)、スコープ削除時はmapIdがmapIdまたはelementIdになっている場合もあるので同様に削除する<br>
	 * スコープ割り当て解除時：nodeList、mapIdが指定される<br>
	 * 
	 * @param nodeList
	 * @param mapList
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	private void deleteMapPosition(List<String> nodeList, String mapId) throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		try {
			m_log.debug("mapId:" + mapId);
			if (nodeList != null) {
				m_log.debug("nodelist:" + nodeList.toString());
			}
			jtm = new JpaTransactionManager();
			jtm.begin();
			HinemosEntityManager em = jtm.getEntityManager();

			List<String> targetList = new ArrayList<>();
			if (nodeList != null && !nodeList.isEmpty()) {
				targetList.addAll(nodeList);
			}
			if (mapId != null && !mapId.equals("")) {
				targetList.add(mapId);
			}
			
			List<MapPositionEntity> deleteList = QueryUtil.getMapPositionByMapIdOrElementId(targetList, targetList);
		
			for(MapPositionEntity bean : deleteList){
				if (nodeList != null && !nodeList.isEmpty() && mapId != null && !mapId.equals("")) {
					for (String node : nodeList) {
						if (bean.getId().getMapId().equals(mapId) && bean.getId().getElementId().equals(node)) {
							em.remove(bean);
						}
					}
				} else {
					em.remove(bean);
				}
			}
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("deleteMapPosition() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 * 指定されたmapInfoに一致するマップ情報を削除します。<br>
	 * 
	 * @param mapId
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	private void deleteMapInfo(String mapId) throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		try {
			m_log.info("mapId:" + mapId);
			jtm = new JpaTransactionManager();
			jtm.begin();
			HinemosEntityManager em = jtm.getEntityManager();

			MapInfoEntity mapInfoEntity = QueryUtil.getMapInfoPK(mapId);
			em.remove(mapInfoEntity);
		
			jtm.commit();
		} catch (NodeMapNotFound e) {
			// mapIdに対応するmapInfoが必ず存在するわけではないのでエラーではない
			m_log.debug("deleteMapInfo() mapId Not Found. mapId:" + mapId);
		} catch (Exception e) {
			m_log.warn("deleteMapInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}
	
	public void deleteMapInfo(List<String> nodeList, String mapId) throws HinemosUnknown {
		deleteMapAssociation(nodeList, mapId);
		deleteMapPosition(nodeList, mapId);
		if (nodeList == null || nodeList.isEmpty()) {
			deleteMapInfo(mapId);
		}
	}
}
