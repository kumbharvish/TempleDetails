package com.billing.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.Customer;
import com.billing.dto.MeasurementUnit;
import com.billing.dto.Product;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.CustomerService;
import com.billing.service.InvoiceService;
import com.billing.service.MeasurementUnitsService;
import com.billing.service.ProductService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.AutoCompleteTextField;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

@Controller
public class CreateInvoiceController extends AppContext implements TabContent {

	private static final Logger logger = LoggerFactory.getLogger(CreateInvoiceController.class);

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	@Autowired
	CustomerService customerService;

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	@Autowired
	MeasurementUnitsService measurementUnitsService;

	@Autowired
	ProductService productService;

	@Autowired
	InvoiceService invoiceService;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	private SortedSet<String> customerEntries;

	private HashMap<Long, Customer> customerMap;

	private SortedSet<String> productEntries;

	private HashMap<String, Product> productMap;

	private HashMap<Long, Product> productMapWithBarcode;

	ObservableList<Product> productTableData;

	@FXML
	private DatePicker dpInvoiceDate;

	@FXML
	private TextField txtInvoiceNumber;

	@FXML
	private Label lblInvoiceDateErrMsg;

	@FXML
	private RadioButton rbBarcode;

	@FXML
	private RadioButton rbItemName;

	@FXML
	private AutoCompleteTextField txtCustomer;

	@FXML
	private AutoCompleteTextField txtItemName;

	@FXML
	private TextField txtItemBarcode;

	@FXML
	private Label lblCustomerErrMsg;

	@FXML
	private Label lblItemNameErrMsg;

	@FXML
	private TextField txtUnit;

	@FXML
	private TextField txtRate;

	@FXML
	private Label lblRateErrMsg;

	@FXML
	private TextField txtQuantity;

	@FXML
	private Label lblQuantityErrMsg;

	@FXML
	private TextField txtAmount;

	@FXML
	private Label lblNoItemError;

	@FXML
	private Button btnRefresh;

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
	private TableColumn<Product, String> tcAmount;

	@FXML
	private TableColumn<Product, String> tcCGST;

	@FXML
	private TableColumn<Product, String> tcSGST;

	@FXML
	private Button btnCashHelp;

	@FXML
	private ComboBox<String> cbPaymentModes;

	@FXML
	private Label lblPayModeErrMSg;

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
	private TextField txtNetSalesAmount;

	@FXML
	private CheckBox cbPrintOnSave;

	@FXML
	private Button btnSave;

	@FXML
	private Button btnClose;

	@Override
	public void initialize() {
		productTableData = FXCollections.observableArrayList();
		ToggleGroup radioButtonGroup = new ToggleGroup();
		rbBarcode.setToggleGroup(radioButtonGroup);
		rbItemName.setToggleGroup(radioButtonGroup);
		if ("BARCODE".equals(appUtils.getAppDataValues("INVOICE_PRODUCT_SEARCH_BY"))) {
			rbBarcode.setSelected(true);
		} else {
			rbItemName.setSelected(true);
		}
		dpInvoiceDate.setValue(LocalDate.now());
		dpInvoiceDate.setDayCellFactory(this::getDateCell);
		dpInvoiceDate.valueProperty().addListener((observable, oldDate, newDate) -> {
			isDirty.set(true);
			LocalDate today = LocalDate.now();
			if (newDate == null || newDate.isAfter(today)) {
				dpInvoiceDate.setValue(today);
			}
		});
		txtItemName.managedProperty().bind(txtItemName.visibleProperty());
		txtItemName.visibleProperty().bind(rbItemName.selectedProperty());
		txtItemBarcode.managedProperty().bind(txtItemBarcode.visibleProperty());
		txtItemBarcode.visibleProperty().bind(rbBarcode.selectedProperty());

		// Error Messages
		lblItemNameErrMsg.managedProperty().bind(lblItemNameErrMsg.visibleProperty());
		lblItemNameErrMsg.visibleProperty().bind(lblItemNameErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblQuantityErrMsg.managedProperty().bind(lblQuantityErrMsg.visibleProperty());
		lblQuantityErrMsg.visibleProperty().bind(lblQuantityErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblCustomerErrMsg.managedProperty().bind(lblCustomerErrMsg.visibleProperty());
		lblCustomerErrMsg.visibleProperty().bind(lblCustomerErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblRateErrMsg.managedProperty().bind(lblRateErrMsg.visibleProperty());
		lblRateErrMsg.visibleProperty().bind(lblRateErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblInvoiceDateErrMsg.managedProperty().bind(lblInvoiceDateErrMsg.visibleProperty());
		lblInvoiceDateErrMsg.visibleProperty()
				.bind(lblInvoiceDateErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblNoItemError.managedProperty().bind(lblNoItemError.visibleProperty());
		lblNoItemError.visibleProperty().bind(lblNoItemError.textProperty().length().greaterThanOrEqualTo(1));

		populatePaymentModes();
		setTableCellFactories();
		getCustomerNameList();
		getProductNameList();
		txtCustomer.createTextField(customerEntries, () -> setNewFoucus());
		txtItemName.createTextField(productEntries, () -> setProductDetails());
		txtDiscountPercent.setText("0.00");
		txtInvoiceNumber.setText(String.valueOf(invoiceService.getNewBillNumber()));
		// Force Number Listner
		txtQuantity.textProperty().addListener(appUtils.getForceDecimalNumberListner());
		txtRate.textProperty().addListener(appUtils.getForceDecimalNumberListner());
		txtDiscountPercent.textProperty().addListener(appUtils.getForceDecimalNumberListner());
		tableView.setItems(productTableData);
		// Register textfield listners

		cbPaymentModes.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					cbPaymentModes.show();
				}
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

		txtItemBarcode.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					if (!txtItemBarcode.getText().equals("")) {
						clearItemErrorFields();
						setProductDetailsWithBarCode(Long.valueOf(txtItemBarcode.getText().trim()));
						txtItemBarcode.setText("");
						setNewFoucus();
					}
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

		rbBarcode.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				txtItemBarcode.requestFocus();
			}
		});
		rbItemName.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				txtItemName.requestFocus();
			}
		});
	}

	private void setTxtAmount() {
		if (!txtRate.getText().equals("") && !txtQuantity.getText().equals("")) {
			Double pRate = Double.parseDouble(txtRate.getText());
			Double pQty = Double.parseDouble(txtQuantity.getText());
			Double pAmount = pQty * pRate;
			txtAmount.setText(appUtils.getDecimalFormat(pAmount));
			lblQuantityErrMsg.setText("");
			lblRateErrMsg.setText("");
		}
	}

	protected void addRecordToTable(Product product) {
		if (validateInvoiceItem(product)) {
			product.setTableDispAmount(Double.valueOf(txtAmount.getText()));
			product.setTableDispRate(Double.valueOf(txtRate.getText()));
			product.setTableDispQuantity(Double.valueOf(txtQuantity.getText()));
			productTableData.add(product);
			resetItemFields();
			setNewFoucus();
		}
	}

	// Set Product details for BarCode
	protected void setProductDetailsWithBarCode(Long productBarCode) {
		Product product = productMapWithBarcode.get(productBarCode);
		if (product == null) {
			lblItemNameErrMsg.setText("Product not preset for Barcode : " + productBarCode);
		} else {
			product.setTableDispQuantity(1.0);
			product.setTableDispAmount(product.getSellPrice());
			product.setTableDispRate(product.getSellPrice());
			if (product.getQuantity() >= product.getTableDispQuantity()) {
				productTableData.add(product);
			} else {
				lblQuantityErrMsg.setText("Available stock is : " + appUtils.getDecimalFormat(product.getQuantity()));
			}
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
				appUtils.getDecimalFormat(cellData.getValue().getTableDispAmount())));
		tcCGST.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getCgst())));
		tcSGST.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getSgst())));
		tcDiscount.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getDiscount())));

		tcItemName.getStyleClass().add("character-cell");
		tcQuantity.getStyleClass().add("numeric-cell");
		tcUnit.getStyleClass().add("character-cell");
		tcRate.getStyleClass().add("numeric-cell");
		tcAmount.getStyleClass().add("numeric-cell");
		tcCGST.getStyleClass().add("numeric-cell");
		tcDiscount.getStyleClass().add("numeric-cell");
		tcSGST.getStyleClass().add("numeric-cell");

	}

	public void populatePaymentModes() {
		for (String mode : appUtils.getPaymentModes()) {
			cbPaymentModes.getItems().add(mode);
		}
		cbPaymentModes.getSelectionModel().select(0);
	}

	public void getCustomerNameList() {
		customerEntries = new TreeSet<String>();
		customerMap = new HashMap<Long, Customer>();
		for (Customer cust : customerService.getAllCustomers()) {
			customerEntries.add(cust.getCustMobileNumber() + " : " + cust.getCustName());
			customerMap.put(cust.getCustMobileNumber(), cust);
		}
	}

	public void getProductNameList() {
		productEntries = new TreeSet<String>();
		productMap = new HashMap<String, Product>();
		productMapWithBarcode = new HashMap<Long, Product>();
		for (Product p : productService.getAllProducts()) {
			productEntries.add(p.getProductName());
			productMap.put(p.getProductName(), p);
			productMapWithBarcode.put(p.getProductBarCode(), p);
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
		closeTab();
	}

	@FXML
	void onRefereshCommand(ActionEvent event) {
		getProductNameList();
		getCustomerNameList();
		txtCustomer.createTextField(customerEntries, () -> setNewFoucus());
		txtItemName.createTextField(productEntries, () -> setProductDetails());
	}

	@FXML
	void onCashHelpCommand(ActionEvent event) {

	}

	@FXML
	void onSaveCommand(ActionEvent event) {

	}

	@Override
	public boolean shouldClose() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void putFocusOnNode() {
		txtCustomer.requestFocus();
	}

	@Override
	public boolean loadData() {
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

	private void resetFields() {
		resetItemFields();
	}

	private void resetItemFields() {
		txtItemName.setText("");
		txtItemBarcode.setText("");
		txtAmount.setText("");
		txtRate.setText("");
		txtQuantity.setText("");
		txtUnit.setText("");
	}

	protected void setNewFoucus() {
		if (rbBarcode.isSelected()) {
			txtItemBarcode.requestFocus();
		} else {
			txtItemName.requestFocus();
		}
	}

	private boolean validateInvoiceItem(Product product) {
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
			valid = false;
		}
		String quantity = txtQuantity.getText().trim();
		if (quantity.isEmpty()) {
			lblQuantityErrMsg.setText("Quantity not specified");
			valid = false;
		} else {
			if (null != product && (Double.valueOf(quantity) >= product.getQuantity())) {
				valid = false;
				lblQuantityErrMsg.setText("Available stock is : " + appUtils.getDecimalFormat(product.getQuantity()));
			}
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

}
