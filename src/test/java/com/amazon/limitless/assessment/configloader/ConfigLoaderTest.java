// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.configloader;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.Test;

class ConfigLoaderTest {

    private final static String TEST_CONFIG_FILE = "src/test/resources/config.json";

    @Test
    void testReadFileHappyPath() {
        ConfigLoader configLoader = ConfigLoader.getInstance();
        configLoader.readFile(TEST_CONFIG_FILE);
        String str = "{"
            + "  \"configuration\" : {"
            + "    \"15.5\": {"
            + "      \"extensions\": [\"apgdbcc\"],"
            + "      \"index_types\": [\"HASH\"],"
            + "      \"ddl_feature_config\": {"
            + "        \"create_schema\" :"
            + "        {"
            + "          \"supported\": true"
            + "        },"
            + "        \"create_schema_with_elements\":"
            + "        {"
            + "          \"supported\": false,"
            + "          \"error_message\": \"CREATE SCHEMA WITH ELEMENTS is not supported\""
            + "        },"
            + "        \"alter_schema_owner_to\":"
            + "        {"
            + "          \"supported\": true"
            + "        },"
            + "        \"dummy\":"
            + "        {"
            + "          \"supported\": false,"
            + "          \"error_message\": \"DUMMY is not supported\""
            + "        }"
            + "      }"
            + "    },"
            + "    \"16.2\": {"
            + "      \"inherit_from\": \"15.5\","
            + "      \"extensions\": [],"
            + "      \"ddl_feature_configs\": {"
            + "        \"dummy\":"
            + "        {"
            + "          \"supported\": true"
            + "        }"
            + "      }"
            + "    }"
            + "  }"
            + "}";

        JsonObject jsonObject = JsonParser.parseString(str).getAsJsonObject();
        assertAll(() -> assertTrue(configLoader.getConfigurationJsonObj() != null),
            () -> assertEquals(jsonObject.getAsJsonObject("configuration").toString(),
                configLoader.getConfigurationJsonObj().toString()));
    }

    @Test
    void testIsSupportedVersionHappyPath() {
        ConfigLoader configLoader = ConfigLoader.getInstance();
        configLoader.readFile(TEST_CONFIG_FILE);
        configLoader.loadConfig("16.2");
        assertAll(() -> assertTrue(configLoader.isSupportedVersion("15.5")),
            () -> assertTrue(configLoader.isSupportedVersion("16.2")),
            () -> assertTrue(!configLoader.isSupportedVersion("16.3")));
    }

    @Test
    void testIsSupportedIndexTypeHappyPath() {
        ConfigLoader configLoader = ConfigLoader.getInstance();
        configLoader.readFile(TEST_CONFIG_FILE);
        configLoader.loadConfig("16.2");
        assertAll(() -> assertTrue(configLoader.isSupportedIndexType("HASH")),
            () -> assertTrue(!configLoader.isSupportedIndexType("BTREE")),
            () -> assertTrue(!configLoader.isSupportedIndexType("dummy")));
    }

    @Test
    void testLoadConfigContentHappyPath() {
        ConfigLoader configLoader = ConfigLoader.getInstance();
        configLoader.readFile(TEST_CONFIG_FILE);
        configLoader.loadConfig("16.2");
        Feature expectedFeatureForCreateSchema = new Feature("create_schema", true, null);
        Feature featureForCreateSchema = configLoader.getFeatureConfig("create_schema");
        assertAll(() -> assertEquals(expectedFeatureForCreateSchema.context, featureForCreateSchema.context),
            () -> assertEquals(expectedFeatureForCreateSchema.errorMessage, featureForCreateSchema.errorMessage),
            () -> assertEquals(expectedFeatureForCreateSchema.isSupported, featureForCreateSchema.isSupported()));
        Feature expectedFeatureForCreateSchemaWithElements =
            new Feature("create_schema_with_elements", false, "CREATE SCHEMA WITH ELEMENTS is not supported");
        Feature featureForCreateSchemaWithElements = configLoader.getFeatureConfig("create_schema_with_elements");
        assertAll(() -> assertEquals(expectedFeatureForCreateSchemaWithElements.context,
                featureForCreateSchemaWithElements.context),
            () -> assertEquals(expectedFeatureForCreateSchemaWithElements.errorMessage,
                featureForCreateSchemaWithElements.errorMessage),
            () -> assertEquals(expectedFeatureForCreateSchemaWithElements.isSupported,
                featureForCreateSchemaWithElements.isSupported()));
        Feature expectedFeatureForCreateEventTriggers = new Feature("dummy", true, null);
        Feature featureForCreateEventTriggers = configLoader.getFeatureConfig("dummy");
        assertAll(
            () -> assertEquals(expectedFeatureForCreateEventTriggers.context, featureForCreateEventTriggers.context),
            () -> assertEquals(expectedFeatureForCreateEventTriggers.errorMessage,
                featureForCreateEventTriggers.errorMessage),
            () -> assertEquals(expectedFeatureForCreateEventTriggers.isSupported,
                featureForCreateEventTriggers.isSupported()));
    }
}