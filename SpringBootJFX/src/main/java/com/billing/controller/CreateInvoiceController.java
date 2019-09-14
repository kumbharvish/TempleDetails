package com.billing.controller;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.CustomerService;
import com.billing.service.MeasurementUnitsService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
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
import javafx.stage.Stage;

@Controller
public class CreateInvoiceController extends AppContext implements TabContent {

	private static final Logger logger = LoggerFactory.getLogger(CreateInvoiceController.class);

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	@Autowired
	CustomerService userService;

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	@Autowired
	MeasurementUnitsService measurementUnitsService;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	@FXML
	private DatePicker dpInvoiceDate;

	@FXML
	private Label lblInvoiceDateErrMsg;

	@FXML
	private RadioButton rbBarcode;

	@FXML
	private RadioButton rbItemName;

	@FXML
	private TextField txtCustomer;

	@FXML
	private Label lblCustomerErrMsg;

	@FXML
	private TextField txtItemName;

	@FXML
	private Label lblItemNameErrMsg;

	@FXML
	private ComboBox<?> cbUnit;

	@FXML
	private Label lblUnitErrMsg;

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
	private TableView<?> tableView;

	@FXML
	private TableColumn<?, ?> tcItemName;

	@FXML
	private TableColumn<?, ?> tcUnit;

	@FXML
	private TableColumn<?, ?> tcQuantity;

	@FXML
	private TableColumn<?, ?> tcRate;

	@FXML
	private TableColumn<?, ?> tcDiscount;

	@FXML
	private TableColumn<?, ?> tcAmount;

	@FXML
	private TableColumn<?, ?> tcCGST;

	@FXML
	private TableColumn<?, ?> tcSGST;

	@FXML
	private Button btnCashHelp;

	@FXML
	private ComboBox<?> cbPaymentModes;

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
		ToggleGroup radioButtonGroup = new ToggleGroup();
		rbBarcode.setToggleGroup(radioButtonGroup);
		rbItemName.setToggleGroup(radioButtonGroup);
		rbBarcode.setSelected(true);
		dpInvoiceDate.setValue(LocalDate.now());
		dpInvoiceDate.setDayCellFactory(this::getDateCell);
		dpInvoiceDate.valueProperty().addListener((observable, oldDate, newDate) -> {
			isDirty.set(true);
			LocalDate today = LocalDate.now();
			if (newDate == null || newDate.isAfter(today)) {
				dpInvoiceDate.setValue(today);
			}
		});

		// Error Messages
		lblItemNameErrMsg.managedProperty().bind(lblItemNameErrMsg.visibleProperty());
		lblItemNameErrMsg.visibleProperty().bind(lblItemNameErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblQuantityErrMsg.managedProperty().bind(lblQuantityErrMsg.visibleProperty());
		lblQuantityErrMsg.visibleProperty().bind(lblQuantityErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblCustomerErrMsg.managedProperty().bind(lblCustomerErrMsg.visibleProperty());
		lblCustomerErrMsg.visibleProperty().bind(lblCustomerErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblUnitErrMsg.managedProperty().bind(lblUnitErrMsg.visibleProperty());
		lblUnitErrMsg.visibleProperty().bind(lblUnitErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblRateErrMsg.managedProperty().bind(lblRateErrMsg.visibleProperty());
		lblRateErrMsg.visibleProperty().bind(lblRateErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblInvoiceDateErrMsg.managedProperty().bind(lblInvoiceDateErrMsg.visibleProperty());
		lblInvoiceDateErrMsg.visibleProperty()
				.bind(lblInvoiceDateErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblNoItemError.managedProperty().bind(lblNoItemError.visibleProperty());
		lblNoItemError.visibleProperty().bind(lblNoItemError.textProperty().length().greaterThanOrEqualTo(1));

		// lblPayModeErrMSg.managedProperty().bind(lblPayModeErrMSg.visibleProperty());
		// lblPayModeErrMSg.visibleProperty().bind(lblPayModeErrMSg.textProperty().length().greaterThanOrEqualTo(1));

		setTableCellFactories();
		// Force Number Listner
		txtQuantity.textProperty().addListener(appUtils.getForceDecimalNumberListner());
		txtRate.textProperty().addListener(appUtils.getForceDecimalNumberListner());
		txtDiscountPercent.textProperty().addListener(appUtils.getForceDecimalNumberListner());
		// Table row selection
		/*
		 * tableView.getSelectionModel().selectedItemProperty().addListener(this::
		 * onSelectedRowChanged);
		 * cbProductCategory.prefWidthProperty().bind(cbMeasuringUnit.widthProperty());
		 * cbTax.prefWidthProperty().bind(cbMeasuringUnit.widthProperty());
		 * populateMUnitComboBox();
		 */
		// Register textfield listners

	}

	private DateCell getDateCell(DatePicker datePicker) {
		return appUtils.getDateCell(datePicker, null, LocalDate.now());
	}

	private void setTableCellFactories() {

		/*
		 * // Table Column Mapping tcCategory.setCellValueFactory(cellData -> new
		 * SimpleStringProperty(cellData.getValue().getProductCategory()));
		 * tcName.setCellValueFactory(cellData -> new
		 * SimpleStringProperty(cellData.getValue().getProductName()));
		 * tcQuantity.setCellValueFactory( cellData -> new
		 * SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().
		 * getQuantity()))); tcMUnit.setCellValueFactory(cellData -> new
		 * SimpleStringProperty(cellData.getValue().getMeasure()));
		 * tcPurchaseRate.setCellValueFactory( cellData -> new
		 * SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().
		 * getPurcaseRate()))); tcTax.setCellValueFactory( cellData -> new
		 * SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().
		 * getProductTax()))); tcSellPrice.setCellValueFactory( cellData -> new
		 * SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().
		 * getSellPrice()))); tcDiscount.setCellValueFactory( cellData -> new
		 * SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().
		 * getDiscount()))); tcDescription.setCellValueFactory(cellData -> new
		 * SimpleStringProperty(cellData.getValue().getDescription())); // Set CSS
		 * tcQuantity.getStyleClass().add("numeric-cell");
		 * tcPurchaseRate.getStyleClass().add("numeric-cell");
		 * tcTax.getStyleClass().add("numeric-cell");
		 * tcSellPrice.getStyleClass().add("numeric-cell");
		 * tcDiscount.getStyleClass().add("numeric-cell");
		 * tcCategory.getStyleClass().add("character-cell");
		 * tcName.getStyleClass().add("character-cell");
		 * tcMUnit.getStyleClass().add("character-cell");
		 * tcDescription.getStyleClass().add("character-cell");
		 */
	}

	/*
	 * public void populateMUnitComboBox() {
	 * cbMeasuringUnit.getItems().add("-- Select Measurement Unit --"); for
	 * (MeasurementUnit u : measurementUnitsService.getAllUOM()) {
	 * cbMeasuringUnit.getItems().add(u.getName()); }
	 * cbMeasuringUnit.getSelectionModel().select(0); }
	 */

	@FXML
	void onCloseAction(ActionEvent event) {
		closeTab();
	}

	@FXML
	void onRefereshCommand(ActionEvent event) {

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
		dpInvoiceDate.requestFocus();
	}

	@Override
	public boolean loadData() {
		/*
		 * List<Product> list = productService.getAllProducts(); ObservableList<Product>
		 * productTableData = FXCollections.observableArrayList();
		 * productTableData.addAll(list); filteredList = new
		 * FilteredList(productTableData, null); tableView.setItems(filteredList);
		 */
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
		/*
		 * // Category int category =
		 * cbProductCategory.getSelectionModel().getSelectedIndex(); if (category == 0)
		 * { alertHelper.beep();
		 * lblProductCategoryErrMsg.setText("Please select category");
		 * cbProductCategory.requestFocus(); valid = false; } else {
		 * lblProductCategoryErrMsg.setText(""); } // Product Name int name =
		 * txtProductName.getText().trim().length(); if (name == 0) {
		 * alertHelper.beep();
		 * lblProductNameErrMsg.setText("Please enter product name");
		 * txtProductName.requestFocus(); valid = false; } else {
		 * lblProductNameErrMsg.setText(""); }
		 * 
		 * // Measurement Unit int mUnit =
		 * cbMeasuringUnit.getSelectionModel().getSelectedIndex(); if (mUnit == 0) {
		 * alertHelper.beep(); lblUnitErrMsg.setText("Please select measurement unit");
		 * cbMeasuringUnit.requestFocus(); valid = false; } else {
		 * lblUnitErrMsg.setText(""); }
		 * 
		 * // Quantity int quantity = txtQuantity.getText().trim().length(); if
		 * (quantity == 0) { alertHelper.beep();
		 * lblQuantityErrMsg.setText("Please enter quantity");
		 * txtQuantity.requestFocus(); valid = false; } else {
		 * lblQuantityErrMsg.setText(""); }
		 * 
		 * // Purchase Rate int purRate = txtPurchaseRate.getText().trim().length(); if
		 * (purRate == 0) { alertHelper.beep();
		 * lblPurRateErrMsg.setText("Please enter purchase rate");
		 * txtPurchaseRate.requestFocus(); valid = false; } else {
		 * lblPurRateErrMsg.setText(""); }
		 * 
		 * // Tax int tax = cbTax.getSelectionModel().getSelectedIndex(); if (tax == 0)
		 * { alertHelper.beep(); lblTaxErrMsg.setText("Please select tax");
		 * cbTax.requestFocus(); valid = false; } else { lblTaxErrMsg.setText(""); }
		 * 
		 * // Sell Price int sellPrice = txtSellPrice.getText().trim().length(); if
		 * (sellPrice == 0) { alertHelper.beep();
		 * lblSellPriceErrMsg.setText("Please enter sell price");
		 * txtSellPrice.requestFocus(); valid = false; } else {
		 * lblSellPriceErrMsg.setText(""); }
		 */

		return valid;
	}

	private void resetFields() {

	}

}
