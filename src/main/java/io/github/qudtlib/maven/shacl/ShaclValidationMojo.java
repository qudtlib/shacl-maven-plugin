package io.github.qudtlib.maven.shacl;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.*;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.shacl.validation.Severity;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.topbraid.shacl.validation.ValidationEngineConfiguration;
import org.topbraid.shacl.validation.ValidationUtil;

@Mojo(name = "validate", defaultPhase = LifecyclePhase.TEST)
public class ShaclValidationMojo extends AbstractShacMojo {

    @Parameter(required = true)
    private List<DataAndShapes> validations;

    /**
     * Reports of this or higher severity cause the build to fail. Property: `shacl.severity.fail`
     */
    @Parameter(defaultValue = "Violation", property = "shacl.severity.fail")
    private ShaclResultSeverity failOnSeverity;

    /**
     * Reports of this or higher severity are logged in the output file. Property:
     * `shacl.severity.log`
     */
    @Parameter(defaultValue = "Info", property = "shacl.severity.log")
    private ShaclResultSeverity logSeverity;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info(
                        String.format(
                                "Running %d SHACL Validation%s",
                                validations.size(), validations.size() > 1 ? "s" : ""));
        getLog().info("");
        int i = 0;
        for (DataAndShapes validation : validations) {
            try {
                long start = System.currentTimeMillis();
                performShaclValidation(validation);
                long duration = System.currentTimeMillis() - start;
                getLog().info(
                                String.format(
                                        "Completed SHACL validation in %s",
                                        makeDurationString(duration)));
                if (i < validations.size() - 1) {
                    getLog().info("");
                }
            } catch (FileNotFoundException e) {
                throw new MojoFailureException("Error performing SHACL validation", e);
            }
            i++;
        }
    }

    private void performShaclValidation(DataAndShapes dataAndShapes)
            throws MojoFailureException, FileNotFoundException {
        try {
            if (dataAndShapes.getMessage() != null) {
                getLog().info(dataAndShapes.getMessage());
            }
            getLog().info("SHACL Validation config ");
            String[] shapesFiles = getFilesForPatterns(dataAndShapes.getShapes());
            String[] dataFiles = getFilesForPatterns(dataAndShapes.getData());
            getLog().info("shapes: ");
            Arrays.stream(shapesFiles).sorted().forEach(file -> getLog().info("    " + file));
            getLog().info("data: ");
            Arrays.stream(dataFiles).sorted().forEach(file -> getLog().info("    " + file));
            if (dataAndShapes.isSkip()) {
                getLog().info("Validation skipped");
                return;
            }
            debug("Loading SHACL shapes");
            Graph shapesGraph = loadRdf(shapesFiles);
            Model shapes = ModelFactory.createModelForGraph(shapesGraph);
            debug("Loading data to validate");
            Graph dataGraph = loadRdf(dataFiles);
            Model data = ModelFactory.createModelForGraph(dataGraph);
            Resource validationReport =
                    ValidationUtil.validateModel(
                            data,
                            shapes,
                            new ValidationEngineConfiguration()
                                    .setReportDetails(true)
                                    .setValidateShapes(false));
            Model model = validationReport.getModel();
            ValidationReport jenaValidationReport =
                    org.apache.jena.shacl.ValidationReport.fromModel(model);
            Model filteredModel = filterModel(model);
            ValidationReport jenaValidationReportFiltered =
                    org.apache.jena.shacl.ValidationReport.fromModel(filteredModel);
            long numResults =
                    countReports(
                            jenaValidationReport,
                            Severity.Violation,
                            Severity.Warning,
                            Severity.Info);
            long numResultsFiltered =
                    countReports(
                            jenaValidationReportFiltered,
                            Severity.Violation,
                            Severity.Warning,
                            Severity.Info);
            getLog().info(
                            String.format(
                                    "SHACL Validation report contains %d validation results%s",
                                    numResults,
                                    numResults != numResultsFiltered
                                            ? " (" + numResultsFiltered + " after filtering)"
                                            : ""));
            writeModelToFile(
                    dataAndShapes.getOutputFile(),
                    filteredModel,
                    "The"
                            + (numResults != numResultsFiltered ? " filtered " : " ")
                            + "validation report was written to %s");
            long violations = countReports(jenaValidationReport, Severity.Violation);
            long filteredViolations =
                    countReports(jenaValidationReportFiltered, Severity.Violation);
            long warnings = countReports(jenaValidationReport, Severity.Warning);
            long filteredWarnings = countReports(jenaValidationReportFiltered, Severity.Warning);
            long infos = countReports(jenaValidationReport, Severity.Info);
            long filteredInfos = countReports(jenaValidationReportFiltered, Severity.Info);
            if (numResults > 0) {
                getLog().info("Result Severities: ");
                getLog().info(
                                String.format(
                                        "\tsh:Violation: %d%s",
                                        violations,
                                        violations != filteredViolations
                                                ? " (" + filteredViolations + " after filtering)"
                                                : ""));
                getLog().info(
                                String.format(
                                        "\tsh:Warning: %d%s",
                                        warnings,
                                        warnings != filteredWarnings
                                                ? " (" + filteredWarnings + " after filtering)"
                                                : ""));
                getLog().info(
                                String.format(
                                        "\tsh:Info: %d%s",
                                        infos,
                                        infos != filteredInfos
                                                ? " (" + filteredInfos + " after filtering)"
                                                : ""));
            }
            boolean buildFails = isBuildFails(jenaValidationReport);
            getLog().info(
                            String.format(
                                    "The threshold for logging validation results is '%s'.",
                                    logSeverity));
            getLog().info(
                            "   To change this behaviour, use the plugin's 'logSeverity' parameter (default: 'Info', other options: 'Warning', 'Info', property: shacl.severity.log)");
            getLog().info(
                            String.format(
                                    "The threshold for failing the build is '%s', therefore, the build %s.",
                                    failOnSeverity, buildFails ? "fails" : "succeeds"));
            getLog().info(
                            "   To change this behaviour, use the plugin's 'failOnSeverity' parameter (default: 'Violation', other options: 'Warning', 'Info', property: shacl.severity.fail)");
            if (buildFails) {
                ShLib.printReport(validationReport);
                throw new MojoFailureException(
                        String.format(
                                "SHACL validation failed.\nShapes files: %s\nData files:%s",
                                Arrays.stream(shapesFiles)
                                        .collect(Collectors.joining("\n\t", "\n\t", "\n")),
                                Arrays.stream(dataFiles)
                                        .collect(Collectors.joining("\n\t", "\n\t", "\n"))));
            }
        } catch (Exception e) {
            if (dataAndShapes.getFailureMessage() != null) {
                getLog().info(dataAndShapes.getFailureMessage());
            }
            throw e;
        }
        if (dataAndShapes.getSuccessMessage() != null) {
            getLog().info(dataAndShapes.getSuccessMessage());
        }
    }

    private Model filterModel(Model model) {
        Graph originalGraph = model.getGraph();
        Graph copyGraph = GraphFactory.createGraphMem();
        originalGraph.stream().forEach(copyGraph::add);
        Model filtered = ModelFactory.createModelForGraph(copyGraph);
        filtered.setNsPrefixes(model.getNsPrefixMap());
        List<Resource> results =
                filtered.listStatements(
                                (Resource) null,
                                RDF.type,
                                filtered.asRDFNode(SHACL.ValidationResult))
                        .mapWith(Statement::getSubject)
                        .toList();
        List<Resource> resultsToDelete = new ArrayList<>();
        Property resultSeverityProp =
                filtered.createProperty(
                        filtered.asRDFNode(SHACL.resultSeverity).asResource().getURI());
        for (Resource result : results) {
            List<RDFNode> severityList =
                    filtered.listStatements(result, resultSeverityProp, (RDFNode) null)
                            .mapWith(Statement::getObject)
                            .toList();
            Resource severityRes = severityList.get(0).asResource();
            ShaclResultSeverity shaclResultSeverity =
                    ShaclResultSeverity.valueOf(severityRes.getLocalName());
            if (!shaclResultSeverity.isEqualOrHigher(this.logSeverity)) {
                resultsToDelete.add(result);
            }
        }

        if (!resultsToDelete.isEmpty()) {
            for (Resource toDelete : resultsToDelete) {
                filtered.removeAll(toDelete, null, null);
                filtered.removeAll(null, null, toDelete);
            }
        }
        return filtered;
    }

    private boolean isBuildFails(ValidationReport validationReport) {
        if (failOnSeverity == ShaclResultSeverity.Info) {
            return validationReport.conforms();
        } else if (failOnSeverity == ShaclResultSeverity.Warning) {
            return (countReports(validationReport, Severity.Warning, Severity.Violation) > 0);
        } else if (failOnSeverity == ShaclResultSeverity.Violation) {
            return (countReports(validationReport, Severity.Violation) > 0);
        }
        throw new IllegalStateException(
                String.format(
                        "Cannot handle value '%s' of parameter'%s'",
                        failOnSeverity, "'failOnSeverity'"));
    }

    private long countReports(ValidationReport validationReport, Severity... severities) {
        return validationReport.getEntries().stream()
                .filter(e -> Arrays.stream(severities).anyMatch(s -> e.severity() == s))
                .count();
    }
}
