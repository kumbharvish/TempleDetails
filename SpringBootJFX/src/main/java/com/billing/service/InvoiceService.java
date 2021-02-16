package com.billing.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.constants.AppConstants;
import com.billing.dto.BillDetails;
import com.billing.dto.InvoiceSearchCriteria;
import com.billing.dto.ItemDetails;
import com.billing.dto.StatusDTO;
import com.billing.repository.InvoiceRepository;

@Service
public class InvoiceService implements AppService<BillDetails> {

	@Autowired
	CustomerService customerService;

	@Autowired
	InvoiceRepository invoiceRepository;

	private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);

	@Override
	public StatusDTO add(BillDetails bill) {
		StatusDTO statusSaveInvoice = new StatusDTO();
		if (AppConstants.PENDING.equals(bill.getPaymentMode())) {
			// Save Bill Details
			StatusDTO status = invoiceRepository.saveInvoiceDetails(bill);
			StatusDTO statusAddPendingAmt = new StatusDTO(-1);
			if (status.getStatusCode() == 0) {
				String narration = "Invoice Amount based on No : " + bill.getBillNumber();
				statusAddPendingAmt = customerService.addCustomerPaymentHistory(bill.getCustomerId(),
						bill.getNetSalesAmt(), 0, AppConstants.CREDIT, narration);
			}
			if (status.getStatusCode() != 0 || statusAddPendingAmt.getStatusCode() != 0) {
				statusSaveInvoice.setStatusCode(-1);
			}
		}
		// Other than PENDING pay mode ex: cash,cheque etc
		if (!AppConstants.PENDING.equals(bill.getPaymentMode())) {
			// Save Bill Details
			StatusDTO status = invoiceRepository.saveInvoiceDetails(bill);
			if (status.getStatusCode() != 0) {
				statusSaveInvoice.setStatusCode(-1);
			}
		}

		return statusSaveInvoice;
	}

	@Override
	public StatusDTO update(BillDetails bill) {
		StatusDTO statusEditInvoice = new StatusDTO(-1);
		StatusDTO statusCustBlanceUpdate = new StatusDTO(-1);
		// Update Invoice Details
		StatusDTO status = invoiceRepository.editInvoiceDetails(bill);
		if (status.getStatusCode() == 0) {
			// Customer payment history
			if (AppConstants.PENDING.equals(bill.getPaymentMode())) {
				if (bill.getCopyCustId() == bill.getCustomerId()) {
					if (AppConstants.PENDING.equals(bill.getCopyPaymode())) {
						double newNetSalesAmt = bill.getCopyNetSalesAmt() - bill.getNetSalesAmt();
						if (newNetSalesAmt > 0) {

							String narration = "Edit Invoice correction based on Invoice No : " + bill.getBillNumber();
							statusCustBlanceUpdate = customerService.addCustomerPaymentHistory(
									bill.getCustomerId(), 0, newNetSalesAmt, AppConstants.DEBIT, narration);
						}
						if (newNetSalesAmt < 0) {

							String narration = "Edit Invoice correction based on Invoice No : " + bill.getBillNumber();
							statusCustBlanceUpdate = customerService.addCustomerPaymentHistory(
									bill.getCustomerId(), Math.abs(newNetSalesAmt), 0, AppConstants.CREDIT,
									narration);
						}
					} else {
						String narration = "Edit Invoice correction based on Invoice No : " + bill.getBillNumber();
						statusCustBlanceUpdate = customerService.addCustomerPaymentHistory(bill.getCustomerId(),
								bill.getNetSalesAmt(), 0, AppConstants.CREDIT, narration);
					}

				} else {
					// if Existing bill is pending then Debit Existing bill customer bill amount and
					// credit into new customer balance amount
					if (AppConstants.PENDING.equals(bill.getCopyPaymode())) {

						String narration = "Edit Invoice correction based on Invoice No : " + bill.getBillNumber();
						statusCustBlanceUpdate = customerService.addCustomerPaymentHistory(bill.getCopyCustId(), 0,
								bill.getCopyNetSalesAmt(), AppConstants.DEBIT, narration);

						String narrationCredit = "Edit Invoice correction based on Invoice No : "
								+ bill.getBillNumber();
						statusCustBlanceUpdate = customerService.addCustomerPaymentHistory(bill.getCustomerId(),
								bill.getNetSalesAmt(), 0, AppConstants.CREDIT, narrationCredit);

					} else {
						String narrationCredit = "Edit Invoice correction based on Invoice No : "
								+ bill.getBillNumber();
						statusCustBlanceUpdate = customerService.addCustomerPaymentHistory(bill.getCustomerId(),
								bill.getNetSalesAmt(), 0, AppConstants.CREDIT, narrationCredit);
					}
				}
			} else {
				if (bill.getCopyCustId() == bill.getCustomerId()) {
					if (AppConstants.PENDING.equals(bill.getCopyPaymode())) {

						String narration = "Edit Invoice correction based on Invoice No : " + bill.getBillNumber();
						statusCustBlanceUpdate = customerService.addCustomerPaymentHistory(bill.getCopyCustId(), 0,
								bill.getCopyNetSalesAmt(), AppConstants.DEBIT, narration);
					} else {
						statusEditInvoice.setStatusCode(0);
						System.out.println("------------- Update Code Here 1 ----------------");
						return statusEditInvoice;
					}
				} else {
					if (AppConstants.PENDING.equals(bill.getCopyPaymode())) {

						String narration = "Edit Invoice correction based on Invoice No : " + bill.getBillNumber();
						statusCustBlanceUpdate = customerService.addCustomerPaymentHistory(bill.getCopyCustId(), 0,
								bill.getCopyNetSalesAmt(), AppConstants.DEBIT, narration);

					} else {
						System.out.println("------------- Update Code Here 2 ----------------");
						statusEditInvoice.setStatusCode(0);
						return statusEditInvoice;
					}
				}

			}
		}

		if (status.getStatusCode() == 0 && statusCustBlanceUpdate.getStatusCode() == 0) {
			System.out.println("------------- Both True ----------------");

			statusEditInvoice.setStatusCode(0);
		}
		return statusEditInvoice;
	}

	@Override
	public StatusDTO delete(BillDetails bill) {
		StatusDTO statusCustBlanceUpdate = new StatusDTO(-1);
		StatusDTO statusDeleteBill = new StatusDTO(-1);
		// Pending Invoice
		if (AppConstants.PENDING.equals(bill.getPaymentMode())) {
			statusDeleteBill = invoiceRepository.deleteInvoiceDetails(bill);
			if (statusDeleteBill.getStatusCode() == 0) {
				String narration = "Delete Invoice adjustment based on Invoice No : " + bill.getBillNumber();
				statusCustBlanceUpdate = customerService.addCustomerPaymentHistory(bill.getCustomerId(), 0,
						bill.getNetSalesAmt(), AppConstants.DEBIT, narration);

				if (statusCustBlanceUpdate.getStatusCode() != 0) {
					statusDeleteBill.setStatusCode(-1);
				}
			}
		}
		// Cash Bill
		if (!AppConstants.PENDING.equals(bill.getPaymentMode())) {
			statusDeleteBill = invoiceRepository.deleteInvoiceDetails(bill);
		}
		return statusDeleteBill;
	}

	public List<ItemDetails> getItemList(BillDetails bill) {
		return invoiceRepository.getItemDetails(bill.getBillNumber());
	}

	public List<BillDetails> getSearchedInvoices(InvoiceSearchCriteria criteria) {
		return invoiceRepository.getSearchedInvoices(criteria);
	}

	public List<BillDetails> getBillDetails(String fromDate, String toDate) {
		return invoiceRepository.getBillDetails(fromDate, toDate);
	}

	public Integer getNewInvoiceNumber() {
		return invoiceRepository.getNewInvoiceNumber();
	}

	@Override
	public List<BillDetails> getAll() {
		// TODO Auto-generated method stub
		return null;
	}
}
