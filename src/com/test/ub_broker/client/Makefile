#%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
# Makefile - java/com/progress/ubroker/client
#%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
ifeq ($(TOPLEVEL),)
  local_targets = $(java/com/progress/ubroker/client.all)
  JBUILD	:= yes
  JBUILD_ONLY	:= yes
  include ${RDLSRC}/Makefile
else
#%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
# Local Targets

java/com/progress/ubroker/client_CLASSES := $(addsuffix ${CLASS}, \
	$(addprefix ${CMKCLASSDIR}/com/progress/ubroker/client/, \
	BrokerSystem \
	CompressionException \
	NetworkClientProtocol \
	TcpClientProtocol \
	TcpClientMsgInputStream \
	TcpClientMsgOutputStream \
	HttpClientProtocol \
	HttpClientMsgInputStream \
	HttpClientMsgOutputStream \
	))

#%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

java/com/progress/ubroker/client.all	:= $(java/com/progress/ubroker/client_CLASSES)

.PHONY					: java/com/progress/ubroker/client
java/com/progress/ubroker/client	: $(java/com/progress/ubroker/client.all)

#%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
endif

