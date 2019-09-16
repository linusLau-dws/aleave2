package hk.com.dataworld.leaveapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import static hk.com.dataworld.leaveapp.Constants.EXTRA_SHIM_NOTIFICATION;

public class MessengerSelectionActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger_selection);
        getSupportActionBar().setTitle(R.string.btn_chatroom);
        ListView listView = findViewById(R.id.selection_listview);
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public Object getItem(int i) {
                return null;
            }

            @Override
            public long getItemId(int i) {
                return 0;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                View v = LayoutInflater.from(MessengerSelectionActivity.this).inflate(R.layout.activity_messenger_rows_inflated, viewGroup, false);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MessengerSelectionActivity.this, MessengerActivity.class);
                        startActivity(intent);
                    }
                });
                return v;
            }
        });

    }
}
