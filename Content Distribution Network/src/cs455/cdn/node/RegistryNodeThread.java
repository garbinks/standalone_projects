package cs455.cdn.node;

import java.io.*;
import java.net.*;

@SuppressWarnings("unused")
public class RegistryNodeThread extends Thread {
	public static Socket messageNodeConnection;

	RegistryNodeThread(Socket s) throws IOException {
		messageNodeConnection = s;
		RegistryNode.numOfThreads++;
	}
	
	public void run(){
		try {
			DataInputStream fromMessageNode = new DataInputStream(messageNodeConnection.getInputStream());
			DataOutputStream toMessageNode = new DataOutputStream(messageNodeConnection.getOutputStream());
			System.out.println("\n\nConnection established with node...");
			
			// '0' -- REGISTER_REQUEST (int type, Socket s)
			// '2' -- DEREGISTER_REQUEST  (type, Socket s)
			// '3' -- TASK_COMPLETE (int type, Socket s)
			// '7' -- TRAFFIC_SUMMARY (int type, Socket s, int messagesSent, int messagesReceived, long recSummation)
			
			// '1' -- REGISTER_RESPONSE  (int type, byte success, String additionalInfo)
			// '4' -- MESSAGING_NODES_LIST (int type, int numberOfNodes, String nodeList))
			// '5' -- MESSAGE_NODE_PAYLOAD (int type, Socket s, int payload)
			// '6' -- PULL_TRAFFIC_SUMMARY (int type)
			// '8' -- START_NODES (int type, Socket s)
			// '9' -- ALL_NODES_COMPLETE (int type, Socket s)
			
			// '10' -- END_OF_BURST_MESSAGE (int type)
			
			while(messageNodeConnection.isConnected()){
				int messageType = fromMessageNode.readInt();
				String nodeAddress = fromMessageNode.readUTF();
				int sentTotal = 0;
				int recTotal = 0;
				long sumSent = 0;
				long sumRec = 0;
				
				switch(messageType){
				case 0:
					System.out.println("Received a REGISTER_REQUEST from " + nodeAddress);
					RegistryNode.addNodeToList(nodeAddress);
					toMessageNode.writeInt(1); // REGISTER_REQUEST_RESPONSE type
					toMessageNode.writeInt(1); // 1 == success, 0 == fail
					break;
				case 2:
					System.out.println("Received a DEREGISTER_REQUEST from " + nodeAddress);
					RegistryNode.removeNodeFromList(nodeAddress);
					break;
				case 3:
					System.out.println("Received a TASK_COMPLETE from " + nodeAddress);
					RegistryNode.nodesComplete ++;
					System.out.println("The number of nodes complete is now " + RegistryNode.nodesComplete + "!");
					break;
	
				case 7:
					System.out.println("Received a TRAFFIC_SUMMARY from " + nodeAddress);
					
					break;

				default:
					System.out.println("Could not properly identify the message!");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("RegistryNodeThread #" + RegistryNode.numOfThreads + " has exited...");
		RegistryNode.numOfThreads--;
	}	
}