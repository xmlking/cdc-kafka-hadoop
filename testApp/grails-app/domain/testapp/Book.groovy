package testapp


class Book {
    String title
    Integer pages
    Date datePublished
    Author author

    static belongsTo = Author

    static constraints = {
        datePublished max: new Date()
    }
}
