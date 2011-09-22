/***
 * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package br.com.caelum.vraptor.vraptor2.outject;

import java.io.IOException;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.vraptor.InterceptionException;
import br.com.caelum.vraptor.core.InterceptorStack;
import br.com.caelum.vraptor.resource.ResourceMethod;
import br.com.caelum.vraptor.vraptor2.ComponentInfoProvider;
import br.com.caelum.vraptor.vraptor2.VRaptorMockery;

public class OutjectionInterceptorTest {

    private VRaptorMockery mockery;
    private OutjectionInterceptor interceptor;
    private InterceptorStack stack;
    private Outjecter outjecter;

    @Before
    public void setup() {
        this.mockery = new VRaptorMockery();
        this.outjecter = mockery.mock(Outjecter.class);
        final ComponentInfoProvider info = mockery.mock(ComponentInfoProvider.class);
        mockery.checking(new Expectations() {
            {
                allowing(info).getOutjecter(); will(returnValue(outjecter));
            }
        });
        this.interceptor = new OutjectionInterceptor(info);
        this.stack = mockery.mock(InterceptorStack.class);
    }

    public static class WithArgsComponent {
        public String isWithArgs(String s) {
            return null;
        }
    }

    public static class NoReturnComponent {
        public void getNoReturn() {
        }
    }

    public static class WeirdIsComponent {
        public void is() {
        }
    }
    
    public class Dog {
        private String name;
        private boolean male;
        public boolean isMale() {
            return male;
        }
        public String getName() {
            return name;
        }
    }

    @Test
    public void shouldComplainAboutGetterWithArgs() throws SecurityException, NoSuchMethodException,
            InterceptionException, IOException {
        final ResourceMethod method = mockery.methodForResource(WithArgsComponent.class);
        final WithArgsComponent component = new WithArgsComponent();
        mockery.checking(new Expectations() {
            {
                one(stack).next(method, component);
            }
        });
        interceptor.intercept(stack, method, component);
        mockery.assertIsSatisfied();
    }

    @Test
    public void shouldIgnoreIsWithNotEnoughChars() throws SecurityException, NoSuchMethodException,
            InterceptionException, IOException {
        final ResourceMethod method = mockery.methodForResource(WeirdIsComponent.class);
        final WeirdIsComponent component = new WeirdIsComponent();
        mockery.checking(new Expectations() {
            {
                one(stack).next(method, component);
            }
        });
        interceptor.intercept(stack, method, component);
        mockery.assertIsSatisfied();
    }

    @Test
    public void shouldComplainAboutNotReturningAnything() throws SecurityException, NoSuchMethodException,
            InterceptionException, IOException {
        final ResourceMethod method = mockery.methodForResource(NoReturnComponent.class);
        final NoReturnComponent component = new NoReturnComponent();
        mockery.checking(new Expectations() {
            {
                one(stack).next(method, component);
            }
        });
        interceptor.intercept(stack, method, component);
        mockery.assertIsSatisfied();
    }

    @Test
    public void shouldOutjectGetAndIsBasedMethod() throws SecurityException, NoSuchMethodException,
            InterceptionException, IOException {
        final Dog dog = new Dog();
        dog.name = "james";
        dog.male = true;
        final ResourceMethod method = mockery.methodForResource(Dog.class);
        mockery.checking(new Expectations() {
            {
                one(outjecter).include("name", "james");
                one(outjecter).include("male", true);
                one(stack).next(method, dog);
            }
        });
        interceptor.intercept(stack, method, dog);
        mockery.assertIsSatisfied();
    }
    
}
