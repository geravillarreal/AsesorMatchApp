package com.uanl.asesormatch.service;

import com.uanl.asesormatch.dto.ProfileDTO;
import com.uanl.asesormatch.repository.ProfileRepository;

public class ProfileService {
	
	private final ProfileRepository profileRepository;
	
	public ProfileService(ProfileRepository profileRepository) {
		this.profileRepository = profileRepository;
	}
	
	public ProfileDTO save() {
		return null;
	}

}
