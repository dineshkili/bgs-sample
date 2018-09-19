package com.aiots.ifitpro.plugin.bluetooth;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.HashMap;

import com.aiotlabs.ifitpro.plugin.bluetooth.service.MokoService;
import com.aiotlabs.ifitpro.plugin.bluetooth.support.MokoConstants;
import com.aiotlabs.ifitpro.plugin.bluetooth.support.MokoSupport;


/**
 * This class echoes a string called from JavaScript.
 */
public class IFitProBtPlugin extends CordovaPlugin implements ServiceConnection{


    private MokoService mService;
    private MokoService.LocalBinder serviceBinder;


    // public static final String TAG = "com.aiots.ifitpro.plugin.bluetooth";
    private CallbackContext callbackContext;
    private String mwMacAddress;
    private HashMap mwCallbackContexts;
    private boolean initialized = false;
    
    /**
     * Constructor.
     */
    public IFitProBtPlugin() {}
    
    /**
     * Sets the context of the Command. This can then be used to do things like
     * get file paths associated with the Activity.
     *
     * @param cordova The context of the main Activity.
     * @param webView The CordovaWebView Cordova is running in.
     */
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {

        super.initialize(cordova, webView);

        // rssi = new RSSI(this);
        // mwAccelerometer = new MWAccelerometer(this);

        mwCallbackContexts = new HashMap(); 
        bluetoothScanner = new BluetoothScanner(this);

        Context applicationContext = cordova.getActivity().getApplicationContext();

        applicationContext.bindService(new Intent(cordova.getActivity(), MokoService.class), this, Context.BIND_AUTO_CREATE);
        // Log.v(TAG,"Init Device");
    }

    public HashMap getMwCallbackContexts(){
        return mwCallbackContexts;
    }



    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackcontext) throws JSONException {
        if(action.equals("add")){
            this.add(args,callbackcontext);
            return true;
        }

        if(action.equals("pluginOnCreate")){
            this.pluginOnCreate();
            return true;
        }
        return false;
    }

    @Override
    public void onDestroy(){
        cordova.getActivity().getApplicationContext().unbindService(this);
    }

    
    @Override
    public void onServiceConnected(ComponentName name, IBinder service){
        serviceBinder = (MokoService.LocalBinder) service;
        // Log.i("MWDevice", "Service Connected");
        initialized = true;
        if(mwCallbackContexts.get(CONNECT) != null){
            connectBoard();
        }else if(mwCallbackContexts.get(SCAN_FOR_DEVICES) != null){
            bluetoothScanner.startBleScan();
        }

        if(mwCallbackContexts.get(CONNECT) == null &&
           mwCallbackContexts.get(SCAN_FOR_DEVICES) == null)
        {
            mwCallbackContexts.get(INITIALIZE).sendPluginResult(new PluginResult(PluginResult.Status.OK,
                                                                             "initialized"));
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {}

    




    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((MokoService.LocalBinder) service).getService();
            // 注册广播接收器
            IntentFilter filter = new IntentFilter();
            filter.addAction(MokoConstants.ACTION_CONN_STATUS_DISCONNECTED);
            filter.addAction(MokoConstants.ACTION_DISCOVER_SUCCESS);
            filter.setPriority(100);
            registerReceiver(mReceiver, filter);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (MokoConstants.ACTION_DISCOVER_SUCCESS.equals(intent.getAction())) {
                    abortBroadcast();
                    // if (!MainActivity.this.isFinishing() && mDialog.isShowing()) {
                    //     mDialog.dismiss();
                    // }
                    // Toast.makeText(MainActivity.this, "Connect success", Toast.LENGTH_SHORT).show();

                    // Intent orderIntent = new Intent(MainActivity.this, SendOrderActivity.class);
                    // orderIntent.putExtra("device", mDevice);
                    // startActivity(orderIntent);
                }
                if (MokoConstants.ACTION_CONN_STATUS_DISCONNECTED.equals(intent.getAction())) {
                    abortBroadcast();
                    if (MokoSupport.getInstance().isBluetoothOpen() && MokoSupport.getInstance().getReconnectCount() > 0) {
                        return;
                    }
                    // if (!MainActivity.this.isFinishing() && mDialog.isShowing()) {
                    //     mDialog.dismiss();
                    // }
                    // Toast.makeText(MainActivity.this, "Connect failed", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    
    private void add(CordovaArgs args,CallbackContext callbackcontext)  throws JSONException{
        String code="sat";
        String recieved = args.getString(0);
        System.out.println(recieved);
         if(recieved.equals(code)) {

             try{
                
                 callbackcontext.success(" Welcome sathish ");
             

             }
             catch(Exception ex){
              callbackcontext.error("something went wrong" +ex);

             }

         }else{
             callbackcontext.error("code doesn't match");
         }
    }
}
