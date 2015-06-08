import java.util.Arrays;
import java.math.BigInteger;

class CA {
	
	int[] state1;
	int[] state2;
	int[] nextState;
	int[] plaintext;

	/*Granularitätszustand*/
	int[] gState;

	int[][] gStates;

	/*Die letzten beiden Zustände nach einer Berechnung (Verschlüsselung, Entschlüsselung, Angriffsversuch ...)*/
	int[] lastState;
	int[] nextToLastState;

	/*Die letzten beiden Zustände nach der Verschlüsselung*/
	int[] lastStateDec;
	int[] nextToLastStateDec;

	int[] gState_Dec;
	
	int rounds;

	String bulkRule;
	String fineRule;

	int[] fineRuleset;
	int[] bulkRuleset;

	CA(String startConfig, String fineRule, String bulkRule) {
		System.out.println("Plaintext length: " + startConfig.length() );
		this.state1 = new int[startConfig.length()+4];
		this.state2 = new int[startConfig.length()+4];
		this.gState = new int[startConfig.length()+4]; 
		this.nextState = new int[startConfig.length()+4];

		for (int i=0; i < state1.length; i++) {
			this.state1[i] = 0;
			this.gState[i] = 0;
		}
		for (int i=0; i < startConfig.length(); i++) {
			this.state2[i+2] = Integer.parseInt( "" + startConfig.charAt(i) );
		}

		this.fineRuleset = createRuleset(fineRule, 2, 2); //Radius=2, #Zustände=2
		this.bulkRuleset = createRuleset(bulkRule, 1, 4); //Radius=1, #Zustände=4

		//System.out.println(Arrays.toString(bulkRuleset) + bulkRuleset.length);
		this.rounds = 0;
		//System.out.println(Arrays.toString(fineRuleset) + fineRuleset.length);

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
		//nextState[0] = (secondBit + state1[ index+1 ]) % 2;
		nextState[0] = secondBit;

		return nextState; 
	}

	public static int localstep_fine (int[] state1, int[] state2, int index, int[] ruleset) {
		String nb = "" + state2[index-2] + state2[index-1] + state2[index] + state2[index+1] + state2[index+2];
		int ruleIndex = Integer.parseInt(nb, 2);

		return (ruleset[ruleIndex] + state1[index]) % 2;
	}

	public static int[] globalstep(int[] state1, int[] state2, int[] gState, int[] fineRuleset, int[] bulkRuleset) {
		int[] newState = new int[state1.length];

		for(int i=2; i<state1.length-2; i++) {

			/*Sondefall: wenn die letzte Zelle fette Granularität haben sollte, dann interpretiere sie nur als dünne Zelle*/
			if (i == state1.length-3 ){
				newState[i] = localstep_fine(state1, state2, i, fineRuleset);
			} else if ( (gState[i] == 1 && gState[i+1] == 1) || (gState[i] == 0 && gState[i+1] == 0) ) { //bulky cell
				int[] tmp = localstep_bulk(state1, state2, i , bulkRuleset);
				newState[ i ] = tmp[0];
				newState[i+1] = tmp[1];
				i++;
			} else { //fine cell
				newState[i] = localstep_fine(state1, state2, i, fineRuleset);
			}
		}

		return newState;
	}

	public static int[] gState_step (int[] gState, int[] state) {
		int[] newState = new int[gState.length];

		for (int i=0; i<gState.length; i++) {
			if(state[i]==1) {
				newState[i] = 1;
				//newState[i+1] = 99;
				i++;
			} 
		}
		return newState;
	}

	public static int[] gState_step2 (int[] gState, int[] state) {
		int[] newState = new int[gState.length];

		if (gState.length != state.length) {
			System.out.println("gState_step2: unequal length");
			Arrays.fill(newState, -1);
			return newState;
		}

		//System.out.println(Arrays.toString(state));
		for(int i=0; i<gState.length; i++) {
			newState[i] = (gState[i] + state[i]) % 2;
		}
		return newState;
	}

	public void encrypt(boolean output) {
		gStates = new int[gState.length][rounds];

		int round = 0;

		if (output) {
			System.out.println("Encryption: ");
			printState(state1);
			printState(state2, gState);
		}
		nextState = Arrays.copyOf(state2, state2.length);
		
		while (round < rounds) {
			/*Granularity Change*/
			gState = gState_step2(gState, nextState);
			/*State Change*/
			if (round % 2 == 0) {
				nextState = globalstep(state1, state2, gState, fineRuleset, bulkRuleset);
				state1 = Arrays.copyOf(nextState, nextState.length);

				lastState 		= Arrays.copyOf(state1, state1.length);
				nextToLastState = Arrays.copyOf(state2, state2.length);
			} else {
				nextState = globalstep(state2, state1, gState, fineRuleset, bulkRuleset);
				state2 = Arrays.copyOf(nextState, nextState.length);

				lastState 		= Arrays.copyOf(state2, state2.length);
				nextToLastState = Arrays.copyOf(state1, state1.length);
			}

			//gStates[round] = Arrays.copyOf(gState, gState.length);

			round++;

			if(output) {
				//jeden Zustand ausgeben
				//System.out.print(round);
				printState(nextState, gState);
			}
		}

		/*Die letzten beiden Zustände nach der Verschlüsselung sichern aka. die Chiffrate*/
		lastStateDec = Arrays.copyOf(lastState, lastState.length);
		nextToLastStateDec = Arrays.copyOf(nextToLastState, nextToLastState.length);
		gState_Dec = Arrays.copyOf(gState, gState.length);

		if(false) {
			System.out.println("last lines");
			printState(nextToLastStateDec);
			printState(lastStateDec);

			/*System.out.println("gStates");
			for (int i=0; i<rounds; i++) {
				printState(gStates[i]);
			}*/
		}
	}

	public void decrypt() {
		System.out.println("Decryption:");

		int round = 0;
		//Chiffrate einfügen
		state1 = Arrays.copyOf(lastStateDec, lastStateDec.length);
		state2 = Arrays.copyOf(nextToLastStateDec, nextToLastStateDec.length);
		gState = Arrays.copyOf(gState_Dec, gState_Dec.length);

		int[] gState2 = gState_step2(gState, lastStateDec);
		printState(state1, gState);
		printState(state2, gState2);

		//printState(nextState);
		Arrays.fill(nextState, 0); 


		while (round < rounds) {
			/*State Change*/
			if (round == 0 ) {

				/*BUG*/
				//gState == G_n
				gState = gState_step2(gState_Dec, lastStateDec);
				//gState == G_n-1
				gState = gState_step2(gState, nextToLastStateDec);
				//gState == G_n-2

				//printState(gState);

				//nextState == C_n-2 = F(C_n, C_n-1, G_n-2)
				nextState = globalstep(state1, state2, gState, fineRuleset, bulkRuleset);
				state1 	= Arrays.copyOf(nextState, nextState.length);

				lastState 		= Arrays.copyOf(state1, state1.length);
				nextToLastState = Arrays.copyOf(state2, state2.length);

				//gState = Arrays.copyOf(gState_Dec, gState_Dec.length);
			} else if (round % 2 == 0) {
				gState = gState_step2(gState, nextState);

				nextState = globalstep(state1, state2, gState, fineRuleset, bulkRuleset);
				state1 	  = Arrays.copyOf(nextState, nextState.length);

				lastState 		= Arrays.copyOf(state1, state1.length);
				nextToLastState = Arrays.copyOf(state2, state2.length);
			} else {
				gState = gState_step2(gState, nextState);
				nextState = globalstep(state2, state1, gState, fineRuleset, bulkRuleset);
				state2 	  = Arrays.copyOf(nextState, nextState.length);

				lastState 		= Arrays.copyOf(state2, state2.length);
				nextToLastState = Arrays.copyOf(state1, state1.length);
			}

			if(round == 0) {
//				printState(nextState, gState_step2(gState, state2) );
				//System.out.print(round);
				printState(nextState, gState);
			} else {
				//System.out.print(round);
				printState(nextState, gState);
			}

//			printState(gState);
			//gState = Arrays.copyOf(gStates[rounds - round -1], gState.length);
			round++;
//			printState(nextState, gState);
		}
	}

	public static void main(String[] args) {
		String br = "34028236692093843463374605431768211455"; //[0-2**64]
		String fr = "429490029"; //[0-2**32]

		//fr = "";
		br = "18446744073709551615";
		br = "340282366920938463463374607431768211455";
		fr = "0";
		CA ca = new CA("000000000001000000000000", fr, br);
		//CA ca = new CA("1010101011100001010101", fr, br);

		ca.rounds = 3;
		
		ca.encrypt(true);
		ca.decrypt();
	}


}