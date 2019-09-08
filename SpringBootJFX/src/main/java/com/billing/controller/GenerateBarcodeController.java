package com.billing.controller;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.Product;
import com.billing.dto.StatusDTO;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.ProductService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.TabContent;

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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

@Controller
public class GenerateBarcodeController extends AppContext implements TabContent {

	private static final Logger logger = LoggerFactory.getLogger(GenerateBarcodeController.class);

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	ProductService productService;

	@Autowired
	AppUtils appUtils;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	FilteredList<Product> filteredList;

	int productCode = 0;

	@FXML
	private TextField txtName;

	@FXML
	private TextField txtQuantity;

	@FXML
	private TextField txtCategory;

	@FXML
	private TextField txtBarcode;

	@FXML
	private Button btnGenerateBarcode;

	@FXML
	private Button btnUpdate;

	@FXML
	private TextField txtSearchProduct;

	@FXML
	private Button btnClose;

	@FXML
	private TableView<Product> tableView;

	@FXML
	private TableColumn<Product, String> tcName;

	@FXML
	private TableColumn<Product, String> tcCategory;

	@FXML
	private TableColumn<Product, String> tcQuantity;

	@FXML
	private TableColumn<Product, String> tcUOM;

	@FXML
	private TableColumn<Product, String> tcDescription;

	@Override
	public void initialize() {
		productCode = 0;
		setTableCellFactories();
		// Table row selection
		tableView.getSelectionModel().selectedItemProperty().addListener(this::onSelectedRowChanged);
		txtSearchProduct.textProperty()
				.addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
					if (newValue == null || newValue.isEmpty()) {
						filteredList.setPredicate(null);
					} else {
						filteredList.setPredicate(
								(Product t) -> t.getProductName().toLowerCase().contains(newValue.toLowerCase()));
					}
				});
		txtBarcode.textProperty().addListener((observable, oldValue, newValue) -> {
			btnUpdate.setDisable(newValue.isEmpty());
		});
		txtName.textProperty().addListener((observable, oldValue, newValue) -> {
			btnGenerateBarcode.setDisable(newValue.isEmpty());
		});
		btnUpdate.setDisable(true);
		btnGenerateBarcode.setDisable(true);
	}

	private void setTableCellFactories() {
		// Table Column Mapping
		tcQuantity.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getQuantity())));
		tcName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductName()));
		tcDescription.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
		tcCategory.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductCategory()));
		tcUOM.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMeasure()));
		// Set CSS
		tcQuantity.getStyleClass().add("numeric-cell");
		tcName.getStyleClass().add("character-cell");
		tcDescription.getStyleClass().add("character-cell");
		tcCategory.getStyleClass().add("character-cell");
		tcUOM.getStyleClass().add("character-cell");
	}

	public void onSelectedRowChanged(ObservableValue<? extends Product> observable, Product oldValue,
			Product newValue) {
		resetFields();
		if (newValue != null) {
			txtName.setText(newValue.getProductName());
			txtQuantity.setText(appUtils.getDecimalFormat(newValue.getQuantity()));
			txtCategory.setText(newValue.getProductCategory());
			productCode = newValue.getProductCode();
			txtBarcode.setText("");
		}
	}

	@FXML
	void onGenerateBarcodeCommand(ActionEvent event) {
		if (txtName.getText().equals("")) {
			alertHelper.showErrorNotification("Please select product");
		} else {
			txtBarcode.setText(String.valueOf(appUtils.getBarcode()));
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
		if (txtName.getText().equals("")) {
			alertHelper.showErrorNotification("Please select product");
		} else {
			saveData();
		}
	}

	@Override
	public boolean shouldClose() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void putFocusOnNode() {
		txtSearchProduct.requestFocus();
	}

	@Override
	public boolean loadData() {
		List<Product> list = productService.getProductsWithNoBarcode();
		ObservableList<Product> tableData = FXCollections.observableArrayList();
		tableData.addAll(list);
		filteredList = new FilteredList(tableData, null);
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
		Product product = new Product();
		product.setProductCode(productCode);
		product.setProductBarCode(Long.valueOf(txtBarcode.getText().trim()));
		HashMap<Long, Product> productMap = productService.getProductBarCodeMap();

		if (productMap.containsKey(product.getProductBarCode())) {
			alertHelper.showErrorNotification("Please regenerate barcode");
		} else {
			StatusDTO status = productService.saveBarcode(product);
			if (status.getStatusCode() == 0) {
				alertHelper.showSuccessNotification("Product barcode saved successfully");
				loadData();
				resetFields();
			} else {
				alertHelper.showDataSaveErrAlert(currentStage);
			}
		}
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
		txtName.setText("");
		txtCategory.setText("");
		txtQuantity.setText("");
		txtBarcode.setText("");
		productCode = 0;
	}

}
