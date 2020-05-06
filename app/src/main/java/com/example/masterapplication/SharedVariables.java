package com.example.masterapplication;

import android.app.Application;

import java.util.ArrayList;
import java.util.HashMap;

public class SharedVariables extends Application {
    private ArrayList<String> connectedEndpointIds = new ArrayList<>();
    HashMap<String, String> endpointIteratorMap = new HashMap<String, String>();
    private String matrix_a, matrix_b;

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


}
