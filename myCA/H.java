package myCA;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.io.*;

public class H {

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

	public static String stateToString(int[] state) {
		String out = "";
		String ONE = "1,";
		String ZERO = "0,";

		for(int i=0; i < state.length; i++) {
			if(state[i] == 1) {
				out += ONE;
			} else if(state[i] == 0) {
				out += ZERO;
			} else {
				//ups, da sollten aber nur Bits raus kommen!
				System.out.print(state[i]);
			}
		}
		out += "\n";
		return out;
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

	public static String stateToString(int[] state, int[] state2) {
		String ONE = "1,";
		String ZERO = "0,";

		String out = "";
		for(int i=0; i < state.length; i++) {
			if(state[i] == 1) {
				out += ONE;
			} else if(state[i] == 0) {
				out += ZERO;
			} else {
				//ups, da sollten aber nur Bits raus kommen!
				System.out.print(state[i]);
			}
		}

		out += " | ";

		for(int i=0; i < state2.length; i++) {
			if(state2[i] == 1) {
				out += ONE;
			} else if(state2[i] == 0) {
				out += ZERO;
			} else {
				//ups, da sollten aber nur Bits raus kommen!
				System.out.print(state2[i]);
			}
		}
		out += "\n";

		return out;
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

	public static int[] StringToInt(String in) {
		int[] out = new int[ in.length() ];
		for (int i=0; i<in.length(); i++) {
			out[i] = Integer.parseInt( "" + in.charAt(i) );
		}

		return out;
	}

	public static int[] valToIntArray(long in) {
		int[] out = new int[64];
		for(int i=0; i<64; i++) {
		//	out[i] = (in & (1<<i)) ? 1 : 0;
		}
		return out;
	}

	public static int localstep_fine (int[] state1, int[] state2, int index, int[] ruleset) {
		int l = state1.length;

		String nb = "";
		for (int i=-2; i<=2; i++) {
			nb += state2[ (index+i+l) % l ];
		}

		//String nb = "" + state2[index-2] + state2[index-1] + state2[index] + state2[index+1] + state2[index+2];
		int ruleIndex = Integer.parseInt(nb, 2);

		//usedKeysF[ruleIndex] = 1;

		return (ruleset[ruleIndex] + state1[index]) % 2;
	}

	//braucht nur 16 Einträge, da wir aber c={0,1,2,3} erhalten können ist die
	//effektive Schlüssellänge in Bits: 4**16 = 2**32 Möglichkeiten => 32 Bits
	public static int[] localstep_bulk (int[] state1, int[] state2, int index, int[] ruleset) {
		int[] nextState = new int[2];
		int l = state1.length;

 		String nb = "" + state2[(index-1+l)%l] + state2[index] + state2[ (index+1)%l ] + state2[ (index+2)%l ];
		int ruleIndex = Integer.parseInt(nb, 2);

		/*if(ruleIndex==15) { //DEBUG
			System.out.println("ruleIndex" +  ruleIndex);
			System.out.println("rule "+ ruleset[ruleIndex]);
			System.out.println("s1 " +  state1[index] +  state1[index+1]);
			System.out.println("bits " + getBit(ruleset[ruleIndex], 1) +getBit(ruleset[ruleIndex], 0) );
		}*/

		//usedKeysB[ruleIndex] = 1;

		int MSB = getBit(ruleset[ruleIndex], 1);
		int LSB = getBit(ruleset[ruleIndex], 0);

		nextState[0] = (MSB + state1[ index   ]) % 2;
		nextState[1] = (LSB + state1[ index+1 ]) % 2;

		/*
		System.out.println(ruleIndex);
		System.out.println(nb);
		if (ruleIndex == 7) {
			System.out.println("***********************");
			System.out.println("ruleIndex " +ruleIndex );
			System.out.println("ruleset " + ruleset[ruleIndex] );
			System.out.println("***********************");
		}


		
		if(ruleIndex==14) { //DEBUG
			System.out.println("+++++++++++++++++++++++++++");
			System.out.println("ruleindex " + ruleIndex);
			System.out.println("at i = " + index);
			System.out.println("nb " + nb );
			System.out.println("Bits " + MSB + LSB);
			System.out.println("state1 " + state1[index] + state1[index+1]);
			System.out.println("res " + nextState[0] + nextState[1]);
			System.out.println("++++++++++++++++++++++++++++");

		}

		if (index==26 ) {
			System.out.println("ruleIndex " +ruleIndex);
			System.out.println("rulset " + ruleset[ruleIndex]);
			System.out.println("MSB " + MSB);
			System.out.println("LSB " +LSB );
		}*/

		return nextState;
	}



	public static int[] globalstep(int[] state1, int[] state2, int[] gState, int[] fineRuleset, int[] bulkRuleset) {
		int[] newState = new int[state1.length];

		for(int i=0; i<state1.length; i++) {
			/*Sondefall: wenn die letzte Zelle fette Granularität haben sollte, dann interpretiere sie nur als dünne Zelle*/
			if (i == state1.length-1 ){
				newState[i] = localstep_fine(state1, state2, i, fineRuleset);

			/*bulky cells*/
			} else if ( (gState[i] == 1 && gState[i+1] == 1) || (gState[i] == 0 && gState[i+1] == 0) ) {
				int[] tmp = localstep_bulk(state1, state2, i , bulkRuleset);
				newState[ i ] = tmp[1];
				newState[i+1] = tmp[0];

				/*
				if(i==26) {
					System.out.println("globalstep");
					System.out.println(newState[i] +""+ newState[i+1]);
					System.out.println(tmp[0] +"" +tmp[1]);
				}*/

				i++;

			/*fine cell*/
			} else {
				newState[i] = localstep_fine(state1, state2, i, fineRuleset);
			}
		}

		return newState;
	}

	public static int[] gState_step (int[] gState, int[] state) {
		int[] newState = new int[gState.length];

		if (gState.length != state.length) {
			System.out.println("gState_step: unequal length");
			Arrays.fill(newState, -1);
			return newState;
		}

		for(int i=0; i<gState.length; i++) {
			newState[i] = (gState[i] + state[i]) % 2;
		}
		return newState;
	}

}