package com.billing.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.StatusDTO;
import com.billing.dto.UserDetails;
import com.billing.repository.UserRepository;

@Service
public class UserService implements AppService<UserDetails> {

	@Autowired
	UserRepository userRepository;

	public UserDetails validateUser(String userName, String password) {
		return userRepository.validateUser(userName, password);
	}

	public UserDetails getUserDetails(UserDetails userDtls) {
		return userRepository.getUserDetails(userDtls);
	}

	public StatusDTO changePassword(UserDetails userDetails, String existingPwd, String newPassword) {
		return userRepository.changePassword(userDetails, existingPwd, newPassword);
	}

	public StatusDTO changeUserName(UserDetails userDetails, String newUserName) {
		return userRepository.changeUserName(userDetails, newUserName);
	}

	@Override
	public StatusDTO add(UserDetails userDetails) {
		StatusDTO status = userRepository.addUser(userDetails);
		//Add new User and Mark admin user as inactive
		if (status.getStatusCode() == 0) {
			UserDetails user = new UserDetails();
			user.setUserName("admin");
			user.setUserType("ADMIN");
			StatusDTO markStatus = userRepository.markUserAsInactive(user);
			return markStatus;
		}
		return status;
	}

	@Override
	public StatusDTO update(UserDetails userDetails) {
		return userRepository.updatePersonalDetails(userDetails);
	}

	@Override
	public StatusDTO delete(UserDetails userDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserDetails> getAll() {
		// TODO Auto-generated method stub
		return null;
	}
}
