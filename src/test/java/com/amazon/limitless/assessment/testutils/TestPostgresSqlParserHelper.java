// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.testutils;

import com.amazon.limitless.parser.postgresql.PostgreSQLLexer;
import com.amazon.limitless.parser.postgresql.PostgreSQLParser;
import org.antlr.v4.runtime.*;

public class TestPostgresSqlParserHelper {
    /**
     * Expects a single SQL query and returns the root context
     * @param sqlQuery
     * @return Root context
     */
    public static PostgreSQLParser.RootContext parseSQL(final String sqlQuery) {
        CharStream input = CharStreams.fromString(sqlQuery);
        PostgreSQLLexer lexer = new PostgreSQLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PostgreSQLParser parser = new PostgreSQLParser(tokens);
        return parser.root();
    }
}
