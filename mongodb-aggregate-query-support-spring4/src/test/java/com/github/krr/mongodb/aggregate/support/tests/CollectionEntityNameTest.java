package com.github.krr.mongodb.aggregate.support.tests;

import com.github.krr.mongodb.aggregate.support.beans.Doctor;
import com.github.krr.mongodb.aggregate.support.beans.Engineer;
import com.github.krr.mongodb.aggregate.support.config.AggregateTestConfiguration;
import com.github.krr.mongodb.aggregate.support.repository.DoctorRepository;
import com.github.krr.mongodb.aggregate.support.repository.EngineerRepository;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by purghant
 * 6/7/18.
 */

@SuppressWarnings({"ConstantConditions", "SpringJavaInjectionPointsAutowiringInspection"})
@ContextConfiguration(classes = AggregateTestConfiguration.class)
public class CollectionEntityNameTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private DoctorRepository doctorRepository;

  @Autowired
  private EngineerRepository engineerRepository;

  private Integer expectedEngineersBelowForty = 0;

  private Integer expectedDoctorsBelowForty = 0;

  private Integer expectedTotalEngineers = 0;

  private Integer expectedTotalDoctors = 0;

  @BeforeClass
  public void setup() {
    List<Engineer> engineers = new ArrayList<>();
    List<Doctor> doctors = new ArrayList<>();
    doctorRepository.deleteAll();
    engineerRepository.deleteAll();
    expectedTotalEngineers = RandomUtils.nextInt(40, 50);
    expectedTotalDoctors = RandomUtils.nextInt(40, 50);
    for (int i = 0; i < expectedTotalEngineers; i++) {
      Engineer engineer = new Engineer();
      engineers.add(engineer);
      if(engineer.getAge() < 40) {
        expectedEngineersBelowForty++;
      }
    }
    for (int i = 0; i < expectedTotalDoctors; i++) {
      Doctor doctor = new Doctor();
      doctors.add(doctor);
      if(doctor.getAge() < 40) {
        expectedDoctorsBelowForty++;
      }
    }
    engineerRepository.insert(engineers);
    doctorRepository.insert(doctors);
  }

  @Test
  public void mustGetAllDoctorsAndEngineers() {
    Integer totalDoctors = doctorRepository.findAll().size();
    Integer totalEngineers = engineerRepository.findAll().size();
    Assert.assertEquals(expectedTotalDoctors, totalDoctors);
    Assert.assertEquals(expectedTotalEngineers, totalEngineers);
  }

  @Test
  public void mustGetAllDoctorsAndEngineersBelowForty() {
    Integer totalDoctorsBelowForty = doctorRepository.getPersonsBelowForty().size();
    Integer totalEngineersBelowForty = engineerRepository.getPersonsBelowForty().size();
    Assert.assertEquals(expectedDoctorsBelowForty, totalDoctorsBelowForty);
    Assert.assertEquals(expectedEngineersBelowForty, totalEngineersBelowForty);
  }
}
