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
 * 🚀 [OutboundRegisterActivity] 모바일 출고 전용 바코드 스캔 처리 (MHSIO204U 전용)
 * 1. 출고의뢰번호 스캔 -> 정상 조회
 * 2. 품목코드/시리얼 스캔 -> HSIO104T_TBL 실시간 출고 저장 -> 수량 카운팅
 * 3. 출고 전용 레이아웃 activity_outbound_register.xml 및 item_outbound_register.xml 사용
 */
public class OutboundRegisterActivity extends AppCompatActivity {

    private Spinner spWarehouse;
    private TextView tvOutboundDate;
    private EditText etOutboundNo, etCustomerName, etOrderNo, etRemark;
    private ListView lvRegisterList;
    private Button btnSave;
    private ArrayAdapter<CodeDto> warehouseAdapter;
    private OutboundRegisterAdapter listAdapter;
    private final List<Map<String, Object>> detailList = new ArrayList<>();
    private ApiService apiService;

    private String cmpycd = "COIT";
    private String userid = "";
    private final String iogbnMode = "200"; // 200: 출고 전용
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
        setContentView(R.layout.activity_outbound_register);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        cmpycd = prefs.getString("cmpycd", "COIT").trim();
        userid = prefs.getString("userId", "");

        apiService = RetrofitClient.getApiService();

        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        spWarehouse = findViewById(R.id.spWarehouse);
        tvOutboundDate = findViewById(R.id.tvOutboundDate);
        ImageButton btnScan = findViewById(R.id.btnScan);
        btnSave = findViewById(R.id.btnSave);
        Button btnReset = findViewById(R.id.btnReset);
        etOutboundNo = findViewById(R.id.etOutboundNo);
        etOrderNo = findViewById(R.id.etOrderNo);
        etCustomerName = findViewById(R.id.etCustomerName);
        etRemark = findViewById(R.id.etRemark);
        lvRegisterList = findViewById(R.id.lvRegisterList);

        if (tvHeaderTitle != null) {
            tvHeaderTitle.setText("바코드 출고 처리");
        }

        listAdapter = new OutboundRegisterAdapter(this, detailList);
        lvRegisterList.setAdapter(listAdapter);

        // 오늘 날짜 기본값 세팅
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        tvOutboundDate.setText(today);
        tvOutboundDate.setOnClickListener(v -> showDatePickerDialog());

        btnScan.setOnClickListener(v -> checkCameraPermissionAndScan());
        findViewById(R.id.btnSearchOrder).setOnClickListener(v -> showOutboundOrderSearchPopup());
        
        if (btnReset != null) btnReset.setOnClickListener(v -> resetFields());
        if (btnSave != null) btnSave.setOnClickListener(v -> finishScanSession());

        loadWarehouses();
    }

    private void openBarcodeScanner() {
        Intent intent = new Intent(this, BarcodeScanActivity.class);
        barcodeLauncher.launch(intent);
    }

    private void handleScannedBarcode(String value) {
        if (value == null || value.trim().isEmpty()) return;
        final String cleanValue = value.trim();
        String rawVal = cleanValue.replace("-", "").replaceAll("\\s+", "");

        // 💡 1. 출고의뢰번호 바코드 정규식 정밀 추출 ("202"로 시작하는 10자리 패턴)
        java.util.regex.Matcher reqMatcher = java.util.regex.Pattern.compile("(202\\d{7})").matcher(rawVal);
        if (reqMatcher.find()) {
            String req10 = reqMatcher.group(1);
            currentIoym = req10.substring(0, 6);
            currentIono = req10.substring(6);
            etOutboundNo.setText(String.format(Locale.getDefault(), "%s-%s", currentIoym, currentIono));
            loadScannedHistory(currentIoym, currentIono);
            Toast.makeText(this, "출고의뢰건 [" + currentIoym + "-" + currentIono + "] 선택 조회 완료!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 💡 2. 의뢰번호가 미선택 상태일 때 품목 바코드를 찍으면 의뢰서 먼저 선택 안내
        if (currentIoym.isEmpty() || currentIono.isEmpty()) {
            Toast.makeText(this, "출고의뢰서 상단 바코드(10자리)를 먼저 스캔하세요.", Toast.LENGTH_LONG).show();
            return;
        }

        // 💡 3. 품목 바코드 (스캔처리 및 수량 +1 카운팅 증가)
        Toast.makeText(this, "스캔 바코드: [" + cleanValue + "]", Toast.LENGTH_SHORT).show();
        saveBarcodeScanToTable(cleanValue);
    }

    private Map<String, Object> findMatchingItem(String scannedBarcode) {
        if (detailList.isEmpty() || scannedBarcode == null) return null;
        String cleanScan = scannedBarcode.replace("-", "").replaceAll("\\s+", "").toLowerCase(Locale.ROOT);

        for (Map<String, Object> item : detailList) {
            String cd = getStringVal(item, "itemcd");
            String bc = getStringVal(item, "barcode");
            String lot = getStringVal(item, "lotno");
            if (scannedBarcode.equalsIgnoreCase(cd) || scannedBarcode.equalsIgnoreCase(bc) || scannedBarcode.equalsIgnoreCase(lot)) {
                return item;
            }
        }

        for (Map<String, Object> item : detailList) {
            String cd = getStringVal(item, "itemcd").replace("-", "").replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
            String bc = getStringVal(item, "barcode").replace("-", "").replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
            String lot = getStringVal(item, "lotno").replace("-", "").replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
            if ((!cd.isEmpty() && cleanScan.equals(cd)) || (!bc.isEmpty() && cleanScan.equals(bc)) || (!lot.isEmpty() && cleanScan.equals(lot))) {
                return item;
            }
        }

        return null;
    }

    private void playErrorTone() {
        try {
            android.media.ToneGenerator toneGen = new android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 100);
            toneGen.startTone(android.media.ToneGenerator.TONE_PROP_BEEP2, 300);
        } catch (Exception ignored) {}
    }

    private void saveBarcodeScanToTable(String scannedBarcode) {
        Map<String, Object> matchedItem = findMatchingItem(scannedBarcode);
        if (matchedItem == null) {
            playErrorTone();
            new AlertDialog.Builder(this)
                    .setTitle("❌ 출고 스캔 오류 안내")
                    .setMessage(String.format(Locale.getDefault(),
                            "현재 출고의뢰서 [%s-%s]에 해당하지 않는 미등록 품목 바코드입니다!\n\n스캔 바코드: [%s]\n\n해당 바코드는 DB에 저장되지 않습니다.",
                            currentIoym, currentIono, scannedBarcode))
                    .setPositiveButton("확인", (dialog, which) -> dialog.dismiss())
                    .show();
            return;
        }

        String matchedItemCd = getStringVal(matchedItem, "itemcd");
        if (matchedItemCd.isEmpty()) matchedItemCd = scannedBarcode;

        String itemSrowNo = getStringVal(matchedItem, "srowno");
        if (itemSrowNo.isEmpty()) itemSrowNo = getStringVal(matchedItem, "SROWNO");
        if (itemSrowNo.isEmpty()) {
            int idx = detailList.indexOf(matchedItem);
            itemSrowNo = String.format(Locale.getDefault(), "%03d", idx >= 0 ? idx + 1 : 1);
        }

        Map<String, Object> param = new HashMap<>();
        param.put("cmpycd", cmpycd);
        param.put("iogbn", iogbnMode);
        param.put("ioym", currentIoym);
        param.put("iono", currentIono);
        param.put("srowno", itemSrowNo);
        param.put("itemcd", matchedItemCd);
        param.put("barcode", scannedBarcode);
        param.put("lotno", scannedBarcode);
        param.put("scan_qty", 1.0);
        param.put("orderno", etOrderNo.getText().toString().trim());
        param.put("updemp", userid);

        apiService.saveBarcodeScanHistory(param).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call,
                                   @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful()) {
                    Map<String, Object> target = findMatchingItem(scannedBarcode);
                    if (target != null) {
                        double cur = 0;
                        try { cur = Double.parseDouble(String.valueOf(target.get("scan_qty"))); } catch (Exception ignored) {}
                        target.put("scan_qty", cur + 1.0);
                        target.put("SCAN_QTY", cur + 1.0);
                        target.put("lotno", scannedBarcode);
                        target.put("LOTNO", scannedBarcode);
                    }

                    runOnUiThread(() -> {
                        if (listAdapter != null) listAdapter.notifyDataSetChanged();
                    });
                    Toast.makeText(OutboundRegisterActivity.this, "출고 바코드 [" + scannedBarcode + "] 스캔 완료 (+1)", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(OutboundRegisterActivity.this, "스캔 저장 실패", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {
                Toast.makeText(OutboundRegisterActivity.this, "통신 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadScannedHistory(String ioym, String iono) {
        Map<String, Object> pMst = new HashMap<>();
        pMst.put("actkind", "S2");
        pMst.put("cmpycd", cmpycd);
        pMst.put("iogbn", iogbnMode);
        pMst.put("whcd", "000");
        pMst.put("fromdt", "20260101");
        pMst.put("todt", "20261231");
        pMst.put("custcd", "0000000");
        pMst.put("ioym", ioym);
        pMst.put("iono", iono);
        pMst.put("slipyn", "Y");

        apiService.executeHsioProcedure("HSIO_620S_STR", pMst).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Map<String, Object> m = response.body().get(0);
                    String custNm = getStringVal(m, "custnm");
                    if (custNm.isEmpty()) custNm = getStringVal(m, "ccustnm");
                    if (custNm.isEmpty()) custNm = getStringVal(m, "cust_nm");
                    etCustomerName.setText(custNm);

                    String ymd = getStringVal(m, "ioymd");
                    if (ymd.length() == 8) {
                        tvOutboundDate.setText(String.format(Locale.getDefault(), "%s-%s-%s", ymd.substring(0, 4), ymd.substring(4, 6), ymd.substring(6, 8)));
                    }
                    if (m.get("remark") != null) etRemark.setText(getStringVal(m, "remark"));

                    String whCd = getStringVal(m, "whcd");
                    String whNm = getStringVal(m, "whnm");
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

        Map<String, Object> pDtl = new HashMap<>(pMst);
        pDtl.put("actkind", "S0");

        apiService.executeHsioProcedure("HSIO_620S_STR", pDtl).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    detailList.clear();
                    detailList.addAll(response.body());

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
                                    String cd = getStringVal(scanRow, "itemcd").toUpperCase(Locale.ROOT).trim();
                                    String bc = getStringVal(scanRow, "barcode").toUpperCase(Locale.ROOT).trim();
                                    String lot = getStringVal(scanRow, "lotno");
                                    double qty = 0;
                                    try { qty = Double.parseDouble(String.valueOf(scanRow.get("scan_qty"))); } catch (Exception ignored) {}
                                    if (qty <= 0) qty = 1.0;

                                    if (!cd.isEmpty()) {
                                        scanMap.put(cd, (scanMap.containsKey(cd) ? scanMap.get(cd) : 0.0) + qty);
                                        if (!lot.isEmpty()) lotMap.put(cd, lot);
                                    }
                                    if (!bc.isEmpty() && !bc.equals(cd)) {
                                        scanMap.put(bc, (scanMap.containsKey(bc) ? scanMap.get(bc) : 0.0) + qty);
                                        if (!lot.isEmpty()) lotMap.put(cd, lot);
                                    }
                                }

                                for (Map<String, Object> item : detailList) {
                                    String cd = getStringVal(item, "itemcd").toUpperCase(Locale.ROOT).trim();
                                    String bc = getStringVal(item, "barcode").toUpperCase(Locale.ROOT).trim();
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

    private void showOutboundOrderSearchPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_purch_order_search, null);
        builder.setView(dialogView);

        EditText etSearch = dialogView.findViewById(R.id.etPopCustNm);
        ListView lvOrders = dialogView.findViewById(R.id.lvPopOrderList);
        List<Map<String, Object>> orderList = new ArrayList<>();

        BaseAdapter adapter = new BaseAdapter() {
            @Override public int getCount() { return orderList.size(); }
            @Override public Object getItem(int p) { return orderList.get(p); }
            @Override public long getItemId(int p) { return p; }
            @Override public View getView(int p, View v, ViewGroup parent) {
                if (v == null) v = LayoutInflater.from(OutboundRegisterActivity.this).inflate(android.R.layout.simple_list_item_2, parent, false);
                Map<String, Object> dto = orderList.get(p);
                TextView t1 = v.findViewById(android.R.id.text1);
                TextView t2 = v.findViewById(android.R.id.text2);

                String ym = getStringVal(dto, "ioym");
                String no = getStringVal(dto, "iono");
                String cust = getStringVal(dto, "custnm");
                String ymd = getStringVal(dto, "ioymd");
                String wh = getStringVal(dto, "whnm");

                t1.setText(String.format("[%s-%s] %s", ym, no, cust));
                t2.setText(String.format("일자: %s | 창고: %s", ymd, wh));
                return v;
            }
        };
        if (lvOrders != null) lvOrders.setAdapter(adapter);

        AlertDialog dialog = builder.create();

        if (dialogView.findViewById(R.id.btnSearch) != null) {
            dialogView.findViewById(R.id.btnSearch).setOnClickListener(v -> {
                String query = etSearch != null ? etSearch.getText().toString().trim() : "";
                Map<String, Object> p = new HashMap<>();
                p.put("actkind", "S2");
                p.put("cmpycd", cmpycd);
                p.put("iogbn", iogbnMode);
                p.put("whcd", "000");
                p.put("fromdt", "20260101");
                p.put("todt", "20261231");
                p.put("custcd", "0000000");
                p.put("custnm", query);
                p.put("slipyn", "Y");

                apiService.executeHsioProcedure("HSIO_620S_STR", p).enqueue(new Callback<List<Map<String, Object>>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            orderList.clear();
                            orderList.addAll(response.body());
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(OutboundRegisterActivity.this, "조회된 출고의뢰건이 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {
                        Toast.makeText(OutboundRegisterActivity.this, "통신 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        if (lvOrders != null) {
            lvOrders.setOnItemClickListener((parent, view, position, id) -> {
                Map<String, Object> selected = orderList.get(position);
                currentIoym = getStringVal(selected, "ioym");
                currentIono = getStringVal(selected, "iono");
                etOutboundNo.setText(String.format("%s-%s", currentIoym, currentIono));
                etCustomerName.setText(getStringVal(selected, "custnm"));
                loadScannedHistory(currentIoym, currentIono);
                dialog.dismiss();
            });
        }

        if (dialogView.findViewById(R.id.btnClose) != null) {
            dialogView.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
        if (dialogView.findViewById(R.id.btnSearch) != null) {
            dialogView.findViewById(R.id.btnSearch).performClick();
        }
    }

    private void finishScanSession() {
        if (currentIoym.isEmpty() || currentIono.isEmpty() || detailList.isEmpty()) {
            Toast.makeText(this, "스캔할 출고건이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "바코드 출고 스캔 처리가 완료되었습니다.", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void resetFields() {
        currentIoym = "";
        currentIono = "";
        selectedItemCode = "";
        if (etOutboundNo != null) etOutboundNo.setText("");
        if (etCustomerName != null) etCustomerName.setText("");
        if (etRemark != null) etRemark.setText("");
        detailList.clear();
        if (listAdapter != null) listAdapter.notifyDataSetChanged();
        Toast.makeText(this, "초기화되었습니다.", Toast.LENGTH_SHORT).show();
    }

    private void checkCameraPermissionAndScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openBarcodeScanner();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void showDatePickerDialog() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String formatted = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
            tvOutboundDate.setText(formatted);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadWarehouses() {
        Map<String, Object> p = new HashMap<>();
        p.put("gubun", "W0");
        p.put("cmpycd", cmpycd);

        apiService.executeHs00Procedure("HS00_000S_STR", p).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call, @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CodeDto> list = new ArrayList<>();
                    for (Map<String, Object> m : response.body()) {
                        CodeDto dto = new CodeDto();
                        dto.codecd = getStringVal(m, "code");
                        if (dto.codecd.isEmpty()) dto.codecd = getStringVal(m, "whcd");
                        dto.codenm = getStringVal(m, "cdnm");
                        if (dto.codenm.isEmpty()) dto.codenm = getStringVal(m, "whnm");
                        list.add(dto);
                    }
                    warehouseAdapter = new ArrayAdapter<>(OutboundRegisterActivity.this, android.R.layout.simple_spinner_item, list);
                    warehouseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spWarehouse.setAdapter(warehouseAdapter);
                }
            }
            @Override public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {}
        });
    }

    private String getStringVal(Map<String, Object> map, String key) {
        if (map == null || key == null) return "";
        Object val = map.get(key);
        if (val == null) val = map.get(key.toUpperCase(Locale.ROOT));
        if (val == null) val = map.get(key.toLowerCase(Locale.ROOT));
        return val != null ? String.valueOf(val).trim() : "";
    }
}
