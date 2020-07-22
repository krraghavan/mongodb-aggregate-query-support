package com.github.krr.mongodb.embeddedmongo.config;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.github.krr.mongodb.embeddedmongo.config.LinuxDistributionReader.*;

public class LinuxDistributionReaderTest {
  private LinuxDistributionReader linuxDistributionReader;
  private TestLinuxPackageIoUtil linuxPackageIoUtil;

  @BeforeMethod
  public void setUp() {
    linuxPackageIoUtil = new TestLinuxPackageIoUtil();
    linuxDistributionReader = new LinuxDistributionReader(linuxPackageIoUtil);
  }

  ///////TESTS///////

  @Test(dataProvider = "testOsReleaseFixture", invocationCount = 5)
  public void testOsRelease(String distribution, String version, String versionLink) {
    linuxPackageIoUtil.setOsReleaseId(distribution);
    linuxPackageIoUtil.setOsReleaseVersionId(version);
    linuxPackageIoUtil.setOsRelease(true);
    Assert.assertEquals(versionLink, linuxDistributionReader.getLinuxVersion());
  }

  @Test(dataProvider = "testRedHatReleaseFixture", invocationCount = 5)
  public void testRedHatRelease(String content, String versionLink) {
    linuxPackageIoUtil.setOsRedHatReleaseContent(content);
    linuxPackageIoUtil.setOsRedHatRelease(true);
    Assert.assertEquals(versionLink, linuxDistributionReader.getLinuxVersion());
  }

  @Test(dataProvider = "testCentOsReleaseFixture", invocationCount = 5)
  public void testCentOsRelease(String content, String versionLink) {
    linuxPackageIoUtil.setOsCentOsReleaseContent(content);
    linuxPackageIoUtil.setOsCentOsRelease(true);
    Assert.assertEquals(versionLink, linuxDistributionReader.getLinuxVersion());
  }

  @Test(expectedExceptions = RuntimeException.class)
  public void testUnkownDistribution() {
    // Fail to fetch linux distribution information
    linuxDistributionReader.getLinuxVersion();
  }

  @Test(expectedExceptions = RuntimeException.class, dataProvider = "testOsInvalidReleaseFixture", invocationCount = 5)
  public void testInvalidVersion(String distribution, String version) {
    // linux version is unsupported
    linuxPackageIoUtil.setOsRelease(true);
    linuxPackageIoUtil.setOsReleaseId(distribution);
    linuxPackageIoUtil.setOsReleaseVersionId(version);
    linuxDistributionReader.getLinuxVersion();
  }

  @Test(expectedExceptions = RuntimeException.class)
  public void testCorruptedOsRelease() {
    // os-release file doesn't have ID or VERSION_ID fields
    linuxPackageIoUtil.setOsRelease(true);
    linuxPackageIoUtil.setOsReleaseId(RandomStringUtils.randomAlphabetic(5));
    linuxPackageIoUtil.setOsReleaseVersionId(RandomStringUtils.randomNumeric(3));
    linuxDistributionReader.getLinuxVersion();
  }

  @Test(expectedExceptions = RuntimeException.class)
  public void testCorruptedRedHatRelease() {
    // redhat-release file doesn't have version info
    linuxPackageIoUtil.setOsRedHatRelease(true);
    linuxPackageIoUtil.setOsRedHatReleaseContent(RandomStringUtils.randomAlphanumeric(20));
    linuxDistributionReader.getLinuxVersion();
  }

  @Test(expectedExceptions = RuntimeException.class)
  public void testCorruptedCentOsRelease() {
    // centos-release file doesn't have version info
    linuxPackageIoUtil.setOsCentOsRelease(true);
    linuxPackageIoUtil.setOsCentOsReleaseContent(RandomStringUtils.randomAlphanumeric(20));
    linuxDistributionReader.getLinuxVersion();
  }

  ///////DATA FIXTURES///////

  @DataProvider
  public Object[][] testRedHatReleaseFixture() {
    String version, distroContent = "Red Hat Enterprise Linux Server release ";
    StringBuilder versionLink = new StringBuilder(RHEL);
    switch (RandomUtils.nextInt(0, 3)) {
      case 0:
        version = "6.2";
        versionLink.append("62");
        break;
      case 1:
        version = "7.0";
        versionLink.append("70");
        break;
      default:
        version = "8.0";
        versionLink.append("80");
    }
    return new Object[][]{
        new Object[]{distroContent.concat(version), versionLink.toString()}
    };
  }

  @DataProvider
  public Object[][] testCentOsReleaseFixture() {
    String version, distroContent = "CentOS release ";
    StringBuilder versionLink = new StringBuilder(RHEL);
    switch (RandomUtils.nextInt(0, 3)) {
      case 0:
        version = "6.2";
        versionLink.append("62");
        break;
      case 1:
        version = "7.0";
        versionLink.append("70");
        break;
      default:
        version = "8.0";
        versionLink.append("80");
    }
    return new Object[][]{
        new Object[]{distroContent.concat(version), versionLink.toString()}
    };
  }

  @DataProvider
  public Object[][] testOsReleaseFixture() {
    String distribution = "", version;
    StringBuilder versionLink = new StringBuilder();
    switch (RandomUtils.nextInt(0, 3)) {
      case 0:
        distribution = UBUNTU;
        versionLink.append(UBUNTU);
        if (RandomUtils.nextBoolean()) {
          version = "\"16.04\"";
          versionLink.append("1604");
        }
        else {
          version = "\"18.04\"";
          versionLink.append("1804");
        }
        break;
      case 1:
        distribution = RHEL;
        versionLink.append(RHEL);
      default:
        if (distribution.isEmpty()) {
          distribution = CENTOS;
          versionLink.append(RHEL);
        }
        switch (RandomUtils.nextInt(0, 3)) {
          case 0:
            version = "\"6.2\"";
            versionLink.append("62");
            break;
          case 1:
            version = "\"7.0\"";
            versionLink.append("70");
            break;
          default:
            version = "\"8.0\"";
            versionLink.append("80");
        }
    }
    return new Object[][]{
        new Object[]{distribution, version, versionLink.toString()}
    };
  }

  @DataProvider
  public Object[][] testOsInvalidReleaseFixture() {
    String distribution = "", version;
    switch (RandomUtils.nextInt(0, 3)) {
      case 0:
        distribution = UBUNTU;
        version = "\"20.04\"";
        break;
      case 1:
        distribution = RHEL;
      default:
        if (distribution.isEmpty()) {
          distribution = CENTOS;
        }
        version = "\"9.0\"";
    }
    return new Object[][]{
        new Object[]{distribution, version}
    };
  }

}