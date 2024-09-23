# SHACL for maven builds

2 targets:
- validate:
- infer

Both targets are configured with 2 filesets ('shapes' and 'data') that `<include>` and `<exclude>` files using ant-style patterns.
The output is written to the `<outputFile>`. One may `<skip/>` such a fileset if needed.

For the 'validate' target, this configuration is provided in '<validations>/<validate>' elements. The validiation report is written to the`<outputFile>`.
The optional `<failOnSeverity>` parameter (values `Violation`, `Warning`, `Info`) is used to specify the severity level that causes the build to fail.

For the 'infer' target, these are provided in '<inferences>/<inference>' elements. The inferred triples are written to the`<outputFile>`.

Example configuring both targets and running them in different [build lifecycle phases](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html):

```xml
<build>
	<plugins>
		<plugin>
			[..]
				<groupId>io.github.qudtlib</groupId>
				<artifactId>shacl-maven-plugin</artifactId>
				<version>1.0</version>
				<configuration>
					<validations>
						<validation>
								<skip>false</skip>
								<shapes>
									<include>my/shapes.ttl</include>
								</shapes>
								<data>
									<include>
										**/*.ttl,
									</include>
									<exclude>
										**/*deprecated*.ttl
									</exclude>
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
									<include>
										**/*.ttl,
									</include>
									<exclude>
										**/*deprecated*.ttl
									</exclude>
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
		[...]
	</plugins>
</build>
```
