package com.example.masterapplication;

import android.app.Application;

import java.util.ArrayList;
import java.util.HashMap;

public class SharedVariables extends Application {
    private ArrayList<String> connectedEndpointIds = new ArrayList<>();
    HashMap<String, String> endpointIteratorMap = new HashMap<String, String>();
    private String matrix_a, matrix_b;
    private int r_a;
    private int c_a;
    private int r_b;
    private int c_b;

    public ArrayList<String> getConnectedEndpoints() {
        return connectedEndpointIds;
    }

    public void addConnectedId(String endpointId) {
        this.connectedEndpointIds.add(endpointId);
    }

    public void removeConnectedId(String endpointId) {
        this.connectedEndpointIds.remove(endpointId);
    }

    public void putEndPointAndIteratorValues(String endpoint, String iteratorValue) {
        this.endpointIteratorMap.put(endpoint, iteratorValue);
    }

    public String getIteratorValueForEndpoint(String endpoint) {
        return this.endpointIteratorMap.get(endpoint);
    }

    public String getMatrix_a() {
        return matrix_a;
    }

    public String getMatrix_b() {
        return matrix_b;
    }

    public void setMatrix_a(String matrix_a) {
        this.matrix_a = matrix_a;
    }

    public void setMatrix_b(String matrix_b) {
        this.matrix_b = matrix_b;
    }

    public void set_rows_a(int rows_a) {
        this.r_a = rows_a;
    }

    public void set_rows_b(int rows_b) {
        this.r_b = rows_b;
    }

    public void set_columns_a(int columns_a) {
        this.c_a = columns_a;
    }

    public void set_columns_b(int columns_b) {
        this.c_b = columns_b;
    }


    public int getRows_a() {
        return r_a;
    }

    public int getColumns_a() {
        return c_a;
    }

    public int getRows_b() {
        return r_b;
    }

    public int getColumns_b() {
        return c_b;
    }


}
