package cs455.cdn.node;

import java.io.*;
import java.net.*;

public class MessageNodeThread extends Thread {
	public static Socket peerConnection;

	MessageNodeThread(Socket s) throws IOException {
		peerConnection = s;
	}
	
	public synchronized void run(){
		try {
			DataInputStream fromPeer = new DataInputStream(peerConnection.getInputStream());
			DataOutputStream toPeer = new DataOutputStream(peerConnection.getOutputStream());
			System.out.println("\n\nConnection established with peer...");
			
			// '5' -- MESSAGE_NODE_PAYLOAD (int type, Socket s, int payload)
			// '8' -- START_NODES (int type, Socket s)
			// '10' -- END_OF_BURST_MESSAGE (int type)

			while(peerConnection.isConnected()){
				int messageType = fromPeer.readInt();
				
				if(messageType == 5){
					int randInt = fromPeer.readInt();
					MessageNode.updateReceived(randInt);
				} else if(messageType == 8){
					MessageNode.incomingNodeList = fromPeer.readUTF();
					System.out.println("Received NODE_LIST message from Registry!\nNode list: " + MessageNode.incomingNodeList);
					String[] temp = MessageNode.incomingNodeList.split(";");
					for(int i = 0; i < temp.length; i++){
						if(!temp[i].equals(MessageNode.localAddress)){
							System.out.println("Added node: " +temp[i]);
							MessageNode.adjustedNodeList.add(temp[i]);
							MessageNode.numberOfNodes++;
						}
					}
					System.out.print("Node list with self removed: ");
					for(String node:MessageNode.adjustedNodeList){
						System.out.print(node + "; ");
					}
					
					MessageNode.fireAtWill();
					
				} else if(messageType == 10){
					peerConnection.close();
				} else {
					System.out.println("Could not properly identify the message!");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("MessageNodeThread has exited...");
	}
}