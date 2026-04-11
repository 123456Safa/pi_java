package Model;

import java.util.Date;

public class Reclamation {

    private int id;
    private String titre;
    private String description;
    private String statut;
    private Date dateCreation;
    private int userId;

    public Reclamation() {}

    public Reclamation(int id, String titre, String description, Date dateCreation, String statut, int userId) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.dateCreation = dateCreation;
        this.statut = statut;
        this.userId = userId;
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }

    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getStatut() { return statut; }

    public void setStatut(String statut) { this.statut = statut; }

    public Date getDateCreation() { return dateCreation; }

    public void setDateCreation(Date dateCreation) { this.dateCreation = dateCreation; }

    public int getUserId() { return userId; }

    public void setUserId(int userId) { this.userId = userId; }
}