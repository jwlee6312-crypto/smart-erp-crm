package com.crmbank.erp.mobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class InboundConfirmAdapter extends BaseAdapter {

    private Context context;
    private List<Map<String, Object>> itemList;
    private LayoutInflater inflater;
    private DecimalFormat decimalFormat = new DecimalFormat("#,###");

    public InboundConfirmAdapter(Context context, List<Map<String, Object>> itemList) {
        this.context = context;
        this.itemList = itemList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_inbound_confirm, parent, false);
            holder = new ViewHolder();
            holder.cbSelect = convertView.findViewById(R.id.cbSelect);
            holder.tvIoYmd = convertView.findViewById(R.id.tvIoYmd);
            holder.tvCustNm = convertView.findViewById(R.id.tvCustNm);
            holder.tvTotAmt = convertView.findViewById(R.id.tvTotAmt);
            holder.tvWhNm = convertView.findViewById(R.id.tvWhNm);
            holder.cbConfirmYn = convertView.findViewById(R.id.cbConfirmYn);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Map<String, Object> item = itemList.get(position);

        // ?곗씠??諛붿씤??(sumamt ?꾨뱶 ?ъ슜)
        holder.tvIoYmd.setText(getString(item, "ioymd"));
        holder.tvCustNm.setText(getString(item, "custnm"));
        
        // 湲덉븸 ?쒖떆 (sumamt ?먮뒗 SUMAMT)
        Object amtObj = item.get("sumamt");
        if (amtObj == null) amtObj = item.get("SUMAMT");
        if (amtObj != null) {
            try {
                double amt = Double.parseDouble(String.valueOf(amtObj));
                holder.tvTotAmt.setText(decimalFormat.format(amt));
            } catch (Exception e) {
                holder.tvTotAmt.setText(amtObj.toString());
            }
        } else {
            holder.tvTotAmt.setText("0");
        }

        holder.tvWhNm.setText(getString(item, "whnm"));

        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbConfirmYn.setOnCheckedChangeListener(null);

        boolean isSelected = "Y".equals(String.valueOf(item.get("selected")));
        boolean isConfirmed = "Y".equals(getString(item, "cfmyn"));

        holder.cbSelect.setChecked(isSelected);
        holder.cbConfirmYn.setChecked(isConfirmed);

        holder.cbConfirmYn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.put("cfmyn", isChecked ? "Y" : "N");
            if (isChecked) {
                item.put("selected", "Y");
                holder.cbSelect.setChecked(true);
            }
        });

        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.put("selected", isChecked ? "Y" : "N");
        });

        return convertView;
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) val = map.get(key.toUpperCase());
        return val != null ? val.toString() : "";
    }

    static class ViewHolder {
        CheckBox cbSelect, cbConfirmYn;
        TextView tvIoYmd, tvCustNm, tvTotAmt, tvWhNm;
    }
}
