// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.common;

import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

public class DependencyObject {
    public static HashMap<String, Set<String>> dependencyObjectMap = new HashMap<>();

    public static Boolean getObject(String fullObjectName, String objectType) {
        Set<String> objectSet = dependencyObjectMap.get(objectType);
        if (objectSet != null) {
            return objectSet.contains(fullObjectName);
        }
        return false;
    }

    public static void setObject(String fullObjectName, String objectType) {
        Set<String> objectSet = dependencyObjectMap.getOrDefault(objectType, new HashSet<>());
        objectSet.add(fullObjectName);
        dependencyObjectMap.put(objectType, objectSet);
    }

    public static String generateFullObjectName(String namespace, String objectName) {
        return String.format("%s.%s", namespace, objectName);
    }
}