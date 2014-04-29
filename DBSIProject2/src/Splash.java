import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/*
 * INSERTION
 * 1) Have h hash multipliers
 * 2) The number of hash buckets is 2^S/B ... and size of each bucket is B
 * 3) Create the data structure with appropriate sizes [can use arrays of user defined data structure]
 * 4) Read inputs from the input file and do multiplicative hashing for each input
 * 5) Put it in the first bucket that is empty..if not empty do (up to R) reinsertions 
 * 
 * PROBING
 * ****** No ifs or while loops allowed here!!
 * 1) Compute h hash functions for the key
 * 2) Probe buckets each of the computed value and in each bucket, check all B keys.
 * 3 If match, return corresponding payload*/

public class Splash {
	
    private static int B, R, S, h; 
    private static int hashMultipliers[];
    private static int hashTable[][]; // Rows = 2^S/B; Columns = 2B[key + payload]
    private static int num_inserted = 0;
    private static int oldest[];
    
	public static void main(String args[]) {
		
		// Accept command line args here and call appropriate functions
		// Instantiate hashMultipliers and hashTable
		B = 2;
		h = 5;
		S = 5;
		
		hashMultipliers = new int[5];
		
		getHashMultipliers();
		init();
		insert(25, 3323);
		insert(21, 123);
		insert(12, 15);
		insert(5,55);
		
		System.out.println(probe(12));
		System.out.println(probe(25));
		
		dump();
		
		print2DArray(hashTable);
		 
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
		
		//print2DArray(hashTable);
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
		
		//int index = (int)((int)(Math.pow(2,S)/B) * temp);
		
		for(int k = 0; k < 2*B; k = k+2) {
			
			if(hashTable[index][k] == 0) {
				
				hashTable[index][k] = key;
				hashTable[index][k + 1] = payload;
				inserted = true;
				break;
			}
		}
		
		//System.out.println("Final Index : " + index);
		
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
		
			//System.out.println("Multiplier : " + multiplier);
		
			
			int shiftBits = getShiftBits((int)(Math.pow(2,S)/B - 1));
			
			int finalShiftBits = 32 - shiftBits;
			
			long newHashVal = temp>>finalShiftBits; /* ??? How many bits to shift ??? */
		
			int flag = (newHashVal > (int)(Math.pow(2, S)/B - 1))?1:0;
			
			newHashVal = flag * (int)(Math.pow(2, S)/B-1) + ((flag==1)?0:1) * newHashVal;
			
			int index = (int)newHashVal; 
			
			count[index] = count[index] + 1;
			//System.out.println(index);
			int payloadIndexTemp = 0;
			for(int k = 0; k < 2*B; k = k+2) {
				int c = (key==hashTable[index][k])?1:0;
				payloadIndexTemp+=c*(k+1);	
			}
			int d =(payloadIndexTemp!=0)?1:0;
			payloadIndex+= payloadIndexTemp*(d/count[index]);
			bucketIndex = bucketIndex + (d/count[index])*index;
			
			
			//System.out.println(bucketIndex+","+payloadIndex);
			
		}
		System.out.println(bucketIndex+","+payloadIndex);
		//return 0;
		
		return payloadIndex & hashTable[bucketIndex][payloadIndex];

	}
	
	private static void dump() {
		
		try {
			
		String content = new String();
		
		content += B + " " + S + " " + h + " " + num_inserted + "\n";
		
		for(int i = 0; i < hashMultipliers.length; i++) 
			content += hashMultipliers[i] + " ";
		
		content += "\n";
		
		for(int i = 0; i < hashTable.length; i++) {
			for(int j = 0; j < hashTable[0].length; j++)
				content += hashTable[i][j] + " ";
			
		content += "\n";
		}
		
		 
		File file = new File("dump.txt");

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
		
		int val = hashMult * key; //key
		
		long hashVal = (long)(val % Math.pow(2,32));
		
		
		long temp = hashVal & (long)(Math.pow(2,32) - 1);
		
		//System.out.println("Multiplier : " + multiplier);
		
		//System.out.println("Temp : " + temp);
		
		int shiftBits = getShiftBits((int)(Math.pow(2,S)/B - 1));
			
		//System.out.println("Test" + shiftBits);
		
		int finalShiftBits = 32 - shiftBits;
		
		//System.out.println("Test1 " + finalShiftBits);
		
		
		long newHashVal = temp>>finalShiftBits; /* ??? How many bits to shift ??? */
		
		
		/*if(multiplier > (int)(Math.pow(2,S)/B - 1))
			multiplier = (int)(Math.pow(2,S)/B - 1);*/
		
		
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
		
		boolean inserted = false;
		
		for(int i = 0; i < hashMultipliers.length; i++) {
		
		int index = getIndex(hashMultipliers[i], key); 
		
		//int index = (int)((int)(Math.pow(2,S)/B) * temp);
		
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
			System.out.print(a[i] + " ");
		}
	}
	
	private static void print2DArray(int a[][]) {
		
		for(int i = 0; i < a.length; i++) {
			for(int j = 0; j < a[0].length; j++)
				System.out.print(a[i][j] + " ");
			
		System.out.println();
		}
	}

}
