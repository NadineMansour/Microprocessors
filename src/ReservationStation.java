
public class ReservationStation {
	String name;
	boolean busy;
	String op  ;
	int Qj , Qk, Vj , Vk;
	int a;
	
	public ReservationStation(String n){
		this.name = n;
		this.busy = false;
		Qj=-1;
		Qk=-1;
		a = 0;
	}
	
	public String display(){
		String result = name + " " +  busy + " " + op + " " + Vj + " " + Vk + " " + Qj + " " + Qk + " " + a;
		return result;
	}
	
}
