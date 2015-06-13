package com.jadenine.circle.notification;

public class AndroidFilecast extends NotificationWithFile {
	public AndroidFilecast() {
		try {
			this.setPredefinedKeyValue("type", "filecast");	
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}

	public boolean setDeviceTokens(String[] tokens) {
		try {
			String fileId = uploadContents(tokens);
			return null != fileId && !fileId.isEmpty();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}