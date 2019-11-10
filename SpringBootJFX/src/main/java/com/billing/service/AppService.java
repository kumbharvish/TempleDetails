package com.billing.service;

import com.billing.dto.StatusDTO;

public interface AppService<T> {

	public StatusDTO add(T t);

	public StatusDTO update(T t);

	public StatusDTO delete(T t);

}
