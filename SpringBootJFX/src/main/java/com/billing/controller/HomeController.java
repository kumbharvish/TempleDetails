/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.main.Global;
import com.billing.service.DBBackupService;
import com.billing.service.DBScheduledDumpTask;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.TabContent;
import com.billing.utils.Utility;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Vishal
 */
@SuppressWarnings("restriction")
@Controller
public class HomeController extends AppContext {

	@Autowired
	AppUtils appUtils;

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	DBBackupService dbBackupService;

	@Autowired
	DBScheduledDumpTask dbScheduledDumpTask;

	public Stage currentStage = null;

	public UserDetails userDetails = null;

	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

	private final static String INVOICE_VIEW_FILE_NAME = "Invoice";

	@FXML
	private MenuItem manageAccountMenuItem;

	@FXML
	private MenuItem storeDetailsMenuItem;

	@FXML
	private MenuItem measurementUnitsMenuItem;

	@FXML
	private MenuItem productCategoryMenuItem;

	@FXML
	private MenuItem productsMenuItem;

	@FXML
	private MenuItem expenseMenuItem;

	@FXML
	private MenuItem exitMenuItem;

	@FXML
	private MenuItem createInvoiceMenuItem;

	@FXML
	private MenuItem searchInvoiceMenuItem;

	@FXML
	private MenuItem returnInvoiceMenuItem;

	@FXML
	private MenuItem customersMenuItem;

	@FXML
	private MenuItem customerPaymentHistoryMenuItem;

	@FXML
	private MenuItem customerPurchaseHistoryMenuItem;

	@FXML
	private MenuItem generateBarcodeMenuItem;

	@FXML
	private MenuItem printBarcodeMenuItem;

	@FXML
	private MenuItem suppliersMenuItem;

	@FXML
	private MenuItem stockEntryMenuItem;

	@FXML
	private MenuItem stockHistoryMenuItem;

	@FXML
	private MenuItem salesStockValueReportMenuItem;

	@FXML
	private MenuItem productProfitReportMenuItem;

	@FXML
	private MenuItem customersReportMenuItem;

	@FXML
	private MenuItem zeroStockProductsReportMenuItem;

	@FXML
	private MenuItem productCategoryWiseStockReprotMenuItem;

	@FXML
	private MenuItem salesReportMenuItem;

	@FXML
	private MenuItem salesReturnReportMenuItem;

	@FXML
	private MenuItem monthlyReportMenuItem;

	@FXML
	private MenuItem profitLossReportMenuItem;

	@FXML
	private MenuItem cashCounterReportMenuItem;
	
	@FXML
	private MenuItem expenseReportMenuItem;

	@FXML
	private MenuItem paymentModeWiseSalesMenuItem;

	@FXML
	private MenuItem dailySalesMenuItem;

	@FXML
	private MenuItem monthlySalesMenuItem;

	@FXML
	private MenuItem productWiseProfitMenuItem;

	@FXML
	private MenuItem billWiseProfitMenuItem;

	@FXML
	private MenuItem stockEntryWiseProfitReportMenuItem;

	@FXML
	private MenuItem customerWiseProfitReportMenuItem;

	@FXML
	private MenuItem productWiseSalesReportMenuItem;

	@FXML
	private MenuItem userPreferencesMenuItem;

	/*
	 * @FXML private MenuItem databackupMailSettingsMenuItem;
	 */

	@FXML
	private MenuItem dataBackupMenuItem;

	@FXML
	private CheckMenuItem hideToolbarMenuItem;

	// Help Menu

	@FXML
	private MenuItem aboutUsMenuItem;

	@FXML
	private ToolBar toolBar;

	@FXML
	private Label lblCreateInvoice;

	@FXML
	private Label lblSearchInvoice;

	@FXML
	private Label lblProducts;

	@FXML
	private Label lblMeasurementUnits;

	@FXML
	private Label lblProductCategories;

	@FXML
	private Label lblSaleReport;

	@FXML
	private Label lblCashCounter;

	@FXML
	private Label lblDataBackup;

	@FXML
	private Label lblUserSettings;

	@FXML
	private TabPane tabPane;

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
		appUtils.licenseExpiryAlert();
		try {
			lblLicenseValidUpto.setText(
					"License Valid Upto : " + AppUtils.dec(appUtils.getAppDataValues("APP_SECURE_KEY").get(0)));
		} catch (Exception e) {
			logger.error("lblLicenseValidUpto -->" + e);
		}

		// Start Scheduled DB Dump task
		startScheduledDBDumpTask();
	}

	private void startScheduledDBDumpTask() {
		Timer time = new Timer();
		Integer dbDumpInterval = Integer.parseInt(appUtils.getAppDataValues("DB_DUMP_INTERVAL").get(0));
		logger.info("---- DB Dump Scheduled with Interval of :: " + dbDumpInterval + " Mins ---");
		dbDumpInterval = dbDumpInterval * 60 * 1000; // Convert Minutes to Milliseconds
		time.schedule(dbScheduledDumpTask, 0, dbDumpInterval);
	}

	@FXML
	void onAboutUsCommand(ActionEvent event) {
		addTab("AboutUs", "About Us");
	}

	@FXML
	void onBillWiseProfitCommand(ActionEvent event) {

	}

	@FXML
	void onCashCounterClick(MouseEvent event) {

	}

	@FXML
	void onCashCounterCommand(ActionEvent event) {

	}
	
	@FXML
	void onExpenseReportCommand(ActionEvent event) {
		
	}

	@FXML
	void onCreateInvoiceClick(MouseEvent event) {

	}

	@FXML
	void onCreateInvoiceCommand(ActionEvent event) {

	}

	@FXML
	void onCustomerCommand(ActionEvent event) {

	}

	@FXML
	void onCustomerPaymentHistoryCommand(ActionEvent event) {

	}

	@FXML
	void onCustomerPurchaseHistoryCommand(ActionEvent event) {

	}

	@FXML
	void onCustomerReportCommand(ActionEvent event) {

	}

	@FXML
	void onCustomerWiseProfitCommand(ActionEvent event) {

	}

	@FXML
	void onDailySalesReportCommand(ActionEvent event) {

	}

	@FXML
	void onDataBackupClick(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
			dataBackupMenuItem.fire();
		}
	}

	/*
	 * @FXML void onDataBackupMailSettingsCommand(ActionEvent event) {
	 * addTab("BackupMailSetting", "Data Backup Mail Settings"); }
	 */

	@FXML
	void onDataBackupCommand(ActionEvent event) {
		dbBackupService.createDBDumpSendOnMail(currentStage);
	}

	@FXML
	private void onExitCommand(ActionEvent event) {
		currentStage.fireEvent(new WindowEvent(currentStage, WindowEvent.WINDOW_CLOSE_REQUEST));
	}

	@FXML
	void onExpenseCommand(ActionEvent event) {
		addTab("Expense", "Expense");
	}

	@FXML
	void onGenerateBarcodeCommand(ActionEvent event) {

	}

	@FXML
	void onHideToolbarCommand(ActionEvent event) {
		CheckMenuItem menuItem = (CheckMenuItem) event.getSource();
		toolBar.setVisible(!menuItem.isSelected());
	}

	@FXML
	void onManageAccountCommand(ActionEvent event) {
		addTab("ManageAccount", "Manage Account");
	}

	@FXML
	void onMeasuermentUnitsClick(MouseEvent event) {

	}

	@FXML
	void onMeasurementUnitsCommand(ActionEvent event) {

	}

	@FXML
	void onMonthlyReportCommand(ActionEvent event) {

	}

	@FXML
	void onMonthlySalesReportCommand(ActionEvent event) {

	}

	@FXML
	void onPaymentModeWiseSalesCommand(ActionEvent event) {

	}

	@FXML
	void onPrintBarcodeCommand(ActionEvent event) {

	}

	@FXML
	void onProductCategoriesClick(MouseEvent event) {
		addTab("ProductCategory", "Product Categories");
	}

	@FXML
	void onProductCategoriesCommand(ActionEvent event) {
		addTab("ProductCategory", "Product Categories");
	}

	@FXML
	void onProductCategoryWiseStockCommand(ActionEvent event) {

	}

	@FXML
	void onProductProfitReportCommand(ActionEvent event) {

	}

	@FXML
	void onProductWiseProfitCommand(ActionEvent event) {

	}

	@FXML
	void onProductWiseSalesCommand(ActionEvent event) {

	}

	@FXML
	void onProductsClick(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
			productsMenuItem.fire();
		}
	}

	@FXML
	void onProductsCommand(ActionEvent event) {
		addTab("Products", "Products");
	}

	@FXML
	void onProfitLossCommand(ActionEvent event) {

	}

	@FXML
	void onReturnInvoiceCommand(ActionEvent event) {

	}

	@FXML
	void onSaleReportClick(MouseEvent event) {

	}

	@FXML
	void onSalesReportCommand(ActionEvent event) {

	}

	@FXML
	void onSalesReturnReportCommand(ActionEvent event) {

	}

	@FXML
	void onSalesStockValueReportCommand(ActionEvent event) {

	}

	@FXML
	void onSearchInvoiceClick(MouseEvent event) {

	}

	@FXML
	void onSearchInvoiceCommand(ActionEvent event) {

	}

	@FXML
	void onStockEntryCommand(ActionEvent event) {

	}

	@FXML
	void onStockEntryWiseProfitCommand(ActionEvent event) {

	}

	@FXML
	void onStockHistoryCommand(ActionEvent event) {

	}

	@FXML
	void onStoreDetailsCommand(ActionEvent event) {
		addTab("StoreDetails", "Store Details");
	}

	@FXML
	void onSuppliersCommand(ActionEvent event) {

	}

	@FXML
	void onUserPreferencesCommand(ActionEvent event) {
		addTab("UserPreferences", "User Preferences");
	}

	@FXML
	void onUserSettingsClick(MouseEvent event) {

	}

	@FXML
	void onZeroStockProductsCommand(ActionEvent event) {

	}

	private void addTab(final String fxmlFileName, final String title) {

		final String KEY = "fxml";

		/*
		 * Ensure that no second instance of a view other than that of Invoice view is
		 * instantiated
		 */
		if (!fxmlFileName.equalsIgnoreCase(INVOICE_VIEW_FILE_NAME)) { // view other than Invoice view
			ObservableList<Tab> tabs = tabPane.getTabs();
			for (Tab tabInstance : tabs) {
				if (tabInstance.getProperties().get(KEY).toString().equalsIgnoreCase(fxmlFileName)) { // view already
																										// instantiated
					tabPane.getSelectionModel().select(tabInstance);
					return;
				}
			}
		}

		final String viewPath = "/com/billing/gui/" + fxmlFileName + ".fxml";

		URL resource = this.getClass().getResource(viewPath);
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setControllerFactory(springContext::getBean);
		fxmlLoader.setLocation(resource);
		Parent rootPane = null;
		try {
			rootPane = fxmlLoader.load();
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
		tab.setContent(rootPane);
		tab.setText(title);
		setContextMenu(tab);

		tab.setOnCloseRequest((Event event1) -> {
			if (!controller.shouldClose()) {
				event1.consume();
			}
		});

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

		tabs.removeAll(tabsToRemove); // actually remove the tags here
		return true;
	}

}
