package com.aiotlabs.ifitpro.plugin.bluetooth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Calendar;


import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.red_folder.phonegap.plugin.backgroundservice.BackgroundService;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;

import com.aiotlabs.ifitpro.plugin.bluetooth.AppConstants;
import com.aiotlabs.ifitpro.plugin.bluetooth.MokoConstants;
import com.aiotlabs.ifitpro.plugin.bluetooth.MokoSupport;
import com.aiotlabs.ifitpro.plugin.bluetooth.MokoConnStateCallback;
import com.aiotlabs.ifitpro.plugin.bluetooth.MokoOrderTaskCallback;
import com.aiotlabs.ifitpro.plugin.bluetooth.OrderTaskResponse;
import com.aiotlabs.ifitpro.plugin.bluetooth.BaseMessageHandler;
import com.aiotlabs.ifitpro.plugin.bluetooth.LogModule;


public class MokoService extends BackgroundService implements MokoScanDeviceCallback, MokoConnStateCallback, MokoOrderTaskCallback {
    

    private final static String TAG = MokoService.class.getSimpleName();

    private HashMap<String, BleDevice> deviceMap;

    
    private String mHelloTo = "World";
    JSONObject scanState = new JSONObject();    
    JSONObject connectionState = new JSONObject();
    JSONObject orderResult = new JSONObject();



    /* ********************************************************************************** 
     * Background Service Segment (Redfolder Plugin)
     * ***********************************************************************************/

	@Override
	protected JSONObject doWork() {
        
        // try {
			
		// 	scanState.put("Message", msg);

		// 	Log.d(TAG, msg);
		// } catch (JSONException e) {
		// }
		
        // return result;	
        
		JSONObject result = new JSONObject();
		
		try {
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
			String now = df.format(new Date(System.currentTimeMillis())); 

			String msg = "Hello " + this.mHelloTo + " - its currently " + now;
			result.put("Message", msg);

            Log.d(TAG, msg);

		} catch (JSONException e) {
		}
		
		return orderResult;	
	}

	@Override
	protected JSONObject getConfig() {
		JSONObject result = new JSONObject();
		
		try {
			result.put("HelloTo", this.mHelloTo);
		} catch (JSONException e) {
		}
		
		return result;
	}

	@Override
	protected void setConfig(JSONObject config) {
        try {
			if (config.has("data")){
                this.mHelloTo = config.getString("data");
                if (this.mHelloTo.equals("s")){
                    searchDevices();
                }
                if (this.mHelloTo.equals("c")){
                    connectBluetoothDevice(scanState.getString("ifitdevaddr"));
                }
                if(this.mHelloTo.equals("steps")){
                    getLastestSteps();
                }
            }
		} catch (JSONException e) {

		}
		
	}     

	@Override
	protected JSONObject initialiseLatestResult() {
		deviceMap = new HashMap<>();    
		return null;
	}

	@Override
	protected void onTimerEnabled() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onTimerDisabled() {
		// TODO Auto-generated method stub
		
    }
    






    

    /* ********************************************************************************** 
     * BLUETOOT CONNECTION related
     * ***********************************************************************************/

    public void searchDevices() {
        Log.d(TAG, "SEARCH DEVICES");

        MokoSupport.getInstance().startScanDevice(this);
    }


    public void connectBluetoothDevice(String address) {
        MokoSupport.getInstance().connDevice(this, address, this);
    }

    public void disConnectBle() {
        MokoSupport.getInstance().setReconnectCount(0);
        MokoSupport.getInstance().disConnectBle();
    }

    public boolean isSyncData() {
        return MokoSupport.getInstance().isSyncData();
    }



     /* ********************************************************************************** 
     * BLUETOOT DATA related
     * ***********************************************************************************/

    public void getLastestSteps() {
        Calendar calendar = Utils.strDate2Calendar("2018-09-18 00:00", AppConstants.PATTERN_YYYY_MM_DD_HH_MM);
        MokoSupport.getInstance().sendOrder(new ZReadStepTask(this, calendar));
    }



    /* ********************************************************************************** 
     * Scan Device Segment
     * ***********************************************************************************/

    /* >> Interface Implmentation: MokoScanDeviceCallback */


    @Override
    public void onStartScan() {
        deviceMap.clear();
        // mDialog.setMessage("Scanning...");
        // mDialog.show();
    }

    @Override
    public void onScanDevice(BleDevice device) {
        deviceMap.put(device.address, device);
        try{
            scanState.put("ifitdevaddr", device.address);
        } catch (JSONException e) {
        }

        LogModule.i(device.address);
        // mDatas.clear();
        // mDatas.addAll(deviceMap.values());
        // mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStopScan() {
        // if (!MainActivity.this.isFinishing() && mDialog.isShowing()) {
        //     mDialog.dismiss();
        // }
        // mDatas.clear();
        // mDatas.addAll(deviceMap.values());
        // mAdapter.notifyDataSetChanged();
    }



    /* ********************************************************************************** 
     * MokoService Segment
     * ***********************************************************************************/

    /* >> Interface Implmentation: MokoConnStateCallback */
    
    @Override
    public void onConnectSuccess() {

        Intent intent = new Intent(MokoConstants.ACTION_DISCOVER_SUCCESS);
        sendOrderedBroadcast(intent, null);
    }

    @Override
    public void onDisConnected() {
        Intent intent = new Intent(MokoConstants.ACTION_CONN_STATUS_DISCONNECTED);
        sendOrderedBroadcast(intent, null);
    }

    @Override
    public void onConnTimeout(int reConnCount) {
        Intent intent = new Intent(MokoConstants.ACTION_DISCOVER_TIMEOUT);
        intent.putExtra(AppConstants.EXTRA_CONN_COUNT, reConnCount);
        sendBroadcast(intent);
    }


    /* >> Interface Implmentation: MokoOrderTaskCallback */


    @Override
    public void onOrderResult(OrderTaskResponse response) {

        Intent intent = new Intent(new Intent(MokoConstants.ACTION_ORDER_RESULT));
        intent.putExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TASK, response);
        parseResponse(response);
        sendOrderedBroadcast(intent, null);
    }

    @Override
    public void onOrderTimeout(OrderTaskResponse response) {
        Intent intent = new Intent(new Intent(MokoConstants.ACTION_ORDER_TIMEOUT));
        intent.putExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TASK, response);
        sendBroadcast(intent);
    }

    @Override
    public void onOrderFinish() {
        sendBroadcast(new Intent(MokoConstants.ACTION_ORDER_FINISH));
    }

    




    public void  parseResponse(OrderTaskResponse response){
            OrderEnum orderEnum = response.order;
            switch (orderEnum) {
                case Z_READ_VERSION:
                    LogModule.i("Version code：" + MokoSupport.versionCode);
                    LogModule.i("Should upgrade：" + MokoSupport.canUpgrade);
                    break;
                case Z_READ_USER_INFO:
                    UserInfo userInfo = MokoSupport.getInstance().getUserInfo();
                    LogModule.i(userInfo.toString());
                    break;
                case Z_READ_ALARMS:
                    ArrayList<BandAlarm> bandAlarms = MokoSupport.getInstance().getAlarms();
                    if (bandAlarms.size() == 0) {
                        return;
                    }
                    for (BandAlarm bandAlarm : bandAlarms) {
                        LogModule.i(bandAlarm.toString());
                    }
                    break;
                case Z_READ_UNIT_TYPE:
                    boolean unitType = MokoSupport.getInstance().getUnitTypeBritish();
                    LogModule.i("Unit type british:" + unitType);
                    break;
                case Z_READ_TIME_FORMAT:
                    int timeFormat = MokoSupport.getInstance().getTimeFormat();
                    LogModule.i("Time format:" + timeFormat);
                    break;
                case Z_READ_AUTO_LIGHTEN:
                    AutoLighten autoLighten = MokoSupport.getInstance().getAutoLighten();
                    LogModule.i(autoLighten.toString());
                    break;
                case Z_READ_SIT_ALERT:
                    SitAlert sitAlert = MokoSupport.getInstance().getSitAlert();
                    LogModule.i(sitAlert.toString());
                    break;
                case Z_READ_LAST_SCREEN:
                    boolean lastScreen = MokoSupport.getInstance().getLastScreen();
                    LogModule.i("Last screen:" + lastScreen);
                    break;
                case Z_READ_HEART_RATE_INTERVAL:
                    int interval = MokoSupport.getInstance().getHeartRateInterval();
                    LogModule.i("Heart rate interval:" + interval);
                    break;
                case Z_READ_CUSTOM_SCREEN:
                    CustomScreen customScreen = MokoSupport.getInstance().getCustomScreen();
                    LogModule.i(customScreen.toString());
                    break;
                case Z_READ_STEPS:
                    ArrayList<DailyStep> lastestSteps = MokoSupport.getInstance().getDailySteps();
                    if (lastestSteps == null || lastestSteps.isEmpty()) {
                        return;
                    }
                    for (DailyStep step : lastestSteps) {
                        try {
                            orderResult.put("steps", step.count);
                            orderResult.put("caloroies", step.calories);
                            orderResult.put("distance", step.distance);
                            orderResult.put("duration", step.duration);

                        } catch (JSONException e) {
                        }
                        LogModule.i("MokoService >> " + step.toString());
                    }
                    break;
                case Z_READ_SLEEP_GENERAL:
                    ArrayList<DailySleep> lastestSleeps = MokoSupport.getInstance().getDailySleeps();
                    if (lastestSleeps == null || lastestSleeps.isEmpty()) {
                        return;
                    }
                    for (DailySleep sleep : lastestSleeps) {
                        LogModule.i(sleep.toString());
                    }
                    break;
                case Z_READ_HEART_RATE:
                    ArrayList<HeartRate> lastestHeartRate = MokoSupport.getInstance().getHeartRates();
                    if (lastestHeartRate == null || lastestHeartRate.isEmpty()) {
                        return;
                    }
                    for (HeartRate heartRate : lastestHeartRate) {
                        LogModule.i(heartRate.toString());
                    }
                    break;
                case Z_READ_STEP_TARGET:
                    LogModule.i("Step target:" + MokoSupport.getInstance().getStepTarget());
                    break;
                case Z_READ_DIAL:
                    LogModule.i("Dial:" + MokoSupport.getInstance().getDial());
                    break;
                case Z_READ_NODISTURB:
                    LogModule.i(MokoSupport.getInstance().getNodisturb().toString());
                    break;
                case Z_READ_PARAMS:
                    LogModule.i("Product batch：" + MokoSupport.getInstance().getProductBatch());
                    LogModule.i("Params：" + MokoSupport.getInstance().getParams().toString());
                    break;
                case Z_READ_LAST_CHARGE_TIME:
                    LogModule.i("Last charge time：" + MokoSupport.getInstance().getLastChargeTime());
                    break;
                case Z_READ_BATTERY:
                    LogModule.i("Battery：" + MokoSupport.getInstance().getBatteryQuantity());
                    break;
            }

    }




















    /* >> Service Entry Point */

    @Override
    public void onCreate() {
        // LogModule.i("创建MokoService...onCreate");
        MokoSupport.getInstance().init(this);

        super.onCreate();
    }

    // @Override
    // public int onStartCommand(Intent intent, int flags, int startId) {
    //     LogModule.i("启动MokoService...onStartCommand");
    //     return super.onStartCommand(intent, flags, startId);
    // }

    // private IBinder mBinder = new LocalBinder();

    // @Override
    // public IBinder onBind(Intent intent) {
    //     LogModule.i("绑定MokoService...onBind");
    //     return mBinder;
    // }

    // @Override
    // public void onLowMemory() {
    //     LogModule.i("内存吃紧，销毁MokoService...onLowMemory");
    //     disConnectBle();
    //     super.onLowMemory();
    // }

    // @Override
    // public boolean onUnbind(Intent intent) {
    //     LogModule.i("解绑MokoService...onUnbind");
    //     return super.onUnbind(intent);
    // }

    // @Override
    // public void onDestroy() {
    //     LogModule.i("销毁MokoService...onDestroy");
    //     disConnectBle();
    //     MokoSupport.getInstance().setOpenReConnect(false);
    //     super.onDestroy();
    // }

    public class LocalBinder extends Binder {
        public MokoService getService() {
            return MokoService.this;
        }
    }

    public ServiceHandler mHandler;

    public class ServiceHandler extends BaseMessageHandler<MokoService> {

        public ServiceHandler(MokoService service) {
            super(service);
        }

        @Override
        protected void handleMessage(MokoService service, Message msg) {
        }
    }
}
