package com.billing.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.GSTDetails;
import com.billing.dto.Product;
import com.billing.dto.PurchaseEntry;
import com.billing.dto.StatusDTO;
import com.billing.repository.PurchaseEntryRepository;

@Service
public class PurchaseEntryService implements AppService<PurchaseEntry> {

	@Autowired
	PurchaseEntryRepository purchaseEntryRepository;

	@Autowired
	TaxesService taxesService;

	@Override
	public StatusDTO add(PurchaseEntry t) {
		return purchaseEntryRepository.addPurchaseEntry(t);
	}

	@Override
	public StatusDTO update(PurchaseEntry t) {
		return null;
	}

	@Override
	public StatusDTO delete(PurchaseEntry t) {
		// TODO Auto-generated method stub
		return null;
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

}
