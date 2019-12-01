package com.billing.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.GSTDetails;
import com.billing.dto.ItemDetails;
import com.billing.dto.Product;
import com.billing.dto.PurchaseEntry;
import com.billing.dto.StatusDTO;
import com.billing.dto.Supplier;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.ProductService;
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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

@Controller
public class PurchaseEntryController extends AppContext implements TabContent {

	private static final Logger logger = LoggerFactory.getLogger(PurchaseEntryController.class);

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	@Autowired
	ProductService productService;

	@Autowired
	SupplierService supplierService;

	@Autowired
	PurchaseEntryService purchaseEntryService;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	private SortedSet<String> supplierEntries;

	private HashMap<String, Integer> suppliersIdMap;

	private SortedSet<String> productEntries;

	private HashMap<String, Product> productMap;

	ObservableList<Product> productTableData;

	@FXML
	private TextField txtPurchaseEntryNo;

	@FXML
	private TextField txtEntryDate;

	@FXML
	private TextField txtComments;

	@FXML
	private AutoCompleteTextField txtSuppliers;

	@FXML
	private Label lblSuppliersErrMsg;

	@FXML
	private TextField txtBillNo;

	@FXML
	private Label lblBillNoErrMsg;

	@FXML
	private DatePicker dpBillDate;

	@FXML
	private AutoCompleteTextField txtItemName;

	@FXML
	private Label lblItemNameErrMsg;

	@FXML
	private TextField txtUnit;

	@FXML
	private TextField txtProductTax;

	@FXML
	private Label lblProductTaxErrMsg;

	@FXML
	private TextField txtRate;

	@FXML
	private Label lblRateErrMsg;

	@FXML
	private Label lblBillDateErrMsg;

	@FXML
	private Label lblNoItemError;

	@FXML
	private TextField txtQuantity;

	@FXML
	private Label lblQuantityErrMsg;

	@FXML
	private TableView<Product> tableView;

	@FXML
	private TableColumn<Product, String> tcItemName;

	@FXML
	private TableColumn<Product, String> tcUnit;

	@FXML
	private TableColumn<Product, String> tcQuantity;

	@FXML
	private TableColumn<Product, String> tcRate;

	@FXML
	private TableColumn<Product, String> tcAmount;

	@FXML
	private TableColumn<Product, String> tcCGST;

	@FXML
	private TableColumn<Product, String> tcSGST;

	@FXML
	private TableColumn<Product, String> tcSGSTPercent;

	@FXML
	private TableColumn<Product, String> tcCGSTPercent;

	@FXML
	private ComboBox<String> cbPaymentModes;

	@FXML
	private TextField txtNoOfItems;

	@FXML
	private TextField txtTotalQty;

	@FXML
	private TextField txtTotalBefTax;

	@FXML
	private TextField txtExtraCharges;

	@FXML
	private TextField txtTotalAmount;

	@FXML
	private TextField txtTotalGSTAmt;

	@FXML
	private Button btnSave;

	@Override
	public void initialize() {

		productTableData = FXCollections.observableArrayList();
		dpBillDate.setValue(LocalDate.now());
		dpBillDate.setDayCellFactory(this::getDateCell);
		dpBillDate.valueProperty().addListener((observable, oldDate, newDate) -> {
			isDirty.set(true);
			LocalDate today = LocalDate.now();
			if (newDate == null || newDate.isAfter(today)) {
				dpBillDate.setValue(today);
				isDirty.set(true);
			}
		});
		txtEntryDate.setText(appUtils.getTodaysDate());
		// Error Messages
		lblItemNameErrMsg.managedProperty().bind(lblItemNameErrMsg.visibleProperty());
		lblItemNameErrMsg.visibleProperty().bind(lblItemNameErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblQuantityErrMsg.managedProperty().bind(lblQuantityErrMsg.visibleProperty());
		lblQuantityErrMsg.visibleProperty().bind(lblQuantityErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblSuppliersErrMsg.managedProperty().bind(lblSuppliersErrMsg.visibleProperty());
		lblSuppliersErrMsg.visibleProperty().bind(lblSuppliersErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblRateErrMsg.managedProperty().bind(lblRateErrMsg.visibleProperty());
		lblRateErrMsg.visibleProperty().bind(lblRateErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblBillDateErrMsg.managedProperty().bind(lblBillDateErrMsg.visibleProperty());
		lblBillDateErrMsg.visibleProperty().bind(lblBillDateErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblBillNoErrMsg.managedProperty().bind(lblBillNoErrMsg.visibleProperty());
		lblBillNoErrMsg.visibleProperty().bind(lblBillNoErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblNoItemError.managedProperty().bind(lblNoItemError.visibleProperty());
		lblNoItemError.visibleProperty().bind(lblNoItemError.textProperty().length().greaterThanOrEqualTo(1));
		lblProductTaxErrMsg.managedProperty().bind(lblProductTaxErrMsg.visibleProperty());
		lblProductTaxErrMsg.visibleProperty().bind(lblProductTaxErrMsg.textProperty().length().greaterThanOrEqualTo(1));

		populatePaymentModes();
		setTableCellFactories();
		getSupplierNameList();
		getProductNameList();
		txtSuppliers.createTextField(supplierEntries, () -> {
			supplierTxtFieldTask();
		});
		txtItemName.createTextField(productEntries, () -> setProductDetails());
		txtPurchaseEntryNo.setText(String.valueOf(purchaseEntryService.getNewPurchaseEntryNumber()));
		// Force Number Listner
		txtQuantity.textProperty().addListener(appUtils.getForceDecimalNumberListner());
		txtRate.textProperty().addListener(appUtils.getForceDecimalNumberListner());
		tableView.setItems(productTableData);
		btnSave.disableProperty().bind(isDirty.not());
		// Register textfield listners

		cbPaymentModes.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					cbPaymentModes.show();
				}
			}
		});

		cbPaymentModes.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				isDirty.set(true);
			}
		});

		txtQuantity.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					if (!txtItemName.getText().equals("")) {
						addRecordToTable(productMap.get(txtItemName.getText()));
					}
				}
			}
		});

		productTableData.addListener(new ListChangeListener<Product>() {

			@Override
			public void onChanged(ListChangeListener.Change<? extends Product> c) {
				isDirty.set(true);
				updateTotalAmount();
				lblNoItemError.setText("");
			}
		});

		tableView.setOnMouseClicked((MouseEvent event) -> {
			if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
				// Show pop up with Edit and Remove Row option
				if (!productTableData.isEmpty() && null != tableView.getSelectionModel().getSelectedItem()) {
					getTableRowEditDeletePopup();
				}

			}
		});

	}

	private void supplierTxtFieldTask() {
		lblSuppliersErrMsg.setText("");
		isDirty.set(true);
		txtItemName.requestFocus();
	}

	protected void addRecordToTable(Product product) {
		if (validatePurchaseEntryItem(product)) {
			// product.setTableDispAmount(Double.valueOf(txtAmount.getText()));
			product.setTableDispRate(Double.valueOf(txtRate.getText()));
			product.setTableDispQuantity(Double.valueOf(txtQuantity.getText()));
			product.setGstDetails(appUtils.getGSTDetails(product));
			productTableData.add(product);
			resetItemFields();
			txtItemName.requestFocus();
		}
	}

	private DateCell getDateCell(DatePicker datePicker) {
		return appUtils.getDateCell(datePicker, null, LocalDate.now());
	}

	private void setTableCellFactories() {
		tcItemName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductName()));
		tcUnit.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMeasure()));
		tcQuantity.setCellValueFactory(cellData -> new SimpleStringProperty(
				appUtils.getDecimalFormat(cellData.getValue().getTableDispQuantity())));
		tcRate.setCellValueFactory(cellData -> new SimpleStringProperty(
				appUtils.getDecimalFormat(cellData.getValue().getTableDispRate())));
		tcAmount.setCellValueFactory(cellData -> new SimpleStringProperty(
				appUtils.getDecimalFormat(cellData.getValue().getTableAmountShowValue())));
		tcCGST.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getCgst())));
		tcSGST.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getSgst())));
		tcCGSTPercent.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getCgstPercent())));
		tcSGSTPercent.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getSgstPercent())));

		tcItemName.getStyleClass().add("character-cell");
		tcQuantity.getStyleClass().add("numeric-cell");
		tcUnit.getStyleClass().add("character-cell");
		tcRate.getStyleClass().add("numeric-cell");
		tcAmount.getStyleClass().add("numeric-cell");
		tcCGST.getStyleClass().add("numeric-cell");
		tcCGSTPercent.getStyleClass().add("numeric-cell");
		tcSGSTPercent.getStyleClass().add("numeric-cell");
		tcSGST.getStyleClass().add("numeric-cell");

	}

	public void populatePaymentModes() {
		for (String mode : appUtils.getPaymentModes()) {
			cbPaymentModes.getItems().add(mode);
		}
		cbPaymentModes.getSelectionModel().select(0);
	}

	public void getSupplierNameList() {
		supplierEntries = new TreeSet<String>();
		suppliersIdMap = new HashMap<String, Integer>();
		for (Supplier s : supplierService.getAll()) {
			supplierEntries.add(s.getSupplierName());
			suppliersIdMap.put(s.getSupplierName(), s.getSupplierID());
		}
	}

	public void getProductNameList() {
		productEntries = new TreeSet<String>();
		productMap = new HashMap<String, Product>();
		for (Product p : productService.getAll()) {
			productEntries.add(p.getProductName());
			productMap.put(p.getProductName(), p);
		}
	}

	private void setProductDetails() {
		if (!txtItemName.getText().equals("")) {
			Product product = productMap.get(txtItemName.getText());
			txtQuantity.setText("");
			if (product != null) {
				txtUnit.setText(product.getMeasure());
				txtRate.setText(appUtils.getDecimalFormat(product.getSellPrice()));
				txtQuantity.requestFocus();
				clearItemErrorFields();
			}
		}
	}

	@FXML
	void onCloseAction(ActionEvent event) {
		if (shouldClose()) {
			closeTab();
		}
	}

	@FXML
	void onRefereshCommand(ActionEvent event) {
		getProductNameList();
		getSupplierNameList();
		txtSuppliers.createTextField(supplierEntries, () -> supplierTxtFieldTask());
		txtItemName.createTextField(productEntries, () -> setProductDetails());
	}

	@FXML
	void onGSTDetailsCommand(ActionEvent event) {
		getGSTDetailsPopUp();
	}

	@FXML
	void onSaveCommand(ActionEvent event) {
		if (!saveData()) {
			return;
		}
	}

	@Override
	public boolean shouldClose() {
		if (isDirty.get()) {
			ButtonType response = appUtils.shouldSaveUnsavedData(currentStage);
			if (response == ButtonType.CANCEL) {
				return false;
			}

			if (response == ButtonType.YES) {
				return saveData();
			}
		}
		return true;
	}

	@Override
	public void putFocusOnNode() {
		txtBillNo.requestFocus();
	}

	@Override
	public boolean loadData() {
		isDirty.set(false);
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
		Boolean saveStatus = true;
		if (!validateInput()) {
			alertHelper.beep();
			return false;
		}
		PurchaseEntry pe = preparePurchaseEntryDetails();
		StatusDTO status = purchaseEntryService.add(pe);
		if (status.getStatusCode() != 0) {
			saveStatus = false;
		}
		if (saveStatus) {
			alertHelper.showSuccessNotification("Purchase entry saved successfully");
			// Reset Purchase Entry UI Fields
			resetFields();
		} else {
			alertHelper.showErrorNotification("Error occured while saving purchase entry");
		}
		return saveStatus;
	}

	private PurchaseEntry preparePurchaseEntryDetails() {
		PurchaseEntry pe = new PurchaseEntry();
		pe.setPurchaseEntryNo(Integer.valueOf(txtPurchaseEntryNo.getText()));
		pe.setSupplierId(suppliersIdMap.get(txtSuppliers.getText()));
		pe.setSupplierName(txtSuppliers.getText());
		pe.setComments(txtComments.getText());
		// Prepare Item List
		pe.setItemDetails(prepareItemList());
		pe.setTotalAmtBeforeTax(Double.valueOf(IndianCurrencyFormatting.removeFormatting(txtTotalBefTax.getText())));
		pe.setNoOfItems(Integer.valueOf(txtNoOfItems.getText()));
		pe.setTotalQuantity(Double.valueOf(txtTotalQty.getText()));
		pe.setPaymentMode(cbPaymentModes.getSelectionModel().getSelectedItem());
		pe.setTotalAmount(
				Double.valueOf(IndianCurrencyFormatting.removeFormattingWithCurrency(txtTotalAmount.getText())));

		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd");
		String billDate = dpBillDate.getValue().format(dateFormatter);
		String billTime = appUtils.getCurrentTime();

		pe.setBillDate(billDate + " " + billTime);
		pe.setTotalGSTAmount(Double.valueOf(IndianCurrencyFormatting.removeFormatting(txtTotalGSTAmt.getText())));
		pe.setCreatedBy(userDetails.getFirstName() + " " + userDetails.getLastName());
		return pe;
	}

	private List<ItemDetails> prepareItemList() {
		List<ItemDetails> itemList = new ArrayList<>();
		for (Product p : productTableData) {
			ItemDetails item = new ItemDetails();
			item.setPurchaseEntryNo(Integer.valueOf(txtPurchaseEntryNo.getText()));
			item.setGstDetails(p.getGstDetails());
			item.setItemName(p.getProductName());
			item.setItemNo(p.getProductCode());
			item.setRate(p.getTableDispRate());
			item.setQuantity(p.getTableDispQuantity());
			item.setMRP(p.getSellPrice());
			item.setPurchasePrice(p.getPurcasePrice());
			item.setUnit(p.getMeasure());
			item.setHsn(p.getHsn());
			itemList.add(item);
		}
		return itemList;
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
		clearPurchaseEntryErrorFields();

		final LocalDate date = dpBillDate.getValue();
		if (date == null) {
			lblBillDateErrMsg.setText("Bill Date not specified!");
			valid = false;
			return valid;
		} else if (date.isAfter(LocalDate.now())) {
			lblBillDateErrMsg.setText("Bill Date can't be later than todays date :" + appUtils.getTodaysDate());
			valid = false;
			return valid;
		}
		int billNo = txtBillNo.getText().trim().length();
		if (billNo == 0) {
			lblBillNoErrMsg.setText("Please enter bill no");
			txtBillNo.requestFocus();
			valid = false;
			return valid;
		} else {
			lblBillNoErrMsg.setText("");
		}

		int supplier = txtSuppliers.getText().trim().length();
		if (supplier == 0) {
			lblSuppliersErrMsg.setText("Please select supplier");
			txtSuppliers.requestFocus();
			valid = false;
			return valid;
		} else if (null == suppliersIdMap.get(txtSuppliers.getText())) {
			lblSuppliersErrMsg.setText("No supplier matches this name");
			txtSuppliers.requestFocus();
			valid = false;
			return valid;
		} else {
			lblSuppliersErrMsg.setText("");
		}

		if (productTableData.size() == 0) {
			lblNoItemError.setText("Please add product to purchase entry");
			txtItemName.requestFocus();
			valid = false;
			return valid;
		} else {
			lblNoItemError.setText("");
		}

		return valid;
	}

	private void resetFields() {
		resetItemFields();
		resetOrignalProductDiscount();
		productTableData.clear();
		dpBillDate.setValue(LocalDate.now());
		txtComments.clear();
		txtBillNo.clear();
		txtPurchaseEntryNo.setText(String.valueOf(purchaseEntryService.getNewPurchaseEntryNumber()));
		txtSuppliers.clear();
		txtSuppliers.requestFocus();
		txtNoOfItems.clear();
		txtTotalQty.clear();
		txtTotalBefTax.clear();
		cbPaymentModes.getSelectionModel().select(0);
		txtTotalGSTAmt.clear();
		txtTotalAmount.clear();
		isDirty.set(false);
		getProductNameList();
		txtItemName.createTextField(productEntries, () -> setProductDetails());

	}

	private void resetOrignalProductDiscount() {
		for (Product p : productTableData) {
			p.setDiscount(p.getOrignalDiscount());
		}
	}

	private void resetItemFields() {
		txtItemName.clear();
		txtProductTax.clear();
		txtRate.clear();
		txtQuantity.clear();
		txtUnit.clear();
	}

	private void clearPurchaseEntryErrorFields() {
		lblBillDateErrMsg.setText("");
		lblSuppliersErrMsg.setText("");
		lblNoItemError.setText("");
		lblBillNoErrMsg.setText("");
	}

	private boolean validatePurchaseEntryItem(Product product) {
		clearItemErrorFields();
		boolean valid = true;
		if (txtItemName.getText().equals("")) {
			lblItemNameErrMsg.setText("Enter product name");
			valid = false;
		}
		if (null == product && !txtItemName.getText().equals("")) {
			lblItemNameErrMsg.setText("Invalid product name");
			valid = false;
		}
		String rate = txtRate.getText().trim();
		if (rate.isEmpty()) {
			lblRateErrMsg.setText("Rate not specified");
			alertHelper.beep();
			valid = false;
		}
		String quantity = txtQuantity.getText().trim();
		if (quantity.isEmpty()) {
			lblQuantityErrMsg.setText("Quantity not specified");
			alertHelper.beep();
			valid = false;
		} else if (0 == Double.valueOf(quantity)) {
			lblQuantityErrMsg.setText("Invalid Quantity");
			alertHelper.beep();
			valid = false;
		} else if (null != product && (product.getQuantity() < Double.valueOf(quantity))) {
			valid = false;
			lblQuantityErrMsg.setText("Available stock is : " + appUtils.getDecimalFormat(product.getQuantity()));
			alertHelper.beep();
		}
		boolean isMatch = productTableData.stream().anyMatch(i -> i.getProductName().equals(product.getProductName()));
		if (isMatch) {
			lblItemNameErrMsg.setText("Prodcut already added");
			valid = false;
		}

		return valid;
	}

	private void clearItemErrorFields() {
		lblItemNameErrMsg.setText("");
		lblQuantityErrMsg.setText("");
		lblRateErrMsg.setText("");
	}

	private void getTableRowEditDeletePopup() {

		Dialog<String> dialog = new Dialog<>();
		dialog.setTitle("Action");

		final String styleSheetPath = "/css/alertDialog.css";
		final DialogPane dialogPane = dialog.getDialogPane();
		dialogPane.getStylesheets().add(AlertHelper.class.getResource(styleSheetPath).toExternalForm());

		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(this.getClass().getResource("/images/shop32X32.png").toString()));
		HBox box = new HBox();
		Label lbl = new Label("Please choose action on product");
		lbl.getStyleClass().add("nodeLabel");
		box.getChildren().add(lbl);
		box.setPrefHeight(55.0);
		dialog.getDialogPane().setContent(box);
		// Set the button types.
		ButtonType updateButtonType = new ButtonType("Edit", ButtonData.OK_DONE);
		ButtonType deleteButtonType = new ButtonType("Remove", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, deleteButtonType, ButtonType.CANCEL);
		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == updateButtonType) {
				Product p = tableView.getSelectionModel().getSelectedItem();
				if (productTableData.contains(p)) {
					productTableData.remove(p);
					txtItemName.setText(p.getProductName());
					setProductDetails();
				}
			} else if (dialogButton == deleteButtonType) {
				Alert alert = alertHelper.showConfirmAlertWithYesNo(currentStage, null, "Are you sure?");
				if (alert.getResult() == ButtonType.YES) {
					Product p = tableView.getSelectionModel().getSelectedItem();
					if (productTableData.contains(p)) {
						productTableData.remove(p);
					}
				}
			}
			return null;
		});
		dialog.showAndWait();
	}

	private void updateTotalAmount() {
		double totalAmtBeforeTax = 0.0;
		double quantity = 0.0;
		int noOfItems = 0;
		double gstAmount = 0.0;
		double totalAmount = 0.0;
		noOfItems = productTableData.size();
		for (Product product : productTableData) {
			GSTDetails gst = product.getGstDetails();
			totalAmtBeforeTax = totalAmtBeforeTax + (product.getTableDispRate() * product.getTableDispQuantity());
			quantity = quantity + product.getTableDispQuantity();
			gstAmount = gstAmount + gst.getGstAmount();
		}
		/*
		 * if (isGSTInclusive) { subTotal = subTotal - gstAmount; }
		 */
		totalAmount = totalAmtBeforeTax + gstAmount;
		txtNoOfItems.setText(String.valueOf(noOfItems));
		txtTotalQty.setText(appUtils.getDecimalFormat(quantity));
		txtTotalBefTax.setText(IndianCurrencyFormatting.applyFormatting(totalAmtBeforeTax));
		txtTotalGSTAmt.setText(IndianCurrencyFormatting.applyFormatting(gstAmount));
		txtTotalAmount
				.setText(IndianCurrencyFormatting.applyFormattingWithCurrency(appUtils.getDecimalRoundUp(totalAmount)));
	}

	private void getGSTDetailsPopUp() {
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setControllerFactory(springContext::getBean);
		fxmlLoader.setLocation(this.getClass().getResource("/com/billing/gui/ViewGSTDetails.fxml"));

		Parent rootPane = null;
		try {
			rootPane = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("getGSTDetailsPopUp Error in loading the view file :", e);
			alertHelper.beep();

			alertHelper.showErrorAlert(currentStage, "Error Occurred", "Error in creating user interface",
					"An error occurred in creating user interface " + "for the selected command");

			return;
		}

		final Scene scene = new Scene(rootPane);
		final GSTDetailsController controller = (GSTDetailsController) fxmlLoader.getController();
		controller.productList = productTableData;
		final Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(currentStage);
		stage.setUserData(controller);
		stage.getIcons().add(new Image("/images/shop32X32.png"));
		stage.setScene(scene);
		stage.setTitle("GST Details");
		controller.loadData();
		stage.showAndWait();
	}

}
