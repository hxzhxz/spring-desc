/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.annotation;

import java.util.Set;

import org.w3c.dom.Element;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.lang.Nullable;

/**
 * 上下文解析器:annotation-config&gt;元素。
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @author Christian Dupuis
 * @since 2.5
 * @see AnnotationConfigUtils
 */
public class AnnotationConfigBeanDefinitionParser implements BeanDefinitionParser {

	@Override
	@Nullable
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		Object source = parserContext.extractSource(element);

		// 获取所有相关BeanPostProcessors的bean定义。
		Set<BeanDefinitionHolder> processorDefinitions =
				AnnotationConfigUtils.registerAnnotationConfigProcessors(parserContext.getRegistry(), source);

		// Register component for the surrounding <context:annotation-config> element.
		//为周围的<context:annotation-config>元素注册组件。
		CompositeComponentDefinition compDefinition = new CompositeComponentDefinition(element.getTagName(), source);
		parserContext.pushContainingComponent(compDefinition);

		// Nest the concrete beans in the surrounding component.
		//将混凝土bean嵌套在周围的组件中。
		for (BeanDefinitionHolder processorDefinition : processorDefinitions) {
			parserContext.registerComponent(new BeanComponentDefinition(processorDefinition));
		}

		// Finally register the composite component.
		//最后注册复合组件。
		parserContext.popAndRegisterContainingComponent();

		return null;
	}

}
