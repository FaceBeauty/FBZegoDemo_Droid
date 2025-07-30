package im.zego.others.effectsbeauty;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import im.zego.commontools.ZegoFileUtil;
import im.zego.others.R;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import im.zego.commontools.logtools.AppLogger;
import im.zego.commontools.logtools.LogView;
import im.zego.commontools.logtools.logLinearLayout;
import im.zego.commontools.uitools.ZegoViewUtil;
import im.zego.keycenter.KeyCenter;
import im.zego.keycenter.UserIDHelper;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoApiCalledEventHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.callback.IZegoPublisherTakeSnapshotCallback;
import im.zego.zegoexpress.constants.ZegoLowlightEnhancementMode;
import im.zego.zegoexpress.constants.ZegoOrientation;
import im.zego.zegoexpress.constants.ZegoPublishChannel;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoVideoConfigPreset;
import im.zego.zegoexpress.constants.ZegoVideoDenoiseMode;
import im.zego.zegoexpress.constants.ZegoVideoDenoiseStrength;
import im.zego.zegoexpress.constants.ZegoVideoMirrorMode;
import im.zego.zegoexpress.constants.ZegoViewMode;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoColorEnhancementParams;
import im.zego.zegoexpress.entity.ZegoEffectsBeautyParam;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zegoexpress.entity.ZegoVideoConfig;
import im.zego.zegoexpress.entity.ZegoVideoDenoiseParams;

public class EffectsBeautyActivity extends AppCompatActivity {

    TextureView preview;
    TextView roomState;
    TextView captureVideoBrightness;

    Switch effectsBeautySwitch;
    SeekBar whitenSeekBar;
    SeekBar rosySeekBar;
    SeekBar smoothSeekBar;
    SeekBar sharpenSeekBar;
    TextView whitenValue;
    TextView rosyValue;
    TextView smoothValue;
    TextView sharpenValue;
    Button startPublishingButton;

    EditText captureWidthText;
    EditText captureHeightText;
    EditText encodeResolutionWidth;
    EditText encodeResolutionHeight;
    EditText fps;
    EditText bitrate;
    Spinner mirrorSpinner;
    Spinner viewModeSpinner;
    Switch cameraSwitch;
    Spinner cameraSelectSpinner;
    Switch colorEnhanceSwitch;
    Button takePublishSnapshotButton;
    EditText intensityText;
    EditText skinText;
    EditText lipText;
    Spinner orientationSpinner;
    Button doneButton;
    Button hideButton;
    ScrollView scrollView;
    Switch adaptiveFpsSwitch;
    Button adaptiveFpsButton;
    EditText adaptiveFpsMinEdit;
    EditText adaptiveFpsMaxEdit;
    Spinner lowLightEnhanceModeSpinner;
    Spinner videoDenoiseModeSpinner;
    Spinner videoDenoiseStrengthSpinner;

    ZegoVideoConfig videoConfig;
    ZegoVideoDenoiseParams videoDenoiseParams = new ZegoVideoDenoiseParams();

    String userID;
    String streamID;
    String roomID;
    ZegoExpressEngine engine;
    Long appID;
    String appSign;
    ZegoUser user;

    ZegoEffectsBeautyParam param;

    //Store whether the user is publishing the stream
    Boolean isPublish = false;

    //Jump to teaching document
    TextView document;

    boolean isHide = false;

    // request for permission
    public void requestPermission() {
        String[] PERMISSIONS_STORAGE = {
                "android.permission.CAMERA",
                "android.permission.RECORD_AUDIO"};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, "android.permission.RECORD_AUDIO") != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(PERMISSIONS_STORAGE, 101);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_effects_beauty);
        bindView();
        getAppIDAndUserIDAndAppSign();
        setDefaultValue();
        initEngineAndUser();
        requestPermission();
        setStartPublishButtonEvent();
        setEffectsBeautyEvent();
        setLogComponent();
        addEditListener();
        setApiCalledResult();
        setEventHandler();
        addWaterMarkInfoAndUrl();
        setColorEnhanceEvent();
        setLowlightEnhanceEvent();
    }
    public void bindView(){
        preview = findViewById(R.id.textureView);

        effectsBeautySwitch = findViewById(R.id.effectsBeautySwitch);
        whitenSeekBar = findViewById(R.id.whitenSeekBar);
        rosySeekBar = findViewById(R.id.rosySeekBar);
        smoothSeekBar = findViewById(R.id.smoothSeekBar);
        sharpenSeekBar = findViewById(R.id.sharpenSeekBar);
        whitenValue = findViewById(R.id.whitenValueTextView);
        rosyValue = findViewById(R.id.rosyValueTextView);
        smoothValue = findViewById(R.id.smoothValueTextView);
        sharpenValue = findViewById(R.id.sharpenValueTextView);
        startPublishingButton = findViewById(R.id.startPublishButton);

        roomState = findViewById(R.id.roomState);
        captureVideoBrightness = findViewById(R.id.captureVideoBrightness);
        document = findViewById(R.id.document);

        captureWidthText = findViewById(R.id.captureWidth);
        captureHeightText = findViewById(R.id.captureHeight);
        encodeResolutionWidth = findViewById(R.id.encodeResolutionWidth);
        encodeResolutionHeight = findViewById(R.id.encodeResolutionHeight);
        fps = findViewById(R.id.videoFps);
        bitrate = findViewById(R.id.videoBitrate);
        mirrorSpinner = findViewById(R.id.spinnerMirrorMode);
        viewModeSpinner = findViewById(R.id.spinnerViewMode);
        cameraSwitch = findViewById(R.id.switchCamera);
        cameraSelectSpinner = findViewById(R.id.spinnerCamera);
        colorEnhanceSwitch = findViewById(R.id.switchColorEnhance);
        takePublishSnapshotButton = findViewById(R.id.buttonSnapshot);
        intensityText = findViewById(R.id.editIntensity);
        skinText = findViewById(R.id.editSkinLevel);
        lipText = findViewById(R.id.editLipLevel);
        orientationSpinner = findViewById(R.id.spinnerOrientation);
        hideButton = findViewById(R.id.buttonHide);
        doneButton = findViewById(R.id.buttonDone);
        scrollView = findViewById(R.id.scrollView);
        adaptiveFpsSwitch = findViewById(R.id.switchCameraAdaptiveFPS);
        adaptiveFpsButton = findViewById(R.id.buttonUpdateAdaptiveFps);
        adaptiveFpsMinEdit = findViewById(R.id.adaptiveFPSMinEdit);
        adaptiveFpsMaxEdit = findViewById(R.id.adaptiveFPSMaxEdit);
        lowLightEnhanceModeSpinner = findViewById(R.id.spinnerLowlightEnhancementMode);
        videoDenoiseModeSpinner = findViewById(R.id.spinnerVideoDenoiseMode);
        videoDenoiseStrengthSpinner = findViewById(R.id.spinnerVideoDenoiseStrength);
    }
    public void setDefaultValue(){
        roomID = "0024";
        streamID = "0024";
        //set the default video configuration
        videoConfig = new ZegoVideoConfig(ZegoVideoConfigPreset.PRESET_360P);

        param = new ZegoEffectsBeautyParam();
        param.whitenIntensity = 50;
        param.rosyIntensity = 50;
        param.smoothIntensity = 50;
        param.sharpenIntensity = 50;
    }
    //get appID and userID and appSign
    public void getAppIDAndUserIDAndAppSign(){
        appID = KeyCenter.getInstance().getAppID();
        userID = UserIDHelper.getInstance().getUserID();
        appSign = KeyCenter.getInstance().getAppSign();
    }
    public void initEngineAndUser(){
        // Initialize ZegoExpressEngine
        ZegoEngineProfile profile = new ZegoEngineProfile();
        profile.appID = appID;
        profile.appSign = appSign;

        // Here we use the high quality video call scenario as an example,
        // you should choose the appropriate scenario according to your actual situation,
        // for the differences between scenarios and how to choose a suitable scenario,
        // please refer to https://docs.zegocloud.com/article/14940
        profile.scenario = ZegoScenario.HIGH_QUALITY_VIDEO_CALL;

        profile.application = getApplication();
        engine = ZegoExpressEngine.createEngine(profile, null);

        // 初始化 Effects 美颜环境
        engine.startEffectsEnv();

        AppLogger.getInstance().callApi("Create ZegoExpressEngine");
        //create the user
        user = new ZegoUser(userID);
    }
    public void loginRoom(){
        //login room
        engine.loginRoom(roomID, user);
        AppLogger.getInstance().callApi("LoginRoom: %s",roomID);
        //enable the camera
        engine.enableCamera(true);
        //enable the microphone
        engine.muteMicrophone(false);
        //enable the speaker
        engine.muteSpeaker(false);
    }
    public void setStartPublishButtonEvent(){
        startPublishingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if the user is publishing the stream, this button is used to stop publishing. Otherwise, this button is used to start publishing.
                if (isPublish){
                    engine.stopPreview();
                    engine.stopPublishingStream();
                    engine.logoutRoom();
                    AppLogger.getInstance().callApi("Stop Publishing Stream:%s",streamID);
                    startPublishingButton.setText(getString(R.string.start_publishing));
                    isPublish = false;

                } else {

                    // get configuration set by the user

                    if (encodeResolutionWidth.getText().toString().equals(""))
                    {
                        Toast.makeText(getApplicationContext(), "Encode Width cannot be Empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        videoConfig.encodeWidth = Integer.parseInt(encodeResolutionWidth.getText().toString());
                    } catch (NumberFormatException e)
                    {
                        Toast.makeText(getApplicationContext(), "Encode Width is too large", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (encodeResolutionHeight.getText().toString().equals(""))
                    {
                        Toast.makeText(getApplicationContext(), "Encode Height cannot be Empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        videoConfig.encodeHeight = Integer.parseInt(encodeResolutionHeight.getText().toString());
                    } catch (NumberFormatException e)
                    {
                        Toast.makeText(getApplicationContext(), "Encode Height is too large", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (fps.getText().toString().equals(""))
                    {
                        Toast.makeText(getApplicationContext(), "FPS cannot be Empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        videoConfig.fps = Integer.parseInt(fps.getText().toString());
                    } catch (NumberFormatException e)
                    {
                        Toast.makeText(getApplicationContext(), "FPS is too large", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (bitrate.getText().toString().equals(""))
                    {
                        Toast.makeText(getApplicationContext(), "Bitrate cannot be Empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        videoConfig.bitrate = Integer.parseInt(bitrate.getText().toString());
                    } catch (NumberFormatException e)
                    {
                        Toast.makeText(getApplicationContext(), "Bitrate is too large", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    videoConfig.captureWidth = Integer.parseInt(captureWidthText.getText().toString());
                    videoConfig.captureHeight = Integer.parseInt(captureHeightText.getText().toString());

                    // set video configuration
                    ZegoExpressEngine.getEngine().setVideoConfig(videoConfig);

                    loginRoom();
                    engine.startPreview(new ZegoCanvas(preview));
                    engine.startPublishingStream(streamID);
                    AppLogger.getInstance().callApi("Start Publishing Stream:%s",streamID);
                    startPublishingButton.setText(getString(R.string.stop_publishing));
                    isPublish = true;
                }
            }
        });
    }

    public void setEffectsBeautyEvent() {
        effectsBeautySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                engine.enableEffectsBeauty(isChecked);
            }
        });

        whitenSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                whitenValue.setText(String.valueOf(progress));
                param.whitenIntensity = progress;
                engine.setEffectsBeautyParam(param);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        rosySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rosyValue.setText(String.valueOf(progress));
                param.rosyIntensity = progress;
                engine.setEffectsBeautyParam(param);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        smoothSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                smoothValue.setText(String.valueOf(progress));
                param.smoothIntensity = progress;
                engine.setEffectsBeautyParam(param);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sharpenSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sharpenValue.setText(String.valueOf(progress));
                param.sharpenIntensity = progress;
                engine.setEffectsBeautyParam(param);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void setEventHandler(){
        engine.setEventHandler(new IZegoEventHandler() {
            // The callback triggered when the room connection state changes.
            @Override
            public void onRoomStateChanged(String roomID, ZegoRoomStateChangedReason reason, int errorCode, JSONObject extendedData) {
                ZegoViewUtil.UpdateRoomState(roomState, reason);
            }

            public void onRecvExperimentalAPI(String content) {
                try {
                    JSONObject dics = new JSONObject(content);
                    String method = dics.getString("method");

                    if ("liveroom.video.on_capture_video_brightness".equals(method)) {
                        JSONObject params = dics.getJSONObject("params");
                        int luma = params.getInt("luma");
                        boolean isLowlightEnhanced = params.getBoolean("is_lowlight_enhanced");
                        boolean isVideoDenoiseEnabled = params.getBoolean("is_video_denoise_enabled");
                        int index = params.getInt("channel");

                        String brightness = String.format("%d;%d;%d;%d", luma, isLowlightEnhanced ? 1 : 0, isVideoDenoiseEnabled ? 1 : 0, index);
                        captureVideoBrightness.setText(brightness);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // The callback triggered when the state of stream publishing changes.
            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
                super.onPublisherStateUpdate(streamID, state, errorCode, extendedData);
                // If the state is PUBLISHER_STATE_NO_PUBLISH and the errcode is not 0, it means that stream publishing has failed
                // and no more retry will be attempted by the engine. At this point, the failure of stream publishing can be indicated
                // on the UI of the App.
                if (errorCode != 0 && state.equals(ZegoPublisherState.NO_PUBLISH)) {
                    if (isPublish) {
                        startPublishingButton.setText(ZegoViewUtil.GetEmojiStringByUnicode(ZegoViewUtil.crossEmoji) + getString(R.string.stop_publishing));
                    }
                } else {
                    if (isPublish) {
                        startPublishingButton.setText(ZegoViewUtil.GetEmojiStringByUnicode(ZegoViewUtil.checkEmoji) + getString(R.string.stop_publishing));
                    }
                }
            }
        });
    }

    //Add EditText listener in order to configure fps and bitrate
    private void addEditListener() {
        captureWidthText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    if (captureWidthText.getText().toString().equals(""))
                    {
                        Toast.makeText(getApplicationContext(), "CaptureWidth cannot be Empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        videoConfig.captureWidth = Integer.parseInt(captureWidthText.getText().toString());
                        ZegoExpressEngine.getEngine().setVideoConfig(videoConfig);
                        AppLogger.getInstance().callApi("Change captureWidth");
                    } catch (NumberFormatException e)
                    {
                        Toast.makeText(getApplicationContext(), "CaptureWidth is too large", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        captureHeightText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    if (captureHeightText.getText().toString().equals(""))
                    {
                        Toast.makeText(getApplicationContext(), "CaptureHeight cannot be Empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        videoConfig.captureHeight = Integer.parseInt(captureHeightText.getText().toString());
                        ZegoExpressEngine.getEngine().setVideoConfig(videoConfig);
                        AppLogger.getInstance().callApi("Change captureHeight");
                    } catch (NumberFormatException e)
                    {
                        Toast.makeText(getApplicationContext(), "CaptureHeight is too large", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        encodeResolutionWidth.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    if (encodeResolutionWidth.getText().toString().equals(""))
                    {
                        Toast.makeText(getApplicationContext(), "EncodeResolutionWidth cannot be Empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        videoConfig.encodeWidth = Integer.parseInt(encodeResolutionWidth.getText().toString());
                        ZegoExpressEngine.getEngine().setVideoConfig(videoConfig);
                        AppLogger.getInstance().callApi("Change encodeResolutionWidth");
                    } catch (NumberFormatException e)
                    {
                        Toast.makeText(getApplicationContext(), "EncodeResolutionWidth is too large", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        encodeResolutionHeight.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    if (encodeResolutionHeight.getText().toString().equals(""))
                    {
                        Toast.makeText(getApplicationContext(), "EncodeResolutionHeight cannot be Empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        videoConfig.encodeHeight = Integer.parseInt(encodeResolutionHeight.getText().toString());
                        ZegoExpressEngine.getEngine().setVideoConfig(videoConfig);
                        AppLogger.getInstance().callApi("Change encodeResolutionHeight");
                    } catch (NumberFormatException e)
                    {
                        Toast.makeText(getApplicationContext(), "EncodeResolutionHeight is too large", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        fps.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    if (fps.getText().toString().equals(""))
                    {
                        Toast.makeText(getApplicationContext(), "FPS cannot be Empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        videoConfig.fps = Integer.parseInt(fps.getText().toString());
                        ZegoExpressEngine.getEngine().setVideoConfig(videoConfig);
                        AppLogger.getInstance().callApi("Change FPS");
                    } catch (NumberFormatException e)
                    {
                        Toast.makeText(getApplicationContext(), "FPS is too large", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        bitrate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    if (bitrate.getText().toString().equals(""))
                    {
                        Toast.makeText(getApplicationContext(), "Bitrate cannot be Empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        videoConfig.bitrate = Integer.parseInt(bitrate.getText().toString());
                        ZegoExpressEngine.getEngine().setVideoConfig(videoConfig);
                        AppLogger.getInstance().callApi("Change bitrate");
                    } catch (NumberFormatException e)
                    {
                        Toast.makeText(getApplicationContext(), "Bitrate is too large", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public void addWaterMarkInfoAndUrl(){
        //add an underline and url jump to the website
        document.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG );
        document.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://doc-zh.zego.im/article/11257");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }
    public void setColorEnhanceEvent(){
        mirrorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] options = getResources().getStringArray(R.array.mirrorMode);
                switch (options[position]) {
                    case "OnlyPreview":
                        engine.setVideoMirrorMode(ZegoVideoMirrorMode.ONLY_PREVIEW_MIRROR, ZegoPublishChannel.MAIN);
                        AppLogger.getInstance().callApi("Switch Exposure Mode: ContinuousAuto");
                        break;
                    case "OnlyPublish":
                        engine.setVideoMirrorMode(ZegoVideoMirrorMode.ONLY_PUBLISH_MIRROR, ZegoPublishChannel.MAIN);
                        AppLogger.getInstance().callApi("Switch Exposure Mode: Auto");
                        break;
                    case "Both":
                        engine.setVideoMirrorMode(ZegoVideoMirrorMode.BOTH_MIRROR, ZegoPublishChannel.MAIN);
                        AppLogger.getInstance().callApi("Switch Exposure Mode: Auto");
                        break;
                    case "None":
                        engine.setVideoMirrorMode(ZegoVideoMirrorMode.NO_MIRROR, ZegoPublishChannel.MAIN);
                        AppLogger.getInstance().callApi("Switch Exposure Mode: Auto");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        viewModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ZegoCanvas canvas = new ZegoCanvas(preview);
                canvas.viewMode = ZegoViewMode.values()[position];
                engine.startPreview(canvas);
                AppLogger.getInstance().callApi("startPreview, viewMode:%d", position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        cameraSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                engine.enableCamera(isChecked);
                AppLogger.getInstance().callApi("enableCamera, enable:%b", isChecked);
            }
        });
        cameraSelectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                boolean frontCamera = (position == 0);
                engine.useFrontCamera(frontCamera);
                AppLogger.getInstance().callApi("useFrontCamera, enable:%b", frontCamera);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        colorEnhanceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ZegoColorEnhancementParams p = new ZegoColorEnhancementParams();
                String strIntensity = intensityText.getText().toString();
                if (!strIntensity.isEmpty()) {
                    p.intensity = Float.parseFloat(strIntensity);
                }
                String strSkin = skinText.getText().toString();
                if (!strSkin.isEmpty()) {
                    p.skinToneProtectionLevel = Float.parseFloat(strSkin);
                }
                String strLip = lipText.getText().toString();
                if (!strLip.isEmpty()) {
                    p.lipColorProtectionLevel = Float.parseFloat(strLip);
                }
                engine.enableColorEnhancement(isChecked, p, ZegoPublishChannel.MAIN);
                AppLogger.getInstance().callApi("enableColorEnhancement, enable:%b, intensity:%f,skinLevel:%f,lipLevel:%f", isChecked,p.intensity,p.skinToneProtectionLevel,p.lipColorProtectionLevel);
            }
        });
        takePublishSnapshotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                engine.takePublishStreamSnapshot(new IZegoPublisherTakeSnapshotCallback() {
                    @Override
                    public void onPublisherTakeSnapshotResult(int errorCode, Bitmap image) {
                        if (image != null) {
                            String filename; // declaration file name
                            // filename to save time
                            Date date = new Date(System.currentTimeMillis());
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                            filename = sdf.format(date) + ".jpeg";
                            ZegoFileUtil.saveBitmap(filename, image, getApplicationContext());
                        } else {
                            String info = String.format("onPublisherTakeSnapshotResult, error:%d", errorCode);
                            Toast.makeText(getApplicationContext(), "info", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        orientationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ZegoOrientation orientation;
                orientation = ZegoOrientation.values()[position];
                engine.setAppOrientation(orientation);
                AppLogger.getInstance().callApi("setAppOrientation, orientation:%s", orientation.toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        hideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hide if
                if(isHide == false){
                    isHide = true;
                    scrollView.setVisibility(View.INVISIBLE);
                }else{
                    isHide = false;
                    scrollView.setVisibility(View.VISIBLE);
                }
            }
        });
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(colorEnhanceSwitch.isChecked()){
                    ZegoColorEnhancementParams p = new ZegoColorEnhancementParams();
                    try {
                        p.intensity = Float.parseFloat(intensityText.getText().toString());
                        p.skinToneProtectionLevel = Float.parseFloat(skinText.getText().toString());
                        p.lipColorProtectionLevel = Float.parseFloat(lipText.getText().toString());
                    }catch (NumberFormatException e)
                    {
                        Toast.makeText(getApplicationContext(), "param format error", Toast.LENGTH_SHORT).show();
                    }finally {
                        engine.enableColorEnhancement(true, p, ZegoPublishChannel.MAIN);
                        AppLogger.getInstance().callApi("enableColorEnhancement, enable:%b, intensity:%f,skinLevel:%f,lipLevel:%f", true,p.intensity,p.skinToneProtectionLevel,p.lipColorProtectionLevel);
                    }
                }
            }
        });
    }

    public void setLowlightEnhanceEvent(){
        adaptiveFpsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int minFps = Integer.parseInt(adaptiveFpsMinEdit.getText().toString());
                int maxFps = Integer.parseInt(adaptiveFpsMaxEdit.getText().toString());
                engine.enableCameraAdaptiveFPS(isChecked, minFps, maxFps, ZegoPublishChannel.MAIN);

                AppLogger.getInstance().callApi(String.format("enableCameraAdaptiveFPS, enable:%b, min:%d, max:%d", isChecked, minFps, maxFps));
            }
        });
        adaptiveFpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adaptiveFpsSwitch.isChecked()){
                    int minFps = Integer.parseInt(adaptiveFpsMinEdit.getText().toString());
                    int maxFps = Integer.parseInt(adaptiveFpsMaxEdit.getText().toString());
                    engine.enableCameraAdaptiveFPS(true, minFps, maxFps, ZegoPublishChannel.MAIN);

                    AppLogger.getInstance().callApi(String.format("enableCameraAdaptiveFPS, enable:%b, min:%d, max:%d", true, minFps, maxFps));
                }
            }
        });
        lowLightEnhanceModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] options = getResources().getStringArray(R.array.lowLightEnhancementMode);
                switch (options[position]) {
                    case "Off":
                        engine.setLowlightEnhancement(ZegoLowlightEnhancementMode.OFF, ZegoPublishChannel.MAIN);
                        AppLogger.getInstance().callApi("setLowlightEnhancement: Off");
                        break;
                    case "On":
                        engine.setLowlightEnhancement(ZegoLowlightEnhancementMode.ON, ZegoPublishChannel.MAIN);
                        AppLogger.getInstance().callApi("setLowlightEnhancement: On");
                        break;
                    case "Auto":
                        engine.setLowlightEnhancement(ZegoLowlightEnhancementMode.AUTO, ZegoPublishChannel.MAIN);
                        AppLogger.getInstance().callApi("setLowlightEnhancement: Auto");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        videoDenoiseModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String[] options = getResources().getStringArray(R.array.videoDenoiseMode);
                switch (options[position]) {
                    case "Off":
                        videoDenoiseParams.mode = ZegoVideoDenoiseMode.OFF;
                        engine.setVideoDenoiseParams(videoDenoiseParams, ZegoPublishChannel.MAIN);
                        AppLogger.getInstance().callApi("setVideoDenoiseMode: Off");
                        break;
                    case "On":
                        videoDenoiseParams.mode = ZegoVideoDenoiseMode.ON;
                        engine.setVideoDenoiseParams(videoDenoiseParams, ZegoPublishChannel.MAIN);
                        AppLogger.getInstance().callApi("setVideoDenoiseMode: On");
                        break;
                    case "Auto":
                        videoDenoiseParams.mode = ZegoVideoDenoiseMode.AUTO;
                        engine.setVideoDenoiseParams(videoDenoiseParams, ZegoPublishChannel.MAIN);
                        AppLogger.getInstance().callApi("setVideoDenoiseMode: Auto");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        videoDenoiseStrengthSpinner.setSelection(1);
        videoDenoiseStrengthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String[] options = getResources().getStringArray(R.array.videoDenoiseStrength);
                switch (options[position]) {
                    case "Light":
                        videoDenoiseParams.strength = ZegoVideoDenoiseStrength.LIGHT;
                        engine.setVideoDenoiseParams(videoDenoiseParams, ZegoPublishChannel.MAIN);
                        AppLogger.getInstance().callApi("setVideoDenoiseStrength: Light");
                        break;
                    case "Medium":
                        videoDenoiseParams.strength = ZegoVideoDenoiseStrength.MEDIUM;
                        engine.setVideoDenoiseParams(videoDenoiseParams, ZegoPublishChannel.MAIN);
                        AppLogger.getInstance().callApi("setVideoDenoiseStrength: Medium");
                        break;
                    case "Heavy":
                        videoDenoiseParams.strength = ZegoVideoDenoiseStrength.HEAVY;
                        engine.setVideoDenoiseParams(videoDenoiseParams, ZegoPublishChannel.MAIN);
                        AppLogger.getInstance().callApi("setVideoDenoiseStrength: Heavy");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

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

    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, EffectsBeautyActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        //log out the room
        engine.logoutRoom(roomID);
        //destroy the engine
        ZegoExpressEngine.destroyEngine(null);
        super.onDestroy();
    }
}
