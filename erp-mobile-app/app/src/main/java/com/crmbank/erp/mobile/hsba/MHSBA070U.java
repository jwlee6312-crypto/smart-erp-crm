package com.crmbank.erp.mobile.hsba;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crmbank.erp.mobile.BaseActivity;
import com.crmbank.erp.mobile.RetrofitClient;
import com.crmbank.erp.mobile.ApiService;
import com.crmbank.erp.mobile.ApiResponse;
import com.crmbank.erp.mobile.CodeDto;
import com.crmbank.erp.mobile.CustDto;
import com.crmbank.erp.mobile.PopupAdapter;
import com.crmbank.erp.mobile.R;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 🚀 [MHSBA070U] 거래처 정보 등록 (사업자등록증 카메라/갤러리 OCR 촬영 및 자동인식 연동 버전)
 */
public class MHSBA070U extends BaseActivity {

    private static final String TAG = "CustReg";
    private EditText etCustCd, etCustNm, etCustNo, etCorpRegNo, etBossNm, etTelNo, etAddress, etDAddress, etCustType, etCustKind, etRegDate;
    private Spinner spStatus;
    private Switch swElcYn, swUseYn;
    private ImageView ivBizLicense;
    private ApiService apiService;
    private String cmpycd, userid;
    private String currentActKind = "I0";
    private final List<CodeDto> statusOptions = new ArrayList<>();

    // 📷 카메라 촬영 결과 런처
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        if (imageBitmap != null) {
                            ivBizLicense.setVisibility(View.VISIBLE);
                            ivBizLicense.setImageBitmap(imageBitmap);
                            processImage(InputImage.fromBitmap(imageBitmap, 0));
                        }
                    }
                }
            });

    // 🖼️ 갤러리/파일 선택 결과 런처
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedUri = result.getData().getData();
                    if (selectedUri != null) {
                        handleSelectedUri(selectedUri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mhsba070u);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        cmpycd = prefs.getString("cmpycd", "coit").trim();
        userid = prefs.getString("userId", "");

        apiService = RetrofitClient.getApiService();

        etCustCd = findViewById(R.id.etCustCd);
        etCustNm = findViewById(R.id.etCustNm);
        etCustNo = findViewById(R.id.etCustNo);
        etCorpRegNo = findViewById(R.id.etCorpRegNo);
        etBossNm = findViewById(R.id.etBossNm);
        etTelNo = findViewById(R.id.etTelNo);
        etAddress = findViewById(R.id.etAddress);
        etDAddress = findViewById(R.id.etDAddress);
        etCustType = findViewById(R.id.etCustType);
        etCustKind = findViewById(R.id.etCustKind);
        etRegDate = findViewById(R.id.etRegDate);
        spStatus = findViewById(R.id.spStatus);
        swElcYn = findViewById(R.id.swElcYn);
        swUseYn = findViewById(R.id.swUseYn);
        ivBizLicense = findViewById(R.id.ivBizLicense);

        findViewById(R.id.btnScanBizLicense).setOnClickListener(v -> showImageSourceDialog());
        findViewById(R.id.btnSearch).setOnClickListener(v -> openCustSearchPopup());
        findViewById(R.id.btnSave).setOnClickListener(v -> save());

        loadStatusOptions();
        initialize();
    }

    private void initialize() {
        currentActKind = "I0";
        etCustCd.setText("");
        etCustCd.setEnabled(true);
        clearFields();
        swElcYn.setChecked(true);
        swUseYn.setChecked(true);
        ivBizLicense.setVisibility(View.GONE);
    }

    private void clearFields() {
        etCustNm.setText("");
        etCustNo.setText("");
        etCorpRegNo.setText("");
        etBossNm.setText("");
        etTelNo.setText("");
        etAddress.setText("");
        etDAddress.setText("");
        etCustType.setText("");
        etCustKind.setText("");
        etRegDate.setText("");
    }

    private void loadStatusOptions() {
        Map<String, Object> p = new HashMap<>();
        p.put("gubun", "E0"); p.put("cmpycd", cmpycd); p.put("gbncd", "280");
        apiService.executeHs00Procedure("HS00_000S_STR", p).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    statusOptions.clear();
                    List<String> names = new ArrayList<>();
                    for (Map<String, Object> m : response.body()) {
                        CodeDto dto = new CodeDto();
                        dto.codecd = getStringVal(m, "code");
                        dto.codenm = getStringVal(m, "cdnm");
                        statusOptions.add(dto);
                        names.add(dto.codenm);
                    }
                    spStatus.setAdapter(new ArrayAdapter<>(MHSBA070U.this, android.R.layout.simple_spinner_item, names));
                }
            }
            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {}
        });
    }

    // 📸 사업자등록증 이미지 가져오기 다이얼로그 (카메라 / 갤러리)
    private void showImageSourceDialog() {
        String[] options = {"카메라 촬영", "갤러리/파일 선택"};
        new AlertDialog.Builder(this).setTitle("사업자등록증 스캔").setItems(options, (dialog, which) -> {
            if (which == 0) openCamera();
            else openGallery();
        }).show();
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) cameraLauncher.launch(takePictureIntent);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"image/*", "application/pdf"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        galleryLauncher.launch(Intent.createChooser(intent, "파일 선택"));
    }

    private void handleSelectedUri(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            ivBizLicense.setVisibility(View.VISIBLE);
            ivBizLicense.setImageBitmap(bitmap);
            processImage(InputImage.fromBitmap(bitmap, 0));
        } catch (Exception e) {
            Toast.makeText(this, "이미지 로드 실패", Toast.LENGTH_SHORT).show();
        }
    }

    // 🔍 ML Kit 한글 OCR 텍스트 인식 처리
    private void processImage(InputImage image) {
        TextRecognizer recognizer = TextRecognition.getClient(new KoreanTextRecognizerOptions.Builder().build());
        recognizer.process(image)
                .addOnSuccessListener(this::parseRecognizedText)
                .addOnFailureListener(e -> Toast.makeText(this, "텍스트 인식 실패", Toast.LENGTH_SHORT).show());
    }

    private void parseRecognizedText(Text visionText) {
        clearFields();
        List<Text.Line> allLines = new ArrayList<>();
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            allLines.addAll(block.getLines());
        }

        for (int i = 0; i < allLines.size(); i++) {
            Rect b = allLines.get(i).getBoundingBox();
            if (b != null) {
                Log.d(TAG, String.format("RAW LINE[%d]: [%s] T:%d, B:%d, L:%d", i, allLines.get(i).getText(), b.top, b.bottom, b.left));
            }
        }

        // 1. 가상 행 구성 (Y좌표 12px 이내 병합)
        List<LogicalRow> rows = new ArrayList<>();
        for (Text.Line line : allLines) {
            boolean found = false;
            for (LogicalRow row : rows) {
                if (row.isSameRow(line)) {
                    row.addFragment(line);
                    found = true;
                    break;
                }
            }
            if (!found) rows.add(new LogicalRow(line));
        }
        Collections.sort(rows, (r1, r2) -> r1.top - r2.top);

        // 2. 상호(법인명) 행 위치 탐색
        int startIdx = 0;
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).getMergedText().replace(" ", "").matches(".*(상호|법인명).*")) {
                startIdx = i;
                break;
            }
        }

        boolean isFinished = false;
        int bizTypeRowIdx = -1;

        // 3. 필드별 데이터 추출 (상호 ~ 종목)
        for (int i = startIdx; i < rows.size() && !isFinished; i++) {
            LogicalRow row = rows.get(i);
            String rawText = row.getMergedText();
            String cleanText = rawText.replace(" ", "");

            if (cleanText.contains("상호") || cleanText.contains("법인명")) {
                // 🚀 상호명 내 모든 공백 제거 규칙 적용
                String nameVal = row.getValueAfter("상호|법인명", "대표|성명|소재지").replace(" ", "");
                etCustNm.setText(nameVal);
            }
            if (cleanText.contains("성명") || cleanText.contains("대표자")) {
                String val = row.getValueAfter("성명|대표자", "번호|등록|소재지");
                etBossNm.setText(val.replaceAll("\\(.*?\\)", "").trim());
            }
            if (cleanText.contains("사업장") || cleanText.contains("소재지") || cleanText.contains("주소")) {
                String fullAddr = row.getValueAfter("사업장소재지|사업장|소재지|주소", "개업|등록|업태");
                if (fullAddr.contains(",")) {
                    int idx = fullAddr.indexOf(",");
                    etAddress.setText(fullAddr.substring(0, idx).trim());
                    etDAddress.setText(fullAddr.substring(idx + 1).trim());
                } else {
                    etAddress.setText(fullAddr.trim());
                    etDAddress.setText("");
                }
            }
            if (cleanText.contains("업태") || (cleanText.contains("업") && cleanText.contains("태"))) {
                String val = row.getValueAfter("업태|업\\s*태|업|태", "종목|개업|등록");
                if (val.isEmpty() && i + 1 < rows.size()) {
                    LogicalRow nextRow = rows.get(i+1);
                    if (!nextRow.isAnyLabel(nextRow.getMergedText().replace(" ",""), "업태")) val = nextRow.getMergedText();
                }
                etCustType.setText(val);
                bizTypeRowIdx = i;
            }

            // 4. 종목 인식 및 병합
            boolean isItemLabel = cleanText.contains("종목") || cleanText.contains("No목") || cleanText.equals("목") || (cleanText.contains("종") && cleanText.contains("목"));
            if (isItemLabel || (bizTypeRowIdx != -1 && i > bizTypeRowIdx && etCustKind.getText().length() == 0)) {
                String val = isItemLabel ? row.getValueAfter("종목|No목|종\\s*목|목", "발급|증명|개업") : "";
                if (val.isEmpty() && !row.isAnyLabel(cleanText, "종목")) val = rawText;

                StringBuilder sb = new StringBuilder(val);
                for (int k = 1; k <= 3; k++) {
                    if (i + k >= rows.size()) break;
                    LogicalRow nextRow = rows.get(i + k);
                    String nt = nextRow.getMergedText();
                    if (!nextRow.isAnyLabel(nt.replace(" ", ""), "종목")) {
                        if (sb.length() > 0) sb.append(" ");
                        sb.append(nt);
                    } else break;
                }
                etCustKind.setText(sb.toString().trim());
                if (etCustKind.getText().length() > 0) isFinished = true;
            }

            if (cleanText.contains("년") && cleanText.contains("월") && cleanText.contains("일")) {
                Matcher m = Pattern.compile("\\d{4}\\s*년\\s*\\d{1,2}\\s*월\\s*\\d{1,2}\\s*일").matcher(rawText);
                if (m.find() && (cleanText.contains("등록") || etRegDate.getText().length() == 0)) {
                    etRegDate.setText(m.group());
                }
            }
        }

        // 번호 패턴 매칭 보강 (사업자등록번호, 법인등록번호)
        String totalText = visionText.getText();
        extractRegex(totalText, "\\d{3}-\\d{2}-\\d{5}", etCustNo);
        extractRegex(totalText, "\\d{6}-\\d{7}", etCorpRegNo);
    }

    private void extractRegex(String text, String regex, EditText target) {
        if (target.getText().length() > 0) return;
        Matcher m = Pattern.compile(regex).matcher(text);
        if (m.find()) target.setText(m.group());
    }

    private void openCustSearchPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_customer_search, null);
        builder.setTitle("거래처 검색").setView(dialogView);
        EditText etSearch = dialogView.findViewById(R.id.etSearchQuery);
        RecyclerView rv = dialogView.findViewById(R.id.rvPopupList);
        rv.setLayoutManager(new LinearLayoutManager(this));
        List<Map<String, Object>> list = new ArrayList<>();
        AlertDialog dialog = builder.create();
        PopupAdapter popupAdapter = new PopupAdapter(list, "CUST", item -> {
            loadDetail(item);
            dialog.dismiss();
        });
        rv.setAdapter(popupAdapter);
        dialogView.findViewById(R.id.btnSearch).setOnClickListener(v -> {
            Map<String, Object> p = new HashMap<>();
            p.put("actkind", "S0"); p.put("cmpycd", cmpycd); p.put("custnm", etSearch.getText().toString().trim());
            apiService.executeHsbaProcedure("HSBA_070U_STR", p).enqueue(new Callback<List<Map<String, Object>>>() {
                @Override public void onResponse(@NonNull Call<List<Map<String, Object>>> c, @NonNull Response<List<Map<String, Object>>> r) {
                    if (r.isSuccessful() && r.body() != null) {
                        list.clear(); list.addAll(r.body()); popupAdapter.notifyDataSetChanged();
                    }
                }
                @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> c, @NonNull Throwable t) {}
            });
        });
        dialogView.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void loadDetail(Map<String, Object> item) {
        currentActKind = "U0";
        etCustCd.setText(getStringVal(item, "custcd"));
        etCustCd.setEnabled(false);
        etCustNm.setText(getStringVal(item, "custnm"));
        etCustNo.setText(getStringVal(item, "custno"));
        etCorpRegNo.setText(getStringVal(item, "legalno"));
        etBossNm.setText(getStringVal(item, "bossnm"));
        etTelNo.setText(getStringVal(item, "telno"));
        etAddress.setText(getStringVal(item, "address"));
        etDAddress.setText(getStringVal(item, "d_address"));
        etCustType.setText(getStringVal(item, "custtype"));
        etCustKind.setText(getStringVal(item, "custkind"));
        
        String stdYmd = getStringVal(item, "stdymd");
        if (stdYmd.length() == 8) {
            etRegDate.setText(String.format("%s-%s-%s", stdYmd.substring(0, 4), stdYmd.substring(4, 6), stdYmd.substring(6, 8)));
        } else {
            etRegDate.setText(stdYmd);
        }

        swElcYn.setChecked("Y".equals(getStringVal(item, "elcyn")));
        swUseYn.setChecked("Y".equals(getStringVal(item, "useyn")));

        String status = getStringVal(item, "status");
        for (int i = 0; i < statusOptions.size(); i++) {
            if (statusOptions.get(i).codecd.equals(status)) { spStatus.setSelection(i); break; }
        }
    }

    private void save() {
        // 상호명 내 모든 공백 제거 규칙 적용
        String name = etCustNm.getText().toString().replace(" ", "").trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "거래처명(상호)을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        String rawDate = etRegDate.getText().toString();
        String cleanDate = rawDate.replaceAll("[^0-9]", "");
        if (cleanDate.isEmpty()) {
            cleanDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        }

        String statusVal = "010";
        if (spStatus.getSelectedItemPosition() >= 0 && spStatus.getSelectedItemPosition() < statusOptions.size()) {
            statusVal = statusOptions.get(spStatus.getSelectedItemPosition()).codecd;
        }

        Map<String, Object> p = new HashMap<>();
        p.put("actkind", currentActKind);
        p.put("cmpycd", cmpycd);
        p.put("custcd", etCustCd.getText().toString().trim());
        p.put("custno", etCustNo.getText().toString().trim().replace("-", ""));
        p.put("custnm", name);
        p.put("custgbn", "010");
        p.put("bossnm", etBossNm.getText().toString().trim());
        p.put("juminno", "");
        p.put("legalno", etCorpRegNo.getText().toString().trim().replace("-", ""));
        p.put("custkind", etCustKind.getText().toString().trim());
        p.put("custtype", etCustType.getText().toString().trim());
        p.put("telno", etTelNo.getText().toString().trim());
        p.put("faxno", "");
        p.put("postno", "");
        p.put("address", etAddress.getText().toString().trim());
        p.put("d_address", etDAddress.getText().toString().trim());
        p.put("iogbn", "010");
        p.put("inprcgbn", "190");
        p.put("outprcgbn", "200");
        p.put("hdamt", 0.0);
        p.put("rcvdd", 0.0);
        p.put("agrpcd", "000");
        p.put("bgrpcd", "000");
        p.put("cgrpcd", "000");
        p.put("area", "000");
        p.put("cdamdang", "");
        p.put("ctelno", "");
        p.put("cemail", "");
        p.put("remark", "");
        p.put("status", statusVal);
        p.put("banknm", "");
        p.put("gujoa", "");
        p.put("fndymd", "");
        p.put("stdymd", cleanDate);
        p.put("clsymd", "99991231");
        p.put("singrd", "A");
        p.put("elcyn", swElcYn.isChecked() ? "Y" : "N");
        p.put("useyn", swUseYn.isChecked() ? "Y" : "N");
        p.put("updemp", userid);
        p.put("gigbcd", "305");
        p.put("outcustcd", "");

        apiService.executeHsbaProcedure("HSBA_070U_STR", p).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MHSBA070U.this, "정상 처리되었습니다.", Toast.LENGTH_SHORT).show();
                    initialize();
                } else {
                    Toast.makeText(MHSBA070U.this, "저장 실패", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {
                Toast.makeText(MHSBA070U.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getStringVal(Map<String, Object> map, String key) {
        if (map == null) return "";
        Object val = map.get(key.toLowerCase());
        if (val == null) val = map.get(key.toUpperCase());
        return val != null ? String.valueOf(val).trim() : "";
    }

    @Override protected String getProgramTitle() { return "거래처 정보 등록"; }
    @Override protected String getProgramId() { return "MHSBA070U"; }

    private static class LogicalRow {
        int top, bottom;
        List<Text.Line> fragments = new ArrayList<>();

        LogicalRow(Text.Line line) {
            Rect b = line.getBoundingBox();
            if (b != null) {
                top = b.top; bottom = b.bottom;
            }
            fragments.add(line);
        }

        boolean isSameRow(Text.Line line) {
            Rect b = line.getBoundingBox();
            if (b == null) return false;
            return (b.centerY() >= top - 12 && b.centerY() <= bottom + 12);
        }

        void addFragment(Text.Line line) {
            fragments.add(line);
            Rect b = line.getBoundingBox();
            if (b != null) {
                top = Math.min(top, b.top);
                bottom = Math.max(bottom, b.bottom);
            }
        }

        String getMergedText() {
            Collections.sort(fragments, (f1, f2) -> {
                Rect b1 = f1.getBoundingBox(); Rect b2 = f2.getBoundingBox();
                int l1 = b1 != null ? b1.left : 0; int l2 = b2 != null ? b2.left : 0;
                return l1 - l2;
            });
            StringBuilder sb = new StringBuilder();
            for (Text.Line f : fragments) sb.append(f.getText().trim()).append(" ");
            return sb.toString().trim();
        }

        String getValueAfter(String labelPattern, String stopPattern) {
            Collections.sort(fragments, (f1, f2) -> {
                Rect b1 = f1.getBoundingBox(); Rect b2 = f2.getBoundingBox();
                int l1 = b1 != null ? b1.left : 0; int l2 = b2 != null ? b2.left : 0;
                return l1 - l2;
            });
            boolean found = false;
            StringBuilder sb = new StringBuilder();
            for (Text.Line f : fragments) {
                String txt = f.getText().trim();
                String clean = txt.replace(" ", "");
                if (found) {
                    if (isAnyLabel(clean, labelPattern)) break;
                    sb.append(txt).append(" ");
                } else if (clean.matches("(?i).*?(" + labelPattern + ").*")) {
                    found = true;
                    String rem = txt.replaceFirst("(?i).*?(" + labelPattern + ")", "").trim();
                    rem = rem.replaceFirst("(?i)^\\s*\\(?(법인명|대표자|사업장|소재지|법인|개인|명|자|태|목)\\s*\\)?", "").trim();
                    rem = rem.replaceFirst("^[\\s:;\\\\-\\\\|\\)\\]\\\\）\\\\[\\\\]\\\\)\\\\}]+", "").trim();
                    if (!rem.isEmpty()) sb.append(rem).append(" ");
                }
            }
            return sb.toString().trim();
        }

        boolean isAnyLabel(String cleanText, String currentPattern) {
            if (cleanText.length() <= 1) return false;
            if (currentPattern != null && cleanText.matches(".*(" + currentPattern.replace("|", "\\|") + ").*")) return false;
            return cleanText.matches(".*(상호|법인명|성명|대표|소재지|주소|업태|종목|번호|일자|등록|증명|개업|발급|교부).*");
        }
    }
}
