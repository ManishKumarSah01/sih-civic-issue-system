package com.sih.civic.web;
import com.sih.civic.model.Complaint;
import com.sih.civic.service.ComplaintService;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {
    private final ComplaintService service;
    public ComplaintController(ComplaintService service){ this.service=service; }
    private boolean admin(Authentication a){ return a.getAuthorities().stream().anyMatch(x -> x.getAuthority().equals("ROLE_ADMIN")); }

    @GetMapping public List<Complaint> list(Authentication auth){ return service.list(auth.getName(),admin(auth)); }
    @GetMapping("/{id}") public Complaint get(@PathVariable Long id, Authentication auth){ return service.get(id,auth.getName(),admin(auth)); }

    @PostMapping(consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(Authentication auth,
        @RequestParam String title, @RequestParam String description, @RequestParam String category,
        @RequestParam String location, @RequestParam(required=false) Double latitude,
        @RequestParam(required=false) Double longitude, @RequestParam(required=false) MultipartFile image) throws IOException {
        return ResponseEntity.status(201).body(service.create(auth.getName(),title,description,category,location,latitude,longitude,image));
    }
    public record StatusRequest(String status) {}
    @PatchMapping("/{id}/status")
    public Complaint status(@PathVariable Long id, @RequestBody StatusRequest req, Authentication auth) {
        if(!admin(auth)) throw new org.springframework.security.access.AccessDeniedException("Admin only");
        return service.updateStatus(id,req.status());
    }
    @GetMapping("/stats") public Map<String,Long> stats(Authentication auth) {
        if(!admin(auth)) throw new org.springframework.security.access.AccessDeniedException("Admin only");
        return service.stats();
    }
}
