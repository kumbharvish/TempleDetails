package com.billing.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.constants.AppConstants;
import com.billing.dto.ItemDetails;
import com.billing.dto.ReturnDetails;
import com.billing.dto.StatusDTO;
import com.billing.repository.SalesReturnRepository;

@Service
public class SalesReturnService implements AppService<ReturnDetails> {

	@Autowired
	SalesReturnRepository salesReturnRepository;

	@Autowired
	CustomerService customerService;

	@Autowired
	ProductService productService;

	@Override
	public StatusDTO add(ReturnDetails returnDetails) {
		StatusDTO statusSaveReturn = new StatusDTO();
		if (AppConstants.PENDING.equals(returnDetails.getPaymentMode())) {
			// Save Return Details
			StatusDTO status = salesReturnRepository.saveReturnDetails(returnDetails);
			StatusDTO statusAddPendingAmt = new StatusDTO(-1);
			if (status.getStatusCode() == 0) {
				String narration = "Sales Return Amount based on Return No : " + returnDetails.getReturnNumber();
				statusAddPendingAmt = customerService.addCustomerPaymentHistory(returnDetails.getCustomerId(), 0,
						returnDetails.getTotalReturnAmount(), AppConstants.DEBIT, narration);
			}
			if (status.getStatusCode() != 0 || statusAddPendingAmt.getStatusCode() != 0) {
				statusSaveReturn.setStatusCode(-1);
			}
		}
		// Other than PENDING pay mode ex: cash,cheque etc
		if (!AppConstants.PENDING.equals(returnDetails.getPaymentMode())) {
			// Save Bill Details
			StatusDTO status = salesReturnRepository.saveReturnDetails(returnDetails);
			if (status.getStatusCode() != 0) {
				statusSaveReturn.setStatusCode(-1);
			}
		}
		return statusSaveReturn;
	}

	@Override
	public StatusDTO update(ReturnDetails returnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StatusDTO delete(ReturnDetails returnDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ReturnDetails> getAll() {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer getNewReturnNumber() {
		return salesReturnRepository.getNewReturnNumber();
	}

	public ReturnDetails getReturnDetails(Integer billNumber) {
		return salesReturnRepository.getReturnDetails(billNumber);
	}

	public List<ReturnDetails> getReturnDetails(String fromDate, String toDate) {
		return salesReturnRepository.getReturnDetails(fromDate, toDate);
	}

	public StatusDTO isSalesReturned(int billNumber) {
		return salesReturnRepository.isSalesReturned(billNumber);
	}
	
	public List<ItemDetails> getReturnedItemList(Integer returnNumber) {
		return salesReturnRepository.getReturnedItemList(returnNumber);
	}


}
