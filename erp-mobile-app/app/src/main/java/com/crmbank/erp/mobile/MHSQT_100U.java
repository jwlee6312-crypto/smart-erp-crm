package com.crmbank.erp.mobile;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

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
 * 🚀 [MHSQT_100U] 견적서 등록 프로그램 (모바일)
 */
public class MHSQT_100U extends AppCompatActivity {

    private TextView tvHeaderTitle, tvQuatYmd, tvValidDate;
    private EditText etQuatYm, etQuatNo, etCustName, etRemark;
    private AppCompatButton btnNew, btnSave, btnSearch, btnAddRow;
    private ListView lvDetailList;

    private ApiService apiService;
    private String cmpycd = "COIT";
    private QuotationDetailAdapter detailAdapter;
    private final List<Map<String, Object>> detailList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quotation_register);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        cmpycd = prefs.getString("cmpycd", "COIT").trim();

        apiService = RetrofitClient.getApiService();

        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText("견적서 등록 (MHSQT_100U)");

        etQuatYm = findViewById(R.id.etQuatYm);
        etQuatNo = findViewById(R.id.etQuatNo);
        etCustName = findViewById(R.id.etCustName);
        tvQuatYmd = findViewById(R.id.tvQuatYmd);
        tvValidDate = findViewById(R.id.tvValidDate);
        etRemark = findViewById(R.id.etRemark);

        btnNew = findViewById(R.id.btnNew);
        btnSave = findViewById(R.id.btnSave);
        btnSearch = findViewById(R.id.btnSearch);
        btnAddRow = findViewById(R.id.btnAddRow);
        lvDetailList = findViewById(R.id.lvDetailList);

        String currentYm = new SimpleDateFormat("yyyyMM", Locale.getDefault()).format(new Date());
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        etQuatYm.setText(currentYm);
        etQuatNo.setText("0000");
        tvQuatYmd.setText(today);
        tvValidDate.setText(today);

        tvQuatYmd.setOnClickListener(v -> showDatePicker(tvQuatYmd));
        tvValidDate.setOnClickListener(v -> showDatePicker(tvValidDate));

        detailAdapter = new QuotationDetailAdapter(this, detailList);
        lvDetailList.setAdapter(detailAdapter);

        addEmptyRow();

        btnNew.setOnClickListener(v -> resetForm());
        btnSearch.setOnClickListener(v -> searchQuotation());
        btnSave.setOnClickListener(v -> saveQuotation());
        btnAddRow.setOnClickListener(v -> addEmptyRow());
    }

    private void showDatePicker(TextView targetView) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            targetView.setText(dateStr);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void addEmptyRow() {
        Map<String, Object> row = new HashMap<>();
        row.put("upkind", "A");
        row.put("qrowno", "");
        row.put("itemcd", "");
        row.put("itemnm", "");
        row.put("itsize", "");
        row.put("unit", "EA");
        row.put("quatqty", "1");
        row.put("price", "0");
        row.put("quatamt", "0");
        row.put("quatvat", "0");
        detailList.add(row);
        detailAdapter.notifyDataSetChanged();
    }

    private void resetForm() {
        String currentYm = new SimpleDateFormat("yyyyMM", Locale.getDefault()).format(new Date());
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        etQuatYm.setText(currentYm);
        etQuatNo.setText("0000");
        etCustName.setText("");
        tvQuatYmd.setText(today);
        tvValidDate.setText(today);
        etRemark.setText("");
        detailList.clear();
        addEmptyRow();
        Toast.makeText(this, "신규 작성 모드", Toast.LENGTH_SHORT).show();
    }

    private void searchQuotation() {
        String quatym = etQuatYm.getText().toString().trim();
        String quatno = etQuatNo.getText().toString().trim();

        if (quatym.length() == 0 || quatno.length() == 0) {
            Toast.makeText(this, "견적년월과 견적번호를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("cmpycd", cmpycd);
        params.put("quatym", quatym);
        params.put("quatno", quatno);

        apiService.getHsqtMaster(params).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Map<String, Object> master = response.body().get(0);
                    etCustName.setText(master.get("custnm") != null ? String.valueOf(master.get("custnm")) : "");
                    tvQuatYmd.setText(master.get("quatymd") != null ? String.valueOf(master.get("quatymd")) : "");
                    tvValidDate.setText(master.get("validdate") != null ? String.valueOf(master.get("validdate")) : "");
                    etRemark.setText(master.get("remark") != null ? String.valueOf(master.get("remark")) : "");
                    loadDetail(quatym, quatno);
                } else {
                    Toast.makeText(MHSQT_100U.this, "조회된 견적서가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(MHSQT_100U.this, "조회 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDetail(String quatym, String quatno) {
        Map<String, Object> params = new HashMap<>();
        params.put("cmpycd", cmpycd);
        params.put("quatym", quatym);
        params.put("quatno", quatno);

        apiService.getHsqtDetail(params).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    detailList.clear();
                    for (Map<String, Object> item : response.body()) {
                        item.put("upkind", "U");
                        detailList.add(item);
                    }
                    if (detailList.isEmpty()) addEmptyRow();
                    detailAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(MHSQT_100U.this, "상세 조회 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveQuotation() {
        String quatym = etQuatYm.getText().toString().trim();
        String quatno = etQuatNo.getText().toString().trim();
        String custnm = etCustName.getText().toString().trim();

        if (quatym.length() == 0) {
            Toast.makeText(this, "견적년월을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (custnm.length() == 0) {
            Toast.makeText(this, "거래처명을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> master = new HashMap<>();
        master.put("cmpycd", cmpycd);
        master.put("quatym", quatym);
        master.put("quatno", quatno);
        master.put("custnm", custnm);
        master.put("quatymd", tvQuatYmd.getText().toString().replace("-", ""));
        master.put("validdate", tvValidDate.getText().toString().replace("-", ""));
        master.put("remark", etRemark.getText().toString());
        master.put("actkind", "0000".equals(quatno) ? "A" : "U");

        Map<String, Object> payload = new HashMap<>();
        payload.put("master", master);
        payload.put("details", detailList);

        apiService.saveHsqt(payload).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MHSQT_100U.this, "정상적으로 저장되었습니다.", Toast.LENGTH_SHORT).show();
                    searchQuotation();
                } else {
                    Toast.makeText(MHSQT_100U.this, "저장 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Toast.makeText(MHSQT_100U.this, "저장 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class QuotationDetailAdapter extends BaseAdapter {
        private final Context context;
        private final List<Map<String, Object>> items;

        public QuotationDetailAdapter(Context context, List<Map<String, Object>> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public int getCount() { return items.size(); }

        @Override
        public Object getItem(int position) { return items.get(position); }

        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
                holder = new ViewHolder();
                holder.text1 = view.findViewById(android.R.id.text1);
                holder.text2 = view.findViewById(android.R.id.text2);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            Map<String, Object> item = items.get(position);
            String itemNm = item.get("itemnm") != null ? String.valueOf(item.get("itemnm")) : "품목 미입력";
            Object qtyObj = item.get("quatqty");
            Object amtObj = item.get("quatamt");
            holder.text1.setText("품목: " + itemNm);
            holder.text2.setText("수량: " + (qtyObj != null ? qtyObj : "0") + " | 금액: " + (amtObj != null ? amtObj : "0"));

            return view;
        }

        private static class ViewHolder {
            TextView text1, text2;
        }
    }
}
