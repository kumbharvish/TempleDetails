package com.billing.controllers;

import java.time.LocalDate;

import com.billing.dto.Expense;
import com.billing.dto.StatusDTO;
import com.billing.service.ExpensesServices;
import com.billing.utils.PDFUtils;
import com.billing.utils.TabContent;
import com.billing.utils.Utility;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ExpenseController implements TabContent {
	
	public Stage MainWindow = null;
    
    private TabPane tabPane = null;	
    
	@FXML
    private ComboBox<String> cbCategory;

    @FXML
    private Text cbCategoryErrorMsg;

    @FXML
    private TextField txtAmount;

    @FXML
    private Text txtAmountErrorMsg;

    @FXML
    private TextField txtDescription;

    @FXML
    private DatePicker dateExpense;

    @FXML
    private Text dateExpenseErrorMsg;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnClose;
    
    private BooleanProperty isDirty = new SimpleBooleanProperty(false);

    @FXML
    void onCloseCommand(ActionEvent event) {
    	 if (isDirty.get()) {
             ButtonType buttonType = PDFUtils.shouldSaveUnsavedData(MainWindow);
             if (buttonType == ButtonType.CANCEL) {
                 return; // no need to take any further action
             } else if (buttonType == ButtonType.YES) {
            	 if (! validateInput()) {
	            		return;
	                }else {
	                	saveData();
	                }
             } 
         }
         
         closeTab();
    }

    @FXML
    void onSaveCommand(ActionEvent event) {
    	if (! validateInput()) {
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
	            ButtonType response = PDFUtils.shouldSaveUnsavedData(MainWindow);
	            if (response == ButtonType.CANCEL) {
	                return false;
	            }

	            if (response == ButtonType.YES) {
	            	if (! validateInput()) {
	            		return false;
	                }else {
	                	saveData();
	                }
	                
	            }

	        }

	        return true;
	}

	@Override
	public void putFocusOnNode() {
		cbCategory.requestFocus();
	}

	@Override
	public boolean loadData() {
		ExpensesServices.populateDropdown(cbCategory);
		cbCategory.getSelectionModel().select(0);
		dateExpense.setValue(LocalDate.now());
		return true;
	}

	@Override
	public void setMainWindow(Stage stage) {
		MainWindow = stage;
	}

	@Override
	public void setTabPane(TabPane tabPane) {
		this.tabPane = tabPane;
	}

	@Override
	public boolean saveData() {
		Expense expense = new Expense();
		expense.setCategory(cbCategory.getSelectionModel().getSelectedItem());
		expense.setDate(java.sql.Date.valueOf(dateExpense.getValue()));
		expense.setAmount(Double.parseDouble(txtAmount.getText()));
		expense.setDescription(txtDescription.getText());
		
		StatusDTO status = ExpensesServices.addExpense(expense);
		if(status.getStatusCode()==0){
    		PDFUtils.showInfoAlert(MainWindow, "Expense saved successfully !", "Information");
    	}else {
    		String message = Utility.getDataSaveErrorText();
            Utility.beep();
            Alert alert = Utility.getErrorAlert("Error Occurred", 
                    "Error in Saving Data",
                    message, MainWindow);
            alert.showAndWait();
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
		 Tab tab = tabPane.selectionModelProperty().get()
                 .selectedItemProperty().get();
         tabPane.getTabs().remove(tab); //close the current tab

	}

	@Override
	public boolean validateInput() {
		boolean valid = true;
        // Category
        int category = cbCategory.getSelectionModel().getSelectedIndex();
        if (category == 0) {
        	 Utility.beep();
        	 cbCategoryErrorMsg.setText("Please select expese category!");
        	 cbCategory.requestFocus();
             valid = false;
        } else {
        	cbCategoryErrorMsg.setText("");
        }
        //Amount
        int amount = txtAmount.getText().trim().length();
        if (amount == 0) {
	       	Utility.beep();
	       	txtAmountErrorMsg.setText("Please enter amount!");
	       	txtAmount.requestFocus();
           valid = false;
       } else {
    	   txtAmountErrorMsg.setText("");
       } 
        LocalDate startDate = dateExpense.getValue();
        if (startDate == null) {
            dateExpenseErrorMsg.setText("Date not specified!");
            valid = false;
        }else {
        	 dateExpenseErrorMsg.setText("");
        }
		return valid;
	}

	@Override
	public void initialize() {
	 cbCategoryErrorMsg.managedProperty().bind(cbCategoryErrorMsg.visibleProperty());
   	 txtAmountErrorMsg.managedProperty().bind(txtAmountErrorMsg.visibleProperty());
   	 dateExpenseErrorMsg.managedProperty().bind(dateExpenseErrorMsg.visibleProperty());
   	 
   	cbCategoryErrorMsg.visibleProperty()
     .bind(cbCategoryErrorMsg.textProperty().length().greaterThanOrEqualTo(1));
   	txtAmountErrorMsg.visibleProperty()
     .bind(txtAmountErrorMsg.textProperty().length().greaterThanOrEqualTo(1));
   	dateExpenseErrorMsg.visibleProperty()
   	.bind(dateExpenseErrorMsg.textProperty().length().greaterThanOrEqualTo(1));
   	
   	 cbCategory.selectionModelProperty().addListener(this::invalidated);
	 txtAmount.textProperty().addListener(this::invalidated);
	 dateExpense.valueProperty().addListener(this::invalidated);
	 txtAmount.textProperty().addListener(Utility.getForceNumberListner());
	 btnSave.disableProperty().bind(isDirty.not());
	}

}
