package com.crmbank.erp.mobile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 🚀 서브메뉴 그룹화를 위한 데이터 클래스
 */
public class MenuHeader {
    private String title;
    private List<Map<String, Object>> items;
    private boolean isExpanded;

    public MenuHeader(String title) {
        this.title = title;
        this.items = new ArrayList<>();
        this.isExpanded = false; // 기본적으로 접혀 있도록 설정
    }

    public String getTitle() { return title; }
    public List<Map<String, Object>> getItems() { return items; }
    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) { isExpanded = expanded; }
    public void toggle() { isExpanded = !isExpanded; }
}
