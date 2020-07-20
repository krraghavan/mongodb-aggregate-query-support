package com.github.krr.mongodb.embeddedmongo.config;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.Paths;
import de.flapdoodle.embed.mongo.distribution.Feature;
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.BitSize;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Platform;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("WeakerAccess")
@Slf4j
public class Mongo42xPaths extends Paths {

  public Mongo42xPaths(Command command) {
    super(command);
  }

  @Override
  public String getPath(Distribution distribution) {
    String versionStr = getVersionPart(distribution.getVersion());

    if (distribution.getPlatform() == Platform.Solaris && isFeatureEnabled(distribution, Feature.NO_SOLARIS_SUPPORT)) {
      throw new IllegalArgumentException("Mongodb for solaris is not available anymore");
    }

    ArchiveType archiveType = getArchiveType(distribution);
    String archiveTypeStr = getArchiveString(archiveType);

    String platformStr = getPlattformString(distribution);

    String bitSizeStr = getBitSize(distribution);

    if ((distribution.getBitsize() == BitSize.B64) && (distribution.getPlatform() == Platform.Windows)) {
      versionStr = (useWindows2008PlusVersion(distribution) ? "2008plus-": "")
                   + (withSsl(distribution) ? "ssl-": "")
                   + versionStr;
    }
    if (distribution.getPlatform() == Platform.OS_X && withSsl(distribution) ) {
      return platformStr + "/mongodb-macos" + "-ssl-" + bitSizeStr + "-" + versionStr + "." + archiveTypeStr;
    }
    else if(distribution.getPlatform() == Platform.OS_X && !withSsl(distribution) ) {
      return platformStr + "/mongodb-macos" + "-" + bitSizeStr + "-" + versionStr + "." + archiveTypeStr;
    }
    else {
      String osDist = System.getenv("OS_DIST");
      if(distribution.getPlatform() == Platform.Linux && "ubuntu1604".equalsIgnoreCase(osDist)) {
        // right now build this for travisci
        //http://downloads.mongodb.org/linux/mongodb-linux-x86_64-ubuntu1604-4.2.4.tgz
        log.error("Downloading Mongo {} for Ubuntu 16.04", versionStr);
        return "linux/mongodb-linux-x86_64-ubuntu1604-" + versionStr + "." + archiveTypeStr;
      }
    }
    return super.getPath(distribution);
  }

  @SuppressWarnings("SameParameterValue")
  private static boolean isFeatureEnabled(Distribution distribution, Feature feature) {
    return (distribution.getVersion() instanceof IFeatureAwareVersion
            &&  ((IFeatureAwareVersion) distribution.getVersion()).enabled(feature));
  }

  private String getArchiveString(ArchiveType archiveType) {
    String sarchiveType;
    switch (archiveType) {
      case TGZ:
        sarchiveType = "tgz";
        break;
      case ZIP:
        sarchiveType = "zip";
        break;
      default:
        throw new IllegalArgumentException("Unknown ArchiveType " + archiveType);
    }
    return sarchiveType;
  }

  private String getPlattformString(Distribution distribution) {
    String splatform;
    switch (distribution.getPlatform()) {
      case Linux:
        splatform = "linux";
        break;
      case Windows:
        splatform = "win32";
        break;
      case OS_X:
        splatform = "osx";
        break;
      case Solaris:
        splatform = "sunos5";
        break;
      case FreeBSD:
        splatform = "freebsd";
        break;
      default:
        throw new IllegalArgumentException("Unknown Platform " + distribution.getPlatform());
    }
    return splatform;
  }

  private String getBitSize(Distribution distribution) {
    String sbitSize;
    switch (distribution.getBitsize()) {
      case B32:
        if (distribution.getVersion() instanceof IFeatureAwareVersion) {
          IFeatureAwareVersion featuredVersion = (IFeatureAwareVersion) distribution.getVersion();
          if (featuredVersion.enabled(Feature.ONLY_64BIT)) {
            throw new IllegalArgumentException("this version does not support 32Bit: "+distribution);
          }
        }

        switch (distribution.getPlatform()) {
          case Linux:
            sbitSize = "i686";
            break;
          case Windows:
          case OS_X:
            sbitSize = "i386";
            break;
          default:
            throw new IllegalArgumentException("Platform " + distribution.getPlatform() + " not supported yet on 32Bit Platform");
        }
        break;
      case B64:
        sbitSize = "x86_64";
        break;
      default:
        throw new IllegalArgumentException("Unknown BitSize " + distribution.getBitsize());
    }
    return sbitSize;
  }

}
