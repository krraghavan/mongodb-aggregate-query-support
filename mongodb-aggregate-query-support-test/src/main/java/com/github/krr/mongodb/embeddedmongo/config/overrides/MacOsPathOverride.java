package com.github.krr.mongodb.embeddedmongo.config.overrides;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.Paths;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Platform;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MacOsPathOverride extends Paths {

  private static final Pattern PATTERN = Pattern.compile("(osx/mongodb-)(osx)(.*)$");

  public MacOsPathOverride(Command command) {
    super(command);
  }

  @Override
  public String getPath(Distribution distribution) {
    if (distribution.platform() == Platform.OS_X) {
      String path = super.getPath(distribution);
      Matcher matcher = PATTERN.matcher(path);
      if (!matcher.matches()) {
        throw new IllegalStateException("Unexpected format of path " + path + " for macos download");
      }

      // path has changed from osx to macos - replace this
      return matcher.group(1) + "macos" + // second osx is replaced by macos
             matcher.group(3);
    }
    return super.getPath(distribution);
  }

  @Override
  protected boolean withSsl(Distribution distribution) {
    if (distribution.platform() == Platform.OS_X) {
      return false;
    }
    return super.withSsl(distribution);
  }
}
