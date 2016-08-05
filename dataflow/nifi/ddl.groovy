import org.apache.nifi.components.state.Scope

def ver = "v1";
def flowFile = session.get();
if (!flowFile) return

def stateManager = context.getStateManager();
def stateMap = stateManager.getState(Scope.CLUSTER);
log.error(stateMap as String);

log.error("version: " +  stateMap.getVersion())
if (stateMap.getVersion() < 0) {
    stateManager.setState(["test.shop":ver], Scope.CLUSTER);

} else {
    ver = stateMap.get("test.shop");
}


log.error(ver);

//def newStateMap = new HashMap<>(stateManager.getState(Scope.CLUSTER).toMap());
//newStateMap["test.shop"] = "v1";
def newStateMap = ["test.shop":"v2"]
stateManager.replace(stateMap, newStateMap, Scope.CLUSTER);

session.transfer(flowFile, REL_SUCCESS);
