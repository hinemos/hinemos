package com.clustercontrol.notify.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;


/**
 * The persistent class for the cc_notify_relation_info database table.
 * 
 */
@XmlType(namespace = "http://notify.ws.clustercontrol.com")
@Entity
@Table(name="cc_notify_relation_info", schema="setting")
@Cacheable(true)
public class NotifyRelationInfo implements Serializable, Comparable<NotifyRelationInfo> {
	private static final long serialVersionUID = 1L;
	private NotifyRelationInfoPK id;
	private Integer notifyType;

	public NotifyRelationInfo() {
	}

	public NotifyRelationInfo(NotifyRelationInfoPK pk) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
	}

	public NotifyRelationInfo(String notifyGroupId, String notifyId) {
		this(new NotifyRelationInfoPK(notifyGroupId, notifyId));
	}

	@XmlTransient
	@EmbeddedId
	public NotifyRelationInfoPK getId() {
		if (id == null)
			id = new NotifyRelationInfoPK();
		return this.id;
	}

	public void setId(NotifyRelationInfoPK id) {
		this.id = id;
	}
	
	@Transient
	public String getNotifyGroupId() {
		return getId().getNotifyGroupId();
	}
	public void setNotifyGroupId(String notifyGroupId) {
		getId().setNotifyGroupId(notifyGroupId);
	}

	@Transient
	public String getNotifyId() {
		return getId().getNotifyId();
	}
	public void setNotifyId(String notifyId) {
		getId().setNotifyId(notifyId);
	}

	@Column(name="notify_type")
	public Integer getNotifyType() {
		return this.notifyType;
	}

	public void setNotifyType(Integer notifyType) {
		this.notifyType = notifyType;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getNotifyGroupId() == null) ? 0 : getNotifyGroupId().hashCode());
		result = prime * result
				+ ((getNotifyId() == null) ? 0 : getNotifyId().hashCode());
		result = prime * result
				+ ((notifyType == null) ? 0 : notifyType.hashCode());
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
		NotifyRelationInfo other = (NotifyRelationInfo) obj;
		if (getNotifyGroupId() == null) {
			if (other.getNotifyGroupId() != null)
				return false;
		} else if (!getNotifyGroupId().equals(other.getNotifyGroupId()))
			return false;
		if (getNotifyId() == null) {
			if (other.getNotifyId() != null)
				return false;
		} else if (!getNotifyId().equals(other.getNotifyId()))
			return false;
		if (notifyType == null) {
			if (other.notifyType != null)
				return false;
		} else if (!notifyType.equals(other.notifyType))
			return false;
		return true;
	}

	@Override
	public int compareTo(NotifyRelationInfo o) {
		return (this.getNotifyGroupId() + this.getNotifyId() + this.notifyType).compareTo(
				o.getNotifyGroupId() + o.getNotifyId() + o.notifyType);
	}

	/**
	 * 単体テスト用
	 * @param args
	 */
	public static void main (String args[]) {
		testEquals();
	}
	public static void testEquals() {

		System.out.println("=== NotifyRelationInfo の単体テスト ===");

		System.out.println("*** 全部一致 ***");
		NotifyRelationInfo info1 = new NotifyRelationInfo();
		info1.setNotifyGroupId("notifyGroup");
		info1.setNotifyId("notify");
		info1.setNotifyType(0);
		NotifyRelationInfo info2 = new NotifyRelationInfo();
		info2.setNotifyGroupId("notifyGroup");
		info2.setNotifyId("notify");
		info2.setNotifyType(0);

		judge(true, info1.equals(info2));

		System.out.println("*** 通知グループIDのみ違う ***");
		info2 = new NotifyRelationInfo();
		info2.setNotifyGroupId("notifyGroup_1");
		info2.setNotifyId("notify");
		info2.setNotifyType(0);

		judge(false, info1.equals(info2));

		System.out.println("*** 通知IDのみ違う ***");
		info2 = new NotifyRelationInfo();
		info2.setNotifyGroupId("notifyGroup");
		info2.setNotifyId("notify_1");
		info2.setNotifyType(0);

		judge(false, info1.equals(info2));

		System.out.println("*** 通知タイプのみ違う ***");
		info2 = new NotifyRelationInfo();
		info2.setNotifyGroupId("notifyGroup");
		info2.setNotifyId("notify");
		info2.setNotifyType(1);

		judge(false, info1.equals(info2));

		System.out.println("*** 通知フラグのみ違う ***");
		info2 = new NotifyRelationInfo();
		info2.setNotifyGroupId("notifyGroup");
		info2.setNotifyId("notify");
		info2.setNotifyType(0);

		judge(false, info1.equals(info2));
	}
	/**
	 * 単体テストの結果が正しいものか判断する
	 * @param judge
	 * @param result
	 */
	private static void judge(boolean judge, boolean result){

		System.out.println("expect : " + judge);
		System.out.print("result : " + result);
		String ret = "NG";
		if (judge == result) {
			ret = "OK";
		}
		System.out.println("    is ...  " + ret);
	}
}