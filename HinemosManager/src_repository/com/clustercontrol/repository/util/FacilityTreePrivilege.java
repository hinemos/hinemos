package com.clustercontrol.repository.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;

/**
 * スコープのオブジェクト権限情報をツリー構造化するためのクラス<BR>
 * 
 */
public class FacilityTreePrivilege implements Serializable, Cloneable {

	private static final long serialVersionUID = 6201836014283569384L;

	/** 親 */
	private FacilityTreePrivilege parent = null;

	/** オブジェクト権限情報オブジェクト（READ, WRITE, EXEC）  true:権限あり */
	private Map<ObjectPrivilegeMode, Boolean> privileges = new HashMap<ObjectPrivilegeMode, Boolean>() {
		private static final long serialVersionUID = 3796378834769263419L;

		{
			for (ObjectPrivilegeMode mode : PrivilegeConstant.objectPrivilegeModes) {
				super.put(mode, false);
			}
		}
	};

	/** ファシリティID */
	private String facilityId = null;

	/** ファシリティ名 */
	private String facilityName = null;

	/** ファシリティタイプ */
	private Integer facilityType = 0;

	/** 子の格納リスト */
	private List<FacilityTreePrivilege> children = null;

	// ----- コンストラクタ ----- //
	public FacilityTreePrivilege() {}

	/**
	 * 引数の情報を保持したインスタンスを取得します。<BR>
	 * 
	 * @param parent
	 *            親のオブジェクト
	 * @param data
	 *            スコープ情報オブジェクト
	 */
	public FacilityTreePrivilege(FacilityTreePrivilege parent, String facilityId, String facilityName, Integer facilityType, Map<ObjectPrivilegeMode, Boolean> privileges) {

		this.setParent(parent);
		this.setFacilityId(facilityId);
		this.setFacilityName(facilityName);
		this.setFacilityType(facilityType);
		if (privileges != null) {
			this.setPrivileges(privileges);
		}

		if (parent != null) {
			parent.addChildren(this);
		}

		this.children = new ArrayList<FacilityTreePrivilege>();
	}

	// ----- instance フィールド ----- //

	/**
	 * 親ファシリティ（スコープ）を取得します。<BR>
	 * 
	 * @return 親
	 */
	public FacilityTreePrivilege getParent() {
		return this.parent;
	}

	/**
	 * 親ファシリティ（スコープ）を設定します。<BR>
	 * 
	 * @return 親ファシリティ
	 */
	public void setParent(FacilityTreePrivilege parent) {
		this.parent = parent;
	}

	/**
	 * ファシリティIDを取得します。<BR>
	 * 
	 * @return ファシリティID
	 */
	public String getFacilityId() {
		return facilityId;
	}

	/**
	 * ファシリティIDを設定します。<BR>
	 * 
	 * @param facilityId ファシリティID
	 */
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	/**
	 * ファシリティ名を取得します。<BR>
	 * 
	 * @return ファシリティ名
	 */
	public String getFacilityName() {
		return facilityName;
	}

	/**
	 * ファシリティ名を設定します。<BR>
	 * 
	 * @param facilityName ファシリティ名
	 */
	public void setFacilityName(String facilityName) {
		this.facilityName = facilityName;
	}

	/**
	 * ファシリティタイプを取得します。<BR>
	 * 
	 * @return ファシリティタイプ
	 */
	public Integer getFacilityType() {
		return facilityType;
	}

	/**
	 * ファシリティタイプを設定します。<BR>
	 * 
	 * @param facilityType ファシリティタイプ
	 */
	public void setFacilityType(Integer facilityType) {
		this.facilityType = facilityType;
	}

	/**
	 * 権限情報を取得します。<BR>
	 * 
	 * @return スコープ情報
	 */
	public Map<ObjectPrivilegeMode, Boolean> getPrivileges() {
		return this.privileges;
	}

	/**
	 * 権限情報を取得します。<BR>
	 * 
	 * @param mode  オブジェクト権限
	 * @return スコープ情報
	 */
	public Boolean getPrivilege(ObjectPrivilegeMode mode) {
		if (this.privileges == null
			|| this.privileges.get(mode) == null) {
			return false;
		} else {
			return this.privileges.get(mode);
		}
	}

	/**
	 * 権限情報を設定します。<BR>
	 * 
	 * @param privilege
	 *            権限情報
	 */
	public void setPrivileges(Map<ObjectPrivilegeMode, Boolean> privileges) {
		this.privileges = privileges;
	}

	/**
	 * 権限情報を設定します。<BR>
	 * 
	 * @param mode  オブジェクト権限
	 * @param privilege
	 *            権限情報
	 */
	public void setPrivilege(ObjectPrivilegeMode mode, Boolean privilege) {
		this.privileges.put(mode, privilege);
	}

	/**
	 * 子ファシリティを追加します。<BR>
	 * <p>
	 * 
	 * この際、childeの親はこのオブジェクトとして設定されます。
	 * 
	 * @param child
	 *            子
	 */
	public void addChildren(FacilityTreePrivilege child) {
		child.setParent(this);
		children.add(child);
	}

	/**
	 * 子ファシリティの数を取得します。<BR>
	 * 
	 * @return 子の数
	 */
	public int size() {
		return children.size();
	}

	/**
	 * 全ての子ファシリティを取得します。<BR>
	 * <p>
	 * 
	 * 並び順は、追加された順となっています。
	 * 
	 * @return 全ての子。
	 */
	public FacilityTreePrivilege[] getChildrenArray() {
		FacilityTreePrivilege[] result = new FacilityTreePrivilege[this.size()];
		return children.toArray(result);
	}
	/**
	 * 直下の子ファシリティのデータがあれば消します。<BR>
	 * 1つだけ消します。
	 * 
	 */
	public boolean removeChild(String facilityId){


		for(int i=0 ; i< children.size(); i++){

			if(facilityId.equals(children.get(i).getFacilityId())){

				//マッチした場合にはその要素を消します。
				children.remove(i);
				return true;
			}
		}

		return false;
	}
	/**
	 * 直下の子ファシリティのデータを取得します。<BR>
	 * 
	 */
	public FacilityTreePrivilege getChild(String facilityId){


		for(int i=0 ; i< children.size(); i++){

			if(facilityId.equals(children.get(i).getFacilityId())){

				//マッチした場合にはその要素を返します。
				return children.get(i);
			}
		}

		return null;
	}


	/**
	 * parentはnullとなっているので注意すること。
	 */
	@Override
	public FacilityTreePrivilege clone(){
		try {
			FacilityTreePrivilege cloneInfo = (FacilityTreePrivilege) super.clone();
			cloneInfo.parent = null;
			cloneInfo.privileges = new HashMap<ObjectPrivilegeMode, Boolean>(this.privileges);
			cloneInfo.facilityId = this.facilityId;
			cloneInfo.facilityName = this.facilityName;
			cloneInfo.facilityType = this.facilityType;
			cloneInfo.children = new ArrayList<FacilityTreePrivilege>();
			for (FacilityTreePrivilege item : this.children) {
				cloneInfo.children.add(item.clone());
			}
			return cloneInfo;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}
}