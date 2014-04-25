import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

// some changes!!..:)
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
	
    static int B, R, S, h; 
    static int hashMultipliers[];
    static int hashTable[][]; // Rows = 2^S/B; Columns = 2B[key + payload]
    static int num_inserted = 0;
    
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
		
		print1DArray(hashMultipliers);
		
		
	}
	
	private static void init() {
		/* Initialize data structures[hashTable] here*/
		
		int numBuckets = (int)(Math.pow(2,S)/B);
		hashTable = new int[numBuckets][2*B];
		
		for(int i = 0; i < numBuckets; i++)
			for(int j = 0; j < 2*B; j++)
				hashTable[i][j] = 0;
		
		//print2DArray(hashTable);
		
	}
	
	private static void insert(int key, int payload) {
		/* Insert key and payload into the hash table*/
		
		boolean inserted = false;
		
		for(int i = 0; i < hashMultipliers.length; i++) {
		
		int val = hashMultipliers[i] * key; //key
		
		long multiplier = (long)(val % Math.pow(2,32));
		
		
		long temp = multiplier & (long)(Math.pow(2,32) - 1);
		
		//System.out.println("Multiplier : " + multiplier);
		
		System.out.println("Temp : " + temp);
		
		int shiftBits = getShiftBits((int)(Math.pow(2,S)/B - 1));
			
		System.out.println("Test" + shiftBits);
		
		int finalShiftBits = 32 - shiftBits;
		
		System.out.println("Test1 " + finalShiftBits);
		
		
		multiplier = temp>>finalShiftBits; /* ??? How many bits to shift ??? */
		
		
		if(multiplier > (int)(Math.pow(2,S)/B - 1))
			multiplier = (int)(Math.pow(2,S)/B - 1);
		
		int index = (int)multiplier; 
		
		//int index = (int)((int)(Math.pow(2,S)/B) * temp);
		
		for(int k = 0; k < 2*B; k = k+2) {
			
			if(hashTable[index][k] == 0) {
				
				hashTable[index][k] = key;
				hashTable[index][k + 1] = payload;
				inserted = true;
				break;
			}
		}
		
		System.out.println("Final Index : " + index);
		
		if(inserted) {
			num_inserted++;
			break;
		}
		}
		
		if(!inserted){
			
			/*Found no place in the first iteration!..now continue reinserting <= R times */
			
			
		}
		
		}
	
	
	private static int probe(int key) {
		return 0;
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
