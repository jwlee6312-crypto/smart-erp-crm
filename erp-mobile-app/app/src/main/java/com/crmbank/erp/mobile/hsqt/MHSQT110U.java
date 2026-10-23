package com.crmbank.erp.mobile.hsqt;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.crmbank.erp.mobile.ApiService;
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
 * 🚀 [MHSQTY_110U] 견적현황 및 견적서 발송 조회 프로그램 (모바일)
 */
public class MHSQT110U extends AppCompatActivity {

    private TextView tvHeaderTitle, tvQuatYmdF, tvQuatYmdT;
    private AppCompatButton btnSearch;

    private ApiService apiService;
    private String cmpycd = "COIT";
    private StatusAdapter statusAdapter;
    private final List<Map<String, Object>> statusList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mhsqty_110u);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        cmpycd = prefs.getString("cmpycd", "COIT").trim();

        apiService = RetrofitClient.getApiService();

        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText("견적현황 (MHSQTY_110U)");

        tvQuatYmdF = findViewById(R.id.tvQuatYmdF);
        tvQuatYmdT = findViewById(R.id.tvQuatYmdT);
        btnSearch = findViewById(R.id.btnSearch);
        ListView lvStatusList = findViewById(R.id.lvStatusList);

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String firstDayOfMonth = today.substring(0, 8) + "01";
        tvQuatYmdF.setText(firstDayOfMonth);
        tvQuatYmdT.setText(today);

        tvQuatYmdF.setOnClickListener(v -> showDatePicker(tvQuatYmdF));
        tvQuatYmdT.setOnClickListener(v -> showDatePicker(tvQuatYmdT));

        statusAdapter = new StatusAdapter(this, statusList);
        lvStatusList.setAdapter(statusAdapter);

        btnSearch.setOnClickListener(v -> searchStatus());
        searchStatus();
    }

    private void showDatePicker(TextView targetView) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            targetView.setText(dateStr);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void searchStatus() {
        String quatymdf = tvQuatYmdF.getText().toString().trim();
        String quatymdt = tvQuatYmdT.getText().toString().trim();

        Map<String, Object> params = new HashMap<>();
        params.put("cmpycd", cmpycd);
        params.put("quatymdf", quatymdf);
        params.put("quatymdt", quatymdt);

        apiService.getHsqtStatus(params).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    statusList.clear();
                    statusList.addAll(response.body());
                    statusAdapter.notifyDataSetChanged();
                    Toast.makeText(MHSQT110U.this, "조회 완료 (" + statusList.size() + "건)", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MHSQT110U.this, "조회 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(MHSQT110U.this, "조회 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class StatusAdapter extends BaseAdapter {
        private final Context context;
        private final List<Map<String, Object>> items;

        public StatusAdapter(Context context, List<Map<String, Object>> items) {
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
            String quatym = item.get("quatym") != null ? String.valueOf(item.get("quatym")) : "";
            String quatno = item.get("quatno") != null ? String.valueOf(item.get("quatno")) : "";
            String quatNo = quatym + "-" + quatno;
            String custNm = item.get("custnm") != null ? String.valueOf(item.get("custnm")) : "거래처 미지정";
            String itemNm = item.get("itemnm") != null ? String.valueOf(item.get("itemnm")) : "품목 미지정";
            Object sumObj = item.get("quatsum");

            holder.text1.setText("[" + quatNo + "] " + custNm);
            holder.text2.setText("품목: " + itemNm + " | 합계금액: " + (sumObj != null ? sumObj : "0"));

            return view;
        }

        private static class ViewHolder {
            TextView text1, text2;
        }
    }
}
