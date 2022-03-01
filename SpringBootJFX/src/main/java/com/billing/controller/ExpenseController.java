package com.billing.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.Expense;
import com.billing.dto.StatusDTO;
import com.billing.dto.UserDetails;
import com.billing.service.ExpensesService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
@Controller
public class ExpenseController implements TabContent {

	@Autowired
	ExpensesService expensesService;

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	@FXML
	private ComboBox<String> cbCategory;

	@FXML
	private Label cbCategoryErrorMsg;

	@FXML
	private TextField txtAmount;

	@FXML
	private Label txtAmountErrorMsg;

	@FXML
	private TextField txtDescription;

	@FXML
	private DatePicker dateExpense;

	@FXML
	private Label dateExpenseErrorMsg;

	@FXML
	private Button btnSave;

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	@FXML
	void onCloseCommand(ActionEvent event) {
		if (isDirty.get()) {
			ButtonType buttonType = appUtils.shouldSaveUnsavedData(currentStage);
			if (buttonType == ButtonType.CANCEL) {
				return; // no need to take any further action
			} else if (buttonType == ButtonType.YES) {
				if (!validateInput()) {
					return;
				} else {
					saveData();
				}
			}
		}

		closeTab();
	}

	@FXML
	void onSaveCommand(ActionEvent event) {
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
		cbCategory.requestFocus();
		cbCategory.show();
		// Expense Category
		cbCategory.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (!cbCategory.getSelectionModel().getSelectedItem().equals("-- Select Category --")
						&& ke.getCode().equals(KeyCode.ENTER)) {
					txtAmount.requestFocus();
				}
			}
		});
		// Amount
		txtAmount.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (!txtAmount.getText().equals("") && ke.getCode().equals(KeyCode.ENTER)) {
					txtDescription.requestFocus();
				}
			}
		});
		// Description
		txtDescription.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					dateExpense.requestFocus();
				}
			}
		});

		// Date
		dateExpense.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					btnSave.requestFocus();
				}
			}
		});
	}

	@Override
	public boolean loadData() {
		expensesService.fillExpenseTypes(cbCategory);
		dateExpense.setValue(LocalDate.now());
		appUtils.setDateConvertor(dateExpense);
		isDirty.set(false);
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
	public boolean saveData() {
		Expense expense = new Expense();
		expense.setCategory(cbCategory.getSelectionModel().getSelectedItem());
		expense.setDate(dateExpense.getValue().toString());
		expense.setAmount(Double.parseDouble(txtAmount.getText()));
		expense.setDescription(txtDescription.getText());

		StatusDTO status = expensesService.add(expense);
		if (status.getStatusCode() == 0) {
			alertHelper.showSuccessNotification("Expense saved successfully");
		} else {
			alertHelper.showDataSaveErrAlert(currentStage);
			return false;
		}
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
		// Category
		int category = cbCategory.getSelectionModel().getSelectedIndex();
		if (category == 0) {
			alertHelper.beep();
			cbCategoryErrorMsg.setText("Please select expese category");
			cbCategory.requestFocus();
			valid = false;
		} else {
			cbCategoryErrorMsg.setText("");
		}
		// Amount
		int amount = txtAmount.getText().trim().length();
		if (amount == 0) {
			alertHelper.beep();
			txtAmountErrorMsg.setText("Please enter amount");
			txtAmount.requestFocus();
			valid = false;
		} else {
			txtAmountErrorMsg.setText("");
		}
		LocalDate startDate = dateExpense.getValue();
		if (startDate == null) {
			dateExpenseErrorMsg.setText("Date not specified");
			valid = false;
		} else {
			dateExpenseErrorMsg.setText("");
		}
		return valid;
	}

	@Override
	public void initialize() {
		cbCategoryErrorMsg.managedProperty().bind(cbCategoryErrorMsg.visibleProperty());
		txtAmountErrorMsg.managedProperty().bind(txtAmountErrorMsg.visibleProperty());
		dateExpenseErrorMsg.managedProperty().bind(dateExpenseErrorMsg.visibleProperty());

		cbCategoryErrorMsg.visibleProperty().bind(cbCategoryErrorMsg.textProperty().length().greaterThanOrEqualTo(1));
		txtAmountErrorMsg.visibleProperty().bind(txtAmountErrorMsg.textProperty().length().greaterThanOrEqualTo(1));
		dateExpenseErrorMsg.visibleProperty().bind(dateExpenseErrorMsg.textProperty().length().greaterThanOrEqualTo(1));

		cbCategory.selectionModelProperty().addListener(this::invalidated);
		txtAmount.textProperty().addListener(this::invalidated);
		dateExpense.valueProperty().addListener(this::invalidated);
		txtAmount.textProperty().addListener(appUtils.getForceNumberListner());
		btnSave.disableProperty().bind(isDirty.not());
		cbCategory.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue) {
				cbCategory.show();
			}
		});
		txtAmount.textProperty().addListener(this::invalidated);
		txtDescription.textProperty().addListener(this::invalidated);
		cbCategory.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				isDirty.set(true);
			}
		});
		dateExpense.valueProperty().addListener((observable, oldDate, newDate) -> {
			isDirty.set(true);
		});
	}

	@Override
	public void setUserDetails(UserDetails user) {
		// TODO Auto-generated method stub

	}

}
