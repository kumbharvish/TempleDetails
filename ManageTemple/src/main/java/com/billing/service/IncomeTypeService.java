package com.billing.service;

import java.sql.Connection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.IncomeType;
import com.billing.dto.StatusDTO;
import com.billing.repository.IncomeTypeRepository;

@Service
public class IncomeTypeService implements AppService<IncomeType> {

	@Autowired
	IncomeTypeRepository measurementUnitsRepository;

	@Override
	public StatusDTO add(IncomeType unit) {
		return measurementUnitsRepository.add(unit);
	}

	@Override
	public StatusDTO update(IncomeType unit) {
		return measurementUnitsRepository.update(unit);
	}

	@Override
	public StatusDTO delete(IncomeType unit) {
		return measurementUnitsRepository.delete(unit.getId());
	}

	@Override
	public List<IncomeType> getAll() {
		return measurementUnitsRepository.getAllIncomeType();
	}

	public List<IncomeType> getAll(Connection conn) {
		return measurementUnitsRepository.getAllIncomeType(conn);
	}

}
