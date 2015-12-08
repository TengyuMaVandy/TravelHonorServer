package com.project.tengyuma.travelhonorserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.logging.Logger;

public class ClientConnection extends Thread {

	private static final Logger LOG = Logger.getLogger(Thread.currentThread()
			.getStackTrace()[0].getClassName());

	private static final int STATUS_ERROR = 0;
	private static final int STATUS_OK = 1;
	private static final int MSG = 2;

	Server server;
	Socket cs;
	String name;
	String password;
	boolean running;
	PrintStream out;
	BufferedReader in;

	public ClientConnection(Server server, Socket cs) throws IOException {
		this.server = server;
		this.cs = cs;
		this.in = new BufferedReader(new InputStreamReader(cs.getInputStream()));
		this.out = new PrintStream(cs.getOutputStream());
		this.running = false;
	}

	synchronized public void sendMsg(int type, String msg) {
		if (type == STATUS_OK)
			out.println("+OK," + msg);
		else if (type == STATUS_ERROR)
			out.println("+ERROR," + msg);
		else if (type == MSG)
			out.println("+MSG," + msg);
	}

	@Override
	public void run() {
		running = true;
		try {
//			sendMsg(STATUS_OK, server.toString());

			while (running) {

				LOG.info(this.cs.getRemoteSocketAddress().toString() + ": Waiting for input from client");
				String msg = in.readLine();
							
				if(msg == null) { // client closed the connection
					break;
				}
				
				// String[] tokens = msg.trim().split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
				String[] tokens = msg.split(",");

				
				StringBuilder sb1 = new StringBuilder();
								for (String t : tokens) {
					sb1.append(t);
					sb1.append(',');
				}
				
				if(sb1.length()>0)
					sb1.deleteCharAt(sb1.length()-1); // remove last comma
				LOG.info(this.cs.getRemoteSocketAddress().toString() + ": read :" + sb1.toString());

				// check if there's a valid command
				if (tokens.length == 0) {
					sendMsg(STATUS_ERROR, "No command given");
				} else {

					String cmd = tokens[0].trim();

					
					// handle BYE
					if ("BYE".equalsIgnoreCase(cmd)) {
						sendMsg(STATUS_OK, "BYE");
						break;
					}
					// handle VERSION
					else if ("VERSION".equalsIgnoreCase(cmd)) {
						sendMsg(STATUS_OK, "VERSION,"+server.toString());
					}

					// handle USERNAME
					else if ("USERNAME".equalsIgnoreCase(cmd)) {

						if (this.name != null) {
							sendMsg(STATUS_ERROR, "USERNAME: already set");
						}

						else if (tokens.length < 2) {
							sendMsg(STATUS_ERROR, "USERNAME: not specified");

						} 
						else if(tokens[1].startsWith("@")) {
							sendMsg(STATUS_ERROR, "USERNAME: cannot start with @");							
						} else

							try {
								server.addClient(tokens[1], this);
								this.name = tokens[1];
								sendMsg(STATUS_OK, "USERNAME," + this.name);
							} catch (ClientNameException e) {
								sendMsg(STATUS_ERROR, "USERNAME,"+ tokens[1] +": already in use");
							}

					}

					// handle PASSWORD
					else if ("PASSWORD".equalsIgnoreCase(cmd)) {												
						if (tokens.length < 2) {
							sendMsg(STATUS_ERROR, "PASSWORD: not specified");
						} 
						else{
							try {
								server.addPassword(tokens[1], this);
								this.password = tokens[1];
								sendMsg(STATUS_OK, "Create successfully");
							} catch (Exception e) {
								sendMsg(STATUS_ERROR, "Error with password");
							}
						}
					}
					
					//handle GETPROFILE
					else if ("GETPROFILE".equalsIgnoreCase(cmd)) {												
						if (tokens.length < 2) {
							sendMsg(STATUS_ERROR, "GETPROFILE: username not specified");
						} 
						else{
							try {
								String profile = server.getProfile(tokens[1], this);
								if (profile.equals("empty")) {
									sendMsg(STATUS_ERROR, "Profile is empty");
								}
								else {
									sendMsg(STATUS_OK, "Get profile successfully" + profile);
									System.out.println("Send profile to client successfully");
								}								
							} catch (Exception e) {
								sendMsg(STATUS_ERROR, "Error with GETPROFILE");
							}
						}
					}
					
					//handle SIGNIN
					else if ("SIGNIN".equalsIgnoreCase(cmd)) {												
						if (tokens.length < 2) {
							sendMsg(STATUS_ERROR, "SIGNIN: username not specified");
						} 
						else{
							try {
								String getUsername;
								getUsername = server.signIn(tokens[1], this);
								if (getUsername.equals("empty")) {
									sendMsg(STATUS_ERROR, "Username doesn't exist");
								}
								else if(getUsername.equals(tokens[1])) {
									server.getClient(tokens[1], this);
									this.name = tokens[1];
									sendMsg(STATUS_OK, "Username is OK");
								}

							} catch (Exception e) {
								sendMsg(STATUS_ERROR, "Error with SIGNIN");
							}
						}
					}
					
					//handle CHECKPASSWORD
					else if ("CHECKPASSWORD".equalsIgnoreCase(cmd)) {												
						if (tokens.length < 2) {
							sendMsg(STATUS_ERROR, "CHECKPASSWORD: password not specified");
							server.removeClient(this);
						} 
						else{
							try {
								String getPassword;
								getPassword = server.getPassword(tokens[1], this);
								if (getPassword.equals("empty")) {
									sendMsg(STATUS_ERROR, "Password doesn't exist");
									server.removeClient(this);
								}
								else if(getPassword.equals(tokens[1])) {
									sendMsg(STATUS_OK, "Log in successfully");
								}
								else {
									sendMsg(STATUS_ERROR, "Password isn't correct");
									server.removeClient(this);
								}

							} catch (Exception e) {
								sendMsg(STATUS_ERROR, "Error with CHECKPASSWORD");
								server.removeClient(this);
							}
						}
					}
					
					//handle GETALLMEDAL
					else if ("GETALLMEDAL".equalsIgnoreCase(cmd)) {												
										
						try {
							String getAllMedal;
							getAllMedal = server.getAllMedal(this);
							if (getAllMedal.equals("empty")) {
								sendMsg(STATUS_ERROR, "Error with server, can't find all medal");
							}
							else {
								sendMsg(STATUS_OK, "Get all medal successfully" + getAllMedal);
							}

						} catch (Exception e) {
							sendMsg(STATUS_ERROR, "Error with GETALLMEDAL");
							server.removeClient(this);
						}
						
					}
					
					//handle COLLECTMEDAL
					else if ("COLLECTMEDAL".equalsIgnoreCase(cmd)) {												
										
						try {
							
							server.collectMedal(tokens[1],this);
							sendMsg(STATUS_OK, "Collect medal successfully");

						} catch (Exception e) {
							sendMsg(STATUS_ERROR, "Error with COLLECTMEDAL");
							server.removeClient(this);
						}
						
					}
					
					//handle GETMYCOLLECT
					else if ("GETMYCOLLECT".equalsIgnoreCase(cmd)) {	
						if (tokens.length < 2) {
							sendMsg(STATUS_ERROR, "CHECKPASSWORD: username not specified");
							server.removeClient(this);
						} else {
							
							try {
								String getMyCollect;
								getMyCollect = server.getMyCollect(tokens[1], this);
								if (getMyCollect.equals("empty")) {
									sendMsg(STATUS_ERROR, "Error with server, can't find your collect");
								}
								else {
									sendMsg(STATUS_OK, "Get your collect successfully" + getMyCollect);
								}

							} catch (Exception e) {
								sendMsg(STATUS_ERROR, "Error with GETMYCOLLECT");
								server.removeClient(this);
							}
						}									
					}

					// handle QUIT
					else if ("QUIT".equalsIgnoreCase(cmd)) {
						if (tokens.length < 2) {
							sendMsg(STATUS_ERROR, "QUIT: no group given");
						}

						else {
							String groupName = tokens[1];

							try {
								server.quitGroup(groupName, this);
								sendMsg(STATUS_OK, "QUIT," + groupName);
							} catch (NoSuchGroupException e) {
								sendMsg(STATUS_ERROR, "QUIT," + groupName
										+ ": group does not exist");
							} catch (NonMemberException e) {
								sendMsg(STATUS_ERROR, "QUIT," + groupName
										+ ": client is not a member");
							}

						}
					}

					// handle MSG
					else if ("MSG".equalsIgnoreCase(cmd)) {

						if (this.name == null) {
							sendMsg(STATUS_ERROR, "MSG: name not set");
						}

						else if (tokens.length < 2) {
							sendMsg(STATUS_ERROR, "MSG: no address given");
						}

						else if (tokens.length < 3) {
							sendMsg(STATUS_ERROR, "MSG: message body empty");
						}

						else {
							String address = tokens[1];
							
							StringBuilder sb = new StringBuilder();
							for(int i=2; i<tokens.length; i++) {
								sb.append(tokens[i]);
								sb.append(',');
							}
							
							if(sb.length()>0)
								sb.deleteCharAt(sb.length()-1);

							String body = sb.toString();

							HashSet<ClientConnection> dsts = new HashSet<ClientConnection>();
							Group g = server.getGroupByName(address);

							if (g != null) {
								LOG.info("Found group " + address + ": "
										+ g.toString());
								synchronized(g.members) {
									dsts.addAll(g.members);
								}
							} else {
								LOG.info("Group " + address + " not found");
							}

							ClientConnection client = server
									.getClientByName(address);
							if (client != null)
								dsts.add(client);

							dsts.remove(this);

							if (!dsts.isEmpty()) {
								int cnt = 0;
								for (ClientConnection c : dsts) {
										c.sendMsg(MSG, this.name + ","
											+ address + "," + body);
										
										if(!c.out.checkError()) {
											cnt++;
										} else {
											// error writing socket output stream: close it
											c.running = false;
											c.out.close();
											// this will implicitly remove the client and its singleton groups
										}
								}
								sendMsg(STATUS_OK,
										"MSG,"+address+","+body+": " + cnt
												+ " client(s) notified");
							} else {
								sendMsg(STATUS_ERROR, "MSG,"+address+","+body+": no recipients found");
							}

						}
					}

					else {
						sendMsg(STATUS_ERROR, "Invalid command (" + cmd+")");
					}
				}
			}

		} catch (IOException e) {
			if(running) {
				// only print error if the client was supposed to be running, i.e. it wasn't stopped explicitly
				e.printStackTrace();
			}
		} finally {
			// make sure client is removed from server's data structures
			server.removeClient(this);
			
			try {
				in.close();
			} catch (IOException e) {}
			out.close();
			try {
				cs.close();
			} catch (IOException e) {}
			LOG.info("Client connection terminated");
		}

	}
}
