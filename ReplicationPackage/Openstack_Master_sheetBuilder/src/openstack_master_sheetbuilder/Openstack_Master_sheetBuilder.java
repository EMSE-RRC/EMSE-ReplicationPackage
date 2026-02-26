
package openstack_master_sheetbuilder;



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
public class Openstack_Master_sheetBuilder {
     String bugID="",BugSet="";
    
    public void buildBugset () throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {
        String line;
        
        BufferedReader br1 = new BufferedReader(new FileReader("openstack_bugliist.csv"));
         while ((line = br1.readLine()) != null) {
            String[] cols1 = line.split("\n");
            BugSet = BugSet + cols1[0] + "," ;
         }        
         BugSet=BugSet.substring(0,(BugSet.length()-1));        
        System.out.println(BugSet);
    }
    public void readBugCSVFile() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {
        String line;          
        
        BufferedReader br = new BufferedReader(new FileReader("openstack_buglist.csv"));
        while ((line = br.readLine()) != null) {
            String[] cols = line.split("\n");
            bugID= cols[0];
            System.out.println("Current Bug value= " + bugID);         
            executeQueryBug(bugID);
            
        }
    }
    public void executeQueryBug(String bugId) throws ClassNotFoundException, SQLException, IOException{
        String owner="",owner_bugs="";
        
        Class.forName("com.mysql.jdbc.Driver");
        Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/Openstack_Eclipse?useSSL=false","root","admin");
        Statement stmt=con.createStatement();  
        String query= "select assigned_to from issues where id="+bugId+";";
        ResultSet rs=stmt.executeQuery(query);
        while(rs.next()) {
            owner=rs.getString(1);           
        }
        Statement stmt1=con.createStatement(); 
        String query1="select count(distinct id) from issues where assigned_to='"+owner+"' ;";
        ResultSet rs1=stmt1.executeQuery(query1);
        while(rs1.next()) {
            owner_bugs=rs1.getString(1);
        }
        String line1="issue_id "+bugId+" owner "+owner+" owner.bugs "+owner_bugs;
        executeQueryComment(bugId,line1,owner);
        con.close();
        executeQueryComment(bugId,"","");
    }
        
    
    public void executeQueryComment(String bugId, String line1, String owner) throws ClassNotFoundException, SQLException, IOException {
        String owner_comments="",owner_comments_nonowned="",comments_by_nonowners="",commenters_nonowners="";
        
        Class.forName("com.mysql.jdbc.Driver");
        Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/Openstack_Eclipse?useSSL=false","root","admin");
 
        Statement stmt5=con.createStatement();   Statement stmt6=con.createStatement();      
         Statement stmt7=con.createStatement();  Statement stmt8=con.createStatement(); 

        String query5= "select count(issue_id) from issues_log_launchpad where submitted_by='"+owner+"' and issue_id in ("+BugSet+");";
       
        String query6="select count(issue_id) from issues_log_launchpad where issues_log_launchpad.submitted_by=(select assigned_to from issues where id="+bugID+") and issues_log_launchpad.submitted_by!=issues_log_launchpad.assigned_to and issue_id in ("+BugSet+") ;";
     
           
        String query7="select count(submitted_by) from issues_log_launchpad where issue_id="+bugID+" and submitted_by!=assigned_to;";
      
        
        String query8="select count(distinct submitted_by) from issues_log_launchpad where issue_id="+bugID+" and submitted_by!=assigned_to;";
                
        
        
        ResultSet rs5=stmt5.executeQuery(query5);  ResultSet rs6=stmt6.executeQuery(query6);
        ResultSet rs7=stmt7.executeQuery(query7); ResultSet rs8=stmt8.executeQuery(query8);
          
        while(rs5.next()) {owner_comments=rs5.getString(1);                }
        while(rs6.next()) {owner_comments_nonowned=rs6.getString(1);       }
        if(rs7.next()) {
            comments_by_nonowners=rs7.getString(1);
        }else comments_by_nonowners="0";
        if(rs8.next()) {
            commenters_nonowners=rs8.getString(1);
        }else commenters_nonowners="0";
        String line2=line1+" owner.comments "+owner_comments+" owner_comments_nonowned "+owner_comments_nonowned+" comments_by_nonowners "+comments_by_nonowners +" commenters_nonowners "+commenters_nonowners;
        
        fileWriter(line2);        
        con.close();
    }
    public void fileWriter(String line) throws IOException {
        FileWriter writer = new FileWriter("OpenstackDetailFile.txt", true);
        writer.write(line);
        writer.write("\r\n");
        writer.close();

    }
    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
            Openstack_Master_sheetBuilder b=new Openstack_Master_sheetBuilder();
            b.buildBugset();
            b.readBugCSVFile();
     }
}

    


