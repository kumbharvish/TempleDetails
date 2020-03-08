package com.billing.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.PurchaseEntry;
import com.billing.dto.PurchaseEntrySearchCriteria;
import com.billing.dto.Supplier;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.PurchaseEntryService;
import com.billing.service.SupplierService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.AutoCompleteTextField;
import com.billing.utils.IndianCurrencyFormatting;
import com.billing.utils.TabContent;

import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.util.Callback;

@Controller
public class PurchaseEntryWiseProfitReportController extends AppContext implements TabContent {

	private static final Logger logger = LoggerFactory.getLogger(PurchaseEntryWiseProfitReportController.class);

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	@Autowired
	PurchaseEntryService purchaseEntryService;

	@Autowired
	SupplierService supplierService;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	ObservableList<PurchaseEntry> productProfitList;

	@FXML
	private AutoCompleteTextField txtSuppliers;

	@FXML
	private DatePicker dpFromDate;

	@FXML
	private DatePicker dpToDate;

	@FXML
	private TableView<PurchaseEntry> tableView;

	@FXML
	private TableColumn<PurchaseEntry, String> tcSupplierName;

	@FXML
	private TableColumn<PurchaseEntry, String> tcPurchaseEntryNo;

	@FXML
	private TableColumn<PurchaseEntry, String> tcDate;

	@FXML
	private TableColumn<PurchaseEntry, Double> tcTotalQty;

	@FXML
	private TableColumn<PurchaseEntry, Double> tcTotalAmount;

	@FXML
	private TableColumn<PurchaseEntry, Double> tcProfitAmount;

	private SortedSet<String> entries;

	private HashMap<String, Supplier> supplierMap;

	@Override
	public boolean shouldClose() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void putFocusOnNode() {
		dpFromDate.requestFocus();
	}

	@Override
	public boolean loadData() {
		productProfitList.clear();
		PurchaseEntrySearchCriteria criteria = new PurchaseEntrySearchCriteria();
		criteria.setStartDate(dpFromDate.getValue());
		criteria.setEndDate(dpToDate.getValue());
		if (txtSuppliers.getText() != "" && supplierMap.get(txtSuppliers.getText()) != null) {
			criteria.setSupplierId(supplierMap.get(txtSuppliers.getText()).getSupplierID());
		}
		List<PurchaseEntry> purchaseEntryList = purchaseEntryService.getSearchedPurchaseEntry(criteria);
		productProfitList.addAll(purchaseEntryList);
		return true;
	}

	public void getSuppliersName() {
		entries = new TreeSet<String>();
		supplierMap = new HashMap<String, Supplier>();
		for (Supplier supplier : supplierService.getAll()) {
			entries.add(supplier.getSupplierName());
			supplierMap.put(supplier.getSupplierName(), supplier);
		}
	}

	private void setTableCellFactories() {

		final Callback<TableColumn<PurchaseEntry, Double>, TableCell<PurchaseEntry, Double>> callback = new Callback<TableColumn<PurchaseEntry, Double>, TableCell<PurchaseEntry, Double>>() {

			@Override
			public TableCell<PurchaseEntry, Double> call(TableColumn<PurchaseEntry, Double> param) {
				TableCell<PurchaseEntry, Double> tableCell = new TableCell<PurchaseEntry, Double>() {

					@Override
					protected void updateItem(Double item, boolean empty) {
						super.updateItem(item, empty);
						if (empty) {
							super.setText(null);
						} else {
							super.setText(IndianCurrencyFormatting.applyFormatting(item));
						}
					}

				};
				tableCell.getStyleClass().add("numeric-cell");
				return tableCell;
			}
		};

		tcSupplierName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSupplierName()));
		tcPurchaseEntryNo.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getPurchaseEntryNo())));
		tcDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPurchaseEntryDate()));

		tcTotalQty.setCellFactory(callback);
		tcProfitAmount.setCellFactory(callback);
		tcTotalAmount.setCellFactory(callback);

		tcSupplierName.getStyleClass().add("character-cell");
		tcPurchaseEntryNo.getStyleClass().add("character-cell");
		tcDate.getStyleClass().add("character-cell");
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
	public void initialize() {
		getSuppliersName();
		txtSuppliers.createTextField(entries, () -> loadData());
		setTableCellFactories();
		productProfitList = FXCollections.observableArrayList();
		tableView.setItems(productProfitList);
		dpFromDate.setValue(LocalDate.now());
		dpToDate.setValue(LocalDate.now());
		dpFromDate.setDayCellFactory(this::getDateCell);
		dpFromDate.valueProperty().addListener((observable, oldDate, newDate) -> {
			LocalDate today = LocalDate.now();
			if (newDate == null || newDate.isAfter(today)) {
				dpFromDate.setValue(today);
			}
			loadData();
		});
		dpToDate.setDayCellFactory(this::getDateCell);
		dpToDate.valueProperty().addListener((observable, oldDate, newDate) -> {
			LocalDate today = LocalDate.now();
			if (newDate == null || newDate.isAfter(today)) {
				dpToDate.setValue(today);
			}
			loadData();
		});

	}

	@Override
	public boolean saveData() {
		return true;
	}

	@Override
	public void invalidated(Observable observable) {

	}

	@FXML
	void onCloseAction(ActionEvent event) {
		closeTab();
	}

	@Override
	public void closeTab() {
		Tab tab = tabPane.selectionModelProperty().get().selectedItemProperty().get();
		tabPane.getTabs().remove(tab); // close the current tab
	}

	@Override
	public boolean validateInput() {
		if (productProfitList.size() == 0) {
			alertHelper.showErrorNotification("No records to export");
			return false;
		}
		return true;
	}

	private DateCell getDateCell(DatePicker datePicker) {
		return appUtils.getDateCell(datePicker, null, LocalDate.now());
	}

}
