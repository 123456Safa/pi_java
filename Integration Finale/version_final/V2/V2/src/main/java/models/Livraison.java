package models;

public class Livraison {
    private int id;
    private String lastName;
    private String firstName;
    private String email;
    private String adresse;
    private String tel;
    private String createdAt;
    private int commandeId;

    public Livraison() {}

    public Livraison(int id, String lastName, String firstName, String email, String adresse, String tel, String createdAt, int commandeId) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.email = email;
        this.adresse = adresse;
        this.tel = tel;
        this.createdAt = createdAt;
        this.commandeId = commandeId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getTel() { return tel; }
    public void setTel(String tel) { this.tel = tel; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public int getCommandeId() { return commandeId; }
    public void setCommandeId(int commandeId) { this.commandeId = commandeId; }
}
