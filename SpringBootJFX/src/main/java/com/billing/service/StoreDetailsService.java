package com.billing.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.MyStoreDetails;
import com.billing.dto.StatusDTO;
import com.billing.repository.StoreDetailsRepository;

@Service
public class StoreDetailsService implements AppService<MyStoreDetails>{
	
	@Autowired
	StoreDetailsRepository storeDetailsRepository;

	private MyStoreDetails storeDetails;

	// Return cached Stored Detils
	public MyStoreDetails getMyStoreDetails() {
		if (storeDetails == null) {
			storeDetails = storeDetailsRepository.getMyStoreDetailsFromDB();
		}

		return storeDetails;
	}

	@Override
	public StatusDTO add(MyStoreDetails myStoreDetails) {
		return storeDetailsRepository.addStoreDetails(myStoreDetails);
	}

	@Override
	public StatusDTO update(MyStoreDetails myStoreDetails) {
		return storeDetailsRepository.updateStoreDetails(myStoreDetails);
	}

	@Override
	public StatusDTO delete(MyStoreDetails myStoreDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<MyStoreDetails> getAll() {
		// TODO Auto-generated method stub
		return null;
	}

}
