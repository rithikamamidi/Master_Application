package com.example.masterapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private static final Strategy STRATEGY = Strategy.P2P_CLUSTER;
    private ConnectionsClient connectionsClient;
    private final String codeName = "master";
    private TextView statusText;
    private TextView executionTime;
    private ArrayList<String> connectedEndpointIds = new ArrayList<>();
    private final ArrayList<String> pendingConnectionEndpointIds = new ArrayList<>();
    private final Map<String, Integer> endPointsBatteryLevelsMap = new HashMap<>();
    private final Map<String, Boolean> endPointsRequestSent = new HashMap<>();
    private final Map<String, Boolean> endpointResult = new HashMap<>();
    private final ArrayList<String> failedEndpoints = new ArrayList<>();
    int[][] matrix_c;
    int flag = 0;
    private double distanceThreshold = 20.0;
    private double lati;
    public double jLatitude;
    public double jLongitude;
    private double longi;
    private TextView latitude_GPS;
    private TextView longitude_GPS;
    LocationManager locationManager;
    LocationListener locationListener;
    public long startTime;
    public static long endTime;
    public static long duration;
    private String path = Environment.getExternalStorageDirectory().getAbsolutePath();
    private String fileContents;
    public static long duration_master;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusText = findViewById(R.id.status_text);
        checkAndRequestPermissions();
        connectionsClient = Nearby.getConnectionsClient(this);
        latitude_GPS = findViewById(R.id.latitude);
        longitude_GPS = findViewById(R.id.longitude);
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        boolean isGPS_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        System.out.println("boolean Value:" + isGPS_enabled);

        if (isGPS_enabled) {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double longitude = location.getLongitude();
                    double latitude = location.getLatitude();
                    try {
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                        if (addressList.get(0) != null) {
                            lati = addressList.get(0).getLatitude();
                            jLatitude = addressList.get(0).getLatitude();
                            jLongitude = addressList.get(0).getLongitude();
                            longi = addressList.get(0).getLongitude();
                            latitude_GPS.setText("latitude:" + lati);
                            longitude_GPS.setText("longitude:" + longi);

                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                latitude_GPS.setText("getting Location");
                longitude_GPS.setText("getting Location");
            }
        } else {
            latitude_GPS.setText("getting latitude");
            longitude_GPS.setText("getting longitude");
        }
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
                                statusText.append("\n" + "Started discovery for clients");
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
//                    statusText.append("\n" + "Found end point" + endpointId + info);
//                    statusText.append("\n" + "Requesting connection" + endpointId);
                    connectionsClient.requestConnection(codeName, endpointId, connectionLifecycleCallback);
                }

                @Override
                public void onEndpointLost(String endpointId) {
                    handleFaultTolerance(endpointId);
                    ((SharedVariables) MainActivity.this.getApplication()).removeConnectedId(endpointId);
                    statusText.append("\n" + "Lost end point:"+ endpointId);
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
//                    statusText.append("\n" + "Connection initiated");
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    if (result.getStatus().isSuccess()) {
                        ((SharedVariables) MainActivity.this.getApplication()).addConnectedId(endpointId);
                        endpointResult.put(endpointId, false);
                        endPointsRequestSent.put(endpointId, false);
                        pendingConnectionEndpointIds.remove(endpointId);
                        connectedEndpointIds = ((SharedVariables) MainActivity.this.getApplication()).getConnectedEndpoints();
                        statusText.append("\n" + "Connection successful!! with client id: " + endpointId);
                    } else {
                        pendingConnectionEndpointIds.remove(endpointId);
                        statusText.append("\n" + "Connection failed :(" + endpointId);
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    endPointsRequestSent.remove(endpointId);
                    ((SharedVariables) MainActivity.this.getApplication()).removeConnectedId(endpointId);
                    handleFaultTolerance(endpointId);
                    statusText.append("\n" + "disconnected from the other end"+ endpointId);
                }
            };

    // Callbacks for receiving payloads
    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    String payloadString = new String(payload.asBytes(), StandardCharsets.UTF_8);
//                    statusText.append("\n" + "Payload received:" + payloadString + "ENDPOINT:" + endpointId);
                    try {
                        JSONObject jsonObject = new JSONObject(payloadString);
                        Iterator<String> keys = jsonObject.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            if (key.equals("batteryLevel")) {
                                handleBatteryLevel(endpointId, jsonObject);
                            } else if (key.equals("location")) {
                                handleLocation(endpointId, jsonObject);
                            } else if (key.equals("request")) {
                                String userConsent = jsonObject.get("request").toString();
                                handleRequest(endpointId, userConsent);
                            } else if (key.equals("calculation_result")) {
                                statusText.append("\n" + "Computation result received from endpoint:" + endpointId);
                                endpointResult.put(endpointId, true);

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

                                for (Map.Entry<String, Boolean> entry : endpointResult.entrySet()) {
                                    System.out.println("In testtttttttt");
                                    if (entry.getValue() == false) {
                                        break;
                                    }
                                    TextView text = findViewById(R.id.result);
                                    String result = "";
                                    for (int[] row : matrix_c)
                                        result+= Arrays.toString(row)+"\n";
                                    text.setText(result);
                                    endTime = System.currentTimeMillis();
                                    duration = (endTime - TakeInput.startTime);
                                    executionTime = findViewById(R.id.et);
                                    executionTime.setText("Execution Time on distributed network including network latency:" + duration + "ms");
                                }

                            }
                        }
                    } catch (JSONException e) {
//                        statusText.append("\n" + "Not a JSON object");
                    }
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
//                    statusText.append("\n" + "Payload transfer update");
                }
            };

    private void handleBatteryLevel(String endpointId, JSONObject jsonObject) throws JSONException {
        int batteryPercentage = Integer.parseInt(jsonObject.get("batteryLevel").toString());
        connectedEndpointIds = ((SharedVariables) MainActivity.this.getApplication()).getConnectedEndpoints();
//        statusText.append("\n" + "BEFORE CONNECTED ENDPOINTS" + connectedEndpointIds.size());
        endPointsBatteryLevelsMap.put(endpointId, batteryPercentage);
        method1(endPointsBatteryLevelsMap);

        int thresholdBatteryPercentage = 1;
        if (batteryPercentage < thresholdBatteryPercentage) {
            connectionsClient.disconnectFromEndpoint(endpointId);
            endPointsRequestSent.remove(endpointId);
            ((SharedVariables) MainActivity.this.getApplication()).removeConnectedId(endpointId);
            connectedEndpointIds = ((SharedVariables) MainActivity.this.getApplication()).getConnectedEndpoints();
            statusText.append("\n" + "CONNECTED ENDPOINTS" + connectedEndpointIds.size());
            statusText.append("\n" + "Less battery level! Disconnected client with endpointId" + endpointId);
        }
        connectedEndpointIds = ((SharedVariables) MainActivity.this.getApplication()).getConnectedEndpoints();
        Iterator<String> endpointsIterator = connectedEndpointIds.iterator();
        while (endpointsIterator.hasNext()) {
            JSONObject requestObject = new JSONObject();
            try {
                requestObject.put("request", "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String endpointId1 = endpointsIterator.next();
            if (endPointsRequestSent.get(endpointId1) != true) {
                endPointsRequestSent.put(endpointId1, true);
                connectionsClient.sendPayload(
                        endpointId1, Payload.fromBytes(requestObject.toString().getBytes(StandardCharsets.UTF_8)));
//                statusText.append("\n" + "Sent Request to:" + endpointId1);
            }

        }
    }

    public void method1(Map<String, Integer> map) {
        try {
            FileOutputStream fileOutputStream = this.openFileOutput("myMap.txt", Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            System.out.println("Saved to myMap.txt");

            objectOutputStream.writeObject(map);
            objectOutputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleLocation(String endpointId, JSONObject jsonObject) throws JSONException {
        String locationString = jsonObject.get("location").toString();
        JSONObject location = new JSONObject(locationString);
        System.out.println("HELLOOO LOCATIOON" + location.get("latitude") + "    " + location.get("longitude"));
        double lati = (double) location.get("latitude");
        double longi = (double) location.get("longitude");
        //Current location//jLatitude,jLongitude
        Location master = new Location("");
        Location dest = new Location("");

        master.setLatitude(jLatitude);
        master.setLongitude(jLongitude);

        dest.setLatitude(lati);
        dest.setLongitude(longi);
        float dist = master.distanceTo(dest);
        System.out.println("DISTANCE" + dist);
        if (dist > distanceThreshold) {
            connectionsClient.disconnectFromEndpoint(endpointId);
            endPointsRequestSent.remove(endpointId);
            ((SharedVariables) MainActivity.this.getApplication()).removeConnectedId(endpointId);
            connectedEndpointIds = ((SharedVariables) MainActivity.this.getApplication()).getConnectedEndpoints();
//            statusText.append("\n" + "CONNECTED ENDPOINTS" + connectedEndpointIds.size());
            statusText.append("\n" + "Far from master ! Disconnected client with endpointId" + endpointId);
        }
    }


    private void handleRequest(String endpointId, String userConsent) {
//        statusText.append("\n" + "User Consent:" + userConsent);
        if (userConsent.equals("yes")) {
//            statusText.append("SEND MATRIX");
        } else if (userConsent.equals("no")) {
            connectionsClient.disconnectFromEndpoint(endpointId);
            endPointsRequestSent.remove(endpointId);
            ((SharedVariables) MainActivity.this.getApplication()).removeConnectedId(endpointId);
            statusText.append("\n" + "Disconnected ! Client not okay with it ! " + endpointId);
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {

            return false;
        }
        return true;
    }


    public void enterMatrices(View view) {
        for (String endpointId : endpointResult.keySet())
            endpointResult.put(endpointId, false);
        Intent activity2intent = new Intent(getApplicationContext(), TakeInput.class);
        connectedEndpointIds = ((SharedVariables) MainActivity.this.getApplication()).getConnectedEndpoints();
        activity2intent.putStringArrayListExtra("slaves_id", connectedEndpointIds);
        startActivity(activity2intent);
    }

    public void handleFaultTolerance(String endpointId) {
        if (endpointResult.get(endpointId) == false) {
            failedEndpoints.add(endpointId);

            if (failedEndpoints.size() != 0) {
                String failedendpoint = failedEndpoints.get(0);
                String endpoint = null;
                Iterator hmiterator = endpointResult.entrySet().iterator();
                while (hmiterator.hasNext()) {
                    Map.Entry pair = (Map.Entry) hmiterator.next();
                    if (pair.getValue().equals(true)) {
                        endpoint = pair.getKey().toString();
                        break;
                    }
                }
                if (endpoint != null) {
                    JSONObject payload_object = new JSONObject();
                    String matrix_a = ((SharedVariables) MainActivity.this.getApplication()).getMatrix_a();
                    String matrix_b = ((SharedVariables) MainActivity.this.getApplication()).getMatrix_b();
                    String iteratorValue = ((SharedVariables) MainActivity.this.getApplication()).getIteratorValueForEndpoint(failedendpoint);
                    int r_a = ((SharedVariables) MainActivity.this.getApplication()).getRows_a();
                    int c_a = ((SharedVariables) MainActivity.this.getApplication()).getColumns_a();
                    int r_b = ((SharedVariables) MainActivity.this.getApplication()).getRows_b();
                    int c_b = ((SharedVariables) MainActivity.this.getApplication()).getColumns_b();
                    String[] iterators = iteratorValue.split(",");
                    System.out.println("Printing " + r_b);
                    System.out.println(c_b);
                    try {
                        payload_object.put("matrix_A", matrix_a);
                        payload_object.put("matrix_B", matrix_b);
                        payload_object.put("rows_a", r_a);
                        payload_object.put("columns_a", c_a);
                        payload_object.put("rows_b", r_b);
                        payload_object.put("columns_b", c_b);
                        payload_object.put("s_itr", Integer.parseInt(iterators[0]));
                        payload_object.put("e_itr", Integer.parseInt(iterators[1]));

                        failedEndpoints.remove(0);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Before sending payload, payload_object : " + payload_object.toString());
                    connectionsClient.sendPayload(
                            endpoint, Payload.fromBytes(payload_object.toString().getBytes(StandardCharsets.UTF_8)));

                }
            }
        }
    }
}
