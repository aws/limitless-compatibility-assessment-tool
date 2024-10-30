// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.reportgenerator;

import lombok.Getter;
import lombok.Setter;

import com.amazon.limitless.assessment.configloader.Feature;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;  
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ReportGenerator {
    @Getter
    @Setter
    private String inputFile;
    private String outputDirectory;
    private String engineVersion;
    private long startTime;
    private long endTime;
    private AssessmentSummary assessmentSummary;
    private static ReportGenerator instance = null;

    private static final SimpleDateFormat reportDate = new SimpleDateFormat("yyyy_MMM_dd_HH.mm.ss");
    private static final SimpleDateFormat humanReadable = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
    private static final SimpleDateFormat elapsedTime = new SimpleDateFormat("HH:mm:ss.SSS");

    private static final String REPORT_FILE_NAME = "Report_LimitlessCompatibilityAssessment_";
    private static final String HTML_SUFFIX = ".html";
    private static final String TABLE_OF_CONTENTS_ANCHOR = "Back to Table of Contents";
    private static final String LICENSING_HEADER = "Limitless Compatibility Assessment Tool";
    private static final String REPORT_METRIC_HEADER = "Report Metrics";
    private static final String EXECUTIVE_SUMMARY_HEADER = "Executive Summary for Limitless CAT";
    private static final String TABLE_OF_CONTENTS_HEADER = "Table Of Contents";
    private static final String SUPPORTED_OBJECTS_HEADER = "SQL features 'Supported' in Limitless";
    private static final String UNSUPPORTED_OBJECTS_HEADER = "SQL features 'Not Supported' in Limitless";
    private static final String PROCESS_FAILED_HEADER = "Unable to process";
    private static final String MANUAL_REVIEW_HEADER = "Manual review";

    private static final String EXECUTIVE_SUMMARY_LINK = "execsumm";
    private static final String TABLE_OF_CONTENTS_LINK = "toc";
    private static final String SUPPORTED_SQL_LINK = "objects_supported";
    private static final String UNSUPPORTED_SQL_LINK = "objects_unsupported";
    private static final String PROCESS_FAILED_LINK = "unable_process";
    private static final String MANUAL_REVIEW_LINK = "manual";


    private static final int DIVIDER_LENGTH = 80;

    public static ReportGenerator getInstance()
    {
        if (instance == null)
        {
            instance = new ReportGenerator();
        }
        return instance;
    }

    private ReportGenerator()
    {
        assessmentSummary = AssessmentSummary.getInstance();
    }

    public void createDirectory(String outputDirectory) throws IOException {
        this.outputDirectory = outputDirectory;
        Path path = Paths.get(outputDirectory);
        Files.createDirectory(path);
    }

    public void populateMetdata(String inputFile, String engineVersion, long startTime) {
        this.inputFile = inputFile;
        this.engineVersion = engineVersion;
        this.startTime = startTime;
    }

    public void dumpReport() throws IOException {
        String reportName = outputDirectory + "/" + REPORT_FILE_NAME + reportDate.format(startTime) + HTML_SUFFIX;

        this.endTime = System.currentTimeMillis();
        // Open file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportName)))
        {
            writeHTMLHeader(writer);

            writeLicensingInfo(writer);
            writeReportetrics(writer);
            writeExecutiveSummary(writer);

            writeTableOfContents(writer);

            writeSupportedObjects(writer);
            writeUnsupportedObjects(writer);

            writeCannotProcess(writer);
            writeManualReview(writer);

            writeHTMLEnd(writer);
        }
    }

    private void writeHTMLHeader(BufferedWriter writer) throws IOException
    {
        writer.write("<!doctype html><html><head><title>Limitless CAT Assessment Report</title>\n");
        writer.write("<meta name=\"description\" content=\"Limitless CAT Assessment Report\">\n");
        writer.write("<meta name=\"keywords\" content=\"Limitless CAT Assessment Report\"></head><body><pre>\n");
        writer.write("</head><body><pre>\n");
    }

    private void writeHTMLEnd(BufferedWriter writer) throws IOException
    {
        writer.write("\n");
        writer.write("</pre></body></html>");
    }

    private void writeLicensingInfo(BufferedWriter writer) throws IOException
    {
        writeSectionHeader(writer, LICENSING_HEADER, null);
        writer.write("Limitless Compatibility Tool version: v.1.0, Sept 2024\n");
        writer.write("Compatibility assessment tool for Aurora Limitless for PostgreSQL\n");
        writer.write("Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.\n"); 
        writer.write("\n");
        writer.write("Notice:\n");
        writer.write("This report contains an assessment based on the resources you scanned with the\n");
        writer.write("Limitless Compatibility Assessment tool. The information contained in this report,\n");
        writer.write("including whether or not a feature is 'supported' or 'not supported', is made\n");
        writer.write("available 'as is', and may be incomplete, incorrect, and subject to interpretation.\n");
        writer.write("You should not base decisions on the information in this report without independently\n");
        writer.write("validating it against the actual SQL/DDL code on which this report is based.\n");
        writer.write("\n");
    }

    private void writeReportetrics(BufferedWriter writer) throws IOException
    {
        writeSectionHeader(writer, REPORT_METRIC_HEADER, null);
        writer.write("Target Aurora PostgreSQL Limitless version: " + engineVersion);
        writer.write("\n");
        writer.write("Input file: " + inputFile);
        writer.write("\n");
        writer.write("Output directory: " + outputDirectory);
        writer.write("\n");
        writer.write("Report location:  " + outputDirectory + "/");
        writer.write("\n");
        writer.write(REPORT_FILE_NAME + reportDate.format(startTime) + HTML_SUFFIX);
        writer.write("\n");
        writer.write("Run start            : " + humanReadable.format(startTime));
        writer.write("\n");
        writer.write("Run end              : " + humanReadable.format(endTime));
        writer.write("\n");
        writer.write("Run time             : " + String.valueOf(elapsedTime.format(endTime - startTime)));
        writer.write("\n");
        writer.write("\n");
    }

    private void writeExecutiveSummary(BufferedWriter writer) throws IOException
    {
        writeSectionHeader(writer, EXECUTIVE_SUMMARY_HEADER, EXECUTIVE_SUMMARY_LINK);

        writer.write("Total number of SQLs parsed: " + String.valueOf(assessmentSummary.getTotalStatementCount()));
        writer.write("\n");
        writer.write("\n");
        writer.write("Total SQLs supported by APG Limitless: " + String.valueOf(assessmentSummary.supportedStatements.size()));
        writer.write("\n");
        writer.write("Total SQLs unsupported by APG Limitless: " + String.valueOf(assessmentSummary.unsupportedStatements.size()));
        writer.write("\n");
        writer.write("Unable to process: " + String.valueOf(assessmentSummary.unableToProcessStatements.size()));
        writer.write("\n");
        writer.write("Manual review: ");
        writer.write("\n");
        writer.write("\n");
    }

    private void writeTableOfContents(BufferedWriter writer) throws IOException
    {
        writeSectionHeader(writer, TABLE_OF_CONTENTS_HEADER, TABLE_OF_CONTENTS_LINK);
        writeAnchorLink(writer, EXECUTIVE_SUMMARY_LINK, EXECUTIVE_SUMMARY_HEADER);
        writeAnchorLink(writer, SUPPORTED_SQL_LINK, SUPPORTED_OBJECTS_HEADER);
        writeAnchorLink(writer, UNSUPPORTED_SQL_LINK, UNSUPPORTED_OBJECTS_HEADER);
        writeAnchorLink(writer, PROCESS_FAILED_LINK, PROCESS_FAILED_HEADER);
        writeAnchorLink(writer, MANUAL_REVIEW_LINK, MANUAL_REVIEW_HEADER);

        writer.write("\n\n");
    }

    private void writeSupportedObjects(BufferedWriter writer) throws IOException
    {
        writeSectionHeader(writer, SUPPORTED_OBJECTS_HEADER, SUPPORTED_SQL_LINK);
        for (int supportedIndex : assessmentSummary.supportedStatements)
        {
            writer.write(assessmentSummary.getInput(supportedIndex));
            writer.write("\n");
        }

        writeAnchorLink(writer, TABLE_OF_CONTENTS_LINK, TABLE_OF_CONTENTS_ANCHOR);
        writer.write("\n");
    }

    private void writeUnsupportedObjects(BufferedWriter writer) throws IOException
    {
        writeSectionHeader(writer, UNSUPPORTED_OBJECTS_HEADER, UNSUPPORTED_SQL_LINK);
        for (Map.Entry<Integer,List<String>> unsupportedFeature : assessmentSummary.unsupportedStatements.entrySet())
        {
            writer.write(assessmentSummary.getInput(unsupportedFeature.getKey()));
            for (String errorMessage : unsupportedFeature.getValue())
            {
                writer.write("\t- " + errorMessage + "\n");
            }
            writer.write("\n");
        }

        writeAnchorLink(writer, TABLE_OF_CONTENTS_LINK, TABLE_OF_CONTENTS_ANCHOR);
        writer.write("\n");
    }

    private void writeCannotProcess(BufferedWriter writer) throws IOException
    {
        writeSectionHeader(writer, PROCESS_FAILED_HEADER, PROCESS_FAILED_LINK);

        writer.write("\n\n");
        writeAnchorLink(writer, TABLE_OF_CONTENTS_LINK, TABLE_OF_CONTENTS_ANCHOR);
        writer.write("\n");
    }

    private void writeManualReview(BufferedWriter writer) throws IOException
    {
        writeSectionHeader(writer, MANUAL_REVIEW_HEADER, MANUAL_REVIEW_LINK);

        writer.write("\n\n");
        writeAnchorLink(writer, TABLE_OF_CONTENTS_LINK, TABLE_OF_CONTENTS_ANCHOR);
        writer.write("\n");
    }

    private void writeSectionHeader(BufferedWriter writer, String header, String link) throws IOException
    {
        if (link != null) writer.write("<a name=\"" + link + "\"></a>");
        writeDivider(writer);
        writer.write("--- " + header + " " + String.join("", Collections.nCopies(DIVIDER_LENGTH-5-header.length(), "-")));
        writer.write("\n");
        writeDivider(writer);
    }

    private void writeAnchorLink(BufferedWriter writer, String linkName, String linkContents) throws IOException
    {
        writer.write("<a href=\"#" + linkName + "\">" + linkContents + "</a>\n");
    }

    private void writeDivider(BufferedWriter writer) throws IOException
    {
        writer.write(String.join("", Collections.nCopies(DIVIDER_LENGTH, "-")) + "\n");
    }
}
