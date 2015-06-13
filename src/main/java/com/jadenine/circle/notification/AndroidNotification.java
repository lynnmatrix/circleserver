package com.jadenine.circle.notification;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public abstract class AndroidNotification extends UmengNotification {
	// Keys can be set in the payload level
	protected static final HashSet<String> PAYLOAD_KEYS = new HashSet<String>(Arrays.asList(new String[]{
			"display_type"}));
	
	// Keys can be set in the body level
	protected static final HashSet<String> BODY_KEYS = new HashSet<String>(Arrays.asList(new String[]{
			"ticker", "title", "text", "builder_id", "icon", "largeIcon", "img", "play_vibrate", "play_lights", "play_sound",
			"sound", "after_open", "url", "activity", "custom"}));
	
	// Set key/value in the rootJson, for the keys can be set please see ROOT_KEYS, PAYLOAD_KEYS, 
	// BODY_KEYS and POLICY_KEYS.
	@Override
	public boolean setPredefinedKeyValue(String key, Object value) throws Exception {
		if (ROOT_KEYS.contains(key)) {
			// This key should be in the root level
			notification.put(key, value);
		} else if (PAYLOAD_KEYS.contains(key)) {
			// This key should be in the payload level
			Map<String, Object> payloadJson = null;
			if (notification.containsKey("payload")) {
				payloadJson = (Map<String, Object>) notification.get("payload");
			} else {
				payloadJson = new HashMap<>();
				notification.put("payload", payloadJson);
			}
			payloadJson.put(key, value);
		} else if (BODY_KEYS.contains(key)) {
			// This key should be in the body level
			Map<String, Object> bodyJson = null;
			Map<String, Object> payloadJson = null;
			// 'body' is under 'payload', so build a payload if it doesn't exist
			if (notification.containsKey("payload")) {
				payloadJson = (Map<String, Object>) notification.get("payload");
			} else {
				payloadJson = new HashMap<>();
				notification.put("payload", payloadJson);
			}
			// Get body JSONObject, generate one if not existed
			if (payloadJson.containsKey("body")) {
				bodyJson = (Map<String, Object>) payloadJson.get("body");
			} else {
				bodyJson = new HashMap<>();
				payloadJson.put("body", bodyJson);
			}
			bodyJson.put(key, value);
		} else if (POLICY_KEYS.contains(key)) {
			// This key should be in the body level
			Map<String, Object> policyJson = null;
			if (notification.containsKey("policy")) {
				policyJson = (Map<String, Object>) notification.get("policy");
			} else {
				policyJson = new HashMap<>();
				notification.put("policy", policyJson);
			}
			policyJson.put(key, value);
		} else {
			if (key == "payload" || key == "body" || key == "policy" || key == "extra") {
				throw new Exception("You don't need to set value for " + key + " , just set values for the sub keys in it.");
			} else {
				throw new Exception("Unknown key: " + key);
			}
		}
		return true;
	}
	
	// Set extra key/value for Android notification
	public boolean setExtraField(String key, String value) throws Exception {
		Map<String, Object> payloadJson = null;
		Map<String, Object> extraJson = null;
		if (notification.containsKey("payload")) {
			payloadJson = (Map<String, Object>) notification.get("payload");
		} else {
			payloadJson = new HashMap<>();
			notification.put("payload", payloadJson);
		}
		
		if (payloadJson.containsKey("extra")) {
			extraJson = (Map<String, Object>) payloadJson.get("extra");
		} else {
			extraJson = new HashMap<>();
			payloadJson.put("extra", extraJson);
		}
		extraJson.put(key, value);
		return true;
	}
	
}
