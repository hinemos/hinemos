package com.clustercontrol.repository.dialog;

import java.util.List;

import javax.xml.ws.WebServiceException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.SnmpProtocolConstant;
import com.clustercontrol.bean.SnmpSecurityLevelConstant;
import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.repository.util.NodeSearchUtil;
import com.clustercontrol.repository.util.RepositoryEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.repository.FacilityDuplicate_Exception;
import com.clustercontrol.ws.repository.HinemosUnknown_Exception;
import com.clustercontrol.ws.repository.InvalidRole_Exception;
import com.clustercontrol.ws.repository.InvalidSetting_Exception;
import com.clustercontrol.ws.repository.InvalidUserPass_Exception;
import com.clustercontrol.ws.repository.NodeInfoDeviceSearch;
import com.clustercontrol.ws.repository.SnmpResponseError_Exception;

public class NodeSearchDialog extends CommonDialog {
	/** マネージャ名コンボボックス用コンポジット */
	private ManagerListComposite m_managerComposite = null;

	/** オーナーロールID用テキスト */
	private RoleIdListComposite m_ownerRoleId = null;

	private Label    ipAddressText = null; //"IP Address";
	private Text     ipAddressBoxFrom  = null;
	private Text     ipAddressBoxTo  = null;
	private Label    communityText = null; //"community";
	private Text     communityBox  = null;
	private Label    portText      = null; //"port";
	private Text     portBox       = null;
	private Label    versionText      = null; //"version";
	private Combo    versionBox       = null;
	private Label    securityLevelText = null;
	private Combo    securityLevelBox       = null;
	private Label    userText = null;
	private Text     userBox  = null;
	private Label    authPassText = null;
	private Text     authPassBox  = null;
	private Label    privPassText = null;
	private Text     privPassBox  = null;
	private Label    empty_label1 = null;
	private Label    empty_label2 = null;
	private Label    empty_label3 = null;
	private Label    empty_label4 = null;
	private Label    empty_label5 = null;
	private Label    empty_label6 = null;
	private Label    authProtocolText = null;
	private Combo    authProtocolBox       = null;
	private Label    privProtocolText = null;
	private Combo    privProtocolBox       = null;

	private Group groupSnmp = null;
	private Composite comp = null;
	private String oldVersion = null;
	private Shell shell = null;

	/** Widgetのスパン設定 */
	private static final int SPAN_PRIV_PROTOCOL_BOX = 1;
	private static final int SPAN_PRIV_PROTOCOL_TEXT = 1;
	private static final int SPAN_PRIV_SPACE = 2;
	private static final int SPAN_PRIV_BOX = 3;
	private static final int SPAN_PRIV_TEXT = 1;
	private static final int SPAN_AUTH_SPACE = 2;
	private static final int SPAN_AUTH_PASS_PROTOCOL_BOX = 1;
	private static final int SPAN_AUTH_PASS_PROTOCOL_TEXT = 1;
	private static final int SPAN_AUTH_PASS_BOX = 3;
	private static final int SPAN_AUTU_PASS_TEXT = 1;
	private static final int SPAN_USER_BOX = 3;
	private static final int SPAN_USER_TEXT = 1;
	private static final int SPAN_SECULITY_LEVEL_BOX = 3;
	private static final int SPAN_SECULITY_LEVEL_TEXT = 1;
	private static final int SPAN_VERSION_BOX = 1;
	private static final int SPAN_VERSION_TEXT = 1;
	private static final int SPAN_COMMUNITY_BOX = 3;
	private static final int SPAN_COMMUNITY_TEXT = 1;
	private static final int SPAN_SNMP_GROUP = 10;
	private static final int SPAN_COMMUNITY_SPACE = 2;
	private static final int SPAN_PORT_BOX = 3;
	private static final int SPAN_PORT_TEXT = 1;
	private static final int SPAN_IPADDRESS_COMP = 10;
	private static final int SPAN_IPADDRESS_TEXT = 1;
	private static final int SPAN_IPADDRESS_FROM = 4;
	private static final int SPAN_IPADDRESS_RANGE = 1;
	private static final int SPAN_IPADDRESS_TO = 4;

	public NodeSearchDialog(Shell parent) {
		super(parent);
	}

	/**
	 * ダイアログの初期サイズを返します。
	 *
	 * @return 初期サイズ
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(500, 345);
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のインスタンス
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		shell = this.getShell();
		// タイトル
		shell.setText(Messages
				.getString("dialog.repository.search.nodes"));

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);
		this.comp = parent;

		/*
		 * マネージャ
		 */
		Label labelManager = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "manager", labelManager);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		labelManager.setLayoutData(gridData);
		labelManager.setText(Messages.getString("facility.manager") + " : ");
		this.m_managerComposite = new ManagerListComposite(parent, SWT.NONE, true);
		WidgetTestUtil.setTestId(this, "managerComposite", this.m_managerComposite);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		this.m_managerComposite.setLayoutData(gridData);

		this.m_managerComposite.getComboManagerName().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String managerName = m_managerComposite.getText();
				m_ownerRoleId.createRoleIdList(managerName);
			}
		});

		/*
		 * オーナーロールID
		 */
		Label labelRoleId = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "roleid", labelRoleId);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		labelRoleId.setLayoutData(gridData);
		labelRoleId.setText(Messages.getString("owner.role.id") + " : ");

		this.m_ownerRoleId = new RoleIdListComposite(parent, SWT.NONE, this.m_managerComposite.getText(), true, Mode.OWNER_ROLE);
		WidgetTestUtil.setTestId(this, "roleidlist", m_ownerRoleId);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		m_ownerRoleId.setLayoutData(gridData);

		//IPアドレス
		Composite ipaddressComp = new Composite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "ipaddress", ipaddressComp);
		ipaddressComp.setLayout(new GridLayout(SPAN_IPADDRESS_COMP, false));
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		ipaddressComp.setLayoutData(gridData);

		this.ipAddressText = new Label(ipaddressComp, SWT.NONE);
		WidgetTestUtil.setTestId(this, "ipaddress", ipAddressText);
		this.ipAddressText.setText(Messages.getString("ip.address") + " : ");
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		gridData.horizontalSpan = SPAN_IPADDRESS_TEXT;
		this.ipAddressText.setLayoutData(gridData);

		this.ipAddressBoxFrom = new Text(ipaddressComp, SWT.BORDER | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, "ipaddress", ipAddressBoxFrom);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.minimumWidth = 120;
		gridData.horizontalSpan = SPAN_IPADDRESS_FROM;
		gridData.grabExcessHorizontalSpace = true;
		this.ipAddressBoxFrom.setLayoutData(gridData);
		this.ipAddressBoxFrom.setText(NodeSearchUtil.generateDefaultIp( "192.168.0." ));

		Label range = new Label(ipaddressComp, SWT.NONE);
		WidgetTestUtil.setTestId(this, "‐", range);
		range.setText("―");
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.horizontalSpan = SPAN_IPADDRESS_RANGE;
		gridData.grabExcessHorizontalSpace = false;
		range.setLayoutData(gridData);

		this.ipAddressBoxTo = new Text(ipaddressComp, SWT.BORDER | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, "ipaddress", ipAddressBoxTo);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.minimumWidth = 120;
		gridData.horizontalSpan = SPAN_IPADDRESS_TO;
		gridData.grabExcessHorizontalSpace = true;
		this.ipAddressBoxTo.setLayoutData(gridData);
		this.ipAddressBoxTo.setText(NodeSearchUtil.generateDefaultIp( "192.168.0." ));

		//SNMPグループ
		groupSnmp = new Group(parent, SWT.RIGHT);
		WidgetTestUtil.setTestId(this, null, groupSnmp);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		groupSnmp.setLayoutData(gridData);
		layout = new GridLayout(SPAN_SNMP_GROUP, false);
		groupSnmp.setLayout(layout);
		groupSnmp.setText(Messages.getString("snmp"));

		//ポート名
		this.portText = new Label(groupSnmp, SWT.NONE);
		WidgetTestUtil.setTestId(this, "portnumber", portText);
		this.portText.setText(Messages.getString("port.number") + " : ");
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		gridData.horizontalSpan = SPAN_PORT_TEXT;
		this.portText.setLayoutData(gridData);

		this.portBox = new Text(groupSnmp, SWT.BORDER | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, "port", portBox);
		gridData = new GridData();
		gridData.minimumWidth = 50;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = SPAN_PORT_BOX;
		this.portBox.setLayoutData(gridData);
		this.portBox.setText("161");

		// 改行のため、ダミーのラベルを挿入。
		Label label = new Label(groupSnmp, SWT.NONE);
		WidgetTestUtil.setTestId(this, "dummy", label);
		gridData = new GridData();
		gridData.horizontalSpan = SPAN_SNMP_GROUP - (SPAN_PORT_TEXT + SPAN_PORT_BOX);
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		//コミュニティ名
		this.communityText = new Label(groupSnmp, SWT.NONE);
		WidgetTestUtil.setTestId(this, "communityname", communityText);
		this.communityText.setText(Messages.getString("community.name") + " : ");
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		gridData.horizontalSpan = SPAN_COMMUNITY_TEXT;
		this.communityText.setLayoutData(gridData);

		this.communityBox = new Text(groupSnmp, SWT.BORDER | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, "community", communityBox);
		gridData = new GridData();
		gridData.horizontalSpan = SPAN_COMMUNITY_BOX;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.communityBox.setLayoutData(gridData);
		this.communityBox.setText("public");

		//ダミーのラベルを挿入。
		label = new Label(groupSnmp, SWT.NONE);
		WidgetTestUtil.setTestId(this, "dummy1", label);
		gridData = new GridData();
		gridData.horizontalSpan = SPAN_COMMUNITY_SPACE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		label.setLayoutData(gridData);

		//バージョン
		this.versionText = new Label(groupSnmp, SWT.NONE);
		WidgetTestUtil.setTestId(this, "snmpversion", versionText);
		this.versionText.setText(Messages.getString("snmp.version") + " : ");
		gridData = new GridData();
		gridData.horizontalSpan = SPAN_VERSION_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		this.versionText.setLayoutData(gridData);

		this.versionBox = new Combo(groupSnmp, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "version", versionBox);
		gridData = new GridData();
		gridData.horizontalSpan = SPAN_VERSION_BOX;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.versionBox.setLayoutData(gridData);
		this.versionBox.add("1",0);
		this.versionBox.add("2c",1);
		this.versionBox.add("3",2);
		// デフォルトをv2cとする
		this.versionBox.select(1);
		oldVersion = versionBox.getText();

		this.versionBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String version = versionBox.getText();
				if (oldVersion.equals(version)) {
					return;
				}
				if (version.equals("3")) {
					setVersion3Item();
				} else if (oldVersion.equals("3"))  {
					empty_label1.dispose();
					empty_label2.dispose();
					empty_label3.dispose();
					empty_label4.dispose();
					empty_label5.dispose();
					empty_label6.dispose();
					securityLevelText.dispose();
					securityLevelBox.dispose();
					userText.dispose();
					userBox.dispose();
					authPassText.dispose();
					authPassBox.dispose();
					privPassText.dispose();
					privPassBox.dispose();
					authProtocolText.dispose();
					authProtocolBox.dispose();
					privProtocolText.dispose();
					privProtocolBox.dispose();
					groupSnmp.layout();
					comp.layout();
					shell.setSize(getInitialSize());
				}
				oldVersion = version;
			}
		});
	}

	private void setVersion3Item() {

		//ダミーのラベルを挿入。
		empty_label1 = new Label(groupSnmp, SWT.NONE);
		WidgetTestUtil.setTestId(this, "empty_label1", empty_label1);
		GridData gridData = new GridData();
		gridData.horizontalSpan = SPAN_SNMP_GROUP
				- (SPAN_COMMUNITY_TEXT + SPAN_COMMUNITY_BOX + SPAN_COMMUNITY_SPACE + SPAN_VERSION_TEXT + SPAN_VERSION_BOX);
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		empty_label1.setLayoutData(gridData);

		//セキュリティレベル
		securityLevelText = new Label(groupSnmp, SWT.NONE);
		WidgetTestUtil.setTestId(this, "securitylevel", securityLevelText);
		securityLevelText.setText(Messages.getString("snmp.security.level") + " : ");
		gridData = new GridData();
		gridData.horizontalSpan = SPAN_SECULITY_LEVEL_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		securityLevelText.setLayoutData(gridData);

		securityLevelBox = new Combo(groupSnmp, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "securitylevelbox", securityLevelBox);
		gridData = new GridData();
		gridData.horizontalSpan = SPAN_SECULITY_LEVEL_BOX;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		securityLevelBox.setLayoutData(gridData);
		securityLevelBox.add(SnmpSecurityLevelConstant.NOAUTH_NOPRIV, 0);
		securityLevelBox.add(SnmpSecurityLevelConstant.AUTH_NOPRIV, 1);
		securityLevelBox.add(SnmpSecurityLevelConstant.AUTH_PRIV, 2);

		// デフォルトセット
		securityLevelBox.select(0);

		//ダミーのラベルを挿入。
		//改行のため、ダミーのラベルを挿入。
		empty_label2 = new Label(groupSnmp, SWT.NONE);
		WidgetTestUtil.setTestId(this, "emptylabelsecurity", empty_label2);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = SPAN_SNMP_GROUP - (SPAN_SECULITY_LEVEL_TEXT + SPAN_SECULITY_LEVEL_BOX);
		empty_label2.setLayoutData(gridData);

		//ユーザー名
		userText = new Label(groupSnmp, SWT.NONE);
		WidgetTestUtil.setTestId(this, "username", userText);
		userText.setText(Messages.getString("user.name") + " : ");
		gridData = new GridData();
		gridData.horizontalSpan = SPAN_USER_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		userText.setLayoutData(gridData);

		userBox = new Text(groupSnmp, SWT.BORDER | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, "userbox", userBox);
		gridData = new GridData();
		gridData.horizontalSpan = SPAN_USER_BOX;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		userBox.setLayoutData(gridData);
		userBox.setText("root");

		//改行のため、ダミーのラベルを挿入。
		empty_label3 = new Label(groupSnmp, SWT.NONE);
		WidgetTestUtil.setTestId(this, "empty_label3", empty_label3);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = SPAN_SNMP_GROUP - (SPAN_USER_TEXT + SPAN_USER_BOX);
		empty_label3.setLayoutData(gridData);

		//認証パスワード
		authPassText = new Label(groupSnmp, SWT.NONE);
		WidgetTestUtil.setTestId(this, "authpassword", authPassText);
		authPassText.setText(Messages.getString("snmp.auth.password") + " : ");
		gridData = new GridData();
		gridData.horizontalSpan = SPAN_AUTU_PASS_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		authPassText.setLayoutData(gridData);

		authPassBox = new Text(groupSnmp, SWT.BORDER | SWT.SINGLE);
		authPassBox.setEchoChar('*');
		WidgetTestUtil.setTestId(this, "authpassbox", authPassBox);
		gridData = new GridData();
		gridData.horizontalSpan = SPAN_AUTH_PASS_BOX;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		authPassBox.setLayoutData(gridData);
		authPassBox.setText("");

		//ダミーのラベルを挿入。
		empty_label4 = new Label(groupSnmp, SWT.NONE);
		WidgetTestUtil.setTestId(this, "empty_label4", empty_label4);
		gridData = new GridData();
		gridData.horizontalSpan = SPAN_AUTH_SPACE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		empty_label4.setLayoutData(gridData);

		//認証プロトコル
		authProtocolText = new Label(groupSnmp, SWT.NONE);
		authProtocolText.setText(Messages.getString("snmp.auth.protocol") + " : ");
		gridData = new GridData();
		gridData.horizontalSpan = SPAN_AUTH_PASS_PROTOCOL_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		authProtocolText.setLayoutData(gridData);

		authProtocolBox = new Combo(groupSnmp, SWT.DROP_DOWN | SWT.READ_ONLY);
		authProtocolBox.setSize(10, 30);
		gridData = new GridData();
		gridData.horizontalSpan = SPAN_AUTH_PASS_PROTOCOL_BOX;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		authProtocolBox.setLayoutData(gridData);
		WidgetTestUtil.setTestId(this, "authprotocolbox", authProtocolBox);
		authProtocolBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		authProtocolBox.add(SnmpProtocolConstant.MD5, 0);
		authProtocolBox.add(SnmpProtocolConstant.SHA, 1);

		// デフォルトセット
		authProtocolBox.select(0);

		//ダミーのラベルを挿入。
		empty_label5 = new Label(groupSnmp, SWT.NONE);
		WidgetTestUtil.setTestId(this, "empty_label5", empty_label5);
		gridData = new GridData();
		gridData.horizontalSpan = SPAN_SNMP_GROUP
				- (SPAN_AUTU_PASS_TEXT + SPAN_AUTH_PASS_BOX
						+ SPAN_AUTH_SPACE + SPAN_AUTH_PASS_PROTOCOL_TEXT + SPAN_AUTH_PASS_PROTOCOL_BOX);
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		empty_label5.setLayoutData(gridData);

		//暗号化パスワード
		privPassText = new Label(groupSnmp, SWT.NONE);
		privPassText.setText(Messages.getString("snmp.priv.password") + " : ");
		gridData = new GridData();
		gridData.horizontalSpan = SPAN_PRIV_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		privPassText.setLayoutData(gridData);

		privPassBox = new Text(groupSnmp, SWT.BORDER | SWT.SINGLE);
		privPassBox.setEchoChar('*');
		WidgetTestUtil.setTestId(this, "privpassbox", privPassBox);
		gridData = new GridData();
		gridData.horizontalSpan = SPAN_PRIV_BOX;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		privPassBox.setLayoutData(gridData);
		privPassBox.setText("");

		//ダミーのラベルを挿入。
		empty_label6 = new Label(groupSnmp, SWT.NONE);
		WidgetTestUtil.setTestId(this, "empty_label6", empty_label6);
		gridData = new GridData();
		gridData.horizontalSpan = SPAN_PRIV_SPACE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		empty_label6.setLayoutData(gridData);

		//暗号化プロトコル
		privProtocolText = new Label(groupSnmp, SWT.NONE);
		privProtocolText.setText(Messages.getString("snmp.priv.protocol") + " : ");
		gridData = new GridData();
		gridData.horizontalSpan = SPAN_PRIV_PROTOCOL_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		privProtocolText.setLayoutData(gridData);

		privProtocolBox = new Combo(groupSnmp, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "privprotocolbox", privProtocolBox);
		gridData = new GridData();
		gridData.horizontalSpan = SPAN_PRIV_PROTOCOL_BOX;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		privProtocolBox.setLayoutData(gridData);
		privProtocolBox.add(SnmpProtocolConstant.DES, 0);
		privProtocolBox.add(SnmpProtocolConstant.AES, 1);
		// デフォルトセット
		privProtocolBox.select(0);

		groupSnmp.layout();
		comp.layout();
		shell.setSize(new Point(500, 465));
	}

	@Override
	protected String getOkButtonText() {
		return Messages.getString("run");
	}

	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}

	@Override
	protected void okPressed() {
		ValidateResult result = this.validate();

		if (result == null || result.isValid()) {
			if(this.action()){
				try {
					String ownerRoleId = m_ownerRoleId.getComboRoleId().getText();
					String ipAddressFrom = ipAddressBoxFrom == null ? null : ipAddressBoxFrom.getText();
					String ipAddressTo = ipAddressBoxTo == null ? null : ipAddressBoxTo.getText();
					int port = Integer.parseInt(portBox.getText());
					String community = communityBox == null ? null : communityBox.getText();
					int version = versionBox == null ? -2 : SnmpVersionConstant.stringToType(versionBox.getText());
					String user = userBox == null || userBox.isDisposed() ? null : userBox.getText();
					String securityLevel = securityLevelBox == null || securityLevelBox.isDisposed() ? null : securityLevelBox.getText();
					String authPass = authPassBox == null || authPassBox.isDisposed() ? null : authPassBox.getText();
					String privPass = privPassBox == null || privPassBox.isDisposed() ? null : privPassBox.getText();
					String authProtocol = authProtocolBox == null || authProtocolBox.isDisposed() ? null : authProtocolBox.getText();
					String privProtocol = privProtocolBox == null || privProtocolBox.isDisposed() ? null : privProtocolBox.getText();

					String managerName = m_managerComposite.getText();
					RepositoryEndpointWrapper wrapper = RepositoryEndpointWrapper.getWrapper(managerName);
					List<NodeInfoDeviceSearch> list = wrapper.searchNodesBySNMP(
							ownerRoleId, ipAddressFrom, ipAddressTo, port, community, version, null, user,
							securityLevel, authPass, privPass, authProtocol, privProtocol);

					StringBuffer buf = new StringBuffer();
					Object[] arg = {list.size()};
					buf.append(Messages.getString("message.repository.nodesearch.8", arg));
					buf.append(System.lineSeparator());

					NodeSearchResultDialog dialog = new NodeSearchResultDialog(shell, list, true);
					dialog.open();

					boolean existsError = false;
					for(NodeInfoDeviceSearch info : list) {
						if (info.getErrorMessage() != null) {
							existsError = true;
							break;
						}
					}
					if(existsError) {
						NodeSearchResultDialog errDialog = new NodeSearchResultDialog(shell, list, false);
						errDialog.open();
					}
				} catch (NumberFormatException e) {
					MessageDialog.openError(shell,
							Messages.getString("failed"),
							Messages.getString("message.hinemos.failure.unexpected") + HinemosMessage.replace(e.getMessage()));
					return;
				} catch (HinemosUnknown_Exception e) {
					MessageDialog.openInformation(shell,
							Messages.getString("message"),
							HinemosMessage.replace(e.getMessage()));
					return;
				} catch (InvalidRole_Exception e) {
					MessageDialog.openError(shell,
							Messages.getString("failed"),
							Messages.getString("message.accesscontrol.16"));
				} catch (InvalidUserPass_Exception e) {
					MessageDialog.openError(shell,
							Messages.getString("failed"),
							Messages.getString("message.accesscontrol.16"));
				} catch (SnmpResponseError_Exception e) {
					MessageDialog.openError(shell,
							Messages.getString("failed"),
							Messages.getString("message.snmp.12"));
					return;
				} catch (WebServiceException e) {
					MessageDialog.openError(shell,
							Messages.getString("failed"),
							Messages.getString("message.process.8") + HinemosMessage.replace(e.getMessage()));
				} catch (FacilityDuplicate_Exception e) {
					Object[] arg = { e.getFaultInfo().getFacilityId() };
					MessageDialog.openInformation(shell,
							Messages.getString("message"),
							Messages.getString("message.repository.26", arg) + System.lineSeparator() + HinemosMessage.replace(e.getMessage()));
				} catch (InvalidSetting_Exception e) {
					MessageDialog.openError(shell,
							Messages.getString("failed"),
							Messages.getString("message.hinemos.failure.unexpected") + HinemosMessage.replace(e.getMessage()));
				}
				super.okPressed();
			}
		} else {
			this.displayError(result);
		}
	}

	@Override
	protected ValidateResult validate() {

		ValidateResult result = new ValidateResult();
		result.initialize();
		try {
			Integer.parseInt(this.portBox.getText());
		} catch (NumberFormatException e1){
			// port番号が不正な場合
			result.setValid(false);
			result.setID(Messages.getString("warning"));
			result.setMessage(Messages.getString("message.repository.nodesearch.2"));

			return result;
		}

		return result;
	}
}
