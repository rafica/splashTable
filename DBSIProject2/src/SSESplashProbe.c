#include<stdio.h>
#include<math.h>
#include<string.h>

#include<xmmintrin.h>
#include<emmintrin.h>
#include<pmmintrin.h>
#include<tmmintrin.h>


/* Declare global parameters */

int B = 4, S, h;

int m1, m2;		// Two hash multipliers

int tabSize;

int hashTable[20][8];


/* Get the two hash table indexes from the register */

int* getIndexes(__m128i var) {
	
	uint32_t *val = (uint32_t*) &var;
	
	int *res = malloc(2 * sizeof(int));
	
	res[0] = val[0];
	res[1] = val[2];
	return res;
	
}



/* Function that taks a 128 bit register and performs the OR-ACROSS operation */

__m128i orAcross(__m128i var) {
	
	uint32_t *val = (uint32_t*) &var;
	
	__m128i v1 = _mm_set_epi32(0, 0, 0, val[0]);
	__m128i v2 = _mm_set_epi32(0, 0, 0, val[1]);
	__m128i v3 = _mm_set_epi32(0, 0, 0, val[2]);
	__m128i v4 = _mm_set_epi32(0, 0, 0, val[3]);
	
	__m128i v_temp1 = _mm_or_si128 (v1, v2);
	
	__m128i v_temp2 = _mm_or_si128 (v3, v4);
	
	__m128i final_payload_result = _mm_or_si128 (v_temp1, v_temp2);		// should contain final payload result if probe key is present, 0 otherwise
	
	
	return final_payload_result;
	
}

/* Store the payload in an int variable from the lowest 32 bits stored in the register */

int getPayloadFromReg(__m128i var) {
	
	uint32_t *val = (uint32_t*) &var;
	
	return val[0];
}


__m128i probeTable(int key) {
	
	
	__m128i probeKey = _mm_set_epi32(key, key, key, key);	// Load the probe key
	__m128i multipliers = _mm_set_epi32(0, m1, 0, m2);		// Looad the 2 hash multipliers
	__m128i hashVal = _mm_mul_epu32(probeKey, multipliers);
	
	__m128i maskHigher = _mm_set_epi32(0, 1, 0, 1);		// Mask to get the lower 32 bits of each hash value
	
	__m128i hashValLower = _mm_mul_epu32(hashVal, maskHigher);			// Get the lower 32 bits of each hash value
	
	
	int to_shift = 32 - log2(tabSize);				// 32 - log2(2^S/B)
	
	
	__m128i indexes = _mm_srli_epi32 (hashValLower, to_shift);	// Register indexes contains the slot index for each of the two hash values
	
	int *i = getIndexes(indexes);
	
	int ind1 = *(i);
	int ind2 = *(i+1);
	
	/* Load 4 keys and 4 payloads for each of the two slots into registers */
	
	__m128i allKeys1 = _mm_set_epi32(hashTable[ind1][0], hashTable[ind1][1], hashTable[ind1][2], hashTable[ind1][3]);
	
	__m128i allPayloads1 = _mm_set_epi32(hashTable[ind1][4], hashTable[ind1][5], hashTable[ind1][6], hashTable[ind1][7]);
	
	__m128i allKeys2 = _mm_set_epi32(hashTable[ind2][0], hashTable[ind2][1], hashTable[ind2][2], hashTable[ind2][3]);
	
	__m128i allPayloads2 = _mm_set_epi32(hashTable[ind2][4], hashTable[ind2][5], hashTable[ind2][6], hashTable[ind2][7]);
	
	
	__m128i res1 = _mm_cmpeq_epi32 (probeKey, allKeys1);	// compare probe key with all keys in slot 1
	
	
	__m128i resAnd1 = _mm_and_si128 (res1, allPayloads1);	
	
	
	__m128i res2 = _mm_cmpeq_epi32 (probeKey, allKeys2);	// compare probe key with all keys in slot 1
	
	
	__m128i resAnd2 = _mm_and_si128 (res2, allPayloads2);	
	
	
	__m128i finalRes = _mm_or_si128 (resAnd1, resAnd2);
	
	
	__m128i probeAns = orAcross(finalRes);
	
	
	return probeAns;
	
	
	
}


int main(int argc, char *argv[]) {
	
	if(argc != 4)
		printf("Please provide correct number of arguments (4)");
	
	char dumpfile[100];
	char probefile[100];
	char resultfile[100];
	
	strcpy(dumpfile, argv[1]);
	strcpy(probefile, argv[2]);
	strcpy(resultfile, argv[3]);
	
	/* Read the hash table from the dump file*/
	
	FILE* fp;
	char  line[255];
	
	fp = fopen(dumpfile , "r");
	
	int c = 0, c1 = 0;
	
	while (fgets(line, sizeof(line), fp) != NULL)
	{
		/* Get B, S and h values from line 1 */
		
		if(c == 0) {
			int i = 0;
			char *p;
			char *a[4];
			p = strtok (line," ");  
			while (p != NULL)
			{
				a[i++] = p;
				
				p = strtok (NULL, " ");
			}
			
			
			B = atoi(a[0]);
			S = atoi(a[1]);
			h = atoi(a[2]);
			
		}
		
		else if(c == 1){
			
			int i = 0;
			char *p;
			char *a[2];
			p = strtok (line," ");  
			while (p != NULL)
			{
				a[i++] = p;
				
				p = strtok (NULL, " ");
			}
			
			m1 = atoi(a[0]);
			m2 = atoi(a[1]);
			
		}
		
		else {
			
			int i = 0;
			char *p;
			char *a[8];		// 4 keys and 4 payloads
			p = strtok (line," ");  
			while (p != NULL)
			{
				a[i++] = p;
				
				p = strtok (NULL, " ");
			}
			
			
			for(int k = 0; k < 8; k++)
				hashTable[c1][k] = atoi(a[k]);
			c1++;
		}
		
		
		c++;
	}
	
	fclose(fp);	
	
	/* Initialize size of the table*/
	
	tabSize = pow(2,S)/B;
	
	/* Read the probe file */
	
	FILE* fp1;
	
	char line1[255];
	
	int probes[20];
	
	int numofProbes;
	
	int i = 0;
	
	fp1 = fopen(probefile , "r");
	
	while (fgets(line1, sizeof(line1), fp1) != NULL)
	{
		probes[i++] = atoi(line1);
		
	}
	
	fclose(fp1);	
	
	numofProbes = i;
	
	/* Probe the hash table using keys in the probe file */
	
	FILE* fp2;

	fp2 = fopen(resultfile , "w");
	
	for (int j = 0; j < numofProbes; j++) {

				
		int key = probes[j];	
		
		__m128i probeAns = probeTable(key);		// probe the table with the given key. m1 and m2 are the two hash multipliers
		
		int payload = getPayloadFromReg(probeAns);
		
		if(payload != 0)
			fprintf(fp2, "%d %d\n", key, payload);
			
				
	}
	
	fclose(fp2);	
		
	return 0;
}
