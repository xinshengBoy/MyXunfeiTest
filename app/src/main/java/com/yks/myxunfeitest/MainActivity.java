package com.yks.myxunfeitest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * 描述：讯飞语音识别和语音合成
 * 作者：zzh
 */
public class MainActivity extends Activity implements View.OnClickListener{

    private Button btn_voice,btn_speak,btn_scan,btn_createCode;
    private EditText et_voice,et_scan;
    private ImageView iv_scan;
    private SpeechSynthesizer mTts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        //todo 实例化
        SpeechUtility.createUtility(MainActivity.this,SpeechConstant.APPID +"=5b7633f7");

        btn_voice = findViewById(R.id.btn_voice);
        et_voice = findViewById(R.id.et_voice);
        btn_speak = findViewById(R.id.btn_speak);
        et_scan = findViewById(R.id.et_scan);
        btn_scan = findViewById(R.id.btn_scan);
        btn_createCode = findViewById(R.id.btn_createCode);
        iv_scan = findViewById(R.id.iv_scan);
        initTTS();

        btn_voice.setOnClickListener(this);
        btn_speak.setOnClickListener(this);
        btn_scan.setOnClickListener(this);
        btn_createCode.setOnClickListener(this);
    }

    /**
     * 描述：语音合成初始化配置
     * 作者：zzh
     */
    private void initTTS(){
        mTts = SpeechSynthesizer.createSynthesizer(MainActivity.this,mTtsListner);
        mTts.setParameter(SpeechConstant.PARAMS,null);
        mTts.setParameter(SpeechConstant.ENGINE_TYPE,SpeechConstant.TYPE_CLOUD);//在线模式
        mTts.setParameter(SpeechConstant.VOICE_NAME,"xiaoyan");//发音人
        mTts.setParameter(SpeechConstant.SPEED,"50");//语速
        mTts.setParameter(SpeechConstant.PITCH,"50");
        mTts.setParameter(SpeechConstant.VOLUME,"60");//音量
        mTts.setParameter(SpeechConstant.STREAM_TYPE,"3");
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS,"true");//如果正在播放音乐，要停止音乐播放
    }

    /**
     * 描述：语音合成初始化监听
     */
    private InitListener mTtsListner = new InitListener() {
        @Override
        public void onInit(int i) {
            if (i != ErrorCode.SUCCESS){
                Toast.makeText(MainActivity.this,"初始化失败，错误码："+i,Toast.LENGTH_SHORT).show();
            }else {

            }
        }
    };

    private SynthesizerListener listener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {

        }

        @Override
        public void onBufferProgress(int i, int i1, int i2, String s) {

        }

        @Override
        public void onSpeakPaused() {

        }

        @Override
        public void onSpeakResumed() {

        }

        @Override
        public void onSpeakProgress(int i, int i1, int i2) {

        }

        @Override
        public void onCompleted(SpeechError speechError) {

        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

    @Override
    public void onClick(View view) {
        if (view == btn_voice){
            btnVoice();
        }else if (view == btn_speak){
            String speakWord = et_voice.getText().toString().trim();
            if (!speakWord.equals("")) {
                int code = mTts.startSpeaking(speakWord, listener);
                if (code != ErrorCode.SUCCESS) {
                    Toast.makeText(MainActivity.this, "播放失败：" + code, Toast.LENGTH_SHORT).show();
                }
            }else {
                mTts.startSpeaking("请输入要说的话", listener);
            }
        }else if (view == btn_scan){
            scanZxing();
        }else if (view == btn_createCode){
            String input = et_scan.getText().toString().trim();
            Bitmap bitmap = createCode(input);
            iv_scan.setImageBitmap(bitmap);
        }
    }

    /**
     * 描述：语音转文字
     * 作者：zzh
     */
    private void btnVoice(){
        RecognizerDialog dialog = new RecognizerDialog(this,null);
        dialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");//选择语言
        dialog.setParameter(SpeechConstant.ACCENT,"mandarin");
        dialog.setListener(new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {
                getResult(recognizerResult);
            }

            @Override
            public void onError(SpeechError speechError) {
                switch (speechError.getErrorCode()) {
                    case 20001:
                        Toast.makeText(MainActivity.this, "请检查网络", Toast.LENGTH_SHORT).show();
                        break;
                    case 20016:
                        Toast.makeText(MainActivity.this, "请允许程序获取录音权限", Toast.LENGTH_SHORT).show();
                        break;
                    case 10118:
                        Toast.makeText(MainActivity.this, "您好像没有说话", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "异常", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        dialog.show();
        Toast.makeText(MainActivity.this,"请开始说话",Toast.LENGTH_SHORT).show();
    }

    private void getResult(RecognizerResult recognizerResult) {
        String text = parselatResult(recognizerResult.getResultString());
        et_voice.append(text);
    }

    /**
     * 描述：解析语音转文字返回的数据格式
     * 作者：zzh
     * @param json 返回的数据格式
     * @return 解析出来的文字结果
     */
    private String parselatResult(String json){
        StringBuffer buffer = new StringBuffer();
        try {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject object = new JSONObject(tokener);
            JSONArray array = object.getJSONArray("ws");
            for (int i=0;i<array.length();i++){
                JSONArray items = array.getJSONObject(i).getJSONArray("cw");
                JSONObject obj = items.getJSONObject(0);
                buffer.append(obj.getString("w"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }

    private void scanZxing(){
        IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
        // 设置要扫描的条码类型，ONE_D_CODE_TYPES：一维码，QR_CODE_TYPES-二维码
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        //设置打开摄像头的Activity
        integrator.setCaptureActivity(ScanActivity.class);
        //底部的提示文字，设为""可以置空
        integrator.setPrompt("请扫描条形码");
        //前置或者后置摄像头
        integrator.setCameraId(0);
        //扫描成功的「哔哔」声，默认开启
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    /**
     * 描述：生成二维码
     * 作者：zzh
     */
    private Bitmap createCode(String input){
        Bitmap bitmap = null;
        BitMatrix result = null;
        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            result = writer.encode(input, BarcodeFormat.QR_CODE,400,400);
            BarcodeEncoder encoder = new BarcodeEncoder();
            bitmap = encoder.createBitmap(result);
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException iae){
            return null;
        }
        return bitmap;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if (result != null){
            String text = result.getContents();
            et_scan.setText(text);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mTts){
            mTts.stopSpeaking();
            mTts.destroy();
        }
    }
}
