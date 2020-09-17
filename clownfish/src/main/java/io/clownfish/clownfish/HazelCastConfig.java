/*
 * Copyright 2020 sulzbachr.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.clownfish.clownfish;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author sulzbachr
 */
@Configuration
@EnableCaching
public class HazelCastConfig {
    @Bean
    public HazelcastCacheManager hazelcastCacheManager() {
        return new HazelcastCacheManager(Hazelcast.newHazelcastInstance(hazelcastConfig()));
    }
    
    @Bean
    public Config hazelcastConfig() {
        return new Config()
                .setInstanceName("clownfish-instance")
                .addMapConfig(new MapConfig()
                        .setName("clownfishCache")
                        .setEvictionPolicy(EvictionPolicy.LRU)
                        .setStatisticsEnabled(true));
    }
}
