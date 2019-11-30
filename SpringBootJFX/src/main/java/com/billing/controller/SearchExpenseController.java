package com.billing.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.Expense;
import com.billing.dto.ExpenseSearchCriteria;
import com.billing.dto.StatusDTO;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.ExpensesService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.IndianCurrencyFormatting;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

@Controller
public class SearchExpenseController extends AppContext implements TabContent {

	private static final Logger logger = LoggerFactory.getLogger(SearchExpenseController.class);

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	private IntegerProperty matchingInvoicesCount = new SimpleIntegerProperty(0);

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	@Autowired
	ExpensesService expenseService;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	private ObservableList<Expense> tableDataList;

	private Label[] errorLabels = null;

	@FXML
	private TitledPane panelSearchCriteria;

	@FXML
	private RadioButton rbSearchByExpenseCategory;

	@FXML
	private HBox panelInvoiceNo;

	@FXML
	private ComboBox<String> cbExpenseCategory;

	@FXML
	private Label lblErrExpenseCategory;

	@FXML
	private RadioButton rbSearchByOtherCriteria;

	@FXML
	private VBox panelOtherCriteria;

	@FXML
	private CheckBox cbExpenseDate;

	@FXML
	private HBox panelDate;

	@FXML
	private DatePicker dpStartDate;

	@FXML
	private Label lblErrStartDate;

	@FXML
	private DatePicker dpEndDate;

	@FXML
	private Label lblErrEndDate;

	@FXML
	private CheckBox cbExpenseAmount;

	@FXML
	private HBox panelAmount;

	@FXML
	private TextField txtStartAmount;

	@FXML
	private Label lblErrStartAmt;

	@FXML
	private TextField txtEndAmount;

	@FXML
	private Label lblErrEndAmt;

	@FXML
	private Label lblErrNoCriteria;

	@FXML
	private Button btnSearchInvoice;

	@FXML
	private TitledPane panelSearchResult;

	@FXML
	private TableView<Expense> tableView;

	@FXML
	private TableColumn<Expense, String> tcExpenseCategory;

	@FXML
	private TableColumn<Expense, String> tcDate;

	@FXML
	private TableColumn<Expense, String> tcDescription;

	@FXML
	private TableColumn<Expense, Double> tcAmount;

	@FXML
	private Label placeholderText;

	@FXML
	private Label lblTotalOfInvoices;

	@FXML
	void onCloseAction(ActionEvent event) {
		closeTab();
	}

	@FXML
	void onDelete(ActionEvent event) {
		Expense expense = tableView.getSelectionModel().getSelectedItem();
		Alert alert = alertHelper.showConfirmAlertWithYesNo(currentStage, null, "Are you sure to delete expense ?");
		if (alert.getResult() == ButtonType.YES) {
			StatusDTO status = expenseService.delete(expense);
			if (status.getStatusCode() == 0) {
				alertHelper.showSuccessNotification("Expense deleted successfully");
				removeDeletedRecord(expense);
			} else {
				alertHelper.showErrorNotification("Error occured while deleting expense");
			}
		}
	}

	private void removeDeletedRecord(Expense expense) {
		if (tableDataList.contains(expense)) {
			tableDataList.remove(expense);
		}
		int count = tableDataList.size();
		if (count == 0) {
			matchingInvoicesCount.set(count);
			panelSearchCriteria.setExpanded(true);
		} else {
			tableView.getSelectionModel().selectFirst();
			tableView.scrollTo(0);
			tableView.requestFocus();

		}

	}

	@FXML
	void onEditAction(ActionEvent event) {
		Expense expense = tableView.getSelectionModel().getSelectedItem();
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setControllerFactory(springContext::getBean);
		fxmlLoader.setLocation(this.getClass().getResource("/com/billing/gui/EditExpense.fxml"));

		Parent rootPane = null;
		try {
			rootPane = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("getEditInvoicePopup Error in loading the view file :", e);
			alertHelper.beep();

			alertHelper.showErrorAlert(currentStage, "Error Occurred", "Error in creating user interface",
					"An error occurred in creating user interface " + "for the selected command");

			return;
		}

		final Scene scene = new Scene(rootPane);
		final EditExpenseController controller = (EditExpenseController) fxmlLoader.getController();
		controller.expense = expense;
		controller.setTask(() -> {
			afterSuccessTask();
		});
		final Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(currentStage);
		stage.setUserData(controller);
		stage.getIcons().add(new Image("/images/shop32X32.png"));
		stage.setScene(scene);
		stage.setTitle("Edit Expense");
		controller.setMainWindow(stage);
		controller.putFocusOnNode();
		controller.setUserDetails(userDetails);
		controller.loadData();
		stage.showAndWait();
	}

	public void afterSuccessTask() {
		panelSearchCriteria.setExpanded(true);
		panelSearchResult.setExpanded(false);
	}

	@FXML
	void onSearchInvoiceAction(ActionEvent event) {
		if (!validateInput()) {
			return;
		}
		tableDataList.clear();
		matchingInvoicesCount.set(0);
		ExpenseSearchCriteria criteria = getCriteria();
		List<Expense> invoiceResults = expenseService.getSearchedExpenses(criteria);
		tableDataList.addAll(invoiceResults);
		int matchCount = invoiceResults.size();

		if (matchCount > 0) {
			panelSearchCriteria.setExpanded(false);
			matchingInvoicesCount.set(matchCount);
			panelSearchResult.setExpanded(true);
			tableView.getSelectionModel().selectFirst();
			tableView.scrollTo(0);
			tableView.requestFocus();
		} else {
			alertHelper.beep();
			alertHelper.showErrorAlert(currentStage, "No Match Found", "No matching expense found",
					"No expense matched your search criteria");
		}
	}

	private ExpenseSearchCriteria getCriteria() {
		ExpenseSearchCriteria criteria = new ExpenseSearchCriteria();

		if (rbSearchByExpenseCategory.isSelected()) {
			criteria.setExpenseCategory(cbExpenseCategory.getSelectionModel().getSelectedItem());
		} else {
			if (cbExpenseDate.isSelected()) {
				criteria.setStartDate(dpStartDate.getValue());
				criteria.setEndDate(dpEndDate.getValue());
			}

			if (cbExpenseAmount.isSelected()) {
				criteria.setStartAmount(txtStartAmount.getText().trim());
				criteria.setEndAmount(txtEndAmount.getText().trim());
			}
		}

		return criteria;
	}

	@Override
	public boolean shouldClose() {
		return true;
	}

	@Override
	public void putFocusOnNode() {
		rbSearchByExpenseCategory.requestFocus();
	}

	@Override
	public boolean loadData() {
		// Populate Expense Category
		expenseService.fillExpenseTypes(cbExpenseCategory);
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
	public void initialize() {
		ToggleGroup searchCriteriaGroup = new ToggleGroup();
		rbSearchByExpenseCategory.setToggleGroup(searchCriteriaGroup);
		rbSearchByOtherCriteria.setToggleGroup(searchCriteriaGroup);
		matchingInvoicesCount.set(0);
		tableDataList = FXCollections.observableArrayList();
		tableView.setItems(tableDataList);
		panelInvoiceNo.managedProperty().bind(panelInvoiceNo.visibleProperty());
		panelInvoiceNo.visibleProperty().bind(rbSearchByExpenseCategory.selectedProperty());

		panelOtherCriteria.managedProperty().bind(panelOtherCriteria.visibleProperty());
		panelOtherCriteria.visibleProperty().bind(rbSearchByOtherCriteria.selectedProperty());

		panelDate.managedProperty().bind(panelDate.visibleProperty());
		panelDate.visibleProperty().bind(cbExpenseDate.selectedProperty());

		panelAmount.managedProperty().bind(panelAmount.visibleProperty());
		panelAmount.visibleProperty().bind(cbExpenseAmount.selectedProperty());

		dpStartDate.setValue(LocalDate.now());
		dpEndDate.setValue(LocalDate.now());
		dpStartDate.valueProperty().addListener((observable, oldDate, newDate) -> {
			LocalDate today = LocalDate.now();
			if (newDate == null) {
				dpStartDate.setValue(today);
			}
		});

		dpEndDate.valueProperty().addListener((observable, oldDate, newDate) -> {
			LocalDate today = LocalDate.now();
			if (newDate == null) {
				dpEndDate.setValue(today);
			}
		});

		btnSearchInvoice.disableProperty().bind(searchCriteriaGroup.selectedToggleProperty().isNull());

		panelSearchResult.managedProperty().bind(panelSearchResult.visibleProperty());
		panelSearchResult.visibleProperty().bind(matchingInvoicesCount.greaterThan(0));

		errorLabels = new Label[] { lblErrExpenseCategory, lblErrEndAmt, lblErrEndDate, lblErrNoCriteria,
				lblErrStartAmt, lblErrStartDate };
		for (Label label : errorLabels) {
			label.managedProperty().bind(label.visibleProperty());
			label.visibleProperty().bind(label.textProperty().length().greaterThan(0));
		}

		setTableCellFactories();

		tableDataList.addListener(new ListChangeListener<Expense>() {

			@Override
			public void onChanged(ListChangeListener.Change<? extends Expense> c) {
				updateTotalAmount();
			}
		});

	}

	protected void updateTotalAmount() {
		Double result = 0.0;
		int invoiceCount = tableDataList.size();
		for (Expense exp : tableDataList) {
			result = result + exp.getAmount();
		}

		lblTotalOfInvoices.setText(String.format("Total of %d Expense(s) is \u20b9 %s", invoiceCount,
				IndianCurrencyFormatting.applyFormatting(result)));
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
		tcExpenseCategory.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory()));
		tcDate.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getFormattedDateWithTime(cellData.getValue().getDate())));
		tcDescription.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
		tcAmount.setCellFactory(callback);

		tcExpenseCategory.getStyleClass().add("character-cell");
		tcDate.getStyleClass().add("character-cell");
		tcDescription.getStyleClass().add("character-cell");
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

	private void clearErrorLabels() {
		for (Label label : errorLabels) {
			label.setText("");
		}
	}

	@Override
	public boolean validateInput() {
		boolean valid = true;
		clearErrorLabels();
		if (rbSearchByExpenseCategory.isSelected()) {
			int expenseCategory = cbExpenseCategory.getSelectionModel().getSelectedIndex();
			if (expenseCategory == 0) {
				lblErrExpenseCategory.setText("Expense category not selected");
				valid = false;
				return valid;
			}

		} else {
			if (!(cbExpenseAmount.isSelected() || cbExpenseDate.isSelected())) {
				lblErrNoCriteria.setText("No criteria selected");
				return false;
			}

			if (cbExpenseDate.isSelected() && !validateInvoiceDate()) {
				valid = false;
			}

			if (cbExpenseAmount.isSelected() && !validateInvoiceAmount()) {
				valid = false;
			}
		}

		return valid;
	}

	private boolean validateInvoiceDate() {
		boolean valid = true;

		LocalDate startDate = dpStartDate.getValue();
		if (startDate == null) {
			lblErrStartDate.setText("Start date not specified");
			valid = false;
		}
		LocalDate endDate = dpEndDate.getValue();
		if (endDate == null) {
			lblErrEndDate.setText("End date not specified");
			valid = false;
		}

		if (valid && startDate.isAfter(endDate)) {
			lblErrStartDate.setText("Start date can't be later than the end date");
			valid = false;
		}
		return valid;
	}

	private boolean validateInvoiceAmount() {
		boolean valid = true;

		BigDecimal startAmount = null;
		String startAmountString = txtStartAmount.getText().trim();

		if (startAmountString.isEmpty()) {
			lblErrStartAmt.setText("Start amount not specified");
			valid = false;
		} else {
			try {
				startAmount = new BigDecimal(startAmountString);
			} catch (NumberFormatException e) {
				lblErrStartAmt.setText("Not a valid amount");
				valid = false;
			}
		}

		BigDecimal endAmount = null;
		String endAmountString = txtEndAmount.getText().trim();

		if (endAmountString.isEmpty()) {
			lblErrEndAmt.setText("End amount not specified");
			valid = false;
		} else {
			try {
				endAmount = new BigDecimal(endAmountString);
			} catch (NumberFormatException e) {
				lblErrEndAmt.setText("Not a valid amount");
				valid = false;
			}

		}

		if (valid && startAmount.compareTo(endAmount) == 1) {
			lblErrStartAmt.setText("Start amount can't be greater than the end amount");
			valid = false;
		}

		return valid;
	}
}
