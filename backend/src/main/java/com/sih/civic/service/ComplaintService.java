package com.sih.civic.service;
import com.sih.civic.model.*;
import com.sih.civic.repo.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
public class ComplaintService {
    private final ComplaintRepository complaints;
    private final UserRepository users;
    private final EmailService email;
    private final Path uploadDir;
    public ComplaintService(ComplaintRepository complaints, UserRepository users, EmailService email,
                            @Value("${app.upload-dir}") String uploadDir) {
        this.complaints=complaints; this.users=users; this.email=email;
        this.uploadDir=Paths.get(uploadDir).toAbsolutePath().normalize();
    }
    public List<Complaint> list(String emailAddress, boolean admin) {
        User user=users.findByEmailIgnoreCase(emailAddress).orElseThrow();
        return admin ? complaints.findAllByOrderByCreatedAtDesc()
                     : complaints.findByCreatedByIdOrderByCreatedAtDesc(user.getId());
    }
    public Complaint get(Long id, String emailAddress, boolean admin) {
        Complaint c=complaints.findById(id).orElseThrow(() -> new NoSuchElementException("Complaint not found"));
        if(!admin && !c.getCreatedBy().getEmail().equalsIgnoreCase(emailAddress))
            throw new SecurityException("You cannot access this complaint");
        return c;
    }
    public Complaint create(String emailAddress, String title, String description, String category,
                            String location, Double latitude, Double longitude, MultipartFile image) throws IOException {
        if(title==null || title.isBlank() || description==null || description.isBlank() ||
           category==null || category.isBlank() || location==null || location.isBlank())
            throw new IllegalArgumentException("Title, description, category and location are required");
        User user=users.findByEmailIgnoreCase(emailAddress).orElseThrow();
        Complaint c=new Complaint();
        c.setTitle(title.trim()); c.setDescription(description.trim()); c.setCategory(category.trim());
        c.setLocation(location.trim()); c.setLatitude(latitude); c.setLongitude(longitude); c.setCreatedBy(user);
        if(image!=null && !image.isEmpty()) {
            if(image.getSize()>5*1024*1024) throw new IllegalArgumentException("Image must be 5 MB or smaller");
            String type=image.getContentType()==null ? "" : image.getContentType();
            if(!List.of("image/jpeg","image/png","image/webp").contains(type))
                throw new IllegalArgumentException("Only JPEG, PNG or WEBP images are allowed");
            Files.createDirectories(uploadDir);
            String ext=switch(type){case "image/jpeg"->".jpg";case "image/png"->".png";default->".webp";};
            String filename=UUID.randomUUID()+ext;
            Path target=uploadDir.resolve(filename).normalize();
            if(!target.startsWith(uploadDir)) throw new IllegalArgumentException("Invalid filename");
            image.transferTo(target);
            c.setImagePath("/uploads/"+filename);
        }
        Complaint saved=complaints.save(c);
        email.notify(saved,"Complaint received: #"+saved.getId(),
            "Hello "+user.getName()+",\\n\\nYour complaint has been received.\\nComplaint ID: "+saved.getId()+
            "\\nTitle: "+saved.getTitle()+"\\nStatus: "+saved.getStatus()+"\\nLocation: "+saved.getLocation());
        return saved;
    }
    public Complaint updateStatus(Long id, String status) {
        String normalized=status==null ? "" : status.toUpperCase(Locale.ROOT);
        if(!Set.of("PENDING","IN_PROGRESS","RESOLVED","REJECTED").contains(normalized))
            throw new IllegalArgumentException("Invalid status");
        Complaint c=complaints.findById(id).orElseThrow(() -> new NoSuchElementException("Complaint not found"));
        c.setStatus(normalized);
        Complaint saved=complaints.save(c);
        email.notify(saved,"Complaint #"+id+" status updated",
            "Your complaint '"+saved.getTitle()+"' is now "+normalized+".");
        return saved;
    }
    public Map<String,Long> stats() {
        return Map.of("total",complaints.count(),"pending",complaints.countByStatus("PENDING"),
            "inProgress",complaints.countByStatus("IN_PROGRESS"),"resolved",complaints.countByStatus("RESOLVED"),
            "rejected",complaints.countByStatus("REJECTED"));
    }
}
