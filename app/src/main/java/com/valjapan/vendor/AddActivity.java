package com.valjapan.vendor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class AddActivity extends AppCompatActivity {
    private String spinnerItems[] = {"コカコーラ", "DyDo", "アサヒ", "キリン", "サントリー", "ポッカサッポロ", "明治", "災害支援型", "その他"};
    private double locateX, locateY;
    EditText contentEdiText;
    String context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        Log.d("AddActivity", "onCreate()");

        Intent intent = getIntent();
        locateX = intent.getDoubleExtra("LocateX", 0);
        locateY = intent.getDoubleExtra("LocateY", 0);
        Log.d("AddActivity", "渡された座標は\n緯度" + String.valueOf(locateX) + "\n経度" + String.valueOf(locateY) + "\nです。");


        contentEdiText = (EditText) findViewById(R.id.contentEditText);

        Spinner spinner = findViewById(R.id.spinner);

        // ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // spinner に adapter をセット
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //　アイテムが選択された時
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner spinner = (Spinner) parent;
                String item = (String) spinner.getSelectedItem();
                Log.d("AddActivity", item);
            }

            //　アイテムが選択されなかった
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void saveData(View v) {
        context = contentEdiText.getText().toString();
        if (TextUtils.isEmpty(context)) {
            context = "特になし";
        }
        Log.d("AddActivity", "保存ボタンを押しました");
        Log.d("AddActivity", "EditTextの内容は -> " + context);
        finish();
    }

}
