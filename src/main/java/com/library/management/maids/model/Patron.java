package com.library.management.maids.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Entity
@Table(name = "patrons", uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "contactInfo"})})
public class Patron {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String contactInfo;
}