// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.exceptions;

public class ConflictingOrRedundantOption extends Exception {
    public ConflictingOrRedundantOption(String message) {
        super(message);
    }

    public ConflictingOrRedundantOption(String message, Throwable cause) {
        super(message, cause);
    }
}
