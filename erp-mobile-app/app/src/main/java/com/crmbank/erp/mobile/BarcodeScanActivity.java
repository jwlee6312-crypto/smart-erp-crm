package com.crmbank.erp.mobile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 🚀 [BarcodeScanActivity] 사용자 확정 터치식 고속 바코드 스캐너
 * 1. 카메라 조준 시 감지된 바코드값을 배지(tvDetectedBarcode)에 실시간 포커스 표출
 * 2. 작업자가 [📸 스캔 확정하기 (클릭)] 누름 ➔ 삑! 비프음 ➔ 스캔 수량 +1 카운팅 확정!
 */
public class BarcodeScanActivity extends AppCompatActivity {

    private static final String TAG = "BarcodeScan";
    private PreviewView previewView;
    private TextView tvDetectedBarcode;
    private String lastDetectedVal = "";
    private final java.util.concurrent.atomic.AtomicBoolean isProcessing = new java.util.concurrent.atomic.AtomicBoolean(false);

    private ExecutorService cameraExecutor;
    private BarcodeScanner scanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scan);

        previewView = findViewById(R.id.previewView);
        tvDetectedBarcode = findViewById(R.id.tvDetectedBarcode);
        Button btnTriggerScan = findViewById(R.id.btnTriggerScan);
        Button btnClose = findViewById(R.id.btnClose);

        // 🚀 1. 사용자 확정 클릭 스캔 버튼 (클릭 시 삑! 소리와 함께 수량 카운팅 확정 리턴)
        if (btnTriggerScan != null) {
            btnTriggerScan.setOnClickListener(v -> {
                if (!lastDetectedVal.isEmpty()) {
                    playBeep(); // 💡 삑! 사용자 스캔 확정 비프음 재생
                    Log.i(TAG, "🎯 [User Confirmed Scan]: " + lastDetectedVal);
                    Intent intent = new Intent();
                    intent.putExtra("BARCODE_VALUE", lastDetectedVal);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(this, "바코드를 레이저 조준선에 먼저 맞추세요.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> finish());
        }

        // 🚀 CODE 128 / CODE 39 정품 바코드 전용 초정밀 스캐너 설정
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_CODE_128,
                        Barcode.FORMAT_CODE_39,
                        Barcode.FORMAT_QR_CODE
                )
                .build();
        scanner = BarcodeScanning.getClient(options);

        cameraExecutor = Executors.newSingleThreadExecutor();
        startCamera();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // 🚀 [4K Ultra HD 3840x2160 하드웨어 최고 해상도 세팅] 
                // 종이 및 노트북 화면의 미세한 Code 128 바코드 선이 크리스탈처럼 선명히 찍히도록 최고 해상도 적용!
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new android.util.Size(3840, 2160))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                imageAnalysis.setAnalyzer(cameraExecutor, this::processImageProxy);

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis);
            } catch (Exception e) {
                Log.e(TAG, "Camera Error", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void processImageProxy(ImageProxy imageProxy) {
        if (imageProxy.getImage() == null) return;

        // 💡 [실시간 밀림 방지] 백그라운드 밀린 큐(Queue) 프레임을 100% 버리는 Atomic 락
        if (!isProcessing.compareAndSet(false, true)) {
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());

        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    boolean found = false;

                    if (barcodes != null && !barcodes.isEmpty()) {
                        // 💡 1. 조준선에 비추고 있는 품목 바코드(예: 1001001)가 카메라 프레임에 계속 비치고 있다면 -> 무조건 100% 그 바코드만 영구 고정!
                        if (!lastDetectedVal.isEmpty()) {
                            for (Barcode barcode : barcodes) {
                                String value = barcode.getRawValue();
                                if (value == null || value.trim().isEmpty()) value = barcode.getDisplayValue();
                                if (value != null && lastDetectedVal.equalsIgnoreCase(value.trim())) {
                                    found = true;
                                    break; // 기존 바코드 100% 단단하게 고정 유지!
                                }
                            }
                        }

                        // 💡 2. 새로 조준한 경우 -> 빨간 레이저 가이드선 수직 중심 Y축에 가장 밀착된 바코드 1개 정밀 선택
                        if (!found) {
                            int rotation = imageProxy.getImageInfo().getRotationDegrees();
                            int rawW = image.getWidth();
                            int sensorCenterY = rawW / 2;

                            Barcode bestBarcode = null;
                            double minDistY = Double.MAX_VALUE;

                            for (Barcode barcode : barcodes) {
                                String value = barcode.getRawValue();
                                if (value == null || value.trim().isEmpty()) value = barcode.getDisplayValue();
                                if (value == null || value.trim().isEmpty()) continue;

                                android.graphics.Rect box = barcode.getBoundingBox();
                                if (box != null) {
                                    // 💡 세로(Portrait) 화면의 빨간 레이저 가이드선 수직 Y축 거리 정밀 계산
                                    int boxCenterY = (rotation == 90 || rotation == 270) ? box.centerX() : box.centerY();
                                    double distY = Math.abs(boxCenterY - sensorCenterY);

                                    if (distY < minDistY) {
                                        minDistY = distY;
                                        bestBarcode = barcode;
                                    }
                                } else if (bestBarcode == null) {
                                    bestBarcode = barcode;
                                }
                            }

                            if (bestBarcode != null) {
                                String value = bestBarcode.getRawValue();
                                if (value == null || value.trim().isEmpty()) value = bestBarcode.getDisplayValue();
                                if (value != null && !value.trim().isEmpty()) {
                                    String clean = value.trim();
                                    lastDetectedVal = clean;
                                    found = true;

                                    runOnUiThread(() -> {
                                        if (tvDetectedBarcode != null) {
                                            tvDetectedBarcode.setText(String.format(Locale.getDefault(), "🎯 감지된 바코드: %s", lastDetectedVal));
                                        }
                                    });
                                }
                            }
                        }
                    }

                    // 💡 [허공 이동 시 0.001초 즉시 삭제] 허공으로 카메라 이동 즉시 잔상/이전 감지값 100% 즉각 삭제!
                    if (!found) {
                        lastDetectedVal = "";
                        runOnUiThread(() -> {
                            if (tvDetectedBarcode != null) {
                                tvDetectedBarcode.setText("");
                            }
                        });
                    }
                })
                .addOnCompleteListener(task -> {
                    isProcessing.set(false);
                    if (imageProxy != null) imageProxy.close();
                });
    }

    private void playBeep() {
        try {
            android.media.ToneGenerator toneGen = new android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 100);
            toneGen.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 150);
        } catch (Exception ignored) {}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) cameraExecutor.shutdown();
        if (scanner != null) scanner.close();
    }
}
