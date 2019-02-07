//**************************************************************
//  Copyright (c) 1984-2015 by Progress Software Corporation
//  All rights reserved.  No part of this program or document
//  may be  reproduced in  any form  or by  any means without
//  permission in writing from Progress Software Corporation.
//*************************************************************
//
//
// @(#)ICmdConst   0.1  7/17/98
//
// Defines the command line option constants for command line
// utilities.
//
//
package com.progress.common.util ;

public interface ICmdConst
{
    public final static int OPT_QUERY       = 10 ;
    public final static int OPT_START       = 20 ;
    public final static int OPT_STOP        = 30 ;
    public final static int OPT_HELP        = 40 ;
    public final static int OPT_NAME        = 50 ;
    public final static int OPT_USER        = 60 ;
    public final static int OPT_ADDSRVRS    = 70 ;
    public final static int OPT_TRIMSRVRS   = 80 ;
    public final static int OPT_HOST        = 90 ;
    public final static int OPT_PORT        = 100 ;
    public final static int OPT_PERSON      = 110 ;
    public final static int OPT_PROP        = 120 ;
    public final static int OPT_MSGR        = 130 ;
    public final static int OPT_VALI        = 140 ;
    public final static int OPT_MSGRNOVAL   = 150 ;
    public final static int OPT_DATABASE    = 160 ;
    public final static int OPT_CONFIG      = 170 ;
    public final static int OPT_KILL        = 180 ;
    public final static int OPT_TIMEOUT     = 190 ;
    public final static int OPT_BRIEF       = 200 ;
    public final static int OPT_ALL         = 210 ;
    public final static int OPT_PASSWORD    = 220 ;
    public final static int OPT_VERBOSE     = 230 ;
    public static final int OPT_DEPLOY      = 240 ;
    public static final int OPT_UNDEPLOY    = 250 ;
    public static final int OPT_LIST        = 260 ;
    public static final int OPT_FNAME       = 270 ;
    public static final int OPT_WSMFILE     = 280 ;
    public static final int OPT_TARGETURI   = 290 ;
    public static final int OPT_WSAURL      = 300 ;
    public static final int OPT_GETSTATS    = 310 ;
    public static final int OPT_RESETSTATS  = 320 ;
    public static final int OPT_VALUE       = 330 ;
    public static final int OPT_GETDEFAULTS = 340 ;
    public static final int OPT_SETDEFAULTS = 350 ;
    public static final int OPT_GETPROPS    = 360 ;
    public static final int OPT_SETPROPS    = 370 ;
    public static final int OPT_IMPORT      = 380 ;
    public static final int OPT_EXPORT      = 390 ;
    public static final int OPT_ENABLE      = 400 ;
    public static final int OPT_DISABLE     = 410 ;
    public static final int OPT_RESETDEFS   = 420 ;
    public static final int OPT_TEST        = 430 ;
    public static final int OPT_WSDFILE     = 440 ;
    public static final int OPT_UPDATE      = 450 ;
    public static final int OPT_RESETPROPS  = 460 ;
    public static final int OPT_NAMESPACE   = 470 ;
    public static final int OPT_ENCODING    = 480 ;
    public static final int OPT_WEBSRVRAUTH = 490 ;
    public static final int OPT_PING        = 500 ;
    public static final int OPT_DOMAIN      = 510 ;
    public static final int OPT_INSTALL     = 520 ;
    public static final int OPT_DEBUG       = 530 ;
    public static final int OPT_AGENT       = 540 ;
    public static final int OPT_VST         = 550 ;
    public static final int OPT_LISTCLIENTS = 560 ;
    public static final int OPT_CLIENTDETAIL = 570 ;
    public static final int OPT_LISTPROPNAME = 580 ;
    public static final int OPT_LISTPROPVAL  = 590 ;
    public static final int OPT_AGENTDETAIL = 600 ;
    public static final int OPT_AGENTSTOP   = 610 ;
    public static final int OPT_AGENTKILL   = 620 ;
    public static final int OPT_AGENTDUMP   = 630 ;
    public static final int OPT_WARFILE     = 640 ;
    public static final int OPT_GETLOGS     = 650 ;
    public static final int OPT_LOGSTARTOFFSET   = 660 ;
    public static final int OPT_LOGENDOFFSET     = 670 ;
    public static final int OPT_LOGMAX           = 680 ;
    public static final int OPT_WEBAUTHVAL       = 690 ;
    public static final int OPT_REPUBLISH        = 700 ;
    public static final int OPT_NEWFILEPATH      = 710 ;
    public static final int OPT_PAARNAME         = 720 ;
    public static final int OPT_UNPUBLISH        = 730 ;
    public static final int OPT_PROJ             = 740 ;
    public static final int OPT_RESTSVC_NAMES    = 750 ;
    public static final int OPT_MOBSVC_NAMES     = 760 ;
    public static final int OPT_MOBAPP_NAMES     = 770 ;
    public static final int OPT_TARGETZIPORWAR   = 780 ;
    public static final int OPT_INCLUDE_JARS     = 790 ;
    public static final int OPT_GENPAAR          = 800 ;
    public static final int OPT_GENMOBPAAR       = 810 ;    
    public static final int OPT_GENRESTWAR       = 820 ;
    public static final int OPT_GETMOBWAR        = 830 ;
    public static final int OPT_GENMOBAPPWAR     = 840 ;
    public static final int OPT_GENONLYMOBAPPWAR = 850 ;
    public final static int OPT_STATUS     	 = 860 ;
    public static final int OPT_FORMAT         = 870 ;
    public static final int OPT_REFRESH        = 880 ;
    public static final int OPT_CANCELREFRESH  = 890 ;
    public static final int OPT_REFRESHSTATUS  = 900 ;    
    
    public final static int UNKOPT          = (int) '?' ;
}
