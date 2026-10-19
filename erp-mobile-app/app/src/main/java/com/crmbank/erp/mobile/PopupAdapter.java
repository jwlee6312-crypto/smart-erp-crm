package com.crmbank.erp.mobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 🚀 대량 데이터 고속 처리를 위한 고성능 팝업 어댑터 (네비/가상화/키 매칭 지원)
 */
public class PopupAdapter extends RecyclerView.Adapter<PopupAdapter.ViewHolder> {

    private final List<Map<String, Object>> items;
    private final String type; // "CUST", "ITEM", "DEPT"
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Map<String, Object> item);
    }

    public PopupAdapter(List<Map<String, Object>> items, String type, OnItemClickListener listener) {
        this.items = items;
        this.type = type;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_popup_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> item = items.get(position);
        
        String title = "";
        String code = "";
        String extra = "";
        
        if ("CUST".equalsIgnoreCase(type) || "DEPT".equalsIgnoreCase(type)) {
            title = getStringVal(item, "custnm");
            if (title.isEmpty()) title = getStringVal(item, "deptnm");
            if (title.isEmpty()) title = getStringVal(item, "cdnm");
            if (title.isEmpty()) title = getStringVal(item, "codenm");

            code = getStringVal(item, "custcd");
            if (code.isEmpty()) code = getStringVal(item, "deptcd");
            if (code.isEmpty()) code = getStringVal(item, "codecd");
            if (code.isEmpty()) code = getStringVal(item, "code");
        } else {
            title = getStringVal(item, "itemnm");
            if (title.isEmpty()) title = getStringVal(item, "cdnm");
            if (title.isEmpty()) title = getStringVal(item, "codenm");

            code = getStringVal(item, "itemcd");
            if (code.isEmpty()) code = getStringVal(item, "codecd");
            if (code.isEmpty()) code = getStringVal(item, "code");

            extra = getStringVal(item, "itsize");
            String unit = getStringVal(item, "unit");
            if (unit.isEmpty()) unit = getStringVal(item, "munit");
            if (!extra.isEmpty() && !unit.isEmpty()) {
                extra = extra + " / " + unit;
            } else if (extra.isEmpty()) {
                extra = unit;
            }
        }
        
        if (!title.isEmpty() && !code.isEmpty()) {
            if (!extra.isEmpty()) {
                holder.tvText.setText(String.format("%s (%s) [%s]", title, code, extra));
            } else {
                holder.tvText.setText(String.format("%s (%s)", title, code));
            }
        } else if (!title.isEmpty()) {
            holder.tvText.setText(title);
        } else if (!code.isEmpty()) {
            holder.tvText.setText(code);
        } else {
            holder.tvText.setText("-");
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    private String getStringVal(Map<String, Object> map, String key) {
        if (map == null || key == null) return "";

        // 1. Exact key match
        if (map.containsKey(key) && map.get(key) != null) {
            return String.valueOf(map.get(key)).trim();
        }

        // 2. Lowercase key match
        String lowerKey = key.toLowerCase(Locale.ROOT);
        if (map.containsKey(lowerKey) && map.get(lowerKey) != null) {
            return String.valueOf(map.get(lowerKey)).trim();
        }

        // 3. Uppercase key match
        String upperKey = key.toUpperCase(Locale.ROOT);
        if (map.containsKey(upperKey) && map.get(upperKey) != null) {
            return String.valueOf(map.get(upperKey)).trim();
        }

        // 4. Case-insensitive iteration fallback
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue() != null ? String.valueOf(entry.getValue()).trim() : "";
            }
        }

        return "";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvText;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(android.R.id.text1);
        }
    }
}
