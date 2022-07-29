/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.util.RoleValidator;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.fault.FilterSettingDuplicate;
import com.clustercontrol.fault.FilterSettingNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.filtersetting.bean.FilterOwner;
import com.clustercontrol.filtersetting.bean.FilterSettingInfo;
import com.clustercontrol.filtersetting.bean.FilterSettingSearchPattern;
import com.clustercontrol.filtersetting.bean.FilterSettingSummaryInfo;
import com.clustercontrol.filtersetting.entity.FilterConditionEntity;
import com.clustercontrol.filtersetting.entity.FilterConditionItemEntity;
import com.clustercontrol.filtersetting.entity.FilterEntity;
import com.clustercontrol.filtersetting.entity.FilterEntityPK;
import com.clustercontrol.rest.endpoint.filtersetting.dto.enumtype.FilterCategoryEnum;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

import jakarta.persistence.EntityExistsException;

/**
 * 手続きベースのAPIを提供するfacadeです。
 */
public class FilterSettingControllerBean {
	private static final Log log = LogFactory.getLog(FilterSettingControllerBean.class);
	private static final Object insertMutex = new Object();

	/** テスト時に置換可能な外部依存処理 */
	public interface External {
		/** オーナーロールが正当(ロールIDが存在し、既存エンティティがあれば変更されていない)かどうかを検証し、不正なら例外を投げます。 */
		void validateOwnerRoleId(String ownerRoleId, FilterEntityPK id) throws InvalidSetting;

		/** ログインユーザがオーナーロールに所属しているかどうかを検証し、非所属なら例外を投げます。 */
		void validateUserRoles(String ownerRoleId) throws InvalidSetting;

		/** ログインユーザのIDを返します。 */
		String getLoginUserId();
	}

	/** デフォルトの外部依存処理実装 */
	private static final External defaultExternal = new External() {
		@Override
		public void validateOwnerRoleId(String ownerRoleId, FilterEntityPK id) throws InvalidSetting {
			CommonValidator.validateOwnerRoleId(ownerRoleId, true, id, HinemosModuleConstant.FILTER_SETTING);
		}

		@Override
		public void validateUserRoles(String ownerRoleId) throws InvalidSetting {
			RoleValidator.validateUserBelongRole(ownerRoleId, HinemosSessionContext.getLoginUserId(),
					HinemosSessionContext.isAdministrator());
		}

		@Override
		public String getLoginUserId() {
			return HinemosSessionContext.getLoginUserId();
		}
	};

	/** デフォルトの外部依存処理実装を返します。 */
	public static External getDefaultExternal() {
		return defaultExternal;
	}

	private External external = getDefaultExternal();

	public FilterSettingControllerBean() {
		this(getDefaultExternal());
	}

	public FilterSettingControllerBean(External external) {
		this.external = external;
	}

	/**
	 * 指定された条件に該当する共通フィルタ設定について、概要情報のリストを返します。
	 * ログインユーザについて参照オブジェクト権限をチェックします。
	 *
	 * @param filterCategory 検索対象のフィルタ分類。
	 * @param pattern フィルタ設定の検索パターン。
	 * @return 条件に概要したフィルタ設定の概要情報のリスト。
	 * @throws HinemosUnknown 想定外のエラーが発生した。
	 */
	public List<FilterSettingSummaryInfo> searchCommonFilterSettings(FilterCategoryEnum filterCategory,
			FilterSettingSearchPattern pattern) throws HinemosUnknown {
		Objects.requireNonNull(filterCategory, "filterCategory");
		Objects.requireNonNull(pattern, "pattern");

		try (FilterTx tx = new FilterTx()) {
			List<FilterEntity> entities;
			if (pattern.isNegative()) {
				entities = tx.searchFilterNot(
						filterCategory.getCode(),
						FilterOwner.ofCommon(),
						pattern.getLikePattern(),
						ObjectPrivilegeMode.READ);
			} else {
				entities = tx.searchFilters(
						filterCategory.getCode(),
						FilterOwner.ofCommon(),
						pattern.getLikePattern(),
						ObjectPrivilegeMode.READ);
			}
			tx.commit();
			return entities.stream().map(FilterSettingSummaryInfo::new).collect(Collectors.toList());

		} catch (RuntimeException e) {
			log.warn("Unexpected exception.", e);
			throw new HinemosUnknown(MessageConstant.MESSAGE_FLTSET_FAILED_TO_SEARCH.getMessage()
					+ " " + e.getMessage());
		}
	}

	/**
	 * 指定された条件に該当するユーザフィルタ設定について、概要情報のリストを返します。
	 * 
	 * @param filterCategory 検索対象のフィルタ分類。
	 * @param ownerUserId ユーザフィルタ設定の所有者ユーザID。
	 * @param pattern フィルタ設定の検索パターン。
	 * @return 条件に概要したフィルタ設定の概要情報のリスト。
	 * @throws HinemosUnknown 想定外のエラーが発生した。
	 */
	public List<FilterSettingSummaryInfo> searchUserFilterSettings(FilterCategoryEnum filterCategory, String ownerUserId,
			FilterSettingSearchPattern pattern) throws HinemosUnknown {
		Objects.requireNonNull(filterCategory, "filterCategory");
		Objects.requireNonNull(ownerUserId, "ownerUserId");
		Objects.requireNonNull(pattern, "pattern");

		try (FilterTx tx = new FilterTx()) {
			List<FilterEntity> entities;
			if (pattern.isNegative()) {
				entities = tx.searchFilterNot(
						filterCategory.getCode(),
						ownerUserId,
						pattern.getLikePattern(),
						ObjectPrivilegeMode.NONE);
			} else {
				entities = tx.searchFilters(
						filterCategory.getCode(),
						ownerUserId,
						pattern.getLikePattern(),
						ObjectPrivilegeMode.NONE);
			}
			tx.commit();
			return entities.stream().map(FilterSettingSummaryInfo::new).collect(Collectors.toList());

		} catch (RuntimeException e) {
			log.warn("Unexpected exception.", e);
			throw new HinemosUnknown(MessageConstant.MESSAGE_FLTSET_FAILED_TO_SEARCH.getMessage()
					+ " " + e.getMessage());
		}
	}

	/**
	 * 指定された条件に該当する全ユーザのフィルタ設定について、概要情報のリストを返します。
	 * 
	 * @param filterCategory 検索対象のフィルタ分類。
	 * @param pattern フィルタ設定の検索パターン。
	 * @return 条件に該当したフィルタ設定の概要情報のリスト。
	 * @throws HinemosUnknown 想定外のエラーが発生した。
	 */
	public List<FilterSettingSummaryInfo> searchAllUserFilterSettings(FilterCategoryEnum filterCategory,
			FilterSettingSearchPattern pattern) throws HinemosUnknown {
		Objects.requireNonNull(filterCategory, "filterCategory");
		Objects.requireNonNull(pattern, "pattern");

		try (FilterTx tx = new FilterTx()) {
			List<FilterEntity> entities;
			if (pattern.isNegative()) {
				entities = tx.searchFiltersOfAllUsersNot(
						filterCategory.getCode(),
						pattern.getLikePattern());
			} else {
				entities = tx.searchFiltersOfAllUsers(
						filterCategory.getCode(),
						pattern.getLikePattern());
			}
			tx.commit();
			return entities.stream().map(FilterSettingSummaryInfo::new).collect(Collectors.toList());

		} catch (RuntimeException e) {
			log.warn("Unexpected exception.", e);
			throw new HinemosUnknown(MessageConstant.MESSAGE_FLTSET_FAILED_TO_SEARCH.getMessage()
					+ " " + e.getMessage());
		}
	}

	/**
	 * 指定されたIDの共通フィルタ設定についての詳細な情報を返します。
	 * ログインユーザについて参照オブジェクト権限をチェックします。
	 * 
	 * @param filterCategory フィルタ分類。
	 * @param filterId フィルタID。
	 * @param objPriv チェックするオブジェクト権限。
	 * @return フィルタ設定の詳細な情報。
	 * @throws HinemosUnknown 想定外のエラーが発生した。
	 * @throws FilterSettingNotFound 指定されたフィルタ設定が存在しない。
	 * @throws InvalidRole 指定されたフィルタ設定に対する権限が不足している。
	 */
	public FilterSettingInfo getCommonFilterSetting(FilterCategoryEnum filterCategory, String filterId)
			throws HinemosUnknown, FilterSettingNotFound, InvalidRole {
		Objects.requireNonNull(filterCategory, "filterCategory");
		Objects.requireNonNull(filterId, "filterId");

		try (FilterTx tx = new FilterTx()) {
			FilterEntityPK pk = new FilterEntityPK(
					filterCategory.getCode(),
					FilterOwner.ofCommon(),
					filterId);

			FilterEntity entity = tx.findFilter(pk, ObjectPrivilegeMode.READ).orElse(null);
			if (entity == null) {
				throw new FilterSettingNotFound(MessageConstant.MESSAGE_FLTSET_NOT_FOUND
						.getMessage(pk.getFilterId()));
			}

			completeChildEntities(tx, Arrays.asList(entity));

			tx.commit();
			return new FilterSettingInfo(entity);

		} catch (RuntimeException e) {
			log.warn("Unexpected exception.", e);
			throw new HinemosUnknown(MessageConstant.MESSAGE_FLTSET_FAILED_TO_GET.getMessage(filterId)
					+ " " + e.getMessage());
		}
	}

	/**
	 * 指定されたIDのユーザフィルタ設定についての詳細な情報を返します。
	 * 
	 * @param filterCategory フィルタ分類。
	 * @param ownerUserId 所有者ユーザID。
	 * @param filterId フィルタID。
	 * @return フィルタ設定の詳細な情報。
	 * @throws HinemosUnknown 想定外のエラーが発生した。
	 * @throws FilterSettingNotFound 指定されたフィルタ設定が存在しない。
	 * @throws InvalidRole 指定されたフィルタ設定に対する権限が不足している。
	 */
	public FilterSettingInfo getUserFilterSetting(FilterCategoryEnum filterCategory, String ownerUserId, String filterId)
			throws HinemosUnknown, FilterSettingNotFound, InvalidRole {
		Objects.requireNonNull(filterCategory, "filterCategory");
		Objects.requireNonNull(ownerUserId, "ownerUserId");
		Objects.requireNonNull(filterId, "filterId");

		try (FilterTx tx = new FilterTx()) {
			FilterEntityPK pk = new FilterEntityPK(
					filterCategory.getCode(),
					ownerUserId,
					filterId);

			FilterEntity entity = tx.findFilter(pk, ObjectPrivilegeMode.NONE).orElse(null);
			if (entity == null) {
				throw new FilterSettingNotFound(MessageConstant.MESSAGE_FLTSET_NOT_FOUND
						.getMessage(pk.getFilterId()));
			}

			completeChildEntities(tx, Arrays.asList(entity));

			tx.commit();
			return new FilterSettingInfo(entity);

		} catch (RuntimeException e) {
			log.warn("Unexpected exception.", e);
			throw new HinemosUnknown(MessageConstant.MESSAGE_FLTSET_FAILED_TO_GET.getMessage(filterId)
					+ " " + e.getMessage());
		}
	}

	/**
	 * フィルタ設定を新規登録します。
	 * 
	 * @param filterSetting フィルタ設定情報。
	 * @return 登録後のフィルタ設定情報。
	 * @throws HinemosUnknown 想定外のエラーが発生した。
	 * @throws FilterSettingDuplicate 同一のIDが既に使用されている。
	 * @throws InvalidSetting フィルタ設定情報に問題がある。
	 */
	public FilterSettingInfo addFilterSetting(FilterSettingInfo filterSetting)
			throws HinemosUnknown, FilterSettingDuplicate, InvalidSetting {
		Objects.requireNonNull(filterSetting, "filterSetting");

		try {
			FilterEntity entity = filterSetting.toEntity();

			// 共通フィルタならオーナーロールIDを検証
			if (FilterOwner.isCommon(entity)) {
				external.validateOwnerRoleId(entity.getOwnerRoleId(), entity.getId());
				external.validateUserRoles(entity.getOwnerRoleId());
			}

			// 署名カラムを記録
			Long now = Long.valueOf(HinemosTime.currentTimeMillis());
			String loginUserId = external.getLoginUserId();
			entity.setRegDate(now);
			entity.setRegUser(loginUserId);
			entity.setUpdateDate(now);
			entity.setUpdateUser(loginUserId);

			// 悲観ロック代わりに同期を行う
			synchronized (insertMutex) {
				try (FilterTx tx = new FilterTx()) {
					// 重複チェック
					tx.checkEntityExists(FilterEntity.class, entity.getId());

					// FKチェックがあるので親テーブルから順番に persist&flush する
					tx.persist(entity);
					tx.flush();
					persistChildEntities(tx, entity);

					tx.commit();
					return new FilterSettingInfo(entity);
				} catch (EntityExistsException e) {
					throw new FilterSettingDuplicate(MessageConstant.MESSAGE_FLTSET_DUPLICATE
							.getMessage(entity.getId().getFilterId()));
				}
			}

		} catch (RuntimeException e) {
			log.warn("Unexpected exception.", e);
			throw new HinemosUnknown(
					MessageConstant.MESSAGE_FLTSET_FAILED_TO_ADD.getMessage(filterSetting.getFilterId())
							+ " " + e.getMessage());
		}
	}

	/**
	 * フィルタ設定を更新します。
	 * 共通フィルタ設定の場合はログインユーザについて変更オブジェクト権限をチェックします。
	 * <p>
	 * 同一トランザクション内での複数回呼び出しはデッドロックの可能性があります。
	 * 
	 * @param filterSetting フィルタ設定情報。
	 * @return 更新後のフィルタ設定情報。
	 * @throws HinemosUnknown 想定外のエラーが発生した。
	 * @throws FilterSettingNotFound 対象のフィルタ設定が存在しない。
	 * @throws InvalidSetting フィルタ設定情報に問題がある。
	 * @throws InvalidRole 指定されたフィルタ設定に対する権限が不足している。
	 */
	public FilterSettingInfo modifyFilterSetting(FilterSettingInfo filterSetting)
			throws HinemosUnknown, FilterSettingNotFound, InvalidSetting, InvalidRole {
		Objects.requireNonNull(filterSetting, "filterSetting");

		try (FilterTx tx = new FilterTx()) {
			tx.alertMultipleCall(getClass(), "modifyFilterSetting");

			// 共通フィルタならオーナーロールIDを検証
			// - ユーザの変更権限チェックはpre-commitで行われるのでここでは不要
			FilterEntityPK pk = filterSetting.getEntityPK();
			ObjectPrivilegeMode priv = ObjectPrivilegeMode.NONE;
			if (filterSetting.getCommon().booleanValue()) {
				external.validateOwnerRoleId(filterSetting.getOwnerRoleId(), pk);
				priv = ObjectPrivilegeMode.MODIFY;
			}

			// 古いエンティティを取得 (子孫は新規から移送するのでここは親のみでよい)
			FilterEntity entity = tx.findFilter(pk, priv).orElse(null);
			if (entity == null) {
				throw new FilterSettingNotFound(MessageConstant.MESSAGE_FLTSET_NOT_FOUND
						.getMessage(pk.getFilterId()));
			}

			// 新規エンティティを生成
			FilterEntity next = filterSetting.toEntity();

			// 移送する
			entity.setFilterName(next.getFilterName());
			entity.setFacilityId(next.getFacilityId());
			entity.setFacilityTarget(next.getFacilityTarget());
			entity.setFilterRange(next.getFilterRange());
			entity.setUpdateDate(HinemosTime.currentTimeMillis());
			entity.setUpdateUser(external.getLoginUserId());
			entity.setConditions(next.getConditions());

			// 親テーブルはupdateする
			tx.flush();
			// 子テーブルはdelete&insertする
			tx.removeConditionItems(pk);
			tx.flush();
			tx.removeConditions(pk);
			tx.flush();
			tx.clear(); // 削除したentityのキャッシュが残っているかもしれないのでクリア
			persistChildEntities(tx, entity);

			tx.commit();
			return new FilterSettingInfo(entity);

		} catch (RuntimeException e) {
			log.warn("Unexpected exception.", e);
			throw new HinemosUnknown(
					MessageConstant.MESSAGE_FLTSET_FAILED_TO_MODIFY.getMessage(filterSetting.getFilterId())
							+ " " + e.getMessage());
		}
	}

	/**
	 * 共通フィルタ設定を削除します。
	 * ログインユーザについてオブジェクト権限をチェックします。
	 * <p>
	 * 同一トランザクション内での複数回呼び出しはデッドロックの可能性があります。
	 * 
	 * @param filterCategory フィルタ分類。
	 * @param filterIds 削除したいフィルタ設定のIDリスト。
	 * @return 削除したフィルタ設定の情報。
	 * @throws HinemosUnknown 想定外のエラーが発生した。
	 * @throws InvalidRole 指定されたフィルタ設定いずれかに対する権限が不足している。
	 */
	public List<FilterSettingInfo> deleteCommonFilterSettings(FilterCategoryEnum filterCategory, List<String> filterIds)
			throws HinemosUnknown, InvalidRole {
		Objects.requireNonNull(filterCategory, "filterCategory");
		Objects.requireNonNull(filterIds, "filterIds");

		try (FilterTx tx = new FilterTx()) {
			tx.alertMultipleCall(getClass(), "deleteCommonFilterSettings");

			List<FilterEntity> deleted = deleteFilterSettings(tx, filterCategory, FilterOwner.ofCommon(), filterIds, ObjectPrivilegeMode.MODIFY);

			tx.commit();
			return deleted.stream().map(FilterSettingInfo::new).collect(Collectors.toList());

		} catch (RuntimeException e) {
			log.warn("Unexpected exception.", e);
			throw new HinemosUnknown(
					MessageConstant.MESSAGE_FLTSET_FAILED_TO_DELETE.getMessage(String.join(",", filterIds))
							+ " " + e.getMessage());
		}
	}

	/**
	 * ユーザフィルタ設定を削除します。
	 * <p>
	 * 同一トランザクション内での複数回呼び出しはデッドロックの可能性があります。
	 * 
	 * @param filterCategory フィルタ分類。
	 * @param ownerUserId ユーザフィルタ設定の場合の所有者ユーザID。
	 * @param filterIds 削除したいフィルタ設定のIDリスト。
	 * @return 削除したフィルタ設定の情報。
	 * @throws HinemosUnknown 想定外のエラーが発生した。
	 * @throws InvalidRole 指定されたフィルタ設定いずれかに対する権限が不足している。
	 */
	public List<FilterSettingInfo> deleteUserFilterSettings(FilterCategoryEnum filterCategory, String ownerUserId, List<String> filterIds)
			throws HinemosUnknown, InvalidRole {
		Objects.requireNonNull(filterCategory, "filterCategory");
		Objects.requireNonNull(ownerUserId, "ownerUserId");
		Objects.requireNonNull(filterIds, "filterIds");

		try (FilterTx tx = new FilterTx()) {
			tx.alertMultipleCall(getClass(), "deleteUserFilterSettings");

			List<FilterEntity> deleted = deleteFilterSettings(tx, filterCategory, ownerUserId, filterIds, ObjectPrivilegeMode.NONE);

			tx.commit();
			return deleted.stream().map(FilterSettingInfo::new).collect(Collectors.toList());

		} catch (RuntimeException e) {
			log.warn("Unexpected exception.", e);
			throw new HinemosUnknown(
					MessageConstant.MESSAGE_FLTSET_FAILED_TO_DELETE.getMessage(String.join(",", filterIds))
							+ " " + e.getMessage());
		}
	}

	/** 削除下請け */
	private List<FilterEntity> deleteFilterSettings(FilterTx tx, FilterCategoryEnum filterCategory, String ownerUserId,
			List<String> filterIds, ObjectPrivilegeMode objPriv) throws InvalidRole {
		FilterEntityPK pk = new FilterEntityPK(
				filterCategory.getCode(),
				ownerUserId,
				null /* set later */);

		// 削除対象のエンティティを取得
		List<FilterEntity> entities = new ArrayList<>();
		for (String filterId : filterIds) {
			pk.setFilterId(filterId);
			FilterEntity entity = tx.findFilter(pk, objPriv).orElse(null);
			if (entity != null) {
				entities.add(entity);
			}
		}
		completeChildEntities(tx, entities); // 子孫データを補填

		// 削除を実行
		String msg = deleteFilterEntities(tx, entities);
		log.info("deleteFilterSettings: removedFilters=" + msg);

		return entities;
	}

	/**
	 * 指定されたユーザに紐づくフィルタ設定を全削除します。
	 * <p>
	 * 同一トランザクション内での複数回呼び出しはデッドロックの可能性があります。
	 * 
	 * @param userIds ユーザIDのリスト。
	 */
	public void deleteAllFilterSettingsOf(List<String> userIds) throws HinemosUnknown {
		Objects.requireNonNull(userIds, "userIds");

		try (FilterTx tx = new FilterTx()) {
			tx.alertMultipleCall(getClass(), "deleteAllFilterSettingOf");

			// 特定ユーザの全フィルタ設定のエンティティを取得
			List<FilterEntity> entities = tx.searchFiltersByOwners(userIds, ObjectPrivilegeMode.NONE);
			completeChildEntities(tx, entities); // 子孫データを補填

			// 削除を実行
			String msg = deleteFilterEntities(tx, entities);
			log.info("deleteAllFilterSettingOf: userIds=" + userIds + ", removedFilters=" + msg);

			tx.commit();
		} catch (RuntimeException e) {
			log.warn("Unexpected exception.", e);
			throw new HinemosUnknown(
					MessageConstant.MESSAGE_FLTSET_FAILED_TO_DELETE_USER.getMessage(String.join(",", userIds))
							+ " " + e.getMessage());
		}
	}

	/**
	 * 渡された {@link FilterEntity} を remove&flush して、そのIDをリストアップした文字列を返します。
	 */
	private String deleteFilterEntities(FilterTx tx, List<FilterEntity> entities) {
		StringBuilder msg = new StringBuilder();
		FilterEntity.sortForSequentialUpdate(entities);
		for (FilterEntity entity : entities) {
			tx.remove(entity);  // 子孫テーブルはPostgreSQLのFK制約でcascade削除される
			tx.flush();
			msg.append(entity.getId().toString() + ", ");
		}
		tx.clear(); // 子孫テーブルのentityがキャッシュに残っているかもしれないので
		return msg.toString();
	}

	/**
	 * 渡された {@link FilterEntity} について、{@link FilterConditionEntity} と {@link FilterConditionItemEntity}
	 * をデータベースから取得してセットします。
	 */
	private void completeChildEntities(FilterTx tx, List<FilterEntity> entities) {
		for (FilterEntity entity : entities) {
			// filter_condition
			List<FilterConditionEntity> conditions = tx.findConditions(entity.getId());
			for (FilterConditionEntity condition : conditions) {
				condition.setItems(new ArrayList<>());
			}
			entity.setConditions(conditions);

			// filter_condition_item
			List<FilterConditionItemEntity> items = tx.findConditionItems(entity.getId());
			for (FilterConditionItemEntity item : items) {
				FilterConditionEntity condition = conditions.get(item.getId().getConditionIdx().intValue());
				condition.getItems().add(item);
			}
		}
	}

	/**
	 * 渡された {@link FilterEntity} について、{@link FilterConditionEntity} と {@link FilterConditionItemEntity}
	 * を永続化します。
	 * {@link FilterEntity} 自身は永続化しません。
	 */
	private void persistChildEntities(FilterTx tx, FilterEntity entity) {
		for (FilterConditionEntity cond : entity.getConditions()) {
			tx.persist(cond);
		}
		tx.flush();
		for (FilterConditionEntity cond : entity.getConditions()) {
			for (FilterConditionItemEntity item : cond.getItems()) {
				tx.persist(item);
			}
		}
		tx.flush();
	}
}
