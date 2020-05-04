package com.example.masterapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class TakeInput extends AppCompatActivity {

    ArrayList<String> connection_ids;
    private ConnectionsClient connectionsClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connection_ids = getIntent().getStringArrayListExtra("slaves_id");
        setContentView(R.layout.activity_take_input);
        connectionsClient = Nearby.getConnectionsClient(this);
    }


    public void compute(View view) throws JSONException {
        int r_a = R.id.rows_a;
        int c_a = R.id.columns_a;
        int r_b = R.id.rows_b;
        int c_b = R.id.columns_b;
        String S_A = String.valueOf(R.id.take_inputA);
        String S_B = String.valueOf(R.id.take_inputB);
        int l = connection_ids.size();
        int start_itr, end_itr,p;
        p = r_a/l;
        for(int i=0;i<l;i++) {
            if(p == 0)
            {
                start_itr = i*(p);
                end_itr = (i+1)*(p);
            }
            else
            {
                start_itr = i*(p+1);
                if(i==l-1) end_itr = r_a;
                else end_itr= (i+1)*(p+1);
            }

            JSONObject payload_object = new JSONObject();
            try {
                payload_object.put("matrix_A",S_A);
                payload_object.put("matrix_B",S_B);
                payload_object.put("rows_a",r_a);
                payload_object.put("columns_a",c_a);
                payload_object.put("rows_b",r_b);
                payload_object.put("columns_b",c_b);
                payload_object.put("s_itr", start_itr);
                payload_object.put("e_itr",end_itr);
            }
            catch (JSONException e){
                e.printStackTrace();
            }
//            JSONObject hi = new JSONObject();
//            hi.put("hi","hi");
            connectionsClient.sendPayload(
                    connection_ids.get(i), Payload.fromBytes(payload_object.toString().getBytes(StandardCharsets.UTF_8)));

            TextView t = (TextView) findViewById(R.id.take_inputB);
            t.append("SENT TEXTTT");
        }


        /*int[][] A = new int[r_a][c_a];
        int[][] B = new int[r_b][c_b];
        int k=0;

        for(int i=0;i<r_a;i++)
        {
            for(int j=0;j<c_a;j++) {
                A[i][j] = Integer.valueOf(S_A.charAt(k));
                k = k+2;
            }
        }
        k=0;

        for(int i=0;i<r_b;i++)
        {
            for(int j=0;j<r_b;j++)
            {
                B[i][j] = Integer.valueOf(S_B.charAt(k));
                k = k+2;
            }
        }

        int[][] c = new int[r_a][c_b];
        for(int i=start_itr;i<end_itr;i++)
        {
            for(int j=0;j<c_b;j++)
            {
                for(int k=0;k<r_b;k++)
                {
                    c[i][j] = c[i][j]+a[i][k]+b[k][j];
                }
            }
        }
        */


    }
}
