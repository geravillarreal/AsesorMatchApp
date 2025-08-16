package com.uanl.asesormatch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uanl.asesormatch.entity.Profile;
import com.uanl.asesormatch.repository.ProfileRepository;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

        @Mock
        ProfileRepository profileRepository;

        ProfileService profileService;

        @BeforeEach
        void setup() {
                profileService = new ProfileService(profileRepository);
        }

        @Test
        void addInterests_removesDuplicates_andNormalizes() {
                Profile p = new Profile();
                p.setId(10L);
                when(profileRepository.findById(10L)).thenReturn(java.util.Optional.of(p));
                when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

                List<String> input = List.of("IA", "ia", "Cloud", "Cloud ");
                Profile saved = profileService.addInterests(10L, input);

                Set<String> expected = Set.of("ia", "cloud");
                assertThat(saved.getInterests()).containsExactlyInAnyOrderElementsOf(expected);
                verify(profileRepository).save(any(Profile.class));
        }

        @Test
        void addAreas_removesDuplicates() {
                Profile p = new Profile();
                p.setId(11L);
                when(profileRepository.findById(11L)).thenReturn(java.util.Optional.of(p));
                when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

                List<String> input = List.of("Programación", "Programación", "Software");
                Profile saved = profileService.addAreas(11L, input);

                assertThat(saved.getAreas()).containsExactlyInAnyOrder("Programación", "Software");
        }
}

