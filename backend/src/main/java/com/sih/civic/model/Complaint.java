package com.sih.civic.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
public class Complaint {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, length=150) private String title;
    @Column(nullable=false, length=4000) private String description;
    @Column(nullable=false, length=80) private String category;
    @Column(nullable=false, length=500) private String location;
    private Double latitude;
    private Double longitude;
    @Column(length=255) private String imagePath;
    @Column(nullable=false, length=30) private String status = "PENDING";
    @ManyToOne(fetch=FetchType.EAGER, optional=false) private User createdBy;
    @Column(nullable=false) private LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable=false) private LocalDateTime updatedAt = LocalDateTime.now();

    public Complaint() {}
    @PreUpdate public void onUpdate(){ updatedAt=LocalDateTime.now(); }
    public Long getId(){ return id; }
    public String getTitle(){ return title; }
    public void setTitle(String v){ title=v; }
    public String getDescription(){ return description; }
    public void setDescription(String v){ description=v; }
    public String getCategory(){ return category; }
    public void setCategory(String v){ category=v; }
    public String getLocation(){ return location; }
    public void setLocation(String v){ location=v; }
    public Double getLatitude(){ return latitude; }
    public void setLatitude(Double v){ latitude=v; }
    public Double getLongitude(){ return longitude; }
    public void setLongitude(Double v){ longitude=v; }
    public String getImagePath(){ return imagePath; }
    public void setImagePath(String v){ imagePath=v; }
    public String getStatus(){ return status; }
    public void setStatus(String v){ status=v; }
    public User getCreatedBy(){ return createdBy; }
    public void setCreatedBy(User v){ createdBy=v; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
    public LocalDateTime getUpdatedAt(){ return updatedAt; }
}
