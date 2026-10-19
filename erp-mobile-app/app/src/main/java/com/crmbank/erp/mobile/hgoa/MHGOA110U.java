package com.crmbank.erp.mobile.hgoa;

import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.crmbank.erp.mobile.BaseActivity;
import com.crmbank.erp.mobile.Config;
import com.crmbank.erp.mobile.R;
import com.crmbank.erp.mobile.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MHGOA110U extends BaseActivity {

    private EditText etSearchPhone;
    private TextView tvSummary;
    private ListView lvCallbackList;
    private CallbackAdapter adapter;
    private List<Map<String, Object>> callbackList;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mhgoa110u);

        etSearchPhone = findViewById(R.id.etSearchPhone);
        tvSummary = findViewById(R.id.tvSummary);
        lvCallbackList = findViewById(R.id.lvCallbackList);
        Button btnSearch = findViewById(R.id.btnSearch);

        callbackList = new ArrayList<>();
        adapter = new CallbackAdapter();
        lvCallbackList.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> fetchList());

        fetchList();
    }

    @Override protected String getProgramTitle() { return "모바일 콜백 관리"; }
    @Override protected String getProgramId() { return "MHGOA110U"; }

    private void fetchList() {
        Map<String, Object> params = new HashMap<>();
        String phone = etSearchPhone.getText().toString().trim();
        if (!phone.isEmpty()) params.put("src_no", phone);

        RetrofitClient.getApiService().getCallbackList(params).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callbackList.clear();
                    callbackList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    updateSummary();
                }
            }
            @Override public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {}
        });
    }

    private void updateSummary() {
        int pending = 0;
        for (Map<String, Object> item : callbackList) {
            if (!"030".equals(getValue(item, "callback_status"))) pending++;
        }
        tvSummary.setText("미결 콜백: " + pending + "건");
    }

    private void showResultDialog(Map<String, Object> item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_callback_result, null);
        builder.setView(dialogView);

        EditText etRemark = dialogView.findViewById(R.id.etRemark);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            Map<String, Object> payload = new HashMap<>();
            payload.put("INTERACTION_ID", getValue(item, "interaction_id"));
            payload.put("rslt_cd", "200"); // 💡 성공 임시 고정
            payload.put("remark", etRemark.getText().toString());

            RetrofitClient.getApiService().saveCallbackResponse(payload).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    Toast.makeText(MHGOA110U.this, "저장 완료", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    fetchList();
                }
                @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
            });
        });
        dialog.show();
    }

    private void playVoice(String filename) {
        if (filename == null || filename.isEmpty()) return;
        String url = Config.BASE_URL + "crm/inbound/play-recording?file=" + filename;
        try {
            if (mediaPlayer != null) mediaPlayer.release();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
        } catch (Exception e) {}
    }

    private String getValue(Map<String, Object> map, String key) {
        if (map == null) return "";
        Object v = map.get(key);
        if (v == null) v = map.get(key.toUpperCase());
        if (v == null) v = map.get(key.toLowerCase());
        return v != null ? v.toString() : "";
    }

    private class CallbackAdapter extends BaseAdapter {
        @Override public int getCount() { return callbackList.size(); }
        @Override public Object getItem(int pos) { return callbackList.get(pos); }
        @Override public long getItemId(int pos) { return pos; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(MHGOA110U.this).inflate(R.layout.item_mhgoa110u, parent, false);
            }
            Map<String, Object> item = callbackList.get(position);
            ((TextView) convertView.findViewById(R.id.tvStartTime)).setText(getValue(item, "start_time").replace("T", " "));
            ((TextView) convertView.findViewById(R.id.tvCustNm)).setText(getValue(item, "custnm"));
            ((TextView) convertView.findViewById(R.id.tvCallbackNo)).setText(getValue(item, "callback_no"));

            convertView.findViewById(R.id.btnPlay).setOnClickListener(v -> playVoice(getValue(item, "rec_file")));
            convertView.findViewById(R.id.btnCall).setOnClickListener(v -> {
                makeCall(getValue(item, "callback_no"));
                showResultDialog(item);
            });
            return convertView;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) mediaPlayer.release();
    }
}
