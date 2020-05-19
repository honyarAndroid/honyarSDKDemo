package com.example.honyarexample.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cazaea.sweetalert.SweetAlertDialog;
import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.espressif.iot.esptouch.util.ByteUtil;
import com.espressif.iot.esptouch.util.TouchNetUtil;
import com.example.honyarexample.R;
import com.example.honyarexample.utils.EspWifiAdminSimple;
import com.example.honyarexample.utils.WiFiMessage;
import com.honyar.SDKHonyarSupport;
import com.honyar.bean.DeviceData102;
import com.honyar.contract.NewDeviceConnectListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class EsptouchDemoActivity extends BaseActivity implements OnClickListener, NewDeviceConnectListener {

	private static final String TAG = "EsptouchDemoActivity";

	private TextView mTvApSsid;

	private EditText mEdtApPassword;

	private ImageButton mBtnConfirm;

	private EspWifiAdminSimple mWifiAdmin;
	
	private Spinner mSpinnerTaskCount;

	private WiFiMessage mWiFiMessage;

	private CheckBox rememberPass;

	private SharedPreferences pref;

	private SharedPreferences.Editor editor;

	private SweetAlertDialog pDialog;

	private String deviceId;
	private EsptouchAsyncTask4 mTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.esptouch_demo_activity);
		setTitle("配网操作");
		mWifiAdmin = new EspWifiAdminSimple(this);
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		mTvApSsid = (TextView) findViewById(R.id.tvApSssidConnected);
		mEdtApPassword = (EditText) findViewById(R.id.edtApPassword);
		mBtnConfirm = (ImageButton) findViewById(R.id.btnConfirm);
		rememberPass = (CheckBox) findViewById(R.id.remember_pass);
		mBtnConfirm.setOnClickListener(this);
		mWiFiMessage = new WiFiMessage();
		initdata();
	}

	private void initdata(){
		SDKHonyarSupport.getInstance().setNewDeviceConnectListener(this);
		boolean isRemember = pref.getBoolean("remember_password", false);
		if (isRemember) {
			String password = pref.getString("password", "");
			mEdtApPassword.setText(password);
			rememberPass.setChecked(true);
		}
		deviceId = "000000000000";

	}



	@Override
	protected void onResume() {
		super.onResume();
		// display the connected ap's ssid
		String apSsid = mWifiAdmin.getWifiConnectedSsid();

		if (apSsid != null) {
			mTvApSsid.setText(apSsid);
		} else {
			mTvApSsid.setText("");
		}
		// check whether the wifi is connected
		boolean isApSsidEmpty = TextUtils.isEmpty(apSsid);
		mBtnConfirm.setEnabled(!isApSsidEmpty);
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
		timer_pause();

	}

	@Override
	public void onClick(View v) {

		if (v == mBtnConfirm) {
			String apSsid = mTvApSsid.getText().toString();
			String apPassword = mEdtApPassword.getText().toString();
			String apBssid = mWifiAdmin.getWifiConnectedBssid();
			String taskResultCountStr = "1";
			editor = pref.edit();
			if (rememberPass.isChecked()) {
				editor.putBoolean("remember_password", true);
				editor.putString("password", apPassword);
			}else {
				editor.clear();
			}
			editor.commit();
			mWiFiMessage.setWIFI_SSID(apSsid);
			mWiFiMessage.setWIFI_PassWD(apPassword);
			pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
			pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
			pDialog.setTitleText("配置中,请稍后......");
			pDialog.setCancelable(false);
//			pDialog.setCancelText("取消");
			pDialog.show();
			timer_start(120,false);
			byte[] broadcast = {(byte) (1)};
			byte[] ssid = ByteUtil.getBytesByString(apSsid);
			byte[] password = ByteUtil.getBytesByString(apPassword);
			byte[] bssid = TouchNetUtil.parseBssid2bytes(apBssid);
			byte[] deviceCount = taskResultCountStr.getBytes();

			if (mTask != null) {
				mTask.cancelEsptouch();
			}
			mTask = new EsptouchAsyncTask4(this);
			mTask.execute(ssid, bssid, password, deviceCount,broadcast);

		}
	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case 1:
					pDialog.dismiss();
					timer_pause();
					mTask.cancelEsptouch();
					new SweetAlertDialog(EsptouchDemoActivity.this, SweetAlertDialog.ERROR_TYPE)
							.setTitleText("添加结果")
							.setContentText("添加失败，请确认WIFI密码是否正确，或者是否进入softAP模式")
							.setCancelText("重新添加")
							.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
								@Override
								public void onClick(SweetAlertDialog sDialog) {
									sDialog.dismissWithAnimation();
								}
							})
							.setConfirmText("进入SoftAP模式")
							.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
								@Override
								public void onClick(SweetAlertDialog sDialog) {
									goto_softAP();
									sDialog.dismissWithAnimation();
								}
							})
							.show();
					break;

				default:
					break;
			}
		}

	};

	@Override
	public void timer_task(){
		new Thread(new Runnable(){

			@Override
			public void run() {
				mHandler.sendEmptyMessage(1);

			}

		}).start();

	}
	private void goto_softAP(){
		new SweetAlertDialog(EsptouchDemoActivity.this, SweetAlertDialog.WARNING_TYPE)
				.setTitleText("SoftAP")
				.setContentText("请确认设备设备已进入SoftAP模式，并且手机已连入设备创建的AP")
				.setCancelText("取消")
				.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
					@Override
					public void onClick(SweetAlertDialog sDialog) {
						sDialog.dismissWithAnimation();
					}
				})
				.setConfirmText("设置WIFI")
				.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
					@Override
					public void onClick(SweetAlertDialog sDialog) {
						sDialog.dismissWithAnimation();
//						toActivity(EsptouchDemoActivity.this, SoftAPActivity.class);
						finish();
					}
				})
				.show();
	}

	public void toActivity(Context mcontext,Class toActivity){
		Intent intent = new Intent();
		intent.setClass(mcontext, toActivity);
		intent.putExtra("WifiMessage",mWiFiMessage);
		startActivity(intent);

	}


	private void GetnewDevice(){
		if (pDialog != null){
			pDialog.dismissWithAnimation();
		}
		if (mTask!=null){
			mTask.cancelEsptouch();
		}else {
			return;
		}

		timer_pause();
		new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
				.setTitleText("添加结果")
				.setContentText("设备添加成功，请检测设备指示灯是否正确")
				.setConfirmText("确认")
				.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
					@Override
					public void onClick(SweetAlertDialog sDialog) {
						sDialog.dismissWithAnimation();
//						toActivity(EsptouchDemoActivity.this, DeviceListActivity.class);
						finish();
					}
				})
				.show();
	}

	@Override
	public void newDeviceConnected(DeviceData102 deviceData102) {
		Log.i(TAG,"new device:"+deviceData102.deviceID);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				GetnewDevice();
			}
		});
	}

	private class EsptouchAsyncTask4 extends AsyncTask<byte[], IEsptouchResult, List<IEsptouchResult>> {
		private WeakReference<EsptouchDemoActivity> mActivity;

		private final Object mLock = new Object();
//		private ProgressDialog mProgressDialog;
//		private AlertDialog mResultDialog;
		private IEsptouchTask mEsptouchTask;

		EsptouchAsyncTask4(EsptouchDemoActivity activity) {
			mActivity = new WeakReference<>(activity);
		}

		void cancelEsptouch() {
			cancel(true);
//			if (mProgressDialog != null) {
//				mProgressDialog.dismiss();
//			}
//			if (mResultDialog != null) {
//				mResultDialog.dismiss();
//			}
			if (mEsptouchTask != null) {
				mEsptouchTask.interrupt();
			}
		}


		@Override
		protected void onPreExecute() {
			Activity activity = mActivity.get();
//			mProgressDialog = new ProgressDialog(activity);
//			mProgressDialog.setMessage(activity.getString(R.string.configuring_message));
//			mProgressDialog.setCanceledOnTouchOutside(false);
//			mProgressDialog.setOnCancelListener(dialog -> {
//				synchronized (mLock) {
//					if (mEsptouchTask != null) {
//						mEsptouchTask.interrupt();
//					}
//				}
//			});
//			mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, activity.getText(android.R.string.cancel),
//					(dialog, which) -> {
//						synchronized (mLock) {
//							if (mEsptouchTask != null) {
//								mEsptouchTask.interrupt();
//							}
//						}
//					});
//			mProgressDialog.show();
		}

		@Override
		protected void onProgressUpdate(IEsptouchResult... values) {
			Context context = mActivity.get();
			if (context != null) {
				IEsptouchResult result = values[0];
				Log.i(TAG, "EspTouchResult: " + result);
				String text = result.getBssid() + " is connected to the wifi";
				Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected List<IEsptouchResult> doInBackground(byte[]... params) {
			EsptouchDemoActivity activity = mActivity.get();
			int taskResultCount;
			synchronized (mLock) {
				byte[] apSsid = params[0];
				byte[] apBssid = params[1];
				byte[] apPassword = params[2];
				byte[] deviceCountData = params[3];
				byte[] broadcastData = params[4];
				taskResultCount = deviceCountData.length == 0 ? -1 : Integer.parseInt(new String(deviceCountData));
				Context context = activity.getApplicationContext();
				mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, context);
				mEsptouchTask.setPackageBroadcast(broadcastData[0] == 1);
				mEsptouchTask.setEsptouchListener(this::publishProgress);
			}
			return mEsptouchTask.executeForResults(taskResultCount);
		}

		@Override
		protected void onPostExecute(List<IEsptouchResult> result) {
			EsptouchDemoActivity activity = mActivity.get();
			activity.mTask = null;
			Log.i(TAG,"post excuerd");
//			mProgressDialog.dismiss();
//			if (result == null) {
//				mResultDialog = new AlertDialog.Builder(activity)
//						.setMessage(R.string.configure_result_failed_port)
//						.setPositiveButton(android.R.string.ok, null)
//						.show();
//				mResultDialog.setCanceledOnTouchOutside(false);
//				return;
//			}

			// check whether the task is cancelled and no results received
			IEsptouchResult firstResult = result.get(0);
			if (firstResult.isCancelled()) {
				return;
			}
			// the task received some results including cancelled while
			// executing before receiving enough results

			if (!firstResult.isSuc()) {
//				mResultDialog = new AlertDialog.Builder(activity)
//						.setMessage(R.string.configure_result_failed)
//						.setPositiveButton(android.R.string.ok, null)
//						.show();
//				mResultDialog.setCanceledOnTouchOutside(false);
				return;
			}

			ArrayList<CharSequence> resultMsgList = new ArrayList<>(result.size());
			for (IEsptouchResult touchResult : result) {
				String message = activity.getString(R.string.configure_result_success_item,
						touchResult.getBssid(), touchResult.getInetAddress().getHostAddress());
				resultMsgList.add(message);
			}
			CharSequence[] items = new CharSequence[resultMsgList.size()];
//			mResultDialog = new AlertDialog.Builder(activity)
//					.setTitle(R.string.configure_result_success)
//					.setItems(resultMsgList.toArray(items), null)
//					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialogInterface, int i) {
//
//						}
//					})
//					.show();
//			mResultDialog.setCanceledOnTouchOutside(false);
		}
	}
}
