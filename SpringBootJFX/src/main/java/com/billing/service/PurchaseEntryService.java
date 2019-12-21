package com.billing.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.constants.AppConstants;
import com.billing.dto.GSTDetails;
import com.billing.dto.ItemDetails;
import com.billing.dto.Product;
import com.billing.dto.PurchaseEntry;
import com.billing.dto.PurchaseEntrySearchCriteria;
import com.billing.dto.StatusDTO;
import com.billing.repository.PurchaseEntryRepository;

@Service
public class PurchaseEntryService implements AppService<PurchaseEntry> {

	@Autowired
	PurchaseEntryRepository purchaseEntryRepository;

	@Autowired
	TaxesService taxesService;

	@Autowired
	SupplierService supplierService;

	@Override
	public StatusDTO add(PurchaseEntry purchaseEntry) {
		StatusDTO addPurchaseEntry = new StatusDTO();
		if (AppConstants.PENDING.equals(purchaseEntry.getPaymentMode())) {
			// Save Purchase Entry Details
			StatusDTO status = purchaseEntryRepository.addPurchaseEntry(purchaseEntry);
			StatusDTO statusAddPendingAmt = new StatusDTO(-1);
			if (status.getStatusCode() == 0) {
				String narration = "Purchase Entry Amount based on No : " + purchaseEntry.getPurchaseEntryNo();
				statusAddPendingAmt = supplierService.addSupplierPaymentHistory(purchaseEntry.getSupplierId(),
						purchaseEntry.getTotalAmount(), 0, AppConstants.CREDIT, narration);
			}
			if (status.getStatusCode() != 0 || statusAddPendingAmt.getStatusCode() != 0) {
				addPurchaseEntry.setStatusCode(-1);
			}
		}
		// Other than PENDING pay mode ex: cash,cheque etc
		if (!AppConstants.PENDING.equals(purchaseEntry.getPaymentMode())) {
			// Save Purchase Entry Details
			StatusDTO status = purchaseEntryRepository.addPurchaseEntry(purchaseEntry);
			if (status.getStatusCode() != 0) {
				addPurchaseEntry.setStatusCode(-1);
			}
		}

		return addPurchaseEntry;

	}

	@Override
	public StatusDTO update(PurchaseEntry purchaseEntry) {
		return null;
	}

	@Override
	public StatusDTO delete(PurchaseEntry purchaseEntry) {
		StatusDTO statusSupplierBlance = new StatusDTO(-1);
		StatusDTO statusDelete = new StatusDTO(-1);
		// Pending Purchase Entry
		if (AppConstants.PENDING.equals(purchaseEntry.getPaymentMode())) {
			statusDelete = purchaseEntryRepository.deletePurchaseEntryDetails(purchaseEntry);
			if (statusDelete.getStatusCode() == 0) {
				String narration = "Delete Purchase Entry adjustment based on No : "
						+ purchaseEntry.getPurchaseEntryNo();
				statusSupplierBlance = supplierService.addSupplierPaymentHistory(purchaseEntry.getSupplierId(), 0,
						purchaseEntry.getTotalAmount(), AppConstants.DEBIT, narration);

				if (statusSupplierBlance.getStatusCode() != 0) {
					statusDelete.setStatusCode(-1);
				}
			}
		}
		// Cash Purchase Entry
		if (!AppConstants.PENDING.equals(purchaseEntry.getPaymentMode())) {
			statusDelete = purchaseEntryRepository.deletePurchaseEntryDetails(purchaseEntry);
		}
		return statusDelete;
	}

	@Override
	public List<PurchaseEntry> getAll() {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer getNewPurchaseEntryNumber() {
		return purchaseEntryRepository.getNewPurchaseEntryNumber();
	}

	public GSTDetails getGSTDetails(Product p) {

		GSTDetails gst = null;

		if (p != null) {
			gst = new GSTDetails();
			double gstAmt = 0.0;

			// Exclusive
			gst.setInclusiveFlag("N");
			gstAmt = exclusiveCalc(p.getTableDispTax(), p.getTableDispAmountForPurEntry());
			gst.setRate(p.getTableDispTax());
			gst.setName(taxesService.getTaxName(p.getTableDispTax()) + " (" + p.getTableDispTax() + "%)");
			if (gstAmt != 0) {
				gst.setCgst(gstAmt / 2);
				gst.setSgst(gstAmt / 2);
			}
			gst.setGstAmount(gstAmt);
			gst.setTaxableAmount(p.getTableDispAmountForPurEntry());
		}

		return gst;

	}

	private Double exclusiveCalc(Double gstRate, Double amount) {
		Double gstAmt = (amount * gstRate) / 100;
		return gstAmt;
	}

	public List<ItemDetails> getItemList(PurchaseEntry purchaseEntry) {
		return purchaseEntryRepository.getItemDetails(purchaseEntry.getPurchaseEntryNo());
	}

	public List<PurchaseEntry> getSearchedInvoices(PurchaseEntrySearchCriteria criteria) {
		return purchaseEntryRepository.getSearchedInvoices(criteria);
	}

}
