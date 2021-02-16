package com.billing.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.ReturnDetails;
import com.billing.dto.SalesReturnReport;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.PrinterService;
import com.billing.service.SalesReturnService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.IndianCurrencyFormatting;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

@Controller
public class SalesReturnReportController extends AppContext implements TabContent {

	private static final Logger logger = LoggerFactory.getLogger(SalesReturnReportController.class);

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	@Autowired
	SalesReturnService salesReturnService;

	@Autowired
	PrinterService pinterService;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	ObservableList<ReturnDetails> returnsList;

	@FXML
	private DatePicker dpFromDate;

	@FXML
	private DatePicker dpToDate;

	@FXML
	private TableView<ReturnDetails> tableView;

	@FXML
	private TableColumn<ReturnDetails, String> tcReturnNo;

	@FXML
	private TableColumn<ReturnDetails, String> tcReturnDate;

	@FXML
	private TableColumn<ReturnDetails, String> tcInvoiceNo;

	@FXML
	private TableColumn<ReturnDetails, String> tcCustomer;

	@FXML
	private TableColumn<ReturnDetails, String> tcNoOfItems;

	@FXML
	private TableColumn<ReturnDetails, Double> tcQuantity;

	@FXML
	private TableColumn<ReturnDetails, String> tcPaymentMode;

	@FXML
	private TableColumn<ReturnDetails, Double> tcReturnAmount;

	@FXML
	private TextField txtTotalReturnCount;

	@FXML
	private TextField txtTotalReturnAmount;

	@Override
	public boolean shouldClose() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void putFocusOnNode() {
		dpFromDate.requestFocus();
	}

	@Override
	public boolean loadData() {
		returnsList.clear();
		List<ReturnDetails> returnList = salesReturnService.getReturnDetails(dpFromDate.getValue().toString(),
				dpToDate.getValue().toString());
		returnsList.addAll(returnList);
		return true;
	}

	private void setTableCellFactories() {

		final Callback<TableColumn<ReturnDetails, Double>, TableCell<ReturnDetails, Double>> callback = new Callback<TableColumn<ReturnDetails, Double>, TableCell<ReturnDetails, Double>>() {

			@Override
			public TableCell<ReturnDetails, Double> call(TableColumn<ReturnDetails, Double> param) {
				TableCell<ReturnDetails, Double> tableCell = new TableCell<ReturnDetails, Double>() {

					@Override
					protected void updateItem(Double item, boolean empty) {
						super.updateItem(item, empty);
						if (empty) {
							super.setText(null);
						} else {
							super.setText(IndianCurrencyFormatting.applyFormatting(item));
						}
					}

				};
				tableCell.getStyleClass().add("numeric-cell");
				return tableCell;
			}
		};

		tcReturnNo.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getReturnNumber())));
		tcInvoiceNo.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getInvoiceNumber())));
		tcReturnDate.setCellValueFactory(cellData -> new SimpleStringProperty(
				appUtils.getFormattedDateWithTime(cellData.getValue().getTimestamp())));
		tcCustomer.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomerName()));
		tcNoOfItems.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getNoOfItems())));
		tcPaymentMode.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPaymentMode()));

		tcQuantity.setCellFactory(callback);
		tcReturnAmount.setCellFactory(callback);

		tcReturnDate.getStyleClass().add("character-cell");
		tcCustomer.getStyleClass().add("character-cell");
		tcInvoiceNo.getStyleClass().add("numeric-cell");
		tcReturnNo.getStyleClass().add("numeric-cell");
		tcNoOfItems.getStyleClass().add("numeric-cell");
		tcPaymentMode.getStyleClass().add("character-cell");
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
	public void initialize() {
		setTableCellFactories();
		returnsList = FXCollections.observableArrayList();
		tableView.setItems(returnsList);
		dpFromDate.setValue(LocalDate.now());
		dpToDate.setValue(LocalDate.now());
		dpFromDate.setDayCellFactory(this::getDateCell);
		dpFromDate.valueProperty().addListener((observable, oldDate, newDate) -> {
			LocalDate today = LocalDate.now();
			if (newDate == null || newDate.isAfter(today)) {
				dpFromDate.setValue(today);
			}
			loadData();
		});
		dpToDate.setDayCellFactory(this::getDateCell);
		dpToDate.valueProperty().addListener((observable, oldDate, newDate) -> {
			LocalDate today = LocalDate.now();
			if (newDate == null || newDate.isAfter(today)) {
				dpToDate.setValue(today);
			}
			loadData();
		});

		returnsList.addListener(new ListChangeListener<ReturnDetails>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends ReturnDetails> c) {
				updateTotals();
			}

		});

		tableView.setOnMouseClicked((MouseEvent event) -> {
			if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
				// Show View Invoice Popup
				if (null != tableView.getSelectionModel().getSelectedItem()) {
					getViewReturnPopup(tableView.getSelectionModel().getSelectedItem());
				}
			}
		});
	}

	@Override
	public boolean saveData() {
		return true;
	}

	private void updateTotals() {

		double totalReturnAmount = 0;

		for (ReturnDetails rd : returnsList) {
			totalReturnAmount = totalReturnAmount + rd.getTotalReturnAmount();
		}
		txtTotalReturnCount.setText(String.valueOf(returnsList.size()));
		txtTotalReturnAmount.setText(IndianCurrencyFormatting.applyFormattingWithCurrency(totalReturnAmount));
	}

	@Override
	public void invalidated(Observable observable) {

	}

	@FXML
	void onCloseAction(ActionEvent event) {
		closeTab();
	}

	@FXML
	void onExportAsPDFClick(MouseEvent event) {
		if (!validateInput()) {
			return;
		}
		SalesReturnReport salesReturnReport = getSalesReturnReport();
		pinterService.exportPDF(salesReturnReport, currentStage);

	}

	private SalesReturnReport getSalesReturnReport() {
		SalesReturnReport salesReturnReport = new SalesReturnReport();
		salesReturnReport.setReturnList(returnsList);
		salesReturnReport.setFromDate(dpFromDate.getValue().toString());
		salesReturnReport.setToDate(dpToDate.getValue().toString());
		salesReturnReport.setTotalReturnAmount(
				Double.valueOf(IndianCurrencyFormatting.removeFormattingWithCurrency(txtTotalReturnAmount.getText())));
		return salesReturnReport;
	}

	@FXML
	void onExportAsExcelClick(MouseEvent event) {
		if (!validateInput()) {
			return;
		}
		SalesReturnReport salesReturnReport = getSalesReturnReport();
		pinterService.exportExcel(salesReturnReport, currentStage);
	}

	@Override
	public void closeTab() {
		Tab tab = tabPane.selectionModelProperty().get().selectedItemProperty().get();
		tabPane.getTabs().remove(tab); // close the current tab
	}

	@Override
	public boolean validateInput() {
		if (returnsList.size() == 0) {
			alertHelper.showErrorNotification("No records to export");
			return false;
		}
		return true;
	}

	private DateCell getDateCell(DatePicker datePicker) {
		return appUtils.getDateCell(datePicker, null, LocalDate.now());
	}

	private void getViewReturnPopup(ReturnDetails returnDetails) {
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setControllerFactory(springContext::getBean);
		fxmlLoader.setLocation(this.getClass().getResource("/com/billing/gui/ViewSalesReturn.fxml"));

		Parent rootPane = null;
		try {
			rootPane = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("getViewReturnPopup Error in loading the view file :", e);
			alertHelper.beep();

			alertHelper.showErrorAlert(currentStage, "Error Occurred", "Error in creating user interface",
					"An error occurred in creating user interface " + "for the selected command");

			return;
		}

		final Scene scene = new Scene(rootPane);
		final ViewSalesReturnController controller = (ViewSalesReturnController) fxmlLoader.getController();
		controller.returnDetails = returnDetails;
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
}
