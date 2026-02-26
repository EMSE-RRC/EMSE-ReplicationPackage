
package openstack_network_builder;



/**
 *
 * @author ReshmaRoychoudhuri
 */
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
import java.util.Arrays;


public class Openstack_Network_Builder {

    String AuthorSet="";
    int count1=0,edgesRowCount=1,rowCount=0,repeatPool=0;
    static int slno;//, p=0,q=0,n=0;
    String bugID="";
    

 
    //In this method each bug Id is fetched from the CSV file and is passed on to the next method for further processing.
    public void readBugCSVFile() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {
        String line;   
        
        
        BufferedReader br = new BufferedReader(new FileReader("openstack_buglist.csv"));
        while ((line = br.readLine()) != null) {
            String[] cols = line.split("\n");
            bugID= cols[0];
            System.out.println("Current Bug value= " + bugID);
            executeQuery(bugID);
            
            
        }
    }
    
    //Creates the list of authors for each bug id and passes it on to the next method
    public void executeQuery(String bugId) throws ClassNotFoundException, SQLException, IOException{
       
        String[] authorList=new String[100];       
        int i=0;
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/Openstack?useSSL=false","root","admin");
        Statement stmt=con.createStatement();  
        Statement stmt1=con.createStatement();
        
        String query= "SELECT DISTINCT AUTHOR FROM ISSUES_LOG_LAUNCHPAD WHERE ISSUE_ID="+bugId+";"; 
        String query1="Select count(*) from edges;";
  
        ResultSet rs=stmt.executeQuery(query);
        ResultSet rs1=stmt1.executeQuery(query1);
        while(rs1.next()){repeatPool=repeatPool+rs1.getInt(1);}
         while(rs.next())  {
             if(count1==0){count1++;}             
             authorList[i] = rs.getString(1);      
             i++;             
         }
         con.close();
         stmt.close();
         stmt1.close();
         rs.close();
         rs1.close();
        verticesBuilder(authorList,bugId);
        
    }
    
    //Checks if the author already exists in the vertices table. If it does not a new row is added. If it exists then the incumbent list is icremented by 1.
     public void verticesBuilder(String[] author, String bugId) throws ClassNotFoundException, SQLException, IOException   {
        int i=0,j=0;
        String[] incumbentList=new String[100];
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/Openstack_Eclipse?useSSL=false","root","admin");
        Statement stmt=con.createStatement();  
        while(author[i]!=null) {
             String query= "SELECT * FROM VERTICES WHERE DEV='"+author[i]+"';";           
             ResultSet rs=stmt.executeQuery(query);
             if(!rs.next())
             {
                 slno++;
                 String query1= "INSERT INTO VERTICES(SLNO,DEV,SHAPE,BUGID,COL1,COL2) VALUES ("+slno+",'"+author[i]+"','triangle','"+bugId+"',null,null);";       
                 stmt.executeUpdate(query1);
             }
             else
             {
                 incumbentList[j]=author[i];
                 j++;
             }
             i++;
             rs.close();
        }
        con.close();
        stmt.close();
        edgesBuilder(author,incumbentList);
        
    }
    
     //The network connections are built here. If no edge exists it is added. If it exists the the weight is incremented.
    public void edgesBuilder(String[] author, String[] incumbent) throws ClassNotFoundException, SQLException, IOException {
        int i,j,q=0,count=0,k=0,S=0,wt=0,newwt=0,fwt=0,fnewwt=0,owt=0,onewwt=0,ni=0,nn=0,ii=0;        
        while(author[k]!=null){count++;k++;}     
        
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/Openstack_Eclipse?useSSL=false","root","admin");
        Statement stmt=con.createStatement();  
        Statement stmt1=con.createStatement();  
        for(i=0;i<(count-1);i++)
        {
            for(j=(i+1);j<=(count-1);j++)
            {
                // Condition where both the authors belong to the incumbent list. However they may or may not have an edge between them
                if(Arrays.asList(incumbent).contains(author[i]) && Arrays.asList(incumbent).contains(author[j]))
                {
                    String query="Select weight from edges where dev1='"+author[i]+"' and dev2='"+author[j]+"';";
                    String reverseQuery="Select weight from edges where dev1='"+author[j]+"' and dev2='"+author[i]+"';";
                    ResultSet rs=stmt.executeQuery(query);
                    ResultSet rs1=stmt1.executeQuery(reverseQuery);
                    while (rs.next()) {wt=rs.getInt(1);}
                    while(rs1.next()) {newwt=rs1.getInt(1);}
   
                    if(wt==0 && newwt==0) //When they do not have an edge between them. In this case we insert an entry in the edges_27MAR table.
                    {              
                        ii++;
                        if(!author[i].equals(author[j])) {                      
                                String query1="Insert into edges (id,dev1,dev2,weight,color,col1,col2) values ("+edgesRowCount+",'"+author[i]+"','"+author[j]+"',1,'blue',null,null);";        
                                stmt.executeUpdate(query1);
                                edgesRowCount++;                     
                        }
                    }
                    else if(wt!=0 && newwt==0) //An edge exists. Update the weight of the edge in that case.
                    {          
                        q++;       
                        int updwt=wt+1;
                        String query2="Update edges set weight="+updwt+" where dev1='"+author[i]+"' and dev2= '"+author[j]+"';";     
                        stmt.executeUpdate(query2);
                    } 
                    else if(newwt!=0 && wt==0) 
                    {           
                            q++;                      
                            int updwt=newwt+1;
                            String query2="Update edges set weight="+updwt+" where dev1='"+author[j]+"' and dev2= '"+author[i]+"';";          
                            stmt.executeUpdate(query2);
                    }
                    wt=0;newwt=0;             
                    rs1.close();
                    rs.close();
                } // The next condition tests if ANY one of the authors belong to the list o incumbent. If any one belong, and not both, then an edge between them cannot belong to the edges_27MAR table.               
                else if((Arrays.asList(incumbent).contains(author[i]) && !Arrays.asList(incumbent).contains(author[j])) || (!Arrays.asList(incumbent).contains(author[i]) && Arrays.asList(incumbent).contains(author[j])))
                {                        
                            ni++;
                            String query3="Insert into edges (id,dev1,dev2,weight,color,col1,col2) values ("+edgesRowCount+",'"+author[i]+"','"+author[j]+"',1,'blue',null,null);";  
                            stmt.executeUpdate(query3);
                            edgesRowCount++;   
                } //Both the authors are new. They do not belong to the incumbent list.
                else if(!Arrays.asList(incumbent).contains(author[i]) && !Arrays.asList(incumbent).contains(author[j]))
                {                      
                            String query4="Insert into edges (id,dev1,dev2,weight,color,col1,col2) values ("+edgesRowCount+",'"+author[i]+"','"+author[j]+"',1,'green',null,null);";
                           // System.out.println(query4);
                            stmt.executeUpdate(query4);
                            edgesRowCount++;
                            nn++;    
                }
            }   
        }
        con.close();
        stmt.close();
        stmt1.close();
        
        pqnBuilder(author,incumbent,q,nn,ni,ii);
        
        
    }

    // After the network building is complete for the bug id, the p,q,n and S values are incremented. 
    
    public void pqnBuilder(String[] author, String[] incumbent,int repeatCollab, int nn, int ni, int ii) throws IOException {
        int i=0,j=0,k=0,p=0,q=0,n=0;;
        FileWriter writer = new FileWriter("MyFileOS.txt", true);
        while(incumbent[i]!=null){p++;i++;}
       
        q=repeatCollab;
        while(author[j]!=null){j++;}
        n=(j-i);
   
        String line="issueID = "+bugID+" p = "+p+"  q = "+q+" n = "+n+" total = "+j+" NN = "+nn+" NI = "+ni+" II = "+ii+" RepeatPool = "+repeatPool;
         
        writer.write(line);
        writer.write("\r\n");
        writer.close();
        
    }
    
    //Finally, from the edges_27MAR and vertices table the pajek file is generated.
    public void pajekFileGenerator() throws ClassNotFoundException, SQLException, IOException    {
        int pcount=1,edgeTotal=0,verTotal=0,slno=0,eweight=0; String dev1="",dev2="",dev1_slNo="",dev2_slNo="",dev="",edev1="",edev2="";
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/Openstack_Eclipse?useSSL=false","root","admin");
        
        System.out.println("In pajek File generator");
        //Prepare table for pajek file formation 
        Statement crtTab=con.createStatement();
        String queryCrt="create table edges_pajek as select * from edges;";
        crtTab.executeUpdate(queryCrt);
        Statement stmt4=con.createStatement();  
        String query4="SELECT COUNT(*) FROM EDGES_PAJEK;";
        ResultSet rs4=stmt4.executeQuery(query4);  
        while(rs4.next()) {edgeTotal=Integer.parseInt(rs4.getString(1)); }
       
        
        Statement stmt5=con.createStatement();  
        String query5="SELECT COUNT(*) FROM VERTICES;";
        ResultSet rs5=stmt5.executeQuery(query5);  
        while(rs5.next()) {verTotal=Integer.parseInt(rs5.getString(1)); }
      
        
        while(pcount<=edgeTotal) {
            Statement stmt=con.createStatement();  
            Statement stmt1=con.createStatement();  
            Statement stmt2=con.createStatement(); 
            Statement stmt3=con.createStatement();    
            String query="SELECT DEV1,DEV2 FROM EDGES_PAJEK WHERE ID="+pcount+";";
            ResultSet rs=stmt.executeQuery(query);  
            while(rs.next()) {dev1=rs.getString(1); dev2=rs.getString(2);}
    
            String query1="SELECT DISTINCT SLNO FROM VERTICES WHERE DEV='"+dev1+"';";
     
            String query2="SELECT DISTINCT SLNO FROM VERTICES WHERE DEV='"+dev2+"';";
      
            ResultSet rs1=stmt1.executeQuery(query1);  
            ResultSet rs2=stmt2.executeQuery(query2);  
            while(rs1.next()) {dev1_slNo=rs1.getString(1);}   while(rs2.next()) {dev2_slNo=rs2.getString(1);}
            String query3="UPDATE EDGES_PAJEK SET DEV1='"+dev1_slNo+"', DEV2='"+dev2_slNo+"' WHERE ID="+pcount+";";
      
            stmt3.executeUpdate(query3); 
            pcount++;
            rs.close(); rs1.close(); rs2.close(); stmt.close(); stmt1.close(); stmt2.close(); stmt3.close();
        }
      
        
        // File Writing starts
 
        System.out.println("File Writing starts");
        Statement stmt6=con.createStatement(); 
        FileWriter writer = new FileWriter("PajekFileOS.net", false); 
        String lineFirst="*Vertices"+" "+verTotal;
        writer.write(lineFirst);
        writer.write("\r\n");
        System.out.println(" Writing Vertices");
        for(int i=1;i<=verTotal;i++)
        {
             
            String query6="SELECT SLNO,DEV FROM VERTICES WHERE SLNO="+i+";";
            ResultSet rs6=stmt6.executeQuery(query6);  
            while(rs6.next()) {slno=Integer.parseInt(rs6.getString(1)); dev=rs6.getString(2);}
            String lineVertices=slno+" "+"\""+dev+"\"";
            writer.write(lineVertices);
            writer.write("\r\n");
            dev="";
            rs6.close();
        }
        String line2="*Edges";
        writer.write(line2);
        writer.write("\r\n"); 
        System.out.println(" Edges");

         for(int j=1;j<=edgeTotal;j++) {
            Statement stmt7=con.createStatement();  
            String query7="SELECT DEV1,DEV2,WEIGHT FROM EDGES_PAJEK WHERE ID="+j+";";
            ResultSet rs7=stmt7.executeQuery(query7);  
            while(rs7.next()) {edev1=rs7.getString(1); edev2=rs7.getString(2); eweight=Integer.parseInt(rs7.getString(3));}
            String lineEdges=edev1+" "+edev2+" "+eweight;
            writer.write(lineEdges);
            writer.write("\r\n"); 
            edev1="";edev2="";
            rs7.close();
            stmt7.close();
         }
         con.close();
         writer.close();
         rs4.close();
         rs5.close();
         stmt4.close();
         stmt5.close();
         stmt6.close();
    }
        
    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
        Openstack_Network_Builder obj =new Openstack_Network_Builder();
        
        obj.readBugCSVFile();       
        obj.pajekFileGenerator();
        
    }
    
}


