import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.text.html.HTMLDocument.Iterator;


public class MainClass {
	static String [] program;
	static String [] data;
	static Icache[] icaches;
	static Icache[] dcaches;
	static int cache_cycles;
	static float amat;
	static float ipc;
	static float ex;
	static float cycle_time = 3.0f;
	static MainMemory main_memory;
	
	//tumasulo variables
	 static int [] registers;
	 static int [] registers_status;
	 static int num_of_RS; //number of functional units
	 static Hashtable<String, Integer> free_units;
	 static Hashtable<String, Integer> execution_cycle;
	 static String[][] Ibuffer; //number of entries = number of instructions
	 static ReservationStation[] table; //number of entries = number of RSs
	 static int next_issue; //the next instruction to be issued 
	 static int ROB_size;
	 static ROB rob;
	 
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
	  * Ibuffer[][9] = started execution or not
	  * Ibuffer[][10] = ROB#
	  * Ibuffer[][11] = can not
	  * Ibuffer[][12] = scoreboard corresponding entry
	  * Ibuffer[][13] = commited
	  * Ibuffer[][14] = 
	  * Ibuffer[][15] = address_calculated
	  * */
	 
	 
	/*
	 * what we need in order to test our simulatior is the following 
	 * Main Memory and give it the access time 
	 * array of caches with length = the cache levels + 1 - index 1 --> L1 - index 2 --> L2 and so on 
	 * */
	public static void main(String [] args){
		
		// the program to be loaded 
		program = new String [4];
		data = new String [10];
		/*program[0] = "Divd R1 R2 R3";
		program[1] = "Add R5 R7 R3";
		program[2] = "Add R6 R5 R5";
		program[3] = "Multd R7 R6 R5";
		 */
		program[0] = "LD R1 4(R2)";
		program[1] = "Add R3 R1 R2";
		program[2] = "Add R6 R3 R3";
		program[3] = "LD R4 4(R2)";
		for (int i = 0; i < data.length; i++) {
			data[i] = 10+"";
		}
		
		//inputs for tumasulo 
		num_of_RS = 9;
		free_units = new Hashtable<String, Integer>();
		free_units.put("LD", 2);
		free_units.put("Store", 2);
		free_units.put("Add", 2);
		free_units.put("Addd", 2);
		free_units.put("Multd", 1);
		execution_cycle = new Hashtable<String, Integer>();
		execution_cycle.put("LD", 2);
		execution_cycle.put("Store", 2);
		execution_cycle.put("Add", 2);
		execution_cycle.put("Addd", 2);
		execution_cycle.put("Multd", 6);
		execution_cycle.put("Divd", 13);
		Ibuffer = new String[program.length][16];
		table = new ReservationStation[num_of_RS];
		initializeScoreBoard();
		initializeRegisters();
		next_issue = 0;
		ROB_size = 4;
		rob = new ROB (ROB_size);
		int at = 6 ; //the access time of the main memory - should change this value
		main_memory = new MainMemory(at,8); //second argument --> line size 
		int cache_levels = 2;
		icaches = new Icache [cache_levels+1];
		dcaches = new Icache [cache_levels+1];
		
		icaches[1] = new Icache(32, 8, 2, 5);
		icaches[2] = new Icache(32, 8, 2, 3);
		dcaches[1] = new Icache(32, 8, 2, 5);
		dcaches[2] = new Icache(32, 8, 2, 3);
		//caches[2] = new Icache(32, 8, 4, 2);
		// repeat the same line for all levels, change the values of the parameters.
		

		// repeat the same line for all the lines of code 
		
		// load the program to main memory
		int start_data = 0;
		int start_program = (int)Math.pow(2, 16)/2 ;
		main_memory.load(program, start_program);
		main_memory.load(data, start_data);
		//fetch all the program instructions
		int required_addres = start_program;
		int end = start_program + (program.length*2) - 2;
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
		
		Tumassulo();
		print_registers();
	}
	

	
	static void Tumassulo(){
		//Ibuffer[Ibuffer.length-1][7].equals("F");
		int x = 0;
		while(x<30){
			System.out.println("Cycle: "+x);
			
			//check if instruction next_issue can be issued and if yes issue it 
			if (next_issue<program.length) {
				issue(next_issue);
			}			
			update_ready_values();
			//execution updates 
			for (int i = Ibuffer.length-1; i >= 0 ; i--) {	
				calculate_address(i);
				execute(i);	
				writeback(i);							
			}
			//commit if possible 
			commit();
			
			
			x++;
			print_scoreboard();
			print_Ibuffer();
			print_registers_status();
			rob.print_ROB();
		}		
	}

	

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
	  * Ibuffer[][9] = started execution or not
	  * */
	
	//Calculate address 
	static void calculate_address(int i){

		if(Ibuffer[i][5].equals("T") && Ibuffer[i][15].equals("F")){
			int table_i = Integer.parseInt(Ibuffer[i][12]);
			if(table[table_i].Qj==-1 && Ibuffer[i][3].equals("LD")){
				if (!Ibuffer[i][11].equals("cal")) {
					Ibuffer[i][11] = "ex";
					int address = Integer.parseInt(Ibuffer[i][14]) + registers[table[table_i].Vj];
					table[table_i].a = address;
					Ibuffer[i][15] = "T";
					System.out.println("Calculate address");
				}
				else{
					Ibuffer[i][11] = "ex";
				}			
			}
		}
	}
	
	//Commit stage
	static void commit(){
		for (int i = 0; i < Ibuffer.length; i++) {
			if(!Ibuffer[i][11].equals("com")){
				int rob_entry = Integer.parseInt(Ibuffer[i][10]);
				int[] commited = rob.commit(rob_entry);
				if(commited != null){
					// write the result into the destination register
					registers[commited[0]] =commited[1];
					//update the register status table 
					registers_status[commited[0]] = -1;
					Ibuffer[i][13] = "T";
					break;
				}
			}
			else{
				Ibuffer[i][11]="";
			}
			
		}
		
	}
	
	static void update_ready_values(){
		for (int i = 0; i < table.length; i++) {
			if(table[i].Qj!=-1){
				if(rob.entries[table[i].Qj].ready){
					table[i].Vj = rob.entries[table[i].Qj].value;
					table[i].Qj = -1;
				}
			}
			if(table[i].Qk!=-1){
				if(rob.entries[table[i].Qk].ready){
					table[i].Vk = rob.entries[table[i].Qk].value;
					table[i].Qk = -1;
				}
			}
		}
	}
	//WriteBack stage
	static void writeback(int x){
		if(Ibuffer[x][6].equals("T") && !Ibuffer[x][7].equals("T")){
			if(!Ibuffer[x][11].equals("wb")){
				rob.update_value(Integer.parseInt(Ibuffer[x][10]), execute_result(x));
				Ibuffer[x][7]="T";	
				//update scoreboard - delete its entry - update all the depending entries 
				update_scoreboard(x);
				remove_scoreboard(x);
				Ibuffer[x][11]="com";
			}
			else{
				Ibuffer[x][11]="";
			}
		}
	}
	
	static void remove_scoreboard(int x){
		String name = Ibuffer[x][4];
		for (int i = 0; i < table.length; i++) {
			if(table[i].name.equals(name)){
				table[i].busy=false;
				table[i].op= null;
				table[i].Qj=-1;
				table[i].Qk=-1;
				table[i].Vj=0;
				table[i].Vk=0;
			}
			
		}
	}
	static void update_scoreboard(int x){
		int rob_row = Integer.parseInt(Ibuffer[x][10]);
		
		for (int i = 0; i < table.length; i++) {
			
			if (table[i].Qj == rob_row) {
				System.out.println("###### "+x);
				table[i].Qj=-1;
				table[i].Vj=rob.entries[rob_row].value;
			}
			if (table[i].Qk == rob_row) {
				System.out.println("###### "+x);
				System.out.println("KK "+rob.entries[rob_row].value);
				table[i].Qk=-1;
				table[i].Vk=rob.entries[rob_row].value;
			}
		}
	}
	//Execute stage 
	static void execute(int x){
		//System.out.println("IBUFFER");
		//print_Ibuffer();
		if(Ibuffer[x][5].equals("T")){
			//casse 1
			if(Ibuffer[x][9].equals("T") && !Ibuffer[x][8].equals("0")){
				int c = Integer.parseInt(Ibuffer[x][8]);
				c--;
				Ibuffer[x][8] = c+"";
				if(c==0){
					Ibuffer[x][6] = "T";
					Ibuffer[x][11] = "wb";
				}			
			}
			//casse 2 --> check if it can start EX
			if(Ibuffer[x][9].equals("F")){
				if(!Ibuffer[x][11].equals("ex")){
					cache_cycles = 0;
					execution_cycle.put("LD", cache_cycles);
					int dest = Integer.parseInt(Ibuffer[x][2].charAt(1)+"");
					int table_i = Integer.parseInt(Ibuffer[x][12]);
					System.out.println("Table#"+table_i);
					if(table[table_i].Qj==-1 && table[table_i].Qk==-1){
						// it can start executing
						Ibuffer[x][9] = "T";
						
						if (Ibuffer[x][3].equals("LD")) {
							cache_cycles = 0;
							int temp = Integer.parseInt(fetch(table[table_i].a));
							execution_cycle.put("LD", cache_cycles);
							Ibuffer[x][8] = cache_cycles+"";
							System.out.println("Load cycles "+execution_cycle.get("LD")+" "+ Ibuffer[x][8]);
						}
						int c = Integer.parseInt(Ibuffer[x][8]);
						c--;
						Ibuffer[x][8] = c+"";
						if(c==0){
							Ibuffer[x][6] = "T";
							Ibuffer[x][11] = "wb";
						}
						//update the registers status 
						registers_status[dest] = Integer.parseInt(Ibuffer[x][10]);
						//update the score board
						for (int i = 0; i < table.length; i++) {
							
						}
					}
				}else{
					Ibuffer[x][11]="";
				}	
			}
		}
	}
	
	static int execute_result(int index){
		int result = 0;
		int table_i = Integer.parseInt(Ibuffer[index][12]);
		int s1 = table[table_i].Vj;
		int s2 = table[table_i].Vk;
		switch (Ibuffer[index][3]) {
		case "Divd":
			result = s1/s2;
			break;
		case "Add":
			result = s1+s2;
			break;
		case "Addd":
			result = s1+s2;
			break;
		case"Multd":
			result = s1*s2;
			break;
		case"LD":
			result = Integer.parseInt(fetch(table[table_i].a));
			break;
		default:
			break;
		}
		return result;
	}
	
	//Issue stage
	
	static boolean issue(int i){
		/*
		 * get the needed FU --> Ibuffer[i][4]
		 * check in table if we have a free unit 
		 * if yes return true
		 * */
		String free_fu = check_issue(Ibuffer[i][4]);
		if(free_fu.equals("")||rob.contains==rob.size){
			//can not issue
		}
		else{
			//update Ibuffer[i] 
			Ibuffer[i][5]="T";
			Ibuffer[i][4]=free_fu;
			//update ROB
			Ibuffer[i][10]=rob.add_entry(Ibuffer[i][3], Integer.parseInt(Ibuffer[i][2].charAt(1)+""))+"";
			//update table
			update_table_after_issue(free_fu, i);
			next_issue++;
			if(Ibuffer[i][3].equals("LD")){
				Ibuffer[i][11]="cal";
			}else{
				Ibuffer[i][11]="ex";
			}
			
			
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
	
	static void update_table_after_issue(String n ,int x){
		for (int i = 0; i < table.length; i++) {
			//System.out.println(n +" "+table[i].name);
			if(table[i].name.equals(n)){
				if (table[i].name.substring(0, 2).equals("LD")) {
					int dest = Integer.parseInt((Ibuffer[x][2].charAt(1)+""));
					table[i].busy = true;
					table[i].op = Ibuffer[x][3];
					//check the registers status table
					int s1 = Integer.parseInt((Ibuffer[x][0].charAt(1)+""));
					if(registers_status[s1]==-1)
						table[i].Vj = registers[Integer.parseInt(Ibuffer[x][0].charAt(1)+"")];
					else
						table[i].Qj = registers_status[s1];
					registers_status[dest] = Integer.parseInt(Ibuffer[x][10]);
					Ibuffer[x][12] = i+"";
					table[i].a = Integer.parseInt(Ibuffer[x][14]);
				}
				else{
					table[i].busy = true;
					table[i].op = Ibuffer[x][3];
					//check the registers status table
					int s1 = Integer.parseInt((Ibuffer[x][0].charAt(1)+""));
					int s2 = Integer.parseInt((Ibuffer[x][1].charAt(1)+""));
					int dest = Integer.parseInt((Ibuffer[x][2].charAt(1)+""));
					if(registers_status[s1]==-1)
						table[i].Vj = registers[Integer.parseInt(Ibuffer[x][0].charAt(1)+"")];
					else
						table[i].Qj = registers_status[s1];
					
					if(registers_status[s2]==-1)
						table[i].Vk = registers[Integer.parseInt(Ibuffer[x][1].charAt(1)+"")];
					else
						table[i].Qk = registers_status[s2];
					registers_status[dest] = Integer.parseInt(Ibuffer[x][10]);
					Ibuffer[x][12] = i+"";
					//the A attribute is not used now
				}
			}				
		}
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
		registers_status = new int[8];
		for (int i = 0; i < registers.length; i++) {
			registers[i] = 2;
			registers_status[i]=-1;
		}
	}
	
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
	  * Ibuffer[][9] = started execution or not
	  * Ibuffer[][10] = ROB#
	  * Ibuffer[][11] = can not
	  * Ibuffer[][12] = scoreboard corresponding entry
	  * Ibuffer[][13] = commited
	  * */
	
	static void updateIbuffer(String[] decoded , int i){
		if(decoded[0].equals("LD")){
			System.out.println(decoded[2]);
			
			Ibuffer[i][0] = decoded[2].substring(decoded[2].length()-3, decoded[2].length()-1);
			Ibuffer[i][2] = decoded[1];
			Ibuffer[i][3] = decoded[0];
			Ibuffer[i][4] = needed_unit(decoded[0]);
			Ibuffer[i][5] = "F";
			Ibuffer[i][6] = "F";
			Ibuffer[i][7] = "F";
			Ibuffer[i][8] = execution_cycle.get(Ibuffer[i][3]).toString();	
			Ibuffer[i][9] = "F";
			Ibuffer[i][10] = "-1";
			Ibuffer[i][11] ="";
			Ibuffer[i][12] ="";
			Ibuffer[i][13] = "F";
			Ibuffer[i][14] = decoded[2].substring(0, decoded[2].length()-4);
			Ibuffer[i][15] = "F";
		}else{
			Ibuffer[i][0] = decoded[2];
			Ibuffer[i][1] = decoded[3];
			Ibuffer[i][2] = decoded[1];
			Ibuffer[i][3] = decoded[0];
			Ibuffer[i][4] = needed_unit(decoded[0]);
			Ibuffer[i][5] = "F";
			Ibuffer[i][6] = "F";
			Ibuffer[i][7] = "F";
			Ibuffer[i][8] = execution_cycle.get(Ibuffer[i][3]).toString();	
			Ibuffer[i][9] = "F";
			Ibuffer[i][10] = "-1";
			Ibuffer[i][11] ="";
			Ibuffer[i][12] ="";
			Ibuffer[i][13] = "F";
			Ibuffer[i][15] = "F";
		}	
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
		String result ="";
		Icache[] used_cache;
		if (address < (int)Math.pow(2, 16) / 2) {
			used_cache = dcaches;
		}
		else{
			used_cache = icaches;
		}
		for (int i = used_cache.length - 1 ; i >= 1; i -- ) {
			String[] cache_result = used_cache[i].check_Icache(address);
			if ( cache_result!=null){
				// hit in level i 
				cache_cycles+=used_cache[i].access_time;
				result = cache_result[cache_result.length-1] ; 
				//update all the higher levels
				
				String [] tempData = new String [cache_result.length - 1];
				for (int n = 0; n < tempData.length; n++) {
					tempData[n] = cache_result[n];
				}
				
				//update_all_caches(i+1, Arrays.copyOfRange(cache_result, 0, cache_result.length-1) , address);
				update_all_caches(i+1, tempData , address);
				used_cache[i].hits+=1;
				return result;
			}else{
				used_cache[i].misses+=1;
				cache_cycles+=used_cache[i].access_time;
			}
		}
		// misses in all the cache levels so we should go to main memory
		String[] mem_result = main_memory.read(address);
		result = mem_result[mem_result.length-1] ;
		update_all_caches(1, Arrays.copyOfRange(mem_result, 0, mem_result.length-1) , address);
		cache_cycles+= main_memory.access_time;
		return result;
	}
	
	static void update_all_caches(int start_level , String []data , int ad){
		for (int i = start_level; i <icaches.length; i++) {
			icaches[i].update_cache(ad, data);
		}
	}
	
	// calculate the hit ratio for all the cache levels 
	void cache_hit_ratio(){
		for (int i = 0; i < icaches.length; i++) {
			icaches[i].hit_ratio = icaches[i].hits / icaches[i].trials;
		}
	}
	
	//Calculations
	
	//calculate the AMAT
	static void AMAT(){
		//Nadine
		// AMAT = hit time + (miss rate * miss penalty) 
		
		float m_ratio = icaches[icaches.length - 1].misses / icaches[icaches.length - 1].trials ;
		System.out.println("m-ratio "+m_ratio);
		amat = icaches[icaches.length - 1].access_time*cycle_time;
		for (int i = icaches.length - 1; i >= 1; i--) {
			if( i > 1 )
				amat +=  m_ratio * icaches[i-1].access_time * cycle_time;
			else
				amat +=   m_ratio * main_memory.access_time * cycle_time;
			m_ratio *= icaches[i].misses / icaches[i].trials;
		}
	}
	
	//calculate the IPC
	static void IPC(){
		//Nadine 
		// CPI = Base CPI + CPI instructions + CPI Data 
		// for now we do not calculate CPI Data
		float cpi = 1; //base  cpi
		float m_ratio = icaches[icaches.length - 1].misses / icaches[icaches.length - 1].trials ;
		for (int i = icaches.length - 1; i >= 1; i--) {
			if( i > 1 )
				cpi +=  m_ratio * icaches[i-1].access_time;
			else
				cpi +=  m_ratio * main_memory.access_time;
			m_ratio *= icaches[i].misses / icaches[i].trials;
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
			String result = "I#" + i + " S:" + Ibuffer[i][0] + " S2:" + Ibuffer[i][1] + " D:" + Ibuffer[i][2] + " Op:"+ Ibuffer[i][3] + " FU:"+ Ibuffer[i][4] + " issued:" + Ibuffer[i][5] + " executed:" + Ibuffer[i][6] + " wb:" +Ibuffer[i][7]+" com:" +Ibuffer[i][13] + " cycles:"+Ibuffer[i][8] +" Started Ex:"+Ibuffer[i][9]+" ROB:"+Ibuffer[i][10] +" A:"+Ibuffer[i][14] + " cant:"+Ibuffer[i][11];    
			System.out.println(result);
		}
		System.out.println("***********************");
	}
	
	static void  print_cache(){
		System.out.println("Cache content");
		for (int i = 1; i < icaches.length; i++) {
			System.out.println("Cache Level "+i);
			icaches[i].print_cache();
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
	
	static void print_registers(){
		System.out.println("Rgisters");
		String result="";
		for (int i = 0; i < registers.length; i++) {
			result+= (registers[i]+" ");
		}
		System.out.println(result);
	}
	

}
