package com.billing.controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.constants.AppConstants;
import com.billing.dto.StatusDTO;
import com.billing.dto.UserDetails;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
@Controller
public class UserPreferencesController implements TabContent {

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	@FXML
	private BorderPane borderPane;

	@FXML
	private ComboBox<String> cbDBBackupInterval;
	
	@FXML
	private CheckBox cbShowPrintPreview;

	@FXML
	private Button btnUpdate;

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
	}

	@Override
	public boolean loadData() {
		cbDBBackupInterval.getItems().add("1 Hour");
		cbDBBackupInterval.getItems().add("2 Hour");
		cbDBBackupInterval.getItems().add("3 Hour");
		cbDBBackupInterval.getItems().add("4 Hour");
		
		HashMap<String, String> userPref = appUtils.getAppData();

		cbShowPrintPreview.setSelected(appUtils.isTrue(userPref.get(AppConstants.SHOW_PRINT_PREVIEW)));
		cbDBBackupInterval.getSelectionModel().select(userPref.get(AppConstants.DB_DUMP_INTERVAL));
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
	public void initialize() {
		cbShowPrintPreview.selectedProperty().addListener(this::invalidated);
		cbDBBackupInterval.getSelectionModel().selectedItemProperty().addListener(this::invalidated);
		btnUpdate.disableProperty().bind(isDirty.not());

	}

	@Override
	public boolean saveData() {
		boolean result = false;
		HashMap<String, String> saveMap = new HashMap<>();

		if (cbShowPrintPreview.isSelected()) {
			saveMap.put(AppConstants.SHOW_PRINT_PREVIEW, "Y");
		} else {
			saveMap.put(AppConstants.SHOW_PRINT_PREVIEW, "N");
		}

		saveMap.put(AppConstants.DB_DUMP_INTERVAL, cbDBBackupInterval.getSelectionModel().getSelectedItem());

		StatusDTO status = appUtils.updateUserPreferences(saveMap);

		if (status.getStatusCode() == 0) {
			alertHelper.showSuccessNotification("Preferences updated successfully");
			result = true;
			appUtils.reloadAppData();
		}
		return result;
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
		return valid;
	}

	@FXML
	void onUpdateCommand(ActionEvent event) {
		if (!validateInput()) {
			return;
		}

		boolean result = saveData();

		if (result) {
			closeTab();
		}
	}

	@FXML
	private void onCloseCommand() {
		if (shouldClose()) {
			closeTab();
		}
	}

	@Override
	public void setUserDetails(UserDetails user) {
	}

}