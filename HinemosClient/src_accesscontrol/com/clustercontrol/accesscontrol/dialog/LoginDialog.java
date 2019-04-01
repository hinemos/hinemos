/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.dialog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.action.StringVerifyListener;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.EndpointUnit;
import com.clustercontrol.util.LoginConstant;
import com.clustercontrol.util.LoginManager;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 接続[ログイン]ダイアログクラスです。
 *
 * @version 5.0.0
 * @since 2.0.0
 */
public class LoginDialog extends Dialog {
	// ログ
	private static Log m_log = LogFactory.getLog( LoginDialog.class );

	/** connection inputs */
	private List<LoginInput> inputList = new ArrayList<>();
	private List<LoginAccount> validatedLoginList = new ArrayList<>();

	private boolean useSameID = false;
	private boolean disableLoginButton = false;
	private int counter = 1;
	private Map<String, String> paramMap;
	private static int maxConnectManager;
	
	static {
		int max_manager = 8;
		try {
			max_manager = Integer.parseInt(System.getProperty("maximum.login.manager", "8"));
		} catch (NumberFormatException e) {
			m_log.info("System environment value \"maximum.login.manager\" is not correct.");
		} finally {
			maxConnectManager = max_manager;
			m_log.info("maxConnectManager = " + maxConnectManager);
		}
	}

	/**
	* コンストラクタ
	*
	* <注意!!!> LoginManager.java以外から呼ばないこと！！！
	*
	* @param parent 親シェル
	*/
	public LoginDialog( Shell parent, Map<String, String> map){
		super(parent);
		paramMap = map;
		
		if (map.containsKey(LoginManager.KEY_BASIC_AUTH) && map.get(LoginManager.KEY_BASIC_AUTH).equals("true")) {
			// Basic認証情報を元にログイン
			String userId = paramMap.get("user");
			String password = paramMap.get("password");
			
			String[] urls = new String[]{};
			String[] managerNames = new String[]{};
			
			if (paramMap.containsKey(LoginManager.KEY_URL_LOGIN_URL)) {
				urls = paramMap.get(LoginManager.KEY_URL_LOGIN_URL).split(";");
			}
			if (paramMap.containsKey(LoginManager.KEY_URL_MANAGER_NAME)) {
				managerNames = paramMap.get(LoginManager.KEY_URL_MANAGER_NAME).split(";");
			}
			
			if (urls.length == 0) {
				// URLが指定されていない
				MessageDialog.openError(null, Messages.getString("message"), Messages.getString("message.hinemos.9"));
			}
			
			for (int cnt = 0; cnt < maxConnectManager; ++cnt) {
				if (urls.length > cnt) {
					// URLが指定されている場合
					String url = urls[cnt];
					String managerName = null;
					
					if (managerNames.length > cnt) {
						managerName = managerNames[cnt];
					}
					
					
					//inputList.add(new LoginInput(userId, password, url, managerName));
					validatedLoginList.add(new LoginInput(userId, password, url, managerName).presetAccount);
					
				} else {
					// URL指定がされてなかったら終了
					break;
				}
			}
		}
	}

	@Override
	protected void configureShell( Shell newShell ){
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.accesscontrol.login"));
	}

	@Override
	public void create() {
		super.create();

		//画面中央に配置
		Shell shell = getShell();
		Display display = shell.getDisplay();
		shell.setLocation(
				(display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);
	}

	@Override
	protected void createButtonsForButtonBar( Composite parent ){
		// Customize createButtonsForButtonBar(parent) here
		Button button;

		button = createButton( parent, IDialogConstants.RETRY_ID, Messages.getString("button.addlogin"), false );
		WidgetTestUtil.setTestId(this, "addlogin", button);
		button.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected( SelectionEvent e ){
				if (inputList.size() >= maxConnectManager ) {
					// 最大マネージャ接続数を超えている
					String msg = Messages.getString("message.accesscontrol.66", new String[] {String.valueOf(maxConnectManager)});
					m_log.warn(msg);
					MessageDialog.openError(null, Messages.getString("message"), msg);
					return;
				}
				
				// Save inputting account info
				for( LoginInput input: inputList ){
					input.saveInfo();
				}

				// Update counter before add a new one and copy last manager input
				reInitializeCounter();

				LoginAccount lastInputAccount = inputList.get( inputList.size() - 1 ).getAccount();
				inputList.add( new LoginInput( lastInputAccount.getUserId(), "", lastInputAccount.getUrl(), null ) );

				int buttonId = ((Integer) e.widget.getData()).intValue();
				setReturnCode(buttonId);
				close();
			}
		});

		// ＯＫボタンのテキスト変更
		button= createButton(parent, IDialogConstants.OK_ID, Messages.getString("login"), true);
		WidgetTestUtil.setTestId(this, "ok", button);
		if( disableLoginButton ){
			button.setEnabled(false);
		}

		// キャンセルボタンのテキスト変更. Don't need to addSelectionListener() because CANCEL button will be set by default.
		button= createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("cancel"), false);
		WidgetTestUtil.setTestId(this, "cancel", button);
	}

	class LoginInput{
		/** ユーザID用テキスト */
		private Text uidText = null;
		/** パスワード用テキスト */
		private Text passwordText = null;
		/**ログイン先用テキスト**/
		private Combo urlCombo = null;
		/** パスワード用テキスト */
		private Text managerNameText = null;

		private boolean isNew = true;

		private LoginAccount presetAccount;

		private LoginInput(){
			// ユーザIDを環境変数/デフォルトから取得します
			String userId = System.getenv(LoginManager.ENV_HINEMOS_MANAGER_USER);
			if( null == userId || 0 == userId.length() ){
				userId = LoginManager.VALUE_UID;
			}

			// パスワードを環境変数/デフォルトから取得する
			String password = System.getenv(LoginManager.ENV_HINEMOS_MANAGER_PASS);
			if( null == password || 0 == password.length() ){
				password = "";
			}

			// 接続先URLを環境変数/デフォルトから取得する
			String url = System.getenv(LoginManager.ENV_HINEMOS_MANAGER_URL);
			if( null == url || 0 == url.length() ){
				url = LoginManager.VALUE_URL;
			}
			String managerName = Messages.getString("facility.manager") + (counter++);
			presetAccount = new LoginAccount( userId, password, url, managerName );
			isNew = true;
		}

		private LoginInput( String userId, String password, String url, String managerName, int status ){
			presetAccount = new LoginAccount( userId, password, url, managerName, status );
			isNew = false;
		}

		private LoginInput( String userId, String password, String url, String managerName ){
			if( null != managerName ){
				updateCounter(managerName);
			}else{
				managerName = Messages.getString("facility.manager") + (counter++);
			}
			presetAccount = new LoginAccount( userId, password, url, managerName );
			isNew = true;
		}

		private void saveInfo( ){
			if( null != uidText && uidText.getEnabled() ){
				presetAccount.setUserId( uidText.getText() );
			}
			if( null != passwordText && passwordText.getEnabled()  ){
				presetAccount.setPassword( passwordText.getText() );
			}
			if( null != urlCombo ){
				presetAccount.setUrl( urlCombo.getText() );
			}
			if( null != managerNameText ){
				presetAccount.setManagerName( managerNameText.getText() );
			}
		}

		private void render( Composite parent, boolean onFirst ){
			if( !onFirst ){
				Label separatorLabel = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
				separatorLabel.setLayoutData(new GridData(GridData.FILL, GridData.VERTICAL_ALIGN_BEGINNING, false, false, 4, 1));
			}

			GridData layoutData;

			Label uidLabel = new Label(parent, SWT.NONE);
			WidgetTestUtil.setTestId(this, "uid", uidLabel);
			uidLabel.setText(Messages.getString("user.id") + " : ");
			uidText = new Text(parent, SWT.BORDER | SWT.FILL);
			WidgetTestUtil.setTestId(this, "uid", uidText);
			layoutData = new GridData(GridData.FILL_HORIZONTAL);
			layoutData.minimumWidth = SizeConstant.SIZE_TEXT_USERID_WIDTH;
			uidText.setLayoutData(layoutData);
			uidText.addVerifyListener(new StringVerifyListener(DataRangeConstant.VARCHAR_64));

			Label passwordLabel = new Label(parent, SWT.NONE);
			WidgetTestUtil.setTestId(this, "password", passwordLabel);
			passwordLabel.setText(Messages.getString("password") + " : ");
			passwordText = new Text(parent, SWT.BORDER | SWT.PASSWORD);
			WidgetTestUtil.setTestId(this, "password", passwordText);
			layoutData = new GridData(GridData.FILL_HORIZONTAL);
			layoutData.minimumWidth = SizeConstant.SIZE_TEXT_USERID_WIDTH;
			passwordText.setLayoutData(layoutData);
			passwordText.addVerifyListener(new StringVerifyListener(DataRangeConstant.VARCHAR_64));

			Label urlLabel = new Label(parent, SWT.NONE);
			WidgetTestUtil.setTestId(this, "url", urlLabel);
			urlLabel.setText(Messages.getString("connection.url") + " : ");
			urlCombo = new Combo(parent, SWT.DROP_DOWN|SWT.BORDER);
			WidgetTestUtil.setTestId(this, "url", urlCombo);
			layoutData = new GridData(GridData.FILL_HORIZONTAL);
			layoutData.horizontalSpan = 3;
			layoutData.minimumWidth = SizeConstant.SIZE_TEXT_URL_WIDTH;
			urlCombo.setLayoutData(layoutData);
			urlCombo.addVerifyListener(new StringVerifyListener(DataRangeConstant.VARCHAR_1024));
			loadUrls();

			Label managerNameLabel = new Label(parent, SWT.NONE);
			WidgetTestUtil.setTestId(this, "managername", managerNameLabel);
			managerNameLabel.setText(Messages.getString("facility.managername") + " : ");
			managerNameText = new Text(parent, SWT.BORDER);
			WidgetTestUtil.setTestId(this, "managername", managerNameText);
			managerNameText.setText( presetAccount.getManagerName() );
			managerNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			managerNameText.addVerifyListener(new StringVerifyListener(DataRangeConstant.VARCHAR_64));

			Label statusLabel = new Label(parent, SWT.NONE | SWT.CENTER);
			WidgetTestUtil.setTestId(this, "status", statusLabel);
			layoutData = new GridData(GridData.FILL, GridData.CENTER, true, true);
			layoutData.horizontalSpan = 1;
			statusLabel.setLayoutData(layoutData);
			if( presetAccount.getStatus() == LoginAccount.STATUS_CONNECTED ){
				statusLabel.setText( Messages.getString("manager.status.connected") );
				statusLabel.setBackground( getShell().getDisplay().getSystemColor( SWT.COLOR_GREEN ) );
			}else{
				statusLabel.setText( Messages.getString("manager.status.unconnected") );
				statusLabel.setBackground( getShell().getDisplay().getSystemColor( SWT.COLOR_GRAY ) );
			}

			Button logoutBtn = new Button(parent, SWT.NONE);
			WidgetTestUtil.setTestId(this, "delete", logoutBtn);
			layoutData = new GridData(GridData.HORIZONTAL_ALIGN_END);
			layoutData.horizontalSpan = 1;
			logoutBtn.setLayoutData(layoutData);
			if( presetAccount.getStatus() == LoginAccount.STATUS_CONNECTED ){
				// Logout and remove from list
				logoutBtn.setText(Messages.getString("logout"));
				logoutBtn.addSelectionListener(new SelectionAdapter(){
					@Override
					public void widgetSelected( SelectionEvent e ){
						LoginManager.disconnect( presetAccount.getManagerName() );
						presetAccount.setStatus( LoginAccount.STATUS_UNCONNECTED );

						// Save inputting account info
						for( LoginInput input: inputList ){
							input.saveInfo();
						}

						setReturnCode(IDialogConstants.RETRY_ID);
						close();
					}
				});
			}else{
				// Just remove from list
				logoutBtn.setText(Messages.getString("delete"));
				logoutBtn.addSelectionListener(new SelectionAdapter(){
					@Override
					public void widgetSelected( SelectionEvent e ){
						// Remove if existed in EndpointManager
						if( !isNew ){
							EndpointManager.delete( presetAccount.getManagerName() );
						}

						// If useSameID is true and the first input is deleted, pass its info to the second input
						if( useSameID && 1 < inputList.size() ){
							for( int i=inputList.indexOf( LoginInput.this )+1; i<inputList.size(); i++ ){
								LoginInput input = inputList.get(i);
								if( input.getAccount().getStatus() == LoginAccount.STATUS_UNCONNECTED ){
									// Disable from the second one
									input.presetAccount.setUserId( uidText.getText() );
									input.presetAccount.setPassword( passwordText.getText() );
									break;
								}
							}
						}

						// Remove from list
						inputList.remove( LoginInput.this );
							
						// If nothing left
						if( 0 == inputList.size() ){
							counter = 1;
							inputList.add( new LoginInput() );
						}

						// Save inputting account info
						for( LoginInput input: inputList ){
							input.saveInfo();
						}

						setReturnCode(IDialogConstants.RETRY_ID);
						close();
					}
				});
			}

			// Set preset value
			uidText.setText( presetAccount.getUserId() );
			passwordText.setText( presetAccount.getPassword() );
			urlCombo.setText( presetAccount.getUrl() );
			if( !isNew ){
				managerNameText.setEnabled( false );
				if( presetAccount.getStatus() != LoginAccount.STATUS_UNCONNECTED ){
					urlCombo.setEnabled( false );
					uidText.setEnabled( false );
					passwordText.setEnabled( false );
				}
			}
		}

		private void setFocus(){
			//通常入力する必要がある項目はパスワードのみであるため、パスワードにフォーカス
			passwordText.setFocus();
		}

		private LoginAccount getAccount(){
			if (uidText == null) {
				
			}
			return new LoginAccount(
					uidText.getText().trim(),
					passwordText.getText().trim(),
					urlCombo.getText().trim(),
					managerNameText.getText().trim(),
					presetAccount.getStatus()
				);
		}

		private void setSkipped( boolean skip ){
			uidText.setEnabled( !skip );
			passwordText.setEnabled( !skip );
		}

		private void loadUrls(){
			for( String url : getPrefURLs() ){
				urlCombo.add(url);
			}
		}
	}

	/**
	* ダイアログエリアを生成します。
	* 
	* @param parent 親コンポジット
	*/
	@Override
	protected Control createDialogArea( Composite parent ){
		// Configure scrolled composite
		ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, true));
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);

		scrolledComposite.setLayout(new GridLayout());
		// Add content to scrolled composite
		Composite scrolledContent = new Composite(scrolledComposite, SWT.NONE);
		scrolledContent.setLayout(new GridLayout(4, false));

		if( inputList.isEmpty() ){
			if( 0 < EndpointManager.sizeOfAll() ){
				// 1. Load manager from EndpointManager
				List<EndpointUnit> endpointUnitList = EndpointManager.getAllManagerList();
				int connectedLen = endpointUnitList.size();
				for( int i=0; i<connectedLen; i++ ){
					EndpointUnit endpointUnit = endpointUnitList.get(i);
					inputList.add( new LoginInput(
							endpointUnit.getUserId(), endpointUnit.getPassword(), endpointUnit.getUrlListStr(),
							endpointUnit.getManagerName(), endpointUnit.getStatus() ) );
				}
			} else if (paramMap.size() > 0) {
				// 2. Load manager from GET query
				String[] urls = new String[]{};
				String[] users = new String[]{};
				String[] managerNames = new String[]{};
				
				if (paramMap.containsKey(LoginManager.KEY_URL_LOGIN_URL)) {
					urls = paramMap.get(LoginManager.KEY_URL_LOGIN_URL).split(";");
				}
				if (paramMap.containsKey(LoginManager.KEY_URL_UID)) {
					users = paramMap.get(LoginManager.KEY_URL_UID).split(";");
				}
				if (paramMap.containsKey(LoginManager.KEY_URL_MANAGER_NAME)) {
					managerNames = paramMap.get(LoginManager.KEY_URL_MANAGER_NAME).split(";");
				}
				
				for (int cnt = 0; cnt < maxConnectManager; ++cnt) {
					if (urls.length > cnt) {
						// URLが指定されている場合
						String url = urls[cnt];
						String userId = LoginManager.VALUE_UID;
						String managerName = null;
						
						if (users.length > cnt) {
							userId = users[cnt];
						}
						
						if (managerNames.length > cnt) {
							managerName = managerNames[cnt];
						}
						
						
						inputList.add(new LoginInput(userId, "", url, managerName));
					} else {
						// URL指定がされてなかったら終了
						break;
					}
				}
			}else{
				// 3. Load record(s) from history
				IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();
				int historyNum = store.getInt(LoginConstant.KEY_LOGIN_STATUS_NUM);
				if (historyNum > maxConnectManager) {
					// 最大マネージャ接続数だけ読み込む
					historyNum = maxConnectManager;
				}
				if( 0 < historyNum ){
					// 1. Try to reload from history
					for( int i=0; i<historyNum; i++ ){
						String userId = store.getString(LoginConstant.KEY_LOGIN_STATUS_UID + "_" + i);
						String password = "";
						String url = store.getString(LoginConstant.KEY_LOGIN_STATUS_URL + "_" + i);
						String managerName = store.getString(LoginConstant.KEY_LOGIN_STATUS_MANAGERNAME + "_" + i);

						// Use ENV setting at first when existed
						if( 0 == i ){
							// 環境変数から取得
							String envUserId = System.getenv(LoginManager.ENV_HINEMOS_MANAGER_USER);
							if( null != envUserId && 0 != envUserId.length() ){
								userId = envUserId;
							}
							String envPassword = System.getenv(LoginManager.ENV_HINEMOS_MANAGER_PASS);
							if( null != envPassword && 0 != envPassword.length() ){
								password = envPassword;
							}
							String envUrl = System.getenv(LoginManager.ENV_HINEMOS_MANAGER_URL);
							if( null != envUrl && 0 != envUrl.length() ){
								url = envUrl;
							}
						}
						inputList.add( new LoginInput(userId, password, url, managerName) );
					}
				}
			}
		}

		int unconnected = 0;
		if( inputList.isEmpty() ){
			// Create a default blank one
			LoginInput loginInput = new LoginInput();
			loginInput.render( scrolledContent, true );
			// 通常入力する必要がある項目はパスワードのみであるため、パスワードにフォーカス.
			loginInput.setFocus();
			inputList.add(loginInput);
			unconnected++;
		}else{
			// Show inputList
			int newLen = inputList.size();
			for( int i=0; i<newLen; i++ ){
				LoginInput input = inputList.get(i);
				input.render( scrolledContent, 0==i );

				if( input.getAccount().getStatus() == LoginAccount.STATUS_UNCONNECTED ){
					unconnected++;
				}
				if( newLen - 1 == i ){
					//通常入力する必要がある項目はパスワードのみであるため、パスワードにフォーカス. Always focus on the last one
					input.setFocus();
				}
			}
			disableLoginButton = unconnected == 0;
		}

		Label separatorLabel = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		separatorLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		m_log.debug("unconnected=" + unconnected + ", inputList.size=" + inputList.size());
		if( 1 < inputList.size() ){
			Button useSameIDChkBox = new Button(parent, SWT.CHECK);
			WidgetTestUtil.setTestId( this, "usesameid", useSameIDChkBox );
			useSameIDChkBox.setText( Messages.getString("checkbox.usesameaccount") );

			if( useSameID ){
				autoIDInput( useSameID );
			}
			useSameIDChkBox.setSelection( useSameID );
			useSameIDChkBox.addSelectionListener( new SelectionAdapter(){
				@Override
				public void widgetSelected(SelectionEvent e ){
					autoIDInput( ((Button)e.widget).getSelection() );
				}
			});
		}

		scrolledComposite.setContent(scrolledContent);
		scrolledComposite.setMinSize(scrolledContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		return scrolledComposite;
	}

	private void reInitializeCounter(){
		// Reset
		counter = 1;

		// Check new inputs
		for( LoginInput input: inputList ){
			updateCounter(input.managerNameText.getText());
		}
	}

	private void updateCounter( String managerName ){
		String pattern = "^" + Pattern.quote(Messages.getString("facility.manager"));
		if( managerName.matches( pattern + "\\d+$" ) ){
			try{
				int found = Integer.parseInt(managerName.replaceFirst(pattern, ""));
				if( found >= counter ){
					counter = found + 1;
				}
			}catch(NumberFormatException e){}
		}
	}

	/**
	 * Auto input ID and password fields
	 */
	private void autoIDInput( boolean useSameID ){
		if( useSameID ){
			// Set dummy ID and password for display
			boolean firstFlag = true;
			for( LoginInput input : inputList ){
				if (!firstFlag) {
					input.setSkipped(true);
				}
				firstFlag = false;
			}
		}else{
			for( LoginInput input : inputList ){
				input.setSkipped(false);
			}
		}
		this.useSameID = useSameID;
	}

	/**
	 * 接続先URLの履歴をプレファレンスから取得します。
	 */
	private List<String> getPrefURLs() {
		ArrayList<String> ret = new ArrayList<String>();
		//リソースストアから接続先URLの履歴数を取得
		IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();
		int numOfUrlHistory = store.getInt(LoginManager.KEY_URL_NUM);
	
		for(int i=numOfUrlHistory-1; i>=0; i--){
			String url = store.getString(LoginManager.KEY_URL + "_" + i);
			if(!url.equals("")){
				ret.add(url);
			}
		}
		return ret;
	}

	/**
	* ＯＫボタンが押された場合に呼ばれるメソッドで、入力値チェックを実施します。
	* <p>
	* 
	* エラーの場合、ダイアログを閉じずにエラー内容を通知します。
	*/
	@Override
	protected void okPressed() {
		ValidateResult result = this.validate();

		if (result == null || result.isValid()) {
			super.okPressed();
		} else {
			validatedLoginList.clear();
			this.displayError(result);
		}
	}

	/**
	* ダイアログの入力値チェックを行います。
	* 
	* 必要に応じて、入力値チェックを実装して下さい。
	* 
	* @return ValidateResultオブジェクト
	*/
	protected ValidateResult validate() {
		ValidateResult result = null;

		// Ignore useSameID if only one input
		if( useSameID && inputList.size() <= 1 ){
			useSameID = false;
		}

		Set<String> managerNameSet = new HashSet<>();
		String commonID = null;
		String commonPassword = null;
		// User same ID and password for all logins
		if( useSameID && 1 < inputList.size() ){
			LoginAccount account = inputList.get( 0 ).getAccount();
			commonID = account.getUserId();
			commonPassword = account.getPassword();
		}

		for( LoginInput input : inputList ){
			LoginAccount account = input.getAccount();

			// Skipped connected
			if( LoginAccount.STATUS_CONNECTED == account.getStatus() ){
				continue;
			}

			String userid;
			String password;
			if( useSameID ){
				userid = commonID;
				password = commonPassword;
			}else{
				userid = account.getUserId();
				password = account.getPassword();
			}
			String url = account.getUrl();
			String managerName = account.getManagerName();

			// マネージャ名 check
			if( managerName.isEmpty() ){
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.accesscontrol.64"));
				return result;
			}

			// 重複 check
			if( managerNameSet.contains( managerName ) ){
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.accesscontrol.dupicatedmanagername"));
				return result;
			}else{
				managerNameSet.add( managerName );
			}

			// ユーザID check
			if( userid.isEmpty() ){
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.accesscontrol.1"));
				return result;
			}
			// パスワード check
			if( password.isEmpty() ){
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.accesscontrol.2"));
				return result;
			}
			// 接続先URL check
			if( url.isEmpty() ){
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.hinemos.9"));
				return result;
			}
			validatedLoginList.add(new LoginAccount(userid, password, url, managerName));
		}

		return null;
	}

	/**
	* エラー内容を通知します。
	* 警告メッセージボックスにて、クライアントに通知します。
	* 
	* @param result
	*            ValidateResultオブジェクト
	*/
	private void displayError(ValidateResult result ){
		MessageDialog.openWarning( null, result.getID(), result.getMessage() );
	}

	/**
	* Allow resize in case of long list
	*/
	@Override
	protected boolean isResizable(){
		return true;
	}

	/**
	* Get login list
	*/
	public List<LoginAccount> getLoginList() {
		return validatedLoginList;
	}

}
