package com.example.FreelanceHub.services;

import com.example.FreelanceHub.models.Freelancer;
import com.example.FreelanceHub.models.Roles;
import com.example.FreelanceHub.repositories.FreeJobRepository;
import com.example.FreelanceHub.repositories.FreelancerRepository;
import com.example.FreelanceHub.repositories.RolesRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FreelancerService {

    @Autowired
    public FreelancerRepository freeRepository;

    @Autowired
    private RolesRepository rolesRepository;
    
    @Autowired
	FreeJobRepository freeJobRepository;

    public boolean registerFreelancer(Freelancer free) {
        try {
            Freelancer savedfree = freeRepository.save(free);
            String unique = "F"+savedfree.getId();
            savedfree.setFreeId(unique);
            freeRepository.save(savedfree);
            addRoleToFree(free.getFreeId(), "freelancer");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateFreelancer(String freeEmail, String password) {
        Freelancer free = freeRepository.findByfreeEmail(freeEmail);
        if(free != null && free.getPassword().equals(password)) {

            return true;
        }
        return false;
    }

    private void addRoleToFree(String freeId, String roleName) {
        try {
            Roles role = new Roles(roleName,freeId);
            rolesRepository.save(role);
        } catch (Exception e) {
            // Log error if needed
            System.err.println("Error adding role: " + e.getMessage());
        }
    }

    public String getUserRole(String freeId) {
        // Fetch the role based on the clientId
        Roles role = rolesRepository.findByRoleId(freeId);
        return role != null ? role.getRole() : null; // Return role or null if not found
    }
    
    public Freelancer findByFreeId(String freeId) {
        return freeRepository.findByFreeId(freeId)
                .orElseThrow(() -> new EntityNotFoundException("Freelancer not found for freeId: " + freeId));
    }
    
    
   

}