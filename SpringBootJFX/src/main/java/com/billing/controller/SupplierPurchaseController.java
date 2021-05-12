package com.billing.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.PurchaseEntry;
import com.billing.dto.PurchaseEntrySearchCriteria;
import com.billing.dto.Supplier;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.PurchaseEntryService;
import com.billing.service.SupplierService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.AutoCompleteTextField;
import com.billing.utils.IndianCurrencyFormatting;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

@Controller
public class SupplierPurchaseController extends AppContext implements TabContent {

	private static final Logger logger = LoggerFactory.getLogger(SupplierPurchaseController.class);

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	SupplierService supplierService;

	@Autowired
	PurchaseEntryService purchaseEntryService;

	@Autowired
	AppUtils appUtils;
	
	private int suppId;

	private SortedSet<String> entries;

	private HashMap<String, Supplier> supplierMap;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	@FXML
	private TextField txtCity;

	@FXML
	private TextField txtEmail;

	@FXML
	private TextField txtSupplier;

	@FXML
	private TextField txtMobileNo;

	@FXML
	private TextField txtPendingAmt;

	@FXML
	private AutoCompleteTextField txtSuppliers;

	@FXML
	private TableView<PurchaseEntry> tableView;

	@FXML
	private TableColumn<PurchaseEntry, String> tcInvoiceNo;

	@FXML
	private TableColumn<PurchaseEntry, String> tcInvoiceDate;

	@FXML
	private TableColumn<PurchaseEntry, String> tcNoOfItems;

	@FXML
	private TableColumn<PurchaseEntry, String> tcQuantity;

	@FXML
	private TableColumn<PurchaseEntry, String> tcInvoiceAmount;

	@Override
	public void initialize() {
		setTableCellFactories();
		getSupplierNameList();
		txtSuppliers.createTextField(entries, () -> setSupplierDetails());
		tableView.setOnMouseClicked((MouseEvent event) -> {
			if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
				// Show View Invoice Popup
				if (null != tableView.getSelectionModel().getSelectedItem()) {
					getViewPurEntryPopup(tableView.getSelectionModel().getSelectedItem());
				}
			}
		});
	}

	private void getViewPurEntryPopup(PurchaseEntry purchaseEntry) {
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setControllerFactory(springContext::getBean);
		fxmlLoader.setLocation(this.getClass().getResource("/com/billing/gui/ViewPurchaseEntry.fxml"));

		Parent rootPane = null;
		try {
			rootPane = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("getViewPurEntryPopup Error in loading the view file :", e);
			alertHelper.beep();

			alertHelper.showErrorAlert(currentStage, "Error Occurred", "Error in creating user interface",
					"An error occurred in creating user interface " + "for the selected command");

			return;
		}

		final Scene scene = new Scene(rootPane);
		final ViewPurchaseEntryController controller = (ViewPurchaseEntryController) fxmlLoader.getController();
		controller.purchaseEntry = purchaseEntry;
		final Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(currentStage);
		stage.setUserData(controller);
		stage.getIcons().add(new Image("/images/shop32X32.png"));
		stage.setScene(scene);
		stage.setTitle("View Purchase Entry");
		controller.loadData();
		stage.showAndWait();
	}
	
	private void setTableCellFactories() {
		// Table Column Mapping
		tcInvoiceNo.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getPurchaseEntryNo())));
		tcInvoiceDate.setCellValueFactory(cellData -> new SimpleStringProperty(
				appUtils.getFormattedDateWithTime(cellData.getValue().getPurchaseEntryDate())));
		tcNoOfItems.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getNoOfItems())));
		tcQuantity.setCellValueFactory(cellData -> new SimpleStringProperty(
				appUtils.getDecimalFormat(cellData.getValue().getTotalQuantity())));
		tcInvoiceAmount.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getTotalAmount())));
		// Set CSS
		tcInvoiceNo.getStyleClass().add("character-cell");
		tcInvoiceNo.getStyleClass().add("tableCellCursor");
		tcInvoiceDate.getStyleClass().add("character-cell");
		tcInvoiceDate.getStyleClass().add("tableCellCursor");
		tcNoOfItems.getStyleClass().add("numeric-cell");
		tcNoOfItems.getStyleClass().add("tableCellCursor");
		tcQuantity.getStyleClass().add("numeric-cell");
		tcQuantity.getStyleClass().add("tableCellCursor");
		tcInvoiceAmount.getStyleClass().add("numeric-cell");
		tcInvoiceAmount.getStyleClass().add("tableCellCursor");
	}

	@FXML
	void onCloseAction(ActionEvent event) {
		closeTab();
	}

	@Override
	public boolean shouldClose() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void putFocusOnNode() {
		txtSuppliers.requestFocus();
	}

	@Override
	public boolean loadData() {
		if (!txtSupplier.getText().equals("")) {
			txtSuppliers.setText("");
			
			PurchaseEntrySearchCriteria criteria = new PurchaseEntrySearchCriteria();
			criteria.setSupplierId(supplierMap.get(txtSupplier.getText()).getSupplierID());
			List<PurchaseEntry> invoiceResults = purchaseEntryService.getSearchedPurchaseEntry(criteria);
			
			ObservableList<PurchaseEntry> tableData = FXCollections.observableArrayList();
			tableData.addAll(invoiceResults);
			tableView.setItems(tableData);
		}

		return true;
	}

	@Override
	public void setMainWindow(Stage stage) {
		currentStage = stage;
	}

	@Override
	public void setTabPane(TabPane pane) {
		this.tabPane = pane;
	}

	@Override
	public void setUserDetails(UserDetails user) {
		userDetails = user;
	}

	@Override
	public boolean saveData() {
		return true;
	}

	@Override
	public void invalidated(Observable observable) {
		isDirty.set(true);
	}

	@Override
	public void closeTab() {
		Tab tab = tabPane.selectionModelProperty().get().selectedItemProperty().get();
		tabPane.getTabs().remove(tab); // close the current tab
	}

	@Override
	public boolean validateInput() {
		boolean valid = true;

		return valid;
	}

	private void setSupplierDetails() {
		String supplierName;
		Supplier supp = null;
		if (!txtSuppliers.getText().isEmpty()) {
			supplierName = txtSuppliers.getText();
			supp = supplierMap.get(supplierName);
		}

		if (supp != null) {
			suppId = supp.getSupplierID();
			txtSupplier.setText(supp.getSupplierName());
			txtCity.setText(supp.getCity());
			txtEmail.setText(supp.getEmailId());
			txtPendingAmt.setText(IndianCurrencyFormatting.applyFormattingWithCurrency(supp.getBalanceAmount()));
			txtMobileNo.setText(String.valueOf(supp.getSupplierMobile()));
		}
		loadData();
	}

	public void getSupplierNameList() {
		entries = new TreeSet<String>();
		supplierMap = new HashMap<String, Supplier>();
		for (Supplier supp : supplierService.getAll()) {
			entries.add(supp.getSupplierName());
			supplierMap.put(supp.getSupplierName(), supp);
		}
	}
}
