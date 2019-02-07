package com.progress.common.util;
import java.lang.reflect.*;

public class Typing
{
    private static void listMembersI (Class c)
    {
        System.out.println ( "  Members from " + c.toString() + ":" );
        {
            Constructor[] constructors = c.getDeclaredConstructors();
            int l = constructors.length;
            if ( l != 0 )
            {
                System.out.println ( "    Constructors:");
                for ( int i = 0; i < l; i++ )
                {
                    Constructor con = constructors[i];
                    System.out.println ( "      " + con.toString() );
                }
            }
        }
        {
            Field[] fields = c.getDeclaredFields();
            int l = fields.length;
            if ( l != 0 )
            {
                System.out.println ( "    Fields:");
                for ( int i = 0; i < l; i++ )
                {
                    Field f = fields[i];
                    System.out.println ( "      " + f.toString() );
                }
            }
        }
        {
            Method[] methods = c.getDeclaredMethods();
            int l = methods.length;
            if ( l != 0 )
            {
                System.out.println ( "    Methods:");
                for ( int i = 0; i < l; i++ )
                {
                    Method m = methods[i];
                    System.out.println ( "      " + m.toString() );
                }
            }
        }
        Class sup = c.getSuperclass();
        if ( sup != null ) listMembersI (sup);
    }
    static public void listMembers (Class c)
    {
        System.out.println ( "Members for " + c.toString() );
        listMembersI (c);
        System.out.println();
    }
    static public boolean subclass   (String subS, String supS)
    {
            Class sub, sup;
            try { sub = Class.forName(subS); }
                catch ( ClassNotFoundException xcp ) { return false; }
            try { sup = Class.forName(supS); }
                catch ( ClassNotFoundException xcp ) { return false; }
            //Tools.listMembers (sub);
            //Tools.listMembers (sup);
            return subclass (sub, sup);
    }
    static public boolean subclass   (Class sub, Class sup)
    {
            //String subClass = sub.getName();
            //String supClass = sup.getName();
            if ( sub == sup ) return true;
            Class subSup = sub.getSuperclass ();
            if ( subSup == null ) return false;
            return subclass ( subSup, sup);
    }
    static public boolean subtype    (Class sub, Class sup)
    {
            String subClass = sub.getName();
            String supClass = sup.getName();
            if ( sub == sup ) return true;

            Class subSup = sub.getSuperclass ();
            if ( subSup != null )
            {
                if ( subtype ( subSup, sup) ) return true;
            }

            Class[] subInt = sub.getInterfaces ();
            for ( int i = 0; i < subInt.length; i++ )
            {
                if ( subtype ( subInt[i], sup) ) return true;
            }

            return false;
    }
    static public boolean instance   (Object obj, Class sup)
    {
            Class sub = obj.getClass();
            return subtype ( sub, sup);
    }
    static public boolean instanceProper (Object obj, Class sup)
    {
            Class sub = obj.getClass();
            return subclass ( sub, sup);
    }
}



