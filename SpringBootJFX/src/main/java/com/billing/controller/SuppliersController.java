package com.billing.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.StatusDTO;
import com.billing.dto.Supplier;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.SupplierService;
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
import javafx.collections.transformation.FilteredList;
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

@Controller
public class SuppliersController extends AppContext implements TabContent {

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	SupplierService supplierService;

	@Autowired
	AppUtils appUtils;

	int supplierId;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	FilteredList<Supplier> filteredList;

	@FXML
	private TextField txtName;

	@FXML
	private Label lblNameErrMsg;

	@FXML
	private TextField txtMobileNo;

	@FXML
	private Label lblMobileNoErrMsg;

	@FXML
	private TextField txtGSTNo;

	@FXML
	private TextField txtPAN;

	@FXML
	private Button btnAdd;

	@FXML
	private Button btnUpdate;

	@FXML
	private Button btnDelete;

	@FXML
	private Button btnReset;

	@FXML
	private TextField txtComments;

	@FXML
	private TextField txtEmail;

	@FXML
	private TextField txtAddress;

	@FXML
	private TextField txtCity;

	@FXML
	private TableView<Supplier> tableView;

	@FXML
	private TableColumn<Supplier, String> tcMobileNo;

	@FXML
	private TableColumn<Supplier, String> tcName;

	@FXML
	private TableColumn<Supplier, String> tcCity;

	@FXML
	private TableColumn<Supplier, String> tcEmail;

	@FXML
	private TableColumn<Supplier, String> tcAddress;

	@FXML
	private TableColumn<Supplier, String> tcGSTNo;

	@FXML
	private TableColumn<Supplier, String> tcPAN;

	@FXML
	private TableColumn<Supplier, String> tcComments;

	@FXML
	private TextField txtSearchSupplier;

	@Override
	public void initialize() {
		// Error Messages
		lblMobileNoErrMsg.managedProperty().bind(lblMobileNoErrMsg.visibleProperty());
		lblMobileNoErrMsg.visibleProperty().bind(lblMobileNoErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		lblNameErrMsg.managedProperty().bind(lblNameErrMsg.visibleProperty());
		lblNameErrMsg.visibleProperty().bind(lblNameErrMsg.textProperty().length().greaterThanOrEqualTo(1));
		setTableCellFactories();
		// Force Number Listner
		txtMobileNo.textProperty().addListener(appUtils.getForceNumberListner());
		// Table row selection
		tableView.getSelectionModel().selectedItemProperty().addListener(this::onSelectedRowChanged);
		supplierId = 0; // Reset Supplier Id
		txtSearchSupplier.textProperty()
				.addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
					if (newValue == null || newValue.isEmpty()) {
						filteredList.setPredicate(null);
					} else {
						filteredList.setPredicate((Supplier t) -> {
							// Compare name and Mobile number
							String lowerCaseFilter = newValue.toLowerCase();
							if (t.getSupplierName().toLowerCase().contains(lowerCaseFilter)) {
								return true;
							} else if (String.valueOf(t.getSupplierMobile()).contains(lowerCaseFilter)) {
								return true;
							}
							return false;
						});
					}
				});
	}

	private void setTableCellFactories() {
		// Table Column Mapping
		tcMobileNo.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getSupplierMobile())));
		tcName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSupplierName()));
		tcEmail.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmailId()));
		tcGSTNo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGstNo()));
		tcPAN.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPanNo()));
		tcComments.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getComments()));
		tcAddress.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSupplierAddress()));
		tcCity.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCity()));
		// Set CSS
		tcMobileNo.getStyleClass().add("numeric-cell");
		tcName.getStyleClass().add("character-cell");
		tcCity.getStyleClass().add("character-cell");
		tcEmail.getStyleClass().add("character-cell");
		tcGSTNo.getStyleClass().add("character-cell");
		tcAddress.getStyleClass().add("character-cell");
		tcComments.getStyleClass().add("character-cell");
		tcPAN.getStyleClass().add("character-cell");
	}

	public void onSelectedRowChanged(ObservableValue<? extends Supplier> observable, Supplier oldValue,
			Supplier newValue) {
		resetFields();
		if (newValue != null) {
			txtMobileNo.setText(String.valueOf(newValue.getSupplierMobile()));
			txtName.setText(newValue.getSupplierName());
			txtCity.setText(newValue.getCity());
			txtEmail.setText(newValue.getEmailId());
			txtAddress.setText(newValue.getSupplierAddress());
			txtGSTNo.setText(newValue.getGstNo());
			txtPAN.setText(newValue.getPanNo());
			txtComments.setText(newValue.getComments());
			supplierId = newValue.getSupplierID();
		}
	}

	@FXML
	void onAddCommand(ActionEvent event) {
		if (supplierId != 0) {
			alertHelper.showErrorNotification("Please reset fields");
		} else {
			if (!validateInput()) {
				return;
			}
			saveData();
		}
	}

	@FXML
	void onDeleteCommand(ActionEvent event) {
		if (supplierId == 0) {
			alertHelper.showErrorNotification("Please select supplier");
		} else {
			if (!validateInput()) {
				return;
			}
			deleteData();
		}
	}

	private void deleteData() {
		StatusDTO status = supplierService.isSupplierEntryAvailable(supplierId);
		if (status.getStatusCode() == 0) {
			alertHelper.showErrorNotification("Delete operation not allowed. Purchase entry present for this supplier");
		} else {
			Alert alert = alertHelper.showConfirmAlertWithYesNo(currentStage, null, "Are you sure?");
			if (alert.getResult() == ButtonType.YES) {
				Supplier supplier = new Supplier();
				supplier.setSupplierID(supplierId);
				StatusDTO statusDelete = supplierService.delete(supplier);
				if (statusDelete.getStatusCode() == 0) {
					resetFields();
					loadData();
					alertHelper.showSuccessNotification("Supplier deleted successfully");
				} else {
					alertHelper.showDataDeleteErrAlert(currentStage);
				}

			} else {
				resetFields();
			}
		}
	}

	@FXML
	void onCloseAction(ActionEvent event) {
		closeTab();
	}

	@FXML
	void onResetCommand(ActionEvent event) {
		resetFields();
		tableView.getSelectionModel().clearSelection();
	}

	@FXML
	void onUpdateCommand(ActionEvent event) {
		if (supplierId == 0) {
			alertHelper.showErrorNotification("Please select supplier");
		} else {
			if (!validateInput()) {
				return;
			}
			updateData();
		}
	}

	@Override
	public boolean shouldClose() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void putFocusOnNode() {
		txtName.requestFocus();
	}

	@Override
	public boolean loadData() {
		List<Supplier> list = supplierService.getAll();
		ObservableList<Supplier> tableData = FXCollections.observableArrayList();
		tableData.addAll(list);
		filteredList = new FilteredList(tableData, null);
		tableView.setItems(filteredList);
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
		Supplier sp = new Supplier();
		sp.setSupplierName(txtName.getText());
		sp.setCity(txtCity.getText());
		sp.setSupplierAddress(txtAddress.getText());
		sp.setEmailId(txtEmail.getText());
		sp.setComments(txtComments.getText());
		sp.setSupplierMobile(Long.parseLong(txtMobileNo.getText()));
		sp.setPanNo(txtPAN.getText());
		sp.setGstNo(txtGSTNo.getText());
		StatusDTO status = supplierService.add(sp);
		if (status.getStatusCode() == 0) {
			resetFields();
			loadData();
			alertHelper.showSuccessNotification("Supplier added successfully");
		} else {
			alertHelper.showDataSaveErrAlert(currentStage);
		}
		return true;
	}

	private void updateData() {
		Supplier sp = new Supplier();
		sp.setSupplierName(txtName.getText());
		sp.setCity(txtCity.getText());
		sp.setSupplierAddress(txtAddress.getText());
		sp.setEmailId(txtEmail.getText());
		sp.setComments(txtComments.getText());
		sp.setSupplierMobile(Long.parseLong(txtMobileNo.getText()));
		sp.setPanNo(txtPAN.getText());
		sp.setGstNo(txtGSTNo.getText());
		sp.setSupplierID(supplierId);

		StatusDTO status = supplierService.update(sp);
		if (status.getStatusCode() == 0) {
			resetFields();
			loadData();
			alertHelper.showSuccessNotification("Supplier updated successfully");
		} else {
			alertHelper.showDataSaveErrAlert(currentStage);
		}
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
		// Mobile Number
		int name = txtMobileNo.getText().trim().length();
		if (name == 0) {
			alertHelper.beep();
			lblMobileNoErrMsg.setText("Please enter mobile number");
			txtMobileNo.requestFocus();
			valid = false;
		} else {
			lblMobileNoErrMsg.setText("");
		}

		// Customer Name
		int mUnit = txtName.getText().trim().length();
		if (mUnit == 0) {
			alertHelper.beep();
			lblNameErrMsg.setText("Please enter name");
			txtName.requestFocus();
			valid = false;
		} else {
			lblNameErrMsg.setText("");
		}

		return valid;
	}

	private void resetFields() {
		txtMobileNo.setText("");
		txtName.setText("");
		txtCity.setText("");
		txtEmail.setText("");
		txtGSTNo.setText("");
		txtPAN.setText("");
		txtAddress.setText("");
		txtComments.setText("");
		supplierId = 0;
	}

}
