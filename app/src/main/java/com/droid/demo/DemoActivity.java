package com.droid.demo;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.ibm.cloud.sdk.core.http.HttpMediaType;
import com.ibm.cloud.sdk.core.service.security.IamOptions;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneHelper;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;
import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType;
import com.ibm.watson.speech_to_text.v1.SpeechToText;
import com.ibm.watson.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.speech_to_text.v1.websocket.BaseRecognizeCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DemoActivity extends AppCompatActivity {
    private static final String TAG = "DemoActivity";
    private TextView tvMessage;
    String child = "audio_file.flac";
    int audioFile = R.raw.audio_file;

    private MicrophoneHelper microphoneHelper;
    private MicrophoneInputStream capture;
    private SpeechToText speechService;
    private Context appContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        appContext = DemoActivity.this;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        tvMessage = findViewById(R.id.tvMessage);

//        SpeechRecognizer speechRecognizer = new SpeechRecognizer(this);
//        InputStream streamResult = getResources().openRawResource(R.raw.sr);
//        StreamPlayer streamPlayer = new StreamPlayer();
//        streamPlayer.playStream(streamResult);


        speechService = new SpeechToText();
        IamOptions options = new IamOptions.Builder()
                .apiKey("6lNGIf2MkbgjCj5yzAIUwaH65KSr4KmV9B8b12Z3aXeK")//My private key, got from IBM
//                .url("https://gateway-lon.watsonplatform.net/speech-to-text/api")
                .build();
        speechService.setIamCredentials(options);
        speechService.setEndPoint("https://gateway-lon.watsonplatform.net/speech-to-text/api");


        createExternalStoragePrivateFile();

//        MediaPlayer mediaPlayer = MediaPlayer.create(this, audioFile);
//        mediaPlayer.start();


        microphoneHelper = new MicrophoneHelper(this);
        capture = microphoneHelper.getInputStream(true);


//        speechService.recognizeUsingWebSocket(new MicrophoneInputStream(false),
//                getRecognizeOptions(capture), new BaseRecognizeCallback() {
//                    @Override
//                    public void onTranscription(SpeechResults speechResults){
//                        String text = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript();
//                        System.out.println(text);
//                    }
//
//                    @Override
//                    public void onError(Exception e) {
//                    }
//
//                    @Override public void onDisconnected() {
//                    }
//
//                });


        speechService.recognizeUsingWebSocket(getRecognizeOptions(capture),
                new MicrophoneRecognizeDelegate());

        /*File audio = new File(getExternalFilesDir(null), child);
        try {
            RecognizeOptions options1 = new RecognizeOptions.Builder()
                    .audio(audio)
                    .contentType(HttpMediaType.AUDIO_FLAC)
                    .build();
            SpeechRecognitionResults transcript = speechService.recognize(options1).execute().getResult();
            System.out.println("Transcription : " + transcript);
            tvMessage.setText(transcript.getResults().get(0).getAlternatives().get(0).getTranscript());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/


        playVideo();

    }

    private void playVideo() {
        SimpleExoPlayerView simpleExoPlayerView = findViewById(R.id.player_view);
        simpleExoPlayerView.requestFocus();

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);

        DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        simpleExoPlayerView.setPlayer(player);

        player.setPlayWhenReady(true);
/*        MediaSource mediaSource = new HlsMediaSource(Uri.parse("https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8"),
                mediaDataSourceFactory, mainHandler, null);*/

        DefaultExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

//        MediaSource mediaSource = new ExtractorMediaSource(Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"),
//                mediaDataSourceFactory, extractorsFactory, null, null);
//        MediaSource mediaSource = new HlsMediaSource(Uri.parse("https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8"),
//                mediaDataSourceFactory, mainHandler, null);
        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(appContext,
                Util.getUserAgent(appContext, "android_wave_list"), defaultBandwidthMeter);
        MediaSource mediaSource = new HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse("https://player.vimeo.com/external/286837723.m3u8?s=3df60d3c1c6c7a11df4047af99c5e05cc2e7ae96"));
        player.prepare(mediaSource);
    }

    void createExternalStoragePrivateFile() {
        File file = new File(getExternalFilesDir(null), child);
        try {
            InputStream is = getResources().openRawResource(audioFile);
            OutputStream os = new FileOutputStream(file);
            byte[] data = new byte[is.available()];
            is.read(data);
            os.write(data);
            is.close();
            os.close();
        } catch (IOException e) {
            Log.w("ExternalStorage", "Error writing " + file, e);
        }
    }

    private SpeechToText initSpeechToTextService() {
        SpeechToText service = new SpeechToText();
        String username = getString(R.string.speech_text_username);
        String password = getString(R.string.speech_text_password);
        service.setUsernameAndPassword(username, password);
        service.setEndPoint(getString(R.string.speech_text_url));
        return service;
    }

    private RecognizeOptions getRecognizeOptions(InputStream captureStream) {
        return new RecognizeOptions.Builder()
                .audio(captureStream)
                .contentType(ContentType.OPUS.toString())
                .model("en-US_BroadbandModel")
                .interimResults(true)
                .inactivityTimeout(2000)
                .build();
    }

    private class MicrophoneRecognizeDelegate extends BaseRecognizeCallback {
        @Override
        public void onTranscription(SpeechRecognitionResults speechResults) {
            System.out.println(speechResults);
            if (speechResults.getResults() != null && !speechResults.getResults().isEmpty()) {
                final String text = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript();
                Log.e(TAG, "onTranscription: " + text);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvMessage.setText(text);
                        tvMessage.setSelected(true);
                    }
                });
            }
        }

        @Override
        public void onError(Exception e) {
            try {
                // This is critical to avoid hangs
                // (see https://github.com/watson-developer-cloud/android-sdk/issues/59)
                capture.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            showError(e);
//            enableMicButton();
        }

        @Override
        public void onDisconnected() {
//            enableMicButton();
        }
    }
    private void showError(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DemoActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                // Update the icon background
//                mic.setBackgroundColor(Color.LTGRAY);
            }
        });
    }

}
