package com.company.finflow.security.auth;

import com.company.finflow.user.domain.User;
import com.company.finflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FinflowUserDetailsService implements UserDetailsService {

    private static final String USERNAME_DELIMITER = "|";

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        int delimiterPosition = username.indexOf(USERNAME_DELIMITER);
        if (delimiterPosition <= 0 || delimiterPosition >= username.length() - 1) {
            throw new UsernameNotFoundException("Invalid username format");
        }

        String tenantPart = username.substring(0, delimiterPosition);
        String emailPart = username.substring(delimiterPosition + 1);
        Long tenantId;
        try {
            tenantId = Long.parseLong(tenantPart);
        } catch (NumberFormatException exception) {
            throw new UsernameNotFoundException("Invalid tenant context");
        }
        return loadByTenantAndEmail(tenantId, emailPart);
    }

    public FinflowUserPrincipal loadByTenantAndEmail(Long tenantId, String email) {
        User user = userRepository.findByEmailIgnoreCaseAndTenant_Id(email, tenantId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new FinflowUserPrincipal(user);
    }
}
