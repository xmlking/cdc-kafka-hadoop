package testapp

class Shop implements Serializable{
    String name;
    String owner;
    String phoneNumber;

    static constraints = {
    }

    static mapping = {
        //id composite: ['id', 'name']
    }
}
