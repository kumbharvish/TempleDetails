package com.billing.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.MeasurementUnit;
import com.billing.dto.Product;
import com.billing.dto.ProductCategory;
import com.billing.dto.StatusDTO;
import com.billing.dto.Tax;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.CustomerService;
import com.billing.service.ProductService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

@Controller
public class ProductsController extends AppContext implements TabContent {

	private static final Logger logger = LoggerFactory.getLogger(ProductsController.class);

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	@Autowired
	CustomerService userService;

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	ProductService productService;

	@Autowired
	AppUtils appUtils;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	private HashMap<String, Integer> productCategoryMap;

	FilteredList<Product> filteredList;

	private String productCode;

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
	private ComboBox<String> cbTax;

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
	private TextField txtDiscount;

	@FXML
	private TextField txtHSN;

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

	@FXML
	private TableColumn<Product, String> tcHSN;

	@FXML
	private TableColumn<Product, String> tcDiscount;

	@Override
	public void initialize() {
		productCode = "";
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
		setTableCellFactories();
		// Force Number Listner
		txtPurchaseRate.textProperty().addListener(appUtils.getForceDecimalNumberListner());
		txtQuantity.textProperty().addListener(appUtils.getForceDecimalNumberListner());
		txtBarcode.textProperty().addListener(appUtils.getForceNumberListner());
		txtSellPrice.textProperty().addListener(appUtils.getForceDecimalNumberListner());
		txtDiscount.textProperty().addListener(appUtils.getForceDecimalNumberListner());
		// Table row selection
		tableView.getSelectionModel().selectedItemProperty().addListener(this::onSelectedRowChanged);
		cbProductCategory.prefWidthProperty().bind(cbMeasuringUnit.widthProperty());
		cbTax.prefWidthProperty().bind(cbMeasuringUnit.widthProperty());
		// Fetch comboxes data
		HashMap<String, List> dataMap = productService.getComboboxData();
		populateCategoryComboBox(dataMap.get("CATEGORIES"));
		populateMUnitComboBox(dataMap.get("UOMS"));
		populateTaxtComboBox(dataMap.get("TAXES"));
		// Register textfield listners
		txtPurchaseRate.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue) {
				setPurchasePrice();
			}
		});
		cbTax.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue) {
				setPurchasePrice();
			} else {
				cbTax.show();
			}
		});
		cbProductCategory.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					cbProductCategory.show();
				}
			}
		});
		cbMeasuringUnit.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					cbMeasuringUnit.show();
				}
			}
		});

		txtSearchProduct.textProperty()
				.addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
					if (newValue == null || newValue.isEmpty()) {
						filteredList.setPredicate(null);
					} else {
						filteredList.setPredicate((Product t) -> {
							// Compare name and category
							String lowerCaseFilter = newValue.toLowerCase();
							if (t.getProductName().toLowerCase().contains(lowerCaseFilter)) {
								return true;
							} else if (t.getProductCategory().toLowerCase().contains(lowerCaseFilter)) {
								return true;
							}
							return false;
						});
					}
				});
	}

	private void setTableCellFactories() {
		// Table Column Mapping
		tcCategory.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductCategory()));
		tcName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductName()));
		tcQuantity.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getQuantity())));
		tcMUnit.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMeasure()));
		tcPurchaseRate.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getPurcaseRate())));
		tcTax.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getProductTax())));
		tcSellPrice.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getSellPrice())));
		tcDiscount.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getDiscount())));
		tcDescription.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
		tcHSN.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHsn()));

		// Set CSS
		tcQuantity.getStyleClass().add("numeric-cell");
		tcPurchaseRate.getStyleClass().add("numeric-cell");
		tcTax.getStyleClass().add("numeric-cell");
		tcSellPrice.getStyleClass().add("numeric-cell");
		tcDiscount.getStyleClass().add("numeric-cell");
		tcCategory.getStyleClass().add("character-cell");
		tcName.getStyleClass().add("character-cell");
		tcMUnit.getStyleClass().add("character-cell");
		tcDescription.getStyleClass().add("character-cell");
		tcHSN.getStyleClass().add("character-cell");
	}

	private void setPurchasePrice() {
		double purRateFinal = 0;
		double purchaseRateTemp = 0;
		if (cbTax.getSelectionModel().getSelectedIndex() != 0 && !txtPurchaseRate.getText().equals("")) {
			Double tax = Double.parseDouble(cbTax.getSelectionModel().getSelectedItem());
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
			productCode = String.valueOf(newValue.getProductCode());
			cbProductCategory.getSelectionModel().select(newValue.getProductCategory());
			cbMeasuringUnit.getSelectionModel().select(newValue.getMeasure());
			txtQuantity.setText(appUtils.getDecimalFormat(newValue.getQuantity()));
			txtQuantity.setDisable(true);
			txtPurchaseRate.setText(appUtils.getDecimalFormat(newValue.getPurcaseRate()));
			cbTax.getSelectionModel().select(appUtils.getDecimalFormat(newValue.getProductTax()));
			lblPurchasePrice.setText(appUtils.getDecimalFormat(newValue.getPurcasePrice()));
			txtSellPrice.setText(appUtils.getDecimalFormat(newValue.getSellPrice()));
			txtDiscount.setText(newValue.getDiscount() == 0 ? "" : appUtils.getDecimalFormat(newValue.getDiscount()));
			txtBarcode.setText(newValue.getProductBarCode() == 0 ? "" : String.valueOf(newValue.getProductBarCode()));
			txtHSN.setText(newValue.getHsn());
			lblEnteredBy.setText(newValue.getEnterBy());
			lblEntryDate.setText(appUtils.getFormattedDateWithTime(newValue.getEntryDate()));
			txtDescription.setText(newValue.getDescription());
		}
	}

	public void populateCategoryComboBox(List<ProductCategory> list) {
		productCategoryMap = new HashMap<String, Integer>();
		cbProductCategory.getItems().add("-- Select Category --");
		for (ProductCategory s : list) {
			cbProductCategory.getItems().add(s.getCategoryName());
			productCategoryMap.put(s.getCategoryName(), s.getCategoryCode());
		}
		cbProductCategory.getSelectionModel().select(0);
	}

	public void populateMUnitComboBox(List<MeasurementUnit> list) {
		cbMeasuringUnit.getItems().add("-- Select Measurement Unit --");
		for (MeasurementUnit u : list) {
			cbMeasuringUnit.getItems().add(u.getName());
		}
		cbMeasuringUnit.getSelectionModel().select(0);
	}

	public void populateTaxtComboBox(List<Tax> list) {
		cbTax.getItems().add("-- Select Tax --");
		cbTax.getItems().add("0.00");
		for (Tax u : list) {
			cbTax.getItems().add(appUtils.getDecimalFormat(u.getValue()));
		}
		cbTax.getSelectionModel().select(0);
	}

	@FXML
	void onPurchasePriceHistClick(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
			Product product = tableView.getSelectionModel().getSelectedItem();
			if (productCode.equals("")) {
				alertHelper.showErrorNotification("Please select product");
			} else {
				getPurchasePriceHistPopUp(product);
			}
		}
	}

	@FXML
	void onViewStockLedgertClick(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
			Product product = tableView.getSelectionModel().getSelectedItem();
			if (productCode.equals("")) {
				alertHelper.showErrorNotification("Please select product");
			} else {
				getViewStockLedgerPopUp(product);
			}
		}
	}

	@FXML
	void onAddCommand(ActionEvent event) {
		if (productCode.equals("")) {
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
		if (productCode.equals("")) {
			alertHelper.showErrorNotification("Please select product");
		} else {
			Alert alert = alertHelper.showConfirmAlertWithYesNo(currentStage, null, "Are you sure?");
			if (alert.getResult() == ButtonType.YES) {
				Product product = new Product();
				product.setProductCode(Integer.parseInt(productCode));
				StatusDTO status = productService.delete(product);
				if (status.getStatusCode() == 0) {
					alertHelper.showSuccessNotification("Product deleted successfully");
					resetFields();
					loadData();
				} else {
					alertHelper.showErrorNotification("Error occured during delete product");
				}

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
		tableView.getSelectionModel().clearSelection();
	}

	@FXML
	void onUpdateCommand(ActionEvent event) {
		if (productCode.equals("")) {
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
		txtProductName.requestFocus();
	}

	@Override
	public boolean loadData() {
		List<Product> list = productService.getAll();
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
		productToSave.setQuantity(Double.valueOf(txtQuantity.getText()));
		productToSave.setCategoryCode(productCategoryMap.get(cbProductCategory.getSelectionModel().getSelectedItem()));
		productToSave.setDiscount(0);
		if (txtDiscount.getText().equals("")) {
			productToSave.setDiscount(0);
		} else {
			productToSave.setDiscount(Double.parseDouble(txtDiscount.getText()));
		}
		productToSave.setPurcaseRate(Double.parseDouble(txtPurchaseRate.getText()));
		productToSave.setProductTax(Double.parseDouble(cbTax.getSelectionModel().getSelectedItem()));
		productToSave.setPurcasePrice(Double.parseDouble(lblPurchasePrice.getText()));
		productToSave.setSellPrice(Double.parseDouble(txtSellPrice.getText()));
		productToSave.setEnterBy(userDetails.getFirstName() + " " + userDetails.getLastName());
		productToSave.setEntryDate(appUtils.getCurrentTimestamp());
		productToSave.setLastUpdateDate(appUtils.getCurrentTimestamp());
		productToSave.setHsn(txtHSN.getText());
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
			StatusDTO status = productService.add(productToSave);
			if (status.getStatusCode() == 0) {
				alertHelper.showSuccessNotification("Product added successfully");
				resetFields();
				loadData();
				txtProductName.requestFocus();
			} else {
				if (status.getException().contains("UNIQUE")) {
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
		productToUpdate.setProductCode(Integer.valueOf(productCode));
		productToUpdate.setProductName(txtProductName.getText());
		productToUpdate.setDescription(txtDescription.getText());
		productToUpdate.setMeasure(cbMeasuringUnit.getSelectionModel().getSelectedItem());
		productToUpdate.setQuantity(Double.valueOf(txtQuantity.getText()));
		productToUpdate
				.setCategoryCode(productCategoryMap.get(cbProductCategory.getSelectionModel().getSelectedItem()));
		productToUpdate.setDiscount(0);
		if (txtDiscount.getText().equals("")) {
			productToUpdate.setDiscount(0);
		} else {
			productToUpdate.setDiscount(Double.parseDouble(txtDiscount.getText()));
		}
		productToUpdate.setPurcaseRate(Double.parseDouble(txtPurchaseRate.getText()));
		productToUpdate.setProductTax(Double.parseDouble(cbTax.getSelectionModel().getSelectedItem()));
		productToUpdate.setPurcasePrice(Double.parseDouble(lblPurchasePrice.getText()));
		productToUpdate.setSellPrice(Double.parseDouble(txtSellPrice.getText()));
		productToUpdate.setEnterBy(userDetails.getFirstName() + " " + userDetails.getLastName());
		productToUpdate.setLastUpdateDate(appUtils.getCurrentTimestamp());
		productToUpdate.setHsn(txtHSN.getText());
		if (txtBarcode.getText().equals("")) {
			productToUpdate.setProductBarCode(Long.valueOf(0));
		} else {
			productToUpdate.setProductBarCode(Long.valueOf(txtBarcode.getText()));
		}
		HashMap<Long, Product> productMap = productService.getProductBarCodeMap();
		if (productMap.containsKey(productToUpdate.getProductBarCode()) && productMap
				.get(productToUpdate.getProductBarCode()).getProductCode() != productToUpdate.getProductCode()) {
			alertHelper.beep();
			alertHelper.showErrorNotification("Entered product barcode already exists");
			txtBarcode.requestFocus();
		} else {
			StatusDTO status = productService.update(productToUpdate);

			if (status.getStatusCode() == 0) {
				alertHelper.showSuccessNotification("Product updated successfully");
				loadData();
				resetFields();
			} else {
				if (status.getException().contains("UNIQUE")) {
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
		int tax = cbTax.getSelectionModel().getSelectedIndex();
		if (tax == 0) {
			alertHelper.beep();
			lblTaxErrMsg.setText("Please select tax");
			cbTax.requestFocus();
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
		productCode = "";
		cbProductCategory.getSelectionModel().select(0);
		cbMeasuringUnit.getSelectionModel().select(0);
		txtQuantity.setText("");
		txtQuantity.setDisable(false);
		txtPurchaseRate.setText("");
		cbTax.getSelectionModel().select(0);
		lblPurchasePrice.setText("");
		txtSellPrice.setText("");
		txtDiscount.setText("");
		txtHSN.setText("");
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

	private void getViewStockLedgerPopUp(Product product) {
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setControllerFactory(springContext::getBean);
		fxmlLoader.setLocation(this.getClass().getResource("/com/billing/gui/ViewStockLedger.fxml"));

		Parent rootPane = null;
		try {
			rootPane = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("getViewStockLedgerPopUp Error in loading the view file :", e);
			alertHelper.beep();

			alertHelper.showErrorAlert(currentStage, "Error Occurred", "Error in creating user interface",
					"An error occurred in creating user interface " + "for the selected command");

			return;
		}

		final Scene scene = new Scene(rootPane);
		final ViewStockLedgerController controller = (ViewStockLedgerController) fxmlLoader.getController();

		controller.product = product;

		final Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(currentStage);
		stage.setUserData(controller);
		stage.getIcons().add(new Image("/images/shop32X32.png"));
		stage.setScene(scene);
		stage.setTitle("Product Stock Ledger");
		controller.loadData();
		stage.showAndWait();
	}

}
