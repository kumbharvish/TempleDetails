package com.billing.service;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.StatusDTO;
import com.billing.dto.Tax;
import com.billing.repository.TaxesRepository;

@Service
public class TaxesService implements AppService<Tax> {

	@Autowired
	TaxesRepository taxesRepository;

	private HashMap<Double, String> taxMap;

	private HashMap<Double, String> getTaxMap() {
		HashMap<Double, String> prop = new HashMap<>();
		for (Tax t : getAll()) {
			prop.put(t.getValue(), t.getName());
		}
		return prop;

	}

	public String getTaxName(Double value) {
		String data = null;
		if (taxMap == null) {
			taxMap = getTaxMap();
		}
		data = taxMap.get(value);

		return data;
	}

	@Override
	public StatusDTO add(Tax tax) {
		return taxesRepository.addTax(tax);
	}

	@Override
	public StatusDTO update(Tax tax) {
		return taxesRepository.updateTax(tax);
	}

	@Override
	public StatusDTO delete(Tax tax) {
		return taxesRepository.deleteTax(tax.getId());
	}

	@Override
	public List<Tax> getAll() {
		return taxesRepository.getAllTax();
	}

	public List<Tax> getAll(Connection con) {
		List<Tax> taxList = taxesRepository.getAllTax(con);
		Collections.sort(taxList, Tax.getComparator(Tax.SortParameter.TAX_VALUE));
		return taxList;
	}

}
