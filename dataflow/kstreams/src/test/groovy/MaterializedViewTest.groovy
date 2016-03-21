import spock.lang.Specification

class MaterializedViewTest extends Specification{
    def "someLibraryMethod returns true"() {
        setup:
        MaterializedView mview = new MaterializedView()
        when:
        def result = mview.testTrue()
        then:
        result == true
    }
}
