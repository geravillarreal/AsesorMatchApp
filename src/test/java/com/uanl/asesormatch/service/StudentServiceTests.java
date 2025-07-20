package com.uanl.asesormatch.service;

import com.uanl.asesormatch.dto.StudentProfileDTO;
import com.uanl.asesormatch.dto.ProjectDTO;
import com.uanl.asesormatch.entity.Project;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.enums.ProjectStatus;
import com.uanl.asesormatch.enums.Role;
import com.uanl.asesormatch.repository.ProjectRepository;
import com.uanl.asesormatch.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.uanl.asesormatch.config.TestEntityScanConfig;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TestEntityScanConfig.class)
class StudentServiceTests {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ProjectRepository projectRepository;

	private StudentService studentService;

	@BeforeEach
	void setUp() {
		studentService = new StudentService(userRepository, projectRepository);
	}

	@Test
	void getProfileReturnsStudentDetailsAndProjects() {
		User student = new User();
		student.setFullName("Student Test");
		student.setEmail("student@test.com");
		student.setFaculty("Engineering");
		student.setRole(Role.STUDENT);
		userRepository.save(student);

		Project p1 = new Project();
		p1.setTitle("P1");
		p1.setDescription("D1");
		p1.setStatus(ProjectStatus.DRAFT);
		p1.setStudent(student);
		projectRepository.save(p1);

		Project p2 = new Project();
		p2.setTitle("P2");
		p2.setDescription("D2");
		p2.setStatus(ProjectStatus.IN_PROGRESS);
		p2.setStudent(student);
		projectRepository.save(p2);

		Optional<StudentProfileDTO> opt = studentService.getProfile(student.getId());
		assertTrue(opt.isPresent());
		StudentProfileDTO dto = opt.get();
		assertEquals(student.getId(), dto.id());
		assertEquals("Student Test", dto.fullName());
		assertEquals(2, dto.projects().size());
		List<Long> ids = dto.projects().stream().map(ProjectDTO::getId).collect(Collectors.toList());
		assertTrue(ids.containsAll(List.of(p1.getId(), p2.getId())));
	}

	@Test
	void getProfileReturnsEmptyForNonStudent() {
		User advisor = new User();
		advisor.setFullName("Advisor");
		advisor.setEmail("adv@test.com");
		advisor.setRole(Role.ADVISOR);
		userRepository.save(advisor);

		assertTrue(studentService.getProfile(advisor.getId()).isEmpty());
	}
}
