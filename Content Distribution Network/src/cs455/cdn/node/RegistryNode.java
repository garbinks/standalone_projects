package cs455.cdn.node;

import java.io.*;
import java.net.*;
import java.util.*;

public class RegistryNode {	
	public static int port = 0;
	public static int numberOfNodes = 0;
	public static int numOfThreads = 0;
	public static int nodesComplete = 0;
	public static String nodeListOut;
	public static ArrayList<String> nodeList = new ArrayList<String>();
	public static InetAddress ipAddr = null;
	public static String hostname = null;
	public static boolean sendStart;
	
	public static int totalSent = 0;
	public static int totalRec = 0;
	public static long totalSentSum = 0;
	public static long totalRecSum = 0;

	private void runRegistry() throws IOException {
		ServerSocket registrySocket = new ServerSocket(port);
		registrySocket.getInetAddress();
		System.out.println("Registry is up and ready for connections" +
				" on " + InetAddress.getLocalHost() + ":" + port);
		new cs455.cdn.util.RegistryConsoleListener().start();
		while (true) {
			Socket nodeConnection = registrySocket.accept();
			new cs455.cdn.node.RegistryNodeThread(nodeConnection).start();
		}
	}
	
	public static synchronized void addNodeToList(String nodeToAdd){	
		boolean alreadyAdded = false;
		String formattedNode = nodeToAdd;
		for(String node:nodeList){
			if(node == nodeToAdd){
				alreadyAdded = true;
			}
		} 
		if(!alreadyAdded){
			nodeListOut += (formattedNode + ";");
			nodeList.add(formattedNode);
			numberOfNodes ++;
			System.out.println("Node successfully added to nodeList!" +
					"\nNumber of nodes is now: " + nodeList.size() +"\n");
		} else if(alreadyAdded){
			System.out.println("Node was already added!");
		}
	}
	
	public static synchronized void startNodeRounds() throws NumberFormatException, UnknownHostException, IOException {
		DataOutputStream toNode = null;
		Socket start = null;
		nodeListOut = nodeListOut.substring(4);
		for(String node:nodeList){
			String[] temp = node.split(":");
			try{
				start = new Socket(temp[0], Integer.parseInt(temp[1]));
				System.out.println("Socket to node opened: " + node);
			}catch(Exception e){
				System.out.println("Wasn't able to open connection to node...");
				e.printStackTrace();
			}
			if(start.isConnected()){
				toNode = new DataOutputStream(start.getOutputStream());
				toNode.writeInt(8);
				toNode.writeUTF(nodeListOut);
				toNode.flush();
				System.out.println("Sent node list " + nodeListOut + " to node: " + node);
				start.close();
			}
		}	
	}
	
	public static synchronized void removeNodeFromList(String nodeToRemove){
		for(String node:nodeList){
			if(node == nodeToRemove){
				nodeList.remove(node);
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		port = Integer.parseInt(args[0]);
		new RegistryNode().runRegistry();
	}
}
