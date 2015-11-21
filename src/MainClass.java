import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.text.html.HTMLDocument.Iterator;


public class MainClass {
	static Icache[] caches;
	static float amat;
	static float ipc;
	static float ex;
	static float cycle_time = 3.0f;
	static MainMemory main_memory;
	
	//tumasulo variables
	 static int [] registers;
	 static String [] registers_status;
	 static int num_of_RS; //number of functional units
	 static Hashtable<String, Integer> free_units;
	 static Hashtable<String, Integer> execution_cycle;
	 static String[][] Ibuffer; //number of entries = number of instructions
	 static ReservationStation[] table; //number of entries = number of RSs
	 static int next; //the next instruction to be issued 
	 
	 /* 
	  * Ibuffer[][0] = source1
	  * Ibuffer[][1] = source2
	  * Ibuffer[][2] = destination 
	  * Ibuffer[][3] = op
	  * Ibuffer[][4] = FU
	  * Ibuffer[][5] = issued
	  * Ibuffer[][6] = executed 
	  * Ibuffer[][7] = written
	  * Ibuffer[][8] = number of cycle left to finish execution 
	  * */
	 
	 
	/*
	 * what we need in order to test our simulatior is the following 
	 * Main Memory and give it the access time 
	 * array of caches with length = the cache levels + 1 - index 1 --> L1 - index 2 --> L2 and so on 
	 * */
	public static void main(String [] args){
		
		// the program to be loaded 
		String [] program = new String [4];
		program[0] = "Divd R1 R2 R3";
		program[1] = "Add R5 R1 R3";
		program[2] = "Addd R6 R5 R5";
		program[3] = "Multd R7 R6 R5";
		
		
		
		//inputs for tumasulo 
		num_of_RS = 9;
		free_units = new Hashtable<String, Integer>();
		free_units.put("Load", 2);
		free_units.put("Store", 2);
		free_units.put("Add", 2);
		free_units.put("Addd", 2);
		free_units.put("Multd", 1);
		execution_cycle = new Hashtable<String, Integer>();
		execution_cycle.put("Load", 5);
		execution_cycle.put("Store", 2);
		execution_cycle.put("Add", 1);
		execution_cycle.put("Addd", 2);
		execution_cycle.put("Multd", 6);
		execution_cycle.put("Divd", 13);
		Ibuffer = new String[program.length][9];
		table = new ReservationStation[num_of_RS];
		initializeScoreBoard();
		initializeRegisters();
		next = 0;
		
		
		int at = 6 ; //the access time of the main memory - should change this value
		main_memory = new MainMemory(at,8); //second argument --> line size 
		int cache_levels = 1;
		caches = new Icache [cache_levels+1];
		
		caches[1] = new Icache(32, 8, 2, 3);
		//caches[2] = new Icache(32, 8, 4, 2);
		// repeat the same line for all levels, change the values of the parameters.
		

		// repeat the same line for all the lines of code 
		
		// load the program to main memory
		int start = 0;
		main_memory.load_program(program, start);
		
		//fetch all the program instructions
		int required_addres = start;
		int end = start + (program.length*2) - 2;
		int i=0;
		while(required_addres <=end){
			
			/*
			 * check the caches starting from the last one in the array
			 * if the result = "" --> miss in this level otherwise it is a hit 
			 * in case of miss go to the next level 
			 * in case of hit in a cache level we need to update all the higher levels using what we found in the cache
			 * in case of misses in all levels go to main memory then update all the caches 
			 * */
			String instruction = fetch(required_addres);
			String[] decoded = decode(instruction);
			System.out.println(instruction);
			
			updateIbuffer(decoded, i);						
			i++;
			required_addres+=2;
		}
		
		
		AMAT();
		IPC();
		EX(program.length);
		//System.out.println("AMAT "+amat);
		//System.out.println("IPC "+ipc );
		//System.out.println("Ex "+ex);
		//print_cache();
		print_scoreboard();
		print_Ibuffer();
		print_registers_status();
	}
	

	
	

	




	

	

	static boolean issue(int i){
		/*
		 * get the needed FU --> Ibuffer[i][4]
		 * check in table if we have a free unit 
		 * if yes return true
		 * */
		String free_fu = check_issue(Ibuffer[i][4]);
		if(free_fu.equals("")){
			//can not issue
		}
		else{
			//update Ibuffer[i] and table
			Ibuffer[i][5]="T";
			Ibuffer[i][4]=free_fu;
		}
			
			
		return false;
	}
	
	static String check_issue(String fu){
		String result="";
		for (int i = 0; i < table.length; i++) {
			String[]temp = table[i].name.split("_");
			if(temp[0].equals(fu) && !table[i].busy)
				return table[i].name;
		}
		return result;
	}
	static void Tumassulo(){
		
	}
	
	//Init methods
	
	static void initializeScoreBoard(){
		int i=0;
		Enumeration<String> keys = free_units.keys();
		while(keys.hasMoreElements()){
			String  k = (String) keys.nextElement();
			int cc = free_units.get(k);
			for (int j = 1; j <= cc; j++) {
				String RU = k+"_"+j;
				table[i] = new ReservationStation(RU);
				i++;
			}
		}		
	}
	
	static void initializeRegisters(){
		registers = new int[8];
		registers_status = new String[8];
		for (int i = 0; i < registers.length; i++) {
			registers[i] = 0;
			registers_status[i]="";
		}
	}
	
	static void updateIbuffer(String[] decoded , int i){
		Ibuffer[i][0] = decoded[2];
		Ibuffer[i][1] = decoded[3];
		Ibuffer[i][2] = decoded[1];
		Ibuffer[i][3] = decoded[0];
		Ibuffer[i][4] = needed_unit(decoded[0]);
		Ibuffer[i][5] = "F";
		Ibuffer[i][6] = "F";
		Ibuffer[i][7] = "F";
		Ibuffer[i][8] = free_units.get(Ibuffer[i][4]).toString();		
	}
	
	static String needed_unit(String op){
		System.out.println(op);
		switch (op) {
		case "Divd":
			return "Multd";

		default:
			return op;
		}
	}
	
	//Fetch - Decode - Caches
	static String[] decode(String instruction){
		// op - dest - source1 - source 2
		String[]result = instruction.split(" ");
		return result;
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
				
				//update_all_caches(i+1, Arrays.copyOfRange(cache_result, 0, cache_result.length-1) , address);
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
		update_all_caches(1, Arrays.copyOfRange(mem_result, 0, mem_result.length-1) , address);
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
	
	//Calculations
	
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
	
	//Printing methods
	
	static void print_scoreboard(){
		System.out.println("ScoreBoard");
		for (int i = 0; i < table.length; i++) {
			System.out.println(table[i].display());
		}
		System.out.println("***********************");
	}
	
	static void print_Ibuffer(){
		System.out.println("Ibuffer");
		for (int i = 0; i < Ibuffer.length; i++) {
			String result = "I#" + i + " S:" + Ibuffer[i][0] + " S2:" + Ibuffer[i][1] + " D:" + Ibuffer[i][2] + " Op:"+ Ibuffer[i][3] + " FU:"+ Ibuffer[i][4] + " issued:" + Ibuffer[i][5] + " executed:" + Ibuffer[i][6] + " wb:" +Ibuffer[i][7] + " cycles:"+Ibuffer[i][8];    
			System.out.println(result);
		}
		System.out.println("***********************");
	}
	
	static void  print_cache(){
		System.out.println("Cache content");
		for (int i = 1; i < caches.length; i++) {
			System.out.println("Cache Level "+i);
			caches[i].print_cache();
			System.out.println("*************************");
		}		
	}
	
	static void print_registers_status(){
		System.out.println("Reg status");
		for (int i = 0; i < registers_status.length; i++) {
			System.out.println(i+" - "+registers_status[i]);
		}
		System.out.println("**************************");
	}
	

}
