/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.editpart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.FacilityElementResponse;
import org.openapitools.client.model.MapAssociationInfoResponse;
import org.openapitools.client.model.MapAssociationInfoResponse.TypeEnum;
import org.openapitools.client.model.NodeMapModelResponse;
import org.openapitools.client.model.RegisterNodeMapModelRequest;
import org.openapitools.client.model.ScopeDataInfoResponse;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.monitor.util.MonitorResultRestClientWrapper;
import com.clustercontrol.nodemap.figure.FacilityFigure;
import com.clustercontrol.nodemap.figure.NodeFigure;
import com.clustercontrol.nodemap.util.FacilityElementComparator;
import com.clustercontrol.nodemap.util.NodeMapRestClientWrapper;
import com.clustercontrol.nodemap.view.NodeMapView;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
/**
 * ノードマップビューをコントロールするクラス。
 * 表示はNodeMapViewクラス。
 * @since 1.0.0
 */
public class MapViewController {

	// ログ
	private static Log m_log = LogFactory.getLog( MapViewController.class );

	private final NodeMapView _view;
	private final String _secondaryId;
	private final String _currentScope;

	// 描画対象のマップの情報を保持したモデル
	private NodeMapModelResponse _map;

	private boolean openAccessInformation = false;

	// モデルと図の関係を保持するマップ
	// 描画対象スコープ、ノードのファシリティIDとそれを描画している図（Figure）のリファレンスを保持
	private ConcurrentHashMap<String, FacilityFigure> m_figureMap = new ConcurrentHashMap<String, FacilityFigure>();

	// モデルとコネクションの関係を保持するマップ
	private ConcurrentHashMap<MapAssociationInfoResponse, PolylineConnection> m_connectionMap =
		new ConcurrentHashMap<MapAssociationInfoResponse, PolylineConnection>();

	// ノードマップ検索用の順番付きファシリティIDリスト
	private ArrayList<String> m_findIdList = new ArrayList<String>(); 
	
	public MapViewController(NodeMapView view, String secondaryId, String currentScope){
		this._secondaryId = secondaryId;
		this._currentScope = currentScope;
		this._view = view;
	}

	/**
	 * マネージャから情報を取得しマップを更新する
	 * NodeMapView以外からは呼ぶべきではない
	 * @throws Exception
	 */
	public void updateMap(boolean statusFlg, boolean eventFlg) throws Exception {
		m_log.debug("status = " + statusFlg + ", event = " + eventFlg);
		m_figureMap.clear();
		m_connectionMap.clear();
		// マネージャからノード情報を取得
		try {
			NodeMapRestClientWrapper wrapper = NodeMapRestClientWrapper.getWrapper(_view.getCanvasComposite().getManagerName());
			_map = wrapper.getNodeMapModel(_currentScope);
			if (_map == null) {
				m_log.warn("updateMap(), updateMap _map is null");
				throw new Exception("updateMap _map is null");
			}
		} catch (Exception e) {
			throw e;
		}

		// ファシリティパスを表示
		_view.setMapName(HinemosMessage.replace(_map.getMapPath()), _map.getMapId());

		// マップの描画
		drawMap();

		// 重要度を図に反映
		updatePriority(statusFlg, eventFlg);

		_view.setEditing(false);
	}
	

	public void drawMap() throws Exception {
		// マップを削除
		_view.m_canvasComposite.clearCanvas();

		_view.m_canvasComposite.drawBgImage(_map.getBgName());

		// ノード（スコープ）を描画
		Map<String, FacilityElementResponse> contents = _map.getContents();
		ArrayList<FacilityElementResponse> facilityElementList = new ArrayList<FacilityElementResponse>();
		ArrayList<FacilityElementResponse> newcomers = new ArrayList<FacilityElementResponse>(); // 座標値を持たない要素を保持
		int maxY = 0;
		// findbugs対応 keySetをentrySetに変更
		for(Entry<String, FacilityElementResponse> rec : contents.entrySet()) {
			FacilityElementResponse element = rec.getValue();
			facilityElementList.add(element);
			if(element.getNewcomer() == false){
				// 座標値を持つ要素をマップに追加
				_view.m_canvasComposite.drawFigure(element);

				// y座標の最大値を求める
				if(element.getY() > maxY){
					maxY = element.getY();
				}
			} else {
				newcomers.add(element);
			}
		}

		// newcomersをソートする。
		Collections.sort(newcomers, new FacilityElementComparator());

		// 座標未登録のノード（スコープ）を自動配置
		int count=0;
		for(FacilityElementResponse element : newcomers){
			// 横に10個ずつ並べる
			int xIndex = count % 10;
			int yIndex = count / 10;

			// 座標を設定
			element.setX(xIndex * 100 + 40);
			element.setY(maxY + yIndex * 100 + 30);
			element.setNewcomer(false);

			// マップに追加
			_view.m_canvasComposite.drawFigure(element);
			count++;
		}

		// 関係を描画
		List<MapAssociationInfoResponse> associations = _map.getAssociations();
		for(MapAssociationInfoResponse association : associations){
			m_log.debug(association.getSource() + "->" + association.getTarget() + ": " + association.getType());
			_view.m_canvasComposite.drawConnection(association);
		}
		
		_view.m_canvasComposite.applyScale();

		// listCompositeの更新
		_view.m_listComposite.setTableList(
				new ArrayList<FacilityElementResponse>(facilityElementList));

		//マップ検索用リストの作成(スコープ->ノードの分類順 で 分類内はID順)
		m_findIdList = new ArrayList<String>();
		Collections.sort(facilityElementList, new FacilityElementComparator());
		for (FacilityElementResponse element : facilityElementList) {
			if (!(FacilityConstant.TYPE_NODE_STRING.equals(element.getTypeName()))){
				m_findIdList.add(element.getFacilityId());
			}
		}
		for (FacilityElementResponse element : facilityElementList) {
			if (FacilityConstant.TYPE_NODE_STRING.equals(element.getTypeName())){
				m_findIdList.add(element.getFacilityId());
			}
		}
		
		if (_view.isAdjust()) {
			_view.m_canvasComposite.zoomAdjust();
		}
	}

	/**
	 * マネージャから情報を取得し、重要度を更新する
	 */
	private void updatePriority(boolean statusFlg, boolean eventFlg) throws Exception {
		// 背景色変更が不要の場合はマネージャにアクセスしない
		if(statusFlg == false && eventFlg == false){
			return;
		}

		List<ScopeDataInfoResponse> infoList = null;
		try {
			/*
			 * (currentScope, statusFlag, eventFlag)
			 * ステータスのみ対象とする。
			 * 監視管理パースペクティブのスコープビューは、
			 * ステータスとイベントの両方を対象としている。
			 */
			MonitorResultRestClientWrapper wrapper = MonitorResultRestClientWrapper.getWrapper(_view.getCanvasComposite().getManagerName());
			infoList = wrapper.getScopeList(_currentScope, statusFlg, eventFlg, false);
		} catch (InvalidRole e) {
			// アクセス権なしの場合
			if (!openAccessInformation) {
				openAccessInformation = true;
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(null, Messages
						.getString("message"), Messages
						.getString("message.accesscontrol.16"));
			}
		} catch (Exception e) {
			m_log.warn("updatePriority(), " + e.getMessage(), e);
			throw e;
		}

		if (infoList != null) {
			for (ScopeDataInfoResponse data : infoList) {
				// ビューに設定
				_view.m_canvasComposite.setPriority(data.getFacilityId(), data.getPriority());
			}
		}
	}
	
	/**
	 * マネージャにアクセスせずに現在の重要度を反映します。
	 */
	public void updatePriorityNotManagerAccess() {
		Map<String, FacilityElementResponse> contents = _map.getContents();
		// findbugs対応 keySetをentrySetに変更
		for(Entry<String, FacilityElementResponse> rec: contents.entrySet()) {
			FacilityElementResponse element = rec.getValue();
			_view.m_canvasComposite.setPriorityNotManagerAccess(element.getFacilityId());
		}
	}

	// 関連を生成します。
	public MapAssociationInfoResponse createAssociation(String sourceFacilityId, String targetFacilityId){
		return createAssociation(sourceFacilityId, targetFacilityId, TypeEnum.NORMAL);
	}
	
	// 関連を生成します。
	public MapAssociationInfoResponse createAssociation(String sourceFacilityId, String targetFacilityId, TypeEnum type){
		if(sourceFacilityId != null && targetFacilityId != null){
			// 関連を生成
			MapAssociationInfoResponse association = new MapAssociationInfoResponse();
			association.setSource(sourceFacilityId);
			association.setTarget(targetFacilityId);
			association.setType(type);

			return association;
		}
		return null;
	}


	// 現在描画されているマップに関連を追加する
	public void addAssociation(String srcFacilityId, String targetFacilityId){
		MapAssociationInfoResponse association = createAssociation(srcFacilityId, targetFacilityId);

		if(association != null){
			List<MapAssociationInfoResponse> associations = _map.getAssociations();
			associations.add(association);

			// 画面に描画
			_view.m_canvasComposite.drawConnection(association);
		}
	}

	// トポロジ結線による結線差分表示用に関連を追加する
	public void autoAssociation(List<MapAssociationInfoResponse> list) {
		m_connectionMap.clear();
		List<MapAssociationInfoResponse> newAssociation = new ArrayList<MapAssociationInfoResponse>();
		for (MapAssociationInfoResponse oldAsso : _map.getAssociations()) {
			MapAssociationInfoResponse exist = null; //nullでなくなったら関連があったということを表す
			for (MapAssociationInfoResponse newAsso : list) {
				if ((oldAsso.getSource().equals(newAsso.getSource()) && oldAsso.getTarget().equals(newAsso.getTarget())) ||
						(oldAsso.getSource().equals(newAsso.getTarget()) && oldAsso.getTarget().equals(newAsso.getSource()))) {
					exist = newAsso;;
					break;
				}
			}
			
			if (exist != null) {
				// 前回と差分がない関連は残す
				
				if (oldAsso.getType() == TypeEnum.REMOVE) {
					// 前回点線の関連は通常に変更
					oldAsso.setType(TypeEnum.NORMAL);
				}
				newAssociation.add(oldAsso);
				list.remove(exist);
			} else {
				if ((getFacilityFigure(oldAsso.getSource()) instanceof NodeFigure ) && (getFacilityFigure(oldAsso.getTarget()) instanceof NodeFigure)) {
					// 前回から消えた関連は点線にする(両端がノードの場合のみ)
					newAssociation.add(createAssociation(oldAsso.getSource(), oldAsso.getTarget(), TypeEnum.REMOVE));
				} else {
					// スコープとの関連はそのまま残す
					newAssociation.add(oldAsso);
				}
			}
		}
		
		// listに残っている関連は今回新しく追加された関連なので太線にする
		for (MapAssociationInfoResponse asso : list) {
			if (asso.getSource().equals(asso.getTarget())) {
				// 関連の両端が同一だったら何もしない
				continue;
			}
			
			// 重複して新しい関連を複数追加しないようにチェックする
			boolean exist = false;
			for (MapAssociationInfoResponse newAsso : newAssociation) {
				if ((asso.getSource().equals(newAsso.getSource()) && asso.getTarget().equals(newAsso.getTarget())) ||
						(asso.getSource().equals(newAsso.getTarget()) && asso.getTarget().equals(newAsso.getSource()))) {
					exist = true;
					break;
				}
			}
			if (!exist) {
				newAssociation.add(createAssociation(asso.getSource(), asso.getTarget(), TypeEnum.NEW));
			}
		}
		
		List<MapAssociationInfoResponse> oldAssociation = _map.getAssociations();
		oldAssociation.clear();
		oldAssociation.addAll(newAssociation);
		
		_view.setEditing(true);
	}

	// 現在描画されているマップから関連を削除する
	public void removeAssociation(MapAssociationInfoResponse association){
		if(association != null){
			// マップのモデルから該当の関連を削除
			List<MapAssociationInfoResponse> associations = _map.getAssociations();
			associations.remove(association);

			// 画面から関連を削除
			_view.m_canvasComposite.removeConnection(association);
		}
	}

	/*
	 * アイコンファイル名を設定する。
	 */
	public void setIconName(String facilityId, String filename) {

		Map<String, FacilityElementResponse> contents = _map.getContents();
		FacilityElementResponse element = null;
		// findbugs対応 keySetをentrySetに変更
		for (Entry<String, FacilityElementResponse> rec : contents.entrySet()) {
			String key = rec.getKey();
			if (facilityId.equals(key)) {
				element = rec.getValue();
				break;
			}
		}

		if (element != null) {
			element.setIconImage(filename);
		}
	}

	/*
	 * アイコンファイル名を設定する。
	 */
	public void setBgName(String filename) {
		_map.setBgName(filename);
	}

	public void registerNodeMap() throws Exception {
		try {
			NodeMapRestClientWrapper wrapper = NodeMapRestClientWrapper.getWrapper(_view.getCanvasComposite().getManagerName());
			RegisterNodeMapModelRequest dtoReq = new RegisterNodeMapModelRequest();
			RestClientBeanUtil.convertBean(_map, dtoReq);
			wrapper.registerNodeMapModel(dtoReq);
			_view.setEditing(false);
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 親スコープのファシリティIDを返します。
	 * @return スコープのファシリティID
	 */
	public String getParent(){
		if (_map == null) {
			m_log.warn("_map is null");
			return "";
		}
		return _map.getParentMapId();
	}

	/**
	 * マップ名を返します。
	 * @return マップ名
	 */
	public String getMapName(){
		if(_map == null){
			return null;
		}

		return _map.getMapName();
	}

	/**
	 * 描画対象スコープのcurrentScopeを返す
	 * @return 描画対象スコープのcurrentScopeを返す
	 */
	public String getCurrentScope(){
		return _currentScope;
	}

	/**
	 * 描画対象スコープのsecondaryIdを返す
	 * @return 描画対象スコープのsecondaryIdを返す
	 */
	public String getSecondaryId(){
		return _secondaryId;
	}

	public void putFacilityFigure (String facilityId, FacilityFigure figure) {
		m_figureMap.put(facilityId, figure);
	}

	public FacilityFigure getFacilityFigure(String facilityId) {
		return m_figureMap.get(facilityId);
	}

	public void putConnection(MapAssociationInfoResponse association, PolylineConnection connection) {
		m_connectionMap.put(association, connection);
	}

	public void removeConnection(MapAssociationInfoResponse association) {
		m_connectionMap.remove(association);
	}

	public PolylineConnection getConnection(MapAssociationInfoResponse association) {
		return m_connectionMap.get(association);
	}

	public Set<MapAssociationInfoResponse> getAssociation() {
		return m_connectionMap.keySet();
	}

	public void updateConnection(Point mousePoint, String fid) {
		// 現在描画されている全てのファシリティの図を取得
		for (FacilityFigure figure : m_figureMap.values()) {
			Point figurePoint = figure.getLocation();
			int startX = figurePoint.x;
			int startY = figurePoint.y;
			Dimension size = figure.getSize();
			int endX = startX + size.width;
			int endY = startY + size.height;

			m_log.debug(startX + " < " + mousePoint.x + " < " + endX);
			m_log.debug(startY + " < " + mousePoint.y + " < " + endY);

			if((startX < mousePoint.x) && (mousePoint.x < endX)){
				if((startY < mousePoint.y) && (mousePoint.y < endY)){
					String facilityId = figure.getFacilityId();
					// モデルに値を反映
					// 2つのファシリティが別インスタンスであることが条件となる（自己参照のコネクションは生成しない）
					if(!fid.equals(facilityId)){
						// 該当コネクションがノードマップ上に存在しない場合は描画
						// 既に存在する場合は削除する
						boolean addFlag = true;
						String srcFacilityId = fid;
						String targetFacilityId = facilityId;

						// 存在する場合は、関連の種類に応じて処理を変える
						// 現バージョンでは、関連の方向性（src->target）は考慮しないため、
						// 逆向きも調査する
						for(MapAssociationInfoResponse association : m_connectionMap.keySet()) {
							String tmpSource = association.getSource();
							String tmpTarget = association.getTarget();
							TypeEnum tmpType = association.getType();

							if (srcFacilityId.equals(tmpSource) && targetFacilityId.equals(tmpTarget)
									|| srcFacilityId.equals(tmpTarget) && targetFacilityId.equals(tmpSource)) {
								if (tmpType == TypeEnum.NORMAL || tmpType == TypeEnum.NEW) {
									// 関連が存在する場合は削除する
									removeAssociation(association);
									addFlag = false;
									break;
								} else {
									// 削除予定の関連の場合は追加する(今の関連は一度消す)
									removeAssociation(association);
									addFlag = true;
									break;
								}
							}
						}
						if(addFlag == true){
							addAssociation(srcFacilityId, targetFacilityId);
						}
					}
					// 終点に複数アイコンが重なっていていてもひとつにしか処理しない
					break;
				}
			}
		}
	}

	public boolean isMapBuiltin() {
		return _map.getBuiltin();
	}
	
	public String getOwnerRoleId() {
		return _map.getOwnerRoleId();
	}

	public String getManagerName() {
		return _view.getCanvasComposite().getManagerName();
	}
	
	public String getMapId() {
		return _map.getMapId();
	}
	
	public String[] getFindIdList() {
		return m_findIdList.toArray(new String[0]);
	}

}
