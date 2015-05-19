import java.util.Arrays;

class simpleCA {
	
	int[] state1;
	int[] state2;
	int[] nextState;
	int[] G_state;
	static int cnt;

	static int round;

	simpleCA(String startConfig) {
		this.state1 = new int[startConfig.length()+4];
		this.state2 = new int[startConfig.length()+4];
		this.G_state = new int[startConfig.length()+4]; //Granularitätszustand
		this.nextState = new int[startConfig.length()+4];

		for (int i=0; i < state1.length; i++) {
			this.state1[i] = 0;
			this.G_state[i] = 0;
		}
		for (int i=0; i < startConfig.length(); i++) {
			this.state2[i+2] = Integer.parseInt( "" + startConfig.charAt(i) );
		}

		round = 0;
	}



	//wandelt eine Dezimalzahl in einen binären Regelsatz um, auf eine sehr murksige Art und Weise
	static int[] createRuleset(long decimal, int radius, int numStates) {
		//Die Werte sollten mit Null initiert sein
		//Anzahl Regeln = |numStates|**(2r+1)
		int numRules = (int)Math.pow(numStates, 2*radius+1) ;
		int[] ruleset = new int[numRules];

		String s = Long.toBinaryString(decimal);

		for(int i=0; i < s.length(); i++) {
			ruleset[ numRules - s.length() + i ] = Integer.parseInt( "" + s.charAt(i) );
		}
		//System.out.println( Arrays.toString(ruleset) );
		return ruleset;
	}

	public static int getBit(int n, int k) {
    	return (n >> k) & 1;
	}

	//bitwise modular addition (XOR) von 2 arrays 
	public static int[] arrayAdd(int[] a1, int[] a2) {
		int[] newArray = new int[a1.length];
		if (a1.length != a2.length) {
			System.out.println("Arrays nicht gleich lang!");
			return a1;
		}

		for(int i=0; i<a1.length; i++) {
			newArray[i] = (a1[i] + a2[i]) % 2;
		}
		return newArray;
	}

	public static void printState(int[] state) {
		for(int i=0; i < state.length; i++) {
			if(state[i] == 1) {
				System.out.print("#");
			} else if(state[i] == 0) {
				System.out.print("_");
			} else {
				//ups, da sollten aber nur Bits raus kommen!
				System.out.print(state[i]);
			}
		}
		System.out.println("");
	}

	/*deprecated
	//r=1, Zustände={0,1}
	public static int[] step(int[] state1, int[] state2) {
		int[] ruleset = new int[8];
		int[] nextState = new int[state1.length];

		for(int i=0; i < state1.length; i++) {
			String nb;
			int index;
			//Wie sieht die lokale Nachbarschaft aus?
			//Null Boundary Condition
			if (i == 0) { //kein linker Nachbar
				nb = "" + 0 + state2[i] + state2[i+1];
			} else if (i == state2.length - 1) { //kein rechter Nachbar
				nb = "" + state2[i-1] + state2[i] + 0; 
			} else { //normaler Fall
				nb = "" + state2[i-1] + state2[i] + state2[i+1];
			}
			index = Integer.parseInt(nb,2);

			if (state1[i] == 1) {
				ruleset = createRuleset(100, 1);
				nextState[i] = ruleset[index];
			} else { 
				ruleset = createRuleset(155, 1);
				nextState[i] = ruleset[index];
			}
		}
		return nextState;
	}
	*/

	//r=1, Zustände = {0,1,2,3}
	/*
	Input: Zustand bei t-1 für Zellen index + index+1
		   Zustand bei t
	Output: Zustand bei t+1 für Zelle index und index +1
	index: Das Element, was betrachtet werden soll
	*/
	public static int[] localstep_bulk (int[] state1, int[] state2, int index, int[] ruleset) {
		int[] nextState = new int[2];
		//int[] ruleset = createRuleset(rule, 1, 4); // |Zustände|**|Anzahl Elemente| => 4**3 = 64 bits
		//der Zustand einer bulky Zelle besteht aus 2-Bits
		//deshalb müssen wir erst 2-bits als Dezimalzahl interpretieren (eigentlich 4-näre Zahl)
		//und nach noch einen Wert weiter schauen, umso die richtige lokale Nachbarschaft zu erhalten
		int ownState =  Integer.parseInt( "" + state2[index] + state2[ index+1 ], 2);
		String nb = "" + state2[ index-1 ] + ownState + state2[ index+2 ];
		int ruleIndex = Integer.parseInt(nb, 4);

		int firstBit  = getBit(ruleset[ruleIndex], 0); //LSB
		int secondBit = getBit(ruleset[ruleIndex], 1);
		//????stimmt die Reihenfolge denn??????
		nextState[1] = (firstBit  + state1[ index   ]) % 2;
		nextState[0] = (secondBit + state1[ index+1 ]) % 2;

		return nextState; 
	}

	public static int localstep_fine (int[] state1, int[] state2, int index, int[] ruleset) {
		String nb = "" + state2[index-2] + state2[index-1] + state2[index] + state2[index+1] + state2[index+2];
		int ruleIndex = Integer.parseInt(nb, 2);
		//System.out.println(index + ": " + (ruleset[ruleIndex] + state1[index]) % 2);
		return (ruleset[ruleIndex] + state1[index]) % 2;
	}

	public static int[] globalstep(int[] state1, int[] state2, int[] G_state, long fineRule, long bulkRule) {
		int[] fineRuleset = createRuleset(fineRule, 2, 2); //radius=2, #states=2
		int[] newState = new int[state1.length];

		for (int i=2; i<state1.length-2; i++) {
			newState[i] = localstep_fine(state1, state2, i, fineRuleset);
		}

		return newState;
	}


	//!!!!!
	public static int[] G_state_step (int[] G_state, int[] state) {
		int[] newState = new int[G_state.length];
		for (int i=0; i<G_state.length; i++) {
			if(state[i]==1) {
				newState[i] = 1;
				i++;
			} 
		}
		return newState;
	}

	public static int[] G_state_step_null (int[] G_state) {
		int[] newState = new int[G_state.length];
		for (int i=0; i<G_state.length; i++) {
			newState[i] = 0;
		}
		return newState;
	}

	public static boolean compareArrays(int[] a, int[] b) {
		boolean same = true;
		if(a == null || b == null) {
			same = false;
		} else {
			if(a.length != b.length) {
				same = false;
			} else {
				for(int i=0; i<a.length; i++) {
					if(a[i] != b[i]) {
						same = false;
					}
				}
			}
		}
		return same;
	}

	public static void main(String[] args) {
		//simpleCA ca = new simpleCA("00000000000001000000000000000100000000000100000010000000000000");
		String plaintext = "00000000000000010000000000000000";
		simpleCA ca = new simpleCA(plaintext);
		int[] pt = ca.state2;

		System.out.println("Plaintext length: " + plaintext.length());
		long bulkRule = 0; //[0-2**64?]
		long fineRule = 429490029L - 1; 
		int[] lastState = new int[ca.state1.length];
		int[] nextToLastState = new int[ca.state1.length];
		int[] lastStateDec = new int[ca.state1.length];
		int[] nextToLastStateDec = new int[ca.state1.length];
		int maxRound = 10;


		/*Encrypt*/
		System.out.println("Encrypt");
		printState(ca.state1);
		printState(ca.state2);
		while (round < maxRound) {

			if (round % 2 == 0) {
				ca.nextState = globalstep(ca.state1, ca.state2, ca.G_state, fineRule, bulkRule);
				ca.state1 = ca.nextState;

				lastState = ca.state1;
				nextToLastState = ca.state2;
			} else {
				ca.nextState = globalstep(ca.state2, ca.state1, ca.G_state, fineRule, bulkRule);
				ca.state2 = ca.nextState;

				lastState = ca.state2;
				nextToLastState = ca.state1;
			}
			round++;
			printState(ca.nextState);
		}

		lastStateDec = lastState;
		nextToLastStateDec = nextToLastState;
		System.out.println("last lines");
		printState(nextToLastStateDec);
		printState(lastStateDec);

		//Decrypt without Key
		
		System.out.println("hack0r");
		round = 0;
		maxRound--;
		ca.state1 = pt;
		printState(ca.state1);

		//create state2 in a awesome way 
		//Annahme: Zellen nicht im Einflussbereich seien 0
		for (int i=0; i<ca.state2.length; i++) {
			ca.state2[i] = 0;
		}
		//Zellen im Einflussbereich bruteforcen
		//für 5-Bits haben wir 32 verschiedene Möglichkeiten
		for(int x=0; x<32; x++) {
			/*irgendwas hier ist kaputt*/
			for (int i=2; i<ca.state2.length-2; i++) {
				if(ca.state1[i] == 1) {
					ca.state2[i-2] = getBit(x, 4); //MSB
					ca.state2[i-1] = getBit(x, 3);
					ca.state2[ i ] = getBit(x, 2);
					ca.state2[i+1] = getBit(x, 1);
					ca.state2[i+2] = getBit(x, 0); //LSB
				}
			}
			System.out.print(x + "");
			printState(ca.state2);
			
			while (round < maxRound) {

				if (round % 2 == 0) {
					ca.nextState = globalstep(ca.state1, ca.state2, ca.G_state, fineRule, bulkRule);
					ca.state1 = ca.nextState;

					lastState = ca.state1;
					nextToLastState = ca.state2;
				} else {
					ca.nextState = globalstep(ca.state2, ca.state1, ca.G_state, fineRule, bulkRule);
					ca.state2 = ca.nextState;

					lastState = ca.state2;
					nextToLastState = ca.state1;
				}
				round++;
				//printState(ca.nextState);

			}

			//System.out.println(x + ": ");
			//printState(ca.state2);
			//printState(ca.state1);

			//System.out.println(
			//	compareArrays(nextToLastState, ca.state1) + " " + compareArrays(lastState,       ca.state2)  + " " +  
			//	compareArrays(lastState,       ca.state1) + " " + compareArrays(nextToLastState, ca.state2) );

		}	
		

		//Decrypt
		/*
		System.out.println("Decrypt");
		round = 0;
		maxRound = 10;
		ca.state1 = lastStateDec;
		ca.state2 = nextToLastStateDec;
		printState(ca.state1);
		printState(ca.state2);
		while (round < maxRound) {

			if (round % 2 == 0) {
				ca.nextState = globalstep(ca.state1, ca.state2, ca.G_state, fineRule, bulkRule);
				ca.state1 = ca.nextState;

				lastState = ca.state1;
				nextToLastState = ca.state2;
			} else {
				ca.nextState = globalstep(ca.state2, ca.state1, ca.G_state, fineRule, bulkRule);
				ca.state2 = ca.nextState;

				lastState = ca.state2;
				nextToLastState = ca.state1;
			}
			round++;
			printState(ca.nextState);
		}
		*/
	
	}
}