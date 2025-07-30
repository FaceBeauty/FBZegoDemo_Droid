package im.zego.others.aivoicechanger;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONObject;

import im.zego.commontools.logtools.AppLogger;
import im.zego.commontools.logtools.LogView;
import im.zego.commontools.logtools.logLinearLayout;
import im.zego.commontools.uitools.ZegoViewUtil;
import im.zego.keycenter.KeyCenter;
import im.zego.keycenter.UserIDHelper;
import im.zego.others.R;
import im.zego.zegoexpress.ZegoAIVoiceChanger;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoAIVoiceChangerEventHandler;
import im.zego.zegoexpress.callback.IZegoApiCalledEventHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoAIVoiceChangerEvent;
import im.zego.zegoexpress.constants.ZegoAudioChannel;
import im.zego.zegoexpress.constants.ZegoAudioDeviceMode;
import im.zego.zegoexpress.constants.ZegoAudioSampleRate;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublishChannel;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoCustomAudioProcessConfig;
import im.zego.zegoexpress.entity.ZegoEngineConfig;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zegoexpress.entity.ZegoAIVoiceChangerSpeakerInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class AIVoiceChanger extends AppCompatActivity {

    EditText editRoomID;
    EditText editUserID;
    EditText editPublishStreamID;
    EditText editPlayStreamID;
    Button updateModelButton;
    AppCompatSpinner setSpeakerSpinner;
    SwitchMaterial earReturnSwitch;
    Button publishButton;
    Button playButton;
    TextureView preview;
    TextView roomState;
    TextView speakerName;
    AlertDialog downloadProgressDialog;
    Long appID;
    String appSign;
    String roomID;
    String userID;
    String publishStreamID;
    String playStreamID;
    boolean isPublish = false;
    boolean isPlay = false;
    boolean modelDownloaded = false;

    ZegoExpressEngine engine;
    ZegoAIVoiceChanger aiVoiceChanger;
    ZegoCanvas previewCanvas;

    ArrayAdapter<String> speakerAdapter;
    HashMap<String, Integer> mapSpeakerNameToID = new HashMap<>();

    HandlerThread thread;
    Handler threadHandler;

    MediaProjectionManager projectionManager;
    int REQUEST_CODE = 1001;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            if (Build.VERSION.SDK_INT >= 34) {
                startForegroundService(new Intent(this, KeepAliveNotificationService.class));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_voice_changer);
        bindView();
        setLogComponent();
        requestPermission();
        getAppIDAndUserIDAndAppSign();
        initEngineAndUser();
        setDefaultConfig();
        setUpdateModelButtonEvent();
        setEventHandler();
        setPublishButtonEvent();
        setPlayButtonEvent();
        setApiCalledResult();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Build.VERSION.SDK_INT >= 34) {
                if (projectionManager == null) {
                    projectionManager = (MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                }
                Intent intent = projectionManager.createScreenCaptureIntent();
                startActivityForResult(intent, REQUEST_CODE);
            } else {
                startForegroundService(new Intent(this, KeepAliveNotificationService.class));
            }

        }

        thread = new HandlerThread("copy_assert");
        thread.start();

        threadHandler = new Handler(thread.getLooper());
        threadHandler.post(new Runnable() {
            @Override
            public void run() {
                copyAssetsFiles();
            }
        });
    }
    public void bindView(){
        editRoomID = findViewById(R.id.editRoomID);
        editUserID = findViewById(R.id.editUserID);
        editPublishStreamID = findViewById(R.id.publishStreamID);
        editPlayStreamID = findViewById(R.id.playStreamID);
        updateModelButton = findViewById(R.id.updateModelButton);
        setSpeakerSpinner = findViewById(R.id.setSpeakerSpinner);
        earReturnSwitch = findViewById(R.id.earReturnSwitch);
        publishButton = findViewById(R.id.publishButton);
        playButton = findViewById(R.id.playButton);
        preview = findViewById(R.id.textureView);
        roomState = findViewById(R.id.roomState);
        speakerName = findViewById(R.id.speakerTitle);

        speakerAdapter = new ArrayAdapter<>(this, R.layout.speaker_item);
        speakerAdapter.add(getString(R.string.speaker_name_original));
        setSpeakerSpinner.setAdapter(speakerAdapter);
        setSpeakerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                int speakerID = 0;

                String speakerName = speakerAdapter.getItem(pos);
                if (mapSpeakerNameToID.containsKey(speakerName)) {
                    speakerID = mapSpeakerNameToID.get(speakerName).intValue();
                }

                if (aiVoiceChanger != null) {
                    aiVoiceChanger.setSpeaker(speakerID);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        earReturnSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                ZegoExpressEngine engine = ZegoExpressEngine.getEngine();
                if (engine != null) {
                    engine.enableHeadphoneMonitor(isChecked);
                }
            }
        });
    }

    // request for permission
    public void requestPermission() {
        ArrayList<String> permissionsStorageLst = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") != PackageManager.PERMISSION_GRANTED) {
                permissionsStorageLst.add("android.permission.CAMERA");
            }
            
            if (ContextCompat.checkSelfPermission(this, "android.permission.RECORD_AUDIO") != PackageManager.PERMISSION_GRANTED) {
                permissionsStorageLst.add("android.permission.RECORD_AUDIO");
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (ContextCompat.checkSelfPermission(this, "android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION") != PackageManager.PERMISSION_GRANTED) {
                    permissionsStorageLst.add("android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION");
                }
                // CAPTURE_VIDEO_OUTPUT & PROJECT_MEDIA have at least one of them
                if (ContextCompat.checkSelfPermission(this, "android.permission.CAPTURE_VIDEO_OUTPUT") != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, "android.permission.PROJECT_MEDIA") != PackageManager.PERMISSION_GRANTED) {
                    permissionsStorageLst.add("android.permission.CAPTURE_VIDEO_OUTPUT");
                }
            }

            if (!permissionsStorageLst.isEmpty()) {
                requestPermissions(permissionsStorageLst.toArray(new String[0]), 101);
            }
        }
    }
    // Set log component. It includes a pop-up dialog.
    public void setLogComponent(){
        logLinearLayout logHiddenView = findViewById(R.id.logView);
        logHiddenView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogView logview = new LogView(getApplicationContext());
                logview.show(getSupportFragmentManager(),null);
            }
        });
    }

    //get appID and userID and appSign
    public void getAppIDAndUserIDAndAppSign(){
        appID = KeyCenter.getInstance().getAppID();
        appSign = KeyCenter.getInstance().getAppSign();
        userID = UserIDHelper.getInstance().getUserID();
    }

    public void initEngineAndUser(){
        ZegoEngineConfig engineConfig = new ZegoEngineConfig();
        engineConfig.advancedConfig.put("audio_loopback_after_prep", "true");
        ZegoExpressEngine.setEngineConfig(engineConfig);

        // Initialize ZegoExpressEngine
        ZegoEngineProfile profile = new ZegoEngineProfile();
        profile.appID = appID;
        profile.appSign = appSign;

        // Here we use the broadcast scenario as an example,
        // you should choose the appropriate scenario according to your actual situation,
        // for the differences between scenarios and how to choose a suitable scenario,
        // please refer to https://docs.zegocloud.com/article/14940
        profile.scenario = ZegoScenario.HIGH_QUALITY_VIDEO_CALL;

        profile.application = getApplication();
        engine = ZegoExpressEngine.createEngine(profile, null);

        boolean isAIVoiceChangerSupported = engine.isAIVoiceChangerSupported();
        AppLogger.getInstance().e("is AI voice changer supported after create engine: %s", isAIVoiceChangerSupported);

        // enable custom audio processing
        ZegoCustomAudioProcessConfig audioProcessConfig = new ZegoCustomAudioProcessConfig();
        audioProcessConfig.channel = ZegoAudioChannel.MONO;
        audioProcessConfig.sampleRate = ZegoAudioSampleRate.ZEGO_AUDIO_SAMPLE_RATE_48K;
        audioProcessConfig.samples = 0;
        engine.enableCustomAudioCaptureProcessing(true, audioProcessConfig);

//        engine.enableCamera(false);
        engine.enableHeadphoneMonitor(true);
        engine.enableAEC(true);
        engine.enableANS(true);
        engine.enableAGC(true);
        engine.setAudioDeviceMode(ZegoAudioDeviceMode.GENERAL);

        // initialize AI void changer
        aiVoiceChanger = engine.createAIVoiceChanger();
        if (aiVoiceChanger == null) {
            AppLogger.getInstance().e("aiVoiceChanger is null");
        }

        if (aiVoiceChanger != null) {
            aiVoiceChanger.setEventHandler(new IZegoAIVoiceChangerEventHandler() {
                @Override
                public void onInit(ZegoAIVoiceChanger aiVoiceChanger, int errorCode) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AppLogger.getInstance().i("onInit, errorCode: %d", errorCode);

                            if (errorCode != 0 && downloadProgressDialog != null) {
                                downloadProgressDialog.dismiss();
                                downloadProgressDialog = null;
                            }
                        }
                    });
                }

                @Override
                public void onUpdateProgress(ZegoAIVoiceChanger aiVoiceChanger, double percent, int fileIndex, int fileCount) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (downloadProgressDialog != null) {
                                String msg = String.format(Locale.getDefault(),"%.2f %d/%d", percent, fileIndex, fileCount);
                                TextView progressText = downloadProgressDialog.findViewById(R.id.progress_text);
                                progressText.setText(msg);
                            }
                        }
                    });
                }

                @Override
                public void onUpdate(ZegoAIVoiceChanger aiVoiceChanger, int errorCode) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AppLogger.getInstance().i("onUpdate, errorCode: %d", errorCode);

                            if (downloadProgressDialog != null) {
                                downloadProgressDialog.dismiss();
                                downloadProgressDialog = null;
                            }

                            Toast.makeText(AIVoiceChanger.this, errorCode == 0 ? "资源已下载":"资源未下载", Toast.LENGTH_SHORT).show();

                            if (errorCode == 0) {
                                modelDownloaded = true;

                                aiVoiceChanger.getSpeakerList();
                            }
                        }
                    });
                }

                @Override
                public void onGetSpeakerList(ZegoAIVoiceChanger aiVoiceChanger, int errorCode,
                                             ArrayList<ZegoAIVoiceChangerSpeakerInfo> speakerList) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AppLogger.getInstance().i("onGetSpeakerList, errorCode:%d", errorCode);

                            speakerAdapter.clear();
                            mapSpeakerNameToID.clear();
                            speakerAdapter.add(getString(R.string.speaker_name_original));
                            for (ZegoAIVoiceChangerSpeakerInfo speaker : speakerList) {
                                speakerAdapter.add(speaker.name);

                                mapSpeakerNameToID.put(speaker.name, speaker.id);
                            }
                        }
                    });
                }

                @Override
                public void onEvent(ZegoAIVoiceChanger aiVoiceChanger, ZegoAIVoiceChangerEvent event) {
                    super.onEvent(aiVoiceChanger, event);

                    AppLogger.getInstance().i("onEvent, event: %s", event);
                }
            });
        }

        // login room
        roomID = editRoomID.getText().toString();
        loginRoom();
        editRoomID.setEnabled(false);
        editUserID.setEnabled(false);
    }

    public void loginRoom(){
        //create the user
        ZegoUser user = new ZegoUser(userID);
        //login room
        engine.loginRoom(roomID, user);
        AppLogger.getInstance().callApi("LoginRoom: %s",roomID);
    }

    public void setDefaultConfig(){
        //set default publish StreamID
        publishStreamID = "0037";
        //set default room ID
        roomID = "0037";
        //set Zego Canvas
        previewCanvas = new ZegoCanvas(preview);

        previewCanvas.backgroundColor = Color.WHITE;
        editUserID.setText(userID);
        editUserID.setEnabled(false);
        setTitle(getString(R.string.ai_voice_changer));
    }

    public void setUpdateModelButtonEvent() {
        updateModelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (aiVoiceChanger == null) {
                    AppLogger.getInstance().e("aiVoiceChanger is null");

                    return;
                }

                if (modelDownloaded) {
                    Toast.makeText(AIVoiceChanger.this, "资源已下载", Toast.LENGTH_SHORT).show();

                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(AIVoiceChanger.this);
                builder.setCancelable(true);
                builder.setTitle("提示");
                builder.setMessage("是否需要下载资源");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (aiVoiceChanger == null) {
                            AppLogger.getInstance().e("aiVoiceChanger is null");

                            return;
                        }

                        aiVoiceChanger.initEngine();

                        View downloadModelView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.download_model_dialog, null);
                        AlertDialog.Builder dialog = new AlertDialog.Builder(AIVoiceChanger.this);
                        dialog.setTitle("下载资源");
                        dialog.setCancelable(false);
                        dialog.setView(downloadModelView);
                        downloadProgressDialog = dialog.show();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

//    public void setSetSpeakerButtonEvent() {
//        setSpeakerButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // TODO:
//            }
//        });
//    }

    public void setPublishButtonEvent(){
        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preview.setVisibility(isPublish ? View.GONE:View.VISIBLE);

                if (!isPublish) {
                    publishStreamID = editPublishStreamID.getText().toString();
                    editPublishStreamID.setEnabled(false);
                    engine.startPreview(previewCanvas);

                    // Start publishing stream
                    engine.startPublishingStream(publishStreamID);
                    AppLogger.getInstance().callApi("Start Publishing Stream:%s",publishStreamID);
                    publishButton.setText(getResources().getString(R.string.stop_publishing));
                    isPublish = true;
                } else {
                    editPublishStreamID.setEnabled(true);
                    engine.stopPreview();
                    engine.stopPublishingStream();
                    isPublish = false;
                    AppLogger.getInstance().callApi("Stop Publishing Stream:%s",publishStreamID);
                    publishButton.setText(getResources().getString(R.string.start_publishing));
                }
            }
        });
    }

    public void setPlayButtonEvent(){
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlay) {
                    playStreamID = editPlayStreamID.getText().toString();
                    editPlayStreamID.setEnabled(false);

                    // Start playing stream
                    engine.startPlayingStream(playStreamID);
                    AppLogger.getInstance().callApi("Start Playing Stream:%s",playStreamID);
                    playButton.setText(getResources().getString(R.string.stop_playing));
                    isPlay = true;
                } else {
                    editPlayStreamID.setEnabled(true);
                    engine.stopPlayingStream(playStreamID);
                    isPlay = false;
                    AppLogger.getInstance().callApi("Stop Playing Stream:%s",playStreamID);
                    playButton.setText(getResources().getString(R.string.start_playing));
                }
            }
        });
    }

    public void setEventHandler(){
        engine.setEventHandler(new IZegoEventHandler() {
            // The callback triggered when the state of stream publishing changes.
            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
                super.onPublisherStateUpdate(streamID, state, errorCode, extendedData);
                // If the state is PUBLISHER_STATE_NO_PUBLISH and the errcode is not 0, it means that stream publishing has failed
                // and no more retry will be attempted by the engine. At this point, the failure of stream publishing can be indicated
                // on the UI of the App.
                if(errorCode != 0 && state.equals(ZegoPublisherState.NO_PUBLISH)) {
                    if (isPublish) {
                        publishButton.setText(ZegoViewUtil.GetEmojiStringByUnicode(ZegoViewUtil.crossEmoji) + getString(R.string.stop_publishing));
                    }
                } else {
                    if (isPublish) {
                        publishButton.setText(ZegoViewUtil.GetEmojiStringByUnicode(ZegoViewUtil.checkEmoji) + getString(R.string.stop_publishing));
                    }
                }
            }

            // The callback triggered when the room connection state changes.
            @Override
            public void onRoomStateChanged(String roomID, ZegoRoomStateChangedReason reason, int errorCode, JSONObject extendedData) {
                ZegoViewUtil.UpdateRoomState(roomState, reason);
            }

            @Override
            public void onPublisherSendVideoFirstFrame(ZegoPublishChannel channel) {
                super.onPublisherSendVideoFirstFrame(channel);
                AppLogger.getInstance().receiveCallback("onPublisherSendVideoFirstFrame. channel:%s", channel);
            }

            @Override
            public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode, JSONObject extendedData) {
                super.onPlayerStateUpdate(streamID, state, errorCode, extendedData);

                if(errorCode != 0 && state.equals(ZegoPlayerState.NO_PLAY)) {
                    if (isPlay) {
                        playButton.setText(ZegoViewUtil.GetEmojiStringByUnicode(ZegoViewUtil.crossEmoji) + getString(R.string.stop_playing));
                    }
                } else {
                    if (isPlay) {
                        playButton.setText(ZegoViewUtil.GetEmojiStringByUnicode(ZegoViewUtil.checkEmoji) + getString(R.string.stop_playing));
                    }
                }
            }
        });
    }

    public void setApiCalledResult(){
        // Update log with api called results
        ZegoExpressEngine.setApiCalledCallback(new IZegoApiCalledEventHandler() {
            @Override
            public void onApiCalledResult(int errorCode, String funcName, String info) {
                super.onApiCalledResult(errorCode, funcName, info);
                if (errorCode == 0){
                    AppLogger.getInstance().success("[%s]:%s", funcName, info);
                } else {
                    AppLogger.getInstance().fail("[%d]%s:%s", errorCode, funcName, info);
                }
            }
        });
    }

    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity,AIVoiceChanger.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        engine.destroyAIVoiceChanger(aiVoiceChanger);

        // Logout room
        engine.logoutRoom(roomID);
        AppLogger.getInstance().callApi("Logout Room:%s",roomID);
        editRoomID.setEnabled(true);

        // Destroy the engine
        ZegoExpressEngine.destroyEngine(null);
        super.onDestroy();
    }

    private void copyAssetsFiles() {
        copyAssetsFile("14f0c5ec86e71debaf3f97a0b221e830");
        copyAssetsFile("2ac6d60dac84fccea4636547b0c0a52d");
        copyAssetsFile("7174f58773e2c65b57a2890e6ae2c44a");
        copyAssetsFile("7a6b5f858631e0b4332b5f369ad208b2");
        copyAssetsFile("a9a0b82d40344d2c935e1a25846ad24f");
    }

    private void copyAssetsFile(String fileName) {
        File modelFolder = new File(getFilesDir(), "vc_model");
        if (!modelFolder.exists()) {
            modelFolder.mkdir();
        }

        final File file = new File(modelFolder, fileName);
        System.out.println("File Path---->" + file.getAbsolutePath());
        if (file.exists()) {
            System.out.println("File exists");
            return;
        }
        try {
            // Get Assets.
            AssetManager assetManager = getAssets();
            InputStream is = assetManager.open(fileName);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}