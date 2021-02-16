package com.billing.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.MeasurementUnit;
import com.billing.dto.StatusDTO;
import com.billing.dto.UserDetails;
import com.billing.service.MeasurementUnitsService;
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
import javafx.stage.Stage;

@SuppressWarnings("restriction")
@Controller
public class UOMController implements TabContent {

	@Autowired
	MeasurementUnitsService measurementUnitsService;

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	@FXML
	private TextField txtUOMName;

	@FXML
	private Label txtUOMNameErrorMsg;

	@FXML
	private TextField txtUOMDesc;

	@FXML
	private Button btnAdd;

	@FXML
	private Button btnUpdate;

	@FXML
	private Button btnDelete;

	@FXML
	private Button btnReset;

	@FXML
	private TableView<MeasurementUnit> tableView;

	@FXML
	private TableColumn<MeasurementUnit, String> tcUOMName;

	@FXML
	private TableColumn<MeasurementUnit, String> tcUOMDesc;

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	private int uomCode = 0;

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
		txtUOMName.requestFocus();
	}

	@Override
	public boolean loadData() {
		List<MeasurementUnit> list = measurementUnitsService.getAll();
		ObservableList<MeasurementUnit> uomTableData = FXCollections.observableArrayList();
		uomTableData.addAll(list);
		tableView.setItems(uomTableData);
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
		txtUOMNameErrorMsg.managedProperty().bind(txtUOMNameErrorMsg.visibleProperty());
		txtUOMNameErrorMsg.visibleProperty().bind(txtUOMNameErrorMsg.textProperty().length().greaterThanOrEqualTo(1));
		setTableCellFactories();
		tableView.getSelectionModel().selectedItemProperty().addListener(this::onSelectedRowChanged);
		uomCode = 0;
	}

	private void setTableCellFactories() {
		tcUOMName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
		tcUOMDesc.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
		// Set CSS
		tcUOMName.getStyleClass().add("character-cell");
		tcUOMDesc.getStyleClass().add("character-cell");

	}

	public void onSelectedRowChanged(ObservableValue<? extends MeasurementUnit> observable, MeasurementUnit oldValue,
			MeasurementUnit newValue) {
		if (newValue != null) {
			txtUOMName.setText(newValue.getName());
			txtUOMDesc.setText(newValue.getDescription());
			uomCode = newValue.getId();
		}
	}

	@Override
	public boolean saveData() {
		MeasurementUnit uom = new MeasurementUnit();
		uom.setName(txtUOMName.getText());
		uom.setDescription(txtUOMDesc.getText());
		StatusDTO status = measurementUnitsService.add(uom);
		if (status.getStatusCode() == 0) {
			restFields();
			loadData();
			alertHelper.showSuccessNotification("Unit of measure added sucessfully");
		} else {
			if (status.getException().contains("UNIQUE")) {
				alertHelper.showErrorNotification("Entered UOM name already exists");
			} else {
				alertHelper.showDataSaveErrAlert(currentStage);
			}
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
		int uom = txtUOMName.getText().trim().length();
		if (uom == 0) {
			alertHelper.beep();
			txtUOMNameErrorMsg.setText("Please enter UOM name");
			txtUOMName.requestFocus();
			valid = false;
		} else {
			txtUOMNameErrorMsg.setText("");
		}
		return valid;
	}

	@FXML
	void onAddCommand(ActionEvent event) {
		if (uomCode == 0) {
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
		if (uomCode == 0) {
			alertHelper.showErrorNotification("Please select UOM");
		} else {
			Alert alert = alertHelper.showConfirmAlertWithYesNo(currentStage, null, "Are you sure?");
			if (alert.getResult() == ButtonType.YES) {
				MeasurementUnit uom = new MeasurementUnit();
				uom.setId(uomCode);
				StatusDTO status = measurementUnitsService.delete(uom);
				if (status.getStatusCode() == 0) {
					alertHelper.showSuccessNotification("Unit of measure deleted sucessfully");
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
		txtUOMName.setText("");
		txtUOMDesc.setText("");
		uomCode = 0;
	}

	@FXML
	void onUpdateCommand(ActionEvent event) {
		if (uomCode == 0) {
			alertHelper.showErrorNotification("Please select UOM");
		} else {
			if (!validateInput()) {
				return;
			}
			updateData();
		}
	}

	private void updateData() {
		MeasurementUnit uom = new MeasurementUnit();
		uom.setId(uomCode);
		uom.setName(txtUOMName.getText());
		uom.setDescription(txtUOMDesc.getText());

		StatusDTO status = measurementUnitsService.update(uom);
		if (status.getStatusCode() == 0) {
			restFields();
			loadData();
			alertHelper.showSuccessNotification("Unit of Measure updated sucessfully");
		} else {
			if (status.getException().contains("UNIQUE")) {
				alertHelper.showErrorNotification("UOM name already exists");
			} else {
				alertHelper.showDataSaveErrAlert(currentStage);
			}
		}
	}

	@Override
	public void setUserDetails(UserDetails user) {
	}

}