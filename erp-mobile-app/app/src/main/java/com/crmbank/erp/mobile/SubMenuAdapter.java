package com.crmbank.erp.mobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 🚀 프리미엄 아코디언(Expandable) 서브메뉴 어댑터
 */
public class SubMenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<MenuHeader> groupList;
    private final List<Object> flatList = new ArrayList<>();
    private final OnSubMenuClickListener listener;

    public interface OnSubMenuClickListener {
        void onProgramClick(Map<String, Object> program);
    }

    public SubMenuAdapter(List<MenuHeader> groupList, OnSubMenuClickListener listener) {
        this.groupList = groupList;
        this.listener = listener;
        generateFlatList();
    }

    public void updateData(List<MenuHeader> newList) {
        this.groupList = newList;
        generateFlatList();
        notifyDataSetChanged();
    }

    private void generateFlatList() {
        flatList.clear();
        if (groupList == null) return;
        
        for (MenuHeader header : groupList) {
            flatList.add(header); 
            if (header.isExpanded()) {
                flatList.addAll(header.getItems()); 
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (flatList.get(position) instanceof MenuHeader) ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu_group_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu_sub_item, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            MenuHeader header = (MenuHeader) flatList.get(position);
            HeaderViewHolder h = (HeaderViewHolder) holder;
            
            h.tvHeader.setText(header.getTitle());
            h.ivArrow.setRotation(header.isExpanded() ? 180 : 0);

            // 🚀 웹소스(SideMenu.vue)와 동일한 폴더 아이콘 적용 (bi-folder2-open 대응)
            h.ivHeaderIcon.setImageResource(android.R.drawable.ic_menu_slideshow); 
            h.ivHeaderIcon.setAlpha(0.6f);
            
            h.layoutHeaderCard.setOnClickListener(v -> {
                boolean targetState = !header.isExpanded();
                if (targetState) {
                    for (MenuHeader other : groupList) {
                        if (other != header) other.setExpanded(false);
                    }
                }
                header.setExpanded(targetState);
                generateFlatList();
                notifyDataSetChanged();
            });
        } else {
            Map<String, Object> program = (Map<String, Object>) flatList.get(position);
            ItemViewHolder h = (ItemViewHolder) holder;
            
            String name = getStringVal(program, "pgmnm");
            if (name.isEmpty()) name = getStringVal(program, "codenm");
            String pgmid = getStringVal(program, "pgmid");
            
            h.tvName.setText(name);

            // 🚀 웹소스(SideMenu.vue)와 동일한 아이콘 적용 (bi-chevron-right 대응)
            h.ivItemIcon.setImageResource(android.R.drawable.ic_media_next);
            h.ivItemIcon.setAlpha(0.3f);
            h.ivItemIcon.setPadding(4, 4, 4, 4);

            h.layoutItemContainer.setOnClickListener(v -> listener.onProgramClick(program));
        }
    }

    @Override
    public int getItemCount() {
        return flatList.size();
    }

    private String getStringVal(Map<String, Object> map, String key) {
        if (map == null) return "";
        Object val = map.get(key.toLowerCase());
        if (val == null) val = map.get(key.toUpperCase());
        return val != null ? String.valueOf(val).trim() : "";
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;
        ImageView ivArrow, ivHeaderIcon;
        View layoutHeaderCard;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tvGroupHeader);
            ivArrow = itemView.findViewById(R.id.ivArrow);
            ivHeaderIcon = itemView.findViewById(R.id.ivHeaderIcon);
            layoutHeaderCard = itemView.findViewById(R.id.layoutHeaderCard);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView ivItemIcon;
        View layoutItemContainer;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProgramName);
            ivItemIcon = itemView.findViewById(R.id.ivItemIcon);
            layoutItemContainer = itemView.findViewById(R.id.layoutItemContainer);
        }
    }
}
