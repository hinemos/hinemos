package com.clustercontrol.calendar.model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * The persistent class for the cc_cal_pattern_detail_info database table.
 * 
 */
@XmlType(namespace = "http://calendar.ws.clustercontrol.com")
@Entity
@Table(name="cc_cal_pattern_detail_info", schema="setting")
@Cacheable(true)
public class YMD implements Serializable {
	private static final long serialVersionUID = 1L;
	private YMDPK id;
	private CalendarPatternInfo calPatternInfoEntity;

	public YMD() {
	}

	public YMD(YMDPK pk) {
		this.setId(pk);
	}

	public YMD(String calPatternId,
			Integer yearNo, Integer monthNo, Integer dayNo) {
		this(new YMDPK(calPatternId, yearNo, monthNo, dayNo));
	}

	@XmlTransient
	@EmbeddedId
	public YMDPK getId() {
		if (id == null)
			id = new YMDPK();
		return this.id;
	}

	public void setId(YMDPK id) {
		this.id = id;
	}

	@Transient
	public String getCalPatternId() {
		return getId().getCalPatternId();
	}
	public void setCalPatternId(String calPatternId) {
		getId().setCalPatternId(calPatternId);
	}
	
	@Transient
	public Integer getYear() {
		return getId().getYear();
	}
	public void setYear(Integer year) {
		getId().setYear(year);
	}
	
	@Transient
	public Integer getMonth() {
		return getId().getMonth();
	}
	public void setMonth(Integer month) {
		getId().setMonth(month);
	}

	@Transient
	public Integer getDay() {
		return getId().getDay();
	}
	public void setDay(Integer day) {
		getId().setDay(day);
	}
	
	//bi-directional many-to-one association to CalPatternInfoEntity
	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="calendar_pattern_id", insertable=false, updatable=false)
	public CalendarPatternInfo getCalPatternInfoEntity() {
		return this.calPatternInfoEntity;
	}

	@Deprecated
	public void setCalPatternInfoEntity(CalendarPatternInfo calPatternInfoEntity) {
		this.calPatternInfoEntity = calPatternInfoEntity;
	}

	/**
	 * CalPatternInfoEntityオブジェクト参照設定<BR>
	 * 
	 * CalPatternInfoEntity設定時はSetterに代わりこちらを使用すること。
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void relateToCalPatternInfoEntity(CalendarPatternInfo calPatternInfoEntity) {
		this.setCalPatternInfoEntity(calPatternInfoEntity);
		if (calPatternInfoEntity != null) {
			List<YMD> list = calPatternInfoEntity.getYmd();
			if (list == null) {
				list = new ArrayList<YMD>();
			} else {
				for(YMD entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			calPatternInfoEntity.setYmd(list);
		}
	}


	/**
	 * 削除前処理<BR>
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void unchain() {

		// CalPatternInfoEntity
		if (this.calPatternInfoEntity != null) {
			List<YMD> list = this.calPatternInfoEntity.getYmd();
			if (list != null) {
				Iterator<YMD> iter = list.iterator();
				while(iter.hasNext()) {
					YMD entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}
	
	/**
	 * 年月日をyyyy/MM/dd形式で返す<BR>
	 * @return yyyy/MM/dd
	 */
	public String yyyyMMdd(){
		String ret = getYear() + "/" + getMonth() + "/" + getDay();
		return ret;
	}
}