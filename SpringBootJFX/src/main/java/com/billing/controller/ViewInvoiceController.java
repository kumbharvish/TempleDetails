package com.billing.controller;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.BillDetails;
import com.billing.dto.ItemDetails;
import com.billing.dto.Product;
import com.billing.dto.ReturnDetails;
import com.billing.main.AppContext;
import com.billing.service.InvoiceService;
import com.billing.service.PrinterService;
import com.billing.service.SalesReturnService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.IndianCurrencyFormatting;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

@Controller
public class ViewInvoiceController extends AppContext {

	BillDetails bill;

	private static final Logger logger = LoggerFactory.getLogger(CreateInvoiceController.class);

	public Stage currentStage = null;

	ObservableList<Product> productList;

	private ReturnDetails returnDetails;

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	@Autowired
	InvoiceService invoiceService;
	
	@Autowired
	PrinterService printerService;

	ObservableList<ItemDetails> productTableData;

	@Autowired
	SalesReturnService salesReturnService;

	@FXML
	private TextField txtInvoiceNo;

	@FXML
	private TextField txtInvoiceDate;

	@FXML
	private TextField txtCustName;

	@FXML
	private TextField txtCustMobile;

	@FXML
	private TableView<ItemDetails> tableView;
	
	@FXML
	private TableColumn<ItemDetails, String> tcProductCode;

	@FXML
	private TableColumn<ItemDetails, String> tcItemName;

	@FXML
	private TableColumn<ItemDetails, String> tcUnit;

	@FXML
	private TableColumn<ItemDetails, String> tcQuantity;

	@FXML
	private TableColumn<ItemDetails, String> tcRate;

	@FXML
	private TableColumn<ItemDetails, String> tcDiscount;

	@FXML
	private TableColumn<ItemDetails, String> tcDiscountAmount;

	@FXML
	private TableColumn<ItemDetails, String> tcAmount;

	@FXML
	private TableColumn<ItemDetails, String> tcCGSTPercent;

	@FXML
	private TableColumn<ItemDetails, String> tcCGST;

	@FXML
	private TableColumn<ItemDetails, String> tcSGSTPercent;

	@FXML
	private TableColumn<ItemDetails, String> tcSGST;

	@FXML
	private TextField txtNoOfItems;

	@FXML
	private TextField txtTotalQty;

	@FXML
	private TextField txtSubTotal;

	@FXML
	private TextField txtDiscountPercent;

	@FXML
	private TextField txtDiscountAmt;

	@FXML
	private TextField txtPaymentMode;

	@FXML
	private TextField txtGstAmount;

	@FXML
	private TextField txtGstType;

	@FXML
	private TextField txtNetSalesAmount;

	@FXML
	private Button btnViewSalesReturn;

	public void initialize() {

		productTableData = FXCollections.observableArrayList();
		productList = FXCollections.observableArrayList();
		tableView.setItems(productTableData);
		setTableCellFactories();
	}

	private void setTableCellFactories() {
		tcProductCode.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getItemNo())));
		tcItemName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItemName()));
		tcUnit.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUnit()));
		tcQuantity.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getQuantity())));
		tcRate.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getRate())));
		tcAmount.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getAmount())));
		tcDiscountAmount.setCellValueFactory(cellData -> new SimpleStringProperty(
				appUtils.getDecimalFormat(cellData.getValue().getDiscountAmount())));
		tcCGST.setCellValueFactory(cellData -> new SimpleStringProperty(
				appUtils.getDecimalFormat(cellData.getValue().getGstDetails().getCgst())));
		tcSGST.setCellValueFactory(cellData -> new SimpleStringProperty(
				appUtils.getDecimalFormat(cellData.getValue().getGstDetails().getSgst())));
		tcCGSTPercent.setCellValueFactory(cellData -> new SimpleStringProperty(
				String.valueOf(cellData.getValue().getGstDetails().getCgstPercent())));
		tcSGSTPercent.setCellValueFactory(cellData -> new SimpleStringProperty(
				String.valueOf(cellData.getValue().getGstDetails().getSgstPercent())));

		tcDiscount.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getDiscountPercent())));

		tcItemName.getStyleClass().add("character-cell");
		tcQuantity.getStyleClass().add("numeric-cell");
		tcUnit.getStyleClass().add("character-cell");
		tcRate.getStyleClass().add("numeric-cell");
		tcAmount.getStyleClass().add("numeric-cell");
		tcDiscountAmount.getStyleClass().add("numeric-cell");
		tcCGST.getStyleClass().add("numeric-cell");
		tcDiscount.getStyleClass().add("numeric-cell");
		tcCGSTPercent.getStyleClass().add("numeric-cell");
		tcSGSTPercent.getStyleClass().add("numeric-cell");
		tcSGST.getStyleClass().add("numeric-cell");
		tcProductCode.getStyleClass().add("character-cell");
	}

	public void loadData() {
		currentStage = (Stage) txtCustMobile.getScene().getWindow();

		List<ItemDetails> itemList = invoiceService.getItemList(bill);
		bill.setItemDetails(itemList);

		for (ItemDetails item : bill.getItemDetails()) {
			Product p = new Product();
			p.setGstDetails(item.getGstDetails());
			productTableData.add(item);
			productList.add(p);
		}

		txtInvoiceNo.setText(String.valueOf(bill.getBillNumber()));
		txtInvoiceDate.setText(appUtils.getFormattedDateWithTime(bill.getTimestamp()));
		txtCustMobile.setText(String.valueOf(bill.getCustomerMobileNo()));
		txtCustName.setText(bill.getCustomerName());
		txtNoOfItems.setText(String.valueOf(bill.getNoOfItems()));
		txtTotalQty.setText(String.valueOf(bill.getTotalQuantity()));
		txtSubTotal.setText(IndianCurrencyFormatting.applyFormatting(bill.getTotalAmount()));
		txtDiscountAmt.setText(IndianCurrencyFormatting.applyFormatting(bill.getDiscountAmt()));
		txtDiscountPercent.setText(String.valueOf(bill.getDiscount()));
		txtPaymentMode.setText(bill.getPaymentMode());
		txtGstAmount.setText(IndianCurrencyFormatting.applyFormatting(bill.getGstAmount()));
		txtGstType.setText(bill.getGstType());
		txtNetSalesAmount.setText(IndianCurrencyFormatting.applyFormattingWithCurrency(bill.getNetSalesAmt()));

		returnDetails = salesReturnService.getReturnDetails(bill.getBillNumber());
		if (returnDetails == null) {
			btnViewSalesReturn.setVisible(false);
		}
	}

	@FXML
	void onViewGSTDetailsAction(ActionEvent event) {
		getGSTDetailsPopUp();
	}

	@FXML
	void onViewSalesReturnAction(ActionEvent event) {
		getViewSalesReturnPopUp(returnDetails);
	}
	
	@FXML
	void onPrintAction(ActionEvent event) {
		printerService.printInvoice(bill);

	}

	private void getViewSalesReturnPopUp(ReturnDetails retunDtls) {
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setControllerFactory(springContext::getBean);
		fxmlLoader.setLocation(this.getClass().getResource("/com/billing/gui/ViewSalesReturn.fxml"));

		Parent rootPane = null;
		try {
			rootPane = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("getViewSalesReturnPopUp Error in loading the view file :", e);
			alertHelper.beep();

			alertHelper.showErrorAlert(currentStage, "Error Occurred", "Error in creating user interface",
					"An error occurred in creating user interface " + "for the selected command");

			return;
		}

		final Scene scene = new Scene(rootPane);
		final ViewSalesReturnController controller = (ViewSalesReturnController) fxmlLoader.getController();
		controller.returnDetails = retunDtls;
		final Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(currentStage);
		stage.setUserData(controller);
		stage.getIcons().add(new Image("/images/shop32X32.png"));
		stage.setScene(scene);
		stage.setTitle("View Return");
		controller.loadData();
		stage.showAndWait();
	}

	private void getGSTDetailsPopUp() {
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setControllerFactory(springContext::getBean);
		fxmlLoader.setLocation(this.getClass().getResource("/com/billing/gui/ViewGSTDetails.fxml"));

		Parent rootPane = null;
		try {
			rootPane = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("getGSTDetailsPopUp Error in loading the view file :", e);
			alertHelper.beep();

			alertHelper.showErrorAlert(null, "Error Occurred", "Error in creating user interface",
					"An error occurred in creating user interface " + "for the selected command");

			return;
		}

		final Scene scene = new Scene(rootPane);
		final GSTDetailsController controller = (GSTDetailsController) fxmlLoader.getController();
		controller.productList = productList;
		final Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(currentStage);
		stage.setUserData(controller);
		stage.getIcons().add(new Image("/images/shop32X32.png"));
		stage.setScene(scene);
		stage.setTitle("GST Details");
		controller.loadData();
		stage.showAndWait();
	}

}
