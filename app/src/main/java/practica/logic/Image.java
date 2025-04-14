package practica.logic;

import java.io.Serializable;
import jakarta.persistence.*;

@Entity
@Table(name = "Imagen")
public class Image implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String mimeType;
    @Lob
    private String fotoBase64;

    public Image() {
    }

    public Image(String name, String mimeType, String fotoBase64){
        this.name = name;
        this.mimeType = mimeType;
        this.fotoBase64 = fotoBase64;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFotoBase64() {
        return fotoBase64;
    }

    public void setFotoBase64(String fotoBase64) {
        this.fotoBase64 = fotoBase64;
    }
}
