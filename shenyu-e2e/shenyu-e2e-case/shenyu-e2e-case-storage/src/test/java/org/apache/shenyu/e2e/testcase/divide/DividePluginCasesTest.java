/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shenyu.e2e.testcase.divide;

import org.apache.shenyu.e2e.engine.scenario.specification.ScenarioSpec;
import org.apache.shenyu.e2e.model.ResourcesData.Resource;
import org.apache.shenyu.e2e.model.handle.Upstreams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verify that selector and discovery upstreams use the same storage test backend.
 */
@ResourceLock(Resources.SYSTEM_PROPERTIES)
class DividePluginCasesTest {

    @Test
    void testComposeUpstream() {
        assertUpstream("shenyu-httpbin:80", "shenyu-httpbin:80");
    }

    @Test
    void testDefaultUpstreamForExistingKubernetesRunners() {
        assertUpstream(null, "httpbin.org");
    }

    private void assertUpstream(final String configured, final String expected) {
        String key = "shenyu.e2e.storage.upstream";
        String previous = System.getProperty(key);
        try {
            if (Objects.isNull(configured)) {
                System.clearProperty(key);
            } else {
                System.setProperty(key, configured);
            }
            List<ScenarioSpec> scenarios = new DividePluginCases().get();
            assertEquals(8, scenarios.size());
            for (ScenarioSpec scenario : scenarios) {
                List<Resource> resources = scenario.getBeforeEachSpec().getResources().getResources();
                assertEquals(1, resources.size());
                Resource resource = resources.get(0);
                Upstreams handle = (Upstreams) resource.getSelector().getHandle();
                assertEquals(expected, handle.getUpstreams().get(0).getUpstreamUrl());
                assertEquals(expected, resource.getBindingData().getDiscoveryUpstreams().get(0).getUrl());
                assertEquals("http://", resource.getBindingData().getDiscoveryUpstreams().get(0).getProtocol());
            }
        } finally {
            if (Objects.isNull(previous)) {
                System.clearProperty(key);
            } else {
                System.setProperty(key, previous);
            }
        }
    }
}
