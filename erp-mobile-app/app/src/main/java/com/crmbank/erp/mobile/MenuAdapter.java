package com.crmbank.erp.mobile;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private final List<Map<String, Object>> menuList;
    private final OnMenuClickListener listener;

    public interface OnMenuClickListener {
        void onMenuClick(Map<String, Object> menu);
    }

    public MenuAdapter(List<Map<String, Object>> menuList, OnMenuClickListener listener) {
        this.menuList = menuList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu_sub_item, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        Map<String, Object> menu = menuList.get(position);
        String name = (String) menu.get("menuNm");
        String code = String.valueOf(menu.get("codecd"));
        holder.tvMenuName.setText(name);
        
        // 🚀 1. 기본 설정 (Default)
        int iconRes = android.R.drawable.ic_menu_agenda; 
        int color = Color.parseColor("#64748B"); // 세련된 Slate Gray

        // 🚀 2. 핵심 4개 영역 전용 디자인 (우선 적용)
        if ("020".equals(code)) { // 구매
            iconRes = android.R.drawable.ic_menu_agenda;
            color = Color.parseColor("#38A169"); // Green
        } else if ("030".equals(code)) { // 영업
            iconRes = android.R.drawable.ic_menu_myplaces;
            color = Color.parseColor("#3182CE"); // Blue
        } else if ("080".equals(code)) { // SFA 영업활동관리
            iconRes = android.R.drawable.ic_menu_edit;
            color = Color.parseColor("#D69E2E"); // Amber
        } else if ("040".equals(code)) { // 관리정보
            iconRes = android.R.drawable.ic_menu_info_details;
            color = Color.parseColor("#805AD5"); // Purple
        } 
        // 🚀 3. 키워드 기반 보조 매핑 (인사, 회계 등)
        else if (name.contains("인사") || name.contains("HR")) {
            iconRes = android.R.drawable.ic_menu_view;
        } else if (name.contains("회계") || name.contains("Account")) {
            iconRes = android.R.drawable.ic_menu_save;
        }

        holder.ivMenuIcon.setImageResource(iconRes);
        holder.ivMenuIcon.setColorFilter(color);
        
        holder.layoutItemContainer.setOnClickListener(v -> listener.onMenuClick(menu));
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    static class MenuViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMenuIcon;
        TextView tvMenuName;
        View layoutItemContainer;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMenuIcon = itemView.findViewById(R.id.ivItemIcon);
            tvMenuName = itemView.findViewById(R.id.tvProgramName);
            layoutItemContainer = itemView.findViewById(R.id.layoutItemContainer);
        }
    }
}
