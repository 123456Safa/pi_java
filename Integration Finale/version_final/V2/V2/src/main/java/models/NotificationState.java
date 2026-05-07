package models;

import java.sql.Timestamp;

public class NotificationState {
    private int id;
    private int produitId;
    private boolean isRead;
    private boolean isDismissed;
    private Timestamp updatedAt;

    public NotificationState() {}

    public NotificationState(int produitId, boolean isRead, boolean isDismissed) {
        this.produitId = produitId;
        this.isRead = isRead;
        this.isDismissed = isDismissed;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProduitId() { return produitId; }
    public void setProduitId(int produitId) { this.produitId = produitId; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public boolean isDismissed() { return isDismissed; }
    public void setDismissed(boolean dismissed) { isDismissed = dismissed; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
