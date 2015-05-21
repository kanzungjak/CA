import java.util.Arrays;

class simpleCA {
	
	int[] state1;
	int[] state2;
	int[] nextState;
	static int cnt;
	static int round;

	/*
	* Eine Konfiguration des CA arbeitet auf der Länge n = Länge des Plaintext
	* Es werden aber links und recht jeweils 2 weitere Zellen hizugefügt,
	* um häßliche if-then Abfragen beim Zugriff auf überstehende Indizes zu vermeiden
	*/
	simpleCA(String startConfig) {
		this.state1 = new int[startConfig.length()+4];
		this.state2 = new int[startConfig.length()+4];
		this.nextState = new int[startConfig.length()+4];

		for (int i=0; i < state1.length; i++) {
			this.state1[i] = 0;
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

	public static void main(String[] args) {
		//simpleCA ca = new simpleCA("00000000000001000000000000000100000000000100000010000000000000");
		String plaintext = "00000000000000010000000000000000";
		simpleCA ca = new simpleCA(plaintext);
		int[] pt = Arrays.copyOf(ca.state2, ca.state2.length);

		System.out.println("Plaintext length: " + plaintext.length());
		long fineRule = 429490029L - 1; 

		//Die letzten beiden Zustände nach einer Berechnung (Verschlüsselung, Entschlüsselung, Angriffsversuch ...)
		int[] lastState = new int[ca.state1.length];
		int[] nextToLastState = new int[ca.state1.length];

		//Die letzten beiden Zustände nach der Verschlüsselung
		int[] lastStateDec = new int[ca.state1.length];
		int[] nextToLastStateDec = new int[ca.state1.length];

		int maxRound = 10;

		/*---------------------------------------------------------*/
		/*-----------------------Encrypt*--------------------------*/
		/*---------------------------------------------------------*/
		System.out.println("Encryption:");
		printState(ca.state1);
		printState(ca.state2);
		while (round < maxRound) {
			if (round % 2 == 0) {
				ca.nextState = globalstep(ca.state1, ca.state2, fineRule);
				ca.state1 = Arrays.copyOf(ca.nextState, ca.nextState.length);

				lastState = Arrays.copyOf(ca.state1, ca.state1.length);
				nextToLastState = Arrays.copyOf(ca.state2, ca.state2.length);
			} else {
				ca.nextState = globalstep(ca.state2, ca.state1, fineRule);
				ca.state2 = Arrays.copyOf(ca.nextState, ca.nextState.length);

				lastState = Arrays.copyOf(ca.state2, ca.state2.length);
				nextToLastState = Arrays.copyOf(ca.state1, ca.state1.length);
			}
			round++;
			//jeden Zustand ausgeben
			//System.out.print(round);
			printState(ca.nextState);
		}

		//Die letzten beiden Zustände nach der Verschlüsselung sichern
		lastStateDec = Arrays.copyOf(lastState, lastState.length);
		nextToLastStateDec = Arrays.copyOf(nextToLastState, nextToLastState.length);
		/*System.out.println("last lines");
		printState(nextToLastStateDec);
		printState(lastStateDec);*/

		/*-----------------------------------------------------------------*/
		/*-----------------------Decrypt without Key-----------------------*/
		/*-----------------------------------------------------------------*/
		System.out.println("hack0r:");
		round = 0;

		//Da wir bei dem Angriff die erste Eingabe Zeile (nur Nullen),
		//müssen wir auch nur eine Runde weniger verschlüsseln
		maxRound--;

		//unser selbst gewählte Eingabe (pt = plaintext)
		/*System.out.println("p ");
		printState(pt);*/
		ca.state1 = Arrays.copyOf(pt, pt.length);
		
		/*System.out.println("state1");
		printState(ca.state1);*/

		//Zellen im Einflussbereich bruteforcen
		//für 5-Bits haben wir 32 verschiedene Möglichkeiten
		for(int x=0; x<32; x++) {
			//state1 auf plaintext zurücksetzen
			ca.state1 = Arrays.copyOf(pt, pt.length);

			/*Die Zellen im Radius eines 1er Wertes bruteforcen*/
			//Annahme: Zellen nicht im Einflussbereich seien 0
			for (int i=0; i<ca.state2.length; i++) {
				ca.state2[i] = 0;
			}

			for (int i=2; i<ca.state2.length-2; i++) {
				if(ca.state1[i] == 1) {
					ca.state2[i-2] = getBit(x, 4); //MSB
					ca.state2[i-1] = getBit(x, 3);
					ca.state2[ i ] = getBit(x, 2);
					ca.state2[i+1] = getBit(x, 1);
					ca.state2[i+2] = getBit(x, 0); //LSB
				}
			}
			//System.out.print(x + "");
			//printState(ca.state2);
			/*
			System.out.println("C1+C2 @ " + x);
			printState(ca.state1);
			printState(ca.state2); 
			*/

			//Jeden Kandidaten 1-32 verschlüsseln wir
			round = 0;
			while (round < maxRound) {
				if (round % 2 == 0) {
					ca.nextState = globalstep(ca.state1, ca.state2, fineRule);
					ca.state1 = Arrays.copyOf(ca.nextState, ca.nextState.length);

					lastState = Arrays.copyOf(ca.state1, ca.state1.length);
					nextToLastState = Arrays.copyOf(ca.state2, ca.state2.length);
				} else {
					ca.nextState = globalstep(ca.state2, ca.state1, fineRule);
					ca.state2 = Arrays.copyOf(ca.nextState, ca.nextState.length);

					lastState = Arrays.copyOf(ca.state2, ca.state2.length);
					nextToLastState = Arrays.copyOf(ca.state1, ca.state1.length);
				}
				round++;
				//Jeden Zustand beim Verschlüsseln ausgeben
				//System.out.print(round) ;
				/*printState(ca.nextState);*/

			}

			/*/Nur die Endzustände ausgeben
			System.out.println(x + ": ");
			printState(ca.state2);
			printState(ca.state1);*/


			/*System.out.println("last lines after Enc()");
			printState(nextToLastStateDec);
			printState(lastStateDec);*/
			if (compareArrays(nextToLastStateDec, ca.state1) ||
			    compareArrays(lastStateDec,       ca.state2) ||
				compareArrays(lastStateDec,       ca.state1) ||
				compareArrays(nextToLastStateDec, ca.state2) ) 
			{
				System.out.println("Kandidat " + x);
			}
			
		}	
		

		/*---------------------------------------------------------*/
		/*-----------------------Decrypt*--------------------------*/
		/*---------------------------------------------------------*/		
		/*
		System.out.println("Decryption:");
		round = 0;
		maxRound = 10;

		ca.state1 = Arrays.copyOf(lastStateDec, lastStateDec.length);
		ca.state2 = Arrays.copyOf(nextTolastStateDec, nextTolastStateDec.length);
		printState(ca.state1);
		printState(ca.state2);
		while (round < maxRound) {

			if (round % 2 == 0) {
				ca.nextState = globalstep(ca.state1, ca.state2, fineRule);
				ca.state1 = Arrays.copyOf(ca.nextState, ca.nextState.length);

				lastState = Arrays.copyOf(ca.state1, ca.state1.length);
				nextToLastState = Arrays.copyOf(ca.state2, ca.state2.length);
			} else {
				ca.nextState = globalstep(ca.state2, ca.state1, fineRule);
				ca.state2 = Arrays.copyOf(ca.nextState, ca.nextState.length);

				lastState = Arrays.copyOf(ca.state2, ca.state2.length);
				nextToLastState = Arrays.copyOf(ca.state1, ca.state1.length);
			}
			round++;
			printState(ca.nextState);
		}
		*/
	
	}
}