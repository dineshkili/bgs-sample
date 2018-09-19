package com.aiotlabs.ifitpro.plugin.bluetooth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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
		
		return result;	
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
        JSONObject result = new JSONObject();
		try {
			if (config.has("search")){
                searchDevices();
                result.put("action", "Searching");
            }
        } 
        catch (JSONException e) {
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
     * METHODS Implementation
     * ***********************************************************************************/

    public void searchDevices() {
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

    
    /* >> Service Entry Point */

    @Override
    public void onCreate() {
        LogModule.i("创建MokoService...onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogModule.i("启动MokoService...onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    private IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        LogModule.i("绑定MokoService...onBind");
        return mBinder;
    }

    @Override
    public void onLowMemory() {
        LogModule.i("内存吃紧，销毁MokoService...onLowMemory");
        disConnectBle();
        super.onLowMemory();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogModule.i("解绑MokoService...onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        LogModule.i("销毁MokoService...onDestroy");
        disConnectBle();
        MokoSupport.getInstance().setOpenReConnect(false);
        super.onDestroy();
    }

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
