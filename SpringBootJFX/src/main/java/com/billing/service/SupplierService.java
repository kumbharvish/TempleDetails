package com.billing.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.StatusDTO;
import com.billing.dto.Supplier;
import com.billing.dto.SupplierPaymentHistory;
import com.billing.repository.SupplierRepository;

@Service
public class SupplierService implements AppService<Supplier> {

	@Autowired
	SupplierRepository supplierRepository;

	@Override
	public StatusDTO add(Supplier supplier) {
		return supplierRepository.addSupplier(supplier);
	}

	@Override
	public StatusDTO update(Supplier supplier) {
		return supplierRepository.updateSupplier(supplier);

	}

	@Override
	public StatusDTO delete(Supplier supplier) {
		return supplierRepository.deleteSupplier(supplier.getSupplierID());

	}

	@Override
	public List<Supplier> getAll() {
		return supplierRepository.getAllSuppliers();
	}

	public StatusDTO isSupplierEntryAvailable(Integer supplierId) {
		return supplierRepository.isSupplierEntryAvailable(supplierId);
	}

	public StatusDTO addSupplierPaymentHistory(Integer supplierId, double creditAmount, double debitAmount, String flag,
			String narration) {
		return supplierRepository.addSupplierPaymentHistory(supplierId, creditAmount, debitAmount, flag, narration);
	}

	public Supplier getSupplier(int supplierId) {
		return supplierRepository.getSupplier(supplierId);
	}
	
	public List<SupplierPaymentHistory> getSuppliersPayHistory(Integer supplierId) {
		return supplierRepository.getSuppliersPayHistory(supplierId);
	}

}
