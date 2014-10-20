package com.geneix.sandbox;

/**
 * Created by andrew on 07/10/14.
 */
public class Main {
    private final static char[][] wordDelimiters = {
            Character.f(0x0009),
            Character.toChars(0x000A),
            Character.toChars(0x000B),
            Character.toChars(0x000C),
            Character.toChars(0x000D),
            Character.toChars(0x0020),
            Character.toChars(0x0085),
            Character.toChars(0x00A0), // No break space .. maybe not break on this
            Character.toChars(0x1680),
            Character.toChars(0x2000),
            Character.toChars(0x2001),
            Character.toChars(0x2002),
            Character.toChars(0x2003),
            Character.toChars(0x2004),
            Character.toChars(0x2005),
            Character.toChars(0x2006),
            Character.toChars(0x2007),
            Character.toChars(0x2008),
            Character.toChars(0x2009),
            Character.toChars(0x200A),
            Character.toChars(0x2028),
            Character.toChars(0x2029),
            Character.toChars(0x202F),
            Character.toChars(0x205F),
            Character.toChars(0x3000),
    };
    public static void main(String[] args) throws Exception {
        for (int i=0; i< wordDelimiters.length; i++){
            System.out.print(i);
            System.out.print(' ');
            System.out.print(wordDelimiters[i].length);
            System.out.print(wordDelimiters[i][0]);
            System.out.print(wordDelimiters[i][0]);
            System.out.println();
        }


    }
}
