package com.billing.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.billing.constants.AppConstants;
import com.billing.dto.BillDetails;
import com.billing.dto.Customer;
import com.billing.dto.InvoiceSearchCriteria;
import com.billing.dto.ItemDetails;
import com.billing.dto.ReturnDetails;
import com.billing.dto.StatusDTO;
import com.billing.dto.UserDetails;
import com.billing.main.AppContext;
import com.billing.service.CustomerService;
import com.billing.service.InvoiceService;
import com.billing.service.PrinterService;
import com.billing.service.ProductHistoryService;
import com.billing.service.SalesReturnService;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
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
public class SearchInvoiceController extends AppContext implements TabContent {

	private static final Logger logger = LoggerFactory.getLogger(SearchInvoiceController.class);

	private BooleanProperty isDirty = new SimpleBooleanProperty(false);

	private IntegerProperty matchingInvoicesCount = new SimpleIntegerProperty(0);

	@Autowired
	AlertHelper alertHelper;

	@Autowired
	AppUtils appUtils;

	@Autowired
	InvoiceService invoiceService;

	@Autowired
	SalesReturnService salesReturnService;

	@Autowired
	CustomerService customerService;

	@Autowired
	ProductHistoryService productHistoryService;

	@Autowired
	PrinterService printerService;

	private UserDetails userDetails;

	public Stage currentStage = null;

	private TabPane tabPane = null;

	private ObservableList<BillDetails> tableDataList;

	@FXML
	private RadioButton rbSearchByInvoiceNo;

	@FXML
	private TextField txtInvoiceNo;

	@FXML
	private RadioButton rbSearchByOtherCriteria;

	@FXML
	private HBox panelInvoiceNo;

	@FXML
	private VBox panelOtherCriteria;

	@FXML
	private HBox panelAmount;

	@FXML
	private VBox panelPayMode;

	@FXML
	private HBox panelDate;

	@FXML
	private CheckBox cbInvoiceDate;

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
	private CheckBox cbInvoiceAmount;

	@FXML
	private TextField txtStartAmount;

	@FXML
	private TextField txtEndAmount;

	@FXML
	private TableView<BillDetails> tableView;

	@FXML
	private TableColumn<BillDetails, String> tcInvoiceNo;

	@FXML
	private TableColumn<BillDetails, String> tcDate;

	@FXML
	private TableColumn<BillDetails, String> tcCustomer;

	@FXML
	private TableColumn<BillDetails, Double> tcAmount;

	@FXML
	private Label lblTotalOfInvoices;

	@FXML
	private Label lblErrInvoiceNo;

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

	@FXML
	void onCloseAction(ActionEvent event) {
		closeTab();
	}

	@FXML
	void onDelete(ActionEvent event) {
		BillDetails bill = tableView.getSelectionModel().getSelectedItem();
		if (!validateInputDelete(bill)) {
			alertHelper.beep();
			return;
		}
		Alert alert = alertHelper.showConfirmAlertWithYesNo(currentStage, null, "Are you sure to delete invoice ?");
		if (alert.getResult() == ButtonType.YES) {
			List<ItemDetails> itemList = invoiceService.getItemList(bill);
			bill.setItemDetails(itemList);
			StatusDTO status = invoiceService.delete(bill);
			if (status.getStatusCode() == 0) {
				alertHelper.showSuccessNotification("Invoice No. " + bill.getBillNumber() + " deleted successfully");
				removeDeletedRecord(bill);
			} else {
				alertHelper.showErrorNotification("Error occured while deleting inovice");
			}
		}
	}

	private boolean validateInputDelete(BillDetails bill) {
		if (bill == null) {
			return false;
		}
		StatusDTO isSalesReturned = salesReturnService.isSalesReturned(bill.getBillNumber());
		if (isSalesReturned.getStatusCode() == 0) {
			alertHelper.showErrorNotification("Sales Return available for this invoice. Delete action not allowed");
			return false;
		}
		if ("PENDING".equals(bill.getPaymentMode())) {
			Customer customer = customerService.getCustomer(bill.getCustomerId());
			if (customer.getBalanceAmt() < bill.getNetSalesAmt()) {
				alertHelper.showErrorNotification(
						"Customer's balance is less than invoice amount. Please check customer payment history");
				return false;
			}
		}

		return true;
	}

	private boolean validateInputEditInvoice(BillDetails bill) {
		if (bill == null) {
			return false;
		}
		StatusDTO isSalesReturned = salesReturnService.isSalesReturned(bill.getBillNumber());
		if (isSalesReturned.getStatusCode() == 0) {
			alertHelper.showErrorNotification("Sales Return available for this invoice. Edit action not allowed");
			return false;
		}
		if ("PENDING".equals(bill.getPaymentMode())) {
			Customer customer = customerService.getCustomer(bill.getCustomerId());
			if (customer.getBalanceAmt() < bill.getNetSalesAmt()) {
				alertHelper.showErrorNotification(
						"Customer balance is less than invoice amount. Please check customer payment history");
				return false;
			}
		}

		return true;
	}

	private boolean validateInputSalesRetun(BillDetails bill) {
		if (bill == null) {
			return false;
		}

		String allowedDays = appUtils.getAppDataValues(AppConstants.SALES_RETURN_ALLOWED_DAYS);
		DateTime dateTime = new DateTime();
		Date backDate = dateTime.minusDays(Integer.valueOf(allowedDays)).toDate();
		Date invoiceDate = appUtils.getDateFromDBTimestamp(bill.getTimestamp());
		System.out.println("backDate : "+backDate);
		System.out.println("invoiceDate : "+invoiceDate);

		if (backDate.after(invoiceDate)) {
			alertHelper.showErrorNotification("Return allowed days limit is expired for Invoice No. : "+bill.getBillNumber());
			return false;
		}

		ReturnDetails rd = salesReturnService.getReturnDetails(Integer.valueOf(bill.getBillNumber()));
		if (rd != null) {
			alertHelper.showErrorNotification(
					"Return No. : " + rd.getReturnNumber() + " already exists for Invoice No. : " + rd.getInvoiceNumber());
			return false;
		}

		return true;
	}

	private void removeDeletedRecord(BillDetails bill) {
		if (tableDataList.contains(bill)) {
			tableDataList.remove(bill);
		}
		int count = tableDataList.size();
		if (count == 0) {
			matchingInvoicesCount.set(count);
			panelSearchCriteria.setExpanded(true);
		} else {
			tableView.getSelectionModel().selectFirst();
			tableView.scrollTo(0);
			tableView.requestFocus();

		}

	}

	@FXML
	void onEditAction(ActionEvent event) {
		BillDetails bill = tableView.getSelectionModel().getSelectedItem();
		if (!validateInputEditInvoice(bill)) {
			alertHelper.beep();
			return;
		}

		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setControllerFactory(springContext::getBean);
		fxmlLoader.setLocation(this.getClass().getResource("/com/billing/gui/EditInvoice.fxml"));

		Parent rootPane = null;
		try {
			rootPane = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("getEditInvoicePopup Error in loading the view file :", e);
			alertHelper.beep();

			alertHelper.showErrorAlert(currentStage, "Error Occurred", "Error in creating user interface",
					"An error occurred in creating user interface " + "for the selected command");

			return;
		}

		final Scene scene = new Scene(rootPane);
		final EditInvoiceController controller = (EditInvoiceController) fxmlLoader.getController();
		controller.bill = bill;
		controller.setTask(() -> {
			afterSuccessTask();
		});
		final Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(currentStage);
		stage.setUserData(controller);
		stage.getIcons().add(new Image("/images/shop32X32.png"));
		stage.setScene(scene);
		stage.setTitle("Edit Invoice");
		controller.setMainWindow(stage);
		controller.setUserDetails(userDetails);
		controller.loadData();
		stage.showAndWait();
	}

	public void afterSuccessTask() {
		panelSearchCriteria.setExpanded(true);
		panelSearchResult.setExpanded(false);
	}

	@FXML
	void onPrintAction(ActionEvent event) {
		BillDetails bill = tableView.getSelectionModel().getSelectedItem();
		List<ItemDetails> itemList = invoiceService.getItemList(bill);
		bill.setItemDetails(itemList);
		printerService.printInvoice(bill);

	}

	@FXML
	void onAddReturnAction(ActionEvent event) {
		BillDetails bill = tableView.getSelectionModel().getSelectedItem();
		if (!validateInputSalesRetun(bill)) {
			alertHelper.beep();
			return;
		}
		getSalesReturnPopup(bill);
	}

	private void getSalesReturnPopup(BillDetails bill) {
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setControllerFactory(springContext::getBean);
		fxmlLoader.setLocation(this.getClass().getResource("/com/billing/gui/SalesReturn.fxml"));

		Parent rootPane = null;
		try {
			rootPane = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Sales Return popup Error in loading the view file :", e);
			alertHelper.beep();

			alertHelper.showErrorAlert(currentStage, "Error Occurred", "Error in creating user interface",
					"An error occurred in creating user interface " + "for the selected command");

			return;
		}

		final Scene scene = new Scene(rootPane);
		final SalesReturnController controller = (SalesReturnController) fxmlLoader.getController();
		controller.bill = bill;
		controller.setTask(() -> {
			afterSuccessTask();
		});
		final Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(currentStage);
		stage.setUserData(controller);
		stage.getIcons().add(new Image("/images/shop32X32.png"));
		stage.setScene(scene);
		stage.setTitle("Sales Return");
		controller.setMainWindow(stage);
		controller.setUserDetails(userDetails);
		controller.loadData();
		stage.showAndWait();
	}

	@FXML
	void onViewAction(ActionEvent event) {
		getViewInvoicePopup(tableView.getSelectionModel().getSelectedItem());
	}

	@FXML
	void onSearchInvoiceAction(ActionEvent event) {
		if (!validateInput()) {
			return;
		}
		tableDataList.clear();
		matchingInvoicesCount.set(0);
		InvoiceSearchCriteria criteria = getCriteria();
		List<BillDetails> invoiceResults = invoiceService.getSearchedInvoices(criteria);
		tableDataList.addAll(invoiceResults);
		int matchCount = invoiceResults.size();

		if (matchCount > 0) {
			panelSearchCriteria.setExpanded(false);
			matchingInvoicesCount.set(matchCount);
			panelSearchResult.setExpanded(true);
			tableView.getSelectionModel().selectFirst();
			tableView.scrollTo(0);
			tableView.requestFocus();
		} else {
			alertHelper.beep();
			alertHelper.showErrorAlert(currentStage, "No Match Found", "No matching invoice found",
					"No invoice matched your search criteria");
		}
	}

	private void getViewInvoicePopup(BillDetails bill) {
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setControllerFactory(springContext::getBean);
		fxmlLoader.setLocation(this.getClass().getResource("/com/billing/gui/ViewInvoice.fxml"));

		Parent rootPane = null;
		try {
			rootPane = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("getViewInvoicePopup Error in loading the view file :", e);
			alertHelper.beep();

			alertHelper.showErrorAlert(currentStage, "Error Occurred", "Error in creating user interface",
					"An error occurred in creating user interface " + "for the selected command");

			return;
		}

		final Scene scene = new Scene(rootPane);
		final ViewInvoiceController controller = (ViewInvoiceController) fxmlLoader.getController();
		controller.bill = bill;
		final Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(currentStage);
		stage.setUserData(controller);
		stage.getIcons().add(new Image("/images/shop32X32.png"));
		stage.setScene(scene);
		stage.setTitle("View Invoice");
		controller.loadData();
		stage.showAndWait();
	}

	private InvoiceSearchCriteria getCriteria() {
		InvoiceSearchCriteria criteria = new InvoiceSearchCriteria();

		if (rbSearchByInvoiceNo.isSelected()) {
			criteria.setInvoiceNumber(txtInvoiceNo.getText().trim());
		} else {
			if (cbInvoiceDate.isSelected()) {
				criteria.setStartDate(dpStartDate.getValue());
				criteria.setEndDate(dpEndDate.getValue());
			}

			if (cbCreditPendingInvoice.isSelected()) {
				if (rbPendingInvoice.isSelected()) {
					criteria.setPendingInvoice("Y");
				} else {
					criteria.setPendingInvoice("N");
				}
			}

			if (cbInvoiceAmount.isSelected()) {
				criteria.setStartAmount(txtStartAmount.getText().trim());
				criteria.setEndAmount(txtEndAmount.getText().trim());
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
		matchingInvoicesCount.set(0);
		ToggleGroup radioPaymode = new ToggleGroup();
		rbCashInvoice.setToggleGroup(radioPaymode);
		rbPendingInvoice.setToggleGroup(radioPaymode);
		tableDataList = FXCollections.observableArrayList();
		tableView.setItems(tableDataList);
		panelInvoiceNo.managedProperty().bind(panelInvoiceNo.visibleProperty());
		panelInvoiceNo.visibleProperty().bind(rbSearchByInvoiceNo.selectedProperty());

		panelOtherCriteria.managedProperty().bind(panelOtherCriteria.visibleProperty());
		panelOtherCriteria.visibleProperty().bind(rbSearchByOtherCriteria.selectedProperty());

		panelDate.managedProperty().bind(panelDate.visibleProperty());
		panelDate.visibleProperty().bind(cbInvoiceDate.selectedProperty());

		panelPayMode.managedProperty().bind(panelPayMode.visibleProperty());
		panelPayMode.visibleProperty().bind(cbCreditPendingInvoice.selectedProperty());

		panelAmount.managedProperty().bind(panelAmount.visibleProperty());
		panelAmount.visibleProperty().bind(cbInvoiceAmount.selectedProperty());

		dpStartDate.setValue(LocalDate.now());
		dpEndDate.setValue(LocalDate.now());
		txtInvoiceNo.textProperty().addListener(appUtils.getForceNumberListner());
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
		panelSearchResult.visibleProperty().bind(matchingInvoicesCount.greaterThan(0));

		errorLabels = new Label[] { lblErrCreditPending, lblErrEndAmt, lblErrEndDate, lblErrInvoiceNo, lblErrNoCriteria,
				lblErrStartAmt, lblErrStartDate };
		for (Label label : errorLabels) {
			label.managedProperty().bind(label.visibleProperty());
			label.visibleProperty().bind(label.textProperty().length().greaterThan(0));
		}

		setTableCellFactories();

		tableDataList.addListener(new ListChangeListener<BillDetails>() {

			@Override
			public void onChanged(ListChangeListener.Change<? extends BillDetails> c) {
				updateTotalAmount();
			}
		});
	}

	protected void updateTotalAmount() {
		Double result = 0.0;
		int invoiceCount = tableDataList.size();
		for (BillDetails b : tableDataList) {
			result = result + b.getNetSalesAmt();
		}

		lblTotalOfInvoices.setText(String.format("Total of %d invoice(s) is \u20b9 %s", invoiceCount,
				IndianCurrencyFormatting.applyFormatting(result)));
	}

	private void setTableCellFactories() {
		
		final Callback<TableColumn<BillDetails, Double>, TableCell<BillDetails, Double>> callback = new Callback<TableColumn<BillDetails, Double>, TableCell<BillDetails, Double>>() {

			@Override
			public TableCell<BillDetails, Double> call(TableColumn<BillDetails, Double> param) {
				TableCell<BillDetails, Double> tableCell = new TableCell<BillDetails, Double>() {

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
				cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getBillNumber())));
		tcDate.setCellValueFactory(cellData -> new SimpleStringProperty(
				appUtils.getFormattedDateWithTime(cellData.getValue().getTimestamp())));
		tcCustomer.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomerName()));
		tcAmount.setCellFactory(callback);

		tcInvoiceNo.getStyleClass().add("character-cell");
		tcDate.getStyleClass().add("character-cell");
		tcCustomer.getStyleClass().add("character-cell");
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
			String numberString = txtInvoiceNo.getText().trim();
			if (numberString.isEmpty()) {
				lblErrInvoiceNo.setText("Invoice number not specified");
				valid = false;
				return valid;
			}

			int no = Integer.valueOf(numberString);
			if (no == 0) {
				lblErrInvoiceNo.setText("Invoice number can't be zero");
				valid = false;
				return valid;
			}
		} else {
			if (!(cbInvoiceAmount.isSelected() || cbCreditPendingInvoice.isSelected() || cbInvoiceDate.isSelected())) {
				lblErrNoCriteria.setText("No criteria selected");
				return false;
			}

			if (cbCreditPendingInvoice.isSelected()) {
				if (!(rbCashInvoice.isSelected() || rbPendingInvoice.isSelected())) {
					lblErrCreditPending.setText("Invoice payment mode not selected");
					valid = false;
				}
			}

			if (cbInvoiceDate.isSelected() && !validateInvoiceDate()) {
				valid = false;
			}

			if (cbInvoiceAmount.isSelected() && !validateInvoiceAmount()) {
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
