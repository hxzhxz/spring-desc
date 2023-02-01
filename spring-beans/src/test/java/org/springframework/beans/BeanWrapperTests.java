/*
 * Copyright 2002-2022 the original author or authors.
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

package org.springframework.beans;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import org.springframework.beans.testfixture.beans.TestBean;
import org.springframework.core.OverridingClassLoader;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.UrlResource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Specific {@link BeanWrapperImpl} tests.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Alef Arendsen
 * @author Arjen Poutsma
 * @author Chris Beams
 * @author Dave Syer
 */
public class BeanWrapperTests extends AbstractPropertyAccessorTests {

	@Override
	protected BeanWrapperImpl createAccessor(Object target) {
		return new BeanWrapperImpl(target);
	}


	@Test
	public void setterDoesNotCallGetter() {
		System.out.println(123);

		GetterBean target = new GetterBean();
		BeanWrapper accessor = createAccessor(target);
		System.out.println(123);
		accessor.setPropertyValue("name", "tom");
		assertThat(target.getAliasedName()).isEqualTo("tom");
		assertThat(accessor.getPropertyValue("aliasedName")).isEqualTo("tom");
	}

	@Test
	public void getterSilentlyFailWithOldValueExtraction() {
		GetterBean target = new GetterBean();
		BeanWrapper accessor = createAccessor(target);
		accessor.setExtractOldValueForEditor(true); // This will call the getter
		accessor.setPropertyValue("name", "tom");
		assertThat(target.getAliasedName()).isEqualTo("tom");
		assertThat(accessor.getPropertyValue("aliasedName")).isEqualTo("tom");
	}

	@Test
	public void aliasedSetterThroughDefaultMethod() {
		GetterBean target = new GetterBean();
		BeanWrapper accessor = createAccessor(target);
		accessor.setPropertyValue("aliasedName", "tom");
		assertThat(target.getAliasedName()).isEqualTo("tom");
		assertThat(accessor.getPropertyValue("aliasedName")).isEqualTo("tom");
	}

	@Test
	public void setValidAndInvalidPropertyValuesShouldContainExceptionDetails() {
		TestBean target = new TestBean();
		String newName = "tony";
		String invalidTouchy = ".valid";
		BeanWrapper accessor = createAccessor(target);
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("age", "foobar"));
		pvs.addPropertyValue(new PropertyValue("name", newName));
		pvs.addPropertyValue(new PropertyValue("touchy", invalidTouchy));
		assertThatExceptionOfType(PropertyBatchUpdateException.class).isThrownBy(() ->
				accessor.setPropertyValues(pvs))
			.satisfies(ex -> {
				assertThat(ex.getExceptionCount()).isEqualTo(2);
				assertThat(ex.getPropertyAccessException("touchy").getPropertyChangeEvent()
						.getNewValue()).isEqualTo(invalidTouchy);
			});
		// Test validly set property matches
		assertThat(target.getName().equals(newName)).as("Valid set property must stick").isTrue();
		assertThat(target.getAge() == 0).as("Invalid set property must retain old value").isTrue();
	}

	@Test
	public void checkNotWritablePropertyHoldPossibleMatches() {
		TestBean target = new TestBean();
		BeanWrapper accessor = createAccessor(target);
		assertThatExceptionOfType(NotWritablePropertyException.class).isThrownBy(() ->
				accessor.setPropertyValue("ag", "foobar"))
			.satisfies(ex -> assertThat(ex.getPossibleMatches()).containsExactly("age"));
	}

	@Test  // Can't be shared; there is no such thing as a read-only field
	public void setReadOnlyMapProperty() {
		TypedReadOnlyMap map = new TypedReadOnlyMap(Collections.singletonMap("key", new TestBean()));
		TypedReadOnlyMapClient target = new TypedReadOnlyMapClient();
		BeanWrapper accessor = createAccessor(target);
		accessor.setPropertyValue("map", map);
	}

	@Test
	public void notWritablePropertyExceptionContainsAlternativeMatch() {
		IntelliBean target = new IntelliBean();
		BeanWrapper bw = createAccessor(target);
		try {
			bw.setPropertyValue("names", "Alef");
		}
		catch (NotWritablePropertyException ex) {
			assertThat(ex.getPossibleMatches()).as("Possible matches not determined").isNotNull();
			assertThat(ex.getPossibleMatches().length).as("Invalid amount of alternatives").isEqualTo(1);
		}
	}

	@Test
	public void notWritablePropertyExceptionContainsAlternativeMatches() {
		IntelliBean target = new IntelliBean();
		BeanWrapper bw = createAccessor(target);
		try {
			bw.setPropertyValue("mystring", "Arjen");
		}
		catch (NotWritablePropertyException ex) {
			assertThat(ex.getPossibleMatches()).as("Possible matches not determined").isNotNull();
			assertThat(ex.getPossibleMatches().length).as("Invalid amount of alternatives").isEqualTo(3);
		}
	}

	@Override
	@Test  // Can't be shared: no type mismatch with a field
	public void setPropertyTypeMismatch() {
		PropertyTypeMismatch target = new PropertyTypeMismatch();
		BeanWrapper accessor = createAccessor(target);
		accessor.setPropertyValue("object", "a String");
		assertThat(target.value).isEqualTo("a String");
		assertThat(target.getObject() == 8).isTrue();
		assertThat(accessor.getPropertyValue("object")).isEqualTo(8);
	}

	@Test
	public void propertyDescriptors() throws Exception {
		TestBean target = new TestBean();
		target.setSpouse(new TestBean());
		BeanWrapper accessor = createAccessor(target);
		accessor.setPropertyValue("name", "a");
		accessor.setPropertyValue("spouse.name", "b");

		assertThat(target.getName()).isEqualTo("a");
		assertThat(target.getSpouse().getName()).isEqualTo("b");
		assertThat(accessor.getPropertyValue("name")).isEqualTo("a");
		assertThat(accessor.getPropertyValue("spouse.name")).isEqualTo("b");
		assertThat(accessor.getPropertyDescriptor("name").getPropertyType()).isEqualTo(String.class);
		assertThat(accessor.getPropertyDescriptor("spouse.name").getPropertyType()).isEqualTo(String.class);

		assertThat(accessor.isReadableProperty("class.package")).isFalse();
		assertThat(accessor.isReadableProperty("class.module")).isFalse();
		assertThat(accessor.isReadableProperty("class.classLoader")).isFalse();
		assertThat(accessor.isReadableProperty("class.name")).isTrue();
		assertThat(accessor.isReadableProperty("class.simpleName")).isTrue();
		assertThat(accessor.getPropertyValue("class.name")).isEqualTo(TestBean.class.getName());
		assertThat(accessor.getPropertyValue("class.simpleName")).isEqualTo(TestBean.class.getSimpleName());
		assertThat(accessor.getPropertyDescriptor("class.name").getPropertyType()).isEqualTo(String.class);
		assertThat(accessor.getPropertyDescriptor("class.simpleName").getPropertyType()).isEqualTo(String.class);

		accessor = createAccessor(new DefaultResourceLoader());

		assertThat(accessor.isReadableProperty("class.package")).isFalse();
		assertThat(accessor.isReadableProperty("class.module")).isFalse();
		assertThat(accessor.isReadableProperty("class.classLoader")).isFalse();
		assertThat(accessor.isReadableProperty("class.name")).isTrue();
		assertThat(accessor.isReadableProperty("class.simpleName")).isTrue();
		assertThat(accessor.isReadableProperty("classLoader")).isTrue();
		assertThat(accessor.isWritableProperty("classLoader")).isTrue();
		OverridingClassLoader ocl = new OverridingClassLoader(getClass().getClassLoader());
		accessor.setPropertyValue("classLoader", ocl);
		assertThat(accessor.getPropertyValue("classLoader")).isSameAs(ocl);

		accessor = createAccessor(new UrlResource("https://spring.io"));

		assertThat(accessor.isReadableProperty("class.package")).isFalse();
		assertThat(accessor.isReadableProperty("class.module")).isFalse();
		assertThat(accessor.isReadableProperty("class.classLoader")).isFalse();
		assertThat(accessor.isReadableProperty("class.name")).isTrue();
		assertThat(accessor.isReadableProperty("class.simpleName")).isTrue();
		assertThat(accessor.isReadableProperty("URL.protocol")).isTrue();
		assertThat(accessor.isReadableProperty("URL.host")).isTrue();
		assertThat(accessor.isReadableProperty("URL.port")).isTrue();
		assertThat(accessor.isReadableProperty("URL.file")).isTrue();
		assertThat(accessor.isReadableProperty("URL.content")).isFalse();
		assertThat(accessor.isReadableProperty("inputStream")).isFalse();
		assertThat(accessor.isReadableProperty("filename")).isTrue();
		assertThat(accessor.isReadableProperty("description")).isTrue();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getPropertyWithOptional() {
		GetterWithOptional target = new GetterWithOptional();
		TestBean tb = new TestBean("x");
		BeanWrapper accessor = createAccessor(target);

		accessor.setPropertyValue("object", tb);
		assertThat(target.value).isSameAs(tb);
		assertThat(target.getObject().get()).isSameAs(tb);
		assertThat(((Optional<TestBean>) accessor.getPropertyValue("object")).get()).isSameAs(tb);
		assertThat(target.value.getName()).isEqualTo("x");
		assertThat(target.getObject().get().getName()).isEqualTo("x");
		assertThat(accessor.getPropertyValue("object.name")).isEqualTo("x");

		accessor.setPropertyValue("object.name", "y");
		assertThat(target.value).isSameAs(tb);
		assertThat(target.getObject().get()).isSameAs(tb);
		assertThat(((Optional<TestBean>) accessor.getPropertyValue("object")).get()).isSameAs(tb);
		assertThat(target.value.getName()).isEqualTo("y");
		assertThat(target.getObject().get().getName()).isEqualTo("y");
		assertThat(accessor.getPropertyValue("object.name")).isEqualTo("y");
	}

	@Test
	public void getPropertyWithOptionalAndAutoGrow() {
		GetterWithOptional target = new GetterWithOptional();
		BeanWrapper accessor = createAccessor(target);
		accessor.setAutoGrowNestedPaths(true);

		accessor.setPropertyValue("object.name", "x");
		assertThat(target.value.getName()).isEqualTo("x");
		assertThat(target.getObject().get().getName()).isEqualTo("x");
		assertThat(accessor.getPropertyValue("object.name")).isEqualTo("x");
	}

	@Test
	public void incompletelyQuotedKeyLeadsToPropertyException() {
		TestBean target = new TestBean();
		BeanWrapper accessor = createAccessor(target);
		assertThatExceptionOfType(NotWritablePropertyException.class).isThrownBy(() ->
				accessor.setPropertyValue("[']", "foobar"))
			.satisfies(ex -> assertThat(ex.getPossibleMatches()).isNull());
	}


	private interface BaseProperty {

		default String getAliasedName() {
			return getName();
		}

		String getName();
	}


	@SuppressWarnings("unused")
	private interface AliasedProperty extends BaseProperty {

		default void setAliasedName(String name) {
			setName(name);
		}

		void setName(String name);
	}


	@SuppressWarnings("unused")
	private static class GetterBean implements AliasedProperty {

		private String name;

		@Override
		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			if (this.name == null) {
				throw new RuntimeException("name property must be set");
			}
			return name;
		}
	}


	@SuppressWarnings("unused")
	private static class IntelliBean {

		public void setName(String name) {
		}

		public void setMyString(String string) {
		}

		public void setMyStrings(String string) {
		}

		public void setMyStriNg(String string) {
		}

		public void setMyStringss(String string) {
		}
	}


	@SuppressWarnings("serial")
	public static class TypedReadOnlyMap extends ReadOnlyMap<String, TestBean> {

		public TypedReadOnlyMap() {
		}

		public TypedReadOnlyMap(Map<? extends String, ? extends TestBean> map) {
			super(map);
		}
	}


	public static class TypedReadOnlyMapClient {

		public void setMap(TypedReadOnlyMap map) {
		}
	}


	public static class PropertyTypeMismatch {

		public String value;

		public void setObject(String object) {
			this.value = object;
		}

		public Integer getObject() {
			return (this.value != null ? this.value.length() : null);
		}
	}


	public static class GetterWithOptional {

		public TestBean value;

		public void setObject(TestBean object) {
			this.value = object;
		}

		public Optional<TestBean> getObject() {
			return Optional.ofNullable(this.value);
		}
	}

}