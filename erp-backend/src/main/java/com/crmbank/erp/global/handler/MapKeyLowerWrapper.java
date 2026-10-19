package com.crmbank.erp.global.handler;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.reflection.wrapper.MapWrapper;

import java.util.Map;

/**
 * 💡 MyBatis 결과가 Map일 경우 키를 무조건 소문자로 처리하는 Wrapper
 */
public class MapKeyLowerWrapper extends MapWrapper {
    private final Map<String, Object> map;

    public MapKeyLowerWrapper(MetaObject metaObject, Map<String, Object> map) {
        super(metaObject, map);
        this.map = map;
    }

    @Override
    public Object get(PropertyTokenizer prop) {
        if (prop.getIndex() != null) {
            return super.get(prop);
        } else {
            // 💡 데이터를 읽을 때도 소문자 키로 조회
            return map.get(prop.getName().toLowerCase());
        }
    }

    @Override
    public void set(PropertyTokenizer prop, Object value) {
        if (prop.getIndex() != null) {
            super.set(prop, value);
        } else {
            String name = prop.getName();
            String key = (name == null) ? "" : name.toLowerCase();

            // 💡 이름이 없거나 이미 존재하는 키일 경우 무조건 새로운 숫자 인덱스(0, 1, 2...) 부여
            // 이를 통해 데이터 유실(Overwrite)을 원천 차단하고 순서를 보존함
            if (key.isEmpty() || map.containsKey(key)) {
                int i = 0;
                while (map.containsKey(String.valueOf(i))) {
                    i++;
                }
                key = String.valueOf(i);
            }
            map.put(key, value);
        }
    }

    @Override
    public boolean hasSetter(String name) {
        return true;
    }

    @Override
    public boolean hasGetter(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.getIndex() != null) {
            return super.hasGetter(name);
        } else {
            // 💡 존재 여부 확인 시에도 소문자로 체크
            return map.containsKey(prop.getName().toLowerCase());
        }
    }
}
