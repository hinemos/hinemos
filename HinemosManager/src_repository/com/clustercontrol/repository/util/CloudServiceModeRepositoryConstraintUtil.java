/*
 * Copyright (c) 2024 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CloudServiceModeRepositoryConstraintUtil {
	
	/**
	 * 1つの制限項目を表すクラス。例: ホスト名、IPv4、IPv6
	 */
	private static class ConstraintItem {
		private final List<Pattern> patterns;
		
		public static ConstraintItem createFromProperties(Properties prop, String keyPrefix) {
			Pattern keyPattern = Pattern.compile(keyPrefix + "\\.[0-9]+");

			List<Pattern> patterns = prop.keySet().stream()
				.map(k -> (String)k)
				.filter(k -> keyPattern.matcher(k).matches())
				.sorted()  // 利用者はキーの番号順にフィルタされることを想定しそうなため、念のためキーでソートしておく
				.map(k -> Pattern.compile((String)prop.get(k)))
				.collect(Collectors.toList());

			m_log.info(String.format("Loaded %d constraints for \"%s\"", patterns.size(), keyPrefix));
			
			return new ConstraintItem(patterns);
		}
		
		public boolean isAllowed(String value) {
			if (patterns.isEmpty()) {
				return true;
			}

			return patterns.stream()
					.map(p -> p.matcher(value))
					.allMatch(m -> !m.matches());  // 全てのパターンにマッチしなければ、登録を許可された値である
		}
		
		private ConstraintItem(List<Pattern> patterns) {
			this.patterns = patterns;
		}
	}

	/**
	 * 複数の制限項目を保持し、プロパティから読み込むクラス。
	 */
	private static class ConstraintProperties {
		private final ConstraintItem nodenameConstraints;
		private final ConstraintItem ipv4Constraints;
		private final ConstraintItem ipv6Constraints;

		public static ConstraintProperties load(Path propertiesFilepath) throws IOException {
			Properties prop = new Properties();
			
			if (propertiesFilepath.toFile().exists()) {
				prop.load(Files.newBufferedReader(propertiesFilepath));
			} else {
				// プロパティファイルが存在しなければ、
				// 空のプロパティファイルを読み込んだときと等価とする
			}
			
			return new ConstraintProperties(
					ConstraintItem.createFromProperties(prop, "nodename"),
					ConstraintItem.createFromProperties(prop, "ipv4"),
					ConstraintItem.createFromProperties(prop, "ipv6"));
		}
		
		private ConstraintProperties(
				ConstraintItem nodenameConstraints,
				ConstraintItem ipv4Constraints,
				ConstraintItem ipv6Constraints) {

			this.nodenameConstraints = nodenameConstraints;
			this.ipv4Constraints = ipv4Constraints;
			this.ipv6Constraints = ipv6Constraints;
		}
		
	}

	private static final String propertiesFilename = "cloudservicemode_repository_constraint.properties";
	private static final Log m_log = LogFactory.getLog(CloudServiceModeRepositoryConstraintUtil.class);

	public boolean isAllowedNodename(String value) throws IOException {
		return getProps().nodenameConstraints.isAllowed(value);
	}

	public boolean isAllowedIpv4(String value) throws IOException {
		return getProps().ipv4Constraints.isAllowed(value);
	}

	public boolean isAllowedIpv6(String value) throws IOException {
		return getProps().ipv6Constraints.isAllowed(value);
	}
	
	private ConstraintProperties props = null;

	private ConstraintProperties getProps() throws IOException {
		if (props == null) {
			// 初めて必要になったときにプロパティファイルを読み込む
			String etcdir = System.getProperty("hinemos.manager.etc.dir");
			props = ConstraintProperties.load(Paths.get(etcdir, propertiesFilename));
		}
		return props;
	}
}

