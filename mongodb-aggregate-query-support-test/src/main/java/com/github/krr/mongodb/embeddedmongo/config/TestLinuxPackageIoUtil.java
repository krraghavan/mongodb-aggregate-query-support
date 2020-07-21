package com.github.krr.mongodb.embeddedmongo.config;

import com.github.krr.mongodb.embeddedmongo.config.impl.LinuxPackageIoUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.github.krr.mongodb.embeddedmongo.config.LinuxDistributionReader.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestLinuxPackageIoUtil implements LinuxPackageIoUtil {
  private boolean isOsRelease;
  private boolean isOsRedHatRelease;
  private boolean isOsCentOsRelease;
  private String osDistEnv;
  private String osReleaseId;
  private String osReleaseVersionId;
  private String osRedHatReleaseContent;
  private String osCentOsReleaseContent;

  @Override
  public boolean isExists(File file) {
    if(ETC_OS_RELEASE.equals(file.getAbsolutePath())) {
      return isOsRelease;
    }
    else if (ETC_REDHAT_RELEASE.equals(file.getAbsolutePath())){
      return isOsRedHatRelease;
    }
    else if (ETC_CENTOS_RELEASE.equals(file.getAbsolutePath())){
      return isOsCentOsRelease;
    }
    return false;
  }

  @Override
  public List<String> readFile(File file) {
    List<String> result = new ArrayList<>();
    if(ETC_OS_RELEASE.equals(file.getAbsolutePath())) {
      result.add(ID.concat("=").concat(osReleaseId));
      result.add(VERSION_ID.concat("=").concat(osReleaseVersionId));
    }
    else if (ETC_REDHAT_RELEASE.equals(file.getAbsolutePath())){
      result.add(osRedHatReleaseContent);
    }
    else if (ETC_CENTOS_RELEASE.equals(file.getAbsolutePath())){
      result.add(osCentOsReleaseContent);
    }
    return result;
  }

  @Override
  public String getEnv(String property) {
    return osDistEnv;
  }
}
