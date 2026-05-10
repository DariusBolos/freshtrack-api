package com.freshtrack.api.recipe;

import com.freshtrack.api.user.User;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Entity
@Table(name = "recipes")
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private String duration;
    private String prepTime;
    private String cookTime;
    private Integer servings;
    private String difficulty;
    private String icon;

    @ElementCollection
    private List<String> ingredients = new ArrayList<>();

    @ElementCollection
    private List<String> steps = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Recipe() {}

    public Recipe(
            Long id,
            String title,
            String description,
            String duration,
            String prepTime,
            String cookTime,
            Integer servings,
            String difficulty,
            String icon,
            List<String> ingredients,
            List<String> steps,
            User user
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.prepTime = prepTime;
        this.cookTime = cookTime;
        this.servings = servings;
        this.difficulty = difficulty;
        this.icon = icon;
        this.ingredients = ingredients == null ? new ArrayList<>() : ingredients;
        this.steps = steps == null ? new ArrayList<>() : steps;
        this.user = user;
    }
}

