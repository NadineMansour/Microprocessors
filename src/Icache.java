
public class Icache {
	
	/*
	 * associativity = 1 --> direct mapped
	 * associativity = size of the cache --> fully associative
	 * associativity = otherwise --> m-way set associative
	 * */
	int s , l , m , access_time ;
	String[][] valid_tag;
	String[][] content;
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
		
		valid_tag = new String[c][2];
		content = new String [c][l];
		//originally the cache is empty
		for (int i = 0; i < valid_tag.length; i++) {
			valid_tag[i][0] = "0";
			valid_tag[i][1] = "";
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
    String[] direct_mapped( int[]address){
		String[] result = new String [l+1];
		//Omar + Zeema
		return result;
	}
	
    
    String[] fully( int[]address){
		String[] result = new String [l+1];
		//Omar + Zeema
		return result;
	}
    
    String[] set( int[]address){
		String[] result = new String [l+1];
		//Omar + Zeema
		return result;
	}
    
    void  update_cache(int ad , String[] data){
    	 //Omar + Zeema
    	/*
    	 * address subdivision on ad 
    	 * in case of direct mapped --> replace the specific index 
    	 * in case of fully or m way --> LRU HOW!!?
    	 * when updating cache valid bit to 1 and tag to the result of the sub division and the content to the data (argument)
    	 * */
    }
    
	public String[] check_Icache(int address){
		String[] result = new String [l+1];
		trials++;
		int [] division = address_subdivision(address);
		if(m == 1 ){
			result = direct_mapped(division);
		}else if (m == c) {
			result = fully(division);
		}else{
			result = set(division);
		}
		return result; 
	}
	

}
