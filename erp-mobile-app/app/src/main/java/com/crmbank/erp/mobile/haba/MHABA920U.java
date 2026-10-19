package com.crmbank.erp.mobile.haba;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.crmbank.erp.mobile.ApiService;
import com.crmbank.erp.mobile.BaseActivity;
import com.crmbank.erp.mobile.R;
import com.crmbank.erp.mobile.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 🚀 [MHABA920U] 비상연락망 (직원현황 조회 및 단체연락)
 * - Airbnb Style Grid List 적용 (기능 보완 완료)
 */
public class MHABA920U extends BaseActivity {

    private EditText etSearch;
    private LinearLayout llAgentList, layoutBulkAction;
    private CheckBox cbSelectAll;
    private ApiService apiService;
    private String cmpycd;
    private final List<Map<String, Object>> agentList = new ArrayList<>();
    private final Map<String, Boolean> selectionMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mhaba920u);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        cmpycd = prefs.getString("cmpycd", "coit"); // 대소문자 호환성 유지

        apiService = RetrofitClient.getApiService();

        initViews();
        search();
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        llAgentList = findViewById(R.id.llAgentList);
        layoutBulkAction = findViewById(R.id.layoutBulkAction);
        cbSelectAll = findViewById(R.id.cbSelectAll);

        findViewById(R.id.btnSearch).setOnClickListener(v -> search());

        cbSelectAll.setOnClickListener(v -> {
            boolean checked = cbSelectAll.isChecked();
            for (Map<String, Object> agent : agentList) {
                selectionMap.put(getStringVal(agent, "userid"), checked);
            }
            refreshList();
            updateBulkActionBar();
        });

        findViewById(R.id.btnBulkSms).setOnClickListener(v -> sendBulkSms());
        findViewById(R.id.btnBulkMail).setOnClickListener(v -> sendBulkEmail());
    }

    private void search() {
        Map<String, Object> p = new HashMap<>();
        p.put("actkind", "S1");
        p.put("word", etSearch.getText().toString().trim());
        p.put("cmpycd", cmpycd);

        apiService.executeHabaProcedure("HABA_920U_STR", p).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    agentList.clear();
                    selectionMap.clear();
                    cbSelectAll.setChecked(false);
                    agentList.addAll(response.body());
                    refreshList();
                    updateBulkActionBar();
                } else {
                    Toast.makeText(MHABA920U.this, "조회 실패 (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {
                Toast.makeText(MHABA920U.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshList() {
        llAgentList.removeAllViews();
        if (agentList.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("조회 결과가 없습니다.");
            tv.setPadding(0, 100, 0, 0);
            tv.setGravity(android.view.Gravity.CENTER);
            llAgentList.addView(tv);
            return;
        }

        for (Map<String, Object> agent : agentList) {
            View v = getLayoutInflater().inflate(R.layout.item_mhaba920u_row, llAgentList, false);
            
            CheckBox cb = v.findViewById(R.id.cbSelect);
            TextView tvName = v.findViewById(R.id.tvRowName);
            TextView tvDept = v.findViewById(R.id.tvRowDept);
            TextView tvHp = v.findViewById(R.id.tvRowHp);
            TextView tvEmail = v.findViewById(R.id.tvRowEmail);

            final String userid = getStringVal(agent, "userid");
            tvName.setText(getStringVal(agent, "usernm") + " " + getStringVal(agent, "jikch"));
            tvDept.setText(getStringVal(agent, "deptnm"));
            tvHp.setText(getStringVal(agent, "hpno"));
            tvEmail.setText(getStringVal(agent, "email"));

            Boolean isSelected = selectionMap.get(userid);
            cb.setChecked(isSelected != null && isSelected);
            
            cb.setOnClickListener(v1 -> {
                selectionMap.put(userid, cb.isChecked());
                updateBulkActionBar();
            });

            // 개별 연락처 영역 클릭 시 체크박스 연동
            v.setOnClickListener(v1 -> {
                boolean newState = !cb.isChecked();
                cb.setChecked(newState);
                selectionMap.put(userid, newState);
                updateBulkActionBar();
            });

            v.findViewById(R.id.btnCall).setOnClickListener(v1 -> {
                String hp = getStringVal(agent, "hpno");
                if (hp.isEmpty()) hp = getStringVal(agent, "telno");
                makeCall(hp);
            });

            v.findViewById(R.id.btnMail).setOnClickListener(v1 -> {
                sendEmail(getStringVal(agent, "email"));
            });

            llAgentList.addView(v);
        }
    }

    private void updateBulkActionBar() {
        boolean hasSelection = false;
        for (Boolean b : selectionMap.values()) {
            if (b != null && b) { hasSelection = true; break; }
        }
        layoutBulkAction.setVisibility(hasSelection ? View.VISIBLE : View.GONE);
    }

    private void sendBulkSms() {
        List<String> numbers = new ArrayList<>();
        for (Map<String, Object> agent : agentList) {
            Boolean isSelected = selectionMap.get(getStringVal(agent, "userid"));
            if (isSelected != null && isSelected) {
                String hp = getStringVal(agent, "hpno").replace("-", "");
                if (!hp.isEmpty()) numbers.add(hp);
            }
        }
        if (numbers.isEmpty()) return;

        // API 21+ 호환을 위해 String.join 대신 직접 결합
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<numbers.size(); i++) {
            sb.append(numbers.get(i));
            if(i < numbers.size()-1) sb.append(";");
        }

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + sb.toString()));
        startActivity(intent);
    }

    private void sendBulkEmail() {
        List<String> emails = new ArrayList<>();
        for (Map<String, Object> agent : agentList) {
            Boolean isSelected = selectionMap.get(getStringVal(agent, "userid"));
            if (isSelected != null && isSelected) {
                String email = getStringVal(agent, "email");
                if (!email.isEmpty()) emails.add(email);
            }
        }
        if (emails.isEmpty()) return;

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, emails.toArray(new String[0]));
        startActivity(intent);
    }

    private String getStringVal(Map<String, Object> map, String key) {
        if (map == null) return "";
        Object v = map.get(key.toLowerCase());
        if (v == null) v = map.get(key.toUpperCase());
        return v != null ? String.valueOf(v).trim() : "";
    }

    @Override protected String getProgramTitle() { return "비상연락망"; }
    @Override protected String getProgramId() { return "MHABA920U"; }
}
