
public class Icache {
	
	/*
	 * associativity = 1 --> direct mapped
	 * associativity = size of the cache --> fully associative
	 * associativity = otherwise --> m-way set associative
	 * */
	int s , l , m , access_time ;
	int[][] valid_tag;
	String[][] content;
	
	int [] lruFully;
	int lruCountFully;
	
	int [][] lruSet;
	int [] lruCountSet;
	
	int c;
	int index_bits , offset_bits , tag_bits;
	int hits , misses , trials; 
	float hit_ratio;
	/*
	 * L --> number of bytes per cache line
	 * S --> L*C where C --> number of cache lines
	 * M --> 
	 * content is a 2D array of Strings 
	 * Col #1 --> valid bit 
	 * Col #2 --> tag 
	 * Col #3 --> the actual content of the block
	 * */
	
	public Icache(	int s ,int l ,int m  ,int access_time){
		this.s = s;
		this.l = l;
		this.m = m;
		this.access_time = access_time;
		hits = 0;
		misses = 0;
		trials = 0;
		c  = s/l;
		
		lruFully = new int[c];
		lruCountFully = 1;
		
		lruSet = new int [c/m][m];
		lruCountSet = new int [c/m];
		
		valid_tag = new int[c][2];
		content = new String [c][l];
		//originally the cache is empty
		for (int i = 0; i < valid_tag.length; i++) {
			valid_tag[i][0] = 0;
			valid_tag[i][1] = -1;
		}
		
		for (int i = 0; i < content.length; i++) {
			for (int j = 0; j < l; j++) {
				content [i][j] = "";
			}
		}
		
		offset_bits = (int)(Math.log(l)/Math.log(2));
		if(m == 1 ){
			//direct mapped
			index_bits = (int)(Math.log(c)/Math.log(2));
		}else if (m == c) {
			// fully 
			index_bits = 0;
		}else{
			// m-way
			index_bits = (int)(Math.log(c/m)/Math.log(2));
		}
		tag_bits = 16 - (index_bits + offset_bits);
	}
	
	/*
	 * Address subdivision
	 * takes an integer address and convert it into offset - index - tag
	 * based on the associativity level
	 * */
	public int[] address_subdivision(int address){
		// Hadeel + Mogh + Badr
		/*
		 * convert the number into binary and then 
		 * extract the index , offset , tag 
		 * convert them into integers again 
		 * cell 0 --> tag
		 * cell 1 --> index
		 * cell 2 --> offset
		 * 
		 * */
		int[] result = new int [3];
		String binary_address = Integer.toBinaryString(address);
		for (int i = binary_address.length(); i < 16; i++) {
			binary_address = "0"+binary_address;
		}
		String offset = binary_address.substring(16 - offset_bits);
		String index = binary_address.substring(16 - offset_bits - index_bits , 16 - offset_bits) ;
		String tag =  binary_address.substring(0 , tag_bits);
		result[0] = Integer.parseInt(tag, 2);
		result[1] = Integer.parseInt(index, 2);
		result[2] = Integer.parseInt(offset, 2);
		return result;
	}
	
	/*
	 * in the nest three methods we should return an array containing the entire block to be able to 
	 * update al the higher caches and the required result which will be stored in the last cell after the data block*/
    String[] direct_mapped( int[]division, int address){ // Omar + Abdelazeem
		String[] result = new String [l+1];

		if (valid_tag[division[1]][1] == division[0] && valid_tag[division[1]][0] == 1) {
			String[] tempResult = content[division[1]];
			
			int i = 0;
			for (i = 0; i < l; i++) {
				result[i] = tempResult[i];
			}
			result[i] = content[division[1]][division[2]];
			return result;
		}
		else {
			return null;
		}
	}
	
    
    String[] fully( int[]division, int address){ // Omar + Abdelazeem
		String[] result = new String [l+1];

		for (int n = 0; n < c; n++) {
			if (valid_tag[n][1] == division[0] && valid_tag[n][0] == 1) {
				String[] tempResult = content[division[1]];

				int i = 0;
				for (i = 0; i < l; i++) {
					result[i] = tempResult[i];
				}
				result[i] = content[n][division[2]];
				return result;
			}
		}
		return null;
	}
    
    String[] set( int[]division, int address){ // Omar + Abdelazeem
		String[] result = new String [l+1];

		for (int n = 0; n < m; n++) {
			if (valid_tag[division[1]+n][1] == division[0] && valid_tag[division[1]+n][0] == 1) {
				String[] tempResult = content[division[1]];
				
				int i = 0;
				for (i = 0; i < l; i++) {
					result[i] = tempResult[i];
				}
				result[i] = content[division[1]][division[2]];
				return result;
			}
		}
		return null;
	}
    
    void  update_cache(int ad , String[] data){
    	 //Omar + Zeema
    	/*
    	 * address subdivision on ad 
    	 * in case of direct mapped --> replace the specific index 
    	 * in case of fully or m way --> LRU HOW!!?
    	 * when updating cache valid bit to 1 and tag to the result of the sub division and the content to the data (argument)
    	 * */
    	
    	int [] division = address_subdivision(ad);
    	
    	//direct-mapped
    	if (m == 1) {
    		int index = division[1];
    		valid_tag[index][0] = 1;
    		valid_tag[index][1] = division[0];
    		content[index] = data;
    		return;
    	}
    	
    	//fully
    	else if (m == c) {
    		for (int i = 0; i < c; i++) {
    			if (valid_tag[i][0] == 0) {
    				valid_tag[i][0] = 1;
    				valid_tag[i][1] = division[0];
    				content[i] = data;
    				return;
    			}
    		}
    		int index = leastRecentlyUsedFully();
    		valid_tag[index][0] = 1;
    		valid_tag[index][1] = division[0];
    		content[index] = data;
    		lruFully[index] = lruCountFully;
    		lruCountFully++;
    	}
    	
    	//set
    	else {
    		int index = division[1];
    		for (int i = index; i < m+index; i++) {
    			if (valid_tag[i][0] == 0) {
    				valid_tag[i][0] = 1;
    				valid_tag[i][1] = division[0];
    				content[i] = data;
    				return;
    			}
    		}
    		int least = leastRecentlyUsedSet(index);
    		valid_tag[(index*m)+least][0] = 1;
    		valid_tag[(index*m)+least][1] = division[0];
    		content[(index*m)+least] = data;
    		
    		// updating the LRU of set-associative
    		lruSet[index][least] = lruCountSet[(index*m)+least];
    		lruCountSet[(index*m)+least]++;
    	}
    	
    	/* 1. address subdivision
    	 * 2. type?
    	 * 3. direct mapped -> access index, write
    	 * 4. set -> check for empty place (valid = 0) / LRU
    	 * 5. fully -> check for empty place (valid = 0) / LRU
    	 * */
    }
    
    int leastRecentlyUsedFully() { // Omar + Abdelazeem
    	int min = lruCountFully;
    	int index = -1;
    	for (int i = 0; i < lruFully.length; i++) {
    		if (lruFully[i] < min) {
    			min = lruFully[i];
    			index = i;
    		}
    	}
    	return index;
    }
    
    int leastRecentlyUsedSet(int index) { // Omar + Abdelazeem
    	int min = lruCountSet[index];
    	int j = -1;
    	for (int i = 0; i < lruSet[index].length; i++) {
    		if (lruSet[index][i] < min) {
    			min = lruSet[index][i];
    			j = i;
    		}
    	}
    	return j;
    }
    
	public String[] check_Icache(int address){
		String[] result = new String [l+1];
		trials++;
		int [] division = address_subdivision(address);
		if(m == 1 ){
			result = direct_mapped(division, address);
		}else if (m == c) {
			result = fully(division, address);
			int index = leastRecentlyUsedFully();
			lruFully[index] = lruCountFully;
    		lruCountFully++;
		}else{
			result = set(division, address);
			int index = division[1];
			int least = leastRecentlyUsedSet(index);
			lruSet[index][least] = lruCountSet[(index*m)+least];
    		lruCountSet[(index*m)+least]++;
		}
		return result; 
	}
	

}
