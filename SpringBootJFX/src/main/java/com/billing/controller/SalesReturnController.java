package com.billing.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.BillDetails;
import com.billing.dto.Customer;
import com.billing.dto.GSTDetails;
import com.billing.dto.ItemDetails;
import com.billing.dto.Product;
import com.billing.dto.ReturnDetails;
import com.billing.dto.StatusDTO;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.CustomerService;
import com.billing.service.InvoiceService;
import com.billing.service.PrinterService;
import com.billing.service.ProductService;
import com.billing.service.SalesReturnService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.IndianCurrencyFormatting;
import com.billing.utils.TabContent;
import com.billing.utils.Task;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

@Controller
public class SalesReturnController extends AppContext implements TabContent {

	private static final Logger logger = LoggerFactory.getLogger(SalesReturnController.class);

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	BillDetails bill;

	Task task;

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	SalesReturnService salesReturnService;

	@Autowired
	CustomerService customerService;

	@Autowired
	AppUtils appUtils;

	@Autowired
	ProductService productService;

	@Autowired
	InvoiceService invoiceService;

	@Autowired
	PrinterService printerService;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	private HashMap<String, Product> productMap;

	private HashMap<Integer, Double> itemQtyMap;

	ObservableList<Product> productTableData;

	boolean tableLoaded = false;

	private boolean isGSTInclusive = false;

	@FXML
	private DatePicker dpReturnDate;

	@FXML
	private TextField txtReturnNumber;

	@FXML
	private TextField txtInvoiceNumber;

	@FXML
	private Label lblReturnDateErrMsg;

	@FXML
	private TextField txtCustomer;

	@FXML
	private TextField txtInvoiceDate;

	@FXML
	private TextField txtItemName;

	@FXML
	private TextField txtComments;

	@FXML
	private TextField txtUnit;

	@FXML
	private TextField txtRate;

	@FXML
	private TextField txtQuantity;

	@FXML
	private Label lblQuantityErrMsg;

	@FXML
	private TextField txtAmount;

	@FXML
	private Label lblNoItemError;

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
	private TableColumn<Product, String> tcDiscount;

	@FXML
	private TableColumn<Product, String> tcDiscountAmount;

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
	private TextField txtPaymentMode;

	@FXML
	private TextField txtNoOfItems;

	@FXML
	private TextField txtTotalQty;

	@FXML
	private TextField txtSubTotal;

	@FXML
	private TextField txtDiscountPercent;

	@FXML
	private TextField txtDiscountAmt;

	@FXML
	private TextField txtReturnTotalAmount;

	@FXML
	private TextField txtGstAmount;

	@FXML
	private TextField txtGstType;

	@FXML
	private Button btnSave;

	@Override
	public void initialize() {
		tableLoaded = false;
		if ("Y".equalsIgnoreCase(appUtils.getAppDataValues("GST_INCLUSIVE"))) {
			isGSTInclusive = true;
			txtGstType.setText("Inclusive");
		} else {
			txtGstType.setText("Exclusive");
		}
		txtReturnNumber.setText(String.valueOf(salesReturnService.getNewReturnNumber()));
		productTableData = FXCollections.observableArrayList();
		itemQtyMap = new HashMap<Integer, Double>();
		dpReturnDate.setValue(LocalDate.now());
		dpReturnDate.setDayCellFactory(this::getDateCell);
		dpReturnDate.valueProperty().addListener((observable, oldDate, newDate) -> {
			isDirty.set(true);
			LocalDate minDate = appUtils
					.convertToLocalDateViaInstant(appUtils.getDateFromDBTimestamp(bill.getTimestamp()));
			LocalDate today = LocalDate.now();
			if (newDate.isAfter(today)) {
				dpReturnDate.setValue(today);
			}
			if (newDate.isEqual(minDate) || newDate.isBefore(minDate)) {
				dpReturnDate.setValue(minDate);
			}
		});

		// Error Messages
		lblQuantityErrMsg.managedProperty().bind(lblQuantityErrMsg.visibleProperty());
		lblQuantityErrMsg.visibleProperty().bind(lblQuantityErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblReturnDateErrMsg.managedProperty().bind(lblReturnDateErrMsg.visibleProperty());
		lblReturnDateErrMsg.visibleProperty().bind(lblReturnDateErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblNoItemError.managedProperty().bind(lblNoItemError.visibleProperty());
		lblNoItemError.visibleProperty().bind(lblNoItemError.textProperty().length().greaterThanOrEqualTo(1));

		setTableCellFactories();
		getProductNameList();
		// Force Number Listner
		txtQuantity.textProperty().addListener(appUtils.getForceDecimalNumberListner());
		txtRate.textProperty().addListener(appUtils.getForceDecimalNumberListner());
		tableView.setItems(productTableData);
		btnSave.disableProperty().bind(isDirty.not());
		// Register textfield listners

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

		txtQuantity.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				txtAmount.setText("");
				if (!txtQuantity.getText().equals("") && !ke.getCode().equals(KeyCode.PERIOD)
						&& !ke.getCode().equals(KeyCode.DECIMAL)) {
					setTxtAmount();
				}
			}
		});

		txtRate.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				txtAmount.setText("");
				if (!txtRate.getText().equals("") && !ke.getCode().equals(KeyCode.PERIOD)
						&& !ke.getCode().equals(KeyCode.DECIMAL)) {
					setTxtAmount();
				}
			}
		});

		productTableData.addListener(new ListChangeListener<Product>() {

			@Override
			public void onChanged(ListChangeListener.Change<? extends Product> c) {
				isDirty.set(true);
				updateReturnAmount();
				lblNoItemError.setText("");
				if (tableLoaded) {
					bill.setItemsEdited(true);
				}
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

	private void setTxtAmount() {
		if (!txtRate.getText().equals("") && !txtQuantity.getText().equals("")) {
			Double pRate = Double.parseDouble(txtRate.getText());
			Double pQty = Double.parseDouble(txtQuantity.getText());
			Double pAmount = pQty * pRate;
			txtAmount.setText(appUtils.getDecimalFormat(pAmount));
		}
	}

	protected void addRecordToTable(Product product) {
		if (validateReturnItem(product)) {
			// updateDiscountAllPerToProduct(product);
			product.setTableDispAmount(Double.valueOf(txtAmount.getText()));
			product.setTableDispRate(Double.valueOf(txtRate.getText()));
			product.setTableDispQuantity(Double.valueOf(txtQuantity.getText()));
			product.setGstDetails(appUtils.getGSTDetails(product));
			productTableData.add(product);
			resetItemFields();
		}
	}

	private DateCell getDateCell(DatePicker datePicker) {
		// Calculate min date
		LocalDate miDate = appUtils.convertToLocalDateViaInstant(appUtils.getDateFromDBTimestamp(bill.getTimestamp()));
		return appUtils.getDateCell(datePicker, miDate, LocalDate.now());
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
		tcDiscountAmount.setCellValueFactory(cellData -> new SimpleStringProperty(
				appUtils.getDecimalFormat(cellData.getValue().getDiscountAmount())));
		tcCGST.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getCgst())));
		tcSGST.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getSgst())));
		tcCGSTPercent.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getCgstPercent())));
		tcSGSTPercent.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getSgstPercent())));

		tcDiscount.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getDiscount())));

		tcItemName.getStyleClass().add("character-cell");
		tcQuantity.getStyleClass().add("numeric-cell");
		tcUnit.getStyleClass().add("character-cell");
		tcRate.getStyleClass().add("numeric-cell");
		tcAmount.getStyleClass().add("numeric-cell");
		tcDiscountAmount.getStyleClass().add("numeric-cell");
		tcCGST.getStyleClass().add("numeric-cell");
		tcDiscount.getStyleClass().add("numeric-cell");
		tcCGSTPercent.getStyleClass().add("numeric-cell");
		tcSGSTPercent.getStyleClass().add("numeric-cell");
		tcSGST.getStyleClass().add("numeric-cell");

	}

	public void getProductNameList() {
		productMap = new HashMap<String, Product>();
		for (Product p : productService.getAll()) {
			productMap.put(p.getProductName(), p);
		}
	}

	private void setProductDetails(double existingQty) {
		if (!txtItemName.getText().equals("")) {
			Product product = productMap.get(txtItemName.getText());
			txtQuantity.setText("");
			if (product != null) {
				txtUnit.setText(product.getMeasure());
				txtRate.setText(appUtils.getDecimalFormat(product.getSellPrice()));
				txtQuantity.setText(appUtils.getDecimalFormat(existingQty));
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
	void onCashHelpCommand(ActionEvent event) {
		if (!txtReturnTotalAmount.getText().equals("")) {
			getCashHelpPopup();
		}
	}

	@FXML
	void onGSTDetailsCommand(ActionEvent event) {
		getGSTDetailsPopUp();
	}

	@FXML
	void onUpdateCommand(ActionEvent event) {
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
		dpReturnDate.requestFocus();
	}

	@Override
	public boolean loadData() {
		List<ItemDetails> itemList = invoiceService.getItemList(bill);
		bill.setItemDetails(itemList);
		bill.setCopyItemDetails(itemList);
		bill.setCopyNetSalesAmt(bill.getNetSalesAmt());
		bill.setCopyCustMobile(bill.getCustomerMobileNo());
		bill.setCopyPaymode(bill.getPaymentMode());
		for (ItemDetails item : bill.getItemDetails()) {
			itemQtyMap.put(item.getItemNo(), item.getQuantity());
			Product p = appUtils.mapItemToProduct(item);
			p.setPurcasePrice(productMap.get(item.getItemName()).getPurcasePrice());
			productTableData.add(p);
		}
		txtCustomer.setText(bill.getCustomerMobileNo() + " : " + bill.getCustomerName());
		txtInvoiceDate.setText(appUtils.getFormattedDateWithTime(bill.getTimestamp()));
		txtInvoiceNumber.setText(String.valueOf(bill.getBillNumber()));
		txtPaymentMode.setText(bill.getPaymentMode());
		txtDiscountPercent.setText(String.valueOf(bill.getDiscount()));
		isDirty.set(false);
		tableLoaded = true;
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
		ReturnDetails returnDetails = prepareReturnDetails();
		StatusDTO status = salesReturnService.saveReturn(returnDetails);
		if (status.getStatusCode() != 0) {
			saveStatus = false;
		}
		if (saveStatus) {
			alertHelper.showInfoAlert(currentStage, "Sales Return", "Return Saved", "Return saved successfully");
			task.doTask();
			closeTab();
		} else {
			alertHelper.showErrorNotification("Error occured while saving return");
		}
		return saveStatus;
	}

	private ReturnDetails prepareReturnDetails() {
		ReturnDetails returnDetails = new ReturnDetails();
		returnDetails.setReturnNumber(Integer.valueOf(txtReturnNumber.getText()));
		returnDetails.setInvoiceNumber(bill.getBillNumber());
		returnDetails.setInvoiceDate(bill.getTimestamp());
		returnDetails.setCustomerMobileNo(bill.getCustomerMobileNo());
		returnDetails.setCustomerName(bill.getCustomerName());
		returnDetails.setComments(txtComments.getText());
		// Prepare Item List
		returnDetails.setItemDetails(prepareItemList());
		returnDetails.setTotalReturnAmount(
				Double.valueOf(IndianCurrencyFormatting.removeFormattingWithCurrency(txtReturnTotalAmount.getText())));
		returnDetails.setNoOfItems(Integer.valueOf(txtNoOfItems.getText()));
		returnDetails.setTotalQuantity(Double.valueOf(txtTotalQty.getText()));
		returnDetails.setDiscount(Double.valueOf(txtDiscountPercent.getText()));
		returnDetails
				.setDiscountAmount(Double.valueOf(IndianCurrencyFormatting.removeFormatting(txtDiscountAmt.getText())));
		returnDetails.setPaymentMode(txtPaymentMode.getText());
		returnDetails.setSubTotal(Double.valueOf(IndianCurrencyFormatting.removeFormatting(txtSubTotal.getText())));
		returnDetails.setInvoiceNetSalesAmt(bill.getNetSalesAmt());
		// New Invoice Amount
		double newInvoiceNetSalesAmt = bill.getNetSalesAmt() - returnDetails.getTotalReturnAmount();
		returnDetails.setNewInvoiceNetSalesAmt(Double.valueOf(newInvoiceNetSalesAmt));

		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd");
		String returnDate = dpReturnDate.getValue().format(dateFormatter);
		String returnTime = appUtils.getCurrentTime();

		returnDetails.setTimestamp(returnDate + " " + returnTime);
		returnDetails.setReturnPurchaseAmt(getBillPurchaseAmount());
		returnDetails.setGstType(txtGstType.getText());
		returnDetails.setGstAmount(Double.valueOf(IndianCurrencyFormatting.removeFormatting(txtGstAmount.getText())));
		returnDetails.setCreatedBy(userDetails.getFirstName() + " " + userDetails.getLastName());
		return returnDetails;
	}

	private double getBillPurchaseAmount() {
		double billPurchaseAmount = 0.0;
		for (Product p : productTableData) {
			billPurchaseAmount = billPurchaseAmount + (p.getPurcasePrice() * p.getTableDispQuantity());
		}
		return billPurchaseAmount;
	}

	private List<ItemDetails> prepareItemList() {
		List<ItemDetails> itemList = new ArrayList<>();
		for (Product p : productTableData) {
			ItemDetails item = new ItemDetails();
			item.setBillNumber(Integer.valueOf(txtReturnNumber.getText()));
			item.setGstDetails(p.getGstDetails());
			item.setItemName(p.getProductName());
			item.setItemNo(p.getProductCode());
			item.setRate(p.getTableDispRate());
			item.setQuantity(p.getTableDispQuantity());
			item.setMRP(p.getSellPrice());
			item.setPurchasePrice(p.getPurcasePrice());
			item.setDiscountPercent(p.getDiscount());
			item.setDiscountAmount(p.getDiscountAmount());
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
		currentStage.close();
	}

	@Override
	public boolean validateInput() {
		boolean valid = true;
		clearReturnErrorFields();

		final LocalDate date = dpReturnDate.getValue();
		if (date == null) {
			lblReturnDateErrMsg.setText("Return Date not specified!");
			valid = false;
			return valid;
		} else if (date.isAfter(LocalDate.now())) {
			lblReturnDateErrMsg.setText("Return Date can't be later than todays date :" + appUtils.getTodaysDate());
			valid = false;
			return valid;
		}
		if (productTableData.size() == 0) {
			lblNoItemError.setText("Please add product to return");
			txtItemName.requestFocus();
			valid = false;
			return valid;
		} else {
			lblNoItemError.setText("");
		}

		if ("PENDING".equals(bill.getPaymentMode())) {
			Customer cust = customerService.getCustomerDetails(bill.getCustomerMobileNo());
			double returnTotalAmt = Double
					.valueOf(IndianCurrencyFormatting.removeFormattingWithCurrency(txtReturnTotalAmount.getText()));
			if (cust.getBalanceAmt() < returnTotalAmt) {
				alertHelper.showErrorNotification(
						"Customer balance is less than return total amount. Please check customer payment history");
				valid = false;
				return valid;
			}
		}
		return valid;
	}

	private void resetItemFields() {
		txtItemName.clear();
		txtAmount.clear();
		txtRate.clear();
		txtQuantity.clear();
		txtUnit.clear();
	}

	private void clearReturnErrorFields() {
		lblReturnDateErrMsg.setText("");
		lblNoItemError.setText("");
	}

	private boolean validateReturnItem(Product product) {
		clearItemErrorFields();
		boolean valid = true;
		String quantity = txtQuantity.getText().trim();
		if (quantity.isEmpty()) {
			lblQuantityErrMsg.setText("Quantity not specified");
			alertHelper.beep();
			valid = false;
		} else if (0 == Double.valueOf(quantity)) {
			lblQuantityErrMsg.setText("Invalid Quantity");
			alertHelper.beep();
			valid = false;
		} else if (Double.valueOf(quantity) > itemQtyMap.get(Integer.valueOf(product.getProductCode()))) {
			valid = false;
			lblQuantityErrMsg.setText("Quantity should be equal to or less than purchased quantity");
			alertHelper.beep();
		} else if (null != product && (product.getQuantity() < Double.valueOf(quantity))) {
			valid = false;
			lblQuantityErrMsg.setText("Available stock is : " + appUtils.getDecimalFormat(product.getQuantity()));
			alertHelper.beep();
		}
		return valid;
	}

	private void clearItemErrorFields() {
		lblQuantityErrMsg.setText("");
	}

	private void getCashHelpPopup() {

		Dialog<String> dialog = new Dialog<>();
		dialog.setTitle("Cash Help");

		final String styleSheetPath = "/css/alertDialog.css";
		final DialogPane dialogPane = dialog.getDialogPane();
		dialogPane.getStylesheets().add(AlertHelper.class.getResource(styleSheetPath).toExternalForm());

		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(this.getClass().getResource("/images/shop32X32.png").toString()));

		// Set the button types.
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 100, 10, 10));

		Label lbl = new Label("Net Sales Amount :");
		lbl.getStyleClass().add("nodeLabel");
		TextField txtNetTotal = new TextField();
		txtNetTotal.setPrefColumnCount(15);
		txtNetTotal.getStyleClass().add("readOnlyField");
		txtNetTotal.setEditable(false);
		txtNetTotal.setText(txtReturnTotalAmount.getText());
		grid.add(lbl, 0, 0);
		grid.add(txtNetTotal, 1, 0);

		Label lblCashAmt = new Label("Cash Amount :");
		lblCashAmt.getStyleClass().add("nodeLabel");
		TextField txtCashAmt = new TextField();
		txtCashAmt.setPrefColumnCount(15);
		txtCashAmt.textProperty().addListener(appUtils.getForceDecimalNumberListner());
		grid.add(lblCashAmt, 0, 1);
		grid.add(txtCashAmt, 1, 1);

		Label lblRAmt = new Label("Return Amount :");
		lblRAmt.getStyleClass().add("nodeLabel");
		TextField txtReturnAmt = new TextField();
		txtReturnAmt.setPrefColumnCount(15);
		txtReturnAmt.getStyleClass().add("readOnlyField");
		txtReturnAmt.getStyleClass().add("summation");
		txtReturnAmt.setEditable(false);
		grid.add(lblRAmt, 0, 2);
		grid.add(txtReturnAmt, 1, 2);
		GridPane.setHalignment(lbl, HPos.RIGHT);
		GridPane.setHalignment(lblCashAmt, HPos.RIGHT);
		GridPane.setHalignment(lblRAmt, HPos.RIGHT);

		dialog.getDialogPane().setContent(grid);

		Platform.runLater(() -> txtCashAmt.requestFocus());

		txtCashAmt.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				txtReturnAmt.setText("");
				if (!txtCashAmt.getText().equals("") && !ke.getCode().equals(KeyCode.PERIOD)
						&& !ke.getCode().equals(KeyCode.DECIMAL)) {
					double netTotal = Double.valueOf(
							IndianCurrencyFormatting.removeFormattingWithCurrency(txtReturnTotalAmount.getText()));
					double cashAmt = Double.valueOf(txtCashAmt.getText());
					txtReturnAmt.setText(appUtils.getDecimalFormat(cashAmt - netTotal));
				}
			}
		});

		dialog.showAndWait();
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
					setProductDetails(p.getTableDispQuantity());
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

	private void updateReturnAmount() {
		double subTotal = 0.0;
		double quantity = 0.0;
		int noOfItems = 0;
		double gstAmount = 0.0;
		double discountAmount = 0.0;
		double totalReturnAmount = 0.0;
		noOfItems = productTableData.size();
		for (Product product : productTableData) {
			GSTDetails gst = product.getGstDetails();
			subTotal = subTotal + (product.getTableDispRate() * product.getTableDispQuantity());
			quantity = quantity + product.getTableDispQuantity();
			gstAmount = gstAmount + gst.getGstAmount();
			discountAmount = discountAmount + product.getDiscountAmount();
		}
		if (isGSTInclusive) {
			subTotal = subTotal - gstAmount;
		}

		totalReturnAmount = (subTotal - discountAmount) + gstAmount;
		txtNoOfItems.setText(String.valueOf(noOfItems));
		txtTotalQty.setText(appUtils.getDecimalFormat(quantity));
		txtSubTotal.setText(IndianCurrencyFormatting.applyFormatting(subTotal));
		txtDiscountAmt.setText(IndianCurrencyFormatting.applyFormatting(discountAmount));
		txtGstAmount.setText(IndianCurrencyFormatting.applyFormatting(gstAmount));
		txtReturnTotalAmount.setText(
				IndianCurrencyFormatting.applyFormattingWithCurrency(appUtils.getDecimalRoundUp(totalReturnAmount)));
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

	public void setTask(Task t) {
		this.task = t;
	}
}
