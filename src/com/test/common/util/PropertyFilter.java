
/*
**  The PropertyFilter class implements the IPropertyValueFilter
**  interface as defined in the PropertyManager. This filter will
**  perform value substitution for NT registry values and UNIX
**  environment values, system properties and group properties.
**
**  The format of a property that gets filtered as as follows:
**    NT: propName=@{Startup\DLC}\bin\_progres.exe
**  UNIX: propName=${DLC}\bin\_progres.exe
**        propName=!{SystemProperty:FOO}
**        propName=!{value-of:group.property}
**        propName=XXX?<value>?<value>?<value>?YYY
*/


package com.progress.common.util;

import java.lang.*;
import java.util.*;
import com.progress.common.util.*;
import com.progress.common.property.*;


public class PropertyFilter implements PropertyManager.IPropertyValueFilter
{
   // constructor to create an instance of Environment.
   public PropertyFilter()
   {
    Env = new Environment();
    m_propMgr = null;
   }

   // to correctly process !{value-of:group.property}, we need a PropertyManager
   public PropertyFilter(PropertyManager p)
   {
    Env = new Environment();
    m_propMgr = p;
   }


   public String filterValue(final String groupName, final String propertyName, String value)
   {
        int i;
        String newValue = value;
        String osName = System.getProperty("os.name");

        /*
         * First - see if any filters were specified.
         */
        if (osName.startsWith("Windows") )
        {
            if (value.indexOf('$') < 0 &&
                value.indexOf('!') < 0 &&
                value.indexOf('?') < 0 &&
                value.indexOf('@') < 0)
               return value;
        }
        else
        {
            if ((value.indexOf('$') < 0 &&
                 value.indexOf('!') < 0 &&
                 value.indexOf('?') < 0 ) )
                return value;
        }


        /*
         * Look for "value-of".  If present, replace with the
         * current value of this property.
         *
         * This processes the following syntax:
         *      propertyName=XXX!{value-of:group.property}YYY
         */
        i = newValue.toLowerCase().indexOf(VALUEOF.toLowerCase() );

        while (i >= 0)
        {
            String propName = null;
            String newGroup = null;
            String currValue = null;
            int end = newValue.indexOf('}', i + VALUEOF.length() );

            String fullName = newValue.substring(i+VALUEOF.length(), end ).trim();

            /*
            **  Handle the following cases:
            **      !{value-of:group.name}     no defaults applied
            **      !{value-of:name}           default to current group
            **      !{value-of:group.}         default to current name
            **      !{value-of:.name}          default to current group
            **      !{value-of:.}              replace with null translation
            */
            if (fullName.lastIndexOf('.') < 0)
            {
                propName = fullName;    /* no group specified */
            }
            else
            {
                propName = fullName.substring(fullName.lastIndexOf('.') + 1 ).trim();
                newGroup = fullName.substring( 0, fullName.lastIndexOf('.') ).trim();
            }

            if (propName == null || propName.length() == 0)
                propName = propertyName;

            if (newGroup == null || newGroup.length() == 0)
                newGroup = groupName;

            if (m_propMgr == null ||
                (propName.equals(propertyName) &&
                 newGroup.equals(groupName) ) )
                 ;          /* avoid infinite translation */
            else
            {
                String currGroup = m_propMgr.getCurrentGroup();
                m_propMgr.setCurrentGroup(newGroup);
                currValue = m_propMgr.getProperty(propName,false);
                m_propMgr.setCurrentGroup(currGroup);
            }

            if (currValue == null)
                currValue = "";       // default value

            newValue = newValue.substring(0,i) +
                       currValue +
                       newValue.substring( end + 1, newValue.length() );
            i = newValue.toLowerCase().indexOf(VALUEOF.toLowerCase() );
        }


        /*
         * Look for "Registry Variable".  If present, replace with the
         * current value of the variable from the NT registry.
         *
         * This processes the following syntax:
         *      propertyName=XXX@{FOO}YYY
         */
        if (osName.startsWith("Windows"))
        {
            i = newValue.indexOf("@{");

            while (i >= 0)
            {
                int end = newValue.indexOf('}', i );

                String propName = newValue.substring(i, end + 1 );
                String currValue = Env.expandPropertyValueJNI(propName);
                if (currValue == null)
                    currValue = "";

                newValue = newValue.substring(0,i) +
                           currValue +
                           newValue.substring(end + 1, newValue.length() );

                i = newValue.indexOf("@{");
            }
        }


        /*
         * Look for "Environment Variable".  If present, replace with the
         * current value of the environment variable.
         *
         * This processes the following syntax:
         *      propertyName=XXX${FOO}YYY
         */
        i = newValue.indexOf("${");

        while (i >= 0)
        {
            int end = newValue.indexOf('}', i );

            String propName = newValue.substring(i, end + 1 );
            String currValue = Env.expandPropertyValueJNI(propName);
            if (currValue == null)
                currValue = "";

            newValue = newValue.substring(0,i) +
                       currValue +
                       newValue.substring(end + 1, newValue.length() );

            i = newValue.indexOf("${");
        }


        /*
         * Environment variables may also start with just a $.
         */
        if (newValue.indexOf('$') >= 0)
        {
            String newValueBackup = newValue;
            newValue = Env.expandPropertyValueJNI(newValue);
            if (newValue == null || newValue.length() == 0)
                newValue = newValueBackup;
        }

        /*
         * Look for "SystemProperty".  If present, replace with the
         * current value of the system property.
         *
         * This processes the following syntax:
         *      propertyName=XXX!{SystemProperty:FOO}YYY
         */
        i = newValue.toLowerCase().indexOf(SYSVALUE.toLowerCase() );

        while (i >= 0)
        {
            String currValue = "";
            int end = newValue.indexOf('}', i + SYSVALUE.length() );

            String propName = newValue.substring(i + SYSVALUE.length(), end ).trim();
            if (propName.length() > 0)
                currValue = System.getProperty(propName);

            newValue = newValue.substring(0,i) +
                       currValue +
                       newValue.substring(end + 1, newValue.length() );

            i = newValue.toLowerCase().indexOf(SYSVALUE.toLowerCase() );
        }


        /*
         * Finally, look for a "?".  At this point, all translations are complete.
         * If a ? is found, eat successive ? characters until a value is found.
         * Add this value to the string and ignore the remaining translations.
         *
         * This processes the following syntax:
         *      propertyName=XXX?<value>?<value>?<value>?YYY
         *
         *  where <value> is any of the previously processed translations:
         *      !{value-of:group.property}
         *      !{SystemProperty:FOO}
         *      ${environ-var}
         *      @{registry-var}   (NT only)
         */

        i = newValue.indexOf('?');

        if (i >= 0)
        {
            String currValue = "";
            int end = newValue.lastIndexOf('?');
            if (end == i)
                end = newValue.length();
            int j = i;

            while (j < end )
            {
                if (newValue.charAt(j) != '?')  // eat ? characters
                    break;
                j++;
            }

            int k = newValue.indexOf('?', j + 1);
            if (k >= 0)
                currValue = newValue.substring(j,k);

            newValue = newValue.substring(0,i) +
                       currValue +
                       newValue.substring( end + 1, newValue.length() );
        }

    /*
     * Done processing filters.
     */

    return newValue;

   }
   private Environment Env;
   private PropertyManager m_propMgr;
   private static final String SYSVALUE = "!{SystemProperty:";
   private static final String VALUEOF = "!{value-of:";
}
