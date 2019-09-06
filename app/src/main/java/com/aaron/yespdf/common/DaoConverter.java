package com.aaron.yespdf.common;

import org.greenrobot.greendao.converter.PropertyConverter;

import java.util.Arrays;
import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public final class DaoConverter implements PropertyConverter<List<String>, String> {

    @Override
    public List<String> convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }
        return Arrays.asList(databaseValue.split(","));
    }

    @Override
    public String convertToDatabaseValue(List<String> entityProperty) {
        if (entityProperty == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String json : entityProperty) {
            sb.append(json).append(",");
        }
        return sb.substring(0, sb.length() - 1);
    }
}
