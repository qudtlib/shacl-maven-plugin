package io.github.qudtlib.maven.shacl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.tools.ant.DirectoryScanner;

public abstract class AbstractShacMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    protected File basedir;

    @Parameter(defaultValue = "${project.build.directory}", readonly = true)
    protected File target;

    static String[] splitPatterns(String patterns) {
        return Arrays.stream(patterns.split("(\\s|\n)*(,|\n)(\\s|\n)*"))
                .map(String::trim)
                .toArray(String[]::new);
    }

    protected void writeModelToFile(String outputFile, Model model, String messageFormat)
            throws FileNotFoundException {
        if (outputFile != null) {
            RDFDataMgr.write(
                    new FileOutputStream(new File(basedir, outputFile)),
                    model,
                    RDFLanguages.resourceNameToLang(outputFile, Lang.TTL));
            getLog().info(String.format(messageFormat, outputFile));
        }
    }

    protected Graph loadRdf(String[] files) {
        Graph graph = GraphFactory.createGraphMem();
        for (String file : files) {
            debug("Loading %s", file);
            RDFDataMgr.read(graph, new File(basedir, file).getAbsolutePath());
        }
        return graph;
    }

    protected void debug(String pattern, Object... args) {
        if (getLog().isDebugEnabled()) {
            getLog().debug(String.format(pattern, args));
        }
    }

    protected String[] getFilesForPatterns(IncludeExcludePatterns includeExcludePatterns) {
        String[] includes = AbstractShacMojo.splitPatterns(includeExcludePatterns.getInclude());
        String[] excludes = AbstractShacMojo.splitPatterns(includeExcludePatterns.getExclude());
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(basedir);
        scanner.setIncludes(includes);
        scanner.setExcludes(excludes);
        scanner.scan();
        return scanner.getIncludedFiles();
    }
}
