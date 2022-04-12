package com.billing.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.TempleDetails;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.main.Global;
import com.billing.service.DBBackupService;
import com.billing.service.DBScheduledDumpTask;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.TabContent;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

@SuppressWarnings("restriction")
@Controller
public class HomeController extends AppContext {

	@Autowired
	AppUtils appUtils;

	@Autowired
	AlertHelper alertHelper;

	TempleDetails templeDetails;

	@Autowired
	DBBackupService dbBackupService;

	@Autowired
	DBScheduledDumpTask dbScheduledDumpTask;

	public Stage currentStage = null;

	public UserDetails userDetails = null;

	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

	@FXML
	private Label lblSupportEmail;

	@FXML
	private Label lblSupportMobile;

	@FXML
	private MenuBar menuBar;

	@FXML
	private MenuItem customersMenuItem;

	// Help Menu
	@FXML
	private ToolBar toolBar;

	@FXML
	private TabPane tabPane;

	@FXML
	private BorderPane rootPane;

	@FXML
	private Label lblLicenseValidUpto;

	public void initialize() {
		tabPane.getSelectionModel().selectedItemProperty()
				.addListener((ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) -> {
					if (newValue != null) {
						Platform.runLater(() -> {
							Object object = newValue.getProperties().get("controller");
							if (object != null) {
								((TabContent) object).putFocusOnNode();
							}
						});
					}
				});

		toolBar.managedProperty().bind(toolBar.visibleProperty());
		// Listner to load dashboard if no tab in tabpane

		tabPane.getTabs().addListener(new ListChangeListener<Tab>() {

			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends Tab> c) {
				if (tabPane.getTabs().size() == 0 && userDetails.getUserType().equals("INTERNAL")) {
					loadDashboard();
				}
			}

		});

		try {
			lblLicenseValidUpto
					.setText("License Valid Upto : " + appUtils.dec(appUtils.getAppDataValues("APP_SECURE_KEY")));
			lblSupportEmail.setText("Copyright © " + appUtils.getAppDataValues("CUSTOMER_SUPPORT_EMAIL"));
			lblSupportMobile.setText("Contact Us : " + appUtils.getAppDataValues("CUSTOMER_SUPPORT_MOBILE"));
		} catch (Exception e) {
			logger.error("lblLicenseValidUpto -->" + e);
		}

		// Start Scheduled DB Dump task
		startScheduledDBDumpTask();
	}

	public void loadData() {
		if (userDetails.getUserType().equals("EXTERNAL")) {
			String allowedMenus = appUtils.getAppDataValues("EXTERNAL_USER_MENUS");
			logger.info("--- Allowed Menus for External User ---> " + allowedMenus);
			toolBar.getItems().removeIf(item -> (item.getId() != null && !allowedMenus.contains(item.getId())));
			menuBar.getMenus().stream().forEach(menu -> {
				updateMenuForExternalUser(menu, allowedMenus);
			});
			// Default tab Donation
			addTab("Donation", "देणगी पावती");
		}
		appUtils.licenseExpiryAlert();
		if (null == templeDetails) {
			// Show Temple Details tab
			addTab("TempleDetails", "मंदिराची माहिती");
		} else if (userDetails.getUserType().equals("INTERNAL")) {
			// Load Dashboard
			loadDashboard();
		}
	}

	private void loadDashboard() {
		URL resource = this.getClass().getResource("/com/billing/gui/Dashboard.fxml");
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setControllerFactory(springContext::getBean);
		fxmlLoader.setLocation(resource);
		Parent pane = null;
		try {
			pane = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
			alertHelper.beep();
			alertHelper.showErrorAlert(currentStage, "Error Occurred", "Error in creating user interface",
					"An error occurred in creating user interface " + "for the selected command");

			return;
		}

		final DashboardController controller = (DashboardController) fxmlLoader.getController();
		controller.currentStage = currentStage;
		controller.userDetails = userDetails;
		controller.storeDetails = templeDetails;
		controller.initialize();
		rootPane.setCenter(pane);
	}

	private void updateMenuForExternalUser(Menu menu, String allowedMenus) {
		menu.getItems().removeIf(item -> (item.getId() != null && !allowedMenus.contains(item.getId())));
		if (menu.getItems().size() == 0) {
			menu.setVisible(false);
		}
	}

	private void startScheduledDBDumpTask() {
		Timer time = new Timer();
		Integer dbDumpInterval = Integer.parseInt(appUtils.getAppDataValues("DB_DUMP_INTERVAL").split("")[0]);
		logger.info("---- DB Dump Scheduled with Interval of :: " + dbDumpInterval + " Hour ---");
		dbDumpInterval = dbDumpInterval * 60 * 60 * 1000; // Convert Hours to Milliseconds
		time.schedule(dbScheduledDumpTask, 0, dbDumpInterval);
	}

	@FXML
	void onDataBackupCommand(ActionEvent event) {
		dbBackupService.saveDBDumpToChoosenLocation(currentStage);
	}

	@FXML
	void onRestoreDatabaseCommand(ActionEvent event) {
		dbBackupService.restoreDatabase(currentStage);
	}
	
	@FXML
	void onUserPrefCommand(ActionEvent event) {
		addTab("UserPreferences", "वापरकर्ता सेटिंग्ज");
	}

	@FXML
	private void onExitCommand(ActionEvent event) {
		currentStage.fireEvent(new WindowEvent(currentStage, WindowEvent.WINDOW_CLOSE_REQUEST));
	}

	@FXML
	void onManageAccountCommand(ActionEvent event) {
		addTab("ManageAccount", "तुमची प्रोफाईल");
	}

	@FXML
	void onIncomeTypeCommand(ActionEvent event) {
		addTab("IncomeType", "उत्पन्नाचा प्रकार");
	}

	@FXML
	void onExpenseTypeCommand(ActionEvent event) {
		addTab("ExpenseType", "खर्चाचा प्रकार");
	}

	@FXML
	void onTempleDetailsCommand(ActionEvent event) {
		addTab("TempleDetails", "मंदिराची माहिती");
	}

	@FXML
	void onDonationCommand(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
			addTab("Donation", "देणगी पावती");
		}
	}

	@FXML
	void onAbhishekCommand(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
			addTab("Abhishek", "अभिषेक पावती");
		}
	}

	@FXML
	void onTodaysReportCommand(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
			addTab("TodaysReport", "आजचा अहवाल");
		}
	}

	@FXML
	void onCustomersCommand(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
			addTab("Customers", "देणगीदार");
		}
	}

	@FXML
	void onAddIncomeCommand(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
			addTab("Income", "जमा");
		}
	}

	@FXML
	void onAddExpenseCommand(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
			addTab("Expense", "खर्च");
		}
	}

	@FXML
	void onSearchTxnCommand(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
			addTab("SearchTransaction", "शोधा");
		}
	}

	@FXML
	void onProfitLossReportCommand(ActionEvent event) {
		addTab("ProfitLossReport", "आर्थिक वर्षाचा अहवाल");
	}

	private void addTab(final String fxmlFileName, final String title) {

		final String KEY = "fxml";

		final String viewPath = "/com/billing/gui/" + fxmlFileName + ".fxml";

		URL resource = this.getClass().getResource(viewPath);
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setControllerFactory(springContext::getBean);
		fxmlLoader.setLocation(resource);
		Parent pane = null;
		try {
			pane = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("HomeController addTab Error in loading the view file :" + fxmlFileName, e);
			alertHelper.beep();

			alertHelper.showErrorAlert(currentStage, "Error Occurred", "Error in creating user interface",
					"An error occurred in creating user interface " + "for the selected command");

			return;
		}

		final TabContent controller = (TabContent) fxmlLoader.getController();
		controller.setMainWindow(currentStage);
		controller.setTabPane(tabPane);
		controller.setUserDetails(userDetails);

		if (!controller.loadData()) {
			return;
		}

		Tab tab = new Tab();
		tab.getProperties().put("controller", controller);
		tab.getProperties().put(KEY, fxmlFileName);
		tab.setContent(pane);
		tab.setText(title);
		setContextMenu(tab);

		tab.setOnCloseRequest((Event event1) -> {
			if (!controller.shouldClose()) {
				event1.consume();
			}
		});
		if (tabPane.getTabs().size() != 0) {
			TabContent controllerPrev = (TabContent) tabPane.getTabs().get(0).getProperties().get("controller");
			if (controllerPrev.shouldClose()) {
				tabPane.getTabs().clear();
			} else {
				return;
			}
		}

		if (tabPane.getTabs().size() == 0) {
			rootPane.setCenter(tabPane);
		}
		tabPane.getTabs().add(tab);
		tabPane.getSelectionModel().select(tab);
		controller.putFocusOnNode();
	}

	private void setContextMenu(final Tab tab) {

		final MenuItem closeTabItem = new MenuItem("Close Tab");
		final MenuItem closeOtherTabsItem = new MenuItem("Close Other Tabs");
		final MenuItem closeAllTabsItem = new MenuItem("Close All Tabs");

		final ContextMenu contextMenu = new ContextMenu(closeTabItem, closeOtherTabsItem, closeAllTabsItem);

		setCloseTabAction(tab, closeTabItem);
		setCloseOtherTabsAction(tab, closeOtherTabsItem);
		setCloseAllTabsAction(tab, closeAllTabsItem);

		tab.setContextMenu(contextMenu);
	}

	private void setCloseAllTabsAction(final Tab tab, final MenuItem menuItem) {
		final EventHandler<ActionEvent> eventHandler = (ActionEvent event) -> {
			closeAllTabs();
		};

		menuItem.setOnAction(eventHandler);
	}

	private void setCloseOtherTabsAction(final Tab tab, final MenuItem menuItem) {
		final EventHandler<ActionEvent> eventHandler = (ActionEvent event) -> {
			final TabPane tabPane = tab.getTabPane();
			Global.closeTabs(tabPane, tab);
		};

		menuItem.setOnAction(eventHandler);
	}

	private void setCloseTabAction(final Tab tab, final MenuItem menuItem) {

		final EventHandler<ActionEvent> eventHandler = (ActionEvent event) -> {
			final TabPane tabPane = tab.getTabPane();
			tabPane.getSelectionModel().select(tab);
			TabContent controller = (TabContent) tab.getProperties().get("controller");
			if (controller.shouldClose()) {
				tabPane.getTabs().remove(tab);
			}
		};

		menuItem.setOnAction(eventHandler);
	}

	public boolean closeAllTabs() {
		final ObservableList<Tab> tabs = tabPane.getTabs();
		final List<Tab> tabsToRemove = new ArrayList<>(tabs.size());

		for (Tab tabControl : tabs) {
			tabPane.getSelectionModel().select(tabControl);
			TabContent controller = (TabContent) tabControl.getProperties().get("controller");
			if (!controller.shouldClose()) {
				return false;
			} else {
				tabsToRemove.add(tabControl); // mark this tab to be removed
			}
		}

		tabs.removeAll(tabsToRemove); // actually remove the tabs here
		return true;
	}

}
