package com.billing.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.dto.ItemDetails;
import com.billing.dto.PurchaseEntry;
import com.billing.dto.PurchaseEntrySearchCriteria;
import com.billing.dto.StatusDTO;
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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

@Controller
public class SearchPurchaseEntryController extends AppContext implements TabContent {

	private static final Logger logger = LoggerFactory.getLogger(SearchPurchaseEntryController.class);

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	private IntegerProperty matchingPurEntryCount = new SimpleIntegerProperty(0);

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	PurchaseEntryService purchaseEntryService;

	@Autowired
	SupplierService supplierService;

	@Autowired
	AppUtils appUtils;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	private ObservableList<PurchaseEntry> tableDataList;

	@FXML
	private RadioButton rbSearchByInvoiceNo;

	@FXML
	private TextField txtPENo;

	@FXML
	private RadioButton rbSearchByOtherCriteria;

	@FXML
	private HBox panelInvoiceNo;

	@FXML
	private VBox panelOtherCriteria;

	@FXML
	private HBox panelAmount;

	@FXML
	private HBox panelSupplier;

	@FXML
	private CheckBox cbSupplier;

	@FXML
	private VBox panelPayMode;

	@FXML
	private HBox panelDate;

	@FXML
	private CheckBox cbPEDate;

	@FXML
	private AutoCompleteTextField txtSuppliers;

	@FXML
	private Label lblErrSupplier;

	@FXML
	private DatePicker dpStartDate;

	@FXML
	private DatePicker dpEndDate;

	@FXML
	private CheckBox cbCreditPendingInvoice;

	@FXML
	private RadioButton rbCashInvoice;

	@FXML
	private Button btnSearchInvoice;

	@FXML
	private RadioButton rbPendingInvoice;

	@FXML
	private CheckBox cbPEAmount;

	@FXML
	private TextField txtStartAmount;

	@FXML
	private TextField txtEndAmount;

	@FXML
	private TableView<PurchaseEntry> tableView;

	@FXML
	private TableColumn<PurchaseEntry, String> tcInvoiceNo;

	@FXML
	private TableColumn<PurchaseEntry, String> tcDate;

	@FXML
	private TableColumn<PurchaseEntry, String> tcSupplier;

	@FXML
	private TableColumn<PurchaseEntry, Double> tcAmount;

	@FXML
	private Label lblTotalOfPurEntires;

	@FXML
	private Label lblErrPENo;

	@FXML
	private Label lblErrStartDate;

	@FXML
	private Label lblErrEndDate;

	@FXML
	private Label lblErrCreditPending;

	@FXML
	private Label lblErrStartAmt;

	@FXML
	private Label lblErrEndAmt;

	@FXML
	private Label lblErrNoCriteria;

	@FXML
	private TitledPane panelSearchResult;

	@FXML
	private TitledPane panelSearchCriteria;

	private Label[] errorLabels = null;

	private SortedSet<String> entries;

	private HashMap<String, Supplier> supplierMap;

	@FXML
	void onCloseAction(ActionEvent event) {
		closeTab();
	}

	@FXML
	void onDelete(ActionEvent event) {
		PurchaseEntry purchaseEntry = tableView.getSelectionModel().getSelectedItem();
		if (!validateInputDelete(purchaseEntry)) {
			alertHelper.beep();
			return;
		}
		Alert alert = alertHelper.showConfirmAlertWithYesNo(currentStage, null,
				"Are you sure to delete purchase entry ?");
		if (alert.getResult() == ButtonType.YES) {
			List<ItemDetails> itemList = purchaseEntryService.getItemList(purchaseEntry);
			purchaseEntry.setItemDetails(itemList);
			StatusDTO status = purchaseEntryService.delete(purchaseEntry);
			if (status.getStatusCode() == 0) {
				alertHelper.showSuccessNotification(
						"Purchase Entry No. " + purchaseEntry.getPurchaseEntryNo() + " deleted successfully");
				removeDeletedRecord(purchaseEntry);
			} else {
				alertHelper.showErrorNotification("Error occured while deleting purchase entry");
			}
		}
	}

	private boolean validateInputDelete(PurchaseEntry pe) {
		if (pe == null) {
			return false;
		}

		if ("PENDING".equals(pe.getPaymentMode())) {
			Supplier supplier = supplierService.getSupplier(pe.getSupplierId());
			if (supplier.getBalanceAmount() < pe.getTotalAmount()) {
				alertHelper.showErrorNotification(
						"Supplier's balance is less than purchase entry amount. Please check supplier payment history");
				return false;
			}
		}

		return true;
	}

	private void removeDeletedRecord(PurchaseEntry purchaseEntry) {
		if (tableDataList.contains(purchaseEntry)) {
			tableDataList.remove(purchaseEntry);
		}
		int count = tableDataList.size();
		if (count == 0) {
			matchingPurEntryCount.set(count);
			panelSearchCriteria.setExpanded(true);
		} else {
			tableView.getSelectionModel().selectFirst();
			tableView.scrollTo(0);
			tableView.requestFocus();

		}

	}

	@FXML
	void onViewAction(ActionEvent event) {
		getViewPurEntryPopup(tableView.getSelectionModel().getSelectedItem());
	}

	@FXML
	void onSearchPurchaseEntryAction(ActionEvent event) {
		if (!validateInput()) {
			return;
		}
		tableDataList.clear();
		matchingPurEntryCount.set(0);
		PurchaseEntrySearchCriteria criteria = getCriteria();
		List<PurchaseEntry> invoiceResults = purchaseEntryService.getSearchedPurchaseEntry(criteria);
		tableDataList.addAll(invoiceResults);
		int matchCount = invoiceResults.size();

		if (matchCount > 0) {
			panelSearchCriteria.setExpanded(false);
			matchingPurEntryCount.set(matchCount);
			panelSearchResult.setExpanded(true);
			tableView.getSelectionModel().selectFirst();
			tableView.scrollTo(0);
			tableView.requestFocus();
		} else {
			alertHelper.beep();
			alertHelper.showErrorAlert(currentStage, "No Match Found", "No matching purchase entry found",
					"No purchase entry matched your search criteria");
		}
	}

	private void getViewPurEntryPopup(PurchaseEntry purchaseEntry) {
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setControllerFactory(springContext::getBean);
		fxmlLoader.setLocation(this.getClass().getResource("/com/billing/gui/ViewPurchaseEntry.fxml"));

		Parent rootPane = null;
		try {
			rootPane = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("getViewPurEntryPopup Error in loading the view file :", e);
			alertHelper.beep();

			alertHelper.showErrorAlert(currentStage, "Error Occurred", "Error in creating user interface",
					"An error occurred in creating user interface " + "for the selected command");

			return;
		}

		final Scene scene = new Scene(rootPane);
		final ViewPurchaseEntryController controller = (ViewPurchaseEntryController) fxmlLoader.getController();
		controller.purchaseEntry = purchaseEntry;
		final Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(currentStage);
		stage.setUserData(controller);
		stage.getIcons().add(new Image("/images/shop32X32.png"));
		stage.setScene(scene);
		stage.setTitle("View Purchase Entry");
		controller.loadData();
		stage.showAndWait();
	}

	private PurchaseEntrySearchCriteria getCriteria() {
		PurchaseEntrySearchCriteria criteria = new PurchaseEntrySearchCriteria();

		if (rbSearchByInvoiceNo.isSelected()) {
			criteria.setPurchaseEntryNo(txtPENo.getText().trim());
		} else {
			if (cbPEDate.isSelected()) {
				criteria.setStartDate(dpStartDate.getValue());
				criteria.setEndDate(dpEndDate.getValue());
			}

			if (cbCreditPendingInvoice.isSelected()) {
				if (rbPendingInvoice.isSelected()) {
					criteria.setPendingPurchaseEntry("Y");
				} else {
					criteria.setPendingPurchaseEntry("N");
				}
			}

			if (cbPEAmount.isSelected()) {
				criteria.setStartAmount(txtStartAmount.getText().trim());
				criteria.setEndAmount(txtEndAmount.getText().trim());
			}

			if (cbSupplier.isSelected()) {
				criteria.setSupplierId(supplierMap.get(txtSuppliers.getText()).getSupplierID());
			}
		}

		return criteria;
	}

	@Override
	public boolean shouldClose() {
		return true;
	}

	@Override
	public void putFocusOnNode() {
		rbSearchByInvoiceNo.requestFocus();
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
		this.tabPane = pane;
	}

	@Override
	public void setUserDetails(UserDetails user) {
		userDetails = user;
	}

	@Override
	public void initialize() {
		ToggleGroup searchCriteriaGroup = new ToggleGroup();
		rbSearchByInvoiceNo.setToggleGroup(searchCriteriaGroup);
		rbSearchByOtherCriteria.setToggleGroup(searchCriteriaGroup);
		matchingPurEntryCount.set(0);
		ToggleGroup radioPaymode = new ToggleGroup();
		rbCashInvoice.setToggleGroup(radioPaymode);
		rbPendingInvoice.setToggleGroup(radioPaymode);
		tableDataList = FXCollections.observableArrayList();
		tableView.setItems(tableDataList);
		getSuppliersName();
		txtSuppliers.createTextField(entries, () -> {
		});

		panelInvoiceNo.managedProperty().bind(panelInvoiceNo.visibleProperty());
		panelInvoiceNo.visibleProperty().bind(rbSearchByInvoiceNo.selectedProperty());

		panelOtherCriteria.managedProperty().bind(panelOtherCriteria.visibleProperty());
		panelOtherCriteria.visibleProperty().bind(rbSearchByOtherCriteria.selectedProperty());

		panelDate.managedProperty().bind(panelDate.visibleProperty());
		panelDate.visibleProperty().bind(cbPEDate.selectedProperty());

		panelSupplier.managedProperty().bind(panelSupplier.visibleProperty());
		panelSupplier.visibleProperty().bind(cbSupplier.selectedProperty());

		panelPayMode.managedProperty().bind(panelPayMode.visibleProperty());
		panelPayMode.visibleProperty().bind(cbCreditPendingInvoice.selectedProperty());

		panelAmount.managedProperty().bind(panelAmount.visibleProperty());
		panelAmount.visibleProperty().bind(cbPEAmount.selectedProperty());

		dpStartDate.setValue(LocalDate.now());
		dpEndDate.setValue(LocalDate.now());
		txtPENo.textProperty().addListener(appUtils.getForceNumberListner());
		dpStartDate.valueProperty().addListener((observable, oldDate, newDate) -> {
			LocalDate today = LocalDate.now();
			if (newDate == null) {
				dpStartDate.setValue(today);
			}
		});

		dpEndDate.valueProperty().addListener((observable, oldDate, newDate) -> {
			LocalDate today = LocalDate.now();
			if (newDate == null) {
				dpEndDate.setValue(today);
			}
		});

		btnSearchInvoice.disableProperty().bind(searchCriteriaGroup.selectedToggleProperty().isNull());

		panelSearchResult.managedProperty().bind(panelSearchResult.visibleProperty());
		panelSearchResult.visibleProperty().bind(matchingPurEntryCount.greaterThan(0));

		errorLabels = new Label[] { lblErrCreditPending, lblErrEndAmt, lblErrEndDate, lblErrPENo, lblErrNoCriteria,
				lblErrStartAmt, lblErrStartDate, lblErrSupplier };
		for (Label label : errorLabels) {
			label.managedProperty().bind(label.visibleProperty());
			label.visibleProperty().bind(label.textProperty().length().greaterThan(0));
		}

		setTableCellFactories();

		tableDataList.addListener(new ListChangeListener<PurchaseEntry>() {

			@Override
			public void onChanged(ListChangeListener.Change<? extends PurchaseEntry> c) {
				updateTotalAmount();
			}
		});
	}

	public void getSuppliersName() {
		entries = new TreeSet<String>();
		supplierMap = new HashMap<String, Supplier>();
		for (Supplier supplier : supplierService.getAll()) {
			entries.add(supplier.getSupplierName());
			supplierMap.put(supplier.getSupplierName(), supplier);
		}
	}

	protected void updateTotalAmount() {
		Double result = 0.0;
		int purchaseEntryCount = tableDataList.size();
		for (PurchaseEntry b : tableDataList) {
			result = result + b.getTotalAmount();
		}

		lblTotalOfPurEntires.setText(String.format("Total of %d Purchase Entry(s) is \u20b9 %s", purchaseEntryCount,
				IndianCurrencyFormatting.applyFormatting(result)));
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
		tcInvoiceNo.setCellValueFactory(
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getPurchaseEntryNo())));
		tcDate.setCellValueFactory(cellData -> new SimpleStringProperty(
				appUtils.getFormattedDateWithTime(cellData.getValue().getPurchaseEntryDate())));
		tcSupplier.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSupplierName()));
		tcAmount.setCellFactory(callback);

		tcInvoiceNo.getStyleClass().add("character-cell");
		tcDate.getStyleClass().add("character-cell");
		tcSupplier.getStyleClass().add("character-cell");
	}

	@Override
	public boolean saveData() {
		return true;
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

	private void clearErrorLabels() {
		for (Label label : errorLabels) {
			label.setText("");
		}
	}

	@Override
	public boolean validateInput() {
		boolean valid = true;
		clearErrorLabels();
		if (rbSearchByInvoiceNo.isSelected()) {
			String numberString = txtPENo.getText().trim();
			if (numberString.isEmpty()) {
				lblErrPENo.setText("P.E. number not specified");
				valid = false;
				return valid;
			}

			int no = Integer.valueOf(numberString);
			if (no == 0) {
				lblErrPENo.setText("P.E. number can't be zero");
				valid = false;
				return valid;
			}
		} else {
			if (!(cbSupplier.isSelected() || cbPEAmount.isSelected() || cbCreditPendingInvoice.isSelected()
					|| cbPEDate.isSelected())) {
				lblErrNoCriteria.setText("No criteria selected");
				return false;
			}

			if (cbSupplier.isSelected()) {
				String numberString = txtSuppliers.getText().trim();
				if (numberString.isEmpty()) {
					lblErrSupplier.setText("Please select supplier");
					valid = false;
					return valid;
				} else if (null == supplierMap.get(txtSuppliers.getText())) {
					lblErrSupplier.setText("No supplier matches this name");
					txtSuppliers.requestFocus();
					valid = false;
					return valid;
				}
			}

			if (cbCreditPendingInvoice.isSelected()) {
				if (!(rbCashInvoice.isSelected() || rbPendingInvoice.isSelected())) {
					lblErrCreditPending.setText("Purchase Entry payment mode not selected");
					valid = false;
				}
			}

			if (cbPEDate.isSelected() && !validateInvoiceDate()) {
				valid = false;
			}

			if (cbPEAmount.isSelected() && !validateInvoiceAmount()) {
				valid = false;
			}
		}

		return valid;
	}

	private boolean validateInvoiceDate() {
		boolean valid = true;

		LocalDate startDate = dpStartDate.getValue();
		if (startDate == null) {
			lblErrStartDate.setText("Start date not specified");
			valid = false;
		}
		LocalDate endDate = dpEndDate.getValue();
		if (endDate == null) {
			lblErrEndDate.setText("End date not specified");
			valid = false;
		}

		if (valid && startDate.isAfter(endDate)) {
			lblErrStartDate.setText("Start date can't be later than the end date");
			valid = false;
		}
		return valid;
	}

	private boolean validateInvoiceAmount() {
		boolean valid = true;

		BigDecimal startAmount = null;
		String startAmountString = txtStartAmount.getText().trim();

		if (startAmountString.isEmpty()) {
			lblErrStartAmt.setText("Start amount not specified");
			valid = false;
		} else {
			try {
				startAmount = new BigDecimal(startAmountString);
			} catch (NumberFormatException e) {
				lblErrStartAmt.setText("Not a valid amount");
				valid = false;
			}
		}

		BigDecimal endAmount = null;
		String endAmountString = txtEndAmount.getText().trim();

		if (endAmountString.isEmpty()) {
			lblErrEndAmt.setText("End amount not specified");
			valid = false;
		} else {
			try {
				endAmount = new BigDecimal(endAmountString);
			} catch (NumberFormatException e) {
				lblErrEndAmt.setText("Not a valid amount");
				valid = false;
			}

		}

		if (valid && startAmount.compareTo(endAmount) == 1) {
			lblErrStartAmt.setText("Start amount can't be greater than the end amount");
			valid = false;
		}

		return valid;
	}
}
