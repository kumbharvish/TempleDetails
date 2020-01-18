package com.billing.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.UserDetails;
import com.billing.service.DBBackupService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
@Controller
public class DeleteDataController implements TabContent {

	@Autowired
	AppUtils appUtils;

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	DBBackupService dbBackupService;

	private TabPane tabPane;

	public Stage currentStage = null;

	@FXML
	private DatePicker dpFromDate;

	@FXML
	private DatePicker dpToDate;

	@FXML
	private Label lblErrMsgFormDate;

	@FXML
	private Label lblErrMsgToDate;

	@FXML
	private Button btnDelete;

	@FXML
	private Button btnClose;

	@FXML
	void onDeleteAction(ActionEvent event) {
		if (!validateInput()) {
			alertHelper.beep();
			return;
		}
		Alert alert = alertHelper.showConfirmAlertWithYesNo(currentStage, null, "Are you sure to delete data ?");
		if (alert.getResult() == ButtonType.YES) {
			dbBackupService.deleteData(dpFromDate.getValue().toString(), dpToDate.getValue().toString());
		}
	}

	@Override
	public boolean shouldClose() {
		return true;
	}

	@Override
	public void putFocusOnNode() {
		dpFromDate.requestFocus();
	}

	@Override
	public boolean loadData() {
		return true;
	}

	@Override
	public void setMainWindow(Stage stage) {
		currentStage = stage;
	}

	@Override
	public void setTabPane(TabPane pane) {
		tabPane = pane;
	}

	@FXML
	private void onCloseAction(ActionEvent event) {
		final Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
		tabPane.getTabs().remove(currentTab);
	}

	@Override
	public boolean saveData() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void invalidated(Observable observable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeTab() {
		Tab tab = tabPane.selectionModelProperty().get().selectedItemProperty().get();
		tabPane.getTabs().remove(tab); // close the current tab
	}

	@Override
	public boolean validateInput() {
		lblErrMsgFormDate.setText("");
		lblErrMsgToDate.setText("");
		final LocalDate fromDate = dpFromDate.getValue();
		if (fromDate == null) {
			lblErrMsgFormDate.setText("From Date not specified!");
			return false;
		}
		final LocalDate toDate = dpToDate.getValue();
		if (toDate == null) {
			lblErrMsgToDate.setText("To Date not specified!");
			return false;
		}
		if (fromDate.isAfter(toDate)) {
			lblErrMsgFormDate.setText("From Date can't be later than To date");
			return false;
		}
		return true;
	}

	@Override
	public void initialize() {
		dpFromDate.setDayCellFactory(this::getDateCell);
		dpToDate.setDayCellFactory(this::getDateCell);

	}

	@Override
	public void setUserDetails(UserDetails user) {
		// TODO Auto-generated method stub

	}

	private DateCell getDateCell(DatePicker datePicker) {
		return appUtils.getDateCell(datePicker, null, LocalDate.now());
	}
}
