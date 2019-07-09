package com.jagtarkhinda.jumpcount;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;
import io.particle.android.sdk.cloud.exceptions.ParticleCloudException;
import io.particle.android.sdk.utils.Async;

public class MainActivity extends AppCompatActivity {
    // MARK: Debug info
    private final String TAG = "JSK";

    // MARK: Particle Account Info
    private final String PARTICLE_USERNAME = "jsk5755@gmail.com";
    private final String PARTICLE_PASSWORD = "Alpha123";

    // MARK: Particle device-specific info
    private final String DEVICE_ID = "22001d000447363333343435";

    // MARK: Particle Publish / Subscribe variables
    private long subscriptionId;

    // MARK: Particle device
    private ParticleDevice mDevice;
    TextView cnt;
    EditText enterGoal;
    String ss;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialize your connection to the Particle API
        ParticleCloudSDK.init(this.getApplicationContext());

        // 2. Setup your device variable
        getDeviceFromCloud();

        cnt = (TextView) findViewById(R.id.output);
        enterGoal = (EditText) findViewById(R.id.enterGoal);
    }

    /**
     * Custom function to connect to the Particle Cloud and get the device
     */
    public void getDeviceFromCloud() {
        // This function runs in the background
        // It tries to connect to the Particle Cloud and get your device
        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {

            @Override
            public Object callApi(@NonNull ParticleCloud particleCloud) throws ParticleCloudException, IOException {
                particleCloud.logIn(PARTICLE_USERNAME, PARTICLE_PASSWORD);
                mDevice = particleCloud.getDevice(DEVICE_ID);
                return -1;
            }

            @Override
            public void onSuccess(Object o) {
                Log.d(TAG, "Successfully got device from Cloud");
            }

            @Override
            public void onFailure(ParticleCloudException exception) {
                Log.d(TAG, exception.getBestMessage());
            }
        });
    }

    public void setGoal(View view) {

        getFromDevice();
        sendToDevice(enterGoal.getText().toString());
        enterGoal.onEditorAction(EditorInfo.IME_ACTION_DONE);
        cnt.setText("0");
        cnt.setTextSize(100);

    }


    //calling this function to get data from hardware
        public void getFromDevice() {

            if (mDevice == null) {
                Log.d(TAG, "Cannot find device");
                return;
            }

            Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {

                @Override
                public Object callApi(@NonNull ParticleCloud particleCloud) throws ParticleCloudException, IOException {
                    subscriptionId = ParticleCloudSDK.getCloud().subscribeToAllEvents(
                            "jmpCount",  // the first argument, "eventNamePrefix", is optional
                            new ParticleEventHandler() {
                                public void onEvent(String eventName, ParticleEvent event) {
                                    Log.i(TAG, "Received event with payload: " + event.dataPayload);
                                    //   ss = (event.dataPayload).toString();

                                    runOnUiThread(new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            cnt.setText(event.dataPayload.toString());
                                            if(enterGoal.getText().toString().equals(event.dataPayload.toString()))
                                            {
                                                cnt.setText("Well Done:)");
                                                cnt.setTextSize(30);
                                            }
                                        }
                                    }));
                                }

                                public void onEventError(Exception e) {
                                    Log.e(TAG, "Event error: ", e);
                                }
                            });
                    return -1;
                }

                @Override
                public void onSuccess(Object o) {
                    Log.d(TAG, "Successfully got device from Cloud");
                }

                @Override
                public void onFailure(ParticleCloudException exception) {
                    Log.d(TAG, exception.getBestMessage());
                }
            });

        }

    //calling this function to sent data to hardware
    public void sendToDevice(String commandToSend){
        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {
            @Override
            public Object callApi(@NonNull ParticleCloud particleCloud) throws ParticleCloudException, IOException {

                // 2. build a list and put the r,g,b into the list
                List<String> functionParameters = new ArrayList<String>();
                functionParameters.add(commandToSend);

                // 3. send the command to the particle
                try {
                    mDevice.callFunction("totalJumps", functionParameters);
                } catch (ParticleDevice.FunctionDoesNotExistException e) {
                    e.printStackTrace();
                }

                return -1;
            }

            @Override
            public void onSuccess(Object o) {
                Log.d(TAG, "Sent colors command to device.");
            }

            @Override
            public void onFailure(ParticleCloudException exception) {
                Log.d(TAG, exception.getBestMessage());
            }
        });
    }
}