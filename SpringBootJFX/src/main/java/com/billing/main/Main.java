package com.billing.main;

import com.sun.javafx.application.*;
public class Main {

	@SuppressWarnings("restriction")
	public static void main(String[] args) {
		LauncherImpl.launchApplication(MyStoreApplication.class,FxPreloader.class,args);
	}
}
