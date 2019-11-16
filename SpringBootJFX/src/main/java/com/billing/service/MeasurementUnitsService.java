package com.billing.service;

import java.sql.Connection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.MeasurementUnit;
import com.billing.dto.StatusDTO;
import com.billing.repository.MeasurementUnitsRepository;

@Service
public class MeasurementUnitsService implements AppService<MeasurementUnit> {

	@Autowired
	MeasurementUnitsRepository measurementUnitsRepository;

	@Override
	public StatusDTO add(MeasurementUnit unit) {
		return measurementUnitsRepository.addUOM(unit);
	}

	@Override
	public StatusDTO update(MeasurementUnit unit) {
		return measurementUnitsRepository.updateUOM(unit);
	}

	@Override
	public StatusDTO delete(MeasurementUnit unit) {
		return measurementUnitsRepository.deleteUOM(unit.getId());
	}

	@Override
	public List<MeasurementUnit> getAll() {
		return measurementUnitsRepository.getAllUOM();
	}

	public List<MeasurementUnit> getAll(Connection conn) {
		return measurementUnitsRepository.getAllUOM(conn);
	}

}
