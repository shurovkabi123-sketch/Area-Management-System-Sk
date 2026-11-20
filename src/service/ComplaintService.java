package service;

import config.DBConnection;
import model.SessionUser;
import java.sql.*;
import java.util.*;

public class ComplaintService {
    public List<String> categories() throws Exception { return names("SELECT category_name FROM categories ORDER BY category_name"); }
    public List<String> departments() throws Exception { return names("SELECT department_name FROM departments ORDER BY department_name"); }
    private List<String> names(String sql) throws Exception { List<String> a=new ArrayList<>(); try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){while(r.next())a.add(r.getString(1));}return a; }

    public static String saveImage(java.io.File source) throws Exception {
        if (source == null) return null;
        if (!source.isFile() || !source.canRead()) throw new Exception("The selected image cannot be read.");
        if (source.length() <= 0 || source.length() > 5L * 1024 * 1024)
            throw new Exception("Image must be between 1 byte and 5 MB.");
        String lower = source.getName().toLowerCase(Locale.ROOT);
        if (!(lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")))
            throw new Exception("Choose a JPG or PNG image.");
        // Verify the actual file content, not only its extension.
        java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(source);
        if (image == null) throw new Exception("The selected file is not a valid JPG or PNG image.");
        if (image.getWidth() > 10000 || image.getHeight() > 10000)
            throw new Exception("Image dimensions are too large (maximum 10000 x 10000 pixels).");
        java.nio.file.Path dir = config.AppConfig.getUploadDir();
        java.nio.file.Files.createDirectories(dir);
        String ext = lower.endsWith(".png") ? ".png" : ".jpg";
        String name = UUID.randomUUID() + ext;
        java.nio.file.Path destination = dir.resolve(name).toAbsolutePath().normalize();
        java.nio.file.Files.copy(source.toPath(), destination, java.nio.file.StandardCopyOption.COPY_ATTRIBUTES);
        return destination.toString();
    }

    /** Resolve a stored image path safely for previewing in any dashboard. */
    public static java.io.File resolveImage(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) return null;
        java.nio.file.Path path = java.nio.file.Paths.get(storedPath);
        if (!path.isAbsolute()) {
            java.nio.file.Path fromWorkingDirectory = path.toAbsolutePath().normalize();
            if (java.nio.file.Files.isRegularFile(fromWorkingDirectory)) return fromWorkingDirectory.toFile();
            // Compatibility with earlier builds that stored only the generated filename.
            path = config.AppConfig.getUploadDir().resolve(path.getFileName()).normalize();
        }
        return path.toFile();
    }

    /** Area-wide report rows; call only from the authenticated SUPER_ADMIN dashboard. */
    public Object[][] allComplaintRows() throws Exception {
        return query("SELECT c.complaint_id,c.tracking_id,w.ward_number,w.ward_name,u.full_name,c.title,cat.category_name,c.status,c.submitted_at,CASE WHEN c.image_path IS NULL OR c.image_path='' THEN 'No' ELSE 'Yes' END " +
                "FROM complaints c JOIN wards w ON w.ward_id=c.ward_id JOIN users u ON u.user_id=c.user_id JOIN categories cat ON cat.category_id=c.category_id ORDER BY c.submitted_at DESC");
    }

    /** Counts for area-wide Super Admin status cards: total, pending, in progress, resolved, rejected. */
    public int[] areaStatusCounts() throws Exception {
        int[] counts = new int[5];
        String sql = "SELECT COUNT(*),SUM(status='PENDING'),SUM(status='IN_PROGRESS'),SUM(status='RESOLVED'),SUM(status='REJECTED') FROM complaints";
        try (Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql); ResultSet r=p.executeQuery()) {
            if (r.next()) for (int i=0;i<5;i++) counts[i]=r.getInt(i+1);
        }
        return counts;
    }

    /** Returns image and description for an area-wide report, without allowing status edits. */
    public Object[] superAdminComplaintDetails(int complaintId) throws Exception {
        try (Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(
                "SELECT c.location,c.description,c.image_path,c.resolution_image_path,c.status,c.tracking_id,w.ward_name FROM complaints c JOIN wards w ON w.ward_id=c.ward_id WHERE c.complaint_id=?")) {
            p.setInt(1,complaintId);
            try (ResultSet r=p.executeQuery()) {
                return r.next()?new Object[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6),r.getString(7)}:null;
            }
        }
    }

    /** Resident-only evidence lookup. The user_id condition prevents residents viewing another resident's photos. */
    public Object[] residentComplaintDetails(int userId, String trackingId) throws Exception {
        try (Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(
                "SELECT c.title,c.category_id,cat.category_name,c.location,c.description,c.status,c.image_path,c.resolution_image_path,c.submitted_at " +
                "FROM complaints c JOIN categories cat ON cat.category_id=c.category_id WHERE c.tracking_id=? AND c.user_id=?")) {
            p.setString(1, trackingId);
            p.setInt(2, userId);
            try (ResultSet r=p.executeQuery()) {
                return r.next()?new Object[]{r.getString(1),r.getInt(2),r.getString(3),r.getString(4),r.getString(5),
                        r.getString(6),r.getString(7),r.getString(8),r.getTimestamp(9)}:null;
            }
        }
    }

    public void submit(SessionUser u,String category,String title,String description,String location,String photo) throws Exception {
        if (u.wardId <= 0) throw new Exception("Your account is not assigned to a ward. Contact the application administrator.");
        String tracking="ACM-"+java.time.Year.now().getValue()+"-"+UUID.randomUUID().toString().substring(0,8).toUpperCase();
        String sql="INSERT INTO complaints(tracking_id,user_id,ward_id,category_id,title,description,location,image_path) VALUES(?,?,?,(SELECT category_id FROM categories WHERE category_name=?),?,?,?,?)";
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,tracking);p.setInt(2,u.id);p.setInt(3,u.wardId);p.setString(4,category);p.setString(5,title);p.setString(6,description);p.setString(7,location);p.setString(8,photo);p.executeUpdate();
        }
        notifyResident(u.id,"Complaint received - "+tracking,"Your complaint has been received. Tracking ID: "+tracking+"\nTitle: "+title+"\nWard: "+u.wardName+"\nStatus: PENDING");
        notifyWardAdmins(u.wardId,"New complaint - "+tracking,"A new complaint was submitted in "+u.wardName+". Tracking ID: "+tracking+"\nTitle: "+title);
    }
    public void submit(SessionUser u,String category,String title,String description,String location)throws Exception{submit(u,category,title,description,location,null);}

    public Object[][] residentRows(int id)throws Exception{return query("SELECT c.tracking_id,c.title,cat.category_name,c.status,c.submitted_at FROM complaints c JOIN categories cat ON c.category_id=cat.category_id WHERE c.user_id=? ORDER BY c.submitted_at DESC",id);}

    public Object[][] adminRows(int wardId)throws Exception{
        return query("SELECT c.complaint_id,c.tracking_id,u.full_name,c.title,cat.category_name,COALESCE(d.department_name,'Unassigned'),c.status,c.submitted_at " +
                "FROM complaints c JOIN users u ON u.user_id=c.user_id JOIN categories cat ON cat.category_id=c.category_id " +
                "LEFT JOIN departments d ON d.department_id=c.department_id WHERE c.ward_id=? ORDER BY c.submitted_at DESC",wardId);
    }

    private Object[][] query(String sql,Object...args)throws Exception{List<Object[]> rows=new ArrayList<>();try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){for(int i=0;i<args.length;i++)p.setObject(i+1,args[i]);try(ResultSet r=p.executeQuery()){int n=r.getMetaData().getColumnCount();while(r.next()){Object[] x=new Object[n];for(int i=1;i<=n;i++)x[i-1]=r.getObject(i);rows.add(x);}}}return rows.toArray(new Object[0][]);}

    public Object[][] historyRows(int id,int wardId)throws Exception{
        return query("SELECT h.old_status,h.new_status,COALESCE(od.department_name,'Unassigned'),COALESCE(nd.department_name,'Unassigned'),u.full_name,h.remarks,h.updated_at " +
                "FROM complaint_history h JOIN complaints c ON c.complaint_id=h.complaint_id JOIN users u ON u.user_id=h.updated_by " +
                "LEFT JOIN departments od ON od.department_id=h.old_department_id LEFT JOIN departments nd ON nd.department_id=h.new_department_id " +
                "WHERE h.complaint_id=? AND c.ward_id=? ORDER BY h.updated_at DESC",id,wardId);
    }

    public Object[] complaintDetails(int id,int wardId)throws Exception{
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT location,description,image_path,resolution_image_path FROM complaints WHERE complaint_id=? AND ward_id=?")){
            p.setInt(1,id);p.setInt(2,wardId);try(ResultSet r=p.executeQuery()){return r.next()?new Object[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4)}:null;}
        }
    }

    public void updateComplaint(int adminId,int wardId,int id,String status,String department,String remarks,String resolutionPhoto)throws Exception{
        String email="",tracking="",title="";
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try{
                String oldStatus;int oldDept;
                try(PreparedStatement p=c.prepareStatement("SELECT status,department_id FROM complaints WHERE complaint_id=? AND ward_id=? FOR UPDATE")){
                    p.setInt(1,id);p.setInt(2,wardId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new Exception("Complaint not found in your ward or access denied.");oldStatus=r.getString(1);oldDept=r.getInt(2);}
                }
                Integer newDept=null;
                try(PreparedStatement p=c.prepareStatement("SELECT department_id FROM departments WHERE department_name=?")){p.setString(1,department);try(ResultSet r=p.executeQuery()){if(r.next())newDept=r.getInt(1);}}
                if ("RESOLVED".equals(status) && (remarks==null || remarks.isBlank())) throw new Exception("Add resolution remarks before marking a complaint resolved.");
                if ("RESOLVED".equals(status) && (resolutionPhoto==null || resolutionPhoto.isBlank())) {
                    try(PreparedStatement p=c.prepareStatement("SELECT resolution_image_path FROM complaints WHERE complaint_id=? AND ward_id=?")){p.setInt(1,id);p.setInt(2,wardId);try(ResultSet r=p.executeQuery()){if(r.next()&&(r.getString(1)==null||r.getString(1).isBlank()))throw new Exception("Please attach an after-resolution photo before marking this complaint resolved.");}}
                }
                try(PreparedStatement p=c.prepareStatement("UPDATE complaints SET status=?,department_id=?,resolution_image_path=COALESCE(?,resolution_image_path) WHERE complaint_id=? AND ward_id=?")){
                    p.setString(1,status);if(newDept==null)p.setNull(2,Types.INTEGER);else p.setInt(2,newDept);p.setString(3,resolutionPhoto);p.setInt(4,id);p.setInt(5,wardId);p.executeUpdate();
                }
                try(PreparedStatement p=c.prepareStatement("INSERT INTO complaint_history(complaint_id,updated_by,old_status,new_status,old_department_id,new_department_id,remarks) VALUES(?,?,?,?,?,?,?)")){
                    p.setInt(1,id);p.setInt(2,adminId);p.setString(3,oldStatus);p.setString(4,status);if(oldDept==0)p.setNull(5,Types.INTEGER);else p.setInt(5,oldDept);if(newDept==null)p.setNull(6,Types.INTEGER);else p.setInt(6,newDept);p.setString(7,remarks);p.executeUpdate();
                }
                try(PreparedStatement p=c.prepareStatement("SELECT u.email,c.tracking_id,c.title FROM complaints c JOIN users u ON u.user_id=c.user_id WHERE c.complaint_id=? AND c.ward_id=?")){
                    p.setInt(1,id);p.setInt(2,wardId);try(ResultSet r=p.executeQuery()){if(r.next()){email=r.getString(1);tracking=r.getString(2);title=r.getString(3);}}
                }
                c.commit();
            }catch(Exception e){c.rollback();throw e;}finally{c.setAutoCommit(true);}
        }
        if(!email.isBlank())try{EmailService.send(email,"Complaint update - "+tracking,"Your complaint '"+title+"' status is now "+status+".\nAdmin remarks: "+(remarks==null?"":remarks));}catch(Exception ignored){}
    }
    public void updateComplaint(int adminId,int wardId,int id,String status,String department,String remarks)throws Exception{updateComplaint(adminId,wardId,id,status,department,remarks,null);}

    public void reopenComplaint(int userId,String trackingId,String reason)throws Exception{
        int id,wardId;String old;
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT complaint_id,status,ward_id FROM complaints WHERE tracking_id=? AND user_id=?")){
            p.setString(1,trackingId);p.setInt(2,userId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new Exception("Complaint not found in your account.");id=r.getInt(1);old=r.getString(2);wardId=r.getInt(3);}
        }
        if(!"RESOLVED".equals(old))throw new Exception("Only resolved complaints can be reopened.");
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("UPDATE complaints SET status='PENDING' WHERE complaint_id=? AND user_id=?")){p.setInt(1,id);p.setInt(2,userId);p.executeUpdate();}
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("INSERT INTO complaint_history(complaint_id,updated_by,old_status,new_status,remarks) VALUES(?,?,'RESOLVED','PENDING',?)")){p.setInt(1,id);p.setInt(2,userId);p.setString(3,"Resident requested reopening: "+reason);p.executeUpdate();}
        notifyWardAdmins(wardId,"Complaint reopened - "+trackingId,"A resident requested reopening. Reason: "+reason+". Tracking ID: "+trackingId);
    }

    private void notifyResident(int id,String subject,String body){try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT email FROM users WHERE user_id=?")){p.setInt(1,id);try(ResultSet r=p.executeQuery()){if(r.next())EmailService.send(r.getString(1),subject,body);}}catch(Exception ignored){}}
    private void notifyWardAdmins(int wardId,String subject,String body){
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT email FROM users WHERE role='ADMIN' AND account_status='ACTIVE' AND ward_id=?")){
            p.setInt(1,wardId);try(ResultSet r=p.executeQuery()){while(r.next())try{EmailService.send(r.getString(1),subject,body);}catch(Exception ignored){}}
        }catch(Exception ignored){}
    }
}
