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
public class ViewSalesReturnController extends AppContext {

	ReturnDetails returnDetails;

	private static final Logger logger = LoggerFactory.getLogger(CreateInvoiceController.class);

	public Stage currentStage = null;

	ObservableList<Product> productList;

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	ObservableList<ItemDetails> productTableData;

	@Autowired
	SalesReturnService salesReturnService;

	@FXML
	private TextField txtReturnNo;

	@FXML
	private TextField txtReturnDate;

	@FXML
	private TextField txtInvoiceNo;

	@FXML
	private TextField txtInvoiceDate;

	@FXML
	private TextField txtComments;

	@FXML
	private TextField txtCustomer;

	@FXML
	private TableView<ItemDetails> tableView;

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
	private TextField txtTotalReturnAmount;

	public void initialize() {

		productTableData = FXCollections.observableArrayList();
		productList = FXCollections.observableArrayList();
		tableView.setItems(productTableData);
		setTableCellFactories();
	}

	private void setTableCellFactories() {
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
	}

	public void loadData() {
		currentStage = (Stage) txtCustomer.getScene().getWindow();

		List<ItemDetails> itemList = salesReturnService.getReturnedItemList(returnDetails.getReturnNumber());
		returnDetails.setItemDetails(itemList);

		for (ItemDetails item : returnDetails.getItemDetails()) {
			Product p = new Product();
			p.setGstDetails(item.getGstDetails());
			productTableData.add(item);
			productList.add(p);
		}
		
		txtComments.setText(returnDetails.getComments());
		txtInvoiceNo.setText(String.valueOf(returnDetails.getInvoiceNumber()));
		txtInvoiceDate.setText(appUtils.getFormattedDateWithTime(returnDetails.getInvoiceDate()));
		txtReturnNo.setText(String.valueOf(returnDetails.getReturnNumber()));
		txtReturnDate.setText(appUtils.getFormattedDateWithTime(returnDetails.getTimestamp()));
		txtCustomer.setText(String.valueOf(returnDetails.getCustomerMobileNo())+" : "+String.valueOf(returnDetails.getCustomerName()));
		txtNoOfItems.setText(String.valueOf(returnDetails.getNoOfItems()));
		txtTotalQty.setText(String.valueOf(returnDetails.getTotalQuantity()));
		txtSubTotal.setText(IndianCurrencyFormatting.applyFormatting(returnDetails.getSubTotal()));
		txtDiscountAmt.setText(IndianCurrencyFormatting.applyFormatting(returnDetails.getDiscountAmount()));
		txtDiscountPercent.setText(String.valueOf(returnDetails.getDiscount()));
		txtPaymentMode.setText(returnDetails.getPaymentMode());
		txtGstAmount.setText(IndianCurrencyFormatting.applyFormatting(returnDetails.getGstAmount()));
		txtGstType.setText(returnDetails.getGstType());
		txtTotalReturnAmount
				.setText(IndianCurrencyFormatting.applyFormattingWithCurrency(returnDetails.getTotalReturnAmount()));
	}

	@FXML
	void onViewGSTDetailsAction(ActionEvent event) {
		getGSTDetailsPopUp();
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
