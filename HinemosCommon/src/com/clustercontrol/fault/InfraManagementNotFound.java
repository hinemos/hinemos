package com.clustercontrol.fault;

/**
 * 環境構築機能情報が存在しない場合に利用するException
 * @version 5.0.0
 * @since 5.0.0
 */
public class InfraManagementNotFound extends HinemosException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4547836044551428061L;
	
	private String managementId;
	
	/**
	 * InfraManagementInfoNotFound コンストラクタ
	 */
	public InfraManagementNotFound(String managementId) {
		super();
		setManagementId(managementId);
	}

	/**
	 * InfraManagementInfoNotFound コンストラクタ
	 * @param messages
	 */
	public InfraManagementNotFound(String managementId, String messages) {
		super(messages);
		setManagementId(managementId);
	}

	/**
	 * InfraManagementInfoNotFound コンストラクタ
	 * @param e
	 */
	public InfraManagementNotFound(String managementId, Throwable e) {
		super(e);
		setManagementId(managementId);
	}

	/**
	 * InfraManagementInfoNotFound コンストラクタ
	 * @param messages
	 * @param e
	 */
	public InfraManagementNotFound(String managementId, String messages, Throwable e) {
		super(messages, e);
		setManagementId(managementId);
	}

	public String getManagementId() {
		return managementId;
	}

	public void setManagementId(String managementId) {
		this.managementId = managementId;
	}
}
