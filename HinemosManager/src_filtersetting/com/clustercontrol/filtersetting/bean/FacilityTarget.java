/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.bean;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.RoleSettingTreeConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.repository.bean.FacilityTargetConstant;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.rest.dto.EnumDto;

/**
 * ファシリティターゲット
 */
public enum FacilityTarget implements EnumDto<Integer> {
	/** 直下 */
	ONE_LEVEL(FacilityTargetConstant.TYPE_BENEATH),

	/** 配下全て */
	ALL(FacilityTargetConstant.TYPE_ALL);

	private static final Log logger = LogFactory.getLog(FacilityTarget.class);

	/** DBにおけるコード値 */
	private Integer code;

	private FacilityTarget(int code) {
		this.code = Integer.valueOf(code);
	}

	/**
	 * コードから列挙値へ変換します。
	 */
	public static FacilityTarget fromCode(Integer code) {
		for (FacilityTarget it : FacilityTarget.values()) {
			if (it.code.equals(code)) return it;
		}
		throw new IllegalArgumentException("Unknown code=" + code);
	}

	@Override
	public Integer getCode() {
		return code;
	}

	/**
	 * 対応する {@link RepositoryControllerBean} 内の定数へ変換します。
	 */
	public int toRepositoryLevelCode() {
		switch (this) {
		case ALL:
			return RepositoryControllerBean.ALL;
		case ONE_LEVEL:
			return RepositoryControllerBean.ONE_LEVEL;
		}
		throw new RuntimeException("Must add the case " + this.name() + "!!!");
	}

	/**
	 * このターゲットの種類に従って、指定されたファシリティIDを起点としたファシリティID群へ展開します。<br/>
	 * 指定されたファシリティIDが null または空文字列の場合、全ファシリティIDを意味する null を返します。<br/>
	 * 例外により展開に失敗した場合は、処理を中止せず、かつ影響が最小限になるように、空のリストを返します。
	 */
	public List<String> expandFacilityIds(String facilityId) {
		try {
			if (facilityId == null || facilityId.isEmpty()) {
				//ファシリティIDの指定が無いとき、すべてのファシリティIDが対象
				return null;
			}
	
			List<String> rtn = new RepositoryControllerBean().getFacilityIdList(facilityId, toRepositoryLevelCode());
	
			if (rtn != null && rtn.size() > 0) {
				// スコープの場合
				if (facilityId.equals(RoleSettingTreeConstant.ROOT_ID)) {
					//ルートスコープが選択されている場合、ファシリティＩＤ＝空白を追加
					//ファシリティＩＤ＝空白はジョブネットが起点の通知が該当する
					rtn.add("");
				}
			} else {
				// ノードの場合
				rtn = new ArrayList<String>();
				rtn.add(facilityId);
			}
	
			return rtn;

		} catch (HinemosUnknown e) {
			logger.warn("expandFacilityIds: ", e);
			return new ArrayList<>();
		}
	}

}
