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

package com.github.krr.mongodb.aggregate.support.tests;

import com.github.krr.mongodb.aggregate.support.beans.Artwork;
import com.github.krr.mongodb.aggregate.support.config.ReactiveAggregateTestConfiguration;
import com.github.krr.mongodb.aggregate.support.fixtures.AggregateQueryFixtures;
import com.github.krr.mongodb.aggregate.support.repository.ReactiveArtworkRepository;
import com.github.krr.mongodb.aggregate.support.repository.ReactiveComplexQueryRepository;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.testng.Assert.*;

/**
 * Created by rkolliva
 * 2/19/17.
 */

@SuppressWarnings({"ConstantConditions", "Duplicates"})
@ContextConfiguration(classes = ReactiveAggregateTestConfiguration.class)
public class ReactiveAggregateFacetTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private ReactiveArtworkRepository artworkRepository;

  @Autowired
  private ReactiveComplexQueryRepository complexQueryRepository;

  private int expectedArtworkCount;

  @BeforeClass
  public void setup() throws Exception {
    List<Artwork> artworks = AggregateQueryFixtures.newArtworkBeans();
    artworkRepository.insert(artworks).collectList().block();
    expectedArtworkCount = artworks.size();
  }

  @Test
  public void mustReturnBucketsFromRepository2() {
    assertNotNull(artworkRepository, "Must have a repository");
    List<Artwork> artworks = artworkRepository.findAll().collectList().block();
    assertNotNull(artworks);
    assertEquals(artworks.size(), expectedArtworkCount);
    Map<String, Object> facets = artworkRepository.getFacetResults2().block();
    assertNotNull(facets);
    assertEquals(facets.size(), 2);
    assertTrue(facets.containsKey("categorizedByTags"));
    assertTrue(facets.containsKey("categorizedByPrice"));
  }

  @Test
  public void mustReturnResultsWithMultipleFacets() {
    assertNotNull(artworkRepository, "Must have a repository");
    List<Artwork> artworks = artworkRepository.findAll().collectList().block();
    assertNotNull(artworks);
    assertEquals(artworks.size(), expectedArtworkCount);
    Map<String, Object> facets = artworkRepository.getFacetResultsWithMultipleFacets().block();
    assertNotNull(facets);
    assertEquals(facets.size(), 1);
    assertTrue(facets.containsKey("count"));
  }

  @Test
  public void mustHonorConditionalsInFacetPipeline() {
    assertNotNull(artworkRepository, "Must have a repository");
    List<Artwork> artworks = artworkRepository.findAll().collectList().block();
    assertNotNull(artworks);
    assertEquals(artworks.size(), expectedArtworkCount);
    Map<String, Object> facets = artworkRepository.getFacetResultsWithConditional(false).block();
    assertNotNull(facets);
    assertEquals(facets.size(), 1);
    assertFalse(facets.containsKey("categorizedByTags"));
    assertTrue(facets.containsKey("categorizedByPrice"));
  }

  @Test
  public void mustHonorConditionalsInFacetPipeline2() {
    assertNotNull(artworkRepository, "Must have a repository");
    List<Artwork> artworks = artworkRepository.findAll().collectList().block();
    assertNotNull(artworks);
    assertEquals(artworks.size(), expectedArtworkCount);
    Map<String, Object> facets = artworkRepository.getFacetResultsWithConditional(true).block();
    assertNotNull(facets);
    assertEquals(facets.size(), 2);
    assertTrue(facets.containsKey("categorizedByTags"));
    assertTrue(facets.containsKey("categorizedByPrice"));
  }

  @Test
  public void mustHonorConditionalsInFacetPipelineStage() {
    assertNotNull(artworkRepository, "Must have a repository");
    List<Artwork> artworks = artworkRepository.findAll().collectList().block();
    assertNotNull(artworks);
    Map<String, Object> facets = artworkRepository.getFacetResultsWithConditional(true, false,
                                                                                  true).block();
    assertNotNull(facets);
    assertEquals(facets.size(), 2);
    assertTrue(facets.containsKey("categorizedByTags"));
    assertTrue(facets.containsKey("categorizedByPrice"));
    Object priceCategories = facets.get("categorizedByPrice");
    assertNotNull(priceCategories);
    List<?> priceCategoryList = (List<?>)priceCategories;
    assertEquals(priceCategoryList.size(), 3);
  }

  @Test
  public void mustHonorConditionalsInFacetPipelineStage2() {
    assertNotNull(artworkRepository, "Must have a repository");
    List<Artwork> artworks = artworkRepository.findAll().collectList().block();
    assertNotNull(artworks);
    assertEquals(artworks.size(), expectedArtworkCount);
    Map<String, Object> facets = artworkRepository.getFacetResultsWithConditional(true, true,
                                                                                  false).block();
    assertNotNull(facets);
    assertEquals(facets.size(), 2);
    assertTrue(facets.containsKey("categorizedByTags"));
    assertTrue(facets.containsKey("categorizedByPrice"));
    Object priceCategories = facets.get("categorizedByPrice");
    assertNotNull(priceCategories);
    List<?> priceCategoryList = (List<?>)priceCategories;
    assertEquals(priceCategoryList.size(), 2);
  }

  @Test
  public void mustDoOrAndOnConditionalsInFacetPipelineStage2() {
    assertNotNull(artworkRepository, "Must have a repository");
    List<Artwork> artworks = artworkRepository.findAll().collectList().block();
    assertNotNull(artworks);
    assertEquals(artworks.size(), expectedArtworkCount);

    //When 2 conditions are false, should limit the results to 3
    Map<String, Object> facets = artworkRepository.getFacetResultsOnMultipleConditionals(false, false).block();
    assertNotNull(facets);
    Object limitPipeline = facets.get("limitPipeline");
    assertNotNull(limitPipeline);
    assertTrue(limitPipeline instanceof List);
    List<?> limitPipelineList = (List<?>)limitPipeline;
    assertEquals(limitPipelineList.size(), 3);

    //When 1 condition is false and the other is true, should limit the results to 2
    facets = artworkRepository.getFacetResultsOnMultipleConditionals(false, true).block();
    assertNotNull(facets);
    limitPipeline = facets.get("limitPipeline");
    assertNotNull(limitPipeline);
    assertTrue(limitPipeline instanceof List);
    limitPipelineList = (List<?>)limitPipeline;
    assertEquals(limitPipelineList.size(), 2);
  }

  // fix for https://github.com/krraghavan/mongodb-aggregate-query-support/issues/50
  @Test
  public void mustProcessFacetPipelineStageWithAtAtPh() {
    String projectStr = "{'" + randomAlphabetic(5) + "':'" + randomAlphabetic(10) + "'}";
    String addFieldsStr = "{'" + randomAlphabetic(5) + "':'" + randomAlphabetic(10) + "'}";

    //noinspection ReactiveStreamsUnusedPublisher
    artworkRepository.facetPipelineStageWithAtAtPh(10L, RandomUtils.nextLong(), projectStr);

    complexQueryRepository.findMappingsByTypeAndVersion("1.2.3", ReactiveComplexQueryRepository.MappingType.BAR);

    String matchStr = "{'" + randomAlphabetic(5) + "':'" + randomAlphabetic(10) + "'}";
    String colMatchStr = "{'" + randomAlphabetic(5) + "':'" + randomAlphabetic(10) + "'}";
    String sortStr = "{'" + randomAlphabetic(5) + "':1}";

    //noinspection ReactiveStreamsUnusedPublisher
    complexQueryRepository.getPrefixesAggQuery(RandomUtils.nextLong(),
                                               null,
                                               addFieldsStr,
                                               matchStr,
                                               colMatchStr,
                                               RandomUtils.nextInt(),
                                               RandomUtils.nextInt(),
                                               RandomUtils.nextInt(),
                                               RandomUtils.nextInt(),
                                               RandomUtils.nextInt(),
                                               sortStr,
                                               RandomUtils.nextInt(),
                                               RandomUtils.nextInt(),
                                               UUID.randomUUID().toString()
                                              );

  }


}
