package com.billing.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

@Controller
public class ManageAccountController implements TabContent{

	private static final Logger logger = LoggerFactory.getLogger(ManageAccountController.class);
	
	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	public Stage currentStage = null;

	private TabPane tabPane = null;
	
    @FXML
    private BorderPane borderPane;

    @FXML
    private Label heading;

    @FXML
    private RadioButton rbPersonalDetails;

    @FXML
    private RadioButton rbChangePassword;

    @FXML
    private RadioButton rbChangeUsername;

    @FXML
    private GridPane gpPersonalDetails;

    @FXML
    private TextField txtFirstName;

    @FXML
    private Label txtFirstNameErrorMsg;

    @FXML
    private TextField txtMobile;

    @FXML
    private Button btnUpdate;

    @FXML
    private TextField txtLastName;

    @FXML
    private TextField txtEmail;

    @FXML
    void onActionChangePassword(ActionEvent event) {

    }

    @FXML
    void onActionChangeUsername(ActionEvent event) {

    }

    @FXML
    void onActionPersonalDetails(ActionEvent event) {

    }

    @FXML
    void onResetCommand(ActionEvent event) {

    }

	@Override
	public boolean shouldClose() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void putFocusOnNode() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean loadData() {
		// TODO Auto-generated method stub
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
	public void initialize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean saveData() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return false;
	}

}
