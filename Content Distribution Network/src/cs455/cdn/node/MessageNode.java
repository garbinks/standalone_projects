package cs455.cdn.node;

//import cs455.roulette.exchange.MessageSend;
import java.io.*;
import java.net.*;
import java.util.*;

public class MessageNode {

    public static Socket registrationSocket = null; // outgoing socket only for registration purposes; closed once registration is complete
    public static Socket regListen1 = null;
    public static ServerSocket listenServer = null; // listening socket for incoming Registry connections/messages

	public static int registryPort;
	public static int listeningPort;
	public static String localAddress; 
	public static String registryHostname;
    public static boolean receivedNodeList = false;
    public static boolean allNodesComplete = false;
	public static boolean successfulRegistration = false;
	public static boolean waitForStart = false;
	public static int start = 0;

    public static int numberOfNodes = 0;
	public static ArrayList<String> adjustedNodeList = new ArrayList<String>();
	public static String incomingNodeList = null;
	
	public static int sendTracker = 0;
	public static int recTracker = 0;
	public static int sendSum = 0;
	public static int recSum = 0;
	public static int currentRound;
	public static int numOfRounds = 50;
	public static int burst;
	
    public static DataOutputStream outToReg = null;
    public static DataInputStream inFromReg = null;
    public static Random randomNode = null;
    public static Random randomInt = new Random();

	void runMessageNode() throws IOException {
		ServerSocket messageNodeSocket = new ServerSocket(listeningPort);
		System.out.println("This MessageNode ready for peer connections on " + registryHostname + ":" + listeningPort + "...");
		while (true) {
			try{
			Socket connection = messageNodeSocket.accept();
			new cs455.cdn.node.MessageNodeThread(connection).start();
			} catch (Exception e){
				System.out.println("Not able to start accepting incoming connections yet.");
			}
		}
	}

	public static synchronized void updateReceived(int in){
		recSum += in;
		recTracker++;
	}
	
	public static synchronized void fireAtWill() throws IOException {
		currentRound = 0;
		numberOfNodes = adjustedNodeList.size();
		System.out.println("Number of nodes: " + numberOfNodes);
		while (currentRound < numOfRounds) {
			int tempRand = randomNode.nextInt();
			System.out.println("Random node selected is [" + tempRand + "] --> " + adjustedNodeList.get(tempRand));
			String[] peer = null;
			peer = (adjustedNodeList.get(tempRand).split(":"));
			Socket peerConnection = new Socket(peer[0], Integer.parseInt(peer[1]));
			burstFive(peerConnection);
			currentRound++;
			peerConnection.close();
		}
		outToReg.writeInt(3);
		outToReg.flush();
	}

	public static synchronized void burstFive(Socket peer) throws IOException {
		DataOutputStream toPeer = new DataOutputStream(peer.getOutputStream());
		burst = 0;
		int rand = 0;
		while (burst < 5) {
			rand = randomInt.nextInt();
			toPeer.writeInt(5);
			toPeer.writeInt(rand);
			toPeer.flush();
			burst++;
			sendSum += rand;
			sendTracker++;
		}
		toPeer.writeInt(10);
		toPeer.flush();
		toPeer.close();
	}

	public static void main(String[] args) throws IOException {
		registryHostname = args[0];
		registryPort = Integer.parseInt(args[1]);

		try{
			registrationSocket = new Socket(registryHostname, registryPort); 
		} catch (Exception e) {
			System.out.println("No Registry available! Please make sure a Registry is running! " +
					"\nMessageNode shutting down...");
			System.exit(1);
		}
		
        outToReg = new DataOutputStream(registrationSocket.getOutputStream()); 
        inFromReg = new DataInputStream(registrationSocket.getInputStream()); 
		
		if(registrationSocket.isConnected()){ 
			listeningPort = registrationSocket.getLocalPort() + 30;
			System.out.println("Message node is up... found RegistryNode.");
			localAddress = (registrationSocket.getLocalAddress().toString() +":"+ listeningPort).substring(1);
			outToReg.writeInt(0);
			outToReg.writeUTF(localAddress);
			outToReg.flush();
		} 
		else {
			System.err.println("Connection to registry was not possible! Check hostname and port number of the registry!");
			System.exit(1);
		}
				
		while(registrationSocket.isConnected()){
			int messageType = inFromReg.readInt();
			int success = inFromReg.readInt();
			if(messageType == 1){
				if(success == 1){
					System.out.println("Received REGISTER_RESPONSE from Registry!");
					successfulRegistration = true;
					break;
				} else {
					System.out.println("Registration was not successful!");
				}
			}
		}
		
		new cs455.cdn.util.MessageNodeConsoleListener().start();
		new MessageNode().runMessageNode();
		
		outToReg.writeInt(2);
		outToReg.writeUTF(localAddress);
		outToReg.flush();
		System.out.println("This node has successfully deregistered! Node will now exit!\nBye-bye!");
		outToReg.close();
		inFromReg.close();
		System.exit(1);
	}
}