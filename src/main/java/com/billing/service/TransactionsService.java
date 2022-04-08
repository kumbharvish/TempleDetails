package com.billing.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.AccountBalanceHistory;
import com.billing.dto.AccountDetails;
import com.billing.dto.StatusDTO;
import com.billing.dto.TransactionDetails;
import com.billing.dto.TxnSearchCriteria;
import com.billing.repository.TransactionsRepository;

@Service
public class TransactionsService implements AppService<TransactionDetails> {

	@Autowired
	TransactionsRepository TransactionsRepository;

	@Override
	public StatusDTO add(TransactionDetails expense) {
		return TransactionsRepository.addTxn(expense);
	}

	@Override
	public StatusDTO update(TransactionDetails expense) {
		return TransactionsRepository.updateTxn(expense);
	}

	@Override
	public StatusDTO delete(TransactionDetails txn) {
		return TransactionsRepository.deleteTxn(txn);
	}

	@Override
	public List<TransactionDetails> getAll() {
		return null;
	}

	public List<TransactionDetails> getSearchedTransactions(TxnSearchCriteria criteria) {
		return TransactionsRepository.getSearchedTransactions(criteria);
	}

	public List<AccountBalanceHistory> getAccountBalanceHistory(String fromDate, String toDate) {
		return TransactionsRepository.getAccountBalanceHistory(fromDate, toDate);
	}

	public AccountDetails getAccountDetails() {
		return TransactionsRepository.getAccountDetails();
	}

}
