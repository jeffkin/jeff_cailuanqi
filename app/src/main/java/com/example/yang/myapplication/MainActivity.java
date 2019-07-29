package com.example.yang.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MainActivity extends AppCompatActivity {

    private String MqttUserString = "5dqexwm/cailuanqi_1";
    private String MqttPwdString = "AwVAzZpCcS64L92Q";
    private String MqttIPString = "5dqexwm.mqtt.iot.gz.baidubce.com";
    private int MqttPort = 1883;
    public String SubscribeString = "cailuanqi_test1";//订阅的主题
    public String PublishString = "cailuanqi_test";//发布的主题

    private String MqttUserStringDefault = "5dqexwm/cailuanqi_1";
    private String MqttPwdStringDefault = "AwVAzZpCcS64L92Q";
    private String MqttIPStringDefault = "5dqexwm.mqtt.iot.gz.baidubce.com";
    private int MqttPortDefault = 1883;
    public String SubscribeStringDefault = "cailuanqi_test1";//订阅的主题
    public String PublishStringDefault = "cailuanqi_test";//发布的主题

    private MqttConnectOptions mqttConnectOptions;
    private MqttClient mqttClient;

    public static String TelephonyIMEI = "";
    MyHandler mHandler;
    TextView textView3;
    Button button,button2,button6;
    EditText EditText1;
    private SharedPreferencesHelper sharedPreferencesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferencesHelper = new SharedPreferencesHelper(MainActivity.this, "MqttConfig");

        MqttUserString = (String) sharedPreferencesHelper.getSharedPreference("MqttUserString", MqttUserString);
        MqttPwdString = (String)sharedPreferencesHelper.getSharedPreference("MqttPwdString", MqttPwdString);
        MqttIPString = (String)sharedPreferencesHelper.getSharedPreference("MqttIPString", MqttIPString);
        MqttPort = (int)sharedPreferencesHelper.getSharedPreference("MqttPort", MqttPort);
        SubscribeString = (String)sharedPreferencesHelper.getSharedPreference("SubscribeString", SubscribeString);
        PublishString = (String)sharedPreferencesHelper.getSharedPreference("PublishString", PublishString);

        mHandler = new MyHandler();
        TelephonyIMEI = getTelephonyIMEI(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        EditText1 = findViewById(R.id.EditText1);
        textView3 = findViewById(R.id.textView3);
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        button6 = findViewById(R.id.button6);


        //发送数据
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String SendString = EditText1.getText().toString().replace(" ","");

                if (SendString.length()>0)
                {
                    if (mqttClient != null && mqttClient.isConnected()){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    MqttMessage msgMessage = new MqttMessage(SendString.getBytes());
                                    mqttClient.publish(PublishString,msgMessage);
                                } catch (MqttPersistenceException e) {
                                } catch (MqttException e) {
                                }catch (Exception e) {
                                }
                            }
                        }).start();
                    }
                }
            }
        });

        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String SendString = EditText1.getText().toString().replace(" ","");

                if (SendString.length()>0)
                {
                    if (mqttClient != null && mqttClient.isConnected()){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    MqttMessage msgMessage = new MqttMessage(SendString.getBytes());
                                    mqttClient.publish(PublishString,msgMessage);
                                } catch (MqttPersistenceException e) {
                                } catch (MqttException e) {
                                }catch (Exception e) {
                                }
                            }
                        }).start();
                    }
                }
            }
        });


        new Thread(new Runnable() {
            @Override
            public void run() {
                InitMqttOptions();
                InitMqttConnect();
                try{
                    mqttClient.connect(mqttConnectOptions);
                    if (mqttClient.isConnected()) {
                        Log.e("MainActivity", "run: Connected" );
                        mqttClient.subscribe(SubscribeString);
                    }
                }catch (Exception e){}
            }
        }).start();
    }


    public void InitMqttOptions(){
        mqttConnectOptions = new MqttConnectOptions();//MQTT的连接设置
        //设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setUserName(MqttUserString);//设置连接的用户名
        mqttConnectOptions.setPassword(MqttPwdString.toCharArray());//设置连接的密码
        mqttConnectOptions.setConnectionTimeout(3);// 设置连接超时时间 单位为秒
        // 设置会话心跳时间 单位为秒
        mqttConnectOptions.setKeepAliveInterval(60);
    }

    public void InitMqttConnect() {
        try{
            long time=System.currentTimeMillis();
            String Str = time+"";
            Str = Str.substring(Str.length()-4,Str.length());
            mqttClient = new MqttClient("tcp://"+MqttIPString+":"+MqttPort,MainActivity.TelephonyIMEI+Str,new MemoryPersistence());
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {

                }

                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                    Message msg = mHandler.obtainMessage();
                    msg.what = 1;
                    msg.obj=mqttMessage.toString();
                    mHandler.sendMessage(msg);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) { }
            });
        }
        catch (Exception e) {
        }
    }


    class MyHandler extends Handler {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                String data = (String) msg.obj;
                textView3.append(data);
            }
        }

    }


    /**配置MQTT对话框*/
    private void MqttConfigAlertDialog(String Title)
    {
        AlertDialog.Builder MqttConfigAlertDialog = new AlertDialog.Builder(MainActivity.this);
        View MqttConfigView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_mqtt_config, null);

        final EditText editTextMqttUser = (EditText) MqttConfigView.findViewById(R.id.editTextDialogMC1);//用户名
        final EditText editTextMqttPwd = (EditText) MqttConfigView.findViewById(R.id.editTextDialogMC2);//密码
        final EditText editTextMqttIP = (EditText) MqttConfigView.findViewById(R.id.editTextDialogMC3);//IP地址
        final EditText editTextMqttPort = (EditText) MqttConfigView.findViewById(R.id.editTextDialogMC4);//端口号
        final EditText editTextMqttSub = (EditText) MqttConfigView.findViewById(R.id.editTextDialogMC5);//订阅的主题
        final EditText editTextMqttPub = (EditText) MqttConfigView.findViewById(R.id.editTextDialogMC6);//发布的主题
        final Button button3 =  MqttConfigView.findViewById(R.id.button3);//取消
        final Button button5 =  MqttConfigView.findViewById(R.id.button5);//默认
        final Button button4 =  MqttConfigView.findViewById(R.id.button4);//确定

        editTextMqttUser.setFocusable(true);
        editTextMqttUser.setFocusableInTouchMode(true);
        editTextMqttUser.requestFocus();//获取焦点 光标出现
        MqttConfigAlertDialog.setTitle(Title);
        MqttConfigAlertDialog.setView(MqttConfigView);//对话框加载视图
//        MqttConfigAlertDialog.show();

        final AlertDialog mqttConfigAlertDialog  = MqttConfigAlertDialog.create();
//        mqttConfigAlertDialog.setCanceledOnTouchOutside(false);//点击外围不消失

        //初始化显示
        mqttConfigAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                editTextMqttUser.setText(MqttUserString);
                editTextMqttPwd.setText(MqttPwdString);
                editTextMqttIP.setText(MqttIPString);
                editTextMqttPort.setText(MqttPort+"");
                editTextMqttSub.setText(SubscribeString);
                editTextMqttPub.setText(PublishString);
                editTextMqttUser.setSelection(editTextMqttUser.getText().length());//将光标移至文字末尾
            }
        });

        mqttConfigAlertDialog.show();

        //取消
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mqttConfigAlertDialog.dismiss();
            }
        });
        //默认
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferencesHelper.put("MqttUserString", MqttUserStringDefault);
                sharedPreferencesHelper.put("MqttPwdString", MqttPwdStringDefault);
                sharedPreferencesHelper.put("MqttIPString", MqttIPStringDefault);
                sharedPreferencesHelper.put("MqttPort", MqttPortDefault);
                sharedPreferencesHelper.put("SubscribeString", SubscribeStringDefault);
                sharedPreferencesHelper.put("PublishString", PublishStringDefault);

                MqttUserString = MqttUserStringDefault;//用户名
                MqttPwdString = MqttPwdStringDefault;//密码
                MqttIPString = MqttIPStringDefault;//IP地址
                MqttPort = MqttPortDefault;//端口号
                SubscribeString = SubscribeStringDefault;//订阅的主题
                PublishString = PublishStringDefault;//发布的主题

                try{
                    mqttClient.disconnect();
                    mqttClient.close();


                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            InitMqttOptions();
                            InitMqttConnect();
                            try{
                                mqttClient.connect(mqttConnectOptions);
                                if (mqttClient.isConnected()) {
                                    Log.e("MainActivity", "run: Connected" );
                                    mqttClient.subscribe(SubscribeString);
                                }
                            }catch (Exception e){}
                        }
                    }).start();

                    mqttConfigAlertDialog.dismiss();
                }catch (Exception e){}
            }
        });
        //确定
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String str1 = editTextMqttUser.getText().toString();
                    String str2 = editTextMqttPwd.getText().toString();
                    String str3 = editTextMqttIP.getText().toString();
                    String str4 = editTextMqttPort.getText().toString();
                    String str5 = editTextMqttSub.getText().toString();
                    String str6 = editTextMqttPub.getText().toString();

                    if (str1.length() == 0 || str2.length() == 0 ||str3.length() == 0 ||str4.length() == 0 ||
                            str5.length() == 0 ||str6.length() == 0) {
                        Toast.makeText(getApplicationContext(), "请检查输入",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    MqttUserString = str1;//用户名
                    MqttPwdString = str2;//密码
                    MqttIPString = str3;//IP地址
                    MqttPort = Integer.parseInt(str4);//端口号
                    SubscribeString = str5;//订阅的主题
                    PublishString = str6;//发布的主题

                    sharedPreferencesHelper.put("MqttUserString", MqttUserString);
                    sharedPreferencesHelper.put("MqttPwdString", MqttPwdString);
                    sharedPreferencesHelper.put("MqttIPString", MqttIPString);
                    sharedPreferencesHelper.put("MqttPort", MqttPort);
                    sharedPreferencesHelper.put("SubscribeString", SubscribeString);
                    sharedPreferencesHelper.put("PublishString", PublishString);

                    try{
                        mqttClient.disconnect();
                        mqttClient.close();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                InitMqttOptions();
                                InitMqttConnect();
                                try{
                                    mqttClient.connect(mqttConnectOptions);
                                    if (mqttClient.isConnected()) {
                                        Log.e("MainActivity", "run: Connected" );
                                        mqttClient.subscribe(SubscribeString);
                                    }
                                }catch (Exception e){}
                            }
                        }).start();
                    }catch (Exception e){}
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "存储失败,请检查输入",Toast.LENGTH_SHORT).show();
                }


                mqttConfigAlertDialog.dismiss();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            MqttConfigAlertDialog("MQQT配置");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /*获取手机IMEI号*/
    private String getTelephonyIMEI(Context context) {
        String id = "IMEI";
        //android.telephony.TelephonyManager
        TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)
        {
            if (mTelephony.getDeviceId() != null)
            {
                id = mTelephony.getDeviceId();
            }
        }
        else
        {
            //android.provider.Settings;
            id = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return id;
    }


    /** 当活动不再可见时调用 */
    @Override
    protected void onStop()
    {
        super.onStop();
        try{
            mqttClient.disconnect();
            mqttClient.close();
        }catch (Exception e){}
    }
}
