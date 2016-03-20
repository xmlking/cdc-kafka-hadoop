NiFi Dataflow
=============

This [flow](./cdc-flow.xml) depends on **nifi-scripting** module, download [nar](https://github.com/xmlking/nifi-scripting/releases) and copy to `$NIFI_HOME/lib`

ExecuteJavaScript's JSON transformation logic:

```js
// logical change record (LCR)
var lcr = util.flowFileToString(flowFile, session);
lcr = JSON.parse(lcr);

var attMap = new java.util.HashMap();
attMap.put('commit', lcr.commit.toString());
attMap.put('database', lcr.database);
attMap.put('table', lcr.table);
attMap.put('ts', lcr.ts.toString());
attMap.put('id', lcr.data.id.toString());
attMap.put('type', lcr.type);
attMap.put('xid', lcr.xid.toString());

session.remove(flowFile);
flowFile = util.stringToFlowFile(JSON.stringify(lcr.data) , session, flowFile);
flowFile = session.putAllAttributes(flowFile, attMap);
```

![cdc dataflow](./cdc-flow.png)
