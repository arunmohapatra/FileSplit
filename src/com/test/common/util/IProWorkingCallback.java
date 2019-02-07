package com.progress.common.util;

public interface IProWorkingCallback
{
    public final int WORKING_DONE    = 0;
    public final int WORKING_CANCEL  = 1;
    
    public void workingCallback( Integer status );
}
 
// END OF FILE
 
