package com.example.FreelanceHub.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.FreelanceHub.Dto.ClientDTO;
import com.example.FreelanceHub.Dto.FreeDTO;
import com.example.FreelanceHub.models.Client;
import com.example.FreelanceHub.models.Freelancer;
import com.example.FreelanceHub.models.Notification;
import com.example.FreelanceHub.repositories.NotificationRepository;
import com.example.FreelanceHub.services.ClientService;
import com.example.FreelanceHub.services.FreelancerService;
import com.example.FreelanceHub.services.NotificationService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class UserController {

    @Autowired
    private ClientService clientService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private FreelancerService freeService;
    
    @Autowired
    private HttpSession session;
    
    @GetMapping("")
    public String showLandingpage(Model model) {
		 String role = (String) session.getAttribute("role");
		 model.addAttribute("role", role);
		 
        return "landing";
    }

    // Selection Page
    @GetMapping("/signup/selection")
    public String showSignupSelectionPage() {
        return "selection"; // Render the signup-selection.html page
    }

    // Client Signup Page
    @GetMapping("/signup/client")
    public String showClientSignupPage(Model model) {
        model.addAttribute("clientDTO", new ClientDTO());
        return "signupclient"; // Render the signup-client.html page
    }

    @PostMapping("/signup/client")
    public String registerClient(@Valid @ModelAttribute("clientDTO") ClientDTO clientDTO,
            BindingResult result, 
            Model model, 
            RedirectAttributes redirectAttributes) {
    	
    	if (result.hasErrors()) {
            model.addAttribute("notificationType", "error");
            model.addAttribute("notificationMessage", "Validation errors occurred. Please correct the errors and try again.");
            return "signupclient";
        }

        // Convert DTO to Entity
        Client client = new Client();
        client.setCompEmail(clientDTO.getCompEmail());
        client.setCompanyName(clientDTO.getCompanyName());
        client.setCompanyDescription(clientDTO.getCompanyDescription());
        client.setTypeOfProject(clientDTO.getTypeOfProject());
        client.setRepName(clientDTO.getRepName());
        client.setRepDesignation(clientDTO.getRepDesignation());
        client.setPassword(clientDTO.getPassword());
        
        boolean isRegistered = clientService.registerClient(client);
        if (isRegistered) {
        	redirectAttributes.addFlashAttribute("notificationType", "success");
            redirectAttributes.addFlashAttribute("notificationMessage", "Sign Up Successful!");
            return "redirect:/login"; // Redirect to the common login page
        } else {
        	model.addAttribute("notificationType", "error");
            model.addAttribute("notificationMessage", "Failed to register. Please try again");
            return "signupclient";
        }
    }

    // Freelancer Signup Page
    @GetMapping("/signup/freelancer")
    public String showFreelancerSignupPage(Model model) {
        model.addAttribute("freeDTO", new FreeDTO());
        return "signupfree"; // Render the signup-freelancer.html page
    }

    @PostMapping("/signup/freelancer")
    public String registerFreelancer(@Valid @ModelAttribute FreeDTO freeDTO, BindingResult result, 
            Model model, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			model.addAttribute("notificationType", "error");
			model.addAttribute("notificationMessage", "Validation errors occurred. Please correct and try again.");
			return "signupfree"; // Return to the signup page with errors
		}
		
		Freelancer freelancer = new Freelancer();
	    freelancer.setFreeEmail(freeDTO.getFreeEmail());
	    freelancer.setFreeName(freeDTO.getFreeName());
	    freelancer.setFreeAge(freeDTO.getFreeAge());
	    freelancer.setCountry(freeDTO.getCountry());
	    freelancer.setFOW(freeDTO.getFOW());
	    freelancer.setExperience(freeDTO.getExperience());
	    freelancer.setQualification(freeDTO.getQualification());
	    freelancer.setSkills(freeDTO.getSkills());
	    freelancer.setPassword(freeDTO.getPassword());

		boolean isRegistered = freeService.registerFreelancer(freelancer);
		if (isRegistered) {
			redirectAttributes.addFlashAttribute("notificationType", "success");
			redirectAttributes.addFlashAttribute("notificationMessage", "Sign Up Successful!");
			return "redirect:/login"; // Redirect to the common login page
		} else {
			model.addAttribute("notificationType", "error");
			model.addAttribute("notificationMessage", "Failed to register. Please try again.");
			return "signupfree";
		}
	}

    // Common Login Page
    @GetMapping("/login")
    public String showLoginPage() {
        return "common-login"; // Render the common login.html page
    }

    @PostMapping("/login")
    public String login(@RequestParam("email") String email,
                        @RequestParam("password") String password,
                        Model model,
                        RedirectAttributes redirectAttributes) {
    	
        // Check in Client table
        if (clientService.validateClient(email, password)) {
        	Client client=clientService.clientRepository.findBycompEmail(email);
        	String role=clientService.getUserRole(client.getClientId());
        	session.setAttribute("role", role);
        	session.setAttribute("userId", client.getClientId());
            
        	redirectAttributes.addFlashAttribute("notificationType", "success");
            redirectAttributes.addFlashAttribute("notificationMessage", "Logged in successfully: Client!");
            return "redirect:"; // Redirect to the dashboard
            
        }
        
        // Check in Freelancer table
        if (freeService.validateFreelancer(email, password)) {
        	Freelancer free=freeService.freeRepository.findByfreeEmail(email);
        	String role=freeService.getUserRole(free.getFreeId());
        	session.setAttribute("role", role);
        	session.setAttribute("userId", free.getFreeId());
            
        	redirectAttributes.addFlashAttribute("notificationType", "success");
            redirectAttributes.addFlashAttribute("notificationMessage", "Logged in successfully: Freelancer!");
            return "redirect:"; // Redirect to the dashboard
        }
        
        // Invalid login
        model.addAttribute("notificationType", "error");
        model.addAttribute("notificationMessage", "Invalid email or password.");
        return "common-login";
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session,RedirectAttributes redirectAttributes) {
        // Invalidate the session to remove all attributes (including the role)
        session.invalidate();
        redirectAttributes.addFlashAttribute("notificationType", "success");
        redirectAttributes.addFlashAttribute("notificationMessage", "Logged out successfully!");
        
        // Redirect to the landing or login page
        return "redirect:/";
    }
    
    @GetMapping("/getUnreadNotifications")
    @ResponseBody
    public Map<String,Object> getUnreadNotifications(@SessionAttribute("userId") String userId) {
    	// Keep only the most recent 10 notifications
    	notificationService.delNotification(userId);
        List<Notification >unreadNotifs= notificationService.getNotifications(userId);
        for (Notification notif : unreadNotifs) {
            notificationRepository.save(notif);
        }
        long unreadCount = unreadNotifs.stream()
            .filter(notif -> "false".equals(notif.isRead()))
            .count();

        // Construct the response
        Map<String, Object> response = new HashMap<>();
        response.put("notifications", unreadNotifs);
        response.put("unreadCount", unreadCount);
        return response;
    }
    
    @PostMapping("/markNotificationsAsRead")
    @ResponseStatus(HttpStatus.NO_CONTENT) // Returns no content if successful
    public void markNotificationsAsRead(@SessionAttribute("userId") String userId) {
        // Fetch unread notifications for the user
        List<Notification> unreadNotifications = notificationService.getNotifications(userId);
        // Mark each notification as read
        for (Notification notif : unreadNotifications) {
            notif.setRead("true");
            notificationRepository.save(notif);
        }
    }
    
    @GetMapping("/profile")
    public String getProfilePage(Model model) {
        String role = (String) session.getAttribute("role");

        if (role == null) {
            return "redirect:/login"; // Redirect to login if role is not found
        }

        if (role.equals("client")) {
            return "redirect:/profile/client"; // Redirect to the client profile page
            
        
        } else if (role.equals("freelancer")) {
            return "redirect:/profile/freelancer"; // Redirect to the freelancer profile page
        }
        

        return "redirect:/"; // Default to landing page if role is unknown
    }
  
    

    
}
