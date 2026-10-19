package com.crmbank.erp.mobile.hsba;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.crmbank.erp.mobile.ApiResponse;
import com.crmbank.erp.mobile.ApiService;
import com.crmbank.erp.mobile.BaseActivity;
import com.crmbank.erp.mobile.CodeDto;
import com.crmbank.erp.mobile.R;
import com.crmbank.erp.mobile.RetrofitClient;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 🚀 [MHSBA010U] 모바일 품목 등록/관리
 * 웹 HSBA010U.vue 기반 100% 매칭
 * 1. 재고자산 & 단위 공통코드 로드 (E0/100, E0/200)
 * 2. 품목 목록 조회 (HSBA_010U_STR 'S0')
 * 3. 품목 신규/수정 저장 (HSBA_010U_STR 'A0'/'U0')
 */
public class MHSBA010U extends BaseActivity {

    private Spinner spSearchAstKind, spAstKind, spUnit;
    private EditText etSearchItemNm, etItemCd, etItemNm, etItSize, etImPrice, etOmPrice, etBarcode, etRemark;
    private ListView lvItemList;
    private ItemAdapter itemAdapter;

    private final List<Map<String, Object>> itemList = new ArrayList<>();
    private final List<CodeDto> assetList = new ArrayList<>();
    private final List<CodeDto> unitList = new ArrayList<>();
    
    private ApiService apiService;
    private String cmpycd = "COIT";
    private String userid = "";
    private String actkind = "A0"; // A0: 신규, U0: 수정

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mhsba010u);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        cmpycd = prefs.getString("cmpycd", "COIT").trim();
        userid = prefs.getString("userId", "");

        apiService = RetrofitClient.getApiService();

        initViews();
        loadOptions();
    }

    private void initViews() {
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        if (tvHeaderTitle != null) tvHeaderTitle.setText("품목 등록 (MHSBA010U)");

        spSearchAstKind = findViewById(R.id.spSearchAstKind);
        spAstKind = findViewById(R.id.spAstKind);
        spUnit = findViewById(R.id.spUnit);

        etSearchItemNm = findViewById(R.id.etSearchItemNm);
        etItemCd = findViewById(R.id.etItemCd);
        etItemNm = findViewById(R.id.etItemNm);
        etItSize = findViewById(R.id.etItSize);
        etImPrice = findViewById(R.id.etImPrice);
        etOmPrice = findViewById(R.id.etOmPrice);
        etBarcode = findViewById(R.id.etBarcode);
        etRemark = findViewById(R.id.etRemark);

        lvItemList = findViewById(R.id.lvItemList);
        itemAdapter = new ItemAdapter();
        lvItemList.setAdapter(itemAdapter);

        findViewById(R.id.btnReset).setOnClickListener(v -> initializeForm());
        findViewById(R.id.btnSearch).setOnClickListener(v -> search());
        findViewById(R.id.btnSave).setOnClickListener(v -> save());

        lvItemList.setOnItemClickListener((parent, view, position, id) -> {
            Map<String, Object> selected = itemList.get(position);
            populateDetail(selected);
        });
    }

    private void loadOptions() {
        // 1. 재고자산 옵션 로드 (HS00_000S_STR gubun: E0, gbncd: 100 - Web HSBA010U.vue 100% 동일)
        Map<String, Object> pAst = new HashMap<>();
        pAst.put("gubun", "E0");
        pAst.put("gbncd", "100");
        pAst.put("cmpycd", cmpycd);
        pAst.put("code", "");
        pAst.put("codenm", "");
        pAst.put("etcval", "");

        apiService.executeHs00Procedure("HS00_000S_STR", pAst).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    assetList.clear();
                    for (Map<String, Object> m : response.body()) {
                        CodeDto dto = new CodeDto();
                        dto.codecd = getStringVal(m, "code");
                        if (dto.codecd.isEmpty()) dto.codecd = getStringVal(m, "codecd");
                        dto.codenm = getStringVal(m, "cdnm");
                        if (dto.codenm.isEmpty()) dto.codenm = getStringVal(m, "codenm");
                        assetList.add(dto);
                    }
                    ArrayAdapter<CodeDto> adapter = new ArrayAdapter<>(MHSBA010U.this, android.R.layout.simple_spinner_item, assetList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spSearchAstKind.setAdapter(adapter);
                    spAstKind.setAdapter(adapter);

                    // 기본값 선택 (120: 제품)
                    setSpinnerSelection(spSearchAstKind, assetList, "120");
                    setSpinnerSelection(spAstKind, assetList, "120");
                }
                search();
            }

            @Override
            public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {
                search();
            }
        });

        // 2. 단위 옵션 로드 (HS00_000S_STR gubun: U0 - Web HSBA010U.vue 100% 동일)
        Map<String, Object> pUnit = new HashMap<>();
        pUnit.put("gubun", "U0");
        pUnit.put("gbncd", "");
        pUnit.put("cmpycd", cmpycd);
        pUnit.put("code", "");
        pUnit.put("codenm", "");
        pUnit.put("etcval", "");

        apiService.executeHs00Procedure("HS00_000S_STR", pUnit).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    unitList.clear();
                    for (Map<String, Object> m : response.body()) {
                        CodeDto dto = new CodeDto();
                        dto.codecd = getStringVal(m, "unit");
                        if (dto.codecd.isEmpty()) dto.codecd = getStringVal(m, "codecd");
                        if (dto.codecd.isEmpty()) dto.codecd = getStringVal(m, "code");

                        dto.codenm = getStringVal(m, "unitnm");
                        if (dto.codenm.isEmpty()) dto.codenm = getStringVal(m, "codenm");
                        if (dto.codenm.isEmpty()) dto.codenm = getStringVal(m, "cdnm");
                        unitList.add(dto);
                    }
                    ArrayAdapter<CodeDto> adapter = new ArrayAdapter<>(MHSBA010U.this, android.R.layout.simple_spinner_item, unitList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spUnit.setAdapter(adapter);

                    // 기본값 EA 선택
                    setSpinnerSelection(spUnit, unitList, "EA");
                }
            }

            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {}
        });
    }

    private void search() {
        String selectedAstKind = "120";
        if (spSearchAstKind != null && spSearchAstKind.getSelectedItem() != null) {
            CodeDto selected = (CodeDto) spSearchAstKind.getSelectedItem();
            selectedAstKind = selected.codecd;
        }

        Map<String, Object> p = new HashMap<>();
        p.put("actkind", "S0");
        p.put("cmpycd", cmpycd);
        p.put("sch_astkind", selectedAstKind);
        p.put("sch_itemnm", etSearchItemNm.getText().toString().trim());
        p.put("icqty", 0.0);
        p.put("ocqty", 0.0);
        p.put("imprice", 0.0);
        p.put("omprice", 0.0);
        p.put("stock", 0.0);
        p.put("qtypnt", 0);

        apiService.executeHsbaProcedure("HSBA_010U_STR", p).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    itemList.clear();
                    itemList.addAll(response.body());
                    itemAdapter.notifyDataSetChanged();
                    Toast.makeText(MHSBA010U.this, "조회되었습니다. (" + itemList.size() + "건)", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MHSBA010U.this, "조회된 품목이 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {
                Toast.makeText(MHSBA010U.this, "통신 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateDetail(Map<String, Object> item) {
        if (item == null) return;
        actkind = "U0";

        etItemCd.setText(getStringVal(item, "itemcd"));
        etItemCd.setFocusable(false);
        etItemCd.setEnabled(false);

        etItemNm.setText(getStringVal(item, "itemnm"));
        etItSize.setText(getStringVal(item, "itsize"));
        etImPrice.setText(formatNumber(getStringVal(item, "imprice")));
        etOmPrice.setText(formatNumber(getStringVal(item, "omprice")));
        etBarcode.setText(getStringVal(item, "barcode"));
        etRemark.setText(getStringVal(item, "remark"));

        String ast = getStringVal(item, "astkind");
        setSpinnerSelection(spAstKind, assetList, ast);

        String u = getStringVal(item, "unit");
        setSpinnerSelection(spUnit, unitList, u);
    }

    private void save() {
        String itemNm = etItemNm.getText().toString().trim();
        if (itemNm.isEmpty()) {
            Toast.makeText(this, "품목명을 입력해 주십시오.", Toast.LENGTH_SHORT).show();
            return;
        }

        String astKind = "120";
        if (spAstKind != null && spAstKind.getSelectedItem() != null) {
            astKind = ((CodeDto) spAstKind.getSelectedItem()).codecd;
        }

        String unit = "EA";
        if (spUnit != null && spUnit.getSelectedItem() != null) {
            unit = ((CodeDto) spUnit.getSelectedItem()).codecd;
        }

        double imPriceVal = 0.0;
        try { imPriceVal = Double.parseDouble(etImPrice.getText().toString().replace(",", "").trim()); } catch (Exception ignored) {}

        double omPriceVal = 0.0;
        try { omPriceVal = Double.parseDouble(etOmPrice.getText().toString().replace(",", "").trim()); } catch (Exception ignored) {}

        Map<String, Object> p = new HashMap<>();
        p.put("actkind", actkind);
        p.put("cmpycd", cmpycd);
        p.put("itemcd", etItemCd.getText().toString().trim());
        p.put("itsize", etItSize.getText().toString().trim());
        p.put("unit", unit);
        p.put("itemnm", itemNm);
        p.put("itemenm", "");
        p.put("inunit", unit);
        p.put("icqty", 1.0);
        p.put("outunit", unit);
        p.put("ocqty", 1.0);
        p.put("setyn", "N");
        p.put("astkind", astKind);
        p.put("agrpcd", "");
        p.put("bgrpcd", "");
        p.put("imprice", imPriceVal);
        p.put("omprice", omPriceVal);
        p.put("stock", 0.0);
        p.put("qtypnt", 0);
        p.put("vatyn", "Y");
        p.put("sotaxyn", "N");
        p.put("udogyn", "N");
        p.put("barcode", etBarcode.getText().toString().trim());
        p.put("hscode", "");
        p.put("remark", etRemark.getText().toString().trim());
        p.put("useyn", "Y");
        p.put("in_custcd", "");
        p.put("autoyn", "N");
        p.put("updemp", userid);

        apiService.executeHsbaProcedure("HSBA_010U_STR", p).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MHSBA010U.this, "성공적으로 저장되었습니다.", Toast.LENGTH_SHORT).show();
                    search();
                    initializeForm();
                } else {
                    Toast.makeText(MHSBA010U.this, "저장 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {
                Toast.makeText(MHSBA010U.this, "저장 중 오류 발생: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeForm() {
        actkind = "A0";
        etItemCd.setText("");
        etItemCd.setFocusable(true);
        etItemCd.setFocusableInTouchMode(true);
        etItemCd.setEnabled(true);

        etItemNm.setText("");
        etItSize.setText("");
        etImPrice.setText("0");
        etOmPrice.setText("0");
        etBarcode.setText("");
        etRemark.setText("");
    }

    private void setSpinnerSelection(Spinner sp, List<CodeDto> list, String code) {
        if (sp == null || list == null || code == null) return;
        for (int i = 0; i < list.size(); i++) {
            if (code.equalsIgnoreCase(list.get(i).codecd)) {
                sp.setSelection(i);
                break;
            }
        }
    }

    private String getStringVal(Map<String, Object> map, String key) {
        if (map == null || key == null) return "";
        Object val = map.get(key);
        if (val == null) val = map.get(key.toUpperCase(Locale.ROOT));
        if (val == null) val = map.get(key.toLowerCase(Locale.ROOT));
        return val != null ? String.valueOf(val).trim() : "";
    }

    private String formatNumber(String str) {
        try {
            double d = Double.parseDouble(str.replace(",", ""));
            return new DecimalFormat("#,###").format(d);
        } catch (Exception e) {
            return str;
        }
    }

    @Override protected String getProgramTitle() { return "품목 등록"; }
    @Override protected String getProgramId() { return "MHSBA010U"; }

    private class ItemAdapter extends BaseAdapter {
        private final DecimalFormat df = new DecimalFormat("#,###");

        @Override public int getCount() { return itemList.size(); }
        @Override public Object getItem(int p) { return itemList.get(p); }
        @Override public long getItemId(int p) { return p; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(MHSBA010U.this).inflate(R.layout.item_mhsba010u, parent, false);
                holder = new ViewHolder();
                holder.tvItemCode = convertView.findViewById(R.id.tvItemCode);
                holder.tvItemName = convertView.findViewById(R.id.tvItemName);
                holder.tvItSize = convertView.findViewById(R.id.tvItSize);
                holder.tvUnit = convertView.findViewById(R.id.tvUnit);
                holder.tvOmPrice = convertView.findViewById(R.id.tvOmPrice);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Map<String, Object> item = itemList.get(position);
            holder.tvItemCode.setText(getStringVal(item, "itemcd"));
            holder.tvItemName.setText(getStringVal(item, "itemnm"));
            holder.tvItSize.setText(getStringVal(item, "itsize"));
            holder.tvUnit.setText(getStringVal(item, "unit"));

            double omPrice = 0;
            try { omPrice = Double.parseDouble(getStringVal(item, "omprice").replace(",", "")); } catch (Exception ignored) {}
            holder.tvOmPrice.setText(df.format(omPrice));

            return convertView;
        }
    }

    static class ViewHolder {
        TextView tvItemCode, tvItemName, tvItSize, tvUnit, tvOmPrice;
    }
}
