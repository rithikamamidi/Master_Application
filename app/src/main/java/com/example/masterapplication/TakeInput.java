package com.example.masterapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class TakeInput extends AppCompatActivity {

    ArrayList<String> connection_ids;
    private ConnectionsClient connectionsClient;
    EditText rows_a;
    EditText columns_a;
    EditText rows_b;
    EditText columns_b;

    EditText matrix_A;
    EditText matrix_B;

    int r_a;
    int c_a;
    int r_b;
    int c_b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        connection_ids = getIntent().getStringArrayListExtra("slaves_id");
        setContentView(R.layout.activity_take_input);
        connectionsClient = Nearby.getConnectionsClient(this);
        rows_a = findViewById(R.id.rows_a);
        columns_a = findViewById(R.id.columns_a);
        rows_b = findViewById(R.id.rows_b);
        columns_b = findViewById(R.id.columns_b);

        matrix_A = findViewById(R.id.matrix_a);
        matrix_B = findViewById(R.id.matrix_b);
    }


    public void compute(View view) throws JSONException {


        int r_a = Integer.parseInt(rows_a.getText().toString());
        int c_a = Integer.parseInt(columns_a.getText().toString());
        int r_b = Integer.parseInt(rows_b.getText().toString());
        int c_b = Integer.parseInt(columns_b.getText().toString());

        String matrix_a = matrix_A.getText().toString();
        String matrix_b = matrix_B.getText().toString();

        int l = connection_ids.size();
        int start_itr, end_itr, p;
        p = r_a / l;
        for (int i = 0; i < l; i++) {
            if (r_a % l == 0) {
                start_itr = i * (p);
                end_itr = (i + 1) * (p);
            } else {
                start_itr = i * (p + 1);
                if (i == l - 1) end_itr = r_a;
                else end_itr = (i + 1) * (p + 1);
            }

            JSONObject payload_object = new JSONObject();
            try {
                payload_object.put("matrix_A", matrix_a);
                payload_object.put("matrix_B", matrix_b);
                payload_object.put("rows_a", r_a);
                payload_object.put("columns_a", c_a);
                payload_object.put("rows_b", r_b);
                payload_object.put("columns_b", c_b);
                payload_object.put("s_itr", start_itr);
                payload_object.put("e_itr", end_itr);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            connectionsClient.sendPayload(
                    connection_ids.get(i), Payload.fromBytes(payload_object.toString().getBytes(StandardCharsets.UTF_8)));
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
