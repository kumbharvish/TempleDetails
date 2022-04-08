package com.billing.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.TempleDetails;
import com.billing.dto.StatusDTO;
import com.billing.repository.TempleDetailsRepository;

@Service
public class TempleDetailsService implements AppService<TempleDetails>{
	
	@Autowired
	TempleDetailsRepository storeDetailsRepository;

	private TempleDetails storeDetails;

	// Return cached Stored Detils
	public TempleDetails getMyStoreDetails() {
		if (storeDetails == null) {
			storeDetails = storeDetailsRepository.getMyStoreDetailsFromDB();
		}

		return storeDetails;
	}

	@Override
	public StatusDTO add(TempleDetails myStoreDetails) {
		return storeDetailsRepository.addStoreDetails(myStoreDetails);
	}

	@Override
	public StatusDTO update(TempleDetails myStoreDetails) {
		return storeDetailsRepository.updateStoreDetails(myStoreDetails);
	}

	@Override
	public StatusDTO delete(TempleDetails myStoreDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TempleDetails> getAll() {
		// TODO Auto-generated method stub
		return null;
	}

}
