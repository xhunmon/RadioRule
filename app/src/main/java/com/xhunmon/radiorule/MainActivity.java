package com.xhunmon.radiorule;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class MainActivity extends Activity implements RadioRulerView.OnValueChangeListener,
        CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private TextView mShow;
    private RadioRulerView mRule;
    private CheckBox mCbAuto;
    private Button mBtStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mShow = (TextView) findViewById(R.id.tv);
        mRule = (RadioRulerView) findViewById(R.id.rule);
        mCbAuto = (CheckBox) findViewById(R.id.cb_auto);
        mBtStart = (Button) findViewById(R.id.bt_start);

        mRule.setMaxLineCount(200);//FM从88.0 ~ 108.0总共有200频道
        mRule.setOnValueChangeListener(this);
        mCbAuto.setOnCheckedChangeListener(this);
        mBtStart.setOnClickListener(this);

    }

    @Override
    public void onValueChange(float value) {
        mShow.setText("FM："+value);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked){
            mRule.setAutoSearchFM(true);
        }else {
            mRule.setAutoSearchFM(false);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.bt_start){
            mRule.startAutoSeachFM();
        }
    }
}
