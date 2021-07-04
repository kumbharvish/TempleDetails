package com.billing.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.constants.AppConstants;
import com.billing.dto.PrintTemplate;
import com.billing.dto.StatusDTO;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.PrinterService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

@Controller
public class InvoiceTemplatesController extends AppContext implements TabContent {

	private static final Logger logger = LoggerFactory.getLogger(InvoiceTemplatesController.class);

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	@Autowired
	PrinterService printerService;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	@FXML
	private RadioButton rbThermalCashInvoice;

	@FXML
	private RadioButton rbThermalTaxInvoice;

	@FXML
	private RadioButton rbA4TaxInvoice1;

	@FXML
	private RadioButton rbA4TaxInvoice2;

	@FXML
	private RadioButton rbA4TaxInvoice3;

	@FXML
	private RadioButton rbA4TaxInvoice4;
	
	@FXML
	private RadioButton rbA4TaxInvoice5;

	@FXML
	private Button btnSetAsDefault;

	@FXML
	private ImageView imageView;

	private String templateName;

	@Override
	public void initialize() {
		ToggleGroup radioButtonGroup = new ToggleGroup();
		rbA4TaxInvoice1.setToggleGroup(radioButtonGroup);
		rbA4TaxInvoice2.setToggleGroup(radioButtonGroup);
		rbA4TaxInvoice3.setToggleGroup(radioButtonGroup);
		rbA4TaxInvoice4.setToggleGroup(radioButtonGroup);
		rbA4TaxInvoice5.setToggleGroup(radioButtonGroup);
		rbThermalTaxInvoice.setToggleGroup(radioButtonGroup);
		rbThermalCashInvoice.setToggleGroup(radioButtonGroup);
		// populateImageView
		rbA4TaxInvoice1.setOnAction(e -> populateImageView());
		rbA4TaxInvoice2.setOnAction(e -> populateImageView());
		rbA4TaxInvoice3.setOnAction(e -> populateImageView());
		rbA4TaxInvoice4.setOnAction(e -> populateImageView());
		rbA4TaxInvoice5.setOnAction(e -> populateImageView());
		rbThermalTaxInvoice.setOnAction(e -> populateImageView());
		rbThermalCashInvoice.setOnAction(e -> populateImageView());

		rbA4TaxInvoice1.selectedProperty().addListener(this::invalidated);
		rbA4TaxInvoice2.selectedProperty().addListener(this::invalidated);
		rbA4TaxInvoice3.selectedProperty().addListener(this::invalidated);
		rbA4TaxInvoice4.selectedProperty().addListener(this::invalidated);
		rbA4TaxInvoice5.selectedProperty().addListener(this::invalidated);
		rbThermalTaxInvoice.selectedProperty().addListener(this::invalidated);
		rbThermalCashInvoice.selectedProperty().addListener(this::invalidated);

		btnSetAsDefault.disableProperty().bind(isDirty.not());
	}

	private void populateImageView() {
		String imageName = "65_Labels.png";
		if (rbThermalCashInvoice.isSelected()) {
			imageName = "Invoice_Template_Thermal_Cash.png";
			templateName = AppConstants.THERMAL_CASH_INVOICE;
		} else if (rbThermalTaxInvoice.isSelected()) {
			imageName = "Invoice_Template_Thermal_Tax.png";
			templateName = AppConstants.THERMAL_TAX_INVOICE;
		} else if (rbA4TaxInvoice4.isSelected()) {
			imageName = "Invoice_Template_A4_Invoice_Tax_4.png";
			templateName = AppConstants.A4_TAX_INVOICE_4;
		} else if (rbA4TaxInvoice3.isSelected()) {
			imageName = "Invoice_Template_A4_Invoice_Tax_3.png";
			templateName = AppConstants.A4_TAX_INVOICE_3;

		} else if (rbA4TaxInvoice2.isSelected()) {
			imageName = "Invoice_Template_A4_Invoice_Tax_2.png";
			templateName = AppConstants.A4_TAX_INVOICE_2;

		} else if (rbA4TaxInvoice1.isSelected()) {
			imageName = "Invoice_Template_A4_Invoice_Tax_1.png";
			templateName = AppConstants.A4_TAX_INVOICE_1;

		}else if (rbA4TaxInvoice5.isSelected()) {
			imageName = "Invoice_Template_A4_Invoice_Tax_5.png";
			templateName = AppConstants.A4_TAX_INVOICE_5;

		}

		imageView.setImage(new Image(this.getClass().getResource("/images/InvoiceTemplates/" + imageName).toString()));
	}

	@FXML
	void onCloseAction(ActionEvent event) {
		if (shouldClose()) {
			closeTab();
		}
	}

	@FXML
	void onSetAsDefaultAction(ActionEvent event) {
		if (!validateInput()) {
			return;
		}

		boolean result = saveData();

		if (result) {
			closeTab();
		}
	}

	@Override
	public boolean shouldClose() {
		if (isDirty.get()) {
			ButtonType response = appUtils.shouldSaveUnsavedData(currentStage);
			if (response == ButtonType.CANCEL) {
				return false;
			}

			if (response == ButtonType.YES) {
				if (!validateInput()) {
					return false;
				} else {
					saveData();
				}

			}

		}
		return true;
	}

	@Override
	public void putFocusOnNode() {
	}

	@Override
	public boolean loadData() {
		PrintTemplate printTemplate = printerService.getDefaultPrintTemplate();
		if (null == printTemplate) {
			alertHelper.showErrorNotification("Please set defualt print template");
		} else {
			String templateName = printTemplate.getName();
			switch (templateName) {
			case AppConstants.THERMAL_CASH_INVOICE:
				rbThermalCashInvoice.setSelected(true);
				break;
			case AppConstants.THERMAL_TAX_INVOICE:
				rbThermalTaxInvoice.setSelected(true);
				break;
			case AppConstants.A4_TAX_INVOICE_1:
				rbA4TaxInvoice1.setSelected(true);
				break;
			case AppConstants.A4_TAX_INVOICE_2:
				rbA4TaxInvoice2.setSelected(true);
				break;
			case AppConstants.A4_TAX_INVOICE_3:
				rbA4TaxInvoice3.setSelected(true);
				break;
			case AppConstants.A4_TAX_INVOICE_4:
				rbA4TaxInvoice4.setSelected(true);
				break;
			case AppConstants.A4_TAX_INVOICE_5:
				rbA4TaxInvoice5.setSelected(true);
				break;
			default:
				rbThermalCashInvoice.setSelected(true);
			}
		}
		isDirty.set(false);
		populateImageView();
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
		StatusDTO status = printerService.updateDefaultInvoiceTemplate(templateName);
		if (status.getStatusCode() == 0) {
			alertHelper.showSuccessNotification("Invoice template set to default successfully");
			return true;
		}
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
		return valid;
	}

}
