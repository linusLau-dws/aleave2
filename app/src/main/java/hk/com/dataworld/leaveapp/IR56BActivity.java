package hk.com.dataworld.leaveapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.NumberPicker;

import com.beardedhen.androidbootstrap.BootstrapButton;

import java.util.Calendar;

import static hk.com.dataworld.leaveapp.Constants.EXTRA_IR56B_YEAR_FROM;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_IR56B_YEAR_TO;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_SHIM_NOTIFICATION;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_TO_PAY_SLIP;

public class IR56BActivity extends BaseActivity {

    NumberPicker dateFromPicker;//, dateToPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ir_56b);

        Calendar cal = Calendar.getInstance();

        BootstrapButton btnGenerate = findViewById(R.id.btnGenerateIR56B);
        dateFromPicker = findViewById(R.id.picker_year_from);
        int year = cal.get(Calendar.YEAR);
        dateFromPicker.setMinValue(2000);
        dateFromPicker.setMaxValue(year);
        dateFromPicker.setValue(year-1);
        String[] displayed = new String[year - 2000 + 1];
        for (int x = 0; x < displayed.length; x++ ) {
            displayed[x] = String.format("%d-%d",x + 1999,x + 2000);
        }
        dateFromPicker.setDisplayedValues(displayed);

//        dateToPicker = findViewById(R.id.picker_year_to);
//        dateToPicker.setMinValue(2000);
//        dateToPicker.setMaxValue(year);
//        dateToPicker.setValue(year);

        btnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mIsEnableRestartBehaviour = false;
                Intent intent = new Intent(IR56BActivity.this, NewDownloadActivity.class);
                intent.putExtra(EXTRA_TO_PAY_SLIP, false);
                intent.putExtra(EXTRA_IR56B_YEAR_FROM, String.valueOf(dateFromPicker.getValue()-1));
                intent.putExtra(EXTRA_IR56B_YEAR_TO, String.valueOf(dateFromPicker.getValue()));
                //intent.putExtra(EXTRA_IR56B_YEAR_TO, String.valueOf(dateToPicker.getValue()));
                startActivity(intent);
            }
        });
    }
}
