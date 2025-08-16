package com.uanl.asesormatch.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uanl.asesormatch.repository.MatchRepository;
import com.uanl.asesormatch.repository.ProjectRepository;
import com.uanl.asesormatch.repository.UserRepository;
import com.uanl.asesormatch.repository.NotificationRepository;

@ExtendWith(MockitoExtension.class)
class MatchingServiceCompatibilityTest {

        @Mock
        private MatchRepository matchRepository;
        @Mock
        private UserRepository userRepository;
        @Mock
        private ProjectRepository projectRepository;
        @Mock
        private NotificationRepository notificationRepository;

        private MatchingService matchingService;

        @BeforeEach
        void setup() {
                NotificationService notificationService = new NotificationService(notificationRepository);
                matchingService = new MatchingService(matchRepository, userRepository, projectRepository, notificationService);
        }

        @Test
        void computeCompatibility_totalPartialZero() {
                double full = matchingService.computeCompatibility(Set.of("IA", "Cloud"), Set.of("IA", "Cloud"));
                double half = matchingService.computeCompatibility(Set.of("IA", "Cloud"), Set.of("IA"));
                double zero = matchingService.computeCompatibility(Set.of("IA", "Cloud"), Set.of("SQL"));

                assertThat(full).isBetween(0.99, 1.01);
                assertThat(half).isBetween(0.49, 0.51);
                assertThat(zero).isEqualTo(0.0);
        }
}

