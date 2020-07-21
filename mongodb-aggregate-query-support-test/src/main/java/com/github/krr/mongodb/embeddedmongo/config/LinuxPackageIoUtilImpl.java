package com.github.krr.mongodb.embeddedmongo.config;

import com.github.krr.mongodb.embeddedmongo.config.impl.LinuxPackageIoUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

@Slf4j
public class LinuxPackageIoUtilImpl implements LinuxPackageIoUtil {
  @Override
  public boolean isExists(File file) {
    return file.exists();
  }

  @Override
  public ArrayList<String> readFile(File file) {
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

  @Override
  public String getEnv(String property) {
    return System.getenv(property);
  }
}
