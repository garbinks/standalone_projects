package cs455.cdn.util;

import java.util.Scanner;
import cs455.cdn.node.MessageNode;

public class MessageNodeConsoleListener extends Thread {
	
	public void run(){
		System.out.println("Enter one of the following commands:\n'list-nodes', 'list-messages', 'list-message-content', or 'shutdown'");
		System.out.print("\n>>>");
		@SuppressWarnings("resource")
		Scanner consoleIn = new Scanner(System.in);
		while(consoleIn.hasNextLine()){
			String commandInput = consoleIn.nextLine();
			
			if(commandInput.equals("list-nodes")){
				System.out.println("      ADDRESS:PORT\n      ------------");
				for(String node:MessageNode.adjustedNodeList){
					System.out.println(">>> " + node);
				}
				System.out.println();
			} else if(commandInput.equals("list-messages")){
				System.out.println("Number of messages sent: " + MessageNode.sendTracker +
						"\nNumber of messages received: " + MessageNode.recTracker);
				System.out.println();
			} else if(commandInput.equals("list-message-content")){
				System.out.println("Sent and received message summations...\n" +
						"Sent total: " + MessageNode.sendSum + "\nReceived total: " + MessageNode.recSum);
				System.out.println();
			} else if(commandInput.equals("shutdown")){
				System.out.println("Message node shutting down.");
				System.exit(1);
				
			} else {
				System.out.println("Invalid command!" +
						"\nValid commands for messaging nodes are: 'list-nodes', 'list-messages'," +
						" 'list-message-content', and 'shutdown'.");
			}
			System.out.println("Enter one of the following commands:\n'list-nodes', 'list-messages', 'list-message-content', or 'shutdown'");
			System.out.print("\n>>>");
		}
	}
}