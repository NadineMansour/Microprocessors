
public class ReservationStation {
	String name;
	boolean busy;
	String op , Vj , Vk , Qj , Qk;
	int a;
	
	public ReservationStation(String n){
		this.name = n;
		this.busy = false;
		a = 0;
	}
	
	public String display(){
		String result = name + " " +  busy + " " + op + " " + Vj + " " + Vk + " " + Qj + " " + Qk + " " + a;
		return result;
	}
	
}
