/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */

import java.util.Formatter;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Cafe {

   // reference to physical database connection.
   private Connection _connection = null;
   static private String user_login = null;
   static private String user_type = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Cafe
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Cafe(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Cafe

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Cafe.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      Cafe esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Cafe object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Cafe (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice! Please try again.\n"); break;
            }//end switch
            if (authorisedUser != null) {
               user_login = authorisedUser;
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. Go to Menu");
                System.out.println("2. Update Profile");
                System.out.println("3. Place a Order");
                System.out.println("4. Update a Order");
                System.out.println(".........................");
                System.out.println("9. Log out");
                switch (readChoice()){
                   case 1: Menu(esql); break;
                   case 2: UpdateProfile(esql); break;
                   case 3: PlaceOrder(esql); break;
                   case 4: UpdateOrder(esql); break;
                   case 9: usermenu = false; break;
                   default : System.out.println("Unrecognized choice! Please try again.\n"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    **/
   public static void CreateUser(Cafe esql){
      try{
         System.out.print("\n*WARNING* User logins are final* \n");
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user phone: ");
         String phone = in.readLine();
         
	    String type="Customer";
	    String favItems="";

				 String query = String.format("INSERT INTO USERS (phoneNum, login, password, favItems, type) VALUES ('%s','%s','%s','%s','%s')", phone, login, password, favItems, type);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Cafe esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USERS WHERE login = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0){
         String query2 = String.format("SELECT type FROM USERS WHERE login = '%s'", login);
         user_type = esql.executeQueryAndReturnResult(query2).get(0).get(0).trim();
		return login;
    }
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

// Rest of the functions definition go in here

  public static void Menu(Cafe esql){                 //DONE
   try{
      boolean usermenu = true;
      String query = "";
      while(usermenu) {
         System.out.println("Viewing Menu: ");
         System.out.println("1. Press 1 to view drinks");
         System.out.println("2. Press 2 to view sweets");
         System.out.println("3. Press 3 to view soups");
         System.out.println("4. Press 4 for Entire Menu");
         System.out.println("5. Press 5 to search by item name");
         System.out.println("6. Press 6 to search by item type"); 
         if(user_type.equals("Manager")){
            System.out.println("7. Press 7 to Add/Update/Delete menu"); 
         }
         System.out.println("9. Press 9 to return to Main Menu");
         switch (readChoice()){
            case 1: 
            query = String.format("SELECT m.itemName, m.price, m.description FROM MENU m WHERE type = 'Drinks';");
            esql.executeQueryAndPrintResult(query); break;
               
            case 2: 
            query = String.format("SELECT m.itemName, m.price, m.description FROM MENU m WHERE type = 'Sweets';");
            esql.executeQueryAndPrintResult(query); break;

            case 3: 
            query = String.format("SELECT m.itemName, m.price, m.description FROM MENU m WHERE type = 'Soup';");
            esql.executeQueryAndPrintResult(query); break;

            case 4: 
            query = String.format("SELECT m.type, m.itemName, m.price, m.description FROM MENU m GROUP BY m.itemName ORDER BY m.type;");
            esql.executeQueryAndPrintResult(query); break;
            
            //SEARCH BY ITEM NAME
            case 5:
            searchByName(esql); 
            break;

            //SEARCH BY ITEM TYPE
            case 6: 
            searchByType(esql); 
            break;
            
            //update menu for user_type = manager
            case 7: 
            if(user_type.equals("Manager")){
               updateOptions(esql);
            }
            break;

            case 9: 
            usermenu = false; break;
            default : System.out.println("Unrecognized choice! Please try again.\n"); break;
         }
      }
      }catch(Exception e){
         System.err.println (e.getMessage ());
      } 
  }

  public static void  updateOptions(Cafe esql) {
     try{
      boolean usermenu = true;
      while(usermenu) {
         System.out.println("Please choose an option: ");
         System.out.println("1. Press 1 to ADD an item to menu");
         System.out.println("2. Press 2 to UPDATE an item to menu");
         System.out.println("3. Press 3 to DELETE an item to menu");
         System.out.println("9. Press 9 to return to Main Menu");
         switch (readChoice()){
            case 1: 
            addMenuItem(esql);
            usermenu = false; break;
               
            case 2: 
            updateMenuItem(esql);
            usermenu = false; break;

            case 3: 
            DeleteMenuItem(esql);
            usermenu = false; break;

            case 9: 
            usermenu = false; break;
            default : System.out.println("Unrecognized choice! Please try again.\n"); break;
         }
      }

     }catch(Exception e){
         System.err.println (e.getMessage ());
      } 
  }

  
   public static void addMenuItem(Cafe esql) {
      try {
        System.out.println("Please input an item's name: ");
        String item_name = in.readLine();
        System.out.println("Please input an item's type: ");
        String item_type = in.readLine();
        System.out.println("Please input an item's price: ");
        float item_price = Float.parseFloat(in.readLine());
        System.out.println("Please input an item's description: ");
        String item_description = in.readLine();
        String query = String.format("INSERT INTO MENU (itemName, type, price, description) VALUES ('%s','%s','%s', '%s');", item_name, item_type, item_price, item_description);
        esql.executeUpdate(query);
        System.out.println("Sucessfully added an item!");
        
    } catch (Exception e) {
         System.err.println (e.getMessage ());
    }
   }

   public static void updateMenuItem(Cafe esql) {
      try{
         boolean usermenu = true;
         String query = "";
         System.out.println("What would item would you like to update: ");
         String item_name = in.readLine();
         query = String.format("SELECT m.itemName, m.price, m.type , m.description FROM MENU m WHERE itemName = '%s';", item_name);
         if (esql.executeQuery(query) == 0) {
            System.out.println("Unknown item. Please try again.");
         }
         else{
            while(usermenu) {
               System.out.println("1. Press 1 to update type");
               System.out.println("2. Press 2 to update price");
               System.out.println("3. Press 3 to update description");
               System.out.println("9. Go Back to main menu");
      
               //UPDATE PHONE NUMBER
               switch (readChoice()){
                  case 1:
                  System.out.println("What would you like to update the type to?: ");
                  String new_type = in.readLine();
                  query = String.format("UPDATE MENU SET type = '%s' WHERE itemName = '%s';", new_type ,item_name);
                  esql.executeUpdate(query);
                  System.out.println("Successfully updated type!\n"); 
                  usermenu = false;
                  break;
                  
                  case 2:
                  System.out.println("What would you like to update the price to?: ");
                  Float new_price = Float.parseFloat(in.readLine());
                  query = String.format("UPDATE MENU SET price = '%f' WHERE itemName = '%s';", new_price ,item_name);
                  esql.executeUpdate(query);
                  System.out.println("Successfully updated price!\n"); 
                  usermenu = false;
                  break;
               
                  case 3:
                  System.out.println("What would you like to update the description to?: ");
                  String new_description = in.readLine();
                  query = String.format("UPDATE MENU SET description = '%s' WHERE itemName = '%s';", new_description ,item_name);
                  esql.executeUpdate(query);
                  System.out.println("Successfully updated description!\n"); 
                  usermenu = false;
                  break;

                  case 9: usermenu = false; break;
                  default : System.out.println("Unrecognized choice! Please try again.\n"); break;
               }
            }   
         }
      }
         catch(Exception e){
               System.err.println (e.getMessage ());
         }
      
   }

   public static void DeleteMenuItem(Cafe esql) {
      try {
        System.out.println("Please input an item's name: ");
        String user_input = in.readLine();
        String query = String.format("SELECT m.itemName, m.price, m.type , m.description FROM MENU m WHERE itemName = '%s';", user_input);
        if (esql.executeQuery(query) == 0) {
            System.out.println("Invalid input please try again\n");
        }
         else {
            query = String.format("DELETE FROM Menu WHERE itemName = '%s';", user_input);
            esql.executeUpdate(query);
            System.out.println("Successfully Deleted Item!\n");
         }
    } catch (Exception e) {
         System.err.println (e.getMessage ());
    }
   }


   public static void searchByName(Cafe esql)  {         //Do we need this?
     try {
        System.out.println("Please input an item's name: ");
        String user_input = in.readLine();
        String query = String.format("SELECT m.itemName, m.price, m.type , m.description FROM MENU m WHERE itemName = '%s';", user_input);
        if (esql.executeQuery(query) == 0) {
            System.out.println("Invalid input please try again\n");
        }
         else {
            esql.executeQueryAndPrintResult(query);
         }

    } catch (Exception e) {
         System.err.println (e.getMessage ());
    }
     
     

  }

   public static void searchByType(Cafe esql) {
     try {
        System.out.println("Please input a type: ");
        String user_input = in.readLine();
        String query = String.format("SELECT m.itemName, m.price, m.type , m.description FROM MENU m WHERE type = '%s';", user_input);
        if (esql.executeQuery(query) == 0) {
            System.out.println("Invalid input please try again\n");
        }
         else {
            esql.executeQueryAndPrintResult(query);
         }

    } catch (Exception e) {
         System.err.println (e.getMessage ());
    }
  }

   public static void UpdateProfile(Cafe esql){
     try{
      boolean usermenu = true;
      String query = "";
      while(usermenu) {
         System.out.println("Update Profile: ");
         System.out.println("1. Update password");
         System.out.println("2. Update phone number");
         System.out.println("3. Update favorite items");
         if(user_type.equals("Manager")){
            System.out.println("4. Select User To Update");
         }
         System.out.println("9. Cancel");
         // System.out.println("3. Update Favorited Items");

         //UPDATE PHONE NUMBER
         switch (readChoice()){
            case 1:
            UpdatePassword(esql, user_login);
            break;

            case 2:
            UpdatePhone(esql,user_login);
            break;

            case 3:
            UpdateFavoriteItems(esql,user_login);
            break;
       


            case 4:
            if(user_type.equals("Manager")){
               System.out.println("Please enter user you want to update: ");
               String user_name = in.readLine();
               query = String.format("SELECT * FROM Users u WHERE login = '%s';", user_name);
               if (esql.executeQuery(query) == 0) {
                  System.out.println("User not found.");
               }
               else {
                  ManagerUpdateMenu(esql, user_name);
               }
            }
            break;

            case 9: usermenu = false; break;
            default : System.out.println("Unrecognized choice! Please try again.\n"); break;
         }

      }
      }catch(Exception e){
         System.err.println (e.getMessage ());
      } 
  }

  public static void ManagerUpdateMenu(Cafe esql, String user_name) {
     try {
        boolean usermenu = true;       
        while(usermenu) {
            System.out.println("Select Option: ");          //VERIFY THAT HE USER IS REAL
            System.out.println("1. Update password");
            System.out.println("2. Update phone number");
            System.out.println("3. Update favorite items");
            System.out.println("4. Update user type");
            System.out.println("9. Cancel");

           
            switch (readChoice()){
            case 1:
            UpdatePassword(esql, user_name);
            break;
            case 2:
            UpdatePhone(esql, user_name);
            break;
            case 3:
            UpdateFavoriteItems(esql, user_name);
            break;
            case 4:
            System.out.println("Please enter new rank: ");
            String rank = in.readLine();
            if(!rank.equals("Customer") && !rank.equals("Manager") && !rank.equals("Employee"))
            {
               System.out.println("Invalid input. Returning to update profile menu\n"); 
            }
            else {
               String query = String.format("UPDATE USERS SET type = '%s' WHERE login = '%s';", rank, user_name);
               esql.executeUpdate(query); 
               System.out.println("Successfully changed rank of " + user_name + ".\n"); 
            }



            case 9: usermenu = false; break;
            default : System.out.println("Unrecognized choice! Please try again.\n"); break;
            }
        }

        
     }catch(Exception e){
         System.err.println (e.getMessage ());
      } 
  }

   public static void UpdatePassword(Cafe esql, String user_name) {
      try {
         String user = user_name;
         System.out.println("Please enter new password: ");
         String password = in.readLine();
         String query = String.format("UPDATE USERS SET password = '%s' WHERE login = '%s';", password, user);
         esql.executeUpdate(query); 
         System.out.println("Successfully changed password!.\n"); 
      }catch(Exception e){
         System.err.println (e.getMessage ());
      } 
   }
   public static void UpdatePhone(Cafe esql, String user_name) {
      try {
         String user = user_name;
         System.out.println("Please enter new phone number: ");
         String phone_number = in.readLine();
         String query2 = String.format("UPDATE USERS SET phoneNum = '%s' WHERE login = '%s';", phone_number, user);
         esql.executeUpdate(query2); 
         System.out.println("Successfully changed phone number!.\n");
         
      }catch(Exception e){
         System.err.println (e.getMessage ());
      } 
   }
   public static void UpdateFavoriteItems(Cafe esql, String user_name) {
      try {
         System.out.println("Viewing current favorite items:"); 
         String user = user_name;
         String query = String.format("SELECT u.favItems FROM USERS u WHERE login = '%s'", user);
         String old_fav = esql.executeQueryAndReturnResult(query).get(0).get(0).trim();
         System.out.println(old_fav);   
         System.out.println("What item would you like to add?\n");        //Consider adding delete
         
         String item_add = in.readLine();
         old_fav = old_fav + "," + item_add;
         query = String.format("UPDATE USERS SET favItems = '%s' WHERE login = '%s';", old_fav, user);
         esql.executeUpdate(query);
         System.out.println("Updated favorite item(s), " + old_fav + "\n");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      } 
   }

   public static void PlaceOrder(Cafe esql){
     try{
      boolean usermenu = true;
      String query = "";
      String user = user_login;
      while(usermenu) {
         System.out.println("Order: ");
         System.out.println("1. Place order(s)");
         System.out.println("2. View order history");
         System.out.println("3. View item status history");

         
         System.out.println("9. Go back");

         switch (readChoice()){
            case 1:
               UserOrder(esql); //Technically done rn
            break;
            
            //DONE!
            case 2: 
            if(!user_type.equals("Customer")){
               query = String.format("SELECT * FROM ORDERS WHERE login = '%s' AND timeStampRecieved >= NOW() - '1 day'::INTERVAL;", user);
               esql.executeQueryAndPrintResult(query); break;
            }
            else{
               query = String.format("SELECT * FROM ORDERS WHERE login = '%s' ORDER BY orderid DESC LIMIT 5", user);
               esql.executeQueryAndPrintResult(query); break;
            }

            case 3:

            query = String.format("SELECT * FROM ITEMSTATUS ORDER BY orderid DESC LIMIT 10", user);
            esql.executeQueryAndPrintResult(query); 
            break;

            case 9: usermenu = false; break;
            default : System.out.println("Unrecognized choice! Please try again.\n"); break;
         }

      }
      }catch(Exception e){
         System.err.println (e.getMessage ());
      } 
  }
  public static void UserOrder(Cafe esql) {
      try{
         String order_name = "";
         List<String> list = new ArrayList<String>();
         String message = "What would you like to order? Or type 'q' to quit";
         while (!order_name.equals("q")) {
            System.out.println(message);
            order_name = in.readLine();
            order_name = order_name.replace("\n", "");
            if (order_name.equals("q")) {
               break;
            }
            String query = String.format("SELECT m.itemName FROM MENU m WHERE m.itemName = '%s';", order_name);      //Check if it exists
            if (esql.executeQuery(query) != 1) {
               System.out.println("Item does not exist... Please try again.");
            }
            else {
               message = "What more would you like to order? Or type 'q' to quit";
               list.add(order_name);
            }
         }
         if (!list.isEmpty()) {
            //FIRST INTO ITEMSTATUS TO CALCULATE TOTAL
            PreparedStatement myStmt;
            String query = String.format("SELECT * FROM Orders o;");
            int order_id = esql.executeQuery(query) + 1;                                  //Set order id to the queries return count + 1
            float totalCost = 0;
            //INSERT INTO ORDERS

            myStmt = esql._connection.prepareStatement("INSERT INTO ORDERS (orderid, login, paid, timeStampRecieved, total) VALUES (?,?,?,?,?);");
            myStmt.setInt(1, order_id);
            myStmt.setString(2, user_login);
            myStmt.setString(3, "f");
            myStmt.setTimestamp(4, new java.sql.Timestamp(new java.util.Date().getTime()));
            myStmt.setFloat(5, totalCost);
            myStmt.executeUpdate();

            for (int i = 0; i < list.size(); i++) {
               query = String.format("SELECT m.price FROM MENU m WHERE m.itemName = '%s';", list.get(i));
               float item_price = Float.parseFloat(esql.executeQueryAndReturnResult(query).get(0).get(0));
               totalCost = totalCost + item_price;
               myStmt = esql._connection.prepareStatement("INSERT INTO itemStatus (orderid, itemName, lastUpdated, status, comments) VALUES (?,?,?, 'Hasn''t started', ?);");
               myStmt.setInt(1, order_id);
               myStmt.setString(2, list.get(i));
               myStmt.setTimestamp(3, new java.sql.Timestamp(new java.util.Date().getTime()));
               myStmt.setString(4, "");
               myStmt.executeUpdate(); 
               myStmt.close();
            }

            query = String.format("UPDATE ORDERS SET total = '%f' WHERE orderid = '%s';", totalCost, order_id);
            esql.executeUpdate(query);

            System.out.println("Order placed!");
            System.out.println("Your Following orders are:");
            query = String.format("SELECT * FROM ITEMSTATUS WHERE orderid = '%s';", order_id);
            esql.executeQueryAndPrintResult(query);
            System.out.println("Your Receipt Is: ");
            query = String.format("SELECT * FROM ORDERS WHERE orderid = '%s';", order_id);
            esql.executeQueryAndPrintResult(query);
         }
         else {
            System.out.println("No orders placed.\n");
         }

      }catch(Exception e){
         System.err.println (e.getMessage ());
      } 
  }
      
  public static void UpdateOrder(Cafe esql){
     try{
      boolean usermenu = true;
      String query = "";
      while(usermenu) {
         System.out.println("Update Orders: ");
         System.out.println("1. Update non-paid order (Customers can update order here if non-paid)");               //CUSTOMER
         if(!user_type.equals("Customer")) {          
            System.out.println("2. Update user order ID to paid (Managers/employees can update user types here)");             //MANAGERS/EMPLOYEES
         }
         if(!user_type.equals("Customer")){
            System.out.println("3. Press 3 to update user order status"); 
         }
         System.out.println("9. Go back to menu");
         
         switch (readChoice()){
            case 1:
            System.out.println("Please enter the non-paid orderID");
            int order_id = Integer.parseInt(in.readLine());
            query = String.format("SELECT * FROM ORDERS WHERE orderid = '%s' AND paid = '%s'", order_id, "f");
            int found_id = esql.executeQuery(query);
            if(found_id > 0){
               esql.executeQueryAndPrintResult(query);
               System.out.println("\nOrderID found! Deleting old order...");
               query = String.format("DELETE FROM ItemStatus WHERE orderid = '%s'", order_id);
               esql.executeUpdate(query);
               query = String.format("DELETE FROM ORDERS WHERE orderid = '%s'", order_id);
               esql.executeUpdate(query);
               System.out.println("Order Successfully Deleted!\n");
               UserOrder(esql);

            }
            else {
                  System.out.println("OrderID not found!\n");
            }

            break;

            case 2:
            if(!user_type.equals("Customer")){
               System.out.println("Please enter the orderID you would like to change to paid.");
               order_id = Integer.parseInt(in.readLine());
               query = String.format("SELECT * FROM ORDERS WHERE orderid = '%s'", order_id);
               // = esql.executeQueryAndReturnResult(query).get(0).get(0).trim();
               found_id = esql.executeQuery(query);

               if (found_id > 0){
                  System.out.println("OrderID found! Updating to paid");
                  query = String.format("UPDATE ORDERS SET paid = '%b' WHERE orderid = '%s';", "t" ,order_id);
                  esql.executeUpdate(query);
                  System.out.println("Order updated successfully!\n");
               }
               else {
                  System.out.println("OrderID not found!\n");
               }
            }
            break;
               
            case 3:
            if(!user_type.equals("Customer")){
               System.out.println("Please enter the orderID you would like to update.");
               order_id = Integer.parseInt(in.readLine());
               query = String.format("SELECT * FROM ORDERS WHERE orderid = '%s'", order_id);
               // = esql.executeQueryAndReturnResult(query).get(0).get(0).trim();
               found_id = esql.executeQuery(query);

               if (found_id > 0){
                  System.out.println("OrderID found! Would you like to update status to Started, Finished, or Hasn't started?");
                  String new_status = in.readLine();
                  if(!new_status.equals("Started") && !new_status.equals("Finished") && !new_status.equals("Hasn't started")){
                     System.out.println("Invalid input. Returning to order menu."); //MAKE THIS GO UNTIL THEY TYPE IN SOMETHING PROPER?
                  }
                  else{
                  query = String.format("UPDATE ITEMSTATUS SET status = '%s' WHERE orderid = '%s';", new_status ,order_id);
                  esql.executeUpdate(query);
                  System.out.println("Order updated successfully!\n");
                  }
               }
               else {
                  System.out.println("OrderID not found!\n");
               }
            }
            break;

            case 9: usermenu = false; break;
            default : System.out.println("Unrecognized choice! Please try again.\n"); break;
         }

      }
      }catch(Exception e){
         System.err.println (e.getMessage ());
      } 
  }
  

}//end Cafe

