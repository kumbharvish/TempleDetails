package com.billing.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.constants.AppConstants;
import com.billing.dto.TempleDetails;
import com.billing.dto.StatusDTO;
import com.billing.dto.UserDetails;
import com.billing.service.TempleDetailsService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
@Controller
public class TempleDetailsController implements TabContent {
	
	@Autowired
	TempleDetailsService myStoreService;
	
	@Autowired
	AlertHelper alertHelper;
	
	@Autowired
	AppUtils appUtils;

	private static final Logger logger = LoggerFactory.getLogger(TempleDetailsController.class);

	public Stage currentStage = null;

	private TabPane tabPane = null;

	private int storeId = 0;
	
	@FXML
	private TextField txtStoreName;

	@FXML
	private Label txtStoreNameErrorMsg;

	@FXML
	private TextField txtAddress;

	@FXML
	private TextField txtCity;

	@FXML
	private Label txtCityErrorMsg;

	@FXML
	private TextField txtDistrict;

	@FXML
	private TextField txtState;

	@FXML
	private TextField txtMobileNo;

	@FXML
	private Button btnSave;

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

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

	@FXML
	private void onCloseCommand(ActionEvent event) {

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

	/**
	 * Initializes the controller class.
	 */
	public void initialize() {

		// btnSave.prefWidthProperty().bind(btnClose.widthProperty());
		txtStoreNameErrorMsg.managedProperty().bind(txtStoreNameErrorMsg.visibleProperty());
		txtCityErrorMsg.managedProperty().bind(txtCityErrorMsg.visibleProperty());
		txtStoreNameErrorMsg.visibleProperty()
				.bind(txtStoreNameErrorMsg.textProperty().length().greaterThanOrEqualTo(1));
		txtCityErrorMsg.visibleProperty().bind(txtCityErrorMsg.textProperty().length().greaterThanOrEqualTo(1));

		txtStoreName.textProperty().addListener(this::invalidated);
		txtAddress.textProperty().addListener(this::invalidated);
		txtCity.textProperty().addListener(this::invalidated);
		txtDistrict.textProperty().addListener(this::invalidated);
		txtState.textProperty().addListener(this::invalidated);
		txtMobileNo.textProperty().addListener(this::invalidated);
		txtMobileNo.textProperty().addListener(appUtils.getForceNumberListner());

		btnSave.disableProperty().bind(isDirty.not());
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
	public boolean saveData() {

		TempleDetails myStoreDetails = new TempleDetails();
		myStoreDetails.setMyStoreId(storeId);
		myStoreDetails.setStoreName(txtStoreName.getText());
		myStoreDetails.setAddress(txtAddress.getText());
		myStoreDetails.setCity(txtCity.getText());
		myStoreDetails.setDistrict(txtDistrict.getText());
		myStoreDetails.setState(txtState.getText());
		myStoreDetails.setPhone(!txtMobileNo.getText().equals("") ? Long.valueOf(txtMobileNo.getText()) : 0);
		myStoreDetails.setCstNo(0);
		myStoreDetails.setPanNo("");
		myStoreDetails.setVatNo(0);
		myStoreDetails.setElectricityNo(0);
		myStoreDetails.setMobileNo(!txtMobileNo.getText().equals("") ? Long.valueOf(txtMobileNo.getText()) : 0);

		StatusDTO statusUpdate = myStoreService.update(myStoreDetails);
		StatusDTO statusAdd = null;
		if (statusUpdate.getStatusCode()==0) {
			alertHelper.showInstructionsAlert(currentStage, "Store Details", "Store details updated successfully",
					AppConstants.STORE_DETAILS, 530, 70);
		} else if(statusUpdate.getStatusCode()==1){
			statusAdd = myStoreService.add(myStoreDetails);
			if(statusAdd.getStatusCode()==0){
				alertHelper.showInstructionsAlert(currentStage, "Store Details", "Store details saved successfully",
						AppConstants.STORE_DETAILS, 530, 70);
			}else {
				alertHelper.showDataSaveErrAlert(currentStage);
				return false;
			}
		}else {
			alertHelper.showDataSaveErrAlert(currentStage);
			return false;
		}
		return true;
	}

	@Override
	public void putFocusOnNode() {
		txtStoreName.requestFocus();
	}

	@Override
	public boolean validateInput() {
		boolean valid = true;

		int nameLength = txtStoreName.getText().trim().length();
		if (nameLength == 0) {
			alertHelper.beep();
			txtStoreNameErrorMsg.setText("Store name not specified");
			txtStoreName.requestFocus();
			valid = false;
		} else if (nameLength < 3 || nameLength > 35) {
			txtStoreNameErrorMsg.setText("Store name should be between 3 and 35 characters in length");
			alertHelper.beep();
			txtStoreName.requestFocus();
			valid = false;
		} else {
			txtStoreNameErrorMsg.setText("");
		}

		int cityLength = txtCity.getText().trim().length();
		if (cityLength == 0) {
			alertHelper.beep();
			txtCityErrorMsg.setText("City not specified");
			txtCity.requestFocus();
			valid = false;
		} else if (cityLength < 3 || cityLength > 20) {
			alertHelper.beep();
			txtCityErrorMsg.setText("City should be between 3 and 20 characters in length");
			txtCity.requestFocus();
			valid = false;
		} else {
			txtCityErrorMsg.setText("");
		}
		return valid;
	}

	@Override
	public boolean loadData() {

		TempleDetails myStoreDetails = null;
		try {
			myStoreDetails = myStoreService.getMyStoreDetails();
		} catch (Exception e) {
			alertHelper.showDataFetchErrAlert(currentStage);
			logger.error("StoreDetailsController loadData -->", e);
			return false;
		}

		if (myStoreDetails == null) {
			return true;
		}
		boolean success = populateFields(myStoreDetails);
		isDirty.set(false);
		return success;

	}

	private boolean populateFields(TempleDetails myStoreDetails) {
		txtStoreName.setText(myStoreDetails.getStoreName());
		storeId = myStoreDetails.getMyStoreId();
		txtCity.setText(myStoreDetails.getCity());
		txtAddress.setText(myStoreDetails.getAddress());
		txtDistrict.setText(myStoreDetails.getDistrict());
		txtState.setText(myStoreDetails.getState());
		if (0 == myStoreDetails.getMobileNo()) {
			txtMobileNo.setText("");
		} else {
			txtMobileNo.setText(String.valueOf(myStoreDetails.getMobileNo()));
		}
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
		// TODO Auto-generated method stub
		
	}

}
