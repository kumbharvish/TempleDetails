package com.billing.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.CashReport;
import com.billing.dto.Customer;
import com.billing.dto.StatusDTO;
import com.billing.dto.UserDetails;
import com.billing.service.ReportService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.IndianCurrencyFormatting;
import com.billing.utils.TabContent;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
@Controller
public class CashReportController implements TabContent {

	@Autowired
	ReportService reportService;

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	@FXML
	private Button btnUpdateOpeningCash;

	@FXML
	private DatePicker datePicker;

	@FXML
	private Label lblCrOpeningCashAmt;

	@FXML
	private Label lblCrTotalSalesAmt;

	@FXML
	private Label lblDrTotalSalesReturnAmt;

	@FXML
	private Label lblDrTotalExpenseAmt;

	@FXML
	private Label lblCrTotalCustSettleAmt;

	@FXML
	private Label lblTotalCreditAmount;

	@FXML
	private Label lblTotalDebitAmount;

	@FXML
	private Label lblTotalCashCounterAmt;

	@FXML
	private Label lblCb_1;

	@FXML
	private Label lblCb_2;

	@FXML
	private Label lblCb_3;

	@FXML
	private Label lblCb_4;

	@FXML
	private Label lblCb_5;

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	@Override
	public boolean shouldClose() {
		return true;
	}

	@Override
	public void putFocusOnNode() {
		datePicker.requestFocus();
	}

	@Override
	public boolean loadData() {
		CashReport totalSalesAmount = new CashReport();
		CashReport totalSalesReturnAmount = new CashReport();
		CashReport totalCustSettleAmount = new CashReport();
		CashReport openingCashBalance = new CashReport();
		CashReport totalAmount = new CashReport();
		CashReport totalExpense = new CashReport();

		for (CashReport cash : reportService.getCashCounterDetails(datePicker.getValue().toString(),
				datePicker.getValue().toString())) {
			if (cash.getDescription().equals("TOTAL_SALES_AMOUNT")) {
				totalSalesAmount = cash;
			}
			if (cash.getDescription().equals("TOTAL_SALES_RETURN_AMOUNT")) {
				totalSalesReturnAmount = cash;
			}
			if (cash.getDescription().equals("TOTAL_CUST_SETTLEMENT_AMOUNT")) {
				totalCustSettleAmount = cash;
			}
			if (cash.getDescription().equals("TOTAL")) {
				totalAmount = cash;
			}
			if (cash.getDescription().equals("OPENING_CASH")) {
				openingCashBalance = cash;
			}
			if (cash.getDescription().equals("TOTAL_EXPENSE_AMOUNT")) {
				totalExpense = cash;
			}

		}

		lblCrOpeningCashAmt.setText(IndianCurrencyFormatting.applyFormatting(openingCashBalance.getCreditAmount()));
		lblCb_1.setText(IndianCurrencyFormatting.applyFormatting(openingCashBalance.getClosingBalance()));
		lblCrTotalSalesAmt.setText(IndianCurrencyFormatting.applyFormatting(totalSalesAmount.getCreditAmount()));
		lblCb_2.setText(IndianCurrencyFormatting.applyFormatting(totalSalesAmount.getClosingBalance()));
		lblDrTotalSalesReturnAmt
				.setText(IndianCurrencyFormatting.applyFormatting(totalSalesReturnAmount.getDebitAmount()));
		lblCb_3.setText(IndianCurrencyFormatting.applyFormatting(totalSalesReturnAmount.getClosingBalance()));
		lblDrTotalExpenseAmt.setText(IndianCurrencyFormatting.applyFormatting(totalExpense.getDebitAmount()));
		lblCb_4.setText(IndianCurrencyFormatting.applyFormatting(totalExpense.getClosingBalance()));

		lblCrTotalCustSettleAmt
				.setText(IndianCurrencyFormatting.applyFormatting(totalCustSettleAmount.getCreditAmount()));
		lblCb_5.setText(IndianCurrencyFormatting.applyFormatting(totalCustSettleAmount.getClosingBalance()));
		lblTotalCreditAmount.setText(IndianCurrencyFormatting.applyFormatting(totalAmount.getCreditAmount()));
		lblTotalDebitAmount.setText(IndianCurrencyFormatting.applyFormatting(totalAmount.getDebitAmount()));
		lblTotalCashCounterAmt
				.setText(IndianCurrencyFormatting.applyFormattingWithCurrency(totalAmount.getClosingBalance()));

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
		datePicker.setValue(LocalDate.now());
		datePicker.setDayCellFactory(this::getDateCell);
		datePicker.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				if (datePicker.getValue().toString().equals(LocalDate.now().toString())) {
					btnUpdateOpeningCash.setDisable(false);
				} else {
					btnUpdateOpeningCash.setDisable(true);
				}
				loadData();
			}
		});

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
		return valid;
	}

	@FXML
	void onCloseCommand(ActionEvent event) {
		closeTab();
	}

	@Override
	public void setUserDetails(UserDetails user) {
		// TODO Auto-generated method stub

	}

	@FXML
	void onUpdateOpeningCashCommand(ActionEvent event) {

		Double openCash = reportService.getOpeningCash(appUtils.getTodaysDateForDB());

		Dialog<String> dialog = new Dialog<>();
		dialog.setTitle("Update Opening Cash");

		final String styleSheetPath = "/css/alertDialog.css";
		final DialogPane dialogPane = dialog.getDialogPane();
		dialogPane.getStylesheets().add(AlertHelper.class.getResource(styleSheetPath).toExternalForm());

		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(this.getClass().getResource("/images/shop32X32.png").toString()));

		// Set the button types.
		ButtonType updateButtonType = new ButtonType("Update", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 100, 10, 10));
		TextField openingCashAmt = new TextField();
		openingCashAmt.setPrefColumnCount(10);
		openingCashAmt.setDisable(true);
		openingCashAmt.getStyleClass().add("readOnlyField");

		Label lbl = new Label("Opening Cash Amount :");
		lbl.getStyleClass().add("nodeLabel");

		grid.add(lbl, 0, 0);
		grid.add(openingCashAmt, 1, 0);

		TextField newOpeningCashAmt = new TextField();
		newOpeningCashAmt.textProperty().addListener(appUtils.getForceDecimalNumberListner());
		newOpeningCashAmt.setPrefColumnCount(10);

		Label lbl2 = new Label("New Opening Cash Amount :");
		lbl2.getStyleClass().add("nodeLabel");

		grid.add(lbl2, 0, 1);
		grid.add(newOpeningCashAmt, 1, 1);

		if (openCash != null) {
			openingCashAmt.setText(appUtils.getDecimalFormat(openCash));
		} else {
			openingCashAmt.setText("0.00");
			reportService.addOpeningCash(0);
		}

		Node validateButton = dialog.getDialogPane().lookupButton(updateButtonType);
		validateButton.setDisable(true);

		newOpeningCashAmt.textProperty().addListener((observable, oldValue, newValue) -> {
			validateButton.setDisable(newValue.trim().isEmpty());
		});

		dialog.getDialogPane().setContent(grid);

		Platform.runLater(() -> newOpeningCashAmt.requestFocus());

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == updateButtonType) {
				return newOpeningCashAmt.getText();
			}
			return null;
		});

		Optional<String> result = dialog.showAndWait();
		result.ifPresent(key -> {
			try {
				if (key != null && key != "") {
					double newopenCash = Double.valueOf(key);
					StatusDTO status = reportService.updateOpeningCash(newopenCash, appUtils.getTodaysDateForDB());
					if (status.getStatusCode() == 0) {
						loadData();
					}
				} else {
					alertHelper.showErrorNotification("Please enter new opening cash amount");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	@FXML
	void onViewSettlementDetailsCommand(ActionEvent event) {
		List<Customer> custList = reportService.getSettledCustomerList(datePicker.getValue().toString());
		Dialog<String> dialog = new Dialog<>();
		dialog.setTitle("Customer Settlements");

		final String styleSheetPath = "/css/alertDialog.css";
		final DialogPane dialogPane = dialog.getDialogPane();
		dialogPane.getStylesheets().add(AlertHelper.class.getResource(styleSheetPath).toExternalForm());
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);

		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(this.getClass().getResource("/images/shop32X32.png").toString()));
		TableView<Customer> tableView = new TableView<Customer>();

		TableColumn<Customer, String> tcName = new TableColumn<>("Customer Name");
		tcName.setPrefWidth(200);
		TableColumn<Customer, String> tcAmount = new TableColumn<>("Amounts");
		tcAmount.setPrefWidth(150);
		tableView.getColumns().add(tcName);
		tableView.getColumns().add(tcAmount);
		Label placeholderText = new Label("<No customer settlements found>");
		placeholderText.setId("placeholderText");
		tableView.setPlaceholder(placeholderText);
		tcName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustName()));
		tcAmount.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getAmount())));
		// Set CSS
		tcName.getStyleClass().add("character-cell");
		tcAmount.getStyleClass().add("numeric-cell");
		ObservableList<Customer> customerTableData = FXCollections.observableArrayList();
		customerTableData.addAll(custList);
		tableView.setItems(customerTableData);
		dialog.getDialogPane().setContent(tableView);
		Optional<String> result = dialog.showAndWait();
	}

	private DateCell getDateCell(DatePicker datePicker) {
		return appUtils.getDateCell(datePicker, null, LocalDate.now());
	}

}