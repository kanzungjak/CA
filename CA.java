import java.util.Arrays;
import java.math.BigInteger;

class CA {
	
	int[] state1;
	int[] state2;
	int[] nextState;
	int[] plaintext;
	int[] G_state;

	/*Die letzten beiden Zustände nach einer Berechnung (Verschlüsselung, Entschlüsselung, Angriffsversuch ...)*/
	int[] lastState;
	int[] nextToLastState;

	/*Die letzten beiden Zustände nach der Verschlüsselung*/
	int[] lastStateDec;
	int[] nextToLastStateDec;

	int[] G_state_Dec;
	
	int rounds;

	String bulkRule;
	String fineRule;

	int[] fineRuleset;
	int[] bulkRuleset;

	CA(String startConfig, String fineRule, String bulkRule) {
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

		this.fineRuleset = createRuleset(fineRule, 2, 2);
		this.bulkRuleset = createRuleset(bulkRule, 1, 4);

		this.rounds = 0;
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

	//für große, große Zahlen
	static int[] createRuleset(String decimal, int radius, int numStates) {
		//Die Werte sollten mit Null initiert sein
		//Anzahl Regeln = |numStates|**(2r+1)
		int numRules = (int)Math.pow(numStates, 2*radius+1) ;
		int[] ruleset = new int[numRules];
		BigInteger num = new BigInteger(decimal, 10);

		String s = num.toString(numStates);
		
		for(int i=0; i < s.length(); i++) {
			ruleset[ numRules - s.length() + i ] = Integer.parseInt( "" + s.charAt(i) );
		}
		//System.out.println( Arrays.toString(ruleset) + " Length: " + ruleset.length);

		return ruleset;
	}

	public static int getBit(int n, int k) {
    	return (n >> k) & 1;
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

	public static void printState(int[] state, int[] state2) {
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

		System.out.print(" ");

		for(int i=0; i < state2.length; i++) {
			if(state2[i] == 1) {
				System.out.print("#");
			} else if(state2[i] == 0) {
				System.out.print("_");
			} else {
				//ups, da sollten aber nur Bits raus kommen!
				System.out.print(state2[i]);
			}
		}
		System.out.println("");
	}

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

		return (ruleset[ruleIndex] + state1[index]) % 2;
	}

	public static int[] globalstep(int[] state1, int[] state2, int[] G_state, int[] fineRuleset, int[] bulkRuleset) {
		int[] newState = new int[state1.length];

		for(int i=2; i<state1.length-2; i++) {

			if (G_state[i] == 1) { //bulky cell
				int[] tmp = localstep_bulk(state1, state2, i , bulkRuleset);
				newState[ i ] = tmp[0];
				newState[i+1] = tmp[1];
				i++;
			} else { //fine cell
				//if (G_state[i-1] == 0) {
					newState[i] = localstep_fine(state1, state2, i, fineRuleset);
				//}
			}
		
		}

		return newState;
	}

	public static int[] G_state_step (int[] G_state, int[] state) {
		int[] newState = new int[G_state.length];

		for (int i=0; i<G_state.length; i++) {
			if(state[i]==1) {
				newState[i] = 1;
				//newState[i+1] = 99;
				i++;
			} 
		}
		return newState;
	}

	public void encrypt(boolean output) {
		int round = 0;
		if (output) {
			System.out.println("Encryption: ");
			//die beiden Eingabewerte
			//printState(state1);
			//printState(state2);

			printState(state1, G_state);
			printState(state2, G_state);
		}
		while (round < rounds) {
			if (round % 2 == 0) {
				nextState = globalstep(state1, state2, G_state, fineRuleset, bulkRuleset);
				state1 = Arrays.copyOf(nextState, nextState.length);

				lastState = Arrays.copyOf(state1, state1.length);
				nextToLastState = Arrays.copyOf(state2, state2.length);
			} else {
				nextState = globalstep(state2, state1, G_state, fineRuleset, bulkRuleset);
				state2 = Arrays.copyOf(nextState, nextState.length);

				lastState = Arrays.copyOf(state2, state2.length);
				nextToLastState = Arrays.copyOf(state1, state1.length);
			}
			G_state = G_state_step(G_state, nextState);
			round++;
			if(output) {
				//jeden Zustand ausgeben
				//System.out.print(round);
				//printState(nextState);
				printState(nextState, G_state);
			}
		}

		//Die letzten beiden Zustände nach der Verschlüsselung sichern aka Chiffrate
		lastStateDec = Arrays.copyOf(lastState, lastState.length);
		nextToLastStateDec = Arrays.copyOf(nextToLastState, nextToLastState.length);
		G_state_Dec = Arrays.copyOf(G_state, G_state.length);
		if(output) {
			System.out.println("last lines");
			printState(nextToLastStateDec);
			printState(lastStateDec);
		}
	}

	public void decrypt() {
		System.out.println("Decryption:");
		int round = 0;
		//Chiffrate einfügen
		state1 = Arrays.copyOf(lastStateDec, lastStateDec.length);
		state2 = Arrays.copyOf(nextToLastStateDec, nextToLastStateDec.length);
		G_state = Arrays.copyOf(G_state_Dec, G_state_Dec.length);

		Arrays.fill(G_state, 0);

		printState(state1, G_state);
		printState(state2, G_state);
		while (round < rounds) {
			if (round % 2 == 0) {
				nextState = globalstep(state1, state2, G_state, fineRuleset, bulkRuleset);
				state1 = Arrays.copyOf(nextState, nextState.length);

				lastState = Arrays.copyOf(state1, state1.length);
				nextToLastState = Arrays.copyOf(state2, state2.length);
			} else {
				nextState = globalstep(state2, state1, G_state, fineRuleset, bulkRuleset);
				state2 = Arrays.copyOf(nextState, nextState.length);

				lastState = Arrays.copyOf(state2, state2.length);
				nextToLastState = Arrays.copyOf(state1, state1.length);
			}
			G_state = G_state_step(G_state, nextState);
			round++;
			printState(nextState, G_state);
		}
	}

	public static void main(String[] args) {
		String br = "34028236692093843463374605431768211455"; //[0-2**64?]
		//br = "0" ;
		String fr = "429490029"; //[0-2**32]
		fr = "0";
		br = "0";
		CA ca = new CA("0000011100000010000000000000", fr, br);

		ca.rounds = 8;
		
		ca.encrypt(true);

		ca.decrypt();
	}

	/*
	TODO
	schauen/aufschreiben welche Regeln additiv sind oder einfache Muster erzeugen (links laufen etc.)

	*/

}