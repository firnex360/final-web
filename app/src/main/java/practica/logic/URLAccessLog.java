package practica.logic;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

// @Entity
// @Table(name = "url_access_log")
public class URLAccessLog implements Serializable{
    // @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @ManyToOne
    // @JoinColumn(name = "url_id")
    @JsonIgnore
    private ShortURL urlEntry;

    private String ip;
    private String browser;
    private String clientDomain;
    private String os;
    @JsonIgnore
    private LocalDateTime accessTime = LocalDateTime.now();

    public URLAccessLog() {

    }

    public URLAccessLog(Long id, ShortURL urlEntry, String ip, String browser, String clientDomain, String os, LocalDateTime accessTime) {
        this.id = id;
        this.urlEntry = urlEntry;
        this.ip = ip;
        this.browser = browser;
        this.clientDomain = clientDomain;
        this.os = os;
        this.accessTime = accessTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ShortURL getUrlEntry() {
        return urlEntry;
    }

    public void setUrlEntry(ShortURL urlEntry) {
        this.urlEntry = urlEntry;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getClientDomain() {
        return clientDomain;
    }

    public void setClientDomain(String clientDomain) {
        this.clientDomain = clientDomain;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public LocalDateTime getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(LocalDateTime accessTime) {
        this.accessTime = accessTime;
    }

    
    
    
}

