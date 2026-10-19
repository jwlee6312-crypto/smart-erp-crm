package com.crmbank.erp.mobile;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
 * 🚀 [InboundRegisterActivity] 모바일 통합 바코드 처리 (입고 100 / 출고 200)
 * 1. 입고/출고 의뢰번호 스캔 -> 정상 조회
 * 2. 품목코드 스캔 -> 스캔값 표시 -> HSIO104T_TBL 실시간 저장 -> 수량 +1 카운팅
 * 3. 재조회 시 DB(HSIO104T_TBL) 등록 수량 100% 매핑 표출
 */
public class InboundRegisterActivity extends AppCompatActivity {

    private Spinner spWarehouse;
    private TextView tvInboundDate;
    private EditText etOrderNo, etCustomerName, etInboundNo, etRemark;
    private ListView lvRegisterList;
    private Button btnSave;
    private ArrayAdapter<CodeDto> warehouseAdapter;
    private InboundRegisterAdapter listAdapter;
    private final List<Map<String, Object>> detailList = new ArrayList<>();
    private ApiService apiService;

    private String cmpycd = "COIT";
    private String userid = "";
    private String iogbnMode = "100"; // 100: 입고, 200: 출고
    private String currentIoym = "";
    private String currentIono = "";
    private String selectedItemCode = "";

    // 🎯 바코드 스캔 결과 런처
    private final ActivityResultLauncher<Intent> barcodeLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    String scannedValue = result.getData().getStringExtra("BARCODE_VALUE");
                    handleScannedBarcode(scannedValue);
                }
            });

    // 🎯 카메라 권한 요청 런처
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) openBarcodeScanner();
                else Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbound_register);

        if (getIntent() != null && getIntent().hasExtra("IOGBN_MODE")) {
            iogbnMode = getIntent().getStringExtra("IOGBN_MODE");
            if (iogbnMode == null || iogbnMode.trim().isEmpty()) iogbnMode = "100";
        }

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        cmpycd = prefs.getString("cmpycd", "COIT").trim();
        userid = prefs.getString("userId", "");

        apiService = RetrofitClient.getApiService();

        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        spWarehouse = findViewById(R.id.spWarehouse);
        tvInboundDate = findViewById(R.id.tvInboundDate);
        ImageButton btnScan = findViewById(R.id.btnScan);
        btnSave = findViewById(R.id.btnSave);
        Button btnReset = findViewById(R.id.btnReset);
        etOrderNo = findViewById(R.id.etOrderNo);
        etInboundNo = findViewById(R.id.etInboundNo);
        etCustomerName = findViewById(R.id.etCustomerName);
        etRemark = findViewById(R.id.etRemark);
        lvRegisterList = findViewById(R.id.lvRegisterList);

        if (tvHeaderTitle != null) {
            tvHeaderTitle.setText("200".equals(iogbnMode) ? "바코드 출고 처리" : "바코드 입고 처리");
        }

        listAdapter = new InboundRegisterAdapter(this, detailList);
        lvRegisterList.setAdapter(listAdapter);

        setupWarehouseSpinner();
        setupInboundDatePicker();
        resetFields();

        if (btnScan != null) {
            btnScan.setOnClickListener(v -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    openBarcodeScanner();
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA);
                }
            });
        }

        findViewById(R.id.btnSearchOrder).setOnClickListener(v -> {
            String val = etInboundNo.getText().toString().trim().replace("-", "");
            if (val.length() >= 10) {
                currentIoym = val.substring(0, 6);
                currentIono = val.substring(6);
                loadScannedHistory(currentIoym, currentIono);
            } else {
                showInboundOrderSearchDialog();
            }
        });

        if (btnReset != null) btnReset.setOnClickListener(v -> resetFields());
        if (btnSave != null) btnSave.setOnClickListener(v -> finishScanSession());
    }

    private void openBarcodeScanner() {
        Intent intent = new Intent(this, BarcodeScanActivity.class);
        barcodeLauncher.launch(intent);
    }

    /** 🚀 [10자리 의뢰번호 정규식 정밀 추출 vs 품목 바코드 수용 핸들러] */
    private void handleScannedBarcode(String value) {
        if (value == null || value.trim().isEmpty()) return;
        final String cleanValue = value.trim();
        String rawVal = cleanValue.replace("-", "").replaceAll("\\s+", "");

        // 💡 1. 의뢰번호 바코드 정규식 정밀 추출 ("202"로 시작하는 10자리 패턴 100% 포착)
        java.util.regex.Matcher reqMatcher = java.util.regex.Pattern.compile("(202\\d{7})").matcher(rawVal);
        if (reqMatcher.find()) {
            String req10 = reqMatcher.group(1);
            currentIoym = req10.substring(0, 6); // 202609
            currentIono = req10.substring(6);    // 0001
            etInboundNo.setText(String.format(Locale.getDefault(), "%s-%s", currentIoym, currentIono));
            loadScannedHistory(currentIoym, currentIono);
            Toast.makeText(this, ("200".equals(iogbnMode) ? "출고의뢰건 [" : "입고의뢰건 [") + currentIoym + "-" + currentIono + "] 선택 조회 완료!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 💡 2. 의뢰번호가 미선택 상태일 때 품목 바코드를 찍으면 의뢰서 먼저 선택 안내
        if (currentIoym.isEmpty() || currentIono.isEmpty()) {
            Toast.makeText(this, ("200".equals(iogbnMode) ? "출고의뢰서" : "입고의뢰서") + " 상단 바코드(10자리)를 먼저 스캔하세요.", Toast.LENGTH_LONG).show();
            return;
        }

        // 💡 3. 품목 바코드 (스캔처리 및 수량 +1 카운팅 증가)
        Toast.makeText(this, "스캔 바코드: [" + cleanValue + "]", Toast.LENGTH_SHORT).show();
        saveBarcodeScanToTable(cleanValue);
    }

    /** 🚀 [100% 정밀 1:1 품목/LOT 매칭] itemcd, barcode, lotno 시리얼 바코드 정밀 1:1 매칭 */
    private Map<String, Object> findMatchingItem(String scannedBarcode) {
        if (detailList.isEmpty() || scannedBarcode == null) return null;
        String cleanScan = scannedBarcode.replace("-", "").replaceAll("\\s+", "").toLowerCase(Locale.ROOT);

        // 1차: 정밀 1:1 완전 일치 (itemcd, barcode, 또는 lotno 시리얼)
        for (Map<String, Object> item : detailList) {
            String cd = getStringValue(item, "itemcd");
            String bc = getStringValue(item, "barcode");
            String lot = getStringValue(item, "lotno");
            if (scannedBarcode.equalsIgnoreCase(cd) || scannedBarcode.equalsIgnoreCase(bc) || scannedBarcode.equalsIgnoreCase(lot)) {
                return item;
            }
        }

        // 2차: 하이픈/공백 제거 후 1:1 완전 일치
        for (Map<String, Object> item : detailList) {
            String cd = getStringValue(item, "itemcd").replace("-", "").replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
            String bc = getStringValue(item, "barcode").replace("-", "").replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
            String lot = getStringValue(item, "lotno").replace("-", "").replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
            if ((!cd.isEmpty() && cleanScan.equals(cd)) || (!bc.isEmpty() && cleanScan.equals(bc)) || (!lot.isEmpty() && cleanScan.equals(lot))) {
                return item;
            }
        }

        // 3차: 화면에서 터치 선택 지정된 품목이 있는 경우 해당 지정 품목 1:1 매칭
        if (!selectedItemCode.isEmpty()) {
            for (Map<String, Object> item : detailList) {
                String cd = getStringValue(item, "itemcd");
                String bc = getStringValue(item, "barcode");
                if (selectedItemCode.equalsIgnoreCase(cd) || selectedItemCode.equalsIgnoreCase(bc)) {
                    return item;
                }
            }
        }

        // 🔴 미등록/불일치 바코드는 100% null 반환하여 차단! (예외 허용 전면 삭제)
        return null;
    }

    private void playErrorTone() {
        try {
            android.media.ToneGenerator toneGen = new android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 100);
            toneGen.startTone(android.media.ToneGenerator.TONE_PROP_BEEP2, 300);
        } catch (Exception ignored) {}
    }

    /** 🚀 [HSIO104T_TBL 실시간 저장] 의뢰서에 존재하는 유효 품목만 엄격하게 검증하여 DB 저장 (미등록 바코드 저장 차단 및 에러 다이얼로그 표출) */
    private void saveBarcodeScanToTable(String scannedBarcode) {
        Map<String, Object> matchedItem = findMatchingItem(scannedBarcode);
        if (matchedItem == null) {
            playErrorTone();
            new AlertDialog.Builder(this)
                    .setTitle("❌ 스캔 오류 안내")
                    .setMessage(String.format(Locale.getDefault(),
                            "현재 %s [%s-%s]에 해당하지 않는 미등록 품목 바코드입니다!\n\n스캔 바코드: [%s]\n\n해당 바코드는 DB에 저장되지 않습니다.",
                            ("200".equals(iogbnMode) ? "출고의뢰서" : "입고의뢰서"), currentIoym, currentIono, scannedBarcode))
                    .setPositiveButton("확인", (dialog, which) -> dialog.dismiss())
                    .show();
            return;
        }

        String matchedItemCd = getStringValue(matchedItem, "itemcd");
        if (matchedItemCd.isEmpty()) matchedItemCd = scannedBarcode;

        String itemSrowNo = getStringValue(matchedItem, "srowno");
        if (itemSrowNo.isEmpty()) itemSrowNo = getStringValue(matchedItem, "SROWNO");
        if (itemSrowNo.isEmpty()) {
            int idx = detailList.indexOf(matchedItem);
            itemSrowNo = String.format(Locale.getDefault(), "%03d", idx >= 0 ? idx + 1 : 1);
        }

        double currentScanCount = 0;
        try { currentScanCount = Double.parseDouble(String.valueOf(matchedItem.get("scan_qty"))); } catch (Exception ignored) {}
        String seq6 = String.format(Locale.getDefault(), "%06d", (int) currentScanCount + 1);

        String autoLotNo = String.format(Locale.getDefault(), "%s%s%s%s", currentIoym, currentIono, itemSrowNo, seq6);

        Map<String, Object> param = new HashMap<>();
        param.put("cmpycd", cmpycd);
        param.put("iogbn", iogbnMode);
        param.put("ioym", currentIoym);
        param.put("iono", currentIono);
        param.put("srowno", itemSrowNo);
        param.put("itemcd", matchedItemCd);
        param.put("barcode", scannedBarcode);

        if ("200".equals(iogbnMode)) {
            param.put("lotno", scannedBarcode);
        } else {
            if (scannedBarcode.isEmpty() || scannedBarcode.equalsIgnoreCase(matchedItemCd)) {
                param.put("lotno", autoLotNo);
            } else {
                param.put("lotno", scannedBarcode);
            }
        }
        param.put("scan_qty", 1.0);
        param.put("orderno", etOrderNo.getText().toString().trim());
        param.put("updemp", userid);

        apiService.saveBarcodeScanHistory(param).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call,
                                   @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful()) {
                    String assignedLot = String.valueOf(param.get("lotno"));
                    Map<String, Object> target = findMatchingItem(scannedBarcode);
                    if (target != null) {
                        double cur = 0;
                        try { cur = Double.parseDouble(String.valueOf(target.get("scan_qty"))); } catch (Exception ignored) {}
                        target.put("scan_qty", cur + 1.0);
                        target.put("SCAN_QTY", cur + 1.0);
                        target.put("lotno", assignedLot);
                        target.put("LOTNO", assignedLot);
                    }

                    runOnUiThread(() -> {
                        if (listAdapter != null) listAdapter.notifyDataSetChanged();
                    });
                    Toast.makeText(InboundRegisterActivity.this, "바코드 [" + scannedBarcode + "] 스캔 완료 (+1)", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(InboundRegisterActivity.this, "스캔 저장 실패", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {
                Toast.makeText(InboundRegisterActivity.this, "통신 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** 🚀 [웹 소스 HSIO104U.vue와 100% 동일] 마스터(S2) & 디테일(S0) 독립 고속 조회 및 HSIO104T_TBL 스캔 수량 매핑 */
    private void loadScannedHistory(String ioym, String iono) {
        String proc = "200".equals(iogbnMode) ? "HSIO_620S_STR" : "HSIO_215S_STR";

        // 1. 마스터 정보 독립 조회 (hsio_215s_str 'S2')
        Map<String, Object> pMst = new HashMap<>();
        pMst.put("actkind", "S2");
        pMst.put("cmpycd", cmpycd);
        pMst.put("iogbn", iogbnMode); // 100: 입고, 200: 출고
        pMst.put("whcd", "000");
        pMst.put("fromdt", "20260101");
        pMst.put("todt", "20261231");
        pMst.put("custcd", "0000000");
        pMst.put("ioym", ioym);
        pMst.put("iono", iono);
        pMst.put("slipyn", "Y");

        apiService.executeHsioProcedure(proc, pMst).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Map<String, Object> m = response.body().get(0);
                    String custNm = getStringValue(m, "custnm");
                    if (custNm.isEmpty()) custNm = getStringValue(m, "ccustnm");
                    if (custNm.isEmpty()) custNm = getStringValue(m, "cust_nm");
                    etCustomerName.setText(custNm);

                    String ymd = getStringValue(m, "ioymd");
                    if (ymd.length() == 8) {
                        tvInboundDate.setText(String.format(Locale.getDefault(), "%s-%s-%s", ymd.substring(0, 4), ymd.substring(4, 6), ymd.substring(6, 8)));
                    }
                    if (m.get("remark") != null) etRemark.setText(getStringValue(m, "remark"));

                    String whCd = getStringValue(m, "whcd");
                    String whNm = getStringValue(m, "whnm");
                    if (spWarehouse != null && warehouseAdapter != null) {
                        boolean found = false;
                        for (int idx = 0; idx < warehouseAdapter.getCount(); idx++) {
                            CodeDto dto = warehouseAdapter.getItem(idx);
                            if (dto != null && ((!whCd.isEmpty() && whCd.equalsIgnoreCase(dto.getCodecd())) ||
                                                (!whNm.isEmpty() && whNm.equalsIgnoreCase(dto.getCodenm())))) {
                                spWarehouse.setSelection(idx);
                                found = true;
                                break;
                            }
                        }
                        if (!found && (!whCd.isEmpty() || !whNm.isEmpty())) {
                            CodeDto customWh = new CodeDto();
                            customWh.codecd = whCd.isEmpty() ? "000" : whCd;
                            customWh.codenm = whNm.isEmpty() ? whCd : whNm;
                            warehouseAdapter.add(customWh);
                            spWarehouse.setSelection(warehouseAdapter.getCount() - 1);
                        }
                    }
                }
            }
            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {}
        });

        // 2. 상세 품목 목록 독립 조회 (hsio_215s_str 'S0') - 웹 HSIO104U.vue와 100% 동일 독립 병렬 호출!
        Map<String, Object> pDtl = new HashMap<>(pMst);
        pDtl.put("actkind", "S0");

        apiService.executeHsioProcedure(proc, pDtl).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    detailList.clear();
                    detailList.addAll(response.body());

                    // 3. HSIO104T_TBL 실스캔 수량 및 최신 LOT 매핑
                    Map<String, Object> pScan = new HashMap<>();
                    pScan.put("cmpycd", cmpycd);
                    pScan.put("iogbn", iogbnMode);
                    pScan.put("ioym", ioym);
                    pScan.put("iono", iono);

                    apiService.getBarcodeScanHistory(pScan).enqueue(new Callback<List<Map<String, Object>>>() {
                        @Override
                        public void onResponse(@NonNull Call<List<Map<String, Object>>> call2, @NonNull Response<List<Map<String, Object>>> response2) {
                            if (response2.isSuccessful() && response2.body() != null) {
                                Map<String, Double> scanMap = new HashMap<>();
                                Map<String, String> lotMap = new HashMap<>();

                                for (Map<String, Object> scanRow : response2.body()) {
                                    String cd = getStringValue(scanRow, "itemcd").toUpperCase(Locale.ROOT).trim();
                                    String bc = getStringValue(scanRow, "barcode").toUpperCase(Locale.ROOT).trim();
                                    String lot = getStringValue(scanRow, "lotno");
                                    double qty = 0;
                                    try { qty = Double.parseDouble(String.valueOf(scanRow.get("scan_qty"))); } catch (Exception ignored) {}
                                    if (qty <= 0) qty = 1.0;

                                    if (!cd.isEmpty()) {
                                        scanMap.put(cd, (scanMap.containsKey(cd) ? scanMap.get(cd) : 0.0) + qty);
                                        if (!lot.isEmpty()) lotMap.put(cd, lot);
                                    }
                                    if (!bc.isEmpty() && !bc.equals(cd)) {
                                        scanMap.put(bc, (scanMap.containsKey(bc) ? scanMap.get(bc) : 0.0) + qty);
                                        if (!lot.isEmpty()) lotMap.put(bc, lot);
                                    }
                                }

                                for (Map<String, Object> item : detailList) {
                                    String cd = getStringValue(item, "itemcd").toUpperCase(Locale.ROOT).trim();
                                    String bc = getStringValue(item, "barcode").toUpperCase(Locale.ROOT).trim();
                                    Double qtyByCd = !cd.isEmpty() ? scanMap.get(cd) : null;
                                    Double qtyByBc = !bc.isEmpty() ? scanMap.get(bc) : null;
                                    double scannedTotal = qtyByCd != null ? qtyByCd : (qtyByBc != null ? qtyByBc : 0.0);
                                    String latestLot = lotMap.get(cd);
                                    if (latestLot == null || latestLot.isEmpty()) latestLot = lotMap.get(bc);

                                    item.put("scan_qty", scannedTotal);
                                    item.put("SCAN_QTY", scannedTotal);
                                    if (latestLot != null && !latestLot.isEmpty()) {
                                        item.put("lotno", latestLot);
                                        item.put("LOTNO", latestLot);
                                    }
                                }
                            }
                            runOnUiThread(() -> {
                                if (listAdapter != null) listAdapter.notifyDataSetChanged();
                            });
                        }
                        @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call2, @NonNull Throwable t2) {
                            runOnUiThread(() -> {
                                if (listAdapter != null) listAdapter.notifyDataSetChanged();
                            });
                        }
                    });
                }
            }
            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {}
        });
    }

    private void finishScanSession() {
        if (currentIoym.isEmpty() || currentIono.isEmpty() || detailList.isEmpty()) {
            Toast.makeText(this, "스캔 내역이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, ("200".equals(iogbnMode) ? "출고검수 바코드 스캔이" : "입고검수 바코드 스캔이") + " 완료되었습니다.", Toast.LENGTH_SHORT).show();
        if (btnSave != null) {
            btnSave.setEnabled(false);
            btnSave.setAlpha(0.5f);
        }
    }

    private void resetFields() {
        if (etOrderNo != null) etOrderNo.setText("");
        if (etInboundNo != null) etInboundNo.setText("");
        if (etCustomerName != null) etCustomerName.setText("");
        if (etRemark != null) etRemark.setText("");
        if (tvInboundDate != null) tvInboundDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        currentIoym = ""; currentIono = "";
        detailList.clear();
        if (listAdapter != null) listAdapter.notifyDataSetChanged();
        if (btnSave != null) {
            btnSave.setEnabled(true);
            btnSave.setAlpha(1.0f);
        }
    }

    private String getStringValue(Map<String, Object> map, String key) {
        if (map == null || key == null) return "";
        Object val = map.get(key);
        if (val == null) val = map.get(key.toUpperCase(Locale.ROOT));
        if (val == null) val = map.get(key.toLowerCase(Locale.ROOT));
        if (val == null) return "";
        String str = String.valueOf(val).trim();
        if (str.endsWith(".0")) {
            str = str.substring(0, str.length() - 2);
        }
        return str;
    }

    private void showInboundOrderSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_purch_order_search, null);

        TextView tvPopTitle = dialogView.findViewById(R.id.tvPopTitle);
        TextView tvPopDateLabel = dialogView.findViewById(R.id.tvPopDateLabel);
        TextView tvPopColYmd = dialogView.findViewById(R.id.tvPopColYmd);
        TextView tvPopColNo = dialogView.findViewById(R.id.tvPopColNo);

        boolean isOutbound = "200".equals(iogbnMode);
        String termText = isOutbound ? "출고" : "입고";

        if (tvPopTitle != null) tvPopTitle.setText(termText + "의뢰번호 검색");
        if (tvPopDateLabel != null) tvPopDateLabel.setText(termText + "의뢰기간");
        if (tvPopColYmd != null) tvPopColYmd.setText(termText + "일");
        if (tvPopColNo != null) tvPopColNo.setText(termText + "의뢰번호");

        builder.setTitle(termText + "의뢰번호 검색").setView(dialogView);

        TextView tvPopStartDate = dialogView.findViewById(R.id.tvPopStartDate);
        TextView tvPopEndDate = dialogView.findViewById(R.id.tvPopEndDate);
        EditText etPopCustNm = dialogView.findViewById(R.id.etPopCustNm);
        Button btnPopSearch = dialogView.findViewById(R.id.btnPopSearch);
        ListView lv = dialogView.findViewById(R.id.lvPopOrderList);

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        if (tvPopEndDate != null) tvPopEndDate.setText(sdf.format(cal.getTime()));
        cal.add(Calendar.MONTH, -3); // 💡 최근 3개월 데이터 100% 포괄 검색되도록 3달 전 1일로 기본 시작일 설정
        cal.set(Calendar.DAY_OF_MONTH, 1);
        if (tvPopStartDate != null) tvPopStartDate.setText(sdf.format(cal.getTime()));

        // 💡 [조회기간 클릭 달력 팝업 수정을 위한 DatePickerDialog 연동]
        if (tvPopStartDate != null) {
            tvPopStartDate.setOnClickListener(v -> {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(this, (view, y, m, d) ->
                        tvPopStartDate.setText(String.format(Locale.getDefault(), "%d-%02d-%02d", y, m + 1, d)),
                        c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            });
        }
        if (tvPopEndDate != null) {
            tvPopEndDate.setOnClickListener(v -> {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(this, (view, y, m, d) ->
                        tvPopEndDate.setText(String.format(Locale.getDefault(), "%d-%02d-%02d", y, m + 1, d)),
                        c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            });
        }

        List<Map<String, Object>> popList = new ArrayList<>();
        InboundOrderPopAdapter popAdapter = new InboundOrderPopAdapter(popList);
        if (lv != null) lv.setAdapter(popAdapter);

        AlertDialog dialog = builder.create();

        if (btnPopSearch != null) {
            btnPopSearch.setOnClickListener(v -> {
                String proc = "200".equals(iogbnMode) ? "HSIO_620S_STR" : "HSIO_215S_STR";
                Map<String, Object> p = new HashMap<>();
                p.put("actkind", "S1"); // 💡 유저 지침: 입고의뢰 팝업 리스트는 hsio_215s_str 'S1' 사용
                p.put("cmpycd", cmpycd);
                p.put("iogbn", iogbnMode);
                p.put("whcd", "000");
                p.put("custcd", "0000000");
                p.put("ioym", "");
                p.put("iono", "");
                p.put("slipyn", "Y"); // 💡 [유저 지침 100% 반영] slipyn = 'Y' 파라미터 적용하여 DB 의뢰 내역 100% 정상 조회!

                if (tvPopStartDate != null) p.put("fromdt", tvPopStartDate.getText().toString().replace("-", ""));
                if (tvPopEndDate != null) p.put("todt", tvPopEndDate.getText().toString().replace("-", ""));
                if (etPopCustNm != null) p.put("custnm", etPopCustNm.getText().toString().trim());

                apiService.executeHsioProcedure(proc, p).enqueue(new Callback<List<Map<String, Object>>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                        popList.clear();
                        if (response.isSuccessful() && response.body() != null) {
                            popList.addAll(response.body());
                        }
                        popAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {
                        popList.clear();
                        popAdapter.notifyDataSetChanged();
                    }
                });
            });
        }

        if (lv != null) {
            lv.setOnItemClickListener((parent, view, position, id) -> {
                Map<String, Object> selected = popList.get(position);
                String ym = getStringValue(selected, "ioym");
                String no = getStringValue(selected, "iono");
                if (!ym.isEmpty() && !no.isEmpty()) {
                    currentIoym = ym;
                    currentIono = no;
                    etInboundNo.setText(String.format(Locale.getDefault(), "%s-%s", currentIoym, currentIono));
                    dialog.dismiss();
                    loadScannedHistory(currentIoym, currentIono);
                }
            });
        }

        View btnClose = dialogView.findViewById(R.id.btnPopClose);
        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
        if (btnPopSearch != null) btnPopSearch.performClick();
    }

    private void setupWarehouseSpinner() {
        warehouseAdapter = new ArrayAdapter<>(this, R.layout.item_popup_list, new ArrayList<>());
        if (spWarehouse != null) spWarehouse.setAdapter(warehouseAdapter);
        apiService.getCommonCode(cmpycd, "KOR", "030").enqueue(new Callback<List<CodeDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<CodeDto>> call, @NonNull Response<List<CodeDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    warehouseAdapter.addAll(response.body());
                    warehouseAdapter.notifyDataSetChanged();
                }
            }
            @Override public void onFailure(@NonNull Call<List<CodeDto>> call, @NonNull Throwable t) {}
        });
    }

    private void setupInboundDatePicker() {
        if (tvInboundDate != null) {
            tvInboundDate.setOnClickListener(v -> {
                Calendar cal = Calendar.getInstance();
                new DatePickerDialog(this, (view, y, m, d) -> 
                    tvInboundDate.setText(String.format(Locale.getDefault(), "%d-%02d-%02d", y, m + 1, d)), 
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
            });
        }
    }

    private class InboundOrderPopAdapter extends BaseAdapter {
        private final List<Map<String, Object>> items;
        public InboundOrderPopAdapter(List<Map<String, Object>> items) { this.items = items; }
        @Override public int getCount() { return items.size(); }
        @Override public Object getItem(int p) { return items.get(p); }
        @Override public long getItemId(int p) { return p; }
        @Override public View getView(int p, View v, ViewGroup pr) {
            View rowView = v;
            if (rowView == null) rowView = LayoutInflater.from(pr.getContext()).inflate(R.layout.item_request_search, pr, false);
            Map<String, Object> i = items.get(p);
            String cNm = getStringValue(i, "custnm");
            if (cNm.isEmpty()) cNm = getStringValue(i, "ccustnm");
            String ymd = getStringValue(i, "ioymd");
            String num = String.format(Locale.getDefault(), "%s-%s", getStringValue(i, "ioym"), getStringValue(i, "iono"));

            TextView tvDept = rowView.findViewById(R.id.tvPopDeptNm);
            TextView tvYmd = rowView.findViewById(R.id.tvPopReqYmd);
            TextView tvNo = rowView.findViewById(R.id.tvPopReqNo);

            if (tvDept != null) tvDept.setText(cNm);
            if (tvYmd != null) tvYmd.setText(ymd);
            if (tvNo != null) tvNo.setText(num);

            return rowView;
        }
    }
}
