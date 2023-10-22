package session;

public class SessionConfiguration {
    double sessionExpiration;

    public SessionConfiguration() {
    }

    public double getSessionExpiration() {
        return sessionExpiration;
    }

    public void setSessionExpiration(double sessionExpiration) {
        this.sessionExpiration = sessionExpiration;
    }
}