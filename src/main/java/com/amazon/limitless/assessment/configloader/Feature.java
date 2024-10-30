// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.configloader;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Feature {
    String context;
    boolean isSupported;
    String errorMessage;

    public Feature(String context, boolean isSupported, String errorMessage) {
        this.context = context;
        this.isSupported = isSupported;
        this.errorMessage = errorMessage;
    }

    public boolean supported() {
        return this.isSupported;
    }

}
