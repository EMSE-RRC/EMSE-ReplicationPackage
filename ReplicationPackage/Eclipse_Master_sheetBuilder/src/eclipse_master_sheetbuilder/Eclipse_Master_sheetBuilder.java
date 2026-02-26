
package eclipse_master_sheetbuilder;



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

public class Eclipse_Master_sheetBuilder {

    String bugID="",BugSet="";
    int bugCount=0;
    public void buildBugset () throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {
        String line;
        
        BufferedReader br1 = new BufferedReader(new FileReader("eclipse_IssueLiist.csv"));
         while ((line = br1.readLine()) != null) {
            String[] cols1 = line.split("\n");
            BugSet = BugSet + cols1[0] + "," ;
         }        
         BugSet=BugSet.substring(0,(BugSet.length()-1));        
        
    }
    public void readBugCSVFile() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {
        String line;          
        
        BufferedReader br = new BufferedReader(new FileReader("eclipse_IssueList.csv"));
        while ((line = br.readLine()) != null) {
            bugCount++;
            String[] cols = line.split("\n");
            bugID= cols[0];
            System.out.println("Current Bug value= " + bugCount);         
            executeQueryBug(bugID);
            
        }
    }
    public void commonFields() throws ClassNotFoundException, SQLException {
      //  System.out.println("Enterng commonFields");
        Class.forName("com.mysql.jdbc.Driver");
        Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/eclipse_tickets?useSSL=false","root","admin");
        Statement stmt=con.createStatement();  
        Statement stmt1=con.createStatement();  
         Statement stmt2=con.createStatement();
        String query="select  count(issues_log_bugzilla.issue_id) as no_of_comments, \n" +
"		issues.id as issue_id, \n" +
"        issues.status as status,       \n" +
"        issues.type as priority_code, \n" +
"        count(distinct issues_log_bugzilla.submitted_by) as commenters        \n" +
"        from issues_log_bugzilla, issues where \n" +
"        issues_log_bugzilla.issue_id=issues.id \n" +
"         and issues.id in  ("+BugSet+") \n"+  
"        group by issues.id INTO OUTFILE 'C:\\\\ProgramData\\\\MySQL\\\\MySQL Server 5.7\\\\Uploads\\\\file_eclipse_common8feb.csv' FIELDS TERMINATED BY ',';";
        stmt.executeQuery(query);
        String query1="select people.name as reported_by, issues.id as issue_id \n" +
            "from issues, people where people.id=issues.submitted_by \n" +
                "         and issues.id in  ("+BugSet+") \n"+  
            "group by issues.id \n" +
            "INTO OUTFILE 'C:\\\\ProgramData\\\\MySQL\\\\MySQL Server 5.7\\\\Uploads\\\\file_ecl_submitted_by8feb.csv' FIELDS TERMINATED BY ',';";
        stmt1.executeQuery(query1);
        String query2="select people.name as owner, \n" +
            "issues.id as issue_id from issues, people \n" +
            "where people.id=issues.assigned_to \n" +
                "         and issues.id in  ("+BugSet+") \n"+  
            "group by issues.id \n" +
            "INTO OUTFILE 'C:\\\\ProgramData\\\\MySQL\\\\MySQL Server 5.7\\\\Uploads\\\\file_ecl_owner8feb.csv' FIELDS TERMINATED BY ',';";
         stmt2.executeQuery(query2);
         con.close();
    }
    public void executeQueryBug(String bugId) throws ClassNotFoundException, SQLException, IOException{
        String owner="",owner_bugs="",comments_by_nonowners="",commenters_nonowners="",elapsed_time="";
  //       System.out.println("ENterng executeQueryBug");
        Class.forName("com.mysql.jdbc.Driver");
        Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/eclipse_tickets?useSSL=false","root","admin");
        Statement stmt=con.createStatement();  
        String query= "select assigned_to from issues_log_bugzilla where id="+bugId+";";
        ResultSet rs=stmt.executeQuery(query);
        if(rs.next()) {
            owner=rs.getString(1);           
        }else owner="0";
        Statement stmt1=con.createStatement(); 
        String query1="select count(distinct id) from issues_log_bugzilla where assigned_to='"+owner+"' ;";
        ResultSet rs1=stmt1.executeQuery(query1);
        if(rs1.next()) {
            owner_bugs=rs1.getString(1);
        }else owner_bugs="0";
        Statement stmt2=con.createStatement(); 
        String query2="select count(submitted_by) from issues_log_bugzilla where issue_id="+bugID+" and submitted_by!=assigned_to;";
        ResultSet rs2=stmt2.executeQuery(query2);
        if(rs2.next()) {
            comments_by_nonowners=rs2.getString(1);
        }else comments_by_nonowners="0";
        Statement stmt3=con.createStatement(); 
        String query3="select count(distinct submitted_by) from issues_log_bugzilla where issue_id="+bugID+" and submitted_by!=assigned_to;";
        ResultSet rs3=stmt3.executeQuery(query3);
        if(rs3.next()) {
            commenters_nonowners=rs3.getString(1);
        }else commenters_nonowners="0";
        Statement stmt4=con.createStatement(); 
        String query4="Select timestampdiff(second, min(date),max(date))/3600 as ElapsedTime , \n" +
                        "issue_id from issues_log_bugzilla where issue_id="+bugID+"";
        ResultSet rs4=stmt4.executeQuery(query4);
        if(rs4.next()) {
            elapsed_time=rs4.getString(1);
        }else elapsed_time="0";
        String line1="issue_id "+bugId+" owner "+owner+" owner.bugs "+owner_bugs+" comments_by_nonowners "+comments_by_nonowners+" commenters_nonowners "+commenters_nonowners+" elapsed_time "+elapsed_time;
        con.close();
        executeQueryComment(bugId,line1,owner);
        
    }
        
    
    public void executeQueryComment(String bugId, String line1, String owner) throws ClassNotFoundException, SQLException, IOException {
        String owner_comments="",owner_comments_nonowned="";
    //    System.out.println("Enterng executeQueryComment");
        Class.forName("com.mysql.jdbc.Driver");
        Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/eclipse_tickets?useSSL=false","root","admin");
 
        Statement stmt5=con.createStatement();   Statement stmt6=con.createStatement();      
        

        String query5= "select count(issue_id) from issues_log_bugzilla where submitted_by='"+owner+"' and issue_id in ("+BugSet+");";
       
        String query6="select count(issue_id) from issues_log_bugzilla where issue_log_bugzilla_trim.submitted_by=(select assigned_to from issues_backup where id="+bugID+") and issue_log_bugzilla_trim.submitted_by!=issue_log_bugzilla_trim.assigned_to and issue_id in ("+BugSet+") ;";
         
        ResultSet rs5=stmt5.executeQuery(query5);  ResultSet rs6=stmt6.executeQuery(query6);
        

        if(rs5.next()) {owner_comments=rs5.getString(1);                }else owner_comments="0";
        if(rs6.next()) {owner_comments_nonowned=rs6.getString(1);       }else owner_comments_nonowned="0";
        
        String line2=line1+" owner.comments "+owner_comments+" owner_comments_nonowned "+owner_comments_nonowned;
        con.close();
        fileWriter(line2);        
        
    }
    public void fileWriter(String line) throws IOException {
           

        FileWriter writer = new FileWriter("EclipseDetailFile.txt", true);
        writer.write(line);
        writer.write("\r\n");
        writer.close();

    }

    public static void main(String[] args) throws IOException, FileNotFoundException, ClassNotFoundException, SQLException {
       
        Eclipse_Master_sheetBuilder obj= new Eclipse_Master_sheetBuilder();
        obj.buildBugset(); obj.commonFields(); 
        obj.readBugCSVFile();
    }
    
}


