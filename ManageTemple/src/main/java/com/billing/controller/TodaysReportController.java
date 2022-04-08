package com.billing.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.AccountBalanceHistory;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.TransactionsService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.IndianCurrencyFormatting;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

@Controller
public class TodaysReportController extends AppContext implements TabContent {

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	TransactionsService transactionsService;

	@Autowired
	AppUtils appUtils;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	@FXML
	private TextField txtExpenseAmount;

	@FXML
	private TextField txtIncomeAmount;

	@FXML
	private TextField txtTotal;

	@FXML
	private TextField txtAccountBalance;

	@FXML
	private TableView<AccountBalanceHistory> tableView;

	@FXML
	private TableColumn<AccountBalanceHistory, String> tcPaymentDate;

	@FXML
	private TableColumn<AccountBalanceHistory, String> tcNarration;

	@FXML
	private TableColumn<AccountBalanceHistory, String> tcCredit;

	@FXML
	private TableColumn<AccountBalanceHistory, String> tcDebit;

	@FXML
	private TableColumn<AccountBalanceHistory, String> tcClosingBalance;

	@FXML
	private DatePicker fromDate;

	@FXML
	private DatePicker toDate;

	double creditAmount = 0.0;
	double debitAmount = 0.0;

	@Override
	public void initialize() {
		setTableCellFactories();
		fromDate.setValue(LocalDate.now());
		toDate.setValue(LocalDate.now());
		txtAccountBalance.setText(IndianCurrencyFormatting
				.applyFormattingWithCurrency(transactionsService.getAccountDetails().getBalance()));
		fromDate.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				loadData();
			}
		});
		toDate.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				loadData();
			}
		});
		appUtils.setDateConvertor(fromDate);
		appUtils.setDateConvertor(toDate);
	}
	
	private DateCell getDateCell(DatePicker datePicker) {
		return appUtils.getDateCell(datePicker, null, LocalDate.now());
	}

	private void setTableCellFactories() {
		// Table Column Mapping
		tcPaymentDate.setCellValueFactory(cellData -> new SimpleStringProperty(
				appUtils.getFormattedDateWithTime(cellData.getValue().getTimestamp())));
		tcNarration.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
		tcCredit.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCreditAmount() != 0
				? appUtils.getDecimalFormat(cellData.getValue().getCreditAmount())
				: ""));
		tcDebit.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDebitAmount() != 0
				? appUtils.getDecimalFormat(cellData.getValue().getDebitAmount())
				: ""));
		tcClosingBalance.setCellValueFactory(cellData -> new SimpleStringProperty(
				appUtils.getDecimalFormat(cellData.getValue().getClosingBalance())));
		// Set CSS
		tcPaymentDate.getStyleClass().add("character-cell");
		tcNarration.getStyleClass().add("character-cell");
		tcCredit.getStyleClass().add("numeric-cell");
		tcDebit.getStyleClass().add("numeric-cell");
		tcClosingBalance.getStyleClass().add("numeric-cell");
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
		fromDate.requestFocus();
	}

	@Override
	public boolean loadData() {
		creditAmount = 0.0;
		debitAmount = 0.0;
		List<AccountBalanceHistory> list = transactionsService.getAccountBalanceHistory(fromDate.getValue().toString(),
				toDate.getValue().toString());
		ObservableList<AccountBalanceHistory> tableData = FXCollections.observableArrayList();
		tableData.addAll(list);
		tableView.setItems(tableData);
		for (AccountBalanceHistory acct : list) {
			creditAmount = creditAmount + acct.getCreditAmount();
			debitAmount = debitAmount + acct.getDebitAmount();
		}
		txtExpenseAmount.setText(IndianCurrencyFormatting.applyFormattingWithCurrency(debitAmount));
		txtIncomeAmount.setText(IndianCurrencyFormatting.applyFormattingWithCurrency(creditAmount));
		txtTotal.setText(IndianCurrencyFormatting.applyFormattingWithCurrency(creditAmount - debitAmount));

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
}
