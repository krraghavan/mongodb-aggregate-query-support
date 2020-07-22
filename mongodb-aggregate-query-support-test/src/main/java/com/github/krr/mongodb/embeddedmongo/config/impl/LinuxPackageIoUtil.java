package com.github.krr.mongodb.embeddedmongo.config.impl;

import java.io.File;
import java.util.List;

public interface LinuxPackageIoUtil {
  boolean isExists(File file);
  List<String> readFile(File file);
}
