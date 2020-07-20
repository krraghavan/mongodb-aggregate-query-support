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
  private static final String CENTOS = "centos";
  private static final String RHEL = "rhel";
  private static final String REDHAT = "redhat";
  private static final String RED_HAT = "red hat";
  private static final String UBUNTU = "ubuntu";
  public static final String VERSION_ID = "VERSION_ID";
  public static final String ID = "ID";
  public static final String UBUNTU_1604 = "ubuntu1604";
  public static final String UBUNTU_1804 = "ubuntu1804";
  public static final String RHEL_62 = "rhel62";
  public static final String RHEL_70 = "rhel70";
  public static final String RHEL_80 = "rhel80";

  public String getLinuxVersion() {
    Map.Entry<String, String> distro = getLinuxDistro();
    String distribution = distro.getKey();
    String version = distro.getValue();
    if (distribution.isEmpty() || version.isEmpty()) {
      log.error("Failed to fetch linux distribution information");
      throw new RuntimeException("Unable to extract linux distribution information." +
                                 " You might need to consider upgrading your linux distribution.");
    }
    switch (distribution.toLowerCase()) {
      case UBUNTU:
        return getUbuntuVersion(version);
      case CENTOS:
      case RHEL:
        return getRedHatVersion(version);
      default:
        // This distribution id isn't supported by mongo
        log.error("Current linux distribution belongs to {}, which isn't supported", distribution);
        throw new RuntimeException(
            String.format("Linux %s isn't supported, please contact support", distribution));
    }
  }

  private String getRedHatVersion(String version) {
    int versionId = (int) Double.parseDouble(version);
    if (versionId <= 6) {
      return RHEL_62;
    }
    else if (versionId == 7) {
      return RHEL_70;
    }
    else if (versionId == 8) {
      return RHEL_80;
    }
    else {
      // As of this commit, mongo only supports CentOS 6.2+.
      // Update the ubuntu version link for later versions here
      log.error("Red Hat {} isn't supported", version);
      throw new RuntimeException(String.format("Red Hat %s isn't supported, please contact support", version));
    }
  }

  private String getUbuntuVersion(String version) {
    int versionId = (int) Double.parseDouble(version);
    if (versionId <= 16) {
      return UBUNTU_1604;
    }
    else if (versionId == 18) {
      return UBUNTU_1804;
    }
    else {
      // As of this commit, mongo only supports Ubuntu-16.04 and Ubuntu-18-04.
      // Update the ubuntu version link for later versions here
      log.error("Ubuntu {} isn't supported", version);
      throw new RuntimeException(String.format("Ubuntu %s isn't supported, please contact support", version));
    }
  }

  private Map.Entry<String, String> getLinuxDistro() {
    if (UBUNTU_1604.equals(System.getenv("OS_DIST"))) {
      return new AbstractMap.SimpleEntry<>(UBUNTU, "16.04");
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
    log.error("Failed to fetch linux distribution information");
    throw new RuntimeException("Unable to extract linux distribution information." +
                               " You might need to consider upgrading your linux distribution.");
  }

  private AbstractMap.SimpleEntry<String, String> getRedHatRelease(File fileVersion) {
    String distribution = "", version = "";
    ArrayList<String> fileContents = readFile(fileVersion);
    for (String line : fileContents) {
      if (!line.isEmpty() && (line.toLowerCase().contains(CENTOS) || line.toLowerCase().contains(REDHAT)
                              || line.toLowerCase().contains(RED_HAT))) {
        distribution = RHEL;
        Scanner scan = new Scanner(line);
        while (scan.hasNext()) {
          if (scan.hasNextDouble()) {
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

  private Map.Entry<String, String> getOsRelease(File fileVersion) {
    String distribution = "", version = "";
    ArrayList<String> fileContents = readFile(fileVersion);
    for (String line : fileContents) {
      String[] properties = line.split("=");
      if (ID.equals(properties[0])) {
        distribution = properties[1];
      }
      else if (VERSION_ID.equals(properties[0])) {
        version = properties[1];
      }
    }
    return new AbstractMap.SimpleEntry<>(distribution, version);
  }

  private ArrayList<String> readFile(File file) {
    ArrayList<String> fileContents = new ArrayList<>();
    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
      while (bufferedReader.ready()) {
        fileContents.add(bufferedReader.readLine());
      }
    }
    catch (IOException e) {
      log.error("Error reading the file {}", file.getName(), e);
    }
    return fileContents;
  }
}
