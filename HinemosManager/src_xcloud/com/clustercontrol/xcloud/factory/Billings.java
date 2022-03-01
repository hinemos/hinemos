/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.InternalManagerError;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.bean.BillingResult;
import com.clustercontrol.xcloud.bean.BillingResult.DataPoint;
import com.clustercontrol.xcloud.bean.BillingResult.FacilityBilling;
import com.clustercontrol.xcloud.bean.BillingResult.ResourceBilling;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.factory.monitors.BillingDetailMonitor;
import com.clustercontrol.xcloud.model.BillingDetailEntity;
import com.clustercontrol.xcloud.model.BillingDetailRelationEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity.OptionCallable;
import com.clustercontrol.xcloud.persistence.Transactional;
import com.clustercontrol.xcloud.util.CloudUtil;
import com.clustercontrol.xcloud.util.CsvUtil;
import com.clustercontrol.xcloud.util.RepositoryControllerBeanWrapper;


@Transactional
public class Billings implements IBillings {
	private static final Pattern PATTERN = Pattern.compile("^(\\d*\\.\\d*[1-9]|\\d*)0*$");

	public static class ZipByteDataSource implements DataSource {
		private String fileName;
		private byte[] zipArray;

		public ZipByteDataSource(String fileName, byte[] zipArray) {
			this.fileName = fileName;
			this.zipArray = zipArray;
		}
		
		@Override
		public InputStream getInputStream() throws IOException {
			return new ByteArrayInputStream(zipArray);
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public String getName() {
			return fileName.endsWith(".zip") ? fileName: fileName + ".zip";
		}
		
		@Override
		public String getContentType() {
//			return typeMap.getContentType(getName());
			return "application/zip";
		}
	};

	@SuppressWarnings("deprecation")
	@Transactional(Transactional.TransactionOption.Supported)
	@Override
	public BillingResult getBillingDetailsByCloudScope(String cloudScopeId, Integer year, Integer month) throws CloudManagerException, InvalidRole {
		HinemosEntityManager em = Session.current().getEntityManager();
		
		CloudScopeEntity entity = CloudManager.singleton().getCloudScopes().getCloudScopeByCurrentHinemosUser(cloudScopeId);
		
		TypedQuery<BillingDetailEntity> query = em.createNamedQuery(BillingDetailEntity.selectBillingDetailEntity, BillingDetailEntity.class);
		
		query.setParameter("cloudScopeId", cloudScopeId);
		
		BillingResult result = new BillingResult();
		entity.optionExecute((scope, option)-> {
			IBillingManagement.BillingPeriod ret = option.getBillingManagement(scope).getBillingPeriod(year, month);
			result.setBeginTime(ret.getBeginTime());
			result.setEndTime(ret.getEndTime());
			
			query.setParameter("start", ret.getBeginTime());
			query.setParameter("end", ret.getEndTime());
		});
		
		List<BillingDetailEntity> details = query.getResultList();
		
		result.setType(BillingResult.TargetType.CloudScope);
		result.setTargetId(cloudScopeId);
		
		result.setTargetName(entity.getName());
		result.setTargetYear(year);
		result.setTargetMonth(month);
		result.setFacilities(new ArrayList<FacilityBilling>());
		
		Map<String, Map<List<Integer>, DataPoint>> totalMap = new HashMap<>();
		Map<String, FacilityBilling> facilityMap = new HashMap<>();
		Map<String, Map<Object, ResourceBilling>> resourceMap = new HashMap<>();
		Map<ResourceBilling, Map<Object, DataPoint>> priceMap = new HashMap<>();
		
		for (BillingDetailEntity d: details) {
			BillingDetailRelationEntity relation = null;
			for (BillingDetailRelationEntity r: d.getBillingDetailRelations()) {
				if (BillingDetailRelationEntity.RelationType.node.equals(r.getRelationType())) {
					// とりあえず最初の一つ目。
					relation = r;
					break;
				}
			}
			String facilityId = null;
			String facilityName = null;
			if (relation != null) {
				facilityId = relation.getFacilityId();
				facilityName = relation.getFacilityName();
			} else {
				facilityId = "others";
			}
			
			FacilityBilling fb = facilityMap.get(facilityId);
			if (fb == null) {
				fb = new FacilityBilling();
				fb.setFacilityId(facilityId);
				fb.setFacilityName(facilityName);
				fb.setTotalsPerDate(new ArrayList<DataPoint>());
				fb.setResources(new ArrayList<ResourceBilling>());
				result.getFacilities().add(fb);
				facilityMap.put(facilityId, fb);
				resourceMap.put(facilityId, new HashMap<Object, ResourceBilling>());
				totalMap.put(facilityId, new HashMap<List<Integer>, DataPoint>());
			}
			
			Object key = Arrays.asList(cloudScopeId, d.getCategory(), d.getCategoryDetail(), d.getDisplayName(), d.getResourceId(), d.getUnit());
			ResourceBilling rb = resourceMap.get(facilityId).get(key);
			if (rb == null) {
				rb = new ResourceBilling();
				rb.setCloudScopeId(cloudScopeId);
				rb.setCloudScopeName(d.getCloudScopeName());
				rb.setCategory(d.getCategory());
				rb.setCategoryDetail(d.getCategoryDetail());
				rb.setDisplayName(d.getDisplayName());
				rb.setPrices(new ArrayList<DataPoint>());
				rb.setResourceId(d.getResourceId());
				rb.setUnit(d.getUnit());
				if (result.getUnit() == null)
					result.setUnit(d.getUnit());
				fb.getResources().add(rb);
				resourceMap.get(facilityId).put(key, rb);
				
				priceMap.put(rb, new HashMap<>());
			}
			
			LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(d.getTargetDate()), ZoneOffset.UTC);
			int costedMonth = time.getMonthValue();
			int costedDay = time.getDayOfMonth();
			
			DataPoint price = priceMap.get(rb).get(Arrays.asList(costedMonth, costedDay));
			if (price == null) {
				price = new DataPoint();
				price.setMonth(costedMonth);
				price.setDay(costedDay);
				priceMap.get(rb).put(Arrays.asList(costedMonth, costedDay), price);
				rb.getPrices().add(price);
			}
			price.setPrice(price.getPrice() + d.getCost());
			
			DataPoint total = totalMap.get(facilityId).get(Arrays.asList(costedMonth, costedDay));
			if (total == null) {
				total = new DataPoint();
				total.setMonth(costedMonth);
				total.setDay(costedDay);
				totalMap.get(facilityId).put(Arrays.asList(costedMonth, costedDay), total);
				fb.getTotalsPerDate().add(total);
			}
			total.setPrice(total.getPrice() + d.getCost());
		}
		
		sort(result);
		return result;
	}
	
	@SuppressWarnings("deprecation")
	@Transactional(Transactional.TransactionOption.Supported)
	@Override
	public BillingResult getBillingDetailsByFacility(String facilityId, Integer year, Integer month) throws CloudManagerException, InvalidRole {
		String facilityName = null;
		try {
			FacilityInfo entity = RepositoryControllerBeanWrapper.bean().getFacilityEntityByPK(facilityId);
			facilityName = entity.getFacilityName();
		}
		catch (FacilityNotFound | HinemosUnknown e) {
			throw ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e);
		}
		
		List<String> facilityIdList = new ArrayList<String>();
		// 与えられたfacilityID配下のノード・スコープを再帰的にリストアップする
		try {
			new Object() {
				public void getChildrenRecursive(String targetFacilityId, List<String> outputList) throws Exception {
					FacilityInfo targetEntity;
					targetEntity = RepositoryControllerBeanWrapper.bean().getFacilityEntityByPK(targetFacilityId);
					
					// スコープの場合は、さらに下位facilityを列挙し、再帰的に処理をする
					if (targetEntity.getFacilityType() == FacilityConstant.TYPE_SCOPE) {
						List<FacilityInfo> children = RepositoryControllerBeanWrapper.bean().getFacilityList(targetFacilityId);
						for (FacilityInfo child : children) {
							this.getChildrenRecursive(child.getFacilityId(), outputList);
						}
					}
					outputList.add(targetFacilityId);
				}
			}.getChildrenRecursive(facilityId, facilityIdList);
		} catch (Exception e) {
			throw ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e);
		}

		// 上でリストアップしたfacilityIdをターゲットとして課金詳細情報をリストアップする
		HinemosEntityManager em = Session.current().getEntityManager();
		LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
		LocalDateTime end = start.plusMonths(1);

		int start_idx = 0;
		int end_idx = 0;
		List<BillingDetailRelationEntity> relations = new ArrayList<>();
		while(start_idx < facilityIdList.size()){
			TypedQuery<BillingDetailRelationEntity> query = em.createNamedQuery(BillingDetailRelationEntity.selectBillingDetailRelationEntity, BillingDetailRelationEntity.class);

			// 月初。
			query.setParameter("start", start.toInstant(ZoneOffset.UTC).toEpochMilli());
			
			// 月末。
			query.setParameter("end", end.toInstant(ZoneOffset.UTC).toEpochMilli());
			
			end_idx = start_idx + CloudUtil.SQL_PARAM_NUMBER_THRESHOLD;
			if (end_idx > facilityIdList.size()){
				end_idx = facilityIdList.size();
			}
			
			List<String> subList = facilityIdList.subList(start_idx, end_idx);
			query.setParameter("facilityIds", subList);
			relations.addAll(query.getResultList());
			start_idx = end_idx;
		}

		BillingResult result = new BillingResult();
		result.setType(BillingResult.TargetType.Facility);
		result.setTargetId(facilityId);
		result.setTargetName(facilityName);
		result.setTargetYear(year);
		result.setTargetMonth(month);
		result.setBeginTime(start.toInstant(ZoneOffset.UTC).toEpochMilli());
		result.setEndTime(end.toInstant(ZoneOffset.UTC).toEpochMilli());
		result.setFacilities(new ArrayList<FacilityBilling>());
		
		Map<List<Integer>, DataPoint> totalMap = new HashMap<>();
		Map<List<Integer>, DataPoint> resourceMap = new HashMap<>();
		Map<List<String>, ResourceBilling> resourceBillingMap = new HashMap<>();
		FacilityBilling fb = null;
		ResourceBilling rb = null;
		for (BillingDetailRelationEntity r: relations) {
			if (fb == null || !r.getFacilityId().equals(fb.getFacilityId())) {
				fb = new FacilityBilling();
				fb.setFacilityId(r.getFacilityId());
				fb.setFacilityName(r.getFacilityName());
				fb.setTotalsPerDate(new ArrayList<DataPoint>());
				fb.setResources(new ArrayList<ResourceBilling>());
				result.getFacilities().add(fb);
				rb = null;
				totalMap.clear();
			}
			List<String> key = Arrays.asList(
					r.getBillingDetail().getCloudScopeId(),
					r.getBillingDetail().getCategory(),
					r.getBillingDetail().getResourceId(),
					r.getBillingDetail().getCategoryDetail(),
					r.getBillingDetail().getDisplayName(),
					r.getBillingDetail().getUnit());
			if (!resourceBillingMap.containsKey(key)) {
				rb = new ResourceBilling();
				rb.setCloudScopeId(r.getBillingDetail().getCloudScopeId());
				rb.setCloudScopeName(r.getBillingDetail().getCloudScopeName());
				rb.setCategory(r.getBillingDetail().getCategory());
				rb.setCategoryDetail(r.getBillingDetail().getCategoryDetail());
				rb.setDisplayName(r.getBillingDetail().getDisplayName());
				rb.setPrices(new ArrayList<DataPoint>());
				rb.setUnit(r.getBillingDetail().getUnit());
				if (result.getUnit() == null)
					result.setUnit(r.getBillingDetail().getUnit());
				rb.setResourceId(r.getBillingDetail().getResourceId());
				fb.getResources().add(rb);
				resourceMap.clear();
				resourceBillingMap.put(key, rb);
			}
			
			LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(r.getBillingDetail().getTargetDate()), ZoneOffset.UTC);
			
			int costedMonth = time.getMonthValue();
			int costedDay = time.getDayOfMonth();
			DataPoint price = resourceMap.get(Arrays.asList(costedMonth, costedDay));
			if (price == null) {
				price = new DataPoint();
				price.setMonth(costedMonth);
				price.setDay(costedDay);
				resourceMap.put(Arrays.asList(costedMonth, costedDay), price);
				rb.getPrices().add(price);
			}
			price.setPrice(price.getPrice() + r.getBillingDetail().getCost());
			
			DataPoint total = totalMap.get(Arrays.asList(costedMonth, costedDay));
			if (total == null) {
				total = new DataPoint();
				total.setMonth(costedMonth);
				total.setDay(costedDay);
				totalMap.put(Arrays.asList(costedMonth, costedDay), total);
				fb.getTotalsPerDate().add(total);
			}
			total.setPrice(total.getPrice() + r.getBillingDetail().getCost());
		}
		sort(result);
		return result;
	}
	
	@SuppressWarnings("serial")
	private final static Map<String, Integer> categoryPriority = new HashMap<String, Integer>(){{
			put("server", 0);
			put("device", 1);
			put("etc", Integer.MAX_VALUE);
		}};
	
	/**
	 * 課金詳細画面・CSVダウンロード用に課金情報をソートする関数。以下の順序でソートを行う
	 * 1. FacilityId
	 * 2. 各FacilityIdの中で
	 *   2.1. 課金区分
	 *   2.1. 課金区分が同じなら課金区分詳細
	 *   2.2. 課金区分詳細が同じならリソースID
	 *   2.3. リソースIDが同じなら表示名
	 */
	private static void sort(BillingResult result) {
		// 1. FacilityIdでソート
		Collections.sort(result.getFacilities(), new Comparator<FacilityBilling>() {
			@Override
			public int compare(FacilityBilling o1, FacilityBilling o2) {
				int result = o1.getFacilityId().compareTo(o2.getFacilityId());
				return result == 0 ? 0: (o1.getFacilityId().equals("others") ? 1: (o2.getFacilityId().equals("others") ? -1: result));
			}});
		for (FacilityBilling fb: result.getFacilities()) {
			// 2. 各FacilityIdごとに、課金区分でソート
			Collections.sort(fb.getResources(), new Comparator<ResourceBilling>() {
				@Override
				public int compare(ResourceBilling o1, ResourceBilling o2) {
					Integer p1 = categoryPriority.get(o1.getCategory());
					Integer p2 = categoryPriority.get(o2.getCategory());
					// 2.1. 課金区分
					int result = p1 == null ? (p2 == null ? o1.getCategory().compareTo(o2.getCategory()): p2 == Integer.MAX_VALUE ? -1: 1): (p2 == null ? (p1 == Integer.MAX_VALUE ? 1: -1): p1 - p2);
					// 2.2. 同じなら区分詳細
					if (o1.getCategoryDetail() == null || o2.getCategoryDetail() == null) {
						result = -1;
					} else {
						result = result == 0 ? 
								(o1.getCategoryDetail().equals(o2.getCategoryDetail()) ? 0: o1.getCategoryDetail() != null ? o1.getCategoryDetail().compareTo(o2.getCategoryDetail()): -1): result;
					}
					// 2.3. 同じならリソースID
					result = result == 0 ? (o1.getResourceId().compareTo(o2.getResourceId())) : result;
					// 2.4. 同じなら表示名
					return result == 0 ? o1.getDisplayName().compareTo(o2.getDisplayName()): result;
				}});

			for (ResourceBilling rb: fb.getResources()) {
				Collections.sort(rb.getPrices(), new Comparator<DataPoint>() {
					@Override
					public int compare(DataPoint o1, DataPoint o2) {
						return Integer.compare(o1.getDay(), o2.getDay());
					}});
			}
			Collections.sort(fb.getTotalsPerDate(), new Comparator<DataPoint>() {
				@Override
				public int compare(DataPoint o1, DataPoint o2) {
					return Integer.compare(o1.getDay(), o2.getDay());
				}});
		}
	}
	
	@Transactional(Transactional.TransactionOption.Supported)
	@Override
	public DataHandler downloadBillingDetailsByCloudScope(String cloudScopeId, Integer year, Integer month) throws CloudManagerException, InvalidRole {
		String csvName = String.format("hinemos_cloud_billing_detail_account_%s_%04d%02d.csv", cloudScopeId, year, month);
		return createDataHandler(csvName, getBillingDetailsByCloudScope(cloudScopeId, year, month));
	}

	@Transactional(Transactional.TransactionOption.Supported)
	@Override
	public DataHandler downloadBillingDetailsByFacility(String facilityId, Integer year, Integer month) throws CloudManagerException, InvalidRole {
		String csvName = String.format("hinemos_cloud_billing_detail_facility_%s_%04d%02d.csv", facilityId, year, month);
		return createDataHandler(csvName, getBillingDetailsByFacility(facilityId, year, month));
	}

	private static DataHandler createDataHandler(String fileName, BillingResult billingResult) throws CloudManagerException {
		try {
			ByteArrayOutputStream zipArray = new ByteArrayOutputStream();
			ZipOutputStream zipOutput = new ZipOutputStream(zipArray);
			zipOutput.putNextEntry(new ZipEntry(fileName));
			try (PrintWriter csvWriter = new PrintWriter(zipOutput)) {
				LocalDateTime beginDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(billingResult.getBeginTime()), ZoneOffset.UTC);
				LocalDateTime endDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(billingResult.getEndTime()), ZoneOffset.UTC);
				
				// カラム名を書き込み。
				List<String> columnNames = new ArrayList<>(Arrays.asList("facilityId", "facilityName", "cloudScopeId", "resourceId", "category", "displayName", "unit"));
				LocalDateTime previous = null;
				for (LocalDateTime dateTime = beginDateTime; dateTime.isBefore(endDateTime); dateTime = dateTime.plusDays(1)) {
					// 最初の日と月が変わったら、日付に月を付与する。
					if (previous == null || previous.getMonthValue() != dateTime.getMonthValue()) {
						columnNames.add(Integer.toString(dateTime.getMonthValue()) + "/" + Integer.toString(dateTime.getDayOfMonth()));
					} else {
						columnNames.add(Integer.toString(dateTime.getDayOfMonth()));
					}
					
					previous = dateTime;
				}
				
				CsvUtil.writeCsvLine(csvWriter, columnNames.toArray(new String[columnNames.size()]));
				for (FacilityBilling fb: billingResult.getFacilities()) {
					List<String> totalValues = new ArrayList<>(Arrays.asList(fb.getFacilityId(), fb.getFacilityName(), "", "", "", "", ""));
					for (LocalDateTime dateTime = beginDateTime; dateTime.isBefore(endDateTime); dateTime = dateTime.plusDays(1)) {
						DataPoint point = null;
						int month = dateTime.getMonthValue();
						int day = dateTime.getDayOfMonth();
						for (DataPoint p: fb.getTotalsPerDate()) {
							if (month == p.getMonth() && day == p.getDay()) {
								point = p;
								break;
							}
						}
						if (point != null) {
							// とりあえず、AWS の 課金の csv が、8 桁になっています。
							String value = new BigDecimal(point.getPrice()).setScale(8, BigDecimal.ROUND_DOWN).toPlainString();
							Matcher m = PATTERN.matcher(value);
							totalValues.add(m.matches() ? m.group(1): ("0.00000000".equals(value) ? "0": value));
						}
						else {
							totalValues.add("-");
						}
					}
					CsvUtil.writeCsvLine(csvWriter, totalValues.toArray(new String[totalValues.size()]));
					
					for (ResourceBilling rb: fb.getResources()) {
						List<String> resourceValues = new ArrayList<>(Arrays.asList("", "", rb.getCloudScopeId(), rb.getResourceId(), rb.getCategoryDetail() == null ? rb.getCategory(): rb.getCategory() + "(" + rb.getCategoryDetail() + ")", rb.getDisplayName(), rb.getUnit()));
						for (LocalDateTime dateTime = beginDateTime; dateTime.isBefore(endDateTime); dateTime = dateTime.plusDays(1)) {
							DataPoint point = null;
							int month = dateTime.getMonthValue();
							int day = dateTime.getDayOfMonth();
							for (DataPoint p: rb.getPrices()) {
								if (month == p.getMonth() && day == p.getDay()) {
									point = p;
									break;
								}
							}
							if (point != null) {
								// とりあえず、AWS の 課金の csv が、8 桁になっています。
								String value = new BigDecimal(point.getPrice()).setScale(8, BigDecimal.ROUND_DOWN).toPlainString();
								Matcher m = PATTERN.matcher(value);
								resourceValues.add(m.matches() ? m.group(1): ("0.00000000".equals(value) ? "0": value));
							}
							else {
								resourceValues.add("-");
							}
						}
						CsvUtil.writeCsvLine(csvWriter, resourceValues.toArray(new String[resourceValues.size()]));
					}
				}
				csvWriter.flush();
			}
			return new DataHandler(new ZipByteDataSource(fileName + ".zip", zipArray.toByteArray()));
		}
		catch (IOException e) {
			throw new InternalManagerError(e.getMessage(), e);
		}
	}

	@Override
	public String writeBillingDetailsByCloudScope(String cloudScopeId, Integer year, Integer month, File tempFile) throws CloudManagerException, InvalidRole {
		String csvName = String.format("hinemos_cloud_billing_detail_account_%s_%04d%02d.csv", cloudScopeId, year, month);
		return writeBillingDetails(tempFile, csvName, getBillingDetailsByCloudScope(cloudScopeId, year, month));
	}

	@Override
	public String writeBillingDetailsByFacility(String facilityId, Integer year, Integer month, File tempFile) throws CloudManagerException, InvalidRole {
		String csvName = String.format("hinemos_cloud_billing_detail_facility_%s_%04d%02d.csv", facilityId, year, month);
		return writeBillingDetails(tempFile, csvName, getBillingDetailsByFacility(facilityId, year, month));
	}

	/**
	 * 課金詳細情報をCSVに書き込み、一時ファイルでzipファイルを作成する。<BR>
	 * 成功したらzipファイル名を返す。
	 * 
	 * @param tempFile
	 *            一時ファイル
	 * @param csvName
	 *            CSVファイル名
	 * @param billingResult
	 *            課金詳細情報
	 * @return zipファイル名
	 * @throws CloudManagerException
	 */
	private String writeBillingDetails(File tempFile, String csvName, BillingResult billingResult) throws CloudManagerException {
		try {
			ZipOutputStream zipOutput = new ZipOutputStream(new FileOutputStream(tempFile));
			zipOutput.putNextEntry(new ZipEntry(csvName));
			try (PrintWriter csvWriter = new PrintWriter(zipOutput)) {
				LocalDateTime beginDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(billingResult.getBeginTime()), ZoneOffset.UTC);
				LocalDateTime endDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(billingResult.getEndTime()), ZoneOffset.UTC);

				// カラム名を書き込み。
				List<String> columnNames = new ArrayList<>(Arrays.asList("facilityId", "facilityName", "cloudScopeId", "resourceId", "category", "displayName", "unit"));
				LocalDateTime previous = null;
				for (LocalDateTime dateTime = beginDateTime; dateTime.isBefore(endDateTime); dateTime = dateTime.plusDays(1)) {
					// 最初の日と月が変わったら、日付に月を付与する。
					if (previous == null || previous.getMonthValue() != dateTime.getMonthValue()) {
						columnNames.add(Integer.toString(dateTime.getMonthValue()) + "/" + Integer.toString(dateTime.getDayOfMonth()));
					} else {
						columnNames.add(Integer.toString(dateTime.getDayOfMonth()));
					}

					previous = dateTime;
				}

				CsvUtil.writeCsvLine(csvWriter, columnNames.toArray(new String[columnNames.size()]));
				for (FacilityBilling fb : billingResult.getFacilities()) {
					List<String> totalValues = new ArrayList<>(Arrays.asList(fb.getFacilityId(), fb.getFacilityName(), "", "", "", "", ""));
					for (LocalDateTime dateTime = beginDateTime; dateTime.isBefore(endDateTime); dateTime = dateTime.plusDays(1)) {
						DataPoint point = null;
						int month = dateTime.getMonthValue();
						int day = dateTime.getDayOfMonth();
						for (DataPoint p : fb.getTotalsPerDate()) {
							if (month == p.getMonth() && day == p.getDay()) {
								point = p;
								break;
							}
						}
						if (point != null) {
							// とりあえず、AWS の 課金の csv が、8 桁になっています。
							String value = new BigDecimal(point.getPrice()).setScale(8, BigDecimal.ROUND_DOWN).toPlainString();
							Matcher m = PATTERN.matcher(value);
							totalValues.add(m.matches() ? m.group(1) : ("0.00000000".equals(value) ? "0" : value));
						} else {
							totalValues.add("-");
						}
					}
					CsvUtil.writeCsvLine(csvWriter, totalValues.toArray(new String[totalValues.size()]));

					for (ResourceBilling rb : fb.getResources()) {
						List<String> resourceValues = new ArrayList<>(Arrays.asList("", "", rb.getCloudScopeId(), rb.getResourceId(),
								rb.getCategoryDetail() == null ? rb.getCategory() : rb.getCategory() + "(" + rb.getCategoryDetail() + ")", rb.getDisplayName(),
								rb.getUnit()));
						for (LocalDateTime dateTime = beginDateTime; dateTime.isBefore(endDateTime); dateTime = dateTime.plusDays(1)) {
							DataPoint point = null;
							int month = dateTime.getMonthValue();
							int day = dateTime.getDayOfMonth();
							for (DataPoint p : rb.getPrices()) {
								if (month == p.getMonth() && day == p.getDay()) {
									point = p;
									break;
								}
							}
							if (point != null) {
								// とりあえず、AWS の 課金の csv が、8 桁になっています。
								String value = new BigDecimal(point.getPrice()).setScale(8, BigDecimal.ROUND_DOWN).toPlainString();
								Matcher m = PATTERN.matcher(value);
								resourceValues.add(m.matches() ? m.group(1) : ("0.00000000".equals(value) ? "0" : value));
							} else {
								resourceValues.add("-");
							}
						}
						CsvUtil.writeCsvLine(csvWriter, resourceValues.toArray(new String[resourceValues.size()]));
					}
				}
				csvWriter.flush();
			} finally {
				zipOutput.close();
			}
			return csvName + ".zip";
		} catch (IOException e) {
			throw new InternalManagerError(e.getMessage(), e);
		}
	}
	
	@Override
	public List<String> getPlatformServices(String cloudScopeId) throws CloudManagerException, InvalidRole {
		CloudScopeEntity cloudScope = CloudManager.singleton().getCloudScopes().getCloudScope(cloudScopeId);
		return cloudScope.optionCall(new OptionCallable<List<String>>() {
			@Override
			public List<String> call(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				return option.getBillingManagement(scope).getPlatformServices();
			}
		});
	}

	@Override
	public PlatformServiceBilling getPlatformServiceBilling(CloudScopeEntity cloudScope, final String service) throws CloudManagerException {
		return cloudScope.optionCall(new OptionCallable<PlatformServiceBilling>() {
			@Override
			public PlatformServiceBilling call(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				IBillingManagement bm = option.getBillingManagement(scope);
				return bm.getPlatformServiceBilling(service);
			}
		});
	}

	@Override
	public void refreshBillingDetails(String cloudScopeId) throws CloudManagerException, InvalidRole {
		HinemosEntityManager em = Session.current().getEntityManager();
		CloudScopeEntity cloudScope = CloudManager.singleton().getCloudScopes().getCloudScope(cloudScopeId);
		
		Query query = em.createNamedQuery(BillingDetailEntity.deleteBillingDetailEntity);
		query.setParameter("cloudScopeId", cloudScopeId);
		query.executeUpdate();
		
		cloudScope.setBillingLastDate(null);
		
		try {
			BillingDetailMonitor.CollectBillingJob.collectBillingDetails(cloudScopeId);
		} catch (Exception e) {
			throw new CloudManagerException(e);
		}
	}

}
