package com.uanl.asesormatch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uanl.asesormatch.entity.Match;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.enums.MatchStatus;
import com.uanl.asesormatch.repository.MatchRepository;
import com.uanl.asesormatch.repository.NotificationRepository;
import com.uanl.asesormatch.repository.ProjectRepository;
import com.uanl.asesormatch.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class MatchingServiceStatusTest {

        @Mock
        private MatchRepository matchRepo;
        @Mock
        private NotificationRepository notificationRepo;
        @Mock
        private UserRepository userRepo;
        @Mock
        private ProjectRepository projectRepo;

        private MatchingService matchingService;

        @BeforeEach
        void setup() {
                NotificationService notificationService = new NotificationService(notificationRepo);
                matchingService = new MatchingService(matchRepo, userRepo, projectRepo, notificationService);
        }

        @Test
        void updateMatchStatus_acceptsOne_rejectsOthersOfSameStudent() {
                Match chosen = new Match();
                chosen.setId(100L);
                User student = new User();
                student.setId(1L);
                chosen.setStudent(student);
                chosen.setStatus(MatchStatus.PENDING);

                Match other1 = new Match();
                other1.setId(101L);
                other1.setStudent(student);
                other1.setStatus(MatchStatus.PENDING);

                Match other2 = new Match();
                other2.setId(102L);
                other2.setStudent(student);
                other2.setStatus(MatchStatus.PENDING);

                when(matchRepo.findById(100L)).thenReturn(Optional.of(chosen));
                when(matchRepo.findByStudent(student)).thenReturn(List.of(chosen, other1, other2));

                matchingService.updateMatchStatus(100L, MatchStatus.ACCEPTED);

                assertThat(chosen.getStatus()).isEqualTo(MatchStatus.ACCEPTED);
                assertThat(other1.getStatus()).isEqualTo(MatchStatus.REJECTED);
                assertThat(other2.getStatus()).isEqualTo(MatchStatus.REJECTED);

                verify(matchRepo, times(1)).save(chosen);
                verify(matchRepo, times(1)).save(other1);
                verify(matchRepo, times(1)).save(other2);
                verify(notificationRepo, atLeastOnce()).save(any());
        }
}

