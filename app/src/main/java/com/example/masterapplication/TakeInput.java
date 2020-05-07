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

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class TakeInput extends AppCompatActivity {

    ArrayList<String> connection_ids;
    private ConnectionsClient connectionsClient;
    EditText rows_a;
    EditText columns_a;
    EditText rows_b;
    EditText columns_b;

    EditText matrix_A;
    EditText matrix_B;
    public static long startTime;


    public long startTime1;
    public static long endTime1;
    public static long duration1;
    TextView executionTime1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        connection_ids = ((SharedVariables) TakeInput.this.getApplication()).getConnectedEndpoints();
        setContentView(R.layout.activity_take_input);
        connectionsClient = Nearby.getConnectionsClient(this);
        rows_a = findViewById(R.id.rows_a);
        columns_a = findViewById(R.id.columns_a);
        rows_b = findViewById(R.id.rows_b);
        columns_b = findViewById(R.id.columns_b);

        matrix_A = findViewById(R.id.matrix_a);
        matrix_B = findViewById(R.id.matrix_b);
    }


    public void compute(View view) {
        startTime = System.currentTimeMillis();
        int r_a = Integer.parseInt(String.valueOf(rows_a.getText()));
        int c_a = Integer.parseInt(String.valueOf(columns_a.getText()));
        int r_b = Integer.parseInt(String.valueOf(rows_b.getText()));
        int c_b = Integer.parseInt(String.valueOf(columns_b.getText()));

        String matrix_a = matrix_A.getText().toString();
        String matrix_b = matrix_B.getText().toString();

        ((SharedVariables) TakeInput.this.getApplication()).setMatrix_a(matrix_a);
        ((SharedVariables) TakeInput.this.getApplication()).setMatrix_b(matrix_b);
        ((SharedVariables) TakeInput.this.getApplication()).set_rows_a(r_a);
        ((SharedVariables) TakeInput.this.getApplication()).set_rows_b(r_b);
        ((SharedVariables) TakeInput.this.getApplication()).set_columns_a(c_a);
        ((SharedVariables) TakeInput.this.getApplication()).set_columns_b(c_b);

        connection_ids = ((SharedVariables) TakeInput.this.getApplication()).getConnectedEndpoints();
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

            String iteratorValues = start_itr + "," + end_itr;
            ((SharedVariables) TakeInput.this.getApplication()).putEndPointAndIteratorValues(connection_ids.get(i), iteratorValues);
            connectionsClient.sendPayload(
                    connection_ids.get(i), Payload.fromBytes(payload_object.toString().getBytes(StandardCharsets.UTF_8)));

            finish();
        }
    }

    public void computeOnMaster(View view) {
        startTime1 = System.currentTimeMillis();
        int r_a = Integer.parseInt(String.valueOf(rows_a.getText()));
        int c_a = Integer.parseInt(String.valueOf(columns_a.getText()));
        int r_b = Integer.parseInt(String.valueOf(rows_b.getText()));
        int c_b = Integer.parseInt(String.valueOf(columns_b.getText()));

        String matrix_a = matrix_A.getText().toString();
        String matrix_b = matrix_B.getText().toString();

        ((SharedVariables) TakeInput.this.getApplication()).setMatrix_a(matrix_a);
        ((SharedVariables) TakeInput.this.getApplication()).setMatrix_b(matrix_b);
        ((SharedVariables) TakeInput.this.getApplication()).set_rows_a(r_a);
        ((SharedVariables) TakeInput.this.getApplication()).set_rows_b(r_b);
        ((SharedVariables) TakeInput.this.getApplication()).set_columns_a(c_a);
        ((SharedVariables) TakeInput.this.getApplication()).set_columns_b(c_b);

        int[][] matrix_a_master = new int[r_a][c_a];
        int[][] matrix_b_master = new int[r_b][c_b];
        int[][] matrix_c = new int[r_a][c_b];
        String[] array_a = matrix_a.split(",");
        String[] array_b = matrix_b.split(",");

        int i = 0;
        for (int j = 0; j < r_a; j++) {
            for (int k = 0; k < c_a; k++) {
                matrix_a_master[j][k] = Integer.parseInt(array_a[i]);
                System.out.println("HERE" + matrix_a_master[j][k]);
                i++;
            }
        }
        i = 0;
        for (int j = 0; j < r_b; j++) {
            for (int k = 0; k < c_b; k++) {
                matrix_b_master[j][k] = Integer.parseInt(array_b[i]);
                System.out.println("HERE B" + matrix_b_master[j][k]);
                i++;
            }
        }

        for (int x = 0; x < r_a; x++) {
            for (int j = 0; j < c_b; j++) {
                matrix_c[x][j] = 0;
                for (int k = 0; k < c_a; k++) {
                    matrix_c[x][j] += matrix_a_master[x][k] * matrix_b_master[k][j];
                }//end of k loop
            }//end of j loop
        }

        endTime1 = System.currentTimeMillis();
        duration1 = (endTime1 - startTime1);
        System.out.println("durataion in master:" + duration1);
        MainActivity.duration_master = duration1;
        executionTime1 = findViewById(R.id.et1);
        executionTime1.setText("Execution Time when on master: " + duration1);
    }
}
