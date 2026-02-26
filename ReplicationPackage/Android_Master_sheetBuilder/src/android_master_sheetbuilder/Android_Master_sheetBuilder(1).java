/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package android_master_sheetbuilder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author ReshmaRoychoudhuri
 */

class buildQueryParams {
    
    String bugID="",BugSet="";
    
    public void buildBugset () throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {
        String line;
        
        BufferedReader br1 = new BufferedReader(new FileReader("androidd_buglist.csv")); //csv that contains list of bugids
         while ((line = br1.readLine()) != null) {
            String[] cols1 = line.split("\n");
            BugSet = BugSet + cols1[0] + "," ;
         }        
         BugSet=BugSet.substring(0,(BugSet.length()-1));        
        
    }
    
    public void readBugCSVFile() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {
        String line;          
        
        BufferedReader br = new BufferedReader(new FileReader("android_buglist.csv"));
        while ((line = br.readLine()) != null) {
            String[] cols = line.split("\n");
            bugID= cols[0];
            System.out.println("Current Bug value= " + bugID);
                 
           priorityCode(bugID);
            
        }
    }
    
    public void priorityCode(String bugId) throws ClassNotFoundException, SQLException, IOException{
        FileWriter writer = new FileWriter("PriorityCode.txt", true);
        Class.forName("com.mysql.jdbc.Driver");
        Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/g_models","root","admin");
        Statement stmt=con.createStatement();  
        String query= "select priority from bug where bugid="+bugId+";";
        ResultSet rs=stmt.executeQuery(query);
        while(rs.next()) {
            writer.write(rs.getString(1));
            writer.write("\r\n");
       }
         writer.close();
         con.close();
         executeQueryBug(bugID); 
    }
    
    public void executeQueryBug(String bugId) throws ClassNotFoundException, SQLException, IOException{
        String status="",owner="",type="",stars="",reportedBy="",owner_bugs="";
        
        Class.forName("com.mysql.jdbc.Driver");
        Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/g_models","root","admin");
        Statement stmt=con.createStatement();  
        String query= "select status,owner,type,stars,reportedBy from bug where bugid="+bugId+";";
        ResultSet rs=stmt.executeQuery(query);
        while(rs.next()) {
            status=rs.getString(1);
            owner=rs.getString(2);
            type=rs.getString(3);
            stars= rs.getString(4);
            reportedBy=rs.getString(5);
        }
        Statement stmt1=con.createStatement(); 
        String query1="select count(distinct bugid) from bug where owner='"+owner+"';";
        ResultSet rs1=stmt1.executeQuery(query1);
        while(rs1.next()) {
            owner_bugs=rs1.getString(1);
        }
        String line1=""+bugId+"   "+status+"   "+owner+"   "+type+"   "+stars+"   "+reportedBy+"   "+owner_bugs;
        executeQueryComment(bugId,line1,owner);
        con.close();
    }
    
    public void executeQueryComment(String bugId, String line1,String owner) throws ClassNotFoundException, SQLException, IOException {
        String comments="",commenters="", comments_by_nonowners="",commenters_nonowners="",owner_comments="",owner_comments_nonowned="";
        
        Class.forName("com.mysql.jdbc.Driver");
        Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/g_models","root","admin");
        Statement stmt1=con.createStatement();  Statement stmt2=con.createStatement();   Statement stmt3=con.createStatement();  
        Statement stmt4=con.createStatement();  Statement stmt5=con.createStatement();   Statement stmt6=con.createStatement();      
        
        String query1= "select count(what) from comment where bugid="+bugId+" and what not in (' ');";
        String query2= "select count(distinct author) from comment where bugid="+bugId+";";
        String query3= "select count(what) from comment where bugid ="+bugId+" and author not in ('"+owner+"') and what not in (' ');";
        String query4= "select count(distinct author) from comment where bugid ="+bugId+" and author not in ('"+owner+"');";
        String query5= "select count(what) from comment where author='"+owner+"' and bugid in ("+BugSet+");";
        String query6= "select count(what) from comment,bug where comment.bugid=bug.bugid and comment.author='"+owner+"' and bug.owner not in ('"+owner+"') and comment.bugid in ("+BugSet+");";

        ResultSet rs1=stmt1.executeQuery(query1);  ResultSet rs2=stmt2.executeQuery(query2);
        ResultSet rs3=stmt3.executeQuery(query3);  ResultSet rs4=stmt4.executeQuery(query4);
        ResultSet rs5=stmt5.executeQuery(query5);  ResultSet rs6=stmt6.executeQuery(query6);
        
        while(rs1.next()) {comments=rs1.getString(1);                      }
        while(rs2.next()) {commenters=rs2.getString(1);                    }
        while(rs3.next()) {comments_by_nonowners=rs3.getString(1);         }
        while(rs4.next()) {commenters_nonowners=rs4.getString(1);          }
        while(rs5.next()) {owner_comments=rs5.getString(1);                }
        while(rs6.next()) {owner_comments_nonowned=rs6.getString(1);       }
        
        String line2=line1+" "+comments+" "+commenters+" "+comments_by_nonowners+" "+commenters_nonowners+" "+owner_comments+" "+owner_comments_nonowned;
        
        fileWriter(line2);        
        con.close();
    }
    public void fileWriter(String line) throws IOException {
        FileWriter writer = new FileWriter("DetailFile.txt", true);
        writer.write(line);
        writer.write("\r\n");
        writer.close();

    }
}
public class Android_Master_sheetBuilder { 
    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException  {
        buildQueryParams b=new buildQueryParams();
            b.buildBugset();
            b.readBugCSVFile();
    }
    
}
