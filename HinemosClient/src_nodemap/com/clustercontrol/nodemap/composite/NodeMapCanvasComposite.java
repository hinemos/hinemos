/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.composite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ColorConstantsWrapper;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.ScalableLayeredPane;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.AssignNodeScopeRequest;
import org.openapitools.client.model.FacilityElementResponse;
import org.openapitools.client.model.MapAssociationInfoResponse;
import org.openapitools.client.model.NodeInfoResponse;
import org.openapitools.client.model.PingResultResponse;
import org.openapitools.client.model.FacilityInfoResponse.FacilityTypeEnum;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.accesscontrol.dialog.ObjectPrivilegeListDialog;
import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.collect.view.CollectGraphView;
import com.clustercontrol.dialog.TextAreaDialog;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.nodemap.bean.ReservedFacilityIdConstant;
import com.clustercontrol.nodemap.dialog.RegisterImageDialog;
import com.clustercontrol.nodemap.editpart.MapViewController;
import com.clustercontrol.nodemap.figure.BgFigure;
import com.clustercontrol.nodemap.figure.FacilityFigure;
import com.clustercontrol.nodemap.figure.FileImageFigure;
import com.clustercontrol.nodemap.figure.NodeFigure;
import com.clustercontrol.nodemap.figure.ScopeFigure;
import com.clustercontrol.nodemap.preference.NodeMapPreferencePage;
import com.clustercontrol.nodemap.util.RelationViewController;
import com.clustercontrol.nodemap.view.NodeMapView;
import com.clustercontrol.nodemap.view.NodeMapView.Mode;
import com.clustercontrol.repository.action.DeleteNodeProperty;
import com.clustercontrol.repository.action.DeleteScopeProperty;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.dialog.NodeAssignDialog;
import com.clustercontrol.repository.dialog.NodeCreateDialog;
import com.clustercontrol.repository.dialog.NodeReleaseDialog;
import com.clustercontrol.repository.dialog.ScopeCreateDialog;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

public class NodeMapCanvasComposite extends Composite{

	// ログ
	private static Log m_log = LogFactory.getLog( NodeMapCanvasComposite.class );

	private FigureCanvas m_canvas;
	private ScalableLayeredPane m_layer;

	private Shell shell;

	private NodeMapView _view;
	
	// メッセージを表示しているかどうか(false:表示していない、true:表示している)
	private boolean message_flg = false;

	// 現在フォーカスされている図を保持
	// スコープ、ノードのアイコンもしくは背景
	private FileImageFigure focusFigure = null;
	
	// ファシリティIDと重要度のキャッシュ（マネージャにアクセスせずに再描画するとき用）
	private ConcurrentHashMap<String, Integer>facilityPriorityCache =new ConcurrentHashMap<String, Integer>();
	
	protected NodeMapSearchBarComposite m_searchBar;
	private Composite mapComposite;

	/*
	 * グリッド
	 */
	private boolean gridSnapFlg;
	private int gridWidth;
	
	/*
	 * 倍率
	 */
	private double scale = 1.0;
	private boolean adjustFlag = false;
	private double adjustDecimal = 0.0;
	private int sizeX;
	private int sizeY;

	private MapViewController m_controller;
	
	private String m_managerName;

	/** 区切り文字(#!#) */
	private static final String SEPARATOR_HASH_EX_HASH = "#!#";
	/**
	 * マウス操作の状態
	 */
	private static enum Status {
		NOT_SELECTED,
		FIGURE_SELECTED_FOR_MOVE,
		FIGURE_SELECTED_FOR_CREATE_CONNECTION,
		FIGURE_SELECTED_FOR_NOT_MOVE
	};

	/**
	 * インスタンスを返します。
	 * 
	 * @param parent
	 *            親のコンポジット
	 * @param style
	 *            スタイル
	 */
	public NodeMapCanvasComposite(Composite parent, int style, NodeMapView view) {
		super(parent, style);
		shell = parent.getShell();
		_view = view;
		initialize();

	}

	private void initialize() {
		applySetting();
		this.setLayout(new GridLayout(1, false));
		m_searchBar = new NodeMapSearchBarComposite(this ,SWT.NONE, true);
		m_searchBar.setLayoutData( new GridData(GridData.GRAB_HORIZONTAL) );
		m_searchBar.setNodeMapCanvasComposite(this);

		mapComposite= new Composite(this, SWT.NONE);
		// キャンバス表示コンポジットをparentの残り領域全体に拡張して表示
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		mapComposite.setLayoutData(gridData);
		this.setLayoutData(gridData);

		// キャンバスコンポジット内のレイアウトを設定
		mapComposite.setLayout(new FillLayout());

		// 図を配置するキャンバスを生成
		m_canvas = new FigureCanvas(mapComposite, SWT.DOUBLE_BUFFERED);

		// 背景(bgimageが存在しない箇所)は灰色
		m_canvas.setBackground(ColorConstantsWrapper.lightGray());

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
		m_layer = new ScalableLayeredPane();
		m_layer.setLayoutManager(new XYLayout());
		m_canvas.setContents(m_layer);
		
		
		DropTarget dropTarget = new DropTarget(m_canvas, DND.DROP_MOVE);
		Transfer[] transferTypes = new Transfer[] {TextTransfer.getInstance()};
		dropTarget.setTransfer(transferTypes);
		dropTarget.addDropListener(new DropTargetListener() {
			@Override
			public void dropAccept(DropTargetEvent event) {
			}
			
			@Override
			public void drop(DropTargetEvent event) {
				String scopeId = m_controller.getCurrentScope();
				String nodeId = event.data.toString();
				
				//すでに存在する場合に、何もしない。
				if (m_controller.getFacilityFigure(nodeId) != null) {
					return;
				}
				
				// 編集中の情報を解除してよいか確認
				if (_view.isEditing()) {
					if (!MessageDialog.openQuestion(
							null,
							Messages.getString("confirmed"),
							com.clustercontrol.nodemap.messages.Messages.getString("edit.refresh.confirm"))) {
						return;
					}
				}
				
				m_log.debug(String.format("Drop node %s to scope %s", nodeId, scopeId));
				m_log.debug(String.format("Drop x=%d, y=%d", event.x, event.y));
				
				RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(m_managerName);
				List<String> nodeIdList = new ArrayList<String>();
				nodeIdList.add(nodeId);
				try {
					AssignNodeScopeRequest assignNodeScopeRequest = new AssignNodeScopeRequest();
					assignNodeScopeRequest.setFacilityIdList(nodeIdList);
					wrapper.assignNodeScope(scopeId, assignNodeScopeRequest);
				} catch (Exception e) {
					m_log.error(e);
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							e.getMessage());
				}
				
				// リポジトリキャッシュの更新
				ClientSession.doCheck();
				
				_view.reload();
			}
			
			@Override
			public void dragOver(DropTargetEvent event) {
			}
			
			@Override
			public void dragOperationChanged(DropTargetEvent event) {
			}
			
			@Override
			public void dragLeave(DropTargetEvent event) {
			}
			
			@Override
			public void dragEnter(DropTargetEvent event) {
				if (m_controller.isMapBuiltin() || !_view.isEditableMode()) {
					event.detail = DND.DROP_NONE;
				}
			}
		});
	}


	/*
	 *  設定情報反映
	 */
	public void applySetting() {
		IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();
		m_log.info("applySetting" +
				", updateFlg=" + store.getBoolean(NodeMapPreferencePage.P_HISTORY_UPDATE_FLG) +
				", interval=" + store.getInt(NodeMapPreferencePage.P_HISTORY_UPDATE_CYCLE)
		);
		gridSnapFlg = store.getBoolean(NodeMapPreferencePage.P_GRID_SNAP_FLG);
		gridWidth = store.getInt(NodeMapPreferencePage.P_GRID_WIDTH);
	}

	public void drawFigure(FacilityElementResponse element) throws Exception {
		// 図を生成する
		FacilityFigure figure = null;
		if(FacilityConstant.TYPE_SCOPE_STRING.equals(element.getTypeName())) {  // スコープの場合
			figure = new ScopeFigure(getManagerName(), element);
			if (element.getIconImage() == null || element.getIconImage().equals("")) {
				figure.draw("scope");
			} else {
				figure.draw(element.getIconImage());
			}
		} else {  // ノードの場合
			figure = new NodeFigure(getManagerName(), element);
			figure.draw(element.getIconImage());
		}

		// モデルとマップの関係を保持
		m_controller.putFacilityFigure(element.getFacilityId(), figure);

		// 配置情報の生成
		Point point = new Point(element.getX(), element.getY());

		// サイズは情報がないので、-1を設定
		Dimension dimension = new Dimension(-1, -1);
		Rectangle rectangle = new Rectangle(point, dimension);

		// 図を描画する
		m_layer.add(figure);
		m_layer.setConstraint(figure, rectangle);

		// マウスイベントを登録する
		MouseEventListener listener = new MouseEventListener(element);
		figure.addMouseListener(listener);
		figure.addMouseMotionListener(listener);

	}


	public void drawBgImage(String filename) throws Exception {

		BgFigure bgFigure = new BgFigure(getManagerName(), 
				m_controller.getCurrentScope().equals("") ? getManagerName() : m_controller.getCurrentScope(), 
				m_controller.getMapName(), 
				m_controller.getOwnerRoleId(),
				m_controller.isMapBuiltin());
		bgFigure.draw(filename);

		// 図を描画する
		m_layer.add(bgFigure);
		Dimension dimension = new Dimension(-1, -1);
		Point point = new Point(0,0);
		Rectangle rectangle = new Rectangle(point, dimension);
		m_layer.setConstraint(bgFigure, rectangle);
		
		sizeX = bgFigure.getWidth();
		sizeY = bgFigure.getHeight();
		m_log.debug("background:sizeX:"+sizeX+",sizeY:"+sizeY);

		// マウスイベントを登録する
		MouseEventListener listener = new MouseEventListener(null);
		bgFigure.addMouseListener(listener);

		focusFigure = bgFigure;

	}

	public void drawConnection(MapAssociationInfoResponse association) {
		FacilityFigure source = m_controller.getFacilityFigure(association.getSource());
		FacilityFigure target = m_controller.getFacilityFigure(association.getTarget());

		// [画面] コネクションを描画
		PolylineConnection connection = new PolylineConnection();
		connection.setAntialias(SWT.ON);
		connection.setSourceAnchor(new ChopboxAnchor(source.getBackGround()));
		connection.setTargetAnchor(new ChopboxAnchor(target.getBackGround()));
		
		Color color = new Color(null, 120, 120, 120);
		int width;
		switch (association.getType()) {
		case NORMAL:
			width = 3;
			break;
		case NEW:
			width = 6;
			break;
		case REMOVE:
			width = 1;
			break;
		default:
			width = 3;
		}
		connection.setForegroundColor(color);
		connection.setLineWidth(width);

		m_layer.add(connection, 1);  // コネクションは背景画像の上に配置（最背面である背景画像のインデックスは0）

		// [コントローラ] モデルと図（コネクション）の関係を保持
		m_controller.putConnection(association, connection);
	}

	public void removeConnection(MapAssociationInfoResponse association) {
		// モデルに該当するコネクションを取得
		PolylineConnection connection = m_controller.getConnection(association);

		if(connection != null){
			// ノードマップ図から削除
			m_layer.remove(connection);

			// モデルと図（コネクション）の関係を更新
			m_controller.removeConnection(association);
		}
	}

	public void setPriority(String facilityId, int priority){
		FacilityFigure figure = m_controller.getFacilityFigure(facilityId);
		facilityPriorityCache.put(facilityId, priority);
		
		if(figure != null){
			figure.setPriority(priority);
		}
	}
	
	// 現在の重要度を適用するためのメソッド（マネージャにアクセスせずに再描画する時用）
	public void setPriorityNotManagerAccess(String facilityId) {
		FacilityFigure figure = m_controller.getFacilityFigure(facilityId);

		if(figure != null){
			// キャッシュにある前回マネージャから取得した重要度の情報を設定する
			Integer priority = facilityPriorityCache.get(facilityId);
			if (priority == null) {
				priority = PriorityConstant.TYPE_NONE;
			}
			figure.setPriority(priority);
		}
	}

	public void clearCanvas(){
		m_layer.removeAll();
	}

	// 個別に設定したメニューが残っている場合があるのでその場合は消しておく
	public void setEnabled(boolean enable) {
		Menu menu = m_canvas.getMenu();

		if (menu != null) {
			MenuItem[] menuItems = menu.getItems();

			for (MenuItem item : menuItems) {
				if (item.getText()
						.equals(com.clustercontrol.nodemap.messages.Messages
								.getString("select.contextmenu.collectgraph"))
						|| item.getText().equals(
								com.clustercontrol.nodemap.messages.Messages.getString("select.contextmenu.ping"))) {
					item.setEnabled(true);
				} else {
					item.setEnabled(enable);
				}
			}
		}
	}

	public void setCanvasFocus() {
		if(m_controller != null){
			m_log.debug("call setFocus() " + m_controller.getCurrentScope());
		}
		
		m_canvas.setFocus();
	}
	
	/**
	 * 縮尺を適用する
	 */
	public void applyScale() {
		m_layer.setScale(scale);
	}

	// draw2D Figure用のイベントリスナ
	private class MouseEventListener extends MouseMotionListener.Stub implements MouseListener {
		// マウス操作の状態を保持
		private Status status = Status.NOT_SELECTED;

		private FacilityElementResponse element;

		// 図を移動させる際のマウスポインタと図の座標の差分
		private Dimension delta = null;

		public MouseEventListener(FacilityElementResponse element){
			this.element = element;
		}

		Polyline tmpline;

		// クリックした時に呼ばれる
		@Override
		public void mousePressed(MouseEvent me) {
			m_log.debug("Mouse Pressed : "  + me.button +
					" " + me.getState() + " "
					+ ((Figure) me.getSource()).hasFocus());
			Figure figure = (Figure)me.getSource();
			m_log.debug("Mouse Pressed : "  + figure);

			// 左クリックの場合
			if (me.button == 1){
				focusFigure = (FileImageFigure)figure;

				// マウスクリックした際に選択されているFigureが、FacilityFigureの場合
				if(figure instanceof FacilityFigure) {

					// アイコン移動可能モードの場合
					if(_view.getMode() == Mode.FLOATING_MODE){
						// 図の位置とマウスポインタの位置の差分を求める
						delta = figure.getLocation().getDifference(me.getLocation());
						status = Status.FIGURE_SELECTED_FOR_MOVE;
					} else if(_view.getMode() == Mode.EDIT_CONNECTION_MODE){ // コネクション編集可能モードの場合
						// 図の位置とマウスポインタの位置の差分を求める
						delta = figure.getLocation().getDifference(me.getLocation());
						// コネクションの始点が選択された状態
						status = Status.FIGURE_SELECTED_FOR_CREATE_CONNECTION;
					} else if(_view.getMode() == Mode.FIXED_MODE) {
						// 図の位置とマウスポインタの位置の差分を求める
						delta = figure.getLocation().getDifference(me.getLocation());
						// コネクションの始点が選択された状態
						status = Status.FIGURE_SELECTED_FOR_NOT_MOVE;
					} else {
						m_log.debug("mode : non");
						status = Status.NOT_SELECTED;
					}
				}
			}

			// 右クリックの場合
			if (me.button == 3){
				m_log.debug("focusFigure class = " + figure.getClass().getSimpleName());
				Menu menu = new Menu(shell, SWT.POP_UP);
				m_canvas.setMenu(menu);
				if (figure instanceof FileImageFigure) {
					MenuItem item = new MenuItem(menu, SWT.PUSH);
					String facilityName = ((FileImageFigure)figure).getFacilityName();
					item.setText(com.clustercontrol.util.Messages.getString("facility.id") +
							" : " + HinemosMessage.replace(facilityName));
					item.setEnabled(false);
					item = new MenuItem(menu, SWT.SEPARATOR);
				}

				// 選択されているアイコンがスコープの場合
				if(figure instanceof ScopeFigure) {
					focusFigure = (ScopeFigure)figure;

					// POPUPメニュー設定 (新規ビュー)
					MenuItem item = new MenuItem(menu, SWT.PUSH);
					String targetScopeFacilityId = focusFigure.getFacilityId();
					item.setText(com.clustercontrol.nodemap.messages.Messages.getString("view.new"));
					item.addSelectionListener(new SelectionListener() {
						@Override
						public void widgetDefaultSelected(SelectionEvent event) {
							m_log.debug("new view widgetDefaultSelected");
						}
						@Override
						public void widgetSelected(SelectionEvent event) {
							m_log.debug("new view widgetSelected");
							// 遷移対象のファシリティIDを取得

							// 新規ビューで対象スコープのマップを表示する
							RelationViewController.createNewView(((ScopeFigure)focusFigure).getManagerName(), targetScopeFacilityId, NodeMapView.class);
						}
					});

					item = new MenuItem(menu, SWT.SEPARATOR);
					
					// POPUPメニュー設定 (イメージ変更)
					item = new MenuItem(menu, SWT.PUSH);
					item.setText(com.clustercontrol.nodemap.messages.Messages.getString(
					"file.select.image.icon"));
					item.addSelectionListener(new SelectionListener() {
						@Override
						public void widgetDefaultSelected(SelectionEvent event) {
							m_log.debug("select image icon widgetDefaultSelected");
						}
						@Override
						public void widgetSelected(SelectionEvent event) {
							m_log.debug("select image icon widgetSelected");
							// アイコン選択ダイアログを開く
							RegisterImageDialog dialog = new RegisterImageDialog(
									shell, m_controller, focusFigure);
							if (dialog.open() == IDialogConstants.OK_ID) {
								_view.setEditing(true);
							}
						}
					});
					// 編集不可モードの場合は、メニューを選択できなくする
					if(_view.getMode() == Mode.FIXED_MODE){
						item.setEnabled(false);
					}

					new MenuItem(menu, SWT.SEPARATOR);


					editRepositoryForScope(menu);
					editRepository(menu, focusFigure);

				} else if (figure instanceof NodeFigure){
					focusFigure = (NodeFigure)figure;
					// POPUPメニュー設定 (イメージ変更)
					MenuItem item = new MenuItem(menu, SWT.PUSH);
					item.setText(com.clustercontrol.nodemap.messages.Messages.getString(
					"file.select.image.icon"));
					item.addSelectionListener(new SelectionListener() {
						@Override
						public void widgetDefaultSelected(SelectionEvent event) {
							m_log.debug("select image icon widgetDefaultSelected");
						}
						@Override
						public void widgetSelected(SelectionEvent event) {
							m_log.debug("select image icon widgetSelected");
							// アイコン選択ダイアログを開く
							RegisterImageDialog dialog = new RegisterImageDialog(
									shell, m_controller, focusFigure);
							if (dialog.open() == IDialogConstants.OK_ID) {
								_view.setEditing(true);
							}
							
						}
					});
					// 編集不可モードの場合は、メニューを選択できなくする
					if(_view.getMode() == Mode.FIXED_MODE){
						item.setEnabled(false);
					}

					new MenuItem(menu, SWT.SEPARATOR);

					editRepositoryForNode(menu);
					editRepository(menu, focusFigure);

				} else if (figure instanceof BgFigure) {
					focusFigure = (BgFigure)figure;
					// POPUPメニュー設定
					MenuItem item = new MenuItem(menu, SWT.PUSH);
					item.setText(com.clustercontrol.nodemap.messages.Messages.getString(
					"file.select.image.bg"));
					item.addSelectionListener(new SelectionListener() {
						@Override
						public void widgetDefaultSelected(SelectionEvent event) {
							m_log.debug("select image bg widgetDefaultSelected");
						}
						@Override
						public void widgetSelected(SelectionEvent event) {
							m_log.debug("select image bg widgetSelected");
							RegisterImageDialog dialog = new RegisterImageDialog(
									shell, m_controller, focusFigure);
							if (dialog.open() == IDialogConstants.OK_ID) {
								_view.setEditing(true);
							}
						}
					});

					// 編集不可モードの場合は、メニューを選択できなくする
					if(_view.getMode() == Mode.FIXED_MODE){
						item.setEnabled(false);
					}

					new MenuItem(menu, SWT.SEPARATOR);

					editRepositoryForScope(menu);

				}
			}

			// クリックした際にFigureを選択している場合は、そのFigureにフォーカスをあわせる
			figure.requestFocus();

			// イベントを消費
			me.consume();
		}

		private void editRepositoryForNode(Menu menu) {
			MenuItem item = null;

			// POPUPメニュー設定 (ノード編集)
			item = new MenuItem(menu, SWT.PUSH);
			item.setText(com.clustercontrol.util.Messages.getString("edit") +
					" (" +
					com.clustercontrol.util.Messages.getString("node") +
			")");
			item.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent event) {
					m_log.debug("edit node widgetDefaultSelected");
				}
				@Override
				public void widgetSelected(SelectionEvent event) {
					m_log.debug("edit node widgetSelected");
					
					// 編集中の情報を解除してよいか確認
					if (_view.isEditing()) {
						if (!MessageDialog.openQuestion(
								null,
								Messages.getString("confirmed"),
								com.clustercontrol.nodemap.messages.Messages.getString("edit.refresh.confirm"))) {
							return;
						}
					}
					
					String facilityId = focusFigure.getFacilityId();
					// ノード編集ダイアログを開く
					NodeCreateDialog dialog = new NodeCreateDialog(
							shell, m_managerName, facilityId, true);
					if (dialog.open() == IDialogConstants.OK_ID) {
						_view.reload();
					}
				}
			});
			// 編集不可モードの場合は、メニューを選択できなくする
			if(_view.getMode() == Mode.FIXED_MODE){
				item.setEnabled(false);
			}

			// POPUPメニュー設定 (ノードコピー)
			item = new MenuItem(menu, SWT.PUSH);
			item.setText(com.clustercontrol.util.Messages.getString("copy") +
					" (" +
					com.clustercontrol.util.Messages.getString("node") +
			")");
			item.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent event) {
					m_log.debug("copy node widgetDefaultSelected");
				}
				@Override
				public void widgetSelected(SelectionEvent event) {
					m_log.debug("copy node widgetSelected");
					
					// 編集中の情報を解除してよいか確認
					if (_view.isEditing()) {
						if (!MessageDialog.openQuestion(
								null,
								Messages.getString("confirmed"),
								com.clustercontrol.nodemap.messages.Messages.getString("edit.refresh.confirm"))) {
							return;
						}
					}
					
					String facilityId = focusFigure.getFacilityId();
					// ノード編集ダイアログを開く
					NodeCreateDialog dialog = new NodeCreateDialog(
							shell, m_managerName, facilityId, false);
					if (dialog.open() == IDialogConstants.OK_ID) {
						// ノード登録後にノード割り当てをする
						// 最上位スコープ、登録ノード全て、未登録ノード、Hinemos内部スコープの場合は、割り当てない。
						String scopeFacilityId = _view.getController().getCurrentScope();
						boolean isScopeBuildin = FacilityTreeAttributeConstant.isBuiltinScope(scopeFacilityId);
						if (!focusFigure.isBuiltin() && !isScopeBuildin &&
								MessageDialog.openQuestion(
										null,
										com.clustercontrol.nodemap.messages.Messages.getString("node.assign.title"),
										com.clustercontrol.nodemap.messages.Messages.getString("node.assign.question"))) {
							try {
								NodeInfoResponse nodeInfo = dialog.getNodeInfo();
								List<String> nodeFacilityIds = new ArrayList<String>();
								nodeFacilityIds.add(nodeInfo.getFacilityId());
								RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(m_managerName);
								AssignNodeScopeRequest request = new AssignNodeScopeRequest();
								request.setFacilityIdList(nodeFacilityIds);
								wrapper.assignNodeScope(scopeFacilityId, request);
								// 成功報告ダイアログを生成
								Object[] arg = {getManagerName()};
								MessageDialog.openInformation(
										null,
										Messages.getString("successful"),
										Messages.getString("message.repository.6", arg));
								_view.reload();
							} catch (Exception e) {
								String errMessage = "";
								if (e instanceof InvalidRole) {
									// アクセス権なしの場合、エラーダイアログを表示する
									MessageDialog.openInformation(
											null,
											Messages.getString("message"),
											Messages.getString("message.accesscontrol.16"));
								} else {
									errMessage = ", " + e.getMessage();
								}
								// 失敗報告ダイアログを生成
								MessageDialog.openError(
										null,
										Messages.getString("failed"),
										Messages.getString("message.repository.7") + errMessage);
							}
						}else{
							_view.reload();
						}
					}
				}
			});
			// 編集不可モードの場合は、メニューを選択できなくする
			if(_view.getMode() == Mode.FIXED_MODE){
				item.setEnabled(false);
			}
			
			// POPUPメニュー設定 (ノード削除)
			item = new MenuItem(menu, SWT.PUSH);
			item.setText(com.clustercontrol.nodemap.messages.Messages.getString("delete") +
					" (" +
					com.clustercontrol.util.Messages.getString("node") +
			")");
			item.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent event) {
					m_log.debug("delete node widgetDefaultSelected");
				}
				@Override
				public void widgetSelected(SelectionEvent event) {
					m_log.debug("delete node widgetSelected");
					
					// 編集中の情報を解除してよいか確認
					if (_view.isEditing()) {
						if (!MessageDialog.openQuestion(
								null,
								Messages.getString("confirmed"),
								com.clustercontrol.nodemap.messages.Messages.getString("edit.refresh.confirm"))) {
							return;
						}
					}
					
					String facilityId = focusFigure.getFacilityId();
					String facilityName = "";
					if (focusFigure instanceof FacilityFigure) {
						facilityName = ((FacilityFigure)focusFigure).getFacilityName();
					} else if (focusFigure instanceof BgFigure) {
						facilityName = ((BgFigure)focusFigure).getFacilityName();
					}
					// 確認ダイアログにて変更が選択された場合、削除処理を行う。
					String[] args = { facilityName, facilityId };
					if (MessageDialog.openConfirm(
							null,
							Messages.getString("confirmed"),
							Messages.getString("message.repository.1", args))) {
						ArrayList<String> delList = new ArrayList<String>();
						delList.add(facilityId);
						new DeleteNodeProperty().delete(m_managerName, delList);
						_view.reload();
					}
				}
			});
			// 編集不可モードの場合は、メニューを選択できなくする
			if(_view.getMode() == Mode.FIXED_MODE){
				item.setEnabled(false);
			}
		}

		private void editRepositoryForScope(Menu menu) {
			MenuItem item = null;
			boolean builtInScopeFlag = focusFigure.isBuiltin();
			boolean compositeFlag = false;
			if (focusFigure.getFacilityId().equals(ReservedFacilityIdConstant.ROOT_SCOPE)) {
				compositeFlag = true;
			}

			// POPUPメニュー設定 (ノード追加)
			m_log.debug("focusFigure : " + focusFigure.getFacilityId());
			if (!builtInScopeFlag ||
					ReservedFacilityIdConstant.REGISTEREFD_SCOPE.equals(focusFigure.getFacilityId())) {
				item = new MenuItem(menu, SWT.PUSH);
				item.setText(com.clustercontrol.util.Messages.getString("add") +
						" (" +
						com.clustercontrol.util.Messages.getString("node") +
				")");
				item.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetDefaultSelected(SelectionEvent event) {
						m_log.debug("add node widgetDefaultSelected");
					}
					@Override
					public void widgetSelected(SelectionEvent event) {
						m_log.debug("add node widgetSelected ");
						
						// 編集中の情報を解除してよいか確認
						if (_view.isEditing()) {
							if (!MessageDialog.openQuestion(
									null,
									Messages.getString("confirmed"),
									com.clustercontrol.nodemap.messages.Messages.getString("edit.refresh.confirm"))) {
								return;
							}
						}

						NodeCreateDialog dialog = new NodeCreateDialog(
								shell, m_managerName, null, false);
						if (dialog.open() == IDialogConstants.OK_ID) {
							// ノード登録後にノード割り当てをする
							// 最上位スコープ、登録ノード全て、未登録ノード、Hinemos内部スコープの場合は、割り当てない。
							String scopeFacilityId = focusFigure.getFacilityId();
							if (!focusFigure.isBuiltin() &&
									MessageDialog.openQuestion(
											null,
											com.clustercontrol.nodemap.messages.Messages.getString("node.assign.title"),
											com.clustercontrol.nodemap.messages.Messages.getString("node.assign.question"))) {
								try {
									NodeInfoResponse nodeInfo = dialog.getNodeInfo();
									List<String> nodeFacilityIds = new ArrayList<String>();
									nodeFacilityIds.add(nodeInfo.getFacilityId());
									RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(m_managerName);
									AssignNodeScopeRequest request = new AssignNodeScopeRequest();
									request.setFacilityIdList(nodeFacilityIds);
									wrapper.assignNodeScope(scopeFacilityId, request);
									// 成功報告ダイアログを生成
									Object[] arg = {getManagerName()};
									MessageDialog.openInformation(
											null,
											Messages.getString("successful"),
											Messages.getString("message.repository.6", arg));
									_view.reload();
								} catch (Exception e) {
									String errMessage = "";
									if (e instanceof InvalidRole) {
										// アクセス権なしの場合、エラーダイアログを表示する
										MessageDialog.openInformation(
												null,
												Messages.getString("message"),
												Messages.getString("message.accesscontrol.16"));
									} else {
										errMessage = ", " + e.getMessage();
									}
									// 失敗報告ダイアログを生成
									MessageDialog.openError(
											null,
											Messages.getString("failed"),
											Messages.getString("message.repository.7") + errMessage);
								}
							}else{
								_view.reload();
							}
						}
					}
				});
				// 編集不可モードの場合は、メニューを選択できなくする
				if(_view.getMode() == Mode.FIXED_MODE){
					item.setEnabled(false);
				}
			}

			// POPUPメニュー設定 (スコープ追加)
			item = new MenuItem(menu, SWT.PUSH);
			item.setText(com.clustercontrol.util.Messages.getString("add") +
					" (" +
					com.clustercontrol.util.Messages.getString("scope") +
			")");
			item.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent event) {
					m_log.debug("add scope widgetDefaultSelected");
				}
				@Override
				public void widgetSelected(SelectionEvent event) {
					m_log.debug("add scope widgetSelected");
					
					// 編集中の情報を解除してよいか確認
					if (_view.isEditing()) {
						if (!MessageDialog.openQuestion(
								null,
								Messages.getString("confirmed"),
								com.clustercontrol.nodemap.messages.Messages.getString("edit.refresh.confirm"))) {
							return;
						}
					}
					
					String parentFacilityId = focusFigure.getFacilityId();
					if(parentFacilityId.equals(ReservedFacilityIdConstant.ROOT_SCOPE)) {
						parentFacilityId = "";
					}
					
					ScopeCreateDialog dialog = new ScopeCreateDialog(
							shell, m_managerName, null, false);
					dialog.setParentFacilityId(parentFacilityId);
					if (dialog.open() == IDialogConstants.OK_ID) {
						_view.reload();
					}
				}
			});
			// 編集不可モードの場合は、メニューを選択できなくする
			// 最上位スコープの場合はメニュー選択可能とする
			if(_view.getMode() == Mode.FIXED_MODE || (builtInScopeFlag && !compositeFlag)){
				item.setEnabled(false);
			}

			// POPUPメニュー設定 (スコープ編集)
			item = new MenuItem(menu, SWT.PUSH);
			item.setText(com.clustercontrol.util.Messages.getString("edit") +
					" (" +
					com.clustercontrol.util.Messages.getString("scope") +
			")");
			item.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent event) {
					m_log.debug("edit scope widgetDefaultSelected");
				}
				@Override
				public void widgetSelected(SelectionEvent event) {
					m_log.debug("edit scope widgetSelected");
					String facilityId = focusFigure.getFacilityId();
					
					// 編集中の情報を解除してよいか確認
					if (_view.isEditing()) {
						if (!MessageDialog.openQuestion(
								null,
								Messages.getString("confirmed"),
								com.clustercontrol.nodemap.messages.Messages.getString("edit.refresh.confirm"))) {
							return;
						}
					}

					ScopeCreateDialog dialog = new ScopeCreateDialog(
							shell, m_managerName, facilityId, true);
					if (dialog.open() == IDialogConstants.OK_ID) {
						if (facilityId != null
								&& facilityId.equals(m_controller.getCurrentScope())) {
							_view.updateView(facilityId);
						} else {
							_view.reload();
						}
					}
				}
			});
			// 編集不可モードの場合は、メニューを選択できなくする
			if(_view.getMode() == Mode.FIXED_MODE || builtInScopeFlag || compositeFlag){
				item.setEnabled(false);
			}

			// POPUPメニュー設定 (スコープ削除)
			item = new MenuItem(menu, SWT.PUSH);
			item.setText(com.clustercontrol.nodemap.messages.Messages.getString("delete") +
					" (" +
					com.clustercontrol.util.Messages.getString("scope") +
			")");
			item.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent event) {
					m_log.debug("delete scope widgetDefaultSelected");
				}
				@Override
				public void widgetSelected(SelectionEvent event) {
					m_log.debug("delete scope widgetSelected");
					
					// 編集中の情報を解除してよいか確認
					if (_view.isEditing()) {
						if (!MessageDialog.openQuestion(
								null,
								Messages.getString("confirmed"),
								com.clustercontrol.nodemap.messages.Messages.getString("edit.refresh.confirm"))) {
							return;
						}
					}
					
					String facilityId = focusFigure.getFacilityId();
					String facilityName = "";
					if (focusFigure instanceof FacilityFigure) {
						facilityName = ((FacilityFigure)focusFigure).getFacilityName();
					} else if (focusFigure instanceof BgFigure) {
						facilityName = ((BgFigure)focusFigure).getFacilityName();
					}
					// 確認ダイアログにて変更が選択された場合、削除処理を行う。
					String[] args = { facilityName, facilityId };
					if (MessageDialog.openConfirm(
							null,
							Messages.getString("confirmed"),
							Messages.getString("message.repository.3", args))) {
						
						List<String> delList = new ArrayList<String>();
						delList.add(facilityId);

						new DeleteScopeProperty().delete(m_managerName, delList);
						if (facilityId != null
								&& facilityId.equals(m_controller.getCurrentScope())) {
							_view.upward();
						} else {
							_view.reload();
						}
					}
				}
			});
			// 編集不可モードの場合は、メニューを選択できなくする
			if(_view.getMode() == Mode.FIXED_MODE || builtInScopeFlag || compositeFlag){
				item.setEnabled(false);
			}

			// POPUPメニュー設定 (ノード割り当て)
			item = new MenuItem(menu, SWT.PUSH);
			item.setText(com.clustercontrol.util.Messages.getString("assign"));
			item.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent event) {
					m_log.debug("assign widgetDefaultSelected");
				}
				@Override
				public void widgetSelected(SelectionEvent event) {
					m_log.debug("assign widgetSelected");
					String facilityId = focusFigure.getFacilityId();
					
					// 編集中の情報を解除してよいか確認
					if (_view.isEditing()) {
						if (!MessageDialog.openQuestion(
								null,
								Messages.getString("confirmed"),
								com.clustercontrol.nodemap.messages.Messages.getString("edit.refresh.confirm"))) {
							return;
						}
					}

					NodeAssignDialog dialog = new NodeAssignDialog(shell, m_managerName, facilityId);
					// ダイアログを開く
					// メッセージダイアログ表示はNodeAssignDialogで行う
					if (dialog.open() == IDialogConstants.OK_ID) {
						_view.reload();
					}
				}
			});
			// 編集不可モードの場合は、メニューを選択できなくする
			if(_view.getMode() == Mode.FIXED_MODE || builtInScopeFlag || compositeFlag){
				item.setEnabled(false);
			}

			// POPUPメニュー設定 (ノード割り当て解除)
			item = new MenuItem(menu, SWT.PUSH);
			item.setText(com.clustercontrol.nodemap.messages.Messages.getString("release"));
			item.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent event) {
					m_log.debug("assign release widgetDefaultSelected");
				}
				@Override
				public void widgetSelected(SelectionEvent event) {
					m_log.debug("assign release widgetSelected");
					
					// 編集中の情報を解除してよいか確認
					if (_view.isEditing()) {
						if (!MessageDialog.openQuestion(
								null,
								Messages.getString("confirmed"),
								com.clustercontrol.nodemap.messages.Messages.getString("edit.refresh.confirm"))) {
							return;
						}
					}
					
					String facilityId = focusFigure.getFacilityId();
					// ノード割り当て解除ダイアログを開く
					NodeReleaseDialog dialog = new NodeReleaseDialog(shell, m_managerName, facilityId);
					// ダイアログを開く
					// メッセージダイアログ表示はNodeReleaseDialogで行う
					if (dialog.open() == IDialogConstants.OK_ID) {
						_view.reload();
					}
				}
			});
			// 編集不可モードの場合は、メニューを選択できなくする
			if(_view.getMode() == Mode.FIXED_MODE || builtInScopeFlag || compositeFlag){
				item.setEnabled(false);
			}
			
			// POPUPメニュー設定 (オブジェクト権限の設定)
			item = new MenuItem(menu, SWT.PUSH);
			item.setText(com.clustercontrol.util.Messages.getString("object.privilege.setting"));
			item.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent event) {
					m_log.debug("assign objectPrivilege");
				}
				@Override
				public void widgetSelected(SelectionEvent event) {
					m_log.debug("assign widgetSelected");
					
					// 編集中の情報を解除してよいか確認
					if (_view.isEditing()) {
						if (!MessageDialog.openQuestion(
								null,
								Messages.getString("confirmed"),
								com.clustercontrol.nodemap.messages.Messages.getString("edit.refresh.confirm"))) {
							return;
						}
					}
					
					String facilityId = focusFigure.getFacilityId();
					String ownerRoleId = focusFigure.getOwnerRoleId();

					// オーナーロールIDを設定する
					ObjectPrivilegeListDialog dialog = new ObjectPrivilegeListDialog(_view.getSite().getShell(),
							m_managerName, facilityId, HinemosModuleConstant.PLATFORM_REPOSITORY, ownerRoleId);
					// ダイアログを開く
					// メッセージダイアログ表示はObjectPrivilegeListDialogで行う
					if (dialog.open() == IDialogConstants.OK_ID) {
						_view.reload();
					}
				}
			});
			// 編集不可モードの場合は、メニューを選択できなくする
			if(_view.getMode() == Mode.FIXED_MODE || compositeFlag 
					|| focusFigure.getFacilityId().equals(ReservedFacilityIdConstant.ROOT_SCOPE)){
				item.setEnabled(false);
			}
		}

		/**
		 * pingと性能グラフのコンテキストメニュー表示の可否、操作実行をします。
		 * 
		 * @param menu
		 * @param figure
		 */
		private void editRepository(Menu menu, FileImageFigure figure) {
			String facilityId = figure.getFacilityId();
			String facilityName = HinemosMessage.replace(figure.getFacilityName());
			MenuItem item = new MenuItem(menu, SWT.SEPARATOR);
			
			// ping
			item = new MenuItem(menu, SWT.PUSH);
			item.setText(com.clustercontrol.nodemap.messages.Messages.getString("select.contextmenu.ping"));
			item.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					m_log.debug("ping icon widgetSelected");
					try {
						String[] args = {facilityName};
						if (!MessageDialog.openQuestion(
								null,
								Messages.getString("confirmed"),
								com.clustercontrol.nodemap.messages.Messages.getString("message.exec.ping.before", args))) {
							return;
						}
						// ここからpingする
						m_log.debug("ping start. facilityId:" + facilityId);
						RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(m_managerName);
						List<PingResultResponse> pingResultList = wrapper.ping(facilityId);
						if (pingResultList == null || pingResultList.size() == 0) {
							m_log.debug("pingResultList is empty");
							MessageDialog.openInformation(
									null,
									Messages.getString("message"),
									com.clustercontrol.nodemap.messages.Messages.getString("message.exec.ping.result.empty"));
							return;
						} else {
							StringBuilder sb = new StringBuilder();
							Collections.sort(pingResultList, new Comparator<PingResultResponse>() {
								@Override
								public int compare(PingResultResponse o1, PingResultResponse o2) {
									return o1.getResult().compareTo(o2.getResult());
								}
							});
							for (PingResultResponse ret : pingResultList) {
								sb.append(ret.getResult()+ "\n--------------\n");
							}
							m_log.debug("ping finish. facilityId:" + facilityId + ", pingResultList.size:" + pingResultList.size());
							TextAreaDialog textDialog = new TextAreaDialog(null, 
									com.clustercontrol.nodemap.messages.Messages.getString("dialog.title.result.ping"), 
									false, false);
							textDialog.setText(sb.toString().replace("\n\n\n", "\n\n"));
							textDialog.setOkButtonText(Messages.getString("ok"));
							textDialog.open();
						}
					} catch (Exception ex) {
						String errMessage = "";
						if (ex instanceof InvalidRole) {
							// アクセス権なしの場合、エラーダイアログを表示する
							MessageDialog.openError(
									null,
									Messages.getString("message"),
									Messages.getString("message.accesscontrol.16"));
							return;
						} else {
							errMessage = ", " + HinemosMessage.replace(ex.getMessage());
						}
						// 失敗報告ダイアログを生成
						MessageDialog.openError(
								null,
								Messages.getString("failed"),
								com.clustercontrol.nodemap.messages.Messages.getString("message.exec.ping.fail") + errMessage);
					}
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					m_log.debug("ping icon widgetDefaultSelected");
				}
			});

			item = new MenuItem(menu, SWT.SEPARATOR);
			
			// 性能グラフ
			item = new MenuItem(menu, SWT.PUSH);
			item.setText(com.clustercontrol.nodemap.messages.Messages.getString("select.contextmenu.collectgraph"));
			item.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					m_log.debug("collect graph icon widgetSelected");
					// 性能パースペクティブを開く
					openCollectPerspective(figure);
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					m_log.debug("collect graph icon widgetDefaultSelected");
				}
			});

		}
		
		// マウスを離した時に呼ばれる
		@Override
		public void mouseReleased(MouseEvent me) {
			if(status == Status.FIGURE_SELECTED_FOR_CREATE_CONNECTION){
				// マウスの位置を取得
				Point mousePoint = me.getLocation();

				// 線分を消去
				if(tmpline != null){
					m_layer.remove(tmpline);
					tmpline = null;
				}

				/*
				 * コネクションを描く、もしくは消す。
				 */
				m_controller.updateConnection(mousePoint, element.getFacilityId());
				_view.setEditing(true);

			}
			// *********************************************************
			status = Status.NOT_SELECTED;
			delta = null;

			// イベントを消費
			me.consume();
		}

		// ダブルクリック時に呼ばれる
		@Override
		public void mouseDoubleClicked(MouseEvent me) {
			Figure figure = (Figure)me.getSource();
			m_log.debug("mouse double click  " + figure.toString());
			if(figure instanceof ScopeFigure) {
				// focusFigure をそのFacilityFigureに変更する。
				focusFigure = (FacilityFigure)figure;

				// 遷移対象のファシリティIDを取得
				String targetScopeFacilityId = ((ScopeFigure)focusFigure).getFacilityId();
				
				// 編集中の情報を解除してよいか確認
				if (_view.isEditing()) {
					if (!MessageDialog.openQuestion(
							null,
							Messages.getString("confirmed"),
							com.clustercontrol.nodemap.messages.Messages.getString("edit.refresh.confirm"))) {
						return;
					}
				}

				// 同じビュー内で画面遷移
				_view.updateView(targetScopeFacilityId);
			}

			// イベントを消費
			me.consume();
		}

		// ドラッグ時に呼ばれる
		@Override
		public void mouseDragged(MouseEvent me) {
			// アイコン移動可能モードの場合は、左クリックでアイコンを移動させる
			if (status == Status.FIGURE_SELECTED_FOR_MOVE) {
				if(delta == null){
					return;
				}
				
				_view.setEditing(true);

				// マウスポインタの位置を取得
				Point mousePoint = me.getLocation();
				adjustPoint (mousePoint);

				// 図を移動させる
				Figure figure = (Figure) me.getSource();
				Point point = new Point(mousePoint.x + delta.width, mousePoint.y + delta.height);
				gridPoint(point);
				Rectangle rectangle = new Rectangle(point, figure.getSize());
				m_layer.setConstraint(figure, rectangle);

				// モデルに値を反映
				element.setX(point.x);
				element.setY(point.y);
				element.setNewcomer(false);

			} else if (status == Status.FIGURE_SELECTED_FOR_CREATE_CONNECTION) {
				// マウスポインタの位置を取得
				Point mousePoint = me.getLocation();
				adjustPoint (mousePoint);
				// コネクションの張り先を明示させるための線分を表示
				// 線分を消去
				if(tmpline != null){
					m_layer.remove(tmpline);
					tmpline = null;
				}
				tmpline = new Polyline();
				Point point = new Point(focusFigure.getLocation().x + focusFigure.getSize().width/2,
						focusFigure.getLocation().y + focusFigure.getSize().height/2);
				m_log.debug(point.toString());
				tmpline.addPoint(point);
				tmpline.addPoint(mousePoint);
				m_layer.add(tmpline);
			} else if (status == Status.FIGURE_SELECTED_FOR_NOT_MOVE) {
				// マウスポインタの位置を取得
				Point mousePoint = me.getLocation();
				Point figurePoint = focusFigure.getLocation();
				if (Math.abs(mousePoint.x - figurePoint.x) +
						Math.abs(mousePoint.y - figurePoint.y) > 100) {
					m_log.debug("me.x=" + mousePoint.x + ", me.y=" + mousePoint.y +
							", width=" + figurePoint.x + ", height=" + figurePoint.y);
					
					// 連続して呼ばれる場合があるので、メッセージが1回しか出ないように制御する
					if (!message_flg) {
						message_flg = true;
						MessageDialog.openInformation(null, Messages.getString("message"),
								com.clustercontrol.nodemap.messages.Messages.getString("info.drag"));
						message_flg = false;
					}
					status = Status.NOT_SELECTED;
				}
		}
			// *********************************************************

			// イベントを消費
			me.consume();
		}
	}



	/**
	 * マウスが遠くに行っても、アイコンは遠くに行かないように調整するメソッド。 キャンバスのMAXを超えないようにする
	 */
	private void adjustPoint (Point point) {
		Point canvasSize = m_layer.getClientArea().getBottomRight();

		if (point.x < 0) {
			point.x = 0;
		} else if (point.x > canvasSize.x) {
			point.x = canvasSize.x;
		}
		if (point.y < 0) {
			point.y = 0;
		} else if (point.y > canvasSize.y) {
			point.y = canvasSize.y;
		}
	}

	/**
	 * グリッドに沿って移動する。
	 */
	private void gridPoint (Point point) {
		if (gridSnapFlg) {
			point.x = point.x - (point.x % gridWidth);
			point.y = point.y - (point.y % gridWidth);
		}
	}

	/**
	 * メモリリークを調査するためのメソッド。
	 * デバッグ用。
	 */
	public void countFigure() {
		HashMap<String, Integer> map = new HashMap<String, Integer> ();
		countFigureSub(m_layer, map);
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			m_log.warn("countFigure(), class=" + entry.getKey() + ", count=" + entry.getValue());
		}
	}

	private void countFigureSub(Figure figure, HashMap<String, Integer> map) {
		for (Object obj : figure.getChildren()) {
			if (obj instanceof Figure) {
				countFigureSub((Figure)obj, map);
			}
		}
		Integer i = map.get(figure.getClass().getSimpleName());
		if (i == null) {
			map.put(figure.getClass().getSimpleName(), 1);
		} else {
			map.put(figure.getClass().getSimpleName(), i+1);
		}
	}

	public void setController(MapViewController m_controller) {
		this.m_controller = m_controller;
	}

	public MapViewController getController() {
		return this.m_controller;
	}

	public void zoomIn() {
		if (adjustFlag) {
			scale += adjustDecimal;
		} else {
			if (scale < 1) {
				scale += 0.1;
			}
		}
		adjustFlag = false;
		
		updateNotManagerAccess();
	}

	public void zoomOut() {
		if (adjustFlag) {
			scale += adjustDecimal;
		}
		if (scale < 0.2) {
			// 0.1 が最小。
		} else {
			scale -= 0.1;
		}
		adjustFlag = false;
		
		updateNotManagerAccess();
	}
	
	public void zoomAdjust() {
		org.eclipse.swt.graphics.Point point = m_canvas.getSize();
		
		m_log.debug("size.x:"+point.x +",size.y:"+point.y);
		double scale_x = (double)(point.x) / sizeX;
		double scale_y = (double)(point.y) / sizeY;
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
		
		adjustFlag = true;
		// 小数第二桁以下を端数として保存しておく
		adjustDecimal = Math.ceil(scale*10) / 10 - scale;
		m_log.debug(scale+","+adjustDecimal);
		
		applyScale();
	}

	public void updateNotManagerAccess() {
		try {
			m_controller.drawMap();
			m_controller.updatePriorityNotManagerAccess();
		} catch (Exception e) {
			m_log.error(e.getMessage(), e);
		}
	}
	
	public String getManagerName() {
		return m_managerName;
	}
	
	public void setManagerName(String managerName) {
		m_managerName = managerName;
	}
	
	/**
	 * 性能グラフを表示します。
	 * 
	 * @param figure
	 */
	private void openCollectPerspective(FileImageFigure figure) {
		String facilityId = figure.getFacilityId();
		m_log.debug("managername:" + m_managerName + ", currentScop:" + m_controller.getCurrentScope() + ", facilityId:" + facilityId);
		FacilityTreeItemResponse targetTreeItem = RelationViewController.getScopeTreeView(m_controller.getCurrentScope(), facilityId);
		String path = m_managerName + SEPARATOR_HASH_EX_HASH;
		
		if (m_controller.getCurrentScope().equals(ReservedFacilityIdConstant.ROOT_SCOPE)) {
			path += facilityId + SEPARATOR_HASH_EX_HASH + FacilityConstant.TYPE_SCOPE;
		} else if (targetTreeItem.getData().getFacilityType().equals(FacilityTypeEnum.NODE)) {
			path += targetTreeItem.getParent().getData().getFacilityId() 
					+ SEPARATOR_HASH_EX_HASH + targetTreeItem.getData().getFacilityId() 
					+ SEPARATOR_HASH_EX_HASH + FacilityConstant.TYPE_NODE;

		} else if (targetTreeItem.getData().getFacilityType().equals(FacilityTypeEnum.SCOPE)) {
			path += targetTreeItem.getData().getFacilityId() 
					+ SEPARATOR_HASH_EX_HASH + FacilityConstant.TYPE_SCOPE;
		}
		m_log.debug("path:" + path);
		
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		String perspectiveName = com.clustercontrol.nodemap.messages.Messages.getString("perspective.collect");
		m_log.debug("perspective name:" + perspectiveName);
		IPerspectiveDescriptor perspective 
		= PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(perspectiveName);

		if (perspective == null || window == null) {
			m_log.debug("perspective == null or window == null. return.");
			return;
		}
		
		window.getActivePage().setPerspective(perspective);
		IWorkbenchPage page = window.getActivePage();

		//viewとcompositeを取得
		CollectGraphView collectView = (CollectGraphView)page.findView(CollectGraphView.ID);
		if (collectView == null) {
			throw new InternalError("collectGraphView is null.");
		}
		List<String> selectNodeInfoList = new ArrayList<>();
		selectNodeInfoList.add(path);
		collectView.setSelectFacilityListFromNodemap(selectNodeInfoList);
		
		if(!collectView.getFacilityTreeComposite().isDisposed()){
			m_log.trace("CollectSettingComposite.checkAsyncExec() is true");
			collectView.getFacilityTreeComposite().getDisplay().asyncExec(new Runnable(){
				@Override
				public void run() {
					// 選択状態の復元
					collectView.setItemCodeCheckedTreeItems();
				}
			});
		}
		else{
			m_log.trace("CollectSettingComposite.checkAsyncExec() is false");
		}
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
		adjustFlag = false;
		m_layer.setScale(scale);

	}

	//指定のファシリティにフォーカスセット
	private void setFocusFacility(String targetID) {
		// フォーカス処理を呼び出すと ツリービューやイベントビューも更新され、表画が遅め。
		// 検索フォーカス時、他ビュー更新の省略を検討したが、他実装との整合が難しいのでそのままとした。
		FacilityFigure setFigure = m_controller.getFacilityFigure(targetID);
		if( setFigure == null ){
			return;
		}
		Point p = setFigure.getLocation();
		m_canvas.scrollSmoothTo(p.x, p.y);
		setFigure.requestFocus();
		this.focusFigure = setFigure;
		if(m_log.isDebugEnabled()){
			m_log.debug("setFocusFacilityEmuration end. FacilityID:" +targetID);
		}
	}

	//表示中のファシリティ図をID検索
	private String searchDisplayFacility( String currentId, String keyword ){
		boolean isCurrentReach =false;
		//カレントの指定がないか、カレントがキーワードに該当していないなら先頭から検索
		if(currentId == null || ( currentId.indexOf( keyword ) == -1 )  ){
			isCurrentReach =true;
		}
		String[] findIdList = m_controller.getFindIdList();
		if( findIdList == null){
			return null;
		}
		for (String checkId :findIdList) {
			if(isCurrentReach == false){
				if( checkId.equals(currentId)){
					isCurrentReach = true;
				}
			}else{
				if( -1 != checkId.indexOf( keyword ) ){
					return checkId;
				}
			}
		}
		return null;
	}

	//背景にフォーカスセット
	private void setFocusBgFigure() {
		//表示中のレイヤーから背景図を取り出して フォーカスをセット
		for (Object obj : m_layer.getChildren()) {
			if (obj instanceof BgFigure) {
				BgFigure bgFigure  = (BgFigure)obj;
				focusFigure.handleFocusLost(null);
				if(bgFigure.hasFocus()){
					bgFigure.handleFocusGained(null);
				}else{
					bgFigure.requestFocus();
				}
				focusFigure = bgFigure;
				Point p = focusFigure.getLocation();
				m_canvas.scrollSmoothTo(p.x, p.y);
				break;
			}
		}
	
	}
	/**
	 * ノードスコープ検索
	 * @param keyword 検索文字列（前方一致検索）
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
		
		//現在のフォーカスを取得
		String curFacilityId = null;
		if( null != focusFigure){ 
			if( m_controller.getFacilityFigure(focusFigure.getFacilityId()) != null  ) {
				curFacilityId = focusFigure.getFacilityId();
			}else{
				m_log.debug("doSearch . currnet top " );
			}
		}else{
			m_log.debug("doSearch . currnet nothing " );
		}
		//検索（現在フォーカスの次を取得）
		String result = searchDisplayFacility( curFacilityId, keyword );
		if( null != result ){
			setFocusFacility(result);
		}else{
			//該当がなければ 、ヒットなしメッセージを表示して
			//背景にフォーカスを戻す（次の検索のために）
			setFocusBgFigure();
			MessageDialog.openInformation( this.getShell(), Messages.getString("message"), Messages.getString("search.not.found") );
		}
	}
	
}
