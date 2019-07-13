package com.billing.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.ProductCategory;
import com.billing.service.ProductCategoryService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
@Controller
public class ProductCategoryController implements TabContent{
	
	@Autowired
	ProductCategoryService productCategoryService;
	
	@Autowired
	AlertHelper alertHelper;
	
	@Autowired
	AppUtils appUtils;

	public Stage MainWindow = null;
    
    private TabPane tabPane = null;	
    
    @FXML
    private TextField txtCategoryName;

    @FXML
    private Text txtCategoryNameErrorMsg;

    @FXML
    private TextField txtCategoryDesc;

    @FXML
    private Button btnAdd;

    @FXML
    private Button btnUpdate;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnReset;

    @FXML
    private TableView<ProductCategory> tableView;
    
    @FXML
    private TableColumn<ProductCategory, String> tcCategoryName;

    @FXML
    private TableColumn<ProductCategory, String> tcCategoryDesc;
    
    private ObservableList<ProductCategory> productCategoryTableData = FXCollections.observableArrayList();


    private BooleanProperty isDirty = new SimpleBooleanProperty(false);
    
	@Override
	public boolean shouldClose() {
		if (isDirty.get()) {
            ButtonType response = appUtils.shouldSaveUnsavedData(MainWindow);
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
		txtCategoryName.requestFocus();
	}

	@Override
	public boolean loadData() {
		 List<ProductCategory> list = productCategoryService.getAllCategories();
		 productCategoryTableData.addAll(list);
	     tableView.setItems(productCategoryTableData);
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
	public void initialize() {
		txtCategoryNameErrorMsg.managedProperty().bind(txtCategoryNameErrorMsg.visibleProperty());	
		txtCategoryNameErrorMsg.visibleProperty()
	     .bind(txtCategoryNameErrorMsg.textProperty().length().greaterThanOrEqualTo(1));
		
		tcCategoryName.setCellValueFactory(cellData -> cellData.getValue().categoryNameProperty());
        tcCategoryDesc.setCellValueFactory(cellData -> cellData.getValue().categoryDescProperty());
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
		 Tab tab = tabPane.selectionModelProperty().get()
                 .selectedItemProperty().get();
         tabPane.getTabs().remove(tab); //close the current tab
	}

	@Override
	public boolean validateInput() {
		boolean valid = true;
        // Category
        int category = txtCategoryName.getText().trim().length();
        if (category == 0) {
        	alertHelper.beep();
        	 txtCategoryNameErrorMsg.setText("Please enter category name!");
        	 txtCategoryName.requestFocus();
             valid = false;
        } else {
        	txtCategoryNameErrorMsg.setText("");
        }
        return valid;
	}
	
	@FXML
    void onAddCommand(ActionEvent event) {

    }

    @FXML
    void onCloseCommand(ActionEvent event) {

    }

    @FXML
    void onDeleteCommand(ActionEvent event) {

    }

    @FXML
    void onResetCommand(ActionEvent event) {
    	txtCategoryName.setText("");
    	txtCategoryDesc.setText("");
    }

    @FXML
    void onUpdateCommand(ActionEvent event) {

    }

}