/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.composite;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ColorConstantsWrapper;
import org.eclipse.draw2d.ConnectionLocator;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.ScalableFreeformLayeredPane;
import org.eclipse.draw2d.ScalableLayeredPane;
import org.eclipse.draw2d.ShortestPathConnectionRouter;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.jobmanagement.composite.WaitRuleComposite;
import com.clustercontrol.jobmanagement.dialog.JobDialog;
import com.clustercontrol.jobmanagement.dialog.WaitRuleDialog;
import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobPropertyUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobUtil;
import com.clustercontrol.jobmanagement.view.ForwardFileView;
import com.clustercontrol.jobmanagement.view.JobNodeDetailView;
import com.clustercontrol.jobmanagement.viewer.JobTreeViewer;
import com.clustercontrol.jobmap.bean.ColorfulAssociation;
import com.clustercontrol.jobmap.editpart.MapViewController;
import com.clustercontrol.jobmap.figure.JobFigure;
import com.clustercontrol.jobmap.figure.JobFigureAnchor;
import com.clustercontrol.jobmap.figure.JobFigureAutoRoute;
import com.clustercontrol.jobmap.util.JobMapActionUtil;
import com.clustercontrol.jobmap.util.JobMapTreeUtil;
import com.clustercontrol.jobmap.view.JobMapEditorView;
import com.clustercontrol.jobmap.view.JobMapHistoryView;
import com.clustercontrol.jobmap.view.JobTreeView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;
import com.clustercontrol.ws.jobmanagement.JobInfo;
import com.clustercontrol.ws.jobmanagement.JobObjectInfo;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;
import com.clustercontrol.ws.jobmanagement.JobWaitRuleInfo;

public class JobMapComposite extends Composite implements ISelectionProvider {

	// ログ
	private static Log m_log = LogFactory.getLog( JobMapComposite.class );

	private FigureCanvas m_canvas;
	private ScalableFreeformLayeredPane m_layer;

	private Shell shell;

	// inner classで利用。
	private JobMapComposite m_composite;

	// 現在フォーカスされている図を保持
	// スコープ、ノードのアイコンもしくは背景
	private JobFigure focusJobFigure = null;

	private PolylineConnection connectionJobFigure = null;

	private MapViewController m_controller;
	
	// 自動調整するか
	private boolean adjust = false;
	// 自動調整直後か
	private boolean adjustDone = false;
	// 自動調整した端数
	private double adjustDecimal = 0.0;

	/**
	 * マウス操作の状態
	 */
	private static enum Status {
		NOT_SELECTED,
		FIGURE_SELECTED_FOR_CREATE_CONNECTION,
		FIGURE_SELECTED_FOR_EMPHASIS_CONNECTION,
	};

	private final JobMapEditorView m_editorView;
	private final JobMapHistoryView m_historyView;

	public double scale = 1.0;

	private String m_managerName;

	/**
	 * インスタンスを返します。
	 * 
	 * @param parent
	 *            親のコンポジット
	 * @param style
	 *            スタイル
	 */
	public JobMapComposite(Composite parent, int style, JobMapEditorView editorView) {
		super(parent, style);
		initialize();
		shell = parent.getShell();
		m_editorView = editorView;
		m_historyView = null;
		m_composite = this;
	}

	public JobMapComposite(Composite parent, int style, JobMapHistoryView historyView) {
		super(parent, style);
		initialize();
		shell = parent.getShell();
		m_editorView = null;
		m_historyView = historyView;
		m_composite = this;
	}

	private void initialize() {
		// キャンバス表示コンポジットをparentの残り領域全体に拡張して表示
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		this.setLayoutData(gridData);

		// キャンバスコンポジット内のレイアウトを設定
		this.setLayout(new FillLayout());

		// 図を配置するキャンバスを生成
		m_canvas = new FigureCanvas(this, SWT.DOUBLE_BUFFERED);

		// 背景(bgimageが存在しない箇所)は白
		m_canvas.setBackground(ColorConstantsWrapper.white());

		// フォーカスリスナを設定
		m_canvas.addFocusListener(new org.eclipse.swt.events.FocusListener(){

			@Override
			public void focusGained(org.eclipse.swt.events.FocusEvent e) {
			}

			@Override
			public void focusLost(org.eclipse.swt.events.FocusEvent e) {
			}

		});

		// スクロール可能なレイヤの作成
		m_layer = new ScalableFreeformLayeredPane();
		m_layer.setLayoutManager(new XYLayout());
		m_canvas.setContents(m_layer);

		// Drop処理
		DropTarget dropTarget = new DropTarget(m_canvas, DND.DROP_MOVE);
		Transfer[] transferTypes = new Transfer[]{TextTransfer.getInstance()};
		dropTarget.setTransfer(transferTypes);
		dropTarget.addDropListener(new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				if (event.data == null
						|| event.data.equals("")
						|| event.data.toString().split(",").length < 3) {
					// 参照元のジョブユニットID、ジョブID、マネージャ名が取得できない場合は処理終了
					return;
				}

				m_log.debug("event.data:" + event.data.toString());
				// 追加する先のジョブユニット・ジョブネット
				JobTreeItem jobTreeItem = m_controller.getCurrentJobTreeItem();
				if (!event.data.toString().split(",")[2].equals(JobTreeItemUtil.getManagerName(jobTreeItem)) 
						|| (!event.data.toString().split(",")[0].equals(jobTreeItem.getData().getJobunitId()) && !m_controller.isDragdropId())) {
					// 同マネージャでない場合は処理終了
					// 同ジョブユニットでない、かつ、参照ジョブドロップ場合は処理終了
					m_log.debug("disp jobitems managerName:" + JobTreeItemUtil.getManagerName(jobTreeItem) 
					+ ", jobunitId:" + jobTreeItem.getData().getJobunitId());
					return;
				}

				// 参照先を検索する
				int dropYCordinate = event.y - m_canvas.toDisplay(m_canvas.getLocation()).y + m_canvas.getViewport().getViewLocation().y;
				int dropXCordinate = event.x - m_canvas.toDisplay(m_canvas.getLocation()).x + m_canvas.getViewport().getViewLocation().x;
				
				if (m_controller.getFigureMap() != null) {
					JobFigure targetFigure = null;
					for (JobFigure figure : m_controller.getFigureMap().values()) {
						if (figure.getJobTreeItem().getData().getType() == JobConstant.TYPE_JOBUNIT
								|| figure.getJobTreeItem().getData().getType() == JobConstant.TYPE_JOBNET){
							Point figurePoint = figure.getLocation();
							int startX = figurePoint.x;
							int startY = figurePoint.y;
							int endX = startX + figure.getSize().width;
							int endY = startY + figure.getSize().height;
						
							if((startX <= dropXCordinate) && (dropXCordinate <= endX)
									&& (startY <= dropYCordinate) && (dropYCordinate <= endY)) {
								if (targetFigure == null
										|| (targetFigure.getSize().width >= figure.getSize().width
										&& targetFigure.getSize().height >= figure.getSize().height)) {
									targetFigure = figure;
								}
							}
						}
					}

					if (targetFigure == null) {
						// ドロップ場所がunit、net以外
						return;
					}
					JobTreeView view = JobMapActionUtil.getJobTreeView();
					JobMapTreeComposite tree = view.getJobMapTreeComposite();

					JobTreeViewer jobtree = tree.getTreeViewer();
					JobTreeItem dragItem = JobMapTreeUtil.getTargetJobTreeItem(jobtree, event.data.toString().split(",")[2], 
							event.data.toString().split(",")[0], event.data.toString().split(",")[1]);
					
					// 追加するジョブネット・ジョブ
					JobTreeItem parentJobTreeItem = targetFigure.getJobTreeItem(); // 追加するところ

					if ((dragItem.getData().getType().equals(JobConstant.TYPE_REFERJOB) 
							|| dragItem.getData().getType().equals(JobConstant.TYPE_REFERJOBNET)) 
							&& !dragItem.getData().getJobunitId().equals(parentJobTreeItem.getData().getJobunitId())) {
						// dropしたものが参照ジョブor参照ジョブネットで、ドロップしたものとドロップした先のjobunitIDが異なる場合はドロップ不可
						m_log.debug("drop target jobtype:" + dragItem.getData().getType() 
								+ ", drop target jobunitId" + dragItem.getData().getJobunitId() 
								+ ", disp jobitems jobunitid:" + parentJobTreeItem.getData().getJobunitId());
						return;
					}
					if ((dragItem.getData().getType().equals(JobConstant.TYPE_REFERJOB) 
							|| dragItem.getData().getType().equals(JobConstant.TYPE_REFERJOBNET)) && !m_controller.isDragdropId()) {
						// dropしたものが参照ジョブor参照ジョブネットで、ドロップ時の挙動が参照コピーの場合、ドロップ不可
						m_log.debug("drop type is refer and copy type is refer return.");
						return;
					}
					
					JobTreeItem assignJobTreeItem = null;
					// trueの場合はコピー、falseは参照
					if (m_controller.isDragdropId()) {
						
						// コピー元のジョブツリーのプロパティーがFullでない場合があるので、
						// ここでコピーしておく。
						JobTreeItem referJobTreeItem = JobUtil.getJobTreeItem(dragItem, // 追加するJobTreeItem
						event.data.toString().split(",")[1]);
						JobTreeItem manager = JobTreeItemUtil.getManager(referJobTreeItem);
						JobPropertyUtil.setJobFullTree(manager.getData().getName(), referJobTreeItem);
						assignJobTreeItem = JobUtil.copy(referJobTreeItem, parentJobTreeItem, 
								parentJobTreeItem.getData().getJobunitId(), referJobTreeItem.getData().getOwnerRoleId());
					} else {
						// 参照元ジョブネット・ジョブを検索する
						JobTreeItem jobunitJobTreeItem  = JobUtil.getTopJobUnitTreeItem(jobTreeItem);// 追加するところのunit
						JobTreeItem referJobTreeItem = JobUtil.getJobTreeItem(jobunitJobTreeItem, // 追加するJobTreeItem
								event.data.toString().split(",")[1]);
						Integer assingnJobType = null;
						if (referJobTreeItem.getData().getType() == JobConstant.TYPE_JOBNET) {
							// 参照ジョブネットの作成
							assingnJobType = JobConstant.TYPE_REFERJOBNET;
						} else {
							// 参照ジョブの作成
							assingnJobType = JobConstant.TYPE_REFERJOB;
						}
						JobInfo assignJobInfo = JobTreeItemUtil.getNewJobInfo(
								parentJobTreeItem.getData().getJobunitId(),
								assingnJobType);
						assignJobInfo.setId(referJobTreeItem.getData().getId());
						assignJobInfo.setName(referJobTreeItem.getData().getName());
						assignJobInfo.setReferJobUnitId(parentJobTreeItem.getData().getJobunitId());
						assignJobInfo.setReferJobId(referJobTreeItem.getData().getId());
						assignJobInfo.setOwnerRoleId(parentJobTreeItem.getData().getOwnerRoleId());
						assignJobInfo.setPropertyFull(true);
						// ジョブIDの名称を変更する
						JobUtil.setReferJobId(assignJobInfo, jobTreeItem);

						assignJobTreeItem = new JobTreeItem();
						assignJobTreeItem.setData(assignJobInfo);
					}

					parentJobTreeItem.getChildren().add(0, assignJobTreeItem);
					assignJobTreeItem.setParent(parentJobTreeItem);
					
					JobEditState editState = JobEditStateUtil.getJobEditState(JobTreeItemUtil.getManagerName(assignJobTreeItem));
					editState.addEditedJobunit(assignJobTreeItem);

					// 画面reload
					tree.refresh(parentJobTreeItem);
					tree.getTreeViewer().setSelection(tree.getTreeViewer().getSelection(), true);
					tree.updateJobMapEditor(null);
				}
			}
		});

		m_controller = new MapViewController(this);
	}

	public void setErrorMessage(String str) {
		Label label = new Label(str);
		label.setText(str);
		label.setVisible(true);
		m_layer.add(label);
		Dimension dimension = new Dimension(-1, -1);
		Point point = new Point(0, 0);
		Rectangle zeroRectangle = new Rectangle(point, dimension);
		m_layer.setConstraint(label, zeroRectangle);
		
		// エラーの場合は縮尺を等倍に戻す
		scale = 1.0;
		adjustDecimal = 0.0;
		adjustDone = false;
		m_layer.setScale(scale);
	}

	public JobFigure drawFigure(Layer layer, JobTreeItem jobTreeItem, int x, int y, boolean collapse) {
		// 図を生成する
		JobFigure figure = new JobFigure(this.m_managerName, jobTreeItem, m_editorView, this, collapse);
		if(jobTreeItem.getData().getType() != JobConstant.TYPE_COMPOSITE) {
			figure.setJob(jobTreeItem);
			figure.draw();
		}

		// モデルとマップの関係を保持
		m_controller.putJobFigure(jobTreeItem.getData().getId(), figure);

		// 配置情報の生成
		Point point = null;
		point = new Point(x, y);
		figure.setPosition(point);

		// サイズは情報がないので、-1を設定
		Dimension dimension = new Dimension(-1, -1);
		Rectangle rectangle = new Rectangle(point, dimension);

		// 図を描画する
		if (layer != null) {
			layer.add(figure);
			layer.setConstraint(figure, rectangle);
		} else {
			m_layer.add(figure);
			m_layer.setConstraint(figure, rectangle);
		}

		// マウスイベントを登録する
		MouseEventListener listener = new MouseEventListener(figure);
		figure.addMouseListener(listener);
		figure.addMouseMotionListener(listener);

		m_layer.setScale(scale);

		return figure;
	}

	public String getManagerName() {
		return m_managerName;
	}
	
	public void drawConnection(ScalableLayeredPane layer, ColorfulAssociation association) {
		JobFigure source = m_controller.getJobFigure(association.getSource());
		JobFigure target = m_controller.getJobFigure(association.getTarget());

		if (source == null || target == null) {
			m_log.debug("source or target is null. " +
					association.getSource() + "," + association.getTarget());
			return;
		}

		// [画面] コネクションを描画
		PolylineConnection connection = new PolylineConnection();
		connection.setAntialias(SWT.ON);

		if (m_controller.isTurnConnection() &&
				((isXyChange() && (target.getPosition().x <= source.getPosition().x)) ||
					!isXyChange() && (target.getPosition().y <= source.getPosition().y))) {
			// 折り返し機能
			JobFigureAutoRoute jRouter = new JobFigureAutoRoute(source, target, isXyChange(), m_layer.getScale());
			connection.setConnectionRouter(jRouter);
			connection.setSourceAnchor(new JobFigureAnchor(source, JobFigureAnchor.TYPE_SOURCE, isXyChange()));
			connection.setTargetAnchor(new JobFigureAnchor(target, JobFigureAnchor.TYPE_TARGET, isXyChange()));
		} else if (m_controller.isDetourConnection()) {
			// 迂回機能
			ShortestPathConnectionRouter router = new ShortestPathConnectionRouter(layer);
			router.setSpacing(5);
			connection.setConnectionRouter(router);
			connection.setSourceAnchor(new JobFigureAnchor(source, JobFigureAnchor.TYPE_SOURCE, isXyChange()));
			connection.setTargetAnchor(new JobFigureAnchor(target, JobFigureAnchor.TYPE_TARGET, isXyChange()));
		
		} else {
			// 5.0.b以前
			connection.setSourceAnchor(new ChopboxAnchor(source.getBackground()));
			connection.setTargetAnchor(new ChopboxAnchor(target.getBackground()));
		}

		// ラベル
		ConnectionLocator locator = new ConnectionLocator(connection, ConnectionLocator.MIDDLE);
		Label label = new Label(association.getLabel());
		label.setForegroundColor(association.getLabelColor());
		connection.add(label, locator);

		// ラベル(target隣接)
		if( association.getTargetAdjacentLabel() != null){
			ConnectionLocator targetAdjacent = new ConnectionLocator(connection, ConnectionLocator.TARGET);
			targetAdjacent.setRelativePosition( PositionConstants.NORTH_WEST);//接点の左上に配置
			targetAdjacent.setGap(5);//接点との間隔調整
			Label targetAdjacentLabel = new Label(association.getTargetAdjacentLabel());
			targetAdjacentLabel.setForegroundColor(ColorConstantsWrapper.black());
			connection.add(targetAdjacentLabel, targetAdjacent);
		}

		// ツールチップ
		connection.setToolTip(association.getToolTip());

		// 見た目を調整
		connection.setForegroundColor(association.getLineColor());
		int width = 3;
		connection.setLineWidth(width);
		PolygonDecoration decoration = new PolygonDecoration();
		decoration.setTemplate(MapViewController.TRIANGLE);
		decoration.setScale(width, width);
		if (m_controller.isAllowWhite()) {
			decoration.setBackgroundColor(ColorConstantsWrapper.white());
		}
		connection.setTargetDecoration(decoration);
		// コネクションは背景画像の上に配置（最背面である背景画像のインデックスは0）
		if (m_controller.isDetourConnection() || layer == null) {
			// 迂回機能を利用する場合、または最上位の階層の場合はm_layerに配置。
			m_layer.add(connection, 1);
		} else {
			// ジョブネットレイヤーに配置。
			layer.add(connection, 1);
		}

		// マウスイベントを登録する
		MouseEventListener listener = new MouseEventListener();
		connection.addMouseListener(listener);
		// [コントローラ] モデルと図（コネクション）の関係を保持
		m_controller.putConnection(association, connection);
	}

	public void setPriority(String jobId, int priority){
		JobFigure figure = m_controller.getJobFigure(jobId);

		if(figure != null){
			figure.setBgColor();
		}
	}

	public void clearCanvas(){
		m_layer.removeAll();
	}

	public void clearMapData() {
		m_controller.clearMapData();
	}

	public void setCanvasFocus() {
		m_canvas.setFocus();
	}

	// draw2D Figure用のイベントリスナ
	private class MouseEventListener extends MouseMotionListener.Stub implements MouseListener {
		// マウス操作の状態を保持
		private Status status = Status.NOT_SELECTED;

		private JobFigure jobFigure = null;

		public MouseEventListener(JobFigure figure){
			jobFigure = figure;
		}

		public MouseEventListener() {

		}

		private PolylineConnection tmpline;

		// ダブルクリック時に呼ばれる
		// ダブルクリックするとジョブ[ノード詳細]ビューを表示する。
		@Override
		public void mouseDoubleClicked(MouseEvent me) {
			Figure figure = (Figure)me.getSource();
			if (figure instanceof JobFigure) {
				JobFigure jobFigure = (JobFigure)figure;
				m_log.debug("! mouse double click  " + figure.toString() +
						"," + jobFigure.getJobTreeItem().getData().getId());
				JobTreeItem jobTreeItem = jobFigure.getJobTreeItem();

				// セッション情報を持っていない(ジョブマップ[設定])の場合
				if (jobTreeItem.getDetail() == null) {
					String managerName = null;
					JobTreeItem mgrTree = JobTreeItemUtil.getManager(jobTreeItem);
					if(mgrTree == null) {
						managerName = jobTreeItem.getChildren().get(0).getData().getId();
					} else {
						managerName = mgrTree.getData().getId();
					}
					
					JobEditState editState = JobEditStateUtil.getJobEditState(managerName);
					boolean readOnly = !editState.isLockedJobunitId(jobTreeItem.getData().getJobunitId());
					JobDialog dialog = new JobDialog(
							JobMapActionUtil.getJobTreeView().getJobMapTreeComposite(),
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							managerName,
							readOnly);
					dialog.setJobTreeItem(jobTreeItem);
					// ダイアログ表示
					if (dialog.open() == IDialogConstants.OK_ID) {
						if (editState.isLockedJobunitId(jobTreeItem.getData().getJobunitId())) {
							// 編集モードのジョブが更新された場合(ダイアログで編集モードになったものを含む）
							editState.addEditedJobunit(jobTreeItem);
							if (jobTreeItem.getData().getType() == JobConstant.TYPE_JOBUNIT) {
								JobUtil.setJobunitIdAll(jobTreeItem, jobTreeItem.getData().getJobunitId());
							}
						}

						// Refresh after modified
						JobTreeView view = JobMapActionUtil.getJobTreeView();
						JobMapTreeComposite tree = view.getJobMapTreeComposite();
						tree.refresh(jobTreeItem.getParent());
						tree.getTreeViewer().setSelection(tree.getTreeViewer().getSelection(), true);
						tree.updateJobMapEditor(null);
					}
				} else {
					int jobType = jobFigure.getJobTreeItem().getData().getType();
					String sessionId = getSessionId();
					String jobunitId = jobTreeItem.getData().getJobunitId();
					String jobId = jobTreeItem.getData().getId();
					if (jobType == JobConstant.TYPE_JOB ||
							jobType == JobConstant.TYPE_APPROVALJOB ||
							jobType == JobConstant.TYPE_MONITORJOB) {
						//アクティブページを手に入れる
						IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
						
						//ジョブ[ファイル転送]ビューを更新する
						IViewPart viewPartDetail = null;
						try {
							viewPartDetail = page.showView(ForwardFileView.ID);
						} catch (PartInitException e) {
							m_log.warn("mouseDoubleClicked() page.showView, " + HinemosMessage.replace(e.getMessage()), e);
						}
						if (viewPartDetail != null) {
							ForwardFileView view = (ForwardFileView) viewPartDetail
							.getAdapter(ForwardFileView.class);
							view.update(m_managerName, sessionId, jobunitId, jobId);
						}
						
						//ジョブ[ノード詳細]ビューを更新する
						viewPartDetail = null;
						try {
							viewPartDetail = page.showView(JobNodeDetailView.ID);
						} catch (PartInitException e) {
							m_log.warn("mouseDoubleClicked() page.showView, " + HinemosMessage.replace(e.getMessage()), e);
						}
						if (viewPartDetail != null) {
							JobNodeDetailView view = (JobNodeDetailView) viewPartDetail
							.getAdapter(JobNodeDetailView.class);
							view.update(m_managerName, sessionId, jobunitId, jobId);
							view.setFocus();
						}
					} else if (jobType == JobConstant.TYPE_FILEJOB){
						//アクティブページを手に入れる
						IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
						
						//ジョブ[ノード詳細]ビューを更新する
						IViewPart viewPartDetail = null;
						try {
							viewPartDetail = page.showView(JobNodeDetailView.ID);
						} catch (PartInitException e) {
							m_log.warn("mouseDoubleClicked() page.showView, " + HinemosMessage.replace(e.getMessage()), e);
						}
						if (viewPartDetail != null) {
							JobNodeDetailView view = (JobNodeDetailView) viewPartDetail
							.getAdapter(JobNodeDetailView.class);
							view.update(m_managerName, sessionId, jobunitId, jobId);
							view.setFocus();
						}
						
						//ジョブ[ファイル転送]ビューを更新する
						viewPartDetail = null;
						try {
							viewPartDetail = page.showView(ForwardFileView.ID);
						} catch (PartInitException e) {
							m_log.warn("mouseDoubleClicked() page.showView, " + HinemosMessage.replace(e.getMessage()), e);
						}
						if (viewPartDetail != null) {
							ForwardFileView view = (ForwardFileView) viewPartDetail
							.getAdapter(ForwardFileView.class);
							view.update(m_managerName, sessionId, jobunitId, jobId);
							view.setFocus();
						}
					}
				}
			} else {
				m_log.debug("mouse double cliek  " +
						figure.getClass().getSimpleName());
			}
			// イベントを消費
			me.consume();
		}

		@Override
		public void mousePressed(MouseEvent me) {
			Figure figure = (Figure)me.getSource();
			if (figure instanceof JobFigure) {
				if(m_log.isDebugEnabled()){
					m_log.debug("mousePressed . figure id=" + ((JobFigure)figure).getJobTreeItem().getData().getId() + " hasFocus="+focusJobFigure.hasFocus() );
				}
				boolean isfocusChange = false;
				if(focusJobFigure.equals( (JobFigure)figure) ==false){
					isfocusChange = true;
				}
				focusJobFigure = (JobFigure)figure;
				if( focusJobFigure.hasFocus()==false){
					focusJobFigure.requestFocus();
				}else if(isfocusChange){
					// handleFocusGainedがmousePressedを追い越すケースへの対応（検索ボックスからのフォーカスチェンジを想定）
					// 内部設定と対象フォーカス状態をチェック、必要ならイベント処理を直接起動
					focusJobFigure.handleFocusGained(null);
				}
			} else if (figure instanceof PolylineConnection) {
				connectionJobFigure = (PolylineConnection)figure;
				if (me.button == 1) {
					// 左クリックの場合
					if (status == Status.NOT_SELECTED) {
						if (tmpline != null) {
							m_layer.remove(tmpline);
							tmpline = null;
						}
						// 選択した線の2倍の太さの線で描画する
						tmpline = new PolylineConnection();
						tmpline.setAntialias(SWT.ON);
						int width = 6;
						tmpline.setLineWidth(width);
						PolygonDecoration decoration = new PolygonDecoration();
						decoration.setTemplate(MapViewController.TRIANGLE);
						decoration.setScale(width, width);
						if (m_controller.isAllowWhite()) {
							decoration.setBackgroundColor(ColorConstantsWrapper.white());
						}
						tmpline.setTargetDecoration(decoration);
						tmpline.setForegroundColor(connectionJobFigure.getForegroundColor());
						tmpline.setPoints(connectionJobFigure.getPoints());
						// ラベル
						ConnectionLocator locator = new ConnectionLocator(tmpline, ConnectionLocator.MIDDLE);
						String labelText = "";
						if (connectionJobFigure.getChildren() != null) {
							for (Object obj : connectionJobFigure.getChildren()) {
								if (obj instanceof Label) {
									labelText = ((Label)obj).getText();
									break;
								}
							}
						}
						Label label = new Label(labelText);
						label.setOpaque(true);
						tmpline.add(label, locator);

						m_layer.add(tmpline);
						status = Status.FIGURE_SELECTED_FOR_EMPHASIS_CONNECTION;
					}
				}
			}

			m_log.debug("me.button=" + me.button);


			// 左クリックの場合
			if (me.button == 1){
				// マウスクリックした際に選択されているFigureが、JobFigureの場合
				if(figure instanceof JobFigure && m_editorView != null) {
					// 編集モードのジョブが選択されている場合
					if (focusJobFigure.isLockedJob()) {
						// コネクションの始点が選択された状態
						status = Status.FIGURE_SELECTED_FOR_CREATE_CONNECTION;
					}
				}
			}

			// 右クリックの場合
			if (me.button == 3){
				if (figure instanceof PolylineConnection && m_editorView != null) {
					// 待ち条件を設定しているジョブが編集モードであるかを調べる
					JobFigure waitSourceJob = m_controller.getJobFigure(m_controller.getConnection((PolylineConnection)figure).getSource());
					if (waitSourceJob.isLockedJob()) {
						// 編集モードのときのみPOPUPメニュー設定
						Menu menu = new Menu(shell, SWT.POP_UP);
						m_canvas.setMenu(menu);
						MenuItem item = new MenuItem(menu, SWT.PUSH);
						item.setText(Messages.getString("wait.rule") +
								Messages.getString("delete"));
						item.addSelectionListener(new SelectionListener() {
							@Override
							public void widgetDefaultSelected(SelectionEvent event) {
							}
							@Override
							public void widgetSelected(SelectionEvent event) {
								// 該当ジョブの待ち条件を消す。
								if (waitSourceJob.isLockedJob()) {
									m_controller.removeConnection(connectionJobFigure);
									update();
								}
							}
						});
						// メニューを表示
						//shell.setMenu(menu);
						//menu.setVisible(true);
						
					} else {
						// 編集モードを終了した後に「待ち条件削除」のコンテキストメニューが出ることがあるので
						// ロックを取得していない場合はコンテキストメニューを見えなくする
						Menu menu = m_canvas.getMenu();
						if (menu != null) {
							MenuItem[] menuItems = menu.getItems();
							for (MenuItem item : menuItems) {
								item.dispose();
							}
						}
					}
				} else if (figure instanceof JobFigure) {
					MenuManager menuManager = new MenuManager();
					menuManager.setRemoveAllWhenShown(true);
					if (m_editorView != null) {
						Menu menu = menuManager.createContextMenu(m_composite);
						m_canvas.setMenu(menu);
						m_editorView.getSite().registerContextMenu(menuManager, m_editorView.getCanvasComposite());
					} else {
						Menu menu = menuManager.createContextMenu(m_historyView.getCanvasComposite());
						m_canvas.setMenu(menu);
						m_historyView.getSite().registerContextMenu(menuManager, m_historyView.getCanvasComposite());
					}
				}
			}
			me.consume();
		}

		// マウスを離した時に呼ばれる
		@Override
		public void mouseReleased(MouseEvent me) {
			if(status == Status.FIGURE_SELECTED_FOR_CREATE_CONNECTION){
				status = Status.NOT_SELECTED;

				// マウスの位置を取得
				Point mousePoint = me.getLocation();

				// 線分を消去
				if(tmpline != null){
					jobFigure.getParent().remove(tmpline);
					tmpline = null;
				}
				

				/*
				 * コネクションを描く。
				 */
				// 先行ジョブ
				JobTreeItem firstItem = jobFigure.getJobTreeItem();
				String firstJobId = firstItem.getData().getId();
				// 後続ジョブ
				JobFigure secondFigure = m_controller.getJobFigure(mousePoint);
				if (secondFigure != null) {
					JobTreeItem secondItem = secondFigure.getJobTreeItem();
					String secondJobId = secondItem.getData().getId();
					m_log.debug("start=" + firstJobId + ", end=" + secondJobId);

					// 同階層のジョブのみ待ち条件に指定可能。
					boolean flag = false;
					m_log.debug("secondItem=" + secondItem);
					m_log.debug("secondItem=" + secondItem.getParent().getChildren());
					for (JobTreeItem item : secondItem.getParent().getChildren()) {
						if (secondJobId.equals(item.getData().getId())) {
							continue;
						}
						if (firstJobId.equals(item.getData().getId())) {
							flag = true;
							break;
						}
					}
					if (flag) {
						// まずFullJobする(5.0.cの改善で表示されてもfullJobされてないケースがあるため)
						JobPropertyUtil.setJobFull(m_managerName, secondItem.getData());
						
						WaitRuleDialog dialog = new WaitRuleDialog(shell, secondItem);
						ArrayList<Object> list = new ArrayList<Object>();
						list.add(JudgmentObjectConstant.TYPE_JOB_END_STATUS);
						list.add(firstJobId);
						list.add(EndStatusMessage.typeToString(EndStatusConstant.TYPE_NORMAL));
						list.add("");
						list.add("");
						list.add("");
						list.add("");
						list.add("");
						dialog.setInputData(list);
						if (dialog.open() == IDialogConstants.OK_ID) {
							JobWaitRuleInfo jobWaitRuleInfo = secondItem.getData().getWaitRule();
							List<JobObjectInfo> infoList = jobWaitRuleInfo.getObject();
							ArrayList<?> inputList = dialog.getInputData();
							m_log.debug("0:" + inputList.get(0));
							m_log.debug("1:" + inputList.get(1));
							m_log.debug("2:" + inputList.get(2));

							JobObjectInfo newInfo = WaitRuleComposite.array2JobObjectInfo(inputList);
							m_log.debug("id=" + newInfo.getJobId() + "," + newInfo.getType() + "," +
									newInfo.getValue());
							for (JobObjectInfo i : infoList) {
								m_log.debug("i " + i.getJobId() + "," + i.getType() + "," +
										i.getValue());
								// 待ち条件に複数の時刻は設定できない。
								if (i.getJobId() == null && newInfo.getJobId() == null) {
									MessageDialog.openInformation(null, Messages.getString("message"),
											Messages.getString("message.job.61"));
									return;
								}
								// 待ち条件が時刻の場合は、重複チェック対象としない。
								if (i.getJobId() == null) {
									continue;
								}
								if (!i.getJobId().equals(newInfo.getJobId())) {
									continue;
								}
								if (!i.getType().equals(newInfo.getType())) {
									continue;
								}
								if (!i.getValue().equals(newInfo.getValue())) {
									continue;
								}
								// 重複した待ち条件は設定できない。
								MessageDialog.openInformation(null, Messages.getString("message"),
										com.clustercontrol.jobmap.messages.Messages.getString("wait.rule.duplicate"));
								return;
							}
							m_log.debug("new JobObjectInfo");
							infoList.add(newInfo);

							// ジョブツリーに変更済みフラグを立てる。
							JobEditState editState = JobEditStateUtil.getJobEditState(JobTreeItemUtil.getManagerName(secondItem));
							editState.addEditedJobunit(secondItem);

							m_composite.update();
						}
					}
				}
			} else if (status == Status.FIGURE_SELECTED_FOR_EMPHASIS_CONNECTION){
				status = Status.NOT_SELECTED;

				// 線分を消去
				if(tmpline != null){
					m_layer.remove(tmpline);
					tmpline = null;
				}
			}

			// イベントを消費
			me.consume();
		}


		// ドラッグ時に呼ばれる
		@Override
		public void mouseDragged(MouseEvent me) {
			//m_log.info("mouseDragged() : start");
			if (status == Status.FIGURE_SELECTED_FOR_CREATE_CONNECTION) {
				// マウスポインタの位置を取得
				Point mousePoint = me.getLocation();
				adjustPoint(mousePoint, jobFigure.getParent());
				// コネクションの張り先を明示させるための線分を表示
				// 線分を消去
				if(tmpline != null){
					jobFigure.getParent().remove(tmpline);
					tmpline = null;
				}
				
				/*
				for (JobTreeItem item : jobFigure.getJobTreeItem().getParent().getChildren()) {
					JobFigure figure = m_controller.getJobFigure(item.getData().getId());
					figure.m_backGround.setLineWidth(3);
				}
				*/
				
				tmpline = new PolylineConnection();
				tmpline.setAntialias(SWT.ON);
				int width = 3;
				tmpline.setLineWidth(width);
				PolygonDecoration decoration = new PolygonDecoration();
				decoration.setScale(width * 2, width * 2);
				tmpline.setTargetDecoration(decoration);

				Point point = new Point(focusJobFigure.getBackground().getLocation().x 
						+ focusJobFigure.getBackground().getSize().width/2,
						focusJobFigure.getBackground().getLocation().y 
						+ focusJobFigure.getBackground().getSize().height/2);
				m_log.debug("mouseDragged point=" + point);
				tmpline.setStart(point);
				tmpline.setEnd(mousePoint);

				jobFigure.getParent().add(tmpline);
			}

			// イベントを消費
			me.consume();
			//m_log.info("mouseDragged() : end");
		}

		/**
		 * 待ち条件の矢印がジョブマップよりも外に出ないように調整するメソッド。
		 */
		private void adjustPoint (Point point, IFigure iFigure) {
			int x = iFigure.getBounds().x;
			int y = iFigure.getBounds().y;
			int width = iFigure.getBounds().width;
			int height = iFigure.getBounds().height;

			if (point.x < x) {
				point.x = x;
			} else if (point.x > x + width) {
				point.x = x + width;
			}
			if (point.y < y) {
				point.y = y;
			} else if (point.y > y + height) {
				point.y = y + height;
			}
		}

	}

	@Override
	public void update() {
		m_controller.updateMap();
	}

	private void applyScale() {
		m_layer.setScale(scale);
	}

	public void update(String sessionId, JobTreeItem jobTreeItem) {
		if (m_controller == null) {
			m_log.debug("update : m_controller is null");
			return;
		}
		long start = new Date().getTime();
		
		if (jobTreeItem != null) {
			JobTreeItem mgrTree = JobTreeItemUtil.getManager(jobTreeItem);
			if(mgrTree == null) {
				m_managerName = jobTreeItem.getChildren().get(0).getData().getId();
			} else {
				m_managerName = mgrTree.getData().getId();
			}
		}
		
		try {
			m_controller.updateMap(m_managerName, sessionId, jobTreeItem);
			focusJobFigure = m_controller.getRootJobFigure();
			if (focusJobFigure != null) {
				focusJobFigure.requestFocus();
			}

		} catch (InvalidRole_Exception e) {
			m_log.error(HinemosMessage.replace(e.getMessage()), e);
			if (ClientSession.isDialogFree()) {
				ClientSession.occupyDialog();
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
				ClientSession.freeDialog();
			}
		} catch (Exception e) {
			m_log.error(HinemosMessage.replace(e.getMessage()), e);
			if (ClientSession.isDialogFree()) {
				ClientSession.occupyDialog();
				MessageDialog.openInformation(null, Messages.getString("message"), HinemosMessage.replace(e.getMessage()));
				ClientSession.freeDialog();
			}
		}
		m_log.debug("CanvasComposite update end. time=" +
				(new Date().getTime() - start) +  "ms");
	}

	public JobFigure getFocusFigure() {
		return focusJobFigure;
	}

	public String getSessionId () {
		return m_controller.getSessionId();
	}


	public void zoomIn() {
		if (adjustDone) {
			scale += adjustDecimal;
		} else {
			if (scale < 1) {
				scale += 0.1;
			}
		}
		adjustDone = false;
		
		applyScale();
	}

	public void zoomOut() {
		if (adjustDone) {
			scale += adjustDecimal;
		}
		if (scale < 0.2) {
			// 0.1 が最小。
		} else {
			scale -= 0.1;
		}
		adjustDone = false;
		
		applyScale();
	}
	
	public void zoomAdjust() {
		org.eclipse.swt.graphics.Point point = getSize();
		m_log.debug("size.x:"+point.x+",size.y:"+point.y);
		double scale_x = (double)point.x / m_controller.maxX;
		double scale_y = (double)point.y / m_controller.maxY;
		if (scale_x < scale_y) {
			scale = scale_x;
		} else {
			scale = scale_y;
		}
		
		if (scale > 1) {
			scale = 1.0;
		}
		if (scale < 0.1) {
			scale = 0.1;
		}
		
		adjustDone = true;
		// 小数第二桁以下を端数として保存しておく
		adjustDecimal = Math.ceil(scale*10) / 10 - scale;
		m_log.debug(scale+","+adjustDecimal);
		
		applyScale();
	}


	public boolean isXyChange() {
		return m_controller.isXyChange();
	}

	public void setXyChange(boolean xyChange) {
		m_controller.setXyChange(xyChange);
	}

	public void applySetting() {
		m_controller.applySetting();
	}

	public void update(String managerName, String sessionId,
			JobTreeItem jobTreeItem) {
		if (managerName != null) {
			m_managerName = managerName;
		}
		
		update(sessionId, jobTreeItem);
	}

	public void addCollapseItem(JobTreeItem jobTreeItem) {
		m_controller.addCollapseItem(jobTreeItem);
	}

	public void removeCollapseItem(JobTreeItem jobTreeItem) {
		m_controller.removeCollapseItem(jobTreeItem);
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
	}

	@Override
	public ISelection getSelection() {
		return focusJobFigure;
	}

	@Override
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
	}

	@Override
	public void setSelection(ISelection selection) {
	}

	public void emphasisConnection(String jobId) {
		m_controller.emphasisConnection(jobId);
	}

	public boolean isZoomAdjust() {
		return adjust;
	}
	
	public void setZoomAdjust(boolean adjust) {
		this.adjust = adjust;
	}

	private void searchJob(String jobId) {
		if(m_log.isDebugEnabled()){
			m_log.debug("searchJob start " + jobId);
		}
		JobFigure target = m_controller.getJobFigure(jobId);
		Point p = target.getLocation();
		this.focusJobFigure = target;
		//フォーカスセット
		if(target.hasFocus()==false){
			focusJobFigure.requestFocus();
		}else{
			//フォーカスをすでに持っているならフォーカス取得時の処理を直接起動
			target.handleFocusGained(null);
		}
		m_canvas.scrollSmoothTo(p.x, p.y);
		if(m_log.isDebugEnabled()){
			m_log.debug("searchJob end " +jobId);
		}
	}
	
	/**
	 * 対象Jobについて フォーカスして表示する
	 * もし 折りたたみ表示の内部にある場合は 関連する部分は展開する
	 */
	private void displayTargetJob(JobTreeItem Item) {
		boolean expandResult = m_controller.doExpandParentDispary(Item);
		if(expandResult){
			searchJob(Item.getData().getId());
		}else{
			m_log.debug("displayTargetJob is abnormal end. target is not in display root. " + Item.getData().getId());
		}
	}
	
	/**
	 * 指定されたジョブに隣接しているジョブをキーワード検索する
	 */
	private JobTreeItem searchNeighbors( JobTreeItem current, String keyword ){
		JobTreeItem found;
		JobTreeItem parent = current.getParent();
		//親がないか 表示の最上位なら取りやめ
		if( null != parent &&  current.equals(m_controller.getCurrentJobTreeItem())==false) {
			do{
				int offset = parent.getChildren().indexOf( current ) + 1;
				found = searchChildren( parent, keyword, offset );
				if( null != found ){
					return found;
				}
				current = parent;
				parent = current.getParent();
				if(current.equals(m_controller.getCurrentJobTreeItem())){
					break;
				}
			}while( null != parent );
		}
		return null;
	}

	/**
	 * 指定されたジョブの子をキーワード検索する
	 */
	private JobTreeItem searchChildren( JobTreeItem parent, String keyword, int offset ){
		List<JobTreeItem> children = parent.getChildren();
		int len = children.size();
		for( int i = offset; i<len; i++ ){
			JobTreeItem child = children.get(i);

			if( -1 != child.getData().getId().indexOf( keyword ) ){
				return child;
			}else{
				JobTreeItem found = searchChildren( child, keyword, 0 );
				if( null != found ){
					return found;
				}
			}
		}
		return null;
	}

	/**
	 * 指定されたジョブのキーワード検索する
	 * 
	 * 連続検索に対応している
     *
	 */
	private JobTreeItem searchItem( JobTreeItem currentItem, String keyword ){
		JobTreeItem found;

		// 1. If no current item ,  search root 
		if( currentItem == null ){
			currentItem = m_controller.getCurrentJobTreeItem();
			if( -1 != currentItem.getData().getId().indexOf( keyword )){
				m_log.debug("searchItem . current null . result id=" + currentItem.getData().getId());
				return currentItem;
			}
		}
		
		// 2. Search children
		found= searchChildren(currentItem, keyword, 0);
		if( null != found ){
			m_log.debug("searchItem . searchChildren . result id=" + found.getData().getId());
			return found;
		}	

		// 3. If not found in children, search in neighbors
		//m_controller.getCurrentJobTreeItem()
		found = searchNeighbors( currentItem, keyword );
		if( null != found ){
			m_log.debug("searchItem . searchNeighbors . result id=" + found.getData().getId());
			return found;
		}

		return null;
	}
	/*
	 * 現在のフォーカスを元に連続検索
	 */
	public void doSearch( String keyword ){
		// Check and format keyword
		if( null == keyword ){
			return;
		}
		keyword = keyword.trim();
		if( keyword.isEmpty() ){
			return;
		}
		//表示がなにもなければ処理しない
		if( m_controller.getFigureMap().size() == 0 ){
			if(m_log.isDebugEnabled()){
				m_log.debug("doSearch . figure is noting  . " );
			}
			MessageDialog.openInformation( this.getShell(), Messages.getString("message"), Messages.getString("search.not.found") );
			return;
		}
		
		JobTreeItem curItem = null;
		if( null != focusJobFigure){ 
			curItem = focusJobFigure.getJobTreeItem();
		}else{
			m_log.debug("doSearch . currnet nothing " );
		}
		JobTreeItem result = searchItem( curItem, keyword );
		if( null != result ){
			displayTargetJob(result);
		}else{
			//該当がなければ 、ヒットなしメッセージを表示
			MessageDialog.openInformation( this.getShell(), Messages.getString("message"), Messages.getString("search.not.found") );
			//最上位にフォーカスを戻す（次の検索のために）
			if( m_controller.getCurrentJobTreeItem() != null ){
				if(m_controller.getCurrentJobTreeItem().getData()!=null){
					//エディタ向けロジック
					displayTargetJob(m_controller.getCurrentJobTreeItem());
				}else{
					//ビューワ向けロジック
					List<JobTreeItem> topChildList = m_controller.getCurrentJobTreeItem().getChildren();
					if( topChildList.size() > 0 && topChildList.get(0).getData() != null ){
						displayTargetJob(topChildList.get(0));
					} 
				}
			}
		}
	}
}
