package com.billing.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.PurchaseEntry;
import com.billing.dto.StatusDTO;
import com.billing.repository.PurchaseEntryRepository;

@Service
public class PurchaseEntryService implements AppService<PurchaseEntry> {

	@Autowired
	PurchaseEntryRepository purchaseEntryRepository;

	@Override
	public StatusDTO add(PurchaseEntry t) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StatusDTO update(PurchaseEntry t) {
		// TODO Auto-generated method stub
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

}
