package com.billing.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.Expense;
import com.billing.dto.ExpenseReport;
import com.billing.dto.UserDetails;
import com.billing.service.ExpensesService;
import com.billing.service.PrinterService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.IndianCurrencyFormatting;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Callback;

@Controller
public class ExpensesReportController implements TabContent {

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	@Autowired
	ExpensesService expensesService;

	@Autowired
	PrinterService pinterService;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	ObservableList<Expense> expenseList;

	@FXML
	private DatePicker dpFromDate;

	@FXML
	private DatePicker dpToDate;

	@FXML
	private ComboBox<String> cbExpenseCategory;

	@FXML
	private TableView<Expense> tableView;

	@FXML
	private TableColumn<Expense, String> tcExpenseCategory;

	@FXML
	private TableColumn<Expense, String> tcDate;

	@FXML
	private TableColumn<Expense, Double> tcAmount;

	@FXML
	private TableColumn<Expense, String> tcDescription;

	@FXML
	private TextField txtTotalExpenses;

	@FXML
	private TextField txtTotalExpeseAmount;

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
		expenseList.clear();
		List<Expense> exList = expensesService.getExpenses(dpFromDate.getValue().toString(),
				dpToDate.getValue().toString(), cbExpenseCategory.getSelectionModel().getSelectedItem());
		expenseList.addAll(exList);
		return true;
	}

	private void setTableCellFactories() {

		final Callback<TableColumn<Expense, Double>, TableCell<Expense, Double>> callback = new Callback<TableColumn<Expense, Double>, TableCell<Expense, Double>>() {

			@Override
			public TableCell<Expense, Double> call(TableColumn<Expense, Double> param) {
				TableCell<Expense, Double> tableCell = new TableCell<Expense, Double>() {

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

		tcDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate()));

		tcExpenseCategory.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory()));
		tcDescription.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));

		tcAmount.setCellFactory(callback);

		tcDate.getStyleClass().add("character-cell");
		tcDescription.getStyleClass().add("character-cell");
		tcExpenseCategory.getStyleClass().add("character-cell");

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
		expenseList = FXCollections.observableArrayList();
		tableView.setItems(expenseList);
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
		expensesService.fillExpenseTypes(cbExpenseCategory);
		cbExpenseCategory.getSelectionModel().select(0);
		cbExpenseCategory.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				loadData();
			}
		});
		expenseList.addListener(new ListChangeListener<Expense>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends Expense> c) {
				updateTotals();
			}

		});

	}

	@Override
	public boolean saveData() {
		return true;
	}

	private void updateTotals() {

		double totalExpenseAmount = 0;

		for (Expense expense : expenseList) {
			totalExpenseAmount = totalExpenseAmount + expense.getAmount();
		}
		txtTotalExpenses.setText(String.valueOf(expenseList.size()));
		txtTotalExpeseAmount.setText(IndianCurrencyFormatting.applyFormattingWithCurrency(totalExpenseAmount));
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
		ExpenseReport expenseReport = getExpenseReport();
		pinterService.exportPDF(expenseReport, currentStage);

	}

	private ExpenseReport getExpenseReport() {
		ExpenseReport expenseReport = new ExpenseReport();
		expenseReport.setExpenseList(expenseList);
		expenseReport.setFromDate(dpFromDate.getValue().toString());
		expenseReport.setToDate(dpToDate.getValue().toString());
		expenseReport.setCategory(cbExpenseCategory.getSelectionModel().getSelectedItem());
		expenseReport.setTotalExpenaseAmount(txtTotalExpeseAmount.getText());
		return expenseReport;
	}

	@FXML
	void onExportAsExcelClick(MouseEvent event) {
		if (!validateInput()) {
			return;
		}
		ExpenseReport expenseReport = getExpenseReport();
		pinterService.exportExcel(expenseReport, currentStage);
	}

	@Override
	public void closeTab() {
		Tab tab = tabPane.selectionModelProperty().get().selectedItemProperty().get();
		tabPane.getTabs().remove(tab); // close the current tab
	}

	@Override
	public boolean validateInput() {
		if (expenseList.size() == 0) {
			alertHelper.showErrorNotification("No records to export");
			return false;
		}
		return true;
	}

	private DateCell getDateCell(DatePicker datePicker) {
		return appUtils.getDateCell(datePicker, null, LocalDate.now());
	}

}
