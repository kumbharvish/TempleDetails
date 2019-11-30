package com.billing.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.constants.AppConstants;
import com.billing.dto.Expense;
import com.billing.dto.ExpenseSearchCriteria;
import com.billing.dto.ExpenseType;
import com.billing.dto.StatusDTO;
import com.billing.repository.ExpensesRepository;

import javafx.scene.control.ComboBox;

@Service
public class ExpensesService implements AppService<Expense> {

	@Autowired
	ExpensesRepository expensesRepository;

	public void fillExpenseTypes(ComboBox<String> combobox) {
		combobox.getItems().add(AppConstants.SELECT_EXPENSE_CATEGORY);
		for (ExpenseType s : getExpenseTypes()) {
			combobox.getItems().add(s.getName());
		}
		combobox.getSelectionModel().select(0);
	}

	@Override
	public StatusDTO add(Expense expense) {
		return expensesRepository.addExpense(expense);
	}

	@Override
	public StatusDTO update(Expense expense) {
		return expensesRepository.updateExpense(expense);
	}

	@Override
	public StatusDTO delete(Expense expense) {
		return expensesRepository.deleteExpense(expense.getId());
	}

	@Override
	public List<Expense> getAll() {
		return null;
	}

	public List<Expense> getExpenses(String fromDate, String toDate, String expenseCategory) {
		if (AppConstants.SELECT_EXPENSE_CATEGORY.equals(expenseCategory)) {
			expenseCategory = null;
		}
		return expensesRepository.getExpenses(fromDate, toDate, expenseCategory);
	}

	public List<ExpenseType> getExpenseTypes() {
		return expensesRepository.getExpenseTypes();
	}
	
	public List<Expense> getSearchedExpenses(ExpenseSearchCriteria criteria) {
		return expensesRepository.getSearchedExpenses(criteria);
	}

}
