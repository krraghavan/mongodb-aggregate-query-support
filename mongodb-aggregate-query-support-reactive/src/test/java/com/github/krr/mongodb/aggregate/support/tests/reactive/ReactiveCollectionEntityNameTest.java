package com.github.krr.mongodb.aggregate.support.tests.reactive;

import com.github.krr.mongodb.aggregate.support.beans.Doctor;
import com.github.krr.mongodb.aggregate.support.beans.Engineer;
import com.github.krr.mongodb.aggregate.support.config.ReactiveAggregateTestConfiguration;
import com.github.krr.mongodb.aggregate.support.repository.reactive.ReactiveDoctorRepository;
import com.github.krr.mongodb.aggregate.support.repository.reactive.ReactiveEngineerRepository;
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
@ContextConfiguration(classes = ReactiveAggregateTestConfiguration.class)
public class ReactiveCollectionEntityNameTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private ReactiveDoctorRepository reactiveDoctorRepository;

  @Autowired
  private ReactiveEngineerRepository reactiveEngineerRepository;

  private Integer expectedEngineersBelowForty = 0;

  private Integer expectedDoctorsBelowForty = 0;

  private Integer expectedTotalEngineers = 0;

  private Integer expectedTotalDoctors = 0;

  @BeforeClass
  public void setup() {
    List<Engineer> engineers = new ArrayList<>();
    List<Doctor> doctors = new ArrayList<>();
    reactiveDoctorRepository.deleteAll();
    reactiveEngineerRepository.deleteAll();
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
    reactiveEngineerRepository.saveAll(engineers).collectList().block();
    reactiveDoctorRepository.saveAll(doctors).collectList().block();
  }

  @Test
  public void mustGetAllDoctorsAndEngineers() {
    Integer totalDoctors = reactiveDoctorRepository.findAll().collectList().block().size();
    Integer totalEngineers = reactiveEngineerRepository.findAll().collectList().block().size();
    Assert.assertEquals(expectedTotalDoctors, totalDoctors);
    Assert.assertEquals(expectedTotalEngineers, totalEngineers);
  }

  @Test
  public void mustGetAllDoctorsAndEngineersBelowForty() {
    Integer totalDoctorsBelowForty = reactiveDoctorRepository.getPersonsBelowForty().collectList().block().size();
    Integer totalEngineersBelowForty = reactiveEngineerRepository.getPersonsBelowForty().collectList().block().size();
    Assert.assertEquals(expectedDoctorsBelowForty, totalDoctorsBelowForty);
    Assert.assertEquals(expectedEngineersBelowForty, totalEngineersBelowForty);
  }
}
