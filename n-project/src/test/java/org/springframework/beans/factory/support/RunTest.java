package org.springframework.beans.factory.support;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * @author Sola
 * @date 2020/09/06
 */
@Slf4j
public class RunTest {

	/**
	 * 手动创建 bean, 以下演示了 4 个步骤:
	 * 1. 定义 beanDefinition, 添加属性
	 * 2. 定义后置处理器
	 * 3. 创建 bean
	 * 4. 添加 bean 到单例池
	 */
	@Test
	public void testCreateBean() {
		// bean 工厂
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		// 添加后置处理器
		beanFactory.addBeanPostProcessor(new BeanPostProcessor() {
			@Override
			public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
				if (bean instanceof MyService) {
					return new MyService$proxy((MyService) bean);
				}
				return bean;
			}
		});
		// 定义 beanDefinition
		RootBeanDefinition mbd = new RootBeanDefinition(MyService.class);
		// 给对象赋值
		mbd.setPropertyValues(new MutablePropertyValues().add("id", "12345"));
		// 创建对象
		MyService myService = (MyService) beanFactory.doCreateBean("myService", mbd, null);
		// 添加到单例池, 有以下两种方式
		beanFactory.addSingleton("myService", myService);
		beanFactory.getSingleton("myService", () -> myService);
		log.info("创建 bean: {}, 并添加到单例池", myService);
	}

	/**
	 * 通过工厂获取 bean
	 * 1. 首先声明 beanFactory, 并注册 beanDefinition
	 * 2. 调用 beanFactory 的 getBean 方法, 该方法会查找 bean, 没有则进行类似上面的创建流程
	 * <p>
	 * 涉及的几个关键类和方法:
	 * 1. DefaultListableBeanFactory#getBean
	 * 2. AbstractBeanFactory#doGetBean
	 * 3. DefaultSingletonBeanRegistry#getSingleton
	 * 4. AbstractAutowireCapableBeanFactory#doCreateBean
	 */
	@Test
	public void testGetBean() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

		RootBeanDefinition mbd = new RootBeanDefinition(MyService.class);
		mbd.setPropertyValues(new MutablePropertyValues().add("id", "12345"));
		beanFactory.registerBeanDefinition("myService", mbd);

		MyService bean = beanFactory.getBean(MyService.class);
		log.info("获取到 bean: {}", bean);
	}

	@Setter
	@Getter
	private static class MyService {
		private int id;
	}

	@Setter
	@Getter
	private static class MyService$proxy extends MyService {
		private MyService myService;

		public MyService$proxy(MyService myService) {
			this.myService = myService;
		}
	}

}
