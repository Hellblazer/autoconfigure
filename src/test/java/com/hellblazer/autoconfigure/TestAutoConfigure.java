/** (C) Copyright 2013 Hal Hildebrand, All Rights Reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package com.hellblazer.autoconfigure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.hellblazer.slp.ServiceListener;
import com.hellblazer.slp.ServiceReference;
import com.hellblazer.slp.ServiceScope;
import com.hellblazer.slp.ServiceURL;
import com.hellblazer.utils.Condition;
import com.hellblazer.utils.TemporaryDirectory;
import com.hellblazer.utils.Utils;

/**
 * @author hhildebrand
 * 
 */
public class TestAutoConfigure {

	@Mock
	private ServiceScope discovery;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testNoServicesRequired() {
		String serviceFormat = "service:test:tcp://%s:%s";
		Map<String, String> serviceProperties = new HashMap<String, String>();
		List<Service> serviceDefinitions = new ArrayList<>();
		List<ServiceCollection> serviceCollectionDefinitions = new ArrayList<>();
		List<File> configurations = new ArrayList<>();
		List<UniqueDirectory> uniqueDirectories = new ArrayList<>();
		Map<String, String> substitutions = new HashMap<>();
		AutoConfigure autoConfigure = new AutoConfigure(serviceFormat, "host",
				"port", "en0", 0, serviceProperties, discovery,
				serviceDefinitions, serviceCollectionDefinitions,
				configurations, substitutions, uniqueDirectories);
		final AtomicBoolean succeeded = new AtomicBoolean();
		final AtomicBoolean completed = new AtomicBoolean();
		Runnable success = new Runnable() {
			@Override
			public void run() {
				succeeded.set(true);
				completed.set(true);
			}
		};
		Runnable failure = new Runnable() {
			@Override
			public void run() {
				succeeded.set(false);
				completed.set(true);
			}
		};
		autoConfigure.configure(success, failure, 10, TimeUnit.MILLISECONDS);
		assertTrue("configuration did not complete",
				Utils.waitForCondition(1000, new Condition() {
					@Override
					public boolean isTrue() {
						return completed.get();
					}
				}));
		assertTrue("configuration did not succeed", succeeded.get());
	}

	@Test
	public void testServiceRegistration() throws Exception {
		String serviceFormat = "service:test:tcp://%s:%s";
		Map<String, String> serviceProperties = new HashMap<String, String>();
		Service service = new Service();
		service.service = "service:testService:tcp";
		List<Service> serviceDefinitions = new ArrayList<>();
		serviceDefinitions.add(service);
		ServiceCollection serviceCollection = new ServiceCollection();
		serviceCollection.service = "service:testServiceCollection:tcp";
		serviceCollection.cardinality = 1;
		List<ServiceCollection> serviceCollectionDefinitions = new ArrayList<>();
		serviceCollectionDefinitions.add(serviceCollection);
		List<File> configurations = new ArrayList<>();
		List<UniqueDirectory> uniqueDirectories = new ArrayList<>();
		Map<String, String> substitutions = new HashMap<>();
		final AtomicBoolean completed = new AtomicBoolean();
		final AtomicBoolean succeeded = new AtomicBoolean();

		ServiceReference serviceRef = mock(ServiceReference.class);
		ServiceURL serviceUrl = mock(ServiceURL.class);
		when(serviceRef.getUrl()).thenReturn(serviceUrl);
		when(serviceUrl.getHost()).thenReturn("example.com");
		when(serviceUrl.getPort()).thenReturn(1);

		ServiceReference serviceCollectionRef = mock(ServiceReference.class);
		ServiceURL serviceCollectionUrl = mock(ServiceURL.class);
		when(serviceCollectionUrl.getHost()).thenReturn("example.com");
		when(serviceCollectionUrl.getPort()).thenReturn(2);
		when(serviceCollectionRef.getUrl()).thenReturn(serviceCollectionUrl);

		Runnable success = new Runnable() {
			@Override
			public void run() {
				succeeded.set(true);
				completed.set(true);
			}
		};
		Runnable failure = new Runnable() {
			@Override
			public void run() {
				succeeded.set(false);
				completed.set(true);
			}
		};

		AutoConfigure autoConfigure = new AutoConfigure(serviceFormat, "host",
				"port", "en0", 0, serviceProperties, discovery,
				serviceDefinitions, serviceCollectionDefinitions,
				configurations, substitutions, uniqueDirectories);
		autoConfigure.configure(success, failure, 100, TimeUnit.MILLISECONDS);
		autoConfigure.discover(serviceRef, service);
		autoConfigure.discover(serviceCollectionRef, serviceCollection);
		assertTrue("configuration did not complete",
				Utils.waitForCondition(1000, new Condition() {
					@Override
					public boolean isTrue() {
						return completed.get();
					}
				}));
		assertTrue("configuration not successful", succeeded.get());

		verify(discovery).addServiceListener(
				isA(ServiceListener.class),
				eq(AutoConfigure.constructFilter(service.service,
						service.serviceProperties)));
		verify(discovery).addServiceListener(
				isA(ServiceListener.class),
				eq(AutoConfigure.constructFilter(serviceCollection.service,
						serviceCollection.serviceProperties)));
	}

	@Test
	public void testConfigurationProcessing() throws Exception {
		try (TemporaryDirectory tempDirectory = new TemporaryDirectory(
				"config-processing", "dir")) {
			Utils.copyDirectory(new File("src/test/resources/configurations"),
					tempDirectory.directory);
			String serviceHost = "example.com";
			int servicePort = 1;
			int serviceCollection1Port = 2;
			int serviceCollection2Port = 2;
			String serviceFormat = "service:test:tcp://%s:%s";
			String hostVariable = "host";
			String portVariable = "port";
			String serviceCollectionVariable = "service.collection";
			String serviceVariable = "service";
			String serviceCollectionConfig = String.format("%s:%s,%s:%s",
					serviceHost, serviceCollection1Port, serviceHost,
					serviceCollection2Port);
			String serviceConfig = String.format("%s:%s", serviceHost,
					servicePort);

			Map<String, String> serviceProperties = new HashMap<String, String>();
			Service service = new Service();
			service.service = "service:testService:tcp";
			service.variable = serviceVariable;
			List<Service> serviceDefinitions = new ArrayList<>();
			serviceDefinitions.add(service);
			ServiceCollection serviceCollection = new ServiceCollection();
			serviceCollection.service = "service:testServiceCollection:tcp";
			serviceCollection.cardinality = 2;
			serviceCollection.variable = serviceCollectionVariable;
			List<UniqueDirectory> uniqueDirectories = new ArrayList<>();
			List<ServiceCollection> serviceCollectionDefinitions = new ArrayList<>();
			serviceCollectionDefinitions.add(serviceCollection);
			List<File> configurations = new ArrayList<>();
			for (File config : tempDirectory.directory.listFiles()) {
				configurations.add(config);
			}
			Map<String, String> substitutions = new HashMap<>();
			substitutions.put("a", "A");
			substitutions.put("b", "B");
			final AtomicBoolean completed = new AtomicBoolean();
			final AtomicBoolean succeeded = new AtomicBoolean();
			Runnable success = new Runnable() {
				@Override
				public void run() {
					succeeded.set(true);
					completed.set(true);
				}
			};
			Runnable failure = new Runnable() {
				@Override
				public void run() {
					succeeded.set(false);
					completed.set(true);
				}
			};
			ServiceReference serviceRef = mock(ServiceReference.class);
			ServiceURL serviceUrl = mock(ServiceURL.class);
			when(serviceRef.getUrl()).thenReturn(serviceUrl);
			when(serviceUrl.getHost()).thenReturn(serviceHost);
			when(serviceUrl.getPort()).thenReturn(servicePort);

			ServiceReference serviceCollection1Ref = mock(ServiceReference.class);
			ServiceURL serviceCollection1Url = mock(ServiceURL.class);
			when(serviceCollection1Url.getHost()).thenReturn(serviceHost);
			when(serviceCollection1Url.getPort()).thenReturn(
					serviceCollection1Port);
			when(serviceCollection1Ref.getUrl()).thenReturn(
					serviceCollection1Url);
			ServiceReference serviceCollection2Ref = mock(ServiceReference.class);
			ServiceURL serviceCollection2Url = mock(ServiceURL.class);
			when(serviceCollection2Url.getHost()).thenReturn(serviceHost);
			when(serviceCollection2Url.getPort()).thenReturn(
					serviceCollection2Port);
			when(serviceCollection2Ref.getUrl()).thenReturn(
					serviceCollection2Url);

			AutoConfigure autoConfigure = new AutoConfigure(serviceFormat,
					hostVariable, portVariable, "en0", 0, serviceProperties,
					discovery, serviceDefinitions,
					serviceCollectionDefinitions, configurations,
					substitutions, uniqueDirectories);
			autoConfigure.configure(success, failure, 100,
					TimeUnit.MILLISECONDS);
			autoConfigure.discover(serviceRef, service);
			autoConfigure.discover(serviceCollection1Ref, serviceCollection);
			autoConfigure.discover(serviceCollection2Ref, serviceCollection);
			assertTrue("configuration did not complete",
					Utils.waitForCondition(1000, new Condition() {
						@Override
						public boolean isTrue() {
							return completed.get();
						}
					}));
			assertTrue("configuration not successful", succeeded.get());
			Properties properties1 = new Properties();
			FileInputStream is = new FileInputStream(new File(
					tempDirectory.directory, "configuration1.properties"));
			properties1.load(is);
			is.close();
			Properties properties2 = new Properties();
			is = new FileInputStream(new File(tempDirectory.directory,
					"configuration2.properties"));
			properties2.load(is);
			is.close();
			InetSocketAddress bound = autoConfigure.getBound();
			assertEquals(bound.getHostName(), properties1.get(hostVariable));
			assertEquals(String.valueOf(bound.getPort()),
					properties1.get(portVariable));
			assertEquals("B", properties1.get("property.b"));
			assertEquals(serviceCollectionConfig,
					properties2.get(serviceCollectionVariable));
			assertEquals(serviceConfig, properties2.get(serviceVariable));
			assertEquals("A", properties2.get("property.a"));
		}
	}
}