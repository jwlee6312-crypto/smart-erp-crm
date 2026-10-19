package com.crmbank.erp.mobile.hsaa;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crmbank.erp.mobile.ApiResponse;
import com.crmbank.erp.mobile.ApiService;
import com.crmbank.erp.mobile.BaseActivity;
import com.crmbank.erp.mobile.PopupAdapter;
import com.crmbank.erp.mobile.R;
import com.crmbank.erp.mobile.RetrofitClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 🚀 [MHSAA130U] 상담일지 등록
 */
public class MHSAA130U extends BaseActivity {

    private EditText etSalesTitle, etContHH, etContMM, etDiaryContent, etReportContent;
    private TextView tvContDt, tvReportContentLabel, tvSalesCoaching, tvCoachingDt;
    private Spinner spKeyman, spChannel;
    private CheckBox cbReportYn;
    private View btnConfirmCoaching;
    private LinearLayout llDiaryList, layoutCoachingResult;

    private ApiService apiService;
    private String cmpycd, userid;
    private String salesid = "";
    private String custcd = "";
    private String currentSer = "";
    private String tostate = ""; // 단계변동 정보는 저장 시 전달을 위해 유지

    private final String DIARY_TEMPLATE = "[방문 목적]\n- \n\n[상담 내용]\n- \n\n[향후 계획]\n- ";

    private final List<Map<String, Object>> diaryList = new ArrayList<>();
    private final List<Map<String, Object>> channelCodeList = new ArrayList<>();
    private final List<Map<String, Object>> stateCodeList = new ArrayList<>();
    private final List<Map<String, Object>> keymanList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mhsaa130u);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        cmpycd = prefs.getString("cmpycd", "coit").trim();
        userid = prefs.getString("userId", "");

        apiService = RetrofitClient.getApiService();

        initViews();
        loadInitialData();

        salesid = getIntent().getStringExtra("salesid");
        if (salesid != null && !salesid.isEmpty()) {
            fetchSalesDetail();
        } else {
            initializeForm();
        }
    }

    private void initViews() {
        etSalesTitle = findViewById(R.id.etSalesTitle);
        etContHH = findViewById(R.id.etContHH);
        etContMM = findViewById(R.id.etContMM);
        etDiaryContent = findViewById(R.id.etDiaryContent);
        etReportContent = findViewById(R.id.etReportContent);
        tvContDt = findViewById(R.id.tvContDt);
        tvReportContentLabel = findViewById(R.id.tvReportContentLabel);
        tvSalesCoaching = findViewById(R.id.tvSalesCoaching);
        tvCoachingDt = findViewById(R.id.tvCoachingDt);
        layoutCoachingResult = findViewById(R.id.layoutCoachingResult);
        btnConfirmCoaching = findViewById(R.id.btnConfirmCoaching);

        spKeyman = findViewById(R.id.spKeyman);
        spChannel = findViewById(R.id.spChannel);
        cbReportYn = findViewById(R.id.cbReportYn);

        llDiaryList = findViewById(R.id.llDiaryList);

        findViewById(R.id.btnReset).setOnClickListener(v -> initializeForm());
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
        findViewById(R.id.btnDelete).setOnClickListener(v -> delete());
        findViewById(R.id.btnSalesSearch).setOnClickListener(v -> openHelp("SALES"));
        
        btnConfirmCoaching.setOnClickListener(v -> confirmCoaching());
        
        tvContDt.setOnClickListener(v -> showDatePicker(tvContDt));

        cbReportYn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tvReportContentLabel.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            etReportContent.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
    }

    private void initializeForm() {
        currentSer = "";
        tostate = "";
        etContHH.setText("10");
        etContMM.setText("00");
        etDiaryContent.setText(DIARY_TEMPLATE);
        etReportContent.setText("");
        cbReportYn.setChecked(false);
        tvContDt.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        
        layoutCoachingResult.setVisibility(View.GONE);
        
        spChannel.setSelection(0);
        if (spKeyman.getAdapter() != null && spKeyman.getAdapter().getCount() > 0) spKeyman.setSelection(0);
    }

    private void loadInitialData() {
        // 채널 코드 (710)
        loadCode("710", spChannel, channelCodeList);
    }

    private void loadCode(String group, Spinner sp, List<Map<String, Object>> list) {
        apiService.getHsaaCodes(group).enqueue(new Callback<ApiResponse<List<Map<String, Object>>>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<List<Map<String, Object>>>> c, @NonNull Response<ApiResponse<List<Map<String, Object>>>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    list.clear(); list.addAll(r.body().getData());
                    setupSpinner(sp, list, "codenm", true);
                }
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<List<Map<String, Object>>>> c, @NonNull Throwable t) {}
        });
    }

    private void setupSpinner(Spinner sp, List<Map<String, Object>> data, String key, boolean hasEmpty) {
        List<String> items = new ArrayList<>();
        if (hasEmpty) items.add("선택안함");
        for (Map<String, Object> d : data) items.add(getStringVal(d, key));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);
    }

    private void fetchSalesDetail() {
        apiService.getHsaaDetail(salesid).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> mst = (Map<String, Object>) response.body().getData().get("master");
                    if (mst != null) {
                        etSalesTitle.setText(getStringVal(mst, "salestitle"));
                        custcd = getStringVal(mst, "custcd");
                        loadKeymen();
                        fetchDiaryHistory();
                    }
                }
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Throwable t) {}
        });
    }

    private void loadKeymen() {
        apiService.getHsaaKeyman(custcd).enqueue(new Callback<ApiResponse<List<Map<String, Object>>>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<List<Map<String, Object>>>> call, @NonNull Response<ApiResponse<List<Map<String, Object>>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    keymanList.clear(); keymanList.addAll(response.body().getData());
                    setupSpinner(spKeyman, keymanList, "name", false);
                }
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<List<Map<String, Object>>>> call, @NonNull Throwable t) {}
        });
    }

    private void fetchDiaryHistory() {
        apiService.getHsaaDiary(salesid).enqueue(new Callback<ApiResponse<List<Map<String, Object>>>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<List<Map<String, Object>>>> call, @NonNull Response<ApiResponse<List<Map<String, Object>>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    diaryList.clear(); diaryList.addAll(response.body().getData());
                    refreshDiaryList();
                }
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<List<Map<String, Object>>>> call, @NonNull Throwable t) {}
        });
    }

    private void refreshDiaryList() {
        llDiaryList.removeAllViews();
        for (Map<String, Object> item : diaryList) {
            View v = getLayoutInflater().inflate(R.layout.item_mhsaa130u_row, llDiaryList, false);
            ((TextView) v.findViewById(R.id.tvRowDate)).setText(formatDate(getStringVal(item, "contdt")));
            ((TextView) v.findViewById(R.id.tvRowSummary)).setText(getStringVal(item, "content"));
            ((TextView) v.findViewById(R.id.tvRowState)).setText(getStringVal(item, "statenm"));
            
            v.setOnClickListener(v1 -> loadDiaryDetail(item));
            llDiaryList.addView(v);
        }
    }

    private void loadDiaryDetail(Map<String, Object> item) {
        currentSer = getStringVal(item, "ser");
        tostate = getStringVal(item, "tostate");
        etContHH.setText(getStringVal(item, "conthh"));
        etContMM.setText(getStringVal(item, "contmm"));
        etDiaryContent.setText(getStringVal(item, "diarycontent"));
        etReportContent.setText(getStringVal(item, "reportcontent"));
        cbReportYn.setChecked("Y".equals(getStringVal(item, "reportyn")));
        tvContDt.setText(formatDate(getStringVal(item, "contdt")));
        
        // 🚀 코칭 내용 표시
        String coaching = getStringVal(item, "salescoaching");
        if (!coaching.isEmpty()) {
            layoutCoachingResult.setVisibility(View.VISIBLE);
            tvSalesCoaching.setText(coaching);
            tvCoachingDt.setText("코칭일: " + formatDate(getStringVal(item, "reportdt")));
            
            // 💡 확인 여부에 따른 버튼 표시
            boolean isRead = "Y".equalsIgnoreCase(getStringVal(item, "coachingreadyn"));
            btnConfirmCoaching.setVisibility(isRead ? View.GONE : View.VISIBLE);
        } else {
            layoutCoachingResult.setVisibility(View.GONE);
        }
        
        setSpinnerSelection(spChannel, channelCodeList, "codecd", getStringVal(item, "channel"), true);
        setSpinnerSelection(spKeyman, keymanList, "custid", getStringVal(item, "custid"), false);
    }

    private void confirmCoaching() {
        if (salesid.isEmpty() || currentSer.isEmpty()) return;

        Map<String, Object> p = new HashMap<>();
        p.put("salesid", salesid);
        p.put("ser", currentSer);
        p.put("contdt", tvContDt.getText().toString().replace("-", ""));

        apiService.confirmHsaaCoaching(p).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MHSAA130U.this, "지시사항 확인을 완료했습니다.", Toast.LENGTH_SHORT).show();
                    fetchDiaryHistory();
                    btnConfirmCoaching.setVisibility(View.GONE);
                }
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {}
        });
    }

    private void save() {
        if (salesid.isEmpty()) { Toast.makeText(this, "영업건을 선택하세요.", Toast.LENGTH_SHORT).show(); return; }
        if (etDiaryContent.getText().toString().isEmpty()) { Toast.makeText(this, "내용을 입력하세요.", Toast.LENGTH_SHORT).show(); return; }

        Map<String, Object> payload = new HashMap<>();
        payload.put("cmpycd", cmpycd);
        payload.put("salesid", salesid);
        payload.put("custcd", custcd);
        payload.put("ser", currentSer);
        payload.put("contdt", tvContDt.getText().toString().replace("-", ""));
        payload.put("conthh", etContHH.getText().toString());
        payload.put("contmm", etContMM.getText().toString());
        payload.put("diarycontent", etDiaryContent.getText().toString());
        payload.put("reportyn", cbReportYn.isChecked() ? "Y" : "N");
        payload.put("reportcontent", etReportContent.getText().toString());
        payload.put("tostate", tostate); // 단계변동 정보는 기존값 유지
        
        // 요약 내용 추출
        String content = etDiaryContent.getText().toString();
        if (content.length() > 50) content = content.substring(0, 50);
        payload.put("content", content);

        if (spKeyman.getSelectedItemPosition() >= 0) payload.put("custid", getStringVal(keymanList.get(spKeyman.getSelectedItemPosition()), "custid"));
        
        int chanIdx = spChannel.getSelectedItemPosition();
        if (chanIdx > 0) payload.put("channel", getStringVal(channelCodeList.get(chanIdx - 1), "codecd"));

        apiService.saveHsaaDiary(payload).enqueue(new Callback<ApiResponse<Object>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MHSAA130U.this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
                    fetchDiaryHistory(); initializeForm();
                }
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {}
        });
    }

    private void delete() {
        if (currentSer.isEmpty()) return;
        new AlertDialog.Builder(this).setTitle("삭제").setMessage("상담일지를 삭제하시겠습니까?")
            .setPositiveButton("예", (d, w) -> {
                apiService.deleteHsaaDiary(salesid, currentSer).enqueue(new Callback<ApiResponse<Object>>() {
                    @Override public void onResponse(@NonNull Call<ApiResponse<Object>> c, @NonNull Response<ApiResponse<Object>> r) {
                        Toast.makeText(MHSAA130U.this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        fetchDiaryHistory(); initializeForm();
                    }
                    @Override public void onFailure(@NonNull Call<ApiResponse<Object>> c, @NonNull Throwable t) {}
                });
            }).setNegativeButton("아니오", null).show();
    }

    private void openHelp(String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_hsaa_search, null);
        builder.setView(dialogView);
        
        TextView tvStart = dialogView.findViewById(R.id.tvPopStartDate);
        TextView tvEnd = dialogView.findViewById(R.id.tvPopEndDate);
        EditText etSch = dialogView.findViewById(R.id.etPopSearch);
        ListView lv = dialogView.findViewById(R.id.lvPopList);

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        tvEnd.setText(sdf.format(cal.getTime()));
        cal.add(Calendar.MONTH, -1);
        tvStart.setText(sdf.format(cal.getTime()));

        tvStart.setOnClickListener(v -> showDatePicker(tvStart));
        tvEnd.setOnClickListener(v -> showDatePicker(tvEnd));
        
        List<Map<String, Object>> list = new ArrayList<>();
        BaseAdapter adapter = new BaseAdapter() {
            @Override public int getCount() { return list.size(); }
            @Override public Object getItem(int p) { return list.get(p); }
            @Override public long getItemId(int p) { return p; }
            @Override public View getView(int p, View v, ViewGroup pr) {
                if (v == null) v = getLayoutInflater().inflate(R.layout.item_hsaa_pop, pr, false);
                Map<String, Object> item = list.get(p);
                ((TextView) v.findViewById(R.id.tvPopCustNm)).setText(getStringVal(item, "custnm"));
                ((TextView) v.findViewById(R.id.tvPopSalesTitle)).setText(getStringVal(item, "salestitle"));
                ((TextView) v.findViewById(R.id.tvPopAddTime)).setText(formatDate(getStringVal(item, "addtime")));
                return v;
            }
        };
        lv.setAdapter(adapter);

        AlertDialog dialog = builder.create();
        dialogView.findViewById(R.id.btnPopSearch).setOnClickListener(v -> {
            Map<String, Object> p = new HashMap<>();
            p.put("fromdt", tvStart.getText().toString().replace("-", ""));
            p.put("todt", tvEnd.getText().toString().replace("-", ""));
            p.put("schcustnm", etSch.getText().toString().trim());
            apiService.getHsaaMaster(p).enqueue(new Callback<ApiResponse<List<Map<String, Object>>>>() {
                @Override public void onResponse(@NonNull Call<ApiResponse<List<Map<String, Object>>>> c, @NonNull Response<ApiResponse<List<Map<String, Object>>>> r) {
                    if (r.isSuccessful() && r.body() != null) {
                        list.clear(); list.addAll(r.body().getData()); adapter.notifyDataSetChanged();
                    }
                }
                @Override public void onFailure(@NonNull Call<ApiResponse<List<Map<String, Object>>>> c, @NonNull Throwable t) {}
            });
        });
        
        lv.setOnItemClickListener((p1, v1, pos, id) -> {
            salesid = getStringVal(list.get(pos), "salesid");
            fetchSalesDetail();
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnPopClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
        dialogView.findViewById(R.id.btnPopSearch).performClick();
    }

    private void showDatePicker(TextView tv) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) -> {
            tv.setText(String.format(Locale.getDefault(), "%d-%02d-%02d", y, m + 1, d));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private String getStringVal(Map<String, Object> map, String key) {
        if (map == null) return "";
        Object val = map.get(key.toLowerCase());
        if (val == null) val = map.get(key.toUpperCase());
        return val != null ? String.valueOf(val).trim() : "";
    }

    private void setSpinnerSelection(Spinner sp, List<Map<String, Object>> list, String key, String val, boolean hasEmpty) {
        int offset = hasEmpty ? 1 : 0;
        for (int i = 0; i < list.size(); i++) {
            if (getStringVal(list.get(i), key).equals(val)) {
                sp.setSelection(i + offset);
                break;
            }
        }
    }

    private String formatDate(String d) {
        if (d == null || d.isEmpty()) return "";
        if (d.contains(" ")) d = d.split(" ")[0];
        return d.length() == 8 ? String.format("%s-%s-%s", d.substring(0,4), d.substring(4,6), d.substring(6,8)) : d;
    }

    @Override protected String getProgramTitle() { return "상담일지 등록"; }
    @Override protected String getProgramId() { return "MHSAA130U"; }
}
