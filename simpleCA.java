import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

class simpleCA {
	
	int[] state1;
	int[] state2;
	int[] nextState;
	int[] plaintext;

	/*Die letzten beiden Zustände nach einer Berechnung (Verschlüsselung, Entschlüsselung, Angriffsversuch ...)*/
	int[] lastState;
	int[] nextToLastState;

	/*Die letzten beiden Zustände nach der Verschlüsselung*/
	int[] lastStateDec;
	int[] nextToLastStateDec;

	/*
	* Eine Konfiguration des CA arbeitet auf der Länge n = Länge des Plaintext
	* Es werden aber links und recht jeweils 2 weitere Zellen hinzugefügt (da wir hier mit Radius=2 arbeiten),
	* um häßliche if-then Abfragen beim Zugriff auf überstehende Indizes zu vermeiden
	*/
	simpleCA(String startConfig) {
		this.state1 = new int[startConfig.length()+4];
		this.state2 = new int[startConfig.length()+4];
		this.nextState = new int[startConfig.length()+4];
		this.lastState = new int[startConfig.length()+4];
		this.nextToLastState = new int[startConfig.length()+4];
		this.plaintext = new int[startConfig.length()+4];

		for (int i=0; i < state1.length; i++) {
			this.state1[i] = 0;
		}
		for (int i=0; i < startConfig.length(); i++) {
			this.state2[i+2] = Integer.parseInt( "" + startConfig.charAt(i) );
			this.plaintext[i+2] = Integer.parseInt( "" + startConfig.charAt(i) );
		}
	}

	simpleCA(int[] s1, int[] s2) {
		this.state1 = new int[s1.length];
		this.state2 = new int[s1.length];
		this.nextState = new int[s1.length];
		this.lastState = new int[s1.length];
		this.nextToLastState = new int[s1.length];
		this.plaintext = new int[s1.length];

		for(int i=0; i<s1.length; i++) {
			this.state1[i] = s1[i];
			this.state2[i] = s2[i];
			this.plaintext[i] = s2[i];
		}
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

	// k=0 ==> LSB 
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

	public static boolean compareArraysAt(int[] a, int[] b,int index, int radius) {
		boolean same = true;
		if(a == null || b == null) {
			same = false;
		} else {
			if(a.length != b.length) {
				same = false;
			} else {
				for(int i=index-radius; i<index+radius; i++) {
					if(a[i] != b[i]) {
						same = false;
					}
				}
			}
		}
		return same;
	}

	public static int localstep (int[] state1, int[] state2, int index, int[] ruleset) {
		String nb = "" + state2[index-2] + state2[index-1] + state2[index] + state2[index+1] + state2[index+2];
		int ruleIndex = Integer.parseInt(nb, 2);
		//System.out.println(index + ": " + (ruleset[ruleIndex] + state1[index]) % 2);
		return (ruleset[ruleIndex] + state1[index]) % 2;
	}

	public static int[] globalstep(int[] state1, int[] state2, long fineRule) {
		int[] fineRuleset = createRuleset(fineRule, 2, 2); //radius=2, #states=2
		int[] newState = new int[state1.length];

		for (int i=2; i<state1.length-2; i++) {
			newState[i] = localstep(state1, state2, i, fineRuleset);
		}
		return newState;
	}

	public void encrypt(int rounds, long rule, boolean output) {
		int round = 0;
		if (output) {
			System.out.println("Encryption: ");
			//die beiden Eingabewerte
			printState(state1);
			printState(state2);
		}
		while (round < rounds) {
			if (round % 2 == 0) {
				nextState = globalstep(state1, state2, rule);
				state1 = Arrays.copyOf(nextState, nextState.length);

				lastState = Arrays.copyOf(state1, state1.length);
				nextToLastState = Arrays.copyOf(state2, state2.length);
			} else {
				nextState = globalstep(state2, state1, rule);
				state2 = Arrays.copyOf(nextState, nextState.length);

				lastState = Arrays.copyOf(state2, state2.length);
				nextToLastState = Arrays.copyOf(state1, state1.length);
			}
			round++;
			if(output) {
				//jeden Zustand ausgeben
				//System.out.print(round);
				printState(nextState);
			}
		}

		//Die letzten beiden Zustände nach der Verschlüsselung sichern aka Chiffrate
		lastStateDec = Arrays.copyOf(lastState, lastState.length);
		nextToLastStateDec = Arrays.copyOf(nextToLastState, nextToLastState.length);
		if(output) {
			System.out.println("last lines");
			printState(nextToLastStateDec);
			printState(lastStateDec);
		}
	}

	public void decrypt(int rounds, long rule) {
		System.out.println("Decryption:");
		int round = 0;
		//Chiffrate einfügen
		state1 = Arrays.copyOf(lastStateDec, lastStateDec.length);
		state2 = Arrays.copyOf(nextToLastStateDec, nextToLastStateDec.length);
		printState(state1);
		printState(state2);
		while (round < rounds) {
			if (round % 2 == 0) {
				nextState = globalstep(state1, state2, rule);
				state1 = Arrays.copyOf(nextState, nextState.length);

				lastState = Arrays.copyOf(state1, state1.length);
				nextToLastState = Arrays.copyOf(state2, state2.length);
			} else {
				nextState = globalstep(state2, state1, rule);
				state2 = Arrays.copyOf(nextState, nextState.length);

				lastState = Arrays.copyOf(state2, state2.length);
				nextToLastState = Arrays.copyOf(state1, state1.length);
			}
			round++;
			printState(nextState);
		}
	}

	public void attack(int rounds, long rule) {
		Set<Integer> candidates = new HashSet<Integer>();
		
		System.out.println("hack0r:");
		int round = 0;

		state1 = Arrays.copyOf(plaintext, plaintext.length);
	
		//Zellen im Einflussbereich bruteforcen
		//für 5-Bits haben wir 32 verschiedene Möglichkeiten
		for(int x=0; x<32; x++) {
			//state1 auf plaintext zurücksetzen
			state1 = Arrays.copyOf(plaintext, plaintext.length);

			/*Die Zellen im Radius eines 1er Wertes bruteforcen*/
			//Annahme: Zellen nicht im Einflussbereich seien 0
			for (int i=0; i<state2.length; i++) {
				state2[i] = 0;
			}

			for (int i=2; i<state2.length-2; i++) {
				if(state1[i] == 1) {
					state2[i-2] = getBit(x, 4); //MSB
					state2[i-1] = getBit(x, 3);
					state2[ i ] = getBit(x, 2);
					state2[i+1] = getBit(x, 1);
					state2[i+2] = getBit(x, 0); //LSB
				}
			}
			//System.out.print(x + "");
			//printState(state2);
			/*
			System.out.println("C1+C2 @ " + x);
			printState(state1);
			printState(state2); 
			*/
			//Jeden Kandidaten 1-32 verschlüsseln wir
			round = 0;

			while (round < rounds) {
				if (round % 2 == 0) {
					nextState = globalstep(state1, state2, rule);
					state1 = Arrays.copyOf(nextState, nextState.length);

					lastState = Arrays.copyOf(state1, state1.length);
					nextToLastState = Arrays.copyOf(state2, state2.length);
				} else {
					nextState = globalstep(state2, state1, rule);
					state2 = Arrays.copyOf(nextState, nextState.length);

					lastState = Arrays.copyOf(state2, state2.length);
					nextToLastState = Arrays.copyOf(state1, state1.length);
				}
				round++;
				//Jeden Zustand beim Verschlüsseln ausgeben
				//System.out.print(round) ;
				//printState(nextState);

			}

			//Nur die Endzustände ausgeben
			/*System.out.println(x + ": ");
			printState(state1);
			printState(state2);*/
			

			/*System.out.println("last lines after Enc()");
			printState(nextToLastStateDec);
			printState(lastStateDec);*/
			/*if (compareArrays(nextToLastStateDec, state1) ||
			    compareArrays(lastStateDec,       state2) ||
				compareArrays(lastStateDec,       state1) ||
				compareArrays(nextToLastStateDec, state2) ) 
			{
				System.out.println("Success! Kandidat: " + x);
			}*/

			for (int i=2; i<state2.length-2; i++) {
					if (compareArrays(lastStateDec, state1) )					
					{
						/*System.out.println("lastState");
						printState(lastStateDec);
						System.out.println("S1");
						printState(state1);

						*/
						//System.out.println("Success! Kandidat: " + x);
						candidates.add(i);
						/*System.out.println("s1");
						printState(state1);
						System.out.println("s2");
						printState(state2);*/
					}
			}
			
		}	
		System.out.print("Candidates: ");
		for (Integer i : candidates) {
				System.out.print(i + " ");
		}
		System.out.println("");
	}

	public static void attack2(int rounds, long rule, int[] plaintext, int[] lastState) {
		Set<Integer> candidates = new HashSet<Integer>();

		//cas= Cellular AutomataS /*TODO bessere Bezeichnung finden*/
		simpleCA[] cas0 = new simpleCA[32];
		simpleCA[] cas1 = new simpleCA[32];
		//custom input, beeinflußbarer Bereich wird brutegeforced, der Rest ist konstant (0 oder 1)
		int[] input0 = new int[plaintext.length];
		int[] input1 = new int[plaintext.length];

		for(int c=0; c<cas0.length; c++) {

			/*Init der beiden Eingabewerte*/
			//S1: 	0------010-------0
			//S2:   0-----XXXXX------0
			for(int j=0; j<input0.length /*+4?*/; j++) {
				//Annahme: nicht beeinflußte Bits seien Null bzw. Eins
				input0[j] = 0;
				input1[j] = 1;
			}

			//"1er" Bereich bruteforcen
			for(int x=2; x<input0.length-2; x++) {
				if(plaintext[x] == 1) {
					input0[x-2] = getBit(c, 4); //MSB
					input0[x-1] = getBit(c, 3);
					input0[ x ] = getBit(c, 2);
					input0[x+1] = getBit(c, 1);
					input0[x+2] = getBit(c, 0); //LSB

					input1[x-2] = getBit(c, 4); //MSB
					input1[x-1] = getBit(c, 3);
					input1[ x ] = getBit(c, 2);
					input1[x+1] = getBit(c, 1);
					input1[x+2] = getBit(c, 0); //LSB
				}
			}
			cas0[c] = new simpleCA(plaintext, input0);
			cas1[c] = new simpleCA(plaintext, input1);

			/*Verschlüsselung der Kandidaten*/
			cas0[c].encrypt(rounds, rule, false);
			cas1[c].encrypt(rounds, rule, false);

			/*Überprüfung ob letzte Zustand der Verschlüsselung mit dem vorletzten Zustand der Kandidaten übereinstimmt*/
			if ( compareArrays(cas0[c].nextToLastState, lastState) == true || 
			 	 compareArrays(cas1[c].nextToLastState, lastState) == true) {
				candidates.add(c);
				System.out.println("Kandidat " + c);
			}
		}

	}

	public static void main(String[] args) {
		//simpleCA ca = new simpleCA("00000000000001000000000000000100000000000100000010000000000000");
		String plaintext = "000000000100000000000";
		//plaintext = "01010101010101010101010101010101";
		
		simpleCA ca = new simpleCA(plaintext);

		System.out.println("Plaintext length: " + plaintext.length());
		long rule = 429490029L - 1; //[0, 2^32-1]
		//rule = 2147483648L ; // = 2^31
		//rule = 0;
		int maxRound = 16;

		/*---------------------------------------------------------*/
		/*-----------------------Encrypt*--------------------------*/
		/*---------------------------------------------------------*/
		ca.encrypt(maxRound, rule, true);
	
		/*---------------------------------------------------------*/
		/*-----------------------Attack----------------------------*/
		/*---------------------------------------------------------*/
		//Da wir bei dem Angriff die erste Eingabezeile (nur Nullen) weglassen,
		//müssen wir auch nur eine Runde weniger verschlüsseln
	//	ca.attack(maxRound, rule);

		attack2(maxRound, rule, ca.plaintext, ca.lastState);

		/*---------------------------------------------------------*/
		/*-----------------------Decrypt*--------------------------*/
		/*---------------------------------------------------------*/		
		ca.decrypt(maxRound, rule);
	
	}
}