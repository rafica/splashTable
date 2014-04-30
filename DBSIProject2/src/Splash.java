import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/*
 * h hash multipliers
 * The number of hash buckets is 2^S/B and size of each bucket is B
 */

public class Splash {
	
    private static int B, R, S, h; 
    private static String inputFile, dumpFile, probeFile, resultFile;
    private static int hashMultipliers[];
    private static int hashTable[][]; // Rows = 2^S/B; Columns = 2B[key + payload]
    private static int num_inserted = 0;
    private static int oldest[];
    
	public static void main(String args[]) {
		// Accept command line args
		if (args.length < 6)
	    {
	      System.out.println("Pass B R S h  inputfile dumpfile probefile resultfile");
	      System.exit(0);
	    }
		int param = 0;
		try{
		B = Integer.parseInt(args[param++]);//0
		R = Integer.parseInt(args[param++]);//1
		S = Integer.parseInt(args[param++]);//2
		h = Integer.parseInt(args[param++]);//3
		inputFile = args[param++];//4
		dumpFile ="";
		if(args[param].equals("-dumpfile")){//5
			param++;
			dumpFile = args[param++];//6
			if (args.length!=9){		
				System.out.println("Pass B R S h  inputfile dumpfile probefile resultfile");
				System.exit(0);	
			}
		}
		probeFile = args[param++]; //5
		resultFile = args[param]; //6
		hashMultipliers = new int[h];
		
		//Get hash multipliers
		getHashMultipliers();
		
		//Initialize table with zeroes
		init();
		
		//Getting inputs for inserting
		String line;
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		while ((line = br.readLine()) != null) {
			String[] parts = line.split(" ");
			if(parts.length==2){
				int key = Integer.parseInt(parts[0]); 
				int payload = Integer.parseInt(parts[1]);
				if(key==0)
					continue;
				insert(key, payload);
				}
			
			}
		br.close();

		//Getting inputs for probing
		int payload;	
		br = new BufferedReader(new FileReader(probeFile));
		PrintWriter writer = new PrintWriter(resultFile, "UTF-8");
		while ((line = br.readLine()) != null) {
				int key = Integer.parseInt(line.replaceAll("\\s", "")); 
				if(key==0) //If key is zero ignore
					continue;
				payload = probe(key);
				if(payload==0) // If payload is zero ignore
					continue;	
				writer.println(key+" "+payload);	
			}	
		writer.close();
		br.close();
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
		
		// Dump table to dump.txt
		if(dumpFile != "")
			dump();
		//print2DArray(hashTable);	 
	}
	
	private static void getHashMultipliers() {
		// populate hashMultipliers
		for(int i = 0; i < h; i++) {
			while(true) {
				int min = 0;
				int max = (int)Math.pow(2, 32) - 1;
				if (max % 2 == 0) --max;
				if (min % 2 == 0) ++min;
				int random_no = min + 2*(int)(Math.random()*((max-min)/2+1));
				if(notExists(hashMultipliers,random_no)) {
					hashMultipliers[i] = random_no;		
					break;
				}
			}
		//System.out.println("Random no. : " + Random_No);
		}
		//print1DArray(hashMultipliers);
	}
	
	private static void init() {
		/* Initialize data structures[hashTable] here*/
		int numBuckets = (int)(Math.pow(2,S)/B);
		hashTable = new int[numBuckets][2*B];
		for(int i = 0; i < numBuckets; i++)
			for(int j = 0; j < 2*B; j++)
				hashTable[i][j] = 0;
		oldest = new int[numBuckets];
		for(int i = 0; i < numBuckets; i++)
			oldest[i] = 0;
	}
	
	private static void insert(int key, int payload) {
		/* Insert key and payload into the hash table*/
		if(keyPresent(key))
			return;
		boolean inserted = false;
		for(int i = 0; i < hashMultipliers.length; i++) {
			int index = getIndex(hashMultipliers[i], key); 
			for(int k = 0; k < 2*B; k = k+2) {
				if(hashTable[index][k] == 0) {
					hashTable[index][k] = key;
					hashTable[index][k + 1] = payload;
					inserted = true;
					break;
				}
		}
		if(inserted) {
			num_inserted++;
			break;
		 	}
		}
		if(!inserted){	
			/*Found no place in the first iteration!..now continue reinserting <= R times */
			
			int kickedout[] = {key,payload};
			for(int i = 0; i < R; i++) {	
				kickedout = reinsert(kickedout[0],kickedout[1]);
				if(kickedout[0] == 0 && kickedout[1] == 0) {
					inserted = true;
					break; // Done!..everything inserted 
				}		
			}
			if(inserted) 
			   num_inserted++;
			// Couldn't reinsert even after R tries
			else {
				System.out.println("Cant insert!");
				dump();
				System.exit(0);
			}
		}
	}
	
	
	
	private static int probe(int key) {
		int payloadIndex=0;
		int bucketIndex =0;
		//System.out.println("in probe");
		int count[] = new int[hashTable.length];
		for(int i = 0; i < hashMultipliers.length; i++) {
			int val = hashMultipliers[i] * key; //key
			long hashVal = (long)(val % Math.pow(2,32));
			long temp = hashVal & (long)(Math.pow(2,32) - 1);
			int shiftBits = getShiftBits((int)(Math.pow(2,S)/B - 1));
			int finalShiftBits = 32 - shiftBits;
			long newHashVal = temp>>finalShiftBits; /* ??? How many bits to shift ??? */
			int index = (int)newHashVal; 
			count[index] = count[index] + 1;
			int payloadIndexTemp = 0;
			for(int k = 0; k < 2*B; k = k+2) {
				int c = (key==hashTable[index][k])?1:0;
				payloadIndexTemp+=c*(k+1);	
			}
			int d =(payloadIndexTemp!=0)?1:0;
			payloadIndex+= payloadIndexTemp*(d/count[index]);
			bucketIndex = bucketIndex + (d/count[index])*index;	
		}
		//System.out.println("probe: "+bucketIndex+","+payloadIndex);
		int payloadFlag = payloadIndex!=0? 1:0;
		return payloadFlag * hashTable[bucketIndex][payloadIndex];
	}
	
	private static void dump() {
		
		String content = new String();
		content += B + " " + S + " " + h + " " + num_inserted + "\n";
		for(int i = 0; i < hashMultipliers.length; i++) 
			content += hashMultipliers[i] + " ";
		content += "\n";
		for(int i = 0; i < hashTable.length; i++) {
			for(int j = 0; j < hashTable[0].length; j=j+2)
				content += hashTable[i][j] + " ";
			for(int j = 1; j < hashTable[0].length; j=j+2)
				content += hashTable[i][j] + " ";
		content += "\n";
		}
		try{
			File file = new File(dumpFile);
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
		}
		catch(IOException e) {	
			System.out.println("Exception : " + e);
		}
	}
	
	private static boolean notExists(int a[],int num) {
		
		for(int i = 0; i < a.length; i++) {
			
			if(num == a[i])
				return false;
		}
		
		return true;
	}
	
	
	private static int getIndex(int hashMult, int key) {	
		int val = hashMult * key;
		long hashVal = (long)(val % Math.pow(2,32));
		long temp = hashVal & (long)(Math.pow(2,32) - 1);	
		int shiftBits = getShiftBits((int)(Math.pow(2,S)/B - 1));		
		int finalShiftBits = 32 - shiftBits;
		long newHashVal = temp>>finalShiftBits;
		int index = (int)newHashVal; 
		return index;
	}
	
	private static boolean keyPresent(int key) {
		boolean present  = false;
		for(int i = 0; i < hashMultipliers.length; i++) {
			int index = getIndex(hashMultipliers[i], key); 
			for(int j = 0; j < 2*B; j = j + 2) {
				if(hashTable[index][j] == key) {
					present = true;
				}
			}
			if(present)
				break;
		}
		return present;
	}
	
	
	private static int getShiftBits(int num) {
		int i = 0, power = 0;
		while(power < num) {
			power = (int)Math.pow(2,i);
			i++;
		}
		if(power == num) 
			return i;
		else
			return i - 1;
	}
	
	private static int[] reinsert(int key, int payload) {	
		System.out.println("Entered reinsertion loop!!");
		boolean inserted = false;
		for(int i = 0; i < hashMultipliers.length; i++) {
			int index = getIndex(hashMultipliers[i], key); 
			for(int k = 0; k < 2*B; k = k+2) {
			if(hashTable[index][k] == 0) {
				hashTable[index][k] = key;
				hashTable[index][k + 1] = payload;
				inserted = true;
				break;
			}
		}	
		if(inserted) {	
			int no_reinsert[] = {0,0};
			return no_reinsert;
		}
		}
		
		//kick out some element[oldest] and insert this one..return the kicked out element to reinsert it
		int index = getIndex(hashMultipliers[0], key); 	// No place to insert, just use one (first) hash function for now..and kick out oldest element in that bucket
		int to_reinsert[] = {hashTable[index][oldest[index]],hashTable[index][oldest[index] + 1]}; 
		hashTable[index][oldest[index]] = key;
		hashTable[index][oldest[index] + 1] = payload;
		if(oldest[index] != 2*B - 2)
			oldest[index] += 2;
		else
			oldest[index] = 0;
		return to_reinsert;
		
		
	}
	
	
	private static void print1DArray(int a[]) {
		
		for(int i = 0; i < a.length; i++) {
			//System.out.print(a[i] + " ");
		}
	}
	
	/*private static void print2DArray(int a[][]) {
		
		for(int i = 0; i < a.length; i++) {
			for(int j = 0; j < a[0].length; j++)
				//System.out.print(a[i][j] + " ");
			
		//System.out.println();
		}
	}*/

}
