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

package org.apache.shenyu.plugin.mock;

import org.apache.shenyu.common.dto.RuleData;
import org.apache.shenyu.common.dto.SelectorData;
import org.apache.shenyu.common.dto.convert.rule.MockHandle;
import org.apache.shenyu.plugin.api.ShenyuPluginChain;
import org.apache.shenyu.plugin.base.utils.CacheKeyUtils;
import org.apache.shenyu.plugin.mock.handler.MockPluginHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test case for MockPlugin.
 */
public class MockPluginTest {

    private final ShenyuPluginChain chain = mock(ShenyuPluginChain.class);

    private final MockPlugin mockPlugin = new MockPlugin();

    private RuleData ruleData;

    private ServerWebExchange exchange;

    @BeforeEach
    public void setUp() {
        when(chain.execute(any())).thenReturn(Mono.empty());
        exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/mock").build());
        ruleData = new RuleData();
        ruleData.setSelectorId("selector");
        ruleData.setId("rule");

        MockHandle mockHandle = new MockHandle();
        mockHandle.setResponseContent("{\"message\":\"mock\"}");
        MockPluginHandler.CACHED_HANDLE.get().cachedHandle(CacheKeyUtils.INST.getKey(ruleData), mockHandle);
    }

    @AfterEach
    public void tearDown() {
        MockPluginHandler.CACHED_HANDLE.get().removeHandle(CacheKeyUtils.INST.getKey(ruleData));
    }

    @Test
    public void testUseOkStatusWhenStatusCodeIsNull() {
        StepVerifier.create(mockPlugin.doExecute(exchange, chain, new SelectorData(), ruleData))
                .verifyComplete();

        assertEquals(HttpStatus.OK, exchange.getResponse().getStatusCode());
    }
}
