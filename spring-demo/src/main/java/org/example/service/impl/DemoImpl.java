package org.example.service.impl;

import org.example.service.Demo;

public class DemoImpl implements Demo {
	public String sayHello(String name) {
		System.out.println("sayHello");
		return "sayHello";
	}

}
