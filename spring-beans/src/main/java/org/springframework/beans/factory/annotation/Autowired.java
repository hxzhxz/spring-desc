/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.beans.factory.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a constructor, field, setter method, or config method as to be autowired by
 * Spring's dependency injection facilities. This is an alternative to the JSR-330
 * {@link javax.inject.Inject} annotation, adding required-vs-optional semantics.
 * <p>
 * 将构造函数、字段、setter方法或配置方法标记为自动连接
 * Spring的依赖注入工具。这是JSR-330的替代方案
 * {@link javax.inject。注释，添加必需和可选语义
 *
 * <h3>Autowired Constructors</h3>
 * <p>Only one constructor of any given bean class may declare this annotation with the
 * 方法声明此注释，任何给定bean类只有一个构造函数可以使用
 * {@link #required} attribute set to {@code true}, indicating <i>the</i> constructor
 * {@link #required}属性设置为{@code true}，表示<i> </i>构造函数
 * to autowire when used as a Spring bean. Furthermore, if the {@code required}
 * 当用作Spring bean时自动装配。此外，如果{@code required}
 * attribute is set to {@code true}, only a single constructor may be annotated
 * 属性被设置为{@code true}时，只有一个构造函数可以被注释
 * with {@code @Autowired}. If multiple <i>non-required</i> constructors declare the
 * {@code @Autowired}。如果多个<i>非必需</i>构造函数声明
 * annotation, they will be considered as candidates for autowiring. The constructor
 * 注释，它们将被视为自动装配的候选。构造函数
 * with the greatest number of dependencies that can be satisfied by matching beans
 * 通过匹配bean来满足最大数量的依赖关系
 * in the Spring container will be chosen. If none of the candidates can be satisfied,
 * 将在Spring容器中被选择。如果没有一个候选人能让你满意，
 * then a primary/default constructor (if present) will be used. Similarly, if a
 * 则使用主/默认构造函数(如果存在)。类似地，如果
 * class declares multiple constructors but none of them is annotated with
 * class声明了多个构造函数，但是它们都没有注释
 * {@code @Autowired}, then a primary/default constructor (if present) will be used.
 * {@code @Autowired}，那么将使用一个主/默认构造函数(如果存在)。
 * If a class only declares a single constructor to begin with, it will always be used,
 * 如果一个类开始时只声明了一个构造函数，它将一直被使用。
 * even if not annotated. An annotated constructor does not have to be public.
 * 即使没有注释。带注释的构造函数不一定是公共的
 *
 * <h3>Autowired Fields</h3>
 * <p>Fields are injected right after construction of a bean, before any config methods
 * are invoked. Such a config field does not have to be public.
 * 在构造bean之后，在任何配置方法之前注入字段
 * *被调用。这样的配置字段不一定是公共的
 *
 * <h3>Autowired Methods</h3>
 * <p>Config methods may have an arbitrary name and any number of arguments; each of
 * those arguments will be autowired with a matching bean in the Spring container.
 * Bean property setter methods are effectively just a special case of such a general
 * config method. Such config methods do not have to be public.
 * 配置方法可以有任意名称和任意数量的参数;
 * 每个参数将自动与Spring容器中的匹配bean连接。
 * Bean属性setter方法实际上只是这种通用配置方法的特殊情况
 * 这样的配置方法不一定是公共的
 *
 * <h3>Autowired Parameters</h3>
 * <p>Although {@code @Autowired} can technically be declared on individual method
 * or constructor parameters since Spring Framework 5.0, most parts of the
 * framework ignore such declarations. The only part of the core Spring Framework
 * that actively supports autowired parameters is the JUnit Jupiter support in
 * the {@code spring-test} module (see the
 * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/testing.html#testcontext-junit-jupiter-di">TestContext framework</a>
 * reference documentation for details).
 * 虽然{@code @Autowired}技术上可以在单个方法上声明
 * 或构造函数参数，自Spring Framework 5.0以来，大部分的
 * 框架忽略这样的声明。核心Spring框架的唯一部分
 * 主动支持自动连接参数的是JUnit Jupiter支持
 * {@code spring-test}模块
 *
 * <h3>Multiple Arguments and 'required' Semantics</h3>
 * <p>In the case of a multi-arg constructor or method, the {@link #required} attribute
 * is applicable to all arguments. Individual parameters may be declared as Java-8 style
 * {@link java.util.Optional} or, as of Spring Framework 5.0, also as {@code @Nullable}
 * or a not-null parameter type in Kotlin, overriding the base 'required' semantics.
 * <p>
 * 对于多参数构造函数或方法，{@link #required}属性
 * 适用于所有参数。单个参数可以声明为Java-8风格
 * {@链接java.util。可选}或者，在Spring Framework 5.0中，也可以作为{@code @Nullable}
 * 或Kotlin中的非空参数类型，覆盖基本的“required”语义。
 *
 * <h3>Autowiring Arrays, Collections, and Maps</h3>
 * <p>In case of an array, {@link java.util.Collection}, or {@link java.util.Map}
 * dependency type, the container autowires all beans matching the declared value
 * type. For such purposes, the map keys must be declared as type {@code String}
 * which will be resolved to the corresponding bean names. Such a container-provided
 * collection will be ordered, taking into account
 * <p>
 * 如果是数组，{@link java.util。集合}或{@link java.util.Map}
 * 依赖类型，容器自动连接所有与声明值匹配的bean
 * 类型。出于这种目的，映射键必须声明为类型{@code String}
 * 将被解析为相应的bean名称。这样的容器提供
 * 收集将被排序，考虑
 *
 * <p>
 * {@link org.springframework.core.Ordered Ordered} and
 * {@link org.springframework.core.annotation.Order @Order} values of the target
 * components, otherwise following their registration order in the container.
 * Alternatively, a single matching target bean may also be a generally typed
 * {@code Collection} or {@code Map} itself, getting injected as such.
 * <p>
 * 组件顺序，否则按照它们在容器中的注册顺序。
 * 或者，单个匹配的目标bean也可以是通用类型的
 *
 * <h3>Not supported in {@code BeanPostProcessor} or {@code BeanFactoryPostProcessor}</h3>
 * <p>Note that actual injection is performed through a
 * {@link org.springframework.beans.factory.config.BeanPostProcessor
 * BeanPostProcessor} which in turn means that you <em>cannot</em>
 * use {@code @Autowired} to inject references into
 * {@link org.springframework.beans.factory.config.BeanPostProcessor
 * BeanPostProcessor} or
 * {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor BeanFactoryPostProcessor}
 * types. Please consult the javadoc for the {@link AutowiredAnnotationBeanPostProcessor}
 * class (which, by default, checks for the presence of this annotation).
 * <p>
 * {@code BeanPostProcessor}或{@code BeanFactoryPostProcessor}不支持
 * 注意实际注入是通过一个
 * {@link org.springframework.beans.factory.config.BeanPostProcessor
 * BeanPostProcessor}，这反过来意味着你不能
 * 使用{@code @Autowired}注入引用
 * {@link org.springframework.beans.factory.config.BeanPostProcessor
 * BeanPostProcessor}或
 * {@link org.springframework.beans.beans.factorypostprocessor}
 * 类型。请咨询，参考javadoc获取 AutowiredAnnotationBeanPostProcessor 类(默认情况下，它检查是否存在该注释)。
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Sam Brannen
 * @see AutowiredAnnotationBeanPostProcessor
 * @see Qualifier
 * @see Value
 * @since 2.5
 */
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {

	/**
	 * Declares whether the annotated dependency is required.
	 * <p>Defaults to {@code true}.
	 */
	boolean required() default true;

}
