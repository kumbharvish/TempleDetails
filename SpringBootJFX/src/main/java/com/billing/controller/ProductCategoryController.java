package com.billing.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.Product;
import com.billing.dto.ProductCategory;
import com.billing.dto.StatusDTO;
import com.billing.dto.UserDetails;
import com.billing.service.ProductCategoryService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
public class ProductCategoryController implements TabContent {

	@Autowired
	ProductCategoryService productCategoryService;

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	@FXML
	private TextField txtCategoryName;

	@FXML
	private Label txtCategoryNameErrorMsg;

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

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	private int categoryCode = 0;

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
		txtCategoryName.requestFocus();
	}

	@Override
	public boolean loadData() {
		List<ProductCategory> list = productCategoryService.getAll();
		ObservableList<ProductCategory> productCategoryTableData = FXCollections.observableArrayList();
		productCategoryTableData.addAll(list);
		tableView.setItems(productCategoryTableData);
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
		txtCategoryNameErrorMsg.managedProperty().bind(txtCategoryNameErrorMsg.visibleProperty());
		txtCategoryNameErrorMsg.visibleProperty()
				.bind(txtCategoryNameErrorMsg.textProperty().length().greaterThanOrEqualTo(1));
		setTableCellFactories();
		tableView.getSelectionModel().selectedItemProperty().addListener(this::onSelectedRowChanged);
		categoryCode = 0;
	}

	private void setTableCellFactories() {
		tcCategoryName.setCellValueFactory(cellData -> cellData.getValue().categoryNameProperty());
		tcCategoryDesc.setCellValueFactory(cellData -> cellData.getValue().categoryDescProperty());
		// Set CSS
		tcCategoryName.getStyleClass().add("character-cell");
		tcCategoryDesc.getStyleClass().add("character-cell");

	}

	public void onSelectedRowChanged(ObservableValue<? extends ProductCategory> observable, ProductCategory oldValue,
			ProductCategory newValue) {
		if (newValue != null) {
			txtCategoryName.setText(newValue.getCategoryName());
			txtCategoryDesc.setText(newValue.getCategoryDescription());
			categoryCode = newValue.getCategoryCode();
		}
	}

	@Override
	public boolean saveData() {
		ProductCategory productCategory = new ProductCategory();
		productCategory.setCategoryName(txtCategoryName.getText());
		productCategory.setCategoryDescription(txtCategoryDesc.getText());
		StatusDTO status = productCategoryService.add(productCategory);
		if (status.getStatusCode() == 0) {
			restFields();
			loadData();
			alertHelper.showSuccessNotification("Category added successfully");
		} else {
			if (status.getException().contains("UNIQUE")) {
				alertHelper.showErrorNotification("Entered category name already exists");
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
		// Category
		int category = txtCategoryName.getText().trim().length();
		if (category == 0) {
			alertHelper.beep();
			txtCategoryNameErrorMsg.setText("Please enter category name");
			txtCategoryName.requestFocus();
			valid = false;
		} else {
			txtCategoryNameErrorMsg.setText("");
		}
		return valid;
	}

	@FXML
	void onAddCommand(ActionEvent event) {
		if (categoryCode == 0) {
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
		if (categoryCode == 0) {
			alertHelper.showErrorNotification("Please select category");
		} else {
			Alert alert = alertHelper.showConfirmAlertWithYesNo(currentStage, null, "Are you sure?");
			if (alert.getResult() == ButtonType.YES) {
				ProductCategory productCategory = new ProductCategory();
				productCategory.setCategoryCode(categoryCode);
				List<Product> productList = productCategoryService.getProductsUnderCategory(productCategory);
				if (productList.size() > 0) {
					alertHelper.showErrorAlert(currentStage, "Error", null, "Total " + productList.size()
							+ " Products under this category. Please delete the products first in order to delete the category.");
				} else {
					StatusDTO status = productCategoryService.delete(productCategory);
					if (status.getStatusCode() == 0) {
						alertHelper.showSuccessNotification("Category deleted successfully");
						loadData();
						restFields();
					} else {
						alertHelper.showDataDeleteErrAlert(currentStage);
					}

				}

			} else {
				restFields();
			}

		}
	}

	@FXML
	void onResetCommand(ActionEvent event) {
		restFields();
		tableView.getSelectionModel().clearSelection();
	}

	private void restFields() {
		txtCategoryName.setText("");
		txtCategoryDesc.setText("");
		categoryCode = 0;
	}

	@FXML
	void onUpdateCommand(ActionEvent event) {
		if (categoryCode == 0) {
			alertHelper.showErrorNotification("Please select category");
		} else {
			if (!validateInput()) {
				return;
			}
			updateData();
		}
	}

	private void updateData() {
		ProductCategory productCategory = new ProductCategory();
		productCategory.setCategoryCode(categoryCode);
		productCategory.setCategoryName(txtCategoryName.getText());
		productCategory.setCategoryDescription(txtCategoryDesc.getText());

		StatusDTO status = productCategoryService.update(productCategory);
		if (status.getStatusCode() == 0) {
			restFields();
			loadData();
			alertHelper.showSuccessNotification("Category updated successfully");
		} else {
			if (status.getException().contains("UNIQUE")) {
				alertHelper.showErrorNotification("Entered category name already exists");
			} else {
				alertHelper.showDataSaveErrAlert(currentStage);
			}
		}
	}

	@Override
	public void setUserDetails(UserDetails user) {
		// TODO Auto-generated method stub

	}

}