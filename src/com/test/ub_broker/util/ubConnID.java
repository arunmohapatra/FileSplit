/*************************************************************/
/* Copyright (c) 1984-2000 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation.*/
/*************************************************************/
/* * $Id: ubConnID.java,v 1.0 2000/03/29  */
/*********************************************************************/
/* Module : ubConnID                                                 */
/* It creates and manages parts defined in a connection id.          */
/*********************************************************************/
package com.progress.ubroker.util;
public class ubConnID
{
  public static String DELIMITER = "::";
  public String m_wholeID;
  public String m_hostName;
  public String m_brokerName;
  public int    m_portNumber;
  public String m_uuid;
 
  public ubConnID()  
  {
    m_wholeID    = null;
    m_hostName   = null;
    m_brokerName = null;
    m_portNumber = 0;
    m_uuid       = null;  
  }  

  //
  // Assumption here is that the specified connection id, inConnID, is
  // well-formated connection id, so we can parse the informatio into 
  // known parts.
  //
  public ubConnID(String inConnID)
  {
    m_wholeID = inConnID;
    parseParts();
  }
  
  public String create(String inHostName, String inBrokerName,
                       int inPortNumber, String inUUID)
  {
    m_hostName = inHostName;
    m_brokerName = inBrokerName;
    m_portNumber = inPortNumber;
    m_uuid = inUUID;
    m_wholeID = makeWholeID();
    return m_wholeID;
  }
  
  private String makeWholeID()
  {   
     return(m_hostName + DELIMITER + m_brokerName + DELIMITER + 
              m_portNumber + DELIMITER + m_uuid);
  }
  
  
  private void parseParts()
  {
    String workString = m_wholeID;
    String partString;    int nextPartEnd = 99;
    int whichPart = 0;
    int delimiterLength = DELIMITER.length();    
  
    while (nextPartEnd > 0)
    {
      nextPartEnd = workString.indexOf(DELIMITER);
      if (nextPartEnd > 0)
      {
        partString = workString.substring(0,nextPartEnd);
        workString = workString.substring(nextPartEnd + delimiterLength);
      }
      else
         partString = workString;
      switch (whichPart)
      {
        case 0:
          m_hostName = partString;
          break;
        case 1:
          m_brokerName = partString;
          break;
        case 2:
          m_portNumber = (new Integer(partString)).intValue();
          break;
        case 3:
          m_uuid = partString;
      }
      whichPart++;
    }
  } 
 }


