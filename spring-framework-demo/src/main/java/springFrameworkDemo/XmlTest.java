package springFrameworkDemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.PropertiesBeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import springFrameworkDemo.bean.User;
import springFrameworkDemo.config.AppConfig;

import javax.security.auth.login.AppConfigurationEntry;

@ComponentScan("springFrameworkDemo")
public class XmlTest {
	@Autowired
	User user;

	public static void main(String[] args) {
		AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(	AppConfig.class	);
		User user = (User) annotationConfigApplicationContext.getBean("Users");
		System.out.println(user);
	}
}
