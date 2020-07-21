package com.github.krr.mongodb.embeddedmongo.config;

import com.github.krr.mongodb.embeddedmongo.config.impl.LinuxPackageIoUtil;
import lombok.Data;

import java.io.File;
import java.util.ArrayList;

import static com.github.krr.mongodb.embeddedmongo.config.LinuxDistributionReader.*;

@Data
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
    if("/etc/os-release".equals(file.getAbsolutePath())) {
      return isOsRelease;
    }
    else if ("/etc/redhat-release".equals(file.getAbsolutePath())){
      return isOsRedHatRelease;
    }
    else if ("/etc/centos-release".equals(file.getAbsolutePath())){
      return isOsCentOsRelease;
    }
    return false;
  }

  @Override
  public ArrayList<String> readFile(File file) {
    ArrayList<String> result = new ArrayList<>();
    if("/etc/os-release".equals(file.getAbsolutePath())) {
      result.add(ID.concat("=").concat(osReleaseId));
      result.add(VERSION_ID.concat("=").concat(osReleaseVersionId));
    }
    else if ("/etc/redhat-release".equals(file.getAbsolutePath())){
      result.add(osRedHatReleaseContent);
    }
    else if ("/etc/centos-release".equals(file.getAbsolutePath())){
      result.add(osCentOsReleaseContent);
    }
    return result;
  }

  @Override
  public String getEnv(String property) {
    return osDistEnv;
  }
}
