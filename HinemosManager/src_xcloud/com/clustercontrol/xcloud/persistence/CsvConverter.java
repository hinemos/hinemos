/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CsvConverter implements AttributeConverter<List<String>, String>{
	@Override
	public String convertToDatabaseColumn(List<String> csv) {
		StringBuilder s = new StringBuilder();
		for(int i = 0; i < csv.size(); ++i){
			if (i != 0)
				s.append(',');
			s.append(csv.get(i));
		}
		String value = s.toString();
		return value.isEmpty() ? null: value;
	}
	@Override
	public List<String> convertToEntityAttribute(String value) {
		if (value == null)
			return Collections.emptyList();
		
		List<String> values = new ArrayList<>();
		for (String v: value.split(",")) {
			if (!v.isEmpty())
				values.add(v.trim());
		}
		return values;
	}
}
