package com.billing.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.Customer;
import com.billing.dto.StatusDTO;
import com.billing.repository.CustomerRepository;

@Service
public class CustomerService implements AppService<Customer> {

	@Autowired
	CustomerRepository customerRepository;

	@Override
	public StatusDTO add(Customer customer) {
		return customerRepository.addCustomer(customer);
	}

	@Override
	public StatusDTO update(Customer customer) {
		return customerRepository.updateCustomer(customer);
	}

	@Override
	public StatusDTO delete(Customer customer) {
		return customerRepository.deleteCustomer(customer.getCustId());
	}

	@Override
	public List<Customer> getAll() {
		return customerRepository.getAllCustomers();
	}

	public StatusDTO addCustomerPaymentHistory(int custId, double creditAmount, double debitAmount,
			String flag, String narration) {
		return customerRepository.addCustomerPaymentHistory(custId, creditAmount, debitAmount, flag, narration);
	}

	public Customer getCustomer(int custId) {
		return customerRepository.getCustomerDetails(custId);
	}

	public int getCustId() {
		return customerRepository.getNewCustId();
	}
}
