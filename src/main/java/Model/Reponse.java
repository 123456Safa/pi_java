package Model;

import java.util.Date;

public class Reponse {

    private int id;
    private String contenu;
    private Date dateReponse;
    private int reclamationId;

    public Reponse() {}

    public Reponse(int id, String contenu, Date dateReponse, int reclamationId) {
        this.id = id;
        this.contenu = contenu;
        this.dateReponse = dateReponse;
        this.reclamationId = reclamationId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public Date getDateReponse() { return dateReponse; }
    public void setDateReponse(Date dateReponse) { this.dateReponse = dateReponse; }

    public int getReclamationId() { return reclamationId; }
    public void setReclamationId(int reclamationId) { this.reclamationId = reclamationId; }
}