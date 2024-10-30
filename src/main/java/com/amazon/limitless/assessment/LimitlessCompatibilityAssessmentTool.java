// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.*;
import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.exceptions.InputValidationException;
import com.amazon.limitless.assessment.exceptions.InvalidPlatformException;
import com.amazon.limitless.assessment.listener.LimitlessAssessmentToolListener;
import com.amazon.limitless.assessment.reportgenerator.AssessmentSummary;
import com.amazon.limitless.assessment.reportgenerator.ReportGenerator;
import com.amazon.limitless.parser.postgresql.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.*;

public class LimitlessCompatibilityAssessmentTool {
    private final static String CONFIG_FILE_PATH = "src/main/resources/config.json";
    private static ConfigLoader configLoader;
    private static AssessmentSummary assessmentSummary;
    private static ReportGenerator reportGenerator;
    private static Options options = null;

    public static void main(String[] args) throws Exception {
        configLoader = ConfigLoader.getInstance();
        assessmentSummary = AssessmentSummary.getInstance();
        reportGenerator = ReportGenerator.getInstance();

        // Develop options for command line
        configLoader = ConfigLoader.getInstance();
        assessmentSummary = AssessmentSummary.getInstance();
        reportGenerator = ReportGenerator.getInstance();
        options = new Options();
        Option help = new Option("h", "help", false, "Display this help message");
        options.addOption(help);
        Option version = new Option("v", "version", false, "Display the tool version");
        options.addOption(version);

        Option inputEngineVersionOption = new Option("e", "engine-version", true, "Input engine version");
        options.addOption(inputEngineVersionOption);

        Option inputFileOption = new Option("i", "input-file", true, "Input file");
        options.addOption(inputFileOption);

        Option outputDirectoryOption = new Option("o", "output-directory", true, "Output directory");
        options.addOption(outputDirectoryOption);

        CommandLineParser commandLineParser = new DefaultParser();
        CommandLine cmd = null;
        try {
            // Parse the command line arguments
            cmd = commandLineParser.parse(options, args);
            if (cmd.hasOption("h")) {
                // Automatically generate the help statement
                displayHelp();
            }
        } catch (ParseException e) {
            displayHelp("Parsing error");
        }

        validateOptions(cmd);
        if (cmd.hasOption("v"))
        {
            // TODO(chsaikia@): manage tool versioning
            System.out.println("Tool version is v1.0");
            System.exit(0);
        }


        final String engineVersion = cmd.getOptionValue("e");
        final String inputFile = cmd.getOptionValue("i");
        final String outputDirectory = cmd.getOptionValue("o");

//        if (StringUtils.isBlank(engineVersion)
//            || (StringUtils.isBlank(inputFile) || StringUtils.isBlank(outputDirectory))) {
//            displayHelp("Input provided is insufficient");
//        }

        //load config file and save to memory
        loadConfigFile(engineVersion);

        // Validation
        // platformCheck();
        validateInputFile(inputFile);
        validateIfOutputDirectoryExists(outputDirectory);

        // Prepare
        long startTime = System.currentTimeMillis();
        prepareReportDirectory(inputFile, outputDirectory, engineVersion, startTime);
        processFileContents(inputFile);

        // Start assessment
        byte[] encoded = Files.readAllBytes(Paths.get(inputFile));
        PostgreSQLLexer lexer = new PostgreSQLLexer(CharStreams.fromString(new String(encoded, StandardCharsets.UTF_8)));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PostgreSQLParser parser = new PostgreSQLParser(tokens);
        LimitlessAssessmentToolListener listener = new LimitlessAssessmentToolListener();
        lexer.removeErrorListeners();
        parser.removeErrorListeners();

        AtomicBoolean isCompiling = new AtomicBoolean(true);
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                    int line, int charPositionInLine, String msg, RecognitionException e) {
                isCompiling.set(false);
            }
        });

        ParseTree tree = parser.root();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(listener, tree);
        if (parser.getNumberOfSyntaxErrors() > 0) {
            throw new RuntimeException("Invalid SQL syntax");
        }

        // Dump report
        dumpReport();
    }

    private static void validateOptions(CommandLine cmd) {
        boolean hasOptionE = cmd.hasOption("e");
        boolean hasOptionI = cmd.hasOption("i");
        boolean hasOptionO = cmd.hasOption("o");
        boolean hasOptionV = cmd.hasOption("v");
        if (hasOptionV && (hasOptionE || hasOptionI || hasOptionO))
        {
            displayHelp("Option --version must not be used with other options");
        }
        else if (hasOptionV || (hasOptionE && hasOptionE && hasOptionO))
        {
            return;
        }
        else if ((hasOptionI && (!hasOptionE || !hasOptionO)) ||
            (hasOptionO && (!hasOptionE || !hasOptionI)) || (hasOptionE && (!hasOptionI || !hasOptionO)))
        {
            displayHelp("Specify all options --input-file, --engine-version and --output-directory");
        }
        else
        {
            displayHelp("Insufficient input");
        }
    }

    private static void displayHelp(String s)
    {
        HelpFormatter formatter = new HelpFormatter();
        // Name to be finalized based on packaging
        formatter.printHelp("limitless-compatibility-tool [options]",
            s,
            options,
            null);
        System.exit(1);
    }
    private static void displayHelp()
    {
        displayHelp(null);
    }

    /* INPUT VALIDATION FUNCTIONS START */
    private static void validateInputFile(String inputFile) throws InputValidationException {
        if (Files.notExists(Paths.get(inputFile)))
        {
            throw new InputValidationException(String.format("Input file [%s] does not exist", inputFile));
        }
    }

    private static void platformCheck() throws Exception {
        // TODO(chsaikia@): Supported platforms is still TBD. Currently, we are only adding support for Amazon Linux 2
        String osName = System.getProperty("os.name").toLowerCase();
        if (!osName.contains("linux")) {
            throw new InvalidPlatformException("Error: The platform is not Amazon Linux 2.");
        }
    }

    private static void loadConfigFile(String version) throws Exception {
        configLoader.readFile(CONFIG_FILE_PATH);
        if (!configLoader.isSupportedVersion(version)) {
            throw new InputValidationException(String.format("Unsupported engine version [%s] specified", version));
        }
        configLoader.loadConfig(version);
    }

    private static void validateIfOutputDirectoryExists(String outputDirectory) throws InputValidationException {
        if (Files.exists(Paths.get(outputDirectory)))
        {
            throw new InputValidationException(String.format("Output directory [%s] already exists. Cannot recreate directory", outputDirectory));
        }
    }

    private static void prepareReportDirectory(String inputFile, String outputDirectory, String engineVersion, long startTime) throws IOException {
        reportGenerator.createDirectory(outputDirectory);
        reportGenerator.populateMetdata(inputFile, engineVersion, startTime);
    }

    /* INPUT VALIDATION FUNCTIONS END */

    /* PREPARE FUNCTIONS START */
    private static void processFileContents(String inputFile) throws IOException {
        String fullPath = Paths.get(inputFile).toAbsolutePath().toString();
        FileInputStream fis = new FileInputStream(fullPath);
        InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(isr);

        final Pattern MULTILINE_COMMENT_START = Pattern.compile("/\\*");
        final Pattern MULTILINE_COMMENT_END = Pattern.compile("\\*/");
        final Pattern SINGLELINE_COMMENT = Pattern.compile("--.*");
        final Pattern CREATE_FUNCTION_START = Pattern.compile("^CREATE\\s+FUNCTION.*", Pattern.CASE_INSENSITIVE);
        final Pattern CREATE_PROCEDURE_START = Pattern.compile("^CREATE\\s+PROCEDURE.*", Pattern.CASE_INSENSITIVE);
        final Pattern DOLLAR_DOUBLE = Pattern.compile("\\$\\$;$");
        final Pattern DOLLAR_UNDERSCORE = Pattern.compile("\\$_\\$;$");

        String line;
        StringBuilder sqlStatement = new StringBuilder();
        boolean inMultilineComment = false;
        boolean inFunctionOrProcedureDefinition = false;

        while ((line = reader.readLine()) != null) {
            line = line.trim();

            // Skip empty lines
            if (line.isEmpty()) {
                continue;
            }

            // Handle multiline comments
            Matcher startMatcher = MULTILINE_COMMENT_START.matcher(line);
            Matcher endMatcher = MULTILINE_COMMENT_END.matcher(line);

            if (startMatcher.find()) {
                inMultilineComment = true;
            }
            if (inMultilineComment) {
                if (endMatcher.find()) {
                    inMultilineComment = false;
                }
                continue;
            }

            // Skip single line comments
            Matcher singleLineMatcher = SINGLELINE_COMMENT.matcher(line);
            if (singleLineMatcher.matches()) {
                continue;
            }

            // Check for the start of a CREATE FUNCTION or CREATE PROCEDURE statement
            if (CREATE_FUNCTION_START.matcher(line).matches() || CREATE_PROCEDURE_START.matcher(line).matches()) {
                inFunctionOrProcedureDefinition = true;
            }

            // Append line to the current SQL statement
            sqlStatement.append(line).append("\n");

            // TODO(chsaikia@): Handle if "CREATE FUNCTION" is in a single line
            // Check if the statement is complete
            if (inFunctionOrProcedureDefinition) {
                // Function/Procedure bodies may contain semicolons, so we need a different end condition
                if (DOLLAR_DOUBLE.matcher(line).find() || DOLLAR_UNDERSCORE.matcher(line).find()) {
                // Print the complete function/procedure definition
                    storeInputSqlInAssessor(sqlStatement.toString());
                    // Reset the StringBuilder for the next statement
                    sqlStatement.setLength(0);
                    inFunctionOrProcedureDefinition = false;
                }
            } else {
                // Regular SQL statements end with a semicolon
                if (line.endsWith(";")) {
                    // Print the complete SQL statement

                    storeInputSqlInAssessor(sqlStatement.toString());
                    // Reset the StringBuilder for the next statement
                    sqlStatement.setLength(0);
                }
            }
        }

        // Print any remaining statement that doesn't end with a semicolon
        if (sqlStatement.length() > 0) {
            storeInputSqlInAssessor(sqlStatement.toString());
        }
    }

    private static void storeInputSqlInAssessor(final String sqlString)
    {
        assessmentSummary.storeInput(sqlString);
    }

    /* REPORT GENERATION FUNCTIONS START */
    private static void dumpReport() throws IOException {
        reportGenerator.dumpReport();
    }

    /* REPORT GENERATION FUNCTIONS END */
}
