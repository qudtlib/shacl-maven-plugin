package io.github.qudtlib.maven.shacl;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.topbraid.jenax.progress.NullProgressMonitor;
import org.topbraid.shacl.rules.RuleUtil;
import org.topbraid.shacl.vocabulary.SH;

@Mojo(name = "infer", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class ShaclInferenceMojo extends AbstractShacMojo {

    @Parameter(required = true)
    private List<DataAndShapes> inferences;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Running SHACL Inferences");
        for (DataAndShapes check : inferences) {
            try {
                performShaclInference(check);
            } catch (FileNotFoundException e) {
                throw new MojoFailureException("Error performing SHACL validation", e);
            }
        }
    }

    private void performShaclInference(DataAndShapes dataAndShapes)
            throws MojoFailureException, FileNotFoundException {
        getLog().info("SHACL Inference configuration:");
        String[] shapesFiles = getFilesForPatterns(dataAndShapes.getShapes());
        String[] dataFiles = getFilesForPatterns(dataAndShapes.getData());
        getLog().info(
                        "\tshapes: "
                                + Arrays.stream(shapesFiles)
                                        .collect(Collectors.joining("\n\t", "\n\t", "\n")));
        getLog().info(
                        "\tdata: "
                                + Arrays.stream(dataFiles)
                                        .collect(Collectors.joining("\n\t", "\n\t", "\n")));
        getLog().info("\toutput: " + dataAndShapes.getOutputFile());
        if (dataAndShapes.getOutputFile() == null) {
            throw new MojoFailureException(
                    "You must specify an output file for the inferred triples!");
        }
        if (dataAndShapes.isSkip()) {
            getLog().info("Inferencing skipped");
            return;
        }
        debug("Loading SHACL shapes");
        Graph shapesGraph = loadRdf(shapesFiles);
        Model shapes = ModelFactory.createModelForGraph(shapesGraph);
        debug("Loading data to infer from");
        Graph dataGraph = loadRdf(dataFiles);
        Model data = ModelFactory.createModelForGraph(dataGraph);
        Model inferences = ModelFactory.createDefaultModel();
        RuleUtil.executeRules(data, shapes, inferences, new NullProgressMonitor());
        writeModelToFile(
                dataAndShapes.getOutputFile(),
                inferences,
                "The inferred triples were written to %s");
    }

    private boolean isValid(Resource validationReport) {
        return !validationReport.hasProperty(SH.result);
    }
}
