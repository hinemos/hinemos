/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.GetJobLinkMessageListRequest;
import org.openapitools.client.model.GetJobLinkMessageListResponse;
import org.openapitools.client.model.JobLinkMessageResponse;
import org.openapitools.client.model.JobLinkMessageResponse.PriorityEnum;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.jobmanagement.action.GetJobLinkMessageTableDefine;
import com.clustercontrol.jobmanagement.dialog.JobLinkMessageDetailDialog;
import com.clustercontrol.jobmanagement.preference.JobManagementPreferencePage;
import com.clustercontrol.jobmanagement.util.JobPropertyUtil;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.jobmanagement.view.JobLinkMessageView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * ジョブ履歴[受信ジョブ連携メッセージ一覧]ビュー用のコンポジットクラスです。
 *
 */
public class JobLinkMessageComposite extends Composite {
	private static Log m_log = LogFactory.getLog(JobLinkMessageComposite.class);

	/** テーブルビューア */
	private CommonTableViewer m_viewer = null;
	/** ヘッダ用ラベル */
	private Label m_labelType = null;
	/** 件数用ラベル */
	private Label m_labelCount = null;

	/** ジョブ連携メッセージID */
	private String m_joblinkMessageId = null;
	/** マネージャ名 */
	private String m_managerName = null;

	private JobLinkMessageView m_view = null;

	/** 更新成功可否フラグ */
	private boolean m_updateSuccess = true;

	/** 受信ジョブ連携メッセージ情報マップ(マネージャ名＋キー、ジョブ連携メッセージ情報) */
	private HashMap<JobLinkMessageKey, JobLinkMessageResponseEx> m_infoMap = new HashMap<>();

	/**
	 * コンストラクタ
	 *
	 * @param parent
	 *            親のコンポジット
	 * @param style
	 *            スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int
	 *      style)
	 * @see #initialize()
	 */
	public JobLinkMessageComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	public void setView(JobLinkMessageView view) {
		m_view = view;
	}

	public JobLinkMessageView getView() {
		return m_view;
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		m_labelType = new Label(this, SWT.LEFT);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		m_labelType.setLayoutData(gridData);

		Table table = new Table(this, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		table.setLayoutData(gridData);

		m_labelCount = new Label(this, SWT.RIGHT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		m_labelCount.setLayoutData(gridData);

		m_viewer = new CommonTableViewer(table);
		m_viewer.createTableColumn(GetJobLinkMessageTableDefine.get(), 
				GetJobLinkMessageTableDefine.SORT_COLUMN_INDEX1,
				GetJobLinkMessageTableDefine.SORT_COLUMN_INDEX2,
				GetJobLinkMessageTableDefine.SORT_ORDER);

		// 列移動が可能に設定
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumn(i).setMoveable(true);
		}

		m_viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				int order = m_viewer.getTable().getSelectionIndex();
				if (order >= 0) {
					ArrayList<?> data = (ArrayList<?>) m_viewer.getTable().getSelection()[0].getData();
					JobLinkMessageKey key = new JobLinkMessageKey(
							(String) data.get(GetJobLinkMessageTableDefine.MANAGER_NAME),
							(String) data.get(GetJobLinkMessageTableDefine.JOBLINK_MESSAGE_ID),
							(String) data.get(GetJobLinkMessageTableDefine.FACILITY_ID),
							(Date) data.get(GetJobLinkMessageTableDefine.SEND_DATE));
					if (m_infoMap.containsKey(key)) {
						Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
						JobLinkMessageDetailDialog dialog = new JobLinkMessageDetailDialog(shell, m_infoMap.get(key));
						dialog.open();
					}
				}
			}
		});
	}

	/**
	 * テーブルビューアーを更新します。<BR>
	 * 受信ジョブ連携メッセージ一覧情報を取得し、共通テーブルビューアーにセットします。
	 */
	@Override
	public void update() {
		update(null);
	}

	/**
	 * テーブルビューアを更新します。<BR>
	 * 引数で指定された条件に一致するジョブ連携メッセージ一覧情報を取得し、共通テーブルビューアーにセットします。
	 *
	 * @param condition
	 *            検索条件
	 */
	public void update(Property condition) {

		// ジョブ連携メッセージ一覧情報取得
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();
		int total = 0;
		int size = 0;
		String conditionManager = null;
		if (condition != null) {
			conditionManager = JobPropertyUtil.getManagerName(condition);
		}

		// 初期化
		m_infoMap.clear();

		if (conditionManager == null || conditionManager.equals("")) {
			for (String managerName : RestConnectManager.getActiveManagerSet()) {
				int[] ret = getList(managerName, condition, m_infoMap, errorMsgs);
				total += ret[0];
				size += ret[1];
			}
		} else {
			int[] ret = getList(conditionManager, condition, m_infoMap, errorMsgs);
			total = ret[0];
			size = ret[1];
		}

		// メッセージ表示
		if (0 < errorMsgs.size() && ClientSession.isDialogFree()) {
			ClientSession.occupyDialog();
			m_updateSuccess = false;
			UIManager.showMessageBox(errorMsgs, true);
			ClientSession.freeDialog();
		}

		if (ClusterControlPlugin.getDefault().getPreferenceStore()
				.getBoolean(JobManagementPreferencePage.P_HISTORY_MESSAGE_FLG)) {
			if (total > size) {
				if (ClientSession.isDialogFree()) {
					ClientSession.occupyDialog();
					// 最大表示件数を超える場合、エラーダイアログを表示する
					MessageDialogWithToggle.openInformation(null, Messages.getString("message"),
							Messages.getString("message.job.33"), Messages.getString("message.will.not.be.displayed"),
							false, ClusterControlPlugin.getDefault().getPreferenceStore(),
							JobManagementPreferencePage.P_HISTORY_MESSAGE_FLG);
					ClientSession.freeDialog();
				}
			}
		}

		List<JobLinkMessageResponseEx> list = jobLinkMessageDataMap2SortedList();
		size = list.size();

		ArrayList<Object> listInput = new ArrayList<Object>();
		for (JobLinkMessageResponseEx info : list) {
			ArrayList<Object> a = new ArrayList<Object>();
			a.add(info.getManagerName());
			a.add(info.getJoblinkMessageId());
			a.add(info.getFacilityId());
			a.add(info.getFacilityName());
			a.add(info.getMonitorDetailId());
			a.add(info.getApplication());
			if (info.getPriority() == null) {
				a.add(null);
			} else if (info.getPriority() == PriorityEnum.INFO) {
				a.add(PriorityConstant.TYPE_INFO);
			} else if (info.getPriority() == PriorityEnum.WARNING) {
				a.add(PriorityConstant.TYPE_WARNING);
			} else if (info.getPriority() == PriorityEnum.CRITICAL) {
				a.add(PriorityConstant.TYPE_CRITICAL);
			} else if (info.getPriority() == PriorityEnum.UNKNOWN) {
				a.add(PriorityConstant.TYPE_UNKNOWN);
			}
			a.add(info.getMessage());
			a.add(info.getMessageOrg());
			a.add(info.getSendDateTime());
			a.add(info.getAcceptDateTime());
			a.add(null);
			listInput.add(a);
		}
		m_viewer.setInput(listInput);

		selectList(listInput);

		if (condition != null) {
			m_labelType.setText(Messages.getString("filtered.list"));
			Object[] args = null;
			if (total > size) {
				args = new Object[] { size };
			} else {
				args = new Object[] { total };
			}
			m_labelCount.setText(Messages.getString("filtered.records", args));
		} else {
			// 表示件数をセット(最大件数以上に達しているか否かの分岐)
			m_labelType.setText("");
			Object[] args = null;
			if (total > size) {
				args = new Object[] { size };
			} else {
				args = new Object[] { total };
			}
			m_labelCount.setText(Messages.getString("records", args));
		}
	}

	private List<JobLinkMessageResponseEx> jobLinkMessageDataMap2SortedList() {
		List<JobLinkMessageResponseEx> ret = new ArrayList<>();

		for (Map.Entry<JobLinkMessageKey, JobLinkMessageResponseEx> entry : m_infoMap.entrySet()) {
			ret.add(entry.getValue());
		}

		// Sort - 送信日時 降順で並べ替え
		Collections.sort(ret, new Comparator<JobLinkMessageResponseEx>() {
			@Override
			public int compare(JobLinkMessageResponseEx o1, JobLinkMessageResponseEx o2) {
				if (o2.getSendDate().compareTo(o1.getSendDate()) == 0) {
					return o2.getManagerName().compareTo(o1.getManagerName());
				} else {
					return o2.getSendDate().compareTo(o1.getSendDate());
				}
			}
		});

		// Slice array
		int max = ClusterControlPlugin.getDefault().getPreferenceStore()
				.getInt(JobManagementPreferencePage.P_HISTORY_MAX_HISTORIES);
		int len = ret.size();
		if (len > max) {
			ret.subList(max, len).clear();
		}
		return ret;
	}

	private int[] getList(String managerName, Property condition,
			Map<JobLinkMessageKey, JobLinkMessageResponseEx> infoMap, Map<String, String> errorMsgs) {
		GetJobLinkMessageListResponse messageInfo = null;

		int total = 0;
		int size = 0;
		try {
			int histories = ClusterControlPlugin.getDefault().getPreferenceStore()
					.getInt(JobManagementPreferencePage.P_HISTORY_MAX_HISTORIES);
			JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);

			if (condition == null) {
				GetJobLinkMessageListRequest request = new GetJobLinkMessageListRequest();
				request.setFilterInfo(null);
				request.setSize(histories);
				messageInfo = wrapper.getJobLinkMessageList(request);
			} else {
				GetJobLinkMessageListRequest request = new GetJobLinkMessageListRequest();
				request.setFilterInfo(JobPropertyUtil.property2jobLinkMessageFilter(condition));
				request.setSize(histories);
				messageInfo = wrapper.getJobLinkMessageList(request);
			}
			total = messageInfo.getTotal();
			size = messageInfo.getList().size();

			// 検索結果の日時はyyyy/MM/dd HH:mm:ss.SSSのフォーマット
			SimpleDateFormat dateFormat = new SimpleDateFormat(JobRestClientWrapper.DATETIME_FORMAT);
			dateFormat.setTimeZone(TimezoneUtil.getTimeZone());
			Date sendDateTime = null;
			Date acceptDateTime = null;

			for (JobLinkMessageResponse response : messageInfo.getList()) {
				if (response.getSendDate() != null) {
					try {
						sendDateTime = new Date(dateFormat.parse(response.getSendDate()).getTime());
					} catch (ParseException e) {
						m_log.warn("update() : send date parse failed. " + e.getMessage());
					}
				}
				if (response.getAcceptDate() != null) {
					try {
						acceptDateTime = new Date(dateFormat.parse(response.getAcceptDate()).getTime());
					} catch (ParseException e) {
						m_log.warn("update() : accept date parse failed. " + e.getMessage());
					}
				}
				infoMap.put(new JobLinkMessageKey(managerName, response.getJoblinkMessageId(),
						response.getFacilityId(), sendDateTime),
						new JobLinkMessageResponseEx(response, managerName, sendDateTime, acceptDateTime));
			}
		} catch (InvalidRole e) {
			errorMsgs.put(managerName, Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			errorMsgs.put(managerName, Messages.getString("message.hinemos.failure.unexpected") + ", "
					+ HinemosMessage.replace(e.getMessage()));
		}

		int[] ret = { total, size };
		return ret;
	}

	/**
	 * 受信ジョブ連携メッセージ一覧情報の行を選択します。<BR>
	 * 前回選択したマネージャ名とジョブ連携メッセージIDが一致する行を選択します。
	 *
	 * @param info
	 *            受信ジョブ連携メッセージ一覧情報
	 */
	public void selectList(ArrayList<Object> info) {
		if ((m_managerName != null && m_managerName.length() > 0)
				&& (m_joblinkMessageId != null && m_joblinkMessageId.length() > 0)) {
			int index = -1;
			for (int i = 0; i < info.size(); i++) {
				ArrayList<?> line = (ArrayList<?>) info.get(i);
				String managerName = (String) line.get(GetJobLinkMessageTableDefine.MANAGER_NAME);
				String joblinkMessageId = (String) line.get(GetJobLinkMessageTableDefine.JOBLINK_MESSAGE_ID);
				if (m_managerName.compareTo(managerName) == 0 && m_joblinkMessageId.compareTo(joblinkMessageId) == 0) {
					index = i;
					break;
				}
			}
			if (index == -1) {
				m_managerName = null;
				m_joblinkMessageId = null;
			} else {
				m_viewer.setSelection(new StructuredSelection(info.get(index)), true);
			}
		}
	}

	/**
	 * このコンポジットが利用するテーブルビューアを返します。
	 *
	 * @return テーブルビューア
	 */
	public TableViewer getTableViewer() {
		return m_viewer;
	}

	/**
	 * このコンポジットが利用するテーブルを返します。
	 *
	 * @return テーブル
	 */
	public Table getTable() {
		return m_viewer.getTable();
	}

	/**
	 * ジョブ連携メッセージIDを返します。
	 *
	 * @return ジョブ連携メッセージID
	 */
	public String getJoblinkMesasgeId() {
		return m_joblinkMessageId;
	}

	/**
	 * ジョブ連携メッセージIDを設定します。
	 *
	 * @param joblinkMessageId
	 *            ジョブ連携メッセージID
	 */
	public void setJoblinkMessageId(String joblinkMessageId) {
		m_joblinkMessageId = joblinkMessageId;
	}

	/**
	 * @return the m_managerName
	 */
	public String getManagerName() {
		return m_managerName;
	}

	/**
	 * @param m_managerName
	 *            the m_managerName to set
	 */
	public void setManagerName(String m_managerName) {
		this.m_managerName = m_managerName;
	}

	/**
	 * 更新成功可否を返します。
	 * 
	 * @return 更新成功可否
	 */
	public boolean isUpdateSuccess() {
		return this.m_updateSuccess;
	}

	/**
	 * 表示順のソート処理の都合上、マネージャ名を付与したJobLinkMessageResponse のListが必要なため 独自拡張したデータクラス
	 * 
	 */
	public static class JobLinkMessageResponseEx extends JobLinkMessageResponse {

		String managerName;
		Date sendDateTime;
		Date acceptDateTime;

		public JobLinkMessageResponseEx(JobLinkMessageResponse org, String managerName, Date sendDateTime, Date acceptDateTime) {
			this.managerName = managerName;
			this.sendDateTime = sendDateTime;
			this.acceptDateTime = acceptDateTime;
			try {
				RestClientBeanUtil.convertBean(org, this);
			} catch (Exception e) {
				// ここには来ない想定（拡張元クラスから拡張先へのデータ移動のため）
				m_log.error("JobLinkMessageResponseEx init :" + e.getMessage(), e);
			}
		}

		public String getManagerName() {
			return this.managerName;
		}

		public Date getSendDateTime() {
			return this.sendDateTime;
		}

		public Date getAcceptDateTime() {
			return this.acceptDateTime;
		}

		@Override
		public boolean equals(Object target) {
			return super.equals(target);
		}

		@Override
		public int hashCode() {
			return super.hashCode();
		}
	}

	/**
	 * マップのキー情報
	 * 
	 */
	public static class JobLinkMessageKey {
		String managerName;
		String joblinkMessageId;
		String facilityId;
		Date sendDateTime = null;

		public JobLinkMessageKey(String managerName, String joblinkMessageId, String facilityId, Date sendDateTime) {
			this.managerName = managerName;
			this.joblinkMessageId = joblinkMessageId;
			this.facilityId = facilityId;
			this.sendDateTime = sendDateTime;
		}

		public String getManagerName() {
			return managerName;
		}

		public String getJoblinkMessageId() {
			return joblinkMessageId;
		}

		public String getFacilityId() {
			return facilityId;
		}

		public Date getSendDateTime() {
			return sendDateTime;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((facilityId == null) ? 0 : facilityId.hashCode());
			result = prime * result + ((joblinkMessageId == null) ? 0 : joblinkMessageId.hashCode());
			result = prime * result + ((managerName == null) ? 0 : managerName.hashCode());
			result = prime * result + ((sendDateTime == null) ? 0 : sendDateTime.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			JobLinkMessageKey other = (JobLinkMessageKey) obj;
			if (facilityId == null) {
				if (other.facilityId != null) {
					return false;
				}
			} else if (!facilityId.equals(other.facilityId)) {
				return false;
			}
			if (joblinkMessageId == null) {
				if (other.joblinkMessageId != null) {
					return false;
				}
			} else if (!joblinkMessageId.equals(other.joblinkMessageId)) {
				return false;
			}
			if (managerName == null) {
				if (other.managerName != null) {
					return false;
				}
			} else if (!managerName.equals(other.managerName)) {
				return false;
			}
			if (sendDateTime == null) {
				if (other.sendDateTime != null) {
					return false;
				}
			} else if (!sendDateTime.equals(other.sendDateTime)) {
				return false;
			}
			return true;
		}
	}
}
