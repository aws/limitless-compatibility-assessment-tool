// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.configloader;

import com.amazon.limitless.assessment.exceptions.ConfigLoaderInternalError;

import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Reader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

public class ConfigLoader {
    private static JsonObject configurationJsonObj = null;
    private static ConfigLoader instance = null;
    private static Map<String, Feature> featureMap;
    private static Set<String> supportedExtensions;
    private static Set<String> supportedIndexTypes;
    private final static String CONFIGURATION = "configuration";
    private final static String SUPPORTED = "supported";
    private final static String ERROR_MESSAGE = "error_message";
    private final static String INHERIT_FROM = "inherit_from";
    // TODO(chsaikia@): Remove default_error_string from config.json and store it in ConfigLoader class
    // TODO(tanyagp@): Add another feature default_unexpected_feature for unexpected SQLs later.
    public static final Feature DEFAULT_UNSUPPORTED_FEATURE = new Feature("unsupported", false, "This statement is not supported");
    public static final Feature DEFAULT_SUPPORTED_FEATURE = new Feature("supported", true, null);
    private final static String SUPPORTED_EXTENSIONS = "extensions";
    private final static String SUPPORTED_INDEX_TYPES = "index_types";

    public static ConfigLoader getInstance() {
        if (instance == null)
        {
            instance = new ConfigLoader();
        }
        return instance;
    }

    private ConfigLoader() {}

    /**
     * public method to read config file
     */
    public void readFile(String filePath) {
        Path path = Paths.get(filePath);
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonParser parser = new JsonParser();
            JsonObject jsonObj = parser.parse(reader).getAsJsonObject();
            //Parse configuration from config
            if (!jsonObj.has(CONFIGURATION)) {
                throw new ConfigLoaderInternalError("Key configuration not exist in config");
            }
            configurationJsonObj  = jsonObj.getAsJsonObject(CONFIGURATION);
        } catch (FileNotFoundException e) {
            //TODO: Adding excepetion handling
            e.printStackTrace();
        } catch (IOException e) {
            //TODO: Adding exception handling
            e.printStackTrace();
        } catch (JsonParseException e) {
            //TODO: Adding exception handling
            e.printStackTrace();
        } catch (Exception e) {
            //TODO: Adding exception handling
            e.printStackTrace();
        }
    }

    /**
     * public method to get loaded configuration json object
     */
    public JsonObject getConfigurationJsonObj() {
        return configurationJsonObj;
    }


    /**
     * public method to check if inputted version is in config
     */
    public boolean isSupportedVersion(String version) {
        return configurationJsonObj.asMap().keySet().contains(version);
    }

    /**
     * public method to load feature configs to feature map based on loading path
     */
    public void loadConfig(String version) {
        List<String> loadingPath = createLoadingPath(version);
        loadConfigContent(loadingPath);
    }

    /**
     * private method to generate loading path
     */
    private List<String> createLoadingPath(String version) {
        List<String> loadingPath = new ArrayList<>();
        try {
            String versionPointer = version;
            while (true) {
                loadingPath.add(versionPointer);
                if (!configurationJsonObj.has(versionPointer)) {
                    throw new ConfigLoaderInternalError("version " + versionPointer + "not exist in config");
                }
                JsonObject versionConfigurationJsonObj = configurationJsonObj.getAsJsonObject(versionPointer);
                if (!versionConfigurationJsonObj.has(INHERIT_FROM)) {
                    break;
                }
                versionPointer = versionConfigurationJsonObj.get(INHERIT_FROM).getAsString();
            }
        }
        catch (ConfigLoaderInternalError e) {
            //TODO: Adding exception handling
            e.printStackTrace();
        } catch (JsonParseException e) {
            //TODO: Adding exception handling
            e.printStackTrace();
        } catch (Exception e) {
            //TODO: Adding exception handling
            e.printStackTrace();
        }
        //Reverse the loading path, we need load config from beginning to end
        Collections.reverse(loadingPath);
        return loadingPath;
    }

    /**
     * Private method to load feature configs to feature map from beginning of loading path to the end
     */
    private void loadConfigContent(List<String> loadingPath) {
        featureMap = new HashMap<>();
        supportedExtensions = new HashSet<>();
        supportedIndexTypes = new HashSet<>();
        try {
            // Iteratively loading config from beginning to end
            for (String engine : loadingPath) {
                JsonObject engineConfigurationJsonObj = configurationJsonObj.getAsJsonObject(engine);
                // We will support below 2 types config in engine configuration
                // Type 1: Feature: [Supported_Individual_Feature_1, Supported_Individual_Feature_2]
                // Example: "extensions": ["apgdbcc", "aws_commons"]
                // Type 2: Feature: {Individual_Feature: {supported: boolean, error_message: string}}
                // Example:"ddl_feature_config": {"create_schema": {"supported": true}}
                
                // Load supported extensions
                if (engineConfigurationJsonObj.has(SUPPORTED_EXTENSIONS)) {
                    JsonArray extensionsArray = engineConfigurationJsonObj.getAsJsonArray(SUPPORTED_EXTENSIONS);
                    for (JsonElement extensionElement : extensionsArray) {
                        supportedExtensions.add(extensionElement.getAsString());
                    }
                }

                // Load supported index types
                if (engineConfigurationJsonObj.has(SUPPORTED_INDEX_TYPES)) {
                    JsonArray indexTypesArray = engineConfigurationJsonObj.getAsJsonArray(SUPPORTED_INDEX_TYPES);
                    for (JsonElement indexTypeElement : indexTypesArray) {
                        supportedIndexTypes.add(indexTypeElement.getAsString());
                    }
                }

                for (String feature : engineConfigurationJsonObj.asMap().keySet()) {
                    JsonElement featureJsonElement = engineConfigurationJsonObj.get(feature);
                    if (featureJsonElement.isJsonArray()) {
                        JsonArray featureJsonArray = featureJsonElement.getAsJsonArray();
                        for (JsonElement individualFeature : featureJsonArray) {
                            featureMap.put(individualFeature.getAsString(), new Feature(individualFeature.getAsString(),
                                true, null));
                        }
                    }
                    else if (featureJsonElement.isJsonObject()) {
                        JsonObject featureJsonObj = featureJsonElement.getAsJsonObject();
                        for (String individualFeatureStr : featureJsonObj.asMap().keySet()) {
                            JsonObject individualFeatureObj = featureJsonObj.getAsJsonObject(individualFeatureStr);
                            if (!individualFeatureObj.has(SUPPORTED)) {
                                throw new ConfigLoaderInternalError("Key supported not exist in individual feature config");
                            }
                            boolean individualFeatureSupport = individualFeatureObj.get(SUPPORTED).getAsBoolean();
                            if (!individualFeatureSupport && !individualFeatureObj.has(ERROR_MESSAGE)) {
                                throw new ConfigLoaderInternalError("Key error_message not exist in individual feature config");
                            }
                            featureMap.put(individualFeatureStr, new Feature(individualFeatureStr,
                                individualFeatureSupport, individualFeatureSupport ? null :
                                    individualFeatureObj.get(ERROR_MESSAGE).getAsString()));
                        }
                    }
                }
            }
        }
        catch (ConfigLoaderInternalError e) {
            //TODO: Adding exception handling
            e.printStackTrace();
        }catch (JsonParseException e) {
            //TODO: Adding exception handling
            e.printStackTrace();
        } catch (Exception e) {
            //TODO: Adding exception handling
            e.printStackTrace();
        }
    }

    /**
     * public method to check if the inputted feature is supported or not
     */
    public Feature getFeatureConfig(String featureName) {
        return featureMap.getOrDefault(featureName, DEFAULT_UNSUPPORTED_FEATURE);
    }

    /**
     * public method to check if the extension is supported
     */
    public boolean isSupportedExtension(String extensionName) {
        return supportedExtensions.contains(extensionName);
    }

    /**
     * public method to check if the index types is supported
     */
    public boolean isSupportedIndexType(String indexType) {
        return supportedIndexTypes.contains(indexType.toUpperCase(Locale.ROOT));
    }
}
