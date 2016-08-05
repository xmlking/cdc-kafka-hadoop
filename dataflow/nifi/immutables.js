var util = Java.type("com.crossbusiness.nifi.processors.NiFiUtils");

var flowFile = session.get();

if (flowFile !== null) {


    function isPk(key) {
        return key.startsWith("pk.")
    }

    function extractPk(keyJson) {
        return Object.keys(keyJson).filter(isPk);
    }

    var key = flowFile.getAttribute("kafka.key");
    keyJson = JSON.parse(key);

    var payload = [];
    extractPk(keyJson).forEach(function (akey) {
        payload.push(akey, keyJson[akey]);
    });

    var lcr = util.flowFileToString(flowFile, session);
    lcr = JSON.parse(lcr);

    var attMap = new java.util.HashMap();
    attMap.put('database', lcr.database);
    attMap.put('table', lcr.table);
    attMap.put('type', lcr.type);
    attMap.put('ts', lcr.ts.toString());
    attMap.put('correlation-identifier', lcr.database + "-" + lcr.table + "-" + lcr.type);


    payload.unshift(attMap['ts'], lcr.type, lcr.database, lcr.table);
    payload = payload.join(", ");

    Object.keys(lcr.data).forEach(function (key) {
        var fFile = util.stringToFlowFile(payload + ", " + key + ", " + lcr.data[key], session);
        fFile = session.putAllAttributes(fFile, attMap);
        fFile = session.putAttribute(fFile, 'key', key);
        session.transfer(fFile, REL_SUCCESS)
    });

    session.remove(flowFile);
}
