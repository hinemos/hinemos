/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.editpart;

import java.rmi.AccessException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.ColorConstantsWrapper;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Color;
import org.openapitools.client.model.JobDetailInfoResponse;
import org.openapitools.client.model.JobNextJobOrderInfoResponse;
import org.openapitools.client.model.JobObjectGroupInfoResponse;
import org.openapitools.client.model.JobObjectInfoResponse;
import org.openapitools.client.model.JobTreeItemResponseP4;
import org.openapitools.client.model.JobWaitRuleInfoResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.jobmanagement.bean.DecisionObjectMessage;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectMessage;
import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobPropertyUtil;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmap.bean.ColorfulAssociation;
import com.clustercontrol.jobmap.composite.JobMapComposite;
import com.clustercontrol.jobmap.figure.JobFigure;
import com.clustercontrol.jobmap.figure.JobMapColor;
import com.clustercontrol.jobmap.preference.JobMapPreferencePage;
import com.clustercontrol.jobmap.util.JobMapRestClientWrapper;
import com.clustercontrol.jobmap.util.JobmapImageCacheUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * ジョブマップビューをコントロールするクラス。
 * 表示はNodeMapViewクラス。
 * @since 1.0.0
 */
public class MapViewController {

	// ログ
	private static Log m_log = LogFactory.getLog( MapViewController.class );

	private final JobMapComposite m_composite;
	private String m_sessionId;

	// 描画対象のマップの情報を保持したモデル
	private JobTreeItemWrapper m_jobTreeItem;

	// アイコンを置く間隔
	public static final int heightPitch = 55;
	public static final int leftMargin = 20;
	public static final int upMargin = 20;
	
	public int maxX = 0;
	public int maxY = 0;
	
	// 最大表示数
	private int maxJob;
	private int maxDepth;
	private boolean autoExpand;
	private boolean detourConnection;
	private boolean compactConnection;
	private boolean turnConnection;
	private int turnWidth;
	private boolean arrowWhite;
	private int textWidth;
	private boolean labelingId;
	private boolean dragdropId;

	
	// xy座標の入れ替えフラグ。
	// true  : 時間軸が左から右へ。(default)
	// false : 時間軸が上から下へ。
	private boolean xyChange = true;

	// モデルと図の関係を保持するマップ
	// 描画対象スコープ、ノードのファシリティIDとそれを描画している図（Figure）のリファレンスを保持
	private ConcurrentHashMap<String, JobFigure> m_figureMap = new ConcurrentHashMap<String, JobFigure>();

	// モデルとコネクションの関係を保持するマップ
	private ConcurrentHashMap<PolylineConnection, ColorfulAssociation> m_connectionMap =
		new ConcurrentHashMap<PolylineConnection, ColorfulAssociation>();

	// 展開状況を保存するセッション数
	private static final int sessionIdSaveCounts = 16;
	
	public static final PointList TRIANGLE;

	private ArrayList<String> m_sessionIdList = new ArrayList<String>();
	private HashMap<String, Set<CollapseItemKey>> m_collapseItemKeySet = new HashMap<String, Set<CollapseItemKey>>();
	private HashMap<String, Set<CollapseItemKey>> m_expandItemKeySet = new HashMap<String, Set<CollapseItemKey>>();
	
	private String m_managerName;

	private JobFigure m_rootJobFigure = null;

	static {
		TRIANGLE = new PointList(3);
		TRIANGLE.addPoint(-3, -2);
		TRIANGLE.addPoint(1, 0);
		TRIANGLE.addPoint(-3, 2);
	}
	
	public MapViewController(JobMapComposite composite){
		m_composite = composite;
	}

	public void updateMap() {
		m_connectionMap.clear();
		m_figureMap.clear();
		
		if (m_jobTreeItem == null) {
			m_log.debug("updateMap _map is null");
			return;
		}

		// マップを削除
		if (m_composite != null) {
			m_composite.clearCanvas();
		}

		// 最上位の場合は描画しない。
		if (m_jobTreeItem.getData() != null &&
				(m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.COMPOSITE || m_jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum .MANAGER)) {
			m_composite.initialMessageDisplay();
			return;
		}

		try {
			// ジョブ、ジョブネット、矢印(待ち条件)を描画
			List<JobTreeItemWrapper> children = new ArrayList<JobTreeItemWrapper>();
			children.add(m_jobTreeItem);
			// ジョブマップ(履歴)の場合は一つ階層が深いので、潜っておく。
			if (m_sessionId != null) {
				children = children.get(0).getChildren();
			}
			
			// 非常に多い場合は表示しない。
			if (maxJob < countJob(children.get(0))) {
				throw new HinemosUnknown("jobs are too many!!!");
			}
			// 非常に深い場合は表示しない。
			if (maxDepth <= JobFigure.getDepth(children.get(0))) {
				throw new HinemosUnknown("jobs are too deep!!!");
			}
			
			// 描画対象のジョブの情報をまとめて取得
			if (m_sessionId == null) {
				JobPropertyUtil.setJobFullList(m_managerName, getChildrenAll(children.get(0)));
			}
			
			updateImage(null, children);
			
			if (m_composite.isZoomAdjust()) {
				m_composite.zoomAdjust();
			}
			
		} catch (HinemosUnknown e) {
			if (m_composite != null) {
				m_composite.clearCanvas();
				m_composite.setErrorMessage(HinemosMessage.replace(e.getMessage()));
			}
		}
	}
	
	private List<JobInfoWrapper> getChildrenAll(JobTreeItemWrapper item) {
		ArrayList<JobInfoWrapper> list = new ArrayList<JobInfoWrapper>();
		list.add(item.getData());
		
		for (JobTreeItemWrapper child : item.getChildren()) {
			list.addAll(getChildrenAll(child));
		}

		return list;
	}

	private int countJob(JobTreeItemWrapper jobTreeItem) {
		if (jobTreeItem.getData() != null &&
				(jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.JOB ||
						jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.FILEJOB ||
						jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.REFERJOB ||
						jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.APPROVALJOB ||
						jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.MONITORJOB ||
						jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.FILECHECKJOB ||
						jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBLINKSENDJOB ||
						jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBLINKRCVJOB ||
						jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.RESOURCEJOB ||
						jobTreeItem.getData().getType() == JobInfoWrapper.TypeEnum.RPAJOB)) {
			return 1;
		}
		int ret = 0;
		// ジョブ履歴の場合、最上位はgetDataがnullになる。
		for (JobTreeItemWrapper item : jobTreeItem.getChildren()) {
			ret += countJob(item);
		}
		return ret;
	}

	public void clearMapData() {
		m_jobTreeItem = null;
		m_sessionId = null;
		m_managerName = null;
	}
	
	/**
	 * マネージャから情報を取得しマップを更新する
	 * NodeMapView以外からは呼ぶべきではない
	 * @param sessionId 
	 * @throws AccessException
	 */
	public void updateMap(String managerName, String sessionId, JobTreeItemWrapper jobTreeItem) throws Exception {
		if( m_log.isDebugEnabled() ){
			m_log.debug("updateMap(String managerName="+managerName+", String sessionId="+sessionId+", JobTreeItemWrapper jobTreeItem="+jobTreeItem+")");
		}
		if (managerName != null) {
			m_managerName = managerName;
		}
		if (sessionId != null) {
			m_sessionId = sessionId;
		}
		if (jobTreeItem != null) {
			m_jobTreeItem = jobTreeItem;
		}

		if (m_managerName == null) {
			return;
		}
		
		if (m_sessionId != null) {
			// ジョブマップビューア
			String removeSessionId = null;
			if (m_sessionIdList.contains(m_sessionId)) {
				removeSessionId = m_sessionId;
			} else {
				if (m_sessionIdList.size() >= sessionIdSaveCounts) {
					// 保存数を超えたらもっとも古いものを削除
					removeSessionId = m_sessionIdList.get(0);
					m_collapseItemKeySet.remove(removeSessionId);
					m_expandItemKeySet.remove(removeSessionId);
				}
				// 展開状況を保存するHashSetを作成
				m_collapseItemKeySet.put(m_sessionId, new HashSet<CollapseItemKey>());
				m_expandItemKeySet.put(m_sessionId, new HashSet<CollapseItemKey>());
			}
			
			if (removeSessionId != null) {
				m_sessionIdList.remove(removeSessionId);
			}
			m_sessionIdList.add(m_sessionId);
		} else {
			// ジョブマップエディタ
			if (!m_collapseItemKeySet.containsKey("")) {
				m_collapseItemKeySet.put("", new HashSet<CollapseItemKey>());
			}
			if (!m_expandItemKeySet.containsKey("")) {
				m_expandItemKeySet.put("", new HashSet<CollapseItemKey>());
			}
		}
		
		try {
			boolean isPublish = JobMapRestClientWrapper.getWrapper(m_managerName).checkPublish().getPublish();
			if (!isPublish) {
				MessageDialog.openWarning(
						null,
						Messages.getString("warning"),
						com.clustercontrol.jobmap.messages.Messages.getString("expiration.term.invalid"));
			}
		} catch (HinemosUnknown | InvalidRole | InvalidUserPass | RestConnectFailed e) {
			String errMsg = com.clustercontrol.jobmap.messages.Messages.getString("message.jobmapkeyfile.notfound.error");
			m_composite.clearCanvas();
			m_composite.setErrorMessage(errMsg);
			return;
		}
		// マネージャからジョブのセッション情報を取得
		if (m_sessionId != null) {
			try {
				JobTreeItemResponseP4  ret = JobRestClientWrapper.getWrapper(m_managerName).getJobDetailList(m_sessionId);
				m_jobTreeItem =JobTreeItemUtil.getItemFromP4(ret);
			} catch (Exception e) {
				m_log.warn("updateMap() getJobDetailList, " + HinemosMessage.replace(e.getMessage()), e);
				// 連続してエラーが起きないように表示内容をリセットする
				m_composite.clearCanvas();
				m_jobTreeItem = null;
				m_sessionId = null;
				m_managerName = null;
				throw e;
			}
			
			//ビューワー向けのマップ情報更新時、アイコンキャッシュもリフレッシュする。
			JobmapImageCacheUtil iconCache = JobmapImageCacheUtil.getInstance();
			iconCache.refresh(m_managerName,true);
		}

		// 描画する。
		updateMap();
	}
	
	private boolean haveWaitedJobsAlreadyBeenDrawn(JobTreeItemWrapper jobTreeItem,
			Set<JobTreeItemWrapper> itemsDrawn, Set<String> allJobIdList) {

		JobWaitRuleInfoResponse jobWaitRuleInfo = jobTreeItem.getData().getWaitRule();
		if (jobWaitRuleInfo == null) {
			return true;
		}
		
		Set<String> jobIdSetDrawn = new HashSet<String>();
		for (JobTreeItemWrapper itemDrawn : itemsDrawn) {
			jobIdSetDrawn.add(itemDrawn.getData().getId());
		}

		boolean haveBeenDrawn = true;
		List<JobObjectGroupInfoResponse> waitGroupList = jobWaitRuleInfo.getObjectGroup();
		if (waitGroupList == null || waitGroupList.isEmpty()) {
			return haveBeenDrawn;
		}
		for (JobObjectGroupInfoResponse waitGroup : waitGroupList) {
			for (JobObjectInfoResponse wait : waitGroup.getJobObjectList()) {
				// 時間待ちとセッション横断待ちはスキップ
				if (wait.getType() == JobObjectInfoResponse.TypeEnum.TIME
						|| wait.getType() == JobObjectInfoResponse.TypeEnum.START_MINUTE
						|| wait.getType() == JobObjectInfoResponse.TypeEnum.CROSS_SESSION_JOB_END_STATUS
						|| wait.getType() == JobObjectInfoResponse.TypeEnum.CROSS_SESSION_JOB_END_VALUE) {
					continue;
				}

				String jobId = wait.getJobId();
				if (!allJobIdList.contains(jobId)) {// 描画範囲外のジョブの場合スキップ
					continue;
				}

				if (!jobIdSetDrawn.contains(jobId)) {
					haveBeenDrawn = false;
					m_log.debug(String.format("%s is waiting for %s.", jobTreeItem.getData().getId(), jobId));
					break;
				}
			}
		}
		return haveBeenDrawn;
	}
	
	private void updateImage(JobFigure parent, List<JobTreeItemWrapper> children) throws HinemosUnknown {
		if (children == null || children.size() == 0) {
			return;
		}
		
		int spaceX = getWidthPitch() - getTextWidth();
		if (xyChange) {
			spaceX += 0;
		} else {
			spaceX += 70;
		}
		int spaceY = heightPitch - JobFigure.textHeight;
		
		Layer layer = null;
		if (parent != null) {
			layer = parent.getLayer();
		}
		
		Set<String> childrenJobIdSet = new HashSet<String>();
		for (JobTreeItemWrapper child : children) {
			childrenJobIdSet.add(child.getData().getId());
		}
		
		int x = 0;
		int y = 0;
		if (parent == null) {
			x = leftMargin;
			y = upMargin; 
		} else {
			x = JobFigure.jobnetBorder;
			y = JobFigure.textHeight + JobFigure.jobnetBorder;
		}

		boolean forceDraw = false;
		Set<JobTreeItemWrapper> childrenDrawn = new HashSet<JobTreeItemWrapper> ();
		
		int previous = isXyChange() ? y : x;
		int interval = 0;
		
		//優先順番号を描画処理順に反映
		List<JobTreeItemWrapper>adjustNextOrderList = adjustNextOrderArrangement(children);
		
		while (children.size() > childrenDrawn.size()) {
			int round =0;
			int prevChildWidth = 0;
			int prevChildHeight = 0;
			int maxChildWidth = 0;
			int maxChildHeight = 0;
			Set<JobTreeItemWrapper> childrenDrawnThisRound = new HashSet<JobTreeItemWrapper> ();
			for (JobTreeItemWrapper jobTreeItem : adjustNextOrderList) {
				//すでに描画済みかどうかのチェック
				if (childrenDrawn.contains(jobTreeItem)) {
					continue;
				}

				//待ち条件ループが発生する場合に、待ち条件のチェックをスキップ
				if (forceDraw) {
					forceDraw = false;
				} else {
					//待つジョブを先に描画すべきのため、描画されていない待つジョブをチェック
					if (!haveWaitedJobsAlreadyBeenDrawn(jobTreeItem, childrenDrawn, childrenJobIdSet)) {
						continue;
					}
				}
				
				//x, yの計算及びFigureJobオブジェクトの作成
				if (round > 0) {//一番目の場合に、spaceX | spaceYの加算が要らない
					if (xyChange) {
						y += prevChildHeight + spaceY;
					} else {
						x += prevChildWidth + spaceX;
					}
				}
				boolean collapse = doesCollapseJobnet(jobTreeItem);
				JobFigure jobFigure = m_composite.drawFigure(
						layer,
						jobTreeItem, x, y,
						collapse);
				
				//自分のサイズを調整する前に、まず子オブジェクトの更新を行う。
				if (!collapse) {
					updateImage(jobFigure, jobTreeItem.getChildren());
				}
				
				//サイズの計算と調整
				jobFigure.adjustBackgroundSize();
				Dimension jobFigureSize = jobFigure.getBackgroundSize();
				if (parent == null) {//top
					maxX = x + jobFigureSize.width;
					maxY = y + jobFigureSize.height;
					
					//root
					m_rootJobFigure  = jobFigure;
				}
				
				//サイズ情報の保存
				prevChildWidth = jobFigureSize.width;
				prevChildHeight = jobFigureSize.height;
				maxChildWidth = Math.max(maxChildWidth, prevChildWidth);
				maxChildHeight = Math.max(maxChildHeight, prevChildHeight);
				if (xyChange) {
					interval = Math.max(interval, y + prevChildHeight);
				} else {
					interval = Math.max(interval, x + prevChildWidth);
				}
				childrenDrawnThisRound.add(jobTreeItem);

				m_log.debug(jobTreeItem.getData().getId() +
						", x=" + x + 
						", y=" + y + 
						", width=" + prevChildWidth +
						", height=" + prevChildHeight +
						", maxChildWidth=" + maxChildWidth +
						", maxChildHeight=" + maxChildHeight +
						", maxX=" + maxX +
						", maxY=" + maxY);
				
				round++;
			}//for

			//待ち条件のループが発生する場合に、
			if (childrenDrawnThisRound.isEmpty()) {
				forceDraw = true;
				continue;
			}
			
			childrenDrawn.addAll(childrenDrawnThisRound);
			
			if (xyChange) {
				int width = 150;
				if (isCompactConnection()) {
					width = width / 3;
				}
				x += maxChildWidth + spaceX + width;
				y = previous;
				
				if (isTurnConnection() && x >= getTurnWidth()/m_composite.scale) {
					x = leftMargin + JobFigure.jobnetBorder;
					y = interval + spaceY;
					previous = y;
					interval = 0;
				}
			} else {
				x = previous;
				int height = 50;
				if (isCompactConnection()) {
					height = height / 3;
				}
				y += maxChildHeight + spaceY + height;
				
				if (isTurnConnection() && y >= getTurnWidth()/m_composite.scale) {
					x = interval + spaceX;
					y = upMargin + JobFigure.textHeight + JobFigure.jobnetBorder;
					previous = x;
					interval = 0;
				}
			}
		}//while
		
		// 待ち条件(矢印)を描画
		drawAssociationLines(parent, children);		
	}
	
	/**
	 * 優先順を元にした描画処理順調整
	 * 
	 * Jobアイコンの並びが 後続ジョブ実行設定（優先順）に従うよう
	 * 対象JobTreeItem一覧の順番を調整（UpdateImageメソッドからの呼び出し専用）
	 * 
	 **/
	private List<JobTreeItemWrapper> adjustNextOrderArrangement( List<JobTreeItemWrapper> lists) {
		//List内の優先順設定を取得 ( 各ジョブに紐付けられた優先順設定のMap )
		HashMap<String,Integer> setOrderMap = new HashMap<String,Integer>(); //Job に割り付けられた優先順番号 Map
		HashMap<String,String > setPreviousMap = new HashMap<String,String>(); //Job に割り付けられた 先行ジョブ Map
		for (JobTreeItemWrapper jobTreeItem : lists) {
			if( jobTreeItem.getData().getWaitRule() == null || jobTreeItem.getData().getWaitRule().getExclusiveBranch() == false ){
				continue;
			}
			m_log.debug("adjustNextOrderArrangement .this is isExclusiveBranch .jobid="+jobTreeItem.getData().getId());
			if( jobTreeItem.getData().getWaitRule().getExclusiveBranchNextJobOrderList()  != null ){
				int orderNo = 0;
				for( JobNextJobOrderInfoResponse jobNextJobOrderInfo : jobTreeItem.getData().getWaitRule().getExclusiveBranchNextJobOrderList()){
					orderNo += 1;
					//複数の優勢設定がある場合はジョブIDが若い側を優先
					if( setPreviousMap.containsKey(jobNextJobOrderInfo.getNextJobId()) == false ){
						setPreviousMap.put(jobNextJobOrderInfo.getNextJobId(),jobTreeItem.getData().getId());
						setOrderMap.put(jobNextJobOrderInfo.getNextJobId(),orderNo);
					}
				}
				
			}
		}
		
		//順番調整用にListを振分
		List<JobTreeItemWrapper>retOrderList = new ArrayList<JobTreeItemWrapper>();
		HashMap<String,HashMap<Integer,JobTreeItemWrapper>>pendingRoleOrderMap = new HashMap<String,HashMap<Integer,JobTreeItemWrapper>>();//待ち対象ジョブID -> 順番号  = JobTreeItemWrapper
		for (JobTreeItemWrapper jobTreeItem : lists) {
			//優先順なし
			String previousJob = setPreviousMap.get(jobTreeItem.getData().getId());
			if( previousJob == null ){
				//そのままの順番（ID順）をキープ
				retOrderList.add(jobTreeItem);
			//優先順あり 
			}else{
				//先行ジョブ毎＋優先順を考慮できる形で保留
				Integer orderNo = setOrderMap.get(jobTreeItem.getData().getId());
				HashMap<Integer,JobTreeItemWrapper> orderMap = pendingRoleOrderMap.get(previousJob);
				if( orderMap != null ) {
					orderMap.put(orderNo,jobTreeItem);
				}else{
					orderMap = new HashMap<Integer,JobTreeItemWrapper>();
					orderMap.put(orderNo,jobTreeItem);
					pendingRoleOrderMap.put(previousJob,orderMap);
				}
			}
		}
		//保留した優先順指定Itemを 優先順を加味して末尾に追加
		String[] roledOrderMapKey = pendingRoleOrderMap.keySet().toArray(new String[0]);
		Arrays.sort(roledOrderMapKey);
		for (String roledJobId : roledOrderMapKey) {
			HashMap<Integer,JobTreeItemWrapper> orderMap  = pendingRoleOrderMap.get(roledJobId);
			Integer[] orderKey = orderMap.keySet().toArray(new Integer[0]);
			Arrays.sort(orderKey);
			for (Integer orderNo : orderKey) {
				retOrderList.add( orderMap.get(orderNo));
			}
		}
		return retOrderList;
	}

	private boolean doesCollapseJobnet(JobTreeItemWrapper jobTreeItem) {
		CollapseItemKey collapseItemKey = new CollapseItemKey(jobTreeItem);
		if (getCollapseItemKeySet(m_sessionId).contains(collapseItemKey)) {
			return true;
		}
		if (getExpandItemKeySet(m_sessionId).contains(collapseItemKey)) {
			return false;
		}
		
		if (!autoExpand) {
			return false;
		}
		
		JobDetailInfoResponse detail = jobTreeItem.getDetail();
		if (detail == null) {
			return false;
		}
		
		JobDetailInfoResponse.StatusEnum status = detail.getStatus();
		if (status == null) {
			return false;
		}
		if (status == JobDetailInfoResponse.StatusEnum.WAIT) {
			return true;
		}
		
		JobDetailInfoResponse.EndStatusEnum endStatus = detail.getEndStatus();
		if (endStatus == null) {
			return false;
		}
		if (endStatus ==JobDetailInfoResponse.EndStatusEnum.NORMAL) {
			return true;
		}
		
		return false;
	}

	private void drawAssociationLines(JobFigure parent,
			List<JobTreeItemWrapper> children) {
		
		//List内の優先順設定をMAPに取得
		HashMap<String,HashMap<String,Integer> > JobWaitOrderMap = new HashMap<String,HashMap<String,Integer> >();// srcのジョブid -> tagのジョブID = 優先順番号
		for (JobTreeItemWrapper jobTreeItem : children) {
			if( jobTreeItem.getData().getWaitRule() == null || jobTreeItem.getData().getWaitRule().getExclusiveBranch() == false ){
				continue;
			}
			if( jobTreeItem.getData().getWaitRule().getExclusiveBranchNextJobOrderList()  != null ){
				HashMap<String,Integer> setMap = new HashMap<String,Integer>() ;
				m_log.debug("drawAssociationLines JobWaitOrderMap getId:" + jobTreeItem.getData().getId());
				int orderNo = 0;
				for( JobNextJobOrderInfoResponse jobNextJobOrderInfo : jobTreeItem.getData().getWaitRule().getExclusiveBranchNextJobOrderList()){
					orderNo += 1; 
					setMap.put(jobNextJobOrderInfo.getNextJobId(),orderNo);
					m_log.debug("drawAssociationLines JobWaitOrderMap put:" +jobTreeItem.getData().getId() +"->" + jobNextJobOrderInfo.getNextJobId() );
				}
				JobWaitOrderMap.put(jobTreeItem.getData().getId(),setMap);
			}
		}
		// associtionListの作成、associationListの描画という流れ。
		// まずは待ち条件からassociationListを作成する。
		ArrayList<ColorfulAssociation> associationList = new ArrayList<ColorfulAssociation>();
		for (JobTreeItemWrapper jobTreeItem : children) {
			JobWaitRuleInfoResponse jobWaitRuleInfo = jobTreeItem.getData().getWaitRule();
			if (jobWaitRuleInfo == null) {
				continue;
			}

			List<JobObjectGroupInfoResponse> objectGroupList = jobWaitRuleInfo.getObjectGroup();
			if (objectGroupList == null || objectGroupList.isEmpty()) {
				continue;
			}
			for (JobObjectGroupInfoResponse waitroup : objectGroupList) {
				List<JobObjectInfoResponse> objectList = waitroup.getJobObjectList();
				for (JobObjectInfoResponse waitRule : objectList) {
					// セッション横断待ちと時刻待ちは対象外
					if (waitRule.getType() == JobObjectInfoResponse.TypeEnum.TIME
							|| waitRule.getType() == JobObjectInfoResponse.TypeEnum.START_MINUTE
							|| waitRule.getType() == JobObjectInfoResponse.TypeEnum.CROSS_SESSION_JOB_END_STATUS
							|| waitRule.getType() == JobObjectInfoResponse.TypeEnum.CROSS_SESSION_JOB_END_VALUE) {
						continue;
					}

					ColorfulAssociation newAssociation = new ColorfulAssociation(waitRule.getJobId(),
							jobTreeItem.getData().getId());

					// 色を決めて
					if (waitRule.getType() == JobObjectInfoResponse.TypeEnum.JOB_END_STATUS) {
						Color color = null;
						if (waitRule.getStatus() == JobObjectInfoResponse.StatusEnum.ABNORMAL) {
							color = JobMapColor.red;
						} else if (waitRule.getStatus() == JobObjectInfoResponse.StatusEnum.NORMAL) {
							color = JobMapColor.green;
						} else if (waitRule.getStatus() == JobObjectInfoResponse.StatusEnum.WARNING) {
							color = JobMapColor.yellow;
						} else {
							m_log.debug("updateImte() association " + waitRule.getValue());
						}
						if (color != null) {
							newAssociation.setLineColor(new Color (null, color.getRed() * 5 / 6, color.getGreen() * 5 / 6,
									color.getBlue() * 5 / 6));
							newAssociation.setLabelColor(new Color (null, color.getRed() * 3 / 4, color.getGreen() * 3 / 4,
									color.getBlue() * 3 / 4));
						}
					} else if (waitRule.getType() == JobObjectInfoResponse.TypeEnum.JOB_RETURN_VALUE){
						newAssociation.setLineColor(JobMapColor.black);
					}

					// ツールチップを作る。
					String tooltip = "";
					String waitValue = "";
					if (waitRule.getType() == JobObjectInfoResponse.TypeEnum.JOB_END_STATUS) {
						waitValue = EndStatusMessage.typeEnumValueToString(waitRule.getStatus().getValue());
					} else if (waitRule.getType() == JobObjectInfoResponse.TypeEnum.JOB_END_VALUE
							|| waitRule.getType() == JobObjectInfoResponse.TypeEnum.JOB_RETURN_VALUE) {
						waitValue = DecisionObjectMessage.typeEnumToString(waitRule.getDecisionCondition()) + ", "
								+ waitRule.getValue();
					}
					tooltip = JudgmentObjectMessage.enumToString(waitRule.getType()) + ", " + waitValue;
					newAssociation.addTooltip(tooltip);

					// ラベルを設定する
					String label = "";
					if (!isCompactConnection() && !waitRule.getDescription().isEmpty()) {
						label = waitRule.getDescription();
						newAssociation.setLabel(label);
					}

					// 優先順指定があればtarget隣接ラベルに設定して tooltipにも記載
					HashMap<String, Integer> setMap = JobWaitOrderMap.get(newAssociation.getSource());
					if (setMap != null) {
						Integer orderNo = setMap.get(newAssociation.getTarget());
						if (orderNo != null) {
							newAssociation.setTargetAdjacentLabel(orderNo + ".");
							newAssociation.setOrderToolTip(
									Messages.getString("job.exclusive.branch.nextjob.order_short") + " : " + orderNo);
							if (m_log.isDebugEnabled()) {
								m_log.debug("drawAssociationLines JobWaitOrderNo target:" + newAssociation.getTarget()
										+ " order:" + orderNo);
							}
						}
					}

					// 重複している要素があるかチェックを行う、重複していた場合要素を上書きする
					ColorfulAssociation duplicateOjb = null;
					for (ColorfulAssociation checkOjb : associationList) {
						duplicateOjb = duplicateCheckOjb(checkOjb, newAssociation, tooltip);
						if (duplicateOjb != null) {
							checkOjb = duplicateOjb;
							break;
						}
					}
					// 重複した要素はnewAssociationはListに追加しない。
					if (duplicateOjb != null) {
						continue;
					}
					if (newAssociation.getSource() == null || newAssociation.getTarget() == null) {
						continue;
					}
					m_log.debug("list " + newAssociation.getSource() + "->" + newAssociation.getTarget());
					associationList.add(newAssociation);
				}
			}
		}

		// associationListの描画
		ColorfulAssociation[] association = associationList.toArray(new ColorfulAssociation[associationList.size()]);
		for(int i=0; i<association.length; i++){
			if (parent == null) {
				m_composite.drawConnection(null, association[i]);
			} else {
				m_composite.drawConnection(parent.getLayer(), association[i]);
			}
		}
	}

	/**
	 * 矢印が重複している場合、処理を行った要素を返す
	 * 
	 * @param checkOjb
	 * @param newAssociation
	 * @param tooltip
	 */
	private ColorfulAssociation duplicateCheckOjb(ColorfulAssociation checkOjb, ColorfulAssociation newAssociation,
			String tooltip) {
		// 矢印の重複チェック(SourceとTargetで確認)。
		if (checkOjb.getSource().equals(newAssociation.getSource())
				&& checkOjb.getTarget().equals(newAssociation.getTarget())) {
			if (m_log.isDebugEnabled()) {
				m_log.debug("drawAssociationLines line duplicate :" + newAssociation.getSource() + "->"
						+ newAssociation.getTarget());
			}
			// 重複していたら、Listに存在するものの色（既定に戻す）,ツールチップ（追加）、ラベル（追加）を変更
			checkOjb.setDefaultColor();
			checkOjb.addTooltip(tooltip);

			// ラベルに新たに追加する要素がなければスキップ
			if (newAssociation.getLabel().isEmpty()) {
				return checkOjb;
			}

			// 重複した要素に既にラベルがあれば追記、なければ新たに追加
			boolean labelExist = !checkOjb.getLabel().isEmpty();
			if (labelExist) {
				checkOjb.setLabel(checkOjb.getLabel() + "\n" + newAssociation.getLabel());
			} else {
				checkOjb.setLabel(newAssociation.getLabel());
			}
			return checkOjb;

		} else {
			return null;
		}
	}
	
	public void emphasisConnection(String jobId) {
		for (PolylineConnection connection : m_connectionMap.keySet()) {
			ColorfulAssociation asso = m_connectionMap.get(connection);
			int width;
			if (asso.getSource().equals(jobId) || asso.getTarget().equals(jobId)) {
				width = 6;
			} else {
				width = 3;
			}
			connection.setLineWidth(width);
			PolygonDecoration decoration = new PolygonDecoration();
			decoration.setTemplate(TRIANGLE);
			decoration.setScale(width, width);
			if (isAllowWhite()) {
				decoration.setBackgroundColor(ColorConstantsWrapper.white());
			}
			connection.setTargetDecoration(decoration);
			// ラベル
			if (connection.getChildren() != null) {
				for (Object obj : connection.getChildren()) {
					if (obj instanceof Label) {
						if (asso.getSource().equals(jobId) || asso.getTarget().equals(jobId)) {
							((Label)obj).setOpaque(true);
						} else {
							((Label)obj).setOpaque(false);
						}
						break;
					}
				}
			}
		}
	}

	// 関連を生成します。
	public ColorfulAssociation createAssociation(String sourceJobId, String targetJobId){
		if(sourceJobId != null && targetJobId != null){
			// 関連を生成
			ColorfulAssociation association = new ColorfulAssociation(sourceJobId, targetJobId);

			return association;
		}
		return null;
	}

	public JobFigure getJobFigure(Point mousePoint) {
		JobFigure ret = null;
		int maxDepth = 0;

		// 現在描画されている全ての図を取得
		for (JobFigure figure : m_figureMap.values()) {
			Point figurePoint = figure.getLocation();
			int startX = figurePoint.x;
			int startY = figurePoint.y;
			Dimension size = figure.getSize();
			int endX = startX + size.width;
			int endY = startY + size.height;

			if((startX < mousePoint.x) && (mousePoint.x < endX)){
				if((startY < mousePoint.y) && (mousePoint.y < endY)){
					// 複数のジョブが重なっている場合は、最上位(画面上で一番上に重なっている)のジョブを選択する。
					// 親、祖父・・・の数が多いものを最上位のジョブを判断する。
					int depth = 0;
					IFigure iFigure = figure.getParent();
					while (true) {
						iFigure = iFigure.getParent();
						if (iFigure == null){
							break;
						}
						depth ++;
					}
					m_log.debug("getJobId : id=" +
							figure.getJobTreeItem().getData().getId() +
							",depth=" + depth);
					if (maxDepth < depth) {
						ret = figure;
						maxDepth = depth;
					}
				}
			}
		}
		return ret;
	}

	/**
	 * 描画対象スコープのcurrentJobTreeItemを返す
	 * @return 描画対象スコープのcurrentJobTreeItemを返す
	 */
	public JobTreeItemWrapper getCurrentJobTreeItem(){
		return m_jobTreeItem;
	}

	public void putJobFigure (String jobId, JobFigure figure) {
		m_figureMap.put(jobId, figure);
	}

	public JobFigure getJobFigure(String jobId) {
		return m_figureMap.get(jobId);
	}

	public void putConnection(ColorfulAssociation association, PolylineConnection connection) {
		m_connectionMap.put(connection, association);
	}

	public ColorfulAssociation getConnection(PolylineConnection connection) {
		return m_connectionMap.get(connection);
	}

	public void removeConnection(PolylineConnection connection) {
		ColorfulAssociation association = m_connectionMap.get(connection);
		String sourceId = association.getSource();
		String targetId = association.getTarget();

		JobTreeItemWrapper targetItem = getJobTreeItem(targetId);
		List<JobObjectGroupInfoResponse> waitGroupList = targetItem.getData().getWaitRule().getObjectGroup();
		ArrayList<JobObjectGroupInfoResponse> deleteWaitGroupList = new ArrayList<JobObjectGroupInfoResponse>();

		for(JobObjectGroupInfoResponse waitGroup : waitGroupList){
			ArrayList<JobObjectInfoResponse> deleteJobList = new ArrayList<JobObjectInfoResponse>();

			for (JobObjectInfoResponse wait : waitGroup.getJobObjectList()) {
				if (sourceId.equals(wait.getJobId())){
					deleteJobList.add(wait);
				}
			}
			m_log.debug("delete wait rule : " + deleteJobList.size());
			// 全ての待ち条件群から対象の先行ジョブを削除する。
			waitGroup.getJobObjectList().removeAll(deleteJobList);

			// 待ち条件群が空となった場合、待ち条件群を削除対象とする。
			if(waitGroup.getJobObjectList().size() == 0){
				deleteWaitGroupList.add(waitGroup);
			}
		}

		waitGroupList.removeAll(deleteWaitGroupList);

		// 更新済みフラグを立てる。
		JobEditState editState = JobEditStateUtil.getJobEditState(JobTreeItemUtil.getManagerName(targetItem));
		editState.addEditedJobunit(targetItem);
	}

	private JobTreeItemWrapper getJobTreeItem (String jobId) {
		return getJobTreeItemSub(jobId, m_jobTreeItem);
	}

	private JobTreeItemWrapper getJobTreeItemSub (String jobId, JobTreeItemWrapper parent) {
		for (JobTreeItemWrapper item : parent.getChildren()) {
			if (jobId.equals(item.getData().getId())) {
				return item;
			} else {
				JobTreeItemWrapper tmp = getJobTreeItemSub(jobId, item);
				if (tmp != null) {
					return tmp;
				}
			}
		}
		return null;
	}

	public String getSessionId() {
		return m_sessionId;
	}

	public boolean isXyChange() {
		return xyChange;
	}

	public void setXyChange(boolean xyChange) {
		this.xyChange = xyChange;
	}
	
	public int getWidthPitch() {
		return getTextWidth() + 30;
	}
	
	/**
	 * アイコンを置く間隔
	 * @return
	 */
	public int getHeightPitch() {
		return heightPitch;
	}
	
	/**
	 * 待ち条件の迂回
	 * @return
	 */
	public boolean isDetourConnection() {
		return detourConnection;
	}
	
	public boolean isTurnConnection() {
		return turnConnection;
	}
	
	/**
	 * 待ち条件の折り返し
	 * @return
	 */
	public int getTurnWidth() {
		return turnWidth;
	}
	
	public boolean isAllowWhite() {
		return arrowWhite;
	}
	
	/**
	 * ジョブの横幅
	 * @return
	 */
	public int getTextWidth() {
		return textWidth;
	}
	
	public boolean isLabelingId() {
		return labelingId;
	}
	
	public boolean isDragdropId() {
		return dragdropId;
	}
	
	public boolean isCompactConnection() {
		return compactConnection;
	}

	public void applySetting() {
		IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();
		maxJob = store.getInt(JobMapPreferencePage.P_MAX_DISPLAY_JOB);
		maxDepth = store.getInt(JobMapPreferencePage.P_MAX_DISPLAY_DEPTH);
		autoExpand = store.getBoolean(JobMapPreferencePage.P_AUTO_EXPAND);
		detourConnection = store.getBoolean(JobMapPreferencePage.P_DETOUR_CONNECTION);
		compactConnection = store.getBoolean(JobMapPreferencePage.P_COMPACT_CONNECTION);
		turnConnection = store.getBoolean(JobMapPreferencePage.P_TURN_CONNECTION);
		turnWidth = store.getInt(JobMapPreferencePage.P_TURN_WIDTH);
		arrowWhite = store.getBoolean(JobMapPreferencePage.P_ARROW_WHITE);
		textWidth = 140; // preferencesから削除したため、固定で指定
		labelingId = store.getBoolean(JobMapPreferencePage.P_LABELING_ID);
		dragdropId = store.getBoolean(JobMapPreferencePage.P_DRAGDROP_ID);

		
		m_log.info("maxJob="+maxJob+",maxDepth="+maxDepth+",autoExpand="+autoExpand+",detour="+detourConnection
				+",turn="+turnConnection+",turnWidth="+turnWidth+",arrowWhite="+arrowWhite
				+",textWidth="+textWidth+",labelingId="+labelingId+",dragdropId="+dragdropId+",compactConnection="+compactConnection);
	}

	public void addCollapseItem(JobTreeItemWrapper jobTreeItem) {
		CollapseItemKey collapseItemKey = new CollapseItemKey(jobTreeItem);
		getCollapseItemKeySet(m_sessionId).add(collapseItemKey);
		getExpandItemKeySet(m_sessionId).remove(collapseItemKey);
	}

	public void removeCollapseItem(JobTreeItemWrapper jobTreeItem) {
		CollapseItemKey collapseItemKey = new CollapseItemKey(jobTreeItem);
		getCollapseItemKeySet(m_sessionId).remove(collapseItemKey);
		getExpandItemKeySet(m_sessionId).add(collapseItemKey);
	}
	
	public JobFigure getRootJobFigure() {
		return m_rootJobFigure;
	}
	
	private Set<CollapseItemKey> getCollapseItemKeySet(String sessaionId) {
		if (sessaionId == null) {
			return m_collapseItemKeySet.get("");
		}
		return m_collapseItemKeySet.get(sessaionId);
	}
	
	private Set<CollapseItemKey> getExpandItemKeySet(String sessaionId) {
		if (sessaionId == null) {
			return m_expandItemKeySet.get("");
		}
		return m_expandItemKeySet.get(sessaionId);
	}

	public ConcurrentHashMap<String, JobFigure> getFigureMap() {
		return m_figureMap;
	}
	
	/**
	 * 指定されたアイテムからrootまでの間にある親Itemの折りたたみ表示を
	 * すべて展開する 
	**/
	public boolean doExpandParentDispary(JobTreeItemWrapper targetItem) {
		JobTreeItemWrapper rootItem = m_rootJobFigure.getJobTreeItem();
		HashSet<JobTreeItemWrapper> expandSet = new HashSet<JobTreeItemWrapper>();
		if(rootItem==null){
			m_log.debug("doExpandParentDispary . rootItem . null"  );
			return false;
		}
		//対象とrootの間にある親Itemをすべて取得
		if(m_log.isDebugEnabled()){
			m_log.debug("doExpandParentDispary . JobTreeItem . " + targetItem.getData().getId() + " root " + rootItem.getData().getId() );
		}
		JobTreeItemWrapper current = targetItem;
		boolean isReachRoot = false;
		for (int counter = 0 ; counter <=  maxDepth ;counter++ ){
			//rootまでたどり着いたらループ終了
			if(current.equals(rootItem)){
				isReachRoot = true;
				break;
			}
			current = current.getParent();
			//親が取得できなければそこまでで中止
			if(current==null){
				break;
			}
			expandSet.add(current);
		}
		//rootにたどり着かない場合は処理しない
		if(isReachRoot == false){
			return false;
		}
		//親Itemが折りたたみ済みなら展開する
		boolean doUpdate = false;
		for( JobTreeItemWrapper expandItem : expandSet.toArray(new JobTreeItemWrapper[0]) ){
			boolean isCollapse = doesCollapseJobnet(expandItem);
			if(isCollapse){
				removeCollapseItem(expandItem);
				if(m_log.isDebugEnabled()){
					m_log.debug("doExpandParentDispary . removeCollapseItem . target:" + expandItem.getData().getId());
				}
				doUpdate= true;
			}
		}
		// 必要なら再描画する。
		if(doUpdate){
			updateMap();
		}
		return true;
	}
	
}

class CollapseItemKey {
	private String jobunitId;
	private String jobId;
	
	public CollapseItemKey(String jobunitId, String jobId) {
		this.jobunitId = jobunitId;
		this.jobId = jobId;
	}

	public CollapseItemKey(JobTreeItemWrapper jobTreeItem) {
		this.jobunitId = jobTreeItem.getData().getJobunitId();
		this.jobId = jobTreeItem.getData().getId();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((jobId == null) ? 0 : jobId.hashCode());
		result = prime * result
				+ ((jobunitId == null) ? 0 : jobunitId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		CollapseItemKey other = (CollapseItemKey) obj;
		if (jobId == null) {
			if (other.jobId != null)
				return false;
		} else if (!jobId.equals(other.jobId))
			return false;
		if (jobunitId == null) {
			if (other.jobunitId != null)
				return false;
		} else if (!jobunitId.equals(other.jobunitId))
			return false;
		
		return true;
	}
}