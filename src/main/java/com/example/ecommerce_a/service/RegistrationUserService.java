package com.example.ecommerce_a.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.ecommerce_a.domain.User;
import com.example.ecommerce_a.form.RegistrationUserForm;
import com.example.ecommerce_a.repository.UserRepository;

/**
 * ユーザ登録時にユーザ情報を操作するサービス．
 * 
 * @author yuiko.mitsui
 *
 */
@Service
public class RegistrationUserService {
	@Autowired
	UserRepository userRepository;

	/**
	 * 新規ユーザの登録をする．
	 * 
	 * @param form ユーザ登録情報．
	 */
	public void insertUser(RegistrationUserForm form) {
		User user = new User();
		BeanUtils.copyProperties(form, user);
		
		userRepository.insertUser(user);
	}
}
