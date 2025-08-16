package com.uanl.asesormatch.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.uanl.asesormatch.entity.Profile;
import com.uanl.asesormatch.repository.ProfileRepository;

@Service
public class ProfileService {

        private final ProfileRepository profileRepository;

        public ProfileService(ProfileRepository profileRepository) {
                this.profileRepository = profileRepository;
        }

        public Profile addInterests(Long profileId, List<String> interests) {
                var profileOpt = profileRepository.findById(profileId);
                if (profileOpt.isEmpty()) {
                        return null;
                }
                Profile profile = profileOpt.get();
                Set<String> normalized = interests.stream()
                                .map(s -> s == null ? "" : s.trim().toLowerCase())
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.toCollection(LinkedHashSet::new));
                profile.setInterests(new ArrayList<>(normalized));
                return profileRepository.save(profile);
        }

        public Profile addAreas(Long profileId, List<String> areas) {
                var profileOpt = profileRepository.findById(profileId);
                if (profileOpt.isEmpty()) {
                        return null;
                }
                Profile profile = profileOpt.get();
                Set<String> dedup = new LinkedHashSet<>();
                for (String area : areas) {
                        if (area != null) {
                                dedup.add(area.trim());
                        }
                }
                profile.setAreas(new ArrayList<>(dedup));
                return profileRepository.save(profile);
        }
}

