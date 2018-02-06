package aebd;



import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.lang.String ; 
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date ; 
import java.util.logging.Level;
import java.util.logging.Logger;

public class Connector  {

    
public static void main(String[] args){
 
    Connection conn1 = null;
    Connection conn2 = null;
    Connection conn3 = null;
    
    
    try{
    Class.forName("oracle.jdbc.OracleDriver");
    
    // METHOD #2
            String dbURL1 = "jdbc:oracle:thin:@//localhost:1521/orcl12c" ;
            String username = "sys as SYSDBA";
            String password = "oracle";            
            conn1 = DriverManager.getConnection(dbURL1, username, password);
            
            String dbURL2 = "jdbc:oracle:thin:@//localhost:1521/orcl" ;
            String username2 = "Manager";
            String password2 = "pass";            
            conn2 = DriverManager.getConnection(dbURL2, username2, password2);
            
            
            String dbURL3 = "jdbc:oracle:thin:@//localhost:1521/orcl12c";
            String username3 = "sys as SYSDBA";
            String password3 = "oracle";            
            conn1 = DriverManager.getConnection(dbURL3, username3, password3);
            
            if (conn1 != null) {
                System.out.println("Connected with connection #1");
            }
            else{
                System.out.println(".....") ; 
            }
            
            if (conn2 != null) {
                System.out.println("Connected with connection #2");
            }
            else{
                System.out.println(".....") ; 
            }
            if (conn3 != null) {
                System.out.println("Connected with connection #3");
            }
            else{
                System.out.println(".....") ; 
            }
            
            while(true){
            // TABELSPACES
            PreparedStatement psmt;
            String updateQuery;	
            String tbs = "select fs.tablespace_name NAME , (df.totalspace - fs.freespace) USED_SIZE, " +
                        " fs.freespace FREE_SIZE, df.totalspace TOTAL_SIZE, c.CURRENT_TIMESTAMP TIMESTAMP "+
                   " from (select tablespace_name, round(sum(bytes) / 1048576) TotalSpace "+
                    " from dba_data_files group by tablespace_name) df, " +              
                    " (select tablespace_name, round(sum(bytes) / 1048576) FreeSpace "+
                    " from dba_free_space group by tablespace_name) fs, "+
                    " (SELECT CURRENT_TIMESTAMP FROM dual) c " + 
                    " where df.tablespace_name = fs.tablespace_name" ; 
            Statement stmt = conn1.createStatement();
            ResultSet resultSet = stmt.executeQuery(tbs);
            
            while(resultSet.next()) {

             Statement stmt1 = conn2.createStatement();
             String tbs1 = "UPDATE TABLESPACE " + 
            	" SET USED_SIZE = " + resultSet.getString("USED_SIZE") + 
            	"," + " FREE_SIZE = " + resultSet.getString("FREE_SIZE") +
            	"," + " TOTAL_SIZE = " + resultSet.getString("TOTAL_SIZE") +
            	"," + " TIMESTAMP = CURRENT_TIMESTAMP" + 
            	" WHERE NAME = " + "'" + resultSet.getString("NAME") + "'";

            int i=0;
            i = stmt1.executeUpdate(tbs1);

            if(i==0) {

            tbs = "INSERT INTO TABLESPACE (NAME,USED_SIZE,FREE_SIZE,TOTAL_SIZE,TIMESTAMP) "
                    + "VALUES(?, ?, ?, ?, CURRENT_TIMESTAMP)" ; 
            	psmt = conn2.prepareStatement(tbs);  
            
                psmt.setString(1,resultSet.getString("NAME")) ; 
                psmt.setFloat(2,Float.parseFloat(resultSet.getString("USED_SIZE"))) ; 
                psmt.setFloat(3,Float.parseFloat(resultSet.getString("FREE_SIZE"))) ; 
                psmt.setFloat(4,Float.parseFloat(resultSet.getString("TOTAL_SIZE"))) ; 
                psmt.executeUpdate() ; 
                psmt.close() ; 
            	}
            }
            
            
            // DATAFILES
            
            String data = "SELECT Substr(df.file_name,1,500) NAME_DF, " +
                            "Substr(df.tablespace_name,1,40) NAME_TB, " +
                            "Round(df.bytes/1024/1024,0) FILE_SIZE, " +
                            "decode(e.used_bytes,NULL,0,Round(e.used_bytes/1024/1024,0)) USED_SIZE, " +
                            "decode(f.free_bytes,NULL,0,Round(f.free_bytes/1024/1024,0)) FREE_SIZE, " +
                            "c.CURRENT_TIMESTAMP TIMESTAMP, " +
                            "d.CURRENT_TIMESTAMP TIMESTAMP_FK " +
                            " FROM DBA_DATA_FILES DF, " +
                            " (SELECT file_id, " +
                            " Sum(Decode(bytes,NULL,0,bytes)) used_bytes " +
                            " FROM dba_extents " +
                            " GROUP by file_id) E, " +
                            " (SELECT Max(bytes) free_bytes, file_id " +
                            " FROM dba_free_space " +
                            " GROUP BY file_id) f, " +
                            " (SELECT CURRENT_TIMESTAMP FROM dual) c, " +
                            " (SELECT CURRENT_TIMESTAMP FROM dual) d " +
                            " WHERE e.file_id (+) = df.file_id " +
                            " AND df.file_id = f.file_id (+) " +
                            " ORDER BY df.tablespace_name,df.file_name" ; 
            
            resultSet = stmt.executeQuery(data); 
            while(resultSet.next()) {
            	
            Statement stmt1 = conn2.createStatement();
            
            String tbs1 = "UPDATE DATAFILE " + 
                    	" SET USED_SIZE = " + resultSet.getString("USED_SIZE") +
                    	"," + " FILE_SIZE = " + resultSet.getString("FILE_SIZE") +
                    	"," + " FREE_SIZE = " + resultSet.getString("FREE_SIZE") +
                    	"," + " TIMESTAMP = CURRENT_TIMESTAMP" + 
                    	" WHERE NAME_DF = " + "'" + resultSet.getString("NAME_DF") + "'";	
        
            int i;
            i = stmt1.executeUpdate(tbs1);	
            	
            if(i==0) {
            	
            data = "INSERT INTO DATAFILE (NAME_DF,NAME_TB,FILE_SIZE,USED_SIZE,FREE_SIZE,TIMESTAMP) "
                    + "VALUES(?, ?, ?, ?, ?, CURRENT_TIMESTAMP)" ; 
            psmt = conn2.prepareStatement(data) ; 
            
            
                psmt.setString(1,resultSet.getString("NAME_DF")) ;
                psmt.setString(2,resultSet.getString("NAME_TB")) ;
                psmt.setFloat(3,Float.parseFloat(resultSet.getString("FILE_SIZE"))) ; 
               if(resultSet.getString("USED_SIZE") != null )
                psmt.setFloat(4,Float.parseFloat(resultSet.getString("USED_SIZE"))) ; 
               else 
                psmt.setNull(4,java.sql.Types.NUMERIC);
               if(resultSet.getString("FREE_SIZE") != null )
                psmt.setFloat(5,Float.parseFloat(resultSet.getString("FREE_SIZE"))) ; 
               else psmt.setNull(5,java.sql.Types.NUMERIC);
                psmt.executeUpdate(); 
                psmt.close() ; 
            }
            
            }
            
            
            // USERS
            
            String users = "select USER_ID,USERNAME,ACCOUNT_STATUS,"+
                        " DEFAULT_TABLESPACE DEFAULT_TB,TEMPORARY_TABLESPACE TEMP_TB,CREATED,c.CURRENT_TIMESTAMP " +
                         " from dba_users, " + 
                        " (SELECT CURRENT_TIMESTAMP FROM dual) c " +
                        " where ACCOUNT_STATUS = 'OPEN' "+
                        " order by 1" ; 
            resultSet = stmt.executeQuery(users); 
            
            while(resultSet.next()) {
            
            Statement stmt3 = conn2.createStatement();
            String tbs3 = " UPDATE USERS" + 
                	" SET ACCOUNT_STATUS = "+"'"+resultSet.getString("ACCOUNT_STATUS")+"'"+
                	"," + " TIMESTAMP = CURRENT_TIMESTAMP "  +
                	" WHERE USER_ID = "  + resultSet.getString("USER_ID") ;
            int i=0;
            i = stmt3.executeUpdate(tbs3);		
            if(i==0) {
            users = "INSERT INTO USERS (USER_ID,USERNAME,ACCOUNT_STATUS,TEMP_TB,CREATED,TIMESTAMP,NAME_TB) "
                    + "VALUES(?, ?, ?, ?, ?, CURRENT_TIMESTAMP,?)" ; 
            psmt = conn2.prepareStatement(users) ; 
           
                psmt.setFloat(1,Float.parseFloat(resultSet.getString("USER_ID"))) ;
                psmt.setString(2,resultSet.getString("USERNAME")) ;
                psmt.setString(3,resultSet.getString("ACCOUNT_STATUS")) ;               
                psmt.setString(4,resultSet.getString("TEMP_TB")) ;
                psmt.setDate(5,resultSet.getDate("CREATED")) ; 
                psmt.setString(6,resultSet.getString("DEFAULT_TB"));
                psmt.executeUpdate(); 
                psmt.close() ; 
            }
            }
           
           // SESSIONS
           
           String ses = "select SID, USER# USER_ID, USERNAME, SERIAL# SERIAL ,c.CURRENT_TIMESTAMP TIMESTAMP" +
                        " from v$session, (SELECT CURRENT_TIMESTAMP FROM dual) c " + " WHERE USERNAME IS NOT NULL" +
                        " order by 1" ;  
           
           resultSet = stmt.executeQuery(ses);
           
           while(resultSet.next()) {
        	   if(!resultSet.getString("USERNAME").equals("MANAGER")) {
        	   
        	   Statement stmt1 = conn2.createStatement();
        	   String tbs1 = " UPDATE SESSIONS" + 
                   	" SET SERIAL = "+resultSet.getString("SERIAL")+
                   	"," + " TIMESTAMP = CURRENT_TIMESTAMP "  +
                   	" WHERE SID = "  + resultSet.getString("SID");
        	   
        	   
        	   int i=0;
               i = stmt1.executeUpdate(tbs1);
               
               if(i==0) {
            	   
            	   ses = "INSERT INTO SESSIONS (SID,USER_ID,USERNAME,SERIAL,TIMESTAMP)"
                    + "VALUES(?, ?, ?, ?, CURRENT_TIMESTAMP)" ; 
            	   psmt = conn2.prepareStatement(ses) ; 
           
                psmt.setString(1,resultSet.getString("SID")) ;
                psmt.setFloat(2,Float.parseFloat(resultSet.getString("USER_ID")));
                psmt.setString(3,resultSet.getString("USERNAME"));
                psmt.setString(4,resultSet.getString("SERIAL")); 
                psmt.executeUpdate(); 
               }
           }
           }
           
           // IO
           
           String io = "select c.CURRENT_TIMESTAMP  TIMESTAMP ,  v1.mem FREE_MEMORY, v2.writes WRITES , v3.rrs READS" +
                        " from ( select sum(bytes)/1024 mem " +
                        " from v$sgastat where name = 'free memory') v1, " +
                        " (select SUM(VALUE) writes " +
                        " from ( select metric_name,begin_time,end_time,value " +
                        " from v$sysmetric_history " +
                        " where metric_name = 'Physical Writes Per Sec' " +
                        " order by 2 )) v2, " +
                        " (select SUM(VALUE) rrs " +
                        " from ( select metric_name,begin_time,end_time,value " +
                        " from v$sysmetric_history " +
                        " where metric_name = 'Physical Reads Per Sec' " +
                        " order by 2 )) v3 , " +
                        " (SELECT CURRENT_TIMESTAMP FROM dual) c " ; 
           resultSet = stmt.executeQuery(io);
           
           while(resultSet.next()) {
        	 
            	   
            	   io = "INSERT INTO IO (TIMESTAMP , WRITES, READS , FREE_MEMORY)"
                    + " VALUES(CURRENT_TIMESTAMP, ?, ?, ?)"; 
                    psmt = conn2.prepareStatement(io) ;
                    
                    psmt.setFloat(1,Float.parseFloat(resultSet.getString("WRITES"))) ; 
                    psmt.setFloat(2,Float.parseFloat(resultSet.getString("READS"))) ; 
                    psmt.setFloat(3,Float.parseFloat(resultSet.getString("FREE_MEMORY"))) ;                 
                    psmt.executeUpdate(); 
                    psmt.close() ;
           }
           
            
           Thread.sleep(5000);
            }
            
    }catch(ClassNotFoundException e){
        System.out.println(e) ; 
        }catch(SQLException e){
            System.out.println(e) ; 
            } catch (InterruptedException ex) {
        Logger.getLogger(Connector.class.getName()).log(Level.SEVERE, null, ex);
    }
        



}

}