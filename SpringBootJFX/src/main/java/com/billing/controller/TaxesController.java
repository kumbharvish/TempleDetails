package com.billing.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.StatusDTO;
import com.billing.dto.Tax;
import com.billing.dto.UserDetails;
import com.billing.service.TaxesService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
@Controller
public class TaxesController implements TabContent {

	@Autowired
	TaxesService taxesService;

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	@FXML
	private TextField txtTaxName;

	@FXML
	private Label txtTaxNameErrorMsg;

	@FXML
	private TextField txtTaxValue;

	@FXML
	private Label txtTaxValueErrorMsg;

	@FXML
	private Button btnAdd;

	@FXML
	private Button btnUpdate;

	@FXML
	private Button btnDelete;

	@FXML
	private Button btnReset;

	@FXML
	private TableView<Tax> tableView;

	@FXML
	private TableColumn<Tax, String> tcTaxName;

	@FXML
	private TableColumn<Tax, String> tcTaxValue;

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	private int taxCode = 0;

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
		txtTaxName.requestFocus();
	}

	@Override
	public boolean loadData() {
		List<Tax> list = taxesService.getAll();
		ObservableList<Tax> taxTableData = FXCollections.observableArrayList();
		taxTableData.addAll(list);
		tableView.setItems(taxTableData);
		// Set Shortcuts
		// Add
		KeyCombination kc = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_ANY);
		Runnable rn = () -> onAddCommand(null);
		currentStage.getScene().getAccelerators().put(kc, rn);

		// Update
		KeyCombination ku = new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_ANY);
		Runnable ru = () -> onUpdateCommand(null);
		currentStage.getScene().getAccelerators().put(ku, ru);

		// Delete
		KeyCombination kd = new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_ANY);
		Runnable rd = () -> onDeleteCommand(null);
		currentStage.getScene().getAccelerators().put(kd, rd);

		// Reset
		KeyCombination kr = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_ANY);
		Runnable rr = () -> onResetCommand(null);
		currentStage.getScene().getAccelerators().put(kr, rr);
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
		txtTaxNameErrorMsg.managedProperty().bind(txtTaxNameErrorMsg.visibleProperty());
		txtTaxNameErrorMsg.visibleProperty().bind(txtTaxNameErrorMsg.textProperty().length().greaterThanOrEqualTo(1));
		txtTaxValueErrorMsg.managedProperty().bind(txtTaxValueErrorMsg.visibleProperty());
		txtTaxValueErrorMsg.visibleProperty().bind(txtTaxValueErrorMsg.textProperty().length().greaterThanOrEqualTo(1));

		txtTaxValue.textProperty().addListener(appUtils.getForceDecimalNumberListner());

		setTableCellFactories();
		tableView.getSelectionModel().selectedItemProperty().addListener(this::onSelectedRowChanged);
		taxCode = 0;
	}

	private void setTableCellFactories() {
		tcTaxName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
		tcTaxValue.setCellValueFactory(
				cellData -> new SimpleStringProperty(appUtils.getDecimalFormat(cellData.getValue().getValue())));
		// Set CSS
		tcTaxName.getStyleClass().add("character-cell");
		tcTaxValue.getStyleClass().add("numeric-cell");

	}

	public void onSelectedRowChanged(ObservableValue<? extends Tax> observable, Tax oldValue, Tax newValue) {
		if (newValue != null) {
			txtTaxName.setText(newValue.getName());
			txtTaxValue.setText(appUtils.getDecimalFormat(newValue.getValue()));
			taxCode = newValue.getId();
		}
	}

	@Override
	public boolean saveData() {
		Tax tax = new Tax();
		tax.setName(txtTaxName.getText());
		tax.setValue(Double.valueOf(txtTaxValue.getText()));
		StatusDTO status = taxesService.add(tax);
		if (status.getStatusCode() == 0) {
			restFields();
			loadData();
			alertHelper.showSuccessNotification("Tax added successfully");
		} else {
			alertHelper.showDataSaveErrAlert(currentStage);
		}
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
		int tax = txtTaxName.getText().trim().length();
		if (tax == 0) {
			alertHelper.beep();
			txtTaxNameErrorMsg.setText("Please enter Tax name");
			txtTaxName.requestFocus();
			valid = false;
		} else {
			txtTaxNameErrorMsg.setText("");
		}
		int value = txtTaxValue.getText().trim().length();
		if (value == 0) {
			alertHelper.beep();
			txtTaxValueErrorMsg.setText("Please enter Tax (%)");
			txtTaxValue.requestFocus();
			valid = false;
		} else {
			txtTaxValueErrorMsg.setText("");
		}
		return valid;
	}

	@FXML
	void onAddCommand(ActionEvent event) {
		if (taxCode == 0) {
			if (!validateInput()) {
				return;
			}
			saveData();
		} else {
			alertHelper.showErrorNotification("Please reset fields");
		}
	}

	@FXML
	void onCloseCommand(ActionEvent event) {
		closeTab();
	}

	@FXML
	void onDeleteCommand(ActionEvent event) {
		if (taxCode == 0) {
			alertHelper.showErrorNotification("Please select Tax");
		} else {
			Alert alert = alertHelper.showConfirmAlertWithYesNo(currentStage, null, "Are you sure?");
			if (alert.getResult() == ButtonType.YES) {
				Tax tax = new Tax();
				tax.setId(taxCode);
				StatusDTO status = taxesService.delete(tax);
				if (status.getStatusCode() == 0) {
					alertHelper.showSuccessNotification("Tax deleted successfully");
					loadData();
					restFields();
				} else {
					alertHelper.showDataDeleteErrAlert(currentStage);
				}

			}

		}
	}

	@FXML
	void onResetCommand(ActionEvent event) {
		restFields();
		tableView.getSelectionModel().clearSelection();
	}

	private void restFields() {
		txtTaxName.setText("");
		txtTaxValue.setText("");
		taxCode = 0;
	}

	@FXML
	void onUpdateCommand(ActionEvent event) {
		if (taxCode == 0) {
			alertHelper.showErrorNotification("Please select Tax");
		} else {
			if (!validateInput()) {
				return;
			}
			updateData();
		}
	}

	private void updateData() {
		Tax tax = new Tax();
		tax.setId(taxCode);
		tax.setName(txtTaxName.getText());
		tax.setValue(Double.valueOf(txtTaxValue.getText()));

		StatusDTO status = taxesService.update(tax);
		if (status.getStatusCode() == 0) {
			restFields();
			loadData();
			alertHelper.showSuccessNotification("Tax updated successfully");
		} else {
			alertHelper.showDataSaveErrAlert(currentStage);
		}
	}

	@Override
	public void setUserDetails(UserDetails user) {
		// TODO Auto-generated method stub

	}

}