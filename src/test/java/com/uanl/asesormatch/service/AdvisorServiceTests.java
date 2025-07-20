package com.uanl.asesormatch.service;

import com.uanl.asesormatch.dto.AdvisorProfileDTO;
import com.uanl.asesormatch.entity.Profile;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.enums.Role;
import com.uanl.asesormatch.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.uanl.asesormatch.config.TestEntityScanConfig;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TestEntityScanConfig.class)
class AdvisorServiceTests {
	@Autowired
	private UserRepository userRepository;

	private AdvisorService advisorService;

	@BeforeEach
	void setUp() {
		advisorService = new AdvisorService(userRepository);
	}

	@Test
	void getProfileReturnsAdvisorInfo() {
		User advisor = new User();
		advisor.setFullName("Advisor Test");
		advisor.setEmail("advisor@test.com");
		advisor.setFaculty("Science");
		advisor.setRole(Role.ADVISOR);

		Profile profile = new Profile();
		profile.setLevel("PhD");
		profile.setUser(advisor);
		advisor.setProfile(profile);

		userRepository.save(advisor);

		Optional<AdvisorProfileDTO> opt = advisorService.getProfile(advisor.getId());
		assertTrue(opt.isPresent());
		AdvisorProfileDTO dto = opt.get();
		assertEquals("Advisor Test", dto.fullName());
		assertNotNull(dto.profile());
		assertEquals("PhD", dto.profile().getLevel());
	}

	@Test
	void getProfileReturnsEmptyForNonAdvisor() {
		User student = new User();
		student.setFullName("Student");
		student.setEmail("student@test.com");
		student.setRole(Role.STUDENT);
		userRepository.save(student);

		assertTrue(advisorService.getProfile(student.getId()).isEmpty());
	}
}
