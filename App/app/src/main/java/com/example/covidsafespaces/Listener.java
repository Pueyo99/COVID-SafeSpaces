package com.example.covidsafespaces;

import org.json.JSONObject;

public interface Listener {
    public void receiveMessage(JSONObject data);
}
