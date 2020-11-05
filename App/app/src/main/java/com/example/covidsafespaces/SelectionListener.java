package com.example.covidsafespaces;

import org.json.JSONArray;

public interface SelectionListener {
    public void receiveMessage(JSONArray data, String path);
}
