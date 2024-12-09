package com.example.FreelanceHub.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.FreelanceHub.Dto.ClientJobDTO;
import com.example.FreelanceHub.models.Client;
import com.example.FreelanceHub.models.ClientJob;
import com.example.FreelanceHub.models.Freelancer;
import com.example.FreelanceHub.models.FreelancerJob;
import com.example.FreelanceHub.models.Jobs;
import com.example.FreelanceHub.repositories.ClientJobRepository;
import com.example.FreelanceHub.repositories.ClientRepository;
import com.example.FreelanceHub.repositories.FreeJobRepository;
import com.example.FreelanceHub.repositories.JobRepository;
import com.example.FreelanceHub.services.ClientJobService;
import com.example.FreelanceHub.services.ClientService;
import com.example.FreelanceHub.services.FreelancerService;
import com.example.FreelanceHub.services.NotificationService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class ClientController {
	
	@Autowired
	private NotificationService notificationService;
	
	@Autowired
    private ClientJobRepository clientJobRepository;
	
	@Autowired
	private FreeJobRepository freelancerJobRepository;
	
	@Autowired
	private JobRepository jobRepository;
		
	@Autowired
	private ClientJobService clientJobService;
	
	@Autowired
    private ClientService clientService;
	
	@Autowired
	private ClientRepository clientRepository;

    @Autowired
    private HttpSession session;

    // Display the job creation form
    @GetMapping("/postjob")
    public String showJobForm(Model model) {
    	String clientId = (String) session.getAttribute("userId");
        model.addAttribute("clientId", clientId);
        model.addAttribute("clientJob", new ClientJob());
        return "postjob"; 
    }

    // Handle form submission
    @PostMapping("/postjob")
	public String createJob(@Valid @ModelAttribute("clientJob") ClientJobDTO clientJobDTO,
			BindingResult result, RedirectAttributes redirectAttributes, Model model) {
    	
    	if (result.hasErrors()) {
            model.addAttribute("notificationType", "error");
            model.addAttribute("notificationMessage", "Job posting failed due to validation errors");
            return "postjob"; 
        }
    	
    	String clientId = (String) session.getAttribute("userId");
    	ClientJob clientJob = new ClientJob();
        clientJob.setClientId(clientId);
        clientJob.setJobTitle(clientJobDTO.getJobTitle());
        clientJob.setJobDesc(clientJobDTO.getJobDesc());
        clientJob.setSkillReq(clientJobDTO.getSkillReq());
        clientJob.setDurMin(clientJobDTO.getDurMin());
        clientJob.setDurMax(clientJobDTO.getDurMax());
        clientJob.setCostMin(clientJobDTO.getCostMin());
        clientJob.setCostMax(clientJobDTO.getCostMax());
        clientJob.setExpMin(clientJobDTO.getExpMin());
        clientJob.setJobStat(clientJobDTO.getJobStat());
        
    	clientJobRepository.save(clientJob); 
    	
    	redirectAttributes.addFlashAttribute("notificationType", "success");
    	redirectAttributes.addFlashAttribute("notificationMessage", "Job Posted Successfully!");
        return "redirect:/posted-jobs"; 
    }
    
    @GetMapping("/posted-jobs")
    public String getPostedJobs(Model model) {
    	String clientId = (String) session.getAttribute("userId");
    	List<ClientJob> Alljobs = clientJobService.findByClientId(clientId);
    	List<ClientJob> jobs=new ArrayList<ClientJob>();
    	for(ClientJob job:Alljobs) {
    		System.out.println(job.getJobStat());
    		if(job.getJobStat().equals("pending")) {
    			jobs.add(job);
    		}
    	}
    	if (jobs == null || jobs.isEmpty()) {
            jobs = new ArrayList<>();
        }
        model.addAttribute("jobs", jobs);
        
        return "postedjobs"; 
    }
    
    @GetMapping("/bidding")
    public String showAllBids(
            @RequestParam(name = "sortBy", required = false, defaultValue = "duration") String sortBy, 
            Model model) {
        String clientId = (String) session.getAttribute("userId");
        // Fetch all jobs for the client
        List<ClientJob> jobs = clientJobService.findByClientId(clientId);
        if (jobs == null || jobs.isEmpty()) {
            model.addAttribute("jobsWithBids", new ArrayList<>()); 
            return "bidding";
        }

        List<Map<String, Object>> jobsWithBids = jobs.stream()
        		.filter(job -> "pending".equals(job.getJobStat()))
        		.map(job -> {
            List<FreelancerJob> freelancerBids = freelancerJobRepository.findByJobId(job);

           List<Map<String, Object>> enrichedBids = freelancerBids.stream().map(freelancerJob -> {
                Freelancer freelancer = freelancerJob.getFreeId();
                Map<String, Object> bidData = new HashMap<>();
                bidData.put("freelancerJob", freelancerJob);
                bidData.put("freelancerName", freelancer != null ? freelancer.getFreeName() : "Unknown");
                bidData.put("freelancerJobDuration", freelancerJob.getDuration());
                bidData.put("freelancerJobSalary", freelancerJob.getSalary());
                bidData.put("freelancerJobExp", freelancerJob.getJobExp());
                bidData.put("freelancerSkillMatch", freelancerJob.getSkillMatch());
                return bidData;
            }).collect(Collectors.toList());

            // Sort based on the chosen criterion
            switch (sortBy) {
                case "duration":
                	enrichedBids.sort((bid1, bid2) -> Integer.compare(
                			(int) bid1.get("freelancerJobDuration"),
                		    (int) bid2.get("freelancerJobDuration")));
                    break;
                case "salary":
                	enrichedBids.sort((bid1, bid2) -> Long.compare(
                			(long) bid1.get("freelancerJobSalary"),
                		    (long) bid2.get("freelancerJobSalary")));
                    break;
                case "experience":
                	enrichedBids.sort((bid1, bid2) -> Integer.compare(
                		    (int) bid2.get("freelancerJobExp"), 
                		    (int) bid1.get("freelancerJobExp")));

                    break;
                case "skillMatch":
                	enrichedBids.sort((bid1, bid2) -> Float.compare(
                		    (float) bid2.get("freelancerSkillMatch"),
                		    (float) bid1.get("freelancerSkillMatch")));
                    break;
                default:
                	enrichedBids.sort((bid1, bid2) -> Integer.compare(
                		    (int) bid1.get("freelancerJobDuration"),
                		    (int) bid2.get("freelancerJobDuration")));
                    break;
            }

            Map<String, Object> jobWithBids = new HashMap<>();
            jobWithBids.put("job", job);
            jobWithBids.put("bids", enrichedBids);

            return jobWithBids;
        }).collect(Collectors.toList());
        if(jobsWithBids==null || jobsWithBids.isEmpty()) {
        	jobsWithBids=new ArrayList<>();
        }
        model.addAttribute("jobsWithBids", jobsWithBids);
        model.addAttribute("sortBy", sortBy);  
        return "bidding";
    }

    @PostMapping("/acceptBid")
    public String acceptBid(@RequestParam("jobId") int jobId,
                            @RequestParam("userId") String userId,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        
        ClientJob clientJob = clientJobRepository.findById(jobId);
        FreelancerJob acceptedBid = freelancerJobRepository.findByJobIdAndFreeId(clientJob, userId);

        if (clientJob == null || acceptedBid == null) {
            model.addAttribute("error", "Invalid job or freelancer.");
            return "bidding"; 
        }

        Jobs newJob = new Jobs();
        newJob.setClientId(clientJob.getClients());
        newJob.setFreeId(acceptedBid.getFreeId());
        newJob.setJobId(clientJob);
        newJob.setProgress("ongoing");

        jobRepository.save(newJob);

        clientJob.setJobStat("assigned");
        clientJobRepository.save(clientJob); 

        acceptedBid.setStatus("accepted");
        acceptedBid.setJobDetails(newJob);
        freelancerJobRepository.save(acceptedBid);  

        List<FreelancerJob> allBids = freelancerJobRepository.findByJobId(clientJob);
        
        notificationService.addNotification(userId, "Your bid was accepted! Check the dashboard.");

        
        for (FreelancerJob bid : allBids) {
            if (!bid.getFreeId().getFreeId().equals(userId)) {
            	notificationService.addNotification(bid.getFreeId().getFreeId(), "Your bid was rejected! Check the dashboard.");
            	bid.setStatus("rejected");
                freelancerJobRepository.save(bid);
            }
        }

        redirectAttributes.addFlashAttribute("notificationType", "success");
        redirectAttributes.addFlashAttribute("notificationMessage", "Bid accepted successfully! Job has been successfully assigned to a freelancer");
        return "redirect:/bidding";
    }
    
    @GetMapping("/assignedjobs")
    public String getAssignedProjects(Model model, HttpSession session) {
        String clientId = (String) session.getAttribute("userId");
        
        List<FreelancerJob> ongoingJobs = freelancerJobRepository.findByClientIdAndProgress(clientId, "ongoing");
        for(FreelancerJob job: freelancerJobRepository.findByClientIdAndProgress(clientId, "unverified")){
        	ongoingJobs.add(job);
        }
        List<FreelancerJob> completedJobs = freelancerJobRepository.findByClientIdAndProgress(clientId, "completed");
        if ((ongoingJobs == null || ongoingJobs.isEmpty())&&(completedJobs==null || completedJobs.isEmpty())) {
            ongoingJobs = new ArrayList<>();
            completedJobs=new ArrayList<>();
        }
        model.addAttribute("ongoingJobs", ongoingJobs);
        model.addAttribute("completedJobs", completedJobs);

        return "assignedjobs";
    }
    
    @PostMapping("/verify-project")
    public String verifyProject(@RequestParam("jobId") Integer jobId,RedirectAttributes redirectAttributes) {
        Jobs job = jobRepository.findById(jobId).orElseThrow(() -> new RuntimeException("Job not found"));
        String freeId=job.getFreeId().getFreeId();        
        job.setProgress("completed");
        notificationService.addNotification(freeId, "One of your projects was verified. Job marked as complete!");
        jobRepository.save(job);
        redirectAttributes.addFlashAttribute("notificationType", "success");
        redirectAttributes.addFlashAttribute("notificationMessage", "Project verified! Job was marked as complete!");
        return "redirect:/assignedjobs"; // Redirect to client dashboard
    }
    
    @GetMapping("/profile/client")
    public String getClientProfile(Model model,HttpSession session) {
        String clientId = (String) session.getAttribute("userId");

        if (clientId == null) {
            return "redirect:/login"; // Redirect to login if no email is found in the session
        }

        Client client = clientService.findByClientId(clientId);
        if (client == null) {
            model.addAttribute("error", "Client not found.");
            return "error"; // Display an error page if client is not found
        }

		/* String clientId = (String) session.getAttribute("userId"); */
            
        List<FreelancerJob> ongoingJobs = freelancerJobRepository.findByClientIdAndProgress(clientId, "ongoing");
        for(FreelancerJob job: freelancerJobRepository.findByClientIdAndProgress(clientId, "unverified")){
        	ongoingJobs.add(job);
        }
        List<FreelancerJob> completedJobs = freelancerJobRepository.findByClientIdAndProgress(clientId, "completed");

        model.addAttribute("ongoingJobs", ongoingJobs);
        model.addAttribute("completedJobs", completedJobs);

        model.addAttribute("client", client);
        return "clientprofile"; // Render client profile page
    }
    

    @GetMapping("/client/edit/{clientId}")
    public String showEditForm(@PathVariable("clientId") String clientId, Model model) {
        Client client = clientService.findByClientId(clientId);
                
        model.addAttribute("client", client);
        return "editClientForm"; // Refers to the Thymeleaf template for editing
    }

    // Handle form submission for updating client details
    @PostMapping("/client/edit")
    public String updateClient(@ModelAttribute("client") Client updatedClient,RedirectAttributes redirectAttributes) {
    	String clientId = (String) session.getAttribute("userId");
        Client existingClient = clientService.findByClientId(clientId);
                
        
        // Update the client fields
        existingClient.setCompEmail(updatedClient.getCompEmail());
        existingClient.setCompanyName(updatedClient.getCompanyName());
        existingClient.setCompanyDescription(updatedClient.getCompanyDescription());
        existingClient.setTypeOfProject(updatedClient.getTypeOfProject());
        existingClient.setRepName(updatedClient.getRepName());
        existingClient.setRepDesignation(updatedClient.getRepDesignation());
		existingClient.setPassword(updatedClient.getPassword()); 
        
        // Save updated client to the database
        clientRepository.save(existingClient);
        redirectAttributes.addFlashAttribute("notificationType", "success");
        redirectAttributes.addFlashAttribute("notificationMessage", "Profile edited successfully!");
        return "redirect:/profile/client"; // Redirect to the client profile page
    }
    




}
