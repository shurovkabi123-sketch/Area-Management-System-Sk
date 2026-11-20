package service;

import config.DBConnection;
import model.SessionUser;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class AuthService {
    public Map<String,Integer> getActiveWards() throws Exception {
        Map<String,Integer> wards = new LinkedHashMap<>();
        try (Connection c=DBConnection.getConnection();
             PreparedStatement p=c.prepareStatement("SELECT ward_id, ward_name FROM wards WHERE active=1 ORDER BY ward_number, ward_name");
             ResultSet r=p.executeQuery()) {
            while (r.next()) wards.put(r.getString("ward_name"), r.getInt("ward_id"));
        }
        return wards;
    }

    public void register(String name,String email,String phone,String address,String password,int wardId) throws Exception {
        String sql="INSERT INTO users(full_name,email,phone,address,password_hash,role,ward_id) VALUES(?,?,?,?,?,'RESIDENT',?)";
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,name);p.setString(2,email);p.setString(3,phone);p.setString(4,address);
            p.setString(5,BCrypt.hashpw(password,BCrypt.gensalt(12)));p.setInt(6,wardId);p.executeUpdate();
        }
    }
    public void changePassword(int userId, String currentPassword, String newPassword) throws Exception {
        try(Connection c=DBConnection.getConnection(); PreparedStatement q=c.prepareStatement("SELECT password_hash FROM users WHERE user_id=?")) {
            q.setInt(1,userId); try(ResultSet r=q.executeQuery()) { if(!r.next() || !BCrypt.checkpw(currentPassword,r.getString(1))) throw new Exception("Current password is incorrect."); }
        }
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement("UPDATE users SET password_hash=? WHERE user_id=?")) {
            p.setString(1,BCrypt.hashpw(newPassword,BCrypt.gensalt(12))); p.setInt(2,userId); p.executeUpdate();
        }
    }

    public void sendPasswordResetCode(String email) throws Exception {
        String code=String.format("%06d", new java.security.SecureRandom().nextInt(1000000));
        int userId;
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement("SELECT user_id FROM users WHERE email=? AND account_status='ACTIVE'")) {
            p.setString(1,email); try(ResultSet r=p.executeQuery()) { if(!r.next()) throw new Exception("No active account found for this email."); userId=r.getInt(1); }
        }
        try(Connection c=DBConnection.getConnection(); PreparedStatement d=c.prepareStatement("DELETE FROM password_reset_tokens WHERE user_id=?")) { d.setInt(1,userId); d.executeUpdate(); }
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement("INSERT INTO password_reset_tokens(user_id,token_hash,expires_at) VALUES(?,?,DATE_ADD(NOW(),INTERVAL 10 MINUTE))")) {
            p.setInt(1,userId); p.setString(2,BCrypt.hashpw(code,BCrypt.gensalt(10))); p.executeUpdate();
        }
        EmailService.send(email,"Area Complaint System - Password reset code","Your password reset code is: "+code+"\nIt expires in 10 minutes. If you did not request this, ignore this email.");
    }

    public void resetPassword(String email, String code, String newPassword) throws Exception {
        int userId; String hash;
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement("SELECT u.user_id,t.token_hash FROM users u JOIN password_reset_tokens t ON t.user_id=u.user_id WHERE u.email=? AND t.expires_at>NOW() ORDER BY t.created_at DESC LIMIT 1")) {
            p.setString(1,email); try(ResultSet r=p.executeQuery()) { if(!r.next()) throw new Exception("Reset code expired or not found. Request a new code."); userId=r.getInt(1); hash=r.getString(2); }
        }
        if(!BCrypt.checkpw(code,hash)) throw new Exception("Incorrect reset code.");
        try(Connection c=DBConnection.getConnection()) { c.setAutoCommit(false); try(PreparedStatement p=c.prepareStatement("UPDATE users SET password_hash=? WHERE user_id=?"); PreparedStatement d=c.prepareStatement("DELETE FROM password_reset_tokens WHERE user_id=?")) {
            p.setString(1,BCrypt.hashpw(newPassword,BCrypt.gensalt(12))); p.setInt(2,userId); p.executeUpdate(); d.setInt(1,userId); d.executeUpdate(); c.commit();
        } catch(Exception e){c.rollback();throw e;} }
    }

    public SessionUser login(String email,String password) throws Exception {
        String sql="SELECT u.user_id,u.full_name,u.email,u.password_hash,u.role,u.ward_id,COALESCE(w.ward_name,'') AS ward_name " +
                   "FROM users u LEFT JOIN wards w ON w.ward_id=u.ward_id WHERE u.email=? AND u.account_status='ACTIVE'";
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,email);
            try(ResultSet r=p.executeQuery()){
                if(!r.next() || !BCrypt.checkpw(password,r.getString("password_hash")))
                    throw new Exception("Incorrect email or password, or the account is inactive.");
                int wardId=r.getInt("ward_id");
                if ("ADMIN".equalsIgnoreCase(r.getString("role")) && (r.wasNull() || wardId<=0))
                    throw new Exception("This administrator account is not assigned to a ward. Contact the system installer.");
                return new SessionUser(r.getInt("user_id"),r.getString("full_name"),r.getString("email"),r.getString("role"),wardId,r.getString("ward_name"));
            }
        }
    }

    public java.util.List<Object[]> listWards() throws Exception {
        java.util.List<Object[]> rows = new java.util.ArrayList<>();
        try (Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(
                "SELECT w.ward_id,w.ward_number,w.ward_name,w.active,COUNT(DISTINCT u.user_id) admins " +
                "FROM wards w LEFT JOIN users u ON u.ward_id=w.ward_id AND u.role='ADMIN' " +
                "GROUP BY w.ward_id,w.ward_number,w.ward_name,w.active ORDER BY w.ward_number"); ResultSet r=p.executeQuery()) {
            while(r.next()) rows.add(new Object[]{r.getInt(1),r.getString(2),r.getString(3),r.getBoolean(4),r.getInt(5)});
        }
        return rows;
    }

    public void createWard(String number,String name,String description) throws Exception {
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(
                "INSERT INTO wards(ward_number,ward_name,description,active) VALUES(?,?,?,1)")) {
            p.setString(1,number.trim());p.setString(2,name.trim());p.setString(3,description==null?null:description.trim());p.executeUpdate();
        }
    }

    public void createWardAdmin(String name,String email,String phone,String address,String password,int wardId) throws Exception {
        if(password==null || password.length()<8) throw new Exception("Password must contain at least 8 characters.");
        String sql="INSERT INTO users(full_name,email,phone,address,password_hash,role,ward_id,account_status) VALUES(?,?,?,?,?,'ADMIN',?,'ACTIVE')";
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,name.trim());p.setString(2,email.trim().toLowerCase());p.setString(3,phone.trim());p.setString(4,address.trim());
            p.setString(5,BCrypt.hashpw(password,BCrypt.gensalt(12)));p.setInt(6,wardId);p.executeUpdate();
        }
    }

    public int countSuperAdmins() throws Exception {
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM users WHERE role='SUPER_ADMIN'");ResultSet r=p.executeQuery()) { r.next(); return r.getInt(1); }
    }

    public void createInitialSuperAdmin(String name,String email,String phone,String password) throws Exception {
        if(password==null || password.length()<10) throw new Exception("Super Admin password must contain at least 10 characters.");
        try(Connection c=DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try(PreparedStatement check=c.prepareStatement("SELECT COUNT(*) FROM users WHERE role='SUPER_ADMIN'");ResultSet r=check.executeQuery()) {
                r.next(); if(r.getInt(1)>0) throw new Exception("A Super Admin already exists.");
            }
            try(PreparedStatement p=c.prepareStatement("INSERT INTO users(full_name,email,phone,address,password_hash,role,ward_id,account_status) VALUES(?,?,?,'System Administration',?,'SUPER_ADMIN',NULL,'ACTIVE')")) {
                p.setString(1,name.trim());p.setString(2,email.trim().toLowerCase());p.setString(3,phone.trim());p.setString(4,BCrypt.hashpw(password,BCrypt.gensalt(12)));p.executeUpdate();
            }
            c.commit();
        }
    }
}
