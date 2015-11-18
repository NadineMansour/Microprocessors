import java.util.Arrays;


public class MainClass {
	static Icache[] caches;
	static float amat;
	static float ipc;
	static float ex;
	static float cycle_time = 3.0f;
	static MainMemory main_memory;
	/*
	 * what we need in order to test our simulatior is the following 
	 * Main Memory and give it the access time 
	 * array of caches with length = the cache levels + 1 - index 1 --> L1 - index 2 --> L2 and so on 
	 * */
	public static void main(String [] args){
		int at = 6 ; //the access time of the main memory - should change this value
		main_memory = new MainMemory(at,8); //second argument --> line size 
		int cache_levels = 1;
		caches = new Icache [cache_levels+1];
		
		caches[1] = new Icache(32, 8, 2, 3);
		//caches[2] = new Icache(32, 8, 4, 2);
		// repeat the same line for all levels, change the values of the parameters.
		
		// the program to be loaded 
		String [] program = new String [24];
		program[0] = "I1";
		program[1] = "I2";
		program[2] = "I3";
		program[3] = "I4";
		program[4] = "I5";
		program[5] = "I6";
		program[6] = "I7";
		program[7] = "I8";
		program[8] = "I9";
		program[9] = "I10";
		program[10] = "I11";
		program[11] = "I12";
		program[12] = "I13";
		program[13] = "I14";
		program[14] = "I15";
		program[15] = "I16";
		program[16] = "I17";
		program[17] = "I18";
		program[18] = "I19";
		program[19] = "I20";
		program[20] = "I21";
		program[21] = "I22";
		program[22] = "I23";
		program[23] = "I24";
		// repeat the same line for all the lines of code 
		
		// load the program to main memory
		int start = 0;
		main_memory.load_program(program, start);
		
		//fetch all the program instructions
		int required_addres = start;
		int end = start + (program.length*2) - 2;
		while(required_addres <=end){
			System.out.println("Fetched Instruction : "+ fetch(required_addres));
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
		AMAT();
		IPC();
		EX(program.length);
		System.out.println("AMAT "+amat);
		System.out.println("IPC "+ipc );
		System.out.println("Ex "+ex);
		print_cache();
	}
	
	static String fetch (int address){
		//Nadine
		String result ="";
		for (int i = caches.length - 1 ; i >= 1; i -- ) {
			String[] cache_result = caches[i].check_Icache(address);
			if ( cache_result!=null){
				// hit in level i 
				result = cache_result[cache_result.length-1] ; 
				//update all the higher levels
				
				String [] tempData = new String [cache_result.length - 1];
				for (int n = 0; n < tempData.length; n++) {
					tempData[n] = cache_result[n];
				}
				
//				update_all_caches(i+1, Arrays.copyOfRange(cache_result, 0, cache_result.length-1) , address);
				update_all_caches(i+1, tempData , address);
				caches[i].hits+=1;
				return result;
			}else{
				caches[i].misses+=1;
			}
		}
		// misses in all the cache levels so we should go to main memory
		String[] mem_result = main_memory.read(address);
		result = mem_result[mem_result.length-1] ;
		
		/*String [] tempData = new String [mem_result.length - 1];
		for (int n = 0; n < tempData.length; n++) {
			tempData[n] = mem_result[n];
		}*/
		
		update_all_caches(1, Arrays.copyOfRange(mem_result, 0, mem_result.length-1) , address);
		//update_all_caches(1, tempData , address);
		return result;
	}
	
	static void update_all_caches(int start_level , String []data , int ad){
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
	static void AMAT(){
		//Nadine
		// AMAT = hit time + (miss rate * miss penalty) 
		
		float m_ratio = caches[caches.length - 1].misses / caches[caches.length - 1].trials ;
		System.out.println("m-ratio "+m_ratio);
		amat = caches[caches.length - 1].access_time*cycle_time;
		for (int i = caches.length - 1; i >= 1; i--) {
			if( i > 1 )
				amat +=  m_ratio * caches[i-1].access_time * cycle_time;
			else
				amat +=   m_ratio * main_memory.access_time * cycle_time;
			m_ratio *= caches[i].misses / caches[i].trials;
		}
	}
	
	//calculate the IPC
	static void IPC(){
		//Nadine 
		// CPI = Base CPI + CPI instructions + CPI Data 
		// for now we do not calculate CPI Data
		float cpi = 1; //base  cpi
		float m_ratio = caches[caches.length - 1].misses / caches[caches.length - 1].trials ;
		for (int i = caches.length - 1; i >= 1; i--) {
			if( i > 1 )
				cpi +=  m_ratio * caches[i-1].access_time;
			else
				cpi +=  m_ratio * main_memory.access_time;
			m_ratio *= caches[i].misses / caches[i].trials;
		}
		ipc = 1 / cpi;
	}
	
	//calculate the total execution time in cycle
	static void EX(int count){
		//Hadeel + Mogh + Badr
		ex = count * (1.0f / ipc) * cycle_time;
	}
	
	static void  print_cache(){
		System.out.println("Cache content");
		for (int i = 1; i < caches.length; i++) {
			System.out.println("Cache Level "+i);
			caches[i].print_cache();
			System.out.println("*************************");
		}
		
	}
	

}
