
public class MainClass {
	static Icache[] caches;
	float amat;
	float ipc;
	int ex;
	/*
	 * what we need in order to test our simulatior is the following 
	 * Main Memory and give it the access time 
	 * array of caches with length = the cache levels + 1 - index 1 --> L1 - index 2 --> L2 and so on 
	 * */
	public static void main(String [] args){
		int at = 0 ; //the access time of the main memory - should change this value
		MainMemory main_memory = new MainMemory(at);
		int cache_levels = 0;
		caches = new Icache [cache_levels+1];
		
		caches[1] = new Icache(0, 0, 0, 0);
		// repeat the same line for all levels, change the values of the parameters.
		
		// the program to be loaded 
		String [] program = new String [1];
		program[0] = "";
		// repeat the same line for all the lines of code 
		
		// load the program to main memory
		main_memory.load_program(program, 0);
		
		//fetch all the program instructions
		//lessa mesh 3arfa momken nemasheha ezai hanetkalem feeha 
	}
	
	// calculate the hit ratio for all the cache levels 
	void cache_hit_ratio(){
		for (int i = 0; i < caches.length; i++) {
			caches[i].hit_ratio = caches[i].hits / caches[i].trials;
		}
	}
	
	//calculate the AMAT
	void AMAT(){
		
	}
	
	//calculate the IPC
	void IPC(){
		
	}
	
	//calculate the totale execution time in cycle
	void EX(){
		
	}
	

}
