// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.common;

import lombok.Getter;

@Getter
public class ObjectName {
    String namespace;
    String objectName;

    public ObjectName(String objectName) {
        this.namespace = "public";
        this.objectName = objectName;
    }

    public ObjectName(String namespace, String objectName) {
        this.objectName = objectName;
        this.namespace = namespace;
    }
}
