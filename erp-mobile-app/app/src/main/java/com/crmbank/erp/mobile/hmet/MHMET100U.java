package com.crmbank.erp.mobile.hmet;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;

import com.crmbank.erp.mobile.ApiService;
import com.crmbank.erp.mobile.BaseActivity;
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
 * 🚀 [MHMET100U] 회의록 등록 및 녹취 STT 자동 변환 프로그램 (모바일)
 */
public class MHMET100U extends BaseActivity {

    private TextView tvMeetingDt, tvAudioFile;
    private EditText etMeetingTime, etLocation, etHostNm, etMeetingTit, etSttText, etSummary, etContent;
    private ApiService apiService;
    private String cmpycd, userid, usernm;
    private String currentMeetingId = "NEW";

    @Override
    protected String getProgramTitle() {
        return "회의록 등록";
    }

    @Override
    protected String getProgramId() {
        return "HMET100U";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hmet100u);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        cmpycd = prefs.getString("cmpycd", "COIT").trim();
        userid = prefs.getString("userId", "").trim();
        usernm = prefs.getString("userNm", "담당자").trim();

        apiService = RetrofitClient.getApiService();

        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText("회의록 등록 (HMET100U)");

        tvMeetingDt = findViewById(R.id.tvMeetingDt);
        etMeetingTime = findViewById(R.id.etMeetingTime);
        etLocation = findViewById(R.id.etLocation);
        etHostNm = findViewById(R.id.etHostNm);
        etMeetingTit = findViewById(R.id.etMeetingTit);
        tvAudioFile = findViewById(R.id.tvAudioFile);
        etSttText = findViewById(R.id.etSttText);
        etSummary = findViewById(R.id.etSummary);
        etContent = findViewById(R.id.etContent);

        AppCompatButton btnNew = findViewById(R.id.btnNew);
        AppCompatButton btnSave = findViewById(R.id.btnSave);
        AppCompatButton btnSttSimulate = findViewById(R.id.btnSttSimulate);

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HHmm", Locale.getDefault()).format(new Date());
        tvMeetingDt.setText(today);
        etMeetingTime.setText(currentTime);
        etHostNm.setText(usernm);

        tvMeetingDt.setOnClickListener(v -> showDatePicker(tvMeetingDt));

        btnNew.setOnClickListener(v -> resetForm());
        btnSave.setOnClickListener(v -> saveMeeting());
        btnSttSimulate.setOnClickListener(v -> simulateSttAndSummary());
    }

    private void showDatePicker(TextView targetView) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            targetView.setText(dateStr);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void resetForm() {
        currentMeetingId = "NEW";
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HHmm", Locale.getDefault()).format(new Date());
        tvMeetingDt.setText(today);
        etMeetingTime.setText(currentTime);
        etLocation.setText("");
        etHostNm.setText(usernm);
        etMeetingTit.setText("");
        tvAudioFile.setText("첨부된 녹취 파일 없음");
        etSttText.setText("");
        etSummary.setText("");
        etContent.setText("");
        Toast.makeText(this, "신규 작성 모드", Toast.LENGTH_SHORT).show();
    }

    private void simulateSttAndSummary() {
        tvAudioFile.setText("sample_meeting_recording_20261023.mp3");
        Toast.makeText(this, "🎙️ 녹취 파일 분석 및 STT 변환 중...", Toast.LENGTH_SHORT).show();

        Map<String, Object> req = new HashMap<>();
        req.put("filename", "sample_meeting_recording_20261023.mp3");

        apiService.sttSimulate(req).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> res = response.body();
                    String stt = res.get("stt_text") != null ? String.valueOf(res.get("stt_text")) : "";
                    String summary = res.get("summary") != null ? String.valueOf(res.get("summary")) : "";

                    etSttText.setText(stt);
                    etSummary.setText(summary);
                    etContent.setText(stt);
                    Toast.makeText(MHMET100U.this, "✅ 녹취→회의록 자동 변환 및 요약 완료!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MHMET100U.this, "STT 변환 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                Toast.makeText(MHMET100U.this, "STT 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveMeeting() {
        String tit = etMeetingTit.getText().toString().trim();
        if (tit.isEmpty()) {
            Toast.makeText(this, "회의제목을 입력하세요.", Toast.LENGTH_SHORT).show();
            etMeetingTit.requestFocus();
            return;
        }

        Map<String, Object> master = new HashMap<>();
        master.put("cmpycd", cmpycd);
        master.put("meetingid", currentMeetingId);
        master.put("meetingtit", tit);
        master.put("meetingdt", tvMeetingDt.getText().toString().replace("-", ""));
        master.put("meetingtime", etMeetingTime.getText().toString().trim());
        master.put("location", etLocation.getText().toString().trim());
        master.put("hostid", userid);
        master.put("hostnm", etHostNm.getText().toString().trim());
        master.put("content", etContent.getText().toString().trim());
        master.put("summary", etSummary.getText().toString().trim());
        master.put("stt_text", etSttText.getText().toString().trim());
        master.put("audio_file_path", tvAudioFile.getText().toString());
        master.put("actkind", "NEW".equals(currentMeetingId) ? "A" : "U");

        List<Map<String, Object>> attendees = new ArrayList<>();
        Map<String, Object> att = new HashMap<>();
        att.put("userid", userid);
        att.put("usernm", etHostNm.getText().toString().trim());
        att.put("attendance_yn", "Y");
        attendees.add(att);

        List<Map<String, Object>> files = new ArrayList<>();
        if (!"첨부된 녹취 파일 없음".equals(tvAudioFile.getText().toString())) {
            Map<String, Object> f = new HashMap<>();
            f.put("filename", tvAudioFile.getText().toString());
            f.put("filepath", "/uploads/meeting/" + tvAudioFile.getText().toString());
            f.put("filesize", 1024500L);
            f.put("filetype", "AUDIO");
            files.add(f);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("master", master);
        payload.put("attendees", attendees);
        payload.put("files", files);

        apiService.saveHmet(payload).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentMeetingId = response.body();
                    Toast.makeText(MHMET100U.this, "정상적으로 저장되었습니다. (회의ID: " + currentMeetingId + ")", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MHMET100U.this, "저장 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(MHMET100U.this, "저장 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
