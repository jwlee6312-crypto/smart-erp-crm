package com.crmbank.erp.global.handler;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.wrapper.ObjectWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;

import java.util.Map;

/**
 * 💡 MyBatis 결과가 Map일 경우 키를 무조건 소문자로 처리하는 Wrapper를 생성하는 Factory
 */
public class MapKeyLowerWrapperFactory implements ObjectWrapperFactory {

    @Override
    public boolean hasWrapperFor(Object object) {
        return object instanceof Map;
    }

    @Override
    public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
        return new MapKeyLowerWrapper(metaObject, (Map<String, Object>) object);
    }
}
