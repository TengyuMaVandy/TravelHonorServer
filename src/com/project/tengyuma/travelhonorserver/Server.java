package com.project.tengyuma.travelhonorserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

public class Server {
	
	private Connection connect = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null; // all above for database

	private static final Logger LOG = Logger.getLogger(
		    Thread.currentThread().getStackTrace()[0].getClassName() );
	
	ServerSocket ss = null;
	
	HashMap<String, ClientConnection> clients = new HashMap<String, ClientConnection>();
	HashMap<String, Group> groups = new HashMap<String, Group>();
	
	public void start(int port) {

		try {
			ss = new ServerSocket(port);
			LOG.info(this.toString()+" started");
			
			try {
				while (true) {
					Socket cs = ss.accept();
					LOG.info("Client connection accepted: "+cs.getRemoteSocketAddress().toString());
					
					new ClientConnection(this, cs).start();
				}
			} catch (IOException e) {
				LOG.severe("Error while accepting connections");
				e.printStackTrace();
			}
		} catch (IOException e) {
			LOG.severe("Cannot create server socket on port " + port);
			e.printStackTrace();
		} finally {
			if (ss != null && !ss.isClosed())
				try {
					LOG.info("Attempting to close server socket");
					ss.close();
				} catch (IOException e) {
					LOG.severe("Error closing server socket");
					e.printStackTrace();
				}
		}

	}

	@Override
	public String toString() {
		try {
			return "GroupCast server 1.0 "+InetAddress.getLocalHost().toString()+":"+ss.getLocalPort();
		} catch (UnknownHostException e) {
			return "GroupCast server 1.0 "+"localhost:"+ss.getLocalPort();
		}
	}

	public void addClient(String name, ClientConnection client) throws ClientNameException {
		synchronized (clients) {
			if(clients.containsKey(name))
				throw new ClientNameException();			
			try {
				  System.out.println("Get into addClient");
			      // This will load the MySQL driver, each DB has its own driver
			      Class.forName("com.mysql.jdbc.Driver");
			      System.out.println("1");
			      // Setup the connection with the DB
			      connect = DriverManager
			          .getConnection("jdbc:mysql://localhost/travelhonor?"
			              + "user=travelhonor&password=travelhonorpw");
			      System.out.println("2");
			      // Statements allow to issue SQL queries to the database
			      // statement = connect.createStatement(); //we never use statement method in addClient
			      System.out.println("3");
			      // PreparedStatements can use variables and are more efficient
			      preparedStatement = connect
			          .prepareStatement("insert into  travelhonor.profile values (default, ?, ?);");
			      //insert username to database travelhonor.profile
			      // "USERNAME, PASSWORD from travelhonor.profile");
			      // Parameters start with 1
			      System.out.println("4");
			      preparedStatement.setString(1, name);
			      preparedStatement.setString(2, "");
			      preparedStatement.executeUpdate();
			      System.out.println("5");
			      
			      /*preparedStatement = connect
				          .prepareStatement("insert into  travelhonor.collect values (default, ?, ?);");
				      //insert username to database travelhonor.collect
				      // "USERNAME, PASSWORD from travelhonor.profile");
				      // Parameters start with 1
				      System.out.println("4");
				      preparedStatement.setString(1, name);
				      preparedStatement.setString(2, "1");
				      preparedStatement.executeUpdate();*/
				      System.out.println("5");

			      preparedStatement = connect
			          .prepareStatement("SELECT USERNAME, PASSWORD from travelhonor.profile;");
			      //check the insert successful or not
			      resultSet = preparedStatement.executeQuery();
			      writeResultSet(resultSet);

			      	      
			    } catch (Exception e) {
			      
			    } finally {
			      close();
			    }
			clients.put(name, client);
		}
	}	
	
	public void getClient(String name, ClientConnection client) throws ClientNameException {
		synchronized (clients) {
			if(clients.containsKey(name))
				throw new ClientNameException();			
			try {
				  System.out.println("Get into getClient");
			      // This will load the MySQL driver, each DB has its own driver
			      Class.forName("com.mysql.jdbc.Driver");
			      System.out.println("1");
			      // Setup the connection with the DB
			      connect = DriverManager
			          .getConnection("jdbc:mysql://localhost/travelhonor?"
			              + "user=travelhonor&password=travelhonorpw");			     
			      System.out.println("2");
			      preparedStatement = connect
			          .prepareStatement("SELECT USERNAME, PASSWORD from travelhonor.profile;");
			      //check the insert successful or not
			      resultSet = preparedStatement.executeQuery();
			      
			      writeResultSet(resultSet);

			      	      
			    } catch (Exception e) {
			      
			    } finally {
			      close();
			    }
			clients.put(name, client);
		}
	}
	
	public void addPassword(String password, ClientConnection client) throws Exception { //add password to database
		synchronized (clients) {
			/*if(clients.containsKey(password))				
				throw new Exception();	//check something is already in use*/
			clients.put(password, client);
			try {
				  System.out.println("Get into addPassword");
			      // This will load the MySQL driver, each DB has its own driver
			      Class.forName("com.mysql.jdbc.Driver");
			      System.out.println("1");
			      // Setup the connection with the DB
			      connect = DriverManager
			          .getConnection("jdbc:mysql://localhost/travelhonor?"
			              + "user=travelhonor&password=travelhonorpw");
			      System.out.println("2");

			      // Statements allow to issue SQL queries to the database
			      // statement = connect.createStatement(); //we don't need statement method in addPassword
			      System.out.println("3");
	
			      // PreparedStatements can use variables and are more efficient
			      preparedStatement = connect
			          .prepareStatement("update travelhonor.profile set PASSWORD=? where USERNAME=?; ");
			      //update the password for specific username
			      System.out.println("4");
			      // "USERNAME, PASSWORD from travelhonor.profile");
			      // Parameters start with 1
			      String clientName = client.name;
			      System.out.println(client.name);
			      preparedStatement.setString(1, password);
			      preparedStatement.setString(2, clientName);
			      preparedStatement.executeUpdate();
			      System.out.println("5");

			      preparedStatement = connect
			          .prepareStatement("SELECT USERNAME, PASSWORD from travelhonor.profile");
			      resultSet = preparedStatement.executeQuery();
			      writeResultSet(resultSet);
			      //check the password update result
			            	      
			    } catch (Exception e) {
			      
			    } finally {
			      close();
			    }

		}
	}

	public String getProfile(String name, ClientConnection client) throws Exception { //get profile to a certain user
		synchronized (clients) {
			/*if(clients.containsKey(password))				
				throw new Exception();	//check something is already in use*/
			//clients.put(name, client); //we don't need push name value again. This method works for getting user's profile
			try {
				  System.out.println("Get into getProfile");
			      // This will load the MySQL driver, each DB has its own driver
			      Class.forName("com.mysql.jdbc.Driver");
			      System.out.println("1");
			      // Setup the connection with the DB
			      connect = DriverManager
			          .getConnection("jdbc:mysql://localhost/travelhonor?"
			              + "user=travelhonor&password=travelhonorpw");
			      System.out.println("2");

			      // Statements allow to issue SQL queries to the database
			      // statement = connect.createStatement();
			      System.out.println("3");
	
			      // PreparedStatements can use variables and are more efficient
			      preparedStatement = connect
			          .prepareStatement("select USERNAME, LATITUDE, LONGITUDE, REMARK from travelhonor.collect, travelhonor.latlng where collect.LATLNG=latlng.id AND collect.USERNAME=?;");
			      System.out.println("4");
			      //get profile from database including USERNAME, LATITUDE, LONGITUDE, REMARK
			      //notice that the latitude and longitude is the memorable location which the user has already collected.
			      //Parameters start with 1
			      String clientName = client.name;
			      System.out.println(client.name);
			      preparedStatement.setString(1, clientName);
			      System.out.println("5");
			      resultSet = preparedStatement.executeQuery();
			      String profile = "";
			      Boolean queryFlag = resultSet.next();
			      if(queryFlag) {
			    	  while (queryFlag) {
					      // It is possible to get the columns via name
					      // also possible to get the columns via the column number
					      // which starts at 1
					      // e.g. resultSet.getSTring(2);
					    	 //for  (int i = 1; i<= resultSet.getMetaData().getColumnCount(); i++){
				    		  String userName = resultSet.getString("USERNAME");
						      String latitude = resultSet.getString("LATITUDE");
						      String longitude = resultSet.getString("LONGITUDE");
						      String remark = resultSet.getString("REMARK");
				    		  //System.out.println(resultSet.getMetaData().getColumnName(i) + ": " + resultSet.getString(resultSet.getMetaData().getColumnName(i)));
						      					      profile = profile + userName + "    |  " + latitude + "  |  " + longitude + "  |  " + remark + "#";
						      System.out.println("profile: " +  profile);
						      queryFlag = resultSet.next();
					   	      //System.out.println("Column " +i  + " "+ resultSet.getMetaData().getColumnName(i));
					    }			      		      
				      System.out.println("6");
				      return profile;
			      }

			    } catch (Exception e) {
			      
			    } finally {
			      close();
			    }
		}
		return "empty";
	}
	
	public String signIn(String name, ClientConnection client) throws Exception { //handle sign in
		synchronized (clients) {
			/*if(clients.containsKey(password))				
				throw new Exception();	//check something is already in use*/
			//clients.put(name, client); //we don't need push name value again. This method works for getting user's profile
			try {
				  System.out.println("Get into signIn");
			      // This will load the MySQL driver, each DB has its own driver
			      Class.forName("com.mysql.jdbc.Driver");
			      System.out.println("1");
			      // Setup the connection with the DB
			      connect = DriverManager
			          .getConnection("jdbc:mysql://localhost/travelhonor?"
			              + "user=travelhonor&password=travelhonorpw");
			      System.out.println("2");

			      // Statements allow to issue SQL queries to the database
			      // statement = connect.createStatement();
			      System.out.println("3");
	
			      // PreparedStatements can use variables and are more efficient
			      preparedStatement = connect
			          .prepareStatement("select USERNAME, PASSWORD from travelhonor.profile where profile.USERNAME=?;");
			      System.out.println("4");
			      //get profile from database including USERNAME, LATITUDE, LONGITUDE, REMARK
			      //notice that the latitude and longitude is the memorable location which the user has already collected.
			      //Parameters start with 1
			      String clientName = name;
			      System.out.println(name);
			      preparedStatement.setString(1, clientName);
			      System.out.println("5");
			      resultSet = preparedStatement.executeQuery();
			      System.out.println("6");
			      Boolean queryFlag = resultSet.next();
			      if(queryFlag) {
			    	  while (queryFlag) {
					      // It is possible to get the columns via name
					      // also possible to get the columns via the column number
					      // which starts at 1
					      // e.g. resultSet.getSTring(2);
				    	 //for  (int i = 1; i<= resultSet.getMetaData().getColumnCount(); i++){
			    		  String userName = resultSet.getString("USERNAME");
					      String password = resultSet.getString("PASSWORD");
			    		  //System.out.println(resultSet.getMetaData().getColumnName(i) + ": " + resultSet.getString(resultSet.getMetaData().getColumnName(i)));
					      System.out.println("USERNAME: " + userName);
					      System.out.println("PASSWORD: " + password);
					      queryFlag = resultSet.next();
					      System.out.println("7");
				   	      //System.out.println("Column " +i  + " "+ resultSet.getMetaData().getColumnName(i));
					      return userName;
			    	  }
			      }
			      

			    } catch (Exception e) {
			      
			    } finally {
			      close();
			    }
		}
		return "empty";
	}
	
	public String getPassword(String password, ClientConnection client) throws Exception { //handle sign in
		synchronized (clients) {
			/*if(clients.containsKey(password))				
				throw new Exception();	//check something is already in use*/
			//clients.put(name, client); //we don't need push name value again. This method works for getting user's profile
			try {
				  System.out.println("Get into getPassword");
			      // This will load the MySQL driver, each DB has its own driver
			      Class.forName("com.mysql.jdbc.Driver");
			      System.out.println("1");
			      // Setup the connection with the DB
			      connect = DriverManager
			          .getConnection("jdbc:mysql://localhost/travelhonor?"
			              + "user=travelhonor&password=travelhonorpw");
			      System.out.println("2");

			      // Statements allow to issue SQL queries to the database
			      // statement = connect.createStatement();
			      System.out.println("3");
	
			      // PreparedStatements can use variables and are more efficient
			      preparedStatement = connect
			          .prepareStatement("select USERNAME, PASSWORD from travelhonor.profile where profile.USERNAME=?;");
			      System.out.println("4");
			      //get profile from database including USERNAME, LATITUDE, LONGITUDE, REMARK
			      //notice that the latitude and longitude is the memorable location which the user has already collected.
			      //Parameters start with 1
			      String clientName = client.name;
			      System.out.println(clientName);
			      preparedStatement.setString(1, clientName);
			      System.out.println("5");
			      resultSet = preparedStatement.executeQuery();
			      System.out.println("6");
			      Boolean queryFlag = resultSet.next();
			      if(queryFlag) {
			    	  while (queryFlag) {
					      // It is possible to get the columns via name
					      // also possible to get the columns via the column number
					      // which starts at 1
					      // e.g. resultSet.getSTring(2);
				    	 //for  (int i = 1; i<= resultSet.getMetaData().getColumnCount(); i++){
			    		  String userName = resultSet.getString("USERNAME");
					      String getPassword = resultSet.getString("PASSWORD");
			    		  //System.out.println(resultSet.getMetaData().getColumnName(i) + ": " + resultSet.getString(resultSet.getMetaData().getColumnName(i)));
					      System.out.println("USERNAME: " + userName);
					      System.out.println("PASSWORD: " + password);
					      queryFlag = resultSet.next();
				   	      //System.out.println("Column " +i  + " "+ resultSet.getMetaData().getColumnName(i));
					      return getPassword;
				      }
			      }
			      

			    } catch (Exception e) {
			      
			    } finally {
			      close();
			    }
		}
		return "empty";
	}
	
	public String getAllMedal(ClientConnection client) throws Exception { //get allmedal to a certain user
		synchronized (clients) {
			/*if(clients.containsKey(password))				
				throw new Exception();	//check something is already in use*/
			//clients.put(name, client); //we don't need push name value again. This method works for getting user's profile
			try {
				  System.out.println("Get into getAllMedal");
			      // This will load the MySQL driver, each DB has its own driver
			      Class.forName("com.mysql.jdbc.Driver");
			      System.out.println("1");
			      // Setup the connection with the DB
			      connect = DriverManager
			          .getConnection("jdbc:mysql://localhost/travelhonor?"
			              + "user=travelhonor&password=travelhonorpw");
			      System.out.println("2");

			      // Statements allow to issue SQL queries to the database
			      // statement = connect.createStatement();
			      System.out.println("3");
	
			      // PreparedStatements can use variables and are more efficient
			      preparedStatement = connect
			          .prepareStatement("select id, LATITUDE, LONGITUDE, REMARK from travelhonor.latlng");
			      System.out.println("4");
			      //get profile from database including USERNAME, LATITUDE, LONGITUDE, REMARK
			      //notice that the latitude and longitude is the memorable location which the user has already collected.
			      //Parameters start with 1
			      resultSet = preparedStatement.executeQuery();
			      String allMedal = "";
			      Boolean queryFlag = resultSet.next();
			      if(queryFlag) {
			    	  while (queryFlag) {
				    	  
					      // It is possible to get the columns via name
					      // also possible to get the columns via the column number
					      // which starts at 1
					      // e.g. resultSet.getSTring(2);
				    	  //for  (int i = 1; i<= resultSet.getMetaData().getColumnCount(); i++){
			    	      Integer id = resultSet.getInt("id");
					      String latitude = resultSet.getString("LATITUDE");
					      String longitude = resultSet.getString("LONGITUDE");
					      String remark = resultSet.getString("REMARK");
			    		  //System.out.println(resultSet.getMetaData().getColumnName(i) + ": " + resultSet.getString(resultSet.getMetaData().getColumnName(i)));
					      					      allMedal = allMedal + id.toString() + "|" + latitude + "|" + longitude + "|" + remark + "#";
					      System.out.println("allMedal: " + allMedal);
					      queryFlag = resultSet.next();
				   	      //System.out.println("Column " +i  + " "+ resultSet.getMetaData().getColumnName(i));
				      }			      		      
			      System.out.println("6");
			      return allMedal;
			      }

			    } catch (Exception e) {
			      
			    } finally {
			      close();
			    }
		}
		return "empty";
	}
	
	public void collectMedal(String nameCollect, ClientConnection client) throws Exception { //get collectMedal to a certain user. nameCollect includes username and his collect loaction
		    synchronized (clients) {
			/*if(clients.containsKey(password))				
				throw new Exception();	//check something is already in use*/
			//clients.put(name, client); //we don't need push name value again. This method works for getting user's profile
			try {
				  System.out.println("Get into collectMedal");
				  String name = nameCollect.split("#")[0];
				  String collectMedal = nameCollect.split("#")[1];
				  System.out.println(name);
				  System.out.println(collectMedal);
				  
			      // This will load the MySQL driver, each DB has its own driver
			      Class.forName("com.mysql.jdbc.Driver");
			      System.out.println("1");
			      // Setup the connection with the DB
			      connect = DriverManager
			          .getConnection("jdbc:mysql://localhost/travelhonor?"
			              + "user=travelhonor&password=travelhonorpw");
			      System.out.println("2");

			      // Statements allow to issue SQL queries to the database
			      // statement = connect.createStatement();
			      System.out.println("3");
	
			      // PreparedStatements can use variables and are more efficient
			      preparedStatement = connect
			          .prepareStatement("insert travelhonor.collect value (default, ?, ?)");
			      preparedStatement.setString(1, name);
			      preparedStatement.setString(2, collectMedal);
			      System.out.println("4");
			      //get profile from database including USERNAME, LATITUDE, LONGITUDE, REMARK
			      //notice that the latitude and longitude is the memorable location which the user has already collected.
			      //Parameters start with 1
			      preparedStatement.executeUpdate();
			      		      
			      System.out.println("6");

			    } catch (Exception e) {
			      
			    } finally {
			      close();
			    }
		}

	}
	
	public String getMyCollect(String name, ClientConnection client) throws Exception { //get my collect location
	    synchronized (clients) {
		/*if(clients.containsKey(password))				
			throw new Exception();	//check something is already in use*/
		//clients.put(name, client); //we don't need push name value again. This method works for getting user's profile
		try {
			  System.out.println("Get into getMyCollect");
		      // This will load the MySQL driver, each DB has its own driver
		      Class.forName("com.mysql.jdbc.Driver");
		      System.out.println("1");
		      // Setup the connection with the DB
		      connect = DriverManager
		          .getConnection("jdbc:mysql://localhost/travelhonor?"
		              + "user=travelhonor&password=travelhonorpw");
		      System.out.println("2");

		      // Statements allow to issue SQL queries to the database
		      // statement = connect.createStatement();
		      System.out.println("3");

		      // PreparedStatements can use variables and are more efficient
		      preparedStatement = connect
		          .prepareStatement("select LATLNG from travelhonor.collect where USERNAME = ?");
		      preparedStatement.setString(1, name);
		      
		      System.out.println("4");
		      //get profile from database including USERNAME, LATITUDE, LONGITUDE, REMARK
		      //notice that the latitude and longitude is the memorable location which the user has already collected.
		      //Parameters start with 1
		      resultSet = preparedStatement.executeQuery();
		      System.out.println("5");
		      String myCollect = "";
		      Boolean queryFlag = resultSet.next();
		      if(queryFlag) {
		    	  while (queryFlag) {
			    	  System.out.println("6");
				      // It is possible to get the columns via name
				      // also possible to get the columns via the column number
				      // which starts at 1
				      // e.g. resultSet.getSTring(2);
				    	 //for  (int i = 1; i<= resultSet.getMetaData().getColumnCount(); i++){
					      String LatLng = resultSet.getString("LATLNG");
					      System.out.println("7");
					      
			    		  //System.out.println(resultSet.getMetaData().getColumnName(i) + ": " + resultSet.getString(resultSet.getMetaData().getColumnName(i)));
					      myCollect = myCollect + LatLng +"#";
					      System.out.println("myCollect: " + myCollect);
					      queryFlag = resultSet.next();
				   	      //System.out.println("Column " +i  + " "+ resultSet.getMetaData().getColumnName(i));
				    }		      		      
			      return myCollect;
		      }
		      

		    } catch (Exception e) {
		      
		    } finally {
		      close();
		    }
	}
	return "empty";
}
	
	public void removeClient(ClientConnection client) {
		synchronized (clients) {
			if(clients.containsKey(client.name))
				clients.remove(client.name);
		}

		
		HashSet<Group> groupSet;
		synchronized (groups) {
			groupSet = new HashSet<Group>(groups.values());			
		}
			
		for(Group g: groupSet) {
			try {
				quitGroup(g, client);
			} catch (NonMemberException e) {}						
		}		
		
	}

	public Group getGroupByName(String groupName) {
		synchronized (groups) {
			if(groups.containsKey(groupName)) {
				return groups.get(groupName);
			} else {
				return null;
			}
		}
	}

	
	public Group joinGroup(String groupName, ClientConnection client, int maxMembers) throws GroupFullException, MaxMembersMismatchException {
		Group g;

		synchronized (groups) {
			if(groups.containsKey(groupName)) {
				// group already exists
				g = groups.get(groupName);
				
				if(maxMembers > 0 && maxMembers != g.maxMembers)
					throw new MaxMembersMismatchException();
				
				if(g.maxMembers == 0 || g.members.size() < g.maxMembers)
					g.addMember(client);
				else
					throw new GroupFullException();
				
			} else {
				// new group needed
				g = new Group();
				g.name = groupName;
				g.maxMembers = maxMembers;
				g.addMember(client);
				groups.put(groupName, g);
			}
		}		
		
		return g;
	}


	public void quitGroup(String groupName, ClientConnection client) throws NoSuchGroupException, NonMemberException {
		synchronized (groups) {
			if(groups.containsKey(groupName)) {
				Group g = groups.get(groupName);
				quitGroup(g, client);
			} else 
				throw new NoSuchGroupException();
		}		
	}

	public void quitGroup(Group g, ClientConnection client) throws NonMemberException {
		synchronized (groups) {
			g.removeMember(client);
			LOG.info("Group members: "+g.members.size());
			if(g.members.isEmpty())
				groups.remove(g.name);
		}		
	}
			
	public ClientConnection getClientByName(String clientName) {
		synchronized (clients) {
			if(clients.containsKey(clientName)) {
				return clients.get(clientName);
			} else 
				return null;
		}		
	}

	public static void main(String[] args) {
		int port = 10721;
		
		// make sure printstream is printing CRLF for newline
		System.setProperty("line.separator","\r\n");
		
		if(args.length > 0) {
			try {
			port = Integer.parseInt(args[0]);
			} catch (Exception e) {
				LOG.warning("Invalid port specified: "+args[0]);
				LOG.warning("Using default port "+port);				
			}
		}

		Server server = new Server();
		server.start(port);
	}

	private void writeMetaData(ResultSet resultSet) throws SQLException { //all below for database 
	    //   Now get some metadata from the database
	    // Result set get the result of the SQL query
	    
	    System.out.println("The columns in the table are: ");
	    
	    System.out.println("Table: " + resultSet.getMetaData().getTableName(1));
	    for  (int i = 1; i<= resultSet.getMetaData().getColumnCount(); i++){
	      System.out.println("Column " +i  + " "+ resultSet.getMetaData().getColumnName(i));
	    }
	  }
	  
	  private void writeResultSet(ResultSet resultSet) throws SQLException {
		    // ResultSet is initially before the first data set
		    while (resultSet.next()) {
		      // It is possible to get the columns via name
		      // also possible to get the columns via the column number
		      // which starts at 1
		      // e.g. resultSet.getSTring(2);
		    	for  (int i = 1; i<= resultSet.getMetaData().getColumnCount(); i++){
		    		 System.out.println(resultSet.getMetaData().getColumnName(i) + ": " + resultSet.getString(resultSet.getMetaData().getColumnName(i)));
		   	    }		     
		     }
		  }

	  // You need to close the resultSet
	  private void close() {
	    try {
	      if (resultSet != null) {
	        resultSet.close();
	      }

	      if (statement != null) {
	        statement.close();
	      }

	      if (connect != null) {
	        connect.close();
	      }
	    } catch (Exception e) {

	    }
	  }
}
