package com.billing.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.constants.AppConstants;
import com.billing.dto.Product;
import com.billing.dto.ProductCategory;
import com.billing.dto.StatusDTO;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.ProductCategoryService;
import com.billing.service.ProductHistoryService;
import com.billing.service.ProductService;
import com.billing.service.UserService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.TabContent;
import com.billing.utils.Utility;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

@Controller
public class ProductsController extends AppContext implements TabContent {

	private static final Logger logger = LoggerFactory.getLogger(ManageAccountController.class);

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	@Autowired
	UserService userService;

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	ProductService productService;

	@Autowired
	ProductHistoryService productHistoryService;

	@Autowired
	AppUtils appUtils;

	@Autowired
	ProductCategoryService productCategoryService;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	private HashMap<String, Integer> productCategoryMap;

	FilteredList<Product> filteredList;

	@FXML
	private Label heading;

	@FXML
	private GridPane gridPane;

	@FXML
	private ComboBox<String> cbProductCategory;

	@FXML
	private Label lblProductCategoryErrMsg;

	@FXML
	private TextField txtQuantity;

	@FXML
	private Label lblQuantityErrMsg;

	@FXML
	private TextField txtBarcode;

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
	private ComboBox<String> cbMeasuringUnit;

	@FXML
	private Label lblUnitErrMsg;

	@FXML
	private TextField txtPurchaseRate;

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

	@FXML
	private TableView<Product> tableView;

	@FXML
	private TableColumn<Product, String> tcCategory;

	@FXML
	private TableColumn<Product, String> tcName;

	@FXML
	private TableColumn<Product, String> tcQuantity;

	@FXML
	private TableColumn<Product, String> tcMUnit;

	@FXML
	private TableColumn<Product, String> tcPurchaseRate;

	@FXML
	private TableColumn<Product, String> tcTax;

	@FXML
	private TableColumn<Product, String> tcSellPrice;

	@FXML
	private TableColumn<Product, String> tcDescription;

	@Override
	public void initialize() {
		// Error Messages
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
		// Table Column Mapping
		tcCategory.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductCategory()));
		tcName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductName()));
		tcQuantity.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getQuanity())));
		tcMUnit.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMeasure()));
		tcPurchaseRate.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getPurcaseRate())));
		tcTax.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getProductTax())));
		tcSellPrice.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getSellPrice())));
		tcDescription.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
		// Force Number Listner
		txtPurchaseRate.textProperty().addListener(Utility.getForceDecimalNumberListner());
		txtQuantity.textProperty().addListener(Utility.getForceDecimalNumberListner());
		txtBarcode.textProperty().addListener(Utility.getForceNumberListner());
		txtTax.textProperty().addListener(Utility.getForceDecimalNumberListner());
		txtSellPrice.textProperty().addListener(Utility.getForceDecimalNumberListner());
		// Table row selection
		tableView.getSelectionModel().selectedItemProperty().addListener(this::onSelectedRowChanged);
		cbMeasuringUnit.prefWidthProperty().bind(cbProductCategory.widthProperty());
		populateCategoryComboBox();
		populateMUnitComboBox();
		// Register textfield listners
		txtPurchaseRate.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue) {
				setPurchasePrice();
			}
		});
		txtTax.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue) {
				setPurchasePrice();
			}
		});

		txtSearchProduct.textProperty()
				.addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
					if (newValue == null || newValue.isEmpty()) {
						filteredList.setPredicate(null);
					} else {
						filteredList.setPredicate(
								(Product t) -> t.getProductName().toLowerCase().contains(newValue.toLowerCase()));
					}
				});
	}

	private void setPurchasePrice() {
		double purRateFinal = 0;
		double purchaseRateTemp = 0;
		if (!txtTax.getText().equals("") && !txtPurchaseRate.getText().equals("")) {
			Double tax = Double.parseDouble(txtTax.getText());
			purchaseRateTemp = Double.parseDouble(txtPurchaseRate.getText());
			double tempPurRate = purchaseRateTemp;
			tempPurRate = tempPurRate + (purchaseRateTemp / 100) * tax;
			purRateFinal = tempPurRate;
			lblPurchasePrice.setText(appUtils.getDecimalFormat(purRateFinal));
		} else {
			if (!txtPurchaseRate.getText().equals("")) {
				purchaseRateTemp = Double.parseDouble(txtPurchaseRate.getText());
				lblPurchasePrice.setText(appUtils.getDecimalFormat(purchaseRateTemp));
			} else {
				purRateFinal = purchaseRateTemp;
				lblPurchasePrice.setText(appUtils.getDecimalFormat(purRateFinal));
			}

		}
	}

	public void onSelectedRowChanged(ObservableValue<? extends Product> observable, Product oldValue,
			Product newValue) {
		resetFields();
		if (newValue != null) {
			txtProductName.setText(newValue.getProductName());
			lblProductCode.setText(String.valueOf(newValue.getProductCode()));
			cbProductCategory.getSelectionModel().select(newValue.getProductCategory());
			cbMeasuringUnit.getSelectionModel().select(newValue.getMeasure());
			txtQuantity.setText(String.valueOf(newValue.getQuanity()));
			txtPurchaseRate.setText(appUtils.getDecimalFormat(newValue.getPurcaseRate()));
			txtTax.setText(appUtils.getDecimalFormat(newValue.getProductTax()));
			lblPurchasePrice.setText(appUtils.getDecimalFormat(newValue.getPurcasePrice()));
			txtSellPrice.setText(appUtils.getDecimalFormat(newValue.getSellPrice()));
			txtBarcode.setText(newValue.getProductBarCode() == 0 ? "" : String.valueOf(newValue.getProductBarCode()));
			lblEnteredBy.setText(newValue.getEnterBy());
			lblEntryDate.setText(newValue.getEntryDate().toString());
			txtDescription.setText(newValue.getDescription());
		}
	}

	public void populateCategoryComboBox() {
		productCategoryMap = new HashMap<String, Integer>();
		cbProductCategory.getItems().add("-- Select Category --");
		for (ProductCategory s : productCategoryService.getAllCategories()) {
			cbProductCategory.getItems().add(s.getCategoryName());
			productCategoryMap.put(s.getCategoryName(), s.getCategoryCode());
		}
		cbProductCategory.getSelectionModel().select(0);
	}

	public void populateMUnitComboBox() {
		cbMeasuringUnit.getItems().add("-- Select Measurement Unit --");
		/*
		 * for (ProductCategory s : productCategoryService.getAllCategories()) {
		 * cbMeasuringUnit.getItems().add(s.getCategoryName()); }
		 */
		cbMeasuringUnit.getItems().add("Qty");
		cbMeasuringUnit.getSelectionModel().select(0);
	}

	@FXML
	void onPurchasePriceHistClick(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
			Product product = tableView.getSelectionModel().getSelectedItem();
			getPurchasePriceHistPopUp(product);
		}
	}

	@FXML
	void onViewStockLedgertClick(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
		}
	}

	@FXML
	void onAddCommand(ActionEvent event) {
		if (lblProductCode.getText().equals("")) {
			if (!validateInput()) {
				return;
			}
			saveData();
		} else {
			alertHelper.showErrorNotification("Please reset fields");
		}
	}

	@FXML
	void onDeleteCommand(ActionEvent event) {
		if (lblProductCode.getText().equals("")) {
			alertHelper.showErrorNotification("Please select product");
		} else {
			Alert alert = alertHelper.showConfirmAlertWithYesNo(currentStage, null, "Are you sure?");
			if (alert.getResult() == ButtonType.YES) {
				productService.deleteProduct(Integer.parseInt(lblProductCode.getText()));
				alertHelper.showSuccessNotification("Product deleted successfully");
				resetFields();
				loadData();
			} else {
				resetFields();
			}

		}
	}

	@FXML
	void onCloseAction(ActionEvent event) {
		closeTab();
	}

	@FXML
	void onResetCommand(ActionEvent event) {
		resetFields();
	}

	@FXML
	void onUpdateCommand(ActionEvent event) {
		if (lblProductCode.getText().equals("")) {
			alertHelper.showErrorNotification("Please select product");
		} else {
			if (!validateInput()) {
				return;
			}
			updateData();
		}
	}

	@Override
	public boolean shouldClose() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void putFocusOnNode() {
		cbProductCategory.requestFocus();
	}

	@Override
	public boolean loadData() {
		List<Product> list = productService.getAllProducts();
		ObservableList<Product> productTableData = FXCollections.observableArrayList();
		productTableData.addAll(list);
		filteredList = new FilteredList(productTableData, null);
		tableView.setItems(filteredList);
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
		Product productToSave = new Product();
		productToSave.setProductCode(appUtils.getRandomCode());
		productToSave.setProductName(txtProductName.getText());
		productToSave.setDescription(txtDescription.getText());
		productToSave.setMeasure(cbMeasuringUnit.getSelectionModel().getSelectedItem());
		productToSave.setQuanity(Integer.valueOf(txtQuantity.getText()));
		// productToSave.setProductCategory((String)productCategory.getSelectedItem());
		productToSave.setCategoryCode(productCategoryMap.get(cbProductCategory.getSelectionModel().getSelectedItem()));
		productToSave.setDiscount(0);
		/*
		 * if(discount.getText().equals("")){ productToSave.setDiscount(0); }else{
		 * productToSave.setDiscount(Double.parseDouble(discount.getText())); }
		 */
		productToSave.setPurcaseRate(Double.parseDouble(txtPurchaseRate.getText()));
		productToSave.setProductTax(Double.parseDouble(txtTax.getText()));
		productToSave.setPurcasePrice(Double.parseDouble(lblPurchasePrice.getText()));
		productToSave.setSellPrice(Double.parseDouble(txtSellPrice.getText()));
		productToSave.setEnterBy(userDetails.getFirstName() + " " + userDetails.getLastName());
		productToSave.setEntryDate(new java.sql.Date(System.currentTimeMillis()));
		productToSave.setLastUpdateDate(new java.sql.Date(System.currentTimeMillis()));
		if (txtBarcode.getText().equals("")) {
			productToSave.setProductBarCode(Long.valueOf(0));
		} else {
			productToSave.setProductBarCode(Long.valueOf(txtBarcode.getText()));
		}

		if (productService.getProductBarCodeMap().containsKey(productToSave.getProductBarCode())) {
			alertHelper.beep();
			alertHelper.showErrorNotification("Entered product barcode already exists");
			txtBarcode.requestFocus();
		} else {
			StatusDTO status = productService.addProduct(productToSave);
			if (status.getStatusCode() == 0) {
				List<Product> list = new ArrayList<Product>();
				productToSave.setDescription("Add Product Opening Stock");
				list.add(productToSave);
				// Update Stock Ledger
				productHistoryService.addProductStockLedger(list, AppConstants.STOCK_IN, AppConstants.ADD_PRODUCT);
				// Add Product Purchase price history
				productToSave.setDescription(AppConstants.ADD_PRODUCT);
				productToSave.setSupplierId(001);
				productHistoryService.addProductPurchasePriceHistory(list);
				alertHelper.showSuccessNotification("Product added successfully");
				resetFields();
				loadData();
			} else {
				if (status.getException().contains("Duplicate entry")) {
					alertHelper.beep();
					alertHelper.showErrorNotification("Entered product name already exists");
					txtProductName.requestFocus();
				} else {
					alertHelper.showDataSaveErrAlert(currentStage);
				}
			}
		}
		return true;
	}

	private void updateData() {
		Product productToUpdate = new Product();
		productToUpdate.setProductCode(Integer.valueOf(lblProductCode.getText()));
		productToUpdate.setProductName(txtProductName.getText());
		productToUpdate.setDescription(txtDescription.getText());
		productToUpdate.setMeasure(cbMeasuringUnit.getSelectionModel().getSelectedItem());
		productToUpdate.setQuanity(Integer.valueOf(txtQuantity.getText()));
		// productToUpdate.setProductCategory((String)productCategory.getSelectedItem());
		productToUpdate
				.setCategoryCode(productCategoryMap.get(cbProductCategory.getSelectionModel().getSelectedItem()));
		productToUpdate.setDiscount(0);
		/*
		 * if(discount.getText().equals("")){ productToUpdate.setDiscount(0); }else{
		 * productToUpdate.setDiscount(Double.parseDouble(discount.getText())); }
		 */
		productToUpdate.setPurcaseRate(Double.parseDouble(txtPurchaseRate.getText()));
		productToUpdate.setProductTax(Double.parseDouble(txtTax.getText()));
		productToUpdate.setPurcasePrice(Double.parseDouble(lblPurchasePrice.getText()));
		productToUpdate.setSellPrice(Double.parseDouble(txtSellPrice.getText()));
		productToUpdate.setEnterBy(userDetails.getFirstName() + " " + userDetails.getLastName());
		productToUpdate.setLastUpdateDate(new java.sql.Date(System.currentTimeMillis()));
		if (txtBarcode.getText().equals("")) {
			productToUpdate.setProductBarCode(Long.valueOf(0));
		} else {
			productToUpdate.setProductBarCode(Long.valueOf(txtBarcode.getText()));
		}
		if (productService.getProductBarCodeMap().containsKey(productToUpdate.getProductBarCode())
				&& productService.getProductBarCodeMap().get(productToUpdate.getProductBarCode())
						.getProductCode() != productToUpdate.getProductCode()) {
			alertHelper.beep();
			alertHelper.showErrorNotification("Entered product barcode already exists");
			txtBarcode.requestFocus();
		} else {
			StatusDTO status = productService.updateProduct(productToUpdate);

			if (status.getStatusCode() == 0) {
				alertHelper.showSuccessNotification("Product updated successfully");
				loadData();
				resetFields();
			} else {
				if (status.getException().contains("Duplicate entry")) {
					alertHelper.beep();
					alertHelper.showErrorNotification("Entered product name already exists");
				} else {
					alertHelper.showDataSaveErrAlert(currentStage);
				}
			}
		}
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
		// Category
		int category = cbProductCategory.getSelectionModel().getSelectedIndex();
		if (category == 0) {
			alertHelper.beep();
			lblProductCategoryErrMsg.setText("Please select category");
			cbProductCategory.requestFocus();
			valid = false;
		} else {
			lblProductCategoryErrMsg.setText("");
		}
		// Product Name
		int name = txtProductName.getText().trim().length();
		if (name == 0) {
			alertHelper.beep();
			lblProductNameErrMsg.setText("Please enter product name");
			txtProductName.requestFocus();
			valid = false;
		} else {
			lblProductNameErrMsg.setText("");
		}

		// Measurement Unit
		int mUnit = cbMeasuringUnit.getSelectionModel().getSelectedIndex();
		if (mUnit == 0) {
			alertHelper.beep();
			lblUnitErrMsg.setText("Please select measurement unit");
			cbMeasuringUnit.requestFocus();
			valid = false;
		} else {
			lblUnitErrMsg.setText("");
		}

		// Quantity
		int quantity = txtQuantity.getText().trim().length();
		if (quantity == 0) {
			alertHelper.beep();
			lblQuantityErrMsg.setText("Please enter quantity");
			txtQuantity.requestFocus();
			valid = false;
		} else {
			lblQuantityErrMsg.setText("");
		}

		// Purchase Rate
		int purRate = txtPurchaseRate.getText().trim().length();
		if (purRate == 0) {
			alertHelper.beep();
			lblPurRateErrMsg.setText("Please enter purchase rate");
			txtPurchaseRate.requestFocus();
			valid = false;
		} else {
			lblPurRateErrMsg.setText("");
		}

		// Tax
		int tax = txtTax.getText().trim().length();
		if (tax == 0) {
			alertHelper.beep();
			lblTaxErrMsg.setText("Please enter tax");
			txtTax.requestFocus();
			valid = false;
		} else {
			lblTaxErrMsg.setText("");
		}

		// Sell Price
		int sellPrice = txtSellPrice.getText().trim().length();
		if (sellPrice == 0) {
			alertHelper.beep();
			lblSellPriceErrMsg.setText("Please enter sell price");
			txtSellPrice.requestFocus();
			valid = false;
		} else {
			lblSellPriceErrMsg.setText("");
		}

		return valid;
	}

	private void resetFields() {
		txtProductName.setText("");
		lblProductCode.setText("");
		cbProductCategory.getSelectionModel().select(0);
		cbMeasuringUnit.getSelectionModel().select(0);
		txtQuantity.setText("");
		txtPurchaseRate.setText("");
		txtTax.setText("");
		lblPurchasePrice.setText("");
		txtSellPrice.setText("");
		txtBarcode.setText("");
		lblEnteredBy.setText("");
		lblEntryDate.setText("");
		txtDescription.setText("");
		// Reset Error msg
		lblProductCategoryErrMsg.setText("");
		lblProductNameErrMsg.setText("");
		lblPurRateErrMsg.setText("");
		lblQuantityErrMsg.setText("");
		lblSellPriceErrMsg.setText("");
		lblTaxErrMsg.setText("");
		lblUnitErrMsg.setText("");

	}

	private void getPurchasePriceHistPopUp(Product product) {
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setControllerFactory(springContext::getBean);
		fxmlLoader.setLocation(this.getClass().getResource("/com/billing/gui/ProductPurchasePriceHistroy.fxml"));

		Parent rootPane = null;
		try {
			rootPane = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("getPurchasePriceHistPopUp Error in loading the view file :", e);
			alertHelper.beep();

			alertHelper.showErrorAlert(currentStage, "Error Occurred", "Error in creating user interface",
					"An error occurred in creating user interface " + "for the selected command");

			return;
		}

		final Scene scene = new Scene(rootPane);
		final PurchasePriceHistoryController controller = (PurchasePriceHistoryController) fxmlLoader.getController();

		controller.product = product;

		final Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(currentStage);
		stage.setUserData(controller);
		stage.getIcons().add(new Image("/images/shop32X32.png"));
		stage.setScene(scene);
		stage.setTitle("Product Purchase Price History");
		controller.loadData();
		stage.showAndWait();
	}

}
