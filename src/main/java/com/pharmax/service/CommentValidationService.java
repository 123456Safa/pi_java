package com.pharmax.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * CommentValidationService — corresponds to PHP
 * App\Service\CommentValidationService
 * Pure business logic service for comment validation (input control).
 * No HTTP, no database — only validation rules.
 */
public class CommentValidationService {

    public static final int MIN_LENGTH = 2;
    public static final int MAX_LENGTH = 1000;
    public static final List<String> ALLOWED_STATUSES = Arrays.asList("valide", "bloque", "en_attente");

    /**
     * Validate comment content.
     *
     * @param contenu the comment text to validate
     * @return list of error messages (empty = valid)
     */
    public List<String> validateContent(String contenu) {
        List<String> errors = new ArrayList<>();
        String trimmed = contenu != null ? contenu.trim() : "";

        if (trimmed.isEmpty()) {
            errors.add("Le contenu du commentaire ne peut pas être vide.");
            return errors;
        }

        if (trimmed.length() < MIN_LENGTH) {
            errors.add(String.format(
                    "Le commentaire doit contenir au minimum %d caractères.", MIN_LENGTH));
        }

        if (trimmed.length() > MAX_LENGTH) {
            errors.add(String.format(
                    "Le commentaire ne doit pas dépasser %d caractères.", MAX_LENGTH));
        }

        return errors;
    }

    /**
     * Check whether a status value is valid.
     *
     * @param statut the status string to check
     * @return true if the status is allowed
     */
    public boolean isValidStatus(String statut) {
        return ALLOWED_STATUSES.contains(statut);
    }

    /**
     * Return the list of allowed statuses.
     *
     * @return list of valid status strings
     */
    public List<String> getAllowedStatuses() {
        return ALLOWED_STATUSES;
    }
}
