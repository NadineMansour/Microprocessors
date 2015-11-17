import java.util.Arrays;


public class MainClass {
	static Icache[] caches;
	float amat;
	float ipc;
	int ex;
	static MainMemory main_memory;
	/*
	 * what we need in order to test our simulatior is the following 
	 * Main Memory and give it the access time 
	 * array of caches with length = the cache levels + 1 - index 1 --> L1 - index 2 --> L2 and so on 
	 * */
	public static void main(String [] args){
		int at = 0 ; //the access time of the main memory - should change this value
		main_memory = new MainMemory(at,0); //second argument --> line size 
		int cache_levels = 0;
		caches = new Icache [cache_levels+1];
		
		caches[1] = new Icache(0, 0, 0, 0);
		// repeat the same line for all levels, change the values of the parameters.
		
		// the program to be loaded 
		String [] program = new String [1];
		program[0] = "";
		// repeat the same line for all the lines of code 
		
		// load the program to main memory
		int start = 0;
		main_memory.load_program(program, start);
		
		//fetch all the program instructions
		int required_addres = start;
		int end = start + (program.length*2) - 2;
		while(required_addres <=end){
			/*
			 * check the caches starting from the last one in the array
			 * if the result = "" --> miss in this level otherwise it is a hit 
			 * in case of miss go to the next level 
			 * in case of hit in a cache level we need to update all the higher levels using what we found in the cache
			 * in case of misses in all levels go to main memory then update all the caches 
			 * */
			// call method fetch on each address
			required_addres+=2;
		}
	}
	
	String fetch (int address){
		//Nadine
		String result ="";
		for (int i = caches.length - 1 ; i <= 1; i -- ) {
			String[] cache_result = caches[i].check_Icache(address);
			if ( !cache_result[cache_result.length-1].equals("")){
				// hit in level i 
				result = cache_result[cache_result.length-1] ; 
				//update all the higher levels
				update_all_caches(i+1, Arrays.copyOfRange(cache_result, 0, cache_result.length-1) , address);
				return result;
			}
		}
		// misses in all the cache levels so we should go to main memory
		String[] mem_result = main_memory.read(address);
		result = mem_result[mem_result.length-1] ;
		update_all_caches(1, Arrays.copyOfRange(mem_result, 0, mem_result.length-1) , address);
		return result;
	}
	
	void update_all_caches(int start_level , String []data , int ad){
		for (int i = start_level; i <caches.length; i++) {
			caches[i].update_cache(ad, data);
		}
	}
	
	// calculate the hit ratio for all the cache levels 
	void cache_hit_ratio(){
		for (int i = 0; i < caches.length; i++) {
			caches[i].hit_ratio = caches[i].hits / caches[i].trials;
		}
	}
	
	
	//calculate the AMAT
	void AMAT(){
		//Nadine
		// AMAT = hit time + (miss rate + miss penalty) 
		float cycle_time = 1.0f;
		float m_ratio = 1.0f;
		amat = caches[caches.length - 1].access_time*cycle_time;
		for (int i = caches.length - 1; i >= 1; i--) {
			if( i > 1 )
				amat += (caches[i].misses / caches[i].trials)* m_ratio * caches[i-1].access_time * cycle_time;
			else
				amat += (caches[i].misses / caches[i].trials) * m_ratio * main_memory.access_time * cycle_time;
			m_ratio += caches[i].misses / caches[i].trials;
		}
	}
	
	//calculate the IPC
	void IPC(){
		//Nadine 
		// CPI = Base CPI + CPI instructions + CPI Data 
		// for now we do not calculate CPI Data
		float cpi = 1; //base  cpi
		float m_ratio = 1.0f;
		for (int i = caches.length - 1; i >= 1; i--) {
			if( i > 1 )
				cpi += (caches[i].misses / caches[i].trials)* m_ratio * caches[i-1].access_time;
			else
				cpi += (caches[i].misses / caches[i].trials) * m_ratio * main_memory.access_time;
			m_ratio += caches[i].misses / caches[i].trials;
		}
		ipc = 1 / cpi;
	}
	
	//calculate the total execution time in cycle
	void EX(){
		//Hadeel + Mogh + Badr
	}
	

}
