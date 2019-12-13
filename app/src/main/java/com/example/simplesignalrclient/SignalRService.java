package com.example.simplesignalrclient;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

import microsoft.aspnet.signalr.client.Credentials;
import microsoft.aspnet.signalr.client.Platform;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.http.Request;
import microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;
import microsoft.aspnet.signalr.client.transport.ClientTransport;
import microsoft.aspnet.signalr.client.transport.ServerSentEventsTransport;

public class SignalRService extends Service {
    private HubConnection mHubConnection;
    private HubProxy mHubProxy;
    private Handler mHandler; // to display Toast message
    private final IBinder mBinder = new LocalBinder(); // Binder given to clients

    public SignalRService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);
        startSignalR();
        return result;
    }

    @Override
    public void onDestroy() {
        mHubConnection.stop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Return the communication channel to the service.
        startSignalR();
        return mBinder;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public SignalRService getService() {
            // Return this instance of SignalRService so clients can call public methods
            return SignalRService.this;
        }
    }

    private void startSignalR() {
        Platform.loadPlatformComponent(new AndroidPlatformComponent());

        Log.e("SimpleSignalR", "Starting SignalR");
        String serverUrl = "http://ec2-18-191-247-3.us-east-2.compute.amazonaws.com:8011/";
        mHubConnection = new HubConnection(serverUrl);
        Credentials credentials = new Credentials() {
            @Override
            public void prepareRequest(Request request) {
                request.addHeader("Authorization", "bearer SdbQxhrI7Buwd7x_lYvqQGguNsYN21hy5ElZNDnJNR3odxwFl_f6aFS7yn8AJ4iARauTZi2NOhPsT2wV6IsLRPJWfbxZlD1rIoj73c8GVSEckkCtWAJBzcQYbwTKuosL6PL5-RknlncjR_wKpxXjz2o4oiVDCfLiW1OjVnOxeAecp2u3mgwGIx9IJpkqzAQRfVoOcY2Q46Uzyc_afkM3lVKL6lMzQpZ_vokvcYLqLg1cSid2_S3AZMF4V2FfNfcEl7r20-hzms-rDs-23b5OAGIu-LczZkXhlnT2EiJcmf0p4jl1unK4ELHSDloUD7rf3ZntOlfjsyWakaTIIuigMM74i3VHEYwh7GMsxL1YQpleWmHqqDRPxORBCTdLGbvZKPxu_Wc756v2tko5_O8SnxrhEXkd6A4TLAT8bVR3zSjzO29O_Y7Hwbq0VdvoYSlrx92eNOZ7_3IGdyfBMClE2i_n2S5jsn3SeD3j6gfYdYA2Hwnduu4Jx6vsAjIxO0YFvv_wXk_v5nJ_EDJL5qPJKA");
                request.addHeader("serialnumber","20191029");
            }
        };
        mHubConnection.setCredentials(credentials);
        String SERVER_HUB_CHAT = "iircadenotification";
        mHubProxy = mHubConnection.createHubProxy(SERVER_HUB_CHAT);


        String CLIENT_METHOD_BROADAST_MESSAGE = "broadcastMessage";
        mHubProxy.on(CLIENT_METHOD_BROADAST_MESSAGE,
                new SubscriptionHandler1<CustomMessage>() {
                    @Override
                    public void run(final CustomMessage msg) {
                        final String finalMsg =  "Id : " + msg.Id + ", Key : " + msg.Key +", Description : " + msg.Description;
                        Log.e("SimpleSignalR", finalMsg);

                        Intent intent1 = new Intent();

                        intent1.setAction("com.example.simplesignalrclient");
                        intent1.putExtra("msg", finalMsg);
                        sendBroadcast(intent1);

                        // display Toast message
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), finalMsg, Toast.LENGTH_LONG).show();

                            }
                        });
                    }
                }
                , CustomMessage.class);


        ClientTransport clientTransport = new ServerSentEventsTransport(mHubConnection.getLogger());
        SignalRFuture<Void> signalRFuture = mHubConnection.start(clientTransport);




        try {
            signalRFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e("SimpleSignalR", e.toString());
            return;
        }

       // sendMessage("Hello from BNK!");


    }
}
