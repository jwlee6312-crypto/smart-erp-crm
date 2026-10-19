package com.crmbank.erp.mobile.haba;

import android.os.Bundle;
import com.crmbank.erp.mobile.BaseActivity;
import com.crmbank.erp.mobile.R;

public class MHABA060U extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template);
    }

    @Override
    protected String getProgramTitle() { return "조직도등록"; }

    @Override
    protected String getProgramId() { return "MHABA060U"; }
}
