/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BooleanTFConverter implements AttributeConverter<Boolean, Integer>{
    @Override
    public Integer convertToDatabaseColumn(Boolean value) {
        if (value != null && value) {
            return 1;
        } else {
            return 0;
        }
    }
    @Override
    public Boolean convertToEntityAttribute(Integer value) {
        return Integer.valueOf(1).equals(value);
    }
}
