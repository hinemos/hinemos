/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.openapitools.client.model.JobTreeItemResponseP4;

import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.action.GetJobDetailTableDefine;
import com.clustercontrol.jobmanagement.composite.action.JobDetailSelectionChangedListener;
import com.clustercontrol.jobmanagement.composite.action.SessionJobDoubleClickListener;
import com.clustercontrol.jobmanagement.dialog.JobDetailFilterDialog.FilterCondition;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmanagement.viewer.JobTableTreeViewer;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TargetPlatformUtil;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.util.WidgetTestUtil;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * ジョブ[ジョブ詳細]ビュー用のコンポジットクラスです。
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class DetailComposite extends Composite {
	
	// ログ
	private static Log m_log = LogFactory.getLog( DetailComposite.class );
	
	/*
	 * ジョブ履歴[ジョブ詳細]ビューの表示用モデルのルート要素
	 * 表示のモードに合わせて、子要素とするジョブを調整する
	 */
	public static class JobDetailViewModel {
		
		protected JobTreeItemWrapper root;
		
		// 階層表示の直下の子要素
		protected final List<JobElement> children = new LinkedList<>();
		
		// フィルタ設定
		protected Optional<FilterCondition> condition = Optional.empty();
		
		// フラット表示の要否
		protected boolean flatView = false;
		
		// 選択項目によるフィルタ要否
		protected boolean selectedView = false;
		
		// フィルタ設定によるフィルタ要否
		// ※RAPとRCPで初期値を切り替える
		// ※ジョブ履歴[ジョブ詳細]ビューで「フィルタ設定でフィルタ」アクションが、初期表示でオン/オフ状態に影響ある
		// ※初期表示をオンにしたいが、RCPで初期表示をオンにする方法が不明
		// ※RCPで表示的にオフ、内部的にオンという状態になる状況を避けるため、プラットフォームで初期値を調整することにした。
		protected boolean filteredView = TargetPlatformUtil.isRAP();
		
		// フラット表示用の全ジョブ要素
		protected final List<JobElement> elements = new LinkedList<>();
		
		// 選択された子要素
		protected final Set<JobElement> selectedSet = new LinkedHashSet<>();
		// 階層表示の際、選択された子要素を表示するのに必要な親要素を含んだセット
//		protected final Set<JobElement> selectedVisibleSet = new LinkedHashSet<>();

		// フィルター設定に該当する子要素
		protected final Set<JobElement> filteredSet = new LinkedHashSet<>();
		// 階層表示の際、フィルタ設定に該当する子要素を表示するのに必要な親要素を含んだセット
//		protected final Set<JobElement> filteredVisibleSet = new LinkedHashSet<>();
		
		// 選択項目の変更イベントのリスナー
		protected final List<Consumer<Set<JobElement>>> selectionListeners = new LinkedList<>();
		// フィルタ設定の変更イベントのリスナー
		protected final List<Consumer<FilterCondition>> filterListeners = new LinkedList<>();
		
		// フラット表示の切り替えイベントのリスナー
		protected final List<Consumer<Boolean>> flatViewListeners = new LinkedList<>();
		// フィルタ設定によるフィルタ表示の切り替えイベントのリスナー
		protected final List<Consumer<Boolean>> filteredViewListeners = new LinkedList<>();
		// 選択項目によるフィルタ表示の切り替えイベントのリスナー
		protected final List<Consumer<Boolean>> selectedViewListeners = new LinkedList<>();
		
		public JobDetailViewModel() {
		}
		
		public JobDetailViewModel(JobTreeItemWrapper root) {
			update(root);
		}
		
		@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Internal UI model.")
		public void update(JobTreeItemWrapper root) {
			if (!Objects.equals(this.root, root)) {
				// ルート要素が変わったので、保持している情報をクリア
				clear();
				
				this.root = root;
				
				// 直下の子要素リストを作成
				buildJobElements();
				
				// フィルタ設定によるフィルタを実施
				filter();
				
				// 選択項目がクリアされたことを通知
				selectionListeners.stream().forEach(c->c.accept(Collections.emptySet()));
			}
		}
		
		@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Internal UI model.")
		public JobTreeItemWrapper getRootItem() {
			return root;
		}
		
		public List<JobElement> getRawJobElements() {
			return Collections.unmodifiableList(children);
		}
		
		/*
		 * 直下の子要素のリスト作成
		 */
		protected void buildJobElements() {
			children.addAll(root.getChildren().stream().map(c->new JobElement(this, c, null)).collect(Collectors.toList()));
		}
		
		/*
		 * 子要素のリストを返す
		 */
		public List<JobElement> getJobElements() {
			boolean filtered = isFilteredView()
					&& condition.isPresent()
					&& !condition.get().isEmpty();
			boolean selected = isSelectedView();
			
			// フラット表示中か？
			if (isFlatView()) {
				// フィルター表示でも選択表示でもないので、全要素を並列に表示
				if (!filtered && !selected) {
					return new LinkedList<>(elements);
				}
				// フィルター表示かつ選択表示で表示対象のみ表示
				if (filtered && selected) {
					return filteredSet.stream().filter(e -> selectedSet.contains(e)).collect(Collectors.toList());
				}
				// フィルター表示のみ
				if (filtered) {
					return new LinkedList<>(filteredSet);
				}
				// 選択表示のみ
				return new LinkedList<>(selectedSet);
			}
			
			// 階層表示で、フィルター表示でも選択表示でもない
			if (!filtered && !selected) {
				return getRawJobElements();
			}
			
			// 階層表示で、選択表示かつフィルター表示対
			return getRawJobElements().stream()
					.filter(e -> ((!filtered || e.isFilteredVisible()) && (!selected || e.isSelectedVisible())) || e.hasVisibleChild())
					.collect(Collectors.toList());
		}
		
		public Set<JobElement> getSelectedElements() {
			return Collections.unmodifiableSet(selectedSet);
		}
		
		public void setFilterCondition(FilterCondition condition) {
			this.condition = Optional.ofNullable(condition).map(c->c.isEmpty() ? null: new FilterCondition(c));
			filter();
			filterListeners.stream().forEach(l->l.accept(this.condition.orElse(null)));
		}
		
		public void setFilteredView(boolean enabled) {
			filteredView = enabled;
			filteredViewListeners.stream().forEach(l->l.accept(filteredView));
		}
		
		public boolean isFilteredView() {
			return filteredView;
		}
		
		public void setSelectedView(boolean enabled) {
			selectedView = enabled;
			selectedViewListeners.stream().forEach(l->l.accept(selectedView));
		}
		
		public boolean isSelectedView() {
			return selectedView;
		}
		
		public void setFlatView(boolean enabled) {
			if (flatView != enabled) {
				flatView = enabled;
				flatViewListeners.stream().forEach(l->l.accept(flatView));
			}
		}
		
		public boolean isFlatView() {
			return flatView;
		}
		
		public Optional<FilterCondition> getFilterCondition() {
			return condition;
		}
		
		protected boolean selected(JobElement target) {
			return selectedSet.contains(target);
		}
		
		/*
		 * 選択項目を表示するのに必要な親要素にマーク
		 */
		protected void select(JobElement target) {
			if (!selectedSet.contains(target)) {
				Set<JobElement> temp = new LinkedHashSet<>(selectedSet);
				temp.add(target);
				
				selectedSet.add(target);
				
				selectionListeners.stream().forEach(c->c.accept(temp));
			}
		}
		
		/*
		 * 選択解除項目を非表示することに表示が不要になった親要素のマークを解除
		 */
		protected void unselect(JobElement target) {
			if (selectedSet.contains(target)) {
				Set<JobElement> temp = new LinkedHashSet<>(selectedSet);
				temp.add(target);
				
				selectedSet.remove(target);
				
				selectionListeners.stream().forEach(c->c.accept(temp));
			}
		}
		
		protected boolean isSelectedVisible(JobElement target) {
			return selectedSet.contains(target);
		}
		
		protected boolean isFilteredVisible(JobElement target) {
			return condition.map(c->filteredSet.contains(target)).orElse(Boolean.TRUE);
		}
		
		protected boolean matched(JobElement target) {
			return filteredSet.contains(target);
		}
		
		protected void addElement(JobElement target) {
			elements.add(target);
		}
		
		/*
		 * フィルタ設定によるフィルタ表示で表示が必要な要素のリストを作成
		 */
		protected void filter() {
			filteredSet.clear();
			
			if (condition.isPresent()) {
				FilterCondition c = condition.get();
				
				Stream<JobElement> stream = elements.stream();
				
				if (c.jobId.isPresent() && !c.jobId.get().isEmpty()) {
					stream = stream.filter(e->e.item.getData().getId().contains(c.jobId.get()));
				}
				
				if (!c.types.isEmpty()) {
					stream = stream.filter(e->c.types.contains(e.item.getData().getType()));
				}
				
				if (c.startTimeRange.start.isPresent()) {
					SimpleDateFormat format = TimezoneUtil.getSimpleDateFormat();
					stream = stream.filter(e->{
						try {
							return c.startTimeRange.start.get().isBefore(format.parse(e.item.getDetail().getStartDate()).toInstant());
						} catch (ParseException ex) {
							return false;
						}
					});
				}
				
				if (c.startTimeRange.end.isPresent()) {
					SimpleDateFormat format = TimezoneUtil.getSimpleDateFormat();
					stream = stream.filter(e->{
						try {
							return c.startTimeRange.end.get().isAfter(format.parse(e.item.getDetail().getStartDate()).toInstant());
						} catch (ParseException ex) {
							return false;
						}
					});
				}
				
				if (c.endTimeRange.start.isPresent()) {
					SimpleDateFormat format = TimezoneUtil.getSimpleDateFormat();
					stream = stream.filter(e->{
						try {
							return c.endTimeRange.start.get().isBefore(format.parse(e.item.getDetail().getEndDate()).toInstant());
						} catch (ParseException ex) {
							return false;
						}
					});
				}
				
				if (c.endTimeRange.end.isPresent()) {
					SimpleDateFormat format = TimezoneUtil.getSimpleDateFormat();
					stream = stream.filter(e->{
						try {
							return c.endTimeRange.end.get().isAfter(format.parse(e.item.getDetail().getEndDate()).toInstant());
						} catch (ParseException ex) {
							return false;
						}
					});
				}
				
				if (!c.runStatus.isEmpty()) {
					stream = stream.filter(e->c.runStatus.contains(e.item.getDetail().getStatus()));
				}
				
				if (!c.endStatus.isEmpty()) {
					stream = stream.filter(e->c.endStatus.contains(e.item.getDetail().getEndStatus()));
				}
				
				filteredSet.addAll(stream.collect(Collectors.toList()));
			}
		}
		
		public void clear() {
			selectedSet.clear();
			filteredSet.clear();
			elements.clear();
			children.clear();
			root = null;
		}
		
		public boolean addSelectionListener(Consumer<Set<JobElement>> listener) {
			return selectionListeners.add(listener);
		}
		
		public boolean removeSelectionListener(Consumer<Set<JobElement>> listener) {
			return selectionListeners.remove(listener);
		}
		
		public List<Consumer<Set<JobElement>>> getSelectionListener() {
			return Collections.unmodifiableList(selectionListeners);
		}
		
		public void addChangeFilterListener(Consumer<FilterCondition> listener) {
			filterListeners.add(listener);
		}
		
		public void addFlatViewListener(Consumer<Boolean> listener) {
			flatViewListeners.add(listener);
		}
		
		public void addSelectedViewListener(Consumer<Boolean> listener) {
			selectedViewListeners.add(listener);
		}
		
		public void addFilteredViewListener(Consumer<Boolean> listener) {
			filteredViewListeners.add(listener);
		}
	}
	
	/*
	 * ジョブ履歴[ジョブ詳細]ビューの表示用モデルのルート要素
	 */
	public static class JobElement {
		protected final JobTreeItemWrapper item;
		
		protected final JobDetailViewModel root;
		protected final JobElement parent;
		
		protected final List<JobElement> children = new LinkedList<>();
		
		@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Internal UI model.")
		public JobElement(JobDetailViewModel root, JobTreeItemWrapper item, JobElement parent) {
			Objects.requireNonNull(root);
			Objects.requireNonNull(item);

			this.item = item;
			this.root = root;
			this.root.addElement(this);
			this.parent = parent;
			
			buildChildren();
		}
		
		@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Internal UI model.")
		public JobDetailViewModel getModel() {
			return root;
		}
		
		@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Internal UI model.")
		public JobElement getParent() {
			return parent;
		}
		
		@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Internal UI model.")
		public List<JobElement> getRawChildren() {
			return children;
		}
		
		private void buildChildren() {
			children.addAll(item.getChildren().stream().map(c->new JobElement(getModel(), c, this)).collect(Collectors.toList()));
		}
		
		public List<JobElement> getChildren() {
			// フラット表示中か？
			if (getModel().isFlatView()) {
				// フラット表示中なので、子要素の子要素はなし
				return Collections.emptyList();
			}
			
			boolean filtered = root.isFilteredView()
					&& root.getFilterCondition().isPresent()
					&& !root.getFilterCondition().get().isEmpty();

			boolean selected = root.isSelectedView();
			
			// 選択表示およびフィルター表示が無効か？
			if (!filtered && !selected) {
				return getRawChildren();
			}
			
			// 子要素で、表示対象があれば、それを返す
			return getRawChildren().stream()
					.filter(e -> ((!filtered || e.isFilteredVisible()) && (!selected || e.isSelectedVisible())) || e.hasVisibleChild())
					.collect(Collectors.toList());
		}
		
		protected boolean hasVisibleChild() {
			boolean filtered = root.isFilteredView()
					&& root.getFilterCondition().isPresent()
					&& !root.getFilterCondition().get().isEmpty();

			boolean selected = root.isSelectedView();
			
			boolean has = getRawChildren().stream().anyMatch(e->(!filtered || e.isFilteredVisible()) && (!selected || e.isSelectedVisible()) || e.hasVisibleChild());
			return has;
		}
		
		public boolean isSelected() {
			return getModel().selected(this);
		}
		
		public void setSelected(boolean selected) {
			if (selected) {
				getModel().select(this);
			} else {
				getModel().unselect(this);
			}
		}
		
		protected boolean isFilteredVisible() {
			boolean is = getModel().isFilteredVisible(this);
			return is;
		}
		
		protected boolean isSelectedVisible() {
			return getModel().isSelectedVisible(this);
		}
		
		public boolean isMatched() {
			return getModel().matched(this);
		}
		
		@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Internal UI model.")
		public JobTreeItemWrapper getItem() {
			return item;
		}
	}
	
	/** テーブルビューアー */
	private JobTableTreeViewer m_viewer = null;
	/** セッションID */
	private String m_sessionId = null;
	/** 所属ジョブユニットのジョブID */
	private String m_jobunitId = null;
	/** ジョブID */
	private String m_jobId = null;
	/** ジョブ名 */
	private String m_jobName = null;
	/** セッションID用ラベル */
	private Label m_sessionIdLabel = null;
	/** ジョブ検索窓 */
	private Text m_jobidText = null;
	/** マネージャ名 */
	private String m_managerName = null;

	protected final JobDetailViewModel model = new JobDetailViewModel();

	/**
	 * コンストラクタ
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public DetailComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
		
		// 表示更新のイベント検知のため各種リスナーを追加
		model.addChangeFilterListener(c->{
				getTableTreeViewer().refresh();
				if (m_jobidText != null) {
					m_jobidText.setText(Optional.ofNullable(c).map(r->r.jobId.orElse(null)).orElse(""));
				}
			});
		
		model.addSelectionListener(v->{
			getTableTreeViewer().refresh();
			});
		
		model.addFilteredViewListener(v->{
			getTableTreeViewer().refresh();
			getTableTreeViewer().expandAll();
			});
		
		model.addSelectedViewListener(v->{
			getTableTreeViewer().refresh();
			getTableTreeViewer().expandAll();
			});
		
		model.addFlatViewListener(v->{
			getTableTreeViewer().refresh();
			getTableTreeViewer().expandAll();
			});
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(2, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		//セッションIDラベル作成
		m_sessionIdLabel = new Label(this, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "sessionid", m_sessionIdLabel);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		m_sessionIdLabel.setLayoutData(gridData);
		
		Composite composite = new Composite(this, SWT.NONE);
		gridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		composite.setLayoutData(gridData);

		GridLayout gl_composite = new GridLayout(2, false);
		gl_composite.marginHeight = 3;
		gl_composite.marginWidth = 0;
		composite.setLayout(gl_composite);
	
		Label jobId = new Label(composite, SWT.RIGHT);
		jobId.setText(Messages.getString("job.id", Locale.getDefault()) + " : ");
		gridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		jobId.setLayoutData(gridData);
		
		//ジョブ検索窓
		m_jobidText = new Text(composite, SWT.BORDER);
		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gridData.widthHint = 300;
		m_jobidText.setLayoutData(gridData);
		
		m_jobidText.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					// ジョブ検索窓でリターンキーを押されたら、ジョブIDのフィルター条件を更新
					if (e.keyCode == SWT.CR) {
						Text t = (Text)e.widget;
						String s = t.getText().trim();
						
						FilterCondition c = new FilterCondition(model.getFilterCondition().orElseGet(()->new FilterCondition()));
						
						if (s.isEmpty()) {
							c.jobId = Optional.empty();
						} else {
							c.jobId = Optional.of(s);
						}
						
						model.setFilterCondition(c);
						
						getTableTreeViewer().refresh();
					}
				}
			});
		
		
		//ジョブ詳細テーブル作成
		Tree tree = new Tree(this, SWT.H_SCROLL | SWT.V_SCROLL | SWT.CHECK
				| SWT.FULL_SELECTION);
		WidgetTestUtil.setTestId(this, null, tree);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 3;
		tree.setLayoutData(gridData);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		m_viewer = new JobTableTreeViewer(tree);
		m_viewer.createTableColumn(GetJobDetailTableDefine.get(),
				GetJobDetailTableDefine.SORT_COLUMN_INDEX,
				GetJobDetailTableDefine.SORT_ORDER);
		// 列移動が可能に設定
		for (int i = 0; i < tree.getColumnCount(); i++) {
			tree.getColumn(i).setMoveable(true);
		}

		m_viewer.addSelectionChangedListener(
				new JobDetailSelectionChangedListener(this));

		m_viewer.addDoubleClickListener(e->{
			List<JobTreeItemWrapper> s = ((List<?>)((IStructuredSelection)e.getSelection()).toList()).stream()
					.map(o->((JobElement)o).getItem()).collect(Collectors.toList());
			SessionJobDoubleClickListener listener = new SessionJobDoubleClickListener(DetailComposite.this);
			listener.doubleClick(new DoubleClickEvent(e.getViewer(), new StructuredSelection(s.toArray())));
			});

		update(null, null, null);
	}

	/**
	 * テーブルビューアーを更新します。<BR>
	 * 引数で指定されたセッションIDのジョブ詳細一覧情報を取得し、
	 * 共通テーブルビューアーにセットします。
	 * <p>
	 * <ol>
	 * <li>引数で指定されたセッションIDのジョブ詳細一覧情報を取得します。</li>
	 * <li>共通テーブルビューアーにジョブ詳細一覧情報をセットします。</li>
	 * </ol>
	 *
	 * @param managerName マネージャ名
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 *
	 * @see com.clustercontrol.jobmanagement.action.GetJobDetail#getJobDetail(String, String)
	 * @see #setJobId(String)
	 */
	public void update(String managerName, String sessionId, String jobunitId) {
		long start = HinemosTime.currentTimeMillis();
		if (m_log.isDebugEnabled()) {
			m_log.debug("DetailComposite update() is start : m_sessionId=" + sessionId + ", startTime="  + start + "ms.");
		}
		//ジョブ詳細情報取得
		JobTreeItemWrapper item = null;
		if (sessionId != null && sessionId.length() > 0) {
			try {
				JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
				JobTreeItemResponseP4 detail = wrapper.getJobDetailList(sessionId);
				item = JobTreeItemUtil.getItemFromP4(detail);
			} catch (InvalidRole e) {
				if(ClientSession.isDialogFree()){
					ClientSession.occupyDialog();
					MessageDialog.openInformation(null, Messages.getString("message"),
							Messages.getString("message.accesscontrol.16"));
					ClientSession.freeDialog();
				}
			} catch (JobInfoNotFound e) {
				// 実行契機削除などでジョブセッション削除のタイミングで履歴情報取得した場合の対策
				// itemはnullのままにする
			} catch (Exception e) {
				m_log.warn("update() getJobDetailList, " + e.getMessage(), e);
				if(ClientSession.isDialogFree()){
					ClientSession.occupyDialog();
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
					ClientSession.freeDialog();
				}
			}
		}
		setItem(managerName, sessionId, jobunitId, item);
		if (m_log.isDebugEnabled()) {
			long end = HinemosTime.currentTimeMillis();
			m_log.debug("DetailComposite update() is end :  m_sessionId=" + sessionId + ", endTime=" + end  + "ms, diffTime="  + (end - start) + "ms.");
		}
	}

	/**
	 * 取得したアイテムをセットします。
	 * @param managerName マネージャ名
	 * @param sessionId セッションID
	 * @param jobunitId ジョブユニットID
	 * @param item アイテム情報
	 */
	public void setItem(String managerName, String sessionId, String jobunitId, JobTreeItemWrapper item) {
		if (item != null
				&& m_sessionId != null && m_sessionId.length() > 0
				&& sessionId != null && sessionId.length() > 0
				&& m_sessionId.compareTo(sessionId) == 0) {
			selectDetail(item.getChildren().get(0));
		} else {
			setJobId(null);
		}
		
		if (item != null) {
			model.update(item);
		} else {
			model.clear();
		}

		getTableTreeViewer().setInput(model);
		getTableTreeViewer().expandAll();
		
		m_managerName = managerName;
		m_sessionId = sessionId;
		m_jobunitId = jobunitId;

		//セッションIDを表示
		if (m_sessionId != null) {
			m_sessionIdLabel.setText(Messages.getString("session.id") + " : "
					+ m_sessionId);
		} else {
			m_sessionIdLabel.setText(Messages.getString("session.id") + " : ");
		}
	}

	/**
	 * ジョブ詳細の行を選択します。<BR>
	 * 前回選択したジョブIDと同じジョブIDの行を選択します。
	 *
	 * @param item テーブルツリーアイテム
	 */
	public void selectDetail(JobTreeItemWrapper item) {
		if (getJobId() != null && getJobId().length() > 0) {
			if (m_viewer.getSelection().isEmpty()) {
				boolean select = false;
				JobInfoWrapper info = item.getData();
				if (info == null) {
					m_log.info("selectDetail info is null");
					return;
				}
				String jobId = info.getId();
				if (getJobId().compareTo(jobId) == 0) {
					select = true;
				}

				if (select) {
					m_viewer.setSelection(new StructuredSelection(item), true);
				} else {
					for (int i = 0; i < item.getChildren().size(); i++) {
						JobTreeItemWrapper children = item.getChildren().get(i);
						selectDetail(children);
					}
				}
			}
		}
	}

	/**
	 * このコンポジットが利用するテーブルツリービューアを返します。
	 *
	 * @return テーブルツリービューア
	 */
	public final JobTableTreeViewer getTableTreeViewer() {
		return m_viewer;
	}

	public Tree getTree() {
		return m_viewer.getTree();
	}

	/**
	 * セッションIDを返します。
	 *
	 * @return セッションID
	 */
	public String getSessionId() {
		return m_sessionId;
	}

	/**
	 * セッションIDを設定します。
	 *
	 * @param sessionId セッションID
	 */
	public void setSessionId(String sessionId) {
		m_sessionId = sessionId;
	}

	/**
	 * ジョブIDを返します。
	 *
	 * @return ジョブID
	 */
	public String getJobId() {
		return m_jobId;
	}

	/**
	 * ジョブIDを設定します。
	 *
	 * @param jobId ジョブID
	 */
	public void setJobId(String jobId) {
		m_jobId = jobId;
	}

	/**
	 * ジョブ名を返します。
	 *
	 * @return ジョブ名
	 */
	public String getJobName() {
		return m_jobName;
	}

	/**
	 * ジョブ名を設定します。
	 *
	 * @param jobName ジョブ名
	 */
	public void setJobName(String jobName) {
		m_jobName = jobName;
	}

	/**
	 * 所属ジョブユニットのジョブIDを返します。
	 *
	 * @return 所属ジョブユニットのジョブID
	 */
	public String getJobunitId() {
		return m_jobunitId;
	}

	/**
	 * 所属ジョブユニットのジョブIDを設定します。
	 *
	 * @param jobunitId 所属ジョブユニットのジョブID
	 */
	public void setJobunitId(String jobunitId) {
		m_jobunitId = jobunitId;
	}

	/**
	 * マネージャ名を取得します。
	 * @return the m_managerName
	 */
	public String getManagerName() {
		return m_managerName;
	}

	/**
	 * マネージャ名を設定します。
	 * @param m_managerName the m_managerName to set
	 */
	public void setManagerName(String managerName) {
		this.m_managerName = managerName;
	}

	@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Internal UI model.")
	public JobDetailViewModel getJobDetailViewModel() {
		return model;
	}
}
