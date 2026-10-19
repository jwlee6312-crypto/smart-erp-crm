package com.crmbank.erp.mobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * 🚀 [OutboundRegisterAdapter] 모바일 바코드 출고 리스트 전용 Readonly 어댑터
 * 1. 의뢰수량(tvOrderQty): 절대 수정 불가능 Readonly
 * 2. 스캔수량(etInboundQty): 바코드 스캔 시에만 증가되는 Readonly
 */
public class OutboundRegisterAdapter extends BaseAdapter {

    private Context context;
    private List<Map<String, Object>> itemList;
    private LayoutInflater inflater;

    public OutboundRegisterAdapter(Context context, List<Map<String, Object>> itemList) {
        this.context = context;
        this.itemList = itemList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return itemList != null ? itemList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return itemList != null ? itemList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_outbound_register, parent, false);
            holder = new ViewHolder();
            holder.tvItemCode = convertView.findViewById(R.id.tvItemCode);
            holder.tvItemName = convertView.findViewById(R.id.tvItemName);
            holder.tvAutoYnBadge = convertView.findViewById(R.id.tvAutoYnBadge);
            holder.tvLotNo = convertView.findViewById(R.id.tvLotNo);
            holder.tvOrderQty = convertView.findViewById(R.id.tvOrderQty);
            holder.etInboundQty = convertView.findViewById(R.id.etInboundQty);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Map<String, Object> item = itemList.get(position);

        // 1. 품목코드 표시
        String cd = getStringValue(item, "ITEMCD");
        if (cd.isEmpty()) cd = getStringValue(item, "itemcd");
        holder.tvItemCode.setText(cd);
        
        // 2. 품명 표시
        String itemNm = getStringValue(item, "ITEMNM");
        if (itemNm.isEmpty()) itemNm = getStringValue(item, "itemnm");
        holder.tvItemName.setText(itemNm);

        // 3. autoyn == 'Y' 시리얼 필수 관리 품목 시각적 강조 배지 표시
        String autoYn = getStringValue(item, "autoyn");
        if (autoYn.isEmpty()) autoYn = getStringValue(item, "AUTOYN");
        if ("Y".equalsIgnoreCase(autoYn)) {
            if (holder.tvAutoYnBadge != null) {
                holder.tvAutoYnBadge.setVisibility(View.VISIBLE);
                holder.tvAutoYnBadge.setText("[스캔필수]");
            }
            holder.tvItemName.setTextColor(android.graphics.Color.parseColor("#E65100"));
        } else {
            if (holder.tvAutoYnBadge != null) {
                holder.tvAutoYnBadge.setVisibility(View.GONE);
            }
            holder.tvItemName.setTextColor(android.graphics.Color.parseColor("#333333"));
        }
        
        // 4. 의뢰수량 표시 (ioqty / balqty / qty) - 절대 수정 불가 Readonly
        String orderQty = getStringValue(item, "ioqty");
        if (orderQty.isEmpty()) orderQty = getStringValue(item, "balqty");
        if (orderQty.isEmpty()) orderQty = getStringValue(item, "qty");
        if (orderQty.isEmpty()) orderQty = "0";
        holder.tvOrderQty.setText(orderQty);

        // 5. 스캔수량 표시 (scan_qty - DB 스캔 기록과 1:1 매핑)
        Object scanQtyObj = item.get("scan_qty");
        if (scanQtyObj == null) scanQtyObj = item.get("SCAN_QTY");
        double scanQtyVal = 0.0;
        if (scanQtyObj != null) {
            try { scanQtyVal = Double.parseDouble(String.valueOf(scanQtyObj)); } catch (Exception ignored) {}
        }
        String scanQtyStr = scanQtyVal == (long) scanQtyVal ? String.format(java.util.Locale.getDefault(), "%d", (long) scanQtyVal) : String.valueOf(scanQtyVal);
        holder.etInboundQty.setText(scanQtyStr);

        // 6. 최근 스캔 LOT / 시리얼 번호 표시
        String lotNo = getStringValue(item, "lotno");
        if (lotNo.isEmpty()) lotNo = getStringValue(item, "LOTNO");
        if (lotNo.isEmpty()) lotNo = "-";
        if (holder.tvLotNo != null) holder.tvLotNo.setText(lotNo);

        return convertView;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        if (map == null || key == null) return "";
        Object val = map.get(key);
        if (val == null) val = map.get(key.toUpperCase(java.util.Locale.ROOT));
        if (val == null) val = map.get(key.toLowerCase(java.util.Locale.ROOT));
        if (val == null) return "";
        String str = String.valueOf(val).trim();
        if (str.endsWith(".0")) {
            str = str.substring(0, str.length() - 2);
        }
        return str;
    }

    static class ViewHolder {
        TextView tvItemCode, tvItemName, tvOrderQty, tvAutoYnBadge, tvLotNo, etInboundQty;
    }
}
