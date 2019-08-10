package com.billing.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.UserDetails;
import com.billing.service.UserService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

@Controller
public class ProductsController implements TabContent {

	private static final Logger logger = LoggerFactory.getLogger(ManageAccountController.class);

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	@Autowired
	UserService userService;

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	@FXML
	private Label heading;

	@FXML
	private GridPane gridPane;

	@FXML
	private ComboBox<?> cbProductCategory;

	@FXML
	private Label lblProductCategoryErrMsg;

	@FXML
	private TextField txtQuantity;

	@FXML
	private Label lblQuantityErrMsg;

	@FXML
	private TextField txtGSTNo;

	@FXML
	private Button btnAdd;

	@FXML
	private Button btnUpdate;

	@FXML
	private Button btnDelete;

	@FXML
	private Button btnReset;

	@FXML
	private TextField lblProductCode;

	@FXML
	private TextField txtProductName;

	@FXML
	private Label lblProductNameErrMsg;

	@FXML
	private ComboBox<?> cbMeasuringUnit;

	@FXML
	private Label lblUnitErrMsg;

	@FXML
	private TextField txtPuirchaseRate;

	@FXML
	private Label lblPurRateErrMsg;

	@FXML
	private TextField txtTax;

	@FXML
	private Label lblTaxErrMsg;

	@FXML
	private TextField lblPurchasePrice;

	@FXML
	private TextField txtSellPrice;

	@FXML
	private Label lblSellPriceErrMsg;

	@FXML
	private TextField lblEnteredBy;

	@FXML
	private TextField lblEntryDate;

	@FXML
	private TextField txtDescription;

	@FXML
	private Label lblViewStockLedger;

	@FXML
	private Label lblPurchasePriceHistory;

	@FXML
	private TextField txtSearchProduct;

	@Override
	public void initialize() {
		lblProductCategoryErrMsg.managedProperty().bind(lblProductCategoryErrMsg.visibleProperty());
		lblProductCategoryErrMsg.visibleProperty()
				.bind(lblProductCategoryErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblQuantityErrMsg.managedProperty().bind(lblQuantityErrMsg.visibleProperty());
		lblQuantityErrMsg.visibleProperty().bind(lblQuantityErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblProductNameErrMsg.managedProperty().bind(lblProductNameErrMsg.visibleProperty());
		lblProductNameErrMsg.visibleProperty()
				.bind(lblProductNameErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblUnitErrMsg.managedProperty().bind(lblUnitErrMsg.visibleProperty());
		lblUnitErrMsg.visibleProperty().bind(lblUnitErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblPurRateErrMsg.managedProperty().bind(lblPurRateErrMsg.visibleProperty());
		lblPurRateErrMsg.visibleProperty().bind(lblPurRateErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblTaxErrMsg.managedProperty().bind(lblTaxErrMsg.visibleProperty());
		lblTaxErrMsg.visibleProperty().bind(lblTaxErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblSellPriceErrMsg.managedProperty().bind(lblSellPriceErrMsg.visibleProperty());
		lblSellPriceErrMsg.visibleProperty().bind(lblSellPriceErrMsg.textProperty().length().greaterThanOrEqualTo(1));

	}

	@FXML
	void onAddCommand(ActionEvent event) {

	}

	@FXML
	void onDeleteCommand(ActionEvent event) {

	}

	@FXML
	void onCloseAction(ActionEvent event) {

	}

	@FXML
	void onResetCommand(ActionEvent event) {

	}

	@FXML
	void onUpdateCommand(ActionEvent event) {

	}

	@Override
	public boolean shouldClose() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void putFocusOnNode() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean loadData() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return true;
	}

}
