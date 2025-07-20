package com.uanl.asesormatch.service;

import com.uanl.asesormatch.entity.Notification;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.enums.Role;
import com.uanl.asesormatch.repository.NotificationRepository;
import com.uanl.asesormatch.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.uanl.asesormatch.config.TestEntityScanConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TestEntityScanConfig.class)
class NotificationServiceTests {
	@Autowired
	private NotificationRepository notificationRepository;
	@Autowired
	private UserRepository userRepository;

	private NotificationService notificationService;

	@BeforeEach
	void setUp() {
		notificationService = new NotificationService(notificationRepository);
	}

	@Test
	void notifyPersistsNotification() {
		User user = new User();
		user.setFullName("User Test");
		user.setEmail("user@test.com");
		user.setRole(Role.STUDENT);
		userRepository.save(user);

		notificationService.notify(user, "Hello");

		List<Notification> list = notificationRepository.findByUserOrderByCreatedAtDesc(user);
		assertEquals(1, list.size());
		Notification n = list.get(0);
		assertEquals("Hello", n.getMessage());
		assertFalse(n.isRead());
		assertNotNull(n.getCreatedAt());
		assertEquals(user.getId(), n.getUser().getId());
	}

	@Test
	void getNotificationsForReturnsDescending() {
		User user = new User();
		user.setFullName("User Test");
		user.setEmail("user@test.com");
		user.setRole(Role.STUDENT);
		userRepository.save(user);

		notificationService.notify(user, "First");
		notificationService.notify(user, "Second");

		List<Notification> list = notificationService.getNotificationsFor(user);
		assertEquals(2, list.size());
		assertEquals("Second", list.get(0).getMessage());
		assertEquals("First", list.get(1).getMessage());
	}
}
