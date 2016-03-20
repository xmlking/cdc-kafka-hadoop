package testapp

class Author {
    String firstName;
    String lastName;
    String email

    static hasMany = [books: Book]

    static constraints = {
        email email: true, blank: false
    }
}
