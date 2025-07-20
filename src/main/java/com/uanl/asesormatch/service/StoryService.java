package com.uanl.asesormatch.service;

import com.uanl.asesormatch.entity.Project;
import com.uanl.asesormatch.entity.Story;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.enums.StoryStatus;
import com.uanl.asesormatch.repository.StoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StoryService {
    private final StoryRepository storyRepository;

    public StoryService(StoryRepository storyRepository) {
        this.storyRepository = storyRepository;
    }

    public Story createStory(Project project, User author, String title, String description) {
        Story s = new Story();
        s.setProject(project);
        s.setAuthor(author);
        s.setTitle(title);
        s.setDescription(description);
        s.setStatus(StoryStatus.TO_DO);
        s.setCreatedAt(LocalDateTime.now());
        return storyRepository.save(s);
    }

    public List<Story> getStories(Project project) {
        return storyRepository.findByProjectOrderByCreatedAtAsc(project);
    }

    public Story getStory(Long id) {
        return storyRepository.findById(id).orElseThrow();
    }

    public boolean hasPendingStories(Project project) {
        return storyRepository.countByProjectAndStatusNot(project, StoryStatus.DONE) > 0;
    }

    public void advanceStatus(Long storyId, User user) {
        Story story = getStory(storyId);
        if (!user.getId().equals(story.getProject().getStudent().getId())) {
            return;
        }
        StoryStatus next = switch (story.getStatus()) {
            case TO_DO -> StoryStatus.IN_PROGRESS;
            case IN_PROGRESS -> StoryStatus.TESTING;
            case TESTING -> StoryStatus.DONE;
            default -> null;
        };
        if (next != null) {
            story.setStatus(next);
            storyRepository.save(story);
        }
    }
}
