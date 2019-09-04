package com.billing.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.constants.AppConstants;
import com.billing.dto.Product;
import com.billing.dto.UserDetails;
import com.billing.service.ProductHistoryService;
import com.billing.service.ProductService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.AutoCompleteTextField;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
@Controller
public class QuickStockCorrectionController implements TabContent {

	@Autowired
	ProductService productService;

	@Autowired
	ProductHistoryService productHistoryService;

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	@FXML
	private BorderPane borderPane;

	@FXML
	private Label heading;

	@FXML
	private RadioButton rbBarcode;

	@FXML
	private RadioButton rbName;

	@FXML
	private AutoCompleteTextField txtSearchBy;

	@FXML
	private Button btnUpdate;

	@FXML
	private Button btnClose;

	@FXML
	private TextField txtName;

	@FXML
	private TextField txtCategory;

	@FXML
	private TextField txtQuantity;

	@FXML
	private Label txtQuantityErrMsg;

	@FXML
	private TextField txtUOM;

	@FXML
	private TextField txtSellPrice;

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	private HashMap<String, Product> productMap;

	private HashMap<Long, Product> productMapWithBarcode;

	private SortedSet<String> entries;

	@Override
	public boolean shouldClose() {
		return true;
	}

	@Override
	public void putFocusOnNode() {
		txtSearchBy.requestFocus();
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
	public void setTabPane(TabPane tabPane) {
		this.tabPane = tabPane;
	}

	@Override
	public void initialize() {
		ToggleGroup radioButtonGroup = new ToggleGroup();
		rbBarcode.setToggleGroup(radioButtonGroup);
		rbName.setToggleGroup(radioButtonGroup);
		rbBarcode.setSelected(true);

		txtQuantityErrMsg.managedProperty().bind(txtQuantityErrMsg.visibleProperty());
		txtQuantityErrMsg.visibleProperty().bind(txtQuantityErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		txtSearchBy.prefWidthProperty().bind(txtQuantity.widthProperty());
		rbBarcode.setOnAction(e -> resetFields());
		rbName.setOnAction(e -> resetFields());
		getProductsName();
		txtSearchBy.createTextField(entries,()->setProductDetails());
	}

	private void setProductDetails() {
		if (txtSearchBy.getText() != null && !txtSearchBy.getText().equals("")) {
			if (rbBarcode.isSelected()) {
				long barcode = 0;
				try {
					barcode = Long.valueOf(txtSearchBy.getText());
					Product p = productMapWithBarcode.get(barcode);
					setTextFields(p);
				} catch (NumberFormatException e) {
					alertHelper.showErrorNotification("Please choose name option for search");
				}

			} else {
				Product p = productMap.get(txtSearchBy.getText());
				setTextFields(p);
			}
		}
	}

	private void setTextFields(Product p) {
		txtName.setText(p.getProductName());
		txtCategory.setText(p.getProductCategory());
		txtQuantity.setText(appUtils.getDecimalFormat(p.getQuantity()));
		txtSellPrice.setText(appUtils.getDecimalFormat(p.getSellPrice()));
		txtUOM.setText(p.getMeasure());
		txtQuantity.requestFocus();
	}

	private void resetFields() {
		txtName.setText("");
		txtCategory.setText("");
		txtQuantity.setText("");
		txtSellPrice.setText("");
		txtUOM.setText("");
		txtSearchBy.setText("");
		txtSearchBy.requestFocus();
	}

	@Override
	public boolean saveData() {
		return false;
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
		int name = txtName.getText().trim().length();
		if (name == 0) {
			alertHelper.showErrorNotification("Please select product");
			txtSearchBy.requestFocus();
			valid = false;
		} else {
			int uom = txtQuantity.getText().trim().length();
			if (uom == 0) {
				alertHelper.beep();
				txtQuantityErrMsg.setText("Please enter quantity");
				txtQuantity.requestFocus();
				valid = false;
			} else {
				txtQuantityErrMsg.setText("");
			}
		}
		return valid;
	}

	@FXML
	void onCloseCommand(ActionEvent event) {
		closeTab();
	}

	@FXML
	void onUpdateCommand(ActionEvent event) {
		if (!validateInput()) {
			return;
		}
		updateData();
	}

	private void updateData() {
		Product product = new Product();
		product.setProductCode(productMap.get(txtName.getText()).getProductCode());
		product.setQuantity(Double.valueOf(txtQuantity.getText()));
		List<Product> productList = new ArrayList<Product>();
		Product p = productService.getProduct(product.getProductCode());
		product.setDescription("Existing Stock: " + appUtils.getDecimalFormat(p.getQuantity()) + " Correction Qty: "
				+ appUtils.getDecimalFormat(product.getQuantity()));
		if (product.getQuantity() < p.getQuantity()) {
			product.setQuantity(p.getQuantity() - product.getQuantity());
			productList.add(product);
			productHistoryService.addProductStockLedger(productList, AppConstants.STOCK_OUT,
					AppConstants.QUICK_STOCK_CORR);
		} else {
			product.setQuantity(product.getQuantity() - p.getQuantity());
			productList.add(product);
			productHistoryService.addProductStockLedger(productList, AppConstants.STOCK_IN,
					AppConstants.QUICK_STOCK_CORR);
		}
		product.setQuantity(Double.valueOf(txtQuantity.getText()));
		boolean flag = productService.quickStockCorrection(product);
		if (flag) {
			resetFields();
			alertHelper.showSuccessNotification("Product stock updated successfully");
			closeTab();
		} else {
			alertHelper.showDataSaveErrAlert(currentStage);
		}

	}

	public void getProductsName() {
		entries = new TreeSet<String>();
		productMap = new HashMap<String, Product>();
		productMapWithBarcode = new HashMap<Long, Product>();
		for (Product product : productService.getAllProducts()) {
			entries.add(product.getProductName());
			productMap.put(product.getProductName(), product);
			productMapWithBarcode.put(product.getProductBarCode(), product);
		}
	}

	@Override
	public void setUserDetails(UserDetails user) {
		// TODO Auto-generated method stub

	}

}