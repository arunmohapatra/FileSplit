// NtKrnlException.java

package com.progress.common.util;

/*********************************************************************/
/*                                                                   */
/*********************************************************************/

public class NtKrnlException 
    extends com.progress.common.exception.ProException
    {
    public NtKrnlException(String detail)
        {
        super("NtKrnlException", new Object[] { detail } );
        };


    public String getDetail()
        {
        return (String)getArgument(0);
        }
    }

