package com.jadenine.circle.notification;

public class AndroidCustomizedcast extends NotificationWithFile {
	public AndroidCustomizedcast() {
		try {
			this.setPredefinedKeyValue("type", "customizedcast");	
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}

	public boolean sendAliasFile(String aliasType, String[] alias) {
		try {
			setPredefinedKeyValue("alias_type", aliasType);
			String fileId = uploadContents(alias);
			return null != fileId && !fileId.isEmpty();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
