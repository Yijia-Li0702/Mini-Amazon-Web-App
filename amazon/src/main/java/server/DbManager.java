package server;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DbManager {
    static final String url = "jdbc:postgresql://127.0.0.1:5432/project";
    static final String username = "postgres";
    static final String password = "passw0rd";
    //static final String password = "postgres";

    public Connection getConn() throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        Connection conn = DriverManager.getConnection(url, username, password);  
        conn.setAutoCommit(false);
        return conn;
    }
    public void printOrder(){
        String sql;
        Connection conn = null;
        sql = "SELECT * FROM amazon_web_order";

        try {
            conn = getConn();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            System.out.println("packege_id product_id amounts status destination_x destination_y ups");
            while(rs.next()){
                System.out.println(rs.getInt(1)+" "+rs.getInt(2)+" "+rs.getInt(3)+" "+rs.getString(4)+" "+rs.getInt(5)+" "+rs.getInt(6)+" "+rs.getString(7)+" ");
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } 
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        finally{
            try {
                conn.rollback();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public void printProduct(){
        String sql;
        Connection conn = null;
        sql = "SELECT * FROM amazon_web_product";

        try {
            conn = getConn();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            System.out.println("id, name, description, amounts, price");
            while(rs.next()){
                System.out.println(rs.getInt(1)+" "+rs.getString(2)+" "+rs.getString(3)+" "+rs.getInt(4)+" "+rs.getInt(5)+" ");
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } 
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        finally{
            try {
                conn.rollback();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public int getSeqNumAndAddOne(){
        Connection conn = null;
        int currSeqNum = 0;
        int updatedSeqNum = 0;
        try {     
            conn = getConn();
            String sql = "SELECT seq_num FROM seq_num for update";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){//already has this product
                currSeqNum = rs.getInt("seq_num");
            }

            sql = "UPDATE seq_num SET seq_num=? WHERE seq_num=?";
            stmt = conn.prepareStatement(sql);
            updatedSeqNum = currSeqNum + 1;
            stmt.setInt(1, updatedSeqNum);
            stmt.setInt(2, currSeqNum);
            stmt.executeUpdate();

            conn.commit();
        }
        catch (SQLException e) {
            e.printStackTrace();
        } 
        catch(ClassNotFoundException e){
            e.printStackTrace();
        }
        finally{
            try {
                conn.rollback();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return currSeqNum;
    }

    public void insertSeqNum(){
        Connection conn = null;
        try{
            conn = getConn();
            String sql = "INSERT INTO seq_num VALUES (?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);   
            pstmt.setInt(1, 20000);
            pstmt.executeUpdate();
            conn.commit();
        }
        catch (SQLException e) {
            e.printStackTrace();
        } 
        catch(ClassNotFoundException e){
            e.printStackTrace();
        }
        finally{
            try {
                conn.rollback();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void insertEntries(){
        Connection conn = null;
        try{
            conn = getConn();
            String sql = "INSERT INTO amazon_web_product(id, name, description, amounts, price) VALUES (?,?,?,?,?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, 1);
            pstmt.setString(2, "suda");
            pstmt.setString(3, "suda");
            pstmt.setInt(4, 0);
            pstmt.setInt(5, 5);
            pstmt.executeUpdate();
       /*
            sql = "INSERT INTO amazon_web_order VALUES (?,?,?,?,?,?,?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, 1);
            pstmt.setInt(2, 1);
            pstmt.setInt(3, 8);
            pstmt.setString(4, "cart");
            pstmt.setInt(5, 26);
            pstmt.setInt(6, 37);
            pstmt.setString(7, "aq23");
            pstmt.executeUpdate();
*/
            sql = "INSERT INTO seq_num VALUES (?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, 20000);
            pstmt.executeUpdate();
            conn.commit();
        }
        catch (SQLException e) {
            e.printStackTrace();
        } 
        catch(ClassNotFoundException e){
            e.printStackTrace();
        }
        finally{
            try {
                conn.rollback();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void dropAllTables(){
        Connection conn = null;

        try{
            conn = getConn();
            String sql = "";
            Statement stmt = conn.createStatement();

            //sql = "delete from account_symbol";
            sql = "drop table if exists amazon_web_order";
            stmt.execute(sql);

            //sql = "delete from account_balance";
            sql = "drop table if exists amazon_web_product";
            stmt.execute(sql);
            
            //sql = "delete from buy_order";
            sql = "drop table if exists seq_num";
            stmt.execute(sql);

            conn.commit();
        }
        catch (SQLException e) {
            e.printStackTrace();
        } 
        catch(ClassNotFoundException e){
            e.printStackTrace();
        }
        finally{
            try {
                conn.rollback();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    public void createSeqNumTable(){
        Connection conn = null;
        System.out.println("creating SEQ NUM table...");
        Statement stmt = null;
        try{
            conn = getConn();
            stmt = conn.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS seq_num(" +
                        "seq_num integer"+
                        ");";
            stmt.execute(sql);

            conn.commit();
        }
        catch (SQLException e) {
            e.printStackTrace();
        } 
        catch(ClassNotFoundException e){
            e.printStackTrace();
        }
        finally{
            try {
                conn.rollback();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    } 

    public void createAllTables(){
        Connection conn = null;
        System.out.println("creating all tables...");
        Statement stmt = null;
        try{
            conn = getConn();
            stmt = conn.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS amazon_web_product(" +
                        "id integer PRIMARY KEY,"+
                        "name varchar(10)," +
                        "description varchar(256),"+ 
                        "amounts integer NOT NULL," +
                        "price integer" +
                        //"wh integer," +
                        ");";
            stmt.execute(sql);
            sql = "CREATE TABLE IF NOT EXISTS amazon_web_order(" +
                        "packege_id integer PRIMARY KEY,"+
                        "product_id integer REFERENCES amazon_web_product (id)," +
                        "amounts integer NOT NULL," +
                        "status varchar(20)," +
                        "destination_x integer," +
                        "destination_y integer," +
                        "ups varchar(50)" +
                        ");";
            stmt.execute(sql);

            sql = "CREATE TABLE IF NOT EXISTS seq_num(" +
                        "seq_num integer"+
                        ");";
            stmt.execute(sql);

            conn.commit();
        }
        catch (SQLException e) {
            e.printStackTrace();
        } 
        catch(ClassNotFoundException e){
            e.printStackTrace();
        }
        finally{
            try {
                conn.rollback();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public int updateProductAmount(boolean add, int amount, long productId){
        Connection conn = null;
        int updatedAmount = 0;
        try {     
            conn = getConn();
            String sql = "SELECT amounts FROM amazon_web_product WHERE id=? for update";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, productId);
            ResultSet rs = stmt.executeQuery();

            int currAmount = 0;
            if(rs.next()){//already has this product
                currAmount = rs.getInt("amounts");
            }
            else{//does not have this product, so need to purchase
                return updatedAmount;
            }
            if(add){
                updatedAmount = currAmount + amount;
                sql = "UPDATE amazon_web_product SET amounts=? WHERE id=?";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, updatedAmount);
                stmt.setLong(2, productId);
                stmt.executeUpdate();
            }
            else{
                updatedAmount = currAmount - amount;
                sql = "UPDATE amazon_web_product SET amounts=? WHERE id=?";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, updatedAmount);
                stmt.setLong(2, productId);
                stmt.executeUpdate();
            }
            stmt.close();
            conn.commit();
        }
        catch (SQLException e) {
            e.printStackTrace();
        } 
        catch(ClassNotFoundException e){
            e.printStackTrace();
        }
        finally{
            try {
                conn.rollback();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return updatedAmount;
    }

    public Warehouse getWarehouse(int whId){
        Connection conn = null;
        Warehouse wh = new Warehouse();
        try {     
            conn = getConn();

            String sql = "SELECT * FROM amazon_web_warehouse WHERE whid=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, whId);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                wh.whId = rs.getInt("whid");
                wh.x = rs.getInt("wh_x");
                wh.y = rs.getInt("wh_y");
            }
            else{
                System.out.println("warehouse does not exist.");
            }
            stmt.close();
            conn.commit();
        }
        catch (SQLException e) {
            e.printStackTrace();
        } 
        catch(ClassNotFoundException e){
            e.printStackTrace();
        }
        finally{
            try {
                conn.rollback();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return wh;
    }

    public List<Product> getAllProducts(){
        Connection conn = null;
        Product product = new Product();
        List<Product> productsList = new ArrayList<>();
        try {     
            conn = getConn();
            //id, name, description, amounts, price
            String sql = "SELECT * FROM amazon_web_product";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                product = new Product();
                product.productId = rs.getLong("id");
                product.description = rs.getString("description");
                product.count = rs.getInt("amounts");
                productsList.add(product);
            }

            stmt.close();
            conn.commit();
        }
        catch (SQLException e) {
            e.printStackTrace();
        } 
        catch(ClassNotFoundException e){
            e.printStackTrace();
        }
        finally{
            try {
                conn.rollback();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return productsList;
    }
    public Product getProduct(long packegeId){
        Connection conn = null;
        Product product = new Product();

        try {     
            conn = getConn();
            String sql = "SELECT product_id, amounts, destination_x, destination_y, ups FROM amazon_web_order WHERE packege_id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, packegeId);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                product.productId = rs.getLong("product_id");
                product.count = rs.getInt("amounts");
                product.UPSAccount = rs.getString("ups");
                product.x = rs.getInt("destination_x");
                product.y = rs.getInt("destination_y");
            }

            sql = "SELECT description FROM amazon_web_product WHERE id=?";
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, product.productId);
            rs = stmt.executeQuery();
            if(rs.next()){
                product.description = rs.getString("description");
            }
            stmt.close();
            conn.commit();
        }
        catch (SQLException e) {
            e.printStackTrace();
        } 
        catch(ClassNotFoundException e){
            e.printStackTrace();
        }
        finally{
            try {
                conn.rollback();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return product;
    }
    
    public void updateOrderStatus(long packegeId, String status){     
        Connection conn = null;  
        try {     
            conn = getConn();
            String sql = "UPDATE amazon_web_order SET status=? WHERE amazon_web_order.packege_id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setLong(2, packegeId);
            stmt.executeUpdate();
            stmt.close();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } 
        catch(ClassNotFoundException e){
            e.printStackTrace();
        }
        finally{
            try {
                conn.rollback();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
