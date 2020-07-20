package com.github.krr.mongodb.embeddedmongo.config;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

@Slf4j
public class LinuxDistributionReader {
  public static String getLinuxVersion() {
    Map.Entry<String, String> distro = getLinuxDistro();
    String distribution = distro.getKey();
    String version = distro.getValue();
    if (distribution.isEmpty() || version.isEmpty()) {
      log.error("Error in parsing through os release files to fetch linux distribution and version");
      return "";
    }
    switch (distribution.toLowerCase()) {
      case "ubuntu":
        return getUbuntuVersion(version);
      case "centos":
      case "rhel":
        return getRedHatVersion(version);
      default:
        // This distribution id isn't supported by mongo
        log.error("The current linux belongs to {} and isn't supported", distribution);
        return "";
    }
  }

  private static String getRedHatVersion(String version) {
    int versionId = (int) Double.parseDouble(version);
    if (versionId <= 6) {
      return "rhel62";
    }
    else if (versionId == 7) {
      return "rhel70";
    }
    else if (versionId == 8) {
      return "rhel80";
    }
    else {
      // As of this commit, mongo only supports CentOS 6.2+.
      // Update the ubuntu version link for later versions here
      log.error("Red Hat {} isn't supported, please contact support", version);
      return "";
    }
  }

  private static String getUbuntuVersion(String version) {
    int versionId = (int) Double.parseDouble(version);
    if (versionId <= 16) {
      return "ubuntu1604";
    }
    else if (versionId == 18) {
      return "ubuntu1804";
    }
    else {
      // As of this commit, mongo only supports Ubuntu-16.04 and Ubuntu-18-04.
      // Update the ubuntu version link for later versions here
      log.error("Ubuntu {} isn't supported, please contact support", version);
      return "";
    }
  }

  private static Map.Entry<String, String> getLinuxDistro() {
    if("ubuntu1604".equals(System.getenv("OS_DIST"))){
      return new AbstractMap.SimpleEntry<>("ubuntu", "16.04");
    }
    File fileVersion = new File("/etc/os-release");
    if (fileVersion.exists()) {
      return getOsRelease(fileVersion);
    }
    fileVersion = new File("/etc/redhat-release");
    if (fileVersion.exists()) {
      return getRedHatRelease(fileVersion);
    }
    fileVersion = new File("/etc/centos-release");
    if (fileVersion.exists()) {
      return getRedHatRelease(fileVersion);
    }
    return new AbstractMap.SimpleEntry<>("", "");
  }

  private static AbstractMap.SimpleEntry<String, String> getRedHatRelease(File fileVersion) {
    String distribution = "", version = "";
    ArrayList<String> fileContents = readFile(fileVersion);
    for (String line : fileContents) {
      if (!line.isEmpty() && (line.toLowerCase().contains("centos") || line.toLowerCase().contains("redhat")
                              || line.toLowerCase().contains("red hat"))) {
        distribution = "rhel";
        Scanner scan = new Scanner(line);
        while (scan.hasNext()) {
          if(scan.hasNextDouble()) {
            version = String.valueOf(scan.nextDouble());
            // Return the first number read as the version number
            return new AbstractMap.SimpleEntry<>(distribution, version);
          }
          else {
            scan.next();
          }
        }
      }
    }
    return new AbstractMap.SimpleEntry<>(distribution, version);
  }

  private static Map.Entry<String, String> getOsRelease(File fileVersion) {
    String distribution = "", version = "";
    ArrayList<String> fileContents = readFile(fileVersion);
    for (String line : fileContents) {
      String[] properties = line.split("=");
      if ("ID".equals(properties[0])) {
        distribution = properties[1];
      }
      else if ("VERSION_ID".equals(properties[0])) {
        version = properties[1];
      }
    }
    return new AbstractMap.SimpleEntry<>(distribution, version);
  }

  private static ArrayList<String> readFile(File file) {
    ArrayList<String> fileContents = new ArrayList<>();
    try {
      BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
      while (bufferedReader.ready()) {
        fileContents.add(bufferedReader.readLine());
      }
    }
    catch (IOException e) {
      log.error("Error reading file {}", file.getName(), e);
    }
    return fileContents;
  }
}
