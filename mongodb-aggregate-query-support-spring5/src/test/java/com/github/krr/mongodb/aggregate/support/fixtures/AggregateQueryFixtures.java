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

package com.github.krr.mongodb.aggregate.support.fixtures;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.github.krr.mongodb.aggregate.support.beans.*;

import java.io.IOException;
import java.util.List;

/**
 * Created by rkolliva
 * 4/5/17.
 */


public class AggregateQueryFixtures {

  private static final String REPLACE_ROOT_DOCS = "[{ \n" +
                                                  "    \"_id\" : 1, \n" +
                                                  "    \"fruit\" : [\n" +
                                                  "        \"apples\", \n" +
                                                  "        \"oranges\"\n" +
                                                  "    ], \n" +
                                                  "    \"in_stock\" : {\n" +
                                                  "        \"oranges\" : 20, \n" +
                                                  "        \"apples\" : 60\n" +
                                                  "    }, \n" +
                                                  "    \"on_order\" : {\n" +
                                                  "        \"oranges\" : 35, \n" +
                                                  "        \"apples\" : 75\n" +
                                                  "    }\n" +
                                                  "},\n" +
                                                  "{ \n" +
                                                  "    \"_id\" : 2, \n" +
                                                  "    \"vegetables\" : [\n" +
                                                  "        \"beets\", \n" +
                                                  "        \"yams\"\n" +
                                                  "    ], \n" +
                                                  "    \"in_stock\" : {\n" +
                                                  "        \"beets\" : 130, \n" +
                                                  "        \"yams\" : 200\n" +
                                                  "    }, \n" +
                                                  "    \"on_order\" : {\n" +
                                                  "        \"beets\" : 90, \n" +
                                                  "        \"yams\" : 145\n" +
                                                  "    }\n" +
                                                  "},\n" +
                                                  "{ \n" +
                                                  "    \"_id\" : 3, \n" +
                                                  "    \"name\" : \"Arlene\", \n" +
                                                  "    \"age\" : 34, \n" +
                                                  "    \"pets\" : {\n" +
                                                  "        \"dogs\" : 2, \n" +
                                                  "        \"cats\" : 1\n" +
                                                  "    }\n" +
                                                  "},\n" +
                                                  "{ \n" +
                                                  "    \"_id\" : 4, \n" +
                                                  "    \"name\" : \"Sam\", \n" +
                                                  "    \"age\" : 41, \n" +
                                                  "    \"pets\" : {\n" +
                                                  "        \"cats\" : 1, \n" +
                                                  "        \"hamsters\" : 3\n" +
                                                  "    }\n" +
                                                  "},\n" +
                                                  "{ \n" +
                                                  "    \"_id\" : 5, \n" +
                                                  "    \"name\" : \"Maria\", \n" +
                                                  "    \"age\" : 25\n" +
                                                  "},\n" +
                                                  "{ \n" +
                                                  "    \"_id\" : 6, \n" +
                                                  "    \"first_name\" : \"Gary\", \n" +
                                                  "    \"last_name\" : \"Sheffield\", \n" +
                                                  "    \"city\" : \"New York\"\n" +
                                                  "},\n" +
                                                  "{ \n" +
                                                  "    \"_id\" : 7, \n" +
                                                  "    \"first_name\" : \"Nancy\", \n" +
                                                  "    \"last_name\" : \"Walker\", \n" +
                                                  "    \"city\" : \"Anaheim\"\n" +
                                                  "},\n" +
                                                  "{ \n" +
                                                  "    \"_id\" : 8, \n" +
                                                  "    \"first_name\" : \"Peter\", \n" +
                                                  "    \"last_name\" : \"Sumner\", \n" +
                                                  "    \"city\" : \"Toledo\"\n" +
                                                  "},\n" +
                                                  "{ \n" +
                                                  "    \"_id\" : 10, \n" +
                                                  "    \"aname\" : \"Susan\", \n" +
                                                  "    \"phones\" : [\n" +
                                                  "        {\n" +
                                                  "            \"cell\" : \"555-653-6527\"\n" +
                                                  "        }, \n" +
                                                  "        {\n" +
                                                  "            \"home\" : \"555-965-2454\"\n" +
                                                  "        }\n" +
                                                  "    ]\n" +
                                                  "},\n" +
                                                  "{ \n" +
                                                  "    \"_id\" : 11, \n" +
                                                  "    \"aname\" : \"Mark\", \n" +
                                                  "    \"phones\" : [\n" +
                                                  "        {\n" +
                                                  "            \"cell\" : \"555-445-8767\"\n" +
                                                  "        }, \n" +
                                                  "        {\n" +
                                                  "            \"home\" : \"555-322-2774\"\n" +
                                                  "        }\n" +
                                                  "    ]\n" +
                                                  "}]\n";

  private static final String TEST_SCORES_DOCUMENTS = "[{\n" +
                                                      "  \"_id\": 1,\n" +
                                                      "  \"student\": \"Maya\",\n" +
                                                      "  \"homework\": [ 10, 5, 10 ],\n" +
                                                      "  \"quiz\": [ 10, 8 ],\n" +
                                                      "  \"extraCredit\": 0\n" +
                                                      "},\n" +
                                                      "{\n" +
                                                      "  \"_id\": 2,\n" +
                                                      "  \"student\": \"Ryan\",\n" +
                                                      "  \"homework\": [ 5, 6, 5 ],\n" +
                                                      "  \"quiz\": [ 8, 8 ],\n" +
                                                      "  \"extraCredit\": 8\n" +
                                                      "}]";

  private static final String TEST_ARTWORK_DOCUMENTS = "[{ \"_id\" : 1, \"title\" : \"The Pillars of Society\", " +
                                                       "    \"artist\" : \"Grosz\", \"year\" : 1926,\n" +
                                                       "    \"price\" : 199.99 },\n" +
                                                       "{ \"_id\" : 2, \"title\" : \"Melancholy III\", " +
                                                       "  \"artist\" : \"Munch\", \"year\" : 1902,\n" +
                                                       "  \"price\" : 280.00 },\n" +
                                                       "{ \"_id\" : 3, \"title\" : \"Dancer\", " +
                                                       "   \"artist\" : \"Miro\", \"year\" : 1925,\n" +
                                                       "   \"price\" : 76.04 },\n" +
                                                       "{ \"_id\" : 4, \"title\" : \"The Great Wave off Kanagawa\"," +
                                                       "  \"artist\" : \"Hokusai\",\n" +
                                                       "  \"price\" : 167.30 },\n" +
                                                       "{ \"_id\" : 5, \"title\" : \"The Persistence of Memory\", " +
                                                       "  \"artist\" : \"Dali\", \"year\" : 1931,\n" +
                                                       "  \"price\" : 483.00 },\n" +
                                                       "{ \"_id\" : 6, \"title\" : \"Composition VII\", " +
                                                       "  \"artist\" : \"Kandinsky\", \"year\" : 1913,\n" +
                                                       "  \"price\" : 385.00 },\n" +
                                                       "{ \"_id\" : 7, \"title\" : \"The Scream\", " +
                                                       "  \"artist\" : \"Munch\", \"year\" : 1893,  \"price\" : 480.00\n" +
                                                       " },\n" +
                                                       "{ \"_id\" : 8, \"title\" : \"Blue Flower\", " +
                                                       "  \"artist\" : \"O'Keefe\", \"year\" : 1918,\n" +
                                                       "    \"price\" : 118.42 }]";

  private static final String TEST_ARTWORK_WITH_TAGS_DOCUMENT = "[{ \"_id\" : 1, \"title\" : \"The Pillars of Society\", \"price\" : 199.99, \"artist\" : \"Grosz\", \"year\" : 1926, \"tags\" : [ \"painting\", \"satire\", \"Expressionism\", \"caricature\" ] },\n" +
                                                                "{ \"_id\" : 2, \"title\" : \"Melancholy III\", \"price\" : 280.00, \"artist\" : \"Munch\", \"year\" : 1902, \"tags\" : [ \"woodcut\", \"Expressionism\" ] },\n" +
                                                                "{ \"_id\" : 3, \"title\" : \"Dancer\", \"price\" : 76.04, \"artist\" : \"Miro\", \"year\" : 1925, \"tags\" : [ \"oil\", \"Surrealism\", \"painting\" ] },\n" +
                                                                "{ \"_id\" : 4, \"title\" : \"The Great Wave off Kanagawa\", \"price\" : 167.30, \"artist\" : \"Hokusai\", \"tags\" : [ \"woodblock\", \"ukiyo-e\" ] },\n" +
                                                                "{ \"_id\" : 5, \"title\" : \"The Persistence of Memory\", \"price\" : 483.00, \"artist\" : \"Dali\", \"year\" : 1931, \"tags\" : [ \"Surrealism\", \"painting\", \"oil\" ] },\n" +
                                                                "{ \"_id\" : 6, \"title\" : \"Composition VII\", \"price\" : 385.00, \"artist\" : \"Kandinsky\", \"year\" : 1913, \"tags\" : [ \"oil\", \"painting\", \"abstract\" ] },\n" +
                                                                "{ \"_id\" : 7, \"title\" : \"The Scream\", \"price\" : 480.00, \"artist\" : \"Munch\", \"year\" : 1893, \"tags\" : [ \"Expressionism\", \"painting\", \"oil\" ] },\n" +
                                                                "{ \"_id\" : 8, \"title\" : \"Blue Flower\", \"price\" : 118.42, \"artist\" : \"O'Keefe\", \"year\" : 1918, \"tags\" : [ \"abstract\", \"painting\" ] }]";

  public static List<TestReplaceRootBean> newReplaceRootFixture() throws IOException {
    ObjectMapper mapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    TypeReference<List<TestReplaceRootBean>> typeReference = new TypeReference<List<TestReplaceRootBean>>() {};
    return mapper.readValue(REPLACE_ROOT_DOCS, typeReference);
  }

  public static List<TestScoreBean> newTestScoresFixture() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    TypeReference<List<TestScoreBean>> typeReference = new TypeReference<List<TestScoreBean>>() {};
    return mapper.readValue(TEST_SCORES_DOCUMENTS, typeReference);
  }


  public static List<Artwork> newArtworkBeans() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    TypeReference<List<Artwork>> typeReference = new TypeReference<List<Artwork>>() {};
    return mapper.readValue(TEST_ARTWORK_WITH_TAGS_DOCUMENT, typeReference);
  }

  public static List<ArtworkSortTestBean> newArtworkBeansWithTags() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    TypeReference<List<ArtworkSortTestBean>> typeReference = new TypeReference<List<ArtworkSortTestBean>>() {};
    return mapper.readValue(TEST_ARTWORK_WITH_TAGS_DOCUMENT, typeReference);
  }

  public static List<ArtworkBucketTestBean> newArtworkBucketBeans() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    TypeReference<List<ArtworkBucketTestBean>> typeReference = new TypeReference<List<ArtworkBucketTestBean>>() {};
    return mapper.readValue(TEST_ARTWORK_DOCUMENTS, typeReference);
  }
}
