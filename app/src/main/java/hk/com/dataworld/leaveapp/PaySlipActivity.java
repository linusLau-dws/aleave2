package hk.com.dataworld.leaveapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.NumberPicker;

import com.beardedhen.androidbootstrap.BootstrapButton;

import java.util.Calendar;

import static hk.com.dataworld.leaveapp.Constants.EXTRA_PAY_SLIP_MONTH;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_PAY_SLIP_YEAR;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_SHIM_NOTIFICATION;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_TO_PAY_SLIP;

public class PaySlipActivity extends BaseActivity {

    NumberPicker monthPicker, yearPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_slip);

        Calendar cal = Calendar.getInstance();
        monthPicker = findViewById(R.id.picker_month);
        yearPicker = findViewById(R.id.picker_year);
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(cal.get(Calendar.MONTH)+1);
        int year = cal.get(Calendar.YEAR);
        yearPicker.setMinValue(2000);
        yearPicker.setMaxValue(year);
        yearPicker.setValue(year);

        BootstrapButton btnPaySlip = findViewById(R.id.btnGeneratePaySlip);
        btnPaySlip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsEnableRestartBehaviour = false;
                Intent intent = new Intent(PaySlipActivity.this, NewDownloadActivity.class);
                intent.putExtra(EXTRA_TO_PAY_SLIP, true);
                intent.putExtra(EXTRA_PAY_SLIP_YEAR, String.format("%d", yearPicker.getValue()));
                intent.putExtra(EXTRA_PAY_SLIP_MONTH, String.format("%02d", monthPicker.getValue()));
                startActivity(intent);
            }
        });
    }
}
