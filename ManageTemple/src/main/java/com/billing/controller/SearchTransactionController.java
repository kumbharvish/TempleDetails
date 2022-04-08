package com.billing.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.constants.AppConstants;
import com.billing.dto.ExpenseType;
import com.billing.dto.IncomeType;
import com.billing.dto.StatusDTO;
import com.billing.dto.TransactionDetails;
import com.billing.dto.TxnSearchCriteria;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.ExpenseTypeService;
import com.billing.service.IncomeTypeService;
import com.billing.service.TransactionsService;
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
public class SearchTransactionController extends AppContext implements TabContent {

	private static final Logger logger = LoggerFactory.getLogger(SearchTransactionController.class);

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	private IntegerProperty matchingInvoicesCount = new SimpleIntegerProperty(0);

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	@Autowired
	TransactionsService transactionsService;

	@Autowired
	IncomeTypeService incomeTypeService;

	@Autowired
	ExpenseTypeService expenseTypeService;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	private ObservableList<TransactionDetails> tableDataList;

	private Label[] errorLabels = null;

	@FXML
	private TitledPane panelSearchCriteria;

	@FXML
	private RadioButton rbIncome;

	@FXML
	private RadioButton rbExpenase;

	@FXML
	private RadioButton rbDonation;

	@FXML
	private RadioButton rbAbhishek;

	@FXML
	private HBox panelExpenseType;

	@FXML
	private HBox panelIncomeType;

	@FXML
	private ComboBox<String> cbExpenseType;

	@FXML
	private ComboBox<String> cbIncomeType;

	@FXML
	private Label lblErrExpenseCategory;

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
	private TableView<TransactionDetails> tableView;

	@FXML
	private TableColumn<TransactionDetails, String> tcExpenseCategory;

	@FXML
	private TableColumn<TransactionDetails, String> tcDate;

	@FXML
	private TableColumn<TransactionDetails, String> tcDescription;

	@FXML
	private TableColumn<TransactionDetails, Double> tcAmount;

	@FXML
	private Label placeholderText;

	@FXML
	private Label lblTotalOfInvoices;

	private HashMap<String, Integer> incomeTypeMap;

	private HashMap<Integer, String> incomeTypeNameMap;

	private HashMap<String, Integer> expenseTypeMap;

	private HashMap<Integer, String> expenseTypeNameMap;

	@FXML
	void onCloseAction(ActionEvent event) {
		closeTab();
	}

	@FXML
	void onDelete(ActionEvent event) {
		TransactionDetails txn = tableView.getSelectionModel().getSelectedItem();
		Alert alert = alertHelper.showConfirmAlertWithYesNo(currentStage, null, "तुम्हाला नक्की हटवायचे आहे का?");
		if (alert.getResult() == ButtonType.YES) {
			StatusDTO status = transactionsService.delete(txn);
			if (status.getStatusCode() == 0) {
				alertHelper.showSuccessNotification("यशस्वीरित्या हटवले");
				removeDeletedRecord(txn);
			} else {
				alertHelper.showErrorNotification("Error occured while deleting");
			}
		}
	}

	private void removeDeletedRecord(TransactionDetails expense) {
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
		TxnSearchCriteria criteria = getCriteria();
		List<TransactionDetails> transactionsList = transactionsService.getSearchedTransactions(criteria);
		mapCategoryName(transactionsList);
		tableDataList.addAll(transactionsList);
		int matchCount = transactionsList.size();

		if (matchCount > 0) {
			panelSearchCriteria.setExpanded(false);
			matchingInvoicesCount.set(matchCount);
			panelSearchResult.setExpanded(true);
			tableView.getSelectionModel().selectFirst();
			tableView.scrollTo(0);
			tableView.requestFocus();
		} else {
			alertHelper.beep();
			alertHelper.showErrorAlert(currentStage, "आढळले नाही", "कोणतेही जुळणारे रेकॉर्ड आढळले नाहीत",
					"तुमच्या शोध निकषांशी कोणतेही रेकॉर्ड जुळले नाहीत");
		}
	}

	private void mapCategoryName(List<TransactionDetails> transactionsList) {

		for (TransactionDetails td : transactionsList) {
			if (td.getTxnType().equals(AppConstants.CREDIT)) {
				if (td.getCategory() == 1) {
					td.setCategoryName("देणगी");
				} else if (td.getCategory() == 2) {
					td.setCategoryName("अभिषेक");
				} else {
					td.setCategoryName(incomeTypeNameMap.get(td.getCategory()));
				}
			} else {
				td.setCategoryName(expenseTypeNameMap.get(td.getCategory()));
			}
		}

	}

	private TxnSearchCriteria getCriteria() {
		TxnSearchCriteria criteria = new TxnSearchCriteria();

		if (rbExpenase.isSelected()) {
			criteria.setCategory(expenseTypeMap.get(cbExpenseType.getSelectionModel().getSelectedItem()));
			criteria.setTxnType(AppConstants.DEBIT);
		}
		if (rbIncome.isSelected()) {
			criteria.setCategory(incomeTypeMap.get(cbIncomeType.getSelectionModel().getSelectedItem()));
			criteria.setTxnType(AppConstants.CREDIT);
		}
		if (rbDonation.isSelected()) {
			criteria.setTxnType(AppConstants.CREDIT);
			criteria.setCategory(1);
		}
		if (rbAbhishek.isSelected()) {
			criteria.setCategory(2);
			criteria.setTxnType(AppConstants.CREDIT);
		}
		if (cbExpenseDate.isSelected()) {
			criteria.setStartDate(dpStartDate.getValue());
			criteria.setEndDate(dpEndDate.getValue());
		}

		if (cbExpenseAmount.isSelected()) {
			criteria.setStartAmount(txtStartAmount.getText().trim());
			criteria.setEndAmount(txtEndAmount.getText().trim());
		}

		return criteria;
	}

	@Override
	public boolean shouldClose() {
		return true;
	}

	@Override
	public void putFocusOnNode() {
	}

	@Override
	public boolean loadData() {
		// Load Income Type
		List<IncomeType> incomeTypeList = incomeTypeService.getAll();
		incomeTypeMap = new HashMap<>();
		incomeTypeNameMap = new HashMap<>();
		cbIncomeType.getItems().add("- Select -");
		for (IncomeType c : incomeTypeList) {
			incomeTypeMap.put(c.getName(), c.getId());
			incomeTypeNameMap.put(c.getId(), c.getName());
			cbIncomeType.getItems().add(c.getName());
		}
		cbIncomeType.getSelectionModel().select(0);

		// Load Expense Type
		cbExpenseType.getItems().add("- Select -");
		List<ExpenseType> expenseTypeList = expenseTypeService.getAll();
		expenseTypeMap = new HashMap<>();
		expenseTypeNameMap = new HashMap<>();
		for (ExpenseType c : expenseTypeList) {
			expenseTypeMap.put(c.getName(), c.getId());
			expenseTypeNameMap.put(c.getId(), c.getName());
			cbExpenseType.getItems().add(c.getName());
		}
		cbExpenseType.getSelectionModel().select(0);
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
		rbExpenase.setToggleGroup(searchCriteriaGroup);
		rbIncome.setToggleGroup(searchCriteriaGroup);
		rbDonation.setToggleGroup(searchCriteriaGroup);
		rbAbhishek.setToggleGroup(searchCriteriaGroup);
		matchingInvoicesCount.set(0);
		tableDataList = FXCollections.observableArrayList();
		tableView.setItems(tableDataList);
		panelExpenseType.managedProperty().bind(panelExpenseType.visibleProperty());
		panelExpenseType.visibleProperty().bind(rbExpenase.selectedProperty());

		panelIncomeType.managedProperty().bind(panelIncomeType.visibleProperty());
		panelIncomeType.visibleProperty().bind(rbIncome.selectedProperty());

		panelDate.managedProperty().bind(panelDate.visibleProperty());
		panelDate.visibleProperty().bind(cbExpenseDate.selectedProperty());

		panelAmount.managedProperty().bind(panelAmount.visibleProperty());
		panelAmount.visibleProperty().bind(cbExpenseAmount.selectedProperty());

		dpStartDate.setValue(LocalDate.now());
		dpEndDate.setValue(LocalDate.now());
		appUtils.setDateConvertor(dpStartDate);
		appUtils.setDateConvertor(dpEndDate);
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

		panelSearchResult.managedProperty().bind(panelSearchResult.visibleProperty());
		panelSearchResult.visibleProperty().bind(matchingInvoicesCount.greaterThan(0));

		errorLabels = new Label[] { lblErrExpenseCategory, lblErrEndAmt, lblErrEndDate, lblErrNoCriteria,
				lblErrStartAmt, lblErrStartDate };
		for (Label label : errorLabels) {
			label.managedProperty().bind(label.visibleProperty());
			label.visibleProperty().bind(label.textProperty().length().greaterThan(0));
		}

		setTableCellFactories();

		tableDataList.addListener(new ListChangeListener<TransactionDetails>() {

			@Override
			public void onChanged(ListChangeListener.Change<? extends TransactionDetails> c) {
				updateTotalAmount();
			}
		});

	}

	protected void updateTotalAmount() {
		Double result = 0.0;
		int invoiceCount = tableDataList.size();
		for (TransactionDetails exp : tableDataList) {
			result = result + exp.getAmount();
		}

		lblTotalOfInvoices.setText(String.format("एकूण  %d रेकॉर्डची  \u20b9 %s", invoiceCount,
				IndianCurrencyFormatting.applyFormatting(result)));
	}

	private void setTableCellFactories() {
		final Callback<TableColumn<TransactionDetails, Double>, TableCell<TransactionDetails, Double>> callback = new Callback<TableColumn<TransactionDetails, Double>, TableCell<TransactionDetails, Double>>() {

			@Override
			public TableCell<TransactionDetails, Double> call(TableColumn<TransactionDetails, Double> param) {
				TableCell<TransactionDetails, Double> tableCell = new TableCell<TransactionDetails, Double>() {

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
		tcExpenseCategory
				.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategoryName()));
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
		if (!(rbExpenase.isSelected() || rbAbhishek.isSelected() || rbIncome.isSelected() || rbDonation.isSelected()
				|| cbExpenseAmount.isSelected() || cbExpenseDate.isSelected())) {
			lblErrNoCriteria.setText("कोणतेही निकष निवडलेले नाहीत");
			return false;
		}

		if (cbExpenseDate.isSelected() && !validateInvoiceDate()) {
			valid = false;
		}

		if (cbExpenseAmount.isSelected() && !validateInvoiceAmount()) {
			valid = false;
		}

		return valid;
	}

	private boolean validateInvoiceDate() {
		boolean valid = true;

		LocalDate startDate = dpStartDate.getValue();
		if (startDate == null) {
			lblErrStartDate.setText("तारीख टाका");
			valid = false;
		}
		LocalDate endDate = dpEndDate.getValue();
		if (endDate == null) {
			lblErrEndDate.setText("तारीख टाका");
			valid = false;
		}

		if (valid && startDate.isAfter(endDate)) {
			lblErrStartDate.setText("प्रारंभ तारीख समाप्ती तारखेपेक्षा नंतरची असू शकत नाही");
			valid = false;
		}
		return valid;
	}

	private boolean validateInvoiceAmount() {
		boolean valid = true;

		BigDecimal startAmount = null;
		String startAmountString = txtStartAmount.getText().trim();

		if (startAmountString.isEmpty()) {
			lblErrStartAmt.setText("रक्कम टाका");
			valid = false;
		} else {
			try {
				startAmount = new BigDecimal(startAmountString);
			} catch (NumberFormatException e) {
				lblErrStartAmt.setText("वैध रक्कम नाही");
				valid = false;
			}
		}

		BigDecimal endAmount = null;
		String endAmountString = txtEndAmount.getText().trim();

		if (endAmountString.isEmpty()) {
			lblErrEndAmt.setText("रक्कम टाका");
			valid = false;
		} else {
			try {
				endAmount = new BigDecimal(endAmountString);
			} catch (NumberFormatException e) {
				lblErrEndAmt.setText("वैध रक्कम नाही");
				valid = false;
			}

		}

		if (valid && startAmount.compareTo(endAmount) == 1) {
			lblErrStartAmt.setText("सुरुवातीची रक्कम शेवटच्या रकमेपेक्षा जास्त असू शकत नाही");
			valid = false;
		}

		return valid;
	}
}
