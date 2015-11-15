
public class Icache {
	
	/*
	 * associativity = 1 --> direct mapped
	 * associativity = size of the cache --> fully associative
	 * associativity = otherwise --> m-way set associative
	 * */
	int s , l , m , access_time ;
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
		
		content = new String[c][3];
		//originally the cache is empty
		for (int i = 0; i < content.length; i++) {
			content[i][0] = "0";
			content[i][1] = "";
			content[i][2] = "";
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
		int[] result = new int [3];
		/*
		 * convert the number into binary and then 
		 * extract the index , offset , tag 
		 * convert them into integers again 
		 * */
		return result;
	}
	
	public String check_Icache(int address){
		String result = "";
		trials++;
		/*
		 * do the address subdivision 
		 * check the specific entry in the cache
		 * if the valid bit is 1 and the tags are equal --> hit and increment the total hits  by 1 and return the instruction
		 * otherwise --> miss and increment the number of misses by 1 and we have to call the check_Icache of the lower level
		 * or read it from main memory in case we are in the last level
		 * lessa mesh 3arfa hane3mel eh in case of miss hanroo7 ezai lel level ely ta7t aw main memory dih hanetefe2 3aleha bokra 
		 * 
		 * */
		return result; 
	}
	

}
