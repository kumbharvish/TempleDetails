package com.billing.service;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.BillDetails;
import com.billing.dto.Customer;
import com.billing.dto.CustomerPaymentHistory;
import com.billing.dto.CustomerProfit;
import com.billing.repository.CustomerHistoryRepository;

@Service
public class CustomerHistoryService {

	@Autowired
	CustomerHistoryRepository customerHistoryRepository;
	
	@Autowired
	CustomerService customerService;

	public HashMap<Long, Customer> getCustomerMap() {
		HashMap<Long, Customer> customerMap = new HashMap<Long, Customer>();
		for (Customer cust : customerService.getAll()) {
			customerMap.put(cust.getCustMobileNumber(), cust);
		}
		return customerMap;

	}

	public List<CustomerPaymentHistory> getAllCustomersPayHistory(int customerId) {
		return customerHistoryRepository.getAllCustomersPayHistory(customerId);
	}

	public List<BillDetails> getBillDetails(int customerId) {
		return customerHistoryRepository.getBillDetails(customerId);

	}

	public List<CustomerProfit> getCustomerWiseProfit(String fromDate, String toDate) {
		return customerHistoryRepository.getCustomerWiseProfit(fromDate, toDate);

	}

}
