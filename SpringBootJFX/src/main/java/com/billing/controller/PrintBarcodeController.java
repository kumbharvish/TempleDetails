package com.billing.controller;

import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.constants.AppConstants;
import com.billing.dto.Barcode;
import com.billing.dto.Product;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.PrinterService;
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
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

@Controller
public class PrintBarcodeController extends AppContext implements TabContent {

	private static final Logger logger = LoggerFactory.getLogger(PrintBarcodeController.class);

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	ProductService productService;

	@Autowired
	AppUtils appUtils;

	@Autowired
	PrinterService printerService;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	int productCode = 0;

	private HashMap<String, Product> productMap;

	private SortedSet<String> entries;

	@FXML
	private AutoCompleteTextField txtName;

	@FXML
	private TextField txtQuantity;

	@FXML
	private TextField txtCategory;

	@FXML
	private TextField txtBarcode;

	@FXML
	private Button btnGenerateBarcode;

	@FXML
	private RadioButton rb_65Stickers;

	@FXML
	private RadioButton rb_40Stickers;

	@FXML
	private RadioButton rb_24Stickers;

	@FXML
	private RadioButton rb_Thermal_Single_5025;

	@FXML
	private RadioButton rb_Thermal_Double_5025;
	
	@FXML
	private RadioButton rb_Thermal_Single_3825;

	@FXML
	private RadioButton rb_Thermal_Double_3825;

	@FXML
	private TextField txtStartPosition;

	@FXML
	private TextField txtNoOfBarcodes;

	@FXML
	private TextField txtSellPrice;

	@FXML
	private ImageView imageView;

	@Override
	public void initialize() {
		productCode = 0;
		ToggleGroup radioButtonGroup = new ToggleGroup();
		rb_24Stickers.setToggleGroup(radioButtonGroup);
		rb_40Stickers.setToggleGroup(radioButtonGroup);
		rb_65Stickers.setToggleGroup(radioButtonGroup);
		rb_Thermal_Single_5025.setToggleGroup(radioButtonGroup);
		rb_Thermal_Double_5025.setToggleGroup(radioButtonGroup);
		rb_Thermal_Single_3825.setToggleGroup(radioButtonGroup);
		rb_Thermal_Double_3825.setToggleGroup(radioButtonGroup);

		txtCategory.textProperty().addListener((observable, oldValue, newValue) -> {
			btnGenerateBarcode.setDisable(newValue.isEmpty());
		});
		btnGenerateBarcode.setDisable(true);

		getProductsName();
		txtName.createTextField(entries, () -> setProductDetails());
		txtStartPosition.setText("1");
		txtNoOfBarcodes.setText("65");

		rb_65Stickers.setOnAction(e -> populateImageView());
		rb_24Stickers.setOnAction(e -> populateImageView());
		rb_40Stickers.setOnAction(e -> populateImageView());
		rb_Thermal_Single_5025.setOnAction(e -> populateImageView());
		rb_Thermal_Double_5025.setOnAction(e -> populateImageView());
		rb_Thermal_Single_3825.setOnAction(e -> populateImageView());
		rb_Thermal_Double_3825.setOnAction(e -> populateImageView());

		rb_65Stickers.setSelected(true);
		imageView.setImage(new Image(this.getClass().getResource("/images/65_Labels.png").toString()));
	}

	private void populateImageView() {
		String imageName = "65_Labels.png";
		if (rb_65Stickers.isSelected()) {
			txtStartPosition.setDisable(false);
			imageName = "65_Labels.png";
			txtStartPosition.setText("1");
			txtNoOfBarcodes.setText("65");
		} else if (rb_40Stickers.isSelected()) {
			txtStartPosition.setDisable(false);
			imageName = "40_Labels.png";
			txtStartPosition.setText("1");
			txtNoOfBarcodes.setText("40");
		} else if (rb_24Stickers.isSelected()) {
			txtStartPosition.setDisable(false);
			imageName = "24_Labels.png";
			txtStartPosition.setText("1");
			txtNoOfBarcodes.setText("24");
		} else if (rb_Thermal_Single_5025.isSelected()) {
			imageName = "Single_5025mm.png";
			txtStartPosition.setDisable(true);
			txtStartPosition.setText("Not Applicable");
			txtNoOfBarcodes.setText("1");
		} else if (rb_Thermal_Double_5025.isSelected()) {
			imageName = "Double_5025mm.png";
			txtStartPosition.setDisable(true);
			txtStartPosition.setText("Not Applicable");
			txtNoOfBarcodes.setText("2");
		} else if (rb_Thermal_Single_3825.isSelected()) {
			imageName = "Single_3825mm.png";
			txtStartPosition.setDisable(true);
			txtStartPosition.setText("Not Applicable");
			txtNoOfBarcodes.setText("1");
		} else if (rb_Thermal_Double_3825.isSelected()) {
			imageName = "Double_3825mm.png";
			txtStartPosition.setDisable(true);
			txtStartPosition.setText("Not Applicable");
			txtNoOfBarcodes.setText("2");
		}

		imageView.setImage(new Image(this.getClass().getResource("/images/" + imageName).toString()));
	}

	@FXML
	void onGenerateBarcodeCommand(ActionEvent event) {
		if (txtName.getText().equals("")) {
			alertHelper.showErrorNotification("Please select product");
		} else if (txtBarcode.getText().equals("")) {
			alertHelper.showErrorNotification("Please generate the barcode for product");
		} else {

			String JrxmlName = null;
			int startPosition = 1;
			int noOfLabels = 0;
			try {
				Product p = productMap.get(txtName.getText());
				Barcode barcode = new Barcode();
				barcode.setBarcode(txtBarcode.getText().trim());
				barcode.setPrice(Double.valueOf(txtSellPrice.getText()));
				barcode.setProductName(txtName.getText());
				barcode.setProductCode(p.getProductCode());
				barcode.setCategoryName(p.getProductCategory());
				if (rb_65Stickers.isSelected()) {
					JrxmlName = AppConstants.BARCODE_65_JASPER;
					noOfLabels = 65;
				} else if (rb_24Stickers.isSelected()) {
					JrxmlName = AppConstants.BARCODE_24_JASPER;
					noOfLabels = 24;
				} else if (rb_40Stickers.isSelected()) {
					JrxmlName = AppConstants.BARCODE_40_JASPER;
					noOfLabels = 40;
				} else if (rb_Thermal_Single_5025.isSelected()) {
					JrxmlName = AppConstants.BARCODE_THERMAL_SINGLE_5025_JASPER;
					noOfLabels = 1;
				} else if (rb_Thermal_Double_5025.isSelected()) {
					JrxmlName = AppConstants.BARCODE_THERMAL_DOUBLE_5025_JASPER;
					noOfLabels = 1;
				} else if (rb_Thermal_Single_3825.isSelected()) {
					JrxmlName = AppConstants.BARCODE_THERMAL_SINGLE_3825_JASPER;
					noOfLabels = 1;
				} else if (rb_Thermal_Double_3825.isSelected()) {
					JrxmlName = AppConstants.BARCODE_THERMAL_DOUBLE_3825_JASPER;
					noOfLabels = 1;
				}
				if (!txtStartPosition.getText().equals("") && !txtStartPosition.getText().equals("Not Applicable")) {
					startPosition = Integer.valueOf(txtStartPosition.getText());
				} else if (txtStartPosition.getText().equals("Not Applicable")) {
					startPosition = 0;
				} else {
					alertHelper.showErrorNotification("Please enter start position");
					return;
				}
				if (!txtNoOfBarcodes.getText().equals("")) {
					noOfLabels = Integer.valueOf(txtNoOfBarcodes.getText());
				} else {
					alertHelper.showErrorNotification("Please enter no of barcodes");
					return;
				}

				boolean isSuccess = printerService.printBarcodeSheet(barcode, noOfLabels, startPosition, JrxmlName);
				if (!isSuccess) {
					alertHelper
							.showErrorNotification("Error occurred while generating barcode sheet");
				}
			} catch (Exception e1) {
				e1.printStackTrace();
				logger.error("Print Barcode Sheet Exception : ", e1);
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

	@Override
	public boolean shouldClose() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void putFocusOnNode() {
		txtName.requestFocus();
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
		txtName.setText("");
		txtCategory.setText("");
		txtQuantity.setText("");
		txtBarcode.setText("");
		txtStartPosition.setText("1");
		txtSellPrice.setText("");
		txtNoOfBarcodes.setText("65");
		productCode = 0;
	}

	private void setProductDetails() {
		Product p = productMap.get(txtName.getText());
		if (p != null) {
			txtCategory.setText(p.getProductCategory());
			txtQuantity.setText(appUtils.getDecimalFormat(p.getQuantity()));
			txtSellPrice.setText(appUtils.getDecimalFormat(p.getSellPrice()));
			if (p.getProductBarCode() == 0) {
				txtBarcode.setText("");
			} else {
				txtBarcode.setText(String.valueOf(p.getProductBarCode()));
			}
		}
	}

	public void getProductsName() {
		entries = new TreeSet<String>();
		productMap = new HashMap<String, Product>();
		for (Product product : productService.getAll()) {
			entries.add(product.getProductName());
			productMap.put(product.getProductName(), product);
		}
	}
}
