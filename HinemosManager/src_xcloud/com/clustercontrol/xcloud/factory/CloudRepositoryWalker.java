/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.bean.FacilityTreeItem;
import com.clustercontrol.repository.model.ScopeInfo;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.InternalManagerError;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.common.CloudConstants;
import com.clustercontrol.xcloud.factory.Repository.RepositoryVisitor;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.EntityEntity;
import com.clustercontrol.xcloud.model.InstanceEntity;
import com.clustercontrol.xcloud.model.LocationEntity;
import com.clustercontrol.xcloud.util.CollectionComparator;
import com.clustercontrol.xcloud.util.FacilityIdUtil;
import com.clustercontrol.xcloud.util.CloudUtil;
import com.clustercontrol.xcloud.util.RepositoryControllerBeanWrapper;
import com.clustercontrol.xcloud.util.RepositoryUtil;

/*
 * リソースツリーをリポジトリツリーから作成する。
 *
 */
public class CloudRepositoryWalker {
	private RepositoryVisitor visitor;
	
	private CloudRepositoryWalker() {
	}
	
	public void walk(RepositoryVisitor visitor) throws CloudManagerException {
		walk(visitor, null);
	}
	
	public void walkByOwerRole(RepositoryVisitor visitor, String ownerRoleId) throws CloudManagerException {
		List<CloudScopeEntity> cloudScopes = CloudManager.singleton().getCloudScopes().getCloudScopesByOwnerRole(ownerRoleId);
		startComparison(visitor, cloudScopes);
	}
	
	public void walk(RepositoryVisitor visitor, String hinemosUser) throws CloudManagerException {
		List<CloudScopeEntity> cloudScopes;
		if (hinemosUser == null) {
			cloudScopes = CloudManager.singleton().getCloudScopes().getCloudScopesByCurrentHinemosUser();
		} else {
			cloudScopes = CloudManager.singleton().getCloudScopes().getCloudScopesByHinemosUser(hinemosUser);
		}
		startComparison(visitor, cloudScopes);
	}
	
	protected void startComparison(RepositoryVisitor visitor, Collection<CloudScopeEntity> cloudScopes) throws CloudManagerException {
	//		リポジトリの前提としては以下。
	//		1. リポジトリは、ユーザーが変更する可能性がある
	//		2. ファシリティ ID が一致しても階層が想定外の可能性もある
	//		上記前提のもと、クライアントへ戻すクラウド管理オプション管理下のリポジトリの内容は、
	//		1. ロケーションまでは更新を行う。
	//		2. ロケーションは以下は、ID でフィルタを行う
	//		積極的にクラウド管理が想定する階層を維持しない
		
		this.visitor = visitor;
		
		// 登録済みのクラウドスコープを public と private に選別。
		final Map<String, List<CloudScopeEntity>> scopesMap = new HashMap<>();
		{
			final List<CloudScopeEntity> publicCloudScopes = new ArrayList<>();
			final List<CloudScopeEntity> privateCloudScopes = new ArrayList<>();
			for (CloudScopeEntity cloudScope: cloudScopes) {
				if (cloudScope.isPublic()) {
					publicCloudScopes.add(cloudScope);
				} else {
					privateCloudScopes.add(cloudScope);
				}
			}
			scopesMap.put(CloudConstants.publicRootId, publicCloudScopes);
			scopesMap.put(CloudConstants.privateRootId, privateCloudScopes);
		}
		
		FacilityTreeItem treeItem;
		try {
			treeItem = RepositoryControllerBeanWrapper.bean().getFacilityTree(null, Locale.getDefault());
		} catch (HinemosUnknown e) {
			throw new InternalManagerError(e.getMessage(), e);
		}
		List<FacilityTreeItem> treeItems = CloudUtil.collectScopes(treeItem, CloudConstants.publicRootId, CloudConstants.privateRootId);
		
		this.visitor.visitStart();
		
		CollectionComparator.compare(new ArrayList<>(Arrays.asList(CloudConstants.publicRootId, CloudConstants.privateRootId)), treeItems, new CollectionComparator.Comparator<String, FacilityTreeItem>() {
			@Override
			public boolean match(String o1, FacilityTreeItem o2) throws CloudManagerException {
				return o1.equals(o2.getData().getFacilityId()) && o2.getData().getFacilityType() == FacilityConstant.TYPE_SCOPE;
			}
			// リポジトリ上にクラウドスコープに該当するスコープが存在している場合
			@Override
			public void matched(String o1, FacilityTreeItem o2) throws CloudManagerException {
				CloudRepositoryWalker.this.visitor.visitCloudScopeRootScope(o2.getData());
				compareCloudScopes(scopesMap.get(o1), o2);
			}
			// リポジトリ上にクラウドスコープに該当するスコープがない
			@Override
			public void afterO1(String o1) throws CloudManagerException {
				final ScopeInfo rootScope;
				switch (o1) {
				case CloudConstants.privateRootId:
					rootScope = RepositoryUtil.createPrivateRootScope();
					break;
				case CloudConstants.publicRootId:
					rootScope = RepositoryUtil.createPublicRootScope();
					break;
				default:
					throw new InternalManagerError();
				}
				CloudRepositoryWalker.this.visitor.visitCloudScopeRootScope(rootScope);
				
				List<CloudScopeEntity> cloudScopes = scopesMap.get(o1);
				for (CloudScopeEntity cloudScope: cloudScopes) {
					RepositoryUtil.createCloudScopeScope(cloudScope, new RepositoryUtil.Tracer() {
						@Override
						public void traceCloudScope(final ScopeInfo cloudScopeScope, CloudScopeEntity cloudScope) throws CloudManagerException {
							CloudRepositoryWalker.this.visitor.visitCloudScopeScope(rootScope, cloudScopeScope, cloudScope);
							
							RepositoryUtil.createCloudScopeNodeIfExist(cloudScope);
							
							// ロケーション用のスコープ作成。
							RepositoryUtil.createLocationScopes(cloudScope, new RepositoryUtil.Tracer() {
								@Override
								public void traceLocation(ScopeInfo locationScope, LocationEntity location) throws CloudManagerException {
									CloudRepositoryWalker.this.visitor.visitLocationScope(cloudScopeScope, locationScope, location);
								}
							});
						}
					});
				}
			}
		});
		visitor.visitEnd();
	}
	
	protected void compareCloudScopes(Collection<CloudScopeEntity> cloudScopes, final FacilityTreeItem rootFacility) throws CloudManagerException {
		CollectionComparator.compare(cloudScopes, rootFacility.getChildren(), new CollectionComparator.Comparator<CloudScopeEntity, FacilityTreeItem>() {
			@Override
			public boolean match(CloudScopeEntity o1, FacilityTreeItem o2) throws CloudManagerException {
				return FacilityIdUtil.getCloudScopeScopeId(o1).equals(o2.getData().getFacilityId()) && o2.getData().getFacilityType() == FacilityConstant.TYPE_SCOPE;
			}
			// リポジトリ上にクラウドスコープに該当するスコープが存在している場合
			@Override
			public void matched(CloudScopeEntity o1, FacilityTreeItem o2) throws CloudManagerException {
				visitor.visitCloudScopeScope(rootFacility.getData(), o2.getData(), o1);

				RepositoryUtil.createCloudScopeNodeIfExist(o1);

				// 引き続きロケーションの更新
				int count = o1.getLocations().size();
				if (count == 1) {
					Pattern p = Pattern.compile(String.format("^_(.*)(_%s|_%s_(.*))$", o1.getId(), o1.getId()));
					for (FacilityTreeItem child: o2.getChildren()) {
						recursiveWalkLocationResources(o1.getLocations().get(0), o2, child, p);
					}
				} else {
					// ロケーションの一覧取得
					compareLocations(o1, o2);
				}
			}
			// リポジトリ上にクラウドスコープに該当するスコープがない
			@Override
			public void afterO1(CloudScopeEntity o1) throws CloudManagerException {
				RepositoryUtil.createCloudScopeRepository(o1, new RepositoryUtil.Tracer() {
					private ScopeInfo cloudScopeScope;
					
					@Override
					public void traceCloudScope(ScopeInfo cloudScopeScope, CloudScopeEntity cloudScope) throws CloudManagerException {
						this.cloudScopeScope = cloudScopeScope;
						// クライアントへ返す形式へ変換。
						visitor.visitCloudScopeScope(rootFacility.getData(), cloudScopeScope, cloudScope);
					}
					@Override
					public void traceLocation(ScopeInfo locationScope, LocationEntity locationEnity) throws CloudManagerException {
						visitor.visitLocationScope(cloudScopeScope, locationScope, locationEnity);
						// クラウドオプション管理のリポジトリツリーのみの取得処理なので、ロケーション配下の情報は更新しない。
					}
				});
			}
			// リポジトリ上にクラウドスコープに該当しないスコープがある
			@Override
			public void afterO2(FacilityTreeItem o2) throws CloudManagerException {
				// 無視する。
			}
		});
	}
	
	protected void compareLocations(final CloudScopeEntity cloudScopeEntity, final FacilityTreeItem cloudScopeScope) throws CloudManagerException {
		CollectionComparator.compare(cloudScopeEntity.getLocations(), cloudScopeScope.getChildren(), new CollectionComparator.Comparator<LocationEntity, FacilityTreeItem>() {
			@Override
			public boolean match(LocationEntity o1, FacilityTreeItem o2) throws CloudManagerException {
				return FacilityIdUtil.getLocationScopeId(o1.getCloudScope().getId(), o1).equals(o2.getData().getFacilityId()) && o2.getData().getFacilityType() == FacilityConstant.TYPE_SCOPE;
			}
			// リポジトリ上にロケーションに該当するスコープが存在している場合
			@Override
			public void matched(LocationEntity o1, FacilityTreeItem o2) throws CloudManagerException {
				visitor.visitLocationScope(cloudScopeScope.getData(), o2.getData(), o1);
				
				Pattern p = Pattern.compile(String.format("^_(.*)(_%s|_%s_(.*))$", cloudScopeEntity.getId(), cloudScopeEntity.getId()));
				for (FacilityTreeItem child: o2.getChildren()) {
					recursiveWalkLocationResources(o1, o2, child, p);
				}
			}
			// リポジトリ上にロケーションに該当するスコープがない
			@Override
			public void afterO1(LocationEntity o1) throws CloudManagerException {
				// ロケーションのスコープ作成
				RepositoryUtil.createLocationScope(o1, new RepositoryUtil.Tracer() {
					@Override
					public void traceLocation(ScopeInfo scope, LocationEntity location) throws CloudManagerException {
						visitor.visitLocationScope(cloudScopeScope.getData(), scope, location);
					}
				});
				// クラウドオプション管理のリポジトリツリーのみの取得処理なので、ロケーション配下の情報は更新しない。
			}
			// リポジトリ上にロケーションに該当しないスコープがある
			@Override
			public void afterO2(FacilityTreeItem o2) throws CloudManagerException {
				// 無視する。
			}
		});
	}
	
	protected void recursiveWalkLocationResources(LocationEntity location, FacilityTreeItem parent, FacilityTreeItem treeItem, Pattern pattern) throws CloudManagerException {
		String allScopeId = FacilityIdUtil.getAllNodeScopeId(location.getCloudScope().getPlatformId(), location.getCloudScope().getId());
		if (allScopeId.equals(treeItem.getData().getFacilityId()))
			return;
		
		Matcher m = pattern.matcher(treeItem.getData().getFacilityId());
		if (!m.matches())
			return;
		
		switch (treeItem.getData().getFacilityType()) {
		case FacilityConstant.TYPE_SCOPE:
			visitor.visitFolder(parent.getData(), treeItem.getData());
			for (FacilityTreeItem child: treeItem.getChildren()) {
				recursiveWalkLocationResources(location, treeItem, child, pattern);
			}
			break;
		case FacilityConstant.TYPE_NODE:
			HinemosEntityManager em = Session.current().getEntityManager();
			try {
				TypedQuery<InstanceEntity> query = em.createNamedQuery(InstanceEntity.findInstancesByFacilityIds, InstanceEntity.class);
				query.setParameter("cloudScopeId", location.getCloudScope().getCloudScopeId());
				query.setParameter("facilityIds", Arrays.asList(treeItem.getData().getFacilityId()));
				InstanceEntity instanceEntity = query.getSingleResult();
				visitor.visitInstance(parent.getData(), treeItem.getData(), instanceEntity);
			} catch (NoResultException e) {
				try {
					TypedQuery<EntityEntity> query = em.createNamedQuery(EntityEntity.findEntitiesByFacilityIds, EntityEntity.class);
					query.setParameter("cloudScopeId", location.getCloudScope().getCloudScopeId());
					query.setParameter("facilityIds", Arrays.asList(treeItem.getData().getFacilityId()));
					EntityEntity entityEntity = query.getSingleResult();
					visitor.visitEntity(parent.getData(), treeItem.getData(), entityEntity);
				} catch (NoResultException e1) {
				}
			}
			break;
		default:
			break;
		}
	}
	
	public static void walkCloudRepository(RepositoryVisitor visitor, String hinemosUserId) throws CloudManagerException {
		new CloudRepositoryWalker().walk(visitor, hinemosUserId);
	}
	
	public static void walkCloudRepository(RepositoryVisitor visitor) throws CloudManagerException {
		new CloudRepositoryWalker().walk(visitor);
	}
	
	public static void walkCloudRepositoryByOwnerRole(RepositoryVisitor visitor, String ownerRoleId) throws CloudManagerException {
		new CloudRepositoryWalker().walkByOwerRole(visitor, ownerRoleId);
	}
}
