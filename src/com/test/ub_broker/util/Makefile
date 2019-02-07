#%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
# Makefile - java/com/progress/ubroker/util
#%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
ifeq ($(TOPLEVEL),)
  local_targets = $(java/com/progress/ubroker/util.all)
  JBUILD	:= yes
  JBUILD_ONLY	:= yes
  include ${RDLSRC}/Makefile
else
#%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
# java/com/progress/ubroker/util specific compile flags

java/com/progress/ubroker/util_CLASSPATH := $(CMKJAVASRC)/extjars/cryptoj.jar${PD}$(CMKSONICKIT)/sonicmq/lib/mgmt_config.jar${PD}$(CMKSONICKIT)/sonicesb/lib/xq_config.jar${PD}$(CMKSONICKIT)/sonicesb/lib/xq_core.jar${PD}$(CMKSONICKIT)/sonicmq/lib/mgmt_client.jar${PD}$(CMKJAVASRC)/extjars/regexp.jar${PD}$(CMKJAVASRC)/extjars/servlet.jar${PD}$(CMKJAVASRC)/extjars/activation.jar${PD}$(CMKJAVASRC)/extjars/mail.jar${PD}$(CMKJAVASRC)/extjars/xercesImpl.jar${PD}$(CMKJAVASRC)/extjars/xmlParserAPIs.jar${PD}$(CMKJAVASRC)/extjars/soap.jar${PD}$(CMKJAVASRC)/extjars/wsdl4j.jar${PD}$(CMKJAVASRC)/extjars/wsdl4j.jar

#%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
# Local Targets

java/com/progress/ubroker/util_CLASSES := $(addsuffix ${CLASS}, \
	$(addprefix ${CMKCLASSDIR}/com/progress/ubroker/util/, \
	Base64 \
	CertLoader \
	CfgValidateErrs \
	DynamicPropertyValues \
	IDynamicPropertyValues \
	ILoggable \
	INetworkProtocol \
	IPUMessages \
	IPropConst \
	IPropFilename \
	ISSLParams \
	ISSLSocketUtils \
	IToolCmdConst \
	IUBLogfileInfo \
        IUbMsgChannel \
	IWatchable \
	IubMsgInputStream \
	IubMsgOutputStream \
	IubThreadFactory \
	List \
	Logger \
	MessageCompressor \
	MsgInputStream \
	MsgOutputStream \
        MsgReader \
	NetworkProtocolException \
	NetworkProtocolFactory \
	NetworkProtocolOptions \
	ObjectHolder \
	PropFilename \
	PropGroupDescriptor \
	PropMgrUtils \
	Queue \
	Request \
	RequestQueue \
	SocketConnectionInfo \
	SocketConnectionInfoEx \
	ToolRemoteCmdDescriptor \
	ToolRemoteCmdStatus \
	UBMetaSchema \
        UbMsgChannel \
	UBPreferenceProperties \
	UBSchemaMgr \
	getServerCnt \
	ubAdminMsg \
	ubAppServerMsg \
	ubConnID \
	ubConstants \
	ubMsg \
	ubMsgTrace \
	ubProperties \
	ubThread \
	ubThreadQueue \
	ubThreadStats \
	ubWatchDog \
	ubWebSpeedMsg \
	ubFileWatchDog \
	ubParentWatchDog \
	ubRMIWatchDog \
	LogEvntHandler \
	ubRqInfo \
	))

#%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

java/com/progress/ubroker/util.all	:= $(java/com/progress/ubroker/util_CLASSES)

.PHONY					: java/com/progress/ubroker/util
java/com/progress/ubroker/util		: $(java/com/progress/ubroker/util.all)

#%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
endif

