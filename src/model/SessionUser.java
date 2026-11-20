package model;

public class SessionUser {
    public final int id;
    public final String name, email, role;
    public final int wardId;
    public final String wardName;

    public SessionUser(int id, String name, String email, String role, int wardId, String wardName) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.wardId = wardId;
        this.wardName = wardName;
    }

    /** Backward-compatible constructor for any existing code creating a session directly. */
    public SessionUser(int id, String name, String email, String role) {
        this(id, name, email, role, 0, "");
    }
}
