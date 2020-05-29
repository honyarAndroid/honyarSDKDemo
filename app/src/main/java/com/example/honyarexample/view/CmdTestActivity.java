package com.example.honyarexample.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.honyarexample.R;
import com.honyar.SDKHonyarSupport;
import com.honyar.bean.DeviceData102;
import com.honyar.contract.DeviceUpDataListener;
import com.honyar.contract.SendDataListener;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * qhny_noid Creat by kiss
 * Description
 */
public class CmdTestActivity extends BaseActivity implements View.OnClickListener, DeviceUpDataListener {
    private DeviceData102 deviceData102;
    private String sendCmd="";
    private Button send_cmd_button,control_cmd,delay_add_cmd,delay_delete_cmd,delay_query_cmd;
    private EditText sendData;
    private TextView receiveData;
    private String sendData_String;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cmdtest);
        setTitle("测试指令");
        initdata();
    }

    private void initdata(){
        SDKHonyarSupport.getInstance().setDeviceUpDataListener(this);
        deviceData102 = (DeviceData102) getIntent().getSerializableExtra("device");
        send_cmd_button= (Button) findViewById(R.id.send_cmd_button);
        send_cmd_button.setOnClickListener(this);
        control_cmd= (Button) findViewById(R.id.control_cmd);
        control_cmd.setOnClickListener(this);
        delay_add_cmd= (Button) findViewById(R.id.delay_add_cmd);
        delay_add_cmd.setOnClickListener(this);
        delay_delete_cmd= (Button) findViewById(R.id.delay_delete_cmd);
        delay_delete_cmd.setOnClickListener(this);
        delay_query_cmd= (Button) findViewById(R.id.delay_query_cmd);
        delay_query_cmd.setOnClickListener(this);

        sendData= (EditText) findViewById(R.id.send_data);
        receiveData= (TextView) findViewById(R.id.receive_data);

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.send_cmd_button:
                try {
                    sendData_String = new JSONObject(sendData.getText().toString()).toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                SDKHonyarSupport.getInstance().sendData(deviceData102.deviceMac, sendData_String, new SendDataListener() {
                    @Override
                    public void sendFaild(int errorCode, String message) {

                    }

                    @Override
                    public void sendSuccess() {

                    }
                });
                break;

            case R.id.control_cmd:
                sendData.setText(getString(R.string.test_cmd_commd));
                break;

            case R.id.delay_add_cmd:
                sendData.setText(getString(R.string.add_delay_commd));
                break;

            case R.id.delay_delete_cmd:
                sendData.setText(getString(R.string.delete_delay_commd));
                break;

            case R.id.delay_query_cmd:
                sendData.setText(getString(R.string.query_delay_commd));
                break;
        }
    }

    @Override
    public void deviceUpData(String data) {
        receiveData.setText(data+"\n"+receiveData.getText().toString());
    }
}
