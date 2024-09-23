package io.github.qudtlib.maven.shacl;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.shacl.validation.Severity;
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

    @Parameter(defaultValue = "Violation")
    private ShaclResultSeverity failOnSeverity;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Running SHACL Validations");
        for (DataAndShapes validation : validations) {
            try {
                performShaclValidation(validation);
            } catch (FileNotFoundException e) {
                throw new MojoFailureException("Error performing SHACL validation", e);
            }
        }
    }

    private void performShaclValidation(DataAndShapes dataAndShapes)
            throws MojoFailureException, FileNotFoundException {
        getLog().info("SHACL Validation config ");
        String[] shapesFiles = getFilesForPatterns(dataAndShapes.getShapes());
        String[] dataFiles = getFilesForPatterns(dataAndShapes.getData());
        getLog().info("shapes: " + String.join(", ", shapesFiles));
        getLog().info("data: " + String.join(", ", dataFiles));
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
        writeModelToFile(
                dataAndShapes.getOutputFile(), model, "The validation report was written to %s");
        ValidationReport jenaValidationReport =
                org.apache.jena.shacl.ValidationReport.fromModel(validationReport.getModel());
        getLog().info(
                        String.format(
                                "%d reports found. Severities:",
                                countReports(jenaValidationReport)));
        getLog().info(
                        String.format(
                                "\tsh:Violoation: %d",
                                countReports(jenaValidationReport, Severity.Violation)));
        getLog().info(
                        String.format(
                                "\tsh:Warning   : %d",
                                countReports(jenaValidationReport, Severity.Warning)));
        getLog().info(
                        String.format(
                                "\tsh:Info      : %d",
                                countReports(jenaValidationReport, Severity.Info)));
        boolean buildFails = isBuildFails(jenaValidationReport);
        getLog().info(
                        String.format(
                                "The threshold for failing the build is '%s', therefore, the build %s.",
                                failOnSeverity, buildFails ? "fails" : "succeeds"));
        getLog().info(
                        "To change this behaviour, use the plugin's 'failOnSeverity' parameter (default: 'Violation', other options: 'Warning', 'Info')");
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
