package com.aiotlabs.ifitpro.plugin.bluetooth;

import android.arch.persistence.room.Room;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Message;
import android.util.Log;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.red_folder.phonegap.plugin.backgroundservice.BackgroundService;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class MokoService extends BackgroundService implements MokoScanDeviceCallback, MokoConnStateCallback, MokoOrderTaskCallback {


    private final String DEVICE_CMDS = "DEVICE_CMDS";
    private final String SCAN = "SCAN";
    private final String CONNECT = "CONNECT";
    // private final String SCANANDCONNECT = "SCANANDCONNECT";
    private final String STEPS = "STEPS";
    private final String SLEEPS = "SLEEPS";
    private final String SETALARM ="SETALARM";
    private final String GETALARM = "GETALARM";
    private final String SETAUTOLIGHTEN = "SETAUTOLIGHTEN";
    private final String GETAUTOLIGHTEN = "GETAUTOLIGHTEN";
    private final String SETSITALERT = "SETSITALERT";
    private final String GETSITALERT = "GETSITALERT";
    private final String SETHEARTRATEINTERVAL = "SETHEARTRATEINTERVAL";
    private final String GETHEARTRATEINTERVAL = "GETHEARTRATEINTERVAL";
    private final String SETSTEPTARGET = "SETSTEPTARGET";
    private final String GETSTEPTARGET = "GETSTEPTARGET";
    private final String GETLATESTHEARTRATE = "GETLATESTHEARTRATE";
    private final String STEPCHANGELISTENER = "STEPCHANGELISTENER";
    private final String SETNODISTURB = "SETNODISTURB";
    private final String GETNODISTURB = "GETNODISTURB";

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://192.168.0.101:8080");
        } catch (URISyntaxException e) {}
    }

    private final static String TAG = MokoService.class.getSimpleName();

    private HashMap<String, BleDevice> deviceMap;


    //Room variables
    public static MyDatabase database;


    private String serviceAction = "init";
    JSONObject scanState = new JSONObject();    
    JSONObject connectionState = new JSONObject();
    JSONObject orderResult = new JSONObject();

    public static int timerCount = 0;
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

        timerCount++;
        try {
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String now = df.format(new Date(System.currentTimeMillis()));

            result.put("action", this.serviceAction);
            result.put("time", now);
            result.put("timerCount", timerCount);

            Log.d(TAG, this.serviceAction);



            List<User> dbUserList = this.database.myDao().getAllUsers();
            Log.d(TAG,"USER LIST from ROOM");

            for(User userData: dbUserList){
                Log.d(TAG,"USERID : " + userData.getId() + "STEPS : " + userData.getSteps());
                result.put("userData", userData.toString());

                mSocket.emit("message", userData.toString());
            }
        } catch (JSONException e) {
        }


        return result;
    }

    @Override
    protected JSONObject getConfig() {
        JSONObject result = new JSONObject();

        try {
            result.put("serviceAction", this.serviceAction);
            result.put("scanResult", this.scanState);
            result.put("connectionResult", this.connectionState);
            result.put("orderResult", this.orderResult);

        } catch (JSONException e) {
        }



        return result;
    }

    @Override
    protected void setConfig(JSONObject config) {
        try {
            if (config.has(DEVICE_CMDS)){
                this.serviceAction = config.getString(DEVICE_CMDS);
                LogModule.i(config.getString(DEVICE_CMDS));

                if (this.serviceAction.equals(SCAN)){
                    searchDevices();
                }
                if (this.serviceAction.equals(CONNECT)){
                    connectBluetoothDevice(scanState.getString("ifitdevaddr"));
                }
                // if(this.serviceAction.equals(SCANANDCONNECT)){
                //     searchAndConnectDevices();
                // }
                if(this.serviceAction.equals(SETALARM)){
                    setAllAlarm();
                }
                if(this.serviceAction.equals(GETALARM)){
                    getAllAlarms();
                }
                if(this.serviceAction.equals(SETAUTOLIGHTEN)){
                    setAutoLighten();
                }
                if(this.serviceAction.equals(GETAUTOLIGHTEN)){
                    getAutoLighten();
                }
                if(this.serviceAction.equals(SETSITALERT)){
                    setSitAlert();
                }
                if(this.serviceAction.equals(GETSITALERT)){
                    getSitAlert();
                }
                if(this.serviceAction.equals(SETHEARTRATEINTERVAL)){
                    setHeartRateInterval();
                }
                if(this.serviceAction.equals(GETHEARTRATEINTERVAL)){
                    getHeartRateInterval();
                }
                if(this.serviceAction.equals(SETSTEPTARGET)){
                    setStepTarget();
                }
                if(this.serviceAction.equals(GETSTEPTARGET)){
                    getStepTarget();
                }
                if(this.serviceAction.equals(STEPS)){
                    getLastestSteps();
                }
                if(this.serviceAction.equals(SLEEPS)){
                    getLatestSleeps();
                }
                if(this.serviceAction.equals(SETNODISTURB)){
                    setNoDisturb();
                }
                if(this.serviceAction.equals(GETNODISTURB)){
                    getNoDisturb();
                }
                if(this.serviceAction.equals(GETLATESTHEARTRATE)){
                    getLatestHeartRate();
                }
                if(this.serviceAction.equals(STEPCHANGELISTENER)){
                    openStepChangeListener();
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

    // public void searchAndConnectDevices(){
    //     MokoSupport.getInstance().startScanDevice(this);
    //     try{
    //         MokoSupport.getInstance().connDevice(this, scanState.getString("deviceaddress"), this);
    //     }catch( JSONException e){}
    // }

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
    public void setAllAlarm(){
        BandAlarm drinkalarm1 =new BandAlarm();
        drinkalarm1.time ="15:48";
        drinkalarm1.state ="10010000";
        drinkalarm1.type = 1; //DRINK WATER

        BandAlarm drinkalarm2 =new BandAlarm();
        drinkalarm2.time ="15:58";
        drinkalarm2.state ="10010000";
        drinkalarm2.type = 1; //DRINK WATER

        BandAlarm drinkalarm3 =new BandAlarm();
        drinkalarm3.time ="16:08";
        drinkalarm3.state ="10010000";
        drinkalarm3.type = 1; //DRINK WATER

        ArrayList<BandAlarm> alarms = new ArrayList<BandAlarm>();
        alarms.add(drinkalarm1);
        alarms.add(drinkalarm2);
        alarms.add(drinkalarm3);

        MokoSupport.getInstance().sendOrder(new ZWriteAlarmsTask(this,alarms));

    }

    public void getAllAlarms(){
        MokoSupport.getInstance().sendOrder(new ZReadAlarmsTask(this));
    }

    public void setAutoLighten(){
        AutoLighten autoLighten = new AutoLighten();
        autoLighten.autoLighten = 1;
        autoLighten.startTime = "08:00";
        autoLighten.endTime = "23:00";

        MokoSupport.getInstance().sendOrder(new ZWriteAutoLightenTask(this, autoLighten));
    }

    public void getAutoLighten(){
        MokoSupport.getInstance().sendOrder(new ZReadAutoLightenTask(this));
    }

    public void setSitAlert(){
        SitAlert alert =new SitAlert();
        alert.alertSwitch = 1;
        alert.startTime = "11:00";
        alert.endTime = "22:00";
        MokoSupport.getInstance().sendOrder(new ZWriteSitAlertTask(this, alert));
    }

    public void getSitAlert(){
        MokoSupport.getInstance().sendOrder(new ZReadSitAlertTask(this));
    }

    public void setHeartRateInterval(){
        MokoSupport.getInstance().sendOrder(new ZWriteHeartRateIntervalTask(this, 2));

    }

    public void getHeartRateInterval(){
        MokoSupport.getInstance().sendOrder(new ZReadHeartRateIntervalTask(this));
    }

    public void setStepTarget(){
        MokoSupport.getInstance().sendOrder(new ZWriteStepTargetTask(this, 1390));
    }

    public void getStepTarget(){
        MokoSupport.getInstance().sendOrder(new ZReadStepTargetTask(this));
    }
    public void getLastestSteps() {
        Calendar calendar = Utils.strDate2Calendar("2018-09-18 00:00", AppConstants.PATTERN_YYYY_MM_DD_HH_MM);
        MokoSupport.getInstance().sendOrder(new ZReadStepTask(this, calendar));
    }

    public void getLatestSleeps(){
        Calendar calendar = Utils.strDate2Calendar("2018-09-18 00:00", AppConstants.PATTERN_YYYY_MM_DD_HH_MM);
        MokoSupport.getInstance().sendOrder(new ZReadSleepGeneralTask(this, calendar));
    }

    public void setNoDisturb(){
        NoDisturb noDisturb = new NoDisturb();
        noDisturb.noDisturb = 1;
        noDisturb.startTime = "23:00";
        noDisturb.endTime = "05:00";
        MokoSupport.getInstance().sendOrder(new ZWriteNoDisturbTask(this, noDisturb));
    }
    public void getNoDisturb(){
        MokoSupport.getInstance().sendOrder(new ZReadNoDisturbTask(this));

    }
    public void getLatestHeartRate(){
        Calendar calendar = Utils.strDate2Calendar("2018-09-18 00:00", AppConstants.PATTERN_YYYY_MM_DD_HH_MM);
        MokoSupport.getInstance().sendOrder(new ZReadHeartRateTask(this, calendar));
    }

    public void openStepChangeListener(){
        MokoSupport.getInstance().sendOrder(new ZOpenStepListenerTask(this));
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
            // scanAndConnectState.put("deviceaddress",device.address);
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
        try{
            connectionState.put("state", MokoConstants.ACTION_DISCOVER_SUCCESS);
        } catch (JSONException e){
        }
        // Intent intent = new Intent(MokoConstants.ACTION_DISCOVER_SUCCESS);
        // sendOrderedBroadcast(intent, null);
    }

    @Override
    public void onDisConnected() {
        try{
            connectionState.put("state", MokoConstants.ACTION_CONN_STATUS_DISCONNECTED);
        } catch (JSONException e){
        }
        // Intent intent = new Intent(MokoConstants.ACTION_CONN_STATUS_DISCONNECTED);
        // sendOrderedBroadcast(intent, null);
    }

    @Override
    public void onConnTimeout(int reConnCount) {
        try{
            connectionState.put("state", MokoConstants.ACTION_DISCOVER_TIMEOUT);
            connectionState.put("AppConstants.EXTRA_CONN_COUNT", reConnCount);
        } catch (JSONException e){
        }

        // Intent intent = new Intent(MokoConstants.ACTION_DISCOVER_TIMEOUT);
        // intent.putExtra(AppConstants.EXTRA_CONN_COUNT, reConnCount);
        // sendBroadcast(intent);
    }


    /* >> Interface Implmentation: MokoOrderTaskCallback */


    @Override
    public void onOrderResult(OrderTaskResponse response) {

        // Intent intent = new Intent(new Intent(MokoConstants.ACTION_ORDER_RESULT));
        // intent.putExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TASK, response);
        parseResponse(response);
        // sendOrderedBroadcast(intent, null);
    }

    @Override
    public void onOrderTimeout(OrderTaskResponse response) {
        // Intent intent = new Intent(new Intent(MokoConstants.ACTION_ORDER_TIMEOUT));
        // intent.putExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TASK, response);
        // sendBroadcast(intent);
    }

    @Override
    public void onOrderFinish() {
        // sendBroadcast(new Intent(MokoConstants.ACTION_ORDER_FINISH));
    }






    public void  parseResponse(OrderTaskResponse response){

        OrderEnum orderEnum = response.order;

        LogModule.i("PARSE RESPONSE : " + orderEnum);

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
                LogModule.i(bandAlarms.toString());

                if (bandAlarms.size() == 0) {
                    return;
                }
                for (BandAlarm bandAlarm : bandAlarms) {
                    try{
                        orderResult.put("alarm time",bandAlarm.time);
                        orderResult.put("alarm state",bandAlarm.state);
                        orderResult.put("alarm type",bandAlarm.type);
                    } catch (JSONException e){
                    }

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
                try{
                    orderResult.put("auto lighten status",autoLighten.autoLighten);
                    orderResult.put("auto lighten starting time",autoLighten.startTime);
                    orderResult.put("auto lighten ending time",autoLighten.endTime);
                } catch (JSONException e){
                }
                LogModule.i(autoLighten.toString());
                break;
            case Z_READ_SIT_ALERT:
                SitAlert sitAlert = MokoSupport.getInstance().getSitAlert();
                try{
                    orderResult.put("set sit alert",sitAlert.alertSwitch);
                    orderResult.put("sit alert start time ", sitAlert.startTime);
                    orderResult.put("sit alert end time",sitAlert.endTime);
                } catch (JSONException e){
                }
                LogModule.i(sitAlert.toString());
                break;
            case Z_READ_LAST_SCREEN:
                boolean lastScreen = MokoSupport.getInstance().getLastScreen();
                LogModule.i("Last screen:" + lastScreen);
                break;
            case Z_READ_HEART_RATE_INTERVAL:
                int interval = MokoSupport.getInstance().getHeartRateInterval();
                try{
                    orderResult.put("heart rate interval",interval);
                }catch(JSONException e){
                }
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
                int i= 0;
                for (DailyStep step : lastestSteps) {
                    try {
                        orderResult.put("steps", step.count);
                        orderResult.put("caloroies", step.calories);
                        orderResult.put("distance", step.distance);
                        orderResult.put("duration", step.duration);

                    } catch (JSONException e) {
                    }

                    LogModule.i("MokoService >> " + step.toString());


                    User user =new User();
                    // user.setId(i+1);
                    user.setSteps(Integer.valueOf(step.count));

                    this.database.myDao().addUser(user);
                    LogModule.i("Data inserted successfully in DataBase");

                }
                break;

            case Z_READ_SLEEP_GENERAL:
                ArrayList<DailySleep> lastestSleeps = MokoSupport.getInstance().getDailySleeps();
                if (lastestSleeps == null || lastestSleeps.isEmpty()) {
                    return;
                }
                for (DailySleep sleep : lastestSleeps) {
                    try{
                        orderResult.put("awake hours",sleep.awakeDuration);
                        orderResult.put("sleep hours",sleep.deepDuration);
                        orderResult.put("wake time",sleep.endTime);
                    } catch (JSONException e) {
                    };
                    LogModule.i(sleep.toString());
                }
                break;

            case Z_READ_HEART_RATE:
                ArrayList<HeartRate> lastestHeartRate = MokoSupport.getInstance().getHeartRates();
                if (lastestHeartRate == null || lastestHeartRate.isEmpty()) {
                    return;
                }
                for (HeartRate heartRate : lastestHeartRate) {
                    try{
                        orderResult.put("latest heartRate at ",heartRate.time);
                        orderResult.put("heartRate Value ",heartRate.value);
                    }catch (JSONException e){
                    }
                    LogModule.i(heartRate.toString());
                }
                break;
            case Z_READ_STEP_TARGET:
                int target = MokoSupport.getInstance().getStepTarget();
                try{
                    orderResult.put("step target",target);
                }catch (JSONException e){
                }
                LogModule.i("Step target:" + MokoSupport.getInstance().getStepTarget());
                break;
            case Z_READ_DIAL:
                LogModule.i("Dial:" + MokoSupport.getInstance().getDial());
                break;
            case Z_READ_NODISTURB:
                NoDisturb noDisturb = MokoSupport.getInstance().getNodisturb();
                try{
                    orderResult.put("noDisturb state",noDisturb.noDisturb);
                    orderResult.put("noDisturb start",noDisturb.startTime);
                    orderResult.put("noDisturb end",noDisturb.endTime);
                }catch (JSONException e){
                }
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





    private BroadcastReceiver mReceiver = new BroadcastReceiver() {


        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();

                if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                    OrderEnum orderEnum = (OrderEnum) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_CURRENT_DATA_TYPE);
                    switch (orderEnum) {
                        case Z_STEPS_CHANGES_LISTENER:
                            DailyStep dailyStep = MokoSupport.getInstance().getDailyStep();
                            LogModule.i(dailyStep.toString());


                            // runOnce();

                            JSONObject tmp = new JSONObject();
                            try{
                                tmp.put("userData",dailyStep.toString());
                            } catch (JSONException e){
                            }
                            sendUpdate(tmp);

                            // // Now call the listeners
                            // Log.i(TAG, "Sending to all listeners");
                            // for (int i = 0; i < mListeners.size(); i++)
                            // {
                            //     try {
                            //         mListeners.get(i).handleUpdate();
                            //         Log.i(TAG, "Sent listener - " + i);
                            //     } catch (RemoteException e) {
                            //         Log.i(TAG, "Failed to send to listener - " + i + " - " + e.getMessage());
                            //     }
                            // }
                            break;
                    }
                }

            }

        }

    };









    /* >> Service Entry Point */

    @Override
    public void onCreate() {
        // LogModule.i("创建MokoService...onCreate");
        MokoSupport.getInstance().init(this);

        super.onCreate();

        IntentFilter filter = new IntentFilter();
        filter.addAction(MokoConstants.ACTION_CONN_STATUS_DISCONNECTED);
        filter.addAction(MokoConstants.ACTION_DISCOVER_TIMEOUT);
        filter.addAction(MokoConstants.ACTION_ORDER_RESULT);
        filter.addAction(MokoConstants.ACTION_ORDER_TIMEOUT);
        filter.addAction(MokoConstants.ACTION_ORDER_FINISH);
        filter.addAction(MokoConstants.ACTION_CURRENT_DATA);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.setPriority(200);
        registerReceiver(mReceiver, filter);



        //Room

        database = Room.databaseBuilder(this, MyDatabase.class, "userdb").build();

        mSocket.connect();


        super.onCreate();

    }

    // @Override
    // public int onStartCommand(Intent intent, int flags, int startId) {
    //     LogModule.i("启动MokoService...onStartCommand");

    // return super.onStartCommand(intent, flags, startId);

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



