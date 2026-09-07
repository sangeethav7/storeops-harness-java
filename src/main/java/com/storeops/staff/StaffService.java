package com.storeops.staff;

import com.storeops.common.exception.AppException;
import com.storeops.common.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class StaffService {

    private final StaffRepository repository;

    public StaffService(StaffRepository repository) {
        this.repository = repository;
    }

    public AuthResponse authenticate(AuthRequest request) {
        Staff staff = repository.findByUsername(request.getUsername())
            .orElseThrow(() -> new AppException(
                "AUTH_FAILED", "Invalid credentials", HttpStatus.UNAUTHORIZED));
        if (!request.getPassword().equals(staff.getPassword())) {
            throw new AppException("AUTH_FAILED", "Invalid credentials", HttpStatus.UNAUTHORIZED);
        }
        return new AuthResponse("token-" + staff.getId(), staff.getId(), staff.getRole());
    }

    public Staff findById(String id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Staff", id));
    }

    public List<Staff> findAll() {
        return repository.findAll();
    }
}
