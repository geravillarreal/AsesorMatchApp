package com.uanl.asesormatch.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.service.MatchingService;
import com.uanl.asesormatch.service.UserService;
import com.uanl.asesormatch.enums.MatchStatus;
import com.uanl.asesormatch.config.AdvisorEmailProvider;

@RestController
@RequestMapping("/match")
public class MatchController {

        private final MatchingService matchingService;
        private final UserService userService;
        private final AdvisorEmailProvider emailProvider;

        public MatchController(MatchingService matchingService, UserService userService,
                               AdvisorEmailProvider emailProvider) {
                this.matchingService = matchingService;
                this.userService = userService;
                this.emailProvider = emailProvider;
        }

        @PostMapping("/request")
        public ResponseEntity<Void> requestMatch(@AuthenticationPrincipal OidcUser principal,
                        @RequestParam Long advisorId, @RequestParam Double score) {

                User student = userService.findByEmail(emailProvider.resolveEmail(principal))
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

		matchingService.requestMatch(student.getId(), advisorId, score);

                URI location = URI.create("/dashboard?matchRequested"); // optional query flag
                return ResponseEntity.status(HttpStatus.SEE_OTHER).location(location).build();
        }

        @PostMapping("/decision")
        public ResponseEntity<Void> decideMatch(@RequestParam Long matchId, @RequestParam String action) {
                MatchStatus status;
                try {
                        status = MatchStatus.valueOf(action.toUpperCase());
                } catch (IllegalArgumentException e) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid action");
                }

                matchingService.updateMatchStatus(matchId, status);

                URI location = URI.create("/advisor-dashboard");
                return ResponseEntity.status(HttpStatus.SEE_OTHER).location(location).build();
        }
}
