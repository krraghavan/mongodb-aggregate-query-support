package com.github.krr.mongodb.embeddedmongo.config.impl;

import java.io.File;
import java.util.ArrayList;

public interface LinuxPackageIoUtil {
  boolean isExists(File file);
  ArrayList<String> readFile(File file);
  String getEnv(String property);
}
