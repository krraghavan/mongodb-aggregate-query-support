package com.github.krr.mongodb.embeddedmongo.config;

import com.github.krr.mongodb.embeddedmongo.config.impl.LinuxPackageIoUtil;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.config.DownloadConfigBuilder;
import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import de.flapdoodle.embed.process.io.directories.FixedPath;
import de.flapdoodle.embed.process.io.directories.IDirectory;
import de.flapdoodle.embed.process.io.directories.UserHome;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "WeakerAccess"})
@Slf4j
public class Mongo42xDownloadConfigBuilder extends DownloadConfigBuilder {

  private final Optional<String> artifactDownloadLocationEnvironmentVariable;

  private final LinuxPackageIoUtil ioUtil;

  public Mongo42xDownloadConfigBuilder() {
    this(new LinuxPackageIoUtilImpl());
  }

  public Mongo42xDownloadConfigBuilder(LinuxPackageIoUtil ioUtil) {
    this(Optional.ofNullable(System.getenv().get("EMBEDDED_MONGO_ARTIFACTS")), ioUtil);
  }

  protected Mongo42xDownloadConfigBuilder(Optional<String> artifactDownloadLocationEnvironmentVariable,
                                          LinuxPackageIoUtil ioUtil) {
    this.artifactDownloadLocationEnvironmentVariable = artifactDownloadLocationEnvironmentVariable;
    this.ioUtil = ioUtil;
  }

  public DownloadConfigBuilder defaults() {
    fileNaming().setDefault(new UUIDTempNaming());
    downloadPath().setDefault(new Mongo42xDownloadPath());
    progressListener().setDefault(new StandardConsoleProgressListener());
    artifactStorePath().setDefault(defaultArtifactDownloadLocation());
    downloadPrefix().setDefault(new DownloadPrefix("embedmongo-download"));
    userAgent().setDefault(new UserAgent("Mozilla/5.0 (compatible; Embedded MongoDB; +https://github.com/flapdoodle-oss/embedmongo.flapdoodle.de)"));
    return this;
  }

  public DownloadConfigBuilder packageResolverForCommand(Command command) {
    packageResolver(new Mongo42xPaths(command, this.ioUtil));
    return this;
  }

  private IDirectory defaultArtifactDownloadLocation() {
    return artifactDownloadLocationEnvironmentVariable.<IDirectory>map(FixedPath::new)
        .orElseGet(() -> new UserHome(".embedmongo"));
  }


}
