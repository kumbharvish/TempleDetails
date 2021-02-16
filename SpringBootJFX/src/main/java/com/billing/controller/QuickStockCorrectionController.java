package com.billing.controller;

import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.Product;
import com.billing.dto.UserDetails;
import com.billing.service.ProductService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.AutoCompleteTextField;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
@Controller
public class QuickStockCorrectionController implements TabContent {

	@Autowired
	ProductService productService;

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	@FXML
	private RadioButton rbBarcode;

	@FXML
	private RadioButton rbName;

	@FXML
	private AutoCompleteTextField txtSearchByItemName;

	@FXML
	private TextField txtSearchByBarcode;

	@FXML
	private Button btnUpdate;

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
		txtSearchByBarcode.requestFocus();
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
		if ("BARCODE".equals(appUtils.getAppDataValues("INVOICE_PRODUCT_SEARCH_BY"))) {
			rbBarcode.setSelected(true);
		} else {
			rbName.setSelected(true);
		}

		txtSearchByItemName.managedProperty().bind(txtSearchByItemName.visibleProperty());
		txtSearchByItemName.visibleProperty().bind(rbName.selectedProperty());
		txtSearchByBarcode.managedProperty().bind(txtSearchByBarcode.visibleProperty());
		txtSearchByBarcode.visibleProperty().bind(rbBarcode.selectedProperty());

		txtQuantityErrMsg.managedProperty().bind(txtQuantityErrMsg.visibleProperty());
		txtQuantityErrMsg.visibleProperty().bind(txtQuantityErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		txtSearchByItemName.prefWidthProperty().bind(txtQuantity.widthProperty());
		txtSearchByBarcode.prefWidthProperty().bind(txtQuantity.widthProperty());
		txtSearchByBarcode.textProperty().addListener(appUtils.getForceNumberListner());
		rbBarcode.setOnAction(e -> resetFields());
		rbName.setOnAction(e -> resetFields());
		getProductsName();
		txtSearchByItemName.createTextField(entries, () -> setProductDetails());

		txtSearchByItemName.setOnKeyPress(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					setProductDetails();
				}
			}
		});

		txtSearchByBarcode.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					setProductDetailsBarcode();
				}
			}
		});
	}

	private void setProductDetails() {
		if (txtSearchByItemName.getText() != null && !txtSearchByItemName.getText().equals("")) {
			Product p = productMap.get(txtSearchByItemName.getText());
			setTextFields(p);
			txtSearchByItemName.setText("");
		}
	}

	private void setProductDetailsBarcode() {
		if (txtSearchByBarcode.getText() != null && !txtSearchByBarcode.getText().equals("")) {
			long barcode = 0;
			barcode = Long.valueOf(txtSearchByBarcode.getText());
			Product p = productMapWithBarcode.get(barcode);
			setTextFields(p);
			txtSearchByBarcode.setText("");
		}
	}

	private void setTextFields(Product p) {
		if (null != p) {
			txtName.setText(p.getProductName());
			txtCategory.setText(p.getProductCategory());
			txtQuantity.setText(appUtils.getDecimalFormat(p.getQuantity()));
			txtSellPrice.setText(appUtils.getDecimalFormat(p.getSellPrice()));
			txtUOM.setText(p.getMeasure());
			txtQuantity.requestFocus();
		}
	}

	private void resetFields() {
		txtName.setText("");
		txtCategory.setText("");
		txtQuantity.setText("");
		txtSellPrice.setText("");
		txtUOM.setText("");
		txtSearchByItemName.setText("");
		setNewFocus();
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
			setNewFocus();
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

	protected void setNewFocus() {
		if (rbBarcode.isSelected()) {
			txtSearchByBarcode.requestFocus();
		} else {
			txtSearchByItemName.requestFocus();
		}
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
		double qty = Double.valueOf(txtQuantity.getText());
		product.setQuantity(qty);
		boolean flag = productService.doQuickStockCorrection(product, qty);
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
		for (Product product : productService.getAll()) {
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