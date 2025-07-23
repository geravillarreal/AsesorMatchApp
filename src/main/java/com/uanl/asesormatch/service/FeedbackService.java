package com.uanl.asesormatch.service;

import com.uanl.asesormatch.entity.Feedback;
import com.uanl.asesormatch.entity.Project;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.enums.Role;
import com.uanl.asesormatch.repository.FeedbackRepository;
import com.uanl.asesormatch.repository.NotificationRepository;
import com.uanl.asesormatch.repository.ProjectRepository;
import com.uanl.asesormatch.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;

@Service
public class FeedbackService {
	private final ProjectRepository projectRepo;
	private final FeedbackRepository feedbackRepo;
	private final NotificationRepository notificationRepo;
	private final NotificationService notificationService;
	private static final Logger logger = LogManager.getLogger(FeedbackService.class);

	public FeedbackService(ProjectRepository projectRepo, FeedbackRepository feedbackRepo, UserRepository userRepo,
			NotificationRepository notificationRepo, NotificationService notificationService) {
		this.projectRepo = projectRepo;
		this.feedbackRepo = feedbackRepo;
		this.notificationRepo = notificationRepo;
		this.notificationService = notificationService;
	}

	public void submitFeedback(User user, Long projectId, Integer rating, String comment) {
		logger.info("User {} submitting feedback for project {}", user.getId(), projectId);
		Project project = projectRepo.findById(projectId).orElseThrow();

		if (!feedbackRepo.existsByProjectAndFromUser(project, user)) {
			User other = user.getId().equals(project.getStudent().getId()) ? project.getAdvisor()
					: project.getStudent();
			Feedback fb = new Feedback();
			fb.setProject(project);
			fb.setFromUser(user);
			fb.setToUser(other);
			fb.setCreatedAt(LocalDateTime.now());
			fb.setRating(rating);
			fb.setComment(comment);
			feedbackRepo.save(fb);
			logger.debug("Feedback saved for project {} from user {}", projectId, user.getId());
		}

		String projectIdToken = "feedbackProjectId=" + projectId;
		var notifications = notificationRepo.findByUserAndMessageContaining(user, projectIdToken);
		if (!notifications.isEmpty()) {
			notificationRepo.deleteAll(notifications);
		}

		User otherUser = user.getId().equals(project.getStudent().getId()) ? project.getAdvisor()
				: project.getStudent();
		if (otherUser != null && !feedbackRepo.existsByProjectAndFromUser(project, otherUser)) {
			String url = otherUser.getRole() == Role.ADVISOR ? "/advisor-dashboard?feedbackProjectId=" + projectId
					: "/dashboard?feedbackProjectId=" + projectId;
			String msg = "The other participant has already submitted their feedback for project '" + project.getTitle()
					+ "'. Please provide yours <a href='" + url + "'>here</a>";
			notificationService.notify(otherUser, msg);
			logger.info("Notification sent to user {} requesting feedback for project {}", otherUser.getId(),
					projectId);
		}
	}
}
