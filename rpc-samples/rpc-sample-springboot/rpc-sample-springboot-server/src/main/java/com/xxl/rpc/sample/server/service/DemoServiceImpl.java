package com.xxl.rpc.sample.server.service;

import com.xxl.rpc.core.remoting.provider.annotation.RpcService;
import com.xxl.rpc.sample.api.DemoService;
import com.xxl.rpc.sample.api.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;

/**
 * @author mzj
 */
@Slf4j
@Service
@RpcService
public class DemoServiceImpl implements DemoService {

	@Override
	public UserDTO sayHi(String name) {
		String word = MessageFormat.format("Hi {0}, from {1} as {2}", name, DemoServiceImpl.class.getName(), String.valueOf(System.currentTimeMillis()));
		if ("error".equalsIgnoreCase(name)) throw new RuntimeException("test exception.");

		UserDTO userDTO = new UserDTO(name, word);
		log.info(userDTO.toString());

		return userDTO;
	}

}
