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
import javafx.scene.control.ComboBox;
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

	long barcode = 0;

	private HashMap<String, Product> productMap;

	private SortedSet<String> entries;

	@FXML
	private AutoCompleteTextField txtName;

	@FXML
	private TextField txtQuantity;

	@FXML
	private TextField txtCategory;

	@FXML
	private TextField txtSellRate;

	@FXML
	private Button btnPreview;

	@FXML
	private Button btnPrint;

	@FXML
	private Button btnSave;

	@FXML
	private TextField txtStartPosition;

	@FXML
	private TextField txtNoOfBarcodes;

	@FXML
	private TextField txtAmountLabel;

	@FXML
	private ImageView imageView;

	@FXML
	private ComboBox<String> cbBarcodeLabelPaperType;

	@FXML
	private RadioButton rbLblOption1;

	@FXML
	private RadioButton rbLblOption2;

	@Override
	public void initialize() {
		barcode = 0;

		ToggleGroup radioButtonGroupGSTType = new ToggleGroup();
		rbLblOption1.setToggleGroup(radioButtonGroupGSTType);
		rbLblOption2.setToggleGroup(radioButtonGroupGSTType);

		txtCategory.textProperty().addListener((observable, oldValue, newValue) -> {
			btnPreview.setDisable(newValue.isEmpty());
			btnPrint.setDisable(newValue.isEmpty());
			btnSave.setDisable(newValue.isEmpty());
		});
		btnPreview.setDisable(true);
		btnPrint.setDisable(true);
		btnSave.setDisable(true);

		getProductsName();
		txtName.createTextField(entries, () -> setProductDetails());
		populateBarcdeTypes();
		rbLblOption1.setSelected(true);
		rbLblOption2.setVisible(false);
		rbLblOption2.selectedProperty().addListener(e -> populateImageView());
		cbBarcodeLabelPaperType.getSelectionModel().selectedItemProperty().addListener(e -> populateImageView());
		cbBarcodeLabelPaperType.getSelectionModel().select(appUtils.getAppDataValues(AppConstants.BARCODE_LABEL_TYPE));
		txtAmountLabel.setText(appUtils.getAppDataValues(AppConstants.BARCODE_AMOUNT_LABEL));
		
	}

	private void populateBarcdeTypes() {
		cbBarcodeLabelPaperType.getItems().add(AppConstants.A4_65);
		cbBarcodeLabelPaperType.getItems().add(AppConstants.A4_40);
		cbBarcodeLabelPaperType.getItems().add(AppConstants.A4_24);
		cbBarcodeLabelPaperType.getItems().add(AppConstants.TH_5025_1);
		cbBarcodeLabelPaperType.getItems().add(AppConstants.TH_5025_2);
		cbBarcodeLabelPaperType.getItems().add(AppConstants.TH_3825_1);
		cbBarcodeLabelPaperType.getItems().add(AppConstants.TH_3825_2);
	}

	private void populateImageView() {
		rbLblOption2.setVisible(false);
		String imageName = "65_Labels.png";
		String selectedType = cbBarcodeLabelPaperType.getSelectionModel().getSelectedItem();
		if (selectedType.equalsIgnoreCase(AppConstants.A4_65)) {
			rbLblOption1.setSelected(true);
			txtStartPosition.setDisable(false);
			imageName = "65_Labels.png";
			txtStartPosition.setText("1");
			txtNoOfBarcodes.setText("65");
		} else if (selectedType.equalsIgnoreCase(AppConstants.A4_40)) {
			rbLblOption1.setSelected(true);
			txtStartPosition.setDisable(false);
			imageName = "40_Labels.png";
			txtStartPosition.setText("1");
			txtNoOfBarcodes.setText("40");
		} else if (selectedType.equalsIgnoreCase(AppConstants.A4_24)) {
			rbLblOption1.setSelected(true);
			txtStartPosition.setDisable(false);
			imageName = "24_Labels.png";
			txtStartPosition.setText("1");
			txtNoOfBarcodes.setText("24");
		} else if (selectedType.equalsIgnoreCase(AppConstants.TH_5025_1)) {
			rbLblOption2.setVisible(true);
			if (rbLblOption1.isSelected()) {
				imageName = "Single_5025mm.png";
			} else if (rbLblOption2.isSelected()) {
				imageName = "Single_5025mm_S2.png";
			}
			txtStartPosition.setDisable(true);
			txtStartPosition.setText("Not Applicable");
			txtNoOfBarcodes.setText("1");
		} else if (selectedType.equalsIgnoreCase(AppConstants.TH_5025_2)) {
			rbLblOption1.setSelected(true);
			imageName = "Double_5025mm.png";
			txtStartPosition.setDisable(true);
			txtStartPosition.setText("Not Applicable");
			txtNoOfBarcodes.setText("2");
		} else if (selectedType.equalsIgnoreCase(AppConstants.TH_3825_1)) {
			rbLblOption1.setSelected(true);
			imageName = "Single_3825mm.png";
			txtStartPosition.setDisable(true);
			txtStartPosition.setText("Not Applicable");
			txtNoOfBarcodes.setText("1");
		} else if (selectedType.equalsIgnoreCase(AppConstants.TH_3825_2)) {
			rbLblOption1.setSelected(true);
			imageName = "Double_3825mm.png";
			txtStartPosition.setDisable(true);
			txtStartPosition.setText("Not Applicable");
			txtNoOfBarcodes.setText("2");
		}

		imageView.setImage(new Image(this.getClass().getResource("/images/" + imageName).toString()));
		if (!txtName.getText().equals("")) {
			prepareSheet("PREVIEW");
			imageView.setImage(appUtils.getBarcodeImage());
		}
	}

	@FXML
	void onPrint(ActionEvent event) {
		prepareSheet("PRINT");
	}

	@FXML
	void onAmountLabelUpdateCommand(ActionEvent event) {
		appUtils.updateAppData(AppConstants.BARCODE_AMOUNT_LABEL, txtAmountLabel.getText());
		alertHelper.showSuccessNotification("Amount Label updated successfully");
		appUtils.reloadAppData();
	}

	@FXML
	void onPreview(ActionEvent event) {
		prepareSheet("PREVIEW");
		imageView.setImage(appUtils.getBarcodeImage());
	}

	@FXML
	void onSave(ActionEvent event) {
		prepareSheet("SAVE");
	}

	private void prepareSheet(String action) {
		if (txtName.getText().equals("")) {
			alertHelper.showErrorNotification("Please select product");
		} else if (barcode == 0) {
			alertHelper.showErrorNotification("Please generate the barcode for the product");
		} else {

			String JrxmlName = null;
			int startPosition = 1;
			int noOfLabels = 0;
			try {
				Product p = productMap.get(txtName.getText());
				Barcode barcode = new Barcode();
				barcode.setBarcode(String.valueOf(p.getProductBarCode()));
				barcode.setPrice(p.getSellPrice());
				barcode.setMrp(p.getProductMRP());
				barcode.setProductName(txtName.getText());
				barcode.setProductCode(p.getProductCode());
				barcode.setCategoryName(p.getProductCategory());
				barcode.setAmountLabel(txtAmountLabel.getText());
				barcode.setPrintName(p.getPrintName());
				String selectedType = cbBarcodeLabelPaperType.getSelectionModel().getSelectedItem();
				if (selectedType.equalsIgnoreCase(AppConstants.A4_65)) {
					JrxmlName = AppConstants.BARCODE_65_JASPER;
					noOfLabels = 65;
				} else if (selectedType.equalsIgnoreCase(AppConstants.A4_24)) {
					JrxmlName = AppConstants.BARCODE_24_JASPER;
					noOfLabels = 24;
				} else if (selectedType.equalsIgnoreCase(AppConstants.A4_40)) {
					JrxmlName = AppConstants.BARCODE_40_JASPER;
					noOfLabels = 40;
				} else if (selectedType.equalsIgnoreCase(AppConstants.TH_5025_1)) {
					if (rbLblOption1.isSelected()) {
						JrxmlName = AppConstants.BARCODE_THERMAL_SINGLE_5025_JASPER;
					} else if (rbLblOption2.isSelected()) {
						JrxmlName = AppConstants.BARCODE_THERMAL_SINGLE_5025_JASPER_S2;
					}
					noOfLabels = 1;
				} else if (selectedType.equalsIgnoreCase(AppConstants.TH_5025_2)) {
					JrxmlName = AppConstants.BARCODE_THERMAL_DOUBLE_5025_JASPER;
					noOfLabels = 1;
				} else if (selectedType.equalsIgnoreCase(AppConstants.TH_3825_1)) {
					JrxmlName = AppConstants.BARCODE_THERMAL_SINGLE_3825_JASPER;
					noOfLabels = 1;
				} else if (selectedType.equalsIgnoreCase(AppConstants.TH_3825_2)) {
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

				boolean isSuccess = printerService.printBarcodeSheet(barcode, noOfLabels, startPosition, JrxmlName,
						action);
				if (!isSuccess) {
					alertHelper.showErrorNotification("Error occurred while generating barcode sheet");
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

	private void setProductDetails() {
		Product p = productMap.get(txtName.getText());
		if (p != null) {
			txtCategory.setText(p.getProductCategory());
			txtQuantity.setText(appUtils.getDecimalFormat(p.getQuantity()));
			barcode = p.getProductBarCode();
			txtSellRate.setText(appUtils.getDecimalFormat(p.getSellPrice()));
		}
		onPreview(null);
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
