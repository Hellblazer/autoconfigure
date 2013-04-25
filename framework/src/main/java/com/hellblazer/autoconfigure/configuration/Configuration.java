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
package com.hellblazer.autoconfigure.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hellblazer.gossip.configuration.GossipConfiguration;

/**
 * @author hhildebrand
 * 
 */
public class Configuration {
	public List<String> additionalPorts = new ArrayList<>();
	public int addressIndex = 0;
	public GossipConfiguration gossip = new GossipConfiguration();
	public String networkInterface = "eth0";
	public List<ServiceCollection> serviceCollections = new ArrayList<>();
	public Map<String, String> serviceProperties = new HashMap<>();
	public List<SingletonService> services = new ArrayList<>();
	public String serviceUrl = "service:someType:http://%s:%s/myURI";
	public List<Template> templates = new ArrayList<>();
	public String totalOrderingFrom = "services";
	public String totalOrderingVariable = "id";
	public List<UniqueDirectory> uniqueDirectories = new ArrayList<>();
	public Map<String, String> variables = new HashMap<>();
	public boolean verboseTemplating = false;
}