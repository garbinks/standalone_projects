package cs455.cdn.util;

import java.util.Scanner;
import cs455.cdn.node.RegistryNode;

public class RegistryConsoleListener extends Thread {
	public void run(){
		System.out.println("\nEnter one of the following commands:\n'list-nodes'," +
				" 'start', or 'shutdown'\n\n...Or wait for nodes " +
				"to connect and register!");
		System.out.print("\n>>>");
		Scanner consoleIn = new Scanner(System.in);
		while(consoleIn.hasNextLine()){
			String commandInput = consoleIn.nextLine();
			if(commandInput.equals("list-nodes")){
				if(RegistryNode.nodeList.size() == 0) {
					System.out.println("No nodes registered at this time...");
				}else{
					System.out.println("Here is a list of currently registered" +
							" nodes...\n      ADDRESS:PORT\n      ------------");
					for(String node:RegistryNode.nodeList){
						System.out.println(">>> " + node);
					}
				}
				System.out.println("\n");
			} 
			
			else if(commandInput.equals("start")){
				if (RegistryNode.nodeList.size() < 2){
					System.out.println("\nStart command could not be sent. Please wait until there are two \nor" +
							" more nodes registered to issue the 'start' command.\n");
				} else{
					try {
						RegistryNode.startNodeRounds();
					} catch (Exception e) {
					}
				}
			} 
			
			else if(commandInput.equals("shutdown")){
				System.out.println("Registry node will now shutdown!");
				System.exit(1);
			} 
			
			else {
				System.out.println("\nInvalid command!\n");
				}
			System.out.println("Enter one of the following commands:\n'list-nodes'," +
					" 'start', or 'shutdown'\n-----------------------------------");
			System.out.print("\n>>>");
			}
		consoleIn.close();
	}
}
