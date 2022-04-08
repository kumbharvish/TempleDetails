package com.billing.service;

import java.sql.Connection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.ExpenseType;
import com.billing.dto.StatusDTO;
import com.billing.repository.ExpenseTypeRepository;

@Service
public class ExpenseTypeService implements AppService<ExpenseType> {

	@Autowired
	ExpenseTypeRepository expenseTypeRepo;

	@Override
	public StatusDTO add(ExpenseType expenseType) {
		return expenseTypeRepo.add(expenseType);
	}

	@Override
	public StatusDTO update(ExpenseType expenseType) {
		return expenseTypeRepo.update(expenseType);
	}

	@Override
	public StatusDTO delete(ExpenseType expenseType) {
		return expenseTypeRepo.delete(expenseType.getId());
	}

	@Override
	public List<ExpenseType> getAll() {
		return expenseTypeRepo.getAllExpenseType();
	}

	public List<ExpenseType> getAll(Connection conn) {
		return expenseTypeRepo.getAllExpenseType(conn);
	}

}
