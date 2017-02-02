package com.clustercontrol.nodemap.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;


/**
 * The persistent class for the cc_map_info database table.
 * 
 */
@Entity
@Table(name="cc_map_info", schema="setting")
@Cacheable(true)
public class MapInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private String mapId;
	private MapBgImageEntity mapBgImageEntity;
	private List<MapPositionEntity> mapPositionEntities;

	@Deprecated
	public MapInfoEntity() {
	}

	public MapInfoEntity(String mapId,
			MapBgImageEntity mapBgImageEntity) {
		this.setMapId(mapId);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToMapBgImageEntity(mapBgImageEntity);
	}


	@Id
	@Column(name="map_id")
	public String getMapId() {
		return this.mapId;
	}

	public void setMapId(String mapId) {
		this.mapId = mapId;
	}


	//bi-directional many-to-one association to MapBgImageEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="background_image")
	public MapBgImageEntity getMapBgImageEntity() {
		return this.mapBgImageEntity;
	}

	@Deprecated
	public void setMapBgImageEntity(MapBgImageEntity mapBgImageEntity) {
		this.mapBgImageEntity = mapBgImageEntity;
	}

	/**
	 * MapBgImageEntityオブジェクト参照設定<BR>
	 * 
	 * MapBgImageEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToMapBgImageEntity(MapBgImageEntity mapBgImageEntity) {
		this.setMapBgImageEntity(mapBgImageEntity);
		if (mapBgImageEntity != null) {
			List<MapInfoEntity> list = mapBgImageEntity.getMapInfoEntities();
			if (list == null) {
				list = new ArrayList<MapInfoEntity>();
			} else {
				for(MapInfoEntity entity : list){
					if (entity.getMapId().equals(this.mapId)) {
						return;
					}
				}
			}
			list.add(this);
			mapBgImageEntity.setMapInfoEntities(list);
		}
	}


	//bi-directional many-to-one association to MapPositionEntity
	@OneToMany(mappedBy="mapInfoEntity", fetch=FetchType.LAZY)
	public List<MapPositionEntity> getMapPositionEntities() {
		return this.mapPositionEntities;
	}

	public void setMapPositionEntities(List<MapPositionEntity> mapPositionEntities) {
		this.mapPositionEntities = mapPositionEntities;
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

		// MapBgImageEntity
		if (this.mapBgImageEntity != null) {
			List<MapInfoEntity> list = this.mapBgImageEntity.getMapInfoEntities();
			if (list != null) {
				Iterator<MapInfoEntity> iter = list.iterator();
				while(iter.hasNext()) {
					MapInfoEntity entity = iter.next();
					if (entity.getMapId().equals(this.getMapId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}