import com.crossbusiness.nifi.processors.NiFiUtils as util

def flowFile = session.get()
if(!flowFile) return

log.debug util.flowFileToString(flowFile, session);
