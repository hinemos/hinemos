package com.clustercontrol.nodemap.model;

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

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;


/**
 * The persistent class for the cc_map_position database table.
 * 
 */
@Entity
@Table(name="cc_map_position", schema="setting")
@Cacheable(true)
public class MapPositionEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private MapPositionEntityPK id;
	private Integer x;
	private Integer y;
	private MapInfoEntity mapInfoEntity;

	@Deprecated
	public MapPositionEntity() {
	}

	public MapPositionEntity(MapPositionEntityPK pk,
			MapInfoEntity mapInfoEntity) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToMapInfoEntity(mapInfoEntity);
	}

	public MapPositionEntity(String mapId, String elementId,
			MapInfoEntity mapInfoEntity) {
		this(new MapPositionEntityPK(mapId, elementId), mapInfoEntity);
	}


	@EmbeddedId
	public MapPositionEntityPK getId() {
		return this.id;
	}

	public void setId(MapPositionEntityPK id) {
		this.id = id;
	}


	public Integer getX() {
		return this.x;
	}

	public void setX(Integer x) {
		this.x = x;
	}


	public Integer getY() {
		return this.y;
	}

	public void setY(Integer y) {
		this.y = y;
	}


	//bi-directional many-to-one association to MapInfoEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="map_id", insertable=false, updatable=false)
	public MapInfoEntity getMapInfoEntity() {
		return this.mapInfoEntity;
	}

	@Deprecated
	public void setMapInfoEntity(MapInfoEntity mapInfoEntity) {
		this.mapInfoEntity = mapInfoEntity;
	}

	/**
	 * MapInfoEntityオブジェクト参照設定<BR>
	 * 
	 * MapInfoEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToMapInfoEntity(MapInfoEntity mapInfoEntity) {
		this.setMapInfoEntity(mapInfoEntity);
		if (mapInfoEntity != null) {
			List<MapPositionEntity> list = mapInfoEntity.getMapPositionEntities();
			if (list == null) {
				list = new ArrayList<MapPositionEntity>();
			} else {
				for(MapPositionEntity entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			mapInfoEntity.setMapPositionEntities(list);
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

		// MapInfoEntity
		if (this.mapInfoEntity != null) {
			List<MapPositionEntity> list = this.mapInfoEntity.getMapPositionEntities();
			if (list != null) {
				Iterator<MapPositionEntity> iter = list.iterator();
				while(iter.hasNext()) {
					MapPositionEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}