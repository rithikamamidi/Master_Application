package com.example.masterapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MainActivity extends AppCompatActivity {
    private static final Strategy STRATEGY = Strategy.P2P_CLUSTER;
    private ConnectionsClient connectionsClient;
    private final String codeName = "master";
    private TextView statusText;
    private final ArrayList<String> connectedEndpointIds = new ArrayList<>();
    private final ArrayList<String> pendingConnectionEndpointIds = new ArrayList<>();
    private final Map<String, Integer> endPointsBatteryLevelsMap = new HashMap<>();
    int[][] matrix_c;
    int flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusText = findViewById(R.id.status_text);
        checkAndRequestPermissions();
        connectionsClient = Nearby.getConnectionsClient(this);

    }

    private void startDiscovery() {
        DiscoveryOptions discoveryOptions =
                new DiscoveryOptions.Builder().setStrategy(STRATEGY).build();
        Nearby.getConnectionsClient(this)
                .startDiscovery("com.example.slaveapplication", endpointDiscoveryCallback, discoveryOptions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                statusText.append("\n" + "Started discovery");
                                System.out.println("Started discovery");
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                statusText.append("\n" + "Cannot start discovery" + e);
                                System.out.println("Cannot start discovery" + e);
                            }
                        });
    }

    // Callbacks for finding other devices
    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                    System.out.println("Found end point" + endpointId + info);
                    statusText.append("\n" + "Found end point" + endpointId + info);
                    statusText.append("\n" + "Requesting connection" + endpointId);
                    connectionsClient.requestConnection(codeName, endpointId, connectionLifecycleCallback);
                }

                @Override
                public void onEndpointLost(String endpointId) {
                    connectedEndpointIds.remove(endpointId);
                    statusText.append("\n" + "Lost end point");
                    System.out.println("Lost end point" + endpointId);
                }
            };

    // Callbacks for connections to other devices
    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    pendingConnectionEndpointIds.add(endpointId);
                    connectionsClient.acceptConnection(endpointId, payloadCallback);
                    statusText.append("\n" + "Connection initiated");
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    if (result.getStatus().isSuccess()) {
                        connectedEndpointIds.add(endpointId);
                        pendingConnectionEndpointIds.remove(endpointId);
                        statusText.append("\n" + "Connection successful!!" + connectedEndpointIds.get(0));
                    } else {
                        pendingConnectionEndpointIds.remove(endpointId);
                        statusText.append("\n" + "Connection failed :(");
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    connectedEndpointIds.remove(endpointId);
                    statusText.append("\n" + "disconnected from the other end");
                }
            };

    // Callbacks for receiving payloads
    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    String payloadString = new String(payload.asBytes(), StandardCharsets.UTF_8);
                    statusText.append("\n" + "Payload received:" + payloadString);
                    try {
                        JSONObject jsonObject = new JSONObject(payloadString);
                        Iterator<String> keys = jsonObject.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            if (key.equals("batteryLevel")) {
                                handleBatteryLevel(endpointId, jsonObject);
                            } else if (key.equals("request")) {
                                String userConsent = jsonObject.get("request").toString();
                                handleRequest(endpointId, userConsent);
                            } else if (key.equals("calculation_result")) {
                                String calculation_result = jsonObject.get("calculation_result").toString();
                                String[] array_c = calculation_result.split(",");
                                int s_itr = Integer.parseInt(jsonObject.get("s_itr").toString());
                                int e_itr = Integer.parseInt(jsonObject.get("e_itr").toString());
                                int r_a = Integer.parseInt(jsonObject.get("r_a").toString());
                                int r_b = Integer.parseInt(jsonObject.get("r_b").toString());
                                int c_a = Integer.parseInt(jsonObject.get("c_a").toString());
                                int c_b = Integer.parseInt(jsonObject.get("c_b").toString());
                                if (flag == 0) {
                                    matrix_c = new int[r_a][c_b];
                                    flag = 1;
                                }

                                int i = 0;
                                for (int x = s_itr; x < e_itr; x++) {
                                    for (int k = 0; k < r_b; k++) {
                                        matrix_c[x][k] = Integer.parseInt(array_c[i]);
                                        System.out.println("HERE" + matrix_c[x][k]);
                                        i++;
                                    }
                                }
                                for (int[] row : matrix_c) {
                                    System.out.println("Matrix C RESULTTTTT");
                                    System.out.println(Arrays.toString(row));
                                }
                            }
                        }
                    } catch (JSONException e) {
                        statusText.append("\n" + "Not a JSON object");
                    }
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                    statusText.append("\n" + "Payload transfer update");
                }
            };

    private void handleBatteryLevel(String endpointId, JSONObject jsonObject) throws JSONException {
        int batteryPercentage = Integer.parseInt(jsonObject.get("batteryLevel").toString());
        statusText.append("\n" + "BEFORE CONNECTED ENDPOINTS" + connectedEndpointIds.size());
        endPointsBatteryLevelsMap.put(endpointId, batteryPercentage);
        int thresholdBatteryPercentage = 10;
        if (batteryPercentage < thresholdBatteryPercentage) {
            connectionsClient.disconnectFromEndpoint(endpointId);
            connectedEndpointIds.remove(endpointId);
            statusText.append("\n" + "CONNECTED ENDPOINTS" + connectedEndpointIds.size());
            statusText.append("\n" + "Less battery level! Disconnected client with endpointId" + endpointId);
        }
        Iterator<String> endpointsIterator = pendingConnectionEndpointIds.iterator();
        while (endpointsIterator.hasNext()) {
            JSONObject requestObject = new JSONObject();
            try {
                requestObject.put("request", "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            connectionsClient.sendPayload(
                    endpointsIterator.next(), Payload.fromBytes(requestObject.toString().getBytes(StandardCharsets.UTF_8)));
            statusText.append("\n" + "Sent Request to:" + endpointId);
        }
    }

    private void handleRequest(String endpointId, String userConsent) {
        statusText.append("\n" + "User Consent:" + userConsent);
        if (userConsent.equals("yes")) {
            statusText.append("SEND MATRIX");
        } else if (userConsent.equals("no")) {
            connectionsClient.disconnectFromEndpoint(endpointId);
            connectedEndpointIds.remove(endpointId);
            statusText.append("\n" + "Disconnected ! Client not okay with it ! :( " + endpointId);
        }
    }

    public void findDevices(View view) {
        startDiscovery();
    }

    public boolean checkAndRequestPermissions() {
        int internet = ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET);
        int loc = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        int loc2 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (internet != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.INTERNET);
        }
        if (loc != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (loc2 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions((Activity) this, listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]), 1);
            return false;
        }
        return true;
    }


    public void enterMatrices(View view) {
        Intent activity2intent = new Intent(getApplicationContext(), TakeInput.class);
        activity2intent.putStringArrayListExtra("slaves_id", connectedEndpointIds);
        startActivity(activity2intent);
    }
}
