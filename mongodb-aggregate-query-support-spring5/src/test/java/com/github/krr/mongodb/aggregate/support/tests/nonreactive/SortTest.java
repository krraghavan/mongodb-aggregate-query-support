/*
 *  Copyright (c) 2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 *
 */

package com.github.krr.mongodb.aggregate.support.tests.nonreactive;

import com.github.krr.mongodb.aggregate.support.beans.ArtworkSortTestBean;
import com.github.krr.mongodb.aggregate.support.beans.TestSortResultsBean;
import com.github.krr.mongodb.aggregate.support.config.NonReactiveAggregateTestConfiguration;
import com.github.krr.mongodb.aggregate.support.fixtures.AggregateQueryFixtures;
import com.github.krr.mongodb.aggregate.support.repository.nonreactive.TestSortRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Created by rkolliva
 * 1/26/17.
 */


@ContextConfiguration(classes = NonReactiveAggregateTestConfiguration.class)
public class SortTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private TestSortRepository testSortRepository;

  @BeforeClass
  public void setup() throws Exception {
    List<ArtworkSortTestBean> artworks = AggregateQueryFixtures.newArtworkBeansWithTags();
    testSortRepository.insert(artworks);
  }


  @Test
  public void mustSortResults() {
    Assert.assertNotNull(testSortRepository);
    List<TestSortResultsBean> sortResultsBeans = testSortRepository.sortByTags();
    Assert.assertNotNull(sortResultsBeans);
    Assert.assertEquals(sortResultsBeans.size(), 22);
  }
}
