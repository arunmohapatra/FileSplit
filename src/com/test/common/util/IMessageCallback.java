package com.progress.common.util;
public interface IMessageCallback
{
        void handleMessage (String message);
        void handleMessage (int level, String message);
        void handleException (Throwable excp, String message);
}

