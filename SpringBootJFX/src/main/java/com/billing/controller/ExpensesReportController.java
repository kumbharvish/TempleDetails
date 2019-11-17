package com.billing.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.Expense;
import com.billing.dto.ExpenseReport;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.ExpensesService;
import com.billing.service.PrinterService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.IndianCurrencyFormatting;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

@Controller
public class ExpensesReportController extends AppContext implements TabContent {

	private static final Logger logger = LoggerFactory.getLogger(ExpensesReportController.class);

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

		tcDate.setCellValueFactory(
				cellData -> new SimpleStringProperty(cellData.getValue().getDate()));

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

		tableView.setOnMouseClicked((MouseEvent event) -> {
			if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
				// Show View Invoice Popup
				if (null != tableView.getSelectionModel().getSelectedItem()) {
					getViewExpensePopup(tableView.getSelectionModel().getSelectedItem());
				}
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

	private void getViewExpensePopup(Expense expense) {
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setControllerFactory(springContext::getBean);
		fxmlLoader.setLocation(this.getClass().getResource("/com/billing/gui/ViewExpense.fxml"));

		Parent rootPane = null;
		try {
			rootPane = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("getViewExpensePopup Error in loading the view file :", e);
			alertHelper.beep();

			alertHelper.showErrorAlert(currentStage, "Error Occurred", "Error in creating user interface",
					"An error occurred in creating user interface " + "for the selected command");

			return;
		}

		final Scene scene = new Scene(rootPane);
		final ViewInvoiceController controller = (ViewInvoiceController) fxmlLoader.getController();
		// controller.expense = expense;
		final Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(currentStage);
		stage.setUserData(controller);
		stage.getIcons().add(new Image("/images/shop32X32.png"));
		stage.setScene(scene);
		stage.setTitle("View Expense");
		controller.loadData();
		stage.showAndWait();
	}
}
