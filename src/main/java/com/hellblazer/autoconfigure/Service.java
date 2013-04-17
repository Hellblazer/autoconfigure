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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hellblazer.slp.ServiceReference;
import com.hellblazer.slp.ServiceURL;

/**
 * The definition of a singluar service that needs to be discovered
 * 
 * @author hhildebrand
 * 
 */
public class Service {
	private volatile ServiceReference discovered;

	public String service = "service:someType:someProtocol";
	public String format = "%s:%s";
	public String variable = "service";
	public List<String> properties = new ArrayList<>();
	public Map<String, String> serviceProperties = new HashMap<>();

	public ServiceReference getDiscovered() {
		return discovered;
	}

	public void discover(ServiceReference discovered) {
		this.discovered = discovered;
	}

	/**
	 * @return the resolved value of this service
	 */
	public String resolve() {
		ServiceURL url = discovered.getUrl();
		List<Object> values = new ArrayList<>();
		values.add(url.getHost());
		values.add(url.getPort());
		for (String property : properties) {
			values.add(discovered.getProperties().get(property));
		}
		return String.format(format, values.toArray());
	}

	public String toString() {
		return String.format("Service [%s] properties %s", service,
				serviceProperties);
	}

	/**
	 * @return the query filter for the service collection
	 */
	public String constructFilter() {
		return AutoConfigure.constructFilter(service, serviceProperties);
	}

	/**
	 * @return true if the service has been discovered
	 */
	public boolean isDiscovered() {
		return discovered != null;
	}
}
