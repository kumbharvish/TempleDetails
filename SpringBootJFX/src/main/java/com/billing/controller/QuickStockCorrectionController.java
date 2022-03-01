package com.billing.controller;

import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.Product;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.ProductService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.AutoCompleteTextField;
import com.billing.utils.Task;

import javafx.animation.PauseTransition;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

@SuppressWarnings("restriction")
@Controller
public class QuickStockCorrectionController extends AppContext {

	@Autowired
	ProductService productService;

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	final PauseTransition scannerDelay = new PauseTransition(Duration.seconds(0.20));

	final StringProperty barcode = new SimpleStringProperty();

	@FXML
	private AutoCompleteTextField txtSearchByItemName;

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

	@FXML
	private Label lblErrProductSearch;

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	private HashMap<String, Product> productMap;

	private HashMap<Long, Product> productMapWithBarcode;

	private SortedSet<String> entries;
	
	Task task;

	public boolean shouldClose() {
		return true;
	}

	public boolean loadData() {
		return true;
	}

	public void setMainWindow(Stage stage) {
		currentStage = stage;
	}

	public void setTabPane(TabPane tabPane) {
		this.tabPane = tabPane;
	}

	public void initialize() {

		txtQuantityErrMsg.managedProperty().bind(txtQuantityErrMsg.visibleProperty());
		txtQuantityErrMsg.visibleProperty().bind(txtQuantityErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		txtSearchByItemName.prefWidthProperty().bind(txtQuantity.widthProperty());
		getProductsName();
		txtSearchByItemName.createTextField(entries, () -> setProductDetails());

		// Read Barcode Scanner Events
		scannerDelay.setOnFinished(event -> barcode.set(txtSearchByItemName.getText()));
		txtSearchByItemName.textProperty().addListener((obs, oldText, newText) -> scannerDelay.playFromStart());
		barcode.addListener((obs, oldBarcode, newBarcode) -> barcodeScanEvent());

		txtSearchByItemName.setOnKeyPress(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					setProductDetails();
				}
			}
		});
		txtSearchByItemName.requestFocus();
		// Qty
		txtQuantity.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (!txtQuantity.getText().equals("") && ke.getCode().equals(KeyCode.ENTER)) {
					onUpdateCommand(null);
				}
			}
		});
	}

	private void setProductDetails() {
		String name = txtSearchByItemName.getText();
		if (!appUtils.isEmptyString(name) && !appUtils.isNumeric(name)) {
			Product p = productMap.get(txtSearchByItemName.getText());
			setTextFields(p);
			txtSearchByItemName.setText("");
		}
	}

	public void barcodeScanEvent() {
		String name = txtSearchByItemName.getText();
		if (!appUtils.isEmptyString(name) && appUtils.isNumeric(name) && name.length() > 4) {
			long barcode = 0;
			barcode = Long.valueOf(name);
			Product p = productMapWithBarcode.get(barcode);
			setTextFields(p);
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
			txtSearchByItemName.setText("");
			lblErrProductSearch.setText("");
		} else {
			// Product Not Found
			lblErrProductSearch.setText("Product not found");
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

	public boolean saveData() {
		return false;
	}

	public void invalidated(Observable observable) {
		isDirty.set(true);
	}

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
		txtSearchByItemName.requestFocus();
	}

	@FXML
	void onCloseCommand(ActionEvent event) {
		Stage stage = (Stage) txtSearchByItemName.getScene().getWindow();
		stage.close();
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
			
			if(task != null) {
				onCloseCommand(null);
				task.doTask();
			} else {
				alertHelper.showSuccessNotification("Product stock updated successfully");
			}
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

	public void setUserDetails(UserDetails user) {
		// TODO Auto-generated method stub

	}

	public void setTask(Task task) {
		this.task = task;
	}
}