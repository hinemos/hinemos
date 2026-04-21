/*
 * Copyright (c) 2026 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.dialog;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.JobDetailInfoResponse.EndStatusEnum;
import org.openapitools.client.model.JobDetailInfoResponse.StatusEnum;
import org.openapitools.client.model.JobInfoResponse.TypeEnum;

import com.clustercontrol.bean.EndStatusImageConstant;
import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.bean.JobImageConstant;
import com.clustercontrol.bean.StatusMessage;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.jobmanagement.JobMessage;
import com.clustercontrol.jobmanagement.bean.StatusImageConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.viewer.AbstractHierarchicalElementViewer;
import com.clustercontrol.xcloud.ui.dialogs.DialogConstants;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/*
 * フィルタ条件を指定するダイアログ
 */
public class JobDetailFilterDialog extends CommonDialog {

	protected static class Viewer extends AbstractHierarchicalElementViewer {
		public static final String CLMN_KEY = "key";
		public static final String CLMN_VALUE = "value";
		
		private final FilterCondition filterCondition;
		
		public Viewer(Composite parent, Optional<FilterCondition> filterCondition) {
			super(parent);
			Objects.requireNonNull(filterCondition);
			this.filterCondition = filterCondition.map(c->new FilterCondition(c)).orElseGet(()->new FilterCondition());
			initializeColumns(setupColumn());
			setInput(buildInput());
		}

		protected final LinkedHashMap<String, TreeViewerColumn> setupColumn() {
			LinkedHashMap<String, TreeViewerColumn> map = new LinkedHashMap<>();

			TreeViewerColumn keyColumn = new TreeViewerColumn(this, SWT.NONE, 0);
			keyColumn.getColumn().setText(Messages.getString("name"));
			keyColumn.getColumn().setWidth(200);
			map.put(CLMN_KEY, keyColumn);
			
			TreeViewerColumn valueColumn = new TreeViewerColumn(this, SWT.NONE);
			valueColumn.getColumn().setText(Messages.getString("value"));
			valueColumn.getColumn().setWidth(220);
			map.put(CLMN_VALUE, valueColumn);

			return map;
		}

		protected final Element[] buildInput() {
			// ジョブID
			Element jobId = Element.build()
				.addProperty(
					PropertyBinding.build(CLMN_KEY, ()->Messages.get("job.id")),
					PropertyBinding.buildText(CLMN_VALUE,
						()->filterCondition.jobId.orElse(null),
						v->filterCondition.jobId = Optional.ofNullable(v)
						)
					);
			
			// ジョブ種別
			Element type = Element.build()
				.addProperty(PropertyBinding.build(CLMN_KEY, ()->Messages.get("type")));
			
			List<TypeEnum> types = Arrays.stream(TypeEnum.values()).filter(
					t->!TypeEnum.MANAGER.equals(t)&&!TypeEnum.JOBUNIT_UNREFERABLE.equals(t)&&!TypeEnum.COMPOSITE.equals(t)
					).collect(Collectors.toList());
			
			types.forEach(
					s->{
						type.addChild(
							Element.build().addProperty(
								PropertyBinding.build(CLMN_KEY,
									()->JobMessage.typeEnumValueToString(s.getValue()),
									()->JobImageConstant.typeEnumValueToImage(s.getValue())
									),
								PropertyBinding.buildCheckBox(CLMN_VALUE,
									()->filterCondition.types.contains(s),
									c->{if (c) {filterCondition.types.add(s);} else {filterCondition.types.remove(s);}}
									)
								)
							);
						}
					);
			
			// 開始時刻
			Element startTime = Element.build()
				.addProperty(PropertyBinding.build(CLMN_KEY, ()->Messages.get("start.rerun.time")))
				.addChild(
					Element.build().addProperty(
						PropertyBinding.build(CLMN_KEY, ()->Messages.get("start")),
						PropertyBinding.buildDateTime(CLMN_VALUE,
							()->filterCondition.startTimeRange.start.map(t->Date.from(t)).orElse(null),
							v->filterCondition.startTimeRange.start = Optional.ofNullable(v).map(r->Optional.of(r.toInstant())).orElse(Optional.empty())
							)
						),
					Element.build().addProperty(
						PropertyBinding.build(CLMN_KEY, ()->Messages.get("end")),
						PropertyBinding.buildDateTime(CLMN_VALUE,
							()->filterCondition.startTimeRange.end.map(t->Date.from(t)).orElse(null),
							v->filterCondition.startTimeRange.end = Optional.ofNullable(v).map(r->Optional.of(r.toInstant())).orElse(Optional.empty())
							)
						)
					);
			
			// 完了時刻
			Element endTIme = Element.build()
				.addProperty(PropertyBinding.build(CLMN_KEY, ()->Messages.get("end.suspend.time")))
				.addChild(
					Element.build().addProperty(
						PropertyBinding.build(CLMN_KEY, ()->Messages.get("start")),
						PropertyBinding.buildDateTime(CLMN_VALUE,
							()->filterCondition.endTimeRange.start.map(t->Date.from(t)).orElse(null),
							v->filterCondition.endTimeRange.start = Optional.ofNullable(v).map(r->Optional.of(r.toInstant())).orElse(Optional.empty())
							)
						),
					Element.build().addProperty(
						PropertyBinding.build(CLMN_KEY, ()->Messages.get("end")),
						PropertyBinding.buildDateTime(CLMN_VALUE,
							()->filterCondition.endTimeRange.end.map(t->Date.from(t)).orElse(null),
							v->filterCondition.endTimeRange.end = Optional.ofNullable(v).map(r->Optional.of(r.toInstant())).orElse(Optional.empty())
							)
						)
					);
			
			// 実行状態
			Element runStatus = Element.build().addProperty(PropertyBinding.build(CLMN_KEY, ()->Messages.get("run.status")));
			
			StatusEnum[] runStatuses = {
				StatusEnum.SCHEDULED,
				StatusEnum.RUNNING,
				StatusEnum.RUNNING_QUEUE,
				StatusEnum.STOPPING,
				StatusEnum.SUSPEND,
				StatusEnum.SUSPEND_QUEUE,
				StatusEnum.STOP,
				StatusEnum.END,
				StatusEnum.END_QUEUE_LIMIT,
				StatusEnum.MODIFIED};
			Arrays.stream(runStatuses).forEach(
				s->{
					runStatus.addChild(
						Element.build().addProperty(
							PropertyBinding.build(CLMN_KEY,
								()->StatusMessage.typeEnumValueToString(s.getValue()),
								()->StatusImageConstant.typeEnumValueToImage(s.getValue())
								),
							PropertyBinding.buildCheckBox(CLMN_VALUE,
								()->filterCondition.runStatus.contains(s),
								c->{if (c) {filterCondition.runStatus.add(s);} else {filterCondition.runStatus.remove(s);}}
								)
							)
						);
					}
				);
			
			// 完了状態
			Element endStatus = Element.build().addProperty(PropertyBinding.build(CLMN_KEY, ()->Messages.get("end.status")));
			
			EndStatusEnum[] endStatuses = {
				EndStatusEnum.NORMAL,
				EndStatusEnum.ABNORMAL,
				EndStatusEnum.WARNING};
			Arrays.stream(endStatuses).forEach(
				s->{
					endStatus.addChild(
						Element.build().addProperty(
							PropertyBinding.build(CLMN_KEY,
								()->EndStatusMessage.typeEnumValueToString(s.getValue()),
								()->EndStatusImageConstant.typeEnumValueToImage(s.getValue())
								),
							PropertyBinding.buildCheckBox(CLMN_VALUE,
								()->filterCondition.endStatus.contains(s),
								c->{if (c) {filterCondition.endStatus.add(s);} else {filterCondition.endStatus.remove(s);}}
								)
							)
						);
					}
				);
				
			return new Element[]{jobId, type, startTime, endTIme, runStatus, endStatus};
		}
		
		@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Internal UI model.")
		public FilterCondition getFilterCondition() {
			return filterCondition;
		}
	}
	
	public static class DateTimeRange {
		
		public Optional<Instant> start;
		public Optional<Instant> end;
		
		public DateTimeRange() {
			this.start = Optional.empty();
			this.end =  Optional.empty();
		}
		
		public DateTimeRange(Optional<Instant> start, Optional<Instant> end) {
			this.start = start;
			this.end = end;
		}
		
		public DateTimeRange(DateTimeRange other) {
			this.start = other.start;
			this.end = other.end;
		}
		
		public void clear() {
			this.start = Optional.empty();
			this.end = Optional.empty();
		}
	}
	
	public static class FilterCondition {
		// ジョブID
		public Optional<String> jobId;

		// 実行状態
		public final Set<TypeEnum> types = new LinkedHashSet<>();
		
		// 開始・再実行日時の開始時間
		public final DateTimeRange startTimeRange;
		
		// 終了・中断日時（自）
		public final DateTimeRange endTimeRange;

		// 実行状態
		public final Set<StatusEnum> runStatus = new LinkedHashSet<>();

		// 終了状態
		public final Set<EndStatusEnum> endStatus = new LinkedHashSet<>();
		
		public FilterCondition() {
			this.jobId = Optional.empty();
			this.startTimeRange = new DateTimeRange();
			this.endTimeRange = new DateTimeRange();
		}
		
		public FilterCondition(Optional<String> jobId, TypeEnum[] types, Optional<DateTimeRange> startTimeRange, Optional<DateTimeRange> endTimeRange, StatusEnum[] runStatus, EndStatusEnum[] endStatus) {
			this.jobId = jobId;
			Optional.ofNullable(types).ifPresent(s->this.types.addAll(Arrays.asList(s)));
			this.startTimeRange = startTimeRange.map(e->new DateTimeRange(e)).orElseGet(()->new DateTimeRange());
			this.endTimeRange = endTimeRange.map(e->new DateTimeRange(e)).orElseGet(()->new DateTimeRange());
			Optional.ofNullable(runStatus).ifPresent(s->this.runStatus.addAll(Arrays.asList(s)));
			Optional.ofNullable(endStatus).ifPresent(s->this.endStatus.addAll(Arrays.asList(s)));
		}
		
		public FilterCondition(FilterCondition other) {
			this.jobId = other.jobId;
			this.types.addAll(other.types);
			this.startTimeRange = new DateTimeRange(other.startTimeRange);
			this.endTimeRange = new DateTimeRange(other.endTimeRange);
			this.runStatus.addAll(other.runStatus);
			this.endStatus.addAll(other.endStatus);
		}
		
		public boolean isEmpty() {
			return (!jobId.isPresent() || jobId.get().isEmpty()) &&
					types.isEmpty() &&
					!startTimeRange.start.isPresent() && !startTimeRange.end.isPresent() &&
					!endTimeRange.start.isPresent() && !endTimeRange.end.isPresent() &&
					runStatus.isEmpty() &&
					endStatus.isEmpty();
		}
		
		public void clear() {
			jobId = Optional.empty();
			types.clear();
			startTimeRange.clear();
			endTimeRange.clear();
			runStatus.clear();;
			endStatus.clear();
		}
	}
	
	protected Optional<FilterCondition> filtercondition;
	
	protected Viewer viewer;
	
	public JobDetailFilterDialog(Shell parent) {
		super(parent);
	}
	
	public JobDetailFilterDialog(Shell parent, Optional<FilterCondition> filtercondition) {
		super(parent);
		this.filtercondition = Optional.of(filtercondition.map(c->new FilterCondition(c)).orElse(new FilterCondition()));
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.job.filter.jobdetail"));
	}
	
	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のコンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		GridLayout gl = new GridLayout(2, false);
		gl.marginHeight = 10;
		gl.marginWidth = 10;
		
		parent.setLayout(gl);
		
		viewer = new Viewer(parent, filtercondition);
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		viewer.expandAll();
		
		Label dummy = new Label(parent, SWT.NONE);
		dummy.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Button btnClear = new Button(parent, SWT.PUSH);
		btnClear.setText(Messages.getString("clear.2"));
		btnClear.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		
		btnClear.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					viewer.getFilterCondition().clear();
					viewer.refresh();
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, DialogConstants.OK_ID, DialogConstants.OK_LABEL, true);
		createButton(parent, DialogConstants.CANCEL_ID, DialogConstants.CANCEL_LABEL, false);
	}
	
	@Override
	protected void okPressed() {
		filtercondition = Optional.of(new FilterCondition(viewer.getFilterCondition()));
		super.okPressed();
	}
	
	public Optional<FilterCondition> getFilterCondition() {
		return filtercondition;
	}
}
