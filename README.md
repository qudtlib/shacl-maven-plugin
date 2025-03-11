# SHACL for maven builds

## Targets
 
`validate`: 
- defines a set of RDF files as 'data', 
- defines a set of RDF files as 'shapes', 
- performs [SHACL validation](https://www.w3.org/TR/shacl/#validation) of the data using the shapes
- writes the validation report (in RDF) to an output file
- causes the build to fail if there is one report at or above the `failOnSeverity` threshold (default:`Violation`)

`infer`:
- defines a set of RDF files as 'data',
- defines a set of RDF files as 'shapes',
- performs [SHACL-AF inference](https://www.w3.org/TR/shacl-af/#rules) of the data using the shapes
- writes the inferred triples  to an output file
 
## Configuration

Both targets are configured with 2 filesets (`<shapes>` and `<data>`) that `<include>` and `<exclude>` files using comma/newline-separated ant-style patterns.
The output is written to the `<outputFile>`. One may `<skip/>` such a fileset if needed.

For the 'validate' target, this configuration is provided in `<validations>/<validate>` elements. The validiation report is written to the`<outputFile>`.

The optional `<failOnSeverity>` parameter (maven property `shacl.severity.fail`, values `Violation`, `Warning`, `Info`, default: `Violation`) is used to specify the severity level that causes the build to fail. 

The optional `<logSeverity>` parameter (maven property `shacl.severity.log`, values `Violation`, `Warning`, `Info`, default: `Info`) is used to filter the validation results - only results with equal or higher severity are logged.

For the 'infer' target, the configuration is provided in `<inferences>/<inference>` elements. The inferred triples are written to the`<outputFile>`.

### Messages

Both `<validation>` and `<inference>` elements allow for 
* `<message>` - displayed at level `INFO` before the validation/inference is executed 
* `<failureMessage>` - displayed at level `INFO` if anything goes wrong
* `<successMessage>` - displayed at level `INFO`  if there are no problems

## Example

Example configuring both targets and running them in different [build lifecycle phases](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html):

```xml
<build> 
  <plugins>
    <plugin>
      <groupId>io.github.qudtlib</groupId>
      <artifactId>shacl-maven-plugin</artifactId>
      <version>1.0.0</version>
      <configuration>
        <validations>
          <validation>
            <message>Running normal validations</message>
            <failureMessage>ttl files are invalid - please check the validation report</failureMessage>
            <successMessage>ttl files are valid</successMessage>
            <skip>false</skip>
            <shapes>
              <include>my/shapes.ttl</include>
            </shapes>
            <data>
              <include>**/*.ttl</include>
              <exclude>**/*deprecated*.ttl</exclude>
            </data>
            <outputFile>target/validationReport.ttl</outputFile>
          </validation>
        </validations>
        <inferences>
          <inference>
            <shapes>
              <include>my/rules.ttl</include>
            </shapes>
            <data>
              <include>**/*.ttl</include>
              <exclude>**/*deprecated*.ttl</exclude>
            </data>
            <outputFile>target/inferences.ttl</outputFile>
          </inference>
        </inferences>
      </configuration>
      <executions>
        <execution>
          <id>infer</id>
          <phase>compile</phase>
          <goals>
            <goal>infer</goal>
          </goals>
        </execution>
        <execution>
          <id>validate</id>
          <phase>test</phase>
          <goals>
            <goal>validate</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```
