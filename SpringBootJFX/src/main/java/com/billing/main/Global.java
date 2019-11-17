package com.billing.main;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.billing.dto.WindowState;
import com.billing.utils.TabContent;

import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public abstract class Global {

	private static Logger logger = Logger.getLogger(Global.class.getName());

	private static String getDefaultAppDataPath() {
		String path = null;

		try {
			path = System.getenv("ProgramData");
			if (path == null) {
				path = System.getenv("ALLUSERSPROFILE");
			}
			if (path == null) {
				path = System.getenv("PUBLIC");
			}
			if (path == null) {
				path = System.getenv("USERPROFILE");
			}
			if (path == null) {
				path = System.getProperty("user.home");
			}
		} catch (SecurityException e) {
			logger.logp(Level.SEVERE, Global.class.getName(), "getAppDataFolderPath",
					"Error on enquiring about Environment variables", e);
		}

		if (path == null) {
			path = "/"; // it represents the root of the current drive
		}

		return path;
	}

	public static boolean closeTabs(final TabPane tabPane, final Tab leaveItOpen) {
		final ObservableList<Tab> tabs = tabPane.getTabs();
		final List<Tab> tabsToRemove = new ArrayList<>(tabs.size());

		for (Tab tab : tabs) {
			if (!tab.equals(leaveItOpen)) {
				tabPane.getSelectionModel().select(tab);
				TabContent controller = (TabContent) tab.getProperties().get("controller");
				if (controller.shouldClose()) {
					tabsToRemove.add(tab); // mark this tab to be removed
				} else {
					return false;
				}
			}
		}

		tabs.removeAll(tabsToRemove); // actually remove the tags here
		return true;

	}

	public static String getUserHomeDirectory() {
		String homePath = null;

		try {
			homePath = System.getProperty("user.home", "/");
		} catch (Exception e) {
			logger.logp(Level.SEVERE, Global.class.getName(), "getUserHomeDirectory",
					"Error in reading User's Home Directory", e);
			homePath = "/";
		}

		return Paths.get(homePath).toAbsolutePath().toString();
	}

	/**
	 * 
	 * @return Returns the default position and size of the application's main
	 *         window
	 */
	public static WindowState getDefaultWindowState() {
		// set width / height values to be 90% of users screen resolution
		Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

		WindowState windowState = new WindowState();
		windowState.setWidth(screenBounds.getWidth() * 0.9);
		windowState.setHeight(screenBounds.getHeight() * 0.9);

		windowState.setXPos((screenBounds.getWidth() - windowState.getWidth()) / 2);
		windowState.setYPos((screenBounds.getHeight() - windowState.getHeight()) / 2);

		windowState.setMaximized(false);
		System.out.println(" -- Window State : " + windowState);
		return windowState;
	}

	public static void setStageDefaultDimensions(final Stage stage) {
		final WindowState defaultWindowState = Global.getDefaultWindowState();
		stage.setWidth(defaultWindowState.getWidth());
		stage.setHeight(defaultWindowState.getHeight());
		stage.setX(defaultWindowState.getXPos());
		stage.setY(defaultWindowState.getYPos());
	}

}
