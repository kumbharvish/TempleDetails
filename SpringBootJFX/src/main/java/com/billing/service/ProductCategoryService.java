package com.billing.service;

import java.sql.Connection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.Product;
import com.billing.dto.ProductCategory;
import com.billing.dto.StatusDTO;
import com.billing.repository.ProductCategoryRepository;

@Service
public class ProductCategoryService implements AppService<ProductCategory> {

	@Autowired
	ProductCategoryRepository productCategoryRepository;

	@Override
	public StatusDTO add(ProductCategory productCategory) {
		return productCategoryRepository.addCategory(productCategory);
	}

	@Override
	public StatusDTO update(ProductCategory productCategory) {
		return productCategoryRepository.updateCategory(productCategory);
	}

	@Override
	public StatusDTO delete(ProductCategory productCategory) {
		return productCategoryRepository.deleteCategory(productCategory.getCategoryCode());
	}

	@Override
	public List<ProductCategory> getAll() {
		return productCategoryRepository.getAllCategories();
	}

	public List<ProductCategory> getAll(Connection conn) {
		return productCategoryRepository.getAllCategories(conn);
	}

	public List<Product> getProductsUnderCategory(ProductCategory productCategory) {
		return productCategoryRepository.getProductsUnderCategory(productCategory.getCategoryCode());
	}

	public List<ProductCategory> getCategoryWiseStockReprot() {
		return productCategoryRepository.getCategoryWiseStockReprot();
	}

}
