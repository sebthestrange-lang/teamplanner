package de.teamplanner.service;

import de.teamplanner.repository.BenutzerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BenutzerDetailsService implements UserDetailsService {

    private final BenutzerRepository benutzerRepository;

    @Override
    public UserDetails loadUserByUsername(String benutzername) throws UsernameNotFoundException {
        return benutzerRepository.findByBenutzername(benutzername)
                .filter(b -> b.isAktiv())
                .map(b -> User.builder()
                        .username(b.getBenutzername())
                        .password(b.getPasswort())
                        .roles(b.getRolle().name())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Benutzer nicht gefunden: " + benutzername));
    }
}
